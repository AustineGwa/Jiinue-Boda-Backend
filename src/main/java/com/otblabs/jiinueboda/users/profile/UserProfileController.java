package com.otblabs.jiinueboda.users.profile;

import com.otblabs.jiinueboda.users.models.ProfileLoanStanding;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/profile")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/profile-picture/{userId}")
    ResponseEntity<String> getUserProfilePicture(@PathVariable int userId){
        try{
            return ResponseEntity.ok(userProfileService.getUserProfilePicture(userId));

        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/loan-standing/{userId}")
    ResponseEntity<ProfileLoanStanding> getProfileLoanStanding(@PathVariable int userId){

        try{
            return ResponseEntity.ok(userProfileService.getProfileLoanStanding(userId));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }


    @GetMapping("/kyc/kyc-docs/{userId}")
    ResponseEntity <List<KycDocument>> getAllUserKycDocuments(@PathVariable int userId){
        try{
            return ResponseEntity.ok(userProfileService.getAllUserKycDocuments(userId));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();

        }
    }

}
