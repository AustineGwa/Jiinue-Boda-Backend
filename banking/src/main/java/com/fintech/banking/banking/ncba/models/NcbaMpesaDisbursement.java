package com.fintech.banking.banking.ncba.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NcbaMpesaDisbursement {
    private String bankCode;               // BANK CODE (e.g., for all Mwallets is 99)
    private String branchCode;             // BRANCH CODE (e.g., for all Mwallets is 002)
    private String beneficiaryAccountName; // NAME OF THE BENEFICIARY
    private String country;                // COUNTRY YOU ARE PAYING INTO (CASE SENSITIVE) – Kenya, Uganda, Tanzania
    private String tranType;               // TRANSACTION TYPE (Mpesa/HalotelTz, AirtelTz, ZantelTz, TigoTz, VodacomTz)
    private String reference;              // TRANSACTION UNIQUE IDENTIFIER
    private String currency;               // CURRENCY (KES for Kenya, TZS for Tanzania)
    private String account;                // ACCOUNT (Mobile number in the format of 254XXX XX XX XX)
    private double amount;                 // AMOUNT
    private String narration;              // NARRATION (fund transfer)
    private String validationId;           // VALIDATION CODE provided from the Validation Request

}
