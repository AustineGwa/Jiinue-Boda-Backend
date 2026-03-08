package com.otblabs.jiinueboda.collections.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class CollectionPerformance {
    private int userId;
    private String loanAccount;
    private int principal;
    private int clientLoanTotal;
    private int loanTerm;
    private double dailyAmountExpected;
    private LocalDate disbursedAt;
    private int loanAge;
    private double expectedAmount;
    private double receivedAmount;
}

