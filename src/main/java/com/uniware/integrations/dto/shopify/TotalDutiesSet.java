package com.uniware.integrations.dto.shopify;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TotalDutiesSet{

	@JsonProperty("shop_money")
	private ShopMoney shopMoney;

	@JsonProperty("presentment_money")
	private PresentmentMoney presentmentMoney;
}