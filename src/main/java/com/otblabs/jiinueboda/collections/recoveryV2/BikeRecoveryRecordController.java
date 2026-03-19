package com.otblabs.jiinueboda.collections.recoveryV2;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recovery-record")
public class BikeRecoveryRecordController {

    private final BikeRecoveryRecordService bikeRecoveryRecordService;

    public BikeRecoveryRecordController(BikeRecoveryRecordService bikeRecoveryRecordService) {
        this.bikeRecoveryRecordService = bikeRecoveryRecordService;
    }

    @PostMapping("/create-new")
    ResponseEntity<Integer> saveToRecoveryRadar(@RequestBody BikeRecoveryRadarRequestDTO bikeRecoveryRadarRequestDTO){
        try{
            return ResponseEntity.ok( bikeRecoveryRecordService.insertRecoveryRadar(bikeRecoveryRadarRequestDTO));
        }catch (Exception e){
            return ResponseEntity.internalServerError().build();
        }

    }

    @GetMapping("/requests/all")
    ResponseEntity<List<BikeRecoveryRadaDAO>> getAllRequestedRecovery(){
        try{
            return ResponseEntity.ok(bikeRecoveryRecordService.getAllRequestedRecovery());
        }catch (Exception e){
            return ResponseEntity.internalServerError().build();
        }

    }
}
