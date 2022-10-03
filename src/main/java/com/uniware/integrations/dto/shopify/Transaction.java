package com.uniware.integrations.dto.shopify;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Transaction {
	@JsonProperty("id")
	private int id;

	@JsonProperty("test")
	private boolean test;

	@JsonProperty("kind")
	private String kind;

	@JsonProperty("created_at")
	@JsonAlias({"created_at", "createdAt"})
	private String createdAt;

	@JsonProperty("message")
	private Object message;
	// TODO: Removed json prop where not required. Use JsonAlias only
	@JsonProperty("error_code")
	@JsonAlias({"error_code", "errorCode"})
	private Object errorCode;

	@JsonProperty("receipt")
	private String receipt;

	@JsonProperty("gateway")
	private String gateway;

	@JsonProperty("status")
	private String status;

	@JsonProperty("amountSet")
	@JsonAlias({"amountSet", "amount_set"})
	private AmountSet amountSet;

}