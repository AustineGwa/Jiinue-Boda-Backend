package com.otblabs.jiinueboda.collections.recoveryV2;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApprovedRecovery {
    private int id;
    private String firstName;
    private String lastName;
    private String account;
    private String phone;
    private int variance;
    private int varRatio;
    private String disbursedAt;
    private String numberPlate;
    private String requestedOn;
}
