package com.otblabs.jiinueboda.jifuel.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoanTierLimit {
    private int id;
    private int level;
    private String tierName;
    private List<Integer> availableLimits;
    private String createdAt;
}
