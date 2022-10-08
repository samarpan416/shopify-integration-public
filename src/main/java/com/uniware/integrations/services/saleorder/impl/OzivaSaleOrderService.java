package com.uniware.integrations.services.saleorder.impl;

import com.uniware.integrations.clients.ShopifyClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Qualifier("ozivaSaleOrderService")
public class OzivaSaleOrderService extends BaseSaleOrderService {

    public OzivaSaleOrderService(ShopifyClient shopifyClient) {
        super(shopifyClient);
    }
}
