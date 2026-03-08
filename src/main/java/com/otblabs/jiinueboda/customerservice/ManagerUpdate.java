package com.otblabs.jiinueboda.customerservice;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ManagerUpdate {
    private int serviceId;
    private String managerComment;
}
