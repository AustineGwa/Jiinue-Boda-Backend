package com.otblabs.jiinueboda.customerservice;

import com.otblabs.jiinueboda.users.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/customer-service")
public class CustomerServiceInfoController {

    private final CustomerServiceInfoService customerServiceInfoService;
    private final JdbcTemplate jdbcTemplateOne;
    private final UserService userService;

    public CustomerServiceInfoController(CustomerServiceInfoService customerServiceInfoService, JdbcTemplate jdbcTemplateOne, UserService userService) {
        this.customerServiceInfoService = customerServiceInfoService;
        this.jdbcTemplateOne = jdbcTemplateOne;
        this.userService = userService;
    }


    //create new service entry
    @PostMapping("/create-new")
    ResponseEntity<Object> createNewServiceEntry(@RequestBody CustomerServiceInfo customerServiceInfo, Principal principal){

        try{
            return ResponseEntity.ok().body(customerServiceInfoService.createNewCustomerServiceInfo(customerServiceInfo, principal.getName()));

        }catch (Exception e){
            return ResponseEntity.internalServerError().build();
        }

    }

    @GetMapping("/services/{status}")
    ResponseEntity<Object> getAllServices(@PathVariable("status") String status){

        try{
            return ResponseEntity.ok(customerServiceInfoService.getAllServices(status));
        }catch(Exception e){
            return ResponseEntity.internalServerError().build();
        }

    }

    @PostMapping("/services/update/manager")
    ResponseEntity<Object> updateServiceStatusManger(@RequestBody ManagerUpdate managerUpdate, Principal principal){

        try{
            return ResponseEntity.ok(customerServiceInfoService.updateServiceStatusManger(managerUpdate, principal.getName()));
        }catch(Exception e){
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }



}
