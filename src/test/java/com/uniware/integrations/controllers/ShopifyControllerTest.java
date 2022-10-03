package com.uniware.integrations.controllers;

import com.uniware.integrations.clients.ShopifyClient;
import com.uniware.integrations.services.saleorder.impl.BaseSaleOrderService;
import com.uniware.integrations.services.saleorder.impl.RustOrangeSaleOrderService;
import com.uniware.integrations.services.saleorder.SaleOrderServiceFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("In ShopifyController")
@ExtendWith(MockitoExtension.class)
@WebMvcTest(ShopifyController.class)
class ShopifyControllerTest {

    @Autowired
    ApplicationContext context;

    @Autowired
    private MockMvc mvc;

    @MockBean
    SaleOrderServiceFactory saleOrderServiceFactory;

    @MockBean
    ShopifyClient shopifyClient;

    @MockBean(name="baseSaleOrderService")
    BaseSaleOrderService baseSaleOrderService;

    @MockBean(name="rustOrangeSaleOrderService")
    RustOrangeSaleOrderService rustOrangeSaleOrderService;

    @DisplayName("/shopify/orders")
    @Test
    void getOrders() throws Exception {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("hostname", "wow-yet-another-test-store.myshopify.com");
        httpHeaders.add("location_id", "61569237174");
        httpHeaders.add("username", "5313621c60e20215c33ee1015ee367f8");
        httpHeaders.add("password", "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");
        httpHeaders.add("tenantCode", "varad");
        mvc.perform(MockMvcRequestBuilders.get("/shopify/orders").headers(httpHeaders)).andDo(print()).andExpect(status().is4xxClientError());
    }

//    @Test
//    void getOrder() {
//    }
//
//    @Test
//    void getOrderTransactions() {
//    }
//
//    @Test
//    void getPendencies() {
//    }
//
//    @Test
//    void reconciliation() {
//    }
//
//    @Test
//    void statusSyncMetadata() {
//    }
}