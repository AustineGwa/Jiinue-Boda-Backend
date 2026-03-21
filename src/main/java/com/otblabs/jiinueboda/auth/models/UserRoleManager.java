package com.otblabs.jiinueboda.auth.models;

import com.otblabs.jiinueboda.users.models.Usertype;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRoleManager {
    private int userId;
    private Usertype usertype;
    private  boolean canApproveLoanLevel1;
    private  boolean canApproveLoanLevel2;
    private  boolean canApproveExpense;

}
