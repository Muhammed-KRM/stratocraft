package me.mami.stratocraft;

import me.mami.stratocraft.listener.*;
import me.mami.stratocraft.listener.NewBatteryListener;
import me.mami.stratocraft.manager.*;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Territory;
import me.mami.stratocraft.task.BuffTask;
import me.mami.stratocraft.task.DisasterTask;
import me.mami.stratocraft.task.MobRideTask;
import me.mami.stratocraft.task.DrillTask;
import me.mami.stratocraft.task.CropTask;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public class Main extends JavaPlugin {
    private static Main instance;

    // Yöneticiler
    private ClanManager clanManager;
    private TerritoryManager territoryManager;
    private BatteryManager batteryManager;
    private NewBatteryManager newBatteryManager;
    private ItemManager itemManager;
    private DisasterManager disasterManager;
    private SiegeManager siegeManager;
    @SuppressWarnings("unused")
    private CaravanManager caravanManager;
    private ScavengerManager scavengerManager;
    private LogisticsManager logisticsManager;
    private ContractManager contractManager;
    private AllianceManager allianceManager;
    private MobManager mobManager;
    private ShopManager shopManager;
    private ResearchManager researchManager;
    private MissionManager missionManager;
    private me.mami.stratocraft.manager.GhostRecipeManager ghostRecipeManager;
    private me.mami.stratocraft.manager.TrainingManager trainingManager;
    private BuffManager buffManager;
    
    // ✅ YENİ: Oyuncu özellik kontrol sistemi
    private me.mami.stratocraft.task.PlayerFeatureMonitor playerFeatureMonitor;
    private DataManager dataManager;
    private VirtualStorageListener virtualStorageListener;
    private ConfigManager configManager;
    private me.mami.stratocraft.util.LangManager langManager;
    private me.mami.stratocraft.gui.ClanMenu clanMenu;
    private me.mami.stratocraft.gui.ClanMissionMenu clanMissionMenu;
    private me.mami.stratocraft.gui.ClanMemberMenu clanMemberMenu;
    private me.mami.stratocraft.gui.ClanStatsMenu clanStatsMenu;
    private me.mami.stratocraft.gui.ContractMenu contractMenu;
    private me.mami.stratocraft.manager.ContractRequestManager contractRequestManager;
    private me.mami.stratocraft.manager.ContractTermsManager contractTermsManager;
    private me.mami.stratocraft.gui.PowerMenu powerMenu;
    private me.mami.stratocraft.gui.ClanBankMenu clanBankMenu;
    private me.mami.stratocraft.gui.ClanStructureMenu clanStructureMenu;
    private me.mami.stratocraft.gui.AllianceMenu allianceMenu;
    private me.mami.stratocraft.gui.PeaceRequestMenu peaceRequestMenu;
    private me.mami.stratocraft.manager.PeaceRequestManager peaceRequestManager;
    private me.mami.stratocraft.gui.CaravanMenu caravanMenu;
    private me.mami.stratocraft.gui.TamingMenu tamingMenu;
    private me.mami.stratocraft.gui.BreedingMenu breedingMenu;
    private me.mami.stratocraft.gui.TrainingMenu trainingMenu;
    private me.mami.stratocraft.listener.PersonalTerminalListener personalTerminalListener;
    private me.mami.stratocraft.manager.CombatLogManager combatLogManager;
    private me.mami.stratocraft.manager.EconomyManager economyManager;
    private me.mami.stratocraft.manager.SiegeWeaponManager siegeWeaponManager;
    private me.mami.stratocraft.manager.SupplyDropManager supplyDropManager;
    private me.mami.stratocraft.manager.TrapManager trapManager;
    // Eski MineManager kaldırıldı, artık sadece NewMineManager kullanılıyor
    private me.mami.stratocraft.manager.NewMineManager newMineManager;
    private me.mami.stratocraft.manager.SpecialItemManager specialItemManager;
    private me.mami.stratocraft.manager.DifficultyManager difficultyManager;
    private me.mami.stratocraft.manager.DungeonManager dungeonManager;
    private me.mami.stratocraft.manager.BiomeManager biomeManager;
    private me.mami.stratocraft.manager.BossManager bossManager;
    private me.mami.stratocraft.manager.BossArenaManager bossArenaManager;
    private me.mami.stratocraft.manager.TamingManager tamingManager;
    
    // Yeni Boss Arena Sistemi (BossManager ile birlikte çalışır)
    private me.mami.stratocraft.manager.NewBossArenaManager newBossArenaManager;
    private me.mami.stratocraft.manager.BreedingManager breedingManager;
    private me.mami.stratocraft.listener.SpecialWeaponListener specialWeaponListener;
    private me.mami.stratocraft.manager.StratocraftPowerSystem stratocraftPowerSystem;
    private me.mami.stratocraft.listener.PowerSystemListener powerSystemListener;
    
    // Güç sistemi yardımcıları (test için)
    private me.mami.stratocraft.manager.SimpleRankingSystem simpleRankingSystem;
    private me.mami.stratocraft.manager.SimplePowerHistory simplePowerHistory;
    private me.mami.stratocraft.manager.HUDManager hudManager;
    private me.mami.stratocraft.manager.BatteryParticleManager batteryParticleManager;
    private me.mami.stratocraft.manager.DisasterArenaManager disasterArenaManager;
    private me.mami.stratocraft.manager.TaskManager taskManager;
    private me.mami.stratocraft.listener.TerritoryListener territoryListener;
    
    // Yeni Yapı Sistemi Manager'ları
    private me.mami.stratocraft.manager.StructureCoreManager structureCoreManager;
    private me.mami.stratocraft.manager.StructureRecipeManager structureRecipeManager;
    private me.mami.stratocraft.manager.StructureActivationItemManager structureActivationItemManager;
    
    // Yeni Model Sistemi Manager'ları
    private me.mami.stratocraft.manager.PlayerDataManager playerDataManager;
    
    // Yapı Efekt Yönetimi
    private me.mami.stratocraft.manager.StructureEffectManager structureEffectManager;
    
    // Merkezi Tarif Yönetim Sistemi
    private me.mami.stratocraft.manager.RecipeManager recipeManager;
    
    // Territory sistemi
    private me.mami.stratocraft.manager.TerritoryBoundaryManager territoryBoundaryManager;
    private me.mami.stratocraft.manager.config.TerritoryConfig territoryConfig;
    private me.mami.stratocraft.gui.ClanTerritoryMenu clanTerritoryMenu;
    private me.mami.stratocraft.task.TerritoryBoundaryParticleTask boundaryParticleTask; // ✅ YENİ: Partikül task'ı
    
    public me.mami.stratocraft.listener.SpecialWeaponListener getSpecialWeaponListener() {
        return specialWeaponListener;
    }

    @Override
    public void onEnable() {
        instance = this;
        
        // CustomBlockData'yı başlat
        me.mami.stratocraft.util.CustomBlockData.initialize(this);

        // Klasör Hazırlığı - WorldEdit'e bağımlı olmadan klasörleri oluştur
        try {
            File schemDir = new File(getDataFolder(), "schematics");
            if (!schemDir.exists()) schemDir.mkdirs();
            
            // Zindan klasörleri
            File dungeonsDir = new File(schemDir, "dungeons");
            if (!dungeonsDir.exists()) dungeonsDir.mkdirs();
            for (int level = 1; level <= 5; level++) {
                File levelDir = new File(dungeonsDir, "level" + level);
                if (!levelDir.exists()) levelDir.mkdirs();
            }
            
            // Biyom klasörleri
            File biomesDir = new File(schemDir, "biomes");
            if (!biomesDir.exists()) biomesDir.mkdirs();
            File biomesStructuresDir = new File(biomesDir, "structures");
            if (!biomesStructuresDir.exists()) biomesStructuresDir.mkdirs();
            File biomesCustomDir = new File(biomesDir, "custom");
            if (!biomesCustomDir.exists()) biomesCustomDir.mkdirs();
            
            getLogger().info("Şema klasörleri hazırlandı.");
        } catch (Exception e) {
            getLogger().warning("§cSchematic klasörleri oluşturulurken hata: " + e.getMessage());
        }
        
        // WorldEdit kontrolü - varsa bilgilendir, yoksa uyar
        boolean worldEditAvailable = isWorldEditAvailable();
        if (worldEditAvailable) {
            getLogger().info("§aWorldEdit bulundu - Schematic özellikleri aktif.");
        } else {
            getLogger().warning("§eWorldEdit bulunamadı - Schematic özellikleri devre dışı.");
        }

        // 1. Yöneticileri Başlat
        // Önce SpecialItemManager'ı oluştur (ItemManager buna ihtiyaç duyuyor)
        specialItemManager = new me.mami.stratocraft.manager.SpecialItemManager();
        
        // Sonra ItemManager'ı başlat ve init() çağır (kanca item'larını oluşturur)
        itemManager = new ItemManager();
        itemManager.init(); // Artık SpecialItemManager'a erişebilir
        
        // Şimdi recipe'leri kaydet (ItemManager.RUSTY_HOOK vb. artık null değil)
        specialItemManager.registerRecipes();
        clanManager = new ClanManager(this);
        territoryManager = new TerritoryManager(clanManager);
        clanManager.setTerritoryManager(territoryManager); // Cache güncellemesi için
        // ✅ YENİ: TerritoryBoundaryManager TerritoryManager'a set edilecek (aşağıda)
        batteryManager = new BatteryManager(this);
        newBatteryManager = new NewBatteryManager(this); // Yeni batarya sistemi
        siegeManager = new SiegeManager();
        
        // ✅ YENİ: PeaceRequestManager başlat
        peaceRequestManager = new me.mami.stratocraft.manager.PeaceRequestManager(clanManager, siegeManager);
        
        disasterManager = new DisasterManager(this);
        batteryManager.setDisasterManager(disasterManager);
        batteryManager.setTerritoryManager(territoryManager);
        batteryManager.setSiegeManager(siegeManager);
        caravanManager = new CaravanManager();
        scavengerManager = new ScavengerManager();
        logisticsManager = new LogisticsManager(territoryManager);
        contractManager = new ContractManager(clanManager);
        contractRequestManager = new me.mami.stratocraft.manager.ContractRequestManager(this);
        contractTermsManager = new me.mami.stratocraft.manager.ContractTermsManager(this);
        allianceManager = new AllianceManager(clanManager);
        mobManager = new MobManager();
        shopManager = new ShopManager();
        researchManager = new ResearchManager();
        // MissionManager DifficultyManager'dan sonra başlatılmalı
        // missionManager = new MissionManager(); // Aşağıda difficultyManager'dan sonra başlatılacak
        ghostRecipeManager = new me.mami.stratocraft.manager.GhostRecipeManager();
        // 75 batarya için hayalet tariflerini ekle (ghostRecipeManager oluşturulduktan sonra)
        ghostRecipeManager.initializeBatteryRecipes(newBatteryManager);
        trainingManager = new me.mami.stratocraft.manager.TrainingManager();
        buffManager = new BuffManager();
        buffManager.setPlugin(this);
        hudManager = new me.mami.stratocraft.manager.HUDManager(this);
        // TaskManager (Memory leak önleme için - diğer manager'lardan önce)
        taskManager = new me.mami.stratocraft.manager.TaskManager(this);
        
        // Yeni Yapı Sistemi Manager'ları
        structureCoreManager = new me.mami.stratocraft.manager.StructureCoreManager(this);
        structureActivationItemManager = new me.mami.stratocraft.manager.StructureActivationItemManager();
        structureRecipeManager = new me.mami.stratocraft.manager.StructureRecipeManager(this);
        
        // Yeni Model Sistemi Manager'ları
        playerDataManager = new me.mami.stratocraft.manager.PlayerDataManager(this);
        
        // YENİ MODEL: ClanManager'a PlayerDataManager'ı set et
        clanManager.setPlayerDataManager(playerDataManager);
        
        batteryParticleManager = new me.mami.stratocraft.manager.BatteryParticleManager(this);
        dataManager = new DataManager(this);
        configManager = new ConfigManager(this);
        langManager = new me.mami.stratocraft.util.LangManager(this);
        clanMenu = new me.mami.stratocraft.gui.ClanMenu(clanManager);
        economyManager = new me.mami.stratocraft.manager.EconomyManager();
        difficultyManager = new me.mami.stratocraft.manager.DifficultyManager(this);
        dungeonManager = new me.mami.stratocraft.manager.DungeonManager(this);
        biomeManager = new me.mami.stratocraft.manager.BiomeManager(this);
        bossArenaManager = new me.mami.stratocraft.manager.BossArenaManager(this); // Eski, devre dışı arena (gerekirse)
        bossManager = new me.mami.stratocraft.manager.BossManager(this);           // Yeni, birleşik Boss sistemi
        tamingManager = new me.mami.stratocraft.manager.TamingManager(this);
        
        // Yeni Boss Arena Sistemi - Sadece çevre dönüşümü için
        newBossArenaManager = new me.mami.stratocraft.manager.NewBossArenaManager(this);
        breedingManager = new me.mami.stratocraft.manager.BreedingManager(this);
        
        // DisasterArenaManager initialize et
        disasterArenaManager = new me.mami.stratocraft.manager.DisasterArenaManager(this);
        
        // MissionManager'ı DifficultyManager ile başlat
        missionManager = new me.mami.stratocraft.manager.MissionManager(difficultyManager, this);

        // Yeni sistemler: Tuzaklar, Mayınlar, Hava Drop (174. satırdan önce initialize edilmeli)
        trapManager = new me.mami.stratocraft.manager.TrapManager(this);
        newMineManager = new me.mami.stratocraft.manager.NewMineManager(this);
        supplyDropManager = new me.mami.stratocraft.manager.SupplyDropManager(this);

        // Manager bağlantıları
        siegeManager.setBuffManager(buffManager);
        disasterManager.setBuffManager(buffManager);
        disasterManager.setTerritoryManager(territoryManager);
        disasterManager.setDifficultyManager(difficultyManager); // DifficultyManager'ı set et
        disasterManager.setConfigManager(configManager.getDisasterConfigManager()); // ConfigManager'ı set et
        disasterManager.setArenaManager(disasterArenaManager); // DisasterArenaManager'ı set et
        
        // BossManager'a DifficultyManager'ı set et (zorluk seviyesine göre boss gücü)
        if (bossManager != null && difficultyManager != null) {
            bossManager.setDifficultyManager(difficultyManager);
        }
        
        // ✅ CONFIG ENTEGRASYONU: Manager'lara GameBalanceConfig'i set et
        if (configManager != null && configManager.getGameBalanceConfig() != null) {
            me.mami.stratocraft.manager.GameBalanceConfig balanceConfig = configManager.getGameBalanceConfig();
            siegeManager.setBalanceConfig(balanceConfig);
            buffManager.setBalanceConfig(balanceConfig);
            contractManager.setBalanceConfig(balanceConfig);
            shopManager.setBalanceConfig(balanceConfig);
            missionManager.setBalanceConfig(balanceConfig);
                    // Faz 2: Savaş sistemleri
                    bossManager.setBalanceConfig(balanceConfig);
                    trapManager.setBalanceConfig(balanceConfig);
                    newMineManager.setBalanceConfig(balanceConfig);
                    mobManager.setBalanceConfig(balanceConfig);
                    // Faz 3: Ekonomi ve üreme sistemleri
                    supplyDropManager.setBalanceConfig(balanceConfig);
                    caravanManager.setBalanceConfig(balanceConfig);
                    tamingManager.setBalanceConfig(balanceConfig);
                    breedingManager.setBalanceConfig(balanceConfig);
                    // Faz 4: Diğer sistemler
                    specialItemManager.setBalanceConfig(balanceConfig);
                }
        
        // Dinamik Zorluk Sistemi Başlatma (tüm manager'lar hazır olduktan sonra)
        initializeDynamicDifficultySystem();
        
        // Klan Güç Sistemi Başlatma
        initializeClanPowerSystem();
        
        // Yeni Klan Sistemleri Başlatma
        initializeClanSystems();

        // 2. Dinleyicileri Kaydet
        // Eski batarya sistemi kaldırıldı, yeni sistem kullanılıyor
        // BatteryListener batteryListener = new BatteryListener(batteryManager, territoryManager, researchManager);
        // batteryListener.setTrainingManager(trainingManager);
        // Bukkit.getPluginManager().registerEvents(batteryListener, this);
        
        // Yeni batarya sistemi
        NewBatteryListener newBatteryListener = new NewBatteryListener(newBatteryManager, territoryManager);
        newBatteryListener.setTrainingManager(trainingManager);
        Bukkit.getPluginManager().registerEvents(newBatteryListener, this);
        CombatListener combatListener = new CombatListener(clanManager);
        combatListener.setAllianceManager(allianceManager);
        Bukkit.getPluginManager().registerEvents(combatListener, this);
        Bukkit.getPluginManager().registerEvents(new SurvivalListener(missionManager), this);
        // YENİ: TerritoryBoundaryManager ve TerritoryConfig
        me.mami.stratocraft.manager.config.TerritoryConfig territoryConfig = 
            configManager != null ? configManager.getTerritoryConfig() : null;
        me.mami.stratocraft.manager.TerritoryBoundaryManager territoryBoundaryManager = null;
        if (territoryConfig != null) {
            territoryBoundaryManager = new me.mami.stratocraft.manager.TerritoryBoundaryManager(
                this, territoryManager, territoryConfig);
            // ✅ YENİ: TerritoryManager'a TerritoryBoundaryManager'ı set et (Y ekseni kontrolü için)
            territoryManager.setBoundaryManager(territoryBoundaryManager);
        }
        
        // ✅ YENİ: TerritoryListener güncelle (field'a atanıyor)
        territoryListener = new TerritoryListener(territoryManager, siegeManager);
        if (territoryBoundaryManager != null) {
            territoryListener.setBoundaryManager(territoryBoundaryManager);
        }
        if (territoryConfig != null) {
            territoryListener.setTerritoryConfig(territoryConfig);
        }
        Bukkit.getPluginManager().registerEvents(territoryListener, this);
        
        // ✅ YENİ: TerritoryBoundaryParticleTask başlat
        if (territoryConfig != null && territoryConfig.isBoundaryParticleEnabled() && territoryBoundaryManager != null) {
            try {
                boundaryParticleTask = new me.mami.stratocraft.task.TerritoryBoundaryParticleTask(
                    this, territoryManager, territoryBoundaryManager, territoryConfig);
                boundaryParticleTask.start();
                getLogger().info("§aTerritoryBoundaryParticleTask başlatıldı.");
            } catch (Exception e) {
                getLogger().warning("TerritoryBoundaryParticleTask başlatılamadı: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // YENİ: ClanTerritoryMenu oluştur
        me.mami.stratocraft.gui.ClanTerritoryMenu clanTerritoryMenu = null;
        if (territoryBoundaryManager != null && territoryConfig != null) {
            try {
                clanTerritoryMenu = new me.mami.stratocraft.gui.ClanTerritoryMenu(
                    this, clanManager, territoryManager, territoryBoundaryManager, territoryConfig);
                Bukkit.getPluginManager().registerEvents(clanTerritoryMenu, this);
            } catch (Exception e) {
                getLogger().warning("ClanTerritoryMenu oluşturulamadı: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // YENİ: Getter metodları için field'ları sakla (getClanTerritoryMenu için)
        this.clanTerritoryMenu = clanTerritoryMenu;
        this.territoryBoundaryManager = territoryBoundaryManager;
        this.territoryConfig = territoryConfig;
        
        // ✅ YENİ: PlayerFeatureMonitor başlat (oyuncu özellik kontrol sistemi)
        if (clanManager != null && buffManager != null) {
            try {
                playerFeatureMonitor = new me.mami.stratocraft.task.PlayerFeatureMonitor(
                    this, clanManager, buffManager);
                playerFeatureMonitor.start();
                getLogger().info("PlayerFeatureMonitor başlatıldı.");
            } catch (Exception e) {
                getLogger().warning("PlayerFeatureMonitor başlatılamadı: " + e.getMessage());
                e.printStackTrace();
            }
        }
        // YENİ: StructureActivationListener'a ClanRankSystem ve StructureCoreManager ekle
        Bukkit.getPluginManager().registerEvents(
            new StructureActivationListener(clanManager, territoryManager, 
                clanRankSystem != null ? clanRankSystem : null,
                structureCoreManager), this); // YAPI AKTİVASYONU
        Bukkit.getPluginManager().registerEvents(new me.mami.stratocraft.listener.StructureMenuListener(this, clanManager, territoryManager), this); // YAPI MENÜLERİ
        // Yeni Yapı Sistemi Listener'ı
        Bukkit.getPluginManager().registerEvents(new me.mami.stratocraft.listener.StructureCoreListener(
            this, structureCoreManager, structureRecipeManager, structureActivationItemManager, 
            clanManager, territoryManager), this); // YAPI ÇEKİRDEĞİ SİSTEMİ
        Bukkit.getPluginManager().registerEvents(new SiegeListener(siegeManager, territoryManager), this);
        Bukkit.getPluginManager().registerEvents(new me.mami.stratocraft.listener.EnderPearlListener(territoryManager), this);
        Bukkit.getPluginManager().registerEvents(new ScavengerListener(scavengerManager), this);
        Bukkit.getPluginManager().registerEvents(new LogisticsListener(logisticsManager), this);
        // RitualListener artık RitualInteractionListener içinde birleştirildi
        RitualInteractionListener ritualListener = new RitualInteractionListener(clanManager, territoryManager);
        ritualListener.setAllianceManager(allianceManager);
        Bukkit.getPluginManager().registerEvents(ritualListener, this);
        Bukkit.getPluginManager().registerEvents(new ShopListener(shopManager), this);
        Bukkit.getPluginManager().registerEvents(new MissionListener(missionManager), this);
        Bukkit.getPluginManager().registerEvents(new ResearchListener(researchManager), this);
        Bukkit.getPluginManager().registerEvents(new StructureListener(clanManager, researchManager), this);
        me.mami.stratocraft.listener.GhostRecipeListener ghostRecipeListener = 
            new me.mami.stratocraft.listener.GhostRecipeListener(ghostRecipeManager, researchManager);
        ghostRecipeListener.setTerritoryManager(territoryManager);
        ghostRecipeListener.setStructureCoreManager(structureCoreManager);
        ghostRecipeListener.setStructureRecipeManager(structureRecipeManager);
        ghostRecipeListener.setPlugin(this);
        Bukkit.getPluginManager().registerEvents(ghostRecipeListener, this);
        Bukkit.getPluginManager().registerEvents(new ConsumableListener(this), this);
        Bukkit.getPluginManager().registerEvents(new VillagerListener(), this);
        Bukkit.getPluginManager().registerEvents(new SpecialArmorListener(this), this);
        specialWeaponListener = new SpecialWeaponListener(this);
        Bukkit.getPluginManager().registerEvents(specialWeaponListener, this);
        
        // Silah Mod Değiştirme Sistemi (GUI)
        WeaponModeManager weaponModeManager = new WeaponModeManager(this);
        Bukkit.getPluginManager().registerEvents(weaponModeManager, this);
        Bukkit.getPluginManager().registerEvents(new GriefProtectionListener(territoryManager), this);
        virtualStorageListener = new VirtualStorageListener(territoryManager);
        Bukkit.getPluginManager().registerEvents(virtualStorageListener, this);
        Bukkit.getPluginManager()
                .registerEvents(new me.mami.stratocraft.listener.ClanChatListener(clanManager, langManager), this);
        // Dünya oluşturma ve doğal spawn listener'ı
        Bukkit.getPluginManager().registerEvents(
                new me.mami.stratocraft.listener.WorldGenerationListener(territoryManager, mobManager, difficultyManager, dungeonManager, bossManager), this);
        
        // Boss sistemi
        Bukkit.getPluginManager().registerEvents(new me.mami.stratocraft.listener.BossListener(bossManager, this), this);
        
        // Felaket hasar takibi
        Bukkit.getPluginManager().registerEvents(new me.mami.stratocraft.listener.DisasterListener(this), this);
        
        // Canlı eğitme sistemi
        Bukkit.getPluginManager().registerEvents(new me.mami.stratocraft.listener.TamingListener(tamingManager, difficultyManager, bossManager), this);
        
        // Çiftleştirme sistemi
        Bukkit.getPluginManager().registerEvents(new me.mami.stratocraft.listener.BreedingListener(breedingManager, tamingManager, this), this);
        
        // Yumurta çatlama kontrolü (her 5 saniyede bir) - PERFORMANS OPTİMİZASYONU
        // world.getEntities() yerine sadece takip edilen yumurtaları kontrol et
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                if (breedingManager == null) return;
                
                // Sadece takip edilen yumurtaları kontrol et (tüm entity'leri taramak yerine)
                java.util.Set<java.util.UUID> trackedEggs = breedingManager.getTrackedEggs();
                java.util.Set<java.util.UUID> toRemove = new java.util.HashSet<>(); // Bulunamayan yumurtalar
                
                for (java.util.UUID eggId : trackedEggs) {
                    org.bukkit.entity.Entity entity = null;
                    // Entity'yi UUID ile bul (sadece gerekli entity'yi yükle)
                    for (org.bukkit.World world : org.bukkit.Bukkit.getWorlds()) {
                        entity = world.getEntity(eggId);
                        if (entity != null) break; // Bulundu, döngüden çık
                    }
                    
                    if (entity == null || entity.isDead()) {
                        // Entity bulunamadı veya öldü, takipten çıkar (memory leak önleme)
                        toRemove.add(eggId);
                        continue;
                    }
                    
                    if (entity instanceof org.bukkit.entity.Turtle) {
                        org.bukkit.entity.Turtle turtle = (org.bukkit.entity.Turtle) entity;
                        if (turtle.hasMetadata("EggOwner") && turtle.getAge() >= 0) {
                            breedingManager.checkEggHatching(turtle);
                        }
                    }
                }
                
                // Bulunamayan yumurtaları temizle (memory leak önleme)
                if (!toRemove.isEmpty() && breedingManager != null) {
                    for (java.util.UUID eggId : toRemove) {
                        breedingManager.removeTrackedEgg(eggId);
                    }
                }
            }
        }.runTaskTimer(this, 100L, 100L); // Her 5 saniye
        // ClanMenu zaten Listener implement ediyor, kaydet
        Bukkit.getPluginManager().registerEvents(clanMenu, this);

        // CombatLogManager kaydet (savaştan kaçmayı engellemek için)
        combatLogManager = new me.mami.stratocraft.manager.CombatLogManager();
        Bukkit.getPluginManager().registerEvents(combatLogManager, this);
        combatLogManager.startCleanupTask(this);

        // Yeni mekanikler: Kervan, Savaş Mühendisliği, Kontratlar
        Bukkit.getPluginManager().registerEvents(new me.mami.stratocraft.listener.CaravanListener(caravanManager),
                this);
        siegeWeaponManager = new me.mami.stratocraft.manager.SiegeWeaponManager(this);
        Bukkit.getPluginManager()
                .registerEvents(new me.mami.stratocraft.listener.SiegeWeaponListener(siegeWeaponManager), this);
        Bukkit.getPluginManager().registerEvents(new me.mami.stratocraft.listener.ContractListener(contractManager),
                this);

        // Yeni sistemler: Tuzaklar, Kancalar, Casusluk, Hava Drop
        // (trapManager, newMineManager, supplyDropManager zaten yukarıda initialize edildi)
        Bukkit.getPluginManager().registerEvents(new me.mami.stratocraft.listener.TrapListener(trapManager), this);
        
        // 25 mayın için hayalet tariflerini ekle (ghostRecipeManager oluşturulduktan sonra)
        ghostRecipeManager.initializeMineRecipes(newMineManager);
        Bukkit.getPluginManager().registerEvents(new me.mami.stratocraft.listener.NewMineListener(newMineManager), this);

        // SpecialItemManager zaten yukarıda başlatıldı (ItemManager için gerekli)
        Bukkit.getPluginManager()
                .registerEvents(new me.mami.stratocraft.listener.SpecialItemListener(specialItemManager), this);

        Bukkit.getPluginManager().registerEvents(new me.mami.stratocraft.listener.SupplyDropListener(supplyDropManager),
                this);
        
        // Mob Drop Listener - Özel mob drop sistemi
        Bukkit.getPluginManager().registerEvents(new me.mami.stratocraft.listener.MobDropListener(), this);
        
        // Seviyeli Silah ve Zırh Listener
        Bukkit.getPluginManager().registerEvents(new me.mami.stratocraft.listener.WeaponArmorListener(), this);

        // Veri yükleme (yeni sistemlerle)
        dataManager.loadAll(clanManager, contractManager, shopManager, virtualStorageListener, 
                allianceManager, disasterManager, clanBankSystem, clanMissionSystem, 
                clanActivitySystem, trapManager, contractRequestManager, contractTermsManager);
        
        // Periyodik otomatik kayıt başlat
        if (dataManager != null) {
            dataManager.startAutoSave(() -> {
                // Auto-save callback: Tüm verileri kaydet (async)
                dataManager.saveAll(clanManager, contractManager, shopManager, virtualStorageListener, 
                        allianceManager, disasterManager, clanBankSystem, clanMissionSystem, 
                        clanActivitySystem, trapManager, contractRequestManager, contractTermsManager, false);
                return null;
            });
            
            // ✅ YENİ: DataManager temizleme task'larını başlat
            dataManager.startCleanupTasks();
            
            // ✅ YENİ: SQLite WAL checkpoint task'ını başlat
            if (dataManager.getDatabaseManager() != null) {
                dataManager.getDatabaseManager().startWalCheckpointTask();
            }
            
            // ✅ YENİ: SQLiteDataManager eski veri temizleme task'ını başlat
            if (dataManager.getSQLiteDataManager() != null) {
                dataManager.getSQLiteDataManager().startOldDataCleanupTask();
            }
        }
        
        // Güç profillerini yükle (StratocraftPowerSystem varsa)
        if (stratocraftPowerSystem != null) {
            stratocraftPowerSystem.loadAllPlayerProfiles();
        }
        
        // ✅ BATARYA PARTİKÜL SİSTEMİ: Config yükle (configManager hazır olduktan sonra)
        if (batteryParticleManager != null && configManager != null) {
            batteryParticleManager.loadConfig(configManager.getConfig());
        }
        
        // HUD Manager'ı başlat (missionManager'dan sonra)
        if (hudManager != null && missionManager != null) {
            hudManager.setManagers(disasterManager, newBatteryManager, shopManager, missionManager, 
                                  contractManager, buffManager, clanManager, territoryManager);
            hudManager.start();
        }

        // ✅ OYUNCU ADI GÜNCELLEME: Periyodik güncelleme (config'den interval al)
        // Güç değiştiğinde seviye değişebilir, oyuncu adını güncelle
        if (stratocraftPowerSystem != null && configManager != null) {
            long updateInterval = configManager.getGameBalanceConfig() != null ? 
                configManager.getGameBalanceConfig().getPowerSystemPlayerNameUpdateInterval() : 600L;
            new org.bukkit.scheduler.BukkitRunnable() {
                @Override
                public void run() {
                    // PowerSystemListener'ı bul ve tüm oyuncuların adlarını güncelle
                    // Field'dan direkt al (daha güvenli ve hızlı)
                    if (powerSystemListener != null) {
                        powerSystemListener.updateAllPlayerNames();
                    }
                }
            }.runTaskTimer(this, updateInterval, updateInterval);
        }
        
        // 3. Zamanlayıcıları Başlat
        // OPTİMİZE: BuffTask interval'i 20L'den 10L'ye düşürüldü ama iç optimizasyonlar ile daha verimli
        new BuffTask(territoryManager, siegeWeaponManager).runTaskTimer(this, 20L, 10L);
        // ✅ PERFORMANS OPTİMİZASYONU: DisasterTask interval'i artırıldı (20L -> 60L = 3 saniye)
        new DisasterTask(disasterManager, territoryManager).runTaskTimer(this, 20L, 60L);
        
        // Otomatik felaket spawn kontrolü (her 10 dakikada bir)
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                disasterManager.checkAutoSpawn();
            }
        }.runTaskTimer(this, 12000L, 12000L); // 10 dakika = 12000 tick
        
        // Countdown BossBar'ı başlangıçta oluştur
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (disasterManager != null) {
                disasterManager.checkAutoSpawn(); // Countdown BossBar'ı oluştur
            }
        }, 40L); // 2 saniye sonra (sunucu tamamen yüklendikten sonra)
        
        // YENİ: StructureEffectManager oluştur (PlayerJoinEvent'ten önce olmalı)
        structureEffectManager = 
            new me.mami.stratocraft.manager.StructureEffectManager(this, clanManager, playerDataManager);
        
        // PlayerJoinEvent listener - BossBar'a yeni oyuncuları ekle ve kontrat cezalarını uygula
        Bukkit.getPluginManager().registerEvents(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler
            public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
                if (disasterManager != null) {
                    disasterManager.onPlayerJoin(event.getPlayer());
                }
                if (contractManager != null) {
                    contractManager.onPlayerJoin(event.getPlayer());
                }
                if (bossManager != null) {
                    bossManager.onPlayerJoin(event.getPlayer());
                }
                if (hudManager != null) {
                    hudManager.onPlayerJoin(event.getPlayer());
                }
                // YENİ: StructureEffectManager - Oyuncu girişinde yapı efektlerini uygula
                if (structureEffectManager != null) {
                    structureEffectManager.onPlayerJoin(event.getPlayer());
                }
                // ✅ OYUNCU ADI GÜNCELLEME: Seviyeye göre renk ve seviye gösterimi
                // PowerSystemListener zaten PlayerJoinEvent'i dinliyor ve updatePlayerName çağırıyor
            }
            
            @org.bukkit.event.EventHandler
            public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
                if (disasterManager != null) {
                    disasterManager.onPlayerQuit(event.getPlayer());
                }
                if (hudManager != null) {
                    hudManager.onPlayerQuit(event.getPlayer());
                }
                // YENİ: StructureEffectManager - Oyuncu çıkışında yapı efektlerini kaldır
                if (structureEffectManager != null) {
                    structureEffectManager.onPlayerQuit(event.getPlayer());
                }
            }
        }, this);
        
        // StructureEffectTask'ı yeni manager ile başlat
        // OPTİMİZE: 20L'den 40L'ye çıkarıldı (saniyede 1'den 0.5'e)
        new me.mami.stratocraft.task.StructureEffectTask(structureEffectManager).runTaskTimer(this, 20L, 40L); // YAPI EFEKTLERİ

        // Casusluk Dürbünü için Scheduler - PERFORMANS OPTİMİZASYONU
        // RayTrace ağır bir işlem olduğu için sıklığı azaltıldı (5 tick -> 20 tick = 1 saniye)
        // Event bazlı kontrol için SpecialItemListener'da PlayerInteractEvent kullanılabilir
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    ItemStack item = player.getInventory().getItemInMainHand();
                    // Özel Casusluk Dürbünü kontrolü
                    if (item != null && item.getType() == org.bukkit.Material.SPYGLASS && 
                        me.mami.stratocraft.manager.ItemManager.isCustomItem(item, "CASUSLUK_DURBUN")) {
                        int maxDistance = configManager != null && configManager.getGameBalanceConfig() != null 
                            ? configManager.getGameBalanceConfig().getMainRayTraceMaxDistance() : 50;
                        org.bukkit.util.RayTraceResult result = player.rayTraceEntities(maxDistance);
                        // Null kontrolü: Oyuncu boşluğa bakıyorsa result null olabilir
                        if (result != null) {
                            specialItemManager.handleSpyglass(player, result);
                        } else {
                            // Boşluğa bakıyorsa spyglass verisini temizle
                            specialItemManager.clearSpyData(player);
                        }
                    } else {
                        specialItemManager.clearSpyData(player);
                    }
                }
            }
        }.runTaskTimer(this, 0L, 
            configManager != null && configManager.getGameBalanceConfig() != null 
                ? configManager.getGameBalanceConfig().getMainRayTraceInterval() : 20L);
        long mobRideInterval = configManager != null && configManager.getGameBalanceConfig() != null 
            ? configManager.getGameBalanceConfig().getMobRideTaskInterval() : 5L;
        new MobRideTask(mobManager).runTaskTimer(this, mobRideInterval, mobRideInterval);
        new DrillTask(territoryManager).runTaskTimer(this, configManager.getDrillInterval(),
                configManager.getDrillInterval());
        new CropTask(territoryManager).runTaskTimer(this, 40L, 40L); // Her 2 saniye

        // 4. Tab Completers
        getCommand("klan").setTabCompleter(new me.mami.stratocraft.command.ClanTabCompleter());
        getCommand("kontrat").setTabCompleter(new me.mami.stratocraft.command.ContractTabCompleter());
        
        // ✅ GÜÇ SİSTEMİ KOMUTU: SGP komutunu kaydet
        if (getCommand("sgp") != null) {
            me.mami.stratocraft.command.SGPCommand sgpCommand = new me.mami.stratocraft.command.SGPCommand();
            getCommand("sgp").setExecutor(sgpCommand);
            getCommand("sgp").setTabCompleter(sgpCommand);
        }
        
        // Silah modu komutu
        if (getCommand("weaponmode") != null) {
            me.mami.stratocraft.command.WeaponModeCommand weaponModeCmd = new me.mami.stratocraft.command.WeaponModeCommand(this);
            getCommand("weaponmode").setExecutor(weaponModeCmd);
            getCommand("weaponmode").setTabCompleter(weaponModeCmd);
        }

        // 7. Komutlar (Sadece Admin için - Oyuncular ritüelleri kullanır)
        getCommand("klan").setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cBu komut sadece oyuncular için!");
                    return true;
                }

                Player p = (Player) sender;

                // HERKESİN KULLANABİLECEĞİ KOMUTLAR (Menü, Bilgi vb.)
                if (args.length > 0 && args[0].equalsIgnoreCase("menü")) {
                    clanMenu.openMenu(p);
                    return true;
                }

                if (args.length > 0 && args[0].equalsIgnoreCase("bilgi")) {
                    Clan clan = clanManager.getClanByPlayer(p.getUniqueId());
                    if (clan != null) {
                        p.sendMessage("§6=== Klan Bilgileri ===");
                        p.sendMessage("§7İsim: §e" + clan.getName());
                        p.sendMessage("§7Üye Sayısı: §e" + clan.getMembers().size());
                        p.sendMessage("§7Bakiye: §e" + clan.getBalance() + " altın");
                        p.sendMessage("§7Teknoloji Seviyesi: §e" + clan.getTechLevel());
                    } else {
                        p.sendMessage("§cBir klana üye değilsin!");
                    }
                    return true;
                }

                // BURADAN SONRASI SADECE ADMİNLER İÇİN
                if (!p.hasPermission("stratocraft.admin")) {
                    p.sendMessage("§cKlan kurmak için Klan Kristali kullanmalısın!");
                    p.sendMessage("§7Klan Kristali craft et ve Klan Çitleri ile çevrelenmiş alana yerleştir.");
                    return true;
                }

                // Admin komutları
                if (args.length > 0 && args[0].equalsIgnoreCase("kur")) {
                    if (args.length > 1) {
                        Clan newClan = clanManager.createClan(args[1], p.getUniqueId());
                        if (newClan != null) {
                            newClan.setTerritory(new Territory(newClan.getId(), p.getLocation()));
                            p.sendMessage("§aKlan kuruldu: " + args[1]);
                        } else {
                            p.sendMessage("§cZaten bir klana üyesin!");
                        }
                    } else {
                        p.sendMessage("§cKullanım: /klan kur <isim>");
                    }
                } else if (args.length > 0 && args[0].equalsIgnoreCase("ayril")) {
                    Clan clan = clanManager.getClanByPlayer(p.getUniqueId());
                    if (clan != null) {
                        if (clan.getRank(p.getUniqueId()) == Clan.Rank.LEADER) {
                            clanManager.disbandClan(clan);
                            territoryManager.setCacheDirty(); // Cache'i güncelle
                            p.sendMessage("§cKlanınız dağıtıldı.");
                        } else {
                            clan.getMembers().remove(p.getUniqueId());
                            p.sendMessage("§eKlandan ayrıldınız.");
                        }
                    } else {
                        p.sendMessage("§cBir klana üye değilsiniz!");
                    }
                } else if (args.length > 0 && args[0].equalsIgnoreCase("kristal")) {
                    Clan clan = clanManager.getClanByPlayer(p.getUniqueId());
                    if (clan == null) {
                        p.sendMessage("§cBir klana üye değilsiniz!");
                        return true;
                    }
                    if (clan.getRank(p.getUniqueId()) != Clan.Rank.LEADER
                            && clan.getRank(p.getUniqueId()) != Clan.Rank.GENERAL) {
                        p.sendMessage("§cBu işlem için yetkiniz yok!");
                        return true;
                    }
                    if (clan.getTerritory() == null) {
                        Territory newTerritory = new Territory(clan.getId(), p.getLocation());
                        clan.setTerritory(newTerritory);
                        territoryManager.setCacheDirty(); // Cache'i güncelle
                        p.sendMessage("§aKristal dikildi! Bölgeniz aktif.");
                    } else {
                        p.sendMessage("§eZaten bir bölgeniz var. Yeni kristal dikmek için mevcut bölgeyi kaldırın.");
                    }
                } else if (args.length > 0 && args[0].equalsIgnoreCase("alan")) {
                    // Alan genişletme - YENİ
                    Clan clan = clanManager.getClanByPlayer(p.getUniqueId());
                    if (clan == null) {
                        p.sendMessage("§cBir klana üye değilsiniz!");
                        return true;
                    }
                    if (clan.getRank(p.getUniqueId()) != Clan.Rank.LEADER
                            && clan.getRank(p.getUniqueId()) != Clan.Rank.GENERAL) {
                        p.sendMessage("§cBu işlem için yetkiniz yok!");
                        return true;
                    }
                    if (clan.getTerritory() == null) {
                        p.sendMessage("§cÖnce bir bölgeniz olmalı! Kristal dikin.");
                        return true;
                    }
                    
                    if (args.length > 1 && args[1].equalsIgnoreCase("genislet")) {
                        if (args.length > 2) {
                            try {
                                int amount = Integer.parseInt(args[2]);
                                if (amount <= 0 || amount > 100) {
                                    p.sendMessage("§cGenişletme miktarı 1-100 arası olmalı!");
                                    return true;
                                }
                                
                                Territory territory = clan.getTerritory();
                                int currentRadius = territory.getRadius();
                                int newRadius = currentRadius + amount;
                                
                                // Maksimum radius kontrolü (config'den alınabilir)
                                int maxRadius = 500; // Varsayılan maksimum
                                if (newRadius > maxRadius) {
                                    p.sendMessage("§cMaksimum alan boyutu: " + maxRadius + " blok!");
                                    return true;
                                }
                                
                                territory.expand(amount);
                                territoryManager.setCacheDirty(); // Cache'i güncelle
                                p.sendMessage("§aAlan genişletildi! Yeni radius: §e" + newRadius + " blok");
                            } catch (NumberFormatException e) {
                                p.sendMessage("§cGeçersiz sayı! Kullanım: /klan alan genislet <miktar>");
                            }
                        } else {
                            p.sendMessage("§cKullanım: /klan alan genislet <miktar>");
                        }
                    } else {
                        Territory territory = clan.getTerritory();
                        p.sendMessage("§a§l═══════════════════════════");
                        p.sendMessage("§aAlan Bilgileri:");
                        p.sendMessage("§7Mevcut Radius: §e" + territory.getRadius() + " blok");
                        p.sendMessage("§7Alanı genişletmek için: §e/klan alan genislet <miktar>");
                        p.sendMessage("§a§l═══════════════════════════");
                    }
                } else if (args.length > 0 && args[0].equalsIgnoreCase("maas")) {
                    // Maaş yönetimi - YENİ
                    Clan clan = clanManager.getClanByPlayer(p.getUniqueId());
                    if (clan == null) {
                        p.sendMessage("§cBir klana üye değilsiniz!");
                        return true;
                    }
                    if (clan.getRank(p.getUniqueId()) != Clan.Rank.LEADER
                            && clan.getRank(p.getUniqueId()) != Clan.Rank.GENERAL) {
                        p.sendMessage("§cBu işlem için yetkiniz yok!");
                        return true;
                    }
                    
                    if (args.length > 1 && args[1].equalsIgnoreCase("iptal")) {
                        // Maaş iptal et (belirli bir üye için)
                        if (args.length > 2) {
                            String targetName = args[2];
                            org.bukkit.OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetName);
                            if (targetPlayer == null || !clan.getMembers().containsKey(targetPlayer.getUniqueId())) {
                                p.sendMessage("§cOyuncu bulunamadı veya klan üyesi değil!");
                                return true;
                            }
                            
                            // Maaş iptal etme (gelecekte implement edilebilir)
                            p.sendMessage("§a" + targetName + " için maaş iptal edildi.");
                        } else {
                            p.sendMessage("§cKullanım: /klan maas iptal <oyuncu>");
                        }
                    } else if (args.length > 1 && args[1].equalsIgnoreCase("aktif")) {
                        // Maaş aktifleştir (belirli bir üye için)
                        if (args.length > 2) {
                            String targetName = args[2];
                            org.bukkit.OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetName);
                            if (targetPlayer == null || !clan.getMembers().containsKey(targetPlayer.getUniqueId())) {
                                p.sendMessage("§cOyuncu bulunamadı veya klan üyesi değil!");
                                return true;
                            }
                            
                            // Maaş aktifleştirme (gelecekte implement edilebilir)
                            p.sendMessage("§a" + targetName + " için maaş aktifleştirildi.");
                        } else {
                            p.sendMessage("§cKullanım: /klan maas aktif <oyuncu>");
                        }
                    } else {
                        p.sendMessage("§a§l═══════════════════════════");
                        p.sendMessage("§aMaaş Yönetimi:");
                        p.sendMessage("§7Maaş iptal et: §e/klan maas iptal <oyuncu>");
                        p.sendMessage("§7Maaş aktifleştir: §e/klan maas aktif <oyuncu>");
                        p.sendMessage("§a§l═══════════════════════════");
                    }
                } else {
                    p.sendMessage("§eKullanım: /klan <menü|bilgi|kur|ayril|kristal|alan|maas>");
                    p.sendMessage("§7Oyuncular: menü, bilgi");
                    p.sendMessage("§7Lider/General: alan, maas");
                    p.sendMessage("§7Adminler: kur, ayril, kristal");
                }
                return true;
            }
        });

        getCommand("kontrat").setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                if (sender instanceof Player) {
                    Player p = (Player) sender;
                    if (args.length > 0 && args[0].equalsIgnoreCase("list")) {
                        // GUI menüsünü aç
                        if (contractMenu != null) {
                            contractMenu.openMainMenu(p, 1);
                        } else {
                            // Fallback: Eski liste gösterimi
                            p.sendMessage("§eAktif Sözleşmeler:");
                            int i = 1;
                            for (me.mami.stratocraft.model.Contract contract : contractManager.getContracts()) {
                                if (contract.getMaterial() != null) {
                                    p.sendMessage("§7" + i + ". " + contract.getMaterial() + " x" + contract.getAmount() +
                                            " → " + contract.getReward() + " altın (ID: "
                                            + contract.getId().toString().substring(0, 8) + ")");
                                } else {
                                    p.sendMessage("§7" + i + ". " + contract.getType() + " → " + contract.getReward() + " altın (ID: "
                                            + contract.getId().toString().substring(0, 8) + ")");
                                }
                                i++;
                            }
                        }
                    } else if (args.length > 0 && args[0].equalsIgnoreCase("olustur")) {
                        Clan clan = clanManager.getClanByPlayer(p.getUniqueId());
                        if (clan == null) {
                            p.sendMessage("§cBir klana üye değilsiniz!");
                            return true;
                        }
                        if (args.length < 4) {
                            p.sendMessage("§cKullanım: /kontrat olustur <malzeme> <miktar> <ödül> <gün>");
                            p.sendMessage("§7Örnek: /kontrat olustur IRON_INGOT 100 5000 2");
                            return true;
                        }
                        try {
                            org.bukkit.Material mat = org.bukkit.Material.valueOf(args[1].toUpperCase());
                            int amount = Integer.parseInt(args[2]);
                            double reward = Double.parseDouble(args[3]);
                            long days = args.length > 4 ? Long.parseLong(args[4]) : 2;

                            if (clan.getBalance() < reward) {
                                p.sendMessage("§cKlanınızın kasasında yeterli para yok!");
                                return true;
                            }

                            contractManager.createContract(clan.getId(), mat, amount, reward, days);
                            p.sendMessage("§aSözleşme oluşturuldu!");
                        } catch (Exception e) {
                            p.sendMessage("§cHatalı parametre! Malzeme adını doğru yazın (örn: IRON_INGOT)");
                        }
                    } else if (args.length > 0 && args[0].equalsIgnoreCase("teslim")) {
                        if (args.length < 2) {
                            p.sendMessage("§cKullanım: /kontrat teslim <sözleşme_id> <miktar>");
                            return true;
                        }
                        try {
                            java.util.UUID contractId = java.util.UUID.fromString(args[1]);
                            int amount = args.length > 2 ? Integer.parseInt(args[2]) : 1;

                            me.mami.stratocraft.model.Contract contract = contractManager.getContract(contractId);
                            if (contract == null) {
                                p.sendMessage("§cSözleşme bulunamadı!");
                                return true;
                            }

                            // Çift taraflı kontrat kontrolü
                            if (contract.isBilateralContract()) {
                                // Çift taraflı kontrat için özel işlem
                                me.mami.stratocraft.model.ContractTerms playerTerms = null;
                                if (contract.getTermsA() != null && contract.getTermsA().getPlayerId().equals(p.getUniqueId())) {
                                    playerTerms = contract.getTermsA();
                                } else if (contract.getTermsB() != null && contract.getTermsB().getPlayerId().equals(p.getUniqueId())) {
                                    playerTerms = contract.getTermsB();
                                }
                                
                                if (playerTerms == null) {
                                    p.sendMessage("§cBu kontratın tarafı değilsiniz!");
                                    return true;
                                }
                                
                                if (playerTerms.getType() != me.mami.stratocraft.enums.ContractType.RESOURCE_COLLECTION) {
                                    p.sendMessage("§cBu kontrat tipi için teslim komutu kullanılamaz!");
                                    return true;
                                }
                                
                                // Envanterden malzeme kontrolü
                                int playerAmount = 0;
                                for (org.bukkit.inventory.ItemStack item : p.getInventory().getContents()) {
                                    if (item != null && item.getType() == playerTerms.getMaterial()) {
                                        playerAmount += item.getAmount();
                                    }
                                }
                                
                                if (playerAmount < amount) {
                                    p.sendMessage("§cYeterli malzemeniz yok! (" + playerAmount + "/" + playerTerms.getAmount() + ")");
                                    return true;
                                }
                                
                                // Malzemeyi kaldır
                                int remaining = amount;
                                for (org.bukkit.inventory.ItemStack item : p.getInventory().getContents()) {
                                    if (item != null && item.getType() == playerTerms.getMaterial() && remaining > 0) {
                                        int remove = Math.min(item.getAmount(), remaining);
                                        item.setAmount(item.getAmount() - remove);
                                        remaining -= remove;
                                    }
                                }
                                
                                // Çift taraflı kontrat teslim et
                                contractManager.deliverBilateralContract(contractId, p.getUniqueId(), amount);
                                p.sendMessage("§a" + amount + " " + playerTerms.getMaterial() + " teslim edildi!");
                                return true;
                            }
                            
                            // Eski sistem (tek taraflı kontrat)
                            // Güvenlik kontrolü: Kontratı kabul eden kişi kontrolü
                            if (contract.getAcceptor() == null) {
                                // Henüz kabul edilmemiş, önce kabul et
                                contractManager.acceptContract(contractId, p.getUniqueId());
                                p.sendMessage("§aSözleşmeyi kabul ettiniz!");
                            } else {
                                // Kontrat zaten kabul edilmiş, sadece kabul eden kişi teslim edebilir
                                if (!contract.getAcceptor().equals(p.getUniqueId())) {
                                    // Klan kontrolü: Aynı klan üyesi olabilir
                                    me.mami.stratocraft.model.Clan acceptorClan = clanManager
                                            .getClanByPlayer(contract.getAcceptor());
                                    me.mami.stratocraft.model.Clan playerClan = clanManager
                                            .getClanByPlayer(p.getUniqueId());

                                    if (acceptorClan == null || playerClan == null
                                            || !acceptorClan.getId().equals(playerClan.getId())) {
                                        p.sendMessage(
                                                "§cBu kontratı siz kabul etmediniz! Sadece kabul eden kişi teslim edebilir.");
                                        return true;
                                    }
                                }
                            }

                            // Envanterden malzeme kontrolü
                            int playerAmount = 0;
                            for (org.bukkit.inventory.ItemStack item : p.getInventory().getContents()) {
                                if (item != null && item.getType() == contract.getMaterial()) {
                                    playerAmount += item.getAmount();
                                }
                            }

                            if (playerAmount < amount) {
                                p.sendMessage("§cYeterli malzemeniz yok! (" + playerAmount + "/" + amount + ")");
                                return true;
                            }

                            // Malzemeyi kaldır
                            int remaining = amount;
                            for (org.bukkit.inventory.ItemStack item : p.getInventory().getContents()) {
                                if (item != null && item.getType() == contract.getMaterial() && remaining > 0) {
                                    int remove = Math.min(item.getAmount(), remaining);
                                    item.setAmount(item.getAmount() - remove);
                                    remaining -= remove;
                                }
                            }

                            contractManager.deliverContract(contractId, amount);
                            p.sendMessage("§a" + amount + " " + contract.getMaterial() + " teslim edildi!");
                        } catch (Exception e) {
                            p.sendMessage("§cHatalı sözleşme ID!");
                        }
                    } else {
                        p.sendMessage("§eKullanım: /kontrat <list|olustur|teslim>");
                    }
                }
                return true;
            }
        });

        // Admin test komutları
        getCommand("stratocraft").setExecutor(new me.mami.stratocraft.command.AdminCommandExecutor(this));
        getCommand("stratocraft").setTabCompleter(new me.mami.stratocraft.command.AdminCommandExecutor(this));

        getLogger().info("Stratocraft v10.0: Eksiksiz Sistem Aktif!");
    }

    @Override
    public void onDisable() {
        // ✅ YENİ: DataManager temizleme task'larını durdur
        if (dataManager != null) {
            dataManager.stopCleanupTasks();
        }
        
        // ✅ YENİ: SQLiteDataManager eski veri temizleme task'ını durdur
        if (dataManager != null && dataManager.getSQLiteDataManager() != null) {
            dataManager.getSQLiteDataManager().stopOldDataCleanupTask();
        }
        
        // ✅ ÖNCE: Tüm async task'ları iptal et (plugin kapatılırken)
        org.bukkit.Bukkit.getScheduler().cancelTasks(this);
        
        // ✅ YENİ: PlayerFeatureMonitor durdur
        if (playerFeatureMonitor != null) {
            playerFeatureMonitor.stop();
            getLogger().info("PlayerFeatureMonitor durduruldu.");
        }
        
        // ✅ YENİ: TerritoryBoundaryParticleTask durdur
        if (boundaryParticleTask != null) {
            boundaryParticleTask.stop();
            getLogger().info("TerritoryBoundaryParticleTask durduruldu.");
        }
        
        // ✅ DisasterArenaManager'ı durdur
        if (disasterArenaManager != null) {
            disasterArenaManager.shutdown();
        }
        
        // ✅ DisasterManager task'larını durdur
        if (disasterManager != null) {
            disasterManager.shutdown();
        }
        
        // TaskManager'ı kapat (tüm task'ları iptal et)
        if (taskManager != null) {
            taskManager.shutdown();
        }
        
        // TerritoryListener'daki aktif kristal taşıma task'larını iptal et
        if (territoryListener != null) {
            territoryListener.cancelAllCrystalMoveTasks();
        }
        
        // HUD Manager'ı durdur
        if (hudManager != null) {
            hudManager.stop();
        }
        
        // Batarya sistemini temizle (geçici barrier bloklarını kaldır)
        if (batteryManager != null) {
            batteryManager.shutdown();
            getLogger().info("Stratocraft: Batarya sistemi temizlendi (geçici bloklar kaldırıldı).");
        }

        // Mayınları kaydet (NewMineManager otomatik kaydediliyor)
        if (newMineManager != null) {
            newMineManager.shutdown();
            getLogger().info("Stratocraft: Mayınlar kaydedildi.");
        }

        // Veri kaydetme (onDisable'da her zaman senkron kayıt yapılmalı - veri kaybı
        // riski)
        // NOT: Sunucu kapanırken thread havuzları kapatılır, asenkron işlemler
        // tamamlanmayabilir
        if (dataManager != null && clanManager != null && contractManager != null &&
                shopManager != null && virtualStorageListener != null && allianceManager != null && disasterManager != null) {
            // Kapanış işlemlerinde her zaman senkron kayıt (yeni sistemlerle)
            // Tuzaklar da DataManager üzerinden kaydediliyor
            dataManager.saveAll(clanManager, contractManager, shopManager, virtualStorageListener, 
                    allianceManager, disasterManager, clanBankSystem, clanMissionSystem, clanActivitySystem, 
                    trapManager, contractRequestManager, contractTermsManager, true);
            getLogger().info("Stratocraft: Veriler kaydedildi.");
            
            // ✅ SQLite veritabanını kapat
            if (dataManager != null && dataManager.getDatabaseManager() != null) {
                dataManager.getDatabaseManager().close();
                getLogger().info("Stratocraft: SQLite veritabanı kapatıldı.");
            }
            
            // Güç profillerini kaydet (sync - onDisable)
            if (stratocraftPowerSystem != null) {
                stratocraftPowerSystem.saveAllPlayerProfilesSync();
            }
        }
        
        // Periyodik otomatik kayıt durdur
        if (dataManager != null) {
            dataManager.stopAutoSave();
        }
        
        // NOT: Tuzaklar artık DataManager üzerinden kaydediliyor
        // TrapManager.saveTraps() çağrılmıyor çünkü duplikasyon olur
        getLogger().info("Stratocraft: Plugin kapatılıyor.");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public me.mami.stratocraft.util.LangManager getLangManager() {
        return langManager;
    }

    public static Main getInstance() {
        return instance;
    }

    // Getter metodları
    public DisasterManager getDisasterManager() {
        return disasterManager;
    }

    public ClanManager getClanManager() {
        return clanManager;
    }

    public ScavengerManager getScavengerManager() {
        return scavengerManager;
    }

    public BuffManager getBuffManager() {
        return buffManager;
    }

    public me.mami.stratocraft.manager.EconomyManager getEconomyManager() {
        return economyManager;
    }

    public TerritoryManager getTerritoryManager() {
        return territoryManager;
    }
    
    public me.mami.stratocraft.manager.StructureCoreManager getStructureCoreManager() {
        return structureCoreManager;
    }
    
    public me.mami.stratocraft.manager.StructureRecipeManager getStructureRecipeManager() {
        return structureRecipeManager;
    }
    
    public me.mami.stratocraft.manager.StructureActivationItemManager getStructureActivationItemManager() {
        return structureActivationItemManager;
    }
    
    public me.mami.stratocraft.manager.PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
    
    public me.mami.stratocraft.manager.StructureEffectManager getStructureEffectManager() {
        return structureEffectManager;
    }
    
    public me.mami.stratocraft.manager.RecipeManager getRecipeManager() {
        return recipeManager;
    }

    public ContractManager getContractManager() {
        return contractManager;
    }
    
    public AllianceManager getAllianceManager() {
        return allianceManager;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public ResearchManager getResearchManager() {
        return researchManager;
    }

    public me.mami.stratocraft.manager.SupplyDropManager getSupplyDropManager() {
        return supplyDropManager;
    }

    public me.mami.stratocraft.manager.GhostRecipeManager getGhostRecipeManager() {
        return ghostRecipeManager;
    }

    public me.mami.stratocraft.manager.TrainingManager getTrainingManager() {
        return trainingManager;
    }

    public me.mami.stratocraft.gui.ClanMenu getClanMenu() {
        return clanMenu;
    }
    
    // YENİ: Territory sistemi getter'ları
    public me.mami.stratocraft.manager.TerritoryBoundaryManager getTerritoryBoundaryManager() {
        return territoryBoundaryManager;
    }
    
    public me.mami.stratocraft.manager.config.TerritoryConfig getTerritoryConfig() {
        return territoryConfig;
    }
    
    public me.mami.stratocraft.gui.ClanTerritoryMenu getClanTerritoryMenu() {
        return clanTerritoryMenu;
    }
    
    public me.mami.stratocraft.gui.ClanMissionMenu getClanMissionMenu() {
        return clanMissionMenu;
    }
    
    public me.mami.stratocraft.gui.ClanMemberMenu getClanMemberMenu() {
        return clanMemberMenu;
    }
    
    public me.mami.stratocraft.gui.ClanStatsMenu getClanStatsMenu() {
        return clanStatsMenu;
    }
    
    public me.mami.stratocraft.gui.ContractMenu getContractMenu() {
        return contractMenu;
    }
    
    public me.mami.stratocraft.manager.ContractRequestManager getContractRequestManager() {
        return contractRequestManager;
    }
    
    public me.mami.stratocraft.manager.ContractTermsManager getContractTermsManager() {
        return contractTermsManager;
    }
    
    public me.mami.stratocraft.gui.PowerMenu getPowerMenu() {
        return powerMenu;
    }
    
    public me.mami.stratocraft.gui.ClanBankMenu getClanBankMenu() {
        return clanBankMenu;
    }
    
    public me.mami.stratocraft.gui.ClanStructureMenu getClanStructureMenu() {
        return clanStructureMenu;
    }
    
    public me.mami.stratocraft.gui.AllianceMenu getAllianceMenu() {
        return allianceMenu;
    }
    
    public me.mami.stratocraft.gui.PeaceRequestMenu getPeaceRequestMenu() {
        return peaceRequestMenu;
    }
    
    public me.mami.stratocraft.manager.PeaceRequestManager getPeaceRequestManager() {
        return peaceRequestManager;
    }
    
    public me.mami.stratocraft.gui.CaravanMenu getCaravanMenu() {
        return caravanMenu;
    }
    
    public me.mami.stratocraft.gui.TamingMenu getTamingMenu() {
        return tamingMenu;
    }
    
    public me.mami.stratocraft.gui.BreedingMenu getBreedingMenu() {
        return breedingMenu;
    }
    
    public me.mami.stratocraft.gui.TrainingMenu getTrainingMenu() {
        return trainingMenu;
    }
    
    public me.mami.stratocraft.listener.PersonalTerminalListener getPersonalTerminalListener() {
        return personalTerminalListener;
    }

    public BatteryManager getBatteryManager() {
        return batteryManager;
    }
    
    public NewBatteryManager getNewBatteryManager() {
        return newBatteryManager;
    }
    
    public me.mami.stratocraft.manager.BatteryParticleManager getBatteryParticleManager() {
        return batteryParticleManager;
    }
    
    public me.mami.stratocraft.manager.TaskManager getTaskManager() {
        return taskManager;
    }

    public SiegeManager getSiegeManager() {
        return siegeManager;
    }

    public MissionManager getMissionManager() {
        return missionManager;
    }

    public MobManager getMobManager() {
        return mobManager;
    }

    public me.mami.stratocraft.manager.SiegeWeaponManager getSiegeWeaponManager() {
        return siegeWeaponManager;
    }

    public CaravanManager getCaravanManager() {
        return caravanManager;
    }

    public me.mami.stratocraft.manager.TrapManager getTrapManager() {
        return trapManager;
    }
    
    public me.mami.stratocraft.manager.NewMineManager getNewMineManager() {
        return newMineManager;
    }

    public me.mami.stratocraft.manager.SpecialItemManager getSpecialItemManager() {
        return specialItemManager;
    }

    public me.mami.stratocraft.manager.DifficultyManager getDifficultyManager() {
        return difficultyManager;
    }
    
    public me.mami.stratocraft.manager.DungeonManager getDungeonManager() {
        return dungeonManager;
    }
    
    public me.mami.stratocraft.manager.BiomeManager getBiomeManager() {
        return biomeManager;
    }
    
    public me.mami.stratocraft.manager.BossManager getBossManager() {
        return bossManager;
    }
    
    public me.mami.stratocraft.manager.BossArenaManager getBossArenaManager() {
        return bossArenaManager;
    }
    
    public me.mami.stratocraft.manager.NewBossArenaManager getNewBossArenaManager() {
        return newBossArenaManager;
    }
    
    public me.mami.stratocraft.manager.TamingManager getTamingManager() {
        return tamingManager;
    }
    
    public me.mami.stratocraft.manager.BreedingManager getBreedingManager() {
        return breedingManager;
    }
    
    public me.mami.stratocraft.manager.HUDManager getHUDManager() {
        return hudManager;
    }
    
    /**
     * Dinamik Zorluk Sistemi Başlatma
     * Tüm manager'lar hazır olduktan sonra çağrılmalı
     */
    private void initializeDynamicDifficultySystem() {
        if (configManager == null || disasterManager == null) {
            getLogger().warning("Dinamik Zorluk Sistemi başlatılamadı: ConfigManager veya DisasterManager null!");
            return;
        }
        
        // Config'den güç ayarlarını al
        me.mami.stratocraft.manager.DisasterPowerConfig powerConfig = 
            configManager.getDisasterPowerConfig();
        
        if (powerConfig == null) {
            getLogger().warning("Dinamik Zorluk Sistemi başlatılamadı: DisasterPowerConfig null!");
            return;
        }
        
        // PlayerPowerCalculator oluştur
        me.mami.stratocraft.manager.PlayerPowerCalculator playerPowerCalculator = 
            new me.mami.stratocraft.manager.PlayerPowerCalculator(
                powerConfig,
                clanManager,
                trainingManager,
                buffManager,
                specialItemManager
            );
        
        // ServerPowerCalculator oluştur
        me.mami.stratocraft.manager.ServerPowerCalculator serverPowerCalculator = 
            new me.mami.stratocraft.manager.ServerPowerCalculator(
                playerPowerCalculator,
                powerConfig
            );
        
        // DisasterManager'a bağla
        disasterManager.initializeDynamicDifficulty(
            powerConfig,
            playerPowerCalculator,
            serverPowerCalculator
        );
        
        getLogger().info("Dinamik Zorluk Sistemi başarıyla başlatıldı!");
    }
    
    /**
     * Klan Güç Sistemi Başlatma
     */
    private void initializeClanPowerSystem() {
        if (clanManager == null || trainingManager == null || specialItemManager == null || 
            configManager == null || buffManager == null || territoryManager == null || 
            siegeManager == null) {
            getLogger().warning("Stratocraft Güç Sistemi başlatılamadı: Gerekli yöneticiler null!");
            return;
        }
        
        // StratocraftPowerSystem oluştur (yeni hibrit sistem)
        stratocraftPowerSystem = new me.mami.stratocraft.manager.StratocraftPowerSystem(
            this,
            clanManager,
            trainingManager,
            specialItemManager,
            buffManager,
            territoryManager,
            siegeManager
        );
        
        // Config'den ayarları yükle
        stratocraftPowerSystem.loadConfig(configManager.getConfig());
        
        // Event listener'ı kaydet (Delta sistemi için TerritoryManager gerekli)
        powerSystemListener = new me.mami.stratocraft.listener.PowerSystemListener(
            stratocraftPowerSystem, 
            clanManager, 
            territoryManager
        );
        Bukkit.getPluginManager().registerEvents(powerSystemListener, this);
        
        // ✅ FELAKET SİSTEMİ ENTEGRASYONU: DisasterManager'a yeni güç sistemini bağla
        if (disasterManager != null) {
            disasterManager.setStratocraftPowerSystem(stratocraftPowerSystem);
        }
        
        // ✅ TEST SİSTEMLERİ: Basit sıralama ve geçmiş sistemlerini başlat
        simpleRankingSystem = new me.mami.stratocraft.manager.SimpleRankingSystem(this);
        simplePowerHistory = new me.mami.stratocraft.manager.SimplePowerHistory(this);
        
        getLogger().info("Stratocraft Güç Sistemi başarıyla başlatıldı!");
    }
    
    /**
     * SimpleRankingSystem getter
     */
    public me.mami.stratocraft.manager.SimpleRankingSystem getSimpleRankingSystem() {
        return simpleRankingSystem;
    }
    
    /**
     * SimplePowerHistory getter
     */
    public me.mami.stratocraft.manager.SimplePowerHistory getSimplePowerHistory() {
        return simplePowerHistory;
    }
    
    /**
     * StratocraftPowerSystem getter
     */
    public me.mami.stratocraft.manager.StratocraftPowerSystem getStratocraftPowerSystem() {
        return stratocraftPowerSystem;
    }
    
    // ========== YENİ KLAN SİSTEMLERİ ==========
    private me.mami.stratocraft.manager.clan.ClanProtectionSystem clanProtectionSystem;
    private me.mami.stratocraft.manager.clan.ClanRankSystem clanRankSystem;
    private me.mami.stratocraft.manager.clan.ClanLevelBonusSystem clanLevelBonusSystem;
    private me.mami.stratocraft.manager.clan.ClanActivitySystem clanActivitySystem;
    private me.mami.stratocraft.manager.clan.ClanBankSystem clanBankSystem;
    private me.mami.stratocraft.manager.clan.ClanMissionSystem clanMissionSystem;
    
    /**
     * Yeni Klan Sistemleri Başlatma
     */
    private void initializeClanSystems() {
        if (clanManager == null || configManager == null) {
            getLogger().warning("Klan sistemleri başlatılamadı: Gerekli yöneticiler null!");
            return;
        }
        
        // StratocraftPowerSystem gerekli (ClanProtectionSystem ve ClanLevelBonusSystem için)
        if (stratocraftPowerSystem == null) {
            getLogger().warning("Klan sistemleri başlatılamadı: StratocraftPowerSystem null!");
            return;
        }
        
        // 1. ClanActivitySystem (diğer sistemlerden bağımsız)
        clanActivitySystem = new me.mami.stratocraft.manager.clan.ClanActivitySystem(
            this, clanManager);
        clanActivitySystem.loadConfig(configManager.getConfig());
        
        // 2. ClanRankSystem (ClanActivitySystem'dan bağımsız)
        clanRankSystem = new me.mami.stratocraft.manager.clan.ClanRankSystem(
            this, clanManager);
        
        // 2.5. ClanMemberMenu (ClanRankSystem gerekli)
        clanMemberMenu = new me.mami.stratocraft.gui.ClanMemberMenu(
            this, clanManager, clanRankSystem);
        Bukkit.getPluginManager().registerEvents(clanMemberMenu, this);
        
        // 3. ClanLevelBonusSystem (StratocraftPowerSystem ve BuffManager gerekli)
        clanLevelBonusSystem = new me.mami.stratocraft.manager.clan.ClanLevelBonusSystem(
            this, stratocraftPowerSystem, buffManager);
        clanLevelBonusSystem.loadConfig(configManager.getConfig());
        
        // 4. ClanProtectionSystem (StratocraftPowerSystem, SiegeManager, ClanActivitySystem gerekli)
        clanProtectionSystem = new me.mami.stratocraft.manager.clan.ClanProtectionSystem(
            this, clanManager, stratocraftPowerSystem, siegeManager, clanActivitySystem);
        clanProtectionSystem.loadConfig(configManager.getConfig());
        
        // 5. ClanBankSystem (ClanRankSystem gerekli)
        clanBankSystem = new me.mami.stratocraft.manager.clan.ClanBankSystem(
            this, clanManager, clanRankSystem);
        clanBankSystem.loadConfig(configManager.getConfig());
        
        // 6. ClanMissionSystem (ClanRankSystem gerekli)
        clanMissionSystem = new me.mami.stratocraft.manager.clan.ClanMissionSystem(
            this, clanManager, clanRankSystem);
        clanMissionSystem.loadConfig(configManager.getConfig());
        
        // 7. ClanMissionMenu (GUI menüsü)
        clanMissionMenu = new me.mami.stratocraft.gui.ClanMissionMenu(
            this, clanManager, clanMissionSystem);
        Bukkit.getPluginManager().registerEvents(clanMissionMenu, this);
        
        // YENİ: StructureActivationListener - ClanRankSystem artık hazır, tekrar kaydet
        // Not: onEnable() içinde zaten null ile kaydedilmiş, burada clanRankSystem ile tekrar kaydediyoruz
        Bukkit.getPluginManager().registerEvents(
            new me.mami.stratocraft.listener.StructureActivationListener(
                clanManager, territoryManager, clanRankSystem, structureCoreManager), this);
        
        // 8. ClanStatsMenu (GUI menüsü)
        clanStatsMenu = new me.mami.stratocraft.gui.ClanStatsMenu(this, clanManager);
        Bukkit.getPluginManager().registerEvents(clanStatsMenu, this);
        
        // 9. ContractMenu (GUI menüsü) - ContractManager ve ClanManager gerekli
        if (contractManager != null) {
            contractMenu = new me.mami.stratocraft.gui.ContractMenu(
                this, contractManager, clanManager);
            // Manager'ları set et
            if (contractRequestManager != null && contractTermsManager != null) {
                contractMenu.setManagers(contractRequestManager, contractTermsManager);
                
                // HUDManager'a kontrat manager referanslarını ekle
                if (hudManager != null) {
                    hudManager.setContractManagers(contractRequestManager, contractTermsManager);
                }
            }
            Bukkit.getPluginManager().registerEvents(contractMenu, this);
            
            // 10. PowerMenu (Güç sistemi GUI)
            if (stratocraftPowerSystem != null) {
                powerMenu = new me.mami.stratocraft.gui.PowerMenu(this, stratocraftPowerSystem);
                Bukkit.getPluginManager().registerEvents(powerMenu, this);
            }
            
            // 11. ClanBankMenu (Klan bankası GUI)
            if (clanBankSystem != null) {
                clanBankMenu = new me.mami.stratocraft.gui.ClanBankMenu(this, clanManager, clanBankSystem, clanRankSystem);
                Bukkit.getPluginManager().registerEvents(clanBankMenu, this);
            }
            
            // 12. ClanStructureMenu (Klan yapıları GUI)
            if (stratocraftPowerSystem != null) {
                clanStructureMenu = new me.mami.stratocraft.gui.ClanStructureMenu(
                    this, clanManager, stratocraftPowerSystem);
                Bukkit.getPluginManager().registerEvents(clanStructureMenu, this);
            }
        }
        
        // 13. AllianceMenu (İttifak GUI)
        if (allianceManager != null) {
            allianceMenu = new me.mami.stratocraft.gui.AllianceMenu(this, clanManager, allianceManager);
            Bukkit.getPluginManager().registerEvents(allianceMenu, this);
        }
        
        // ✅ YENİ: 13.5. PeaceRequestMenu (Barış Anlaşması GUI)
        if (peaceRequestManager != null && siegeManager != null) {
            peaceRequestMenu = new me.mami.stratocraft.gui.PeaceRequestMenu(
                this, clanManager, peaceRequestManager, siegeManager);
            Bukkit.getPluginManager().registerEvents(peaceRequestMenu, this);
        }
        
        // 14. CaravanMenu (Kervan GUI)
        if (caravanManager != null && configManager != null && configManager.getGameBalanceConfig() != null) {
            me.mami.stratocraft.manager.GameBalanceConfig balanceConfig = configManager.getGameBalanceConfig();
            caravanMenu = new me.mami.stratocraft.gui.CaravanMenu(
                this, clanManager, caravanManager, balanceConfig);
            Bukkit.getPluginManager().registerEvents(caravanMenu, this);
        }
        
        // 15. TamingMenu (Eğitme GUI)
        if (tamingManager != null) {
            tamingMenu = new me.mami.stratocraft.gui.TamingMenu(this, clanManager, tamingManager);
            Bukkit.getPluginManager().registerEvents(tamingMenu, this);
        }
        
        // 16. BreedingMenu (Üreme GUI)
        if (breedingManager != null && tamingManager != null) {
            breedingMenu = new me.mami.stratocraft.gui.BreedingMenu(
                this, clanManager, tamingManager, breedingManager);
            Bukkit.getPluginManager().registerEvents(breedingMenu, this);
        }
        
        // 17. TrainingMenu (Eğitim GUI)
        if (trainingManager != null) {
            trainingMenu = new me.mami.stratocraft.gui.TrainingMenu(this, trainingManager);
            Bukkit.getPluginManager().registerEvents(trainingMenu, this);
        }
        
        // 18. PersonalTerminalListener (Kişisel Terminal)
        personalTerminalListener = new me.mami.stratocraft.listener.PersonalTerminalListener(this);
        Bukkit.getPluginManager().registerEvents(personalTerminalListener, this);
        
        // ClanManager'a yeni sistemleri bağla (setter injection)
        if (clanManager != null) {
            clanManager.setClanActivitySystem(clanActivitySystem);
            clanManager.setClanBankSystem(clanBankSystem);
            clanManager.setClanMissionSystem(clanMissionSystem);
            // YENİ MODEL: PlayerDataManager zaten yukarıda set edildi, burada tekrar kontrol et
            if (playerDataManager != null && clanManager != null) {
                clanManager.setPlayerDataManager(playerDataManager);
            }
        }
        
        // Event listener'ı kaydet
        me.mami.stratocraft.listener.ClanSystemListener clanSystemListener = 
            new me.mami.stratocraft.listener.ClanSystemListener(this, clanManager);
        clanSystemListener.setProtectionSystem(clanProtectionSystem);
        clanSystemListener.setActivitySystem(clanActivitySystem);
        clanSystemListener.setBankSystem(clanBankSystem);
        clanSystemListener.setMissionSystem(clanMissionSystem);
        Bukkit.getPluginManager().registerEvents(clanSystemListener, this);
        
        // Scheduled task'ları başlat
        startClanSystemTasks();
        
        getLogger().info("Yeni Klan Sistemleri başarıyla başlatıldı!");
    }
    
    /**
     * Klan sistemleri scheduled task'larını başlat
     */
    private void startClanSystemTasks() {
        // Config'den interval'leri al (varsayılan değerler)
        long salaryInterval = 72000L; // 1 saat = 72000 tick (varsayılan)
        long contractInterval = 6000L; // 5 dakika = 6000 tick (varsayılan)
        
        if (configManager != null && configManager.getConfig() != null) {
            // Config'den oku (eğer varsa)
            salaryInterval = configManager.getConfig().getLong(
                "clan.bank-system.salary.task-interval", 72000L);
            contractInterval = configManager.getConfig().getLong(
                "clan.bank-system.contract.task-interval", 6000L);
        }
        
        // Geçersiz değer kontrolleri
        if (salaryInterval < 1200L) salaryInterval = 72000L; // Minimum 1 dakika
        if (contractInterval < 200L) contractInterval = 6000L; // Minimum 10 saniye
        
        // Otomatik maaş dağıtımı (config'den interval)
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            try {
                if (clanBankSystem != null) {
                    clanBankSystem.distributeSalaries();
                }
            } catch (Exception e) {
                getLogger().warning("Maaş dağıtımı hatası: " + e.getMessage());
                e.printStackTrace();
            }
        }, salaryInterval, salaryInterval);
        
        // Otomatik transfer kontratları (config'den interval)
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            try {
                if (clanBankSystem != null) {
                    clanBankSystem.processTransferContracts();
                }
            } catch (Exception e) {
                getLogger().warning("Transfer kontratları işleme hatası: " + e.getMessage());
                e.printStackTrace();
            }
        }, contractInterval, contractInterval);
        
        // Süresi dolmuş görevleri temizle (her 1 saat)
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            try {
                if (clanMissionSystem != null) {
                    clanMissionSystem.cleanupExpiredMissions();
                }
            } catch (Exception e) {
                getLogger().warning("Görev temizleme hatası: " + e.getMessage());
                e.printStackTrace();
            }
        }, 72000L, 72000L); // 1 saat = 72000 tick
        
        // YENİ: Süresi dolmuş kontrat isteklerini temizle (her 1 saat)
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            try {
                if (contractRequestManager != null) {
                    cleanupExpiredContractRequests();
                }
            } catch (Exception e) {
                getLogger().warning("Kontrat isteği temizleme hatası: " + e.getMessage());
                e.printStackTrace();
            }
        }, 72000L, 72000L); // 1 saat = 72000 tick
    }
    
    /**
     * YENİ: Süresi dolmuş kontrat isteklerini temizle
     * İstekler 24 saat sonra otomatik olarak iptal edilir
     */
    private void cleanupExpiredContractRequests() {
        if (contractRequestManager == null) return;
        
        long expireTime = 24 * 60 * 60 * 1000L; // 24 saat (milisaniye)
        long currentTime = System.currentTimeMillis();
        
        java.util.List<me.mami.stratocraft.model.ContractRequest> allRequests = 
            contractRequestManager.getAllRequests();
        
        int cleanedCount = 0;
        for (me.mami.stratocraft.model.ContractRequest request : allRequests) {
            if (request.getStatus() == me.mami.stratocraft.model.ContractRequest.ContractRequestStatus.PENDING) {
                long age = currentTime - request.getCreatedAt();
                if (age > expireTime) {
                    // Süresi dolmuş, iptal et
                    request.setStatus(me.mami.stratocraft.model.ContractRequest.ContractRequestStatus.CANCELLED);
                    contractRequestManager.removeRequest(request.getId());
                    cleanedCount++;
                }
            }
        }
        
        if (cleanedCount > 0) {
            getLogger().info("§a" + cleanedCount + " adet süresi dolmuş kontrat isteği temizlendi.");
        }
    }
    
    // Getters
    public me.mami.stratocraft.manager.clan.ClanProtectionSystem getClanProtectionSystem() {
        return clanProtectionSystem;
    }
    
    public me.mami.stratocraft.manager.clan.ClanRankSystem getClanRankSystem() {
        return clanRankSystem;
    }
    
    public me.mami.stratocraft.manager.clan.ClanLevelBonusSystem getClanLevelBonusSystem() {
        return clanLevelBonusSystem;
    }
    
    public me.mami.stratocraft.manager.clan.ClanActivitySystem getClanActivitySystem() {
        return clanActivitySystem;
    }
    
    public me.mami.stratocraft.manager.clan.ClanBankSystem getClanBankSystem() {
        return clanBankSystem;
    }
    
    public me.mami.stratocraft.manager.clan.ClanMissionSystem getClanMissionSystem() {
        return clanMissionSystem;
    }
    
    /**
     * @deprecated ClanPowerSystem yerine StratocraftPowerSystem kullanın
     */
    @Deprecated
    public me.mami.stratocraft.manager.ClanPowerSystem getClanPowerSystem() {
        return null; // Eski sistem kaldırıldı
    }
    
    /**
     * WorldEdit yüklü mü kontrol et
     * Bu metod sınıfı yüklemeden kontrol yapar (NoClassDefFoundError önleme)
     */
    public static boolean isWorldEditAvailable() {
        try {
            Class.forName("com.sk89q.worldedit.WorldEdit");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}