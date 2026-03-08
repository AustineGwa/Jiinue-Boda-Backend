package com.otblabs.jiinueboda.users;

import com.otblabs.jiinueboda.users.models.*;
import com.otblabs.jiinueboda.users.profile.UserKyc;
import com.otblabs.jiinueboda.utility.response.RestResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UsersController {
    private final UserService userService;
    public UsersController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    ResponseEntity<RestResponseBody<Object>> getAllUsers(){
         try{
             return ResponseEntity.ok(new RestResponseBody<>(userService.getAllUsers()));
         } catch (Exception e) {
             e.printStackTrace();
             return ResponseEntity.internalServerError().build();
         }
    }

    @GetMapping("/agents/all")
    ResponseEntity<RestResponseBody<Object>>getAllUsersByAgents(){
        try{
            return ResponseEntity.ok(new RestResponseBody<>(userService.getAllUsersByAgents()));
        } catch (Exception e) {
          return  ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/agents/{refId}")
    ResponseEntity<RestResponseBody<Object>> getAllUsersByAgents(@PathVariable String refId){
        try{
            return ResponseEntity.ok(new RestResponseBody<>(userService.getAllUsersByAgentRefference(refId)));
        } catch (Exception e) {
            return  ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/partner/{partnerId}")
    ResponseEntity<RestResponseBody<Object>> getAllUsersForPartner(@PathVariable int partnerId){
        try{
            return ResponseEntity.ok(new RestResponseBody<>(userService.getAllUsersForPartner(partnerId)));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/refferals/{referalId}")
    ResponseEntity<RestResponseBody<Object>> getAllUsersRefferals(@PathVariable String referalId){
        try{
            return ResponseEntity.ok(new RestResponseBody<>(userService.getAllUsersRefferals(referalId)));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/createuser")
    ResponseEntity<Object> createNewUser(@RequestBody NewUserRequest newUserRequest ){

        SystemUser userByPhone = userService.getByEmailOrPhone(newUserRequest.getPhone());
        if(userByPhone != null){
            return ResponseEntity.unprocessableEntity().body("Phone number already exist, please login to your account");
        }

        SystemUser userEmail  = userService.getByEmailOrPhone(newUserRequest.getEmail());
        if(userEmail != null){
            return ResponseEntity.unprocessableEntity().body("Email already exist, please login to your account");
        }

        try{

            return  ResponseEntity.ok(userService.createNewUserWithRole(newUserRequest));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/invite/createuser")
    ResponseEntity<Object> createNewUserWithInvite(@RequestBody NewUserRequest newUserRequest ){

        if(newUserRequest.getRefferedBy() == null){
            return ResponseEntity.unprocessableEntity().body("No referral code detected");
        }

        SystemUser userByPhone = userService.getByEmailOrPhone(newUserRequest.getPhone());
        if(userByPhone != null){
            return ResponseEntity.unprocessableEntity().body("Phone number already exist, please login to your account");
        }

        SystemUser userEmail  = userService.getByEmailOrPhone(newUserRequest.getEmail());
        if(userEmail != null){
            return ResponseEntity.unprocessableEntity().body("Email already exist, please login to your account");
        }

        try{

            return  ResponseEntity.ok(userService.createNewUserWithRole(newUserRequest));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/update-user-kyc")
    ResponseEntity<Object> updateUserKyc(@RequestBody UserKyc userKyc, Principal principal){

        SystemUser user = userService.getUserByID(userKyc.getUserId());
        if(user == null){
            return ResponseEntity.unprocessableEntity().body("This user does not exist, please create a new user account");
        }

        try{
            return  ResponseEntity.ok(userService.updateUserKyc(userKyc,principal.getName()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/users/{usertype}")
    ResponseEntity<Object> getUsers(@PathVariable String usertype){
        try {
            return ResponseEntity.ok(userService.getAllUsers(Usertype.valueOf(usertype)));
        }catch (Exception exception){
            exception.printStackTrace();
            return  ResponseEntity.internalServerError().build();
        }

    }

    @PostMapping("/update/{userID}/{appID}")
    ResponseEntity<Object> updateUserStatus(@RequestBody String status, @PathVariable int userID, @PathVariable int appID){
        try{
            return ResponseEntity.ok(userService.updateUserStatus(status,userID,appID));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/partner/update/{userID}/{appID}")
    ResponseEntity<Object> partnerUpdateUserStatus(@RequestBody String status, @PathVariable int userID, @PathVariable int appID){
        try{
            return ResponseEntity.ok(userService.partnerUpdateUserStatus(status,userID,appID));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/update/{appID}")
    ResponseEntity<Object> updateUserProfile(@RequestBody UserProfile userProfile, @PathVariable int appID){
        try{
            return ResponseEntity.ok(userService.updateUserProfile(userProfile,appID));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

    @PostMapping("/approve/{userId}")
    ResponseEntity<Object> approveUser(@PathVariable int userId, @RequestParam(name="approved") int approved){
        try{
            return ResponseEntity.ok(userService.approveUser(userId,approved));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

    @PostMapping("/update/group")
    ResponseEntity<Object> updateUserGroup(@RequestBody UpdateUserDto updateUserDto ){
        try{
            return ResponseEntity.ok(userService.updateUserGroup(updateUserDto));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }



    @GetMapping("/profiles/all")
    ResponseEntity<List<UserProfile>> getUserProfiles(){
        try{
            return ResponseEntity.ok(userService.getUserProfiles());
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/profiles/partner/{partnerId}")
    ResponseEntity<List<UserProfile>> getUserProfilesForPartner(@PathVariable int partnerId){

        try{
            return ResponseEntity.ok(userService.getUserProfilesForPartner(partnerId));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/profiles/{userID}")
    ResponseEntity<UserProfile> getUserProfiles(@PathVariable int userID){
        try{
            return ResponseEntity.ok(userService.getUserProfile(userID));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/update/refferal")
    ResponseEntity<Object> enrollRefferal(@RequestBody UserReferral userReferral, Principal principal){

        try{
            return ResponseEntity.ok(userService.enrollRefferal(userReferral,principal.getName()));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

    @GetMapping("/taken-loan/completed-loan/inactive")
    ResponseEntity<Object> getAllInactiveClientsWithPastLoan(){
        try{
            return ResponseEntity.ok(userService.getAllInactiveClientsWithPastLoan());
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/user-kyc/{userId}")
    ResponseEntity<Object> getAllUserKYCEntries(@PathVariable int userId){
        try{
            return ResponseEntity.ok(userService.getAllUserKYCEntries(userId));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/update-dob/{userId}")
    ResponseEntity<Object> updateDateOfBirthForClient(@RequestBody UpdateDob dob, @PathVariable int userId){


        try{
            return ResponseEntity.ok(userService.updateDateOfBirthForClient(dob.getDob(), userId));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/agent-users/update-stage")
    ResponseEntity<Object> updateUserStage(@RequestBody StageUpdateDTO stageUpdateDTO){
        try{
            return ResponseEntity.ok(userService.updateUserStage(stageUpdateDTO));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/update-online-rider-status")
    ResponseEntity<Object> updateOnlineRiderStatus(@RequestBody OnlineRiderStatusDTO onlineRiderStatusDTO){
        try{
            return ResponseEntity.ok(userService.updateOnlineRiderStatus(onlineRiderStatusDTO));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }



}
