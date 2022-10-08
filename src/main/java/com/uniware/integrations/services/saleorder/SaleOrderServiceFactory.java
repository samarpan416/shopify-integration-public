package com.uniware.integrations.services.saleorder;

import com.uniware.integrations.services.saleorder.impl.BaseSaleOrderService;
import com.uniware.integrations.services.saleorder.impl.OzivaSaleOrderService;
import com.uniware.integrations.services.saleorder.impl.RarerabbitSaleOrderService;
import com.uniware.integrations.services.saleorder.impl.RustOrangeSaleOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class SaleOrderServiceFactory {
    BaseSaleOrderService baseSaleOrderService;
    RustOrangeSaleOrderService rustOrangeSaleOrderService;
    RarerabbitSaleOrderService rarerabbitSaleOrderService;
    OzivaSaleOrderService ozivaSaleOrderService;

    @Autowired
    public SaleOrderServiceFactory(@Qualifier("baseSaleOrderService") BaseSaleOrderService baseSaleOrderService,
                                   @Qualifier("rustOrangeSaleOrderService") RustOrangeSaleOrderService rustOrangeSaleOrderService,
                                   @Qualifier("rarerabbitSaleOrderService") RarerabbitSaleOrderService rarerabbitSaleOrderService,
                                   @Qualifier("ozivaSaleOrderService") OzivaSaleOrderService ozivaSaleOrderService) {
        this.baseSaleOrderService = baseSaleOrderService;
        this.rarerabbitSaleOrderService = rarerabbitSaleOrderService;
        this.rustOrangeSaleOrderService = rustOrangeSaleOrderService;
        this.ozivaSaleOrderService = ozivaSaleOrderService;
    }

    public ISaleOrderService getService(String tenantCode) {
        switch(tenantCode.toLowerCase()) {
            case "rarerabbit": return rarerabbitSaleOrderService;
            case "rustorange": return rustOrangeSaleOrderService;
            case "oziva": return ozivaSaleOrderService;
            default: return baseSaleOrderService;
        }
    }
}
