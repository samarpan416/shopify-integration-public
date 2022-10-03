package com.uniware.integrations.dto.shopify.bulk;

import lombok.Data;

@Data
public class BulkOperationRunQueryPayload {
    BulkOperation bulkOperation;
    UserError[] userErrors;
}
