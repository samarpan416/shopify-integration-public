package com.uniware.integrations.exception;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShopifyGlobalException extends RuntimeException{
    private String code;
    private String message;
    public ShopifyGlobalException(String message) {
        super(message);
    }
}
