package com.otblabs.jiinueboda.recovery;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recovery")
public class BikeRecoveryController {

    private final BikeRecoveryService bikeRecoveryService;

    public BikeRecoveryController(BikeRecoveryService bikeRecoveryService) {
        this.bikeRecoveryService = bikeRecoveryService;
    }

    @GetMapping("/full-list/{branch}")
    public Object getRecoveryList(@PathVariable int branch){
        try{
            return ResponseEntity.ok(bikeRecoveryService.getRecoveryList(branch));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

    @GetMapping("/storage-list")
    public Object getBikesInStorageList(){
        try{
            return ResponseEntity.ok(bikeRecoveryService.getStorageList());
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

    @PostMapping("/exempt-recovery")
    public Object exemptFromRecovery(@RequestBody ExemptionData exemptionData){
        try{
            return ResponseEntity.ok(bikeRecoveryService.exemptFromRecovery(exemptionData));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

    @PostMapping("/update-repo")
    public Object updateRepo(@RequestBody RepoData repoData){
        try{
            return ResponseEntity.ok(bikeRecoveryService.updateRepo(repoData));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

    @PostMapping("/update-release")
    public Object updateRelease(@RequestBody BikeReleaseData bikeReleaseData){
        try{
            return ResponseEntity.ok(bikeRecoveryService.updateRelease(bikeReleaseData));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }


}
