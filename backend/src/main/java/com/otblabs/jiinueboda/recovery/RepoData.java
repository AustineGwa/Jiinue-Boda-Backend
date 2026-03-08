package com.otblabs.jiinueboda.recovery;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RepoData {
    int userId;
    int recoveryId;
    int recoveryAmount;
    String recoveryDate;
    String recoveryComment;

}
