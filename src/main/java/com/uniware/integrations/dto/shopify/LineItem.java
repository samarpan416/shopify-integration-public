package com.uniware.integrations.dto.shopify;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class LineItem{

	@JsonProperty("id")
	private Long id;

	@JsonProperty("name")
	private String name;

	@JsonProperty("sku")
	private String sku;

	@JsonProperty("product_id")
	private Long productId;

	@JsonProperty("variant_id")
	private Long variantId;

	@JsonProperty("quantity")
	private int quantity;

	@JsonProperty("fulfillable_quantity")
	private int fulfillableQuantity;

	@JsonProperty("price")
	private String price;

	@JsonProperty("price_set")
	private PriceSet priceSet;

	@JsonProperty("total_discount")
	private String totalDiscount;

	@JsonProperty("fulfillment_status")
	private String fulfillmentStatus;

	@JsonProperty("tax_lines")
	private List<TaxLine> taxLines;

	@JsonProperty("discount_allocations")
	private List<DiscountAllocation> discountAllocations;

	@JsonProperty("properties")
	private List<Property> properties;
}