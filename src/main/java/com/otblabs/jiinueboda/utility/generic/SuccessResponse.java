package com.otblabs.jiinueboda.utility.generic;

public class SuccessResponse {

    public String success;
    public String message;

    public SuccessResponse() {
    }

    /**
     * Constructor to customize API response
     *
     * @param success
     * @param message
     */
    public SuccessResponse(String success, String message) {
        this.success = success;
        this.message = message;
    }

}