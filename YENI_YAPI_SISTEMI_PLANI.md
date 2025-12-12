# 🏗️ Yeni Yapı Sistemi Planı

## 📋 İÇİNDEKİLER

1. [Genel Bakış](#genel-bakış)
2. [Sistem Mimarisi](#sistem-mimarisi)
3. [Yapı Çekirdeği Sistemi](#yapı-çekirdeği-sistemi)
4. [Tarif Sistemi](#tarif-sistemi)
5. [Aktivasyon Sistemi](#aktivasyon-sistemi)
6. [Yapı Kategorileri](#yapı-kategorileri)
7. [Kod Yapısı](#kod-yapısı)
8. [Örnekler](#örnekler)
9. [Geçiş Planı](#geçiş-planı)

---

## 🎯 GENEL BAKIŞ

### Mevcut Sistem
- ❌ Yapılar şemalarla (WorldEdit .schem dosyaları) kontrol ediliyor
- ❌ Shift + Sağ Tık ile BLUEPRINT item'ı ile aktifleştiriliyor
- ❌ Tüm yapılar için aynı aktivasyon yöntemi

### Yeni Sistem
- ✅ **Yapı Çekirdeği**: Her yapının bir çekirdeği olacak (tuzaklardaki gibi)
- ✅ **Kod İçi Tarifler**: Basit yapılar kod içinde tariflenecek
- ✅ **Şema Tarifleri**: Karmaşık yapılar hala şema ile kontrol edilecek
- ✅ **Item Bazlı Aktivasyon**: Her yapıyı aktifleştirmek için farklı item'lar gerekecek

### Temel Prensip
**Tuzak sistemindeki mantık:**
- Tuzak Çekirdeği (Trap Core) → Etrafında Magma Block çerçevesi → Yakıt ile aktifleştirme
- **Yapı Çekirdeği (Structure Core)** → Etrafında bloklar → Aktivasyon item'ı ile aktifleştirme

---

## 🏛️ SİSTEM MİMARİSİ

### 1. Yapı Çekirdeği (Structure Core)

**Özellikler:**
- Özel bir item (STRUCTURE_CORE) olacak
- Yerleştirildiğinde bir blok oluşturacak (veya mevcut bir blok çekirdek olarak işaretlenecek)
- Çekirdek, yapının merkez noktası olacak
- Çekirdek etrafındaki bloklar tarif ile kontrol edilecek

**Item:**
```java
STRUCTURE_CORE = create(Material.END_CRYSTAL, "STRUCTURE_CORE", "§e§lYapı Çekirdeği",
    Arrays.asList(
        "§7Yapıların temel taşı",
        "§7Yerleştir ve etrafına yapıyı kur",
        "§7Aktivasyon item'ı ile aktifleştir"
    ));
```

### 2. Tarif Sistemi

**İki Tip Tarif:**

#### A. Kod İçi Tarifler (Basit Yapılar)
- Görev Loncası, Banka, Kontrat Bürosu gibi basit yapılar
- Kod içinde `BlockRecipe` sınıfı ile tanımlanacak
- Merkez bloğa göre relative pozisyonlar

**Örnek:**
```java
BlockRecipe missionGuildRecipe = new BlockRecipe()
    .setCore(Material.END_CRYSTAL) // Yapı çekirdeği
    .addBlock(0, -1, 0, Material.COBBLESTONE) // Altında kırıktaş
    .addBlock(0, 1, 0, Material.LECTERN); // Üstünde kürsü
```

#### B. Şema Tarifleri (Karmaşık Yapılar)
- Simya Kulesi, Tektonik Sabitleyici gibi karmaşık yapılar
- WorldEdit .schem dosyaları ile kontrol edilecek
- Mevcut `StructureValidator` sistemi kullanılacak

### 3. Aktivasyon Sistemi

**Item Bazlı Aktivasyon:**
- Her yapı tipi için farklı aktivasyon item'ı
- Basit yapılar: Demir, Odun, Taş, Kömür, Elmas, Altın
- Karmaşık yapılar: Özel boss itemleri, Titanyum, vb.

**Aktivasyon İşlemi:**
1. Oyuncu yapı çekirdeğini yerleştirir
2. Etrafına yapıyı kurar (tarife göre)
3. Aktivasyon item'ı elinde tutarak çekirdeğe sağ tıklar
4. Sistem tarifi kontrol eder
5. Doğruysa → Yapı aktif olur

---

## 🔧 YAPI ÇEKİRDEĞİ SİSTEMİ

### Yapı Çekirdeği Item'ı

**ItemManager.java:**
```java
public static ItemStack STRUCTURE_CORE;

// init() içinde:
STRUCTURE_CORE = create(Material.END_CRYSTAL, "STRUCTURE_CORE", "§e§lYapı Çekirdeği",
    Arrays.asList(
        "§7Yapıların temel taşı",
        "§7Yerleştir ve etrafına yapıyı kur",
        "§7Aktivasyon item'ı ile aktifleştir"
    ));
```

### Yapı Çekirdeği Yerleştirme

**StructureCoreListener.java (Yeni):**
```java
@EventHandler
public void onStructureCorePlace(BlockPlaceEvent event) {
    ItemStack item = event.getItemInHand();
    if (!ItemManager.isCustomItem(item, "STRUCTURE_CORE")) return;
    
    Block placed = event.getBlockPlaced();
    Location coreLoc = placed.getLocation();
    
    // Metadata ekle (inaktif çekirdek)
    placed.setMetadata("StructureCore", new FixedMetadataValue(plugin, true));
    placed.setMetadata("StructureCoreOwner", 
        new FixedMetadataValue(plugin, event.getPlayer().getUniqueId().toString()));
    
    // Inaktif çekirdekler listesine ekle
    structureCoreManager.addInactiveCore(coreLoc, event.getPlayer().getUniqueId());
    
    event.getPlayer().sendMessage("§aYapı çekirdeği yerleştirildi!");
    event.getPlayer().sendMessage("§7Etrafına yapıyı kur ve aktivasyon item'ı ile aktifleştir.");
}
```

### Yapı Çekirdeği Tespit

**StructureCoreManager.java (Yeni):**
```java
public class StructureCoreManager {
    // Inaktif çekirdekler: Location -> Owner UUID
    private final Map<Location, UUID> inactiveCores = new HashMap<>();
    
    // Aktif yapılar: Location -> Structure
    private final Map<Location, Structure> activeStructures = new HashMap<>();
    
    public void addInactiveCore(Location loc, UUID owner) {
        inactiveCores.put(loc, owner);
    }
    
    public boolean isInactiveCore(Location loc) {
        return inactiveCores.containsKey(loc);
    }
    
    public UUID getCoreOwner(Location loc) {
        return inactiveCores.get(loc);
    }
}
```

---

## 📐 TARİF SİSTEMİ

### BlockRecipe Sınıfı (Yeni)

**BlockRecipe.java (Yeni):**
```java
public class BlockRecipe {
    private Material coreMaterial; // Çekirdek bloğu
    private final List<BlockRequirement> requirements = new ArrayList<>();
    
    public BlockRecipe setCore(Material material) {
        this.coreMaterial = material;
        return this;
    }
    
    public BlockRecipe addBlock(int relX, int relY, int relZ, Material material) {
        requirements.add(new BlockRequirement(relX, relY, relZ, material));
        return this;
    }
    
    public boolean validate(Location coreLocation) {
        Block coreBlock = coreLocation.getBlock();
        if (coreBlock.getType() != coreMaterial) {
            return false;
        }
        
        for (BlockRequirement req : requirements) {
            Location checkLoc = coreLocation.clone().add(req.relX, req.relY, req.relZ);
            Block checkBlock = checkLoc.getBlock();
            if (checkBlock.getType() != req.material) {
                return false;
            }
        }
        
        return true;
    }
    
    private static class BlockRequirement {
        final int relX, relY, relZ;
        final Material material;
        
        BlockRequirement(int relX, int relY, int relZ, Material material) {
            this.relX = relX;
            this.relY = relY;
            this.relZ = relZ;
            this.material = material;
        }
    }
}
```

### Tarif Kayıt Sistemi

**StructureRecipeManager.java (Yeni):**
```java
public class StructureRecipeManager {
    // Kod içi tarifler: Structure.Type -> BlockRecipe
    private final Map<Structure.Type, BlockRecipe> codeRecipes = new HashMap<>();
    
    // Şema tarifleri: Structure.Type -> Schematic Name
    private final Map<Structure.Type, String> schematicRecipes = new HashMap<>();
    
    public void registerCodeRecipe(Structure.Type type, BlockRecipe recipe) {
        codeRecipes.put(type, recipe);
    }
    
    public void registerSchematicRecipe(Structure.Type type, String schematicName) {
        schematicRecipes.put(type, schematicName);
    }
    
    public boolean validateStructure(Location coreLocation, Structure.Type type) {
        // Önce kod içi tarif kontrolü
        if (codeRecipes.containsKey(type)) {
            return codeRecipes.get(type).validate(coreLocation);
        }
        
        // Şema tarif kontrolü
        if (schematicRecipes.containsKey(type)) {
            StructureValidator validator = new StructureValidator();
            // Async validation (callback ile)
            // Şimdilik sync döndür (ileride async yapılabilir)
            return validator.validate(coreLocation, schematicRecipes.get(type));
        }
        
        return false;
    }
}
```

---

## ⚡ AKTİVASYON SİSTEMİ

### Aktivasyon Item'ları

**Yapı Tipi → Aktivasyon Item'ı:**

| Yapı Tipi | Aktivasyon Item'ı | Açıklama |
|-----------|-------------------|----------|
| PERSONAL_MISSION_GUILD | IRON_INGOT | Demir |
| CLAN_BANK | GOLD_INGOT | Altın |
| CONTRACT_OFFICE | DIAMOND | Elmas |
| CLAN_MISSION_GUILD | EMERALD | Zümrüt |
| MARKET_PLACE | COAL | Kömür |
| RECIPE_LIBRARY | BOOK | Kitap |
| ALCHEMY_TOWER | TITANIUM_INGOT | Titanyum (özel) |
| TECTONIC_STABILIZER | BOSS_ITEM | Boss item'i (özel) |
| POISON_REACTOR | BOSS_ITEM | Boss item'i (özel) |

### Aktivasyon Listener

**StructureActivationListener.java (Güncellenecek):**
```java
@EventHandler(priority = EventPriority.HIGH)
public void onStructureActivation(PlayerInteractEvent event) {
    if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
    if (event.getHand() != EquipmentSlot.HAND) return;
    
    Player player = event.getPlayer();
    Block clicked = event.getClickedBlock();
    if (clicked == null) return;
    
    // Yapı çekirdeği kontrolü
    if (!clicked.hasMetadata("StructureCore")) return;
    
    Location coreLoc = clicked.getLocation();
    StructureCoreManager coreManager = plugin.getStructureCoreManager();
    
    // Inaktif çekirdek kontrolü
    if (!coreManager.isInactiveCore(coreLoc)) {
        // Zaten aktif bir yapı, menü aç
        Structure activeStructure = coreManager.getActiveStructure(coreLoc);
        if (activeStructure != null) {
            openStructureMenu(player, activeStructure);
        }
        return;
    }
    
    // Aktivasyon item'ı kontrolü
    ItemStack handItem = player.getInventory().getItemInMainHand();
    if (handItem == null) {
        player.sendMessage("§cAktivasyon item'ı elinde olmalı!");
        return;
    }
    
    // Hangi yapı tipi için bu item kullanılabilir?
    Structure.Type targetType = getStructureTypeForActivationItem(handItem.getType());
    if (targetType == null) {
        player.sendMessage("§cBu item ile yapı aktifleştirilemez!");
        player.sendMessage("§7Farklı bir aktivasyon item'ı deneyin.");
        return;
    }
    
    // Tarif kontrolü
    StructureRecipeManager recipeManager = plugin.getStructureRecipeManager();
    if (!recipeManager.validateStructure(coreLoc, targetType)) {
        player.sendMessage("§c§l✗ Yapı tarife uymuyor!");
        player.sendMessage("§7Lütfen yapıyı doğru şekilde kurun.");
        player.playSound(coreLoc, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        return;
    }
    
    // Aktivasyon item'ını tüket
    handItem.setAmount(handItem.getAmount() - 1);
    
    // Yapıyı aktifleştir
    activateStructure(player, coreLoc, targetType);
}

private Structure.Type getStructureTypeForActivationItem(Material item) {
    switch (item) {
        case IRON_INGOT: return Structure.Type.PERSONAL_MISSION_GUILD;
        case GOLD_INGOT: return Structure.Type.CLAN_BANK;
        case DIAMOND: return Structure.Type.CONTRACT_OFFICE;
        case EMERALD: return Structure.Type.CLAN_MISSION_GUILD;
        case COAL: return Structure.Type.MARKET_PLACE;
        case BOOK: return Structure.Type.RECIPE_LIBRARY;
        // Özel item'lar için ItemManager.isCustomItem kontrolü
        default:
            if (ItemManager.isCustomItem(new ItemStack(item), "TITANIUM_INGOT")) {
                return Structure.Type.ALCHEMY_TOWER;
            }
            // Boss item'ları için kontrol
            return null;
    }
}
```

---

## 📊 YAPI KATEGORİLERİ

### 1. Basit Yapılar (Kod İçi Tarifler)

**Özellikler:**
- Küçük yapılar (1-5 blok)
- Kod içinde tariflenebilir
- Basit aktivasyon item'ları (Demir, Altın, Elmas, vb.)

**Yapılar:**
- ✅ **PERSONAL_MISSION_GUILD** (Görev Loncası)
- ✅ **CLAN_BANK** (Klan Bankası)
- ✅ **CONTRACT_OFFICE** (Kontrat Bürosu)
- ✅ **CLAN_MISSION_GUILD** (Klan Görev Loncası)
- ✅ **MARKET_PLACE** (Market)
- ✅ **RECIPE_LIBRARY** (Tarif Kütüphanesi)

### 2. Orta Yapılar (Kod İçi Tarifler - Gelecekte)

**Özellikler:**
- Orta boyutlu yapılar (5-20 blok)
- Kod içinde tariflenebilir
- Orta seviye aktivasyon item'ları

**Yapılar:**
- TRAINING_ARENA (Eğitim Alanı)
- CARAVAN_STATION (Kervan İstasyonu)
- XP_BANK (Tecrübe Bankası)

### 3. Karmaşık Yapılar (Şema Tarifleri)

**Özellikler:**
- Büyük yapılar (20+ blok)
- WorldEdit şemaları ile kontrol edilir
- Özel aktivasyon item'ları (Boss itemleri, Titanyum, vb.)

**Yapılar:**
- ✅ **ALCHEMY_TOWER** (Simya Kulesi)
- ✅ **TECTONIC_STABILIZER** (Tektonik Sabitleyici)
- ✅ **POISON_REACTOR** (Zehir Reaktörü)
- ✅ **AUTO_TURRET** (Otomatik Taret)
- ✅ **GLOBAL_MARKET_GATE** (Global Pazar)
- ✅ **SIEGE_FACTORY** (Kuşatma Fabrikası)
- ✅ **WALL_GENERATOR** (Sur Jeneratörü)
- ✅ **GRAVITY_WELL** (Yerçekimi Kuyusu)
- ✅ **LAVA_TRENCHER** (Lav Hendekçisi)
- ✅ **WATCHTOWER** (Gözetleme Kulesi)
- ✅ **DRONE_STATION** (Drone İstasyonu)
- ✅ **AUTO_DRILL** (Otomatik Madenci)
- ✅ **MAG_RAIL** (Manyetik Ray)
- ✅ **TELEPORTER** (Işınlanma Platformu)
- ✅ **FOOD_SILO** (Buzdolabı)
- ✅ **OIL_REFINERY** (Petrol Rafinerisi)
- ✅ **HEALING_BEACON** (Şifa Kulesi)
- ✅ **WEATHER_MACHINE** (Hava Kontrolcüsü)
- ✅ **CROP_ACCELERATOR** (Tarım Hızlandırıcı)
- ✅ **MOB_GRINDER** (Mob Öğütücü)
- ✅ **INVISIBILITY_CLOAK** (Görünmezlik Perdesi)
- ✅ **ARMORY** (Cephanelik)
- ✅ **LIBRARY** (Kütüphane)

---

## 💻 KOD YAPISI

### Yeni Sınıflar

1. **StructureCoreManager.java**
   - Inaktif çekirdekleri yönetir
   - Aktif yapıları yönetir
   - Çekirdek tespit ve kontrol

2. **BlockRecipe.java**
   - Kod içi tarif tanımlama
   - Relative pozisyon kontrolü
   - Tarif doğrulama

3. **StructureRecipeManager.java**
   - Tüm tarifleri yönetir (kod + şema)
   - Tarif kayıt sistemi
   - Tarif doğrulama

4. **StructureCoreListener.java**
   - Yapı çekirdeği yerleştirme
   - Çekirdek tespit
   - Çekirdek etkileşim

### Güncellenecek Sınıflar

1. **StructureActivationListener.java**
   - Yeni aktivasyon sistemi
   - Item bazlı aktivasyon
   - Tarif kontrolü

2. **ItemManager.java**
   - STRUCTURE_CORE item'ı eklenecek

3. **Main.java**
   - Yeni manager'lar initialize edilecek

---

## 📝 ÖRNEKLER

### Örnek 1: Görev Loncası (PERSONAL_MISSION_GUILD)

**Tarif:**
```
Merkez: Yapı Çekirdeği (END_CRYSTAL)
Altında: Kırıktaş (COBBLESTONE) - relY: -1
Üstünde: Kürsü (LECTERN) - relY: +1
```

**Kod:**
```java
// StructureRecipeManager.registerCodeRecipe() içinde:
BlockRecipe missionGuildRecipe = new BlockRecipe()
    .setCore(Material.END_CRYSTAL)
    .addBlock(0, -1, 0, Material.COBBLESTONE) // Altında kırıktaş
    .addBlock(0, 1, 0, Material.LECTERN); // Üstünde kürsü

recipeManager.registerCodeRecipe(Structure.Type.PERSONAL_MISSION_GUILD, missionGuildRecipe);
```

**Aktivasyon:**
- Item: `IRON_INGOT` (Demir)
- İşlem: Demir ile çekirdeğe sağ tık

### Örnek 2: Klan Bankası (CLAN_BANK)

**Tarif:**
```
Merkez: Yapı Çekirdeği (END_CRYSTAL)
Altında: Altın Blok (GOLD_BLOCK) - relY: -1
Üstünde: Sandık (CHEST) - relY: +1
```

**Kod:**
```java
BlockRecipe bankRecipe = new BlockRecipe()
    .setCore(Material.END_CRYSTAL)
    .addBlock(0, -1, 0, Material.GOLD_BLOCK) // Altında altın blok
    .addBlock(0, 1, 0, Material.CHEST); // Üstünde sandık

recipeManager.registerCodeRecipe(Structure.Type.CLAN_BANK, bankRecipe);
```

**Aktivasyon:**
- Item: `GOLD_INGOT` (Altın)
- İşlem: Altın ile çekirdeğe sağ tık

### Örnek 3: Kontrat Bürosu (CONTRACT_OFFICE)

**Tarif:**
```
Merkez: Yapı Çekirdeği (END_CRYSTAL)
Altında: Taş (STONE) - relY: -1
Üstünde: Masa (CRAFTING_TABLE) - relY: +1
```

**Kod:**
```java
BlockRecipe contractOfficeRecipe = new BlockRecipe()
    .setCore(Material.END_CRYSTAL)
    .addBlock(0, -1, 0, Material.STONE) // Altında taş
    .addBlock(0, 1, 0, Material.CRAFTING_TABLE); // Üstünde masa

recipeManager.registerCodeRecipe(Structure.Type.CONTRACT_OFFICE, contractOfficeRecipe);
```

**Aktivasyon:**
- Item: `DIAMOND` (Elmas)
- İşlem: Elmas ile çekirdeğe sağ tık

### Örnek 4: Simya Kulesi (ALCHEMY_TOWER) - Şema Tarifi

**Tarif:**
- Şema dosyası: `alchemy_tower.schem`
- Mevcut `StructureValidator` sistemi kullanılacak

**Kod:**
```java
recipeManager.registerSchematicRecipe(Structure.Type.ALCHEMY_TOWER, "alchemy_tower");
```

**Aktivasyon:**
- Item: `TITANIUM_INGOT` (Titanyum - özel item)
- İşlem: Titanyum ile çekirdeğe sağ tık
- Tarif kontrolü: Şema dosyası ile

---

## 🔄 GEÇİŞ PLANI

### Faz 1: Temel Sistem (Şimdi)

1. ✅ **Yapı Çekirdeği Item'ı** oluştur
2. ✅ **StructureCoreManager** sınıfını oluştur
3. ✅ **BlockRecipe** sınıfını oluştur
4. ✅ **StructureRecipeManager** sınıfını oluştur
5. ✅ **StructureCoreListener** sınıfını oluştur
6. ✅ **Basit yapılar için tarifler** ekle (Görev Loncası, Banka, Kontrat Bürosu)
7. ✅ **Aktivasyon sistemi** güncelle

### Faz 2: Diğer Basit Yapılar (Gelecek)

1. Klan Görev Loncası
2. Market
3. Tarif Kütüphanesi
4. Eğitim Alanı
5. Kervan İstasyonu

### Faz 3: Karmaşık Yapılar (Gelecek)

1. Şema tarifleri için yeni sistem entegrasyonu
2. Yüksek seviye yapılar için boss item aktivasyonu
3. Yapı seviye sistemi (ileride)

---

## 🎮 KULLANIM AKIŞI

### Oyuncu Perspektifi

1. **Yapı Çekirdeği Al:**
   - `/stratocraft give structure_core` veya craft

2. **Yapıyı Kur:**
   - Yapı çekirdeğini yerleştir
   - Etrafına tarife göre blokları yerleştir
   - Örnek: Görev Loncası için altına kırıktaş, üstüne kürsü

3. **Yapıyı Aktifleştir:**
   - Aktivasyon item'ını eline al (örnek: Demir)
   - Yapı çekirdeğine sağ tık yap
   - Sistem tarifi kontrol eder
   - Doğruysa → Yapı aktif olur, efektler gösterilir

4. **Yapıyı Kullan:**
   - Aktif yapıya sağ tık → Menü açılır

---

## 📋 YAPILACAKLAR LİSTESİ

### Şimdi Yapılacaklar

- [ ] **ItemManager.java**: STRUCTURE_CORE item'ı ekle
- [ ] **StructureCoreManager.java**: Yeni sınıf oluştur
- [ ] **BlockRecipe.java**: Yeni sınıf oluştur
- [ ] **StructureRecipeManager.java**: Yeni sınıf oluştur
- [ ] **StructureCoreListener.java**: Yeni listener oluştur
- [ ] **StructureActivationListener.java**: Güncelle (yeni aktivasyon sistemi)
- [ ] **Main.java**: Yeni manager'ları initialize et
- [ ] **Basit yapı tarifleri**: Görev Loncası, Banka, Kontrat Bürosu

### Gelecekte Yapılacaklar

- [ ] Diğer basit yapılar için tarifler
- [ ] Karmaşık yapılar için şema entegrasyonu
- [ ] Yapı seviye sistemi
- [ ] Yapı yükseltme sistemi

---

## 🔍 TEKNİK DETAYLAR

### Yapı Çekirdeği Metadata

```java
// Yerleştirme
block.setMetadata("StructureCore", new FixedMetadataValue(plugin, true));
block.setMetadata("StructureCoreOwner", 
    new FixedMetadataValue(plugin, player.getUniqueId().toString()));

// Kontrol
if (block.hasMetadata("StructureCore")) {
    // Yapı çekirdeği
}
```

### Tarif Doğrulama

```java
// Kod içi tarif
BlockRecipe recipe = recipeManager.getCodeRecipe(type);
if (recipe != null) {
    boolean valid = recipe.validate(coreLocation);
}

// Şema tarif
String schematicName = recipeManager.getSchematicRecipe(type);
if (schematicName != null) {
    StructureValidator validator = new StructureValidator();
    validator.validateAsync(coreLocation, schematicName, (isValid) -> {
        // Callback
    });
}
```

### Aktivasyon Item Kontrolü

```java
private Structure.Type getStructureTypeForActivationItem(Material item) {
    // Normal item'lar
    switch (item) {
        case IRON_INGOT: return Structure.Type.PERSONAL_MISSION_GUILD;
        case GOLD_INGOT: return Structure.Type.CLAN_BANK;
        // ...
    }
    
    // Özel item'lar
    ItemStack itemStack = new ItemStack(item);
    if (ItemManager.isCustomItem(itemStack, "TITANIUM_INGOT")) {
        return Structure.Type.ALCHEMY_TOWER;
    }
    
    return null;
}
```

---

## ✅ SONUÇ

Bu yeni sistem:
- ✅ **Daha esnek**: Kod içi ve şema tarifleri birlikte
- ✅ **Daha basit**: Basit yapılar için kolay tarif
- ✅ **Daha özelleştirilebilir**: Her yapı için farklı aktivasyon item'ı
- ✅ **Tuzak sistemi ile tutarlı**: Aynı mantık, farklı kullanım

**Durum:** ✅ PLAN HAZIR - İMPLEMENTASYONA BAŞLANABİLİR

---

**Son Güncelleme:** 2024
**Versiyon:** 1.0-PLAN
**Durum:** ✅ PLAN TAMAMLANDI

