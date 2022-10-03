package com.uniware.integrations.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pendency {
    private String channelProductId;
    private String sellerSkuCode;
    private String productName;
    private int quantity;
}
