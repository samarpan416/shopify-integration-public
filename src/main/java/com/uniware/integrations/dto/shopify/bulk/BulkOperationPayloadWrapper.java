package com.uniware.integrations.dto.shopify.bulk;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class BulkOperationPayloadWrapper {

    @JsonAlias({"currentBulkOperation", "node"})
    BulkOperation bulkOperation;
}
