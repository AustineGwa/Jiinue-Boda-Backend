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
public class Item {
    @JsonProperty("Name")
    String name;
    @JsonProperty("Value")
    String value;
}
