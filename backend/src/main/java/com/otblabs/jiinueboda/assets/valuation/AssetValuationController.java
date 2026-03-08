package com.otblabs.jiinueboda.assets.valuation;

import com.otblabs.jiinueboda.assets.models.ClientAsset;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/valuations")
public class AssetValuationController {

    private final AssetValuationService assetValuationService;

    public AssetValuationController(AssetValuationService assetValuationService) {
        this.assetValuationService = assetValuationService;
    }

    @GetMapping("/asset-valuation/{userId}")
    ResponseEntity<List<ClientAsset>> getPendingAssignedValuations(@PathVariable String userId){
        try {
            List<ClientAsset> assetValuation = assetValuationService.getPendingAssignedValuations(userId);
            return ResponseEntity.ok(assetValuation);
        } catch (Exception e) {
            e.printStackTrace();
            return  ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/asset-valuation/{valuerID}")
    ResponseEntity<Object> saveAssetEvaluation(@RequestBody AssetValuationForm assetValuationForm, @PathVariable int valuerID){
        try {
            boolean assetValuation = assetValuationService.saveAssetEvaluation(assetValuationForm,valuerID);
            return ResponseEntity.ok(assetValuation);
        } catch (Exception e) {
            e.printStackTrace();
            return  ResponseEntity.internalServerError().build();
        }
    }
}
