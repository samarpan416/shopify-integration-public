package com.uniware.integrations.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class ConnectorParameters {
    @NotEmpty
    private String apiKey;
    @NotEmpty
    @Pattern(regexp = "^([a-zA-Z\\d\\-]*)\\.myshopify.com",message = "is invalid")
    private String hostname;
    @NotEmpty
    private String password;
    private String accessToken;
    private String prefix;
    @NotEmpty
    private String locationId;
}
