package com.otblabs.jiinueboda.loans;

import com.otblabs.jiinueboda.collections.models.SpecialCaseLoan;
import com.otblabs.jiinueboda.filemanagement.FileManagementService;
import com.otblabs.jiinueboda.loans.models.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/loans")
public class LoansManagementController {

    private final LoanManagementService loanManagementService;
    private final LendingService lendingService;
    private final FileManagementService fileManagementService;


    public LoansManagementController(LoanManagementService loanManagementService,LendingService lendingService, FileManagementService fileManagementService) {
        this.loanManagementService = loanManagementService;
        this.lendingService = lendingService;
        this.fileManagementService = fileManagementService;
    }



    @PostMapping(value = "/request/new", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<Object> createNewLoanRequest(@ModelAttribute NewLoanRequest newLoanRequest, Principal principal){

        //check if there is a valid client asset and that the asset belongs to this client
        if(newLoanRequest.getClientAsset() == 0){
            return ResponseEntity.unprocessableEntity().body("There is no valid asset attached");
        }

        //check if there is a valid loan aggreement
        if(newLoanRequest.getLoanAgreementForm() == null){
            return ResponseEntity.unprocessableEntity().body("There is no valid loan agreement found");
        }

//        //check for availability of both 2 guarantors
//        if(newLoanRequest.getGuarantor1Phone() == null || newLoanRequest.getGuarantor2Phone() == null){
//            return ResponseEntity.unprocessableEntity().body("Please fill all guarantors");
//        }

        //check loan purpose
        if(newLoanRequest.getLoanPurpose().equals("")){
            return ResponseEntity.unprocessableEntity().body("Please fill a loan purpose");
        }

        try{

            int loanAgreementId = fileManagementService.uploadLoanAgreement(newLoanRequest.getLoanAgreementForm(),"LOAN_AGREEMENT", newLoanRequest.getUserId());

            return   ResponseEntity.ok(lendingService.createNewLoanRequest(newLoanRequest,loanAgreementId, principal.getName()));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/loan-restructure")
    ResponseEntity<Object> restructureLoans(@RequestBody LoanRestructureData loanRestructureData){

        try{
            if(!lendingService.checkRestructureEligibility(loanRestructureData)){
                return ResponseEntity.unprocessableEntity().body("Client must fully pay the loan within the loan term to be adjusted");
            }

            return ResponseEntity.ok(lendingService.restructureLoan(loanRestructureData));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/approval/approve-loan")
    ResponseEntity<Object> approveLoans(Principal principal, @RequestBody LoanApprovalRequest loanApprovalRequest){
        try{
            return ResponseEntity.ok(lendingService.updateLoanStatus(principal.getName(), loanApprovalRequest));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @RequestMapping("/user-profile/{userId}")
    ResponseEntity<List<UserProfileLoanData>> getAllLoansForUser(@PathVariable int userId){
        try{
            return   ResponseEntity.ok(loanManagementService.getAllLoansForUser(userId));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

    @GetMapping("/transactions/statement/{loanId}")
    ResponseEntity<List<LoanStatement>> getLoanStatementsForLoan(@PathVariable String loanId){
        try{
            return   ResponseEntity.ok(loanManagementService.getLoanStatementsForLoan(loanId));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

    @GetMapping("/statement/profile-info/{loanId}")
    ResponseEntity<LoanStatementProfileInfo>  getLoanStatementProfileInfo(@PathVariable String loanId){
        try{
            return ResponseEntity.ok(loanManagementService.getLoanStatementProfileInfo(loanId));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

    @GetMapping("/first-time/month/{month}/branch/{branch}")
    ResponseEntity<Object> getMonthlyFirstTimeLoans(@PathVariable int month, @PathVariable int branch){
        try{
            return ResponseEntity.ok(loanManagementService.getMonthlyFirstTimeLoans(month,branch));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/loan-history/{userId}")
    ResponseEntity <ClientLoanData> getClientLoanData(@PathVariable int userId){
        try{
            return ResponseEntity.ok(loanManagementService.getClientLoanData(userId));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();

        }
    }

    @GetMapping("/active/balances-tracker/{branch}")
    public ResponseEntity<List<PendingLoanData>> getActiveLoansBalances(@PathVariable int branch){
        try{
            return ResponseEntity.ok(loanManagementService.getActiveLoansBalances(branch));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/overdue/balances-tracker")
    public ResponseEntity<List<PendingLoanData>> getAllOverdueLoansBalances(){
        try{
            return ResponseEntity.ok(loanManagementService.getAllOverdueLoans());
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/active/profile/balances-tracker/{loanAccount}")
    public ResponseEntity<PendingLoanData> getActiveLoanBalanceDetails(@PathVariable String loanAccount){
        try{
            return ResponseEntity.ok(loanManagementService.getActiveLoanBalanceDetails(loanAccount));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/in-active/balances-tracker/{branch}")
    public ResponseEntity<List<PendingLoanData>> getInActiveLoansBalances(@PathVariable int branch){
        try{
            return ResponseEntity.ok(loanManagementService.getInActiveLoansBalances(branch));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/all/balances-tracker/{branch}")
    public ResponseEntity<List<PendingLoanData>> getAllSystemLoanBalances(@PathVariable int branch){
        try{
            return ResponseEntity.ok(loanManagementService.getAllSystemLoanBalances(branch));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/approval/all-pending-loans")
    ResponseEntity<List<PendingDisbursement>> getAllPendingDisburesments(){
        try{
            return ResponseEntity.ok(loanManagementService.getAllPendingDisburesments());
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/approval/failed/all-pending-loans")
    ResponseEntity<List<PendingDisbursement>> getAllFailedDisburesments(){
        try{
            return ResponseEntity.ok(loanManagementService.getAllFailedDisburesments());
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/report-special-case")
    ResponseEntity<Object> reportSpecialCase(@RequestBody SpecialCaseLoan specialCaseLoan, Principal principal){

        try{
            return ResponseEntity.ok(loanManagementService.reportSpecialCase(specialCaseLoan, principal.getName()));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/report-red-flag")
    ResponseEntity<Object> reportRedFlag(@RequestBody SpecialCaseLoan specialCaseLoan, Principal principal){

        try{
            return ResponseEntity.ok(loanManagementService.reportRedFlag(specialCaseLoan, principal.getName()));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
