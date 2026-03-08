package com.otblabs.jiinueboda.jiinue.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PendingDisbursement {
    private String firstName;
    private String lastName;
    private int groupId;
    private String account;
    private int userId;
    private double loanPrincipal;
    private double disburseAmount;
    private double totalLoan;
    private double dailyAmountExpected;
    private int loanTerm;
    private String loanPurpose;
    private String loanStatus;
    private String loanStatusLevelOne;
    private String loanStatusLevelTwo;
    private String applicationDate;
    private String lOneUpdateComment;
    private String lTwoUpdateComment;
    private String branch;
}



