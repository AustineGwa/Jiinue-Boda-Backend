package com.otblabs.jiinueboda.auth;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LoginTrail {
    private int userId;
    private String loginTime;
    private String logoutTime;
    private String loginIpAddress;
    private String bearerToken;
    private String userArgent;
    private String isLongLivedSession;

}
