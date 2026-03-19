package com.otblabs.jiinueboda.loans;

import com.otblabs.jiinueboda.collections.models.SpecialCaseLoan;
import com.otblabs.jiinueboda.loans.models.*;
import com.otblabs.jiinueboda.users.UserService;
import com.otblabs.jiinueboda.users.models.SystemUser;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class LoanManagementService {

    private final JdbcTemplate jdbcTemplateOne;
    private final UserService userService;

    public LoanManagementService(JdbcTemplate jdbcTemplateOne, UserService userService) {
        this.jdbcTemplateOne = jdbcTemplateOne;
        this.userService = userService;
    }

    public List<UserProfileLoanData> getAllLoansForUser(int userId) {

        String sql = """
               SELECT loanAccountMPesa,LOANPRINCIPAL, INTERESTPERCENTAGE,client_loan_total,
               loanPurpose,loan_term,daily_amount_expected,loanStatus,loanStatusLevelOne,loanStatusLevelTwo,createdAt,disbursed_at,
               DATEDIFF(DATE(NOW()) , DATE(disbursed_at)) as loan_age,
               (SELECT public_url from client_attachments WHERE id = agreement_attachment_id ) as loan_agreement_url
               from loans WHERE userID = ?
                """;

        return jdbcTemplateOne.query(sql,(rs,i)->mapRowToUserProfileLoanData(rs),userId);

    }

    private  UserProfileLoanData mapRowToUserProfileLoanData(ResultSet rs) throws SQLException {
        UserProfileLoanData userProfileLoanData = new UserProfileLoanData();
        userProfileLoanData.setAccount(rs.getString("loanAccountMPesa"));
        userProfileLoanData.setLoanPrincipal(rs.getDouble("LOANPRINCIPAL"));
        userProfileLoanData.setInterestPercentage(rs.getDouble("INTERESTPERCENTAGE"));
        userProfileLoanData.setClientLoanTotal(rs.getDouble("client_loan_total"));
        userProfileLoanData.setLoanPurpose(rs.getString("loanPurpose"));
        userProfileLoanData.setLoanTerm(rs.getInt("loan_term"));
        userProfileLoanData.setDailyAmountExpected(rs.getDouble("daily_amount_expected"));
        userProfileLoanData.setLoanStatus(rs.getString("loanStatus"));
        userProfileLoanData.setLoanStatusLevelOne(rs.getString("loanStatusLevelOne"));
        userProfileLoanData.setLoanStatusLevelTwo(rs.getString("loanStatusLevelTwo"));
        userProfileLoanData.setCreatedAt(rs.getString("createdAt"));
        userProfileLoanData.setDisbursedAt(rs.getString("disbursed_at"));
        userProfileLoanData.setLoanAge(rs.getInt("loan_age"));
        userProfileLoanData.setLoanAgreementUrl(rs.getString("loan_agreement_url"));
        return userProfileLoanData;
    }

    public List<LoanStatement> getLoanStatementsForLoan(String loanId) {
        String sql = """
                 SELECT TransactionType,FirstName,TransID,TransTime,TransAmount FROM mpesa_c2b WHERE BillRefNumber =?  ORDER BY TransTime DESC
                """;
        return jdbcTemplateOne.query(sql,(rs,i)->{
            LoanStatement loanStatement = new LoanStatement();
            loanStatement.setTransactionId(rs.getString("TransID"));
            loanStatement.setTransactionTime(rs.getString("TransTime"));
            loanStatement.setTransactionAmount(rs.getInt("TransAmount"));
            loanStatement.setTransactionType(rs.getString("TransactionType"));
            loanStatement.setUserName(rs.getString("FirstName"));
            return loanStatement;
        },loanId);
    }

    public LoanStatementProfileInfo getLoanStatementProfileInfo(String loanId) {
        String sql = """
                SELECT u.first_name,u.last_name, u.phone, u.nationalId,l.loanAccountMPesa as account,l.loanPrincipal,
                l.interestPercentage,l.total_interest_amount,l.total_mon_fee, l.ntsa_fee,l.credit_life_insurance,
                l.loan_processing_fee,l.loan_term,l.total_loan_disburse,l.client_loan_total,mb2c.receiver_party_public_name,mb2c.transaction_completed_datetime,
                mb2c.transaction_id,mb2c.transaction_amount
                from loans l
                LEFT JOIN users u ON l.userID = u.id
                left join mpesa_b2c mb2c on l.loanAccountMPesa = mb2c.occasion
                WHERE l.loanAccountMPesa = ?
                """;
        return jdbcTemplateOne.queryForObject(sql,(rs,i)->mapRowToStatementProfile(rs),loanId);
    }

    private LoanStatementProfileInfo mapRowToStatementProfile(ResultSet rs) throws SQLException {
        LoanStatementProfileInfo loanStatementProfileInfo = new LoanStatementProfileInfo();

        loanStatementProfileInfo.setFirstName(rs.getString("first_name"));
        loanStatementProfileInfo.setLastName(rs.getString("last_name"));
        loanStatementProfileInfo.setPhone(rs.getString("phone"));
        loanStatementProfileInfo.setNationalId(rs.getString("nationalId"));
        loanStatementProfileInfo.setAccount(rs.getString("account"));
        loanStatementProfileInfo.setLoanPrincipal(rs.getInt("loanPrincipal"));
        loanStatementProfileInfo.setInterestPercentage(rs.getInt("interestPercentage"));
        loanStatementProfileInfo.setTotalInterestAmount(rs.getInt("total_interest_amount"));
        loanStatementProfileInfo.setTotalMonFee(rs.getInt("total_mon_fee"));
        loanStatementProfileInfo.setNtsaFee(rs.getInt("ntsa_fee"));
        loanStatementProfileInfo.setCreditLifeInsurance(rs.getInt("credit_life_insurance"));
        loanStatementProfileInfo.setLoanProcessingFee(rs.getInt("loan_processing_fee"));
        loanStatementProfileInfo.setLoanTerm(rs.getInt("loan_term"));
        loanStatementProfileInfo.setTotalLoanDisburse(rs.getInt("total_loan_disburse"));
        loanStatementProfileInfo.setClientLoanTotal(rs.getInt("client_loan_total"));
        loanStatementProfileInfo.setRecieverPublicName(rs.getString("receiver_party_public_name"));
        loanStatementProfileInfo.setTransactionCompletedTime(rs.getString("transaction_completed_datetime"));
        loanStatementProfileInfo.setB2cTransId(rs.getString("transaction_id"));
        loanStatementProfileInfo.setB2cTransAmount(rs.getInt("transaction_amount"));
        return loanStatementProfileInfo;
    }

    public int getMonthlyFirstTimeLoans(int month, int branch) throws Exception{

        if(branch == 0){
            String sql = """
                SELECT count(userId) as total_new_loans from (
                SELECT l.userId, MIN(l.createdAt) AS first_time_loan
                FROM loans l
                WHERE l.disbursed_at is not null
                GROUP BY l.userId
                HAVING MONTH(first_time_loan) = ? AND YEAR(first_time_loan) = YEAR(CURDATE())
                )x
                """;

            return jdbcTemplateOne.queryForObject(sql, (rs,p) ->{
                return rs.getInt("total_new_loans");
            },month);
        }else{
            String sql = """
               SELECT count(userId) as total_new_loans
               FROM (
                   SELECT l.userId, MIN(l.createdAt) AS first_time_loan
                   FROM loans l
                   INNER JOIN users u ON u.id = l.userId
                   WHERE l.disbursed_at is not null AND u.patner_id = ?
                   GROUP BY l.userId
                   HAVING MONTH(first_time_loan) = ? AND YEAR(first_time_loan) = YEAR(CURDATE())
               ) AS loan_data;
                """;

            return jdbcTemplateOne.queryForObject(sql, (rs,p) ->{
                return rs.getInt("total_new_loans");
            },branch,month);
        }


    }



    public List<PendingLoanData> getAllOverdueLoans(){

        String sql = """
                SELECT  id,Account,first_name,middle_name,last_name,phone,client_age,all_loans_taken,patner_id,group_id,stage_name,
                                           stage_county,stage_sub_county,stage_ward,l_plate,brand,make, model,
                                            Principal,TotalLoan,Term,loanAge,LoanBalance,DisburseDate,LastPaymentDate,DSLP,Status,TotalPaid,totalExpected,
                                            IF(TotalPaid > totalExpected, 0, totalExpected - TotalPaid)                as variance,
                                            ROUND(IF(TotalPaid > totalExpected, 0, totalExpected - TotalPaid) / dailyExpected) as varRatio,
                                            dailyExpected,daysPaid
                                                         from (
                                                             select
                                                                 g.group_name as stage_name,
                                                                 (SELECT name from counties WHERE county_id = (SELECT county_id from `groups` where id=g.id)) as stage_county,
                                                                 (SELECT name from sub_counties WHERE sub_county_id = (SELECT sub_county_id from `groups` where id=g.id)) as stage_sub_county,
                                                                 (SELECT name from wards WHERE ward_id = (SELECT ward_id from `groups` where id=g.id)) as stage_ward,
                                                                 ca.l_plate,ca.brand,ca.make, ca.model, u.id,u.first_name,u.middle_name,u.last_name,u.phone,
                                                                   (SELECT TIMESTAMPDIFF(YEAR, dob, CURDATE()) AS client_age FROM users WHERE id = u.id) as client_age,
                                                                   u.patner_id,u.group_id,l.loanAccountMPesa as Account,
                                                                   (SELECT COUNT(userID) FROM loans WHERE userID=u.id) as all_loans_taken,
                                                                   l.loanPrincipal as Principal,
                                                                   l.client_loan_total as TotalLoan,l.loan_term as Term,l.loan_balance as LoanBalance, DATE(l.disbursed_at) as DisburseDate,
                                                         IFNULL(DATE(last_payment_date),'NO PAYMENT YET') as LastPaymentDate,
                                                         DATEDIFF(curdate(),DATE(last_payment_date))  as DSLP,DATEDIFF(now(),disbursed_at) as loanAge,
                                
                                                         IF(l.loan_term < DATEDIFF(now(), disbursed_at), (IF(l.loan_balance < 1, ('Completed  Loan'), 'Overdue Loan')), 'Active Loan')  as Status,
                                
                                                         l.paid_amount as TotalPaid, l.expected_amount as totalExpected, l.daily_amount_expected as dailyExpected,
                                                         floor(IFNULL(paid_amount,0) / l.daily_amount_expected) as daysPaid
                                                        from loans l
                                                        inner join users u on u.id = l.userID left join client_assets ca ON l.asset_id = ca.id  left join `groups` g ON g.id = u.group_id
                                                        where l.disbursed_at is not null AND loan_balance > 0 AND DATEDIFF(CURDATE(), DATE(disbursed_at)) > loan_term  ORDER BY l.createdAt DESC )x
                                
                """;

        return jdbcTemplateOne.query(sql,(rs,i)->mapRowToPendingLoanData(rs));
    }


    @Cacheable(value = "loanBalancesTrackerCache", key = "'loanBalances-' + #branch")
    public List<PendingLoanData> getActiveLoansBalances(int branch) {

        String sql;

        if (branch == 0) {
            sql = """
                SELECT  id,Account,first_name,middle_name,last_name,phone,is_online_rider,client_age,all_loans_taken,patner_id,group_id,stage_name,
                stage_county,stage_sub_county,stage_ward,l_plate,brand,make, model,
                Principal,TotalLoan,Term,loanAge,LoanBalance,DisburseDate,LastPaymentDate,DSLP,Status,TotalPaid,totalExpected,
                IF(TotalPaid > totalExpected, 0, totalExpected - TotalPaid)                as variance,
                ROUND(IF(TotalPaid > totalExpected, 0, totalExpected - TotalPaid) / dailyExpected) as varRatio,
                dailyExpected,daysPaid
                             from (
                                 select
                                     g.group_name as stage_name,
                                     (SELECT name from counties WHERE county_id = (SELECT county_id from `groups` where id=g.id)) as stage_county,
                                     (SELECT name from sub_counties WHERE sub_county_id = (SELECT sub_county_id from `groups` where id=g.id)) as stage_sub_county,
                                     (SELECT name from wards WHERE ward_id = (SELECT ward_id from `groups` where id=g.id)) as stage_ward,
                                     ca.l_plate,ca.brand,ca.make, ca.model, u.id,u.first_name,u.middle_name,u.last_name,u.phone,u.is_online_rider,
                                       (SELECT TIMESTAMPDIFF(YEAR, dob, CURDATE()) AS client_age FROM users WHERE id = u.id) as client_age,
                                       u.patner_id,u.group_id,l.loanAccountMPesa as Account,
                                       (SELECT COUNT(userID) FROM loans WHERE userID=u.id) as all_loans_taken,
                                       l.loanPrincipal as Principal,
                                       l.client_loan_total as TotalLoan,l.loan_term as Term,l.loan_balance as LoanBalance, DATE(l.disbursed_at) as DisburseDate,
                             IFNULL(DATE(last_payment_date),'NO PAYMENT YET') as LastPaymentDate,
                             DATEDIFF(curdate(),DATE(last_payment_date))  as DSLP,DATEDIFF(now(),disbursed_at) as loanAge,

                             IF(l.loan_term < DATEDIFF(now(), disbursed_at), (IF(l.loan_balance < 1, ('Completed  Loan'), 'Overdue Loan')), 'Active Loan')  as Status,

                             l.paid_amount as TotalPaid, l.expected_amount as totalExpected, l.daily_amount_expected as dailyExpected,
                             floor(IFNULL(paid_amount,0) / l.daily_amount_expected) as daysPaid
                            from loans l
                            inner join users u on u.id = l.userID left join client_assets ca ON l.asset_id = ca.id  left join `groups` g ON g.id = u.group_id
                            where l.disbursed_at is not null AND loan_balance > 0 ORDER BY l.createdAt DESC )x
                """;

            return jdbcTemplateOne.query(sql,(rs,i)->mapRowToPendingLoanData(rs));

        }
        else {
            sql = """
                     SELECT  id,Account,first_name,middle_name,last_name,phone,is_online_rider,client_age,all_loans_taken,patner_id,group_id,stage_name,
                                               stage_county,stage_sub_county,stage_ward,l_plate,brand,make, model,
                                                Principal,TotalLoan,Term,loanAge,LoanBalance,DisburseDate,LastPaymentDate,DSLP,Status,TotalPaid,totalExpected,
                                                IF(TotalPaid > totalExpected, 0, totalExpected - TotalPaid)                as variance,
                                                ROUND(IF(TotalPaid > totalExpected, 0, totalExpected - TotalPaid) / dailyExpected) as varRatio,
                                                dailyExpected,daysPaid
                                                             from (
                                                                 select
                                                                     g.group_name as stage_name,
                                                                     (SELECT name from counties WHERE county_id = (SELECT county_id from `groups` where id=g.id)) as stage_county,
                                                                     (SELECT name from sub_counties WHERE sub_county_id = (SELECT sub_county_id from `groups` where id=g.id)) as stage_sub_county,
                                                                     (SELECT name from wards WHERE ward_id = (SELECT ward_id from `groups` where id=g.id)) as stage_ward,
                                                                     ca.l_plate,ca.brand,ca.make, ca.model, u.id,u.first_name,u.middle_name,u.last_name,u.phone,u.is_online_rider,
                                                                       (SELECT TIMESTAMPDIFF(YEAR, dob, CURDATE()) AS client_age FROM users WHERE id = u.id) as client_age,
                                                                       u.patner_id,u.group_id,l.loanAccountMPesa as Account,
                                                                       (SELECT COUNT(userID) FROM loans WHERE userID=u.id) as all_loans_taken,
                                                                       l.loanPrincipal as Principal,
                                                                       l.client_loan_total as TotalLoan,l.loan_term as Term,l.loan_balance as LoanBalance, DATE(l.disbursed_at) as DisburseDate,
                                                             IFNULL(DATE(last_payment_date),'NO PAYMENT YET') as LastPaymentDate,
                                                             DATEDIFF(curdate(),DATE(last_payment_date))  as DSLP,DATEDIFF(now(),disbursed_at) as loanAge,
                    
                                                             IF(l.loan_term < DATEDIFF(now(), disbursed_at), (IF(l.loan_balance < 1, ('Completed  Loan'), 'Overdue Loan')), 'Active Loan')  as Status,
                    
                                                             l.paid_amount as TotalPaid, l.expected_amount as totalExpected, l.daily_amount_expected as dailyExpected,
                                                             floor(IFNULL(paid_amount,0) / l.daily_amount_expected) as daysPaid
                                                            from loans l
                                                            inner join users u on u.id = l.userID left join client_assets ca ON l.asset_id = ca.id  left join `groups` g ON g.id = u.group_id
                                                            where l.disbursed_at is not null AND application_branch = ? AND loan_balance > 0 ORDER BY l.createdAt DESC )x
                    """;

            return jdbcTemplateOne.query(sql,(rs,i)->mapRowToPendingLoanData(rs),branch);
        }




    }


    public PendingLoanData getActiveLoanBalanceDetails(String loanAccount) {

        String sql = """
                
                   SELECT  id,l_plate,first_name,last_name,phone,patner_id,group_id,Account,Principal,TotalLoan,Term,loanAge,LoanBalance,DisburseDate,LastPaymentDate,DSLP,
                               Status,TotalPaid,totalExpected,
                       IF(TotalPaid > totalExpected, 0, totalExpected - TotalPaid)                as variance,
                       ROUND(IF(TotalPaid > totalExpected, 0, totalExpected - TotalPaid) / dailyExpected) as varRatio,
                               dailyExpected,daysPaid
                       from (
                               select ca.l_plate, u.id,u.first_name,u.patner_id,u.group_id,u.last_name,u.phone,l.loanAccountMPesa as Account,l.loanPrincipal as Principal,
                               l.client_loan_total as TotalLoan,l.loan_term as Term,l.loan_balance as LoanBalance, DATE(l.disbursed_at) as DisburseDate,
                       IFNULL(DATE(last_payment_date),'NO PAYMENT YET') as LastPaymentDate,
                       DATEDIFF(curdate(),DATE(last_payment_date))  as DSLP,DATEDIFF(now(),disbursed_at) as loanAge,
                       IF(l.loan_term < DATEDIFF(now(), disbursed_at), 'Overdue  Loan', 'Active Loan')  as Status,
                       l.paid_amount as TotalPaid, l.expected_amount as totalExpected, l.daily_amount_expected as dailyExpected,
                       floor(IFNULL(paid_amount,0) / l.daily_amount_expected) as daysPaid
                       from loans l
                       inner join users u on u.id = l.userID
                       left join client_assets ca ON l.asset_id = ca.id
                       where  loanAccountMPesa = ?
                                         )x
                """;

        return jdbcTemplateOne.queryForObject(sql,(rs,i)->mapRowToPendingLoanData(rs),loanAccount);
    }




    public List<PendingLoanData> getInActiveLoansBalances(int branch) {

        String sql;

        if(branch == 0) {
            sql = """                
                     SELECT  id,Account,first_name,middle_name,last_name,phone,client_age,all_loans_taken,patner_id,group_id,stage_name,
                             stage_county,stage_sub_county,stage_ward,l_plate,brand,make, model,
                              Principal,TotalLoan,Term,loanAge,LoanBalance,DisburseDate,LastPaymentDate,DSLP,Status,TotalPaid,totalExpected,
                              IF(TotalPaid > totalExpected, 0, totalExpected - TotalPaid)                as variance,
                              ROUND(IF(TotalPaid > totalExpected, 0, totalExpected - TotalPaid) / dailyExpected) as varRatio,
                              dailyExpected,daysPaid
                                           from (
                                               select
                                                   g.group_name as stage_name,
                                                   (SELECT name from counties WHERE county_id = (SELECT county_id from `groups` where id=g.id)) as stage_county,
                                                   (SELECT name from sub_counties WHERE sub_county_id = (SELECT sub_county_id from `groups` where id=g.id)) as stage_sub_county,
                                                   (SELECT name from wards WHERE ward_id = (SELECT ward_id from `groups` where id=g.id)) as stage_ward,
                                                   ca.l_plate,ca.brand,ca.make, ca.model, u.id,u.first_name,u.middle_name,u.last_name,u.phone,
                                                     (SELECT TIMESTAMPDIFF(YEAR, dob, CURDATE()) AS client_age FROM users WHERE id = u.id) as client_age,
                                                     u.patner_id,u.group_id,l.loanAccountMPesa as Account,
                                                     (SELECT COUNT(userID) FROM loans WHERE userID=u.id) as all_loans_taken,
                                                     l.loanPrincipal as Principal,
                                                     l.client_loan_total as TotalLoan,l.loan_term as Term,l.loan_balance as LoanBalance, DATE(l.disbursed_at) as DisburseDate,
                                           IFNULL(DATE(last_payment_date),'NO PAYMENT YET') as LastPaymentDate,
                                           DATEDIFF(curdate(),DATE(last_payment_date))  as DSLP,DATEDIFF(now(),disbursed_at) as loanAge,
                     
                                           IF(l.loan_term < DATEDIFF(now(), disbursed_at), (IF(l.loan_balance < 1, ('Completed  Loan'), 'Overdue Loan')), 'Active Loan')  as Status,
                     
                                           l.paid_amount as TotalPaid, l.expected_amount as totalExpected, l.daily_amount_expected as dailyExpected,
                                           floor(IFNULL(paid_amount,0) / l.daily_amount_expected) as daysPaid
                                          from loans l
                                          inner join users u on u.id = l.userID left join client_assets ca ON l.asset_id = ca.id  left join `groups` g ON g.id = u.group_id
                                          where l.disbursed_at is not null AND loan_balance < 1 ORDER BY l.createdAt DESC)x
                """;

            return jdbcTemplateOne.query(sql, (rs, i) -> mapRowToPendingLoanData(rs));
        }else{

            sql = """                
                     SELECT  id,Account,first_name,middle_name,last_name,phone,client_age,all_loans_taken,patner_id,group_id,stage_name,
                             stage_county,stage_sub_county,stage_ward,l_plate,brand,make, model,
                              Principal,TotalLoan,Term,loanAge,LoanBalance,DisburseDate,LastPaymentDate,DSLP,Status,TotalPaid,totalExpected,
                              IF(TotalPaid > totalExpected, 0, totalExpected - TotalPaid)                as variance,
                              ROUND(IF(TotalPaid > totalExpected, 0, totalExpected - TotalPaid) / dailyExpected) as varRatio,
                              dailyExpected,daysPaid
                                           from (
                                               select
                                                   g.group_name as stage_name,
                                                   (SELECT name from counties WHERE county_id = (SELECT county_id from `groups` where id=g.id)) as stage_county,
                                                   (SELECT name from sub_counties WHERE sub_county_id = (SELECT sub_county_id from `groups` where id=g.id)) as stage_sub_county,
                                                   (SELECT name from wards WHERE ward_id = (SELECT ward_id from `groups` where id=g.id)) as stage_ward,
                                                   ca.l_plate,ca.brand,ca.make, ca.model, u.id,u.first_name,u.middle_name,u.last_name,u.phone,
                                                     (SELECT TIMESTAMPDIFF(YEAR, dob, CURDATE()) AS client_age FROM users WHERE id = u.id) as client_age,
                                                     u.patner_id,u.group_id,l.loanAccountMPesa as Account,
                                                     (SELECT COUNT(userID) FROM loans WHERE userID=u.id) as all_loans_taken,
                                                     l.loanPrincipal as Principal,
                                                     l.client_loan_total as TotalLoan,l.loan_term as Term,l.loan_balance as LoanBalance, DATE(l.disbursed_at) as DisburseDate,
                                           IFNULL(DATE(last_payment_date),'NO PAYMENT YET') as LastPaymentDate,
                                           DATEDIFF(curdate(),DATE(last_payment_date))  as DSLP,DATEDIFF(now(),disbursed_at) as loanAge,
                     
                                           IF(l.loan_term < DATEDIFF(now(), disbursed_at), (IF(l.loan_balance < 1, ('Completed  Loan'), 'Overdue Loan')), 'Active Loan')  as Status,
                     
                                           l.paid_amount as TotalPaid, l.expected_amount as totalExpected, l.daily_amount_expected as dailyExpected,
                                           floor(IFNULL(paid_amount,0) / l.daily_amount_expected) as daysPaid
                                          from loans l
                                          inner join users u on u.id = l.userID left join client_assets ca ON l.asset_id = ca.id  left join `groups` g ON g.id = u.group_id
                                          where l.disbursed_at is not null AND application_branch = ? loan_balance < 1 ORDER BY l.createdAt DESC)x
                """;

            return jdbcTemplateOne.query(sql, (rs, i) -> mapRowToPendingLoanData(rs), branch);
        }
    }



    public List<PendingLoanData> getAllSystemLoanBalances(int branch) {

        String sql;

        if(branch == 0) {
            sql = """                
                     SELECT  id,Account,first_name,middle_name,last_name,phone,client_age,all_loans_taken,patner_id,group_id,stage_name,
                             stage_county,stage_sub_county,stage_ward,l_plate,brand,make, model,
                              Principal,TotalLoan,Term,loanAge,LoanBalance,DisburseDate,LastPaymentDate,DSLP,Status,TotalPaid,totalExpected,
                              IF(TotalPaid > totalExpected, 0, totalExpected - TotalPaid)                as variance,
                              ROUND(IF(TotalPaid > totalExpected, 0, totalExpected - TotalPaid) / dailyExpected) as varRatio,
                              dailyExpected,daysPaid
                                           from (
                                               select
                                                   g.group_name as stage_name,
                                                   (SELECT name from counties WHERE county_id = (SELECT county_id from `groups` where id=g.id)) as stage_county,
                                                   (SELECT name from sub_counties WHERE sub_county_id = (SELECT sub_county_id from `groups` where id=g.id)) as stage_sub_county,
                                                   (SELECT name from wards WHERE ward_id = (SELECT ward_id from `groups` where id=g.id)) as stage_ward,
                                                   ca.l_plate,ca.brand,ca.make, ca.model, u.id,u.first_name,u.middle_name,u.last_name,u.phone,
                                                     (SELECT TIMESTAMPDIFF(YEAR, dob, CURDATE()) AS client_age FROM users WHERE id = u.id) as client_age,
                                                     u.patner_id,u.group_id,l.loanAccountMPesa as Account,
                                                     (SELECT COUNT(userID) FROM loans WHERE userID=u.id) as all_loans_taken,
                                                     l.loanPrincipal as Principal,
                                                     l.client_loan_total as TotalLoan,l.loan_term as Term,l.loan_balance as LoanBalance, DATE(l.disbursed_at) as DisburseDate,
                                           IFNULL(DATE(last_payment_date),'NO PAYMENT YET') as LastPaymentDate,
                                           DATEDIFF(curdate(),DATE(last_payment_date))  as DSLP,DATEDIFF(now(),disbursed_at) as loanAge,
                     
                                           IF(l.loan_term < DATEDIFF(now(), disbursed_at), (IF(l.loan_balance < 1, ('Completed  Loan'), 'Overdue Loan')), 'Active Loan')  as Status,
                     
                                           l.paid_amount as TotalPaid, l.expected_amount as totalExpected, l.daily_amount_expected as dailyExpected,
                                           floor(IFNULL(paid_amount,0) / l.daily_amount_expected) as daysPaid
                                          from loans l
                                          inner join users u on u.id = l.userID left join client_assets ca ON l.asset_id = ca.id  left join `groups` g ON g.id = u.group_id
                                          where l.disbursed_at is not null  ORDER BY l.createdAt DESC)x
                """;

            return jdbcTemplateOne.query(sql, (rs, i) -> mapRowToPendingLoanData(rs));
        }else {
            sql = """                
                     SELECT  id,Account,first_name,middle_name,last_name,phone,client_age,all_loans_taken,patner_id,group_id,stage_name,
                             stage_county,stage_sub_county,stage_ward,l_plate,brand,make, model,
                              Principal,TotalLoan,Term,loanAge,LoanBalance,DisburseDate,LastPaymentDate,DSLP,Status,TotalPaid,totalExpected,
                              IF(TotalPaid > totalExpected, 0, totalExpected - TotalPaid)                as variance,
                              ROUND(IF(TotalPaid > totalExpected, 0, totalExpected - TotalPaid) / dailyExpected) as varRatio,
                              dailyExpected,daysPaid
                                           from (
                                               select
                                                   g.group_name as stage_name,
                                                   (SELECT name from counties WHERE county_id = (SELECT county_id from `groups` where id=g.id)) as stage_county,
                                                   (SELECT name from sub_counties WHERE sub_county_id = (SELECT sub_county_id from `groups` where id=g.id)) as stage_sub_county,
                                                   (SELECT name from wards WHERE ward_id = (SELECT ward_id from `groups` where id=g.id)) as stage_ward,
                                                   ca.l_plate,ca.brand,ca.make, ca.model, u.id,u.first_name,u.middle_name,u.last_name,u.phone,
                                                     (SELECT TIMESTAMPDIFF(YEAR, dob, CURDATE()) AS client_age FROM users WHERE id = u.id) as client_age,
                                                     u.patner_id,u.group_id,l.loanAccountMPesa as Account,
                                                     (SELECT COUNT(userID) FROM loans WHERE userID=u.id) as all_loans_taken,
                                                     l.loanPrincipal as Principal,
                                                     l.client_loan_total as TotalLoan,l.loan_term as Term,l.loan_balance as LoanBalance, DATE(l.disbursed_at) as DisburseDate,
                                           IFNULL(DATE(last_payment_date),'NO PAYMENT YET') as LastPaymentDate,
                                           DATEDIFF(curdate(),DATE(last_payment_date))  as DSLP,DATEDIFF(now(),disbursed_at) as loanAge,
                     
                                           IF(l.loan_term < DATEDIFF(now(), disbursed_at), (IF(l.loan_balance < 1, ('Completed  Loan'), 'Overdue Loan')), 'Active Loan')  as Status,
                     
                                           l.paid_amount as TotalPaid, l.expected_amount as totalExpected, l.daily_amount_expected as dailyExpected,
                                           floor(IFNULL(paid_amount,0) / l.daily_amount_expected) as daysPaid
                                          from loans l
                                          inner join users u on u.id = l.userID left join client_assets ca ON l.asset_id = ca.id  left join `groups` g ON g.id = u.group_id
                                          where l.disbursed_at is not null AND application_branch = ? ORDER BY l.createdAt DESC)x
                """;

            return jdbcTemplateOne.query(sql, (rs, i) -> mapRowToPendingLoanData(rs),branch);
        }
    }



    private PendingLoanData mapRowToPendingLoanData(ResultSet rs) throws SQLException {
        PendingLoanData loanData = new PendingLoanData();
        loanData.setUserId(rs.getInt("id"));
        loanData.setAssetPlate(rs.getString("l_plate"));
        loanData.setFirstName(rs.getString("first_name"));
        loanData.setLastName(rs.getString("last_name"));
        loanData.setPhone(rs.getString("phone"));

        try{
            loanData.setOnlineRider(rs.getBoolean("is_online_rider"));
        } catch (Exception ignored) {}



        loanData.setPartnerId(rs.getInt("patner_id"));
        loanData.setGroupId(rs.getInt("group_id"));
        loanData.setAccount(rs.getString("Account"));
        loanData.setPrincipal(rs.getDouble("Principal"));
        loanData.setTotalLoan(rs.getDouble("TotalLoan"));
        loanData.setTerm(rs.getInt("Term"));
        loanData.setLoanAge(rs.getInt("loanAge"));
        loanData.setLoanBalance(rs.getDouble("LoanBalance"));
        loanData.setDisburseDate(rs.getString("DisburseDate"));
        loanData.setLastPaymentDate(rs.getString("LastPaymentDate"));
        loanData.setDspl(rs.getInt("DSLP"));
        loanData.setStatus(rs.getString("Status"));
        loanData.setTotalPaid(rs.getDouble("TotalPaid"));
        loanData.setTotalExpected(rs.getDouble("totalExpected"));
        loanData.setVariance(rs.getDouble("variance"));
        loanData.setVarRatio(rs.getDouble("varRatio"));
        loanData.setDailyExpected(rs.getDouble("dailyExpected"));
        loanData.setDaysPaid(rs.getInt("daysPaid"));
        loanData.setMiddleName(rs.getString("middle_name"));
        loanData.setClientAge(rs.getInt("client_age"));
        loanData.setAllLoansTaken(rs.getInt("all_loans_taken"));
        loanData.setStageName(rs.getString("stage_name"));
        loanData.setStageCounty(rs.getString("stage_county"));
        loanData.setStageSubCounty(rs.getString("stage_sub_county"));
        loanData.setStageWard(rs.getString("stage_ward"));
        loanData.setBikeBrand(rs.getString("brand"));
        loanData.setBikeMake(rs.getString("make"));
        loanData.setBikeModel(rs.getString("model"));

        return loanData;
    }


    public List<PendingDisbursement> getAllPendingDisburesments() {

        String sql = """
                SELECT u.first_name,u.last_name, u.group_id,loanAccountMPesa as account,userID, loanPrincipal,total_loan_disburse as disburse_amount, client_loan_total as total_loan, daily_amount_expected,
                loan_term, loanPurpose, loanStatus,(SELECT name from partners WHERE id = loans.application_branch) as branch, loanStatusLevelOne, loanStatusLevelTwo,  createdAt as application_date, l_one_update_comment, l_two_update_comment
                FROM loans
                left join users u ON u.id = loans.userID
                WHERE disbursed_at is null AND loanStatus = 'PENDING_APPROVAL'
                AND u.group_id is not null AND u.group_id != '999999'
                ORDER BY application_date DESC
                """;

        return jdbcTemplateOne.query(sql,(rs,i)->mapRowToPendingDisbursement(rs));
    }

    public List<PendingDisbursement> getAllFailedDisburesments() {

        String sql = """
                SELECT u.first_name,u.last_name, u.group_id,loanAccountMPesa as account,userID, loanPrincipal,total_loan_disburse as disburse_amount, client_loan_total as total_loan, daily_amount_expected,
                                       loan_term, loanPurpose, loanStatus,(SELECT name from partners WHERE id = loans.application_branch) as branch, loanStatusLevelOne, loanStatusLevelTwo,  createdAt as application_date, l_one_update_comment, l_two_update_comment
                                FROM loans
                                left join users u ON u.id = loans.userID
                                WHERE disburse_initiated =1 AND disbursed_at is null ORDER BY application_date DESC
                """;

        return jdbcTemplateOne.query(sql,(rs,i)->mapRowToPendingDisbursement(rs));
    }

    private PendingDisbursement mapRowToPendingDisbursement(ResultSet rs) throws SQLException {
        PendingDisbursement pendingDisbursement = new PendingDisbursement();
        pendingDisbursement.setFirstName(rs.getString("first_name"));
        pendingDisbursement.setLastName(rs.getString("last_name"));
        pendingDisbursement.setGroupId(rs.getInt("group_id"));
        pendingDisbursement.setAccount(rs.getString("account"));
        pendingDisbursement.setUserId(rs.getInt("userID"));
        pendingDisbursement.setLoanPrincipal(rs.getDouble("loanPrincipal"));
        pendingDisbursement.setDisburseAmount(rs.getDouble("disburse_amount"));
        pendingDisbursement.setTotalLoan(rs.getDouble("total_loan"));
        pendingDisbursement.setDailyAmountExpected(rs.getDouble("daily_amount_expected"));
        pendingDisbursement.setLoanTerm(rs.getInt("loan_term"));
        pendingDisbursement.setLoanPurpose(rs.getString("loanPurpose"));
        pendingDisbursement.setLoanStatus(rs.getString("loanStatus"));
        pendingDisbursement.setLoanStatusLevelOne(rs.getString("loanStatusLevelOne"));
        pendingDisbursement.setLoanStatusLevelTwo(rs.getString("loanStatusLevelTwo"));
        pendingDisbursement.setApplicationDate(rs.getString("application_date"));
        pendingDisbursement.setLOneUpdateComment(rs.getString("l_one_update_comment"));
        pendingDisbursement.setLTwoUpdateComment(rs.getString("l_two_update_comment"));
        pendingDisbursement.setBranch(rs.getString("branch"));

        return pendingDisbursement;
    }

    public Object reportSpecialCase(SpecialCaseLoan specialCaseLoan, String name) throws Exception{

        SystemUser user = userService.getByEmailOrPhone(name);

                String sql = """
                INSERT INTO special_cases(loan_id, case_category, case_description, created_by, created_at)
                VALUES (?,?,?,?,NOW())
                """;

        return jdbcTemplateOne.update(sql,
                specialCaseLoan.getLoanId(),
                specialCaseLoan.getCategory(),
                specialCaseLoan.getDescription(),
                user.getId()
             );
    }

    public Object reportRedFlag(SpecialCaseLoan specialCaseLoan, String name) {
        SystemUser user = userService.getByEmailOrPhone(name);

        String sql = """
                INSERT INTO red_flag_cases(loan_id, case_category, case_description, created_by, created_at)
                VALUES (?,?,?,?,NOW())
                """;

        return jdbcTemplateOne.update(sql,
                specialCaseLoan.getLoanId(),
                specialCaseLoan.getCategory(),
                specialCaseLoan.getDescription(),
                user.getId()
        );
    }
}
