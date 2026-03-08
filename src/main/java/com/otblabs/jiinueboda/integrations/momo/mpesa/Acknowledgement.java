package com.otblabs.jiinueboda.integrations.momo.mpesa;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Acknowledgement {
    @JsonProperty("ResultCode")
    int ResultCode;
    @JsonProperty("ResultDesc")
    String ResultDesc;
}
