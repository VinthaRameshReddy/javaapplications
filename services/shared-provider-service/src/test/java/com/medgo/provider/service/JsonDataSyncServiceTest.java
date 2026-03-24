package com.medgo.provider.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medgo.provider.domain.entity.AffiliationView;
import com.medgo.provider.domain.entity.HealthFacilityEntity;
import com.medgo.provider.domain.response.ViewDoctorHospitalResponse;
import com.medgo.provider.mapper.MedGoClaimsMapper;
import com.medgo.provider.repository.AffiliationViewRepository;
import com.medgo.provider.repository.HealthFacilityRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JsonDataSyncServiceTest {

    private String originalUserDir;

    @BeforeEach
    void setUp() {
        originalUserDir = System.getProperty("user.dir");
    }

    @AfterEach
    void tearDown() {
        if (originalUserDir != null) {
            System.setProperty("user.dir", originalUserDir);
        }
    }

    @Test
    void syncJsonDataFromDatabase_writesFiles_whenEnabled() throws Exception {
        Path temp = Files.createTempDirectory("jds-test");
        System.setProperty("user.dir", temp.toString());

        HealthFacilityRepository hfRepo = Mockito.mock(HealthFacilityRepository.class);
        AffiliationViewRepository affRepo = Mockito.mock(AffiliationViewRepository.class);
        MedGoClaimsMapper mapper = Mockito.mock(MedGoClaimsMapper.class);
        ObjectMapper om = new ObjectMapper();

        HealthFacilityEntity h = new HealthFacilityEntity();
        h.setHfId(1L);
        when(hfRepo.findAll()).thenReturn(List.of(h));

        AffiliationView a = new AffiliationView();
        when(affRepo.findAll()).thenReturn(List.of(a));
        when(mapper.toViewDoctorHospitalResponse(any())).thenReturn(new ViewDoctorHospitalResponse());

        JsonDataSyncService svc = new JsonDataSyncService(hfRepo, affRepo, mapper, om);

        // enable flag via reflection
        var f = JsonDataSyncService.class.getDeclaredField("syncEnabled");
        f.setAccessible(true);
        f.set(svc, true);

        svc.syncJsonDataFromDatabase();

        File hospitals = temp.resolve("src/main/resources/static-data/hospitals.json").toFile();
        File doctors = temp.resolve("src/main/resources/static-data/doctors.json").toFile();

        assertTrue(hospitals.exists());
        assertTrue(doctors.exists());
    }
}























