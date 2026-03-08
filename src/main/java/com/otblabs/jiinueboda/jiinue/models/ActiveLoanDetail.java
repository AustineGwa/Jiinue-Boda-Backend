package com.otblabs.jiinueboda.jiinue.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ActiveLoanDetail {
        private int id;
        private String firstName;
        private String lastName;
        private String phone;
        private String account;
        private double principal;
        private double interest;
        private double interestAmount;
        private double processingFee;
        private double batteryFee;
        private double batteryFeeExpected;
        private double batteryFeePaid;
        private double batteryFeeVariance;
        private double insurance;
        private double monitoringFee;
        private double disbursed;
        private double totalLoan;
        private int term;
        private int loanAge;
        private double loanBalance;
        private double interestEarned;
        private double dailyInterest;
        private double interestExpected;
        private double interestBalance;
        private String disburseDate;
        private String lastPaymentDate;
        private double dslp;
        private String status;
        private double totalPaid;
        private double totalExpected;
        private double variance;
        private double varRatio;
        private double dailyExpected;
        private int daysPaid;
        private double monitoringFeePaid;
        private double monitoringFeeExpected;
        private double monitoringFeeBalance;

   }
