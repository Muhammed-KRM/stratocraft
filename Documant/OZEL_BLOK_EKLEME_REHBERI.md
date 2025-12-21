# Özel Blok Ekleme Rehberi

Bu rehber, Stratocraft plugin'ine yeni bir özel blok (custom block) eklemek için adım adım talimatlar içerir. Bu rehber, klan çiti, yapı çekirdeği ve tuzak çekirdeği gibi çalışan örneklerden faydalanarak hazırlanmıştır.

---

## 📋 İçindekiler

1. [Özel Blok Ekleme Adımları](#1-özel-blok-ekleme-adımları)
2. [Karşılaşılabilecek Sorunlar ve Çözümleri](#2-karşılaşılabilecek-sorunlar-ve-çözümleri)
3. [Örnekler](#3-örnekler)

---

## 1. Özel Blok Ekleme Adımları

### Adım 1: ItemManager'da Item Oluşturma

**Dosya:** `src/main/java/me/mami/stratocraft/manager/ItemManager.java`

#### 1.1. Static Field Tanımlama

ItemManager sınıfının başına (diğer static field'ların yanına) yeni item'ınız için static field ekleyin:

```java
public static ItemStack MY_CUSTOM_BLOCK; // Yeni Özel Blok
```

**Örnek:**
```java
public static ItemStack CLAN_FENCE; // Klan Çiti
public static ItemStack STRUCTURE_CORE; // Yapı Çekirdeği
public static ItemStack TRAP_CORE; // Tuzak Çekirdeği
```

#### 1.2. Item Oluşturma (init() Metodunda)

`init()` metodunda (genellikle `registerRecipes()` çağrılmadan önce) item'ınızı oluşturun:

```java
MY_CUSTOM_BLOCK = create(Material.MATERIAL_TYPE, "MY_CUSTOM_BLOCK_ID", "§6§lYeni Özel Blok",
    Arrays.asList("§7Açıklama satırı 1", "§7Açıklama satırı 2"));
```

**Önemli Parametreler:**
- `Material.MATERIAL_TYPE`: Blok tipi (OAK_FENCE, OAK_LOG, LODESTONE, vb.)
- `"MY_CUSTOM_BLOCK_ID"`: Item'ı tanımlamak için kullanılan ID (büyük harf, alt çizgi ile)
- `"§6§lYeni Özel Blok"`: Görünen isim (renk kodları ile)
- `Arrays.asList(...)`: Lore (açıklama) satırları

**Örnek:**
```java
CLAN_FENCE = create(Material.OAK_FENCE, "CLAN_FENCE", "§6§lKlan Çiti",
    Arrays.asList("§7Klan bölgesi sınırlarını belirler."));
```

#### 1.3. Recipe Ekleme (Opsiyonel)

Eğer item craft edilebilir olacaksa, `registerRecipes()` metodunda recipe ekleyin:

```java
private void registerMyCustomBlockRecipe() {
    if (MY_CUSTOM_BLOCK == null) {
        MY_CUSTOM_BLOCK = create(Material.MATERIAL_TYPE, "MY_CUSTOM_BLOCK_ID", "§6§lYeni Özel Blok",
            Arrays.asList("§7Açıklama"));
    }
    
    ShapedRecipe recipe = new ShapedRecipe(
        new NamespacedKey(Main.getInstance(), "my_custom_block"), 
        MY_CUSTOM_BLOCK
    );
    recipe.shape("ABC", "DEF", "GHI"); // Craft pattern
    recipe.setIngredient('A', Material.INGREDIENT_TYPE);
    // ... diğer ingredient'lar
    
    Bukkit.addRecipe(recipe);
}
```

**Önemli:** `registerRecipes()` metodunda bu yeni metodunuzu çağırın.

---

### Adım 2: CustomBlockData'da PDC Metodları Ekleme

**Dosya:** `src/main/java/me/mami/stratocraft/util/CustomBlockData.java`

#### 2.1. NamespacedKey Tanımlama

Sınıfın başına (diğer key'lerin yanına) yeni key'inizi ekleyin:

```java
private static final NamespacedKey MY_CUSTOM_BLOCK_KEY = 
    new NamespacedKey("stratocraft", "my_custom_block");
```

**Örnek:**
```java
private static final NamespacedKey CLAN_FENCE_KEY = 
    new NamespacedKey("stratocraft", "clan_fence");
```

#### 2.2. Runtime Set Ekleme (Opsiyonel - TileState Olmayan Bloklar İçin)

Eğer blok TileState değilse (OAK_FENCE, LODESTONE gibi), runtime fallback ekleyin:

```java
private static final java.util.Set<String> myCustomBlockRuntime =
    java.util.concurrent.ConcurrentHashMap.newKeySet();
```

**Örnek:**
```java
private static final java.util.Set<String> clanFenceRuntime =
    java.util.concurrent.ConcurrentHashMap.newKeySet();
```

#### 2.3. setData() Metodu Ekleme

Blok verisini kaydetmek için metod ekleyin:

```java
/**
 * Yeni özel blok verisini kaydet
 * 
 * @param block Blok
 * @return Başarılıysa true
 */
public static boolean setMyCustomBlockData(Block block) {
    if (block == null) return false;

    // ✅ TileState olmayan bloklar için: Önce runtime'a yaz
    String rtKey = runtimeKey(block);
    if (rtKey != null) {
        myCustomBlockRuntime.add(rtKey);
    }
    
    try {
        // Chunk yükleme kontrolü
        org.bukkit.Chunk chunk = block.getChunk();
        if (!chunk.isLoaded()) {
            chunk.load(false);
            if (!chunk.isLoaded()) {
                // Chunk yüklenemedi ama runtime'a eklendi
                return true;
            }
        }
        
        // TileState kontrolü
        BlockState state = block.getState();
        PersistentDataContainer container = null;
        boolean isTileState = false;
        
        if (state instanceof TileState) {
            TileState tileState = (TileState) state;
            container = tileState.getPersistentDataContainer();
            isTileState = true;
        } else {
            // TileState değilse CustomBlockData kütüphanesi kullan
            container = getCustomBlockDataContainer(block);
            if (container == null) {
                return true; // Runtime'a eklendi
            }
        }
        
        if (container == null) {
            return true; // Runtime'a eklendi
        }
        
        // Veriyi kaydet (BYTE, STRING, UUID gibi tip kullanabilirsiniz)
        container.set(MY_CUSTOM_BLOCK_KEY, PersistentDataType.BYTE, (byte) 1);
        
        // Cache'i temizle
        if (!isTileState) {
            clearPDCCache(block);
        }
        
        // TileState ise update() çağır
        if (isTileState) {
            ((TileState) state).update();
        }
        
        return true;
    } catch (Exception e) {
        if (plugin != null) {
            plugin.getLogger().warning("Yeni özel blok verisi kaydedilemedi: " + e.getMessage());
        }
        return true; // Runtime'a eklendi
    }
}
```

#### 2.4. isData() Metodu Ekleme

Blok verisini kontrol etmek için metod ekleyin:

```java
/**
 * Yeni özel blok mu kontrol et
 * 
 * @param block Blok
 * @return Yeni özel blok ise true
 */
public static boolean isMyCustomBlock(Block block) {
    if (block == null || block.getType() != Material.MATERIAL_TYPE) {
        return false;
    }

    // ✅ Önce runtime kontrolü (TileState olmayan bloklar için)
    String rtKey = runtimeKey(block);
    if (rtKey != null && myCustomBlockRuntime.contains(rtKey)) {
        return true;
    }
    
    try {
        // Chunk yükleme kontrolü
        org.bukkit.Chunk chunk = block.getChunk();
        if (!chunk.isLoaded()) {
            boolean loaded = chunk.load(false);
            if (!loaded || !chunk.isLoaded()) {
                return false;
            }
        }
        
        // BlockState al
        BlockState state = block.getState();
        PersistentDataContainer container = null;
        
        if (state instanceof TileState) {
            TileState tileState = (TileState) state;
            container = tileState.getPersistentDataContainer();
        } else {
            container = getCustomBlockDataContainer(block);
            if (container == null) {
                return false;
            }
        }
        
        // PDC kontrolü
        if (container.has(MY_CUSTOM_BLOCK_KEY, PersistentDataType.BYTE)) {
            return true;
        }
        
        return false;
    } catch (Exception e) {
        return false;
    }
}
```

#### 2.5. removeData() Metodu Ekleme

Blok verisini temizlemek için metod ekleyin:

```java
/**
 * Yeni özel blok verisini temizle
 * 
 * @param block Blok
 */
public static void removeMyCustomBlockData(Block block) {
    if (block == null) return;

    // ✅ Önce runtime'dan temizle
    String rtKey = runtimeKey(block);
    if (rtKey != null) {
        myCustomBlockRuntime.remove(rtKey);
    }
    
    try {
        BlockState state = block.getState();
        PersistentDataContainer container = null;
        boolean isTileState = false;
        
        if (state instanceof TileState) {
            TileState tileState = (TileState) state;
            container = tileState.getPersistentDataContainer();
            isTileState = true;
        } else {
            container = getCustomBlockDataContainer(block);
            if (container == null) {
                return;
            }
        }
        
        if (container != null) {
            container.remove(MY_CUSTOM_BLOCK_KEY);
            
            if (isTileState) {
                ((TileState) state).update();
            } else {
                clearPDCCache(block);
            }
        }
    } catch (Exception e) {
        if (plugin != null) {
            plugin.getLogger().warning("Yeni özel blok verisi temizlenemedi: " + e.getMessage());
        }
    }
}
```

**Önemli:** `runtimeKey()` metodu zaten mevcut, sadece kullanın.

---

### Adım 3: Listener Oluşturma ve Event Handling

**Dosya:** Yeni listener dosyası oluşturun veya mevcut bir listener'a ekleyin (örn: `TerritoryListener.java`)

#### 3.1. BlockPlaceEvent - Yerleştirme

```java
@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
public void onMyCustomBlockPlace(BlockPlaceEvent event) {
    ItemStack item = event.getItemInHand();
    
    // ✅ ÖNCE: Item kontrolü (blok yerleştirilmeden önce)
    if (!ItemManager.isCustomItem(item, "MY_CUSTOM_BLOCK_ID")) {
        return;
    }
    
    // ✅ Blok yerleştirildikten SONRA işaretle
    Block placed = event.getBlockPlaced();
    if (placed == null || placed.getType() != Material.MATERIAL_TYPE) {
        return;
    }
    
    // ✅ KRİTİK: PDC'ye yaz
    me.mami.stratocraft.util.CustomBlockData.setMyCustomBlockData(placed);
    
    // ✅ Opsiyonel: Memory'de tut (eğer manager varsa)
    // myCustomBlockManager.addBlock(placed.getLocation(), player.getUniqueId());
    
    Player player = event.getPlayer();
    player.sendMessage("§a§lYeni özel blok yerleştirildi!");
}
```

#### 3.2. BlockPlaceEvent - MONITOR Priority (Opsiyonel - Güvenlik İçin)

Eğer PDC yazımı başarısız olabilirse, MONITOR priority'de tekrar deneyin:

```java
@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
public void onMyCustomBlockPlaceRestore(BlockPlaceEvent event) {
    Block block = event.getBlock();
    ItemStack item = event.getItemInHand();
    
    if (block.getType() == Material.MATERIAL_TYPE) {
        if (ItemManager.isCustomItem(item, "MY_CUSTOM_BLOCK_ID")) {
            // ✅ MONITOR priority'de blok kesinlikle dünyada, PDC yazımını garantile
            me.mami.stratocraft.util.CustomBlockData.setMyCustomBlockData(block);
        }
    }
}
```

#### 3.3. BlockBreakEvent - Kırma

```java
@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
public void onMyCustomBlockBreak(BlockBreakEvent event) {
    Block block = event.getBlock();
    
    // Material kontrolü
    if (block.getType() != Material.MATERIAL_TYPE) {
        return;
    }
    
    // Chunk yükleme kontrolü (PDC okumak için)
    org.bukkit.Chunk chunk = block.getChunk();
    if (!chunk.isLoaded()) {
        try {
            chunk.load(false);
        } catch (Exception e) {
            return;
        }
    }
    
    // ✅ Blok kontrolü
    if (!me.mami.stratocraft.util.CustomBlockData.isMyCustomBlock(block)) {
        return; // Normal blok
    }
    
    // ✅ Normal drop'ları iptal et
    event.setDropItems(false);
    
    // ✅ Özel item'ı drop et
    ItemStack customBlockItem = ItemManager.MY_CUSTOM_BLOCK != null ? 
        ItemManager.MY_CUSTOM_BLOCK.clone() : null;
    
    if (customBlockItem != null) {
        block.getWorld().dropItemNaturally(block.getLocation(), customBlockItem);
    }
    
    // ✅ Veriyi temizle
    me.mami.stratocraft.util.CustomBlockData.removeMyCustomBlockData(block);
    
    // ✅ Opsiyonel: Memory'den temizle
    // myCustomBlockManager.removeBlock(block.getLocation());
}
```

**Önemli Event Priority'ler:**
- `HIGH`: Yerleştirme işaretleme (diğer listener'lar önce çalışsın)
- `HIGHEST`: Kırma işlemi (diğer listener'lar override etmesin)
- `MONITOR`: Ek güvence (blok kesinlikle dünyada)

---

### Adım 4: AdminCommandExecutor'da Komut Ekleme

**Dosya:** `src/main/java/me/mami/stratocraft/command/AdminCommandExecutor.java`

#### 4.1. Komut Handler'larına Ekleme

`getItemByNameConstruction()`, `getItemByNameUtility()` veya `getItemByNameAllCategories()` metodlarından birine ekleyin:

```java
case "my_custom_block":
case "yeni_ozel_blok":
    // ✅ KRİTİK: ItemManager'dan clone() kullan (yanlış PDC key kullanmayın!)
    return ItemManager.MY_CUSTOM_BLOCK != null ? ItemManager.MY_CUSTOM_BLOCK.clone() : null;
```

**ÖNEMLİ UYARI:** ❌ **YENİ BİR METOD OLUŞTURUP YANLIŞ PDC KEY KULLANMAYIN!**
- ❌ Yanlış: `"clan_item"` key kullanmak
- ✅ Doğru: `ItemManager.MY_CUSTOM_BLOCK.clone()` kullanmak (doğru `"custom_id"` key'i otomatik gelir)

**Yanlış Örnek (YAPMAYIN):**
```java
// ❌ YANLIŞ - Yeni metod oluşturup yanlış key kullanmak
private ItemStack createMyCustomBlock() {
    ItemStack block = new ItemStack(Material.MATERIAL_TYPE);
    meta.getPersistentDataContainer().set(
        new NamespacedKey(plugin, "clan_item"), // ❌ YANLIŞ KEY!
        PersistentDataType.STRING, "MY_BLOCK"
    );
    return block;
}
```

**Doğru Örnek:**
```java
// ✅ DOĞRU - ItemManager'dan clone() kullanmak
return ItemManager.MY_CUSTOM_BLOCK != null ? ItemManager.MY_CUSTOM_BLOCK.clone() : null;
```

#### 4.2. Help Mesajlarına Ekleme

`showHelp()` metodunda item listesine ekleyin (opsiyonel).

---

### Adım 5: Main.java'da Listener Kaydetme

**Dosya:** `src/main/java/me/mami/stratocraft/Main.java`

Yeni listener oluşturduysanız, `onEnable()` metodunda kaydedin:

```java
Bukkit.getPluginManager().registerEvents(new MyCustomBlockListener(manager), this);
```

---

## 2. Karşılaşılabilecek Sorunlar ve Çözümleri

### Sorun 1: Item Tanınmıyor (isCustomItem false dönüyor)

**Semptomlar:**
- Log'da `isCustomItem: false` görünüyor
- Blok yerleştirildiğinde özel işaretleme yapılmıyor

**Nedenler:**
1. ✅ **Komut ile item verirken yanlış PDC key kullanılıyor**
   - **Çözüm:** `ItemManager.MY_CUSTOM_BLOCK.clone()` kullanın, yeni metod oluşturup `"clan_item"` gibi yanlış key kullanmayın

2. ✅ **ItemManager'da item oluşturulurken yanlış ID kullanılmış**
   - **Çözüm:** `create()` metodunda ID'nin büyük harf ve doğru olduğundan emin olun

**Kontrol Adımları:**
```java
// Item'ın PDC'sini kontrol edin
ItemStack item = ...;
String customId = item.getItemMeta().getPersistentDataContainer()
    .get(new NamespacedKey(Main.getInstance(), "custom_id"), PersistentDataType.STRING);
System.out.println("Custom ID: " + customId); // "MY_CUSTOM_BLOCK_ID" olmalı
```

---

### Sorun 2: Blok Kırıldığında Normal Item Düşüyor

**Semptomlar:**
- Blok kırıldığında özel item değil, normal vanilla item düşüyor

**Nedenler:**
1. ✅ **isCustomBlock() false dönüyor**
   - **Çözüm:** Runtime kontrolü ekleyin (TileState olmayan bloklar için)
   - **Çözüm:** Chunk yükleme kontrolü yapın

2. ✅ **Event priority çok düşük**
   - **Çözüm:** `HIGHEST` priority kullanın ki diğer listener'lar override etmesin

3. ✅ **setDropItems(false) çağrılmıyor**
   - **Çözüm:** `onMyCustomBlockBreak()` metodunda `event.setDropItems(false)` çağrısını ekleyin

**Debug Adımları:**
```java
// isCustomBlock() metoduna debug log ekleyin
public static boolean isMyCustomBlock(Block block) {
    plugin.getLogger().info("[DEBUG] isMyCustomBlock kontrol: " + block.getLocation());
    // ... kontroller
}
```

---

### Sorun 3: PDC Yazılamıyor / Okunamıyor

**Semptomlar:**
- Blok yerleştirildiğinde PDC yazılmıyor
- Blok kırıldığında PDC'den okunamıyor

**Nedenler:**
1. ✅ **Chunk yüklü değil**
   - **Çözüm:** Chunk yükleme kontrolü ekleyin:
   ```java
   org.bukkit.Chunk chunk = block.getChunk();
   if (!chunk.isLoaded()) {
       chunk.load(false);
       if (!chunk.isLoaded()) {
           // Chunk yüklenemedi, runtime'a ekleyin
           return true;
       }
   }
   ```

2. ✅ **TileState olmayan blok için CustomBlockData container alınamıyor**
   - **Çözüm:** Runtime fallback kullanın:
   ```java
   private static final java.util.Set<String> myCustomBlockRuntime =
       java.util.concurrent.ConcurrentHashMap.newKeySet();
   ```

3. ✅ **BlockPlaceEvent'te blok henüz dünyada değil**
   - **Çözüm:** `getBlockPlaced()` kullanın, `getBlock()` değil
   - **Çözüm:** MONITOR priority'de tekrar deneyin

---

### Sorun 4: Server Restart Sonrası Veri Kayboluyor

**Semptomlar:**
- Server restart sonrası bloklar özel olarak tanınmıyor

**Nedenler:**
1. ✅ **PDC yazılmıyor (sadece runtime kullanılıyor)**
   - **Çözüm:** PDC yazımını garantilemek için MONITOR priority event ekleyin
   - **Çözüm:** Chunk yükleme kontrolü yapın

2. ✅ **TileState olmayan blok için CustomBlockData container null**
   - **Çözüm:** `getCustomBlockDataContainer()` metodunun doğru çalıştığından emin olun
   - **Çözüm:** Cache temizleme yapın: `clearPDCCache(block)`

---

### Sorun 5: Item'lar Stacklenmiyor

**Semptomlar:**
- Aynı özel blok item'ları stacklenmiyor

**Nedenler:**
1. ✅ **Item'a ownerId veya benzersiz veri yazılıyor**
   - **Çözüm:** Item'a ownerId YAZMAYIN, sadece blok PDC'sine yazın
   - **Çözüm:** `onMyCustomBlockBreak()` metodunda clone() edilen item'a ek veri eklemeyin

**Doğru Örnek:**
```java
// ✅ DOĞRU - Owner verisi item'a yazılmıyor
ItemStack customBlockItem = ItemManager.MY_CUSTOM_BLOCK.clone();
block.getWorld().dropItemNaturally(block.getLocation(), customBlockItem);
```

**Yanlış Örnek:**
```java
// ❌ YANLIŞ - Owner verisi item'a yazılıyor (stacklenmeyi engeller)
ItemStack customBlockItem = ItemManager.MY_CUSTOM_BLOCK.clone();
customBlockItem.getItemMeta().getPersistentDataContainer()
    .set(ownerKey, PersistentDataType.STRING, playerId.toString()); // ❌ YANLIŞ!
```

---

### Sorun 6: Event Priority Çakışması

**Semptomlar:**
- Başka bir listener override ediyor
- Drop işlemi çalışmıyor

**Nedenler:**
1. ✅ **Priority çok düşük**
   - **Çözüm:** 
     - Yerleştirme: `HIGH` priority kullanın
     - Kırma: `HIGHEST` priority kullanın

**Örnek:**
```java
@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true) // Yerleştirme
@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true) // Kırma
```

---

### Sorun 7: Admin Bypass Sorunları

**Semptomlar:**
- Admin bypass ile blok yerleştirilince işaretleme yapılmıyor

**Nedenler:**
1. ✅ **Admin bypass return ediyor, işaretleme yapılmıyor**
   - **Çözüm:** Admin bypass kontrolünü item kontrolünden SONRA yapın:
   ```java
   if (!ItemManager.isCustomItem(item, "MY_CUSTOM_BLOCK_ID")) {
       // Normal blok kontrolü - bypass kontrolü burada
       if (!hasAdminBypass) {
           event.setCancelled(true);
       }
       return;
   }
   
   // ✅ Admin bypass olsa bile özel blok işaretlemesi yapılmalı
   CustomBlockData.setMyCustomBlockData(placed);
   ```

---

## 3. Örnekler

### Örnek 1: Klan Çiti (Basit Boolean Bayrak)

**Özellikler:**
- TileState değil (OAK_FENCE)
- Sadece boolean bayrak tutuyor (clanId yok)
- Stacklenebilir

**CustomBlockData Metodları:**
```java
// Sadece bayrak kaydediyor
container.set(CLAN_FENCE_KEY, PersistentDataType.BYTE, (byte) 1);

// Sadece bayrak kontrolü yapıyor
container.has(CLAN_FENCE_KEY, PersistentDataType.BYTE);
```

**Dosyalar:**
- `ItemManager.java`: `CLAN_FENCE` static field
- `CustomBlockData.java`: `setClanFenceData()`, `isClanFence()`, `removeClanFenceData()`
- `TerritoryListener.java`: `onFencePlace()`, `onFenceBreak()`

---

### Örnek 2: Yapı Çekirdeği (UUID ile)

**Özellikler:**
- TileState (OAK_LOG)
- UUID tutuyor (ownerId)
- Stacklenebilir (item'da UUID yok, sadece blok PDC'sinde)

**CustomBlockData Metodları:**
```java
// UUID kaydediyor
container.set(STRUCTURE_CORE_KEY, PersistentDataType.STRING, ownerId.toString());

// UUID okuyor
String ownerIdStr = container.get(STRUCTURE_CORE_KEY, PersistentDataType.STRING);
```

**Dosyalar:**
- `ItemManager.java`: `STRUCTURE_CORE` static field
- `CustomBlockData.java`: `setStructureCoreData()`, `getStructureCoreOwner()`, `removeStructureCoreData()`
- `StructureCoreListener.java`: `onStructureCorePlace()`, `onStructureCoreBreak()`
- `StructureCoreManager.java`: Memory'de de tutuluyor

---

### Örnek 3: Tuzak Çekirdeği (UUID ile + Memory)

**Özellikler:**
- TileState değil (LODESTONE)
- UUID tutuyor (ownerId)
- Memory'de de tutuluyor (TrapManager)
- Stacklenebilir

**CustomBlockData Metodları:**
```java
// UUID kaydediyor
container.set(TRAP_CORE_KEY, PersistentDataType.STRING, ownerId.toString());
```

**Özel Özellikler:**
- `PlayerInteractEvent` ile yerleştiriliyor (BlockPlaceEvent değil)
- Memory'de de tutuluyor: `trapManager.registerInactiveTrapCore()`

**Dosyalar:**
- `ItemManager.java`: `TRAP_CORE` static field
- `CustomBlockData.java`: `setTrapCoreData()`, `getTrapCoreOwner()`, `removeTrapCoreData()`
- `TrapListener.java`: `onTrapInteract()`, `onTrapCoreBreak()`
- `TrapManager.java`: Memory yönetimi

---

## 📝 Checklist

Yeni özel blok eklerken bu checklist'i takip edin:

- [ ] ItemManager'da static field tanımladım
- [ ] ItemManager.init()'te item oluşturdum
- [ ] Recipe ekledim (opsiyonel)
- [ ] CustomBlockData'da NamespacedKey tanımladım
- [ ] Runtime set ekledim (TileState olmayan bloklar için)
- [ ] setData() metodu ekledim
- [ ] isData() metodu ekledim
- [ ] removeData() metodu ekledim
- [ ] Listener'da BlockPlaceEvent (HIGH priority) ekledim
- [ ] Listener'da BlockPlaceEvent (MONITOR priority - opsiyonel) ekledim
- [ ] Listener'da BlockBreakEvent (HIGHEST priority) ekledim
- [ ] AdminCommandExecutor'da komut ekledim (ItemManager'dan clone() kullandım)
- [ ] Main.java'da listener kaydettim (yeni listener ise)
- [ ] Test ettim: Item tanınıyor mu?
- [ ] Test ettim: Blok yerleştirilince işaretleniyor mu?
- [ ] Test ettim: Blok kırılınca özel item düşüyor mu?
- [ ] Test ettim: Server restart sonrası çalışıyor mu?
- [ ] Test ettim: Item'lar stackleniyor mu?

---

## 🔍 Debug İpuçları

### Debug Log Ekleme

Kritik noktalara debug log ekleyin:

```java
plugin.getLogger().info("[MY_CUSTOM_BLOCK] Event tetiklendi");
plugin.getLogger().info("[MY_CUSTOM_BLOCK] isCustomItem: " + isCustomItem);
plugin.getLogger().info("[MY_CUSTOM_BLOCK] setData() sonucu: " + result);
plugin.getLogger().info("[MY_CUSTOM_BLOCK] isData() sonucu: " + isData);
```

### Console'da Kontrol

Test sırasında console log'larını kontrol edin:
- `[MY_CUSTOM_BLOCK]` etiketli mesajları arayın
- `isCustomItem: false` görüyorsanız → Item tanıma sorunu
- `setData() sonucu: false` görüyorsanız → PDC yazma sorunu
- `isData() sonucu: false` görüyorsanız → PDC okuma sorunu

---

## ✅ Başarı Kriterleri

Yeni özel blok başarıyla eklendiğinde:

1. ✅ `/stratocraft give material my_custom_block` komutu çalışıyor
2. ✅ Item envanterde doğru görünüyor (isim, lore)
3. ✅ Item yere konulunca özel blok olarak işaretleniyor
4. ✅ Özel blok kırılınca özel item düşüyor (normal item değil)
5. ✅ Aynı özel blok item'ları stackleniyor
6. ✅ Server restart sonrası bloklar hala özel olarak tanınıyor

---

**Son Güncelleme:** 2025-12-21
**Versiyon:** 1.0
**Hazırlayan:** AI Assistant (Klan Çiti sorunlarından öğrenilenler ile)
