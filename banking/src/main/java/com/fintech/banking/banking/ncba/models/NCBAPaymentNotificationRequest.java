package com.fintech.banking.banking.ncba.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
@JsonIgnoreProperties
public class NCBAPaymentNotificationRequest {
    @JsonProperty("User")
    private String user;

    @JsonProperty("Password")
    private String password;

    @JsonProperty("HashVal")
    private String hashVal;

    @JsonProperty("TransType")
    private String transType;

    @JsonProperty("TransID")
    private String transID;

    @JsonProperty("TransTime")
    private String transTime;

    @JsonProperty("TransAmount")
    private String transAmount;

    @JsonProperty("AccountNr")
    private String accountNr;

    @JsonProperty("Narrative")
    private String narrative;

    @JsonProperty("PhoneNr")
    private String phoneNr;

    @JsonProperty("CustomerName")
    private String customerName;

    @JsonProperty("Status")
    private String status;


    public String getNarrative() {
        return narrative;
    }

    public void setNarrative(String narrative) {
        this.narrative = narrative;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String value) {
        this.user = value;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String value) {
        this.password = value;
    }

    public String getHashVal() {
        return hashVal;
    }

    public void setHashVal(String value) {
        this.hashVal = value;
    }

    public String getTransType() {
        return transType;
    }

    public void setTransType(String value) {
        this.transType = value;
    }

    public String getTransID() {
        return transID;
    }

    public void setTransID(String value) {
        this.transID = value;
    }

    public String getTransTime() {
        return transTime;
    }

    public void setTransTime(String value) {
        this.transTime = value;
    }

    public String getTransAmount() {
        return transAmount;
    }

    public void setTransAmount(String value) {
        this.transAmount = value;
    }

    public String getAccountNr() {
        return accountNr;
    }

    public void setAccountNr(String value) {
        this.accountNr = value;
    }

    public String getPhoneNr() {
        return phoneNr;
    }

    public void setPhoneNr(String value) {
        this.phoneNr = value;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String value) {
        this.customerName = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        this.status = value;
    }

}
