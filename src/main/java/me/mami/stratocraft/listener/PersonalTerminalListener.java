package me.mami.stratocraft.listener;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.gui.*;
import me.mami.stratocraft.manager.ItemManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Kişisel Yönetim Terminali ve Özel Item Listener
 * 
 * Özellikler:
 * - Personal Terminal item'ına sağ tık ile menü açma
 * - Kişisel menülere erişim (Güç, Eğitim, Canlılar, Görevler, Kontratlar, Üreme)
 * - Kontrat Kağıdı item'ına sağ tık ile kontrat menüsü açma
 */
public class PersonalTerminalListener implements Listener {
    private final Main plugin;
    
    public PersonalTerminalListener(Main plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onTerminalClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && 
            event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null) return;
        
        // Personal Terminal kontrolü
        if (ItemManager.isCustomItem(item, "PERSONAL_TERMINAL")) {
            event.setCancelled(true);
            openMainMenu(player);
            return;
        }
        
        // Kontrat Kağıdı kontrolü
        if (ItemManager.isCustomItem(item, "CONTRACT_PAPER")) {
            event.setCancelled(true);
            if (plugin.getContractMenu() != null) {
                plugin.getContractMenu().openMainMenu(player, 0);
            } else {
                player.sendMessage("§cKontrat sistemi aktif değil!");
            }
            return;
        }
    }
    
    /**
     * Ana terminal menüsünü aç
     */
    private void openMainMenu(Player player) {
        if (player == null) return;
        
        org.bukkit.inventory.Inventory menu = org.bukkit.Bukkit.createInventory(null, 27, "§e§lKişisel Yönetim Terminali");
        
        // Güç Menüsü (Slot 10)
        menu.setItem(10, createButton(Material.DIAMOND, "§e§lGüç Sistemi", 
            java.util.Arrays.asList("§7Güç bilgilerinizi görüntüleyin",
                "§7Oyuncu ve klan gücü")));
        
        // Eğitim İlerlemesi (Slot 12)
        menu.setItem(12, createButton(Material.EXPERIENCE_BOTTLE, "§a§lEğitim İlerlemesi", 
            java.util.Arrays.asList("§7Ritüel/batarya antrenman durumu",
                "§7Mastery seviyeleri")));
        
        // Eğitilmiş Canlılar (Slot 14)
        menu.setItem(14, createButton(Material.SPAWNER, "§d§lEğitilmiş Canlılar", 
            java.util.Arrays.asList("§7Eğittiğiniz canlıları yönetin",
                "§7Canlı detayları ve yönetimi")));
        
        // Görevler (Slot 16)
        menu.setItem(16, createButton(Material.TOTEM_OF_UNDYING, "§6§lKişisel Görevler", 
            java.util.Arrays.asList("§7Aktif görevlerinizi görüntüleyin",
                "§7Görev ilerlemesi")));
        
        // Kontratlar (Slot 20)
        menu.setItem(20, createButton(Material.PAPER, "§b§lKişisel Kontratlar", 
            java.util.Arrays.asList("§7Kontratlarınızı görüntüleyin",
                "§7Kabul ettiğiniz kontratlar")));
        
        // Üreme Yönetimi (Slot 22)
        menu.setItem(22, createButton(Material.GOLDEN_APPLE, "§5§lÜreme Yönetimi", 
            java.util.Arrays.asList("§7Üreme çiftlerinizi yönetin",
                "§7Üreme ilerlemesi")));
        
        // Bilgi (Slot 4)
        menu.setItem(4, createButton(Material.BOOK, "§eBilgi", 
            java.util.Arrays.asList("§7Kişisel Yönetim Terminali",
                "§7Tüm kişisel işlemlerinizi buradan yönetebilirsiniz")));
        
        // Kapat (Slot 26)
        menu.setItem(26, createButton(Material.BARRIER, "§cKapat", 
            java.util.Arrays.asList("§7Menüyü kapat")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        
        if (!title.equals("§e§lKişisel Yönetim Terminali")) {
            return;
        }
        
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        int slot = event.getSlot();
        
        switch (slot) {
            case 10: // Güç Menüsü
                if (plugin.getPowerMenu() != null) {
                    plugin.getPowerMenu().openMainMenu(player);
                } else {
                    player.sendMessage("§cGüç sistemi aktif değil!");
                }
                break;
                
            case 12: // Eğitim İlerlemesi
                if (plugin.getTrainingMenu() != null) {
                    plugin.getTrainingMenu().openMainMenu(player);
                } else {
                    player.sendMessage("§cEğitim sistemi aktif değil!");
                }
                break;
                
            case 14: // Eğitilmiş Canlılar
                if (plugin.getTamingMenu() != null) {
                    // Kişisel modda aç (sadece oyuncunun canlıları)
                    plugin.getTamingMenu().openMainMenu(player, true);
                } else {
                    player.sendMessage("§cEğitme sistemi aktif değil!");
                }
                break;
                
            case 16: // Kişisel Görevler
                if (plugin.getMissionManager() != null) {
                    me.mami.stratocraft.model.Mission mission = plugin.getMissionManager().getActiveMission(player.getUniqueId());
                    if (mission != null) {
                        me.mami.stratocraft.gui.MissionMenu.openMenu(player, mission, plugin.getMissionManager());
                    } else {
                        player.sendMessage("§eAktif göreviniz yok!");
                        player.sendMessage("§7Görev almak için Görev Loncası'na gidin.");
                    }
                } else {
                    player.sendMessage("§cGörev sistemi aktif değil!");
                }
                break;
                
            case 20: // Kişisel Kontratlar
                if (plugin.getContractMenu() != null) {
                    // Sadece oyuncunun kontratlarını göster (sayfa 0)
                    plugin.getContractMenu().openMainMenu(player, 0);
                } else {
                    player.sendMessage("§cKontrat sistemi aktif değil!");
                }
                break;
                
            case 22: // Üreme Yönetimi
                if (plugin.getBreedingMenu() != null) {
                    plugin.getBreedingMenu().openMainMenu(player);
                } else {
                    player.sendMessage("§cÜreme sistemi aktif değil!");
                }
                break;
                
            case 26: // Kapat
                player.closeInventory();
                break;
        }
    }
    
    /**
     * Buton oluştur
     */
    private ItemStack createButton(Material material, String name, java.util.List<String> lore) {
        ItemStack item = new ItemStack(material);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null && !lore.isEmpty()) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}

