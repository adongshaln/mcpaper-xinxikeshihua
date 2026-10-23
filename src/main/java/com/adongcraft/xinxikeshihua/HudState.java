package com.adongcraft.xinxikeshihua;

import org.bukkit.entity.LivingEntity;

import java.util.UUID;

/** 记录当前瞄准的实体，让血条上刚掉的那一段慢慢追上来。 */
final class HudState {
    private UUID entityId;
    private double shownHealth;

    double advance(LivingEntity entity) {
        double health = Math.max(0.0D, entity.getHealth());
        if (!entity.getUniqueId().equals(entityId)) {
            entityId = entity.getUniqueId();
            shownHealth = health;
            return shownHealth;
        }
        if (health >= shownHealth) {
            shownHealth = health;
            return shownHealth;
        }
        double gap = shownHealth - health;
        shownHealth = Math.max(health, shownHealth - Math.max(0.25D, gap * 0.18D));
        return shownHealth;
    }

    void reset() {
        entityId = null;
        shownHealth = 0.0D;
    }
}
