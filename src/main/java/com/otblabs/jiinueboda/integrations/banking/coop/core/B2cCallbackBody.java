package com.otblabs.jiinueboda.integrations.banking.coop.core;
//import com.google.gson.Gson;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class B2cCallbackBody {
    @JsonProperty("MessageReference")
    private String MessageReference;
    @JsonProperty("MessageDateTime")
    private String MessageDateTime;
    @JsonProperty("MessageCode")
    private String MessageCode;
    @JsonProperty("MessageDescription")
    private String MessageDescription;
    @JsonProperty("Source")
    private Source Source;
    @JsonProperty("Destinations")
    private List<Destination> Destinations;

    @Data
    public static class Source {
        @JsonProperty("")
        private String AccountNumber;
        @JsonProperty("")
        private int Amount;
        @JsonProperty("")
        private String TransactionCurrency;
        @JsonProperty("")
        private String Narration;
        @JsonProperty("")
        private String ResponseCode;
        @JsonProperty("")
        private String ResponseDescription;
    }

    @Data
    public static class Destination {
        @JsonProperty("ReferenceNumber")
        private String ReferenceNumber;
        @JsonProperty("PhoneNumber")
        private String PhoneNumber;
        @JsonProperty("Amount")
        private int Amount;
        @JsonProperty("TransactionCurrency")
        private String TransactionCurrency;
        @JsonProperty("Narration")
        private String Narration;
        @JsonProperty("TransactionID")
        private String TransactionID;
        @JsonProperty("ResponseCode")
        private String ResponseCode;
        @JsonProperty("ResponseDescription")
        private String ResponseDescription;
    }
}


