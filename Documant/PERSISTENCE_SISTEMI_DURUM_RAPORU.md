# PERSISTENCE SİSTEMİ DURUM RAPORU

## ✅ ŞUAN KAYDEDİLEN SİSTEMLER

1. **Klan Sistemi (Clans)**
   - Klan bilgileri (isim, üyeler, rütbeler)
   - Territory (alan, merkez, radius, outposts)
   - Structures (yapılar, seviyeler, shield fuel)
   - Bank balance ve stored XP
   - Guests (misafirler)
   - Crystal location

2. **Kontratlar (Contracts)**
   - Tüm aktif kontratlar
   - Issuer, acceptor, material, amount, reward
   - Deadline ve delivery durumu

3. **Alışveriş (Shops)**
   - Shop bilgileri (owner, location, items)
   - Offers (teklifler)
   - Shop ayarları

4. **Virtual Inventories**
   - Klan sanal envanterleri

5. **İttifaklar (Alliances)**
   - İttifak bilgileri
   - Type, duration, active status
   - Broken status ve breaker

6. **Felaketler (Disasters)**
   - Aktif felaket durumu
   - Type, category, level, start time, duration

7. **Güç Profilleri (Power Profiles)**
   - Oyuncu güç profilleri (StratocraftPowerSystem)
   - Gear power, training power, buff power, ritual power

8. **Tuzaklar (Traps)**
   - TrapManager.saveTraps() ile kaydediliyor
   - Ama DataManager entegrasyonu yok

9. **Mayınlar (Mines)**
   - NewMineManager otomatik kaydediyor
   - DataManager entegrasyonu yok

---

## ✅ YENİ EKLENEN KAYIT SİSTEMLERİ

### 1. Klan Bank Sistemi (ClanBankSystem) ✅
**Kaydedilen Veriler:**
- ✅ `lastSalaryTime` (üye -> son maaş zamanı)
- ✅ `transferContracts` (klan -> transfer kontratları listesi)
- ✅ `bankChestLocations` (klan -> banka sandığı konumu)

**Durum:** ✅ TAMAMLANDI - DataManager entegrasyonu yapıldı

### 2. Klan Görev Sistemi (ClanMissionSystem) ✅
**Kaydedilen Veriler:**
- ✅ Görev tahtası konumları (missionBoardLocations)
- ⚠️ Aktif görevler (runtime'da oluşturulabilir, konumlar yeterli)

**Durum:** ✅ TAMAMLANDI - DataManager entegrasyonu yapıldı

### 3. Klan Aktivite Sistemi (ClanActivitySystem) ✅
**Kaydedilen Veriler:**
- ✅ Üye aktivite verileri (player -> last online time)

**Durum:** ✅ TAMAMLANDI - DataManager entegrasyonu yapıldı

---

## ❌ HENÜZ EKSİK KAYIT SİSTEMLERİ

### 4. Tuzaklar (TrapManager)
**Durum:** ✅ Kaydediliyor ama DataManager entegrasyonu yok
**Sorun:** TrapManager kendi dosyasına kaydediyor, DataManager ile entegre değil

**Öncelik:** 🟡 ORTA (Yapılar için önemli)

### 5. Mayınlar (NewMineManager)
**Durum:** ✅ Otomatik kaydediliyor
**Sorun:** DataManager entegrasyonu yok

**Öncelik:** 🟢 DÜŞÜK (Zaten kaydediliyor)

### 6. Boss Kill History
**Eksik Veriler:**
- Kesilen bosslar (player, boss type, time, location)
- Boss loot history

**Öncelik:** 🟡 ORTA (İstatistik için)

### 7. Siege History
**Eksik Veriler:**
- Savaş geçmişi (attacker, defender, result, time)
- Savaş istatistikleri

**Öncelik:** 🟡 ORTA (İstatistik için)

### 8. Player Buffs
**Eksik Veriler:**
- Aktif bufflar (player -> buff listesi)
- Buff süreleri ve kaynakları

**Öncelik:** 🟢 DÜŞÜK (Geçici veriler)

### 9. Territory Changes History
**Eksik Veriler:**
- Alan değişiklikleri (expand, reset)
- Değişiklik zamanları ve nedenleri

**Öncelik:** 🟢 DÜŞÜK (İstatistik için)

### 10. Structure Changes History
**Eksik Veriler:**
- Yapı değişiklikleri (create, upgrade, destroy)
- Değişiklik zamanları

**Öncelik:** 🟢 DÜŞÜK (İstatistik için)

---

## 📋 ÖNCELİK SIRASI

1. **🔴 YÜKSEK ÖNCELİK:**
   - Clan Bank System (salaries, transfer contracts)
   - Clan Mission System

2. **🟡 ORTA ÖNCELİK:**
   - Clan Activity System
   - TrapManager DataManager entegrasyonu
   - Boss Kill History
   - Siege History

3. **🟢 DÜŞÜK ÖNCELİK:**
   - Player Buffs (geçici veriler)
   - Territory Changes History
   - Structure Changes History

---

## 🔧 YAPILACAKLAR

1. DataManager'a yeni snapshot sınıfları ekle:
   - `ClanBankSnapshot`
   - `ClanMissionSnapshot`
   - `ClanActivitySnapshot`

2. DataManager.saveAll() metodunu genişlet:
   - Yeni parametreler ekle (ClanBankSystem, ClanMissionSystem, ClanActivitySystem)
   - Snapshot oluşturma metodları ekle
   - Write metodları ekle

3. DataManager.loadAll() metodunu genişlet:
   - Yeni load metodları ekle
   - Sistemlere veri yükleme metodları ekle

4. Main.java'da entegrasyon:
   - onEnable'da yeni sistemleri yükle
   - onDisable'da yeni sistemleri kaydet

5. Sistemlere load metodları ekle:
   - ClanBankSystem.loadData()
   - ClanMissionSystem.loadData()
   - ClanActivitySystem.loadData()

---

## 📝 NOTLAR

- Tuzaklar ve mayınlar zaten kendi dosyalarına kaydediliyor, DataManager entegrasyonu opsiyonel
- Player buffs geçici veriler, sunucu restart'ta kaybolması normal
- History verileri istatistik için, kritik değil
- En kritik eksikler: Clan Bank ve Clan Mission sistemleri

