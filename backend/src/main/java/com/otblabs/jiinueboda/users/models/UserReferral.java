package com.otblabs.jiinueboda.users.models;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserReferral {
    private int id;
    private int refererUserId;
    private String referedUserPhone;
    private String refferedUserName;

}
