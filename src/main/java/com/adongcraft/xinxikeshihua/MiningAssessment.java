package com.adongcraft.xinxikeshihua;

/** 一次瞄准方块的采集判断结果。 */
public record MiningAssessment(
        boolean unbreakable,
        boolean canHarvestNow,
        ToolKind requiredKind,
        ToolTier requiredTier,
        boolean hasRequirement,
        Status status
) {
    public enum Status {
        UNBREAKABLE,
        SPECTATOR,
        CREATIVE,
        CAN_HARVEST,
        WRONG_TOOL,
        TOO_WEAK
    }
}
