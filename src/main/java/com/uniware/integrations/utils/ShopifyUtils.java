package com.uniware.integrations.utils;

import com.unifier.core.utils.StringUtils;

import java.util.Base64;

public class ShopifyUtils {

    public static String getBasicToken(String username, String password) {
        if(StringUtils.isBlank(username) || StringUtils.isBlank(password))
            throw  new IllegalArgumentException("Username and password cannot be blank");

        String stringToBeEncoded=username+':'+password;
        return Base64.getEncoder().encodeToString(stringToBeEncoded.getBytes());
    }
}
