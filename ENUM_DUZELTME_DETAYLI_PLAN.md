# Enum Düzeltme Detaylı Plan

## 🔍 TESPİT EDİLEN SORUNLAR

### 1. BatteryCategory ✅ DOĞRU
- **Mevcut:** ATTACK, CONSTRUCTION, SUPPORT
- **Durum:** ✅ Doğru - 3 kategori var
- **Aksiyon:** Değişiklik yok

### 2. WeaponType ❌ YANLIŞ
- **Mevcut:** SWORD, AXE, BOW (merkezi enum)
- **Sorun:** 
  - Silahlar özel itemler (WEAPON_L1_1, WEAPON_L2_1 gibi)
  - Kılıç/yay gibi kategori yok
  - ItemManager'da WeaponType sadece crafting tarifleri için kullanılıyor (hangi material - IRON_SWORD, DIAMOND_AXE, vb.)
  - Ama kategori olarak kullanılmamalı
- **Aksiyon:** 
  - WeaponType merkezi enum'u sil
  - ItemManager'daki inner WeaponType enum'u tut (sadece crafting için)
  - ItemCategory kullan

### 3. ArmorType ⚠️ KONTROL GEREKLİ
- **Mevcut:** HELMET, CHESTPLATE, LEGGINGS, BOOTS (merkezi enum)
- **Durum:** 
  - Zırhlar özel itemler (ARMOR_L1_1, ARMOR_L2_1 gibi)
  - ItemManager'da ArmorType sadece crafting tarifleri için kullanılıyor (hangi material - IRON_HELMET, DIAMOND_CHESTPLATE, vb.)
  - Ama kategori olarak kullanılmamalı
- **Aksiyon:**
  - ArmorType merkezi enum'u sil
  - ItemManager'daki inner ArmorType enum'u tut (sadece crafting için)
  - ItemCategory kullan

### 4. RecipeCategory ⚠️ KONTROL GEREKLİ
- **Mevcut:** WEAPON, ARMOR, TOOL, STRUCTURE, TRAP, BATTERY, RITUAL, CONSUMABLE, MATERIAL, SPECIAL
- **Durum:** Bu kategoriler doğru görünüyor (tarif kategorileri)
- **Aksiyon:** Kontrol et, gerekirse düzelt

### 5. RecipeType ⚠️ KONTROL GEREKLİ
- **Mevcut:** CRAFTING, FURNACE, SMITHING, BREWING, ENCHANTING, CUSTOM, STRUCTURE, BATTERY, RITUAL, GHOST
- **Durum:** Bu tipler doğru görünüyor (tarif tipleri)
- **Aksiyon:** Kontrol et, gerekirse düzelt

## 🎯 ÇÖZÜM PLANI

### 1. ItemCategory Enum'u ✅ OLUŞTURULDU
```java
public enum ItemCategory {
    ATTACK,         // Saldırı eşyaları (WEAPON, WAR_FAN, vb.)
    DEFENSE,        // Savunma eşyaları (ARMOR, TOWER_SHIELD, vb.)
    SUPPORT,        // Destek eşyaları (şifa, hız, efekt veren)
    CONSTRUCTION,   // Oluşturma eşyaları (blok oluşturma, yapı)
    UTILITY         // Yardımcı eşyalar (PERSONAL_TERMINAL, COMPASS, CLOCK, RECIPE, vb.)
}
```

### 2. WeaponType Merkezi Enum'u Sil
- `src/main/java/me/mami/stratocraft/enums/WeaponType.java` sil
- ItemManager'daki inner WeaponType enum'u tut (sadece crafting için)

### 3. ArmorType Merkezi Enum'u Sil
- `src/main/java/me/mami/stratocraft/enums/ArmorType.java` sil
- ItemManager'daki inner ArmorType enum'u tut (sadece crafting için)

### 4. Model Güncellemeleri
- WeaponItem modeli → ItemCategory kullanmalı (eğer kullanılıyorsa)
- BaseItem modeli → ItemCategory field'ı eklenmeli (eğer kullanılıyorsa)

### 5. Admin Komutları Güncelle
- `give` komutu kategorileri:
  - `attack` → ItemCategory.ATTACK
  - `defense` → ItemCategory.DEFENSE
  - `support` → ItemCategory.SUPPORT
  - `construction` → ItemCategory.CONSTRUCTION
  - `utility` → ItemCategory.UTILITY
- Eski kategoriler (`weapon`, `armor`) → Yeni kategorilere map et

### 6. Tab Completion Güncelle
- `getGiveTabComplete` metodunu güncelle
- ItemCategory'ye göre filtreleme yap

## 📋 YAPILACAKLAR SIRASI

1. ✅ ItemCategory enum'u oluşturuldu
2. ❌ WeaponType merkezi enum'u sil
3. ❌ ArmorType merkezi enum'u sil
4. ⚠️ Model güncellemeleri (eğer kullanılıyorsa)
5. ⚠️ Admin komutlarını güncelle
6. ⚠️ Tab completion'ı güncelle
7. ⚠️ Tüm kullanımları kontrol et ve güncelle

