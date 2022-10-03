package com.uniware.integrations.dto.pincodeApi;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class PincodeApiResult {
    @SerializedName("Status")
    private String status;

    @SerializedName("Message")
    private String message;

    @SerializedName("PostOffice")
    private List<PostOffice> postOffices;

    @Data
    public static class PostOffice {
        @SerializedName("Circle")
        private String circle;

        @SerializedName("Description")
        private Object description;

        @SerializedName("BranchType")
        private String branchType;

        @SerializedName("State")
        private String state;

        @SerializedName("DeliveryStatus")
        private String deliveryStatus;

        @SerializedName("Region")
        private String region;

        @SerializedName("Block")
        private String block;

        @SerializedName("Country")
        private String country;

        @SerializedName("Division")
        private String division;

        @SerializedName("District")
        private String district;

        @SerializedName("Pincode")
        private String pincode;

        @SerializedName("Name")
        private String name;
    }
}