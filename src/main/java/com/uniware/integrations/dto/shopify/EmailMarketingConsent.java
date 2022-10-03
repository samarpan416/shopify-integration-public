package com.uniware.integrations.dto.shopify;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EmailMarketingConsent{

	@JsonProperty("consent_updated_at")
	private String consentUpdatedAt;

	@JsonProperty("state")
	private String state;

	@JsonProperty("opt_in_level")
	private Object optInLevel;
}