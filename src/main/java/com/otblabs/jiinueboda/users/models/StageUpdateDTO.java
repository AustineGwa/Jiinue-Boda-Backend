package com.otblabs.jiinueboda.users.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StageUpdateDTO {
    private int userId;
    private int newStageId;
}
