package com.uniware.integrations.dto.shopify;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CurrentTotalTaxSet{

	@JsonProperty("shop_money")
	private ShopMoney shopMoney;

	@JsonProperty("presentment_money")
	private PresentmentMoney presentmentMoney;
}