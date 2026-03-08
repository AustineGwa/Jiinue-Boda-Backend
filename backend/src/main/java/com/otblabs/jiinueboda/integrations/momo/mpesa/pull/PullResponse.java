package com.otblabs.jiinueboda.integrations.momo.mpesa.pull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class PullResponse {
    @JsonProperty("ResponseRefID")
    String  responseRefID;
    @JsonProperty("ResponseCode")
    String  responseCode;
    @JsonProperty("ResponseMessage")
    String  responseMessage;
    @JsonProperty("Response")
    List<List<MResponse>> response;
}
