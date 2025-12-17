# STRATOCRAFT - KUŞATMA SİSTEMİ (YENİ VERSİYON)

## ⚔️ Kuşatma Nedir?

Kuşatma, **iki taraflı bir savaş sistemidir**. Bir klan diğerine savaş ilan ettiğinde, **her iki klan da birbirine saldırabilir** ve **korumalar karşılıklı olarak kalkar**. Savaş, bir taraf pes edene veya kristal kırılana kadar **sınırsız süre** devam eder.

---

## 📋 İÇİNDEKİLER

1. [Savaş İlanı (Savaş Totemi)](#savaş-ilanı-savaş-totemi)
2. [İki Taraflı Savaş Sistemi](#iki-taraflı-savaş-sistemi)
3. [Çoklu Savaş Desteği](#çoklu-savaş-desteği)
4. [Savaş Kuralları](#savaş-kurallari)
5. [Barış Anlaşması](#barış-anlaşması)
6. [Beyaz Bayrak - Pes Etme](#beyaz-bayrak---pes-etme)
7. [Zafer ve Ödüller](#zafer-ve-ödüller)
8. [İttifak Sistemi](#ittifak-sistemi)

---

## 🏰 SAVAŞ İLANI (SAVAŞ TOTEMİ)

### ✅ YENİ: Özel Totem Yapısı

Savaş ilanı için **özel bir totem yapısı** gerekir. Bu yapı **çok daha kolay** yapılabilir:

**Totem Yapısı**:
```
[IRON_BLOCK] [IRON_BLOCK]  (Y: +1 - Üst katman)
[GOLD_BLOCK] [GOLD_BLOCK]  (Y: 0 - Alt katman)
```

**Gereksinimler**:
```
Malzemeler:
- 2 Altın Blok (GOLD_BLOCK)
- 2 Demir Blok (IRON_BLOCK)
- Yetki: General veya Lider (sadece)

Koşullar:
- Düşman klanın 50 blok yakınında olmalı
- Klanın %35'i aktif olmalı (online)
- En az bir General aktif olmalı
- Grace Period kontrolü (yeni klanlar 24 saat korunur)
- Spam önleme: 5 dakika cooldown
```

### Adım 1: Konum Seç

```
Düşman klan sınırının 50 blok yakını:

[Düşman Bölgesi]
        ↓ 50 blok içinde
     [Totem]  ← Buraya koy
```

**Kural**: Çok yakın koyma (saldırı alanına düşersin), çok uzak koyma (geçersiz)

---

### Adım 2: Totem Yapısını Oluştur

**2x2 Yapı**:
```
Y: +1 (Üst):
[IRON_BLOCK] [IRON_BLOCK]

Y: 0 (Alt):
[GOLD_BLOCK] [GOLD_BLOCK]
```

**Önemli**: 
- Altın blokları alt katmana (Y: 0)
- Demir blokları üst katmana (Y: +1) yerleştir
- Yapı tamamlandığında otomatik olarak savaş ilanı başlar

---

### Adım 3: Savaş Başlar

```
Totem yapısı tamamlandığında:
→ İki taraflı savaş başlar
→ Hazırlık süresi (config'den, varsayılan 5 dakika)
→ İki klana bildirim gider
→ Her iki klanın korumaları karşılıklı kalkar
```

**Görsel Efektler**:
- Şimşek çakar (Totem'de)
- EXPLOSION partikülleri
- Broadcast mesajı: "SAVAŞ İLANI! [Klan A] ve [Klan B] klanları savaşa girdi!"

---

## ⚔️ İKİ TARAFLI SAVAŞ SİSTEMİ

### ✅ YENİ: Karşılıklı Saldırı

**Önceki Sistem**: Sadece saldıran klan korumaları kaldırıyordu.

**Yeni Sistem**: 
- **Her iki klan da birbirine saldırabilir**
- **Korumalar karşılıklı olarak kalkar**
- Savaşta olunan klan için korumalar kalkar, diğer klanlar hala dokunamaz

**Örnek Senaryo**:
```
Klan A → Klan B'ye savaş ilan eder
→ Klan A, Klan B'ye saldırabilir (korumalar kalktı)
→ Klan B, Klan A'ya saldırabilir (korumalar kalktı)
→ Klan C, Klan A'ya dokunamaz (savaşta değil)
→ Klan C, Klan B'ye dokunamaz (savaşta değil)
```

---

## 🔄 ÇOKLU SAVAŞ DESTEĞİ

### ✅ YENİ: Aynı Anda Birden Fazla Savaş

**Önceki Sistem**: Bir klan sadece bir klanla savaşta olabilirdi.

**Yeni Sistem**:
- **Bir klan aynı anda birden fazla klanla savaşta olabilir**
- Her savaş bağımsız olarak yönetilir
- Barış anlaşması veya pes etme belirli bir klanla savaşı bitirir

**Örnek Senaryo**:
```
Klan A:
→ Klan B ile savaşta
→ Klan C ile savaşta
→ Klan D ile savaşta

Her savaş bağımsız:
→ Klan A, Klan B'ye karşı korumaları kaldırdı
→ Klan A, Klan C'ye karşı korumaları kaldırdı
→ Klan A, Klan D'ye karşı korumaları kaldırdı
→ Diğer klanlar (Klan E, F, G...) hala dokunamaz
```

---

## ⏱️ HAZIRLIK SÜRECİ

### Config'den Belirlenen Süre

**Varsayılan**: 5 dakika (config'den değiştirilebilir)

**Ne Olur?**:
```
T-0: Totem dikilir
     → "SAVAŞ İLANI!" mesajı
     → Hazırlık süresi başlar

T-1dk: Savunan hazırlık yapar
       → Tuzaklar kur
       → Bataryalar yükle
       → Takım topla

T-3dk: Saldıran konumlanır
       → Bataryalar yarat
       → Mancınık kur
       → Strateji belirle

T-5dk: SAVAŞ BAŞLAR!
       → Yapılar hasarlı hale gelir
       → Bölge koruması kalkar (sadece savaşta olunan klan için)
```

---

### Hazırlık Boyunca

**Savunan Yapabilir**:
- Tuzak kurmak
- Batarya hazırlamak
- Zırh giymek
- Yakıt eklemek (kristale)
- Enerji kalkanı aktif etmek

**Saldıran Yapabilir**:
- Batarya kurmak (Totem dışında)
- Mancınık yerleştirmek
- Takımla koordine olmak
- Dalışma planlamak

**Her İki Taraf YAPAMAZ**:
- Birbirine saldırmak (henüz)
- Düşman bölgesine girmek
- Yapılara hasar vermek

---

## ⚔️ SAVAŞ KURALLARI

### ✅ YENİ: Korumalar Sadece Savaşta Olunan Klan İçin Kalkar

**Önceki Sistem**: Savaş başladığında tüm korumalar herkes için kalkıyordu.

**Yeni Sistem**:
- **Korumalar sadece savaşta olunan klan için kalkar**
- Diğer klanlar ve oyuncular hala dokunamaz
- Bir klan aynı anda birden fazla klanla savaşta olabilir

**Örnek**:
```
Klan A, Klan B ile savaşta:
→ Klan A üyeleri, Klan B bölgesinde blok kırabilir
→ Klan B üyeleri, Klan A bölgesinde blok kırabilir
→ Klan C üyeleri, Klan A'ya dokunamaz (savaşta değil)
→ Klan C üyeleri, Klan B'ye dokunamaz (savaşta değil)
```

### Savaş Başladığında (Hazırlık süresi sonrası)

**İzin Verilenler** (Sadece savaşta olunan klan için):
```
✓ Düşman bölgesine girme
✓ Düşman yapılarına hasar verme
✓ PvP (oyuncu öldürme)
✓ Kristale saldırma
✓ Çitleri kırma
✓ Sandıkları açma (savaşta olunan klan için)
✓ Blok yerleştirme (savaşta olunan klan için)
```

**Yasak Olanlar**:
```
❌ Savaşta olunmayan klanlara dokunma
❌ Enerji kalkanını bypass etme (kristal koruması)
❌ Yapı çekirdeklerini kırma (korunmalı)
```

---

### Kristal İmhası

**Kırma Yöntemi**:
```
1. Çitleri kır (bölgeye gir)
2. Savunma yapılarını aş
   - Zehir Kulesi
   - Tuzaklar
   - Taretler
3. Kristale ulaş (EnderCrystal entity)
4. NORMAL SİLAHLA vur
5. Kristal kırılır
6. SAVAŞ BİTER - ZAFER!
```

**Önemli**: Özel ritüel YOK, normal vurarak kırılır

---

### Enerji Kalkanı

**Eğer Savunan Kalkan Aktif Ettiyse**:
```
Kalkan Etkisi:
- Kristal %90 daha az hasar alır
- Her vuruşta yakıt tüketir
- Yakıt bitince kalkan düşer

Saldıran Strateji:
→ Kalkanı tüket (çok vur)
→ Veya Kalkan Jeneratörü yok et (yapı)
```

---

## 🕊️ BARIŞ ANLAŞMASI

### ✅ YENİ: Karşılıklı Onay ile Savaş Bitirme

Barış anlaşması, savaşı **karşılıklı onay** ile bitirmenin yoludur. **Hiçbir taraf kayıp yaşamaz**.

### Nasıl Kullanılır?

**1. Klan Yönetim Menüsüne Git**:
```
1. /klan menü komutunu kullan
2. "Barış Anlaşması" butonuna tıkla (Slot 23)
3. Menü açılır
```

**2. Savaşta Olunan Klanları Gör**:
```
Menüde savaşta olunan tüm klanlar listelenir:
- Klan B (Savaş Halinde)
- Klan C (Savaş Halinde)
- Klan D (Savaş Halinde)
```

**3. İstek Gönder**:
```
1. Barış yapmak istediğin klana SOL TIK
2. İstek otomatik gönderilir
3. Diğer klan bildirim alır
```

**4. İstek Onaylama**:
```
Diğer klan:
1. Klan Yönetim Menüsü → Barış Anlaşması
2. "Gelen İstekler" butonuna tıkla
3. İsteği görüntüle
4. SOL TIK: Onayla
5. SAĞ TIK: Reddet
```

**5. Sonuç**:
```
İstek onaylandığında:
→ Savaş biter (sadece bu iki klan arasında)
→ Hiçbir taraf kayıp yaşamaz
→ Ganimet transferi olmaz
→ Broadcast: "BARIŞ ANLAŞMASI! [Klan A] ve [Klan B] klanları barış imzaladı!"
```

### Barış Anlaşması Özellikleri

**Gereksinimler**:
```
- Klanınız savaşta olmalı
- Yetki: General veya Lider (sadece)
- İstek 24 saat geçerlidir
```

**Menü Özellikleri**:
```
- Ana Menü: Savaşta olunan klanlar listesi
- Gelen İstekler: Size gönderilen barış anlaşması istekleri
- Gönderilen İstekler: Gönderdiğiniz isteklerin durumu
```

**Önemli**:
- İstek süresi: 24 saat
- Süresi dolan istekler otomatik temizlenir
- Aynı anda birden fazla klana istek gönderebilirsiniz
- Her istek bağımsız olarak yönetilir

---

## 🏳️ BEYAZ BAYRAK - PES ETME SİSTEMİ

### Nasıl Pes Edilir?

Savaşta kaybetmek üzereyseniz, **Beyaz Bayrak** çekerek pes edebilirsiniz.

**Gereksinimler**:
```
- Klanınız savaşta olmalı
- Yetki: General veya Lider (sadece)
- Beyaz Bayrak (White Banner) klan bölgenizde olmalı
```

**Adımlar**:
```
1. Klan bölgenize White Banner (Beyaz Bayrak) koy
2. Eline hiçbir şey alma (boş el)
3. Shift + Sağ Tık (Beyaz Bayrağa)
4. SONUÇ: Klanınız pes eder (belirli bir klana karşı)
```

**Not**: Çoklu savaş durumunda, GUI menüsünden belirli bir klana karşı pes etme seçeneği eklenecek.

**Pes Etme Sonuçları**:
```
✓ Klan YOK OLMAZ (dağılmaz)
✓ Savaş biter (sadece bu klanla)
✓ Klandaki TÜM sandıkların itemlerinin YARISI gider
✓ Klan kasasının %50'si kazanan klana gider
✓ Kazanan klan Fatih Buff'ı alır
```

**Önemli**: Pes etmek, kristal kırılmasından daha iyi bir seçenektir çünkü klanınız dağılmaz!

---

## 🏆 ZAFER VE ÖDÜLLER

### Kristal Kırıldığında

**Anında Etkiler**:
```
1. Düşman klan dağılır
2. Kristal patlıyor (explosion)
3. Bölge koruması kalkar
4. Savaş biter (sadece bu iki klan arasında)
```

---

### Kazanan Klan Ödülleri

#### **1. Ganimet (Loot)**

**Temel Ganimet**:
```
Kaybeden klanın kasasındaki paranın %50'si:

Örnek:
Kaybeden kasa: 10,000 Altın
→ Kazanan alır: 5,000 Altın
→ Kaybeden kalır: 5,000 Altın
```

#### **✅ YENİ: İttifak Ganimet Paylaşımı**

**Özel Senaryo**:
```
Klan A, Klan B ve Klan C ile savaşta
Klan A'nın kristali Klan B kırarsa:

Durum 1: İttifak YOK
→ Tüm ganimet sadece Klan B'ye gider

Durum 2: İttifak VAR (OFFENSIVE veya FULL)
→ Klan B ve Klan C ittifak ise:
  → Ganimet eşit paylaşılır
  → Klan B: %50
  → Klan C: %50
```

**İttifak Tipleri**:
- **OFFENSIVE**: Saldırı ittifakı - Ganimet paylaşılır
- **FULL**: Tam ittifak - Ganimet paylaşılır
- **DEFENSIVE**: Savunma ittifakı - Ganimet paylaşılmaz
- **TRADE**: Ticaret ittifakı - Ganimet paylaşılmaz

#### **2. Fatih Buff'ı (Conqueror Buff)** - 24 Saat

```
Etkiler:
- +%20 Hasar (tüm saldırılar)
- +%30 Üretim Hızı (crafting, mining)
- +%15 Hareket Hızı
- Glowing efekti (altın parıltı)

Süre: 24 saat
```

#### **3. Yapı Malzemeleri**

```
Düşman yapılarının ana blokları düşer:

Zehir Kulesi → İblis Yılanın Gözü
Simya Kulesi → Alchemist Crystal
Radar → Sonar Core
vb.

Bu malzemeler ile kendi yapılarını güçlendir!
```

---

### Kaybeden Klan

**Kristal Kırıldığında**:
```
- Klan dağılır
- Kasanın %50'si gider
- Bölge koruması kalkar
- Tüm yapılar savunmasız
```

**Pes Ettiğinde (Beyaz Bayrak)**:
```
- Klan YOK OLMAZ (dağılmaz)
- Sandıkların itemlerinin yarısı gider
- Kasanın %50'si gider
- Bölge koruması KALIR
- Yapılar KALIR
- Savaş biter (sadece bu klanla)
```

**Kurtarma**:
```
Kristal kırıldıysa:
→ Klan lideri 24 saat içinde yeniden kurabilir
→ Aynı isimle
→ Eski üyelerden bazılarını davet et
→ Yeniden başla

Pes ettiyse:
→ Klan zaten var, sadece kaynaklar azaldı
→ Normal şekilde devam edebilir
```

---

## 🤝 İTTİFAK SİSTEMİ

### ✅ YENİ: İttifak İsteği Gönderme

**Klan Yönetim Menüsü**:
```
1. /klan menü komutunu kullan
2. "İttifaklar" butonuna tıkla (Slot 18)
3. İttifak menüsü açılır
```

**İttifak İsteği Gönderme**:
```
Şu anda sadece fiziksel ritüel destekleniyor:
1. Diğer klanın liderini bulun
2. Shift tuşuna basılı tutun
3. Elmas ile liderin üzerine sağ tık yapın
4. Ritüel otomatik başlayacak
```

**İttifak Tipleri**:
```
- DEFENSIVE: Savunma İttifakı
- OFFENSIVE: Saldırı İttifakı (ganimet paylaşımı)
- TRADE: Ticaret İttifakı
- FULL: Tam İttifak (ganimet paylaşımı)
```

**Ganimet Paylaşımı**:
```
Sadece OFFENSIVE ve FULL ittifaklar ganimet paylaşır:
→ Savaş kazanıldığında
→ İttifak klanları ganimeti eşit paylaşır
```

---

## ⏰ SAVAŞ SÜRESİ

### ✅ YENİ: Sınırsız Savaş

**Önceki Sistem**: Max savaş süresi vardı.

**Yeni Sistem**:
- **Savaş sınırsız süre devam eder**
- Sadece şu yollarla biter:
  1. **Kristal kırılması** (zafer)
  2. **Beyaz Bayrak** (pes etme)
  3. **Barış Anlaşması** (karşılıklı onay)
  4. **Admin komutu**

**Önemli**: 
- İki taraf hiçbir şey yapmazsa klanlar **hep savaş halinde kalır**
- Bu, özgürlük felsefesine uygundur
- Klanlar kendi kararlarını verir

---

## 🎯 KUŞATMA STRATEJİLERİ

### Saldıran İçin

**Hızlı Dalış**:
```
1. Hazırlık süresinde bataryalar hazırla
2. Savaş başlar başlamaz hızlı gir
3. Direkt kristale koş
4. Savunma hazırlanamadan vur
```

**Kuşatma Usulü**:
```
1. Uzaktan mancınık kur
2. Savunma yapılarını yavaş yavaş yık
3. Güvenli koridor aç
4. Sonra kristale ilerle
```

---

### Savunan İçin

**Katmanlı Savunma**:
```
Dış Hat: Tuzaklar (yavaşlatma)
Orta Hat: Zehir Kulesi, Taretler
İç Hat: Patlayıcı tuzaklar
Kristal: Enerji kalkanı + son savunma
```

**Gerilla Taktiği**:
```
1. Saldırana sürekli vur (pvp)
2. Bataryalarını yok et (engelle)
3. Mancınıkları kır
4. Zamanı kazanacak savun
```

---

## ⚠️ ÖNEMLİ NOTLAR

### Kuşatma Kuralları

1. **Savaş İlanı Yetkisi**: Sadece General ve Lider savaş açabilir
2. **Aktif Üye Gereksinimi**: Klanın %35'i aktif olmalı
3. **General Gereksinimi**: En az bir General aktif olmalı
4. **Pes Etme Yetkisi**: Sadece General ve Lider pes edebilir
5. **Barış Anlaşması Yetkisi**: Sadece General ve Lider istek gönderebilir/onaylayabilir
6. **İttifak Yetkisi**: Sadece General ve Lider ittifak kurabilir
7. **Grace Period**: Yeni kurulan klanlar 24 saat korunur
8. **Spam Önleme**: 5 dakika cooldown (aynı klan tekrar savaş açamaz)
9. **Offline Koruma**: Offline klan koruması aktifse yakıt tüketir
10. **Çoklu Savaş**: Bir klan aynı anda birden fazla klanla savaşta olabilir
11. **İki Taraflı Savaş**: Her iki klan da birbirine saldırabilir
12. **Korumalar**: Sadece savaşta olunan klan için kalkar

---

### Veritabanı ve Model

**Yeni Alanlar**:
```
Clan Model:
- warringClans: Set<UUID> (savaşta olunan klanlar)
- allianceClans: Set<UUID> (ittifak olduğu klanlar - referans)

Veritabanı:
- warringClans: List<String> (UUID listesi)
- allianceClans: List<String> (UUID listesi)
```

**Önemli**: 
- Server restart sonrası savaşlar ve ittifaklar korunur
- Geriye dönük uyumluluk: Eski verilerde bu alanlar null olabilir

---

## 🎯 HIZLI KUŞATMA REHBERİ

### Savaş İlanı (3 Adım)

```
1. 2 Altın + 2 Demir blok topla
2. Düşman klan 50 blok yakınına git
3. Totem yapısını oluştur:
   [IRON_BLOCK] [IRON_BLOCK]  (Y: +1)
   [GOLD_BLOCK] [GOLD_BLOCK]  (Y: 0)
→ SAVAŞ BAŞLADI!
```

### Hızlı Zafer (Saldıran)

```
Hazırlık (5 dk):
→ 50x Ateş Topu yükle
→ Takımla hazırla

Savaş (5. dk):
→ Sprint ile gir
→ Kristale koş
→ VUR VR VUR
→ ZAFER!
```

### Barış Anlaşması (Hızlı)

```
1. /klan menü → Barış Anlaşması
2. Savaşta olunan klana SOL TIK
3. İstek gönderildi
4. Diğer klan onaylar
→ SAVAŞ BİTTİ (kayıpsız)
```

---

## 📊 SAVAŞ DURUMU YÖNETİMİ

### Çoklu Savaş Senaryosu

**Örnek**:
```
Klan A:
→ Klan B ile savaşta
→ Klan C ile savaşta

Klan A'nın Kristali Klan B kırarsa:
→ Sadece Klan B ile savaş biter
→ Klan A hala Klan C ile savaşta
→ Ganimet sadece Klan B'ye gider (ittifak yoksa)
```

**İttifak Senaryosu**:
```
Klan A:
→ Klan B ile savaşta
→ Klan C ile savaşta

Klan B ve Klan C ittifak (OFFENSIVE veya FULL):
→ Klan A'nın kristali Klan B kırarsa
→ Ganimet eşit paylaşılır:
  → Klan B: %50
  → Klan C: %50
```

---

**🎮 Kuşatma ile düşmanı yok et, ganimetleri topla, Fatih ol!**

**🕊️ Barış anlaşması ile savaşı kayıpsız bitir!**

**🤝 İttifak kur, ganimetleri paylaş!**
