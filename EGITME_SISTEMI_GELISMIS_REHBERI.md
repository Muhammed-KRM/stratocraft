# 🐾 Gelişmiş Eğitme Sistemi - Detaylı Rehber

## 📋 Genel Bakış

Stratocraft'ta **gelişmiş eğitme sistemi** artık sahiplik, klan kontrolü, dişi/erkek sistemi ve çiftleştirme özelliklerine sahip!

### Yeni Özellikler:
- ✅ **Sahiplik ve Klan Kontrolü** - Sadece eğiten ve klanı kullanabilir
- ✅ **Shift+Sağ Tık** - Takip edilecek kişi belirleme
- ✅ **Koruma Sistemi** - Takip edilen kişiyi köpekler gibi koruma
- ✅ **Dişi/Erkek Sistemi** - Cinsiyet işaretleri ve metadata
- ✅ **Çiftleştirme** - Memeli ve yumurtlayan canlılar
- ✅ **Çiftleştirme Tesisi** - Seviyeye göre üretim
- ✅ **Admin Komutları** - Anında eğitme ve çiftleştirme

---

## 🎯 Sahiplik ve Klan Kontrolü

### Kullanım Kuralları:
- **Sahip:** Eğitilen canlıyı tam kontrol edebilir
- **Klan Üyeleri:** Aynı klan üyeleri canlıyı kullanabilir
- **Diğerleri:** Canlıyı kullanamaz

### Shift+Sağ Tık ile Takip:
1. Eğitilmiş canlıya **Shift+Sağ tık** yap
2. Canlı artık seni takip eder
3. Sadece aynı klan üyeleri takip edilebilir

**Örnek:**
```
1. Eğitilmiş canlıya Shift+Sağ tık
2. "§a§lCanlı artık seni takip ediyor!" mesajı
3. Canlı seni takip etmeye başlar
```

---

## 🛡️ Koruma Sistemi

Eğitilmiş canlılar **takip ettikleri kişiyi** köpekler gibi korur:

### Özellikler:
- Takip edilen kişiye saldırıldığında canlı saldırıcıya saldırır
- Canlı **oturtulmamışsa** (binilmemişse) koruma aktif
- Aynı klan üyelerine saldırmaz
- Mob saldırılarına da tepki verir

**Örnek Senaryo:**
```
1. Oyuncu A, eğitilmiş canlıyı takip ettirir
2. Oyuncu B, Oyuncu A'ya saldırır
3. Eğitilmiş canlı (oturtulmamışsa) Oyuncu B'ye saldırır
4. "§a§l[Canlı İsmi] seni koruyor!" mesajı
```

---

## ♂️♀️ Dişi/Erkek Sistemi

### Cinsiyet Göstergeleri:
- **Erkek:** `§b♂` (Mavi işaret)
- **Dişi:** `§d♀` (Pembe işaret)

### Cinsiyet Belirleme:
- Eğitilirken **rastgele** cinsiyet atanır
- İsim yanında **cinsiyet işareti** görünür
- Metadata'da saklanır

**Örnek İsimler:**
```
Ork §b♂ §7[Eğitilmiş]  (Erkek)
Ejderha §d♀ §7[Eğitilmiş]  (Dişi)
```

---

## 💑 Çiftleştirme Sistemi

### İki Yöntem:

#### 1. Doğal Çiftleştirme (Yemek Verme)
- **Yöntem:** Eğitilmiş canlılara **yemek ver**
- **Gereksinimler:**
  - 1 dişi + 1 erkek canlı
  - Aynı sahip
  - Yakında olmalılar
- **Süre:** 1 dakika
- **Sonuç:** Memeli → Yavru, Yumurtlayan → Yumurta

#### 2. Çiftleştirme Tesisi
- **Yöntem:** Beacon bloğu üzerinde tesis kur
- **Gereksinimler:**
  - 1 dişi + 1 erkek canlı
  - Yeterince yiyecek (3+ blok)
  - Seviyeye göre süre (1-5 gün)
- **Sonuç:** Memeli → Yavru, Yumurtlayan → Yumurta

---

## 🏭 Çiftleştirme Tesisi

### Oluşturma:
1. **Beacon bloğu** yerleştir
2. `/stratocraft tame facility create <level>` (admin)
3. Tesise **1 dişi + 1 erkek** canlı getir
4. **Yiyecek** bırak (3+ blok)
5. Çiftleştirme otomatik başlar

### Seviyeler ve Süreler:
| Seviye | Süre |
|--------|------|
| 1 | 1 gün |
| 2 | 2 gün |
| 3 | 3 gün |
| 4 | 4 gün |
| 5 | 5 gün |

### Yiyecek Blokları:
- Hay Balesi (HAY_BLOCK)
- Havuç (CARROTS)
- Buğday (WHEAT)
- Pancar (BEETROOTS)
- Patates (POTATOES)
- Karpuz (MELON)
- Balkabağı (PUMPKIN)

**Minimum:** 3 yiyecek bloğu (3x3 alan içinde)

---

## 🐣 Memeli vs Yumurtlayan

### Memeli Canlılar:
- **Örnekler:** Ork, Troll, Goblin, Minotaur, Savaş Ayısı, Kurt Adam
- **Çiftleştirme Sonucu:** Direkt **yavru** spawn olur
- **Yavru:** Otomatik eğitilmiş olur (parent'ın sahibi)

### Yumurtlayan Canlılar:
- **Örnekler:** Ejderha, Griffin, Phoenix, Wyvern, Hydra, T-Rex, Harpy
- **Çiftleştirme Sonucu:** **Yumurta** spawn olur (kaplumbağa mantığı)
- **Yumurta:** Çatladığında yavru spawn olur ve otomatik eğitilmiş olur

---

## 🎮 Admin Komutları

### Anında Eğitme:
```bash
/stratocraft tame instant <entity>
```
- Yakındaki canlıya bak ve komutu kullan
- Canlı anında eğitilir

### Anında Çiftleştirme:
```bash
/stratocraft tame breed <female> <male>
```
- Yakındaki dişi ve erkek canlıya bak
- Çiftleştirme anında tamamlanır

### Çiftleştirme Tesisi:
```bash
# Tesis oluştur
/stratocraft tame facility create <level>

# Süreyi anında bitir
/stratocraft tame facility complete
```
- Beacon bloğuna bak ve komutu kullan

---

## 📊 Kullanım Senaryoları

### Senaryo 1: Klan Koruması
```
1. Klan lideri bir ejderha eğitir
2. Klan üyesi Shift+Sağ tık ile ejderhayı takip ettirir
3. Düşman saldırırsa ejderha korur
```

### Senaryo 2: Çiftleştirme
```
1. 1 dişi + 1 erkek ork eğit
2. İkisine de yemek ver
3. 1 dakika sonra yavru ork doğar
4. Yavru otomatik eğitilmiş olur
```

### Senaryo 3: Çiftleştirme Tesisi
```
1. Beacon bloğu yerleştir
2. /stratocraft tame facility create 3
3. 1 dişi + 1 erkek ejderha getir
4. Yiyecek bırak
5. 3 gün sonra yumurta oluşur
6. Yumurta çatladığında yavru ejderha doğar
```

---

## 🔧 Teknik Detaylar

### Metadata:
- `Tamed`: Eğitilmiş mi?
- `TamedOwner`: Sahip UUID
- `TamedGender`: Cinsiyet (MALE/FEMALE)
- `FollowingTarget`: Takip edilecek kişi UUID

### Dosya Kayıtları:
- `tamed_creatures.yml`: Eğitilmiş canlılar
- Cinsiyet ve takip bilgileri kaydedilir

### AI Davranışı:
- Eğitilmiş canlılar **Tameable** interface'ini kullanır
- Sahibini veya takip edilecek kişiyi takip eder
- Koruma sistemi **EntityDamageByEntityEvent** ile çalışır

---

## ⚠️ Önemli Notlar

1. **Sahiplik:** Sadece eğiten ve klanı kullanabilir
2. **Takip:** Shift+Sağ tık ile belirlenir, sadece aynı klan
3. **Koruma:** Oturtulmamış canlılar korur
4. **Çiftleştirme:** Aynı sahip olmalı, karşı cins olmalı
5. **Yavru:** Otomatik eğitilmiş olur (parent'ın sahibi)

---

**İyi eğitimler! 🐾**

