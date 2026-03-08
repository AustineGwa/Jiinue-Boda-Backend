package com.otblabs.jiinueboda.tracking.trackers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tracker {
    private int id;
    private String model;
    private String imei;
    private String simcard;
    private int userId;
    private String loanId;
}
