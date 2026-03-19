package com.otblabs.jiinueboda.collections.recoveryV2;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BikeRecoveryRadaDAO {
    private int id;
    private String firstName;
    private String lastName;
    private String account;
    private int branch;
    private String phone;
    private int loanTerm;
    private int loanAge;
    private int variance;
    private int varRatio;
    private String disbursedAt;
    private String numberPlate;
    private String creationComment;
    private String requestedOn;
    private boolean adminApproved;
    private String adminComment;
    private String adminCommentOn;
}


