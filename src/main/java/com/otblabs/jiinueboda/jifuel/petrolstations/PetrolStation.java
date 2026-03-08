package com.otblabs.jiinueboda.jifuel.petrolstations;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PetrolStation {
    int id;
    String name;
    PaymentMode paymentMode;
    double latitude;
    double longitude;
    String playbillNumber;
    String accountNumber;
    String tillNumber;
}
