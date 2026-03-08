package com.fintech.banking.banking.ncba;

import com.fintech.banking.banking.ncba.models.NCBAPaymentNotificationRequest;
import com.fintech.banking.banking.ncba.models.NCBAPaymentNotificationResult;
import com.fintech.banking.banking.ncba.models.PaybillNotification;
import com.fintech.banking.banking.ncba.models.PaybillNotificationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;

@Service
public class NotificationService {

    private final JdbcTemplate jdbcTemplateOne;

    @Value("${server.port}")
    private int serverPort;

    public NotificationService(JdbcTemplate jdbcTemplateOne) {
        this.jdbcTemplateOne = jdbcTemplateOne;
    }

    public NCBAPaymentNotificationResult getBankNotification(NCBAPaymentNotificationRequest ncbaPaymentNotificationRequest){

        NCBAPaymentNotificationResult ncbaPaymentNotificationResult = new NCBAPaymentNotificationResult();



    try {
    String sql = "INSERT INTO ncba_incoming_payments(user_,password,hashVal,transType,transID,transTime," +
            "transAmount,accountNr,phoneNr,customerName,narrative,status,mpesaCode,phone_Number_m,customerName_m,created_at,updated_at) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,NOW(),NOW())";

    String user = ncbaPaymentNotificationRequest.getUser();
    String password = ncbaPaymentNotificationRequest.getPassword();
    String hashVal = String.valueOf(ncbaPaymentNotificationRequest.getHashVal());
    String transType = ncbaPaymentNotificationRequest.getTransType();
    String transID = ncbaPaymentNotificationRequest.getTransID();
    String transTime = String.valueOf(ncbaPaymentNotificationRequest.getTransTime());  //(YYYYMMDDhhmmss)
    String transAmount = ncbaPaymentNotificationRequest.getTransAmount();
    String accountNr = ncbaPaymentNotificationRequest.getAccountNr();
    String phoneNr = ncbaPaymentNotificationRequest.getPhoneNr();
    String customerName = ncbaPaymentNotificationRequest.getCustomerName();
    String status = ncbaPaymentNotificationRequest.getStatus();
    String narrative = ncbaPaymentNotificationRequest.getNarrative();

    KeyHolder keyHolder = new GeneratedKeyHolder();

    jdbcTemplateOne.update( connection -> {
                String mpesaCode = "";
                String phoneNumber_m = "";
                String customerName_m = "";

                if("171".equals(transType) && !customerName.isEmpty()){
                    String[] extractedVariables = extractVariables(customerName);
                    mpesaCode = extractedVariables[0];
                    phoneNumber_m = extractedVariables[1];
                    customerName_m = extractedVariables[2];
                }

                PreparedStatement ps = connection.prepareStatement(sql, new String[] {"id"});
                ps.setString(1, user);
                ps.setString(2,password);
                ps.setString(3, hashVal);
                ps.setString(4, transType);
                ps.setString(5,transID);
                ps.setString(6, transTime);
                ps.setString(7, String.valueOf(transAmount));
                ps.setString(8,accountNr);
                ps.setString(9,phoneNr);
                ps.setString(10,customerName);
                ps.setString(11,narrative);
                ps.setString(12,status);
                ps.setString(13,mpesaCode);
                ps.setString(14,phoneNumber_m);
                ps.setString(15,customerName_m);
                return ps;
            }, keyHolder);

    int id = keyHolder.getKey().intValue();
    ncbaPaymentNotificationResult.setResult("OK");

    } catch (Exception e) {
        e.printStackTrace();
        ncbaPaymentNotificationResult.setResult("FAIL");
    }
    return ncbaPaymentNotificationResult;

    }

    public PaybillNotificationResponse getBankNotificationPaybill(PaybillNotification paybillNotification){
        System.out.println(paybillNotification);
        PaybillNotificationResponse paybillNotificationResponse = new PaybillNotificationResponse();

        return paybillNotificationResponse;

    }

    public static String[] extractVariables(String inputString) {
        String[] variables = new String[3];
        int firstSpaceIndex = inputString.indexOf(' ');
        int secondSpaceIndex = inputString.indexOf(' ', firstSpaceIndex + 1);

        if (firstSpaceIndex != -1 && secondSpaceIndex != -1) {
            variables[0] = inputString.substring(0, firstSpaceIndex);
            variables[1] = inputString.substring(firstSpaceIndex + 1, secondSpaceIndex);
            variables[2] = inputString.substring(secondSpaceIndex + 1);
        }

        return variables;
    }

//    public void updateTableData() throws Exception{
//        String sql = "SELECT num,customerName FROM ncba_incoming_payments_prod WHERE transType =171 and mpesaCode is null";
//        jdbcTemplateOne.query(sql,(rs,i)->{
//
//            String mpesaCode = "";
//            String phoneNumber_m = "";
//            String customerName_m = "";
//
//
//            String[] extractedVariables = extractVariables(rs.getString("customerName"));
//            mpesaCode = extractedVariables[0];
//            phoneNumber_m = extractedVariables[1];
//            customerName_m = extractedVariables[2];
//
//
//            String sqlUpdate = "UPDATE ncba_incoming_payments_prod SET mpesaCode=?,phone_Number_m=?,customerName_m=? WHERE num=?";
//            jdbcTemplateOne.update(sqlUpdate,mpesaCode,phoneNumber_m,customerName_m,rs.getInt("num"));
//
//            return null;
//        });
//    }
}
