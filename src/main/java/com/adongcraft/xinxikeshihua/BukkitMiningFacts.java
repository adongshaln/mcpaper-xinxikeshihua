package com.adongcraft.xinxikeshihua;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.data.BlockData;

/** 把 Bukkit 的方块数据接到采集规则上。 */
public final class BukkitMiningFacts implements MiningFacts {
    private final float hardness;
    private final boolean requiresCorrectTool;
    private final boolean pickaxe;
    private final boolean axe;
    private final boolean shovel;
    private final boolean hoe;
    private final boolean cobweb;
    private final int requiredTier;

    private BukkitMiningFacts(BlockData data) {
        Material material = data.getMaterial();
        this.hardness = material.getHardness();
        this.cobweb = material == Material.COBWEB;
        // 蜘蛛网用手也能打掉，但只有剪刀或剑才掉线；把它当作有工具条件。
        this.requiresCorrectTool = data.requiresCorrectToolForDrops() || cobweb;
        this.pickaxe = Tag.MINEABLE_PICKAXE.isTagged(material);
        this.axe = Tag.MINEABLE_AXE.isTagged(material);
        this.shovel = Tag.MINEABLE_SHOVEL.isTagged(material);
        this.hoe = Tag.MINEABLE_HOE.isTagged(material);
        this.requiredTier = Tag.NEEDS_DIAMOND_TOOL.isTagged(material) ? 3
                : Tag.NEEDS_IRON_TOOL.isTagged(material) ? 2
                : Tag.NEEDS_STONE_TOOL.isTagged(material) ? 1 : 0;
    }

    public static MiningFacts of(BlockData data) {
        return new BukkitMiningFacts(data);
    }

    @Override
    public float hardness() {
        return hardness;
    }

    @Override
    public boolean requiresCorrectTool() {
        return requiresCorrectTool;
    }

    @Override
    public boolean mineable(ToolKind kind) {
        return switch (kind) {
            case PICKAXE -> pickaxe;
            case AXE -> axe;
            case SHOVEL -> shovel;
            case HOE -> hoe;
            case SHEARS, SWORD -> cobweb;
            default -> false;
        };
    }

    @Override
    public int requiredTier() {
        return requiredTier;
    }
}
