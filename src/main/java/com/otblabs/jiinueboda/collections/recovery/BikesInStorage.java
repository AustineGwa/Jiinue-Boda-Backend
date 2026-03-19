package com.otblabs.jiinueboda.collections.recovery;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BikesInStorage {
    private int recoveryId;
    private String bikeRecoveryDate;
    private String firstName;
    private String middleName;
    private String lastName;
    private String phone;
    private int group;
    private int branch;
    private String loanAccount;
    private String asset;
    private int totalLoan;
    private int variance;
    private int loanAge;
    private int recoveryAmount;
}
