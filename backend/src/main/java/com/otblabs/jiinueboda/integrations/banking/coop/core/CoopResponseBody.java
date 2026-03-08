package com.otblabs.jiinueboda.integrations.banking.coop.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CoopResponseBody {
    @JsonProperty("MessageCode")
    private String messageCode;
    @JsonProperty("Message")
    private String message;
}
