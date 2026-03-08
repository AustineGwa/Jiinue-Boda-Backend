package com.otblabs.jiinueboda.users.profile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileData {
    private int userId;
    private String fullName;
    private String profilePicture;
    private String email;
    private String phone;
    private String nationalId;
    private boolean status;
    private int partnerId;
    private int partnerApproved;
}
