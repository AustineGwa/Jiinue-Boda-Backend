package com.otblabs.jiinueboda.integrations.banking.IandM.models;

import lombok.Data;

@Data
public class MpesaBankResponseData {
    private int resultCode;
    private String resultDesc;
    private String erpRefId;
}
