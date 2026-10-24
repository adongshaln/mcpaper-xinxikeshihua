package com.adongcraft.xinxikeshihua;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public final class XinxiPlugin extends JavaPlugin {
    private Settings settings;
    private HudManager hud;
    private DamageNumbers numbers;
    private OverlayFeed overlayFeed;
    private OverlayServer overlayServer;
    private NativeHud nativeHud;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        // Check the saved file, ignoring bundled defaults, so existing installs migrate too.
        if(!getConfig().contains("native-hud-version",true) || getConfig().getInt("native-hud-version",0)<1) {
            getConfig().set("native-hud-version",1);getConfig().set("hud.world-enabled",false);
            getConfig().set("hud.background-opacity",0);getConfig().set("damage-numbers.in-world",true);saveConfig();
        }
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
        nativeHud=new NativeHud(this);getServer().getPluginManager().registerEvents(nativeHud,this);nativeHud.start();
        if (settings.overlayEnabled()) {
            startOverlay();
        }
        getLogger().info("信息可视化已启用");
    }

    @Override
    public void onDisable() {
        if(nativeHud!=null)nativeHud.stop();
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
        overlayServer = new OverlayServer(this, null);
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
