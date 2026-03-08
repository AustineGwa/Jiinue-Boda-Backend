package com.otblabs.jiinueboda.fieldapp.controllers;

import com.otblabs.jiinueboda.fieldapp.models.*;
import com.otblabs.jiinueboda.fieldapp.services.FieldOperationsTrackerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/field-operations")
@RequiredArgsConstructor
@Slf4j
public class FieldOperationsTrackerController {

    private final FieldOperationsTrackerService trackerService;

    @GetMapping("/sessions/active/{userId}")
    ResponseEntity<Map<String, Object>> getActiveSessionForUser(@PathVariable Long userId){
        try{

            var data = trackerService.getActiveSessionForUser(userId);

            if(data.isPresent()){
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Data retrieved successfully",
                        "trackerData", data
                ));
            }else {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "There are no active sesions",
                        "trackerData", List.of()
                ));
            }



        }catch(Exception exception){
            exception.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "An error occurred while processing request"
                    ));

        }
    }

    @PostMapping("/start-day/{userId}")
    public ResponseEntity<Map<String, Object>> startDay(@PathVariable Long userId,  @RequestBody StartDayRequest request) {

        try {
            Long trackerId = trackerService.startDay(userId, request);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Day started successfully",
                    "trackerId", trackerId
            ));

        } catch (IllegalStateException e) {
            log.warn("Failed to start day for user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        } catch (Exception e) {
            log.error("Error starting day for user {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "An error occurred while starting the day"
                    ));
        }
    }

    @PostMapping("/end-day/{userId}")
    public ResponseEntity<Map<String, Object>> endDay(@PathVariable Long userId,@RequestBody EndDayRequest request) {

        try {
            trackerService.endDay(userId, request);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Day ended successfully"
            ));

        } catch (IllegalStateException e) {
            log.warn("Failed to end day for user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        } catch (Exception e) {
            log.error("Error ending day for user {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "An error occurred while ending the day"
                    ));
        }
    }

    @PutMapping("/pause-session")
    public ResponseEntity<Map<String, Object>> pauseSession(@RequestHeader("User-ID") Long userId,@RequestBody PauseSessionRequest request) {

        try {
            trackerService.pauseSession(userId, request);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Session paused successfully"
            ));

        } catch (IllegalStateException e) {
            log.warn("Failed to pause session for user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        } catch (Exception e) {
            log.error("Error pausing session for user {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "An error occurred while pausing the session"
                    ));
        }
    }

    @PutMapping("/resume-session")
    public ResponseEntity<Map<String, Object>> resumeSession(@RequestHeader("User-ID") Long userId) {

        try {
            trackerService.resumeSession(userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Session resumed successfully"
            ));

        } catch (IllegalStateException e) {
            log.warn("Failed to resume session for user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        } catch (Exception e) {
            log.error("Error resuming session for user {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "An error occurred while resuming the session"
                    ));
        }
    }

    @PutMapping("/update-session")
    public ResponseEntity<Map<String, Object>> updateSession(@RequestHeader("User-ID") Long userId, @RequestBody UpdateSessionRequest request) {

        try {
            trackerService.updateSession(userId, request);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Session updated successfully"
            ));

        } catch (IllegalStateException e) {
            log.warn("Failed to update session for user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        } catch (Exception e) {
            log.error("Error updating session for user {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "An error occurred while updating the session"
                    ));
        }
    }

    @PutMapping("/cancel-session")
    public ResponseEntity<Map<String, Object>> cancelSession(@RequestHeader("User-ID") Long userId) {

        try {
            trackerService.cancelSession(userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Session cancelled successfully"
            ));

        } catch (IllegalStateException e) {
            log.warn("Failed to cancel session for user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        } catch (Exception e) {
            log.error("Error cancelling session for user {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "An error occurred while cancelling the session"
                    ));
        }
    }
}
