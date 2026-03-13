package com.otblabs.jiinueboda.accounting.expenses;

import com.otblabs.jiinueboda.accounting.expenses.models.*;
import com.otblabs.jiinueboda.integrations.momo.mpesa.MpesaTransactionsService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class ExpensesService {
    private final JdbcTemplate jdbcTemplateOne;
    private final MpesaTransactionsService mpesaTransactionsService;

    public ExpensesService(JdbcTemplate jdbcTemplateOne, MpesaTransactionsService mpesaTransactionsService) {
        this.jdbcTemplateOne = jdbcTemplateOne;
        this.mpesaTransactionsService = mpesaTransactionsService;
    }

    public List<PendingExpense> getAllExpenses() {

        String sql = """
                 SELECT id, description, reciever_type, reciever, account_number, amount, created_at FROM temp_expense_requests 
                 WHERE status='APPROVED' AND deleted_at is null ORDER BY created_at DESC
                """;

        return jdbcTemplateOne.query(sql,(rs,i)->setPendingExpenses(rs));
    }

    public List<PendingExpense> getAllExpenses(String startDate, String endDate) {


        String sql = """
                  SELECT id,description, reciever_type, reciever, account_number, amount, created_at,
                                                           (SELECT category_name FROM expense_categories_2 WHERE id= main_category_id) as main_category,
                                                           (SELECT subcategory_name FROM expense_subcategories_2 WHERE id= subcategory_id) as sub_category,
                                                           (SELECT minor_subcategory_name FROM expense_minor_subcategories WHERE id= minor_subcategory_id) as minor_subcategory,
                                                           (SELECT transaction_id FROM mpesa_b2c WHERE occasion = CONCAT('EXPENSE',temp_expense_requests.id) AND result_code = 0  LIMIT 1) as mpesa_refference
                                                    FROM temp_expense_requests
                                                    WHERE status='APPROVED' AND  DATE(created_at) BETWEEN DATE('2026-02-01') AND DATE('2026-02-28')
                                                      AND deleted_at is null ORDER BY created_at DESC
                """;

        return jdbcTemplateOne.query(sql,(rs,i)->setPendingExpenses(rs),startDate,endDate);
    }

    public int createExpense(CreateExpense createExpense) throws Exception{

        String createExpenseSql = """
                INSERT INTO temp_expense_requests(main_category_id,subcategory_id,minor_subcategory_id, description, reciever_type, reciever, account_number, amount,status,created_at)
                                VALUES(?,?,?,?,?,?,?,?,'PENDING_APPROVAL',NOW())
                """;

        return jdbcTemplateOne.update(createExpenseSql,
                createExpense.getMainMainCategoryId(),
                createExpense.getSubCategoryId(),
                createExpense.getMinorSubcategoryId(),
                createExpense.getDescription(),
                createExpense.getRecieverType().name(),
                createExpense.getReciever(),
                createExpense.getAccountNumber(),
                createExpense.getAmount());
    }

    public List<PendingExpense> getAllPendingExpenses() throws Exception{
        String sql = """
        SELECT id, description, reciever_type, reciever, account_number, amount, created_at
        FROM temp_expense_requests 
        WHERE status='PENDING_APPROVAL'
        """;
        return jdbcTemplateOne.query(sql, (rs, i) -> setPendingExpenses(rs));
    }

    private PendingExpense setPendingExpenses(ResultSet rs) throws SQLException {
        PendingExpense pendingExpense = new PendingExpense();
        pendingExpense.setId(rs.getInt("id"));
        pendingExpense.setDescription(rs.getString("description"));
        pendingExpense.setReciever(rs.getString("reciever"));
        pendingExpense.setAccountNumber(rs.getString("account_number"));
        pendingExpense.setAmount(rs.getInt("amount"));
        pendingExpense.setCreatedAt(rs.getString("created_at"));

        try{
            pendingExpense.setMpesaRefferenceCode(rs.getString("mpesa_refference"));
        }catch (Exception ignored){}


        try {

            // Handle enum conversion for reciever_type
            String receiverTypeStr = rs.getString("reciever_type");
            if (receiverTypeStr != null) {
                try {
                    RecieverType receiverType = RecieverType.valueOf(receiverTypeStr);
                    pendingExpense.setRecieverType(receiverType);
                } catch (IllegalArgumentException e) {
                    // Log the error and set to null or default value
                    System.err.println("Invalid receiver type: " + receiverTypeStr);
                    pendingExpense.setRecieverType(null);
                }
            }

        } catch (SQLException e) {
            throw new SQLException("Error mapping PendingExpense from ResultSet", e);
        }

        try{
            pendingExpense.setMainCategory(rs.getString("main_category"));
            pendingExpense.setSubCategory(rs.getString("sub_category"));
            pendingExpense.setMinorSubCategory(rs.getString("minor_subcategory"));
        }catch (Exception ignored){}

        return pendingExpense;
    }

    public List<ExpenseCategory> getAllExpenseTypes() {

        String sql = """
                SELECT category_id,category_name,description FROM expense_categories
                """;
        return jdbcTemplateOne.query(sql,(rs,i)->{
            ExpenseCategory expenseCategory = new ExpenseCategory();
            expenseCategory.setId(rs.getInt("category_id"));
            expenseCategory.setName(rs.getString("category_name"));
            expenseCategory.setDescription(rs.getString("description"));
            return expenseCategory;
        });
    }

    @Transactional
    public boolean approvePendingExpenses(PendingExpense pendingExpense, boolean approvalStatus, int userId) throws Exception{

        String currentStatus = jdbcTemplateOne.queryForObject("SELECT status FROM temp_expense_requests WHERE id=?",(rs,i)-> rs.getString("status"), pendingExpense.getId());

        if(!"PENDING_APPROVAL".equals(currentStatus)){
            throw  new RuntimeException("Transaction already approved/rejected");
        }

        updatePendingStatus(approvalStatus,userId,pendingExpense.getId());

        if(!approvalStatus){
            return false;
        }

        if(pendingExpense.getRecieverType() == RecieverType.PHONENUMBER){
            mpesaTransactionsService.sendMoney(String.valueOf(pendingExpense.getAmount()),pendingExpense.getReciever(),"EXPENSE"+pendingExpense.getId());

        }else if(pendingExpense.getRecieverType() == RecieverType.MPESA_PAYBILL){
            mpesaTransactionsService.payToPaybill(pendingExpense.getAmount(),pendingExpense.getReciever(),pendingExpense.getAccountNumber(),"EXPENSE"+pendingExpense.getId());

        }else if(pendingExpense.getRecieverType() == RecieverType.MPESA_TILL){
            mpesaTransactionsService.payToTill(pendingExpense.getAmount(),pendingExpense.getReciever(),"EXPENSE"+pendingExpense.getId());
        }else {
            System.out.println("N/A");
        }
        return true;
    }

    private void updatePendingStatus(boolean approvalStatus,int userID,int expenseId) throws Exception {
        String sql;
        if(approvalStatus){
            sql =  "UPDATE temp_expense_requests  SET disburse_initiated=1, approval_status=1, approved_by=?,status='APPROVED' WHERE id=?";
        }else{
            sql = "UPDATE temp_expense_requests SET disburse_initiated=0, approval_status=0, approved_by=?,status='REJECTED' where id=?";
        }
        jdbcTemplateOne.update(sql,userID,expenseId);
    }
}

