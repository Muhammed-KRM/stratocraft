# STRATOCRAFT - MARKET SİSTEMİ

## 🛒 Market Sistemi Nedir?

Market sistemi, oyuncuların **eşya satıp alabileceği** ve **teklif verebileceği** bir ticaret sistemidir.

**KOD DOĞRULANDI**: `ShopManager.java`, `ShopListener.java`, `ShopMenu.java` - Tüm mekanikler aktif.

---

## 📋 İÇİNDEKİLER

1. [Market Kurulumu](#market-kurulumu)
2. [Alışveriş Yapma](#alışveriş-yapma)
3. [Teklif Sistemi](#teklif-sistemi)
4. [GUI Menü Sistemi](#gui-menü-sistemi)
5. [Vergi Sistemi](#vergi-sistemi)

---

## 🏪 MARKET KURULUMU

### Adım 1: Sandık Koy

```
1. Sandık (Chest) koy
2. Sandığa satılacak item'ı koy
3. Sandığa ödeme item'ını koy (fiyat)
```

### Adım 2: Tabela Oluştur

**Tabela Formatı**:
```
Satır 1: [SHOP]
Satır 2: <Satılan Item İsmi>
Satır 3: <Fiyat Miktarı> <Ödeme Item İsmi>
Satır 4: (Boş veya açıklama)
```

**Örnek Tabela**:
```
[SHOP]
Titanyum Külçesi
64 Elmas
Satılık!
```

### Adım 3: Tabelayı Sandığa Yerleştir

```
1. Tabelayı sandığın yanına koy
2. Sandığa sağ tık → Market aktif!
```

---

## 🛍️ ALIŞVERIŞ YAPMA

### Klasik Satın Alma

**Adımlar**:
```
1. Market sandığına sağ tık
2. GUI menü açılır
3. "Satın Al" butonuna tıkla
4. Ödeme otomatik alınır
5. Item envantere eklenir
```

**Özellikler**:
- Otomatik stok kontrolü
- Otomatik ödeme kontrolü
- Vergi hesaplama (koruma bölgesinde %5)
- Anında işlem

---

## 💰 TEKLİF SİSTEMİ

### Teklif Verme

**Nasıl Çalışır?**:
```
1. Market menüsünde "Teklif Ver" butonuna tıkla
2. Teklif menüsü açılır
3. Envanterinden item seç
4. Miktar belirle
5. "Teklif Gönder" butonuna tıkla
6. Satıcıya bildirim gider
```

**Teklif Özellikleri**:
- Alternatif ödeme: Farklı item ile ödeme yapabilirsin
- Miktar ayarlama: İstediğin miktarı teklif edebilirsin
- Bildirim: Satıcı teklif geldiğinde bildirim alır
- Maksimum teklif: Her market için maksimum 10 teklif

**Örnek**:
```
Market: 20 Elmas → 64 Titanyum
Sen: 30 Obsidian teklif et
Satıcı: Kabul ederse → 30 Obsidian ver, 64 Titanyum al
```

### Teklif Yönetimi (Satıcı İçin)

**Teklifleri Görüntüleme**:
```
1. Market menüsünde "Teklifler" butonuna tıkla
2. Teklif listesi açılır (sayfalama)
3. Her teklif için:
   - Teklif veren oyuncu
   - Teklif edilen item
   - Teklif miktarı
   - "Kabul Et" / "Reddet" butonları
```

**Teklif Kabul/Reddetme**:
```
1. Teklif listesinden teklifi seç
2. "Kabul Et" → Teklif kabul edilir, item takası yapılır
3. "Reddet" → Teklif reddedilir, bildirim gider
```

---

## 🖥️ GUI MENÜ SİSTEMİ

### Ana Market Menüsü (27 Slot)

**Özellikler**:
```
- Slot 11: Satılan item (görsel + miktar)
- Slot 13: İstenen ödeme (görsel + miktar)
- Slot 15: "Satın Al" butonu (yeşil emerald block)
- Slot 17: "Teklif Ver" butonu (altın block) - Teklif kabul ediliyorsa
- Slot 22: "Teklifler" butonu (sadece satıcı için, teklif varsa)
- Slot 26: "Kapat" butonu
```

### Teklif Verme Menüsü (27 Slot)

**Özellikler**:
```
- Slot 0-26: Envanter item'ları (seçilebilir)
- Slot 13: Seçilen item önizleme
- Slot 15: Miktar ayarlama (+/- butonları)
- Slot 22: "Teklif Gönder" butonu
- Slot 26: "Geri" butonu
```

**Kullanım**:
```
1. Envanterinden item seç (tıkla)
2. Miktar ayarla (+/- butonları)
3. "Teklif Gönder" butonuna tıkla
4. Teklif gönderilir!
```

### Teklifler Listesi Menüsü (54 Slot)

**Özellikler**:
```
- Slot 0-44: Teklif listesi (sayfalama)
- Her teklif için:
  - Slot X: Teklif item'ı (oyuncu kafası + item)
  - Slot X+1: "Kabul Et" butonu
  - Slot X+2: "Reddet" butonu
- Slot 45-53: Sayfalama butonları
- Slot 49: "Geri" butonu
```

---

## 💸 VERGİ SİSTEMİ

### Koruma Bölgesi Vergisi

**Vergi Oranı**: %5

**Nasıl Çalışır?**:
```
1. Market koruma bölgesinde ise
2. Satış yapıldığında %5 vergi kesilir
3. Vergi sandığa eklenir
4. Kalan ödeme satıcıya gider
```

**Örnek**:
```
Satış: 100 Elmas
Vergi: 5 Elmas (%5)
Satıcı: 95 Elmas alır
Sandık: 5 Elmas vergi olarak kalır
```

### Normal Bölge

```
Koruma bölgesi dışında:
- Vergi YOK
- Tüm ödeme satıcıya gider
```

---

## 🎯 MARKET STRATEJİLERİ

### Satıcı İçin

**Fiyatlandırma**:
```
1. Piyasa fiyatını araştır
2. Rekabetçi fiyat koy
3. Teklif sistemini aç (daha fazla alıcı)
4. Koruma bölgesinde kur (güvenlik)
```

**Stok Yönetimi**:
```
1. Sandığı düzenli kontrol et
2. Stok bitince yenile
3. Çok satılan item'ları stokla
4. Nadir item'ları yüksek fiyata sat
```

### Alıcı İçin

**Fiyat Karşılaştırma**:
```
1. Farklı marketleri gez
2. En uygun fiyatı bul
3. Teklif ver (daha uygun fiyat için)
4. Toplu alım yap (indirim için)
```

**Teklif Stratejisi**:
```
1. Alternatif item ile ödeme yap
2. Miktar artır (daha iyi teklif)
3. Birden fazla teklif ver (şans artır)
4. Sabırlı ol (satıcı kabul edebilir)
```

---

## ⚠️ ÖNEMLİ NOTLAR

### Market Kuralları

1. **Sandık Zorunlu**: Market için sandık gerekli
2. **Tabela Zorunlu**: Tabela olmadan market çalışmaz
3. **Stok Kontrolü**: Stok bitince satış yapılamaz
4. **Ödeme Kontrolü**: Yeterli ödeme yoksa satın alınamaz
5. **Teklif Limiti**: Maksimum 10 teklif per market

### Güvenlik

**Koruma Bölgesi**:
- Market koruma bölgesinde ise güvenli
- Vergi alınır ama güvenlik sağlanır
- Saldırıya karşı korumalı

**Normal Bölge**:
- Vergi yok ama güvenlik yok
- Saldırıya açık
- Riskli ama karlı

---

## 🎮 HIZLI MARKET REHBERİ

### İlk Market Kurma

```
1. Sandık koy
2. Satılacak item'ı koy (örn: 64 Demir)
3. Ödeme item'ını koy (örn: 10 Elmas)
4. Tabela yap: [SHOP] / Demir / 10 Elmas
5. Tabelayı sandığa yerleştir
6. Market hazır!
```

### İlk Alışveriş

```
1. Market sandığına sağ tık
2. GUI menü açılır
3. "Satın Al" butonuna tıkla
4. Item envantere eklenir
5. Ödeme otomatik alınır
```

### İlk Teklif

```
1. Market menüsünde "Teklif Ver" butonuna tıkla
2. Envanterinden item seç
3. Miktar ayarla
4. "Teklif Gönder" butonuna tıkla
5. Satıcıya bildirim gider
```

---

**🎮 Marketlerle ticaret yap, zengin ol, ekonomiye katıl!**

