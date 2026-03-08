package com.otblabs.jiinueboda.collections.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoanCollectionsData {
    private String loanAccountNumber;
    private LocalDate dueDate;
    private double dailyAmountExpected;
    private double cumulativeAmountExpected;
    private double amountPaidThatDay;
    private double totalPaidToDate;
    private double cumulativeArrears;
}
