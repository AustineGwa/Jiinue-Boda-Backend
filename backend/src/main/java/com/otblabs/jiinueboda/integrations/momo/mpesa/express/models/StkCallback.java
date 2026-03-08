package com.otblabs.jiinueboda.integrations.momo.mpesa.express.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class StkCallback {
    @JsonProperty("MerchantRequestID")
    private String merchantRequestID;
    @JsonProperty("CheckoutRequestID")
    private String checkoutRequestID;
    @JsonProperty("ResultCode")
    private String resultCode;
    @JsonProperty("ResultDesc")
    private String resultDesc;
    @JsonProperty("CallbackMetadata")
    private CallbackMetadata callbackMetadata;
}
