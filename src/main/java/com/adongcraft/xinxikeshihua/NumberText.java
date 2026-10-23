package com.adongcraft.xinxikeshihua;

import java.util.Locale;

/** 数字尽量短：整数不带小数点，非整数保留一位。 */
public final class NumberText {
    private NumberText() {
    }

    public static String format(double value) {
        if (!Double.isFinite(value)) {
            return "0";
        }
        if (Math.abs(value - Math.rint(value)) < 0.05D) {
            return Long.toString(Math.round(value));
        }
        return String.format(Locale.ROOT, "%.1f", value);
    }
}
