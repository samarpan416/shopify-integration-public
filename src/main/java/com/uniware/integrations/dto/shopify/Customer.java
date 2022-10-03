package com.uniware.integrations.dto.shopify;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
@Data
public class Customer{

	@JsonProperty("default_address")
	private ShopifyAddress defaultAddress;
}