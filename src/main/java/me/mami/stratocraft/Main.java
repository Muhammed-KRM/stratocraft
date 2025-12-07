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
    private DataManager dataManager;
    private VirtualStorageListener virtualStorageListener;
    private ConfigManager configManager;
    private me.mami.stratocraft.util.LangManager langManager;
    private me.mami.stratocraft.gui.ClanMenu clanMenu;
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
    private me.mami.stratocraft.manager.HUDManager hudManager;
    
    public me.mami.stratocraft.listener.SpecialWeaponListener getSpecialWeaponListener() {
        return specialWeaponListener;
    }

    @Override
    public void onEnable() {
        instance = this;

        // Klasör Hazırlığı
        File schemDir = new File(getDataFolder(), "schematics");
        if (!schemDir.exists())
            schemDir.mkdirs();
        
        // Şema klasörlerini otomatik oluştur
        // Schematic klasörlerini oluştur (hata durumunda plugin yine de açılsın)
        try {
            me.mami.stratocraft.manager.StructureBuilder.createSchematicDirectories();
        } catch (Exception e) {
            getLogger().warning("§cSchematic klasörleri oluşturulurken hata: " + e.getMessage());
            e.printStackTrace();
        }

        // 1. Yöneticileri Başlat
        // Önce SpecialItemManager'ı oluştur (ItemManager buna ihtiyaç duyuyor)
        specialItemManager = new me.mami.stratocraft.manager.SpecialItemManager();
        
        // Sonra ItemManager'ı başlat ve init() çağır (kanca item'larını oluşturur)
        itemManager = new ItemManager();
        itemManager.init(); // Artık SpecialItemManager'a erişebilir
        
        // Şimdi recipe'leri kaydet (ItemManager.RUSTY_HOOK vb. artık null değil)
        specialItemManager.registerRecipes();
        clanManager = new ClanManager();
        territoryManager = new TerritoryManager(clanManager);
        clanManager.setTerritoryManager(territoryManager); // Cache güncellemesi için
        batteryManager = new BatteryManager(this);
        newBatteryManager = new NewBatteryManager(this); // Yeni batarya sistemi
        siegeManager = new SiegeManager();
        disasterManager = new DisasterManager(this);
        batteryManager.setDisasterManager(disasterManager);
        batteryManager.setTerritoryManager(territoryManager);
        batteryManager.setSiegeManager(siegeManager);
        caravanManager = new CaravanManager();
        scavengerManager = new ScavengerManager();
        logisticsManager = new LogisticsManager(territoryManager);
        contractManager = new ContractManager(clanManager);
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
        
        // MissionManager'ı DifficultyManager ile başlat
        missionManager = new me.mami.stratocraft.manager.MissionManager(difficultyManager, this);

        // Manager bağlantıları
        siegeManager.setBuffManager(buffManager);
        disasterManager.setBuffManager(buffManager);
        disasterManager.setTerritoryManager(territoryManager);

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
        Bukkit.getPluginManager().registerEvents(new TerritoryListener(territoryManager, siegeManager), this);
        Bukkit.getPluginManager().registerEvents(new StructureActivationListener(clanManager, territoryManager), this); // YAPI
                                                                                                                        // AKTİVASYONU
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
        trapManager = new me.mami.stratocraft.manager.TrapManager(this);
        Bukkit.getPluginManager().registerEvents(new me.mami.stratocraft.listener.TrapListener(trapManager), this);
        
        // Yeni Mayın Sistemi (eski sistem kaldırıldı)
        newMineManager = new me.mami.stratocraft.manager.NewMineManager(this);
        // 25 mayın için hayalet tariflerini ekle (ghostRecipeManager oluşturulduktan sonra)
        ghostRecipeManager.initializeMineRecipes(newMineManager);
        Bukkit.getPluginManager().registerEvents(new me.mami.stratocraft.listener.NewMineListener(newMineManager), this);

        // SpecialItemManager zaten yukarıda başlatıldı (ItemManager için gerekli)
        Bukkit.getPluginManager()
                .registerEvents(new me.mami.stratocraft.listener.SpecialItemListener(specialItemManager), this);

        supplyDropManager = new me.mami.stratocraft.manager.SupplyDropManager(this);
        Bukkit.getPluginManager().registerEvents(new me.mami.stratocraft.listener.SupplyDropListener(supplyDropManager),
                this);
        
        // Mob Drop Listener - Özel mob drop sistemi
        Bukkit.getPluginManager().registerEvents(new me.mami.stratocraft.listener.MobDropListener(), this);
        
        // Seviyeli Silah ve Zırh Listener
        Bukkit.getPluginManager().registerEvents(new me.mami.stratocraft.listener.WeaponArmorListener(), this);

        // Veri yükleme
        dataManager.loadAll(clanManager, contractManager, shopManager, virtualStorageListener, allianceManager, disasterManager);
        
        // HUD Manager'ı başlat (missionManager'dan sonra)
        if (hudManager != null && missionManager != null) {
            hudManager.setManagers(disasterManager, newBatteryManager, shopManager, missionManager, 
                                  contractManager, buffManager, clanManager, territoryManager);
            hudManager.start();
        }

        // 3. Zamanlayıcıları Başlat
        new BuffTask(territoryManager, siegeWeaponManager).runTaskTimer(this, 20L, 20L);
        new DisasterTask(disasterManager, territoryManager).runTaskTimer(this, 20L, 20L);
        
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
            }
            
            @org.bukkit.event.EventHandler
            public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
                if (hudManager != null) {
                    hudManager.onPlayerQuit(event.getPlayer());
                }
            }
        }, this);
        
        new me.mami.stratocraft.task.StructureEffectTask(clanManager).runTaskTimer(this, 20L, 20L); // YAPI EFEKTLERİ

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
                        org.bukkit.util.RayTraceResult result = player.rayTraceEntities(50);
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
        }.runTaskTimer(this, 0L, 20L); // Her 20 tickte bir (1 saniye) - ÖNCEKİ: 5 tick (0.25 saniye)
        new MobRideTask(mobManager).runTaskTimer(this, 5L, 5L); // Performans için 5 tick (0.25 saniye)
        new DrillTask(territoryManager).runTaskTimer(this, configManager.getDrillInterval(),
                configManager.getDrillInterval());
        new CropTask(territoryManager).runTaskTimer(this, 40L, 40L); // Her 2 saniye

        // 4. Tab Completers
        getCommand("klan").setTabCompleter(new me.mami.stratocraft.command.ClanTabCompleter());
        getCommand("kontrat").setTabCompleter(new me.mami.stratocraft.command.ContractTabCompleter());
        
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
                } else {
                    p.sendMessage("§eKullanım: /klan <menü|bilgi|kur|ayril|kristal>");
                    p.sendMessage("§7Oyuncular: menü, bilgi");
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
                        p.sendMessage("§eAktif Sözleşmeler:");
                        int i = 1;
                        for (me.mami.stratocraft.model.Contract contract : contractManager.getContracts()) {
                            p.sendMessage("§7" + i + ". " + contract.getMaterial() + " x" + contract.getAmount() +
                                    " → " + contract.getReward() + " altın (ID: "
                                    + contract.getId().toString().substring(0, 8) + ")");
                            i++;
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
        // HUD Manager'ı durdur
        if (hudManager != null) {
            hudManager.stop();
        }
        
        // Batarya sistemini temizle (geçici barrier bloklarını kaldır)
        if (batteryManager != null) {
            batteryManager.shutdown();
            getLogger().info("Stratocraft: Batarya sistemi temizlendi (geçici bloklar kaldırıldı).");
        }

        // Tuzakları kaydet
        if (trapManager != null) {
            trapManager.saveTraps();
            getLogger().info("Stratocraft: Tuzaklar kaydedildi.");
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
            // Kapanış işlemlerinde her zaman senkron kayıt
            dataManager.saveAll(clanManager, contractManager, shopManager, virtualStorageListener, allianceManager, disasterManager, true);
            getLogger().info("Stratocraft: Veriler kaydedildi.");
        }

        // Tuzakları da kaydet (eğer trapManager varsa)
        if (trapManager != null) {
            trapManager.saveTraps();
        }
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

    public BatteryManager getBatteryManager() {
        return batteryManager;
    }
    
    public NewBatteryManager getNewBatteryManager() {
        return newBatteryManager;
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
}