package com.otblabs.jiinueboda.dashboard.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DashboardData {
    private int totalPrincipal;
    private int tLoans;
    private int tLoansPaid;
    private int collectionExpected;
    private int collectionVariance;
    private double par;
    private int totalLoanCount;
    private int newUsersThisMonth;
    private int collectionExpectedToday;
    private int outstandingBalance;
    private int loansThisMonth;
}
