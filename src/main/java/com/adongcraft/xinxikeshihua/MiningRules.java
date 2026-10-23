package com.adongcraft.xinxikeshihua;

/**
 * 原版采集规则的简化版：方块是否需要正确工具、需要哪种工具、至少多少级。
 * 只回答“现在这一刀能不能拿到掉落物”，不计算挖掘速度。
 */
public final class MiningRules {
    private static final ToolKind[] PRIORITY = {
            ToolKind.PICKAXE, ToolKind.AXE, ToolKind.SHOVEL, ToolKind.HOE, ToolKind.SHEARS, ToolKind.SWORD
    };

    private MiningRules() {
    }

    public static MiningAssessment evaluate(MiningFacts facts, HeldTool held, MiningMode mode) {
        if (facts.unbreakable()) {
            return new MiningAssessment(true, false, ToolKind.UNKNOWN, ToolTier.WOOD, false,
                    MiningAssessment.Status.UNBREAKABLE);
        }

        boolean hasRequirement = facts.requiresCorrectTool();
        ToolKind requiredKind = requiredKind(facts, held);
        ToolTier requiredTier = ToolTier.byLevel(facts.requiredTier());

        if (mode == MiningMode.SPECTATOR) {
            return new MiningAssessment(false, false, requiredKind, requiredTier, hasRequirement,
                    MiningAssessment.Status.SPECTATOR);
        }
        if (mode == MiningMode.CREATIVE) {
            return new MiningAssessment(false, true, requiredKind, requiredTier, hasRequirement,
                    MiningAssessment.Status.CREATIVE);
        }
        if (!hasRequirement) {
            return new MiningAssessment(false, true, ToolKind.UNKNOWN, ToolTier.WOOD, false,
                    MiningAssessment.Status.CAN_HARVEST);
        }
        if (requiredKind == ToolKind.UNKNOWN) {
            return new MiningAssessment(false, false, ToolKind.UNKNOWN, requiredTier, true,
                    MiningAssessment.Status.WRONG_TOOL);
        }
        if (!held.isTool() || !facts.mineable(held.kind())) {
            return new MiningAssessment(false, false, requiredKind, requiredTier, true,
                    MiningAssessment.Status.WRONG_TOOL);
        }
        if (!held.tier().atLeast(requiredTier)) {
            return new MiningAssessment(false, false, requiredKind, requiredTier, true,
                    MiningAssessment.Status.TOO_WEAK);
        }
        return new MiningAssessment(false, true, requiredKind, requiredTier, true,
                MiningAssessment.Status.CAN_HARVEST);
    }

    private static ToolKind requiredKind(MiningFacts facts, HeldTool held) {
        if (held.isTool() && facts.mineable(held.kind())) {
            return held.kind();
        }
        for (ToolKind kind : PRIORITY) {
            if (facts.mineable(kind)) {
                return kind;
            }
        }
        return ToolKind.UNKNOWN;
    }
}
