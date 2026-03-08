package com.otblabs.jiinueboda.integrations.momo.mpesa.paybill;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaybillResult {
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
    private PaybillResultParameters resultParameters;

    @JsonProperty("ReferenceData")
    private PaybillReferenceData referenceData;
}
