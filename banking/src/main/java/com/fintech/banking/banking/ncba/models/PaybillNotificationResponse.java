package com.fintech.banking.banking.ncba.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PaybillNotificationResponse {
    @JsonProperty("STATUS")
    private String status;
    @JsonProperty("DESCRIPTION")
    private String description;
}
