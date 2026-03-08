package com.otblabs.jiinueboda.wallet.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserTransaction {
    int userId;
    String accountNumber;
    double amount;

}
