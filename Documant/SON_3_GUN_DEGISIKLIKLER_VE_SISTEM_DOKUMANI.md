# 📊 SON 3 GÜN DEĞİŞİKLİKLERİ VE SİSTEM DÖKÜMANI

**Tarih:** Son 3 Gün (Son Commit'ler)  
**Kapsam:** Tüm sistemler, düzeltmeler, yeni özellikler ve algoritmalar

---

## 📋 İÇİNDEKİLER

1. [Klan Sistemi Düzeltmeleri](#1-klan-sistemi-düzeltmeleri)
2. [Ritüel Sistemi](#2-ritüel-sistemi)
3. [Özel Bloklar Sistemi](#3-özel-bloklar-sistemi)
4. [Territory Boundary Particle Sistemi](#4-territory-boundary-particle-sistemi)
5. [Cash Kullanımı ve Banka Sistemi](#5-cash-kullanımı-ve-banka-sistemi)
6. [Felaketler Sistemi](#6-felaketler-sistemi)
7. [Data Persistence Sistemi](#7-data-persistence-sistemi)
8. [Cache Sistemi ve Optimizasyonlar](#9-cache-sistemi-ve-optimizasyonlar)
9. [Kontrat Sistemi](#10-kontrat-sistemi)
10. [Alışveriş (Shop) Sistemi](#11-alışveriş-shop-sistemi)
11. [İttifak (Alliance) Sistemi](#12-ittifak-alliance-sistemi)
12. [Virtual Inventory Sistemi](#13-virtual-inventory-sistemi)
13. [Kuşatma (Siege) Sistemi](#14-kuşatma-siege-sistemi)
14. [Batarya (Battery) Sistemi](#15-batarya-battery-sistemi)
15. [Sistem Algoritmaları ve Çalışma Süreçleri](#8-sistem-algoritmaları-ve-çalışma-süreçleri)

---

## 1. KLAN SİSTEMİ DÜZELTMELERİ

### 1.1. Klan Kristali Persistence ve Restore Sistemi

**Sorun:** Sunucu restart sonrası klan kristalleri kayboluyordu ve klanlar "kristalsiz klan" durumuna düşüyordu.

**Çözüm:** Kapsamlı persistence ve restore sistemi eklendi.

#### 1.1.1. Veri Tutarlılığı Düzeltmeleri

**Dosya:** `Clan.java`

**Değişiklikler:**

```java
// setCrystalLocation() - crystalLocation ve hasCrystal senkronizasyonu
public void setCrystalLocation(Location loc) {
    this.crystalLocation = loc;
    this.hasCrystal = (loc != null); // ✅ Otomatik senkronizasyon
}

// setCrystalEntity() - crystalEntity null olsa bile crystalLocation varsa hasCrystal true
public void setCrystalEntity(EnderCrystal crystal) {
    this.crystalEntity = crystal;
    this.hasCrystal = (this.crystalLocation != null); // ✅ Location'dan çıkar
}

// hasCrystal() - Tutarsızlık kontrolü ve otomatik düzeltme
public boolean hasCrystal() {
    boolean result = hasCrystal || (crystalLocation != null);
    
    // ✅ DEBUG: Tutarsızlık tespit edilirse otomatik düzelt
    if (crystalLocation != null && !hasCrystal) {
        this.hasCrystal = true; // Düzelt
        return true;
    }
    return result;
}

// setHasCrystal() - hasCrystal false yapılırsa crystalLocation'ı da null yap
public void setHasCrystal(boolean hasCrystal) {
    this.hasCrystal = hasCrystal;
    if (!hasCrystal && crystalLocation != null) {
        this.crystalLocation = null;
        this.crystalEntity = null; // ✅ Tutarsızlık önleme
    }
}
```

**Algoritma:**
1. `crystalLocation` varsa → `hasCrystal` otomatik `true`
2. `hasCrystal` `false` yapılırsa → `crystalLocation` ve `crystalEntity` `null` yapılır
3. `hasCrystal()` çağrıldığında tutarsızlık tespit edilirse otomatik düzeltilir

#### 1.1.2. Kristal Restore Sistemi

**Dosya:** `Main.java` - `restoreClanCrystals()`

**Algoritma:**

```java
private void restoreClanCrystals(ClanManager clanManager) {
    for (Clan clan : clanManager.getAllClans()) {
        Location crystalLoc = clan.getCrystalLocation();
        boolean hasCrystal = clan.hasCrystal();
        
        // ✅ 1. crystalLocation null ise atla
        if (crystalLoc == null) continue;
        
        // ✅ 2. hasCrystal false ama crystalLocation varsa düzelt
        if (!hasCrystal) {
            clan.setHasCrystal(true); // Düzelt
        }
        
        // ✅ 3. World ve Chunk kontrolü
        World world = crystalLoc.getWorld();
        if (world == null) continue;
        
        Chunk chunk = world.getChunkAt(crystalLoc);
        if (!chunk.isLoaded()) {
            chunk.load(false);
        }
        
        // ✅ 4. Mevcut entity kontrolü (aynı konumda)
        boolean crystalExists = false;
        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof EnderCrystal) {
                Location entityLoc = entity.getLocation();
                if (entityLoc.getBlockX() == crystalLoc.getBlockX() &&
                    entityLoc.getBlockY() == crystalLoc.getBlockY() &&
                    entityLoc.getBlockZ() == crystalLoc.getBlockZ()) {
                    // ✅ Mevcut entity bulundu, bağla
                    clan.setCrystalEntity((EnderCrystal) entity);
                    
                    // ✅ Metadata ekle (yoksa)
                    if (territoryConfig != null) {
                        String metadataKey = territoryConfig.getCrystalMetadataKey();
                        if (!entity.hasMetadata(metadataKey)) {
                            entity.setMetadata(metadataKey, 
                                new FixedMetadataValue(this, true));
                        }
                    }
                    crystalExists = true;
                    break;
                }
            }
        }
        
        // ✅ 5. Entity yoksa yeni oluştur
        if (!crystalExists) {
            Location spawnLoc = crystalLoc.clone();
            if (spawnLoc.getX() == spawnLoc.getBlockX() && 
                spawnLoc.getZ() == spawnLoc.getBlockZ()) {
                spawnLoc.add(0.5, 0, 0.5); // Blok merkezine ayarla
            }
            
            EnderCrystal newCrystal = (EnderCrystal) world.spawnEntity(
                spawnLoc, EntityType.ENDER_CRYSTAL);
            newCrystal.setShowingBottom(true);
            newCrystal.setBeamTarget(null);
            
            // ✅ Metadata ekle
            if (territoryConfig != null) {
                String metadataKey = territoryConfig.getCrystalMetadataKey();
                newCrystal.setMetadata(metadataKey, 
                    new FixedMetadataValue(this, true));
            }
            
            // ✅ Klan'a bağla
            clan.setCrystalEntity(newCrystal);
            clan.setHasCrystal(true);
        }
    }
}
```

**Çalışma Süreci:**
1. Sunucu açıldığında `onEnable()` içinde `restoreClanCrystals()` çağrılır
2. Her klan için `crystalLocation` kontrol edilir
3. `hasCrystal` tutarsızlığı düzeltilir
4. Chunk yüklenir (gerekirse)
5. Aynı konumda mevcut entity aranır
6. Mevcut entity varsa bağlanır ve metadata eklenir
7. Yoksa yeni entity oluşturulur ve bağlanır

#### 1.1.3. Kristal Kırma ve Klan Dağıtma Sistemi

**Dosya:** `TerritoryListener.java` - `onCrystalBreak()`, `onCrystalDeath()`, `findClanByCrystal()`

**Algoritma:**

```java
// findClanByCrystal() - Entity referansı ve location kontrolü
private Clan findClanByCrystal(EnderCrystal crystal) {
    Location crystalLoc = crystal.getLocation();
    
    for (Clan clan : territoryManager.getClanManager().getAllClans()) {
        // ✅ 1. Önce entity referansına bak
        if (clan.getCrystalEntity() != null && 
            clan.getCrystalEntity().equals(crystal)) {
            return clan;
        }
        
        // ✅ 2. Entity referansı null ise location kontrolü yap
        Location clanCrystalLoc = clan.getCrystalLocation();
        if (clanCrystalLoc != null) {
            boolean locationMatch = 
                clanCrystalLoc.getBlockX() == crystalLoc.getBlockX() &&
                clanCrystalLoc.getBlockY() == crystalLoc.getBlockY() &&
                clanCrystalLoc.getBlockZ() == crystalLoc.getBlockZ() &&
                clanCrystalLoc.getWorld().equals(crystalLoc.getWorld());
            
            if (locationMatch) {
                // ✅ Entity referansını güncelle (sunucu restart sonrası)
                clan.setCrystalEntity(crystal);
                
                // ✅ Metadata ekle (yoksa)
                if (territoryConfig != null && !crystal.hasMetadata(metadataKey)) {
                    crystal.setMetadata(metadataKey, 
                        new FixedMetadataValue(Main.getInstance(), true));
                }
                return clan;
            }
        }
    }
    return null;
}

// onCrystalBreak() - Kristal kırılma kontrolü
@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
public void onCrystalBreak(EntityDamageEvent event) {
    if (!(event.getEntity() instanceof EnderCrystal)) return;
    
    EnderCrystal crystal = (EnderCrystal) event.getEntity();
    Clan owner = findClanByCrystal(crystal);
    
    if (owner == null) return; // Normal end crystal
    
    // ✅ Final damage kontrolü (kırılma kontrolü)
    if (event.getFinalDamage() >= 1.0 && !event.isCancelled()) {
        // ✅ Lider kırıyorsa özel mesaj
        if (owner.getLeader().equals(event.getDamager().getUniqueId())) {
            // Lider kendi kristalini kıramaz (normalde)
        }
        
        // ✅ Klan dağıt
        territoryManager.getClanManager().disbandClan(owner);
        territoryManager.setCacheDirty();
        
        // ✅ Tüm üyelere mesaj gönder
        for (UUID memberId : owner.getMembers().keySet()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.sendMessage("§c§lKLAN KRISTALİ YOK OLDU!");
                member.sendMessage("§7Klanınız dağıtıldı.");
            }
        }
        
        // ✅ Patlama efekti
        crystal.getWorld().spawnParticle(
            Particle.EXPLOSION_LARGE, crystal.getLocation(), 1);
    }
}

// onCrystalDeath() - Özel item drop
@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
public void onCrystalDeath(EntityDeathEvent event) {
    if (!(event.getEntity() instanceof EnderCrystal)) return;
    
    EnderCrystal crystal = (EnderCrystal) event.getEntity();
    Clan owner = findClanByCrystal(crystal);
    
    if (owner == null) return; // Normal end crystal
    
    // ✅ Klan zaten dağıtıldıysa item drop etme
    if (owner.getCrystalEntity() == null || !owner.hasCrystal() || 
        owner.getCrystalEntity() != crystal) {
        event.getDrops().clear();
        return;
    }
    
    // ✅ Normal drop'ları iptal et
    event.getDrops().clear();
    
    // ✅ Özel item oluştur (END_CRYSTAL + PDC verisi)
    ItemStack crystalItem = new ItemStack(Material.END_CRYSTAL);
    ItemMeta meta = crystalItem.getItemMeta();
    if (meta != null) {
        meta.setDisplayName("§5§lKlan Kristali");
        List<String> lore = new ArrayList<>();
        lore.add("§7Klan bölgesinin merkezi.");
        meta.setLore(lore);
        
        // ✅ PDC verisini ekle
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey("stratocraft", "clan_crystal");
        container.set(key, PersistentDataType.STRING, owner.getId().toString());
        
        // ✅ ItemManager.isClanItem() için custom_id ekle
        NamespacedKey customIdKey = new NamespacedKey(
            Main.getInstance(), "custom_id");
        container.set(customIdKey, PersistentDataType.STRING, "CLAN_CRYSTAL");
        
        crystalItem.setItemMeta(meta);
    }
    
    // ✅ Özel item'ı drop et
    crystal.getWorld().dropItemNaturally(crystal.getLocation(), crystalItem);
}
```

**Çalışma Süreci:**
1. Kristal hasar alır → `onCrystalBreak()` tetiklenir
2. `findClanByCrystal()` ile klan bulunur (entity referansı veya location kontrolü)
3. Final damage >= 1.0 ise kristal kırılır
4. Klan dağıtılır (`disbandClan()`)
5. Tüm üyelere mesaj gönderilir
6. Patlama efekti gösterilir
7. `onCrystalDeath()` tetiklenir
8. Özel item oluşturulur (PDC verisi ile)
9. Item drop edilir

**Debug Logları:**
- Tüm kritik noktalara debug logları eklendi:
  - `[CLAN_CRYSTAL_RESTORE]` - Restore süreci
  - `[KRISTAL KIRMA]` - Kırma süreci
  - `[KRISTAL ÖLÜM]` - Item drop süreci
  - `[KRISTAL BULMA]` - Klan bulma süreci
  - `[CLAN]` - Clan model değişiklikleri

---

## 2. RİTÜEL SİSTEMİ

### 2.1. Yeni Ritüel Yapıları

**Dosya:** `RitualInteractionListener.java`

#### 2.1.1. Klan Üye Alma Ritüeli

**Yapı:** 5x5 Soyulmuş Odun Çerçeve (End Portalı gibi)

```
[O][O][O][O][O]  <- Üst kenar (5 blok soyulmuş odun)
[O][ ][ ][ ][O]  <- Sol kenar | İç alan (3x3 boş) | Sağ kenar
[O][ ][ ][ ][O]  <- İç alan tamamen boş olmalı (AIR)
[O][ ][ ][ ][O]
[O][O][O][O][O]  <- Alt kenar (5 blok soyulmuş odun)
```

**Aktivasyon:**
- Shift + Sağ Tık (kenardaki bloğa)
- Elde Çakmak (Flint and Steel)

**Yetki Kontrolü:**
```java
Clan.Rank rank = clan.getRank(player.getUniqueId());
if (rank != Clan.Rank.LEADER && 
    rank != Clan.Rank.GENERAL && 
    rank != Clan.Rank.ELITE) {
    player.sendMessage("§cBu ritüeli sadece Lider, General veya Elite yapabilir!");
    return;
}
```

**Algoritma:**
1. Tıklanan blok kenarda mı kontrol et (`findRitualFrame()`)
2. Çerçeve yapısını kontrol et (`checkRitualFrameStructure()`)
3. İç alandaki oyuncuları bul (3x3 alan, 2 blok yükseklik)
4. Her oyuncu için:
   - Klansız mı veya farklı klanda mı kontrol et
   - Klan üyesi yap (Rank.RECRUIT)
   - Mesaj gönder
5. Partikül ve ses efekti göster

#### 2.1.2. Klandan Çıkma Ritüeli

**Yapı:** 5x5 Taş Tuğla Çerçeve (Soyulmuş Odun'dan farklı)

```
[T][T][T][T][T]  <- Üst kenar (5 blok taş tuğla)
[T][ ][ ][ ][T]  <- Sol kenar | İç alan (3x3 boş) | Sağ kenar
[T][ ][ ][ ][T]  <- İç alan tamamen boş olmalı (AIR)
[T][ ][ ][ ][T]
[T][T][T][T][T]  <- Alt kenar (5 blok taş tuğla)
```

**Aktivasyon:**
- Shift + Sağ Tık (kenardaki bloğa)
- Elde Çakmak (Flint and Steel)

**Yetki Kontrolü:**
```java
Clan.Rank rank = clan.getRank(player.getUniqueId());
if (rank == Clan.Rank.LEADER) {
    player.sendMessage("§cLider klanından ayrılamaz!");
    return;
}
// ✅ Lider hariç herkes yapabilir
```

**Algoritma:**
1. Tıklanan blok kenarda mı kontrol et (`findRitualFrame()` - `useStrippedLog = false`)
2. Çerçeve yapısını kontrol et (`checkRitualFrameStructure()` - `useStrippedLog = false`)
3. Lider kontrolü yap
4. Oyuncuyu klanından çıkar
5. Mesaj gönder
6. Partikül ve ses efekti göster

#### 2.1.3. Terfi Ritüeli

**Yapı:** 5x5 Soyulmuş Odun Çerçeve (Klan Üye Alma ile aynı)

**Aktivasyon:**
- Shift + Sağ Tık (kenardaki bloğa)
- Elde Çakmak (Flint and Steel)
- Elde Altın Külçe (Member → General) veya Demir Külçe (Recruit → Member)

**Yetki Kontrolü:**
```java
Clan.Rank rank = clan.getRank(player.getUniqueId());
if (rank != Clan.Rank.LEADER && rank != Clan.Rank.GENERAL) {
    player.sendMessage("§cBu ritüeli sadece Lider veya General yapabilir!");
    return;
}
```

**Algoritma:**
1. Çerçeve kontrolü (Klan Üye Alma ile aynı)
2. Eldeki item kontrolü (Altın Külçe veya Demir Külçe)
3. İç alandaki oyuncuları bul
4. Her oyuncu için:
   - Klan üyesi mi kontrol et
   - Mevcut rütbeyi kontrol et
   - Altın Külçe → Member → General
   - Demir Külçe → Recruit → Member
   - Rütbeyi güncelle
   - Mesaj gönder
5. Partikül ve ses efekti göster

### 2.2. Ritüel Yapı Kontrol Algoritması

**Dosya:** `RitualInteractionListener.java` - `findRitualFrame()`, `checkRitualFrameStructure()`

**Algoritma:**

```java
// findRitualFrame() - Çerçeve bulma
private RitualFrame findRitualFrame(Block clickedBlock, boolean useStrippedLog) {
    Material frameMaterial = useStrippedLog ? 
        Material.STRIPPED_OAK_LOG : Material.STONE_BRICKS;
    
    // ✅ Tıklanan blok kenarda mı kontrol et
    // 5x5 çerçeve için: minX, maxX, minZ, maxZ hesapla
    int centerX = clickedBlock.getX();
    int centerZ = clickedBlock.getZ();
    int centerY = clickedBlock.getY();
    
    // ✅ Çerçeve sınırlarını bul (tıklanan bloktan itibaren)
    int minX = centerX - 2;
    int maxX = centerX + 2;
    int minZ = centerZ - 2;
    int maxZ = centerZ + 2;
    
    // ✅ Kenarları kontrol et
    // Üst kenar: minZ, minX -> maxX
    // Alt kenar: maxZ, minX -> maxX
    // Sol kenar: minX, minZ -> maxZ
    // Sağ kenar: maxX, minZ -> maxZ
    
    // ✅ İç alanı kontrol et (3x3 boş olmalı)
    int innerMinX = minX + 1;
    int innerMaxX = maxX - 1;
    int innerMinZ = minZ + 1;
    int innerMaxZ = maxZ - 1;
    
    // ✅ İç alan tamamen boş mu kontrol et
    for (int x = innerMinX; x <= innerMaxX; x++) {
        for (int z = innerMinZ; z <= innerMaxZ; z++) {
            Block block = clickedBlock.getWorld().getBlockAt(x, centerY, z);
            if (block.getType() != Material.AIR) {
                return null; // İç alan boş değil
            }
        }
    }
    
    return new RitualFrame(minX, maxX, minZ, maxZ, 
        innerMinX, innerMaxX, innerMinZ, innerMaxZ, 
        new Location(clickedBlock.getWorld(), centerX, centerY, centerZ));
}

// checkRitualFrameStructure() - Çerçeve yapısı kontrolü
private String checkRitualFrameStructure(RitualFrame frame, boolean useStrippedLog) {
    Material frameMaterial = useStrippedLog ? 
        Material.STRIPPED_OAK_LOG : Material.STONE_BRICKS;
    
    // ✅ Kenarları kontrol et (20 blok)
    int frameBlockCount = 0;
    List<String> errors = new ArrayList<>();
    
    // Üst kenar
    for (int x = frame.minX; x <= frame.maxX; x++) {
        Block block = frame.center.getWorld().getBlockAt(x, frame.center.getY(), frame.minZ);
        if (isStrippedLog(block.getType()) == useStrippedLog && 
            block.getType() == frameMaterial) {
            frameBlockCount++;
        } else {
            errors.add("Üst kenar (" + x + "," + frame.minZ + ") yanlış blok: " + block.getType());
        }
    }
    
    // Alt kenar, Sol kenar, Sağ kenar (aynı mantık)
    // ...
    
    if (frameBlockCount != 20) {
        return "Çerçeve eksik! " + frameBlockCount + "/20 blok bulundu. Hatalar: " + 
            String.join(", ", errors);
    }
    
    // ✅ İç alan kontrolü (zaten findRitualFrame'de yapıldı)
    return null; // Başarılı
}
```

**Çalışma Süreci:**
1. Oyuncu kenardaki bloğa tıklar
2. `findRitualFrame()` çerçeveyi bulur
3. İç alan boş mu kontrol edilir
4. `checkRitualFrameStructure()` kenarları kontrol eder
5. Hata varsa detaylı mesaj gönderilir
6. Başarılıysa ritüel tetiklenir

---

## 3. ÖZEL BLOKLAR SİSTEMİ

### 3.1. CustomBlockData Utility Sistemi

**Dosya:** `CustomBlockData.java`

**Amaç:** Özel blokların verilerini PersistentDataContainer (PDC) ile saklamak.

**Desteklenen Bloklar:**
- **Klan Çiti (Clan Fence):** `OAK_FENCE` + PDC (`clan_fence`)
- **Tuzak Çekirdeği (Trap Core):** `LODESTONE` + PDC (`trap_core`)
- **Yapı Çekirdeği (Structure Core):** `OAK_LOG` + PDC (`structure_core`)
- **Klan Kristali (Clan Crystal):** `END_CRYSTAL` (entity) + PDC (`clan_crystal`)

**Algoritma:**

```java
// setClanFenceData() - Klan çiti verisi kaydet
public static void setClanFenceData(Block block, UUID clanId) {
    if (block == null || !(block.getState() instanceof TileState)) {
        // ✅ Runtime fallback: clanFenceRuntime Map kullan
        clanFenceRuntime.put(block.getLocation(), clanId);
        return;
    }
    
    TileState state = (TileState) block.getState();
    PersistentDataContainer container = state.getPersistentDataContainer();
    NamespacedKey key = new NamespacedKey(Main.getInstance(), "clan_fence");
    container.set(key, PersistentDataType.STRING, clanId.toString());
    state.update();
}

// getClanFenceData() - Klan çiti verisi oku
public static UUID getClanFenceData(Block block) {
    if (block == null) return null;
    
    // ✅ Önce runtime'dan kontrol et
    UUID runtimeData = clanFenceRuntime.get(block.getLocation());
    if (runtimeData != null) return runtimeData;
    
    if (!(block.getState() instanceof TileState)) {
        return null;
    }
    
    TileState state = (TileState) block.getState();
    PersistentDataContainer container = state.getPersistentDataContainer();
    NamespacedKey key = new NamespacedKey(Main.getInstance(), "clan_fence");
    String clanIdStr = container.get(key, PersistentDataType.STRING);
    
    if (clanIdStr == null) return null;
    try {
        return UUID.fromString(clanIdStr);
    } catch (IllegalArgumentException e) {
        return null;
    }
}
```

**Runtime Fallback Sistemi:**
- Bazı bloklar (ör. `OAK_FENCE`) TileState değildir
- Bu durumda `clanFenceRuntime` Map kullanılır
- Map: `Map<Location, UUID>` - Blok konumu → Klan ID

**Çalışma Süreci:**
1. Blok yerleştirilir → `BlockPlaceEvent` tetiklenir
2. Item PDC kontrolü yapılır (`isClanItem()`)
3. Blok PDC'ye veri yazılır (`setClanFenceData()`)
4. Blok kırılır → `BlockBreakEvent` tetiklenir
5. Blok PDC'den veri okunur (`getClanFenceData()`)
6. Özel item drop edilir (normal item değil)

### 3.2. Özel Blok Ekleme Rehberi

**Dosya:** `Documant/OZEL_BLOK_EKLEME_REHBERI.md`

**Adımlar:**

1. **ItemManager'da Item Oluşturma:**
   ```java
   // Static field
   public static ItemStack MY_CUSTOM_BLOCK;
   
   // init() metodunda
   MY_CUSTOM_BLOCK = create(Material.MATERIAL_TYPE, "MY_CUSTOM_BLOCK_ID", 
       "§6§lYeni Özel Blok", Arrays.asList("§7Açıklama"));
   ```

2. **CustomBlockData Utility'ye Metodlar Ekleme:**
   ```java
   // setMyCustomBlockData()
   public static void setMyCustomBlockData(Block block, UUID ownerId) {
       // PDC'ye yaz
   }
   
   // getMyCustomBlockData()
   public static UUID getMyCustomBlockData(Block block) {
       // PDC'den oku
   }
   ```

3. **Listener'da Event Handling:**
   ```java
   @EventHandler
   public void onBlockPlace(BlockPlaceEvent event) {
       if (ItemManager.isCustomItem(event.getItemInHand(), "MY_CUSTOM_BLOCK_ID")) {
           CustomBlockData.setMyCustomBlockData(event.getBlock(), 
               event.getPlayer().getUniqueId());
       }
   }
   
   @EventHandler
   public void onBlockBreak(BlockBreakEvent event) {
       UUID ownerId = CustomBlockData.getMyCustomBlockData(event.getBlock());
       if (ownerId != null) {
           // Özel item drop et
           event.setDropItems(false);
           event.getBlock().getWorld().dropItemNaturally(
               event.getBlock().getLocation(), ItemManager.MY_CUSTOM_BLOCK);
       }
   }
   ```

**Karşılaşılabilecek Sorunlar:**
1. **Blok TileState değilse:** Runtime fallback kullan
2. **Item kırıldığında normal item düşüyorsa:** `event.setDropItems(false)` kullan
3. **PDC verisi kayboluyorsa:** Chunk yükleme kontrolü yap

---

## 4. TERRITORY BOUNDARY PARTICLE SİSTEMİ

### 4.1. Dinamik Partikül Yoğunluğu

**Dosya:** `TerritoryBoundaryParticleTask.java`

**Algoritma:**

```java
private void showBoundaryParticles(Player player, TerritoryData territoryData) {
    Location playerLoc = player.getLocation();
    Location center = territoryData.getCenter();
    
    // ✅ Mesafe kontrolü (squared - performans)
    double distanceSquared = playerLoc.distanceSquared(center);
    int maxParticleDistance = config.getMaxParticleDistance();
    double radius = territoryData.getRadius();
    double maxVisibleDistance = maxParticleDistance + radius;
    double maxVisibleDistanceSquared = maxVisibleDistance * maxVisibleDistance;
    
    if (distanceSquared > maxVisibleDistanceSquared) {
        return; // Çok uzak
    }
    
    // ✅ Sınır çizgisini al
    List<Location> boundaryLine = territoryData.getBoundaryLine();
    
    // ✅ BoundaryLine boşsa ama radius varsa, dinamik olarak hesapla
    if (boundaryLine.isEmpty() && radius > 0) {
        // Daire çevresi boyunca partikül noktaları oluştur
        int particleCount = (int) (radius * 2 * Math.PI / 2.0); // Her 2 blokta bir
        if (particleCount < 8) particleCount = 8; // Minimum 8 nokta
        
        for (int i = 0; i < particleCount; i++) {
            double angle = (2 * Math.PI * i) / particleCount;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            Location boundaryLoc = new Location(center.getWorld(), x, center.getY(), z);
            boundaryLine.add(boundaryLoc);
        }
    }
    
    // ✅ Dinamik yoğunluk hesaplama (oyuncuya yakın partiküller daha yoğun)
    for (Location boundaryLoc : boundaryLine) {
        if (boundaryLoc == null || boundaryLoc.getWorld() == null) continue;
        if (!boundaryLoc.getWorld().equals(playerLoc.getWorld())) continue;
        
        // ✅ Oyuncuya olan mesafe (squared - performans)
        double distanceToParticleSquared = playerLoc.distanceSquared(boundaryLoc);
        double distanceToParticle = Math.sqrt(distanceToParticleSquared);
        
        // ✅ Dinamik yoğunluk: Yakın partiküller daha yoğun
        int particleCount = 1;
        if (distanceToParticle < 10) {
            particleCount = 3; // Çok yakın: 3 partikül
        } else if (distanceToParticle < 20) {
            particleCount = 2; // Yakın: 2 partikül
        } else {
            particleCount = 1; // Uzak: 1 partikül
        }
        
        // ✅ Y seviyesini oyuncu seviyesine göre ayarla
        Location particleLoc = boundaryLoc.clone();
        particleLoc.setY(playerLoc.getY());
        
        // ✅ Partikül göster
        for (int i = 0; i < particleCount; i++) {
            player.spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0);
        }
    }
}
```

**Çalışma Süreci:**
1. Her tick'te (config'den interval) task çalışır
2. Online oyuncular kontrol edilir
3. Her oyuncu için:
   - Klanı var mı kontrol et
   - TerritoryData al
   - Oyuncu alana yakın mı kontrol et (mesafe kontrolü)
   - BoundaryLine al (yoksa dinamik hesapla)
   - Her boundary noktası için:
     - Oyuncuya olan mesafeyi hesapla
     - Dinamik yoğunluk belirle (yakın = daha yoğun)
     - Partikül göster
4. Cooldown kontrolü (performans için)

**Optimizasyonlar:**
- `distanceSquared` kullanımı (sqrt hesaplama maliyetinden kaçınma)
- Cooldown sistemi (her oyuncu için)
- Mesafe limiti (çok uzak partiküller gösterilmez)
- Chunk yükleme kontrolü

### 4.2. Territory Boundary Manager

**Dosya:** `TerritoryBoundaryManager.java`

**Algoritma:**

```java
// calculateBoundaries() - Sınırları hesapla
public void calculateBoundaries(Clan clan) {
    TerritoryData territoryData = getTerritoryData(clan);
    if (territoryData == null) return;
    
    Location center = territoryData.getCenter();
    if (center == null) return;
    
    // ✅ Çitlerden sınır hesapla (flood fill algoritması)
    List<Location> fenceLocations = territoryData.getFenceLocations();
    if (!fenceLocations.isEmpty()) {
        // Flood fill ile çitlerin çevrelediği alanı bul
        Set<Location> enclosedArea = floodFillEnclosure(center, fenceLocations);
        
        // ✅ Sınır çizgisini hesapla (çitlerin dış kenarları)
        List<Location> boundaryLine = calculateBoundaryLine(enclosedArea, fenceLocations);
        territoryData.setBoundaryLine(boundaryLine);
    } else {
        // ✅ Çitler yoksa radius'tan hesapla
        int radius = territoryData.getRadius();
        if (radius > 0) {
            List<Location> boundaryLine = calculateCircularBoundary(center, radius);
            territoryData.setBoundaryLine(boundaryLine);
        }
    }
    
    territoryData.setBoundariesDirty(false);
}

// floodFillEnclosure() - Flood fill ile alan bulma
private Set<Location> floodFillEnclosure(Location start, List<Location> fences) {
    Set<Location> enclosed = new HashSet<>();
    Queue<Location> queue = new LinkedList<>();
    queue.add(start);
    enclosed.add(start);
    
    while (!queue.isEmpty()) {
        Location current = queue.poll();
        
        // ✅ 6 yöne bak (X, Y, Z eksenleri)
        for (BlockFace face : new BlockFace[]{
            BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, 
            BlockFace.WEST, BlockFace.UP, BlockFace.DOWN}) {
            
            Location neighbor = current.clone().add(
                face.getModX(), face.getModY(), face.getModZ());
            
            // ✅ Zaten ziyaret edildi mi?
            if (enclosed.contains(neighbor)) continue;
            
            // ✅ Çit mi?
            if (isFence(neighbor, fences)) continue;
            
            // ✅ Sınır dışına çıktı mı? (max radius kontrolü)
            if (start.distance(neighbor) > MAX_RADIUS) continue;
            
            enclosed.add(neighbor);
            queue.add(neighbor);
        }
    }
    
    return enclosed;
}
```

**Çalışma Süreci:**
1. Klan kristali yerleştirilir
2. Çitler toplanır (`collectFenceLocations()`)
3. `calculateBoundaries()` çağrılır
4. Çitler varsa flood fill ile alan bulunur
5. Sınır çizgisi hesaplanır
6. TerritoryData güncellenir
7. Partikül task'ı güncellenmiş veriyi kullanır

---

## 5. CASH KULLANIMI VE BANKA SİSTEMİ

### 5.1. Klan Bankası Sistemi

**Dosya:** `ClanBankSystem.java`

**Özellikler:**
- Para yatırma/çekme
- Item yatırma/çekme
- Otomatik maaş dağıtımı
- Transfer kontratları
- Yetki kontrolü

**Algoritma:**

```java
// depositItem() - Item yatırma (transaction mantığı)
public boolean depositItem(Player player, ItemStack item, int amount) {
    // ✅ 1. ÖNCE ENVANTERDEN AL (transaction başlat)
    ItemStack toRemove = item.clone();
    toRemove.setAmount(amount);
    HashMap<Integer, ItemStack> removeResult = player.getInventory().removeItem(toRemove);
    
    if (!removeResult.isEmpty()) {
        // Envanterden alınamadı, işlem iptal
        return false;
    }
    
    // ✅ 2. SONRA BANKAYA EKLE
    ItemStack depositItem = item.clone();
    depositItem.setAmount(amount);
    Inventory bankChest = getBankChest(clan);
    HashMap<Integer, ItemStack> overflow = bankChest.addItem(depositItem);
    
    if (!overflow.isEmpty()) {
        // Sandık dolu, item'i geri ver (rollback)
        player.getInventory().addItem(toRemove);
        return false;
    }
    
    // ✅ 3. Transaction başarılı
    return true;
}

// withdrawItem() - Item çekme (transaction mantığı)
public boolean withdrawItem(Player player, Material material, int amount) {
    // ✅ Yetki kontrolü
    if (!hasWithdrawPermission(clan.getRank(player.getUniqueId()))) {
        player.sendMessage("§cBu işlem için yetkiniz yok!");
        return false;
    }
    
    // ✅ 1. ÖNCE BANKADAN AL
    Inventory bankChest = getBankChest(clan);
    ItemStack toWithdraw = new ItemStack(material, amount);
    HashMap<Integer, ItemStack> removeResult = bankChest.removeItem(toWithdraw);
    
    if (!removeResult.isEmpty()) {
        // Bankada yeterli item yok
        return false;
    }
    
    // ✅ 2. SONRA ENVANTERE EKLE
    HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(toWithdraw);
    
    if (!overflow.isEmpty()) {
        // Envanter dolu, item'i geri ver (rollback)
        bankChest.addItem(toWithdraw);
        return false;
    }
    
    // ✅ 3. Transaction başarılı
    return true;
}
```

**Yetki Kontrolü:**
```java
private boolean hasWithdrawPermission(Clan.Rank rank) {
    switch (rank) {
        case LEADER: return true; // Sınırsız
        case GENERAL: return true; // Sınırsız
        case ELITE: return true; // Limitli (gelecekte)
        case MEMBER: return false; // Çekemez
        case RECRUIT: return false; // Çekemez
        default: return false;
    }
}
```

**Otomatik Maaş Sistemi:**
```java
// distributeSalaries() - Otomatik maaş dağıtımı
public void distributeSalaries() {
    // ✅ Rate limiting (lag önleme)
    int processedClans = 0;
    int maxClansPerTick = 5;
    
    for (Clan clan : clanManager.getAllClans()) {
        if (processedClans >= maxClansPerTick) break;
        
        // ✅ Maaş zamanı kontrolü
        long lastSalaryTime = getLastSalaryTime(clan);
        long currentTime = System.currentTimeMillis();
        long salaryInterval = 24 * 60 * 60 * 1000L; // 24 saat
        
        if (currentTime - lastSalaryTime < salaryInterval) {
            continue; // Henüz zamanı gelmedi
        }
        
        // ✅ Maaş dağıt
        Inventory bankChest = getBankChest(clan);
        if (bankChest == null) continue;
        
        int processedMembers = 0;
        int maxMembersPerClan = 10;
        
        for (UUID memberId : clan.getMembers().keySet()) {
            if (processedMembers >= maxMembersPerClan) break;
            
            Player member = Bukkit.getPlayer(memberId);
            if (member == null || !member.isOnline()) continue;
            
            Clan.Rank rank = clan.getRank(memberId);
            ItemStack salary = getSalaryItem(rank); // Config'den
            
            // ✅ Bankadan maaş çek
            if (bankChest.containsAtLeast(salary, salary.getAmount())) {
                bankChest.removeItem(salary);
                member.getInventory().addItem(salary);
                member.sendMessage("§aMaaşınız alındı: " + salary.getAmount() + "x " + 
                    salary.getType().name());
            }
            
            processedMembers++;
        }
        
        // ✅ Maaş zamanını güncelle
        setLastSalaryTime(clan, currentTime);
        processedClans++;
    }
}
```

**Çalışma Süreci:**
1. Her tick'te `distributeSalaries()` çağrılır
2. Her klan için maaş zamanı kontrol edilir
3. Zamanı gelmişse:
   - Banka sandığı kontrol edilir
   - Her online üye için:
     - Rütbeye göre maaş item'i alınır
     - Bankadan çekilir
     - Oyuncuya verilir
   - Maaş zamanı güncellenir
4. Rate limiting ile lag önlenir

---

## 6. FELAKETLER SİSTEMİ

### 6.1. Felaket Kategorileri ve Seviyeleri

**Dosya:** `Disaster.java`, `DisasterManager.java`

**Kategoriler:**
- **CREATURE:** Canlı felaketler (bosslar)
- **NATURAL:** Doğa olayları (güneş patlaması, deprem)
- **MINI:** Mini felaketler

**Seviye Sistemi (İki Katmanlı):**
1. **Kategori Seviyeleri:** Otomatik spawn sıklığı
   - Seviye 1: Her gün
   - Seviye 2: 3 günde bir
   - Seviye 3: 7 günde bir
2. **İç Seviyeler:** Admin komutunda belirtilen, felaketin gücünü belirler
   - Seviye 1: Zayıf form
   - Seviye 2: Orta form
   - Seviye 3: Güçlü form

**Algoritma:**

```java
// triggerDisaster() - Felaket başlat
public void triggerDisaster(DisasterType type, int categoryLevel, int internalLevel, Location spawnLoc) {
    if (activeDisaster != null && !activeDisaster.isDead()) {
        return; // Zaten aktif felaket var
    }
    
    // ✅ Chunk yükleme
    World world = spawnLoc.getWorld();
    Chunk chunk = world.getChunkAt(spawnLoc);
    if (!chunk.isLoaded()) {
        chunk.load(false);
    }
    
    // ✅ Kategori ve güç hesaplama
    DisasterCategory category = Disaster.getCategory(type);
    DisasterPower power = calculateDisasterPower(internalLevel);
    long duration = Disaster.getDefaultDuration(type, categoryLevel);
    
    // ✅ Entity oluştur (canlı felaketler için)
    Entity entity = null;
    if (category == DisasterCategory.CREATURE) {
        entity = spawnDisasterEntity(type, spawnLoc, power);
    }
    
    // ✅ Felaket oluştur
    activeDisaster = new Disaster(type, category, internalLevel, entity, spawnLoc, power, duration);
    
    // ✅ Handler'ı çağır
    DisasterHandler handler = handlerRegistry.getHandler(type);
    if (handler != null) {
        handler.onDisasterStart(activeDisaster);
    }
    
    // ✅ Broadcast mesajı
    Bukkit.broadcastMessage("§c§lFELAKET BAŞLADI: " + type.getDisplayName());
}

// calculateDisasterPower() - Güç hesaplama
private DisasterPower calculateDisasterPower(int level) {
    // ✅ Oyuncu gücü ve sunucu gücü hesapla
    double playerPower = playerPowerCalculator.calculate();
    double serverPower = serverPowerCalculator.calculate();
    
    // ✅ Seviyeye göre çarpan
    double healthMultiplier = 1.0 + (level - 1) * 0.5; // Seviye 1: 1.0, Seviye 2: 1.5, Seviye 3: 2.0
    double damageMultiplier = 1.0 + (level - 1) * 0.3;
    
    // ✅ Güç hesapla
    double baseHealth = playerPower * 10 + serverPower * 5;
    double baseDamage = playerPower * 2 + serverPower * 1;
    
    return new DisasterPower(
        baseHealth * healthMultiplier,
        baseDamage * damageMultiplier,
        healthMultiplier
    );
}
```

**Çalışma Süreci:**
1. Admin komutu veya otomatik spawn ile felaket başlatılır
2. Felaket tipi, kategori seviyesi ve iç seviye belirlenir
3. Güç hesaplanır (oyuncu gücü + sunucu gücü + seviye çarpanı)
4. Entity oluşturulur (canlı felaketler için)
5. Felaket objesi oluşturulur
6. Handler çağrılır (felaket tipine özel işlemler)
7. Broadcast mesajı gönderilir
8. Felaket task'ı başlatılır (hasar takibi, faz geçişleri, vb.)

---

## 7. DATA PERSISTENCE SİSTEMİ

### 7.1. Atomic Write ve Backup Sistemi

**Dosya:** `DataManager.java`

**Özellikler:**
- Atomic write (geçici dosya + rename)
- Otomatik backup (son 5 backup)
- Error recovery
- Data validation
- Scheduled auto-save

**Algoritma:**

```java
// atomicWrite() - Atomik yazma
private boolean atomicWrite(File file, String content) {
    File tempFile = new File(file.getParent(), file.getName() + ".tmp");
    
    try {
        // ✅ 1. Geçici dosyaya yaz
        FileWriter writer = new FileWriter(tempFile);
        writer.write(content);
        writer.close();
        
        // ✅ 2. Başarılı olursa rename ile taşı
        if (tempFile.exists()) {
            if (file.exists()) {
                file.delete();
            }
            return tempFile.renameTo(file);
        }
        
        return false;
    } catch (IOException e) {
        plugin.getLogger().severe("Atomic write hatası: " + e.getMessage());
        if (tempFile.exists()) {
            tempFile.delete();
        }
        return false;
    }
}

// createBackup() - Backup oluştur
private void createBackup(File file) {
    if (!file.exists()) return;
    
    File backupDir = new File(plugin.getDataFolder(), "backups");
    if (!backupDir.exists()) {
        backupDir.mkdirs();
    }
    
    String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
    File backupFile = new File(backupDir, file.getName() + "_" + timestamp + ".bak");
    
    try {
        Files.copy(file.toPath(), backupFile.toPath());
        
        // ✅ Son 5 backup'ı tut
        File[] backups = backupDir.listFiles((dir, name) -> 
            name.startsWith(file.getName() + "_") && name.endsWith(".bak"));
        
        if (backups != null && backups.length > 5) {
            Arrays.sort(backups, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
            for (int i = 5; i < backups.length; i++) {
                backups[i].delete();
            }
        }
    } catch (IOException e) {
        plugin.getLogger().warning("Backup oluşturulamadı: " + e.getMessage());
    }
}

// saveAll() - Tüm verileri kaydet
public void saveAll(ClanManager clanManager, ...) {
    saveLock.lock();
    try {
        // ✅ Her dosya için ayrı try-catch (bir hata diğerlerini etkilemesin)
        boolean success = true;
        
        // ✅ Backup oluştur
        createBackup(clansFile);
        
        // ✅ Snapshot al
        ClanSnapshot snapshot = createClanSnapshot(clanManager);
        
        // ✅ JSON'a çevir
        String json = gson.toJson(snapshot);
        
        // ✅ Atomik yazma
        if (!atomicWrite(clansFile, json)) {
            plugin.getLogger().severe("Klan verileri kaydedilemedi!");
            success = false;
        }
        
        // ✅ Diğer sistemler için aynı işlem (contracts, shops, vb.)
        // ...
        
        return success;
    } finally {
        saveLock.unlock();
    }
}
```

**Çalışma Süreci:**
1. `saveAll()` çağrılır (sunucu kapanırken veya scheduled task)
2. Her dosya için:
   - Backup oluşturulur
   - Snapshot alınır (runtime veriler → JSON)
   - Atomik yazma yapılır (geçici dosya + rename)
3. Hata durumunda:
   - Log kaydedilir
   - Backup'tan geri yükleme önerisi yapılır
4. Scheduled auto-save (config'den interval)

### 7.2. Data Loading ve Validation

**Algoritma:**

```java
// loadClans() - Klan verilerini yükle
public void loadClans(ClanManager clanManager) {
    if (!clansFile.exists()) {
        plugin.getLogger().info("Klan dosyası bulunamadı, yeni oluşturuluyor.");
        return;
    }
    
    try {
        // ✅ JSON'u oku
        String json = Files.readString(clansFile.toPath());
        
        // ✅ Validation
        if (!isValidJson(json)) {
            plugin.getLogger().warning("Klan dosyası bozuk! Backup'tan geri yükleniyor...");
            restoreFromBackup(clansFile);
            return;
        }
        
        // ✅ Parse et
        ClanSnapshot snapshot = gson.fromJson(json, ClanSnapshot.class);
        
        // ✅ Validation
        if (snapshot == null || snapshot.clans == null) {
            plugin.getLogger().warning("Klan snapshot'ı geçersiz!");
            return;
        }
        
        // ✅ Klanları oluştur
        for (ClanData data : snapshot.clans) {
            // ✅ UUID validation
            if (!isValidUUID(data.id)) {
                plugin.getLogger().warning("Geçersiz UUID: " + data.id);
                continue;
            }
            
            // ✅ Klan oluştur
            Clan clan = new Clan(data.name, UUID.fromString(data.leaderId));
            clan.setId(UUID.fromString(data.id));
            
            // ✅ Üyeleri ekle
            for (Map.Entry<String, String> entry : data.members.entrySet()) {
                UUID memberId = UUID.fromString(entry.getKey());
                Clan.Rank rank = Clan.Rank.valueOf(entry.getValue());
                clan.addMember(memberId, rank);
            }
            
            // ✅ Kristal verilerini yükle
            if (data.crystalLocation != null) {
                Location crystalLoc = deserializeLocation(data.crystalLocation);
                clan.setCrystalLocation(crystalLoc);
                
                // ✅ hasCrystal tutarsızlığı düzelt
                if (data.hasCrystal != null) {
                    clan.setHasCrystal(data.hasCrystal);
                } else {
                    // Eski veriler için: crystalLocation varsa hasCrystal = true
                    clan.setHasCrystal(true);
                }
            }
            
            // ✅ Diğer veriler (territory, structures, vb.)
            // ...
            
            clanManager.addClan(clan);
        }
        
        plugin.getLogger().info("Klan verileri yüklendi: " + snapshot.clans.size() + " klan");
    } catch (Exception e) {
        plugin.getLogger().severe("Klan verileri yüklenirken hata: " + e.getMessage());
        e.printStackTrace();
    }
}
```

**Çalışma Süreci:**
1. Sunucu açıldığında `loadAll()` çağrılır
2. Her dosya için:
   - Dosya var mı kontrol et
   - JSON'u oku
   - Validation yap (JSON format, UUID format, vb.)
   - Parse et
   - Runtime objeleri oluştur
   - Manager'lara ekle
3. Hata durumunda:
   - Log kaydedilir
   - Backup'tan geri yükleme önerisi yapılır
   - Varsayılan değerler kullanılır

---

## 8. SİSTEM ALGORİTMALARI VE ÇALIŞMA SÜREÇLERİ

### 8.1. Yeni Sistem Ekleme Süreci

**Genel Yaklaşım:**

1. **Model Oluşturma:**
   - `model/` klasöründe model sınıfı oluştur
   - `BaseModel`'den türet (id, createdAt, updatedAt)
   - Gerekli field'ları ekle

2. **Manager Oluşturma:**
   - `manager/` klasöründe manager sınıfı oluştur
   - CRUD işlemleri ekle
   - Event handling ekle (gerekirse)

3. **Data Persistence:**
   - `DataManager.java`'ya snapshot sınıfı ekle
   - `create*Snapshot()` metodu ekle
   - `write*Snapshot()` metodu ekle
   - `load*()` metodu ekle
   - `Main.java`'da `onEnable()` ve `onDisable()`'a ekle

4. **Listener Ekleme:**
   - `listener/` klasöründe listener sınıfı oluştur
   - Gerekli event'leri handle et
   - `Main.java`'da register et

5. **Config Ekleme:**
   - `config.yml`'a ayarları ekle
   - Config manager sınıfı oluştur (gerekirse)

6. **Test:**
   - Sunucu restart testi
   - Veri kaybı testi
   - Performans testi

### 8.2. Algoritma Tasarım Prensipleri

**1. Transaction Mantığı:**
- Önce kaynaktan al, sonra hedefe ekle
- Hata durumunda rollback yap
- Dupe önleme için sıralama önemli

**2. Veri Tutarlılığı:**
- İlişkili veriler senkronize tutulmalı
- Tutarsızlık tespit edilirse otomatik düzelt
- Debug logları ekle

**3. Performans:**
- `distanceSquared` kullan (sqrt maliyetinden kaçın)
- Rate limiting ekle (lag önleme)
- Cooldown sistemi kullan
- Chunk yükleme kontrolü yap

**4. Hata Yönetimi:**
- Null check'ler yap
- Try-catch blokları kullan
- Log kaydet
- Kullanıcıya anlaşılır mesaj gönder

**5. Persistence:**
- Atomic write kullan
- Backup sistemi ekle
- Validation yap
- Hata durumunda recovery mekanizması

---

## 📝 ÖZET

Son 3 günde yapılan tüm değişiklikler:

1. **Klan Sistemi:**
   - Kristal persistence ve restore sistemi
   - Veri tutarlılığı düzeltmeleri
   - Kristal kırma ve klan dağıtma sistemi
   - Kapsamlı debug logları

2. **Ritüel Sistemi:**
   - 3 yeni ritüel (Üye Alma, Çıkma, Terfi)
   - Permission kontrolü
   - Farklı yapılar (Soyulmuş Odun, Taş Tuğla)
   - Detaylı hata mesajları

3. **Özel Bloklar:**
   - CustomBlockData utility sistemi
   - Runtime fallback mekanizması
   - Özel blok ekleme rehberi

4. **Territory Boundary:**
   - Dinamik partikül yoğunluğu
   - Flood fill algoritması
   - Performans optimizasyonları

5. **Banka Sistemi:**
   - Transaction mantığı
   - Otomatik maaş dağıtımı
   - Transfer kontratları
   - Yetki kontrolü

6. **Felaketler:**
   - İki katmanlı seviye sistemi
   - Dinamik güç hesaplama
   - Handler registry sistemi

7. **Data Persistence:**
   - Atomic write
   - Backup sistemi
   - Validation
   - Scheduled auto-save

8. **Cache Sistemi ve Optimizasyonlar:**
   - LRU Cache (memory leak önleme)
   - Event-based cache invalidation
   - Chunk-based territory cache
   - Thread-safe cache yapıları
   - Periyodik cache temizleme

9. **Kontrat Sistemi:** ⭐ GÜNCELLENDİ
   - **Çift Taraflı Kontrat Sistemi:**
     - ContractRequest ve ContractTerms yönetimi
     - İki oyuncu arasında karşılıklı anlaşma
     - Her iki tarafın şartlarını belirleme ve onaylama
   - **Wizard Sistemi İyileştirmeleri:** ⭐ YENİ
     - Adım adım kontrat oluşturma (9 adım)
     - Menü başlıklarına adım numarası eklendi (`[Adım 4/9] Ödül Belirle`)
     - Özet menüsünde her iki tarafın şartları gösteriliyor
     - Final onay menüsü iyileştirildi (54 slot, yan yana şartlar)
     - Açıklayıcı mesajlar ve bilgi butonları
     - Her menüde [GERİ] ve [İPTAL] butonları
     - Oyuncu seçimi akışı düzeltildi (istek şartlar belirlendikten sonra gönderiliyor)
   - **Kalıcı Can Kaybı Sistemi:**
     - Kan imzası: -3 kalp (6 can) kalıcı kayıp
     - Kontrat tamamlanınca: +1 kalp (2 can) geri kazanım
     - Kalıcı takip: `permanentHealthLoss` Map ile
   - **Persistence Entegrasyonu:**
     - Aktif kontratlar (`contracts.json`)
     - Kontrat istekleri (`contract_requests.json`)
     - Kontrat şartları (`contract_terms.json`)
   - **Akış Şeması:** Detaylı akış şeması için `KONTRAT_SISTEMI_AKIS_SEMASI.md` dosyasına bakın

10. **Alışveriş (Shop) Sistemi:**
    - Race condition düzeltmeleri
    - Transaction mantığı
    - Teklif sistemi
    - Vergi sistemi

11. **İttifak (Alliance) Sistemi:**
    - 4 ittifak tipi (Defensive, Offensive, Trade, Full)
    - İhlal ve ceza sistemi
    - Persistence entegrasyonu

12. **Virtual Inventory Sistemi:**
    - Klan sanal envanterleri
    - 54 slot envanter
    - Persistence entegrasyonu

13. **Kuşatma (Siege) Sistemi:**
    - Çoklu savaş desteği
    - İki taraflı savaş
    - İttifak kontrolü

14. **Batarya (Battery) Sistemi:**
    - 75 batarya (3 kategori x 5 seviye x 5 batarya)
    - Çakışma sorunu düzeltmesi
    - Merkez blok kontrolü

---

## 9. CACHE SİSTEMİ VE OPTİMİZASYONLAR

### 9.1. Cache Sistemi Genel Bakış

**Amaç:** Performans optimizasyonu ve memory leak önleme

**Temel Prensipler:**
1. **LRU Cache:** En son kullanılmayan entry'ler otomatik silinir
2. **Time-based Cache:** Belirli süre sonra cache geçersiz olur
3. **Event-based Invalidation:** Veri değiştiğinde cache temizlenir
4. **Thread-safe:** ConcurrentHashMap ve synchronized kullanımı
5. **Memory Leak Önleme:** Periyodik temizleme ve maksimum boyut limiti

### 9.2. LRU Cache Implementasyonu

**Dosya:** `util/LRUCache.java`

**Algoritma:**

```java
public class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int maxSize;
    
    public LRUCache(int maxSize) {
        super(16, 0.75f, true); // accessOrder = true (LRU için)
        this.maxSize = maxSize;
    }
    
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxSize; // Max size aşılırsa en eski entry silinir
    }
}
```

**Kullanım:**
- `StratocraftPowerSystem`: Max 500 oyuncu profili cache'i
- En son kullanılmayan entry'ler otomatik silinir
- Memory leak önlenir

### 9.3. TerritoryManager - Chunk-based Cache

**Dosya:** `TerritoryManager.java`

**Algoritma:**

```java
// Chunk-based cache: O(1) lookup için
private final Map<String, UUID> chunkTerritoryCache = new HashMap<>();
private boolean isCacheDirty = true; // Event-based cache güncelleme

public Clan getTerritoryOwner(Location loc) {
    // ✅ Chunk key oluştur
    int chunkX = loc.getBlockX() >> 4;
    int chunkZ = loc.getBlockZ() >> 4;
    String chunkKey = chunkX + ";" + chunkZ;
    
    // ✅ Cache'den kontrol et (O(1) lookup)
    UUID cachedClanId = chunkTerritoryCache.get(chunkKey);
    if (cachedClanId != null) {
        Clan cachedClan = clanManager.getAllClans().stream()
            .filter(c -> c.getId().equals(cachedClanId))
            .findFirst().orElse(null);
        
        if (cachedClan != null && boundaryManager != null) {
            TerritoryData data = boundaryManager.getTerritoryData(cachedClan);
            if (data != null && data.isInsideTerritory(loc)) {
                return cachedClan; // ✅ Cache hit - O(1) lookup
            }
        }
    }
    
    // ✅ Cache miss - Tüm klanları kontrol et (O(N))
    // Bulunursa cache'e ekle
    for (Clan clan : clanManager.getAllClans()) {
        TerritoryData data = boundaryManager.getTerritoryData(clan);
        if (data != null && data.isInsideTerritory(loc)) {
            chunkTerritoryCache.put(chunkKey, clan.getId()); // Cache'e ekle
            return clan;
        }
    }
    
    // ✅ Cache dirty ise güncelle (event-based)
    if (isCacheDirty) {
        updateChunkCache();
        isCacheDirty = false;
    }
    
    return null;
}

// Event-based cache invalidation
public void setCacheDirty() {
    this.isCacheDirty = true; // Bir sonraki lookup'ta güncellenecek
}

// Chunk cache'i güncelle (tüm chunk'ları hesapla)
private void updateChunkCache() {
    chunkTerritoryCache.clear();
    
    for (Clan clan : clanManager.getAllClans()) {
        Territory t = clan.getTerritory();
        if (t == null || t.getCenter() == null) continue;
        
        int radius = t.getRadius();
        Location center = t.getCenter();
        
        // Bölgenin kapsadığı chunk'ları hesapla
        int minChunkX = (center.getBlockX() - radius) >> 4;
        int maxChunkX = (center.getBlockX() + radius) >> 4;
        int minChunkZ = (center.getBlockZ() - radius) >> 4;
        int maxChunkZ = (center.getBlockZ() + radius) >> 4;
        
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                String chunkKey = chunkX + ";" + chunkZ;
                Location chunkCenter = new Location(center.getWorld(), 
                    (chunkX << 4) + 8, center.getY(), (chunkZ << 4) + 8);
                if (GeometryUtil.isInsideRadius(center, chunkCenter, radius)) {
                    chunkTerritoryCache.put(chunkKey, clan.getId());
                }
            }
        }
    }
}
```

**Çalışma Süreci:**
1. `getTerritoryOwner()` çağrılır
2. Chunk key oluşturulur (`chunkX;chunkZ`)
3. Cache'den kontrol edilir (O(1) lookup)
4. Cache hit ise hemen döner
5. Cache miss ise tüm klanları kontrol eder (O(N))
6. Bulunursa cache'e eklenir
7. `isCacheDirty` ise cache güncellenir

**Event-based Invalidation:**
- Klan kristali yerleştirilir → `setCacheDirty()`
- Çit yerleştirilir/kırılır → `setCacheDirty()`
- Alan genişletilir → `setCacheDirty()`
- Klan dağıtılır → `setCacheDirty()`

**Performans:**
- **Cache hit:** O(1) lookup (çok hızlı)
- **Cache miss:** O(N) lookup (nadiren)
- **Cache update:** O(N × M) (N = klan sayısı, M = chunk sayısı)

### 9.4. StratocraftPowerSystem - Güç Profili Cache

**Dosya:** `StratocraftPowerSystem.java`

**Cache Türleri:**

#### 9.4.1. Player Profile Cache (LRU)

```java
// LRU Cache (max 500 entry)
private final Map<UUID, PlayerPowerProfile> playerProfileCache = 
    Collections.synchronizedMap(new LRUCache<>(500));
private final Map<UUID, Long> playerProfileCacheTime = new ConcurrentHashMap<>();
private static final long PLAYER_CACHE_DURATION = 5000L; // 5 saniye

public PlayerPowerProfile calculatePlayerProfile(Player player) {
    UUID playerId = player.getUniqueId();
    long now = System.currentTimeMillis();
    
    // ✅ Cache kontrolü
    PlayerPowerProfile cached = playerProfileCache.get(playerId);
    if (cached != null) {
        Long cacheTime = playerProfileCacheTime.get(playerId);
        if (cacheTime != null && now - cacheTime < PLAYER_CACHE_DURATION) {
            return cached; // ✅ Cache hit
        }
    }
    
    // ✅ Double-check locking (race condition önleme)
    Object lock = playerLocks.computeIfAbsent(playerId, k -> new Object());
    synchronized (lock) {
        // Tekrar kontrol et (başka thread hesaplamış olabilir)
        cached = playerProfileCache.get(playerId);
        if (cached != null) {
            Long cacheTime = playerProfileCacheTime.get(playerId);
            if (cacheTime != null && now - cacheTime < PLAYER_CACHE_DURATION) {
                return cached;
            }
        }
        
        // ✅ Hesaplama (sadece bir thread)
        PlayerPowerProfile profile = calculatePlayerProfileInternal(player, now);
        
        // ✅ Cache'e kaydet (atomic)
        playerProfileCache.put(playerId, profile);
        playerProfileCacheTime.put(playerId, now);
        
        return profile;
    }
}
```

**Özellikler:**
- **LRU Cache:** Max 500 entry, en eski otomatik silinir
- **Time-based:** 5 saniye cache süresi
- **Thread-safe:** Double-check locking ile race condition önleme
- **Memory leak önleme:** LRU ile otomatik temizleme

#### 9.4.2. Offline Player Cache

```java
// Offline player cache (24 saat geçerli)
private final Map<UUID, PlayerPowerProfile> offlinePlayerCache = new ConcurrentHashMap<>();
private final Map<UUID, Long> offlineCacheTime = new ConcurrentHashMap<>();
private static final long OFFLINE_CACHE_DURATION = 86400000L; // 24 saat

public PlayerPowerProfile calculateOfflinePlayerProfile(UUID playerId) {
    long now = System.currentTimeMillis();
    
    // ✅ Offline cache kontrolü
    PlayerPowerProfile cached = offlinePlayerCache.get(playerId);
    if (cached != null) {
        Long cacheTime = offlineCacheTime.get(playerId);
        if (cacheTime != null && now - cacheTime < OFFLINE_CACHE_DURATION) {
            return cached; // ✅ Cache hit
        }
    }
    
    // ✅ Hesaplama (offline oyuncu için)
    PlayerPowerProfile profile = calculateOfflinePlayerProfileInternal(playerId);
    
    // ✅ Cache'e kaydet
    offlinePlayerCache.put(playerId, profile);
    offlineCacheTime.put(playerId, now);
    
    return profile;
}
```

**Özellikler:**
- **24 saat cache:** Offline oyuncular için uzun süreli cache
- **Memory leak önleme:** Periyodik temizleme (5 dakika)

#### 9.4.3. Training Data Cache

```java
// Training data cache (30 saniye)
private final Map<UUID, Map<String, Integer>> trainingDataCache = new ConcurrentHashMap<>();
private final Map<UUID, Long> trainingDataCacheTime = new ConcurrentHashMap<>();
private static final long TRAINING_CACHE_DURATION = 30000L; // 30 saniye

private Map<String, Integer> getPlayerTrainingData(UUID playerId) {
    long now = System.currentTimeMillis();
    
    // ✅ Cache kontrolü
    Map<String, Integer> cached = trainingDataCache.get(playerId);
    if (cached != null) {
        Long cacheTime = trainingDataCacheTime.get(playerId);
        if (cacheTime != null && now - cacheTime < TRAINING_CACHE_DURATION) {
            return cached; // ✅ Cache hit
        }
    }
    
    // ✅ TrainingManager'dan al (thread-safe kopya)
    Map<String, Integer> data = trainingManager.getAllTrainingData().get(playerId);
    if (data == null) return new ConcurrentHashMap<>();
    
    // ✅ Defensive copy (thread-safe)
    Map<String, Integer> playerData = new ConcurrentHashMap<>(data);
    
    // ✅ Cache'e kaydet
    trainingDataCache.put(playerId, playerData);
    trainingDataCacheTime.put(playerId, now);
    
    return playerData;
}
```

**Özellikler:**
- **30 saniye cache:** Training data sık değişmez
- **Thread-safe:** Defensive copy ile

#### 9.4.4. Buff Power Cache (Event-based)

```java
// Buff power cache (event-based)
private final Map<UUID, Double> buffPowerCache = new ConcurrentHashMap<>();

// Event-based cache update
@EventHandler
public void onPotionEffectAdd(PotionEffectAddEvent event) {
    if (event.getEntity() instanceof Player) {
        Player player = (Player) event.getEntity();
        updateBuffPowerCache(player); // ✅ Cache'i güncelle
    }
}

@EventHandler
public void onPotionEffectRemove(PotionEffectRemoveEvent event) {
    if (event.getEntity() instanceof Player) {
        Player player = (Player) event.getEntity();
        updateBuffPowerCache(player); // ✅ Cache'i güncelle
    }
}

private void updateBuffPowerCache(Player player) {
    double totalPower = 0.0;
    for (PotionEffect effect : player.getActivePotionEffects()) {
        int amplifier = effect.getAmplifier() + 1;
        totalPower += amplifier * 10.0;
    }
    buffPowerCache.put(player.getUniqueId(), totalPower); // ✅ Event-based update
}
```

**Özellikler:**
- **Event-based:** Potion effect değiştiğinde cache güncellenir
- **Anlık güncelleme:** Hesaplama yapılmaz, direkt cache'den okunur

#### 9.4.5. Clan Profile Cache

```java
// Clan profile cache (5 dakika)
private final Map<UUID, ClanPowerProfile> clanProfileCache = new ConcurrentHashMap<>();
private static final long CLAN_CACHE_DURATION = 300000L; // 5 dakika

public ClanPowerProfile calculateClanProfile(Clan clan) {
    UUID clanId = clan.getId();
    long now = System.currentTimeMillis();
    
    // ✅ Cache kontrolü
    ClanPowerProfile cached = clanProfileCache.get(clanId);
    if (cached != null && now - cached.getLastUpdate() < CLAN_CACHE_DURATION) {
        return cached; // ✅ Cache hit
    }
    
    // ✅ Double-check locking (race condition önleme)
    Object lock = clanLocks.computeIfAbsent(clanId, k -> new Object());
    synchronized (lock) {
        // Tekrar kontrol et
        cached = clanProfileCache.get(clanId);
        if (cached != null && now - cached.getLastUpdate() < CLAN_CACHE_DURATION) {
            return cached;
        }
        
        // ✅ Hesaplama (sadece bir thread)
        ClanPowerProfile profile = calculateClanProfileInternal(clan, now);
        
        // ✅ Cache'e kaydet
        clanProfileCache.put(clanId, profile);
        
        return profile;
    }
}
```

**Özellikler:**
- **5 dakika cache:** Klan gücü sık değişmez
- **Thread-safe:** Double-check locking ile

### 9.5. HUDManager - HUD Cache

**Dosya:** `HUDManager.java`

**Cache Türleri:**

#### 9.5.1. HUD Data Cache

```java
// HUD cache (5 saniye)
private final Map<UUID, CachedHUDData> hudCache = new ConcurrentHashMap<>();
private static final long CACHE_DURATION = 5000L; // 5 saniye

private static class CachedHUDData {
    UUID clanId;
    List<Contract> contracts;
    Contract bounty;
    long lastUpdate;
    boolean hasNotifications;
}

public HUDLine getContractLine(Player player) {
    UUID playerId = player.getUniqueId();
    long now = System.currentTimeMillis();
    
    // ✅ Cache kontrolü
    CachedHUDData cached = hudCache.get(playerId);
    if (cached != null && now - cached.lastUpdate < CACHE_DURATION) {
        // ✅ Cache'den al
        if (cached.contracts.isEmpty() && cached.bounty == null) {
            return null;
        }
        if (cached.bounty != null) {
            return new HUDLine("§c⚠ Bounty: §6" + (int)cached.bounty.getReward() + " altın");
        }
        return new HUDLine("§e📜 Kontrat: §6" + cached.contracts.size() + " aktif");
    }
    
    // ✅ Cache miss - hesapla
    Clan clan = clanManager.getClanByPlayer(playerId);
    List<Contract> contracts = contractManager.getActiveContracts(playerId);
    Contract bounty = contractManager.getBountyContract(playerId);
    
    // ✅ Cache'e kaydet
    hudCache.put(playerId, new CachedHUDData(
        clan != null ? clan.getId() : null, 
        contracts, bounty, now, hasNotifications));
    
    // ✅ HUD line oluştur
    // ...
}
```

**Özellikler:**
- **5 saniye cache:** HUD sık güncellenir ama cache ile optimize
- **Event-based invalidation:** Kontrat değiştiğinde cache temizlenir

#### 9.5.2. Power Cache

```java
// Power cache (5 saniye)
private final Map<UUID, PlayerPowerProfile> powerCache = new ConcurrentHashMap<>();
private final Map<UUID, Long> powerCacheTime = new ConcurrentHashMap<>();
private static final long POWER_CACHE_DURATION = 5000L; // 5 saniye

private PlayerPowerProfile getPlayerPower(Player player) {
    UUID playerId = player.getUniqueId();
    long now = System.currentTimeMillis();
    
    // ✅ Cache kontrolü
    PlayerPowerProfile profile = powerCache.get(playerId);
    Long cacheTime = powerCacheTime.get(playerId);
    
    if (profile == null || cacheTime == null || now - cacheTime > POWER_CACHE_DURATION) {
        // ✅ Cache miss - StratocraftPowerSystem'den al (kendi cache'i var)
        profile = stratocraftPowerSystem.calculatePlayerProfile(player);
        powerCache.put(playerId, profile);
        powerCacheTime.put(playerId, now);
    }
    
    return profile;
}
```

**Özellikler:**
- **5 saniye cache:** Güç bilgisi sık değişmez
- **Nested cache:** StratocraftPowerSystem'in kendi cache'i de var

### 9.6. ClanBankSystem - Bank Chest Cache

**Dosya:** `ClanBankSystem.java`

**Algoritma:**

```java
// Bank chest cache (5 saniye)
private final Map<UUID, Inventory> bankChestCache = new ConcurrentHashMap<>();
private final Map<UUID, Long> bankChestCacheTime = new ConcurrentHashMap<>();
private static final long BANK_CHEST_CACHE_DURATION = 5000L; // 5 saniye

public Inventory getBankChest(Clan clan) {
    UUID clanId = clan.getId();
    long now = System.currentTimeMillis();
    
    // ✅ Cache kontrolü
    Inventory cached = bankChestCache.get(clanId);
    Long cacheTime = bankChestCacheTime.get(clanId);
    
    if (cached != null && cacheTime != null && now - cacheTime < BANK_CHEST_CACHE_DURATION) {
        return cached; // ✅ Cache hit
    }
    
    // ✅ Cache miss - Sandık konumunu al
    Location chestLoc = getBankChestLocation(clan);
    if (chestLoc == null) {
        // Cache'i temizle
        bankChestCache.remove(clanId);
        bankChestCacheTime.remove(clanId);
        return null;
    }
    
    // ✅ Sandığı al
    Block block = chestLoc.getBlock();
    if (block.getType() != Material.CHEST && block.getType() != Material.TRAPPED_CHEST) {
        // Cache'i temizle
        bankChestCache.remove(clanId);
        bankChestCacheTime.remove(clanId);
        return null;
    }
    
    Chest chest = (Chest) block.getState();
    Inventory inventory = chest.getInventory();
    
    // ✅ Cache'e kaydet
    bankChestCache.put(clanId, inventory);
    bankChestCacheTime.put(clanId, now);
    
    return inventory;
}
```

**Özellikler:**
- **5 saniye cache:** Sandık sık değişmez
- **Null handling:** Sandık yoksa cache temizlenir

### 9.7. BossManager - Nearby Bosses Cache

**Dosya:** `BossManager.java`

**Algoritma:**

```java
// Nearby bosses cache (2 saniye)
private final Map<UUID, List<UUID>> playerNearbyBossesCache = new ConcurrentHashMap<>();
private final Map<UUID, Long> playerNearbyBossesCacheTime = new ConcurrentHashMap<>();
private static final long PLAYER_NEARBY_BOSSES_CACHE_DURATION = 2000L; // 2 saniye

private List<UUID> getNearbyBosses(Player player) {
    UUID playerId = player.getUniqueId();
    long now = System.currentTimeMillis();
    
    // ✅ Cache kontrolü
    Long cacheTime = playerNearbyBossesCacheTime.get(playerId);
    if (cacheTime != null && (now - cacheTime) < PLAYER_NEARBY_BOSSES_CACHE_DURATION) {
        return playerNearbyBossesCache.get(playerId); // ✅ Cache hit
    }
    
    // ✅ Cache miss - Hesapla
    List<UUID> nearbyBossIds = new ArrayList<>();
    Location playerLoc = player.getLocation();
    
    for (Map.Entry<UUID, Boss> entry : activeBosses.entrySet()) {
        Boss boss = entry.getValue();
        if (boss.getEntity() == null) continue;
        
        Location bossLoc = boss.getEntity().getLocation();
        if (playerLoc.getWorld().equals(bossLoc.getWorld()) &&
            playerLoc.distance(bossLoc) <= 50) { // 50 blok mesafe
            nearbyBossIds.add(entry.getKey());
        }
    }
    
    // ✅ Cache'e kaydet
    playerNearbyBossesCache.put(playerId, nearbyBossIds);
    playerNearbyBossesCacheTime.put(playerId, now);
    
    return nearbyBossIds;
}
```

**Özellikler:**
- **2 saniye cache:** BossBar güncellemesi için kısa süreli cache
- **Oyuncu çıkışında temizleme:** Memory leak önleme

### 9.8. StructureEffectManager - Player Clan Cache

**Dosya:** `StructureEffectManager.java`

**Algoritma:**

```java
// Player → Clan cache (5 saniye)
private final Map<UUID, Clan> playerClanCache = new ConcurrentHashMap<>();
private final Map<UUID, Long> playerClanCacheTime = new ConcurrentHashMap<>();
// ✅ Negatif cache (klan yok) için ayrı Set
private final Set<UUID> playerNoClanCache = ConcurrentHashMap.newKeySet();
private static final long PLAYER_CLAN_CACHE_DURATION = 5000L; // 5 saniye

private Clan getPlayerClan(UUID playerId) {
    long now = System.currentTimeMillis();
    
    // ✅ Cache kontrolü
    Long cacheTime = playerClanCacheTime.get(playerId);
    if (cacheTime != null && (now - cacheTime) < PLAYER_CLAN_CACHE_DURATION) {
        // ✅ Negatif cache kontrolü
        if (playerNoClanCache.contains(playerId)) {
            return null; // ✅ Negatif cache - klan yok
        }
        return playerClanCache.get(playerId); // ✅ Cache hit
    }
    
    // ✅ Cache miss - ClanManager'dan al
    Clan clan = clanManager.getClanByPlayer(playerId);
    
    if (clan != null) {
        playerClanCache.put(playerId, clan);
        playerClanCacheTime.put(playerId, now);
        playerNoClanCache.remove(playerId); // Negatif cache'den kaldır
    } else {
        // ✅ Klan yok - negatif cache'e kaydet
        playerNoClanCache.add(playerId);
        playerClanCacheTime.put(playerId, now);
        playerClanCache.remove(playerId); // Cache'den kaldır
    }
    
    return clan;
}
```

**Özellikler:**
- **Negatif cache:** Klan yoksa da cache'lenir (gereksiz arama önleme)
- **5 saniye cache:** Klan üyeliği sık değişmez

### 9.9. DisasterManager - Server Power Cache

**Dosya:** `DisasterManager.java`

**Algoritma:**

```java
// Server power cache (10 saniye)
private double cachedServerPowerNewSystem = 0.0;
private long lastServerPowerUpdate = 0;
private static final long SERVER_POWER_CACHE_DURATION = 10000L; // 10 saniye

public double getServerPowerNewSystem() {
    long now = System.currentTimeMillis();
    
    // ✅ Cache kontrolü
    if (now - lastServerPowerUpdate < SERVER_POWER_CACHE_DURATION) {
        return cachedServerPowerNewSystem; // ✅ Cache hit
    }
    
    // ✅ Cache miss - Hesapla
    if (stratocraftPowerSystem == null) {
        cachedServerPowerNewSystem = 0.0;
        return 0.0;
    }
    
    // ✅ Tüm online oyuncuların güç puanlarını topla (cache kullanır)
    double totalPower = 0.0;
    int playerCount = 0;
    
    for (Player player : Bukkit.getOnlinePlayers()) {
        if (player == null || !player.isOnline()) continue;
        
        PlayerPowerProfile profile = stratocraftPowerSystem.calculatePlayerProfile(player);
        totalPower += profile.getTotalPower();
        playerCount++;
    }
    
    if (playerCount == 0) {
        cachedServerPowerNewSystem = 0.0;
        return 0.0;
    }
    
    // ✅ Ortalama güç × oyuncu sayısı çarpanı
    double averagePower = totalPower / playerCount;
    double playerCountMultiplier = Math.sqrt(playerCount); // Kök çarpanı
    
    cachedServerPowerNewSystem = averagePower * playerCountMultiplier;
    lastServerPowerUpdate = now;
    
    return cachedServerPowerNewSystem;
}

// ✅ Cache temizleme (oyuncu giriş/çıkışında)
public void clearServerPowerCache() {
    cachedServerPowerNewSystem = 0.0;
    lastServerPowerUpdate = 0;
}
```

**Özellikler:**
- **10 saniye cache:** Server power sık değişmez
- **Event-based invalidation:** Oyuncu giriş/çıkışında cache temizlenir

### 9.10. CustomBlockData - PDC Cache

**Dosya:** `CustomBlockData.java`

**Algoritma:**

```java
// PDC cache (5 saniye)
private static final Map<Location, UUID> pdcCache = new ConcurrentHashMap<>();
private static final Map<Location, Long> pdcCacheTime = new ConcurrentHashMap<>();
private static final long PDC_CACHE_DURATION = 5000L; // 5 saniye

public static UUID getClanFenceData(Block block) {
    if (block == null) return null;
    
    Location loc = block.getLocation();
    long now = System.currentTimeMillis();
    
    // ✅ Cache kontrolü
    Long cacheTime = pdcCacheTime.get(loc);
    if (cacheTime != null && now - cacheTime < PDC_CACHE_DURATION) {
        return pdcCache.get(loc); // ✅ Cache hit
    }
    
    // ✅ Cache miss - PDC'den oku
    UUID clanId = readFromPDC(block);
    
    // ✅ Cache'e kaydet
    if (clanId != null) {
        pdcCache.put(loc, clanId);
        pdcCacheTime.put(loc, now);
    }
    
    return clanId;
}

// ✅ Periyodik cache temizleme
public static void cleanupCache() {
    long now = System.currentTimeMillis();
    pdcCacheTime.entrySet().removeIf(entry -> 
        now - entry.getValue() > PDC_CACHE_DURATION);
    pdcCache.entrySet().removeIf(entry -> 
        !pdcCacheTime.containsKey(entry.getKey()));
}
```

**Özellikler:**
- **5 saniye cache:** PDC okuma maliyetli
- **Periyodik temizleme:** Eski entry'ler otomatik silinir

### 9.11. Periyodik Cache Temizleme

**Dosya:** `StratocraftPowerSystem.java`

**Algoritma:**

```java
// Periyodik cache temizleme (5 dakika)
private void startCacheCleanupTask() {
    Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
        long now = System.currentTimeMillis();
        
        // ✅ Player profile cache temizleme
        long playerExpireTime = now - (PLAYER_CACHE_DURATION * 2); // 10 saniye
        playerProfileCacheTime.entrySet().removeIf(entry -> 
            entry.getValue() < playerExpireTime);
        playerProfileCache.entrySet().removeIf(entry -> 
            !playerProfileCacheTime.containsKey(entry.getKey()));
        
        // ✅ Offline cache temizleme (24 saat)
        long offlineExpireTime = now - OFFLINE_CACHE_DURATION;
        offlineCacheTime.entrySet().removeIf(entry -> 
            entry.getValue() < offlineExpireTime);
        offlinePlayerCache.entrySet().removeIf(entry -> 
            !offlineCacheTime.containsKey(entry.getKey()));
        
        // ✅ Training data cache temizleme (30 saniye)
        long trainingExpireTime = now - TRAINING_CACHE_DURATION;
        trainingDataCacheTime.entrySet().removeIf(entry -> 
            entry.getValue() < trainingExpireTime);
        trainingDataCache.entrySet().removeIf(entry -> 
            !trainingDataCacheTime.containsKey(entry.getKey()));
        
        // ✅ Player lookup cache temizleme (offline oyuncular)
        playerLookupCache.entrySet().removeIf(entry -> {
            Player player = entry.getValue();
            return player == null || !player.isOnline();
        });
    }, 0L, 6000L); // Her 5 dakika (300 saniye = 6000 tick)
}
```

**Özellikler:**
- **Async task:** Ana thread'i bloklamaz
- **Periyodik temizleme:** 5 dakikada bir
- **Memory leak önleme:** Eski entry'ler otomatik silinir

### 9.12. Cache Optimizasyon Prensipleri

**1. Time-based Cache:**
- Kısa süreli cache: 2-5 saniye (sık değişen veriler)
- Orta süreli cache: 30 saniye - 5 dakika (orta sıklıkta değişen)
- Uzun süreli cache: 24 saat (offline oyuncular)

**2. Event-based Invalidation:**
- Veri değiştiğinde cache temizlenir
- `setCacheDirty()` ile işaretleme
- Lazy update (bir sonraki lookup'ta güncelleme)

**3. Thread Safety:**
- `ConcurrentHashMap` kullanımı
- Double-check locking (race condition önleme)
- Per-entity locks (clan locks, player locks)

**4. Memory Leak Önleme:**
- LRU Cache (max size limiti)
- Periyodik temizleme
- Oyuncu çıkışında cache temizleme
- Negatif cache (null değerler için)

**5. Performans:**
- O(1) lookup (cache hit)
- O(N) lookup (cache miss, nadiren)
- Batch processing (N+1 problem çözümü)
- Parallel stream (çoklu hesaplama)

---

## 10. KONTRAT SİSTEMİ

### 10.1. Çift Taraflı Kontrat Sistemi ⭐ GÜNCELLENDİ

**Dosya:** `ContractManager.java`, `ContractRequestManager.java`, `ContractTermsManager.java`, `ContractMenu.java`

**Yeni Özellikler:**
- **ContractRequest:** Kontrat isteği gönderme/kabul etme
- **ContractTerms:** Her iki tarafın şartlarını belirleme
- **Çift Taraflı Kontrat:** İki oyuncu arasında karşılıklı anlaşma
- **Wizard Sistemi:** Adım adım kontrat oluşturma (9 adım)
- **Menü İyileştirmeleri:** Adım numaraları, açıklayıcı mesajlar, her iki tarafın şartları

**Algoritma:**

```java
// ContractRequestManager - İstek gönderme
public ContractRequest sendRequest(UUID sender, UUID target, Contract.ContractScope scope) {
    // ✅ Aynı istek zaten var mı kontrol et
    ContractRequest existing = requests.stream()
        .filter(r -> r.getSender().equals(sender) && 
                    r.getTarget().equals(target) && 
                    r.getStatus() == ContractRequestStatus.PENDING)
        .findFirst()
        .orElse(null);
    
    if (existing != null) {
        return null; // Zaten bekleyen bir istek var
    }
    
    // ✅ Yeni istek oluştur
    ContractRequest request = new ContractRequest(sender, target, scope);
    requests.add(request);
    
    // ✅ Bildirim gönder
    Player targetPlayer = Bukkit.getPlayer(target);
    if (targetPlayer != null && targetPlayer.isOnline()) {
        targetPlayer.sendMessage("§e§lYENİ KONTRAT İSTEĞİ!");
        // HUD'a bildirim ekle
        plugin.getHUDManager().addContractNotification(targetPlayer.getUniqueId(), 
            "Yeni kontrat isteği: " + senderName, 
            ContractNotificationType.INFO);
    }
    
    return request;
}

// ContractTermsManager - Şart oluşturma
public ContractTerms createTerms(UUID requestId, UUID playerId, ContractWizardState state) {
    ContractTerms terms = new ContractTerms(requestId, playerId, state.contractType);
    
    // ✅ Tip'e göre parametreleri set et
    switch (state.contractType) {
        case RESOURCE_COLLECTION:
            terms.setMaterial(state.material);
            terms.setAmount(state.amount);
            break;
        case COMBAT:
            terms.setTargetPlayer(state.targetPlayer);
            break;
        case TERRITORY:
            terms.setRestrictedAreas(state.restrictedAreas);
            terms.setRestrictedRadius(state.restrictedRadius);
            break;
        case CONSTRUCTION:
            terms.setStructureType(state.structureType);
            break;
    }
    
    // ✅ Genel parametreler
    terms.setDeadline(System.currentTimeMillis() + (long)(state.deadlineDays * 24 * 60 * 60 * 1000));
    terms.setReward(state.reward);
    terms.setPenaltyType(state.penaltyType);
    terms.setPenalty(state.penalty);
    
    allTerms.add(terms);
    return terms;
}
```

**Çalışma Süreci:**
1. Oyuncu A, kontrat oluşturma wizard'ını başlatır
2. Tip, kapsam, oyuncu seçer
3. Şartları belirler (ödül, ceza, süre, tip'e özel)
4. Özet menüsünde [ONAYLA VE GÖNDER] tıklar
5. ✅ İstek gönderilir (ContractRequest oluşturulur)
6. ✅ Sender'ın şartları kaydedilir (ContractTerms)
7. ✅ Sender'ın şartları otomatik onaylanır
8. ✅ Target oyuncuya bildirim gönderilir
9. Target isteği görür ve iki seçenek:
   - **Direkt Kabul:** Sender'ın şartlarını direkt kabul eder
   - **Şart Ekle:** Kendi şartlarını belirler
10. Target şartlarını belirledikten sonra Sender'a "Son Onay Gerekiyor" mesajı gider
11. Sender Final Onay Menüsü'nde her iki tarafın şartlarını görür
12. Sender onaylarsa → Kontrat aktif olur
13. Her iki taraf şartlarını yerine getirmeye çalışır
14. Tamamlanma veya ihlal durumunda ceza/ödül uygulanır

### 10.2. Kontrat Wizard Sistemi İyileştirmeleri ⭐ YENİ

**Dosya:** `ContractMenu.java`

**Yapılan İyileştirmeler:**

#### 10.2.1. Menü Başlıklarına Adım Numarası Eklendi
- Her menüde adım numarası gösteriliyor (örn: `[Adım 4/9] Ödül Belirle`)
- Oyuncu hangi adımda olduğunu anlıyor
- Toplam adım sayısı gösteriliyor

**Güncellenen Menüler:**
```java
openTypeSelectionMenu() → "[Adım 1/9] Kontrat Tipi Seç"
openScopeSelectionMenu() → "[Adım 2/9] Kontrat Kapsamı Seç"
openPlayerSelectionMenuForRequest() → "[Adım 3/9] Hedef Oyuncu Seç"
openRewardSliderMenu() → "[Adım 4/9] Ödül Belirle"
openSummaryMenu() → "[Adım 9/9] Kontrat Özeti"
```

#### 10.2.2. Özet Menüsünde Her İki Tarafın Şartları
- Target şartlarını belirledikten sonra özet menüsünde:
  - "SİZİN ŞARTLARINIZ" bölümü
  - "KARŞI TARAFIN ŞARTLARI" bölümü (eğer varsa)
- Her iki tarafın şartları karşılaştırılabilir şekilde gösteriliyor

**Kod:**
```java
// openSummaryMenu() içinde
if (state.contractRequestId != null && contractRequestManager != null) {
    ContractRequest request = contractRequestManager.getRequest(state.contractRequestId);
    if (request != null) {
        UUID otherPlayerId = request.getSender().equals(player.getUniqueId()) ? 
            request.getTarget() : request.getSender();
        ContractTerms otherTerms = 
            contractTermsManager.getTermsByRequest(state.contractRequestId, otherPlayerId);
        
        if (otherTerms != null) {
            summaryLore.add("§7§lKARŞI TARAFIN ŞARTLARI:");
            summaryLore.addAll(createTermsLore(otherTerms, otherName, false));
        }
    }
}
```

#### 10.2.3. Final Onay Menüsü İyileştirildi
- Daha büyük menü (54 slot = 6x9)
- Her iki tarafın şartları yan yana gösteriliyor:
  - Slot 20: Sizin Şartlarınız (sol taraf)
  - Slot 24: Karşı Tarafın Şartları (sağ taraf)
- Açıklayıcı başlık: "⚠️ SON ONAY GEREKİYOR!"
- [✅ ONAYLA] ve [❌ REDDET] butonları net bir şekilde yerleştirildi

#### 10.2.4. Açıklayıcı Mesajlar ve Bilgi Butonları
- Özet menüsünde açıklayıcı bilgi mesajları eklendi
- Oyuncu seçim menüsünde bilgi butonu eklendi (Slot 49)
- Her adımda oyuncuya ne yapması gerektiği açıklanıyor

**Örnek Mesajlar:**
```
"ℹ️ Oyuncu seçildikten sonra şartları belirleyeceksiniz."
"ℹ️ İstek şartlar belirlendikten sonra gönderilecek."
"ℹ️ Bu şartlar karşı tarafa gönderilecek."
"ℹ️ Karşı taraf kabul ederse kontrat aktif olacak."
```

#### 10.2.5. Her Menüde İptal ve Geri Butonları
- Özet menüsünde [İPTAL] butonu var
- Oyuncu seçim menüsünde [İPTAL] butonu eklendi (Slot 53)
- Tüm menülerde [GERİ] butonu var
- İptal edildiğinde state temizleniyor
- Geri gidildiğinde önceki adıma dönülüyor

#### 10.2.6. Oyuncu Seçimi Akışı Düzeltmesi
- **ÖNCEKİ SORUN:** Oyuncu seçildiğinde istek hemen gönderiliyordu
- **YENİ ÇÖZÜM:** Oyuncu seçildiğinde sadece state'e kaydediliyor, istek gönderilmiyor
- Şartlar belirlendikten sonra özet menüsünde [ONAYLA VE GÖNDER] tıklanınca istek gönderiliyor

**Kod:**
```java
// handlePlayerSelectionClick() içinde
if (currentMenuTitle.equals("§6Hedef Oyuncu Seç") && 
    state.scope == Contract.ContractScope.PLAYER_TO_PLAYER) {
    // ✅ Oyuncuyu state'e kaydet (istek gönderilmeden önce)
    state.targetPlayerForRequest = targetUUID;
    state.step = 2;
    openRewardSliderMenu(player);
    // İstek gönderilmez, sadece state'e kaydedilir
}

// createContractFromState() içinde
if (state.scope == Contract.ContractScope.PLAYER_TO_PLAYER && 
    state.targetPlayerForRequest != null) {
    // ✅ Şimdi istek gönder (şartlar belirlendikten sonra)
    ContractRequest request = contractRequestManager.sendRequest(
        player.getUniqueId(), 
        state.targetPlayerForRequest, 
        state.scope
    );
    // Şartları kaydet...
}
```

### 10.3. Kalıcı Can Kaybı Sistemi

**Dosya:** `ContractManager.java`

**Algoritma:**

```java
// Kalıcı can kaybı takibi
private final Map<UUID, Integer> permanentHealthLoss = new ConcurrentHashMap<>();

// Kan imzası - can kaybı
public void signContractWithBlood(Player player) {
    // ✅ -3 kalp can kaybı (6 can)
    double currentMaxHealth = player.getMaxHealth();
    double newMaxHealth = Math.max(1.0, currentMaxHealth - 6.0);
    player.setMaxHealth(newMaxHealth);
    
    // ✅ Kalıcı can kaybını kaydet
    int lostHearts = permanentHealthLoss.getOrDefault(player.getUniqueId(), 0);
    permanentHealthLoss.put(player.getUniqueId(), lostHearts + 3);
}

// Kontrat tamamlandığında can geri kazanımı
public void restorePermanentHealth(UUID playerId, int hearts) {
    Player player = Bukkit.getPlayer(playerId);
    if (player == null || !player.isOnline()) return;
    
    // ✅ Can kaybını azalt
    int lostHearts = permanentHealthLoss.getOrDefault(playerId, 0);
    if (lostHearts <= 0) return;
    
    int restoreAmount = Math.min(hearts, lostHearts);
    permanentHealthLoss.put(playerId, lostHearts - restoreAmount);
    
    // ✅ Can geri kazan (1 kalp = 2 can)
    double currentMaxHealth = player.getMaxHealth();
    double newMaxHealth = currentMaxHealth + (restoreAmount * 2.0);
    player.setMaxHealth(Math.min(20.0, newMaxHealth)); // Max 20 can
    
    player.sendMessage("§aKontrat tamamlandı! " + restoreAmount + " kalp canınız geri kazandınız.");
}
```

**Özellikler:**
- Kan imzası: -3 kalp (6 can) kalıcı kayıp
- Kontrat tamamlanınca: +1 kalp (2 can) geri kazanım
- Kalıcı takip: `permanentHealthLoss` Map ile

### 10.3. Kontrat Persistence

**Dosya:** `DataManager.java`

**Kaydedilen Veriler:**
- Aktif kontratlar (`contracts.json`)
- Kontrat istekleri (`contract_requests.json`)
- Kontrat şartları (`contract_terms.json`)

**Algoritma:**

```java
// ContractSnapshot oluşturma
private ContractSnapshot createContractSnapshot(ContractManager contractManager) {
    ContractSnapshot snapshot = new ContractSnapshot();
    
    for (Contract contract : contractManager.getContracts()) {
        ContractData data = new ContractData();
        data.id = contract.getId().toString();
        data.issuer = contract.getIssuer().toString();
        data.acceptor = contract.getAcceptor() != null ? contract.getAcceptor().toString() : null;
        data.type = contract.getType().name();
        data.scope = contract.getScope().name();
        data.reward = contract.getReward();
        data.penalty = contract.getPenalty();
        data.deadline = contract.getDeadline();
        data.completed = contract.isCompleted();
        data.breached = contract.isBreached();
        
        // ✅ Çift taraflı kontrat verileri
        if (contract.isBilateralContract()) {
            data.playerA = contract.getPlayerA() != null ? contract.getPlayerA().toString() : null;
            data.playerB = contract.getPlayerB() != null ? contract.getPlayerB().toString() : null;
            data.contractRequestId = contract.getContractRequestId() != null ? 
                contract.getContractRequestId().toString() : null;
            data.contractStatus = contract.getContractStatus() != null ? 
                contract.getContractStatus().name() : null;
        }
        
        snapshot.contracts.add(data);
    }
    
    return snapshot;
}
```

**Çalışma Süreci:**
1. Sunucu kapanırken `saveAll()` çağrılır
2. Tüm aktif kontratlar snapshot'a alınır
3. JSON'a çevrilir ve `contracts.json`'a yazılır
4. ContractRequest ve ContractTerms ayrı dosyalara yazılır
5. Sunucu açılırken `loadAll()` çağrılır
6. JSON'dan okunur ve runtime objeleri oluşturulur

### 10.5. Akış Şeması Dökümanı ⭐ YENİ

**Dosya:** `KONTRAT_SISTEMI_AKIS_SEMASI.md`

Detaylı akış şeması ve iyileştirmeler için bu dosyaya bakın:
- İlk gönderen oyuncu (Sender) akışı
- Hedef oyuncu (Target) akışı
- Sender'ın son onay akışı
- Tüm iyileştirmeler ve çözümler

---

## 11. ALIŞVERIŞ (SHOP) SİSTEMİ

### 11.1. Race Condition Düzeltmeleri

**Dosya:** `ShopManager.java`

**Sorun:** Ödeme alındıktan sonra stok kontrolü yapılıyordu, race condition riski vardı.

**Çözüm:** Transaction mantığı ve stok tekrar kontrolü.

**Algoritma:**

```java
public void handlePurchase(Player buyer, Shop shop) {
    // ✅ 1. Kendinle ticaret engelleme
    if (shop.getOwnerId().equals(buyer.getUniqueId())) {
        buyer.sendMessage("§cKendi marketinden alışveriş yapamazsın!");
        return;
    }
    
    // ✅ 2. Null check'ler
    ItemStack priceItem = shop.getPriceItem();
    ItemStack sellingItem = shop.getSellingItem();
    if (priceItem == null || sellingItem == null) {
        buyer.sendMessage("§cMarket bilgileri hatalı!");
        return;
    }
    
    // ✅ 3. Stok kontrolü - ÖNCE (ödeme alınmadan önce)
    Chest chest = (Chest) shop.getLocation().getBlock().getState();
    if (!chest.getInventory().containsAtLeast(sellingItem, sellingItem.getAmount())) {
        buyer.sendMessage("§cMarket stoğu tükenmiş!");
        return;
    }
    
    // ✅ 4. Ödeme kontrolü
    if (!buyer.getInventory().containsAtLeast(priceItem, priceItem.getAmount())) {
        buyer.sendMessage("§cYeterli ödemeye sahip değilsin!");
        return;
    }
    
    // ✅ 5. Ödemeyi al (clone kullan - orijinal item'ı koru)
    ItemStack paymentClone = priceItem.clone();
    HashMap<Integer, ItemStack> removeResult = buyer.getInventory().removeItem(paymentClone);
    
    if (!removeResult.isEmpty()) {
        buyer.sendMessage("§cÖdeme alınamadı! Lütfen tekrar deneyin.");
        return;
    }
    
    // ✅ 6. Stok TEKRAR kontrolü (race condition önleme)
    if (!chest.getInventory().containsAtLeast(sellingItem, sellingItem.getAmount())) {
        // Stok tükenmiş, ödemeyi geri ver (rollback)
        buyer.getInventory().addItem(paymentClone);
        buyer.sendMessage("§cMarket stoğu tükenmiş! Ödemeniz iade edildi.");
        return;
    }
    
    // ✅ 7. Item'i al (sandıktan)
    ItemStack itemToGive = sellingItem.clone();
    HashMap<Integer, ItemStack> removeFromChest = chest.getInventory().removeItem(itemToGive);
    
    if (!removeFromChest.isEmpty()) {
        // Sandıktan alınamadı, ödemeyi geri ver (rollback)
        buyer.getInventory().addItem(paymentClone);
        buyer.sendMessage("§cMarket stoğu tükenmiş! Ödemeniz iade edildi.");
        return;
    }
    
    // ✅ 8. Item'i ver (envantere)
    HashMap<Integer, ItemStack> overflow = buyer.getInventory().addItem(itemToGive);
    
    if (!overflow.isEmpty()) {
        // Envanter dolu, hem item'i hem ödemeyi geri ver (rollback)
        chest.getInventory().addItem(itemToGive);
        buyer.getInventory().addItem(paymentClone);
        buyer.sendMessage("§cEnvanteriniz dolu! İşlem iptal edildi.");
        return;
    }
    
    // ✅ 9. Ödemeyi sandığa ekle
    HashMap<Integer, ItemStack> paymentOverflow = chest.getInventory().addItem(paymentClone);
    
    if (!paymentOverflow.isEmpty()) {
        // Sandık dolu, ödemeyi geri ver (ama item verildi, bu edge case)
        buyer.getInventory().addItem(paymentOverflow.values().iterator().next());
        buyer.sendMessage("§eMarket sandığı dolu, ödemeniz iade edildi.");
    }
    
    // ✅ 10. Vergi hesapla (eğer korumalı bölgedeyse)
    boolean isProtectedZone = false;
    if (plugin != null && plugin.getTerritoryManager() != null) {
        Clan territoryOwner = plugin.getTerritoryManager().getTerritoryOwner(shop.getLocation());
        isProtectedZone = (territoryOwner != null);
    }
    
    if (isProtectedZone) {
        double tax = paymentClone.getAmount() * getTaxPercentage();
        // Vergi işlemi (territory owner'a ver)
    }
    
    buyer.sendMessage("§aAlışveriş başarılı!");
}
```

**Çalışma Süreci:**
1. Null check'ler
2. Stok kontrolü (ödeme alınmadan önce)
3. Ödeme kontrolü
4. Ödemeyi al (transaction başlat)
5. Stok tekrar kontrolü (race condition önleme)
6. Item'i sandıktan al
7. Item'i envantere ekle
8. Envanter overflow kontrolü (rollback gerekirse)
9. Ödemeyi sandığa ekle
10. Vergi hesapla (koruma bölgesindeyse)

### 11.2. Teklif Sistemi

**Dosya:** `Shop.java`, `ShopManager.java`

**Algoritma:**

```java
// Shop.java - Offer sınıfı
public static class Offer {
    private final UUID offerer; // Teklif veren
    private final ItemStack offerItem; // Teklif edilen item
    private final int offerAmount; // Teklif miktarı
    private final long offerTime; // Teklif zamanı
    private boolean accepted = false;
    private boolean rejected = false;
}

// ShopManager.java - Teklif ekleme
public void addOffer(Shop shop, Player offerer, ItemStack offerItem, int amount) {
    // ✅ Maksimum teklif kontrolü
    if (shop.getOffers().size() >= shop.getMaxOffers()) {
        offerer.sendMessage("§cBu market için maksimum teklif sayısına ulaşıldı!");
        return;
    }
    
    // ✅ Envanter kontrolü
    if (!offerer.getInventory().containsAtLeast(offerItem, amount)) {
        offerer.sendMessage("§cYeterli item yok!");
        return;
    }
    
    // ✅ Teklif oluştur
    Shop.Offer offer = new Shop.Offer(offerer.getUniqueId(), offerItem, amount);
    shop.getOffers().add(offer);
    
    // ✅ Market sahibine bildirim
    Player owner = Bukkit.getPlayer(shop.getOwnerId());
    if (owner != null && owner.isOnline()) {
        owner.sendMessage("§eYeni teklif: " + offerer.getName() + " - " + amount + "x " + offerItem.getType().name());
    }
}
```

**Özellikler:**
- Maksimum teklif sayısı (config'den)
- Teklif kabul/red sistemi
- Otomatik teklif temizleme (süresi dolanlar)

---

## 12. İTTİFAK (ALLIANCE) SİSTEMİ

### 12.1. İttifak Tipleri ve Persistence

**Dosya:** `AllianceManager.java`, `Alliance.java`

**İttifak Tipleri:**
- **DEFENSIVE:** Savunma İttifakı (birine saldırılırsa diğeri yardım eder)
- **OFFENSIVE:** Saldırı İttifakı (birlikte saldırı yapılır)
- **TRADE:** Ticaret İttifakı (ticaret bonusları)
- **FULL:** Tam İttifak (en güçlü)

**Algoritma:**

```java
// Alliance oluşturma
public Alliance createAlliance(UUID clan1Id, UUID clan2Id, Alliance.Type type, long durationDays) {
    // ✅ Zaten ittifak var mı kontrol et
    if (hasAlliance(clan1Id, clan2Id)) {
        return null;
    }
    
    // ✅ Cooldown kontrolü (spam önleme)
    long now = System.currentTimeMillis();
    Long lastAllianceTime = allianceCooldowns.get(clan1Id);
    if (lastAllianceTime != null && now - lastAllianceTime < ALLIANCE_COOLDOWN) {
        return null; // Cooldown'da
    }
    
    // ✅ İttifak oluştur
    Alliance alliance = new Alliance(clan1Id, clan2Id, type, durationDays);
    activeAlliances.add(alliance);
    
    // ✅ Cooldown ekle
    allianceCooldowns.put(clan1Id, now);
    allianceCooldowns.put(clan2Id, now);
    
    return alliance;
}

// İttifak ihlal etme
public void breakAlliance(UUID allianceId, UUID breakerClanId) {
    Alliance alliance = getAlliance(allianceId);
    if (alliance == null || !alliance.isActive()) return;
    
    // ✅ İttifakı ihlal et
    alliance.breakAlliance(breakerClanId);
    
    // ✅ Cezalar
    Clan breakerClan = clanManager.getClanById(breakerClanId);
    if (breakerClan != null) {
        // İhlal cezası: Klan bakiyesinin %20'si
        double penalty = breakerClan.getBalance() * 0.2;
        breakerClan.withdraw(penalty);
        
        // Broadcast
        Bukkit.broadcastMessage("§4§lİTTİFAK İHLALİ! §c" + breakerClan.getName() + 
            " klanı ittifakı bozdu! Cezası: " + penalty + " altın");
        
        // ✅ İhlal eden klan üyelerine "Hain" etiketi
        for (UUID memberId : breakerClan.getMembers().keySet()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.sendMessage("§4§l[HAİN] §cİttifak ihlali nedeniyle cezalandırıldınız!");
            }
        }
    }
}
```

**Persistence:**
- `AllianceSnapshot` ile kaydedilir
- `alliances.json` dosyasına yazılır
- Sunucu açılırken geri yüklenir

---

## 13. VIRTUAL INVENTORY SİSTEMİ

### 13.1. Klan Sanal Envanterleri

**Dosya:** `VirtualStorageListener.java`

**Algoritma:**

```java
// Virtual inventory oluşturma
public Inventory getVirtualInventory(Clan clan) {
    UUID clanId = clan.getId();
    
    // ✅ Cache kontrolü
    Inventory cached = virtualInventories.get(clanId);
    if (cached != null) {
        return cached;
    }
    
    // ✅ Yeni inventory oluştur
    Inventory inventory = Bukkit.createInventory(null, 54, 
        "§6Klan Envanteri: " + clan.getName());
    
    // ✅ Cache'e kaydet
    virtualInventories.put(clanId, inventory);
    
    return inventory;
}

// Item ekleme
public boolean addItemToVirtualInventory(Clan clan, ItemStack item) {
    Inventory inventory = getVirtualInventory(clan);
    
    // ✅ Envanter overflow kontrolü
    HashMap<Integer, ItemStack> overflow = inventory.addItem(item);
    
    if (!overflow.isEmpty()) {
        return false; // Envanter dolu
    }
    
    return true;
}
```

**Özellikler:**
- Her klan için 54 slot sanal envanter
- Cache ile optimize edilmiş
- Persistence ile kaydedilir

---

## 14. KUŞATMA (SIEGE) SİSTEMİ

### 14.1. Çoklu Savaş Desteği

**Dosya:** `SiegeManager.java`, `Clan.java`

**Algoritma:**

```java
// Clan.java - Savaşta olunan klanlar
private final Set<UUID> warringClans = Collections.synchronizedSet(new HashSet<>());

public void addWarringClan(UUID clanId) {
    if (clanId != null && !clanId.equals(this.id)) {
        warringClans.add(clanId);
    }
}

public boolean isAtWarWith(UUID clanId) {
    return warringClans.contains(clanId);
}

// SiegeManager.java - İki taraflı savaş başlatma
public void startSiege(Clan attacker, Clan defender, Player attackerPlayer) {
    // ✅ İttifak kontrolü
    if (allianceManager != null && allianceManager.hasAlliance(attacker.getId(), defender.getId())) {
        attackerPlayer.sendMessage("§cİttifaklı klanlara saldıramazsın!");
        return;
    }
    
    // ✅ Her iki klanı da savaşta işaretle
    attacker.addWarringClan(defender.getId());
    defender.addWarringClan(attacker.getId());
    
    // ✅ Eski sistem (geriye uyumluluk)
    activeSieges.put(defender, attacker);
    
    // ✅ Broadcast
    Bukkit.broadcastMessage("§c§lKUŞATMA BAŞLADI!");
    Bukkit.broadcastMessage("§7Saldıran: " + attacker.getName());
    Bukkit.broadcastMessage("§7Savunan: " + defender.getName());
}
```

**Özellikler:**
- Çoklu savaş desteği (bir klan birden fazla klanla savaşabilir)
- İki taraflı savaş (her iki klan da birbirine saldırabilir)
- İttifak kontrolü (ittifaklı klanlara saldırılamaz)

---

## 15. BATARYA (BATTERY) SİSTEMİ

### 15.1. 75 Batarya Sistemi

**Dosya:** `NewBatteryManager.java`

**Kategoriler:**
- **Saldırı Bataryaları:** 25 batarya (hasar veren)
- **Oluşturma Bataryaları:** 25 batarya (yapı yapan)
- **Destek Bataryaları:** 25 batarya (şifa, hız, zırh)

**Seviyeler:**
- L1: 5 batarya/kategori (toplam 15)
- L2: 5 batarya/kategori (toplam 15)
- L3: 5 batarya/kategori (toplam 15)
- L4: 5 batarya/kategori (toplam 15)
- L5: 5 batarya/kategori (toplam 15)

### 15.2. Çakışma Sorunu Düzeltmesi

**Sorun:** Farklı tarifli bataryalar çakışıyordu.

**Çözüm:** Merkez blok kontrolü.

**Algoritma:**

```java
// Merkez blok kontrolü ile çakışma önleme
private BatteryData checkBatteryRecipe(Block centerBlock) {
    // ✅ Önce merkez bloğa göre filtrele
    List<RecipeChecker> matchingCenterBlock = allRecipeCheckers.stream()
        .filter(checker -> checker.getPattern().getCenterBlock() == centerBlock.getType())
        .collect(Collectors.toList());
    
    // ✅ Sadece aynı merkez bloğu olan tarifler kontrol ediliyor
    for (RecipeChecker checker : matchingCenterBlock) {
        if (checker.checkRecipe(centerBlock)) {
            return checker.getBatteryData();
        }
    }
    
    return null;
}
```

**Özellikler:**
- Merkez blok kontrolü ile çakışma önleme
- Her batarya için özel `RecipeChecker` interface'i
- Esnek `BlockPattern` sistemi

---

**Son Güncelleme:** Son 3 Gün (Son Commit'ler)  
**Döküman Versiyonu:** 1.2
