package com.uniware.integrations.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


@DisplayName("When running ShopifyUtils")
class ShopifyUtilsTest {

    @DisplayName("getBasicToken")
    @Test
    void getBasicToken() {
        assertAll(
            ()->{
                String expected="YWJjZDE6MmVmZ2g=";
                String actual=ShopifyUtils.getBasicToken("abcd1","2efgh");
                assertEquals(expected,actual,"should return correct token");
            },
            ()->{
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,()->ShopifyUtils.getBasicToken(null,"2efgh"),"Throw error if username or/and password is blank -->");
                assertEquals("Username and password cannot be blank", exception.getMessage(),"should throw an error");
            });
    }
}