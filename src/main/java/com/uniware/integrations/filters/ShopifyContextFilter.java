package com.uniware.integrations.filters;

import com.uniware.integrations.contexts.ShopifyRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Order(2)
public class ShopifyContextFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShopifyContextFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try {
            String hostname = request.getHeader("hostname");
            String authorization = request.getHeader("Authorization");

            ShopifyRequestContext.current().setHostname(hostname);
            ShopifyRequestContext.current().getHeaders().put("Authorization",authorization);

            LOGGER.info("ShopifyRequestContext data:{}", ShopifyRequestContext.current());

            filterChain.doFilter(request, response);
        } finally {
            ShopifyRequestContext.destroy();
        }

    }
}