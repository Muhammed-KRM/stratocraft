# Enum ve Admin Komut Güncelleme Raporu

## ✅ TAMAMLANAN İŞLEMLER

### 1. ContractType Enum Düzeltildi
- **Dosya:** `src/main/java/me/mami/stratocraft/enums/ContractType.java`
- **Eski Kategoriler (Silindi):**
  - DELIVERY, ESCORT, PROTECTION, TRADE, RESOURCE, COMBAT, EXPLORATION
- **Yeni Kategoriler:**
  - `RESOURCE_COLLECTION` - Kaynak toplama kontratları (şu kadar kaynak ver/verme)
  - `CONSTRUCTION` - İnşaat kontratları (şu yapıyı yapma/yap)
  - `COMBAT` - Saldırı kontratları (şu oyuncuyu öldürme/öldür, şu oyuncuya vurma/vur)
  - `TERRITORY` - Bölge kontratları (şu verilen 4 köşenin kordinatları arasındaki bölgeye gitme/git)

### 2. PenaltyType Enum Oluşturuldu
- **Dosya:** `src/main/java/me/mami/stratocraft/enums/PenaltyType.java`
- **Ceza Tipleri:**
  - `HEALTH_PENALTY` - Can cezası (şu kadar kalıcı canı gidecek)
  - `BANK_PENALTY` - Banka cezası (bankadan şu kadar şu kaynak bana gelecek, yoksa borç olacak, bankaya koyduğu anda geçecek)
  - `MORTGAGE` - Hipotek (şu item silinecek/bana geçecek)

### 3. MissionScope Enum Oluşturuldu
- **Dosya:** `src/main/java/me/mami/stratocraft/enums/MissionScope.java`
- **Görev Kapsamı:**
  - `PERSONAL` - Kişisel görevler
  - `CLAN` - Klan görevleri

### 4. MissionType Enum Güncellendi
- **Dosya:** `src/main/java/me/mami/stratocraft/enums/MissionType.java`
- **Değişiklikler:**
  - Kişisel görevler ve klan görevleri ayrıldı
  - Kişisel görevler: KILL_MOBS, COLLECT_ITEMS, EXPLORE_AREA, TRADE_ITEMS, CRAFT_ITEMS, DEFEAT_BOSS, SURVIVE_DISASTER
  - Klan görevleri: BUILD_STRUCTURE, DEFEND_CLAN, COMPLETE_RITUAL, CLAN_TERRITORY, CLAN_WAR, CLAN_RESOURCE
  - Not: Görevler MissionScope ile kişisel veya klan görevi olarak işaretlenir

### 5. Battery Admin Komutları Güncellendi
- **Dosya:** `src/main/java/me/mami/stratocraft/command/AdminCommandExecutor.java`
- **Yeni Format:** `build battery <kategori> <seviye> <isim>`
- **Kategoriler:** attack, construction, support
- **Seviyeler:** 1-5
- **Tab Completion:**
  - `build battery` → kategorileri göster (attack, construction, support)
  - `build battery <kategori>` → seviyeleri göster (1, 2, 3, 4, 5)
  - `build battery <kategori> <seviye>` → isimleri göster (kategori ve seviyeye göre filtrelenmiş)
- **Eski Format:** `build battery <seviye> <isim>` (hala çalışıyor, geriye uyumluluk)
- **Yeni Metodlar:**
  - `buildBatteryByCategoryLevelAndName()` - Kategori, seviye ve isme göre batarya oluşturur
  - `getBatteryNamesByCategoryAndLevel()` - Kategori ve seviyeye göre batarya isimlerini döndürür
  - `getBatteryCategoryFromName()` - Batarya isminden kategoriyi tahmin eder

### 6. Weapon Admin Komutları
- **Mevcut Format:** `give weapon <seviye> <isim>` (zaten doğru format)
- **Tab Completion:**
  - `give weapon` → seviyeleri göster (1, 2, 3, 4, 5)
  - `give weapon <seviye>` → isimleri göster (seviyeye göre filtrelenmiş)
- **Not:** Weapon'lar zaten ItemCategory.ATTACK altında, kategori eklemeye gerek yok

## 📋 KULLANIM ÖRNEKLERİ

### Battery Admin Komutları
**Yeni Format (Önerilen):**
```
/stratocraft build battery attack 5 yildirim_firtinasi
/stratocraft build battery construction 3 tas_kalesi
/stratocraft build battery support 2 can_hiz_kombinasyonu
```

**Eski Format (Hala Çalışıyor):**
```
/stratocraft build battery 5 support_heal_l5
```

### Weapon Admin Komutları
**Mevcut Format:**
```
/stratocraft give weapon 1 hiz_hançeri
/stratocraft give weapon 5 zamanı_büken
```

## 🔄 GERİYE UYUMLULUK

- ✅ Eski battery komut formatı (`build battery <seviye> <isim>`) hala çalışıyor
- ✅ Eski weapon komut formatı (`give weapon <seviye> <isim>`) hala çalışıyor
- ✅ Eski ContractType enum değerleri deprecated olarak işaretlendi (Contract model'de)

## ⚠️ YAPILMASI GEREKENLER

1. **Contract Model Güncelleme:**
   - Contract model'ini yeni ContractType enum'unu kullanacak şekilde güncelle
   - PenaltyType enum'unu Contract model'ine entegre et
   - İki tarafta bağımsız şartlar ve süreler desteği ekle

2. **Mission Model Güncelleme:**
   - Mission model'ini yeni MissionType ve MissionScope enum'larını kullanacak şekilde güncelle
   - Kişisel ve klan görevleri için ayrı işlemler ekle

3. **Sistemleri Yeni Modelleri Kullanacak Şekilde Güncelle:**
   - ContractManager'ı yeni ContractType ve PenaltyType enum'larını kullanacak şekilde güncelle
   - MissionManager'ı yeni MissionType ve MissionScope enum'larını kullanacak şekilde güncelle

4. **Tarif Yönetim Sistemi:**
   - Tarif yönetim sistemi oluştur
   - Tarifleri JSON/YAML formatına taşı

## 📝 NOTLAR

1. **Battery Kategorizasyonu:** Battery isimlerinden kategori tahmin ediliyor (GhostRecipeManager'daki mantık kullanılıyor). İleride NewBatteryManager'a kategori bilgisi eklenebilir.

2. **Weapon Kategorizasyonu:** Weapon'lar zaten ItemCategory.ATTACK altında, ayrı bir kategori eklemeye gerek yok.

3. **Contract İki Taraflı Şartlar:** Kontratlar iki tarafta bağımsız şekilde şartlar ve süreler verebilecek şekilde tasarlandı, ancak henüz implement edilmedi.

4. **Mission Scope:** MissionScope enum'u oluşturuldu, ancak Mission model'ine henüz entegre edilmedi.

