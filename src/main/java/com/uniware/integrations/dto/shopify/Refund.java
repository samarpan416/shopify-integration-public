package com.uniware.integrations.dto.shopify;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Refund {

	@JsonProperty("note")
	private String note;

	@JsonProperty("order_adjustments")
	private List<Object> orderAdjustments;

	@JsonProperty("created_at")
	private String createdAt;

	@JsonProperty("restock")
	private boolean restock;

	@JsonProperty("total_duties_set")
	private TotalDutiesSet totalDutiesSet;

	@JsonProperty("total_additional_fees_set")
	private TotalAdditionalFeesSet totalAdditionalFeesSet;

	@JsonProperty("refund_line_items")
	private List<RefundLineItem> refundLineItems;

	@JsonProperty("additional_fees")
	private List<Object> additionalFees;

	@JsonProperty("user_id")
	private Long userId;

	@JsonProperty("admin_graphql_api_id")
	private String adminGraphqlApiId;

	@JsonProperty("processed_at")
	private String processedAt;

	@JsonProperty("id")
	private Long id;

	@JsonProperty("order_id")
	private Long orderId;

	@JsonProperty("duties")
	private List<Object> duties;
}