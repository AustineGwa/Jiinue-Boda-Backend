package com.otblabs.jiinueboda.collections;

import com.otblabs.jiinueboda.collections.models.PendingCollection;
import com.otblabs.jiinueboda.collections.models.ReconciliationData;
import com.otblabs.jiinueboda.integrations.momo.mpesa.MpesaTransactionsService;
import com.otblabs.jiinueboda.loans.models.OldAccountNewAccountPayment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/collections")
public class ReconciliationController {

    private final CollectionsService collectionsService;
    private final MpesaTransactionsService mpesaTransactionsService;

    public ReconciliationController(CollectionsService collectionsService, MpesaTransactionsService mpesaTransactionsService) {
        this.collectionsService = collectionsService;
        this.mpesaTransactionsService = mpesaTransactionsService;
    }


    @GetMapping("/pending-reconciliations/{month}/{year}")
    ResponseEntity<List<PendingCollection>> getPendingCollections(@PathVariable int month, @PathVariable int year){

        try {

            if (month == 0 && year == 0) {
                return ResponseEntity.ok(collectionsService.getUnReconciledCollections());
            } else if (month == 0) {
                return ResponseEntity.ok(collectionsService.getUnReconciledCollections(year));
            } else {
                return ResponseEntity.ok(collectionsService.getUnReconciledCollections(month, year));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/post-reconciliations")
    ResponseEntity<Object> postTransaction(@RequestBody ReconciliationData reconciliationData){
        try{
            var res = collectionsService.postTransaction(reconciliationData);
            mpesaTransactionsService.adjustUserLoanBalance(reconciliationData.getCorrectLoanId().trim());

            mpesaTransactionsService.confirmPaymentRecieved(
                    reconciliationData.getTransactionId(),
                    String.valueOf(reconciliationData.getAmount()),
                    reconciliationData.getCorrectLoanId()
            );
            return ResponseEntity.ok(res);
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

    @PostMapping("/reconcile-to-new-account")
    ResponseEntity<Object> movePaymentToNewAccount(@RequestBody OldAccountNewAccountPayment oldAccountNewAccountPayment){
        try{
            int response = collectionsService.movePaymentToNewAccount(oldAccountNewAccountPayment);
            return ResponseEntity.ok(response);
        }catch(Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }
}
