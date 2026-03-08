package com.otblabs.jiinueboda.jiinue.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RestructuredLoanTempData {
    private int loanTerm;
    private double totalMonthlyMonitoringFee;
    private double totalInterest;
    private double clientLoanTotal;
    private int dailyExpected;
}
