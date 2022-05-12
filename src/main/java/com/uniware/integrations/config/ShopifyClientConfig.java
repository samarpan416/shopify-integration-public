package com.uniware.integrations.config;

import com.uniware.integrations.contexts.ShopifyRequestContext;
import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import feign.okhttp.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShopifyClientConfig {
    @Value("${shopify.api.version}")
    private String apiVersion;

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.HEADERS;
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
//            pick from request context
            requestTemplate.target("https://"+ShopifyRequestContext.current().getHostname()+"/admin/api/"+apiVersion);
//            requestTemplate.headers(ShopifyRequestContext.current().getHeaders());
            requestTemplate.header("Authorization","Basic NTMxMzYyMWM2MGUyMDIxNWMzM2VlMTAxNWVlMzY3Zjg6c2hwYXRfNDY0YTExZmU1YzkyMzEyMzFiMjY0ZDk3Zjc0ZWQxZjQ=");
        };
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new ShopifyErrorDecoder();
    }

    @Bean
    public OkHttpClient client() {
        return new OkHttpClient();
    }
}