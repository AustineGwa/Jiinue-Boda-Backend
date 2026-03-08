package com.otblabs.jiinueboda.integrations.momo.mpesa.express.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class CallbackMetadata {
    @JsonProperty("Item")
    List<Item> item;
}
