package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.ItemManager;
import me.mami.stratocraft.manager.NewBatteryManager;
import me.mami.stratocraft.manager.NewBatteryManager.BlockPattern;
import me.mami.stratocraft.manager.NewBatteryManager.NewBatteryData;
import me.mami.stratocraft.manager.NewBatteryManager.RecipeCheckResult;
import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Structure;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Yeni Esnek Batarya Sistemi Listener
 * Her bataryanın kendine özel tarif kontrol fonksiyonu var
 */
public class NewBatteryListener implements Listener {
    
    private final NewBatteryManager batteryManager;
    private final TerritoryManager territoryManager;
    private me.mami.stratocraft.manager.TrainingManager trainingManager;
    private me.mami.stratocraft.manager.BatteryParticleManager particleManager;
    private me.mami.stratocraft.manager.GameBalanceConfig balanceConfig;
    
    public NewBatteryListener(NewBatteryManager bm, TerritoryManager tm) {
        this.batteryManager = bm;
        this.territoryManager = tm;
        // ✅ PARTİKÜL SİSTEMİ: BatteryParticleManager'ı al
        me.mami.stratocraft.Main plugin = me.mami.stratocraft.Main.getInstance();
        if (plugin != null) {
            this.particleManager = plugin.getBatteryParticleManager();
            // ✅ OYUN DENGE CONFIG: GameBalanceConfig'i al
            if (plugin.getConfigManager() != null) {
                this.balanceConfig = plugin.getConfigManager().getGameBalanceConfig();
            }
        }
    }
    
    public void setTrainingManager(me.mami.stratocraft.manager.TrainingManager tm) {
        this.trainingManager = tm;
    }
    
    private int getAlchemyTowerLevel(Player p) {
        Clan clan = territoryManager.getClanManager().getClanByPlayer(p.getUniqueId());
        if (clan == null) return 0;
        return clan.getStructures().stream()
                .filter(s -> s.getType() == Structure.Type.ALCHEMY_TOWER)
                .mapToInt(Structure::getLevel)
                .max()
                .orElse(0);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        // Çift el kontrolünü engelle (Sadece ana el)
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        
        Player player = event.getPlayer();
        Action action = event.getAction();
        int slot = player.getInventory().getHeldItemSlot();
        
        // --- DURUM 1: ATEŞLEME (SOL TIK) ---
        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            if (batteryManager.hasLoadedBattery(player, slot)) {
                event.setCancelled(true);
                fireBattery(player, slot);
                return;
            }
        }
        
        // --- DURUM 2: YÜKLEME (SAĞ TIK + SHIFT) ---
        if (action == Action.RIGHT_CLICK_BLOCK && player.isSneaking()) {
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null) {
                checkAndLoadBattery(player, clickedBlock, slot, event);
            }
        }
        
        // --- DURUM 3: İPTAL ETME (SAĞ TIK - Yüklü batarya varsa) ---
        if ((action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) && 
            !player.isSneaking()) {
            if (batteryManager.hasLoadedBattery(player, slot)) {
                event.setCancelled(true);
                dischargeBattery(player, slot);
                return;
            }
        }
    }
    
    /**
     * Batarya yükleme kontrolü
     */
    private void checkAndLoadBattery(Player player, Block centerBlock, int slot, PlayerInteractEvent event) {
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem == null) return;
        
        // MAYIN İTEMI KONTROLÜ - Eğer mayın itemı ise, batarya kontrolü yapma!
        if (ItemManager.isCustomItem(handItem, "MINE_EXPLOSIVE") ||
            ItemManager.isCustomItem(handItem, "MINE_POISON") ||
            ItemManager.isCustomItem(handItem, "MINE_SLOWNESS") ||
            ItemManager.isCustomItem(handItem, "MINE_LIGHTNING") ||
            ItemManager.isCustomItem(handItem, "MINE_FIRE") ||
            ItemManager.isCustomItem(handItem, "MINE_CAGE") ||
            ItemManager.isCustomItem(handItem, "MINE_LAUNCH") ||
            ItemManager.isCustomItem(handItem, "MINE_MOB_SPAWN") ||
            ItemManager.isCustomItem(handItem, "MINE_BLINDNESS") ||
            ItemManager.isCustomItem(handItem, "MINE_WEAKNESS") ||
            ItemManager.isCustomItem(handItem, "MINE_FREEZE") ||
            ItemManager.isCustomItem(handItem, "MINE_CONFUSION") ||
            ItemManager.isCustomItem(handItem, "MINE_FATIGUE") ||
            ItemManager.isCustomItem(handItem, "MINE_POISON_CLOUD") ||
            ItemManager.isCustomItem(handItem, "MINE_LIGHTNING_STORM") ||
            ItemManager.isCustomItem(handItem, "MINE_MEGA_EXPLOSIVE") ||
            ItemManager.isCustomItem(handItem, "MINE_LARGE_CAGE") ||
            ItemManager.isCustomItem(handItem, "MINE_SUPER_LAUNCH") ||
            ItemManager.isCustomItem(handItem, "MINE_ELITE_MOB_SPAWN") ||
            ItemManager.isCustomItem(handItem, "MINE_MULTI_EFFECT") ||
            ItemManager.isCustomItem(handItem, "MINE_NUCLEAR_EXPLOSIVE") ||
            ItemManager.isCustomItem(handItem, "MINE_DEATH_CLOUD") ||
            ItemManager.isCustomItem(handItem, "MINE_THUNDERSTORM") ||
            ItemManager.isCustomItem(handItem, "MINE_BOSS_SPAWN") ||
            ItemManager.isCustomItem(handItem, "MINE_CHAOS") ||
            ItemManager.isCustomItem(handItem, "MINE_CONCEALER")) {
            return; // Mayın itemı, batarya kontrolü yapma!
        }
        
        // BASINÇ PLAKASI KONTROLÜ - Eğer basınç plakasına tıklıyorsa, batarya kontrolü yapma!
        if (centerBlock.getType() == Material.STONE_PRESSURE_PLATE ||
            centerBlock.getType() == Material.OAK_PRESSURE_PLATE ||
            centerBlock.getType() == Material.BIRCH_PRESSURE_PLATE ||
            centerBlock.getType() == Material.DARK_OAK_PRESSURE_PLATE ||
            centerBlock.getType() == Material.WARPED_PRESSURE_PLATE ||
            centerBlock.getType() == Material.CRIMSON_PRESSURE_PLATE ||
            centerBlock.getType() == Material.POLISHED_BLACKSTONE_PRESSURE_PLATE ||
            centerBlock.getType() == Material.LIGHT_WEIGHTED_PRESSURE_PLATE ||
            centerBlock.getType() == Material.HEAVY_WEIGHTED_PRESSURE_PLATE) {
            return; // Basınç plakası, batarya kontrolü yapma!
        }
        
        // ÖNCELİKLE BATARYA TARİFİNİ KONTROL ET!
        RecipeCheckResult result = batteryManager.checkAllRecipes(centerBlock);
        
        // Eğer batarya tarifi değilse, SESSIZCE ÇIK (diğer sistemlere karışma!)
        if (!result.matches()) {
            return;
        }
        
        // Tarif eşleşti, ŞİMDİ yakıt kontrolü yap
        Material fuel = handItem.getType();
        boolean isRedDiamond = ItemManager.isCustomItem(handItem, "RED_DIAMOND");
        boolean isDarkMatter = ItemManager.isCustomItem(handItem, "DARK_MATTER");
        
        boolean isValidFuel = fuel == Material.DIAMOND || fuel == Material.IRON_INGOT ||
                             isRedDiamond || isDarkMatter;
        
        if (!isValidFuel) {
            player.sendMessage("§cGeçersiz yakıt! Elmas, Demir, Kırmızı Elmas veya Karanlık Madde kullanın.");
            return;
        }
        
        // Tarif zaten eşleşti (yukarıda kontrol ettik), bataryayı yükle
        String batteryName = result.getBatteryName();
        
        // Seviye bilgisini RecipeChecker'dan al (her tarif kendi seviyesini belirler)
        int batteryLevel = batteryManager.getBatteryLevel(batteryName);
        
        // Seviye 5 için DARK_MATTER zorunlu kontrolü (sadece yakıt kontrolü, tarif kontrolü değil)
        if (batteryLevel == 5 && !isDarkMatter) {
            player.sendMessage("§c§lSeviye 5 bataryalar için §5§lKaranlık Madde §cgerekli!");
            event.setCancelled(true);
            return;
        }
        
        // Batarya verisini oluştur
        int alchemyLevel = getAlchemyTowerLevel(player);
        ItemStack offHand = player.getInventory().getItemInOffHand();
        boolean hasAmplifier = ItemManager.isCustomItem(offHand, "FLAME_AMPLIFIER");
        
        // Antrenman çarpanını hesapla (seviye bazlı)
        String trainingKey = getTrainingKey(batteryName);
        double trainingMultiplier = 1.0;
        if (trainingManager != null) {
            trainingMultiplier = trainingManager.getMasteryMultiplier(player.getUniqueId(), trainingKey, batteryLevel);
        }
        
        // Batarya ismini al (RecipeCheckResult'tan - her tarif kendi ismini belirler)
        String batteryNameFromResult = result.getBatteryName();
        if (batteryNameFromResult == null || batteryNameFromResult.isEmpty()) {
            player.sendMessage("§cHata: Batarya ismi bulunamadı!");
            return;
        }
        
        NewBatteryData data = new NewBatteryData(
            batteryNameFromResult,
            fuel,
            alchemyLevel,
            hasAmplifier,
            trainingMultiplier,
            isRedDiamond,
            isDarkMatter,
            batteryLevel
        );
        
        // Yükleme işlemi
        loadBattery(player, centerBlock, handItem, slot, data, result.getBlocksToRemove(), event);
    }
    
    /**
     * Bataryayı yükleme işlemi
     */
    private void loadBattery(Player player, Block center, ItemStack handItem, int slot, 
                            NewBatteryData data, java.util.List<Block> blocksToRemove, 
                            PlayerInteractEvent event) {
        event.setCancelled(true);
        
        // 1. Yakıtı tüket
        if (handItem.getType() != Material.LAVA_BUCKET) {
            handItem.setAmount(handItem.getAmount() - 1);
        }
        
        // 2. Tüm blokları yok et
        for (Block block : blocksToRemove) {
            if (block != null && block.getType() != Material.AIR) {
                block.setType(Material.AIR);
            }
        }
        
        // 3. Efektler
        org.bukkit.Location effectLoc = center.getLocation().add(0.5, 0.5, 0.5);
        player.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_LARGE, effectLoc, 1);
        player.getWorld().spawnParticle(org.bukkit.Particle.SMOKE_LARGE, effectLoc, 10);
        
        // 4. Bataryayı Manager'a kaydet
        batteryManager.loadBattery(player, slot, data);
        
        // 5. ✅ YENİ PARTİKÜL SİSTEMİ: Modüler partikül efekti başlat
        if (particleManager != null) {
            org.bukkit.Particle particleType = me.mami.stratocraft.manager.BatteryParticleManager.getParticleType(data.getBatteryName());
            particleManager.startBatteryParticles(player, slot, particleType);
        } else {
            // Fallback: Eski sistem (geriye dönük uyumluluk)
            startBatteryParticles(player, slot, data);
        }
        
        // 6. Ses efekti
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 2f);
        
        // 7. Seviye mesajı
        if (data.getBatteryLevel() > 1) {
            player.sendMessage("§6§lSeviye " + data.getBatteryLevel() + " Batarya tespit edildi!");
            if (data.getBatteryLevel() == 5) {
                player.sendMessage("§c§lEFSANEVI GÜÇ AKTİF!");
            }
        }
        
        // 8. Antrenman durumu mesajı (detaylı)
        if (trainingManager != null) {
            String trainingKey = getTrainingKey(data.getBatteryName());
            int batteryLvl = data.getBatteryLevel();
            double multiplier = trainingManager.getMasteryMultiplier(player.getUniqueId(), trainingKey, batteryLvl);
            int uses = trainingManager.getTotalUses(player.getUniqueId(), trainingKey);
            
            if (multiplier < 1.0) {
                // Antrenman modu
                player.sendMessage("§e§l━━━━━━━━━━━━━━━━━━━━━━━━━━");
                player.sendMessage("§e§l⚡ ANTRENMAN MODU AKTİF ⚡");
                player.sendMessage("§7Mevcut Güç: §e%" + String.format("%.0f", multiplier * 100));
                player.sendMessage("§7Kullanım: §e" + uses + "§7/§e5 §7(Tam güç için)");
                
                // Sonraki seviye bilgisi
                if (uses < 5) {
                    double nextMultiplier = trainingManager.getMasteryMultiplier(player.getUniqueId(), trainingKey, batteryLvl);
                    int nextUses = uses + 1;
                    // Sonraki kullanımdaki gücü hesapla
                    double nextPower = calculateNextPower(uses + 1, batteryLvl);
                    player.sendMessage("§7Sonraki: §a%" + String.format("%.0f", nextPower * 100) + " §7(+" + String.format("%.0f", (nextPower - multiplier) * 100) + "%)");
                }
                player.sendMessage("§e§l━━━━━━━━━━━━━━━━━━━━━━━━━━");
            } else if (multiplier == 1.0) {
                // Tam güç
                if (uses <= 5) {
                    player.sendMessage("§a§l✓ TAM GÜÇ ULAŞILDI!");
                    player.sendMessage("§720 kullanımda §6§lMASTERY §7kilidi açılacak!");
                }
            } else if (multiplier > 1.0 && multiplier < 1.5) {
                // Mastery modu (henüz maksimuma ulaşmadı)
                player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━");
                player.sendMessage("§6§l⭐ MASTERY MODU AKTİF ⭐");
                player.sendMessage("§7Mevcut Güç: §6%" + String.format("%.0f", multiplier * 100));
                player.sendMessage("§7Kullanım: §6" + uses + "§7/§630 §7(Maksimum güç için)");
                double remaining = 1.5 - multiplier;
                player.sendMessage("§7Kalan Artış: §e+" + String.format("%.0f", remaining * 100) + "%");
                player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━");
            } else {
                // Maksimum güç
                player.sendMessage("§d§l━━━━━━━━━━━━━━━━━━━━━━━━━━");
                player.sendMessage("§d§l★ MAKSİMUM GÜÇ! ★");
                player.sendMessage("§7Güç: §d%150 §7(§6+" + uses + " kullanım§7)");
                player.sendMessage("§d§l━━━━━━━━━━━━━━━━━━━━━━━━━━");
            }
        }
    }
    
    /**
     * Bataryayı ateşleme mantığı
     */
    private void fireBattery(Player player, int slot) {
        NewBatteryData data = batteryManager.getLoadedBattery(player, slot);
        if (data == null) return;
        
        // ✅ YENİ PARTİKÜL SİSTEMİ: Partikül efektini durdur
        if (particleManager != null) {
            particleManager.stopBatteryParticles(player, slot);
        } else {
            stopBatteryParticles(player, slot);
        }
        
        // Antrenman kaydı
        if (trainingManager != null) {
            String trainingKey = getTrainingKey(data.getBatteryName());
            trainingManager.recordUse(player.getUniqueId(), trainingKey);
        }
        
        // NewBatteryManager'dan ateşleme fonksiyonunu çağır
        batteryManager.fireBattery(player, data);
        
        // Ateşlendikten sonra bataryayı sil
        batteryManager.removeBattery(player, slot);
        
        // Ses efekti
        player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 1f);
        
        // Mastery mesajları
        if (trainingManager != null) {
            handleMasteryMessages(player, getTrainingKey(data.getBatteryName()), data);
        }
    }
    
    /**
     * Training key'i batarya isminden al
     */
    private String getTrainingKey(String batteryName) {
        // Batarya ismini training key'e çevir
        return batteryName.toUpperCase().replace(" ", "_").replace("(", "").replace(")", "")
            .replace("Ç", "C").replace("Ğ", "G").replace("İ", "I")
            .replace("Ö", "O").replace("Ş", "S").replace("Ü", "U")
            .replace("EFSANEVİ", "LEG").replace("GELİŞMİŞ", "ADV");
    }
    
    /**
     * Sonraki kullanımdaki gücü hesapla (bilgi mesajları için)
     */
    private double calculateNextPower(int nextUses, int batteryLevel) {
        // ✅ CONFIG: Başlangıç gücü (config'den al)
        double startPower;
        if (balanceConfig != null) {
            switch (batteryLevel) {
                case 1: startPower = balanceConfig.getTrainingLevel1StartPower(); break;
                case 2: startPower = balanceConfig.getTrainingLevel2StartPower(); break;
                case 3: startPower = balanceConfig.getTrainingLevel3StartPower(); break;
                case 4: startPower = balanceConfig.getTrainingLevel4StartPower(); break;
                case 5: startPower = balanceConfig.getTrainingLevel5StartPower(); break;
                default: startPower = 0.5; break;
            }
        } else {
            // Fallback (config yüklenmemişse)
            switch (batteryLevel) {
                case 1: startPower = 0.2; break;
                case 2: startPower = 0.4; break;
                case 3: startPower = 0.6; break;
                case 4: startPower = 0.7; break;
                case 5: startPower = 0.8; break;
                default: startPower = 0.5; break;
            }
        }
        
        // ✅ CONFIG: Kullanım sayısına göre güç (config'den eşikler al)
        int fullPowerUses = balanceConfig != null ? balanceConfig.getTrainingFullPowerUses() : 5;
        int masteryStartUses = balanceConfig != null ? balanceConfig.getTrainingMasteryStartUses() : 20;
        int maxPowerUses = balanceConfig != null ? balanceConfig.getTrainingMaxPowerUses() : 30;
        double increment2 = balanceConfig != null ? balanceConfig.getTrainingPowerIncrement2() : 0.2;
        double increment3 = balanceConfig != null ? balanceConfig.getTrainingPowerIncrement3() : 0.4;
        double increment4 = balanceConfig != null ? balanceConfig.getTrainingPowerIncrement4() : 0.6;
        double maxMultiplier = balanceConfig != null ? balanceConfig.getTrainingMaxPowerMultiplier() : 1.5;
        double masteryIncrement = balanceConfig != null ? balanceConfig.getTrainingMasteryPowerIncrement() : 0.5;
        
        if (nextUses <= 1) {
            return startPower;
        } else if (nextUses == 2) {
            return Math.min(1.0, startPower + increment2);
        } else if (nextUses == 3) {
            return Math.min(1.0, startPower + increment3);
        } else if (nextUses == 4) {
            return Math.min(1.0, startPower + increment4);
        } else if (nextUses >= fullPowerUses && nextUses <= masteryStartUses) {
            return 1.0;
        } else if (nextUses > masteryStartUses && nextUses <= maxPowerUses) {
            int extraUses = nextUses - masteryStartUses;
            int masteryRange = maxPowerUses - masteryStartUses;
            double extraPower = (extraUses / (double) masteryRange) * masteryIncrement;
            return 1.0 + extraPower;
        } else {
            return maxMultiplier;
        }
    }
    
    /**
     * Partikül efekti başlat (etrafında dönmeli)
     */
    private void startBatteryParticles(Player player, int slot, NewBatteryData data) {
        // Partikül tipini batarya tipine göre belirle
        org.bukkit.Particle particleType = getParticleType(data.getBatteryName());
        
        // Partikül task'ı başlat
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                if (!batteryManager.hasLoadedBattery(player, slot)) {
                    cancel();
                    return;
                }
                
                // Oyuncu etrafında dönen partiküller
                org.bukkit.Location loc = player.getLocation();
                double time = System.currentTimeMillis() / 1000.0;
                double radius = 1.5;
                
                for (int i = 0; i < 8; i++) {
                    double angle = (time * 2.0) + (i * Math.PI / 4);
                    double x = loc.getX() + Math.cos(angle) * radius;
                    double y = loc.getY() + 1.0 + Math.sin(time * 3.0) * 0.3;
                    double z = loc.getZ() + Math.sin(angle) * radius;
                    
                    org.bukkit.Location particleLoc = new org.bukkit.Location(loc.getWorld(), x, y, z);
                    
                    // Sadece diğer oyunculara göster (kendine gösterme)
                    for (org.bukkit.entity.Player other : org.bukkit.Bukkit.getOnlinePlayers()) {
                        if (!other.equals(player)) {
                            other.spawnParticle(particleType, particleLoc, 1, 0, 0, 0, 0);
                        }
                    }
                }
            }
        }.runTaskTimer(batteryManager.getPlugin(), 0L, 2L); // Her 2 tick'te bir (0.1 saniye)
    }
    
    /**
     * Partikül tipini batarya isminden belirle
     */
    private org.bukkit.Particle getParticleType(String batteryName) {
        if (batteryName.contains("Ateş") || batteryName.contains("Cehennem") || batteryName.contains("Lava")) {
            return org.bukkit.Particle.FLAME;
        } else if (batteryName.contains("Yıldırım") || batteryName.contains("Elektrik") || batteryName.contains("Şok")) {
            return org.bukkit.Particle.ELECTRIC_SPARK;
        } else if (batteryName.contains("Buz") || batteryName.contains("Kale")) {
            return org.bukkit.Particle.SNOWBALL;
        } else if (batteryName.contains("Zehir") || batteryName.contains("Asit")) {
            return org.bukkit.Particle.DRIP_LAVA;
        } else if (batteryName.contains("Meteor") || batteryName.contains("Kıyamet")) {
            return org.bukkit.Particle.EXPLOSION_LARGE;
        } else if (batteryName.contains("Köprü") || batteryName.contains("Duvar") || batteryName.contains("Kale")) {
            // BLOCK_CRACK yerine VILLAGER_HAPPY kullan (BlockData gerektirmez)
            return org.bukkit.Particle.VILLAGER_HAPPY;
        } else if (batteryName.contains("Can") || batteryName.contains("Yenilenme")) {
            return org.bukkit.Particle.HEART;
        } else if (batteryName.contains("Hız")) {
            return org.bukkit.Particle.CLOUD;
        } else if (batteryName.contains("Hasar")) {
            return org.bukkit.Particle.CRIT;
        } else if (batteryName.contains("Zırh")) {
            return org.bukkit.Particle.TOTEM;
        }
        return org.bukkit.Particle.ENCHANTMENT_TABLE;
    }
    
    /**
     * Partikül efektini durdur
     */
    private void stopBatteryParticles(Player player, int slot) {
        // Task'lar otomatik olarak cancel olacak (hasLoadedBattery kontrolü ile)
    }
    
    /**
     * Mastery mesajlarını yönet (yeni sistem)
     */
    private void handleMasteryMessages(Player player, String trainingKey, NewBatteryData data) {
        if (trainingManager == null) return;
        
        int batteryLevel = data.getBatteryLevel();
        int totalUses = trainingManager.getTotalUses(player.getUniqueId(), trainingKey);
        int previousUses = totalUses - 1;
        
        double currentMultiplier = trainingManager.getMasteryMultiplier(player.getUniqueId(), trainingKey, batteryLevel);
        double previousMultiplier = calculateNextPower(previousUses, batteryLevel);
        
        // Önemli geçişleri kontrol et
        boolean justReachedFullPower = (previousUses < 5 && totalUses >= 5);
        boolean justStartedMastery = (previousUses <= 20 && totalUses > 20);
        boolean justReachedMaxPower = (previousUses < 30 && totalUses >= 30);
        
        // Her kullanımda güç bilgisi
        if (totalUses <= 5) {
            player.sendMessage("§e⚡ Güç Artışı: §7%" + String.format("%.0f", previousMultiplier * 100) 
                + " §a→ §e%" + String.format("%.0f", currentMultiplier * 100) 
                + " §7(+" + String.format("%.0f", (currentMultiplier - previousMultiplier) * 100) + "%)");
        }
        
        // Tam güce ulaşma
        if (justReachedFullPower) {
            @SuppressWarnings("deprecation")
            String title = "§a§lTAM GÜÇ!";
            @SuppressWarnings("deprecation")
            String subtitle = "§e%100 Güce Ulaştın!";
            player.sendTitle(title, subtitle, 10, 70, 20);
            
            player.sendMessage("§a§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("§e§l★★★ TAM GÜÇ ULAŞILDI! ★★★");
            player.sendMessage("§7" + data.getBatteryName() + " §aartık tam gücünde!");
            player.sendMessage("§720 kullanımda §6§lMASTERY §7sistemine erişebilirsin.");
            player.sendMessage("§7Mastery ile §d%150 güce §7kadar çıkabilirsin!");
            player.sendMessage("§a§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            player.getWorld().spawnParticle(org.bukkit.Particle.TOTEM, player.getLocation().add(0, 1, 0), 50, 0.5, 0.5, 0.5, 0.1);
        }
        
        // Mastery başlangıcı
        if (justStartedMastery) {
            @SuppressWarnings("deprecation")
            String title = "§6§lMASTERY AÇILDI!";
            @SuppressWarnings("deprecation")
            String subtitle = "§eÜstün Güç Sistemi Aktif!";
            player.sendTitle(title, subtitle, 10, 70, 20);
            
            player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("§e§l⭐⭐⭐ MASTERY AÇILDI! ⭐⭐⭐");
            player.sendMessage("§7Artık §6%100'den fazla güç §7kullanabilirsin!");
            player.sendMessage("§730 kullanımda §d%150 maksimum güce §7ulaşacaksın.");
            player.sendMessage("§7Mevcut Güç: §6%" + String.format("%.0f", currentMultiplier * 100));
            player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
            player.getWorld().spawnParticle(org.bukkit.Particle.END_ROD, player.getLocation().add(0, 1, 0), 100, 0.5, 0.5, 0.5, 0.1);
        }
        
        // Maksimum güce ulaşma
        if (justReachedMaxPower) {
            @SuppressWarnings("deprecation")
            String title = "§d§lMAKSİMUM GÜÇ!";
            @SuppressWarnings("deprecation")
            String subtitle = "§5%150 Güce Ulaştın!";
            player.sendTitle(title, subtitle, 10, 70, 20);
            
            player.sendMessage("§d§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("§5§l★★★★ MAKSİMUM GÜÇ! ★★★★");
            player.sendMessage("§7" + data.getBatteryName() + " §dmaksimum gücüne ulaştı!");
            player.sendMessage("§7Güç: §d%150 §7(Tüm zamanların en yükseği!)");
            player.sendMessage("§7Toplam Kullanım: §e" + totalUses);
            player.sendMessage("§d§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.5f);
            player.getWorld().spawnParticle(org.bukkit.Particle.DRAGON_BREATH, player.getLocation().add(0, 1, 0), 150, 0.5, 1.0, 0.5, 0.1);
        }
        
        // Mastery ilerlemesi (21-29 arası)
        if (totalUses > 20 && totalUses < 30 && !justStartedMastery) {
            player.sendMessage("§6⭐ Mastery İlerlemesi: §e%" + String.format("%.0f", currentMultiplier * 100) 
                + " §7(" + totalUses + "/30)");
        }
    }
    
    /**
     * Bataryayı iptal etme (boşaltma)
     */
    private void dischargeBattery(Player player, int slot) {
        NewBatteryData data = batteryManager.getLoadedBattery(player, slot);
        if (data == null) return;
        
        // ✅ YENİ PARTİKÜL SİSTEMİ: Partikül efektini durdur
        if (particleManager != null) {
            particleManager.stopBatteryParticles(player, slot);
        } else {
            stopBatteryParticles(player, slot);
        }
        
        batteryManager.removeBattery(player, slot);
        player.sendMessage("§eBatarya iptal edildi: " + data.getBatteryName());
    }
}

