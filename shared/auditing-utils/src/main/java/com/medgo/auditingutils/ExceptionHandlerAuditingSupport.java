package com.medgo.auditingutils;

import com.fasterxml.jackson.databind.ObjectMapper;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.MethodArgumentNotValidException;

public abstract class ExceptionHandlerAuditingSupport {

    @Autowired
    ObjectMapper objectMapper;

    @Value("${spring.application.name:SmartNow}")
    String serviceName;

    public void handleMethodArgumentNotValidException(Object requestBody, MethodArgumentNotValidException mae){
      /*  Event.EventBuilder eventBuilder = Event.builder();
        AuditData.AuditDataBuilder auditDataBuilder = AuditData.builder();
        eventBuilder.type("com.auditing.http_request")
                .source(serviceName);
        String requestUri = MDC.get("requestUri");
        String clientIpAddress = MDC.get("clientIpAddress");
        String requestId = MDC.get("correlationId");
        CustomerData customer = getCustomerData();
        auditDataBuilder.build().setCustomer(customer);
        String errorMessage = mae.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + " " + fieldError.getDefaultMessage())
                .collect(Collectors.joining());
        auditDataBuilder.build().setRequest(new HttpRequestData(requestId, clientIpAddress, requestUri, requestBody,
                null, errorMessage));
        eventBuilder.data(auditDataBuilder.build());
        writeEvent(eventBuilder.build());*/
    }

 /*   CustomerData getCustomerData() {
        String customerId = MDC.get("customerId");
        String deviceId = MDC.get("deviceId");
        String device = MDC.get("device");
        String mobileNo = MDC.get("mobileNo");
        String persona = MDC.get("persona");
        return CustomerData.builder()
                .customerId(customerId)
                .device(device)
                .deviceId(deviceId)
                .mobileNo(mobileNo)
                .persona(persona)
                .build();
    }

    void writeEvent(Event event) {
        String customerId;
        customerId = "";
        this.eventPublisher.publishAsync(KafkaTopic.AUDIT_EVENTS, customerId, event);
    }*/
}
