# STRATOCRAFT - ARAŞTIRMA SİSTEMİ

## 📚 Araştırma Sistemi Nedir?

Araştırma, **çok güçlü eşyaları** craft etmen için gereken **Tarif Kitabı** sistemidir.

**KOD DOĞRULANDI**: ResearchManager.java'dan tüm mekanikler doğrulanmıştır.

---

## 📋 İÇİNDEKİLER

1. [Tarif Kitabı Sistemi](#tarif-kitabi-sistemi)
2. [Araştırma Masası](#araştirma-masasi)
3. [Gerekli/Gereksiz Tarifler](#gerekli-gereksiz-tarifler)

---

## 📖 TARİF KİTABI SİSTEMİ

### Nedir?

**Tariffler Kitabı** = Güçlü eşyalar için zorunlu bilgi

**Kural**: Sadece **çok güçlü** eşyalar için gerekli

---

### Nasıl Bulunur?

**Kaynaklar**:
```
1. Boss Dropları:
   - Titan Golem → "Tarif: Tektonik Sabitleyici"
   - Phoenix → "Tarif: Alev Amplifikatörü"
   - Lich → "Tarif: Karanlık Madde Silahları"

2. Görev Loncası:
   - Elmas Totem görevleri
   - Ödül: Rastgele tarif

3. Köylü Takası:
   - Çok nadir
   - Çok pahalı

4. Supply Drop:
   - %2 şans
   - Rastgele tarif
```

---

## 🔬 ARAŞTIRMA MASASI

### Kurulum (Kod Doğrulandı)

**Yapı**:
```
Çalışma Masası (Crafting Table)
       ↓ Üstüne
Kürsü (Lectern) koy
```

**Kullanım** (KOD DOĞRULANDI):
```java
// ResearchManager.java satır 18-30
// Yakındaki (10 blok) Lectern'leri tara
for (BlockState state : chunk.getTileEntities()) {
    if (state instanceof Lectern) {
        if (lectern.getLocation().distance(player.getLocation()) <= 10) {
            // Kürsüdeki kitabı kontrol et
            ItemStack book = lectern.getInventory().getItem(0);
            if (ItemManager.isCustomItem(book, fullId)) {
                return true; // Tarif var!
            }
        }
    }
}
```

**Menzil**: **10 blok** yarıçap

---

### Nasıl Çalışır?

**Adımlar**:
```
1. Tarif Kitabını bul (Boss, görev vb.)
2. Araştırma Masası kur:
   - Crafting Table + Lectern
3. Kitabı Lectern'e koy
4. 10 blok yarıçapta herkes o tarifi kullanabilir
5. Kitap olmadan craft EDİLEMEZ
```

**Örnek**:
```
Durum: "Tektonik Sabitleyici" yapmak istiyorsun

Adımlar
:
1. Titan Golem yok et
2. "Tarif: Tektonik Sabitleyici" düşür
3. Kitabı Lectern'e koy
4. Malzemeleri topla (Titanyum, Piston vb.)
5. 10 blok yarıçapta craft yap
6. TEKTONİK SABİTLEYİCİ ELDE ET!

Kitap olmadan:
→ Malzemen olsa bile YAPAMAZSIN
```

---

## ⚖️ GEREKLİ/GEREKSİZ TARİFLER

### Tarif Kitabı GEREKLİ (Çok Güçlü Eşyalar)

**Yapılar**:
```
- Tektonik Sabitleyici (Felaket kalkanı)
- Manyetik Ray İstasyonu
- Otomatik Taret
- Force Field Jeneratörü
- Teleporter
```

**Silahlar**:
```
- Karanlık Madde Kılıcı
- Ejderha Yayı
- Lazer Silahları
- Netherite+ Zırhlar
```

**Özel Eşyalar**:
```
- Alev Amplifikatörü
- Tower Shield (Kule Kalkanı)
- Titan Kancası
- Adamantite Zırh Seti
- Ozon Kalkanı (Güneş Fırtınasına karşı)
```

---

### Tarif Kitabı GEREKMİYOR (Normal Eşyalar)

**Temel Yapılar**:
```
- Basit savunma yapıları (Taş/Demir seviye)
- Simya Kulesi (Lv1-2)
- Gözetleme Kulesi (Lv1)
```

**Bataryalar**:
```
- Ateş Topu (Magma Block)
- Toprak Suru
- Basit büyüler
```

**Silahlar**:
```
- Titanyum Kılıç/Zırh
- Normal Enchanted gear
- Vanilla silahlar
```

**Klan Eşyaları**:
```
- Klan Çiti
- Klan Kristali
- Tuzak Çekirdeği
```

---

### Nasıl Anlarım Gerekli Mi?

**Kural**:
```
IF Felaket dropu OR Efsanevi güç OR Tarif Kitabı yazdıysa:
    → Tarif Kitabı GEREKLİ
ELSE:
    → Normal craft
```

**Örnek**:
```
"Tektonik Sabitleyici":
→ Felaket hasarını %99 azaltır
→ ÇOK GÜÇLÜ
→ Tarif GEREKLİ

"Titanyum Kılıç":
→ Normal güçlü silah
→ Tarif GEREKMİYOR
```

---

## 🎯 ARAŞTIRMA STRATEJİSİ

### Tarif Avcılığı

**Öncelik Sırası**:
```
1. "Tektonik Sabitleyici" (Felaket koruması - ZORUNLU)
2. "Otomatik Taret" (Savunma - ÖNEMLİ)
3. "Titan Kancası" (Mobilite - Faydalı)
4. "Karanlık Madde Silahları" (DPS - Lüks)
```

**Toplama Yöntemi**:
```
Boss Farming:
→ Titan Golem tekrar tekrar öldür
→ %10 tarif dropu
→ 10 kill = 1 tarif (ortalama)

Görev:
→ Elmas Totem görevleri yap
→ Rastgele tarif ödülü

Supply Drop:
→ Her drop'a koş
→ %2 şans
→ Şans işi
```

---

### Araştırma Merkezi Kurma

**Klan İçin**:
```
Merkezi Araştırma Odası:
1. 10x10 oda yap
2. Ortaya Crafting Table + Lectern
3. Tüm tarif kitaplarını buraya koy
4. Klan üyeleri 10 blok içinde craft yapar

Avantaj:
→ Tek merkezden erişim
→ Organizlekitaplı
```

---

## ⚠️ ÖNEMLİ NOTLAR

### Tarif Sistemi Kuralları

**10 Blok Menzil** (KOD DOĞRULANDI):
```java
// ResearchManager.java satır 23
if (lectern.getLocation().distance(player.getLocation()) <= 10)
```
- Lectern'dan **10 blok** uzakta craft yapamazsın
- Yakın ol!

**Lectern Zorunlu**:
```
Envanterdeki kitap ÇALIŞMAZ!
Mutlaka Lectern'e koymalısın.
```

**Kitap Kaldırma**:
```
Lectern'den kitabı alırsan:
→ Tarif kaybolur
→ Craft yapılamaz
→ Tekrar koy
```

---

## 🎯 HIZLI ARAŞTIRMA REHBERİ

### İlk Tarif (Yeni Başlayanlar)

```
Hedef: İlk tarif kitabını bul

Yöntem:
1. Görev Loncası kur (Taş Todem)
2. Basit görevleri yap (10-20 görev)
3. Elmas Totem'e upgrade
4. Zor görevleri yap
5. Ödül: İlk tarif kitabın!

Süre: 2-3 gün
Zorluk: Orta
```

### Tam Araştırma (Pro)

```
Hedef: Tüm tarifleri topla

Yöntem:
1. Boss farming (Titan Golem)
   - 50-100 kill
   - 5-10 tarif
2. Supply Drop avcılığı
   - Her drop'a git
   - Rastgele tarifler
3. Görev spam
   - Sürekli görev yap
   - Tarif ödülleri

Süre: 1-2 ay
Koleksiyon: %80-90 tarifler
```

---

**🎮 Tarifleri topla, en güçlü eşyaları yap, rakiplerini geride bırak!**
