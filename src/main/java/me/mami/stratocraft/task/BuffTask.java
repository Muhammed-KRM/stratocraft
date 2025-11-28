package me.mami.stratocraft.task;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.manager.SiegeWeaponManager;
import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Structure;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
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
    private final SiegeWeaponManager siegeWeaponManager;
    private final Map<UUID, Long> lastRadarWarnings = new HashMap<>(); // Chat spam önleme

    public BuffTask(TerritoryManager tm, SiegeWeaponManager swm) {
        this.territoryManager = tm;
        this.clanManager = tm.getClanManager();
        this.siegeWeaponManager = swm;
    }
    
    private Long getLastRadarWarning(UUID playerId) {
        return lastRadarWarnings.get(playerId);
    }
    
    private void setLastRadarWarning(UUID playerId, long time) {
        lastRadarWarnings.put(playerId, time);
    }

    @Override
    public void run() {
        // ========== KATEGORİ 2: KLAN ÖZEL YAPILAR (Klan alanı dışında da çalışır) ==========
        if (siegeWeaponManager != null) {
            // 1. CAN TAPINAĞI - İyileştirme
            for (Map.Entry<Location, UUID> entry : siegeWeaponManager.getAllHealingShrines().entrySet()) {
                Location shrineLoc = entry.getKey();
                UUID ownerClanId = entry.getValue();
                
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getLocation().distance(shrineLoc) <= 10) {
                        Clan playerClan = clanManager.getClanByPlayer(p.getUniqueId());
                        if (playerClan != null && playerClan.getId().equals(ownerClanId)) {
                            double maxHealth = p.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
                            if (p.getHealth() < maxHealth) {
                                p.setHealth(Math.min(maxHealth, p.getHealth() + 2.0));
                                p.spawnParticle(org.bukkit.Particle.HEART, p.getLocation().add(0, 2, 0), 1);
                            }
                        }
                    }
                }
            }
            
            // 2. GÜÇ TOTEMİ - Güç buff
            for (Map.Entry<Location, UUID> entry : siegeWeaponManager.getAllPowerTotems().entrySet()) {
                Location totemLoc = entry.getKey();
                UUID ownerClanId = entry.getValue();
                
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getLocation().distance(totemLoc) <= 15) {
                        Clan playerClan = clanManager.getClanByPlayer(p.getUniqueId());
                        if (playerClan != null && playerClan.getId().equals(ownerClanId)) {
                            p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 40, 0, false, false));
                            p.spawnParticle(org.bukkit.Particle.VILLAGER_ANGRY, totemLoc.clone().add(0, 1, 0), 1);
                        }
                    }
                }
            }
            
            // 3. HIZ ÇEMBERİ - Hız buff
            for (Map.Entry<Location, UUID> entry : siegeWeaponManager.getAllSpeedCircles().entrySet()) {
                Location circleLoc = entry.getKey();
                UUID ownerClanId = entry.getValue();
                
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getLocation().distance(circleLoc) <= 12) {
                        Clan playerClan = clanManager.getClanByPlayer(p.getUniqueId());
                        if (playerClan != null && playerClan.getId().equals(ownerClanId)) {
                            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1, false, false));
                            p.spawnParticle(org.bukkit.Particle.CLOUD, circleLoc.clone().add(0, 0.5, 0), 1);
                        }
                    }
                }
            }
            
            // 4. SAVUNMA DUVARI - Direnç buff
            for (Map.Entry<Location, UUID> entry : siegeWeaponManager.getAllDefenseWalls().entrySet()) {
                Location wallLoc = entry.getKey();
                UUID ownerClanId = entry.getValue();
                
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getLocation().distance(wallLoc) <= 12) {
                        Clan playerClan = clanManager.getClanByPlayer(p.getUniqueId());
                        if (playerClan != null && playerClan.getId().equals(ownerClanId)) {
                            p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 0, false, false));
                            p.spawnParticle(org.bukkit.Particle.BLOCK_CRACK, wallLoc.clone().add(0, 0.5, 0), 1, 0.1, 0.1, 0.1, 0.1, org.bukkit.Material.BARRIER.createBlockData());
                        }
                    }
                }
            }
        }
        
        // ========== KLAN ALANI İÇİNDEKİ YAPILAR ==========
        for (Player p : Bukkit.getOnlinePlayers()) {
            Clan territoryClan = territoryManager.getTerritoryOwner(p.getLocation());
            if (territoryClan == null) continue;

            boolean isFriendly = territoryClan.getMembers().containsKey(p.getUniqueId());

            for (Structure s : territoryClan.getStructures()) {
                // HEALING_BEACON (Klan alanı içindeki eski sistem - geriye dönük uyumluluk)
                if (s.getType() == Structure.Type.HEALING_BEACON && isFriendly) {
                    // 10 blok yarıçapında kontrol
                    if (p.getLocation().distance(s.getLocation()) <= 10) {
                        double maxHealth = p.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
                        if (p.getHealth() < maxHealth) {
                            // Saniyede 1 kalp (2 HP) iyileştir
                            p.setHealth(Math.min(maxHealth, p.getHealth() + 2.0));
                            // Partikül efekti
                            p.spawnParticle(org.bukkit.Particle.HEART, p.getLocation().add(0, 2, 0), 1);
                        }
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

