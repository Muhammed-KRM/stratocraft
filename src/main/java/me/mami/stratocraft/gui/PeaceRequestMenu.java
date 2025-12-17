package me.mami.stratocraft.gui;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.manager.PeaceRequestManager;
import me.mami.stratocraft.manager.SiegeManager;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.PeaceRequest;
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
 * Barış Anlaşması GUI Menüsü
 * 
 * Özellikler:
 * - Savaşta olunan klanları listeleme
 * - Barış anlaşması isteği gönderme
 * - Gelen istekleri görüntüleme ve onaylama/reddetme
 */
public class PeaceRequestMenu implements Listener {
    private final Main plugin;
    private final ClanManager clanManager;
    private final PeaceRequestManager peaceRequestManager;
    private final SiegeManager siegeManager;
    
    // Açık menü durumları
    private final java.util.Map<UUID, String> openMenus = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.Map<UUID, UUID> selectedClanForRequest = new java.util.concurrent.ConcurrentHashMap<>();
    
    public PeaceRequestMenu(Main plugin, ClanManager clanManager, 
                           PeaceRequestManager peaceRequestManager, SiegeManager siegeManager) {
        this.plugin = plugin;
        this.clanManager = clanManager;
        this.peaceRequestManager = peaceRequestManager;
        this.siegeManager = siegeManager;
    }
    
    /**
     * Ana barış anlaşması menüsünü aç
     */
    public void openMainMenu(Player player) {
        if (player == null) return;
        
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage("§cBir klana üye değilsiniz!");
            return;
        }
        
        // Yetki kontrolü: Sadece Lider ve General
        Clan.Rank rank = clan.getRank(player.getUniqueId());
        if (rank != Clan.Rank.LEADER && rank != Clan.Rank.GENERAL) {
            player.sendMessage("§cSadece Lider ve General barış anlaşması yapabilir!");
            return;
        }
        
        // 54 slotlu menü (6x9)
        Inventory menu = Bukkit.createInventory(null, 54, "§6Barış Anlaşması");
        
        // Savaşta olunan klanları listele
        List<UUID> warringClans = new ArrayList<>(clan.getWarringClans());
        
        int slot = 0;
        for (UUID warringClanId : warringClans) {
            if (slot >= 36) break; // İlk 4 satır (36 slot)
            
            Clan warringClan = clanManager.getClanById(warringClanId);
            if (warringClan == null) continue;
            
            // Zaten aktif istek var mı?
            boolean hasActiveRequest = peaceRequestManager.hasActiveRequest(clan.getId(), warringClanId);
            
            ItemStack item = new ItemStack(Material.RED_BANNER);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§c§l" + warringClan.getName());
                List<String> lore = new ArrayList<>();
                lore.add("§7═══════════════════════");
                lore.add("§7Durum: §cSavaş Halinde");
                lore.add("§7═══════════════════════");
                if (hasActiveRequest) {
                    lore.add("§eAktif istek mevcut");
                    lore.add("§7İstek durumunu kontrol edin");
                } else {
                    lore.add("§aSol Tık: §7Barış isteği gönder");
                }
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            menu.setItem(slot++, item);
        }
        
        // Gelen İstekler Butonu (Slot 45)
        List<PeaceRequest> receivedRequests = peaceRequestManager.getReceivedRequests(clan.getId());
        int requestCount = receivedRequests != null ? receivedRequests.size() : 0;
        menu.setItem(45, createButton(Material.ENVELOPE, "§aGelen İstekler §7(" + requestCount + ")", 
            Arrays.asList("§7Size gönderilen barış anlaşması",
                "§7isteklerini görüntüleyin",
                "§7Toplam: §e" + requestCount + " istek")));
        
        // Gönderilen İstekler Butonu (Slot 46)
        List<PeaceRequest> sentRequests = peaceRequestManager.getSentRequests(clan.getId());
        int sentCount = sentRequests != null ? sentRequests.size() : 0;
        menu.setItem(46, createButton(Material.PAPER, "§eGönderilen İstekler §7(" + sentCount + ")", 
            Arrays.asList("§7Gönderdiğiniz barış anlaşması",
                "§7isteklerini görüntüleyin",
                "§7Toplam: §e" + sentCount + " istek")));
        
        // Bilgi Butonu (Slot 49)
        menu.setItem(49, createButton(Material.BOOK, "§eBilgi", 
            Arrays.asList("§7Barış Anlaşması:",
                "§7- İki klan da onaylamalı",
                "§7- Savaş sona erer",
                "§7- Hiçbir taraf kayıp yaşamaz",
                "§7- İstek 24 saat geçerlidir")));
        
        // Geri Butonu (Slot 53)
        menu.setItem(53, createButton(Material.ARROW, "§7Geri", 
            Arrays.asList("§7Ana klan menüsüne dön")));
        
        openMenus.put(player.getUniqueId(), "main");
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Gelen istekler menüsünü aç
     */
    public void openReceivedRequestsMenu(Player player) {
        if (player == null) return;
        
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) return;
        
        List<PeaceRequest> requests = peaceRequestManager.getReceivedRequests(clan.getId());
        if (requests == null) requests = new ArrayList<>();
        
        Inventory menu = Bukkit.createInventory(null, 54, "§6Gelen Barış İstekleri");
        
        int slot = 0;
        for (PeaceRequest request : requests) {
            if (slot >= 45) break;
            
            if (!request.isValid()) continue; // Geçersiz istekleri atla
            
            Clan senderClan = clanManager.getClanById(request.getSenderClanId());
            if (senderClan == null) continue;
            
            ItemStack item = new ItemStack(Material.WHITE_BANNER);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§f§l" + senderClan.getName());
                List<String> lore = new ArrayList<>();
                lore.add("§7═══════════════════════");
                lore.add("§7Gönderen: §e" + senderClan.getName());
                long remainingTime = (request.getExpiresAt() - System.currentTimeMillis()) / 1000;
                long hours = remainingTime / 3600;
                long minutes = (remainingTime % 3600) / 60;
                lore.add("§7Kalan Süre: §e" + hours + " saat " + minutes + " dakika");
                lore.add("§7═══════════════════════");
                lore.add("§aSol Tık: §7İsteği onayla");
                lore.add("§cSağ Tık: §7İsteği reddet");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            menu.setItem(slot++, item);
        }
        
        // Geri Butonu
        menu.setItem(45, createButton(Material.ARROW, "§7Geri", 
            Arrays.asList("§7Ana menüye dön")));
        
        openMenus.put(player.getUniqueId(), "received");
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Gönderilen istekler menüsünü aç
     */
    public void openSentRequestsMenu(Player player) {
        if (player == null) return;
        
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) return;
        
        List<PeaceRequest> requests = peaceRequestManager.getSentRequests(clan.getId());
        if (requests == null) requests = new ArrayList<>();
        
        Inventory menu = Bukkit.createInventory(null, 54, "§6Gönderilen Barış İstekleri");
        
        int slot = 0;
        for (PeaceRequest request : requests) {
            if (slot >= 45) break;
            
            if (!request.isValid()) continue;
            
            Clan targetClan = clanManager.getClanById(request.getTargetClanId());
            if (targetClan == null) continue;
            
            ItemStack item = new ItemStack(Material.YELLOW_BANNER);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§e§l" + targetClan.getName());
                List<String> lore = new ArrayList<>();
                lore.add("§7═══════════════════════");
                lore.add("§7Hedef: §e" + targetClan.getName());
                lore.add("§7Durum: §eBeklemede");
                long remainingTime = (request.getExpiresAt() - System.currentTimeMillis()) / 1000;
                long hours = remainingTime / 3600;
                long minutes = (remainingTime % 3600) / 60;
                lore.add("§7Kalan Süre: §e" + hours + " saat " + minutes + " dakika");
                lore.add("§7═══════════════════════");
                lore.add("§7İstek henüz onaylanmadı");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            menu.setItem(slot++, item);
        }
        
        // Geri Butonu
        menu.setItem(45, createButton(Material.ARROW, "§7Geri", 
            Arrays.asList("§7Ana menüye dön")));
        
        openMenus.put(player.getUniqueId(), "sent");
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        
        if (title.equals("§6Barış Anlaşması")) {
            handleMainMenuClick(event);
        } else if (title.equals("§6Gelen Barış İstekleri")) {
            handleReceivedRequestsClick(event);
        } else if (title.equals("§6Gönderilen Barış İstekleri")) {
            handleSentRequestsClick(event);
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
        
        if (slot == 45) {
            // Gelen istekler
            openReceivedRequestsMenu(player);
            return;
        }
        
        if (slot == 46) {
            // Gönderilen istekler
            openSentRequestsMenu(player);
            return;
        }
        
        if (slot < 36) {
            // Savaşta olunan klan seçildi
            List<UUID> warringClans = new ArrayList<>(clan.getWarringClans());
            if (slot < warringClans.size()) {
                UUID targetClanId = warringClans.get(slot);
                Clan targetClan = clanManager.getClanById(targetClanId);
                
                if (targetClan != null && event.isLeftClick()) {
                    // Barış isteği gönder
                    PeaceRequest request = peaceRequestManager.sendPeaceRequest(clan.getId(), targetClanId);
                    if (request != null) {
                        player.sendMessage("§a" + targetClan.getName() + " klanına barış anlaşması isteği gönderildi!");
                        player.closeInventory();
                    } else {
                        player.sendMessage("§cİstek gönderilemedi! (Zaten aktif istek var veya savaşta değilsiniz)");
                    }
                }
            }
        }
    }
    
    private void handleReceivedRequestsClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) return;
        
        int slot = event.getSlot();
        
        if (slot == 45) {
            // Geri butonu
            openMainMenu(player);
            return;
        }
        
        if (slot < 45) {
            List<PeaceRequest> requests = peaceRequestManager.getReceivedRequests(clan.getId());
            if (requests != null && slot < requests.size()) {
                PeaceRequest request = requests.get(slot);
                if (request == null || !request.isValid()) return;
                
                if (event.isLeftClick()) {
                    // İsteği onayla
                    boolean success = peaceRequestManager.acceptRequest(request.getId(), clan.getId());
                    if (success) {
                        player.sendMessage("§aBarış anlaşması onaylandı! Savaş sona erdi.");
                        player.closeInventory();
                    } else {
                        player.sendMessage("§cİstek onaylanamadı!");
                    }
                } else if (event.isRightClick()) {
                    // İsteği reddet
                    boolean success = peaceRequestManager.rejectRequest(request.getId(), clan.getId());
                    if (success) {
                        player.sendMessage("§eBarış anlaşması isteği reddedildi.");
                        openReceivedRequestsMenu(player);
                    } else {
                        player.sendMessage("§cİstek reddedilemedi!");
                    }
                }
            }
        }
    }
    
    private void handleSentRequestsClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        int slot = event.getSlot();
        
        if (slot == 45) {
            // Geri butonu
            openMainMenu(player);
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

