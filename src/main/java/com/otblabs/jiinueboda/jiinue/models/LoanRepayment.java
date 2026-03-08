package com.otblabs.jiinueboda.jiinue.models;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LoanRepayment {
    int userId;
    int loanId;
    int amountPaid;
    int transactionCode;
    String paymentDate;
}
