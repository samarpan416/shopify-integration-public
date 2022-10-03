package com.uniware.integrations.dto.shopify;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ShopMoney{

	@JsonProperty("amount")
	private String amount;

	@JsonProperty("currency_code")
	@JsonAlias({"currency_code", "currencyCode"})
	private String currencyCode;
}