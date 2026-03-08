package com.otblabs.jiinueboda.collections.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HourlyCollection {
    String hourOfDay;
    int amountCollected;
}
