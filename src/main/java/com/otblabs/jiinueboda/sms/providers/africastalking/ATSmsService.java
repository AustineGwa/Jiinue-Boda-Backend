package com.otblabs.jiinueboda.sms.providers.africastalking;

import okhttp3.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class ATSmsService {

    private final JdbcTemplate jdbcTemplateOne;

    public ATSmsService(JdbcTemplate jdbcTemplateOne) {
        this.jdbcTemplateOne = jdbcTemplateOne;
    }


    public void sendATSms(int appId, String phoneNumber, String message) throws Exception {

        AtConfig atConfig = getAtConfigForAccount(appId);
        RequestBody formBody = new FormBody.Builder()
                .add("username", atConfig.getAccountUserName())
                .add("to", phoneNumber)
                .add("from",atConfig.getAccountName())
                .add("message",message)
                .build();

        Request request = new Request.Builder()
                .url("https://api.africastalking.com/version1/messaging")
                .post(formBody)
                .addHeader("apiKey", atConfig.getApiKey())
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept", "application/json")
                .build();

        OkHttpClient okHttpClient = new OkHttpClient();
        Call call = okHttpClient.newCall(request);
        call.execute();

    }

    AtConfig getAtConfigForAccount(int appId) throws Exception{
        String sql = "SELECT * FROM at_config WHERE app_id = ?";
        return jdbcTemplateOne.queryForObject(sql, (resultSet, i) -> setAtConfig(resultSet),appId);
    }

    private AtConfig setAtConfig(ResultSet rs) throws SQLException {
        AtConfig atConfig = new AtConfig();
        atConfig.setAccountUserName(rs.getString("auth_username"));
        atConfig.setAccountName(rs.getString("account_name"));
        atConfig.setApiKey(rs.getString("api_key"));
        return atConfig;

    }
}
