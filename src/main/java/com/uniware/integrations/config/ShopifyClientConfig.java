package com.uniware.integrations.config;

import com.uniware.integrations.contexts.ShopifyRequestContext;
import feign.Logger;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import feign.okhttp.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class ShopifyClientConfig {
    @Value("${shopify.api.version}")
    private String apiVersion;

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
//            pick from request context
            String target="https://"+ShopifyRequestContext.current().getHostname()+"/admin";
            if(requestTemplate.url().equals("/access_scopes.json"))
                target+="/oauth";
            else
                target+="/api/"+apiVersion;
            requestTemplate.target(target);
            requestTemplate.headers(ShopifyRequestContext.current().getHeaders());
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

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(100L, TimeUnit.SECONDS.toMillis(3L), 5);
    }

//    @Bean Decoder decoder() {
//        return new ShopifyResponseDecoder();
//    }
}