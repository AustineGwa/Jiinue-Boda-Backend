package com.otblabs.jiinueboda.utility.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class RestResponseBody<T> {
        private  T  responseMessage;
}
