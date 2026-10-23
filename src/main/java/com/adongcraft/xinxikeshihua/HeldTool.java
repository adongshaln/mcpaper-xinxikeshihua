package com.adongcraft.xinxikeshihua;

/** 玩家主手里那件东西，只保留判断采集条件需要的部分。 */
public record HeldTool(ToolKind kind, ToolTier tier) {
    public static HeldTool empty() {
        return new HeldTool(ToolKind.HAND, ToolTier.WOOD);
    }

    public boolean isTool() {
        return kind != ToolKind.HAND && kind != ToolKind.OTHER;
    }
}
