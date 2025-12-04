package me.mami.stratocraft.listener;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.WeaponModeManager;
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
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.attribute.Attribute;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Özel silah yeteneklerini yöneten listener
 */
public class SpecialWeaponListener implements Listener {
    private final Main plugin;
    private final NamespacedKey itemKey;
    private final Map<UUID, String> weaponMode = new HashMap<>(); // Seviye 5 silahlar için mod
    private final Map<UUID, Long> lastLaserTime = new HashMap<>(); // Seviye 4 lazer cooldown
    private final Map<UUID, Long> lastModeChangeTime = new HashMap<>(); // Mod değiştirme cooldown
    private final Map<UUID, Boolean> modeSelectionActive = new HashMap<>(); // Mod seçim ekranı aktif mi
    private final Map<UUID, Long> cooldowns = new HashMap<>(); // Cooldown sistemi
    private final Map<UUID, java.util.List<EntityType>> killedMobs = new HashMap<>(); // L5_4 için öldürülen moblar
    private final Map<UUID, Location> lastLocation = new HashMap<>(); // L5_5 Geri Sar için konum
    private final Map<UUID, Double> lastHealth = new HashMap<>(); // L5_5 Geri Sar için can
    
    public SpecialWeaponListener(Main plugin) {
        this.plugin = plugin;
        this.itemKey = new NamespacedKey(plugin, "special_item_id");
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
     * Özel item ID'sini kontrol et (yeni sistem)
     */
    private String getSpecialItemId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer()
            .get(new org.bukkit.NamespacedKey(Main.getInstance(), "special_item_id"), PersistentDataType.STRING);
    }
    
    /**
     * Seviye 3: Patlama Atabilme - Sağ tık ile 20 blok menzile patlama at
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        // Önce event.getItem() kontrol et, yoksa elindeki item'ı al
        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR) {
            item = player.getInventory().getItemInMainHand();
        }
        if (item == null || item.getType() == Material.AIR) return;
        
        // YENİ SİSTEM: special_item_id kontrolü (önce yeni sistemi kontrol et)
        String specialItemId = getSpecialItemId(item);
        if (specialItemId != null) {
            // Tier 1-3 silahlar için yetenekleri işle
            if (specialItemId.startsWith("l1_") || specialItemId.startsWith("l2_") || specialItemId.startsWith("l3_")) {
                handleTier1To3Interact(player, specialItemId, event);
                return;
            }
            
            // Tier 4-5 silahlar için
            if (specialItemId.startsWith("l4_") || specialItemId.startsWith("l5_")) {
                // Shift basılıyken menü açılır (WeaponModeManager hallediyor), burada işleme
                if (player.isSneaking() && event.getAction().toString().contains("RIGHT")) return;
                
                if (!item.getItemMeta().getPersistentDataContainer().has(itemKey, PersistentDataType.STRING)) return;
                
                int mode = WeaponModeManager.getWeaponMode(item);
                
                if (event.getAction().toString().contains("RIGHT") || event.getAction() == Action.PHYSICAL) {
                    handleNewSystemWeapons(player, specialItemId, event);
                }
                return; // Yeni sistem silahları için eski sisteme geçme
            }
        }
        
        // ESKİ SİSTEM: WEAPON_L formatı
        int weaponLevel = getWeaponLevel(item);
        String weaponId = getSpecialWeaponId(item);
        
        // Özel silah kontrolü - sadece özel silahlar için çalışsın
        if (weaponId == null || !weaponId.startsWith("WEAPON_L")) return;
        
        // Shift+Sağ Tık kontrolü WeaponModeManager tarafından yapılıyor (GUI sistemi)
        // Burada sadece normal sağ tık yeteneklerini işle
        if (player.isSneaking() && 
            (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            // WeaponModeManager bu işlemi hallediyor, burada sadece return et
            return;
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
            // WeaponModeManager'dan modu al (integer)
            int modeInt = WeaponModeManager.getWeaponMode(item);
            String mode = getModeStringFromInt(weaponId, modeInt);
            
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
        // WeaponModeManager'dan modu al
        int modeInt = WeaponModeManager.getWeaponMode(item, plugin);
        String mode = getModeStringFromInt(weaponId, modeInt);
        
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
     * Integer mod numarasını string mod ismine çevir
     */
    private String getModeStringFromInt(String weaponId, int modeInt) {
        String[] availableModes = getAvailableModesForWeapon(weaponId);
        if (modeInt < 1 || modeInt > availableModes.length) {
            return availableModes[0]; // Varsayılan mod
        }
        return availableModes[modeInt - 1];
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
     * Yeni sistem silahlarını işle (special_item_id ile)
     */
    private void handleNewSystemWeapons(Player player, String itemId, PlayerInteractEvent event) {
        // Önce event.getItem() kontrol et, yoksa elindeki item'ı al
        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR) {
            item = player.getInventory().getItemInMainHand();
        }
        if (item == null || item.getItemMeta() == null) return;
        
        if (!item.getItemMeta().getPersistentDataContainer().has(itemKey, PersistentDataType.STRING)) return;
        
        int mode = WeaponModeManager.getWeaponMode(item);

        if (event.getAction().toString().contains("RIGHT") || event.getAction() == Action.PHYSICAL) {
            
            // --- L5_1: HİPERİYON KILICI ---
            if (itemId.equals("l5_1_void_walker")) {
                if (mode == 1) { // Işınlanma ve Patlama
                    if (checkCooldown(player, 3000)) return; // 3 saniye cooldown
                    Block target = player.getTargetBlockExact(8);
                    if (target != null && target.getType() != Material.AIR) {
                        Location loc = target.getLocation().add(0, 1, 0);
                        loc.setDirection(player.getLocation().getDirection());
                        player.teleport(loc);
                        player.getWorld().createExplosion(loc, 4F, false, false); // Blok kırmayan patlama
                        player.playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                    }
                } else { // Kara Delik Kalkanı
                    player.sendMessage("§5Kara Delik Kalkanı Aktif! (Kodlanacak)");
                    // Buraya hasar emme mantığı eklenebilir.
                }
            }

            // --- L5_2: METEOR ÇAĞIRAN ---
            else if (itemId.equals("l5_2_meteor_caller")) {
                if (checkCooldown(player, 5000)) return; // 5 Saniye cooldown

                if (mode == 1) { // Meteor
                    Block target = player.getTargetBlockExact(50);
                    if (target != null) {
                        Location skyLoc = target.getLocation().add(0, 30, 0);
                        Fireball fireball = player.getWorld().spawn(skyLoc, Fireball.class);
                        fireball.setDirection(new Vector(0, -1, 0)); // Aşağı doğru
                        fireball.setShooter(player);
                        fireball.setYield(3F); // Patlama gücü
                        player.sendMessage("§6Meteor çağırıldı!");
                    }
                } else { // Yer Yaran
                    if (checkCooldown(player, 4000)) return; // 4 saniye cooldown
                    // Önündeki blokları lav yapma mantığı
                    Block target = player.getTargetBlockExact(5);
                    if(target != null) {
                        target.setType(Material.LAVA);
                        player.sendMessage("§cYer yarıldı!");
                    }
                }
            }
            
            // --- L5_5: ZAMANI BÜKEN ---
            else if (itemId.equals("l5_5_time_keeper")) {
                if (mode == 1) { // Zamanı Durdur
                    if (checkCooldown(player, 20000)) return; // 20 sn cooldown
                    
                    player.sendMessage("§bZaman Durdu!");
                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 2f, 0.5f);
                    
                    // 5 Saniye boyunca yakındaki mobları dondur (optimize edilmiş - her 10 tick'te bir kontrol)
                    java.util.List<LivingEntity> frozenEntities = new java.util.ArrayList<>();
                    for (Entity e : player.getNearbyEntities(20, 20, 20)) {
                        if (e instanceof LivingEntity && e != player) {
                            ((LivingEntity) e).setAI(false);
                            e.setVelocity(new Vector(0,0,0));
                            e.setGravity(false);
                            frozenEntities.add((LivingEntity) e);
                        }
                    }
                    
                    new BukkitRunnable() {
                        int ticks = 0;
                        @Override
                        public void run() {
                            if (ticks >= 100 || !player.isOnline()) { // 5 saniye (20 tick * 5)
                                // Mobları çöz
                                for (LivingEntity e : frozenEntities) {
                                    if (e.isValid()) {
                                        e.setAI(true);
                                        e.setGravity(true);
                                    }
                                }
                                this.cancel();
                                player.sendMessage("§bZaman akmaya devam ediyor.");
                                return;
                            }
                            ticks += 10; // Her 10 tick'te bir kontrol et (optimizasyon)
                        }
                    }.runTaskTimer(plugin, 0L, 10L); // 10 tick'te bir çalış (0.5 saniye)
                    
                } else { // Mod 2: Geri Sar
                    if (checkCooldown(player, 15000)) return; // 15 saniye cooldown
                    
                    Location savedLoc = lastLocation.get(player.getUniqueId());
                    Double savedHealth = lastHealth.get(player.getUniqueId());
                    
                    if (savedLoc == null || savedHealth == null) {
                        player.sendMessage("§cGeri sarılacak veri yok! (5 saniye önceki konum/can kaydedilmedi)");
                        return;
                    }
                    
                    // Konumu geri sar
                    player.teleport(savedLoc);
                    // Canı geri sar
                    double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                    player.setHealth(Math.min(maxHealth, savedHealth));
                    
                    player.sendMessage("§e5 saniye önceki konumuna ve canına döndün!");
                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.5f);
                    player.getWorld().spawnParticle(Particle.TOTEM, player.getLocation().add(0, 1, 0), 20);
                }
            }
            
            // --- TIER 4 SİLAHLAR ---
            
            // L4_1: Element Kılıcı - "Her vuruşta" özelliği onDamage eventinde işleniyor
            // Sağ tık özelliği yok, sadece vuruşta çalışır
            
            // L4_2: Yaşam ve Ölüm
            else if (itemId.equals("l4_2_life_death")) {
                if (checkCooldown(player, 2000)) return;
                if (mode == 1) { // ÖLÜM (Wither Skull)
                    player.launchProjectile(WitherSkull.class);
                    player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1f, 1f);
                } else { // YAŞAM (Heal)
                    double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                    player.setHealth(Math.min(maxHealth, player.getHealth() + 4));
                    player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0,2,0), 5);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 2f);
                }
            }
            
            // L4_3: Mjölnir V2
            else if (itemId.equals("l4_3_mjolnir_v2")) {
                if (mode == 1) { // ZİNCİRLEME ŞİMŞEK (Vuruş eventi ile tetiklenir)
                    player.sendMessage("§eŞimşek modu aktif! Vurunca çarpar.");
                } else { // FIRLAT
                    if (checkCooldown(player, 5000)) return;
                    Item projectile = player.getWorld().dropItem(player.getEyeLocation(), item.clone());
                    projectile.setVelocity(player.getLocation().getDirection().multiply(1.5));
                    projectile.setPickupDelay(1000);
                    projectile.addScoreboardTag("mjolnir_throw");
                    player.getInventory().setItemInMainHand(null);
                    
                    // Geri dönme mantığı (optimize edilmiş - her 5 tick'te bir kontrol)
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (!projectile.isValid() || !player.isOnline()) { 
                                cancel(); 
                                return; 
                            }
                            Vector dir = player.getEyeLocation().toVector().subtract(projectile.getLocation().toVector()).normalize();
                            projectile.setVelocity(dir.multiply(1.5));
                            if (projectile.getLocation().distance(player.getLocation()) < 1.5) {
                                if (player.getInventory().firstEmpty() != -1) {
                                    player.getInventory().addItem(item);
                                } else {
                                    player.getWorld().dropItem(player.getLocation(), item);
                                }
                                projectile.remove();
                                player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_RETURN, 1f, 1f);
                                cancel();
                            }
                        }
                    }.runTaskTimer(plugin, 20L, 5L); // 2L'den 5L'ye çıkarıldı (optimizasyon)
                }
            }
            
            // L4_4: Avcı Yayı
            else if (itemId.equals("l4_4_ranger_pride")) {
                event.setCancelled(true);
                if (checkCooldown(player, 1000)) return;
                
                if (mode == 1) { // SNIPER (Hitscan)
                    Location eye = player.getEyeLocation();
                    RayTraceResult result = player.getWorld().rayTraceEntities(eye, eye.getDirection(), 100, 0.5, e -> e != player && e instanceof LivingEntity);
                    
                    double distance = (result != null && result.getHitEntity() != null) ? 
                        eye.distance(result.getHitEntity().getLocation()) : 50;
                    // Optimize edilmiş: Sadece hedefe kadar particle göster
                    int particleDistance = Math.min((int)distance, 50); // Maksimum 50 blok
                    spawnParticleLine(player, Particle.CRIT, particleDistance);
                    player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 2f);
                    
                    if (result != null && result.getHitEntity() != null) {
                        ((LivingEntity) result.getHitEntity()).damage(15, player);
                        player.sendMessage("§cTam isabet!");
                    }
                } else { // SHOTGUN (Çoklu Ok)
                    for (int i = 0; i < 5; i++) {
                        Arrow arrow = player.launchProjectile(Arrow.class);
                        Vector spread = new Vector((Math.random()-0.5)*0.3, (Math.random()-0.5)*0.3, (Math.random()-0.5)*0.3);
                        arrow.setVelocity(player.getLocation().getDirection().add(spread).multiply(1.5));
                        arrow.setDamage(4);
                    }
                    player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 2f);
                }
            }
            
            // L4_5: Manyetik Eldiven
            else if (itemId.equals("l4_5_magnetic_glove")) {
                event.setCancelled(true);
                if (checkCooldown(player, 2000)) return;
                
                Entity target = getTargetEntity(player, 15);
                if (target != null) {
                    Vector dir = player.getLocation().toVector().subtract(target.getLocation().toVector()).normalize();
                    if (mode == 1) { // ÇEK
                        target.setVelocity(dir.multiply(1.5));
                        player.sendMessage("§aGel buraya!");
                    } else { // İT
                        target.setVelocity(dir.multiply(-2.0));
                        player.sendMessage("§cUza!");
                    }
                }
            }
            
            // --- TIER 5 SİLAHLAR (Eksik olanlar) ---
            
            // L5_3: Titan Katili
            else if (itemId.equals("l5_3_titan_slayer")) {
                if (mode == 2) { // MIZRAK YAĞMURU (10 mızrak düşmeli)
                    if (checkCooldown(player, 5000)) return;
                    Block targetBlock = player.getTargetBlockExact(30);
                    if (targetBlock != null) {
                        Location sky = targetBlock.getLocation().add(0, 15, 0);
                        for(int i=0; i<10; i++) { // 5'ten 10'a çıkarıldı
                            final int index = i;
                            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                                Trident tr = player.getWorld().spawn(sky.clone().add(Math.random()*4-2, 0, Math.random()*4-2), Trident.class);
                                tr.setShooter(player);
                                tr.setVelocity(new Vector(0, -1, 0).multiply(2)); // Aşağı doğru hızlı düş
                            }, index*3L); // 3 tick arayla (daha hızlı)
                        }
                        player.sendMessage("§cMızrak yağmuru başladı!");
                    }
                }
                // Mod 1 (Yüzde hasar) onDamage eventinde işlenir
            }
            
            // L5_4: Ruh Biçen
            else if (itemId.equals("l5_4_soul_reaper")) {
                if (checkCooldown(player, 10000)) return;
                if (mode == 1) { // ÇAĞIR - Öldürülen son 3 mobu çağır
                    java.util.List<EntityType> mobs = killedMobs.getOrDefault(player.getUniqueId(), new java.util.ArrayList<>());
                    if (mobs.isEmpty()) {
                        player.sendMessage("§cHenüz hiç mob öldürmedin!");
                        return;
                    }
                    
                    int count = Math.min(3, mobs.size());
                    for (int i = 0; i < count; i++) {
                        EntityType mobType = mobs.get(mobs.size() - 1 - i); // Son 3'ü al
                        try {
                            Entity summoned = player.getWorld().spawn(player.getLocation().add(Math.random()*3-1.5, 0, Math.random()*3-1.5), 
                                (Class<? extends LivingEntity>) mobType.getEntityClass());
                            if (summoned instanceof LivingEntity) {
                                ((LivingEntity) summoned).setCustomName("§2Hortlak");
                                ((LivingEntity) summoned).setCustomNameVisible(true);
                                // Hedef belirleme AI'sı kaldırılabilir
                            }
                        } catch (Exception e) {
                            // Entity type spawn edilemezse atla
                        }
                    }
                    player.sendMessage("§5" + count + " hortlak yükseldi!");
                } else { // PATLAT - Yakındaki tüm hortlakları patlat
                    int count = 0;
                    for (Entity e : player.getNearbyEntities(10, 10, 10)) {
                        if (e instanceof LivingEntity && e.getCustomName() != null && 
                            e.getCustomName().contains("Hortlak")) {
                            e.getWorld().createExplosion(e.getLocation(), 2f, false, false);
                            e.remove();
                            count++;
                        }
                    }
                    if (count > 0) {
                        player.sendMessage("§c" + count + " hortlak patlatıldı!");
                    } else {
                        player.sendMessage("§cYakında hortlak yok!");
                    }
                }
            }
        }
    }
    
    /**
     * Hedef entity bul
     */
    private Entity getTargetEntity(Player p, int range) {
        RayTraceResult res = p.getWorld().rayTraceEntities(p.getEyeLocation(), p.getEyeLocation().getDirection(), range, 1.0, e -> e != p && e instanceof LivingEntity);
        return res != null ? res.getHitEntity() : null;
    }
    
    /**
     * Cooldown kontrolü
     */
    private boolean checkCooldown(Player player, long timeMillis) {
        if (cooldowns.containsKey(player.getUniqueId())) {
            long timeLeft = (cooldowns.get(player.getUniqueId()) + timeMillis) - System.currentTimeMillis();
            if (timeLeft > 0) {
                player.sendMessage("§cBekle: " + String.format("%.1f", timeLeft / 1000.0) + "sn");
                return true;
            }
        }
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        return false;
    }
    
    /**
     * Tier 1-3 silahlar için sağ tık yetenekleri
     */
    private void handleTier1To3Interact(Player player, String itemId, PlayerInteractEvent event) {
        if (!event.getAction().toString().contains("RIGHT")) return;
        
        // Tier 1: Yerçekimi Gürzü
        if (itemId.equals("l1_3_gravity_mace")) {
            if (checkCooldown(player, 5000)) return;
            player.setVelocity(player.getLocation().getDirection().multiply(1.5).setY(1.2));
            player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 1f);
            player.setFallDistance(-100); // Düşüş hasarını engelle
        }
        
        // Tier 2: Alev Kılıcı
        else if (itemId.equals("l2_1_inferno_sword")) {
            if (checkCooldown(player, 4000)) return;
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1f, 1f);
            spawnParticleLine(player, Particle.FLAME, 10);
            for (Entity e : player.getNearbyEntities(5, 2, 5)) {
                if (e instanceof LivingEntity && e != player) {
                    // Basit bir görüş açısı kontrolü (Önünde mi?)
                    Vector toEntity = e.getLocation().toVector().subtract(player.getLocation().toVector());
                    if (player.getLocation().getDirection().angle(toEntity) < 0.5) {
                        e.setFireTicks(100); // 5 saniye yak
                        ((LivingEntity) e).damage(4, player);
                    }
                }
            }
        }
        
        // Tier 2: Buz Asası
        else if (itemId.equals("l2_2_frost_wand")) {
            if (checkCooldown(player, 6000)) return;
            Snowball snowball = player.launchProjectile(Snowball.class);
            snowball.setShooter(player);
            // Kartopu metadata'sına işaret koy
            snowball.addScoreboardTag("frost_bolt");
            player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1f, 2f);
        }
        
        // Tier 2: Golem Kalkanı (Eğilme Kontrolü)
        else if (itemId.equals("l2_4_guardian_shield") && player.isSneaking()) {
            if (checkCooldown(player, 10000)) return;
            player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0,1,0), 10, 2, 0.5, 2);
            for(Entity e : player.getNearbyEntities(5, 5, 5)) {
                if(e instanceof Player && e != player) {
                    ((Player) e).addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1));
                    ((Player) e).addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 0));
                    player.sendMessage("§aTakım arkadaşların korundu!");
                }
            }
        }
        
        // Tier 3: Gölge Katanası (Dash) - 6 blok dash
        else if (itemId.equals("l3_1_shadow_katana")) {
            if (checkCooldown(player, 3000)) return;
            Vector dash = player.getLocation().getDirection().multiply(3.0); // 6 blok için 3.0 (2.5'ten artırıldı)
            player.setVelocity(dash);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.5f);
            player.getWorld().spawnParticle(Particle.SMOKE_LARGE, player.getLocation(), 10, 0.5, 1, 0.5, 0);
            
            // Yoldaki moblara hasar ver (6 blok yol boyunca)
            Location start = player.getLocation();
            Vector direction = player.getLocation().getDirection().normalize();
            for (int i = 1; i <= 6; i++) {
                Location checkLoc = start.clone().add(direction.clone().multiply(i));
                for(Entity e : player.getWorld().getNearbyEntities(checkLoc, 1, 1, 1)) {
                    if(e instanceof LivingEntity && e != player) {
                        ((LivingEntity) e).damage(6, player);
                    }
                }
            }
        }
        
        // Tier 3: Deprem Çekici
        else if (itemId.equals("l3_2_earthshaker")) {
            if (checkCooldown(player, 8000)) return;
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 1f, 0.5f);
            createEarthquake(player);
        }
        
        // Tier 3: Taramalı Yay
        else if (itemId.equals("l3_3_machine_crossbow")) {
            if (checkCooldown(player, 5000)) return;
            new BukkitRunnable() {
                int count = 0;
                @Override
                public void run() {
                    if (count >= 5) { cancel(); return; }
                    Arrow arrow = player.launchProjectile(Arrow.class);
                    arrow.setVelocity(player.getLocation().getDirection().multiply(2.5));
                    arrow.setDamage(2); // Düşük hasar ama seri
                    player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1f, 2f);
                    count++;
                }
            }.runTaskTimer(plugin, 0L, 2L); // Her 0.1 saniyede bir atış
        }
        
        // Tier 3: Büyücü Küresi (Güdümlü Mermiler)
        else if (itemId.equals("l3_4_witch_orb")) {
            if (checkCooldown(player, 4000)) return;
            // En yakındaki 3 düşmana güdümlü mermi at
            java.util.List<LivingEntity> targets = new java.util.ArrayList<>();
            for (Entity e : player.getNearbyEntities(20, 20, 20)) {
                if (e instanceof LivingEntity && e != player && !(e instanceof Player)) {
                    targets.add((LivingEntity) e);
                }
            }
            // En yakın 3'ü al
            targets.sort((a, b) -> Double.compare(
                a.getLocation().distance(player.getLocation()),
                b.getLocation().distance(player.getLocation())
            ));
            
            int count = Math.min(3, targets.size());
            for (int i = 0; i < count; i++) {
                LivingEntity target = targets.get(i);
                // Güdümlü mermi (Snowball ile simüle ediyoruz)
                Snowball missile = player.launchProjectile(Snowball.class);
                missile.addScoreboardTag("guided_missile");
                missile.setShooter(player);
                
                // Mermiyi hedefe yönlendir
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!missile.isValid() || !target.isValid()) {
                            cancel();
                            return;
                        }
                        Vector direction = target.getLocation().add(0, 1, 0).toVector()
                            .subtract(missile.getLocation().toVector()).normalize();
                        missile.setVelocity(direction.multiply(1.2));
                        
                        // Hedefe ulaştıysa patlat
                        if (missile.getLocation().distance(target.getLocation()) < 1.5) {
                            target.getWorld().createExplosion(missile.getLocation(), 2F, false, false);
                            target.damage(8, player);
                            missile.remove();
                            cancel();
                        }
                    }
                }.runTaskTimer(plugin, 0L, 2L);
            }
            player.sendMessage("§5Güdümlü mermiler fırlatıldı!");
        }
        
        // Tier 3: Hayalet Hançeri (Görünmezlik)
        else if (itemId.equals("l3_5_phantom_dagger")) {
            if (checkCooldown(player, 15000)) return;
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 0, false, false)); // 5 sn
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1));
            player.sendMessage("§7Gölgeye karıştın...");
            player.getWorld().playSound(player.getLocation(), Sound.AMBIENT_CAVE, 1f, 2f);
        }
    }
    
    /**
     * Partikül çizgisi oluştur
     */
    private void spawnParticleLine(Player player, Particle particle, int distance) {
        // Optimize edilmiş: Her 2 blokta bir particle spawn et (lag azaltma)
        Location start = player.getEyeLocation();
        Vector direction = start.getDirection().normalize();
        int step = Math.max(2, distance / 25); // Maksimum 25 particle
        for (int i = 0; i < distance; i += step) {
            Location loc = start.clone().add(direction.clone().multiply(i));
            player.getWorld().spawnParticle(particle, loc, 1, 0, 0, 0, 0);
        }
    }
    
    /**
     * Deprem efekti oluştur
     */
    private void createEarthquake(Player player) {
        for (Entity e : player.getNearbyEntities(5, 5, 5)) {
            if (e instanceof LivingEntity && e != player) {
                e.setVelocity(new Vector(0, 1.5, 0)); // Havaya fırlat
                ((LivingEntity) e).damage(4, player);
            }
        }
        // Yerdeki blok efekti
        Location loc = player.getLocation();
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                if (Math.random() > 0.7) {
                    player.getWorld().spawnParticle(Particle.BLOCK_CRACK, loc.clone().add(x, 0, z), 5, Bukkit.createBlockData(Material.DIRT));
                }
            }
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
        
        // WeaponModeManager'dan modu al
        int modeInt = WeaponModeManager.getWeaponMode(item);
        String mode = getModeStringFromInt(weaponId, modeInt);
        
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
        player.getWorld().spawnParticle(Particle.CLOUD, startLoc, 10, 0.5, 0.5, 0.5, 0.1); // 20'den 10'a düşürüldü
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
     * Vuruş yetenekleri (Tier 1-3 ve eski sistem)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        
        Player player = (Player) event.getDamager();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null) return;
        
        // YENİ SİSTEM: Tier 1-5 silahlar
        String specialItemId = getSpecialItemId(item);
        if (specialItemId != null) {
            if (specialItemId.startsWith("l1_") || specialItemId.startsWith("l2_") || specialItemId.startsWith("l3_")) {
                handleTier1To3Damage(player, specialItemId, event);
                return;
            }
            if (specialItemId.startsWith("l4_") || specialItemId.startsWith("l5_")) {
                handleTier4To5Damage(player, specialItemId, event);
                return;
            }
        }
        
        // ESKİ SİSTEM: WEAPON_L formatı
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
                    livingTarget.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 0, false, false, true));
                    player.sendMessage("§2Zehir etkisi uygulandı!");
                    break;
                case "WEAPON_L2_2": // Yavaşlatma Baltası - 3 saniye yavaşlatma
                    livingTarget.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 0, false, false, true));
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
    
    /**
     * Tier 1-3 silahlar için vuruş yetenekleri
     */
    private void handleTier1To3Damage(Player player, String itemId, EntityDamageByEntityEvent event) {
        Entity target = event.getEntity();
        if (!(target instanceof LivingEntity)) return;
        
        // Tier 1: Hız Hançeri (Geri İtme ve Hızlı Vuruş)
        if (itemId.equals("l1_1_rogue_dagger")) {
            // Geri itme efekti (küçük)
            Vector knockback = player.getLocation().getDirection().multiply(-0.2);
            target.setVelocity(target.getVelocity().add(knockback));
            // Attack Speed zaten attribute modifier ile eklenmiş
        }
        
        // Tier 1: Çiftçi Tırpanı (Alan Hasarı)
        else if (itemId.equals("l1_2_harvest_scythe")) {
            for (Entity e : target.getNearbyEntities(3, 2, 3)) {
                if (e instanceof LivingEntity && e != player) {
                    ((LivingEntity) e).damage(event.getDamage() * 0.5, player);
                }
            }
            player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, target.getLocation(), 1);
        }
        
        // Tier 1: Vampir Dişi (Can Çalma)
        else if (itemId.equals("l1_5_vampire_blade")) {
            double heal = event.getDamage() * 0.20;
            double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            if (player.getHealth() + heal > maxHealth) player.setHealth(maxHealth);
            else player.setHealth(player.getHealth() + heal);
        }
        
        // Tier 2: Şok Baltası (Kritik Yıldırım)
        else if (itemId.equals("l2_5_thunder_axe")) {
            // Kritik vuruş kontrolü (Oyuncu düşerken vurursa)
            if (player.getVelocity().getY() < -0.08 && !player.isOnGround()) {
                target.getWorld().strikeLightningEffect(target.getLocation());
                event.setDamage(event.getDamage() + 5); // +5 Gerçek Hasar
            }
        }
        
        // Tier 3: Hayalet Hançeri (Suikast Bonusu)
        else if (itemId.equals("l3_5_phantom_dagger")) {
            if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                event.setDamage(event.getDamage() * 3); // x3 Hasar
                player.removePotionEffect(PotionEffectType.INVISIBILITY); // Görünmezlik bozulur
                player.sendMessage("§cSUİKAST!");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1f, 0.5f);
            }
        }
    }
    
    /**
     * Tier 4-5 silahlar için vuruş yetenekleri
     */
    private void handleTier4To5Damage(Player player, String itemId, EntityDamageByEntityEvent event) {
        Entity target = event.getEntity();
        if (!(target instanceof LivingEntity)) return;
        
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getItemMeta() == null) return;
        
        // itemId'yi item'dan kontrol et (güvenlik için)
        String actualItemId = getSpecialItemId(item);
        if (actualItemId == null || !actualItemId.equals(itemId)) return;
        
        int mode = WeaponModeManager.getWeaponMode(item);
        
        // L4_1: Element Kılıcı - Her vuruşta etrafa alev saçar veya yavaşlatır
        if (itemId.equals("l4_1_elementalist")) {
            if (mode == 1) { // ATEŞ MODU - Her vuruşta etrafa alev saçar
                for (Entity e : target.getNearbyEntities(3, 2, 3)) {
                    if (e instanceof LivingEntity && e != player && e != target) {
                        e.setFireTicks(40);
                        ((LivingEntity) e).damage(2, player);
                    }
                }
                player.getWorld().spawnParticle(Particle.FLAME, target.getLocation(), 5, 0.5, 0.5, 0.5, 0.1);
            } else if (mode == 2) { // BUZ MODU - Her vuruşta yavaşlatır (Slowness III)
                ((LivingEntity) target).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 2)); // Slowness III
                player.getWorld().spawnParticle(Particle.SNOWFLAKE, target.getLocation().add(0,1,0), 5);
            }
        }
        
        // L4_3: Mjölnir Zincirleme Şimşek
        else if (itemId.equals("l4_3_mjolnir_v2") && mode == 1) {
            int jumps = 0;
            Entity current = target;
            for(Entity nearby : current.getNearbyEntities(5, 5, 5)) {
                if(nearby instanceof LivingEntity && nearby != player && jumps < 3) {
                    ((LivingEntity) nearby).damage(5, player);
                    nearby.getWorld().strikeLightningEffect(nearby.getLocation());
                    jumps++;
                }
            }
        }
        
        // L5_3: Titan Katili (Yüzdelik Hasar)
        else if (itemId.equals("l5_3_titan_slayer") && mode == 1) {
            LivingEntity livingTarget = (LivingEntity) target;
            double damage = livingTarget.getHealth() * 0.05; // Mevcut canın %5'i
            event.setDamage(event.getDamage() + damage);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 2f);
        }
    }
    
    /**
     * Mermi isabet yetenekleri
     */
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) return;
        Player shooter = (Player) event.getEntity().getShooter();
        
        // Kartopu (Buz Asası)
        if (event.getEntity() instanceof Snowball && event.getEntity().getScoreboardTags().contains("frost_bolt")) {
            if (event.getHitEntity() instanceof LivingEntity) {
                LivingEntity target = (LivingEntity) event.getHitEntity();
                target.setFreezeTicks(100); // 5 saniye donma (1.17+)
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 4)); // Hareket edemez
                target.getWorld().spawnParticle(Particle.SNOWFLAKE, target.getLocation(), 10, 0.5, 1, 0.5, 0.1); // 20'den 10'a düşürüldü
            }
        }
        
        // Ok (Patlayıcı Yay)
        if (event.getEntity() instanceof Arrow && event.getEntity().getScoreboardTags().contains("boom_arrow")) {
            event.getEntity().getWorld().createExplosion(event.getEntity().getLocation(), 2F, false, false);
        }
        
        // Mızrak (Zehirli Mızrak)
        if (event.getEntity() instanceof Trident) {
            // Trident yere düştüyse veya birine çarptıysa zehir bulutu
            if (event.getHitBlock() != null || event.getHitEntity() != null) {
                AreaEffectCloud cloud = (AreaEffectCloud) event.getEntity().getWorld().spawnEntity(
                    event.getEntity().getLocation(), EntityType.AREA_EFFECT_CLOUD);
                cloud.setRadius(3f);
                cloud.setDuration(100); // 5 sn
                cloud.addCustomEffect(new PotionEffect(PotionEffectType.POISON, 100, 1), true);
                cloud.setParticle(Particle.SLIME);
            }
        }
    }
    
    /**
     * Yay atışı - Mermiye tag ekle
     */
    @EventHandler
    public void onShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        ItemStack bow = event.getBow();
        if (bow == null) return;
        
        String id = getSpecialItemId(bow);
        if ("l1_4_boom_bow".equals(id)) {
            event.getProjectile().addScoreboardTag("boom_arrow");
        }
    }
    
    /**
     * Mob öldürme - L5_4 için öldürülen mobları kaydet
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.getKiller() == null) return;
        
        Player killer = entity.getKiller();
        ItemStack weapon = killer.getInventory().getItemInMainHand();
        if (weapon == null) return;
        
        String itemId = getSpecialItemId(weapon);
        if (itemId != null && itemId.equals("l5_4_soul_reaper")) {
            // Öldürülen mobu kaydet (maksimum 3)
            java.util.List<EntityType> mobs = killedMobs.getOrDefault(killer.getUniqueId(), new java.util.ArrayList<>());
            mobs.add(entity.getType());
            // Son 3'ü tut
            if (mobs.size() > 3) {
                mobs.remove(0);
            }
            killedMobs.put(killer.getUniqueId(), mobs);
        }
    }
    
    /**
     * L5_5 için konum ve can kaydetme (her 5 saniyede bir)
     */
    @EventHandler
    public void onPlayerMove(org.bukkit.event.player.PlayerMoveEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null) return;
        
        String itemId = getSpecialItemId(item);
        if (itemId != null && itemId.equals("l5_5_time_keeper")) {
            // Her 5 saniyede bir konum ve can kaydet (100 tick = 5 saniye)
            Long lastSave = lastLocation.containsKey(player.getUniqueId()) ? 
                System.currentTimeMillis() : 0L;
            
            if (System.currentTimeMillis() - lastSave > 5000) { // 5 saniye
                lastLocation.put(player.getUniqueId(), player.getLocation().clone());
                lastHealth.put(player.getUniqueId(), player.getHealth());
            }
        }
    }
}

