package com.otblabs.jiinueboda.users.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UpdateUserDto {
    private int userId;
    private int groupId;
    private String phoneNumber;
    private String idNumber;

}
