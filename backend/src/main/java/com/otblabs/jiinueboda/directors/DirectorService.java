package com.otblabs.jiinueboda.directors;

import com.otblabs.jiinueboda.accounting.expenses.ExpensesService;
import com.otblabs.jiinueboda.integrations.momo.mpesa.MpesaTransactionsService;
import com.otblabs.jiinueboda.integrations.momo.mpesa.b2c.models.B2CRequest;
import com.otblabs.jiinueboda.integrations.momo.mpesa.b2c.models.B2CRequestResponse;
import com.otblabs.jiinueboda.integrations.momo.mpesa.b2c.models.MpesaCommandId;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Service
public class DirectorService {

    private final JdbcTemplate jdbcTemplateOne;
    private final ExpensesService expensesService;
    private final MpesaTransactionsService mpesaTransactionsService;

    public DirectorService(JdbcTemplate jdbcTemplateOne, ExpensesService expensesService, MpesaTransactionsService mpesaTransactionsService) {
        this.jdbcTemplateOne = jdbcTemplateOne;
        this.expensesService = expensesService;
        this.mpesaTransactionsService = mpesaTransactionsService;
    }

    public void sendParentsPackage(){
        getDirectorsNextOfKin().forEach(director ->{
            System.out.println("Send to "+director.getUserName());
            sendToDad(director);
            sendToMum(director);
        });
    }

    public void sendPersonalPackage(){
        getDirectorsNextOfKin().forEach(director ->{
            System.out.println("Send to "+director.getUserName());
            sendToSelf(director);
        });
    }

    private void sendToSelf(DirectorNextOfKin director) {
        int rowId = createSelfExpenseEntry(director.getPhone(),5000);
    }

    private void sendToMum(DirectorNextOfKin director) {
        int rowId = createExpenseEntry(director.getMumPhone(),director.getMumWeeklyPay());
        sendMoney(String.valueOf(director.getMumWeeklyPay()),director.getMumPhone(),"EXPENSE"+rowId);
    }

    private int createSelfExpenseEntry(String phone, int amount) {
        String createExpenseSql = """
            INSERT INTO temp_expense_requests(main_category_id,subcategory_id, minor_subcategory_id, description, reciever_type, reciever,amount,status,created_at)
            VALUES(2,8,1,'Weekly Petty Cash','PHONENUMBER',?,?,'PENDING_APPROVAL',NOW())
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplateOne.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(createExpenseSql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, phone);
            ps.setInt(2, amount);
            return ps;
        }, keyHolder);

        return keyHolder.getKey().intValue();
    }

    private int createExpenseEntry(String phone, int amount) {
        String createExpenseSql = """
            INSERT INTO temp_expense_requests(main_category_id,subcategory_id, minor_subcategory_id, description, reciever_type, reciever,amount,status,created_at)
            VALUES(2,8,2,'Parents Weekly Pay','PHONENUMBER',?,?,'APPROVED',NOW())
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplateOne.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(createExpenseSql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, phone);
            ps.setInt(2, amount);
            return ps;
        }, keyHolder);

        return keyHolder.getKey().intValue();
    }

    private void sendToDad(DirectorNextOfKin director) {
        int rowId = createExpenseEntry(director.getDadPhone(), director.getDadWeeklyPay());
        sendMoney(String.valueOf(director.getDadWeeklyPay()),director.getDadPhone(),"EXPENSE"+rowId);
    }


    private void sendMoney(String amount, String phone, String occasion) {
        B2CRequest b2CRequest = new B2CRequest();
        b2CRequest.setCommandID(MpesaCommandId.BusinessPayment);
        b2CRequest.setAmount(amount);
        b2CRequest.setPartyB(phone);
        b2CRequest.setRemarks(occasion);
        b2CRequest.setAppId(3);
        b2CRequest.setOccasion(occasion);

        try {
            B2CRequestResponse res = mpesaTransactionsService.initiateb2c(b2CRequest, 3);
            System.out.println(res.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    List<DirectorNextOfKin> getDirectorsNextOfKin(){

        String sql = """
                SELECT director_name,personal_phone, dad_phone,dad_weekly_pay, mum_phone,mum_weekly_pay, wife_phone,wife_weekly_pay, created_at, deleted_at\s
                FROM directors_nextofkin WHERE deleted_at is null
                """;

        return jdbcTemplateOne.query(sql,(rs,i)->{
            DirectorNextOfKin directorNextOfKin = new DirectorNextOfKin();
            directorNextOfKin.setUserName(rs.getString("director_name"));
            directorNextOfKin.setPhone(rs.getString("personal_phone"));
            directorNextOfKin.setDadPhone(rs.getString("dad_phone"));
            directorNextOfKin.setDadWeeklyPay(rs.getInt("dad_weekly_pay"));
            directorNextOfKin.setMumPhone(rs.getString("mum_phone"));
            directorNextOfKin.setMumWeeklyPay(rs.getInt("mum_weekly_pay"));
            return directorNextOfKin;
        });

    }
}
