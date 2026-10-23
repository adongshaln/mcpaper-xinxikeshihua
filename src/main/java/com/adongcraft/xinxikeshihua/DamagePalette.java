package com.adongcraft.xinxikeshihua;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

/** 伤害数字的颜色：普通攻击偏红，暴击金色，元素伤害各有颜色。 */
public final class DamagePalette {
    private DamagePalette() {
    }

    public static NamedTextColor color(DamageCause cause, boolean critical) {
        if (critical) {
            return NamedTextColor.GOLD;
        }
        if (cause == null) {
            return NamedTextColor.RED;
        }
        return switch (cause) {
            case FIRE, FIRE_TICK, LAVA, HOT_FLOOR, CAMPFIRE, LIGHTNING -> NamedTextColor.GOLD;
            case POISON -> NamedTextColor.GREEN;
            case WITHER -> NamedTextColor.DARK_GRAY;
            case MAGIC, DRAGON_BREATH -> NamedTextColor.LIGHT_PURPLE;
            case FALL, FLY_INTO_WALL -> NamedTextColor.WHITE;
            case VOID -> NamedTextColor.DARK_PURPLE;
            case DROWNING -> NamedTextColor.BLUE;
            case STARVATION -> NamedTextColor.YELLOW;
            case FREEZE -> NamedTextColor.AQUA;
            case SONIC_BOOM -> NamedTextColor.DARK_AQUA;
            case THORNS -> NamedTextColor.LIGHT_PURPLE;
            default -> NamedTextColor.RED;
        };
    }
}
