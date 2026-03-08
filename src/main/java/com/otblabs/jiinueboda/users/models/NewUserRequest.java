package com.otblabs.jiinueboda.users.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NewUserRequest {
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String gender;
    private String dob;
    private String phone;
    private String nationalID;
    private int partnerId;
    private int createdBy;
    private int groupId;
    private String refferedBy;
    private String alternativeNumber;
    private String joiningChanel;
}
