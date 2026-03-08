package com.otblabs.jiinueboda.mail.backups;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResult {
    private String commandId;
    private String partyB;
    private String remarks;
    private String occasion;
    private String responseCode;
    private String responseDescription;
    private String conversationId;
    private String originatorConversationId;
    private String resultDesc;
    private String resultType;
    private String resultCode;
    private String transactionId;
    private String transactionReceipt;
    private String resultParameters;
    private int transactionAmount;
    private String receiverName;
    private int b2cWorkingAccountAvailableFunds;
    private int b2cUtilityAccountAvailableFunds;
    private String transactionCompletedDatetime;
    private String receiverPartyPublicName;
    private String createdAt;
}

