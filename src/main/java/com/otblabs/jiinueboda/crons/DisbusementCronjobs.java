package com.otblabs.jiinueboda.crons;

import com.otblabs.jiinueboda.integrations.momo.mpesa.b2c.models.B2CRequestResponse;
import com.otblabs.jiinueboda.investors.InvestmentManagementService;
import com.otblabs.jiinueboda.jiinue.LendingService;
import com.otblabs.jiinueboda.jiinue.models.Loan;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DisbusementCronjobs {

    private final LendingService lendingService;
    private final InvestmentManagementService investmentManagementService;


    public DisbusementCronjobs(LendingService lendingService, InvestmentManagementService investmentManagementService) {
        this.lendingService = lendingService;
        this.investmentManagementService = investmentManagementService;
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

    @Scheduled(fixedRate = 60 * 60 * 1000) //every 1 hour
    public void checkFailedInvestorRepaymentsEntry(){
        try {
            investmentManagementService.checkFailedInvestorRepaymentsEntry();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
