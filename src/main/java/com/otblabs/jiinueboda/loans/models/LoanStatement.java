package com.otblabs.jiinueboda.loans.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanStatement {
    private String transactionId;
    private String transactionTime;
    private int transactionAmount;
    private String transactionType;
    private String userName;
}
