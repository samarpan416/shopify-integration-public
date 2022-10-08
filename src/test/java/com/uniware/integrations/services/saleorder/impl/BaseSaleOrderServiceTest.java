package com.uniware.integrations.services.saleorder.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unifier.core.utils.CollectionUtils;
import com.uniware.integrations.dto.LineItemMetadata;
import com.uniware.integrations.clients.ShopifyClient;
import com.uniware.integrations.dto.ApiResponse;
import com.uniware.integrations.dto.SaleOrderStatusSyncRequest;
import com.uniware.integrations.dto.ShopifyOrderMetadata;
import com.uniware.integrations.dto.StatusSyncSoi;
import com.uniware.integrations.dto.shopify.*;
import com.uniware.integrations.uniware.dto.saleOrder.request.PushSaleOrderStatusRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.*;

import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("When running BaseSaleOrderService")
@ExtendWith(MockitoExtension.class)
class BaseSaleOrderServiceTest {

    public static final String DEFAULT_ORDER_ID = "4509980229745";
    @Mock
    ShopifyClient mockShopifyClient;
    @Spy
    @InjectMocks
    BaseSaleOrderService baseSaleOrderService;

    @DisplayName("getOrder method")
    @Test
    void getOrder() throws JsonProcessingException {
        String id = DEFAULT_ORDER_ID;
        OrderWrapper mockOrderWrapper = getMockOrderWrapper(id);
        when(mockShopifyClient.getOrderById(any(String.class))).thenReturn(mockOrderWrapper);
        ApiResponse<Order> response = baseSaleOrderService.getOrder(id);
        assertEquals("Order found", response.getMessage(), "should set correct message");
        assertEquals(mockOrderWrapper.getOrder(), response.getData(), "should return correct order json");
    }

//     @DisplayName("shouldFetchOrder method")
//     @Test
//     void shouldFetchOrder() throws JsonProcessingException {
//        BaseSaleOrderService spyBaseSaleOrderService=spy(baseSaleOrderService);
//        doReturn(Arrays.asList("UP", "MH")).when(baseSaleOrderService).getAllowedProvinceCodes();
//        doReturn("prepaid").when(baseSaleOrderService).getPaymentMode(any(String.class));
//        assertAll(() -> {
//            Order mockOrder = getMockOrder(null);
//            mockOrder.setConfirmed(true);
//            assertFalse(baseSaleOrderService.shouldFetchOrder(mockOrder));
// //            assertEquals(Arrays.asList("UP","MH"),baseSaleOrderService.testStub());
//        }, () -> {
// //            verify(baseSaleOrderService).getAllowedProvinceCodes();
//        });
//     }

    @DisplayName("orderStatusSync method")
    @Test
    void orderStatusSync_case1() {
        SaleOrderStatusSyncRequest saleOrderStatusSyncRequest = new SaleOrderStatusSyncRequest();
        ShopifyOrderMetadata shopifyOrderMetadata = new ShopifyOrderMetadata();
        List<StatusSyncSoi> statusSyncSaleOrderItems = new ArrayList<>();
        StatusSyncSoi statusSyncSoi1 = new StatusSyncSoi();
        statusSyncSoi1.setCode("11626649190582-0");
        statusSyncSoi1.setChannelSaleOrderItemCode("11626649190582");
        statusSyncSoi1.setCombinationIdentifier("11626649190582");
        statusSyncSoi1.setStatusCode("FULFILLABLE");
        statusSyncSoi1.setCancellable(true);
        statusSyncSoi1.setReturned(false);
        statusSyncSoi1.setReversePickable(false);
        StatusSyncSoi statusSyncSoi2 = new StatusSyncSoi();
        statusSyncSoi2.setCode("11626649190582-1");
        statusSyncSoi2.setChannelSaleOrderItemCode("11626649190582");
        statusSyncSoi2.setCombinationIdentifier("11626649190582");
        statusSyncSoi2.setStatusCode("FULFILLABLE");
        statusSyncSoi2.setCancellable(true);
        statusSyncSoi2.setReturned(false);
        statusSyncSoi2.setReversePickable(false);
        statusSyncSaleOrderItems.add(statusSyncSoi1);
        statusSyncSaleOrderItems.add(statusSyncSoi2);

        Map<String, LineItemMetadata> lineItemIdToMetadata = new HashMap<>();
        LineItemMetadata lineItemMetadata1 = new LineItemMetadata();
        lineItemMetadata1.setCancelledQty(1);
        lineItemMetadata1.setReturnedQty(0);
        lineItemIdToMetadata.put("11626649190582", lineItemMetadata1);

        shopifyOrderMetadata.setOrderCancelled(false);
        shopifyOrderMetadata.setFulfillmentStatus(null);
        shopifyOrderMetadata.setLineItemIdToMetadata(lineItemIdToMetadata);
        saleOrderStatusSyncRequest.setShopifyOrderMetadata(shopifyOrderMetadata);
        saleOrderStatusSyncRequest.setStatusSyncSaleOrderItems(statusSyncSaleOrderItems);
        ApiResponse<List<PushSaleOrderStatusRequest.WsSaleOrder.WsSaleOrderItem>> apiResponse = baseSaleOrderService.orderStatusSync("4568483954870", saleOrderStatusSyncRequest);
        List<PushSaleOrderStatusRequest.WsSaleOrder.WsSaleOrderItem> wsSaleOrderItems = apiResponse.getData();

        List<String> cancelledSoiCodes = wsSaleOrderItems.stream().filter(wsSaleOrderItem -> "CANCELLED".equals(wsSaleOrderItem.getStatusCode())).map(PushSaleOrderStatusRequest.WsSaleOrder.WsSaleOrderItem::getCode).collect(Collectors.toList());
        List<String> expectedCancelledSoiCodes = Arrays.asList("11626649190582-0", "11626649190582-1");
        assertEquals(ApiResponse.STATUS.SUCCESS, apiResponse.getStatus());
        assertEquals(expectedCancelledSoiCodes.size(), cancelledSoiCodes.size());
        assertTrue(isEqualList(expectedCancelledSoiCodes, cancelledSoiCodes));
    }

    @DisplayName("orderStatusSync method")
    @Test
    void orderStatusSync_case2() {
        SaleOrderStatusSyncRequest saleOrderStatusSyncRequest = new SaleOrderStatusSyncRequest();
        ShopifyOrderMetadata shopifyOrderMetadata = new ShopifyOrderMetadata();
        List<StatusSyncSoi> statusSyncSaleOrderItems = new ArrayList<>();
        StatusSyncSoi statusSyncSoi1 = new StatusSyncSoi();
        statusSyncSoi1.setCode("11626649190582-0");
        statusSyncSoi1.setChannelSaleOrderItemCode("11626649190582");
        statusSyncSoi1.setStatusCode("FULFILLABLE");
        statusSyncSoi1.setCancellable(true);
        statusSyncSoi1.setReturned(false);
        statusSyncSoi1.setReversePickable(false);
        StatusSyncSoi statusSyncSoi2 = new StatusSyncSoi();
        statusSyncSoi2.setCode("11626649190582-1");
        statusSyncSoi2.setChannelSaleOrderItemCode("11626649190582");
        statusSyncSoi2.setStatusCode("FULFILLABLE");
        statusSyncSoi2.setCancellable(true);
        statusSyncSoi2.setReturned(false);
        statusSyncSoi2.setReversePickable(false);
        statusSyncSaleOrderItems.add(statusSyncSoi1);
        statusSyncSaleOrderItems.add(statusSyncSoi2);

        Map<String, LineItemMetadata> lineItemIdToMetadata = new HashMap<>();
        LineItemMetadata lineItemMetadata1 = new LineItemMetadata();
        lineItemMetadata1.setCancelledQty(1);
        lineItemMetadata1.setReturnedQty(0);
        lineItemIdToMetadata.put("11626649190582", lineItemMetadata1);

        shopifyOrderMetadata.setOrderCancelled(false);
        shopifyOrderMetadata.setFulfillmentStatus(null);
        shopifyOrderMetadata.setLineItemIdToMetadata(lineItemIdToMetadata);
        saleOrderStatusSyncRequest.setShopifyOrderMetadata(shopifyOrderMetadata);
        saleOrderStatusSyncRequest.setStatusSyncSaleOrderItems(statusSyncSaleOrderItems);
        ApiResponse<List<PushSaleOrderStatusRequest.WsSaleOrder.WsSaleOrderItem>> apiResponse = baseSaleOrderService.orderStatusSync("4568483954870", saleOrderStatusSyncRequest);
        List<PushSaleOrderStatusRequest.WsSaleOrder.WsSaleOrderItem> wsSaleOrderItems = apiResponse.getData();

        List<String> cancelledSoiCodes = wsSaleOrderItems.stream().filter(wsSaleOrderItem -> "CANCELLED".equals(wsSaleOrderItem.getStatusCode())).map(PushSaleOrderStatusRequest.WsSaleOrder.WsSaleOrderItem::getCode).collect(Collectors.toList());
        List<String> expectedCancelledSoiCodes = Arrays.asList("11626649190582-0", "11626649190582-1");
        assertEquals(ApiResponse.STATUS.SUCCESS, apiResponse.getStatus());
        assertEquals(1, cancelledSoiCodes.size());
        assertEquals(1,expectedCancelledSoiCodes.stream().filter(cancelledSoiCodes::contains).count());
    }

    private <T> boolean isEqualList(List<T> list1, List<T> list2) {
        if (list1 == null && list2 == null) return true;
        if (list1 == null || list2 == null) return false;
        return list1.size() == list2.size() && list1.containsAll(list2) && list2.containsAll(list1);
    }

    @DisplayName("parseTagsAsList method")
    @Test
    void shouldParseTagsAsList() {
        assertEquals(Collections.singletonList("COD_CANCEL"), baseSaleOrderService.parseAsList("COD_CANCEL", ","), "should parse only 1 tag");
        assertEquals(Arrays.asList("COD_CANCEL", "COD_PENDING"), baseSaleOrderService.parseAsList("COD_CANCEL,COD_PENDING", ","), "should parse more than 1 tags");
        assertEquals(Arrays.asList("COD_CANCEL", "COD_PENDING"), baseSaleOrderService.parseAsList("COD_CANCEL ,COD_PENDING", ","), "should trim spaces for each tag");
    }

    @DisplayName("parseTagsAsSet method")
    @Test
    void shouldParseTagsAsSet() {
        Set<String> distinctTags = CollectionUtils.asSet(Arrays.asList("COD_CANCEL", "COD_PENDING"));
        assertEquals(distinctTags, baseSaleOrderService.parseTagsAsSet("COD_CANCEL,COD_CANCEL ,COD_PENDING"), "should return distinct tags");
    }

    // @DisplayName("getProvinceCode method")
    // @Test
    // void shouldReturnProvinceCode() throws JsonProcessingException {
    //     Order mockOrder = getMockOrder(null);
    //     ShopifyAddress shippingAddress = new ShopifyAddress();
    //     shippingAddress.setProvinceCode("UK");
    //     ShopifyAddress billingAddress = new ShopifyAddress();
    //     billingAddress.setProvinceCode("UP");
    //     assertAll(() -> {
    //         doReturn(shippingAddress).when(baseSaleOrderService).getShippingAddress(any(Order.class));
    //         doReturn(billingAddress).when(baseSaleOrderService).getBillingAddress(any(Order.class));
    //         assertEquals(shippingAddress.getProvinceCode(), baseSaleOrderService.getProvinceCode(mockOrder), "should return shipping address pincode");
    //     }, () -> {
    //         doReturn(null).when(baseSaleOrderService).getShippingAddress(any(Order.class));
    //         doReturn(billingAddress).when(baseSaleOrderService).getBillingAddress(any(Order.class));
    //         assertEquals(billingAddress.getProvinceCode(), baseSaleOrderService.getProvinceCode(mockOrder), "should return billing address pincode");
    //     }, () -> {
    //         doReturn(null).when(baseSaleOrderService).getShippingAddress(any(Order.class));
    //         doReturn(null).when(baseSaleOrderService).getBillingAddress(any(Order.class));
    //         assertNull(baseSaleOrderService.getProvinceCode(mockOrder), "should return null");
    //     });
    // }

    @DisplayName("prepareCustomFieldValues method")
    void prepareCustomFieldValues() {
//        baseSaleOrderService.prepareCustomFieldValues("",)
    }

    private OrderWrapper getMockOrderWrapper(String id) throws JsonProcessingException {
        Order mockOrder = getMockOrder(id);
        return new OrderWrapper(mockOrder);
    }

    @DisplayName("updateLineItemMetadata method")
    private void should_update_cancelled_qty_when_restock_type_CANCEL() {
        try {
            Method updateLineItemMetadataMethod = BaseSaleOrderService.class.getDeclaredMethod("updateLineItemMetadata", List.class, Map.class);
            List<Refund> refunds = new ArrayList<>();
            Refund refund = new Refund();
            List<RefundLineItem> refundLineItems = new ArrayList<>();
            RefundLineItem refundLineItem = new RefundLineItem();
            refundLineItem.setLineItemId(1L);
            refundLineItems.add(refundLineItem);
            refund.setRefundLineItems(refundLineItems);
            refunds.add(refund);
            updateLineItemMetadataMethod.invoke(updateLineItemMetadataMethod, refunds, null);
        } catch (NoSuchMethodException e) {

        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Order getMockOrder(String id) throws JsonProcessingException {
        String orderJson = getMockOrderJson(id);
        ObjectMapper objectMapper = getObjectMapper();
        return objectMapper.readValue(orderJson, Order.class);
    }

    private ObjectMapper getObjectMapper() {
        return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private String getMockOrderJson(String id) {
        id = id != null ? id : DEFAULT_ORDER_ID;
        return "{\n" +
                "        \"id\": " + id + ",\n" +
                "        \"admin_graphql_api_id\": \"gid://shopify/Order/" + id + "\",\n" +
                "        \"app_id\": 580111,\n" +
                "        \"browser_ip\": \"45.119.14.68\",\n" +
                "        \"buyer_accepts_marketing\": true,\n" +
                "        \"cancel_reason\": null,\n" +
                "        \"cancelled_at\": null,\n" +
                "        \"cart_token\": \"869117b6f4c06351a547cf4103d3384c\",\n" +
                "        \"checkout_id\": 29163831132273,\n" +
                "        \"checkout_token\": \"34b2189594324ff956cb8b56b72b97e6\",\n" +
                "        \"client_details\": {\n" +
                "            \"accept_language\": \"en-IN,en-GB;q=0.9,en;q=0.8\",\n" +
                "            \"browser_height\": 859,\n" +
                "            \"browser_ip\": \"45.119.14.68\",\n" +
                "            \"browser_width\": 428,\n" +
                "            \"session_hash\": null,\n" +
                "            \"user_agent\": \"Mozilla/5.0 (iPhone; CPU iPhone OS 15_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148 Instagram 239.2.0.17.109 (iPhone13,4; iOS 15_5; en_IN; en-IN; scale=3.00; 1284x2778; 376668393)\"\n" +
                "        },\n" +
                "        \"closed_at\": null,\n" +
                "        \"confirmed\": true,\n" +
                "        \"contact_email\": \"veduparthe4444@gmail.com\",\n" +
                "        \"created_at\": \"2022-06-17T22:14:13+05:30\",\n" +
                "        \"currency\": \"INR\",\n" +
                "        \"current_subtotal_price\": \"399.00\",\n" +
                "        \"current_subtotal_price_set\": {\n" +
                "            \"shop_money\": {\n" +
                "                \"amount\": \"399.00\",\n" +
                "                \"currency_code\": \"INR\"\n" +
                "            },\n" +
                "            \"presentment_money\": {\n" +
                "                \"amount\": \"399.00\",\n" +
                "                \"currency_code\": \"INR\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"current_total_discounts\": \"0.00\",\n" +
                "        \"current_total_discounts_set\": {\n" +
                "            \"shop_money\": {\n" +
                "                \"amount\": \"0.00\",\n" +
                "                \"currency_code\": \"INR\"\n" +
                "            },\n" +
                "            \"presentment_money\": {\n" +
                "                \"amount\": \"0.00\",\n" +
                "                \"currency_code\": \"INR\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"current_total_duties_set\": null,\n" +
                "        \"current_total_price\": \"399.00\",\n" +
                "        \"current_total_price_set\": {\n" +
                "            \"shop_money\": {\n" +
                "                \"amount\": \"399.00\",\n" +
                "                \"currency_code\": \"INR\"\n" +
                "            },\n" +
                "            \"presentment_money\": {\n" +
                "                \"amount\": \"399.00\",\n" +
                "                \"currency_code\": \"INR\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"current_total_tax\": \"60.86\",\n" +
                "        \"current_total_tax_set\": {\n" +
                "            \"shop_money\": {\n" +
                "                \"amount\": \"60.86\",\n" +
                "                \"currency_code\": \"INR\"\n" +
                "            },\n" +
                "            \"presentment_money\": {\n" +
                "                \"amount\": \"60.86\",\n" +
                "                \"currency_code\": \"INR\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"customer_locale\": \"en-IN\",\n" +
                "        \"device_id\": null,\n" +
                "        \"discount_codes\": [],\n" +
                "        \"email\": \"veduparthe4444@gmail.com\",\n" +
                "        \"estimated_taxes\": false,\n" +
                "        \"financial_status\": \"pending\",\n" +
                "        \"fulfillment_status\": null,\n" +
                "        \"gateway\": \"Cash on Delivery (COD)\",\n" +
                "        \"landing_site\": \"/products/wow-skin-science-green-tea-under-eye-cream-roller-tube-20-ml?utm_source=Facebook&utm_medium=Instagram_Reels&utm_campaign=Conversion_New_Acquisition&utm_content=GT_Under_Eye&fbclid=PAAaYILSOx100h5TMbEcMrragwyw_ERiSq7fUAjWzFR5v-f9xxD0VUFCVG9TM_aem_Aev7YjHaG0loaIDWTyQRD5y9jxX_3uz97gbgKjvkqbXCld4e0sawjmJ8FqqLpRkMP0G1H01fuc13ds7kHUGef_BBZIc7zm8a5BiKBRRu5lf_wW_oxfkoqmQ4WIWPxM6zA_k\",\n" +
                "        \"landing_site_ref\": null,\n" +
                "        \"location_id\": null,\n" +
                "        \"name\": \"3451037\",\n" +
                "        \"note\": null,\n" +
                "        \"note_attributes\": [\n" +
                "            {\n" +
                "                \"name\": \"_elevar__fbc\",\n" +
                "                \"value\": \"fb.1.1655484200530.PAAaYILSOx100h5TMbEcMrragwyw_ERiSq7fUAjWzFR5v-f9xxD0VUFCVG9TM_aem_Aev7YjHaG0loaIDWTyQRD5y9jxX_3uz97gbgKjvkqbXCld4e0sawjmJ8FqqLpRkMP0G1H01fuc13ds7kHUGef_BBZIc7zm8a5BiKBRRu5lf_wW_oxfkoqmQ4WIWPxM6zA_k\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"_elevar__fbp\",\n" +
                "                \"value\": \"fb.1.1655484200531.1136516621\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"_elevar__ga\",\n" +
                "                \"value\": \"GA1.1.50354733.1655484192\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"_elevar__ga_3N2K3CC9R3\",\n" +
                "                \"value\": \"GS1.1.1655484178.1\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"_elevar__ga_1X3TVJK6CQ\",\n" +
                "                \"value\": \"GS1.1.1655484178.1\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"_elevar_visitor_info\",\n" +
                "                \"value\": \"{\\\"utm_source\\\":\\\"Facebook\\\",\\\"utm_medium\\\":\\\"Instagram_Reels\\\",\\\"utm_campaign\\\":\\\"Conversion_New_Acquisition\\\",\\\"utm_content\\\":\\\"GT_Under_Eye\\\",\\\"fbclid\\\":\\\"PAAaYILSOx100h5TMbEcMrragwyw_ERiSq7fUAjWzFR5v-f9xxD0VUFCVG9TM_aem_Aev7YjHaG0loaIDWTyQRD5y9jxX_3uz97gbgKjvkqbXCld4e0sawjmJ8FqqLpRkMP0G1H01fuc13ds7kHUGef_BBZIc7zm8a5BiKBRRu5lf_wW_oxfkoqmQ4WIWPxM6zA_k\\\",\\\"user_id\\\":\\\"06abfffe-5a38-4b3c-85db-3d98017c1e3c\\\"}\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"number\": 3450037,\n" +
                "        \"order_number\": 3451037,\n" +
                "        \"order_status_url\": \"https://www.buywow.in/13754957/orders/28f8011de205a3e344c5b7ed717956c5/authenticate?key=9b2e7e6a57305e8c2a0cb42ae4a0e39d\",\n" +
                "        \"original_total_duties_set\": null,\n" +
                "        \"payment_gateway_names\": [\n" +
                "            \"Cash on Delivery (COD)\"\n" +
                "        ],\n" +
                "        \"phone\": null,\n" +
                "        \"presentment_currency\": \"INR\",\n" +
                "        \"processed_at\": \"2022-06-17T22:14:12+05:30\",\n" +
                "        \"processing_method\": \"manual\",\n" +
                "        \"reference\": null,\n" +
                "        \"referring_site\": \"\",\n" +
                "        \"source_identifier\": null,\n" +
                "        \"source_name\": \"web\",\n" +
                "        \"source_url\": null,\n" +
                "        \"subtotal_price\": \"399.00\",\n" +
                "        \"subtotal_price_set\": {\n" +
                "            \"shop_money\": {\n" +
                "                \"amount\": \"399.00\",\n" +
                "                \"currency_code\": \"INR\"\n" +
                "            },\n" +
                "            \"presentment_money\": {\n" +
                "                \"amount\": \"399.00\",\n" +
                "                \"currency_code\": \"INR\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"tags\": \"\",\n" +
                "        \"tax_lines\": [\n" +
                "            {\n" +
                "                \"price\": \"60.86\",\n" +
                "                \"rate\": 0.18,\n" +
                "                \"title\": \"IGST\",\n" +
                "                \"price_set\": {\n" +
                "                    \"shop_money\": {\n" +
                "                        \"amount\": \"60.86\",\n" +
                "                        \"currency_code\": \"INR\"\n" +
                "                    },\n" +
                "                    \"presentment_money\": {\n" +
                "                        \"amount\": \"60.86\",\n" +
                "                        \"currency_code\": \"INR\"\n" +
                "                    }\n" +
                "                },\n" +
                "                \"channel_liable\": false\n" +
                "            }\n" +
                "        ],\n" +
                "        \"taxes_included\": true,\n" +
                "        \"test\": false,\n" +
                "        \"token\": \"28f8011de205a3e344c5b7ed717956c5\",\n" +
                "        \"total_discounts\": \"0.00\",\n" +
                "        \"total_discounts_set\": {\n" +
                "            \"shop_money\": {\n" +
                "                \"amount\": \"0.00\",\n" +
                "                \"currency_code\": \"INR\"\n" +
                "            },\n" +
                "            \"presentment_money\": {\n" +
                "                \"amount\": \"0.00\",\n" +
                "                \"currency_code\": \"INR\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"total_line_items_price\": \"399.00\",\n" +
                "        \"total_line_items_price_set\": {\n" +
                "            \"shop_money\": {\n" +
                "                \"amount\": \"399.00\",\n" +
                "                \"currency_code\": \"INR\"\n" +
                "            },\n" +
                "            \"presentment_money\": {\n" +
                "                \"amount\": \"399.00\",\n" +
                "                \"currency_code\": \"INR\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"total_outstanding\": \"399.00\",\n" +
                "        \"total_price\": \"399.00\",\n" +
                "        \"total_price_set\": {\n" +
                "            \"shop_money\": {\n" +
                "                \"amount\": \"399.00\",\n" +
                "                \"currency_code\": \"INR\"\n" +
                "            },\n" +
                "            \"presentment_money\": {\n" +
                "                \"amount\": \"399.00\",\n" +
                "                \"currency_code\": \"INR\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"total_price_usd\": \"5.12\",\n" +
                "        \"total_shipping_price_set\": {\n" +
                "            \"shop_money\": {\n" +
                "                \"amount\": \"0.00\",\n" +
                "                \"currency_code\": \"INR\"\n" +
                "            },\n" +
                "            \"presentment_money\": {\n" +
                "                \"amount\": \"0.00\",\n" +
                "                \"currency_code\": \"INR\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"total_tax\": \"60.86\",\n" +
                "        \"total_tax_set\": {\n" +
                "            \"shop_money\": {\n" +
                "                \"amount\": \"60.86\",\n" +
                "                \"currency_code\": \"INR\"\n" +
                "            },\n" +
                "            \"presentment_money\": {\n" +
                "                \"amount\": \"60.86\",\n" +
                "                \"currency_code\": \"INR\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"total_tip_received\": \"0.00\",\n" +
                "        \"total_weight\": 0,\n" +
                "        \"updated_at\": \"2022-06-17T22:14:15+05:30\",\n" +
                "        \"user_id\": null,\n" +
                "        \"billing_address\": {\n" +
                "            \"first_name\": \"Vedika\",\n" +
                "            \"address1\": \"Round Grass Banglow\",\n" +
                "            \"phone\": \"+919028002190\",\n" +
                "            \"city\": \"Panvel\",\n" +
                "            \"zip\": \"410206\",\n" +
                "            \"province\": \"Maharashtra\",\n" +
                "            \"country\": \"India\",\n" +
                "            \"last_name\": \"Parthe\",\n" +
                "            \"address2\": \"Near By Biknare Peth Street\",\n" +
                "            \"company\": null,\n" +
                "            \"latitude\": null,\n" +
                "            \"longitude\": null,\n" +
                "            \"name\": \"Vedika Parthe\",\n" +
                "            \"country_code\": \"IN\",\n" +
                "            \"province_code\": \"MH\"\n" +
                "        },\n" +
                "        \"customer\": {\n" +
                "            \"id\": 3790645166193,\n" +
                "            \"email\": \"veduparthe4444@gmail.com\",\n" +
                "            \"accepts_marketing\": true,\n" +
                "            \"created_at\": \"2021-01-01T16:07:48+05:30\",\n" +
                "            \"updated_at\": \"2022-06-17T22:16:27+05:30\",\n" +
                "            \"first_name\": \"Vedika\",\n" +
                "            \"last_name\": \"Parthe\",\n" +
                "            \"orders_count\": 2,\n" +
                "            \"state\": \"disabled\",\n" +
                "            \"total_spent\": \"829.00\",\n" +
                "            \"last_order_id\": 4484270751857,\n" +
                "            \"note\": null,\n" +
                "            \"verified_email\": true,\n" +
                "            \"multipass_identifier\": null,\n" +
                "            \"tax_exempt\": false,\n" +
                "            \"phone\": null,\n" +
                "            \"tags\": \"wallet_120d31cffaddfea52d883e2acd59aae2\",\n" +
                "            \"last_order_name\": \"3451037\",\n" +
                "            \"currency\": \"INR\",\n" +
                "            \"accepts_marketing_updated_at\": \"2021-01-01T16:07:49+05:30\",\n" +
                "            \"marketing_opt_in_level\": \"single_opt_in\",\n" +
                "            \"tax_exemptions\": [],\n" +
                "            \"sms_marketing_consent\": null,\n" +
                "            \"admin_graphql_api_id\": \"gid://shopify/Customer/3790645166193\",\n" +
                "            \"default_address\": {\n" +
                "                \"id\": 7075209379953,\n" +
                "                \"customer_id\": 3790645166193,\n" +
                "                \"first_name\": \"Vedika\",\n" +
                "                \"last_name\": \"Parthe\",\n" +
                "                \"company\": null,\n" +
                "                \"address1\": \"Round Grass Banglow\",\n" +
                "                \"address2\": \"Near By Biknare Peth Street\",\n" +
                "                \"city\": \"Panvel\",\n" +
                "                \"province\": \"Maharashtra\",\n" +
                "                \"country\": \"India\",\n" +
                "                \"zip\": \"410206\",\n" +
                "                \"phone\": \"+919028002190\",\n" +
                "                \"name\": \"Vedika Parthe\",\n" +
                "                \"province_code\": \"MH\",\n" +
                "                \"country_code\": \"IN\",\n" +
                "                \"country_name\": \"India\",\n" +
                "                \"default\": true\n" +
                "            }\n" +
                "        },\n" +
                "        \"discount_applications\": [],\n" +
                "        \"fulfillments\": [],\n" +
                "        \"line_items\": [\n" +
                "            {\n" +
                "                \"id\": 11411103481969,\n" +
                "                \"admin_graphql_api_id\": \"gid://shopify/LineItem/11411103481969\",\n" +
                "                \"destination_location\": {\n" +
                "                    \"id\": 3500573589617,\n" +
                "                    \"country_code\": \"IN\",\n" +
                "                    \"province_code\": \"MH\",\n" +
                "                    \"name\": \"Vedika Parthe\",\n" +
                "                    \"address1\": \"Round Grass Banglow\",\n" +
                "                    \"address2\": \"Near By Biknare Peth Street\",\n" +
                "                    \"city\": \"Panvel\",\n" +
                "                    \"zip\": \"410206\"\n" +
                "                },\n" +
                "                \"fulfillable_quantity\": 1,\n" +
                "                \"fulfillment_service\": \"manual\",\n" +
                "                \"fulfillment_status\": null,\n" +
                "                \"gift_card\": false,\n" +
                "                \"grams\": 0,\n" +
                "                \"name\": \"Green Tea Under Eye Cream - Roller Tube  - 20 ml\",\n" +
                "                \"origin_location\": {\n" +
                "                    \"id\": 2659531817073,\n" +
                "                    \"country_code\": \"IN\",\n" +
                "                    \"province_code\": \"PB\",\n" +
                "                    \"name\": \"Wow Skin Science\",\n" +
                "                    \"address1\": \"Khasra No. 709/828, 810/942, Prabhat Godown Area,\",\n" +
                "                    \"address2\": \"\",\n" +
                "                    \"city\": \"Zirakpur\",\n" +
                "                    \"zip\": \"140603\"\n" +
                "                },\n" +
                "                \"price\": \"399.00\",\n" +
                "                \"price_set\": {\n" +
                "                    \"shop_money\": {\n" +
                "                        \"amount\": \"399.00\",\n" +
                "                        \"currency_code\": \"INR\"\n" +
                "                    },\n" +
                "                    \"presentment_money\": {\n" +
                "                        \"amount\": \"399.00\",\n" +
                "                        \"currency_code\": \"INR\"\n" +
                "                    }\n" +
                "                },\n" +
                "                \"product_exists\": true,\n" +
                "                \"product_id\": 4495118270577,\n" +
                "                \"properties\": [],\n" +
                "                \"quantity\": 1,\n" +
                "                \"requires_shipping\": true,\n" +
                "                \"sku\": \"WOW_GT_UNDEREYE\",\n" +
                "                \"taxable\": true,\n" +
                "                \"title\": \"Green Tea Under Eye Cream - Roller Tube  - 20 ml\",\n" +
                "                \"total_discount\": \"0.00\",\n" +
                "                \"total_discount_set\": {\n" +
                "                    \"shop_money\": {\n" +
                "                        \"amount\": \"0.00\",\n" +
                "                        \"currency_code\": \"INR\"\n" +
                "                    },\n" +
                "                    \"presentment_money\": {\n" +
                "                        \"amount\": \"0.00\",\n" +
                "                        \"currency_code\": \"INR\"\n" +
                "                    }\n" +
                "                },\n" +
                "                \"variant_id\": 31741221077105,\n" +
                "                \"variant_inventory_management\": \"shopify\",\n" +
                "                \"variant_title\": \"\",\n" +
                "                \"vendor\": \"Wow Skin Science\",\n" +
                "                \"tax_lines\": [\n" +
                "                    {\n" +
                "                        \"channel_liable\": false,\n" +
                "                        \"price\": \"60.86\",\n" +
                "                        \"price_set\": {\n" +
                "                            \"shop_money\": {\n" +
                "                                \"amount\": \"60.86\",\n" +
                "                                \"currency_code\": \"INR\"\n" +
                "                            },\n" +
                "                            \"presentment_money\": {\n" +
                "                                \"amount\": \"60.86\",\n" +
                "                                \"currency_code\": \"INR\"\n" +
                "                            }\n" +
                "                        },\n" +
                "                        \"rate\": 0.18,\n" +
                "                        \"title\": \"IGST\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"duties\": [],\n" +
                "                \"discount_allocations\": []\n" +
                "            }\n" +
                "        ],\n" +
                "        \"payment_terms\": null,\n" +
                "        \"refunds\": [],\n" +
                "        \"shipping_address\": {\n" +
                "            \"first_name\": \"Vedika\",\n" +
                "            \"address1\": \"Round Grass Banglow\",\n" +
                "            \"phone\": \"+919028002190\",\n" +
                "            \"city\": \"Panvel\",\n" +
                "            \"zip\": \"410206\",\n" +
                "            \"province\": \"Maharashtra\",\n" +
                "            \"country\": \"India\",\n" +
                "            \"last_name\": \"Parthe\",\n" +
                "            \"address2\": \"Near By Biknare Peth Street\",\n" +
                "            \"company\": null,\n" +
                "            \"latitude\": null,\n" +
                "            \"longitude\": null,\n" +
                "            \"name\": \"Vedika Parthe\",\n" +
                "            \"country_code\": \"IN\",\n" +
                "            \"province_code\": \"MH\"\n" +
                "        },\n" +
                "        \"shipping_lines\": [\n" +
                "            {\n" +
                "                \"id\": 3810895724657,\n" +
                "                \"carrier_identifier\": null,\n" +
                "                \"code\": \"Free Shipping\",\n" +
                "                \"delivery_category\": null,\n" +
                "                \"discounted_price\": \"0.00\",\n" +
                "                \"discounted_price_set\": {\n" +
                "                    \"shop_money\": {\n" +
                "                        \"amount\": \"0.00\",\n" +
                "                        \"currency_code\": \"INR\"\n" +
                "                    },\n" +
                "                    \"presentment_money\": {\n" +
                "                        \"amount\": \"0.00\",\n" +
                "                        \"currency_code\": \"INR\"\n" +
                "                    }\n" +
                "                },\n" +
                "                \"phone\": null,\n" +
                "                \"price\": \"0.00\",\n" +
                "                \"price_set\": {\n" +
                "                    \"shop_money\": {\n" +
                "                        \"amount\": \"0.00\",\n" +
                "                        \"currency_code\": \"INR\"\n" +
                "                    },\n" +
                "                    \"presentment_money\": {\n" +
                "                        \"amount\": \"0.00\",\n" +
                "                        \"currency_code\": \"INR\"\n" +
                "                    }\n" +
                "                },\n" +
                "                \"requested_fulfillment_service_id\": null,\n" +
                "                \"source\": \"shopify\",\n" +
                "                \"title\": \"Free Shipping\",\n" +
                "                \"tax_lines\": [],\n" +
                "                \"discount_allocations\": []\n" +
                "            }\n" +
                "        ]\n" +
                "    }";
    }
}