package com.uniware.integrations.dto.shopify;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.uniware.integrations.dto.shopify.bulk.UserError;
import lombok.Data;

import java.util.List;

@Data
public class InventoryBulkAdjustQuantityAtLocationPayload {

    List<InventoryLevelGQL> inventoryLevels;
    List<UserError> userErrors;
}
