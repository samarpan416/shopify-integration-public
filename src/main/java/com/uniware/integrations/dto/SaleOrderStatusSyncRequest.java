package com.uniware.integrations.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class SaleOrderStatusSyncRequest {
    @NotEmpty
    @Valid
    List<StatusSyncSoi> statusSyncSaleOrderItems;
    @NotNull
    @Valid
    ShopifyOrderMetadata shopifyOrderMetadata;
}
