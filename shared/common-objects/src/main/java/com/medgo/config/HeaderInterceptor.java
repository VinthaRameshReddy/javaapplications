package com.medgo.config;

import com.medgo.commons.RequestContext;
import com.medgo.exception.ImportantHeadersMissingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class HeaderInterceptor implements HandlerInterceptor {

    private static final String TRACE_HEADER = "X-APPLICATION-ID";
    private static final String DEVICE_HEADER = "X-DEVICE-ID";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        String deviceHeader = request.getHeader(DEVICE_HEADER);
        if (!StringUtils.hasText(deviceHeader)) {
            throw new ImportantHeadersMissingException("Missing mandatory header: " + DEVICE_HEADER);
        }
        RequestContext.setDeviceId(deviceHeader);

        String traceHeader = request.getHeader(TRACE_HEADER);
        if (!StringUtils.hasText(traceHeader)) {
            throw new ImportantHeadersMissingException("Missing mandatory header: " + TRACE_HEADER);
        }
        RequestContext.setTraceId(traceHeader);
        return true; // continue request
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // cleanup to avoid memory leaks
        RequestContext.clear();
    }
}
