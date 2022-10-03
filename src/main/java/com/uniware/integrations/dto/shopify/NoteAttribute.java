package com.uniware.integrations.dto.shopify;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class NoteAttribute{

	@JsonProperty("name")
	private String name;

	@JsonProperty("value")
	private String value;
}