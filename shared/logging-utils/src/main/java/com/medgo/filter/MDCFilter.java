package com.medgo.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // run as early as possible
public class MDCFilter extends OncePerRequestFilter {

    public static final String MDC_CLIENT_IP_ADDRESS = "clientIpAddress";
    public static final String MDC_REQUEST_URI = "requestUri";
    public static final String MDC_CLIENT_CUSTOMER_ID = "customerId";
    public static final String MDC_CLIENT_DEVICE_ID = "deviceId";
    public static final String MDC_CLIENT_DEVICE = "device";
    public static final String MDC_CLIENT_MOBILE_NUMBER = "mobileNo";
    public static final String MDC_X_CORRELATION_ID = "correlationId";
    public static final String MDC_X_CUSTOMER_PERSONA = "persona";
    public static final String MDC_X_CLIENT_APP_ID = "appId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        MDC.put(MDC_CLIENT_IP_ADDRESS, getIpAddress(request));
        MDC.put(MDC_REQUEST_URI, request.getRequestURI());
        logger.info("MDC FILTER Header deviceId : " + request.getHeader("X-DEVICE-ID"));

        MDC.put(MDC_CLIENT_CUSTOMER_ID, "");
        MDC.put(MDC_CLIENT_DEVICE_ID, request.getHeader("X-DEVICE-ID"));
        MDC.put(MDC_CLIENT_DEVICE, "");
        MDC.put(MDC_CLIENT_MOBILE_NUMBER, "");
        MDC.put(MDC_X_CORRELATION_ID, getCorrelationId(request));
        MDC.put(MDC_X_CUSTOMER_PERSONA, "");
        MDC.put(MDC_X_CLIENT_APP_ID, "");
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_CLIENT_IP_ADDRESS);
            MDC.remove(MDC_REQUEST_URI);
            MDC.remove(MDC_CLIENT_CUSTOMER_ID);
            MDC.remove(MDC_CLIENT_DEVICE_ID);
            MDC.remove(MDC_CLIENT_MOBILE_NUMBER);
            MDC.remove(MDC_X_CORRELATION_ID);
            MDC.remove(MDC_X_CUSTOMER_PERSONA);
            MDC.remove(MDC_X_CLIENT_APP_ID);
        }
    }

    private String getCorrelationId(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("X-Correlation-ID"))
                .orElse(UUID.randomUUID().toString().replace("-", ""));
    }

    private String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress != null
                && !ipAddress.isBlank()
                && ipAddress.split(",").length > 0) {
            String[] ips = ipAddress.split(",");
            return ips[0].trim();
        }
        return Optional.ofNullable(ipAddress)
                .filter(ip -> !ip.isBlank())
                .orElse(request.getRemoteAddr());
    }
}


