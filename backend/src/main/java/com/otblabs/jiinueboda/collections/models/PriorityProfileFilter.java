package com.otblabs.jiinueboda.collections.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PriorityProfileFilter {
    int varRationMore;
    int numberOfCallsLess;
    int loanAgeMore;
    int loanAgeLess;
}
