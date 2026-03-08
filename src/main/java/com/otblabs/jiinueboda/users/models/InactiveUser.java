package com.otblabs.jiinueboda.users.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class InactiveUser {
    private int userId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String phone;
    private int group;
    private int branch;
    private String userSince;
    private String numberPlate;
}
