package com.otblabs.jiinueboda.integrations.momo.mpesa.b2c.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class B2cResultNotification {
    @JsonProperty("Result")
    private Result result;
}
