package com.otblabs.jiinueboda.integrations.momo.mpesa.paybill;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaybillResultParameters {
    @JsonProperty("ResultParameter")
    private List<PaybillResultParameter> resultParameters;
}
