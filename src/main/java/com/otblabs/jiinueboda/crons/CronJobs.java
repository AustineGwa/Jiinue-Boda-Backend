package com.otblabs.jiinueboda.crons;

import com.otblabs.jiinueboda.collections.CollectionsService;
import com.otblabs.jiinueboda.collections.models.LoansByAge;
import com.otblabs.jiinueboda.mail.backups.EmailBackupsService;
import com.otblabs.jiinueboda.recovery.BikeRecoveryService;
import com.otblabs.jiinueboda.integrations.momo.mpesa.b2c.models.B2CRequestResponse;
import com.otblabs.jiinueboda.investors.InvestmentManagementService;
import com.otblabs.jiinueboda.jifuel.FuelLoanService;
import com.otblabs.jiinueboda.jiinue.LendingService;
import com.otblabs.jiinueboda.jiinue.models.Loan;
import com.otblabs.jiinueboda.sms.SmsService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CronJobs {

    private final LendingService lendingService;
    private final FuelLoanService fuelLoanService;
    private final CollectionsService collectionsService;
    private final SmsService smsService;
    private final InvestmentManagementService investmentManagementService;

    private final BikeRecoveryService bikeRecoveryService;

    private final EmailBackupsService emailBackupsService;

    public CronJobs(LendingService lendingService, FuelLoanService fuelLoanService, CollectionsService collectionsService, SmsService smsService, InvestmentManagementService investmentManagementService, BikeRecoveryService bikeRecoveryService, EmailBackupsService emailBackupsService) {
        this.lendingService = lendingService;
        this.fuelLoanService = fuelLoanService;
        this.collectionsService = collectionsService;
        this.smsService = smsService;
        this.investmentManagementService = investmentManagementService;
        this.bikeRecoveryService = bikeRecoveryService;
        this.emailBackupsService = emailBackupsService;
    }

    @Scheduled(fixedRate = 1000) //every second
    public void disburseLoans(){
        List<Loan> pendingDisbursments = lendingService.getAllPendingDisbursements();
        pendingDisbursments.forEach(loan->{

            try {

                if(investmentManagementService.investorDisburse(loan)){
                    lendingService.setLoanDisbursementInitiated(loan.getLoanAccountMpesa());
                }

                try {
                    B2CRequestResponse b2CRequestResponse = lendingService.disburseLoan(loan);
                }catch (Exception exception){
                    exception.printStackTrace();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        });
    }

//    @Scheduled(fixedRate = 10_000) //every 10 seconds
//    public void disburseFuelLoans(){
//        List<FuelLoan> pendingFuelDisbursments = fuelLoanService.getAllPendingFuelDisbursements();
//        pendingFuelDisbursments.forEach(fuelLoan->{
//            try {
//                fuelLoanService.setLoanDisbursementInitiated(fuelLoan.getLoanID());
//                BuygoodsRequestResponse buygoodsRequestResponse = fuelLoanService.disburseFuelLoan(fuelLoan);
//                fuelLoanService.updateMpesaConversionId(buygoodsRequestResponse,fuelLoan);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
//    }

    @Scheduled(fixedRate = 60 * 60 * 1000) //every 1 hour
    public void checkFailedInvestorRepaymentsEntry(){
        try {
            investmentManagementService.checkFailedInvestorRepaymentsEntry();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Scheduled(cron = "0 0 20 * * *") //every 8 pm
    public void sendDailyPaymentReminder(){
        List<LoansByAge> pendingLoanUserDetailList = collectionsService.getLoansByVariance(0);
        smsService.sendDailyReminder(pendingLoanUserDetailList);
    }

    /*
        The cron expression 0 0 07 * * consists of five fields:
        0 - Seconds (0-59)
        0 - Minutes (0-59)
        07 - Hours (0-23)
        * - Day of the month (1-31)
        * - Month (1-12, or JAN-DEC)
        This expression means that the annotated method should run at 0 seconds, 0 minutes, 07 hours (7 AM), every day of the month, and every month.
     */
    @Scheduled(cron = "0 0 11 * * *") //every 11 am
    public void sendDailyPaymentReminderForAreas(){
        List<LoansByAge> pendingLoanUserDetailList = collectionsService.getLoansByVariance(0);
        smsService.sendDailyReminder(pendingLoanUserDetailList);

    }





    @Scheduled(cron = "0 0 00 * * *")  //every 12 am
    public void updateDailyExpectedCollection(){
        collectionsService.updateDailyExpectedCollection();
    }

    @Scheduled(cron = "0 0 01 * * *")  //every 1 am
    public void updateLoanTotalExpectedPaySum(){
        collectionsService.updateLoanTotalExpectedPaySum();
    }

    @Scheduled(cron = "0 0 01 * * *")  //every 1 am
    public void updateAmountPaidPerClient(){
        collectionsService.updateAmountPaidPerClient();
    }

    @Scheduled(cron = "0 0 01 * * *")  //every 1 am
    public void updateLoanBalances(){
        collectionsService.updateLoanBalances();
    }

    @Scheduled(cron = "0 0 01 * * *")  //every 1 am
    public void updateLastPaymentDate(){
        collectionsService.updateLastPaymentDate();
    }

    @Scheduled(cron = "0 0 01 * * *")  //every 1 am
    public void updateNonPerformingLoans(){
        collectionsService.updateNonPerformingLoans();
    }

    @Scheduled(cron = "0 0 02 * * *")  //every 1 am
    public void updateLoanTotalExpectedPaySumForCompletedLoan(){
        collectionsService.updateLoanTotalExpectedPaySumForCompletedLoan();
    }

    @Scheduled(cron = "0 0 02 * * *")  //every 1 am
    public void updateupdateExpiredExcemption(){
        bikeRecoveryService.updateExpiredExcemption();
    }

    @Scheduled(cron = "0 0 2 * * MON")
    public void updateWeeklyRecovery(){
        bikeRecoveryService.updateWeeklyRecovery();
    }

    @Scheduled(cron = "0 0 00 * * *")  //every 12 am
    public void dailyLoanBookBackup(){
        emailBackupsService.sendDailyLoanBookBackup();
        emailBackupsService.sendDailyExpenses();
        emailBackupsService.sendDailyDisbursements();
    }

}