package me.mami.stratocraft.gui;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.TrainingManager;
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
 * Eğitim GUI Menüsü
 * 
 * Özellikler:
 * - Antrenman ilerlemesini görüntüleme
 * - Mastery seviyeleri
 * - Ritüel/batarya antrenman durumu
 */
public class TrainingMenu implements Listener {
    private final Main plugin;
    private final TrainingManager trainingManager;
    
    public TrainingMenu(Main plugin, TrainingManager trainingManager) {
        this.plugin = plugin;
        this.trainingManager = trainingManager;
    }
    
    /**
     * Ana eğitim menüsünü aç
     */
    public void openMainMenu(Player player) {
        if (player == null) return;
        
        UUID playerId = player.getUniqueId();
        
        // Tüm bataryaları/ritüelleri listele
        List<TrainingItem> trainingItems = getTrainingItems(playerId);
        
        Inventory menu = Bukkit.createInventory(null, 54, "§6Eğitim İlerlemesi");
        
        // Eğitim öğelerini listele (45 slot - 0-44)
        int slot = 0;
        for (TrainingItem item : trainingItems) {
            if (item == null || slot >= 45) break;
            
            Material icon = getBatteryIcon(item.ritualId);
            String name = getBatteryDisplayName(item.ritualId);
            
            int masteryLevel = trainingManager.getMasteryLevel(playerId, item.ritualId);
            double progress = trainingManager.getTrainingProgress(playerId, item.ritualId);
            int totalUses = trainingManager.getTotalUses(playerId, item.ritualId);
            int requiredUses = trainingManager.getRequiredUses(item.ritualId);
            int remainingUses = trainingManager.getRemainingUses(playerId, item.ritualId);
            
            List<String> lore = new ArrayList<>();
            lore.add("§7═══════════════════════");
            
            if (masteryLevel == -1) {
                // Antrenman modu
                lore.add("§c§lANTREMAN MODU");
                lore.add("§7İlerleme: §e" + String.format("%.1f", progress * 100) + "%");
                lore.add("§7Kullanım: §e" + totalUses + "§7/§e" + requiredUses);
                lore.add("§7Kalan: §c" + remainingUses + " kullanım");
                lore.add("§7Güç: §c%20 (Antrenman)");
            } else if (masteryLevel == 0) {
                // Antrenman tamamlandı
                lore.add("§a§lANTREMAN TAMAMLANDI");
                lore.add("§7Toplam Kullanım: §e" + totalUses);
                lore.add("§7Güç: §a%100 (Normal)");
                lore.add("§7Mastery: §7Henüz başlamadı");
            } else {
                // Mastery seviyesi var
                String masteryName = getMasteryName(masteryLevel);
                double masteryMultiplier = trainingManager.getMasteryMultiplier(playerId, item.ritualId);
                int masteryPower = (int) (masteryMultiplier * 100);
                
                lore.add("§6§lMASTERY SEVİYESİ: §e" + masteryLevel);
                lore.add("§7Seviye: §e" + masteryName);
                lore.add("§7Toplam Kullanım: §e" + totalUses);
                lore.add("§7Güç: §6%" + masteryPower + " (" + masteryName + ")");
                
                // Sonraki seviye için gereken kullanım
                int nextLevelUses = getNextMasteryLevelUses(masteryLevel);
                if (nextLevelUses > 0) {
                    int remainingForNext = nextLevelUses - totalUses;
                    lore.add("§7Sonraki Seviye: §e" + remainingForNext + " kullanım");
                }
            }
            
            lore.add("§7═══════════════════════");
            
            ItemStack itemStack = new ItemStack(icon);
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§e" + name);
                meta.setLore(lore);
                itemStack.setItemMeta(meta);
            }
            
            menu.setItem(slot++, itemStack);
        }
        
        // Bilgi butonu
        menu.setItem(45, createButton(Material.BOOK, "§eBilgi", 
            Arrays.asList("§7Toplam Ritüel: §e" + trainingItems.size(),
                "§7Antrenman ilerlemenizi görüntüleyin")));
        
        // Geri butonu
        menu.setItem(53, createButton(Material.ARROW, "§7Geri", Arrays.asList("§7Ana menüye dön")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Eğitim öğelerini getir
     */
    private List<TrainingItem> getTrainingItems(UUID playerId) {
        List<TrainingItem> items = new ArrayList<>();
        
        // Bilinen batarya/ritüel ID'leri
        String[] knownRituals = {
            "MAGMA_BATTERY", "LIGHTNING_BATTERY", "INSTANT_BRIDGE", "INSTANT_BUNKER",
            "BLACK_HOLE", "GRAVITY_ANCHOR", "EARTH_WALL",
            "MAGNETIC_DISRUPTOR", "OZONE_SHIELD", "ENERGY_WALL",
            "SEISMIC_HAMMER", "LAVA_TRENCH"
        };
        
        for (String ritualId : knownRituals) {
            // Oyuncunun bu ritüel için kaydı varsa veya antrenman gerektiriyorsa ekle
            int requiredUses = trainingManager.getRequiredUses(ritualId);
            if (requiredUses > 0) {
                items.add(new TrainingItem(ritualId));
            }
        }
        
        // Yeni batarya sistemi için seviye bazlı bataryalar
        for (int level = 1; level <= 5; level++) {
            // Seviye bazlı bataryalar için genel bir giriş ekle
            items.add(new TrainingItem("BATTERY_L" + level));
        }
        
        return items;
    }
    
    /**
     * Batarya ikonunu döndür
     */
    private Material getBatteryIcon(String ritualId) {
        if (ritualId == null) return Material.BARRIER;
        
        String upper = ritualId.toUpperCase();
        if (upper.contains("MAGMA") || upper.contains("LAVA")) return Material.LAVA_BUCKET;
        if (upper.contains("LIGHTNING") || upper.contains("ELECTRIC")) return Material.LIGHTNING_ROD;
        if (upper.contains("BRIDGE")) return Material.OAK_PLANKS;
        if (upper.contains("BUNKER")) return Material.OBSIDIAN;
        if (upper.contains("BLACK_HOLE")) return Material.END_PORTAL_FRAME;
        if (upper.contains("GRAVITY")) return Material.ANVIL;
        if (upper.contains("EARTH") || upper.contains("WALL")) return Material.STONE;
        if (upper.contains("MAGNETIC")) return Material.IRON_INGOT;
        if (upper.contains("OZONE") || upper.contains("SHIELD")) return Material.SHIELD;
        if (upper.contains("ENERGY")) return Material.REDSTONE;
        if (upper.contains("SEISMIC") || upper.contains("HAMMER")) return Material.IRON_PICKAXE;
        if (upper.contains("BATTERY_L")) {
            int level = extractLevel(ritualId);
            switch (level) {
                case 1: return Material.COAL;
                case 2: return Material.IRON_INGOT;
                case 3: return Material.GOLD_INGOT;
                case 4: return Material.DIAMOND;
                case 5: return Material.NETHERITE_INGOT;
            }
        }
        
        return Material.BEACON;
    }
    
    /**
     * Batarya görünen adını döndür
     */
    private String getBatteryDisplayName(String ritualId) {
        if (ritualId == null) return "Bilinmeyen Ritüel";
        
        String upper = ritualId.toUpperCase();
        if (upper.contains("BATTERY_L")) {
            int level = extractLevel(ritualId);
            return "Batarya Seviye " + level;
        }
        
        // Türkçe isimler
        switch (upper) {
            case "MAGMA_BATTERY": return "Magma Bataryası";
            case "LIGHTNING_BATTERY": return "Yıldırım Bataryası";
            case "INSTANT_BRIDGE": return "Anında Köprü";
            case "INSTANT_BUNKER": return "Anında Sığınak";
            case "BLACK_HOLE": return "Kara Delik";
            case "GRAVITY_ANCHOR": return "Yerçekimi Çapası";
            case "EARTH_WALL": return "Toprak Duvarı";
            case "MAGNETIC_DISRUPTOR": return "Manyetik Bozucu";
            case "OZONE_SHIELD": return "Ozon Kalkanı";
            case "ENERGY_WALL": return "Enerji Duvarı";
            case "SEISMIC_HAMMER": return "Sismik Çekiç";
            case "LAVA_TRENCH": return "Lav Çukuru";
            default:
                return ritualId.replace("_", " ");
        }
    }
    
    /**
     * Mastery seviye adını döndür
     */
    private String getMasteryName(int level) {
        switch (level) {
            case 1: return "Usta";
            case 2: return "Uzman";
            case 3: return "Efsanevi";
            default: return "Bilinmeyen";
        }
    }
    
    /**
     * Sonraki mastery seviyesi için gereken kullanım sayısını döndür
     */
    private int getNextMasteryLevelUses(int currentLevel) {
        switch (currentLevel) {
            case 0: return 20; // Seviye 1 için
            case 1: return 40; // Seviye 2 için
            case 2: return 50; // Seviye 3 için
            case 3: return -1; // Maksimum seviye
            default: return -1;
        }
    }
    
    /**
     * Seviye numarasını çıkar
     */
    private int extractLevel(String ritualId) {
        if (ritualId == null) return 1;
        try {
            if (ritualId.contains("_L")) {
                String levelStr = ritualId.substring(ritualId.indexOf("_L") + 2);
                return Integer.parseInt(levelStr);
            }
        } catch (Exception e) {
            // Hata durumunda varsayılan
        }
        return 1;
    }
    
    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        
        if (title.equals("§6Eğitim İlerlemesi")) {
            handleMainMenuClick(event);
        }
    }
    
    private void handleMainMenuClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        int slot = event.getSlot();
        
        if (slot == 53) {
            // Geri butonu
            if (plugin.getClanMenu() != null) {
                plugin.getClanMenu().openMenu(player);
            }
            return;
        }
    }
    
    /**
     * Eğitim öğesi veri sınıfı
     */
    private static class TrainingItem {
        final String ritualId;
        
        TrainingItem(String ritualId) {
            this.ritualId = ritualId;
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

