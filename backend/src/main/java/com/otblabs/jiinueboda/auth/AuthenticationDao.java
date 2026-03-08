package com.otblabs.jiinueboda.auth;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AuthenticationDao {
    private final JdbcTemplate jdbcTemplateOne;

    public AuthenticationDao(JdbcTemplate jdbcTemplateOne) {
        this.jdbcTemplateOne = jdbcTemplateOne;
    }

    void loginUser(){}
}
