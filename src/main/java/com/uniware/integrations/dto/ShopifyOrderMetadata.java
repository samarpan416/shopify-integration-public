package com.uniware.integrations.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uniware.integrations.LineItemMetadata;
import com.uniware.integrations.services.saleorder.impl.BaseSaleOrderService.UNIWARE_ORDER_ITEM_STATUS;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ShopifyOrderMetadata {
    private UNIWARE_ORDER_ITEM_STATUS status;
    private Map<String, LineItemMetadata> lineItemIdToMetadata = new HashMap<>();

    @JsonIgnore
    public boolean isCancelled() {
        return UNIWARE_ORDER_ITEM_STATUS.CANCELLED.equals(status);
    }

    @JsonIgnore
    public boolean isFullfilled() {
        return UNIWARE_ORDER_ITEM_STATUS.DISPATCHED.equals(status);
    }

    public Map<String, LineItemMetadata> getLineItemIdToMetadata() {
        if (lineItemIdToMetadata == null) {
            lineItemIdToMetadata = new HashMap<>();
        }
        return lineItemIdToMetadata;
    }
}
