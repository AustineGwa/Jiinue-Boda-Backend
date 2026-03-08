package com.otblabs.jiinueboda.collections.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DefaultList {
    private int id;
    private String firstName;
    private String lastName;
    private String phone;
    private String account;
    private double totalPaid;
    private double totalExpected;
    private double variance;
    private double varRatio;
    private double dailyExpected;
    private int loanBalance;
    private String loanStatus;
    private int loanAge;

}
