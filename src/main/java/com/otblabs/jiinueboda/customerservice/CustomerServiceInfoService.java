package com.otblabs.jiinueboda.customerservice;

import com.otblabs.jiinueboda.users.UserService;
import com.otblabs.jiinueboda.users.models.SystemUser;
import com.otblabs.jiinueboda.utility.UtilityFunctions;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class CustomerServiceInfoService {

    private final JdbcTemplate jdbcTemplateOne;
    private final UserService userService;

    public CustomerServiceInfoService(JdbcTemplate jdbcTemplateOne, UserService userService) {
        this.jdbcTemplateOne = jdbcTemplateOne;
        this.userService = userService;
    }


    int createNewCustomerServiceInfo(CustomerServiceInfo customerServiceInfo, String name) throws Exception{

        SystemUser user = userService.getByEmailOrPhone(name);

        String sql = """
                INSERT INTO customer_service(branch_id, service_category, service_category_other, customerType, customer_id_number, phone_number,
                               customer_can_be_served, service_status,created_by, loan_officer_comment,created_at) VALUES(?,?,?,?,?,?,?,?,?,?,NOW())
                """;

        return jdbcTemplateOne.update(sql,
                customerServiceInfo.getBranchId(),
                customerServiceInfo.getServiceCategory(),
                customerServiceInfo.getServiceCategoryOther(),
                customerServiceInfo.getCustomerType(),
                customerServiceInfo.getCustomerIdNumber(),
                UtilityFunctions.formatPhoneNumber(customerServiceInfo.getCustomerPhoneNumber()),
                customerServiceInfo.isCustomerCanBeServed(),
                "PENDING",
                user.getId(),
                customerServiceInfo.getLoanOfficerComment()
                );
    }

    public List<CustomerServiceInfo> getAllServices(String status) {

        String sql = "SELECT * FROM customer_service WHERE service_status =? ORDER BY created_at desc";

        return jdbcTemplateOne.query(sql,(rs, i) -> setCustomerServiceInfo(rs), status);
    }

    private CustomerServiceInfo setCustomerServiceInfo(ResultSet rs) throws SQLException {
        CustomerServiceInfo customerServiceInfo = new CustomerServiceInfo();
        customerServiceInfo.setServiceId(rs.getInt("id"));
        customerServiceInfo.setBranchId(rs.getInt("branch_id"));
        customerServiceInfo.setServiceCategory(rs.getString("service_category"));
        customerServiceInfo.setServiceCategoryOther(rs.getString("service_category_other"));
        customerServiceInfo.setCustomerType(rs.getString("customerType"));
        customerServiceInfo.setCustomerIdNumber(rs.getString("customer_id_number"));
        customerServiceInfo.setCustomerPhoneNumber(rs.getString("phone_number"));
        customerServiceInfo.setCustomerCanBeServed(rs.getBoolean("customer_can_be_served"));
        customerServiceInfo.setServiceStatus(rs.getString("service_status"));
        customerServiceInfo.setLoanOfficerComment(rs.getString("loan_officer_comment"));
        customerServiceInfo.setOpsMangerComment(rs.getString("ops_manger_comment"));
        customerServiceInfo.setCreatedAt(rs.getString("created_at"));
        return customerServiceInfo;
    }

    public int updateServiceStatusManger(ManagerUpdate managerUpdate, String name) throws Exception{
        SystemUser user = userService.getByEmailOrPhone(name);

        String sql = "UPDATE customer_service SET ops_manger_comment =?, service_status ='COMPLETE', completed_by=?, completed_at=NOW() WHERE id =?";
        return  jdbcTemplateOne.update(sql,
                managerUpdate.getManagerComment(),
                user.getId(),
                managerUpdate.getServiceId()
        );
    }
}
