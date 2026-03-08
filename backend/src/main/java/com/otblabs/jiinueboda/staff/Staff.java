package com.otblabs.jiinueboda.staff;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Staff {
    private int id;
    private  String firstName;
    private  String middleName;
    private  String lastName;
    private  String email;
    private  String phone;
    private  String nationalId;
    private  String usertype;
    private  int baseSalary;
}
