package com.otblabs.jiinueboda.staff;

import com.otblabs.jiinueboda.users.UserService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Service
public class StaffService {

    private final JdbcTemplate jdbcTemplateOne;
    private final UserService userService;

    public StaffService(JdbcTemplate jdbcTemplateOne, UserService userService) {
        this.jdbcTemplateOne = jdbcTemplateOne;
        this.userService = userService;
    }

    public List<Staff> getAllStaff() throws Exception{

        String sql = """
               
                SELECT id,first_name,middle_name,last_name,email,phone,nationalId,usertype, p.base_salary FROM users u left join user_roles ur on u.id = ur.user_id
                              left join payroll p on u.id = p.user_id
                              WHERE ur.usertype NOT IN ('Admin','Client', 'FuelLoan','Evaluator','AMBOSSODOR')
                              AND u.deleted_at is null
                              UNION ALL
                              SELECT id,first_name,middle_name,last_name, 'N/A' as email,phone_number as phone,id_number as nationalId,role as usertype, wage as base_salary
                              FROM casual_workers
                              where deleted_at is null
                              ORDER BY id
                """;
        return  jdbcTemplateOne.query(sql,(rs,i)-> setStaff(rs));

    }

    private Staff setStaff(ResultSet rs) throws SQLException {
        Staff staff = new Staff();
        staff.setId(rs.getInt("id"));
        staff.setFirstName(rs.getString("first_name"));
        staff.setMiddleName(rs.getString("middle_name"));
        staff.setLastName(rs.getString("last_name"));
        staff.setEmail(rs.getString("email"));
        staff.setPhone(rs.getString("phone"));
        staff.setNationalId(rs.getString("nationalId"));
        staff.setUsertype(rs.getString("usertype"));
        staff.setBaseSalary(rs.getInt("base_salary"));
        return staff;
    }

    @Transactional
    public Object createNewStaff(NewStaffRequest staff) throws Exception{
        return userService.createNewStaff(staff);
    }

    public List<Staff> getInternalStaff() {

        String sql = """
                SELECT  phone  FROM users  WHERE deleted_at is null AND id in (SELECT user_id FROM user_roles WHERE usertype != 'client' && usertype != 'FuelLoan')
                """;

        return  jdbcTemplateOne.query(sql,(rs,i)-> setStaff(rs));
    }

    public int stageStaffSalary(Staff staff){

        String description = staff.getFirstName() + " February Salary";

        String createExpenseSql = """
            INSERT INTO temp_expense_requests(main_category_id,subcategory_id, minor_subcategory_id, description, reciever_type, reciever,amount,status,created_at)
            VALUES(2,9,4,?,'PHONENUMBER',?,?,'PENDING_APPROVAL',NOW())
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplateOne.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(createExpenseSql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, description);
            ps.setString(2, staff.getPhone());
            ps.setInt(3, staff.getBaseSalary());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().intValue();
    }

}
