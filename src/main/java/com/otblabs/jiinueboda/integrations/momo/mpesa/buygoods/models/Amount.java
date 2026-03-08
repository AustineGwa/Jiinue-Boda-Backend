package com.otblabs.jiinueboda.integrations.momo.mpesa.buygoods.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Amount {
    @JsonProperty("CurrencyCode")
    private String currencyCode;
    @JsonProperty("MinimumAmount")
    private String minimumAmount;
    @JsonProperty("BasicAmount")
    private String basicAmount;
}
