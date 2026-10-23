package com.adongcraft.xinxikeshihua;

import org.bukkit.configuration.file.FileConfiguration;

/** 一次 reload 之后不再变化的配置快照。 */
public final class Settings {
    private final boolean enabled;
    private final boolean worldHudEnabled;
    private final double blockReach;
    private final double entityReach;
    private final int updateIntervalTicks;
    private final double hudDistance;
    private final double hudScale;
    private final double anchorX;
    private final double anchorY;
    private final double lineHeightFactor;
    private final int backgroundOpacity;
    private final int maxEffects;
    private final boolean showBlockIcon;
    private final boolean showToolIcon;
    private final boolean damageEnabled;
    private final int damageLifetimeTicks;
    private final double damageRise;
    private final double damageScale;
    private final boolean damageSeeThrough;
    private final boolean showHealing;
    private final int maxActiveNumbers;
    private final boolean inWorldDamageNumbers;
    private final boolean overlayEnabled;
    private final String overlayBind;
    private final int overlayPort;
    private final String overlayToken;
    private final int overlayUpdateTicks;

    private Settings(FileConfiguration config) {
        enabled = config.getBoolean("enabled", true);
        worldHudEnabled = config.getBoolean("hud.world-enabled", false);
        blockReach = clamp(config.getDouble("hud.block-reach", 5.0D), 1.0D, 8.0D);
        entityReach = clamp(config.getDouble("hud.entity-reach", 4.0D), 1.0D, 8.0D);
        updateIntervalTicks = clampInt(config.getInt("hud.update-interval-ticks", 1), 1, 10);
        hudDistance = clamp(config.getDouble("hud.distance", 0.9D), 0.3D, 3.0D);
        hudScale = clamp(config.getDouble("hud.scale", 0.085D), 0.02D, 0.5D);
        anchorX = clamp(config.getDouble("hud.anchor-x", -0.85D), -2.0D, 0.0D);
        anchorY = clamp(config.getDouble("hud.anchor-y", 0.34D), 0.0D, 1.0D);
        lineHeightFactor = clamp(config.getDouble("hud.line-height-factor", 0.25D), 0.1D, 0.5D);
        backgroundOpacity = clampInt(config.getInt("hud.background-opacity", 140), 0, 255);
        maxEffects = clampInt(config.getInt("hud.max-effects", 4), 0, 8);
        showBlockIcon = config.getBoolean("hud.show-block-icon", true);
        showToolIcon = config.getBoolean("hud.show-tool-icon", true);

        damageEnabled = config.getBoolean("damage-numbers.enabled", true);
        damageLifetimeTicks = clampInt(config.getInt("damage-numbers.lifetime-ticks", 24), 8, 60);
        damageRise = clamp(config.getDouble("damage-numbers.rise-per-tick", 0.035D), 0.0D, 0.2D);
        damageScale = clamp(config.getDouble("damage-numbers.scale", 0.55D), 0.2D, 2.0D);
        damageSeeThrough = config.getBoolean("damage-numbers.see-through", true);
        showHealing = config.getBoolean("damage-numbers.show-healing", true);
        maxActiveNumbers = clampInt(config.getInt("damage-numbers.max-active", 80), 1, 500);
        inWorldDamageNumbers = config.getBoolean("damage-numbers.in-world", false);

        overlayEnabled = config.getBoolean("overlay.enabled", true);
        String bind = config.getString("overlay.bind", "0.0.0.0");
        overlayBind = bind == null || bind.isBlank() ? "0.0.0.0" : bind;
        overlayPort = clampInt(config.getInt("overlay.port", 8765), 1024, 65535);
        String token = config.getString("overlay.token", "change-me");
        overlayToken = token == null ? "" : token;
        overlayUpdateTicks = clampInt(config.getInt("overlay.update-interval-ticks", 2), 1, 10);
    }

    public static Settings load(FileConfiguration config) {
        return new Settings(config);
    }

    public boolean enabled() {
        return enabled;
    }

    public boolean worldHudEnabled() {
        return worldHudEnabled;
    }

    public double blockReach() {
        return blockReach;
    }

    public double entityReach() {
        return entityReach;
    }

    public int updateIntervalTicks() {
        return updateIntervalTicks;
    }

    public double hudDistance() {
        return hudDistance;
    }

    public double hudScale() {
        return hudScale;
    }

    public double anchorX() {
        return anchorX;
    }

    public double anchorY() {
        return anchorY;
    }

    public double lineHeightFactor() {
        return lineHeightFactor;
    }

    public int backgroundOpacity() {
        return backgroundOpacity;
    }

    public int maxEffects() {
        return maxEffects;
    }

    public boolean showBlockIcon() {
        return showBlockIcon;
    }

    public boolean showToolIcon() {
        return showToolIcon;
    }

    public boolean damageEnabled() {
        return damageEnabled;
    }

    public int damageLifetimeTicks() {
        return damageLifetimeTicks;
    }

    public double damageRise() {
        return damageRise;
    }

    public double damageScale() {
        return damageScale;
    }

    public boolean damageSeeThrough() {
        return damageSeeThrough;
    }

    public boolean showHealing() {
        return showHealing;
    }

    public int maxActiveNumbers() {
        return maxActiveNumbers;
    }

    public boolean inWorldDamageNumbers() {
        return inWorldDamageNumbers;
    }

    public boolean overlayEnabled() {
        return overlayEnabled;
    }

    public String overlayBind() {
        return overlayBind;
    }

    public int overlayPort() {
        return overlayPort;
    }

    public String overlayToken() {
        return overlayToken;
    }

    public int overlayUpdateTicks() {
        return overlayUpdateTicks;
    }

    private static double clamp(double value, double min, double max) {
        if (!Double.isFinite(value)) {
            return min;
        }
        return Math.max(min, Math.min(max, value));
    }

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
