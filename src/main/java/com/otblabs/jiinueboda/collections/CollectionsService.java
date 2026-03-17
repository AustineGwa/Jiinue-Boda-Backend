package com.otblabs.jiinueboda.collections;

import com.otblabs.jiinueboda.dashboard.models.DailyPaymentTracker;
import com.otblabs.jiinueboda.collections.models.MonthlyGeneralPerformance;
import com.otblabs.jiinueboda.dashboard.models.MonthlyPerformance;
import com.otblabs.jiinueboda.collections.models.*;
import com.otblabs.jiinueboda.loans.models.OldAccountNewAccountPayment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CollectionsService {

    private final JdbcTemplate jdbcTemplateOne;


    public CollectionsService(JdbcTemplate jdbcTemplateOne) {
        this.jdbcTemplateOne = jdbcTemplateOne;
    }

    public List<UserTransaction> getTopTransactions(int num) {
        String sql = "SELECT FirstName, TransID,BillRefNumber,TransAmount,created_at FROM mpesa_c2b ORDER  BY created_at DESC LIMIT ?";
        return jdbcTemplateOne.query(sql,(rs,i)->setTransaction(rs),num);

    }

    public UserTransaction getSingleTransaction(String transactionId) {
        String sql = "SELECT FirstName, TransID,BillRefNumber,TransAmount,created_at FROM mpesa_c2b WHERE TransID =? ";
        return jdbcTemplateOne.queryForObject(sql,(rs,i)->setTransaction(rs),transactionId);

    }

    private UserTransaction setTransaction(ResultSet rs) throws SQLException {
        UserTransaction userTransaction = new UserTransaction();
        userTransaction.setName(rs.getString("FirstName"));
        userTransaction.setTransactionId(rs.getString("TransID"));
        userTransaction.setLoanId(rs.getString("BillRefNumber"));
        userTransaction.setAmount(rs.getInt("TransAmount"));
        userTransaction.setTime(rs.getString("created_at"));
        return userTransaction;
    }

    public List<PendingCollection> getUnReconciledCollections() throws Exception{
        String sql = """
          SELECT c.FirstName,
                 mht.number as MSISDN,
                 c.BillRefNumber as LoanID,
                 c.TransID,
                 c.TransTime, c.TransAmount
          FROM (
                  SELECT *
                  FROM mpesa_c2b
                  WHERE BillRefNumber NOT IN (SELECT loanAccountMPesa FROM loans)
                    AND BillRefNumber NOT IN (SELECT loanID FROM fuel_loan)
                    AND TransAmount < 30000
                    AND BillRefNumber != 'JWINV2'
               ) c
          LEFT JOIN useful_mpesa_hash_table mht
                 ON c.MSISDN = mht.hash
          ORDER BY c.created_at DESC;
        """;
        return jdbcTemplateOne.query(sql,(rs,i)->setPendingCollections(rs));
    }

    public List<PendingCollection> getUnReconciledCollections(int year) throws Exception{
        String sql = """
          SELECT c.FirstName,
                                   mht.number as MSISDN,
                                   c.BillRefNumber as LoanID,
                                   c.TransID,
                                   c.TransTime, c.TransAmount
                            FROM (
                                    SELECT *
                                    FROM mpesa_c2b
                                    WHERE
                                      YEAR(created_at) = ?
                                      AND BillRefNumber NOT IN (SELECT loanAccountMPesa FROM loans)
                                      AND BillRefNumber NOT IN (SELECT loanID FROM fuel_loan)
                                      AND TransAmount < 30000
                                      AND BillRefNumber != 'JWINV2'
                                 ) c
                            LEFT JOIN useful_mpesa_hash_table mht
                                   ON c.MSISDN = mht.hash
                            ORDER BY c.created_at DESC;
        """;
        return jdbcTemplateOne.query(sql,(rs,i)->setPendingCollections(rs), year);
    }

    public List<PendingCollection> getUnReconciledCollections(int month, int year) throws Exception{
        String sql = """
          SELECT c.FirstName,
                                   mht.number as MSISDN,
                                   c.BillRefNumber as LoanID,
                                   c.TransID,
                                   c.TransTime, c.TransAmount
                            FROM (
                                    SELECT *
                                    FROM mpesa_c2b
                                    WHERE
                                      MONTH(created_at) = ? AND YEAR(created_at) = ?
                                      AND BillRefNumber NOT IN (SELECT loanAccountMPesa FROM loans)
                                      AND BillRefNumber NOT IN (SELECT loanID FROM fuel_loan)
                                      AND TransAmount < 30000
                                      AND BillRefNumber != 'JWINV2'
                                 ) c
                            LEFT JOIN useful_mpesa_hash_table mht
                                   ON c.MSISDN = mht.hash
                            ORDER BY c.created_at DESC;
        """;
        return jdbcTemplateOne.query(sql,(rs,i)->setPendingCollections(rs), month,year);
    }

    private PendingCollection setPendingCollections(ResultSet rs) throws SQLException {
        PendingCollection pendingCollection = new PendingCollection();
        pendingCollection.setFirstName(rs.getString("FirstName"));
        pendingCollection.setPhoneNumber(rs.getString("MSISDN"));
        pendingCollection.setLoanID(rs.getString("LoanID"));
        pendingCollection.setTransID(rs.getString("TransID"));
        pendingCollection.setTransTime(rs.getString("TransTime"));
        pendingCollection.setTransactionAmount(rs.getString("TransAmount"));
        return pendingCollection;
    }

    public boolean postTransaction(ReconciliationData reconciliationData) throws Exception{

        String sql = "UPDATE mpesa_c2b SET BillRefNumber=?, is_manual=1, updated_at=NOW() WHERE BillRefNumber=? AND TransID=?";
        jdbcTemplateOne.update(sql,reconciliationData.getCorrectLoanId(),reconciliationData.getWrongLoanId(),reconciliationData.getTransactionId());
        return true;
    }

    public void updateDailyExpectedCollection(){
        String sql = """
                INSERT INTO daily_expected_summary (date, daily_expected)
                SELECT
                            current_date,
                            COALESCE(SUM(
                                             CASE
                                                 WHEN l.disbursed_at <= current_date THEN
                                                     CASE
                                                         WHEN (l.client_loan_total - IFNULL(
                                                                 (SELECT SUM(m.TransAmount)
                                                                  FROM mpesa_c2b m
                                                                  WHERE (l.loanAccountMPesa = m.BillRefNumber)
                                                                    AND DATE(m.TransTime) <= current_date
                                        ), 0)) > 0
                                                             THEN l.daily_amount_expected
                                                         ELSE 0
                                                         END
                                                 ELSE 0
                                                 END
                                         ), 0) as daily_expected
                FROM loans l
                ON DUPLICATE KEY UPDATE daily_expected = VALUES(daily_expected);
                """;
        jdbcTemplateOne.update(sql);
    }



    public List<DefaultList> getDefaultsList() {
        String query = """
                        select id,first_name,last_name,phone, Account,
                                                TotalPaid,totalExpected,
                                                IF(TotalPaid > totalExpected, 0, totalExpected - TotalPaid)                as variance,
                                                IF(TotalPaid > totalExpected, 0, totalExpected - TotalPaid) / dailyExpected as varRatio,
                                                LoanBalance,Status,loanAge,patner_id,
                                                dailyExpected
                                                from (
                                                select
                                                u.id,u.first_name, u.last_name,u.phone,l.loanAccountMPesa as Account,u.patner_id,
                                                l.paid_amount as TotalPaid,
                                                l.expected_amount as totalExpected,
                                                l.daily_amount_expected  as dailyExpected,
                                                l.loan_balance as LoanBalance,
                                                IF(l.loan_term < DATEDIFF(now(), disbursed_at), 'Overdue  Loan', 'Active Loan') as Status,
                                                DATEDIFF(now(),disbursed_at)  as loanAge
                                                from loans l
                                                inner join users u on u.id = l.userID
                                                where l.loan_balance > 0
                                              AND l.disbursed_at is not null
                                              AND (IF(paid_amount > expected_amount, 0, expected_amount - paid_amount)) > 0                                                  
                                                )x ORDER BY varRatio DESC
                        """;

        return jdbcTemplateOne.query(query, this::mapRowToDefaultList);

    }

    public List<DefaultList> getActiveLoanDetailsForPartner(int partnerID){
        String query = """
                  select id,first_name,last_name,phone, Account,
                                          TotalPaid,totalExpected,
                                          IF(TotalPaid > totalExpected, 0, totalExpected - TotalPaid)                as variance,
                                          IF(TotalPaid > totalExpected, 0, totalExpected - TotalPaid) / dailyExpected as varRatio,
                                          LoanBalance,Status,loanAge,patner_id,dailyExpected
                                          from (
                                          select
                                          u.id,u.first_name, u.last_name,u.phone,l.loanAccountMPesa as Account,u.patner_id,
                                          IFNULL(l.paid_amount,0) as TotalPaid,
                                          IFNULL(l.expected_amount,0) as totalExpected,
                                          l.daily_amount_expected  as dailyExpected,
                                          ifnull(l.loan_balance,0) as LoanBalance,
                                          IF(l.loan_term < DATEDIFF(now(), disbursed_at), 'Overdue  Loan', 'Active Loan') as Status,
                                          DATEDIFF(now(),disbursed_at)  as loanAge
                                          from loans l
                                          inner join users u on u.id = l.userID
                                          where l.loan_balance > 0
                                            AND (IF(l.paid_amount > l.expected_amount, 0, expected_amount - paid_amount)) > 0
                                            AND l.disbursed_at is not null
                                            AND patner_id = ?
                                          )x ORDER BY varRatio DESC
                """;

        return jdbcTemplateOne.query(query, this::mapRowToDefaultList,partnerID);
    }

    private DefaultList mapRowToDefaultList(ResultSet rs, int i) throws SQLException {
        DefaultList loanRecord = new DefaultList();
        loanRecord.setId(rs.getInt("id"));
        loanRecord.setFirstName(rs.getString("first_name"));
        loanRecord.setLastName(rs.getString("last_name"));
        loanRecord.setPhone(rs.getString("phone"));
        loanRecord.setAccount(rs.getString("Account"));
        loanRecord.setTotalPaid(rs.getDouble("TotalPaid"));
        loanRecord.setTotalExpected(rs.getDouble("totalExpected"));
        loanRecord.setVariance(rs.getDouble("variance"));
        double roundedUpToTwoDecimalPlaces = Math.round(rs.getDouble("varRatio") * 100) / 100.0;
        loanRecord.setVarRatio(roundedUpToTwoDecimalPlaces);
        loanRecord.setDailyExpected(rs.getDouble("dailyExpected"));
        loanRecord.setLoanBalance(rs.getInt("LoanBalance"));
        loanRecord.setLoanStatus(rs.getString("Status"));
        loanRecord.setLoanAge(rs.getInt("loanAge"));

        return loanRecord;
    }

    public List<DefaultList> getDefaultsWithCloseOverdue(int daysTillOverdue){
        String query;
        if(daysTillOverdue == 0){
             query = """
                select id,first_name,last_name,phone, Account,
                                        TotalPaid,totalExpected,
                                        IF(TotalPaid > totalExpected, 0, totalExpected - TotalPaid)                as variance,
                                        IF(TotalPaid > totalExpected, 0, totalExpected - TotalPaid) / dailyExpected as varRatio,
                                        LoanBalance,Status,loanAge,patner_id,loan_term,
                                        dailyExpected
                                        from (
                                        select
                                        u.id,u.first_name, u.last_name,u.phone,l.loanAccountMPesa as Account,u.patner_id,
                                        l.paid_amount as TotalPaid,
                                        l.expected_amount as totalExpected,
                                        l.daily_amount_expected  as dailyExpected,
                                        l.loan_balance as LoanBalance,l.loan_term,
                                        IF(l.loan_term < DATEDIFF(now(), disbursed_at), 'Overdue  Loan', 'Active Loan') as Status,
                                        DATEDIFF(now(),disbursed_at)  as loanAge
                                        from loans l
                                        inner join users u on u.id = l.userID
                                        where l.loan_balance > 0
                                          AND l.disbursed_at is not null
                                          AND (IF(paid_amount > expected_amount, 0, expected_amount - paid_amount)) > 0                                          
                                        )x WHERE ((loan_term - loanAge) < ? )ORDER BY varRatio DESC
                """;
        }else {
            query = """
                     select id,first_name,last_name,phone, Account,
                                        TotalPaid,totalExpected,
                                        IF(TotalPaid > totalExpected, 0, totalExpected - TotalPaid)                as variance,
                                        IF(TotalPaid > totalExpected, 0, totalExpected - TotalPaid) / dailyExpected as varRatio,
                                        LoanBalance,Status,loanAge,patner_id,loan_term,
                                        dailyExpected
                                        from (
                                        select
                                        u.id,u.first_name, u.last_name,u.phone,l.loanAccountMPesa as Account,u.patner_id,
                                        l.paid_amount as TotalPaid,
                                        l.expected_amount as totalExpected,
                                        l.daily_amount_expected  as dailyExpected,
                                        l.loan_balance as LoanBalance,l.loan_term,
                                        IF(l.loan_term < DATEDIFF(now(), disbursed_at), 'Overdue  Loan', 'Active Loan') as Status,
                                        DATEDIFF(now(),disbursed_at)  as loanAge
                                        from loans l
                                        inner join users u on u.id = l.userID
                                        where l.loan_balance > 0
                                          AND l.disbursed_at is not null
                                          AND (IF(paid_amount > expected_amount, 0, expected_amount - paid_amount)) > 0                                          
                                        )x WHERE ((loan_term - loanAge) < ? AND (loan_term - loanAge) > 0 ) ORDER BY varRatio DESC
                    """;
        }


        return jdbcTemplateOne.query(query, this::mapRowToDefaultList,daysTillOverdue);
    }

    public void updateLoanTotalExpectedPaySum() {
        String sql = """
                UPDATE loans l
                SET expected_amount = (
                    IF(l.loan_term < DATEDIFF(NOW(), l.disbursed_at),l.loan_term * l.daily_amount_expected,DATEDIFF(NOW(), l.disbursed_at) * l.daily_amount_expected)
                    )
                """;

        jdbcTemplateOne.update(sql);

    }

    public void updateLoanTotalExpectedPaySumForCompletedLoan() {
        String sql = """
                UPDATE loans SET expected_amount = client_loan_total WHERE expected_amount > client_loan_total
                """;

        jdbcTemplateOne.update(sql);

    }


    public void updateLoanBalances() {
        String sql = """
                UPDATE loans l
                    JOIN (
                    SELECT loanAccountMPesa,
                    client_loan_total - IFNULL((SELECT SUM(TransAmount)
                    FROM mpesa_c2b m
                    WHERE m.BillRefNumber = loans.loanAccountMPesa), 0) AS calculated_balance
                    FROM loans
                    ) AS calc
                ON l.loanAccountMPesa = calc.loanAccountMPesa
                    SET l.loan_balance = calc.calculated_balance
                """;

        jdbcTemplateOne.update(sql);
    }

    public void updateAmountPaidPerClient() {

        String sql = """
                UPDATE loans SET paid_amount =(
                                    SELECT IFNULL((select sum(TransAmount) from mpesa_c2b m where (m.BillRefNumber = loanAccountMPesa)),0)
                                )
                """;

        jdbcTemplateOne.update(sql);
    }

    public List<MonthlyPerformance> getMonthlyperformance(int month, int year){
        String sql = """
                SELECT loanAccountMPesa,first_name,middle_name, last_name,phone, group_id, patner_id ,loanPrincipal as principal,loan_term,(DATEDIFF(curdate(),DATE(disbursed_at))) as loan_age,daily_amount_expected,
                       IFNULL(paid_amount,0) as paid_amount, IFNULL(expected_amount,0) as expected_amount,(IFNULL(expected_amount,0) - IFNULL(paid_amount,0)) as balance ,(paid_amount / l.expected_amount) * 100 as repayment_rate, loanPurpose, DATE(disbursed_at) as date_disbursed
                FROM loans l LEFT JOIN users u ON l.userID = u.id  WHERE MONTH(disbursed_at) = ? AND YEAR(disbursed_at) = ? ORDER BY balance desc
                """;

        return jdbcTemplateOne.query(sql,(rs,i)->monthlyperformanceRowMapper(rs),month, year);
    }

    private MonthlyPerformance monthlyperformanceRowMapper(ResultSet rs) throws SQLException {
        MonthlyPerformance performance = new MonthlyPerformance();
        performance.setAccount(rs.getString("loanAccountMPesa"));
        performance.setFirstName(rs.getString("first_name"));
        performance.setMiddleName(rs.getString("middle_name"));
        performance.setLastName(rs.getString("last_name"));
        performance.setPhone(rs.getString("phone"));
        performance.setGroupId(rs.getInt("group_id"));
        performance.setPartnerId(rs.getInt("patner_id"));
        performance.setPrincipal(rs.getInt("principal"));
        performance.setLoanTerm(rs.getInt("loan_term"));
        performance.setLoanAge(rs.getInt("loan_age"));
        performance.setDailyAmountExpected(rs.getInt("daily_amount_expected"));
        performance.setExpectedAmount(rs.getInt("expected_amount"));
        performance.setAmountPaid(rs.getInt("paid_amount"));
        performance.setBalance(rs.getInt("balance"));
        performance.setRepaymentRate(rs.getDouble("repayment_rate"));
        performance.setLoanPurpose(rs.getString("loanPurpose"));
        performance.setDisbursedAt(rs.getString("date_disbursed"));
        return performance;
    }

//    public Map<String,List<LoansByAge>> getAllLoansByVarianceRation(int branch){
//
//        return getLoansByVariance(branch).stream()
//                .collect(Collectors.groupingBy(loan -> {
//                    int varRatio = loan.getVarRatio();
//                    if (varRatio >= 7) return "7+";
//                    if (varRatio < 0) return "0";
//                    return String.valueOf(varRatio);
//                }));
//
//    }

    public Map<String,List<LoansByAge>> getAllLoansByVarianceRation(int branch, int collectionLevel){


        return getLoansByVariance(branch).stream()
                .filter(loan -> checkVarRatio(loan , collectionLevel) )
                .collect(Collectors.groupingBy(loan -> {
                    int varRatio = loan.getVarRatio();
                    if (varRatio > 15) return "16+";
                    if (varRatio < 1) return "0";
                    return String.valueOf(varRatio);
                }));

    }

    private boolean checkVarRatio(LoansByAge loan, int collectionLevel) {

        int ratio = loan.getVarRatio();

        if (collectionLevel == 1) {
            return ratio >= 2 && ratio <= 5;
        } else if (collectionLevel == 2) {
            return ratio >= 6 && ratio <= 10;
        } else if (collectionLevel == 3) {
            return ratio >= 11 && ratio <= 15;
        } else {
            return ratio > 15;
        }
    }


    public List<LoansByAge> getLoansByVariance(int branch) {
        String sql;
        List<LoansByAge> loansByAgeList = null;

        if(branch == 0){
            sql = """
                   SELECT loanAccountMpesa as Account,u.patner_id, u.id,u.first_name,u.last_name,u.phone,disbursed_at,
                   (IFNULL(expected_amount,0) - IFNULL(paid_amount,0)) as variance,
                   loan_term,ROUND(((IFNULL(expected_amount,0) - IFNULL(paid_amount,0))/loans.daily_amount_expected)) as varRatio,
                   (DATEDIFF(now(), DATE(disbursed_at))) as loanAge,ca.l_plate
                   FROM loans LEFT JOIN users u ON loans.userID = u.id
                   left join client_assets ca on u.id = ca.user_id
                   WHERE loan_balance > 0
                   AND expected_amount > paid_amount
                   AND disbursed_at is not null
                   And loanAccountMPesa NOT IN (SELECT loan_id from special_cases WHERE deleted_at is null)
                """;

            loansByAgeList = jdbcTemplateOne.query(sql, (rs, i) -> loansByAgeRowMapper(rs));
        }else{
            sql = """
              SELECT loanAccountMpesa as Account,u.patner_id, u.id,u.first_name,u.last_name,u.phone,disbursed_at,
              (IFNULL(expected_amount,0) - IFNULL(paid_amount,0)) as variance,
              loan_term,ROUND(((IFNULL(expected_amount,0) - IFNULL(paid_amount,0))/loans.daily_amount_expected)) as varRatio,
              (DATEDIFF(now(), DATE(disbursed_at))) as loanAge,ca.l_plate
              FROM loans LEFT JOIN users u ON loans.userID = u.id
              LEFT JOIN client_assets ca on u.id = ca.user_id
              WHERE loan_balance > 0
              AND expected_amount > paid_amount
              AND u.patner_id = ?
              AND disbursed_at is not null
              And loanAccountMPesa NOT IN (SELECT loan_id from special_cases WHERE deleted_at is null)
                """;

            loansByAgeList = jdbcTemplateOne.query(sql, (rs, i) -> loansByAgeRowMapper(rs),branch);

        }

        return loansByAgeList;
    }


    public Map<String,List<LoansByAge>> getLoansByLoanAge(int branch){

        String sql;
        List<LoansByAge> loansByAgeList = null;

        if(branch == 0){
            sql = """
                   SELECT loanAccountMpesa as Account,u.patner_id, u.id,u.first_name,u.last_name,u.phone,disbursed_at,
                   (IFNULL(expected_amount,0) - IFNULL(paid_amount,0)) as variance,
                   loan_term,ROUND(((IFNULL(expected_amount,0) - IFNULL(paid_amount,0))/loans.daily_amount_expected)) as varRatio,
                   (DATEDIFF(now(), DATE(disbursed_at))) as loanAge
                   FROM loans LEFT JOIN users u ON loans.userID = u.id
                   WHERE loan_balance > 0
                   AND expected_amount > paid_amount
                   AND disbursed_at is not null
                   And loanAccountMPesa NOT IN (SELECT loan_id from special_cases)
                """;

            loansByAgeList = jdbcTemplateOne.query(sql, (rs, i) -> loansByAgeRowMapper(rs));
        }else{
            sql = """
                
                   SELECT loanAccountMpesa as Account,u.patner_id, u.id,u.first_name,u.last_name,u.phone,disbursed_at,
                   (IFNULL(expected_amount,0) - IFNULL(paid_amount,0)) as variance,
                   loan_term,ROUND(((IFNULL(expected_amount,0) - IFNULL(paid_amount,0))/loans.daily_amount_expected)) as varRatio,
                   (DATEDIFF(now(), DATE(disbursed_at))) as loanAge
                   FROM loans LEFT JOIN users u ON loans.userID = u.id
                   WHERE loan_balance > 0
                   AND expected_amount > paid_amount
                   AND u.patner_id = ?
                   AND disbursed_at is not null
                   And loanAccountMPesa NOT IN (SELECT loan_id from special_cases)

                """;

            loansByAgeList = jdbcTemplateOne.query(sql, (rs, i) -> loansByAgeRowMapper(rs),branch);

        }



        return loansByAgeList.stream()
                .collect(Collectors.groupingBy(loan -> {
                    int age = loan.getLoanAge();
                    int term = loan.getLoanTerm();

                    // If the loan age exceeds the loan term, mark it as overdue
                    if (age > term) return "Overdue";
                    if (age <= 7) return "7";
                    if (age <= 30) return "30";
                    if (age <= 60) return "60";
                    if (age <= 90) return "90";
                    if (age <= 120) return "120";
                    return "Overdue";
                }));

    }

    public List<BadLoans> getAllBadLoans(int branch){

        String sql;

        if(branch == 0){
            sql = """
                    SELECT loanAccountMpesa as Account,u.patner_id, u.id,u.first_name,u.last_name,u.phone,disbursed_at,
                      (IFNULL(expected_amount,0) - IFNULL(paid_amount,0)) as variance,
                      loan_term,ROUND(((IFNULL(expected_amount,0) - IFNULL(paid_amount,0))/loans.daily_amount_expected)) as varRatio,
                      (DATEDIFF(now(), DATE(disbursed_at))) as loanAge,ca.l_plate, sc.case_category , sc.case_description
                      FROM loans LEFT JOIN users u ON loans.userID = u.id
                      left join client_assets ca on u.id = ca.user_id
                      left join special_cases sc on sc.loan_id = loans.loanAccountMPesa
                      WHERE loan_balance > 0
                      AND expected_amount > paid_amount
                      AND disbursed_at is not null
                      And loanAccountMPesa IN (SELECT loan_id from special_cases WHERE deleted_at is null)
                """;

            return jdbcTemplateOne.query(sql, (rs, i) -> badLoansRowMapper(rs));
        }else{
            sql = """
                   
                   SELECT loanAccountMpesa as Account,u.patner_id, u.id,u.first_name,u.last_name,u.phone,disbursed_at,
                      (IFNULL(expected_amount,0) - IFNULL(paid_amount,0)) as variance,
                      loan_term,ROUND(((IFNULL(expected_amount,0) - IFNULL(paid_amount,0))/loans.daily_amount_expected)) as varRatio,
                      (DATEDIFF(now(), DATE(disbursed_at))) as loanAge,ca.l_plate,sc.case_category , sc.case_description
                      FROM loans LEFT JOIN users u ON loans.userID = u.id
                      left join client_assets ca on u.id = ca.user_id
                      left join special_cases sc on sc.loan_id = loans.loanAccountMPesa
                      WHERE loan_balance > 0
                      AND expected_amount > paid_amount
                        AND u.patner_id = ?
                      AND disbursed_at is not null
                      And loanAccountMPesa IN (SELECT loan_id from special_cases WHERE deleted_at is null)

                """;

            return jdbcTemplateOne.query(sql, (rs, i) -> badLoansRowMapper(rs),branch);

        }
    }

    public List<BadLoans> getAllRedFlags(int branch){

        String sql;

        if(branch == 0){
            sql = """
                    SELECT loanAccountMpesa as Account,u.patner_id, u.id,u.first_name,u.last_name,u.phone,disbursed_at,
                      (IFNULL(expected_amount,0) - IFNULL(paid_amount,0)) as variance,
                      loan_term,ROUND(((IFNULL(expected_amount,0) - IFNULL(paid_amount,0))/loans.daily_amount_expected)) as varRatio,
                      (DATEDIFF(now(), DATE(disbursed_at))) as loanAge,ca.l_plate, sc.case_category , sc.case_description
                      FROM loans LEFT JOIN users u ON loans.userID = u.id
                      left join client_assets ca on u.id = ca.user_id
                      left join red_flag_cases sc on sc.loan_id = loans.loanAccountMPesa
                      WHERE loan_balance > 0
                      AND expected_amount > paid_amount
                      AND disbursed_at is not null
                      And loanAccountMPesa IN (SELECT loan_id from red_flag_cases WHERE deleted_at is null)
                """;

            return jdbcTemplateOne.query(sql, (rs, i) -> badLoansRowMapper(rs));
        }else{
            sql = """
                   
                   SELECT loanAccountMpesa as Account,u.patner_id, u.id,u.first_name,u.last_name,u.phone,disbursed_at,
                      (IFNULL(expected_amount,0) - IFNULL(paid_amount,0)) as variance,
                      loan_term,ROUND(((IFNULL(expected_amount,0) - IFNULL(paid_amount,0))/loans.daily_amount_expected)) as varRatio,
                      (DATEDIFF(now(), DATE(disbursed_at))) as loanAge,ca.l_plate,sc.case_category , sc.case_description
                      FROM loans LEFT JOIN users u ON loans.userID = u.id
                      left join client_assets ca on u.id = ca.user_id
                      left join red_flag_cases sc on sc.loan_id = loans.loanAccountMPesa
                      WHERE loan_balance > 0
                      AND expected_amount > paid_amount
                        AND u.patner_id = ?
                      AND disbursed_at is not null
                      And loanAccountMPesa IN (SELECT loan_id from red_flag_cases WHERE deleted_at is null)

                """;

            return jdbcTemplateOne.query(sql, (rs, i) -> badLoansRowMapper(rs),branch);

        }
    }


    private BadLoans badLoansRowMapper(ResultSet rs) throws SQLException {
        BadLoans badLoans = new BadLoans();
        badLoans.setId(rs.getString("id"));
        badLoans.setFirstName(rs.getString("first_name"));
        badLoans.setLastName(rs.getString("last_name"));
        badLoans.setAccount(rs.getString("Account"));
        badLoans.setBranch(rs.getInt("patner_id"));
        badLoans.setPhone(rs.getString("phone"));
        badLoans.setLoanTerm(rs.getInt("loan_term"));
        badLoans.setLoanAge(rs.getInt("loanAge"));
        badLoans.setVariance(rs.getInt("variance"));
        badLoans.setVarRatio(rs.getInt("varRatio"));
        badLoans.setDisbursedAt(rs.getString("disbursed_at"));
        badLoans.setNumberPlate(rs.getString("l_plate"));
        badLoans.setCategory(rs.getString("case_category"));
        badLoans.setDescription(rs.getString("case_description"));
        return badLoans;
    }

    private LoansByAge loansByAgeRowMapper(ResultSet rs) throws SQLException {
        LoansByAge loansByAge = new LoansByAge();
        loansByAge.setId(rs.getString("id"));
        loansByAge.setFirstName(rs.getString("first_name"));
        loansByAge.setLastName(rs.getString("last_name"));
        loansByAge.setAccount(rs.getString("Account"));
        loansByAge.setBranch(rs.getInt("patner_id"));
        loansByAge.setPhone(rs.getString("phone"));
        loansByAge.setLoanTerm(rs.getInt("loan_term"));
        loansByAge.setLoanAge(rs.getInt("loanAge"));
        loansByAge.setVariance(rs.getInt("variance"));
        loansByAge.setVarRatio(rs.getInt("varRatio"));
        loansByAge.setDisbursedAt(rs.getString("disbursed_at"));
        loansByAge.setNumberPlate(rs.getString("l_plate"));
        return loansByAge;
    }


    public void updateLastPaymentDate() {
        String sql = """
                UPDATE loans
                SET last_payment_date = (
                    SELECT MAX(m2.TransTime)
                FROM mpesa_c2b m2
                WHERE loans.loanAccountMPesa = m2.BillRefNumber );
                """;

        jdbcTemplateOne.update(sql);

    }

//    paid less than 1/3 of expected
//    variance is more than 1/3 of total loan
//    loan age is more than half the loan term
//    has not paid anything within the last 7 days

    public void updateNonPerformingLoans() {
        String sql = """
                UPDATE loans set non_performing =1 WHERE loanAccountMPesa IN (
                    SELECT account from (
                                        (SELECT l.loanAccountMPesa                                               as account,
                                                u.first_name,
                                                u.middle_name,
                                                u.last_name,
                                                l.loanPrincipal,
                                                loan_term,
                                                DATEDIFF(NOW(), disbursed_at)                                    as loanAge,
                                                expected_amount,
                                                paid_amount,
                                                (expected_amount - paid_amount)                                  as variance,
                                                ROUND(((expected_amount - paid_amount) / daily_amount_expected)) as varRation,
                                                last_payment_date
                                         FROM loans l
                                                  LEFT JOIN users u ON l.userID = u.id
                                         WHERE disbursed_at is not null
                                           AND paid_amount < (expected_amount / 3)
                                           AND (expected_amount - paid_amount) > (client_loan_total / 3)
                                           AND DATEDIFF(NOW(), disbursed_at) > (loan_term / 2)
                                           AND DATEDIFF(NOW(), last_payment_date) > 7
                                         ORDER BY varRation desc)
                                                                )x
                    )
                """;

        jdbcTemplateOne.update(sql);
    }

    public List<HourlyCollection> getHourlyCollectionsPerDay(String date){
        String sql = """
                   SELECT HOUR(TIME(TransTime)) as hour_of_day, sum(TransAmount) amout_colected  FROM mpesa_c2b
                                      WHERE  (BillRefNumber IN ( SELECT  loanAccountMPesa from loans) )
                                      AND DATE(TransTime) = ?
                                      GROUP BY hour_of_day
                                      order by hour_of_day
                """;

        return jdbcTemplateOne.query(sql ,(rs,i)-> HourlyCollectionRowMapper(rs),date);
    }

    private HourlyCollection HourlyCollectionRowMapper(ResultSet rs) throws SQLException {
        HourlyCollection hourlyCollection = new HourlyCollection();
        hourlyCollection.setHourOfDay(rs.getString("hour_of_day"));
        hourlyCollection.setAmountCollected(rs.getInt("amout_colected"));
        return hourlyCollection;
    }

    public List<CollectionByAge> getCollectionByLoanAgeGroup(String date) {
        String sql = """
                SELECT
                    CASE
                        WHEN l.loanAge BETWEEN 1 AND 30 THEN '1-30 days'
                        WHEN l.loanAge BETWEEN 31 AND 60 THEN '31-60 days'
                        WHEN l.loanAge BETWEEN 61 AND 90 THEN '61-90 days'
                        WHEN l.loanAge BETWEEN 91 AND 120 THEN '91-120 days'
                        WHEN l.loanAge > 120 THEN 'overdues'
                        ELSE 'Unknown'
                    END AS LoanAgeGroup,
                    SUM(m.TransAmount) AS TotalCollected
                FROM
                    (SELECT
                         loanAccountMpesa AS Account,
                         (DATEDIFF(NOW(), DATE(disbursed_at))) AS loanAge
                     FROM loans
                     WHERE loan_balance > 0
                       AND expected_amount > paid_amount
                       AND disbursed_at IS NOT NULL
                    ) l
                LEFT JOIN
                    mpesa_c2b m
                ON
                    l.Account = m.BillRefNumber
                WHERE
                    DATE(m.TransTime) = CURDATE()
                GROUP BY
                    LoanAgeGroup
                ORDER BY
                    LoanAgeGroup;
                """;

        return jdbcTemplateOne.query(sql, (rs,i)->{
            CollectionByAge collectionByAge = new CollectionByAge();
            collectionByAge.setLoanAgeGroup(rs.getString("LoanAgeGroup"));
            collectionByAge.setAmountCollected(rs.getInt("TotalCollected"));
            return  collectionByAge;
        });
    }

    public List<PriorityProfile> getPriorityList(PriorityProfileFilter priorityProfileFilter) throws Exception{
        
        String sql = """                                                
                SELECT * FROM (SELECT l.disbursed_at,
                                                      l.loanAccountMPesa                                                     as account,
                                                      u.first_name,
                                                      u.last_name,
                                                      u.phone,
                                                      u.patner_id                                                            as branch,
                                                      l.loan_term,
                                                      DATEDIFF(NOW(), l.disbursed_at)                                        as loanAge,
                                                      (l.expected_amount - l.paid_amount)                                    as variance,
                                                      ROUND(((l.expected_amount - l.paid_amount) / l.daily_amount_expected)) as varianceRatio,
                                                      count(ccl.loan_account)                                                as numberOfCalls
                                               from loans l
                                                        LEFT JOIN call_center_logs ccl ON l.loanAccountMPesa = ccl.loan_account
                                                        LEFT JOIN users u on l.userID = u.id
                                               WHERE l.loan_balance > 0
                                                 AND ((l.expected_amount - l.paid_amount) / l.daily_amount_expected) > ?
                                                 AND u.id not in (SELECT  userId from bike_recovery where is_excempted =1 OR bike_recoverd_at is not null)
                                               GROUP BY loan_account, loanAccountMPesa, disbursed_at, expected_amount, paid_amount,
                                                        daily_amount_expected, first_name, last_name, patner_id,phone,loan_term
                                               ORDER BY numberOfCalls)x
                                WHERE numberOfCalls < ? AND loanAge > ? AND loanAge < ?
                """;
        
        return jdbcTemplateOne.query(sql,(rs,i)->priorityListRowMapper(rs),
                priorityProfileFilter.getVarRationMore(),
                priorityProfileFilter.getNumberOfCallsLess(),
                priorityProfileFilter.getLoanAgeMore(),
                priorityProfileFilter.getLoanAgeLess()
        );
    }

    private PriorityProfile priorityListRowMapper(ResultSet rs) throws SQLException {
        PriorityProfile priorityProfile = new PriorityProfile();
        priorityProfile.setLoanDisbursedAt(rs.getString("disbursed_at"));
        priorityProfile.setAccount(rs.getString("account"));
        priorityProfile.setFirstName(rs.getString("first_name"));
        priorityProfile.setLastName(rs.getString("last_name"));
        priorityProfile.setPhone(rs.getString("phone"));
        priorityProfile.setLoanTerm(rs.getInt("loan_term"));
        priorityProfile.setBranch(rs.getString("branch"));
        priorityProfile.setLoanAge(rs.getInt("loanAge"));
        priorityProfile.setVariance(rs.getInt("variance"));
        priorityProfile.setVarRatio(rs.getInt("varianceRatio"));
        priorityProfile.setNumberOfCalls(rs.getInt("numberOfCalls"));

        return priorityProfile;
    }

    public List<DailyPaymentTracker> getDailyPaymentTracker(String loanAccount) {

        String sqlSetAccount = "SET @account = ?";
        jdbcTemplateOne.update(sqlSetAccount,loanAccount);

        String sqlGetDailyTracker =
                """
                WITH RECURSIVE date_series AS (
                    -- Generate the date series starting from the loan's disbursement date
                    SELECT DATE(disbursed_at) AS date
                    FROM loans
                    WHERE loanAccountMPesa = @account

                    UNION ALL

                    -- Continue generating dates up to the loan term end or the last payment date
                    SELECT date + INTERVAL 1 DAY
                    FROM date_series
                    WHERE date < CURDATE() AND date  <= (
                        SELECT GREATEST(
                            DATE_ADD(DATE(l.disbursed_at), INTERVAL l.loan_term DAY),
                            COALESCE(MAX(DATE(m.TransTime)), DATE_ADD(DATE(l.disbursed_at), INTERVAL l.loan_term DAY))
                        )
                        FROM loans l
                        LEFT JOIN mpesa_c2b m
                            ON l.loanAccountMPesa = m.BillRefNumber
                        WHERE l.loanAccountMPesa = @account
                        GROUP BY l.loanAccountMPesa, l.disbursed_at, l.loan_term
                    )
                )
                SELECT
                    ds.date,
                    -- Set expected amount to 0 for disbursement date, normal amount for other dates
                                               CASE
                                                   WHEN ds.date = (SELECT DATE(disbursed_at) FROM loans WHERE loanAccountMPesa = @account) THEN 0
                                                   ELSE l.daily_amount_expected
                                               END AS daily_amount_expected,
                    COALESCE(SUM(m.TransAmount), 0) AS amount_paid
                FROM date_series ds
                JOIN loans l ON l.loanAccountMPesa = @account
                LEFT JOIN mpesa_c2b m
                    ON ds.date = DATE(m.TransTime)
                    AND (m.BillRefNumber = @account)
                GROUP BY ds.date, l.daily_amount_expected
                ORDER BY ds.date;

                """;

        return jdbcTemplateOne.query(sqlGetDailyTracker,(rs,i)->{
            DailyPaymentTracker dailyPaymentTracker = new DailyPaymentTracker();
            dailyPaymentTracker.setDate(rs.getString("date"));
            dailyPaymentTracker.setDailyAmountExpected(rs.getInt("daily_amount_expected"));
            dailyPaymentTracker.setAmountPaid(rs.getInt("amount_paid"));
            return dailyPaymentTracker;
        });

    }

    public MonthlyGeneralPerformance getMonthlyGeneralPerformance() {
        String sql = """
                SELECT (SELECT SUM(daily_expected) as daily_expected FROM daily_expected_summary WHERE MONTH(DATE) = MONTH(curdate()) AND YEAR(date) = YEAR(CURDATE())) as total_expected,
                                       (SELECT SUM(TransAmount) FROM mpesa_c2b WHERE MONTH(TransTime) = MONTH(curdate()) AND YEAR(TransTime) = YEAR(CURDATE()) AND BillRefNumber IN (SELECT loanAccountMPesa FROM loans) ) as total_collected,
                                       (SELECT SUM(client_loan_total) FROM loans WHERE MONTH(disbursed_at) = MONTH(curdate()) AND YEAR(disbursed_at) = YEAR(CURDATE())) as total_disbursed,
                  (SELECT SUM(loanPrincipal) FROM loans WHERE MONTH(disbursed_at) = MONTH(curdate()) AND YEAR(disbursed_at) = YEAR(CURDATE())) as total_principal
                """;

        return jdbcTemplateOne.queryForObject(sql,(rs,i) ->setMonthlyGeneralPerformance(rs));
    }

    private MonthlyGeneralPerformance setMonthlyGeneralPerformance(ResultSet rs) throws SQLException {
        MonthlyGeneralPerformance monthlyGeneralPerformance = new MonthlyGeneralPerformance();
        monthlyGeneralPerformance.setExpectedAmount(rs.getInt("total_expected"));
        monthlyGeneralPerformance.setTotalCollected(rs.getInt("total_collected"));
        monthlyGeneralPerformance.setTotalDisbursed(rs.getInt("total_disbursed"));
        monthlyGeneralPerformance.setTotalPrincipal(rs.getInt("total_principal"));
        return monthlyGeneralPerformance;
    }

    public int movePaymentToNewAccount(OldAccountNewAccountPayment oldAccountNewAccountPayment) throws Exception{

        String sql = "UPDATE mpesa_c2b SET BillRefNumber=? WHERE TransID=?";
        return jdbcTemplateOne.update(sql,oldAccountNewAccountPayment.getNewAccount(),oldAccountNewAccountPayment.getTransactionId());
    }


    public ParHolder getPAR(int parDays) {

        String sql = """
                WITH loan_details AS (
                    SELECT
                        loanAccountMPesa as loan_id,
                        client_loan_total,
                        loanPrincipal as total_principal,
                        ((loanPrincipal/client_loan_total)*100) as percentage_principle,
                        expected_amount,
                        (expected_amount - loans.paid_amount) as arrears,
                        ROUND(((expected_amount - loans.paid_amount)/daily_amount_expected)) as days_in_arrears,
                        paid_amount,
                        (((loanPrincipal/client_loan_total)) * paid_amount) as paid_principal,
                        (loanPrincipal - (((loanPrincipal/client_loan_total)) * paid_amount)) as outstanding_principal
                    FROM loans
                    WHERE (expected_amount - loans.paid_amount) > 0
                )
                SELECT
                    SUM(outstanding_principal) as total_outstanding_principal,
                    SUM(CASE WHEN days_in_arrears >= ? THEN outstanding_principal ELSE 0 END) as PAR_amount,
                    ROUND((SUM(CASE WHEN days_in_arrears >= ? THEN outstanding_principal ELSE 0 END) / SUM(outstanding_principal)) * 100, 2) as PAR_percentage
                FROM loan_details;
                """;

        return jdbcTemplateOne.queryForObject(sql,(rs,i)-> setPar(rs),parDays,parDays);
    }

    private ParHolder setPar(ResultSet rs) throws SQLException {
        ParHolder parHolder = new ParHolder();
        parHolder.setOutstandingPrincipal(rs.getDouble("total_outstanding_principal"));
        parHolder.setParPrincipal(rs.getDouble("PAR_amount"));
        parHolder.setParPercentage(rs.getDouble("PAR_percentage"));
        return parHolder;
    }

    public List<CollectionPerformance> getColectionPerformanceMoM(String cutOffdate) {
        String setCutOffSql = """
            SET @cutoff_date = ?
            """;
        jdbcTemplateOne.update(setCutOffSql, cutOffdate);

        String sql = """
            SELECT
                l.userID,
                l.loanAccountMPesa AS loan_account,
                l.loanPrincipal,
                l.client_loan_total,
                l.loan_term,
                l.daily_amount_expected,
                l.disbursed_at,

                DATEDIFF(LEAST(@cutoff_date, CURDATE()), l.disbursed_at) AS loan_age,

                LEAST(
                    DATEDIFF(LEAST(@cutoff_date, CURDATE()), l.disbursed_at) * l.daily_amount_expected,
                    l.client_loan_total
                ) AS expected_amount,

                (
                    SELECT COALESCE(SUM(c2b.TransAmount),0)
                    FROM mpesa_c2b c2b
                    WHERE c2b.BillRefNumber = l.loanAccountMPesa
                      AND DATE(c2b.TransTime) <= @cutoff_date
                ) AS received_amount

            FROM loans l
            WHERE l.disbursed_at <= @cutoff_date;
            """;

        return jdbcTemplateOne.query(sql, (rs, i) -> setCollectionPerformanceData(rs));
    }

    private CollectionPerformance setCollectionPerformanceData(ResultSet rs) throws SQLException {
        CollectionPerformance cp = new CollectionPerformance();
        cp.setUserId(rs.getInt("userID"));
        cp.setLoanAccount(rs.getString("loan_account"));
        cp.setPrincipal(rs.getInt("loanPrincipal"));
        cp.setClientLoanTotal(rs.getInt("client_loan_total"));
        cp.setLoanTerm(rs.getInt("loan_term"));
        cp.setDailyAmountExpected(rs.getDouble("daily_amount_expected"));
        cp.setDisbursedAt(rs.getDate("disbursed_at").toLocalDate());
        cp.setLoanAge(rs.getInt("loan_age"));
        cp.setExpectedAmount(rs.getDouble("expected_amount"));
        cp.setReceivedAmount(rs.getDouble("received_amount"));
        return cp;
    }

    private List<LoanCollectionsData> getLoanCollectionsDataForAllActiveLoans(){
        String sql = """
                SELECT e.loan_id,
                                      e.due_date,
                                      e.daily_amount,
                                      e.cumulative_amount,
                                      COALESCE(SUM(CASE WHEN DATE(m.TransTime) = e.due_date THEN m.TransAmount END),
                                               0)                                                                        AS amount_paid_that_day,
                                      COALESCE(SUM(CASE WHEN DATE(m.TransTime) <= e.due_date THEN m.TransAmount END),
                                               0)                                                                        AS total_paid_to_date,
                                      'expected'                                                                         AS source_type
                               FROM loan_daily_payment_expectations e
                                        JOIN loans l ON e.loan_id = l.id
                                        LEFT JOIN mpesa_c2b m ON m.BillRefNumber = e.account_number
                               GROUP BY e.loan_id, e.due_date, e.daily_amount, e.cumulative_amount
                
                               UNION ALL
                
                -- unmatched payments (no due_date in expectations)
                               SELECT l.id               AS loan_id,
                                      DATE(m.TransTime)  AS due_date,
                                      0                  AS daily_amount,
                                      0                  AS cumulative_amount,
                                      SUM(m.TransAmount) AS amount_paid_that_day,
                                      SUM(m.TransAmount) AS total_paid_to_date,
                                      'overdue'          AS source_type
                               FROM mpesa_c2b m
                                        JOIN loans l ON m.BillRefNumber = l.loanAccountMPesa
                               WHERE NOT EXISTS (SELECT 1
                                                 FROM loan_daily_payment_expectations e
                                                 WHERE e.loan_id = l.id
                                                   AND e.due_date = DATE(m.TransTime))
                               GROUP BY l.id, DATE(m.TransTime)
                """;

        return jdbcTemplateOne.query(sql , (rs, i) -> loanCollectionsDataRowMapper(rs));
    }

    private LoanCollectionsData loanCollectionsDataRowMapper(ResultSet rs) throws SQLException {

        LoanCollectionsData loanCollectionsData = new LoanCollectionsData();
        loanCollectionsData.setLoanAccountNumber(rs.getString("loan_id"));
        loanCollectionsData.setDueDate(rs.getDate("due_date").toLocalDate());
        loanCollectionsData.setDailyAmountExpected(rs.getDouble("daily_amount"));
        loanCollectionsData.setCumulativeAmountExpected(rs.getDouble("cumulative_amount"));
        loanCollectionsData.setAmountPaidThatDay(rs.getDouble("amount_paid_that_day"));
        loanCollectionsData.setTotalPaidToDate(rs.getDouble("total_paid_to_date"));
//        loanCollectionsData.setCumulativeArrears(rs.getDouble("cumulative_arrears"));
        return loanCollectionsData;
    }

    public CallCentreStats getCallcentrePerformance(int repId, String startDate, String endDate) {

        String startDateSql = "SET @start_date = ?";
        jdbcTemplateOne.update(startDateSql, startDate);

        String endDateSql = "SET @end_date = ?";
        jdbcTemplateOne.update(endDateSql, endDate);

        if(repId == 0){
            String sql = """
                SELECT
                (SELECT count(id) FROM call_center_logs WHERE DATE(created_at) BETWEEN DATE(@start_date) AND DATE(@end_date) ) as all_calls_attempts,
                (SELECT count(id) FROM call_center_logs WHERE DATE(created_at) BETWEEN DATE(@start_date) AND DATE(@end_date) AND call_picked = 1) as calls_picked,
                ((SELECT count(id) FROM call_center_logs WHERE DATE(created_at) BETWEEN DATE(@start_date) AND DATE(@end_date) AND call_picked = 0 AND reason_not_picked = 'NotReachable') )
                    as user_not_reachable,
                ((SELECT count(id) FROM call_center_logs WHERE DATE(created_at) BETWEEN DATE(@start_date) AND DATE(@end_date) AND call_picked = 0 AND reason_not_picked = 'ignored') )
                as user_ignored,
                ((SELECT count(id) FROM call_center_logs WHERE DATE(created_at) BETWEEN DATE(@start_date) AND DATE(@end_date) AND call_picked = 0 AND reason_not_picked = 'blocked') )
                as user_blocked
                """;

            return jdbcTemplateOne.queryForObject(sql,(rs,i)-> callcentreRowMapper(rs));
        }else{

            String repIdSql = "SET @repId = ?";
            jdbcTemplateOne.update(repIdSql, repId);

            String sql = """
                SELECT
                (SELECT count(id) FROM call_center_logs WHERE DATE(created_at) BETWEEN DATE(@start_date) AND DATE(@end_date) AND rep_id = @repId ) as all_calls_attempts,
                (SELECT count(id) FROM call_center_logs WHERE DATE(created_at) BETWEEN DATE(@start_date) AND DATE(@end_date) AND call_picked = 1 AND rep_id = @repId) as calls_picked,
                ((SELECT count(id) FROM call_center_logs WHERE DATE(created_at) BETWEEN DATE(@start_date) AND DATE(@end_date) AND call_picked = 0 AND reason_not_picked = 'NotReachable' AND rep_id = @repId))
                    as user_not_reachable,
                ((SELECT count(id) FROM call_center_logs WHERE DATE(created_at) BETWEEN DATE(@start_date) AND DATE(@end_date) AND call_picked = 0 AND reason_not_picked = 'ignored' AND rep_id = @repId) )
                as user_ignored,
                ((SELECT count(id) FROM call_center_logs WHERE DATE(created_at) BETWEEN DATE(@start_date) AND DATE(@end_date) AND call_picked = 0 AND reason_not_picked = 'blocked' AND rep_id = @repId) )
                as user_blocked
                """;

            return jdbcTemplateOne.queryForObject(sql,(rs,i)-> callcentreRowMapper(rs));
        }

    }

    private CallCentreStats callcentreRowMapper(ResultSet rs) throws SQLException {
        CallCentreStats callCentreStats = new CallCentreStats();
        callCentreStats.setAllCallsAttempted(rs.getInt("all_calls_attempts"));
        callCentreStats.setAllCallsPicked(rs.getInt("calls_picked"));
        callCentreStats.setUserNotReachable(rs.getInt("user_not_reachable"));
        callCentreStats.setUserIgnored(rs.getInt("user_ignored"));
        callCentreStats.setUserBlocked(rs.getInt("user_blocked"));
        return callCentreStats;
    }
}
