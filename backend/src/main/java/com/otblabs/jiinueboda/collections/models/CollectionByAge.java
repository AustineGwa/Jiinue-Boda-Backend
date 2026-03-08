package com.otblabs.jiinueboda.collections.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CollectionByAge {
    private String loanAgeGroup;
    private int amountCollected;
}
