package com.otblabs.jiinueboda.integrations.banking.IandM.models;

import lombok.Data;

@Data
public class IAndMConfig {
    private int id;
    private int appId;
    private String cifNumber;
    private String channelId;
    private String senderAccount;
    private String consumerKey;
    private String consumerSecret;
    private String checksumPublicKey;
    private String createdAt;
}
