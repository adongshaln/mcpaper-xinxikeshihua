package com.adongcraft.xinxikeshihua;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonTest {
    @Test
    void quotesAreEscaped() {
        assertEquals("\"a\\\"b\"", Json.quote("a\"b"));
        assertEquals("\"a\\\\b\"", Json.quote("a\\b"));
        assertEquals("\"a\\nb\"", Json.quote("a\nb"));
        assertEquals("null", Json.quote(null));
    }

    @Test
    void invalidNumbersBecomeZero() {
        assertEquals("0", Json.number(Double.NaN));
        assertEquals("0", Json.number(Double.POSITIVE_INFINITY));
        assertEquals("2.5", Json.number(2.5D));
        assertEquals("7", Json.number(7L));
    }
}
