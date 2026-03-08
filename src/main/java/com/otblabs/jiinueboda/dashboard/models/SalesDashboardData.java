package com.otblabs.jiinueboda.dashboard.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SalesDashboardData {
    private int totalLoans;
    private int totalNewLoans;
    private int totalPrincipal;
    private int totalNewStagesMapped;
    private int completedLoans;
    private int totalLeadsCreated;
    private int totalUsersServed;
    private int totalActiveUsers;
    private int allLoansDisbursed;
}
