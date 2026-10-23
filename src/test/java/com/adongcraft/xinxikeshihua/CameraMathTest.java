package com.adongcraft.xinxikeshihua;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CameraMathTest {
    private static final double EPSILON = 1.0E-6D;

    @Test
    void facingSouthMovesAlongPositiveZ() {
        CameraMath.Point point = CameraMath.offset(0, 0, 0, 0.0F, 0.0F, 2.0D, 0.0D, 0.0D);
        assertEquals(0.0D, point.x(), EPSILON);
        assertEquals(0.0D, point.y(), EPSILON);
        assertEquals(2.0D, point.z(), EPSILON);
    }

    @Test
    void facingWestMovesAlongNegativeX() {
        CameraMath.Point point = CameraMath.offset(0, 0, 0, 90.0F, 0.0F, 2.0D, 0.0D, 0.0D);
        assertEquals(-2.0D, point.x(), EPSILON);
        assertEquals(0.0D, point.y(), EPSILON);
        assertEquals(0.0D, point.z(), EPSILON);
    }

    @Test
    void facingNorthMovesAlongNegativeZ() {
        CameraMath.Point point = CameraMath.offset(0, 0, 0, 180.0F, 0.0F, 1.0D, 0.0D, 0.0D);
        assertEquals(0.0D, point.x(), EPSILON);
        assertEquals(-1.0D, point.z(), EPSILON);
    }

    @Test
    void lookingUpMovesAlongPositiveY() {
        CameraMath.Point point = CameraMath.offset(0, 0, 0, 0.0F, -90.0F, 1.0D, 0.0D, 0.0D);
        assertEquals(0.0D, point.x(), EPSILON);
        assertEquals(1.0D, point.y(), EPSILON);
        assertEquals(0.0D, point.z(), EPSILON);
    }

    @Test
    void lookingDownMovesAlongNegativeY() {
        CameraMath.Point point = CameraMath.offset(0, 0, 0, 0.0F, 90.0F, 1.0D, 0.0D, 0.0D);
        assertEquals(0.0D, point.x(), EPSILON);
        assertEquals(-1.0D, point.y(), EPSILON);
        assertEquals(0.0D, point.z(), EPSILON);
    }

    @Test
    void rightOffsetGoesWestWhenFacingSouth() {
        CameraMath.Point point = CameraMath.offset(0, 0, 0, 0.0F, 0.0F, 0.0D, 2.0D, 0.0D);
        assertEquals(-2.0D, point.x(), EPSILON);
        assertEquals(0.0D, point.z(), EPSILON);
    }

    @Test
    void upOffsetIsVerticalWhenLevel() {
        CameraMath.Point point = CameraMath.offset(0, 0, 0, 0.0F, 0.0F, 0.0D, 0.0D, 3.0D);
        assertEquals(0.0D, point.x(), EPSILON);
        assertEquals(3.0D, point.y(), EPSILON);
        assertEquals(0.0D, point.z(), EPSILON);
    }

    @Test
    void eyePositionIsTheOriginOfTheOffset() {
        CameraMath.Point point = CameraMath.offset(10.0D, 64.0D, -5.0D, 0.0F, 0.0F, 1.0D, 0.0D, 0.0D);
        assertEquals(10.0D, point.x(), EPSILON);
        assertEquals(64.0D, point.y(), EPSILON);
        assertEquals(-4.0D, point.z(), EPSILON);
    }
}
