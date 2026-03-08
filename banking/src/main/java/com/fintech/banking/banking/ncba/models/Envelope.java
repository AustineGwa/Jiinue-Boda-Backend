package com.fintech.banking.banking.ncba.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JsonIgnoreProperties
@JacksonXmlRootElement(localName = "Envelope", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
public class Envelope {

    @JacksonXmlProperty(localName = "Header", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
    private Header header;
    @JacksonXmlProperty(localName = "Body", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
    private Body body;

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }
}