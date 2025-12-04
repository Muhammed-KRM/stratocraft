# STRATOCRAFT - DİĞER SİSTEMLER

## 📋 Bu Dokümantasyonda

Bu dokümanda 6 sistem var:
1. Görev Sistemi (Mission System)
2. Antrenman Sistemi (Training System)
3. Lojistik Sistemi (Logistics System)
4. Ekonomi Sistemi (Economy System)
5. Biyom Sistemi (Biome System) - **YENİ!**
6. Zindan Sistemi (Dungeon System) - **YENİ!**

**NOT**: İlk 4 sistem kodda mevcut ama **henüz tam implement edilmemiş** veya **basittir**. Biyom ve Zindan sistemleri **aktif ve çalışıyor**.

---

## 🎯 16. GÖREV SİSTEMİ (Mission System)

### Durum

**Kod Dosyası**: `MissionManager.java`, `MissionListener.java`, `Mission.java`, `MissionMenu.java`

**Mevcut Durum**: **✅ TAM İMPLEMENT EDİLMİŞ - ÇALIŞIYOR**

**KOD DOĞRULANDI**: Tüm görev tipleri, GUI menü, ilerleme takibi ve ödül sistemi aktif.

**GÜVENLİK**: Envanter kontrolü ve ödül yere düşme sistemi eklendi.

---

### 🎮 Nasıl Çalışır?

**Görev Loncası (Totem)**:
```
1. Totem (Totem of Undying) koy
2. Totem'e SAĞ TIK → Görev menüsü açılır
3. Yeni görev al veya aktif görevi görüntüle
4. Görevi tamamla
5. Menüden "Teslim Et" butonuna tıkla
6. Ödülü al!
```

---

### 📋 Görev Tipleri (8 Tip)

**1. Mob Avı (KILL_MOB)**
```
Hedef: Belirli mobları öldür
Örnek: "10 Zombie öldür"
İlerleme: Otomatik takip (EntityDeathEvent)
```

**2. Malzeme Toplama (GATHER_ITEM)**
```
Hedef: Belirli malzemeleri topla
Örnek: "64 Demir topla"
İlerleme: Otomatik takip (BlockBreakEvent)
```

**3. Lokasyon Ziyareti (VISIT_LOCATION)**
```
Hedef: Belirli bir koordinata git
Örnek: "X: 1000, Z: 2000 koordinatına git"
İlerleme: Otomatik takip (PlayerMoveEvent - 10 blok optimizasyonu)
```

**4. Yapı İnşa (BUILD_STRUCTURE)**
```
Hedef: Belirli bir yapıyı inşa et
Örnek: "Alchemy Tower inşa et"
İlerleme: Otomatik takip (BlockPlaceEvent)
```

**5. Oyuncu Avı (KILL_PLAYER)**
```
Hedef: Belirli bir oyuncuyu öldür
Örnek: "OyuncuX'i öldür"
İlerleme: Otomatik takip (PlayerDeathEvent)
```

**6. Item Craft (CRAFT_ITEM)**
```
Hedef: Belirli bir item craft et
Örnek: "Titanyum Kılıç craft et"
İlerleme: Otomatik takip (CraftItemEvent)
```

**7. Blok Kazma (MINE_BLOCK)**
```
Hedef: Belirli blokları kaz
Örnek: "50 Titanyum Ore kaz"
İlerleme: Otomatik takip (BlockBreakEvent)
```

**8. Mesafe Kat Etme (TRAVEL_DISTANCE)**
```
Hedef: Belirli mesafeyi kat et
Örnek: "1000 blok yol kat et"
İlerleme: Otomatik takip (PlayerMoveEvent - 10 blok optimizasyonu)
```

---

### 🎚️ Zorluk Seviyeleri

**Kolay (EASY)** - Seviye 1 oyuncular için:
```
- Düşük hedef miktarı
- Kısa süre (1-2 gün)
- Düşük ödül (100-500 Altın)
```

**Orta (MEDIUM)** - Seviye 2-3 oyuncular için:
```
- Orta hedef miktarı
- Orta süre (3-5 gün)
- Orta ödül (500-2000 Altın)
```

**Zor (HARD)** - Seviye 4-5 oyuncular için:
```
- Yüksek hedef miktarı
- Uzun süre (5-7 gün)
- Yüksek ödül (2000-5000 Altın)
```

**Uzman (EXPERT)** - Seviye 5+ oyuncular için:
```
- Çok yüksek hedef miktarı
- Çok uzun süre (7-10 gün)
- Çok yüksek ödül (5000-10000 Altın)
```

---

### 🖥️ GUI Menü Sistemi

**Görev Menüsü** (27 slot):
```
- Slot 0-8: İlerleme barı (yeşil/gri cam paneller)
- Slot 13: Görev bilgisi (tip, zorluk, hedef, süre)
- Slot 15: Ödül önizleme (item)
- Slot 22: "Teslim Et" butonu (görev tamamlandıysa)
- Slot 26: "Kapat" butonu
```

**Menü Özellikleri**:
- İlerleme barı: Görsel progress gösterimi
- Süre gösterimi: Kalan süre (gün/saat/dakika)
- Ödül önizleme: Para ve item ödülleri
- Otomatik güncelleme: İlerleme anlık güncellenir

---

### 💰 Ödül Sistemi

**Para Ödülü**:
```
- Zorluğa göre değişir
- Otomatik bankaya yatırılır (Vault)
- Görev tamamlandığında anında ödenir
```

**Item Ödülü**:
```
- Zorluğa göre rastgele item
- Envantere eklenir
- Eğer envanter doluysa yere düşer
```

---

### ⚙️ Otomatik İlerleme Takibi

**Event-Based Tracking**:
```java
// MissionListener.java
- EntityDeathEvent → KILL_MOB
- BlockBreakEvent → GATHER_ITEM, MINE_BLOCK
- PlayerMoveEvent → VISIT_LOCATION, TRAVEL_DISTANCE (10 blok optimizasyonu)
- BlockPlaceEvent → BUILD_STRUCTURE
- PlayerDeathEvent → KILL_PLAYER
- CraftItemEvent → CRAFT_ITEM
```

**Performans Optimizasyonu**:
```
- PlayerMoveEvent: Her 10 blokta bir kontrol (lag önleme)
- Chunk-based cache: Chunk bazlı veri saklama
- Event priority: NORMAL (diğer sistemlerle uyumlu)
```

---

### 🎯 Görev Stratejileri

**Yeni Başlayanlar İçin**:
```
1. Totem bul veya craft et
2. Kolay görevler al (EASY)
3. Malzeme toplama görevleri (en kolay)
4. Para biriktir
5. Zor görevlere geç
```

**Para Kazanma**:
```
- Günlük 5-10 görev yap
- Orta zorluk görevler (en verimli)
- Günlük kazanç: 2000-5000 Altın
```

**Klan İçin**:
```
- Tüm klan üyeleri görev yapsın
- Zor görevleri takım halinde tamamla
- Klan kasasına para aktar
```

---

### ⚠️ ÖNEMLİ NOTLAR

**Görev Kuralları**:
1. **Tek Aktif Görev**: Aynı anda sadece 1 görev aktif
2. **Süre Sınırı**: Süre dolduğunda görev iptal olur
3. **Otomatik Takip**: İlerleme otomatik güncellenir
4. **GUI Menü**: Totem'e sağ tık ile menü açılır
5. **Ödül Anında**: Görev tamamlandığında ödül anında verilir

**Performans**:
- PlayerMoveEvent optimizasyonu: Her 10 blokta bir kontrol
- Chunk-based cache: Performans için chunk bazlı veri saklama
- Event priority: NORMAL (diğer sistemlerle uyumlu)
- Blok değişimi kontrolü: Sadece blok değiştiğinde işlem yapılır

### 🔒 Güvenlik Özellikleri

**Ödül Sistemi**:
- Envanter kontrolü: Ödül verilmeden önce envanter kontrol edilir
- Yere düşme: Envanter doluysa ödül yere düşer
- Uyarı mesajı: Oyuncuya envanter durumu bildirilir

**Örnek Senaryo**:
```
1. Görev tamamlandı
2. Envanter kontrolü yapılır
3. Doluysa: Ödül yere düşer + "Envanterin dolu! Ödül yere düştü." mesajı
4. Boşsa: Ödül envantere eklenir
```

---

## 🏋️ 17. ANTRENMAN SİSTEMİ (Training System)

### Durum

**Kod Dosyası**: `TrainingManager.java`

**Mevcut Durum**: **Basit - Minimal implement**

**Şu an**: Çok basit bir training mekanizması var.

### Beklenen İşleyiş

**Antrenman Kukla Sistemi**:
```
1. Antrenman Kukla kur
2. Vurmadukça skill kazan
3. Progressively güçlen
```

**NOT**: Şu an **tam çalışmıyor**. Kodda var ama aktif değil.

---

## 📦 18. LOJİSTİK SİSTEMİ (Logistics System)

### Durum

**Kod Dosyası**: `LogisticsManager.java`, `LogisticsListener.java`

**Mevcut Durum**: **Basit - Kervan ile entegre**

**Şu an**: Lojistik sistemi **Kervan Sistemi** ile birleştirilmiş.

### İşleyiş

**Kervan = Lojistik**:
```
Kervan Sistemi (12_kervan_sistemi.md) dokümantasyonuna bak.

Lojistik:
- Manyetik Ray (Mağ-Rail) - Yapılarda var
- Teleporter - Yapılarda var
- Kervan - Kervan dokümantasyonunda

Tüm lojistik bu sistemlerle yapılıyor.
```

**NOT**: Ayrı bir lojistik sistemi yok, **mevcut sistemler kullanılıyor**.

---

## 💰 19. EKONOMİ SİSTEMİ (Economy System)

### Durum

**Kod Dosyası**: `EconomyManager.java`

**Mevcut Durum**: **Vault Entegrasyonu**

**Şu an**: **Vault** plugin kullanılıyor ekonomi için.

### İşleyiş (KOD DOĞRULANDI)

**Vault Sistemi**:
```java
// EconomyManager.java
// Vault kullanarak para yönetimi:
- depositPlayer(Player, amount) → Para yatır
- withdrawPlayer(Player, amount) → Para çek
- getBalance(Player) → Bakıye gör
```

**Kullanım Yerleri**:
- Kontratlar → Ödül/ceza transferi
- Kervanlar → Kazanç
- Görevler → Ödül (gelecekte)
- Klan Kasası → Klan parası

**Para Kazanma Yolları** (Oyunda):
1. **Kontratlar**: Görevleri tamamla → Para kazan
2. **Kervanlar**: Malzeme taşı → x1.5 değer
3. **Boss Dropları**: Sat → Para kazan
4. **Ticaret**: Oyuncular arası

**Harcama Yerleri**:
1. Kontrat açma (ödül koyma)
2. Kervan maliyeti (gelecekte)
3. Yapı upgrade (gelecekte)
4. Özel eşya craftı (gelecekte)


---

## 🌲 20. BİYOM SİSTEMİ (Biome System)

### Durum

**Kod Dosyası**: `BiomeManager.java`

**Mevcut Durum**: **Aktif - Zorluk bazlı biyomlar**

### İşleyiş

**Zorluk Seviyesine Göre Biyom Değişimi**:
```
Seviye 1 (200-1000 blok):
→ Forest, Plains, Birch Forest

Seviye 2 (1000-3000 blok):
→ Taiga, Swamp, Dark Forest

Seviye 3 (3000-5000 blok):
→ Jungle, Savanna, Badlands

Seviye 4 (5000-10000 blok):
→ Nether Wastes, Soul Sand Valley, Crimson Forest

Seviye 5 (10000+ blok):
→ End Barrens, End Highlands, The End
```

**Biyom-Specific Özellikler**:
- Her biyomda farklı yapılar spawn olur
- Biyoma özel moblar spawn olur
- Biyom değişimi chunk generation sırasında olur

**Config Ayarları**:
```yaml
biomes:
  enabled: true
  custom-biomes:
    level1: [FOREST, PLAINS, BIRCH_FOREST]
    level2: [TAIGA, SWAMP, DARK_FOREST]
    # vb.
```

---

## 🏰 21. ZİNDAN SİSTEMİ (Dungeon System)

### Durum

**Kod Dosyası**: `DungeonManager.java`

**Mevcut Durum**: **Aktif - Yeraltı zindanları**

### İşleyiş

**Otomatik Zindan Spawn**:
```
Spawn Şansı: %5 (her chunk için)
Konum: Yeraltı (Y: 10-50 arası)
Boyut: Zorluk seviyesine göre değişir
```

**Zorluk Seviyesine Göre Zindanlar**:
```
Seviye 1: Basit mağara zindanı
Seviye 2: Taş tuğla zindan
Seviye 3: Karanlık kale
Seviye 4: Nether kalesi
Seviye 5: End şehri
```

**Zindan İçeriği**:
```
1. Mob Spawn:
   - Zorluk seviyesine göre moblar
   - Daha fazla mob = Daha yüksek seviye

2. Loot Sandıkları:
   - Zorluk seviyesine göre ödüller
   - Tarif Kitapları (nadir)
   - Özel eşyalar

3. Yapılar:
   - Tuzaklar
   - Gizli odalar
   - Boss odası (yüksek seviyelerde)
```

**Zindan Tipleri** (Config'den):
```
- cave_dungeon (Seviye 1-2)
- fortress (Seviye 3)
- nether_fortress (Seviye 4)
- end_city (Seviye 5)
```

**Manuel Spawn** (Admin):
```
/stratocraft dungeon spawn <level> <type>

Örnek:
/stratocraft dungeon spawn 3 fortress
```

---

## ⚠️ ÖNEMLİ NOT


### Bu Sistemler Neden Basit?

**Cevap**: Oyun **fiziksel mühendisliğe** odaklanmış. Komut yok, her şey blok düzenekleriyle.

**Öncelik**:
1. ✅ Klan Sistemi (complete)
2. ✅ Batarya Sistemi (complete)
3. ✅ Tuzak Sistemi (complete)
4. ✅ Feladetler (complete)
5. ✅ Kuşatma (complete)
6. ❌ Görev/Training/vb. (basit/placeholder)

**Gelecek Güncellemeler**: Bu sistemler daha sonra genişletilebilir.

---

## 🎯 HIZLI REFERANS

### Hangi Sistemi Kullanmalıyım?

**Para Kazanmak İçin**:
→ **Kontratlar** (11_kontrat_sistemi.md)
→ **Kervanlar** (12_kervan_sistemi.md)
→ **Supply Drop** (14_supply_drop.md)

**Lojistik İçin**:
→ **Kervan** (12_kervan_sistemi.md)
→ **Manyetik Ray** (07_yapilar.md - Yapılar)
→ **Teleporter** (07_yapilar.md - Yapılar)

**Ekonomi İçin**:
→ Vault plugin (sunucu admin ayarlasın)
→ Klan kasası sistemini kutanılan

---

**🎮 Mevcut sistemleri kullan, gelecek güncellemeleri bekle!**
