package com.otblabs.jiinueboda.crons;

import com.otblabs.jiinueboda.investors.InvestmentManagementService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class InvestorsCronJobs {

    private final InvestmentManagementService investmentManagementService;

    public InvestorsCronJobs(InvestmentManagementService investmentManagementService) {
        this.investmentManagementService = investmentManagementService;
    }

    @Scheduled(cron = "0 0 0 11 * ?")
    void sendSogomoMonthlyInterest(){
        investmentManagementService.sendPeriodicInterestToInvestor("542542","03405413296150");
    }

    @Scheduled(cron = "0 0 0 15 * ?")
    void sendClifMonthlyInterest(){
        investmentManagementService.sendPeriodicInterestToInvestor("522522","1298739764");
    }


}
