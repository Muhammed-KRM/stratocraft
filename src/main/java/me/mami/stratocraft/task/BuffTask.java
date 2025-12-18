package me.mami.stratocraft.task;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.manager.SiegeWeaponManager;
import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Structure;

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

    // OPTİMİZASYON: Tick sayacı (her şeyi her tick'te çalıştırma)
    private int tickCounter = 0;
    
    @Override
    public void run() {
        tickCounter++;
        
        // Online oyuncu yoksa hiçbir şey yapma
        if (Bukkit.getOnlinePlayers().isEmpty()) return;
        
        // ========== KATEGORİ 2: KLAN ÖZEL YAPILAR ==========
        // OPTİMİZE: Her 2 tick'te bir çalış (saniyede 10 kez yerine 5 kez)
        if (tickCounter % 2 == 0 && siegeWeaponManager != null) {
            processSpecialStructures();
        }
        
        // ========== KLAN ALANI İÇİNDEKİ YAPILAR ==========
        // ✅ OPTİMİZE: Her 5 tick'te bir çalış (performans için)
        if (tickCounter % 5 == 0) {
            processTerritoryStructures();
        }
    }
    
    /**
     * OPTİMİZE: Özel yapıları işle (şifa tapınağı, güç totemi vb.)
     */
    private void processSpecialStructures() {
        // Oyuncu listesini bir kez al
        var players = Bukkit.getOnlinePlayers();
        if (players.isEmpty()) return;
        
        // Oyuncu -> Klan cache'i (aynı oyuncu için tekrar sorgu yapma)
        Map<UUID, Clan> playerClanCache = new HashMap<>();
        
        for (Player p : players) {
            if (p == null || !p.isOnline()) continue;
            
            UUID playerId = p.getUniqueId();
            Clan playerClan = playerClanCache.computeIfAbsent(playerId, 
                id -> clanManager.getClanByPlayer(id));
            if (playerClan == null) continue;
            
            Location playerLoc = p.getLocation();
            UUID clanId = playerClan.getId();
            
            // 1. CAN TAPINAĞI
            checkHealingShrine(p, playerLoc, clanId);
            
            // 2. GÜÇ TOTEMİ
            checkPowerTotem(p, playerLoc, clanId);
            
            // 3. HIZ ÇEMBERİ
            checkSpeedCircle(p, playerLoc, clanId);
            
            // 4. SAVUNMA DUVARI
            checkDefenseWall(p, playerLoc, clanId);
        }
    }
    
    // OPTİMİZE: distanceSquared kullan (Math.sqrt pahalı)
    private void checkHealingShrine(Player p, Location playerLoc, UUID clanId) {
        for (Map.Entry<Location, UUID> entry : siegeWeaponManager.getAllHealingShrines().entrySet()) {
            if (!entry.getValue().equals(clanId)) continue;
            Location shrineLoc = entry.getKey();
            if (!shrineLoc.getWorld().equals(playerLoc.getWorld())) continue;
            if (playerLoc.distanceSquared(shrineLoc) > 100) continue; // 10^2 = 100
            
            double maxHealth = p.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
            if (p.getHealth() < maxHealth) {
                p.setHealth(Math.min(maxHealth, p.getHealth() + 2.0));
                p.spawnParticle(org.bukkit.Particle.HEART, p.getLocation().add(0, 2, 0), 1);
            }
            break;
        }
    }
    
    private void checkPowerTotem(Player p, Location playerLoc, UUID clanId) {
        for (Map.Entry<Location, UUID> entry : siegeWeaponManager.getAllPowerTotems().entrySet()) {
            if (!entry.getValue().equals(clanId)) continue;
            Location totemLoc = entry.getKey();
            if (!totemLoc.getWorld().equals(playerLoc.getWorld())) continue;
            if (playerLoc.distanceSquared(totemLoc) > 225) continue; // 15^2
            
            p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 40, 0, false, false));
            break;
        }
    }
    
    private void checkSpeedCircle(Player p, Location playerLoc, UUID clanId) {
        for (Map.Entry<Location, UUID> entry : siegeWeaponManager.getAllSpeedCircles().entrySet()) {
            if (!entry.getValue().equals(clanId)) continue;
            Location circleLoc = entry.getKey();
            if (!circleLoc.getWorld().equals(playerLoc.getWorld())) continue;
            if (playerLoc.distanceSquared(circleLoc) > 144) continue; // 12^2
            
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1, false, false));
            break;
        }
    }
    
    private void checkDefenseWall(Player p, Location playerLoc, UUID clanId) {
        for (Map.Entry<Location, UUID> entry : siegeWeaponManager.getAllDefenseWalls().entrySet()) {
            if (!entry.getValue().equals(clanId)) continue;
            Location wallLoc = entry.getKey();
            if (!wallLoc.getWorld().equals(playerLoc.getWorld())) continue;
            if (playerLoc.distanceSquared(wallLoc) > 144) continue; // 12^2
            
            p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 0, false, false));
            break;
        }
    }
    
    /**
     * ✅ OPTİMİZE: Klan alanı yapılarını işle (performans için optimize edildi)
     */
    private void processTerritoryStructures() {
        // ✅ OPTİMİZE: Oyuncu listesini bir kez al
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        if (onlinePlayers.isEmpty()) return;
        
        for (Player p : onlinePlayers) {
            if (p == null || !p.isOnline()) continue;
            
            Location playerLoc = p.getLocation();
            if (playerLoc == null || playerLoc.getWorld() == null) continue;
            
            Clan territoryClan = territoryManager.getTerritoryOwner(playerLoc);
            if (territoryClan == null) continue;

            boolean isFriendly = territoryClan.getMembers().containsKey(p.getUniqueId());
            
            // ✅ OPTİMİZE: Sadece yakındaki yapıları kontrol et (mesafe kontrolü önce)
            for (Structure s : territoryClan.getStructures()) {
                Location structLoc = s.getLocation();
                if (structLoc == null || structLoc.getWorld() == null) continue;
                if (!structLoc.getWorld().equals(playerLoc.getWorld())) continue;
                
                // ✅ OPTİMİZE: Mesafe kontrolü önce (uzaktaki yapıları atla)
                double distanceSquared = playerLoc.distanceSquared(structLoc);
                if (distanceSquared > 400) continue; // 20 bloktan uzaktaki yapıları atla
                
                Structure.Type type = s.getType();
                
                // HEALING_BEACON
                if (type == Structure.Type.HEALING_BEACON && isFriendly) {
                    if (distanceSquared <= 100) { // 10^2
                        double maxHealth = p.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
                        if (p.getHealth() < maxHealth) {
                            p.setHealth(Math.min(maxHealth, p.getHealth() + 2.0));
                        }
                    }
                }

                // GRAVITY_WELL
                else if (type == Structure.Type.GRAVITY_WELL && !isFriendly && p.getGameMode() != GameMode.CREATIVE) {
                    if (distanceSquared <= 100) { // Sadece yakındayken etkili
                        p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 40, 200));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1));
                    }
                }

                // POISON_REACTOR
                else if (type == Structure.Type.POISON_REACTOR && !isFriendly && p.getGameMode() != GameMode.CREATIVE) {
                    if (distanceSquared <= 100) { // Sadece yakındayken etkili
                        int level = s.getLevel();
                        p.damage(level);
                        p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 0));
                        if (level >= 3) {
                            p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
                            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1));
                        }
                    }
                }

                // WATCHTOWER - ✅ OPTİMİZE: Her 20 tick'te bir çalış (performans için)
                else if (type == Structure.Type.WATCHTOWER && isFriendly && tickCounter % 20 == 0) {
                    processWatchtower(p, territoryClan, s);
                }
                
                // MOB_GRINDER - ✅ OPTİMİZE: Her 10 tick'te bir çalış (performans için)
                else if (type == Structure.Type.MOB_GRINDER && isFriendly && tickCounter % 10 == 0) {
                    processMobGrinder(s);
                }

                // CORE - ✅ OPTİMİZE: Her 40 tick'te bir çalış (performans için)
                else if (type == Structure.Type.CORE && tickCounter % 40 == 0) {
                    boolean anyOnline = territoryClan.getMembers().keySet().stream()
                            .anyMatch(uuid -> Bukkit.getPlayer(uuid) != null);
                    if (!anyOnline) {
                        s.consumeFuel();
                    }
                }
            }
        }
    }
    
    private void processWatchtower(Player p, Clan territoryClan, Structure s) {
        UUID playerId = p.getUniqueId();
        Long lastWarning = getLastRadarWarning(playerId);
        long currentTime = System.currentTimeMillis();
        
        if (lastWarning != null && (currentTime - lastWarning) < 10000) return;
        
        Location structLoc = s.getLocation();
        // ✅ OPTİMİZE: Sadece aynı dünyadaki ve yakındaki oyuncuları kontrol et
        Collection<? extends Player> nearbyPlayers = structLoc.getWorld().getPlayers();
        for (Player nearby : nearbyPlayers) {
            if (nearby == null || !nearby.isOnline()) continue;
            if (territoryClan.getMembers().containsKey(nearby.getUniqueId())) continue;
            if (nearby.getLocation().distanceSquared(structLoc) > 40000) continue; // 200^2
            
            int distance = (int) nearby.getLocation().distance(structLoc);
            p.sendMessage("§c§l[RADAR] §7Düşman: §c" + nearby.getName() + " §7(" + distance + " blok)");
            setLastRadarWarning(playerId, currentTime);
            break;
        }
    }
    
    private void processMobGrinder(Structure s) {
        Location loc = s.getLocation();
        for (org.bukkit.entity.Entity entity : loc.getWorld().getNearbyEntities(loc, 10, 10, 10)) {
            if (entity instanceof org.bukkit.entity.LivingEntity && 
                !(entity instanceof Player) && 
                !(entity instanceof org.bukkit.entity.Villager)) {
                ((org.bukkit.entity.LivingEntity) entity).damage(2.0);
            }
        }
    }
}

