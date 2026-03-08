package com.otblabs.jiinueboda.accounting.hr;

import com.otblabs.jiinueboda.integrations.momo.mpesa.MpesaTransactionsService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Service
public class PayrollService {
    private final JdbcTemplate jdbcTemplateOne;
    private final MpesaTransactionsService mpesaTransactionsService;

    public PayrollService(JdbcTemplate jdbcTemplateOne, MpesaTransactionsService mpesaTransactionsService) {
        this.jdbcTemplateOne = jdbcTemplateOne;
        this.mpesaTransactionsService = mpesaTransactionsService;
    }



    // 1. Add Employee to Payroll
    public void addEmployeeToPayroll(Long userId, BigDecimal baseSalary) {
        String sql = "INSERT INTO payroll (user_id, base_salary, payment_status) " +
                "VALUES (?, ?, 'Pending') ON DUPLICATE KEY UPDATE base_salary = VALUES(base_salary)";
        jdbcTemplateOne.update(sql, userId, baseSalary);
    }

    // 2. Record Salary Advance
    public void recordSalaryAdvance(Long userId, BigDecimal amount) {
        String sql = "INSERT INTO salary_advances (user_id, amount, advance_date) VALUES (?, ?, CURDATE())";
        jdbcTemplateOne.update(sql, userId, amount);
    }



    // 4. Process Monthly Salary Payment
    public void processMonthlySalariesForFullTimeEmployees() {

//        step 1 get all salaries where amount to be paid is more than 0
        List<Employee> employeesList = getAllPayrollRecords().stream().filter(employee -> (employee.getSalary() - employee.getAdvancesThisMonth()) > 0).toList();

//      insert salary to salary payments
        employeesList.forEach(employee -> {

          String sql = "INSERT INTO salary_payments (user_id, month, salary_paid, payment_status, payment_date) VALUES(?,MONTH(CURDATE()),?,?,NOW())";

          double paidAmount = employee.getSalary() - employee.getAdvancesThisMonth();

          String paymentStatus = null;
            if(paidAmount + employee.getAdvancesThisMonth() == employee.getSalary()){
                paymentStatus = "Fully Paid";
            }else{
                paymentStatus = "Partially Paid";
            }

            System.out.println("proccessing salary for "+ employee.getFullName() +
                    " of ID "+employee.getUserId()+
                    " Total Amount sent = "+paidAmount +
                    " which is their salary in "+paymentStatus);

            System.out.println("===============================================================================");

           //insert salary to salary_payments db
          jdbcTemplateOne.update(sql,employee.getUserId(),paidAmount,paymentStatus);

//          update last payment date on payroll
            String updateSLPDSql = "UPDATE payroll SET last_payment_date = NOW() WHERE user_id =?";
            jdbcTemplateOne.update(updateSLPDSql,employee.getUserId());

//          //send actual money
          mpesaTransactionsService.sendMoney(String.valueOf(paidAmount),employee.getMpesaAccount(), "Monthly wage");

        });
    }

//    public void processMonthlySalariesForDirectors() {
//
////        step 1 get all salaries where amount to be paid is more than 0
//        List<Employee> employeesList = getAllPayrollRecords().stream().filter(employee -> (employee.getSalary() - employee.getAdvancesThisMonth()) > 0).toList();
//
//        Employee employee = new Employee();
//        employee.setUserId(2);
//        employee.setSalary(50_000);
//        employee.se
//
////      insert salary to salary payments
//        employeesList.forEach(employee -> {
//
//            String sql = "INSERT INTO salary_payments (user_id, month, salary_paid, payment_status, payment_date) VALUES(?,MONTH(CURDATE()),?,?,NOW())";
//
//            double paidAmount = employee.getSalary() - employee.getAdvancesThisMonth();
//
//            String paymentStatus = null;
//            if(paidAmount + employee.getAdvancesThisMonth() == employee.getSalary()){
//                paymentStatus = "Fully Paid";
//            }else{
//                paymentStatus = "Partially Paid";
//            }
//
//            System.out.println("proccessing salary for "+ employee.getFullName() +
//                    " of ID "+employee.getUserId()+
//                    " Total Amount sent = "+paidAmount +
//                    " which is their salary in "+paymentStatus);
//
//            System.out.println("===============================================================================");
//
//            //insert salary to salary_payments db
//            jdbcTemplateOne.update(sql,employee.getUserId(),paidAmount,paymentStatus);
//
////          update last payment date on payroll
//            String updateSLPDSql = "UPDATE payroll SET last_payment_date = NOW() WHERE user_id =?";
//            jdbcTemplateOne.update(updateSLPDSql,employee.getUserId());
//
////          //send actual money
//            mpesaTransactionsService.sendMoney(String.valueOf(paidAmount),employee.getMpesaAccount(), "Monthly wage");
//
//        });
//    }

    // 5. Record Salary Withdrawal
    public void recordSalaryWithdrawal(Long userId, BigDecimal amount) {
        String sql = "INSERT INTO salary_withdrawals (user_id, amount, withdrawal_date) VALUES (?, ?, CURDATE())";
        jdbcTemplateOne.update(sql, userId, amount);

        String updateSql = "UPDATE salary_payments SET salary_paid = salary_paid - ? " +
                "WHERE user_id = ? AND month = DATE_FORMAT(CURDATE(), '%Y-%m')";
        jdbcTemplateOne.update(updateSql, amount, userId);
    }

    // 6. Get Salary Payment Trend for the Last 6 Months
    public List<Map<String, Object>> getSalaryPaymentTrend(Long userId) {
        String sql = "SELECT month, salary_paid, payment_status, payment_date " +
                "FROM salary_payments WHERE user_id = ? " +
                "AND month >= DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 6 MONTH), '%Y-%m') " +
                "ORDER BY month DESC";
        return jdbcTemplateOne.queryForList(sql, userId);
    }

    public List<Employee> getAllPayrollRecords() {
        String sql = """
                        SELECT p.user_id,p.mpesa_account, p.bank_account, u.first_name,u.last_name, p.base_salary, (select sum(amount) from salary_advances
                                                WHERE user_id = p.user_id AND MONTH(DATE(advance_date) = MONTH(CURDATE()))) as advance_total,last_payment_date
                                                FROM payroll p LEFT JOIN users u on p.user_id = u.id WHERE p.deleted_at is null ORDER BY u.created_at
                    """;
        return jdbcTemplateOne.query(sql,(rs,i)->employeePayrollRowMapper(rs));
    }

    private Employee employeePayrollRowMapper(ResultSet rs) throws SQLException {
        Employee employee = new Employee();
        employee.setUserId(rs.getInt("user_id"));
        employee.setFullName(rs.getString("first_name") +" "+ rs.getString("last_name"));
        employee.setMpesaAccount(rs.getString("mpesa_account"));
        employee.setBankAccount(rs.getString("bank_account"));
        employee.setSalary(rs.getDouble("base_salary"));
        employee.setAdvancesThisMonth(rs.getDouble("advance_total"));
//        employee.setOverdraftThisMonth(rs.getDouble(""));
        employee.setLastPaymentDate(rs.getString("last_payment_date"));
        return employee;
    }

}
