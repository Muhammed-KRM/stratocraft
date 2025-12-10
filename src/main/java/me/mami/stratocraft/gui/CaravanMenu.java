package me.mami.stratocraft.gui;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.CaravanManager;
import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.manager.GameBalanceConfig;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.util.CaravanHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mule;
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
 * Kervan GUI Menüsü
 * 
 * Özellikler:
 * - Aktif kervanları listeleme
 * - Kervan oluşturma menüsü
 * - Kervan detayları
 * - Kervan yönetimi
 */
public class CaravanMenu implements Listener {
    private final Main plugin;
    private final ClanManager clanManager;
    private final CaravanManager caravanManager;
    private final GameBalanceConfig balanceConfig;
    
    // Açık menüler (player -> location/entity)
    private final java.util.Map<UUID, Location> pendingStartLocations = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.Map<UUID, Location> pendingEndLocations = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.Map<UUID, Entity> openDetailMenus = new java.util.concurrent.ConcurrentHashMap<>();
    
    public CaravanMenu(Main plugin, ClanManager clanManager, CaravanManager caravanManager,
                      GameBalanceConfig balanceConfig) {
        this.plugin = plugin;
        this.clanManager = clanManager;
        this.caravanManager = caravanManager;
        this.balanceConfig = balanceConfig;
    }
    
    /**
     * Ana kervan menüsünü aç
     */
    public void openMainMenu(Player player) {
        if (player == null) return;
        
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage("§cBir klana üye değilsiniz!");
            return;
        }
        
        // Aktif kervanları bul (klan üyelerinin kervanları)
        List<Entity> activeCaravans = new ArrayList<>();
        for (UUID memberId : clan.getMembers().keySet()) {
            Entity caravan = caravanManager.getCaravan(memberId);
            if (caravan != null && caravan.isValid()) {
                activeCaravans.add(caravan);
            }
        }
        
        Inventory menu = Bukkit.createInventory(null, 54, "§6Kervan Yönetimi");
        
        // Kervanları listele (45 slot - 0-44)
        int slot = 0;
        for (Entity caravan : activeCaravans) {
            if (caravan == null || slot >= 45) break;
            
            UUID ownerId = caravanManager.getOwner(caravan);
            org.bukkit.OfflinePlayer owner = ownerId != null ? Bukkit.getOfflinePlayer(ownerId) : null;
            String ownerName = owner != null && owner.getName() != null ? owner.getName() : "Bilinmeyen";
            
            // Target location bilgisi (CaravanManager'dan alınabilir - reflection gerekebilir)
            Location target = null;
            try {
                // Reflection ile caravanTargets map'inden al
                java.lang.reflect.Field targetsField = CaravanManager.class.getDeclaredField("caravanTargets");
                targetsField.setAccessible(true);
                @SuppressWarnings("unchecked")
                java.util.Map<UUID, Location> targets = (java.util.Map<UUID, Location>) targetsField.get(caravanManager);
                if (targets != null && ownerId != null) {
                    target = targets.get(ownerId);
                }
            } catch (Exception e) {
                // Hata durumunda null kalır
            }
            
            String status = CaravanHelper.getCaravanStatus(caravan);
            String arrivalTime = target != null ? CaravanHelper.getEstimatedArrivalTime(caravan.getLocation(), target) : "Bilinmeyen";
            
            List<String> lore = new ArrayList<>();
            lore.add("§7═══════════════════════");
            lore.add("§7Sahip: §e" + ownerName);
            lore.add("§7Durum: " + status);
            if (target != null) {
                lore.add("§7Tahmini Varış: " + arrivalTime);
            }
            if (caravan instanceof Mule) {
                Mule mule = (Mule) caravan;
                int cargoCount = 0;
                for (ItemStack item : mule.getInventory().getContents()) {
                    if (item != null && item.getType() != Material.AIR) {
                        cargoCount++;
                    }
                }
                lore.add("§7Yük Slotları: §e" + cargoCount);
            }
            lore.add("§7═══════════════════════");
            lore.add("§aSol Tık: §7Detayları gör");
            
            ItemStack item = new ItemStack(Material.CHEST_MINECART);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§eKervan - " + ownerName);
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            
            menu.setItem(slot++, item);
        }
        
        // Yeni kervan oluştur butonu
        menu.setItem(49, createButton(Material.EMERALD, "§a§lYENİ KERVAN OLUŞTUR", 
            Arrays.asList("§7Kervan oluşturmak için tıklayın",
                "§7Önce başlangıç konumunu seçin")));
        
        // Bilgi butonu
        menu.setItem(45, createButton(Material.BOOK, "§eBilgi", 
            Arrays.asList("§7Toplam Kervan: §e" + activeCaravans.size(),
                "§7Kervanlar mallarınızı taşır",
                "§7Hedefe ulaşınca x1.5 değer kazanır")));
        
        // Geri butonu
        menu.setItem(53, createButton(Material.ARROW, "§7Geri", Arrays.asList("§7Ana klan menüsüne dön")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Kervan oluşturma menüsünü aç
     */
    public void openCreateCaravanMenu(Player player) {
        if (player == null) return;
        
        Inventory menu = Bukkit.createInventory(null, 27, "§6Kervan Oluştur");
        
        // Başlangıç konumu seç
        Location startLoc = pendingStartLocations.get(player.getUniqueId());
        String startLocStr = startLoc != null ? 
            "§aSeçildi: §e" + startLoc.getBlockX() + ", " + startLoc.getBlockY() + ", " + startLoc.getBlockZ() :
            "§cSeçilmedi";
        
        menu.setItem(11, createButton(Material.GREEN_CONCRETE, "§a§lBAŞLANGIÇ KONUMU", 
            Arrays.asList("§7Mevcut konumunuzu başlangıç",
                "§7olarak ayarlamak için tıklayın",
                "§7═══════════════════════",
                startLocStr)));
        
        // Hedef konumu seç
        Location endLoc = pendingEndLocations.get(player.getUniqueId());
        String endLocStr = endLoc != null ? 
            "§aSeçildi: §e" + endLoc.getBlockX() + ", " + endLoc.getBlockY() + ", " + endLoc.getBlockZ() :
            "§cSeçilmedi";
        
        menu.setItem(15, createButton(Material.ORANGE_CONCRETE, "§e§lHEDEF KONUMU", 
            Arrays.asList("§7Hedef konumu ayarlamak için",
                "§7tıklayın ve chat'e koordinat",
                "§7yazın (X Y Z)",
                "§7═══════════════════════",
                endLocStr)));
        
        // Yük bilgisi
        int totalItems = 0;
        double totalValue = 0.0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                totalItems += item.getAmount();
                totalValue += CaravanHelper.calculateCargoValue(Arrays.asList(item));
            }
        }
        
        int minStacks = balanceConfig != null ? balanceConfig.getCaravanMinStacks() : 20;
        int minItems = minStacks * 64;
        double minValue = balanceConfig != null ? balanceConfig.getCaravanMinValue() : 5000.0;
        
        boolean hasEnoughItems = totalItems >= minItems;
        boolean hasEnoughValue = totalValue >= minValue;
        
        List<String> cargoLore = new ArrayList<>();
        cargoLore.add("§7═══════════════════════");
        cargoLore.add("§7Envanter Yükü:");
        cargoLore.add("§7Toplam Item: §e" + totalItems + "§7/§e" + minItems);
        cargoLore.add("§7Yük Değeri: §e" + String.format("%.1f", totalValue) + "§7/§e" + minValue);
        cargoLore.add("§7═══════════════════════");
        if (hasEnoughItems && hasEnoughValue) {
            cargoLore.add("§aYeterli yük var!");
        } else {
            cargoLore.add("§cYetersiz yük!");
            if (!hasEnoughItems) {
                cargoLore.add("§7En az " + minItems + " item gerekli");
            }
            if (!hasEnoughValue) {
                cargoLore.add("§7En az " + minValue + " değer gerekli");
            }
        }
        
        menu.setItem(13, createButton(Material.CHEST, "§eYük Bilgisi", cargoLore));
        
        // Oluştur butonu
        boolean canCreate = startLoc != null && endLoc != null && hasEnoughItems && hasEnoughValue;
        if (canCreate) {
            // Mesafe kontrolü
            if (startLoc.getWorld().equals(endLoc.getWorld())) {
                double distance = startLoc.distance(endLoc);
                int minDistance = balanceConfig != null ? balanceConfig.getCaravanMinDistance() : 1000;
                if (distance < minDistance) {
                    canCreate = false;
                }
            } else {
                canCreate = false;
            }
        }
        
        if (canCreate) {
            menu.setItem(22, createButton(Material.GREEN_CONCRETE, "§a§lKERVAN OLUŞTUR", 
                Arrays.asList("§7Kervanı oluştur ve gönder",
                    "§7Mallarınız hedefe taşınacak")));
        } else {
            menu.setItem(22, createButton(Material.RED_CONCRETE, "§cEksik Bilgi", 
                Arrays.asList("§7Tüm bilgileri doldurun")));
        }
        
        // Geri butonu
        menu.setItem(18, createButton(Material.ARROW, "§7Geri", Arrays.asList("§7Ana menüye dön")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Kervan detay menüsünü aç
     */
    public void openCaravanDetailMenu(Player player, Entity caravan) {
        if (player == null || caravan == null || !caravan.isValid()) return;
        
        Inventory menu = Bukkit.createInventory(null, 54, "§6Kervan Detayları");
        
        // Kervan bilgileri
        UUID ownerId = caravanManager.getOwner(caravan);
        org.bukkit.OfflinePlayer owner = ownerId != null ? Bukkit.getOfflinePlayer(ownerId) : null;
        String ownerName = owner != null && owner.getName() != null ? owner.getName() : "Bilinmeyen";
        
        Location current = caravan.getLocation();
        String status = CaravanHelper.getCaravanStatus(caravan);
        
        // Target location'ı al
        Location target = null;
        try {
            java.lang.reflect.Field targetsField = CaravanManager.class.getDeclaredField("caravanTargets");
            targetsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<UUID, Location> targets = (java.util.Map<UUID, Location>) targetsField.get(caravanManager);
            if (targets != null && ownerId != null) {
                target = targets.get(ownerId);
            }
        } catch (Exception e) {
            // Hata durumunda null kalır
        }
        
        List<String> info = CaravanHelper.getCaravanInfo(caravan, target, balanceConfig);
        
        List<String> lore = new ArrayList<>();
        lore.add("§7═══════════════════════");
        lore.add("§7Sahip: §e" + ownerName);
        lore.add("§7Durum: " + status);
        lore.add("§7Konum: §e" + current.getBlockX() + ", " + current.getBlockY() + ", " + current.getBlockZ());
        lore.add("§7Dünya: §e" + (current.getWorld() != null ? current.getWorld().getName() : "Bilinmeyen"));
        lore.add("§7═══════════════════════");
        lore.addAll(info);
        
        ItemStack item = new ItemStack(Material.CHEST_MINECART);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e§lKervan - " + ownerName);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        menu.setItem(13, item);
        
        // Işınlanma butonu
        menu.setItem(31, createButton(Material.ENDER_PEARL, "§eIşınlan", 
            Arrays.asList("§7Kervanın konumuna ışınlan")));
        
        // Geri butonu
        menu.setItem(45, createButton(Material.ARROW, "§7Geri", Arrays.asList("§7Kervan listesine dön")));
        
        openDetailMenus.put(player.getUniqueId(), caravan);
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        
        if (title.equals("§6Kervan Yönetimi")) {
            handleMainMenuClick(event);
        } else if (title.equals("§6Kervan Oluştur")) {
            handleCreateMenuClick(event);
        } else if (title.equals("§6Kervan Detayları")) {
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
        
        if (slot == 49 && clicked.getType() == Material.EMERALD) {
            // Yeni kervan oluştur
            openCreateCaravanMenu(player);
            return;
        }
        
        if (slot < 45) {
            // Kervan seçildi
            List<Entity> activeCaravans = new ArrayList<>();
            for (UUID memberId : clan.getMembers().keySet()) {
                Entity caravan = caravanManager.getCaravan(memberId);
                if (caravan != null && caravan.isValid()) {
                    activeCaravans.add(caravan);
                }
            }
            
            if (slot < activeCaravans.size()) {
                Entity caravan = activeCaravans.get(slot);
                if (caravan != null) {
                    openCaravanDetailMenu(player, caravan);
                }
            }
        }
    }
    
    private void handleCreateMenuClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        int slot = event.getSlot();
        
        if (slot == 18) {
            // Geri butonu
            openMainMenu(player);
            return;
        }
        
        if (slot == 11 && clicked.getType() == Material.GREEN_CONCRETE) {
            // Başlangıç konumu seç
            pendingStartLocations.put(player.getUniqueId(), player.getLocation());
            player.sendMessage("§aBaşlangıç konumu ayarlandı: §e" + 
                player.getLocation().getBlockX() + ", " + 
                player.getLocation().getBlockY() + ", " + 
                player.getLocation().getBlockZ());
            openCreateCaravanMenu(player); // Menüyü yenile
            return;
        }
        
        if (slot == 15 && clicked.getType() == Material.ORANGE_CONCRETE) {
            // Hedef konumu seç - Chat input iste
            player.closeInventory();
            player.sendMessage("§e§l═══════════════════════════");
            player.sendMessage("§eHedef konumu girin:");
            player.sendMessage("§7Format: §eX Y Z §7(örnek: 100 64 200)");
            player.sendMessage("§7Veya 'iptal' yazarak iptal edin");
            player.sendMessage("§e§l═══════════════════════════");
            
            // Chat listener için işaretle
            plugin.getServer().getPluginManager().registerEvents(
                new org.bukkit.event.Listener() {
                    @EventHandler
                    public void onChat(org.bukkit.event.player.AsyncPlayerChatEvent e) {
                        if (!e.getPlayer().equals(player)) return;
                        e.setCancelled(true);
                        
                        String message = e.getMessage().trim();
                        if (message.equalsIgnoreCase("iptal")) {
                            player.sendMessage("§cİşlem iptal edildi");
                            org.bukkit.event.HandlerList.unregisterAll(this);
                            return;
                        }
                        
                        String[] parts = message.split(" ");
                        if (parts.length == 3) {
                            try {
                                int x = Integer.parseInt(parts[0]);
                                int y = Integer.parseInt(parts[1]);
                                int z = Integer.parseInt(parts[2]);
                                
                                Location endLoc = new Location(player.getWorld(), x, y, z);
                                pendingEndLocations.put(player.getUniqueId(), endLoc);
                                
                                player.sendMessage("§aHedef konumu ayarlandı: §e" + x + ", " + y + ", " + z);
                                openCreateCaravanMenu(player);
                            } catch (NumberFormatException ex) {
                                player.sendMessage("§cGeçersiz format! Örnek: 100 64 200");
                            }
                        } else {
                            player.sendMessage("§cGeçersiz format! X Y Z şeklinde girin");
                        }
                        
                        org.bukkit.event.HandlerList.unregisterAll(this);
                    }
                }, plugin);
            return;
        }
        
        if (slot == 22 && clicked.getType() == Material.GREEN_CONCRETE) {
            // Kervan oluştur
            createCaravan(player);
        }
    }
    
    private void handleDetailMenuClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        Entity caravan = openDetailMenus.get(player.getUniqueId());
        if (caravan == null || !caravan.isValid()) {
            openMainMenu(player);
            return;
        }
        
        int slot = event.getSlot();
        
        if (slot == 45) {
            // Geri butonu
            openMainMenu(player);
            return;
        }
        
        if (slot == 31 && clicked.getType() == Material.ENDER_PEARL) {
            // Işınlanma
            Location loc = caravan.getLocation();
            if (loc != null && loc.getWorld() != null) {
                player.teleport(loc.add(0.5, 1, 0.5));
                player.sendMessage("§aKervanın konumuna ışınlandınız!");
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            }
        }
    }
    
    /**
     * Kervan oluştur
     */
    private void createCaravan(Player player) {
        if (player == null) return;
        
        Location start = pendingStartLocations.get(player.getUniqueId());
        Location end = pendingEndLocations.get(player.getUniqueId());
        
        if (start == null || end == null) {
            player.sendMessage("§cBaşlangıç ve hedef konumları seçilmelidir!");
            return;
        }
        
        // Yükü topla (envanterden)
        List<ItemStack> cargo = new ArrayList<>();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                cargo.add(item.clone());
            }
        }
        
        // Kontroller
        if (!CaravanHelper.canCreateCaravan(player, start, end, cargo, balanceConfig)) {
            player.sendMessage("§cKervan oluşturma gereksinimleri karşılanmıyor!");
            return;
        }
        
        // Kervan oluştur
        boolean success = caravanManager.createCaravan(player, start, end, cargo);
        
        if (success) {
            // Envanterden itemleri kaldır
            for (ItemStack item : cargo) {
                player.getInventory().removeItem(item);
            }
            
            player.sendMessage("§a§lKervan oluşturuldu!");
            player.sendMessage("§7Hedefe ulaştığında mallarınız x1.5 değer kazanacak");
            
            // Temizle
            pendingStartLocations.remove(player.getUniqueId());
            pendingEndLocations.remove(player.getUniqueId());
            
            openMainMenu(player);
        } else {
            player.sendMessage("§cKervan oluşturulamadı!");
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

