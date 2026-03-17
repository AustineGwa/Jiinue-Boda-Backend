package com.otblabs.jiinueboda.loans.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoanStatementProfileInfo {
    private String firstName;
    private String lastName;
    private String phone;
    private String nationalId;
    private String account;
    private double loanPrincipal;
    private double interestPercentage;
    private double totalInterestAmount;
    private double totalMonFee;
    private double ntsaFee;
    private double creditLifeInsurance;
    private double loanProcessingFee;
    private int loanTerm;
    private double totalLoanDisburse;
    private double clientLoanTotal;
    private String recieverPublicName;
    private String transactionCompletedTime;
    private String b2cTransId;
    private int b2cTransAmount;
}
