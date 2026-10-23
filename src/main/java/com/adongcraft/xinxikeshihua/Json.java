package com.adongcraft.xinxikeshihua;

import java.util.Locale;

/** 极小的 JSON 输出工具，避免为悬浮窗接口引入额外依赖。 */
final class Json {
    private Json() {
    }

    static String quote(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder out = new StringBuilder(value.length() + 2);
        out.append('"');
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                case '\b' -> out.append("\\b");
                case '\f' -> out.append("\\f");
                default -> {
                    if (character < 0x20) {
                        out.append(String.format(Locale.ROOT, "\\u%04x", (int) character));
                    } else {
                        out.append(character);
                    }
                }
            }
        }
        return out.append('"').toString();
    }

    static String number(double value) {
        if (!Double.isFinite(value)) {
            return "0";
        }
        return Double.toString(value);
    }

    static String number(long value) {
        return Long.toString(value);
    }

    static String bool(boolean value) {
        return Boolean.toString(value);
    }
}
