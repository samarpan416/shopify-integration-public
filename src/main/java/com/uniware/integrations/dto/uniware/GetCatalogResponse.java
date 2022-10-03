package com.uniware.integrations.dto.uniware;

import lombok.Data;

import java.util.List;

@Data
public class GetCatalogResponse {
    private Metadata metadata;
    private List<WsChannelItemType> channelItemTypes;
    @Data
    public static class Metadata {
        private String bulkOperationId;
        private boolean bulkOperationComplete = false;
    }
}
