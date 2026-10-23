package com.adongcraft.xinxikeshihua;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public final class XinxiPlugin extends JavaPlugin {
    private Settings settings;
    private HudManager hud;
    private DamageNumbers numbers;
    private OverlayFeed overlayFeed;
    private OverlayServer overlayServer;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        settings = Settings.load(getConfig());

        hud = new HudManager(this);
        numbers = new DamageNumbers(this);
        getServer().getPluginManager().registerEvents(hud, this);
        getServer().getPluginManager().registerEvents(numbers, this);

        XinxiCommand command = new XinxiCommand(this);
        var pluginCommand = getCommand("xinxikeshihua");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        }

        hud.start();
        numbers.start();
        if (settings.overlayEnabled()) {
            startOverlay();
        }
        getLogger().info("信息可视化已启用");
    }

    @Override
    public void onDisable() {
        stopOverlay();
        if (hud != null) {
            hud.shutdown();
        }
        if (numbers != null) {
            numbers.shutdown();
        }
    }

    public void reloadPlugin() {
        stopOverlay();
        reloadConfig();
        settings = Settings.load(getConfig());
        hud.reload();
        numbers.reload();
        if (settings.overlayEnabled()) {
            startOverlay();
        }
    }

    private void startOverlay() {
        overlayFeed = new OverlayFeed(this);
        getServer().getPluginManager().registerEvents(overlayFeed, this);
        overlayFeed.start();
        overlayServer = new OverlayServer(this, overlayFeed);
        overlayServer.start();
    }

    private void stopOverlay() {
        if (overlayServer != null) {
            overlayServer.stop();
            overlayServer = null;
        }
        if (overlayFeed != null) {
            HandlerList.unregisterAll(overlayFeed);
            overlayFeed.stop();
            overlayFeed = null;
        }
    }

    public Settings settings() {
        return settings;
    }

    HudManager hud() {
        return hud;
    }

    DamageNumbers numbers() {
        return numbers;
    }

    OverlayFeed overlayFeed() {
        return overlayFeed;
    }
}
