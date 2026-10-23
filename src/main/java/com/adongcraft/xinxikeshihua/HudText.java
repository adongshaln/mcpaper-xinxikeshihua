package com.adongcraft.xinxikeshihua;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffectType.Category;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** 把方块/实体数据写成 Adventure 组件。每行前面留四个空格给图标。 */
public final class HudText {
    private static final String LEAD = "    ";

    public record Panel(Component text, int lines, Material blockIcon, Material toolIcon) {
    }

    private HudText() {
    }

    public static Panel block(Targets.BlockTarget target, Settings settings) {
        List<Component> lines = new ArrayList<>();
        lines.add(line(blockName(target)));
        lines.add(line(statusLine(target.assessment())));

        Material toolIcon = null;
        if (target.assessment().hasRequirement()) {
            lines.add(line(requirementLine(target.assessment())));
            toolIcon = ToolIcons.icon(target.assessment().requiredKind(), target.assessment().requiredTier());
        }
        Component text = Component.join(JoinConfiguration.newlines(), lines);
        return new Panel(text, lines.size(), target.icon(), toolIcon);
    }

    public static Panel entity(LivingEntity entity, Settings settings, double shownHealth) {
        List<Component> lines = new ArrayList<>();
        lines.add(line(entityName(entity)));
        lines.add(line(healthLine(entity, shownHealth)));
        for (Component effect : effects(entity, settings.maxEffects())) {
            lines.add(line(effect));
        }
        Component text = Component.join(JoinConfiguration.newlines(), lines);
        return new Panel(text, lines.size(), null, null);
    }

    private static Component blockName(Targets.BlockTarget target) {
        String key = target.material().getBlockTranslationKey();
        Component typeName = key == null
                ? Styles.text(target.material().getKey().getKey(), NamedTextColor.WHITE)
                : Styles.translatable(key, NamedTextColor.WHITE);
        Component custom = target.customName();
        if (custom == null) {
            return typeName;
        }
        Component typeInGray = key == null
                ? Styles.text(target.material().getKey().getKey(), NamedTextColor.DARK_GRAY)
                : Styles.translatable(key, NamedTextColor.DARK_GRAY);
        return custom.decoration(TextDecoration.ITALIC, false)
                .append(Component.text(" ", Styles.plain(NamedTextColor.GRAY)))
                .append(typeInGray);
    }

    private static Component healthLine(LivingEntity entity, double shownHealth) {
        double health = entity.getHealth();
        double maxHealth = maxHealth(entity);
        double absorption = entity.getAbsorptionAmount();
        HealthBar.Segments segments = HealthBar.segments(health, shownHealth, maxHealth, HealthBar.SEGMENTS);
        double ratio = maxHealth <= 0.0D ? 0.0D : Math.max(0.0D, Math.min(1.0D, health / maxHealth));
        NamedTextColor filledColor = ratio > 0.6D ? NamedTextColor.GREEN
                : ratio > 0.3D ? NamedTextColor.YELLOW : NamedTextColor.RED;

        Component bar = Component.empty();
        for (int index = 0; index < segments.filled(); index++) {
            bar = bar.append(Styles.glyph("█", filledColor));
        }
        for (int index = 0; index < segments.trailing(); index++) {
            bar = bar.append(Styles.glyph("▒", NamedTextColor.GOLD));
        }
        for (int index = 0; index < segments.empty(); index++) {
            bar = bar.append(Styles.glyph("░", NamedTextColor.DARK_GRAY));
        }

        Component line = Component.empty()
                .append(Styles.text("❤ ", NamedTextColor.RED))
                .append(bar)
                .append(Component.text(" ", Styles.plain(NamedTextColor.GRAY)))
                .append(Styles.text(NumberText.format(health) + "/" + NumberText.format(maxHealth),
                        NamedTextColor.WHITE));
        if (absorption > 0.05D) {
            line = line.append(Styles.text(" +" + NumberText.format(absorption), NamedTextColor.GOLD));
        }
        return line;
    }

    private static double maxHealth(LivingEntity entity) {
        AttributeInstance attribute = entity.getAttribute(Attribute.MAX_HEALTH);
        if (attribute != null && Double.isFinite(attribute.getValue()) && attribute.getValue() > 0.0D) {
            return attribute.getValue();
        }
        return 20.0D;
    }

    private static Component entityName(LivingEntity entity) {
        if (entity instanceof Player player) {
            return player.displayName();
        }
        Component typeName = Styles.translatable(entity.getType().translationKey(), NamedTextColor.GRAY);
        Component custom = entity.customName();
        if (custom == null) {
            return Styles.translatable(entity.getType().translationKey(), NamedTextColor.WHITE);
        }
        return custom.decoration(TextDecoration.ITALIC, false)
                .append(Component.text(" ", Styles.plain(NamedTextColor.GRAY)))
                .append(typeName);
    }

    private static Component statusLine(MiningAssessment assessment) {
        return switch (assessment.status()) {
            case UNBREAKABLE -> Styles.text("✖ 无法破坏", NamedTextColor.DARK_RED);
            case SPECTATOR -> Styles.text("✖ 旁观模式无法破坏", NamedTextColor.GRAY);
            case CREATIVE -> Styles.text("✔ 创造模式可破坏", NamedTextColor.GREEN);
            case CAN_HARVEST -> Styles.text("✔ 当前可采集", NamedTextColor.GREEN);
            case WRONG_TOOL -> Styles.text("✖ 当前工具不符", NamedTextColor.RED);
            case TOO_WEAK -> Styles.text("✖ 工具等级不足", NamedTextColor.RED);
        };
    }

    private static Component requirementLine(MiningAssessment assessment) {
        ToolKind kind = assessment.requiredKind();
        ToolTier tier = assessment.requiredTier();
        Component needed;
        if (kind == ToolKind.UNKNOWN) {
            needed = Styles.text("正确工具", NamedTextColor.YELLOW);
        } else if (kind == ToolKind.SHEARS || kind == ToolKind.SWORD) {
            needed = Styles.text(kind.label(), NamedTextColor.YELLOW);
        } else if (tier == ToolTier.WOOD) {
            needed = Styles.text("任意" + kind.label(), NamedTextColor.YELLOW);
        } else {
            needed = Styles.text(tier.label() + kind.label() + " 或更好", NamedTextColor.YELLOW);
        }
        return Component.empty()
                .append(Styles.text("需要 ", NamedTextColor.GRAY))
                .append(needed);
    }

    private static List<Component> effects(LivingEntity entity, int maxEffects) {
        List<Component> lines = new ArrayList<>();
        List<PotionEffect> active = new ArrayList<>(entity.getActivePotionEffects());
        active.sort(Comparator
                .comparingInt((PotionEffect effect) -> rank(effect.getType().getEffectCategory()))
                .thenComparing(effect -> effect.getType().translationKey())
                .thenComparing(Comparator.comparingInt(PotionEffect::getAmplifier).reversed()));

        int limit = Math.max(0, maxEffects);
        int shown = 0;
        for (PotionEffect effect : active) {
            if (shown >= limit) {
                break;
            }
            lines.add(effectLine(effect));
            shown++;
        }
        if (active.size() > shown) {
            lines.add(Styles.gray("+" + (active.size() - shown) + " 种状态"));
        }
        if (entity.getFireTicks() > 0) {
            lines.add(Styles.text("● 着火 " + EffectFormat.seconds(entity.getFireTicks()), NamedTextColor.GOLD));
        }
        if (entity.getFreezeTicks() > 0) {
            lines.add(Styles.text("● 冰冻 " + EffectFormat.seconds(entity.getFreezeTicks()), NamedTextColor.AQUA));
        }
        if (lines.isEmpty()) {
            lines.add(Styles.text("● 无状态", NamedTextColor.DARK_GRAY));
        }
        return lines;
    }

    private static Component effectLine(PotionEffect effect) {
        PotionEffectType type = effect.getType();
        Category category = type.getEffectCategory();
        TextColor dotColor = potionColor(type, category);
        NamedTextColor categoryColor = categoryColor(category);
        int duration = effect.isInfinite() ? -1 : effect.getDuration();
        return Component.empty()
                .append(Styles.glyph("●", dotColor))
                .append(Component.text(" ", Styles.plain(NamedTextColor.GRAY)))
                .append(Styles.translatable(type.translationKey(), categoryColor))
                .append(Styles.text(" " + EffectFormat.level(effect.getAmplifier()), NamedTextColor.WHITE))
                .append(Styles.text(" " + EffectFormat.duration(duration), NamedTextColor.GRAY));
    }

    private static TextColor potionColor(PotionEffectType type, Category category) {
        Color color = type.getColor();
        if (color != null && (color.getRed() != 0 || color.getGreen() != 0 || color.getBlue() != 0)) {
            return TextColor.color(color.getRed(), color.getGreen(), color.getBlue());
        }
        TextColor categoryColor = category.getColor();
        return categoryColor != null ? categoryColor : categoryColor(category);
    }

    private static NamedTextColor categoryColor(Category category) {
        return switch (category) {
            case BENEFICIAL -> NamedTextColor.GREEN;
            case HARMFUL -> NamedTextColor.RED;
            case NEUTRAL -> NamedTextColor.GRAY;
        };
    }

    private static int rank(Category category) {
        return switch (category) {
            case HARMFUL -> 0;
            case NEUTRAL -> 1;
            case BENEFICIAL -> 2;
        };
    }

    private static Component line(Component content) {
        return Component.text(LEAD, Styles.plain(NamedTextColor.WHITE)).append(content);
    }
}
