package me.mami.stratocraft.gui;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.AllianceManager;
import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.model.Alliance;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.util.AllianceHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * İttifak GUI Menüsü
 * 
 * Özellikler:
 * - Aktif ittifakları listeleme
 * - İttifak detayları
 * - İttifak yönetimi (iptal, yenileme)
 * - İttifak bonusları gösterimi
 */
public class AllianceMenu implements Listener {
    private final Main plugin;
    private final ClanManager clanManager;
    private final AllianceManager allianceManager;
    
    // Açık detay menüleri (player -> alliance)
    private final java.util.Map<UUID, Alliance> openDetailMenus = new java.util.concurrent.ConcurrentHashMap<>();
    
    public AllianceMenu(Main plugin, ClanManager clanManager, AllianceManager allianceManager) {
        this.plugin = plugin;
        this.clanManager = clanManager;
        this.allianceManager = allianceManager;
    }
    
    /**
     * Ana ittifak menüsünü aç
     */
    public void openMainMenu(Player player) {
        if (player == null) return;
        
        // Manager null kontrolleri
        if (clanManager == null) {
            player.sendMessage("§cKlan sistemi aktif değil!");
            plugin.getLogger().warning("ClanManager null! Menü açılamıyor.");
            return;
        }
        
        if (allianceManager == null) {
            player.sendMessage("§cİttifak sistemi aktif değil!");
            plugin.getLogger().warning("AllianceManager null! Menü açılamıyor.");
            return;
        }
        
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage("§cBir klana üye değilsiniz!");
            return;
        }
        
        List<Alliance> alliances = allianceManager.getAlliances(clan.getId());
        if (alliances == null) {
            alliances = new ArrayList<>();
        }
        
        // 54 slotlu menü (6x9)
        Inventory menu = Bukkit.createInventory(null, 54, "§6Klan İttifakları");
        
        // İttifakları listele (45 slot - 0-44)
        int slot = 0;
        for (Alliance alliance : alliances) {
            if (alliance == null || slot >= 45) break;
            
            // Diğer klanı bul
            UUID otherClanId = alliance.getOtherClan(clan.getId());
            Clan otherClan = clanManager.getAllClans().stream()
                .filter(c -> c.getId().equals(otherClanId))
                .findFirst().orElse(null);
            
            String otherClanName = otherClan != null ? otherClan.getName() : "Bilinmeyen Klan";
            Material icon = AllianceHelper.getAllianceTypeIcon(alliance.getType());
            String typeName = AllianceHelper.getAllianceTypeDisplayName(alliance.getType());
            String remainingTime = AllianceHelper.getRemainingTime(alliance);
            
            // Lore oluştur
            List<String> lore = new ArrayList<>();
            lore.add("§7═══════════════════════");
            lore.add("§7İttifak Tipi: §e" + typeName);
            lore.add("§7Diğer Klan: §e" + otherClanName);
            lore.add("§7Kalan Süre: " + remainingTime);
            lore.add("§7Durum: " + (alliance.isActive() ? "§aAktif" : "§cPasif"));
            lore.add("§7═══════════════════════");
            lore.add("§aSol Tık: §7Detayları gör");
            if (AllianceHelper.canDissolveAlliance(alliance, clan, player)) {
                lore.add("§cSağ Tık: §7İttifakı sonlandır");
            }
            
            ItemStack item = new ItemStack(icon);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§e" + otherClanName);
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            
            menu.setItem(slot++, item);
        }
        
        // ✅ YENİ: İttifak isteği gönderme butonu (sadece Lider/General)
        Clan.Rank playerRank = clan.getRank(player.getUniqueId());
        if (playerRank == Clan.Rank.LEADER || playerRank == Clan.Rank.GENERAL) {
            menu.setItem(49, createButton(Material.DIAMOND, "§a§lİTTİFAK İSTEĞİ GÖNDER", 
                Arrays.asList("§7Diğer klanlara ittifak isteği gönder",
                    "§7Sol tık: İstek gönderme menüsü",
                    "§7Sağ tık: Fiziksel ritüel (Shift + Elmas)")));
        }
        
        // Bilgi butonu
        menu.setItem(45, createButton(Material.BOOK, "§eBilgi", 
            Arrays.asList("§7Toplam İttifak: §e" + alliances.size(),
                "§7İttifak kurmak için fiziksel ritüel",
                "§7gerekli (Shift + Elmas)")));
        
        // Geri butonu
        menu.setItem(53, createButton(Material.ARROW, "§7Geri", Arrays.asList("§7Ana klan menüsüne dön")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * İttifak detay menüsünü aç
     */
    public void openAllianceDetailMenu(Player player, Alliance alliance) {
        if (player == null || alliance == null) return;
        
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage("§cBir klana üye değilsiniz!");
            return;
        }
        
        // İttifak klanı içeriyor mu kontrolü
        if (!alliance.involvesClan(clan.getId())) {
            player.sendMessage("§cBu ittifak klanınızla ilgili değil!");
            openMainMenu(player);
            return;
        }
        
        // Diğer klanı bul
        UUID otherClanId = alliance.getOtherClan(clan.getId());
        Clan otherClan = clanManager.getAllClans().stream()
            .filter(c -> c.getId().equals(otherClanId))
            .findFirst().orElse(null);
        
        String otherClanName = otherClan != null ? otherClan.getName() : "Bilinmeyen Klan";
        
        Inventory menu = Bukkit.createInventory(null, 54, "§6İttifak Detayları");
        
        // İttifak bilgileri
        Material icon = AllianceHelper.getAllianceTypeIcon(alliance.getType());
        String typeName = AllianceHelper.getAllianceTypeDisplayName(alliance.getType());
        List<String> typeDescription = AllianceHelper.getAllianceTypeDescription(alliance.getType());
        List<String> bonuses = AllianceHelper.getAllianceBonuses(alliance);
        String remainingTime = AllianceHelper.getRemainingTime(alliance);
        
        // Lore oluştur
        List<String> lore = new ArrayList<>();
        lore.add("§7═══════════════════════");
        lore.add("§7İttifak Tipi: §e" + typeName);
        lore.add("§7Diğer Klan: §e" + otherClanName);
        lore.add("§7Kalan Süre: " + remainingTime);
        lore.add("§7Durum: " + (alliance.isActive() ? "§aAktif" : "§cPasif"));
        lore.add("§7═══════════════════════");
        lore.addAll(typeDescription);
        lore.add("§7═══════════════════════");
        lore.addAll(bonuses);
        
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e§l" + typeName);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        menu.setItem(13, item);
        
        // İttifak sonlandırma butonu (sadece Lider ve aktif ittifak)
        if (AllianceHelper.canDissolveAlliance(alliance, clan, player)) {
            menu.setItem(31, createButton(Material.RED_CONCRETE, "§c§lİTTİFAKI SONLANDIR", 
                Arrays.asList("§7İttifakı karşılıklı olarak sonlandır",
                    "§7Cezasız iptal (karşılıklı)",
                    "§cDikkat: Bu işlem geri alınamaz!")));
        } else {
            if (!alliance.isActive()) {
                menu.setItem(31, createButton(Material.BARRIER, "§cPasif İttifak", 
                    Arrays.asList("§7Bu ittifak artık aktif değil")));
            } else {
                menu.setItem(31, createButton(Material.BARRIER, "§cYetki Yok", 
                    Arrays.asList("§7Sadece klan lideri iptal edebilir")));
            }
        }
        
        // Geri butonu
        menu.setItem(45, createButton(Material.ARROW, "§7Geri", Arrays.asList("§7İttifak listesine dön")));
        
        openDetailMenus.put(player.getUniqueId(), alliance);
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        
        if (title.equals("§6Klan İttifakları")) {
            handleMainMenuClick(event);
        } else if (title.equals("§6İttifak Detayları")) {
            handleDetailMenuClick(event);
        }
    }
    
    private void handleMainMenuClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) return;
        
        int slot = event.getSlot();
        
        if (slot == 53) {
            // Geri butonu
            if (plugin.getClanMenu() != null) {
                plugin.getClanMenu().openMenu(player);
            }
            return;
        }
        
        if (slot == 49 && clicked.getType() == Material.DIAMOND) {
            // Yeni ittifak kurma butonu
            player.sendMessage("§e§l═══════════════════════════");
            player.sendMessage("§eİttifak Kurma:");
            player.sendMessage("§71. Diğer klanın liderini bulun");
            player.sendMessage("§72. Shift tuşuna basılı tutun");
            player.sendMessage("§73. Elmas ile liderin üzerine sağ tık yapın");
            player.sendMessage("§74. Ritüel otomatik başlayacak");
            player.sendMessage("§e§l═══════════════════════════");
            player.closeInventory();
            return;
        }
        
        if (slot < 45) {
            // İttifak seçildi
            List<Alliance> alliances = allianceManager.getAlliances(clan.getId());
            if (alliances != null && slot < alliances.size()) {
                Alliance alliance = alliances.get(slot);
                if (alliance != null) {
                    if (event.isLeftClick()) {
                        openAllianceDetailMenu(player, alliance);
                    } else if (event.isRightClick() && AllianceHelper.canDissolveAlliance(alliance, clan, player)) {
                        // İttifakı sonlandır onay menüsü
                        openDissolveConfirmMenu(player, alliance);
                    }
                }
            }
        }
    }
    
    private void handleDetailMenuClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        Alliance alliance = openDetailMenus.get(player.getUniqueId());
        if (alliance == null) {
            openMainMenu(player);
            return;
        }
        
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) return;
        
        int slot = event.getSlot();
        
        if (slot == 45) {
            // Geri butonu
            openMainMenu(player);
            return;
        }
        
        if (slot == 31 && clicked.getType() == Material.RED_CONCRETE) {
            // İttifakı sonlandır
            if (AllianceHelper.canDissolveAlliance(alliance, clan, player)) {
                openDissolveConfirmMenu(player, alliance);
            }
        }
    }
    
    /**
     * İttifak sonlandırma onay menüsü
     */
    private void openDissolveConfirmMenu(Player player, Alliance alliance) {
        if (player == null || alliance == null) return;
        
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) return;
        
        UUID otherClanId = alliance.getOtherClan(clan.getId());
        Clan otherClan = clanManager.getAllClans().stream()
            .filter(c -> c.getId().equals(otherClanId))
            .findFirst().orElse(null);
        
        String otherClanName = otherClan != null ? otherClan.getName() : "Bilinmeyen Klan";
        
        Inventory menu = Bukkit.createInventory(null, 27, "§6İttifak Sonlandırma");
        
        // Uyarı mesajı
        menu.setItem(13, createButton(Material.RED_CONCRETE, "§c§lİTTİFAKI SONLANDIR", 
            Arrays.asList("§7═══════════════════════",
                "§7Diğer Klan: §e" + otherClanName,
                "§7İttifak Tipi: §e" + AllianceHelper.getAllianceTypeDisplayName(alliance.getType()),
                "§7═══════════════════════",
                "§cBu işlem geri alınamaz!",
                "§7Karşılıklı sonlandırma",
                "§7(ceza yok)",
                "§7═══════════════════════")));
        
        // Onay butonu
        menu.setItem(11, createButton(Material.GREEN_CONCRETE, "§a§lONAYLA", 
            Arrays.asList("§7İttifakı sonlandır")));
        
        // İptal butonu
        menu.setItem(15, createButton(Material.RED_CONCRETE, "§c§lİPTAL", 
            Arrays.asList("§7İşlemi iptal et")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * İttifakı sonlandır
     */
    private void dissolveAlliance(Player player, Alliance alliance) {
        if (player == null || alliance == null) return;
        
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) return;
        
        // Sonlandırma kontrolü
        if (!AllianceHelper.canDissolveAlliance(alliance, clan, player)) {
            player.sendMessage("§cBu ittifak sonlandırılamaz!");
            return;
        }
        
        // İttifakı sonlandır
        allianceManager.dissolveAlliance(alliance.getId(), clan.getId());
        
        player.sendMessage("§aİttifak başarıyla sonlandırıldı!");
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        
        // Ana menüye dön
        openMainMenu(player);
    }
    
    @EventHandler
    public void onConfirmMenuClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§6İttifak Sonlandırma")) return;
        
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        Alliance alliance = openDetailMenus.get(player.getUniqueId());
        if (alliance == null) {
            openMainMenu(player);
            return;
        }
        
        int slot = event.getSlot();
        
        if (slot == 11 && clicked.getType() == Material.GREEN_CONCRETE) {
            // Onay
            dissolveAlliance(player, alliance);
            openDetailMenus.remove(player.getUniqueId()); // Detay menüsünden çıkar
        } else if (slot == 15 && clicked.getType() == Material.RED_CONCRETE) {
            // İptal
            openAllianceDetailMenu(player, alliance);
        }
    }
    
    /**
     * Buton oluştur
     */
    private ItemStack createButton(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
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








