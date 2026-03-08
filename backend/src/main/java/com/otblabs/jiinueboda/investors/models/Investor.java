package com.otblabs.jiinueboda.investors.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Investor {
    private String firstName;
    private String lastName;
    private String primaryPhone;
    private String email;
    private String userName;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private  String password;
}
