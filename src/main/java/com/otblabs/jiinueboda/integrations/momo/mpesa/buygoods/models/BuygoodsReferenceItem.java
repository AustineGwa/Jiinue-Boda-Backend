package com.otblabs.jiinueboda.integrations.momo.mpesa.buygoods.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuygoodsReferenceItem {
    @JsonProperty("Key")
    private String key;

    @JsonProperty("Value")
    private String value;
}
