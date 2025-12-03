package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Canlı Eğitme Sistemi
 * 
 * - Her canlı eğitilebilir
 * - Seviyeye göre ritüel (zorluk seviyesine göre)
 * - Her boss için ayrı eğitme ritüeli
 * - Binilebilirlik kontrolü
 */
public class TamingManager {
    private final Main plugin;
    private final DifficultyManager difficultyManager;
    private final BossManager bossManager;
    
    // Eğitilmiş canlılar (Entity UUID -> Owner UUID)
    private final Map<UUID, UUID> tamedCreatures = new HashMap<>();
    
    // Takip edilecek kişiler (Entity UUID -> Player UUID) - Shift+sağ tık ile belirlenir
    private final Map<UUID, UUID> followingTargets = new HashMap<>();
    
    // Dişi/Erkek bilgisi (Entity UUID -> Gender)
    private final Map<UUID, Gender> creatureGenders = new HashMap<>();
    
    // Ritüel cooldown (Location -> Long)
    private final Map<Location, Long> ritualCooldowns = new HashMap<>();
    private static final long RITUAL_COOLDOWN = 30000L; // 30 saniye
    
    /**
     * Cinsiyet enum
     */
    public enum Gender {
        MALE,   // Erkek
        FEMALE  // Dişi
    }
    
    private File tamedFile;
    private FileConfiguration tamedConfig;
    
    /**
     * Binilebilir canlı tipleri
     */
    public enum RideableType {
        DRAGON,         // Ejderha - Binilebilir
        TREX,           // T-Rex - Binilebilir
        GRIFFIN,        // Griffin - Binilebilir
        WAR_BEAR,       // Savaş Ayısı - Binilebilir
        PHOENIX,        // Phoenix - Binilebilir
        WYVERN,         // Wyvern - Binilebilir
        HELL_DRAGON,    // Cehennem Ejderi - Binilebilir
        HYDRA,          // Hydra - Binilebilir
        CHAOS_GOD       // Khaos Tanrısı - Binilebilir
    }
    
    /**
     * Binilebilir mi kontrol et
     */
    public boolean isRideable(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        
        String customName = entity.getCustomName();
        if (customName == null) {
            return false;
        }
        
        // Binilebilir canlı isimleri
        if (customName.contains("EJDERHA") || customName.contains("Ejderha") || customName.contains("DRAGON")) {
            return true;
        }
        if (customName.contains("T-REX") || customName.contains("T-Rex")) {
            return true;
        }
        if (customName.contains("Griffin") || customName.contains("GRIFFIN")) {
            return true;
        }
        if (customName.contains("Savaş Ayısı") || customName.contains("WAR_BEAR")) {
            return true;
        }
        if (customName.contains("Phoenix") || customName.contains("PHOENIX")) {
            return true;
        }
        if (customName.contains("Wyvern") || customName.contains("WYVERN")) {
            return true;
        }
        if (customName.contains("CEHENNEM EJDERİ") || customName.contains("Cehennem Ejderi")) {
            return true;
        }
        if (customName.contains("HYDRA") || customName.contains("Hydra")) {
            return true;
        }
        if (customName.contains("KHAOS TANRISI") || customName.contains("Khaos Tanrısı")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Canlı eğitilebilir mi?
     */
    public boolean canBeTamed(LivingEntity entity) {
        if (entity == null || entity.isDead()) {
            return false;
        }
        
        // Zaten eğitilmiş mi?
        if (isTamed(entity)) {
            return false;
        }
        
        // Boss mu?
        if (bossManager != null && bossManager.getBossData(entity.getUniqueId()) != null) {
            return true; // Bosslar eğitilebilir
        }
        
        // Normal canlı mı?
        String customName = entity.getCustomName();
        if (customName == null) {
            return false; // İsimsiz canlılar eğitilemez
        }
        
        // Tüm özel isimli canlılar eğitilebilir
        return true;
    }
    
    /**
     * Canlı eğitilmiş mi?
     */
    public boolean isTamed(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        
        return tamedCreatures.containsKey(entity.getUniqueId()) || 
               entity.hasMetadata("Tamed");
    }
    
    /**
     * Canlının sahibi kim?
     */
    public UUID getOwner(LivingEntity entity) {
        if (entity == null) {
            return null;
        }
        
        if (entity.hasMetadata("TamedOwner")) {
            return UUID.fromString(entity.getMetadata("TamedOwner").get(0).asString());
        }
        
        return tamedCreatures.get(entity.getUniqueId());
    }
    
    /**
     * Canlıyı eğit
     */
    public boolean tameCreature(LivingEntity entity, UUID ownerId, int difficultyLevel) {
        if (entity == null || entity.isDead() || ownerId == null) {
            return false;
        }
        
        // Zaten eğitilmiş mi?
        if (isTamed(entity)) {
            return false;
        }
        
        // Rastgele cinsiyet belirle
        Gender gender = new Random().nextBoolean() ? Gender.MALE : Gender.FEMALE;
        creatureGenders.put(entity.getUniqueId(), gender);
        
        // Eğit
        tamedCreatures.put(entity.getUniqueId(), ownerId);
        entity.setMetadata("Tamed", new FixedMetadataValue(plugin, true));
        entity.setMetadata("TamedOwner", new FixedMetadataValue(plugin, ownerId.toString()));
        entity.setMetadata("TamedGender", new FixedMetadataValue(plugin, gender.name()));
        
        // Başlangıçta sahibini takip et
        followingTargets.put(entity.getUniqueId(), ownerId);
        entity.setMetadata("FollowingTarget", new FixedMetadataValue(plugin, ownerId.toString()));
        
        // AI'yı kapat (sahibini takip eder)
        if (entity instanceof Tameable) {
            ((Tameable) entity).setTamed(true);
            ((Tameable) entity).setOwner(Bukkit.getOfflinePlayer(ownerId));
        }
        
        // Özel isim ekle (cinsiyet işareti ile)
        String currentName = entity.getCustomName();
        String genderSymbol = gender == Gender.MALE ? "§b♂" : "§d♀";
        if (currentName != null) {
            entity.setCustomName(currentName + " " + genderSymbol + " §7[Eğitilmiş]");
        } else {
            entity.setCustomName("§7Eğitilmiş Canlı " + genderSymbol);
        }
        
        // Glow efekti (eğitilmiş canlılar parlar)
        entity.setGlowing(true);
        
        saveTamedCreatures();
        return true;
    }
    
    /**
     * Canlının cinsiyeti
     */
    public Gender getGender(LivingEntity entity) {
        if (entity == null) {
            return null;
        }
        
        if (entity.hasMetadata("TamedGender")) {
            String genderStr = entity.getMetadata("TamedGender").get(0).asString();
            return Gender.valueOf(genderStr);
        }
        
        return creatureGenders.get(entity.getUniqueId());
    }
    
    /**
     * Canlının takip ettiği kişi
     */
    public UUID getFollowingTarget(LivingEntity entity) {
        if (entity == null) {
            return null;
        }
        
        if (entity.hasMetadata("FollowingTarget")) {
            return UUID.fromString(entity.getMetadata("FollowingTarget").get(0).asString());
        }
        
        return followingTargets.get(entity.getUniqueId());
    }
    
    /**
     * Canlının takip edeceği kişiyi belirle (Shift+sağ tık)
     */
    public boolean setFollowingTarget(LivingEntity entity, UUID targetPlayerId, UUID requesterId) {
        if (entity == null || !isTamed(entity)) {
            return false;
        }
        
        // Sadece sahip veya klan üyesi yapabilir
        UUID ownerId = getOwner(entity);
        if (ownerId == null || !canUseCreature(entity, requesterId)) {
            return false;
        }
        
        // Takip edilecek kişiyi ayarla
        followingTargets.put(entity.getUniqueId(), targetPlayerId);
        entity.setMetadata("FollowingTarget", new FixedMetadataValue(plugin, targetPlayerId.toString()));
        
        // AI takip sistemi (Tameable ise)
        if (entity instanceof Tameable) {
            ((Tameable) entity).setOwner(Bukkit.getOfflinePlayer(targetPlayerId));
        }
        
        return true;
    }
    
    /**
     * Canlıyı kullanabilir mi? (Sahip veya aynı klan)
     */
    public boolean canUseCreature(LivingEntity entity, UUID playerId) {
        if (entity == null || playerId == null) {
            return false;
        }
        
        UUID ownerId = getOwner(entity);
        if (ownerId == null) {
            return false;
        }
        
        // Sahip mi?
        if (ownerId.equals(playerId)) {
            return true;
        }
        
        // Aynı klan mı?
        me.mami.stratocraft.manager.ClanManager clanManager = plugin.getClanManager();
        if (clanManager != null) {
            me.mami.stratocraft.model.Clan ownerClan = clanManager.getClanByPlayer(ownerId);
            me.mami.stratocraft.model.Clan playerClan = clanManager.getClanByPlayer(playerId);
            
            if (ownerClan != null && playerClan != null && ownerClan.equals(playerClan)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Ritüel deseni kontrol et (seviyeye göre)
     * Merkez bloğun Eğitim Çekirdeği olup olmadığını kontrol eder
     */
    public boolean checkRitualPattern(Block centerBlock, int difficultyLevel) {
        // Merkez bloğun Eğitim Çekirdeği olup olmadığını kontrol et (metadata ile)
        if (!centerBlock.hasMetadata("TamingCore")) {
            return false;
        }
        
        Material[][] pattern = getRitualPatternForLevel(difficultyLevel);
        if (pattern == null) {
            return false;
        }
        
        int size = pattern.length;
        int offset = size / 2;
        
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                Block checkBlock = centerBlock.getRelative(x - offset, -1, z - offset);
                Material required = pattern[x][z];
                
                if (required != null && checkBlock.getType() != required) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Seviyeye göre ritüel deseni (public - listener için)
     * Merkezde Eğitim Çekirdeği olacak, seviyeye göre malzemeler zorlaşacak
     */
    public Material[][] getRitualPatternForLevel(int level) {
        switch (level) {
            case 1:
                // 3x3 Dirt/Grass Block + Merkez Eğitim Çekirdeği (basit malzemeler)
                return new Material[][] {
                    {Material.DIRT, Material.GRASS_BLOCK, Material.DIRT},
                    {Material.GRASS_BLOCK, null, Material.GRASS_BLOCK}, // Merkez Eğitim Çekirdeği (blok olarak kontrol edilir)
                    {Material.DIRT, Material.GRASS_BLOCK, Material.DIRT}
                };
                
            case 2:
                // 3x3 Cobblestone + Merkez Eğitim Çekirdeği
                return new Material[][] {
                    {Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE},
                    {Material.COBBLESTONE, null, Material.COBBLESTONE}, // Merkez Eğitim Çekirdeği
                    {Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE}
                };
                
            case 3:
                // 5x5 Stone Bricks + Merkez Eğitim Çekirdeği
                return new Material[][] {
                    {Material.STONE_BRICKS, Material.STONE_BRICKS, Material.STONE_BRICKS, Material.STONE_BRICKS, Material.STONE_BRICKS},
                    {Material.STONE_BRICKS, null, null, null, Material.STONE_BRICKS},
                    {Material.STONE_BRICKS, null, null, null, Material.STONE_BRICKS}, // Merkez Eğitim Çekirdeği
                    {Material.STONE_BRICKS, null, null, null, Material.STONE_BRICKS},
                    {Material.STONE_BRICKS, Material.STONE_BRICKS, Material.STONE_BRICKS, Material.STONE_BRICKS, Material.STONE_BRICKS}
                };
                
            case 4:
                // 5x5 Obsidian + Merkez Eğitim Çekirdeği
                return new Material[][] {
                    {Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN},
                    {Material.OBSIDIAN, null, null, null, Material.OBSIDIAN},
                    {Material.OBSIDIAN, null, null, null, Material.OBSIDIAN}, // Merkez Eğitim Çekirdeği
                    {Material.OBSIDIAN, null, null, null, Material.OBSIDIAN},
                    {Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN}
                };
                
            case 5:
                // 7x7 Bedrock + Merkez Eğitim Çekirdeği
                return new Material[][] {
                    {Material.BEDROCK, Material.BEDROCK, Material.BEDROCK, Material.BEDROCK, Material.BEDROCK, Material.BEDROCK, Material.BEDROCK},
                    {Material.BEDROCK, null, null, null, null, null, Material.BEDROCK},
                    {Material.BEDROCK, null, null, null, null, null, Material.BEDROCK},
                    {Material.BEDROCK, null, null, null, null, null, Material.BEDROCK}, // Merkez Eğitim Çekirdeği
                    {Material.BEDROCK, null, null, null, null, null, Material.BEDROCK},
                    {Material.BEDROCK, null, null, null, null, null, Material.BEDROCK},
                    {Material.BEDROCK, Material.BEDROCK, Material.BEDROCK, Material.BEDROCK, Material.BEDROCK, Material.BEDROCK, Material.BEDROCK}
                };
                
            default:
                return null;
        }
    }
    
    /**
     * Ritüel aktifleştirme itemi (seviyeye göre - zorlaşacak)
     */
    public Material getRitualActivationItem(int difficultyLevel) {
        switch (difficultyLevel) {
            case 1: return Material.WHEAT; // En basit
            case 2: return Material.BREAD; // Biraz daha zor
            case 3: return Material.GOLDEN_APPLE; // Orta seviye
            case 4: return Material.ENCHANTED_GOLDEN_APPLE; // Zor
            case 5: return Material.NETHER_STAR; // En zor
            default: return Material.WHEAT;
        }
    }
    
    /**
     * Boss eğitme ritüel deseni
     */
    public boolean checkBossRitualPattern(Block centerBlock, BossManager.BossType bossType) {
        Material[][] pattern = getBossRitualPattern(bossType);
        if (pattern == null) {
            return false;
        }
        
        int size = pattern.length;
        int offset = size / 2;
        
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                Block checkBlock = centerBlock.getRelative(x - offset, -1, z - offset);
                Material required = pattern[x][z];
                
                if (required != null && checkBlock.getType() != required) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Boss eğitme ritüel deseni (public - listener için)
     */
    public Material[][] getBossRitualPattern(BossManager.BossType bossType) {
        switch (bossType) {
            case GOBLIN_KING:
                // 3x3 Gold Block + Merkez Hay Block (Rotten Flesh temsili)
                return new Material[][] {
                    {Material.GOLD_BLOCK, Material.GOLD_BLOCK, Material.GOLD_BLOCK},
                    {Material.GOLD_BLOCK, Material.HAY_BLOCK, Material.GOLD_BLOCK},
                    {Material.GOLD_BLOCK, Material.GOLD_BLOCK, Material.GOLD_BLOCK}
                };
                
            case ORC_CHIEF:
                // 3x3 Iron Block + Merkez Iron Block (Iron Sword temsili)
                return new Material[][] {
                    {Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK},
                    {Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK},
                    {Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK}
                };
                
            case TROLL_KING:
                // 3x3 Diamond Block + Merkez Stone Block (Stone Axe temsili)
                return new Material[][] {
                    {Material.DIAMOND_BLOCK, Material.DIAMOND_BLOCK, Material.DIAMOND_BLOCK},
                    {Material.DIAMOND_BLOCK, Material.STONE, Material.DIAMOND_BLOCK},
                    {Material.DIAMOND_BLOCK, Material.DIAMOND_BLOCK, Material.DIAMOND_BLOCK}
                };
                
            case DRAGON:
                // 5x5 Emerald Block + Merkez Dragon Egg (blok)
                return new Material[][] {
                    {Material.EMERALD_BLOCK, Material.EMERALD_BLOCK, Material.EMERALD_BLOCK, Material.EMERALD_BLOCK, Material.EMERALD_BLOCK},
                    {Material.EMERALD_BLOCK, null, null, null, Material.EMERALD_BLOCK},
                    {Material.EMERALD_BLOCK, null, Material.DRAGON_EGG, null, Material.EMERALD_BLOCK},
                    {Material.EMERALD_BLOCK, null, null, null, Material.EMERALD_BLOCK},
                    {Material.EMERALD_BLOCK, Material.EMERALD_BLOCK, Material.EMERALD_BLOCK, Material.EMERALD_BLOCK, Material.EMERALD_BLOCK}
                };
                
            case TREX:
                // 5x5 Gold Block + Merkez Bone Block
                return new Material[][] {
                    {Material.GOLD_BLOCK, Material.GOLD_BLOCK, Material.GOLD_BLOCK, Material.GOLD_BLOCK, Material.GOLD_BLOCK},
                    {Material.GOLD_BLOCK, null, null, null, Material.GOLD_BLOCK},
                    {Material.GOLD_BLOCK, null, Material.BONE_BLOCK, null, Material.GOLD_BLOCK},
                    {Material.GOLD_BLOCK, null, null, null, Material.GOLD_BLOCK},
                    {Material.GOLD_BLOCK, Material.GOLD_BLOCK, Material.GOLD_BLOCK, Material.GOLD_BLOCK, Material.GOLD_BLOCK}
                };
                
            case CYCLOPS:
                // 5x5 Emerald Block + Merkez End Stone (Ender Eye temsili)
                return new Material[][] {
                    {Material.EMERALD_BLOCK, Material.EMERALD_BLOCK, Material.EMERALD_BLOCK, Material.EMERALD_BLOCK, Material.EMERALD_BLOCK},
                    {Material.EMERALD_BLOCK, null, null, null, Material.EMERALD_BLOCK},
                    {Material.EMERALD_BLOCK, null, Material.END_STONE, null, Material.EMERALD_BLOCK},
                    {Material.EMERALD_BLOCK, null, null, null, Material.EMERALD_BLOCK},
                    {Material.EMERALD_BLOCK, Material.EMERALD_BLOCK, Material.EMERALD_BLOCK, Material.EMERALD_BLOCK, Material.EMERALD_BLOCK}
                };
                
            case TITAN_GOLEM:
                // 7x7 Netherite Block + Merkez Beacon (Nether Star temsili)
                return new Material[][] {
                    {Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK},
                    {Material.NETHERITE_BLOCK, null, null, null, null, null, Material.NETHERITE_BLOCK},
                    {Material.NETHERITE_BLOCK, null, null, null, null, null, Material.NETHERITE_BLOCK},
                    {Material.NETHERITE_BLOCK, null, null, Material.BEACON, null, null, Material.NETHERITE_BLOCK},
                    {Material.NETHERITE_BLOCK, null, null, null, null, null, Material.NETHERITE_BLOCK},
                    {Material.NETHERITE_BLOCK, null, null, null, null, null, Material.NETHERITE_BLOCK},
                    {Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK}
                };
                
            case HELL_DRAGON:
                // 7x7 Netherrack + Merkez Magma Block (Blaze Rod temsili)
                return new Material[][] {
                    {Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK},
                    {Material.NETHERRACK, null, null, null, null, null, Material.NETHERRACK},
                    {Material.NETHERRACK, null, null, null, null, null, Material.NETHERRACK},
                    {Material.NETHERRACK, null, null, Material.MAGMA_BLOCK, null, null, Material.NETHERRACK},
                    {Material.NETHERRACK, null, null, null, null, null, Material.NETHERRACK},
                    {Material.NETHERRACK, null, null, null, null, null, Material.NETHERRACK},
                    {Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK}
                };
                
            case HYDRA:
                // 7x7 Prismarine + Merkez Conduit (Heart of the Sea temsili)
                return new Material[][] {
                    {Material.PRISMARINE, Material.PRISMARINE, Material.PRISMARINE, Material.PRISMARINE, Material.PRISMARINE, Material.PRISMARINE, Material.PRISMARINE},
                    {Material.PRISMARINE, null, null, null, null, null, Material.PRISMARINE},
                    {Material.PRISMARINE, null, null, null, null, null, Material.PRISMARINE},
                    {Material.PRISMARINE, null, null, Material.CONDUIT, null, null, Material.PRISMARINE},
                    {Material.PRISMARINE, null, null, null, null, null, Material.PRISMARINE},
                    {Material.PRISMARINE, null, null, null, null, null, Material.PRISMARINE},
                    {Material.PRISMARINE, Material.PRISMARINE, Material.PRISMARINE, Material.PRISMARINE, Material.PRISMARINE, Material.PRISMARINE, Material.PRISMARINE}
                };
                
            case CHAOS_GOD:
                // 9x9 Bedrock + Merkez Beacon (Nether Star temsili)
                return new Material[][] {
                    {Material.BEDROCK, Material.BEDROCK, Material.BEDROCK, Material.BEDROCK, Material.BEDROCK, Material.BEDROCK, Material.BEDROCK, Material.BEDROCK, Material.BEDROCK},
                    {Material.BEDROCK, null, null, null, null, null, null, null, Material.BEDROCK},
                    {Material.BEDROCK, null, null, null, null, null, null, null, Material.BEDROCK},
                    {Material.BEDROCK, null, null, null, null, null, null, null, Material.BEDROCK},
                    {Material.BEDROCK, null, null, null, Material.BEACON, null, null, null, Material.BEDROCK},
                    {Material.BEDROCK, null, null, null, null, null, null, null, Material.BEDROCK},
                    {Material.BEDROCK, null, null, null, null, null, null, null, Material.BEDROCK},
                    {Material.BEDROCK, null, null, null, null, null, null, null, Material.BEDROCK},
                    {Material.BEDROCK, Material.BEDROCK, Material.BEDROCK, Material.BEDROCK, Material.BEDROCK, Material.BEDROCK, Material.BEDROCK, Material.BEDROCK, Material.BEDROCK}
                };
                
            default:
                return null;
        }
    }
    
    /**
     * Boss eğitme aktifleştirme itemi
     */
    public Material getBossRitualActivationItem(BossManager.BossType bossType) {
        switch (bossType) {
            case GOBLIN_KING: return Material.ROTTEN_FLESH;
            case ORC_CHIEF: return Material.IRON_SWORD;
            case TROLL_KING: return Material.STONE_AXE;
            case DRAGON: return Material.DRAGON_EGG;
            case TREX: return Material.BONE;
            case CYCLOPS: return Material.ENDER_EYE;
            case TITAN_GOLEM: return Material.NETHER_STAR;
            case HELL_DRAGON: return Material.BLAZE_ROD;
            case HYDRA: return Material.HEART_OF_THE_SEA;
            case CHAOS_GOD: return Material.NETHER_STAR;
            default: return null;
        }
    }
    
    /**
     * Canlıyı binilebilir yap
     */
    public void makeRideable(LivingEntity entity, Player rider) {
        if (entity == null || rider == null) {
            return;
        }
        
        if (!isRideable(entity)) {
            return;
        }
        
        // Oyuncuyu canlının üzerine bindir
        entity.addPassenger(rider);
        
        // Binme efekti
        Location loc = entity.getLocation();
        loc.getWorld().spawnParticle(org.bukkit.Particle.HEART, loc, 10, 0.5, 1, 0.5, 0.1);
        loc.getWorld().playSound(loc, org.bukkit.Sound.ENTITY_HORSE_SADDLE, 1.0f, 1.0f);
    }
    
    /**
     * Eğitilmiş canlıları kaydet
     */
    private void saveTamedCreatures() {
        if (tamedFile == null) {
            tamedFile = new File(plugin.getDataFolder(), "tamed_creatures.yml");
        }
        
        if (tamedConfig == null) {
            tamedConfig = new YamlConfiguration();
        }
        
        tamedConfig.set("creatures", null);
        
        int index = 0;
        for (Map.Entry<UUID, UUID> entry : tamedCreatures.entrySet()) {
            String path = "creatures." + index;
            UUID entityId = entry.getKey();
            UUID ownerId = entry.getValue();
            
            tamedConfig.set(path + ".entity", entityId.toString());
            tamedConfig.set(path + ".owner", ownerId.toString());
            
            // Cinsiyet
            Gender gender = creatureGenders.get(entityId);
            if (gender != null) {
                tamedConfig.set(path + ".gender", gender.name());
            }
            
            // Takip edilecek kişi
            UUID followingTarget = followingTargets.get(entityId);
            if (followingTarget != null) {
                tamedConfig.set(path + ".followingTarget", followingTarget.toString());
            }
            
            index++;
        }
        
        try {
            tamedConfig.save(tamedFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Eğitilmiş canlılar kaydedilemedi: " + e.getMessage());
        }
    }
    
    /**
     * Eğitilmiş canlıları yükle
     */
    private void loadTamedCreatures() {
        tamedFile = new File(plugin.getDataFolder(), "tamed_creatures.yml");
        
        if (!tamedFile.exists()) {
            try {
                tamedFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Eğitilmiş canlılar dosyası oluşturulamadı: " + e.getMessage());
            }
            return;
        }
        
        tamedConfig = YamlConfiguration.loadConfiguration(tamedFile);
        
        if (!tamedConfig.contains("creatures")) {
            return;
        }
        
        for (String key : tamedConfig.getConfigurationSection("creatures").getKeys(false)) {
            String path = "creatures." + key;
            UUID entityId = UUID.fromString(tamedConfig.getString(path + ".entity"));
            UUID ownerId = UUID.fromString(tamedConfig.getString(path + ".owner"));
            
            tamedCreatures.put(entityId, ownerId);
            
            // Cinsiyet yükle
            if (tamedConfig.contains(path + ".gender")) {
                try {
                    Gender gender = Gender.valueOf(tamedConfig.getString(path + ".gender"));
                    creatureGenders.put(entityId, gender);
                } catch (IllegalArgumentException e) {
                    // Geçersiz cinsiyet, rastgele atama
                    creatureGenders.put(entityId, new Random().nextBoolean() ? Gender.MALE : Gender.FEMALE);
                }
            }
            
            // Takip edilecek kişi yükle
            if (tamedConfig.contains(path + ".followingTarget")) {
                UUID followingTarget = UUID.fromString(tamedConfig.getString(path + ".followingTarget"));
                followingTargets.put(entityId, followingTarget);
            }
        }
        
        plugin.getLogger().info("Eğitilmiş canlılar yüklendi: " + tamedCreatures.size() + " canlı");
    }
    
    public TamingManager(Main plugin) {
        this.plugin = plugin;
        this.difficultyManager = plugin.getDifficultyManager();
        this.bossManager = plugin.getBossManager();
        loadTamedCreatures();
    }
    
    /**
     * Cooldown kontrolü
     */
    public boolean isOnCooldown(Location loc) {
        if (!ritualCooldowns.containsKey(loc)) {
            return false;
        }
        
        long cooldownTime = ritualCooldowns.get(loc);
        return System.currentTimeMillis() - cooldownTime < RITUAL_COOLDOWN;
    }
    
    /**
     * Cooldown kaydet
     */
    public void setCooldown(Location loc) {
        ritualCooldowns.put(loc, System.currentTimeMillis());
    }
    
    /**
     * Eğitilmiş canlıyı kaldır (öldüğünde)
     */
    public void removeTamedCreature(UUID entityId) {
        tamedCreatures.remove(entityId);
        saveTamedCreatures();
    }
}

