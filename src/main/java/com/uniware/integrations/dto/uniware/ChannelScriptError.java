package com.uniware.integrations.dto.uniware;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChannelScriptError {
    public enum ScriptErrorCodes {
        INVALID_CREDENTIALS,
        CHANNEL_REFUSED_SYNC,
        CHANNEL_ERROR,
        SERVER_DOWN,
        DISABLED
    }

    private ScriptErrorCodes code;
    private String message;
}
