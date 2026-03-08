package com.otblabs.jiinueboda.assets.tracking.activations;

import com.otblabs.jiinueboda.sms.SmsService;
import org.springframework.stereotype.Service;

@Service
public class ActivationService {

    private final SmsService smsService;

    public ActivationService(SmsService smsService) {
        this.smsService = smsService;
    }

    /*
    [14:41, 28/04/2025] Bryon: change IP and port by yourself
    [14:42, 28/04/2025] Bryon: S102A-Server information: gps.itrack.top:8841
    [14:42, 28/04/2025] Bryon: R11 Server information: s06a.itrack.top:9985
    [14:42, 28/04/2025] Bryon: R16-Server information: gps.itrack.top:8841
    [14:42, 28/04/2025] Bryon: SERVER,1,gps.itrack.top,8841,0#
    [14:50, 28/04/2025] Bryon: IMEI
    354778344404101
    0114947413
    [14:54, 28/04/2025] Bryon: SERVER,1,gps.itrack.top,8841,0#
    [15:13, 28/04/2025] Bryon: Apn-<SPBSJ*P:BSJGPS*P:BSJGPS*A:Safaricom>
     */

//    public void activateNewDevice(String phoneNumber) throws Exception{
//
//        String message =  "SERVER,1,gps.itrack.top,8841,0#";
//
//        ApiMessageDTO apiMessageDTO = new ApiMessageDTO();
//        apiMessageDTO.setMessage(message);
//        apiMessageDTO.setReciver(phoneNumber);
//
//        try{
//            smsService.  sendPromotionalSms(apiMessageDTO);
//        }catch (Exception ignored){}
//
//        System.out.println("Finished sending message to \n " +phoneNumber + "=======");
//
//    }
}
