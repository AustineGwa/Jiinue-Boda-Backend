package com.otblabs.jiinueboda.jiinue.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileLoanData {
    private String account;
    private double loanPrincipal;
    private double interestPercentage;
    private double clientLoanTotal;
    private String loanPurpose;
    private int loanTerm;
    private double dailyAmountExpected;
    private String loanStatus;
    private String loanStatusLevelOne;
    private String loanStatusLevelTwo;
    private String createdAt;
    private String disbursedAt;
    private int loanAge;
    private String loanAgreementUrl;
}
