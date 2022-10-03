package com.uniware.integrations.dto.shopify.error;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ShopifyError {

    @JsonProperty("errors")
    private String errors;
}