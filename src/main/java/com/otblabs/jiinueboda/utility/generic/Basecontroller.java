package com.otblabs.jiinueboda.utility.generic;

import org.springframework.http.ResponseEntity;

public abstract class Basecontroller {

    public ResponseEntity<SuccessResponse> success() {
        return ResponseEntity.status(200).body(new SuccessResponse("true", "Successful"));
    }

    public ResponseEntity<SuccessResponse> success(String message) {
        return ResponseEntity.status(200).body(new SuccessResponse("true", message));
    }

    public ResponseEntity<SuccessResponse> failure(String msg) {
        return ResponseEntity.status(400).body(new SuccessResponse("false", msg));
    }

    public ResponseEntity<SuccessResponse> unauthorized(String msg) {
        return ResponseEntity.status(401).body(new SuccessResponse("false", msg));
    }

    public ResponseEntity<SuccessResponse> accessDenied() {
        return ResponseEntity.status(403).body(new SuccessResponse("false", "Access denied"));
    }

    public ResponseEntity<SuccessResponse> notPermitted() {
        return ResponseEntity.status(404).body(new SuccessResponse("false", "Operation Not Permitted"));
    }

    public ResponseEntity<SuccessResponse> notFound() {
        return ResponseEntity.status(404).body(new SuccessResponse("false", "Not found"));
    }

    public ResponseEntity<SuccessResponse> notFound(String msg) {
        return ResponseEntity.status(404).body(new SuccessResponse("false", msg));
    }

    public ResponseEntity<SuccessResponse> invalidInput() {
        return ResponseEntity.status(405).body(new SuccessResponse("false", "Invalid input"));
    }

    public ResponseEntity<SuccessResponse> invalidInput(String msg) {
        return ResponseEntity.status(405).body(new SuccessResponse("false", msg));
    }

    public ResponseEntity<SuccessResponse> serverFailure() {
        return ResponseEntity.status(500).body(new SuccessResponse("false", "Server Error"));
    }

    public ResponseEntity<SuccessResponse> noContent() {
        return ResponseEntity.status(202).body(new SuccessResponse("true", "Success"));
    }

    public ResponseEntity<SuccessResponse> noContent(String msg) {
        return ResponseEntity.status(202).body(new SuccessResponse("true", msg));
    }

    public ResponseEntity<?> entity(Object entity) {
        return ResponseEntity.status(200).header("Content-Type", "application/json").body(entity);
    }

    public ResponseEntity<?> response(Object entity, String contentType) {
        if (contentType == null)
            contentType = "application/json";
        return ResponseEntity.status(200).header("Content-Type", contentType).body(entity);
    }

}
