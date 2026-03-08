package com.otblabs.jiinueboda.fieldapp.geographic.models;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ward {
    private Long wardId;
    private String name;
    private Long subCountyId;
}
