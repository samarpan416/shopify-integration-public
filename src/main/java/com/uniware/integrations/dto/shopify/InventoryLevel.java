package com.uniware.integrations.dto.shopify;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class InventoryLevel {

    @JsonAlias("inventory_item_id")
    String inventoryItemId;

    @JsonAlias("location_id")
    String locationId;

    Integer available;

    @JsonAlias("updated_at")
    String updatedAt;

    @JsonAlias("admin_graphql_api_id")
    String gqlId;
}
