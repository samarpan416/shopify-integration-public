package com.uniware.integrations.dto.shopify;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClientDetails{

	@JsonProperty("session_hash")
	private Object sessionHash;

	@JsonProperty("accept_language")
	private Object acceptLanguage;

	@JsonProperty("browser_width")
	private Object browserWidth;

	@JsonProperty("browser_height")
	private Object browserHeight;

	@JsonProperty("browser_ip")
	private String browserIp;

	@JsonProperty("user_agent")
	private Object userAgent;
}