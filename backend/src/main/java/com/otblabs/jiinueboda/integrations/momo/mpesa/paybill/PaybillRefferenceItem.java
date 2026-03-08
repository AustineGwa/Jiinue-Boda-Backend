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
public class PaybillRefferenceItem {
    @JsonProperty("Key")
    private String key;

    @JsonProperty("Value")
    private String value;
}
