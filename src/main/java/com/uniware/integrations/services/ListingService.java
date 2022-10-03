package com.uniware.integrations.services;

import com.google.gson.JsonObject;
import com.unifier.core.utils.JsonUtils;
import com.unifier.core.utils.StringUtils;
import com.uniware.integrations.clients.ShopifyClient;
import com.uniware.integrations.contexts.ShopifyRequestContext;
import com.uniware.integrations.dto.ApiResponse;
import com.uniware.integrations.dto.GraphQLQueries;
import com.uniware.integrations.dto.GraphQLRequest;
import com.uniware.integrations.dto.GraphQLResponse;
import com.uniware.integrations.dto.shopify.bulk.BulkOperation;
import com.uniware.integrations.dto.shopify.bulk.BulkOperationRunQueryPayloadWrapper;
import com.uniware.integrations.dto.shopify.bulk.BulkOperationPayloadWrapper;
import com.uniware.integrations.dto.shopify.bulk.BulkOperationStatus;
import com.uniware.integrations.dto.uniware.GetCatalogResponse;
import com.uniware.integrations.dto.uniware.GetCatalogResponse;
import com.uniware.integrations.dto.uniware.WsChannelItemType;
import com.uniware.integrations.dto.uniware.Attribute;
import com.uniware.integrations.exception.BadRequest;
import com.uniware.integrations.web.context.TenantRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ListingService {

    private static final Logger LOG = LoggerFactory.getLogger(ListingService.class);
    private static final String LISTINGS_BULK_QUERY_PREFIX = "query ListingsExport";

    @Autowired
    ShopifyClient client;

    public ApiResponse<GetCatalogResponse> fetchCatalog() {
        BulkOperation blockingBulkOperation = null;

        // 1. Initiate BulkOperationRunQuery
        BulkOperation initiatingBulkOperation = postBulkOperationRunQuery();
        // 2. IF BulkOperation NOT_CREATED [ID : IF_1]
        if (initiatingBulkOperation.getId() == null) {
            // 3. Fetch CurrentBulkOperation
            BulkOperation currBulkOperation = getCurrentBulkOperation();
            // 4. Check if CurrentBulkOperation is for LISTINGS [ID : IF_1.1]
            if (isCurrentBulkOperationForListings(currBulkOperation)) {
                blockingBulkOperation = currBulkOperation;
            } // 5. TODO : ELSE, throw ERROR [ID : IF-ELSE_1.1]
        }
        // 6. [ID : IF-ELSE_1]
        //else continue;

        // 7. SET bulkOperationId
        GetCatalogResponse getCatalogResponse = new GetCatalogResponse();
        GetCatalogResponse.Metadata catalogMetadata = new GetCatalogResponse.Metadata();
        getCatalogResponse.setMetadata(catalogMetadata);
        catalogMetadata.setBulkOperationId(blockingBulkOperation == null ? initiatingBulkOperation.getId() : blockingBulkOperation.getId());

        return ApiResponse.<GetCatalogResponse>success().data(getCatalogResponse).build();
    }

    // TODO : Refactor, generic method, only dynamic input is GraphQLRequest.query.
    // NOTE : There shouldn't be a scenario where GraphQLRequest.variables is required.
    private BulkOperation postBulkOperationRunQuery() {
        // 1. Create GraphQLRequest
        GraphQLRequest bulkOperationRunQueryRequest = new GraphQLRequest();
        bulkOperationRunQueryRequest.setQuery(GraphQLQueries.GET_LISTINGS_BULK_OPERATION_RUN_QUERY);
        LOG.info(bulkOperationRunQueryRequest.getQuery());

        // 2. HTTP Request
        GraphQLResponse<BulkOperationRunQueryPayloadWrapper> gqlResponse = client.postBulkOperationRunQuery(bulkOperationRunQueryRequest);

        // 3. Response Handling
        /* TODO : Handle for "errors"
                  and "userErrors", since "query" JSON field in GraphQL request is of "mutation".
        */
        // NOTE : Blocking BulkOperation will be known from BulkOperationRunQueryPayload.userError[].message.
        //        Message shall contain "A bulk query operation for this app and shop is already in progress"
        return gqlResponse.getData().getBulkOperationRunQueryPayload().getBulkOperation();
    }

    private BulkOperation getCurrentBulkOperation() {
        // 1. Create GraphQLRequest
        GraphQLRequest currentBulkOperationRequest = new GraphQLRequest();
        currentBulkOperationRequest.setQuery(GraphQLQueries.GET_CURRENT_BULK_OPERATION);
        LOG.info(currentBulkOperationRequest.getQuery());

        // 2. HTTP Request
        GraphQLResponse<BulkOperationPayloadWrapper> currentBulkOperationResponse = client.getCurrentBulkOperation(currentBulkOperationRequest);

        // 3. Response Handling
        // TODO : Handle for "errors"
        return currentBulkOperationResponse.getData().getBulkOperation();
    }

    private Boolean isCurrentBulkOperationForListings(BulkOperation currentBulkOperation) {
        return currentBulkOperation.getQuery().startsWith(LISTINGS_BULK_QUERY_PREFIX);
    }

    // TODO : Refactor, generic method, dynamic value is GraphQLRequest.variables
    private BulkOperation getBulkOperationDetails(String bulkOperationId) {
        // 1. Create GraphQLRequest
        GraphQLRequest bulkOperationDetailsRequest = new GraphQLRequest();
        bulkOperationDetailsRequest.setQuery(GraphQLQueries.GET_BULK_OPERATION_DETAILS);
        Map<String, Object> bulkOperationDetailsRequestVariables = new HashMap<>();
        bulkOperationDetailsRequestVariables.put("bulkOperationId", bulkOperationId);
        bulkOperationDetailsRequest.setVariables(bulkOperationDetailsRequestVariables);
        LOG.info(bulkOperationDetailsRequest.getQuery());

        // 2. HTTP Request
        GraphQLResponse<BulkOperationPayloadWrapper> currentBulkOperationResponse = client.getBulkOperationDetails(bulkOperationDetailsRequest);

        // 3. Response Handling
        // TODO : Handle for "errors"
        return currentBulkOperationResponse.getData().getBulkOperation();
    }

    public ApiResponse<GetCatalogResponse> pollJob(String bulkOperationId) {
        if(StringUtils.isBlank(bulkOperationId))
            throw BadRequest.builder().message("bulkOperationId cannot be blank").build();
        BulkOperation bulkOperation = getBulkOperationDetails(bulkOperationId);
        GetCatalogResponse getCatalogResponse = new GetCatalogResponse();
        GetCatalogResponse.Metadata catalogMetadata = new GetCatalogResponse.Metadata();
        getCatalogResponse.setMetadata(catalogMetadata);
        if (BulkOperationStatus.RUNNING.equals(bulkOperation.getStatus())) {
            catalogMetadata.setBulkOperationId(bulkOperationId);
            return ApiResponse.<GetCatalogResponse>success().data(getCatalogResponse).build();
        } else if (BulkOperationStatus.COMPLETED.equals(bulkOperation.getStatus())) {
            catalogMetadata.setBulkOperationComplete(true);
            getCatalogResponse.setChannelItemTypes(processOnCompleted(bulkOperation));
            return ApiResponse.<GetCatalogResponse>success().data(getCatalogResponse).build();
        }
        return ApiResponse.<GetCatalogResponse>failure().message("Oops!").build();
    }

    private List<WsChannelItemType> processOnCompleted(BulkOperation bulkOperation) {

        List<WsChannelItemType> cits = null;

        String fileUrl = bulkOperation.getUrl();
        // TODO : Set proper file-path (may be considered to be pick basepath from properties file)
        String bulkOperationLegacyId = bulkOperation.getId().split("/")[bulkOperation.getId().split("/").length-1];
        String filePath = "/tmp/" + bulkOperationLegacyId + ".jsonl";

        // 1. TODO : Check at LOCAL if File exists.

        // 2. IF FALSE, then Download file from BulkOperation
        downloadFile(fileUrl, filePath);

        // 3. Iterate over the file
        try (BufferedReader br = Files.newBufferedReader(Paths.get(filePath))) {
            cits = br.lines()
                    .map(line -> (JsonObject) JsonUtils.stringToJson(line))
                    .filter(lineJson -> lineJson.has("__parentId"))
                    .filter(lineJson -> belongsToCurrentLocation(lineJson))
                    .map(lineJson -> transform(lineJson))
                    .collect(Collectors.toList());
            /*
            String line = br.readLine();
            while (line != null) {
                JsonObject lineJson = (JsonObject) JsonUtils.stringToJson(line);
                if (!lineJson.has("__parentId")) {
                    line = br.readLine();
                    continue;
                } else if (!belongsToCurrentLocation(lineJson)){
                    line = br.readLine();
                    continue;
                } else {
                    try {
                        WsChannelItemType cit = transform(lineJson);

                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            }

             */
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
        TODO :
            1. Write to a file FILE_NAME.
            2. Upload file FILE_NAME to S3.
            3. Pass S3 URL to Uniware.
         */
        // 4. Return CIT objects

        return cits;
    }

    // TODO : Discuss about ExceptionHandling for transform(JsonObject).
    private WsChannelItemType transform(JsonObject inventoryLevelJson) {
        WsChannelItemType cit = new WsChannelItemType();

        // ALERT : Is it possible to Products without Variants? If YES, will there be any difference in JSON?
        // OBSERVATION : Variant is created for a Product by default.
        JsonObject inventoryItemJson = inventoryLevelJson.getAsJsonObject("item");
        JsonObject variantJson = inventoryItemJson.getAsJsonObject("variant");
        JsonObject productJson = variantJson.getAsJsonObject("product");

        // ChannelProductId
        String variantId = variantJson.get("legacyResourceId").getAsString();
        String productId = productJson.get("legacyResourceId").getAsString();
        String channelProductId = productId + "-" + variantId;
        cit.setChannelProductId(channelProductId);

        // SellerSkuCode
        String sellerSkuCode = inventoryItemJson.get("sku").isJsonNull() ? channelProductId : inventoryItemJson.get("sku").getAsString();
        cit.setSellerSkuCode(sellerSkuCode);

        // ChannelCode
        String channelCode = TenantRequestContext.current().getChannelCode();
        cit.setChannelCode(channelCode);

        // Live
        Boolean isLive = "ACTIVE".equalsIgnoreCase(productJson.get("status").getAsString());
        cit.setLive(isLive);

        // ProductName
        String productName = variantJson.get("displayName").getAsString();
        cit.setProductName(productName);

        // CurrentInventoryOnChannel
        // QUERY : Is there any need for using method "has", since fields are mentioned inn GraphQL query?
        if (inventoryLevelJson.has("available") && !inventoryLevelJson.get("available").isJsonNull()) {
            cit.setCurrentInventoryOnChannel(inventoryLevelJson.get("available").getAsInt());
        }

        // TODO : ImageUrls

        // WsChannelItemTypeAttributes
        List<Attribute> inventoryItemAttributes = new ArrayList<>(2);
        Attribute legacyIdOfInventoryItem = new Attribute();
        legacyIdOfInventoryItem.setName("inventoryItemLegacyId");
        legacyIdOfInventoryItem.setValue(inventoryItemJson.get("legacyResourceId").getAsString());
        inventoryItemAttributes.add(legacyIdOfInventoryItem);

        Attribute gqlIdOfInventoryItem = new Attribute();
        gqlIdOfInventoryItem.setName("inventoryItemGraphQlId");
        gqlIdOfInventoryItem.setValue(inventoryItemJson.get("id").getAsString());
        inventoryItemAttributes.add(gqlIdOfInventoryItem);

        cit.setAttributes(inventoryItemAttributes);

        return cit;
    }

    private Boolean belongsToCurrentLocation(JsonObject inventoryLevelJson) {
        LOG.info("InventoryLevelJson : {}", inventoryLevelJson);
        String inventoryLevelLocationId = inventoryLevelJson.getAsJsonObject("location").get("legacyResourceId").getAsString();
        LOG.info("InventoryLevelLocationId : {}, ShopifyRequestContext.LocationId : {}", inventoryLevelLocationId, ShopifyRequestContext.current().getLocationId());

        return inventoryLevelLocationId.equalsIgnoreCase(ShopifyRequestContext.current().getLocationId());
    }

    private void downloadFile(String fUrl, String fName) {
        // https://shopify.dev/api/usage/bulk-operations/queries#download-result-data
        try {
            URL url = new URL(fUrl);
            try (
                    ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
                    FileOutputStream fileOutputStream = new FileOutputStream(fName);
                    FileChannel fileChannel = fileOutputStream.getChannel();
            ) {
                fileOutputStream.getChannel()
                        .transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            } catch (Exception e) {
                // TODO : Exception Handling
                System.out.println(e);
            }
        } catch (Exception e) {
            // TODO : Exception Handling
            System.out.println(e);
        }
    }
}
