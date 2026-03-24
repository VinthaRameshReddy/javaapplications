package com.medgo.provider.controller;

import com.medgo.provider.service.JsonDataSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller to manually trigger JSON data sync from database
 */
@RestController
@RequestMapping("/admin/sync")
public class JsonSyncController {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonSyncController.class);
    
    private final JsonDataSyncService jsonDataSyncService;

    @Autowired
    public JsonSyncController(JsonDataSyncService jsonDataSyncService) {
        this.jsonDataSyncService = jsonDataSyncService;
    }

    /**
     * Manually trigger JSON data sync from database
     * This will fetch the latest data from the database and update the JSON files
     */
    @PostMapping("/trigger")
    public ResponseEntity<Map<String, Object>> triggerJsonSync() {
        LOGGER.info("Manual JSON sync triggered at {}", 
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        try {
            jsonDataSyncService.triggerManualSync();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "JSON data sync completed successfully");
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LOGGER.error("Error during manual JSON sync: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to sync JSON data: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}


