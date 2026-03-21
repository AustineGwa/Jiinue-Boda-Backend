package com.otblabs.jiinueboda.auth.models;

import lombok.Data;

@Data
public class OtpVerificationRequest {
    private String userIdentifier;
    private String sessionId;
    private String otpCode;
}
