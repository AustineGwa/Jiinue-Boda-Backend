package com.otblabs.jiinueboda.groups;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GroupLocationUpdate {
    private int groupId;
    private int countyId;
    private int subCountyId;
    private int wardId;
}
