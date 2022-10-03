package com.uniware.integrations.dto.shopify;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AmountSet{

	@JsonProperty("shop_money")
	@JsonAlias({"shop_money", "shopMoney"})
	private ShopMoney shopMoney;

	@JsonProperty("presentment_money")
	@JsonAlias({"presentment_money", "presentmentMoney"})
	private PresentmentMoney presentmentMoney;
}