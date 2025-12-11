package me.mami.stratocraft.gui;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.manager.StratocraftPowerSystem;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Structure;
import me.mami.stratocraft.util.StructureHelper;
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
import java.util.Map;
import java.util.UUID;

/**
 * Klan Yapıları GUI Menüsü
 * 
 * Özellikler:
 * - Yapı listesi görüntüleme
 * - Yapı detayları
 * - Yapı seviye yükseltme
 * - Yapı güç katkısı gösterimi
 */
public class ClanStructureMenu implements Listener {
    private final Main plugin;
    private final ClanManager clanManager;
    private final StratocraftPowerSystem powerSystem;
    
    // Açık menüler (player -> structure) - Detay menüsü için
    private final java.util.Map<UUID, Structure> openDetailMenus = new java.util.concurrent.ConcurrentHashMap<>();
    
    public ClanStructureMenu(Main plugin, ClanManager clanManager, 
                            StratocraftPowerSystem powerSystem) {
        this.plugin = plugin;
        this.clanManager = clanManager;
        this.powerSystem = powerSystem;
    }
    
    /**
     * Ana yapı menüsünü aç
     */
    public void openMainMenu(Player player) {
        if (player == null) return;
        
        // Manager null kontrolü
        if (clanManager == null) {
            player.sendMessage("§cKlan sistemi aktif değil!");
            plugin.getLogger().warning("ClanManager null! Menü açılamıyor.");
            return;
        }
        
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage("§cBir klana üye değilsiniz!");
            return;
        }
        
        List<Structure> structures = clan.getStructures();
        if (structures == null) {
            structures = new ArrayList<>();
        }
        
        // 54 slotlu menü (6x9) - Sayfalama ile
        Inventory menu = Bukkit.createInventory(null, 54, "§6Klan Yapıları");
        
        // Yapıları listele (45 slot - 0-44)
        int slot = 0;
        for (Structure structure : structures) {
            if (structure == null || slot >= 45) break;
            
            Material icon = StructureHelper.getStructureIcon(structure.getType());
            String name = StructureHelper.getStructureDisplayName(structure.getType());
            List<String> description = StructureHelper.getStructureDescription(structure.getType());
            
            // Güç katkısı
            double powerContribution = StructureHelper.getStructurePowerContribution(structure, powerSystem);
            
            // Lore oluştur
            List<String> lore = new ArrayList<>();
            lore.add("§7═══════════════════════");
            lore.add("§7Seviye: §e" + structure.getLevel() + "§7/§e" + StructureHelper.getMaxLevel(structure.getType()));
            lore.add("§7Güç Katkısı: §e" + String.format("%.1f", powerContribution));
            lore.add("§7Kategori: §e" + StructureHelper.getStructureCategory(structure.getType()));
            lore.add("§7═══════════════════════");
            lore.addAll(description);
            lore.add("§7═══════════════════════");
            lore.add("§aSol Tık: §7Detayları gör");
            if (StructureHelper.canUpgrade(structure, clan, player)) {
                lore.add("§eSağ Tık: §7Yükselt");
            }
            
            ItemStack item = new ItemStack(icon);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§e" + name);
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            
            menu.setItem(slot++, item);
        }
        
        // Bilgi butonu
        menu.setItem(49, createButton(Material.BOOK, "§eBilgi", 
            Arrays.asList("§7Toplam Yapı: §e" + structures.size(),
                "§7Teknoloji Seviyesi: §e" + clan.getTechLevel())));
        
        // Geri butonu
        menu.setItem(45, createButton(Material.ARROW, "§7Geri", Arrays.asList("§7Ana klan menüsüne dön")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Yapı detay menüsünü aç
     */
    public void openStructureDetailMenu(Player player, Structure structure) {
        if (player == null || structure == null) return;
        
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage("§cBir klana üye değilsiniz!");
            return;
        }
        
        // Yapı klana ait mi kontrolü
        if (!clan.getStructures().contains(structure)) {
            player.sendMessage("§cBu yapı klanınıza ait değil!");
            openMainMenu(player);
            return;
        }
        
        Inventory menu = Bukkit.createInventory(null, 54, "§6Yapı Detayları");
        
        // Yapı bilgileri
        Material icon = StructureHelper.getStructureIcon(structure.getType());
        String name = StructureHelper.getStructureDisplayName(structure.getType());
        List<String> description = StructureHelper.getStructureDescription(structure.getType());
        
        // Güç katkısı
        double powerContribution = StructureHelper.getStructurePowerContribution(structure, powerSystem);
        
        // Konum bilgisi
        org.bukkit.Location loc = structure.getLocation();
        String locationStr = "§7Dünya: §e" + (loc.getWorld() != null ? loc.getWorld().getName() : "Bilinmeyen") +
                           " §7| §7X: §e" + loc.getBlockX() +
                           " §7| §7Y: §e" + loc.getBlockY() +
                           " §7| §7Z: §e" + loc.getBlockZ();
        
        // Shield yakıt bilgisi
        int shieldFuel = structure.getShieldFuel();
        boolean shieldActive = structure.isShieldActive();
        
        // Lore oluştur
        List<String> lore = new ArrayList<>();
        lore.add("§7═══════════════════════");
        lore.add("§7Seviye: §e" + structure.getLevel() + "§7/§e" + StructureHelper.getMaxLevel(structure.getType()));
        lore.add("§7Güç Katkısı: §e" + String.format("%.1f", powerContribution));
        lore.add("§7Kategori: §e" + StructureHelper.getStructureCategory(structure.getType()));
        lore.add("§7═══════════════════════");
        lore.add(locationStr);
        lore.add("§7═══════════════════════");
        lore.add("§7Shield Durumu: " + (shieldActive ? "§aAktif" : "§cPasif"));
        if (shieldActive) {
            lore.add("§7Yakıt: §e" + shieldFuel);
        }
        lore.add("§7═══════════════════════");
        lore.addAll(description);
        lore.add("§7═══════════════════════");
        
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e§l" + name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        menu.setItem(13, item);
        
        // Yükseltme butonu (eğer yükseltilebilirse)
        if (StructureHelper.canUpgrade(structure, clan, player)) {
            int nextLevel = structure.getLevel() + 1;
            Map<Material, Integer> upgradeCost = StructureHelper.getUpgradeCost(structure, nextLevel);
            
            List<String> upgradeLore = new ArrayList<>();
            upgradeLore.add("§7═══════════════════════");
            upgradeLore.add("§7Hedef Seviye: §e" + nextLevel);
            upgradeLore.add("§7═══════════════════════");
            upgradeLore.add("§7Gerekli Malzemeler:");
            for (Map.Entry<Material, Integer> entry : upgradeCost.entrySet()) {
                upgradeLore.add("§7- §e" + entry.getValue() + "x §7" + getMaterialDisplayName(entry.getKey()));
            }
            upgradeLore.add("§7═══════════════════════");
            upgradeLore.add("§aSol Tık: §7Yükselt");
            
            menu.setItem(31, createButton(Material.ANVIL, "§e§lYÜKSELT", upgradeLore));
        } else {
            if (structure.getLevel() >= StructureHelper.getMaxLevel(structure.getType())) {
                menu.setItem(31, createButton(Material.BARRIER, "§cMaksimum Seviye", 
                    Arrays.asList("§7Bu yapı maksimum seviyede")));
            } else {
                menu.setItem(31, createButton(Material.BARRIER, "§cYükseltilemez", 
                    Arrays.asList("§7Yükseltme için yetkiniz yok")));
            }
        }
        
        // Işınlanma butonu (konuma git)
        menu.setItem(40, createButton(Material.ENDER_PEARL, "§eIşınlan", 
            Arrays.asList("§7Yapının konumuna ışınlan",
                "§7(Teleportasyon özelliği)")));
        
        // Geri butonu
        menu.setItem(45, createButton(Material.ARROW, "§7Geri", Arrays.asList("§7Yapı listesine dön")));
        
        openDetailMenus.put(player.getUniqueId(), structure);
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Yükseltme menüsünü aç
     */
    public void openUpgradeMenu(Player player, Structure structure) {
        if (player == null || structure == null) return;
        
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage("§cBir klana üye değilsiniz!");
            return;
        }
        
        // Yükseltme kontrolü
        if (!StructureHelper.canUpgrade(structure, clan, player)) {
            player.sendMessage("§cBu yapı yükseltilemez!");
            openStructureDetailMenu(player, structure);
            return;
        }
        
        int currentLevel = structure.getLevel();
        int targetLevel = currentLevel + 1;
        int maxLevel = StructureHelper.getMaxLevel(structure.getType());
        
        if (targetLevel > maxLevel) {
            player.sendMessage("§cMaksimum seviyeye ulaşıldı!");
            openStructureDetailMenu(player, structure);
            return;
        }
        
        Inventory menu = Bukkit.createInventory(null, 54, "§6Yapı Yükseltme");
        
        // Yapı bilgisi
        Material icon = StructureHelper.getStructureIcon(structure.getType());
        String name = StructureHelper.getStructureDisplayName(structure.getType());
        
        List<String> structureLore = new ArrayList<>();
        structureLore.add("§7═══════════════════════");
        structureLore.add("§7Mevcut Seviye: §e" + currentLevel);
        structureLore.add("§7Hedef Seviye: §e" + targetLevel);
        structureLore.add("§7═══════════════════════");
        
        menu.setItem(13, createButton(icon, "§e" + name, structureLore));
        
        // Maliyet listesi
        Map<Material, Integer> upgradeCost = StructureHelper.getUpgradeCost(structure, targetLevel);
        
        int slot = 28;
        for (Map.Entry<Material, Integer> entry : upgradeCost.entrySet()) {
            if (slot >= 45) break;
            
            Material material = entry.getKey();
            int amount = entry.getValue();
            
            // Oyuncunun envanterinde var mı kontrolü
            int playerAmount = countItemInInventory(player, material);
            boolean hasEnough = playerAmount >= amount;
            
            List<String> costLore = new ArrayList<>();
            costLore.add("§7═══════════════════════");
            costLore.add("§7Gerekli: §e" + amount);
            costLore.add("§7Sahip: §" + (hasEnough ? "a" : "c") + playerAmount);
            costLore.add("§7═══════════════════════");
            if (!hasEnough) {
                costLore.add("§cYetersiz malzeme!");
            } else {
                costLore.add("§aYeterli malzeme var");
            }
            
            ItemStack costItem = new ItemStack(material, amount);
            ItemMeta costMeta = costItem.getItemMeta();
            if (costMeta != null) {
                costMeta.setDisplayName("§e" + getMaterialDisplayName(material));
                costMeta.setLore(costLore);
                costItem.setItemMeta(costMeta);
            }
            
            menu.setItem(slot++, costItem);
        }
        
        // Onay butonu
        boolean canUpgrade = upgradeCost.entrySet().stream()
            .allMatch(entry -> countItemInInventory(player, entry.getKey()) >= entry.getValue());
        
        if (canUpgrade) {
            menu.setItem(49, createButton(Material.GREEN_CONCRETE, "§a§lYÜKSELT", 
                Arrays.asList("§7Yapıyı seviye " + targetLevel + " yükselt",
                    "§7Malzemeler envanterden alınacak")));
        } else {
            menu.setItem(49, createButton(Material.RED_CONCRETE, "§cYetersiz Malzeme", 
                Arrays.asList("§7Tüm malzemeleri toplayın")));
        }
        
        // Geri butonu
        menu.setItem(45, createButton(Material.ARROW, "§7Geri", Arrays.asList("§7Yapı detaylarına dön")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        
        if (title.equals("§6Klan Yapıları")) {
            handleMainMenuClick(event);
        } else if (title.equals("§6Yapı Detayları")) {
            handleDetailMenuClick(event);
        } else if (title.equals("§6Yapı Yükseltme")) {
            handleUpgradeMenuClick(event);
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
        
        if (slot == 45) {
            // Geri butonu
            if (plugin.getClanMenu() != null) {
                plugin.getClanMenu().openMenu(player);
            }
            return;
        }
        
        if (slot < 45) {
            // Yapı seçildi
            List<Structure> structures = clan.getStructures();
            if (structures != null && slot < structures.size()) {
                Structure structure = structures.get(slot);
                if (structure != null) {
                    if (event.isLeftClick()) {
                        openStructureDetailMenu(player, structure);
                    } else if (event.isRightClick() && StructureHelper.canUpgrade(structure, clan, player)) {
                        openUpgradeMenu(player, structure);
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
        
        Structure structure = openDetailMenus.get(player.getUniqueId());
        if (structure == null) {
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
        
        if (slot == 31) {
            // Yükseltme butonu
            if (StructureHelper.canUpgrade(structure, clan, player)) {
                openUpgradeMenu(player, structure);
            }
        } else         if (slot == 40) {
            // Işınlanma butonu
            org.bukkit.Location loc = structure.getLocation();
            if (loc == null) {
                player.sendMessage("§cYapı konumu geçersiz!");
                return;
            }
            
            if (loc.getWorld() == null) {
                player.sendMessage("§cYapı dünyası geçersiz!");
                return;
            }
            
            player.teleport(loc.add(0.5, 1, 0.5));
            player.sendMessage("§aYapının konumuna ışınlandınız!");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        }
    }
    
    private void handleUpgradeMenuClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        Structure structure = openDetailMenus.get(player.getUniqueId());
        if (structure == null) {
            openMainMenu(player);
            return;
        }
        
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) return;
        
        int slot = event.getSlot();
        
        if (slot == 45) {
            // Geri butonu
            openStructureDetailMenu(player, structure);
            return;
        }
        
        if (slot == 49 && clicked.getType() == Material.GREEN_CONCRETE) {
            // Yükseltme onayı
            upgradeStructure(player, structure, clan);
        }
    }
    
    /**
     * Yapıyı yükselt
     */
    private void upgradeStructure(Player player, Structure structure, Clan clan) {
        if (player == null || structure == null || clan == null) return;
        
        // Yükseltme kontrolü
        if (!StructureHelper.canUpgrade(structure, clan, player)) {
            player.sendMessage("§cBu yapı yükseltilemez!");
            return;
        }
        
        int currentLevel = structure.getLevel();
        int targetLevel = currentLevel + 1;
        int maxLevel = StructureHelper.getMaxLevel(structure.getType());
        
        if (targetLevel > maxLevel) {
            player.sendMessage("§cMaksimum seviyeye ulaşıldı!");
            return;
        }
        
        // Maliyet kontrolü
        Map<Material, Integer> upgradeCost = StructureHelper.getUpgradeCost(structure, targetLevel);
        
        for (Map.Entry<Material, Integer> entry : upgradeCost.entrySet()) {
            int required = entry.getValue();
            int available = countItemInInventory(player, entry.getKey());
            
            if (available < required) {
                player.sendMessage("§cYetersiz malzeme: " + getMaterialDisplayName(entry.getKey()) + 
                                 " (Gerekli: " + required + ", Sahip: " + available + ")");
                return;
            }
        }
        
        // Malzemeleri tüket
        for (Map.Entry<Material, Integer> entry : upgradeCost.entrySet()) {
            removeItemsFromInventory(player, entry.getKey(), entry.getValue());
        }
        
        // Yapıyı yükselt
        structure.setLevel(targetLevel);
        
        // Başarı mesajı ve efektler
        player.sendMessage("§a§l" + StructureHelper.getStructureDisplayName(structure.getType()) + 
                          " seviye " + targetLevel + " yükseltildi!");
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
        player.getWorld().spawnParticle(org.bukkit.Particle.TOTEM, 
            structure.getLocation().add(0.5, 1, 0.5), 50, 0.5, 0.5, 0.5, 0.3);
        
        // Menüyü yenile
        openStructureDetailMenu(player, structure);
    }
    
    /**
     * Envanterde item sayısını say
     */
    private int countItemInInventory(Player player, Material material) {
        if (player == null || material == null) return 0;
        
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }
    
    /**
     * Envanterden item kaldır
     */
    private void removeItemsFromInventory(Player player, Material material, int amount) {
        if (player == null || material == null || amount <= 0) return;
        
        int remaining = amount;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                int itemAmount = item.getAmount();
                if (itemAmount <= remaining) {
                    player.getInventory().removeItem(item);
                    remaining -= itemAmount;
                } else {
                    item.setAmount(itemAmount - remaining);
                    remaining = 0;
                }
                if (remaining <= 0) break;
            }
        }
    }
    
    /**
     * Material'in Türkçe ismini döndür
     */
    private String getMaterialDisplayName(Material material) {
        if (material == null) return "Bilinmeyen";
        
        // Basit çeviri (gerekirse genişletilebilir)
        switch (material) {
            case IRON_INGOT:
                return "Demir Külçesi";
            case GOLD_INGOT:
                return "Altın Külçesi";
            case DIAMOND:
                return "Elmas";
            case OBSIDIAN:
                return "Obsidyen";
            case STONE_BRICKS:
                return "Taş Tuğla";
            case IRON_BLOCK:
                return "Demir Bloğu";
            case REDSTONE:
                return "Kızıltaş";
            case BREWING_STAND:
                return "Simya Masası";
            case BLAZE_POWDER:
                return "Blaze Tozu";
            case PRISMARINE:
                return "Prismarine";
            case SPIDER_EYE:
                return "Örümcek Gözü";
            case END_ROD:
                return "End Çubuğu";
            case STONE:
                return "Taş";
            default:
                return material.name();
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

