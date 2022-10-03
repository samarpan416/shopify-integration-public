package com.uniware.integrations.clients;

import com.uniware.integrations.config.UniwareClientConfig;
import com.uniware.integrations.uniware.dto.saleOrder.request.CreateSaleOrderRequest;
import feign.HeaderMap;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;

@FeignClient(value = "uniware",configuration = {UniwareClientConfig.class})
public interface UniwareClient {
    @PostMapping(path = "/saleOrder/create",consumes = MediaType.APPLICATION_JSON_VALUE)
    Response createSaleOrder(@RequestBody CreateSaleOrderRequest createSaleOrderRequest, @HeaderMap HashMap<String,String> headers);
}
