package com.uniware.integrations.services.saleorder;

import com.uniware.integrations.services.saleorder.impl.BaseSaleOrderService;
import com.uniware.integrations.services.saleorder.impl.RustOrangeSaleOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class SaleOrderServiceFactory {
    BaseSaleOrderService baseSaleOrderService;
    RustOrangeSaleOrderService rustOrangeSaleOrderService;

    @Autowired
    public SaleOrderServiceFactory(@Qualifier("baseSaleOrderService") BaseSaleOrderService baseSaleOrderService, @Qualifier("rustOrangeSaleOrderService") RustOrangeSaleOrderService rustOrangeSaleOrderService) {
        this.baseSaleOrderService = baseSaleOrderService;
        this.rustOrangeSaleOrderService = rustOrangeSaleOrderService;
    }

    public ISaleOrderService getService(String tenantCode) {
        if ("rustorange".equalsIgnoreCase(tenantCode)) {
            return rustOrangeSaleOrderService;
        }
        return baseSaleOrderService;
    }
}
