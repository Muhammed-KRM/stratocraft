package me.mami.stratocraft.listener;

import me.mami.stratocraft.Main;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Özel silah yeteneklerini yöneten listener
 */
public class SpecialWeaponListener implements Listener {
    private final Main plugin;
    private final Map<UUID, String> weaponMode = new HashMap<>(); // Seviye 5 silahlar için mod
    private final Map<UUID, Long> lastLaserTime = new HashMap<>(); // Seviye 4 lazer cooldown
    private final Map<UUID, Long> lastModeChangeTime = new HashMap<>(); // Mod değiştirme cooldown
    private final Map<UUID, Boolean> modeSelectionActive = new HashMap<>(); // Mod seçim ekranı aktif mi
    
    public SpecialWeaponListener(Main plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Silah seviyesini kontrol et
     */
    private int getWeaponLevel(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        Integer level = item.getItemMeta().getPersistentDataContainer()
            .get(new org.bukkit.NamespacedKey(Main.getInstance(), "weapon_level"), PersistentDataType.INTEGER);
        return level != null ? level : 0;
    }
    
    /**
     * Özel silah ID'sini kontrol et
     */
    private String getSpecialWeaponId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer()
            .get(new org.bukkit.NamespacedKey(Main.getInstance(), "special_weapon_id"), PersistentDataType.STRING);
    }
    
    /**
     * Seviye 3: Patlama Atabilme - Sağ tık ile 20 blok menzile patlama at
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null) return;
        
        int weaponLevel = getWeaponLevel(item);
        String weaponId = getSpecialWeaponId(item);
        
        // Özel silah kontrolü - sadece özel silahlar için çalışsın
        if (weaponId == null || !weaponId.startsWith("WEAPON_L")) return;
        
        // Seviye 5 silahlar için özel kontrol - Shift+Sağ Tık her zaman mod menüsü açsın
        if (weaponLevel == 5 && player.isSneaking() && 
            (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            event.setCancelled(true);
            showModeSelectionMenu(player);
            return; // Diğer kontrolleri atla
        }
        
        // Seviye 3: Patlama Atabilme
        if (weaponLevel == 3 && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            event.setCancelled(true);
            
            // Ray trace ile hedef bul
            RayTraceResult result = player.rayTraceBlocks(20);
            Location targetLoc;
            
            if (result != null && result.getHitBlock() != null) {
                targetLoc = result.getHitBlock().getLocation();
            } else {
                // 20 blok ileriye patlama
                Vector direction = player.getLocation().getDirection().multiply(20);
                targetLoc = player.getLocation().add(direction);
            }
            
            // Patlama oluştur
            player.getWorld().createExplosion(targetLoc, 3.0f, false, false);
            player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, targetLoc, 1);
            player.getWorld().playSound(targetLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
            
            // Yakındaki canlılara hasar ver
            for (Entity entity : player.getWorld().getNearbyEntities(targetLoc, 5, 5, 5)) {
                if (entity instanceof LivingEntity && entity != player) {
                    ((LivingEntity) entity).damage(10.0);
                }
            }
            
            player.sendMessage("§cPatlama atıldı!");
        }
        
        // Seviye 4: Devamlı Lazer
        if (weaponLevel == 4 && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            event.setCancelled(true);
            
            long currentTime = System.currentTimeMillis();
            Long lastLaser = lastLaserTime.get(player.getUniqueId());
            
            // Cooldown kontrolü (0.5 saniye)
            if (lastLaser != null && currentTime - lastLaser < 500) {
                return;
            }
            
            lastLaserTime.put(player.getUniqueId(), currentTime);
            
            // Lazer at
            fireLaser(player);
        }
        
        // Seviye 5: Çok Modlu Silah
        if (weaponLevel == 5) {
            // Shift+Sağ Tık: Mod Seçim Ekranı
            if (player.isSneaking() && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                event.setCancelled(true);
                showModeSelectionMenu(player);
                return;
            }
            
            // Normal Sağ Tık: Atılma/Patlama (sadece throw modunda)
            String mode = weaponMode.getOrDefault(player.getUniqueId(), "block_throw");
            if (mode.equals("throw") && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                event.setCancelled(true);
                throwWeapon(player, item);
                return;
            }
        }
    }
    
    /**
     * Seviye 4: Lazer at
     */
    private void fireLaser(Player player) {
        Vector direction = player.getLocation().getDirection();
        Location startLoc = player.getEyeLocation();
        
        // Lazer partikülü ve hasar
        new BukkitRunnable() {
            double distance = 0;
            Location currentLoc = startLoc.clone();
            
            @Override
            public void run() {
                if (distance > 30 || currentLoc.getBlock().getType().isSolid()) {
                    cancel();
                    return;
                }
                
                // Partikül
                player.getWorld().spawnParticle(Particle.END_ROD, currentLoc, 3, 0.1, 0.1, 0.1, 0.05);
                
                // Hasar ver (sadece düşmanlar)
                for (Entity entity : player.getWorld().getNearbyEntities(currentLoc, 1, 1, 1)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        // Oyuncu kontrolü - aynı klan değilse hasar ver
                        if (entity instanceof Player) {
                            // Klan kontrolü burada yapılabilir, şimdilik tüm oyunculara hasar ver
                            ((LivingEntity) entity).damage(5.0);
                        } else {
                            // Mob'lara hasar ver
                            ((LivingEntity) entity).damage(5.0);
                        }
                    }
                }
                
                distance += 0.5;
                currentLoc.add(direction.clone().multiply(0.5));
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        player.sendMessage("§6Lazer atıldı!");
    }
    
    /**
     * Seviye 5: Silahı at (patlama yapar)
     */
    private void throwWeapon(Player player, ItemStack weapon) {
        // Silahı fırlat
        Item droppedItem = player.getWorld().dropItem(player.getLocation(), weapon.clone());
        droppedItem.setVelocity(player.getLocation().getDirection().multiply(1.5));
        droppedItem.setPickupDelay(100);
        
        // 2 saniye sonra patlama
        new BukkitRunnable() {
            @Override
            public void run() {
                if (droppedItem.isValid()) {
                    Location loc = droppedItem.getLocation();
                    player.getWorld().createExplosion(loc, 5.0f, false, false);
                    droppedItem.remove();
                }
            }
        }.runTaskLater(plugin, 40L); // 2 saniye
        
        player.getInventory().setItemInMainHand(null);
        player.sendMessage("§dSilah atıldı! 2 saniye sonra patlayacak!");
    }
    
    /**
     * Seviye 5: Q tuşu - Blok Fırlatma (sadece block_throw modunda)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDropItem(org.bukkit.event.player.PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null) return;
        
        int weaponLevel = getWeaponLevel(item);
        if (weaponLevel != 5) return;
        
        // Sadece özel silah kontrolü
        String weaponId = getSpecialWeaponId(item);
        if (weaponId == null || !weaponId.startsWith("WEAPON_L5")) return;
        
        String mode = weaponMode.getOrDefault(player.getUniqueId(), "block_throw");
        
        // Sadece blok fırlatma modunda Q tuşu çalışsın
        if (mode.equals("block_throw")) {
            event.setCancelled(true);
            
            // Bakılan bloğu al ve fırlat
            RayTraceResult result = player.rayTraceBlocks(10);
            if (result != null && result.getHitBlock() != null) {
                Block block = result.getHitBlock();
                Material blockType = block.getType();
                
                if (blockType != Material.BEDROCK && blockType != Material.AIR && !blockType.name().contains("COMMAND")) {
                    block.setType(Material.AIR);
                    
                    // Blok item'ı fırlat
                    Item droppedBlock = player.getWorld().dropItem(block.getLocation(), new ItemStack(blockType));
                    droppedBlock.setVelocity(player.getLocation().getDirection().multiply(2.0));
                    
                    player.sendMessage("§eBlok fırlatıldı!");
                }
            }
        }
        // Diğer modlarda normal item drop çalışsın
    }
    
    /**
     * Mod Seçim Ekranı Göster (GTA 5 tarzı)
     */
    private void showModeSelectionMenu(Player player) {
        String currentMode = weaponMode.getOrDefault(player.getUniqueId(), "block_throw");
        
        // ActionBar ile mod seçim menüsü göster (sürekli güncellenir)
        updateModeSelectionActionBar(player, currentMode);
        
        // Chat mesajları
        player.sendMessage("§6§l═══════════════════════════");
        player.sendMessage("§6§l        MOD SEÇİMİ");
        player.sendMessage("§6§l═══════════════════════════");
        player.sendMessage("");
        player.sendMessage("§eMevcut Mod: §f" + getModeDisplayName(currentMode));
        player.sendMessage("");
        player.sendMessage("§7§lKullanılabilir Modlar:");
        player.sendMessage("");
        
        // Mod seçeneklerini göster (mevcut modu vurgula)
        if (currentMode.equals("block_throw")) {
            player.sendMessage("§e§l▶ §e1. Blok Fırlatma §7(Q tuşu ile kullan)");
        } else {
            player.sendMessage("§7  1. Blok Fırlatma §7(Q tuşu ile kullan)");
        }
        
        if (currentMode.equals("wall_build")) {
            player.sendMessage("§a§l▶ §a2. Duvar Yapma §7(Sağ tık ile kullan)");
        } else {
            player.sendMessage("§7  2. Duvar Yapma §7(Sağ tık ile kullan)");
        }
        
        if (currentMode.equals("throw")) {
            player.sendMessage("§d§l▶ §d3. Atılma/Patlama §7(Sağ tık ile kullan)");
        } else {
            player.sendMessage("§7  3. Atılma/Patlama §7(Sağ tık ile kullan)");
        }
        
        player.sendMessage("");
        player.sendMessage("§7Mod değiştirmek için: §e/weaponmode <1|2|3>");
        player.sendMessage("§7veya tekrar §eShift+Sağ Tık §7yapın");
        player.sendMessage("§6§l═══════════════════════════");
        
        // ActionBar'ı 10 saniye boyunca güncelle
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (!player.isOnline() || count >= 200) { // 10 saniye (200 tick)
                    player.sendActionBar("");
                    cancel();
                    return;
                }
                
                // Her 20 tick'te bir (1 saniye) güncelle
                if (count % 20 == 0) {
                    String mode = weaponMode.getOrDefault(player.getUniqueId(), "block_throw");
                    updateModeSelectionActionBar(player, mode);
                }
                
                count++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * ActionBar'da mod seçim menüsünü göster
     */
    private void updateModeSelectionActionBar(Player player, String currentMode) {
        String modeName = getModeDisplayName(currentMode);
        player.sendActionBar("§6§l[MOD SEÇİMİ] §7Mevcut: " + modeName + " §7| §e/weaponmode <1|2|3>");
    }
    
    /**
     * Mod ismini göster
     */
    private String getModeDisplayName(String mode) {
        switch (mode) {
            case "block_throw": return "§eBlok Fırlatma";
            case "wall_build": return "§aDuvar Yapma";
            case "throw": return "§dAtılma/Patlama";
            default: return "§7Bilinmeyen";
        }
    }
    
    /**
     * Mod değiştir (komut ile)
     */
    public void changeWeaponMode(Player player, int modeNumber) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null) return;
        
        int weaponLevel = getWeaponLevel(item);
        if (weaponLevel != 5) {
            player.sendMessage("§cBu özellik sadece Seviye 5 silahlarda çalışır!");
            return;
        }
        
        String newMode;
        String displayName;
        
        switch (modeNumber) {
            case 1:
                newMode = "block_throw";
                displayName = "§eBlok Fırlatma";
                break;
            case 2:
                newMode = "wall_build";
                displayName = "§aDuvar Yapma";
                break;
            case 3:
                newMode = "throw";
                displayName = "§dAtılma/Patlama";
                break;
            default:
                player.sendMessage("§cGeçersiz mod! 1, 2 veya 3 seçin.");
                return;
        }
        
        weaponMode.put(player.getUniqueId(), newMode);
        player.sendMessage("§aMod değiştirildi: " + displayName);
        player.sendMessage("§7Kullanım:");
        if (newMode.equals("block_throw")) {
            player.sendMessage("§7  Q tuşu: Blok fırlat");
        } else if (newMode.equals("wall_build")) {
            player.sendMessage("§7  Sağ tık: Duvar yap");
        } else if (newMode.equals("throw")) {
            player.sendMessage("§7  Sağ tık: Silahı at (patlama)");
        }
    }
    
    /**
     * Seviye 5: Duvar yapma modunda sağ tık ile duvar yap
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractWallBuild(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null) return;
        
        int weaponLevel = getWeaponLevel(item);
        if (weaponLevel != 5) return;
        
        String weaponId = getSpecialWeaponId(item);
        if (weaponId == null || !weaponId.startsWith("WEAPON_L5")) return;
        
        String mode = weaponMode.getOrDefault(player.getUniqueId(), "block_throw");
        
        // Duvar yapma modu
        if (mode.equals("wall_build") && event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            event.setCancelled(true);
            
            // Duvar yap (3x3 alan)
            Block center = event.getClickedBlock();
            Location centerLoc = center.getLocation();
            
            int blocksPlaced = 0;
            for (int x = -1; x <= 1; x++) {
                for (int y = 0; y <= 2; y++) {
                    for (int z = -1; z <= 1; z++) {
                        Block targetBlock = centerLoc.clone().add(x, y, z).getBlock();
                        if (targetBlock.getType() == Material.AIR) {
                            targetBlock.setType(Material.OBSIDIAN);
                            blocksPlaced++;
                        }
                    }
                }
            }
            
            if (blocksPlaced > 0) {
                player.sendMessage("§aDuvar oluşturuldu! (" + blocksPlaced + " blok)");
            }
        }
    }
}

