package com.uniware.integrations.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class StatusSyncSoi {
    @NotEmpty
    private String code;
    @NotEmpty
    private String statusCode;
    @NotNull
    private boolean cancellable;
    @NotNull
    private boolean returned;
    @NotNull
    private boolean reversePickable;
    @NotEmpty
    private String channelSaleOrderItemCode;
    @NotEmpty
    private String combinationIdentifier;
}
