package com.otblabs.jiinueboda.jiinue.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LoanRequest {
    private int userID;
    private int loanPrincipal;
    private String loanPurpose;
    private int guarantorId1;
    private int guarantorId2;
    private String paymentDate;
}
