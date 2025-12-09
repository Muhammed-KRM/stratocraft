# 🔍 GÜÇ HESAPLAMA SİSTEMİ KONTROL RAPORU

## 📋 RAPOR AMACI

Bu rapor, dökümanlarda belirtilen güç hesaplama özelliklerinin kodda olup olmadığını kontrol eder ve eksikleri belirler.

---

## ✅ VAR OLAN ÖZELLİKLER

### 1. **Silah Gücü** ✅
**Durum:** ✅ TAMAMEN VAR
- ✅ Envanterdeki tüm seviyeli silahlar hesaplanıyor
- ✅ Stack boyutuna göre çarpılıyor
- ✅ Config'den ayarlanabilir

**Dosyalar:**
- `StratocraftPowerSystem.java` (satır 463-482)
- `ClanPowerConfig.java` (weapon level güçleri)

---

### 2. **Zırh Gücü** ✅
**Durum:** ✅ TAMAMEN VAR
- ✅ Takılı zırh + envanterdeki zırhlar hesaplanıyor
- ✅ Stack boyutuna göre çarpılıyor
- ✅ Tam set bonusu var (4 parça)
- ✅ Config'den ayarlanabilir

**Dosyalar:**
- `StratocraftPowerSystem.java` (satır 488-531)
- `ClanPowerConfig.java` (armor level güçleri)

---

### 3. **Özel Item Gücü** ✅
**Durum:** ✅ TAMAMEN VAR
- ✅ Envanterdeki özel itemler hesaplanıyor
- ✅ Tier bazlı güç hesaplama var
- ✅ Stack boyutuna göre çarpılıyor

**Dosyalar:**
- `StratocraftPowerSystem.java` (satır 537-579)

---

### 4. **Ustalık Gücü** ✅
**Durum:** ✅ TAMAMEN VAR
- ✅ Ritüel ustalığı güç hesaplama var
- ✅ Config'den ayarlanabilir

**Dosyalar:**
- `StratocraftPowerSystem.java` (satır 584+)
- `ClanPowerConfig.java` (mastery güçleri)

---

### 5. **Klan Ritüel Blok Gücü** ✅
**Durum:** ✅ TAMAMEN VAR
- ✅ Klan yerleşimindeki ritüel bloklar hesaplanıyor
- ✅ Elmas Blok: 25 puan
- ✅ Obsidyen: 30 puan
- ✅ Config'den ayarlanabilir

**Dosyalar:**
- `ClanPowerSystem.java` (ritual block hesaplama)
- `ClanPowerConfig.java` (ritual block güçleri)

---

### 6. **Klan Ritüel Kaynak Gücü** ✅
**Durum:** ✅ TAMAMEN VAR
- ✅ Ritüellerde kullanılan kaynaklar hesaplanıyor
- ✅ Elmas: 10 puan
- ✅ Kızıl Elmas: 18 puan
- ✅ Karanlık Madde: 50 puan
- ✅ Config'den ayarlanabilir

**Dosyalar:**
- `ClanPowerSystem.java` (ritual resource hesaplama)
- `ClanPowerConfig.java` (ritual resource güçleri)

---

## ❌ EKSİK OLAN ÖZELLİKLER

### 1. **Oyuncu Envanter Materyal Gücü** ❌
**Durum:** ❌ YOK
**Döküman:** `STRATOCRAFT_GUC_SISTEMI_DETAYLI_TASARIM_RAPORU.md` - Ritüel kaynak güçleri var ama sadece klan bazlı

**Eksik Özellikler:**
- ❌ Oyuncu envanterindeki **Elmas** güç vermiyor (olması gereken: 10 puan/item)
- ❌ Oyuncu envanterindeki **Karanlık Madde** güç vermiyor (olması gereken: 50 puan/item)
- ❌ Oyuncu envanterindeki **Obsidyen** güç vermiyor (olması gereken: 30 puan/item)
- ❌ Oyuncu envanterindeki **Kızıl Elmas** güç vermiyor (olması gereken: 18 puan/item)
- ❌ Oyuncu envanterindeki **Titanyum** güç vermiyor (olması gereken: 15 puan/item)
- ❌ Diğer değerli materyaller güç vermiyor

**Mevcut Durum:**
- Sadece **ritüel kaynak güçleri** klan bazlı hesaplanıyor
- Oyuncu envanterindeki materyaller **hiç hesaplanmıyor**

**Yapılması Gerekenler:**
1. `calculateMaterialPower()` metodu ekle
2. Envanterdeki materyalleri kontrol et (Elmas, Karanlık Madde, Obsidyen, vb.)
3. Config'den materyal güç değerlerini al
4. Stack boyutuna göre çarp
5. `calculateGearPower()` metoduna ekle

**Öncelik:** ⭐⭐⭐⭐ (Yüksek - Dökümanla uyum için kritik)

---

### 2. **Materyal Güç Config Değerleri** ⚠️
**Durum:** ⚠️ KISMEN VAR
- ✅ Ritüel kaynak güçleri config'de var (klan bazlı)
- ❌ Oyuncu envanter materyal güçleri config'de yok

**Config'de Var Olan:**
```yaml
ritual-resources:
  iron: 5
  diamond: 10
  red-diamond: 18
  dark-matter: 50
  titanium: 15
```

**Eksik:**
- Oyuncu envanter materyal güçleri için ayrı config bölümü yok
- Obsidyen için oyuncu envanter güç değeri yok (ritüel blok olarak var: 30)

**Yapılması Gerekenler:**
1. Config'e `player-inventory-materials` bölümü ekle
2. Materyal güç değerlerini ekle (ritüel kaynak güçleriyle aynı veya farklı olabilir)

**Öncelik:** ⭐⭐⭐ (Orta - Config tutarlılığı için)

---

## 📊 ÖZET TABLO

| # | Özellik | Durum | Öncelik | Notlar |
|---|---------|-------|---------|--------|
| 1 | Silah Gücü | ✅ VAR | - | Tam çalışıyor |
| 2 | Zırh Gücü | ✅ VAR | - | Tam çalışıyor |
| 3 | Özel Item Gücü | ✅ VAR | - | Tam çalışıyor |
| 4 | Ustalık Gücü | ✅ VAR | - | Tam çalışıyor |
| 5 | Klan Ritüel Blok Gücü | ✅ VAR | - | Tam çalışıyor |
| 6 | Klan Ritüel Kaynak Gücü | ✅ VAR | - | Tam çalışıyor |
| 7 | **Oyuncu Envanter Materyal Gücü** | ❌ YOK | ⭐⭐⭐⭐ | **EN ÖNEMLİ EKSİK** |
| 8 | Materyal Güç Config | ⚠️ KISMEN | ⭐⭐⭐ | Config'de eksik |

---

## 🎯 ÖNCELİK SIRALAMASI

### 🔴 YÜKSEK ÖNCELİK (Hemen Yapılmalı)

1. **Oyuncu Envanter Materyal Gücü** ⭐⭐⭐⭐
   - Elmas, Karanlık Madde, Obsidyen, Kızıl Elmas, Titanyum gibi materyallerin güç vermesi
   - Dökümanla uyum için kritik
   - Oyuncu deneyimi için önemli

### 🟡 ORTA ÖNCELİK (Yakın Gelecekte)

2. **Materyal Güç Config** ⭐⭐⭐
   - Config'e oyuncu envanter materyal güçleri ekle
   - Ritüel kaynak güçleriyle tutarlılık sağla

---

## 📝 SONUÇ

### ✅ Başarılar
- **Temel güç sistemleri tamamen çalışıyor:** Silah, zırh, özel item, ustalık
- **Klan bazlı güç sistemleri çalışıyor:** Ritüel blok, ritüel kaynak
- **Config tabanlı yönetim var:** Tüm değerler config'den ayarlanabilir

### ⚠️ Eksikler
- **En kritik eksik:** Oyuncu envanterindeki materyallerin (Elmas, Karanlık Madde, Obsidyen, vb.) güç vermemesi
- **Config eksikliği:** Oyuncu envanter materyal güçleri için config bölümü yok

### 🎯 Öneriler
1. **Öncelik 1:** `calculateMaterialPower()` metodunu ekle ve `calculateGearPower()` metoduna entegre et
2. **Öncelik 2:** Config'e `player-inventory-materials` bölümü ekle
3. **Öncelik 3:** Test et ve dengele

---

## 🔧 ÖNERİLEN KOD DEĞİŞİKLİKLERİ

### 1. `StratocraftPowerSystem.java` - Materyal Gücü Ekle

```java
/**
 * Materyal gücü hesapla (envanterdeki değerli materyaller)
 * Elmas, Karanlık Madde, Obsidyen, Kızıl Elmas, Titanyum vb.
 */
private double calculateMaterialPower(Player player) {
    double totalPower = 0.0;
    
    // Envanterdeki tüm itemleri kontrol et
    for (ItemStack item : player.getInventory().getContents()) {
        if (item == null || item.getType() == org.bukkit.Material.AIR) continue;
        
        // Seviyeli silah/zırh/özel item değilse materyal kontrolü yap
        if (!ItemManager.isLeveledWeapon(item) && 
            !ItemManager.isLeveledArmor(item) && 
            !isSpecialItem(item)) {
            
            // Materyal gücü (config'den)
            double materialPower = powerConfig.getMaterialPower(item.getType());
            if (materialPower > 0) {
                // Stack boyutuna göre çarp
                totalPower += materialPower * item.getAmount();
            }
        }
    }
    
    return totalPower;
}

// calculateGearPower metodunu güncelle:
public double calculateGearPower(Player player) {
    return calculateWeaponPower(player) + 
           calculateArmorPower(player) + 
           calculateSpecialItemPower(player) +
           calculateMaterialPower(player); // YENİ
}
```

### 2. `PowerConfig.java` - Materyal Gücü Config Ekle

```java
// Config'den materyal gücü al
public double getMaterialPower(Material material) {
    // Config'den oku (player-inventory-materials bölümü)
    // Varsayılan değerler:
    switch (material) {
        case DIAMOND: return 10.0;
        case OBSIDIAN: return 30.0;
        case EMERALD: return 35.0;
        case GOLD_INGOT: return 5.0;
        case IRON_INGOT: return 3.0;
        // Karanlık Madde, Kızıl Elmas, Titanyum için özel item kontrolü gerekebilir
        default: return 0.0;
    }
}
```

### 3. `config.yml` - Materyal Güç Config Ekle

```yaml
power-system:
  # Oyuncu envanter materyal güçleri
  player-inventory-materials:
    diamond: 10          # Elmas
    obsidian: 30         # Obsidyen
    emerald: 35          # Zümrüt
    gold_ingot: 5        # Altın
    iron_ingot: 3        # Demir
    # Özel itemler için NBT tag kontrolü gerekebilir
    # dark-matter: 50    # Karanlık Madde (özel item)
    # red-diamond: 18    # Kızıl Elmas (özel item)
    # titanium: 15       # Titanyum (özel item)
```

---

**Rapor Tarihi:** 2024  
**Versiyon:** 1.0  
**Durum:** ✅ Kontrol Tamamlandı - Eksikler Belirlendi
