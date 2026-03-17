package com.otblabs.jiinueboda.assets.valuation.online;

import com.otblabs.jiinueboda.assets.AssetsService;
import com.otblabs.jiinueboda.assets.valuation.online.models.OnlineAssetValuation;
import com.otblabs.jiinueboda.assets.valuation.online.models.ValuationRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/online-valuation")
public class OnlineValuationController {

    private final OnlineValuationService service;
    private final AssetsService assetsService;

    public OnlineValuationController(OnlineValuationService service, AssetsService assetsService) {
        this.service = service;
        this.assetsService = assetsService;
    }

    @PostMapping("/create-new-valuation")
    public ResponseEntity<OnlineAssetValuation> create( @RequestBody ValuationRequest request, Principal principal) {

        System.out.println("VALUATION REQUEST "+ request.toString());
        try{
            OnlineAssetValuation saved = service.create(request, principal.getName());
            assetsService.updateEvalStatusOnAsset(request.getTechnicianId(), request.getAssetId());
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

    @GetMapping("/assets/{assetId}")
    public ResponseEntity<List<OnlineAssetValuation>> getByAsset( @PathVariable int assetId) {
        try{
            List<OnlineAssetValuation> results = service.ValuationForAssetByAssetId(assetId);
            return ResponseEntity.ok(results);

        }catch(Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }
}
