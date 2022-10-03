package com.uniware.integrations.filters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniware.integrations.contexts.ShopifyRequestContext;
import com.uniware.integrations.dto.ApiError;
import com.uniware.integrations.dto.ApiResponse;
import com.uniware.integrations.dto.HEADER_NAMES;
import com.uniware.integrations.utils.ShopifyUtils;
import io.micrometer.core.instrument.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component("ShopifyContextFilter")
@Order(1)
public class ShopifyContextFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShopifyContextFilter.class);
    @Autowired
    private final ObjectMapper objectMapper;
    private static final HEADER_NAMES[] requiredHeaders = {HEADER_NAMES.HOSTNAME, HEADER_NAMES.USERNAME, HEADER_NAMES.PASSWORD, HEADER_NAMES.TENANT_CODE, HEADER_NAMES.LOCATION_ID};

    public ShopifyContextFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
//            byte[] body = StreamUtils.copyToByteArray(request.getInputStream());

//            Map<String, Object> jsonRequest = objectMapper.readValue(body, Map.class);
//            LOGGER.info("Request: {}", jsonRequest);
            String hostname = request.getHeader(HEADER_NAMES.HOSTNAME.toString());
            String username = request.getHeader(HEADER_NAMES.USERNAME.toString());
            String password = request.getHeader(HEADER_NAMES.PASSWORD.toString());
            String tenantCode = request.getHeader(HEADER_NAMES.TENANT_CODE.toString());
            String locationId = request.getHeader(HEADER_NAMES.LOCATION_ID.toString());
            if (handleMissingHeaders(request, response)) return;
            ShopifyRequestContext.current().setHostname(hostname);
            ShopifyRequestContext.current().setTenantCode(tenantCode);
            ShopifyRequestContext.current().setLocationId(locationId);
            ShopifyRequestContext.current().getHeaders().put("Authorization", Collections.singletonList("Basic " + ShopifyUtils.getBasicToken(username, password)));

            LOGGER.info("ShopifyRequestContext data:{}", ShopifyRequestContext.current());

            filterChain.doFilter(request, response);
        } finally {
            ShopifyRequestContext.destroy();
        }

    }

    private boolean handleMissingHeaders(HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<ApiError> apiErrors = new ArrayList<>();
        for (HEADER_NAMES requiredHeaderName : requiredHeaders) {
            if (StringUtils.isBlank(request.getHeader(requiredHeaderName.toString())))
                apiErrors.add(new ApiError(requiredHeaderName.toString(),"is a required header"));
        }
        if (apiErrors.isEmpty()) return false;

        ApiResponse errorResponse = ApiResponse.failure().message("Required headers are missing").errors(apiErrors).build();
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        return true;
    }
}