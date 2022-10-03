package com.uniware.integrations.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class GetSaleOrdersRequest {
    @NotNull @Valid
    private ConnectorParameters connectorParameters;
    @NotNull @Valid
    private ConfigurationParameters configurationParameters;
}
