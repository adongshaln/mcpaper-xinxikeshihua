package com.adongcraft.xinxikeshihua;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import java.io.*;
import java.util.*;

/** Sends only the observing player's current target through their game connection. */
final class NativeHud implements Listener {
    private final XinxiPlugin plugin;
    private final Set<UUID> clients=new HashSet<>();
    private final Map<UUID,byte[]> previous=new HashMap<>();
    private BukkitTask task;private int tick;
    NativeHud(XinxiPlugin plugin){this.plugin=plugin;}
    void start() {
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin,"xinxikeshihua:hud");
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin,"xinxikeshihua:hello",(channel,player,bytes)->{
            if(bytes.length==1 && bytes[0]==1) {clients.add(player.getUniqueId());previous.remove(player.getUniqueId());}
        });
        task=Bukkit.getScheduler().runTaskTimer(plugin,this::tick,2,2);
    }
    private void tick() {
        tick+=2;
        for(UUID id:List.copyOf(clients)) {
            Player player=Bukkit.getPlayer(id);if(player==null) {clients.remove(id);previous.remove(id);continue;}
            Targets target=plugin.settings().enabled() && plugin.hud().allowed(player) && !player.isDead()?TargetScanner.scan(player,plugin.settings()):null;
            byte[] bytes=encode(target);
            if(tick%20==0 || !Arrays.equals(bytes,previous.get(id))) {player.sendPluginMessage(plugin,"xinxikeshihua:hud",bytes);previous.put(id,bytes);}
        }
    }
    private byte[] encode(Targets target) {
        try {
            var bytes=new ByteArrayOutputStream();var out=new DataOutputStream(bytes);out.writeByte(1);
            if(target instanceof Targets.BlockTarget block) {
                out.writeByte(1);out.writeUTF(block.material().getBlockTranslationKey());out.writeUTF(plain(block.customName()));
                out.writeUTF((block.icon()==null?block.material():block.icon()).getKey().toString());
                out.writeUTF(block.assessment().status().name());
                var a=block.assessment();out.writeUTF(a.hasRequirement()?ToolIcons.icon(a.requiredKind(),a.requiredTier()).getItemTranslationKey():"");
            } else if(target instanceof Targets.EntityTarget value) {
                LivingEntity entity=value.entity();out.writeByte(2);out.writeUTF(entity.getType().translationKey());
                out.writeUTF(plain(entity instanceof Player p?p.displayName():entity.customName()));
                out.writeFloat((float)entity.getHealth());var maximum=entity.getAttribute(Attribute.MAX_HEALTH);
                out.writeFloat((float)(maximum==null?20:maximum.getValue()));out.writeFloat((float)entity.getAbsorptionAmount());
                var effects=entity.getActivePotionEffects().stream().sorted(Comparator.comparingInt(e->e.getType().getEffectCategory()==org.bukkit.potion.PotionEffectType.Category.HARMFUL?0:1))
                        .limit(Math.min(4,plugin.settings().maxEffects())).toList();
                out.writeByte(effects.size());for(var effect:effects) {out.writeUTF(effect.getType().translationKey());out.writeInt(effect.getAmplifier());out.writeInt(effect.isInfinite()?-1:effect.getDuration());}
            } else out.writeByte(0);
            return bytes.toByteArray();
        } catch(IOException impossible) {throw new IllegalStateException(impossible);}
    }
    private static String plain(Component text) {
        if(text==null)return "";String value=PlainTextComponentSerializer.plainText().serialize(text);return value.substring(0,Math.min(128,value.length()));
    }
    @EventHandler public void onQuit(PlayerQuitEvent event) {clients.remove(event.getPlayer().getUniqueId());previous.remove(event.getPlayer().getUniqueId());}
    void stop() {
        if(task!=null)task.cancel();clients.clear();previous.clear();
        Bukkit.getMessenger().unregisterIncomingPluginChannel(plugin,"xinxikeshihua:hello");
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(plugin,"xinxikeshihua:hud");
    }
}
