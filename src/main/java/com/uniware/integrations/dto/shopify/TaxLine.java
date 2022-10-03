package com.uniware.integrations.dto.shopify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TaxLine {
	@JsonProperty("price")
	private String price;

	@JsonProperty("price_set")
	private PriceSet priceSet;

	@JsonProperty("title")
	private String title;
}