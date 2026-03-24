package com.medgo.provider.controller;

import com.medgo.provider.service.JsonDataSyncService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JsonSyncControllerTest {

    @Test
    void triggerJsonSync_success() {
        JsonDataSyncService svc = Mockito.mock(JsonDataSyncService.class);
        JsonSyncController controller = new JsonSyncController(svc);
        ResponseEntity<Map<String, Object>> resp = controller.triggerJsonSync();
        assertEquals(200, resp.getStatusCode().value());
        assertEquals("success", resp.getBody().get("status"));
        verify(svc, times(1)).triggerManualSync();
    }

    @Test
    void triggerJsonSync_error() {
        JsonDataSyncService svc = Mockito.mock(JsonDataSyncService.class);
        doThrow(new RuntimeException("boom")).when(svc).triggerManualSync();
        JsonSyncController controller = new JsonSyncController(svc);
        ResponseEntity<Map<String, Object>> resp = controller.triggerJsonSync();
        assertEquals(500, resp.getStatusCode().value());
        assertEquals("error", resp.getBody().get("status"));
    }
}























