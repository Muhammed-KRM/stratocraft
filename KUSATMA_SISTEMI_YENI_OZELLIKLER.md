# Kuşatma Sistemi - Yeni Özellikler

## ✅ Tamamlanan Değişiklikler

### 1. İki Taraflı Savaş Sistemi
- **Önceki**: Tek taraflı savaş (sadece saldıran klan korumaları kaldırıyordu)
- **Yeni**: İki taraflı savaş (her iki klan da birbirine saldırabilir, korumalar karşılıklı kalkar)
- **Değişiklikler**:
  - `Clan` modeline `warringClans` (Set<UUID>) eklendi
  - `SiegeManager.startSiege()` her iki klanın savaş listesine ekliyor
  - `TerritoryListener` korumaları sadece savaşta olunan klan için kaldırıyor

### 2. Özel Savaş Totemi
- **Önceki**: Beacon (Fener) yerleştirme ile savaş ilanı
- **Yeni**: Özel totem yapısı (2 Altın Blok + 2 Demir Blok)
- **Yapı**:
  ```
  [IRON_BLOCK] [IRON_BLOCK]  (Y: +1)
  [GOLD_BLOCK] [GOLD_BLOCK]  (Y: 0)
  ```
- **Değişiklikler**:
  - `SiegeListener.onSiegeAnitPlace()` totem yapısı kontrolü eklendi
  - `checkWarTotemStructure()` metodu eklendi

### 3. Çoklu Savaş Desteği
- **Önceki**: Bir klan sadece bir klanla savaşta olabilirdi
- **Yeni**: Bir klan aynı anda birden fazla klanla savaşta olabilir
- **Değişiklikler**:
  - `SiegeManager.activeWars` Map<UUID, Set<UUID>> yapısına dönüştürüldü
  - `Clan.warringClans` Set<UUID> eklendi
  - `SiegeManager.endWar()` belirli bir klanla savaşı bitirir
  - `SiegeManager.surrender()` belirli bir klana karşı pes etme

### 4. Korumalar Sadece Savaşta Olunan Klan İçin Kalkıyor
- **Önceki**: Savaş başladığında tüm korumalar herkes için kalkıyordu
- **Yeni**: Korumalar sadece savaşta olunan klan için kalkıyor, diğer klanlar hala dokunamaz
- **Değişiklikler**:
  - `TerritoryListener.onBreak()`: `owner.isAtWarWith(playerClan.getId())` kontrolü
  - `TerritoryListener.onInventoryOpen()`: Aynı kontrol
  - `TerritoryListener.onBlockPlaceInTerritory()`: Aynı kontrol
  - `ClanProtectionSystem.isClanAtWar()`: `clan.isAtWarWith()` kontrolü

### 5. Ganimet Paylaşımı (İttifak Desteği)
- **Önceki**: Ganimet sadece kazanan klana gidiyordu
- **Yeni**: İttifak varsa ganimet paylaşılıyor
- **Mantık**:
  - Klan A, Klan B ve Klan C ile savaşta
  - Klan A'nın kristali Klan B kırarsa:
    - Eğer Klan B ve Klan C ittifak ise (OFFENSIVE veya FULL): Ganimet eşit paylaşılır
    - Değilse: Tüm ganimet Klan B'ye gider
- **Değişiklikler**:
  - `SiegeManager.endSiege()` ittifak kontrolü eklendi
  - `SiegeManager.surrender()` ittifak kontrolü eklendi

### 6. Barış Anlaşması Sistemi (Model ve Manager)
- **Yeni Model**: `PeaceRequest`
- **Yeni Manager**: `PeaceRequestManager`
- **Özellikler**:
  - İstek gönderme
  - İstek onaylama/reddetme
  - İstek listeleme (gönderilen/alınan)
  - 24 saatlik süre sınırı

### 7. İttifak Sistemi Model Güncellemesi
- **Yeni**: `Clan.allianceClans` Set<UUID> eklendi (referans için)
- **Not**: İttifaklar hala `AllianceManager`'da yönetiliyor, bu sadece referans

## ⏳ Devam Eden İşler

### 1. Barış Anlaşması GUI Menüsü
- **Gereksinimler**:
  - Klan yönetim menüsüne "Barış Anlaşması" butonu
  - Savaşta olunan klanları listeleme
  - İstek gönderme
  - Gelen istekleri görüntüleme ve onaylama/reddetme

### 2. İttifak Sistemi GUI Güncellemesi
- **Gereksinimler**:
  - Klan yönetim menüsüne "İttifak" butonu
  - İttifak isteği gönderme (tip seçimi ile)
  - Gelen istekleri görüntüleme ve onaylama/reddetme
  - İttifak tipleri: DEFENSIVE, OFFENSIVE, TRADE, FULL

### 3. Yetki Kontrolleri
- **Gereksinimler**:
  - Sadece Lider ve General:
    - Savaş ilanı
    - Barış anlaşması isteği gönderme/onaylama
    - İttifak isteği gönderme/onaylama
    - Beyaz bayrak (pes etme)

### 4. DataManager Güncellemeleri
- **Gereksinimler**:
  - `Clan.warringClans` kaydetme/yükleme
  - `Clan.allianceClans` kaydetme/yükleme (opsiyonel, referans için)
  - `PeaceRequest` kaydetme/yükleme

## 📝 Notlar

1. **Savaş Süresi**: Sınırsız (kullanıcı isteği)
2. **Savaş Bitirme Yöntemleri**:
   - Kristal kırma (zafer)
   - Beyaz Bayrak (pes etme)
   - Barış Anlaşması (karşılıklı onay)
   - Admin komutu

3. **İttifak Ganimet Paylaşımı**:
   - Sadece OFFENSIVE ve FULL ittifaklar ganimet paylaşır
   - DEFENSIVE ve TRADE ittifaklar ganimet paylaşmaz

4. **Beyaz Bayrak**:
   - Şu anda ilk savaşta olunan klana pes ediyor
   - GUI'den belirli bir klana karşı pes etme eklenecek

