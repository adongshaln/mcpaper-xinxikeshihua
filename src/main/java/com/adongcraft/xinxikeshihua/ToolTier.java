package com.adongcraft.xinxikeshihua;

/** 原版工具的采集等级。金工具和木工具同级，铜工具按石级处理。 */
public enum ToolTier {
    WOOD("木", "WOODEN", 0),
    STONE("石", "STONE", 1),
    IRON("铁", "IRON", 2),
    DIAMOND("钻石", "DIAMOND", 3),
    NETHERITE("下界合金", "NETHERITE", 4);

    private final String label;
    private final String prefix;
    private final int level;

    ToolTier(String label, String prefix, int level) {
        this.label = label;
        this.prefix = prefix;
        this.level = level;
    }

    public String label() {
        return label;
    }

    public String prefix() {
        return prefix;
    }

    public int level() {
        return level;
    }

    public boolean atLeast(ToolTier other) {
        return level >= other.level;
    }

    public static ToolTier byLevel(int level) {
        for (ToolTier tier : values()) {
            if (tier.level == level) {
                return tier;
            }
        }
        return level >= NETHERITE.level ? NETHERITE : WOOD;
    }
}
