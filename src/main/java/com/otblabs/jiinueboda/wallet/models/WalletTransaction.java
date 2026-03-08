package com.otblabs.jiinueboda.wallet.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class WalletTransaction {
    private int userId;
    private String transactionChannel;
    private String transactionReference;
    private String walletAccountNumber;
    private String transactionAmount;
    private boolean isVerified;
    private int verifiedBy;
    private String verifiedAt;
    private String createdAt;
    private String updatedAt;
    private String deletedAt;
}
