package com.uniware.integrations.dto.shopify;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Fulfillment {

	@JsonProperty("created_at")
	private String createdAt;

	@JsonProperty("origin_address")
	private OriginAddress originAddress;

	@JsonProperty("tracking_company")
	private String trackingCompany;

	@JsonProperty("line_items")
	private List<LineItemsItem> lineItems;

	@JsonProperty("tracking_urls")
	private List<String> trackingUrls;

	@JsonProperty("location_id")
	private Long locationId;

	@JsonProperty("updated_at")
	private String updatedAt;

	@JsonProperty("service")
	private String service;

	@JsonProperty("admin_graphql_api_id")
	private String adminGraphqlApiId;

	@JsonProperty("name")
	private String name;

	@JsonProperty("tracking_number")
	private String trackingNumber;

	@JsonProperty("receipt")
	private Receipt receipt;

	@JsonProperty("id")
	private Long id;

	@JsonProperty("tracking_numbers")
	private List<String> trackingNumbers;

	@JsonProperty("order_id")
	private Long orderId;

	@JsonProperty("tracking_url")
	private String trackingUrl;

	@JsonProperty("shipment_status")
	private Object shipmentStatus;

	@JsonProperty("status")
	private String status;
}