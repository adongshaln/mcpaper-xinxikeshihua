package com.adongcraft.xinxikeshihua;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EffectFormatTest {
    @Test
    void durationsUseCeilingSeconds() {
        assertEquals("0s", EffectFormat.duration(0));
        assertEquals("1s", EffectFormat.duration(1));
        assertEquals("1s", EffectFormat.duration(20));
        assertEquals("2s", EffectFormat.duration(21));
        assertEquals("∞", EffectFormat.duration(-1));
    }

    @Test
    void longDurationsUseMinutes() {
        assertEquals("1:00", EffectFormat.duration(1200));
        assertEquals("2:05", EffectFormat.duration(2500));
    }

    @Test
    void romanNumeralsStopAtTen() {
        assertEquals("0", EffectFormat.roman(0));
        assertEquals("I", EffectFormat.roman(1));
        assertEquals("IV", EffectFormat.roman(4));
        assertEquals("V", EffectFormat.roman(5));
        assertEquals("IX", EffectFormat.roman(9));
        assertEquals("X", EffectFormat.roman(10));
        assertEquals("11", EffectFormat.roman(11));
    }

    @Test
    void levelStartsAtOne() {
        assertEquals("I", EffectFormat.level(0));
        assertEquals("II", EffectFormat.level(1));
        assertEquals("III", EffectFormat.level(2));
    }
}
