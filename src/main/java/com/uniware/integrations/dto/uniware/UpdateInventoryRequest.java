package com.uniware.integrations.dto.uniware;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uniware.integrations.dto.ServiceRequest;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class UpdateInventoryRequest extends ServiceRequest {
    List<InventoryUpdateItem> inventoryUpdateItems;

    @Data
    public static class InventoryUpdateItem {
        String channelProductId;
        String channelSkuCode;
        List<Attribute> attributes;
        Integer quantity;

        @JsonIgnore
        public String getAttributeValue(@NotNull String name) {
            for (Attribute attribute : attributes) {
                if (name.equals(attribute.getName())) {
                    return attribute.getValue();
                }
            }
            return null;
        }
    }
}
