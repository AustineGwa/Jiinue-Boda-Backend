package com.otblabs.jiinueboda.integrations.momo.mpesa.buygoods.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuygoodsResultSuccess {
    @JsonProperty("ResultType")
    private String resultType;

    @JsonProperty("ResultCode")
    private String resultCode;

    @JsonProperty("ResultDesc")
    private String resultDesc;

    @JsonProperty("OriginatorConversationID")
    private String originatorConversationID;

    @JsonProperty("ConversationID")
    private String conversationID;

    @JsonProperty("TransactionID")
    private String transactionID;

    @JsonProperty("ResultParameters")
    private BuygoodsResultParameters resultParameters;

    @JsonProperty("ReferenceData")
    private BuygoodsReferenceData referenceData;

}
