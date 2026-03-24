package com.medgo.provider.entity;

import com.medgo.provider.domain.entity.HealthFacilityEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HealthFacilityEntityTest {

    @Test
    void loadContactDetailList_handlesValidAndInvalidJson() {
        HealthFacilityEntity e = new HealthFacilityEntity();
        e.setContactDetails("[{}]");
        e.loadContactDetailList();
        assertNotNull(e.getContactDetailList());
        assertEquals(1, e.getContactDetailList().size());

        e.setContactDetails("not-json");
        e.loadContactDetailList();
        assertNotNull(e.getContactDetailList());
        assertEquals(0, e.getContactDetailList().size());
    }
}























