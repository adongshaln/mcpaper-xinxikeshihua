package com.adongcraft.xinxikeshihua;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

final class XinxiCommand implements CommandExecutor, TabCompleter {
    private final XinxiPlugin plugin;

    XinxiCommand(XinxiPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("xinxikeshihua.reload")) {
                sender.sendMessage(Component.text("你没有权限这么做。", NamedTextColor.RED));
                return true;
            }
            plugin.reloadPlugin();
            sender.sendMessage(Component.text("信息可视化配置已重载。", NamedTextColor.GREEN));
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("toggle")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("这个命令只能由玩家使用。", NamedTextColor.RED));
                return true;
            }
            if (!player.hasPermission("xinxikeshihua.use")) {
                player.sendActionBar(Component.text("你没有使用信息可视化的权限", NamedTextColor.RED));
                return true;
            }
            boolean enabled = plugin.hud().toggle(player);
            player.sendActionBar(Component.text(enabled ? "信息可视化已开启" : "信息可视化已关闭",
                    enabled ? NamedTextColor.GREEN : NamedTextColor.GRAY));
            return true;
        }
        sender.sendMessage(Component.text("/" + label + " toggle：打开或关闭 HUD", NamedTextColor.GRAY));
        if (sender.hasPermission("xinxikeshihua.reload")) {
            sender.sendMessage(Component.text("/" + label + " reload：重载配置", NamedTextColor.GRAY));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) {
            return List.of();
        }
        String prefix = args[0].toLowerCase(Locale.ROOT);
        List<String> options = sender.hasPermission("xinxikeshihua.reload")
                ? List.of("toggle", "reload")
                : List.of("toggle");
        return options.stream()
                .filter(option -> option.startsWith(prefix))
                .toList();
    }
}
