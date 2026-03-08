package com.otblabs.jiinueboda.integrations.momo.mpesa.b2c.models;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class B2cResultParameter {
    @JsonProperty("Key")
    private String key;

    @JsonProperty("Value")
    private String value;
}