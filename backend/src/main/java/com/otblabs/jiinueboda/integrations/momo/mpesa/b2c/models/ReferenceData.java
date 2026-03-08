package com.otblabs.jiinueboda.integrations.momo.mpesa.b2c.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReferenceData {
    @JsonProperty("ReferenceItem")
    private ReferenceItem referenceItem;

}
