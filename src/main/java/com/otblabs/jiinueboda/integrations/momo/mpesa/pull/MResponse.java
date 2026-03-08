package com.otblabs.jiinueboda.integrations.momo.mpesa.pull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MResponse{
    String  transactionId;
    String  trxDate;
    String  msisdn;
    String  sender;
    String  transactiontype;
    String  billreference;
    String  amount;
    String  organizationname;
}
