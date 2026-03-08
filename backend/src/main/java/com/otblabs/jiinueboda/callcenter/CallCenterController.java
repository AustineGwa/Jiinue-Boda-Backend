package com.otblabs.jiinueboda.callcenter;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/callcenter")
public class CallCenterController {

    private final CallCenterService callCenterService;

    public CallCenterController(CallCenterService callCenterService) {
        this.callCenterService = callCenterService;
    }

    @RequestMapping("/loan-profile-info/{loanId}")
    public ResponseEntity<MinimalProfile> getMinimalProfileInfo(@PathVariable String loanId){
        try{
            return  ResponseEntity.ok(callCenterService.getMinimalProfileInfo(loanId));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @RequestMapping("/get-all-comments/{loanId}")
    public ResponseEntity<List<CallComment>> getAllLoanComments(@PathVariable String loanId){
        try{
            return  ResponseEntity.ok(callCenterService.getAllLoanComments(loanId));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/post-comment")
    ResponseEntity<Integer> postCallComment(@RequestBody CallComment callComment, Principal principal){

        try{
           return  ResponseEntity.ok(callCenterService.postCallComment(callComment, principal.getName()));
        }catch (Exception exception){
            exception.printStackTrace();
           return ResponseEntity.internalServerError().build();
        }

    }


    @PostMapping(path = "/post-comment-multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<Integer> postCallCommentMultipart(@ModelAttribute CallCommentMultipart callComment, Principal principal){

        try{
            return  ResponseEntity.ok(callCenterService.postCallCommentMultipart(callComment, principal.getName()));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }
}
