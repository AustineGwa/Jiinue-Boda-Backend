package com.otblabs.jiinueboda.staff;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NewStaffRequest {
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String gender;
    private String phone;
    private String nationalID;
    private int createdBy;
    private String role;

}
