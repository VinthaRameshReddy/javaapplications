package com.medgo.member.domain.entity.membership;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DemoTest {



    @Test
    void testDemoClassInstantiation() {
        Demo demo = new Demo();
        assertNotNull(demo, "Demo instance should not be null");
    }

    @Test
    void testDemoClassExists() {
        // Verify class can be loaded
        assertDoesNotThrow(() -> {
            Class<?> clazz = Class.forName("com.medgo.member.domain.entity.membership.Demo");
            assertNotNull(clazz);
            assertEquals("Demo", clazz.getSimpleName());
        });
    }

    @Test
    void testMultipleInstances() {
        Demo demo1 = new Demo();
        Demo demo2 = new Demo();
        assertNotNull(demo1);
        assertNotNull(demo2);
        assertNotSame(demo1, demo2, "Each instance should be unique");
    }
}

