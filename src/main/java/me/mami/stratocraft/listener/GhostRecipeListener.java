package me.mami.stratocraft.listener;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.gui.RecipeMenu;
import me.mami.stratocraft.manager.GhostRecipeManager;
import me.mami.stratocraft.manager.ItemManager;
import me.mami.stratocraft.manager.ResearchManager;
import me.mami.stratocraft.manager.StructureCoreManager;
import me.mami.stratocraft.manager.StructureRecipeManager;
import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Clan;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.util.RayTraceResult;

/**
 * GhostRecipeListener - Hayalet tarif sistemi listener'ı
 * Oyuncu tarif kitabına sağ tıkladığında hayalet yapı gösterir
 */
public class GhostRecipeListener implements Listener {
    private final GhostRecipeManager ghostRecipeManager;
    private final ResearchManager researchManager;
    private TerritoryManager territoryManager;
    private StructureCoreManager structureCoreManager;
    private StructureRecipeManager structureRecipeManager;
    private Main plugin;
    
    public GhostRecipeListener(GhostRecipeManager grm, ResearchManager rm) {
        this.ghostRecipeManager = grm;
        this.researchManager = rm;
    }
    
    public void setTerritoryManager(TerritoryManager tm) {
        this.territoryManager = tm;
    }
    
    public void setStructureCoreManager(StructureCoreManager scm) {
        this.structureCoreManager = scm;
    }
    
    public void setStructureRecipeManager(StructureRecipeManager srm) {
        this.structureRecipeManager = srm;
    }
    
    public void setPlugin(Main plugin) {
        this.plugin = plugin;
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
                Inventory menu = RecipeMenu.createRecipeMenu(recipeId);
                player.openInventory(menu);
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
               upperId.contains("TRAP_CORE") ||
               upperId.contains("WEAPON_") ||
               upperId.contains("ARMOR_");
               // NOT: BATTERY tarifleri hayalet yapı gösterir, GUI menü değil
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
        
        // YENİ: Tarif tamamlanma kontrolü (hayalet tarif varsa)
        boolean wasRecipeActive = ghostRecipeManager.hasActiveRecipe(player.getUniqueId());
        
        ghostRecipeManager.checkAndRemoveBlock(player, blockLocation, placedMaterial);
        
        // YENİ: Tarif tamamlandıysa efekt göster (GhostRecipeManager içinde zaten yapılıyor)
        // Ayrıca yapı çekirdeği yakınında yapı tamamlanma kontrolü
        checkStructureCompletionNearCore(player, blockLocation);
    }
    
    /**
     * YENİ: Yapı çekirdeği yakınında yapı tamamlanma kontrolü
     * Oyuncu bir yapı çekirdeği koyduysa ve 5 blok yakınındaysa kontrol yap
     */
    private void checkStructureCompletionNearCore(Player player, Location blockLocation) {
        if (structureCoreManager == null || structureRecipeManager == null || plugin == null) {
            return;
        }
        
        // Oyuncunun 5 blok yakınındaki inaktif çekirdekleri kontrol et
        for (Location coreLoc : structureCoreManager.getAllInactiveCoreBlocks().keySet()) {
            if (coreLoc == null || coreLoc.getWorld() == null) continue;
            if (!coreLoc.getWorld().equals(blockLocation.getWorld())) continue;
            
            double distance = coreLoc.distance(blockLocation);
            if (distance > 5.0) continue; // 5 bloktan uzaksa atla
            
            // Çekirdek sahibi kontrolü
            UUID coreOwner = structureCoreManager.getCoreOwner(coreLoc);
            if (coreOwner == null || !coreOwner.equals(player.getUniqueId())) {
                continue; // Bu oyuncunun çekirdeği değil
            }
            
            // Yapı tipini tespit et (StructureCoreBlock'tan al)
            me.mami.stratocraft.model.block.StructureCoreBlock coreBlock = 
                structureCoreManager.getInactiveCoreBlock(coreLoc);
            if (coreBlock == null) continue;
            
            me.mami.stratocraft.enums.StructureType structureType = coreBlock.getStructureType();
            if (structureType == null) {
                // Yapı tipi belirlenmemiş, pattern'den tespit et
                // StructureActivationListener'daki pattern detection kullanılabilir
                // Şimdilik atla, aktivasyon sırasında tespit edilecek
                continue;
            }
            
            // Yapı doğrulama (async - performanslı)
            structureRecipeManager.validateStructureAsync(coreLoc, structureType, (isValid) -> {
                if (isValid) {
                    // Yapı tamamlandı! Efekt göster
                    org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                        Location centerLoc = coreLoc.clone().add(0.5, 0.5, 0.5);
                        coreLoc.getWorld().spawnParticle(Particle.TOTEM, centerLoc, 50, 0.5, 1, 0.5, 0.3);
                        coreLoc.getWorld().spawnParticle(Particle.END_ROD, centerLoc, 30, 0.5, 1, 0.5, 0.1);
                        coreLoc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, centerLoc, 20, 0.5, 1, 0.5, 0.1);
                        player.playSound(coreLoc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                        player.playSound(coreLoc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                        player.sendMessage("§a§l✓ Yapı tamamlandı! Aktivasyon item'ı ile aktifleştirebilirsiniz.");
                    });
                }
            });
        }
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
        
        // Yeni 75 Batarya Tarifleri
        // ATTACK bataryaları (L1-L5, her seviyede 5)
        for (int level = 1; level <= 5; level++) {
            for (int num = 1; num <= 5; num++) {
                String recipeId = "RECIPE_BATTERY_ATTACK_L" + level + "_" + num;
                if (ItemManager.isCustomItem(item, recipeId)) {
                    return "BATTERY_ATTACK_L" + level + "_" + num;
                }
            }
        }
        
        // CONSTRUCTION bataryaları (L1-L5, her seviyede 5)
        for (int level = 1; level <= 5; level++) {
            for (int num = 1; num <= 5; num++) {
                String recipeId = "RECIPE_BATTERY_CONSTRUCTION_L" + level + "_" + num;
                if (ItemManager.isCustomItem(item, recipeId)) {
                    return "BATTERY_CONSTRUCTION_L" + level + "_" + num;
                }
            }
        }
        
        // SUPPORT bataryaları (L1-L5, her seviyede 5)
        for (int level = 1; level <= 5; level++) {
            for (int num = 1; num <= 5; num++) {
                String recipeId = "RECIPE_BATTERY_SUPPORT_L" + level + "_" + num;
                if (ItemManager.isCustomItem(item, recipeId)) {
                    return "BATTERY_SUPPORT_L" + level + "_" + num;
                }
            }
        }
        
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
        
        // Silah tarif kitapları (RECIPE_WEAPON_L1_1 -> WEAPON_L1_1)
        for (int level = 1; level <= 5; level++) {
            for (int variant = 1; variant <= 5; variant++) {
                String recipeId = "RECIPE_WEAPON_L" + level + "_" + variant;
                if (ItemManager.isCustomItem(item, recipeId)) {
                    return "WEAPON_L" + level + "_" + variant;
                }
            }
        }
        
        // Zırh tarif kitapları (RECIPE_ARMOR_L1_1 -> ARMOR_L1_1)
        for (int level = 1; level <= 5; level++) {
            for (int variant = 1; variant <= 5; variant++) {
                String recipeId = "RECIPE_ARMOR_L" + level + "_" + variant;
                if (ItemManager.isCustomItem(item, recipeId)) {
                    return "ARMOR_L" + level + "_" + variant;
                }
            }
        }
        
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
                ghostRecipeManager.removeFixedRecipeAt(clickedLoc);
                player.sendMessage("§aSabit tarif kaldırıldı.");
                event.setCancelled(true);
            } else {
                player.sendMessage("§7Bu konumda sabit tarif yok.");
            }
        }
    }
    
    /**
     * Tarif kitabı GUI menüsü tıklama işlemleri - TAM KORUMA
     * Tüm item transferlerini engeller, sadece butonlar çalışır
     */
    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onRecipeMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        // Adventure API - güvenli title çevirme
        String title = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        
        // Title kontrolü - daha esnek (renk kodları olmadan da çalışır)
        // Tarif Kütüphanesi menüsü de kontrol edilmeli
        if (!title.contains("Tarif:") && !title.contains("Tarif Bulunamadı") && !title.contains("Tarif Kütüphanesi")) return;
        
        // KRİTİK: TÜM TIKLAMALARI ENGELLE (en başta)
        event.setCancelled(true);
        event.setResult(org.bukkit.event.Event.Result.DENY);
        
        Player player = (Player) event.getWhoClicked();
        Inventory topInventory = event.getView().getTopInventory();
        Inventory clickedInventory = event.getClickedInventory();
        int slot = event.getSlot();
        int rawSlot = event.getRawSlot();
        ItemStack clicked = event.getCurrentItem();
        
        // Tarif Kütüphanesi menüsü kontrolü
        if (title.contains("Tarif Kütüphanesi")) {
            // Sayfa numarasını al
            int currentPage = 1;
            try {
                String pageStr = title.split(" - Sayfa ")[1];
                currentPage = Integer.parseInt(pageStr);
            } catch (Exception e) {
                // Sayfa numarası parse edilemedi
            }
            
            if (clicked != null) {
                if (clicked.getType() == Material.ARROW) {
                    if (slot == 45) {
                        // Önceki sayfa
                        player.openInventory(RecipeMenu.createRecipeLibraryMenu(player, currentPage - 1));
                    } else if (slot == 53) {
                        // Sonraki sayfa
                        player.openInventory(RecipeMenu.createRecipeLibraryMenu(player, currentPage + 1));
                    }
                } else if (clicked.getType() == Material.BARRIER) {
                    player.closeInventory();
                } else {
                    // Tarif seçildi - Tarif detay menüsünü aç
                    String recipeId = getRecipeIdFromItem(clicked);
                    if (recipeId != null) {
                        player.openInventory(RecipeMenu.createRecipeMenu(recipeId));
                    }
                }
            }
            return;
        }
        
        // GUI envanterinden tıklama kontrolü
        if (clickedInventory != null && clickedInventory.equals(topInventory)) {
            // GUI envanterinden tıklama - item transfer engelle
            
            if (clicked == null || clicked.getType() == Material.AIR) {
                // Boş slot - engelle
                return;
            }
            
            // Buton tıklamalarını işle
            // Kapat butonu (Slot 53)
            if (slot == 53 && clicked.getType() == Material.BARRIER) {
                player.closeInventory();
                player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                return;
            }
            
            // Bilgi butonu (Slot 49) - Detaylı bilgi göster
            if (slot == 49 && clicked.getType() == Material.BOOK) {
                player.sendMessage("§6§l════════════════════════════");
                player.sendMessage("§e§lTARİF BİLGİSİ");
                player.sendMessage("§6§l════════════════════════════");
                if (clicked.hasItemMeta() && clicked.getItemMeta().hasLore()) {
                    for (String line : clicked.getItemMeta().getLore()) {
                        player.sendMessage(line);
                    }
                }
                player.sendMessage("§6§l════════════════════════════");
                player.playSound(player.getLocation(), org.bukkit.Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
                return;
            }
            
            // Malzeme listesi butonu (Slot 31) - Chat'te göster
            if (slot == 31 && clicked.getType() == Material.BOOK) {
                player.sendMessage("§6§l════════════════════════════");
                player.sendMessage("§e§lGEREKLİ MALZEMELER");
                player.sendMessage("§6§l════════════════════════════");
                if (clicked.hasItemMeta() && clicked.getItemMeta().hasLore()) {
                    for (String line : clicked.getItemMeta().getLore()) {
                        player.sendMessage(line);
                    }
                }
                player.sendMessage("§6§l════════════════════════════");
                player.playSound(player.getLocation(), org.bukkit.Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
                return;
            }
            
            // Sonuç item (Slot 40) - Item bilgisi göster
            if (slot == 40 && clicked.hasItemMeta()) {
                ItemMeta meta = clicked.getItemMeta();
                player.sendMessage("§6§l════════════════════════════");
                player.sendMessage("§e§lCRAFT EDİLECEK İTEM");
                player.sendMessage("§6§l════════════════════════════");
                player.sendMessage("§7İsim: §e" + meta.getDisplayName());
                if (meta.hasLore()) {
                    for (String line : meta.getLore()) {
                        if (!line.contains("═══════════════════")) {
                            player.sendMessage(line);
                        }
                    }
                }
                player.sendMessage("§6§l════════════════════════════");
                player.playSound(player.getLocation(), org.bukkit.Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
                return;
            }
            
            // Crafting grid slotları (10-12, 19-21, 28-30) - Sadece görsel, tıklama sesi
            if ((slot >= 10 && slot <= 12) || (slot >= 19 && slot <= 21) || (slot >= 28 && slot <= 30)) {
                player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 0.8f);
                return;
            }
            
            // Ok işareti (Slot 25) - Sadece görsel
            if (slot == 25) {
                return;
            }
            
            // Diğer tüm GUI slotları - engelle
            return;
        }
        
        // Oyuncu envanterinden GUI'ye transfer - ENGELLE
        if (clickedInventory != null && clickedInventory.equals(player.getInventory())) {
            // Oyuncu envanterinden GUI'ye item koyma - ENGELLE
            if (rawSlot >= 0 && rawSlot < topInventory.getSize()) {
                // GUI slotuna item koymaya çalışıyor - engelle (zaten setCancelled(true) yapıldı)
                return;
            }
        }
        
        // Shift+Click, Number Key, Middle Click - TÜMÜNÜ ENGELLE
        if (rawSlot >= 0 && rawSlot < topInventory.getSize()) {
            // GUI slotuna herhangi bir şekilde item koymaya çalışıyor - engelle (zaten setCancelled(true) yapıldı)
            return;
        }
        
        // GUI'den oyuncu envanterine transfer - ENGELLE
        if (rawSlot >= topInventory.getSize() && rawSlot < topInventory.getSize() + 36) {
            // GUI'den oyuncu envanterine item alma - ENGELLE
            // (Bu durumda clickedInventory topInventory olmalı)
            if (clickedInventory != null && clickedInventory.equals(topInventory)) {
                // Zaten setCancelled(true) yapıldı, ekstra bir şey yapmaya gerek yok
                return;
            }
        }
    }
    
    /**
     * Tarif kitabı GUI menüsü drag işlemleri - SÜRÜKLEME ENGELLEME
     * Basit ve etkili yaklaşım
     */
    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onRecipeMenuDrag(org.bukkit.event.inventory.InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        // Adventure API - güvenli title çevirme
        String title = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if (!title.startsWith("§eTarif:") && !title.startsWith("§cTarif Bulunamadı")) return;
        
        // KRİTİK: TÜM DRAG İŞLEMLERİNİ ENGELLE
        event.setCancelled(true);
        event.setResult(org.bukkit.event.Event.Result.DENY);
        
        // GUI envanterine drag işlemini kontrol et
        Inventory topInventory = event.getView().getTopInventory();
        for (int slot : event.getRawSlots()) {
            if (slot < topInventory.getSize()) {
                // GUI slotuna drag yapılmaya çalışılıyor - uyarı ver
                Player player = (Player) event.getWhoClicked();
                player.sendMessage("§cBu menüde item taşıyamazsınız!");
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
                break;
            }
        }
    }
    
    /**
     * Tarif kitabı GUI menüsü kapatma işlemleri
     */
    @EventHandler
    public void onRecipeMenuClose(org.bukkit.event.inventory.InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        
        // Adventure API - güvenli title çevirme
        String title = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if (title.startsWith("§eTarif:") || title.startsWith("§cTarif Bulunamadı")) {
            // Menü kapatıldı - ses efekti (opsiyonel)
            // Player player = (Player) event.getPlayer();
            // player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.3f, 0.5f);
        }
    }
}

