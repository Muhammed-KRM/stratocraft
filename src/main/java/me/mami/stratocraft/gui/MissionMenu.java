package me.mami.stratocraft.gui;

import me.mami.stratocraft.manager.MissionManager;
import me.mami.stratocraft.model.Mission;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;

public class MissionMenu {
    
    /**
     * Ana Görev Menüsü
     */
    public static void openMenu(Player player, Mission mission, MissionManager missionManager) {
        Inventory menu = Bukkit.createInventory(null, 27, "§eGörev Menüsü");
        
        if (mission == null) {
            // Görev yok - Yeni görev al butonu
            menu.setItem(13, createButton(Material.EMERALD, "§aYeni Görev Al", 
                "§7Totem'e sağ tık yap"));
            player.openInventory(menu);
            return;
        }
        
        // Aktif görev bilgileri
        ItemStack missionItem = new ItemStack(Material.BOOK);
        ItemMeta meta = missionItem.getItemMeta();
        meta.setDisplayName("§e" + getMissionTypeName(mission.getType()));
        List<String> lore = new ArrayList<>();
        lore.add("§7Zorluk: §e" + mission.getDifficulty().name());
        
        // İlerleme gösterimi
        if (mission.getType() == Mission.Type.TRAVEL_DISTANCE) {
            int progressPercent = (int) ((mission.getTravelProgress() * 100) / mission.getTargetDistance());
            lore.add("§7İlerleme: §a" + String.format("%.0f", mission.getTravelProgress()) + 
                    "§7/§a" + mission.getTargetDistance() + " blok");
            lore.add("§7Yüzde: §e" + progressPercent + "%");
        } else {
            lore.add("§7İlerleme: §a" + mission.getProgress() + "§7/§a" + mission.getTargetAmount());
        }
        
        // Süre bilgisi
        long remaining = mission.getDeadline() - System.currentTimeMillis();
        if (remaining > 0) {
            lore.add("§7Kalan Süre: §e" + formatTime(remaining));
        } else {
            lore.add("§cSüre Doldu!");
        }
        
        lore.add("");
        
        // Tip'e göre hedef bilgisi
        switch (mission.getType()) {
            case KILL_MOB:
                lore.add("§7Hedef: §c" + (mission.getTargetEntity() != null ? mission.getTargetEntity().name() : "Bilinmeyen"));
                break;
            case GATHER_ITEM:
            case CRAFT_ITEM:
            case MINE_BLOCK:
                lore.add("§7Hedef: §e" + (mission.getTargetMaterial() != null ? mission.getTargetMaterial().name() : "Bilinmeyen"));
                break;
            case VISIT_LOCATION:
                if (mission.getTargetLocation() != null) {
                    lore.add("§7Hedef: §e" + mission.getTargetLocation().getBlockX() + ", " + 
                            mission.getTargetLocation().getBlockZ());
                    lore.add("§7Mesafe: §e" + String.format("%.0f", 
                            player.getLocation().distance(mission.getTargetLocation())) + " blok");
                }
                break;
            case BUILD_STRUCTURE:
                lore.add("§7Hedef: §e" + (mission.getStructureType() != null ? mission.getStructureType() : "Bilinmeyen"));
                break;
            case KILL_PLAYER:
                if (mission.getTargetPlayer() != null) {
                    String targetName = Bukkit.getOfflinePlayer(mission.getTargetPlayer()).getName();
                    lore.add("§7Hedef: §c" + (targetName != null ? targetName : "Bilinmeyen"));
                }
                break;
            case TRAVEL_DISTANCE:
                lore.add("§7Hedef: §e" + mission.getTargetDistance() + " blok kat et");
                break;
        }
        
        lore.add("");
        
        // Ödül bilgisi
        if (mission.getRewardMoney() > 0) {
            lore.add("§7Ödül: §a" + mission.getRewardMoney() + " Altın");
        }
        if (mission.getReward() != null) {
            lore.add("§7+ " + mission.getReward().getType().name() + " x" + mission.getReward().getAmount());
        }
        
        meta.setLore(lore);
        missionItem.setItemMeta(meta);
        menu.setItem(13, missionItem);
        
        // İlerleme barı (görsel) - Slot 0-8
        int progressPercent;
        if (mission.getType() == Mission.Type.TRAVEL_DISTANCE) {
            progressPercent = (int) ((mission.getTravelProgress() * 100) / mission.getTargetDistance());
        } else {
            progressPercent = (mission.getProgress() * 100) / mission.getTargetAmount();
        }
        int filledSlots = (progressPercent * 9) / 100;
        for (int i = 0; i < 9; i++) {
            if (i < filledSlots) {
                menu.setItem(i, new ItemStack(Material.GREEN_STAINED_GLASS_PANE));
            } else {
                menu.setItem(i, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
            }
        }
        
        // Ödül önizleme
        if (mission.getReward() != null) {
            menu.setItem(15, mission.getReward());
        }
        
        // Tamamlandıysa teslim et butonu
        if (mission.isCompleted()) {
            menu.setItem(22, createButton(Material.EMERALD_BLOCK, "§a[Teslim Et]", 
                "§7Ödülü al"));
        }
        
        // Kapat butonu
        menu.setItem(26, createButton(Material.BARRIER, "§cKapat", null));
        
        player.openInventory(menu);
    }
    
    /**
     * Görev tipi ismini al
     */
    private static String getMissionTypeName(Mission.Type type) {
        switch (type) {
            case KILL_MOB:
                return "Mob Avı";
            case GATHER_ITEM:
                return "Malzeme Toplama";
            case VISIT_LOCATION:
                return "Lokasyon Ziyareti";
            case BUILD_STRUCTURE:
                return "Yapı İnşa";
            case KILL_PLAYER:
                return "Oyuncu Avı";
            case CRAFT_ITEM:
                return "Item Craft";
            case MINE_BLOCK:
                return "Blok Kazma";
            case TRAVEL_DISTANCE:
                return "Mesafe Kat Etme";
            default:
                return "Bilinmeyen Görev";
        }
    }
    
    /**
     * Yardımcı metod: Buton oluştur
     */
    private static ItemStack createButton(Material material, String name, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (description != null) {
            List<String> lore = new ArrayList<>();
            lore.add(description);
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Zaman formatla
     */
    private static String formatTime(long timeMillis) {
        long totalSeconds = timeMillis / 1000;
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        
        if (days > 0) {
            return days + "g " + hours + "s " + minutes + "d";
        } else if (hours > 0) {
            return hours + "s " + minutes + "d " + seconds + "sn";
        } else if (minutes > 0) {
            return minutes + "d " + seconds + "sn";
        } else {
            return seconds + "sn";
        }
    }
}

