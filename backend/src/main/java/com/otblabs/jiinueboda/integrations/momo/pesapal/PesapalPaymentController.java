package com.otblabs.jiinueboda.integrations.momo.pesapal;

import com.otblabs.jiinueboda.integrations.momo.pesapal.models.Credentials;
import com.otblabs.jiinueboda.integrations.momo.pesapal.models.PaymentRequestResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments/pesapal")
public class PesapalPaymentController {

    private final PesapalPaymentService pesapalPaymentService;

    public PesapalPaymentController(PesapalPaymentService pesapalPaymentService) {
        this.pesapalPaymentService = pesapalPaymentService;
    }


    @PostMapping("/request-payment")
    public ResponseEntity<PaymentRequestResponse> initiatePayment(@RequestBody PaymentInfo paymentInfo){

        String data = pesapalPaymentService.authenticate(new Credentials(Constants.test_consumer_key,Constants.test_consumer_secret));

        pesapalPaymentService.submitOnetimePaymentRequest(paymentInfo);
        return null;

    }
}
