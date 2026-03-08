package com.fintech.banking.banking.ncba.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties
public class Body {
    @JacksonXmlProperty(localName = "NCBAPaymentNotificationRequest", namespace = "http://mega.com/ncba")
    private NCBAPaymentNotificationRequest request;

    public NCBAPaymentNotificationRequest getRequest() {
        return request;
    }

    public void setRequest(NCBAPaymentNotificationRequest request) {
        this.request = request;
    }
}
