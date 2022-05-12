package com.uniware.integrations.services;

import com.uniware.integrations.clients.ShopifyClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;

@Service
public class TestService {
    ShopifyClient shopifyClient;

    @Autowired
    public TestService(ShopifyClient shopifyClient) {
        this.shopifyClient = shopifyClient;
    }

    public List<Object> test() {
        URI determinedBasePathUri = URI.create("https://gorest.co.in/public/v2");
        return shopifyClient.getStores();
    }
    public List<Object> getPosts() {
//        URI determinedBasePathUri = URI.create("https://gorest.co.in/public/v2");
        return shopifyClient.getPosts();
    }
    public Object getOrders() {
//        URI determinedBasePathUri = URI.create("https://gorest.co.in/public/v2");
        return shopifyClient.getOrders();
    }
}
