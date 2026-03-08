package com.otblabs.jiinueboda.collections.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserTransaction {
    private String name;
    private String transactionId;
    private String loanId;
    private String time;
    private int amount;
}
