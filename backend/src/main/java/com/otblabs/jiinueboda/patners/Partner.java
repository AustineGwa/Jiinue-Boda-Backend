package com.otblabs.jiinueboda.patners;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Partner {
    private  int id;
    private  String name;
    private  String organisation;
    private  String contactPhone;
    private  String contactEmail;
    private  String loginUsername;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private  String password;
    private  String createdAt;
    private  String updatedAt;
    private  String deletedAt;
}
