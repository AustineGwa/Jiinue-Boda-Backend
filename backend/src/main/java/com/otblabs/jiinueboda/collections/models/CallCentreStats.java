package com.otblabs.jiinueboda.collections.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CallCentreStats {
    private int allCallsAttempted;
    private int allCallsPicked;
    private int userNotReachable;
    private int userIgnored;
    private int userBlocked;
}
