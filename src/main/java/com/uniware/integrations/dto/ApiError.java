package com.uniware.integrations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ApiError {
    private String fieldName;
    private String message;
}
