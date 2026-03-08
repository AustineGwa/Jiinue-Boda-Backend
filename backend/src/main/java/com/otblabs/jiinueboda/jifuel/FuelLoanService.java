package com.otblabs.jiinueboda.jifuel;

import com.otblabs.jiinueboda.integrations.momo.mpesa.MpesaTransactionsService;
import com.otblabs.jiinueboda.integrations.momo.mpesa.b2c.models.MpesaCommandId;
import com.otblabs.jiinueboda.integrations.momo.mpesa.buygoods.models.BuyGoodsRequest;
import com.otblabs.jiinueboda.integrations.momo.mpesa.buygoods.models.BuygoodsRequestResponse;
import com.otblabs.jiinueboda.integrations.momo.mpesa.paybill.PaybillRequest;
import com.otblabs.jiinueboda.jifuel.models.*;
import com.otblabs.jiinueboda.jifuel.petrolstations.PaymentMode;
import com.otblabs.jiinueboda.jifuel.petrolstations.PetrolStation;
import com.otblabs.jiinueboda.users.models.SystemUser;
import com.otblabs.jiinueboda.users.UserService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
public class FuelLoanService {

    private final JdbcTemplate jdbcTemplateOne;
    private final UserService userService;
    private final MpesaTransactionsService mpesaTransactionsService;

    public FuelLoanService(JdbcTemplate jdbcTemplateOne, UserService userService, MpesaTransactionsService mpesaTransactionsService) {
        this.jdbcTemplateOne = jdbcTemplateOne;
        this.userService = userService;
        this.mpesaTransactionsService = mpesaTransactionsService;
    }


    public Object requestFuelLoan(FuelLoan fuelLoan, String name) throws Exception{

        SystemUser systemUser = userService.getByEmailOrPhone(name);

        PetrolStation petrolStation = fuelLoan.getPetrolStation();
        String insertStationSql =  "INSERT INTO petrol_station(name,paymentMode,latitude,longitude,playbillNumber,accountNumber,tillNumber) VALUES(?,?,?,?,?,?,?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplateOne.update( connection -> {

                    PreparedStatement ps = connection.prepareStatement(insertStationSql, new String[]{"id"});
                    ps.setString(1, petrolStation.getName());
                    ps.setString(2, petrolStation.getPaymentMode().name());
                    ps.setDouble(3, petrolStation.getLatitude());
                    ps.setDouble(4, petrolStation.getLongitude());
                    ps.setString(5, petrolStation.getPlaybillNumber());
                    ps.setString(6, petrolStation.getAccountNumber());
                    ps.setString(7, petrolStation.getTillNumber());
                    return ps;
                },keyHolder);

        Number petrolStationId = keyHolder.getKey();

        String sql = """
        INSERT INTO fuel_loan(userID,appID,loanID,loanPrincipal, interestPercentage,petrolStationID,createdAt,status,fuel_amount_purchased)
         VALUES (?,?,?,?,?,?,NOW(),?,?)
        """;
        fuelLoan.setStatus("APPROVED");
        String loanId = getLoadId();
        double interest = 10;
        double fuelLoanPurchased = fuelLoan.getLoanPrincipal() * ((100 - interest)/100);
        return jdbcTemplateOne.update(sql,systemUser.getId(),systemUser.getAppId(),loanId,fuelLoan.getLoanPrincipal(),interest,petrolStationId,fuelLoan.getStatus(),fuelLoanPurchased);
    }

    public boolean userHasPendingLoan(SystemUser systemUser) {

        String sql = """
            SELECT * from (
                         SELECT X.loanID, ifnull((X.loanPrincipal - total_paid),0) as balance FROM (
                            SELECT T1.* ,IFNULL((SELECT Sum(TransAmount) FROM mpesa_c2b WHERE BillRefNumber=T1.loanID),0) as total_paid FROM fuel_loan T1 WHERE userID=? AND  disbursedAt is not null
                                          )X
                        ) y WHERE balance > 0
            """;
        List<String> loanIds = jdbcTemplateOne.query(sql,(rs,i)-> rs.getString("loanID"),systemUser.getId());
        return loanIds.isEmpty();
    }

    public int getPartnerBalance(int partnerId) {

        // Execute the SET statement first
        String setSql = "SET @partnerid = ?";
        jdbcTemplateOne.update(setSql, partnerId);

        String sql = """
                
                SELECT (SELECT IFNULL((SELECT jifuel_monthly_budget from partners where id = @partnerid),0) as maximum_amount_balance) - IFNULL((SELECT sum(balance) FROM (
                                                                                SELECT  (T1.loanPrincipal - COALESCE((
                                                                                    SELECT SUM(TransAmount)
                                                                                    FROM mpesa_c2b
                                                                                    WHERE BillRefNumber = T1.loanID
                                                                                ), 0)) AS balance
                                                                                FROM fuel_loan T1
                                                                                LEFT JOIN petrol_station T2 ON T1.petrolStationID = T2.id
                                                                                WHERE (T1.is_paid != 1 OR T1.is_paid IS NULL)
                                                                                AND disbursedAt is not null
                                                                                AND userID IN (SELECT id FROM users WHERE patner_id = @partnerid)
                                                                                AND (
                                                                                    SELECT COALESCE(SUM(TransAmount), 0)
                                                                                    FROM mpesa_c2b
                                                                                    WHERE BillRefNumber = T1.loanID
                                                                                ) < T1.loanPrincipal) x),0) as total_jifuel_available_balance
                """;

        return jdbcTemplateOne.queryForObject(sql ,(rs,i)-> rs.getInt("total_jifuel_available_balance"));

    }


    public List<FuelLoan> getExistingLoanForUser(String user_) {
            SystemUser user = userService.getByEmailOrPhone(user_);

        String sql = """
       SELECT T1.*, T2.*, (T1.loanPrincipal - COALESCE((
                                            SELECT SUM(TransAmount)
                                            FROM mpesa_c2b
                                            WHERE BillRefNumber = T1.loanPrincipal
                                        ), 0)) AS balance
                                        FROM fuel_loan T1
                                        LEFT JOIN petrol_station T2 ON T1.petrolStationID = T2.id
                                        WHERE (T1.is_paid != 1 OR T1.is_paid IS NULL)
                                        AND T1.userID = ?
                                        AND T1.disbursedAt is not null
                                        AND (
                                            SELECT COALESCE(SUM(TransAmount), 0)
                                            FROM mpesa_c2b
                                            WHERE BillRefNumber = T1.loanID
                                        ) < T1.loanPrincipal
                                        ORDER BY balance ASC;
   """;
            return jdbcTemplateOne.query(sql,(rs,i)->mapRowToFuelLoan(rs),user.getId());
    }

    /*
      //INTERVIEW QUIZ
      I have a loan product with very high chances of default.
      I am giving each loan at SH 500 at a 10% interest daily.
      if 3/4 of the people I give fail to pay and only 1/4 pays
      (i)
      Write a java function that given the opening balance it will give us a closing balance
      (ii)
      how much interest do I need on the opening balance in order to close at a 2% profit
      (iii)
      what percentage rapayment do I need in order to close at a 2% profit

      NOTE
      The interest is deducted before disbursing the loan.

      SOLUTION

       x = openingBalance
       Y = individual loanPrinciple = 500
       z = loanInterest = (10%)
       w = closing balance = (1.1x)

       dailyInterest = 0.1X
       totalDisbursed = x - (10%x) = 0.9x
       totalDailyCollection = 0.25 *  0.9X = 0.225x
       totalDailyLostAmount = 0.75 × 0.9X = 0.675x

       -to get daily 1% return profit on openingBalance
       (w = 1.01x) = (totalDailyCollection = 0.225x) + (dailyInterest = 0.1x)
       1.01x = 0.225x + 0.1x
       1.01x = 0.325x
       0 = 0.325x + 1.01x
       0= -0.685x
       x = 1/0.685
       x=1.46
       */
    public static void lossCalculator() {
        double individualLoanPrincipal = 500;
        double interestRate = 0.1;
        double defaultRate = 0.05;
        double successRate = 0.95;

        List.of(1_00_000.00, 2_00_000.00).forEach(openingBalance -> {
            double interestDeducted = openingBalance * interestRate;
            double totalDisbursed = openingBalance - interestDeducted;
            double totalDailyCollection = openingBalance * successRate;
            double totalDailyLost = totalDisbursed * defaultRate;

            double closingBalance = interestDeducted + totalDailyCollection;
            if (closingBalance > openingBalance) {

            }
        });
    }


    private String getLoadId() {
        String CHARACTERS = "23456789ABCDEFGHJKMNPQRSTUVWXYZ";
        int LENGTH = 5;
        Random RANDOM = new Random();

        StringBuilder loanID = new StringBuilder(LENGTH);

        for (int i = 0; i < 5; i++) {
            int randomIndex = RANDOM.nextInt(CHARACTERS.length());
            loanID.append(CHARACTERS.charAt(randomIndex));
        }

        String generatedID = loanID.toString();

        if(isUniqueInDb(generatedID)){
            return generatedID;
        }else{
            return getLoadId();
        }

    }

    private boolean isUniqueInDb(String generatedID) {
        String sql = "SELECT COUNT(*) FROM fuel_loan WHERE loanID = ?";
        int count = jdbcTemplateOne.queryForObject(sql, new Object[]{generatedID}, Integer.class);
        return count == 0;
    }

    public List<FuelLoan> getPaidFuelLoanRequests() throws Exception {
        String sql = """
            SELECT T1.*, T2.*, (T1.loanPrincipal - COALESCE((
                                        SELECT SUM(TransAmount)
                                        FROM mpesa_c2b
                                        WHERE BillRefNumber = T1.loanID
                                    ), 0)) AS balance
                                    FROM fuel_loan T1
                                    LEFT JOIN petrol_station T2 ON T1.petrolStationID = T2.id
                                    WHERE (T1.is_paid != 1 OR T1.is_paid IS NULL)
                                    AND disbursedAt is not null
                                    AND (
                                        SELECT COALESCE(SUM(TransAmount), 0)
                                        FROM mpesa_c2b
                                        WHERE BillRefNumber = T1.loanID
                                    ) = T1.loanPrincipal
            
                                    ORDER BY createdAt DESC
""";
        return jdbcTemplateOne.query(sql,(rs,i)->mapRowToFuelLoan(rs));
    }

    public List<FuelLoan> getPartnerPaidFuelLoanRequests(int partnerId) throws Exception {
        String sql = """
            SELECT T1.*, T2.*, (T1.loanPrincipal - COALESCE((
                                        SELECT SUM(TransAmount)
                                        FROM mpesa_c2b
                                        WHERE BillRefNumber = T1.loanID
                                    ), 0)) AS balance
                                    FROM fuel_loan T1
                                    LEFT JOIN petrol_station T2 ON T1.petrolStationID = T2.id
                                    WHERE (T1.is_paid != 1 OR T1.is_paid IS NULL)
                                    AND disbursedAt is not null
                                    AND userID IN (SELECT id FROM users WHERE patner_id = ?) 
                                    AND (
                                        SELECT COALESCE(SUM(TransAmount), 0)
                                        FROM mpesa_c2b
                                        WHERE BillRefNumber = T1.loanID
                                    ) = T1.loanPrincipal
            
                                    ORDER BY createdAt DESC
""";
        return jdbcTemplateOne.query(sql,(rs,i)->mapRowToFuelLoan(rs),partnerId);
    }

    public List<FuelLoan> getUnpaidPaidFuelLoanRequests() throws Exception {
        String sql = """
            SELECT T1.*, T2.*, (T1.loanPrincipal - COALESCE((
                                        SELECT SUM(TransAmount)
                                        FROM mpesa_c2b
                                        WHERE BillRefNumber = T1.loanID
                                    ), 0)) AS balance
                                    FROM fuel_loan T1
                                    LEFT JOIN petrol_station T2 ON T1.petrolStationID = T2.id
                                    WHERE (T1.is_paid != 1 OR T1.is_paid IS NULL)
                                    AND disbursedAt is not null
                                    AND (
                                        SELECT COALESCE(SUM(TransAmount), 0)
                                        FROM mpesa_c2b
                                        WHERE BillRefNumber = T1.loanID
                                    ) < T1.loanPrincipal
            
                                    ORDER BY createdAt DESC
""";
        return jdbcTemplateOne.query(sql,(rs,i)->mapRowToFuelLoan(rs));
    }

    public List<FuelLoan> getPartnerUnpaidPaidFuelLoanRequests(int partnerId) throws Exception {
        String sql = """
             SELECT T1.*, T2.*, (T1.loanPrincipal - COALESCE((
                                                    SELECT SUM(TransAmount)
                                                    FROM mpesa_c2b
                                                    WHERE BillRefNumber = T1.loanID
                                                ), 0)) AS balance
                                                FROM fuel_loan T1
                                                LEFT JOIN petrol_station T2 ON T1.petrolStationID = T2.id
                                                WHERE (T1.is_paid != 1 OR T1.is_paid IS NULL)
                                                AND disbursedAt is not null
                                                AND userID IN (SELECT id FROM users WHERE patner_id = ?) 
                                                AND (
                                                    SELECT COALESCE(SUM(TransAmount), 0)
                                                    FROM mpesa_c2b
                                                    WHERE BillRefNumber = T1.loanID
                                                ) < T1.loanPrincipal
            
                                                ORDER BY createdAt DESC
""";
        return jdbcTemplateOne.query(sql,(rs,i)->mapRowToFuelLoan(rs),partnerId);
    }



    public List<FuelLoan> getAllPendingFuelDisbursements() {
        String sql = """
        SELECT T1.*, T2.*  FROM fuel_loan T1 LEFT JOIN petrol_station T2 ON T1.petrolStationID = T2.id
                WHERE status = 'APPROVED' AND disburseInitiated != 1 AND disbursedAt is null
        """;
        return jdbcTemplateOne.query(sql,(rs,i)->mapRowToFuelLoan(rs));
    }

    public BuygoodsRequestResponse disburseFuelLoan(FuelLoan loan) throws Exception {
        SystemUser user = userService.getUserByID(loan.getUserID());
        if(loan.getPetrolStation().getPaymentMode().equals(PaymentMode.BUYGOODS)){
            BuyGoodsRequest request = new BuyGoodsRequest();
            request.setCommandID(MpesaCommandId.BusinessBuyGoods);
            request.setAmount(loan.getFuelLoanPurchased());
            request.setPartyB(loan.getPetrolStation().getTillNumber());
            request.setAccountReference(loan.getLoanID());
            request.setRequester(user.getPhone());
            request.setRemarks("Payment being processed");
            return mpesaTransactionsService.initiateBuyGoods(request, loan.getAppID());
        }else if(loan.getPetrolStation().getPaymentMode().equals(PaymentMode.PAYBILL)){
            PaybillRequest request = new PaybillRequest();
            request.setCommandID(MpesaCommandId.BusinessPayBill);
            request.setAmount(loan.getLoanPrincipal());
            request.setPartyB(loan.getPetrolStation().getTillNumber());
            request.setAccountReference(loan.getLoanID());
            request.setRequester(user.getPhone());
            request.setRemarks("Payment being processed");

//            TODO add return type
            mpesaTransactionsService.initiatePaybill(request, loan.getAppID());
        }
        return null;
    }

    private FuelLoan mapRowToFuelLoan(ResultSet rs) throws SQLException {
        FuelLoan fuelLoan = new FuelLoan();
        fuelLoan.setAppID(rs.getInt("appID"));
        fuelLoan.setUserID(rs.getInt("userID"));
        fuelLoan.setLoanID(rs.getString("loanId"));
        fuelLoan.setLoanPrincipal(rs.getInt("loanPrincipal"));
        fuelLoan.setFuelLoanPurchased(rs.getInt("fuel_amount_purchased"));
        fuelLoan.setDisbursedAt(rs.getString("disbursedAt"));
        fuelLoan.setMpesaDisburseConversationID(rs.getString("mpesa_disburse_conversation_id"));

        PetrolStation petrolStation = new PetrolStation();

        try{
            petrolStation.setPaymentMode(PaymentMode.valueOf(rs.getString("paymentMode")));
        }catch (Exception ignored){}
        petrolStation.setName(rs.getString("name"));
        petrolStation.setTillNumber(rs.getString("tillNumber"));
        fuelLoan.setPetrolStation(petrolStation);
        try{
            fuelLoan.setBalance(rs.getInt("balance"));
        }catch (Exception ignored){}

        SystemUser systemUser = new SystemUser();

        try{
            systemUser.setFirstName(rs.getString("first_name"));
        }catch (Exception ignored){}

        try{
            systemUser.setMiddleName(rs.getString("middle_name"));
        }catch (Exception ignored){}

        try{
            systemUser.setLastName(rs.getString("last_name"));
        }catch (Exception ignored){}

        try{
            systemUser.setPhone(rs.getString("phone"));
        }catch (Exception ignored){}

        try{
            systemUser.setNationalID(rs.getString("nationalId"));
        }catch (Exception ignored){}

        return fuelLoan;
    }


    public void setLoanDisbursementInitiated(String loanID) {
        String sql = "UPDATE fuel_loan SET disburseInitiated=1 WHERE loanID=?";
        jdbcTemplateOne.update(sql,loanID);
    }

    public void updateMpesaConversionId(BuygoodsRequestResponse buygoodsRequestResponse, FuelLoan fuelLoan) {
        String updateConversationId = "UPDATE  fuel_loan  SET mpesa_disburse_conversation_id=? WHERE loanID=?";
        jdbcTemplateOne.update(updateConversationId,buygoodsRequestResponse.getConversationID(),fuelLoan.getLoanID());
    }

    public DashboardData getMainDashboardData() {

        String sql = """
                SELECT
                                        COUNT(CASE WHEN DATE(createdAt) = CURDATE() THEN id END) AS loans_today,
                                        COUNT(id) AS all_loans_todate,
                                        SUM(CASE WHEN DATE(createdAt) = CURDATE() THEN fuel_amount_purchased ELSE 0 END) AS total_fuel_purchased_today,
                                        SUM(fuel_amount_purchased) AS total_fuel_purchased_todate,
                                        (SELECT COUNT(num)  FROM mpesa_c2b WHERE BillRefNumber IN (SELECT loanID FROM fuel_loan) AND DATE(created_at) = CURDATE() AND MONTH(DATE(created_at))= MONTH(CURDATE()) AND YEAR(DATE(created_at))= YEAR(CURDATE())) as total_repayments_count_today,
                                        (SELECT COUNT(num)  FROM mpesa_c2b WHERE BillRefNumber IN (SELECT loanID FROM fuel_loan)) as total_repayments_count_todate,
                                        (SELECT SUM(TransAmount) as total_repayments  FROM mpesa_c2b WHERE BillRefNumber IN (SELECT loanID FROM fuel_loan) AND DATE(created_at) = CURDATE() AND MONTH(DATE(created_at))= MONTH(CURDATE()) AND YEAR(DATE(created_at))= YEAR(CURDATE()) ) as total_repayments_amount_today,
                                        (SELECT SUM(TransAmount) as total_repayments  FROM mpesa_c2b WHERE BillRefNumber IN (SELECT loanID FROM fuel_loan)) as total_repayments_amount_todate
                                        FROM fuel_loan
                                        WHERE disbursedAt is not null
                """;
       return jdbcTemplateOne.queryForObject(sql,(rs,i)->setDashboardData(rs));
    }

    public DashboardData getPartnerDashboardData(int partnerId) {

        // Execute the SET statement first
        String setSql = "SET @partnerid = ?";
        jdbcTemplateOne.update(setSql, partnerId);

        String sql = """
                                                  
                 SELECT
                                        COUNT(CASE WHEN DATE(createdAt) = CURDATE() THEN id END) AS loans_today,
                                        COUNT(id) AS all_loans_todate,
                                        SUM(CASE WHEN DATE(createdAt) = CURDATE() THEN fuel_amount_purchased ELSE 0 END) AS total_fuel_purchased_today,
                                        SUM(fuel_amount_purchased) AS total_fuel_purchased_todate,
                                        (SELECT COUNT(num)  FROM mpesa_c2b WHERE BillRefNumber IN (SELECT loanID FROM fuel_loan  WHERE userID IN (SELECT id FROM users WHERE patner_id =fuel_loan.userID )) AND DATE(created_at) = CURDATE() AND MONTH(DATE(created_at))= MONTH(CURDATE()) AND YEAR(DATE(created_at))= YEAR(CURDATE())) as total_repayments_count_today,
                                        (SELECT COUNT(num)  FROM mpesa_c2b WHERE BillRefNumber IN (SELECT loanID FROM fuel_loan  WHERE userID IN (SELECT id FROM users WHERE patner_id =fuel_loan.userID ))) as total_repayments_count_todate,
                                        (SELECT SUM(TransAmount) as total_repayments  FROM mpesa_c2b WHERE BillRefNumber IN (SELECT loanID FROM fuel_loan WHERE userID IN (SELECT id FROM users WHERE patner_id =fuel_loan.userID )) AND DATE(created_at) = CURDATE() AND MONTH(DATE(created_at))= MONTH(CURDATE()) AND YEAR(DATE(created_at))= YEAR(CURDATE()) ) as total_repayments_amount_today,
                                        (SELECT SUM(TransAmount) as total_repayments  FROM mpesa_c2b WHERE BillRefNumber IN (SELECT loanID FROM fuel_loan WHERE userID IN (SELECT id FROM users WHERE patner_id =fuel_loan.userID ))) as total_repayments_amount_todate,
                                        (SELECT (SELECT IFNULL((SELECT jifuel_monthly_budget from partners where id = @partnerid),0) as maximum_amount_balance) - IFNULL((SELECT sum(balance) FROM (
                                                                SELECT  (T1.loanPrincipal - COALESCE((
                                                                    SELECT SUM(TransAmount)
                                                                    FROM mpesa_c2b
                                                                    WHERE BillRefNumber = T1.loanID
                                                                ), 0)) AS balance
                                                                FROM fuel_loan T1
                                                                LEFT JOIN petrol_station T2 ON T1.petrolStationID = T2.id
                                                                WHERE (T1.is_paid != 1 OR T1.is_paid IS NULL)
                                                                AND disbursedAt is not null
                                                                AND userID IN (SELECT id FROM users WHERE patner_id = @partnerid)
                                                                AND (
                                                                    SELECT COALESCE(SUM(TransAmount), 0)
                                                                    FROM mpesa_c2b
                                                                    WHERE BillRefNumber = T1.loanID
                                                                ) < T1.loanPrincipal) x),0)) as total_jifuel_available_balance
                                        FROM fuel_loan
                                        WHERE userID IN (SELECT id FROM users  WHERE patner_id = @partnerid)
                                        AND disbursedAt is not null
                """;

        return jdbcTemplateOne.queryForObject(sql,(rs,i)->setDashboardData(rs));
    }

    private DashboardData setDashboardData(ResultSet rs) throws SQLException {
        DashboardData dashboardData = new DashboardData();
        dashboardData.setLoanCountToday(rs.getInt("loans_today"));
        dashboardData.setLoanCountTodate(rs.getInt("all_loans_todate"));
        dashboardData.setTotalAmountPurchasedToday(rs.getInt("total_fuel_purchased_today"));
        dashboardData.setTotalAmountPurchasedTodate(rs.getInt("total_fuel_purchased_todate"));
        dashboardData.setTotalRepaymentsCountToday(rs.getInt("total_repayments_count_today"));
        dashboardData.setTotalRepaymentsCountTodate(rs.getInt("total_repayments_count_todate"));
        dashboardData.setTotalRepaymentsAmountToday(rs.getInt("total_repayments_amount_today"));
        dashboardData.setTotalRepaymentsAmountTodate(rs.getInt("total_repayments_amount_todate"));

        try{
            dashboardData.setJifuelAvailableUsageBalance(rs.getInt("total_jifuel_available_balance"));
        }catch (Exception ignored){}
        return dashboardData;
    }

    public List<LoansPerDay> getTotalDailyTotalLoans() {

        String sql = """
                SELECT DATE(disbursedAt) AS loan_date, SUM(fuel_amount_purchased) AS total_disbursed
                FROM fuel_loan
                WHERE MONTH(disbursedAt) = MONTH(CURDATE())
                AND YEAR(disbursedAt) = YEAR(CURDATE())
                AND disbursedAt IS NOT NULL
                GROUP BY loan_date
                ORDER BY loan_date;
                """;

        return  jdbcTemplateOne.query(sql,(rs,i) -> setDailyLoans(rs));
    }

    private LoansPerDay setDailyLoans(ResultSet rs) throws SQLException {
        LoansPerDay loansPerDay = new LoansPerDay();
        loansPerDay.setDay(rs.getString("loan_date"));
        loansPerDay.setTotal(rs.getInt("total_disbursed"));
        return loansPerDay;
    }

    public List<MonthlyFinancial> getMonthlyFinancials() {
        String sql = """
                SELECT MONTHNAME(disbursedAt) AS loan_month, SUM(fuel_amount_purchased) AS total_disbursed,
                (SELECT SUM(TransAmount) FROM mpesa_c2b WHERE MONTH(created_at) = MONTH(CURDATE()) AND BillRefNumber in (SELECT loanID from fuel_loan)) as total_repayed
                FROM fuel_loan
                WHERE MONTH(disbursedAt) = MONTH(CURDATE())
                AND YEAR(disbursedAt) = YEAR(CURDATE())
                AND disbursedAt IS NOT NULL
                GROUP BY loan_month
                ORDER BY loan_month;
                """;
        return jdbcTemplateOne.query(sql,(rs,i) -> setMonthlyFinancials(rs));
    }

    private MonthlyFinancial setMonthlyFinancials(ResultSet rs) throws SQLException {
        MonthlyFinancial monthlyFinancial = new MonthlyFinancial();
        monthlyFinancial.setMonth(rs.getString("loan_month"));
        monthlyFinancial.setTotalDisbursed(rs.getInt("total_disbursed"));
        monthlyFinancial.setTotalRepaid(rs.getInt("total_repayed"));
        return monthlyFinancial;
    }

    public List<LoanPerUser> getLoansPerUser() {
        String sql = """
                SELECT u.first_name, u.id, u.last_name, DATE(u.created_at) as signup_on, x.total_loans
                FROM users u
                JOIN (
                  SELECT userID, count(userID) as total_loans
                  FROM fuel_loan
                  WHERE disbursedAt is not null
                  GROUP BY userID
                ) x ON u.id = x.userID
                WHERE u.id IN (
                  SELECT userID
                  FROM (
                    SELECT userID
                    FROM fuel_loan
                    GROUP BY userID
                  ) y
                )
                ORDER BY total_loans DESC
                """;
        return jdbcTemplateOne.query(sql,(rs,i)->setLoanCountPerUser(rs));

    }

    private LoanPerUser setLoanCountPerUser(ResultSet rs) throws SQLException {
        LoanPerUser loanPerUser = new LoanPerUser();
        loanPerUser.setFirstName(rs.getString("first_name"));
        loanPerUser.setLastName(rs.getString("last_name"));
        loanPerUser.setSignupOn(rs.getString("signup_on"));
        loanPerUser.setTotalloans(rs.getInt("total_loans"));
        return loanPerUser;
    }

    public List<LoanTierLimit> getTierLimits(String name) {
//        SystemUser systemUser = userService.getByEmailOrPhone(name);
        String sql = "SELECT id,group_level, tier_name, available_limits, created_at FROM group_loan_levels WHERE group_level =? ";
        return jdbcTemplateOne.query(sql,(rs,i)->setTierLimits(rs),0);
    }

    private LoanTierLimit setTierLimits(ResultSet rs) throws SQLException {
        LoanTierLimit limit = new LoanTierLimit();
        limit.setId(rs.getInt("id"));
        limit.setLevel(rs.getInt("group_level"));
        limit.setTierName(rs.getString("tier_name"));

        String limits = rs.getString("available_limits");
        List<Integer> numbersList = Arrays.stream(limits.split(","))
                .map(Integer::parseInt)
                .toList();
        limit.setAvailableLimits(numbersList);
        limit.setCreatedAt(rs.getString("created_at"));
        return limit;
    }
}
