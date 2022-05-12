package com.uniware.integrations.clients;

import com.uniware.integrations.config.ShopifyClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@FeignClient(value = "shopify",url = "https://xyz.com",configuration = {ShopifyClientConfig.class})
public interface ShopifyClient {
    @RequestMapping(method = RequestMethod.GET, value = "/users")
    List<Object> getStores();
    @RequestMapping(method = RequestMethod.GET, value = "/posts")
    List<Object> getPosts();
    @ResponseStatus(value =  HttpStatus.OK)
    @RequestMapping(method = RequestMethod.GET, value = "/orders.json")
    Object getOrders();
}
