package com.fintech.banking.banking.ncba.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PaybillNotification {
    @JsonProperty("TransType")
    private String transType;
    @JsonProperty("TransID")
    private String transID;
    @JsonProperty("TransTime")
    private String transTime;
    @JsonProperty("TransAmount")
    private int transAmount;
    @JsonProperty("BusinessShortCode")
    private String businessShortCode;
    @JsonProperty("BillRefNumber")
    private String billRefNumber;
    @JsonProperty("Mobile")
    private String mobile;
    @JsonProperty("Name")
    private String name;
    @JsonProperty("created_at")
    private String created_at;
    @JsonProperty("Username")
    private String userName;
    @JsonProperty("Password")
    private String password;
    @JsonProperty("Hash")
    private String hash;

}
