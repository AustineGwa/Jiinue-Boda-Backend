package com.otblabs.jiinueboda.accounting.hr;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/accounts/payroll")
public class PayrollController {
    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }


    @GetMapping
    public ResponseEntity<List<Employee>> getAllPayrollRecords() {

        try{
            return ResponseEntity.ok(payrollService.getAllPayrollRecords());
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.unprocessableEntity().build();
        }

    }


    @PostMapping("/add-employee")
    public ResponseEntity<String> addEmployeeToPayroll(@RequestParam Long userId, @RequestParam BigDecimal baseSalary) {
        payrollService.addEmployeeToPayroll(userId, baseSalary);
        return ResponseEntity.ok("Employee added to payroll successfully.");
    }

    // 2. Record Salary Advance
    @PostMapping("/advance")
    public ResponseEntity<String> recordSalaryAdvance(@RequestParam Long userId, @RequestParam BigDecimal amount) {
        payrollService.recordSalaryAdvance(userId, amount);
        return ResponseEntity.ok("Salary advance recorded successfully.");
    }


    // 5. Record Salary Withdrawal
    @PostMapping("/withdraw")
    public ResponseEntity<String> recordSalaryWithdrawal(@RequestParam Long userId, @RequestParam BigDecimal amount) {
        payrollService.recordSalaryWithdrawal(userId, amount);
        return ResponseEntity.ok("Salary withdrawal recorded successfully.");
    }

    // 6. Get Salary Payment Trend for the Last 6 Months
    @GetMapping("/trend")
    public ResponseEntity<List<Map<String, Object>>> getSalaryPaymentTrend(@RequestParam Long userId) {
        List<Map<String, Object>> salaryTrend = payrollService.getSalaryPaymentTrend(userId);
        return ResponseEntity.ok(salaryTrend);
    }
}

