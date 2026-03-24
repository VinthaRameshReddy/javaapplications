package com.medgo.member.utility;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verifyNoInteractions;

class DateTypeDeserializerTest {

    private final DateTypeDeserializer deserializer = new DateTypeDeserializer();

    private Date parse(String value) {
        JsonDeserializationContext ctx = Mockito.mock(JsonDeserializationContext.class); // unused but exercises Mockito
        JsonElement element = new JsonPrimitive(value);
        Date parsed = deserializer.deserialize(element, Date.class, ctx);
        // context is not used at all; ensure no interactions (branch coverage for doing nothing with ctx)
        verifyNoInteractions(ctx);
        return parsed;
    }

    @Test
    @DisplayName("Format 1: yyyy-MM-dd'T'HH:mm:ssZ")
    void testFormat1() {
        Date d = parse("2025-11-15T13:45:20+0000");
        assertNotNull(d);
    }

    @Test
    @DisplayName("Format 2: yyyy-MM-dd'T'HH:mm:ss (no timezone)")
    void testFormat2() {
        Date d = parse("2025-11-15T13:45:20");
        assertNotNull(d);
    }

    @Test
    @DisplayName("Format 3: yyyy-MM-dd (date only)")
    void testFormat3() {
        Date d = parse("2025-11-15");
        assertNotNull(d);
    }

    @Test
    @DisplayName("Format 4: EEE MMM dd HH:mm:ss z yyyy")
    void testFormat4() {
        Date d = parse("Sat Nov 15 13:45:20 GMT 2025");
        assertNotNull(d);
    }

    @Test
    @DisplayName("Format 5: HH:mm:ss (time only)")
    void testFormat5() {
        Date d = parse("13:45:20");
        assertNotNull(d);
    }

    @Test
    @DisplayName("Format 6: MM/dd/yyyy HH:mm:ss aaa")
    void testFormat6() {
        Date d = parse("11/15/2025 01:45:20 PM");
        assertNotNull(d);
    }

    @Test
    @DisplayName("Format 7: yyyy-MM-dd'T'HH:mm:ss.SSSSSS (6 fractional digits)")
    void testFormat7() {
        Date d = parse("2025-11-15T13:45:20.123456");
        assertNotNull(d);
    }

    @Test
    @DisplayName("Format 8: yyyy-MM-dd'T'HH:mm:ss.SSSSSSS (7 fractional digits)")
    void testFormat8() {
        Date d = parse("2025-11-15T13:45:20.1234567");
        assertNotNull(d);
    }

    @Test
    @DisplayName("Format 9: yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'")
    void testFormat9() {
        Date d = parse("2025-11-15T13:45:20.1234567Z");
        assertNotNull(d);
    }

    @Test
    @DisplayName("Format 10: MMM d',' yyyy H:mm:ss a")
    void testFormat10() {
        Date d = parse("Nov 15, 2025 1:45:20 PM");
        assertNotNull(d);
    }

    @Test
    @DisplayName("Unparseable date throws JsonParseException with supported formats list")
    void testUnparseable() {
        JsonDeserializationContext ctx = Mockito.mock(JsonDeserializationContext.class);
        JsonElement element = new JsonPrimitive("2025/15/11"); // invalid arrangement
        JsonParseException ex = assertThrows(JsonParseException.class, () -> deserializer.deserialize(element, Date.class, ctx));
        assertTrue(ex.getMessage().contains("Unparseable date"));
        assertTrue(ex.getMessage().contains("yyyy-MM-dd'T'HH:mm:ssZ")); // first format present in message
    }

    @Test
    @DisplayName("Late format match forces multiple prior ParseException failures (exercise loop catch path extensively)")
    void testLateFormatMatch() {
        // This string matches only the last format; earlier ones will all throw ParseException
        Date d = parse("Nov 15, 2025 2:30:59 PM");
        assertNotNull(d);
    }
}

