package com.otblabs.jiinueboda.staff;

import com.otblabs.jiinueboda.users.UserService;
import com.otblabs.jiinueboda.users.models.SystemUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/staff")
public class StaffController {

    private final StaffService staffService;
    private final UserService  userService;


    public StaffController(StaffService staffService, UserService userService) {
        this.staffService = staffService;
        this.userService = userService;
    }

    @PostMapping("/create")
    ResponseEntity<Object> createStaff(@RequestBody NewStaffRequest newStaffRequest){

        SystemUser userByPhone = userService.getByEmailOrPhone(newStaffRequest.getPhone());
        if(userByPhone != null){
            return ResponseEntity.unprocessableEntity().body("Phone number already exist, please login to your account");
        }

        SystemUser userEmail  = userService.getByEmailOrPhone(newStaffRequest.getEmail());
        if(userEmail != null){
            return ResponseEntity.unprocessableEntity().body("Email already exist, please login to your account");
        }

        try{
            return ResponseEntity.ok(staffService.createNewStaff(newStaffRequest));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

    @GetMapping("/all")
    ResponseEntity<Object> getAllStaff(){

        try{
            return ResponseEntity.ok(staffService.getAllStaff());
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }
}
