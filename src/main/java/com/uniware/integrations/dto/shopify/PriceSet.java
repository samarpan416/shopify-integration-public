package com.uniware.integrations.dto.shopify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PriceSet{

	@JsonProperty("shop_money")
	private ShopMoney shopMoney;

	@JsonProperty("presentment_money")
	private PresentmentMoney presentmentMoney;
}