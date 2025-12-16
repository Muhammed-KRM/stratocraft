package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.enums.StructureType;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Structure;
import me.mami.stratocraft.model.player.PlayerData;
import me.mami.stratocraft.model.structure.BaseStructure;
import me.mami.stratocraft.model.structure.ClanStructure;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Yapı Efekt Yönetim Sistemi
 * 
 * Sorumluluklar:
 * - Yapı efektlerini oyunculara uygulama
 * - Oyuncu giriş/çıkışında efekt kontrolü
 * - Periyodik efekt güncelleme
 * - Yapı aktif/pasif durumu kontrolü
 * 
 * Thread-Safe: ConcurrentHashMap kullanır
 */
public class StructureEffectManager {
    
    private final Main plugin;
    private final ClanManager clanManager;
    private final PlayerDataManager playerDataManager;
    private final me.mami.stratocraft.manager.StructureCoreManager structureCoreManager;
    
    // Oyuncu -> Aktif efektler (oyuncu girişinde uygulanan)
    private final Map<UUID, Set<StructureType>> playerActiveEffects = new ConcurrentHashMap<>();
    
    // Tick counter (periyodik efektler için)
    private int tickCounter = 0;
    
    public StructureEffectManager(Main plugin, ClanManager clanManager, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.clanManager = clanManager;
        this.playerDataManager = playerDataManager;
        this.structureCoreManager = plugin.getStructureCoreManager();
    }
    
    /**
     * Oyuncu giriş yaptığında yapı efektlerini uygula
     */
    public void onPlayerJoin(Player player) {
        if (player == null || !player.isOnline()) return;
        
        UUID playerId = player.getUniqueId();
        Clan clan = clanManager.getClanByPlayer(playerId);
        
        if (clan == null) return;
        
        // Klan yapılarını kontrol et (OPTİMİZASYON: Tek döngüde hem efekt uygula hem de kaydet)
        Set<StructureType> activeEffects = new HashSet<>();
        for (Structure structure : clan.getStructures()) {
            if (structure == null) continue;
            
            StructureType type = convertToStructureType(structure.getType());
            if (type == null) continue;
            
            // Yapı aktif mi kontrol et (StructureCoreManager'dan)
            if (structureCoreManager != null && !structureCoreManager.isActiveStructure(structure.getLocation())) {
                continue; // Pasif yapılar efekt vermez
            }
            
            // Efekt tipine göre uygula
            applyEffectOnJoin(player, type, structure.getLevel());
            
            // Aktif efektleri kaydet
            activeEffects.add(type);
        }
        playerActiveEffects.put(playerId, activeEffects);
    }
    
    /**
     * Oyuncu çıkış yaptığında yapı efektlerini kaldır
     */
    public void onPlayerQuit(Player player) {
        if (player == null) return;
        
        UUID playerId = player.getUniqueId();
        playerActiveEffects.remove(playerId);
        
        // Tüm potion efektlerini kaldır (yapı efektleri)
        // Not: Sadece yapı efektlerini kaldırmalı, diğer efektleri korumalı
        // Bu yüzden sadece belirli efektleri kaldırıyoruz
        removeStructureEffects(player);
    }
    
    /**
     * Periyodik efekt güncelleme (StructureEffectTask'tan çağrılacak)
     */
    public void updateEffects() {
        tickCounter++;
        
        // Her klan için yapı efektlerini uygula
        for (Clan clan : clanManager.getAllClans()) {
            for (Structure structure : clan.getStructures()) {
                if (structure == null) continue;
                
                // Yapı aktif mi kontrol et (StructureCoreManager'dan)
                if (structureCoreManager != null && !structureCoreManager.isActiveStructure(structure.getLocation())) {
                    continue; // Pasif yapılar efekt vermez
                }
                
                StructureType type = convertToStructureType(structure.getType());
                if (type == null) continue;
                
                applyPeriodicEffect(clan, structure, type);
            }
        }
    }
    
    /**
     * Oyuncu girişinde efekt uygula
     */
    private void applyEffectOnJoin(Player player, StructureType type, int level) {
        if (player == null || type == null || !player.isOnline()) return;
        
        switch (type) {
            case INVISIBILITY_CLOAK:
                // Görünmezlik (sürekli)
                int duration = 200 + (level * 100); // Lv1: 10s, Lv2: 15s, Lv3: 20s
                player.addPotionEffect(new PotionEffect(
                    PotionEffectType.INVISIBILITY, duration, 0, false, false));
                break;
            case HEALING_BEACON:
                // Regeneration buff
                player.addPotionEffect(new PotionEffect(
                    PotionEffectType.REGENERATION, 200, level - 1, false, false));
                break;
            // Diğer efektler periyodik olarak uygulanacak
            default:
                break;
        }
    }
    
    /**
     * Periyodik efekt uygula
     */
    private void applyPeriodicEffect(Clan clan, Structure structure, StructureType type) {
        if (clan == null || structure == null || type == null) return;
        
        Location loc = structure.getLocation();
        if (loc == null || loc.getWorld() == null) return;
        
        int level = structure.getLevel();
        World world = loc.getWorld();
        
        switch (type) {
            case ALCHEMY_TOWER:
                applyAlchemyTowerEffect(clan, loc, level);
                break;
            case POISON_REACTOR:
                applyPoisonReactorEffect(clan, loc, level);
                break;
            case WATCHTOWER:
                applyWatchtowerEffect(clan, loc, level);
                break;
            case HEALING_BEACON:
                applyHealingBeaconEffect(clan, loc, level);
                break;
            case CROP_ACCELERATOR:
                applyCropAcceleratorEffect(loc, level);
                break;
            case WEATHER_MACHINE:
                applyWeatherMachineEffect(loc, level);
                break;
            case INVISIBILITY_CLOAK:
                applyInvisibilityCloakEffect(clan, loc, level);
                break;
            default:
                break;
        }
    }
    
    // ========== YAPI EFEKTLERİ ==========
    
    /**
     * Simya Kulesi: Yakındaki klan üyelerinin bataryalarını güçlendirir
     */
    private void applyAlchemyTowerEffect(Clan clan, Location loc, int level) {
        // Her 5 saniyede bir partiküller göster
        if (tickCounter % 100 == 0) {
            loc.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE,
                    loc.clone().add(0, 3, 0), 20, 1, 1, 1, 0.1);
        }
        
        // Yakındaki klan üyelerine buff (opsiyonel - zaten BatteryManager yapıyor)
        int radius = 10 + (level * 5); // Lv1: 15, Lv2: 20, Lv3: 25 blok
        
        for (Player player : getNearbyPlayersFromClan(loc, radius, clan)) {
            // Batarya buff'ı BatteryManager'da uygulanıyor
            // Burada sadece görsel efekt
            if (tickCounter % 100 == 0) {
                player.spawnParticle(Particle.VILLAGER_HAPPY,
                        player.getLocation().add(0, 2, 0), 3, 0.3, 0.3, 0.3, 0.01);
            }
        }
    }
    
    /**
     * Zehir Reaktörü: Yakındaki düşmanlara sürekli zehir verir
     */
    private void applyPoisonReactorEffect(Clan clan, Location loc, int level) {
        int radius = 15 + (level * 5); // Lv1: 20, Lv2: 25, Lv3: 30 blok
        World world = loc.getWorld();
        
        // Zehir bulutu partikülleri
        if (tickCounter % 20 == 0) {
            world.spawnParticle(Particle.SPELL_MOB, loc.clone().add(0, 2, 0),
                    30, radius / 2.0, 2, radius / 2.0, 0.01);
        }
        
        // Düşman oyunculara zehir uygula (her 2 saniyede)
        if (tickCounter % 40 == 0) {
            for (Entity entity : world.getNearbyEntities(loc, radius, radius, radius)) {
                if (entity instanceof Player) {
                    Player target = (Player) entity;
                    
                    // Klan üyesi değilse ve misafir de değilse
                    Clan targetClan = clanManager.getClanByPlayer(target.getUniqueId());
                    if (targetClan == null || !targetClan.equals(clan)) {
                        if (!clan.isGuest(target.getUniqueId())) {
                            // Zehir seviyesi: Lv1=I, Lv2=II, Lv3=III
                            int poisonLevel = level;
                            target.addPotionEffect(new PotionEffect(
                                    PotionEffectType.POISON, 60, poisonLevel - 1, false, true));
                            
                            // Level 3'te Slowness da ekle
                            if (level >= 3) {
                                target.addPotionEffect(new PotionEffect(
                                        PotionEffectType.SLOW, 60, 0, false, true));
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Gözetleme Kulesi: Düşman tespiti ve uyarı
     */
    private void applyWatchtowerEffect(Clan clan, Location loc, int level) {
        int radius = 50 + (level * 25); // Lv1: 75, Lv2: 100, Lv3: 125 blok
        
        // Her 10 saniyede bir tarama
        if (tickCounter % 200 == 0) {
            World world = loc.getWorld();
            int enemyCount = 0;
            
            for (Entity entity : world.getNearbyEntities(loc, radius, radius, radius)) {
                if (entity instanceof Player) {
                    Player target = (Player) entity;
                    Clan targetClan = clanManager.getClanByPlayer(target.getUniqueId());
                    
                    // Düşman tespit edildi
                    if (targetClan == null || !targetClan.equals(clan)) {
                        if (!clan.isGuest(target.getUniqueId())) {
                            enemyCount++;
                        }
                    }
                }
            }
            
            // Düşman varsa klan üyelerini uyar
            if (enemyCount > 0) {
                for (UUID memberId : clan.getMembers().keySet()) {
                    Player member = Bukkit.getPlayer(memberId);
                    if (member != null && member.isOnline()) {
                        member.sendMessage("§c⚠ GÖZETLEME KULESİ: " + enemyCount +
                                " düşman tespit edildi! (Menzil: " + radius + " blok)");
                        member.playSound(member.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 0.5f);
                    }
                }
                
                // Uyarı partikülleri
                world.spawnParticle(Particle.REDSTONE, loc.clone().add(0, 10, 0),
                        50, 3, 3, 3, 0.1);
            }
        }
    }
    
    /**
     * Şifa Kulesi: Yakındaki klan üyelerini sürekli iyileştirir
     */
    private void applyHealingBeaconEffect(Clan clan, Location loc, int level) {
        int radius = 10 + (level * 3); // Lv1: 13, Lv2: 16, Lv3: 19 blok
        
        // Şifa partikülleri
        if (tickCounter % 10 == 0) {
            loc.getWorld().spawnParticle(Particle.HEART,
                    loc.clone().add(0, 2, 0), 5, radius / 3.0, 1, radius / 3.0, 0.1);
        }
        
        // Her saniyede şifa (20 tick = 1 saniye)
        if (tickCounter % 20 == 0) {
            double healAmount = 0.5 + (level * 0.5); // Lv1: 1 HP, Lv2: 1.5 HP, Lv3: 2 HP
            
            for (Player player : getNearbyPlayersFromClan(loc, radius, clan)) {
                double maxHealth = player.getMaxHealth();
                double currentHealth = player.getHealth();
                
                if (currentHealth < maxHealth) {
                    player.setHealth(Math.min(maxHealth, currentHealth + healAmount));
                    
                    // Görsel feedback
                    if (tickCounter % 40 == 0) {
                        player.spawnParticle(Particle.HEART,
                                player.getLocation().add(0, 2, 0), 3, 0.3, 0.3, 0.3, 0.01);
                    }
                }
                
                // Regeneration buff
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.REGENERATION, 40, level - 1, false, false));
            }
        }
    }
    
    /**
     * Tarım Hızlandırıcı: Yakındaki bitkileri hızlandırır
     */
    private void applyCropAcceleratorEffect(Location loc, int level) {
        // Her 2 saniyede (CropTask ile senkronize çalışır)
        if (tickCounter % 40 == 0) {
            int radius = 8 + (level * 2); // Lv1: 10, Lv2: 12, Lv3: 14 blok
            
            loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY,
                    loc.clone().add(0, 1, 0), 20, radius / 2.0, 1, radius / 2.0, 0.1);
            
            // Asıl büyüme efekti CropTask'ta uygulanıyor
        }
    }
    
    /**
     * Hava Kontrolcüsü: Hava durumunu değiştirir
     */
    private void applyWeatherMachineEffect(Location loc, int level) {
        // Her 30 saniyede bir (600 tick)
        if (tickCounter % 600 == 0) {
            World world = loc.getWorld();
            
            // Level'e göre hava durumu kontrolü
            if (level >= 2) {
                // Level 2+: Yağmuru durdur
                if (world.hasStorm()) {
                    world.setStorm(false);
                    world.setThundering(false);
                }
            }
            
            // Partikül efekti
            world.spawnParticle(Particle.CLOUD, loc.clone().add(0, 5, 0),
                    30, 2, 2, 2, 0.01);
        }
    }
    
    /**
     * Görünmezlik Perdesi: Yakındaki klan üyelerine görünmezlik
     */
    private void applyInvisibilityCloakEffect(Clan clan, Location loc, int level) {
        int radius = 8 + (level * 2); // Lv1: 10, Lv2: 12, Lv3: 14 blok
        
        // Her saniyede buff ver
        if (tickCounter % 20 == 0) {
            for (Player player : getNearbyPlayersFromClan(loc, radius, clan)) {
                // Görünmezlik süresi: Lv1=10s, Lv2=15s, Lv3=20s
                int duration = 200 + (level * 100);
                
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.INVISIBILITY, duration, 0, false, false));
                
                // Partikül efekti
                if (tickCounter % 60 == 0) {
                    player.spawnParticle(Particle.SMOKE_NORMAL,
                            player.getLocation(), 5, 0.3, 1, 0.3, 0.01);
                }
            }
        }
    }
    
    // ========== YARDIMCI METODLAR ==========
    
    /**
     * Belirli yarıçaptaki klan üyelerini getir
     */
    private Collection<Player> getNearbyPlayersFromClan(Location loc, int radius, Clan clan) {
        return loc.getWorld().getNearbyEntities(loc, radius, radius, radius).stream()
                .filter(e -> e instanceof Player)
                .map(e -> (Player) e)
                .filter(p -> {
                    Clan playerClan = clanManager.getClanByPlayer(p.getUniqueId());
                    return playerClan != null && playerClan.equals(clan);
                })
                .toList();
    }
    
    /**
     * Yapı efektlerini kaldır (oyuncu çıkışında)
     */
    private void removeStructureEffects(Player player) {
        if (player == null) return;
        
        // Sadece belirli efektleri kaldır (yapı efektleri)
        // Diğer efektleri koru
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        // Not: Regeneration gibi efektler zaten süreli, otomatik kalkacak
    }
    
    /**
     * Structure.Type'ı StructureType'a dönüştür
     */
    private StructureType convertToStructureType(Structure.Type oldType) {
        if (oldType == null) return null;
        try {
            return StructureType.valueOf(oldType.name());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Tick counter'ı al (test için)
     */
    public int getTickCounter() {
        return tickCounter;
    }
}

