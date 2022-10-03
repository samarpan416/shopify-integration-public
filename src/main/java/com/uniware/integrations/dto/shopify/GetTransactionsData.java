package com.uniware.integrations.dto.shopify;

import lombok.Data;

import java.util.List;
@Data
public class GetTransactionsData {
    private Order order;
    @Data
    public static class Order {
        private List<Transaction> transactions;
    }
}
