package com.otblabs.jiinueboda.loans;

import com.otblabs.jiinueboda.loans.models.*;
import com.otblabs.jiinueboda.integrations.momo.mpesa.MpesaTransactionsService;
import com.otblabs.jiinueboda.integrations.momo.mpesa.b2c.models.MpesaCommandId;
import com.otblabs.jiinueboda.integrations.momo.mpesa.b2c.models.B2CRequest;
import com.otblabs.jiinueboda.integrations.momo.mpesa.b2c.models.B2CRequestResponse;
import com.otblabs.jiinueboda.sms.SmsService;
import com.otblabs.jiinueboda.users.models.SystemUser;
import com.otblabs.jiinueboda.users.UserService;
import com.otblabs.jiinueboda.users.models.Usertype;
import com.otblabs.jiinueboda.utility.UtilityFunctions;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Service
public class LendingService {


    private final JdbcTemplate jdbcTemplateOne;
    private final UserService userService;
    private final MpesaTransactionsService mpesaTransactionsService;
    private final SmsService smsService;




    public LendingService(JdbcTemplate jdbcTemplateOne, UserService userService, MpesaTransactionsService mpesaTransactionsService, SmsService smsService) {
        this.jdbcTemplateOne = jdbcTemplateOne;
        this.userService = userService;
        this.mpesaTransactionsService = mpesaTransactionsService;
        this.smsService = smsService;
    }

    public List<Loan> getAllPendingDisbursements() {
        String sql = "SELECT * FROM loans WHERE loanStatus = 'APPROVED' AND disburse_initiated is null AND  loanAccountMPesa is not null";
        return jdbcTemplateOne.query(sql,(rs,i)->setLoan(rs));
    }

    public void setLoanDisbursementInitiated(String loanAccount) throws Exception{
        String sql = "UPDATE loans SET disburse_initiated =1 WHERE loanAccountMPesa =?";
        jdbcTemplateOne.update(sql,loanAccount);
    }

    public B2CRequestResponse disburseLoan(Loan loan) throws Exception {
        SystemUser user = userService.getUserByID(loan.getUserID());
        B2CRequest b2CRequest = new B2CRequest();
        b2CRequest.setCommandID(MpesaCommandId.BusinessPayment);
        b2CRequest.setAmount(String.valueOf(loan.getTotalLoanDisburse()));
        b2CRequest.setPartyB(user.getPhone());
        b2CRequest.setRemarks("Disbursement");
        b2CRequest.setAppId(loan.getAppID());
        b2CRequest.setOccasion(loan.getLoanAccountMpesa());
        return mpesaTransactionsService.initiateb2c(b2CRequest, loan.getAppID());
    }

    private Loan setLoan(ResultSet resultSet) throws SQLException {
        Loan loan = new Loan();
        int principal = resultSet.getInt("loanPrincipal");
        int totalLoanDisburse = resultSet.getInt("total_loan_disburse");
        double interest = resultSet.getDouble("interestPercentage");
        double amountDueToday = principal * ((100 + interest) /100);

        loan.setLoanID(resultSet.getInt("id"));
        loan.setUserID(resultSet.getInt("userID"));
        loan.setAppID(resultSet.getInt("appID"));
        loan.setLoanAccountMpesa(resultSet.getString("loanAccountMPesa"));
        loan.setLoanPrincipal(principal);
        loan.setTotalLoanDisburse(totalLoanDisburse);
        loan.setInterestPercentage(interest);
        loan.setLoanPurpose(resultSet.getString("loanPurpose"));
        loan.setGuarantorId1(resultSet.getString("guarantorID_one"));
        loan.setGuarantorId2(resultSet.getString("guarantorID_two"));
        loan.setAmountDueToday(amountDueToday);
        loan.setPaymentDate(resultSet.getString("paymentDate"));
        loan.setDaysOverdue(0);
        loan.setLoanStatus(LoanStatus.valueOf(resultSet.getString("loanStatus")));
        loan.setCreatedAt(resultSet.getString("createdAt"));
        loan.setUpdatedAt(resultSet.getString("updatedAt"));


        return loan;
    }

    public String createNewLoanRequest(NewLoanRequest newLoanRequest, int loanAgreementID,String user) throws Exception {

        //get this user profile
        SystemUser loanApplicant = userService.getUserByID(newLoanRequest.getUserId());

        // Constants
        int appId = 3;
        int userId = newLoanRequest.getUserId();
        int loanAsset = newLoanRequest.getClientAsset();
        double loanPerc = 0.05;
        double loanProcessingPercentage = 0.05;
        int monFee = 600;
        String accountNumber = createLoanId();
        String loanReason = newLoanRequest.getLoanPurpose();
        String guarantor1 = newLoanRequest.getGuarantor1Phone();
        String guarantor2 = newLoanRequest.getGuarantor2Phone();

        // Charges and calculations
        int loanTermDays = newLoanRequest.getLoanTerm();
        double loanTermMonths = loanTermDays / 30.0;
        double ntsaFee = newLoanRequest.getBatteryAmount();
        double loanAmount = newLoanRequest.getLoanPrincipal();
        double procFee = loanAmount * loanProcessingPercentage;

        // Calculate total interest based on loan term in months
        double totalInterest = loanAmount * loanPerc * loanTermMonths;

        // Calculate total monitoring fee based on loan term in months
        double totalMonFee = monFee * loanTermMonths;

        // Calculate client loan total (before insurance and processing fee)
        double clientLoanTotal = loanAmount + totalMonFee + ntsaFee + totalInterest;

        // Calculate credit life insurance with minimum of 300
        double creditLifeInsurance = Math.ceil(clientLoanTotal * 0.02);
        double finalInsurance = Math.max(creditLifeInsurance, 300);

        // Calculate total disbursed amount (subtract processing fee and insurance)
        double totalDisburse = loanAmount - (procFee + finalInsurance);

        // Calculate daily expected amount based on total days
        int dailyExpected = (int) Math.ceil(clientLoanTotal / loanTermDays);

        // Calculate payment date (add loan term days, not months)
        LocalDate payDate = LocalDate.now().plusDays(loanTermDays);

        SystemUser createdBy = userService.getByEmailOrPhone(user);

        String sql = """        
                INSERT INTO loans (userID, appID, asset_id, loanPrincipal, interestPercentage,
                monthly_mon_fee, ntsa_fee, credit_life_insurance, loan_processing_fee,
                total_interest_amount, total_mon_fee, total_loan_disburse, client_loan_total,
                daily_amount_expected, loan_term, loanPurpose, guarantorID_one, guarantorID_two,
                paymentDate, loanStatus, loanAccountMPesa, agreement_attachment_id,created_by, createdAt, updatedAt,tracker_imei,tracker_simcard,application_branch)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?,?)
        """;

        jdbcTemplateOne.update(
                sql,
                userId, appId, loanAsset, loanAmount, loanPerc * 100, monFee, ntsaFee,
                finalInsurance, procFee, totalInterest, totalMonFee, totalDisburse,
                clientLoanTotal, dailyExpected, loanTermDays, loanReason, guarantor1,
                guarantor2, Date.valueOf(payDate), "PENDING_APPROVAL", accountNumber,
                loanAgreementID,
                createdBy.getId(),
                Timestamp.valueOf(LocalDateTime.now()),
                Timestamp.valueOf(LocalDateTime.now()),
                newLoanRequest.getTrackerImei(),
                UtilityFunctions.formatPhoneNumber(newLoanRequest.getTrackerSimcard()),
                newLoanRequest.getLoanBranch()
        );

        smsService.sendLoanApplicationConfirmation(loanApplicant.getPhone());

        return "Loan request submitted";
    }

    public boolean updateLoanStatus(String user, LoanApprovalRequest loanApprovalRequest) throws Exception {

        SystemUser systemUser = userService.getByEmailOrPhone(user);


        if (systemUser.getUsertype() != Usertype.Admin) {
            throw new RuntimeException("Operation not permitted for this user");
        }

        int updatedRows =0;

        switch (loanApprovalRequest.getApprovalLevel()) {
            case "ONE" -> {
                if (systemUser.getAprovalLevel() != 1) {
                    throw new RuntimeException("Operation not permitted for this user");
                }

                String sql = """
                        UPDATE loans SET loanStatusLevelOne = ?, l_one_updated_at = ?, l_one_update_comment = ? WHERE loanAccountMPesa = ?
                        """;
                updatedRows = jdbcTemplateOne.update(sql,
                        "APPROVED".equals(loanApprovalRequest.getUpdatedStatus()) ? 1 : 2,
                        LocalDateTime.now(),
                        loanApprovalRequest.getApprovalComment(),
                        loanApprovalRequest.getLoanId());

            }
            case "TWO" -> {
                if (systemUser.getAprovalLevel() != 2 && systemUser.getId() != 2) {
                    throw new RuntimeException("Operation not permitted for this user");
                }

                String sql = """
                        UPDATE loans SET loanStatus = ?, loanStatusLevelTwo = ?, l_two_updated_at = ?, l_two_update_comment = ? WHERE loanAccountMPesa = ?
                        """;

                updatedRows = jdbcTemplateOne.update(sql ,
                        loanApprovalRequest.getUpdatedStatus(),
                        "APPROVED".equals(loanApprovalRequest.getUpdatedStatus()) ? 1 : 2, LocalDateTime.now(),
                        loanApprovalRequest.getApprovalComment(),
                        loanApprovalRequest.getLoanId()
                );

                String message;



                // Send SMS notification based on loan status
                if ("APPROVED".equals(loanApprovalRequest.getUpdatedStatus())) {
                    message = "Dear " + systemUser.getFirstName() + ", Congratulations! Your Loan Has Been APPROVED. Funds Should Disburse To This Number Shortly. Karibu Jiinue";
                } else {
                    message = "Dear " + systemUser.getFirstName() + ", Unfortunately Your Loan Has Been DENIED. Please Contact Us For More Details. Karibu Jiinue";
                }

//                smsService.sendSMS(systemUser.getPhone(), message, 3);
            }
            default -> throw new IllegalArgumentException("Invalid approval level");
        }

        return updatedRows ==1;


    }


    String createLoanId(){
        // Generate loan account number
        char randomAscii1, randomAscii2;
        do {
            randomAscii1 = (char) (ThreadLocalRandom.current().nextInt(65, 91));
        } while ("ILO".indexOf(randomAscii1) != -1);

        do {
            randomAscii2 = (char) (ThreadLocalRandom.current().nextInt(65, 91));
        } while ("ILO".indexOf(randomAscii2) != -1);

        List<Integer> digits = IntStream.rangeClosed(1, 9).boxed().collect(Collectors.toList());
        Collections.shuffle(digits);
        int number = Integer.parseInt(digits.subList(0, 8).stream().map(Object::toString).collect(Collectors.joining()));

        return  "" + randomAscii1 + number + randomAscii2;
    }

    public Object restructureLoan(LoanRestructureData loanRestructureData) throws Exception{

        RestructuredLoanTempData restructuredLoanTempData = getLoanRestructureData(loanRestructureData);

        String sql = """
                 UPDATE loans set loan_term = ?, total_mon_fee = ?, total_interest_amount = ?, client_loan_total = ?, 
                 daily_amount_expected =?  where loanAccountMPesa = ?
                """;

        return jdbcTemplateOne.update(sql,
                restructuredLoanTempData.getLoanTerm(),
                restructuredLoanTempData.getTotalMonthlyMonitoringFee(),
                restructuredLoanTempData.getTotalInterest(),
                restructuredLoanTempData.getClientLoanTotal(),
                restructuredLoanTempData.getDailyExpected(),
                loanRestructureData.getLoanID());

    }


    RestructuredLoanTempData  getLoanRestructureData(LoanRestructureData loanRestructureData){

        double batteryFee =jdbcTemplateOne.queryForObject("SELECT ntsa_fee from loans WHERE loanAccountMPesa=?",(rs,i)->{
            return rs.getDouble("ntsa_fee");
        },loanRestructureData.getLoanID());

        int loanAmount = jdbcTemplateOne.queryForObject("SELECT LOANPRINCIPAL from loans WHERE loanAccountMPesa=?",(rs,i)->{
            return   rs.getInt("LOANPRINCIPAL");
        },loanRestructureData.getLoanID());

        int loanTerm = loanRestructureData.getUpdatedLoanTerm();
        double loanTermMonths = loanTerm / 30.0;
        double ntsaFee = batteryFee;
        double loanPerc = 0.05;
        int monFee = 600;

        // Calculate total interest based on loan term in months
        double totalInterest = loanAmount * loanPerc * loanTermMonths;

        // Calculate total monitoring fee based on loan term in months
        double totalMonFee = monFee * loanTermMonths;

        // Calculate client loan total (before insurance and processing fee)
        double clientLoanTotal = loanAmount + totalMonFee + ntsaFee + totalInterest;


        // Calculate daily expected amount based on total days
        int dailyExpected = (int) Math.ceil(clientLoanTotal / loanTerm);

        RestructuredLoanTempData restructuredLoanTempData = new RestructuredLoanTempData();
        restructuredLoanTempData.setLoanTerm(loanTerm);
        restructuredLoanTempData.setTotalMonthlyMonitoringFee(totalMonFee);
        restructuredLoanTempData.setTotalInterest(totalInterest);
        restructuredLoanTempData.setClientLoanTotal(clientLoanTotal);
        restructuredLoanTempData.setDailyExpected(dailyExpected);
        return restructuredLoanTempData;
    }


    public boolean checkRestructureEligibility(LoanRestructureData loanRestructureData) throws Exception {

        RestructuredLoanTempData restructuredLoanTempData = getLoanRestructureData(loanRestructureData);
        double adjustedLoanTotal = restructuredLoanTempData.getClientLoanTotal();

        double currentLoanBalance = jdbcTemplateOne.queryForObject("SELECT loan_balance from loans WHERE loanAccountMPesa=?",(rs,i)->{
            return rs.getDouble("loan_balance");
        },loanRestructureData.getLoanID());

        double currentPaidAmount = jdbcTemplateOne.queryForObject("SELECT paid_amount from loans WHERE loanAccountMPesa=?",(rs,i)->{
            return rs.getDouble("paid_amount");
        },loanRestructureData.getLoanID());

        double  adjustedBalance = adjustedLoanTotal - currentPaidAmount;

        double loanAge = jdbcTemplateOne.queryForObject("SELECT DATEDIFF(NOW(), disbursed_at) as loanAge from loans WHERE loanAccountMPesa=?",(rs,i)->{
            return rs.getDouble("loanAge");
        },loanRestructureData.getLoanID());

       return adjustedBalance < 1 && loanAge <= loanRestructureData.getUpdatedLoanTerm();
    }
}
