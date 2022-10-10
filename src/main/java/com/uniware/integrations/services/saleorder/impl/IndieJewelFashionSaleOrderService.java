package com.uniware.integrations.services.saleorder.impl;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.uniware.integrations.clients.ShopifyClient;
import com.uniware.integrations.dto.shopify.Order;
import com.uniware.integrations.utils.ShopifyUtils;

@Service
@Qualifier("indieJewelFashionSaleOrderService")
public class IndieJewelFashionSaleOrderService extends BaseSaleOrderService {
    public IndieJewelFashionSaleOrderService(ShopifyClient shopifyClient) {
        super(shopifyClient);
    }

    @Override
    public String getPaymentMode(Order order) {
        String paymentMode = super.getPaymentMode(order);
        // TODO: Keep these tags in mongo for easy configuration
        if (ShopifyUtils.containsAnyIgnoreCase(order.getTags(), "GIVA_App")
                && !ShopifyUtils.containsAnyIgnoreCase(order.getTags(), "App_Prepaid")) {
            paymentMode = "cod";
        }
        return paymentMode;
    }
}
