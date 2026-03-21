package com.otblabs.jiinueboda.auth.models;

import lombok.*;

@Data
@AllArgsConstructor
public class LoginResponse<T> {
    private String message;
    private T data;
    private boolean otpRequired;
    private String sessionId;

    public LoginResponse(String message, T data) {
        this.message = message;
        this.data = data;
        this.otpRequired = false;
        this.sessionId = null;
    }
}
