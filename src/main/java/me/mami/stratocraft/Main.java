package me.mami.stratocraft;

import me.mami.stratocraft.listener.*;
import me.mami.stratocraft.manager.*;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Territory;
import me.mami.stratocraft.task.BuffTask;
import me.mami.stratocraft.task.DisasterTask;
import me.mami.stratocraft.task.MobRideTask;
import me.mami.stratocraft.task.DrillTask;
import me.mami.stratocraft.task.CropTask;
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
    private ItemManager itemManager;
    private DisasterManager disasterManager;
    private SiegeManager siegeManager;
    @SuppressWarnings("unused")
    private CaravanManager caravanManager;
    private ScavengerManager scavengerManager;
    private LogisticsManager logisticsManager;
    private ContractManager contractManager;
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

    @Override
    public void onEnable() {
        instance = this;
        
        // Klasör Hazırlığı
        File schemDir = new File(getDataFolder(), "schematics");
        if (!schemDir.exists()) schemDir.mkdirs();

        // 1. Yöneticileri Başlat
        itemManager = new ItemManager(); itemManager.init(); 
        clanManager = new ClanManager();
        territoryManager = new TerritoryManager(clanManager);
        batteryManager = new BatteryManager();
        siegeManager = new SiegeManager();
        disasterManager = new DisasterManager();
        batteryManager.setDisasterManager(disasterManager);
        caravanManager = new CaravanManager();
        scavengerManager = new ScavengerManager();
        logisticsManager = new LogisticsManager(territoryManager);
        contractManager = new ContractManager(clanManager);
        mobManager = new MobManager();
        shopManager = new ShopManager();
        researchManager = new ResearchManager();
        missionManager = new MissionManager();
        ghostRecipeManager = new me.mami.stratocraft.manager.GhostRecipeManager();
        trainingManager = new me.mami.stratocraft.manager.TrainingManager();
        buffManager = new BuffManager();
        buffManager.setPlugin(this);
        dataManager = new DataManager(this);
        configManager = new ConfigManager(this);
        langManager = new me.mami.stratocraft.util.LangManager(this);
        clanMenu = new me.mami.stratocraft.gui.ClanMenu(clanManager);
        economyManager = new me.mami.stratocraft.manager.EconomyManager();

        // Manager bağlantıları
        siegeManager.setBuffManager(buffManager);
        disasterManager.setBuffManager(buffManager);
        disasterManager.setTerritoryManager(territoryManager);

        // 2. Dinleyicileri Kaydet
        BatteryListener batteryListener = new BatteryListener(batteryManager, territoryManager, researchManager);
        batteryListener.setTrainingManager(trainingManager);
        Bukkit.getPluginManager().registerEvents(batteryListener, this);
        Bukkit.getPluginManager().registerEvents(new CombatListener(clanManager), this);
        Bukkit.getPluginManager().registerEvents(new SurvivalListener(missionManager), this);
        Bukkit.getPluginManager().registerEvents(new TerritoryListener(territoryManager, siegeManager), this);
        Bukkit.getPluginManager().registerEvents(new SiegeListener(siegeManager, territoryManager), this);
        Bukkit.getPluginManager().registerEvents(new ScavengerListener(scavengerManager), this);
        Bukkit.getPluginManager().registerEvents(new LogisticsListener(logisticsManager), this);
        // RitualListener artık RitualInteractionListener içinde birleştirildi
        Bukkit.getPluginManager().registerEvents(new RitualInteractionListener(clanManager, territoryManager), this);
        Bukkit.getPluginManager().registerEvents(new ShopListener(shopManager), this);
        Bukkit.getPluginManager().registerEvents(new MissionListener(missionManager), this);
        Bukkit.getPluginManager().registerEvents(new ResearchListener(researchManager), this);
        Bukkit.getPluginManager().registerEvents(new StructureListener(clanManager, researchManager), this);
        Bukkit.getPluginManager().registerEvents(new me.mami.stratocraft.listener.GhostRecipeListener(ghostRecipeManager, researchManager), this);
        Bukkit.getPluginManager().registerEvents(new ConsumableListener(), this);
        Bukkit.getPluginManager().registerEvents(new VillagerListener(), this);
        Bukkit.getPluginManager().registerEvents(new GriefProtectionListener(territoryManager), this);
        virtualStorageListener = new VirtualStorageListener(territoryManager);
        Bukkit.getPluginManager().registerEvents(virtualStorageListener, this);
        Bukkit.getPluginManager().registerEvents(new me.mami.stratocraft.listener.ClanChatListener(clanManager), this);
        // ClanMenu zaten Listener implement ediyor, kaydet
        Bukkit.getPluginManager().registerEvents(clanMenu, this);
        
        // CombatLogManager kaydet (savaştan kaçmayı engellemek için)
        combatLogManager = new me.mami.stratocraft.manager.CombatLogManager();
        Bukkit.getPluginManager().registerEvents(combatLogManager, this);
        combatLogManager.startCleanupTask(this);
        
        // Veri yükleme
        dataManager.loadAll(clanManager, contractManager, shopManager, virtualStorageListener);

        // 3. Zamanlayıcıları Başlat
        new BuffTask(territoryManager).runTaskTimer(this, 20L, 20L); 
        new DisasterTask(disasterManager, territoryManager).runTaskTimer(this, 20L, 20L); 
        new MobRideTask(mobManager).runTaskTimer(this, 1L, 1L);
        new DrillTask(territoryManager).runTaskTimer(this, configManager.getDrillInterval(), configManager.getDrillInterval());
        new CropTask(territoryManager).runTaskTimer(this, 40L, 40L); // Her 2 saniye 

        // 4. Tab Completers
        getCommand("klan").setTabCompleter(new me.mami.stratocraft.command.ClanTabCompleter());
        getCommand("kontrat").setTabCompleter(new me.mami.stratocraft.command.ContractTabCompleter());
        
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
                    p.sendMessage("§cDiğer işlemler için Ritüelleri kullanmalısın!");
                    p.sendMessage("§7Ritüeller: Klan Kurma (3x3 Cobblestone + Crafting Table + Named Paper)");
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
                        if (clan.getRank(p.getUniqueId()) != Clan.Rank.LEADER && clan.getRank(p.getUniqueId()) != Clan.Rank.GENERAL) {
                            p.sendMessage("§cBu işlem için yetkiniz yok!");
                            return true;
                        }
                        if (clan.getTerritory() == null) {
                            Territory newTerritory = new Territory(clan.getId(), p.getLocation());
                            clan.setTerritory(newTerritory);
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
                                " → " + contract.getReward() + " altın (ID: " + contract.getId().toString().substring(0, 8) + ")");
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
                            
                            if (contract.getAcceptor() == null) {
                                contractManager.acceptContract(contractId, p.getUniqueId());
                                p.sendMessage("§aSözleşmeyi kabul ettiniz!");
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

        getLogger().info("Stratocraft v10.0: Eksiksiz Sistem Aktif!");
    }
    
    @Override
    public void onDisable() {
        // Veri kaydetme (Async veya Sync)
        if (dataManager != null && clanManager != null && contractManager != null && 
            shopManager != null && virtualStorageListener != null) {
            if (configManager != null && configManager.isAsyncSaving()) {
                // Async kayıt
                Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                    dataManager.saveAll(clanManager, contractManager, shopManager, virtualStorageListener);
                    getLogger().info("Stratocraft: Veriler asenkron olarak kaydedildi.");
                });
            } else {
                // Sync kayıt
                dataManager.saveAll(clanManager, contractManager, shopManager, virtualStorageListener);
                getLogger().info("Stratocraft: Veriler kaydedildi.");
            }
        }
        getLogger().info("Stratocraft: Plugin kapatılıyor.");
    }
    
    public ConfigManager getConfigManager() { return configManager; }
    public me.mami.stratocraft.util.LangManager getLangManager() { return langManager; }

    public static Main getInstance() { return instance; }
    
    // Getter metodları
    public DisasterManager getDisasterManager() { return disasterManager; }
    public ClanManager getClanManager() { return clanManager; }
    public ScavengerManager getScavengerManager() { return scavengerManager; }
    public BuffManager getBuffManager() { return buffManager; }
    public me.mami.stratocraft.manager.EconomyManager getEconomyManager() { return economyManager; }
    public TerritoryManager getTerritoryManager() { return territoryManager; }
    public ContractManager getContractManager() { return contractManager; }
    public ShopManager getShopManager() { return shopManager; }
    public ResearchManager getResearchManager() { return researchManager; }
    public me.mami.stratocraft.manager.GhostRecipeManager getGhostRecipeManager() { return ghostRecipeManager; }
    public me.mami.stratocraft.manager.TrainingManager getTrainingManager() { return trainingManager; }
    public me.mami.stratocraft.gui.ClanMenu getClanMenu() { return clanMenu; }
    public BatteryManager getBatteryManager() { return batteryManager; }
    public SiegeManager getSiegeManager() { return siegeManager; }
    public MissionManager getMissionManager() { return missionManager; }
}