package com.otblabs.jiinueboda.accounting.expenses;

import com.otblabs.jiinueboda.accounting.expenses.models.CreateExpense;
import com.otblabs.jiinueboda.accounting.expenses.models.PendingExpense;
import com.otblabs.jiinueboda.auth.LoggedInUser;
import com.otblabs.jiinueboda.users.UserService;
import com.otblabs.jiinueboda.users.models.SystemUser;
import com.otblabs.jiinueboda.users.models.Usertype;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/expenses")
public class ExpensesController {

    private final ExpensesService expensesService;
    private final UserService userService;

    public ExpensesController(ExpensesService expensesService, UserService userService) {
        this.expensesService = expensesService;
        this.userService = userService;
    }

    @GetMapping("/all/expense-types")
    public ResponseEntity<Object> getAllExpenseTypes(){
        try {
            return  ResponseEntity.ok(expensesService.getAllExpenseTypes());
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.unprocessableEntity().build();
        }
    }


    @PostMapping("/create-new")
    public ResponseEntity<Object> createExpense(@RequestBody CreateExpense createExpense) {
        try {
            return  ResponseEntity.ok(expensesService.createExpense(createExpense));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.unprocessableEntity().build();
        }
    }

    @PostMapping("/update/{expenseID}")
    public ResponseEntity<Object> updateExpenseDetails(@RequestBody CreateExpense createExpense, @PathVariable int expenseID) {
        try {
            return  ResponseEntity.ok(expensesService.updateExpenseDetails(createExpense, expenseID));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.unprocessableEntity().build();
        }
    }

    @PostMapping("/update/category/{expenseID}")
    public ResponseEntity<Object> updateExpenseCategory(@RequestBody CreateExpense createExpense, @PathVariable int expenseID) {
        try {
            return  ResponseEntity.ok(expensesService.updateExpenseCategory(createExpense, expenseID));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.unprocessableEntity().build();
        }
    }

    @GetMapping("/all/{startDate}/{endDate}")
    public ResponseEntity<Object> getAllExpenses(@PathVariable String startDate, @PathVariable String endDate){
        try {
            return  ResponseEntity.ok(expensesService.getAllExpenses(startDate,endDate));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.unprocessableEntity().build();
        }
    }

    @GetMapping("/pending-new")
    public ResponseEntity<List<PendingExpense>> getAllPendingExpenses() {
        try {
            return  ResponseEntity.ok(expensesService.getAllPendingExpenses());
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.unprocessableEntity().build();
        }
    }

    @PostMapping("/approve-new/{approvalStatus}")
    public ResponseEntity<Boolean> approvePendingExpenses(@RequestBody PendingExpense pendingExpense, @PathVariable boolean approvalStatus,Principal principal) {

        LoggedInUser loggedInUser = userService.getLoggedInUser(principal.getName());

        if (loggedInUser.getUsertype() != Usertype.Admin) {
            throw new RuntimeException("Operation not permitted for this user");
        }

        if( loggedInUser.getAprovalLevel() == 0){
            throw new RuntimeException("Operation not permitted for this user");
        }

        try {
            return  ResponseEntity.ok(expensesService.approvePendingExpenses(pendingExpense,approvalStatus,loggedInUser.getId()));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.unprocessableEntity().build();
        }
    }


}
