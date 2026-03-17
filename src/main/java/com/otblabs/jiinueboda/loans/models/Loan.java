package com.otblabs.jiinueboda.loans.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Loan {
    private int loanID;
    private int userID;
    private int appID;
    private String loanAccountMpesa;
    private int loanPrincipal;
    private int totalLoanDisburse;
    private double interestPercentage;
    private String loanPurpose;
    private String guarantorId1;
    private String guarantorId2;
    private double amountDueToday;
    private String paymentDate;
    private int daysOverdue;
    private LoanStatus loanStatus;
    private boolean isBadLoan;
    private String createdAt;
    private String updatedAt;
}
