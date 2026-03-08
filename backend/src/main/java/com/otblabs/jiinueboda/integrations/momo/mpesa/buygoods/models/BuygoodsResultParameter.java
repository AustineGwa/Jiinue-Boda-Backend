package com.otblabs.jiinueboda.integrations.momo.mpesa.buygoods.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BuygoodsResultParameter {
    @JsonProperty("Key")
    private String key;

    @JsonProperty("Value")
    private String value;
}
