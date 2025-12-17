# ⚔️ KUŞATMA SİSTEMİ (KLAN SAVAŞI) - DETAYLI AKIŞ ŞEMASI

## 📋 İÇİNDEKİLER

1. [Savaş Başlatma](#savaş-başlatma)
2. [Hazırlık Süresi (Warmup)](#hazırlık-süresi-warmup)
3. [Savaş Sırasında Kurallar](#savaş-sırasında-kurallar)
4. [Savaş Bitirme Yöntemleri](#savaş-bitirme-yöntemleri)
5. [Savaş Sonrası](#savaş-sonrası)
6. [Mantıksızlıklar ve Eksikler](#mantıksızlıklar-ve-eksikler)

---

## 🎯 SAVAŞ BAŞLATMA

### Akış Şeması

```
┌─────────────────────────────────────────────────────────┐
│ 1. OYUNCU BEACON YERLEŞTİRİR (Kuşatma Anıtı)            │
└─────────────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│ 2. KONTROLLER                                           │
│    ├─ Klan üyesi mi?                                   │
│    ├─ General veya Lider mi?                           │
│    ├─ %35 aktif üye var mı?                           │
│    ├─ En az 1 General aktif mi?                       │
│    ├─ Düşman bölgesinin 50 blok yakınında mı?         │
│    ├─ Grace Period kontrolü (24 saat)                 │
│    ├─ Spam önleme (5 dakika cooldown)                 │
│    └─ Savunan klandan en az 1 kişi online mu?         │
└─────────────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│ 3. OFFLINE KORUMA KONTROLÜ                              │
│    ├─ Savunan klan offline mı?                         │
│    ├─ Enerji Kalkanı aktif mi?                         │
│    └─ Yakıt tüket (max 5 yakıt)                       │
└─────────────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│ 4. SAVAŞ BAŞLATILIR                                     │
│    ├─ SiegeManager.startSiege()                       │
│    ├─ Broadcast mesajı                                 │
│    └─ SiegeTimer başlatılır                            │
└─────────────────────────────────────────────────────────┘
```

### Detaylı Kurallar

**Gereksinimler**:
- ✅ **Yetki**: General veya Lider olmalı
- ✅ **Aktif Üye**: Klanın %35'i online olmalı
- ✅ **General Aktif**: En az 1 General online olmalı
- ✅ **Mesafe**: Düşman bölgesinin 50 blok yakınında olmalı
- ✅ **Grace Period**: Yeni klanlar 24 saat korunur
- ✅ **Cooldown**: Aynı saldıran klan 5 dakika içinde tekrar anıt dikemez
- ✅ **Offline Baskın Önleme**: Savunan klandan en az 1 kişi online olmalı

**Offline Koruma**:
- Eğer savunan klan offline ise ve Enerji Kalkanı aktifse
- Maksimum 5 yakıt tüketilir (spam önleme)
- Savaş başlatılır

---

## ⏱️ HAZIRLIK SÜRESİ (WARMUP)

### Akış Şeması

```
┌─────────────────────────────────────────────────────────┐
│ SİEGE TIMER BAŞLAR (Config'den süre, varsayılan 5 dk)   │
└─────────────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│ GERİ SAYIM                                              │
│    ├─ Her 60 saniyede bir mesaj                        │
│    ├─ 30 saniye kala mesaj                             │
│    └─ 10 saniye kala mesaj                             │
└─────────────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│ SÜRE BİTER                                              │
│    └─ "SAVAŞ BAŞLADI!" mesajı                          │
│    └─ Korumalar kalkar                                 │
└─────────────────────────────────────────────────────────┘
```

### Özellikler

- **Süre**: Config'den alınır (varsayılan 300 saniye = 5 dakika)
- **Mesajlar**: 60 saniye, 30 saniye, 10 saniye kala
- **Hazırlık**: Bu süre boyunca korumalar hala aktif

---

## ⚔️ SAVAŞ SIRASINDA KURALLAR

### Akış Şeması

```
┌─────────────────────────────────────────────────────────┐
│ SAVAŞ AKTİF (Warmup bitti)                             │
└─────────────────────────────────────────────────────────┘
                        │
        ┌───────────────┼───────────────┐
        │               │               │
        ▼               ▼               ▼
┌───────────────┐ ┌───────────────┐ ┌───────────────┐
│ BLOK KIRMA    │ │ BLOK YERLEŞT. │ │ SANDIK AÇMA   │
└───────────────┘ └───────────────┘ └───────────────┘
        │               │               │
        ▼               ▼               ▼
┌───────────────┐ ┌───────────────┐ ┌───────────────┐
│ Saldıran klan │ │ Saldıran klan │ │ Saldıran klan │
│ kırabilir     │ │ yerleştirebilir│ │ açabilir      │
└───────────────┘ └───────────────┘ └───────────────┘
```

### Detaylı Kurallar

#### ✅ KALKAN KURALLAR

**Blok Kırma**:
- ✅ Saldıran klan düşman bölgesinde blok kırabilir
- ✅ Yapı çekirdekleri kırılamaz (korunur)
- ✅ Enerji Kalkanı aktifse offline koruma çalışır
- ✅ Kristal kırılabilir (kalkan yoksa)

**Blok Yerleştirme**:
- ✅ Saldıran klan düşman bölgesinde blok yerleştirebilir
- ✅ Stratejik yıkım ve inşaat yapılabilir

**Sandık Açma**:
- ✅ Saldıran klan düşman sandıklarını açabilir
- ✅ İtem çalma mümkün

**PvP (Oyuncu Saldırısı)**:
- ✅ **TÜM KORUMA KURALLARI KALKAR**
- ✅ Savaşta herkes herkese saldırabilir
- ✅ Güç farkı koruması kalkar
- ✅ Seviye farkı koruması kalkar
- ✅ Acemi koruması kalkar
- ✅ Klan içi koruma kalkar (savaşta)

**Kristal Kırma**:
- ✅ Saldıran klan kristali kırabilir
- ⚠️ Enerji Kalkanı aktifse kırılamaz (yakıt tüketilir)
- ✅ Kristal kırılırsa savaş biter (saldıran kazanır)

#### ❌ KORUNAN KURALLAR

**Yapı Çekirdekleri**:
- ❌ Yapı çekirdekleri kırılamaz (korunur)
- ❌ Yapıları kaldırmak için klan menüsü kullanılmalı

**Enerji Kalkanı**:
- ❌ Kalkan aktifse kristal kırılamaz
- ❌ Offline koruma çalışır (yakıt varsa)

---

## 🏁 SAVAŞ BİTİRME YÖNTEMLERİ

### Akış Şeması

```
┌─────────────────────────────────────────────────────────┐
│ SAVAŞ BİTİRME YÖNTEMLERİ                                │
└─────────────────────────────────────────────────────────┘
                        │
        ┌───────────────┼───────────────┐
        │               │               │
        ▼               ▼               ▼
┌───────────────┐ ┌───────────────┐ ┌───────────────┐
│ 1. KRISTAL    │ │ 2. BEYAZ BAYRAK│ │ 3. ADMIN      │
│    KIRMA      │ │    (PES ETME)  │ │    KOMUTU     │
└───────────────┘ └───────────────┘ └───────────────┘
        │               │               │
        ▼               ▼               ▼
┌───────────────┐ ┌───────────────┐ ┌───────────────┐
│ Saldıran      │ │ Savunan       │ │ Admin         │
│ Kazanır       │ │ Pes Eder      │ │ Zorla Bitirir │
└───────────────┘ └───────────────┘ └───────────────┘
```

### 1. Kristal Kırma (Zafer)

**Akış**:
```
┌─────────────────────────────────────────────────────────┐
│ Saldıran klan kristali kırmaya çalışır                 │
└─────────────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│ Enerji Kalkanı kontrolü                                 │
│    ├─ Kalkan aktif mi?                                  │
│    ├─ Yakıt var mı?                                    │
│    └─ Kalkan aktifse → Kırılamaz (yakıt tüketilir)    │
└─────────────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│ Kalkan yoksa veya yakıt bittiyse                        │
│    ├─ Kristal kırılır                                  │
│    ├─ SiegeManager.endSiege() çağrılır                 │
│    ├─ Savunan klan bozulur                             │
│    ├─ Para ödülü verilir                               │
│    └─ Fatih Buff'ı uygulanır                           │
└─────────────────────────────────────────────────────────┘
```

**Sonuçlar**:
- ✅ Saldıran klan kazanır
- ✅ Savunan klan bozulur (disband)
- ✅ Para ödülü (config'den yüzde)
- ✅ Fatih Buff'ı

### 2. Beyaz Bayrak (Pes Etme)

**Akış**:
```
┌─────────────────────────────────────────────────────────┐
│ Savunan klan General/Lider                              │
│ Shift + Sağ Tık → Beyaz Bayrak (WHITE_BANNER)          │
└─────────────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│ KONTROLLER                                               │
│    ├─ Savaşta mı?                                       │
│    ├─ General veya Lider mi?                           │
│    └─ Klan bölgesinde mi?                              │
└─────────────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│ SiegeManager.surrender() çağrılır                       │
│    ├─ Savaş bitirilir                                   │
│    ├─ Sandıkların yarısı alınır                        │
│    ├─ Para ödülü verilir                               │
│    └─ Fatih Buff'ı uygulanır                           │
└─────────────────────────────────────────────────────────┘
```

**Sonuçlar**:
- ✅ Savunan klan pes eder
- ✅ Klan bozulmaz (korunur)
- ✅ Sandıkların yarısı kazanan klana gider
- ✅ Para ödülü (config'den yüzde)
- ✅ Fatih Buff'ı

**Sandık Loot Sistemi**:
- Chunk bazlı tarama (performans)
- Zamana yayılmış işleme (lag önleme)
- Her sandıktan itemlerin yarısı alınır
- Yere düşürülür (kazanan klan toplayabilir)

### 3. Admin Komutu

**Komut**: `/stratocraft siege surrender <klan>`

**Sonuçlar**:
- Admin zorla pes ettirebilir
- Normal pes etme sonuçları uygulanır

---

## 🎁 SAVAŞ SONRASI

### Kazanan Klan (Saldıran)

**Ödüller**:
- ✅ **Para Ödülü**: Kaybeden klanın bakiyesinin yüzdesi (config'den)
- ✅ **Fatih Buff'ı**: Geçici güçlendirme
- ✅ **Sandık Loot**: Pes etme durumunda sandıkların yarısı

### Kaybeden Klan (Savunan)

**Sonuçlar**:
- ❌ **Kristal Kırılırsa**: Klan bozulur (disband)
- ❌ **Para Kaybı**: Bakiyesinin yüzdesi kaybedilir
- ✅ **Pes Ederse**: Klan korunur, sadece sandık/para kaybı

---

## ⚠️ MANTIKSIZLIKLAR VE EKSİKLER

### 1. ⚠️ **MANTIKSIZLIK: Savaş Başlatma Kontrolü Eksik**

**Sorun**: 
- Savaş başlatılırken sadece savunan klandan 1 kişi online olması kontrol ediliyor
- Ancak saldıran klandan da en az 1 kişi online olmalı kontrolü yok

**Kod Yeri**: `SiegeManager.startSiege()` - Satır 38-51

**Öneri**: Saldıran klandan da en az 1 kişi online kontrolü eklenmeli

---

### 2. ⚠️ **MANTIKSIZLIK: Warmup Süresi Boyunca Korumalar Aktif**

**Sorun**:
- Warmup süresi boyunca (5 dakika) korumalar hala aktif
- Saldıran klan hazırlık yapamaz (blok yerleştiremez, kıramaz)
- Bu süre boyunca sadece beklemek zorunda

**Kod Yeri**: `SiegeTimer.java` - Warmup süresi boyunca korumalar kalkmıyor

**Öneri**: Warmup süresi boyunca da sınırlı erişim verilebilir (örneğin sadece blok yerleştirme)

---

### 3. ⚠️ **EKSİK: Savaş Süresi Limiti Yok**

**Sorun**:
- Savaş süresi limiti yok
- Savaş süresiz devam edebilir
- Bu durumda sunucu kaynakları tükenebilir

**Kod Yeri**: `SiegeTimer.java` - Sadece warmup süresi var, savaş süresi yok

**Öneri**: Maksimum savaş süresi eklenmeli (örneğin 1 saat), süre bitince otomatik pes etme

---

### 4. ⚠️ **EKSİK: Savaş İstatistikleri Yok**

**Sorun**:
- Savaş sırasında öldürme sayıları takip edilmiyor
- Savaş sonrası istatistik gösterilmiyor
- Hangi klan daha iyi performans gösterdi bilinmiyor

**Kod Yeri**: `SiegeManager.java` - İstatistik sistemi yok

**Öneri**: Savaş istatistikleri eklenmeli (öldürme, ölüm, hasar vb.)

---

### 5. ⚠️ **MANTIKSIZLIK: Yapı Çekirdekleri Kırılamaz**

**Sorun**:
- Savaş sırasında yapı çekirdekleri kırılamaz
- Ancak yapıların kendisi kırılabilir
- Bu mantıksız: Yapı çekirdeği korunurken yapı kırılabilir

**Kod Yeri**: `TerritoryListener.java` - Satır 100-106

**Öneri**: Savaş sırasında yapı çekirdekleri de kırılabilir olmalı (veya hiçbir yapı kırılamaz)

---

### 6. ⚠️ **EKSİK: Savaş Bildirimleri Yetersiz**

**Sorun**:
- Savaş başladığında sadece broadcast mesajı var
- Savaş sırasında durum güncellemeleri yok
- Oyuncular savaş durumunu takip edemiyor

**Kod Yeri**: `SiegeManager.java` - Bildirim sistemi yetersiz

**Öneri**: 
- Savaş durumu komutu eklenmeli (`/siege status`)
- Periyodik durum güncellemeleri (her 5 dakikada bir)
- HUD göstergesi (savaş süresi, kalan süre vb.)

---

### 7. ⚠️ **EKSİK: Çoklu Savaş Desteği Yok**

**Sorun**:
- Bir klan aynı anda sadece bir savaşta olabilir
- Bir klan hem saldıran hem savunan olamaz
- Bu durumda stratejik savaşlar yapılamaz

**Kod Yeri**: `SiegeManager.java` - `activeSieges` Map yapısı tek savaş destekliyor

**Öneri**: Çoklu savaş desteği eklenmeli (bir klan hem saldıran hem savunan olabilir)

---

### 8. ⚠️ **MANTIKSIZLIK: Offline Koruma Yakıt Tüketimi**

**Sorun**:
- Savaş başlatılırken offline koruma yakıtı tüketiliyor (max 5)
- Ancak savaş sırasında da offline koruma çalışıyor
- Bu durumda yakıt çift tüketiliyor olabilir

**Kod Yeri**: 
- `SiegeListener.java` - Satır 110-127 (savaş başlatma)
- `TerritoryListener.java` - Satır 127-144 (savaş sırasında)

**Öneri**: Yakıt tüketimi mantığı gözden geçirilmeli

---

### 9. ⚠️ **EKSİK: Savaş Sonrası Cooldown Yok**

**Sorun**:
- Bir klan savaş kaybettikten sonra hemen tekrar savaş açılabilir
- Bu durumda klanlar sürekli saldırıya uğrayabilir
- Koruma süresi yok

**Kod Yeri**: `SiegeManager.java` - Cooldown sistemi yok

**Öneri**: Savaş kaybeden klanlar için koruma süresi eklenmeli (örneğin 24 saat)

---

### 10. ⚠️ **EKSİK: Savaş İttifak Sistemi Yok**

**Sorun**:
- İttifaklar savaşta yardım edemiyor
- İttifak üyeleri savaşta taraf seçemiyor
- Stratejik ittifak savaşları yapılamıyor

**Kod Yeri**: `SiegeManager.java` - İttifak kontrolü yok

**Öneri**: İttifak sistemi entegre edilmeli (ittifak üyeleri savaşta yardım edebilir)

---

### 11. ⚠️ **MANTIKSIZLIK: Blok Kırma Kontrolünde BEACON Kontrolü**

**Sorun**:
- `TerritoryListener.onBreak()` metodunda kristal kırma kontrolü için `Material.BEACON` kontrolü yapılıyor
- Ancak klan kristali aslında `EnderCrystal` entity'si, blok değil
- Bu kontrol hiçbir zaman çalışmayacak (BEACON blok kırma event'inde kristal entity'si kontrol edilemez)

**Kod Yeri**: `TerritoryListener.java` - Satır 150-151

**Mevcut Kod**:
```java
if (event.getBlock().getType() == Material.BEACON) {
    // Bu kontrol hiçbir zaman çalışmayacak
    // Çünkü kristal entity'si, blok değil
}
```

**Doğru Kontrol**: `onCrystalBreak()` metodunda `EnderCrystal` entity kontrolü yapılıyor (Satır 996-1092)

**Öneri**: `onBreak()` metodundaki BEACON kontrolü kaldırılmalı veya düzeltilmeli

---

## 📊 ÖZET TABLO

| Özellik | Durum | Açıklama |
|---------|-------|----------|
| Savaş Başlatma | ✅ Çalışıyor | Beacon yerleştirme ile |
| Warmup Süresi | ✅ Çalışıyor | Config'den alınan süre |
| Blok Kırma | ✅ Çalışıyor | Saldıran klan kırabilir |
| Blok Yerleştirme | ✅ Çalışıyor | Saldıran klan yerleştirebilir |
| Sandık Açma | ✅ Çalışıyor | Saldıran klan açabilir |
| PvP | ✅ Çalışıyor | Tüm korumalar kalkar |
| Kristal Kırma | ✅ Çalışıyor | Kalkan yoksa kırılabilir |
| Pes Etme | ✅ Çalışıyor | Beyaz Bayrak ile |
| Savaş Süresi Limiti | ❌ Yok | Süresiz devam edebilir |
| Savaş İstatistikleri | ❌ Yok | Takip edilmiyor |
| Çoklu Savaş | ❌ Yok | Tek savaş desteği |
| Savaş Sonrası Cooldown | ❌ Yok | Koruma süresi yok |
| İttifak Desteği | ❌ Yok | İttifaklar savaşta yardım edemiyor |

---

**Tarih**: Son Analiz
**Durum**: ✅ Temel sistem çalışıyor, ancak bazı mantıksızlıklar ve eksikler var

