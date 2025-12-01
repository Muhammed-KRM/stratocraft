# 🎨 Hazır Model Kurulum Rehberi - Adım Adım

## 💰 MODELENGINE FİYAT BİLGİSİ

**ModelEngine Ücretli Bir Plugin:**
- **SpigotMC:** Genelde $15-25 arası (versiyona göre değişir)
- **MC-Market:** Genelde $20-30 arası
- **Resmi Site:** https://www.spigotmc.org/resources/modelengine.107955/

**⚠️ ÖNEMLİ:** 
- **HAZIR MODEL KULLANSA DA PARA ÖDEMENİZ GEREKİR!**
- ModelEngine plugin'ini kullanmak için satın almanız gerekir
- Hazır model kullanıyor olmanız fiyatı değiştirmez
- Plugin'in kendisi ücretli, modeller ücretsiz olabilir ama plugin ücretli!

---

## ⚠️ ÖNEMLİ: ItemsAdder HAKKINDA GERÇEKÇİ DEĞERLENDİRME

**ItemsAdder Custom Entity Desteği:**
- ⚠️ **SINIRLI** - Entity desteği var ama gelişmiş değil
- ❌ **HAZIR MODELLERİ BULMAK ZOR** - Gerçekten zor bulunuyor
- ❌ **ÇOKLU HİTBOX BELİRSİZ** - Desteği net değil
- ⚠️ Daha çok **item ve block** için tasarlanmış

**SONUÇ:** 
- ItemsAdder ile hazır mob tasarımları bulmak **GERÇEKTEN ZOR**
- Custom entity model desteği **SINIRLI**
- **ÖNERİM:** ModelEngine kullanın (ücretli ama en iyi çözüm)

---

## 📋 HAZIR MODEL KURULUMU - ADIM ADIM

### SEÇENEK 1: ItemsAdder ile (ÜCRETSİZ) ⭐ ÖNERİLEN

#### ADIM 1: ItemsAdder Kurulumu

1. **ItemsAdder İndir:**
   - https://www.spigotmc.org/resources/itemsadder.73355/
   - "Download" butonuna tıkla
   - Minecraft 1.20.4 için uygun versiyonu seç

2. **Test Server'a Kur:**
   ```
   C:\mc\test-server\plugins\ItemsAdder.jar
   ```

3. **Sunucuyu Başlat:**
   - Konsolda "ItemsAdder enabled" mesajını görmelisiniz
   - Otomatik olarak klasör yapısı oluşur:
     ```
     C:\mc\test-server\plugins\ItemsAdder\
     ├── contents\
     │   ├── items\
     │   ├── entities\
     │   └── resourcepack\
     ```

#### ADIM 2: Hazır Model Bulma

**Kaynak 1: PlanetMinecraft (ÖNERİLEN)**

1. **Siteye Git:**
   - https://www.planetminecraft.com/resources/models/

2. **Arama Yap:**
   - Arama kutusuna: `"ork" "blockbench"` veya `"dragon" "blockbench"`
   - Filtreler:
     - ✅ "Free" işaretle
     - ✅ "Downloadable" işaretle
     - ✅ "Creative Commons" lisanslı olanları seç

3. **Model Seç:**
   - Beğendiğiniz bir modele tıklayın
   - Sayfada "Download" butonunu bulun
   - İndirme başlar

4. **Dosya Formatı:**
   - İndirilen dosya genelde `.bbmodel` (Blockbench) formatında olur
   - Veya `.geo.json` (Geckolib) formatında olabilir

**Kaynak 2: Blockbench Community**

1. **Siteye Git:**
   - https://www.blockbench.net/community
   - Veya: https://github.com/search?q=minecraft+blockbench+model

2. **Model İndir:**
   - `.bbmodel` formatında modeller bulun
   - "Download" ile indirin

**Kaynak 3: CurseForge**

1. **Siteye Git:**
   - https://www.curseforge.com/minecraft/texture-packs
   - Arama: "custom entity model"

2. **Model İndir:**
   - Uygun modeli bulun ve indirin

#### ADIM 3: Model Dosyalarını Yerleştirme

**ItemsAdder Klasör Yapısı:**

```
C:\mc\test-server\plugins\ItemsAdder\
└── contents\
    └── entities\
        └── ork\
            ├── ork.geo.json      ← Model dosyası
            ├── ork.png           ← Texture (doku)
            └── ork.yml           ← Config dosyası (oluşturulacak)
```

**Adımlar:**

1. **Entity Klasörü Oluştur:**
   ```
   C:\mc\test-server\plugins\ItemsAdder\contents\entities\ork\
   ```
   - `ork` yerine model adınızı yazın (örn: `hell_dragon`, `goblin`)

2. **Model Dosyasını Kopyala:**
   - İndirdiğiniz `.bbmodel` dosyasını bulun
   - Eğer `.bbmodel` ise, Blockbench ile `.geo.json`'a çevirin:
     - Blockbench'i aç
     - "File" → "Open" → `.bbmodel` dosyasını aç
     - "File" → "Export" → "Geckolib Entity" → `ork.geo.json` olarak kaydet
   - `ork.geo.json` dosyasını şuraya kopyala:
     ```
     C:\mc\test-server\plugins\ItemsAdder\contents\entities\ork\ork.geo.json
     ```

3. **Texture (Doku) Dosyasını Kopyala:**
   - Model ile birlikte gelen `.png` dosyasını bulun
   - Eğer yoksa, Blockbench'te texture'ı export edin
   - `ork.png` dosyasını şuraya kopyala:
     ```
     C:\mc\test-server\plugins\ItemsAdder\contents\entities\ork\ork.png
     ```

4. **Config Dosyası Oluştur:**
   - `ork.yml` dosyası oluştur:
     ```yaml
     # C:\mc\test-server\plugins\ItemsAdder\contents\entities\ork\ork.yml
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
            # Çoklu hitbox (uzun canavarlar için)
            # segments:
            #   - offset: {x: 0, y: 0, z: 0}
            #     size: {width: 0.9, height: 2.7, depth: 0.9}
            #   - offset: {x: 0, y: 0, z: 2.0}
            #     size: {width: 0.8, height: 2.5, depth: 0.8}
          attributes:
            max_health: 80.0
            attack_damage: 8.0
     ```

#### ADIM 4: ItemsAdder'i Yeniden Yükle

1. **Sunucuda Komut:**
   ```
   /iareload
   ```
   - Veya sunucuyu yeniden başlat

2. **Kontrol:**
   - Konsolda hata mesajı olmamalı
   - `/iaentities` komutu ile entity listesini görebilirsiniz

#### ADIM 5: Kod Entegrasyonu (OPSİYONEL)

**MobManager.java'ya Ekle:**

```java
import dev.lone.itemsadder.api.CustomEntity;

public void spawnOrkWithModel(Location loc) {
    if (loc == null || loc.getWorld() == null) return;
    
    // ItemsAdder entity spawn
    CustomEntity customEntity = CustomEntity.spawn("itemsadder:ork", loc);
    if (customEntity != null) {
        Entity entity = customEntity.getBukkitEntity();
        if (entity instanceof Zombie) {
            Zombie ork = (Zombie) entity;
            ork.setCustomName("§cOrk");
            // Diğer ayarlar...
        }
    } else {
        // Fallback: Normal zombie spawn
        spawnOrk(loc); // Mevcut metodunuz
    }
}
```

**VEYA Basit Yöntem (Kod Değişikliği Yok):**

ItemsAdder entity'leri otomatik olarak spawn olur, sadece config'de ayarlayın:

```yaml
# ork.yml
entities:
  ork:
    # ... yukarıdaki config
    spawn_egg:
      enabled: true
      material: ZOMBIE_SPAWN_EGG
```

Sonra oyunda:
```
/ia give <player> ork_spawn_egg
```

---

### SEÇENEK 2: ModelEngine ile (ÜCRETLİ)

#### ADIM 1: ModelEngine Satın Alma ve Kurulum

1. **ModelEngine Satın Al:**
   - https://www.spigotmc.org/resources/modelengine.107955/
   - Veya: https://www.mc-market.org/resources/22155/
   - Fiyat: ~$20-30 (versiyona göre değişir)

2. **Citizens Kur (ZORUNLU):**
   - https://www.spigotmc.org/resources/citizens.13811/
   - Ücretsiz

3. **Test Server'a Kur:**
   ```
   C:\mc\test-server\plugins\ModelEngine.jar
   C:\mc\test-server\plugins\Citizens.jar
   ```

4. **Sunucuyu Başlat:**
   - Konsolda "ModelEngine enabled" mesajını görmelisiniz

#### ADIM 2: Hazır Model Bulma

**Aynı kaynaklar (yukarıdaki gibi):**
- PlanetMinecraft
- Blockbench Community
- CurseForge

#### ADIM 3: Model Dosyalarını Yerleştirme

**ModelEngine Klasör Yapısı:**

```
C:\mc\test-server\plugins\ModelEngine\
├── models\
│   └── ork.bbmodel      ← Model dosyası
└── textures\
    └── ork.png          ← Texture (doku)
```

**Adımlar:**

1. **Model Dosyasını Kopyala:**
   - İndirdiğiniz `.bbmodel` dosyasını bulun
   - `ork.bbmodel` dosyasını şuraya kopyala:
     ```
     C:\mc\test-server\plugins\ModelEngine\models\ork.bbmodel
     ```

2. **Texture Dosyasını Kopyala:**
   - Model ile birlikte gelen `.png` dosyasını bulun
   - `ork.png` dosyasını şuraya kopyala:
     ```
     C:\mc\test-server\plugins\ModelEngine\textures\ork.png
     ```

#### ADIM 4: ModelEngine'de Model Yükleme

1. **Sunucuda Komut:**
   ```
   /meg model load ork
   ```

2. **Kontrol:**
   ```
   /meg model list
   ```
   - `ork` listede görünmeli

#### ADIM 5: Model'i Entity'ye Uygulama

**Yöntem 1: Komut ile (Test için)**

1. **Entity Spawn Et:**
   ```
   /summon zombie ~ ~ ~
   ```

2. **Model Uygula:**
   ```
   /meg model apply @e[type=zombie,limit=1] ork
   ```

**Yöntem 2: Kod ile (MobManager.java)**

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
        // Fallback: Normal spawn
    }
    
    // Diğer ayarlar
    if (ork.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
        ork.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(80.0);
    }
    ork.setHealth(80.0);
}
```

#### ADIM 6: Hitbox Ayarları

**ModelEngine Config:**

```
C:\mc\test-server\plugins\ModelEngine\config.yml
```

Ekle:

```yaml
models:
  ork:
    hitbox:
      width: 0.9
      height: 2.7
      depth: 0.9
```

Veya komut ile:

```
/meg hitbox set @e[type=zombie,limit=1] 0.9 2.7 0.9
```

---

## 🎯 ÖRNEK: ORK MODELİ KURULUMU

### ItemsAdder ile (ÜCRETSİZ)

1. **Model İndir:**
   - PlanetMinecraft'dan "ork blockbench model" ara
   - Ücretsiz bir model bul ve indir

2. **Dosyaları Yerleştir:**
   ```
   C:\mc\test-server\plugins\ItemsAdder\contents\entities\ork\
   ├── ork.geo.json  (Blockbench'ten export et)
   ├── ork.png       (Texture)
   └── ork.yml       (Config - yukarıdaki örnek)
   ```

3. **Yeniden Yükle:**
   ```
   /iareload
   ```

4. **Test:**
   ```
   /ia give <player> ork_spawn_egg
   ```

### ModelEngine ile (ÜCRETLİ)

1. **Model İndir:**
   - Aynı kaynaklardan

2. **Dosyaları Yerleştir:**
   ```
   C:\mc\test-server\plugins\ModelEngine\models\ork.bbmodel
   C:\mc\test-server\plugins\ModelEngine\textures\ork.png
   ```

3. **Yükle:**
   ```
   /meg model load ork
   ```

4. **Test:**
   ```
   /summon zombie ~ ~ ~
   /meg model apply @e[type=zombie,limit=1] ork
   ```

---

## ⚠️ ÖNEMLİ NOTLAR

### Format Dönüştürme

**`.bbmodel` → `.geo.json` (ItemsAdder için):**

1. Blockbench'i aç
2. "File" → "Open" → `.bbmodel` dosyasını aç
3. "File" → "Export" → "Geckolib Entity"
4. `ork.geo.json` olarak kaydet

**`.geo.json` → `.bbmodel` (ModelEngine için):**

1. Blockbench'i aç
2. "File" → "Open" → `.geo.json` dosyasını aç
3. "File" → "Export" → "Bedrock Entity"
4. `ork.bbmodel` olarak kaydet

### Telif Hakkı

✅ **Güvenli:**
- "Free to use"
- "Creative Commons"
- "No attribution required"

❌ **Dikkat:**
- "All rights reserved"
- Ücretli modeller (izin alın)

### Kod Değişikliği Gerekli mi?

**ItemsAdder:**
- ✅ Kod değişikliği OPSİYONEL
- Config ile çalışır
- Spawn egg ile test edebilirsiniz

**ModelEngine:**
- ⚠️ Kod değişikliği ÖNERİLİR
- Komut ile de çalışır ama kod daha iyi

---

## 📝 ÖZET: HANGİSİNİ SEÇMELİ? (GERÇEKÇİ DEĞERLENDİRME)

### ⚠️ ItemsAdder (ÜCRETSİZ ama SINIRLI):
- ✅ **ÜCRETSİZ** - Para ödemenize gerek yok
- ❌ **HAZIR MODELLERİ BULMAK ÇOK ZOR** - Gerçekten zor bulunuyor
- ❌ Custom entity desteği **SINIRLI**
- ❌ Çoklu hitbox desteği **BELİRSİZ**
- ⚠️ Daha çok item/block için tasarlanmış

### 💵 ModelEngine (ÜCRETLİ ama EN İYİ) ⭐ ÖNERİLEN:
- ⚠️ **ÜCRETLİ** - ~$20-30 ödemeniz gerekir
- ✅ **HAZIR MODELLERİ BULMAK KOLAY** - PlanetMinecraft, MC-Market
- ✅ Custom entity desteği **TAM**
- ✅ Çoklu hitbox desteği **KESİN**
- ✅ En profesyonel çözüm
- ✅ Hazır model kullanıyor olsanız bile plugin ücretli (ama değer)

### 🆓 ArmorStand Manipülasyonu (ÜCRETSİZ ama SINIRLI):
- ✅ **TAMAMEN ÜCRETSİZ**
- ✅ Kod ile yapılır
- ❌ Sınırlı görünüm (sadece armor stand)
- ❌ Çoklu hitbox zor
- ❌ Animasyon yok

**GERÇEKÇİ SONUÇ:** 
- **ItemsAdder ile hazır mob tasarımları bulmak GERÇEKTEN ZOR**
- **ModelEngine en iyi çözüm ama ÜCRETLİ**
- **Para ödeyebiliyorsanız ModelEngine kullanın**
- **Para ödemek istemiyorsanız ArmorStand manipülasyonu ile başlayın**

---

## 🚀 HIZLI BAŞLANGIÇ (ItemsAdder - ÜCRETSİZ)

1. **ItemsAdder İndir ve Kur:**
   ```
   C:\mc\test-server\plugins\ItemsAdder.jar
   ```

2. **Model İndir:**
   - PlanetMinecraft'dan ücretsiz model bul

3. **Dosyaları Yerleştir:**
   ```
   C:\mc\test-server\plugins\ItemsAdder\contents\entities\ork\
   ├── ork.geo.json
   ├── ork.png
   └── ork.yml
   ```

4. **Yeniden Yükle:**
   ```
   /iareload
   ```

5. **Test:**
   ```
   /ia give <player> ork_spawn_egg
   ```

**Hepsi bu kadar! Kod değişikliği gerekmez!**

---

**Son Güncelleme:** 2024-12-01
**Versiyon:** 1.0

