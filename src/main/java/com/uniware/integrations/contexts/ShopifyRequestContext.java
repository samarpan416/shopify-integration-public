package com.uniware.integrations.contexts;

import java.util.HashMap;

public class ShopifyRequestContext {

    private static final ThreadLocal<ShopifyRequestContext> ctx = new ThreadLocal<>();

    private String hostname;
    HashMap<String,String> headers;

    public ShopifyRequestContext(String hostname,HashMap<String,String> headers) {
        this.hostname=hostname;
        this.headers=headers;
    }
    public ShopifyRequestContext(ShopifyRequestContext shopifyRequestContext){
        setHostname(shopifyRequestContext.getHostname());
        setHeaders(shopifyRequestContext.getHeaders());
    }

    public static void setContext(ShopifyRequestContext shopifyRequestContext){
        ctx.set(new ShopifyRequestContext(shopifyRequestContext));
    }

    public static ShopifyRequestContext current() {
        ShopifyRequestContext requestContext = ctx.get();
        if (requestContext == null) {
            requestContext = new ShopifyRequestContext("xyz.com",new HashMap<>());
            ctx.set(requestContext);
        }
        return requestContext;
    }

    public static void destroy() {
        ctx.remove();
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(HashMap<String, String> headers) {
        this.headers = headers;
    }
}
