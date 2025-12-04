package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.GhostRecipeManager;
import me.mami.stratocraft.manager.ItemManager;
import me.mami.stratocraft.manager.ResearchManager;
import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Clan;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.Bukkit;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.RayTraceResult;
import java.util.ArrayList;
import java.util.List;

/**
 * GhostRecipeListener - Hayalet tarif sistemi listener'ı
 * Oyuncu tarif kitabına sağ tıkladığında hayalet yapı gösterir
 */
public class GhostRecipeListener implements Listener {
    private final GhostRecipeManager ghostRecipeManager;
    private final ResearchManager researchManager;
    private TerritoryManager territoryManager;
    
    public GhostRecipeListener(GhostRecipeManager grm, ResearchManager rm) {
        this.ghostRecipeManager = grm;
        this.researchManager = rm;
    }
    
    public void setTerritoryManager(TerritoryManager tm) {
        this.territoryManager = tm;
    }
    
    /**
     * Oyuncu tarif kitabına sağ tıkladığında hayalet yapı göster
     */
    @EventHandler
    public void onRecipeBookInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null) return;
        
        // Tarif kitabı kontrolü
        String recipeId = getRecipeIdFromItem(item);
        if (recipeId == null) return;
        
        // Item tarifleri için: Normal sağ tık → GUI menü, Shift+Sağ tık → Chat
        if (isItemRecipe(recipeId)) {
            if (player.isSneaking()) {
                // Shift+Sağ tık: Chat'te göster (eski sistem)
                showCraftingRecipe(player, recipeId);
            } else {
                // Normal sağ tık: GUI menü aç
                openRecipeMenu(player, recipeId);
            }
            event.setCancelled(true);
            return;
        }
        
        // Yapı tarifleri için: Hayalet yapı göster
        // Oyuncunun bu tarife sahip olduğunu kontrol et
        if (!researchManager.hasRecipeBook(player, recipeId)) {
            player.sendMessage("§cBu tarif kitabına sahip değilsin!");
            return;
        }
        
        // Ray trace ile oyuncunun baktığı yönü bul
        RayTraceResult rayTrace = player.rayTraceBlocks(50);
        Location targetLocation;
        
        if (rayTrace != null && rayTrace.getHitBlock() != null) {
            // Blok üzerine tıkladıysa, o blokun bir blok üstünden başla (yer üstüne doğru)
            org.bukkit.block.Block clickedBlock = rayTrace.getHitBlock();
            targetLocation = clickedBlock.getRelative(org.bukkit.block.BlockFace.UP).getLocation();
        } else {
            // Havaya tıkladıysa, oyuncunun önüne 5 blok mesafede
            Location playerLoc = player.getLocation();
            targetLocation = playerLoc.clone().add(playerLoc.getDirection().multiply(5));
            // Havaya tıklanınca da yer üstüne doğru yerleştir
            targetLocation.setY(Math.floor(targetLocation.getY()) + 1);
        }
        
        // Yapı türü kontrolü ve uyarı
        if (territoryManager != null) {
            checkStructureLocationWarning(player, recipeId, targetLocation);
        }
        
        // Hayalet tarifi göster
        ghostRecipeManager.showGhostRecipe(player, recipeId, targetLocation);
        
        event.setCancelled(true);
    }
    
    /**
     * Item tarifi mi kontrol et
     */
    private boolean isItemRecipe(String recipeId) {
        String upperId = recipeId.toUpperCase();
        return upperId.contains("LIGHTNING_CORE") ||
               upperId.contains("TITANIUM") ||
               upperId.contains("DARK_MATTER") ||
               upperId.contains("RED_DIAMOND") ||
               upperId.contains("RUBY") ||
               upperId.contains("ADAMANTITE") ||
               upperId.contains("STAR_CORE") ||
               upperId.contains("FLAME_AMPLIFIER") ||
               upperId.contains("DEVIL") ||
               upperId.contains("WAR_FAN") ||
               upperId.contains("TOWER_SHIELD") ||
               upperId.contains("HELL_FRUIT") ||
               upperId.contains("SULFUR") ||
               upperId.contains("BAUXITE") ||
               upperId.contains("ROCK_SALT") ||
               upperId.contains("MITHRIL") ||
               upperId.contains("ASTRAL") ||
               upperId.contains("HOOK") ||
               upperId.contains("TRAP_CORE");
    }
    
    /**
     * Item tariflerinde crafting recipe göster
     */
    private void showCraftingRecipe(Player player, String recipeId) {
        player.sendMessage("§6§l════════════════════════════");
        player.sendMessage("§e§lCRAFTİNG TARİFİ");
        player.sendMessage("§6§l════════════════════════════");
        
        // Tarif ID'sine göre crafting bilgisi göster
        String upperId = recipeId.toUpperCase();
        switch (upperId) {
            case "LIGHTNING_CORE":
                player.sendMessage("§7Yıldırım Çekirdeği:");
                player.sendMessage("§7[G][E][G]");
                player.sendMessage("§7[E][D][E]");
                player.sendMessage("§7[G][E][G]");
                player.sendMessage("§7G = Altın, E = Ender İncisi, D = Elmas");
                break;
            case "TITANIUM_INGOT":
                player.sendMessage("§7Titanyum Külçesi:");
                player.sendMessage("§7Crafting masasında yapılır.");
                player.sendMessage("§7Titanyum Parçası + Kömür");
                break;
            case "TRAP_CORE":
                player.sendMessage("§7Tuzak Çekirdeği:");
                player.sendMessage("§7[O][E][O]");
                player.sendMessage("§7[I][D][I]");
                player.sendMessage("§7[O][E][O]");
                player.sendMessage("§7O = Obsidyen, E = Ender İncisi");
                player.sendMessage("§7I = Demir, D = Elmas");
                break;
            case "RUSTY_HOOK":
                player.sendMessage("§7Paslı Kanca:");
                player.sendMessage("§7[ ] [I] [ ]");
                player.sendMessage("§7[ ] [I] [ ]");
                player.sendMessage("§7[ ] [S] [ ]");
                player.sendMessage("§7I = Demir (2x), S = İp");
                break;
            default:
                player.sendMessage("§7Bu eşya için crafting tarifi:");
                player.sendMessage("§7Crafting masasında yapılır.");
                player.sendMessage("§7Detaylı tarif için dökümanlara bakın.");
        }
        
        player.sendMessage("§6§l════════════════════════════");
    }
    
    /**
     * Tarif kitabı GUI menüsünü aç
     */
    private void openRecipeMenu(Player player, String recipeId) {
        String recipeIdUpper = recipeId.toUpperCase().replace("RECIPE_", "");
        ItemManager.RecipeInfo info = ItemManager.getRecipeInfo(recipeIdUpper);
        
        if (info == null) {
            player.sendMessage("§cBu tarif için bilgi bulunamadı!");
            return;
        }
        
        Inventory menu = Bukkit.createInventory(null, 27, "§eTarif: " + info.getDisplayName());
        
        // Crafting grid gösterimi (Slot 10-16: 3x3 grid)
        // Slot 10-12: İlk satır
        // Slot 13-15: İkinci satır
        // Slot 16-18: Üçüncü satır (ama 27 slot'ta 16-18 yok, 16-17-18 kullanacağız)
        // Daha iyi: Slot 10-18 kullan (3x3 grid)
        
        // Crafting recipe bilgisi varsa göster
        if (info.getCraftingRecipe() != null && !info.getCraftingRecipe().isEmpty()) {
            // Crafting recipe'yi parse et ve göster
            List<String> recipeLines = info.getCraftingRecipe();
            // İlk 3 satır crafting grid'i
            int slot = 10;
            for (int i = 0; i < Math.min(3, recipeLines.size()); i++) {
                String line = recipeLines.get(i);
                // Basit bir parsing (örnek: "[G][E][G]" -> G, E, G)
                // Daha gelişmiş parsing için ItemManager'dan malzeme bilgisi alınabilir
                ItemStack gridItem = createRecipeGridItem(line);
                menu.setItem(slot, gridItem);
                slot++;
            }
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
            menu.setItem(13, infoItem);
        }
        
        // Sonuç item (Slot 22)
        ItemStack resultItem = getResultItem(recipeIdUpper);
        if (resultItem != null) {
            menu.setItem(22, resultItem);
        }
        
        // Malzeme listesi (Slot 0-8: Lore'da gösterilecek, burada bilgi item'ı)
        ItemStack materialItem = new ItemStack(Material.BOOK);
        ItemMeta materialMeta = materialItem.getItemMeta();
        materialMeta.setDisplayName("§6Gerekli Malzemeler");
        List<String> materialLore = new ArrayList<>();
        if (info.getCraftingRecipe() != null && info.getCraftingRecipe().size() > 3) {
            // 4. satırdan itibaren malzeme açıklamaları
            for (int i = 3; i < info.getCraftingRecipe().size(); i++) {
                materialLore.add("§7" + info.getCraftingRecipe().get(i));
            }
        } else {
            materialLore.add("§7Detaylı bilgi için Shift+Sağ Tık yapın");
        }
        materialMeta.setLore(materialLore);
        materialItem.setItemMeta(materialMeta);
        menu.setItem(4, materialItem);
        
        // Kapat butonu (Slot 26)
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName("§cKapat");
        closeItem.setItemMeta(closeMeta);
        menu.setItem(26, closeItem);
        
        player.openInventory(menu);
    }
    
    /**
     * Crafting grid item'ı oluştur (basit gösterim)
     */
    private ItemStack createRecipeGridItem(String line) {
        // Basit gösterim için crafting table ikonu
        ItemStack item = new ItemStack(Material.CRAFTING_TABLE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§7" + line);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Recipe ID'ye göre sonuç item'ı al
     */
    private ItemStack getResultItem(String recipeId) {
        // ItemManager'dan static field'ları kullan
        switch (recipeId) {
            case "LIGHTNING_CORE":
                return ItemManager.LIGHTNING_CORE;
            case "TITANIUM_INGOT":
                return ItemManager.TITANIUM_INGOT;
            case "TRAP_CORE":
                return ItemManager.TRAP_CORE;
            case "RUSTY_HOOK":
                return ItemManager.RUSTY_HOOK;
            default:
                return new ItemStack(Material.BOOK);
        }
    }
    
    /**
     * Yapı türüne göre konum uyarısı
     */
    private void checkStructureLocationWarning(Player player, String recipeId, Location targetLocation) {
        // Klan yapısı kontrolü
        if (isClanStructure(recipeId)) {
            Clan owner = territoryManager.getTerritoryOwner(targetLocation);
            Clan playerClan = territoryManager.getClanManager().getClanByPlayer(player.getUniqueId());
            
            if (owner == null || !owner.equals(playerClan)) {
                // Dışarıda veya başkasının bölgesinde
                player.sendMessage("§c§l⚠ UYARI!");
                player.sendMessage("§cBu bir §eKlan Yapısı§c'dır!");
                player.sendMessage("§7Sadece §e§lklan bölgeniz içinde§7 yapabilirsiniz.");
                if (playerClan == null) {
                    player.sendMessage("§7Önce bir klana üye olmalısınız.");
                } else if (owner != null && !owner.equals(playerClan)) {
                    player.sendMessage("§7Bu bölge başka bir klana ait!");
                } else {
                    player.sendMessage("§7Klan bölgeniz dışındasınız!");
                }
            }
        }
    }
    
    /**
     * Tarif ID'sinin klan yapısı olup olmadığını kontrol et
     */
    private boolean isClanStructure(String recipeId) {
        // Tüm yapılar klan yapısıdır (HEALING_BEACON ve WARNING_SIGN hariç)
        String upperId = recipeId.toUpperCase();
        return !upperId.equals("HEALING_BEACON") && 
               !upperId.equals("WARNING_SIGN") &&
               !upperId.contains("LIGHTNING_CORE") &&
               !upperId.contains("TITANIUM") &&
               !upperId.contains("DARK_MATTER") &&
               !upperId.contains("RED_DIAMOND") &&
               !upperId.contains("RUBY") &&
               !upperId.contains("ADAMANTITE") &&
               !upperId.contains("STAR_CORE") &&
               !upperId.contains("FLAME_AMPLIFIER") &&
               !upperId.contains("DEVIL") &&
               !upperId.contains("WAR_FAN") &&
               !upperId.contains("TOWER_SHIELD") &&
               !upperId.contains("HELL_FRUIT") &&
               !upperId.contains("SULFUR") &&
               !upperId.contains("BAUXITE") &&
               !upperId.contains("ROCK_SALT") &&
               !upperId.contains("MITHRIL") &&
               !upperId.contains("ASTRAL") &&
               !upperId.contains("HOOK") &&
               !upperId.contains("TRAP_CORE") &&
               !upperId.contains("MAGMA_BATTERY") &&
               !upperId.contains("CLAN_CREATE");
    }
    
    /**
     * Oyuncu hareket ettiğinde mesafe kontrolü yap
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // Sadece blok değişikliğinde kontrol et (performans için)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        
        if (ghostRecipeManager.hasActiveRecipe(player.getUniqueId())) {
            ghostRecipeManager.checkDistance(player);
        }
    }
    
    /**
     * Oyuncu blok koyduğunda tarif tamamlanma kontrolü
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        
        // Hem aktif hem sabit tarifleri kontrol et
        // checkAndRemoveBlock metodu zaten her ikisini de kontrol ediyor
        
        // Blok koyulduğunda kontrol et ve doğru blok ise hayalet görüntüsünü kaldır
        Location blockLocation = event.getBlockPlaced().getLocation();
        Material placedMaterial = event.getBlockPlaced().getType();
        
        ghostRecipeManager.checkAndRemoveBlock(player, blockLocation, placedMaterial);
    }
    
    /**
     * Oyuncu el değiştirdiğinde hayalet tarifi kaldır
     */
    @EventHandler
    public void onItemHeldChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        
        if (ghostRecipeManager.hasActiveRecipe(player.getUniqueId())) {
            // Eğer yeni elinde tarif kitabı yoksa hayalet tarifi kaldır
            ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
            String recipeId = getRecipeIdFromItem(newItem);
            
            if (recipeId == null) {
                ghostRecipeManager.removeGhostRecipe(player);
                player.sendMessage("§cHayalet tarif kaldırıldı.");
            }
        }
    }
    
    /**
     * Oyuncu oyundan çıktığında hayalet tarifi temizle
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        ghostRecipeManager.removeGhostRecipe(event.getPlayer());
    }
    
    /**
     * ItemStack'ten tarif ID'sini çıkar
     */
    private String getRecipeIdFromItem(ItemStack item) {
        if (item == null) return null;
        
        // RECIPE_ prefix'li özel eşyaları kontrol et
        // Yapılar
        if (ItemManager.isCustomItem(item, "RECIPE_CORE")) return "CORE";
        if (ItemManager.isCustomItem(item, "RECIPE_ALCHEMY") || ItemManager.isCustomItem(item, "RECIPE_ALCHEMY_TOWER")) return "ALCHEMY_TOWER";
        if (ItemManager.isCustomItem(item, "RECIPE_POISON_REACTOR")) return "POISON_REACTOR";
        if (ItemManager.isCustomItem(item, "RECIPE_TECTONIC") || ItemManager.isCustomItem(item, "RECIPE_TECTONIC_STABILIZER")) return "TECTONIC_STABILIZER";
        if (ItemManager.isCustomItem(item, "RECIPE_SIEGE_FACTORY")) return "SIEGE_FACTORY";
        if (ItemManager.isCustomItem(item, "RECIPE_WALL_GENERATOR")) return "WALL_GENERATOR";
        if (ItemManager.isCustomItem(item, "RECIPE_GRAVITY_WELL")) return "GRAVITY_WELL";
        if (ItemManager.isCustomItem(item, "RECIPE_LAVA_TRENCHER")) return "LAVA_TRENCHER";
        if (ItemManager.isCustomItem(item, "RECIPE_WATCHTOWER")) return "WATCHTOWER";
        if (ItemManager.isCustomItem(item, "RECIPE_DRONE_STATION")) return "DRONE_STATION";
        if (ItemManager.isCustomItem(item, "RECIPE_AUTO_TURRET")) return "AUTO_TURRET";
        if (ItemManager.isCustomItem(item, "RECIPE_GLOBAL_MARKET_GATE")) return "GLOBAL_MARKET_GATE";
        if (ItemManager.isCustomItem(item, "RECIPE_AUTO_DRILL")) return "AUTO_DRILL";
        if (ItemManager.isCustomItem(item, "RECIPE_XP_BANK")) return "XP_BANK";
        if (ItemManager.isCustomItem(item, "RECIPE_MAG_RAIL")) return "MAG_RAIL";
        if (ItemManager.isCustomItem(item, "RECIPE_TELEPORTER")) return "TELEPORTER";
        if (ItemManager.isCustomItem(item, "RECIPE_FOOD_SILO")) return "FOOD_SILO";
        if (ItemManager.isCustomItem(item, "RECIPE_OIL_REFINERY")) return "OIL_REFINERY";
        if (ItemManager.isCustomItem(item, "RECIPE_HEALING_BEACON")) return "HEALING_BEACON";
        if (ItemManager.isCustomItem(item, "RECIPE_WEATHER_MACHINE")) return "WEATHER_MACHINE";
        if (ItemManager.isCustomItem(item, "RECIPE_CROP_ACCELERATOR")) return "CROP_ACCELERATOR";
        if (ItemManager.isCustomItem(item, "RECIPE_MOB_GRINDER")) return "MOB_GRINDER";
        if (ItemManager.isCustomItem(item, "RECIPE_INVISIBILITY_CLOAK")) return "INVISIBILITY_CLOAK";
        if (ItemManager.isCustomItem(item, "RECIPE_ARMORY")) return "ARMORY";
        if (ItemManager.isCustomItem(item, "RECIPE_LIBRARY")) return "LIBRARY";
        if (ItemManager.isCustomItem(item, "RECIPE_WARNING_SIGN")) return "WARNING_SIGN";
        
        // Bataryalar
        if (ItemManager.isCustomItem(item, "RECIPE_MAGMA_BATTERY")) return "MAGMA_BATTERY";
        
        // Ritüeller
        if (ItemManager.isCustomItem(item, "RECIPE_CLAN_CREATE")) return "CLAN_CREATE";
        
        // Özel eşyalar (hayalet blok göstermez ama tarif ID döndürür)
        if (ItemManager.isCustomItem(item, "RECIPE_LIGHTNING_CORE")) return "LIGHTNING_CORE";
        if (ItemManager.isCustomItem(item, "RECIPE_TITANIUM_INGOT")) return "TITANIUM_INGOT";
        if (ItemManager.isCustomItem(item, "RECIPE_DARK_MATTER")) return "DARK_MATTER";
        if (ItemManager.isCustomItem(item, "RECIPE_RED_DIAMOND")) return "RED_DIAMOND";
        if (ItemManager.isCustomItem(item, "RECIPE_RUBY")) return "RUBY";
        if (ItemManager.isCustomItem(item, "RECIPE_ADAMANTITE")) return "ADAMANTITE";
        if (ItemManager.isCustomItem(item, "RECIPE_STAR_CORE")) return "STAR_CORE";
        if (ItemManager.isCustomItem(item, "RECIPE_FLAME_AMPLIFIER")) return "FLAME_AMPLIFIER";
        if (ItemManager.isCustomItem(item, "RECIPE_DEVIL_HORN")) return "DEVIL_HORN";
        if (ItemManager.isCustomItem(item, "RECIPE_DEVIL_SNAKE_EYE")) return "DEVIL_SNAKE_EYE";
        if (ItemManager.isCustomItem(item, "RECIPE_WAR_FAN")) return "WAR_FAN";
        if (ItemManager.isCustomItem(item, "RECIPE_TOWER_SHIELD")) return "TOWER_SHIELD";
        if (ItemManager.isCustomItem(item, "RECIPE_HELL_FRUIT")) return "HELL_FRUIT";
        if (ItemManager.isCustomItem(item, "RECIPE_SULFUR")) return "SULFUR";
        if (ItemManager.isCustomItem(item, "RECIPE_BAUXITE_INGOT")) return "BAUXITE_INGOT";
        if (ItemManager.isCustomItem(item, "RECIPE_ROCK_SALT")) return "ROCK_SALT";
        if (ItemManager.isCustomItem(item, "RECIPE_MITHRIL_INGOT")) return "MITHRIL_INGOT";
        if (ItemManager.isCustomItem(item, "RECIPE_MITHRIL_STRING")) return "MITHRIL_STRING";
        if (ItemManager.isCustomItem(item, "RECIPE_ASTRAL_CRYSTAL")) return "ASTRAL_CRYSTAL";
        if (ItemManager.isCustomItem(item, "RECIPE_RUSTY_HOOK")) return "RUSTY_HOOK";
        if (ItemManager.isCustomItem(item, "RECIPE_GOLDEN_HOOK")) return "GOLDEN_HOOK";
        if (ItemManager.isCustomItem(item, "RECIPE_TITAN_GRAPPLE")) return "TITAN_GRAPPLE";
        if (ItemManager.isCustomItem(item, "RECIPE_TRAP_CORE")) return "TRAP_CORE";
        
        return null;
    }
    
    /**
     * Yere Shift+Sağ tıklayınca tarifi sabitle
     */
    @EventHandler
    public void onFixRecipe(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        
        Player player = event.getPlayer();
        if (!player.isSneaking()) return; // Shift tuşu basılı mı?
        
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null) return;
        
        // Tarif kitabı kontrolü
        String recipeId = getRecipeIdFromItem(item);
        if (recipeId == null) return;
        
        // Aktif tarif var mı?
        if (!ghostRecipeManager.hasActiveRecipe(player.getUniqueId())) {
            // Aktif tarif yoksa, yeni bir tane göster ve sabitle
            // Tıklanan bloğun bir blok üstünden başla (yer üstüne doğru)
            org.bukkit.block.Block clickedBlock = event.getClickedBlock();
            Location targetLocation = clickedBlock.getRelative(org.bukkit.block.BlockFace.UP).getLocation();
            ghostRecipeManager.showGhostRecipe(player, recipeId, targetLocation);
            ghostRecipeManager.fixGhostRecipe(player, targetLocation);
            event.setCancelled(true);
            return;
        }
        
        // Aktif tarifi sabitle
        // Tıklanan bloğun bir blok üstünden başla (yer üstüne doğru)
        org.bukkit.block.Block clickedBlock = event.getClickedBlock();
        Location targetLocation = clickedBlock.getRelative(org.bukkit.block.BlockFace.UP).getLocation();
        ghostRecipeManager.fixGhostRecipe(player, targetLocation);
        event.setCancelled(true);
    }
    
    /**
     * Shift+Sol tık ile tarifi kaldır
     */
    @EventHandler
    public void onRemoveRecipe(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_AIR) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        
        Player player = event.getPlayer();
        if (!player.isSneaking()) return; // Shift tuşu basılı mı?
        
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null) return;
        
        // Tarif kitabı kontrolü
        String recipeId = getRecipeIdFromItem(item);
        if (recipeId == null) return;
        
        // Aktif tarif var mı?
        if (ghostRecipeManager.hasActiveRecipe(player.getUniqueId())) {
            ghostRecipeManager.removeGhostRecipe(player);
            player.sendMessage("§aHayalet tarif kaldırıldı.");
            event.setCancelled(true);
            return;
        }
        
        // Sabit tarif var mı? (tıklanan blokta)
        if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.getClickedBlock() != null) {
            Location clickedLoc = event.getClickedBlock().getLocation();
            if (ghostRecipeManager.hasFixedRecipeAt(clickedLoc)) {
                if (ghostRecipeManager.removeFixedRecipeAt(clickedLoc)) {
                    player.sendMessage("§aSabit tarif kaldırıldı.");
                    event.setCancelled(true);
                }
            } else {
                player.sendMessage("§7Bu konumda sabit tarif yok.");
            }
        }
    }
}

