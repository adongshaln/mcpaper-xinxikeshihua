package com.adongcraft.xinxikeshihua;

import org.bukkit.entity.Entity;

/** 插件自己生成的展示实体，不能被当成瞄准目标，也不能再弹伤害数字。 */
final class Tags {
    static final String HUD = "mcpaper_xinxikeshihua_hud";
    static final String NUMBER = "mcpaper_xinxikeshihua_number";

    private Tags() {
    }

    static boolean isHud(Entity entity) {
        return entity != null && entity.getScoreboardTags().contains(HUD);
    }

    static boolean isNumber(Entity entity) {
        return entity != null && entity.getScoreboardTags().contains(NUMBER);
    }

    static boolean isMarker(Entity entity) {
        return isHud(entity) || isNumber(entity);
    }
}
