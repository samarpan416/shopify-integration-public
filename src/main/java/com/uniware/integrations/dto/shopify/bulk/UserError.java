package com.uniware.integrations.dto.shopify.bulk;

import lombok.Data;

@Data
public class UserError {
    String field;
    String message;
}
