package com.uniware.integrations.exception;

public class FailureResponse extends RuntimeException {
    public FailureResponse(String message) {
        this.message = message;
    }

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}