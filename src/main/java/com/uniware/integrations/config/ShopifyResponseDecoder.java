package com.uniware.integrations.config;

import feign.FeignException;
import feign.Request;
import feign.Response;
import feign.codec.DecodeException;
import feign.gson.GsonDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;


public class ShopifyResponseDecoder extends GsonDecoder {
    private static final Logger LOG = LoggerFactory.getLogger(ShopifyResponseDecoder.class);
    @Override
    public Object decode(Response response, Type type) throws IOException, DecodeException, FeignException {
        LOG.info(type.getTypeName());
        Request request=response.request();
        return super.decode(response,type);
    }
}
