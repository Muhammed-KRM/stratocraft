package me.mami.stratocraft.listener;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Structure;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.UUID;

/**
 * Yapı Menü Listener
 * 
 * Yapılara sağ tık ile menü açma sistemi
 */
public class StructureMenuListener implements Listener {
    private final Main plugin;
    private final ClanManager clanManager;
    private final TerritoryManager territoryManager;
    
    // Cooldown: Oyuncu UUID -> Son tıklama zamanı
    private final java.util.Map<UUID, Long> clickCooldowns = new java.util.concurrent.ConcurrentHashMap<>();
    private static final long CLICK_COOLDOWN = 1000L; // 1 saniye
    
    public StructureMenuListener(Main plugin, ClanManager clanManager, TerritoryManager territoryManager) {
        this.plugin = plugin;
        this.clanManager = clanManager;
        this.territoryManager = territoryManager;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onStructureInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        if (event.getHand() != EquipmentSlot.HAND)
            return;
        if (event.getPlayer().isSneaking())
            return; // Shift + Sağ Tık aktivasyon için
        
        Player player = event.getPlayer();
        Block clicked = event.getClickedBlock();
        if (clicked == null)
            return;
        
        // Cooldown kontrolü
        if (isOnCooldown(player.getUniqueId())) {
            return;
        }
        
        // Yapı kontrolü
        Structure structure = findStructureAt(clicked.getLocation());
        if (structure == null)
            return;
        
        event.setCancelled(true);
        setCooldown(player.getUniqueId());
        
        // Yapı tipine göre menü aç
        openMenuForStructure(player, structure);
    }
    
    /**
     * Konumdaki yapıyı bul
     */
    private Structure findStructureAt(org.bukkit.Location location) {
        // Tüm klanları kontrol et
        for (Clan clan : clanManager.getAllClans()) {
            for (Structure structure : clan.getStructures()) {
                if (structure.getLocation().distance(location) <= 2.0) {
                    // Kişisel yapılar herkese açık, klan yapıları klan kontrolü gerektirir
                    Structure.Type type = structure.getType();
                    if (type == Structure.Type.PERSONAL_MISSION_GUILD ||
                        type == Structure.Type.CONTRACT_OFFICE ||
                        type == Structure.Type.MARKET_PLACE ||
                        type == Structure.Type.RECIPE_LIBRARY) {
                        // Kişisel yapılar: Herkese açık
                        return structure;
                    } else {
                        // Klan yapıları: Klan kontrolü yapılacak (openMenuForStructure'da)
                        return structure;
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Yapı tipine göre menü aç
     */
    private void openMenuForStructure(Player player, Structure structure) {
        Structure.Type type = structure.getType();
        
        switch (type) {
            case PERSONAL_MISSION_GUILD:
                // Kişisel Görev Loncası
                if (plugin.getMissionManager() != null) {
                    me.mami.stratocraft.model.Mission mission = plugin.getMissionManager().getActiveMission(player.getUniqueId());
                    if (mission != null) {
                        me.mami.stratocraft.gui.MissionMenu.openMenu(player, mission, plugin.getMissionManager());
                    } else {
                        player.sendMessage("§eAktif göreviniz yok!");
                        player.sendMessage("§7Yeni görev almak için Totem'e sağ tıklayın.");
                    }
                }
                break;
                
            case CLAN_MANAGEMENT_CENTER:
                // Klan Yönetim Merkezi
                Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
                if (clan == null) {
                    player.sendMessage("§cBir klana üye değilsiniz!");
                    return;
                }
                // Klan bölgesinde mi kontrol
                Clan owner = territoryManager.getTerritoryOwner(structure.getLocation());
                if (owner == null || !owner.equals(clan)) {
                    player.sendMessage("§cBu yapıya erişim yetkiniz yok!");
                    return;
                }
                if (plugin.getClanMenu() != null) {
                    plugin.getClanMenu().openMenu(player);
                }
                break;
                
            case CLAN_BANK:
                // Klan Bankası
                clan = clanManager.getClanByPlayer(player.getUniqueId());
                if (clan == null) {
                    player.sendMessage("§cBir klana üye değilsiniz!");
                    return;
                }
                owner = territoryManager.getTerritoryOwner(structure.getLocation());
                if (owner == null || !owner.equals(clan)) {
                    player.sendMessage("§cBu yapıya erişim yetkiniz yok!");
                    return;
                }
                if (plugin.getClanBankMenu() != null) {
                    plugin.getClanBankMenu().openMainMenu(player);
                }
                break;
                
            case CLAN_MISSION_GUILD:
                // Klan Görev Loncası
                clan = clanManager.getClanByPlayer(player.getUniqueId());
                if (clan == null) {
                    player.sendMessage("§cBir klana üye değilsiniz!");
                    return;
                }
                owner = territoryManager.getTerritoryOwner(structure.getLocation());
                if (owner == null || !owner.equals(clan)) {
                    player.sendMessage("§cBu yapıya erişim yetkiniz yok!");
                    return;
                }
                if (plugin.getClanMissionMenu() != null) {
                    plugin.getClanMissionMenu().openMenu(player);
                }
                break;
                
            case TRAINING_ARENA:
                // Eğitim Alanı
                clan = clanManager.getClanByPlayer(player.getUniqueId());
                if (clan == null) {
                    player.sendMessage("§cBir klana üye değilsiniz!");
                    return;
                }
                owner = territoryManager.getTerritoryOwner(structure.getLocation());
                if (owner == null || !owner.equals(clan)) {
                    player.sendMessage("§cBu yapıya erişim yetkiniz yok!");
                    return;
                }
                // Eğitilmiş Canlılar menüsü aç
                if (plugin.getTamingMenu() != null) {
                    plugin.getTamingMenu().openMainMenu(player);
                }
                break;
                
            case CARAVAN_STATION:
                // Kervan İstasyonu
                clan = clanManager.getClanByPlayer(player.getUniqueId());
                if (clan == null) {
                    player.sendMessage("§cBir klana üye değilsiniz!");
                    return;
                }
                owner = territoryManager.getTerritoryOwner(structure.getLocation());
                if (owner == null || !owner.equals(clan)) {
                    player.sendMessage("§cBu yapıya erişim yetkiniz yok!");
                    return;
                }
                if (plugin.getCaravanMenu() != null) {
                    plugin.getCaravanMenu().openMainMenu(player);
                }
                break;
                
            case CONTRACT_OFFICE:
                // Kontrat Bürosu (genel)
                if (plugin.getContractMenu() != null) {
                    plugin.getContractMenu().openMainMenu(player, 0);
                }
                break;
                
            case MARKET_PLACE:
                // Market (genel) - Tüm shopları listele
                if (plugin.getShopManager() != null) {
                    java.util.List<me.mami.stratocraft.model.Shop> shops = plugin.getShopManager().getAllShops();
                    if (shops.isEmpty()) {
                        player.sendMessage("§eHenüz hiç market yok!");
                        player.sendMessage("§7Market kurmak için Chest + Sign kullanın.");
                    } else {
                        player.openInventory(me.mami.stratocraft.gui.ShopMenu.createMarketListMenu(shops, 1));
                    }
                }
                break;
                
            case RECIPE_LIBRARY:
                // Tarif Kütüphanesi (genel) - Tüm tarifleri listele
                player.openInventory(me.mami.stratocraft.gui.RecipeMenu.createRecipeLibraryMenu(player, 1));
                break;
                
            default:
                // Diğer yapı tipleri için menü yok
                break;
        }
        
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Cooldown kontrolü
     */
    private boolean isOnCooldown(UUID playerId) {
        if (!clickCooldowns.containsKey(playerId))
            return false;
        
        long lastTime = clickCooldowns.get(playerId);
        long currentTime = System.currentTimeMillis();
        
        return (currentTime - lastTime) < CLICK_COOLDOWN;
    }
    
    /**
     * Cooldown ayarla
     */
    private void setCooldown(UUID playerId) {
        clickCooldowns.put(playerId, System.currentTimeMillis());
    }
}

