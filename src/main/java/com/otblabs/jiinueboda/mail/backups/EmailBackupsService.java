package com.otblabs.jiinueboda.mail.backups;

import com.otblabs.jiinueboda.accounting.expenses.ExpensesService;
import com.otblabs.jiinueboda.accounting.expenses.models.PendingExpense;
import com.otblabs.jiinueboda.loans.LoanManagementService;
import com.otblabs.jiinueboda.loans.models.PendingLoanData;
import com.otblabs.jiinueboda.mail.EmailService;
import com.otblabs.jiinueboda.utility.CSVConverter;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailBackupsService {

    private final LoanManagementService loanManagementService;
    private final EmailService emailService;
    private final CSVConverter csvConverter;
    private final ExpensesService expensesService;
    private final TransactionsBackupService transactionsBackupService;

    public EmailBackupsService(LoanManagementService loanManagementService, EmailService emailService, CSVConverter csvConverter, ExpensesService expensesService, TransactionsBackupService transactionsBackupService) {
        this.loanManagementService = loanManagementService;
        this.emailService = emailService;
        this.csvConverter = csvConverter;
        this.expensesService = expensesService;
        this.transactionsBackupService = transactionsBackupService;
    }

    public void sendDailyLoanBookBackup(){
        try {
            List<PendingLoanData> allLoans = loanManagementService.getAllSystemLoanBalances(0);

            String csvContent = csvConverter.convertToCSV(allLoans);
            // Send email with CSV attachment
            emailService.sendEmailWithCSVAttachment(
                    csvContent,
                    "Daily_Loan_Backup.csv",
                    "bongacomlimited@gmail.com",
                    "Daily loan Book Backup"
            );


        } catch (Exception e) {
            System.err.println("Error occurred: " + e.getClass().getName());
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void sendDailyExpenses() {
        try {
            List<PendingExpense> allExpenses = expensesService.getAllExpenses();
            String csvContent = csvConverter.convertToCSV(allExpenses);
            // Send email with CSV attachment
            emailService.sendEmailWithCSVAttachment(
                    csvContent,
                    "Daily_Expenses_Backup.csv",
                    "bongacomlimited@gmail.com",
                    "Daily Expenses Backup"
            );


        } catch (Exception e) {
            System.err.println("Error occurred: " + e.getClass().getName());
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendDailyDisbursements() {
        try {
            List<TransactionResult> allDisbursements = transactionsBackupService.getAllTransactions() ;

            String csvContent = csvConverter.convertToCSV(allDisbursements);
            // Send email with CSV attachment
            emailService.sendEmailWithCSVAttachment(
                    csvContent,
                    "Daily_Disbursements_Backup.csv",
                     "bongacomlimited@gmail.com",
                     "Daily Disbursements Backup"
            );


        } catch (Exception e) {
            System.err.println("Error occurred: " + e.getClass().getName());
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
