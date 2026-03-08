package com.otblabs.jiinueboda.accounting.income;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/income-statement")
public class IncomeStatementController {

    private final IncomeStatementService incomeStatementService;

    public IncomeStatementController(IncomeStatementService incomeStatementService) {
        this.incomeStatementService = incomeStatementService;
    }

    @GetMapping("/monthly-statement/{month}/{year}")
    public ResponseEntity<Object> getMonthlyIncomeStatement(@PathVariable String month, @PathVariable String year){
        try {
            return  ResponseEntity.ok(incomeStatementService.getMonthlyIncomeStatement(month,year));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.unprocessableEntity().build();
        }
    }



}




/*
5% service charge
5% per month interest rate
2% insuarance
600 per loan monitoring fee
battery fee
 */
