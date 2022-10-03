package com.uniware.integrations.dto.shopify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DiscountApplication {

	@JsonProperty("allocation_method")
	private String allocationMethod;

	@JsonProperty("value_type")
	private String valueType;

	@JsonProperty("code")
	private String code;

	@JsonProperty("target_type")
	private String targetType;

	@JsonProperty("target_selection")
	private String targetSelection;

	@JsonProperty("type")
	private String type;

	@JsonProperty("value")
	private String value;
}