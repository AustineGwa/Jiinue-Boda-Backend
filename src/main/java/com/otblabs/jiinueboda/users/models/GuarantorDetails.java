package com.otblabs.jiinueboda.users.models;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GuarantorDetails {
    private String fullName;
    private String phoneNumber;
    private String idNumber;
    private String relationship;
}
