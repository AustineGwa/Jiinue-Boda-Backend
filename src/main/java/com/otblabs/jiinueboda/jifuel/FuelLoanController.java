package com.otblabs.jiinueboda.jifuel;

import com.otblabs.jiinueboda.jifuel.models.*;
import com.otblabs.jiinueboda.users.UserService;
import com.otblabs.jiinueboda.users.models.SystemUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/fuel/loan")
public class FuelLoanController {

     private final FuelLoanService fuelLoanService;
     private final UserService userService;

    public FuelLoanController(FuelLoanService fuelLoanService, UserService userService) {
        this.fuelLoanService = fuelLoanService;
        this.userService = userService;
    }


    @GetMapping("/loan-limits")
    ResponseEntity<List<LoanTierLimit>> getTierLimits(Principal principal){
        try{
            return ResponseEntity.ok(fuelLoanService.getTierLimits(principal.getName()));
        }catch (Exception exception){
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/dashboard/main")
    ResponseEntity<Object> getMainDashboardData(){
        try {
            return ResponseEntity.ok(fuelLoanService.getMainDashboardData());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.unprocessableEntity().build();
        }
    }

    @GetMapping("/dashboard/main/partners/{partnerId}")
    ResponseEntity<Object> getPartnerDashboardData(@PathVariable int partnerId){
        try {
            return ResponseEntity.ok(fuelLoanService.getPartnerDashboardData(partnerId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.unprocessableEntity().build();
        }
    }

    @GetMapping("/monthly_financials")
    ResponseEntity<List<MonthlyFinancial>> getMonthlyFinancials(){
        try{
            return ResponseEntity.ok(fuelLoanService.getMonthlyFinancials());
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/totals_per_day")
    ResponseEntity<List<LoansPerDay>> getTotalDailyTotalLoans(){
        try{
           return ResponseEntity.ok(fuelLoanService.getTotalDailyTotalLoans());
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/request")
    ResponseEntity<Object> requestFuelLoan(@RequestBody FuelLoan fuelLoan, Principal principal){

        try {
            SystemUser systemUser = userService.getByEmailOrPhone(principal.getName());

            if(systemUser.getPartnerId() == 0){
                return ResponseEntity.unprocessableEntity().body("Sorry we can't process your request at this time, your don't belong to any sacco");
            }

            //check if user belongs to a partner with a valid balance
            if(fuelLoanService.getPartnerBalance(systemUser.getPartnerId()) < fuelLoan.getFuelLoanPurchased()){
                return ResponseEntity.unprocessableEntity().body("Sorry we can't process your request at this time, your sacco balance is depleted");
            }

            if(!fuelLoanService.userHasPendingLoan(systemUser)){
                return ResponseEntity.unprocessableEntity().body("Sorry we can't process your request at this time, You have an unpaid loan");
            }

            return ResponseEntity.ok(fuelLoanService.requestFuelLoan(fuelLoan,principal.getName()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.unprocessableEntity().build();
        }
    }

    @GetMapping("/requests/paid")
    ResponseEntity<List<FuelLoan>> getPaidFuelLoanRequests(){

        try {
            return ResponseEntity.ok(fuelLoanService.getPaidFuelLoanRequests());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.unprocessableEntity().build();
        }
    }

    @GetMapping("/requests/unpaid")
    ResponseEntity<List<FuelLoan>> getUnPaidFuelLoanRequests(){

        try {
            return ResponseEntity.ok(fuelLoanService.getUnpaidPaidFuelLoanRequests());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.unprocessableEntity().build();
        }
    }

    @GetMapping("/requests/paid/partners/{partnerId}")
    ResponseEntity<List<FuelLoan>> getPartnerPaidFuelLoanRequests(@PathVariable int partnerId){

        try {
            return ResponseEntity.ok(fuelLoanService.getPartnerPaidFuelLoanRequests(partnerId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.unprocessableEntity().build();
        }
    }

    @GetMapping("/requests/unpaid/partners/{partnerId}")
    ResponseEntity<List<FuelLoan>> getPartnerUnPaidFuelLoanRequests(@PathVariable int partnerId){

        try {
            return ResponseEntity.ok(fuelLoanService.getPartnerUnpaidPaidFuelLoanRequests(partnerId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.unprocessableEntity().build();
        }
    }

    @GetMapping("/requests/user")
    ResponseEntity<List<FuelLoan>> getFuelLoanRequestsForUser(Principal principal){

        try {
            return ResponseEntity.ok(fuelLoanService.getExistingLoanForUser(principal.getName()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.unprocessableEntity().build();
        }
    }

    @GetMapping("/loans_per_user")
    ResponseEntity<List<LoanPerUser>> getLoansPerUser(){
        try{
            return ResponseEntity.ok(fuelLoanService.getLoansPerUser());
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.unprocessableEntity().build();
        }

    }

}
