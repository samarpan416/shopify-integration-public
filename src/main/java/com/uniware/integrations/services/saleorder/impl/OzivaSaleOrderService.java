package com.uniware.integrations.services.saleorder.impl;

import com.unifier.core.utils.StringUtils;
import com.uniware.integrations.clients.ShopifyClient;
import com.uniware.integrations.dto.ConfigurationParameters;
import com.uniware.integrations.dto.ConnectorParameters;
import com.uniware.integrations.dto.shopify.Order;
import com.uniware.integrations.utils.ShopifyUtils;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Qualifier("ozivaSaleOrderService")
public class OzivaSaleOrderService extends BaseSaleOrderService {

    public OzivaSaleOrderService(ShopifyClient shopifyClient) {
        super(shopifyClient);
    }

    @Override
    public boolean shouldFetchOrder(Order order, ConfigurationParameters configurationParameters,
            ConnectorParameters connectorParameters) {
        boolean shouldFetchOrder = super.shouldFetchOrder(order, configurationParameters, connectorParameters);
        if(shouldFetchOrder) {
            String paymentMode = getPaymentMode(order);
            if("cod".equals(paymentMode) && !StringUtils.equalsIngoreCaseAny(order.getTags(), "COD_CONFIRM", "COD_CONFIRM_SPECIAL", "COD_CONFIRM_MANUAL")) {
                shouldFetchOrder = false;
            }
        }
        return shouldFetchOrder;
    }

    @Override
    public String getPaymentMode(Order order) {
        String paymentMode = super.getPaymentMode(order);
        if (ShopifyUtils.containsAnyIgnoreCase(order.getTags(), "COD_CONFIRM_SPECIAL", "COD_CONFIRM")) {
            paymentMode = "cod";
        }
        return paymentMode;
    }
}
