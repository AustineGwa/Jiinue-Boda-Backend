package com.otblabs.jiinueboda.integrations.banking.IandM;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.otblabs.jiinueboda.integrations.banking.IandM.models.MpesaBankRequestData;
import com.otblabs.jiinueboda.integrations.banking.IandM.models.MpesaBankResponseData;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test/banking/im")
//@RequestMapping("/prod/banking/im")
public class IandMController {

    private final IandMservice iandMservice;

    public IandMController(IandMservice iandMservice) {
        this.iandMservice = iandMservice;
    }

    @PostMapping("/momo/recieve-ipn/{appId}")
    ResponseEntity<MpesaBankResponseData> recieveMpesaPush(@RequestBody String data, int appId){
            try{

                ObjectMapper objectMapper = new ObjectMapper();
                MpesaBankRequestData requestData = objectMapper.readValue(data, MpesaBankRequestData.class);
                return ResponseEntity.ok(iandMservice.saveMpesaTransaction(requestData,appId));
            }catch (Exception exception){
                exception.printStackTrace();
                MpesaBankResponseData responseData = new MpesaBankResponseData();
                responseData.setResultCode(1);
                responseData.setResultDesc("There was an error processing this request");
                return ResponseEntity.unprocessableEntity().body(responseData);
            }

    }


//    @PostMapping("/validate-payment/{appId}")
//    ResponseEntity<Object> getPaymentValidation(@RequestBody String data, @PathVariable int appId){
//        try{
//            return ResponseEntity.ok(iandMservice.getPaymentValidation(data,appId));
//        }catch (Exception exception){
//            exception.printStackTrace();
//            return ResponseEntity.internalServerError().build();
//        }
//
//    }

//    @PostMapping("/recieve-ipn/{appId}")
//    ResponseEntity<Object> getImMpesaTransaction(@RequestBody String data,
//                                                 @PathVariable int appId,
//                                                 @RequestHeader("username") String username,
//                                                 @RequestHeader("password") String password
//    ){
//
////        if(username.equals("tester@me") && password.equals("jfybv766!Tx#")){
//        if(username.equals("prod@m2_") && password.equals("jfmhjk7854!Tx#")){
//            try{
//                return ResponseEntity.ok(iandMservice.saveMpesaTransaction(data,appId));
//            }catch (Exception exception){
//                exception.printStackTrace();
//                BankResponseData responseData = new BankResponseData();
//                responseData.setResultCode(1);
//                responseData.setResultDesc("There was an error processing this request");
//                return ResponseEntity.unprocessableEntity().body(responseData);
//            }
//        }else {
//            return ResponseEntity.badRequest().body("Wrong API credentials provided");
//        }
//
//
//    }

//    @PostMapping("/init-b2c/{appId}")
//    ResponseEntity<Object> initImMpesaB2cTransaction(@PathVariable int appId, @RequestBody PaymentRequest paymentRequest){
//
//        try {
//
//            InitB2cResponse response = iandMservice.initImMpesaB2cTransaction(paymentRequest, appId);
//
//            if (response == null || response.getResponseCode().equals("FAILED")) {
//                return ResponseEntity.unprocessableEntity().body(response);
//            }else{
//                return ResponseEntity.ok().body(response);
//            }
//
//        }catch (Exception exception){
//            exception.printStackTrace();
//            return ResponseEntity.internalServerError().build();
//        }
//
//    }

//    @PostMapping("/receive-disburse-ipn/{appId}")
//    ResponseEntity<Object> receiveMesabi2cTransactionIPN(@PathVariable int appId, @RequestBody String data){
//
//        try {
//
//            return ResponseEntity.ok().build();
//        }catch (Exception exception){
//            exception.printStackTrace();
//            return ResponseEntity.internalServerError().build();
//        }
//
//    }
}
