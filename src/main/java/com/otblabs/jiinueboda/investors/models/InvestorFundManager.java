package com.otblabs.jiinueboda.investors.models;

import lombok.Data;

@Data
public class InvestorFundManager {
    private int investorId;
    private double totalInvested;
    private double totalInvestmentUsed;
    private double availableFunds;
}
