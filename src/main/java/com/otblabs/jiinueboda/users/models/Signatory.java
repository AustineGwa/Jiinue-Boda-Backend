package com.otblabs.jiinueboda.users.models;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Signatory {
    int userId;
    int appId;
    String notificationNumber;
    String notificationEmail;
    int level;

}
