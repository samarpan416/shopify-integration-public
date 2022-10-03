package com.uniware.integrations.utils;

import com.unifier.core.utils.StringUtils;
import com.uniware.integrations.clients.PincodeDetailsApiClient;
import com.uniware.integrations.dto.pincodeApi.PincodeApiResult;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.slf4j.Slf4jLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;

public class ZipCodeUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ZipCodeUtils.class);
    private static Properties zipCodeProperties = null;
    private static final PincodeDetailsApiClient pincodeDetailsApiClient = getPincodeApiClient();

    private static PincodeDetailsApiClient getPincodeApiClient() {
        return Feign.builder()
                .decoder(new GsonDecoder())
                .logLevel(feign.Logger.Level.FULL)
                .logger(new Slf4jLogger(PincodeDetailsApiClient.class))
                .target(PincodeDetailsApiClient.class, "https://api.postalpincode.in");
    }

    static {
        try {
            zipCodeProperties = new Properties();
            zipCodeProperties.load(new InputStreamReader(new ClassPathResource("zipCode/zipCode.properties", ZipCodeUtils.class.getClassLoader()).getInputStream()));
        } catch (IOException e) {
            LOG.error("Error loading zipcodes", e);
            e.printStackTrace();
        }
    }

    public static String getStateCode(String zipcode) {
        String stateCode = null;
        stateCode = getStateCodeByZipPrefix(zipcode);
        if (StringUtils.isNotBlank(stateCode)) return stateCode;
        stateCode = getStateCodeByZipCode(zipcode);
        if (StringUtils.isNotBlank(stateCode)) return stateCode;
        stateCode = getStateCodeFromApi(zipcode);
        return stateCode;
    }

    private static String getStateCodeFromApi(String zipCode) {
        List<PincodeApiResult> results = pincodeDetailsApiClient.getPincodeDetails(zipCode);
        boolean isValidResponse = validatePincodeApiResults(zipCode, results);
        return isValidResponse ? results.get(0).getPostOffices().get(0).getState() : "";
    }

    private static boolean validatePincodeApiResults(String zipCode, List<PincodeApiResult> results) {
        if (results.isEmpty() || results.get(0).getStatus().equals("Error")) {
            LOG.error("Details not available on india post API for zipCode: {} | Error message: {}", zipCode, results.get(0).getMessage());
            return false;
        }
        return true;
    }

    public static String getStateCodeByZipPrefix(String zipCode) {
        String stateCode = null;
        int threeDigitPrefix = Integer.parseInt(zipCode.substring(0, 3));
        int twoDigitPrefix = Integer.parseInt(zipCode.substring(0, 2));

        //Daman and Diu
        if (zipCode.startsWith("396210")) stateCode = "DD";

            //Check zipcode on the bases of starting three digit
            //Dadra and Nagar Haveli
        else if (threeDigitPrefix == 396) stateCode = "DN";

            //Goa
        else if (threeDigitPrefix == 403) stateCode = "GA";

            //Puducherry
        else if (threeDigitPrefix == 605) stateCode = "PY";

            //Lakshadweep
        else if (threeDigitPrefix == 682) stateCode = "LD";

            //Sikkim
        else if (threeDigitPrefix == 737) stateCode = "SK";

            //Andaman and Nicobar Islands
        else if (threeDigitPrefix == 744) stateCode = "AN";

            //Manipur
        else if (threeDigitPrefix == 795) stateCode = "MN";

            //Mizoram
        else if (threeDigitPrefix == 796) stateCode = "MZ";

            //Tripura
        else if (threeDigitPrefix == 799) stateCode = "TR";

            //Arunachal Pradesh
        else if (threeDigitPrefix >= 790 && threeDigitPrefix <= 792) stateCode = "AR";

            //Meghalaya
        else if (threeDigitPrefix >= 793 && threeDigitPrefix <= 794) stateCode = "ML";

            //Nagaland
        else if (threeDigitPrefix >= 797 && threeDigitPrefix <= 798) stateCode = "NL";

            //Check zipcode on the bases of starting two digit
            //Delhi
        else if (twoDigitPrefix == 11) stateCode = "DL";

            //Chandigarh
        else if (twoDigitPrefix == 16) stateCode = "CH";

            //Himachal Pradesh
        else if (twoDigitPrefix == 17) stateCode = "HP";

            //Chhattisgarh
        else if (twoDigitPrefix == 49) stateCode = "CT";

            //Telangana
        else if (twoDigitPrefix == 50) stateCode = "TG";

            //Assam
        else if (twoDigitPrefix == 78) stateCode = "AS";

            //Haryana
        else if (twoDigitPrefix >= 12 && twoDigitPrefix <= 13) stateCode = "HR";

            //Punjab
        else if (twoDigitPrefix >= 14 && twoDigitPrefix <= 15) stateCode = "PB";

            //Rajasthan
        else if (twoDigitPrefix >= 30 && twoDigitPrefix <= 34) stateCode = "RJ";

            //Gujarat
        else if (twoDigitPrefix >= 36 && twoDigitPrefix <= 39) stateCode = "GJ";

            //Maharashtra
        else if (twoDigitPrefix >= 40 && twoDigitPrefix <= 44) stateCode = "MH";

            //Madhya Pradesh
        else if (twoDigitPrefix >= 45 && twoDigitPrefix <= 48) stateCode = "MP";

            //Andhra Pradesh
        else if (twoDigitPrefix >= 51 && twoDigitPrefix <= 53) stateCode = "AP";

            //Karnataka
        else if (twoDigitPrefix >= 56 && twoDigitPrefix <= 59) stateCode = "KA";

            //Tamil Nadu
        else if (twoDigitPrefix >= 60 && twoDigitPrefix <= 66) stateCode = "TN";

            //Kerala
        else if (twoDigitPrefix >= 67 && twoDigitPrefix <= 69) stateCode = "KL";

            //West Bengal
        else if (twoDigitPrefix >= 70 && twoDigitPrefix <= 74) stateCode = "WB";

            //Odisha
        else if (twoDigitPrefix >= 75 && twoDigitPrefix <= 77) stateCode = "OR";

        return stateCode;
    }

    public static String getStateCodeByZipCode(String zipCode) {
        return (String) zipCodeProperties.get(zipCode);
    }
}
