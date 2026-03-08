package com.otblabs.jiinueboda.jifuel.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DashboardData {
    private int loanCountToday;
    private int loanCountTodate;
    private int totalAmountPurchasedToday;
    private int totalAmountPurchasedTodate;
    private int totalRepaymentsCountToday;
    private int totalRepaymentsCountTodate;
    private int totalRepaymentsAmountToday;
    private int totalRepaymentsAmountTodate;
    private int jifuelAvailableUsageBalance;
}
