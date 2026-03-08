package com.otblabs.jiinueboda.sms.providers.ampletech;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MessageData {
    List<Contact> contact;
}
