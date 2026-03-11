package com.otblabs.jiinueboda.crons;

import com.otblabs.jiinueboda.accounting.expenses.ExpensesService;
import com.otblabs.jiinueboda.accounting.expenses.models.CreateExpense;
import com.otblabs.jiinueboda.accounting.expenses.models.RecieverType;
import org.springframework.stereotype.Component;

@Component
public class InvestorsCronJobs {

    private final ExpensesService expensesService;

    public InvestorsCronJobs(ExpensesService expensesService) {
        this.expensesService = expensesService;
    }

    void sendSogomoMonthlyInterest(){
        sendToInvestor("542542","03405413296150");
    }

    void sendToInvestor(String paybill, String accountNumber){

        CreateExpense createExpense = new CreateExpense();
        createExpense.setAmount(16700);
        createExpense.setMainMainCategoryId(2);
        createExpense.setSubCategoryId(14);
        createExpense.setMinorSubcategoryId(19);
        createExpense.setDescription("investor monthly interest");
        createExpense.setRecieverType(RecieverType.valueOf("MPESA_PAYBILL"));
        createExpense.setReciever(paybill); //change
        createExpense.setAccountNumber(accountNumber);


        try {
            expensesService.createExpense(createExpense);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
