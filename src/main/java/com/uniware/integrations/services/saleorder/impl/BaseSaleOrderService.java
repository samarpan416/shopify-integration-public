package com.uniware.integrations.services.saleorder.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;
import com.unifier.core.utils.CollectionUtils;
import com.unifier.core.utils.JsonUtils;
import com.unifier.core.utils.ValidatorUtils;
import com.unifier.scraper.sl.expression.SLExpression;
import com.unifier.scraper.sl.runtime.ScriptExecutionContext;
import com.uniware.integrations.LineItemMetadata;
import com.uniware.integrations.clients.ShopifyClient;
import com.uniware.integrations.contexts.ShopifyRequestContext;
import com.uniware.integrations.dto.*;
import com.unifier.core.utils.StringUtils;
import com.uniware.integrations.dto.shopify.*;
import com.uniware.integrations.dto.shopify.ShopifyAddress;
import com.uniware.integrations.exception.BadRequest;
import com.uniware.integrations.services.saleorder.ISaleOrderService;
import com.uniware.integrations.uniware.dto.saleOrder.request.*;
import com.uniware.integrations.utils.Pair;
import com.uniware.integrations.utils.ZipCodeUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.ParseException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.uniware.integrations.constants.ShopifyConstants.*;

@Service
@Qualifier("baseSaleOrderService")
public class BaseSaleOrderService implements ISaleOrderService {
    private static final Logger LOG = LoggerFactory.getLogger(BaseSaleOrderService.class);

    private static final String DEFAULT_PHONE = "9999999999";
    public static final String SHIPPING_METHOD_CODE = "STD";
    public static final String PRINTABLE_CHARACTERS_REGEX = "\\P{Print}";
//    public static final String SERVICE_HOSTNAME = getHostname();

    private static final ObjectMapper objectMapper = getObjectMapper();
    ShopifyClient shopifyClient;

    @Value("${service.public.url}")
    public String SERVICE_HOSTNAME;

    enum OrderState {
        UNFULFILLED, REFUNDED, PARTIALLY_REFUNDED, CANCELLED, FULFILLED
    }

    public enum QueryParamNames {
        UPDATED_AT_MIN, UPDATED_AT_MAX, PAGE_INFO, FULFILLMENT_STATUS, FINANCIAL_STATUS, FIELDS, STATUS, LIMIT;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    static EnumMap<OrderState, Map<String, Object>> orderStateToRequestParams = getOrderStatusToRequestParams();
    Map<String, List<Transaction>> orderIdToTransactions = new HashMap<>();

    @Autowired
    // https://reflectoring.io/constructor-injection/
    public BaseSaleOrderService(ShopifyClient shopifyClient) {
        this.shopifyClient = shopifyClient;
    }

    public static String getHostname() {
        try (InputStream hostnameIS = Runtime.getRuntime().exec("hostname").getInputStream();
             InputStreamReader hostnameISR = new InputStreamReader(hostnameIS);
             BufferedReader hostnameBR = new BufferedReader(hostnameISR);) {
            return hostnameBR.readLine();
        } catch (IOException e) {
            LOG.error("Error reading hostname. {}", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public String getPincodeDetails(String pincode) {
        return ZipCodeUtils.getStateCode(pincode);
    }

    private String getNextPageInfo(String link) {
        if (link == null) return null;
        String[] parts = link.split(",");
        String nextPart = parts.length > 1 ? parts[1] : parts[0];
        String nextPageInfoPattern = "(?<=page_info=)(.*)(?=>; rel=\"next\")";
        Matcher nextPageInfoMatcher = Pattern.compile(nextPageInfoPattern).matcher(nextPart);
        String nextPageInfo = null;
        while (nextPageInfoMatcher.find()) {
            nextPageInfo = nextPageInfoMatcher.group();
        }
        LOG.info("next page_info : {}", nextPageInfo);
        return nextPageInfo;
    }


    @Override
    public ResponseEntity<ApiResponse<Map<String, SaleOrder>>> getSaleOrders(LocalDate from, LocalDate to, String pageSize, String pageInfo, GetSaleOrdersRequest getSaleOrdersRequest) {
        validateRequestParams(from, to, pageSize);
        validateGetSaleOrdersRequest(getSaleOrdersRequest);
        Pair<String, List<Order>> paginatedOrders = getSaleOrdersInternal(from, to, pageSize, pageInfo, orderStateToRequestParams.get(OrderState.UNFULFILLED));
        String nextPageInfo = paginatedOrders.getFirst();

        HttpHeaders responseHeaders = new HttpHeaders();
        if (StringUtils.isNotBlank(nextPageInfo)) responseHeaders.set("Link", getNextPageUrl(nextPageInfo));
        Map<String, SaleOrder> saleOrderIdToWsSaleOrder = new HashMap<>();
        for (Order order : paginatedOrders.getSecond()) {
            List<ApiError> errors = order.validate();
            if (!errors.isEmpty()) {
                LOG.info("Skipping order {} . Errors: {}", order.getId(), errors);
                continue;
            }
            if (shouldFetchOrder(order, getSaleOrdersRequest.getConfigurationParameters(), getSaleOrdersRequest.getConnectorParameters()))
                saleOrderIdToWsSaleOrder.put(order.getId().toString(), prepareSaleOrder(order, getSaleOrdersRequest.getConfigurationParameters(), getSaleOrdersRequest.getConnectorParameters()));
            else
                LOG.info("Skipping order {}", order.getId());
        }
        return ResponseEntity.ok().headers(responseHeaders).body(ApiResponse.<Map<String, SaleOrder>>success().message("Orders fetched successfully").data(saleOrderIdToWsSaleOrder).build());
    }

    private void validateGetSaleOrdersRequest(GetSaleOrdersRequest getSaleOrdersRequest) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<GetSaleOrdersRequest>> validate = validator.validate(getSaleOrdersRequest);
        if (!validate.isEmpty()) {
            List<ApiError> errors = new ArrayList<>();
            validate.forEach(e -> {
                errors.add(new ApiError(e.getPropertyPath().toString(), e.getMessage()));
            });
            throw BadRequest.builder().message("Invalid request body").errors(errors).build();
        }
    }

    private void validateRequestParams(LocalDate from, LocalDate to, String pageSize) {
        List<ApiError> apiErrors = new ArrayList<>();

        if (!NumberUtils.isNumber(pageSize))
            apiErrors.add(new ApiError("pageSize", "should be a number"));
        apiErrors.addAll(validateDateRange(from, to));
        if (!apiErrors.isEmpty())
            throw BadRequest.builder().message("Invalid request param(s)").errors(apiErrors).build();
    }

    private List<String> getMissingAccessScopes() {
        AccessScopesWrapper accessScopesWrapper = shopifyClient.getAccessScopes();
        List<AccessScopesWrapper.AccessScope> shopAccessScopes = accessScopesWrapper.getAccessScopes();
        List<String> accessScopeHandles = shopAccessScopes.stream().map(AccessScopesWrapper.AccessScope::getHandle).collect(Collectors.toList());
        return REQUIRED_ACCESS_SCOPES.stream().filter(requiredAccessScope -> !accessScopeHandles.contains(requiredAccessScope)).collect(Collectors.toList());
    }

    private Pair<String, List<Order>> getSaleOrdersInternal(LocalDate from, LocalDate to, String pageSize, String pageInfo, Map<String, Object> additionalParams) {
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put(QueryParamNames.LIMIT.toString(), getPageSize(pageSize));

        boolean isFirstPage = StringUtils.isBlank(pageInfo);
        if (isFirstPage) {
            // Here both from and to are non-null
            if (from == null) from = LocalDate.now().minusDays(NumberUtils.toLong(DAYS_TO_FETCH_ORDERS_FOR));
            if (to == null) to = LocalDate.now();
            to = to.plusDays(1);
            requestParams.putAll(additionalParams);
            requestParams.put(QueryParamNames.UPDATED_AT_MIN.toString(), from.toString());
            requestParams.put(QueryParamNames.UPDATED_AT_MAX.toString(), to.toString());
        } else requestParams.put(QueryParamNames.PAGE_INFO.toString(), pageInfo);

        ResponseEntity<OrdersWrapper> response = shopifyClient.getOrders(requestParams);
        OrdersWrapper ordersWrapper = response.getBody();
        String linkHeaderValue = response.getHeaders().get("Link") == null ? null : response.getHeaders().get("Link").get(0);
        LOG.info("Link header : {}", linkHeaderValue);

        String nextPageInfo = getNextPageInfo(linkHeaderValue);
        List<Order> orders = ordersWrapper != null ? ordersWrapper.getOrders() : null;
        return new Pair<>(nextPageInfo, orders);
    }

    private List<ApiError> validateDateRange(LocalDate from, LocalDate to) {
        List<ApiError> apiErrors = new ArrayList<>();
        if (from == null && to == null) return apiErrors;
//        String responseMessage = "Invalid request param(s)";
        if (from == null) {
            apiErrors.add(new ApiError("to", "should be null"));
            return apiErrors;
        }
        if (to == null) {
            apiErrors.add(new ApiError("from", "should be null"));
            return apiErrors;
        }
        if (from.isAfter(to)) {
            apiErrors.add(new ApiError("from", "should be before to"));
            return apiErrors;
        }
        return Collections.emptyList();
    }

    public ResponseEntity<List<Pendency>> getPendencies(LocalDate from, LocalDate to, String pageSize, String pageInfo) {
        validateRequestParams(from, to, pageSize);
        Pair<String, List<Order>> paginatedOrders = null;
        List<Order> pendencyOrders = null;
        String currentPageInfo = pageInfo;
        do {
            paginatedOrders = getSaleOrdersInternal(from, to, pageSize, currentPageInfo, orderStateToRequestParams.get(OrderState.UNFULFILLED));
            pendencyOrders = paginatedOrders.getSecond().stream().filter(this::isPendency).collect(Collectors.toList());
            currentPageInfo = paginatedOrders.getFirst();
        } while (currentPageInfo != null && pendencyOrders.isEmpty());

        String nextPageInfo = paginatedOrders.getFirst();
        List<Pendency> pendencies = new ArrayList<>();
        for (Order order : pendencyOrders) {
            for (LineItem lineItem : order.getLineItems()) {
                String itemName = prepareItemName(lineItem);
                String channelProductId = prepareChannelProductId(lineItem);
                String itemSku = prepareItemSku(lineItem);
                // TODO : Check if we need to use fullfillable quantity here?
                pendencies.add(new Pendency(channelProductId, itemSku, itemName, lineItem.getQuantity()));
            }
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        if (StringUtils.isNotBlank(nextPageInfo)) responseHeaders.set("Link", getNextPageUrl(nextPageInfo));

        return ResponseEntity.ok().headers(responseHeaders).body(pendencies);
    }

    private String getNextPageUrl(String nextPageInfo) {
        return SERVICE_HOSTNAME + "/shopify/orders" + "?pageInfo=" + nextPageInfo;
    }

    private String getPageSize(String pageSize) {
        return NumberUtils.isNumber(pageSize) ? pageSize : PAGE_SIZE;
    }

    @Override
    public Object orderReconciliation(LocalDate from, LocalDate to, ConfigurationParameters configurationParameters, ConnectorParameters connectorParameters) {
        String nextPageInfo = null;
        List<CreateSaleOrderRequest> requests = new ArrayList<>();
        do {
            Pair<String, List<Order>> paginatedOrders = getSaleOrdersInternal(from, to, PAGE_SIZE, nextPageInfo, orderStateToRequestParams.get(OrderState.UNFULFILLED));
            nextPageInfo = paginatedOrders.getFirst();
            List<Order> orders = paginatedOrders.getSecond();
            orders.stream().filter(order -> shouldFetchOrder(order, configurationParameters, connectorParameters)).forEach(order -> {
                CreateSaleOrderRequest createSaleOrderRequest = prepareCreateSaleOrderRequest(order);
                LOG.info(createSaleOrderRequest.toString());
                requests.add(createSaleOrderRequest);
//                    uniwareClient.createSaleOrder(createSaleOrderRequest, null);
            });

        } while (StringUtils.isNotBlank(nextPageInfo));
        return requests;
    }

    @Override
    public ApiResponse<Order> getOrder(String id) {
        Order order = getOrderById(id);
        if (order == null) return ApiResponse.<Order>failure().message("Order not found").build();
//        String orderStr = "";
//        try {
//            orderStr = objectMapper.writeValueAsString(order);
//        } catch (Exception e) {
//            LOG.error("Error converting order to json");
//        }
        return ApiResponse.<Order>success().message("Order found").data(order).build();
    }

    @Override
    public String getSplitShipmentCondition() {
        return getSplitShipmentConditionInternal();
    }

    public String getSplitShipmentConditionInternal() {
        return "placeholder";
    }

    private Location getLocation(String id) {
        Map<String, Object> queryVariables = new HashMap<>();
        queryVariables.put("id", "gid://shopify/Location/" + id);
        GraphQLRequest getLocationByIdRequest = new GraphQLRequest(GraphQLQueries.GET_LOCATION_BY_ID, queryVariables);

        GraphQLResponse<GetLocationByIdData> getLocationByIdResponse = shopifyClient.getLocationById(getLocationByIdRequest);
        return getLocationByIdResponse.getData().getLocation();
    }

    public ApiResponse<Location> getLocationById(String id) {
        Location location = getLocation(id);

        if (location == null)
            throw BadRequest.builder().message("No location found with id " + id).build();

        return ApiResponse.<Location>success().message("Location fetched successfully").data(location).build();
    }

    private void validateConnectorParameters(ConnectorParameters connectorParameters) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<ConnectorParameters>> validate = validator.validate(connectorParameters);
        if (!validate.isEmpty()) {
            List<ApiError> errors = new ArrayList<>();
            validate.forEach(e -> {
                errors.add(new ApiError(e.getPropertyPath().toString(), e.getMessage()));
            });
            throw BadRequest.builder().message("Invalid request body").errors(errors).build();
        }
    }

    public ApiResponse<Object> verifyConnectors(ConnectorParameters connectorParameters) {
        validateConnectorParameters(connectorParameters);
        String locationId = connectorParameters.getLocationId();
        Location location = getLocation(locationId);
        if (location == null) {
            ApiError apiError = new ApiError("locationId", "not found on Shopify");
            throw BadRequest.builder().message("Verification failed").errors(Collections.singletonList(apiError)).build();
        }
        List<String> missingAccessScopes = getMissingAccessScopes();
        if (!missingAccessScopes.isEmpty()) {
            ApiError apiError = new ApiError(String.join(",", missingAccessScopes), "access scope(s) are missing");
            throw BadRequest.builder().message("Verification failed").errors(Collections.singletonList(apiError)).build();
        }
        return ApiResponse.<Object>success().message("Verification successful").build();
    }

    @Override
    public ApiResponse<CreateSaleOrderRequest> getCreateSaleOrderRequest(String orderId) {
        Order order = getOrderById(orderId);
        if (order == null) return ApiResponse.<CreateSaleOrderRequest>failure().message("Order not found").build();
        CreateSaleOrderRequest createSaleOrderRequest = prepareCreateSaleOrderRequest(order);
        return ApiResponse.<CreateSaleOrderRequest>success().data(createSaleOrderRequest).message("Order created").build();
    }

    @Override
    public CreateSaleOrderRequest prepareCreateSaleOrderRequest(Order order) {
        CreateSaleOrderRequest createSaleOrderRequest = new CreateSaleOrderRequest();
        createSaleOrderRequest.setSaleOrder(prepareSaleOrder(order, null, null));
        return createSaleOrderRequest;
    }

    Order getOrderById(String id) {
        OrderWrapper orderWrapper = shopifyClient.getOrderById(id);
        return orderWrapper.getOrder();
    }

    private static ObjectMapper getObjectMapper() {
        return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public <T> T stringToJson(String json, Class<T> typeOfT) throws JsonProcessingException {
        return objectMapper.readValue(json, typeOfT);
//        return stringToJsonUtil(json,typeOfT);
    }

    private void validateGetWsSaleOrderRequest(GetWsSaleOrderRequest getWsSaleOrderRequest) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<GetWsSaleOrderRequest>> validate = validator.validate(getWsSaleOrderRequest);
        if (!validate.isEmpty()) {
            List<ApiError> errors = new ArrayList<>();
            validate.forEach(e -> {
                errors.add(new ApiError(e.getPropertyPath().toString(), e.getMessage()));
            });
            throw BadRequest.builder().message("Invalid request body").errors(errors).build();
        }
        List<ApiError> validationErrors = getWsSaleOrderRequest.getOrder().validate();
        if (!validationErrors.isEmpty()) {
            throw BadRequest.builder().errors(validationErrors).message("Invalid order").build();
        }
    }

    public ApiResponse<SaleOrder> getWsSaleOrder(GetWsSaleOrderRequest getWsSaleOrderRequest) {
        LOG.info("getWsSaleOrderRequest: {}", getWsSaleOrderRequest);

        validateGetWsSaleOrderRequest(getWsSaleOrderRequest);

        Order order = getWsSaleOrderRequest.getOrder();

        return ApiResponse.<SaleOrder>success().message("WsSaleOrder created successfully").data(prepareSaleOrder(order, getWsSaleOrderRequest.getConfigurationParameters(), getWsSaleOrderRequest.getConnectorParameters())).build();
    }

    private List<Transaction> getTransactions(Order order) {
        List<Transaction> transactions = order.getTransactions();
        if (transactions == null) {
            transactions = fetchOrderTransactions(order.getAdminGraphqlApiId());
            order.setTransactions(transactions);
        }
        return transactions;
    }

    private List<Transaction> fetchOrderTransactions(String adminGraphqlApiId) {
        Map<String, Object> queryVars = new HashMap<>();
        queryVars.put("id", adminGraphqlApiId);
        GraphQLRequest graphQLRequest = new GraphQLRequest(GraphQLQueries.GET_SALE_ORDER_TRANSACTIONS, queryVars);
        GraphQLResponse<GetTransactionsData> graphQLResponse = shopifyClient.getTransactions(graphQLRequest);
        return graphQLResponse.getData().getOrder() == null ? null : graphQLResponse.getData().getOrder().getTransactions();
    }

    public SaleOrder prepareSaleOrder(Order order, ConfigurationParameters configurationParameters, ConnectorParameters connectorParameters) {
        SaleOrder saleOrder = new SaleOrder();
        String orderId = order.getId().toString();
        saleOrder.setCode(orderId);
        List<Transaction> transactions = getTransactions(order);
        boolean isCashOnDelivery = "cod".equals(getPaymentMode(transactions));
        saleOrder.setCashOnDelivery(isCashOnDelivery);
        boolean shouldIncludePrefixOrSuffix = "TRUE".equalsIgnoreCase(connectorParameters.getPrefix());
        if (shouldIncludePrefixOrSuffix) saleOrder.setDisplayOrderCode(order.getName());
        else saleOrder.setDisplayOrderCode(order.getOrderNumber().toString());

        if (order.getCheckoutId() != null) {
            HashMap<String, String> additionalInfo = new HashMap<>();
            additionalInfo.put("checkoutId", order.getCheckoutId());
            saleOrder.setAdditionalInfo(JsonUtils.objectToString(additionalInfo));
        }
        saleOrder.setTransactionId(order.getCheckoutId());
        saleOrder.setTransactionDate(order.getCreatedAt());
        List<String> paymentGatewayNames = order.getPaymentGatewayNames();
        if (!paymentGatewayNames.isEmpty())
            saleOrder.setPaymentMode(paymentGatewayNames.get(paymentGatewayNames.size() - 1));
        saleOrder.setDisplayOrderDateTime(order.getCreatedAt() + "+05:30");
        saleOrder.setNotificationMobile(getMobileNumber(order));
        saleOrder.setNotificationEmail(order.getEmail());
        saleOrder.setCurrencyCode(order.getCurrency());
        saleOrder.setCustomerGSTIN(getCustomerGSTIN(order.getNoteAttributes()));
        saleOrder.setTotalCashOnDeliveryCharges(getCodCharges(order.getShippingLines()));
        BigDecimal shippingCharges = getShippingCharges(order.getShippingLines());
        saleOrder.setTotalShippingCharges(shippingCharges);
        BigDecimal giftDiscount = getGiftDiscount(transactions);
        saleOrder.setTotalPrepaidAmount(getPrepaidAmount(order, shippingCharges, giftDiscount));
        saleOrder.setAddresses(prepareAddresses(order, configurationParameters.isStateCodeRequired()));
        saleOrder.setBillingAddress(new BillingAddress("1"));
        saleOrder.setShippingAddress(new ShippingAddress(String.valueOf(saleOrder.getAddresses().size())));
        saleOrder.setSaleOrderItems(prepareSaleOrderItems(order));
        saleOrder.setCustomFieldValues(prepareCustomFieldValues(configurationParameters.getCustomFieldsCustomization(), order, transactions));
        return saleOrder;
    }

    private String getShouldIncludePrefixOrSuffix() {
        return "TRUE";
    }

    private String getMobileNumber(Order order) {
        ShopifyAddress billingAddress = order.getBillingAddress();
        return billingAddress == null ? null : ValidatorUtils.getValidMobileOrNull(billingAddress.getPhone());
    }

    public List<CustomFieldValue> prepareCustomFieldValues(String customFieldsCustomization, Order order, List<Transaction> transactions) {
        List<CustomFieldValue> customFieldValues = new ArrayList<>();
        try {
            JsonElement customFieldsCustomizationJson = (JsonElement) JsonUtils.stringToJson(customFieldsCustomization);
            JsonArray customFieldsCustomizationsArray = customFieldsCustomizationJson.getAsJsonArray();
            ScriptExecutionContext context = ScriptExecutionContext.current();
            context.addVariable("receiptJson", getReceipt(transactions));
            context.addVariable("saleOrderJson", JsonUtils.stringToJson(objectMapper.writeValueAsString(order)));
            for (JsonElement customFieldCustomization : customFieldsCustomizationsArray) {
                JsonObject customFieldCustomizationObject = customFieldCustomization.getAsJsonObject();
                for (String customFieldName : customFieldCustomizationObject.keySet()) {
                    String customFieldValueExpression = customFieldCustomizationObject.get(customFieldName).getAsString();
                    try {
                        String customFieldValue = (String) SLExpression.compile(customFieldValueExpression).evaluate(context);
                        customFieldValues.add(new CustomFieldValue(customFieldName, customFieldValue));
                    } catch (ParseException e) {
                        LOG.info("Error parsing expression. {}", e.getMessage());
                    } catch (EvaluationException e) {
                        LOG.info("Error evaluating expression. {}", e.getMessage());
                    } catch (Exception e) {
                        LOG.info("Error adding custom field with name : {} , {}", customFieldName, e.getMessage());
                    }
                }
            }
        } catch (JsonSyntaxException | JsonProcessingException e) {
            LOG.error("Error parsing JSON for customFieldsCustomization {}", e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            LOG.error("Error with custom fields {}", e.getMessage());
            e.printStackTrace();
        }
        return customFieldValues;
    }

    private JsonElement getReceipt(List<Transaction> transactions) {
        JsonElement receiptJson = (JsonElement) JsonUtils.stringToJson("{}");
        for (Transaction transaction : transactions) {
            String receipt = transaction.getReceipt();
            if (StringUtils.isNotBlank(receipt)) receiptJson = (JsonElement) JsonUtils.stringToJson(receipt);
        }
        return receiptJson;
    }

    private SaleOrderItem prepareSaleOrderItem(Order order, LineItem lineItem, String code, int packetNumberForItem, BigDecimal itemTax) {
        SaleOrderItem saleOrderItem = new SaleOrderItem();
        saleOrderItem.setCode(code);
        saleOrderItem.setChannelSaleOrderItemCode(prepareChannelSaleOrderItemCode(lineItem));
        saleOrderItem.setItemSku(prepareItemSku(lineItem));
        saleOrderItem.setChannelProductId(prepareChannelProductId(lineItem));
        saleOrderItem.setItemName(prepareItemName(lineItem));
        saleOrderItem.setShippingMethodCode(SHIPPING_METHOD_CODE);
        saleOrderItem.setGiftMessage(prepareGiftMessage(lineItem));
        BigDecimal itemDiscount = prepareDiscount(lineItem);
        BigDecimal sellingPrice = prepareSellingPrice(lineItem, itemDiscount, itemTax);
        saleOrderItem.setDiscount(itemDiscount);
        saleOrderItem.setTotalPrice(sellingPrice);
        saleOrderItem.setSellingPrice(sellingPrice);
        saleOrderItem.setVoucherCode(prepareVoucherCode(order.getDiscountCodes()));
        saleOrderItem.setPacketNumber(packetNumberForItem);

        return saleOrderItem;
    }

    private String prepareChannelSaleOrderItemCode(LineItem lineItem) {
        return lineItem.getId().toString();
    }

    private String prepareItemSku(LineItem lineItem) {
        String sku = lineItem.getSku();
        if (StringUtils.isBlank(sku)) {
            return prepareChannelProductId(lineItem);
        }
        return sku;
    }

    private String prepareChannelProductId(LineItem lineItem) {
        String productId = lineItem.getProductId().toString();
        String variantId = lineItem.getVariantId().toString();

        if (StringUtils.isNotBlank(productId) && StringUtils.isNotBlank(variantId)) return productId + "-" + variantId;
        else if (StringUtils.isNotBlank(productId)) return productId;
        else {
            String itemName = prepareItemName(lineItem);
            return itemName.length() > 127 ? itemName.substring(0, 127) : itemName;
        }
    }

    private String prepareItemName(LineItem lineItem) {
        return lineItem.getName().length() > 199 ? lineItem.getName().substring(0, 199) : lineItem.getName();
    }

    private String prepareVoucherCode(List<DiscountCode> discountCodes) {
        for (DiscountCode discountCode : discountCodes) {
            String code = discountCode.getCode();
            if (StringUtils.isNotBlank(code)) {
                if (code.length() > 45) code = code.substring(0, 45);
                return code;
            }
        }
        return null;
    }

    private BigDecimal prepareSellingPrice(LineItem lineItem, BigDecimal itemDiscount, BigDecimal itemTax) {
        BigDecimal lineItemPrice = new BigDecimal(lineItem.getPrice());
        BigDecimal sellingPrice = lineItemPrice.add(itemTax).subtract(itemDiscount).setScale(2, RoundingMode.HALF_EVEN);
        return sellingPrice.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : sellingPrice;
    }

    private BigDecimal prepareDiscount(LineItem lineItem) {
        return lineItem.getDiscountAllocations().stream().reduce(BigDecimal.ZERO, (totalDiscount, discountAllocation) -> totalDiscount.add(new BigDecimal(discountAllocation.getAmount())), BigDecimal::add);
    }

    private String getChannelPackageType() {
        // Fetch from mongo
        return "placeholder";
    }

    private List<SaleOrderItem> prepareSaleOrderItems(Order order) {
        List<SaleOrderItem> saleOrderItems = new ArrayList<>();
        LinkedHashMap<String, Integer> boxIdToPacketNumberMap = new LinkedHashMap<>();
        int totalPackets = 1;

        String countryCode = getCountryCode(order);

        for (LineItem lineItem : order.getLineItems()) {
            if (lineItem.getQuantity() > 0) {
                int qty = lineItem.getQuantity();
                String channelSaleOrderItemCode = prepareChannelSaleOrderItemCode(lineItem);
                Pair<Integer, Integer> packetInfo = getPacketInfo(lineItem, countryCode, totalPackets, boxIdToPacketNumberMap);
                totalPackets = packetInfo.getFirst();
                int packetNumberForItem = packetInfo.getSecond();
                // TODO : Can't we just pass id-count both
                BigDecimal lineItemTaxPerQty = order.isTaxesIncluded() ? BigDecimal.ZERO : getLineItemTaxPerQty(lineItem, qty);
                if (qty == 1) {
                    saleOrderItems.add(prepareSaleOrderItem(order, lineItem, channelSaleOrderItemCode, packetNumberForItem, lineItemTaxPerQty));
                    totalPackets = updateTotalPackets(countryCode, totalPackets);
                } else {
                    for (int itemCount = 0; itemCount < qty; ++itemCount) {
                        String soiCode = channelSaleOrderItemCode + '-' + itemCount;
                        saleOrderItems.add(prepareSaleOrderItem(order, lineItem, soiCode, packetNumberForItem, lineItemTaxPerQty));
                        totalPackets = updateTotalPackets(countryCode, totalPackets);
                    }
                }
            } else {
                LOG.info("Skipping line item since it's cancelled on Shopify");
            }
        }
        return saleOrderItems;
    }

    private BigDecimal getLineItemTaxPerQty(LineItem lineItem, int qty) {
        // TODO: Use shop_money instead of price
        BigDecimal totalLineItemTax = lineItem.getTaxLines().stream().reduce(BigDecimal.ZERO, (totalTax, taxLine) -> {
            BigDecimal taxLineAmount = new BigDecimal(taxLine.getPriceSet().getShopMoney().getAmount());
            return totalTax.add(taxLineAmount);
        }, BigDecimal::add);
        return totalLineItemTax.divide(BigDecimal.valueOf(qty), 2, RoundingMode.HALF_EVEN);
    }

    private String getCountryCode(Order order) {
        ShopifyAddress billingAddress = order.getBillingAddress();
        return billingAddress != null ? billingAddress.getCountryCode() : null;
    }

    private Pair<Integer, Integer> getPacketInfo(LineItem lineItem, String countryCode, int totalPackets, LinkedHashMap<String, Integer> boxIdToPacketNumberMap) {
        String splitShipmentCondition = getSplitShipmentCondition();
        int packetNumberForItem = 0;
        String channelPackageType = getChannelPackageType();
        if ("FULFILLMENT".equalsIgnoreCase(splitShipmentCondition)) {
            if ("FULFILLED".equalsIgnoreCase(lineItem.getFulfillmentStatus())) packetNumberForItem = 2;
            else packetNumberForItem = 1;
        } else if ("CUSTOM".equalsIgnoreCase(splitShipmentCondition)) {
            Pair<Integer, Integer> packetInfo = getPacketInfoForCYOB(lineItem, totalPackets, packetNumberForItem, boxIdToPacketNumberMap);
            totalPackets = packetInfo.getFirst();
            packetNumberForItem = packetInfo.getSecond();
        } else if ("DOMESTIC".equalsIgnoreCase(splitShipmentCondition) && "IN".equalsIgnoreCase(countryCode)) {
            packetNumberForItem = totalPackets;
        } else if ("FIXED".equalsIgnoreCase(channelPackageType)) {
            // TODO : Check in backend code if packet number = 0 or 1 have some special handling or not
            packetNumberForItem = 1;
        }

        return new Pair<>(totalPackets, packetNumberForItem);
    }

    private Pair<Integer, Integer> getPacketInfoForCYOB(LineItem lineItem, int totalPackets, int packetNumberForItem, LinkedHashMap<String, Integer> boxIdToPacketNumberMap) {
        for (Property property : lineItem.getProperties()) {
            if ("_BID".equals(property.getName())) {
                String boxId = property.getValue();
                if (boxId != null) {
                    if (!boxIdToPacketNumberMap.containsKey(boxId)) boxIdToPacketNumberMap.put(boxId, totalPackets++);
                    packetNumberForItem = boxIdToPacketNumberMap.get(boxId);
                }
                break;
            }
        }
        return new Pair<>(totalPackets, packetNumberForItem);
    }

    private String prepareGiftMessage(LineItem lineItem) {
        List<String> giftMessages = new ArrayList<>();
        lineItem.getProperties().forEach(property -> {
            String message = property.getName() + ":" + property.getValue();
            giftMessages.add(message);
        });
        String giftMessage = String.join(",", giftMessages);
        if (giftMessage.length() > 256) giftMessage = giftMessage.substring(0, 256);
        giftMessage = giftMessage.replaceAll("[^\u0000-\uffff]", "");
        return giftMessage;
    }

    private Integer updateTotalPackets(String countryCode, int totalPackets) {
        String splitShipmentCondition = getSplitShipmentCondition();
        if ("DOMESTIC".equalsIgnoreCase(splitShipmentCondition) && "IN".equalsIgnoreCase(countryCode)) ++totalPackets;
        return totalPackets;
    }

    private List<Address> prepareAddresses(Order order, boolean stateCodeRequired) {
        ShopifyAddress shopifyBillingAddress = order.getBillingAddress();
        ShopifyAddress shopifyShippingAddress = order.getShippingAddress();
        List<Address> addresses = new ArrayList<>();
        addresses.add(prepareAddress(shopifyBillingAddress, "1", stateCodeRequired));

        if (shopifyBillingAddress != shopifyShippingAddress)
            addresses.add(prepareAddress(shopifyShippingAddress, "2", stateCodeRequired));
        return addresses;
    }

    private Address prepareAddress(ShopifyAddress shopifyAddress, String refId, boolean stateCodeRequired) {
        String lastName = StringUtils.getNotNullValue(shopifyAddress.getLastName());
        String name = shopifyAddress.getFirstName() + " " + lastName;
        name = name.replaceAll("[^\u0000-\uffff]", "");
        String addressLine1 = StringUtils.getNotNullValue(shopifyAddress.getAddress1());
        addressLine1 = addressLine1.replaceAll(PRINTABLE_CHARACTERS_REGEX, "");
        String company = StringUtils.getNotNullValue(shopifyAddress.getCompany());
        company = company.replaceAll(PRINTABLE_CHARACTERS_REGEX, "");
        if (StringUtils.isNotBlank(company)) addressLine1 = company + ", " + addressLine1;
        String addressLine2 = StringUtils.getNotNullValue(shopifyAddress.getAddress2());
        addressLine2 = addressLine2.replaceAll(PRINTABLE_CHARACTERS_REGEX, "");
        String pincode = shopifyAddress.getZip() != null ? StringUtils.removeNonWordChars(shopifyAddress.getZip()) : null;
        String state = shopifyAddress.getProvince();
        if (StringUtils.isBlank(state)) {
            if (!stateCodeRequired) state = shopifyAddress.getCity();
            else if ("INDIA".equalsIgnoreCase(shopifyAddress.getCountry())) {
                state = ZipCodeUtils.getStateCode(pincode);
            }
        }
        String countryCode = shopifyAddress.getCountryCode();
        String city = shopifyAddress.getCity();
        if (StringUtils.isNotBlank(countryCode)) {
            if ("SA".equalsIgnoreCase(countryCode)) {
                state = "SA-SA";
                city = shopifyAddress.getCity() + ", " + shopifyAddress.getProvince();
            }
            if ("SG".equalsIgnoreCase(countryCode)) {
                state = "SG-SG";
            }
        }
        String phone = StringUtils.isNotBlank(shopifyAddress.getPhone()) ? shopifyAddress.getPhone() : DEFAULT_PHONE;
        if ("IN".equalsIgnoreCase(shopifyAddress.getCountryCode()) && phone.length() >= 3 && "+91".equalsIgnoreCase(phone.substring(0, 3))) {
            phone = phone.substring(3);
        }

        Address address = new Address();
        address.setId(refId);
        address.setName(name);
        address.setAddressLine1(addressLine1);
        address.setAddressLine2(addressLine2);
        address.setCity(city);
        address.setState(state);
        address.setPincode(pincode);
        address.setCountry(shopifyAddress.getCountry());
        address.setPhone(phone);

        return address;
    }

    private BigDecimal getCodCharges(List<ShippingLine> shippingLines) {
        AtomicReference<String> codCharges = new AtomicReference<>();
        shippingLines.forEach(shippingLine -> {
            if (shippingLine.getTitle().contains("Cash on Delivery")) codCharges.set(shippingLine.getPrice());
        });
        return codCharges.get() != null ? new BigDecimal(codCharges.get()) : BigDecimal.ZERO;
    }

    private BigDecimal getShippingCharges(List<ShippingLine> shippingLines) {
        AtomicReference<String> shippingCharges = new AtomicReference<>();
        shippingLines.forEach(shippingLine -> {
            if (!shippingLine.getTitle().contains("Cash on Delivery")) shippingCharges.set(shippingLine.getPrice());
        });
        return shippingCharges.get() != null ? new BigDecimal(shippingCharges.get()) : BigDecimal.ZERO;
    }

    private BigDecimal getPrepaidAmount(Order order, BigDecimal shippingCharges, BigDecimal giftDiscount) {
        BigDecimal prepaidAmount = giftDiscount;
        BigDecimal subtotal = new BigDecimal(order.getTotalLineItemsPrice());
        BigDecimal totalOrderAmount = subtotal.add(shippingCharges);
        BigDecimal totalDiscount = new BigDecimal(order.getTotalDiscounts());

        if (prepaidAmount.add(totalDiscount).compareTo(totalOrderAmount) > 0)
            prepaidAmount = totalOrderAmount.subtract(totalDiscount);
        return prepaidAmount;
    }

    private String getCustomerGSTIN(List<NoteAttribute> noteAttributes) {
        AtomicReference<String> customerGstin = new AtomicReference<>();
        noteAttributes.forEach(noteAttribute -> {
            if ("CustomerGSTIN".equalsIgnoreCase(noteAttribute.getName())) customerGstin.set(noteAttribute.getValue());
        });
        return customerGstin.get();
    }

//    ShopifyAddress getBillingAddress(Order order) {
//        if (order.getBillingAddress() != null) return order.getBillingAddress();
//        ShopifyAddress customerDefaultAddress = getCustomerDefaultAddress(order);
//        return customerDefaultAddress != null ? customerDefaultAddress : getShippingAddress(order);
//    }
//
//    private ShopifyAddress getCustomerDefaultAddress(Order order) {
//        return order.getCustomer() != null ? order.getCustomer().getDefaultAddress() : null;
//    }
//
//    ShopifyAddress getShippingAddress(Order order) {
//        return order.getShippingAddress() != null ? order.getShippingAddress() : getCustomerDefaultAddress(order);
//    }

//    String getProvinceCode(Order order) {
//        ShopifyAddress shippingAddress = getShippingAddress(order);
//        ShopifyAddress billingAddress = getBillingAddress(order);
//        if (shippingAddress != null) return shippingAddress.getProvinceCode();
//        if (billingAddress != null) return billingAddress.getProvinceCode();
//        return null;
//    }

    @Override
    public List<Order> filterOrders(List<Order> orders, ConfigurationParameters configurationParameters, ConnectorParameters connectorParameters) {
        return orders.stream().filter(order -> shouldFetchOrder(order, configurationParameters, connectorParameters)).collect(Collectors.toList());
    }

    boolean shouldFetchOrder(Order order, ConfigurationParameters configurationParameters, ConnectorParameters connectorParameters) {
        if (isPendency(order)) {
            LOG.warn("Order {} is a pendency", order.getId());
            return false;
        }
        if (!order.isConfirmed()) {
            LOG.warn("Order {} not confirmed", order.getId());
            return false;
        }
        String channelLocationId = connectorParameters.getLocationId();
        boolean isPosEnabled = configurationParameters.isPosEnabled();
        if (!(isPosEnabled && order.getLocationId() != null && channelLocationId.equalsIgnoreCase(order.getLocationId().toString())) && !(!isPosEnabled && (order.getLocationId() == null || StringUtils.isEmpty(order.getLocationId().toString())))) {
            LOG.warn("Order {} not confirmed", order.getId());
            return false;
        }

        List<String> allowedProvinceCodesList = parseAsList(configurationParameters.getApplicableProvinceCodes(), ",");
        String provinceCode = order.getProvinceCode();
        if (!allowedProvinceCodesList.isEmpty() && !allowedProvinceCodesList.contains(provinceCode)) {
            LOG.warn("Order {} provinceCode {} not in applicableProvinceCodes {}", order.getId(), provinceCode, allowedProvinceCodesList);
            return false;
        }

        if (order.getFulfillmentStatus() != null && !Order.FulfillmentStatus.PARTIAL.equals(order.getFulfillmentStatus())) {
            LOG.warn("Order {} | Invalid fulfillmentStatus {}", order.getId(), order.getFulfillmentStatus());
            return false;
        }

        List<Transaction> transactions = getTransactions(order);
        String paymentMode = getPaymentMode(transactions);
        if ("prepaid".equals(paymentMode) && !Order.FinancialStatus.PAID.equals(order.getFinancialStatus())) {
            LOG.warn("Order {} | Invalid financialStatus {} | paymentMode {}", order.getId(), order.getFinancialStatus(), paymentMode);
            return false;
        }
        return true;
    }

    private String getChannelLocationId() {
        return ShopifyRequestContext.current().getLocationId();
    }

    List<String> parseAsList(String input, String delimiter) {
        if(StringUtils.isBlank(input))
            return Collections.emptyList();
        List<String> tagsList = Arrays.asList(StringUtils.getNotNullValue(input).split(delimiter));
        tagsList = tagsList.stream().map(String::trim).collect(Collectors.toList());
        return tagsList;
    }

    Set<String> parseTagsAsSet(String tags) {
        List<String> tagsList = parseAsList(tags, ",");
        return CollectionUtils.asSet(tagsList);
    }

    boolean isPendency(Order order) {
        Set<String> orderTags = parseTagsAsSet(order.getTags());
        Set<String> pendencyTags = parseTagsAsSet(getPendencyTags());
        Set<String> intersection = pendencyTags.stream().filter(orderTags::contains).collect(Collectors.toSet());
        return !intersection.isEmpty();
    }

    // TODO : Get from mongo
    private String getPendencyTags() {
        return "COD_CANCEL";
    }

    boolean isNotPendency(Order order) {
        return !isPendency(order);
    }

    String getPaymentMode(List<Transaction> transactions) {
        String paymentMode = "prepaid";
        for (Transaction transaction : transactions) {
            String gateway = transaction.getGateway();
            String status = transaction.getStatus();
            if (("PENDING".equalsIgnoreCase(status) || "SUCCESS".equalsIgnoreCase(status)) && containsAnyIgnoreCase(gateway, "COD", "cash on delivery", "cash_on_delivery")) {
                paymentMode = "cod";
                break;
            }
        }
        return paymentMode;
    }

    public static boolean containsAnyIgnoreCase(String input, String... values) {
        if (input == null) return false;

        for (String value : values) {
            if (input.toLowerCase().contains(value.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private BigDecimal getGiftDiscount(List<Transaction> transactions) {
        return transactions.stream().reduce(BigDecimal.ZERO, (giftDiscount, transaction) -> {
            if (!"gift_card".equalsIgnoreCase(transaction.getGateway()) || !"success".equalsIgnoreCase(transaction.getStatus()))
                return giftDiscount;
            BigDecimal transactionAmount = new BigDecimal(transaction.getAmountSet().getShopMoney().getAmount());
            if ("REFUND".equalsIgnoreCase(transaction.getKind())) return giftDiscount.subtract(transactionAmount);
            else return giftDiscount.add(transactionAmount);
        }, BigDecimal::add);
    }

    public static <T> T stringToJsonUtil(String json, Class<T> typeOfT) {
        return new GsonBuilder().create().fromJson(json, typeOfT);
    }

    private static EnumMap<OrderState, Map<String, Object>> getOrderStatusToRequestParams() {
        EnumMap<OrderState, Map<String, Object>> orderStateToRequestParams = new EnumMap<>(OrderState.class);
        String responseFields = "id,financial_status,fulfillment_status,status,refunds,cancelled_at";
        Map<String, Object> refundedQueryParams = new HashMap<>();
        refundedQueryParams.put(QueryParamNames.STATUS.toString(), "any");
        refundedQueryParams.put(QueryParamNames.FINANCIAL_STATUS.toString(), "refunded");
        refundedQueryParams.put(QueryParamNames.FIELDS.toString(), responseFields);
        orderStateToRequestParams.put(OrderState.REFUNDED, refundedQueryParams);

        Map<String, Object> partiallyRefundedQueryParams = new HashMap<>();
        partiallyRefundedQueryParams.put(QueryParamNames.STATUS.toString(), "any");
        partiallyRefundedQueryParams.put(QueryParamNames.FINANCIAL_STATUS.toString(), "partially_refunded");
        partiallyRefundedQueryParams.put(QueryParamNames.FIELDS.toString(), responseFields);
        orderStateToRequestParams.put(OrderState.PARTIALLY_REFUNDED, partiallyRefundedQueryParams);

        Map<String, Object> cancelledQueryParams = new HashMap<>();
        cancelledQueryParams.put(QueryParamNames.STATUS.toString(), "cancelled");
        cancelledQueryParams.put(QueryParamNames.FIELDS.toString(), responseFields);
        orderStateToRequestParams.put(OrderState.CANCELLED, cancelledQueryParams);

        Map<String, Object> fulfilledQueryParams = new HashMap<>();
        fulfilledQueryParams.put(QueryParamNames.STATUS.toString(), "any");
        fulfilledQueryParams.put(QueryParamNames.FULFILLMENT_STATUS.toString(), "shipped");
        fulfilledQueryParams.put(QueryParamNames.FIELDS.toString(), responseFields);
        orderStateToRequestParams.put(OrderState.FULFILLED, fulfilledQueryParams);

        Map<String, Object> unfulfilledQueryParams = new HashMap<>();
        fulfilledQueryParams.put(QueryParamNames.FULFILLMENT_STATUS.toString(), "unfulfilled");
        fulfilledQueryParams.put(QueryParamNames.FIELDS.toString(), responseFields);
        orderStateToRequestParams.put(OrderState.UNFULFILLED, unfulfilledQueryParams);
        return orderStateToRequestParams;
    }

    public ApiResponse<List<PushSaleOrderStatusRequest.WsSaleOrder.WsSaleOrderItem>> orderStatusSync(String orderId, SaleOrderStatusSyncRequest saleOrderStatusSyncRequest) {
        LOG.info("orderId: {} , saleOrderStatusSyncRequest: {}", orderId, saleOrderStatusSyncRequest);
        Map<String, String> soiCodeToUniwareStatusCode = orderStatusSyncInternal(orderId, saleOrderStatusSyncRequest.getStatusSyncSaleOrderItems(), saleOrderStatusSyncRequest.getShopifyOrderMetadata());
        List<PushSaleOrderStatusRequest.WsSaleOrder.WsSaleOrderItem> wsSaleOrderItems = new ArrayList<>();
        for (Map.Entry<String, String> soiCodeToUniwareStatusCodeEntry : soiCodeToUniwareStatusCode.entrySet()) {
            String soiCode = soiCodeToUniwareStatusCodeEntry.getKey();
            String uniwareStatus = soiCodeToUniwareStatusCodeEntry.getValue();
            PushSaleOrderStatusRequest.WsSaleOrder.WsSaleOrderItem wsSaleOrderItem = new PushSaleOrderStatusRequest.WsSaleOrder.WsSaleOrderItem();
            wsSaleOrderItem.setCode(soiCode);
            wsSaleOrderItem.setStatusCode(uniwareStatus);
            wsSaleOrderItems.add(wsSaleOrderItem);
        }
        return ApiResponse.<List<PushSaleOrderStatusRequest.WsSaleOrder.WsSaleOrderItem>>success().message("Status sync data prepared successfully").data(wsSaleOrderItems).build();
    }

    // Status sync
    private Map<String, String> orderStatusSyncInternal(String orderId, List<StatusSyncSoi> statusSyncSoiList, ShopifyOrderMetadata shopifyOrderMetadata) {
        HashMap<String, String> soiCodeToUniwareStatusCode = new HashMap<>();
        if (shopifyOrderMetadata.isOrderCancelled()) {
            LOG.info("Marking order : {} as CANCELLED in uniware", orderId);
            statusSyncSoiList.forEach(statusSyncSoi -> {
                if (statusSyncSoi.isCancellable()) soiCodeToUniwareStatusCode.put(statusSyncSoi.getCode(), "CANCELLED");
                else LOG.info("Not marking {} as cancelled since it's not cancellable in UC", statusSyncSoi.getCode());
            });
        } else {
            HashMap<String, ChannelSoiData> channelSoiCodeToData = prepareChannelSoiCodeToData(statusSyncSoiList);
            for (Map.Entry<String, LineItemMetadata> lineItemMetadataEntry : shopifyOrderMetadata.getLineItemIdToMetadata().entrySet()) {
                String lineItemId = lineItemMetadataEntry.getKey();
                LineItemMetadata lineItemMetadata = lineItemMetadataEntry.getValue();
                doLineItemCancellation(lineItemId, lineItemMetadata, channelSoiCodeToData.get(lineItemId), soiCodeToUniwareStatusCode);
                doLineItemCustomerReturn(lineItemId, lineItemMetadata, channelSoiCodeToData.get(lineItemId), soiCodeToUniwareStatusCode);
            }
            doOrderDispatch(shopifyOrderMetadata, statusSyncSoiList, soiCodeToUniwareStatusCode);
        }
        return soiCodeToUniwareStatusCode;
    }

    private HashMap<String, ChannelSoiData> prepareChannelSoiCodeToData(List<StatusSyncSoi> statusSyncSoiList) {
        HashMap<String, ChannelSoiData> channelSoiCodeToData = new HashMap<>();
        statusSyncSoiList.forEach(statusSyncSoi -> {
            String channelSoiCode = statusSyncSoi.getChannelSaleOrderItemCode();
            boolean isBundleItem = StringUtils.isNotBlank(statusSyncSoi.getCombinationIdentifier());
            ChannelSoiData channelSoiData = channelSoiCodeToData.computeIfAbsent(channelSoiCode, k -> new ChannelSoiData());
            List<StatusSyncSoi> statusSyncSois = null;
            if (isBundleItem) {
                HashMap<String, List<StatusSyncSoi>> bundleCodeToStatusSyncSois = channelSoiData.getBundleCodeToStatusSyncSois();
                if (bundleCodeToStatusSyncSois == null) {
                    bundleCodeToStatusSyncSois = new HashMap<>();
                    channelSoiData.setBundleCodeToStatusSyncSois(bundleCodeToStatusSyncSois);
                }
                statusSyncSois = bundleCodeToStatusSyncSois.computeIfAbsent(statusSyncSoi.getCombinationIdentifier(), k -> new ArrayList<>());
            } else {
                statusSyncSois = channelSoiData.getStatusSyncSois();
                if (statusSyncSois == null) {
                    statusSyncSois = new ArrayList<>();
                    channelSoiData.setStatusSyncSois(statusSyncSois);
                }
            }
            statusSyncSois.add(statusSyncSoi);
        });
        return channelSoiCodeToData;
    }

    private int getUcCancelledQtyForLineItem(ChannelSoiData channelSoiData) {
        if (channelSoiData.isBundleItem()) {
            HashMap<String, List<StatusSyncSoi>> bundleCodeToStatusSyncSois = channelSoiData.getBundleCodeToStatusSyncSois();
            AtomicInteger cancelledQty = new AtomicInteger();
            bundleCodeToStatusSyncSois.forEach((bundleCode, statusSyncSois) -> {
                int cancelledSoiCountInBundle = (int) statusSyncSois.stream().filter(statusSyncSoi -> "CANCELLED".equals(statusSyncSoi.getStatusCode())).count();
                int totalSoiCountInBundle = statusSyncSois.size();
                if (cancelledSoiCountInBundle == totalSoiCountInBundle) cancelledQty.incrementAndGet();
            });
            return cancelledQty.get();
        } else {
            return (int) channelSoiData.getStatusSyncSois().stream().filter(statusSyncSoi -> "CANCELLED".equals(statusSyncSoi.getStatusCode())).count();
        }
    }

    private int getUcCustomerReturnedQtyForLineItem(ChannelSoiData channelSoiData) {
        if (channelSoiData.isBundleItem()) {
            HashMap<String, List<StatusSyncSoi>> bundleCodeToStatusSyncSois = channelSoiData.getBundleCodeToStatusSyncSois();
            AtomicInteger returnedQty = new AtomicInteger();
            bundleCodeToStatusSyncSois.forEach((bundleCode, statusSyncSois) -> {
                int returnedSoiCountInBundle = (int) statusSyncSois.stream().filter(StatusSyncSoi::isReturned).count();
                int totalSoiCountInBundle = statusSyncSois.size();
                if (returnedSoiCountInBundle == totalSoiCountInBundle) returnedQty.incrementAndGet();
            });
            return returnedQty.get();
        } else {
            return (int) channelSoiData.getStatusSyncSois().stream().filter(StatusSyncSoi::isReturned).count();
        }
    }

    private void doLineItemCancellation(String lineItemId, LineItemMetadata lineItemMetadata, ChannelSoiData channelSoiData, HashMap<String, String> soiCodeToUniwareStatus) {
        int shopifyCancelledQty = lineItemMetadata.getCancelledQty();
        if (shopifyCancelledQty == 0) {
            LOG.info("No cancellation required for lineItemId : {} | shopifyCancelledQty : {}", lineItemId, shopifyCancelledQty);
            return;
        }

        int ucCancelledQty = getUcCancelledQtyForLineItem(channelSoiData);
        if (shopifyCancelledQty <= ucCancelledQty) {
            LOG.info("No cancellation required in UC for lineItemId : {}", lineItemId);
            return;
        }
        int qtyToCancelInUc = shopifyCancelledQty - ucCancelledQty;

        if (channelSoiData.isBundleItem()) {
            HashMap<String, List<StatusSyncSoi>> bundleCodeToStatusSyncSois = channelSoiData.getBundleCodeToStatusSyncSois();
            Map<Integer, List<String>> priorityToBundleCodes = new TreeMap<>(Collections.reverseOrder());
            channelSoiData.getBundleCodeToStatusSyncSois().forEach((bundleCode, statusSyncSois) -> {
                int preferredSoiCount = (int) statusSyncSois.stream().filter(statusSyncSoi -> StringUtils.equalsAny(statusSyncSoi.getStatusCode(), "UNFULFILLABLE", "ALTERNATE_SUGGESTED", "LOCATION_NOT_SERVICEABLE")).count();
                List<String> bundleCodes = priorityToBundleCodes.computeIfAbsent(preferredSoiCount, k -> new ArrayList<>());
                bundleCodes.add(bundleCode);
            });
            for (Map.Entry<Integer, List<String>> entry : priorityToBundleCodes.entrySet()) {
                List<String> bundleCodes = entry.getValue();
                if (qtyToCancelInUc == 0) break;
                for (String bundleCode : bundleCodes) {
                    if (qtyToCancelInUc == 0) break;
                    List<StatusSyncSoi> statusSyncSois = bundleCodeToStatusSyncSois.get(bundleCode);
                    List<String> soiCodesToCancelInBundle = new ArrayList<>();
                    for (StatusSyncSoi statusSyncSoi : statusSyncSois) {
                        if (statusSyncSoi.isCancellable()) {
                            soiCodesToCancelInBundle.add(statusSyncSoi.getCode());
                        }
                    }
                    LOG.info("bundleCode : {} , soiCountCancelledInBundle : {} , bundleSoiCodes size : {}", bundleCode, soiCodesToCancelInBundle.size(), statusSyncSois.size());
                    if (soiCodesToCancelInBundle.size() == statusSyncSois.size()) {
                        --qtyToCancelInUc;
                        soiCodesToCancelInBundle.forEach(soiCode -> soiCodeToUniwareStatus.put(soiCode, "CANCELLED"));
                    }
                }
            }
        } else {
            List<StatusSyncSoi> orderedStatusSyncSois = new ArrayList<>();
            List<StatusSyncSoi> otherStatusSyncSois = new ArrayList<>();
            channelSoiData.getStatusSyncSois().forEach(statusSyncSoi -> {
                if (StringUtils.equalsAny(statusSyncSoi.getStatusCode(), "UNFULFILLABLE", "ALTERNATE_SUGGESTED", "LOCATION_NOT_SERVICEABLE"))
                    orderedStatusSyncSois.add(statusSyncSoi);
                else if (!"CANCELLED".equalsIgnoreCase(statusSyncSoi.getStatusCode()))
                    otherStatusSyncSois.add(statusSyncSoi);
            });
            orderedStatusSyncSois.addAll(otherStatusSyncSois);
            for (StatusSyncSoi statusSyncSoi : orderedStatusSyncSois) {
                if (qtyToCancelInUc == 0) break;

                if (statusSyncSoi.isCancellable()) {
                    soiCodeToUniwareStatus.put(statusSyncSoi.getCode(), "CANCELLED");
                    --qtyToCancelInUc;
                }
            }
        }
        if (qtyToCancelInUc != 0) {
            LOG.info("No cancellable item found for {} in UC", lineItemId);
        }
    }

    private void doLineItemCustomerReturn(String lineItemId, LineItemMetadata lineItemMetadata, ChannelSoiData channelSoiData, HashMap<String, String> soiCodeToUniwareStatus) {
        int shopifyReturnedQty = lineItemMetadata.getReturnedQty();

        if (shopifyReturnedQty == 0) {
            LOG.info("No return required for lineItemId : {} since shopifyReturnedQty is 0", lineItemId);
            return;
        }

        int ucReturnedQty = getUcCustomerReturnedQtyForLineItem(channelSoiData);
        LOG.info("lineItemId : {} | ucCustomerReturnedQty: {} , shopifyReturnedQty: {}", lineItemId, ucReturnedQty, shopifyReturnedQty);
        if (shopifyReturnedQty <= ucReturnedQty) {
            LOG.info("No return required in UC for lineItemId : {}", lineItemId);
            return;
        }

        int qtyToReturnInUc = shopifyReturnedQty - ucReturnedQty;

        if (channelSoiData.isBundleItem()) {
            HashMap<String, List<StatusSyncSoi>> bundleCodeToStatusSyncSois = channelSoiData.getBundleCodeToStatusSyncSois();

            for (Map.Entry<String, List<StatusSyncSoi>> entry : bundleCodeToStatusSyncSois.entrySet()) {
                String bundleCode = entry.getKey();
                List<StatusSyncSoi> statusSyncSois = entry.getValue();
                if (qtyToReturnInUc == 0) break;

                List<String> soiCodesToReturnInBundle = new ArrayList<>();
                for (StatusSyncSoi statusSyncSoi : statusSyncSois) {
                    if (statusSyncSoi.isReversePickable()) {
                        soiCodesToReturnInBundle.add(statusSyncSoi.getCode());
                    }
                }
                LOG.info("bundleCode : {} , soiCountReturnedInBundle : {} , bundleSoiCodes size : {}", bundleCode, soiCodesToReturnInBundle.size(), statusSyncSois.size());
                if (soiCodesToReturnInBundle.size() == statusSyncSois.size()) {
                    --qtyToReturnInUc;
                    soiCodesToReturnInBundle.forEach(soiCode -> soiCodeToUniwareStatus.put(soiCode, "RETURN_EXPECTED"));
                }
            }
        } else {
            for (StatusSyncSoi statusSyncSoi : channelSoiData.getStatusSyncSois()) {
                if (qtyToReturnInUc == 0) break;

                if (statusSyncSoi.isReversePickable()) {
                    soiCodeToUniwareStatus.put(statusSyncSoi.getCode(), "RETURN_EXPECTED");
                    --qtyToReturnInUc;
                }
            }
        }
        if (qtyToReturnInUc != 0) {
            LOG.info("No returnable item found in UC for {}", lineItemId);
        }

    }


    private void doOrderDispatch(ShopifyOrderMetadata shopifyOrderMetadata, List<StatusSyncSoi> statusSyncSois, HashMap<String, String> soiCodeToUniwareStatus) {
        if (!shopifyOrderMetadata.isOrderFulfilled()) {
            LOG.info("Skipping dispatch since fulfillmentStatus is {}", shopifyOrderMetadata.getFulfillmentStatus());
            return;
        }
        // Backend can't handle cancelled and dispatched status together
        // https://unicommerce.atlassian.net/browse/AE-1368
        boolean noItemCancelled = soiCodeToUniwareStatus.values().stream().noneMatch("CANCELLED"::equals);
        if (noItemCancelled) {
            statusSyncSois.forEach(statusSyncSoi -> {
                if (!StringUtils.equalsAny(statusSyncSoi.getStatusCode(), "UNFULFILLABLE", "DISPATCHED", "DELIVERED", "REPLACED", "RESHIPPED", "CANCELLED", "LOCATION_NOT_SERVICEABLE")) {
                    soiCodeToUniwareStatus.put(statusSyncSoi.getCode(), "DISPATCHED");
                    LOG.info("Marking soi : {} as DISPATCHED | UC statusCode : {}", statusSyncSoi.getCode(), statusSyncSoi.getStatusCode());
                } else {
                    LOG.info("Not marking soi : {} as DISPATCHED since UC statusCode is {}", statusSyncSoi.getCode(), statusSyncSoi.getStatusCode());
                }
            });
        }
    }

    public ApiResponse<Map<String, ShopifyOrderMetadata>> statusSyncMetadata(LocalDate from, LocalDate to, String pageSize) {
        validateRequestParams(from, to, pageSize);
        Map<String, ShopifyOrderMetadata> saleOrderCodeToMetadata = new HashMap<>();
        List<OrderState> orderStatesToSync = new ArrayList<>();
        orderStatesToSync.add(OrderState.REFUNDED);
        orderStatesToSync.add(OrderState.PARTIALLY_REFUNDED);
        orderStatesToSync.add(OrderState.CANCELLED);
        orderStatesToSync.add(OrderState.FULFILLED);
        for (OrderState orderState : orderStatesToSync) {
            String nextPageInfo = null;
            do {
                Pair<String, List<Order>> paginatedOrders = getSaleOrdersInternal(from, to, pageSize, nextPageInfo, orderStateToRequestParams.get(orderState));
                nextPageInfo = paginatedOrders.getFirst();
                List<Order> orders = paginatedOrders.getSecond();
                orders.forEach(order -> {
                    String saleOrderCode = order.getId().toString();

                    if (saleOrderCodeToMetadata.containsKey(saleOrderCode)) {
                        LOG.warn("Skipping sale order {} since it has already been handled", saleOrderCode);
                    } else {
                        saleOrderCodeToMetadata.put(saleOrderCode, getShopifyOrderMetadata(order));
                    }
                });
            } while (StringUtils.isNotBlank(nextPageInfo));
        }
        return ApiResponse.<Map<String, ShopifyOrderMetadata>>success().data(saleOrderCodeToMetadata).message("Successfully fetched orders metadata").build();
    }

    public ShopifyOrderMetadata getShopifyOrderMetadata(String id) {
        Order order = getOrderById(id);
        if (order == null) return null;
        return getShopifyOrderMetadata(order);
    }

    private ShopifyOrderMetadata getShopifyOrderMetadata(Order order) {
        String saleOrderCode = order.getId().toString();
        Order.FulfillmentStatus fulfillmentStatus = order.getFulfillmentStatus();
        Order.FinancialStatus financialStatus = order.getFinancialStatus();
        ShopifyOrderMetadata shopifyOrderMetadata = new ShopifyOrderMetadata();
        boolean isOrderCancelled = order.getCancelledAt() != null;
        shopifyOrderMetadata.setOrderCancelled(isOrderCancelled);
        if (isOrderCancelled) LOG.info("Order : {} is cancelled on Shopify", saleOrderCode);
        else {
            if (Order.FulfillmentStatus.FULFILLED.equals(fulfillmentStatus))
                shopifyOrderMetadata.setFulfillmentStatus(fulfillmentStatus.toString());
            if (financialStatus != null && StringUtils.equalsAny(financialStatus, Order.FinancialStatus.PARTIALLY_REFUNDED, Order.FinancialStatus.REFUNDED)) {
                Map<String, LineItemMetadata> lineItemIdToMetadata = shopifyOrderMetadata.getLineItemIdToMetadata();
                updateLineItemMetadata(order.getRefunds(), lineItemIdToMetadata);
            }
        }
        return shopifyOrderMetadata;
    }

    private void updateLineItemMetadata(List<Refund> refunds, Map<String, LineItemMetadata> lineItemIdToMetadata) {
        refunds.forEach(refund -> refund.getRefundLineItems().forEach(refundLineItem -> {
            RefundLineItem.RestockType restockType = refundLineItem.getRestockType();
            String lineItemId = refundLineItem.getLineItemId().toString();
            int refundedQty = refundLineItem.getQuantity();
            LineItemMetadata lineItemMetadata = lineItemIdToMetadata.computeIfAbsent(lineItemId, s -> new LineItemMetadata());
            switch (restockType) {
                case CANCEL:
                    lineItemMetadata.setCancelledQty(lineItemMetadata.getCancelledQty() + refundedQty);
                    break;
                case RETURN:
                    lineItemMetadata.setReturnedQty(lineItemMetadata.getReturnedQty() + refundedQty);
                    break;
                default:
                    LOG.info("Skipping refund for line item : {} , restockType : {}", lineItemId, restockType);
            }
        }));

    }
}