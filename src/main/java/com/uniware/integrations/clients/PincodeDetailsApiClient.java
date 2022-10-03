package com.uniware.integrations.clients;

import com.uniware.integrations.dto.pincodeApi.PincodeApiResult;
import feign.Param;
import feign.RequestLine;
import org.springframework.cloud.openfeign.FeignClient;
import java.util.List;

@FeignClient(name = "pincode")
public interface PincodeDetailsApiClient {
    @RequestLine("GET /pincode/{pincode}")
    List<PincodeApiResult> getPincodeDetails(@Param("pincode") String pincode);
}

