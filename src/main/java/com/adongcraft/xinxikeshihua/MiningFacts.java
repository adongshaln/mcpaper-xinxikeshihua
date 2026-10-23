package com.adongcraft.xinxikeshihua;

/** 判断挖掘条件所需的方块事实。测试里用假实现。 */
public interface MiningFacts {
    float hardness();

    boolean requiresCorrectTool();

    boolean mineable(ToolKind kind);

    /** 至少需要的采集等级：0 木/金，1 石/铜，2 铁，3 钻石，4 下界合金。 */
    int requiredTier();

    default boolean unbreakable() {
        return hardness() < 0.0F;
    }
}
