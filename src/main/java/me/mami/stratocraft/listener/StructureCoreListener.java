package me.mami.stratocraft.listener;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.manager.ItemManager;
import me.mami.stratocraft.manager.StructureActivationItemManager;
import me.mami.stratocraft.manager.StructureCoreManager;
import me.mami.stratocraft.manager.StructureRecipeManager;
import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Structure;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Yapı Çekirdeği Listener
 * 
 * Sorumluluklar:
 * - Yapı çekirdeği yerleştirme
 * - Yapı aktivasyonu (item ile)
 * - Yapı menü açma (aktif yapılara sağ tık)
 * 
 * Thread-Safe: Cooldown için ConcurrentHashMap
 */
public class StructureCoreListener implements Listener {
    
    private final Main plugin;
    private final StructureCoreManager coreManager;
    private final StructureRecipeManager recipeManager;
    private final StructureActivationItemManager activationItemManager;
    private final ClanManager clanManager;
    private final TerritoryManager territoryManager;
    
    // Cooldown: Player UUID -> Son aktivasyon zamanı
    private final ConcurrentHashMap<UUID, Long> activationCooldowns = new ConcurrentHashMap<>();
    private static final long ACTIVATION_COOLDOWN = 2000L; // 2 saniye
    
    public StructureCoreListener(Main plugin, 
                                StructureCoreManager coreManager,
                                StructureRecipeManager recipeManager,
                                StructureActivationItemManager activationItemManager,
                                ClanManager clanManager,
                                TerritoryManager territoryManager) {
        this.plugin = plugin;
        this.coreManager = coreManager;
        this.recipeManager = recipeManager;
        this.activationItemManager = activationItemManager;
        this.clanManager = clanManager;
        this.territoryManager = territoryManager;
    }
    
    /**
     * Yapı çekirdeği yerleştirme
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onStructureCorePlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (!ItemManager.isCustomItem(item, "STRUCTURE_CORE")) {
            return;
        }
        
        Block placed = event.getBlockPlaced();
        Location coreLoc = placed.getLocation();
        Player player = event.getPlayer();
        
        // Yapı çekirdeği olarak işaretle
        coreManager.addInactiveCore(coreLoc, player.getUniqueId());
        
        // Mesaj
        player.sendMessage("§a§l✓ Yapı çekirdeği yerleştirildi!");
        player.sendMessage("§7Etrafına yapıyı kur ve aktivasyon item'ı ile aktifleştir.");
        
        // Efekt
        player.playSound(coreLoc, Sound.BLOCK_END_CRYSTAL_PLACE, 1.0f, 1.0f);
        Location particleLoc = coreLoc.clone().add(0.5, 0.5, 0.5);
        coreLoc.getWorld().spawnParticle(Particle.END_ROD, particleLoc, 20, 0.3, 0.3, 0.3, 0.1);
    }
    
    /**
     * Yapı aktivasyonu ve menü açma
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onStructureCoreInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        
        Player player = event.getPlayer();
        Block clicked = event.getClickedBlock();
        if (clicked == null) return;
        
        Location coreLoc = clicked.getLocation();
        
        // Yapı çekirdeği kontrolü
        if (!coreManager.isStructureCore(clicked)) {
            return;
        }
        
        // Aktif yapı kontrolü (menü açma)
        Structure activeStructure = coreManager.getActiveStructure(coreLoc);
        if (activeStructure != null) {
            // Aktif yapıya sağ tık → Menü aç (StructureMenuListener'da işlenecek)
            // Burada sadece event'i iptal et
            event.setCancelled(true);
            return;
        }
        
        // Inaktif çekirdek → Aktivasyon işlemi
        if (!coreManager.isInactiveCore(coreLoc)) {
            return;
        }
        
        event.setCancelled(true);
        
        // Cooldown kontrolü
        if (isOnCooldown(player.getUniqueId())) {
            player.sendMessage("§cYapı aktivasyonu için beklemen gerekiyor!");
            return;
        }
        
        // Aktivasyon item'ı kontrolü
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem == null || handItem.getType() == Material.AIR) {
            player.sendMessage("§cAktivasyon item'ı elinde olmalı!");
            player.sendMessage("§7Yapı tipine göre farklı item'lar gerekiyor.");
            return;
        }
        
        // Hangi yapı tipi için bu item kullanılabilir?
        Structure.Type targetType = activationItemManager.getStructureTypeForItem(handItem);
        if (targetType == null) {
            player.sendMessage("§cBu item ile yapı aktifleştirilemez!");
            player.sendMessage("§7Farklı bir aktivasyon item'ı deneyin.");
            return;
        }
        
        // Tarif kontrolü (async - performanslı)
        player.sendMessage("§7Yapı kontrol ediliyor...");
        player.playSound(coreLoc, Sound.BLOCK_NOTE_BLOCK_HARP, 0.5f, 1.0f);
        
        recipeManager.validateStructureAsync(coreLoc, targetType, (isValid) -> {
            // Main thread'de devam et
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!isValid) {
                    // Hata mesajı
                    player.sendMessage("§c§l✗ Yapı tarife uymuyor!");
                    player.sendMessage("§7Lütfen yapıyı doğru şekilde kurun.");
                    player.sendMessage("§7Tarif: §e" + recipeManager.getRecipeInfo(targetType));
                    player.playSound(coreLoc, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    Location errorParticleLoc = coreLoc.clone().add(0.5, 1, 0.5);
                    coreLoc.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, 
                        errorParticleLoc, 20, 0.5, 0.5, 0.5, 0.1);
                    return;
                }
                
                // Aktivasyon başarılı
                activateStructure(player, coreLoc, targetType, handItem);
            });
        });
        
        setCooldown(player.getUniqueId());
    }
    
    /**
     * Yapıyı aktifleştir
     */
    private void activateStructure(Player player, Location coreLoc, Structure.Type type, ItemStack activationItem) {
        // Klan kontrolü (kişisel yapılar hariç)
        Clan clan = null;
        boolean isPersonalStructure = isPersonalStructure(type);
        
        if (!isPersonalStructure) {
            clan = clanManager.getClanByPlayer(player.getUniqueId());
            if (clan == null) {
                player.sendMessage("§cKlan yapıları için bir klana üye olmanız gerekiyor!");
                return;
            }
            
            // Klan bölgesinde mi?
            Clan owner = territoryManager.getTerritoryOwner(coreLoc);
            if (owner == null || !owner.equals(clan)) {
                player.sendMessage("§cBu yapıyı sadece kendi klan bölgenizde kurabilirsiniz!");
                return;
            }
        } else {
            // Kişisel yapılar için en yakın klanı bul (varsa)
            clan = territoryManager.getTerritoryOwner(coreLoc);
            if (clan == null) {
                clan = clanManager.getClanByPlayer(player.getUniqueId());
            }
        }
        
        // Yapı oluştur
        Structure structure = new Structure(type, coreLoc, 1);
        
        // Klan varsa ekle
        if (clan != null) {
            clan.addStructure(structure);
        }
        
        // Çekirdeği aktif yapıya dönüştür
        coreManager.activateCore(coreLoc, structure);
        
        // Aktivasyon item'ını tüket
        if (activationItem.getAmount() > 1) {
            activationItem.setAmount(activationItem.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
        
        // Başarı mesajı ve efektler
        String structureName = getStructureDisplayName(type);
        player.sendMessage("§a§l✓ " + structureName + " başarıyla aktif edildi!");
        player.sendMessage("§7Yapıya sağ tıklayarak menüyü açabilirsiniz.");
        
        // Ses efektleri ve partikül efektleri
        Location effectLoc = coreLoc.clone().add(0.5, 1, 0.5);
        player.playSound(effectLoc, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
        player.playSound(effectLoc, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.2f);
        player.playSound(effectLoc, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
        
        // Partikül efektleri
        coreLoc.getWorld().spawnParticle(Particle.END_ROD, effectLoc, 100, 1.5, 1.5, 1.5, 0.1);
        coreLoc.getWorld().spawnParticle(Particle.TOTEM, effectLoc, 50, 1.0, 1.0, 1.0, 0.05);
        coreLoc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, effectLoc, 30, 1.0, 1.0, 1.0, 0.1);
        coreLoc.getWorld().spawnParticle(Particle.FIREWORK, effectLoc, 20, 0.5, 0.5, 0.5, 0.1);
        
        // Başlık mesajı
        player.sendTitle("§a§l✓ BAŞARILI", "§7" + structureName + " aktif edildi!", 10, 40, 10);
    }
    
    /**
     * Kişisel yapı mı kontrol et
     */
    private boolean isPersonalStructure(Structure.Type type) {
        if (type == null) return false;
        
        return type == Structure.Type.PERSONAL_MISSION_GUILD ||
               type == Structure.Type.CONTRACT_OFFICE ||
               type == Structure.Type.MARKET_PLACE ||
               type == Structure.Type.RECIPE_LIBRARY;
    }
    
    /**
     * Yapı görünen adını al
     */
    private String getStructureDisplayName(Structure.Type type) {
        if (type == null) return "Yapı";
        
        switch (type) {
            case PERSONAL_MISSION_GUILD: return "Görev Loncası";
            case CLAN_BANK: return "Klan Bankası";
            case CONTRACT_OFFICE: return "Kontrat Bürosu";
            case CLAN_MISSION_GUILD: return "Klan Görev Loncası";
            case MARKET_PLACE: return "Market";
            case RECIPE_LIBRARY: return "Tarif Kütüphanesi";
            case ALCHEMY_TOWER: return "Simya Kulesi";
            case TECTONIC_STABILIZER: return "Tektonik Sabitleyici";
            case POISON_REACTOR: return "Zehir Reaktörü";
            case AUTO_TURRET: return "Otomatik Taret";
            case GLOBAL_MARKET_GATE: return "Global Pazar";
            default: return type.name();
        }
    }
    
    /**
     * Cooldown kontrolü
     */
    private boolean isOnCooldown(UUID playerId) {
        Long lastTime = activationCooldowns.get(playerId);
        if (lastTime == null) return false;
        
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastTime) < ACTIVATION_COOLDOWN;
    }
    
    /**
     * Cooldown ayarla
     */
    private void setCooldown(UUID playerId) {
        activationCooldowns.put(playerId, System.currentTimeMillis());
    }
}

