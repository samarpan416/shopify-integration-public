package com.uniware.integrations.exception;

import com.uniware.integrations.dto.HEADER_NAMES;

public class AuthenticationException extends ShopifyGlobalException {
    public AuthenticationException() {
        super(GlobalErrorCode.UNAUTHENTICATED, "Invalid " + HEADER_NAMES.USERNAME + " or " + HEADER_NAMES.PASSWORD);
    }

    public AuthenticationException(String message) {
        super(GlobalErrorCode.UNAUTHENTICATED, message);
    }
}
