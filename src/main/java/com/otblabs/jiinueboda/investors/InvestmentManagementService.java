package com.otblabs.jiinueboda.investors;

import com.otblabs.jiinueboda.accounting.expenses.ExpensesService;
import com.otblabs.jiinueboda.accounting.expenses.models.CreateExpense;
import com.otblabs.jiinueboda.accounting.expenses.models.RecieverType;
import com.otblabs.jiinueboda.investors.models.*;
import com.otblabs.jiinueboda.jiinue.models.Loan;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Service
public class InvestmentManagementService {

    private final JdbcTemplate jdbcTemplateOne;
    private final ExpensesService expensesService;

    public InvestmentManagementService(JdbcTemplate jdbcTemplateOne, ExpensesService expensesService) {
        this.jdbcTemplateOne = jdbcTemplateOne;
        this.expensesService = expensesService;
    }

    public int getInvestorByID(int id){
        return 0;
    }

    public Investor getInvestorByUsername(String userName){

        String sql = "SELECT first_name,last_name,p_phone,email,user_name,password FROM investors WHERE user_name =? AND can_login=1 AND deleted_at is null";
        return  jdbcTemplateOne.queryForObject(sql,(rs,position)->{
            Investor investor = new Investor();
            investor.setFirstName(rs.getString("first_name"));
            investor.setLastName(rs.getString("last_name"));
            investor.setPrimaryPhone(rs.getString("p_phone"));
            investor.setEmail(rs.getString("email"));
            investor.setUserName(rs.getString("user_name"));
            investor.setPassword(rs.getString("password"));
            return  investor;
        },userName);
    }

    public int getInvestmentUsageByID(int id){
        return 0;
    }

    public int createInvestmentUsage(InvestmentUsage investmentUsage){
        return 0;
    }

    public int createInvestmentUsageRepayment(InvestmentUsageRepayments investmentUsageRepayments){
        return 0;
    }

    public boolean recordInvestmentRepayment(Loan loan){
       return false;
    }

    @Transactional
    public boolean investorDisburse(Loan loan) {

        double loanAmount = loan.getTotalLoanDisburse() ;
        double loanAmountStatic = loan.getTotalLoanDisburse();

        List<Map<String, Object>> availableInvestors = jdbcTemplateOne.queryForList(
                "SELECT * FROM (" +
                        "SELECT invID, total_invested, total_investment_used, total_investment_repaid, " +
                        "((total_invested - total_investment_used) + (total_investment_repaid - (total_investment_repaid * 0.08))) AS available_funds " +
                        "FROM (" +
                        "SELECT i.id AS invID, i.first_name, i.last_name, " +
                        "(SELECT IFNULL(SUM(j.investment_amount), 0) FROM investments j WHERE j.investor_id = i.id) AS total_invested, " +
                        "(SELECT IFNULL(SUM(k.amount), 0) FROM investment_usage k WHERE k.investor_id = i.id) AS total_investment_used, " +
                        "(SELECT IFNULL(SUM(l.amount_paid), 0) FROM investment_usage_repayments l WHERE l.investor_id = i.id) AS total_investment_repaid " +
                        "FROM investors i" +
                        ") X" +
                        ") Y WHERE (available_funds BETWEEN 1 AND ?) OR available_funds >= ?",
                loanAmount, loanAmount
        );

        double availableInvestorsAmount = availableInvestors.stream()
                .mapToDouble(map -> (double) map.get("available_funds"))
                .sum();

        if (availableInvestorsAmount < loanAmountStatic) {
            return false; // or throw an exception indicating low available funds
        } else {
            String invIDs = availableInvestors.stream()
                    .map(map -> String.valueOf(map.get("invID")))
                    .reduce((s1, s2) -> s1 + "," + s2)
                    .orElse("");

            List<Map<String, Object>> investments = jdbcTemplateOne.queryForList(
                    "SELECT id, investment_balance, investor_id AS invID " +
                            "FROM investments " +
                            "WHERE investor_id IN (" + invIDs + ") AND investment_balance > 0 " +
                            "ORDER BY RAND()"
            );

            for (Map<String, Object> investment : investments) {
                float deductAmount = (float) Math.min(loanAmount, ((Number) investment.get("investment_balance")).floatValue());


                loanAmount -= deductAmount;

                jdbcTemplateOne.update(
                        "INSERT INTO investment_usage (loan_id, investment_id, investor_id, amount, total_loan_percentage, created_at, updated_at) " +
                                "VALUES (?, ?, ?, ?, ?, NOW(), NOW())",loan.getLoanID(), investment.get("id"), investment.get("invID"), deductAmount,
                        (deductAmount / loanAmountStatic) * 100 );

                jdbcTemplateOne.update(
                        "UPDATE investments SET investment_balance = ? WHERE id = ?",
                        ((float) investment.get("investment_balance")) - deductAmount, investment.get("id")
                );

                if (loanAmount <= 0) {
                    break;
                }
            }

            return true;
        }
    }

    @Transactional
    public boolean investorRepayment(int loanId,String transactionCode, double amountPaid) throws Exception{

        String sql = "SELECT * FROM investment_usage WHERE loan_id = ?";
        jdbcTemplateOne.query(sql, (rs, i) -> insertRepayments(rs, transactionCode, amountPaid), loanId);
        String sql2 = "UPDATE  mpesa_c2b SET posted_to_investor_usage_repayments = 1 WHERE TransID =?";
        jdbcTemplateOne.update(sql2,transactionCode);
        return true;
    }

    private boolean insertRepayments(ResultSet rs, String transactionCode, double amountPaid) throws SQLException {
         String sql = """
                 INSERT INTO investment_usage_repayments(investment_usage_id, investor_id, transaction_code, amount_paid, created_at)
                 VALUES(?,?,?,?,NOW())
                 """;
         jdbcTemplateOne.update(sql,
                 rs.getInt("id"),
                 rs.getInt("investor_id"),
                 transactionCode,
                 (rs.getDouble("total_loan_percentage") / 100) * amountPaid
                 );

         return true;
    }

    public boolean checkFailedInvestorRepaymentsEntry() throws Exception{
        String sql = """
                SELECT i.BillRefNumber,i.TransID,i.TransAmount, j.id as loanid FROM mpesa_c2b as i
                LEFT JOIN loans j  ON i.BillRefNumber = j.loanAccountMPesa
                where posted_to_investor_usage_repayments is null
                """;

        jdbcTemplateOne.query(sql, (rs, i) -> {
            try {
                investorRepayment(rs.getInt("loanid"),rs.getString("TransID"), rs.getDouble("TransAmount"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return 0;
        });
        return false;
    }

    public List<InvestorProfile> getAllInvestors() {
        String sql = """
                SELECT id, first_name, last_name, p_phone, s_phone, email, (SELECT Sum(investment_amount) from investments 
                WHERE investor_id = investors.id) as invested_amount,created_at FROM investors
                """;

        return jdbcTemplateOne.query(sql, (rs,i) -> setInvestorProfile(rs));
    }

    private InvestorProfile setInvestorProfile(ResultSet rs) throws SQLException {
        InvestorProfile investorProfile = new InvestorProfile();
        investorProfile.setId(rs.getInt("id"));
        investorProfile.setFirstName(rs.getString("first_name"));
        investorProfile.setLastName(rs.getString("last_name"));
        investorProfile.setPrimaryPhone(rs.getString("p_phone"));
        investorProfile.setSecondaryPhone(rs.getString("s_phone"));
        investorProfile.setEmail(rs.getString("email"));
        investorProfile.setTotalInvested(rs.getInt("invested_amount"));
        investorProfile.setCreatedOn(rs.getString("created_at"));
        return investorProfile;
    }

    public List<Investment> getAllInvestorInvestments(int investorId) {
        String sql = "SELECT id, investment_amount, investment_earning_perc,status, created_at FROM investments WHERE investor_id=?";
        return jdbcTemplateOne.query(sql, (rs,i)->setInvestment(rs),investorId);
    }

    private Investment setInvestment(ResultSet rs) throws SQLException {
        Investment investment = new Investment();
        investment.setId(rs.getInt("id"));
        investment.setAmountInvested(rs.getInt("investment_amount"));
        investment.setInterestPercentage(rs.getDouble("investment_earning_perc"));
        investment.setInvestmentStatus(rs.getBoolean("status"));
        investment.setCreatedAt(rs.getString("created_at"));
        return investment;
    }

    public void sendPeriodicInterestToInvestor(String paybill, String accountNumber){

        CreateExpense createExpense = new CreateExpense();
        createExpense.setAmount(16700);
        createExpense.setMainMainCategoryId(2);
        createExpense.setSubCategoryId(14);
        createExpense.setMinorSubcategoryId(19);
        createExpense.setDescription("investor monthly interest");
        createExpense.setRecieverType(RecieverType.valueOf("MPESA_PAYBILL"));
        createExpense.setReciever(paybill); //change
        createExpense.setAccountNumber(accountNumber);


        try {
            expensesService.createExpense(createExpense);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
