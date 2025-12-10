package me.mami.stratocraft.listener;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.manager.clan.ClanActivitySystem;
import me.mami.stratocraft.manager.clan.ClanBankSystem;
import me.mami.stratocraft.manager.clan.ClanMissionSystem;
import me.mami.stratocraft.manager.clan.ClanProtectionSystem;
import me.mami.stratocraft.model.Clan;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Klan Sistemleri Event Listener
 * 
 * Tüm yeni klan sistemlerinin event entegrasyonu
 */
public class ClanSystemListener implements Listener {
    private final Main plugin;
    private final ClanManager clanManager;
    private ClanProtectionSystem protectionSystem;
    private ClanActivitySystem activitySystem;
    private ClanBankSystem bankSystem;
    private ClanMissionSystem missionSystem;
    
    public ClanSystemListener(Main plugin, ClanManager clanManager) {
        this.plugin = plugin;
        this.clanManager = clanManager;
    }
    
    /**
     * Sistem setter'ları (setter injection)
     */
    public void setProtectionSystem(ClanProtectionSystem system) {
        this.protectionSystem = system;
    }
    
    public void setActivitySystem(ClanActivitySystem system) {
        this.activitySystem = system;
    }
    
    public void setBankSystem(ClanBankSystem system) {
        this.bankSystem = system;
    }
    
    public void setMissionSystem(ClanMissionSystem system) {
        this.missionSystem = system;
    }
    
    /**
     * Oyuncu girişi - Aktivite güncelle
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (activitySystem == null) return;
        
        Player player = event.getPlayer();
        if (player == null) return;
        
        try {
            activitySystem.updateActivity(player.getUniqueId());
        } catch (Exception e) {
            plugin.getLogger().warning("Aktivite güncelleme hatası: " + e.getMessage());
        }
    }
    
    /**
     * Oyuncu çıkışı - Aktivite güncelle (son aktivite zamanı)
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (activitySystem == null) return;
        
        Player player = event.getPlayer();
        if (player == null) return;
        
        try {
            // Son aktivite zamanı güncelle (çıkış zamanı)
            activitySystem.updateActivity(player.getUniqueId());
        } catch (Exception e) {
            plugin.getLogger().warning("Aktivite güncelleme hatası: " + e.getMessage());
        }
    }
    
    /**
     * Oyuncu saldırısı - Koruma sistemi kontrolü
     * Not: CombatListener'da da kontrol ediliyor, burada sadece ek kontroller yapılabilir
     * Priority: NORMAL (CombatListener'dan sonra)
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // CombatListener'da zaten kontrol ediliyor, burada sadece ek işlemler yapılabilir
        // Şimdilik boş bırakıldı (gelecekte ek özellikler eklenebilir)
    }
    
    /**
     * Blok yerleştirme - Görev ilerlemesi (yapı inşası)
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (missionSystem == null) return;
        
        Player player = event.getPlayer();
        if (player == null) return;
        
        Block block = event.getBlock();
        if (block == null) return;
        
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) return;
        
        // Yapı bloğu mu? (Basit kontrol - gelecekte StructureManager entegrasyonu)
        Material material = block.getType();
        if (isStructureBlock(material)) {
            try {
                missionSystem.updateMissionProgress(clan, player.getUniqueId(), 
                    ClanMissionSystem.MissionType.BUILD_STRUCTURE, 1);
            } catch (Exception e) {
                plugin.getLogger().warning("Görev ilerlemesi hatası: " + e.getMessage());
            }
        }
    }
    
    /**
     * Yapı bloğu mu? (Basit kontrol)
     */
    private boolean isStructureBlock(Material material) {
        if (material == null) return false;
        
        // Yapı blokları (basit kontrol - gelecekte StructureManager'dan alınabilir)
        switch (material) {
            case BEACON:
            case ENCHANTING_TABLE:
            case ANVIL:
            case SMITHING_TABLE:
            case CRAFTING_TABLE:
            case FURNACE:
            case BLAST_FURNACE:
            case SMOKER:
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Oyuncu etkileşimi - Banka ve görev tahtası
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Player player = event.getPlayer();
        if (player == null) return;
        
        Block clicked = event.getClickedBlock();
        if (clicked == null) return;
        
        // Banka sistemi entegrasyonu (gelecekte GUI açılabilir)
        if (bankSystem != null && clicked.getType() == Material.ENDER_CHEST) {
            // Metadata kontrolü (ClanBankSystem'den)
            if (clicked.hasMetadata("ClanBank")) {
                // Banka GUI açılabilir (gelecekte)
                // Şimdilik sadece metadata kontrolü
            }
        }
        
        // Görev tahtası entegrasyonu (gelecekte GUI açılabilir)
        if (missionSystem != null && clicked.getType() == Material.LECTERN) {
            // Metadata kontrolü (ClanMissionSystem'den)
            if (clicked.hasMetadata("ClanMissionBoard")) {
                // Görev GUI açılabilir (gelecekte)
                // Şimdilik sadece metadata kontrolü
            }
        }
    }
    
    /**
     * Ritüel kullanımı - Görev ilerlemesi
     * Not: RitualInteractionListener'da da kontrol edilebilir
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRitualUse(org.bukkit.event.block.BlockBreakEvent event) {
        // Bu event RitualInteractionListener'da işleniyor
        // Burada sadece görev ilerlemesi için entegrasyon yapılabilir
    }
}

