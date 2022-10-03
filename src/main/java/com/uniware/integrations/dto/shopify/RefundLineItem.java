package com.uniware.integrations.dto.shopify;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.JsonNull;
import lombok.Data;

@Data
public class RefundLineItem {
	public enum RestockType {
		@JsonProperty("cancel")
		CANCEL,
		@JsonProperty("return")
		RETURN,
		@JsonProperty("no_restock")
		NO_RESTOCK,
		@JsonProperty("legacy_restock")
		LEGACY_RESTOCK
	}
	@JsonProperty("line_item_id")
	private Long lineItemId;

	@JsonProperty("quantity")
	private int quantity;

	@JsonProperty("line_item")
	private LineItem lineItem;

	@JsonProperty("subtotal")
	private double subtotal;

	@JsonProperty("total_tax_set")
	private TotalTaxSet totalTaxSet;

	@JsonProperty("id")
	private Long id;

	@JsonProperty("subtotal_set")
	private SubtotalSet subtotalSet;

	@JsonProperty("total_tax")
	private double totalTax;

	@JsonProperty("location_id")
	private Long locationId;

	@JsonProperty("restock_type")
	private RestockType restockType;
}