package com.otblabs.jiinueboda.jiinue.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserLoanDetail {
    private String firstName;
    private String lastName;
    private String phone;
    private int loanId;
    private double loanPrincipal;
    private double interestPecentage;
    private double totalLoan;
    private double totalPaid;
    private double balance;
    private int clientLoanTotal;
    private int shotcode;

}
