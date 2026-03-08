package com.otblabs.jiinueboda.users.models;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserProfile {
    private int id;
    private int userId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String referralId;
    private String referredBy;
    private String phone;
    private String password;
    private String nationalId;
    private String userStatus;
    private String createdBy;
    private String groupId;
    private String appId;
    private boolean hasUpdatedProfile;
    private boolean hasUpdatedKyc;
    private String stageName;
    private String county;
    private String constituency;
    private String ward;
    private String welfare;
    private String sacco;
    private String referee;
    private String userType;
    private int approvalLevel;
    private String refferee;
    private String createdAt;
    private int partnerId;
    private String nationalIdUrl;
    private String passportPictureUrl;
    private String kraPinUrl;
    private String drivingLicenceUrl;
}



