package com.otblabs.jiinueboda.collections.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ReconciliationData {
    private String transactionId;
    private String wrongLoanId;
    private String correctLoanId;
    private int amount;
}
