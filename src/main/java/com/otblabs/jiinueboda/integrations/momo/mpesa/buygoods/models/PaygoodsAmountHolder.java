package com.otblabs.jiinueboda.integrations.momo.mpesa.buygoods.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PaygoodsAmountHolder {
    @JsonProperty("Amount")
    Amount amount;

}
