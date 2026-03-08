package com.otblabs.jiinueboda.fieldapp.geographic;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/location-services")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;

    @GetMapping("/options")
    public ResponseEntity<Map<String, Object>> getLocationOptions() {
        try{
            return ResponseEntity.ok(locationService.getLocationDataOptions());
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }
}

