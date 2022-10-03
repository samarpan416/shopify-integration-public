package com.uniware.integrations.exception;

import com.uniware.integrations.dto.ApiResponse;
import feign.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BadRequest.class)
    public final ResponseEntity<ApiResponse> badRequest(BadRequest ex, WebRequest request) {
        ApiResponse exceptionResponse = ApiResponse.failure().message(ex.getMessage()).errors(ex.getErrors()).build();
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    public final ResponseEntity<ApiResponse> dataNotAvailable(AuthenticationException ex, WebRequest request) {
        ApiResponse exceptionResponse = ApiResponse.failure().message(ex.getMessage()).build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exceptionResponse);
    }

    @ExceptionHandler(FailureResponse.class)
    public final ResponseEntity<ApiResponse> handleFailureResponse(FailureResponse ex, WebRequest request) {
        LOG.error("FailureResponse message :-  {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ApiResponse.failure().message(ex.getMessage()).build());
    }
    @ExceptionHandler(NotFoundException.class)
    public final ResponseEntity<NotFoundException> handleNotFoundException(NotFoundException ex, WebRequest request) {
        LOG.error("NotFoundException message :-  {}", ex.getMessage());
        return new ResponseEntity<>(ex,HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ApiResponse> handleAllExceptions(Exception ex, WebRequest request) {
        LOG.error("Something went wrong :-  {}", ex.getMessage(), ex);
        ApiResponse exceptionResponse = ApiResponse.failure().message(ex.getMessage()).build();
        return ResponseEntity.internalServerError().body(exceptionResponse);
    }
}