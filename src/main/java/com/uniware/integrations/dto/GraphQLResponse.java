package com.uniware.integrations.dto;

import com.uniware.integrations.dto.shopify.Extensions;
import lombok.Data;

@Data
public class GraphQLResponse<T> {
    private T data;
    private Extensions extensions;
    /* TODO : "errors" JSON field support
              https://shopify.dev/api/admin-graphql#status_and_error_codes
     */
}
