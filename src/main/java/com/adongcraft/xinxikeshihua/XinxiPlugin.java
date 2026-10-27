package com.adongcraft.xinxikeshihua;

import org.bukkit.plugin.java.JavaPlugin;

public final class XinxiPlugin extends JavaPlugin {
    private Settings settings;
    private HudManager hud;
    private DamageNumbers numbers;
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
        getLogger().info("信息可视化已启用");
    }

    @Override
    public void onDisable() {
        if(nativeHud!=null)nativeHud.stop();
        if (hud != null) {
            hud.shutdown();
        }
        if (numbers != null) {
            numbers.shutdown();
        }
    }

    public void reloadPlugin() {
        reloadConfig();
        settings = Settings.load(getConfig());
        hud.reload();
        numbers.reload();
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
}
