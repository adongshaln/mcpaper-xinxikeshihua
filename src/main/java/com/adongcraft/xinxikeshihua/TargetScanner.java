package com.adongcraft.xinxikeshihua;

import net.kyori.adventure.text.Component;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Nameable;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

/** 每个玩家每刻做一次方块 + 实体的射线检测，取更近的目标。 */
public final class TargetScanner {
    private static final double ENTITY_HITBOX_EXPANSION = 0.35D;

    private TargetScanner() {
    }

    public static Targets scan(Player player, Settings settings) {
        Location eye = player.getEyeLocation();
        Vector direction = eye.getDirection();

        RayTraceResult blockHit = player.getWorld().rayTraceBlocks(
                eye, direction, settings.blockReach(), FluidCollisionMode.NEVER, false);
        RayTraceResult entityHit = player.getWorld().rayTraceEntities(
                eye, direction, settings.entityReach(), ENTITY_HITBOX_EXPANSION,
                entity -> entity instanceof LivingEntity
                        && entity != player
                        && !entity.isDead()
                        && player.canSee(entity)
                        && !Tags.isMarker(entity));

        double blockDistance = distance(eye, blockHit);
        double entityDistance = distance(eye, entityHit);

        if (entityHit != null && entityHit.getHitEntity() instanceof LivingEntity living
                && entityDistance <= blockDistance) {
            return new Targets.EntityTarget(living);
        }
        if (blockHit != null && blockHit.getHitBlock() != null) {
            Block block = blockHit.getHitBlock();
            BlockData data = block.getBlockData();
            MiningAssessment assessment = MiningRules.evaluate(
                    BukkitMiningFacts.of(data),
                    HeldTools.of(player.getInventory().getItemInMainHand()),
                    mode(player));
            return new Targets.BlockTarget(block.getType(), customName(block), assessment, icon(block.getType()));
        }
        return null;
    }

    private static MiningMode mode(Player player) {
        return switch (player.getGameMode()) {
            case CREATIVE -> MiningMode.CREATIVE;
            case SPECTATOR -> MiningMode.SPECTATOR;
            default -> MiningMode.SURVIVAL;
        };
    }

    private static Component customName(Block block) {
        BlockState state = block.getState(false);
        if (state instanceof Nameable nameable && nameable.customName() != null) {
            return nameable.customName();
        }
        return null;
    }

    private static double distance(Location eye, RayTraceResult hit) {
        if (hit == null || hit.getHitPosition() == null) {
            return Double.MAX_VALUE;
        }
        return eye.toVector().distance(hit.getHitPosition());
    }

    /** 没有对应物品的方块（水、火、作物等）给一个意思相近的图标。 */
    static Material icon(Material type) {
        if (type.isItem()) {
            return type;
        }
        String name = type.name();
        Material fallback = switch (type) {
            case WATER, BUBBLE_COLUMN -> Material.WATER_BUCKET;
            case LAVA -> Material.LAVA_BUCKET;
            case FIRE, SOUL_FIRE -> Material.FLINT_AND_STEEL;
            case NETHER_PORTAL, END_PORTAL, END_GATEWAY -> Material.ENDER_EYE;
            case TRIPWIRE -> Material.STRING;
            case REDSTONE_WIRE -> Material.REDSTONE;
            case CARROTS -> Material.CARROT;
            case POTATOES -> Material.POTATO;
            case BEETROOTS -> Material.BEETROOT;
            case FARMLAND, DIRT_PATH -> Material.DIRT;
            case TALL_GRASS -> Material.SHORT_GRASS;
            case LARGE_FERN -> Material.FERN;
            case KELP_PLANT -> Material.KELP;
            default -> null;
        };
        if (fallback != null) {
            return fallback;
        }
        if (name.endsWith("_WALL_SIGN")) {
            return Material.OAK_SIGN;
        }
        if (name.endsWith("_WALL_BANNER")) {
            return Material.WHITE_BANNER;
        }
        if (name.startsWith("POTTED_")) {
            return Material.FLOWER_POT;
        }
        return null;
    }
}
