package com.uniware.integrations.services.saleorder.impl;

import com.uniware.integrations.clients.ShopifyClient;
import com.uniware.integrations.dto.ConfigurationParameters;
import com.uniware.integrations.services.saleorder.impl.BaseSaleOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Qualifier("rustOrangeSaleOrderService")
public class RustOrangeSaleOrderService extends BaseSaleOrderService {

    @Autowired
    public RustOrangeSaleOrderService(ShopifyClient shopifyClient) {
        super(shopifyClient);
    }

    @Override
    public SPLIT_SHIPMENT_CONDITION getSplitShipmentCondition(ConfigurationParameters configurationParameters) {
        return SPLIT_SHIPMENT_CONDITION.DOMESTIC;
    }
}
