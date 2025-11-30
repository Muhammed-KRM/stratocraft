# 📚 Stratocraft Plugin - Kapsamlı Kullanım Kılavuzu

**Versiyon:** 10.0  
**Minecraft:** 1.20.4  
**Sunucu:** Paper/Spigot

---

## 📋 İçindekiler

1. [Giriş](#giriş)
2. [Klan Sistemi](#klan-sistemi)
3. [Bölge (Territory) Sistemi](#bölge-territory-sistemi)
4. [Ritüel Sistemi](#ritüel-sistemi)
5. [Özel Eşyalar](#özel-eşyalar)
6. [Özel Moblar](#özel-moblar)
7. [Yapılar (Structures)](#yapılar-structures)
8. [Tuzak Sistemi](#tuzak-sistemi)
9. [Kuşatma Sistemi](#kuşatma-sistemi)
10. [Felaket Sistemi](#felaket-sistemi)
11. [Kontrat Sistemi](#kontrat-sistemi)
12. [Kervan Sistemi](#kervan-sistemi)
13. [Batarya Sistemi](#batarya-sistemi)
14. [Özel Eşyalar (Araçlar)](#özel-eşyalar-araçlar)
15. [Malzeme Düşüşü (Supply Drop)](#malzeme-düşüşü-supply-drop)
16. [Araştırma Sistemi](#araştırma-sistemi)
17. [Görev Sistemi](#görev-sistemi)
18. [Antrenman Sistemi](#antrenman-sistemi)
19. [Lojistik Sistemi](#lojistik-sistemi)
20. [Ekonomi Sistemi](#ekonomi-sistemi)
21. [Admin Komutları](#admin-komutları)

---

## 🎮 Giriş

Stratocraft, Minecraft için geliştirilmiş kapsamlı bir klan tabanlı savaş ve strateji pluginidir. Oyuncular klanlar kurarak, bölgeler fethederek, yapılar inşa ederek ve birbirleriyle savaşarak ilerlerler.

### Temel Özellikler
- ✅ Klan kurma ve yönetimi
- ✅ Bölge kontrolü ve koruması
- ✅ Ritüel tabanlı sistemler
- ✅ Özel eşyalar ve moblar
- ✅ Yapı inşası (WorldEdit schematics)
- ✅ Tuzak sistemi
- ✅ Kuşatma mekaniği
- ✅ Felaket sistemi
- ✅ Ekonomi entegrasyonu

---

## 👥 Klan Sistemi

Stratocraft'ta klan kurmak için **iki farklı yöntem** vardır. Her iki yöntem de geçerlidir ve aynı sonucu verir.

---

### 🎯 Yöntem 1: Temel Taşı Ritüeli (Ritüel ile Klan Kurma)

Bu yöntem, **ritüel sistemi** kullanarak klan kurmaktır. Komut gerektirmez, sadece fiziksel etkileşimlerle yapılır.

#### Gereksinimler:
1. **3x3 Cobblestone Platform:** Crafting Table'ın altında 3x3 kırık taş (Cobblestone) platformu
2. **İsimlendirilmiş Kağıt:** Örs'te isim verilmiş bir kağıt (kağıdın üzerinde klan ismi yazılı olmalı)
3. **Crafting Table:** Platformun ortasına yerleştirilmiş
4. **Oyuncu:** Crafting Table'ın üzerinde durmalı

#### Adım Adım:
1. **Platform Hazırlığı:**
   - Yere 3x3 Cobblestone platformu yerleştir
   - Platformun ortasına Crafting Table koy

2. **Kağıt Hazırlığı:**
   - Örs'te bir kağıda klan ismini yaz (örn: "Aslanlar")
   - Kağıdı eline al

3. **Ritüel:**
   - Crafting Table'ın üzerine çık
   - Crafting Table'a **sağ tıkla**

#### Sonuç:
- ✅ Klan kurulur (kağıttaki isimle)
- ✅ Oyuncu otomatik olarak **Lider** olur
- ✅ Bölge (Territory) otomatik oluşturulur (Crafting Table'ın konumu merkez olur)
- ✅ Şimşek ve partikül efektleri gösterilir
- ✅ Sunucuya duyuru yapılır
- ✅ Kağıt tüketilir (1 adet azalır)
- ✅ 10 saniye cooldown uygulanır

#### Örnek Senaryo:
```
Oyuncu: "Aslanlar" isimli kağıtla Crafting Table'a tıklar
→ Klan "Aslanlar" kurulur
→ Oyuncu Lider olur
→ Bölge aktif olur (Crafting Table konumu merkez)
→ Sunucuya: "OyuncuAdı klanı kurdu: Aslanlar"
```

**Not:** Bu yöntemle kurulan klanın kristali yoktur. Bölge koruması aktif olsa da görsel bir kristal entity'si oluşturulmaz.

---

### 💎 Yöntem 2: Klan Kristali ile Klan Kurma

Bu yöntem, **Klan Kristali** craft edip yerleştirerek klan kurmaktır. Daha görsel ve savaş mekaniği içerir.

#### Gereksinimler:
1. **Klan Kristali:** Craft edilmiş olmalı
2. **Klan Çitleri:** Kristal yerleştirilecek alan **tamamen** Klan Çitleri ile çevrelenmiş olmalı
3. **Minimum Alan:** En az 3x3 (9 blok) kapalı alan olmalı
4. **Maksimum Alan:** En fazla 500 blok kapalı alan olabilir

#### Klan Kristali Tarifi:
```
Boş - Elmas Blok - Boş
Elmas Blok - Ender İncisi - Elmas Blok
Boş - Obsidyen - Boş
```

#### Klan Çiti Tarifi:
```
Tahta - Demir - Tahta
Tahta - Demir - Tahta
```

#### Adım Adım:
1. **Alan Hazırlığı:**
   - Klan Çitleri ile kapalı bir alan oluştur (minimum 3x3)
   - Alan tamamen çevrelenmiş olmalı (açık yer olmamalı)

2. **Kristal Yerleştirme:**
   - Klan Kristali'ni eline al
   - Çevrelenmiş alanın içindeki bir bloğa **sağ tıkla**
   - Kristal entity olarak oluşturulur (EnderCrystal)

#### Sonuç:
- ✅ Klan kurulur (isim: `OyuncuAdı_Klanı` - otomatik)
- ✅ Oyuncu otomatik olarak **Lider** olur
- ✅ Bölge (Territory) otomatik oluşturulur (Kristal konumu merkez olur)
- ✅ Kristal entity oluşturulur (görsel)
- ✅ Şimşek ve partikül efektleri gösterilir
- ✅ Sunucuya duyuru yapılır
- ✅ Kristal eşyası tüketilir

#### Örnek Senaryo:
```
Oyuncu: Klan Çitleri ile 5x5 alan oluşturur
→ Klan Kristali'ni alanın içine yerleştirir
→ Klan "OyuncuAdı_Klanı" kurulur
→ Kristal entity oluşturulur
→ Bölge aktif olur (Kristal konumu merkez)
```

**Not:** Bu yöntemle kurulan klanın kristali vardır. Kristal kırılırsa klan bozulur (savaş mekaniği).

---

### ⚔️ İki Yöntem Arasındaki Farklar

| Özellik | Temel Taşı Ritüeli | Klan Kristali |
|---------|-------------------|---------------|
| **Gereksinim** | 3x3 Cobblestone + Crafting Table + İsimli Kağıt | Klan Kristali + Klan Çitleri |
| **Klan İsmi** | Kağıttaki isim | Otomatik: `OyuncuAdı_Klanı` |
| **Kristal Entity** | ❌ Yok | ✅ Var (EnderCrystal) |
| **Savaş Mekaniği** | Kristal yok, kırılamaz | Kristal kırılabilir, klan bozulur |
| **Görsel** | Sadece efektler | Kristal entity görünür |
| **Cooldown** | 10 saniye | Yok |

**Öneri:** 
- **Hızlı kurulum** için: Temel Taşı Ritüeli
- **Savaş mekaniği** için: Klan Kristali (kuşatma sistemi ile uyumlu)

---

### 👥 Klan Üye Alma

#### Ritüel: "Ateş Ritüeli"

**Gereksinimler:**
1. **3x3 Soyulmuş Odun (Stripped Log) Platformu**
2. **Çakmak (Flint and Steel)**
3. **Shift + Sağ Tık** ile ritüel başlatılır
4. **Yetki:** Sadece Lider veya Generaller

**Adımlar:**
1. 3x3 Soyulmuş Odun platformu hazırla
2. Çakmağı eline al
3. **Shift** tuşuna basılı tut
4. Platformun ortasına sağ tıkla
5. Ritüel alanındaki klansız oyuncular otomatik klana katılır

**Sonuç:**
- Ritüel alanındaki klansız oyuncular klana **Recruit** rütbesiyle eklenir
- Ateş partikülleri ve ses efektleri
- Her yeni üyeye özel mesaj gönderilir
- Çakmak dayanıklılığı azalır

**Örnek:**
```
Lider: Shift + Sağ Tık (Çakmak ile)
→ 3x3 alandaki 2 klansız oyuncu klana katılır
→ "Aslanlar klanına ruhun bağlandı!" mesajı
```

---

### 🗑️ Klan Bozma (Disband) ve Ayrılma

#### 1. Lider Klanı Bozma

**Yöntem A: Komut ile**
```
/klan ayril
```
- Lider bu komutu kullanırsa → Klan tamamen dağıtılır
- Tüm üyeler klansız kalır
- Bölge koruması kalkar
- Kristal varsa kırılır
- Sunucuya duyuru yapılır: `"[Klan İsmi] klanı dağıtıldı."`

**Yöntem B: Kristal Kırma (Sadece Kristal ile kurulan klanlar için)**
- Lider kristali kırarsa → Klan tamamen dağıtılır
- Kristal entity kırılır
- Tüm üyeler klansız kalır
- Bölge koruması kalkar

**Yöntem C: Kuşatma Sırasında Kristal Kırılması**
- Düşman klan kuşatma sırasında kristali kırarsa → Klan tamamen dağıtılır
- Saldıran klan zafer kazanır
- Kırılan klanın tüm üyeleri klansız kalır

#### 2. Normal Üye Ayrılma

**Komut:**
```
/klan ayril
```
- Normal üye (Lider değil) bu komutu kullanırsa → Sadece kendisi ayrılır
- Klan devam eder
- Diğer üyeler etkilenmez
- Mesaj: `"Klandan ayrıldınız."`

#### 3. Lider Kristali Taşıma (Sadece Kristal ile kurulan klanlar için)

**Yöntem:**
1. Lider olmalısın
2. **Shift** tuşuna basılı tut
3. Kristal entity'sine **sağ tıkla** (boş elle)
4. Yeni konum seç (Klan Çitleri ile çevrili olmalı)
5. Kristal taşınır, bölge merkezi güncellenir

**Not:** Kristal taşındığında bölge merkezi de güncellenir.

---

### 📋 Klan Komutları

#### Oyuncu Komutları

```
/klan menü          → Klan menüsünü açar (GUI)
/klan bilgi         → Klan bilgilerini gösterir
/klan ayril         → Klandan ayrılır (üye) veya klanı dağıtır (lider)
```

#### Admin Komutları

```
/klan kur <isim>    → Klan kurar (admin - komut ile)
/klan kristal       → Bölge oluşturur (admin - komut ile, kristal yok)
```

**Not:** Normal oyuncular için `/klan kur` ve `/klan kristal` komutları çalışmaz. Sadece ritüeller kullanılabilir.

---

### 🎖️ Klan Rütbeleri

1. **LEADER (Lider)**
   - Tüm yetkilere sahip
   - Klanı dağıtabilir (komut veya kristal kırma)
   - Üye ekleyip çıkarabilir
   - Rütbe değiştirebilir
   - Kristali taşıyabilir (varsa)
   - Ritüelleri yapabilir

2. **GENERAL (General)**
   - Üye ekleyip çıkarabilir
   - Rütbe değiştirebilir (Lider hariç)
   - Ritüelleri yapabilir (Ateş Ritüeli)
   - Bölge yönetimi

3. **MEMBER (Üye)**
   - Standart üye
   - Bölge içinde inşaat yapabilir
   - Klan kasasına erişebilir

4. **RECRUIT (Acemi)**
   - Yeni katılan üyeler
   - En düşük rütbe
   - Sınırlı yetkiler

**Not:** Kodda `OFFICER` rütbesi yoktur. Sadece 4 rütbe vardır: LEADER, GENERAL, MEMBER, RECRUIT.

---

### 📊 Klan Menüsü

`/klan menü` komutu ile açılan GUI menüsü:
- Klan bilgileri (isim, üye sayısı, bakiye)
- Üye listesi
- Rütbe yönetimi
- Bölge bilgileri
- Klan kasası
- Teknoloji seviyesi

---

### ⚠️ Önemli Notlar

1. **İki Yöntem Birleştirilemez:**
   - Ritüel ile kurulan klanın kristali yoktur
   - Kristal ile kurulan klanın kristali vardır
   - İkisi birleştirilemez

2. **Kristal Güvenliği:**
   - Sadece Lider kristali kırabilir (normal durumda)
   - Kuşatma sırasında düşmanlar da kırabilir
   - Doğal hasar (lava, patlama) kristali kıramaz (korunur)

3. **Bölge Koruması:**
   - Her iki yöntemle de bölge koruması aktif olur
   - Bölge merkezi kurulum anındaki konumdur
   - Kristal taşındığında bölge merkezi güncellenir

4. **Klan İsmi:**
   - Ritüel yöntemi: Kağıttaki isim kullanılır
   - Kristal yöntemi: Otomatik `OyuncuAdı_Klanı` oluşturulur
   - İsim değiştirme: Kodda `setName()` metodu var, muhtemelen GUI menüsünden yapılabilir

---

## 🗺️ Bölge (Territory) Sistemi

Bölge (Territory), klan kurulduğunda **otomatik olarak** oluşturulur. Ayrı bir işlem gerektirmez.

---

### 🎯 Bölge Oluşturma

Bölge, klan kurulduğunda otomatik oluşturulur:

#### Temel Taşı Ritüeli ile:
- Bölge merkezi: **Crafting Table'ın konumu**
- Bölge otomatik oluşturulur
- Kristal entity yoktur

#### Klan Kristali ile:
- Bölge merkezi: **Kristal entity'nin konumu**
- Bölge otomatik oluşturulur
- Kristal entity vardır

**Not:** Admin komutu `/klan kristal` ile de bölge oluşturulabilir (sadece adminler için).

---

### 🛡️ Bölge Koruması

Bölge içinde (merkez etrafında belirli bir yarıçap):

#### Klan Üyeleri:
- ✅ Blok kırabilir
- ✅ Blok yerleştirebilir
- ✅ Eşya alabilir (sandık, fırın vb.)
- ✅ Yapı inşa edebilir
- ✅ Tüm işlemleri yapabilir

#### Düşman Klanlar (Farklı klan üyeleri):
- ❌ Blok kıramaz (koruma aktif)
- ❌ Blok yerleştiremez (koruma aktif)
- ❌ Eşya alamaz (sandık, fırın vb. - koruma aktif)
- ✅ PvP yapabilir (savaş mümkün)
- ✅ Kristali kırabilir (kuşatma sırasında)

#### Klansız Oyuncular:
- ❌ Blok kıramaz
- ❌ Blok yerleştiremez
- ❌ Eşya alamaz
- ✅ PvP yapabilir (klan üyeleri ile)

---

### 📏 Bölge Boyutu

Bölge boyutu config dosyasından ayarlanabilir (varsayılan: muhtemelen 50x50 blok veya yarıçap bazlı).

**Not:** Kodda bölge boyutu `Territory` sınıfında tanımlıdır. Config dosyasından kontrol edilebilir.

---

### 🔄 Bölge Merkezi Değiştirme

#### Temel Taşı Ritüeli ile Kurulan Klanlar:
- Bölge merkezi değiştirilemez
- Sadece klan bozulup yeniden kurulabilir

#### Klan Kristali ile Kurulan Klanlar:
- Lider kristali taşıyabilir (Shift + Sağ Tık)
- Kristal taşındığında bölge merkezi otomatik güncellenir
- Yeni konum Klan Çitleri ile çevrili olmalı

---

### ⚔️ Bölge ve Kuşatma

Kuşatma sırasında:
- Bölge koruması **kalkar** (düşmanlar blok kırabilir)
- Düşman klan kristali kırabilir
- Kristal kırılırsa klan bozulur
- Saldıran klan zafer kazanır

---

### 🗑️ Bölge Kaldırma

Bölge, klan bozulduğunda otomatik kaldırılır:
- Lider `/klan ayril` komutunu kullanırsa
- Lider kristali kırarsa (varsa)
- Kuşatma sırasında kristal kırılırsa
- Bölge koruması kalkar
- Tüm üyeler klansız kalır

---

## 🔮 Ritüel Sistemi

Ritüeller, komut yerine **fiziksel etkileşimlerle** yapılan özel işlemlerdir.

### 1. Temel Taşı Ritüeli (Klan Kurma)
- **Gereksinim:** 3x3 Cobblestone + Crafting Table + İsimlendirilmiş Kağıt
- **Kullanım:** Crafting Table'a sağ tıkla (üzerinde durarak)
- **Cooldown:** 10 saniye

### 2. Ateş Ritüeli (Üye Alma)
- **Gereksinim:** 3x3 Soyulmuş Odun + Çakmak
- **Kullanım:** Shift + Sağ Tık (Çakmak ile)
- **Yetki:** Lider veya General

### 3. Yapı Ritüelleri
Her yapı için farklı ritüel gereksinimleri vardır (aşağıda detaylı).

---

## 🎒 Özel Eşyalar

### Madenler

#### 1. Titanyum
- **Cevher:** Titanyum Parçası (Flint görünümünde)
- **Külçe:** Titanyum Külçesi (Iron Ingot görünümünde)
- **Kullanım:** Gelişmiş eşya yapımında

#### 2. Kükürt
- **Cevher:** Kükürt Cevheri (Yellow Concrete Powder)
- **Kükürt:** Kükürt (Gunpowder)
- **Kullanım:** Patlayıcı ve kimyasal üretim

#### 3. Boksit
- **Cevher:** Boksit Cevheri (Orange Concrete Powder)
- **Külçe:** Boksit Külçesi (Copper Ingot)
- **Kullanım:** Alüminyum üretimi

#### 4. Tuz Kayası
- **Cevher:** Tuz Kayası (Quartz Block)
- **Tuz:** Tuz (Sugar)
- **Kullanım:** Yiyecek ve konserve

#### 5. Mithril
- **Cevher:** Mithril Cevheri (Light Blue Concrete Powder)
- **Külçe:** Mithril Külçesi (Iron Ingot)
- **İp:** Mithril İpi (String)
- **Kullanım:** Gelişmiş zırh ve eşya

#### 6. Astral
- **Cevher:** Astral Cevheri (Amethyst Block)
- **Kristal:** Astral Kristali (Echo Shard)
- **Kullanım:** Büyülü eşyalar

### Özel Materyaller

#### 1. Karanlık Madde (Dark Matter)
- **Görünüm:** Coal
- **Kullanım:** Güçlü eşyalar

#### 2. Kızıl Elmas (Red Diamond)
- **Görünüm:** Diamond
- **Kullanım:** Değerli eşyalar

#### 3. Yakut (Ruby)
- **Görünüm:** Redstone
- **Kullanım:** Enerji sistemleri

#### 4. Adamantite
- **Görünüm:** Netherite Ingot
- **Kullanım:** En güçlü eşyalar

#### 5. Yıldız Çekirdeği (Star Core)
- **Görünüm:** Nether Star
- **Kullanım:** Efsanevi eşyalar

### Özel Eşyalar

#### 1. Mühendis Şeması (Blueprint Paper)
- **Tarif:** Kağıt + Lapis Lazuli
- **Kullanım:** Yapı planları

#### 2. Yıldırım Çekirdeği (Lightning Core)
- **Tarif:** 
  ```
  Altın - Ender İncisi - Altın
  Ender İncisi - Elmas - Ender İncisi
  Altın - Ender İncisi - Altın
  ```
- **Kullanım:** Şok tuzakları, enerji sistemleri

#### 3. Tuzak Çekirdeği (Trap Core)
- **Tarif:**
  ```
  Obsidyen - Ender İncisi - Obsidyen
  Demir - Elmas - Demir
  Obsidyen - Ender İncisi - Obsidyen
  ```
- **Kullanım:** Tuzak kurulumu

#### 4. Paslı Kanca (Rusty Hook)
- **Tarif:**
  ```
  Boş - Demir - Boş
  Boş - Demir - Boş
  Boş - İp - Boş
  ```
- **Özellikler:**
  - Menzil: 7 blok
  - Cooldown: 2 saniye
  - Kısa menzilli kanca

#### 5. Titan Kancası (Titan Grapple)
- **Tarif:**
  ```
  Boş - Titanyum - Boş
  Boş - Titanyum - Boş
  Mithril İpi - Nether Star - Boş
  ```
- **Özellikler:**
  - Menzil: 40 blok
  - Cooldown: 2 saniye
  - Slow Falling efekti
  - Dayanıklılık azalır

#### 6. Savaş Yelpazesi (War Fan)
- **Görünüm:** Feather
- **Kullanım:** Özel yetenekler

#### 7. Kule Kalkanı (Tower Shield)
- **Görünüm:** Shield
- **Kullanım:** Gelişmiş savunma

#### 8. Cehennem Meyvesi (Hell Fruit)
- **Görünüm:** Apple
- **Kullanım:** Özel tüketim

### Admin Komutu ile Eşya Verme

```
/stratocraft give <eşya_adı> [miktar]
```

**Örnekler:**
```
/stratocraft give titanium_ingot 64
/stratocraft give trap_core 1
/stratocraft give titan_grapple 1
```

**Mevcut Eşyalar:**
- `blueprint`, `lightning_core`, `titanium_ore`, `titanium_ingot`
- `dark_matter`, `red_diamond`, `ruby`, `adamantite`, `star_core`
- `flame_amplifier`, `devil_horn`, `devil_snake_eye`
- `war_fan`, `tower_shield`, `hell_fruit`
- `sulfur_ore`, `sulfur`, `bauxite_ore`, `bauxite_ingot`
- `rock_salt_ore`, `rock_salt`, `mithril_ore`, `mithril_ingot`, `mithril_string`
- `astral_ore`, `astral_crystal`
- `rusty_hook`, `titan_grapple`, `trap_core`

---

## 👹 Özel Moblar

### Efsanevi Moblar

#### 1. Cehennem Ejderi (Hell Dragon)
- **Tür:** Phantom (büyük boyut)
- **Can:** 200 HP
- **Özellikler:** Uçan, güçlü saldırı
- **Spawn:** `/stratocraft spawn hell_dragon`

#### 2. Wyvern
- **Tür:** Phantom (orta boyut)
- **Can:** 250 HP
- **Özellikler:** Hızlı, dayanıklı
- **Spawn:** `/stratocraft spawn wyvern`

#### 3. Toprak Solucanı (Terror Worm)
- **Tür:** Silverfish
- **Can:** 100 HP
- **Özellikler:** Yer altında hareket
- **Spawn:** `/stratocraft spawn terror_worm`

#### 4. Savaş Ayısı (War Bear)
- **Tür:** Polar Bear
- **Can:** 150 HP
- **Özellikler:** Güçlü saldırı
- **Spawn:** `/stratocraft spawn war_bear`

#### 5. Gölge Panteri (Shadow Panther)
- **Tür:** Cat (evcilleştirilebilir)
- **Can:** 80 HP
- **Özellikler:** Hızlı, gizli
- **Spawn:** `/stratocraft spawn shadow_panther`

#### 6. Ateş Amfibiterü (Fire Amphiptere)
- **Tür:** Blaze
- **Can:** 120 HP
- **Saldırı:** 10 hasar
- **Spawn:** `/stratocraft spawn fire_amphiptere`

### Sık Gelen Canavarlar

#### 1. Goblin
- **Tür:** Zombie (bebek)
- **Can:** 30 HP
- **Hız:** Yüksek (0.35)
- **Spawn:** `/stratocraft spawn goblin`

#### 2. Ork
- **Tür:** Zombie
- **Can:** 80 HP
- **Saldırı:** 8 hasar
- **Zırh:** Deri kask
- **Spawn:** `/stratocraft spawn ork`

#### 3. Troll
- **Tür:** Zombie
- **Can:** 120 HP
- **Hız:** Yavaş (0.2)
- **Saldırı:** 10 hasar
- **Spawn:** `/stratocraft spawn troll`

#### 4. İskelet Şövalye (Skeleton Knight)
- **Tür:** Skeleton
- **Can:** 60 HP
- **Zırh:** Demir zırh seti
- **Silah:** Demir kılıç
- **Spawn:** `/stratocraft spawn skeleton_knight`

#### 5. Zombi Savaşçı (Zombie Warrior)
- **Tür:** Zombie
- **Can:** 100 HP
- **Zırh:** Altın zırh
- **Silah:** Altın kılıç
- **Spawn:** `/stratocraft spawn zombie_warrior`

#### 6. Creeper Patlayıcı (Creeper Exploder)
- **Tür:** Creeper
- **Can:** 40 HP
- **Patlama:** Büyük hasar
- **Spawn:** `/stratocraft spawn creeper_exploder`

#### 7. Örümcek Zehirli (Spider Poisonous)
- **Tür:** Spider
- **Can:** 50 HP
- **Efekt:** Zehir
- **Spawn:** `/stratocraft spawn spider_poisonous`

#### 8. Enderman Savaşçı (Enderman Warrior)
- **Tür:** Enderman
- **Can:** 80 HP
- **Özellikler:** Işınlanma
- **Spawn:** `/stratocraft spawn enderman_warrior`

#### 9. Wither İskelet (Wither Skeleton)
- **Tür:** Wither Skeleton
- **Can:** 100 HP
- **Efekt:** Wither
- **Spawn:** `/stratocraft spawn wither_skeleton`

#### 10. Blaze Savaşçı (Blaze Warrior)
- **Tür:** Blaze
- **Can:** 60 HP
- **Saldırı:** Ateş topu
- **Spawn:** `/stratocraft spawn blaze_warrior`

### Admin Komutu ile Mob Spawn

```
/stratocraft spawn <mob_adı>
```

**Örnekler:**
```
/stratocraft spawn hell_dragon
/stratocraft spawn goblin
/stratocraft spawn ork
```

**Mevcut Moblar:**
- `hell_dragon`, `terror_worm`, `war_bear`, `shadow_panther`
- `wyvern`, `fire_amphiptere`
- `goblin`, `ork`, `troll`, `skeleton_knight`, `zombie_warrior`
- `creeper_exploder`, `spider_poisonous`, `enderman_warrior`
- `wither_skeleton`, `blaze_warrior`

---

## 🏗️ Yapılar (Structures)

Yapılar, WorldEdit schematics kullanılarak inşa edilir. Her yapı için özel ritüel gereksinimleri vardır.

### Yapı Tipleri

#### 1. Ana Kristal (CORE)
- **Açıklama:** Klanın merkezi yapısı
- **Ritüel:** Klan Kristali dikme
- **Özellikler:** Bölge koruması, klan merkezi

#### 2. Simya Kulesi (ALCHEMY_TOWER)
- **Açıklama:** Batarya buff'ları sağlar
- **Ritüel:** Özel malzemeler
- **Özellikler:** Enerji üretimi

#### 3. Zehir Reaktörü (POISON_REACTOR)
- **Açıklama:** Zehir saldırıları
- **Ritüel:** Zehirli malzemeler
- **Özellikler:** Düşmanlara zehir efekti

#### 4. Tektonik Sabitleyici (TECTONIC_STABILIZER)
- **Açıklama:** Felaket koruması
- **Ritüel:** Özel tarif kitabı gerekli
- **Özellikler:** Deprem, fırtına koruması

#### 5. Kuşatma Fabrikası (SIEGE_FACTORY)
- **Açıklama:** Kuşatma silahları üretir
- **Ritüel:** Savaş malzemeleri
- **Özellikler:** Top, mancınık üretimi

#### 6. Sur Jeneratörü (WALL_GENERATOR)
- **Açıklama:** Otomatik duvar inşası
- **Ritüel:** Taş ve demir
- **Özellikler:** Bölge savunması

#### 7. Yerçekimi Kuyusu (GRAVITY_WELL)
- **Açıklama:** Düşmanları çeker
- **Ritüel:** Özel çekirdekler
- **Özellikler:** Alan kontrolü

#### 8. Lav Hendekçisi (LAVA_TRENCHER)
- **Açıklama:** Lav hendekleri oluşturur
- **Ritüel:** Lav ve obsidyen
- **Özellikler:** Savunma hattı

#### 9. Gözetleme Kulesi (WATCHTOWER)
- **Açıklama:** Uzun menzilli görüş
- **Ritüel:** Cam ve demir
- **Özellikler:** Keşif ve uyarı

#### 10. Drone İstasyonu (DRONE_STATION)
- **Açıklama:** Otomatik dronlar
- **Ritüel:** Redstone ve demir
- **Özellikler:** Otomatik savunma

#### 11. Otomatik Taret (AUTO_TURRET)
- **Açıklama:** Otomatik ateş
- **Ritüel:** Hurda teknolojisi
- **Özellikler:** Düşman tespiti ve saldırı

#### 12. Global Pazar Kapısı (GLOBAL_MARKET_GATE)
- **Açıklama:** Ticaret merkezi
- **Ritüel:** Altın ve elmas
- **Özellikler:** Klanlar arası ticaret

#### 13. Otomatik Madenci (AUTO_DRILL)
- **Açıklama:** Otomatik madencilik
- **Ritüel:** Demir ve redstone
- **Özellikler:** Sürekli kaynak üretimi

#### 14. Tecrübe Bankası (XP_BANK)
- **Açıklama:** XP saklama
- **Ritüel:** Lapis ve altın
- **Özellikler:** Tecrübe biriktirme

#### 15. Manyetik Ray (MAG_RAIL)
- **Açıklama:** Hızlı ulaşım
- **Ritüel:** Demir ve redstone
- **Özellikler:** Bölgeler arası ulaşım

#### 16. Işınlanma Platformu (TELEPORTER)
- **Açıklama:** Anında ışınlanma
- **Ritüel:** Ender İncisi ve obsidyen
- **Özellikler:** Uzun mesafe ulaşım

#### 17. Buzdolabı (FOOD_SILO)
- **Açıklama:** Yiyecek saklama
- **Ritüel:** Taş ve demir
- **Özellikler:** Yiyecek koruması

#### 18. Petrol Rafinerisi (OIL_REFINERY)
- **Açıklama:** Yakıt üretimi
- **Ritüel:** Lav ve demir
- **Özellikler:** Enerji kaynağı

#### 19. Şifa Kulesi (HEALING_BEACON)
- **Açıklama:** Alan şifası
- **Ritüel:** Altın ve elmas
- **Özellikler:** Klan üyelerine şifa

#### 20. Hava Kontrolcüsü (WEATHER_MACHINE)
- **Açıklama:** Hava kontrolü
- **Ritüel:** Redstone ve cam
- **Özellikler:** Yağmur, fırtına kontrolü

#### 21. Tarım Hızlandırıcı (CROP_ACCELERATOR)
- **Açıklama:** Bitki büyütme
- **Ritüel:** Kemik unu ve su
- **Özellikler:** Hızlı hasat

#### 22. Mob Öğütücü (MOB_GRINDER)
- **Açıklama:** Mob öğütme
- **Ritüel:** Demir ve lav
- **Özellikler:** Otomatik kaynak

#### 23. Görünmezlik Perdesi (INVISIBILITY_CLOAK)
- **Açıklama:** Gizlenme
- **Ritüel:** Ender İncisi ve cam
- **Özellikler:** Bölge gizleme

#### 24. Cephanelik (ARMORY)
- **Açıklama:** Silah saklama
- **Ritüel:** Demir ve altın
- **Özellikler:** Eşya organizasyonu

#### 25. Kütüphane (LIBRARY)
- **Açıklama:** Araştırma merkezi
- **Ritüel:** Kitap ve raf
- **Özellikler:** Teknoloji geliştirme

#### 26. Yasaklı Bölge Tabelası (WARNING_SIGN)
- **Açıklama:** Uyarı işareti
- **Ritüel:** Tahta ve demir
- **Özellikler:** Bölge işaretleme

### Yapı İnşası

Yapılar, WorldEdit `.schem` dosyaları kullanılarak inşa edilir. Dosyalar `plugins/Stratocraft/schematics/` klasöründe olmalıdır.

**Admin Komutu:**
```
/stratocraft build <yapı_tipi> [seviye]
```

**Örnek:**
```
/stratocraft build CORE 1
/stratocraft build SIEGE_FACTORY 1
```

---

## 🪤 Tuzak Sistemi

Tuzaklar, düşman klanları yakalamak için kullanılan ritüel tabanlı sistemlerdir.

### Tuzak Kurulumu

#### 1. Tuzak Çekirdeği Yerleştirme

**Gereksinimler:**
1. **Tuzak Çekirdeği (Trap Core)** - Craft edilmiş
2. **Magma Block Çerçeve** - 3x3 veya 5x5 (tuzak tipine göre)
3. **LODESTONE Bloğu** - Çekirdek olarak yerleştirilir

**Adımlar:**
1. Tuzak Çekirdeği'ni craft et
2. Magma Block'lardan bir çerçeve oluştur (3x3 veya 5x5)
3. Çerçevenin ortasına LODESTONE bloğu koy
4. LODESTONE'a Tuzak Çekirdeği ile sağ tıkla
5. Tuzak tipini seç (yakıt ile belirlenir)

**Tuzak Tipleri:**

#### 1. Cehennem Tuzağı (HELL_TRAP)
- **Yakıt:** Magma Cream
- **Efekt:** 3x3 alanda lav oluşturur
- **Hasar:** Yüksek (yanma)

#### 2. Şok Tuzağı (SHOCK_TRAP)
- **Yakıt:** Lightning Core
- **Efekt:** Yıldırım düşer
- **Hasar:** Orta-yüksek (elektrik)

#### 3. Kara Delik (BLACK_HOLE)
- **Yakıt:** Ender Pearl
- **Efekt:** Körlük + Yavaşlık
- **Hasar:** Düşük (dezavantaj)

#### 4. Mayın (MINE)
- **Yakıt:** TNT
- **Efekt:** Büyük patlama
- **Hasar:** Çok yüksek

#### 5. Zehir Tuzağı (POISON_TRAP)
- **Yakıt:** Spider Eye
- **Efekt:** Zehir efekti
- **Hasar:** Orta (zamanla)

### Tuzak Aktifleştirme

1. Tuzak çekirdeği yerleştirildikten sonra **üstü kapatılmalı** (gizlenmeli)
2. Düşman oyuncu tuzak alanına girdiğinde otomatik tetiklenir
3. Yakıt tüketilir (her patlama için 1 yakıt)
4. Yakıt bitince tuzak pasif olur

### Yakıt Ekleme

**Yakıt Tipleri:**
- **Elmas:** 5 patlama
- **Zümrüt:** 10 patlama
- **Titanyum:** 20 patlama

**Kullanım:**
1. Yakıtı eline al
2. Aktif tuzak çekirdeğine sağ tıkla
3. Yakıt envanterden kaldırılır, tuzak yakıt kazanır

### Tuzak Görünürlüğü

- Tuzaklar sadece **sahip ve klan üyelerine** görünür (kırmızı partiküller)
- Düşmanlar tuzakları göremez (gizli)
- Partiküller sürekli gösterilir (uyarı)

### Admin Komutları

```
/stratocraft trap give <oyuncu>     → Tuzak Çekirdeği verir
/stratocraft trap list              → Aktif tuzakları listeler
/stratocraft trap remove <x> <y> <z> → Tuzak kaldırır
```

---

## ⚔️ Kuşatma Sistemi

Kuşatma, düşman klan bölgelerine saldırmak için kullanılan sistemdir.

### Kuşatma Başlatma

**Gereksinimler:**
1. **Kuşatma Anıtı (Siege Monument)** - Yapı olarak inşa edilmiş
2. **Kuşatma Silahları** - Top, mancınık vb.
3. **Yeterli Üye** - Minimum üye sayısı

**Adımlar:**
1. Kuşatma Anıtı'nı inşa et
2. Anıta sağ tıkla
3. Hedef klanı seç
4. Kuşatmayı başlat

### Kuşatma Silahları

#### 1. Top (Cannon)
- **Yapım:** Kuşatma Fabrikası'nda üretilir
- **Kullanım:** Duvar yıkma
- **Hasar:** Yüksek

#### 2. Mancınık (Catapult)
- **Yapım:** Kuşatma Fabrikası'nda üretilir
- **Kullanım:** Uzun menzil saldırı
- **Hasar:** Orta

#### 3. Koçbaşı (Battering Ram)
- **Yapım:** Tahta ve demir
- **Kullanım:** Kapı kırma
- **Hasar:** Yüksek (kapılara)

### Kuşatma Süreci

1. **Hazırlık:** Silahlar üretilir, üyeler toplanır
2. **Başlatma:** Kuşatma Anıtı'ndan başlatılır
3. **Savaş:** Bölge koruması kalkar, PvP aktif olur
4. **Sonuç:** Kazanan klan bölgeyi ele geçirir veya kaybeden klan savunur

### Kuşatma Zamanlayıcı

Kuşatmalar belirli bir süre içinde tamamlanmalıdır (varsayılan: 30 dakika).

**Admin Komutu:**
```
/stratocraft siege start <klan_adı>
/stratocraft siege stop
```

---

## 🌋 Felaket Sistemi

Felaketler, oyun dünyasını etkileyen doğal afetlerdir.

### Felaket Tipleri

#### 1. Deprem (Earthquake)
- **Efekt:** Yer sarsılır, bloklar düşer
- **Hasar:** Yapılara hasar
- **Süre:** 2-5 dakika

#### 2. Fırtına (Storm)
- **Efekt:** Şimşek, yağmur, rüzgar
- **Hasar:** Açık alandaki oyunculara
- **Süre:** 5-10 dakika

#### 3. Meteor Yağmuru (Meteor Shower)
- **Efekt:** Gökyüzünden meteorlar düşer
- **Hasar:** Yüksek (patlama)
- **Süre:** 3-7 dakika

#### 4. Volkanik Patlama (Volcanic Eruption)
- **Efekt:** Lav akışı, kül bulutu
- **Hasar:** Çok yüksek (yanma)
- **Süre:** 5-15 dakika

#### 5. Tsunami
- **Efekt:** Büyük dalga
- **Hasar:** Kıyı bölgelerine
- **Süre:** 3-5 dakika

### Felaket Koruması

**Tektonik Sabitleyici** yapısı felaketleri engelleyebilir:
- Bölge içindeki felaketler etkisiz hale gelir
- Yapı seviyesi arttıkça koruma artar

### Admin Komutu

```
/stratocraft disaster <felaket_tipi> [süre]
```

**Örnekler:**
```
/stratocraft disaster earthquake 300
/stratocraft disaster storm 600
/stratocraft disaster meteor_shower 180
```

**Mevcut Felaketler:**
- `earthquake`, `storm`, `meteor_shower`, `volcanic_eruption`, `tsunami`

---

## 📜 Kontrat Sistemi

Kontratlar, klanlar arası ticaret ve görev sistemidir.

### Kontrat Oluşturma

**Komut:**
```
/kontrat olustur <malzeme> <miktar> <ödül> [gün]
```

**Örnek:**
```
/kontrat olustur IRON_INGOT 100 5000 2
```
- **Malzeme:** 100 Demir Külçesi
- **Ödül:** 5000 altın
- **Süre:** 2 gün

**Gereksinimler:**
- Klan üyesi olmalısın
- Klan kasasında ödül kadar para olmalı
- Ödül klan kasasından kesilir (teslimatta geri verilir)

### Kontrat Listeleme

**Komut:**
```
/kontrat list
```

Aktif kontratlar listelenir:
- Malzeme ve miktar
- Ödül miktarı
- Kontrat ID'si

### Kontrat Kabul Etme

**Komut:**
```
/kontrat teslim <kontrat_id>
```

**Not:** İlk teslim komutu kontratı otomatik kabul eder.

### Kontrat Teslim Etme

**Komut:**
```
/kontrat teslim <kontrat_id> <miktar>
```

**Adımlar:**
1. Kontratı kabul et (ilk teslim komutu)
2. Gerekli malzemeyi envanterine al
3. Teslim komutunu kullan
4. Malzeme envanterden kaldırılır
5. Ödül klan kasasına eklenir (veya oyuncuya verilir)

**Güvenlik:**
- Sadece kontratı kabul eden kişi veya aynı klan üyesi teslim edebilir
- Yeterli malzeme kontrolü yapılır

**Örnek Senaryo:**
```
Oyuncu A: /kontrat olustur DIAMOND 50 10000 3
→ Kontrat oluşturulur, 10000 altın klan kasasından kesilir

Oyuncu B: /kontrat list
→ Kontrat görünür: "DIAMOND x50 → 10000 altın"

Oyuncu B: /kontrat teslim <id> 50
→ Kontrat kabul edilir, 50 elmas envanterden kaldırılır
→ 10000 altın Oyuncu B'nin klan kasasına eklenir
```

---

## 🚚 Kervan Sistemi

Kervanlar, klanlar arası malzeme taşıma sistemidir.

### Kervan Oluşturma

**Admin Komutu:**
```
/stratocraft caravan create <başlangıç> <hedef> <malzeme> <miktar>
```

**Örnek:**
```
/stratocraft caravan create spawn base IRON_INGOT 1000
```

### Kervan Yolculuğu

1. Kervan oluşturulur
2. Kervan yolu boyunca ilerler
3. Düşman klanlar saldırabilir
4. Başarılı ulaşımda malzeme hedefe ulaşır

### Kervan Koruması

- Kervanlar korumalı olabilir (üye sayısına göre)
- Saldırılar PvP ile yapılır
- Kervan yok edilirse malzeme kaybolur

---

## 🔋 Batarya Sistemi

Batarya, klan bölgelerine enerji sağlayan sistemdir.

### Batarya Kurulumu

1. **Batarya Bloğu** yerleştirilir (özel blok)
2. **Yakıt** eklenir (kömür, odun vb.)
3. Batarya aktif olur

### Batarya Kullanımı

- **Yapılar:** Enerji gerektiren yapılar bataryadan beslenir
- **Koruma:** Bölge koruması için enerji gerekir
- **Buff'lar:** Simya Kulesi batarya ile çalışır

### Batarya Seviyeleri

- **Seviye 1:** Temel enerji
- **Seviye 2:** Orta enerji
- **Seviye 3:** Yüksek enerji

### Batarya Yakıtı

- **Kömür:** 100 enerji
- **Odun:** 50 enerji
- **Lav Kova:** 500 enerji

---

## 🎯 Özel Eşyalar (Araçlar)

### Casusluk Dürbünü (Spyglass)

**Kullanım:**
1. Spyglass'ı eline al
2. Bir oyuncuya 3 saniye bak
3. Oyuncu bilgileri gösterilir

**Gösterilen Bilgiler:**
- Can (HP)
- Maksimum can
- Zırh puanı (0-20)
- Envanter doluluğu (%)

**Örnek Çıktı:**
```
═══════════════════════════
CASUSLUK RAPORU: OyuncuAdı
Can: 15.0/20.0
Zırh: 12/20
Envanter: 25/36 (69%)
═══════════════════════════
```

### Paslı Kanca (Rusty Hook)

**Özellikler:**
- **Menzil:** 7 blok
- **Cooldown:** 2 saniye
- **Kullanım:** Kısa menzilli çekme

**Kullanım:**
1. Olta gibi kullan (sağ tık)
2. Bloğa takılınca otomatik çekilirsin
3. Cooldown bitene kadar tekrar kullanamazsın

### Titan Kancası (Titan Grapple)

**Özellikler:**
- **Menzil:** 40 blok
- **Cooldown:** 2 saniye
- **Efekt:** Slow Falling (düşme koruması)
- **Dayanıklılık:** Her kullanımda azalır

**Kullanım:**
1. Olta gibi kullan (sağ tık)
2. Uzun menzilli çekme
3. Düşerken Slow Falling efekti alırsın
4. Dayanıklılık bittiğinde kırılır

---

## 📦 Malzeme Düşüşü (Supply Drop)

Supply Drop, gökyüzünden malzeme kutusu düşürme sistemidir.

### Supply Drop Çağırma

**Admin Komutu:**
```
/stratocraft supplydrop <x> <y> <z> [dünya]
```

**Örnek:**
```
/stratocraft supplydrop 100 100 200 world
```

### Supply Drop İçeriği

- Rastgele özel eşyalar
- Madenler
- Silahlar
- Zırh

### Supply Drop Süreci

1. **Çağırma:** Admin komutu ile çağrılır
2. **Düşüş:** Gökyüzünden kutu düşer (firework efekti)
3. **İniş:** Yere iner, sandık oluşur
4. **Açılma:** Oyuncular sandığı açabilir
5. **İçerik:** Rastgele eşyalar dağıtılır

### Supply Drop Uyarısı

Supply Drop düşmeden önce sunucuya duyuru yapılır:
```
[Supply Drop] Malzeme kutusu düşüyor: X, Y, Z
```

---

## 🔬 Araştırma Sistemi

Araştırma, yeni teknolojiler ve tarifler açmak için kullanılır.

### Araştırma Yapma

1. **Araştırma Masası** kullan
2. **Malzemeler** hazırla
3. **Araştırmayı** başlat
4. **Süre** beklenir
5. **Tarif** açılır

### Araştırma Tipleri

- **Temel Teknolojiler:** Basit eşyalar
- **Gelişmiş Teknolojiler:** Karmaşık eşyalar
- **Efsanevi Teknolojiler:** En güçlü eşyalar

### Hayalet Tarifler

Araştırma yapılmadan önce "hayalet tarifler" görülebilir:
- Tarif görünür ama yapılamaz
- Araştırma tamamlandığında aktif olur

---

## 📋 Görev Sistemi

Görevler, oyunculara hedefler veren sistemdir.

### Görev Tipleri

1. **Öldürme Görevleri:** Belirli mobları öldür
2. **Toplama Görevleri:** Malzeme topla
3. **İnşaat Görevleri:** Yapı inşa et
4. **Keşif Görevleri:** Bölgeleri keşfet

### Görev Ödülleri

- Altın
- XP
- Özel eşyalar
- Klan puanı

---

## 🏋️ Antrenman Sistemi

Antrenman, oyuncuların yeteneklerini geliştirmesi için kullanılır.

### Antrenman Tipleri

1. **Güç Antrenmanı:** Saldırı gücü artar
2. **Dayanıklılık Antrenmanı:** Can artar
3. **Hız Antrenmanı:** Hareket hızı artar
4. **Savunma Antrenmanı:** Zırh etkisi artar

### Antrenman Yapma

1. **Antrenman Alanı** bul
2. **Antrenman Eşyası** kullan
3. **Süre** boyunca antrenman yap
4. **Bonus** kazan

---

## 🚛 Lojistik Sistemi

Lojistik, klan içi malzeme taşıma sistemidir.

### Lojistik Ağı

1. **Lojistik Merkezi** kur
2. **Bağlantılar** oluştur
3. **Malzeme** taşı
4. **Otomatik** dağıtım

### Lojistik Özellikleri

- Otomatik malzeme taşıma
- Merkezi depolama
- Dağıtım ağı

---

## 💰 Ekonomi Sistemi

Stratocraft, Vault entegrasyonu ile ekonomi sistemini destekler.

### Ekonomi Entegrasyonu

- **Vault:** EssentialsX, CMI vb. ekonomi pluginleri ile uyumlu
- **Kendi Sistemi:** Vault yoksa kendi ekonomi sistemi kullanılır

### Klan Kasası

- Her klanın kendi kasası vardır
- Üyeler para yatırabilir/çekebilir
- Lider ve generaller yönetebilir

### Para Kaynakları

- Kontrat ödülleri
- Görev ödülleri
- Ticaret
- Savaş ganimetleri

---

## 🛠️ Admin Komutları

### Genel Komutlar

```
/stratocraft help                    → Yardım menüsü
/stratocraft list                    → Tüm öğeleri listele
```

### Eşya Komutları

```
/stratocraft give <eşya> [miktar]   → Eşya ver
```

**Örnekler:**
```
/stratocraft give titanium_ingot 64
/stratocraft give trap_core 1
/stratocraft give titan_grapple 1
```

### Mob Komutları

```
/stratocraft spawn <mob>            → Mob spawn et
```

**Örnekler:**
```
/stratocraft spawn hell_dragon
/stratocraft spawn goblin
/stratocraft spawn ork
```

### Felaket Komutları

```
/stratocraft disaster <tip> [süre]  → Felaket başlat
```

**Örnekler:**
```
/stratocraft disaster earthquake 300
/stratocraft disaster storm 600
```

### Kuşatma Komutları

```
/stratocraft siege start <klan>     → Kuşatma başlat
/stratocraft siege stop             → Kuşatma durdur
```

### Kervan Komutları

```
/stratocraft caravan create <başlangıç> <hedef> <malzeme> <miktar>
```

### Tuzak Komutları

```
/stratocraft trap give <oyuncu>     → Tuzak Çekirdeği ver
/stratocraft trap list               → Aktif tuzakları listele
/stratocraft trap remove <x> <y> <z> → Tuzak kaldır
```

### Yapı Komutları

```
/stratocraft build <yapı_tipi> [seviye]
```

**Örnekler:**
```
/stratocraft build CORE 1
/stratocraft build SIEGE_FACTORY 1
```

### Supply Drop Komutları

```
/stratocraft supplydrop <x> <y> <z> [dünya]
```

---

## 📝 Notlar ve İpuçları

### Performans

- Tuzaklar ve yapılar performansı etkileyebilir
- Çok sayıda aktif yapı varsa sunucu yavaşlayabilir
- Batarya sistemi optimize edilmiştir

### Güvenlik

- Bölge koruması aktifken düşmanlar blok kıramaz
- Tuzaklar sadece sahip ve klan üyelerine görünür
- Kontrat sistemi güvenli teslimat sağlar

### Veri Kaybı

- Tüm veriler otomatik kaydedilir
- Sunucu kapanırken senkron kayıt yapılır (veri kaybı önlenir)
- Tuzaklar kalıcı olarak kaydedilir

### Hata Ayıklama

- Admin komutları test için kullanılabilir
- Log dosyaları hata ayıklama için yeterli bilgi sağlar

---

## 🎉 Sonuç

Stratocraft, kapsamlı bir klan tabanlı savaş ve strateji pluginidir. Bu dokümantasyon, tüm özelliklerin nasıl kullanılacağını detaylı olarak açıklamaktadır.

**İyi oyunlar! 🎮**

---

**Versiyon:** 10.0  
**Son Güncelleme:** 2025-11-28  
**Yazar:** Mami

