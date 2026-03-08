package com.otblabs.jiinueboda.sms;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sms")
public class SendSmsController {

    private final SmsService smsService;

    public SendSmsController(SmsService smsService) {
        this.smsService = smsService;
    }

    @PostMapping("/send/transactional/single")
    public ResponseEntity<Object> sendTransactionalSms(@RequestBody ApiMessageDTO apiMessageDTO){
        try {
            String response = smsService.sendSingleAPITransactionalMessage(apiMessageDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

    @PostMapping("/send/transactional/bulk")
    public ResponseEntity<Object> sendBulkTransactionalSms(@RequestBody List<ApiMessageDTO> apiMessageDTO){
        try {
            String response = smsService.sendBulkTransactionalSms(apiMessageDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

    @PostMapping("/send/promotional/single")
    public ResponseEntity<Object> sendPromotionalSms(@RequestBody ApiMessageDTO apiMessageDTO){
        try {
            String response = smsService.sendSingleAPIPromotionalMessage(apiMessageDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

    @PostMapping("/send/promotional/bulk")
    public ResponseEntity<Object> sendBulkPromotionalSms(@RequestBody List<ApiMessageDTO> apiMessageDTO){
        try {
            String response = smsService.sendBulkAPIPromotionalMessage(apiMessageDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }


}
