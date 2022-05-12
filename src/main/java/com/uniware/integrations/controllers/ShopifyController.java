package com.uniware.integrations.controllers;

import com.uniware.integrations.services.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("shopify")
public class ShopifyController {
    TestService testService;

    @Autowired
    public ShopifyController(TestService testService) {
        this.testService = testService;
    }

    @GetMapping(value="/test", produces = "application/json")
    public @ResponseBody Object getBook() {
        return testService.test();
    }

    @GetMapping(value="/posts", produces = "application/json")
    public @ResponseBody Object getPosts() {
        return testService.getPosts();
    }
    @GetMapping(value="/orders", produces = "application/json")
    public @ResponseBody Object getOrders() {
        return testService.getOrders();
    }
}
