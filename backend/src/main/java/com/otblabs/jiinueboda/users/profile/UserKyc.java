package com.otblabs.jiinueboda.users.profile;

import com.otblabs.jiinueboda.users.models.GuarantorDetails;
import com.otblabs.jiinueboda.users.models.NextOfKinDetails;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserKyc {
    private int userId;
    private MaritalStatus maritalStatus;
    private String countyOfResidence;
    private String currentResidentialAddress;
    private double incomePerDay;
    private boolean hasOtherSourcesOfIncome;
    private String incomeSourcesDetails;
    private double totalIncomeFromOtherSourcesPerDay;
    private GuarantorDetails guarantor;
    private NextOfKinDetails nextOfKin;
    public enum MaritalStatus {
        SINGLE,MARRIED
    }
    private String createdAt;
}






