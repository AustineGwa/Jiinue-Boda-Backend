package com.otblabs.jiinueboda.crons;

import com.otblabs.jiinueboda.investors.InvestmentManagementService;
import org.springframework.stereotype.Component;

@Component
public class InvestorsCronJobs {

    private final InvestmentManagementService investmentManagementService;

    public InvestorsCronJobs(InvestmentManagementService investmentManagementService) {
        this.investmentManagementService = investmentManagementService;
    }

    void sendSogomoMonthlyInterest(){
        investmentManagementService.sendPeriodicInterestToInvestor("542542","03405413296150");
    }


}
