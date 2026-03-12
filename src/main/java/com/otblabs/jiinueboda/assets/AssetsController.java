package com.otblabs.jiinueboda.assets;

import com.otblabs.jiinueboda.assets.models.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/assets")
public class AssetsController {

    private final AssetsService assetsService;

    public AssetsController(AssetsService assetsService) {
        this.assetsService = assetsService;
    }

    @PostMapping(value = "/create-new")
    public ResponseEntity<?> registerAsset(@RequestBody NewAssetDto newAssetDto, Principal principal) {
        try{
            return ResponseEntity.ok(assetsService.createNewAsset(newAssetDto, principal.getName()));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().body(exception.getMessage());
        }
    }

    @PostMapping(value = "/new", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerAsset(@ModelAttribute NewAssetData assetData, Principal principal) {
        try{
          return ResponseEntity.ok(assetsService.createNewAsset(assetData, principal.getName()));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().body(exception.getMessage());
        }
    }

    @PostMapping(value = "/update-asset-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerAsset(@ModelAttribute AssetImagesDto assetImagesDto) {
        try{
            return ResponseEntity.ok(assetsService.updateAssetImages(assetImagesDto));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().body(exception.getMessage());
        }
    }



    @PostMapping(value = "/valuation/complete", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> assignValuation(@ModelAttribute ValuationSubmissionData valuationSubmissionData) {
        try{
            return ResponseEntity.ok(assetsService.updateValuationForAsset(valuationSubmissionData));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().body(exception.getMessage());
        }
    }

    @PostMapping(value = "/charged-logbook/complete", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> saveChargedLogbook(@ModelAttribute LogbookSubmissionData logbookSubmissionData) {
        try{
            return ResponseEntity.ok(assetsService.saveChargedLogbookForAsset(logbookSubmissionData));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().body(exception.getMessage());
        }
    }

    @GetMapping
    ResponseEntity<List<ClientAsset>> getAllAssets(){
        try{
            return ResponseEntity.ok(assetsService.getAllAssets());
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();

        }
    }

    @GetMapping("/{userId}")
    ResponseEntity<List<ClientAsset>> getAllClientAssets(@PathVariable int userId){
        try{
            return ResponseEntity.ok(assetsService.getAllClientAssets(userId));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();

        }
    }

    @GetMapping("/asset-attachments/{userId}")
    ResponseEntity <List<AssetAttachment>> getAllClientAssetAttachments(@PathVariable int userId){
        try{
            return ResponseEntity.ok(assetsService.getAllClientAssetAttachments(userId));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();

        }
    }







}
