# 🔧 CONFIG DÖNÜŞÜMÜ ÖNCELİK PLANI

Bu doküman, tüm sistemlerin config'den kontrol edilebilir hale getirilmesi için öncelik sırasını ve planı içerir.

---

## 📊 ÖNCELİK SIRASI

### 🔴 KRİTİK ÖNCELİK (1. Faz - Hemen Başla)

Bu sistemler oyun dengesini doğrudan etkiler ve sık sık değiştirilmesi gerekir:

1. **SiegeManager** (Kuşatma Sistemi)
   - Warmup süresi ✅ (zaten var)
   - Loot yüzdesi ✅ (zaten var)
   - ⚠️ Eksik: Saldırı hasarları, savunma güçleri, süreler, ödüller

2. **BuffManager** (Buff Sistemi)
   - Conqueror buff süresi ✅ (zaten var)
   - Hero buff süresi ✅ (zaten var)
   - ⚠️ Eksik: Buff güçleri, çarpanlar, efekt seviyeleri

3. **ContractManager** (Kontrat Sistemi)
   - ⚠️ Eksik: Ödüller, süreler, zorluk çarpanları, başarı kriterleri

4. **ShopManager** (Market Sistemi)
   - ⚠️ Eksik: Fiyatlar, ödüller, indirim oranları, stok limitleri

5. **MissionManager** (Görev Sistemi)
   - ⚠️ Eksik: Ödüller, süreler, zorluk seviyeleri, başarı kriterleri

---

### 🟠 YÜKSEK ÖNCELİK (2. Faz)

Bu sistemler savaş ve hasar sistemlerini etkiler:

6. **MobManager** (Mob Sistemi)
   - ⚠️ Eksik: Spawn oranları, güçler, ödüller, özel yetenekler

7. **BossManager** (Boss Sistemi)
   - ⚠️ Eksik: Güçler, ödüller, spawn süreleri, özel yetenekler

8. **TrapManager** (Tuzak Sistemi)
   - ⚠️ Eksik: Hasar değerleri, süreler, etki alanları, cooldown'lar

9. **NewMineManager** (Mayın Sistemi)
   - ⚠️ Eksik: Hasar değerleri, süreler, etki alanları, patlama güçleri

---

### 🟡 ORTA ÖNCELİK (3. Faz)

Bu sistemler ekonomi ve üreme sistemlerini etkiler:

10. **SupplyDropManager** (Supply Drop)
    - ⚠️ Eksik: Ödüller, spawn süreleri, mesafeler, drop şansları

11. **CaravanManager** (Kervan Sistemi)
    - ⚠️ Eksik: Ödüller, süreler, mesafeler, saldırı şansları

12. **TamingManager** (Eğitme Sistemi)
    - ⚠️ Eksik: Süreler, güçler, başarı şansları, seviye artışları

13. **BreedingManager** (Üreme Sistemi)
    - ⚠️ Eksik: Süreler, şanslar, yavru özellikleri, cooldown'lar

---

### 🟢 DÜŞÜK ÖNCELİK (4. Faz)

Bu sistemler nadiren değiştirilir ama yine de config'de olmalı:

14. **TerritoryManager** (Bölge Sistemi)
    - ⚠️ Eksik: Mesafeler, süreler, koruma güçleri, genişleme limitleri

15. **ResearchManager** (Araştırma Sistemi)
    - ⚠️ Eksik: Maliyetler, süreler, unlock kriterleri

16. **SpecialItemManager** (Özel Eşya)
    - ⚠️ Eksik: Güçler, süreler, cooldown'lar, özel yetenekler

17. **SpecialWeaponListener** (Özel Silah)
    - ⚠️ Eksik: Hasar değerleri, süreler, cooldown'lar, özel yetenekler

18. **SpecialArmorListener** (Özel Zırh)
    - ⚠️ Eksik: Savunma değerleri, süreler, set bonusları

---

## 📋 UYGULAMA PLANI

### Faz 1: Kritik Sistemler (1-2 Hafta)

1. **SiegeManager** - Eksik değerleri ekle
2. **BuffManager** - Eksik değerleri ekle
3. **ContractManager** - Tüm değerleri config'e taşı
4. **ShopManager** - Tüm değerleri config'e taşı
5. **MissionManager** - Tüm değerleri config'e taşı

### Faz 2: Savaş Sistemleri (1-2 Hafta)

6. **MobManager** - Tüm değerleri config'e taşı
7. **BossManager** - Tüm değerleri config'e taşı
8. **TrapManager** - Tüm değerleri config'e taşı
9. **NewMineManager** - Tüm değerleri config'e taşı

### Faz 3: Ekonomi ve Üreme (1 Hafta)

10. **SupplyDropManager** - Tüm değerleri config'e taşı
11. **CaravanManager** - Tüm değerleri config'e taşı
12. **TamingManager** - Tüm değerleri config'e taşı
13. **BreedingManager** - Tüm değerleri config'e taşı

### Faz 4: Diğer Sistemler (1 Hafta)

14. **TerritoryManager** - Tüm değerleri config'e taşı
15. **ResearchManager** - Tüm değerleri config'e taşı
16. **SpecialItemManager** - Tüm değerleri config'e taşı
17. **SpecialWeaponListener** - Tüm değerleri config'e taşı
18. **SpecialArmorListener** - Tüm değerleri config'e taşı

---

## 🎯 HER SİSTEM İÇİN YAPILACAKLAR

Her sistem için şu adımlar izlenecek:

1. **Hardcoded değerleri bul** - Kod içinde sabit değerleri tespit et
2. **GameBalanceConfig'e ekle** - Yeni config sınıfı oluştur veya mevcut olana ekle
3. **Config.yml'e ekle** - Varsayılan değerleri config.yml'e ekle
4. **Manager'da kullan** - Hardcoded değerler yerine config'den oku
5. **Dokümantasyon** - 23_config_degerleri.md'ye ekle

---

## 📝 NOTLAR

- Tüm değerler opsiyonel olmalı (varsayılan değerlerle çalışmalı)
- Performans kritik değerler için uyarılar eklenmeli
- Her değer için açıklama ve önerilen aralık belirtilmeli
- Config değişiklikleri reload ile çalışmalı (mümkünse)

---

**Son Güncelleme:** Bu plan, sistemlerin öncelik sırasına göre güncellenecektir.

