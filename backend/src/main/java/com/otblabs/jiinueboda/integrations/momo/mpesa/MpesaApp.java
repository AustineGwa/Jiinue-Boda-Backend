package com.otblabs.jiinueboda.integrations.momo.mpesa;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MpesaApp {
    private int id;
    private String appName;
    private String consumerKey;
    private String consumerSecret;
    private String b2cConsumerKey;
    private String b2cConsumerSecret;
    private String apiKey;
    private String shotCode;
    private String productsActivated;
    private String responseType;
    private String transactionType;
    private String confirmationURL;
    private String validationURL;
    private String c2bConfirmationURL;
    private String c2bValidationURL;
    private boolean isB2cEnabled;
    private String  b2cBusinessShortcode;
    private String  b2cInitiator;
    private String  b2cPassword;
    private String b2cCallbackUrl;
    private String b2cQueueTimeOutUrl;
    private String buygoodsCallbackUrl;
    private String buygoodsTimeoutUrl;
    private String paybillCallBackUrl;
    private String paybillTimeoutUrl;
    private String pullUrl;
    private String nominatedNumber;
}
