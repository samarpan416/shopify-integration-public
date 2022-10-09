package com.uniware.integrations.dto;

import com.unifier.core.utils.StringUtils;
import com.uniware.integrations.services.saleorder.impl.BaseSaleOrderService.SPLIT_SHIPMENT_CONDITION;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonProperty;

@Data
public class ConfigurationParameters {
    private String applicableProvinceCodes;
    private boolean stateCodeRequired = true;
    @JsonProperty("enablePOS")
    private boolean posEnabled = false;
    private String customFieldsCustomization = "[]";
    private SPLIT_SHIPMENT_CONDITION splitShipmentCondition;

    public String getCustomFieldsCustomization() {
        return StringUtils.isEmpty(customFieldsCustomization) ? "[]" : customFieldsCustomization;
    }
}
