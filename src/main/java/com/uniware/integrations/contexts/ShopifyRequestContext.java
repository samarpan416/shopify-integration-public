package com.uniware.integrations.contexts;

import lombok.ToString;

import java.util.*;

@ToString
public class ShopifyRequestContext {
    private static final ThreadLocal<ShopifyRequestContext> ctx = new ThreadLocal<>();

    private String hostname;
    private String tenantCode;
    private String locationId;
    Map<String, Collection<String>> headers = new HashMap<>();

    public static ShopifyRequestContext current() {
        ShopifyRequestContext requestContext = ctx.get();
        if (requestContext == null) {
            requestContext = new ShopifyRequestContext();
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

    public Map<String, Collection<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, Collection<String>> headers) {
        this.headers = headers;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }
}
