package me.mami.stratocraft.listener;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.enums.StructureOwnershipType;
import me.mami.stratocraft.enums.StructureType;
import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Structure;
import me.mami.stratocraft.util.StructureOwnershipHelper;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.UUID;

/**
 * Yapı Menü Listener
 * 
 * Yapılara sağ tık ile menü açma sistemi
 */
public class StructureMenuListener implements Listener {
    private final Main plugin;
    private final ClanManager clanManager;
    private final TerritoryManager territoryManager;
    
    // Cooldown: Oyuncu UUID -> Son tıklama zamanı
    private final java.util.Map<UUID, Long> clickCooldowns = new java.util.concurrent.ConcurrentHashMap<>();
    private static final long CLICK_COOLDOWN = 1000L; // 1 saniye
    
    public StructureMenuListener(Main plugin, ClanManager clanManager, TerritoryManager territoryManager) {
        this.plugin = plugin;
        this.clanManager = clanManager;
        this.territoryManager = territoryManager;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onStructureInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        if (event.getHand() != EquipmentSlot.HAND)
            return;
        if (event.getPlayer().isSneaking())
            return; // Shift + Sağ Tık aktivasyon için
        
        Player player = event.getPlayer();
        Block clicked = event.getClickedBlock();
        if (clicked == null)
            return;
        
        // Cooldown kontrolü
        if (isOnCooldown(player.getUniqueId())) {
            return;
        }
        
        // Yapı kontrolü
        Structure structure = findStructureAt(clicked.getLocation());
        if (structure == null)
            return;
        
        // YENİ: Yapı sahiplik kontrolü
        if (!checkStructureOwnership(player, structure)) {
            return; // Yetki yok, mesaj zaten gönderildi
        }
        
        event.setCancelled(true);
        setCooldown(player.getUniqueId());
        
        // Yapı tipine göre menü aç
        openMenuForStructure(player, structure);
    }
    
    /**
     * Konumdaki yapıyı bul
     * ✅ DÜZELTME: Kişisel yapılar klansız bölgede de olabilir, bu yüzden tüm klanları kontrol et
     */
    private Structure findStructureAt(org.bukkit.Location location) {
        // Tüm klanları kontrol et
        for (Clan clan : clanManager.getAllClans()) {
            for (Structure structure : clan.getStructures()) {
                if (structure.getLocation().distance(location) <= 2.0) {
                    // Kişisel yapılar herkese açık, klan yapıları klan kontrolü gerektirir
                    // YENİ: StructureType kullan
                    me.mami.stratocraft.enums.StructureType type = 
                        me.mami.stratocraft.enums.StructureType.valueOf(structure.getType().name());
                    if (type == me.mami.stratocraft.enums.StructureType.PERSONAL_MISSION_GUILD ||
                        type == me.mami.stratocraft.enums.StructureType.CONTRACT_OFFICE ||
                        type == me.mami.stratocraft.enums.StructureType.MARKET_PLACE ||
                        type == me.mami.stratocraft.enums.StructureType.RECIPE_LIBRARY) {
                        // Kişisel yapılar: Herkese açık
                        return structure;
                    } else {
                        // Klan yapıları: Klan kontrolü yapılacak (openMenuForStructure'da)
                        return structure;
                    }
                }
            }
        }
        
        // ✅ DÜZELTME: Eğer klanlarda bulunamadıysa, oyuncunun klanını kontrol et
        // (Yapı aktivasyon sonrası oyuncunun klanına eklenmiş olabilir)
        Player nearbyPlayer = null;
        double minDistance = Double.MAX_VALUE;
        for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
            double dist = p.getLocation().distance(location);
            if (dist < 10 && dist < minDistance) {
                minDistance = dist;
                nearbyPlayer = p;
            }
        }
        
        if (nearbyPlayer != null) {
            Clan playerClan = clanManager.getClanByPlayer(nearbyPlayer.getUniqueId());
            if (playerClan != null) {
                for (Structure structure : playerClan.getStructures()) {
                    if (structure.getLocation().distance(location) <= 2.0) {
                        // YENİ: StructureType kullan
                        me.mami.stratocraft.enums.StructureType type = 
                            me.mami.stratocraft.enums.StructureType.valueOf(structure.getType().name());
                        if (type == me.mami.stratocraft.enums.StructureType.PERSONAL_MISSION_GUILD ||
                            type == me.mami.stratocraft.enums.StructureType.CONTRACT_OFFICE ||
                            type == me.mami.stratocraft.enums.StructureType.MARKET_PLACE ||
                            type == me.mami.stratocraft.enums.StructureType.RECIPE_LIBRARY) {
                            return structure;
                        }
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Yapı sahiplik kontrolü
     * 
     * @param player Oyuncu
     * @param structure Yapı
     * @return Yetki var mı?
     */
    private boolean checkStructureOwnership(Player player, Structure structure) {
        // StructureType'a çevir
        StructureType type;
        try {
            type = StructureType.valueOf(structure.getType().name());
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cYapı tipi geçersiz!");
            return false;
        }
        
        // Sahiplik tipini al
        StructureOwnershipType ownershipType = StructureOwnershipHelper.getOwnershipType(type);
        
        // PUBLIC yapılar için kontrol yok
        if (ownershipType == StructureOwnershipType.PUBLIC) {
            return true;
        }
        
        // CLAN_ONLY yapılar için klan kontrolü
        if (ownershipType == StructureOwnershipType.CLAN_ONLY) {
            Clan playerClan = clanManager.getClanByPlayer(player.getUniqueId());
            if (playerClan == null) {
                player.sendMessage("§cBu yapıya erişim için bir klana üye olmalısınız!");
                return false;
            }
            
            // Yapının bulunduğu bölgenin sahibi kontrolü
            Clan owner = territoryManager.getTerritoryOwner(structure.getLocation());
            if (owner == null || !owner.equals(playerClan)) {
                player.sendMessage("§cBu yapıya erişim yetkiniz yok! (Klan bölgesi dışında)");
                return false;
            }
            
            return true;
        }
        
        // CLAN_OWNED yapılar için yapan oyuncu veya klan kontrolü
        if (ownershipType == StructureOwnershipType.CLAN_OWNED) {
            // Yapı sahibi kontrolü (ileride yapı modeline ownerId eklenecek)
            // Şimdilik klan kontrolü yapıyoruz
            Clan playerClan = clanManager.getClanByPlayer(player.getUniqueId());
            if (playerClan == null) {
                player.sendMessage("§cBu yapıya erişim için bir klana üye olmalısınız!");
                return false;
            }
            
            // Yapının bulunduğu bölgenin sahibi kontrolü
            Clan owner = territoryManager.getTerritoryOwner(structure.getLocation());
            if (owner != null && owner.equals(playerClan)) {
                return true; // Klan bölgesinde, erişim var
            }
            
            // YENİ: OwnerId kontrolü (CLAN_OWNED yapılar için)
            UUID structureOwnerId = structure.getOwnerId();
            if (structureOwnerId != null) {
                // Yapı sahibi kontrolü
                if (structureOwnerId.equals(player.getUniqueId())) {
                    return true; // Yapı sahibi, erişim var
                }
                // Yapı sahibinin klanı kontrolü
                Clan structureOwnerClan = clanManager.getClanByPlayer(structureOwnerId);
                if (structureOwnerClan != null && structureOwnerClan.equals(playerClan)) {
                    return true; // Yapı sahibinin klanı, erişim var
                }
                return false; // Yapı sahibi veya klanı değil, erişim yok
            }
            
            // OwnerId yoksa klan kontrolü yeterli (geriye uyumluluk)
            return true;
        }
        
        return false;
    }
    
    /**
     * Yapı tipine göre menü aç
     */
    private void openMenuForStructure(Player player, Structure structure) {
        // YENİ: StructureType kullan (geriye uyumluluk için dönüştür)
        me.mami.stratocraft.enums.StructureType type;
        try {
            type = me.mami.stratocraft.enums.StructureType.valueOf(structure.getType().name());
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cYapı tipi geçersiz!");
            return;
        }
        
        switch (type) {
            case PERSONAL_MISSION_GUILD:
                // Kişisel Görev Loncası
                if (plugin.getMissionManager() != null) {
                    me.mami.stratocraft.model.Mission mission = plugin.getMissionManager().getActiveMission(player.getUniqueId());
                    if (mission != null) {
                        me.mami.stratocraft.gui.MissionMenu.openMenu(player, mission, plugin.getMissionManager());
                    } else {
                        player.sendMessage("§eAktif göreviniz yok!");
                        player.sendMessage("§7Yeni görev almak için Totem'e sağ tıklayın.");
                    }
                }
                break;
                
            case CLAN_MANAGEMENT_CENTER:
                // Klan Yönetim Merkezi
                Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
                if (clan == null) {
                    player.sendMessage("§cBir klana üye değilsiniz!");
                    return;
                }
                
                // YENİ: Yetki kontrolü (Lider veya General)
                Clan.Rank rank = clan.getRank(player.getUniqueId());
                if (rank != Clan.Rank.LEADER && rank != Clan.Rank.GENERAL) {
                    player.sendMessage("§cBu menüye erişim yetkiniz yok! (Lider/General)");
                    return;
                }
                
                // Klan bölgesinde mi kontrol
                Clan owner = territoryManager.getTerritoryOwner(structure.getLocation());
                if (owner == null || !owner.equals(clan)) {
                    player.sendMessage("§cBu yapıya erişim yetkiniz yok!");
                    return;
                }
                
                // YENİ: Klan Alanı Yönetim Menüsü
                if (plugin.getClanTerritoryMenu() != null) {
                    plugin.getClanTerritoryMenu().openMenu(player);
                } else {
                    // Fallback: Eski klan menüsü
                    if (plugin.getClanMenu() != null) {
                        plugin.getClanMenu().openMenu(player);
                    }
                }
                break;
                
            case CLAN_BANK:
                // Klan Bankası
                clan = clanManager.getClanByPlayer(player.getUniqueId());
                if (clan == null) {
                    player.sendMessage("§cBir klana üye değilsiniz!");
                    return;
                }
                owner = territoryManager.getTerritoryOwner(structure.getLocation());
                if (owner == null || !owner.equals(clan)) {
                    player.sendMessage("§cBu yapıya erişim yetkiniz yok!");
                    return;
                }
                if (plugin.getClanBankMenu() != null) {
                    plugin.getClanBankMenu().openMainMenu(player);
                }
                break;
                
            case CLAN_MISSION_GUILD:
                // Klan Görev Loncası
                clan = clanManager.getClanByPlayer(player.getUniqueId());
                if (clan == null) {
                    player.sendMessage("§cBir klana üye değilsiniz!");
                    return;
                }
                owner = territoryManager.getTerritoryOwner(structure.getLocation());
                if (owner == null || !owner.equals(clan)) {
                    player.sendMessage("§cBu yapıya erişim yetkiniz yok!");
                    return;
                }
                if (plugin.getClanMissionMenu() != null) {
                    plugin.getClanMissionMenu().openMenu(player);
                }
                break;
                
            case TRAINING_ARENA:
                // Eğitim Alanı
                clan = clanManager.getClanByPlayer(player.getUniqueId());
                if (clan == null) {
                    player.sendMessage("§cBir klana üye değilsiniz!");
                    return;
                }
                owner = territoryManager.getTerritoryOwner(structure.getLocation());
                if (owner == null || !owner.equals(clan)) {
                    player.sendMessage("§cBu yapıya erişim yetkiniz yok!");
                    return;
                }
                // Eğitilmiş Canlılar menüsü aç (klan modu)
                if (plugin.getTamingMenu() != null) {
                    plugin.getTamingMenu().openMainMenu(player, false);
                }
                break;
                
            case CARAVAN_STATION:
                // Kervan İstasyonu
                clan = clanManager.getClanByPlayer(player.getUniqueId());
                if (clan == null) {
                    player.sendMessage("§cBir klana üye değilsiniz!");
                    return;
                }
                owner = territoryManager.getTerritoryOwner(structure.getLocation());
                if (owner == null || !owner.equals(clan)) {
                    player.sendMessage("§cBu yapıya erişim yetkiniz yok!");
                    return;
                }
                if (plugin.getCaravanMenu() != null) {
                    plugin.getCaravanMenu().openMainMenu(player);
                }
                break;
                
            case CONTRACT_OFFICE:
                // Kontrat Bürosu (genel)
                if (plugin.getContractMenu() != null) {
                    plugin.getContractMenu().openMainMenu(player, 0);
                }
                break;
                
            case MARKET_PLACE:
                // Market (genel) - Tüm shopları listele
                if (plugin.getShopManager() != null) {
                    java.util.List<me.mami.stratocraft.model.Shop> shops = plugin.getShopManager().getAllShops();
                    if (shops.isEmpty()) {
                        player.sendMessage("§eHenüz hiç market yok!");
                        player.sendMessage("§7Market kurmak için Chest + Sign kullanın.");
                    } else {
                        player.openInventory(me.mami.stratocraft.gui.ShopMenu.createMarketListMenu(shops, 1));
                    }
                }
                break;
                
            case RECIPE_LIBRARY:
                // Tarif Kütüphanesi (genel) - Tüm tarifleri listele
                player.openInventory(me.mami.stratocraft.gui.RecipeMenu.createRecipeLibraryMenu(player, 1));
                break;
                
            case ALCHEMY_TOWER:
            case TECTONIC_STABILIZER:
            case GLOBAL_MARKET_GATE:
            case POISON_REACTOR:
            case AUTO_TURRET:
            case HEALING_BEACON:
            case XP_BANK:
            case TELEPORTER:
            case AUTO_DRILL:
            case MAG_RAIL:
            case FOOD_SILO:
            case OIL_REFINERY:
            case WEATHER_MACHINE:
            case CROP_ACCELERATOR:
            case MOB_GRINDER:
            case INVISIBILITY_CLOAK:
            case ARMORY:
            case LIBRARY:
            case WALL_GENERATOR:
            case SIEGE_FACTORY:
            case GRAVITY_WELL:
            case LAVA_TRENCHER:
            case WATCHTOWER:
            case DRONE_STATION:
            case WARNING_SIGN:
            case CORE:
                // ✅ DÜZELTME: Bu yapılar için genel yapı menüsü aç
                // Klan yapıları için kontrol
                clan = clanManager.getClanByPlayer(player.getUniqueId());
                if (clan == null) {
                    player.sendMessage("§cBir klana üye değilsiniz!");
                    return;
                }
                owner = territoryManager.getTerritoryOwner(structure.getLocation());
                if (owner == null || !owner.equals(clan)) {
                    player.sendMessage("§cBu yapıya erişim yetkiniz yok!");
                    return;
                }
                // Genel yapı detay menüsü (ClanStructureMenu'dan)
                if (plugin.getClanStructureMenu() != null) {
                    plugin.getClanStructureMenu().openStructureDetailMenu(player, structure);
                } else {
                    player.sendMessage("§e" + structure.getType().name() + " yapısı aktif!");
                    player.sendMessage("§7Yapı seviyesi: " + structure.getLevel());
                }
                break;
                
            default:
                // Diğer yapı tipleri için menü yok
                player.sendMessage("§eBu yapı için menü bulunmuyor.");
                break;
        }
        
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Cooldown kontrolü
     */
    private boolean isOnCooldown(UUID playerId) {
        if (!clickCooldowns.containsKey(playerId))
            return false;
        
        long lastTime = clickCooldowns.get(playerId);
        long currentTime = System.currentTimeMillis();
        
        return (currentTime - lastTime) < CLICK_COOLDOWN;
    }
    
    /**
     * Cooldown ayarla
     */
    private void setCooldown(UUID playerId) {
        clickCooldowns.put(playerId, System.currentTimeMillis());
    }
}

