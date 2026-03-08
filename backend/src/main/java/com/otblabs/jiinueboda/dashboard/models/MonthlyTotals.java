package com.otblabs.jiinueboda.dashboard.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyTotals {
    private String monthName;
    private int monthNumber;
    private int totalLoans;
    private int totalPayments;
}
