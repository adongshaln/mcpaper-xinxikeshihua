package com.adongcraft.xinxikeshihua;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MiningRulesTest {
    @Test
    void unbreakableBlockIsNeverHarvestable() {
        MiningAssessment result = MiningRules.evaluate(
                facts(-1.0F, true, 0, ToolKind.PICKAXE),
                held(ToolKind.PICKAXE, ToolTier.NETHERITE),
                MiningMode.SURVIVAL);
        assertTrue(result.unbreakable());
        assertFalse(result.canHarvestNow());
        assertEquals(MiningAssessment.Status.UNBREAKABLE, result.status());
        assertFalse(result.hasRequirement());
    }

    @Test
    void dirtNeedsNoTool() {
        MiningAssessment result = MiningRules.evaluate(
                facts(0.5F, false, 0, ToolKind.SHOVEL),
                held(ToolKind.HAND, ToolTier.WOOD),
                MiningMode.SURVIVAL);
        assertTrue(result.canHarvestNow());
        assertFalse(result.hasRequirement());
        assertEquals(MiningAssessment.Status.CAN_HARVEST, result.status());
        assertEquals(ToolKind.UNKNOWN, result.requiredKind());
    }

    @Test
    void stoneNeedsAnyPickaxe() {
        MiningFacts stone = facts(1.5F, true, 0, ToolKind.PICKAXE);

        MiningAssessment byHand = MiningRules.evaluate(stone, held(ToolKind.HAND, ToolTier.WOOD), MiningMode.SURVIVAL);
        assertFalse(byHand.canHarvestNow());
        assertEquals(MiningAssessment.Status.WRONG_TOOL, byHand.status());
        assertEquals(ToolKind.PICKAXE, byHand.requiredKind());
        assertEquals(ToolTier.WOOD, byHand.requiredTier());
        assertTrue(byHand.hasRequirement());

        MiningAssessment wooden = MiningRules.evaluate(stone, held(ToolKind.PICKAXE, ToolTier.WOOD),
                MiningMode.SURVIVAL);
        assertTrue(wooden.canHarvestNow());
        assertEquals(MiningAssessment.Status.CAN_HARVEST, wooden.status());

        MiningAssessment iron = MiningRules.evaluate(stone, held(ToolKind.PICKAXE, ToolTier.IRON),
                MiningMode.SURVIVAL);
        assertTrue(iron.canHarvestNow());
    }

    @Test
    void ironOreNeedsStoneTier() {
        MiningFacts ironOre = facts(3.0F, true, 1, ToolKind.PICKAXE);

        MiningAssessment wooden = MiningRules.evaluate(ironOre, held(ToolKind.PICKAXE, ToolTier.WOOD),
                MiningMode.SURVIVAL);
        assertFalse(wooden.canHarvestNow());
        assertEquals(MiningAssessment.Status.TOO_WEAK, wooden.status());
        assertEquals(ToolTier.STONE, wooden.requiredTier());

        MiningAssessment stone = MiningRules.evaluate(ironOre, held(ToolKind.PICKAXE, ToolTier.STONE),
                MiningMode.SURVIVAL);
        assertTrue(stone.canHarvestNow());
    }

    @Test
    void obsidianNeedsDiamondTier() {
        MiningFacts obsidian = facts(50.0F, true, 3, ToolKind.PICKAXE);

        MiningAssessment iron = MiningRules.evaluate(obsidian, held(ToolKind.PICKAXE, ToolTier.IRON),
                MiningMode.SURVIVAL);
        assertFalse(iron.canHarvestNow());
        assertEquals(MiningAssessment.Status.TOO_WEAK, iron.status());
        assertEquals(ToolTier.DIAMOND, iron.requiredTier());

        MiningAssessment diamond = MiningRules.evaluate(obsidian, held(ToolKind.PICKAXE, ToolTier.DIAMOND),
                MiningMode.SURVIVAL);
        assertTrue(diamond.canHarvestNow());
    }

    @Test
    void wrongToolKindIsRejected() {
        MiningFacts log = facts(2.0F, true, 0, ToolKind.AXE);

        MiningAssessment pickaxe = MiningRules.evaluate(log, held(ToolKind.PICKAXE, ToolTier.NETHERITE),
                MiningMode.SURVIVAL);
        assertFalse(pickaxe.canHarvestNow());
        assertEquals(MiningAssessment.Status.WRONG_TOOL, pickaxe.status());
        assertEquals(ToolKind.AXE, pickaxe.requiredKind());

        MiningAssessment axe = MiningRules.evaluate(log, held(ToolKind.AXE, ToolTier.WOOD),
                MiningMode.SURVIVAL);
        assertTrue(axe.canHarvestNow());
    }

    @Test
    void creativeCanHarvestByHand() {
        MiningAssessment result = MiningRules.evaluate(
                facts(1.5F, true, 0, ToolKind.PICKAXE),
                held(ToolKind.HAND, ToolTier.WOOD),
                MiningMode.CREATIVE);
        assertTrue(result.canHarvestNow());
        assertEquals(MiningAssessment.Status.CREATIVE, result.status());
        assertTrue(result.hasRequirement());
    }

    @Test
    void spectatorCannotHarvest() {
        MiningAssessment result = MiningRules.evaluate(
                facts(1.5F, true, 0, ToolKind.PICKAXE),
                held(ToolKind.PICKAXE, ToolTier.NETHERITE),
                MiningMode.SPECTATOR);
        assertFalse(result.canHarvestNow());
        assertEquals(MiningAssessment.Status.SPECTATOR, result.status());
    }

    @Test
    void multipleToolKindsUseTheHeldOne() {
        MiningAssessment result = MiningRules.evaluate(
                facts(1.0F, true, 0, ToolKind.PICKAXE, ToolKind.AXE),
                held(ToolKind.AXE, ToolTier.WOOD),
                MiningMode.SURVIVAL);
        assertTrue(result.canHarvestNow());
        assertEquals(ToolKind.AXE, result.requiredKind());
    }

    @Test
    void unknownRequirementStillBlocksHarvest() {
        MiningAssessment result = MiningRules.evaluate(
                facts(1.0F, true, 0),
                held(ToolKind.PICKAXE, ToolTier.NETHERITE),
                MiningMode.SURVIVAL);
        assertFalse(result.canHarvestNow());
        assertEquals(MiningAssessment.Status.WRONG_TOOL, result.status());
        assertEquals(ToolKind.UNKNOWN, result.requiredKind());
        assertTrue(result.hasRequirement());
    }

    private static HeldTool held(ToolKind kind, ToolTier tier) {
        return new HeldTool(kind, tier);
    }

    private static MiningFacts facts(float hardness, boolean correctTool, int requiredTier, ToolKind... kinds) {
        Set<ToolKind> mineable = kinds.length == 0
                ? EnumSet.noneOf(ToolKind.class)
                : EnumSet.copyOf(Set.of(kinds));
        return new MiningFacts() {
            @Override
            public float hardness() {
                return hardness;
            }

            @Override
            public boolean requiresCorrectTool() {
                return correctTool;
            }

            @Override
            public boolean mineable(ToolKind kind) {
                return mineable.contains(kind);
            }

            @Override
            public int requiredTier() {
                return requiredTier;
            }
        };
    }
}
