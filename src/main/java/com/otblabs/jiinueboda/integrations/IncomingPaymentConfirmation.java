package com.otblabs.jiinueboda.integrations;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IncomingPaymentConfirmation {
    private String loanAccount;
    private String transactionId;
    private double transactionAmount;

}
