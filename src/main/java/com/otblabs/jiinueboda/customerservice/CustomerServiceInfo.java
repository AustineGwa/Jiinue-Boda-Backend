package com.otblabs.jiinueboda.customerservice;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerServiceInfo {
    private int serviceId;
    private int branchId;
    private String serviceCategory;
    private String serviceCategoryOther;
    private String customerType;
    private String customerIdNumber;
    private String customerPhoneNumber;
    private boolean customerCanBeServed;
    private String serviceStatus;
    private String loanOfficerComment;
    private String opsMangerComment;
    private String createdAt;
}
