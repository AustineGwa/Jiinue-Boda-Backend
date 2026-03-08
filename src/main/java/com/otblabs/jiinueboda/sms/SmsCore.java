package com.otblabs.jiinueboda.sms;

import com.otblabs.jiinueboda.sms.providers.ampletech.AMTSmsService;
import com.otblabs.jiinueboda.sms.providers.ampletech.Contact;
import com.otblabs.jiinueboda.sms.providers.ampletech.MessageData;
import com.otblabs.jiinueboda.sms.providers.africastalking.ATSmsService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SmsCore {

    private final ATSmsService atSmsService;
    private final JdbcTemplate jdbcTemplateOne;
    private final AMTSmsService amtSmsService;

    public SmsCore(ATSmsService atSmsService, JdbcTemplate jdbcTemplateOne, AMTSmsService amtSmsService) {
        this.atSmsService = atSmsService;
        this.jdbcTemplateOne = jdbcTemplateOne;
        this.amtSmsService = amtSmsService;
    }


    public String sendSingleTransactionalSms(ApiMessageDTO apiMessageDTO) throws Exception {

        List<Contact> contactList = new ArrayList<>();
        Contact contact = new Contact();
        contact.setNumber(apiMessageDTO.getReciver());
        contact.setBody(apiMessageDTO.getMessage());
        contact.setSms_type("plain");
        contactList.add(contact);
        MessageData messageData = new MessageData();
        messageData.setContact(contactList);

        String sql = "SELECT api_key from ample_tech_sms_config WHERE app_id=3";
        String apiKey = jdbcTemplateOne.queryForObject(sql,(rs,i)->rs.getString("api_key"));
        return  amtSmsService.sendSms(messageData,apiKey);
    }

    public String sendBulkTransactionalSms(List<ApiMessageDTO> apiMessageDTO) throws Exception {

        apiMessageDTO.forEach(message -> {
            List<Contact> contactList = new ArrayList<>();
            Contact contact = new Contact();
            contact.setNumber(message.getReciver());
            contact.setBody(message.getMessage());
            contact.setSms_type("plain");
            contactList.add(contact);
            MessageData messageData = new MessageData();
            messageData.setContact(contactList);

            String sql = "SELECT api_key from ample_tech_sms_config WHERE app_id=3";
            String apiKey = jdbcTemplateOne.queryForObject(sql,(rs,i)->rs.getString("api_key"));
            amtSmsService.sendSms(messageData,apiKey);
        });

        return "success";


    }

    public String sendSinglePromotionalSms(ApiMessageDTO apiMessageDTO) throws Exception {

        List<Contact> contactList = new ArrayList<>();
        Contact contact = new Contact();
        contact.setNumber(apiMessageDTO.getReciver());
        contact.setBody(apiMessageDTO.getMessage());
        contact.setSms_type("plain");
        contactList.add(contact);

        MessageData messageData = new MessageData();
        messageData.setContact(contactList);

        String sql = "SELECT api_key from ample_tech_sms_config WHERE app_id=6";
        String apiKey = jdbcTemplateOne.queryForObject(sql,(rs,i)->rs.getString("api_key"));
        return  amtSmsService.sendPromotionalSms(messageData,apiKey);
    }

    public String sendBulkPromotionalSms(List<ApiMessageDTO> apiMessageDTO) {

        apiMessageDTO.forEach(message -> {
            List<Contact> contactList = new ArrayList<>();
            Contact contact = new Contact();
            contact.setNumber(message.getReciver());
            contact.setBody(message.getMessage());
            contact.setSms_type("plain");
            contactList.add(contact);

            MessageData messageData = new MessageData();
            messageData.setContact(contactList);

            String sql = "SELECT api_key from ample_tech_sms_config WHERE app_id=6";
            String apiKey = jdbcTemplateOne.queryForObject(sql,(rs,i)->rs.getString("api_key"));
            amtSmsService.sendPromotionalSms(messageData,apiKey);
        });
        return "success";
    }
}
