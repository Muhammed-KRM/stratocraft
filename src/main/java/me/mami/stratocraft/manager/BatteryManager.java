package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.BossManager.BossData;
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
    
    // Manager referansları (klan kontrolü için)
    private TerritoryManager territoryManager;
    private SiegeManager siegeManager;
    
    public void setTerritoryManager(TerritoryManager tm) {
        this.territoryManager = tm;
    }
    
    public void setSiegeManager(SiegeManager sm) {
        this.siegeManager = sm;
    }

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
        // Varsayılan: RED_DIAMOND ve DARK_MATTER false (geriye dönük uyumluluk)
        fireMagmaBattery(p, fuel, alchemyLevel, hasAmplifier, trainingMultiplier, batteryLevel, false, false);
    }
    
    public void fireMagmaBattery(Player p, Material fuel, int alchemyLevel, boolean hasAmplifier,
            double trainingMultiplier, int batteryLevel, boolean isRedDiamond, boolean isDarkMatter) {
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
        
        // Yakıt tipine göre çarpan (BatteryData'dan gelen bilgiyi kullan)
        double fuelMultiplier = 1.0;
        if (isDarkMatter) {
            fuelMultiplier = 10.0; // Karanlık Madde = en güçlü
        } else if (isRedDiamond) {
            fuelMultiplier = 5.0; // Kızıl Elmas = çok güçlü
        } else if (fuel == Material.DIAMOND) {
            fuelMultiplier = 2.5; // Elmas = güçlü
        } else if (fuel == Material.IRON_INGOT) {
            fuelMultiplier = 1.0; // Demir = standart
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
        
        // PERFORMANS OPTİMİZASYONU: Chunk kontrolü ve batch işlem (daha az blok işle)
        new BukkitRunnable() {
            int processed = 0;
            final int maxPerTick = 20; // Her tick'te maksimum 20 blok işle (daha optimize)
            int currentX = -radius;
            int currentZ = -radius;
            int currentY = -height/2;
            
            @Override
            public void run() {
                if (!p.isOnline()) {
                    cancel();
                    return;
                }
                
                int count = 0;
                // Sadece yüklü chunk'ları işle
                while (count < maxPerTick && currentY <= height/2) {
                    Location loc = center.clone().add(currentX, currentY, currentZ);
                    
                    // Chunk yüklü mü kontrol et (performans için kritik)
                    if (!loc.getChunk().isLoaded()) {
                        // Chunk yüklü değilse, sonraki koordinata geç
                        currentX++;
                        if (currentX > radius) {
                            currentX = -radius;
                            currentZ++;
                            if (currentZ > radius) {
                                currentZ = -radius;
                                currentY++;
                            }
                        }
                        continue;
                    }
                    
                    double distance = Math.sqrt(currentX*currentX + currentZ*currentZ);
                    if (distance <= radius) {
                        Block block = loc.getBlock();
                        Material type = block.getType();
                        
                        // Sadece doğal blokları yok et (yapıları koru)
                        if (type != Material.AIR && 
                            type != Material.BEDROCK &&
                            type.isSolid() &&
                            !type.name().contains("STRUCTURE") &&
                            !type.name().contains("BARRIER")) {
                            
                            // PERFORMANS: setType kullan (breakNaturally daha yavaş)
                            block.setType(Material.AIR);
                            count++;
                            processed++;
                        }
                    }
                    
                    // Sonraki koordinata geç
                    currentX++;
                    if (currentX > radius) {
                        currentX = -radius;
                        currentZ++;
                        if (currentZ > radius) {
                            currentZ = -radius;
                            currentY++;
                        }
                    }
                }
                
                // Partikül efekti (optimize: her 50 blokta bir)
                if (processed > 0 && processed % 50 == 0) {
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
                if (currentY > height/2) {
                    p.sendMessage("§c§lDağ yıkımı tamamlandı! " + processed + " blok yok edildi.");
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 2L); // Her 2 tick'te bir çalış (daha optimize)
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
     * Optimize yapı yıkımı (Performans iyileştirildi)
     */
    private void destroyStructureOptimized(Location center, int radius) {
        // Chunk kontrolü
        if (!center.getChunk().isLoaded()) {
            return;
        }
        
        // PERFORMANS OPTİMİZASYONU: Sadece yapı bloklarını yok et, daha az kontrol
        int destroyed = 0;
        final int maxBlocks = 100; // Maksimum 100 blok yok et (performans için)
        
        // Batch işlem: sadece yapı bloklarını yok et
        for (int x = -radius; x <= radius && destroyed < maxBlocks; x++) {
            for (int y = -radius; y <= radius && destroyed < maxBlocks; y++) {
                for (int z = -radius; z <= radius && destroyed < maxBlocks; z++) {
                    double distance = Math.sqrt(x*x + y*y + z*z);
                    if (distance > radius) continue; // Mesafe kontrolü (daha hızlı)
                    
                    Location loc = center.clone().add(x, y, z);
                    
                    // Chunk yüklü mü kontrol et
                    if (!loc.getChunk().isLoaded()) continue;
                    
                    Block block = loc.getBlock();
                    Material type = block.getType();
                    
                    // Yapı bloklarını yok et (optimize: daha spesifik kontrol)
                    if (type != Material.AIR && 
                        type != Material.BEDROCK &&
                        type.isSolid() &&
                        (type.name().contains("BLOCK") || 
                         type.name().contains("BRICK") ||
                         type.name().contains("STONE") ||
                         type.name().contains("WOOD") ||
                         type.name().contains("PLANK"))) {
                        block.setType(Material.AIR);
                        destroyed++;
                    }
                }
            }
        }
    }
    
    /**
     * Seviye 5 Özel Güç: Boss Yıkımı (Optimize)
     */
    public void fireBossDestroyer(Player p, me.mami.stratocraft.manager.BossManager bossManager) {
        if (bossManager == null) return;
        
        Block targetBlock = p.getTargetBlock(null, 100);
        if (targetBlock == null) return;
        
        Location target = targetBlock.getLocation();
        
        // PERFORMANS OPTİMİZASYONU: Daha küçük arama yarıçapı ve filtreleme
        double searchRadius = 30.0; // 50'den 30'a düşürüldü
        int foundCount = 0;
        final int maxBosses = 3; // Maksimum 3 boss'a hasar ver (performans için)
        
        // Yakındaki bossları bul (optimize: sadece LivingEntity'leri kontrol et)
        for (org.bukkit.entity.Entity entity : target.getWorld().getNearbyEntities(target, searchRadius, searchRadius, searchRadius)) {
            if (foundCount >= maxBosses) break; // Maksimum sayıya ulaşıldı
            
            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) entity;
                
                // PERFORMANS: Önce mesafe kontrolü (getNearbyEntities zaten yapıyor ama ekstra güvenlik)
                if (living.getLocation().distance(target) > searchRadius) continue;
                
                // Boss kontrolü
                if (bossManager.getBossData(living.getUniqueId()) != null) {
                    // Boss'a büyük hasar ver
                    double maxHealth = living.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
                    double damage = maxHealth * 0.5; // %50 hasar
                    living.damage(damage);
                    
                    // Efekt (optimize: daha az partikül)
                    living.getWorld().spawnParticle(
                        org.bukkit.Particle.EXPLOSION_HUGE,
                        living.getLocation(),
                        5, // 10'dan 5'e düşürüldü
                        2, 2, 2,
                        0.1
                    );
                    
                    String bossName = living.getCustomName();
                    if (bossName == null || bossName.isEmpty()) {
                        bossName = living.getType().name();
                    }
                    p.sendMessage("§c§lBOSS YIKICI! " + bossName + " büyük hasar aldı!");
                    foundCount++;
                }
            }
        }
        
        if (foundCount == 0) {
            p.sendMessage("§cYakında boss bulunamadı!");
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
    
    // ========== YENİ BATARYA SİSTEMİ - 3 KATEGORİ (75 BATARYA) ==========
    
    /**
     * Batarya Kategorileri
     */
    public enum BatteryCategory {
        ATTACK,      // Saldırı (Yok Etme) Bataryaları
        CONSTRUCTION, // Oluşturma Bataryaları
        SUPPORT      // Destek Bataryaları
    }
    
    /**
     * Batarya Tipi Enum (75 batarya için)
     */
    public enum BatteryType {
        // SALDIRI BATARYALARI (Seviye 1-5, her seviyede 5)
        // Seviye 1
        ATTACK_FIREBALL_L1("Ateş Topu", BatteryCategory.ATTACK, 1, Material.MAGMA_BLOCK, null),
        ATTACK_LIGHTNING_L1("Yıldırım", BatteryCategory.ATTACK, 1, Material.IRON_BLOCK, null),
        ATTACK_ICE_BALL_L1("Buz Topu", BatteryCategory.ATTACK, 1, Material.PACKED_ICE, null),
        ATTACK_POISON_ARROW_L1("Zehir Oku", BatteryCategory.ATTACK, 1, Material.EMERALD_BLOCK, null),
        ATTACK_SHOCK_L1("Şok", BatteryCategory.ATTACK, 1, Material.REDSTONE_BLOCK, null),
        
        // Seviye 2
        ATTACK_DOUBLE_FIREBALL_L2("Çift Ateş Topu", BatteryCategory.ATTACK, 2, Material.MAGMA_BLOCK, Material.NETHERRACK),
        ATTACK_CHAIN_LIGHTNING_L2("Zincir Yıldırım", BatteryCategory.ATTACK, 2, Material.IRON_BLOCK, Material.GOLD_BLOCK),
        ATTACK_ICE_STORM_L2("Buz Fırtınası", BatteryCategory.ATTACK, 2, Material.PACKED_ICE, Material.BLUE_ICE),
        ATTACK_ACID_RAIN_L2("Asit Yağmuru", BatteryCategory.ATTACK, 2, Material.EMERALD_BLOCK, Material.SLIME_BLOCK),
        ATTACK_ELECTRIC_NET_L2("Elektrik Ağı", BatteryCategory.ATTACK, 2, Material.REDSTONE_BLOCK, Material.LAPIS_BLOCK),
        
        // Seviye 3
        ATTACK_METEOR_SHOWER_L3("Meteor Yağmuru", BatteryCategory.ATTACK, 3, Material.OBSIDIAN, Material.MAGMA_BLOCK),
        ATTACK_STORM_L3("Fırtına", BatteryCategory.ATTACK, 3, Material.IRON_BLOCK, Material.DIAMOND_BLOCK),
        ATTACK_ICE_AGE_L3("Buz Çağı", BatteryCategory.ATTACK, 3, Material.PACKED_ICE, Material.BLUE_ICE),
        ATTACK_POISON_BOMB_L3("Zehir Bombası", BatteryCategory.ATTACK, 3, Material.EMERALD_BLOCK, Material.POISONOUS_POTATO),
        ATTACK_LIGHTNING_STORM_L3("Yıldırım Fırtınası", BatteryCategory.ATTACK, 3, Material.REDSTONE_BLOCK, Material.GLOWSTONE),
        
        // Seviye 4
        ATTACK_HELLFIRE_L4("Cehennem Ateşi", BatteryCategory.ATTACK, 4, Material.MAGMA_BLOCK, Material.NETHER_STAR),
        ATTACK_THUNDER_L4("Gök Gürültüsü", BatteryCategory.ATTACK, 4, Material.IRON_BLOCK, Material.BEACON),
        ATTACK_ICE_AGE_L4("Buz Çağı", BatteryCategory.ATTACK, 4, Material.PACKED_ICE, Material.FROSTED_ICE),
        ATTACK_DEATH_CLOUD_L4("Ölüm Bulutu", BatteryCategory.ATTACK, 4, Material.EMERALD_BLOCK, Material.WITHER_SKELETON_SKULL),
        ATTACK_ELECTRIC_STORM_L4("Elektrik Fırtınası", BatteryCategory.ATTACK, 4, Material.REDSTONE_BLOCK, Material.END_CRYSTAL),
        
        // Seviye 5
        ATTACK_MOUNTAIN_DESTROYER_L5("Dağ Yok Edici", BatteryCategory.ATTACK, 5, Material.BEDROCK, Material.NETHER_STAR),
        ATTACK_LAVA_TSUNAMI_L5("Lava Tufanı", BatteryCategory.ATTACK, 5, Material.BEDROCK, Material.LAVA_BUCKET),
        ATTACK_BOSS_KILLER_L5("Boss Katili", BatteryCategory.ATTACK, 5, Material.BEDROCK, Material.DRAGON_HEAD),
        ATTACK_AREA_DESTROYER_L5("Alan Yok Edici", BatteryCategory.ATTACK, 5, Material.BEDROCK, Material.COMMAND_BLOCK),
        ATTACK_APOCALYPSE_L5("Kıyamet", BatteryCategory.ATTACK, 5, Material.BEDROCK, Material.END_CRYSTAL),
        
        // OLUŞTURMA BATARYALARI (Seviye 1-5, her seviyede 5)
        // Seviye 1
        CONSTRUCTION_OBSIDIAN_WALL_L1("Obsidyen Duvar", BatteryCategory.CONSTRUCTION, 1, Material.OBSIDIAN, null),
        CONSTRUCTION_STONE_BRIDGE_L1("Taş Köprü", BatteryCategory.CONSTRUCTION, 1, Material.STONE, null),
        CONSTRUCTION_IRON_CAGE_L1("Demir Kafes", BatteryCategory.CONSTRUCTION, 1, Material.IRON_BLOCK, null),
        CONSTRUCTION_GLASS_WALL_L1("Cam Duvar", BatteryCategory.CONSTRUCTION, 1, Material.GLASS, null),
        CONSTRUCTION_WOOD_BARRICADE_L1("Ahşap Barikat", BatteryCategory.CONSTRUCTION, 1, Material.OAK_PLANKS, null),
        
        // Seviye 2
        CONSTRUCTION_OBSIDIAN_CAGE_L2("Obsidyen Kafes", BatteryCategory.CONSTRUCTION, 2, Material.OBSIDIAN, Material.IRON_BLOCK),
        CONSTRUCTION_STONE_BRIDGE_L2("Taş Köprü (Gelişmiş)", BatteryCategory.CONSTRUCTION, 2, Material.STONE, Material.COBBLESTONE),
        CONSTRUCTION_IRON_WALL_L2("Demir Duvar", BatteryCategory.CONSTRUCTION, 2, Material.IRON_BLOCK, Material.IRON_INGOT),
        CONSTRUCTION_GLASS_TUNNEL_L2("Cam Tünel", BatteryCategory.CONSTRUCTION, 2, Material.GLASS, Material.GLASS_PANE),
        CONSTRUCTION_WOOD_CASTLE_L2("Ahşap Kale", BatteryCategory.CONSTRUCTION, 2, Material.OAK_PLANKS, Material.OAK_LOG),
        
        // Seviye 3
        CONSTRUCTION_OBSIDIAN_WALL_L3("Obsidyen Duvar (Güçlü)", BatteryCategory.CONSTRUCTION, 3, Material.OBSIDIAN, Material.BEDROCK),
        CONSTRUCTION_NETHERITE_BRIDGE_L3("Netherite Köprü", BatteryCategory.CONSTRUCTION, 3, Material.NETHERITE_BLOCK, Material.NETHERITE_INGOT),
        CONSTRUCTION_IRON_PRISON_L3("Demir Hapishane", BatteryCategory.CONSTRUCTION, 3, Material.IRON_BLOCK, Material.IRON_BARS),
        CONSTRUCTION_GLASS_TOWER_L3("Cam Kule", BatteryCategory.CONSTRUCTION, 3, Material.GLASS, Material.GLASS_PANE),
        CONSTRUCTION_STONE_CASTLE_L3("Taş Kale", BatteryCategory.CONSTRUCTION, 3, Material.STONE, Material.COBBLESTONE),
        
        // Seviye 4
        CONSTRUCTION_OBSIDIAN_CASTLE_L4("Obsidyen Kale", BatteryCategory.CONSTRUCTION, 4, Material.OBSIDIAN, Material.END_CRYSTAL),
        CONSTRUCTION_NETHERITE_BRIDGE_L4("Netherite Köprü (Gelişmiş)", BatteryCategory.CONSTRUCTION, 4, Material.NETHERITE_BLOCK, Material.BEACON),
        CONSTRUCTION_IRON_PRISON_L4("Demir Hapishane (Güçlü)", BatteryCategory.CONSTRUCTION, 4, Material.IRON_BLOCK, Material.ANVIL),
        CONSTRUCTION_GLASS_TOWER_L4("Cam Kule (Gelişmiş)", BatteryCategory.CONSTRUCTION, 4, Material.GLASS, Material.BEACON),
        CONSTRUCTION_STONE_FORTRESS_L4("Taş Şato", BatteryCategory.CONSTRUCTION, 4, Material.STONE, Material.BEACON),
        
        // Seviye 5
        CONSTRUCTION_OBSIDIAN_PRISON_L5("Obsidyen Hapishane", BatteryCategory.CONSTRUCTION, 5, Material.BEDROCK, Material.END_CRYSTAL),
        CONSTRUCTION_NETHERITE_BRIDGE_L5("Netherite Köprü (Efsanevi)", BatteryCategory.CONSTRUCTION, 5, Material.BEDROCK, Material.BEACON),
        CONSTRUCTION_IRON_CASTLE_L5("Demir Kale (Efsanevi)", BatteryCategory.CONSTRUCTION, 5, Material.BEDROCK, Material.ANVIL),
        CONSTRUCTION_GLASS_TOWER_L5("Cam Kule (Efsanevi)", BatteryCategory.CONSTRUCTION, 5, Material.BEDROCK, Material.BEACON),
        CONSTRUCTION_STONE_FORTRESS_L5("Taş Kalesi (Efsanevi)", BatteryCategory.CONSTRUCTION, 5, Material.BEDROCK, Material.BEACON),
        
        // DESTEK BATARYALARI (Seviye 1-5, her seviyede 5)
        // Seviye 1
        SUPPORT_HEAL_L1("Can Yenileme", BatteryCategory.SUPPORT, 1, Material.GOLD_BLOCK, null),
        SUPPORT_SPEED_L1("Hız Artışı", BatteryCategory.SUPPORT, 1, Material.EMERALD_BLOCK, null),
        SUPPORT_DAMAGE_L1("Hasar Artışı", BatteryCategory.SUPPORT, 1, Material.DIAMOND_BLOCK, null),
        SUPPORT_ARMOR_L1("Zırh Artışı", BatteryCategory.SUPPORT, 1, Material.IRON_BLOCK, null),
        SUPPORT_REGENERATION_L1("Yenilenme", BatteryCategory.SUPPORT, 1, Material.LAPIS_BLOCK, null),
        
        // Seviye 2
        SUPPORT_HEAL_L2("Can Yenileme (Gelişmiş)", BatteryCategory.SUPPORT, 2, Material.GOLD_BLOCK, Material.GOLD_INGOT),
        SUPPORT_SPEED_L2("Hız Artışı (Gelişmiş)", BatteryCategory.SUPPORT, 2, Material.EMERALD_BLOCK, Material.EMERALD),
        SUPPORT_DAMAGE_L2("Hasar Artışı (Gelişmiş)", BatteryCategory.SUPPORT, 2, Material.DIAMOND_BLOCK, Material.DIAMOND),
        SUPPORT_ARMOR_L2("Zırh Artışı (Gelişmiş)", BatteryCategory.SUPPORT, 2, Material.IRON_BLOCK, Material.IRON_INGOT),
        SUPPORT_REGENERATION_L2("Yenilenme (Gelişmiş)", BatteryCategory.SUPPORT, 2, Material.LAPIS_BLOCK, Material.LAPIS_LAZULI),
        
        // Seviye 3
        SUPPORT_HEAL_L3("Can Yenileme (Güçlü)", BatteryCategory.SUPPORT, 3, Material.GOLD_BLOCK, Material.GOLDEN_APPLE),
        SUPPORT_SPEED_L3("Hız Artışı (Güçlü)", BatteryCategory.SUPPORT, 3, Material.EMERALD_BLOCK, Material.EMERALD_BLOCK),
        SUPPORT_DAMAGE_L3("Hasar Artışı (Güçlü)", BatteryCategory.SUPPORT, 3, Material.DIAMOND_BLOCK, Material.DIAMOND_BLOCK),
        SUPPORT_ARMOR_L3("Zırh Artışı (Güçlü)", BatteryCategory.SUPPORT, 3, Material.IRON_BLOCK, Material.IRON_BLOCK),
        SUPPORT_REGENERATION_L3("Yenilenme (Güçlü)", BatteryCategory.SUPPORT, 3, Material.LAPIS_BLOCK, Material.LAPIS_BLOCK),
        
        // Seviye 4
        SUPPORT_HEAL_L4("Can Yenileme (Çok Güçlü)", BatteryCategory.SUPPORT, 4, Material.GOLD_BLOCK, Material.ENCHANTED_GOLDEN_APPLE),
        SUPPORT_SPEED_L4("Hız Artışı (Çok Güçlü)", BatteryCategory.SUPPORT, 4, Material.EMERALD_BLOCK, Material.BEACON),
        SUPPORT_DAMAGE_L4("Hasar Artışı (Çok Güçlü)", BatteryCategory.SUPPORT, 4, Material.DIAMOND_BLOCK, Material.BEACON),
        SUPPORT_ARMOR_L4("Zırh Artışı (Çok Güçlü)", BatteryCategory.SUPPORT, 4, Material.IRON_BLOCK, Material.BEACON),
        SUPPORT_REGENERATION_L4("Yenilenme (Çok Güçlü)", BatteryCategory.SUPPORT, 4, Material.LAPIS_BLOCK, Material.BEACON),
        
        // Seviye 5
        SUPPORT_HEAL_L5("Can Yenileme (Efsanevi)", BatteryCategory.SUPPORT, 5, Material.BEDROCK, Material.NETHER_STAR),
        SUPPORT_SPEED_L5("Hız Artışı (Efsanevi)", BatteryCategory.SUPPORT, 5, Material.BEDROCK, Material.NETHER_STAR),
        SUPPORT_DAMAGE_L5("Hasar Artışı (Efsanevi)", BatteryCategory.SUPPORT, 5, Material.BEDROCK, Material.NETHER_STAR),
        SUPPORT_ARMOR_L5("Zırh Artışı (Efsanevi)", BatteryCategory.SUPPORT, 5, Material.BEDROCK, Material.NETHER_STAR),
        SUPPORT_REGENERATION_L5("Yenilenme (Efsanevi)", BatteryCategory.SUPPORT, 5, Material.BEDROCK, Material.NETHER_STAR);
        
        private final String displayName;
        private final BatteryCategory category;
        private final int level;
        private final Material baseBlock;
        private final Material sideBlock;
        
        BatteryType(String displayName, BatteryCategory category, int level, Material baseBlock, Material sideBlock) {
            this.displayName = displayName;
            this.category = category;
            this.level = level;
            this.baseBlock = baseBlock;
            this.sideBlock = sideBlock;
        }
        
        public String getDisplayName() { return displayName; }
        public BatteryCategory getCategory() { return category; }
        public int getLevel() { return level; }
        public Material getBaseBlock() { return baseBlock; }
        public Material getSideBlock() { return sideBlock; }
    }
    
    /**
     * Klan kontrolü - Savaşta olmayan klan alanlarında blok yok etme/yapma yapılamaz
     */
    private boolean canModifyTerritory(Player player, Location loc) {
        if (territoryManager == null || siegeManager == null) return true; // Manager yoksa izin ver
        
        Clan territoryOwner = territoryManager.getTerritoryOwner(loc);
        if (territoryOwner == null) return true; // Bölge sahibi yok, izin ver
        
        Clan playerClan = territoryManager.getClanManager().getClanByPlayer(player.getUniqueId());
        if (playerClan == null) return true; // Oyuncu klanı yok, izin ver
        
        // Aynı klan ise izin ver
        if (territoryOwner.equals(playerClan)) return true;
        
        // Savaşta mı kontrol et
        return siegeManager.isUnderSiege(territoryOwner) && 
               siegeManager.getAttacker(territoryOwner).equals(playerClan);
    }
    
    /**
     * Yakındaki klan üyelerini bul
     */
    private List<Player> getNearbyClanMembers(Player player, double radius) {
        List<Player> members = new ArrayList<>();
        Clan playerClan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
        if (playerClan == null) return members;
        
        for (Player nearby : player.getWorld().getPlayers()) {
            if (nearby.equals(player)) continue;
            if (nearby.getLocation().distance(player.getLocation()) > radius) continue;
            
            Clan nearbyClan = plugin.getClanManager().getClanByPlayer(nearby.getUniqueId());
            if (nearbyClan != null && nearbyClan.equals(playerClan)) {
                members.add(nearby);
            }
        }
        return members;
    }
    
    /**
     * RayTrace ile hedef bul (oyuncunun baktığı yön)
     */
    private Location getTargetLocation(Player player, int maxDistance) {
        org.bukkit.util.RayTraceResult result = player.rayTraceBlocks(maxDistance);
        if (result != null && result.getHitBlock() != null) {
            return result.getHitBlock().getLocation();
        }
        // Hedef bulunamadıysa, oyuncunun baktığı yöne doğru maxDistance kadar ileri
        Vector direction = player.getLocation().getDirection().normalize();
        return player.getLocation().add(direction.multiply(maxDistance));
    }
    
    // ========== YENİ BATARYA METODLARI (75 BATARYA) ==========
    
    /**
     * Batarya ateşleme - Ana metod (kategori ve tip'e göre yönlendirme)
     */
    public void fireBattery(Player player, BatteryType batteryType, BatteryData data) {
        switch (batteryType.getCategory()) {
            case ATTACK:
                fireAttackBattery(player, batteryType, data);
                break;
            case CONSTRUCTION:
                fireConstructionBattery(player, batteryType, data);
                break;
            case SUPPORT:
                fireSupportBattery(player, batteryType, data);
                break;
        }
        // Bataryayı kullan (removeBattery çağrısı BatteryListener'da yapılıyor)
    }
    
    /**
     * SALDIRI BATARYALARI (25 batarya)
     */
    private void fireAttackBattery(Player player, BatteryType batteryType, BatteryData data) {
        Location target = getTargetLocation(player, getAttackRange(batteryType));
        int level = batteryType.getLevel();
        
        switch (batteryType) {
            // Seviye 1
            case ATTACK_FIREBALL_L1:
                fireFireball(player, target, 3, 2.0, level);
                break;
            case ATTACK_LIGHTNING_L1:
                fireLightning(player, target, 5, 5.0, level);
                break;
            case ATTACK_ICE_BALL_L1:
                fireIceBall(player, target, 5, 2.0, level);
                break;
            case ATTACK_POISON_ARROW_L1:
                firePoisonArrow(player, target, 8, 2.0, 3, level);
                break;
            case ATTACK_SHOCK_L1:
                fireShock(player, target, 4, 3.0, level);
                break;
            
            // Seviye 2
            case ATTACK_DOUBLE_FIREBALL_L2:
                fireDoubleFireball(player, target, 5, 4.0, level);
                break;
            case ATTACK_CHAIN_LIGHTNING_L2:
                fireChainLightning(player, target, 8, 4.0, 3, level);
                break;
            case ATTACK_ICE_STORM_L2:
                fireIceStorm(player, target, 7, 6.0, level);
                break;
            case ATTACK_ACID_RAIN_L2:
                fireAcidRain(player, target, 5, 5, level);
                break;
            case ATTACK_ELECTRIC_NET_L2:
                fireElectricNet(player, target, 5, 5.0, level);
                break;
            
            // Seviye 3
            case ATTACK_METEOR_SHOWER_L3:
                fireMeteorShower(player, target, 10, 8.0, 5, level);
                break;
            case ATTACK_STORM_L3:
                fireStorm(player, target, 8, 7.0, 5, level);
                break;
            case ATTACK_ICE_AGE_L3:
                fireIceAge(player, target, 15, 8.0, 10, level);
                break;
            case ATTACK_POISON_BOMB_L3:
                firePoisonBomb(player, target, 8, 10, level);
                break;
            case ATTACK_LIGHTNING_STORM_L3:
                fireLightningStorm(player, target, 7, 5.0, 5, level);
                break;
            
            // Seviye 4
            case ATTACK_HELLFIRE_L4:
                fireHellfire(player, target, 12, 10, level);
                break;
            case ATTACK_THUNDER_L4:
                fireThunder(player, target, 15, 20.0, level);
                break;
            case ATTACK_ICE_AGE_L4:
                fireIceAge(player, target, 15, 10.0, 10, level);
                break;
            case ATTACK_DEATH_CLOUD_L4:
                fireDeathCloud(player, target, 12, 15, level);
                break;
            case ATTACK_ELECTRIC_STORM_L4:
                fireElectricStorm(player, target, 10, 7.0, 8, level);
                break;
            
            // Seviye 5
            case ATTACK_MOUNTAIN_DESTROYER_L5:
                fireMountainDestroyer(player, target, 50, 500.0, 30, level);
                break;
            case ATTACK_LAVA_TSUNAMI_L5:
                fireLavaTsunami(player, target, 30, 300.0, 60, level);
                break;
            case ATTACK_BOSS_KILLER_L5:
                fireBossKiller(player, target, 50, 500.0, 50.0, level);
                break;
            case ATTACK_AREA_DESTROYER_L5:
                fireAreaDestroyer(player, target, 30, 500.0, 30, level);
                break;
            case ATTACK_APOCALYPSE_L5:
                fireApocalypse(player, target, 40, 600.0, 40, level);
                break;
        }
    }
    
    /**
     * OLUŞTURMA BATARYALARI (25 batarya)
     */
    private void fireConstructionBattery(Player player, BatteryType batteryType, BatteryData data) {
        Location target = getTargetLocation(player, getConstructionRange(batteryType));
        int level = batteryType.getLevel();
        
        // Klan kontrolü
        if (!canModifyTerritory(player, target)) {
            player.sendMessage("§cBu bölgede yapı oluşturamazsın! Sadece savaşta olan klan alanlarında çalışır.");
            return;
        }
        
        switch (batteryType) {
            // Seviye 1
            case CONSTRUCTION_OBSIDIAN_WALL_L1:
                createObsidianWall(player, target, 5, 5, 3, level);
                break;
            case CONSTRUCTION_STONE_BRIDGE_L1:
                createStoneBridge(player, target, 10, level);
                break;
            case CONSTRUCTION_IRON_CAGE_L1:
                createIronCage(player, target, 5, 5, 3, level);
                break;
            case CONSTRUCTION_GLASS_WALL_L1:
                createGlassWall(player, target, 5, 5, 3, level);
                break;
            case CONSTRUCTION_WOOD_BARRICADE_L1:
                createWoodBarricade(player, target, 5, 5, 2, level);
                break;
            
            // Seviye 2
            case CONSTRUCTION_OBSIDIAN_CAGE_L2:
                createObsidianCage(player, target, 10, 10, 5, level);
                break;
            case CONSTRUCTION_STONE_BRIDGE_L2:
                createStoneBridge(player, target, 20, level);
                break;
            case CONSTRUCTION_IRON_WALL_L2:
                createIronWall(player, target, 10, 5, 3, level);
                break;
            case CONSTRUCTION_GLASS_TUNNEL_L2:
                createGlassTunnel(player, target, 15, level);
                break;
            case CONSTRUCTION_WOOD_CASTLE_L2:
                createWoodCastle(player, target, 10, 10, 5, level);
                break;
            
            // Seviye 3
            case CONSTRUCTION_OBSIDIAN_WALL_L3:
                createObsidianWall(player, target, 15, 5, 5, level);
                break;
            case CONSTRUCTION_NETHERITE_BRIDGE_L3:
                createNetheriteBridge(player, target, 30, level);
                break;
            case CONSTRUCTION_IRON_PRISON_L3:
                createIronPrison(player, target, 15, 15, 8, level);
                break;
            case CONSTRUCTION_GLASS_TOWER_L3:
                createGlassTower(player, target, 10, 10, 15, level);
                break;
            case CONSTRUCTION_STONE_CASTLE_L3:
                createStoneCastle(player, target, 15, 15, 10, level);
                break;
            
            // Seviye 4
            case CONSTRUCTION_OBSIDIAN_CASTLE_L4:
                createObsidianCastle(player, target, 20, 20, 10, level);
                break;
            case CONSTRUCTION_NETHERITE_BRIDGE_L4:
                createNetheriteBridge(player, target, 50, level);
                break;
            case CONSTRUCTION_IRON_PRISON_L4:
                createIronPrison(player, target, 20, 20, 12, level);
                break;
            case CONSTRUCTION_GLASS_TOWER_L4:
                createGlassTower(player, target, 15, 15, 20, level);
                break;
            case CONSTRUCTION_STONE_FORTRESS_L4:
                createStoneFortress(player, target, 25, 25, 15, level);
                break;
            
            // Seviye 5
            case CONSTRUCTION_OBSIDIAN_PRISON_L5:
                createObsidianPrison(player, target, 25, 25, 15, level);
                break;
            case CONSTRUCTION_NETHERITE_BRIDGE_L5:
                createNetheriteBridge(player, target, 100, level);
                break;
            case CONSTRUCTION_IRON_CASTLE_L5:
                createIronCastle(player, target, 30, 30, 20, level);
                break;
            case CONSTRUCTION_GLASS_TOWER_L5:
                createGlassTower(player, target, 20, 20, 30, level);
                break;
            case CONSTRUCTION_STONE_FORTRESS_L5:
                createStoneFortress(player, target, 40, 40, 25, level);
                break;
        }
    }
    
    /**
     * DESTEK BATARYALARI (25 batarya)
     */
    private void fireSupportBattery(Player player, BatteryType batteryType, BatteryData data) {
        int level = batteryType.getLevel();
        double radius = getSupportRadius(level);
        
        switch (batteryType) {
            // Seviye 1
            case SUPPORT_HEAL_L1:
                applyHealSupport(player, radius, 5.0, level);
                break;
            case SUPPORT_SPEED_L1:
                applySpeedSupport(player, radius, 1, 10, level);
                break;
            case SUPPORT_DAMAGE_L1:
                applyDamageSupport(player, radius, 1, 10, level);
                break;
            case SUPPORT_ARMOR_L1:
                applyArmorSupport(player, radius, 1, 10, level);
                break;
            case SUPPORT_REGENERATION_L1:
                applyRegenerationSupport(player, radius, 1, 10, level);
                break;
            
            // Seviye 2
            case SUPPORT_HEAL_L2:
                applyHealSupport(player, radius, 10.0, level);
                break;
            case SUPPORT_SPEED_L2:
                applySpeedSupport(player, radius, 2, 15, level);
                break;
            case SUPPORT_DAMAGE_L2:
                applyDamageSupport(player, radius, 2, 15, level);
                break;
            case SUPPORT_ARMOR_L2:
                applyArmorSupport(player, radius, 2, 15, level);
                break;
            case SUPPORT_REGENERATION_L2:
                applyRegenerationSupport(player, radius, 2, 15, level);
                break;
            
            // Seviye 3
            case SUPPORT_HEAL_L3:
                applyHealSupport(player, radius, 20.0, level);
                break;
            case SUPPORT_SPEED_L3:
                applySpeedSupport(player, radius, 3, 20, level);
                break;
            case SUPPORT_DAMAGE_L3:
                applyDamageSupport(player, radius, 3, 20, level);
                break;
            case SUPPORT_ARMOR_L3:
                applyArmorSupport(player, radius, 3, 20, level);
                break;
            case SUPPORT_REGENERATION_L3:
                applyRegenerationSupport(player, radius, 3, 20, level);
                break;
            
            // Seviye 4
            case SUPPORT_HEAL_L4:
                applyHealSupport(player, radius, 30.0, level);
                break;
            case SUPPORT_SPEED_L4:
                applySpeedSupport(player, radius, 4, 30, level);
                break;
            case SUPPORT_DAMAGE_L4:
                applyDamageSupport(player, radius, 4, 30, level);
                break;
            case SUPPORT_ARMOR_L4:
                applyArmorSupport(player, radius, 4, 30, level);
                break;
            case SUPPORT_REGENERATION_L4:
                applyRegenerationSupport(player, radius, 4, 30, level);
                break;
            
            // Seviye 5
            case SUPPORT_HEAL_L5:
                applyHealSupport(player, radius, -1.0, level); // -1 = tam can + 50 ekstra
                break;
            case SUPPORT_SPEED_L5:
                applySpeedSupport(player, radius, 5, 60, level);
                break;
            case SUPPORT_DAMAGE_L5:
                applyDamageSupport(player, radius, 5, 60, level);
                break;
            case SUPPORT_ARMOR_L5:
                applyArmorSupport(player, radius, 5, 60, level);
                break;
            case SUPPORT_REGENERATION_L5:
                applyRegenerationSupport(player, radius, 5, 60, level);
                break;
        }
    }
    
    // ========== HELPER METODLAR ==========
    
    private int getAttackRange(BatteryType type) {
        int level = type.getLevel();
        switch (level) {
            case 1: return 8;
            case 2: return 10;
            case 3: return 15;
            case 4: return 20;
            case 5: return 50;
            default: return 10;
        }
    }
    
    private int getConstructionRange(BatteryType type) {
        int level = type.getLevel();
        switch (level) {
            case 1: return 10;
            case 2: return 15;
            case 3: return 20;
            case 4: return 25;
            case 5: return 30;
            default: return 15;
        }
    }
    
    private double getSupportRadius(int level) {
        switch (level) {
            case 1: return 10.0;
            case 2: return 15.0;
            case 3: return 20.0;
            case 4: return 25.0;
            case 5: return 30.0;
            default: return 10.0;
        }
    }
    
    // ========== SALDIRI BATARYA METODLARI ==========
    
    private void fireFireball(Player player, Location target, int radius, double damage, int level) {
        // Ateş topu at
        org.bukkit.entity.Fireball fireball = player.getWorld().spawn(target, org.bukkit.entity.Fireball.class);
        fireball.setDirection(player.getLocation().getDirection());
        fireball.setYield((float) (damage / 2.0));
        
        // Hasar ver
        for (Entity entity : player.getWorld().getNearbyEntities(target, radius, radius, radius)) {
            if (entity instanceof LivingEntity && entity != player) {
                ((LivingEntity) entity).damage(damage);
                entity.setFireTicks(100);
            }
        }
        
        player.sendMessage("§cAteş topu atıldı!");
    }
    
    private void fireLightning(Player player, Location target, int radius, double damage, int level) {
        player.getWorld().strikeLightning(target);
        
        // Hasar ver
        for (Entity entity : player.getWorld().getNearbyEntities(target, radius, radius, radius)) {
            if (entity instanceof LivingEntity && entity != player) {
                ((LivingEntity) entity).damage(damage);
            }
        }
        
        player.sendMessage("§eYıldırım düştü!");
    }
    
    private void fireIceBall(Player player, Location target, int radius, double damage, int level) {
        // Buz topu efekti
        player.getWorld().spawnParticle(org.bukkit.Particle.SNOWBALL, target, 20, 0.5, 0.5, 0.5, 0.1);
        
        // Hasar ve dondurma
        for (Entity entity : player.getWorld().getNearbyEntities(target, radius, radius, radius)) {
            if (entity instanceof LivingEntity && entity != player) {
                ((LivingEntity) entity).damage(damage);
                ((LivingEntity) entity).addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.SLOW, 60, 1, false, false, true));
            }
        }
        
        player.sendMessage("§bBuz topu atıldı!");
    }
    
    private void firePoisonArrow(Player player, Location target, int radius, double damage, int duration, int level) {
        // Zehir oku efekti
        org.bukkit.entity.Arrow arrow = player.getWorld().spawn(target, org.bukkit.entity.Arrow.class);
        arrow.setVelocity(player.getLocation().getDirection().multiply(2.0));
        
        // Hasar ve zehir
        for (Entity entity : player.getWorld().getNearbyEntities(target, radius, radius, radius)) {
            if (entity instanceof LivingEntity && entity != player) {
                ((LivingEntity) entity).damage(damage);
                ((LivingEntity) entity).addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.POISON, duration * 20, 0, false, false, true));
            }
        }
        
        player.sendMessage("§2Zehir oku atıldı!");
    }
    
    private void fireShock(Player player, Location target, int radius, double damage, int level) {
        // Şok efekti
        player.getWorld().spawnParticle(org.bukkit.Particle.ELECTRIC_SPARK, target, 30, 1.0, 1.0, 1.0, 0.1);
        player.getWorld().playSound(target, org.bukkit.Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
        
        // Hasar ver
        for (Entity entity : player.getWorld().getNearbyEntities(target, radius, radius, radius)) {
            if (entity instanceof LivingEntity && entity != player) {
                ((LivingEntity) entity).damage(damage);
            }
        }
        
        player.sendMessage("§eŞok atıldı!");
    }
    
    private void fireDoubleFireball(Player player, Location target, int radius, double damage, int level) {
        // İki ateş topu
        Vector direction = player.getLocation().getDirection();
        Vector perpendicular = new Vector(-direction.getZ(), 0, direction.getX()).normalize().multiply(1.5);
        
        Location target1 = target.clone().add(perpendicular);
        Location target2 = target.clone().subtract(perpendicular);
        
        fireFireball(player, target1, radius, damage, level);
        fireFireball(player, target2, radius, damage, level);
        
        player.sendMessage("§cÇift ateş topu atıldı!");
    }
    
    private void fireChainLightning(Player player, Location target, int radius, double damage, int chainCount, int level) {
        Location currentTarget = target;
        
        for (int i = 0; i < chainCount; i++) {
            fireLightning(player, currentTarget, radius, damage, level);
            
            // Sonraki hedef bul
            Entity nearest = null;
            double nearestDist = Double.MAX_VALUE;
            for (Entity entity : player.getWorld().getNearbyEntities(currentTarget, 10, 10, 10)) {
                if (entity instanceof LivingEntity && entity != player && entity != nearest) {
                    double dist = entity.getLocation().distance(currentTarget);
                    if (dist < nearestDist) {
                        nearestDist = dist;
                        nearest = entity;
                    }
                }
            }
            
            if (nearest != null) {
                currentTarget = nearest.getLocation();
            } else {
                break;
            }
        }
        
        player.sendMessage("§eZincir yıldırım atıldı!");
    }
    
    private void fireIceStorm(Player player, Location target, int radius, double damage, int level) {
        // Buz fırtınası
        for (int i = 0; i < 10; i++) {
            Location randomLoc = target.clone().add(
                (Math.random() - 0.5) * radius * 2,
                Math.random() * 5,
                (Math.random() - 0.5) * radius * 2
            );
            fireIceBall(player, randomLoc, radius / 2, damage / 2, level);
        }
        
        player.sendMessage("§bBuz fırtınası atıldı!");
    }
    
    private void fireAcidRain(Player player, Location target, int radius, int duration, int level) {
        // Asit yağmuru (sürekli zehir)
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= duration) {
                    cancel();
                    return;
                }
                
                for (Entity entity : player.getWorld().getNearbyEntities(target, radius, radius, radius)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        ((LivingEntity) entity).addPotionEffect(new org.bukkit.potion.PotionEffect(
                            org.bukkit.potion.PotionEffectType.POISON, 20, 0, false, false, true));
                        ((LivingEntity) entity).damage(1.0);
                    }
                }
                
                player.getWorld().spawnParticle(org.bukkit.Particle.DRIP_LAVA, target, 10, radius, 5, radius, 0.1);
                count++;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        
        player.sendMessage("§2Asit yağmuru başladı!");
    }
    
    private void fireElectricNet(Player player, Location target, int radius, double damage, int level) {
        // Elektrik ağı (çoklu şok)
        for (int i = 0; i < 5; i++) {
            Location randomLoc = target.clone().add(
                (Math.random() - 0.5) * radius * 2,
                Math.random() * 3,
                (Math.random() - 0.5) * radius * 2
            );
            fireShock(player, randomLoc, radius / 2, damage, level);
        }
        
        player.sendMessage("§eElektrik ağı atıldı!");
    }
    
    private void fireMeteorShower(Player player, Location target, int radius, double damage, int meteorCount, int level) {
        // Meteor yağmuru
        for (int i = 0; i < meteorCount; i++) {
            Location meteorLoc = target.clone().add(
                (Math.random() - 0.5) * radius * 2,
                20 + Math.random() * 10,
                (Math.random() - 0.5) * radius * 2
            );
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (meteorLoc.getY() <= target.getY()) {
                        // Patlama
                        player.getWorld().createExplosion(meteorLoc, 3.0f, false, false);
                        
                        // Hasar ve blok kırma (klan kontrolü ile)
                        for (Entity entity : player.getWorld().getNearbyEntities(meteorLoc, 5, 5, 5)) {
                            if (entity instanceof LivingEntity && entity != player) {
                                ((LivingEntity) entity).damage(damage);
                            }
                        }
                        
                        // Blok kırma (sadece savaşta olan klan alanlarında)
                        if (canModifyTerritory(player, meteorLoc)) {
                            for (int x = -2; x <= 2; x++) {
                                for (int y = -2; y <= 2; y++) {
                                    for (int z = -2; z <= 2; z++) {
                                        Block block = meteorLoc.clone().add(x, y, z).getBlock();
                                        if (block.getType() != Material.BEDROCK && block.getType() != Material.AIR) {
                                            block.setType(Material.AIR);
                                        }
                                    }
                                }
                            }
                        }
                        
                        cancel();
                        return;
                    }
                    
                    // Meteor düşüşü
                    meteorLoc.add(0, -1, 0);
                    player.getWorld().spawnParticle(org.bukkit.Particle.FLAME, meteorLoc, 5, 0.3, 0.3, 0.3, 0.05);
                }
            }.runTaskTimer(plugin, 0L, 2L);
        }
        
        player.sendMessage("§cMeteor yağmuru başladı!");
    }
    
    private void fireStorm(Player player, Location target, int radius, double damage, int lightningCount, int level) {
        // Fırtına (çoklu yıldırım)
        for (int i = 0; i < lightningCount; i++) {
            Location lightningLoc = target.clone().add(
                (Math.random() - 0.5) * radius * 2,
                0,
                (Math.random() - 0.5) * radius * 2
            );
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    fireLightning(player, lightningLoc, radius / 2, damage, level);
                }
            }.runTaskLater(plugin, i * 10L);
        }
        
        player.sendMessage("§eFırtına başladı!");
    }
    
    private void fireIceAge(Player player, Location target, int radius, double damage, int duration, int level) {
        // Buz çağı (sürekli dondurma)
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= duration) {
                    cancel();
                    return;
                }
                
                for (Entity entity : player.getWorld().getNearbyEntities(target, radius, radius, radius)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        ((LivingEntity) entity).damage(damage / duration);
                        ((LivingEntity) entity).addPotionEffect(new org.bukkit.potion.PotionEffect(
                            org.bukkit.potion.PotionEffectType.SLOW, 40, 2, false, false, true));
                    }
                }
                
                player.getWorld().spawnParticle(org.bukkit.Particle.SNOWBALL, target, 50, radius, 5, radius, 0.1);
                count++;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        
        player.sendMessage("§bBuz çağı başladı!");
    }
    
    private void firePoisonBomb(Player player, Location target, int radius, int duration, int level) {
        // Zehir bombası (büyük alan zehir)
        player.getWorld().spawnParticle(org.bukkit.Particle.DRAGON_BREATH, target, 100, radius, 5, radius, 0.1);
        
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= duration) {
                    cancel();
                    return;
                }
                
                for (Entity entity : player.getWorld().getNearbyEntities(target, radius, radius, radius)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        ((LivingEntity) entity).addPotionEffect(new org.bukkit.potion.PotionEffect(
                            org.bukkit.potion.PotionEffectType.POISON, 40, 1, false, false, true));
                        ((LivingEntity) entity).damage(2.0);
                    }
                }
                
                count++;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        
        player.sendMessage("§2Zehir bombası patladı!");
    }
    
    private void fireLightningStorm(Player player, Location target, int radius, double damage, int duration, int level) {
        // Yıldırım fırtınası (sürekli şok)
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= duration) {
                    cancel();
                    return;
                }
                
                Location randomLoc = target.clone().add(
                    (Math.random() - 0.5) * radius * 2,
                    0,
                    (Math.random() - 0.5) * radius * 2
                );
                fireLightning(player, randomLoc, radius / 2, damage, level);
                count++;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        
        player.sendMessage("§eYıldırım fırtınası başladı!");
    }
    
    private void fireHellfire(Player player, Location target, int radius, int duration, int level) {
        // Cehennem ateşi (sürekli yanma + blok kırma)
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= duration) {
                    cancel();
                    return;
                }
                
                for (Entity entity : player.getWorld().getNearbyEntities(target, radius, radius, radius)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        entity.setFireTicks(100);
                        ((LivingEntity) entity).damage(3.0);
                    }
                }
                
                // Blok kırma (sadece savaşta olan klan alanlarında)
                if (canModifyTerritory(player, target)) {
                    for (int x = -1; x <= 1; x++) {
                        for (int z = -1; z <= 1; z++) {
                            Block block = target.clone().add(x, 0, z).getBlock();
                            if (block.getType() != Material.BEDROCK && block.getType() != Material.AIR) {
                                block.setType(Material.AIR);
                            }
                        }
                    }
                }
                
                player.getWorld().spawnParticle(org.bukkit.Particle.FLAME, target, 50, radius, 5, radius, 0.1);
                count++;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        
        player.sendMessage("§cCehennem ateşi başladı!");
    }
    
    private void fireThunder(Player player, Location target, int radius, double damage, int level) {
        // Gök gürültüsü (dev yıldırım + patlama)
        fireLightning(player, target, radius, damage, level);
        player.getWorld().createExplosion(target, 5.0f, false, false);
        
        player.sendMessage("§eGök gürültüsü düştü!");
    }
    
    private void fireDeathCloud(Player player, Location target, int radius, int duration, int level) {
        // Ölüm bulutu (ölümcül zehir)
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= duration) {
                    cancel();
                    return;
                }
                
                for (Entity entity : player.getWorld().getNearbyEntities(target, radius, radius, radius)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        ((LivingEntity) entity).addPotionEffect(new org.bukkit.potion.PotionEffect(
                            org.bukkit.potion.PotionEffectType.POISON, 40, 2, false, false, true));
                        ((LivingEntity) entity).addPotionEffect(new org.bukkit.potion.PotionEffect(
                            org.bukkit.potion.PotionEffectType.WITHER, 40, 0, false, false, true));
                        ((LivingEntity) entity).damage(5.0);
                    }
                }
                
                // Blok kırma (sadece savaşta olan klan alanlarında)
                if (canModifyTerritory(player, target)) {
                    for (int x = -1; x <= 1; x++) {
                        for (int z = -1; z <= 1; z++) {
                            Block block = target.clone().add(x, 0, z).getBlock();
                            if (block.getType() != Material.BEDROCK && block.getType() != Material.AIR) {
                                block.setType(Material.AIR);
                            }
                        }
                    }
                }
                
                player.getWorld().spawnParticle(org.bukkit.Particle.DRAGON_BREATH, target, 100, radius, 5, radius, 0.1);
                count++;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        
        player.sendMessage("§4Ölüm bulutu başladı!");
    }
    
    private void fireElectricStorm(Player player, Location target, int radius, double damage, int duration, int level) {
        // Elektrik fırtınası (sürekli şok alanı)
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= duration) {
                    cancel();
                    return;
                }
                
                for (Entity entity : player.getWorld().getNearbyEntities(target, radius, radius, radius)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        ((LivingEntity) entity).damage(damage);
                        player.getWorld().spawnParticle(org.bukkit.Particle.ELECTRIC_SPARK, entity.getLocation(), 5, 0.3, 0.3, 0.3, 0.05);
                    }
                }
                
                player.getWorld().spawnParticle(org.bukkit.Particle.ELECTRIC_SPARK, target, 50, radius, 5, radius, 0.1);
                count++;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        
        player.sendMessage("§eElektrik fırtınası başladı!");
    }
    
    private void fireMountainDestroyer(Player player, Location target, int radius, double damage, int areaSize, int level) {
        // Dağ yok edici (30x30 alan, 500 hasar, blok kırma)
        int halfSize = areaSize / 2;
        
        for (int x = -halfSize; x <= halfSize; x++) {
            for (int z = -halfSize; z <= halfSize; z++) {
                Location loc = target.clone().add(x, 0, z);
                
                // Hasar ver
                for (Entity entity : player.getWorld().getNearbyEntities(loc, 2, 2, 2)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        ((LivingEntity) entity).damage(damage / (areaSize * areaSize));
                    }
                }
                
                // Blok kırma (sadece savaşta olan klan alanlarında)
                if (canModifyTerritory(player, loc)) {
                    for (int y = -10; y <= 10; y++) {
                        Block block = loc.clone().add(0, y, 0).getBlock();
                        if (block.getType() != Material.BEDROCK && block.getType() != Material.AIR) {
                            block.setType(Material.AIR);
                        }
                    }
                }
            }
        }
        
        player.getWorld().createExplosion(target, 10.0f, false, false);
        player.sendMessage("§4§lDAĞ YOK EDİCİ AKTİF!");
    }
    
    private void fireLavaTsunami(Player player, Location target, int radius, double damage, int duration, int level) {
        // Lava tufanı (60 saniye sürekli lava)
        int halfSize = radius / 2;
        
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= duration) {
                    cancel();
                    return;
                }
                
                // Lava spawnla (sadece savaşta olan klan alanlarında)
                if (canModifyTerritory(player, target)) {
                    for (int x = -halfSize; x <= halfSize; x++) {
                        for (int z = -halfSize; z <= halfSize; z++) {
                            Location loc = target.clone().add(x, 0, z);
                            Block block = loc.getBlock();
                            if (block.getType() == Material.AIR) {
                                block.setType(Material.LAVA);
                            }
                        }
                    }
                }
                
                // Hasar ver
                for (Entity entity : player.getWorld().getNearbyEntities(target, radius, radius, radius)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        entity.setFireTicks(100);
                        ((LivingEntity) entity).damage(damage / duration);
                    }
                }
                
                player.getWorld().spawnParticle(org.bukkit.Particle.LAVA, target, 100, radius, 5, radius, 0.1);
                count++;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        
        player.sendMessage("§4§lLAVA TUFANI BAŞLADI!");
    }
    
    private void fireBossKiller(Player player, Location target, int radius, double bossDamage, double normalDamage, int level) {
        // Boss katili (bosslara özel hasar)
        BossManager bossManager = plugin.getBossManager();
        
        for (Entity entity : player.getWorld().getNearbyEntities(target, radius, radius, radius)) {
            if (entity instanceof LivingEntity && entity != player) {
                // Boss kontrolü - getBossData ile kontrol et
                boolean isBoss = false;
                if (bossManager != null) {
                    BossData bossData = bossManager.getBossData(entity.getUniqueId());
                    isBoss = (bossData != null);
                }
                
                if (isBoss) {
                    ((LivingEntity) entity).damage(bossDamage);
                    player.sendMessage("§c§lBOSS HASARI: " + bossDamage + " kalp!");
                } else {
                    ((LivingEntity) entity).damage(normalDamage);
                }
            }
        }
        
        player.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_LARGE, target, 1);
        player.sendMessage("§4§lBOSS KATİLİ AKTİF!");
    }
    
    private void fireAreaDestroyer(Player player, Location target, int radius, double damage, int areaSize, int level) {
        // Alan yok edici (30x30 alan, 500 hasar)
        int halfSize = areaSize / 2;
        
        for (int x = -halfSize; x <= halfSize; x++) {
            for (int z = -halfSize; z <= halfSize; z++) {
                Location loc = target.clone().add(x, 0, z);
                
                // Hasar ver
                for (Entity entity : player.getWorld().getNearbyEntities(loc, 2, 2, 2)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        ((LivingEntity) entity).damage(damage / (areaSize * areaSize));
                    }
                }
                
                // Blok kırma (sadece savaşta olan klan alanlarında)
                if (canModifyTerritory(player, loc)) {
                    for (int y = -5; y <= 5; y++) {
                        Block block = loc.clone().add(0, y, 0).getBlock();
                        if (block.getType() != Material.BEDROCK && block.getType() != Material.AIR) {
                            block.setType(Material.AIR);
                        }
                    }
                }
            }
        }
        
        player.getWorld().createExplosion(target, 8.0f, false, false);
        player.sendMessage("§4§lALAN YOK EDİCİ AKTİF!");
    }
    
    private void fireApocalypse(Player player, Location target, int radius, double damage, int areaSize, int level) {
        // Kıyamet (tüm elementlerin kombinasyonu)
        fireMeteorShower(player, target, areaSize, damage / 4, 5, level);
        fireStorm(player, target, areaSize, damage / 4, 5, level);
        fireDeathCloud(player, target, areaSize, 10, level);
        fireIceAge(player, target, areaSize, damage / 4, 10, level);
        
        player.sendMessage("§4§lKIYAMET BAŞLADI!");
    }
    
    // ========== OLUŞTURMA BATARYA METODLARI ==========
    
    private void createObsidianWall(Player player, Location target, int width, int height, int depth, int level) {
        Vector direction = player.getLocation().getDirection().setY(0).normalize();
        Vector perpendicular = new Vector(-direction.getZ(), 0, direction.getX()).normalize();
        
        int placed = 0;
        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                for (int d = 0; d < depth; d++) {
                    Location loc = target.clone().add(perpendicular.clone().multiply(w - width/2))
                        .add(0, h, direction.getZ() * d);
                    Block block = loc.getBlock();
                    if (block.getType() == Material.AIR) {
                        block.setType(Material.OBSIDIAN);
                        placed++;
                    }
                }
            }
        }
        
        player.sendMessage("§5Obsidyen duvar oluşturuldu! (" + placed + " blok)");
    }
    
    private void createStoneBridge(Player player, Location target, int length, int level) {
        Vector direction = player.getLocation().getDirection().setY(0).normalize();
        int placed = 0;
        
        for (int i = 0; i < length; i++) {
            Location loc = target.clone().add(direction.clone().multiply(i));
            Block block = loc.getBlock();
            if (block.getType() == Material.AIR) {
                block.setType(Material.STONE);
                placed++;
            }
        }
        
        player.sendMessage("§7Taş köprü oluşturuldu! (" + placed + " blok)");
    }
    
    private void createIronCage(Player player, Location target, int width, int height, int depth, int level) {
        int placed = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    if (x == 0 || x == width-1 || y == 0 || y == height-1 || z == 0 || z == depth-1) {
                        Location loc = target.clone().add(x - width/2, y, z - depth/2);
                        Block block = loc.getBlock();
                        if (block.getType() == Material.AIR) {
                            block.setType(Material.IRON_BARS);
                            placed++;
                        }
                    }
                }
            }
        }
        
        player.sendMessage("§7Demir kafes oluşturuldu! (" + placed + " blok)");
    }
    
    private void createGlassWall(Player player, Location target, int width, int height, int depth, int level) {
        Vector direction = player.getLocation().getDirection().setY(0).normalize();
        Vector perpendicular = new Vector(-direction.getZ(), 0, direction.getX()).normalize();
        
        int placed = 0;
        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                for (int d = 0; d < depth; d++) {
                    Location loc = target.clone().add(perpendicular.clone().multiply(w - width/2))
                        .add(0, h, direction.getZ() * d);
                    Block block = loc.getBlock();
                    if (block.getType() == Material.AIR) {
                        block.setType(Material.GLASS);
                        placed++;
                    }
                }
            }
        }
        
        player.sendMessage("§bCam duvar oluşturuldu! (" + placed + " blok)");
    }
    
    private void createWoodBarricade(Player player, Location target, int width, int height, int depth, int level) {
        int placed = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    Location loc = target.clone().add(x - width/2, y, z - depth/2);
                    Block block = loc.getBlock();
                    if (block.getType() == Material.AIR) {
                        block.setType(Material.OAK_PLANKS);
                        placed++;
                    }
                }
            }
        }
        
        player.sendMessage("§6Ahşap barikat oluşturuldu! (" + placed + " blok)");
    }
    
    private void createObsidianCage(Player player, Location target, int width, int height, int depth, int level) {
        int placed = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    if (x == 0 || x == width-1 || y == 0 || y == height-1 || z == 0 || z == depth-1) {
                        Location loc = target.clone().add(x - width/2, y, z - depth/2);
                        Block block = loc.getBlock();
                        if (block.getType() == Material.AIR) {
                            block.setType(Material.OBSIDIAN);
                            placed++;
                        }
                    }
                }
            }
        }
        
        player.sendMessage("§5Obsidyen kafes oluşturuldu! (" + placed + " blok)");
    }
    
    private void createIronWall(Player player, Location target, int width, int height, int depth, int level) {
        Vector direction = player.getLocation().getDirection().setY(0).normalize();
        Vector perpendicular = new Vector(-direction.getZ(), 0, direction.getX()).normalize();
        
        int placed = 0;
        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                for (int d = 0; d < depth; d++) {
                    Location loc = target.clone().add(perpendicular.clone().multiply(w - width/2))
                        .add(0, h, direction.getZ() * d);
                    Block block = loc.getBlock();
                    if (block.getType() == Material.AIR) {
                        block.setType(Material.IRON_BLOCK);
                        placed++;
                    }
                }
            }
        }
        
        player.sendMessage("§7Demir duvar oluşturuldu! (" + placed + " blok)");
    }
    
    private void createGlassTunnel(Player player, Location target, int length, int level) {
        Vector direction = player.getLocation().getDirection().setY(0).normalize();
        Vector perpendicular = new Vector(-direction.getZ(), 0, direction.getX()).normalize();
        
        int placed = 0;
        for (int i = 0; i < length; i++) {
            for (int h = 0; h < 3; h++) {
                for (int w = -1; w <= 1; w++) {
                    Location loc = target.clone().add(direction.clone().multiply(i))
                        .add(perpendicular.clone().multiply(w)).add(0, h, 0);
                    Block block = loc.getBlock();
                    if (block.getType() == Material.AIR) {
                        if (h == 0 || h == 2 || w == -1 || w == 1) {
                            block.setType(Material.GLASS);
                            placed++;
                        }
                    }
                }
            }
        }
        
        player.sendMessage("§bCam tünel oluşturuldu! (" + placed + " blok)");
    }
    
    private void createWoodCastle(Player player, Location target, int width, int height, int depth, int level) {
        int placed = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    if (x == 0 || x == width-1 || y == 0 || y == height-1 || z == 0 || z == depth-1) {
                        Location loc = target.clone().add(x - width/2, y, z - depth/2);
                        Block block = loc.getBlock();
                        if (block.getType() == Material.AIR) {
                            block.setType(Material.OAK_PLANKS);
                            placed++;
                        }
                    }
                }
            }
        }
        
        player.sendMessage("§6Ahşap kale oluşturuldu! (" + placed + " blok)");
    }
    
    private void createNetheriteBridge(Player player, Location target, int length, int level) {
        Vector direction = player.getLocation().getDirection().setY(0).normalize();
        int placed = 0;
        
        for (int i = 0; i < length; i++) {
            Location loc = target.clone().add(direction.clone().multiply(i));
            Block block = loc.getBlock();
            if (block.getType() == Material.AIR) {
                block.setType(Material.NETHERITE_BLOCK);
                placed++;
            }
        }
        
        player.sendMessage("§5Netherite köprü oluşturuldu! (" + placed + " blok)");
    }
    
    private void createIronPrison(Player player, Location target, int width, int height, int depth, int level) {
        int placed = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    if (x == 0 || x == width-1 || y == 0 || y == height-1 || z == 0 || z == depth-1) {
                        Location loc = target.clone().add(x - width/2, y, z - depth/2);
                        Block block = loc.getBlock();
                        if (block.getType() == Material.AIR) {
                            block.setType(Material.IRON_BLOCK);
                            placed++;
                        }
                    }
                }
            }
        }
        
        player.sendMessage("§7Demir hapishane oluşturuldu! (" + placed + " blok)");
    }
    
    private void createGlassTower(Player player, Location target, int width, int height, int depth, int level) {
        int placed = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    if (x == 0 || x == width-1 || y == 0 || y == height-1 || z == 0 || z == depth-1) {
                        Location loc = target.clone().add(x - width/2, y, z - depth/2);
                        Block block = loc.getBlock();
                        if (block.getType() == Material.AIR) {
                            block.setType(Material.GLASS);
                            placed++;
                        }
                    }
                }
            }
        }
        
        player.sendMessage("§bCam kule oluşturuldu! (" + placed + " blok)");
    }
    
    private void createStoneCastle(Player player, Location target, int width, int height, int depth, int level) {
        int placed = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    if (x == 0 || x == width-1 || y == 0 || y == height-1 || z == 0 || z == depth-1) {
                        Location loc = target.clone().add(x - width/2, y, z - depth/2);
                        Block block = loc.getBlock();
                        if (block.getType() == Material.AIR) {
                            block.setType(Material.STONE);
                            placed++;
                        }
                    }
                }
            }
        }
        
        player.sendMessage("§7Taş kale oluşturuldu! (" + placed + " blok)");
    }
    
    private void createObsidianCastle(Player player, Location target, int width, int height, int depth, int level) {
        int placed = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    if (x == 0 || x == width-1 || y == 0 || y == height-1 || z == 0 || z == depth-1) {
                        Location loc = target.clone().add(x - width/2, y, z - depth/2);
                        Block block = loc.getBlock();
                        if (block.getType() == Material.AIR) {
                            block.setType(Material.OBSIDIAN);
                            placed++;
                        }
                    }
                }
            }
        }
        
        player.sendMessage("§5Obsidyen kale oluşturuldu! (" + placed + " blok)");
    }
    
    private void createStoneFortress(Player player, Location target, int width, int height, int depth, int level) {
        int placed = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    if (x == 0 || x == width-1 || y == 0 || y == height-1 || z == 0 || z == depth-1) {
                        Location loc = target.clone().add(x - width/2, y, z - depth/2);
                        Block block = loc.getBlock();
                        if (block.getType() == Material.AIR) {
                            block.setType(Material.STONE);
                            placed++;
                        }
                    }
                }
            }
        }
        
        player.sendMessage("§7Taş şato oluşturuldu! (" + placed + " blok)");
    }
    
    private void createObsidianPrison(Player player, Location target, int width, int height, int depth, int level) {
        int placed = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    if (x == 0 || x == width-1 || y == 0 || y == height-1 || z == 0 || z == depth-1) {
                        Location loc = target.clone().add(x - width/2, y, z - depth/2);
                        Block block = loc.getBlock();
                        if (block.getType() == Material.AIR) {
                            block.setType(Material.OBSIDIAN);
                            placed++;
                        }
                    }
                }
            }
        }
        
        player.sendMessage("§5Obsidyen hapishane oluşturuldu! (" + placed + " blok)");
    }
    
    private void createIronCastle(Player player, Location target, int width, int height, int depth, int level) {
        int placed = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    if (x == 0 || x == width-1 || y == 0 || y == height-1 || z == 0 || z == depth-1) {
                        Location loc = target.clone().add(x - width/2, y, z - depth/2);
                        Block block = loc.getBlock();
                        if (block.getType() == Material.AIR) {
                            block.setType(Material.IRON_BLOCK);
                            placed++;
                        }
                    }
                }
            }
        }
        
        player.sendMessage("§7Demir kale oluşturuldu! (" + placed + " blok)");
    }
    
    // ========== DESTEK BATARYA METODLARI ==========
    
    private void applyHealSupport(Player player, double radius, double healAmount, int level) {
        List<Player> members = getNearbyClanMembers(player, radius);
        members.add(player); // Kendini de ekle
        
        for (Player member : members) {
            if (healAmount < 0) {
                // Efsanevi: Tam can + 50 ekstra
                member.setHealth(member.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue());
                member.setAbsorptionAmount(50.0f);
            } else {
                // Normal can yenileme
                double newHealth = Math.min(
                    member.getHealth() + healAmount,
                    member.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue()
                );
                member.setHealth(newHealth);
            }
        }
        
        player.sendMessage("§aCan yenileme uygulandı! (" + members.size() + " oyuncu)");
    }
    
    private void applySpeedSupport(Player player, double radius, int amplifier, int duration, int level) {
        List<Player> members = getNearbyClanMembers(player, radius);
        members.add(player);
        
        for (Player member : members) {
            member.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.SPEED, duration * 20, amplifier, false, false, true));
        }
        
        player.sendMessage("§eHız artışı uygulandı! (" + members.size() + " oyuncu)");
    }
    
    private void applyDamageSupport(Player player, double radius, int amplifier, int duration, int level) {
        List<Player> members = getNearbyClanMembers(player, radius);
        members.add(player);
        
        for (Player member : members) {
            member.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.INCREASE_DAMAGE, duration * 20, amplifier, false, false, true));
        }
        
        player.sendMessage("§cHasar artışı uygulandı! (" + members.size() + " oyuncu)");
    }
    
    private void applyArmorSupport(Player player, double radius, int amplifier, int duration, int level) {
        List<Player> members = getNearbyClanMembers(player, radius);
        members.add(player);
        
        for (Player member : members) {
            member.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.DAMAGE_RESISTANCE, duration * 20, amplifier, false, false, true));
        }
        
        player.sendMessage("§bZırh artışı uygulandı! (" + members.size() + " oyuncu)");
    }
    
    private void applyRegenerationSupport(Player player, double radius, int amplifier, int duration, int level) {
        List<Player> members = getNearbyClanMembers(player, radius);
        members.add(player);
        
        for (Player member : members) {
            member.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.REGENERATION, duration * 20, amplifier, false, false, true));
        }
        
        player.sendMessage("§dYenilenme uygulandı! (" + members.size() + " oyuncu)");
    }
}
