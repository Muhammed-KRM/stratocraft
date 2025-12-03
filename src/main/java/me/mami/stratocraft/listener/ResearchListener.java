package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.ItemManager;
import me.mami.stratocraft.manager.ResearchManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import me.mami.stratocraft.Main;

public class ResearchListener implements Listener {
    private final ResearchManager researchManager;

    public ResearchListener(ResearchManager rm) {
        this.researchManager = rm;
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player p = (Player) event.getWhoClicked();
        
        // Admin bypass kontrolü
        if (me.mami.stratocraft.util.ListenerUtil.hasAdminBypass(p)) {
            return; // Admin bypass yetkisi varsa tarif kısıtlamalarını atla
        }
        
        ItemStack result = event.getRecipe().getResult();
        
        // Özel eşyalar için tarif kontrolü
        if (ItemManager.isCustomItem(result, "RECIPE_TECTONIC") || 
            ItemManager.isCustomItem(result, "TITANIUM_INGOT") ||
            ItemManager.isCustomItem(result, "DARK_MATTER")) {
            
            // Eğer tarif kitabı yoksa crafting'i iptal et
            String recipeId = "";
            if (ItemManager.isCustomItem(result, "RECIPE_TECTONIC")) {
                recipeId = "TECTONIC";
            } else if (ItemManager.isCustomItem(result, "TITANIUM_INGOT")) {
                recipeId = "TITANIUM";
            }
            
            if (!recipeId.isEmpty() && !researchManager.hasRecipeBook(p, recipeId)) {
                event.setCancelled(true);
                p.sendMessage("§cBu eşyayı yapmak için gerekli tarif kitabına sahip değilsin!");
                return; // Event iptal edildi, devam etme
            }
        }
        
        // Özel silah ve zırh tarif kontrolü
        if (result != null && result.hasItemMeta()) {
            // Özel silah kontrolü
            String weaponId = result.getItemMeta().getPersistentDataContainer()
                .get(new NamespacedKey(Main.getInstance(), "special_weapon_id"), PersistentDataType.STRING);
            
            if (weaponId != null && weaponId.startsWith("WEAPON_L")) {
                // Seviye ve varyantı çıkar (örn: WEAPON_L3_2 -> level=3, variant=2)
                String[] parts = weaponId.split("_");
                if (parts.length >= 3) {
                    int level = Integer.parseInt(parts[1].substring(1)); // L3 -> 3
                    int variant = Integer.parseInt(parts[2]); // 2
                    
                    // Tarif kitabı kontrolü
                    String recipeId = "ARMOR_L" + level + "_" + variant; // Yanlış! WEAPON olmalı
                    recipeId = "WEAPON_L" + level + "_" + variant; // Düzeltildi
                    if (!researchManager.hasRecipeBook(p, recipeId)) {
                        event.setCancelled(true);
                        p.sendMessage("§cBu silahı yapmak için gerekli tarif kitabına sahip değilsin!");
                        p.sendMessage("§7Gerekli: §e" + recipeId);
                        return;
                    }
                    
                    // Boss item kontrolü (seviye 2+ için)
                    if (level >= 2) {
                        ItemStack[] matrix = event.getInventory().getMatrix();
                        boolean hasBossItem = false;
                        
                        // Gerekli boss item'ı belirle
                        ItemStack requiredBossItem = getRequiredBossItemForWeapon(level, variant);
                        
                        if (requiredBossItem != null) {
                            for (ItemStack item : matrix) {
                                if (item != null && ItemManager.isCustomItem(item, getBossItemId(requiredBossItem))) {
                                    hasBossItem = true;
                                    break;
                                }
                            }
                            
                            if (!hasBossItem) {
                                event.setCancelled(true);
                                p.sendMessage("§cBu silahı yapmak için gerekli boss item'ına sahip değilsin!");
                                return;
                            }
                        }
                    }
                }
            }
            
            // Özel zırh kontrolü
            String armorId = result.getItemMeta().getPersistentDataContainer()
                .get(new NamespacedKey(Main.getInstance(), "special_armor_id"), PersistentDataType.STRING);
            
            if (armorId != null && armorId.startsWith("ARMOR_L")) {
                // Seviye ve varyantı çıkar (örn: ARMOR_L3_2 -> level=3, variant=2)
                String[] parts = armorId.split("_");
                if (parts.length >= 3) {
                    int level = Integer.parseInt(parts[1].substring(1)); // L3 -> 3
                    int variant = Integer.parseInt(parts[2]); // 2
                    
                    // Tarif kitabı kontrolü
                    String recipeId = "ARMOR_L" + level + "_" + variant;
                    if (!researchManager.hasRecipeBook(p, recipeId)) {
                        event.setCancelled(true);
                        p.sendMessage("§cBu zırhı yapmak için gerekli tarif kitabına sahip değilsin!");
                        p.sendMessage("§7Gerekli: §e" + recipeId);
                        return;
                    }
                    
                    // Boss item kontrolü (seviye 2+ için)
                    if (level >= 2) {
                        ItemStack[] matrix = event.getInventory().getMatrix();
                        boolean hasBossItem = false;
                        
                        // Gerekli boss item'ı belirle
                        ItemStack requiredBossItem = getRequiredBossItemForArmor(level, variant);
                        
                        if (requiredBossItem != null) {
                            for (ItemStack item : matrix) {
                                if (item != null && ItemManager.isCustomItem(item, getBossItemId(requiredBossItem))) {
                                    hasBossItem = true;
                                    break;
                                }
                            }
                            
                            if (!hasBossItem) {
                                event.setCancelled(true);
                                p.sendMessage("§cBu zırhı yapmak için gerekli boss item'ına sahip değilsin!");
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Silah için gerekli boss item'ı döndür
     */
    private ItemStack getRequiredBossItemForWeapon(int level, int variant) {
        switch (level) {
            case 2:
                switch (variant) {
                    case 1: return ItemManager.GOBLIN_CROWN;
                    case 2: return ItemManager.ORC_AMULET;
                    case 3: return ItemManager.GOBLIN_CROWN;
                    case 4: return ItemManager.ORC_AMULET;
                    case 5: return ItemManager.TROLL_HEART;
                }
                break;
            case 3:
                switch (variant) {
                    case 1: return ItemManager.DRAGON_SCALE;
                    case 2: return ItemManager.TREX_TOOTH;
                    case 3: return ItemManager.CYCLOPS_EYE;
                    case 4: return ItemManager.DRAGON_SCALE;
                    case 5: return ItemManager.TREX_TOOTH;
                }
                break;
            case 4:
                switch (variant) {
                    case 1: return ItemManager.TITAN_CORE;
                    case 2: return ItemManager.PHOENIX_FEATHER;
                    case 3: return ItemManager.KRAKEN_TENTACLE;
                    case 4: return ItemManager.TITAN_CORE;
                    case 5: return ItemManager.PHOENIX_FEATHER;
                }
                break;
            case 5:
                switch (variant) {
                    case 1: return ItemManager.DEMON_LORD_HORN;
                    case 2: return ItemManager.VOID_DRAGON_HEART;
                    case 3: return ItemManager.DEMON_LORD_HORN;
                    case 4: return ItemManager.VOID_DRAGON_HEART;
                    case 5: return ItemManager.DEMON_LORD_HORN;
                }
                break;
        }
        return null; // Seviye 1 için boss item gerekmez
    }
    
    /**
     * Zırh için gerekli boss item'ı döndür
     */
    private ItemStack getRequiredBossItemForArmor(int level, int variant) {
        switch (level) {
            case 2:
                switch (variant) {
                    case 1: return ItemManager.GOBLIN_CROWN;
                    case 2: return ItemManager.ORC_AMULET;
                    case 3: return ItemManager.GOBLIN_CROWN;
                    case 4: return ItemManager.ORC_AMULET;
                    case 5: return ItemManager.TROLL_HEART;
                }
                break;
            case 3:
                switch (variant) {
                    case 1: return ItemManager.DRAGON_SCALE;
                    case 2: return ItemManager.TREX_TOOTH;
                    case 3: return ItemManager.CYCLOPS_EYE;
                    case 4: return ItemManager.DRAGON_SCALE;
                    case 5: return ItemManager.TREX_TOOTH;
                }
                break;
            case 4:
                switch (variant) {
                    case 1: return ItemManager.TITAN_CORE;
                    case 2: return ItemManager.PHOENIX_FEATHER;
                    case 3: return ItemManager.KRAKEN_TENTACLE;
                    case 4: return ItemManager.TITAN_CORE;
                    case 5: return ItemManager.PHOENIX_FEATHER;
                }
                break;
            case 5:
                switch (variant) {
                    case 1: return ItemManager.DEMON_LORD_HORN;
                    case 2: return ItemManager.VOID_DRAGON_HEART;
                    case 3: return ItemManager.DEMON_LORD_HORN;
                    case 4: return ItemManager.VOID_DRAGON_HEART;
                    case 5: return ItemManager.DEMON_LORD_HORN;
                }
                break;
        }
        return null; // Seviye 1 için boss item gerekmez
    }
    
    /**
     * Boss item'ın ID'sini al
     */
    private String getBossItemId(ItemStack bossItem) {
        if (bossItem == null) return null;
        if (bossItem.equals(ItemManager.GOBLIN_CROWN)) return "GOBLIN_CROWN";
        if (bossItem.equals(ItemManager.ORC_AMULET)) return "ORC_AMULET";
        if (bossItem.equals(ItemManager.TROLL_HEART)) return "TROLL_HEART";
        if (bossItem.equals(ItemManager.DRAGON_SCALE)) return "DRAGON_SCALE";
        if (bossItem.equals(ItemManager.TREX_TOOTH)) return "TREX_TOOTH";
        if (bossItem.equals(ItemManager.CYCLOPS_EYE)) return "CYCLOPS_EYE";
        if (bossItem.equals(ItemManager.TITAN_CORE)) return "TITAN_CORE";
        if (bossItem.equals(ItemManager.PHOENIX_FEATHER)) return "PHOENIX_FEATHER";
        if (bossItem.equals(ItemManager.KRAKEN_TENTACLE)) return "KRAKEN_TENTACLE";
        if (bossItem.equals(ItemManager.DEMON_LORD_HORN)) return "DEMON_LORD_HORN";
        if (bossItem.equals(ItemManager.VOID_DRAGON_HEART)) return "VOID_DRAGON_HEART";
        return null;
    }
}

