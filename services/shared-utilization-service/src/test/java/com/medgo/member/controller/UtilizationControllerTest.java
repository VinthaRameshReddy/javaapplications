package com.medgo.member.controller;

import com.medgo.commons.CommonResponse;
import com.medgo.member.domain.request.UtilizationRequest;
import com.medgo.member.service.Utilization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UtilizationControllerTest {




    @Mock
    private Utilization utilization;

    @InjectMocks
    private UtilizationController utilizationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUtilizationPdf() {
        UtilizationRequest request = new UtilizationRequest();
        CommonResponse expectedResponse = new CommonResponse();
        when(utilization.getUtilizationPdf(request)).thenReturn(expectedResponse);

        CommonResponse actualResponse = utilizationController.getUtilizationPdf(request);

        assertEquals(expectedResponse, actualResponse);
        verify(utilization, times(1)).getUtilizationPdf(request);
    }
}

