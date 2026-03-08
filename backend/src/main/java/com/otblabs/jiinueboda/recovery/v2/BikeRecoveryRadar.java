package com.otblabs.jiinueboda.recovery.v2;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BikeRecoveryRadar {
    private int id;
    private String loanId;
    private String createdAt;
    private String creationComment;
    private boolean adminApproval;
    private String adminComment;

}
