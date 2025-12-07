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
    
    // Performans ayarları - HIZLANDIRILMIŞ OPTİMİZASYON
    private static final int MAX_ACTIVE_ARENAS = 50; // Maksimum aktif arena sayısı (performans için artırıldı)
    private static final long TASK_INTERVAL = 40L; // Her 2 saniyede bir (20 tick = 1 saniye) - HIZLANDIRILDI
    private static final int BLOCKS_PER_CYCLE = 8; // Her döngüde 8 blok (3'ten artırıldı - HIZLANDIRILDI)
    private static final int HAZARD_CREATE_INTERVAL = 5; // Her 5 döngüde bir tehlike oluştur
    private static final double FAR_DISTANCE = 100.0; // 100 blok içindeki arenalar aktif, dışındakiler pasif
    
    private BukkitTask centralTask; // Merkezi task (tüm arenaları yönetir)
    private int globalCycleCount = 0; // Global döngü sayacı
    
    public NewBossArenaManager(Main plugin) {
        this.plugin = plugin;
        this.activeArenas = new ConcurrentHashMap<>();
        startCentralArenaTask(); // Merkezi task'ı başlat
    }
    
    /**
     * Arena transformasyonunu başlat
     * Artık merkezi task sistemi kullanılıyor - her arena için ayrı task yok
     */
    public void startArenaTransformation(Location center, BossManager.BossType bossType, int level, UUID bossId) {
        // Maksimum arena limiti kontrolü
        if (activeArenas.size() >= MAX_ACTIVE_ARENAS) {
            plugin.getLogger().warning("Maksimum arena sayısına ulaşıldı (" + MAX_ACTIVE_ARENAS + "). Yeni arena oluşturulamıyor.");
            return;
        }
        
        // Arena verisi oluştur
        ArenaData arena = new ArenaData(center, bossType, level, bossId);
        activeArenas.put(bossId, arena);
        
        plugin.getLogger().info("Boss arena transformasyonu başlatıldı: " + bossId);
    }
    
    /**
     * Merkezi arena task'ı - Tüm arenaları mesafeye göre önceliklendirir
     */
    private void startCentralArenaTask() {
        if (centralTask != null && !centralTask.isCancelled()) {
            return; // Zaten çalışıyor
        }
        
        centralTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (activeArenas.isEmpty()) {
                    return; // Arena yoksa işlem yapma
                }
                
                // Tüm oyuncuları al
                List<org.bukkit.entity.Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
                
                // Her arena için en yakın oyuncu mesafesini hesapla ve önceliklendir
                List<Map.Entry<UUID, ArenaData>> sortedArenas = new ArrayList<>(activeArenas.entrySet());
                
                // Önce mesafeleri hesapla ve sırala
                for (Map.Entry<UUID, ArenaData> entry : sortedArenas) {
                    ArenaData arena = entry.getValue();
                    Location center = arena.getCenter();
                    World world = center.getWorld();
                    
                    if (world == null) continue;
                    
                    // En yakın oyuncu mesafesini bul
                    double minDistance = Double.MAX_VALUE;
                    for (org.bukkit.entity.Player player : players) {
                        if (!player.getWorld().equals(world)) continue;
                        double dist = player.getLocation().distance(center);
                        if (dist < minDistance) {
                            minDistance = dist;
                        }
                    }
                    
                    // Arena'ya mesafeyi kaydet
                    arena.setNearestPlayerDistance(minDistance);
                }
                
                // Mesafeye göre sırala (en yakın önce)
                sortedArenas.sort(Comparator.comparingDouble(e -> e.getValue().getNearestPlayerDistance()));
                
                // Öncelikli arenaları işle (en yakın 20 arena) - HIZLANDIRILDI
                int processed = 0;
                int maxProcessPerCycle = 20; // Her döngüde maksimum 20 arena işle (10'dan artırıldı)
                
                for (Map.Entry<UUID, ArenaData> entry : sortedArenas) {
                    if (processed >= maxProcessPerCycle) break;
                    
                    UUID bossId = entry.getKey();
                    ArenaData arena = entry.getValue();
                    
                    // Boss kontrolü
                    BossManager.BossData bossData = plugin.getBossManager().getBossData(bossId);
                    if (bossData == null || bossData.getEntity() == null || bossData.getEntity().isDead()) {
                        stopArenaTransformation(bossId);
                        continue;
                    }
                    
                    // Boss'un güncel konumunu al
                    Location bossLoc = bossData.getEntity().getLocation();
                    arena.setCenter(bossLoc);
                    
                    // Arena transformasyonunu gerçekleştir (mesafeye göre önceliklendirilmiş)
                    transformArenaBlocks(arena, globalCycleCount);
                    
                    processed++;
                }
                
                globalCycleCount++;
            }
        }.runTaskTimer(plugin, 0L, TASK_INTERVAL);
    }
    
    /**
     * Arena transformasyonunu durdur
     */
    public void stopArenaTransformation(UUID bossId) {
        activeArenas.remove(bossId);
        plugin.getLogger().info("Boss arena transformasyonu durduruldu: " + bossId);
    }
    
    /**
     * Arena bloklarını dönüştür (Sürekli - YAYILMA MEKANİZMASI)
     * Boss'tan başlayarak dışa doğru yayılır
     * PERFORMANS OPTİMİZE EDİLDİ: Oyuncu kontrolü esnetildi
     */
    private void transformArenaBlocks(ArenaData arena, int cycleCount) {
        Location center = arena.getCenter();
        World world = center.getWorld();
        if (world == null) return;
        
        // En yakın oyuncu mesafesini al (merkezi task tarafından hesaplanmış)
        double nearestPlayerDistance = arena.getNearestPlayerDistance();
        boolean isNearArena = nearestPlayerDistance <= FAR_DISTANCE; // 100 blok içindeyse aktif
        
        // UZAK ARENALAR (100+ blok) İÇİN HİÇBİR ŞEY YAPILMAZ - ERKEN ÇIKIŞ
        if (!isNearArena) {
            return; // Uzak arenalar için hiçbir transformasyon yapılmaz
        }
        
        int maxRadius = getArenaRadius(arena.getLevel());

        // Kuleler sürekli oluşturulur (yayılma gibi) - Dakikada 1 kere
        // Task interval = 40 tick (2 saniye), dakikada 1 = 60 saniye = 30 döngü
        if (cycleCount % 30 == 0) {
            createBossTowers(arena, maxRadius);
        }
        
        // Rastgele örümcek ağları, lavlar ve sular oluştur (3 KAT ARTTIRILDI)
        // Sadece yakın arenalarda oluşturulur
        if (cycleCount % HAZARD_CREATE_INTERVAL == 0) {
            createRandomHazards(arena, 50);
        }
        
        // Mevcut genişleme yarıçapını artır (3 KAT HIZLANDIRILDI)
        // Sadece yakın arenalarda genişler
        double currentRadius = arena.getCurrentRadius();
        if (currentRadius < maxRadius) {
            arena.setCurrentRadius(currentRadius + 1.2); // Her 2 saniyede 1.2 blok genişle (3 KAT HIZLANDIRILDI - 0.4'ten)
        }
        
        // Şu anki radius'ta blokları dönüştür (daha az blok)
        // Sadece yakın arenalarda blok transformasyonu yapılır
        
        int transformed = 0;
        int maxAttempts = BLOCKS_PER_CYCLE * 3; // Maksimum deneme sayısı (chunk yüklü değilse atla) - optimize edildi
        
        for (int attempt = 0; attempt < maxAttempts && transformed < BLOCKS_PER_CYCLE; attempt++) {
            // Rastgele açı seç (çember oluşturmak için)
            double angle = Math.random() * 2 * Math.PI;
            
            // Mevcut radius civarında blok seç (yayılma efekti)
            double distance = currentRadius + (Math.random() * 3 - 1.5); // ±1.5 blok tolerans
            if (distance < 3) distance = 3; // Boss'a çok yakın olma
            if (distance > maxRadius) distance = maxRadius; // Max radius'u geçme
            
            int x = (int) (center.getX() + distance * Math.cos(angle));
            int z = (int) (center.getZ() + distance * Math.sin(angle));
            
            // KRİTİK: Chunk yüklü mü kontrol et (performans için - önce chunk kontrolü)
            Chunk chunk = world.getChunkAt(x >> 4, z >> 4);
            if (!chunk.isLoaded()) {
                continue; // Chunk yüklü değilse atla
            }
            
            // Y koordinatını bul (ZEMİN bloğunun kendisi) - optimize edilmiş
            int y = findGroundLevelOptimized(world, x, center.getBlockY(), z);
            
            if (y == -1) continue; // Zemin bulunamadı
            
            // ZEMİN BLOĞUNU DEĞİŞTİR (hava bloğu değil!)
            Location blockLoc = new Location(world, x, y, z);
            
            // Boss'a çok yakınsa atlama
            if (blockLoc.distance(center) < 3) continue;
            
            // Bloğu dönüştür
            transformSingleBlock(blockLoc, arena);
            transformed++;
        }
    }

    /**
     * Boss'un 50 blok etrafında rastgele örümcek ağları, lavlar ve sular oluşturur.
     * Oyuncunun hareketini zorlaştıran çevresel tehlikeler.
     * PERFORMANS OPTİMİZE EDİLDİ: Daha az tehlike, chunk kontrolü
     * NOT: Oyuncu kontrolü çağıran metod tarafından yapılıyor
     */
    private void createRandomHazards(ArenaData arena, int maxRadius) {
        Location center = arena.getCenter();
        World world = center.getWorld();
        if (world == null) return;
        
        Random random = new Random();
        
        // Her döngüde rastgele sayıda tehlike oluştur (3 KAT ARTTIRILDI)
        int hazardCount = 6 + random.nextInt(4); // 6-9 tehlike (3 KAT ARTTIRILDI - 2-3'ten)
        
        for (int i = 0; i < hazardCount; i++) {
            // Rastgele açı ve mesafe
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = 8 + random.nextDouble() * (maxRadius - 8); // Boss'tan en az 8 blok uzakta
            
            int x = (int) (center.getX() + distance * Math.cos(angle));
            int z = (int) (center.getZ() + distance * Math.sin(angle));
            
            // Zemin seviyesini bul (optimize edilmiş)
            int groundY = findGroundLevelOptimized(world, x, center.getBlockY(), z);
            if (groundY == -1) continue;
            
            // Boss'a çok yakınsa atlama
            Location hazardLoc = new Location(world, x, groundY, z);
            if (hazardLoc.distance(center) < 5) continue;
            
            // KRİTİK: Chunk yüklü mü kontrol et (performans için - önce chunk kontrolü)
            Chunk chunk = world.getChunkAt(x >> 4, z >> 4);
            if (!chunk.isLoaded()) continue;
            
            // Rastgele tehlike tipi seç - DAHA ÇOK LAV VE ÖRÜMCEK AĞI
            double hazardType = random.nextDouble();
            
            if (hazardType < 0.5) {
                // %50 şans: Örümcek ağı (zemin üzerinde veya havada) - ARTTIRILDI
                int webY = groundY + 1 + random.nextInt(4); // Zemin + 1-4 blok yukarıda (artırıldı)
                if (webY < world.getMaxHeight()) {
                    Block webBlock = world.getBlockAt(x, webY, z);
                    if (webBlock.getType() == Material.AIR) {
                        webBlock.setType(Material.COBWEB);
                    }
                }
            } else if (hazardType < 0.9) {
                // %40 şans: Lav (zemin seviyesinde) - ARTTIRILDI
                Block lavaBlock = world.getBlockAt(x, groundY + 1, z);
                if (lavaBlock.getType() == Material.AIR) {
                    // Lava kaynağı oluştur (sadece kaynak, akan lav değil)
                    lavaBlock.setType(Material.LAVA);
                }
            } else {
                // %10 şans: Su (zemin seviyesinde) - AZALTILDI
                Block waterBlock = world.getBlockAt(x, groundY + 1, z);
                if (waterBlock.getType() == Material.AIR) {
                    // Su kaynağı oluştur (sadece kaynak, akan su değil)
                    waterBlock.setType(Material.WATER);
                }
            }
        }
    }
    
    /**
     * Boss'un etrafına kuleler oluşturur.
     * - 50 blok yarıçap içinde
     * - Yükseklik: 3, 5, 8, 10
     * - Kalınlık (kare taban): 1, 2, 4, 6
     * - Malzeme boss tipine göre seçilir (ör: Titan Golem demir, Chaos God obsidyen)
     * PERFORMANS OPTİMİZE EDİLDİ: Daha az kule, chunk kontrolü önceden
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
        // Sürekli oluşturma için daha az kule (tek seferde yeterli, ama sürekli oluşmaya devam etsin)
        int towerCount = 5 + random.nextInt(5); // 3-5 kule (dakikada 1 kere oluşturulur)

        // ÇEŞİTLENDİRİLMİŞ KULE BOYUTLARI - Daha fazla çeşitlilik
        int[] heights = {2, 3, 4, 5, 6, 7, 8, 10, 12, 15}; // Daha kısa ve uzun kuleler
        int[] widths = {1, 1, 2, 2, 3, 4, 5, 6}; // Tek blok, 2 blok, daha geniş kuleler

        Material towerMaterial = getTowerMaterial(type, level);

        for (int i = 0; i < towerCount; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = 6 + random.nextDouble() * (maxRadius - 10); // boss'tan en az 6 blok uzakta

            int baseX = (int) (center.getX() + distance * Math.cos(angle));
            int baseZ = (int) (center.getZ() + distance * Math.sin(angle));

            // KRİTİK: Chunk yüklü mü kontrol et (performans için - önce chunk kontrolü)
            Chunk chunk = world.getChunkAt(baseX >> 4, baseZ >> 4);
            if (!chunk.isLoaded()) continue; // Chunk yüklü değilse bu kuleyi atla
            
            int baseY = findGroundLevelOptimized(world, baseX, center.getBlockY(), baseZ);
            if (baseY == -1) continue;

            int height = heights[random.nextInt(heights.length)];
            int width = widths[random.nextInt(widths.length)];
            int half = width / 2;

            // Kule bloklarını oluştur
            for (int dx = -half; dx <= half; dx++) {
                for (int dz = -half; dz <= half; dz++) {
                    for (int dy = 0; dy < height; dy++) {
                        int blockX = baseX + dx;
                        int blockZ = baseZ + dz;
                        int blockY = baseY + dy;
                        
                        // Aynı chunk içinde mi kontrol et (performans)
                        if ((blockX >> 4) != (baseX >> 4) || (blockZ >> 4) != (baseZ >> 4)) {
                            continue; // Farklı chunk'a geçme
                        }
                        
                        Block block = world.getBlockAt(blockX, blockY, blockZ);

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
     * OPTİMİZE EDİLDİ: Daha az blok kontrol eder
     */
    private int findGroundLevel(World world, int x, int startY, int z) {
        return findGroundLevelOptimized(world, x, startY, z);
    }
    
    /**
     * Optimize edilmiş zemin bulma (daha az blok kontrol eder)
     */
    private int findGroundLevelOptimized(World world, int x, int startY, int z) {
        // Chunk yüklü mü kontrol et (performans)
        Chunk chunk = world.getChunkAt(x >> 4, z >> 4);
        if (!chunk.isLoaded()) {
            return -1; // Chunk yüklü değilse zemin bulunamaz
        }
        
        // Aşağıya doğru tara - ZEMİN BLOĞUNU BUL (daha az aralık)
        int searchRange = 5; // 10'dan 5'e azaltıldı (performans)
        for (int y = startY; y > startY - searchRange && y > world.getMinHeight(); y--) {
            Block currentBlock = world.getBlockAt(x, y, z);
            
            // Bu blok katı mı? (zemin bloğu)
            if (currentBlock.getType().isSolid() && !currentBlock.getType().isAir()) {
                return y; // ZEMİN BLOĞUNUN Y'sini döndür
            }
        }
        
        // Yukarıya doğru tara (daha az aralık)
        for (int y = startY; y < startY + searchRange && y < world.getMaxHeight(); y++) {
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
                
            case CHAOS_GOD:
            case CHAOS_TITAN:
                // Seviye 5: Kaos malzemeleri - Obsidyen, örümcek ağı, netherrack
                materials.add(Material.OBSIDIAN);
                materials.add(Material.CRYING_OBSIDIAN);
                materials.add(Material.NETHERRACK);
                materials.add(Material.BLACKSTONE);
                materials.add(Material.BASALT);
                materials.add(Material.COBWEB); // Örümcek ağı
                if (Math.random() < 0.2) materials.add(Material.MAGMA_BLOCK);
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
        private double nearestPlayerDistance = Double.MAX_VALUE; // En yakın oyuncu mesafesi
        
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
        public double getNearestPlayerDistance() { return nearestPlayerDistance; }
        public void setNearestPlayerDistance(double distance) { this.nearestPlayerDistance = distance; }
    }
}

