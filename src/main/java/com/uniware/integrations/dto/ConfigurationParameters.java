package com.uniware.integrations.dto;

import com.unifier.core.utils.StringUtils;
import lombok.Data;
import org.codehaus.jackson.annotate.JsonProperty;

@Data
public class ConfigurationParameters {
    private String applicableProvinceCodes;
    private boolean stateCodeRequired = true;
    @JsonProperty("enablePOS")
    private boolean posEnabled = false;
    private String customFieldsCustomization = "[]";

    public String getCustomFieldsCustomization() {
        return StringUtils.isEmpty(customFieldsCustomization) ? "[]" : customFieldsCustomization;
    }
}
