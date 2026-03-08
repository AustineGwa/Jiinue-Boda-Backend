package com.otblabs.jiinueboda.recovery.v2;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recovery-record")
public class BikeRecoveryRecordController {

    private final BikeRecoveryRecordService bikeRecoveryRecordService;

    public BikeRecoveryRecordController(BikeRecoveryRecordService bikeRecoveryRecordService) {
        this.bikeRecoveryRecordService = bikeRecoveryRecordService;
    }

    @PostMapping("/create-new")
    ResponseEntity<Integer> saveToRecoveryRadar(@RequestBody BikeRecoveryRadar bikeRecoveryRadar){
        try{
            return ResponseEntity.ok( bikeRecoveryRecordService.insertRecoveryRadar(bikeRecoveryRadar));
        }catch (Exception e){
            return ResponseEntity.internalServerError().build();
        }

    }
}
