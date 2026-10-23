package com.adongcraft.xinxikeshihua;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HealthBarTest {
    @Test
    void fullHealthHasOnlyFilledSegments() {
        HealthBar.Segments segments = HealthBar.segments(20.0D, 20.0D, 20.0D, 20);
        assertEquals(20, segments.filled());
        assertEquals(0, segments.trailing());
        assertEquals(0, segments.empty());
    }

    @Test
    void halfHealthHasTenEmptySegments() {
        HealthBar.Segments segments = HealthBar.segments(10.0D, 10.0D, 20.0D, 20);
        assertEquals(10, segments.filled());
        assertEquals(0, segments.trailing());
        assertEquals(10, segments.empty());
    }

    @Test
    void recentDamageLeavesATrailingBar() {
        HealthBar.Segments segments = HealthBar.segments(10.0D, 20.0D, 20.0D, 20);
        assertEquals(10, segments.filled());
        assertEquals(10, segments.trailing());
        assertEquals(0, segments.empty());
    }

    @Test
    void trailingBarShrinksWithShownHealth() {
        HealthBar.Segments segments = HealthBar.segments(5.0D, 10.0D, 20.0D, 20);
        assertEquals(5, segments.filled());
        assertEquals(5, segments.trailing());
        assertEquals(10, segments.empty());
    }

    @Test
    void zeroHealthIsAllEmpty() {
        HealthBar.Segments segments = HealthBar.segments(0.0D, 0.0D, 20.0D, 20);
        assertEquals(0, segments.filled());
        assertEquals(0, segments.trailing());
        assertEquals(20, segments.empty());
    }

    @Test
    void valuesAreClampedToTheBar() {
        HealthBar.Segments over = HealthBar.segments(25.0D, 25.0D, 20.0D, 20);
        assertEquals(20, over.filled());
        assertEquals(0, over.empty());

        HealthBar.Segments under = HealthBar.segments(-5.0D, -5.0D, 20.0D, 20);
        assertEquals(0, under.filled());
        assertEquals(20, under.empty());
    }

    @Test
    void invalidMaxHealthDoesNotDivideByZero() {
        HealthBar.Segments segments = HealthBar.segments(10.0D, 10.0D, 0.0D, 20);
        assertEquals(0, segments.filled());
        assertEquals(20, segments.empty());
    }

    @Test
    void numberTextKeepsIntegersShort() {
        assertEquals("7", NumberText.format(7.0D));
        assertEquals("7", NumberText.format(7.04D));
        assertEquals("7.5", NumberText.format(7.5D));
        assertEquals("0", NumberText.format(Double.NaN));
    }
}
