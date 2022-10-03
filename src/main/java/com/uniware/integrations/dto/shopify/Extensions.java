package com.uniware.integrations.dto.shopify;

import lombok.Data;

@Data
public class Extensions {
    private Cost cost;
    public static class Cost{
        private int requestedQueryCost;
        private int actualQueryCost;
        private ThrottleStatus throttleStatus;
        public static class ThrottleStatus{
            private float maximumAvailable;
            private float currentlyAvailable;
            private int restoreRate;
        }
    }
}
