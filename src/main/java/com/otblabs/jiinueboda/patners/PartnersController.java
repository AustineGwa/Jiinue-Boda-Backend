package com.otblabs.jiinueboda.patners;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/patners")
public class PartnersController {

    private final PartnersService partnersService;

    public PartnersController(PartnersService partnersService) {
        this.partnersService = partnersService;
    }

    @PostMapping("/create/new")
    ResponseEntity<Partner> createNewPartner(@RequestBody Partner partner){
        try {
          return ResponseEntity.ok(partnersService.insertPartner(partner));
        }catch (Exception exception){
            exception.printStackTrace();
          return ResponseEntity.internalServerError().build();
        }

    }

    @GetMapping("/all")
    ResponseEntity<List<Partner>> getAllPatners(){
        try {
            return ResponseEntity.ok(partnersService.getAllPartners());
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }


}
