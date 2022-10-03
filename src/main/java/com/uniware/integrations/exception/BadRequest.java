package com.uniware.integrations.exception;

import com.uniware.integrations.dto.ApiError;
import lombok.*;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder
public class BadRequest extends RuntimeException {
    private String message;
    private List<ApiError> errors;
}
