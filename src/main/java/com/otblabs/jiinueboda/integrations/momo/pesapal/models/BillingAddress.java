package com.otblabs.jiinueboda.integrations.momo.pesapal.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BillingAddress {
    private String emailAddress;
    private String phoneNumber;
    private String countryCode;
    private String firstName;
    private String middleName;
    private String lastName;
    private String line1;
    private String line2;
    private String city;
    private String state;
    private String postalCode;
    private String zipCode;
}
