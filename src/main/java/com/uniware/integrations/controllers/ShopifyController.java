package com.uniware.integrations.controllers;

import com.uniware.integrations.contexts.ShopifyRequestContext;
import com.uniware.integrations.dto.*;
import com.uniware.integrations.dto.shopify.Location;
import com.uniware.integrations.dto.shopify.Order;
import com.uniware.integrations.dto.uniware.GetCatalogResponse;
import com.uniware.integrations.dto.uniware.UpdateInventoryRequest;
import com.uniware.integrations.dto.uniware.UpdateInventoryResponse;
import com.uniware.integrations.services.InventoryService;
import com.uniware.integrations.services.ListingService;
import com.uniware.integrations.services.saleorder.ISaleOrderService;
import com.uniware.integrations.services.saleorder.SaleOrderServiceFactory;
import com.uniware.integrations.uniware.dto.saleOrder.request.CreateSaleOrderRequest;
import com.uniware.integrations.uniware.dto.saleOrder.request.PushSaleOrderStatusRequest;
import com.uniware.integrations.uniware.dto.saleOrder.request.SaleOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.uniware.integrations.constants.ShopifyConstants.*;

@Controller
@RequestMapping("shopify")
public class ShopifyController {
    SaleOrderServiceFactory saleOrderServiceFactory;

    @Autowired
    ListingService listingService;

    @Autowired
    InventoryService inventoryService;

    @Autowired
    public ShopifyController(SaleOrderServiceFactory saleOrderServiceFactory) {
        this.saleOrderServiceFactory = saleOrderServiceFactory;
    }

    @PostMapping(value = "/orders", produces = "application/json")
    public @ResponseBody ResponseEntity<ApiResponse<Map<String, SaleOrder>>> getOrders(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to, @RequestParam(defaultValue = PAGE_SIZE) String pageSize, @RequestHeader(required = false) String pageInfo, @RequestBody GetSaleOrdersRequest getSaleOrdersRequest) {
        ISaleOrderService saleOrderService = saleOrderServiceFactory.getService(ShopifyRequestContext.current().getTenantCode());
        return saleOrderService.getSaleOrders(from, to, pageSize, pageInfo, getSaleOrdersRequest);
    }

    @GetMapping(value = "/orders/{id}", produces = "application/json")
    public @ResponseBody ApiResponse<Order> getOrder(@PathVariable String id) {
        ISaleOrderService saleOrderService = saleOrderServiceFactory.getService(ShopifyRequestContext.current().getTenantCode());
        return saleOrderService.getOrder(id);
    }

//    @GetMapping(value = "/orders/{id}/transactions", produces = "application/json")
//    public @ResponseBody Object getOrderTransactions(@PathVariable String id) {
//        ISaleOrderService saleOrderService = saleOrderServiceFactory.getService(ShopifyRequestContext.current().getTenantCode());
//        return saleOrderService.getOrderTransactions("gid://shopify/Order/" + id);
//    }

    @GetMapping(value = "/pendency", produces = "application/json")
    public @ResponseBody ResponseEntity<List<Pendency>> getPendencies(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to, @RequestParam(defaultValue = PAGE_SIZE) String pageSize, @RequestHeader(required = false) String pageInfo) {
        ISaleOrderService saleOrderService = saleOrderServiceFactory.getService(ShopifyRequestContext.current().getTenantCode());
        return saleOrderService.getPendencies(from, to, pageSize, pageInfo);
    }

    @GetMapping(value = "/orders/reconciliation", produces = "application/json")
    public @ResponseBody Object reconciliation(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        ISaleOrderService saleOrderService = saleOrderServiceFactory.getService(ShopifyRequestContext.current().getTenantCode());
        return saleOrderService.orderReconciliation(from, to, null, null);
    }

    @GetMapping(value = "/orders/status-sync/metadata", produces = "application/json")
    public @ResponseBody ApiResponse<Map<String, ShopifyOrderMetadata>> statusSyncMetadata(@RequestParam(defaultValue = PAGE_SIZE) String pageSize, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        ISaleOrderService saleOrderService = saleOrderServiceFactory.getService(ShopifyRequestContext.current().getTenantCode());
        return saleOrderService.statusSyncMetadata(from, to, pageSize);
    }

    @PostMapping(value = "/orders/{id}/status-sync", produces = "application/json")
    public @ResponseBody ApiResponse<List<PushSaleOrderStatusRequest.WsSaleOrder.WsSaleOrderItem>> orderStatusSync(@PathVariable String id, @RequestBody SaleOrderStatusSyncRequest saleOrderStatusSyncRequest) {
        ISaleOrderService saleOrderService = saleOrderServiceFactory.getService(ShopifyRequestContext.current().getTenantCode());
        return saleOrderService.orderStatusSync(id, saleOrderStatusSyncRequest);
    }

    @GetMapping(value = "/pincode", produces = "application/json")
    public @ResponseBody Object getPincodeDetails(@RequestParam String pincode) {
        ISaleOrderService saleOrderService = saleOrderServiceFactory.getService(ShopifyRequestContext.current().getTenantCode());
        return saleOrderService.getPincodeDetails(pincode);
    }

    @GetMapping(value = "/order/create", produces = "application/json")
    public @ResponseBody ApiResponse<CreateSaleOrderRequest> createOrder(@RequestParam String orderId) {
        ISaleOrderService saleOrderService = saleOrderServiceFactory.getService(ShopifyRequestContext.current().getTenantCode());
        return saleOrderService.getCreateSaleOrderRequest(orderId);
    }

    @PostMapping(value = "/ws-sale-order", produces = "application/json")
    public @ResponseBody ApiResponse<SaleOrder> getWsSaleOrder(@RequestBody GetWsSaleOrderRequest getWsSaleOrderRequest) {
        ISaleOrderService saleOrderService = saleOrderServiceFactory.getService(ShopifyRequestContext.current().getTenantCode());
        return saleOrderService.getWsSaleOrder(getWsSaleOrderRequest);
    }

    @GetMapping(value = "/locations/{id}", produces = "application/json")
    public @ResponseBody ApiResponse<Location> getLocationById(@PathVariable String id) {
        ISaleOrderService saleOrderService = saleOrderServiceFactory.getService(ShopifyRequestContext.current().getTenantCode());
        return saleOrderService.getLocationById(id);
    }

    @PostMapping(value = "/connectors/verify", produces = "application/json")
    public @ResponseBody ApiResponse<Object> verifyconnectors(@RequestBody ConnectorParameters connectorParameters) {
        ISaleOrderService saleOrderService = saleOrderServiceFactory.getService(ShopifyRequestContext.current().getTenantCode());
        return saleOrderService.verifyConnectors(connectorParameters);
    }

    @GetMapping(value = "/orders/{id}/status-sync/metadata", produces = "application/json")
    public @ResponseBody ShopifyOrderMetadata getShopifyOrderMetadata(@PathVariable("id") String orderId) {
        ISaleOrderService saleOrderService = saleOrderServiceFactory.getService(ShopifyRequestContext.current().getTenantCode());
        return saleOrderService.getShopifyOrderMetadata(orderId);
    }

    @PostMapping(value = "/catalog", produces = "application/json")
    public @ResponseBody ApiResponse<GetCatalogResponse> fetchCatalog() {
        return listingService.fetchCatalog();
    }

    @GetMapping(value = "/catalog", produces = "application/json")
    public @ResponseBody ApiResponse pollJob(@RequestParam String bulkOperationId) {
        return listingService.pollJob(bulkOperationId);
    }


    // TODO : https://www.baeldung.com/spring-validate-list-controller
    @PutMapping(value = "/inventory", produces = "application/json")
    public @ResponseBody ApiResponse<UpdateInventoryResponse> updateInventory(@RequestBody UpdateInventoryRequest updateInventoryRequest) {
        return inventoryService.updateInventory(updateInventoryRequest);
    }

//    @GetMapping(value="/orders/reconciliation", produces = "application/json")
//    public @ResponseBody ApiResponse reconciliation() {
//        ISaleOrderService saleOrderService=saleOrderServiceFactory.getService(ShopifyRequestContext.current().getTenantCode());
//        return saleOrderService.getOrder(id);
//    }
//    @GetMapping(value="/accessScopes", produces = "application/json")
//    public @ResponseBody AccessScopesWrapper getAccessScopes() {
//        ISaleOrderService saleOrderService=saleOrderServiceFactory.getService(ShopifyRequestContext.current().getTenantCode());
//        return saleOrderService.getAccessScopes();
//    }
//    @GetMapping(value="/verify-connectors", produces = "application/json")
//    public @ResponseBody ApiResponse verifyConnectors(@RequestHeader Map<String,String> headers) {
//        ISaleOrderService saleOrderService=saleOrderServiceFactory.getService(ShopifyRequestContext.current().getTenantCode());
//        return saleOrderService.verifyConnectors(headers);
//    }
//    @GetMapping(value="/catalog", produces = "application/json")
//    public @ResponseBody Object getCatalog(@RequestHeader Map<String,String> headers,@RequestParam(name = "transaction_id",required = false) String transactionId) {
//        ISaleOrderService saleOrderService=saleOrderServiceFactory.getService(ShopifyRequestContext.current().getTenantCode());
//        return saleOrderService.getCatalog(headers,transactionId);
//    }
}
