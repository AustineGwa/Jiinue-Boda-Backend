package com.otblabs.jiinueboda.collections;

import com.otblabs.jiinueboda.dashboard.models.DailyPaymentTracker;
import com.otblabs.jiinueboda.collections.models.MonthlyGeneralPerformance;
import com.otblabs.jiinueboda.dashboard.models.MonthlyPerformance;
import com.otblabs.jiinueboda.collections.models.*;
import com.otblabs.jiinueboda.integrations.momo.mpesa.MpesaTransactionsService;
import com.otblabs.jiinueboda.loans.models.OldAccountNewAccountPayment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/collections")
public class CollectionsController {

    private final CollectionsService collectionsService;

    public CollectionsController(CollectionsService collectionsService) {
        this.collectionsService = collectionsService;
    }

    @GetMapping("/top/{num}")
    ResponseEntity<List<UserTransaction>> getTopTransactions(@PathVariable int num){
        try{
            List<UserTransaction> transactionList = collectionsService.getTopTransactions(num);
            return ResponseEntity.ok(transactionList);
        }catch(Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }
    @GetMapping("/single/{transID}")
    ResponseEntity<UserTransaction> getSingleTransaction(@PathVariable String transID){
        try{
            UserTransaction transactionList = collectionsService.getSingleTransaction(transID);
            return ResponseEntity.ok(transactionList);
        }catch(Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

    @GetMapping ("/loans/hourly-collection/{date}")
    ResponseEntity<Object> getAllCollectionsPerhour(@PathVariable String date){
        try{
            List<HourlyCollection> hourlyCollection = collectionsService.getHourlyCollectionsPerDay(date);
            return ResponseEntity.ok(hourlyCollection);
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }




    @GetMapping ("/loans/disbursments/{month}/{year}")
    ResponseEntity<List<MonthlyPerformance>> getperformanceForMonthlyDisbursements(@PathVariable int month, @PathVariable int year){
        try{
            List<MonthlyPerformance> monthlyPerformances = collectionsService.getMonthlyperformance(month,year);
            return ResponseEntity.ok(monthlyPerformances);
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping ("/loans/loans-by-age/{branch}")
    ResponseEntity<Object> getAllLoansByAge(@PathVariable int branch){
        try{
            Map<String,List<LoansByAge>> loansByAgeMap = collectionsService.getLoansByLoanAge(branch);
            return ResponseEntity.ok(loansByAgeMap);
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping ("/loans/loans-by-varRatio/{branch}/{collectionStage}")
    ResponseEntity<Object> getAllLoansByVarianceRation(@PathVariable int branch,@PathVariable int collectionStage){

        try{
            Map<String,List<LoansByAge>> loansByAgeMap = collectionsService.getAllLoansByVarianceRation(branch,collectionStage);
            return ResponseEntity.ok(loansByAgeMap);
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping ("/loans/collection-by-age/{date}")
    ResponseEntity<Object> getCollectionByLoanAgeGroup(@PathVariable String date){
        try{
            List<CollectionByAge> collectionByAgeList = collectionsService.getCollectionByLoanAgeGroup(date);
            return ResponseEntity.ok(collectionByAgeList);
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/loans/defaults/days-till-overdue/{daysTillOverdue}")
    ResponseEntity<List<DefaultList>> getDefaultsWithCloseOverdue(@PathVariable int daysTillOverdue){
        try{
            List<DefaultList> defaulters = collectionsService.getDefaultsWithCloseOverdue(daysTillOverdue);
            return ResponseEntity.ok(defaulters);
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }



    @GetMapping("/loans/defaults")
    ResponseEntity<List<DefaultList>> getPossibleDefaults(){
        try{
            List<DefaultList> defaulters = collectionsService.getDefaultsList();
            return ResponseEntity.ok(defaulters);
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping ("/loans/bad-loans/{branch}")
    ResponseEntity<Object> getAllSpecialCasesLoans(@PathVariable int branch){
        try{
            return ResponseEntity.ok(collectionsService.getAllBadLoans(branch));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping ("/loans/red-flags/{branch}")
    ResponseEntity<Object> getAllRedFlags(@PathVariable int branch){
        try{
            return ResponseEntity.ok(collectionsService.getAllRedFlags(branch));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /*
    add link for requested recoveries
     */

    @GetMapping("/loans/partners/{partnerId}")
    ResponseEntity<List<DefaultList>> getBranchLoans(@PathVariable int partnerId){

        try{
            List<DefaultList> defaulters = collectionsService.getActiveLoanDetailsForPartner(partnerId);
            return ResponseEntity.ok(defaulters);
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }



    @PostMapping("/priority-profile-list")
    ResponseEntity<List<PriorityProfile>> getPriorityList(@RequestBody PriorityProfileFilter priorityProfileFilter){

        try{
            return ResponseEntity.ok(collectionsService.getPriorityList(priorityProfileFilter));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/daily-payment-tracker/{loanAccount}")
    ResponseEntity<List<DailyPaymentTracker>> getDailyPaymentTracker(@PathVariable String loanAccount){

        try{
            return ResponseEntity.ok(collectionsService.getDailyPaymentTracker(loanAccount));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }



    @GetMapping("/monthly-general-performance")
    ResponseEntity<MonthlyGeneralPerformance> getMonthlyGeneralPerformance(){

        try{
            return ResponseEntity.ok(collectionsService.getMonthlyGeneralPerformance());
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/get-par/{parDays}")
    ResponseEntity<ParHolder> getPAR(@PathVariable int parDays){

        try{
            return ResponseEntity.ok(collectionsService.getPAR(parDays));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/get-collection-performance/{cutOffdate}")
    ResponseEntity<Object> getCoolectionPerformanceMoM(@PathVariable String cutOffdate){

        try{
            return ResponseEntity.ok(collectionsService.getColectionPerformanceMoM(cutOffdate));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/get-callcentre-performance/{repId}/{startDate}/{endDate}")
    ResponseEntity<Object> getCallcentrePerformance(@PathVariable int repId, @PathVariable String startDate, @PathVariable String endDate ){

        try{
            return ResponseEntity.ok(collectionsService.getCallcentrePerformance(repId,startDate,endDate));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
