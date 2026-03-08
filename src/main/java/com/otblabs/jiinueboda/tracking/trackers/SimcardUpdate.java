package com.otblabs.jiinueboda.tracking.trackers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimcardUpdate {
    int trackerId;
    String simcard;
}
