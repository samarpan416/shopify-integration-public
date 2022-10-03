package com.uniware.integrations.dto.uniware;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class Attribute {
    @NotBlank
    private String name;

    @NotBlank
    private String value;
}
