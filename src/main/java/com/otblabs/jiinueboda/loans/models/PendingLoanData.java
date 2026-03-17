package com.otblabs.jiinueboda.loans.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PendingLoanData {
    private int userId;
    private String account;
    private String firstName;
    private String middleName;
    private String lastName;
    private String phone;
    private boolean onlineRider;
    private int clientAge;
    private int allLoansTaken;
    private int partnerId;
    private int groupId;
    private String stageName;
    private String stageCounty;
    private String stageSubCounty;
    private String stageWard;
    private String bikeBrand;
    private String bikeMake;
    private String bikeModel;
    private String assetPlate;
    private double principal;
    private double totalLoan;
    private int term;
    private int loanAge;
    private double loanBalance;
    private String disburseDate;
    private String lastPaymentDate;
    private int dspl;
    private String status;
    private double totalPaid;
    private double totalExpected;
    private double variance;
    private double varRatio;
    private double dailyExpected;
    private int daysPaid;
}
