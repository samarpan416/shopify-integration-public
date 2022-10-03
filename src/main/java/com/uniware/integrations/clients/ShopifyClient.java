package com.uniware.integrations.clients;

import com.uniware.integrations.config.ShopifyClientConfig;
import com.uniware.integrations.dto.AccessScopesWrapper;

import com.uniware.integrations.dto.GetLocationByIdData;
import com.uniware.integrations.dto.GraphQLRequest;
import com.uniware.integrations.dto.GraphQLResponse;
import com.uniware.integrations.dto.shopify.*;
import com.uniware.integrations.dto.shopify.bulk.BulkOperationRunQueryPayloadWrapper;
import com.uniware.integrations.dto.shopify.bulk.BulkOperationPayloadWrapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(value = "shopify",configuration = {ShopifyClientConfig.class})
public interface ShopifyClient {
    @GetMapping(path="/users")
    List<Object> getStores();
    @GetMapping(path="/posts")
    List<Object> getPosts();
    @ResponseStatus(value =  HttpStatus.OK)
    @GetMapping("/orders.json")
    ResponseEntity<OrdersWrapper> getOrders(@RequestParam Map<String,Object> filterParams);
    @ResponseStatus(value =  HttpStatus.OK)
    @GetMapping(path="/orders/{id}.json")
    OrderWrapper getOrderById(@PathVariable String id);

    @GetMapping(path = "/access_scopes.json")
    AccessScopesWrapper getAccessScopes();

//    @PostMapping(path = "/graphql.json",consumes = MediaType.APPLICATION_JSON_VALUE)
//    <T> GraphQLResponse<T> doGraphQLRequest(@RequestBody GraphQLRequest graphQLRequest);
    @PostMapping(path = "/graphql.json",consumes = MediaType.APPLICATION_JSON_VALUE)
    GraphQLResponse<GetTransactionsData> getTransactions(@RequestBody GraphQLRequest graphQLRequest);
    @PostMapping(path = "/graphql.json",consumes = MediaType.APPLICATION_JSON_VALUE)
    GraphQLResponse<GetLocationByIdData> getLocationById(@RequestBody GraphQLRequest graphQLRequest);

    @PostMapping(path = "/graphql.json",consumes = MediaType.APPLICATION_JSON_VALUE)
    GraphQLResponse<BulkOperationRunQueryPayloadWrapper> postBulkOperationRunQuery(@RequestBody GraphQLRequest graphQLRequest);

    @PostMapping(path = "/graphql.json",consumes = MediaType.APPLICATION_JSON_VALUE)
    GraphQLResponse<BulkOperationPayloadWrapper> getCurrentBulkOperation(@RequestBody GraphQLRequest graphQLRequest);

    @PostMapping(path = "/graphql.json",consumes = MediaType.APPLICATION_JSON_VALUE)
    GraphQLResponse<BulkOperationPayloadWrapper> getBulkOperationDetails(@RequestBody GraphQLRequest graphQLRequest);

    @GetMapping(path ="/inventory_levels.json",consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<InventoryLevelsPayloadWrapper> getInventoryLevels(@RequestParam("inventory_item_ids") String inventoryItemIds, @RequestParam("location_ids") String locationIds);

    @PostMapping(path = "/graphql.json",consumes = MediaType.APPLICATION_JSON_VALUE)
    GraphQLResponse<InventoryBulkAdjustQuantityAtLocationPayloadWrapper> bulkAdjustInventoryAtLocation(@RequestBody GraphQLRequest graphQLRequest);
}
