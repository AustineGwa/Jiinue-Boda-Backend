package com.otblabs.jiinueboda.dashboard.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyPerformance {
    private String account;
    private String firstName;
    private String middleName;
    private String lastName;
    private String phone;
    private int groupId;
    private int partnerId;
    private int principal;
    private int loanTerm;
    private int loanAge;
    private int dailyAmountExpected;
    private int expectedAmount;
    private int amountPaid;
    private int balance;
    private double repaymentRate;
    private String loanPurpose;
    private String disbursedAt;
}
