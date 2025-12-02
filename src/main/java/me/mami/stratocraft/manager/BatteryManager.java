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
import org.bukkit.block.BlockFace;
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
    // Barrier bloklarını takip etmek için (Location -> Material) - Ozon Kalkanı ve
    // Enerji Duvarı için
    private final Map<Location, Material> temporaryBarriers;
    // Batarya aktivasyon zamanı takibi (UUID -> (Slot -> ActivationTime)) - İptal
    // edilemez süre için
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
        private final int batteryLevel; // Seviye (1-5)

        public BatteryData(String type, Material fuel, int alchemyLevel, boolean hasAmplifier,
                double trainingMultiplier, boolean isRedDiamond, boolean isDarkMatter) {
            this(type, fuel, alchemyLevel, hasAmplifier, trainingMultiplier, isRedDiamond, isDarkMatter, 1);
        }
        
        public BatteryData(String type, Material fuel, int alchemyLevel, boolean hasAmplifier,
                double trainingMultiplier, boolean isRedDiamond, boolean isDarkMatter, int batteryLevel) {
            this.type = type;
            this.fuel = fuel;
            this.alchemyLevel = alchemyLevel;
            this.hasAmplifier = hasAmplifier;
            this.trainingMultiplier = trainingMultiplier;
            this.isRedDiamond = isRedDiamond;
            this.isDarkMatter = isDarkMatter;
            this.batteryLevel = batteryLevel;
        }

        public String getType() {
            return type;
        }

        public Material getFuel() {
            return fuel;
        }

        public int getAlchemyLevel() {
            return alchemyLevel;
        }

        public boolean hasAmplifier() {
            return hasAmplifier;
        }

        public double getTrainingMultiplier() {
            return trainingMultiplier;
        }

        public boolean isRedDiamond() {
            return isRedDiamond;
        }

        public boolean isDarkMatter() {
            return isDarkMatter;
        }
        
        public int getBatteryLevel() {
            return batteryLevel;
        }
    }

    // Partikül animasyon açıları (her oyuncu için ayrı)
    private final Map<UUID, Double> particleAngles = new HashMap<>();

    public BatteryManager(Main plugin) {
        this.plugin = plugin;
        this.loadedBatteries = new HashMap<>();
        this.temporaryBarriers = new HashMap<>();
        this.batteryActivationTimes = new HashMap<>();
        if (plugin != null) {
            startInfoTask(); // Bilgi mesajı döngüsünü başlat
            startParticleTask(); // Partikül döngüsünü başlat
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
        if (!batteryActivationTimes.containsKey(player.getUniqueId()))
            return false;
        Map<Integer, Long> slotTimes = batteryActivationTimes.get(player.getUniqueId());
        if (!slotTimes.containsKey(slot))
            return false;

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
        if (!hasLoadedBattery(player, slot))
            return null;
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
        particleAngles.remove(player.getUniqueId()); // Partikül açısını da temizle
        batteryActivationTimes.remove(player.getUniqueId()); // Aktivasyon zamanlarını da temizle
    }
    
    /**
     * Oyuncunun herhangi bir yüklü bataryası var mı?
     */
    public boolean hasAnyLoadedBattery(Player player) {
        if (!loadedBatteries.containsKey(player.getUniqueId())) {
            return false;
        }
        Map<Integer, BatteryData> playerBatteries = loadedBatteries.get(player.getUniqueId());
        return playerBatteries != null && !playerBatteries.isEmpty();
    }
    
    /**
     * Oyuncunun tüm yüklü bataryalarını al
     */
    public Map<Integer, BatteryData> getAllLoadedBatteries(Player player) {
        if (!loadedBatteries.containsKey(player.getUniqueId())) {
            return new HashMap<>();
        }
        Map<Integer, BatteryData> playerBatteries = loadedBatteries.get(player.getUniqueId());
        return playerBatteries != null ? new HashMap<>(playerBatteries) : new HashMap<>();
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
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                        TextComponent.fromLegacyText(message));
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Her saniye (20 tick) çalışır
    }

    /**
     * Aktif bataryalar için partikül gösterimi (diğer oyunculara görünür)
     */
    private void startParticleTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : loadedBatteries.keySet()) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null || !player.isOnline())
                        continue;

                    Map<Integer, BatteryData> playerBatteries = loadedBatteries.get(uuid);
                    if (playerBatteries == null || playerBatteries.isEmpty())
                        continue;

                    // Açıyı güncelle (yavaş dönüş için)
                    double currentAngle = particleAngles.getOrDefault(uuid, 0.0);
                    currentAngle += 0.1; // Her tick'te 0.1 radyan artır (yavaş dönüş)
                    if (currentAngle >= 2 * Math.PI) {
                        currentAngle = 0.0; // 360 derece = 0
                    }
                    particleAngles.put(uuid, currentAngle);

                    Location playerLoc = player.getLocation();
                    if (playerLoc == null || playerLoc.getWorld() == null)
                        continue; // Güvenlik kontrolü

                    double radius = 1.5; // Oyuncunun etrafında 1.5 blok yarıçap

                    // Her slot için partikül göster
                    int slotIndex = 0;
                    int batteryCount = playerBatteries.size();
                    if (batteryCount == 0)
                        continue; // Güvenlik kontrolü

                    for (Map.Entry<Integer, BatteryData> entry : playerBatteries.entrySet()) {
                        int slot = entry.getKey();
                        BatteryData battery = entry.getValue();

                        // Null kontrolü
                        if (battery == null) {
                            slotIndex++;
                            continue;
                        }

                        // Slot'a göre renk belirle
                        org.bukkit.Color particleColor = getSlotColor(slotIndex);

                        // Yakıt tipine göre partikül miktarı (custom item desteği ile)
                        int particleCount = getParticleCountByBatteryData(battery);

                        // Partikül pozisyonu (oyuncunun etrafında dönen)
                        double angle = currentAngle + (slotIndex * (2 * Math.PI / batteryCount));
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;
                        double y = 0.5 + (slotIndex * 0.3); // Her slot için biraz yukarı

                        Location particleLoc = playerLoc.clone().add(x, y, z);

                        // Tüm oyunculara partikül göster
                        for (Player viewer : Bukkit.getOnlinePlayers()) {
                            if (viewer == null || !viewer.isOnline())
                                continue;
                            if (viewer.getWorld() == null || viewer.getWorld() != player.getWorld())
                                continue;

                            Location viewerLoc = viewer.getLocation();
                            if (viewerLoc == null)
                                continue;
                            if (viewerLoc.distance(playerLoc) > 32)
                                continue; // 32 blok mesafe limiti

                            try {
                                viewer.spawnParticle(
                                        org.bukkit.Particle.REDSTONE,
                                        particleLoc,
                                        particleCount,
                                        0.1, 0.1, 0.1, 0,
                                        new org.bukkit.Particle.DustOptions(particleColor, 1.0f));
                            } catch (Exception e) {
                                // Partikül spawn hatası (oyuncu çok uzakta veya dünya yüklenmemiş)
                                // Sessizce atla
                            }
                        }

                        slotIndex++;
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 2L); // Her 2 tick'te bir (çok hızlı dönüş için)
    }

    /**
     * Slot numarasına göre renk döndür
     */
    private org.bukkit.Color getSlotColor(int slotIndex) {
        switch (slotIndex % 9) {
            case 0:
                return org.bukkit.Color.RED; // Kırmızı
            case 1:
                return org.bukkit.Color.fromRGB(255, 165, 0); // Turuncu
            case 2:
                return org.bukkit.Color.YELLOW; // Sarı
            case 3:
                return org.bukkit.Color.LIME; // Yeşil
            case 4:
                return org.bukkit.Color.BLUE; // Mavi
            case 5:
                return org.bukkit.Color.PURPLE; // Mor
            case 6:
                return org.bukkit.Color.fromRGB(255, 192, 203); // Pembe
            case 7:
                return org.bukkit.Color.WHITE; // Beyaz
            case 8:
                return org.bukkit.Color.AQUA; // Cyan
            default:
                return org.bukkit.Color.RED;
        }
    }

    /**
     * Yakıt tipine göre partikül miktarı döndür
     */
    private int getParticleCountByFuel(Material fuel) {
        if (fuel == null)
            return 12; // Varsayılan

        switch (fuel) {
            case DIAMOND:
                return 20; // Elmas = çok partikül
            case EMERALD:
                return 15; // Zümrüt = orta
            case IRON_INGOT:
                return 10; // Demir = az
            default:
                // Material ismi kontrolü (custom item'lar için)
                String fuelName = fuel.name();
                if (fuelName.contains("TITANIUM") || fuelName.contains("ANCIENT_DEBRIS")) {
                    return 25; // Titanyum = en çok
                }
                return 12; // Varsayılan
        }
    }

    /**
     * BatteryData'dan yakıt tipine göre partikül miktarı (custom item desteği ile)
     */
    private int getParticleCountByBatteryData(BatteryData battery) {
        if (battery == null)
            return 12;

        Material fuel = battery.getFuel();
        if (fuel == null)
            return 12;

        // Custom item kontrolü (Titanyum, Kızıl Elmas, Karanlık Madde)
        if (battery.isRedDiamond()) {
            return 30; // Kızıl Elmas = çok fazla
        }
        if (battery.isDarkMatter()) {
            return 35; // Karanlık Madde = en fazla
        }

        return getParticleCountByFuel(fuel);
    }

    /**
     * Batarya seviyesini tespit et (blok sayısına göre)
     * @param centerBlock Merkez blok
     * @param blockType Kontrol edilecek blok tipi
     * @return Seviye (1-5), 0 = geçersiz
     */
    public int detectBatteryLevel(Block centerBlock, Material blockType) {
        if (centerBlock.getType() != blockType) return 0;
        
        int count = 1; // Merkez blok
        Block current = centerBlock;
        
        // Yukarı say
        while (current.getRelative(BlockFace.UP).getType() == blockType) {
            current = current.getRelative(BlockFace.UP);
            count++;
            if (count >= 11) break; // Maksimum 11 blok
        }
        
        // Aşağı say
        current = centerBlock;
        while (current.getRelative(BlockFace.DOWN).getType() == blockType) {
            current = current.getRelative(BlockFace.DOWN);
            count++;
            if (count >= 11) break; // Maksimum 11 blok
        }
        
        // Seviye belirleme
        if (count >= 11) {
            // Seviye 5: 11 blok + özel kontrol (alt ve üstte özel bloklar)
            Block bottom = centerBlock;
            while (bottom.getRelative(BlockFace.DOWN).getType() == blockType) {
                bottom = bottom.getRelative(BlockFace.DOWN);
            }
            Block top = centerBlock;
            while (top.getRelative(BlockFace.UP).getType() == blockType) {
                top = top.getRelative(BlockFace.UP);
            }
            // Alt ve üstte özel blok kontrolü (örneğin: altında BEACON, üstünde NETHER_STAR)
            Block belowSpecial = bottom.getRelative(BlockFace.DOWN);
            Block aboveSpecial = top.getRelative(BlockFace.UP);
            if (belowSpecial.getType() == Material.BEACON && 
                (aboveSpecial.getType() == Material.NETHER_STAR || 
                 aboveSpecial.getType() == Material.BEDROCK)) {
                return 5;
            }
            return 4; // 11+ blok ama özel blok yok = Seviye 4
        } else if (count >= 9) {
            return 4;
        } else if (count >= 7) {
            return 3;
        } else if (count >= 5) {
            return 2;
        } else if (count >= 3) {
            return 1;
        }
        return 0;
    }

    // 1. ATEŞ TOPU (Geliştirilmiş - Seviyeli)
    public void fireMagmaBattery(Player p, Material fuel, int alchemyLevel, boolean hasAmplifier) {
        fireMagmaBattery(p, fuel, alchemyLevel, hasAmplifier, 1.0, 1);
    }
    
    public void fireMagmaBattery(Player p, Material fuel, int alchemyLevel, boolean hasAmplifier,
            double trainingMultiplier) {
        fireMagmaBattery(p, fuel, alchemyLevel, hasAmplifier, trainingMultiplier, 1);
    }

    public void fireMagmaBattery(Player p, Material fuel, int alchemyLevel, boolean hasAmplifier,
            double trainingMultiplier, int batteryLevel) {
        // Seviyeye göre temel güç
        int baseCount;
        float baseYield;
        double levelMultiplier;
        
        switch (batteryLevel) {
            case 1:
                baseCount = 2;
                baseYield = 2.0f;
                levelMultiplier = 1.0;
                break;
            case 2:
                baseCount = 5;
                baseYield = 3.0f;
                levelMultiplier = 1.5;
                break;
            case 3:
                baseCount = 15;
                baseYield = 5.0f;
                levelMultiplier = 2.5;
                break;
            case 4:
                baseCount = 40;
                baseYield = 8.0f;
                levelMultiplier = 4.0;
                break;
            case 5:
                baseCount = 100;
                baseYield = 15.0f;
                levelMultiplier = 10.0;
                break;
            default:
                baseCount = 2;
                baseYield = 2.0f;
                levelMultiplier = 1.0;
        }
        
        // Yakıt tipine göre çarpan
        double fuelMultiplier = 1.0;
        if (fuel == Material.DIAMOND) {
            fuelMultiplier = 2.5;
        } else if (ItemManager.RED_DIAMOND != null &&
                p.getInventory().getItemInMainHand().equals(ItemManager.RED_DIAMOND)) {
            fuelMultiplier = 5.0;
        } else if (ItemManager.DARK_MATTER != null &&
                p.getInventory().getItemInMainHand().equals(ItemManager.DARK_MATTER)) {
            fuelMultiplier = 10.0;
        }
        
        int count = (int) (baseCount * fuelMultiplier * levelMultiplier);

        // Simya Kulesi seviyesine göre güç artışı: Seviye 1 = %10, Seviye 5 = %50
        if (alchemyLevel > 0) {
            double multiplier = 1.0 + (alchemyLevel * 0.1); // Seviye 1: 1.1x, Seviye 5: 1.5x
            count = (int) (count * multiplier);
        }

        // Mastery çarpanı uygula (0.2 = antrenman, 1.0 = normal, 1.2-1.4 = mastery
        // bonus)
        count = (int) (count * trainingMultiplier);
        if (count < 1)
            count = 1; // En az 1 ateş topu

        @SuppressWarnings("unused")
        float size = hasAmplifier ? 2.0f : 1.0f;
        float yield = hasAmplifier ? baseYield * 2.0f : baseYield; // Alev Amplifikatörü ile çap 2 katına çıkar
        yield = (float) (yield * trainingMultiplier * levelMultiplier); // Mastery çarpanı yield'e de uygulanır

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

        String ampMsg = hasAmplifier ? " §c§l[ALEV AMPLİFİKATÖRÜ AKTİF!]" : "";

        String levelMsg = batteryLevel > 1 ? " §6§l[Seviye " + batteryLevel + " Batarya]" : "";
        
        p.sendMessage("§6Ateş topları fırlatıldı! (" + count + " adet)" +
                (alchemyLevel > 0 ? " [Simya Kulesi Seviye " + alchemyLevel + "]" : "") +
                masteryMsg + ampMsg + levelMsg);
        
        // Seviye 5 özel güç: Dağ yıkma
        if (batteryLevel == 5 && fuel == Material.DIAMOND) {
            fireMountainDestroyer(p);
        }
    }
    
    /**
     * Seviye 5 Özel Güç: Dağ Yıkma (Optimize)
     */
    private void fireMountainDestroyer(Player p) {
        Block targetBlock = p.getTargetBlock(null, 100);
        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            return;
        }
        
        Location center = targetBlock.getLocation();
        int radius = 30; // 30 blok yarıçap
        int height = 50; // 50 blok yükseklik
        
        p.sendMessage("§c§lDAĞ YIKICI AKTİF! Büyük alan yıkımı başlıyor...");
        
        // Optimize: Chunk kontrolü ve batch işlem
        new BukkitRunnable() {
            int processed = 0;
            final int maxPerTick = 50; // Her tick'te maksimum 50 blok işle
            
            @Override
            public void run() {
                int count = 0;
                for (int x = -radius; x <= radius && count < maxPerTick; x++) {
                    for (int z = -radius; z <= radius && count < maxPerTick; z++) {
                        for (int y = -height/2; y <= height/2 && count < maxPerTick; y++) {
                            Location loc = center.clone().add(x, y, z);
                            
                            // Chunk yüklü mü kontrol et
                            if (!loc.getChunk().isLoaded()) {
                                continue;
                            }
                            
                            double distance = center.distance(loc);
                            if (distance <= radius) {
                                Block block = loc.getBlock();
                                Material type = block.getType();
                                
                                // Sadece doğal blokları yok et (yapıları koru)
                                if (type != Material.AIR && 
                                    type != Material.BEDROCK &&
                                    !type.name().contains("STRUCTURE") &&
                                    !type.name().contains("BARRIER")) {
                                    
                                    // Optimize: setType yerine breakNaturally (daha hızlı)
                                    block.breakNaturally();
                                    count++;
                                    processed++;
                                }
                            }
                        }
                    }
                }
                
                // Partikül efekti (optimize: her 100 blokta bir)
                if (processed % 100 == 0) {
                    p.getWorld().spawnParticle(
                        org.bukkit.Particle.EXPLOSION_LARGE,
                        center.clone().add(
                            (Math.random() - 0.5) * radius * 2,
                            (Math.random() - 0.5) * height,
                            (Math.random() - 0.5) * radius * 2
                        ),
                        1
                    );
                }
                
                // İşlem tamamlandı mı?
                if (processed >= radius * radius * height * 0.3) { // %30'u yeterli
                    p.sendMessage("§c§lDağ yıkımı tamamlandı! " + processed + " blok yok edildi.");
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L); // Her tick'te çalış
    }
    
    /**
     * Seviye 5 Özel Güç: Klan Yıkımı (Tüm yapıları tek seferde)
     */
    public void fireClanDestroyer(Player p, me.mami.stratocraft.manager.TerritoryManager territoryManager) {
        if (territoryManager == null) return;
        
        Block targetBlock = p.getTargetBlock(null, 100);
        if (targetBlock == null) return;
        
        Location target = targetBlock.getLocation();
        me.mami.stratocraft.model.Clan targetClan = territoryManager.getTerritoryOwner(target);
        
        if (targetClan == null) {
            p.sendMessage("§cHedef bölgede klan yok!");
            return;
        }
        
        p.sendMessage("§c§lKLAN YIKICI AKTİF! " + targetClan.getName() + " klanının tüm yapıları yok ediliyor...");
        
        // Tüm yapıları yok et
        int destroyed = 0;
        for (me.mami.stratocraft.model.Structure structure : new ArrayList<>(targetClan.getStructures())) {
            Location structLoc = structure.getLocation();
            if (structLoc != null && structLoc.getWorld() != null) {
                // Yapıyı yok et (optimize: batch işlem)
                destroyStructureOptimized(structLoc, 5); // 5 blok yarıçap
                destroyed++;
            }
        }
        
        p.sendMessage("§c§l" + destroyed + " yapı yok edildi!");
    }
    
    /**
     * Optimize yapı yıkımı
     */
    private void destroyStructureOptimized(Location center, int radius) {
        // Chunk kontrolü
        if (!center.getChunk().isLoaded()) {
            return;
        }
        
        // Batch işlem: sadece yapı bloklarını yok et
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location loc = center.clone().add(x, y, z);
                    if (loc.distance(center) <= radius) {
                        Block block = loc.getBlock();
                        Material type = block.getType();
                        
                        // Yapı bloklarını yok et
                        if (type != Material.AIR && 
                            type != Material.BEDROCK &&
                            (type.name().contains("BLOCK") || 
                             type.name().contains("BRICK") ||
                             type.name().contains("STONE"))) {
                            block.setType(Material.AIR);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Seviye 5 Özel Güç: Boss Yıkımı
     */
    public void fireBossDestroyer(Player p, me.mami.stratocraft.manager.BossManager bossManager) {
        if (bossManager == null) return;
        
        Block targetBlock = p.getTargetBlock(null, 100);
        if (targetBlock == null) return;
        
        Location target = targetBlock.getLocation();
        
        // Yakındaki bossları bul
        for (org.bukkit.entity.Entity entity : target.getWorld().getNearbyEntities(target, 50, 50, 50)) {
            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) entity;
                if (bossManager.getBossData(living.getUniqueId()) != null) {
                    // Boss'a büyük hasar ver
                    double maxHealth = living.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
                    double damage = maxHealth * 0.5; // %50 hasar
                    living.damage(damage);
                    
                    // Efekt
                    living.getWorld().spawnParticle(
                        org.bukkit.Particle.EXPLOSION_HUGE,
                        living.getLocation(),
                        10,
                        2, 2, 2,
                        0.1
                    );
                    
                    p.sendMessage("§c§lBOSS YIKICI! " + living.getCustomName() + " büyük hasar aldı!");
                }
            }
        }
    }

    // 2. YILDIRIM
    public void fireLightningBattery(Player p) {
        Block targetBlock = p.getTargetBlock(null, 50);
        // Hedef bulunamadıysa (gökyüzüne bakıyorsa veya çok uzaksa) iptal et
        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            p.sendMessage("§cHata: Hedef çok uzak veya boşluğa bakıyorsun!");
            p.playSound(p.getLocation(), org.bukkit.Sound.BLOCK_DISPENSER_FAIL, 1, 1);
            return; // Bataryayı harcatma, işlemi iptal et
        }
        Location target = targetBlock.getLocation();
        p.getWorld().strikeLightning(target);
        p.sendMessage("§eYıldırım düştü!");
    }

    // 3. KARA DELİK
    public void fireBlackHole(Player p) {
        Block targetBlock = p.getTargetBlock(null, 30);
        // Hedef bulunamadıysa iptal et
        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            p.sendMessage("§cHata: Hedef çok uzak veya boşluğa bakıyorsun!");
            p.playSound(p.getLocation(), org.bukkit.Sound.BLOCK_DISPENSER_FAIL, 1, 1);
            return;
        }
        Location target = targetBlock.getLocation();
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
            if (point.getY() < -64 || point.getY() > 319)
                continue;

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
                        if (blockLoc.getY() < -64 || blockLoc.getY() > 319)
                            continue;

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
                if (blockLoc.getY() < -64 || blockLoc.getY() > 319)
                    continue;

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
        Block targetBlock = p.getTargetBlock(null, 30);
        // Hedef bulunamadıysa iptal et
        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            p.sendMessage("§cHata: Hedef çok uzak veya boşluğa bakıyorsun!");
            p.playSound(p.getLocation(), org.bukkit.Sound.BLOCK_DISPENSER_FAIL, 1, 1);
            return;
        }
        Location target = targetBlock.getLocation();
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
                if (x * x + z * z <= radius * radius) {
                    Location loc = center.clone().add(x, 0, z);
                    // Yükseklik sınırı kontrolü
                    if (loc.getY() < -64 || loc.getY() > 319)
                        continue;

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
            p.sendMessage(
                    "§bOzon Kalkanı aktif! Güneş Fırtınası koruması sağlandı. (" + placedBlocks + " blok, 20 saniye)");
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
                if (loc.getY() < -64 || loc.getY() > 319)
                    continue;

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
            if (loc.getY() < -64 || loc.getY() > 319)
                continue;

            // Eğer önünde blok varsa (LAVA değilse ve AIR değilse), o bloğu yok etme, es
            // geç
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
        if (plugin == null || locations.isEmpty())
            return;

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
