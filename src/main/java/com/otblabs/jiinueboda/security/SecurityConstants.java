package com.otblabs.jiinueboda.security;

public class SecurityConstants {
    static int latestAppVersion = 2;
    public static final String SECRET = "ef18adcd-8eac-4aa4-bd9b-9fbebde4b78c-driversCC" + latestAppVersion;
    public static final long EXPIRATION_TIME = 43_200_000;
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
}
