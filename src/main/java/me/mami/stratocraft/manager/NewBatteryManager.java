package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Yeni Esnek Batarya Sistemi
 * Her bataryanın kendine özel tarif kontrol fonksiyonu var
 */
public class NewBatteryManager {
    
    private final Main plugin;
    
    /**
     * Batarya veri sınıfı
     */
    public static class NewBatteryData {
        private final String batteryName;
        private final Material fuel;
        private final int alchemyLevel;
        private final boolean hasAmplifier;
        private final double trainingMultiplier;
        private final boolean isRedDiamond;
        private final boolean isDarkMatter;
        private final int batteryLevel;
        
        public NewBatteryData(String batteryName, Material fuel, int alchemyLevel, 
                             boolean hasAmplifier, double trainingMultiplier,
                             boolean isRedDiamond, boolean isDarkMatter, int batteryLevel) {
            this.batteryName = batteryName;
            this.fuel = fuel;
            this.alchemyLevel = alchemyLevel;
            this.hasAmplifier = hasAmplifier;
            this.trainingMultiplier = trainingMultiplier;
            this.isRedDiamond = isRedDiamond;
            this.isDarkMatter = isDarkMatter;
            this.batteryLevel = batteryLevel;
        }
        
        // Getters
        public String getBatteryName() { return batteryName; }
        public Material getFuel() { return fuel; }
        public int getAlchemyLevel() { return alchemyLevel; }
        public boolean hasAmplifier() { return hasAmplifier; }
        public double getTrainingMultiplier() { return trainingMultiplier; }
        public boolean isRedDiamond() { return isRedDiamond; }
        public boolean isDarkMatter() { return isDarkMatter; }
        public int getBatteryLevel() { return batteryLevel; }
    }
    
    /**
     * Blok Konumu - Merkez bloktan göreceli koordinat
     */
    public static class BlockPosition {
        private final int x, y, z; // Merkez bloktan göreceli (0,0,0 = merkez)
        
        public BlockPosition(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        
        public int getX() { return x; }
        public int getY() { return y; }
        public int getZ() { return z; }
        
        // Yardımcı metodlar
        public static BlockPosition up(int count) { return new BlockPosition(0, count, 0); }
        public static BlockPosition down(int count) { return new BlockPosition(0, -count, 0); }
        public static BlockPosition east(int count) { return new BlockPosition(count, 0, 0); }
        public static BlockPosition west(int count) { return new BlockPosition(-count, 0, 0); }
        public static BlockPosition north(int count) { return new BlockPosition(0, 0, -count); }
        public static BlockPosition south(int count) { return new BlockPosition(0, 0, count); }
        public static BlockPosition at(int x, int y, int z) { return new BlockPosition(x, y, z); }
    }
    
    /**
     * Blok Yerleşim Deseni - Tamamen esnek, herhangi bir şekilde blok kontrolü
     */
    public static class BlockPattern {
        // Merkez blok (zorunlu)
        private final Material centerBlock;
        
        // Herhangi bir konumda blok kontrolü (x, y, z -> Material)
        private final Map<BlockPosition, Material> requiredBlocks;
        
        public BlockPattern(Material centerBlock) {
            this.centerBlock = centerBlock;
            this.requiredBlocks = new HashMap<>();
        }
        
        /**
         * Belirli bir konumda blok ekle
         * @param position Merkez bloktan göreceli konum
         * @param material Beklenen blok tipi
         */
        public BlockPattern addBlock(BlockPosition position, Material material) {
            requiredBlocks.put(position, material);
            return this;
        }
        
        /**
         * Yardımcı metodlar - kolay kullanım için
         */
        public BlockPattern addBlockAbove(int count, Material material) {
            return addBlock(BlockPosition.up(count), material);
        }
        
        public BlockPattern addBlockBelow(int count, Material material) {
            return addBlock(BlockPosition.down(count), material);
        }
        
        public BlockPattern addBlockEast(int count, Material material) {
            return addBlock(BlockPosition.east(count), material);
        }
        
        public BlockPattern addBlockWest(int count, Material material) {
            return addBlock(BlockPosition.west(count), material);
        }
        
        public BlockPattern addBlockNorth(int count, Material material) {
            return addBlock(BlockPosition.north(count), material);
        }
        
        public BlockPattern addBlockSouth(int count, Material material) {
            return addBlock(BlockPosition.south(count), material);
        }
        
        public BlockPattern addBlockAt(int x, int y, int z, Material material) {
            return addBlock(BlockPosition.at(x, y, z), material);
        }
        
        // Getters
        public Material getCenterBlock() { return centerBlock; }
        public Map<BlockPosition, Material> getRequiredBlocks() { return requiredBlocks; }
    }
    
    /**
     * Tarif Kontrol Sonucu
     */
    public static class RecipeCheckResult {
        private final boolean matches;
        private final BlockPattern matchedPattern;
        private final List<Block> blocksToRemove;
        private final String errorMessage;
        private final String batteryName;
        
        public RecipeCheckResult(boolean matches, BlockPattern pattern, List<Block> blocksToRemove, 
                               String errorMessage, String batteryName) {
            this.matches = matches;
            this.matchedPattern = pattern;
            this.blocksToRemove = blocksToRemove != null ? new ArrayList<>(blocksToRemove) : new ArrayList<>();
            this.errorMessage = errorMessage;
            this.batteryName = batteryName;
        }
        
        public static RecipeCheckResult success(BlockPattern pattern, List<Block> blocksToRemove, String batteryName) {
            return new RecipeCheckResult(true, pattern, blocksToRemove, null, batteryName);
        }
        
        public static RecipeCheckResult failure(String errorMessage) {
            return new RecipeCheckResult(false, null, null, errorMessage, null);
        }
        
        public boolean matches() { return matches; }
        public BlockPattern getMatchedPattern() { return matchedPattern; }
        public List<Block> getBlocksToRemove() { return blocksToRemove; }
        public String getErrorMessage() { return errorMessage; }
        public String getBatteryName() { return batteryName; }
    }
    
    /**
     * Tarif Kontrol Fonksiyonu Interface'i
     * Her batarya kendi kontrol fonksiyonunu implement eder
     */
    public interface RecipeChecker {
        /**
         * Merkez bloktan başlayarak tarif kontrolü yapar
         * @param centerBlock Merkez blok
         * @return Tarif eşleşiyorsa success, değilse failure
         */
        RecipeCheckResult checkRecipe(Block centerBlock);
        
        /**
         * Bu tarifin adı
         */
        String getBatteryName();
        
        /**
         * Bu tarifin seviyesi
         */
        int getLevel();
        
        /**
         * Bu tarifin pattern'ini döndür (build için)
         */
        BlockPattern getPattern();
    }
    
    // Batarya ismi -> RecipeChecker mapping
    private final Map<String, RecipeChecker> recipeCheckers;
    
    // Yüklü bataryalar (UUID -> (Slot -> BatteryData))
    private final Map<UUID, Map<Integer, NewBatteryData>> loadedBatteries;
    
    public NewBatteryManager(Main plugin) {
        this.plugin = plugin;
        this.recipeCheckers = new HashMap<>();
        this.loadedBatteries = new HashMap<>();
        
        // Tüm batarya tariflerini kaydet
        registerAllRecipes();
    }
    
    public Main getPlugin() {
        return plugin;
    }
    
    /**
     * Tüm batarya tariflerini kaydet
     */
    private void registerAllRecipes() {
        // Seviye 1 - Saldırı Bataryaları
        registerRecipe(new LightningStaffL1Recipe());
        registerRecipe(new HellfireBallL1Recipe());
        registerRecipe(new IceBallL1Recipe());
        registerRecipe(new PoisonArrowL1Recipe());
        registerRecipe(new ShockWaveL1Recipe());
        
        // Seviye 2 - Saldırı Bataryaları
        registerRecipe(new DoubleFireballL2Recipe());
        registerRecipe(new ChainLightningL2Recipe());
        registerRecipe(new IceStormL2Recipe());
        registerRecipe(new AcidRainL2Recipe());
        registerRecipe(new ElectricNetL2Recipe());
        
        // Seviye 3 - Saldırı Bataryaları
        registerRecipe(new MeteorShowerL3Recipe());
        registerRecipe(new LightningStormL3Recipe());
        registerRecipe(new IceAgeL3Recipe());
        registerRecipe(new PoisonBombL3Recipe());
        registerRecipe(new ElectricStormL3Recipe());
        
        // Seviye 4 - Saldırı Bataryaları
        registerRecipe(new TeslaTowerL4Recipe());
        registerRecipe(new HellfireL4Recipe());
        registerRecipe(new IceFortressL4Recipe());
        registerRecipe(new DeathCloudL4Recipe());
        registerRecipe(new ElectricShieldL4Recipe());
        
        // Seviye 5 - Saldırı Bataryaları
        registerRecipe(new ApocalypseReactorL5Recipe());
        registerRecipe(new LavaTsunamiL5Recipe());
        registerRecipe(new BossKillerL5Recipe());
        registerRecipe(new AreaDestroyerL5Recipe());
        registerRecipe(new MountainDestroyerL5Recipe());
        
        // ========== OLUŞTURMA BATARYALARI (25 Batarya) ==========
        // Seviye 1 - Oluşturma Bataryaları
        registerRecipe(new StoneBridgeL1Recipe());
        registerRecipe(new ObsidianWallL1Recipe());
        registerRecipe(new IronCageL1Recipe());
        registerRecipe(new GlassWallL1Recipe());
        registerRecipe(new WoodBarricadeL1Recipe());
        
        // Seviye 2 - Oluşturma Bataryaları
        registerRecipe(new ObsidianCageL2Recipe());
        registerRecipe(new StoneBridgeAdvL2Recipe());
        registerRecipe(new IronWallL2Recipe());
        registerRecipe(new GlassTunnelL2Recipe());
        registerRecipe(new WoodCastleL2Recipe());
        
        // Seviye 3 - Oluşturma Bataryaları
        registerRecipe(new ObsidianCastleL3Recipe());
        registerRecipe(new NetheriteBridgeL3Recipe());
        registerRecipe(new IronPrisonL3Recipe());
        registerRecipe(new GlassTowerL3Recipe());
        registerRecipe(new StoneCastleL3Recipe());
        
        // Seviye 4 - Oluşturma Bataryaları
        registerRecipe(new ObsidianPrisonL4Recipe());
        registerRecipe(new NetheriteBridgeAdvL4Recipe());
        registerRecipe(new IronCastleL4Recipe());
        registerRecipe(new GlassTowerAdvL4Recipe());
        registerRecipe(new StoneFortressL4Recipe());
        
        // Seviye 5 - Oluşturma Bataryaları
        registerRecipe(new ObsidianPrisonLegL5Recipe());
        registerRecipe(new NetheriteBridgeLegL5Recipe());
        registerRecipe(new IronCastleLegL5Recipe());
        registerRecipe(new GlassTowerLegL5Recipe());
        registerRecipe(new StoneFortressLegL5Recipe());
        
        // ========== DESTEK BATARYALARI (25 Batarya) ==========
        // Seviye 1 - Destek Bataryaları
        registerRecipe(new HealL1Recipe());
        registerRecipe(new SpeedL1Recipe());
        registerRecipe(new DamageL1Recipe());
        registerRecipe(new ArmorL1Recipe());
        registerRecipe(new RegenerationL1Recipe());
        
        // Seviye 2 - Destek Bataryaları
        registerRecipe(new HealSpeedComboL2Recipe());
        registerRecipe(new DamageArmorComboL2Recipe());
        registerRecipe(new RegenerationHealComboL2Recipe());
        registerRecipe(new SpeedDamageComboL2Recipe());
        registerRecipe(new ArmorRegenerationComboL2Recipe());
        
        // Seviye 3 - Destek Bataryaları
        registerRecipe(new AbsorptionShieldL3Recipe());
        registerRecipe(new FlightL3Recipe());
        registerRecipe(new CriticalStrikeL3Recipe());
        registerRecipe(new ReflectionShieldL3Recipe());
        registerRecipe(new LifeStealL3Recipe());
        
        // Seviye 4 - Destek Bataryaları
        registerRecipe(new FullHealAbsorptionL4Recipe());
        registerRecipe(new TimeSlowL4Recipe());
        registerRecipe(new LightningStrikeL4Recipe());
        registerRecipe(new InvisibilityShieldL4Recipe());
        registerRecipe(new ImmortalityMomentL4Recipe());
        
        // Seviye 5 - Destek Bataryaları
        registerRecipe(new LegendaryHealL5Recipe());
        registerRecipe(new TimeStopL5Recipe());
        registerRecipe(new DeathTouchL5Recipe());
        registerRecipe(new PhaseShiftL5Recipe());
        registerRecipe(new RebirthL5Recipe());
    }
    
    private void registerRecipe(RecipeChecker checker) {
        recipeCheckers.put(checker.getBatteryName(), checker);
    }
    
    /**
     * Merkez bloktan başlayarak tüm tarifleri kontrol et
     * ÖNCE tıklanan bloğun hangi tarifin merkez bloğu olduğunu kontrol eder
     * SADECE tıklanan bloğun merkez bloğu olduğu tarifler kontrol edilir (çakışma önleme)
     */
    public RecipeCheckResult checkAllRecipes(Block centerBlock) {
        Material clickedMaterial = centerBlock.getType();
        
        // ÖNCE: Tıklanan bloğun hangi tarifin merkez bloğu olduğunu kontrol et
        List<RecipeChecker> matchingCenterCheckers = new ArrayList<>();
        for (RecipeChecker checker : recipeCheckers.values()) {
            BlockPattern pattern = checker.getPattern();
            if (pattern != null && pattern.getCenterBlock() == clickedMaterial) {
                matchingCenterCheckers.add(checker);
            }
        }
        
        // Eğer tıklanan blok hiçbir tarifin merkez bloğu değilse, hiçbir tarif eşleşmemeli
        if (matchingCenterCheckers.isEmpty()) {
            return RecipeCheckResult.failure("Tıklanan blok hiçbir tarifin merkez bloğu değil");
        }
        
        // SADECE tıklanan bloğun merkez bloğu olduğu tarifleri kontrol et
        for (RecipeChecker checker : matchingCenterCheckers) {
            RecipeCheckResult result = checker.checkRecipe(centerBlock);
            if (result.matches()) {
                return result;
            }
        }
        
        return RecipeCheckResult.failure("Tıklanan blok merkez bloğu ama tarif eşleşmedi");
    }
    
    /**
     * Belirli bir batarya tarifini kontrol et
     */
    public RecipeCheckResult checkRecipe(String batteryName, Block centerBlock) {
        RecipeChecker checker = recipeCheckers.get(batteryName);
        if (checker == null) {
            return RecipeCheckResult.failure("Batarya tarifi bulunamadı: " + batteryName);
        }
        return checker.checkRecipe(centerBlock);
    }
    
    /**
     * Batarya isminden seviyeyi al
     */
    public int getBatteryLevel(String batteryName) {
        RecipeChecker checker = recipeCheckers.get(batteryName);
        if (checker == null) return 1;
        return checker.getLevel();
    }
    
    /**
     * Tüm batarya isimlerini al
     */
    public List<String> getAllBatteryNames() {
        return new ArrayList<>(recipeCheckers.keySet());
    }
    
    /**
     * Seviyeye göre batarya isimlerini al
     */
    public List<String> getBatteryNamesByLevel(int level) {
        List<String> names = new ArrayList<>();
        for (Map.Entry<String, RecipeChecker> entry : recipeCheckers.entrySet()) {
            if (entry.getValue().getLevel() == level) {
                names.add(entry.getKey());
            }
        }
        return names;
    }
    
    /**
     * Tüm RecipeChecker'ları al (GhostRecipeManager için)
     */
    public Map<String, RecipeChecker> getAllRecipeCheckers() {
        return new HashMap<>(recipeCheckers);
    }
    
    /**
     * Batarya isminden RecipeChecker al
     */
    public RecipeChecker getRecipeChecker(String batteryName) {
        return recipeCheckers.get(batteryName);
    }
    
    /**
     * BlockPattern'i gerçek bloklara çevir (build için)
     */
    public static void buildPattern(Location centerLocation, BlockPattern pattern) {
        Block centerBlock = centerLocation.getBlock();
        
        // 1. Merkez blok
        centerBlock.setType(pattern.getCenterBlock());
        
        // 2. Tüm gerekli blokları yerleştir
        for (Map.Entry<BlockPosition, Material> entry : pattern.getRequiredBlocks().entrySet()) {
            BlockPosition pos = entry.getKey();
            Material material = entry.getValue();
            
            // Merkez bloktan göreceli konumu hesapla
            Location targetLocation = centerLocation.clone();
            targetLocation.add(pos.getX(), pos.getY(), pos.getZ());
            
            // Blok yerleştir
            targetLocation.getBlock().setType(material);
        }
    }
    
    /**
     * Bataryayı yükle
     */
    public void loadBattery(Player player, int slot, NewBatteryData data) {
        loadedBatteries.putIfAbsent(player.getUniqueId(), new HashMap<>());
        loadedBatteries.get(player.getUniqueId()).put(slot, data);
        player.sendMessage(ChatColor.GREEN + "⚡ " + data.getBatteryName() + " " + (slot + 1) + ". slota yüklendi!");
    }
    
    /**
     * Batarya var mı?
     */
    public boolean hasLoadedBattery(Player player, int slot) {
        return loadedBatteries.containsKey(player.getUniqueId()) &&
               loadedBatteries.get(player.getUniqueId()).containsKey(slot);
    }
    
    /**
     * Bataryayı al
     */
    public NewBatteryData getLoadedBattery(Player player, int slot) {
        if (!hasLoadedBattery(player, slot)) return null;
        return loadedBatteries.get(player.getUniqueId()).get(slot);
    }
    
    /**
     * Bataryayı kaldır
     */
    public void removeBattery(Player player, int slot) {
        if (loadedBatteries.containsKey(player.getUniqueId())) {
            loadedBatteries.get(player.getUniqueId()).remove(slot);
        }
    }
    
    /**
     * Bataryayı ateşle (ana metod)
     * ✅ MANTIK DÜZELTMESİ: Sadece başarılı batarya ateşlemelerinde güç ver
     */
    public void fireBattery(Player player, NewBatteryData data) {
        if (player == null || data == null) return;
        
        String batteryName = data.getBatteryName();
        boolean success = false;
        
        // Kategori belirleme (isimden)
        if (batteryName.contains("Yıldırım") || batteryName.contains("Cehennem") || 
            batteryName.contains("Buz") || batteryName.contains("Zehir") || 
            batteryName.contains("Şok") || batteryName.contains("Elektrik") ||
            batteryName.contains("Meteor") || batteryName.contains("Tesla") ||
            batteryName.contains("Ölüm") || batteryName.contains("Kıyamet") ||
            batteryName.contains("Lava") || batteryName.contains("Boss") ||
            batteryName.contains("Alan") || batteryName.contains("Dağ")) {
            fireAttackBattery(player, data);
            success = true; // Saldırı bataryaları genelde başarılı olur
        } else if (batteryName.contains("Köprü") || batteryName.contains("Duvar") ||
                   batteryName.contains("Kafes") || batteryName.contains("Kale") ||
                   batteryName.contains("Hapishane") || batteryName.contains("Kule") ||
                   batteryName.contains("Şato") || batteryName.contains("Barikat") ||
                   batteryName.contains("Tünel") || batteryName.contains("Obsidyen") ||
                   batteryName.contains("Netherite") || batteryName.contains("Demir") ||
                   batteryName.contains("Cam") || batteryName.contains("Taş") ||
                   batteryName.contains("Ahşap")) {
            fireConstructionBattery(player, data);
            success = true; // Oluşturma bataryaları genelde başarılı olur
        } else {
            // Destek bataryaları
            fireSupportBattery(player, data);
            success = true; // Destek bataryaları genelde başarılı olur
        }
        
        // ✅ GÜÇ SİSTEMİ ENTEGRASYONU: Sadece başarılı batarya ateşlemelerinde güç ver
        // Not: Batarya sisteminde başarısızlık durumu yok gibi görünüyor, 
        // ama gelecekte eklenebilir diye kontrol ekliyoruz
        if (success) {
            me.mami.stratocraft.manager.TerritoryManager territoryManager = plugin.getTerritoryManager();
            if (territoryManager != null) {
                me.mami.stratocraft.model.Clan clan = territoryManager.getTerritoryOwner(player.getLocation());
                // ✅ NULL KONTROLÜ: Klan yoksa oyuncunun klanını kontrol et
                if (clan == null && plugin.getClanManager() != null) {
                    clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
                }
                
                if (clan != null && plugin.getStratocraftPowerSystem() != null) {
                    // Batarya yakıt tipine göre kaynak belirle
                    java.util.Map<String, Integer> usedResources = new java.util.HashMap<>();
                    org.bukkit.Material fuel = data.getFuel();
                    
                    if (fuel == org.bukkit.Material.IRON_INGOT) {
                        usedResources.put("IRON", 1);
                    } else if (fuel == org.bukkit.Material.DIAMOND) {
                        usedResources.put("DIAMOND", 1);
                    } else if (fuel == org.bukkit.Material.EMERALD) {
                        usedResources.put("RED_DIAMOND", 1); // Kızıl elmas (emerald olarak kullanılıyor)
                    } else if (data.isDarkMatter()) {
                        usedResources.put("DARK_MATTER", 1);
                    } else {
                        usedResources.put("DEFAULT", 1);
                    }
                    
                    plugin.getStratocraftPowerSystem().onRitualSuccess(
                        clan,
                        "BATTERY_" + batteryName.replace(" ", "_").toUpperCase(),
                        usedResources
                    );
                }
            }
        }
    }
    
    /**
     * Saldırı bataryalarını ateşle
     */
    private void fireAttackBattery(Player player, NewBatteryData data) {
        String batteryName = data.getBatteryName();
        Location target = getTargetLocation(player, 50);
        int level = data.getBatteryLevel();
        double fuelMultiplier = getFuelMultiplier(data);
        double trainingMultiplier = data.getTrainingMultiplier();
        double totalMultiplier = fuelMultiplier * trainingMultiplier;
        
        // Seviye çarpanı
        double levelMultiplier = 1.0 + (level - 1) * 0.3; // L1: 1.0x, L2: 1.3x, L3: 1.6x, L4: 1.9x, L5: 2.2x
        
        // Final çarpan
        double finalMultiplier = totalMultiplier * levelMultiplier;
        
        // Batarya ismine göre özel ateşleme
        switch (batteryName) {
            // ========== SEVİYE 1 SALDIRI BATARYALARI ==========
            case "Yıldırım Asası":
                fireLightningStaff(player, target, finalMultiplier, level);
                break;
            case "Cehennem Topu":
                fireHellfireBall(player, target, finalMultiplier, level);
                break;
            case "Buz Topu":
                fireIceBall(player, target, finalMultiplier, level);
                break;
            case "Zehir Oku":
                firePoisonArrow(player, target, finalMultiplier, level);
                break;
            case "Şok Dalgası":
                fireShockWave(player, target, finalMultiplier, level);
                break;
            
            // ========== SEVİYE 2 SALDIRI BATARYALARI ==========
            case "Çift Ateş Topu":
                fireDoubleFireball(player, target, finalMultiplier, level);
                break;
            case "Zincir Yıldırım":
                fireChainLightning(player, target, finalMultiplier, level);
                break;
            case "Buz Fırtınası":
                fireIceStorm(player, target, finalMultiplier, level);
                break;
            case "Asit Yağmuru":
                fireAcidRain(player, target, finalMultiplier, level);
                break;
            case "Elektrik Ağı":
                fireElectricNet(player, target, finalMultiplier, level);
                break;
            
            // ========== SEVİYE 3 SALDIRI BATARYALARI ==========
            case "Meteor Yağmuru":
                fireMeteorShower(player, target, finalMultiplier, level);
                break;
            case "Yıldırım Fırtınası":
                fireLightningStorm(player, target, finalMultiplier, level);
                break;
            case "Buz Çağı":
                fireIceAge(player, target, finalMultiplier, level);
                break;
            case "Zehir Bombası":
                firePoisonBomb(player, target, finalMultiplier, level);
                break;
            case "Elektrik Fırtınası":
                fireElectricStorm(player, target, finalMultiplier, level);
                break;
            
            // ========== SEVİYE 4 SALDIRI BATARYALARI ==========
            case "Tesla Kulesi":
                fireTeslaTower(player, target, finalMultiplier, level);
                break;
            case "Cehennem Ateşi":
                fireHellfire(player, target, finalMultiplier, level);
                break;
            case "Buz Kalesi":
                fireIceFortress(player, target, finalMultiplier, level);
                break;
            case "Ölüm Bulutu":
                fireDeathCloud(player, target, finalMultiplier, level);
                break;
            case "Elektrik Kalkanı":
                fireElectricShield(player, target, finalMultiplier, level);
                break;
            
            // ========== SEVİYE 5 SALDIRI BATARYALARI ==========
            case "Kıyamet Reaktörü":
                fireApocalypseReactor(player, target, finalMultiplier, level);
                break;
            case "Lava Tufanı":
                fireLavaTsunami(player, target, finalMultiplier, level);
                break;
            case "Boss Katili":
                fireBossKiller(player, target, finalMultiplier, level);
                break;
            case "Alan Yok Edici":
                fireAreaDestroyer(player, target, finalMultiplier, level);
                break;
            case "Dağ Yok Edici":
                fireMountainDestroyer(player, target, finalMultiplier, level);
                break;
            
            default:
                player.sendMessage("§cBilinmeyen batarya: " + batteryName);
                break;
        }
    }
    
    /**
     * Batarya ismine göre BatteryType bul
     */
    private BatteryManager.BatteryType findBatteryTypeByName(String batteryName) {
        for (BatteryManager.BatteryType type : BatteryManager.BatteryType.values()) {
            if (type.getDisplayName().equals(batteryName)) {
                return type;
            }
        }
        return null;
    }
    
    /**
     * Oluşturma bataryalarını ateşle
     */
    private void fireConstructionBattery(Player player, NewBatteryData data) {
        String batteryName = data.getBatteryName();
        Location target = getTargetLocation(player, 30);
        int level = data.getBatteryLevel();
        double fuelMultiplier = getFuelMultiplier(data);
        double trainingMultiplier = data.getTrainingMultiplier();
        double totalMultiplier = fuelMultiplier * trainingMultiplier;
        double levelMultiplier = 1.0 + (level - 1) * 0.3;
        double finalMultiplier = totalMultiplier * levelMultiplier;
        
        // Batarya ismine göre özel oluşturma
        switch (batteryName) {
            // ========== SEVİYE 1 OLUŞTURMA BATARYALARI ==========
            case "Taş Köprü":
                createStoneBridge(player, target, (int)(10 * finalMultiplier), level);
                break;
            case "Obsidyen Duvar":
                createObsidianWall(player, target, (int)(5 * finalMultiplier), (int)(5 * finalMultiplier), (int)(3 * finalMultiplier), level);
                break;
            case "Demir Kafes":
                createIronCage(player, target, (int)(5 * finalMultiplier), (int)(5 * finalMultiplier), (int)(5 * finalMultiplier), level);
                break;
            case "Cam Duvar":
                createGlassWall(player, target, (int)(5 * finalMultiplier), (int)(5 * finalMultiplier), (int)(3 * finalMultiplier), level);
                break;
            case "Ahşap Barikat":
                createWoodBarricade(player, target, (int)(5 * finalMultiplier), (int)(5 * finalMultiplier), (int)(2 * finalMultiplier), level);
                break;
            
            // ========== SEVİYE 2 OLUŞTURMA BATARYALARI ==========
            case "Obsidyen Kafes":
                createObsidianCage(player, target, (int)(10 * finalMultiplier), (int)(10 * finalMultiplier), (int)(5 * finalMultiplier), level);
                break;
            case "Taş Köprü (Gelişmiş)":
                createStoneBridge(player, target, (int)(20 * finalMultiplier), level);
                break;
            case "Demir Duvar":
                createIronWall(player, target, (int)(10 * finalMultiplier), (int)(5 * finalMultiplier), (int)(3 * finalMultiplier), level);
                break;
            case "Cam Tünel":
                createGlassTunnel(player, target, (int)(15 * finalMultiplier), level);
                break;
            case "Ahşap Kale":
                createWoodCastle(player, target, (int)(10 * finalMultiplier), (int)(10 * finalMultiplier), (int)(5 * finalMultiplier), level);
                break;
            
            // ========== SEVİYE 3 OLUŞTURMA BATARYALARI ==========
            case "Obsidyen Kale":
                createObsidianCastle(player, target, (int)(20 * finalMultiplier), (int)(20 * finalMultiplier), (int)(10 * finalMultiplier), level);
                break;
            case "Netherite Köprü":
                createNetheriteBridge(player, target, (int)(30 * finalMultiplier), level);
                break;
            case "Demir Hapishane":
                createIronPrison(player, target, (int)(15 * finalMultiplier), (int)(15 * finalMultiplier), (int)(8 * finalMultiplier), level);
                break;
            case "Cam Kule":
                createGlassTower(player, target, (int)(10 * finalMultiplier), (int)(10 * finalMultiplier), (int)(15 * finalMultiplier), level);
                break;
            case "Taş Kale":
                createStoneCastle(player, target, (int)(15 * finalMultiplier), (int)(15 * finalMultiplier), (int)(10 * finalMultiplier), level);
                break;
            
            // ========== SEVİYE 4 OLUŞTURMA BATARYALARI ==========
            case "Obsidyen Hapishane":
                createObsidianPrison(player, target, (int)(25 * finalMultiplier), (int)(25 * finalMultiplier), (int)(15 * finalMultiplier), level);
                break;
            case "Netherite Köprü (Gelişmiş)":
                createNetheriteBridge(player, target, (int)(50 * finalMultiplier), level);
                break;
            case "Demir Kale":
                createIronCastle(player, target, (int)(20 * finalMultiplier), (int)(20 * finalMultiplier), (int)(15 * finalMultiplier), level);
                break;
            case "Cam Kule (Gelişmiş)":
                createGlassTower(player, target, (int)(15 * finalMultiplier), (int)(15 * finalMultiplier), (int)(20 * finalMultiplier), level);
                break;
            case "Taş Şato":
                createStoneFortress(player, target, (int)(25 * finalMultiplier), (int)(25 * finalMultiplier), (int)(15 * finalMultiplier), level);
                break;
            
            // ========== SEVİYE 5 OLUŞTURMA BATARYALARI ==========
            case "Obsidyen Hapishane (Efsanevi)":
                createObsidianPrison(player, target, (int)(50 * finalMultiplier), (int)(50 * finalMultiplier), (int)(20 * finalMultiplier), level);
                break;
            case "Netherite Köprü (Efsanevi)":
                createNetheriteBridge(player, target, (int)(100 * finalMultiplier), level);
                break;
            case "Demir Kale (Efsanevi)":
                createIronCastle(player, target, (int)(40 * finalMultiplier), (int)(40 * finalMultiplier), (int)(20 * finalMultiplier), level);
                break;
            case "Cam Kule (Efsanevi)":
                createGlassTower(player, target, (int)(20 * finalMultiplier), (int)(20 * finalMultiplier), (int)(30 * finalMultiplier), level);
                break;
            case "Taş Kalesi (Efsanevi)":
                createStoneFortress(player, target, (int)(50 * finalMultiplier), (int)(50 * finalMultiplier), (int)(25 * finalMultiplier), level);
                break;
            
            default:
                player.sendMessage("§cBilinmeyen oluşturma bataryası: " + batteryName);
                break;
        }
    }
    
    /**
     * Destek bataryalarını ateşle
     */
    private void fireSupportBattery(Player player, NewBatteryData data) {
        String batteryName = data.getBatteryName();
        int level = data.getBatteryLevel();
        double fuelMultiplier = getFuelMultiplier(data);
        double trainingMultiplier = data.getTrainingMultiplier();
        double totalMultiplier = fuelMultiplier * trainingMultiplier;
        double levelMultiplier = 1.0 + (level - 1) * 0.3;
        double finalMultiplier = totalMultiplier * levelMultiplier;
        double radius = (5.0 + (level * 2.0)) * finalMultiplier;
        
        // Batarya ismine göre özel destek
        switch (batteryName) {
            // ========== SEVİYE 1 DESTEK BATARYALARI ==========
            case "Can Yenileme":
                applyHealSupport(player, radius, 5.0 * finalMultiplier, level);
                break;
            case "Hız Artışı":
                applySpeedSupport(player, radius, 1, (int)(10 * finalMultiplier), level);
                break;
            case "Hasar Artışı":
                applyDamageSupport(player, radius, 1, (int)(10 * finalMultiplier), level);
                break;
            case "Zırh Artışı":
                applyArmorSupport(player, radius, 1, (int)(10 * finalMultiplier), level);
                break;
            case "Yenilenme":
                applyRegenerationSupport(player, radius, 1, (int)(10 * finalMultiplier), level);
                break;
            
            // ========== SEVİYE 2 DESTEK BATARYALARI (Kombinasyonlar) ==========
            case "Can + Hız Kombinasyonu":
                applyHealSupport(player, radius, 5.0 * finalMultiplier, level);
                applySpeedSupport(player, radius, 1, (int)(15 * finalMultiplier), level);
                break;
            case "Hasar + Zırh Kombinasyonu":
                applyDamageSupport(player, radius, 1, (int)(15 * finalMultiplier), level);
                applyArmorSupport(player, radius, 1, (int)(15 * finalMultiplier), level);
                break;
            case "Yenilenme + Can Kombinasyonu":
                applyRegenerationSupport(player, radius, 1, (int)(15 * finalMultiplier), level);
                applyHealSupport(player, radius, 3.0 * finalMultiplier, level);
                break;
            case "Hız + Hasar Kombinasyonu":
                applySpeedSupport(player, radius, 1, (int)(15 * finalMultiplier), level);
                applyDamageSupport(player, radius, 1, (int)(15 * finalMultiplier), level);
                break;
            case "Zırh + Yenilenme Kombinasyonu":
                applyArmorSupport(player, radius, 1, (int)(15 * finalMultiplier), level);
                applyRegenerationSupport(player, radius, 1, (int)(15 * finalMultiplier), level);
                break;
            
            // ========== SEVİYE 3 DESTEK BATARYALARI ==========
            case "Absorption Kalkanı":
                applyAbsorptionShield(player, radius, (int)(20 * finalMultiplier), level);
                break;
            case "Uçma Yeteneği":
                applyFlight(player, radius, (int)(10 * finalMultiplier), level);
                break;
            case "Kritik Vuruş Artışı":
                applyCriticalStrike(player, radius, (int)(20 * finalMultiplier), level);
                break;
            case "Yansıtma Kalkanı":
                applyReflectionShield(player, radius, (int)(20 * finalMultiplier), level);
                break;
            case "Can Çalma":
                applyLifeSteal(player, radius, (int)(20 * finalMultiplier), level);
                break;
            
            // ========== SEVİYE 4 DESTEK BATARYALARI ==========
            case "Tam Can + Absorption":
                applyFullHealAbsorption(player, radius, level);
                break;
            case "Zaman Yavaşlatma":
                applyTimeSlow(player, radius, (int)(30 * finalMultiplier), level);
                break;
            case "Yıldırım Vuruşu":
                applyLightningStrike(player, radius, (int)(30 * finalMultiplier), level);
                break;
            case "Görünmezlik Kalkanı":
                applyInvisibilityShield(player, radius, (int)(30 * finalMultiplier), level);
                break;
            case "Ölümsüzlük Anı":
                applyImmortalityMoment(player, radius, level);
                break;
            
            // ========== SEVİYE 5 DESTEK BATARYALARI ==========
            case "Efsanevi Can Yenileme":
                applyLegendaryHeal(player, radius, (int)(60 * finalMultiplier), level);
                break;
            case "Zaman Durdurma":
                applyTimeStop(player, radius, (int)(10 * finalMultiplier), level);
                break;
            case "Ölüm Dokunuşu":
                applyDeathTouch(player, radius, (int)(60 * finalMultiplier), level);
                break;
            case "Faz Değiştirme":
                applyPhaseShift(player, radius, (int)(5 * finalMultiplier), level);
                break;
            case "Yeniden Doğuş":
                applyRebirth(player, radius, level);
                break;
            
            default:
                player.sendMessage("§cBilinmeyen destek bataryası: " + batteryName);
                break;
        }
    }
    
    /**
     * Yakıt çarpanını hesapla
     */
    private double getFuelMultiplier(NewBatteryData data) {
        if (data.isDarkMatter()) return 10.0;
        if (data.isRedDiamond()) return 5.0;
        if (data.getFuel() == Material.DIAMOND) return 2.5;
        return 1.0; // IRON_INGOT
    }
    
    /**
     * RayTrace ile hedef bul
     */
    private Location getTargetLocation(Player player, int maxDistance) {
        org.bukkit.util.RayTraceResult result = player.rayTraceBlocks(maxDistance);
        if (result != null && result.getHitBlock() != null) {
            return result.getHitBlock().getLocation();
        }
        org.bukkit.util.Vector direction = player.getLocation().getDirection().normalize();
        return player.getLocation().add(direction.multiply(maxDistance));
    }
    
    // ========== SALDIRI BATARYA ATEŞLEME METODLARI ==========
    
    /**
     * Yıldırım Asası L1: Manuel nişanlı tek nokta yıldırım
     */
    private void fireLightningStaff(Player player, Location target, double multiplier, int level) {
        double damage = 5.0 * multiplier;
        int radius = (int)(5 * multiplier);
        
        player.getWorld().strikeLightning(target);
        player.getWorld().spawnParticle(org.bukkit.Particle.ELECTRIC_SPARK, target, 20, 1.0, 1.0, 1.0, 0.1);
        
        for (org.bukkit.entity.Entity entity : player.getWorld().getNearbyEntities(target, radius, radius, radius)) {
            if (entity instanceof org.bukkit.entity.LivingEntity && entity != player) {
                ((org.bukkit.entity.LivingEntity) entity).damage(damage);
            }
        }
        
        player.sendMessage("§e⚡ Yıldırım Asası ateşlendi! (Hasar: " + String.format("%.1f", damage) + ")");
    }
    
    /**
     * Cehennem Topu L1: Düz atış ateş topu
     */
    private void fireHellfireBall(Player player, Location target, double multiplier, int level) {
        double damage = 2.0 * multiplier;
        int radius = (int)(3 * multiplier);
        
        org.bukkit.entity.Fireball fireball = player.getWorld().spawn(target, org.bukkit.entity.Fireball.class);
        fireball.setDirection(player.getLocation().getDirection());
        fireball.setYield((float) (damage / 2.0));
        fireball.setIsIncendiary(true);
        
        for (org.bukkit.entity.Entity entity : player.getWorld().getNearbyEntities(target, radius, radius, radius)) {
            if (entity instanceof org.bukkit.entity.LivingEntity && entity != player) {
                ((org.bukkit.entity.LivingEntity) entity).damage(damage);
                entity.setFireTicks(100);
            }
        }
        
        player.sendMessage("§c🔥 Cehennem Topu ateşlendi! (Hasar: " + String.format("%.1f", damage) + ")");
    }
    
    /**
     * Buz Topu L1: Düz atış buz topu (yavaşlatma)
     */
    private void fireIceBall(Player player, Location target, double multiplier, int level) {
        double damage = 2.0 * multiplier;
        int radius = (int)(5 * multiplier);
        
        player.getWorld().spawnParticle(org.bukkit.Particle.SNOWBALL, target, 20, 0.5, 0.5, 0.5, 0.1);
        player.getWorld().playSound(target, org.bukkit.Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f);
        
        for (org.bukkit.entity.Entity entity : player.getWorld().getNearbyEntities(target, radius, radius, radius)) {
            if (entity instanceof org.bukkit.entity.LivingEntity && entity != player) {
                ((org.bukkit.entity.LivingEntity) entity).damage(damage);
                ((org.bukkit.entity.LivingEntity) entity).addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.SLOW, 60, 1, false, false, true));
            }
        }
        
        player.sendMessage("§b❄ Buz Topu ateşlendi! (Hasar: " + String.format("%.1f", damage) + ")");
    }
    
    /**
     * Zehir Oku L1: Zehirli ok atışı
     */
    private void firePoisonArrow(Player player, Location target, double multiplier, int level) {
        double damage = 2.0 * multiplier;
        int radius = (int)(8 * multiplier);
        int duration = (int)(3 * multiplier);
        
        org.bukkit.entity.Arrow arrow = player.getWorld().spawn(target, org.bukkit.entity.Arrow.class);
        arrow.setVelocity(player.getLocation().getDirection().multiply(2.0));
        arrow.setCritical(true);
        
        for (org.bukkit.entity.Entity entity : player.getWorld().getNearbyEntities(target, radius, radius, radius)) {
            if (entity instanceof org.bukkit.entity.LivingEntity && entity != player) {
                ((org.bukkit.entity.LivingEntity) entity).damage(damage);
                ((org.bukkit.entity.LivingEntity) entity).addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.POISON, duration * 20, 0, false, false, true));
            }
        }
        
        player.sendMessage("§2☠ Zehir Oku ateşlendi! (Hasar: " + String.format("%.1f", damage) + ")");
    }
    
    /**
     * Şok Dalgası L1: Elektrik şok dalgası (dairesel)
     */
    private void fireShockWave(Player player, Location target, double multiplier, int level) {
        double damage = 3.0 * multiplier;
        int radius = (int)(4 * multiplier);
        
        player.getWorld().spawnParticle(org.bukkit.Particle.ELECTRIC_SPARK, target, 30, 1.0, 1.0, 1.0, 0.1);
        player.getWorld().playSound(target, org.bukkit.Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
        
        for (org.bukkit.entity.Entity entity : player.getWorld().getNearbyEntities(target, radius, radius, radius)) {
            if (entity instanceof org.bukkit.entity.LivingEntity && entity != player) {
                ((org.bukkit.entity.LivingEntity) entity).damage(damage);
                player.getWorld().spawnParticle(org.bukkit.Particle.ELECTRIC_SPARK, entity.getLocation(), 5, 0.3, 0.3, 0.3, 0.05);
            }
        }
        
        player.sendMessage("§e⚡ Şok Dalgası ateşlendi! (Hasar: " + String.format("%.1f", damage) + ")");
    }
    
    /**
     * Çift Ateş Topu L2: İki ateş topu paralel atış
     */
    private void fireDoubleFireball(Player player, Location target, double multiplier, int level) {
        double damage = 4.0 * multiplier;
        int radius = (int)(5 * multiplier);
        int fireballCount = (int)(2 * multiplier);
        
        org.bukkit.util.Vector direction = player.getLocation().getDirection();
        org.bukkit.util.Vector perpendicular = new org.bukkit.util.Vector(-direction.getZ(), 0, direction.getX()).normalize().multiply(1.5);
        
        for (int i = 0; i < fireballCount; i++) {
            Location fireballLoc = target.clone();
            if (i > 0) {
                fireballLoc.add(perpendicular.clone().multiply((i % 2 == 0) ? 1 : -1));
            }
            
            org.bukkit.entity.Fireball fireball = player.getWorld().spawn(fireballLoc, org.bukkit.entity.Fireball.class);
            fireball.setDirection(direction);
            fireball.setYield((float) (damage / 2.0));
            
            for (org.bukkit.entity.Entity entity : player.getWorld().getNearbyEntities(fireballLoc, radius, radius, radius)) {
                if (entity instanceof org.bukkit.entity.LivingEntity && entity != player) {
                    ((org.bukkit.entity.LivingEntity) entity).damage(damage / fireballCount);
                    entity.setFireTicks(100);
                }
            }
        }
        
        player.sendMessage("§c🔥 Çift Ateş Topu ateşlendi! (Hasar: " + String.format("%.1f", damage) + ")");
    }
    
    /**
     * Zincir Yıldırım L2: Zincirleme yıldırım (3 hedef)
     */
    private void fireChainLightning(Player player, Location target, double multiplier, int level) {
        double damage = 4.0 * multiplier;
        int radius = (int)(8 * multiplier);
        int chainCount = (int)(3 * multiplier);
        
        Location currentTarget = target;
        
        for (int i = 0; i < chainCount; i++) {
            player.getWorld().strikeLightning(currentTarget);
            player.getWorld().spawnParticle(org.bukkit.Particle.ELECTRIC_SPARK, currentTarget, 20, 1.0, 1.0, 1.0, 0.1);
            
            for (org.bukkit.entity.Entity entity : player.getWorld().getNearbyEntities(currentTarget, radius, radius, radius)) {
                if (entity instanceof org.bukkit.entity.LivingEntity && entity != player) {
                    ((org.bukkit.entity.LivingEntity) entity).damage(damage / chainCount);
                }
            }
            
            // Sonraki hedef bul
            org.bukkit.entity.Entity nearest = null;
            double nearestDist = Double.MAX_VALUE;
            for (org.bukkit.entity.Entity entity : player.getWorld().getNearbyEntities(currentTarget, 10, 10, 10)) {
                if (entity instanceof org.bukkit.entity.LivingEntity && entity != player && entity != nearest) {
                    double dist = entity.getLocation().distance(currentTarget);
                    if (dist < nearestDist) {
                        nearestDist = dist;
                        nearest = entity;
                    }
                }
            }
            
            if (nearest != null) {
                currentTarget = nearest.getLocation();
            } else {
                break;
            }
        }
        
        player.sendMessage("§e⚡ Zincir Yıldırım ateşlendi! (Zincir: " + chainCount + ")");
    }
    
    /**
     * Buz Fırtınası L2: Çoklu buz topu
     */
    private void fireIceStorm(Player player, Location target, double multiplier, int level) {
        double damage = 6.0 * multiplier;
        int radius = (int)(7 * multiplier);
        int iceBallCount = (int)(10 * multiplier);
        
        for (int i = 0; i < iceBallCount; i++) {
            Location randomLoc = target.clone().add(
                (Math.random() - 0.5) * radius * 2,
                Math.random() * 5,
                (Math.random() - 0.5) * radius * 2
            );
            
            player.getWorld().spawnParticle(org.bukkit.Particle.SNOWBALL, randomLoc, 5, 0.3, 0.3, 0.3, 0.05);
            
            for (org.bukkit.entity.Entity entity : player.getWorld().getNearbyEntities(randomLoc, radius / 2, radius / 2, radius / 2)) {
                if (entity instanceof org.bukkit.entity.LivingEntity && entity != player) {
                    ((org.bukkit.entity.LivingEntity) entity).damage(damage / iceBallCount);
                    ((org.bukkit.entity.LivingEntity) entity).addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.SLOW, 40, 1, false, false, true));
                }
            }
        }
        
        player.sendMessage("§b❄ Buz Fırtınası ateşlendi! (Top sayısı: " + iceBallCount + ")");
    }
    
    /**
     * Asit Yağmuru L2: Sürekli zehir alanı
     */
    private void fireAcidRain(Player player, Location target, double multiplier, int level) {
        int radius = (int)(5 * multiplier);
        int duration = (int)(5 * multiplier);
        
        new org.bukkit.scheduler.BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= duration) {
                    cancel();
                    return;
                }
                
                for (org.bukkit.entity.Entity entity : player.getWorld().getNearbyEntities(target, radius, radius, radius)) {
                    if (entity instanceof org.bukkit.entity.LivingEntity && entity != player) {
                        ((org.bukkit.entity.LivingEntity) entity).addPotionEffect(new org.bukkit.potion.PotionEffect(
                            org.bukkit.potion.PotionEffectType.POISON, 20, 0, false, false, true));
                        ((org.bukkit.entity.LivingEntity) entity).damage(1.0 * multiplier);
                    }
                }
                
                player.getWorld().spawnParticle(org.bukkit.Particle.DRIP_LAVA, target, 10, radius, 5, radius, 0.1);
                count++;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        
        player.sendMessage("§2☠ Asit Yağmuru başladı! (Süre: " + duration + " saniye)");
    }
    
    /**
     * Elektrik Ağı L2: Çoklu şok
     */
    private void fireElectricNet(Player player, Location target, double multiplier, int level) {
        double damage = 5.0 * multiplier;
        int radius = (int)(5 * multiplier);
        int shockCount = (int)(5 * multiplier);
        
        for (int i = 0; i < shockCount; i++) {
            Location randomLoc = target.clone().add(
                (Math.random() - 0.5) * radius * 2,
                Math.random() * 3,
                (Math.random() - 0.5) * radius * 2
            );
            
            player.getWorld().spawnParticle(org.bukkit.Particle.ELECTRIC_SPARK, randomLoc, 10, 0.5, 0.5, 0.5, 0.1);
            player.getWorld().playSound(randomLoc, org.bukkit.Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.5f);
            
            for (org.bukkit.entity.Entity entity : player.getWorld().getNearbyEntities(randomLoc, radius / 2, radius / 2, radius / 2)) {
                if (entity instanceof org.bukkit.entity.LivingEntity && entity != player) {
                    ((org.bukkit.entity.LivingEntity) entity).damage(damage / shockCount);
                }
            }
        }
        
        player.sendMessage("§e⚡ Elektrik Ağı ateşlendi! (Şok sayısı: " + shockCount + ")");
    }
    
    /**
     * Meteor Yağmuru L3: Çoklu meteor düşüşü (60 hasar)
     */
    private void fireMeteorShower(Player player, Location target, double multiplier, int level) {
        double damage = 60.0 * multiplier;
        int radius = (int)(10 * multiplier);
        int meteorCount = (int)(5 * multiplier);
        
        for (int i = 0; i < meteorCount; i++) {
            Location meteorLoc = target.clone().add(
                (Math.random() - 0.5) * radius * 2,
                20 + Math.random() * 10,
                (Math.random() - 0.5) * radius * 2
            );
            
            new org.bukkit.scheduler.BukkitRunnable() {
                Location currentLoc = meteorLoc.clone();
                @Override
                public void run() {
                    if (currentLoc.getY() <= target.getY()) {
                        // Patlama
                        player.getWorld().createExplosion(currentLoc, (float)(3.0 * multiplier), false, false);
                        player.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_LARGE, currentLoc, 1);
                        
                        // Hasar
                        for (org.bukkit.entity.Entity entity : player.getWorld().getNearbyEntities(currentLoc, 5, 5, 5)) {
                            if (entity instanceof org.bukkit.entity.LivingEntity && entity != player) {
                                ((org.bukkit.entity.LivingEntity) entity).damage(damage / meteorCount);
                            }
                        }
                        
                        cancel();
                        return;
                    }
                    
                    // Meteor düşüşü
                    currentLoc.add(0, -1, 0);
                    player.getWorld().spawnParticle(org.bukkit.Particle.FLAME, currentLoc, 5, 0.3, 0.3, 0.3, 0.05);
                }
            }.runTaskTimer(plugin, i * 10L, 2L);
        }
        
        player.sendMessage("§c☄ Meteor Yağmuru başladı! (Meteor sayısı: " + meteorCount + ")");
    }
    
    /**
     * Yıldırım Fırtınası L3: Sürekli yıldırım (50 hasar)
     */
    private void fireLightningStorm(Player player, Location target, double multiplier, int level) {
        double damage = 50.0 * multiplier;
        int radius = (int)(7 * multiplier);
        int duration = (int)(5 * multiplier);
        
        new org.bukkit.scheduler.BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= duration) {
                    cancel();
                    return;
                }
                
                Location randomLoc = target.clone().add(
                    (Math.random() - 0.5) * radius * 2,
                    0,
                    (Math.random() - 0.5) * radius * 2
                );
                
                player.getWorld().strikeLightning(randomLoc);
                
                for (org.bukkit.entity.Entity entity : player.getWorld().getNearbyEntities(randomLoc, radius / 2, radius / 2, radius / 2)) {
                    if (entity instanceof org.bukkit.entity.LivingEntity && entity != player) {
                        ((org.bukkit.entity.LivingEntity) entity).damage(damage / duration);
                    }
                }
                
                count++;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        
        player.sendMessage("§e⚡ Yıldırım Fırtınası başladı! (Süre: " + duration + " saniye)");
    }
    
    /**
     * Buz Çağı L3: Sürekli dondurma (70 hasar)
     */
    private void fireIceAge(Player player, Location target, double multiplier, int level) {
        double damage = 70.0 * multiplier;
        int radius = (int)(15 * multiplier);
        int duration = (int)(10 * multiplier);
        
        new org.bukkit.scheduler.BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= duration) {
                    cancel();
                    return;
                }
                
                for (org.bukkit.entity.Entity entity : player.getWorld().getNearbyEntities(target, radius, radius, radius)) {
                    if (entity instanceof org.bukkit.entity.LivingEntity && entity != player) {
                        ((org.bukkit.entity.LivingEntity) entity).damage(damage / duration);
                        ((org.bukkit.entity.LivingEntity) entity).addPotionEffect(new org.bukkit.potion.PotionEffect(
                            org.bukkit.potion.PotionEffectType.SLOW, 40, 2, false, false, true));
                    }
                }
                
                player.getWorld().spawnParticle(org.bukkit.Particle.SNOWBALL, target, 50, radius, 5, radius, 0.1);
                count++;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        
        player.sendMessage("§b❄ Buz Çağı başladı! (Süre: " + duration + " saniye)");
    }
    
    /**
     * Zehir Bombası L3: Büyük alan zehir (55 hasar)
     */
    private void firePoisonBomb(Player player, Location target, double multiplier, int level) {
        int radius = (int)(8 * multiplier);
        int duration = (int)(10 * multiplier);
        
        player.getWorld().spawnParticle(org.bukkit.Particle.DRAGON_BREATH, target, 100, radius, 5, radius, 0.1);
        player.getWorld().playSound(target, org.bukkit.Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.5f);
        
        new org.bukkit.scheduler.BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= duration) {
                    cancel();
                    return;
                }
                
                for (org.bukkit.entity.Entity entity : player.getWorld().getNearbyEntities(target, radius, radius, radius)) {
                    if (entity instanceof org.bukkit.entity.LivingEntity && entity != player) {
                        ((org.bukkit.entity.LivingEntity) entity).addPotionEffect(new org.bukkit.potion.PotionEffect(
                            org.bukkit.potion.PotionEffectType.POISON, 40, 1, false, false, true));
                        ((org.bukkit.entity.LivingEntity) entity).damage(55.0 * multiplier / duration);
                    }
                }
                
                count++;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        
        player.sendMessage("§2☠ Zehir Bombası patladı! (Süre: " + duration + " saniye)");
    }
    
    /**
     * Elektrik Fırtınası L3: Sürekli şok alanı (65 hasar)
     */
    private void fireElectricStorm(Player player, Location target, double multiplier, int level) {
        double damage = 65.0 * multiplier;
        int radius = (int)(10 * multiplier);
        int duration = (int)(8 * multiplier);
        
        new org.bukkit.scheduler.BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= duration) {
                    cancel();
                    return;
                }
                
                for (org.bukkit.entity.Entity entity : player.getWorld().getNearbyEntities(target, radius, radius, radius)) {
                    if (entity instanceof org.bukkit.entity.LivingEntity && entity != player) {
                        ((org.bukkit.entity.LivingEntity) entity).damage(damage / duration);
                        player.getWorld().spawnParticle(org.bukkit.Particle.ELECTRIC_SPARK, entity.getLocation(), 5, 0.3, 0.3, 0.3, 0.05);
                    }
                }
                
                player.getWorld().spawnParticle(org.bukkit.Particle.ELECTRIC_SPARK, target, 50, radius, 5, radius, 0.1);
                count++;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        
        player.sendMessage("§e⚡ Elektrik Fırtınası başladı! (Süre: " + duration + " saniye)");
    }
    
    /**
     * Tesla Kulesi L4: Otomatik alan yıldırım (100 hasar)
     */
    private void fireTeslaTower(Player player, Location target, double multiplier, int level) {
        double damage = 100.0 * multiplier;
        int radius = (int)(30 * multiplier);
        int duration = (int)(30 * multiplier);
        
        new org.bukkit.scheduler.BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= duration) {
                    cancel();
                    return;
                }
                
                // En yakın düşmanı bul
                org.bukkit.entity.LivingEntity nearest = null;
                double nearestDist = Double.MAX_VALUE;
                for (org.bukkit.entity.Entity entity : player.getWorld().getNearbyEntities(target, radius, radius, radius)) {
                    if (entity instanceof org.bukkit.entity.LivingEntity && entity != player) {
                        double dist = entity.getLocation().distance(target);
                        if (dist < nearestDist) {
                            nearestDist = dist;
                            nearest = (org.bukkit.entity.LivingEntity) entity;
                        }
                    }
                }
                
                if (nearest != null) {
                    Location lightningLoc = nearest.getLocation();
                    player.getWorld().strikeLightning(lightningLoc);
                    nearest.damage(damage / duration);
                }
                
                count++;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        
        player.sendMessage("§e⚡ Tesla Kulesi aktif! (Süre: " + duration + " saniye)");
    }
    
    /**
     * Cehennem Ateşi L4: Sürekli yanma + blok kırma (80 hasar)
     */
    private void fireHellfire(Player player, Location target, double multiplier, int level) {
        int radius = (int)(12 * multiplier);
        int duration = (int)(10 * multiplier);
        
        new org.bukkit.scheduler.BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= duration) {
                    cancel();
                    return;
                }
                
                for (org.bukkit.entity.Entity entity : player.getWorld().getNearbyEntities(target, radius, radius, radius)) {
                    if (entity instanceof org.bukkit.entity.LivingEntity && entity != player) {
                        entity.setFireTicks(100);
                        ((org.bukkit.entity.LivingEntity) entity).damage(80.0 * multiplier / duration);
                    }
                }
                
                // Blok kırma (sadece savaşta olan klan alanlarında)
                if (canModifyTerritory(player, target)) {
                    for (int x = -1; x <= 1; x++) {
                        for (int z = -1; z <= 1; z++) {
                            org.bukkit.block.Block block = target.clone().add(x, 0, z).getBlock();
                            if (block.getType() != org.bukkit.Material.BEDROCK && block.getType() != org.bukkit.Material.AIR) {
                                block.setType(org.bukkit.Material.AIR);
                            }
                        }
                    }
                }
                
                player.getWorld().spawnParticle(org.bukkit.Particle.FLAME, target, 50, radius, 5, radius, 0.1);
                count++;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        
        player.sendMessage("§c🔥 Cehennem Ateşi başladı! (Süre: " + duration + " saniye)");
    }
    
    /**
     * Buz Kalesi L4: Büyük buz yapısı + dondurma (90 hasar)
     */
    private void fireIceFortress(Player player, Location target, double multiplier, int level) {
        double damage = 90.0 * multiplier;
        int radius = (int)(15 * multiplier);
        int duration = (int)(10 * multiplier);
        
        // Buz blokları oluştur
        if (canModifyTerritory(player, target)) {
            for (int x = -3; x <= 3; x++) {
                for (int z = -3; z <= 3; z++) {
                    for (int y = 0; y <= 5; y++) {
                        org.bukkit.block.Block block = target.clone().add(x, y, z).getBlock();
                        if (block.getType() == org.bukkit.Material.AIR) {
                            block.setType(org.bukkit.Material.PACKED_ICE);
                        }
                    }
                }
            }
        }
        
        new org.bukkit.scheduler.BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= duration) {
                    cancel();
                    return;
                }
                
                for (org.bukkit.entity.Entity entity : player.getWorld().getNearbyEntities(target, radius, radius, radius)) {
                    if (entity instanceof org.bukkit.entity.LivingEntity && entity != player) {
                        ((org.bukkit.entity.LivingEntity) entity).damage(damage / duration);
                        ((org.bukkit.entity.LivingEntity) entity).addPotionEffect(new org.bukkit.potion.PotionEffect(
                            org.bukkit.potion.PotionEffectType.SLOW, 40, 2, false, false, true));
                    }
                }
                
                player.getWorld().spawnParticle(org.bukkit.Particle.SNOWBALL, target, 100, radius, 5, radius, 0.1);
                count++;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        
        player.sendMessage("§b❄ Buz Kalesi oluşturuldu! (Süre: " + duration + " saniye)");
    }
    
    /**
     * Ölüm Bulutu L4: Ölümcül zehir (120 hasar)
     */
    private void fireDeathCloud(Player player, Location target, double multiplier, int level) {
        int radius = (int)(12 * multiplier);
        int duration = (int)(15 * multiplier);
        
        new org.bukkit.scheduler.BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= duration) {
                    cancel();
                    return;
                }
                
                for (org.bukkit.entity.Entity entity : player.getWorld().getNearbyEntities(target, radius, radius, radius)) {
                    if (entity instanceof org.bukkit.entity.LivingEntity && entity != player) {
                        ((org.bukkit.entity.LivingEntity) entity).addPotionEffect(new org.bukkit.potion.PotionEffect(
                            org.bukkit.potion.PotionEffectType.POISON, 40, 2, false, false, true));
                        ((org.bukkit.entity.LivingEntity) entity).addPotionEffect(new org.bukkit.potion.PotionEffect(
                            org.bukkit.potion.PotionEffectType.WITHER, 40, 0, false, false, true));
                        ((org.bukkit.entity.LivingEntity) entity).damage(120.0 * multiplier / duration);
                    }
                }
                
                // Blok kırma
                if (canModifyTerritory(player, target)) {
                    for (int x = -1; x <= 1; x++) {
                        for (int z = -1; z <= 1; z++) {
                            org.bukkit.block.Block block = target.clone().add(x, 0, z).getBlock();
                            if (block.getType() != org.bukkit.Material.BEDROCK && block.getType() != org.bukkit.Material.AIR) {
                                block.setType(org.bukkit.Material.AIR);
                            }
                        }
                    }
                }
                
                player.getWorld().spawnParticle(org.bukkit.Particle.DRAGON_BREATH, target, 100, radius, 5, radius, 0.1);
                count++;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        
        player.sendMessage("§4☠ Ölüm Bulutu başladı! (Süre: " + duration + " saniye)");
    }
    
    /**
     * Elektrik Kalkanı L4: Koruyucu elektrik alanı (70 hasar)
     */
    private void fireElectricShield(Player player, Location target, double multiplier, int level) {
        double damage = 70.0 * multiplier;
        int radius = (int)(10 * multiplier);
        int duration = (int)(30 * multiplier);
        
        new org.bukkit.scheduler.BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= duration) {
                    cancel();
                    return;
                }
                
                // Oyuncuya yakın düşmanları şokla
                for (org.bukkit.entity.Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius)) {
                    if (entity instanceof org.bukkit.entity.LivingEntity && entity != player) {
                        ((org.bukkit.entity.LivingEntity) entity).damage(damage / duration);
                        player.getWorld().spawnParticle(org.bukkit.Particle.ELECTRIC_SPARK, entity.getLocation(), 10, 0.5, 0.5, 0.5, 0.1);
                    }
                }
                
                // Oyuncu etrafında koruyucu partiküller
                org.bukkit.Location loc = player.getLocation();
                double time = System.currentTimeMillis() / 1000.0;
                for (int i = 0; i < 16; i++) {
                    double angle = (time * 2.0) + (i * Math.PI / 8);
                    double x = loc.getX() + Math.cos(angle) * 2.0;
                    double y = loc.getY() + 1.0;
                    double z = loc.getZ() + Math.sin(angle) * 2.0;
                    org.bukkit.Location particleLoc = new org.bukkit.Location(loc.getWorld(), x, y, z);
                    player.getWorld().spawnParticle(org.bukkit.Particle.ELECTRIC_SPARK, particleLoc, 1, 0, 0, 0, 0);
                }
                
                count++;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        
        player.sendMessage("§e⚡ Elektrik Kalkanı aktif! (Süre: " + duration + " saniye)");
    }
    
    /**
     * Kıyamet Reaktörü L5: Tüm elementlerin kombinasyonu (300 hasar)
     */
    private void fireApocalypseReactor(Player player, Location target, double multiplier, int level) {
        double damage = 300.0 * multiplier;
        int radius = (int)(40 * multiplier);
        int areaSize = (int)(40 * multiplier);
        
        // Meteor yağmuru
        fireMeteorShower(player, target, multiplier, level);
        
        // Yıldırım fırtınası
        fireLightningStorm(player, target, multiplier, level);
        
        // Ölüm bulutu
        fireDeathCloud(player, target, multiplier, level);
        
        // Buz çağı
        fireIceAge(player, target, multiplier, level);
        
        // Büyük patlama
        player.getWorld().createExplosion(target, (float)(10.0 * multiplier), false, false);
        
        player.sendMessage("§4§l☠ KIYAMET REAKTÖRÜ AKTİF! ☠");
    }
    
    /**
     * Lava Tufanı L5: Sürekli lava spawn - OPTİMİZE EDİLMİŞ
     */
    private void fireLavaTsunami(Player player, Location target, double multiplier, int level) {
        double damage = 300.0 * multiplier;
        int radius = 30;
        int duration = 20; // 60 saniye yerine 20 saniye
        int areaSize = 30;
        
        new org.bukkit.scheduler.BukkitRunnable() {
            int count = 0;
            int currentRadius = 0;
            
            @Override
            public void run() {
                if (count >= duration) {
                    cancel();
                    player.sendMessage("§4§l🔥 LAVA TUFANI SONA ERDİ!");
                    return;
                }
                
                // Dairesel genişleme (her saniye yarıçap artar)
                currentRadius = Math.min(areaSize / 2, count * 2);
                
                // Her saniye sadece 50 rastgele blok lava'ya dönüştür
                if (canModifyTerritory(player, target)) {
                    for (int i = 0; i < 50; i++) {
                        double angle = Math.random() * Math.PI * 2;
                        double dist = Math.random() * currentRadius;
                        int x = (int)(Math.cos(angle) * dist);
                        int z = (int)(Math.sin(angle) * dist);
                        
                        org.bukkit.Location loc = target.clone().add(x, 0, z);
                        org.bukkit.block.Block block = loc.getBlock();
                        if (block.getType() == org.bukkit.Material.AIR) {
                            block.setType(org.bukkit.Material.LAVA);
                        }
                    }
                }
                
                // Hasar ver
                for (org.bukkit.entity.Entity entity : player.getWorld().getNearbyEntities(target, currentRadius, 5, currentRadius)) {
                    if (entity instanceof org.bukkit.entity.LivingEntity && entity != player) {
                        entity.setFireTicks(100);
                        ((org.bukkit.entity.LivingEntity) entity).damage(damage / duration);
                    }
                }
                
                // Partikül (azaltılmış)
                player.getWorld().spawnParticle(org.bukkit.Particle.LAVA, target, 20, currentRadius, 3, currentRadius, 0.1);
                count++;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        
        player.sendMessage("§4§l🔥 LAVA TUFANI BAŞLADI! (Süre: " + duration + " saniye)");
    }
    
    /**
     * Boss Katili L5: Bosslara özel hasar (Bosslara 300, diğerlerine 100)
     */
    private void fireBossKiller(Player player, Location target, double multiplier, int level) {
        double bossDamage = 300.0 * multiplier;
        double normalDamage = 100.0 * multiplier;
        int radius = (int)(50 * multiplier);
        
        me.mami.stratocraft.manager.BossManager bossManager = plugin.getBossManager();
        
        for (org.bukkit.entity.Entity entity : player.getWorld().getNearbyEntities(target, radius, radius, radius)) {
            if (entity instanceof org.bukkit.entity.LivingEntity && entity != player) {
                // Boss kontrolü
                boolean isBoss = false;
                if (bossManager != null) {
                    me.mami.stratocraft.manager.BossManager.BossData bossData = bossManager.getBossData(entity.getUniqueId());
                    isBoss = (bossData != null);
                }
                
                if (isBoss) {
                    ((org.bukkit.entity.LivingEntity) entity).damage(bossDamage);
                    player.sendMessage("§c§lBOSS HASARI: " + String.format("%.1f", bossDamage) + " kalp!");
                } else {
                    ((org.bukkit.entity.LivingEntity) entity).damage(normalDamage);
                }
            }
        }
        
        player.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_LARGE, target, 1);
        player.sendMessage("§4§l☠ BOSS KATİLİ AKTİF! ☠");
    }
    
    /**
     * Alan Yok Edici L5: Büyük alan yıkımı (300 hasar, 50x50 alan) - OPTİMİZE EDİLMİŞ
     */
    private void fireAreaDestroyer(Player player, Location target, double multiplier, int level) {
        double damage = 300.0 * multiplier;
        int areaSize = 50; // Sabit 50x50 alan
        
        // ÖNCE: Tüm entity'lere hasar ver (tek seferde)
        int halfSize = areaSize / 2;
        for (org.bukkit.entity.Entity entity : player.getWorld().getNearbyEntities(target, halfSize, 10, halfSize)) {
            if (entity instanceof org.bukkit.entity.LivingEntity && entity != player) {
                ((org.bukkit.entity.LivingEntity) entity).damage(damage);
            }
        }
        
        // Merkez patlama efekti
        player.getWorld().createExplosion(target, 8.0f, false, false);
        
        // Blok yok etme (async, tick bazlı) - OPTİMİZE EDİLMİŞ
        new org.bukkit.scheduler.BukkitRunnable() {
            int currentX = -halfSize;
            int blocksDestroyed = 0;
            int blocksChecked = 0;
            int blocksBlocked = 0;
            
            @Override
            public void run() {
                // Her tick'te 10 sütun işle (daha hızlı)
                for (int xOffset = 0; xOffset < 10 && currentX <= halfSize; xOffset++, currentX++) {
                    for (int z = -halfSize; z <= halfSize; z++) {
                        org.bukkit.Location loc = target.clone().add(currentX, 0, z);
                        
                        // Blok kırma
                        boolean canModify = canModifyTerritory(player, loc);
                        if (!canModify) {
                            blocksBlocked++;
                        }
                        
                        if (canModify) {
                            for (int y = -5; y <= 5; y++) {
                                org.bukkit.block.Block block = loc.clone().add(0, y, 0).getBlock();
                                blocksChecked++;
                                if (block.getType() != org.bukkit.Material.BEDROCK && 
                                    block.getType() != org.bukkit.Material.AIR &&
                                    block.getType() != org.bukkit.Material.BARRIER) {
                                    block.setType(org.bukkit.Material.AIR);
                                    blocksDestroyed++;
                                }
                            }
                        }
                    }
                    
                    // Her 10 sütunda bir partikül
                    if (currentX % 10 == 0) {
                        org.bukkit.Location particleLoc = target.clone().add(currentX, 0, 0);
                        player.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_LARGE, particleLoc, 2);
                    }
                }
                
                // Tamamlandı mı?
                if (currentX > halfSize) {
                    cancel();
                    player.sendMessage("§4§l💥 ALAN YOK EDİCİ TAMAMLANDI! 💥");
                    player.sendMessage("§c" + blocksDestroyed + " blok yok edildi!");
                    player.sendMessage("§7" + blocksChecked + " blok kontrol edildi.");
                    if (blocksBlocked > 0) {
                        player.sendMessage("§e" + blocksBlocked + " sütun korumalı alanda.");
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L); // Her tick çalış
        
        player.sendMessage("§4§l💥 ALAN YOK EDİCİ AKTİF! 💥");
        player.sendMessage("§eBloklar yok ediliyor...");
    }
    
    /**
     * Dağ Yok Edici L5: Dev alan yıkımı (300 hasar, 50x50 alan, dikey 20 blok) - OPTİMİZE EDİLMİŞ
     */
    private void fireMountainDestroyer(Player player, Location target, double multiplier, int level) {
        double damage = 300.0 * multiplier;
        int areaSize = 50; // Sabit 50x50 alan
        
        // ÖNCE: Tüm entity'lere hasar ver (tek seferde)
        int halfSize = areaSize / 2;
        for (org.bukkit.entity.Entity entity : player.getWorld().getNearbyEntities(target, halfSize, 15, halfSize)) {
            if (entity instanceof org.bukkit.entity.LivingEntity && entity != player) {
                ((org.bukkit.entity.LivingEntity) entity).damage(damage);
            }
        }
        
        // Merkez patlama efekti
        player.getWorld().createExplosion(target, 10.0f, false, false);
        
        // Blok yok etme (async, tick bazlı) - OPTİMİZE EDİLMİŞ
        new org.bukkit.scheduler.BukkitRunnable() {
            int currentX = -halfSize;
            int blocksDestroyed = 0;
            int blocksChecked = 0;
            int blocksBlocked = 0;
            
            @Override
            public void run() {
                // Her tick'te 10 sütun işle (daha hızlı)
                for (int xOffset = 0; xOffset < 10 && currentX <= halfSize; xOffset++, currentX++) {
                    for (int z = -halfSize; z <= halfSize; z++) {
                        org.bukkit.Location loc = target.clone().add(currentX, 0, z);
                        
                        // Blok kırma (dikey 20 blok)
                        boolean canModify = canModifyTerritory(player, loc);
                        if (!canModify) {
                            blocksBlocked++;
                        }
                        
                        if (canModify) {
                            for (int y = -10; y <= 10; y++) {
                                org.bukkit.block.Block block = loc.clone().add(0, y, 0).getBlock();
                                blocksChecked++;
                                if (block.getType() != org.bukkit.Material.BEDROCK && 
                                    block.getType() != org.bukkit.Material.AIR &&
                                    block.getType() != org.bukkit.Material.BARRIER) {
                                    block.setType(org.bukkit.Material.AIR);
                                    blocksDestroyed++;
                                }
                            }
                        }
                    }
                    
                    // Her 10 sütunda bir partikül
                    if (currentX % 10 == 0) {
                        org.bukkit.Location particleLoc = target.clone().add(currentX, 0, 0);
                        player.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_LARGE, particleLoc, 2);
                        player.getWorld().spawnParticle(org.bukkit.Particle.LAVA, particleLoc, 3);
                    }
                }
                
                // Tamamlandı mı?
                if (currentX > halfSize) {
                    cancel();
                    player.sendMessage("§4§l⛰ DAĞ YOK EDİCİ TAMAMLANDI! ⛰");
                    player.sendMessage("§c" + blocksDestroyed + " blok yok edildi!");
                    player.sendMessage("§7" + blocksChecked + " blok kontrol edildi.");
                    if (blocksBlocked > 0) {
                        player.sendMessage("§e" + blocksBlocked + " sütun korumalı alanda.");
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L); // Her tick çalış
        
        player.sendMessage("§4§l⛰ DAĞ YOK EDİCİ AKTİF! ⛰");
        player.sendMessage("§eBloklar yok ediliyor...");
    }
    
    /**
     * Bölge değiştirme kontrolü (ESNETİLMİŞ)
     * SADECE spawn ve özel korumalı bölgeleri korur
     */
    private boolean canModifyTerritory(Player player, Location loc) {
        // Spawn kontrolü (spawn yakınında blok yok etme engellenir)
        Location spawnLoc = loc.getWorld().getSpawnLocation();
        if (spawnLoc != null && loc.distance(spawnLoc) < 100) {
            return false; // Spawn yakınında blok yok etme yasak
        }
        
        // TerritoryManager kontrolü (opsiyonel)
        me.mami.stratocraft.manager.TerritoryManager territoryManager = plugin.getTerritoryManager();
        if (territoryManager == null) {
            return true; // TerritoryManager yoksa her yerde blok yok edilebilir
        }
        
        // Bölge sahibi var mı?
        me.mami.stratocraft.model.Clan territoryOwner = territoryManager.getTerritoryOwner(loc);
        if (territoryOwner == null) {
            return true; // Boş arazi, blok yok edilebilir
        }
        
        // Oyuncunun klanı var mı?
        me.mami.stratocraft.model.Clan playerClan = territoryManager.getClanManager().getClanByPlayer(player.getUniqueId());
        if (playerClan == null) {
            return false; // Klansız oyuncu klan alanında blok yok edemez
        }
        
        // Kendi klan alanı mı?
        if (territoryOwner.getId().equals(playerClan.getId())) {
            return true; // Kendi klan alanında blok yok edilebilir
        }
        
        // Düşman klan alanı - savaş kontrolü
        me.mami.stratocraft.manager.SiegeManager siegeManager = plugin.getSiegeManager();
        if (siegeManager == null) {
            return false; // SiegeManager yoksa düşman alanında blok yok edilemez
        }
        
        // Savaş durumu kontrolü
        if (siegeManager.isUnderSiege(territoryOwner)) {
            return siegeManager.getAttacker(territoryOwner).equals(playerClan);
        }
        
        return false; // Düşman klan alanı ve savaş yok, blok yok edilemez
    }
    
    // ========== TARİF KONTROL FONKSİYONLARI ==========
    
    /**
     * Genel tarif kontrol yardımcı fonksiyonu
     * BlockPattern'e göre blokları kontrol eder - TAMAMEN ESNEK
     */
    public static RecipeCheckResult checkBlockPattern(Block centerBlock, BlockPattern pattern, String batteryName) {
        // Merkez blok kontrolü
        if (centerBlock.getType() != pattern.getCenterBlock()) {
            return RecipeCheckResult.failure("Merkez blok eşleşmedi");
        }
        
        // 4 rotasyonu dene (0°, 90°, 180°, 270°)
        for (int rotation = 0; rotation < 4; rotation++) {
            RecipeCheckResult result = checkBlockPatternWithRotation(centerBlock, pattern, batteryName, rotation);
            if (result.matches()) {
                return result; // İlk eşleşen rotasyonu döndür
            }
        }
        
        return RecipeCheckResult.failure("Hiçbir rotasyonda eşleşmedi");
    }
    
    /**
     * Belirli bir rotasyonla blok pattern kontrolü
     * @param rotation 0=0°, 1=90°, 2=180°, 3=270°
     */
    private static RecipeCheckResult checkBlockPatternWithRotation(Block centerBlock, BlockPattern pattern, String batteryName, int rotation) {
        List<Block> blocksToRemove = new ArrayList<>();
        blocksToRemove.add(centerBlock);
        
        // Tüm gerekli blokları kontrol et
        for (Map.Entry<BlockPosition, Material> entry : pattern.getRequiredBlocks().entrySet()) {
            BlockPosition pos = entry.getKey();
            Material expected = entry.getValue();
            
            // Rotasyonu uygula
            BlockPosition rotatedPos = rotatePosition(pos, rotation);
            
            // Merkez bloktan göreceli konumu hesapla
            Block targetBlock = centerBlock.getRelative(rotatedPos.getX(), rotatedPos.getY(), rotatedPos.getZ());
            
            // Blok tipini kontrol et
            if (targetBlock.getType() != expected) {
                return RecipeCheckResult.failure("Blok eşleşmedi");
            }
            
            blocksToRemove.add(targetBlock);
        }
        
        return RecipeCheckResult.success(pattern, blocksToRemove, batteryName);
    }
    
    /**
     * Pozisyonu Y ekseni etrafında döndür
     * @param pos Orijinal pozisyon
     * @param rotation 0=0°, 1=90°, 2=180°, 3=270°
     * @return Döndürülmüş pozisyon
     */
    private static BlockPosition rotatePosition(BlockPosition pos, int rotation) {
        int x = pos.getX();
        int y = pos.getY(); // Y değişmez (yukarı/aşağı)
        int z = pos.getZ();
        
        // Y ekseni etrafında rotasyon (saat yönünde)
        switch (rotation) {
            case 0: // 0° - Değişiklik yok
                return new BlockPosition(x, y, z);
            case 1: // 90° saat yönünde
                return new BlockPosition(-z, y, x);
            case 2: // 180°
                return new BlockPosition(-x, y, -z);
            case 3: // 270° saat yönünde (= 90° saat yönünün tersi)
                return new BlockPosition(z, y, -x);
            default:
                return pos;
        }
    }
    
    // ========== ÖRNEK TARİF İMPLEMENTASYONLARI ==========
    
    /**
     * Yıldırım Asası L1: 3x IRON_BLOCK üst üste (Dikey Kule)
     */
    private static class LightningStaffL1Recipe implements RecipeChecker {
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.IRON_BLOCK)
                .addBlockAbove(1, Material.IRON_BLOCK)
                .addBlockBelow(1, Material.IRON_BLOCK);
        }
        
        @Override
        public String getBatteryName() { return "Yıldırım Asası"; }
        
        @Override
        public int getLevel() { return 1; }
    }
    
    /**
     * Cehennem Topu L1: 3x MAGMA_BLOCK yatay (Doğu-Batı)
     */
    private static class HellfireBallL1Recipe implements RecipeChecker {
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.MAGMA_BLOCK)
                .addBlockEast(1, Material.MAGMA_BLOCK)
                .addBlockWest(1, Material.MAGMA_BLOCK);
        }
        
        @Override
        public String getBatteryName() { return "Cehennem Topu"; }
        
        @Override
        public int getLevel() { return 1; }
    }
    
    /**
     * Buz Topu L1: T şekli (Merkez + Kuzey + Güney + Yukarı)
     */
    private static class IceBallL1Recipe implements RecipeChecker {
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.PACKED_ICE)
                .addBlockAbove(1, Material.PACKED_ICE)
                .addBlockNorth(1, Material.PACKED_ICE)
                .addBlockSouth(1, Material.PACKED_ICE);
        }
        
        @Override
        public String getBatteryName() { return "Buz Topu"; }
        
        @Override
        public int getLevel() { return 1; }
    }
    
    /**
     * Zehir Oku L1: 2x2 Kare (Merkez + Doğu + Kuzey + Doğu-Kuzey)
     */
    private static class PoisonArrowL1Recipe implements RecipeChecker {
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.EMERALD_BLOCK)
                .addBlockEast(1, Material.EMERALD_BLOCK)
                .addBlockNorth(1, Material.EMERALD_BLOCK)
                .addBlockAt(1, 0, -1, Material.EMERALD_BLOCK); // Doğu-Kuzey köşe
        }
        
        @Override
        public String getBatteryName() { return "Zehir Oku"; }
        
        @Override
        public int getLevel() { return 1; }
    }
    
    /**
     * Şok Dalgası L1: Artı (+) şekli (Merkez + 4 yön)
     */
    private static class ShockWaveL1Recipe implements RecipeChecker {
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.REDSTONE_BLOCK)
                .addBlockEast(1, Material.REDSTONE_BLOCK)
                .addBlockWest(1, Material.REDSTONE_BLOCK)
                .addBlockNorth(1, Material.REDSTONE_BLOCK)
                .addBlockSouth(1, Material.REDSTONE_BLOCK);
        }
        
        @Override
        public String getBatteryName() { return "Şok Dalgası"; }
        
        @Override
        public int getLevel() { return 1; }
    }
    
    /**
     * Çift Ateş Topu L2: Piramit şekli (3x3 taban, 1 üstte)
     */
    private static class DoubleFireballL2Recipe implements RecipeChecker {
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.MAGMA_BLOCK)
                // 3x3 taban (merkez + 8 yan)
                .addBlockEast(1, Material.MAGMA_BLOCK)
                .addBlockWest(1, Material.MAGMA_BLOCK)
                .addBlockNorth(1, Material.MAGMA_BLOCK)
                .addBlockSouth(1, Material.MAGMA_BLOCK)
                .addBlockAt(1, 0, -1, Material.MAGMA_BLOCK)  // Doğu-Kuzey
                .addBlockAt(1, 0, 1, Material.MAGMA_BLOCK)    // Doğu-Güney
                .addBlockAt(-1, 0, -1, Material.MAGMA_BLOCK)  // Batı-Kuzey
                .addBlockAt(-1, 0, 1, Material.MAGMA_BLOCK)  // Batı-Güney
                // Üstte 1 blok
                .addBlockAbove(1, Material.NETHERRACK);
        }
        
        @Override
        public String getBatteryName() { return "Çift Ateş Topu"; }
        
        @Override
        public int getLevel() { return 2; }
    }
    
    /**
     * Zincir Yıldırım L2: Yatay çizgi (5 blok doğu-batı)
     */
    private static class ChainLightningL2Recipe implements RecipeChecker {
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.IRON_BLOCK)
                .addBlockEast(1, Material.IRON_BLOCK)
                .addBlockEast(2, Material.IRON_BLOCK)
                .addBlockWest(1, Material.IRON_BLOCK)
                .addBlockWest(2, Material.GOLD_BLOCK);
        }
        
        @Override
        public String getBatteryName() { return "Zincir Yıldırım"; }
        
        @Override
        public int getLevel() { return 2; }
    }
    
    /**
     * Buz Fırtınası L2: L şekli (3 yukarı + 2 doğu)
     */
    private static class IceStormL2Recipe implements RecipeChecker {
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.PACKED_ICE)
                .addBlockAbove(1, Material.PACKED_ICE)
                .addBlockAbove(2, Material.PACKED_ICE)
                .addBlockEast(1, Material.PACKED_ICE)
                .addBlockEast(2, Material.BLUE_ICE);
        }
        
        @Override
        public String getBatteryName() { return "Buz Fırtınası"; }
        
        @Override
        public int getLevel() { return 2; }
    }
    
    /**
     * Asit Yağmuru L2: Çapraz çizgi (X şekli)
     */
    private static class AcidRainL2Recipe implements RecipeChecker {
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.EMERALD_BLOCK)
                .addBlockAt(1, 0, 1, Material.EMERALD_BLOCK)   // Doğu-Güney
                .addBlockAt(-1, 0, -1, Material.EMERALD_BLOCK) // Batı-Kuzey
                .addBlockAt(1, 0, -1, Material.SLIME_BLOCK)   // Doğu-Kuzey
                .addBlockAt(-1, 0, 1, Material.EMERALD_BLOCK); // Batı-Güney
        }
        
        @Override
        public String getBatteryName() { return "Asit Yağmuru"; }
        
        @Override
        public int getLevel() { return 2; }
    }
    
    /**
     * Elektrik Ağı L2: 3x3 Kare (9 blok)
     */
    private static class ElectricNetL2Recipe implements RecipeChecker {
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.REDSTONE_BLOCK)
                // 3x3 kare (merkez + 8 yan)
                .addBlockEast(1, Material.REDSTONE_BLOCK)
                .addBlockWest(1, Material.REDSTONE_BLOCK)
                .addBlockNorth(1, Material.REDSTONE_BLOCK)
                .addBlockSouth(1, Material.REDSTONE_BLOCK)
                .addBlockAt(1, 0, -1, Material.REDSTONE_BLOCK)  // Doğu-Kuzey
                .addBlockAt(1, 0, 1, Material.REDSTONE_BLOCK)    // Doğu-Güney
                .addBlockAt(-1, 0, -1, Material.REDSTONE_BLOCK)  // Batı-Kuzey
                .addBlockAt(-1, 0, 1, Material.LAPIS_BLOCK);    // Batı-Güney (özel)
        }
        
        @Override
        public String getBatteryName() { return "Elektrik Ağı"; }
        
        @Override
        public int getLevel() { return 2; }
    }
    
    /**
     * Meteor Yağmuru L3: 2 katlı piramit (5x5 alt, 3x3 üst)
     * Merkez: GOLD_BLOCK (diğerlerinden farklı)
     */
    private static class MeteorShowerL3Recipe implements RecipeChecker {
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.GOLD_BLOCK) // Merkez farklı
                // Alt kat (5x5) - merkez + 24 yan
                .addBlockEast(1, Material.OBSIDIAN)
                .addBlockEast(2, Material.OBSIDIAN)
                .addBlockWest(1, Material.OBSIDIAN)
                .addBlockWest(2, Material.OBSIDIAN)
                .addBlockNorth(1, Material.OBSIDIAN)
                .addBlockNorth(2, Material.OBSIDIAN)
                .addBlockSouth(1, Material.OBSIDIAN)
                .addBlockSouth(2, Material.OBSIDIAN)
                // Köşeler
                .addBlockAt(2, 0, 2, Material.OBSIDIAN)
                .addBlockAt(2, 0, -2, Material.OBSIDIAN)
                .addBlockAt(-2, 0, 2, Material.OBSIDIAN)
                .addBlockAt(-2, 0, -2, Material.OBSIDIAN)
                // Üst kat (3x3) - 1 yukarıda
                .addBlockAbove(1, Material.OBSIDIAN)
                .addBlockAt(1, 1, 0, Material.OBSIDIAN)
                .addBlockAt(-1, 1, 0, Material.OBSIDIAN)
                .addBlockAt(0, 1, 1, Material.OBSIDIAN)
                .addBlockAt(0, 1, -1, Material.OBSIDIAN)
                .addBlockAt(1, 1, 1, Material.MAGMA_BLOCK)
                .addBlockAt(1, 1, -1, Material.OBSIDIAN)
                .addBlockAt(-1, 1, 1, Material.OBSIDIAN)
                .addBlockAt(-1, 1, -1, Material.OBSIDIAN);
        }
        
        @Override
        public String getBatteryName() { return "Meteor Yağmuru"; }
        
        @Override
        public int getLevel() { return 3; }
    }
    
    /**
     * Yıldırım Fırtınası L3: H şekli (yatay + dikey)
     * Merkez: DIAMOND_BLOCK (diğerlerinden farklı)
     */
    private static class LightningStormL3Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.DIAMOND_BLOCK) // Merkez farklı
                // Yatay çizgi (3 blok)
                .addBlockEast(1, Material.IRON_BLOCK)
                .addBlockWest(1, Material.IRON_BLOCK)
                // Dikey çizgiler (her iki yanda 2'şer blok yukarı)
                .addBlockAbove(1, Material.IRON_BLOCK)
                .addBlockAbove(2, Material.DIAMOND_BLOCK)
                .addBlockAt(1, 1, 0, Material.IRON_BLOCK)
                .addBlockAt(1, 2, 0, Material.IRON_BLOCK)
                .addBlockAt(-1, 1, 0, Material.IRON_BLOCK)
                .addBlockAt(-1, 2, 0, Material.IRON_BLOCK);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Yıldırım Fırtınası"; }
        
        @Override
        public int getLevel() { return 3; }
    }
    
    /**
     * Buz Çağı L3: Yıldız şekli (5 uçlu)
     */
    private static class IceAgeL3Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.PACKED_ICE)
                // 5 uçlu yıldız
                .addBlockAbove(1, Material.PACKED_ICE)
                .addBlockBelow(1, Material.PACKED_ICE)
                .addBlockEast(1, Material.PACKED_ICE)
                .addBlockWest(1, Material.PACKED_ICE)
                .addBlockNorth(1, Material.PACKED_ICE)
                .addBlockSouth(1, Material.FROSTED_ICE);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Buz Çağı"; }
        
        @Override
        public int getLevel() { return 3; }
    }
    
    /**
     * Zehir Bombası L3: Çapraz kule (X şekli dikey)
     * Merkez: EMERALD (diğerlerinden farklı - zaten farklı)
     */
    private static class PoisonBombL3Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.EMERALD) // Merkez farklı (EMERALD vs EMERALD_BLOCK)
                // Çapraz X şekli (3 kat yukarı)
                .addBlockAt(1, 1, 1, Material.EMERALD_BLOCK)
                .addBlockAt(1, 2, 1, Material.EMERALD_BLOCK)
                .addBlockAt(-1, 1, -1, Material.EMERALD_BLOCK)
                .addBlockAt(-1, 2, -1, Material.EMERALD_BLOCK)
                .addBlockAt(1, 1, -1, Material.EMERALD_BLOCK)
                .addBlockAt(1, 2, -1, Material.POISONOUS_POTATO)
                .addBlockAt(-1, 1, 1, Material.EMERALD_BLOCK)
                .addBlockAt(-1, 2, 1, Material.EMERALD_BLOCK);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Zehir Bombası"; }
        
        @Override
        public int getLevel() { return 3; }
    }
    
    /**
     * Elektrik Fırtınası L3: Z şekli (yatay + çapraz)
     * Merkez: REDSTONE (diğerlerinden farklı)
     */
    private static class ElectricStormL3Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.REDSTONE) // Merkez farklı (REDSTONE vs REDSTONE_BLOCK)
                // Z şekli
                .addBlockEast(1, Material.REDSTONE_BLOCK)
                .addBlockEast(2, Material.REDSTONE_BLOCK)
                .addBlockAt(2, 1, 0, Material.REDSTONE_BLOCK)  // Çapraz
                .addBlockAt(1, 1, 0, Material.REDSTONE_BLOCK)  // Çapraz
                .addBlockWest(1, Material.REDSTONE_BLOCK)
                .addBlockWest(2, Material.GLOWSTONE);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Elektrik Fırtınası"; }
        
        @Override
        public int getLevel() { return 3; }
    }
    
    /**
     * Tesla Kulesi L4: 3 katlı kule (her katta 3x3)
     * Merkez: BEACON (diğerlerinden farklı - ortada parlayan)
     */
    private static class TeslaTowerL4Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.BEACON) // Merkez farklı (BEACON parlar)
                // Alt kat (3x3)
                .addBlockEast(1, Material.COPPER_BLOCK)
                .addBlockWest(1, Material.COPPER_BLOCK)
                .addBlockNorth(1, Material.COPPER_BLOCK)
                .addBlockSouth(1, Material.COPPER_BLOCK)
                .addBlockAt(1, 0, -1, Material.COPPER_BLOCK)
                .addBlockAt(1, 0, 1, Material.COPPER_BLOCK)
                .addBlockAt(-1, 0, -1, Material.COPPER_BLOCK)
                .addBlockAt(-1, 0, 1, Material.COPPER_BLOCK)
                // Orta kat (3x3) - 1 yukarıda
                .addBlockAbove(1, Material.COPPER_BLOCK)
                .addBlockAt(1, 1, 0, Material.COPPER_BLOCK)
                .addBlockAt(-1, 1, 0, Material.COPPER_BLOCK)
                .addBlockAt(0, 1, 1, Material.COPPER_BLOCK)
                .addBlockAt(0, 1, -1, Material.COPPER_BLOCK)
                .addBlockAt(1, 1, 1, Material.COPPER_BLOCK)
                .addBlockAt(1, 1, -1, Material.COPPER_BLOCK)
                .addBlockAt(-1, 1, 1, Material.COPPER_BLOCK)
                .addBlockAt(-1, 1, -1, Material.REDSTONE_BLOCK)
                // Üst kat (3x3) - 2 yukarıda
                .addBlockAbove(2, Material.COPPER_BLOCK)
                .addBlockAt(1, 2, 0, Material.COPPER_BLOCK)
                .addBlockAt(-1, 2, 0, Material.COPPER_BLOCK)
                .addBlockAt(0, 2, 1, Material.COPPER_BLOCK)
                .addBlockAt(0, 2, -1, Material.COPPER_BLOCK)
                .addBlockAt(1, 2, 1, Material.COPPER_BLOCK)
                .addBlockAt(1, 2, -1, Material.COPPER_BLOCK)
                .addBlockAt(-1, 2, 1, Material.COPPER_BLOCK)
                .addBlockAt(-1, 2, -1, Material.COPPER_BLOCK);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Tesla Kulesi"; }
        
        @Override
        public int getLevel() { return 4; }
    }
    
    /**
     * Cehennem Ateşi L4: Çapraz spiral (X şekli 3D)
     * Merkez: NETHER_STAR (diğerlerinden farklı - parlayan)
     */
    private static class HellfireL4Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.NETHER_STAR) // Merkez farklı (parlayan)
                // Çapraz spiral
                .addBlockAt(1, 0, 1, Material.MAGMA_BLOCK)
                .addBlockAt(1, 1, 1, Material.MAGMA_BLOCK)
                .addBlockAt(1, 2, 1, Material.MAGMA_BLOCK)
                .addBlockAt(-1, 0, -1, Material.MAGMA_BLOCK)
                .addBlockAt(-1, 1, -1, Material.MAGMA_BLOCK)
                .addBlockAt(-1, 2, -1, Material.MAGMA_BLOCK)
                .addBlockAt(1, 0, -1, Material.MAGMA_BLOCK)
                .addBlockAt(1, 1, -1, Material.MAGMA_BLOCK)
                .addBlockAt(-1, 0, 1, Material.NETHER_STAR)
                .addBlockAt(-1, 1, 1, Material.MAGMA_BLOCK)
                .addBlockAt(-1, 2, 1, Material.MAGMA_BLOCK);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Cehennem Ateşi"; }
        
        @Override
        public int getLevel() { return 4; }
    }
    
    /**
     * Buz Kalesi L4: Kale şekli (duvarlar + köşeler)
     * Merkez: BLUE_ICE (diğerlerinden farklı - daha parlak)
     */
    private static class IceFortressL4Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.BLUE_ICE) // Merkez farklı (daha parlak)
                // Duvarlar (5x5 çerçeve)
                .addBlockEast(1, Material.PACKED_ICE)
                .addBlockEast(2, Material.PACKED_ICE)
                .addBlockWest(1, Material.PACKED_ICE)
                .addBlockWest(2, Material.PACKED_ICE)
                .addBlockNorth(1, Material.PACKED_ICE)
                .addBlockNorth(2, Material.PACKED_ICE)
                .addBlockSouth(1, Material.PACKED_ICE)
                .addBlockSouth(2, Material.PACKED_ICE)
                // Köşeler
                .addBlockAt(2, 0, 2, Material.PACKED_ICE)
                .addBlockAt(2, 0, -2, Material.PACKED_ICE)
                .addBlockAt(-2, 0, 2, Material.PACKED_ICE)
                .addBlockAt(-2, 0, -2, Material.SNOW_BLOCK)
                // Üstte köşeler
                .addBlockAt(2, 1, 2, Material.PACKED_ICE)
                .addBlockAt(2, 1, -2, Material.PACKED_ICE)
                .addBlockAt(-2, 1, 2, Material.PACKED_ICE)
                .addBlockAt(-2, 1, -2, Material.PACKED_ICE);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Buz Kalesi"; }
        
        @Override
        public int getLevel() { return 4; }
    }
    
    /**
     * Ölüm Bulutu L4: Yıldız şekli (8 uçlu)
     * Merkez: WITHER_SKELETON_SKULL (diğerlerinden farklı)
     */
    private static class DeathCloudL4Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.WITHER_SKELETON_SKULL) // Merkez farklı
                // 8 yönlü yıldız
                .addBlockEast(1, Material.EMERALD_BLOCK)
                .addBlockEast(2, Material.EMERALD_BLOCK)
                .addBlockWest(1, Material.EMERALD_BLOCK)
                .addBlockWest(2, Material.EMERALD_BLOCK)
                .addBlockNorth(1, Material.EMERALD_BLOCK)
                .addBlockNorth(2, Material.EMERALD_BLOCK)
                .addBlockSouth(1, Material.EMERALD_BLOCK)
                .addBlockSouth(2, Material.EMERALD_BLOCK)
                // Çaprazlar
                .addBlockAt(1, 0, 1, Material.EMERALD_BLOCK)
                .addBlockAt(2, 0, 2, Material.EMERALD_BLOCK)
                .addBlockAt(-1, 0, -1, Material.EMERALD_BLOCK)
                .addBlockAt(-2, 0, -2, Material.EMERALD_BLOCK)
                .addBlockAt(1, 0, -1, Material.EMERALD_BLOCK)
                .addBlockAt(2, 0, -2, Material.EMERALD_BLOCK)
                .addBlockAt(-1, 0, 1, Material.WITHER_SKELETON_SKULL)
                .addBlockAt(-2, 0, 2, Material.EMERALD_BLOCK);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Ölüm Bulutu"; }
        
        @Override
        public int getLevel() { return 4; }
    }
    
    /**
     * Elektrik Kalkanı L4: Kare halka (içi boş 5x5)
     * Merkez: END_CRYSTAL (diğerlerinden farklı - parlayan)
     */
    private static class ElectricShieldL4Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.END_CRYSTAL) // Merkez farklı (parlayan)
                // Dış halka (5x5 çerçeve)
                .addBlockEast(1, Material.REDSTONE_BLOCK)
                .addBlockEast(2, Material.REDSTONE_BLOCK)
                .addBlockWest(1, Material.REDSTONE_BLOCK)
                .addBlockWest(2, Material.REDSTONE_BLOCK)
                .addBlockNorth(1, Material.REDSTONE_BLOCK)
                .addBlockNorth(2, Material.REDSTONE_BLOCK)
                .addBlockSouth(1, Material.REDSTONE_BLOCK)
                .addBlockSouth(2, Material.REDSTONE_BLOCK)
                // Köşeler
                .addBlockAt(2, 0, 2, Material.REDSTONE_BLOCK)
                .addBlockAt(2, 0, -2, Material.REDSTONE_BLOCK)
                .addBlockAt(-2, 0, 2, Material.REDSTONE_BLOCK)
                .addBlockAt(-2, 0, -2, Material.REDSTONE_BLOCK)
                // Üstte halka
                .addBlockAt(2, 1, 2, Material.REDSTONE_BLOCK)
                .addBlockAt(2, 1, -2, Material.REDSTONE_BLOCK)
                .addBlockAt(-2, 1, 2, Material.REDSTONE_BLOCK)
                .addBlockAt(-2, 1, -2, Material.END_CRYSTAL)
                .addBlockAt(2, 1, 0, Material.REDSTONE_BLOCK)
                .addBlockAt(-2, 1, 0, Material.REDSTONE_BLOCK)
                .addBlockAt(0, 1, 2, Material.REDSTONE_BLOCK)
                .addBlockAt(0, 1, -2, Material.REDSTONE_BLOCK);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Elektrik Kalkanı"; }
        
        @Override
        public int getLevel() { return 4; }
    }
    
    /**
     * Kıyamet Reaktörü L5: Büyük piramit (7x7 taban, 5x5, 3x3, 1 üstte)
     * Merkez: BEACON (diğerlerinden farklı - ortada parlayan)
     */
    private static class ApocalypseReactorL5Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.BEACON) // Merkez farklı (parlayan)
                // 7x7 taban (en altta)
                .addBlockBelow(1, Material.OBSIDIAN)
                .addBlockAt(0, -1, 0, Material.OBSIDIAN) // Merkez alt
                // 5x5 orta kat
                .addBlockEast(1, Material.OBSIDIAN)
                .addBlockEast(2, Material.OBSIDIAN)
                .addBlockWest(1, Material.OBSIDIAN)
                .addBlockWest(2, Material.OBSIDIAN)
                .addBlockNorth(1, Material.OBSIDIAN)
                .addBlockNorth(2, Material.OBSIDIAN)
                .addBlockSouth(1, Material.OBSIDIAN)
                .addBlockSouth(2, Material.OBSIDIAN)
                // 3x3 üst kat
                .addBlockAbove(1, Material.OBSIDIAN)
                .addBlockAt(1, 1, 0, Material.OBSIDIAN)
                .addBlockAt(-1, 1, 0, Material.OBSIDIAN)
                .addBlockAt(0, 1, 1, Material.OBSIDIAN)
                .addBlockAt(0, 1, -1, Material.OBSIDIAN)
                // En üstte özel blok
                .addBlockAbove(2, Material.END_CRYSTAL)
                // En altta özel blok
                .addBlockBelow(2, Material.BEACON);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Kıyamet Reaktörü"; }
        
        @Override
        public int getLevel() { return 5; }
    }
    
    /**
     * Lava Tufanı L5: Yatay dalga şekli (5x5 yatay + üstte/altta özel)
     * Merkez: LAVA (diğerlerinden farklı - akan lav)
     */
    private static class LavaTsunamiL5Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.LAVA) // Merkez farklı (akan lav)
                // 5x5 yatay düzlem
                .addBlockEast(1, Material.MAGMA_BLOCK)
                .addBlockEast(2, Material.MAGMA_BLOCK)
                .addBlockWest(1, Material.MAGMA_BLOCK)
                .addBlockWest(2, Material.MAGMA_BLOCK)
                .addBlockNorth(1, Material.MAGMA_BLOCK)
                .addBlockNorth(2, Material.MAGMA_BLOCK)
                .addBlockSouth(1, Material.MAGMA_BLOCK)
                .addBlockSouth(2, Material.MAGMA_BLOCK)
                // Köşeler
                .addBlockAt(2, 0, 2, Material.MAGMA_BLOCK)
                .addBlockAt(2, 0, -2, Material.MAGMA_BLOCK)
                .addBlockAt(-2, 0, 2, Material.MAGMA_BLOCK)
                .addBlockAt(-2, 0, -2, Material.MAGMA_BLOCK)
                // Üstte özel blok
                .addBlockAbove(1, Material.MAGMA_BLOCK)
                // Altta özel blok
                .addBlockBelow(1, Material.BEACON);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Lava Tufanı"; }
        
        @Override
        public int getLevel() { return 5; }
    }
    
    /**
     * Boss Katili L5: T şekli 3D (yatay + dikey + özel bloklar)
     * Merkez: DRAGON_HEAD (diğerlerinden farklı - özel)
     */
    private static class BossKillerL5Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.DRAGON_HEAD) // Merkez farklı (özel)
                // Yatay T şekli
                .addBlockEast(1, Material.NETHERITE_BLOCK)
                .addBlockEast(2, Material.NETHERITE_BLOCK)
                .addBlockWest(1, Material.NETHERITE_BLOCK)
                .addBlockWest(2, Material.NETHERITE_BLOCK)
                .addBlockNorth(1, Material.NETHERITE_BLOCK)
                .addBlockNorth(2, Material.NETHERITE_BLOCK)
                // Dikey T şekli
                .addBlockAbove(1, Material.NETHERITE_BLOCK)
                .addBlockAbove(2, Material.NETHERITE_BLOCK)
                .addBlockBelow(1, Material.NETHERITE_BLOCK)
                .addBlockBelow(2, Material.NETHERITE_BLOCK)
                // Üstte özel blok
                .addBlockAbove(3, Material.DRAGON_HEAD)
                // Altta özel blok
                .addBlockBelow(3, Material.BEACON);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Boss Katili"; }
        
        @Override
        public int getLevel() { return 5; }
    }
    
    /**
     * Alan Yok Edici L5: Büyük kare (7x7 düzlem)
     * Merkez: COMMAND_BLOCK (diğerlerinden farklı - özel)
     */
    private static class AreaDestroyerL5Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.COMMAND_BLOCK) // Merkez farklı (özel)
                // 7x7 kare (merkez + 48 yan)
                .addBlockEast(1, Material.ANVIL)
                .addBlockEast(2, Material.ANVIL)
                .addBlockEast(3, Material.ANVIL)
                .addBlockWest(1, Material.ANVIL)
                .addBlockWest(2, Material.ANVIL)
                .addBlockWest(3, Material.ANVIL)
                .addBlockNorth(1, Material.ANVIL)
                .addBlockNorth(2, Material.ANVIL)
                .addBlockNorth(3, Material.ANVIL)
                .addBlockSouth(1, Material.ANVIL)
                .addBlockSouth(2, Material.ANVIL)
                .addBlockSouth(3, Material.ANVIL)
                // Köşeler ve ara bloklar (tüm kombinasyonlar)
                .addBlockAt(3, 0, 3, Material.ANVIL)
                .addBlockAt(3, 0, -3, Material.ANVIL)
                .addBlockAt(-3, 0, 3, Material.ANVIL)
                .addBlockAt(-3, 0, -3, Material.ANVIL)
                .addBlockAt(2, 0, 3, Material.ANVIL)
                .addBlockAt(3, 0, 2, Material.ANVIL)
                .addBlockAt(2, 0, -3, Material.ANVIL)
                .addBlockAt(3, 0, -2, Material.ANVIL)
                .addBlockAt(-2, 0, 3, Material.ANVIL)
                .addBlockAt(-3, 0, 2, Material.ANVIL)
                .addBlockAt(-2, 0, -3, Material.ANVIL)
                .addBlockAt(-3, 0, -2, Material.ANVIL)
                // Üstte özel blok
                .addBlockAbove(1, Material.ANVIL)
                // Altta özel blok
                .addBlockBelow(1, Material.BEACON);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Alan Yok Edici"; }
        
        @Override
        public int getLevel() { return 5; }
    }
    
    /**
     * Dağ Yok Edici L5: Çapraz X şekli 3D (her yönde 5 blok)
     * Merkez: BEDROCK (diğerlerinden farklı - en güçlü)
     */
    private static class MountainDestroyerL5Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.BEDROCK) // Merkez farklı (en güçlü)
                // Çapraz X şekli (her yönde 5 blok)
                .addBlockAt(1, 0, 1, Material.NETHER_STAR)
                .addBlockAt(2, 0, 2, Material.NETHER_STAR)
                .addBlockAt(3, 0, 3, Material.NETHER_STAR)
                .addBlockAt(4, 0, 4, Material.NETHER_STAR)
                .addBlockAt(-1, 0, -1, Material.NETHER_STAR)
                .addBlockAt(-2, 0, -2, Material.NETHER_STAR)
                .addBlockAt(-3, 0, -3, Material.NETHER_STAR)
                .addBlockAt(-4, 0, -4, Material.NETHER_STAR)
                .addBlockAt(1, 0, -1, Material.NETHER_STAR)
                .addBlockAt(2, 0, -2, Material.NETHER_STAR)
                .addBlockAt(3, 0, -3, Material.NETHER_STAR)
                .addBlockAt(4, 0, -4, Material.NETHER_STAR)
                .addBlockAt(-1, 0, 1, Material.NETHER_STAR)
                .addBlockAt(-2, 0, 2, Material.NETHER_STAR)
                .addBlockAt(-3, 0, 3, Material.NETHER_STAR)
                .addBlockAt(-4, 0, 4, Material.NETHER_STAR)
                // Üstte özel blok
                .addBlockAbove(1, Material.NETHER_STAR)
                // Altta özel blok
                .addBlockBelow(1, Material.BEACON);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Dağ Yok Edici"; }
        
        @Override
        public int getLevel() { return 5; }
    }
    
    // ========== OLUŞTURMA BATARYALARI (25 Batarya) ==========
    
    /**
     * Taş Köprü L1: 3x STONE üst üste
     */
    private static class StoneBridgeL1Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.STONE)
                .addBlockAbove(1, Material.STONE)
                .addBlockBelow(1, Material.STONE);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Taş Köprü"; }
        
        @Override
        public int getLevel() { return 1; }
    }
    
    /**
     * Obsidyen Duvar L1: 3x OBSIDIAN yatay
     */
    private static class ObsidianWallL1Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.OBSIDIAN)
                .addBlockEast(1, Material.OBSIDIAN)
                .addBlockWest(1, Material.OBSIDIAN);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Obsidyen Duvar"; }
        
        @Override
        public int getLevel() { return 1; }
    }
    
    /**
     * Demir Kafes L1: 3x IRON_BARS T şekli
     */
    private static class IronCageL1Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.IRON_BARS)
                .addBlockAbove(1, Material.IRON_BARS)
                .addBlockNorth(1, Material.IRON_BARS)
                .addBlockSouth(1, Material.IRON_BARS);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Demir Kafes"; }
        
        @Override
        public int getLevel() { return 1; }
    }
    
    /**
     * Cam Duvar L1: 2x2 kare
     */
    private static class GlassWallL1Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.GLASS)
                .addBlockEast(1, Material.GLASS)
                .addBlockNorth(1, Material.GLASS)
                .addBlockAt(1, 0, -1, Material.GLASS);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Cam Duvar"; }
        
        @Override
        public int getLevel() { return 1; }
    }
    
    /**
     * Ahşap Barikat L1: Artı (+) şekli
     */
    private static class WoodBarricadeL1Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.OAK_PLANKS)
                .addBlockEast(1, Material.OAK_PLANKS)
                .addBlockWest(1, Material.OAK_PLANKS)
                .addBlockNorth(1, Material.OAK_PLANKS)
                .addBlockSouth(1, Material.OAK_PLANKS);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Ahşap Barikat"; }
        
        @Override
        public int getLevel() { return 1; }
    }
    
    /**
     * Obsidyen Kafes L2: Piramit (3x3 taban + 1 üstte)
     */
    private static class ObsidianCageL2Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.OBSIDIAN)
                .addBlockEast(1, Material.OBSIDIAN)
                .addBlockWest(1, Material.OBSIDIAN)
                .addBlockNorth(1, Material.OBSIDIAN)
                .addBlockSouth(1, Material.OBSIDIAN)
                .addBlockAt(1, 0, -1, Material.OBSIDIAN)
                .addBlockAt(1, 0, 1, Material.OBSIDIAN)
                .addBlockAt(-1, 0, -1, Material.OBSIDIAN)
                .addBlockAt(-1, 0, 1, Material.OBSIDIAN)
                .addBlockAbove(1, Material.IRON_BLOCK);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Obsidyen Kafes"; }
        
        @Override
        public int getLevel() { return 2; }
    }
    
    /**
     * Taş Köprü (Gelişmiş) L2: Yatay çizgi (5 blok)
     */
    private static class StoneBridgeAdvL2Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.STONE)
                .addBlockEast(1, Material.STONE)
                .addBlockEast(2, Material.STONE)
                .addBlockWest(1, Material.STONE)
                .addBlockWest(2, Material.COBBLESTONE);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Taş Köprü (Gelişmiş)"; }
        
        @Override
        public int getLevel() { return 2; }
    }
    
    /**
     * Demir Duvar L2: L şekli
     */
    private static class IronWallL2Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.IRON_BARS)
                .addBlockAbove(1, Material.IRON_BARS)
                .addBlockAbove(2, Material.IRON_BARS)
                .addBlockEast(1, Material.IRON_BARS)
                .addBlockEast(2, Material.IRON_INGOT);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Demir Duvar"; }
        
        @Override
        public int getLevel() { return 2; }
    }
    
    /**
     * Cam Tünel L2: Çapraz X şekli
     */
    private static class GlassTunnelL2Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.GLASS)
                .addBlockAt(1, 0, 1, Material.GLASS)
                .addBlockAt(-1, 0, -1, Material.GLASS)
                .addBlockAt(1, 0, -1, Material.GLASS_PANE)
                .addBlockAt(-1, 0, 1, Material.GLASS);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Cam Tünel"; }
        
        @Override
        public int getLevel() { return 2; }
    }
    
    /**
     * Ahşap Kale L2: 3x3 kare
     */
    private static class WoodCastleL2Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.OAK_PLANKS)
                .addBlockEast(1, Material.OAK_PLANKS)
                .addBlockWest(1, Material.OAK_PLANKS)
                .addBlockNorth(1, Material.OAK_PLANKS)
                .addBlockSouth(1, Material.OAK_PLANKS)
                .addBlockAt(1, 0, -1, Material.OAK_PLANKS)
                .addBlockAt(1, 0, 1, Material.OAK_PLANKS)
                .addBlockAt(-1, 0, -1, Material.OAK_PLANKS)
                .addBlockAt(-1, 0, 1, Material.OAK_LOG);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Ahşap Kale"; }
        
        @Override
        public int getLevel() { return 2; }
    }
    
    /**
     * Obsidyen Kale L3: 2 katlı piramit
     * Merkez: DIAMOND_BLOCK (farklı)
     */
    private static class ObsidianCastleL3Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.DIAMOND_BLOCK) // Merkez farklı
                .addBlockEast(1, Material.OBSIDIAN)
                .addBlockEast(2, Material.OBSIDIAN)
                .addBlockWest(1, Material.OBSIDIAN)
                .addBlockWest(2, Material.OBSIDIAN)
                .addBlockNorth(1, Material.OBSIDIAN)
                .addBlockNorth(2, Material.OBSIDIAN)
                .addBlockSouth(1, Material.OBSIDIAN)
                .addBlockSouth(2, Material.OBSIDIAN)
                .addBlockAt(2, 0, 2, Material.OBSIDIAN)
                .addBlockAt(2, 0, -2, Material.OBSIDIAN)
                .addBlockAt(-2, 0, 2, Material.OBSIDIAN)
                .addBlockAt(-2, 0, -2, Material.OBSIDIAN)
                .addBlockAbove(1, Material.OBSIDIAN)
                .addBlockAt(1, 1, 0, Material.OBSIDIAN)
                .addBlockAt(-1, 1, 0, Material.OBSIDIAN)
                .addBlockAt(0, 1, 1, Material.OBSIDIAN)
                .addBlockAt(0, 1, -1, Material.OBSIDIAN)
                .addBlockAt(1, 1, 1, Material.BEDROCK)
                .addBlockAt(1, 1, -1, Material.OBSIDIAN)
                .addBlockAt(-1, 1, 1, Material.OBSIDIAN)
                .addBlockAt(-1, 1, -1, Material.OBSIDIAN);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Obsidyen Kale"; }
        
        @Override
        public int getLevel() { return 3; }
    }
    
    /**
     * Netherite Köprü L3: H şekli
     * Merkez: GOLD_BLOCK (farklı)
     */
    private static class NetheriteBridgeL3Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.GOLD_BLOCK) // Merkez farklı
                .addBlockEast(1, Material.NETHERITE_BLOCK)
                .addBlockWest(1, Material.NETHERITE_BLOCK)
                .addBlockAbove(1, Material.NETHERITE_BLOCK)
                .addBlockAbove(2, Material.NETHERITE_BLOCK)
                .addBlockAt(1, 1, 0, Material.NETHERITE_BLOCK)
                .addBlockAt(1, 2, 0, Material.NETHERITE_BLOCK)
                .addBlockAt(-1, 1, 0, Material.NETHERITE_BLOCK)
                .addBlockAt(-1, 2, 0, Material.NETHERITE_INGOT);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Netherite Köprü"; }
        
        @Override
        public int getLevel() { return 3; }
    }
    
    /**
     * Demir Hapishane L3: Yıldız şekli
     */
    private static class IronPrisonL3Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.IRON_BARS)
                .addBlockAbove(1, Material.IRON_BARS)
                .addBlockBelow(1, Material.IRON_BARS)
                .addBlockEast(1, Material.IRON_BARS)
                .addBlockWest(1, Material.IRON_BARS)
                .addBlockNorth(1, Material.IRON_BARS)
                .addBlockSouth(1, Material.IRON_BLOCK);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Demir Hapishane"; }
        
        @Override
        public int getLevel() { return 3; }
    }
    
    /**
     * Cam Kule L3: Çapraz kule
     */
    private static class GlassTowerL3Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.GLASS)
                .addBlockAt(1, 1, 1, Material.GLASS)
                .addBlockAt(1, 2, 1, Material.GLASS)
                .addBlockAt(-1, 1, -1, Material.GLASS)
                .addBlockAt(-1, 2, -1, Material.GLASS)
                .addBlockAt(1, 1, -1, Material.GLASS)
                .addBlockAt(1, 2, -1, Material.GLASS_PANE)
                .addBlockAt(-1, 1, 1, Material.GLASS)
                .addBlockAt(-1, 2, 1, Material.GLASS);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Cam Kule"; }
        
        @Override
        public int getLevel() { return 3; }
    }
    
    /**
     * Taş Kale L3: Z şekli
     */
    private static class StoneCastleL3Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.STONE)
                .addBlockEast(1, Material.STONE)
                .addBlockEast(2, Material.STONE)
                .addBlockAt(2, 1, 0, Material.STONE)
                .addBlockAt(1, 1, 0, Material.STONE)
                .addBlockWest(1, Material.STONE)
                .addBlockWest(2, Material.COBBLESTONE);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Taş Kale"; }
        
        @Override
        public int getLevel() { return 3; }
    }
    
    /**
     * Obsidyen Hapishane L4: 3 katlı kule
     * Merkez: END_CRYSTAL (farklı - parlayan)
     */
    private static class ObsidianPrisonL4Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.END_CRYSTAL) // Merkez farklı
                .addBlockEast(1, Material.OBSIDIAN)
                .addBlockWest(1, Material.OBSIDIAN)
                .addBlockNorth(1, Material.OBSIDIAN)
                .addBlockSouth(1, Material.OBSIDIAN)
                .addBlockAt(1, 0, -1, Material.OBSIDIAN)
                .addBlockAt(1, 0, 1, Material.OBSIDIAN)
                .addBlockAt(-1, 0, -1, Material.OBSIDIAN)
                .addBlockAt(-1, 0, 1, Material.OBSIDIAN)
                .addBlockAbove(1, Material.OBSIDIAN)
                .addBlockAt(1, 1, 0, Material.OBSIDIAN)
                .addBlockAt(-1, 1, 0, Material.OBSIDIAN)
                .addBlockAt(0, 1, 1, Material.OBSIDIAN)
                .addBlockAt(0, 1, -1, Material.OBSIDIAN)
                .addBlockAt(1, 1, 1, Material.OBSIDIAN)
                .addBlockAt(1, 1, -1, Material.OBSIDIAN)
                .addBlockAt(-1, 1, 1, Material.OBSIDIAN)
                .addBlockAt(-1, 1, -1, Material.OBSIDIAN)
                .addBlockAbove(2, Material.OBSIDIAN)
                .addBlockAt(1, 2, 0, Material.OBSIDIAN)
                .addBlockAt(-1, 2, 0, Material.OBSIDIAN)
                .addBlockAt(0, 2, 1, Material.OBSIDIAN)
                .addBlockAt(0, 2, -1, Material.OBSIDIAN)
                .addBlockAt(1, 2, 1, Material.OBSIDIAN)
                .addBlockAt(1, 2, -1, Material.OBSIDIAN)
                .addBlockAt(-1, 2, 1, Material.OBSIDIAN)
                .addBlockAt(-1, 2, -1, Material.OBSIDIAN);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Obsidyen Hapishane"; }
        
        @Override
        public int getLevel() { return 4; }
    }
    
    /**
     * Netherite Köprü (Gelişmiş) L4: Çapraz spiral
     * Merkez: BEACON (farklı - parlayan)
     */
    private static class NetheriteBridgeAdvL4Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.BEACON) // Merkez farklı
                .addBlockAt(1, 0, 1, Material.NETHERITE_BLOCK)
                .addBlockAt(1, 1, 1, Material.NETHERITE_BLOCK)
                .addBlockAt(1, 2, 1, Material.NETHERITE_BLOCK)
                .addBlockAt(-1, 0, -1, Material.NETHERITE_BLOCK)
                .addBlockAt(-1, 1, -1, Material.NETHERITE_BLOCK)
                .addBlockAt(-1, 2, -1, Material.NETHERITE_BLOCK)
                .addBlockAt(1, 0, -1, Material.NETHERITE_BLOCK)
                .addBlockAt(1, 1, -1, Material.NETHERITE_BLOCK)
                .addBlockAt(-1, 0, 1, Material.NETHERITE_BLOCK)
                .addBlockAt(-1, 1, 1, Material.NETHERITE_BLOCK)
                .addBlockAt(-1, 2, 1, Material.BEACON);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Netherite Köprü (Gelişmiş)"; }
        
        @Override
        public int getLevel() { return 4; }
    }
    
    /**
     * Demir Kale L4: Kale şekli
     * Merkez: ANVIL (farklı)
     */
    private static class IronCastleL4Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.ANVIL) // Merkez farklı
                .addBlockEast(1, Material.IRON_BARS)
                .addBlockEast(2, Material.IRON_BARS)
                .addBlockWest(1, Material.IRON_BARS)
                .addBlockWest(2, Material.IRON_BARS)
                .addBlockNorth(1, Material.IRON_BARS)
                .addBlockNorth(2, Material.IRON_BARS)
                .addBlockSouth(1, Material.IRON_BARS)
                .addBlockSouth(2, Material.IRON_BARS)
                .addBlockAt(2, 0, 2, Material.IRON_BARS)
                .addBlockAt(2, 0, -2, Material.IRON_BARS)
                .addBlockAt(-2, 0, 2, Material.IRON_BARS)
                .addBlockAt(-2, 0, -2, Material.ANVIL)
                .addBlockAt(2, 1, 2, Material.IRON_BARS)
                .addBlockAt(2, 1, -2, Material.IRON_BARS)
                .addBlockAt(-2, 1, 2, Material.IRON_BARS)
                .addBlockAt(-2, 1, -2, Material.IRON_BARS);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Demir Kale"; }
        
        @Override
        public int getLevel() { return 4; }
    }
    
    /**
     * Cam Kule (Gelişmiş) L4: Yıldız şekli (8 uçlu)
     * Merkez: GLOWSTONE (farklı - parlayan)
     */
    private static class GlassTowerAdvL4Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.GLOWSTONE) // Merkez farklı
                .addBlockEast(1, Material.GLASS)
                .addBlockEast(2, Material.GLASS)
                .addBlockWest(1, Material.GLASS)
                .addBlockWest(2, Material.GLASS)
                .addBlockNorth(1, Material.GLASS)
                .addBlockNorth(2, Material.GLASS)
                .addBlockSouth(1, Material.GLASS)
                .addBlockSouth(2, Material.GLASS)
                .addBlockAt(1, 0, 1, Material.GLASS)
                .addBlockAt(2, 0, 2, Material.GLASS)
                .addBlockAt(-1, 0, -1, Material.GLASS)
                .addBlockAt(-2, 0, -2, Material.GLASS)
                .addBlockAt(1, 0, -1, Material.GLASS)
                .addBlockAt(2, 0, -2, Material.GLASS)
                .addBlockAt(-1, 0, 1, Material.BEACON)
                .addBlockAt(-2, 0, 2, Material.GLASS);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Cam Kule (Gelişmiş)"; }
        
        @Override
        public int getLevel() { return 4; }
    }
    
    /**
     * Taş Şato L4: Kare halka
     * Merkez: BEACON (farklı - parlayan)
     */
    private static class StoneFortressL4Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.BEACON) // Merkez farklı
                .addBlockEast(1, Material.STONE)
                .addBlockEast(2, Material.STONE)
                .addBlockWest(1, Material.STONE)
                .addBlockWest(2, Material.STONE)
                .addBlockNorth(1, Material.STONE)
                .addBlockNorth(2, Material.STONE)
                .addBlockSouth(1, Material.STONE)
                .addBlockSouth(2, Material.STONE)
                .addBlockAt(2, 0, 2, Material.STONE)
                .addBlockAt(2, 0, -2, Material.STONE)
                .addBlockAt(-2, 0, 2, Material.STONE)
                .addBlockAt(-2, 0, -2, Material.STONE)
                .addBlockAt(2, 1, 2, Material.STONE)
                .addBlockAt(2, 1, -2, Material.STONE)
                .addBlockAt(-2, 1, 2, Material.STONE)
                .addBlockAt(-2, 1, -2, Material.BEACON)
                .addBlockAt(2, 1, 0, Material.STONE)
                .addBlockAt(-2, 1, 0, Material.STONE)
                .addBlockAt(0, 1, 2, Material.STONE)
                .addBlockAt(0, 1, -2, Material.STONE);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Taş Şato"; }
        
        @Override
        public int getLevel() { return 4; }
    }
    
    /**
     * Obsidyen Hapishane (Efsanevi) L5: Büyük piramit
     * Merkez: BEACON (farklı - parlayan)
     */
    private static class ObsidianPrisonLegL5Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.BEACON) // Merkez farklı
                .addBlockBelow(1, Material.OBSIDIAN)
                .addBlockEast(1, Material.OBSIDIAN)
                .addBlockEast(2, Material.OBSIDIAN)
                .addBlockWest(1, Material.OBSIDIAN)
                .addBlockWest(2, Material.OBSIDIAN)
                .addBlockNorth(1, Material.OBSIDIAN)
                .addBlockNorth(2, Material.OBSIDIAN)
                .addBlockSouth(1, Material.OBSIDIAN)
                .addBlockSouth(2, Material.OBSIDIAN)
                .addBlockAbove(1, Material.OBSIDIAN)
                .addBlockAt(1, 1, 0, Material.OBSIDIAN)
                .addBlockAt(-1, 1, 0, Material.OBSIDIAN)
                .addBlockAt(0, 1, 1, Material.OBSIDIAN)
                .addBlockAt(0, 1, -1, Material.OBSIDIAN)
                .addBlockAbove(2, Material.END_CRYSTAL)
                .addBlockBelow(2, Material.BEACON);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Obsidyen Hapishane (Efsanevi)"; }
        
        @Override
        public int getLevel() { return 5; }
    }
    
    /**
     * Netherite Köprü (Efsanevi) L5: Yatay dalga
     * Merkez: NETHER_STAR (farklı - parlayan)
     */
    private static class NetheriteBridgeLegL5Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.NETHER_STAR) // Merkez farklı
                .addBlockEast(1, Material.NETHERITE_BLOCK)
                .addBlockEast(2, Material.NETHERITE_BLOCK)
                .addBlockWest(1, Material.NETHERITE_BLOCK)
                .addBlockWest(2, Material.NETHERITE_BLOCK)
                .addBlockNorth(1, Material.NETHERITE_BLOCK)
                .addBlockNorth(2, Material.NETHERITE_BLOCK)
                .addBlockSouth(1, Material.NETHERITE_BLOCK)
                .addBlockSouth(2, Material.NETHERITE_BLOCK)
                .addBlockAt(2, 0, 2, Material.NETHERITE_BLOCK)
                .addBlockAt(2, 0, -2, Material.NETHERITE_BLOCK)
                .addBlockAt(-2, 0, 2, Material.NETHERITE_BLOCK)
                .addBlockAt(-2, 0, -2, Material.NETHERITE_BLOCK)
                .addBlockAbove(1, Material.BEACON)
                .addBlockBelow(1, Material.BEACON);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Netherite Köprü (Efsanevi)"; }
        
        @Override
        public int getLevel() { return 5; }
    }
    
    /**
     * Demir Kale (Efsanevi) L5: T şekli 3D
     * Merkez: ANVIL (farklı)
     */
    private static class IronCastleLegL5Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.ANVIL) // Merkez farklı
                .addBlockEast(1, Material.IRON_BARS)
                .addBlockEast(2, Material.IRON_BARS)
                .addBlockWest(1, Material.IRON_BARS)
                .addBlockWest(2, Material.IRON_BARS)
                .addBlockNorth(1, Material.IRON_BARS)
                .addBlockNorth(2, Material.IRON_BARS)
                .addBlockAbove(1, Material.IRON_BARS)
                .addBlockAbove(2, Material.IRON_BARS)
                .addBlockBelow(1, Material.IRON_BARS)
                .addBlockBelow(2, Material.IRON_BARS)
                .addBlockAbove(3, Material.ANVIL)
                .addBlockBelow(3, Material.BEACON);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Demir Kale (Efsanevi)"; }
        
        @Override
        public int getLevel() { return 5; }
    }
    
    /**
     * Cam Kule (Efsanevi) L5: Büyük kare
     * Merkez: END_CRYSTAL (farklı - parlayan)
     */
    private static class GlassTowerLegL5Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.END_CRYSTAL) // Merkez farklı
                .addBlockEast(1, Material.GLASS)
                .addBlockEast(2, Material.GLASS)
                .addBlockEast(3, Material.GLASS)
                .addBlockWest(1, Material.GLASS)
                .addBlockWest(2, Material.GLASS)
                .addBlockWest(3, Material.GLASS)
                .addBlockNorth(1, Material.GLASS)
                .addBlockNorth(2, Material.GLASS)
                .addBlockNorth(3, Material.GLASS)
                .addBlockSouth(1, Material.GLASS)
                .addBlockSouth(2, Material.GLASS)
                .addBlockSouth(3, Material.GLASS)
                .addBlockAt(3, 0, 3, Material.GLASS)
                .addBlockAt(3, 0, -3, Material.GLASS)
                .addBlockAt(-3, 0, 3, Material.GLASS)
                .addBlockAt(-3, 0, -3, Material.GLASS)
                .addBlockAt(2, 0, 3, Material.GLASS)
                .addBlockAt(3, 0, 2, Material.GLASS)
                .addBlockAt(2, 0, -3, Material.GLASS)
                .addBlockAt(3, 0, -2, Material.GLASS)
                .addBlockAt(-2, 0, 3, Material.GLASS)
                .addBlockAt(-3, 0, 2, Material.GLASS)
                .addBlockAt(-2, 0, -3, Material.GLASS)
                .addBlockAt(-3, 0, -2, Material.GLASS)
                .addBlockAbove(1, Material.BEACON)
                .addBlockBelow(1, Material.BEACON);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Cam Kule (Efsanevi)"; }
        
        @Override
        public int getLevel() { return 5; }
    }
    
    /**
     * Taş Kalesi (Efsanevi) L5: Çapraz X şekli 3D
     * Merkez: BEACON (farklı - parlayan)
     */
    private static class StoneFortressLegL5Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.BEACON) // Merkez farklı
                .addBlockAt(1, 0, 1, Material.STONE)
                .addBlockAt(2, 0, 2, Material.STONE)
                .addBlockAt(3, 0, 3, Material.STONE)
                .addBlockAt(4, 0, 4, Material.STONE)
                .addBlockAt(-1, 0, -1, Material.STONE)
                .addBlockAt(-2, 0, -2, Material.STONE)
                .addBlockAt(-3, 0, -3, Material.STONE)
                .addBlockAt(-4, 0, -4, Material.STONE)
                .addBlockAt(1, 0, -1, Material.STONE)
                .addBlockAt(2, 0, -2, Material.STONE)
                .addBlockAt(3, 0, -3, Material.STONE)
                .addBlockAt(4, 0, -4, Material.STONE)
                .addBlockAt(-1, 0, 1, Material.STONE)
                .addBlockAt(-2, 0, 2, Material.STONE)
                .addBlockAt(-3, 0, 3, Material.STONE)
                .addBlockAt(-4, 0, 4, Material.STONE)
                .addBlockAbove(1, Material.BEACON)
                .addBlockBelow(1, Material.BEACON);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Taş Kalesi (Efsanevi)"; }
        
        @Override
        public int getLevel() { return 5; }
    }
    
    // ========== DESTEK BATARYALARI (25 Batarya) ==========
    
    /**
     * Can Yenileme L1: 3x GOLD_BLOCK üst üste
     */
    private static class HealL1Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.GOLD_BLOCK)
                .addBlockAbove(1, Material.GOLD_BLOCK)
                .addBlockBelow(1, Material.GOLD_BLOCK);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Can Yenileme"; }
        
        @Override
        public int getLevel() { return 1; }
    }
    
    /**
     * Hız Artışı L1: 3x EMERALD_BLOCK yatay
     */
    private static class SpeedL1Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.EMERALD_BLOCK)
                .addBlockEast(1, Material.EMERALD_BLOCK)
                .addBlockWest(1, Material.EMERALD_BLOCK);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Hız Artışı"; }
        
        @Override
        public int getLevel() { return 1; }
    }
    
    /**
     * Hasar Artışı L1: T şekli
     */
    private static class DamageL1Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.DIAMOND_BLOCK)
                .addBlockAbove(1, Material.DIAMOND_BLOCK)
                .addBlockNorth(1, Material.DIAMOND_BLOCK)
                .addBlockSouth(1, Material.DIAMOND_BLOCK);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Hasar Artışı"; }
        
        @Override
        public int getLevel() { return 1; }
    }
    
    /**
     * Zırh Artışı L1: 2x2 kare
     */
    private static class ArmorL1Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.IRON_BARS)
                .addBlockEast(1, Material.IRON_BARS)
                .addBlockNorth(1, Material.IRON_BARS)
                .addBlockAt(1, 0, -1, Material.IRON_BARS);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Zırh Artışı"; }
        
        @Override
        public int getLevel() { return 1; }
    }
    
    /**
     * Yenilenme L1: Artı (+) şekli
     */
    private static class RegenerationL1Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.LAPIS_BLOCK)
                .addBlockEast(1, Material.LAPIS_BLOCK)
                .addBlockWest(1, Material.LAPIS_BLOCK)
                .addBlockNorth(1, Material.LAPIS_BLOCK)
                .addBlockSouth(1, Material.LAPIS_BLOCK);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Yenilenme"; }
        
        @Override
        public int getLevel() { return 1; }
    }
    
    /**
     * Can + Hız Kombinasyonu L2: Piramit
     */
    private static class HealSpeedComboL2Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.GOLD_BLOCK)
                .addBlockEast(1, Material.GOLD_BLOCK)
                .addBlockWest(1, Material.GOLD_BLOCK)
                .addBlockNorth(1, Material.GOLD_BLOCK)
                .addBlockSouth(1, Material.GOLD_BLOCK)
                .addBlockAt(1, 0, -1, Material.GOLD_BLOCK)
                .addBlockAt(1, 0, 1, Material.GOLD_BLOCK)
                .addBlockAt(-1, 0, -1, Material.GOLD_BLOCK)
                .addBlockAt(-1, 0, 1, Material.GOLD_BLOCK)
                .addBlockAbove(1, Material.EMERALD);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Can + Hız Kombinasyonu"; }
        
        @Override
        public int getLevel() { return 2; }
    }
    
    /**
     * Hasar + Zırh Kombinasyonu L2: Yatay çizgi
     */
    private static class DamageArmorComboL2Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.DIAMOND_BLOCK)
                .addBlockEast(1, Material.DIAMOND_BLOCK)
                .addBlockEast(2, Material.DIAMOND_BLOCK)
                .addBlockWest(1, Material.DIAMOND_BLOCK)
                .addBlockWest(2, Material.IRON_INGOT);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Hasar + Zırh Kombinasyonu"; }
        
        @Override
        public int getLevel() { return 2; }
    }
    
    /**
     * Yenilenme + Can Kombinasyonu L2: L şekli
     */
    private static class RegenerationHealComboL2Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.LAPIS_BLOCK)
                .addBlockAbove(1, Material.LAPIS_BLOCK)
                .addBlockAbove(2, Material.LAPIS_BLOCK)
                .addBlockEast(1, Material.LAPIS_BLOCK)
                .addBlockEast(2, Material.GOLD_INGOT);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Yenilenme + Can Kombinasyonu"; }
        
        @Override
        public int getLevel() { return 2; }
    }
    
    /**
     * Hız + Hasar Kombinasyonu L2: Çapraz X
     */
    private static class SpeedDamageComboL2Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.EMERALD_BLOCK)
                .addBlockAt(1, 0, 1, Material.EMERALD_BLOCK)
                .addBlockAt(-1, 0, -1, Material.EMERALD_BLOCK)
                .addBlockAt(1, 0, -1, Material.DIAMOND)
                .addBlockAt(-1, 0, 1, Material.EMERALD_BLOCK);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Hız + Hasar Kombinasyonu"; }
        
        @Override
        public int getLevel() { return 2; }
    }
    
    /**
     * Zırh + Yenilenme Kombinasyonu L2: 3x3 kare
     */
    private static class ArmorRegenerationComboL2Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.IRON_BARS)
                .addBlockEast(1, Material.IRON_BARS)
                .addBlockWest(1, Material.IRON_BARS)
                .addBlockNorth(1, Material.IRON_BARS)
                .addBlockSouth(1, Material.IRON_BARS)
                .addBlockAt(1, 0, -1, Material.IRON_BARS)
                .addBlockAt(1, 0, 1, Material.IRON_BARS)
                .addBlockAt(-1, 0, -1, Material.IRON_BARS)
                .addBlockAt(-1, 0, 1, Material.LAPIS_LAZULI);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Zırh + Yenilenme Kombinasyonu"; }
        
        @Override
        public int getLevel() { return 2; }
    }
    
    /**
     * Absorption Kalkanı L3: 2 katlı piramit
     * Merkez: GOLDEN_APPLE (farklı)
     */
    private static class AbsorptionShieldL3Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.GOLDEN_APPLE) // Merkez farklı
                .addBlockEast(1, Material.GOLD_BLOCK)
                .addBlockEast(2, Material.GOLD_BLOCK)
                .addBlockWest(1, Material.GOLD_BLOCK)
                .addBlockWest(2, Material.GOLD_BLOCK)
                .addBlockNorth(1, Material.GOLD_BLOCK)
                .addBlockNorth(2, Material.GOLD_BLOCK)
                .addBlockSouth(1, Material.GOLD_BLOCK)
                .addBlockSouth(2, Material.GOLD_BLOCK)
                .addBlockAt(2, 0, 2, Material.GOLD_BLOCK)
                .addBlockAt(2, 0, -2, Material.GOLD_BLOCK)
                .addBlockAt(-2, 0, 2, Material.GOLD_BLOCK)
                .addBlockAt(-2, 0, -2, Material.GOLD_BLOCK)
                .addBlockAbove(1, Material.GOLD_BLOCK)
                .addBlockAt(1, 1, 0, Material.GOLD_BLOCK)
                .addBlockAt(-1, 1, 0, Material.GOLD_BLOCK)
                .addBlockAt(0, 1, 1, Material.GOLD_BLOCK)
                .addBlockAt(0, 1, -1, Material.GOLD_BLOCK)
                .addBlockAt(1, 1, 1, Material.GOLD_BLOCK)
                .addBlockAt(1, 1, -1, Material.GOLD_BLOCK)
                .addBlockAt(-1, 1, 1, Material.GOLD_BLOCK)
                .addBlockAt(-1, 1, -1, Material.GOLD_BLOCK);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Absorption Kalkanı"; }
        
        @Override
        public int getLevel() { return 3; }
    }
    
    /**
     * Uçma Yeteneği L3: H şekli
     * Merkez: FEATHER (farklı)
     */
    private static class FlightL3Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.FEATHER) // Merkez farklı
                .addBlockEast(1, Material.EMERALD_BLOCK)
                .addBlockWest(1, Material.EMERALD_BLOCK)
                .addBlockAbove(1, Material.EMERALD_BLOCK)
                .addBlockAbove(2, Material.EMERALD_BLOCK)
                .addBlockAt(1, 1, 0, Material.EMERALD_BLOCK)
                .addBlockAt(1, 2, 0, Material.EMERALD_BLOCK)
                .addBlockAt(-1, 1, 0, Material.EMERALD_BLOCK)
                .addBlockAt(-1, 2, 0, Material.EMERALD_BLOCK);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Uçma Yeteneği"; }
        
        @Override
        public int getLevel() { return 3; }
    }
    
    /**
     * Kritik Vuruş Artışı L3: Yıldız şekli
     */
    private static class CriticalStrikeL3Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.DIAMOND_BLOCK)
                .addBlockAbove(1, Material.DIAMOND_BLOCK)
                .addBlockBelow(1, Material.DIAMOND_BLOCK)
                .addBlockEast(1, Material.DIAMOND_BLOCK)
                .addBlockWest(1, Material.DIAMOND_BLOCK)
                .addBlockNorth(1, Material.DIAMOND_BLOCK)
                .addBlockSouth(1, Material.DIAMOND_SWORD);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Kritik Vuruş Artışı"; }
        
        @Override
        public int getLevel() { return 3; }
    }
    
    /**
     * Yansıtma Kalkanı L3: Çapraz kule
     */
    private static class ReflectionShieldL3Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.IRON_BARS)
                .addBlockAt(1, 1, 1, Material.IRON_BARS)
                .addBlockAt(1, 2, 1, Material.IRON_BARS)
                .addBlockAt(-1, 1, -1, Material.IRON_BARS)
                .addBlockAt(-1, 2, -1, Material.IRON_BARS)
                .addBlockAt(1, 1, -1, Material.IRON_BARS)
                .addBlockAt(1, 2, -1, Material.SHIELD)
                .addBlockAt(-1, 1, 1, Material.IRON_BARS)
                .addBlockAt(-1, 2, 1, Material.IRON_BARS);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Yansıtma Kalkanı"; }
        
        @Override
        public int getLevel() { return 3; }
    }
    
    /**
     * Can Çalma L3: Z şekli
     */
    private static class LifeStealL3Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.LAPIS_BLOCK)
                .addBlockEast(1, Material.LAPIS_BLOCK)
                .addBlockEast(2, Material.LAPIS_BLOCK)
                .addBlockAt(2, 1, 0, Material.LAPIS_BLOCK)
                .addBlockAt(1, 1, 0, Material.LAPIS_BLOCK)
                .addBlockWest(1, Material.LAPIS_BLOCK)
                .addBlockWest(2, Material.ROTTEN_FLESH);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Can Çalma"; }
        
        @Override
        public int getLevel() { return 3; }
    }
    
    /**
     * Tam Can + Absorption L4: 3 katlı kule
     * Merkez: ENCHANTED_GOLDEN_APPLE (farklı)
     */
    private static class FullHealAbsorptionL4Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.ENCHANTED_GOLDEN_APPLE) // Merkez farklı
                .addBlockEast(1, Material.GOLD_BLOCK)
                .addBlockWest(1, Material.GOLD_BLOCK)
                .addBlockNorth(1, Material.GOLD_BLOCK)
                .addBlockSouth(1, Material.GOLD_BLOCK)
                .addBlockAt(1, 0, -1, Material.GOLD_BLOCK)
                .addBlockAt(1, 0, 1, Material.GOLD_BLOCK)
                .addBlockAt(-1, 0, -1, Material.GOLD_BLOCK)
                .addBlockAt(-1, 0, 1, Material.GOLD_BLOCK)
                .addBlockAbove(1, Material.GOLD_BLOCK)
                .addBlockAt(1, 1, 0, Material.GOLD_BLOCK)
                .addBlockAt(-1, 1, 0, Material.GOLD_BLOCK)
                .addBlockAt(0, 1, 1, Material.GOLD_BLOCK)
                .addBlockAt(0, 1, -1, Material.GOLD_BLOCK)
                .addBlockAt(1, 1, 1, Material.GOLD_BLOCK)
                .addBlockAt(1, 1, -1, Material.GOLD_BLOCK)
                .addBlockAt(-1, 1, 1, Material.GOLD_BLOCK)
                .addBlockAt(-1, 1, -1, Material.GOLD_BLOCK)
                .addBlockAbove(2, Material.GOLD_BLOCK)
                .addBlockAt(1, 2, 0, Material.GOLD_BLOCK)
                .addBlockAt(-1, 2, 0, Material.GOLD_BLOCK)
                .addBlockAt(0, 2, 1, Material.GOLD_BLOCK)
                .addBlockAt(0, 2, -1, Material.GOLD_BLOCK)
                .addBlockAt(1, 2, 1, Material.GOLD_BLOCK)
                .addBlockAt(1, 2, -1, Material.GOLD_BLOCK)
                .addBlockAt(-1, 2, 1, Material.GOLD_BLOCK)
                .addBlockAt(-1, 2, -1, Material.GOLD_BLOCK);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Tam Can + Absorption"; }
        
        @Override
        public int getLevel() { return 4; }
    }
    
    /**
     * Zaman Yavaşlatma L4: Çapraz spiral
     * Merkez: CLOCK (farklı)
     */
    private static class TimeSlowL4Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.CLOCK) // Merkez farklı
                .addBlockAt(1, 0, 1, Material.EMERALD_BLOCK)
                .addBlockAt(1, 1, 1, Material.EMERALD_BLOCK)
                .addBlockAt(1, 2, 1, Material.EMERALD_BLOCK)
                .addBlockAt(-1, 0, -1, Material.EMERALD_BLOCK)
                .addBlockAt(-1, 1, -1, Material.EMERALD_BLOCK)
                .addBlockAt(-1, 2, -1, Material.EMERALD_BLOCK)
                .addBlockAt(1, 0, -1, Material.EMERALD_BLOCK)
                .addBlockAt(1, 1, -1, Material.EMERALD_BLOCK)
                .addBlockAt(-1, 0, 1, Material.EMERALD_BLOCK)
                .addBlockAt(-1, 1, 1, Material.EMERALD_BLOCK)
                .addBlockAt(-1, 2, 1, Material.EMERALD_BLOCK);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Zaman Yavaşlatma"; }
        
        @Override
        public int getLevel() { return 4; }
    }
    
    /**
     * Yıldırım Vuruşu L4: Kale şekli
     * Merkez: LIGHTNING_ROD (farklı)
     */
    private static class LightningStrikeL4Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.LIGHTNING_ROD) // Merkez farklı
                .addBlockEast(1, Material.DIAMOND_BLOCK)
                .addBlockEast(2, Material.DIAMOND_BLOCK)
                .addBlockWest(1, Material.DIAMOND_BLOCK)
                .addBlockWest(2, Material.DIAMOND_BLOCK)
                .addBlockNorth(1, Material.DIAMOND_BLOCK)
                .addBlockNorth(2, Material.DIAMOND_BLOCK)
                .addBlockSouth(1, Material.DIAMOND_BLOCK)
                .addBlockSouth(2, Material.DIAMOND_BLOCK)
                .addBlockAt(2, 0, 2, Material.DIAMOND_BLOCK)
                .addBlockAt(2, 0, -2, Material.DIAMOND_BLOCK)
                .addBlockAt(-2, 0, 2, Material.DIAMOND_BLOCK)
                .addBlockAt(-2, 0, -2, Material.DIAMOND_BLOCK)
                .addBlockAt(2, 1, 2, Material.DIAMOND_BLOCK)
                .addBlockAt(2, 1, -2, Material.DIAMOND_BLOCK)
                .addBlockAt(-2, 1, 2, Material.DIAMOND_BLOCK)
                .addBlockAt(-2, 1, -2, Material.DIAMOND_BLOCK);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Yıldırım Vuruşu"; }
        
        @Override
        public int getLevel() { return 4; }
    }
    
    /**
     * Görünmezlik Kalkanı L4: Yıldız şekli (8 uçlu)
     * Merkez: GLASS_PANE (farklı)
     */
    private static class InvisibilityShieldL4Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.GLASS_PANE) // Merkez farklı
                .addBlockEast(1, Material.IRON_BARS)
                .addBlockEast(2, Material.IRON_BARS)
                .addBlockWest(1, Material.IRON_BARS)
                .addBlockWest(2, Material.IRON_BARS)
                .addBlockNorth(1, Material.IRON_BARS)
                .addBlockNorth(2, Material.IRON_BARS)
                .addBlockSouth(1, Material.IRON_BARS)
                .addBlockSouth(2, Material.IRON_BARS)
                .addBlockAt(1, 0, 1, Material.IRON_BARS)
                .addBlockAt(2, 0, 2, Material.IRON_BARS)
                .addBlockAt(-1, 0, -1, Material.IRON_BARS)
                .addBlockAt(-2, 0, -2, Material.IRON_BARS)
                .addBlockAt(1, 0, -1, Material.IRON_BARS)
                .addBlockAt(2, 0, -2, Material.IRON_BARS)
                .addBlockAt(-1, 0, 1, Material.GLASS_PANE)
                .addBlockAt(-2, 0, 2, Material.IRON_BARS);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Görünmezlik Kalkanı"; }
        
        @Override
        public int getLevel() { return 4; }
    }
    
    /**
     * Ölümsüzlük Anı L4: Kare halka
     * Merkez: TOTEM_OF_UNDYING (farklı)
     */
    private static class ImmortalityMomentL4Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.TOTEM_OF_UNDYING) // Merkez farklı
                .addBlockEast(1, Material.LAPIS_BLOCK)
                .addBlockEast(2, Material.LAPIS_BLOCK)
                .addBlockWest(1, Material.LAPIS_BLOCK)
                .addBlockWest(2, Material.LAPIS_BLOCK)
                .addBlockNorth(1, Material.LAPIS_BLOCK)
                .addBlockNorth(2, Material.LAPIS_BLOCK)
                .addBlockSouth(1, Material.LAPIS_BLOCK)
                .addBlockSouth(2, Material.LAPIS_BLOCK)
                .addBlockAt(2, 0, 2, Material.LAPIS_BLOCK)
                .addBlockAt(2, 0, -2, Material.LAPIS_BLOCK)
                .addBlockAt(-2, 0, 2, Material.LAPIS_BLOCK)
                .addBlockAt(-2, 0, -2, Material.LAPIS_BLOCK)
                .addBlockAt(2, 1, 2, Material.LAPIS_BLOCK)
                .addBlockAt(2, 1, -2, Material.LAPIS_BLOCK)
                .addBlockAt(-2, 1, 2, Material.LAPIS_BLOCK)
                .addBlockAt(-2, 1, -2, Material.TOTEM_OF_UNDYING)
                .addBlockAt(2, 1, 0, Material.LAPIS_BLOCK)
                .addBlockAt(-2, 1, 0, Material.LAPIS_BLOCK)
                .addBlockAt(0, 1, 2, Material.LAPIS_BLOCK)
                .addBlockAt(0, 1, -2, Material.LAPIS_BLOCK);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Ölümsüzlük Anı"; }
        
        @Override
        public int getLevel() { return 4; }
    }
    
    /**
     * Efsanevi Can Yenileme L5: Büyük piramit
     * Merkez: NETHER_STAR (farklı - parlayan)
     */
    private static class LegendaryHealL5Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.NETHER_STAR) // Merkez farklı
                .addBlockBelow(1, Material.GOLD_BLOCK)
                .addBlockEast(1, Material.GOLD_BLOCK)
                .addBlockEast(2, Material.GOLD_BLOCK)
                .addBlockWest(1, Material.GOLD_BLOCK)
                .addBlockWest(2, Material.GOLD_BLOCK)
                .addBlockNorth(1, Material.GOLD_BLOCK)
                .addBlockNorth(2, Material.GOLD_BLOCK)
                .addBlockSouth(1, Material.GOLD_BLOCK)
                .addBlockSouth(2, Material.GOLD_BLOCK)
                .addBlockAbove(1, Material.GOLD_BLOCK)
                .addBlockAt(1, 1, 0, Material.GOLD_BLOCK)
                .addBlockAt(-1, 1, 0, Material.GOLD_BLOCK)
                .addBlockAt(0, 1, 1, Material.GOLD_BLOCK)
                .addBlockAt(0, 1, -1, Material.GOLD_BLOCK)
                .addBlockAbove(2, Material.NETHER_STAR)
                .addBlockBelow(2, Material.BEACON);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Efsanevi Can Yenileme"; }
        
        @Override
        public int getLevel() { return 5; }
    }
    
    /**
     * Zaman Durdurma L5: Yatay dalga
     * Merkez: CLOCK (farklı)
     */
    private static class TimeStopL5Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.CLOCK) // Merkez farklı
                .addBlockEast(1, Material.EMERALD_BLOCK)
                .addBlockEast(2, Material.EMERALD_BLOCK)
                .addBlockWest(1, Material.EMERALD_BLOCK)
                .addBlockWest(2, Material.EMERALD_BLOCK)
                .addBlockNorth(1, Material.EMERALD_BLOCK)
                .addBlockNorth(2, Material.EMERALD_BLOCK)
                .addBlockSouth(1, Material.EMERALD_BLOCK)
                .addBlockSouth(2, Material.EMERALD_BLOCK)
                .addBlockAt(2, 0, 2, Material.EMERALD_BLOCK)
                .addBlockAt(2, 0, -2, Material.EMERALD_BLOCK)
                .addBlockAt(-2, 0, 2, Material.EMERALD_BLOCK)
                .addBlockAt(-2, 0, -2, Material.EMERALD_BLOCK)
                .addBlockAbove(1, Material.NETHER_STAR)
                .addBlockBelow(1, Material.BEACON);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Zaman Durdurma"; }
        
        @Override
        public int getLevel() { return 5; }
    }
    
    /**
     * Ölüm Dokunuşu L5: T şekli 3D
     * Merkez: WITHER_SKELETON_SKULL (farklı)
     */
    private static class DeathTouchL5Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.WITHER_SKELETON_SKULL) // Merkez farklı
                .addBlockEast(1, Material.DIAMOND_BLOCK)
                .addBlockEast(2, Material.DIAMOND_BLOCK)
                .addBlockWest(1, Material.DIAMOND_BLOCK)
                .addBlockWest(2, Material.DIAMOND_BLOCK)
                .addBlockNorth(1, Material.DIAMOND_BLOCK)
                .addBlockNorth(2, Material.DIAMOND_BLOCK)
                .addBlockAbove(1, Material.DIAMOND_BLOCK)
                .addBlockAbove(2, Material.DIAMOND_BLOCK)
                .addBlockBelow(1, Material.DIAMOND_BLOCK)
                .addBlockBelow(2, Material.DIAMOND_BLOCK)
                .addBlockAbove(3, Material.NETHER_STAR)
                .addBlockBelow(3, Material.BEACON);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Ölüm Dokunuşu"; }
        
        @Override
        public int getLevel() { return 5; }
    }
    
    /**
     * Faz Değiştirme L5: Büyük kare
     * Merkez: END_CRYSTAL (farklı - parlayan)
     */
    private static class PhaseShiftL5Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.END_CRYSTAL) // Merkez farklı
                .addBlockEast(1, Material.IRON_BARS)
                .addBlockEast(2, Material.IRON_BARS)
                .addBlockEast(3, Material.IRON_BARS)
                .addBlockWest(1, Material.IRON_BARS)
                .addBlockWest(2, Material.IRON_BARS)
                .addBlockWest(3, Material.IRON_BARS)
                .addBlockNorth(1, Material.IRON_BARS)
                .addBlockNorth(2, Material.IRON_BARS)
                .addBlockNorth(3, Material.IRON_BARS)
                .addBlockSouth(1, Material.IRON_BARS)
                .addBlockSouth(2, Material.IRON_BARS)
                .addBlockSouth(3, Material.IRON_BARS)
                .addBlockAt(3, 0, 3, Material.IRON_BARS)
                .addBlockAt(3, 0, -3, Material.IRON_BARS)
                .addBlockAt(-3, 0, 3, Material.IRON_BARS)
                .addBlockAt(-3, 0, -3, Material.IRON_BARS)
                .addBlockAt(2, 0, 3, Material.IRON_BARS)
                .addBlockAt(3, 0, 2, Material.IRON_BARS)
                .addBlockAt(2, 0, -3, Material.IRON_BARS)
                .addBlockAt(3, 0, -2, Material.IRON_BARS)
                .addBlockAt(-2, 0, 3, Material.IRON_BARS)
                .addBlockAt(-3, 0, 2, Material.IRON_BARS)
                .addBlockAt(-2, 0, -3, Material.IRON_BARS)
                .addBlockAt(-3, 0, -2, Material.IRON_BARS)
                .addBlockAbove(1, Material.NETHER_STAR)
                .addBlockBelow(1, Material.BEACON);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Faz Değiştirme"; }
        
        @Override
        public int getLevel() { return 5; }
    }
    
    /**
     * Yeniden Doğuş L5: Çapraz X şekli 3D
     * Merkez: TOTEM_OF_UNDYING (farklı)
     */
    private static class RebirthL5Recipe implements RecipeChecker {
        @Override
        public BlockPattern getPattern() {
            return new BlockPattern(Material.TOTEM_OF_UNDYING) // Merkez farklı
                .addBlockAt(1, 0, 1, Material.LAPIS_BLOCK)
                .addBlockAt(2, 0, 2, Material.LAPIS_BLOCK)
                .addBlockAt(3, 0, 3, Material.LAPIS_BLOCK)
                .addBlockAt(4, 0, 4, Material.LAPIS_BLOCK)
                .addBlockAt(-1, 0, -1, Material.LAPIS_BLOCK)
                .addBlockAt(-2, 0, -2, Material.LAPIS_BLOCK)
                .addBlockAt(-3, 0, -3, Material.LAPIS_BLOCK)
                .addBlockAt(-4, 0, -4, Material.LAPIS_BLOCK)
                .addBlockAt(1, 0, -1, Material.LAPIS_BLOCK)
                .addBlockAt(2, 0, -2, Material.LAPIS_BLOCK)
                .addBlockAt(3, 0, -3, Material.LAPIS_BLOCK)
                .addBlockAt(4, 0, -4, Material.LAPIS_BLOCK)
                .addBlockAt(-1, 0, 1, Material.LAPIS_BLOCK)
                .addBlockAt(-2, 0, 2, Material.LAPIS_BLOCK)
                .addBlockAt(-3, 0, 3, Material.LAPIS_BLOCK)
                .addBlockAt(-4, 0, 4, Material.LAPIS_BLOCK)
                .addBlockAbove(1, Material.NETHER_STAR)
                .addBlockBelow(1, Material.BEACON);
        }
        
        @Override
        public RecipeCheckResult checkRecipe(Block centerBlock) {
            return checkBlockPattern(centerBlock, getPattern(), getBatteryName());
        }
        
        @Override
        public String getBatteryName() { return "Yeniden Doğuş"; }
        
        @Override
        public int getLevel() { return 5; }
    }
    
    // ========== OLUŞTURMA METODLARI ==========
    
    private void createStoneBridge(Player player, Location target, int length, int level) {
        org.bukkit.util.Vector direction = player.getLocation().getDirection().setY(0).normalize();
        int placed = 0;
        
        for (int i = 0; i < length; i++) {
            Location loc = target.clone().add(direction.clone().multiply(i));
            org.bukkit.block.Block block = loc.getBlock();
            if (block.getType() == org.bukkit.Material.AIR) {
                block.setType(org.bukkit.Material.STONE);
                placed++;
            }
        }
        
        player.sendMessage("§7Taş köprü oluşturuldu! (" + placed + " blok)");
    }
    
    private void createObsidianWall(Player player, Location target, int width, int height, int depth, int level) {
        org.bukkit.util.Vector direction = player.getLocation().getDirection().setY(0).normalize();
        org.bukkit.util.Vector perpendicular = new org.bukkit.util.Vector(-direction.getZ(), 0, direction.getX()).normalize();
        
        int placed = 0;
        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                for (int d = 0; d < depth; d++) {
                    Location loc = target.clone().add(perpendicular.clone().multiply(w - width/2))
                        .add(0, h, direction.getZ() * d);
                    org.bukkit.block.Block block = loc.getBlock();
                    if (block.getType() == org.bukkit.Material.AIR) {
                        block.setType(org.bukkit.Material.OBSIDIAN);
                        placed++;
                    }
                }
            }
        }
        
        player.sendMessage("§5Obsidyen duvar oluşturuldu! (" + placed + " blok)");
    }
    
    private void createIronCage(Player player, Location target, int width, int height, int depth, int level) {
        int placed = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    if (x == 0 || x == width-1 || y == 0 || y == height-1 || z == 0 || z == depth-1) {
                        Location loc = target.clone().add(x - width/2, y, z - depth/2);
                        org.bukkit.block.Block block = loc.getBlock();
                        if (block.getType() == org.bukkit.Material.AIR) {
                            block.setType(org.bukkit.Material.IRON_BARS);
                            placed++;
                        }
                    }
                }
            }
        }
        
        player.sendMessage("§7Demir kafes oluşturuldu! (" + placed + " blok)");
    }
    
    private void createGlassWall(Player player, Location target, int width, int height, int depth, int level) {
        org.bukkit.util.Vector direction = player.getLocation().getDirection().setY(0).normalize();
        org.bukkit.util.Vector perpendicular = new org.bukkit.util.Vector(-direction.getZ(), 0, direction.getX()).normalize();
        
        int placed = 0;
        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                for (int d = 0; d < depth; d++) {
                    Location loc = target.clone().add(perpendicular.clone().multiply(w - width/2))
                        .add(0, h, direction.getZ() * d);
                    org.bukkit.block.Block block = loc.getBlock();
                    if (block.getType() == org.bukkit.Material.AIR) {
                        block.setType(org.bukkit.Material.GLASS);
                        placed++;
                    }
                }
            }
        }
        
        player.sendMessage("§bCam duvar oluşturuldu! (" + placed + " blok)");
    }
    
    private void createWoodBarricade(Player player, Location target, int width, int height, int depth, int level) {
        int placed = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    Location loc = target.clone().add(x - width/2, y, z - depth/2);
                    org.bukkit.block.Block block = loc.getBlock();
                    if (block.getType() == org.bukkit.Material.AIR) {
                        block.setType(org.bukkit.Material.OAK_PLANKS);
                        placed++;
                    }
                }
            }
        }
        
        player.sendMessage("§6Ahşap barikat oluşturuldu! (" + placed + " blok)");
    }
    
    private void createObsidianCage(Player player, Location target, int width, int height, int depth, int level) {
        int placed = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    if (x == 0 || x == width-1 || y == 0 || y == height-1 || z == 0 || z == depth-1) {
                        Location loc = target.clone().add(x - width/2, y, z - depth/2);
                        org.bukkit.block.Block block = loc.getBlock();
                        if (block.getType() == org.bukkit.Material.AIR) {
                            block.setType(org.bukkit.Material.OBSIDIAN);
                            placed++;
                        }
                    }
                }
            }
        }
        
        player.sendMessage("§5Obsidyen kafes oluşturuldu! (" + placed + " blok)");
    }
    
    private void createIronWall(Player player, Location target, int width, int height, int depth, int level) {
        org.bukkit.util.Vector direction = player.getLocation().getDirection().setY(0).normalize();
        org.bukkit.util.Vector perpendicular = new org.bukkit.util.Vector(-direction.getZ(), 0, direction.getX()).normalize();
        
        int placed = 0;
        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                for (int d = 0; d < depth; d++) {
                    Location loc = target.clone().add(perpendicular.clone().multiply(w - width/2))
                        .add(0, h, direction.getZ() * d);
                    org.bukkit.block.Block block = loc.getBlock();
                    if (block.getType() == org.bukkit.Material.AIR) {
                        block.setType(org.bukkit.Material.IRON_BLOCK);
                        placed++;
                    }
                }
            }
        }
        
        player.sendMessage("§7Demir duvar oluşturuldu! (" + placed + " blok)");
    }
    
    private void createGlassTunnel(Player player, Location target, int length, int level) {
        org.bukkit.util.Vector direction = player.getLocation().getDirection().setY(0).normalize();
        org.bukkit.util.Vector perpendicular = new org.bukkit.util.Vector(-direction.getZ(), 0, direction.getX()).normalize();
        
        int placed = 0;
        for (int i = 0; i < length; i++) {
            for (int h = 0; h < 3; h++) {
                for (int w = -1; w <= 1; w++) {
                    Location loc = target.clone().add(direction.clone().multiply(i))
                        .add(perpendicular.clone().multiply(w)).add(0, h, 0);
                    org.bukkit.block.Block block = loc.getBlock();
                    if (block.getType() == org.bukkit.Material.AIR) {
                        if (h == 0 || h == 2 || w == -1 || w == 1) {
                            block.setType(org.bukkit.Material.GLASS);
                            placed++;
                        }
                    }
                }
            }
        }
        
        player.sendMessage("§bCam tünel oluşturuldu! (" + placed + " blok)");
    }
    
    private void createWoodCastle(Player player, Location target, int width, int height, int depth, int level) {
        int placed = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    if (x == 0 || x == width-1 || y == 0 || y == height-1 || z == 0 || z == depth-1) {
                        Location loc = target.clone().add(x - width/2, y, z - depth/2);
                        org.bukkit.block.Block block = loc.getBlock();
                        if (block.getType() == org.bukkit.Material.AIR) {
                            block.setType(org.bukkit.Material.OAK_PLANKS);
                            placed++;
                        }
                    }
                }
            }
        }
        
        player.sendMessage("§6Ahşap kale oluşturuldu! (" + placed + " blok)");
    }
    
    private void createNetheriteBridge(Player player, Location target, int length, int level) {
        org.bukkit.util.Vector direction = player.getLocation().getDirection().setY(0).normalize();
        int placed = 0;
        
        for (int i = 0; i < length; i++) {
            Location loc = target.clone().add(direction.clone().multiply(i));
            org.bukkit.block.Block block = loc.getBlock();
            if (block.getType() == org.bukkit.Material.AIR) {
                block.setType(org.bukkit.Material.NETHERITE_BLOCK);
                placed++;
            }
        }
        
        player.sendMessage("§5Netherite köprü oluşturuldu! (" + placed + " blok)");
    }
    
    private void createIronPrison(Player player, Location target, int width, int height, int depth, int level) {
        int placed = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    if (x == 0 || x == width-1 || y == 0 || y == height-1 || z == 0 || z == depth-1) {
                        Location loc = target.clone().add(x - width/2, y, z - depth/2);
                        org.bukkit.block.Block block = loc.getBlock();
                        if (block.getType() == org.bukkit.Material.AIR) {
                            block.setType(org.bukkit.Material.IRON_BLOCK);
                            placed++;
                        }
                    }
                }
            }
        }
        
        player.sendMessage("§7Demir hapishane oluşturuldu! (" + placed + " blok)");
    }
    
    private void createGlassTower(Player player, Location target, int width, int height, int depth, int level) {
        int placed = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    if (x == 0 || x == width-1 || y == 0 || y == height-1 || z == 0 || z == depth-1) {
                        Location loc = target.clone().add(x - width/2, y, z - depth/2);
                        org.bukkit.block.Block block = loc.getBlock();
                        if (block.getType() == org.bukkit.Material.AIR) {
                            block.setType(org.bukkit.Material.GLASS);
                            placed++;
                        }
                    }
                }
            }
        }
        
        player.sendMessage("§bCam kule oluşturuldu! (" + placed + " blok)");
    }
    
    private void createStoneCastle(Player player, Location target, int width, int height, int depth, int level) {
        int placed = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    if (x == 0 || x == width-1 || y == 0 || y == height-1 || z == 0 || z == depth-1) {
                        Location loc = target.clone().add(x - width/2, y, z - depth/2);
                        org.bukkit.block.Block block = loc.getBlock();
                        if (block.getType() == org.bukkit.Material.AIR) {
                            block.setType(org.bukkit.Material.STONE);
                            placed++;
                        }
                    }
                }
            }
        }
        
        player.sendMessage("§7Taş kale oluşturuldu! (" + placed + " blok)");
    }
    
    private void createObsidianCastle(Player player, Location target, int width, int height, int depth, int level) {
        int placed = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    if (x == 0 || x == width-1 || y == 0 || y == height-1 || z == 0 || z == depth-1) {
                        Location loc = target.clone().add(x - width/2, y, z - depth/2);
                        org.bukkit.block.Block block = loc.getBlock();
                        if (block.getType() == org.bukkit.Material.AIR) {
                            block.setType(org.bukkit.Material.OBSIDIAN);
                            placed++;
                        }
                    }
                }
            }
        }
        
        player.sendMessage("§5Obsidyen kale oluşturuldu! (" + placed + " blok)");
    }
    
    private void createStoneFortress(Player player, Location target, int width, int height, int depth, int level) {
        int placed = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    if (x == 0 || x == width-1 || y == 0 || y == height-1 || z == 0 || z == depth-1) {
                        Location loc = target.clone().add(x - width/2, y, z - depth/2);
                        org.bukkit.block.Block block = loc.getBlock();
                        if (block.getType() == org.bukkit.Material.AIR) {
                            block.setType(org.bukkit.Material.STONE);
                            placed++;
                        }
                    }
                }
            }
        }
        
        player.sendMessage("§7Taş şato oluşturuldu! (" + placed + " blok)");
    }
    
    private void createObsidianPrison(Player player, Location target, int width, int height, int depth, int level) {
        int placed = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    if (x == 0 || x == width-1 || y == 0 || y == height-1 || z == 0 || z == depth-1) {
                        Location loc = target.clone().add(x - width/2, y, z - depth/2);
                        org.bukkit.block.Block block = loc.getBlock();
                        if (block.getType() == org.bukkit.Material.AIR) {
                            block.setType(org.bukkit.Material.OBSIDIAN);
                            placed++;
                        }
                    }
                }
            }
        }
        
        player.sendMessage("§5Obsidyen hapishane oluşturuldu! (" + placed + " blok)");
    }
    
    private void createIronCastle(Player player, Location target, int width, int height, int depth, int level) {
        int placed = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    if (x == 0 || x == width-1 || y == 0 || y == height-1 || z == 0 || z == depth-1) {
                        Location loc = target.clone().add(x - width/2, y, z - depth/2);
                        org.bukkit.block.Block block = loc.getBlock();
                        if (block.getType() == org.bukkit.Material.AIR) {
                            block.setType(org.bukkit.Material.IRON_BLOCK);
                            placed++;
                        }
                    }
                }
            }
        }
        
        player.sendMessage("§7Demir kale oluşturuldu! (" + placed + " blok)");
    }
    
    // ========== DESTEK METODLARI ==========
    
    private void applyHealSupport(Player player, double radius, double healAmount, int level) {
        List<Player> members = getNearbyClanMembers(player, radius);
        members.add(player);
        
        for (Player member : members) {
            double newHealth = Math.min(
                member.getHealth() + healAmount,
                member.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue()
            );
            member.setHealth(newHealth);
        }
        
        player.sendMessage("§aCan yenileme uygulandı! (" + members.size() + " oyuncu)");
    }
    
    private void applySpeedSupport(Player player, double radius, int amplifier, int duration, int level) {
        List<Player> members = getNearbyClanMembers(player, radius);
        members.add(player);
        
        for (Player member : members) {
            member.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.SPEED, duration * 20, amplifier, false, false, true));
        }
        
        player.sendMessage("§eHız artışı uygulandı! (" + members.size() + " oyuncu)");
    }
    
    private void applyDamageSupport(Player player, double radius, int amplifier, int duration, int level) {
        List<Player> members = getNearbyClanMembers(player, radius);
        members.add(player);
        
        for (Player member : members) {
            member.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.INCREASE_DAMAGE, duration * 20, amplifier, false, false, true));
        }
        
        player.sendMessage("§cHasar artışı uygulandı! (" + members.size() + " oyuncu)");
    }
    
    private void applyArmorSupport(Player player, double radius, int amplifier, int duration, int level) {
        List<Player> members = getNearbyClanMembers(player, radius);
        members.add(player);
        
        for (Player member : members) {
            member.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.DAMAGE_RESISTANCE, duration * 20, amplifier, false, false, true));
        }
        
        player.sendMessage("§bZırh artışı uygulandı! (" + members.size() + " oyuncu)");
    }
    
    private void applyRegenerationSupport(Player player, double radius, int amplifier, int duration, int level) {
        List<Player> members = getNearbyClanMembers(player, radius);
        members.add(player);
        
        for (Player member : members) {
            member.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.REGENERATION, duration * 20, amplifier, false, false, true));
        }
        
        player.sendMessage("§dYenilenme uygulandı! (" + members.size() + " oyuncu)");
    }
    
    private void applyAbsorptionShield(Player player, double radius, int duration, int level) {
        double absorption = 10.0 * level;
        List<Player> members = getNearbyClanMembers(player, radius);
        members.add(player);
        
        for (Player member : members) {
            member.setAbsorptionAmount((float) absorption);
        }
        
        player.sendMessage("§eAbsorption Kalkanı aktif! (" + members.size() + " oyuncu)");
    }
    
    private void applyFlight(Player player, double radius, int duration, int level) {
        List<Player> members = getNearbyClanMembers(player, radius);
        members.add(player);
        
        for (Player member : members) {
            member.setAllowFlight(true);
            member.setFlying(true);
            
            // Duration sonra uçmayı kapat
            org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (member.isOnline() && !member.getGameMode().equals(org.bukkit.GameMode.CREATIVE)) {
                    member.setAllowFlight(false);
                    member.setFlying(false);
                }
            }, duration * 20L);
        }
        
        player.sendMessage("§bUçma Yeteneği aktif! (" + duration + " saniye)");
    }
    
    private void applyCriticalStrike(Player player, double radius, int duration, int level) {
        List<Player> members = getNearbyClanMembers(player, radius);
        members.add(player);
        
        for (Player member : members) {
            member.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.INCREASE_DAMAGE, duration * 20, level, false, false, true));
        }
        
        player.sendMessage("§cKritik Vuruş Artışı aktif! (" + members.size() + " oyuncu)");
    }
    
    private void applyReflectionShield(Player player, double radius, int duration, int level) {
        List<Player> members = getNearbyClanMembers(player, radius);
        members.add(player);
        
        for (Player member : members) {
            member.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.DAMAGE_RESISTANCE, duration * 20, 2, false, false, true));
        }
        
        player.sendMessage("§6Yansıtma Kalkanı aktif! (" + members.size() + " oyuncu)");
    }
    
    private void applyLifeSteal(Player player, double radius, int duration, int level) {
        List<Player> members = getNearbyClanMembers(player, radius);
        members.add(player);
        
        for (Player member : members) {
            member.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.ABSORPTION, duration * 20, 1, false, false, true));
        }
        
        player.sendMessage("§4Can Çalma aktif! (" + members.size() + " oyuncu)");
    }
    
    private java.util.List<Player> getNearbyClanMembers(Player player, double radius) {
        java.util.List<Player> members = new java.util.ArrayList<>();
        me.mami.stratocraft.manager.ClanManager clanManager = plugin.getClanManager();
        if (clanManager == null) return members;
        
        me.mami.stratocraft.model.Clan playerClan = clanManager.getClanByPlayer(player.getUniqueId());
        if (playerClan == null) return members;
        
        for (Player nearby : player.getWorld().getPlayers()) {
            if (nearby != player && nearby.getLocation().distance(player.getLocation()) <= radius) {
                me.mami.stratocraft.model.Clan nearbyClan = clanManager.getClanByPlayer(nearby.getUniqueId());
                if (nearbyClan != null && nearbyClan.getId().equals(playerClan.getId())) {
                    members.add(nearby);
                }
            }
        }
        
        return members;
    }
    
    private void applyFullHealAbsorption(Player player, double radius, int level) {
        double absorption = 20.0;
        List<Player> members = getNearbyClanMembers(player, radius);
        members.add(player);
        
        for (Player member : members) {
            member.setHealth(member.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue());
            member.setAbsorptionAmount((float) absorption);
        }
        
        player.sendMessage("§aTam Can + Absorption uygulandı! (" + members.size() + " oyuncu)");
    }
    
    private void applyLegendaryHeal(Player player, double radius, int duration, int level) {
        double absorption = 50.0;
        List<Player> members = getNearbyClanMembers(player, radius);
        members.add(player);
        
        for (Player member : members) {
            member.setHealth(member.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue());
            member.setAbsorptionAmount((float) absorption);
            member.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.REGENERATION, duration * 20, 4, false, false, true));
        }
        
        player.sendMessage("§6Efsanevi Can Yenileme uygulandı! (" + members.size() + " oyuncu)");
    }
    
    private void applyTimeSlow(Player player, double radius, int duration, int level) {
        for (org.bukkit.entity.Entity e : player.getNearbyEntities(radius, radius, radius)) {
            if (e instanceof org.bukkit.entity.LivingEntity && e != player) {
                ((org.bukkit.entity.LivingEntity) e).addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.SLOW, duration * 20, 4, false, false, true));
            }
        }
        
        player.sendMessage("§bZaman Yavaşlatma aktif! (" + duration + " saniye)");
    }
    
    private void applyLightningStrike(Player player, double radius, int duration, int level) {
        List<Player> members = getNearbyClanMembers(player, radius);
        members.add(player);
        
        for (Player member : members) {
            member.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.CONDUIT_POWER, duration * 20, 0, false, false, true));
        }
        
        player.sendMessage("§eYıldırım Vuruşu aktif! (" + duration + " saniye)");
    }
    
    private void applyInvisibilityShield(Player player, double radius, int duration, int level) {
        List<Player> members = getNearbyClanMembers(player, radius);
        members.add(player);
        
        for (Player member : members) {
            member.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.INVISIBILITY, duration * 20, 0, false, false, true));
            member.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.DAMAGE_RESISTANCE, duration * 20, 2, false, false, true));
        }
        
        player.sendMessage("§7Görünmezlik Kalkanı aktif! (" + duration + " saniye)");
    }
    
    private void applyImmortalityMoment(Player player, double radius, int level) {
        List<Player> members = getNearbyClanMembers(player, radius);
        members.add(player);
        
        for (Player member : members) {
            member.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.DAMAGE_RESISTANCE, 3 * 20, 255, false, false, true));
        }
        
        player.sendMessage("§6Ölümsüzlük Anı aktif! (3 saniye)");
    }
    
    private void applyTimeStop(Player player, double radius, int duration, int level) {
        java.util.List<org.bukkit.entity.LivingEntity> frozen = new java.util.ArrayList<>();
        for (org.bukkit.entity.Entity e : player.getNearbyEntities(radius, radius, radius)) {
            if (e instanceof org.bukkit.entity.LivingEntity && e != player) {
                org.bukkit.entity.LivingEntity le = (org.bukkit.entity.LivingEntity) e;
                le.setAI(false);
                le.setGravity(false);
                le.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
                frozen.add(le);
            }
        }
        
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                for (org.bukkit.entity.LivingEntity le : frozen) {
                    if (le.isValid()) {
                        le.setAI(true);
                        le.setGravity(true);
                    }
                }
            }
        }.runTaskLater(plugin, duration * 20L);
        
        player.sendMessage("§bZaman Durdurma aktif! (" + duration + " saniye)");
    }
    
    private void applyDeathTouch(Player player, double radius, int duration, int level) {
        List<Player> members = getNearbyClanMembers(player, radius);
        members.add(player);
        
        for (Player member : members) {
            member.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.WITHER, duration * 20, 0, false, false, true));
        }
        
        player.sendMessage("§4Ölüm Dokunuşu aktif! (" + duration + " saniye)");
    }
    
    private void applyPhaseShift(Player player, double radius, int duration, int level) {
        List<Player> members = getNearbyClanMembers(player, radius);
        members.add(player);
        
        for (Player member : members) {
            member.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.DAMAGE_RESISTANCE, duration * 20, 255, false, false, true));
        }
        
        player.sendMessage("§5Faz Değiştirme aktif! (" + duration + " saniye)");
    }
    
    private void applyRebirth(Player player, double radius, int level) {
        List<Player> members = getNearbyClanMembers(player, radius);
        members.add(player);
        
        for (Player member : members) {
            member.sendMessage("§6Yeniden Doğuş hazır! (60 saniye içinde ölürsen canlanacaksın)");
        }
    }
}

