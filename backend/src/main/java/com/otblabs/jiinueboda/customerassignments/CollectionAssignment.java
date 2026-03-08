package com.otblabs.jiinueboda.customerassignments;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CollectionAssignment {
    private String id;
    private int customerId;
    private int repId;
    private String assignedOn;
    private String validUntil;

}
