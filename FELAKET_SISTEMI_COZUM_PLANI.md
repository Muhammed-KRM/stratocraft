# 🌋 FELAKET SİSTEMİ ÇÖZÜM PLANI

## 📋 GENEL BAKIŞ

Bu plan, felaket sistemindeki tüm hataları ve eksiklikleri düzeltmek için hazırlanmıştır.

---

## 🎯 ÖNCELİK SIRASI

### FAZE 1: KRİTİK HATALAR (Öncelik: YÜKSEK)
1. ✅ 1000 blok yarıçap klan tespiti
2. ✅ Merkeze ulaşma kontrolü
3. ✅ Merkezde 1000 blok yarıçap klan kontrolü
4. ✅ En yakın oyuncu saldırısı
5. ✅ Oyuncu saldırısı sırasında klan kontrolü
6. ✅ 3 saat kuralı

### FAZE 2: ORTA ÖNCELİKLİ (Öncelik: ORTA)
7. ⚠️ Hasar takibi
8. ⚠️ Hasar bazlı ödül dağıtımı

### FAZE 3: DÜŞÜK ÖNCELİKLİ (Öncelik: DÜŞÜK)
9. ⚠️ Admin komut tab completion
10. ⚠️ Grup felaket spawn

---

## 📝 DETAYLI ÇÖZÜM PLANI

### 1. 1000 Blok Yarıçap Klan Tespiti

**Dosya:** `DisasterManager.java`

**Mevcut Durum:**
- `findNearestCrystal()` sadece en yakın kristali buluyor
- Yarıçap kontrolü yok

**Yapılacaklar:**
1. Yeni metod: `findCrystalsInRadius(Location from, double radius)` ekle
2. Mevcut `findNearestCrystal()` metodunu güncelle veya yeni metodu kullan
3. `DisasterTask` içinde yeni metodu kullan

**Kod Değişiklikleri:**
- `DisasterManager.java`: Yeni metod ekle
- `DisasterTask.java`: Yeni metodu kullan

---

### 2. Merkeze Ulaşma Kontrolü

**Dosya:** `DisasterTask.java`

**Mevcut Durum:**
- Merkeze ulaşma kontrolü yok

**Yapılacaklar:**
1. `hasReachedCenter(Disaster disaster, Location current)` metodu ekle
2. `Disaster.java` model'ine `centerReachedTime` alanı ekle
3. `handleCreatureDisaster()` içinde merkeze ulaşma kontrolü yap

**Kod Değişiklikleri:**
- `Disaster.java`: `centerReachedTime` alanı ve getter/setter ekle
- `DisasterTask.java`: `hasReachedCenter()` metodu ekle
- `DisasterTask.java`: `handleCreatureDisaster()` güncelle

---

### 3. Merkezde 1000 Blok Yarıçap Klan Kontrolü

**Dosya:** `DisasterTask.java`

**Mevcut Durum:**
- Merkeze ulaştıktan sonra klan kontrolü yok

**Yapılacaklar:**
1. `hasClansInCenterRadius(Location center, double radius)` metodu ekle
2. `handleCreatureDisaster()` içinde merkeze ulaştıktan sonra klan kontrolü yap

**Kod Değişiklikleri:**
- `DisasterTask.java`: `hasClansInCenterRadius()` metodu ekle
- `DisasterTask.java`: `handleCreatureDisaster()` güncelle

---

### 4. En Yakın Oyuncu Saldırısı

**Dosya:** `DisasterBehavior.java`

**Mevcut Durum:**
- `attackPlayers()` tüm yakındaki oyunculara saldırıyor

**Yapılacaklar:**
1. Yeni metod: `attackNearestPlayer()` ekle
2. `DisasterTask.java` içinde yeni metodu kullan
3. `attackNearbyPlayersIfNeeded()` metodunu güncelle

**Kod Değişiklikleri:**
- `DisasterBehavior.java`: `attackNearestPlayer()` metodu ekle
- `DisasterTask.java`: `attackNearestPlayerIfNeeded()` metodu ekle
- `DisasterTask.java`: `handleCreatureDisaster()` güncelle

---

### 5. Oyuncu Saldırısı Sırasında Klan Kontrolü

**Dosya:** `DisasterTask.java`

**Mevcut Durum:**
- Oyunculara saldırırken klan kontrolü yok

**Yapılacaklar:**
1. `handleCreatureDisaster()` içinde oyuncu saldırısı sırasında klan kontrolü ekle
2. Yeni klan görünce hedefi değiştir

**Kod Değişiklikleri:**
- `DisasterTask.java`: `handleCreatureDisaster()` güncelle

---

### 6. 3 Saat Kuralı

**Dosya:** `DisasterTask.java`

**Mevcut Durum:**
- Merkeze ulaştıktan sonra 3 saatlik süre kontrolü yok

**Yapılacaklar:**
1. `Disaster.java` model'ine `centerReachedTime` alanı ekle (zaten ekleniyor)
2. `handleCreatureDisaster()` içinde 3 saat kontrolü yap
3. Süre dolunca felaketi yok et

**Kod Değişiklikleri:**
- `DisasterTask.java`: `handleCreatureDisaster()` güncelle

---

### 7. Hasar Takibi

**Dosya:** `Disaster.java`, `DisasterListener.java` (yeni)

**Mevcut Durum:**
- Hasar takibi yok

**Yapılacaklar:**
1. `Disaster.java` model'ine `playerDamage` Map'i ekle
2. Yeni listener: `DisasterListener.java` oluştur
3. `EntityDamageByEntityEvent` event'inde hasar kaydet

**Kod Değişiklikleri:**
- `Disaster.java`: `playerDamage` Map'i ve metodlar ekle
- `DisasterListener.java`: Yeni dosya oluştur
- `Main.java`: Listener'ı kaydet

---

### 8. Hasar Bazlı Ödül Dağıtımı

**Dosya:** `DisasterManager.java`

**Mevcut Durum:**
- Hasar bazlı ödül dağıtımı yok

**Yapılacaklar:**
1. `dropRewards()` metodunu güncelle
2. Hasar yüzdesine göre ödül hesapla
3. Her oyuncuya ödül ver

**Kod Değişiklikleri:**
- `DisasterManager.java`: `dropRewards()` güncelle

---

### 9. Admin Komut Tab Completion

**Dosya:** `AdminCommandExecutor.java`

**Mevcut Durum:**
- Tab completion mantığı karışık

**Yapılacaklar:**
1. `onTabComplete()` metodunu düzelt
2. Argüman sayısına göre doğru önerileri göster

**Kod Değişiklikleri:**
- `AdminCommandExecutor.java`: `onTabComplete()` güncelle

---

### 10. Grup Felaket Spawn

**Dosya:** `DisasterManager.java`

**Mevcut Durum:**
- Bazı felaket tipleri spawn edilemiyor

**Yapılacaklar:**
1. `triggerDisaster()` içinde grup felaket kontrolü ekle
2. Grup felaketler için `spawnGroupDisaster()` veya `spawnSwarmDisaster()` çağır

**Kod Değişiklikleri:**
- `DisasterManager.java`: `triggerDisaster()` güncelle

---

## 🔧 UYGULAMA SIRASI

1. **Disaster.java** - Model güncellemeleri (centerReachedTime, playerDamage)
2. **DisasterManager.java** - findCrystalsInRadius() metodu
3. **DisasterBehavior.java** - attackNearestPlayer() metodu
4. **DisasterTask.java** - Tüm mantık güncellemeleri
5. **DisasterListener.java** - Yeni listener (hasar takibi)
6. **DisasterManager.java** - dropRewards() güncelleme
7. **AdminCommandExecutor.java** - Tab completion düzeltme
8. **DisasterManager.java** - Grup felaket spawn düzeltme

---

## ✅ TEST EDİLMESİ GEREKENLER

1. ✅ 1000 blok yarıçapında birden fazla klan varsa hepsini buluyor mu?
2. ✅ Merkeze ulaştığında doğru mesaj gösteriliyor mu?
3. ✅ Merkeze ulaştıktan sonra 1000 blok yarıçapında klan kontrolü yapılıyor mu?
4. ✅ En yakın oyuncuya saldırı yapılıyor mu?
5. ✅ Oyunculara saldırırken yeni klan görünce ona yöneliyor mu?
6. ✅ 3 saat geçince felaket yok oluyor mu?
7. ✅ Hasar takibi çalışıyor mu?
8. ✅ Hasar bazlı ödül dağıtımı çalışıyor mu?
9. ✅ Tab completion düzgün çalışıyor mu?
10. ✅ Grup felaketler spawn ediliyor mu?

---

## 📊 İLERLEME TAKİBİ

- [ ] 1. 1000 blok yarıçap klan tespiti
- [ ] 2. Merkeze ulaşma kontrolü
- [ ] 3. Merkezde 1000 blok yarıçap klan kontrolü
- [ ] 4. En yakın oyuncu saldırısı
- [ ] 5. Oyuncu saldırısı sırasında klan kontrolü
- [ ] 6. 3 saat kuralı
- [ ] 7. Hasar takibi
- [ ] 8. Hasar bazlı ödül dağıtımı
- [ ] 9. Admin komut tab completion
- [ ] 10. Grup felaket spawn

