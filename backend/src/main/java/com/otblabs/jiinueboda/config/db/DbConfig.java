package com.otblabs.jiinueboda.config.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DbConfig {

    @Value("${fintech.dbuser}")
    String DB_USER;

    @Value("${fintech.dbpass}")
    String DB_PASS;

    @Value("${fintech.dburl}")
    String DB_URL;

    @Bean
    public HikariConfig hikariConfig1() {
        HikariConfig config = new HikariConfig();
        config.setUsername(DB_USER);
        config.setPassword(DB_PASS);
        config.setJdbcUrl(DB_URL);
        return config;
    }

    @Bean
    @Primary
    public DataSource firstDataSource(HikariConfig hikariConfig1){return new HikariDataSource(hikariConfig1); }

    @Bean
    public JdbcTemplate jdbcTemplateOne(@Qualifier("firstDataSource") DataSource ds){
        return new JdbcTemplate(ds);
    }

}
