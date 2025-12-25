# SAVAŞ TOTEMİ VE SİSTEM KONTROL RAPORU

## 📋 MEVCUT DURUM ANALİZİ

### ✅ 1. Savaş Totemi Sistemi (Mevcut)

**Dosya:** `src/main/java/me/mami/stratocraft/listener/SiegeListener.java`

**Mevcut Çalışma Şekli:**
- ✅ Totem yapısı kontrolü var: 2x2 (GOLD_BLOCK alt, IRON_BLOCK üst)
- ✅ `BlockPlaceEvent` ile tetikleniyor (blok yerleştirilince)
- ✅ 50 blok yakınına alan kontrolü var
- ❌ Yapı çekirdeği sistemi kullanılmıyor
- ❌ Klan dışına yapılabilen yapılar kategorisinde değil
- ❌ Bir kere aktif edildikten sonra işlevini kaybetmiyor

**Sorunlar:**
1. ❌ Yapı çekirdeği sistemi kullanılmıyor
2. ❌ Klan dışına yapılabilen yapılar kategorisinde değil
3. ❌ Bir kere aktif edildikten sonra işlevini kaybetmiyor
4. ❌ Herkesin kullanabildiği yapılar olarak yapılmamış

---

### ✅ 2. Savaş Listesi Güncellemeleri

**Dosya:** `src/main/java/me/mami/stratocraft/manager/SiegeManager.java`

**Mevcut Durum:**
- ✅ `startSiege()` metodunda her iki klan da savaş listesine ekleniyor:
  ```java
  activeWars.computeIfAbsent(attackerId, k -> new HashSet<>()).add(defenderId);
  attacker.addWarringClan(defenderId);
  activeWars.computeIfAbsent(defenderId, k -> new HashSet<>()).add(attackerId);
  defender.addWarringClan(attackerId);
  ```

**Kontrol Edilmesi Gerekenler:**
- ✅ Birden fazla klanla savaşa girme desteği var (Set kullanılıyor)
- ⚠️ Potansiyel sorun: Thread-safe kontrolü gerekli
- ⚠️ Potansiyel sorun: Aynı klanla iki kez savaş açılabilir mi? (Kontrol var: `isAtWarWith`)

---

### ✅ 3. Savaş Bitirme Kontrolleri

**Dosya:** `src/main/java/me/mami/stratocraft/manager/SiegeManager.java`

**Mevcut Durum:**

#### 3.1. Kristal Kırılınca Savaş Bitirme
- ✅ `endSiege(winner, loser)` metodu var
- ✅ `endWar(winner, loser)` çağrılıyor
- ✅ `TerritoryListener.onCrystalBreak()` içinde `endSiege()` çağrılıyor

**Kontrol:**
```java
// TerritoryListener.java - satır 1849
siegeManager.endSiege(attacker, owner);
```

#### 3.2. Pes Etme ile Savaş Bitirme
- ✅ `surrender(surrenderingClan, targetClanId, clanManager)` metodu var
- ✅ `endWar(surrenderingClan, attacker)` çağrılıyor

**Kontrol:**
```java
// SiegeManager.java - satır 254
endWar(surrenderingClan, attacker);
```

#### 3.3. endWar() Metodu
- ✅ Her iki klanın savaş listesinden kaldırılıyor:
  ```java
  clan1.removeWarringClan(clan2Id);
  clan2.removeWarringClan(clan1Id);
  ```
- ✅ `activeWars` Map'inden de kaldırılıyor

**Sorunlar:**
- ⚠️ Potansiyel sorun: Klan dağıtılınca (`disbandClan`) savaş listesi temizleniyor mu?
- ⚠️ Potansiyel sorun: Tersi durum kontrol ediliyor mu? (Her iki taraftan da siliniyor mu?)

---

### ✅ 4. İttifak Sistemi Kontrolleri

**Dosya:** `src/main/java/me/mami/stratocraft/manager/AllianceManager.java`

**Mevcut Durum:**

#### 4.1. İttifak Oluşturma
- ✅ `createAlliance()` metodu var
- ✅ `hasAlliance()` kontrolü var
- ❌ İttifak isteği gönderme sistemi yok (sadece admin komutu var)
- ❌ Karşı taraf onaylama sistemi yok

#### 4.2. İttifak Bitirme
- ✅ `breakAlliance()` metodu var (ihlal durumunda)
- ✅ `dissolveAlliance()` metodu var (karşılıklı)
- ❌ İttifak listesinden silme kontrolü eksik (Clan model'de `allianceClans` Set'i var ama güncelleniyor mu?)

#### 4.3. İttifak Kontrolü
- ✅ `hasAlliance()` metodu var
- ✅ `getAlliances()` metodu var
- ⚠️ Potansiyel sorun: `Clan.allianceClans` Set'i güncelleniyor mu?

---

### ❌ 5. İttifak Varken Savaş Açma Sorunu

**Dosya:** `src/main/java/me/mami/stratocraft/manager/SiegeManager.java`

**Mevcut Durum:**
- ❌ `startSiege()` metodunda ittifak kontrolü yok
- ❌ İttifak varken savaş açılırsa ittifak silinmiyor
- ❌ İttifak listesinden o klan silinmiyor
- ❌ Savaş listesine ekleniyor ama ittifak listesinden silinmiyor

**Sorun:**
```java
// startSiege() metodunda ittifak kontrolü yok!
// İttifak varken savaş açılırsa:
// 1. İttifak silinmeli
// 2. allianceClans Set'inden kaldırılmalı
// 3. Savaş listesine eklenmeli
```

---

## 🔍 TESPİT EDİLEN SORUNLAR

### ❌ Sorun 1: Savaş Totemi Yapı Çekirdeği Sistemi Kullanmıyor

**Açıklama:**
- Şu an `BlockPlaceEvent` ile tetikleniyor
- Yapı çekirdeği sistemi kullanılmıyor
- Klan dışına yapılabilen yapılar kategorisinde değil

**Çözüm:**
- Yapı çekirdeği sistemi kullanılmalı
- `StructureActivationListener` içine eklenecek
- `StructureType.WAR_TOTEM` enum'u eklenecek
- `StructureOwnershipType.PUBLIC` olarak işaretlenecek

---

### ❌ Sorun 2: Savaş Totemi Bir Kere Aktif Edildikten Sonra İşlevini Kaybetmiyor

**Açıklama:**
- Şu an totem yapısı her yerleştirildiğinde kontrol ediliyor
- Bir kere aktif edildikten sonra işlevini kaybetmiyor

**Çözüm:**
- Yapı çekirdeği aktif edildikten sonra yapıyı "kullanılmış" olarak işaretle
- Bir kere aktif edildikten sonra tekrar kullanılamaz

---

### ❌ Sorun 3: İttifak Varken Savaş Açma Sorunu

**Açıklama:**
- `startSiege()` metodunda ittifak kontrolü yok
- İttifak varken savaş açılırsa ittifak silinmiyor
- `allianceClans` Set'inden kaldırılmıyor

**Çözüm:**
- `startSiege()` metodunda ittifak kontrolü ekle
- İttifak varsa:
  1. İttifakı sil (`breakAlliance()` veya `dissolveAlliance()`)
  2. `allianceClans` Set'inden kaldır
  3. Savaş listesine ekle

---

### ⚠️ Sorun 4: Klan Dağıtılınca Savaş Listesi Temizleniyor mu?

**Açıklama:**
- Klan dağıtılınca (`disbandClan`) savaş listesi temizleniyor mu?
- Diğer klanların savaş listesinden bu klan kaldırılıyor mu?

**Kontrol:**
- `Clan.disbandClan()` veya `ClanManager.disbandClan()` metodunu kontrol et
- Savaş listesi temizleniyor mu?

---

### ⚠️ Sorun 5: İttifak Listesi Güncellemeleri

**Açıklama:**
- `Clan.allianceClans` Set'i güncelleniyor mu?
- İttifak oluşturulunca `allianceClans` Set'ine ekleniyor mu?
- İttifak bitince `allianceClans` Set'inden kaldırılıyor mu?

**Kontrol:**
- `AllianceManager.createAlliance()` metodunda `allianceClans` Set'ine ekleniyor mu?
- `AllianceManager.breakAlliance()` metodunda `allianceClans` Set'inden kaldırılıyor mu?

---

## 📝 YAPILACAKLAR

### 1. Savaş Totemi Yapı Çekirdeği Sistemine Dönüştür

**Adımlar:**
1. `StructureType` enum'una `WAR_TOTEM` ekle
2. `StructureOwnershipHelper` içine `WAR_TOTEM` için `PUBLIC` ekle
3. `StructureActivationListener` içine `WAR_TOTEM` pattern kontrolü ekle
4. Pattern: Demir ve altın blok ile yapılacak (yapımı zor olmasın)
5. Aktivasyon sırasında savaş başlat
6. Bir kere aktif edildikten sonra işlevini kaybet

---

### 2. İttifak Varken Savaş Açma Sorununu Düzelt

**Adımlar:**
1. `startSiege()` metodunda ittifak kontrolü ekle
2. İttifak varsa:
   - İttifakı sil (`breakAlliance()` veya `dissolveAlliance()`)
   - `allianceClans` Set'inden kaldır
   - Savaş listesine ekle

---

### 3. Savaş Listesi Güncellemelerini Kontrol Et

**Adımlar:**
1. `endWar()` metodunu kontrol et (her iki taraftan da siliniyor mu?)
2. `disbandClan()` metodunu kontrol et (savaş listesi temizleniyor mu?)
3. Thread-safe kontrolü yap

---

### 4. İttifak Listesi Güncellemelerini Kontrol Et

**Adımlar:**
1. `createAlliance()` metodunda `allianceClans` Set'ine ekleniyor mu?
2. `breakAlliance()` metodunda `allianceClans` Set'inden kaldırılıyor mu?
3. `dissolveAlliance()` metodunda `allianceClans` Set'inden kaldırılıyor mu?

---

## 🎯 SONUÇ

**Tespit Edilen Sorunlar:**
1. ❌ Savaş totemi yapı çekirdeği sistemi kullanmıyor
2. ❌ Savaş totemi bir kere aktif edildikten sonra işlevini kaybetmiyor
3. ❌ İttifak varken savaş açılırsa ittifak silinmiyor
4. ⚠️ Klan dağıtılınca savaş listesi temizleniyor mu? (Kontrol gerekli)
5. ⚠️ İttifak listesi güncellemeleri (Kontrol gerekli)

**Öncelik:**
1. **YÜKSEK**: İttifak varken savaş açma sorunu (mantık hatası)
2. **YÜKSEK**: Savaş totemi yapı çekirdeği sistemine dönüştürme
3. **ORTA**: Savaş listesi güncellemeleri kontrolü
4. **ORTA**: İttifak listesi güncellemeleri kontrolü

