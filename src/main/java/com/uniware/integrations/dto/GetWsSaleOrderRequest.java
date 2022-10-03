package com.uniware.integrations.dto;

import com.uniware.integrations.dto.shopify.Order;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class GetWsSaleOrderRequest {
    @NotEmpty
    String orderId;
    @NotNull
    @Valid
    Order order;
    @NotNull
    @Valid
    ConfigurationParameters configurationParameters;
    @NotNull
    @Valid
    ConnectorParameters connectorParameters;
}
