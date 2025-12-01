# 🌋 Gelişmiş Felaket Sistemi - Detaylı Rehber

## 📋 Genel Bakış

Stratocraft'ta **gelişmiş felaket sistemi** artık oyuncu sayısı ve klan seviyesine göre dinamik olarak güçleniyor!

### Özellikler:
- ✅ **Dinamik Güç Sistemi** (oyuncu sayısı + klan seviyesi)
- ✅ **Kategoriler:** Canlı felaketler, Doğa olayları
- ✅ **Seviyeler:** 1-3 (güç ve spawn sıklığı)
- ✅ **Otomatik Spawn** (seviyeye göre zamanlama)
- ✅ **Ekranda Sayaç** (BossBar)
- ✅ **Özel Hareketler** (sıkışmayı önleme)

---

## 🎯 Güç Hesaplama Formülü

Felaketlerin gücü **oyuncu sayısı** ve **klan seviyesi**ne göre hesaplanır:

### Formül:
```
Güç Çarpanı = 1.0 + (Oyuncu Sayısı × 0.1) + (Ortalama Klan Seviyesi × 0.15)
Hesaplanmış Can = Temel Can × Güç Çarpanı
Hesaplanmış Hasar = Temel Hasar × Güç Çarpanı
```

### Örnek:
- **10 oyuncu**, **Ortalama klan seviyesi: 5**
- Güç Çarpanı = 1.0 + (10 × 0.1) + (5 × 0.15) = **2.75x**
- Seviye 3 Titan Golem: 5000 × 2.75 = **13,750 can**

---

## 📊 Kategoriler

### 1. Canlı Felaketler (CREATURE)

Güçlü bosslar şeklinde olurlar. **Merkezden en uzak seviyede** spawn olur ve **merkeze doğru ilerler**.

**Özellikler:**
- Merkeze doğru ilerler
- Yoluna çıkan tüm klanları yok eder
- Arada bir oyunculara özel saldırılar yapar
- Pasif olarak sürekli etrafa hasar verir

**Canlı Felaketler:**
- **Titan Golem** (Seviye 3)
- **Hiçlik Solucanı** (Seviye 2)
- **Khaos Ejderi** (Seviye 3)
- **Boşluk Titanı** (Seviye 3)

---

### 2. Doğa Olayları (NATURAL)

Doğada olan olayların dünyaya etki etmesidir.

**Özellikler:**
- Belirli bir süre boyunca aktif kalır
- Tüm dünyayı etkiler
- Oyuncuları ve yapıları etkiler

**Doğa Olayları:**
- **Güneş Patlaması** (Seviye 1)
- **Deprem** (Seviye 2)
- **Meteor Yağmuru** (Seviye 2)
- **Volkanik Patlama** (Seviye 3)

---

## 📈 Seviyeler

Felaketlerin güç seviyesini ve ortaya çıkma zamanlarını belirler:

### Seviye 1
- **Güç:** Güçsüz
- **Süre:** Kısa (10 dakika)
- **Hasar:** Aşırı hasar yaratmaz
- **Spawn Sıklığı:** Her gün bir tane

**Felaketler:**
- Güneş Patlaması

---

### Seviye 2
- **Güç:** Orta
- **Süre:** Orta (20 dakika)
- **Hasar:** Orta seviye hasar
- **Spawn Sıklığı:** 3 günde bir

**Felaketler:**
- Hiçlik Solucanı
- Deprem
- Meteor Yağmuru

---

### Seviye 3
- **Güç:** Çok güçlü
- **Süre:** Uzun (30 dakika)
- **Hasar:** Çok fazla hasar
- **Spawn Sıklığı:** 7 günde bir
- **Gereksinim:** Yenmek için çok fazla kişi gerekir

**Felaketler:**
- Titan Golem
- Khaos Ejderi
- Boşluk Titanı
- Volkanik Patlama

---

## 🎮 Felaket Detayları

### Canlı Felaketler

#### **Titan Golem**
- **Seviye:** 3
- **Özellikler:**
  - Zıplama-Patlama yeteneği (her 15-20 saniyede bir)
  - Blok fırlatma (her 10-15 saniyede bir)
  - Pasif patlama (her 10 saniyede bir)
  - Sıkışma önleme (zıplama)
  - Klan yapılarını yok eder

#### **Hiçlik Solucanı**
- **Seviye:** 2
- **Özellikler:**
  - Yer altında ilerler
  - Temelleri kazar
  - Sıkışma önleme (ışınlanma)
  - Görünmez

#### **Khaos Ejderi**
- **Seviye:** 3
- **Özellikler:**
  - Ateş püskürtme
  - Uçarak ilerler
  - Oyunculara özel saldırılar

#### **Boşluk Titanı**
- **Seviye:** 3
- **Özellikler:**
  - Boşluk patlaması
  - Güçlü hasar
  - Çok dayanıklı

---

### Doğa Olayları

#### **Güneş Patlaması**
- **Seviye:** 1
- **Süre:** 10 dakika
- **Etkiler:**
  - Yüzeydeki oyuncular yanar
  - Yanıcı bloklar (ahşap, yün vb.) tutuşur
  - Çatı altında olanlar korunur
  - Klan bölgelerinde yakma yapılmaz

#### **Deprem**
- **Seviye:** 2
- **Süre:** 5 dakika
- **Etkiler:**
  - Rastgele konumlarda sarsıntı
  - Yüksek binalarda düşme hasarı
  - Blokları kırar

#### **Meteor Yağmuru**
- **Seviye:** 2
- **Süre:** 20 dakika
- **Etkiler:**
  - Rastgele konumlarda meteor düşer
  - Meteorlar hasar verir
  - Blokları kırar

#### **Volkanik Patlama**
- **Seviye:** 3
- **Süre:** 60 dakika
- **Etkiler:**
  - Rastgele konumlarda lav fışkırması
  - Lav blokları yerleştirir
  - Patlama efektleri

---

## 🎯 Özel Hareketler

Canlı felaketler **sıkışmayı önlemek** için özel hareketler kullanır:

### Zıplama
- **Kullanıcı:** Titan Golem
- **Açıklama:** Önünde blok varsa yüksek zıplama yapar

### Işınlanma
- **Kullanıcı:** Hiçlik Solucanı
- **Açıklama:** Sıkıştığında 5 blok ileriye ışınlanır

### Kazma
- **Kullanıcı:** Hiçlik Solucanı
- **Açıklama:** Önündeki ve altındaki blokları kazar

---

## 📱 Ekranda Sayaç

Her oyuncunun ekranında **sağ üst kısımda** küçük bir **BossBar** görünür:

**Gösterilen Bilgiler:**
- Felaket ismi
- Seviye
- Can (mevcut/maksimum)
- Kalan süre (dakika:saniye)

**Örnek:**
```
Titan Golem | 13,750/13,750 | 25:30
```

---

## 🎮 Admin Komutları

### Felaket Başlat
```bash
/stratocraft disaster start <type> [level] [konum]
```

**Örnekler:**
```bash
/stratocraft disaster start titan_golem 3
/stratocraft disaster start solar_flare 1 ben
/stratocraft disaster start earthquake 2 100 64 200
```

**Parametreler:**
- `type`: Felaket tipi (TITAN_GOLEM, SOLAR_FLARE, vb.)
- `level`: Seviye (1-3, varsayılan: tip'e göre)
- `konum`: 'ben' veya 'X Y Z' koordinatları

---

### Felaketi Durdur
```bash
/stratocraft disaster stop
```

Aktif felaketi durdurur ve yok eder.

---

### Felaket Bilgisi
```bash
/stratocraft disaster info
```

Aktif felaket hakkında detaylı bilgi gösterir:
- Tip
- Kategori
- Seviye
- Can
- Hasar çarpanı
- Kalan süre

---

### Felaket Listesi
```bash
/stratocraft disaster list
```

Tüm felaket tiplerini ve seviyelerini listeler.

---

### Felaketi Yok Et (Eski Komut)
```bash
/stratocraft disaster clear
```

`stop` komutu ile aynı işlevi görür.

---

## ⚙️ Otomatik Spawn Sistemi

Felaketler **otomatik olarak** spawn olur:

### Spawn Zamanları:
- **Seviye 1:** Her gün (24 saat)
- **Seviye 2:** 3 günde bir (72 saat)
- **Seviye 3:** 7 günde bir (168 saat)

### Spawn Konumu:
- **Merkezden en uzak nokta** (5000 blok)
- Rastgele yön (kuzey/güney/doğu/batı)

### Kontrol:
- Her 10 dakikada bir kontrol edilir
- Zaten aktif felaket varsa spawn edilmez

---

## 🛡️ Klan Koruması

### Tektonik Sabitleyici
- Felaketlerin blok kırma hasarını **%90 azaltır**
- Yakıt tüketir
- 50 blok yarıçap içinde etkilidir

### Klan Bölgeleri
- Doğa olayları klan bölgelerinde **etkisizdir**
- Güneş patlaması klan bölgelerinde yakma yapmaz

---

## 📊 Felaket Özeti

| Felaket | Kategori | Seviye | Spawn Sıklığı | Süre |
|---------|----------|--------|---------------|------|
| Güneş Patlaması | Doğa | 1 | Her gün | 10 dk |
| Hiçlik Solucanı | Canlı | 2 | 3 günde bir | 20 dk |
| Deprem | Doğa | 2 | 3 günde bir | 5 dk |
| Meteor Yağmuru | Doğa | 2 | 3 günde bir | 20 dk |
| Titan Golem | Canlı | 3 | 7 günde bir | 30 dk |
| Khaos Ejderi | Canlı | 3 | 7 günde bir | 30 dk |
| Boşluk Titanı | Canlı | 3 | 7 günde bir | 30 dk |
| Volkanik Patlama | Doğa | 3 | 7 günde bir | 60 dk |

---

## 🎯 Strateji İpuçları

1. **Güç Hesaplama:**
   - Daha fazla oyuncu = Daha güçlü felaket
   - Yüksek klan seviyesi = Daha güçlü felaket
   - Felaketleri yenmek için birlik olun!

2. **Klan Koruması:**
   - Tektonik Sabitleyici kurun
   - Klan bölgelerinde korunun
   - Yapılarınızı güçlendirin

3. **Doğa Olayları:**
   - Çatı altında kalın
   - Klan bölgelerinde korunun
   - Yanıcı blokları koruyun

4. **Canlı Felaketler:**
   - Birlik olun
   - Tektonik Sabitleyici kullanın
   - Felaketi merkeze ulaşmadan durdurun

---

**İyi şanslar! 🌋**

