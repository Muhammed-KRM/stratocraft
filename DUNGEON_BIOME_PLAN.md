# 🏰 Zindan ve Biyom Sistemi - Hybrid Yaklaşım Planı

## 📋 GENEL BAKIŞ

Bu plan, mevcut kod tabanınızı kullanarak zindan ve biyom sistemini eklemek için hibrit bir yaklaşım sunar:
- ✅ Kendi kodumuzla yönetim (tam kontrol)
- ✅ WorldEdit API (mevcut)
- ✅ FastAsyncWorldEdit (FAWE) - performans için
- ✅ Minecraft Structure API - vanilla yapılar için
- ✅ Hazır şemalar (telif sorunu olmayan)

---

## 🎯 HEDEFLER

### Zindanlar:
- Difficulty seviyesine göre zindanlar (1-5)
- Her seviye için 3-5 farklı zindan tipi
- Otomatik spawn (chunk generation)
- Özel moblar ve bosslar
- Özel loot sistemleri

### Biyomlar:
- Difficulty seviyesine göre özel biyomlar
- Custom biome generation
- Biome-specific structures
- Difficulty-based biome distribution

---

## 📦 KURULUM ADIMLARI

### 1. Plugin Bağımlılıkları

#### A. FastAsyncWorldEdit (FAWE) - ÖNERİLEN
**Neden?**
- WorldEdit'in async versiyonu
- Büyük yapılar için çok daha hızlı
- Sunucu performansını etkilemez

**Kurulum:**
1. [FAWE Spigot Builds](https://ci.athion.net/job/FastAsyncWorldEdit/) adresinden indir
2. `plugins/` klasörüne koy
3. Sunucuyu başlat (otomatik yüklenir)

**pom.xml'e ekle:**
```xml
<dependency>
    <groupId>com.fastasyncworldedit</groupId>
    <artifactId>FastAsyncWorldEdit-Core</artifactId>
    <version>2.8.3</version>
    <scope>provided</scope>
</dependency>
```

#### B. Minecraft Structure API (Vanilla)
**Neden?**
- Vanilla yapılar için
- .nbt dosyaları ile çalışır
- Ek bağımlılık yok (Bukkit API içinde)

**Kullanım:**
- Vanilla structure dosyaları kullanılabilir
- WorldEdit ile .nbt'den .schem'e çevrilebilir

---

## 📁 DOSYA YAPISI

```
stratocraft/
├── src/main/java/me/mami/stratocraft/
│   ├── manager/
│   │   ├── DungeonManager.java          [YENİ]
│   │   ├── BiomeManager.java            [YENİ]
│   │   └── StructureBuilder.java        [GÜNCELLENECEK]
│   └── listener/
│       └── WorldGenerationListener.java  [GÜNCELLENECEK]
│
└── src/main/resources/
    └── schematics/
        ├── dungeons/
        │   ├── level1/
        │   │   ├── goblin_cave.schem
        │   │   ├── spider_nest.schem
        │   │   └── bandit_hideout.schem
        │   ├── level2/
        │   │   ├── orc_fortress.schem
        │   │   ├── skeleton_crypt.schem
        │   │   └── dark_temple.schem
        │   ├── level3/
        │   │   ├── dragon_lair.schem
        │   │   ├── ancient_ruins.schem
        │   │   └── demon_castle.schem
        │   ├── level4/
        │   │   ├── titan_tomb.schem
        │   │   ├── void_prison.schem
        │   │   └── hell_fortress.schem
        │   └── level5/
        │       ├── cosmic_temple.schem
        │       ├── god_realm.schem
        │       └── chaos_dimension.schem
        └── biomes/
            ├── structures/
            │   ├── level1_tree.schem
            │   ├── level2_ruin.schem
            │   └── level3_shrine.schem
            └── custom/
                └── (biome-specific structures)
```

---

## 🗂️ HAZIR ŞEMA KAYNAKLARI

### 1. PlanetMinecraft (ÖNERİLEN)
**URL:** https://www.planetminecraft.com/resources/schematics/
**Lisans:** Çoğu Creative Commons veya kullanım izni var
**Nasıl İndirilir:**
1. PlanetMinecraft'a git
2. "Schematics" bölümüne git
3. "Dungeon" veya "Structure" ara
4. Filtrele: "Free" ve "Download"
5. İndirilen `.schematic` dosyasını `.schem` olarak kaydet

**Örnek Arama Terimleri:**
- "dungeon schematic"
- "cave system"
- "underground structure"
- "ruins schematic"
- "temple schematic"

### 2. Minecraft Structure Database
**URL:** https://www.minecraft-schematics.com/
**Not:** Bazıları ücretli, ücretsiz olanları filtrele

### 3. CurseForge
**URL:** https://www.curseforge.com/minecraft/texture-packs
**Not:** Mod paketlerinden structure dosyaları çıkarılabilir

### 4. Kendi Yapılarınızı Oluşturma
**Araçlar:**
- WorldEdit (in-game)
- WorldPainter (dünya editörü)
- MCEdit (eski ama hala kullanılabilir)

---

## 💻 KOD MİMARİSİ

### 1. DungeonManager.java

```java
public class DungeonManager {
    private final Main plugin;
    private final DifficultyManager difficultyManager;
    private final Map<Integer, List<String>> dungeonSchematics; // Seviye -> Şema listesi
    
    // Zindan spawn kontrolü
    public boolean shouldSpawnDungeon(Location loc, int difficultyLevel);
    
    // Zindan spawn et
    public void spawnDungeon(Location loc, int difficultyLevel);
    
    // Zindan tipi seç (rastgele)
    public String selectDungeonType(int difficultyLevel);
    
    // Zindan içi mob spawn
    public void spawnDungeonMobs(Location dungeonCenter, int difficultyLevel);
    
    // Zindan loot yerleştir
    public void placeDungeonLoot(Location loc, int difficultyLevel);
}
```

### 2. BiomeManager.java

```java
public class BiomeManager {
    private final Main plugin;
    private final DifficultyManager difficultyManager;
    
    // Biome değiştir (chunk generation'da)
    public Biome getBiomeForDifficulty(Location loc, int difficultyLevel);
    
    // Custom biome structure spawn
    public void spawnBiomeStructure(Location loc, Biome biome, int difficultyLevel);
    
    // Biome-specific mob spawn
    public void spawnBiomeMobs(Location loc, Biome biome);
}
```

### 3. StructureBuilder.java (Güncelleme)

```java
// FAWE desteği ekle
public static boolean pasteSchematicFAWE(Location location, String schematicName);
public static boolean pasteSchematicVanilla(Location location, String schematicName);
```

---

## 🔄 ENTEGRASYON PLANI

### Adım 1: DungeonManager Oluştur
1. `DungeonManager.java` oluştur
2. `DifficultyManager` ile entegre et
3. Şema dosyalarını yükle
4. Spawn logic ekle

### Adım 2: WorldGenerationListener Güncelle
1. `onChunkLoad` metoduna zindan spawn ekle
2. Difficulty level kontrolü
3. Rastgele spawn şansı

### Adım 3: BiomeManager Oluştur
1. `BiomeManager.java` oluştur
2. Custom biome generation
3. Biome-specific structures

### Adım 4: Config Dosyası
```yaml
dungeons:
  enabled: true
  spawn-chance:
    level1: 0.05  # %5 şans
    level2: 0.08
    level3: 0.10
    level4: 0.12
    level5: 0.15
  types:
    level1:
      - goblin_cave
      - spider_nest
      - bandit_hideout
    level2:
      - orc_fortress
      - skeleton_crypt
      - dark_temple
    # ... diğer seviyeler

biomes:
  enabled: true
  custom-biomes:
    level1:
      - "FOREST"
      - "PLAINS"
    level2:
      - "TAIGA"
      - "SWAMP"
    # ... diğer seviyeler
```

---

## 📥 HAZIR ŞEMA İNDİRME REHBERİ

### PlanetMinecraft'dan İndirme:

1. **Siteye Git:**
   - https://www.planetminecraft.com/resources/schematics/

2. **Arama Yap:**
   - "dungeon" veya "cave" veya "structure" ara
   - Filtrele: "Free" ve "Downloadable"

3. **İndir:**
   - Projeye tıkla
   - "Download" butonuna tıkla
   - `.schematic` veya `.schem` dosyasını indir

4. **Dosyayı Yerleştir:**
   ```
   plugins/Stratocraft/schematics/dungeons/level1/goblin_cave.schem
   ```

5. **Format Dönüştürme (Gerekirse):**
   - `.schematic` → `.schem`: WorldEdit komutu ile
   - `/schematic load <name>`
   - `/schematic save <name>`

### Telif Kontrolü:

✅ **Güvenli Kaynaklar:**
- Creative Commons lisanslı
- "Free to use" belirtilen
- "No attribution required" olanlar

❌ **Dikkat Edilmesi Gerekenler:**
- "All rights reserved" olanlar
- Ücretli şemalar
- Belirsiz lisans

---

## 🚀 UYGULAMA SIRASI

### Faz 1: Temel Altyapı (1-2 gün)
1. ✅ DungeonManager oluştur
2. ✅ Config dosyası hazırla
3. ✅ StructureBuilder'ı FAWE desteği ile güncelle

### Faz 2: Zindan Sistemi (2-3 gün)
1. ✅ WorldGenerationListener'a entegre et
2. ✅ Şema dosyalarını yükle
3. ✅ Spawn logic test et
4. ✅ Mob spawn entegrasyonu

### Faz 3: Biyom Sistemi (2-3 gün)
1. ✅ BiomeManager oluştur
2. ✅ Custom biome generation
3. ✅ Biome structures

### Faz 4: Test ve Optimizasyon (1-2 gün)
1. ✅ Performans testleri
2. ✅ Spawn rate ayarları
3. ✅ Bug fixler

---

## 📊 BEKLENEN SONUÇLAR

### Zindanlar:
- Her difficulty seviyesinde 3-5 zindan tipi
- Otomatik spawn (chunk generation)
- Özel moblar ve bosslar
- Difficulty-based loot

### Biyomlar:
- Custom biome generation
- Biome-specific structures
- Difficulty-based distribution

---

## ⚠️ ÖNEMLİ NOTLAR

1. **Performans:**
   - FAWE kullan (async yapı yükleme)
   - Chunk başına maksimum 1 zindan
   - Biome değişiklikleri sınırlı tut

2. **Dosya Boyutu:**
   - Şemalar çok büyük olmamalı (< 1MB)
   - Optimize edilmiş şemalar kullan

3. **Telif:**
   - Sadece "free to use" şemalar kullan
   - Lisans bilgilerini kontrol et

---

## 🎮 TEST KOMUTLARI

```java
// Admin komutları eklenecek:
/stratocraft dungeon spawn <level> [type]
/stratocraft dungeon list
/stratocraft biome set <biome>
/stratocraft biome list
```

---

## 📝 SONRAKI ADIMLAR

1. Bu planı onayla
2. DungeonManager kodunu yazmaya başla
3. Hazır şemaları indir ve yerleştir
4. Test et ve optimize et

