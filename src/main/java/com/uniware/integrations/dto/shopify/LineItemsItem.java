package com.uniware.integrations.dto.shopify;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LineItemsItem{

	@JsonProperty("variant_title")
	private String variantTitle;

	@JsonProperty("fulfillment_status")
	private Object fulfillmentStatus;

	@JsonProperty("total_discount")
	private String totalDiscount;

	@JsonProperty("gift_card")
	private boolean giftCard;

	@JsonProperty("requires_shipping")
	private boolean requiresShipping;

	@JsonProperty("total_discount_set")
	private TotalDiscountSet totalDiscountSet;

	@JsonProperty("title")
	private String title;

	@JsonProperty("product_exists")
	private boolean productExists;

	@JsonProperty("variant_id")
	private int variantId;

	@JsonProperty("tax_lines")
	private List<TaxLine> taxLines;

	@JsonProperty("price")
	private String price;

	@JsonProperty("vendor")
	private Object vendor;

	@JsonProperty("product_id")
	private int productId;

	@JsonProperty("id")
	private int id;

	@JsonProperty("grams")
	private int grams;

	@JsonProperty("sku")
	private String sku;

	@JsonProperty("fulfillable_quantity")
	private int fulfillableQuantity;

	@JsonProperty("quantity")
	private int quantity;

	@JsonProperty("fulfillment_service")
	private String fulfillmentService;

	@JsonProperty("taxable")
	private boolean taxable;

	@JsonProperty("variant_inventory_management")
	private String variantInventoryManagement;

	@JsonProperty("discount_allocations")
	private List<DiscountAllocation> discountAllocations;

	@JsonProperty("admin_graphql_api_id")
	private String adminGraphqlApiId;

	@JsonProperty("name")
	private String name;

	@JsonProperty("price_set")
	private PriceSet priceSet;

	@JsonProperty("properties")
	private List<Property> properties;

	@JsonProperty("duties")
	private List<Object> duties;
}