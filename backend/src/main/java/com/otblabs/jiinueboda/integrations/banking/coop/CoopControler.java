package com.otblabs.jiinueboda.integrations.banking.coop;

import com.otblabs.jiinueboda.integrations.banking.coop.core.CoopResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/test/banking/coop")
//@RequestMapping("/prod/banking/coop")
public class CoopControler {

    private final CoopService coopService;

    public CoopControler(CoopService coopService) {
        this.coopService = coopService;
    }

    @PostMapping("recieve-ipn")
    ResponseEntity<CoopResponseBody> getCoopCoreBanking(@RequestBody String data){
        try{
            return ResponseEntity.ok(coopService.saveCoreBanking(data));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

    @PostMapping("init-mpesa-transfer")
    ResponseEntity<Object> initMpesaTransfer(){

        try{
            return ResponseEntity.ok(coopService.initiateSendToMpesaFundsTransfer());
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/send-to-mpes-ipn")
    ResponseEntity<Object> getSendToMpesaIpn(@RequestBody String data){
        try{
            return ResponseEntity.ok(coopService.saveSendToMpesaIpn(data));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

//    @GetMapping("/init-mpesa-transfer")
//    ResponseEntity<Object> initMpesaTransfer(){
//        try{
//            return ResponseEntity.ok(coopService.initiatePesalinkToPhoneTransfer());
//        }catch (Exception exception){
//            exception.printStackTrace();
//            return ResponseEntity.internalServerError().build();
//        }
//
//    }
}
