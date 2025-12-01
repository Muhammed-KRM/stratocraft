# 🎯 Gerçekçi Çözüm: Custom Entity Model ve Hitbox

## ⚠️ ÖNEMLİ: DÜRÜST DEĞERLENDİRME

Araştırmalarım sonucunda şunu söylemeliyim:

**ItemsAdder'ın custom entity model desteği:**
- ✅ Var ama **sınırlı**
- ❌ Hazır modelleri bulmak **gerçekten zor**
- ❌ Çoklu hitbox desteği **belirsiz**

**ModelEngine:**
- ✅ En iyi çözüm ama **ÜCRETLİ** (~$20-30)
- ✅ Hazır modelleri bulmak daha kolay
- ✅ Çoklu hitbox desteği kesin

---

## 💡 GERÇEKÇİ ÇÖZÜMLER

### SEÇENEK 1: ModelEngine (ÜCRETLİ - EN İYİ) ⭐

**Neden:**
- Hazır modelleri bulmak en kolay
- Çoklu hitbox desteği kesin
- En profesyonel çözüm

**Fiyat:** ~$20-30

**Hazır Model Kaynakları:**
- PlanetMinecraft (ModelEngine için modeller)
- MC-Market (ModelEngine model paketleri)
- ModelEngine Discord topluluğu

---

### SEÇENEK 2: MythicMobs + ModelEngine (ÜCRETLİ)

**MythicMobs:**
- Ücretsiz versiyonu var ama sınırlı
- Tam özellikli versiyon ücretli

**ModelEngine:**
- Ücretli (~$20-30)

**Birlikte kullanım:**
- En güçlü kombinasyon
- Ama iki plugin de ücretli

---

### SEÇENEK 3: ArmorStand Manipülasyonu (ÜCRETSİZ - SINIRLI)

**Yöntem:**
- ArmorStand'ları kullanarak görünüm oluştur
- Kod ile hitbox ayarla
- Sınırlı ama ücretsiz

**Avantajlar:**
- ✅ Tamamen ücretsiz
- ✅ Kod ile yapılır
- ✅ Kendi kontrolünüz

**Dezavantajlar:**
- ❌ Sınırlı görünüm (sadece armor stand)
- ❌ Çoklu hitbox zor
- ❌ Animasyon yok

**Örnek Kod:**

```java
public void spawnOrkWithArmorStand(Location loc) {
    if (loc == null || loc.getWorld() == null) return;
    
    // Ana entity (zombie)
    Zombie ork = (Zombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
    ork.setCustomName("§cOrk");
    
    // Görünüm için armor stand (1.5x büyük)
    ArmorStand visual = (ArmorStand) loc.getWorld()
        .spawnEntity(loc.clone().add(0, -1.5, 0), EntityType.ARMOR_STAND);
    
    visual.setVisible(false);
    visual.setGravity(false);
    visual.setMarker(true);
    visual.setSmall(false);
    
    // Zırh ekle (görünüm için)
    if (visual.getEquipment() != null) {
        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
        ItemMeta meta = helmet.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(1); // Custom model data
            helmet.setItemMeta(meta);
        }
        visual.getEquipment().setHelmet(helmet);
    }
    
    // Ana entity'ye bağla
    visual.setLeashHolder(ork);
    
    // Hitbox ayarla (reflection ile)
    try {
        Object nmsEntity = ork.getClass().getMethod("getHandle").invoke(ork);
        // Hitbox ayarlama kodu (versiyona göre değişir)
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

---

### SEÇENEK 4: Resource Pack + Custom Entity (ÜCRETSİZ - KARMAŞIK)

**Yöntem:**
- Resource pack ile görünüm değiştir
- Custom entity API ile hitbox ayarla
- Karmaşık ama ücretsiz

**Avantajlar:**
- ✅ Ücretsiz
- ✅ Görünüm kontrolü

**Dezavantajlar:**
- ❌ Çok karmaşık
- ❌ Çoklu hitbox zor
- ❌ Oyuncular resource pack yüklemeli

---

### SEÇENEK 5: Denizen (ÜCRETSİZ - ORTA)

**Denizen:**
- Ücretsiz
- Script tabanlı
- Custom entity desteği sınırlı

**Avantajlar:**
- ✅ Ücretsiz
- ✅ Script ile kontrol

**Dezavantajlar:**
- ❌ Custom model desteği yok
- ❌ Sadece vanilla entity'ler

---

## 🎯 ÖNERİ: GERÇEKÇİ YAKLAŞIM

### Durum 1: Para Ödeyebiliyorsanız

**ModelEngine Kullanın:**
- En kolay ve en iyi çözüm
- Hazır modelleri bulmak kolay
- Çoklu hitbox desteği kesin
- Fiyat: ~$20-30

**Hazır Model Kaynakları:**
1. **PlanetMinecraft:**
   - Arama: "ModelEngine model"
   - Filtre: "Free" + "ModelEngine"

2. **MC-Market:**
   - ModelEngine model paketleri
   - Genelde ücretli ama kaliteli

3. **ModelEngine Discord:**
   - Topluluk modelleri
   - Ücretsiz paylaşımlar

### Durum 2: Para Ödemek İstemiyorsanız

**ArmorStand Manipülasyonu:**
- Ücretsiz
- Kod ile yapılır
- Sınırlı ama çalışır

**Veya:**
- Basit görünüm değişiklikleri (zırh, boyut)
- Hitbox ayarları (reflection ile)
- Çoklu hitbox için armor stand zinciri

---

## 📋 ARMORSTAND YÖNTEMİ - DETAYLI

### Basit Görünüm (Ücretsiz)

```java
public void spawnOrkWithVisual(Location loc) {
    Zombie ork = (Zombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
    ork.setCustomName("§cOrk");
    
    // Görünüm için zırh
    if (ork.getEquipment() != null) {
        // Zırh ekle
        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        
        ork.getEquipment().setHelmet(helmet);
        ork.getEquipment().setChestplate(chestplate);
        ork.getEquipment().setLeggings(leggings);
        ork.getEquipment().setBoots(boots);
    }
    
    // Boyut ayarla (1.20.4'te scale attribute yok, ama görünüm için armor stand)
    // Hitbox için reflection kullan
}
```

### Hitbox Ayarlama (Reflection)

```java
public void setEntityHitbox(Entity entity, double width, double height) {
    try {
        Object nmsEntity = entity.getClass().getMethod("getHandle").invoke(entity);
        Class<?> entityClass = nmsEntity.getClass();
        
        // BoundingBox field'ını bul
        Field boundingBoxField = entityClass.getField("boundingBox");
        
        // NMS BoundingBox sınıfı
        Class<?> boundingBoxClass = Class.forName("net.minecraft.world.phys.AxisAlignedBB");
        
        // Yeni bounding box oluştur
        double x = entity.getLocation().getX();
        double y = entity.getLocation().getY();
        double z = entity.getLocation().getZ();
        
        Constructor<?> constructor = boundingBoxClass.getConstructor(
            double.class, double.class, double.class,
            double.class, double.class, double.class
        );
        
        Object newBoundingBox = constructor.newInstance(
            x - width/2, y, z - width/2,
            x + width/2, y + height, z + width/2
        );
        
        boundingBoxField.set(nmsEntity, newBoundingBox);
        
    } catch (Exception e) {
        plugin.getLogger().warning("Hitbox ayarlanamadı: " + e.getMessage());
    }
}
```

---

## 🎯 SONUÇ VE ÖNERİ

### Gerçekçi Değerlendirme:

1. **ModelEngine (ÜCRETLİ):**
   - ✅ En kolay
   - ✅ En iyi sonuç
   - ✅ Hazır modelleri bulmak kolay
   - ❌ Para gerekiyor (~$20-30)

2. **ItemsAdder (ÜCRETSİZ ama SINIRLI):**
   - ⚠️ Entity desteği belirsiz
   - ⚠️ Hazır modelleri bulmak zor
   - ⚠️ Çoklu hitbox belirsiz

3. **ArmorStand (ÜCRETSİZ):**
   - ✅ Ücretsiz
   - ✅ Kod ile yapılır
   - ❌ Sınırlı görünüm
   - ❌ Çoklu hitbox zor

### Benim Önerim:

**Eğer para ödeyebiliyorsanız:**
→ **ModelEngine kullanın** (en kolay ve en iyi)

**Eğer para ödemek istemiyorsanız:**
→ **ArmorStand manipülasyonu** ile başlayın
→ Basit görünüm değişiklikleri yapın
→ Hitbox'ları reflection ile ayarlayın
→ İleride ModelEngine'e geçebilirsiniz

---

## 📝 HIZLI KARAR AĞACI

```
Para ödeyebiliyor musunuz?
├─ EVET → ModelEngine kullan (en kolay)
└─ HAYIR → ArmorStand manipülasyonu (ücretsiz ama sınırlı)
```

---

**Son Güncelleme:** 2024-12-01
**Versiyon:** 1.0 (Gerçekçi Değerlendirme)

