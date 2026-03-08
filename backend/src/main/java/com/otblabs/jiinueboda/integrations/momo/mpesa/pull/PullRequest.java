package com.otblabs.jiinueboda.integrations.momo.mpesa.pull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PullRequest {
    private String StartDate;
    private String EndDate;
}
