package com.otblabs.jiinueboda.integrations.banking.IandM.models.mpesa;

import lombok.Data;

@Data
public class MobileMoneyPayment {
    private String commandID;
    private String receiverPartyIdentifierType;
    private String receiverPartyIdentifier;
}
