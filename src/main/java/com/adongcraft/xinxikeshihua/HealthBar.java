package com.adongcraft.xinxikeshihua;

/**
 * 血条只负责把“当前血量 / 刚刚的血量 / 最大血量”换算成三段：
 * 实心是当前血量，橙色是一小段刚掉的血，空心的剩余部分。
 */
public final class HealthBar {
    public static final int SEGMENTS = 20;

    public record Segments(int filled, int trailing, int empty) {
    }

    private HealthBar() {
    }

    public static Segments segments(double health, double shownHealth, double maxHealth, int count) {
        int segments = Math.max(1, count);
        if (!Double.isFinite(maxHealth) || maxHealth <= 0.0D) {
            return new Segments(0, 0, segments);
        }
        double healthValue = clamp(health, 0.0D, maxHealth);
        double shownValue = clamp(Math.max(shownHealth, healthValue), 0.0D, maxHealth);
        int filled = clampInt((int) Math.round(healthValue / maxHealth * segments), 0, segments);
        int shown = clampInt((int) Math.round(shownValue / maxHealth * segments), filled, segments);
        return new Segments(filled, shown - filled, segments - shown);
    }

    private static double clamp(double value, double min, double max) {
        if (!Double.isFinite(value)) {
            return min;
        }
        return Math.max(min, Math.min(max, value));
    }

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
