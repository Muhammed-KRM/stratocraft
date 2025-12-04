package me.mami.stratocraft.gui;

import me.mami.stratocraft.model.Contract;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;

public class ContractMenu {
    
    /**
     * Ana Sözleşme Menüsü (54 slot - sayfalama)
     */
    public static Inventory createMainMenu(List<Contract> contracts, int page) {
        Inventory menu = Bukkit.createInventory(null, 54, "§6Aktif Sözleşmeler - Sayfa " + page);
        
        int startIndex = (page - 1) * 45; // Her sayfada 45 sözleşme
        int endIndex = Math.min(startIndex + 45, contracts.size());
        
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            Contract contract = contracts.get(i);
            menu.setItem(slot, createContractItem(contract));
            slot++;
        }
        
        // Sayfalama butonları
        if (page > 1) {
            menu.setItem(45, createButton(Material.ARROW, "§eÖnceki Sayfa", "§7Sayfa " + (page - 1)));
        }
        if (endIndex < contracts.size()) {
            menu.setItem(53, createButton(Material.ARROW, "§eSonraki Sayfa", "§7Sayfa " + (page + 1)));
        }
        
        menu.setItem(49, createButton(Material.BARRIER, "§cKapat", null));
        
        return menu;
    }
    
    /**
     * Sözleşme Detay Menüsü
     */
    public static Inventory createDetailMenu(Contract contract) {
        Inventory menu = Bukkit.createInventory(null, 27, "§6Sözleşme Detayları");
        
        // Sözleşme bilgileri
        ItemStack infoItem = new ItemStack(Material.PAPER);
        ItemMeta meta = infoItem.getItemMeta();
        meta.setDisplayName("§e" + getContractTypeName(contract.getType()));
        List<String> lore = new ArrayList<>();
        lore.add("§7İssuer: §e" + Bukkit.getOfflinePlayer(contract.getIssuer()).getName());
        lore.add("§7Ödül: §a" + contract.getReward() + " Altın");
        lore.add("§7Cezası: §c" + contract.getPenalty() + " Altın");
        lore.add("§7Süre: §e" + formatTime(contract.getDeadline()));
        
        // Tip'e göre özel bilgiler
        switch (contract.getType()) {
            case PLAYER_KILL:
                if (contract.getTargetPlayer() != null) {
                    lore.add("§7Hedef: §c" + Bukkit.getOfflinePlayer(contract.getTargetPlayer()).getName());
                }
                break;
            case TERRITORY_RESTRICT:
                lore.add("§7Yasak Bölgeler: §c" + contract.getRestrictedAreas().size() + " adet");
                lore.add("§7Yarıçap: §e" + contract.getRestrictedRadius() + " blok");
                break;
            case NON_AGGRESSION:
                if (contract.getNonAggressionTarget() != null) {
                    lore.add("§7Hedef: §c" + Bukkit.getOfflinePlayer(contract.getNonAggressionTarget()).getName());
                }
                break;
            case MATERIAL_DELIVERY:
                if (contract.getMaterial() != null) {
                    lore.add("§7Malzeme: §e" + contract.getMaterial().name());
                    lore.add("§7Miktar: §e" + contract.getAmount());
                    lore.add("§7Teslim: §a" + contract.getDelivered() + "§7/§a" + contract.getAmount());
                }
                break;
            case STRUCTURE_BUILD:
                if (contract.getStructureType() != null) {
                    lore.add("§7Yapı: §e" + contract.getStructureType());
                }
                break;
        }
        
        meta.setLore(lore);
        infoItem.setItemMeta(meta);
        menu.setItem(13, infoItem);
        
        // Kabul Et butonu
        if (contract.getAcceptor() == null) {
            menu.setItem(11, createButton(Material.EMERALD_BLOCK, "§a[Kabul Et]", "§7Kan imzası gerekli"));
        }
        
        // Reddet butonu
        menu.setItem(15, createButton(Material.REDSTONE_BLOCK, "§c[Reddet]", null));
        
        // Geri butonu
        menu.setItem(22, createButton(Material.ARROW, "§eGeri", null));
        
        return menu;
    }
    
    /**
     * Sözleşme item'ı oluştur
     */
    private static ItemStack createContractItem(Contract contract) {
        Material icon = getContractIcon(contract.getType());
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§e" + getContractTypeName(contract.getType()));
        List<String> lore = new ArrayList<>();
        lore.add("§7Ödül: §a" + contract.getReward() + " Altın");
        lore.add("§7Süre: §e" + formatTime(contract.getDeadline()));
        if (contract.getAcceptor() != null) {
            lore.add("§7Kabul Eden: §e" + Bukkit.getOfflinePlayer(contract.getAcceptor()).getName());
        } else {
            lore.add("§7Durum: §aAçık");
        }
        if (contract.isBreached()) {
            lore.add("§c§lİHLAL EDİLDİ!");
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Sözleşme tipi ikonu
     */
    private static Material getContractIcon(Contract.ContractType type) {
        switch (type) {
            case PLAYER_KILL:
                return Material.DIAMOND_SWORD;
            case TERRITORY_RESTRICT:
                return Material.BARRIER;
            case NON_AGGRESSION:
                return Material.SHIELD;
            case BASE_PROTECTION:
                return Material.BEACON;
            case MATERIAL_DELIVERY:
                return Material.CHEST;
            case STRUCTURE_BUILD:
                return Material.STRUCTURE_BLOCK;
            default:
                return Material.PAPER;
        }
    }
    
    /**
     * Sözleşme tipi ismi
     */
    private static String getContractTypeName(Contract.ContractType type) {
        switch (type) {
            case MATERIAL_DELIVERY:
                return "Malzeme Temini";
            case PLAYER_KILL:
                return "Oyuncu Avı";
            case TERRITORY_RESTRICT:
                return "Bölge Yasağı";
            case NON_AGGRESSION:
                return "Saldırmama Anlaşması";
            case BASE_PROTECTION:
                return "Base Koruma";
            case STRUCTURE_BUILD:
                return "Yapı İnşa";
            default:
                return "Bilinmeyen";
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
        long totalSeconds = (timeMillis - System.currentTimeMillis()) / 1000;
        if (totalSeconds <= 0) return "§cSüre Doldu";
        
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

