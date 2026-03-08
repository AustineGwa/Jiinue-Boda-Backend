package com.otblabs.jiinueboda.dashboard.metrics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.YearMonth;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyMetrics {
    private YearMonth month;

    // Loan Portfolio Performance
    private int numberOfLoansDisbursed;
    private double totalPrincipalDisbursed;
    private double loanGrowthRateMoM; // Compared to previous month
    private double averageLoanSizePerClient;

    // Client Engagement & Retention
    private int numberOfActiveClients;
    private int totalUniqueClients;
    private int repeatLoanCount;
    private double clientRetentionRate;
    private int droppedOffClients;
}
