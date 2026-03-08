package com.otblabs.jiinueboda.tracking.trackers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/trackers")
public class TrackersController {

    private final TrackerService trackerService;

    public TrackersController(TrackerService trackerService) {
        this.trackerService = trackerService;
    }

    @GetMapping("/all")
    ResponseEntity<Object> getAll() {
        try{
            return ResponseEntity.ok(trackerService.getAllTrackers());
        }catch (Exception ex){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/update-tracker-simcard")
    ResponseEntity<Object> updateTrackerSimcard(@RequestBody SimcardUpdate simcardUpdate){
        try{
            return ResponseEntity.ok(trackerService.updateTrackerSimcard(simcardUpdate.trackerId, simcardUpdate.simcard));
        }catch (Exception ex){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
