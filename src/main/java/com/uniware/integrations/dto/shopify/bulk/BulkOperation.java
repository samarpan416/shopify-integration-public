package com.uniware.integrations.dto.shopify.bulk;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class BulkOperation {

    //    When the bulk operation was successfully completed.
    String completedAt;

    //    When the bulk operation was created.
    //@NotNull
    String createdAt;

    //    Error code for failed operations.
    BulkOperation errorCode;

    //    File size in bytes of the file in the url field.
    Long fileSize;

    //    A globally-unique identifier.
    @NotNull
    String id;

    //    A running count of all the objects processed. For example, when fetching all the products and their variants, this field counts both products and variants. This field can be used to track operation progress.
    Long objectCount;

    //    The URL that points to the partial or incomplete response data (in JSONL format) that was returned by a failed operation. The URL expires 7 days after the operation fails. Returns null when there's no data available.
    String partialDataUrl;

    //    GraphQL query document specified in bulkOperationRunQuery.
    //@NotNull
    String query;

    //    A running count of all the objects that are processed at the root of the query. For example, when fetching all the products and their variants, this field only counts products. This field can be used to track operation progress.
    //@NotNull
    Long rootObjectCount;

    //    Status of the bulk operation.
    //@NotNull
    BulkOperationStatus status;

    //    The bulk operation's type.
    //@NotNull
    BulkOperationType type;

    //     The URL that points to the response data in JSONL format. The URL expires 7 days after the operation completes.
    String url;
}
