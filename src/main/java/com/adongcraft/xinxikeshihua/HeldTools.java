package com.adongcraft.xinxikeshihua;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/** 把主手物品映射成采集规则需要的工具种类和等级。 */
public final class HeldTools {
    private HeldTools() {
    }

    public static HeldTool of(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return HeldTool.empty();
        }
        Material type = item.getType();
        String name = type.name();
        ToolKind kind = kindOf(name);
        if (kind == ToolKind.HAND || kind == ToolKind.OTHER) {
            return new HeldTool(kind, ToolTier.WOOD);
        }
        return new HeldTool(kind, tierOf(name));
    }

    private static ToolKind kindOf(String name) {
        if (name.equals("SHEARS")) {
            return ToolKind.SHEARS;
        }
        if (name.endsWith("_PICKAXE")) {
            return ToolKind.PICKAXE;
        }
        if (name.endsWith("_AXE")) {
            return ToolKind.AXE;
        }
        if (name.endsWith("_SHOVEL")) {
            return ToolKind.SHOVEL;
        }
        if (name.endsWith("_HOE")) {
            return ToolKind.HOE;
        }
        if (name.endsWith("_SWORD")) {
            return ToolKind.SWORD;
        }
        return ToolKind.OTHER;
    }

    private static ToolTier tierOf(String name) {
        if (name.startsWith("GOLDEN_") || name.startsWith("WOODEN_")) {
            return ToolTier.WOOD;
        }
        if (name.startsWith("COPPER_") || name.startsWith("STONE_")) {
            return ToolTier.STONE;
        }
        if (name.startsWith("IRON_")) {
            return ToolTier.IRON;
        }
        if (name.startsWith("DIAMOND_")) {
            return ToolTier.DIAMOND;
        }
        if (name.startsWith("NETHERITE_")) {
            return ToolTier.NETHERITE;
        }
        return ToolTier.WOOD;
    }
}
