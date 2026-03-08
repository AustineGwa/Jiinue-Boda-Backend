package com.otblabs.jiinueboda.assets.valuation.online;

import com.otblabs.jiinueboda.assets.valuation.online.models.OnlineAssetValuation;
import com.otblabs.jiinueboda.assets.valuation.online.models.ValuationRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/online-valuation")
public class OnlineValuationController {

    private final OnlineValuationService service;

    public OnlineValuationController(OnlineValuationService service) {
        this.service = service;
    }

    @PostMapping("/create-new-valuation")
    public ResponseEntity<OnlineAssetValuation> create(@Valid @RequestBody ValuationRequest request) {
        try{
            OnlineAssetValuation saved = service.create(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }


    @GetMapping("/{id}")
    public ResponseEntity<OnlineAssetValuation> getById(@PathVariable Integer id) {
        return service.findValuationByById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Valuation not found with id: " + id));
    }

    @GetMapping
    public ResponseEntity<List<OnlineAssetValuation>> getByAsset( @RequestParam(name = "assetId") Integer assetId) {
        List<OnlineAssetValuation> results = service.ValuationForAssetByAssetId(assetId);
        return ResponseEntity.ok(results);
    }
}
