package com.uniware.integrations.dto.shopify;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
@Data
public class ShopifyAddress {

	@JsonProperty("phone")
	private String phone;

	@JsonProperty("first_name")
	private String firstName;
	
	@JsonProperty("last_name")
	private String lastName;

	@JsonProperty("zip")
	private String zip;

	@JsonProperty("country")
	private String country;

	@JsonProperty("city")
	private String city;

	@JsonProperty("address2")
	private String address2;

	@JsonProperty("address1")
	private String address1;

	@JsonProperty("province_code")
	private String provinceCode;

	@JsonProperty("country_code")
	private String countryCode;

	@JsonProperty("province")
	private String province;

	@JsonProperty("name")
	private String name;

	@JsonProperty("company")
	private String company;
}