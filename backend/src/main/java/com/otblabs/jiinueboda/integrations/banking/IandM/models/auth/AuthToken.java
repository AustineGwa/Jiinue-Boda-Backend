package com.otblabs.jiinueboda.integrations.banking.IandM.models.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AuthToken {
    @JsonProperty("access_token")
    String accessToken;
    @JsonProperty("refresh_token_expires_in")
    String refreshTokenExpiresIn;
    @JsonProperty("refresh_token")
    String refreshToken;
    @JsonProperty("scope")
    String scope;
    @JsonProperty("expires_in")
    String expiresIn;
}
