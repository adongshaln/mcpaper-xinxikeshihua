package com.adongcraft.xinxikeshihua;

import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

/**
 * 单个玩家的 HUD：一块文字 + 两个物品图标。
 * 三个实体都只对本人可见，位置每刻贴在镜头前方。
 */
final class PlayerHud {
    private final XinxiPlugin plugin;
    private final Player player;
    private final HudState state = new HudState();

    private TextDisplay text;
    private ItemDisplay blockIcon;
    private ItemDisplay toolIcon;

    private boolean textVisible;
    private boolean blockVisible;
    private boolean toolVisible;

    private Component lastText;
    private Material lastBlockMaterial;
    private Material lastToolMaterial;
    private double textScale = -1.0D;
    private double iconScale = -1.0D;
    private int backgroundOpacity = -1;

    PlayerHud(XinxiPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    void create() {
        Settings settings = plugin.settings();
        Location at = player.getEyeLocation();
        text = spawnText(at, settings);
        blockIcon = spawnIcon(at, settings);
        toolIcon = spawnIcon(at, settings);
        applySettings(settings);
    }

    boolean valid() {
        return text != null && text.isValid() && player.isOnline();
    }

    void render(Targets target, Settings settings) {
        if (text == null) {
            return;
        }
        HudText.Panel panel;
        if (target instanceof Targets.BlockTarget block) {
            state.reset();
            panel = HudText.block(block, settings);
        } else if (target instanceof Targets.EntityTarget entityTarget) {
            double shown = state.advance(entityTarget.entity());
            panel = HudText.entity(entityTarget.entity(), settings, shown);
        } else {
            return;
        }

        setText(panel.text());
        if (settings.showBlockIcon() && panel.blockIcon() != null) {
            setBlockIcon(panel.blockIcon());
        } else {
            hideBlockIcon();
        }
        if (settings.showToolIcon() && panel.toolIcon() != null) {
            setToolIcon(panel.toolIcon());
        } else {
            hideToolIcon();
        }
        position(settings, panel.lines());
    }

    void applySettings(Settings settings) {
        if (text == null) {
            return;
        }
        if (Math.abs(settings.hudScale() - textScale) > 1.0E-6D) {
            text.setTransformation(scale(settings.hudScale()));
            textScale = settings.hudScale();
        }
        if (settings.backgroundOpacity() != backgroundOpacity) {
            text.setBackgroundColor(Color.fromARGB(settings.backgroundOpacity(), 8, 10, 16));
            backgroundOpacity = settings.backgroundOpacity();
        }
    }

    void hide() {
        hideText();
        hideBlockIcon();
        hideToolIcon();
        state.reset();
    }

    void remove() {
        if (text != null && text.isValid()) {
            text.remove();
        }
        if (blockIcon != null && blockIcon.isValid()) {
            blockIcon.remove();
        }
        if (toolIcon != null && toolIcon.isValid()) {
            toolIcon.remove();
        }
        text = null;
        blockIcon = null;
        toolIcon = null;
        textVisible = false;
        blockVisible = false;
        toolVisible = false;
    }

    private void setText(Component component) {
        if (text == null || !text.isValid()) {
            return;
        }
        if (lastText == null || !lastText.equals(component)) {
            text.text(component);
            lastText = component;
        }
        showText();
    }

    private void setBlockIcon(Material material) {
        if (blockIcon == null) {
            return;
        }
        if (material != lastBlockMaterial) {
            blockIcon.setItemStack(new ItemStack(material));
            lastBlockMaterial = material;
        }
        showBlockIcon();
    }

    private void setToolIcon(Material material) {
        if (toolIcon == null) {
            return;
        }
        if (material != lastToolMaterial) {
            toolIcon.setItemStack(new ItemStack(material));
            lastToolMaterial = material;
        }
        showToolIcon();
    }

    private void position(Settings settings, int lineCount) {
        HudLayout.Layout layout = HudLayout.compute(player.getEyeLocation(), settings, lineCount,
                blockVisible, toolVisible);
        teleport(text, layout.text());
        if (blockVisible && layout.blockIcon() != null) {
            teleport(blockIcon, layout.blockIcon());
        }
        if (toolVisible && layout.toolIcon() != null) {
            teleport(toolIcon, layout.toolIcon());
        }
        setIconScale(layout.iconScale());
    }

    private void setIconScale(double scale) {
        if (Math.abs(scale - iconScale) <= 1.0E-6D) {
            return;
        }
        if (blockIcon != null) {
            blockIcon.setTransformation(scale(scale));
        }
        if (toolIcon != null) {
            toolIcon.setTransformation(scale(scale));
        }
        iconScale = scale;
    }

    private void teleport(Entity entity, CameraMath.Point point) {
        if (entity == null || !entity.isValid()) {
            return;
        }
        entity.teleport(new Location(player.getWorld(), point.x(), point.y(), point.z()));
    }

    private void showText() {
        if (text != null && text.isValid() && !textVisible) {
            player.showEntity(plugin, text);
            textVisible = true;
        }
    }

    private void hideText() {
        if (text != null && text.isValid() && textVisible) {
            player.hideEntity(plugin, text);
            textVisible = false;
        }
    }

    private void showBlockIcon() {
        if (blockIcon != null && blockIcon.isValid() && !blockVisible) {
            player.showEntity(plugin, blockIcon);
            blockVisible = true;
        }
    }

    private void hideBlockIcon() {
        if (blockIcon != null && blockIcon.isValid() && blockVisible) {
            player.hideEntity(plugin, blockIcon);
            blockVisible = false;
        }
    }

    private void showToolIcon() {
        if (toolIcon != null && toolIcon.isValid() && !toolVisible) {
            player.showEntity(plugin, toolIcon);
            toolVisible = true;
        }
    }

    private void hideToolIcon() {
        if (toolIcon != null && toolIcon.isValid() && toolVisible) {
            player.hideEntity(plugin, toolIcon);
            toolVisible = false;
        }
    }

    private TextDisplay spawnText(Location at, Settings settings) {
        World world = at.getWorld();
        if (world == null) {
            return null;
        }
        try {
            TextDisplay display = world.spawn(at, TextDisplay.class, entity -> {
                entity.setVisibleByDefault(false);
                entity.setPersistent(false);
                entity.setGravity(false);
                entity.setBillboard(Display.Billboard.CENTER);
                entity.setAlignment(TextDisplay.TextAlignment.LEFT);
                entity.setSeeThrough(true);
                entity.setShadowed(true);
                entity.setDefaultBackground(false);
                entity.setBackgroundColor(Color.fromARGB(settings.backgroundOpacity(), 8, 10, 16));
                entity.setBrightness(new Display.Brightness(15, 15));
                entity.setViewRange(2.0F);
                entity.setLineWidth(1000);
                entity.setTextOpacity((byte) 255);
                entity.setTransformation(scale(settings.hudScale()));
                entity.setDisplayWidth(3.0F);
                entity.setDisplayHeight(3.0F);
                entity.addScoreboardTag(Tags.HUD);
                entity.text(Component.empty());
            });
            textScale = settings.hudScale();
            backgroundOpacity = settings.backgroundOpacity();
            return display;
        } catch (RuntimeException exception) {
            plugin.getLogger().warning("无法创建 HUD 文字：" + exception.getMessage());
            return null;
        }
    }

    private ItemDisplay spawnIcon(Location at, Settings settings) {
        World world = at.getWorld();
        if (world == null) {
            return null;
        }
        try {
            return world.spawn(at, ItemDisplay.class, entity -> {
                entity.setVisibleByDefault(false);
                entity.setPersistent(false);
                entity.setGravity(false);
                entity.setBillboard(Display.Billboard.CENTER);
                entity.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
                entity.setBrightness(new Display.Brightness(15, 15));
                entity.setViewRange(2.0F);
                entity.setTransformation(scale(0.03D));
                entity.setDisplayWidth(1.0F);
                entity.setDisplayHeight(1.0F);
                entity.addScoreboardTag(Tags.HUD);
                entity.setItemStack(new ItemStack(Material.AIR));
            });
        } catch (RuntimeException exception) {
            plugin.getLogger().warning("无法创建 HUD 图标：" + exception.getMessage());
            return null;
        }
    }

    private static Transformation scale(double value) {
        return new Transformation(
                new Vector3f(0.0F, 0.0F, 0.0F),
                new AxisAngle4f(0.0F, 0.0F, 1.0F, 0.0F),
                new Vector3f((float) value, (float) value, (float) value),
                new AxisAngle4f(0.0F, 0.0F, 1.0F, 0.0F)
        );
    }
}
