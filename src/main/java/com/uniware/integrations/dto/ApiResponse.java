package com.uniware.integrations.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ApiResponse<T> {
    public enum STATUS {
        @JsonProperty("success")
        SUCCESS,
        @JsonProperty("failure")
        FAILURE;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    private STATUS status;
    private String message;
    private T data;
    private List<ApiError> errors;

    public static <T> ApiResponseBuilder<T> success() {
        return ApiResponse.<T>builder().status(STATUS.SUCCESS);
    }

    public static <T> ApiResponseBuilder<T> failure() {
        return ApiResponse.<T>builder().status(STATUS.FAILURE);
    }
}

