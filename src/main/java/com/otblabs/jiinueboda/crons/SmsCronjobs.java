package com.otblabs.jiinueboda.crons;

import com.otblabs.jiinueboda.collections.CollectionsService;
import com.otblabs.jiinueboda.collections.models.LoansByAge;
import com.otblabs.jiinueboda.sms.SmsService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SmsCronjobs {

    private final SmsService smsService;
    private final CollectionsService collectionsService;

    public SmsCronjobs(SmsService smsService, CollectionsService collectionsService) {
        this.smsService = smsService;
        this.collectionsService = collectionsService;
    }

//    @Scheduled(cron = "0 0 20 * * *") //every 8 pm
//    public void sendDailyPaymentReminder(){
//        List<LoansByAge> pendingLoanUserDetailList = collectionsService.getLoansByVariance(0);
//        smsService.sendDailyReminder(pendingLoanUserDetailList);
//    }

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
}
