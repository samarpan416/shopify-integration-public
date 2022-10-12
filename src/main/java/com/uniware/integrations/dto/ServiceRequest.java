package com.uniware.integrations.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ConstraintViolation;

import com.uniware.integrations.exception.BadRequest;

public class ServiceRequest {

    private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    public void validate() throws BadRequest {
        validate(this);
    }

    public static <T> void validate(T input) throws BadRequest {
        Set<ConstraintViolation<T>> violations = validator.validate(input);
        if (!violations.isEmpty()) {
            List<ApiError> errors = new ArrayList<>();
            violations.forEach(e -> {
                errors.add(new ApiError(e.getPropertyPath().toString(), e.getMessage()));
            });
            throw BadRequest.builder().message("Invalid request body").errors(errors).build();
        }
    }
}
