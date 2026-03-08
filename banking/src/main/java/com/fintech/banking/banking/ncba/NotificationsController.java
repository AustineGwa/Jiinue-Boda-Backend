package com.fintech.banking.banking.ncba;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fintech.banking.banking.ncba.models.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationsController {

    private final NotificationService notificationService;

    public NotificationsController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping(value = "/payments/ncba", consumes = MediaType.TEXT_XML_VALUE, produces = MediaType.TEXT_XML_VALUE)
    public NCBAPaymentNotificationResult handlePaymentNotification(@RequestBody String xmlPayload) {

        XmlMapper xmlMapper = new XmlMapper();
        Envelope envelope = null;
        try {
            envelope = xmlMapper.readValue(xmlPayload, Envelope.class);
            NCBAPaymentNotificationRequest request = envelope.getBody().getRequest();
            return  notificationService.getBankNotification(request);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            NCBAPaymentNotificationResult result = new NCBAPaymentNotificationResult();
            result.setResult("FAIL");
            return result;
        }

    }

    @PostMapping(value = "/payments/ncba/paybill")
    public PaybillNotificationResponse handlePaymentNotificationPaybill(@RequestBody String payload) {

        PaybillNotificationResponse result = new PaybillNotificationResponse();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            PaybillNotification request = objectMapper.readValue(payload,PaybillNotification.class);

            System.out.println("PAYLOAD "+request.toString());

            return  notificationService.getBankNotificationPaybill(request);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            result.setStatus("0");
            result.setDescription("Failed API Response");
            return result;
        }



    }

//    @GetMapping("/payments/ncba/run-test")
//    String updateTableData(){
//        try {
//            bankNotificationService.updateTableData();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        return "complete";
//    }

}