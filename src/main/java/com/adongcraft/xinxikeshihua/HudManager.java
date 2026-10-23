package com.adongcraft.xinxikeshihua;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 管理每个在线玩家的一组 HUD 实体。 */
final class HudManager implements Listener {
    private final XinxiPlugin plugin;
    private final Map<UUID, PlayerHud> huds = new HashMap<>();
    private final NamespacedKey offKey;
    private Settings settings;
    private BukkitTask task;

    HudManager(XinxiPlugin plugin) {
        this.plugin = plugin;
        this.settings = plugin.settings();
        this.offKey = new NamespacedKey(plugin, "hud-off");
    }

    void start() {
        if (settings.enabled() && settings.worldHudEnabled()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (allowed(player)) {
                    ensure(player);
                }
            }
        }
        schedule();
    }

    void reload() {
        settings = plugin.settings();
        schedule();
        for (PlayerHud hud : huds.values()) {
            hud.applySettings(settings);
            if (!settings.enabled() || !settings.worldHudEnabled()) {
                hud.hide();
            }
        }
    }

    void shutdown() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        for (PlayerHud hud : huds.values()) {
            hud.remove();
        }
        huds.clear();
    }

    /** @return 切换之后是否开启 */
    boolean toggle(Player player) {
        boolean wasOff = isToggledOff(player);
        player.getPersistentDataContainer().set(offKey, PersistentDataType.BYTE, (byte) (wasOff ? 0 : 1));
        if (!wasOff) {
            PlayerHud hud = huds.get(player.getUniqueId());
            if (hud != null) {
                hud.hide();
            }
        }
        return wasOff;
    }

    boolean allowed(Player player) {
        return player.hasPermission("xinxikeshihua.use") && !isToggledOff(player);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (settings.enabled() && settings.worldHudEnabled() && allowed(event.getPlayer())) {
            ensure(event.getPlayer());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        remove(event.getPlayer());
    }

    @EventHandler
    public void onChangedWorld(PlayerChangedWorldEvent event) {
        // 展示实体还在旧世界，直接换一组更稳。
        remove(event.getPlayer());
        if (settings.enabled() && settings.worldHudEnabled() && allowed(event.getPlayer())) {
            ensure(event.getPlayer());
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        // 重生后客户端可能已经把展示实体清掉，换一组最稳。
        remove(event.getPlayer());
        if (settings.enabled() && settings.worldHudEnabled() && allowed(event.getPlayer())) {
            ensure(event.getPlayer());
        }
    }

    private void schedule() {
        if (task != null) {
            task.cancel();
        }
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, settings.updateIntervalTicks());
    }

    private void tick() {
        if (!settings.enabled() || !settings.worldHudEnabled()) {
            for (PlayerHud hud : huds.values()) {
                hud.hide();
            }
            return;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!allowed(player) || player.isDead() || !player.isOnline()) {
                PlayerHud hidden = huds.get(player.getUniqueId());
                if (hidden != null) {
                    hidden.hide();
                }
                continue;
            }
            PlayerHud hud = ensure(player);
            if (hud == null) {
                continue;
            }
            Targets target = TargetScanner.scan(player, settings);
            if (target == null) {
                hud.hide();
            } else {
                hud.render(target, settings);
            }
        }
    }

    private PlayerHud ensure(Player player) {
        UUID playerId = player.getUniqueId();
        PlayerHud hud = huds.get(playerId);
        if (hud != null && hud.valid()) {
            return hud;
        }
        if (hud != null) {
            hud.remove();
            huds.remove(playerId);
        }
        PlayerHud created = new PlayerHud(plugin, player);
        created.create();
        if (!created.valid()) {
            created.remove();
            return null;
        }
        huds.put(playerId, created);
        return created;
    }

    private void remove(Player player) {
        PlayerHud hud = huds.remove(player.getUniqueId());
        if (hud != null) {
            hud.remove();
        }
    }

    private boolean isToggledOff(Player player) {
        return player.getPersistentDataContainer()
                .getOrDefault(offKey, PersistentDataType.BYTE, (byte) 0) != 0;
    }
}
