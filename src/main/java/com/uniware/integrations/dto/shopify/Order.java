package com.uniware.integrations.dto.shopify;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.uniware.integrations.dto.ApiError;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.*;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    public enum FulfillmentStatus {
        @JsonProperty("fulfilled")
        FULFILLED,
        @JsonProperty("partial")
        PARTIAL,
        @JsonProperty("restocked")
        RESTOCKED;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    public enum FinancialStatus {
        @JsonProperty("pending")
        PENDING,
        @JsonProperty("authorized")
        AUTHORIZED,
        @JsonProperty("partially_paid")
        PARTIALLY_PAID,
        @JsonProperty("paid")
        PAID,
        @JsonProperty("partially_refunded")
        PARTIALLY_REFUNDED,
        @JsonProperty("refunded")
        REFUNDED,
        @JsonProperty("voided")
        VOIDED;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    @JsonProperty("id")
    private Long id;
    @JsonProperty("line_items")
    private List<LineItem> lineItems;
    @JsonProperty("discount_codes")
    private List<DiscountCode> discountCodes;
    @JsonProperty("discount_applications")
    private List<DiscountApplication> discountApplications;
    @JsonProperty("name")
    private String name;
    @JsonProperty("order_number")
    private Long orderNumber;
    @JsonProperty("location_id")
    private Long locationId;
    @JsonProperty("checkout_id")
    private String checkoutId;
    @JsonProperty("payment_gateway_names")
    private List<String> paymentGatewayNames;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("shopMoney")
    private ShopMoney shopMoney;
    @JsonProperty("email")
    private String email;
    @JsonProperty("currency")
    private String currency;
    @JsonProperty("note_attributes")
    private List<NoteAttribute> noteAttributes;
    @JsonProperty("current_total_discounts")
    private String currentTotalDiscounts;
    @JsonProperty("total_discounts")
    private String totalDiscounts;
    @JsonProperty("shipping_lines")
    private List<ShippingLine> shippingLines;
    @JsonProperty("total_line_items_price")
    private String totalLineItemsPrice;
    @JsonProperty("customer")
    private Customer customer;
    @JsonProperty("shipping_address")
    private ShopifyAddress shippingAddress;
    @JsonProperty("billing_address")
    private ShopifyAddress billingAddress;
    @JsonProperty("admin_graphql_api_id")
    private String adminGraphqlApiId;
    @JsonProperty("confirmed")
    private boolean confirmed;
    @JsonProperty("fulfillment_status")
    private FulfillmentStatus fulfillmentStatus;
    @JsonProperty("financial_status")
    private FinancialStatus financialStatus;
    @JsonProperty("tags")
    private String tags;
    @JsonProperty("cancelled_at")
    private String cancelledAt;
    @JsonProperty("refunds")
    private List<Refund> refunds;
    @JsonProperty("taxes_included")
    private boolean taxesIncluded;
    @JsonIgnore
    private List<Transaction> transactions;
    @JsonProperty("gateway")
    private String gateway;
    @JsonProperty("source_name")
    private String sourceName;
    @JsonIgnore
    private BigDecimal prepaidDiscountCodeAmount = BigDecimal.ZERO;

    public ShopifyAddress getBillingAddress() {
        if (this.billingAddress != null) return this.billingAddress;
        ShopifyAddress customerDefaultAddress = this.getCustomerDefaultAddress();
        return customerDefaultAddress != null ? customerDefaultAddress : this.getShippingAddress();
    }

    @JsonIgnore
    public ShopifyAddress getCustomerDefaultAddress() {
        return this.getCustomer() != null ? this.getCustomer().getDefaultAddress() : null;
    }

    public ShopifyAddress getShippingAddress() {
        return this.shippingAddress != null ? this.shippingAddress : this.getCustomerDefaultAddress();
    }

    @JsonIgnore
    public String getProvinceCode() {
        ShopifyAddress shippingAddress = this.getShippingAddress();
        ShopifyAddress billingAddress = this.getBillingAddress();
        if (shippingAddress != null) return shippingAddress.getProvinceCode();
        if (billingAddress != null) return billingAddress.getProvinceCode();
        return null;
    }

    @JsonIgnore
    public List<ApiError> validate() {
        List<ApiError> errors = new ArrayList<>();
        if (this.shippingAddress == null && this.billingAddress == null && this.getCustomerDefaultAddress() == null) {
            errors.add(ApiError.builder().message("shippingAddress, billingAddress and customerDefaultAddress are null").build());
        }
        return errors;
    }

    @JsonIgnore
    public BigDecimal addPrepaidDiscountCodeAmount(BigDecimal amount) {
        return this.prepaidDiscountCodeAmount.add(amount);
    }
}
