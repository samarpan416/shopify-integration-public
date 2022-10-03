package com.uniware.integrations.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unifier.core.utils.StringUtils;
import com.uniware.integrations.clients.ShopifyClient;
import com.uniware.integrations.contexts.ShopifyRequestContext;
import com.uniware.integrations.dto.ApiResponse;
import com.uniware.integrations.dto.GraphQLQueries;
import com.uniware.integrations.dto.GraphQLRequest;
import com.uniware.integrations.dto.GraphQLResponse;
import com.uniware.integrations.dto.shopify.*;
import com.uniware.integrations.dto.uniware.Attribute;
import com.uniware.integrations.dto.uniware.ChannelScriptError;
import com.uniware.integrations.dto.uniware.UpdateInventoryRequest;
import com.uniware.integrations.dto.uniware.UpdateInventoryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    ShopifyClient client;

    @Autowired
    public InventoryService(ShopifyClient client) {
        this.client = client;
    }


    private boolean areRequiredAttributesPresent(UpdateInventoryRequest.InventoryUpdateItem inventoryUpdateItem) {
        return inventoryUpdateItem.getAttributes().stream().map(Attribute::getName).filter(name -> name.equalsIgnoreCase("inventoryItemLegacyId") || name.equalsIgnoreCase("inventoryItemGraphQlId")).collect(Collectors.toSet()).size() == 2;
    }

    private boolean isTrackingEnabled(InventoryLevel inventoryLevel) {
        // TODO : Check what happens when JSON field has JsonNull value?
        return (inventoryLevel.getAvailable() != null);
    }

    public ApiResponse<UpdateInventoryResponse> updateInventory(UpdateInventoryRequest updateInventoryRequest) {
        // NOTE : To keep one-to-one mapping b/w GET and POST, let's SET inventory.update.count to 50.

        Map<String, String> inventoryItemIdToChannelProductId = new HashMap<>();
        Map<String, UpdateInventoryRequest.InventoryUpdateItem> channelProductIdToUpdateInventoryRequest = new HashMap<>();
        Map<String, InventoryItemAdjustment> inventoryItemIdToInventoryItemAdjustment = new HashMap<>();
        UpdateInventoryResponse updateInventoryResponse = new UpdateInventoryResponse();
        Map<String, ChannelScriptError> failedChannelItemTypes = new HashMap<>();
        List<String> successfulChannelItemTypes = new ArrayList<>();
        updateInventoryResponse.setFailedChannelItemTypes(failedChannelItemTypes);
        updateInventoryResponse.setSuccessfulChannelItemTypes(successfulChannelItemTypes);

        for (UpdateInventoryRequest.InventoryUpdateItem inventoryUpdateItem : updateInventoryRequest.getInventoryUpdateItems()) {
            channelProductIdToUpdateInventoryRequest.put(inventoryUpdateItem.getChannelProductId(), inventoryUpdateItem);
            if (!areRequiredAttributesPresent(inventoryUpdateItem)) {
                // TODO : Add error for the updateInventoryDTO
                failedChannelItemTypes.put(inventoryUpdateItem.getChannelProductId(), new ChannelScriptError(ChannelScriptError.ScriptErrorCodes.DISABLED, "Required attributes missing"));
            } else {

                String inventoryItemLegacyId = inventoryUpdateItem.getAttributeValue("inventoryItemLegacyId");

                inventoryItemIdToChannelProductId.put(inventoryItemLegacyId, inventoryUpdateItem.getChannelProductId());

                inventoryItemIdToInventoryItemAdjustment.put(inventoryItemLegacyId, new InventoryItemAdjustment(inventoryUpdateItem));
            }
        }
        if (inventoryItemIdToInventoryItemAdjustment.size() == 0)
            return ApiResponse.<UpdateInventoryResponse>failure().message("No valid items for inventory update").data(updateInventoryResponse).build();

        String inventoryItemIds = StringUtils.join(inventoryItemIdToInventoryItemAdjustment.keySet());

        ResponseEntity<InventoryLevelsPayloadWrapper> inventoryLevelsResponse = client.getInventoryLevels(inventoryItemIds, ShopifyRequestContext.current().getLocationId());
        if (inventoryLevelsResponse.getBody() == null)
            return ApiResponse.<UpdateInventoryResponse>builder().status(getUpdateInventoryResponseStatus(failedChannelItemTypes, successfulChannelItemTypes)).message("Inventory update done").build();

        List<InventoryLevel> inventoryLevels = inventoryLevelsResponse.getBody().getInventoryLevels();

        for (InventoryLevel inventoryLevel : inventoryLevels) {
            if (!isTrackingEnabled(inventoryLevel)) {
                failedChannelItemTypes.put(inventoryLevel.getInventoryItemId(), new ChannelScriptError(ChannelScriptError.ScriptErrorCodes.DISABLED, "Tracking disabled on Shopify"));
            } else {
                InventoryItemAdjustment inventoryItemAdjustment = inventoryItemIdToInventoryItemAdjustment.get(inventoryLevel.getInventoryItemId());
                //successfulChannelItemTypes.add(inventoryLevel.getInventoryItemId());
                // TODO : There might be a possibility that "AvailableDelta = 0". Need to ignore those in the GraphQL request.
                //          Or just need to know about the behaviour in case delta is 0. [There is no bad result of setting delta to 0.]
                inventoryItemAdjustment.setAvailableDelta(inventoryItemAdjustment.getAvailableDelta() - inventoryLevel.getAvailable());
            }
        }

        Map<String, Object> gqlRequestVariables = new HashMap<>();
        gqlRequestVariables.put("inventoryItemAdjustments", inventoryItemIdToInventoryItemAdjustment.values());
        gqlRequestVariables.put("locationId", "gid://shopify/Location/" + ShopifyRequestContext.current().getLocationId());

        GraphQLRequest inventoryBulkAdjustQuantityAtLocationRequest = new GraphQLRequest();
        inventoryBulkAdjustQuantityAtLocationRequest.setQuery(GraphQLQueries.INVENTORY_BULK_ADJUST_AT_LOCATION_MUTATION);
        inventoryBulkAdjustQuantityAtLocationRequest.setVariables(gqlRequestVariables);

        //GraphQLRequest
        GraphQLResponse<InventoryBulkAdjustQuantityAtLocationPayloadWrapper> inventoryBulkAdjustQuantityAtLocationPayload = client.bulkAdjustInventoryAtLocation(inventoryBulkAdjustQuantityAtLocationRequest);

        if (inventoryBulkAdjustQuantityAtLocationPayload.getData().getInventoryBulkAdjustQuantityAtLocation().getUserErrors() == null || inventoryBulkAdjustQuantityAtLocationPayload.getData().getInventoryBulkAdjustQuantityAtLocation().getUserErrors().isEmpty()) {
            for (InventoryLevelGQL inventoryLevel : inventoryBulkAdjustQuantityAtLocationPayload.getData().getInventoryBulkAdjustQuantityAtLocation().getInventoryLevels()) {
                String successfulInventoryItemId = inventoryLevel.getId().split("=")[1];
                //System.out.println("successfulInventoryItemId : " + successfulInventoryItemId);
                successfulChannelItemTypes.add(inventoryItemIdToChannelProductId.get(successfulInventoryItemId));
            }
        }


        // POST Adjustments at Channel
        return ApiResponse.<UpdateInventoryResponse>builder().status(getUpdateInventoryResponseStatus(failedChannelItemTypes, successfulChannelItemTypes)).message("Inventory update done").data(updateInventoryResponse).build();
    }

    private ApiResponse.STATUS getUpdateInventoryResponseStatus(Map<String, ChannelScriptError> failedChannelItemTypes, List<String> successfulChannelItemTypes) {
        if ((failedChannelItemTypes.isEmpty() && successfulChannelItemTypes.isEmpty()) ||
                (!failedChannelItemTypes.isEmpty() && successfulChannelItemTypes.isEmpty())) {
            return ApiResponse.STATUS.FAILURE;
        } else {
            return ApiResponse.STATUS.SUCCESS;
        }
    }


}
