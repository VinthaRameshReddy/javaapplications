package com.medgo.provider.domain.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CityEntityAccessorsTest {

    @Test
    void gettersSetters_cover() {
        CityEntity c = new CityEntity();
        c.setId(1L);
        c.setCode("C");
        c.setDescription("Desc");
        c.setProvinceId(10L);
        c.setDeleted("N");
        c.setCreatedDate(LocalDateTime.now());
        c.setUpdatedDate(LocalDateTime.now());
        c.setCreatedBy("u1");
        c.setUpdatedBy("u2");

        assertEquals(1L, c.getId());
        assertEquals("C", c.getCode());
        assertEquals("Desc", c.getDescription());
        assertEquals(10L, c.getProvinceId());
        assertEquals("N", c.getDeleted());
        assertNotNull(c.getCreatedDate());
        assertNotNull(c.getUpdatedDate());
        assertEquals("u1", c.getCreatedBy());
        assertEquals("u2", c.getUpdatedBy());
    }
}























