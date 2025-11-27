package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Clan;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BatteryManager {
    
    private final Main plugin;
    // Oyuncu UUID -> (Slot Numarası -> Batarya Bilgisi)
    private final Map<UUID, Map<Integer, BatteryData>> loadedBatteries;
    // Barrier bloklarını takip etmek için (Location -> Material) - Ozon Kalkanı ve Enerji Duvarı için
    private final Map<Location, Material> temporaryBarriers;
    // Batarya aktivasyon zamanı takibi (UUID -> (Slot -> ActivationTime)) - İptal edilemez süre için
    private final Map<UUID, Map<Integer, Long>> batteryActivationTimes;
    
    /**
     * Batarya veri sınıfı - tip ve ek bilgileri tutar
     */
    public static class BatteryData {
        private final String type;
        private final Material fuel;
        private final int alchemyLevel;
        private final boolean hasAmplifier;
        private final double trainingMultiplier;
        private final boolean isRedDiamond;
        private final boolean isDarkMatter;
        
        public BatteryData(String type, Material fuel, int alchemyLevel, boolean hasAmplifier, 
                          double trainingMultiplier, boolean isRedDiamond, boolean isDarkMatter) {
            this.type = type;
            this.fuel = fuel;
            this.alchemyLevel = alchemyLevel;
            this.hasAmplifier = hasAmplifier;
            this.trainingMultiplier = trainingMultiplier;
            this.isRedDiamond = isRedDiamond;
            this.isDarkMatter = isDarkMatter;
        }
        
        public String getType() { return type; }
        public Material getFuel() { return fuel; }
        public int getAlchemyLevel() { return alchemyLevel; }
        public boolean hasAmplifier() { return hasAmplifier; }
        public double getTrainingMultiplier() { return trainingMultiplier; }
        public boolean isRedDiamond() { return isRedDiamond; }
        public boolean isDarkMatter() { return isDarkMatter; }
    }
    
    public BatteryManager(Main plugin) {
        this.plugin = plugin;
        this.loadedBatteries = new HashMap<>();
        this.temporaryBarriers = new HashMap<>();
        this.batteryActivationTimes = new HashMap<>();
        if (plugin != null) {
            startInfoTask(); // Bilgi mesajı döngüsünü başlat
        }
    }
    
    /**
     * Bataryayı slota yükle
     */
    public void loadBattery(Player player, int slot, BatteryData data) {
        loadedBatteries.putIfAbsent(player.getUniqueId(), new HashMap<>());
        loadedBatteries.get(player.getUniqueId()).put(slot, data);
        
        // Aktivasyon zamanını kaydet (yükleme = aktivasyon)
        batteryActivationTimes.putIfAbsent(player.getUniqueId(), new HashMap<>());
        batteryActivationTimes.get(player.getUniqueId()).put(slot, System.currentTimeMillis());
        
        player.sendMessage(ChatColor.GREEN + "⚡ " + data.getType() + " " + (slot + 1) + ". slota yüklendi!");
        player.sendMessage(ChatColor.GRAY + "Ateşlemek için SOL, iptal için SAĞ tıkla.");
    }
    
    /**
     * Batarya yeni aktif edildi mi? (2 saniye içinde)
     */
    public boolean isBatteryRecentlyActivated(Player player, int slot) {
        if (!batteryActivationTimes.containsKey(player.getUniqueId())) return false;
        Map<Integer, Long> slotTimes = batteryActivationTimes.get(player.getUniqueId());
        if (!slotTimes.containsKey(slot)) return false;
        
        long activationTime = slotTimes.get(slot);
        long currentTime = System.currentTimeMillis();
        return (currentTime - activationTime) < 2000; // 2 saniye
    }
    
    /**
     * Slotta yüklü batarya var mı?
     */
    public boolean hasLoadedBattery(Player player, int slot) {
        return loadedBatteries.containsKey(player.getUniqueId()) && 
               loadedBatteries.get(player.getUniqueId()).containsKey(slot);
    }
    
    /**
     * Yüklü bataryanın verisini al
     */
    public BatteryData getLoadedBattery(Player player, int slot) {
        if (!hasLoadedBattery(player, slot)) return null;
        return loadedBatteries.get(player.getUniqueId()).get(slot);
    }
    
    /**
     * Bataryayı kullan/sil
     */
    public void removeBattery(Player player, int slot) {
        if (loadedBatteries.containsKey(player.getUniqueId())) {
            loadedBatteries.get(player.getUniqueId()).remove(slot);
            // Eğer oyuncunun başka bataryası kalmadıysa map'ten temizle
            if (loadedBatteries.get(player.getUniqueId()).isEmpty()) {
                loadedBatteries.remove(player.getUniqueId());
            }
        }
        // Aktivasyon zamanını da temizle
        if (batteryActivationTimes.containsKey(player.getUniqueId())) {
            batteryActivationTimes.get(player.getUniqueId()).remove(slot);
            if (batteryActivationTimes.get(player.getUniqueId()).isEmpty()) {
                batteryActivationTimes.remove(player.getUniqueId());
            }
        }
    }
    
    /**
     * Oyuncunun tüm yüklü bataryalarını temizle (logout vb. durumlar için)
     */
    public void clearBatteries(Player player) {
        loadedBatteries.remove(player.getUniqueId());
    }
    
    /**
     * Sürekli çalışan ve oyuncuya görsel bildirim veren görev
     */
    private void startInfoTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : loadedBatteries.keySet()) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        int currentSlot = player.getInventory().getHeldItemSlot();
                        
                        if (hasLoadedBattery(player, currentSlot)) {
                            BatteryData data = getLoadedBattery(player, currentSlot);
                            if (data != null) {
                                // Ekranın üstünde (Action Bar) uyarı mesajı
                                String message = ChatColor.RED + "🔴 YÜKLÜ: " + ChatColor.GOLD + data.getType() + 
                                               ChatColor.GRAY + " [Slot: " + (currentSlot + 1) + "]";
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Her saniye (20 tick) çalışır
    }

    // 1. ATEŞ TOPU (Geliştirilmiş)
    public void fireMagmaBattery(Player p, Material fuel, int alchemyLevel, boolean hasAmplifier) {
        fireMagmaBattery(p, fuel, alchemyLevel, hasAmplifier, 1.0);
    }
    
    public void fireMagmaBattery(Player p, Material fuel, int alchemyLevel, boolean hasAmplifier, double trainingMultiplier) {
        int count;
        if (fuel == Material.DIAMOND) count = 5;
        else if (ItemManager.RED_DIAMOND != null && 
                 p.getInventory().getItemInMainHand().equals(ItemManager.RED_DIAMOND)) {
            count = 20;
        } else if (ItemManager.DARK_MATTER != null && 
                   p.getInventory().getItemInMainHand().equals(ItemManager.DARK_MATTER)) {
            count = 50;
        } else {
            count = 2;
        }
        
        // Simya Kulesi seviyesine göre güç artışı: Seviye 1 = %10, Seviye 5 = %50
        if (alchemyLevel > 0) {
            double multiplier = 1.0 + (alchemyLevel * 0.1); // Seviye 1: 1.1x, Seviye 5: 1.5x
            count = (int) (count * multiplier);
        }
        
        // Mastery çarpanı uygula (0.2 = antrenman, 1.0 = normal, 1.2-1.4 = mastery bonus)
        count = (int) (count * trainingMultiplier);
        if (count < 1) count = 1; // En az 1 ateş topu
        
        @SuppressWarnings("unused")
        float size = hasAmplifier ? 2.0f : 1.0f;
        float yield = hasAmplifier ? 4.0f : 2.0f; // Alev Amplifikatörü ile çap 2 katına çıkar
        yield = (float) (yield * trainingMultiplier); // Mastery çarpanı yield'e de uygulanır
        
        // Ateş toplarını sırayla at (aynı anda değil, delay ile)
        final int finalCount = count;
        final float finalYield = yield;
        final boolean finalIsIncendiary = (alchemyLevel >= 5 && trainingMultiplier >= 1.0);
        
        new BukkitRunnable() {
            int fired = 0;
            
            @Override
            public void run() {
                if (fired >= finalCount || !p.isOnline()) {
                    cancel();
                    return;
                }
                
                // Oyuncunun 1 blok önünden başlat (içinde patlamasın)
                Location spawnLoc = p.getEyeLocation().clone();
                Vector direction = p.getLocation().getDirection().normalize(); // Normalize et
                spawnLoc.add(direction.multiply(1.5)); // 1.5 blok önünden başlat (daha güvenli)
                
                // Ateş topunu spawn et
                Fireball fb = spawnLoc.getWorld().spawn(spawnLoc, Fireball.class);
                fb.setVelocity(direction.multiply(1.5));
                fb.setYield(finalYield);
                fb.setShooter(p);
                
                // Seviye 5'te yanma etkisi ekle (antrenman modunda yok)
                if (finalIsIncendiary) {
                    fb.setIsIncendiary(true);
                }
                
                fired++;
            }
        }.runTaskTimer(plugin, 0L, 2L); // Her 2 tick'te bir ateş topu (0.1 saniye aralık)
        
        // Mastery mesajı (antrenman modu veya mastery bonus)
        String masteryMsg = "";
        if (trainingMultiplier < 1.0) {
            masteryMsg = " §7[Antrenman Modu]";
        } else if (trainingMultiplier > 1.0) {
            int bonusPercent = (int) ((trainingMultiplier - 1.0) * 100);
            masteryMsg = " §a[Mastery +%" + bonusPercent + "]";
        }
        p.sendMessage("§6Ateş topları fırlatıldı! (" + count + " adet)" + (alchemyLevel > 0 ? " [Simya Kulesi Seviye " + alchemyLevel + "]" : "") + masteryMsg);
    }

    // 2. YILDIRIM
    public void fireLightningBattery(Player p) {
        Location target = p.getTargetBlock(null, 50).getLocation();
        p.getWorld().strikeLightning(target);
        p.sendMessage("§eYıldırım düştü!");
    }

    // 3. KARA DELİK
    public void fireBlackHole(Player p) {
        Location target = p.getTargetBlock(null, 30).getLocation();
        p.getWorld().createExplosion(target, 0F);
        p.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_HUGE, target, 1);
        for (Entity e : target.getWorld().getNearbyEntities(target, 15, 15, 15)) {
            if (e instanceof LivingEntity && e != p) {
                Vector dir = target.toVector().subtract(e.getLocation().toVector()).normalize().multiply(1.5);
                e.setVelocity(dir);
            }
        }
        p.sendMessage("§5Kara Delik aktif!");
    }

    // 4. ANLIK KÖPRÜ
    public void createInstantBridge(Player p) {
        Location start = p.getLocation().clone().subtract(0, 1, 0);
        Vector dir = p.getLocation().getDirection().setY(0).normalize();
        int placedBlocks = 0;
        
        for (int i = 1; i <= 15; i++) {
            Location point = start.clone().add(dir.clone().multiply(i));
            // Yükseklik sınırı kontrolü
            if (point.getY() < -64 || point.getY() > 319) continue;
            
            // Eğer önünde blok varsa (AIR değilse), o bloğu yok etme, es geç
            if (point.getBlock().getType() == Material.AIR) {
                point.getBlock().setType(Material.PACKED_ICE);
                placedBlocks++;
            }
            // Eğer blok varsa, continue ile es geç (yok etme)
        }
        
        if (placedBlocks > 0) {
            p.sendMessage("§bBuz Köprüsü kuruldu! (" + placedBlocks + " blok)");
        } else {
            p.sendMessage("§cKöprü kurulamadı! Önünde engel var.");
        }
    }

    // 5. SIĞINAK KÜPÜ
    public void createInstantBunker(Player p) {
        Location center = p.getLocation().clone();
        int r = 2;
        int placedBlocks = 0;
        
        for (int x = -r; x <= r; x++) {
            for (int y = 0; y <= 3; y++) {
                for (int z = -r; z <= r; z++) {
                    if (Math.abs(x) == r || Math.abs(z) == r || y == 3 || y == 0) {
                        Location blockLoc = center.clone().add(x, y, z);
                        // Yükseklik sınırı kontrolü
                        if (blockLoc.getY() < -64 || blockLoc.getY() > 319) continue;
                        
                        Block b = blockLoc.getBlock();
                        // Eğer önünde blok varsa (AIR değilse), o bloğu yok etme, es geç
                        if (b.getType() == Material.AIR) {
                            b.setType(Material.COBBLESTONE);
                            placedBlocks++;
                        }
                        // Eğer blok varsa, continue ile es geç (yok etme)
                    }
                }
            }
        }
        
        // Sadece yeterli blok yerleştirildiyse teleport et
        if (placedBlocks > 0) {
            p.teleport(center.clone().add(0, 1, 0));
            p.sendMessage("§7Sığınak oluşturuldu! (" + placedBlocks + " blok)");
        } else {
            p.sendMessage("§cSığınak oluşturulamadı! Yeterli boş alan yok.");
        }
    }

    // 6. YERÇEKİMİ ÇAPASI (ANTI-AIR)
    public void fireGravityAnchor(Player p) {
        p.sendMessage("§5Yerçekimi Çapası Aktif!");
        for (Entity e : p.getNearbyEntities(50, 100, 50)) {
            if (e instanceof Player && ((Player) e).isGliding()) {
                e.setVelocity(new Vector(0, -3, 0));
                ((Player) e).setGliding(false);
                e.sendMessage("§c§lYERÇEKİMİ ÇAPASINA YAKALANDIN!");
            }
        }
    }

    // 7. TOPRAK SURU (Savunma)
    public void createEarthWall(Player p, Material material) {
        Location start = p.getLocation().clone().add(p.getLocation().getDirection().setY(0).normalize().multiply(2));
        boolean isTitanium = ItemManager.TITANIUM_INGOT != null && 
                             ItemManager.isCustomItem(p.getInventory().getItemInMainHand(), "TITANIUM_INGOT");
        boolean isAdamantite = ItemManager.ADAMANTITE != null && 
                               ItemManager.isCustomItem(p.getInventory().getItemInMainHand(), "ADAMANTITE");
        
        int height = isTitanium ? 5 : 3;
        Material wallMat = Material.COBBLESTONE;
        
        List<Location> barrierLocations = new ArrayList<>();
        
        if (isAdamantite) {
            // Adamantite ile şeffaf, içinden ok geçmeyen enerji kalkanı
            wallMat = Material.BARRIER;
            height = 4;
            p.sendMessage("§5Adamantite Enerji Kalkanı oluşturuldu!");
        } else if (isTitanium) {
            wallMat = Material.IRON_BLOCK;
        }
        
        int placedBlocks = 0;
        
        for (int y = 0; y < height; y++) {
            for (int x = -1; x <= 1; x++) {
                Location blockLoc = start.clone().add(x, y, 0);
                // Yükseklik sınırı kontrolü
                if (blockLoc.getY() < -64 || blockLoc.getY() > 319) continue;
                
                // Eğer önünde blok varsa (AIR değilse), o bloğu yok etme, es geç
                if (blockLoc.getBlock().getType() == Material.AIR) {
                    Material originalType = blockLoc.getBlock().getType();
                    blockLoc.getBlock().setType(wallMat);
                    placedBlocks++;
                    
                    if (isAdamantite) {
                        // Barrier bloklarını kaydet (otomatik silme için)
                        temporaryBarriers.put(blockLoc.clone(), originalType);
                        barrierLocations.add(blockLoc.clone());
                        // Enerji efekti
                        p.getWorld().spawnParticle(org.bukkit.Particle.ELECTRIC_SPARK, blockLoc.add(0.5, 0.5, 0.5), 3);
                    }
                }
                // Eğer blok varsa, continue ile es geç (yok etme)
            }
        }
        
        // Adamantite kullanıldıysa 15 saniye sonra barrier bloklarını sil
        if (isAdamantite && !barrierLocations.isEmpty()) {
            scheduleBarrierRemoval(barrierLocations, 15 * 20); // 15 saniye = 300 tick
        }
        if (!isAdamantite) {
            if (placedBlocks > 0) {
                p.sendMessage("§7Toprak Suru oluşturuldu! (" + placedBlocks + " blok)");
            } else {
                p.sendMessage("§cToprak Suru oluşturulamadı! Önünde engel var.");
            }
        }
    }

    // 8. MANYETİK BOZUCU (Utility)
    public void fireMagneticDisruptor(Player p) {
        p.sendMessage("§5Manyetik Bozucu Aktif!");
        for (Entity e : p.getNearbyEntities(20, 20, 20)) {
            if (e instanceof Player && e != p) {
                Player target = (Player) e;
                ItemStack mainHand = target.getInventory().getItemInMainHand();
                if (mainHand != null && mainHand.getType() != Material.AIR) {
                    target.getWorld().dropItemNaturally(target.getLocation(), mainHand.clone());
                    target.getInventory().setItemInMainHand(null);
                    target.sendMessage("§c§lSİLAHIN DÜŞTÜ!");
                }
            }
        }
    }

    // 9. SİSMİK ÇEKİÇ (Felaket Mücadele)
    private me.mami.stratocraft.manager.DisasterManager disasterManager;
    
    public void setDisasterManager(me.mami.stratocraft.manager.DisasterManager dm) {
        this.disasterManager = dm;
    }
    
    public void fireSeismicHammer(Player p) {
        Location target = p.getTargetBlock(null, 30).getLocation();
        p.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_LARGE, target, 5);
        p.sendMessage("§6Sismik Çekiç Aktif! Yer altı titreşimleri gönderildi!");
        // Hiçlik Solucanı için titreşim sinyali
        if (disasterManager != null) {
            disasterManager.forceWormSurface(target);
        }
    }

    // 10. OZON KALKANI (Güneş Fırtınası Koruma)
    public void activateOzoneShield(Player p, Location center) {
        int radius = 15;
        List<Location> barrierLocations = new ArrayList<>();
        int placedBlocks = 0;
        
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x*x + z*z <= radius*radius) {
                    Location loc = center.clone().add(x, 0, z);
                    // Yükseklik sınırı kontrolü
                    if (loc.getY() < -64 || loc.getY() > 319) continue;
                    
                    // Eğer önünde blok varsa (AIR değilse), o bloğu yok etme, es geç
                    if (loc.getBlock().getType() == Material.AIR) {
                        Material originalType = loc.getBlock().getType();
                        loc.getBlock().setType(Material.BARRIER);
                        temporaryBarriers.put(loc.clone(), originalType);
                        barrierLocations.add(loc.clone());
                        p.getWorld().spawnParticle(org.bukkit.Particle.END_ROD, loc, 1);
                        placedBlocks++;
                    }
                    // Eğer blok varsa, continue ile es geç (yok etme)
                }
            }
        }
        
        // 20 saniye sonra barrier bloklarını sil
        if (!barrierLocations.isEmpty()) {
            scheduleBarrierRemoval(barrierLocations, 20 * 20); // 20 saniye = 400 tick
        }
        
        if (placedBlocks > 0) {
            p.sendMessage("§bOzon Kalkanı aktif! Güneş Fırtınası koruması sağlandı. (" + placedBlocks + " blok, 20 saniye)");
        } else {
            p.sendMessage("§cOzon Kalkanı oluşturulamadı! Yeterli boş alan yok.");
        }
    }

    // 11. ENERJİ DUVARI (Gelişmiş Savunma)
    public void createEnergyWall(Player p) {
        Location start = p.getLocation().clone().add(p.getLocation().getDirection().setY(0).normalize().multiply(2));
        List<Location> barrierLocations = new ArrayList<>();
        int placedBlocks = 0;
        
        for (int y = 0; y < 5; y++) {
            for (int x = -2; x <= 2; x++) {
                Location loc = start.clone().add(x, y, 0);
                // Yükseklik sınırı kontrolü
                if (loc.getY() < -64 || loc.getY() > 319) continue;
                
                // Eğer önünde blok varsa (AIR değilse), o bloğu yok etme, es geç
                if (loc.getBlock().getType() == Material.AIR) {
                    Material originalType = loc.getBlock().getType();
                    loc.getBlock().setType(Material.BARRIER);
                    temporaryBarriers.put(loc.clone(), originalType);
                    barrierLocations.add(loc.clone());
                    p.getWorld().spawnParticle(org.bukkit.Particle.ELECTRIC_SPARK, loc, 3);
                    placedBlocks++;
                }
                // Eğer blok varsa, continue ile es geç (yok etme)
            }
        }
        
        // 15 saniye sonra barrier bloklarını sil
        if (!barrierLocations.isEmpty()) {
            scheduleBarrierRemoval(barrierLocations, 15 * 20); // 15 saniye = 300 tick
        }
        
        if (placedBlocks > 0) {
            p.sendMessage("§bEnerji Duvarı oluşturuldu! (" + placedBlocks + " blok, 15 saniye)");
        } else {
            p.sendMessage("§cEnerji Duvarı oluşturulamadı! Önünde engel var.");
        }
    }

    // 12. LAV HENDEKÇİSİ (Alan Savunması)
    public void createLavaTrench(Player p, TerritoryManager territoryManager) {
        Location start = p.getLocation().clone().add(p.getLocation().getDirection().setY(0).normalize().multiply(3));
        
        // Territory kontrolü
        Clan owner = territoryManager.getTerritoryOwner(start);
        Clan playerClan = territoryManager.getClanManager().getClanByPlayer(p.getUniqueId());
        
        // Eğer başkasının bölgesindeyse ve savaş durumunda değilse engelle
        if (owner != null && playerClan != null && !owner.equals(playerClan)) {
            // Savaş kontrolü - SiegeManager'dan kontrol et
            me.mami.stratocraft.Main plugin = me.mami.stratocraft.Main.getInstance();
            if (plugin != null && plugin.getSiegeManager() != null) {
                me.mami.stratocraft.manager.SiegeManager siegeManager = plugin.getSiegeManager();
                // Savaş durumunda değilse engelle
                if (!siegeManager.isUnderSiege(owner)) {
                    p.sendMessage("§cLav Hendekçisi sadece kendi bölgende veya savaş durumunda kullanılabilir!");
                    return;
                }
            } else {
                // SiegeManager yoksa engelle
                p.sendMessage("§cLav Hendekçisi sadece kendi bölgende kullanılabilir!");
                return;
            }
        }
        
        int placedBlocks = 0;
        
        for (int i = 0; i < 10; i++) {
            Location loc = start.clone().add(i, -1, 0);
            // Yükseklik sınırı kontrolü
            if (loc.getY() < -64 || loc.getY() > 319) continue;
            
            // Eğer önünde blok varsa (LAVA değilse ve AIR değilse), o bloğu yok etme, es geç
            // Sadece AIR veya su gibi sıvı blokların üzerine lav koyabilir
            Material currentType = loc.getBlock().getType();
            if (currentType == Material.AIR || currentType == Material.WATER || currentType == Material.LAVA) {
                if (currentType != Material.LAVA) {
                    loc.getBlock().setType(Material.LAVA);
                    placedBlocks++;
                }
            }
            // Eğer solid blok varsa, continue ile es geç (yok etme)
        }
        
        if (placedBlocks > 0) {
            p.sendMessage("§cLav Hendekçisi kuruldu! (" + placedBlocks + " blok)");
        } else {
            p.sendMessage("§cLav Hendekçisi kurulamadı! Önünde engel var.");
        }
    }
    
    /**
     * Barrier bloklarını belirli bir süre sonra otomatik olarak sil
     */
    private void scheduleBarrierRemoval(List<Location> locations, long delayTicks) {
        if (plugin == null || locations.isEmpty()) return;
        
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Location loc : locations) {
                    if (loc.getBlock().getType() == Material.BARRIER) {
                        Material originalType = temporaryBarriers.getOrDefault(loc, Material.AIR);
                        loc.getBlock().setType(originalType);
                        temporaryBarriers.remove(loc);
                    }
                }
            }
        }.runTaskLater(plugin, delayTicks);
    }
    
    /**
     * Sunucu kapanırken (onDisable) çağırılmalı.
     * Aktif olan tüm geçici blokları temizler.
     * Bu, sunucu restart durumunda barrier bloklarının kalıcı kalmasını önler.
     */
    public void shutdown() {
        // Hafızadaki tüm geçici bariyerleri kaldır
        for (Map.Entry<Location, Material> entry : temporaryBarriers.entrySet()) {
            Location loc = entry.getKey();
            Material original = entry.getValue();
            
            // Null kontrolü ve world kontrolü
            if (loc != null && loc.getWorld() != null) {
                try {
                    // Eğer hala barrier ise, orijinal haline döndür
                    if (loc.getBlock().getType() == Material.BARRIER) {
                        loc.getBlock().setType(original);
                    }
                } catch (Exception e) {
                    // World yüklenmemiş olabilir veya chunk yüklenmemiş olabilir
                    // Bu durumda sessizce geç (loglama yapılabilir ama şimdilik skip)
                }
            }
        }
        
        temporaryBarriers.clear();
        
        // Yüklü batarya verilerini temizle (sunucu kapanırken zaten gereksiz)
        loadedBatteries.clear();
    }
}

