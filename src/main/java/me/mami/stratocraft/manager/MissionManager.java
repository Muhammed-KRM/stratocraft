package me.mami.stratocraft.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.enums.MissionType;
import me.mami.stratocraft.model.Mission;

public class MissionManager {
    private final Map<UUID, Mission> activeMissions = new HashMap<>();
    private final Random random = new Random();
    private DifficultyManager difficultyManager;
    private Main plugin;
    private me.mami.stratocraft.manager.GameBalanceConfig balanceConfig;

    public MissionManager() {
        // Eski constructor - geriye uyumluluk
    }
    
    public MissionManager(DifficultyManager dm, Main plugin) {
        this.difficultyManager = dm;
        this.plugin = plugin;
    }
    
    public void setDifficultyManager(DifficultyManager dm) {
        this.difficultyManager = dm;
    }
    
    public void setPlugin(Main plugin) {
        this.plugin = plugin;
    }
    
    public void setBalanceConfig(me.mami.stratocraft.manager.GameBalanceConfig config) {
        this.balanceConfig = config;
    }
    
    private int getTier1KillMobRewardAmount() {
        return balanceConfig != null ? balanceConfig.getMissionTier1KillMobRewardAmount() : 5;
    }
    
    private int getTier1GatherRewardAmount() {
        return balanceConfig != null ? balanceConfig.getMissionTier1GatherRewardAmount() : 3;
    }
    
    private int getTier2KillMobRewardAmount() {
        return balanceConfig != null ? balanceConfig.getMissionTier2KillMobRewardAmount() : 5;
    }
    
    private int getTier1KillMobTarget() {
        return balanceConfig != null ? balanceConfig.getMissionTier1KillMobTarget() : 10;
    }
    
    private int getTier1GatherTarget() {
        return balanceConfig != null ? balanceConfig.getMissionTier1GatherTarget() : 64;
    }
    
    private int getTier2KillMobTarget() {
        return balanceConfig != null ? balanceConfig.getMissionTier2KillMobTarget() : 20;
    }
    
    private int getTier2GatherTarget() {
        return balanceConfig != null ? balanceConfig.getMissionTier2GatherTarget() : 10;
    }

    public void interactWithTotem(Player p, Material totemMaterial) {
        if (activeMissions.containsKey(p.getUniqueId())) {
            Mission mission = activeMissions.get(p.getUniqueId());
            if (mission.isCompleted()) {
                // Ödül ver
                if (mission.getReward() != null) {
                p.getInventory().addItem(mission.getReward());
                }
                if (mission.getRewardMoney() > 0 && plugin != null && plugin.getEconomyManager() != null) {
                    plugin.getEconomyManager().depositPlayer(p, mission.getRewardMoney());
                }
                p.sendMessage("§a[LONCA] Görev Tamamlandı! Ödülünü aldın.");
                activeMissions.remove(p.getUniqueId());
            } else {
                // GUI menü aç
                openMissionMenu(p, mission);
            }
        } else {
            // Yeni görev üret
            Mission newMission = generateRandomMission(p);
            if (newMission != null) {
                activeMissions.put(p.getUniqueId(), newMission);
                openMissionMenu(p, newMission);
            } else {
                // Eski sistem (totem seviyesine göre)
            assignNewMission(p, totemMaterial);
            }
        }
    }
    
    /**
     * GUI menü aç
     */
    public void openMissionMenu(Player player, Mission mission) {
        if (plugin != null) {
            me.mami.stratocraft.gui.MissionMenu.openMenu(player, mission, this);
        } else {
            // Fallback: Chat mesajı
            player.sendMessage("§e[LONCA] §7Durum: " + mission.getProgress() + "/" + mission.getTargetAmount());
        }
    }

    private void assignNewMission(Player p, Material tier) {
        Mission mission;
        // Totem seviye sistemi
        if (tier == Material.COBBLESTONE || tier == Material.STONE) {
            // Taş Totem - Basit görevler (config'den)
            int tier1KillTarget = getTier1KillMobTarget();
            int tier1KillReward = getTier1KillMobRewardAmount();
            int tier1GatherTarget = getTier1GatherTarget();
            int tier1GatherReward = getTier1GatherRewardAmount();
            
            if (random.nextBoolean()) {
                mission = new Mission(p.getUniqueId(), Mission.Type.KILL_MOB, EntityType.ZOMBIE, tier1KillTarget, new ItemStack(Material.IRON_INGOT, tier1KillReward));
                p.sendMessage("§e[LONCA] §7Yeni Görev: " + tier1KillTarget + " Zombi Öldür.");
            } else {
                mission = new Mission(p.getUniqueId(), Mission.Type.GATHER_ITEM, Material.OAK_LOG, tier1GatherTarget, new ItemStack(Material.GOLD_INGOT, tier1GatherReward));
                p.sendMessage("§e[LONCA] §7Yeni Görev: " + tier1GatherTarget + " Odun Topla.");
            }
        } else if (tier == Material.DIAMOND_BLOCK || tier == Material.DIAMOND) {
            // Elmas Totem - Zor görevler (config'den)
            int tier2KillTarget = getTier2KillMobTarget();
            int tier2KillReward = getTier2KillMobRewardAmount();
            int tier2GatherTarget = getTier2GatherTarget();
            
            if (random.nextBoolean()) {
                ItemStack reward = random.nextBoolean() ? ItemManager.RECIPE_BOOK_TECTONIC : ItemManager.DEVIL_HORN;
                mission = new Mission(p.getUniqueId(), Mission.Type.KILL_MOB, EntityType.ENDERMAN, tier2KillTarget, reward);
                p.sendMessage("§6[LONCA] §cZorlu Görev: " + tier2KillTarget + " Enderman Avla.");
            } else {
                ItemStack reward = ItemManager.TITANIUM_INGOT != null ? ItemManager.TITANIUM_INGOT : new ItemStack(Material.DIAMOND, tier2KillReward);
                mission = new Mission(p.getUniqueId(), Mission.Type.GATHER_ITEM, Material.DEEPSLATE_DIAMOND_ORE, tier2GatherTarget, reward);
                p.sendMessage("§6[LONCA] §cZorlu Görev: " + tier2GatherTarget + " Derin Elmas Madeni Topla.");
            }
        } else {
            // Varsayılan (config'den)
            int tier1KillTarget = getTier1KillMobTarget();
            int tier1KillReward = getTier1KillMobRewardAmount();
            mission = new Mission(p.getUniqueId(), Mission.Type.KILL_MOB, EntityType.ZOMBIE, tier1KillTarget, new ItemStack(Material.IRON_INGOT, tier1KillReward));
            p.sendMessage("§e[LONCA] §7Yeni Görev: 10 Zombi Öldür.");
        }
        activeMissions.put(p.getUniqueId(), mission);
    }
    
    public void handleKill(Player p, EntityType type) {
        if (activeMissions.containsKey(p.getUniqueId())) {
            Mission m = activeMissions.get(p.getUniqueId());
            if (m.getType() == Mission.Type.KILL_MOB && m.getTargetEntity() == type) {
                m.addProgress(1);
                if (m.isCompleted()) {
                    p.sendMessage("§aGörev hedefine ulaşıldı! Toteme dön.");
                    openMissionMenu(p, m);
                }
            }
        }
    }
    
    public void handleGather(Player p, Material material) {
        if (activeMissions.containsKey(p.getUniqueId())) {
            Mission m = activeMissions.get(p.getUniqueId());
            if (m.getType() == Mission.Type.GATHER_ITEM && m.getTargetMaterial() == material) {
                m.addProgress(1);
                if (m.isCompleted()) {
                    p.sendMessage("§aGörev hedefine ulaşıldı! Toteme dön.");
                    openMissionMenu(p, m);
                }
            }
        }
    }
    
    // ========== YENİ SİSTEM: RASTGELE GÖREV ÜRETİMİ ==========
    
    /**
     * Seviyeye göre rastgele görev üret
     */
    public Mission generateRandomMission(Player player) {
        if (difficultyManager == null) return null; // DifficultyManager yoksa eski sistemi kullan
        
        // Oyuncunun konumuna göre zorluk seviyesi belirle
        Location playerLoc = player.getLocation();
        int difficultyLevel = difficultyManager.getDifficultyLevel(playerLoc);
        
        // Seviyeye göre zorluk belirle
        Mission.Difficulty difficulty = getDifficultyByLevel(difficultyLevel);
        
        // Rastgele görev tipi seç (YENİ: MissionType enum kullanır)
        MissionType[] availableTypes = getAvailableTypes(difficulty);
        MissionType selectedType = availableTypes[random.nextInt(availableTypes.length)];
        
        // Görev oluştur
        return createMissionByType(player, selectedType, difficulty);
    }
    
    /**
     * Seviyeye göre zorluk belirle
     */
    private Mission.Difficulty getDifficultyByLevel(int level) {
        if (level <= 1) return Mission.Difficulty.EASY;
        if (level <= 3) return Mission.Difficulty.MEDIUM;
        if (level <= 5) return Mission.Difficulty.HARD;
        return Mission.Difficulty.EXPERT;
    }
    
    /**
     * Zorluğa göre mevcut görev tipleri (YENİ: MissionType enum kullanır)
     */
    private MissionType[] getAvailableTypes(Mission.Difficulty difficulty) {
        switch (difficulty) {
            case EASY:
                return new MissionType[]{
                    MissionType.KILL_MOBS,
                    MissionType.COLLECT_ITEMS,
                    MissionType.COLLECT_ITEMS // MINE_BLOCK yerine
                };
            case MEDIUM:
                return new MissionType[]{
                    MissionType.KILL_MOBS,
                    MissionType.COLLECT_ITEMS,
                    MissionType.EXPLORE_AREA,
                    MissionType.CRAFT_ITEMS
                };
            case HARD:
                return new MissionType[]{
                    MissionType.KILL_MOBS,
                    MissionType.EXPLORE_AREA,
                    MissionType.BUILD_STRUCTURE,
                    MissionType.DEFEND_CLAN // KILL_PLAYER yerine
                };
            case EXPERT:
                return new MissionType[]{
                    MissionType.BUILD_STRUCTURE,
                    MissionType.DEFEND_CLAN,
                    MissionType.EXPLORE_AREA // TRAVEL_DISTANCE yerine
                };
        }
        return new MissionType[]{MissionType.KILL_MOBS};
    }
    
    /**
     * Tip'e göre görev oluştur (YENİ: MissionType ve MissionScope enum kullanır)
     */
    private Mission createMissionByType(Player player, MissionType missionType, Mission.Difficulty difficulty) {
        int targetAmount = getTargetAmountByDifficulty(difficulty, missionType);
        ItemStack reward = getRewardByDifficulty(difficulty);
        double rewardMoney = getRewardMoneyByDifficulty(difficulty);
        long deadlineDays = getDeadlineByDifficulty(difficulty);
        
        // Scope belirle (tip'e göre)
        me.mami.stratocraft.enums.MissionScope scope = determineScopeFromType(missionType);
        
        Mission mission = new Mission(player.getUniqueId(), missionType, scope, difficulty, 
                                     targetAmount, reward, rewardMoney, deadlineDays);
        
        // Tip'e göre hedef belirle
        switch (missionType) {
            case KILL_MOBS:
                mission.setTargetEntity(getRandomMobByDifficulty(difficulty));
                break;
            case COLLECT_ITEMS:
                mission.setTargetMaterial(getRandomMaterialByDifficulty(difficulty));
                break;
            case EXPLORE_AREA:
                mission.setTargetLocation(generateRandomLocation(player.getLocation(), difficulty));
                break;
            case BUILD_STRUCTURE:
                mission.setStructureType(getRandomStructureByDifficulty(difficulty));
                break;
            case DEFEND_CLAN:
                // Rastgele bir online oyuncu seç (kendisi hariç)
                mission.setTargetPlayer(getRandomOnlinePlayer(player));
                break;
            case EXPLORE_AREA:
                mission.setTargetDistance(getTargetDistanceByDifficulty(difficulty));
                break;
            case CRAFT_ITEMS:
                mission.setTargetMaterial(getRandomCraftableMaterialByDifficulty(difficulty));
                break;
            default:
                // Diğer tipler için varsayılan
                break;
        }
        
        return mission;
    }
    
    /**
     * MissionType'dan scope belirle
     */
    private me.mami.stratocraft.enums.MissionScope determineScopeFromType(MissionType type) {
        switch (type) {
            case BUILD_STRUCTURE:
            case DEFEND_CLAN:
            case COMPLETE_RITUAL:
            case CLAN_TERRITORY:
            case CLAN_WAR:
            case CLAN_RESOURCE:
                return me.mami.stratocraft.enums.MissionScope.CLAN;
            default:
                return me.mami.stratocraft.enums.MissionScope.PERSONAL;
        }
    }
    
    /**
     * Zorluğa göre hedef miktar (YENİ: MissionType enum kullanır)
     */
    private int getTargetAmountByDifficulty(Mission.Difficulty difficulty, MissionType type) {
        int base;
        switch (type) {
            case KILL_MOBS:
            case COLLECT_ITEMS:
                base = 10;
                break;
            case EXPLORE_AREA:
                base = 1;
                break;
            case BUILD_STRUCTURE:
                base = 1;
                break;
            case DEFEND_CLAN:
                base = 1;
                break;
            case CRAFT_ITEMS:
                base = 5;
                break;
            default:
                base = 10;
                break;
        }
        
        switch (difficulty) {
            case EASY: return base;
            case MEDIUM: return base * 2;
            case HARD: return base * 3;
            case EXPERT: return base * 5;
            default: return base;
        }
    }
    
    /**
     * Zorluğa göre hedef miktar (GERİYE UYUMLULUK: eski Mission.Type enum kullanır)
     * @deprecated MissionType kullanın
     */
    @Deprecated
    private int getTargetAmountByDifficulty(Mission.Difficulty difficulty, Mission.Type type) {
        // Eski enum'u yeni enum'a map et
        MissionType missionType = null;
        try {
            missionType = MissionType.valueOf(type.name());
        } catch (IllegalArgumentException e) {
            switch (type) {
                case KILL_MOB: missionType = MissionType.KILL_MOBS; break;
                case GATHER_ITEM: missionType = MissionType.COLLECT_ITEMS; break;
                case VISIT_LOCATION: missionType = MissionType.EXPLORE_AREA; break;
                case BUILD_STRUCTURE: missionType = MissionType.BUILD_STRUCTURE; break;
                case KILL_PLAYER: missionType = MissionType.DEFEND_CLAN; break;
                case CRAFT_ITEM: missionType = MissionType.CRAFT_ITEMS; break;
                case MINE_BLOCK: missionType = MissionType.COLLECT_ITEMS; break;
                case TRAVEL_DISTANCE: missionType = MissionType.EXPLORE_AREA; break;
                default: missionType = MissionType.KILL_MOBS; break;
            }
        }
        return getTargetAmountByDifficulty(difficulty, missionType);
    }
    
    /**
     * Rastgele lokasyon üret (oyuncunun konumuna göre)
     */
    private Location generateRandomLocation(Location playerLoc, Mission.Difficulty difficulty) {
        int radius;
        switch (difficulty) {
            case EASY:
                radius = 500;      // 500 blok
                break;
            case MEDIUM:
                radius = 1000;   // 1000 blok
                break;
            case HARD:
                radius = 2000;      // 2000 blok
                break;
            case EXPERT:
                radius = 5000;    // 5000 blok
                break;
            default:
                radius = 1000;
        }
        
        int x = playerLoc.getBlockX() + random.nextInt(radius * 2) - radius;
        int z = playerLoc.getBlockZ() + random.nextInt(radius * 2) - radius;
        int y = playerLoc.getWorld().getHighestBlockYAt(x, z);
        
        return new Location(playerLoc.getWorld(), x, y, z);
    }
    
    /**
     * Zorluğa göre rastgele mob seç
     */
    private EntityType getRandomMobByDifficulty(Mission.Difficulty difficulty) {
        EntityType[] mobs;
        switch (difficulty) {
            case EASY:
                mobs = new EntityType[]{EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER, EntityType.CREEPER};
                break;
            case MEDIUM:
                mobs = new EntityType[]{EntityType.ENDERMAN, EntityType.WITCH, EntityType.BLAZE, EntityType.MAGMA_CUBE};
                break;
            case HARD:
                mobs = new EntityType[]{EntityType.WITHER_SKELETON, EntityType.GHAST, EntityType.ENDER_DRAGON, EntityType.WITHER};
                break;
            case EXPERT:
                mobs = new EntityType[]{EntityType.ENDER_DRAGON, EntityType.WITHER, EntityType.ELDER_GUARDIAN};
                break;
            default:
                mobs = new EntityType[]{EntityType.ZOMBIE};
        }
        return mobs[random.nextInt(mobs.length)];
    }
    
    /**
     * Zorluğa göre rastgele malzeme seç
     */
    private Material getRandomMaterialByDifficulty(Mission.Difficulty difficulty) {
        Material[] materials;
        switch (difficulty) {
            case EASY:
                materials = new Material[]{Material.OAK_LOG, Material.STONE, Material.COAL_ORE, Material.IRON_ORE};
                break;
            case MEDIUM:
                materials = new Material[]{Material.GOLD_ORE, Material.DIAMOND_ORE, Material.EMERALD_ORE, Material.REDSTONE_ORE};
                break;
            case HARD:
                materials = new Material[]{Material.ANCIENT_DEBRIS, Material.NETHERITE_SCRAP, Material.END_STONE};
                break;
            case EXPERT:
                materials = new Material[]{Material.ANCIENT_DEBRIS, Material.NETHERITE_SCRAP};
                break;
            default:
                materials = new Material[]{Material.STONE};
        }
        return materials[random.nextInt(materials.length)];
    }
    
    /**
     * Zorluğa göre rastgele craft edilebilir item seç
     */
    private Material getRandomCraftableMaterialByDifficulty(Mission.Difficulty difficulty) {
        Material[] materials;
        switch (difficulty) {
            case EASY:
                materials = new Material[]{Material.WOODEN_SWORD, Material.STONE_PICKAXE, Material.IRON_INGOT, Material.BREAD};
                break;
            case MEDIUM:
                materials = new Material[]{Material.DIAMOND_SWORD, Material.DIAMOND_PICKAXE, Material.ENCHANTED_BOOK, Material.GOLDEN_APPLE};
                break;
            case HARD:
                materials = new Material[]{Material.NETHERITE_SWORD, Material.NETHERITE_PICKAXE, Material.BEACON};
                break;
            case EXPERT:
                materials = new Material[]{Material.NETHERITE_SWORD, Material.BEACON};
                break;
            default:
                materials = new Material[]{Material.IRON_INGOT};
        }
        return materials[random.nextInt(materials.length)];
    }
    
    /**
     * Zorluğa göre rastgele kazılabilir blok seç
     */
    private Material getRandomMineableMaterialByDifficulty(Mission.Difficulty difficulty) {
        Material[] materials;
        switch (difficulty) {
            case EASY:
                materials = new Material[]{Material.COBBLESTONE, Material.DIRT, Material.SAND, Material.GRAVEL};
                break;
            case MEDIUM:
                materials = new Material[]{Material.DEEPSLATE, Material.BASALT, Material.BLACKSTONE};
                break;
            case HARD:
                materials = new Material[]{Material.OBSIDIAN, Material.ANCIENT_DEBRIS, Material.END_STONE};
                break;
            case EXPERT:
                materials = new Material[]{Material.ANCIENT_DEBRIS, Material.OBSIDIAN};
                break;
            default:
                materials = new Material[]{Material.STONE};
        }
        return materials[random.nextInt(materials.length)];
    }
    
    /**
     * Zorluğa göre rastgele yapı seç
     */
    private String getRandomStructureByDifficulty(Mission.Difficulty difficulty) {
        String[] structures;
        switch (difficulty) {
            case EASY:
                structures = new String[]{"CORE", "ALCHEMY_TOWER"};
                break;
            case MEDIUM:
                structures = new String[]{"BATTERY", "TURRET", "FORCE_FIELD"};
                break;
            case HARD:
                structures = new String[]{"TELEPORTER", "MAGNETIC_RAIL", "SIEGE_WEAPON"};
                break;
            case EXPERT:
                structures = new String[]{"TELEPORTER", "SIEGE_WEAPON"};
                break;
            default:
                structures = new String[]{"CORE"};
        }
        return structures[random.nextInt(structures.length)];
    }
    
    /**
     * Rastgele online oyuncu seç (kendisi hariç)
     */
    private UUID getRandomOnlinePlayer(Player player) {
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        onlinePlayers.remove(player);
        if (onlinePlayers.isEmpty()) {
            return null; // Online oyuncu yok
        }
        return onlinePlayers.get(random.nextInt(onlinePlayers.size())).getUniqueId();
    }
    
    /**
     * Zorluğa göre hedef mesafe
     */
    private int getTargetDistanceByDifficulty(Mission.Difficulty difficulty) {
        switch (difficulty) {
            case EASY:
                return 1000; // 1000 blok
            case MEDIUM:
                return 2000; // 2000 blok
            case HARD:
                return 5000; // 5000 blok
            case EXPERT:
                return 10000; // 10000 blok
        }
        return 1000;
    }
    
    /**
     * Zorluğa göre ödül
     */
    private ItemStack getRewardByDifficulty(Mission.Difficulty difficulty) {
        switch (difficulty) {
            case EASY:
                return new ItemStack(Material.IRON_INGOT, 5);
            case MEDIUM:
                return new ItemStack(Material.GOLD_INGOT, 10);
            case HARD:
                return new ItemStack(Material.DIAMOND, 5);
            case EXPERT:
                // Özel item'lar (ItemManager'dan)
                if (ItemManager.TITANIUM_INGOT != null) {
                    return ItemManager.TITANIUM_INGOT.clone();
                }
                return new ItemStack(Material.NETHERITE_INGOT, 1);
        }
        return new ItemStack(Material.IRON_INGOT, 5);
    }
    
    /**
     * Zorluğa göre para ödülü
     */
    private double getRewardMoneyByDifficulty(Mission.Difficulty difficulty) {
        switch (difficulty) {
            case EASY:
                return 50.0;
            case MEDIUM:
                return 150.0;
            case HARD:
                return 500.0;
            case EXPERT:
                return 1500.0;
        }
        return 50.0;
    }
    
    /**
     * Zorluğa göre süre (gün)
     */
    private long getDeadlineByDifficulty(Mission.Difficulty difficulty) {
        switch (difficulty) {
            case EASY:
                return 1; // 1 gün
            case MEDIUM:
                return 2; // 2 gün
            case HARD:
                return 3; // 3 gün
            case EXPERT:
                return 5; // 5 gün
        }
        return 1;
    }
    
    // ========== İLERLEME TAKİBİ METODLARI ==========
    
    /**
     * Lokasyon ziyareti takibi
     */
    public void handleLocationVisit(Player player, Location location) {
        if (!activeMissions.containsKey(player.getUniqueId())) return;
        Mission mission = activeMissions.get(player.getUniqueId());
        
        if (mission.getType() == Mission.Type.VISIT_LOCATION && mission.getTargetLocation() != null) {
            double distance = location.distance(mission.getTargetLocation());
            if (distance <= 10.0) { // 10 blok yakınsa ziyaret sayılır
                mission.addProgress(1);
                if (mission.isCompleted()) {
                    player.sendMessage("§aGörev hedefine ulaşıldı! Toteme dön.");
                    openMissionMenu(player, mission);
                }
            }
        }
    }
    
    /**
     * Yapı inşa takibi
     */
    public void handleStructureBuild(Player player, Location location) {
        if (!activeMissions.containsKey(player.getUniqueId())) return;
        Mission mission = activeMissions.get(player.getUniqueId());
        
        if (mission.getType() == Mission.Type.BUILD_STRUCTURE && mission.getStructureType() != null) {
            // StructureActivationListener'dan gelen yapı tipini kontrol et
            // Bu kısım StructureManager ile entegre edilebilir
            mission.addProgress(1);
            if (mission.isCompleted()) {
                player.sendMessage("§aGörev hedefine ulaşıldı! Toteme dön.");
                openMissionMenu(player, mission);
            }
        }
    }
    
    /**
     * Oyuncu öldürme takibi
     */
    public void handlePlayerKill(Player killer, Player victim) {
        if (!activeMissions.containsKey(killer.getUniqueId())) return;
        Mission mission = activeMissions.get(killer.getUniqueId());
        
        if (mission.getType() == Mission.Type.KILL_PLAYER && mission.getTargetPlayer() != null) {
            if (mission.getTargetPlayer().equals(victim.getUniqueId())) {
                mission.addProgress(1);
                if (mission.isCompleted()) {
                    killer.sendMessage("§aGörev hedefine ulaşıldı! Toteme dön.");
                    openMissionMenu(killer, mission);
                }
            }
        }
    }
    
    /**
     * Item craft takibi
     */
    public void handleCraft(Player player, Material crafted) {
        if (!activeMissions.containsKey(player.getUniqueId())) return;
        Mission mission = activeMissions.get(player.getUniqueId());
        
        if (mission.getType() == Mission.Type.CRAFT_ITEM && mission.getTargetMaterial() == crafted) {
            mission.addProgress(1);
            if (mission.isCompleted()) {
                player.sendMessage("§aGörev hedefine ulaşıldı! Toteme dön.");
                openMissionMenu(player, mission);
            }
        }
    }
    
    /**
     * Mesafe kat etme takibi
     */
    public void handleTravel(Player player, double distance) {
        if (!activeMissions.containsKey(player.getUniqueId())) return;
        Mission mission = activeMissions.get(player.getUniqueId());
        
        if (mission.getType() == Mission.Type.TRAVEL_DISTANCE) {
            mission.addTravelProgress(distance);
            if (mission.isCompleted()) {
                player.sendMessage("§aGörev hedefine ulaşıldı! Toteme dön.");
                openMissionMenu(player, mission);
            }
        }
    }
    
    /**
     * Blok kazma takibi
     */
    public void handleMine(Player player, Material mined) {
        if (!activeMissions.containsKey(player.getUniqueId())) return;
        Mission mission = activeMissions.get(player.getUniqueId());
        
        if (mission.getType() == Mission.Type.MINE_BLOCK && mission.getTargetMaterial() == mined) {
            mission.addProgress(1);
            if (mission.isCompleted()) {
                player.sendMessage("§aGörev hedefine ulaşıldı! Toteme dön.");
                openMissionMenu(player, mission);
            }
        }
    }
    
    /**
     * Aktif görev al
     */
    public Mission getActiveMission(UUID playerId) {
        return activeMissions.get(playerId);
    }
    
    /**
     * Görevi kaldır
     */
    public void removeMission(UUID playerId) {
        activeMissions.remove(playerId);
    }
}

