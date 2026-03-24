package com.medgo.provider.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medgo.provider.domain.entity.AffiliationView;
import com.medgo.provider.domain.entity.HealthFacilityEntity;
import com.medgo.provider.domain.response.ViewDoctorHospitalResponse;
import com.medgo.provider.mapper.MedGoClaimsMapper;
import com.medgo.provider.repository.AffiliationViewRepository;
import com.medgo.provider.repository.HealthFacilityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service to sync JSON data from database daily at 12 AM
 */
@Service
public class JsonDataSyncService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonDataSyncService.class);
    
    private final HealthFacilityRepository healthFacilityRepository;
    private final AffiliationViewRepository affiliationViewRepository;
    private final MedGoClaimsMapper mapper;
    private final ObjectMapper objectMapper;
    
    @Value("${json.data.sync.enabled:true}")
    private boolean syncEnabled;

    public JsonDataSyncService(
            HealthFacilityRepository healthFacilityRepository,
            AffiliationViewRepository affiliationViewRepository,
            MedGoClaimsMapper mapper,
            ObjectMapper objectMapper) {
        this.healthFacilityRepository = healthFacilityRepository;
        this.affiliationViewRepository = affiliationViewRepository;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    /**
     * Scheduled task to sync data from database to JSON files
     * Runs daily at 12:00 AM (midnight)
     */
    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Manila")
    public void syncJsonDataFromDatabase() {
        if (!syncEnabled) {
            LOGGER.info("JSON data sync is disabled");
            return;
        }
        
        LOGGER.info("Starting scheduled JSON data sync at {}", 
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        try {
            // Sync hospitals data
            syncHospitalsData();
            
            // Sync doctors data
            syncDoctorsData();
            
            LOGGER.info("JSON data sync completed successfully at {}", 
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        } catch (Exception e) {
            LOGGER.error("Error during JSON data sync: {}", e.getMessage(), e);
        }
    }

    /**
     * Sync hospitals data from database to hospitals.json
     */
    private void syncHospitalsData() {
        LOGGER.info("Fetching all hospitals from database...");
        List<HealthFacilityEntity> hospitals = healthFacilityRepository.findAll();
        
        if (hospitals.isEmpty()) {
            LOGGER.warn("No hospitals found in database");
            return;
        }
        
        LOGGER.info("Found {} hospitals in database", hospitals.size());
        
        try {
            // Get the path to the resources/static-data directory
            String resourcesPath = getResourcesPath();
            File hospitalsFile = new File(resourcesPath, "hospitals.json");
            
            // Write to JSON file
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(hospitalsFile, hospitals);
            
            LOGGER.info("Successfully updated hospitals.json with {} hospitals at {}", 
                hospitals.size(), hospitalsFile.getAbsolutePath());
        } catch (Exception e) {
            LOGGER.error("Error writing hospitals.json: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update hospitals.json", e);
        }
    }

    /**
     * Sync doctors data from database to doctors.json
     */
    private void syncDoctorsData() {
        LOGGER.info("Fetching all doctor-hospital affiliations from database...");
        List<AffiliationView> affiliations = affiliationViewRepository.findAll();
        
        if (affiliations.isEmpty()) {
            LOGGER.warn("No doctor-hospital affiliations found in database");
            return;
        }
        
        LOGGER.info("Found {} doctor-hospital affiliations in database", affiliations.size());
        
        try {
            // Convert AffiliationView to ViewDoctorHospitalResponse
            List<ViewDoctorHospitalResponse> doctors = affiliations.stream()
                    .map(mapper::toViewDoctorHospitalResponse)
                    .collect(Collectors.toList());
            
            // Get the path to the resources/static-data directory
            String resourcesPath = getResourcesPath();
            File doctorsFile = new File(resourcesPath, "doctors.json");
            
            // Write to JSON file
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(doctorsFile, doctors);
            
            LOGGER.info("Successfully updated doctors.json with {} doctor records at {}", 
                doctors.size(), doctorsFile.getAbsolutePath());
        } catch (Exception e) {
            LOGGER.error("Error writing doctors.json: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update doctors.json", e);
        }
    }

    /**
     * Get the resources path where JSON files are stored
     */
    private String getResourcesPath() {
        // Try to get the resources directory
        String currentDir = System.getProperty("user.dir");
        
        // For development: src/main/resources/static-data
        String devPath = Paths.get(currentDir, "src", "main", "resources", "static-data").toString();
        File devDir = new File(devPath);
        
        if (devDir.exists()) {
            return devPath;
        }
        
        // For production: target/classes/static-data
        String targetPath = Paths.get(currentDir, "target", "classes", "static-data").toString();
        File targetDir = new File(targetPath);
        
        if (targetDir.exists()) {
            return targetPath;
        }
        
        // For production: external static-data directory
        String prodPath = Paths.get(currentDir, "static-data").toString();
        File prodDir = new File(prodPath);
        
        if (prodDir.exists()) {
            return prodPath;
        }
        
        // Create directory if it doesn't exist
        File staticDataDir = new File(devPath);
        if (!staticDataDir.exists()) {
            boolean created = staticDataDir.mkdirs();
            if (!created) {
                LOGGER.warn("Failed to create directory: {}", devPath);
            }
        }
        
        return devPath;
    }

    /**
     * Manual trigger method to sync data immediately (can be called via API if needed)
     */
    public void triggerManualSync() {
        LOGGER.info("Manual sync triggered");
        syncJsonDataFromDatabase();
    }
}

