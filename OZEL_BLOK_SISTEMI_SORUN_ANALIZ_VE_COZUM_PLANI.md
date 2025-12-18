# ÖZEL BLOK SİSTEMİ SORUN ANALİZİ VE ÇÖZÜM PLANI

## 📋 İÇİNDEKİLER

1. [Tespit Edilen Sorunlar (Detaylı Analiz)](#tespit-edilen-sorunlar-detaylı-analiz)
2. [Minecraft Özel Blok Veri Tutma Yöntemleri (Karşılaştırmalı)](#minecraft-özel-blok-veri-tutma-yöntemleri-karşılaştırmalı)
3. [Topluluk Deneyimleri ve Çözümler](#topluluk-deneyimleri-ve-çözümler)
4. [Mevcut Sistem Analizi (Kod İncelemesi)](#mevcut-sistem-analizi-kod-incelemesi)
5. [Performans ve Edge Case Analizi](#performans-ve-edge-case-analizi)
6. [Çözüm Mimarisi (Detaylı)](#çözüm-mimarisi-detaylı)
7. [Adım Adım Çözüm Planı (Uygulama Detayları)](#adım-adım-çözüm-planı-uygulama-detayları)
8. [Çit Algılama Sistemi Düzeltmeleri (Algoritma Detayları)](#çit-algılama-sistemi-düzeltmeleri-algoritma-detayları)
9. [Test Senaryoları ve Doğrulama](#test-senaryoları-ve-doğrulama)
10. [Risk Analizi ve Önlemler](#risk-analizi-ve-önlemler)

---

## 🐛 TESPİT EDİLEN SORUNLAR (DETAYLI ANALİZ)

### 1. ❌ KRİTİK: Özel Blok Verisi Kayboluyor

#### Sorun Tanımı
- **Klan çiti kırıldığında normal çit olarak geri geliyor**
  - Oyuncu klan çiti kırıyor
  - Drop olarak normal `OAK_FENCE` geliyor
  - Özel blok verisi (clanId, metadata) kayboluyor
  - Oyuncu tekrar koyduğunda normal çit oluyor

- **Yaratıcı modda orta tık ile kopyalama yapınca özel blok değil, temel blok geliyor**
  - Yaratıcı modda orta tık (pick block) yapılıyor
  - Sadece `Material.OAK_FENCE` kopyalanıyor
  - PersistentDataContainer verisi kopyalanmıyor
  - Yerleştirildiğinde normal çit oluyor

- **Server restart sonrası metadata kayboluyor**
  - Server kapatılıyor
  - Metadata memory'de tutulduğu için kayboluyor
  - Server açıldığında çitler normal çit oluyor
  - Klan alanları algılanmıyor

#### Teknik Nedenler

**1. Metadata Kullanımı (Geçici Sistem)**
```java
// Mevcut kod (TerritoryListener.java:326)
block.setMetadata(metadataKey, new FixedMetadataValue(plugin, true));
```

**Sorunlar:**
- ❌ Metadata **geçici** bir sistemdir (memory-only)
- ❌ Server restart'ta **tamamen kaybolur**
- ❌ Chunk unload'da kaybolur
- ❌ World save/load'da kaybolur
- ❌ Yaratıcı modda kopyalama çalışmaz
- ❌ BlockBreakEvent'te metadata erişilebilir ama item'a aktarılmıyor

**2. BlockState Güncellemesi Eksik**
- BlockState güncellenmiyor
- PersistentDataContainer kullanılmıyor
- Veri sadece memory'de tutuluyor

**3. Blok Kırılma Event'inde Veri Geri Getirme Yok**
```java
// Mevcut kod - onFenceBreak() yok!
// BlockBreakEvent'te özel blok verisi item'a eklenmiyor
```

#### Etki Analizi

**Oyuncu Deneyimi:**
- ⚠️ Klan çitleri kırıldığında normal çit oluyor → Oyuncu kaynak kaybediyor
- ⚠️ Server restart sonrası klan alanları algılanmıyor → Klan sistemi bozuluyor
- ⚠️ Yaratıcı modda test edilemiyor → Geliştirme zorlaşıyor

**Sistem Etkisi:**
- ⚠️ Klan alanları kayboluyor
- ⚠️ Territory sistemi çalışmıyor
- ⚠️ Klan kristali koyulamıyor

---

### 2. ❌ KRİTİK: Klan Kristali Koyma Sorunu

#### Sorun Tanımı
- **Klan kristali koyamıyor**
  - Oyuncu klan kristali item'ını elinde tutuyor
  - Çitlerle çevrili alana sağ tıklıyor
  - "Klan çiti ile çevrilmiş alan istiyor" uyarısı alıyor
  - Çitlerle çevrili olsa bile algılamıyor

#### Teknik Nedenler

**1. Çit Algılama Sistemi Metadata'ya Bağımlı**
```java
// isClanFenceFast() - Sadece metadata kontrolü
if (block.hasMetadata(metadataKey)) {
    return true;
}
```

**Sorun:**
- Metadata kaybolduğu için çitler algılanmıyor
- Server restart sonrası tüm çitler normal çit oluyor
- `isSurroundedByClanFences()` false dönüyor

**2. Çit Algılama Algoritması Eksik**
- Sadece 2D kontrolü (X, Z)
- Y ekseni kontrolü yok
- Havada olan çitler algılanmıyor
- Yükseklik farkı dikkate alınmıyor

**3. Flood-Fill Algoritması Yetersiz**
```java
// Mevcut kod - Sadece 4 yöne bakıyor
BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
// Y ekseni YOK!
```

#### Senaryo Analizi

**Senaryo 1: Server Restart Sonrası**
1. Oyuncu çitleri koyuyor (metadata ile işaretleniyor)
2. Server restart oluyor
3. Metadata kayboluyor
4. Çitler normal çit oluyor
5. Klan kristali koyulamıyor ❌

**Senaryo 2: Yükseklik Farkı**
1. Oyuncu düz alanda çitler koyuyor
2. Bir kısmı 5 blok yukarıda
3. Algoritma sadece 2D kontrol ediyor
4. Yükseklik farkı algılanmıyor
5. Klan kristali koyulamıyor ❌

**Senaryo 3: Havada Çitler**
1. Oyuncu havada köprü yapıyor
2. Çitler havada (yere değmiyor)
3. Algoritma sadece yere bakıyor
4. Havada olan çitler algılanmıyor
5. Klan kristali koyulamıyor ❌

---

### 3. ❌ KRİTİK: Çit Algılama Sistemi Hatalı

#### Sorun Tanımı
- **Yükseklik farkı olduğunda algılamıyor**
  - Çitler farklı Y koordinatlarında
  - Algoritma sadece aynı Y seviyesinde kontrol ediyor
  - Yükseklik farkı olan çitler algılanmıyor

- **Havada olan çitler algılanmıyor**
  - Çitler havada (yere değmiyor)
  - Algoritma sadece yere bakıyor
  - Havada olan çitler atlanıyor

- **Sadece 2D (X, Z) kontrolü yapılıyor, Y ekseni dikkate alınmıyor**
  - Flood-fill algoritması 2D
  - Y ekseni kontrolü yok
  - 3D alan algılanamıyor

#### Teknik Analiz

**Mevcut Algoritma (isSurroundedByClanFences)**
```java
// Satır 914-980
private boolean isSurroundedByClanFences(Block center) {
    Set<Long> visited = new HashSet<>();
    Queue<Block> queue = new LinkedList<>();
    
    queue.add(center);
    visited.add(packCoords(center));
    
    while (!queue.isEmpty()) {
        Block current = queue.poll();
        
        // ❌ SORUN: Sadece 4 yöne bakıyor
        BlockFace[] faces = {
            BlockFace.NORTH, BlockFace.SOUTH, 
            BlockFace.EAST, BlockFace.WEST
            // Y ekseni YOK!
        };
        
        for (BlockFace face : faces) {
            Block neighbor = current.getRelative(face);
            // Y ekseni kontrolü YOK!
        }
    }
}
```

**Sorunlar:**
1. ❌ Y ekseni kontrolü yok (UP, DOWN)
2. ❌ Yükseklik farkı toleransı yok
3. ❌ Havada olan çitler algılanmıyor
4. ❌ Çit bağlantı kontrolü yok (fence connection)

**Edge Case'ler:**
- Çitler 5 blok yukarıda → Algılanmıyor ❌
- Çitler havada köprü → Algılanmıyor ❌
- Çitler farklı Y seviyelerinde → Algılanmıyor ❌
- Çitler bağlantısız ama yakın → Yanlış algılanıyor ❌

---

## 🔍 MINECRAFT ÖZEL BLOK VERİ TUTMA YÖNTEMLERİ (KARŞILAŞTIRMALI)

### 1. PersistentDataContainer (ÖNERİLEN ✅)

#### Açıklama
Minecraft'ın **resmi kalıcı veri tutma sistemi**. BlockState, TileState, Entity ve ItemStack'te kullanılabilir.

#### Teknik Detaylar

**API:**
```java
// BlockState'de PersistentDataContainer
BlockState state = block.getState();
PersistentDataContainer container = state.getPersistentDataContainer();

// Veri kaydetme
NamespacedKey key = new NamespacedKey(plugin, "clan_fence");
container.set(key, PersistentDataType.STRING, clanId.toString());
state.update(); // ✅ KRİTİK: BlockState güncellemesi gerekli!

// Veri okuma
String clanId = container.get(key, PersistentDataType.STRING);
```

**Desteklenen Veri Tipleri:**
- `PersistentDataType.BYTE`
- `PersistentDataType.SHORT`
- `PersistentDataType.INTEGER`
- `PersistentDataType.LONG`
- `PersistentDataType.FLOAT`
- `PersistentDataType.DOUBLE`
- `PersistentDataType.STRING`
- `PersistentDataType.BYTE_ARRAY`
- `PersistentDataType.INTEGER_ARRAY`
- `PersistentDataType.LONG_ARRAY`
- `PersistentDataType.TAG_CONTAINER` (Nested container)

**Kalıcılık:**
- ✅ Chunk yüklendiğinde otomatik yüklenir
- ✅ World save/load'da korunur
- ✅ Server restart'ta kaybolmaz
- ✅ Chunk unload'da kaybolmaz

**Performans:**
- ⚠️ BlockState güncellemesi gerekir (küçük performans maliyeti)
- ✅ Chunk yüklendiğinde otomatik yüklenir (lazy loading)
- ✅ Memory'de cache'lenir

**Yaratıcı Mod Desteği:**
- ✅ Pick block (orta tık) ile kopyalanır
- ✅ BlockState kopyalanır
- ✅ PersistentDataContainer kopyalanır

**Blok Kırılma:**
- ✅ BlockBreakEvent'te erişilebilir
- ✅ ItemStack'e aktarılabilir
- ✅ Geri yerleştirilebilir

#### Avantajlar
- ✅ **Kalıcı** (server restart'ta kaybolmaz)
- ✅ **Resmi API** (Minecraft tarafından desteklenir)
- ✅ **Yaratıcı mod desteği** (pick block çalışır)
- ✅ **Blok kırılma desteği** (veri geri getirilebilir)
- ✅ **Performanslı** (chunk-based lazy loading)

#### Dezavantajlar
- ⚠️ BlockState güncellemesi gerekir (küçük performans maliyeti)
- ⚠️ Chunk yüklenene kadar erişilemez (async kontrol gerekir)

#### Topluluk Deneyimleri
- ✅ **Yaygın kullanım**: Çoğu modern plugin PersistentDataContainer kullanıyor
- ✅ **Güvenilir**: Spigot/Paper tarafından resmi olarak destekleniyor
- ✅ **Performanslı**: Chunk-based lazy loading ile optimize edilmiş

---

### 2. Metadata (MEVCUT - KALDIRILMALI ❌)

#### Açıklama
Bukkit'in **eski geçici veri tutma sistemi**. Sadece memory'de tutulur.

#### Teknik Detaylar

**API:**
```java
// Metadata kullanımı
block.setMetadata(key, new FixedMetadataValue(plugin, value));
boolean has = block.hasMetadata(key);
MetadataValue meta = block.getMetadata(key).get(0);
```

**Kalıcılık:**
- ❌ Server restart'ta **tamamen kaybolur**
- ❌ Chunk unload'da kaybolur
- ❌ World save/load'da kaybolur
- ❌ Memory-only (disk'e yazılmaz)

**Performans:**
- ✅ Hızlı (memory-only)
- ❌ Server restart'ta kaybolur (büyük sorun)

**Yaratıcı Mod Desteği:**
- ❌ Pick block ile kopyalanmaz
- ❌ BlockState'de saklanmaz

**Blok Kırılma:**
- ⚠️ BlockBreakEvent'te erişilebilir
- ❌ ItemStack'e aktarılamaz (doğrudan)

#### Sorunlar
- ❌ **Geçici** (server restart'ta kaybolur)
- ❌ **Eski API** (deprecated değil ama önerilmiyor)
- ❌ **Yaratıcı mod desteği yok**
- ❌ **Kalıcılık yok**

#### Topluluk Deneyimleri
- ❌ **Eski sistem**: Modern plugin'ler kullanmıyor
- ❌ **Sorunlu**: Server restart'ta veri kaybı
- ⚠️ **Sadece geçici veri için**: Runtime-only veriler için kullanılabilir

---

### 3. Custom BlockData (ALTERNATİF - GEREKLİ DEĞİL)

#### Açıklama
Özel BlockData sınıfları oluşturarak blok verilerini saklama. Daha karmaşık ama daha güçlü.

#### Kullanım Senaryoları
- Özel blok tipleri için
- Karmaşık blok durumları için
- Bizim durumumuzda gerekli değil (PersistentDataContainer yeterli)

---

### 4. Veritabanı + PersistentDataContainer (HİBRİT - ÖNERİLEN ✅)

#### Açıklama
Hem PersistentDataContainer (hızlı erişim) hem de veritabanı (backup/restore) kullanma.

#### Avantajlar
- ✅ PersistentDataContainer: Hızlı erişim
- ✅ Veritabanı: Backup/restore, migration
- ✅ Çift güvenlik: Veri kaybı riski minimum

#### Kullanım Senaryosu
- PersistentDataContainer: Runtime erişim
- Veritabanı: Backup, migration, analytics

---

## 👥 TOPLULUK DENEYİMLERİ VE ÇÖZÜMLER

### Sorun 1: Server Restart Sonrası Veri Kaybı

**Yaygın Sorun:**
- Plugin geliştiricileri metadata kullanıyor
- Server restart sonrası veriler kayboluyor
- Oyuncular şikayet ediyor

**Çözüm (Topluluk):**
- ✅ **PersistentDataContainer kullan**: Çoğu modern plugin bu yöntemi kullanıyor
- ✅ **Veritabanı backup**: Ekstra güvenlik için
- ✅ **Chunk load listener**: Chunk yüklendiğinde verileri kontrol et

**Örnek Kod (Topluluk):**
```java
// Yaygın kullanım pattern'i
@EventHandler
public void onChunkLoad(ChunkLoadEvent event) {
    for (BlockState state : event.getChunk().getTileEntities()) {
        PersistentDataContainer container = state.getPersistentDataContainer();
        if (container.has(customKey, PersistentDataType.STRING)) {
            // Özel blok verisi var, işle
        }
    }
}
```

---

### Sorun 2: Blok Kırılma Sonrası Veri Kaybı

**Yaygın Sorun:**
- Blok kırıldığında özel veri kayboluyor
- ItemStack'e aktarılmıyor
- Oyuncu kaynak kaybediyor

**Çözüm (Topluluk):**
- ✅ **BlockBreakEvent'te veri oku**: PersistentDataContainer'dan oku
- ✅ **ItemStack'e ekle**: ItemStack'in PersistentDataContainer'ına yaz
- ✅ **BlockPlaceEvent'te geri yükle**: ItemStack'ten oku ve bloka yaz

**Örnek Kod (Topluluk):**
```java
@EventHandler
public void onBlockBreak(BlockBreakEvent event) {
    Block block = event.getBlock();
    BlockState state = block.getState();
    PersistentDataContainer container = state.getPersistentDataContainer();
    
    if (container.has(customKey, PersistentDataType.STRING)) {
        String data = container.get(customKey, PersistentDataType.STRING);
        
        // ItemStack'e ekle
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (item != null) {
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer itemContainer = meta.getPersistentDataContainer();
            itemContainer.set(customKey, PersistentDataType.STRING, data);
            item.setItemMeta(meta);
        }
    }
}
```

---

### Sorun 3: Yaratıcı Mod Kopyalama

**Yaygın Sorun:**
- Yaratıcı modda orta tık ile kopyalama çalışmıyor
- Sadece temel blok kopyalanıyor
- Özel veri kayboluyor

**Çözüm (Topluluk):**
- ✅ **BlockState kopyalama**: Pick block event'inde BlockState'i kopyala
- ✅ **ItemStack'e ekle**: ItemStack'in PersistentDataContainer'ına yaz
- ✅ **BlockPlaceEvent'te geri yükle**: ItemStack'ten oku ve bloka yaz

**Örnek Kod (Topluluk):**
```java
@EventHandler
public void onPlayerInteract(PlayerInteractEvent event) {
    if (event.getAction() == Action.RIGHT_CLICK_BLOCK && 
        event.getPlayer().getGameMode() == GameMode.CREATIVE) {
        
        Block block = event.getClickedBlock();
        BlockState state = block.getState();
        PersistentDataContainer container = state.getPersistentDataContainer();
        
        if (container.has(customKey, PersistentDataType.STRING)) {
            // ItemStack'e ekle (pick block için)
            ItemStack item = new ItemStack(block.getType());
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer itemContainer = meta.getPersistentDataContainer();
            itemContainer.set(customKey, PersistentDataType.STRING, 
                container.get(customKey, PersistentDataType.STRING));
            item.setItemMeta(meta);
            event.getPlayer().getInventory().setItemInMainHand(item);
        }
    }
}
```

---

### Sorun 4: 3D Flood-Fill Algoritması

**Yaygın Sorun:**
- 2D flood-fill algoritması yaygın
- Y ekseni kontrolü eksik
- Yükseklik farkı algılanmıyor

**Çözüm (Topluluk):**
- ✅ **3D flood-fill**: 6 yöne bak (NORTH, SOUTH, EAST, WEST, UP, DOWN)
- ✅ **Yükseklik toleransı**: Config'den ayarlanabilir
- ✅ **Çit bağlantı kontrolü**: Fence connection API kullan

**Örnek Algoritma (Topluluk):**
```java
private boolean isSurrounded3D(Block center, int heightTolerance) {
    Set<Location> visited = new HashSet<>();
    Queue<Block> queue = new LinkedList<>();
    
    queue.add(center);
    visited.add(center.getLocation());
    
    // 6 yöne bak (3D)
    BlockFace[] faces = {
        BlockFace.NORTH, BlockFace.SOUTH,
        BlockFace.EAST, BlockFace.WEST,
        BlockFace.UP, BlockFace.DOWN
    };
    
    while (!queue.isEmpty()) {
        Block current = queue.poll();
        
        for (BlockFace face : faces) {
            Block neighbor = current.getRelative(face);
            
            // Yükseklik toleransı kontrolü
            int heightDiff = Math.abs(neighbor.getY() - center.getY());
            if (heightDiff > heightTolerance) continue;
            
            // Çit kontrolü
            if (isClanFence(neighbor)) {
                visited.add(neighbor.getLocation());
                continue;
            }
            
            // Hava kontrolü
            if (neighbor.getType() == Material.AIR) {
                if (!visited.contains(neighbor.getLocation())) {
                    visited.add(neighbor.getLocation());
                    queue.add(neighbor);
                }
            }
        }
    }
    
    return visited.size() >= minArea;
}
```

---

## 📊 MEVCUT SİSTEM ANALİZİ (KOD İNCELEMESİ)

### TerritoryListener.java - Çit Yerleştirme

**Mevcut Kod:**
```java
// Satır 285-328
@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
public void onFencePlace(BlockPlaceEvent event) {
    Block block = event.getBlock();
    
    // Material kontrolü
    if (block.getType() != Material.OAK_FENCE) {
        return;
    }
    
    // Item kontrolü
    ItemStack item = event.getItemInHand();
    boolean isClanFence = false;
    
    if (territoryConfig != null && territoryConfig.isRequireClanFenceItem()) {
        if (item != null && ItemManager.isClanItem(item, "FENCE")) {
            isClanFence = true;
        }
    }
    
    if (!isClanFence) {
        event.setCancelled(true);
        return;
    }
    
    // ❌ SORUN: Metadata kullanılıyor (geçici)
    if (territoryConfig != null) {
        String metadataKey = territoryConfig.getFenceMetadataKey();
        block.setMetadata(metadataKey, new org.bukkit.metadata.FixedMetadataValue(
            me.mami.stratocraft.Main.getInstance(), true));
    }
    
    // ✅ İYİ: TerritoryData'ya ekleniyor
    if (boundaryManager != null) {
        boundaryManager.addFenceLocation(playerClan, block.getLocation());
    }
}
```

**Sorunlar:**
1. ❌ **Metadata kullanılıyor**: Geçici, server restart'ta kaybolur
2. ❌ **PersistentDataContainer yok**: Kalıcı veri sistemi kullanılmıyor
3. ❌ **BlockState güncellenmiyor**: Veri sadece memory'de
4. ❌ **Blok kırılma kontrolü yok**: onFenceBreak() metodu yok

**Düzeltme Gereksinimleri:**
- ✅ PersistentDataContainer kullan
- ✅ BlockState güncelle
- ✅ ClanId kaydet
- ✅ onFenceBreak() ekle

---

### TerritoryListener.java - Çit Kontrolü

**Mevcut Kod:**
```java
// Satır 993-1011
private boolean isClanFenceFast(Block block) {
    if (block.getType() != Material.OAK_FENCE) {
        return false;
    }
    
    // ❌ SORUN: Sadece metadata kontrolü
    if (territoryConfig != null) {
        String metadataKey = territoryConfig.getFenceMetadataKey();
        if (block.hasMetadata(metadataKey)) {
            return true;
        }
    }
    
    // ❌ SORUN: TerritoryData döngüsü kaldırılmış (yorum satırında)
    // OPTİMİZE: TerritoryData döngüsü kaldırıldı - çok yavaştı
    // Metadata yoksa klan çiti değil kabul et
    // NOT: Server restart sonrası çitler metadata kaybedebilir
    // Bu durumda çitlerin yeniden koyulması gerekir
    
    return false;
}
```

**Sorunlar:**
1. ❌ **Sadece metadata kontrolü**: Server restart sonrası çalışmıyor
2. ❌ **PersistentDataContainer kontrolü yok**: Kalıcı veri kontrol edilmiyor
3. ❌ **Fallback mekanizması yok**: Metadata yoksa TerritoryData'ya bakmıyor

**Düzeltme Gereksinimleri:**
- ✅ PersistentDataContainer kontrolü ekle
- ✅ Fallback: TerritoryData kontrolü ekle
- ✅ Performans optimizasyonu: Cache kullan

---

### TerritoryListener.java - Çit Algılama

**Mevcut Kod:**
```java
// Satır 914-980
private boolean isSurroundedByClanFences(Block center) {
    Set<Long> visited = new HashSet<>();
    Queue<Block> queue = new LinkedList<>();
    boolean foundClanFence = false;
    
    queue.add(center);
    visited.add(packCoords(center));
    
    int minArea = 9;
    int maxIterations = 500;
    
    while (!queue.isEmpty()) {
        Block current = queue.poll();
        iterations++;
        
        if (iterations > maxIterations) {
            return false;
        }
        
        // ❌ SORUN: Sadece 4 yöne bakıyor (2D)
        BlockFace[] faces = {
            BlockFace.NORTH, BlockFace.SOUTH, 
            BlockFace.EAST, BlockFace.WEST
            // Y ekseni YOK!
        };
        
        for (BlockFace face : faces) {
            Block neighbor = current.getRelative(face);
            long neighborKey = packCoords(neighbor);
            if (visited.contains(neighborKey)) continue;
            
            Material type = neighbor.getType();
            
            // Çit kontrolü
            if (type == Material.OAK_FENCE) {
                if (isClanFenceFast(neighbor)) {
                    foundClanFence = true;
                    visited.add(neighborKey);
                    continue;
                } else {
                    return false; // Normal çit - alan açık
                }
            }
            
            // Solid blok - engel
            if (type != Material.AIR && type != Material.CAVE_AIR && type != Material.VOID_AIR) {
                visited.add(neighborKey);
                continue;
            }
            
            // Hava - aramaya devam
            visited.add(neighborKey);
            queue.add(neighbor);
        }
    }
    
    return visited.size() >= minArea && foundClanFence;
}
```

**Sorunlar:**
1. ❌ **Sadece 2D kontrolü**: Y ekseni yok
2. ❌ **Yükseklik farkı yok**: Farklı Y seviyelerinde çitler algılanmıyor
3. ❌ **Havada çitler algılanmıyor**: Sadece yere bakıyor
4. ❌ **Çit bağlantı kontrolü yok**: Fence connection API kullanılmıyor

**Düzeltme Gereksinimleri:**
- ✅ 3D flood-fill: 6 yöne bak
- ✅ Yükseklik toleransı: Config'den ayarlanabilir
- ✅ Çit bağlantı kontrolü: Fence connection API
- ✅ Havada çitler: Y ekseni kontrolü

---

## ⚡ PERFORMANS VE EDGE CASE ANALİZİ

### Performans Sorunları

**1. BlockState Güncellemesi**
- ⚠️ Her blok yerleştirmede BlockState güncelleniyor
- ⚠️ Küçük performans maliyeti var
- ✅ Çözüm: Batch update (toplu güncelleme)

**2. Chunk Loading**
- ⚠️ Chunk yüklenene kadar veri erişilemez
- ⚠️ Async kontrol gerekir
- ✅ Çözüm: ChunkLoadEvent listener

**3. Flood-Fill Algoritması**
- ⚠️ Büyük alanlarda yavaş olabilir
- ⚠️ Max iterations limiti var (500)
- ✅ Çözüm: Async flood-fill, optimizasyon

---

### Edge Case'ler

**1. Chunk Unload/Load**
- Senaryo: Chunk unload oluyor, sonra tekrar load oluyor
- Sorun: PersistentDataContainer otomatik yüklenir ama kontrol edilmeli
- Çözüm: ChunkLoadEvent listener ekle

**2. World Edit / WorldGuard**
- Senaryo: World Edit ile bloklar kopyalanıyor
- Sorun: PersistentDataContainer kopyalanmayabilir
- Çözüm: World Edit hook ekle (opsiyonel)

**3. Çoklu Dünya**
- Senaryo: Farklı dünyalarda çitler var
- Sorun: Dünya kontrolü yapılmalı
- Çözüm: World kontrolü ekle

**4. Çit Bağlantısızlığı**
- Senaryo: Çitler yakın ama bağlantısız
- Sorun: Yanlış algılanabilir
- Çözüm: Fence connection API kullan

---

## 🏗️ ÇÖZÜM MİMARİSİ (DETAYLI)

### 1. PersistentDataContainer Entegrasyonu

#### Mimari Tasarım

**Katman 1: Yardımcı Sınıf (CustomBlockData)**
```
CustomBlockData.java
├── setClanFenceData(Block, UUID) → PersistentDataContainer'a yaz
├── getClanFenceData(Block) → PersistentDataContainer'dan oku
├── isClanFence(Block) → Kontrol et
├── removeClanFenceData(Block) → Temizle
└── setClanCrystalData(Block, UUID) → Benzer metodlar
```

**Katman 2: Event Handler (TerritoryListener)**
```
TerritoryListener.java
├── onFencePlace() → CustomBlockData.setClanFenceData()
├── onFenceBreak() → CustomBlockData.getClanFenceData() → ItemStack'e ekle
├── isClanFenceFast() → CustomBlockData.isClanFence()
└── onChunkLoad() → PersistentDataContainer kontrolü
```

**Katman 3: Fallback Mekanizması**
```
TerritoryBoundaryManager
├── Fence location tracking (backup)
└── Chunk load'da verileri geri yükle
```

---

### 2. Çit Algılama Sistemi Düzeltmesi

#### 3D Flood-Fill Algoritması

**Algoritma Tasarımı:**
```
isSurroundedByClanFences3D(Block center, int heightTolerance)
├── 3D flood-fill başlat
├── 6 yöne bak (NORTH, SOUTH, EAST, WEST, UP, DOWN)
├── Yükseklik toleransı kontrolü
├── Çit bağlantı kontrolü
└── Minimum alan kontrolü
```

**Optimizasyonlar:**
- ✅ Async flood-fill (büyük alanlar için)
- ✅ Max iterations limiti (lag önleme)
- ✅ Cache mekanizması (aynı alanı tekrar kontrol etme)

---

### 3. Blok Kırılma Geri Getirme Sistemi

#### Veri Akışı

```
BlockBreakEvent
    ↓
PersistentDataContainer'dan veri oku
    ↓
ItemStack'in PersistentDataContainer'ına yaz
    ↓
ItemStack'i oyuncuya ver
    ↓
BlockPlaceEvent (tekrar yerleştirme)
    ↓
ItemStack'ten veri oku
    ↓
BlockState'in PersistentDataContainer'ına yaz
    ↓
BlockState güncelle
```

---

## 📝 ADIM ADIM ÇÖZÜM PLANI (UYGULAMA DETAYLARI)

### FAZE 1: PersistentDataContainer Entegrasyonu

#### Adım 1.1: Yardımcı Sınıf Oluştur
**Dosya:** `src/main/java/me/mami/stratocraft/util/CustomBlockData.java`

**Görev:**
- PersistentDataContainer işlemleri için yardımcı metodlar
- Thread-safe (concurrent access)
- Error handling (null checks)
- Performance optimized (caching)

**Metodlar:**
```java
public class CustomBlockData {
    private static final NamespacedKey CLAN_FENCE_KEY = 
        new NamespacedKey(plugin, "clan_fence");
    private static final NamespacedKey CLAN_CRYSTAL_KEY = 
        new NamespacedKey(plugin, "clan_crystal");
    
    // Çit metodları
    public static boolean setClanFenceData(Block block, UUID clanId)
    public static UUID getClanFenceData(Block block)
    public static boolean isClanFence(Block block)
    public static void removeClanFenceData(Block block)
    
    // Kristal metodları
    public static boolean setClanCrystalData(Block block, UUID clanId)
    public static UUID getClanCrystalData(Block block)
    public static boolean isClanCrystal(Block block)
    
    // Yardımcı metodlar
    private static BlockState getBlockState(Block block)
    private static void updateBlockState(BlockState state)
}
```

**Özellikler:**
- ✅ Null safety (tüm null kontrolleri)
- ✅ Error handling (try-catch)
- ✅ Thread-safe (synchronized gerekirse)
- ✅ Performance (caching)

---

#### Adım 1.2: Çit Yerleştirme Düzeltmesi
**Dosya:** `src/main/java/me/mami/stratocraft/listener/TerritoryListener.java`

**Değişiklikler:**
```java
@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
public void onFencePlace(BlockPlaceEvent event) {
    Block block = event.getBlock();
    Player player = event.getPlayer();
    
    // ... mevcut kontroller ...
    
    // ✅ YENİ: PersistentDataContainer kullan
    Clan playerClan = territoryManager.getClanManager()
        .getClanByPlayer(player.getUniqueId());
    
    if (playerClan != null) {
        // PersistentDataContainer'a kaydet
        CustomBlockData.setClanFenceData(block, playerClan.getId());
    } else {
        // Klan yok ama çit yerleştirilebilir (sonra klan kurulabilir)
        // Geçici olarak null kaydet (sonra güncellenebilir)
        CustomBlockData.setClanFenceData(block, null);
    }
    
    // ❌ ESKİ: Metadata kaldır
    // block.setMetadata(...); // KALDIRILACAK
    
    // ✅ İYİ: TerritoryData'ya ekle (backup)
    if (boundaryManager != null && playerClan != null) {
        boundaryManager.addFenceLocation(playerClan, block.getLocation());
    }
}
```

---

#### Adım 1.3: Çit Kontrolü Düzeltmesi
**Dosya:** `src/main/java/me/mami/stratocraft/listener/TerritoryListener.java`

**Değişiklikler:**
```java
private boolean isClanFenceFast(Block block) {
    if (block.getType() != Material.OAK_FENCE) {
        return false;
    }
    
    // ✅ YENİ: PersistentDataContainer kontrolü
    UUID clanId = CustomBlockData.getClanFenceData(block);
    if (clanId != null) {
        return true; // Klan çiti
    }
    
    // ✅ FALLBACK: TerritoryData kontrolü (backup)
    if (boundaryManager != null) {
        // TerritoryData'da bu konum var mı?
        // (Performans için cache kullanılabilir)
    }
    
    // ❌ ESKİ: Metadata kontrolü kaldır
    // if (block.hasMetadata(...)) return true; // KALDIRILACAK
    
    return false;
}
```

**Optimizasyon:**
- ✅ Cache mekanizması (aynı blok tekrar kontrol edilmesin)
- ✅ Async kontrol (chunk yüklenmemişse)

---

#### Adım 1.4: Blok Kırılma Geri Getirme
**Dosya:** `src/main/java/me/mami/stratocraft/listener/TerritoryListener.java`

**Yeni Metod:**
```java
@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
public void onFenceBreak(BlockBreakEvent event) {
    Block block = event.getBlock();
    
    // ✅ PersistentDataContainer'dan veri oku
    UUID clanId = CustomBlockData.getClanFenceData(block);
    if (clanId == null) {
        return; // Normal çit, işlem yok
    }
    
    // ✅ ItemStack'e veri ekle
    ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
    if (item != null && item.getType() == Material.OAK_FENCE) {
        // ItemStack'in PersistentDataContainer'ına yaz
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "clan_fence");
            container.set(key, PersistentDataType.STRING, clanId.toString());
            item.setItemMeta(meta);
            
            // Özel item olarak işaretle
            // (ItemManager.isClanItem() kontrolü için)
        }
    }
    
    // ✅ TerritoryData'dan kaldır (backup)
    if (boundaryManager != null) {
        Clan clan = territoryManager.getClanManager().getClan(clanId);
        if (clan != null) {
            boundaryManager.removeFenceLocation(clan, block.getLocation());
        }
    }
}
```

---

#### Adım 1.5: BlockPlaceEvent'te Geri Yükleme
**Dosya:** `src/main/java/me/mami/stratocraft/listener/TerritoryListener.java`

**Yeni Metod:**
```java
@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
public void onFencePlaceRestore(BlockPlaceEvent event) {
    Block block = event.getBlock();
    ItemStack item = event.getItemInHand();
    
    // ✅ ItemStack'ten veri oku
    if (item != null && item.hasItemMeta()) {
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "clan_fence");
        
        if (container.has(key, PersistentDataType.STRING)) {
            String clanIdStr = container.get(key, PersistentDataType.STRING);
            UUID clanId = UUID.fromString(clanIdStr);
            
            // ✅ Bloka veri yaz
            CustomBlockData.setClanFenceData(block, clanId);
            
            // ✅ TerritoryData'ya ekle (backup)
            if (boundaryManager != null) {
                Clan clan = territoryManager.getClanManager().getClan(clanId);
                if (clan != null) {
                    boundaryManager.addFenceLocation(clan, block.getLocation());
                }
            }
        }
    }
}
```

---

#### Adım 1.6: Chunk Load Listener
**Dosya:** `src/main/java/me/mami/stratocraft/listener/TerritoryListener.java`

**Yeni Metod:**
```java
@EventHandler(priority = EventPriority.MONITOR)
public void onChunkLoad(ChunkLoadEvent event) {
    // ✅ Chunk yüklendiğinde özel blokları kontrol et
    // (PersistentDataContainer otomatik yüklenir ama kontrol edilmeli)
    
    Chunk chunk = event.getChunk();
    for (BlockState state : chunk.getTileEntities()) {
        if (state.getType() == Material.OAK_FENCE) {
            Block block = state.getBlock();
            UUID clanId = CustomBlockData.getClanFenceData(block);
            
            if (clanId != null) {
                // ✅ TerritoryData'ya ekle (backup)
                if (boundaryManager != null) {
                    Clan clan = territoryManager.getClanManager().getClan(clanId);
                    if (clan != null) {
                        boundaryManager.addFenceLocation(clan, block.getLocation());
                    }
                }
            }
        }
    }
}
```

---

### FAZE 2: Çit Algılama Sistemi Düzeltmesi

#### Adım 2.1: 3D Flood-Fill Algoritması
**Dosya:** `src/main/java/me/mami/stratocraft/listener/TerritoryListener.java`

**Yeni Metod:**
```java
private boolean isSurroundedByClanFences3D(Block center, int heightTolerance) {
    Set<Location> visited = new HashSet<>();
    Queue<Block> queue = new LinkedList<>();
    boolean foundClanFence = false;
    
    queue.add(center);
    visited.add(center.getLocation());
    
    int minArea = 9; // Minimum alan (3x3)
    int maxIterations = 1000; // Artırıldı (3D için daha fazla iteration gerekebilir)
    int iterations = 0;
    
    int centerY = center.getY();
    
    while (!queue.isEmpty()) {
        Block current = queue.poll();
        iterations++;
        
        if (iterations > maxIterations) {
            return false; // Çok büyük alan
        }
        
        // ✅ YENİ: 6 yöne bak (3D)
        BlockFace[] faces = {
            BlockFace.NORTH, BlockFace.SOUTH,
            BlockFace.EAST, BlockFace.WEST,
            BlockFace.UP, BlockFace.DOWN  // ✅ Y ekseni eklendi
        };
        
        for (BlockFace face : faces) {
            Block neighbor = current.getRelative(face);
            Location neighborLoc = neighbor.getLocation();
            
            if (visited.contains(neighborLoc)) continue;
            
            // ✅ YENİ: Yükseklik toleransı kontrolü
            int heightDiff = Math.abs(neighbor.getY() - centerY);
            if (heightDiff > heightTolerance) {
                visited.add(neighborLoc); // Ziyaret edildi olarak işaretle
                continue; // Tolerans dışında, atla
            }
            
            Material type = neighbor.getType();
            
            // Çit kontrolü
            if (type == Material.OAK_FENCE) {
                if (isClanFenceFast(neighbor)) {
                    foundClanFence = true;
                    visited.add(neighborLoc);
                    continue; // Klan çiti, devam et
                } else {
                    return false; // Normal çit - alan açık
                }
            }
            
            // Solid blok - engel (yükseklik farkı olabilir)
            if (type != Material.AIR && 
                type != Material.CAVE_AIR && 
                type != Material.VOID_AIR) {
                visited.add(neighborLoc);
                continue;
            }
            
            // Hava - aramaya devam (3D)
            visited.add(neighborLoc);
            queue.add(neighbor);
        }
    }
    
    return visited.size() >= minArea && foundClanFence;
}
```

**Özellikler:**
- ✅ 3D flood-fill (6 yöne bak)
- ✅ Yükseklik toleransı (config'den)
- ✅ Havada çitler algılanır
- ✅ Yükseklik farkı dikkate alınır

---

#### Adım 2.2: Çit Bağlantı Kontrolü
**Yeni Metod:**
```java
/**
 * İki çitin birbirine bağlı olup olmadığını kontrol et
 * 
 * ÖNEMLİ: Çitlerin birbirine bağlı olması, klan alanı algılama için kritiktir.
 * Çitler arası bağlantı kopmuşsa, o çitler ayrı alanlar olarak algılanır.
 * 
 * @param fence1 İlk çit bloğu
 * @param fence2 İkinci çit bloğu
 * @return Çitler birbirine bağlıysa true, değilse false
 */
private boolean isFenceConnected(Block fence1, Block fence2) {
    // ✅ Material kontrolü
    if (fence1.getType() != Material.OAK_FENCE || 
        fence2.getType() != Material.OAK_FENCE) {
        return false;
    }
    
    // ✅ Fence BlockData kontrolü
    BlockData data1 = fence1.getBlockData();
    BlockData data2 = fence2.getBlockData();
    
    if (data1 instanceof Fence && data2 instanceof Fence) {
        Fence fenceData1 = (Fence) data1;
        Fence fenceData2 = (Fence) data2;
        
        // ✅ Yön hesaplama
        BlockFace direction = getDirection(fence1, fence2);
        if (direction == null) {
            return false; // Geçersiz yön
        }
        
        // ✅ Çitlerin birbirine bağlı olup olmadığını kontrol et
        // fence1'in direction yönünde yüzü var mı?
        // fence2'nin direction'ın tersi yönünde yüzü var mı?
        return fenceData1.hasFace(direction) && 
               fenceData2.hasFace(direction.getOppositeFace());
    }
    
    return false;
}

/**
 * İki blok arasındaki yönü hesapla
 * 
 * @param from Başlangıç bloğu
 * @param to Hedef bloğu
 * @return Yön (BlockFace) veya null (geçersiz yön)
 */
private BlockFace getDirection(Block from, Block to) {
    int dx = to.getX() - from.getX();
    int dy = to.getY() - from.getY();
    int dz = to.getZ() - from.getZ();
    
    // ✅ Sadece 1 blok mesafede olan bloklar için yön hesapla
    // (Çitler birbirine bitişik olmalı)
    if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) != 1) {
        return null; // 1 bloktan fazla mesafe
    }
    
    // ✅ Yön hesaplama
    if (dx == 1 && dy == 0 && dz == 0) return BlockFace.EAST;
    if (dx == -1 && dy == 0 && dz == 0) return BlockFace.WEST;
    if (dx == 0 && dy == 0 && dz == 1) return BlockFace.SOUTH;
    if (dx == 0 && dy == 0 && dz == -1) return BlockFace.NORTH;
    if (dx == 0 && dy == 1 && dz == 0) return BlockFace.UP;
    if (dx == 0 && dy == -1 && dz == 0) return BlockFace.DOWN;
    
    return null;
}
```

**3D Flood-Fill Algoritmasına Entegrasyon:**
```java
private boolean isSurroundedByClanFences3D(Block center, int heightTolerance) {
    // ... mevcut kod ...
    
    for (BlockFace face : faces) {
        Block neighbor = current.getRelative(face);
        
        // ... yükseklik kontrolü ...
        
        // Çit kontrolü
        if (type == Material.OAK_FENCE) {
            if (isClanFenceFast(neighbor)) {
                foundClanFence = true;
                visited.add(neighborLoc);
                
                // ✅ Çit bağlantı kontrolü (opsiyonel)
                if (territoryConfig != null && territoryConfig.isFenceConnectionRequired()) {
                    // Mevcut çit ile komşu çit arasında bağlantı var mı?
                    if (current.getType() == Material.OAK_FENCE && 
                        isClanFenceFast(current)) {
                        if (!isFenceConnected(current, neighbor)) {
                            // Bağlantısız çit - alan açık
                            return false;
                        }
                    }
                }
                
                continue; // Klan çiti, devam et
            } else {
                return false; // Normal çit - alan açık
            }
        }
        
        // ... devamı ...
    }
}
```

**Config Entegrasyonu:**
```yaml
territory:
  fence-height-tolerance: 5  # Çitler arası maksimum yükseklik farkı
  fence-connection-required: true  # Çitlerin bağlantılı olması gerekli mi?
  # true: Çitler birbirine bağlı olmalı (daha sıkı kontrol)
  # false: Çitler yakın olması yeterli (daha esnek kontrol)
```

**Kullanım Senaryoları:**

**Senaryo 1: Bağlantılı Çitler (Başarılı)**
```
Çit1 ── Çit2 ── Çit3
 │       │       │
Çit4 ── Çit5 ── Çit6
```
- Tüm çitler birbirine bağlı
- `isFenceConnected()` true döner
- Klan alanı algılanır ✅

**Senaryo 2: Bağlantısız Çitler (Başarısız)**
```
Çit1     Çit2     Çit3
 │                 │
Çit4     Çit5     Çit6
```
- Çit1 ve Çit2 arasında bağlantı yok
- `isFenceConnected()` false döner
- Klan alanı algılanmaz ❌ (config'de `fence-connection-required: true` ise)

**Senaryo 3: Yükseklik Farkı ile Bağlantılı Çitler (Başarılı)**
```
Çit1 ── Çit2 (5 blok yukarıda)
 │       │
Çit3 ── Çit4
```
- Çitler farklı Y seviyelerinde ama bağlantılı
- Yükseklik toleransı içinde (5 blok)
- `isFenceConnected()` true döner
- Klan alanı algılanır ✅

---

#### Adım 2.3: Yükseklik Farkı Toleransı
**Config:**
```yaml
territory:
  fence-height-tolerance: 5  # Çitler arası maksimum yükseklik farkı
  fence-connection-required: true  # Çitlerin bağlantılı olması gerekli mi?
```

**Kullanım:**
```java
int heightTolerance = territoryConfig.getFenceHeightTolerance(); // Varsayılan: 5
boolean isSurrounded = isSurroundedByClanFences3D(center, heightTolerance);
```

---

### FAZE 3: Yaratıcı Mod Kopyalama Desteği

#### Adım 3.1: Pick Block Event Handler
**Yeni Metod:**
```java
@EventHandler(priority = EventPriority.HIGH)
public void onCreativeCopy(PlayerInteractEvent event) {
    if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
    if (event.getPlayer().getGameMode() != GameMode.CREATIVE) return;
    
    Block block = event.getClickedBlock();
    if (block == null) return;
    
    // ✅ PersistentDataContainer'dan veri oku
    UUID clanId = CustomBlockData.getClanFenceData(block);
    if (clanId == null) return; // Özel blok değil
    
    // ✅ ItemStack oluştur
    ItemStack item = new ItemStack(block.getType());
    ItemMeta meta = item.getItemMeta();
    if (meta != null) {
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "clan_fence");
        container.set(key, PersistentDataType.STRING, clanId.toString());
        item.setItemMeta(meta);
        
        // ✅ Özel item olarak işaretle
        // (ItemManager.isClanItem() için)
    }
    
    // ✅ Oyuncuya ver
    event.getPlayer().getInventory().setItemInMainHand(item);
    event.setCancelled(true);
}
```

---

## 🔧 ÇİT ALGILAMA SİSTEMİ DÜZELTMELERİ (ALGORİTMA DETAYLARI)

### Mevcut Algoritma (2D) - Sorunlu

```java
// Sadece 4 yöne bakıyor
BlockFace[] faces = {
    BlockFace.NORTH, BlockFace.SOUTH, 
    BlockFace.EAST, BlockFace.WEST
    // ❌ Y ekseni YOK!
};
```

**Sorunlar:**
- ❌ Y ekseni kontrolü yok
- ❌ Havada olan çitler algılanmıyor
- ❌ Yükseklik farkı dikkate alınmıyor
- ❌ Çit bağlantı kontrolü yok

---

### Yeni Algoritma (3D) - Düzeltilmiş

```java
// 6 yöne bak (3D)
BlockFace[] faces = {
    BlockFace.NORTH, BlockFace.SOUTH,
    BlockFace.EAST, BlockFace.WEST,
    BlockFace.UP, BlockFace.DOWN  // ✅ Y ekseni eklendi
};

// Yükseklik farkı toleransı
int heightTolerance = territoryConfig.getFenceHeightTolerance(); // Varsayılan: 5

// Yükseklik kontrolü
int heightDiff = Math.abs(neighbor.getY() - centerY);
if (heightDiff > heightTolerance) {
    continue; // Tolerans dışında, atla
}
```

**Özellikler:**
- ✅ 3D flood-fill (6 yöne bak)
- ✅ Yükseklik farkı toleransı (config'den)
- ✅ Havada olan çitler algılanır
- ✅ Çitlerin bağlantılı olması kontrol edilir (opsiyonel)

---

### Algoritma Optimizasyonları

**1. Async Flood-Fill (Büyük Alanlar İçin)**
```java
// Async flood-fill (main thread'i kilitlememek için)
Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
    boolean isValid = isSurroundedByClanFences3D(center, heightTolerance);
    
    Bukkit.getScheduler().runTask(plugin, () -> {
        // Main thread'de sonucu işle
        if (isValid) {
            // Klan kur
        }
    });
});
```

**2. Cache Mekanizması**
```java
// Aynı alanı tekrar kontrol etme
private final Map<Location, Boolean> fenceCheckCache = new ConcurrentHashMap<>();

private boolean isSurroundedByClanFences3DCached(Block center) {
    Location centerLoc = center.getLocation();
    Boolean cached = fenceCheckCache.get(centerLoc);
    if (cached != null) {
        return cached;
    }
    
    boolean result = isSurroundedByClanFences3D(center, heightTolerance);
    fenceCheckCache.put(centerLoc, result);
    
    // Cache temizleme (5 dakika sonra)
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
        fenceCheckCache.remove(centerLoc);
    }, 6000L); // 5 dakika = 6000 tick
    
    return result;
}
```

**3. Max Iterations Optimizasyonu**
```java
// Büyük alanlar için max iterations artır
int maxIterations = 1000; // 3D için daha fazla gerekebilir

// Erken çıkış (minimum alan bulunduysa)
if (visited.size() >= minArea && foundClanFence) {
    return true; // Erken çıkış
}
```

---

## 🧪 TEST SENARYOLARI VE DOĞRULAMA

### Test Senaryosu 1: Server Restart

**Adımlar:**
1. Klan çiti yerleştir
2. Server restart yap
3. Çit kontrolü yap (isClanFenceFast)
4. Klan kristali koy

**Beklenen Sonuç:**
- ✅ Çit klan çiti olarak algılanır
- ✅ Klan kristali koyulabilir

---

### Test Senaryosu 2: Blok Kırılma

**Adımlar:**
1. Klan çiti yerleştir
2. Çiti kır
3. Drop'u kontrol et
4. Tekrar yerleştir

**Beklenen Sonuç:**
- ✅ Drop klan çiti item'ı olur
- ✅ Tekrar yerleştirildiğinde klan çiti olur

---

### Test Senaryosu 3: Yükseklik Farkı

**Adımlar:**
1. Düz alanda çitler koy
2. Bir kısmı 5 blok yukarıda
3. Klan kristali koy

**Beklenen Sonuç:**
- ✅ Yükseklik farkı algılanır
- ✅ Klan kristali koyulabilir

---

### Test Senaryosu 4: Havada Çitler

**Adımlar:**
1. Havada köprü yap (çitler)
2. Klan kristali koy

**Beklenen Sonuç:**
- ✅ Havada olan çitler algılanır
- ✅ Klan kristali koyulabilir

---

### Test Senaryosu 5: Yaratıcı Mod Kopyalama

**Adımlar:**
1. Yaratıcı modda klan çitine orta tık
2. Kopyalanan item'ı kontrol et
3. Yerleştir

**Beklenen Sonuç:**
- ✅ Item klan çiti verisi içerir
- ✅ Yerleştirildiğinde klan çiti olur

---

## ⚠️ RİSK ANALİZİ VE ÖNLEMLER

### Risk 1: BlockState Güncelleme Performansı

**Risk:**
- Her blok yerleştirmede BlockState güncelleniyor
- Performans sorunu olabilir

**Önlem:**
- ✅ Batch update (toplu güncelleme)
- ✅ Async güncelleme (küçük gecikme kabul edilebilir)
- ✅ Cache mekanizması

---

### Risk 2: Chunk Unload/Load

**Risk:**
- Chunk unload olursa veri erişilemez
- Chunk load'da veri yüklenmeyebilir

**Önlem:**
- ✅ ChunkLoadEvent listener
- ✅ Fallback: TerritoryData (backup)
- ✅ Veritabanı backup (opsiyonel)

---

### Risk 3: World Edit / WorldGuard

**Risk:**
- World Edit ile bloklar kopyalanıyor
- PersistentDataContainer kopyalanmayabilir

**Önlem:**
- ✅ World Edit hook (opsiyonel)
- ✅ Fallback: TerritoryData
- ✅ Uyarı mesajı (World Edit kullanımında)

---

### Risk 4: Çoklu Dünya

**Risk:**
- Farklı dünyalarda çitler var
- Dünya kontrolü yapılmıyor

**Önlem:**
- ✅ World kontrolü ekle
- ✅ Dünya bazlı cache

---

## 📋 YAPILACAKLAR LİSTESİ (DETAYLI)

### Öncelik 1: PersistentDataContainer Entegrasyonu
- [ ] `CustomBlockData.java` yardımcı sınıfı oluştur
  - [ ] setClanFenceData() metodu
  - [ ] getClanFenceData() metodu
  - [ ] isClanFence() metodu
  - [ ] removeClanFenceData() metodu
  - [ ] Error handling
  - [ ] Null safety
- [ ] `onFencePlace()` → PersistentDataContainer kullan
  - [ ] Metadata kaldır
  - [ ] BlockState güncelle
  - [ ] TerritoryData backup
- [ ] `isClanFenceFast()` → PersistentDataContainer kontrolü
  - [ ] Metadata kontrolünü kaldır
  - [ ] Fallback: TerritoryData
  - [ ] Cache mekanizması
- [ ] `onFenceBreak()` → Veri geri getirme
  - [ ] PersistentDataContainer'dan oku
  - [ ] ItemStack'e ekle
  - [ ] TerritoryData'dan kaldır
- [ ] `onFencePlaceRestore()` → ItemStack'ten geri yükle
  - [ ] ItemStack'ten oku
  - [ ] BlockState'e yaz
  - [ ] TerritoryData'ya ekle
- [ ] `onChunkLoad()` → Chunk yüklendiğinde kontrol
  - [ ] PersistentDataContainer kontrolü
  - [ ] TerritoryData backup

### Öncelik 2: Çit Algılama Düzeltmesi
- [ ] `isSurroundedByClanFences3D()` → 3D flood-fill
  - [ ] 6 yöne bak (UP, DOWN eklendi)
  - [ ] Yükseklik toleransı
  - [ ] Async flood-fill (büyük alanlar için)
  - [ ] Max iterations optimizasyonu
- [ ] Y ekseni kontrolü ekle
  - [ ] UP, DOWN yönleri
  - [ ] Yükseklik farkı hesaplama
- [ ] Yükseklik farkı toleransı ekle
  - [ ] Config'den ayarlanabilir
  - [ ] Varsayılan: 5 blok
- [ ] Çit bağlantı kontrolü ekle
  - [ ] Fence connection API
  - [ ] Config'den açılabilir/kapatılabilir

### Öncelik 3: Yaratıcı Mod Desteği
- [ ] `onCreativeCopy()` → Pick block event handler
  - [ ] PersistentDataContainer kopyalama
  - [ ] ItemStack'e ekle
  - [ ] Özel item işaretleme

### Öncelik 4: Diğer Sistemlerin Güncellenmesi
- [ ] `ClanTerritoryMenu.java` güncellemesi
  - [ ] `isSurroundedByClanFences()` metodu güncellenecek
  - [ ] Metadata kontrolü kaldırılacak
  - [ ] `CustomBlockData.isClanFence()` kullanılacak
  - [ ] 3D flood-fill algoritması eklenecek
- [ ] `TerritoryBoundaryManager.java` güncellemesi
  - [ ] `areFencesConnected()` metodu 3D flood-fill ile uyumlu hale getirilecek
  - [ ] Yükseklik toleransı eklenecek
  - [ ] UP, DOWN yönleri eklenecek
  - [ ] Çit bağlantı kontrolü eklenecek

### Öncelik 5: Test ve Doğrulama
- [ ] Server restart testi
  - [ ] Çit verisi korunuyor mu?
  - [ ] Klan kristali koyulabiliyor mu?
  - [ ] Metadata migration çalışıyor mu?
- [ ] Blok kırılma testi
  - [ ] Veri item'a ekleniyor mu?
  - [ ] Tekrar yerleştirilebiliyor mu?
  - [ ] Klan çiti item'ı dönüyor mu?
- [ ] Yaratıcı mod kopyalama testi
  - [ ] Veri kopyalanıyor mu?
  - [ ] Yerleştirilebiliyor mu?
  - [ ] Pick block çalışıyor mu?
- [ ] Çit algılama testi
  - [ ] Yükseklik farkı algılanıyor mu?
  - [ ] Havada çitler algılanıyor mu?
  - [ ] 3D alan algılanıyor mu?
  - [ ] Çit bağlantı kontrolü çalışıyor mu?
- [ ] Diğer sistemler testi
  - [ ] StructureCoreManager çalışıyor mu? (metadata ile)
  - [ ] TrapManager çalışıyor mu? (metadata ile)
  - [ ] ClanBankSystem çalışıyor mu? (metadata ile)

---

## 🎯 BEKLENEN SONUÇLAR

### Önce:
- ❌ Klan çiti kırıldığında normal çit oluyor
- ❌ Server restart sonrası çitler algılanmıyor
- ❌ Yaratıcı modda kopyalama çalışmıyor
- ❌ Yükseklik farkı algılanmıyor
- ❌ Havada olan çitler algılanmıyor
- ❌ Klan kristali koyulamıyor

### Sonra:
- ✅ Klan çiti kırıldığında klan çiti olarak geri geliyor
- ✅ Server restart sonrası çitler algılanıyor
- ✅ Yaratıcı modda kopyalama çalışıyor
- ✅ Yükseklik farkı algılanıyor
- ✅ Havada olan çitler algılanıyor
- ✅ Klan kristali koyulabiliyor
- ✅ Çit bağlantı kontrolü yapılıyor
- ✅ Performans optimize edildi

---

## 📚 KAYNAKLAR VE REFERANSLAR

### Resmi Dokümantasyon
- [Minecraft PersistentDataContainer API](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/persistence/PersistentDataContainer.html)
- [BlockState API](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/block/BlockState.html)
- [Fence Connection API](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/block/data/type/Fence.html)
- [BlockBreakEvent API](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/block/BlockBreakEvent.html)
- [BlockPlaceEvent API](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/block/BlockPlaceEvent.html)

### Topluluk Kaynakları
- SpigotMC Forums: PersistentDataContainer kullanım örnekleri
- GitHub: Açık kaynak plugin'ler (örnek kodlar)
- Stack Overflow: Benzer sorunlar ve çözümler

### Best Practices
- ✅ PersistentDataContainer kullan (metadata yerine)
- ✅ BlockState güncelle (kalıcılık için)
- ✅ ChunkLoadEvent listener ekle (veri kontrolü için)
- ✅ Fallback mekanizması kullan (TerritoryData backup)
- ✅ Async işlemler (performans için)
- ✅ Error handling (null checks, try-catch)

---

## 🔄 MİGRASYON PLANI

### Eski Sistemden Yeni Sisteme Geçiş

**Adım 1: Mevcut Metadata'yı PersistentDataContainer'a Taşı**
```java
// ChunkLoadEvent'te mevcut metadata'yı PersistentDataContainer'a taşı
@EventHandler
public void onChunkLoadMigration(ChunkLoadEvent event) {
    Chunk chunk = event.getChunk();
    for (BlockState state : chunk.getTileEntities()) {
        if (state.getType() == Material.OAK_FENCE) {
            Block block = state.getBlock();
            
            // Eski metadata kontrolü
            if (block.hasMetadata("ClanFence")) {
                // Metadata'dan veri al
                // PersistentDataContainer'a yaz
                // Metadata'yı temizle
            }
        }
    }
}
```

**Adım 2: TerritoryData Backup**
- Mevcut TerritoryData'yı koru (backup)
- PersistentDataContainer ile senkronize et

**Adım 3: Metadata Kaldırma**
- Tüm metadata kullanımlarını kaldır
- PersistentDataContainer kullan

---

## 📊 PERFORMANS METRİKLERİ

### Önce (Metadata):
- ⚠️ Server restart: Veri kaybı %100
- ⚠️ Chunk unload: Veri kaybı %100
- ✅ Blok kontrolü: Hızlı (memory-only)
- ❌ Kalıcılık: Yok

### Sonra (PersistentDataContainer):
- ✅ Server restart: Veri kaybı %0
- ✅ Chunk unload: Veri kaybı %0
- ⚠️ Blok kontrolü: Küçük gecikme (BlockState okuma)
- ✅ Kalıcılık: Var

**Performans İyileştirmeleri:**
- ✅ Cache mekanizması (aynı blok tekrar kontrol edilmesin)
- ✅ Async işlemler (büyük alanlar için)
- ✅ Batch update (toplu güncelleme)

---

## ✅ SORUN ÇÖZÜM KONTROL LİSTESİ

### Kullanıcının Belirttiği Tüm Sorunlar ve Çözümleri

#### ✅ Sorun 1: Klan Çiti Kırıldığında Normal Çit Olarak Geri Geliyor
**Çözüm:** ✅ Plan'da mevcut
- **FAZE 1 - Adım 1.4**: `onFenceBreak()` metodu eklenecek
- PersistentDataContainer'dan veri okunacak
- ItemStack'e veri eklenecek
- Blok kırıldığında klan çiti item'ı dönecek

**Kod Konumu:**
- `TerritoryListener.java` → `onFenceBreak()` metodu
- `CustomBlockData.java` → `getClanFenceData()` metodu

---

#### ✅ Sorun 2: Yaratıcı Modda Orta Tık ile Kopyalama Yapınca Özel Blok Değil, Temel Blok Geliyor
**Çözüm:** ✅ Plan'da mevcut
- **FAZE 3 - Adım 3.1**: `onCreativeCopy()` metodu eklenecek
- Pick block event'inde PersistentDataContainer'dan veri okunacak
- ItemStack'e veri eklenecek
- Yerleştirildiğinde klan çiti olacak

**Kod Konumu:**
- `TerritoryListener.java` → `onCreativeCopy()` metodu
- `CustomBlockData.java` → `getClanFenceData()` metodu

---

#### ✅ Sorun 3: Server Restart Sonrası Metadata Kayboluyor
**Çözüm:** ✅ Plan'da mevcut
- **FAZE 1 - Adım 1.1**: PersistentDataContainer kullanılacak
- Metadata yerine PersistentDataContainer kullanılacak
- ChunkLoadEvent'te veriler kontrol edilecek
- TerritoryData backup mekanizması korunacak

**Kod Konumu:**
- `CustomBlockData.java` → Tüm metodlar
- `TerritoryListener.java` → `onChunkLoad()` metodu
- `TerritoryListener.java` → `onFencePlace()` metodu (metadata kaldırılacak)

---

#### ✅ Sorun 4: Klan Kristali Koyamıyor - Çitlerle Çevrili Olsa Bile Algılamıyor
**Çözüm:** ✅ Plan'da mevcut
- **FAZE 1 - Adım 1.3**: `isClanFenceFast()` metodu düzeltilecek
- PersistentDataContainer kontrolü eklenecek
- Fallback: TerritoryData kontrolü eklenecek
- Metadata kontrolü kaldırılacak

**Kod Konumu:**
- `TerritoryListener.java` → `isClanFenceFast()` metodu
- `CustomBlockData.java` → `isClanFence()` metodu

---

#### ✅ Sorun 5: Yükseklik Farkı Olduğunda Algılanmıyor
**Çözüm:** ✅ Plan'da mevcut
- **FAZE 2 - Adım 2.1**: `isSurroundedByClanFences3D()` metodu eklenecek
- 3D flood-fill algoritması (6 yöne bak)
- Yükseklik toleransı eklenecek (config'den)
- Y ekseni kontrolü eklenecek

**Kod Konumu:**
- `TerritoryListener.java` → `isSurroundedByClanFences3D()` metodu
- `TerritoryConfig.java` → `fence-height-tolerance` config ayarı

---

#### ✅ Sorun 6: Havada Olan Çitler Algılanmıyor
**Çözüm:** ✅ Plan'da mevcut
- **FAZE 2 - Adım 2.1**: 3D flood-fill algoritması
- UP, DOWN yönleri eklenecek
- Havada olan çitler algılanacak
- Yükseklik farkı dikkate alınacak

**Kod Konumu:**
- `TerritoryListener.java` → `isSurroundedByClanFences3D()` metodu
- BlockFace.UP, BlockFace.DOWN eklenecek

---

#### ✅ Sorun 7: Çitlerin Birbirine Bağlı Olması Önemli
**Çözüm:** ✅ Plan'da mevcut
- **FAZE 2 - Adım 2.2**: `isFenceConnected()` metodu eklenecek
- Fence connection API kullanılacak
- Çitlerin birbirine bağlı olup olmadığı kontrol edilecek
- Config'den açılabilir/kapatılabilir

**Kod Konumu:**
- `TerritoryListener.java` → `isFenceConnected()` metodu
- `TerritoryConfig.java` → `fence-connection-required` config ayarı

**Detaylı Açıklama:**
- Çitlerin birbirine bağlı olması için Fence BlockData API kullanılacak
- `fenceData1.hasFace(direction)` kontrolü yapılacak
- Bağlantısız çitler algılanmayacak (config'den kontrol edilebilir)
- Yükseklik farkı olsa bile bağlantılı çitler algılanacak

---

## 🔄 DİĞER SİSTEMLERİN ETKİLENMEMESİ İÇİN MİGRASYON PLANI

### Metadata Kullanan Diğer Sistemler

**Tespit Edilen Sistemler:**
1. ✅ **TerritoryListener** → Klan çitleri (düzeltilecek)
2. ⚠️ **StructureCoreManager** → Yapı çekirdekleri (şu an çalışıyor, sonra düzeltilebilir)
3. ⚠️ **TrapManager** → Tuzak çekirdekleri (şu an çalışıyor, sonra düzeltilebilir)
4. ⚠️ **ClanBankSystem** → Klan bankası (şu an çalışıyor, sonra düzeltilebilir)
5. ⚠️ **ClanTerritoryMenu** → Klan alanı menüsü (güncellenecek)

---

### Migration Stratejisi

#### Aşama 1: Klan Çitleri (Öncelik 1 - KRİTİK)
**Hedef:** Sadece klan çitleri için PersistentDataContainer kullanılacak

**Değişiklikler:**
- ✅ `TerritoryListener.java` → Metadata kaldırılacak, PersistentDataContainer kullanılacak
- ✅ `CustomBlockData.java` → Yeni yardımcı sınıf
- ✅ `TerritoryConfig.java` → Config ayarları (metadata key kaldırılabilir)

**Diğer Sistemler:**
- ⚠️ **StructureCoreManager**: Metadata kullanmaya devam edecek (değişiklik yok)
- ⚠️ **TrapManager**: Metadata kullanmaya devam edecek (değişiklik yok)
- ⚠️ **ClanBankSystem**: Metadata kullanmaya devam edecek (değişiklik yok)

**Geriye Uyumluluk:**
- ✅ Eski metadata'lı çitler ChunkLoadEvent'te PersistentDataContainer'a taşınacak
- ✅ TerritoryData backup mekanizması korunacak
- ✅ Fallback: Metadata yoksa TerritoryData kontrolü yapılacak

---

#### Aşama 2: ClanTerritoryMenu Güncellemesi (Öncelik 2)
**Hedef:** ClanTerritoryMenu'de metadata kontrolü yerine PersistentDataContainer kullanılacak

**Değişiklikler:**
- ✅ `ClanTerritoryMenu.java` → `isSurroundedByClanFences()` metodu güncellenecek
- ✅ Metadata kontrolü kaldırılacak
- ✅ `CustomBlockData.isClanFence()` kullanılacak

**Kod:**
```java
// ÖNCE (HATALI):
if (neighbor.hasMetadata(metadataKey)) {
    isClanFence = true;
}

// SONRA (DÜZELTİLMİŞ):
if (CustomBlockData.isClanFence(neighbor)) {
    isClanFence = true;
}
```

**Diğer Sistemler:**
- ⚠️ **StructureCoreManager**: Metadata kullanmaya devam edecek (değişiklik yok)
- ⚠️ **TrapManager**: Metadata kullanmaya devam edecek (değişiklik yok)
- ⚠️ **ClanBankSystem**: Metadata kullanmaya devam edecek (değişiklik yok)

---

#### Aşama 3: TerritoryBoundaryManager Güncellemesi (Öncelik 3)
**Hedef:** TerritoryBoundaryManager'da çit kontrolü güncellenecek

**Değişiklikler:**
- ✅ `TerritoryBoundaryManager.java` → Çit kontrolü metodları güncellenecek
- ✅ `areFencesConnected()` metodu 3D flood-fill ile uyumlu hale getirilecek
- ✅ Yükseklik toleransı eklenecek

**Kod:**
```java
// ÖNCE (2D):
Block[] neighbors = {
    current.getRelative(BlockFace.NORTH),
    current.getRelative(BlockFace.SOUTH),
    current.getRelative(BlockFace.EAST),
    current.getRelative(BlockFace.WEST)
};

// SONRA (3D):
Block[] neighbors = {
    current.getRelative(BlockFace.NORTH),
    current.getRelative(BlockFace.SOUTH),
    current.getRelative(BlockFace.EAST),
    current.getRelative(BlockFace.WEST),
    current.getRelative(BlockFace.UP),    // ✅ Eklendi
    current.getRelative(BlockFace.DOWN)   // ✅ Eklendi
};
```

**Diğer Sistemler:**
- ⚠️ **StructureCoreManager**: Metadata kullanmaya devam edecek (değişiklik yok)
- ⚠️ **TrapManager**: Metadata kullanmaya devam edecek (değişiklik yok)
- ⚠️ **ClanBankSystem**: Metadata kullanmaya devam edecek (değişiklik yok)

---

### Geriye Uyumluluk Mekanizması

**1. ChunkLoadEvent Migration**
```java
@EventHandler
public void onChunkLoadMigration(ChunkLoadEvent event) {
    Chunk chunk = event.getChunk();
    for (BlockState state : chunk.getTileEntities()) {
        if (state.getType() == Material.OAK_FENCE) {
            Block block = state.getBlock();
            
            // Eski metadata kontrolü
            if (block.hasMetadata("ClanFence")) {
                // Metadata'dan veri al
                // PersistentDataContainer'a yaz
                // Metadata'yı temizle
            }
        }
    }
}
```

**2. Fallback Mekanizması**
```java
private boolean isClanFenceFast(Block block) {
    // ✅ ÖNCE: PersistentDataContainer kontrolü
    UUID clanId = CustomBlockData.getClanFenceData(block);
    if (clanId != null) {
        return true;
    }
    
    // ✅ FALLBACK: TerritoryData kontrolü (backup)
    if (boundaryManager != null) {
        // TerritoryData'da bu konum var mı?
    }
    
    // ❌ ESKİ: Metadata kontrolü kaldırıldı
    // if (block.hasMetadata(...)) return true;
    
    return false;
}
```

---

### Diğer Sistemlerin Bozulmaması İçin Önlemler

**1. NamespacedKey İzolasyonu**
- Her sistem kendi NamespacedKey'ini kullanacak
- Klan çitleri: `new NamespacedKey(plugin, "clan_fence")`
- Yapı çekirdekleri: `new NamespacedKey(plugin, "structure_core")` (gelecekte)
- Tuzak çekirdekleri: `new NamespacedKey(plugin, "trap_core")` (gelecekte)

**2. Aşamalı Geçiş**
- Önce sadece klan çitleri düzeltilecek
- Diğer sistemler metadata kullanmaya devam edecek
- İleride diğer sistemler de PersistentDataContainer'a geçirilebilir

**3. Test Senaryoları**
- Klan çitleri test edilecek
- Yapı çekirdekleri test edilecek (metadata ile çalışmaya devam etmeli)
- Tuzak çekirdekleri test edilecek (metadata ile çalışmaya devam etmeli)

---

## ✅ SONUÇ

Bu plan, özel blok sistemindeki tüm sorunları çözmek için kapsamlı bir yaklaşım sunmaktadır. PersistentDataContainer kullanarak kalıcı veri tutma, 3D flood-fill algoritması ile gelişmiş çit algılama ve blok kırılma geri getirme sistemi ile tam bir çözüm sağlanacaktır.

**Tüm Sorunların Çözüm Durumu:**
- ✅ Sorun 1: Klan çiti kırıldığında normal çit olarak geri geliyor → **ÇÖZÜLECEK**
- ✅ Sorun 2: Yaratıcı modda orta tık ile kopyalama → **ÇÖZÜLECEK**
- ✅ Sorun 3: Server restart sonrası metadata kayboluyor → **ÇÖZÜLECEK**
- ✅ Sorun 4: Klan kristali koyamıyor → **ÇÖZÜLECEK**
- ✅ Sorun 5: Yükseklik farkı algılanmıyor → **ÇÖZÜLECEK**
- ✅ Sorun 6: Havada olan çitler algılanmıyor → **ÇÖZÜLECEK**
- ✅ Sorun 7: Çitlerin birbirine bağlı olması önemli → **ÇÖZÜLECEK**

**Öncelik Sırası:**
1. **FAZE 1**: PersistentDataContainer entegrasyonu (kritik)
2. **FAZE 2**: Çit algılama düzeltmesi (kritik)
3. **FAZE 3**: Yaratıcı mod desteği (opsiyonel)
4. **FAZE 4**: Diğer sistemlerin güncellenmesi (ClanTerritoryMenu, TerritoryBoundaryManager)

**Tahmini Süre:**
- FAZE 1: 2-3 saat
- FAZE 2: 2-3 saat
- FAZE 3: 1 saat
- FAZE 4: 1-2 saat
- Test: 1-2 saat
- **Toplam: 7-11 saat**

**Diğer Sistemlerin Durumu:**
- ✅ **StructureCoreManager**: Metadata kullanmaya devam edecek (bozulmayacak)
- ✅ **TrapManager**: Metadata kullanmaya devam edecek (bozulmayacak)
- ✅ **ClanBankSystem**: Metadata kullanmaya devam edecek (bozulmayacak)
- ✅ **ClanTerritoryMenu**: Güncellenecek (PersistentDataContainer kullanacak)
- ✅ **TerritoryBoundaryManager**: Güncellenecek (3D flood-fill ile uyumlu hale getirilecek)

---

## 📋 TÜM SORUNLARIN ÇÖZÜM ÖZETİ

### Kullanıcının Belirttiği Sorunlar ve Çözüm Durumu

| # | Sorun | Çözüm Durumu | Plan Konumu | Uygulama Adımı |
|---|-------|--------------|-------------|----------------|
| 1 | Klan çiti kırıldığında normal çit olarak geri geliyor | ✅ ÇÖZÜLECEK | FAZE 1 - Adım 1.4 | `onFenceBreak()` metodu |
| 2 | Yaratıcı modda orta tık ile kopyalama yapınca özel blok değil, temel blok geliyor | ✅ ÇÖZÜLECEK | FAZE 3 - Adım 3.1 | `onCreativeCopy()` metodu |
| 3 | Server restart sonrası metadata kayboluyor | ✅ ÇÖZÜLECEK | FAZE 1 - Adım 1.1 | PersistentDataContainer kullanımı |
| 4 | Klan kristali koyamıyor - çitlerle çevrili olsa bile algılamıyor | ✅ ÇÖZÜLECEK | FAZE 1 - Adım 1.3 | `isClanFenceFast()` düzeltmesi |
| 5 | Yükseklik farkı olduğunda algılanmıyor | ✅ ÇÖZÜLECEK | FAZE 2 - Adım 2.1 | 3D flood-fill algoritması |
| 6 | Havada olan çitler algılanmıyor | ✅ ÇÖZÜLECEK | FAZE 2 - Adım 2.1 | UP, DOWN yönleri eklendi |
| 7 | Çitlerin birbirine bağlı olması önemli | ✅ ÇÖZÜLECEK | FAZE 2 - Adım 2.2 | `isFenceConnected()` metodu |

**Tüm Sorunlar:** ✅ **7/7 ÇÖZÜLECEK**

---

### Çözüm Mimarisi Özeti

**1. PersistentDataContainer Entegrasyonu**
- ✅ Metadata yerine PersistentDataContainer kullanılacak
- ✅ Kalıcı veri tutma (server restart'ta kaybolmaz)
- ✅ ChunkLoadEvent'te veriler kontrol edilecek
- ✅ Fallback: TerritoryData backup mekanizması

**2. 3D Flood-Fill Algoritması**
- ✅ 6 yöne bak (NORTH, SOUTH, EAST, WEST, UP, DOWN)
- ✅ Yükseklik toleransı (config'den ayarlanabilir)
- ✅ Havada olan çitler algılanır
- ✅ Yükseklik farkı dikkate alınır

**3. Çit Bağlantı Kontrolü**
- ✅ Fence connection API kullanılacak
- ✅ Çitlerin birbirine bağlı olup olmadığı kontrol edilecek
- ✅ Config'den açılabilir/kapatılabilir

**4. Blok Kırılma Geri Getirme**
- ✅ PersistentDataContainer'dan veri okunacak
- ✅ ItemStack'e veri eklenecek
- ✅ Blok kırıldığında klan çiti item'ı dönecek

**5. Yaratıcı Mod Desteği**
- ✅ Pick block event'inde veri kopyalanacak
- ✅ ItemStack'e veri eklenecek
- ✅ Yerleştirildiğinde klan çiti olacak

---

### Diğer Sistemlerin Etkilenmemesi

**Metadata Kullanan Sistemler (Değişmeyecek):**
- ✅ **StructureCoreManager**: Metadata kullanmaya devam edecek
- ✅ **TrapManager**: Metadata kullanmaya devam edecek
- ✅ **ClanBankSystem**: Metadata kullanmaya devam edecek

**Güncellenecek Sistemler:**
- ✅ **TerritoryListener**: PersistentDataContainer kullanacak
- ✅ **ClanTerritoryMenu**: PersistentDataContainer kullanacak
- ✅ **TerritoryBoundaryManager**: 3D flood-fill ile uyumlu hale getirilecek

**Geriye Uyumluluk:**
- ✅ Eski metadata'lı çitler ChunkLoadEvent'te PersistentDataContainer'a taşınacak
- ✅ TerritoryData backup mekanizması korunacak
- ✅ Fallback: Metadata yoksa TerritoryData kontrolü yapılacak

---

### Uygulama Sırası

**1. FAZE 1: PersistentDataContainer Entegrasyonu (KRİTİK)**
- `CustomBlockData.java` yardımcı sınıfı oluştur
- `TerritoryListener.java` güncelle
- Metadata kaldır, PersistentDataContainer kullan
- Blok kırılma geri getirme ekle

**2. FAZE 2: Çit Algılama Düzeltmesi (KRİTİK)**
- 3D flood-fill algoritması ekle
- Yükseklik toleransı ekle
- Çit bağlantı kontrolü ekle

**3. FAZE 3: Yaratıcı Mod Desteği (OPSİYONEL)**
- Pick block event handler ekle
- Veri kopyalama ekle

**4. FAZE 4: Diğer Sistemlerin Güncellenmesi**
- `ClanTerritoryMenu.java` güncelle
- `TerritoryBoundaryManager.java` güncelle

**5. Test ve Doğrulama**
- Tüm test senaryoları çalıştır
- Diğer sistemlerin çalıştığını doğrula

---

## ✅ PLAN DOĞRULAMA

**Tüm Sorunlar Çözülecek mi?** ✅ **EVET**
- 7/7 sorun çözülecek
- Her sorun için detaylı çözüm planı mevcut
- Kod örnekleri ve uygulama adımları belirtilmiş

**Diğer Sistemler Bozulacak mı?** ✅ **HAYIR**
- Metadata kullanan diğer sistemler değişmeyecek
- Sadece klan çitleri için PersistentDataContainer kullanılacak
- Geriye uyumluluk mekanizması eklenecek

**Tutarlılık Kontrolü:** ✅ **TAMAM**
- Tüm çözümler birbiriyle uyumlu
- Çakışan değişiklikler yok
- Migration planı mevcut

**Uygulanabilirlik:** ✅ **TAMAM**
- Tüm adımlar detaylı açıklanmış
- Kod örnekleri mevcut
- Test senaryoları belirtilmiş
