package com.adongcraft.xinxikeshihua;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 给启动器悬浮窗准备的数据。主线程每两刻扫一次目标，HTTP 线程只读快照。
 * 伤害数字是事件队列，按 id 增量取走。
 */
final class OverlayFeed implements Listener {
    private static final long EVENT_TTL_MILLIS = 5_000L;
    private static final int MAX_EVENTS = 64;

    private final XinxiPlugin plugin;
    private final Map<UUID, Snapshot> snapshots = new ConcurrentHashMap<>();
    private final Map<UUID, EventQueue> events = new ConcurrentHashMap<>();
    private BukkitTask task;

    OverlayFeed(XinxiPlugin plugin) {
        this.plugin = plugin;
    }

    void start() {
        Settings settings = plugin.settings();
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, settings.overlayUpdateTicks());
    }

    void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        snapshots.clear();
        events.clear();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        snapshots.remove(playerId);
        events.remove(playerId);
    }

    void recordDamage(Player attacker, LivingEntity target, double amount, boolean critical,
                      org.bukkit.event.entity.EntityDamageEvent.DamageCause cause) {
        if (attacker == null || target == null || amount < 0.05D) {
            return;
        }
        Location location = target.getLocation();
        queue(attacker).add(amount, false, critical, cause == null ? "CUSTOM" : cause.name(),
                location, target.getHeight());
    }

    void recordHeal(Player player, double amount) {
        if (player == null || amount < 0.05D) {
            return;
        }
        queue(player).add(amount, true, false, "HEAL", player.getLocation(), player.getHeight());
    }

    String response(Player player, long since) {
        Snapshot snapshot = snapshots.get(player.getUniqueId());
        if (snapshot == null) {
            snapshot = new Snapshot(new Camera(0, 0, 0, 0, 0), null);
        }
        EventQueue queue = events.get(player.getUniqueId());
        List<Event> newEvents = queue == null ? List.of() : queue.since(since);
        long lastId = queue == null ? since : queue.lastId();
        boolean hidden = !plugin.hud().allowed(player);
        return toJson(player.getName(), hidden, snapshot, newEvents, lastId);
    }

    private void tick() {
        Settings settings = plugin.settings();
        if (!settings.overlayEnabled()) {
            snapshots.clear();
            return;
        }
        long cutoff = System.currentTimeMillis() - EVENT_TTL_MILLIS;
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID playerId = player.getUniqueId();
            EventQueue queue = events.get(playerId);
            if (queue != null) {
                queue.prune(cutoff);
            }
            if (player.isDead() || !player.isOnline()) {
                snapshots.put(playerId, new Snapshot(camera(player), null));
                continue;
            }
            Targets target = TargetScanner.scan(player, settings);
            snapshots.put(playerId, new Snapshot(camera(player), targetData(target)));
        }
    }

    private TargetData targetData(Targets target) {
        if (target instanceof Targets.BlockTarget block) {
            return blockData(block);
        }
        if (target instanceof Targets.EntityTarget entity) {
            return entityData(entity.entity());
        }
        return null;
    }

    private BlockData blockData(Targets.BlockTarget target) {
        Material material = target.material();
        MiningAssessment assessment = target.assessment();
        String nameKey = material.getBlockTranslationKey();
        if (nameKey == null) {
            nameKey = "block." + material.getKey().getNamespace() + "." + material.getKey().getKey();
        }
        Material icon = target.icon() != null ? target.icon() : material;
        String requiredKind = null;
        String requiredTier = null;
        String toolIconKey = null;
        String toolNameKey = null;
        if (assessment.hasRequirement()) {
            requiredKind = assessment.requiredKind().name();
            requiredTier = assessment.requiredTier().name();
            Material tool = ToolIcons.icon(assessment.requiredKind(), assessment.requiredTier());
            toolIconKey = tool.getKey().toString();
            toolNameKey = "item." + tool.getKey().getNamespace() + "." + tool.getKey().getKey();
        }
        return new BlockData(nameKey, plain(target.customName()), icon.getKey().toString(),
                assessment.status().name(), assessment.canHarvestNow(), requiredKind, requiredTier,
                toolIconKey, toolNameKey);
    }

    private EntityData entityData(LivingEntity entity) {
        String customName = entity instanceof Player player
                ? plain(player.displayName())
                : plain(entity.customName());
        List<EffectData> effects = new ArrayList<>();
        for (PotionEffect effect : effects(entity)) {
            PotionEffectType type = effect.getType();
            effects.add(new EffectData(type.translationKey(), effect.getAmplifier(),
                    effect.isInfinite() ? -1 : effect.getDuration(), type.getEffectCategory().name()));
        }
        return new EntityData(entity.getUniqueId().toString(), entity.getType().translationKey(), customName,
                entity.getHealth(), maxHealth(entity), entity.getAbsorptionAmount(), List.copyOf(effects));
    }

    private static List<PotionEffect> effects(LivingEntity entity) {
        List<PotionEffect> active = new ArrayList<>(entity.getActivePotionEffects());
        active.sort(Comparator
                .comparingInt((PotionEffect effect) -> rank(effect.getType().getEffectCategory()))
                .thenComparing(effect -> effect.getType().translationKey())
                .thenComparing(Comparator.comparingInt(PotionEffect::getAmplifier).reversed()));
        return active;
    }

    private static int rank(org.bukkit.potion.PotionEffectType.Category category) {
        return switch (category) {
            case HARMFUL -> 0;
            case NEUTRAL -> 1;
            case BENEFICIAL -> 2;
        };
    }

    private static double maxHealth(LivingEntity entity) {
        AttributeInstance attribute = entity.getAttribute(Attribute.MAX_HEALTH);
        if (attribute != null && Double.isFinite(attribute.getValue()) && attribute.getValue() > 0.0D) {
            return attribute.getValue();
        }
        return 20.0D;
    }

    private static Camera camera(Player player) {
        Location eye = player.getEyeLocation();
        return new Camera(eye.getX(), eye.getY(), eye.getZ(), eye.getYaw(), eye.getPitch());
    }

    private static String plain(Component component) {
        if (component == null) {
            return null;
        }
        String text = PlainTextComponentSerializer.plainText().serialize(component);
        return text.isBlank() ? null : text;
    }

    private EventQueue queue(Player player) {
        return events.computeIfAbsent(player.getUniqueId(), ignored -> new EventQueue());
    }

    private static String toJson(String playerName, boolean hidden, Snapshot snapshot,
                                List<Event> events, long lastId) {
        StringBuilder json = new StringBuilder(640);
        json.append("{\"online\":true,\"hidden\":").append(Json.bool(hidden))
                .append(",\"player\":").append(Json.quote(playerName));
        json.append(",\"camera\":{\"x\":").append(Json.number(snapshot.camera().x()))
                .append(",\"y\":").append(Json.number(snapshot.camera().y()))
                .append(",\"z\":").append(Json.number(snapshot.camera().z()))
                .append(",\"yaw\":").append(Json.number(snapshot.camera().yaw()))
                .append(",\"pitch\":").append(Json.number(snapshot.camera().pitch()))
                .append('}');
        json.append(",\"target\":");
        if (snapshot.target() instanceof BlockData block) {
            json.append("{\"type\":\"block\",\"nameKey\":").append(Json.quote(block.nameKey()))
                    .append(",\"customName\":").append(Json.quote(block.customName()))
                    .append(",\"iconKey\":").append(Json.quote(block.iconKey()))
                    .append(",\"status\":").append(Json.quote(block.status()))
                    .append(",\"canHarvest\":").append(Json.bool(block.canHarvest()))
                    .append(",\"requiredKind\":").append(Json.quote(block.requiredKind()))
                    .append(",\"requiredTier\":").append(Json.quote(block.requiredTier()))
                    .append(",\"toolIconKey\":").append(Json.quote(block.toolIconKey()))
                    .append(",\"toolNameKey\":").append(Json.quote(block.toolNameKey()))
                    .append('}');
        } else if (snapshot.target() instanceof EntityData entity) {
            json.append("{\"type\":\"entity\",\"entityId\":").append(Json.quote(entity.entityId()))
                    .append(",\"nameKey\":").append(Json.quote(entity.nameKey()))
                    .append(",\"customName\":").append(Json.quote(entity.customName()))
                    .append(",\"health\":").append(Json.number(entity.health()))
                    .append(",\"maxHealth\":").append(Json.number(entity.maxHealth()))
                    .append(",\"absorption\":").append(Json.number(entity.absorption()))
                    .append(",\"effects\":[");
            for (int index = 0; index < entity.effects().size(); index++) {
                if (index > 0) {
                    json.append(',');
                }
                EffectData effect = entity.effects().get(index);
                json.append("{\"key\":").append(Json.quote(effect.key()))
                        .append(",\"amplifier\":").append(effect.amplifier())
                        .append(",\"duration\":").append(effect.duration())
                        .append(",\"category\":").append(Json.quote(effect.category()))
                        .append('}');
            }
            json.append("]}");
        } else {
            json.append("null");
        }
        json.append(",\"events\":[");
        for (int index = 0; index < events.size(); index++) {
            if (index > 0) {
                json.append(',');
            }
            Event event = events.get(index);
            json.append("{\"id\":").append(event.id())
                    .append(",\"amount\":").append(Json.number(event.amount()))
                    .append(",\"healing\":").append(Json.bool(event.healing()))
                    .append(",\"critical\":").append(Json.bool(event.critical()))
                    .append(",\"cause\":").append(Json.quote(event.cause()))
                    .append(",\"x\":").append(Json.number(event.x()))
                    .append(",\"y\":").append(Json.number(event.y()))
                    .append(",\"z\":").append(Json.number(event.z()))
                    .append(",\"height\":").append(Json.number(event.height()))
                    .append('}');
        }
        json.append("],\"lastEventId\":").append(lastId).append('}');
        return json.toString();
    }

    private record Camera(double x, double y, double z, float yaw, float pitch) {
    }

    private sealed interface TargetData permits BlockData, EntityData {
    }

    private record BlockData(String nameKey, String customName, String iconKey, String status,
                             boolean canHarvest, String requiredKind, String requiredTier,
                             String toolIconKey, String toolNameKey) implements TargetData {
    }

    private record EntityData(String entityId, String nameKey, String customName, double health,
                              double maxHealth, double absorption, List<EffectData> effects)
            implements TargetData {
    }

    private record EffectData(String key, int amplifier, int duration, String category) {
    }

    private record Snapshot(Camera camera, TargetData target) {
    }

    private record Event(long id, long time, double amount, boolean healing, boolean critical, String cause,
                         double x, double y, double z, double height) {
    }

    private static final class EventQueue {
        private final ArrayDeque<Event> queue = new ArrayDeque<>();
        private long nextId = 1;

        synchronized void add(double amount, boolean healing, boolean critical, String cause,
                              Location location, double height) {
            queue.addLast(new Event(nextId++, System.currentTimeMillis(), amount, healing, critical, cause,
                    location.getX(), location.getY(), location.getZ(), height));
            while (queue.size() > MAX_EVENTS) {
                queue.removeFirst();
            }
        }

        synchronized void prune(long cutoff) {
            while (!queue.isEmpty() && queue.peekFirst().time() < cutoff) {
                queue.removeFirst();
            }
        }

        synchronized List<Event> since(long since) {
            List<Event> result = new ArrayList<>();
            for (Event event : queue) {
                if (event.id() > since) {
                    result.add(event);
                }
            }
            return result;
        }

        synchronized long lastId() {
            return nextId - 1;
        }
    }
}
