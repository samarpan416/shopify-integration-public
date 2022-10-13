package com.uniware.integrations.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class ConnectorVerificationRequest extends ServiceRequest {
    @NotNull
    @Valid
    private ConnectorParameters connectorParameters;
}
