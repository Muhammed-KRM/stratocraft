# GÜÇ VE KORUMA SİSTEMİ ANALİZ RAPORU

## 📋 MEVCUT DURUM ANALİZİ

### ✅ ÇALIŞAN SİSTEMLER

#### 1. Güç Hesaplama Sistemleri
- ✅ **Oyuncu Gücü Hesaplama**: `StratocraftPowerSystem.calculatePlayerProfile()` çalışıyor
- ✅ **Klan Gücü Hesaplama**: `StratocraftPowerSystem.calculateClanProfile()` çalışıyor
- ✅ **Seviye Hesaplama**: `ClanPowerConfig.calculatePlayerLevel()` ve `calculateClanLevel()` çalışıyor
- ✅ **Cache Sistemi**: Performans için cache mekanizması var

#### 2. Klan Savaş Sistemi
- ✅ **Çoklu Savaş Desteği**: `Clan.warringClans` Set ile birden fazla klanla savaş
- ✅ **İki Taraflı Savaş**: `SiegeManager.startSiege()` her iki klanı da savaşta işaretliyor
- ✅ **Territory Koruması Kalkıyor**: `TerritoryListener` savaş durumunda korumaları kaldırıyor
- ✅ **Savaş Kontrolü**: `Clan.isAtWarWith()` metodu var

#### 3. Mevcut Koruma Sistemleri
- ✅ **ClanProtectionSystem**: `canAttackPlayer()` metodu var
- ✅ **Güç Bazlı Koruma**: %40 eşik kontrolü var
- ✅ **Seviye Bazlı Koruma**: 5 seviye farkı kontrolü var
- ✅ **Acemi Koruması**: 3,000 güç + Seviye 5 altı kontrolü var
- ✅ **Klan Savaşı İstisnası**: Savaşta korumalar kalkıyor

---

## ❌ EKSİK ÖZELLİKLER

### 1. Oyuncu Koruma Sistemi (3 Seviye Farkı - %95 Hasar Azaltma)

**İstenen:**
- Kendinden 3 seviye aşağıdaki birine vurursa %95 hasar azaltma
- Savaş durumunda bu koruma kalkar

**Mevcut Durum:**
- ❌ 3 seviye farkı kontrolü yok (sadece 5 seviye var)
- ❌ %95 hasar azaltma yok
- ❌ Savaş durumunda koruma kalkma kontrolü yok

**Gerekli:**
- `EntityDamageByEntityEvent` listener'ında hasar azaltma
- Seviye farkı kontrolü (3 seviye)
- Savaş durumu kontrolü

---

### 2. Klan Koruma Sistemi (3 Seviye Farkı)

**İstenen:**
- Kendisinden 3 seviye aşağıdaki bir klana savaş açamaz
- İstisna: 50 blok yakınına başka bir klanın alanı geliyorsa otomatik savaş başlar
- Kendinden 3 seviye altı bir klanın 50 blok yakınına klan kurulamaz
- Tersi: Kendinden 3 seviye üst bir klanın yanına klan kurulabilir ama otomatik savaş başlar

**Mevcut Durum:**
- ❌ 3 seviye farkı kontrolü yok
- ❌ 50 blok yakınına alan kontrolü yok
- ❌ Otomatik savaş başlatma yok
- ❌ Klan kurma sırasında seviye kontrolü yok

**Gerekli:**
- `SiegeManager.startSiege()` metodunda seviye kontrolü
- `TerritoryListener` klan kurma sırasında seviye kontrolü
- 50 blok yakınına alan kontrolü
- Otomatik savaş başlatma mekanizması

---

### 3. Savaş Sistemi Kontrolleri

**İstenen:**
- 50 blok yakınına totem yapıp savaş başlatma
- En az %35'i aktif ve bir general aktif olmalı koşulu
- Sadece general başlatabilir koşulu

**Mevcut Durum:**
- ❌ Totem yapısı kontrolü yok (dökümanlarda var ama kodda yok)
- ❌ %35 aktif üye kontrolü yok
- ❌ General aktif kontrolü yok
- ❌ General yetki kontrolü yok

**Gerekli:**
- Totem yapısı tespiti (2x2: IRON_BLOCK üst, GOLD_BLOCK alt)
- Aktif üye yüzdesi kontrolü
- General aktif kontrolü
- General yetki kontrolü

---

## 🎯 İSTENEN ÖZELLİKLER DETAYLI LİSTESİ

### 1. Oyuncu Koruma Sistemi

#### Özellik 1.1: 3 Seviye Farkı Kontrolü
- **Açıklama**: Saldıran oyuncunun seviyesi, hedef oyuncunun seviyesinden 3 veya daha fazla yüksekse koruma aktif
- **Formül**: `attackerLevel >= targetLevel + 3`
- **Hasar Azaltma**: %95 (0.05 çarpanı)
- **İstisna**: Savaş durumunda koruma kalkar

#### Özellik 1.2: Hasar Azaltma Uygulama
- **Event**: `EntityDamageByEntityEvent`
- **Kontrol Sırası**:
  1. Savaş durumu kontrolü (en yüksek öncelik)
  2. Seviye farkı kontrolü (3 seviye)
  3. Hasar azaltma uygulama (%95)
  4. Mesaj gönderme

#### Özellik 1.3: Savaş Durumu İstisnası
- **Kontrol**: `Clan.isAtWarWith()` metodu kullanılacak
- **Açıklama**: Eğer saldıran ve hedef farklı klanlardaysa ve bu klanlar savaştaysa koruma kalkar

---

### 2. Klan Koruma Sistemi

#### Özellik 2.1: Savaş Açma Koruması (3 Seviye Farkı)
- **Açıklama**: Saldıran klanın seviyesi, savunan klanın seviyesinden 3 veya daha fazla yüksekse savaş açılamaz
- **Formül**: `attackerClanLevel >= defenderClanLevel + 3`
- **Mesaj**: "§cKendinden 3 seviye aşağıdaki bir klana savaş açamazsın!"

#### Özellik 2.2: 50 Blok Yakınına Alan Kontrolü (Otomatik Savaş)
- **Açıklama**: Eğer bir klanın 50 blok yakınına başka bir klanın alanı geliyorsa otomatik savaş başlar
- **Kontrol**: `TerritoryManager.getTerritoryOwner()` ile 50 blok yarıçapında kontrol
- **Otomatik Savaş**: `SiegeManager.startSiege()` otomatik çağrılır
- **Mesaj**: "§c§lOTOMATİK SAVAŞ! §e" + attacker.getName() + " ve " + defender.getName() + " klanları 50 blok yakınında!"

#### Özellik 2.3: Klan Kurma Koruması (3 Seviye Farkı)
- **Açıklama**: Kendinden 3 seviye altı bir klanın 50 blok yakınına klan kurulamaz
- **Kontrol**: Klan kurma sırasında (`TerritoryListener.onCrystalPlace()`)
- **Formül**: `newClanLevel < nearbyClanLevel - 3` → Kurulamaz
- **Mesaj**: "§cKendinden 3 seviye altı bir klanın 50 blok yakınına klan kuramazsın!"

#### Özellik 2.4: Tersi Senaryo (3 Seviye Üst Klan)
- **Açıklama**: Kendinden 3 seviye üst bir klanın yanına klan kurulabilir ama otomatik savaş başlar
- **Kontrol**: Klan kurma sırasında
- **Formül**: `newClanLevel < nearbyClanLevel - 3` → Kurulabilir ama savaş başlar
- **Otomatik Savaş**: `SiegeManager.startSiege()` otomatik çağrılır

---

### 3. Savaş Sistemi Kontrolleri

#### Özellik 3.1: Totem Yapısı Tespiti
- **Yapı**: 2x2
  - Üst katman (Y: +1): 2x IRON_BLOCK
  - Alt katman (Y: 0): 2x GOLD_BLOCK
- **Kontrol**: `BlockPlaceEvent` veya `BlockBreakEvent` listener'ında
- **Konum**: Düşman klanın 50 blok yakınında olmalı
- **Sonuç**: Totem yapısı tamamlandığında savaş başlar

#### Özellik 3.2: %35 Aktif Üye Kontrolü
- **Açıklama**: Klanın toplam üye sayısının en az %35'i online olmalı
- **Formül**: `onlineMembers >= totalMembers * 0.35`
- **Kontrol**: `SiegeManager.startSiege()` metodunda
- **Mesaj**: "§cSavaş başlatmak için klandan en az %35'i aktif olmalı!"

#### Özellik 3.3: General Aktif Kontrolü
- **Açıklama**: En az bir General (veya Lider) online olmalı
- **Kontrol**: `SiegeManager.startSiege()` metodunda
- **Mesaj**: "§cSavaş başlatmak için en az bir General aktif olmalı!"

#### Özellik 3.4: General Yetki Kontrolü
- **Açıklama**: Sadece General veya Lider savaş başlatabilir
- **Kontrol**: `SiegeManager.startSiege()` metodunda
- **Rütbe Kontrolü**: `Clan.getRank(playerId)` → GENERAL veya LEADER olmalı
- **Mesaj**: "§cSadece General veya Lider savaş başlatabilir!"

---

## 🔍 MEVCUT KOD İNCELEMESİ

### ClanProtectionSystem.java
- ✅ `canAttackPlayer()` metodu var
- ✅ Seviye kontrolü var (5 seviye farkı)
- ❌ 3 seviye farkı kontrolü yok
- ❌ %95 hasar azaltma yok
- ❌ Savaş durumu kontrolü var ama hasar azaltma yok

### SiegeManager.java
- ✅ `startSiege()` metodu var
- ✅ Online üye kontrolü var (en az 1 kişi)
- ❌ %35 aktif üye kontrolü yok
- ❌ General aktif kontrolü yok
- ❌ General yetki kontrolü yok
- ❌ 3 seviye farkı kontrolü yok
- ❌ Totem yapısı kontrolü yok

### TerritoryListener.java
- ✅ Klan kurma kontrolü var (`onCrystalPlace()`)
- ✅ Savaş durumunda territory koruması kalkıyor
- ❌ Klan kurma sırasında seviye kontrolü yok
- ❌ 50 blok yakınına alan kontrolü yok
- ❌ Otomatik savaş başlatma yok

---

## 📝 YAPILACAKLAR LİSTESİ

### 1. Oyuncu Koruma Sistemi
- [ ] `PlayerProtectionSystem` sınıfı oluştur
- [ ] `EntityDamageByEntityEvent` listener ekle
- [ ] 3 seviye farkı kontrolü ekle
- [ ] %95 hasar azaltma uygula
- [ ] Savaş durumu kontrolü ekle
- [ ] Mesaj sistemi ekle

### 2. Klan Koruma Sistemi
- [ ] `SiegeManager.startSiege()` metoduna seviye kontrolü ekle
- [ ] `TerritoryListener.onCrystalPlace()` metoduna seviye kontrolü ekle
- [ ] 50 blok yakınına alan kontrolü ekle
- [ ] Otomatik savaş başlatma mekanizması ekle
- [ ] Mesaj sistemi ekle

### 3. Savaş Sistemi Kontrolleri
- [ ] Totem yapısı tespiti ekle (`BlockPlaceEvent` listener)
- [ ] %35 aktif üye kontrolü ekle
- [ ] General aktif kontrolü ekle
- [ ] General yetki kontrolü ekle
- [ ] Mesaj sistemi ekle

---

## 🎯 SONUÇ

**Mevcut Durum:**
- Güç hesaplama sistemleri çalışıyor ✅
- Klan savaş sistemi temel olarak çalışıyor ✅
- Territory koruması savaş durumunda kalkıyor ✅

**Eksikler:**
- Oyuncu koruma sistemi (3 seviye farkı, %95 hasar azaltma) ❌
- Klan koruma sistemi (3 seviye farkı, 50 blok kontrolü) ❌
- Savaş sistemi kontrolleri (totem, %35 aktif, general) ❌

**Öncelik:**
1. **YÜKSEK**: Oyuncu koruma sistemi (hasar azaltma)
2. **YÜKSEK**: Klan koruma sistemi (savaş açma, klan kurma)
3. **ORTA**: Savaş sistemi kontrolleri (totem, koşullar)

