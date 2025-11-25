package me.mami.stratocraft;

import me.mami.stratocraft.listener.*;
import me.mami.stratocraft.manager.*;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Territory;
import me.mami.stratocraft.task.BuffTask;
import me.mami.stratocraft.task.DisasterTask;
import me.mami.stratocraft.task.MobRideTask;
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
    private CaravanManager caravanManager;
    private ScavengerManager scavengerManager;
    private LogisticsManager logisticsManager;
    private ContractManager contractManager;
    private MobManager mobManager;
    private ShopManager shopManager;
    private ResearchManager researchManager;
    private MissionManager missionManager;

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
        caravanManager = new CaravanManager();
        scavengerManager = new ScavengerManager();
        logisticsManager = new LogisticsManager(territoryManager);
        contractManager = new ContractManager(clanManager);
        mobManager = new MobManager();
        shopManager = new ShopManager();
        researchManager = new ResearchManager();
        missionManager = new MissionManager();

        // 2. Dinleyicileri Kaydet
        Bukkit.getPluginManager().registerEvents(new BatteryListener(batteryManager), this);
        Bukkit.getPluginManager().registerEvents(new CombatListener(clanManager), this);
        Bukkit.getPluginManager().registerEvents(new SurvivalListener(), this);
        Bukkit.getPluginManager().registerEvents(new TerritoryListener(territoryManager, siegeManager), this);
        Bukkit.getPluginManager().registerEvents(new SiegeListener(siegeManager, territoryManager), this);
        Bukkit.getPluginManager().registerEvents(new ScavengerListener(scavengerManager), this);
        Bukkit.getPluginManager().registerEvents(new LogisticsListener(logisticsManager), this);
        Bukkit.getPluginManager().registerEvents(new RitualListener(clanManager), this);
        Bukkit.getPluginManager().registerEvents(new ShopListener(shopManager), this);
        Bukkit.getPluginManager().registerEvents(new MissionListener(missionManager), this);
        Bukkit.getPluginManager().registerEvents(new ResearchListener(), this);
        Bukkit.getPluginManager().registerEvents(new StructureListener(clanManager, researchManager), this);

        // 3. Zamanlayıcıları Başlat
        new BuffTask(territoryManager).runTaskTimer(this, 20L, 20L); 
        new DisasterTask(disasterManager, territoryManager).runTaskTimer(this, 20L, 20L); 
        new MobRideTask(mobManager).runTaskTimer(this, 1L, 1L); 

        // 4. Komutlar
        getCommand("klan").setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                if (sender instanceof Player) {
                    Player p = (Player) sender;
                    if (args.length > 0 && args[0].equalsIgnoreCase("kur")) {
                        if (args.length > 1) {
                            Clan newClan = clanManager.createClan(args[1], p.getUniqueId());
                            if (newClan != null) {
                                newClan.setTerritory(new Territory(newClan.getId(), p.getLocation()));
                            }
                            p.sendMessage("Klan kuruldu: " + args[1]);
                        }
                    }
                }
                return true;
            }
        });

        getLogger().info("Stratocraft v10.0: Eksiksiz Sistem Aktif!");
    }

    public static Main getInstance() { return instance; }
}