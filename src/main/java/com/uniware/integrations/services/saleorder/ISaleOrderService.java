package com.uniware.integrations.services.saleorder;

import com.uniware.integrations.dto.*;
import com.uniware.integrations.dto.shopify.Location;
import com.uniware.integrations.dto.shopify.Order;
import com.uniware.integrations.dto.shopify.Transaction;
import com.uniware.integrations.uniware.dto.saleOrder.request.CreateSaleOrderRequest;
import com.uniware.integrations.uniware.dto.saleOrder.request.CustomFieldValue;
import com.uniware.integrations.uniware.dto.saleOrder.request.PushSaleOrderStatusRequest;
import com.uniware.integrations.uniware.dto.saleOrder.request.SaleOrder;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ISaleOrderService {
    ResponseEntity<ApiResponse<Map<String, SaleOrder>>> getSaleOrders(LocalDate from, LocalDate to, String pageSize, String pageInfo,GetSaleOrdersRequest getSaleOrdersRequest);
    List<CustomFieldValue> prepareCustomFieldValues(String customFieldsCustomization,Order order, List<Transaction> transactions);
    CreateSaleOrderRequest prepareCreateSaleOrderRequest(Order order);
    List<Order> filterOrders(List<Order> orders, ConfigurationParameters configurationParameters, ConnectorParameters connectorParameters);
    ApiResponse<Order> getOrder(String id);
    String getSplitShipmentCondition();
    Object orderReconciliation(LocalDate from, LocalDate to, ConfigurationParameters configurationParameters, ConnectorParameters connectorParameters);
    ResponseEntity<List<Pendency>> getPendencies(LocalDate from, LocalDate to, String pageSize, String pageInfo);
//    List<Transaction> getOrderTransactions(String id);
    ApiResponse<Map<String, ShopifyOrderMetadata>> statusSyncMetadata(LocalDate from, LocalDate to, String pageSize);
    ApiResponse<List<PushSaleOrderStatusRequest.WsSaleOrder.WsSaleOrderItem>> orderStatusSync(String orderId, SaleOrderStatusSyncRequest saleOrderStatusSyncRequest);
    String getPincodeDetails(String pincode);
    ApiResponse<CreateSaleOrderRequest> getCreateSaleOrderRequest(String orderId);
    ApiResponse<SaleOrder> getWsSaleOrder(GetWsSaleOrderRequest getWsSaleOrderRequest);
    ApiResponse<Location> getLocationById(String id);
    ApiResponse<Object> verifyConnectors(ConnectorVerificationRequest verifyConnectorsRequest);
    ShopifyOrderMetadata getShopifyOrderMetadata(String id);
}
