package com.uniware.integrations.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class GetSaleOrdersRequest extends ServiceRequest {
    @NotNull
    @Valid
    private ConnectorParameters connectorParameters;
    @NotNull
    @Valid
    private ConfigurationParameters configurationParameters;
    @NotNull
    @Valid
    private TenantSpecificConfigurations tenantSpecificConfigurations;
}
