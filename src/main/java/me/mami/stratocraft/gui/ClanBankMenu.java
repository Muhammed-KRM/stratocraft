package me.mami.stratocraft.gui;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.manager.clan.ClanBankSystem;
import me.mami.stratocraft.manager.clan.ClanRankSystem;
import me.mami.stratocraft.model.Clan;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Klan Bankası GUI Menüsü
 * 
 * Özellikler:
 * - Banka sandığı görüntüleme
 * - Item yatırma/çekme
 * - Rütbe bazlı yetki kontrolü
 * - Maaş bilgisi
 * - Transfer kontratları
 */
public class ClanBankMenu implements Listener {
    private final Main plugin;
    private final ClanManager clanManager;
    private final ClanBankSystem bankSystem;
    private final ClanRankSystem rankSystem;
    
    // Açık banka menüleri (player -> inventory)
    private final java.util.Map<UUID, Inventory> openMenus = new java.util.concurrent.ConcurrentHashMap<>();
    
    // ✅ PERFORMANS: Menü açılışında klan ID cache'i (oyuncu çıkışına kadar geçerli)
    private final java.util.Map<UUID, UUID> menuClanCache = new java.util.concurrent.ConcurrentHashMap<>();
    
    public ClanBankMenu(Main plugin, ClanManager clanManager, ClanBankSystem bankSystem, ClanRankSystem rankSystem) {
        this.plugin = plugin;
        this.clanManager = clanManager;
        this.bankSystem = bankSystem;
        this.rankSystem = rankSystem;
    }
    
    /**
     * Ana banka menüsünü aç
     */
    public void openMainMenu(Player player) {
        if (player == null || bankSystem == null) return;
        
        // Manager null kontrolü
        if (clanManager == null) {
            player.sendMessage("§cKlan sistemi aktif değil!");
            plugin.getLogger().warning("ClanManager null! Menü açılamıyor.");
            return;
        }
        
        UUID playerId = player.getUniqueId();
        Clan clan = clanManager.getClanByPlayer(playerId);
        if (clan == null) {
            player.sendMessage("§cBir klana üye değilsiniz!");
            return;
        }
        
        // ✅ PERFORMANS: Cache'e klan ID'sini kaydet
        menuClanCache.put(playerId, clan.getId());
        
        Inventory menu = Bukkit.createInventory(null, 27, "§6Klan Bankası");
        
        // Banka sandığı aç
        org.bukkit.inventory.Inventory bankChest = bankSystem.getBankChest(clan);
        if (bankChest != null) {
            menu.setItem(11, createButton(Material.ENDER_CHEST, "§e§lBanka Sandığı", 
                Arrays.asList("§7Banka sandığını aç", "§7Item yatır/çek")));
        } else {
            menu.setItem(11, createButton(Material.BARRIER, "§cBanka Sandığı Yok", 
                Arrays.asList("§7Klan bankası henüz oluşturulmamış", "§7Lider veya General yetkisi gerekli")));
        }
        
        // Maaş bilgisi
        long lastSalary = bankSystem.getLastSalaryTime(player.getUniqueId());
        long salaryInterval = bankSystem.getConfig().getSalaryInterval();
        long timeUntilSalary = Math.max(0, salaryInterval - (System.currentTimeMillis() - lastSalary));
        long hoursUntil = timeUntilSalary / (1000 * 60 * 60);
        long minutesUntil = (timeUntilSalary % (1000 * 60 * 60)) / (1000 * 60);
        
        List<String> salaryLore = new ArrayList<>();
        salaryLore.add("§7═══════════════════════");
        if (timeUntilSalary > 0) {
            salaryLore.add("§7Sonraki maaş: §e" + hoursUntil + "s " + minutesUntil + "d");
        } else {
            salaryLore.add("§aMaaş alınabilir!");
        }
        salaryLore.add("§7═══════════════════════");
        menu.setItem(13, createButton(Material.GOLD_INGOT, "§eMaaş Bilgisi", salaryLore));
        
        // Transfer kontratları
        List<ClanBankSystem.TransferContract> contracts = bankSystem.getTransferContracts(clan.getId());
        if (contracts != null && !contracts.isEmpty()) {
            menu.setItem(15, createButton(Material.PAPER, "§eTransfer Kontratları", 
                Arrays.asList("§7Aktif kontratlar: §e" + contracts.size())));
        }
        
        // Bilgi
        menu.setItem(22, createButton(Material.BOOK, "§eBilgi", 
            Arrays.asList("§7Rütbe bazlı yetkiler:", 
                "§7- Lider: Tüm yetkiler",
                "§7- General: Yatır/Çek",
                "§7- Elite: Yatır/Çek",
                "§7- Member: Sadece Yatır")));
        
        // Kapat
        menu.setItem(18, createButton(Material.BARRIER, "§cKapat", null));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Banka sandığı menüsünü aç
     */
    public void openBankChestMenu(Player player) {
        if (player == null || bankSystem == null) return;
        
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage("§cBir klana üye değilsiniz!");
            return;
        }
        
        org.bukkit.inventory.Inventory bankChest = bankSystem.getBankChest(clan);
        if (bankChest == null) {
            player.sendMessage("§cKlan bankası bulunamadı!");
            return;
        }
        
        // Banka sandığını aç (kopya oluştur)
        Inventory menu = Bukkit.createInventory(null, 54, "§6Klan Bankası - Sandık");
        
        // Sandık içeriğini kopyala
        ItemStack[] contents = bankChest.getContents();
        for (int i = 0; i < Math.min(contents.length, 45); i++) {
            if (contents[i] != null && contents[i].getType() != Material.AIR) {
                menu.setItem(i, contents[i].clone());
            }
        }
        
        // Yatır/Çek butonları
        menu.setItem(45, createButton(Material.GREEN_CONCRETE, "§a§lYATIR", 
            Arrays.asList("§7Elindeki itemleri bankaya yatır")));
        menu.setItem(46, createButton(Material.ORANGE_CONCRETE, "§e§lÇEK", 
            Arrays.asList("§7Bankadan item çek")));
        
        // Geri butonu
        menu.setItem(49, createButton(Material.ARROW, "§7Geri", Arrays.asList("§7Ana menüye dön")));
        
        openMenus.put(player.getUniqueId(), menu);
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
    }
    
    /**
     * Transfer kontratları menüsü
     */
    public void openTransferContractsMenu(Player player) {
        if (player == null || bankSystem == null) return;
        
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage("§cBir klana üye değilsiniz!");
            return;
        }
        
        List<ClanBankSystem.TransferContract> contracts = bankSystem.getTransferContracts(clan.getId());
        
        Inventory menu = Bukkit.createInventory(null, 54, "§6Transfer Kontratları");
        
        if (contracts == null || contracts.isEmpty()) {
            menu.setItem(22, createButton(Material.BARRIER, "§cKontrat Yok", 
                Arrays.asList("§7Henüz transfer kontratı yok")));
        } else {
            int slot = 0;
            for (ClanBankSystem.TransferContract contract : contracts) {
                if (slot >= 45) break;
                
                List<String> lore = new ArrayList<>();
                lore.add("§7═══════════════════════");
                if (contract.getTargetPlayerId() != null) {
                    org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(contract.getTargetPlayerId());
                    lore.add("§7Hedef: §e" + (target.getName() != null ? target.getName() : "Bilinmeyen"));
                } else {
                    lore.add("§7Hedef: §eBilinmeyen");
                }
                lore.add("§7Malzeme: §e" + contract.getMaterial().name());
                lore.add("§7Miktar: §e" + contract.getAmount());
                lore.add("§7Durum: §e" + (contract.isActive() ? "Aktif" : "Pasif"));
                lore.add("§7═══════════════════════");
                
                menu.setItem(slot++, createButton(contract.getMaterial(), 
                    "§eTransfer Kontratı", lore));
            }
        }
        
        // Geri butonu
        menu.setItem(49, createButton(Material.ARROW, "§7Geri", Arrays.asList("§7Ana menüye dön")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        
        if (title.equals("§6Klan Bankası")) {
            handleMainMenuClick(event);
        } else if (title.equals("§6Klan Bankası - Sandık")) {
            handleBankChestClick(event);
        } else if (title.equals("§6Transfer Kontratları")) {
            handleTransferContractsClick(event);
        } else if (title.equals("§6Item Çek")) {
            onWithdrawMenuClick(event);
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            UUID playerId = player.getUniqueId();
            openMenus.remove(playerId);
            // ✅ PERFORMANS: Menü kapandığında cache'i temizle (oyuncu çıkışında da temizlenecek)
            // Not: Cache'i burada temizlemiyoruz çünkü oyuncu menüyü tekrar açabilir
        }
    }
    
    /**
     * Oyuncu çıkışında cache'i temizle
     * ✅ PERFORMANS: Memory leak önleme
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        menuClanCache.remove(playerId);
        openMenus.remove(playerId);
    }
    
    private void handleMainMenuClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        UUID playerId = player.getUniqueId();
        // ✅ PERFORMANS: Cache'den klan ID'sini al
        UUID cachedClanId = menuClanCache.get(playerId);
        Clan clan = null;
        
        if (cachedClanId != null) {
            // Cache'den al
            clan = clanManager.getClanById(cachedClanId);
        }
        
        if (clan == null) {
            // Cache'de yoksa hesapla
            clan = clanManager.getClanByPlayer(playerId);
            if (clan == null) {
                player.sendMessage("§cBir klana üye değilsiniz!");
                player.closeInventory();
                return;
            }
            // Cache'e kaydet
            menuClanCache.put(playerId, clan.getId());
        }
        
        // YENİ: Yetki kontrolü
        if (rankSystem != null && !rankSystem.hasPermission(clan, playerId, 
                ClanRankSystem.Permission.MANAGE_BANK)) {
            player.sendMessage("§cBanka işlemleri için yetkiniz yok!");
            player.closeInventory();
            return;
        }
        
        switch (clicked.getType()) {
            case ENDER_CHEST:
                // Banka sandığı aç
                openBankChestMenu(player);
                break;
                
            case PAPER:
                // Transfer kontratları
                openTransferContractsMenu(player);
                break;
                
            case BARRIER:
                // Kapat
                player.closeInventory();
                break;
        }
    }
    
    private void handleBankChestClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        int slot = event.getSlot();
        
        // Yatır/Çek butonları
        if (slot == 45 || slot == 46) {
            event.setCancelled(true);
            
            UUID playerId = player.getUniqueId();
            // ✅ PERFORMANS: Cache'den klan ID'sini al
            UUID cachedClanId = menuClanCache.get(playerId);
            Clan clan = null;
            
            if (cachedClanId != null) {
                clan = clanManager.getClanById(cachedClanId);
            }
            
            if (clan == null) {
                // Cache'de yoksa hesapla
                clan = clanManager.getClanByPlayer(playerId);
                if (clan == null) return;
                // Cache'e kaydet
                menuClanCache.put(playerId, clan.getId());
            }
            
            if (slot == 45) {
                // Yatır - Elindeki itemleri bankaya yatır
                depositAllItems(player, clan);
            } else if (slot == 46) {
                // Çek - Bankadan item çek menüsü
                openWithdrawMenu(player, clan);
            }
            return;
        }
        
        // Geri butonu
        if (slot == 49) {
            event.setCancelled(true);
            openMainMenu(player);
            return;
        }
        
        // Sandık slotları (0-44) - Normal işlem
        if (slot < 45) {
            // Sandık içeriğini güncelle
            UUID playerId = player.getUniqueId();
            // ✅ PERFORMANS: Cache'den klan ID'sini al
            UUID cachedClanId = menuClanCache.get(playerId);
            Clan playerClan = null;
            
            if (cachedClanId != null) {
                playerClan = clanManager.getClanById(cachedClanId);
            }
            
            if (playerClan == null) {
                playerClan = clanManager.getClanByPlayer(playerId);
                if (playerClan != null) {
                    menuClanCache.put(playerId, playerClan.getId());
                }
            }
            
            if (playerClan != null) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    updateBankChest(player, playerClan);
                }, 1L);
            }
        }
    }
    
    private void handleTransferContractsClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        if (clicked.getType() == Material.ARROW) {
            openMainMenu(player);
        }
    }
    
    /**
     * Tüm itemleri bankaya yatır
     */
    private void depositAllItems(Player player, Clan clan) {
        int deposited = 0;
        
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                if (bankSystem.depositItem(player, item, item.getAmount())) {
                    deposited++;
                }
            }
        }
        
        if (deposited > 0) {
            player.sendMessage("§a" + deposited + " farklı item bankaya yatırıldı!");
        } else {
            player.sendMessage("§cYatırılacak item bulunamadı!");
        }
        
        // Menüyü yenile
        openBankChestMenu(player);
    }
    
    /**
     * Çekme menüsü
     */
    private void openWithdrawMenu(Player player, Clan clan) {
        org.bukkit.inventory.Inventory bankChest = bankSystem.getBankChest(clan);
        if (bankChest == null) {
            player.sendMessage("§cKlan bankası bulunamadı!");
            return;
        }
        
        Inventory menu = Bukkit.createInventory(null, 54, "§6Item Çek");
        
        // Banka içeriğini göster
        ItemStack[] contents = bankChest.getContents();
        int slot = 0;
        for (ItemStack item : contents) {
            if (item != null && item.getType() != Material.AIR && slot < 45) {
                List<String> lore = new ArrayList<>();
                lore.add("§7═══════════════════════");
                lore.add("§7Miktar: §e" + item.getAmount());
                lore.add("§7═══════════════════════");
                lore.add("§aSol Tık: §7Tümünü çek");
                lore.add("§eSağ Tık: §7Yarısını çek");
                lore.add("§cShift+Sol: §7Tek çek");
                
                ItemStack displayItem = item.clone();
                ItemMeta meta = displayItem.getItemMeta();
                if (meta != null) {
                    meta.setLore(lore);
                    displayItem.setItemMeta(meta);
                }
                
                menu.setItem(slot++, displayItem);
            }
        }
        
        // Geri butonu
        menu.setItem(49, createButton(Material.ARROW, "§7Geri", Arrays.asList("§7Banka sandığına dön")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Çekme menüsü tıklama
     */
    @EventHandler
    public void onWithdrawMenuClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§6Item Çek")) return;
        
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        if (clicked.getType() == Material.ARROW) {
            // Geri
            Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
            if (clan != null) {
                openBankChestMenu(player);
            }
            return;
        }
        
        // Item çek
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) return;
        
        Material material = clicked.getType();
        int amount = clicked.getAmount();
        
        if (event.isShiftClick()) {
            // Tek çek
            amount = 1;
        } else if (event.isRightClick()) {
            // Yarısını çek
            amount = amount / 2;
            if (amount < 1) amount = 1;
        }
        // Sol tık = Tümünü çek (amount zaten doğru)
        
        if (bankSystem.withdrawItem(player, material, amount)) {
            player.sendMessage("§a" + amount + "x " + material.name() + " çekildi!");
            // Menüyü yenile
            openWithdrawMenu(player, clan);
        }
    }
    
    /**
     * Banka sandığını güncelle
     */
    private void updateBankChest(Player player, Clan clan) {
        Inventory menu = openMenus.get(player.getUniqueId());
        if (menu == null) return;
        
        org.bukkit.inventory.Inventory bankChest = bankSystem.getBankChest(clan);
        if (bankChest == null) return;
        
        // Sandık içeriğini güncelle
        for (int i = 0; i < 45; i++) {
            ItemStack menuItem = menu.getItem(i);
            if (menuItem != null && menuItem.getType() != Material.AIR) {
                // Menüden bankaya kopyala
                bankChest.setItem(i, menuItem.clone());
            } else {
                // Boş slot
                bankChest.setItem(i, null);
            }
        }
    }
    
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

