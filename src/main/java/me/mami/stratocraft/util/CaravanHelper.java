package me.mami.stratocraft.util;

import me.mami.stratocraft.manager.GameBalanceConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mule;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kervan Sistemi Yardımcı Sınıfı
 * 
 * Kervanlar için yardımcı fonksiyonlar sağlar:
 * - Yük değeri hesaplama
 * - Rota kontrolü
 * - Durum bilgisi
 * - Tahmini varış süresi
 */
public class CaravanHelper {
    
    /**
     * Yük değerini hesapla (basit fiyat sistemi)
     */
    public static double calculateCargoValue(List<ItemStack> cargo) {
        if (cargo == null || cargo.isEmpty()) return 0.0;
        
        double totalValue = 0.0;
        Map<Material, Double> itemValues = getItemValues();
        
        for (ItemStack item : cargo) {
            if (item == null || item.getType() == Material.AIR) continue;
            
            Material material = item.getType();
            int amount = item.getAmount();
            double unitValue = itemValues.getOrDefault(material, 1.0);
            
            totalValue += unitValue * amount;
        }
        
        return totalValue;
    }
    
    /**
     * Item değerleri (basit fiyat sistemi)
     */
    private static Map<Material, Double> getItemValues() {
        Map<Material, Double> values = new HashMap<>();
        
        // Temel malzemeler
        values.put(Material.IRON_INGOT, 5.0);
        values.put(Material.GOLD_INGOT, 10.0);
        values.put(Material.DIAMOND, 50.0);
        values.put(Material.EMERALD, 30.0);
        values.put(Material.COAL, 1.0);
        values.put(Material.REDSTONE, 2.0);
        values.put(Material.LAPIS_LAZULI, 3.0);
        
        // Bloklar (malzeme değerinin 9 katı)
        values.put(Material.IRON_BLOCK, 45.0);
        values.put(Material.GOLD_BLOCK, 90.0);
        values.put(Material.DIAMOND_BLOCK, 450.0);
        values.put(Material.EMERALD_BLOCK, 270.0);
        
        // Özel malzemeler (Stratocraft)
        values.put(Material.NETHERITE_INGOT, 100.0);
        values.put(Material.NETHERITE_BLOCK, 900.0);
        
        return values;
    }
    
    /**
     * Rota kontrolü
     */
    public static boolean validateCaravanRoute(Location start, Location end, 
                                               GameBalanceConfig balanceConfig) {
        if (start == null || end == null) return false;
        
        // Dünya kontrolü
        if (!start.getWorld().equals(end.getWorld())) {
            return false;
        }
        
        // Mesafe kontrolü
        double distance = start.distance(end);
        int minDistance = balanceConfig != null ? balanceConfig.getCaravanMinDistance() : 1000;
        
        if (distance < minDistance) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Kervan durum bilgisi
     */
    public static String getCaravanStatus(Entity caravan) {
        if (caravan == null || !caravan.isValid()) {
            return "§cYok";
        }
        
        if (caravan instanceof Mule) {
            Mule mule = (Mule) caravan;
            if (mule.isTamed()) {
                return "§aAktif";
            }
        }
        
        return "§cPasif";
    }
    
    /**
     * Tahmini varış süresi (mesafeye göre)
     */
    public static String getEstimatedArrivalTime(Location start, Location end) {
        if (start == null || end == null) return "Bilinmeyen";
        
        if (!start.getWorld().equals(end.getWorld())) {
            return "§cFarklı dünya";
        }
        
        double distance = start.distance(end);
        
        // Kervan hızı: ~5 blok/saniye (varsayılan)
        double speed = 5.0; // blok/saniye
        double timeSeconds = distance / speed;
        
        long minutes = (long) (timeSeconds / 60);
        long seconds = (long) (timeSeconds % 60);
        
        if (minutes > 0) {
            return "§e" + minutes + " dakika " + seconds + " saniye";
        } else {
            return "§e" + seconds + " saniye";
        }
    }
    
    /**
     * Yük miktarı kontrolü
     */
    public static boolean validateCargoAmount(List<ItemStack> cargo, GameBalanceConfig balanceConfig) {
        if (cargo == null || cargo.isEmpty()) return false;
        
        int totalItems = 0;
        for (ItemStack item : cargo) {
            if (item != null && item.getType() != Material.AIR) {
                totalItems += item.getAmount();
            }
        }
        
        int minStacks = balanceConfig != null ? balanceConfig.getCaravanMinStacks() : 20;
        int minItems = minStacks * 64;
        
        return totalItems >= minItems;
    }
    
    /**
     * Yük değeri kontrolü
     */
    public static boolean validateCargoValue(List<ItemStack> cargo, GameBalanceConfig balanceConfig) {
        if (cargo == null || cargo.isEmpty()) return false;
        
        double totalValue = calculateCargoValue(cargo);
        double minValue = balanceConfig != null ? balanceConfig.getCaravanMinValue() : 5000.0;
        
        return totalValue >= minValue;
    }
    
    /**
     * Kervan oluşturulabilir mi kontrol et
     */
    public static boolean canCreateCaravan(org.bukkit.entity.Player player, Location start, Location end,
                                          List<ItemStack> cargo, GameBalanceConfig balanceConfig) {
        if (player == null || start == null || end == null || cargo == null) {
            return false;
        }
        
        // Rota kontrolü
        if (!validateCaravanRoute(start, end, balanceConfig)) {
            return false;
        }
        
        // Yük miktarı kontrolü
        if (!validateCargoAmount(cargo, balanceConfig)) {
            return false;
        }
        
        // Yük değeri kontrolü
        if (!validateCargoValue(cargo, balanceConfig)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Kervan bilgilerini döndür
     */
    public static List<String> getCaravanInfo(Entity caravan, Location target, 
                                             GameBalanceConfig balanceConfig) {
        List<String> info = new ArrayList<>();
        
        if (caravan == null || !caravan.isValid()) {
            info.add("§cKervan bulunamadı");
            return info;
        }
        
        Location current = caravan.getLocation();
        String status = getCaravanStatus(caravan);
        String arrivalTime = target != null ? getEstimatedArrivalTime(current, target) : "Bilinmeyen";
        
        info.add("§7═══════════════════════");
        info.add("§7Durum: " + status);
        info.add("§7Tahmini Varış: " + arrivalTime);
        
        if (caravan instanceof Mule) {
            Mule mule = (Mule) caravan;
            int cargoSlots = 0;
            int totalItems = 0;
            
            for (ItemStack item : mule.getInventory().getContents()) {
                if (item != null && item.getType() != Material.AIR) {
                    cargoSlots++;
                    totalItems += item.getAmount();
                }
            }
            
            double cargoValue = calculateCargoValue(java.util.Arrays.asList(mule.getInventory().getContents()));
            double rewardMultiplier = balanceConfig != null ? balanceConfig.getCaravanRewardMultiplier() : 1.5;
            double estimatedReward = cargoValue * rewardMultiplier;
            
            info.add("§7Yük Slotları: §e" + cargoSlots);
            info.add("§7Toplam Item: §e" + totalItems);
            info.add("§7Yük Değeri: §e" + String.format("%.1f", cargoValue));
            info.add("§7Tahmini Ödül: §e" + String.format("%.1f", estimatedReward));
        }
        
        info.add("§7═══════════════════════");
        
        return info;
    }
}

