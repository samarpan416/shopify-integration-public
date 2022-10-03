package com.uniware.integrations.dto.uniware;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class UpdateInventoryResponse {
    private List<String> successfulChannelItemTypes;
    private Map<String, ChannelScriptError> failedChannelItemTypes;
}
