package com.uniware.integrations.dto.shopify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DiscountCode {

	@JsonProperty("amount")
	private String amount;

	@JsonProperty("code")
	private String code;

	@JsonProperty("type")
	private String type;
}