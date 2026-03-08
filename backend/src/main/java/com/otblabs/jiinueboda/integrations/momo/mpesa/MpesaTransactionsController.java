package com.otblabs.jiinueboda.integrations.momo.mpesa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.otblabs.jiinueboda.integrations.momo.mpesa.b2c.models.B2cResultNotification;
import com.otblabs.jiinueboda.integrations.momo.mpesa.b2c.models.Result;
import com.otblabs.jiinueboda.integrations.momo.mpesa.c2b.models.C2BNotification;
import com.otblabs.jiinueboda.jiinue.models.LoanPayeeDetail;
import com.otblabs.jiinueboda.sms.SmsService;
import com.otblabs.jiinueboda.users.UserService;
import com.otblabs.jiinueboda.users.models.Signatory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments/momo")
public class MpesaTransactionsController {

    private final MpesaTransactionsService mpesaTransactionsService;
    private final UserService userService;
    private final SmsService smsService;

    public MpesaTransactionsController(MpesaTransactionsService mpesaTransactionsService, UserService userService, SmsService smsService) {
        this.mpesaTransactionsService = mpesaTransactionsService;
        this.userService = userService;
        this.smsService = smsService;
    }

    @GetMapping("/register/{appId}")
    public ResponseEntity<String> registerUrl(@PathVariable int appId){
        try {
            return ResponseEntity.ok(mpesaTransactionsService.registerURL(appId));
        } catch (Exception ex) {
            ex.printStackTrace();
            return  ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/postpull/{appId}")
    public ResponseEntity<Acknowledgement> getPullUrlNotification(@RequestBody String pullResponse, @PathVariable int appId){
        try {
            Acknowledgement acknowledgement = mpesaTransactionsService.postPullTransactions(pullResponse,appId);
            return ResponseEntity.ok(acknowledgement);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.unprocessableEntity().build();
        }
    }

    @PostMapping("/postc2b/{appId}")
    public ResponseEntity<Acknowledgement> getC2bNotification(@RequestBody String c2bresponse, @PathVariable int appId){
        try {

            ObjectMapper objectMapper = new ObjectMapper();
            C2BNotification data = objectMapper.readValue(c2bresponse, C2BNotification.class);

            mpesaTransactionsService.saveC2bNotification(data,appId, false);
            mpesaTransactionsService.adjustUserLoanBalance(data.getBillRefNumber().trim());
            mpesaTransactionsService.confirmPaymentRecieved(data.getTransID(),data.getTransAmount(),data.getBillRefNumber());

            return ResponseEntity.ok(new Acknowledgement(0,"Success"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.unprocessableEntity().build();
        }
    }

    @PostMapping("/poststkpush/{appId}")
    public ResponseEntity<Acknowledgement> getStkPushNotification(@RequestBody String stkPushResponse , @PathVariable int appId) {

        try {
            Acknowledgement acknowledgement = mpesaTransactionsService.saveStkPushNotification(stkPushResponse, appId);
            return ResponseEntity.ok(acknowledgement);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.unprocessableEntity().build();
        }
    }

    @PostMapping("/postb2c/{appId}")
    public ResponseEntity<Acknowledgement> saveb2cResponse(
            @RequestBody String b2cResponse,
            @PathVariable int appId) {

        try {

            ObjectMapper objectMapper = new ObjectMapper();
            B2cResultNotification b2cResultNotification = objectMapper.readValue(b2cResponse,B2cResultNotification.class);
            Result result = b2cResultNotification.getResult();

            if(mpesaTransactionsService.b2cTransactionAlreadyProcessed(result)){
                return ResponseEntity.ok(new Acknowledgement(0,"Already processed"));
            }

            Acknowledgement acknowledgement = mpesaTransactionsService.saveB2cResponse(result, appId);

            if(result.getResultCode().equals("0")){

                int transactionAmount = Integer.parseInt(result.getResultParameters().getB2cResultParameter().get(0).getValue());
                String recieverName = result.getResultParameters().getB2cResultParameter().get(2).getValue();


                mpesaTransactionsService.updateDisbursement(result.getConversationId());
                LoanPayeeDetail loanPayeeDetail = userService.getLoanPayeeDetail(result.getConversationId());



                if(mpesaTransactionsService.isLoan(result.getConversationId())){
                    smsService.sendMessageToBorrower(recieverName,transactionAmount,loanPayeeDetail);
                }else{

                    String description  = mpesaTransactionsService.getB2cTransactionDescription(result.getConversationId());
                    smsService.sendMessageToReciever(recieverName,transactionAmount,loanPayeeDetail.getPartyB(),appId, description);
                }
                List<Signatory> signatories = userService.getSignatoriesByAppId(appId);
                String balance_ua = mpesaTransactionsService.getCurrentAccountLoanBalance(result.getConversationId());
                smsService.sendMessageToSignatories(appId,transactionAmount,recieverName,balance_ua,signatories);



            }


            return ResponseEntity.ok(acknowledgement);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.unprocessableEntity().build();
        }
    }

    @PostMapping("/postbuygoods/{appId}")
    public ResponseEntity<Acknowledgement> savebuygoodsResponse(@RequestBody String buygoodsResponse , @PathVariable int appId) {

        try {
            Acknowledgement acknowledgement = mpesaTransactionsService.saveBuyGoodsResponse(buygoodsResponse, appId);
            return ResponseEntity.ok(acknowledgement);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.unprocessableEntity().build();
        }
    }

    @PostMapping("/postpaybill/{appId}")
    public ResponseEntity<Acknowledgement> savepaybillsResponse(@RequestBody String paybillResponse , @PathVariable int appId) {

        try {
            Acknowledgement acknowledgement = mpesaTransactionsService.savePaybillsResponse(paybillResponse, appId);
            return ResponseEntity.ok(acknowledgement);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.unprocessableEntity().build();
        }
    }

}
    



