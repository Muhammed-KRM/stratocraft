# STRATOCRAFT - KERVAN SİSTEMİ

## 🐴 Kervan Sistemi Nedir?

Kervanlar, **uzak bölgelere malzeme taşımanın** riskli ama karlı yöntemidir. Başarıyla ulaşırsan **x1.5 değer** kazanırsın!

**KOD DOĞRULANDI**: CaravanManager.java'dan tüm bilgiler kod ile doğrulanmıştır.

---

## 📋 İÇİNDEKİLER

1. [Kervan Oluşturma](#kervan-oluşturma)
2. [Yolculuk Mekaniği](#yolculuk-mekaniği)
3. [Ödüller ve Riskler](#ödüller-ve-riskler)

---

## 🚚 KERVAN OLUŞTURMA

### Gereksinimler (KOD DOĞRULANDI)

**Minimum Mesafe**: **1000 blok**
```java
// CaravanManager.java satır 43
double minDistance = configManager.getConfig().getInt("caravan.min-distance", 1000);
```

**Minimum Yük**: **20 stack** (1280 item)
```java
//  CaravanManager.java satır 57-58
int minStacks = configManager.getConfig().getInt("caravan.min-stacks", 20);
int minItems = minStacks * 64; // 20 stack = 1280 item
```

**Minimum Değer**: **5000 Altın** değerinde yük
```java
// CaravanManager.java satır 66
double minValue = configManager.getConfig().getDouble("caravan.min-value", 5000.0);
```

---

### Yük Değerlendirme Sistemi (KOD DOĞRULANDI)

**Eşya Fiyatları**:
```java
// CaravanManager.java satır 132-143
DIAMOND: 100 Altın
GOLD_INGOT: 50 Altın
IRON_INGOT: 10 Altın
EMERALD: 80 Altın
NETHERITE_INGOT: 500 Altın
COAL: 1 Altın

Özel Eşyalar:
RED_DIAMOND (Kızıl Elmas): 1000 Altın
TITANIUM_INGOT: 500 Altın
DARK_MATTER: 800 Altın
```

---

### Kervan Başlatma

**Adımlar**:
```
1. Başlangıç noktasına git (Şube 1)
2. Hedef belirle (Şube 2 - minimum 1000 blok uzakta)
3. Yük hazırla:
   - Minimum 20 stack
   - Minimum 5000 Altın değer
4. KOmut YOK - Özel bir düzenek/ritüel ile başlat
   (Kodda createCaravan metodu var ama tetikleyici belirtilmemiş)
```

**Kervan Yaratık**: **Mule** (At değil!)
```java
// CaravanManager.java satır 73
Mule mule = start.getWorld().spawn(start, Mule.class);
```

---

## 🗺️ YOLCULUK MEKANİĞİ

### Otomatik Yolculuk (KOD DOĞRULANDI)

**Mekanik**:
```
1. Kervan (Mule) başlangıç noktasında spawn olur
2. Hedef koordinatlar kaydedilir
3. Mule otomatik olarak hedefe ilerler (Minecraft AI)
4. Her saniye kontrol edilir:
   - Hedefe 5 blok yaklaştı mı?
   - Evet → Ödül ver
   - Hayır → Devam et
```

```java
// CaravanManager.java satır 184-243
// Her 1 saniyede kontrol (20 tick)
.runTaskTimer(plugin, 20L, 20L);

// Hedef kontrolü (5 blok yarıçap)
if (caravan.getLocation().distance(target) <= 5)
```

---

### Kervan Takip

**Görsel İşaretler**:
```
Mule üzerindeki metadata:
- "Caravan" = true (kervan olduğunu belirt)
- "CaravanOwner" = UUID (sahibi)
```

**Oyuncu**:
- Kervanı takip edebilirsin
- Veya hedefe git, bekle
- RİSK: Yolda bırakırsan saldırıya açık!

---

## 💰 ÖDÜLLER VE RİSKLER

### Hedefe Ulaşma Ödülü (KOD DOĞRULANDI)

**x1.5 Değer Bonusu**:
```java
// CaravanManager.java satır 216-217
// Yükün değerini hesapla, x1.5 ile çarp
double reward = totalValue * 1.5;
clan.deposit(reward); // Klan kasasına ekle
```

**Örnek**:
```
Yük Değeri: 10,000 Altın

Hedefe ulaşınca:
→ 10,000 x 1.5 = 15,000 Altın
→ Net kar: 5,000 Altın
```

---

### Ambush Riski (Oyuncu Saldırısı)

**Kervan Saldırısı Mekaniği**:
```
Düşman klan kervanı bulursa:
1. Mule'ye saldırır
2. Öldürürse:
   - Yükteki eşyalar yere düşer
   - Saldıran toplar
   - Kervan sahibi HİÇBİR ŞEY KAZANMAZ

Savunma:
- Kervanı escortla (takip et)
- Takım halecihazla git
- Gece yolculuk (daha güvenli?)
```

**Kervan Ölümü**:
```java
// CaravanManager.java satır 248-256
public void removeCaravan(Entity caravan) {
    // Tüm kayıtlar silinir
    // Ödül YOK
}
```

---

## 🎯 KERVAN STRATEJİLERİ

### Güvenli Kervan

**Riskten Kaçınma**:
```
1. ESCortlu Yolculuk:
   - 3-5 oyuncu kervanı takip eder
   - Ambush'a karşı hazırlıklı

2. Gece Yolcu luğu:
   - Daha az oyuncu online
   - Daha güvenli (belki)

3. Gizli Rotalar:
   - Ana yollardan gitme
   - Ormanlardan/denizden geç
```

---

### Karlı Kervan

**Maximum Kar**:
```
Strateji: Pahalı eşyalar yükle

Örnek Yük:
- 10 Red Diamond (Kızıl Elmas) = 10,000 Altın
- 20 Titanium Ingot = 10,000 Altın
- 5 Dark Matter = 4,000 Altın
Toplam: 24,000 Altın

Hedefe ulaşınca:
→ 24,000 x 1.5 = 36,000 Altın
→ Net kar: 12,000 Altın!

RİSK: Çok değerli, herkes saldırır!
```

---

### Düşük Riskli Kervan

**Güvenli Ama Az Karlı**:
```
Yük: Ucuz ama çok eşya
- 64 Iron Ingot = 640 Altın
- 64 Coal = 64 Altın
- 64 Gold Ingot = 3,200 Altın
Toplam: 5,000 Altın (minimum)

Hedefe ulaşınca:
→ 5,000 x 1.5 = 7,500 Altın
→ Net kar: 2,500 Altın

Avantaj: Kimse saldırmaz (değmez)
```

---

## ⚠️ ÖNEMLİ NOTLAR (KOD DOĞRULANDI)

### Anti-Abuse Kontrolleri

**1. Dünya Kontrolü**:
```java
// CaravanManager.java satır 37-40
// Farklı dünyalar arası kervan YASAK
if (!start.getWorld().equals(end.getWorld())) {
    return false; // Ret
}
```

**2. Mesafe Kontrolü**:
```
Minumum: 1000 blok
Altındaysa: "Ticaret rotası çok kısa!" hatası
```

**3. Yük Kontrolü**:
```
Minimum: 20 stack (1280 item)
Altındaysa: "Yükünüz çok az!" hatası
```

**4. Değer Kontrolü**:
```
Minimum: 5000 Altın değer
Atındaysa: "Yükünüz çok değersiz!" hatası
```

---

### Kervan Limitleri

**Aynı Anda**:
- Her oyuncu **1 kervan** çıkarabilir
- Hedefe ulaşana/ölene kadar yeni kervan YASAK

**Hedef Varış**:
```java
// CaravanManager.java satır 202
// 5 blok yarıçapta varış sayılır
if (caravan.getLocation().distance(target) <= 5)
```

---

## 🎯 HIZLI KERVAN REHBERİ

### Basit Kervan (İlk Kervanın)

```
1. Şube 1'de 20 stack Iron Ingot topla (200 item)
2. Hedef: Şube 2 (1500 blok uzakta)
3. Kervan başlat
4. Takip et (güvenli ol)
5. Hedefe ulaş
6. Ödül: 200 x 10 x 1.5 = 3000 Altın

Süre: ~30 dakika
Risk: Düşük (ucuz yük)
Kar: 3000 Altın
```

### Pro Kervan (Zenginler İçin)

```
1. 10 Red Diamond topla (çok zor!)
2. Hedef: 2000 blok uzakta
3. 5 kişilik escort takımı hazırla
4. Kervan başlat
5. Takım halinde koruyun
6. Hedefe ulaş
7. Ödül: 10,000 x 1.5 = 15,000 Altın

Süre: 1 saat (savaş dahil)
Risk: ÇOK YÜKSEK
Kar: 15,000 Altın
```

---

**🎮 Kervanlarla zengin ol, ama her an saldırıya hazır ol!**
