package com.otblabs.jiinueboda.reconciliations.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MpesaTransaction {
    private String firstName;
    private String transactionId;
    private String transactionTime;
    private String transactionAmount;
    private String loanAccount;
}
