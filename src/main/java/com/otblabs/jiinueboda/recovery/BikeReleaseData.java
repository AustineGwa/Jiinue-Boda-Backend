package com.otblabs.jiinueboda.recovery;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BikeReleaseData {
    int userId;
    int recoveryId;
    int storageAmount;
    String releaseDate;
    String releaseComment;
}
