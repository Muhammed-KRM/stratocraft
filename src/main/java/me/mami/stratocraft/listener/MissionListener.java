package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.MissionManager;
import me.mami.stratocraft.model.Mission;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MissionListener implements Listener {
    private final MissionManager missionManager;
    
    // Lokasyon ziyareti için takip (her oyuncu için son konum)
    private final Map<UUID, Location> lastLocations = new HashMap<>();

    public MissionListener(MissionManager mm) { 
        this.missionManager = mm; 
    }

    @EventHandler
    public void onMobKill(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            missionManager.handleKill(event.getEntity().getKiller(), event.getEntityType());
        }
    }
    
    /**
     * Oyuncu Öldürme Takibi
     */
    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer == null) return;
        missionManager.handlePlayerKill(killer, victim);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onTotemInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.LODESTONE) {
            Block base = event.getClickedBlock().getRelative(0, -1, 0);
            if (base.getType() == Material.COBBLESTONE || base.getType() == Material.IRON_BLOCK || base.getType() == Material.DIAMOND_BLOCK) {
                event.setCancelled(true); // GUI menü açılacak
                missionManager.interactWithTotem(event.getPlayer(), base.getType());
            }
        }
    }
    
    /**
     * Lokasyon Ziyareti ve Mesafe Takibi
     * PERFORMANS OPTİMİZASYONU: Sadece blok değiştiyse çalış
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        // PERFORMANS FİLTRESİ: Sadece blok değiştiyse çalış (X, Y, Z kontrolü)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return; // Oyuncu sadece kafasını çevirmiş, işlem yapma
        }
        
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;
        
        // Aktif görev yoksa işlem yapma
        if (missionManager.getActiveMission(player.getUniqueId()) == null) return;
        
        // Mesafe kat etme takibi (her blok değişiminde)
        double distance = from.distance(to);
        if (distance > 0.1) { // Gerçek hareket
            missionManager.handleTravel(player, distance);
        }
        
        // Lokasyon ziyareti takibi (10 bloktan fazla hareket ettiyse)
        Location lastLoc = lastLocations.get(player.getUniqueId());
        if (lastLoc == null || lastLoc.distance(to) >= 10) { // 10 bloktan fazla hareket ettiyse
            lastLocations.put(player.getUniqueId(), to);
            missionManager.handleLocationVisit(player, to);
        }
    }
    
    /**
     * Yapı İnşa Takibi
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onStructureBuild(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Material placed = event.getBlockPlaced().getType();
        
        // Yapı pattern kontrolü (StructureActivationListener'dan)
        // Eğer yapı aktive edildiyse, MissionManager'a bildir
        // Şimdilik basit kontrol - StructureManager ile entegre edilebilir
        missionManager.handleStructureBuild(player, event.getBlockPlaced().getLocation());
    }
    
    /**
     * Item Craft Takibi
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        Material crafted = event.getRecipe().getResult().getType();
        missionManager.handleCraft(player, crafted);
    }
    
    /**
     * Blok Kazma Takibi
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material mined = event.getBlock().getType();
        missionManager.handleMine(player, mined);
    }
    
    /**
     * GUI Menü Tıklama İşlemleri
     */
    @EventHandler
    public void onMissionMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        if (title.equals("§eGörev Menüsü")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;
            
            Mission mission = missionManager.getActiveMission(player.getUniqueId());
            if (mission == null) return;
            
            // Tamamlandıysa teslim et butonu
            if (clicked.getType() == Material.EMERALD_BLOCK && 
                clicked.getItemMeta() != null && 
                clicked.getItemMeta().getDisplayName().equals("§a[Teslim Et]")) {
                if (mission.isCompleted()) {
                    // Ödül ver
                    if (mission.getReward() != null) {
                        player.getInventory().addItem(mission.getReward());
                    }
                    if (mission.getRewardMoney() > 0) {
                        // EconomyManager kullan
                        me.mami.stratocraft.Main plugin = me.mami.stratocraft.Main.getInstance();
                        if (plugin != null && plugin.getEconomyManager() != null) {
                            plugin.getEconomyManager().depositPlayer(player, mission.getRewardMoney());
                        }
                    }
                    player.sendMessage("§a[LONCA] Görev Tamamlandı! Ödülünü aldın.");
                    missionManager.removeMission(player.getUniqueId());
                    player.closeInventory();
                }
            } else if (clicked.getType() == Material.BARRIER && 
                       clicked.getItemMeta() != null && 
                       clicked.getItemMeta().getDisplayName().equals("§cKapat")) {
                player.closeInventory();
            }
        }
    }
}

