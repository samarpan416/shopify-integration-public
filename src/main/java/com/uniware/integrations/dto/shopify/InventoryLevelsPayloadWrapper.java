package com.uniware.integrations.dto.shopify;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.util.List;

@Data
public class InventoryLevelsPayloadWrapper {
    @JsonAlias("inventory_levels")
    List<InventoryLevel> inventoryLevels;
}
