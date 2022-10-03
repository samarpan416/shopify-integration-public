package com.uniware.integrations.dto.shopify;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.uniware.integrations.dto.uniware.Attribute;
import com.uniware.integrations.dto.uniware.UpdateInventoryRequest;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class InventoryItemAdjustment {

    @JsonProperty("inventoryItemId")
    String gqlId;

    @JsonProperty("availableDelta")
    Integer availableDelta;

    @JsonIgnore
    String channelProductId;

    public InventoryItemAdjustment() {
    }

    public InventoryItemAdjustment(@NotNull UpdateInventoryRequest.InventoryUpdateItem inventoryUpdateItem) {
        this.gqlId = inventoryUpdateItem.getAttributeValue("inventoryItemGraphQlId");
        this.availableDelta = inventoryUpdateItem.getQuantity();
        this.channelProductId = inventoryUpdateItem.getChannelProductId();
    }
}
