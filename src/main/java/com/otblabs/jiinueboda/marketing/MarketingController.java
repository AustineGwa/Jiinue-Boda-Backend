package com.otblabs.jiinueboda.marketing;

import com.otblabs.jiinueboda.marketing.models.LeadFollowUp;
import com.otblabs.jiinueboda.marketing.models.MarketingLead;
import com.otblabs.jiinueboda.marketing.models.MarketingQuestionaire;
import com.otblabs.jiinueboda.users.UserService;
import com.otblabs.jiinueboda.users.models.SystemUser;
import com.otblabs.jiinueboda.utility.Functions;
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
    ResponseEntity<Object> createNewMarketingLeads( @RequestBody MarketingLead marketingLead, Principal principal  ) {

        if (marketingLead.getPhone() == null || marketingLead.getPhone().isBlank()) {
            return ResponseEntity.badRequest().body("Phone number is required");
        }

        SystemUser existingUser = userService.getByEmailOrPhone(marketingLead.getPhone());
        if (existingUser != null) {
            return ResponseEntity.unprocessableEntity()
                    .body("Phone number already exists, please login to your account");
        }

        try {
            int result = marketingService.createNewMarketingLeads(marketingLead, principal.getName());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to create lead");
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

    @GetMapping("/channels")
    public ResponseEntity<?> getChannels() {
        return ResponseEntity.ok(marketingService.getChannels());
    }

    @GetMapping("/campaigns")
    public ResponseEntity<?> getCampaigns() {
        return ResponseEntity.ok(marketingService.getCampaigns());
    }

    @GetMapping("/agents")
    public ResponseEntity<?> getAgents() {
        return ResponseEntity.ok(marketingService.getAgents());
    }

    @GetMapping("/branches")
    public ResponseEntity<?> getBranches() {
        return ResponseEntity.ok(marketingService.getBranches());
    }

    @GetMapping("/funnel-stages")
    public ResponseEntity<?> getFunnelStages() {
        return ResponseEntity.ok(marketingService.getFunnelStages());
    }

    @GetMapping("/funnel-event-types")
    public ResponseEntity<?> getFunnelEventTypes() {
        return ResponseEntity.ok(marketingService.getFunnelEventTypes());
    }




}
