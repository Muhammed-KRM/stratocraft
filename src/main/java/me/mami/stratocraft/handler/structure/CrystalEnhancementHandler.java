package me.mami.stratocraft.handler.structure;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Structure;
import me.mami.stratocraft.manager.ItemManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Kristal Güçlendirme Yapısı Handler
 * Özel itemler atılarak kristal canını artırır
 */
public class CrystalEnhancementHandler implements Listener {
    private final Main plugin;
    private final Map<UUID, Long> lastProcessTime = new HashMap<>(); // Yapı UUID -> Son işleme zamanı
    private final Map<UUID, Integer> itemsProcessedThisMinute = new HashMap<>(); // Yapı UUID -> Bu dakikada işlenen item sayısı
    
    // Item -> HP artışı mapping
    private final Map<String, Double> itemHealthBoost = new HashMap<>();
    
    public CrystalEnhancementHandler(Main plugin) {
        this.plugin = plugin;
        initializeItemHealthBoosts();
    }
    
    private void initializeItemHealthBoosts() {
        // Özel itemler ve HP artışları
        itemHealthBoost.put("CRYSTAL_ENHANCEMENT_STONE", 25.0); // Temel taş
        itemHealthBoost.put("CRYSTAL_ENHANCEMENT_STONE_ADVANCED", 50.0); // Gelişmiş taş
        itemHealthBoost.put("CRYSTAL_ENHANCEMENT_STONE_ELITE", 100.0); // Elite taş
        itemHealthBoost.put("CRYSTAL_ENHANCEMENT_STONE_LEGENDARY", 200.0); // Efsanevi taş
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;
        
        Player player = event.getPlayer();
        Location blockLoc = clickedBlock.getLocation();
        
        // Yapı kontrolü
        Structure structure = findStructureAt(blockLoc);
        if (structure == null || structure.getType() != Structure.Type.CRYSTAL_ENHANCEMENT_STRUCTURE) {
            return;
        }
        
        // Klan kontrolü
        Clan clan = plugin.getTerritoryManager().getTerritoryOwner(blockLoc);
        if (clan == null) {
            player.sendMessage("§cBu yapı bir klana ait değil!");
            return;
        }
        
        // Oyuncu klan üyesi mi?
        if (clan.getRank(player.getUniqueId()) == null) {
            player.sendMessage("§cBu yapıyı kullanmak için klan üyesi olmalısınız!");
            return;
        }
        
        // Mesafe kontrolü (5 blok içinde)
        if (player.getLocation().distance(blockLoc) > 5) {
            player.sendMessage("§cYapıya çok uzaksınız! (5 blok içinde olmalısınız)");
            return;
        }
        
        // Item kontrolü
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage("§cElinde özel bir item tutmalısınız!");
            return;
        }
        
        // Item tipi kontrolü
        String itemId = getItemId(item);
        if (itemId == null || !itemHealthBoost.containsKey(itemId)) {
            player.sendMessage("§cBu item kristal güçlendirme için kullanılamaz!");
            return;
        }
        
        // İşleme hızı kontrolü
        if (!canProcessItem(structure, player)) {
            player.sendMessage("§cYapı şu anda çok fazla item işliyor! Lütfen bekleyin.");
            return;
        }
        
        // Item işleme
        processItem(structure, clan, item, itemId, player);
        
        event.setCancelled(true);
    }
    
    private Structure findStructureAt(Location loc) {
        if (plugin.getTerritoryManager() == null) return null;
        
        Clan clan = plugin.getTerritoryManager().getTerritoryOwner(loc);
        if (clan == null) return null;
        
        for (Structure structure : clan.getStructures()) {
            if (structure.getLocation().distance(loc) < 2.0) {
                return structure;
            }
        }
        
        return null;
    }
    
    private String getItemId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "custom_id");
        org.bukkit.persistence.PersistentDataContainer container = 
            item.getItemMeta().getPersistentDataContainer();
        
        if (container.has(key, org.bukkit.persistence.PersistentDataType.STRING)) {
            return container.get(key, org.bukkit.persistence.PersistentDataType.STRING);
        }
        
        return null;
    }
    
    private boolean canProcessItem(Structure structure, Player player) {
        UUID structureId = UUID.nameUUIDFromBytes(
            (structure.getLocation().getBlockX() + ";" + 
             structure.getLocation().getBlockY() + ";" + 
             structure.getLocation().getBlockZ()).getBytes()
        );
        long now = System.currentTimeMillis();
        
        // Son işleme zamanı kontrolü (minimum 1 saniye aralık)
        Long lastTime = lastProcessTime.get(structureId);
        if (lastTime != null && (now - lastTime) < 1000) {
            return false;
        }
        
        // Dakika başına maksimum item kontrolü
        int maxItemsPerMinute = getMaxItemsPerMinute(structure.getLevel());
        Integer processed = itemsProcessedThisMinute.get(structureId);
        
        if (processed != null && processed >= maxItemsPerMinute) {
            // 1 dakika geçti mi kontrol et
            if (lastTime != null && (now - lastTime) < 60000) {
                return false;
            } else {
                // 1 dakika geçti, sıfırla
                itemsProcessedThisMinute.put(structureId, 0);
            }
        }
        
        return true;
    }
    
    private int getMaxItemsPerMinute(int level) {
        switch (level) {
            case 1: return 10;
            case 2: return 20;
            case 3: return 30;
            default: return 10;
        }
    }
    
    private void processItem(Structure structure, Clan clan, ItemStack item, String itemId, Player player) {
        UUID structureId = UUID.nameUUIDFromBytes(
            (structure.getLocation().getBlockX() + ";" + 
             structure.getLocation().getBlockY() + ";" + 
             structure.getLocation().getBlockZ()).getBytes()
        );
        long now = System.currentTimeMillis();
        
        // HP artışı hesapla
        double baseHealthBoost = itemHealthBoost.get(itemId);
        double levelMultiplier = getLevelMultiplier(structure.getLevel());
        double finalHealthBoost = baseHealthBoost * levelMultiplier;
        
        // Kristal canını artır
        clan.increaseCrystalMaxHealth(finalHealthBoost);
        
        // Item tüket
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
        
        // İşleme kayıtları
        lastProcessTime.put(structureId, now);
        itemsProcessedThisMinute.put(structureId, 
            itemsProcessedThisMinute.getOrDefault(structureId, 0) + 1);
        
        // Efektler (performans için partikül sayısı azaltıldı)
        Location structureLoc = structure.getLocation();
        structureLoc.getWorld().spawnParticle(
            org.bukkit.Particle.TOTEM, 
            structureLoc.add(0.5, 1, 0.5), 
            15, // 30 -> 15 (performans optimizasyonu)
            0.5, 0.5, 0.5, 0.1
        );
        
        // Mesajlar
        player.sendMessage("§aKristal güçlendirildi! (+" + 
            String.format("%.1f", finalHealthBoost) + " HP)");
        
        // Klan üyelerine bildir
        for (UUID memberId : clan.getMembers().keySet()) {
            Player member = org.bukkit.Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline() && !member.equals(player)) {
                member.sendMessage("§7" + player.getName() + " kristali güçlendirdi! (+" + 
                    String.format("%.1f", finalHealthBoost) + " HP)");
            }
        }
        
        // İşleme animasyonu (1 saniye sonra)
        new BukkitRunnable() {
            @Override
            public void run() {
                // İşleme tamamlandı efekti
                structureLoc.getWorld().spawnParticle(
                    org.bukkit.Particle.ENCHANTMENT_TABLE,
                    structureLoc.add(0.5, 1, 0.5),
                    20,
                    0.3, 0.3, 0.3, 0.1
                );
            }
        }.runTaskLater(plugin, 20L); // 1 saniye sonra
    }
    
    private double getLevelMultiplier(int level) {
        switch (level) {
            case 1: return 1.0;
            case 2: return 2.0;
            case 3: return 4.0;
            default: return 1.0;
        }
    }
}

