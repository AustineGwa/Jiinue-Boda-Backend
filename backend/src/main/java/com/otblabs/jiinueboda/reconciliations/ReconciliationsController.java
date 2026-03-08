package com.otblabs.jiinueboda.reconciliations;

import com.otblabs.jiinueboda.reconciliations.models.UtilityMpesaTransaction;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

@RestController
@RequestMapping("/reconcile")
public class ReconciliationsController {
    private final MissingPaymentService missingPaymentService;

    public ReconciliationsController(MissingPaymentService missingPaymentService) {
        this.missingPaymentService = missingPaymentService;
    }


    @PostMapping("/confirm-missing-payments")
    public ResponseEntity<Object> processCsvFile(@RequestParam("file") MultipartFile file ) {
        try {

            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Please upload a file");
            }

            // Check if file is CSV
            if (!file.getContentType().equals("text/csv") &&  !file.getOriginalFilename().endsWith(".csv")) {
                return ResponseEntity.badRequest().body("Please upload a CSV file");
            }

            // Create temporary file
            Path tempFile = Files.createTempFile("utility-account-", ".csv");

            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

            // Parse the file
            List<UtilityMpesaTransaction> transactions = missingPaymentService.parseUtilityAccountFile(tempFile.toString());

            // Clean up temp file
            Files.delete(tempFile);

            return ResponseEntity.ok().body(missingPaymentService.getAndUpdateMissingMpesaTransactions(transactions));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error processing CSV file: " + e.getMessage());
        }
    }

    @PostMapping("/pull-request/{startDate}/{endDate}")
    public ResponseEntity<Object> getMissingTransactionsFromPullData(@PathVariable String startDate, @PathVariable String endDate){
        try{
            return ResponseEntity.ok(missingPaymentService.getMissingTransactionsFromPullData(startDate,endDate));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

}
