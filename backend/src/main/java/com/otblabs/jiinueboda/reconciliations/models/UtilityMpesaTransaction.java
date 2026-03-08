package com.otblabs.jiinueboda.reconciliations.models;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UtilityMpesaTransaction {
    private String receiptNo;
    private LocalDateTime completionTime;
    private LocalDateTime initiationTime;
    private String details;
    private String transactionStatus;
    private BigDecimal paidIn;
    private BigDecimal withdrawn;
    private BigDecimal balance;
    private Boolean balanceConfirmed;
    private String reasonType;
    private String otherPartyInfo;
    private String linkedTransactionId;
    private String accountNo;
}
