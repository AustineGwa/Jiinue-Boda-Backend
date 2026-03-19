package com.otblabs.jiinueboda.crons;

import com.otblabs.jiinueboda.collections.CollectionsService;
import com.otblabs.jiinueboda.mail.backups.EmailBackupsService;
import com.otblabs.jiinueboda.collections.recovery.BikeRecoveryService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LoanBookCronJobs {


    private final CollectionsService collectionsService;


    private final BikeRecoveryService bikeRecoveryService;

    private final EmailBackupsService emailBackupsService;

    public LoanBookCronJobs(CollectionsService collectionsService, BikeRecoveryService bikeRecoveryService, EmailBackupsService emailBackupsService) {
        this.collectionsService = collectionsService;
        this.bikeRecoveryService = bikeRecoveryService;
        this.emailBackupsService = emailBackupsService;
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