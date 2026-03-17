package com.otblabs.jiinueboda.loans.models;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PendingLoanUserDetail {
    private int userId;
    private int appId;
    private String firstName;
    private String lastName;
    private String phone;
    private double loanPlusInterest;
    private double totalPaid;
    private double balance;
    private String lastPaimentDate;
    private String daysSinceLastPayment;
    private String loanID;
    private int clientLoanTotal;
    private int shotcode;
}
