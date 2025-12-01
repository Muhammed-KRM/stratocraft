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

**Kod Dosyası**: `MissionManager.java`, `MissionListener.java`, `Mission.java`

**Mevcut Durum**: **Placeholder - Henüz tam implement edilmemiş**

**Şu an**: Basit bir görev yapısı var ama tam çalışmıyor.

###Expectations Beklenen İşleyiş (Kod'dan)

**Görev Loncasıgibi çalışacaktı**:
```
1. Totem koy
2. Sağ tık → Görev al
3. Görevi tamamla
4. Ödül kazantanı

Görev Tipleri:
- Malzeme getir (örn: 64 Demir)
- Mob öldür (örn: 10 Zombie)
- Boss öldür (örn: 1 Titan Golem)
```

**Ödüller**:
- Para (Gold)
- Özel eşyalar
- Tarif Kitapları

**NOT**: Şu an **kullanılabilir değil**. Gelecekte güncellenebilir.

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
