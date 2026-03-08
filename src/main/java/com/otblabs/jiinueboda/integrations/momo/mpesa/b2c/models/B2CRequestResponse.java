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
public class B2CRequestResponse {
    @JsonProperty("OriginatorConversationID")
    private String originatorConversationID;
    @JsonProperty("ConversationID")
    private String conversationID;
    @JsonProperty("ResponseCode")
    private String responseCode;
    @JsonProperty("ResponseDescription")
    private String responseDescription;
}
