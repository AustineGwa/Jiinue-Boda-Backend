package com.otblabs.jiinueboda.integrations.momo.mpesa.b2c.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class B2CRequest {
        private MpesaCommandId commandID;
        private String amount;
        private String partyA;
        private String partyB;
        private String remarks;
        private String occasion;
        private int appId;
}
