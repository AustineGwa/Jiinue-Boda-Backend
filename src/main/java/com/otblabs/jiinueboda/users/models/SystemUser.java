package com.otblabs.jiinueboda.users.models;


import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SystemUser {
    private int id;
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String dob;
    private String refferedBy;
    private String refferalId;
    private String phone;
    private String nationalID;
    private String createdAt;
    private int createdBy;
    private int groupId;
    private int appId;
    private int partnerId;
    private boolean onlineRider;

}
