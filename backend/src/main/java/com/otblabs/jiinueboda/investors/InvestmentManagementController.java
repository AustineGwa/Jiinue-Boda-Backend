package com.otblabs.jiinueboda.investors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/investors")
public class InvestmentManagementController {

    private final  InvestmentManagementService investmentManagementService;

    public InvestmentManagementController(InvestmentManagementService investmentManagementService) {
        this.investmentManagementService = investmentManagementService;
    }

    @GetMapping("/all")
    ResponseEntity<Object> getAllInvestor(){
        try{

            return ResponseEntity.ok(investmentManagementService.getAllInvestors());
        }catch (Exception exception){
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/investments/{investorId}")
    ResponseEntity<Object> getAllInvestorInvestments(@PathVariable int investorId){
        try{
            return ResponseEntity.ok(investmentManagementService.getAllInvestorInvestments(investorId));
        }catch (Exception exception){
            return ResponseEntity.internalServerError().build();
        }
    }


}
