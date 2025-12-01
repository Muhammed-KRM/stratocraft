# STRATOCRAFT - KONTRAT SİSTEMİ

## 📜 Kontrat Sistemi Nedir?

Kontratlar, oyuncular arasında **koda dayalı** anlaşmalardır. Sözleşmeyi bozan otomatik cezalandırılır!

---

## 📋 İÇİNDEKİLER

1. [Kontrat Oluşturma](#kontrat-oluşturma)
2. [Kan İmzası](#kan-imzasi)
3. [Kontrat Tipleri](#kontrat-tipleri)
4. [İhlal ve Ceza](#ihlal-ve-ceza)

---

## 📝 KONTRAT OLUŞTURMA

### Adım 1: Kontrat Kağıdı Hazırla

**Malz eme**: Named Paper (Örs'te isimlendir)

```
Örs'te Paper'a şartları yaz:

Örnek 1:
"64 Titanyum - 3 gün - 1000 Altın"

Örnek 2:
"Dragon öldür - 7 gün - 5000 Altın + Tarif Kitabı"

Örnek 3:
"Base koru - 24 saat - 500 Altın"
```

---

### Adım 2: İhale Panosuna As

**Panel Craft**:
```
[W][W][W]
[W][S][W]    W = Oak Planks
[W][W][W]    S = Sign

= Contract Board (Kontrat Panosu)
```

**Kullanım**:
```
1. Contract Board koy (klan bölgesine veya güvenli yere)
2. Eline kontrat kağıdı al
3. Board'a SAĞ TIK
4. Kontrat panoya asılır
5. Herkes görebilir
```

---

### Adım 3: Kontrat Kabul

**Kabul Eden**:
```
1. Contract Board'a sağ tık
2. Kontrat listesini gör
3. İstediğini seç
4. "Kabul Et" butonuna tık
5. Kan imzası gerekli (sonraki adım)
```

---

## 🩸 KAN İMZASI

### Mekanik

**Her İki Taraf Kan İle İmzalar**:
```
1. Kontrat kabul edildi
2. Her iki oyuncu -3 kalp can kaybeder (Blood signature)
3. Kontrat AKTİF olur
4. Süre başlar
```

**Görsel Efektler**:
```
- BLOOD partikülü (kırmızı)
- "KONTRAT İMZALANDI!" title
- Sözleşme numarası verilir (#12345)
```

---

## 📋 KONTRAT TİPLERİ

### 1. Malzeme Temini Kontratı

**Şablon**:
```
ŞART: 64 Titanyum getir
SÜRE: 3 gün
ÖDÜL: 1000 Altın

İhlal: Tazminat 1500 Altın
```

**İşleyiş**:
```
1. Kontrat imzalanır
2. Alıcı 3 gün içinde 64 Titanyum verir
3. Verer contract board'a koyar
4. Sistem otomatik kontrol eder
5. Doğruysa → 1000 Altın transfer
6. Yanlışsa/Süre bitti → Ceza
```

---

### 2. Boss Av Kontratı

**Şablon**:
```
ŞART: Titan Golem öldür
SÜRE: 7 gün
ÖDÜL: 5000 Altın + Tarif Kitabı

Kanıt: Karanlık Madde drop göster
```

**İşleyiş**:
```
1. Boss öldür
2. Dropu al (Karanlık Madde)
3. Contract board'a koy
4. Sistem doğrular
5. Ödül transfer
```

---

### 3. Koruma Kontratı

**Şablon**:
```
ŞART: Base'i 24 saat koru
SÜRE: 24 saat
ÖDÜL: 500 Altın/saat

İhlal: Base hasar alırsa ceza
```

**İşleyiş**:
```
1. Kontrat imzalanır
2. Koruma başlar
3. 24 saat boyunca base hasar almazsa → Ödül
4. Hasar alırsa → İhlal, ceza
```

---

### 4. İnşaat Kontratı

**Şablon**:
```
ŞART: 10x10 ev yap
SÜRE: 2 gün
ÖDÜL: 800 Altın

Kanıt: Screenshot + koordinat
```

---

## ⚖️ İHLAL VE CEZA

### İhlal Durumları

```
1. Süre Bitti + Şart Yerine Gelmedi
   → Otomatik ihlal

2. Yanlış Malzeme Verildi
   → İhlal

3. Kanıt Sunulamadı
   → İhlal

4. Koruma Başarısız
   → İhlal
```

---

### Otomatik Ceza Sistemi

#### **1. Hain Damgası (Traitor Tag)**

```
İsim Rengi KIRMIZI olur:
§c[HAIN] Oyuncu_Adı

Etki:
- Herkes görür
- Kimse güvenmez
- 7 gün sürer (veya tazminat ödeyene kadar)
```

---

#### **2. Otomatik Tazminat**

```
Bankadan Otomatik Çekilir:

Örnek:
Kontrat: 1000 Altın ödül
İhlal: 1500 Altın tazminat

Sistem:
1. İhlal eden oyuncunun bankasını kontrol et
2. 1500 Altın varsa → Otomatik çek
3. Karşı tarafa transfer
4. Mesaj: "Tazminat ödendi."
```

---

#### **3. Envanter Kilidi** (Para Yoksa)

```
Durum: Bankada para yok ama ihlal var

Ceza:
1. Envanteri KİLİTLENİR
2. Hiçbir şey düşüremez/kullanamaz
3. Sadece madencilik/loglama yapabilir
4. Kazandığı para otomatik kesilir
5. Tazminat tamam olana kadar sürer
```

**Görsel**:
```
Her login:
"§cKONTRAT BORCU: 1500 Altın kaldı!"
"§7Envanterin kilitli. Borcu öde."
```

---

## 🎯 KONTRAT STRATEJİLERİ

### Güvenli Kontrat Yazma

**İPUÇLARI**:
```
1. NET ŞART: "64 Titanyum" (belirsiz değil)
2. AÇIK SÜRE: "3 gün" (3 gün 0 saat 0 dakika)
3. TAZMİNAT EKLE: "İhlal: 1.5x ödül tazminat"
4. KANIT BELİRT: "Kanıt: Malzeme board'a koyulmalı"
```

---

### Tüccar Oyuncu İçin

**Para Kazanma**:
```
Strateji: Malzeme kontratları al

1. Contract board'ları tara
2. Kolay kontratları seç:
   - "64 Demir - 1 gün - 50 Altın"
   - "32 Odun - 2 saat - 20 Altın"
3. Hızlıca topla
4. Teslim et
5. Tekrarla

Günlük kazanç: 500-1000 Altın (safe)
```

---

### Klan İçin

**Büyük Kontratlar**:
```
Klan kontratı as:
"1000 Titanyum - 7 gün - 10,000 Altın"

Tek oyuncuya zor ama:
→ Takım halinde teslim edilir
→ Kar paylaşımı
→ Klan zenginleşir
```

---

## ⚠️ ÖNEMLİ NOTLAR

### Kontrat Kuralları

1. **İptal Edilemez**: İmzaladıktan sonra iptal YOK
2. **Kan Gerekli**: -3 kalp can kaybı (hazır ol)
3. **Tazminat Zorunlu**: İhlal = Otomatik ceza
4. **Hain Tag**: 7 gün boyunca kırmızı isim
5. **Envanter Kilidi**: Borç bitene kadar kilitli

---

### Güvenlik İpuçları

**Kontrat Almadan Önce**:
```
1. Şartları DİKKATLE oku
2. Süreyi kontrol et (yetişir mi?)
3. Tazminatı gör (ödeyebilir misin?)
4. Karşı tarafa güven (scam riski)
```

**Kontrat Verirken**:
```
1. NET yaz (belirsizlik yok)
2. Ödül = Makul (çok yüksek verme)
3. Tazminat = Caydırıcı (1.5x-2x)
4. Kanıt iste (screenshot, item vb.)
```

---

## 🎯 HIZLI KONTRAT REHBERİ

### Basit Malzeme Kontratı

```
1. Paper al
2. Örs'te isimlendir: "64 Iron - 1 day - 100 Gold"
3. Contract Board koy (klan bölgesine)
4. Board'a paper ile sağ tık
5. Bekle (birisi alana kadar)
```

### Kontrat Kabul Etme

```
1. Contract Board bul
2. Sağ tık → Kontrat listesi
3. Uygun olanı seç
4. "Kabul Et"
5. -3 kalp can kaybı (Kan imzası)
6. BAŞLA! (süre işliyor)
```

---

**🎮 Kontratlarla ticaret yap, güvenilir ol, zengin ol!**
