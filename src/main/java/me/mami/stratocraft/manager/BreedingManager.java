package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Clan;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * Çiftleştirme Sistemi
 * 
 * - Dişi ve erkek canlıları çiftleştirme
 * - Memeli canlılar → Direkt çocuk
 * - Yumurtlayan canlılar → Yumurta (kaplumbağa mantığı)
 * - Çiftleştirme tesisi (seviyeye göre)
 */
public class BreedingManager {
    private final Main plugin;
    private final TamingManager tamingManager;
    private final ClanManager clanManager;
    private final MobManager mobManager;
    
    // Çiftleştirme tesisleri (Location -> BreedingFacility)
    private final Map<Location, BreedingFacility> breedingFacilities = new HashMap<>();
    
    // Aktif çiftleştirmeler (Entity UUID -> BreedingData)
    private final Map<UUID, BreedingData> activeBreedings = new HashMap<>();
    
    /**
     * Çiftleştirme tesisi verisi
     */
    public static class BreedingFacility {
        private final Location location;
        private final UUID ownerId;
        private final int level; // Seviye (1-5)
        private LivingEntity female; // Dişi canlı
        private LivingEntity male;   // Erkek canlı
        private long startTime;      // Başlangıç zamanı
        private long duration;       // Süre (ms)
        private boolean isBreeding;  // Çiftleştirme aktif mi?
        private BukkitTask task;    // Task referansı
        
        public BreedingFacility(Location location, UUID ownerId, int level) {
            this.location = location;
            this.ownerId = ownerId;
            this.level = level;
            this.duration = getBreedingDuration(level);
            this.isBreeding = false;
        }
        
        public Location getLocation() { return location; }
        public UUID getOwnerId() { return ownerId; }
        public int getLevel() { return level; }
        public LivingEntity getFemale() { return female; }
        public LivingEntity getMale() { return male; }
        public void setFemale(LivingEntity female) { this.female = female; }
        public void setMale(LivingEntity male) { this.male = male; }
        public long getStartTime() { return startTime; }
        public void setStartTime(long startTime) { this.startTime = startTime; }
        public long getDuration() { return duration; }
        public boolean isBreeding() { return isBreeding; }
        public void setBreeding(boolean isBreeding) { this.isBreeding = isBreeding; }
        public BukkitTask getTask() { return task; }
        public void setTask(BukkitTask task) { this.task = task; }
        
        public long getRemainingTime() {
            if (!isBreeding) return 0;
            long elapsed = System.currentTimeMillis() - startTime;
            return Math.max(0, duration - elapsed);
        }
        
        public boolean isComplete() {
            return isBreeding && getRemainingTime() <= 0;
        }
        
        private long getBreedingDuration(int level) {
            // Seviyeye göre süre (1 gün = 86400000 ms)
            switch (level) {
                case 1: return 86400000L;  // 1 gün
                case 2: return 172800000L; // 2 gün
                case 3: return 259200000L; // 3 gün
                case 4: return 345600000L; // 4 gün
                case 5: return 432000000L; // 5 gün
                default: return 86400000L;
            }
        }
    }
    
    /**
     * Çiftleştirme verisi (doğal çiftleştirme için)
     */
    public static class BreedingData {
        private final LivingEntity female;
        private final LivingEntity male;
        private final UUID ownerId;
        private long startTime;
        private long duration;
        
        public BreedingData(LivingEntity female, LivingEntity male, UUID ownerId, long duration) {
            this.female = female;
            this.male = male;
            this.ownerId = ownerId;
            this.startTime = System.currentTimeMillis();
            this.duration = duration;
        }
        
        public LivingEntity getFemale() { return female; }
        public LivingEntity getMale() { return male; }
        public UUID getOwnerId() { return ownerId; }
        public long getRemainingTime() {
            long elapsed = System.currentTimeMillis() - startTime;
            return Math.max(0, duration - elapsed);
        }
        public boolean isComplete() {
            return getRemainingTime() <= 0;
        }
    }
    
    public BreedingManager(Main plugin) {
        this.plugin = plugin;
        this.tamingManager = plugin.getTamingManager();
        this.clanManager = plugin.getClanManager();
        this.mobManager = plugin.getMobManager();
    }
    
    /**
     * Canlı memeli mi? (direkt çocuk)
     */
    public boolean isMammal(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        
        String customName = entity.getCustomName();
        if (customName == null) {
            return false;
        }
        
        // Memeli canlılar
        String name = customName.toUpperCase();
        return name.contains("ORK") || name.contains("TROLL") || 
               name.contains("GOBLIN") || name.contains("MINOTAUR") ||
               name.contains("WAR_BEAR") || name.contains("SAVAŞ AYISI") ||
               name.contains("WEREWOLF") || name.contains("KURT ADAM");
    }
    
    /**
     * Canlı yumurtlayan mı? (yumurta)
     */
    public boolean isEggLayer(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        
        String customName = entity.getCustomName();
        if (customName == null) {
            return false;
        }
        
        // Yumurtlayan canlılar
        String name = customName.toUpperCase();
        return name.contains("EJDERHA") || name.contains("DRAGON") ||
               name.contains("GRIFFIN") || name.contains("PHOENIX") ||
               name.contains("WYVERN") || name.contains("HYDRA") ||
               name.contains("HARPY") || name.contains("T-REX");
    }
    
    /**
     * Çiftleştirme tesisi oluştur
     */
    public boolean createBreedingFacility(Location location, Player player, int level) {
        if (location == null || player == null) {
            return false;
        }
        
        // Zaten tesis var mı?
        if (breedingFacilities.containsKey(location)) {
            return false;
        }
        
        // Tesis oluştur
        BreedingFacility facility = new BreedingFacility(location, player.getUniqueId(), level);
        breedingFacilities.put(location, facility);
        
        player.sendMessage("§a§lÇiftleştirme Tesisi oluşturuldu! (Seviye " + level + ")");
        player.sendMessage("§7Tesise 1 dişi ve 1 erkek canlı getirin ve yiyecek bırakın.");
        
        return true;
    }
    
    /**
     * Canlıyı tesise ekle
     */
    public boolean addCreatureToFacility(Location facilityLoc, LivingEntity creature, Player player) {
        BreedingFacility facility = breedingFacilities.get(facilityLoc);
        if (facility == null) {
            return false;
        }
        
        // Sahip kontrolü
        UUID ownerId = tamingManager.getOwner(creature);
        if (ownerId == null || !ownerId.equals(facility.getOwnerId())) {
            player.sendMessage("§cBu canlı sana ait değil!");
            return false;
        }
        
        // Eğitilmiş mi?
        if (!tamingManager.isTamed(creature)) {
            player.sendMessage("§cSadece eğitilmiş canlılar çiftleştirilebilir!");
            return false;
        }
        
        // Cinsiyet kontrolü
        TamingManager.Gender gender = tamingManager.getGender(creature);
        if (gender == null) {
            player.sendMessage("§cCanlının cinsiyeti belirlenemiyor!");
            return false;
        }
        
        // Tesis dolu mu?
        if (facility.getFemale() != null && facility.getMale() != null) {
            player.sendMessage("§cTesis zaten dolu! (1 dişi + 1 erkek)");
            return false;
        }
        
        // Canlıyı ekle
        if (gender == TamingManager.Gender.FEMALE) {
            if (facility.getFemale() != null) {
                player.sendMessage("§cTesis zaten bir dişi canlı içeriyor!");
                return false;
            }
            facility.setFemale(creature);
            player.sendMessage("§a§lDişi canlı tesise eklendi!");
        } else {
            if (facility.getMale() != null) {
                player.sendMessage("§cTesis zaten bir erkek canlı içeriyor!");
                return false;
            }
            facility.setMale(creature);
            player.sendMessage("§a§lErkek canlı tesise eklendi!");
        }
        
        // Her ikisi de varsa çiftleştirmeyi başlat
        if (facility.getFemale() != null && facility.getMale() != null) {
            startBreedingInFacility(facility, player);
        }
        
        return true;
    }
    
    /**
     * Tesiste çiftleştirmeyi başlat
     */
    private void startBreedingInFacility(BreedingFacility facility, Player player) {
        // Yiyecek kontrolü (tesiste yeterince yiyecek var mı?)
        if (!hasEnoughFood(facility.getLocation())) {
            player.sendMessage("§cTesis yeterince yiyecek içermiyor!");
            return;
        }
        
        facility.setBreeding(true);
        facility.setStartTime(System.currentTimeMillis());
        
        // Çiftleştirme task'ı
        BukkitTask task = org.bukkit.Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (facility.isComplete()) {
                // Çiftleştirme tamamlandı
                completeBreeding(facility);
                if (facility.getTask() != null) {
                    facility.getTask().cancel();
                }
            }
        }, 0L, 1200L); // Her 1 dakika kontrol
        
        facility.setTask(task);
        
        player.sendMessage("§a§lÇiftleştirme başladı!");
        player.sendMessage("§7Süre: §e" + formatTime(facility.getDuration()));
    }
    
    /**
     * Çiftleştirme tamamlandı
     */
    private void completeBreeding(BreedingFacility facility) {
        LivingEntity female = facility.getFemale();
        LivingEntity male = facility.getMale();
        
        if (female == null || male == null || female.isDead() || male.isDead()) {
            return;
        }
        
        Location spawnLoc = facility.getLocation().clone().add(0.5, 1, 0.5);
        UUID ownerId = facility.getOwnerId();
        
        // Memeli mi yoksa yumurtlayan mı?
        if (isMammal(female)) {
            // Memeli - direkt çocuk spawn et
            spawnOffspring(female, spawnLoc, ownerId);
        } else if (isEggLayer(female)) {
            // Yumurtlayan - yumurta spawn et
            spawnEgg(female, spawnLoc, ownerId);
        }
        
        // Tesis temizle
        facility.setFemale(null);
        facility.setMale(null);
        facility.setBreeding(false);
        
        // Broadcast
        org.bukkit.Bukkit.broadcastMessage("§a§lÇiftleştirme tamamlandı!");
    }
    
    /**
     * Yavru spawn et (memeli)
     */
    private void spawnOffspring(LivingEntity parent, Location loc, UUID ownerId) {
        // Parent'ın tipine göre yavru spawn et
        String parentName = parent.getCustomName();
        if (parentName == null) {
            return;
        }
        
        // Yavru spawn et (parent'ın tipine göre)
        LivingEntity offspring = spawnCreatureByType(parentName, loc);
        if (offspring == null) {
            return;
        }
        
        // Otomatik eğitilmiş yap
        int difficultyLevel = plugin.getDifficultyManager().getDifficultyLevel(loc);
        tamingManager.tameCreature(offspring, ownerId, difficultyLevel);
        
        // Cinsiyet zaten tameCreature'da atanır, isim de orada güncellenir
        // İsim güncelle (cinsiyet işareti zaten tameCreature'da eklenir)
        String currentName = offspring.getCustomName();
        if (currentName != null) {
            // Parent ismini temizle (cinsiyet işaretleri ve eğitilmiş etiketi)
            String cleanParentName = parentName.replace(" [Eğitilmiş]", "")
                                               .replace(" §b♂", "")
                                               .replace(" §d♀", "")
                                               .trim();
            // Cinsiyet işaretini al
            TamingManager.Gender gender = tamingManager.getGender(offspring);
            String genderSymbol = gender == TamingManager.Gender.MALE ? "§b♂" : "§d♀";
            // Yeni isim oluştur
            String baseName = currentName.replace("§7Eğitilmiş Canlı", cleanParentName + " Yavrusu");
            offspring.setCustomName(baseName + " " + genderSymbol + " §7[Eğitilmiş]");
        }
        
        // Efekt
        loc.getWorld().spawnParticle(org.bukkit.Particle.HEART, loc, 20, 1, 1, 1, 0.1);
        loc.getWorld().playSound(loc, org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
    }
    
    /**
     * Yumurta spawn et (yumurtlayan)
     */
    private void spawnEgg(LivingEntity parent, Location loc, UUID ownerId) {
        // Yumurta spawn et (kaplumbağa mantığı)
        org.bukkit.entity.Turtle turtle = (org.bukkit.entity.Turtle) loc.getWorld().spawnEntity(loc, EntityType.TURTLE);
        turtle.setCustomName("§e" + parent.getCustomName().replace(" [Eğitilmiş]", "") + " Yumurtası");
        turtle.setBaby(true);
        turtle.setAge(-6000); // Yumurta aşaması
        
        // Metadata ile sahip bilgisi
        turtle.setMetadata("EggOwner", new org.bukkit.metadata.FixedMetadataValue(plugin, ownerId.toString()));
        turtle.setMetadata("EggParent", new org.bukkit.metadata.FixedMetadataValue(plugin, parent.getUniqueId().toString()));
        
        // Efekt
        loc.getWorld().spawnParticle(org.bukkit.Particle.HEART, loc, 20, 1, 1, 1, 0.1);
        loc.getWorld().playSound(loc, org.bukkit.Sound.ENTITY_CHICKEN_EGG, 1.0f, 1.0f);
    }
    
    /**
     * Canlı tipine göre spawn et
     */
    private LivingEntity spawnCreatureByType(String parentName, Location loc) {
        // Cinsiyet işaretlerini ve eğitilmiş etiketini temizle
        String cleanName = parentName.replace(" [Eğitilmiş]", "")
                                     .replace(" §b♂", "")
                                     .replace(" §d♀", "")
                                     .trim();
        String name = cleanName.toUpperCase();
        
        // MobManager metodları void döndürüyor, bu yüzden manuel spawn yapmalıyız
        if (name.contains("ORK")) {
            org.bukkit.entity.Zombie ork = (org.bukkit.entity.Zombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
            ork.setCustomName("§cOrk");
            if (ork.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH) != null) {
                ork.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(50.0);
            }
            ork.setHealth(50.0);
            return ork;
        } else if (name.contains("GOBLIN")) {
            org.bukkit.entity.Zombie goblin = (org.bukkit.entity.Zombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
            goblin.setCustomName("§2Goblin");
            goblin.setBaby(true);
            if (goblin.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH) != null) {
                goblin.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(30.0);
            }
            goblin.setHealth(30.0);
            return goblin;
        } else if (name.contains("TROLL")) {
            org.bukkit.entity.Zombie troll = (org.bukkit.entity.Zombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
            troll.setCustomName("§5Troll");
            if (troll.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH) != null) {
                troll.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(80.0);
            }
            troll.setHealth(80.0);
            return troll;
        } else if (name.contains("MINOTAUR")) {
            org.bukkit.entity.Zombie minotaur = (org.bukkit.entity.Zombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
            minotaur.setCustomName("§6Minotaur");
            if (minotaur.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH) != null) {
                minotaur.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(100.0);
            }
            minotaur.setHealth(100.0);
            return minotaur;
        }
        
        // Varsayılan: Zombie
        org.bukkit.entity.Zombie defaultMob = (org.bukkit.entity.Zombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
        defaultMob.setCustomName("§7Yavru");
        return defaultMob;
    }
    
    /**
     * Tesiste yeterince yiyecek var mı?
     */
    private boolean hasEnoughFood(Location loc) {
        // 3x3 alanı kontrol et
        int foodCount = 0;
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Block block = loc.clone().add(x, 0, z).getBlock();
                Material type = block.getType();
                
                // Yiyecek blokları
                if (type == Material.HAY_BLOCK || type == Material.CARROTS || 
                    type == Material.WHEAT || type == Material.BEETROOTS ||
                    type == Material.POTATOES || type == Material.MELON ||
                    type == Material.PUMPKIN) {
                    foodCount++;
                }
            }
        }
        
        return foodCount >= 3; // En az 3 yiyecek bloğu
    }
    
    /**
     * Zaman formatla
     */
    private String formatTime(long ms) {
        long days = ms / 86400000L;
        long hours = (ms % 86400000L) / 3600000L;
        long minutes = (ms % 3600000L) / 60000L;
        
        if (days > 0) {
            return days + " gün " + hours + " saat";
        } else if (hours > 0) {
            return hours + " saat " + minutes + " dakika";
        } else {
            return minutes + " dakika";
        }
    }
    
    /**
     * Doğal çiftleştirme (yemek verme)
     */
    public boolean breedCreatures(LivingEntity female, LivingEntity male, Player player) {
        if (female == null || male == null || player == null) {
            return false;
        }
        
        // Her ikisi de eğitilmiş mi?
        if (!tamingManager.isTamed(female) || !tamingManager.isTamed(male)) {
            return false;
        }
        
        // Sahip kontrolü
        UUID ownerId = tamingManager.getOwner(female);
        if (ownerId == null || !ownerId.equals(tamingManager.getOwner(male))) {
            player.sendMessage("§cCanlılar aynı sahibe ait olmalı!");
            return false;
        }
        
        // Cinsiyet kontrolü
        TamingManager.Gender femaleGender = tamingManager.getGender(female);
        TamingManager.Gender maleGender = tamingManager.getGender(male);
        
        if (femaleGender != TamingManager.Gender.FEMALE || maleGender != TamingManager.Gender.MALE) {
            player.sendMessage("§cBir dişi ve bir erkek canlı gerekli!");
            return false;
        }
        
        // Çiftleştirme başlat
        long duration = 60000L; // 1 dakika (doğal çiftleştirme)
        BreedingData breeding = new BreedingData(female, male, ownerId, duration);
        activeBreedings.put(female.getUniqueId(), breeding);
        activeBreedings.put(male.getUniqueId(), breeding);
        
        // Task başlat (60000ms = 1200 tick)
        final LivingEntity finalFemale = female;
        final LivingEntity finalMale = male;
        final UUID finalOwnerId = ownerId;
        long ticks = duration / 50L; // 60000ms / 50 = 1200 tick = 60 saniye
        
        org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (finalFemale != null && !finalFemale.isDead() && 
                finalMale != null && !finalMale.isDead()) {
                completeNaturalBreeding(breeding);
                activeBreedings.remove(finalFemale.getUniqueId());
                activeBreedings.remove(finalMale.getUniqueId());
            }
        }, ticks);
        
        player.sendMessage("§a§lÇiftleştirme başladı!");
        return true;
    }
    
    /**
     * Doğal çiftleştirme tamamlandı
     */
    private void completeNaturalBreeding(BreedingData breeding) {
        Location spawnLoc = breeding.getFemale().getLocation().clone().add(0.5, 1, 0.5);
        
        if (isMammal(breeding.getFemale())) {
            spawnOffspring(breeding.getFemale(), spawnLoc, breeding.getOwnerId());
        } else if (isEggLayer(breeding.getFemale())) {
            spawnEgg(breeding.getFemale(), spawnLoc, breeding.getOwnerId());
        }
    }
    
    /**
     * Yumurta çatladı mı kontrol et
     */
    public void checkEggHatching(org.bukkit.entity.Turtle turtle) {
        if (turtle == null || turtle.isDead()) {
            return;
        }
        
        // Yumurta mı?
        if (!turtle.hasMetadata("EggOwner") || !turtle.hasMetadata("EggParent")) {
            return;
        }
        
        // Yaş 0 veya üzeri ise çatlamış demektir
        if (turtle.getAge() >= 0) {
            try {
                UUID ownerId = UUID.fromString(turtle.getMetadata("EggOwner").get(0).asString());
                UUID parentId = UUID.fromString(turtle.getMetadata("EggParent").get(0).asString());
                
                // Parent'ı bul
                LivingEntity parent = null;
                for (org.bukkit.entity.Entity entity : turtle.getWorld().getEntities()) {
                    if (entity.getUniqueId().equals(parentId) && entity instanceof LivingEntity) {
                        parent = (LivingEntity) entity;
                        break;
                    }
                }
                
                if (parent != null && !parent.isDead()) {
                    // Yavru spawn et
                    spawnOffspring(parent, turtle.getLocation(), ownerId);
                } else {
                    // Parent bulunamadı, varsayılan yavru spawn et
                    String eggName = turtle.getCustomName();
                    if (eggName != null) {
                        String parentType = eggName.replace(" Yumurtası", "").replace("§e", "").trim();
                        // Varsayılan yavru spawn et
                        LivingEntity defaultOffspring = spawnCreatureByType(parentType, turtle.getLocation());
                        if (defaultOffspring != null) {
                            int difficultyLevel = plugin.getDifficultyManager().getDifficultyLevel(turtle.getLocation());
                            tamingManager.tameCreature(defaultOffspring, ownerId, difficultyLevel);
                        }
                    }
                }
                
                // Yumurtayı kaldır
                turtle.remove();
            } catch (Exception e) {
                plugin.getLogger().warning("Yumurta çatlama hatası: " + e.getMessage());
            }
        }
    }
    
    // Getter/Setter
    public BreedingFacility getFacility(Location loc) {
        return breedingFacilities.get(loc);
    }
    
    public void removeFacility(Location loc) {
        BreedingFacility facility = breedingFacilities.remove(loc);
        if (facility != null && facility.getTask() != null) {
            facility.getTask().cancel();
        }
    }
    
    /**
     * Çiftleştirme süresini anında bitir (admin)
     */
    public void completeBreedingInstantly(Location facilityLoc) {
        BreedingFacility facility = breedingFacilities.get(facilityLoc);
        if (facility == null || !facility.isBreeding()) {
            return;
        }
        
        completeBreeding(facility);
        if (facility.getTask() != null) {
            facility.getTask().cancel();
        }
    }
}

