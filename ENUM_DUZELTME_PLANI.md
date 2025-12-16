# Enum Düzeltme Planı

## 🔍 TESPİT EDİLEN SORUNLAR

### 1. BatteryCategory ✅ DOĞRU
- **Mevcut:** ATTACK, CONSTRUCTION, SUPPORT
- **Durum:** ✅ Doğru - 3 kategori var
- **Aksiyon:** Değişiklik yok

### 2. WeaponType ❌ YANLIŞ
- **Mevcut:** SWORD, AXE, BOW
- **Sorun:** 
  - Silahlar özel itemler (WEAPON_L1_1, WEAPON_L2_1 gibi)
  - Kılıç/yay gibi kategori yok
  - Saat, pusula gibi özel itemler de var
- **Doğru Kategorizasyon:** 
  - ATTACK (Saldırı itemleri - silahlar, savaş eşyaları)
  - DEFENSE (Savunma itemleri - zırhlar, kalkanlar)
  - SUPPORT (Destek itemleri - şifa, hız, efekt veren)
  - CONSTRUCTION (Oluşturma itemleri - blok oluşturma, yapı)
  - UTILITY (Yardımcı itemler - COMPASS, CLOCK, RECIPE, vb.)
- **Aksiyon:** WeaponType'ı ItemCategory olarak değiştir

### 3. ArmorType ⚠️ KONTROL GEREKLİ
- **Mevcut:** HELMET, CHESTPLATE, LEGGINGS, BOOTS
- **Durum:** Zırhlar özel itemler (ARMOR_L1_1, ARMOR_L2_1 gibi)
- **Sorun:** Zırhlar da özel itemler, bu kategoriler yanlış
- **Aksiyon:** ArmorType'ı da ItemCategory kullanacak şekilde değiştir

### 4. RecipeCategory ⚠️ KONTROL GEREKLİ
- **Mevcut:** WEAPON, ARMOR, TOOL, STRUCTURE, TRAP, BATTERY, RITUAL, CONSUMABLE, MATERIAL, SPECIAL
- **Durum:** Bu kategoriler doğru görünüyor, ama kontrol gerekli

### 5. RecipeType ⚠️ KONTROL GEREKLİ
- **Mevcut:** CRAFTING, FURNACE, SMITHING, BREWING, ENCHANTING, CUSTOM, STRUCTURE, BATTERY, RITUAL, GHOST
- **Durum:** Bu tipler doğru görünüyor, ama kontrol gerekli

## 🎯 ÇÖZÜM PLANI

### 1. Yeni Enum: ItemCategory
```java
public enum ItemCategory {
    ATTACK,         // Saldırı itemleri (WEAPON, WAR_FAN, vb.)
    DEFENSE,        // Savunma itemleri (ARMOR, TOWER_SHIELD, vb.)
    SUPPORT,        // Destek itemleri (şifa, hız, efekt veren)
    CONSTRUCTION,   // Oluşturma itemleri (blok oluşturma, yapı)
    UTILITY         // Yardımcı itemler (PERSONAL_TERMINAL, COMPASS, CLOCK, RECIPE, vb.)
}
```

### 2. WeaponType → ItemCategory
- WeaponType silinmeli
- ItemCategory kullanılmalı
- Tüm özel itemler (WEAPON, ARMOR, COMPASS, CLOCK, RECIPE, vb.) ItemCategory ile kategorize edilmeli

### 3. ArmorType → ItemCategory
- ArmorType silinmeli (zırhlar da özel itemler)
- ItemCategory kullanılmalı

### 4. Model Güncellemeleri
- WeaponItem modeli → ItemCategory kullanmalı
- BaseItem modeli → ItemCategory field'ı eklenmeli

### 5. Admin Komutları
- Tab completion güncellenmeli
- ItemCategory'ye göre filtreleme yapılmalı

## 📋 YAPILACAKLAR

1. ✅ BatteryCategory kontrol edildi - Doğru
2. ❌ WeaponType → ItemCategory'ye dönüştür
3. ❌ ArmorType → ItemCategory kullan (sil)
4. ⚠️ RecipeCategory kontrol et
5. ⚠️ RecipeType kontrol et
6. ⚠️ Diğer enum'ları kontrol et
7. ⚠️ Model güncellemeleri
8. ⚠️ Admin komutlarını güncelle
9. ⚠️ Tab completion'ı güncelle

