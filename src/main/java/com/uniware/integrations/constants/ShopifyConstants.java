package com.uniware.integrations.constants;

import java.util.Arrays;
import java.util.List;

public final class ShopifyConstants {
    public static final String DAYS_TO_FETCH_ORDERS_FOR="7";
    public static final String PAGE_SIZE = "250";
    public static final List<String> REQUIRED_ACCESS_SCOPES = Arrays.asList("read_locations", "read_products", "read_inventory", "write_inventory", "read_orders");
    private ShopifyConstants() {
    }
}
