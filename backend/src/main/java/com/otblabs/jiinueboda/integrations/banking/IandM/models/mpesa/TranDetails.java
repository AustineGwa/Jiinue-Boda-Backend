package com.otblabs.jiinueboda.integrations.banking.IandM.models.mpesa;

import lombok.Data;

@Data
public class TranDetails {
    private String transAmount;
    private String tranCCY;
    private String narration;
    private String eventID;
}
