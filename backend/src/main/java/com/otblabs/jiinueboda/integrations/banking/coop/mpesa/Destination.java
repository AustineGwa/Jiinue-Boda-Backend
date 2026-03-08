package com.otblabs.jiinueboda.integrations.banking.coop.mpesa;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Destination {
    @JsonProperty("ReferenceNumber")
    private String refferenceNumber;
    @JsonProperty("MobileNumber")
    private String mobileNumber;
    @JsonProperty("Amount")
    private int amount;
    @JsonProperty("Narration")
    private String narration;
}