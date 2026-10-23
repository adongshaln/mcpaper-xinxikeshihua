package com.adongcraft.xinxikeshihua;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** 给启动器悬浮窗用的只读 HTTP 接口。 */
final class OverlayServer {
    private final XinxiPlugin plugin;
    private final OverlayFeed feed;
    private HttpServer server;
    private ExecutorService executor;

    OverlayServer(XinxiPlugin plugin, OverlayFeed feed) {
        this.plugin = plugin;
        this.feed = feed;
    }

    void start() {
        Settings settings = plugin.settings();
        try {
            server = HttpServer.create(new InetSocketAddress(settings.overlayBind(), settings.overlayPort()), 0);
            server.createContext("/hud", this::hud);
            server.createContext("/health", this::health);
            executor = Executors.newSingleThreadExecutor(runnable -> {
                Thread thread = new Thread(runnable, "mcpaper-xinxikeshihua-overlay");
                thread.setDaemon(true);
                return thread;
            });
            server.setExecutor(executor);
            server.start();
            plugin.getLogger().info("悬浮窗数据接口已启动：http://" + settings.overlayBind() + ":"
                    + settings.overlayPort() + "/hud");
            if (settings.overlayToken().isBlank() || settings.overlayToken().equals("change-me")) {
                plugin.getLogger().warning("overlay.token 仍是默认值，请改成和启动器一致的随机字符串");
            }
        } catch (IOException | RuntimeException exception) {
            plugin.getLogger().warning("悬浮窗数据接口启动失败：" + exception.getMessage());
            stop();
        }
    }

    void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    private void health(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, 405, "{\"error\":\"method\"}");
            return;
        }
        send(exchange, 200, "{\"ok\":true}");
    }

    private void hud(HttpExchange exchange) throws IOException {
        try {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "{\"error\":\"method\"}");
                return;
            }
            if (!tokenMatches(plugin.settings().overlayToken(), token(exchange))) {
                send(exchange, 401, "{\"error\":\"token\"}");
                return;
            }
            Map<String, String> query = query(exchange.getRequestURI().getRawQuery());
            String playerName = query.get("player");
            long since = parseLong(query.get("since"));
            Player player = playerName == null ? null : Bukkit.getPlayerExact(playerName);
            String body = player == null ? "{\"online\":false}" : feed.response(player, since);
            send(exchange, 200, body);
        } catch (RuntimeException exception) {
            plugin.getLogger().warning("悬浮窗请求失败：" + exception.getMessage());
            send(exchange, 500, "{\"error\":\"internal\"}");
        }
    }

    private static String token(HttpExchange exchange) {
        String direct = exchange.getRequestHeaders().getFirst("X-Adongcraft-Token");
        if (direct != null && !direct.isBlank()) {
            return direct;
        }
        String authorization = exchange.getRequestHeaders().getFirst("Authorization");
        if (authorization != null && authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return authorization.substring(7).trim();
        }
        return null;
    }

    private static boolean tokenMatches(String expected, String provided) {
        if (expected == null || expected.isBlank() || provided == null) {
            return false;
        }
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                provided.getBytes(StandardCharsets.UTF_8));
    }

    private static Map<String, String> query(String rawQuery) {
        Map<String, String> values = new HashMap<>();
        if (rawQuery == null || rawQuery.isEmpty()) {
            return values;
        }
        for (String part : rawQuery.split("&")) {
            int equals = part.indexOf('=');
            if (equals <= 0) {
                continue;
            }
            String key = URLDecoder.decode(part.substring(0, equals), StandardCharsets.UTF_8);
            String value = URLDecoder.decode(part.substring(equals + 1), StandardCharsets.UTF_8);
            values.put(key, value);
        }
        return values;
    }

    private static long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return 0L;
        }
        try {
            return Math.max(0L, Long.parseLong(value));
        } catch (NumberFormatException exception) {
            return 0L;
        }
    }

    private static void send(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }
}
