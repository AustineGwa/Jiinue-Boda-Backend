package com.otblabs.jiinueboda.fieldapp.marketing;

import com.otblabs.jiinueboda.fieldapp.marketing.models.LeadFollowUp;
import com.otblabs.jiinueboda.fieldapp.marketing.models.MarketingLead;
import com.otblabs.jiinueboda.fieldapp.marketing.models.MarketingQuestionaire;
import com.otblabs.jiinueboda.users.UserService;
import com.otblabs.jiinueboda.users.models.SystemUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/marketing")
public class MarketingController {

    private final MarketingService marketingService;
    private final UserService userService;

    public MarketingController(MarketingService marketingService, UserService userService) {
        this.marketingService = marketingService;
        this.userService = userService;
    }

    @PostMapping("/new-leads")
    ResponseEntity<Object> createNewMarketingLeads(@RequestBody MarketingLead marketingLead, Principal principal){

        SystemUser userByPhone = userService.getByEmailOrPhone(marketingLead.getPhone());
        if(userByPhone != null){
            return ResponseEntity.unprocessableEntity().body("Phone number already exist, please login to your account");
        }

        try{
            return ResponseEntity.ok(marketingService.createNewMarketingLeads(marketingLead, principal.getName()));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

    @PostMapping("/lead-followup")
    ResponseEntity<Object> followupOnMarketingLeads(@RequestBody LeadFollowUp leadFollowUp, Principal principal){

        try{
            return ResponseEntity.ok(marketingService.followupOnMarketingLeads(leadFollowUp, principal.getName()));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

    @GetMapping("/lead-followup/comments/{leadId}")
    ResponseEntity<Object> getLeadsComments(@PathVariable int leadId){

        try{
            return ResponseEntity.ok(marketingService.getLeadsComments(leadId));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

    @PostMapping("/new-questionaire")
    ResponseEntity<Object> createNewMarketingQuestionaire(@RequestBody MarketingQuestionaire marketingQuestionaire, Principal principal){

        try{
            return ResponseEntity.ok(marketingService.createNewMarketingQuestionaire(marketingQuestionaire, principal.getName()));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }
}
