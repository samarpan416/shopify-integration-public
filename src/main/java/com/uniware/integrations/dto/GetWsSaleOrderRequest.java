package com.uniware.integrations.dto;

import com.uniware.integrations.dto.shopify.Order;
import com.uniware.integrations.exception.BadRequest;

import lombok.Data;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class GetWsSaleOrderRequest extends ServiceRequest {
    @NotEmpty
    String orderId;
    @NotNull @Valid
    Order order;
    @NotNull @Valid
    ConfigurationParameters configurationParameters;
    @NotNull @Valid
    ConnectorParameters connectorParameters;

    @Override
    public void validate() throws BadRequest {
        validate(this);
        List<ApiError> validationErrors = this.order.validate();
        if (!validationErrors.isEmpty()) {
            throw BadRequest.builder().errors(validationErrors).message("Invalid order").build();
        }
    }
}
