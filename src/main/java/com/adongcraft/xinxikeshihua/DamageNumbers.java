package com.adongcraft.xinxikeshihua;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** 伤害和治疗时在生物头顶跳出的数字。所有数字共用一个每刻任务。 */
final class DamageNumbers implements Listener {
    private final XinxiPlugin plugin;
    private final List<Floating> floating = new ArrayList<>();
    private Settings settings;
    private BukkitTask task;

    DamageNumbers(XinxiPlugin plugin) {
        this.plugin = plugin;
        this.settings = plugin.settings();
    }

    void start() {
        task = org.bukkit.Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
    }

    void reload() {
        settings = plugin.settings();
        if (!settings.damageEnabled()) {
            clear();
            return;
        }
        while (floating.size() > settings.maxActiveNumbers()) {
            Floating oldest = floating.removeFirst();
            oldest.remove();
        }
    }

    void shutdown() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        clear();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!settings.damageEnabled() || !(event.getEntity() instanceof LivingEntity entity)
                || Tags.isMarker(entity)) {
            return;
        }
        double amount = event.getFinalDamage();
        if (amount < 0.05D) {
            return;
        }
        boolean critical = event instanceof EntityDamageByEntityEvent byEntity && byEntity.isCritical();
        spawn(entity, amount, event.getCause(), critical, false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHeal(EntityRegainHealthEvent event) {
        if (!settings.damageEnabled() || !settings.showHealing()
                || !(event.getEntity() instanceof LivingEntity entity)
                || Tags.isMarker(entity)) {
            return;
        }
        double amount = event.getAmount();
        if (amount < 0.05D) {
            return;
        }
        spawn(entity, amount, null, false, true);
    }

    private void spawn(LivingEntity entity, double amount, EntityDamageEvent.DamageCause cause,
                       boolean critical, boolean healing) {
        World world = entity.getWorld();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Location at = entity.getLocation()
                .add(0.0D, entity.getHeight() * 0.8D + 0.25D, 0.0D)
                .add(random.nextDouble(-0.3D, 0.3D), random.nextDouble(0.0D, 0.12D),
                        random.nextDouble(-0.3D, 0.3D));
        NamedTextColor color = healing ? NamedTextColor.GREEN : DamagePalette.color(cause, critical);
        final Component label = healing
                ? Styles.bold("+" + NumberText.format(amount), color)
                : (critical
                        ? Styles.bold("-" + NumberText.format(amount) + "!", NamedTextColor.GOLD)
                        : Styles.bold("-" + NumberText.format(amount), color));

        TextDisplay display;
        try {
            display = world.spawn(at, TextDisplay.class, entityDisplay -> {
                entityDisplay.text(label);
                entityDisplay.setGravity(false);
                entityDisplay.setBillboard(Display.Billboard.CENTER);
                entityDisplay.setSeeThrough(settings.damageSeeThrough());
                entityDisplay.setShadowed(true);
                entityDisplay.setDefaultBackground(false);
                entityDisplay.setTextOpacity((byte) 255);
                entityDisplay.setBrightness(new Display.Brightness(15, 15));
                entityDisplay.setViewRange(1.0F);
                entityDisplay.setLineWidth(200);
                entityDisplay.setTransformation(scale(settings.damageScale()));
                entityDisplay.setDisplayWidth(1.0F);
                entityDisplay.setDisplayHeight(1.0F);
                entityDisplay.setPersistent(false);
                entityDisplay.addScoreboardTag(Tags.NUMBER);
            });
        } catch (RuntimeException exception) {
            plugin.getLogger().warning("无法创建伤害数字：" + exception.getMessage());
            return;
        }

        while (floating.size() >= settings.maxActiveNumbers()) {
            Floating oldest = floating.removeFirst();
            oldest.remove();
        }
        floating.add(new Floating(display, at.clone(), settings.damageLifetimeTicks(), settings.damageRise()));
    }

    private void tick() {
        Iterator<Floating> iterator = floating.iterator();
        while (iterator.hasNext()) {
            if (!iterator.next().tick()) {
                iterator.remove();
            }
        }
    }

    private void clear() {
        for (Floating number : floating) {
            number.remove();
        }
        floating.clear();
    }

    private static Transformation scale(double value) {
        return new Transformation(
                new Vector3f(0.0F, 0.0F, 0.0F),
                new AxisAngle4f(0.0F, 0.0F, 1.0F, 0.0F),
                new Vector3f((float) value, (float) value, (float) value),
                new AxisAngle4f(0.0F, 0.0F, 1.0F, 0.0F)
        );
    }

    private static final class Floating {
        private final TextDisplay display;
        private final Location location;
        private final int lifetime;
        private final double rise;
        private int age;
        private int lastAlpha = 255;

        private Floating(TextDisplay display, Location location, int lifetime, double rise) {
            this.display = display;
            this.location = location;
            this.lifetime = lifetime;
            this.rise = rise;
        }

        private boolean tick() {
            if (display == null || !display.isValid()) {
                return false;
            }
            age++;
            if (age >= lifetime) {
                remove();
                return false;
            }
            location.add(0.0D, rise, 0.0D);
            display.teleport(location);

            int fadeStart = lifetime * 6 / 10;
            int alpha = age <= fadeStart
                    ? 255
                    : Math.max(0, 255 * (lifetime - age) / Math.max(1, lifetime - fadeStart));
            if (alpha != lastAlpha) {
                display.setTextOpacity((byte) alpha);
                lastAlpha = alpha;
            }
            return true;
        }

        private void remove() {
            if (display != null && display.isValid()) {
                display.remove();
            }
        }
    }
}
