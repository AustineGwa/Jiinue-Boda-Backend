package com.otblabs.jiinueboda.integrations.banking.coop.mpesa;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SendToMpesaRequestBody {
    @JsonProperty("MessageReference")
    private String messageRefference;
    @JsonProperty("CallBackUrl")
    private String callbackUrl;
    @JsonProperty("Source")
    private Source source;
    @JsonProperty("Destinations")
    private List<Destination> destinations;
}
