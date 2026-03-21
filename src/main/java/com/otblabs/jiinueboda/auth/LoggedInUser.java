package com.otblabs.jiinueboda.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.otblabs.jiinueboda.users.models.Usertype;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoggedInUser {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private Usertype usertype;
    private int aprovalLevel;
}
