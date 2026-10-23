package com.adongcraft.xinxikeshihua;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

/** 统一关闭原版默认斜体，避免中文名字看起来歪着。 */
final class Styles {
    private Styles() {
    }

    static Style plain(TextColor color) {
        return Style.style(color)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, false);
    }

    static Style bold(TextColor color) {
        return Style.style(color)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true);
    }

    static Component text(String value, TextColor color) {
        return Component.text(value, plain(color));
    }

    static Component bold(String value, TextColor color) {
        return Component.text(value, bold(color));
    }

    static Component glyph(String value, TextColor color) {
        return Component.text(value, plain(color));
    }

    static Component translatable(String key, TextColor color) {
        return Component.translatable(key, plain(color));
    }

    static Component gray(String value) {
        return text(value, NamedTextColor.GRAY);
    }
}
