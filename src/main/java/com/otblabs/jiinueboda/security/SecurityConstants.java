package com.otblabs.jiinueboda.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SecurityConstants {

    public static String SECRET;

    @Value("${fintech.SECRET_KEY}")
    public void setSecret(String secret) {
        SECRET = secret;
    }

    public static final long EXPIRATION_TIME = 43_200_000;
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
}
