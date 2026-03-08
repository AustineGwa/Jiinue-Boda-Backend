package com.fintech.banking.banking.ncba;

import com.fintech.banking.banking.ncba.models.NcbaMpesaDisbursement;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentsController {


    private final PaymentsService paymentService;

    public PaymentsController(PaymentsService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/makePayment")
    public String makePayment(@RequestBody NcbaMpesaDisbursement paymentRequest) {
        return paymentService.makePayment(paymentRequest);
    }


}
