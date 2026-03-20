package com.otblabs.jiinueboda.collections.recoveryV2;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/recovery-record")
public class BikeRecoveryRecordController {

    private final BikeRecoveryRecordService bikeRecoveryRecordService;

    public BikeRecoveryRecordController(BikeRecoveryRecordService bikeRecoveryRecordService) {
        this.bikeRecoveryRecordService = bikeRecoveryRecordService;
    }

    @GetMapping("/requests/all")
    ResponseEntity<List<BikeRecoveryRadaDAO>> getAllRequestedRecovery(){
        try{
            return ResponseEntity.ok(bikeRecoveryRecordService.getAllRequestedRecovery());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

    @GetMapping("/approved-recoveries/all")
    ResponseEntity<List<ApprovedRecovery>> getAllApprovedRecovery(){
        try{
            return ResponseEntity.ok(bikeRecoveryRecordService.getAllApprovedRecovery());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

    @PostMapping("/create-new")
    ResponseEntity<Integer> saveToRecoveryRadar(@RequestBody BikeRecoveryRadarRequestDTO bikeRecoveryRadarRequestDTO, Principal principal){
        try{
            return ResponseEntity.ok( bikeRecoveryRecordService.insertRecoveryRadar(bikeRecoveryRadarRequestDTO, principal.getName()));
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

    @PostMapping("/update-admin-comment")
    ResponseEntity<Integer> saveUpdateAdminComment(@RequestBody AdminRecoveryCommentDTO adminRecoveryCommentDTO, Principal principal){
        try{
            return ResponseEntity.ok( bikeRecoveryRecordService.saveUpdateAdminComment(adminRecoveryCommentDTO, principal.getName()));
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

    @PostMapping("/update-confirm-recovery")
    ResponseEntity<Integer> saveConfirmRecovery(@RequestBody ConfirmRecoveryDTO confirmRecoveryDTO, Principal principal){
        try{
            return ResponseEntity.ok( bikeRecoveryRecordService.saveConfirmRecovery(confirmRecoveryDTO, principal.getName()));
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

    @PostMapping("/update-confirm-release")
    ResponseEntity<Integer> saveConfirmRelease(@RequestBody AssetReleaseDTO assetReleaseDTO, Principal principal){
        try{
            return ResponseEntity.ok( bikeRecoveryRecordService.assetReleaseDTO(assetReleaseDTO, principal.getName()));
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }


}
