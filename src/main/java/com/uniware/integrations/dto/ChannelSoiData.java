package com.uniware.integrations.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.List;

@Data
public class ChannelSoiData {
    private List<StatusSyncSoi> statusSyncSois;
    private HashMap<String,List<StatusSyncSoi>> bundleCodeToStatusSyncSois;

    public boolean isBundleItem() {
        return  bundleCodeToStatusSyncSois != null;
    }
}
