package com.otblabs.jiinueboda.integrations.momo.mpesa.b2c.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Result {
    @JsonProperty("ConversationID")
    private String conversationId;
    @JsonProperty("OriginatorConversationID")
    private String originatorConversationId;
    @JsonProperty("ResultDesc")
    private String resultDesc;
    @JsonProperty("ResultType")
    private String resultType;
    @JsonProperty("ResultCode")
    private String resultCode;
    @JsonProperty("TransactionID")
    private String transactionID;
    @JsonProperty("ResultParameters")
    private ResultParameters resultParameters;
    @JsonProperty("ReferenceData")
    private ReferenceData referenceData;
}
