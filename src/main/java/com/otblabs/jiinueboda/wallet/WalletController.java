package com.otblabs.jiinueboda.wallet;

import com.otblabs.jiinueboda.wallet.models.TransactionApproval;
import com.otblabs.jiinueboda.wallet.models.UserTransaction;
import com.otblabs.jiinueboda.wallet.models.Wallet;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }


    @GetMapping("/balance/{userId}")
    ResponseEntity<Object> getUserWallet(@PathVariable int userId){
        try {
            return ResponseEntity.ok(walletService.getWalletForUser(userId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

    @PostMapping("/request/withdraw")
    ResponseEntity<Object> requestCreditWallet(@RequestBody UserTransaction userTransaction){
        try {
            Wallet wallet = walletService.requestCreditWallet(userTransaction);
            return  ResponseEntity.ok(wallet);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

    @PostMapping("/transactions/approve")
    ResponseEntity<Object> approveWithdrawal(@RequestBody TransactionApproval transactionApproval){


        try {
             walletService.approveTransactions(transactionApproval);
            return  ResponseEntity.ok("Successfully approved");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

}
