package com.otblabs.jiinueboda.collections.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PendingCollection {
    private String firstName;
    private String phoneNumber;
    private String loanID;
    private String transID;
    private String transTime;
    private String transactionAmount;
}
