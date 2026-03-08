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
public class PaybillApiResponse {

    @JsonProperty("Result")
    private PaybillResult paybillResult;
}
