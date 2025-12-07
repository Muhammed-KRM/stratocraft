package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Yeni Boss Arena Yönetim Sistemi
 * Boss'un etrafında sürekli değişen, dinamik arena
 */
public class NewBossArenaManager {
    private final Main plugin;
    private final Map<UUID, ArenaData> activeArenas;
    private final Map<UUID, BukkitTask> arenaTasks;
    
    public NewBossArenaManager(Main plugin) {
        this.plugin = plugin;
        this.activeArenas = new ConcurrentHashMap<>();
        this.arenaTasks = new ConcurrentHashMap<>();
    }
    
    /**
     * Arena transformasyonunu başlat
     */
    public void startArenaTransformation(Location center, BossManager.BossType bossType, int level, UUID bossId) {
        // Arena verisi oluştur
        ArenaData arena = new ArenaData(center, bossType, level, bossId);
        activeArenas.put(bossId, arena);
        
        // Sürekli transformasyon görevini başlat
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                BossManager.BossData bossData = plugin.getBossManager().getBossData(bossId);
                if (bossData == null || bossData.getEntity() == null || bossData.getEntity().isDead()) {
                    stopArenaTransformation(bossId);
                    cancel();
                    return;
                }
                
                // Boss'un güncel konumunu al
                Location bossLoc = bossData.getEntity().getLocation();
                arena.setCenter(bossLoc);
                
                // Not: Eskiden burada boss'un altını STONE ile dolduruyorduk (fillBelowBoss),
                // bu da sütun / kule oluşumuna sebep oluyordu. Artık tamamen kapatıldı.
                
                // Arena transformasyonunu gerçekleştir
                transformArenaBlocks(arena);
            }
        }.runTaskTimer(plugin, 0L, 20L); // Her saniye
        
        arenaTasks.put(bossId, task);
        
        plugin.getLogger().info("Boss arena transformasyonu başlatıldı: " + bossId);
    }
    
    /**
     * Arena transformasyonunu durdur
     */
    public void stopArenaTransformation(UUID bossId) {
        BukkitTask task = arenaTasks.remove(bossId);
        if (task != null) {
            task.cancel();
        }
        
        activeArenas.remove(bossId);
        plugin.getLogger().info("Boss arena transformasyonu durduruldu: " + bossId);
    }
    
    /**
     * Arena bloklarını dönüştür (Sürekli - YAYILMA MEKANİZMASI)
     * Boss'tan başlayarak dışa doğru yayılır
     */
    private void transformArenaBlocks(ArenaData arena) {
        Location center = arena.getCenter();
        int maxRadius = getArenaRadius(arena.getLevel());
        int blocksPerCycle = 15; // Her döngüde 15 blok değiştir

        // Kuleler henüz oluşturulmadıysa, bir kez oluştur (oyuncunun hareketini zorlaştıran yapı)
        if (!arena.isTowersCreated()) {
            createBossTowers(arena, maxRadius);
            arena.setTowersCreated(true);
        }
        
        // Mevcut genişleme yarıçapını artır (yavaşça)
        double currentRadius = arena.getCurrentRadius();
        if (currentRadius < maxRadius) {
            arena.setCurrentRadius(currentRadius + 0.5); // Her saniye 0.5 blok genişle
        }
        
        // Şu anki radius'ta blokları dönüştür
        for (int i = 0; i < blocksPerCycle; i++) {
            // Rastgele açı seç (çember oluşturmak için)
            double angle = Math.random() * 2 * Math.PI;
            
            // Mevcut radius civarında blok seç (yayılma efekti)
            double distance = currentRadius + (Math.random() * 3 - 1.5); // ±1.5 blok tolerans
            if (distance < 3) distance = 3; // Boss'a çok yakın olma
            if (distance > maxRadius) distance = maxRadius; // Max radius'u geçme
            
            int x = (int) (center.getX() + distance * Math.cos(angle));
            int z = (int) (center.getZ() + distance * Math.sin(angle));
            
            // Y koordinatını bul (ZEMİN bloğunun kendisi)
            int y = findGroundLevel(center.getWorld(), x, center.getBlockY(), z);
            
            if (y == -1) continue; // Zemin bulunamadı
            
            // ZEMİN BLOĞUNU DEĞİŞTİR (hava bloğu değil!)
            Location blockLoc = new Location(center.getWorld(), x, y, z);
            
            // Boss'a çok yakınsa atlama
            if (blockLoc.distance(center) < 3) continue;
            
            // Bloğu dönüştür
            transformSingleBlock(blockLoc, arena);
        }
    }

    /**
     * Boss'un etrafına kuleler oluşturur.
     * - 50 blok yarıçap içinde
     * - Yükseklik: 3, 5, 8, 10
     * - Kalınlık (kare taban): 1, 2, 4, 6
     * - Malzeme boss tipine göre seçilir (ör: Titan Golem demir, Chaos God obsidyen)
     */
    private void createBossTowers(ArenaData arena, int arenaRadius) {
        Location center = arena.getCenter();
        World world = center.getWorld();
        if (world == null) return;

        BossManager.BossType type = arena.getBossType();
        int level = arena.getLevel();

        int maxRadius = Math.min(50, arenaRadius);
        if (maxRadius < 8) maxRadius = 8; // minimum mantıklı alan

        Random random = new Random();
        int towerCount = 6 + random.nextInt(5); // 6–10 kule

        int[] heights = {3, 5, 8, 10};
        int[] widths = {1, 2, 4, 6};

        Material towerMaterial = getTowerMaterial(type, level);

        for (int i = 0; i < towerCount; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = 6 + random.nextDouble() * (maxRadius - 10); // boss'tan en az 6 blok uzakta

            int baseX = (int) (center.getX() + distance * Math.cos(angle));
            int baseZ = (int) (center.getZ() + distance * Math.sin(angle));

            int baseY = findGroundLevel(world, baseX, center.getBlockY(), baseZ);
            if (baseY == -1) continue;

            int height = heights[random.nextInt(heights.length)];
            int width = widths[random.nextInt(widths.length)];
            int half = width / 2;

            for (int dx = -half; dx <= half; dx++) {
                for (int dz = -half; dz <= half; dz++) {
                    for (int dy = 0; dy < height; dy++) {
                        Block block = world.getBlockAt(baseX + dx, baseY + dy, baseZ + dz);
                        if (!block.getChunk().isLoaded()) continue;

                        // Sadece hava veya geçilebilir blokların yerine kule bloğu koy
                        if (!block.getType().isSolid() || block.getType() == Material.AIR) {
                            block.setType(towerMaterial);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Zemin seviyesini bul (ZEMİN BLOĞUNUN KENDİSİNİN Y'sini döndürür, hava bloğunu değil!)
     */
    private int findGroundLevel(World world, int x, int startY, int z) {
        // Aşağıya doğru tara - ZEMİN BLOĞUNU BUL
        for (int y = startY; y > startY - 10 && y > world.getMinHeight(); y--) {
            Block currentBlock = world.getBlockAt(x, y, z);
            
            // Bu blok katı mı? (zemin bloğu)
            if (currentBlock.getType().isSolid() && !currentBlock.getType().isAir()) {
                return y; // ZEMİN BLOĞUNUN Y'sini döndür
            }
        }
        
        // Yukarıya doğru tara
        for (int y = startY; y < startY + 10 && y < world.getMaxHeight(); y++) {
            Block currentBlock = world.getBlockAt(x, y, z);
            
            if (currentBlock.getType().isSolid() && !currentBlock.getType().isAir()) {
                return y; // ZEMİN BLOĞUNUN Y'sini döndür
            }
        }
        
        return -1; // Zemin bulunamadı
    }
    
    /**
     * Tek bir bloğu dönüştür (ZEMİN BLOĞUNU DEĞİŞTİRİR - ASLA HAVA BLOĞU YAPMA)
     */
    private void transformSingleBlock(Location location, ArenaData arena) {
        Block block = location.getBlock();
        
        // Chunk yüklü değilse atlama
        if (!block.getChunk().isLoaded()) return;
        
        // Hava bloğu ise atlama (zemin bloğu olmalı)
        if (block.getType() == Material.AIR) return;
        
        // Boss'un çok yakınındaki bloklara hiç dokunma (altını doldurma/kazma tamamen kapalı)
        Location bossLoc = arena.getCenter();
        double horizontalDist = Math.sqrt(
            Math.pow(location.getX() - bossLoc.getX(), 2) + 
            Math.pow(location.getZ() - bossLoc.getZ(), 2)
        );
        if (horizontalDist < 5) {
            return;
        }
        
        // ZEMIN BLOĞUNU DEĞİŞTİR (ASLA HAVA BLOĞU YAPMA!)
        Material oldMaterial = block.getType();
        Material newMaterial = getArenaMaterial(arena.getBossType(), arena.getLevel(), false);
        
        // KRİTİK: Yeni malzeme AIR olmamalı! (çukur oluşmasını engeller)
        if (newMaterial == Material.AIR || !newMaterial.isSolid()) {
            return; // Hava bloğu yapmayı engelle!
        }
        
        // Aynı malzeme ise değiştirme (performans)
        if (oldMaterial == newMaterial) return;
        
        block.setType(newMaterial);
        
        // Efekt ekle (değişim anı)
        if (Math.random() < 0.15) { // %15 şansla
            location.getWorld().spawnParticle(
                Particle.BLOCK_CRACK, 
                location.clone().add(0.5, 1.0, 0.5), 
                15, 
                0.4, 0.4, 0.4, 
                0.1, 
                oldMaterial.createBlockData()
            );
            
            // Ses efekti
            location.getWorld().playSound(location, Sound.BLOCK_STONE_BREAK, 0.3f, 0.8f);
        }
    }
    
    /**
     * Arena materyalini al (Boss tipine göre)
     */
    private Material getArenaMaterial(BossManager.BossType bossType, int level, boolean filling) {
        // Doldurma için güvenli malzeme
        if (filling) {
            return Material.STONE;
        }
        
        // Boss tipine göre malzemeler
        List<Material> materials = new ArrayList<>();
        
                switch (bossType) {
            case GOBLIN_KING:
            case ORC_CHIEF:
                // Seviye 1-2: Basit malzemeler
                materials.add(Material.DIRT);
                materials.add(Material.COARSE_DIRT);
                materials.add(Material.GRAVEL);
                materials.add(Material.COBBLESTONE);
                materials.add(Material.STONE);
                break;
                
            case TROLL_KING:
                // Seviye 2: Orman ve taş
                materials.add(Material.GRASS_BLOCK);
                materials.add(Material.PODZOL);
                materials.add(Material.MOSSY_COBBLESTONE);
                materials.add(Material.STONE);
                materials.add(Material.ANDESITE);
                break;
                
            case DRAGON:
                // Seviye 3: Yanık ve kayalık
                materials.add(Material.NETHERRACK);
                materials.add(Material.BLACKSTONE);
                materials.add(Material.BASALT);
                materials.add(Material.MAGMA_BLOCK);
                // KRİTİK: LAVA KALDIRMA (çukur oluşturur!)
                // if (Math.random() < 0.1) materials.add(Material.LAVA); // KALDIRILDI
                break;
                
            case TITAN_GOLEM:
                // Seviye 4: Taş ve maden
                materials.add(Material.STONE);
                materials.add(Material.DIORITE);
                materials.add(Material.GRANITE);
                materials.add(Material.DEEPSLATE);
                materials.add(Material.IRON_BLOCK);
                if (Math.random() < 0.05) materials.add(Material.OBSIDIAN);
                // KRİTİK: LAVA VE MAGMA BLOĞU KALDIRMA (çukur oluşturabilir)
                break;
                
            case VOID_DRAGON:
                // Seviye 5: End malzemeleri
                materials.add(Material.END_STONE);
                materials.add(Material.END_STONE_BRICKS);
                materials.add(Material.PURPUR_BLOCK);
                materials.add(Material.OBSIDIAN);
                if (Math.random() < 0.1) materials.add(Material.CRYING_OBSIDIAN);
                break;
                
            default:
                materials.add(Material.STONE);
        }
        
        // Seviyeye göre ek malzemeler
        if (level >= 3 && Math.random() < 0.15) {
            materials.add(Material.COBWEB); // Örümcek ağı
        }
        
        if (level >= 4 && Math.random() < 0.1) {
            materials.add(Material.MAGMA_BLOCK); // Magma bloğu
        }
        
        if (level >= 5 && Math.random() < 0.05) {
            materials.add(Material.NETHERITE_BLOCK); // Netherite bloğu
        }
        
        return materials.get(new Random().nextInt(materials.size()));
    }

    /**
     * Kule malzemesini boss tipine göre seç.
     */
    private Material getTowerMaterial(BossManager.BossType bossType, int level) {
        switch (bossType) {
            case TITAN_GOLEM:
                return Material.IRON_BLOCK;
            case CHAOS_GOD:
            case CHAOS_TITAN:
                return Material.OBSIDIAN;
            case VOID_DRAGON:
                return Material.END_STONE_BRICKS;
            case DRAGON:
            case HELL_DRAGON:
                return Material.NETHERRACK;
            case HYDRA:
                return Material.PRISMARINE_BRICKS;
            case PHOENIX:
                return Material.MAGMA_BLOCK;
            case GOBLIN_KING:
            case ORC_CHIEF:
            case TROLL_KING:
                return Material.COBBLESTONE;
            default:
                return Material.STONE_BRICKS;
        }
    }
    
    /**
     * Boss'un altını doldurma sistemi.
     *
     * NOT: Daha önce burada boss'un altındaki boşlukları STONE ile dolduruyorduk,
     * bu da boss zıpladıkça yukarı doğru taş kuleler oluşmasına sebep oluyordu.
     * Oyuncu isteği üzerine bu mantık TAMAMEN DEVRE DIŞI bırakıldı.
     */
    private void fillBelowBoss(Location bossLoc) {
        // Bilinçli olarak boş bırakıldı.
    }
    
    /**
     * Arena yarıçapını al
     */
    private int getArenaRadius(int level) {
        switch (level) {
            case 1: return 15;
            case 2: return 20;
            case 3: return 25;
            case 4: return 30;
            case 5: return 35;
            default: return 20;
        }
    }
    
    /**
     * Arena verisi (YAYILMA RADIUS'U EKLENDİ)
     */
    private static class ArenaData {
        private Location center;
        private final BossManager.BossType bossType;
        private final int level;
        private final UUID bossId;
        private double currentRadius; // Mevcut yayılma yarıçapı
        private boolean towersCreated; // Kuleler bir kez oluşturuldu mu?
        
        public ArenaData(Location center, BossManager.BossType bossType, int level, UUID bossId) {
            this.center = center;
            this.bossType = bossType;
            this.level = level;
            this.bossId = bossId;
            this.currentRadius = 3.0; // Boss'un 3 blok çevresinden başla
            this.towersCreated = false;
        }
        
        public Location getCenter() { return center; }
        public void setCenter(Location newCenter) { 
            // Boss hareket edince radius'u sıfırla (yeni yerden başlat)
            // 5 BLOK hareket = yeni alan oluştur
            if (center.distance(newCenter) > 5) {
                this.currentRadius = 3.0;
            }
            this.center = newCenter; 
        }
        public BossManager.BossType getBossType() { return bossType; }
        public int getLevel() { return level; }
        public UUID getBossId() { return bossId; }
        public double getCurrentRadius() { return currentRadius; }
        public void setCurrentRadius(double radius) { this.currentRadius = radius; }
        public boolean isTowersCreated() { return towersCreated; }
        public void setTowersCreated(boolean towersCreated) { this.towersCreated = towersCreated; }
    }
}

