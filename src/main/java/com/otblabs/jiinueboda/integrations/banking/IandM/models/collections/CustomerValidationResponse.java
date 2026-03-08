package com.otblabs.jiinueboda.integrations.banking.IandM.models.collections;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerValidationResponse {
    @JsonProperty("resultCode")
    int resultCode;
    @JsonProperty("resultDesc")
    String resultDesc;
    @JsonProperty("balance")
    int balance;
    @JsonProperty("customertName")
    String customerName;
    @JsonProperty("customerID")
    String customerID;
}
