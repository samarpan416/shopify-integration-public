package com.uniware.integrations.dto.shopify;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentDetails{

	@JsonProperty("credit_card_wallet")
	private Object creditCardWallet;

	@JsonProperty("credit_card_number")
	private String creditCardNumber;

	@JsonProperty("avs_result_code")
	private Object avsResultCode;

	@JsonProperty("credit_card_expiration_month")
	private Object creditCardExpirationMonth;

	@JsonProperty("credit_card_expiration_year")
	private Object creditCardExpirationYear;

	@JsonProperty("cvv_result_code")
	private Object cvvResultCode;

	@JsonProperty("credit_card_bin")
	private Object creditCardBin;

	@JsonProperty("credit_card_name")
	private Object creditCardName;

	@JsonProperty("credit_card_company")
	private String creditCardCompany;
}