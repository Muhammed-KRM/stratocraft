package me.mami.stratocraft.task;

import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Structure;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BuffTask extends BukkitRunnable {
    private final TerritoryManager territoryManager;
    private final ClanManager clanManager;
    private final Map<UUID, Long> lastRadarWarnings = new HashMap<>(); // Chat spam önleme

    public BuffTask(TerritoryManager tm) {
        this.territoryManager = tm;
        this.clanManager = tm.getClanManager();
    }
    
    private Long getLastRadarWarning(UUID playerId) {
        return lastRadarWarnings.get(playerId);
    }
    
    private void setLastRadarWarning(UUID playerId, long time) {
        lastRadarWarnings.put(playerId, time);
    }

    @Override
    public void run() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            Clan territoryClan = territoryManager.getTerritoryOwner(p.getLocation());
            if (territoryClan == null) continue;

            boolean isFriendly = territoryClan.getMembers().containsKey(p.getUniqueId());

            for (Structure s : territoryClan.getStructures()) {
                if (s.getType() == Structure.Type.HEALING_BEACON && isFriendly) {
                    if (p.getHealth() < p.getMaxHealth()) {
                        p.setHealth(Math.min(p.getHealth() + 0.5, p.getMaxHealth()));
                    }
                }

                if (s.getType() == Structure.Type.GRAVITY_WELL && !isFriendly && p.getGameMode() != GameMode.CREATIVE) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 40, 200));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1));
                }

                // ZEHİR REAKTÖRÜ - Düşmanlara zehir ver (Seviye bazlı hasar)
                if (s.getType() == Structure.Type.POISON_REACTOR && !isFriendly && p.getGameMode() != GameMode.CREATIVE) {
                    int level = s.getLevel();
                    int damage = level; // Seviye 1 = 1 hasar, Seviye 5 = 5 hasar
                    p.damage(damage);
                    p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 0));
                    if (level >= 3) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1));
                    }
                }

                // ERKEN UYARI RADARI - Düşman yaklaşımı uyarısı (Geliştirilmiş: Koordinat bilgisi + Chat Spam Önleme)
                if (s.getType() == Structure.Type.WATCHTOWER && isFriendly) {
                    // 200 blok yarıçapında düşman kontrolü
                    // Chat spam önleme: Her oyuncu için son uyarı zamanını tut
                    UUID playerId = p.getUniqueId();
                    Long lastRadarWarning = getLastRadarWarning(playerId);
                    long currentTime = System.currentTimeMillis();
                    
                    // 10 saniyede bir uyarı gönder (chat spam önleme)
                    if (lastRadarWarning == null || (currentTime - lastRadarWarning) >= 10000) {
                        for (Player nearby : Bukkit.getOnlinePlayers()) {
                            if (nearby.getLocation().distance(s.getLocation()) <= 200 && 
                                !territoryClan.getMembers().containsKey(nearby.getUniqueId())) {
                                org.bukkit.Location enemyLoc = nearby.getLocation();
                                int distance = (int) enemyLoc.distance(s.getLocation());
                                
                                // ActionBar kullan (chat spam önleme)
                                try {
                                    p.spigot().sendMessage(
                                        net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                                        new net.md_5.bungee.api.chat.TextComponent(
                                            "§c§l[RADAR] §7" + nearby.getName() + " - " + distance + " blok"
                                        )
                                    );
                                } catch (Exception e) {
                                    // Spigot API yoksa normal mesaj gönder
                                    p.sendMessage("§c§l[RADAR] §7" + nearby.getName() + " - " + distance + " blok");
                                }
                                
                                // Chat'e sadece önemli bilgileri yaz (10 saniyede bir)
                                p.sendMessage("§c§l[RADAR] §7Düşman: §c" + nearby.getName() + 
                                    " §7(" + distance + " blok) - X:" + (int)enemyLoc.getX() + 
                                    " Y:" + (int)enemyLoc.getY() + " Z:" + (int)enemyLoc.getZ());
                                
                                setLastRadarWarning(playerId, currentTime);
                                break; // Bir düşman bulundu, döngüyü kır
                            }
                        }
                    }
                }
                
                // MOB ÖĞÜTÜCÜ - Etraftaki moblara hasar ver
                if (s.getType() == Structure.Type.MOB_GRINDER && isFriendly) {
                    int radius = 10;
                    for (org.bukkit.entity.Entity entity : s.getLocation().getWorld()
                            .getNearbyEntities(s.getLocation(), radius, radius, radius)) {
                        if (entity instanceof org.bukkit.entity.LivingEntity && 
                            !(entity instanceof Player) && 
                            !(entity instanceof org.bukkit.entity.Villager)) {
                            org.bukkit.entity.LivingEntity mob = (org.bukkit.entity.LivingEntity) entity;
                            mob.damage(2.0); // Her saniye 2 hasar
                            s.getLocation().getWorld().spawnParticle(
                                org.bukkit.Particle.DAMAGE_INDICATOR, 
                                mob.getLocation().add(0, 1, 0), 
                                3
                            );
                        }
                    }
                }

                if (s.getType() == Structure.Type.CORE) {
                    boolean anyOnline = clanManager.getAllClans().stream()
                            .filter(clan -> clan.equals(territoryClan))
                            .flatMap(clan -> clan.getMembers().keySet().stream())
                            .anyMatch(uuid -> Bukkit.getPlayer(uuid) != null);

                    if (!anyOnline) {
                        s.consumeFuel();
                    }
                }
            }
        }
    }
}

