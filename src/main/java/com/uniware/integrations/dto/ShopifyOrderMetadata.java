package com.uniware.integrations.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uniware.integrations.LineItemMetadata;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ShopifyOrderMetadata {
    private boolean orderCancelled;
    private String fulfillmentStatus;
    private Map<String, LineItemMetadata> lineItemIdToMetadata = new HashMap<>();

    @JsonIgnore
    public boolean isOrderFulfilled() {
        return "fulfilled".equalsIgnoreCase(fulfillmentStatus);
    }
    public Map<String, LineItemMetadata> getLineItemIdToMetadata() {
        if (lineItemIdToMetadata == null) {
            lineItemIdToMetadata = new HashMap<>();
        }
        return lineItemIdToMetadata;
    }
}
