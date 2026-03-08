package com.otblabs.jiinueboda.dashboard.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MainDashboard {
    private int activeLoans;
    private int loansThisMonth;
    private int totalPrincipal;
    private int cashCollected;
    private int cashExpected;
    private int overdueLoans;
    private int variance;
    private int periodVariance;
    private int totalLoanCountExpected;
    private int totalLoanCountExpectedThisMonth;
    private int newLoansCountExpectedThisMonth;
    private int principalExpected;
}


