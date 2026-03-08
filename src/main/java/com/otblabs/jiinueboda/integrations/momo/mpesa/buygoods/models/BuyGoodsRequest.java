package com.otblabs.jiinueboda.integrations.momo.mpesa.buygoods.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.otblabs.jiinueboda.integrations.momo.mpesa.b2c.models.MpesaCommandId;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuyGoodsRequest {

    @JsonProperty("Initiator")
    private String initiator;

    @JsonProperty("SecurityCredential")
    private String securityCredential;

    @JsonProperty("Command ID")
    private MpesaCommandId commandID;

    @JsonProperty("Amount")
    private int amount;

    @JsonProperty("PartyB")
    private String partyB;  //The shortcode to which money will be moved

    @JsonProperty("AccountReference")
    private String accountReference;

    @JsonProperty("Requester")
    private String requester;

    @JsonProperty("Remarks")
    private String remarks;


}
