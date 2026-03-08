package com.otblabs.jiinueboda.recovery.v2;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BikeRecoveryRecord {
    private int id;
    private String firstName;
    private String lastName;
    private String loanAccount;
    private String plate;
    private String date;
    private int recoveryAmount;
    private String transactionId;
    private int arrearsAsAtRecovery;
    private int currentArrears;
    private String loanStatus;
    private String clientStatus;
    private int numberOfLoans;
    private String bikeMake;
    private String bikeModel;
    private int bikecc;
    private int yearOfManufacture;

}

