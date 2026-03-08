package com.otblabs.jiinueboda.exceptions;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ExceptionsHandlerService {

    private JdbcTemplate jdbcTemplateOne;

    public ExceptionsHandlerService(JdbcTemplate jdbcTemplateOne) {
        this.jdbcTemplateOne = jdbcTemplateOne;
    }

    public void saveExceptionToDb(String exception, int appID){
        String sql = "INSERT INTO app_exceptions(exception, appID, createdAt) VALUES (?,?,NOW())";
        jdbcTemplateOne.update(sql,exception,appID);
    }
}
