package com.uniware.integrations.dto.shopify;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Receipt{

	@JsonProperty("authorization")
	private String authorization;

	@JsonProperty("testcase")
	private boolean testcase;
}