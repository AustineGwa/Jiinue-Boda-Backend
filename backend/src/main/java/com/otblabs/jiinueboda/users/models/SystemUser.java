package com.otblabs.jiinueboda.users.models;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private String nationalID;
    private Usertype usertype;
    private int aprovalLevel;
    private String createdAt;
    private int createdBy;
    private int groupId;
    private int appId;
    private List<String> userRoles;
    private UserProfile userProfile;
    private int partnerId;
    private boolean onlineRider;

}
