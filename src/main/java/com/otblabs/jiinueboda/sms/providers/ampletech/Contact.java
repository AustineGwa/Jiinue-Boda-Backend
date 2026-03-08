package com.otblabs.jiinueboda.sms.providers.ampletech;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Contact {
    private String number;
    private String body;
    private String sms_type;
}
