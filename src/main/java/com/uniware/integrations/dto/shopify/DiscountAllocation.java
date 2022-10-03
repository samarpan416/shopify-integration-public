package com.uniware.integrations.dto.shopify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DiscountAllocation{

	@JsonProperty("amount")
	private String amount;

	@JsonProperty("discount_application_index")
	private int discountApplicationIndex;

	@JsonProperty("amount_set")
	private AmountSet amountSet;
}