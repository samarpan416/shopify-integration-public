package com.uniware.integrations.dto;

public class GraphQLQueries {
    public static final String GET_LOCATIONS_QUERY = "query ($locationId: ID!) { \n" +
            "        location(id:$locationId) {\n" +
            "            id\n" +
            "        }\n" +
            "    }";
    public static final String GET_LISTINGS_BULK_OPERATION_RUN_QUERY = "mutation {\n" +
            "    bulkOperationRunQuery(\n" +
            "        query: \"\"\"\n" +
            "        {\n" +
            "             inventoryItems\n" +
            "             {\n" +
            "                 edges\n" +
            "                 {\n" +
            "                 node\n" +
            "                 {\n" +
            "                     id\n" +
            "                     inventoryLevels\n" +
            "                     {\n" +
            "                     edges\n" +
            "                     {\n" +
            "                         node{\n" +
            "                            id\n" +
            "                            location\n" +
            "                            {\n" +
            "                                legacyResourceId\n" +
            "                            }\n" +
            "                            available\n" +
            "                            incoming\n" +
            "                            item{\n" +
            "                                countryCodeOfOrigin\n" +
            "                                id\n" +
            "                                legacyResourceId\n" +
            "                                sku\n" +
            "                                unitCost {\n" +
            "                                    amount\n" +
            "                                    currencyCode\n" +
            "                                }\n" +
            "                                variant {\n" +
            "                                    displayName\n" +
            "                                    id\n" +
            "                                    legacyResourceId title image{originalSrc}\n" +
            "                                    product{\n" +
            "                                        id\n" +
            "                                        legacyResourceId\n" +
            "                                        status\n" +
            "                                        title\n" +
            "                                        featuredImage\n" +
            "                                        {\n" +
            "                                            originalSrc\n" +
            "                                        }\n" +
            "                                    }\n" +
            "                                }\n" +
            "                            }\n" +
            "                         }\n" +
            "                     }\n" +
            "                     }\n" +
            "                     locationsCount\n" +
            "                 }\n" +
            "                 }\n" +
            "             }\n" +
            "         }\n" +
            "        \"\"\"\n" +
            "    ) {\n" +
            "        bulkOperation {\n" +
            "            id\n" +
            "            status\n" +
            "            query\n" +
            "            errorCode\n" +
            "            createdAt\n" +
            "            completedAt\n" +
            "            objectCount\n" +
            "            fileSize\n" +
            "            url\n" +
            "            partialDataUrl\n" +
            "        }\n" +
            "        userErrors {\n" +
            "            field\n" +
            "            message\n" +
            "        }\n" +
            "    }\n" +
            "}\n";

    //TODO : Identify whether GET_BULK_OPERATION_DETAILS provides details for several "bulkOperationIds" or just one at a time.
    public static final String GET_BULK_OPERATION_DETAILS =
            "query ($bulkOperationId: ID!){\n" +
            "        node(id: $bulkOperationId)\n" +
            "        {\n" +
            "            ... on BulkOperation\n" +
            "            {\n" +
            "                id\n" +
            "                status\n" +
            "                errorCode\n" +
            "                createdAt\n" +
            "                completedAt\n" +
            "                objectCount\n" +
            "                fileSize\n" +
            "                url\n" +
            "                partialDataUrl\n" +
            "            }\n" +
            "        }\n" +
            "    }";
    public static final String INVENTORY_BULK_ADJUST_AT_LOCATION_MUTATION =
            "mutation inventoryBulkAdjustQuantityAtLocation(\n" +
            "    $inventoryItemAdjustments: [InventoryAdjustItemInput!]!\n" +
            "    $locationId: ID!\n" +
            ") {\n" +
            "    inventoryBulkAdjustQuantityAtLocation(\n" +
            "        inventoryItemAdjustments: $inventoryItemAdjustments\n" +
            "        locationId: $locationId\n" +
            "    ) {\n" +
            "        inventoryLevels {\n" +
            "            id\n" +
            "            available\n" +
            "        }\n" +
            "        userErrors {\n" +
            "            field\n" +
            "            message\n" +
            "        }\n" +
            "    }\n" +
            "}";
    public static final String GET_SALE_ORDER_TRANSACTIONS = "query($id:ID!) {\n" +
            "    order(id: $id) {\n" +
            "        transactions {\n" +
            "            amountSet {\n" +
            "                shopMoney {\n" +
            "                    amount\n" +
            "                    currencyCode\n" +
            "                }\n" +
            "            }\n" +
            "            gateway\n" +
            "            kind\n" +
            "            status\n" +
            "            receipt\n" +
            "        }\n" +
            "    }\n" +
            "}";
    public static final String GET_LOCATION_BY_ID = "query($id:ID!) {\n" +
            "    location(id: $id) {\n" +
            "        id\n" +
            "    }\n" +
            "}";

    public static final String GET_CURRENT_BULK_OPERATION =
            "{\n" +
            "  currentBulkOperation {\n" +
            "    id\n" +
            "    status\n" +
            "    query\n" +
            "    errorCode\n" +
            "    createdAt\n" +
            "    completedAt\n" +
            "    objectCount\n" +
            "    fileSize\n" +
            "    url\n" +
            "    partialDataUrl\n" +
            "  }\n" +
            "}";
}
