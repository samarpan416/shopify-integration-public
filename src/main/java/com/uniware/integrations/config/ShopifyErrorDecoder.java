package com.uniware.integrations.config;

import com.google.gson.JsonElement;
import com.unifier.core.utils.JsonUtils;
import com.uniware.integrations.dto.shopify.error.ShopifyError;
import com.uniware.integrations.exception.AuthenticationException;
import com.uniware.integrations.exception.DataNotAvailable;
import feign.FeignException;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;

import java.util.List;

public class ShopifyErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        FeignException exception = feign.FeignException.errorStatus(methodKey, response);
        switch (response.status()) {
//          case 400:
//                return new BadRequestException();
            case 404:
                return new DataNotAvailable("Order not found");
            case 401:
                return new AuthenticationException();
            case 403:
                String errorMessage = exception.getMessage();
                if (exception.responseBody().isPresent()) {
                    ShopifyError shopifyError = JsonUtils.stringToJson(new String(exception.responseBody().get().array()), ShopifyError.class);
                    errorMessage = shopifyError.getErrors();
                }
                return new AuthenticationException(errorMessage);
            case 429:
                return new RetryableException(
                        response.status(),
                        exception.getMessage(),
                        response.request().httpMethod(),
                        exception,
                        null,
                        response.request());
            default:
                return exception;
        }
    }
}
