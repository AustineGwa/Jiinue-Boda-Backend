package com.otblabs.jiinueboda.config.mobile;

import com.otblabs.jiinueboda.config.mobile.models.AppConfig;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class AppConfigService {
    private final JdbcTemplate jdbcTemplateOne;

    public AppConfigService(JdbcTemplate jdbcTemplateOne) {
        this.jdbcTemplateOne = jdbcTemplateOne;
    }


    public AppConfig getAppConfig(int appID) throws Exception{
        String sql = "SELECT * FROM app_config WHERE appID=?";
        return jdbcTemplateOne.queryForObject(sql,(rs,i)->setAppConfig(rs),appID);
    }

    private AppConfig setAppConfig(ResultSet rs) throws SQLException {
        AppConfig appConfig = new AppConfig();
        appConfig.setCurrentVersion(rs.getInt("currentVersion"));
        appConfig.setDownloadUrl(rs.getString("downloadUrl"));
        return appConfig;
    }
}
