package com.otblabs.jiinueboda.integrations.banking.IandM.models.collections;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerValidationRequest {
    @JsonProperty("customerid")
    String customerId;
    @JsonProperty("apiusername")
    String apiUsername;
    @JsonProperty("apipassword")
    String apiPassword;
}
