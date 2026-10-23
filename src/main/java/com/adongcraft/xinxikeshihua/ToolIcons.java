package com.adongcraft.xinxikeshihua;

import org.bukkit.Material;

/** 需求图标的物品。UNKNOWN 用屏障表示“说不清”。 */
public final class ToolIcons {
    private ToolIcons() {
    }

    public static Material icon(ToolKind kind, ToolTier tier) {
        if (kind == ToolKind.SHEARS) {
            return Material.SHEARS;
        }
        if (kind == ToolKind.UNKNOWN || kind == ToolKind.HAND || kind == ToolKind.OTHER) {
            return Material.BARRIER;
        }
        String name = tier.prefix() + "_" + kind.name();
        try {
            return Material.valueOf(name);
        } catch (IllegalArgumentException exception) {
            return Material.BARRIER;
        }
    }
}
