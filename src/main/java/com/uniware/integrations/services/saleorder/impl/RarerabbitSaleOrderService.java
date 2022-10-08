package com.uniware.integrations.services.saleorder.impl;

import com.uniware.integrations.clients.ShopifyClient;
import com.uniware.integrations.dto.shopify.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
@Service
@Qualifier("rarerabbitSaleOrderService")
public class RarerabbitSaleOrderService extends BaseSaleOrderService {
    public RarerabbitSaleOrderService(ShopifyClient shopifyClient) {
        super(shopifyClient);
    }

    @Override
    public BigDecimal prepareDiscount(Order order, LineItem lineItem) {
        BigDecimal lineItemDiscount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
        for (DiscountAllocation discountAllocation : lineItem.getDiscountAllocations()) {
            DiscountApplication discountApplication = order.getDiscountApplications().get(discountAllocation.getDiscountApplicationIndex());
            BigDecimal discountAllocationAmount = new BigDecimal(discountAllocation.getAmount());
            if (discountApplication.getCode().contains("FLITS")) {
                order.addFlitsDiscountCodeAmount(discountAllocationAmount);
            } else {
                lineItemDiscount = lineItemDiscount.add(discountAllocationAmount);
            }
        }
        return lineItemDiscount;
    }

    @Override
    public BigDecimal getPrepaidAmount(Order order, BigDecimal shippingCharges, BigDecimal giftDiscount) {
        BigDecimal prepaidAmount = super.getPrepaidAmount(order, shippingCharges, giftDiscount);
        prepaidAmount = prepaidAmount.add(order.getFlitsDiscountCodeAmount());
        return prepaidAmount;
    }
}
