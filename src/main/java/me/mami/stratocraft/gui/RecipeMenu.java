package me.mami.stratocraft.gui;

import me.mami.stratocraft.manager.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Tarif Kitabı GUI Menüsü
 * Crafting recipe'lerini görsel olarak gösterir
 */
public class RecipeMenu {
    
    /**
     * Tarif menüsü oluştur
     * @param recipeId Tarif ID'si (örn: "ADAMANTITE", "WEAPON_L1_1", "ARMOR_L2_3")
     * @return Inventory menü
     */
    public static Inventory createRecipeMenu(String recipeId) {
        String recipeIdUpper = recipeId.toUpperCase().replace("RECIPE_", "");
        ItemManager.RecipeInfo info = ItemManager.getRecipeInfo(recipeIdUpper);
        
        if (info == null) {
            // Bilgi bulunamadı - boş menü
            Inventory menu = Bukkit.createInventory(null, 27, "§cTarif Bulunamadı");
            menu.setItem(13, createButton(Material.BARRIER, "§cTarif bilgisi bulunamadı!", 
                "§7Bu tarif için bilgi yok."));
            return menu;
        }
        
        Inventory menu = Bukkit.createInventory(null, 54, "§eTarif: " + info.getDisplayName());
        
        // Arka plan doldur (görsel iyileştirme)
        fillBackground(menu);
        
        // Crafting grid gösterimi (Slot 10-12, 19-21, 28-30: 3x3 grid)
        // Layout:
        // 0  1  2  3  4  5  6  7  8
        // 9  10 11 12 13 14 15 16 17
        // 18 19 20 21 22 23 24 25 26
        // 27 28 29 30 31 32 33 34 35
        // 36 37 38 39 40 41 42 43 44
        // 45 46 47 48 49 50 51 52 53
        
        // Crafting recipe bilgisi varsa göster
        if (info.getCraftingRecipe() != null && !info.getCraftingRecipe().isEmpty()) {
            setupCraftingGrid(menu, info.getCraftingRecipe());
        } else {
            // Crafting recipe yoksa bilgi göster
            ItemStack infoItem = new ItemStack(Material.CRAFTING_TABLE);
            ItemMeta meta = infoItem.getItemMeta();
            meta.setDisplayName("§eCrafting Tarifi");
            List<String> lore = new ArrayList<>();
            lore.add("§7" + info.getFunctionInfo());
            if (info.getLocationInfo() != null) {
                lore.add("");
                lore.add("§7" + info.getLocationInfo());
            }
            meta.setLore(lore);
            infoItem.setItemMeta(meta);
            menu.setItem(22, infoItem);
        }
        
        // Sonuç item (Slot 40 - merkez)
        ItemStack resultItem = getResultItem(recipeIdUpper);
        if (resultItem != null) {
            ItemStack displayResult = resultItem.clone();
            ItemMeta resultMeta = displayResult.getItemMeta();
            if (resultMeta != null) {
                List<String> resultLore = resultMeta.getLore();
                if (resultLore == null) resultLore = new ArrayList<>();
                resultLore.add(0, "§6§l═══════════════════");
                resultLore.add(1, "§e§lCraft Edilecek Item");
                resultLore.add(2, "§6§l═══════════════════");
                resultLore.add(3, "");
                resultLore.add(4, "§7§oBu item craft edilecek");
                resultLore.add(5, "§7§osonuç item'dır.");
                resultMeta.setLore(resultLore);
                displayResult.setItemMeta(resultMeta);
            }
            menu.setItem(40, displayResult);
        }
        
        // Malzeme listesi (Slot 31 - sol taraf)
        setupMaterialList(menu, info);
        
        // Bilgi butonu (Slot 49)
        ItemStack infoButton = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoButton.getItemMeta();
        infoMeta.setDisplayName("§e§lTarif Bilgisi");
        List<String> infoLore = new ArrayList<>();
        infoLore.add("§7" + info.getFunctionInfo());
        if (info.getLocationInfo() != null) {
            infoLore.add("");
            infoLore.add("§7" + info.getLocationInfo());
        }
        infoLore.add("");
        infoLore.add("§e§lTıklayın: §7Detaylı bilgi göster");
        infoMeta.setLore(infoLore);
        infoButton.setItemMeta(infoMeta);
        menu.setItem(49, infoButton);
        
        // Kapat butonu (Slot 53)
        menu.setItem(53, createButton(Material.BARRIER, "§c§lKapat", "§7Menüyü kapatmak için tıklayın"));
        
        return menu;
    }
    
    /**
     * Crafting grid'i ayarla (3x3)
     * Özel item'ları (TROLL_HEART, GOBLIN_CROWN, vb.) ItemManager'dan alır
     */
    private static void setupCraftingGrid(Inventory menu, List<String> recipeLines) {
        // Crafting grid slotları (3x3)
        // Slot 10-12: İlk satır
        // Slot 19-21: İkinci satır
        // Slot 28-30: Üçüncü satır
        int[] gridSlots = {10, 11, 12, 19, 20, 21, 28, 29, 30};
        
        String[] gridLines = new String[3];
        Map<String, ItemStack> itemMap = new HashMap<>(); // Material yerine ItemStack kullan
        
        // Recipe satırlarını parse et
        for (String line : recipeLines) {
            if (line.startsWith("Satır 1:")) {
                gridLines[0] = line.replace("Satır 1:", "").trim();
            } else if (line.startsWith("Satır 2:")) {
                gridLines[1] = line.replace("Satır 2:", "").trim();
            } else if (line.startsWith("Satır 3:")) {
                gridLines[2] = line.replace("Satır 3:", "").trim();
            } else if (line.contains("=")) {
                // Malzeme açıklaması (örn: "B = Troll Kralı Kalbi")
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String materialName = parts[1].trim();
                    
                    // Önce özel item'ları kontrol et (ItemManager'dan)
                    ItemStack customItem = getCustomItemByName(materialName);
                    if (customItem != null) {
                        itemMap.put(key, customItem.clone());
                    } else {
                        // Normal Material kontrolü
                        Material mat = parseMaterialName(materialName);
                        if (mat != null) {
                            itemMap.put(key, new ItemStack(mat));
                        }
                    }
                }
            }
        }
        
        // 3x3 grid'i oluştur
        for (int row = 0; row < 3; row++) {
            String gridLine = gridLines[row];
            if (gridLine != null && !gridLine.isEmpty()) {
                // Parse et: "[I] [F] [I]" -> I, F, I
                Pattern pattern = Pattern.compile("\\[([^\\]]+)\\]");
                Matcher matcher = pattern.matcher(gridLine);
                int col = 0;
                while (matcher.find() && col < 3) {
                    String key = matcher.group(1).trim();
                    int slotIndex = row * 3 + col;
                    if (slotIndex < gridSlots.length) {
                        ItemStack item = itemMap.get(key);
                        if (item != null) {
                            menu.setItem(gridSlots[slotIndex], item.clone());
                        } else if (key.isEmpty() || key.equals(" ")) {
                            // Boş slot - hava bırak
                        } else {
                            // Bilinmeyen malzeme - bilgi item'ı
                            ItemStack infoItem = new ItemStack(Material.PAPER);
                            ItemMeta meta = infoItem.getItemMeta();
                            meta.setDisplayName("§7" + key);
                            List<String> lore = new ArrayList<>();
                            lore.add("§7Malzeme: §e" + key);
                            meta.setLore(lore);
                            infoItem.setItemMeta(meta);
                            menu.setItem(gridSlots[slotIndex], infoItem);
                        }
                    }
                    col++;
                }
            }
        }
        
        // Ok işareti (Slot 25)
        menu.setItem(25, createButton(Material.ARROW, "§e→", null));
    }
    
    /**
     * Özel item'ı isimden al (ItemManager'dan)
     * Örneğin: "Troll Kralı Kalbi" -> TROLL_HEART item'ı
     */
    private static ItemStack getCustomItemByName(String name) {
        name = name.toLowerCase().trim();
        
        // Boss item'ları
        if (name.contains("troll") && name.contains("kalbi")) {
            return ItemManager.TROLL_HEART != null ? ItemManager.TROLL_HEART.clone() : null;
        }
        if (name.contains("goblin") && (name.contains("taç") || name.contains("tac"))) {
            return ItemManager.GOBLIN_CROWN != null ? ItemManager.GOBLIN_CROWN.clone() : null;
        }
        if (name.contains("ork") && (name.contains("amulet") || name.contains("amule"))) {
            return ItemManager.ORC_AMULET != null ? ItemManager.ORC_AMULET.clone() : null;
        }
        if (name.contains("ejderha") && (name.contains("ölçek") || name.contains("olcek"))) {
            return ItemManager.DRAGON_SCALE != null ? ItemManager.DRAGON_SCALE.clone() : null;
        }
        if ((name.contains("t-rex") || name.contains("trex")) && (name.contains("diş") || name.contains("dis"))) {
            return ItemManager.TREX_TOOTH != null ? ItemManager.TREX_TOOTH.clone() : null;
        }
        if (name.contains("cyclops") && (name.contains("göz") || name.contains("goz"))) {
            return ItemManager.CYCLOPS_EYE != null ? ItemManager.CYCLOPS_EYE.clone() : null;
        }
        if (name.contains("titan") && (name.contains("çekirdek") || name.contains("cekirdek"))) {
            return ItemManager.TITAN_CORE != null ? ItemManager.TITAN_CORE.clone() : null;
        }
        if (name.contains("phoenix") && (name.contains("tüy") || name.contains("tuy"))) {
            return ItemManager.PHOENIX_FEATHER != null ? ItemManager.PHOENIX_FEATHER.clone() : null;
        }
        if (name.contains("kraken") && (name.contains("dokunaç") || name.contains("dokunac"))) {
            return ItemManager.KRAKEN_TENTACLE != null ? ItemManager.KRAKEN_TENTACLE.clone() : null;
        }
        if (name.contains("void") && name.contains("dragon") && name.contains("heart")) {
            return ItemManager.VOID_DRAGON_HEART != null ? ItemManager.VOID_DRAGON_HEART.clone() : null;
        }
        if (name.contains("demon") && name.contains("lord") && (name.contains("boynuz") || name.contains("horn"))) {
            return ItemManager.DEMON_LORD_HORN != null ? ItemManager.DEMON_LORD_HORN.clone() : null;
        }
        
        return null;
    }
    
    /**
     * Malzeme listesini ayarla
     */
    private static void setupMaterialList(Inventory menu, ItemManager.RecipeInfo info) {
        ItemStack materialItem = new ItemStack(Material.BOOK);
        ItemMeta materialMeta = materialItem.getItemMeta();
        materialMeta.setDisplayName("§6Gerekli Malzemeler");
        List<String> materialLore = new ArrayList<>();
        
        if (info.getCraftingRecipe() != null && !info.getCraftingRecipe().isEmpty()) {
            for (String line : info.getCraftingRecipe()) {
                if (line.contains("=")) {
                    materialLore.add("§7" + line);
                }
            }
            if (materialLore.isEmpty()) {
                materialLore.add("§7Detaylı bilgi için Shift+Sağ Tık yapın");
            }
        } else {
            materialLore.add("§7" + info.getFunctionInfo());
            if (info.getLocationInfo() != null) {
                materialLore.add("");
                materialLore.add("§7" + info.getLocationInfo());
            }
        }
        
        materialMeta.setLore(materialLore);
        materialItem.setItemMeta(materialMeta);
        menu.setItem(31, materialItem);
    }
    
    /**
     * Malzeme ismini Material'a çevir
     */
    private static Material parseMaterialName(String name) {
        name = name.toLowerCase().trim();
        
        // Türkçe isimlerden İngilizce Material'a çevir
        Map<String, Material> materialMap = new HashMap<>();
        materialMap.put("demir külçe", Material.IRON_INGOT);
        materialMap.put("demir blok", Material.IRON_BLOCK);
        materialMap.put("altın külçe", Material.GOLD_INGOT);
        materialMap.put("altın blok", Material.GOLD_BLOCK);
        materialMap.put("elmas", Material.DIAMOND);
        materialMap.put("elmas blok", Material.DIAMOND_BLOCK);
        materialMap.put("ender incisi", Material.ENDER_PEARL);
        materialMap.put("ender gözü", Material.ENDER_EYE);
        materialMap.put("tüy", Material.FEATHER);
        materialMap.put("çubuk", Material.STICK);
        materialMap.put("ip", Material.STRING);
        materialMap.put("goblin kralı taçı", Material.GOLDEN_HELMET); // Placeholder
        materialMap.put("troll kralı kalbi", Material.HEART_OF_THE_SEA); // Placeholder
        materialMap.put("t-rex dişi", Material.BONE); // Placeholder
        materialMap.put("obsidyen", Material.OBSIDIAN);
        materialMap.put("tnt", Material.TNT);
        materialMap.put("barut", Material.GUNPOWDER);
        materialMap.put("buz", Material.PACKED_ICE);
        materialMap.put("örümcek gözü", Material.SPIDER_EYE);
        materialMap.put("redstone", Material.REDSTONE);
        materialMap.put("redstone bloğu", Material.REDSTONE_BLOCK);
        materialMap.put("kömür bloğu", Material.COAL_BLOCK);
        materialMap.put("paratoner", Material.LIGHTNING_ROD);
        materialMap.put("buğday", Material.WHEAT);
        materialMap.put("magma blok", Material.MAGMA_BLOCK);
        materialMap.put("magma kremi", Material.MAGMA_CREAM);
        materialMap.put("netherrack", Material.NETHERRACK);
        materialMap.put("zümrüt blok", Material.EMERALD_BLOCK);
        materialMap.put("slime blok", Material.SLIME_BLOCK);
        materialMap.put("lapis blok", Material.LAPIS_BLOCK);
        materialMap.put("bakır blok", Material.COPPER_BLOCK);
        materialMap.put("glowstone", Material.GLOWSTONE);
        materialMap.put("zehirli patates", Material.POISONOUS_POTATO);
        materialMap.put("frosted ice", Material.FROSTED_ICE);
        materialMap.put("beacon", Material.BEACON);
        materialMap.put("nether star", Material.NETHER_STAR);
        materialMap.put("end crystal", Material.END_CRYSTAL);
        materialMap.put("bedrock", Material.BEDROCK);
        materialMap.put("taş", Material.STONE);
        materialMap.put("cam", Material.GLASS);
        materialMap.put("ahşap", Material.OAK_PLANKS);
        materialMap.put("planks", Material.OAK_PLANKS);
        materialMap.put("netherite blok", Material.NETHERITE_BLOCK);
        materialMap.put("iron bars", Material.IRON_BARS);
        materialMap.put("demir çubuk", Material.IRON_BARS);
        materialMap.put("wither kafası", Material.WITHER_SKELETON_SKULL);
        materialMap.put("kemik", Material.BONE);
        materialMap.put("demir balta", Material.IRON_AXE);
        materialMap.put("altın balta", Material.GOLDEN_AXE);
        materialMap.put("netherite kılıç", Material.NETHERITE_SWORD);
        materialMap.put("dürbün", Material.SPYGLASS);
        materialMap.put("olta", Material.FISHING_ROD);
        materialMap.put("alev tozu", Material.BLAZE_POWDER);
        materialMap.put("alev amplifikatörü", Material.BLAZE_ROD);
        materialMap.put("titan golem çekirdeği", Material.NETHER_STAR); // Placeholder
        materialMap.put("void dragon heart", Material.HEART_OF_THE_SEA); // Placeholder
        materialMap.put("hayalet zarı", Material.PHANTOM_MEMBRANE);
        materialMap.put("ateş topu", Material.FIRE_CHARGE);
        
        // Direkt Material ismi kontrolü
        try {
            Material directMat = Material.valueOf(name.toUpperCase().replace(" ", "_"));
            return directMat;
        } catch (IllegalArgumentException e) {
            // Material bulunamadı
        }
        
        // Türkçe map'ten ara
        for (Map.Entry<String, Material> entry : materialMap.entrySet()) {
            if (name.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        return null;
    }
    
    /**
     * Recipe ID'ye göre sonuç item'ı al
     */
    private static ItemStack getResultItem(String recipeId) {
        // ItemManager'dan static field'ları kullan
        switch (recipeId) {
            // Özel eşyalar
            case "LIGHTNING_CORE":
                return ItemManager.LIGHTNING_CORE != null ? ItemManager.LIGHTNING_CORE.clone() : null;
            case "TITANIUM_INGOT":
                return ItemManager.TITANIUM_INGOT != null ? ItemManager.TITANIUM_INGOT.clone() : null;
            case "TRAP_CORE":
                return ItemManager.TRAP_CORE != null ? ItemManager.TRAP_CORE.clone() : null;
            case "RUSTY_HOOK":
                return ItemManager.RUSTY_HOOK != null ? ItemManager.RUSTY_HOOK.clone() : null;
            case "ADAMANTITE":
                return ItemManager.ADAMANTITE != null ? ItemManager.ADAMANTITE.clone() : null;
            case "DARK_MATTER":
                return ItemManager.DARK_MATTER != null ? ItemManager.DARK_MATTER.clone() : null;
            case "RED_DIAMOND":
                return ItemManager.RED_DIAMOND != null ? ItemManager.RED_DIAMOND.clone() : null;
            case "RUBY":
                return ItemManager.RUBY != null ? ItemManager.RUBY.clone() : null;
            case "STAR_CORE":
                return ItemManager.STAR_CORE != null ? ItemManager.STAR_CORE.clone() : null;
            case "FLAME_AMPLIFIER":
                return ItemManager.FLAME_AMPLIFIER != null ? ItemManager.FLAME_AMPLIFIER.clone() : null;
            case "DEVIL_HORN":
                return ItemManager.DEVIL_HORN != null ? ItemManager.DEVIL_HORN.clone() : null;
            case "DEVIL_SNAKE_EYE":
                return ItemManager.DEVIL_SNAKE_EYE != null ? ItemManager.DEVIL_SNAKE_EYE.clone() : null;
            case "WAR_FAN":
                return ItemManager.WAR_FAN != null ? ItemManager.WAR_FAN.clone() : null;
            case "TOWER_SHIELD":
                return ItemManager.TOWER_SHIELD != null ? ItemManager.TOWER_SHIELD.clone() : null;
            case "HELL_FRUIT":
                return ItemManager.HELL_FRUIT != null ? ItemManager.HELL_FRUIT.clone() : null;
            case "SULFUR":
                return ItemManager.SULFUR != null ? ItemManager.SULFUR.clone() : null;
            case "BAUXITE_INGOT":
                return ItemManager.BAUXITE_INGOT != null ? ItemManager.BAUXITE_INGOT.clone() : null;
            case "ROCK_SALT":
                return ItemManager.ROCK_SALT != null ? ItemManager.ROCK_SALT.clone() : null;
            case "MITHRIL_INGOT":
                return ItemManager.MITHRIL_INGOT != null ? ItemManager.MITHRIL_INGOT.clone() : null;
            case "MITHRIL_STRING":
                return ItemManager.MITHRIL_STRING != null ? ItemManager.MITHRIL_STRING.clone() : null;
            case "ASTRAL_CRYSTAL":
                return ItemManager.ASTRAL_CRYSTAL != null ? ItemManager.ASTRAL_CRYSTAL.clone() : null;
            case "GOLDEN_HOOK":
                return ItemManager.GOLDEN_HOOK != null ? ItemManager.GOLDEN_HOOK.clone() : null;
            case "TITAN_GRAPPLE":
                return ItemManager.TITAN_GRAPPLE != null ? ItemManager.TITAN_GRAPPLE.clone() : null;
            
            // Silah tarifleri
            case "WEAPON_L1_1":
                return ItemManager.WEAPON_L1_1 != null ? ItemManager.WEAPON_L1_1.clone() : null;
            case "WEAPON_L1_2":
                return ItemManager.WEAPON_L1_2 != null ? ItemManager.WEAPON_L1_2.clone() : null;
            case "WEAPON_L1_3":
                return ItemManager.WEAPON_L1_3 != null ? ItemManager.WEAPON_L1_3.clone() : null;
            case "WEAPON_L1_4":
                return ItemManager.WEAPON_L1_4 != null ? ItemManager.WEAPON_L1_4.clone() : null;
            case "WEAPON_L1_5":
                return ItemManager.WEAPON_L1_5 != null ? ItemManager.WEAPON_L1_5.clone() : null;
            case "WEAPON_L2_1":
                return ItemManager.WEAPON_L2_1 != null ? ItemManager.WEAPON_L2_1.clone() : null;
            case "WEAPON_L2_2":
                return ItemManager.WEAPON_L2_2 != null ? ItemManager.WEAPON_L2_2.clone() : null;
            case "WEAPON_L2_3":
                return ItemManager.WEAPON_L2_3 != null ? ItemManager.WEAPON_L2_3.clone() : null;
            case "WEAPON_L2_4":
                return ItemManager.WEAPON_L2_4 != null ? ItemManager.WEAPON_L2_4.clone() : null;
            case "WEAPON_L2_5":
                return ItemManager.WEAPON_L2_5 != null ? ItemManager.WEAPON_L2_5.clone() : null;
            case "WEAPON_L3_1":
                return ItemManager.WEAPON_L3_1 != null ? ItemManager.WEAPON_L3_1.clone() : null;
            case "WEAPON_L3_2":
                return ItemManager.WEAPON_L3_2 != null ? ItemManager.WEAPON_L3_2.clone() : null;
            case "WEAPON_L3_3":
                return ItemManager.WEAPON_L3_3 != null ? ItemManager.WEAPON_L3_3.clone() : null;
            case "WEAPON_L3_4":
                return ItemManager.WEAPON_L3_4 != null ? ItemManager.WEAPON_L3_4.clone() : null;
            case "WEAPON_L3_5":
                return ItemManager.WEAPON_L3_5 != null ? ItemManager.WEAPON_L3_5.clone() : null;
            case "WEAPON_L4_1":
                return ItemManager.WEAPON_L4_1 != null ? ItemManager.WEAPON_L4_1.clone() : null;
            case "WEAPON_L4_2":
                return ItemManager.WEAPON_L4_2 != null ? ItemManager.WEAPON_L4_2.clone() : null;
            case "WEAPON_L4_3":
                return ItemManager.WEAPON_L4_3 != null ? ItemManager.WEAPON_L4_3.clone() : null;
            case "WEAPON_L4_4":
                return ItemManager.WEAPON_L4_4 != null ? ItemManager.WEAPON_L4_4.clone() : null;
            case "WEAPON_L4_5":
                return ItemManager.WEAPON_L4_5 != null ? ItemManager.WEAPON_L4_5.clone() : null;
            case "WEAPON_L5_1":
                return ItemManager.WEAPON_L5_1 != null ? ItemManager.WEAPON_L5_1.clone() : null;
            case "WEAPON_L5_2":
                return ItemManager.WEAPON_L5_2 != null ? ItemManager.WEAPON_L5_2.clone() : null;
            case "WEAPON_L5_3":
                return ItemManager.WEAPON_L5_3 != null ? ItemManager.WEAPON_L5_3.clone() : null;
            case "WEAPON_L5_4":
                return ItemManager.WEAPON_L5_4 != null ? ItemManager.WEAPON_L5_4.clone() : null;
            case "WEAPON_L5_5":
                return ItemManager.WEAPON_L5_5 != null ? ItemManager.WEAPON_L5_5.clone() : null;
            
            // Zırh tarifleri
            case "ARMOR_L1_1":
                return ItemManager.ARMOR_L1_1 != null ? ItemManager.ARMOR_L1_1.clone() : null;
            case "ARMOR_L1_2":
                return ItemManager.ARMOR_L1_2 != null ? ItemManager.ARMOR_L1_2.clone() : null;
            case "ARMOR_L1_3":
                return ItemManager.ARMOR_L1_3 != null ? ItemManager.ARMOR_L1_3.clone() : null;
            case "ARMOR_L1_4":
                return ItemManager.ARMOR_L1_4 != null ? ItemManager.ARMOR_L1_4.clone() : null;
            case "ARMOR_L1_5":
                return ItemManager.ARMOR_L1_5 != null ? ItemManager.ARMOR_L1_5.clone() : null;
            case "ARMOR_L2_1":
                return ItemManager.ARMOR_L2_1 != null ? ItemManager.ARMOR_L2_1.clone() : null;
            case "ARMOR_L2_2":
                return ItemManager.ARMOR_L2_2 != null ? ItemManager.ARMOR_L2_2.clone() : null;
            case "ARMOR_L2_3":
                return ItemManager.ARMOR_L2_3 != null ? ItemManager.ARMOR_L2_3.clone() : null;
            case "ARMOR_L2_4":
                return ItemManager.ARMOR_L2_4 != null ? ItemManager.ARMOR_L2_4.clone() : null;
            case "ARMOR_L2_5":
                return ItemManager.ARMOR_L2_5 != null ? ItemManager.ARMOR_L2_5.clone() : null;
            case "ARMOR_L3_1":
                return ItemManager.ARMOR_L3_1 != null ? ItemManager.ARMOR_L3_1.clone() : null;
            case "ARMOR_L3_2":
                return ItemManager.ARMOR_L3_2 != null ? ItemManager.ARMOR_L3_2.clone() : null;
            case "ARMOR_L3_3":
                return ItemManager.ARMOR_L3_3 != null ? ItemManager.ARMOR_L3_3.clone() : null;
            case "ARMOR_L3_4":
                return ItemManager.ARMOR_L3_4 != null ? ItemManager.ARMOR_L3_4.clone() : null;
            case "ARMOR_L3_5":
                return ItemManager.ARMOR_L3_5 != null ? ItemManager.ARMOR_L3_5.clone() : null;
            case "ARMOR_L4_1":
                return ItemManager.ARMOR_L4_1 != null ? ItemManager.ARMOR_L4_1.clone() : null;
            case "ARMOR_L4_2":
                return ItemManager.ARMOR_L4_2 != null ? ItemManager.ARMOR_L4_2.clone() : null;
            case "ARMOR_L4_3":
                return ItemManager.ARMOR_L4_3 != null ? ItemManager.ARMOR_L4_3.clone() : null;
            case "ARMOR_L4_4":
                return ItemManager.ARMOR_L4_4 != null ? ItemManager.ARMOR_L4_4.clone() : null;
            case "ARMOR_L4_5":
                return ItemManager.ARMOR_L4_5 != null ? ItemManager.ARMOR_L4_5.clone() : null;
            case "ARMOR_L5_1":
                return ItemManager.ARMOR_L5_1 != null ? ItemManager.ARMOR_L5_1.clone() : null;
            case "ARMOR_L5_2":
                return ItemManager.ARMOR_L5_2 != null ? ItemManager.ARMOR_L5_2.clone() : null;
            case "ARMOR_L5_3":
                return ItemManager.ARMOR_L5_3 != null ? ItemManager.ARMOR_L5_3.clone() : null;
            case "ARMOR_L5_4":
                return ItemManager.ARMOR_L5_4 != null ? ItemManager.ARMOR_L5_4.clone() : null;
            case "ARMOR_L5_5":
                return ItemManager.ARMOR_L5_5 != null ? ItemManager.ARMOR_L5_5.clone() : null;
            
            default:
                // Recipe ID'den silah/armor kontrolü - reflection ile
                if (recipeId.startsWith("WEAPON_") || recipeId.startsWith("ARMOR_")) {
                    try {
                        java.lang.reflect.Field field = ItemManager.class.getField(recipeId);
                        Object value = field.get(null);
                        if (value instanceof ItemStack) {
                            return ((ItemStack) value).clone();
                        }
                    } catch (Exception e) {
                        // Field bulunamadı
                    }
                }
                return new ItemStack(Material.BOOK);
        }
    }
    
    /**
     * Yardımcı metod: Buton oluştur
     */
    private static ItemStack createButton(Material material, String name, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (description != null) {
                List<String> lore = new ArrayList<>();
                lore.add(description);
                meta.setLore(lore);
            }
            // Butonları değiştirilemez yap (glow efekti)
            meta.setUnbreakable(true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * Arka plan doldur (görsel iyileştirme)
     */
    private static void fillBackground(Inventory menu) {
        ItemStack background = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta bgMeta = background.getItemMeta();
        bgMeta.setDisplayName(" ");
        bgMeta.setLore(new ArrayList<>());
        background.setItemMeta(bgMeta);
        
        // Boş slotları doldur (sadece görsel)
        for (int i = 0; i < menu.getSize(); i++) {
            // Crafting grid, sonuç item, butonlar hariç
            if (i != 10 && i != 11 && i != 12 && 
                i != 19 && i != 20 && i != 21 && 
                i != 28 && i != 29 && i != 30 &&
                i != 25 && // Ok işareti
                i != 31 && // Malzeme listesi
                i != 40 && // Sonuç item
                i != 49 && // Bilgi butonu
                i != 53) { // Kapat butonu
                if (menu.getItem(i) == null) {
                    menu.setItem(i, background);
                }
            }
        }
    }
}

