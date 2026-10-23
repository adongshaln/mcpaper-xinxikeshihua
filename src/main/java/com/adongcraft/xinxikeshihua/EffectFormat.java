package com.adongcraft.xinxikeshihua;

import java.util.Locale;

/** 状态效果剩余时间和等级的文字。 */
public final class EffectFormat {
    private static final String[] ROMAN = {
            "", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"
    };

    private EffectFormat() {
    }

    public static String duration(int ticks) {
        if (ticks < 0) {
            return "∞";
        }
        int seconds = (int) Math.ceil(ticks / 20.0D);
        if (seconds >= 60) {
            return String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60);
        }
        return seconds + "s";
    }

    public static String seconds(int ticks) {
        return Math.max(0, (int) Math.ceil(ticks / 20.0D)) + "s";
    }

    public static String roman(int value) {
        if (value >= 1 && value < ROMAN.length) {
            return ROMAN[value];
        }
        return Integer.toString(value);
    }

    public static String level(int amplifier) {
        return roman(amplifier + 1);
    }
}
