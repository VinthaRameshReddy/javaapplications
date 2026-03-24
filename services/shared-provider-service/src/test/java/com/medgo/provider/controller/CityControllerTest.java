package com.medgo.provider.controller;

import com.medgo.commons.CommonResponse;
import com.medgo.provider.domain.request.CityRequest;
import com.medgo.provider.service.CityService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CityControllerTest {

    @Test
    void listCities_delegatesToService() {
        CityService svc = Mockito.mock(CityService.class);
        when(svc.getCities(anyInt(), anyInt(), any(), any())).thenReturn(CommonResponse.success("ok"));
        CityController controller = new CityController(svc);
        CommonResponse resp = controller.listCities(0, 10, null, new CityRequest());
        assertTrue(resp.isSuccess());
        verify(svc).getCities(eq(0), eq(10), isNull(), any());
    }
}

