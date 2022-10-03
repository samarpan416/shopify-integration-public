package com.uniware.integrations.config;

import com.uniware.integrations.contexts.ShopifyRequestContext;
import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UniwareClientConfig {

//    @Bean
//    public RequestInterceptor requestInterceptor() {
//        return requestTemplate -> {
////            pick from request context
//            String target=ShopifyRequestContext.current().getHostname()+"/services/rest/v1/oms";
//            requestTemplate.target(target);
//            requestTemplate.headers(ShopifyRequestContext.current().getHeaders());
//        };
//    }
}
