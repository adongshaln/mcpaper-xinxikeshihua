package com.adongcraft.xinxikeshihua;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;

/** 准星指向的东西。 */
public sealed interface Targets permits Targets.BlockTarget, Targets.EntityTarget {
    record BlockTarget(Material material, Component customName, MiningAssessment assessment, Material icon)
            implements Targets {
    }

    record EntityTarget(LivingEntity entity) implements Targets {
    }
}
