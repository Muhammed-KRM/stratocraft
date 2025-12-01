# 🎨 Custom Entity Model ve Hitbox Rehberi

## 📋 İÇİNDEKİLER

1. [Yöntem Seçimi](#yöntem-seçimi)
2. [ModelEngine Kullanımı (ÖNERİLEN)](#modelengine-kullanımı-önerilen)
3. [Blockbench ile Model Oluşturma](#blockbench-ile-model-oluşturma)
4. [Hitbox Ayarları](#hitbox-ayarları)
5. [Çoklu Hitbox (Uzun Canavarlar)](#çoklu-hitbox-uzun-canavarlar)
6. [Hazır Model Kaynakları](#hazır-model-kaynakları)
7. [Kod Entegrasyonu](#kod-entegrasyonu)

---

## 🎯 YÖNTEM SEÇİMİ

### Seçenek 1: ModelEngine (ÖNERİLEN) ⭐

**Avantajlar:**
- ✅ 3D modelleri doğrudan kullanır
- ✅ Otomatik hitbox hesaplama
- ✅ Animasyon desteği
- ✅ MythicMobs ile tam entegrasyon
- ✅ Çoklu hitbox desteği (uzun canavarlar için)

**Dezavantajlar:**
- ❌ Ücretli plugin (ama en iyi çözüm)
- ❌ Biraz öğrenme eğrisi var

### Seçenek 2: ItemsAdder

**Avantajlar:**
- ✅ Ücretsiz (açık kaynak)
- ✅ Custom item ve entity desteği
- ✅ Resource pack entegrasyonu

**Dezavantajlar:**
- ❌ ModelEngine kadar gelişmiş değil
- ❌ Hitbox ayarları daha sınırlı

### Seçenek 3: ArmorStand Manipülasyonu (Basit)

**Avantajlar:**
- ✅ Ücretsiz (kod ile yapılır)
- ✅ Hızlı implementasyon

**Dezavantajlar:**
- ❌ Sınırlı görünüm (sadece armor stand)
- ❌ Hitbox ayarları zor
- ❌ Çoklu hitbox çok karmaşık

**ÖNERİ:** ModelEngine kullanın! En profesyonel ve esnek çözüm.

---

## 🚀 MODELENGINE KULLANIMI (ÖNERİLEN)

### ADIM 1: ModelEngine Kurulumu

1. **ModelEngine İndir:**
   - https://www.spigotmc.org/resources/modelengine.107955/
   - Veya: https://www.mc-market.org/resources/22155/
   - Minecraft 1.20.4 için uygun versiyonu seçin

2. **Test Server'a Kur:**
   ```
   C:\mc\test-server\plugins\ModelEngine.jar
   ```

3. **Bağımlılıklar:**
   - **Citizens** (zorunlu): https://www.spigotmc.org/resources/citizens.13811/
   - **MythicMobs** (önerilir): https://www.spigotmc.org/resources/mythicmobs.5702/

4. **Sunucuyu Başlat:**
   - Konsolda "ModelEngine enabled" mesajını görmelisiniz

### ADIM 2: Model Dosyası Yapısı

ModelEngine modelleri `.bbmodel` (Blockbench) veya `.geo` (Geckolib) formatında olmalı.

**Klasör Yapısı:**
```
C:\mc\test-server\plugins\ModelEngine\
└── models\
    ├── ork.bbmodel
    ├── hell_dragon.bbmodel
    ├── goblin.bbmodel
    └── textures\
        ├── ork.png
        ├── hell_dragon.png
        └── goblin.png
```

### ADIM 3: ModelEngine Komutları

```bash
# Model yükle
/modelengine model load <model_name>

# Model'i entity'ye bağla
/modelengine model apply <entity> <model_name>

# Hitbox ayarla
/modelengine hitbox set <entity> <width> <height>

# Model listesi
/modelengine model list
```

---

## 🎨 BLOCKBENCH İLE MODEL OLUŞTURMA

### ADIM 1: Blockbench Kurulumu

1. **Blockbench İndir:**
   - https://www.blockbench.net/
   - Ücretsiz ve açık kaynak

2. **Kurulum:**
   - Windows: `.exe` dosyasını çalıştır
   - Otomatik kurulum

### ADIM 2: Yeni Model Oluşturma

1. **Blockbench'i Aç:**
   - "New Model" → "Java Block/Item" seçin
   - Veya "Bedrock Entity" (ModelEngine için)

2. **Model Oluştur:**
   - Sol panelden "Add Cube" ile parça ekleyin
   - Her parça için:
     - **Position:** X, Y, Z koordinatları
     - **Size:** Genişlik, Yükseklik, Derinlik
     - **Rotation:** Döndürme açıları
     - **Texture:** Doku ataması

3. **Örnek: Ork Modeli (1.5x Büyük)**
   ```
   Ana Gövde:
   - Size: 0.6 x 1.8 x 0.6 (normal zombie)
   - Scale: 1.5x → 0.9 x 2.7 x 0.9
   
   Kafa:
   - Size: 0.6 x 0.6 x 0.6
   - Scale: 1.5x → 0.9 x 0.9 x 0.9
   
   Kollar:
   - Size: 0.4 x 1.2 x 0.4
   - Scale: 1.5x → 0.6 x 1.8 x 0.6
   ```

4. **Texture (Doku) Oluştur:**
   - "Textures" sekmesine gidin
   - "New Texture" ile yeni doku oluşturun
   - 64x64 veya 128x128 piksel önerilir
   - Her model parçasına doku atayın

5. **Export:**
   - "File" → "Export" → "Bedrock Entity" (ModelEngine için)
   - Veya "Java Block/Item" (ItemsAdder için)
   - Dosyayı kaydedin: `ork.bbmodel`

### ADIM 3: Uzun Canavarlar (Cehennem Ejderi)

**Çoklu Parça Yaklaşımı:**

1. **Model Yapısı:**
   ```
   Kafa (1 parça)
   ├── Boyun (1 parça)
   ├── Gövde 1 (1 parça)
   ├── Gövde 2 (1 parça)
   ├── Gövde 3 (1 parça)
   ├── Kuyruk 1 (1 parça)
   ├── Kuyruk 2 (1 parça)
   └── Kuyruk 3 (1 parça)
   ```

2. **Blockbench'te:**
   - Her parçayı ayrı "Cube" olarak ekleyin
   - Parçaları birbirine bağlayın (parent-child ilişkisi)
   - Animasyon ekleyin (isteğe bağlı)

3. **Hitbox Ayarları:**
   - ModelEngine otomatik hesaplar
   - Veya manuel ayarlayın (aşağıda)

---

## 📦 HİTBOX AYARLARI

### Hitbox Nedir?

Hitbox, oyuncunun canavara vurabileceği alanı belirler. Minecraft'ta genelde tek bir kutu (AxisAlignedBB) kullanılır.

### Basit Hitbox (Tek Parça)

**Minecraft API ile:**
```java
// Entity'nin hitbox'unu değiştir
Entity entity = ...; // Ork, Ejderha vb.

// Reflection ile hitbox değiştirme (1.20.4)
try {
    Object nmsEntity = entity.getClass().getMethod("getHandle").invoke(entity);
    Field boundingBoxField = nmsEntity.getClass().getField("boundingBox");
    
    // Yeni boyutlar (1.5x büyük ork için)
    double width = 0.9;  // Normal: 0.6
    double height = 2.7; // Normal: 1.8
    
    // BoundingBox oluştur
    // (NMS kodu, versiyona göre değişir)
} catch (Exception e) {
    e.printStackTrace();
}
```

**ModelEngine ile:**
```yaml
# ModelEngine config
models:
  ork:
    hitbox:
      width: 0.9
      height: 2.7
      depth: 0.9
```

### Çoklu Hitbox (Uzun Canavarlar)

**Yöntem 1: ModelEngine (ÖNERİLEN)**

ModelEngine, uzun modeller için otomatik çoklu hitbox hesaplar:

```yaml
# ModelEngine config
models:
  hell_dragon:
    hitbox:
      # Ana hitbox
      width: 2.0
      height: 1.5
      depth: 2.0
      
      # Ek hitbox'lar (uzunluk için)
      segments:
        - offset: {x: 0, y: 0, z: 0}
          size: {width: 2.0, height: 1.5, depth: 2.0}
        - offset: {x: 0, y: 0, z: 2.0}
          size: {width: 1.8, height: 1.3, depth: 2.0}
        - offset: {x: 0, y: 0, z: 4.0}
          size: {width: 1.5, height: 1.0, depth: 1.8}
        # ... daha fazla segment
```

**Yöntem 2: ArmorStand Zinciri (Kod ile)**

Uzun canavarlar için birden fazla armor stand kullanın:

```java
public void spawnHellDragon(Location loc) {
    // Ana entity (Phantom)
    Phantom dragon = (Phantom) loc.getWorld().spawnEntity(loc, EntityType.PHANTOM);
    dragon.setCustomName("§4Cehennem Ejderi");
    dragon.setSize(25);
    
    // Ek hitbox'lar için armor stand'lar (görünmez)
    for (int i = 1; i <= 5; i++) {
        Location segmentLoc = loc.clone().add(0, 0, i * 2);
        ArmorStand segment = (ArmorStand) segmentLoc.getWorld()
            .spawnEntity(segmentLoc, EntityType.ARMOR_STAND);
        
        segment.setVisible(false);
        segment.setGravity(false);
        segment.setMarker(true); // Hitbox yok ama collision var
        
        // Ana entity'ye bağla
        segment.setLeashHolder(dragon);
    }
}
```

**Yöntem 3: Custom Entity (Gelişmiş)**

Kendi entity sınıfınızı oluşturun:

```java
public class HellDragonEntity extends Phantom {
    private List<BoundingBox> hitboxSegments = new ArrayList<>();
    
    @Override
    public AxisAlignedBB getBoundingBox() {
        // Çoklu hitbox'ları birleştir
        AxisAlignedBB mainBox = super.getBoundingBox();
        for (BoundingBox segment : hitboxSegments) {
            mainBox = mainBox.union(segment.toAABB());
        }
        return mainBox;
    }
}
```

---

## 🌐 HAZIR MODEL KAYNAKLARI

### 1. PlanetMinecraft (ÖNERİLEN)

**URL:** https://www.planetminecraft.com/resources/models/

**Arama Terimleri:**
- "dungeon mob"
- "fantasy creature"
- "custom entity"
- "blockbench model"

**Filtreler:**
- ✅ "Free" işaretle
- ✅ "Downloadable" işaretle
- ✅ "Creative Commons" lisanslı olanları seç

**Örnek Arama:**
- "ork blockbench model"
- "dragon blockbench model"
- "goblin minecraft model"

### 2. Blockbench Community

**URL:** https://www.blockbench.net/community

**Özellikler:**
- Blockbench formatında modeller
- Doğrudan kullanılabilir
- Topluluk tarafından paylaşılan

### 3. CurseForge

**URL:** https://www.curseforge.com/minecraft/texture-packs

**Arama:**
- "custom mob"
- "entity model"
- "blockbench"

### 4. GitHub Repositories

**Arama:**
- "minecraft custom entity model"
- "blockbench model pack"
- "minecraft mob models"

**Örnek Repo'lar:**
- https://github.com/search?q=minecraft+custom+entity+model

### 5. ModelEngine Marketplace

**URL:** https://www.mc-market.org/resources/categories/modelengine.60/

**Özellikler:**
- ModelEngine için hazır modeller
- Genelde ücretli ama kaliteli
- Doğrudan kullanılabilir

### Telif Hakkı Kontrolü

**Güvenli Lisanslar:**
- ✅ Creative Commons (CC0, CC BY, CC BY-SA)
- ✅ "Free to use"
- ✅ "No attribution required"
- ✅ "Public domain"

**Dikkat:**
- ❌ "All rights reserved"
- ❌ Ücretli modeller (izin alın)
- ❌ Belirsiz lisans

---

## 💻 KOD ENTEGRASYONU

### ModelEngine Entegrasyonu

**MobManager.java'ya Ekle:**

```java
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;

public void spawnOrkWithModel(Location loc) {
    if (loc == null || loc.getWorld() == null) return;
    
    // Normal entity spawn
    Zombie ork = (Zombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
    ork.setCustomName("§cOrk");
    
    // ModelEngine model uygula
    try {
        ActiveModel model = ModelEngineAPI.getModel("ork");
        if (model != null) {
            ModelEngineAPI.getModeledEntity(ork).addModel(model, true);
        }
    } catch (Exception e) {
        plugin.getLogger().warning("ModelEngine model yüklenemedi: ork");
    }
    
    // Hitbox ayarla (1.5x büyük)
    // ModelEngine otomatik yapar, ama manuel de ayarlanabilir
    if (ork.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
        ork.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(80.0);
    }
    ork.setHealth(80.0);
}
```

### ItemsAdder Entegrasyonu

**ItemsAdder config:**

```yaml
# items/entities/ork.yml
items:
  ork_spawn_egg:
    display_name: "Ork Spawn Egg"
    resource:
      material: ZOMBIE_SPAWN_EGG
      generate: true
      textures:
        - item/ork_spawn_egg.png
    behaviours:
      spawn_entity:
        entity: ork

entities:
  ork:
    display_name: "Ork"
    type: ZOMBIE
    model:
      path: entity/ork.geo.json
      texture: entity/ork.png
    hitbox:
      width: 0.9
      height: 2.7
      depth: 0.9
```

---

## 📝 ÖRNEK: ORK MODELİ (1.5X BÜYÜK)

### Blockbench Adımları

1. **Yeni Model:**
   - Blockbench → "New Model" → "Bedrock Entity"

2. **Parçalar:**
   ```
   Kafa:
   - Position: 0, 1.8, 0
   - Size: 0.9 x 0.9 x 0.9
   
   Gövde:
   - Position: 0, 0.9, 0
   - Size: 0.9 x 1.8 x 0.9
   
   Sol Kol:
   - Position: -0.6, 1.2, 0
   - Size: 0.6 x 1.8 x 0.6
   
   Sağ Kol:
   - Position: 0.6, 1.2, 0
   - Size: 0.6 x 1.8 x 0.6
   
   Sol Bacak:
   - Position: -0.3, 0, 0
   - Size: 0.6 x 0.9 x 0.6
   
   Sağ Bacak:
   - Position: 0.3, 0, 0
   - Size: 0.6 x 0.9 x 0.6
   ```

3. **Texture:**
   - 64x64 veya 128x128 piksel
   - Ork görünümü (yeşil deri, zırh parçaları)

4. **Export:**
   - "File" → "Export" → "Bedrock Entity"
   - `ork.bbmodel` olarak kaydet

5. **ModelEngine'e Yükle:**
   ```
   C:\mc\test-server\plugins\ModelEngine\models\ork.bbmodel
   ```

6. **Texture Yükle:**
   ```
   C:\mc\test-server\plugins\ModelEngine\textures\ork.png
   ```

---

## 🎯 ÖRNEK: CEHENNEM EJDERİ (UZUN)

### Blockbench Adımları

1. **Yeni Model:**
   - Blockbench → "New Model" → "Bedrock Entity"

2. **Parçalar (Uzun Yapı):**
   ```
   Kafa:
   - Position: 0, 1.5, 0
   - Size: 1.5 x 1.5 x 2.0
   
   Boyun:
   - Position: 0, 0.5, -1.0
   - Size: 1.2 x 1.2 x 2.0
   
   Gövde 1:
   - Position: 0, 0, -3.0
   - Size: 2.0 x 1.5 x 2.0
   
   Gövde 2:
   - Position: 0, 0, -5.0
   - Size: 1.8 x 1.3 x 2.0
   
   Gövde 3:
   - Position: 0, 0, -7.0
   - Size: 1.5 x 1.0 x 1.8
   
   Kuyruk 1:
   - Position: 0, 0, -9.0
   - Size: 1.2 x 0.8 x 1.5
   
   Kuyruk 2:
   - Position: 0, 0, -11.0
   - Size: 1.0 x 0.6 x 1.2
   
   Kuyruk 3:
   - Position: 0, 0, -13.0
   - Size: 0.8 x 0.4 x 1.0
   ```

3. **Parent-Child İlişkisi:**
   - Kafa → Boyun → Gövde 1 → Gövde 2 → ... → Kuyruk 3
   - Her parça bir öncekine bağlı

4. **Texture:**
   - Kırmızı/siyah ejder derisi
   - Ateş efektleri (opsiyonel)

5. **Export ve Yükle:**
   - `hell_dragon.bbmodel` olarak kaydet
   - ModelEngine'e yükle

6. **Hitbox Ayarları:**
   ```yaml
   # ModelEngine config
   models:
     hell_dragon:
       hitbox:
         segments:
           - {offset: {x: 0, y: 0, z: 0}, size: {w: 2.0, h: 1.5, d: 2.0}}
           - {offset: {x: 0, y: 0, z: -2.0}, size: {w: 1.8, h: 1.3, d: 2.0}}
           - {offset: {x: 0, y: 0, z: -4.0}, size: {w: 1.5, h: 1.0, d: 1.8}}
           # ... daha fazla
   ```

---

## 🔧 SORUN GİDERME

### Problem: Model görünmüyor

**Çözüm:**
1. ModelEngine yüklü mü kontrol et
2. Model dosyası doğru klasörde mi?
3. Texture dosyası var mı?
4. Konsol hatalarını kontrol et

### Problem: Hitbox yanlış

**Çözüm:**
1. ModelEngine config'de hitbox ayarlarını kontrol et
2. Model boyutları ile hitbox boyutları eşleşiyor mu?
3. `/modelengine hitbox set` komutu ile test et

### Problem: Çoklu hitbox çalışmıyor

**Çözüm:**
1. ModelEngine versiyonunu kontrol et (en son sürüm gerekli)
2. Config formatını kontrol et
3. Alternatif: ArmorStand zinciri kullan

---

## 📚 EK KAYNAKLAR

### Blockbench Öğreticileri
- https://www.blockbench.net/docs
- YouTube: "Blockbench tutorial"

### ModelEngine Dokümantasyonu
- https://modelengine.gitbook.io/
- Discord: ModelEngine topluluğu

### Minecraft Entity API
- https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/Entity.html

---

## ✅ ÖZET: YAPILACAKLAR

1. **ModelEngine Kur:**
   - [ ] ModelEngine plugin'ini indir
   - [ ] Citizens plugin'ini kur
   - [ ] Test server'a yerleştir

2. **Model Oluştur/Bul:**
   - [ ] Blockbench'i indir
   - [ ] Kendi modellerini oluştur VEYA
   - [ ] PlanetMinecraft'dan hazır model indir

3. **Model Yükle:**
   - [ ] Model dosyasını ModelEngine klasörüne koy
   - [ ] Texture'ı ekle
   - [ ] `/modelengine model load` ile yükle

4. **Kod Entegrasyonu:**
   - [ ] MobManager.java'ya ModelEngine entegrasyonu ekle
   - [ ] Her mob için model uygula

5. **Hitbox Ayarla:**
   - [ ] ModelEngine config'de hitbox ayarları yap
   - [ ] Test et ve düzelt

---

**Son Güncelleme:** 2024-12-01
**Versiyon:** 1.0

