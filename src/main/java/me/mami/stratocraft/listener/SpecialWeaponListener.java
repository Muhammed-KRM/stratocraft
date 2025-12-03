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
        
        // Seviye 1-2: Özel yetenekler (saldırıda pasif olarak çalışır, sağ tık ile aktif edilebilir)
        if (weaponLevel == 1 || weaponLevel == 2) {
            handleLevel1And2Abilities(player, weaponId, weaponLevel, event);
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
            
            // Normal Sağ Tık: Mod'a göre farklı işlemler
            String mode = weaponMode.getOrDefault(player.getUniqueId(), getAvailableModesForWeapon(weaponId)[0]);
            
            if (mode.equals("throw") && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                event.setCancelled(true);
                throwWeapon(player, item);
                return;
            } else if (mode.equals("dash_explosion") && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                event.setCancelled(true);
                dashExplosion(player);
                return;
            }
            // wall_build modu onPlayerInteractWallBuild'de işleniyor
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
        
        String[] availableModes = getAvailableModesForWeapon(weaponId);
        String mode = weaponMode.getOrDefault(player.getUniqueId(), availableModes[0]);
        
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
     * Mod Seçim Ekranı Göster (GTA 5 tarzı) - Silah ID'sine göre özelleştirilmiş
     */
    private void showModeSelectionMenu(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null) return;
        
        String weaponId = getSpecialWeaponId(item);
        if (weaponId == null || !weaponId.startsWith("WEAPON_L5")) return;
        
        String currentMode = weaponMode.getOrDefault(player.getUniqueId(), "block_throw");
        
        // Silah ID'sine göre mod listesi al
        String[] availableModes = getAvailableModesForWeapon(weaponId);
        String[] modeNames = getModeNamesForWeapon(weaponId);
        String[] modeDescriptions = getModeDescriptionsForWeapon(weaponId);
        
        // ActionBar ile mod seçim menüsü göster
        updateModeSelectionActionBar(player, currentMode, availableModes, modeNames);
        
        // Chat mesajları - GTA 5 tarzı menü
        player.sendMessage("§6§l═══════════════════════════════════");
        player.sendMessage("§6§l         MOD SEÇİM MENÜSÜ");
        player.sendMessage("§6§l═══════════════════════════════════");
        player.sendMessage("");
        // Mevcut modun index'ini bul
        int currentIndex = 0;
        for (int i = 0; i < availableModes.length; i++) {
            if (availableModes[i].equals(currentMode)) {
                currentIndex = i;
                break;
            }
        }
        player.sendMessage("§eMevcut Mod: §f" + modeNames[currentIndex]);
        player.sendMessage("");
        player.sendMessage("§7§lKullanılabilir Modlar:");
        player.sendMessage("");
        
        // Her mod için seçenek göster
        for (int i = 0; i < availableModes.length; i++) {
            String mode = availableModes[i];
            String modeName = modeNames[i];
            String modeDesc = modeDescriptions[i];
            
            if (currentMode.equals(mode)) {
                player.sendMessage("§e§l▶ §e" + (i + 1) + ". " + modeName);
                player.sendMessage("§7    " + modeDesc);
            } else {
                player.sendMessage("§7   " + (i + 1) + ". " + modeName);
                player.sendMessage("§7    " + modeDesc);
            }
            player.sendMessage("");
        }
        
        player.sendMessage("§7Mod değiştirmek için: §e/weaponmode <1|2|3>");
        player.sendMessage("§7veya tekrar §eShift+Sağ Tık §7yapın");
        player.sendMessage("§6§l═══════════════════════════════════");
        
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
                    String mode = weaponMode.getOrDefault(player.getUniqueId(), availableModes[0]);
                    updateModeSelectionActionBar(player, mode, availableModes, modeNames);
                }
                
                count++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Silah ID'sine göre kullanılabilir modları döndür
     */
    private String[] getAvailableModesForWeapon(String weaponId) {
        switch (weaponId) {
            case "WEAPON_L5_1": // Kılıç
                return new String[]{"block_throw", "wall_build", "throw"};
            case "WEAPON_L5_2": // Balta
                return new String[]{"block_throw", "dash_explosion", "throw"};
            case "WEAPON_L5_3": // Mızrak
                return new String[]{"wall_build", "dash_explosion", "throw"};
            case "WEAPON_L5_4": // Yay
                return new String[]{"block_throw", "dash_explosion", "wall_build"};
            case "WEAPON_L5_5": // Çekiç
                return new String[]{"throw", "dash_explosion", "block_throw"};
            default:
                return new String[]{"block_throw", "wall_build", "throw"};
        }
    }
    
    /**
     * Silah ID'sine göre mod isimlerini döndür
     */
    private String[] getModeNamesForWeapon(String weaponId) {
        switch (weaponId) {
            case "WEAPON_L5_1": // Kılıç
                return new String[]{"§eBlok Fırlatma", "§aDuvar Yapma", "§dAtılma/Patlama"};
            case "WEAPON_L5_2": // Balta
                return new String[]{"§eBlok Fırlatma", "§cDash/Patlama", "§dAtılma/Patlama"};
            case "WEAPON_L5_3": // Mızrak
                return new String[]{"§aDuvar Yapma", "§cDash/Patlama", "§dAtılma/Patlama"};
            case "WEAPON_L5_4": // Yay
                return new String[]{"§eBlok Fırlatma", "§cDash/Patlama", "§aDuvar Yapma"};
            case "WEAPON_L5_5": // Çekiç
                return new String[]{"§dAtılma/Patlama", "§cDash/Patlama", "§eBlok Fırlatma"};
            default:
                return new String[]{"§eBlok Fırlatma", "§aDuvar Yapma", "§dAtılma/Patlama"};
        }
    }
    
    /**
     * Silah ID'sine göre mod açıklamalarını döndür
     */
    private String[] getModeDescriptionsForWeapon(String weaponId) {
        switch (weaponId) {
            case "WEAPON_L5_1": // Kılıç
                return new String[]{
                    "§7Q tuşu ile bakılan bloğu fırlat",
                    "§7Sağ tık ile 3x3 duvar yap",
                    "§7Sağ tık ile silahı at (patlama)"
                };
            case "WEAPON_L5_2": // Balta
                return new String[]{
                    "§7Q tuşu ile bakılan bloğu fırlat",
                    "§7Sağ tık ile ileriye dash yap ve patlat",
                    "§7Sağ tık ile silahı at (patlama)"
                };
            case "WEAPON_L5_3": // Mızrak
                return new String[]{
                    "§7Sağ tık ile 3x3 duvar yap",
                    "§7Sağ tık ile ileriye dash yap ve patlat",
                    "§7Sağ tık ile silahı at (patlama)"
                };
            case "WEAPON_L5_4": // Yay
                return new String[]{
                    "§7Q tuşu ile bakılan bloğu fırlat",
                    "§7Sağ tık ile ileriye dash yap ve patlat",
                    "§7Sağ tık ile 3x3 duvar yap"
                };
            case "WEAPON_L5_5": // Çekiç
                return new String[]{
                    "§7Sağ tık ile silahı at (patlama)",
                    "§7Sağ tık ile ileriye dash yap ve patlat",
                    "§7Q tuşu ile bakılan bloğu fırlat"
                };
            default:
                return new String[]{
                    "§7Q tuşu ile bakılan bloğu fırlat",
                    "§7Sağ tık ile 3x3 duvar yap",
                    "§7Sağ tık ile silahı at (patlama)"
                };
        }
    }
    
    /**
     * ActionBar'da mod seçim menüsünü göster
     */
    private void updateModeSelectionActionBar(Player player, String currentMode, String[] availableModes, String[] modeNames) {
        // Mevcut modun index'ini bul
        int currentIndex = 0;
        for (int i = 0; i < availableModes.length; i++) {
            if (availableModes[i].equals(currentMode)) {
                currentIndex = i;
                break;
            }
        }
        
        String modeName = modeNames[currentIndex];
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
            case "dash_explosion": return "§cDash/Patlama";
            default: return "§7Bilinmeyen";
        }
    }
    
    /**
     * Mod değiştir (komut ile) - Silah ID'sine göre özelleştirilmiş
     */
    public void changeWeaponMode(Player player, int modeNumber) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null) {
            player.sendMessage("§cElinde silah yok!");
            return;
        }
        
        int weaponLevel = getWeaponLevel(item);
        if (weaponLevel != 5) {
            player.sendMessage("§cBu özellik sadece Seviye 5 silahlarda çalışır!");
            return;
        }
        
        String weaponId = getSpecialWeaponId(item);
        if (weaponId == null || !weaponId.startsWith("WEAPON_L5")) {
            player.sendMessage("§cBu özel silah değil!");
            return;
        }
        
        // Silah ID'sine göre mod listesi al
        String[] availableModes = getAvailableModesForWeapon(weaponId);
        String[] modeNames = getModeNamesForWeapon(weaponId);
        String[] modeDescriptions = getModeDescriptionsForWeapon(weaponId);
        
        if (modeNumber < 1 || modeNumber > availableModes.length) {
            player.sendMessage("§cGeçersiz mod! 1-" + availableModes.length + " arası seçin.");
            return;
        }
        
        String newMode = availableModes[modeNumber - 1];
        String displayName = modeNames[modeNumber - 1];
        String description = modeDescriptions[modeNumber - 1];
        
        weaponMode.put(player.getUniqueId(), newMode);
        player.sendMessage("§aMod değiştirildi: " + displayName);
        player.sendMessage(description);
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
        
        String mode = weaponMode.getOrDefault(player.getUniqueId(), getAvailableModesForWeapon(weaponId)[0]);
        
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
    
    /**
     * Seviye 5: Dash/Patlama modu
     */
    private void dashExplosion(Player player) {
        Vector direction = player.getLocation().getDirection();
        Location startLoc = player.getLocation();
        
        // Dash efekti
        player.setVelocity(direction.multiply(2.0));
        player.getWorld().spawnParticle(Particle.CLOUD, startLoc, 20, 0.5, 0.5, 0.5, 0.1);
        player.getWorld().playSound(startLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.5f);
        
        // 1 saniye sonra patlama
        new BukkitRunnable() {
            @Override
            public void run() {
                Location explosionLoc = player.getLocation();
                player.getWorld().createExplosion(explosionLoc, 4.0f, false, false);
                player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, explosionLoc, 1);
                player.getWorld().playSound(explosionLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
                
                // Yakındaki canlılara hasar ver
                for (Entity entity : player.getWorld().getNearbyEntities(explosionLoc, 5, 5, 5)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        ((LivingEntity) entity).damage(15.0);
                    }
                }
            }
        }.runTaskLater(plugin, 20L); // 1 saniye
        
        player.sendMessage("§cDash/Patlama aktif! 1 saniye sonra patlayacak!");
    }
    
    /**
     * Seviye 1-2: Özel yetenekleri işle
     */
    private void handleLevel1And2Abilities(Player player, String weaponId, int weaponLevel, PlayerInteractEvent event) {
        // Seviye 1 yetenekler
        if (weaponLevel == 1) {
            switch (weaponId) {
                case "WEAPON_L1_1": // Hız Kılıcı - Sağ tık ile hız artışı
                    if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        event.setCancelled(true);
                        player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                            org.bukkit.potion.PotionEffectType.SPEED, 60, 0, false, false, true));
                        player.sendMessage("§eHız artışı aktif! (3 saniye)");
                    }
                    break;
                case "WEAPON_L1_2": // Kritik Baltası - Saldırıda otomatik çalışır (EntityDamageByEntityEvent'te)
                    // Pasif yetenek, saldırıda işlenecek
                    break;
                case "WEAPON_L1_3": // Savunma Mızrağı - Sağ tık ile savunma bonusu
                    if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        event.setCancelled(true);
                        player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                            org.bukkit.potion.PotionEffectType.DAMAGE_RESISTANCE, 60, 0, false, false, true));
                        player.sendMessage("§bSavunma bonusu aktif! (3 saniye)");
                    }
                    break;
                case "WEAPON_L1_4": // Hızlı Yay - Pasif yetenek (ok atışında işlenecek)
                    // Pasif yetenek, ok atışında işlenecek
                    break;
                case "WEAPON_L1_5": // Güç Çekici - Sağ tık ile güç artışı
                    if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        event.setCancelled(true);
                        player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                            org.bukkit.potion.PotionEffectType.INCREASE_DAMAGE, 60, 0, false, false, true));
                        player.sendMessage("§cGüç artışı aktif! (3 saniye)");
                    }
                    break;
            }
        }
        
        // Seviye 2 yetenekler
        if (weaponLevel == 2) {
            switch (weaponId) {
                case "WEAPON_L2_1": // Zehir Kılıcı - Saldırıda otomatik çalışır
                    // Pasif yetenek, saldırıda işlenecek
                    break;
                case "WEAPON_L2_2": // Yavaşlatma Baltası - Saldırıda otomatik çalışır
                    // Pasif yetenek, saldırıda işlenecek
                    break;
                case "WEAPON_L2_3": // Ateş Mızrağı - Saldırıda otomatik çalışır
                    // Pasif yetenek, saldırıda işlenecek
                    break;
                case "WEAPON_L2_4": // Buz Yayı - Ok atışında işlenecek
                    // Pasif yetenek, ok atışında işlenecek
                    break;
                case "WEAPON_L2_5": // Şok Çekici - Saldırıda otomatik çalışır
                    // Pasif yetenek, saldırıda işlenecek
                    break;
            }
        }
    }
    
    /**
     * Seviye 1-2: Saldırıda pasif yetenekleri işle
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(org.bukkit.event.entity.EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        
        Player player = (Player) event.getDamager();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null) return;
        
        int weaponLevel = getWeaponLevel(item);
        String weaponId = getSpecialWeaponId(item);
        
        if (weaponId == null || !weaponId.startsWith("WEAPON_L")) return;
        if (weaponLevel != 1 && weaponLevel != 2) return;
        
        Entity target = event.getEntity();
        if (!(target instanceof LivingEntity)) return;
        
        LivingEntity livingTarget = (LivingEntity) target;
        
        // Seviye 1 yetenekler
        if (weaponLevel == 1) {
            switch (weaponId) {
                case "WEAPON_L1_2": // Kritik Baltası - %15 şansla 2x hasar
                    if (Math.random() < 0.15) {
                        double currentDamage = event.getDamage();
                        event.setDamage(currentDamage * 2.0);
                        player.sendMessage("§e§lKRİTİK VURUŞ!");
                        player.getWorld().spawnParticle(Particle.CRIT, target.getLocation(), 10, 0.5, 0.5, 0.5, 0.1);
                        player.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.0f);
                    }
                    break;
            }
        }
        
        // Seviye 2 yetenekler
        if (weaponLevel == 2) {
            switch (weaponId) {
                case "WEAPON_L2_1": // Zehir Kılıcı - 3 saniye zehir
                    livingTarget.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.POISON, 60, 0, false, false, true));
                    player.sendMessage("§2Zehir etkisi uygulandı!");
                    break;
                case "WEAPON_L2_2": // Yavaşlatma Baltası - 3 saniye yavaşlatma
                    livingTarget.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.SLOW, 60, 0, false, false, true));
                    player.sendMessage("§bYavaşlatma etkisi uygulandı!");
                    break;
                case "WEAPON_L2_3": // Ateş Mızrağı - 5 saniye ateş
                    livingTarget.setFireTicks(100); // 5 saniye
                    player.sendMessage("§cAteş etkisi uygulandı!");
                    break;
                case "WEAPON_L2_5": // Şok Çekici - Yakındaki düşmanlara hasar
                    for (Entity nearby : player.getWorld().getNearbyEntities(target.getLocation(), 3, 3, 3)) {
                        if (nearby instanceof LivingEntity && nearby != target && nearby != player) {
                            ((LivingEntity) nearby).damage(3.0);
                            player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, nearby.getLocation(), 5, 0.3, 0.3, 0.3, 0.05);
                        }
                    }
                    player.sendMessage("§eŞok etkisi uygulandı!");
                    break;
            }
        }
    }
}

