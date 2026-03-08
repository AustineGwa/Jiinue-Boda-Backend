package com.otblabs.jiinueboda.integrations.momo.mpesa.b2c.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ResultParameters {
    @JsonProperty("ResultParameter")
    private List<B2cResultParameter> b2cResultParameter;
}
