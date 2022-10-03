package com.uniware.integrations.dto.uniware;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
// https://www.baeldung.com/jackson-ignore-null-fields
public class WsChannelItemType {
    @NotBlank
    private String                           channelCode;

    @NotBlank
    private String                           channelProductId;

    private String                           sellerSkuCode;

    private String                           skuCode;

    private String                           productName;

    private String                           productUrl;

    private String                           size;

    private String                           color;

    private String                           brand;

    private Set<String>                      imageUrls;

    private String                           productDescription;

    private String                           vertical;

    private Integer                          blockedInventory;

    private int                              pendency;

    private boolean                          live = true;

    private boolean                          verified;

    private Boolean                          disabled;

    private BigDecimal                       commissionPercentage;

    private BigDecimal                       paymentGatewayCharge;

    private BigDecimal                       logisticsCost;

    private Integer                          currentInventoryOnChannel;

    private BigDecimal                       transferPrice;

    private String                           shippingPackageTypeCode;

    @Valid
    private List<Attribute>                  attributes;
}
