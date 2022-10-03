package com.uniware.integrations.exception;

import lombok.ToString;

@ToString
public class DataNotAvailable extends RuntimeException {
    private String message;

    public DataNotAvailable(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}