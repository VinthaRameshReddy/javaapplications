package com.medgo.provider.controller;

import com.medgo.commons.CommonResponse;
import com.medgo.provider.domain.request.HospitalRequest;
import com.medgo.provider.domain.request.ViewDoctorHospitalRequest;
import com.medgo.provider.service.HealthFacilityService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HealthFacilityControllerTest {

    @Test
    void getViewDoctorHospitalV2_delegatesToService() {
        HealthFacilityService svc = Mockito.mock(HealthFacilityService.class);
        when(svc.getViewDoctorHospitalV2(anyInt(), anyInt(), any())).thenReturn(CommonResponse.success("ok"));
        HealthFacilityController controller = new HealthFacilityController(svc);
        CommonResponse resp = controller.getViewDoctorHospitalV2(0, 5, new ViewDoctorHospitalRequest());
        assertTrue(resp.isSuccess());
        verify(svc).getViewDoctorHospitalV2(eq(0), eq(5), any());
    }

    @Test
    void getHospitalsList_delegatesToService() {
        HealthFacilityService svc = Mockito.mock(HealthFacilityService.class);
        when(svc.getHospitalsList(anyInt(), anyInt(), any(), any())).thenReturn(CommonResponse.success("ok"));
        HealthFacilityController controller = new HealthFacilityController(svc);
        CommonResponse resp = controller.getHospitalsList(0, 10, null, new HospitalRequest());
        assertTrue(resp.isSuccess());
        verify(svc).getHospitalsList(eq(0), eq(10), isNull(), any());
    }
}

