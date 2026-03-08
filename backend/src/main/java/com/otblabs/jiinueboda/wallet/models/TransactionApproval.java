package com.otblabs.jiinueboda.wallet.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TransactionApproval {
    String transactionType;
    boolean isApproved;
    int requestId;
    int userId;
    String accountNumber;
    double amount;
    int approvedBy;
    String approvedAt;
    String is_completed;
    String completedAt;
}
