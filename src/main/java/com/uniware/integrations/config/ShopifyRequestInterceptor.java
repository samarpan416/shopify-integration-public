package com.uniware.integrations.config;

import com.uniware.integrations.contexts.ShopifyRequestContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ShopifyRequestInterceptor implements RequestInterceptor {
    @Value("${shopify.api.version}")
    private String apiVersion;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.target("https://"+ ShopifyRequestContext.current().getHostname()+"/admin/api/"+apiVersion);
        requestTemplate.header("Authorization","Basic NTMxMzYyMWM2MGUyMDIxNWMzM2VlMTAxNWVlMzY3Zjg6c2hwYXRfNDY0YTExZmU1YzkyMzEyMzFiMjY0ZDk3Zjc0ZWQxZjQ=");
    }
}