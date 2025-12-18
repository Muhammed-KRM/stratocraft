# 🏗️ YAPI TARİFLERİ REHBERİ

Bu doküman, tüm yönetim yapılarının nasıl yapılacağını ve hangi menülere erişim sağladığını açıklar.

---

## 📋 İÇİNDEKİLER

1. [Kişisel Yapılar](#kişisel-yapılar)
2. [Klan Yapıları](#klan-yapıları)
3. [Genel Yapılar](#genel-yapılar)
4. [Yapı Aktivasyonu](#yapı-aktivasyonu)
5. [Menü Erişim Tablosu](#menü-erişim-tablosu)

---

## 👤 KİŞİSEL YAPILAR

Bu yapılar **her yere yapılabilir**, klan zorunlu değildir.

### 1. Kişisel Görev Loncası (PERSONAL_MISSION_GUILD)

**Açılan Menü:** Kişisel Görevler (MissionMenu)

**Tarif:**
```
[Taş] [Taş]
[Taş] [Taş]
  [Lectern]
```

**Malzemeler:**
- 1x Lectern (Okuma Masası)
- 4x Taş (Stone, Cobblestone veya Stone Bricks)

**Yapılış:**
1. 2x2 Taş bloğu yerleştir
2. Üstüne Lectern yerleştir
3. Lectern'e Shift + Sağ Tık yap

**Erişim:**
- Yapıya Sağ Tık → Kişisel Görevler menüsü açılır
- Aktif görev yoksa bilgilendirme mesajı

---

## 🏛️ KLAN YAPILARI

Bu yapılar **sadece klan bölgesinde** yapılabilir ve **klan üyeliği** gerektirir.

### 1. Klan Yönetim Merkezi (CLAN_MANAGEMENT_CENTER)

**Açılan Menüler:**
- Ana Klan Menüsü (ClanMenu)
- Üye Yönetimi (ClanMemberMenu)
- Klan İstatistikleri (ClanStatsMenu)
- Yapı Yönetimi (ClanStructureMenu)
- İttifak Yönetimi (AllianceMenu)

**Tarif:**
```
[Demir] [Demir] [Demir]
[Demir] [Demir] [Demir]
[Demir] [Demir] [Demir]
      [Beacon]
```

**Malzemeler:**
- 1x Beacon (İşaret Feneri)
- 9x Demir Bloğu (Iron Block)

**Seviye Sistemi:**
- **Seviye 1:** 6-7 Demir Bloğu
- **Seviye 2:** 8 Demir Bloğu
- **Seviye 3:** 9 Demir Bloğu

**Yapılış:**
1. 3x3 Demir Bloğu platformu yerleştir
2. Üstüne Beacon yerleştir
3. Beacon'a Shift + Sağ Tık yap

**Erişim:**
- Yapıya Sağ Tık → Ana Klan Menüsü açılır
- Ana menüden alt menülere geçiş

**Yetki:**
- Klan üyesi olmalı
- Klan bölgesinde olmalı

---

### 2. Klan Bankası (CLAN_BANK)

**Açılan Menü:** Klan Bankası (ClanBankMenu)

**Tarif:**
```
[Demir] [Demir]
[Demir] [Demir]
[Ender Chest]
```

**Malzemeler:**
- 1x Ender Chest (Ender Sandığı)
- 4x Demir Bloğu (Iron Block)

**Seviye Sistemi:**
- **Seviye 1:** 3 Demir Bloğu
- **Seviye 2:** 4 Demir Bloğu

**Yapılış:**
1. 2x2 Demir Bloğu yerleştir
2. Üstüne Ender Chest yerleştir
3. Ender Chest'e Shift + Sağ Tık yap

**Erişim:**
- Yapıya Sağ Tık → Klan Bankası menüsü açılır
- Item yatırma/çekme
- Maaş bilgileri

**Yetki:**
- Klan üyesi olmalı
- Klan bölgesinde olmalı
- Rütbe bazlı yetki (yatırma/çekme)

---

### 3. Klan Görev Loncası (CLAN_MISSION_GUILD)

**Açılan Menü:** Klan Görevleri (ClanMissionMenu)

**Tarif:**
```
[Demir] [Demir]
[Demir] [Demir]
  [Lectern]
```

**Malzemeler:**
- 1x Lectern (Okuma Masası)
- 4x Demir Bloğu (Iron Block)

**Seviye Sistemi:**
- **Seviye 1:** 3 Demir Bloğu
- **Seviye 2:** 4 Demir Bloğu

**Yapılış:**
1. 2x2 Demir Bloğu yerleştir
2. Üstüne Lectern yerleştir
3. Lectern'e Shift + Sağ Tık yap

**Erişim:**
- Yapıya Sağ Tık → Klan Görevleri menüsü açılır
- Görev oluşturma/yönetme

**Yetki:**
- Klan üyesi olmalı
- Klan bölgesinde olmalı
- General veya Lider rütbesi (görev oluşturma için)

---

### 4. Eğitim Alanı (TRAINING_ARENA)

**Açılan Menüler:**
- Eğitilmiş Canlılar (TamingMenu)
- Üreme Yönetimi (BreedingMenu)

**Tarif:**
```
[Demir] [Demir]
[Demir] [Demir]
[Enchanting Table]
```

**Malzemeler:**
- 1x Enchanting Table (Büyü Masası)
- 4x Demir Bloğu (Iron Block)

**Seviye Sistemi:**
- **Seviye 1:** 3 Demir Bloğu
- **Seviye 2:** 4 Demir Bloğu

**Yapılış:**
1. 2x2 Demir Bloğu yerleştir
2. Üstüne Enchanting Table yerleştir
3. Enchanting Table'a Shift + Sağ Tık yap

**Erişim:**
- Yapıya Sağ Tık → Eğitilmiş Canlılar menüsü açılır
- Ana menüden Üreme menüsüne geçiş

**Yetki:**
- Klan üyesi olmalı
- Klan bölgesinde olmalı

---

### 5. Kervan İstasyonu (CARAVAN_STATION)

**Açılan Menü:** Kervan Yönetimi (CaravanMenu)

**Tarif:**
```
[Demir] [Demir]
[Demir] [Demir]
  [Chest]
```

**Malzemeler:**
- 1x Chest (Sandık)
- 4x Demir Bloğu (Iron Block)

**Seviye Sistemi:**
- **Seviye 1:** 3 Demir Bloğu
- **Seviye 2:** 4 Demir Bloğu

**Yapılış:**
1. 2x2 Demir Bloğu yerleştir
2. Üstüne Chest yerleştir
3. Chest'e Shift + Sağ Tık yap

**Erişim:**
- Yapıya Sağ Tık → Kervan menüsü açılır
- Kervan oluşturma/yönetme

**Yetki:**
- Klan üyesi olmalı
- Klan bölgesinde olmalı
- General veya Lider rütbesi gerektirir

---

## 🌐 GENEL YAPILAR

Bu yapılar **her yere yapılabilir**, klan zorunlu değildir.

### 1. Kontrat Bürosu (CONTRACT_OFFICE)

**Açılan Menü:** Kontrat Menüsü (ContractMenu)

**Tarif:**
```
[Taş] [Taş]
[Taş] [Taş]
  [Anvil]
```

**Malzemeler:**
- 1x Anvil (Örs - normal, çatlak veya hasarlı olabilir)
- 4x Taş (Stone, Cobblestone veya Stone Bricks)

**Yapılış:**
1. 2x2 Taş bloğu yerleştir
2. Üstüne Anvil yerleştir
3. Anvil'e Shift + Sağ Tık yap

**Erişim:**
- Yapıya Sağ Tık → Kontrat menüsü açılır
- Tüm kontratları görüntüleme
- Kontrat oluşturma

**Yetki:**
- Herkes erişebilir
- Kontrat oluşturma için klan üyesi olmalı (klan kontratları için)

---

### 2. Market (MARKET_PLACE)

**Açılan Menü:** Market Menüsü (ShopMenu)

**Tarif:**
```
[Taş] [Taş]
[Taş] [Taş]
  [Chest]
  [Sign] (yanında veya üstünde)
```

**Malzemeler:**
- 1x Chest (Sandık)
- 1x Sign (Tabela - herhangi bir tür)
- 4x Taş (Stone, Cobblestone veya Stone Bricks)

**Yapılış:**
1. 2x2 Taş bloğu yerleştir
2. Üstüne Chest yerleştir
3. Chest'in yanına veya üstüne Sign yerleştir
4. Chest'e Shift + Sağ Tık yap

**Erişim:**
- Yapıya Sağ Tık → Market menüsü açılır
- Alışveriş ve teklif verme

**Yetki:**
- Herkes erişebilir

---

### 3. Tarif Kütüphanesi (RECIPE_LIBRARY)

**Açılan Menü:** Tarif Menüsü (RecipeMenu)

**Tarif:**
```
[Bookshelf] [Lectern] [Bookshelf]
     (yanında en az 2 Bookshelf)
```

**Malzemeler:**
- 1x Lectern (Okuma Masası)
- 2-4x Bookshelf (Kitaplık) veya Chiseled Bookshelf

**Seviye Sistemi:**
- **Seviye 1:** 2 Bookshelf
- **Seviye 2:** 4 Bookshelf

**Yapılış:**
1. Lectern yerleştir
2. Lectern'in yanına (kuzey, güney, doğu, batı) en az 2 Bookshelf yerleştir
3. Lectern'e Shift + Sağ Tık yap

**Erişim:**
- Yapıya Sağ Tık → Tarif menüsü açılır
- Tüm tarifleri görüntüleme

**Yetki:**
- Herkes erişebilir
- Tarif kitaplarına sahip olanlar detayları görebilir

---

## 🔧 YAPI AKTİVASYONU

### Aktivasyon Adımları

1. **Yapıyı Kur:** Pattern'e göre blokları yerleştir
2. **Shift + Sağ Tık:** Merkez bloğa (Lectern, Beacon, vb.) Shift + Sağ Tık yap
3. **Aktivasyon:** Sistem pattern'i kontrol eder
4. **Başarı:** Yapı aktif olur, klana eklenir (klan yapıları için)

### Önemli Notlar

- **Kişisel Yapılar:** Klan zorunlu değil, her yere yapılabilir
- **Klan Yapıları:** Klan bölgesinde olmalı, klan üyeliği gerektirir
- **Recruit Rütbesi:** Klan yapıları aktive edemez
- **Cooldown:** 5 saniye aktivasyon cooldown'u var

---

## 📊 MENÜ ERİŞİM TABLOSU

| Yapı | Tip | Menü | Erişim | Gereksinimler |
|------|-----|------|--------|---------------|
| **Kişisel Yapılar** |
| Kişisel Görev Loncası | Kişisel | MissionMenu | Sağ Tık | - |
| **Klan Yapıları** |
| Klan Yönetim Merkezi | Klan | ClanMenu + Alt Menüler | Sağ Tık | Klan üyesi, Klan bölgesi |
| Klan Bankası | Klan | ClanBankMenu | Sağ Tık | Klan üyesi, Klan bölgesi |
| Klan Görev Loncası | Klan | ClanMissionMenu | Sağ Tık | Klan üyesi, Klan bölgesi |
| Eğitim Alanı | Klan | TamingMenu + BreedingMenu | Sağ Tık | Klan üyesi, Klan bölgesi |
| Kervan İstasyonu | Klan | CaravanMenu | Sağ Tık | Klan üyesi, Klan bölgesi |
| **Genel Yapılar** |
| Kontrat Bürosu | Genel | ContractMenu | Sağ Tık | - |
| Market | Genel | ShopMenu | Sağ Tık | - |
| Tarif Kütüphanesi | Genel | RecipeMenu | Sağ Tık | - |

---

## 🎯 HIZLI TARİF ÖZETİ

### Kişisel Yapılar (Her Yere)
- **Kişisel Görev Loncası:** Lectern + 2x2 Taş
- **Kontrat Bürosu:** Anvil + 2x2 Taş
- **Market:** Chest + Sign + 2x2 Taş
- **Tarif Kütüphanesi:** Lectern + 2+ Bookshelf

### Klan Yapıları (Klan Bölgesinde)
- **Klan Yönetim Merkezi:** Beacon + 3x3 Demir Bloğu
- **Klan Bankası:** Ender Chest + 2x2 Demir Bloğu
- **Klan Görev Loncası:** Lectern + 2x2 Demir Bloğu
- **Eğitim Alanı:** Enchanting Table + 2x2 Demir Bloğu
- **Kervan İstasyonu:** Chest + 2x2 Demir Bloğu

---

## ⚠️ ÖNEMLİ NOTLAR

### Aktivasyon
- **Shift + Sağ Tık:** Yapı aktivasyonu için
- **Normal Sağ Tık:** Menü açmak için
- **Cooldown:** Aktivasyon için 5 saniye, menü için 1 saniye

### Yetki Kontrolleri
- **Kişisel Yapılar:** Herkes yapabilir, herkes kullanabilir
- **Klan Yapıları:** Sadece klan üyeleri yapabilir ve kullanabilir
- **Recruit:** Klan yapıları aktive edemez (ama kullanabilir)

### Yapı Seviyeleri
- Seviye yapıya göre değişir
- Genellikle malzeme miktarına göre belirlenir
- Yüksek seviye = daha fazla özellik (ileride eklenebilir)

---

**Hazırlayan:** AI Assistant  
**Tarih:** 2024  
**Versiyon:** 1.0













