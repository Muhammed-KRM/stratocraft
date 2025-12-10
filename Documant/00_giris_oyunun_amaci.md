# STRATOCRAFT: MÜHENDİSLİK, FETİH VE FELAKET

## 📖 0. Giriş: Dünyanın Mantığı ve Oyunun Amacı

Stratocraft'a hoş geldiniz. Burası sıradan bir Minecraft dünyası değil; sürekli değişen, oyuncuyu zorlayan ve strateji gerektiren yaşayan bir evrendir.

---

## 🎯 Ana Amaç Nedir?

Bu dünyadaki tek amacınız **Hayatta Kalmak** değildir; **Felaketlere Hükmetmektir**. Dünya, belirli aralıklarla "Felaket Titanı" (30 blok boyunda dev golem) veya "Güneş Fırtınaları" (Solar Flare) gibi devasa Felaketler tarafından saldırıya uğrar. Tek başınıza hayatta kalamazsınız.

### Oyun Döngüsü Şöyledir:

```
1. Klan Kur → Güvenilir müttefikler bul
2. Bölge Al → Stratejik bir yere "Kristal" dikerek orayı güvenli bölge yap
3. Makineleş → Sadece ev yapma; seni koruyacak Zehir Kuleleri ve Radarlar inşa et
4. Savaş → Hem Felaketlere hem de diğer rakip klanlara karşı "Mühendislik" kullanarak savaş
```

---

## ❓ Temel Kavramlar (Nedir?)

### 1. Yapılar (Structures) Nedir?

Bunlar sadece dekoratif binalar değildir. Klan bölgenize kurduğunuz, size **pasif güç veren makinelerdir**.

**Örnek**: Bölgenize bir "Simya Kulesi" dikerseniz, o bölgedeki klan üyelerinin bataryaları %20 güçlenir. Yapı yoksa, güç de yok.

**Yapı Kategorileri**:
- **Klan Yapıları**: Sadece klan bölgesinde, büyük ve güçlü (Simya Kulesi, Zehir Reaktörü, Tektonik Sabitleyici)
- **Dışarı Yapıları**: Savaşta kullanılan geçici yapılar (Şifa Tapınağı, Geçici Kale)
- **Sosyal Yapılar**: Herkesin kullanabileceği yapılar (Görev Loncası, Ticaret Platformu)

**Yapı Seviyeleri**: Her yapı 1-5 seviye arası geliştirilebilir. Seviye arttıkça güç artar.

---

### 2. Ritüeller ve Bataryalar Nedir?

Bu oyunda büyücü asası veya "büyü kitabı" yoktur. Saldırmak için **fiziksel düzenekler kurmalısınız**. Biz buna **"Batarya"** diyoruz.

#### Nasıl Çalışır?

Blokları belirli bir sırayla yere koyarsın, eline yakıtı alırsın ve sistemi ateşlersin.

**Basit Örnek: Düşmana ateş topu mu atmak istiyorsun?**

```
1. Yere 3 tane Magma Bloğu üst üste koy
2. Eline Elmas al
3. Eğilerek (Shift) bloğa sağ tıkla
4. Sonuç: Düzenek çalışır ve elindeki elması harcayarak karşıya ateş topu fırlatır
```

**Batarya Türleri**:
- **Ateş Topu** (3x Magma Block): Sürekli hasar, yangın efekti
- **Yıldırım** (3x Iron Block): Güçlü tek vuruş, zincirleme hasar
- **Ses Dalgası** (3x Note Block): Düşmanları iter, sersemletir
- **Asit** (3x Emerald Block): Zırh aşındırır, zehir verir
- **Buz** (3x Packed Ice): Yavaşlatır, donma efekti

**Yakıt Kalitesi**: Demir → Elmas → Kızıl Elmas → Karanlık Madde (güç artar)

---

### 3. Klan ve Bölge Sistemi

**Klan**: Hayatta kalmanın temeli. Fiziksel etkileşimle kurulur (komut yok!).

**Nasıl Kurulur?**:
```
1. Klan Çiti craft et (64 adet)
2. Minimum 10x10 kapalı alan çevrele (maksimum limit yok)
3. İçine Klan Kristali yerleştir
4. Klan kuruldu!
5. ⛔ 24 saat başlangıç koruması aktif!
```

**Başlangıç Koruması (Grace Period)**:
- Yeni kurulan klanlar **24 saat** boyunca saldırıya karşı korunur
- Bu süre içinde klan güvenli bir şekilde gelişebilir
- Grace period süresi dolduktan sonra normal savaş kuralları geçerli olur

**Bölge Korumaları**:
- Düşman klanlar blok kıramaz
- Klan üyeleri birbirine zarar veremez
- Sadece klan üyeleri yapı kurabilir

**Klan Koruma Sistemi**:
- **3 seviye altındaki klana saldırılamaz**: Güçlü klanlar zayıf klanları ezemez
- **%95 hasar azaltma**: 3 seviye altındaki klanın oyuncularına neredeyse hiç hasar verilemez
- **Amaç**: Yeni başlayan klanların korunması ve adil rekabet

**Örnek**:
```
Klan A (Seviye 5) → Klan B (Seviye 1):
❌ Kuşatma başlatılamaz
❌ PvP'de %95 hasar azaltma (10 kalp → 0.5 kalp)
✅ Yapılara minimal hasar verilebilir
```

**Rütbe Sistemi**: Lider → General → Üye → Acemi (her rütbenin farklı yetkileri var)

**Güç Sistemi (YENİ)**: 
- **Stratocraft Güç Puanı (SGP)**: Her oyuncu ve klanın güç puanı vardır
- **Combat Power**: Eşya gücü (silah + zırh) + Buff gücü
- **Progression Power**: Ustalık gücü + Ritüel gücü (bloklar + kaynaklar)
- **Oyuncu Seviyesi**: Hibrit algoritma (1-10: karekök, 11+: logaritmik)
- **Klan Seviyesi**: Logaritmik algoritma (maksimum 15)
- **PvP Koruma**: Güçlü oyuncular zayıf oyunculara saldıramaz (onurlu savaş aralığı, acemi koruması)
- **HUD Entegrasyonu**: Güç bilgisi sağ taraftaki bilgi panosunda görünür (`💪 Güç: 1234 SGP (Seviye 5)`)
- **Komutlar**: `/sgp` komutu ile güç bilgilerini görüntüle, top sıralamalarına bak

**İttifak Sistemi**: Klanlar arası kalıcı anlaşmalar. İki lider elinde Elmas ile ritüel yaparak ittifak kurulur. İttifaklı klanlara saldırılamaz; ihlal edilirse ağır ceza uygulanır (klan bakiyesinin %20'si + Hain etiketi). Detaylar için `01_klan_sistemi.md` dosyasına bakın.

---

### 4. Felaketler: Dünyanın Gerçek Tehdidi

Felaketler **doğa olaylarıdır**, normal boss değil! Merkezden uzakta spawn olur ve merkeze doğru ilerleyerek yoldaki baseleri yok eder.

**9 Felaket Tipi**:
1. **Felaket Titanı (CATASTROPHIC_TITAN)**: 30 blok boyunda dev golem, toprak fırlatır, zıplar, şok dalgası yaratır, klan kristallerini yok eder
2. **Hiçlik Solucanı (Abyssal Worm)**: Yeraltından ilerler, temelleri kazar
3. **Güneş Fırtınası (Solar Flare)**: 10 dakika süren olay, yüzeydekiler yanar
4. **Buzul Leviathan**: Suda yüzer, baseleri dondurur
5. **Meteor Yağmuru**: Gökyüzünden anvil düşer
6. **Kaos Ejderi (Chaos Dragon)**: Uçan ejderha, kaos enerjisi yayar
7. **Hiçlik Titanı (Void Titan)**: Dev yaratık, hiçlik patlamaları yaratır
8. **Deprem (Earthquake)**: Yeri sarsar, binaları yıkar
9. **Volkanik Patlama (Volcanic Eruption)**: Lav fışkırır, kül bulutu yayar

**Dinamik Güç Sistemi**: Felaketler, oyuncuların gerçek güç puanlarına (SGP) göre güçlenir! Artık sadece oyuncu sayısına değil, eşya gücü, ustalık, ritüel gücü ve yapı gücüne göre ayarlanır.

**4 Fazlı Felaket Sistemi**: Felaketler artık 4 fazdan geçer (Keşif → Saldırı → Öfke → Çaresizlik). Her fazda farklı hız, saldırı aralığı ve özel yetenekler aktif olur.

**BossBar Görüntüsü**: 
- **Felaket Bossları** (Felaket Titanı, Felaket Hiçlik Solucanı, Felaket Khaos Ejderi, Felaket Boşluk Titanı, Felaket Buzul Leviathan): Ekranın üstünde can ve kalan süre gösterilir. Normal bosslardan tamamen ayrı, çok daha güçlü.
- **Doğal Felaketler** (Solar Flare, Meteor Shower, Earthquake, Volcanic Eruption): ActionBar'da kalan süre gösterilir
- **Bosslar**: Tüm bossların canı ve fazı (multi-phase bosslar için) ekranın üstünde gösterilir

---

## 🛠️ Yan Aktiviteler: Başka Ne Yapabilirim?

Savaşçı olmak zorunda değilsiniz. Klanınıza destek olmak için şu yolları izleyebilirsiniz:

### 1. Ticaret ve Sözleşmeler

**Kontrat Sistemi**: Panodaki "Bize 500 Titanyum Getir" görevlerini alıp, teslimat yaparak zengin olabilirsiniz.

**Nasıl Çalışır?**:
- `/kontrat` komutu ile GUI menü açılır
- Kontrat seçilir ve "Kabul Et" butonuna tıklanır
- Kan imzası ile sözleşme imzalanır (-3 kalp can kaybı)
- Kontrat tamamlandığında → +1 kalp geri (kan imzası geri ödeniyor)
- Süre içinde tamamlanmazsa → Hain damgası + otomatik tazminat + kalıcı can kaybı
- Başarılı olursa → Ödül kazanırsın

**Güvenlik**: Performans optimizasyonları (1 saniye cooldown) ve can kaybı geri kazanım sistemi aktif.

**Kervan Sistemi**: Uzak bölgelere malzeme taşı, x1.5 değer kazan!

**Market Sistemi**: Eşya satıp alabileceğin ve teklif verebileceğin ticaret sistemi.
- Sandık + Tabela ile market kur
- GUI menü ile alışveriş yap
- Teklif sistemi ile alternatif ödeme yap
- Koruma bölgesinde %5 vergi

**Görev Sistemi**: Totem'e sağ tık yaparak görev al, tamamla, ödül kazan!
- 8 farklı görev tipi (Mob Avı, Malzeme Toplama, Lokasyon Ziyareti, vb.)
- 4 zorluk seviyesi (Kolay, Orta, Zor, Uzman)
- GUI menü ile görev takibi
- Otomatik ilerleme takibi

---

### 2. PvE ve Canavar Avı

Dünyada sadece zombiler yok. **Eğitilebilir Ejderhalar**, yer altı Solucanları ve zırhlı bosslar var.

**Özel Moblar**:
- **Sık Gelen**: Goblin, Ork, Troll (30-120 HP)
- **Nadir**: Ejderha, T-Rex, Griffin (300-500 HP)
- **Efsanevi**: Titan Golem, Hydra, Behemoth (400-1000 HP)

**Boss Sistemi**: 
- **13 Farklı Boss**: Goblin King, Orc Chief, Troll King, Dragon, T-Rex, Cyclops, Titan Golem, Hell Dragon, Hydra, Phoenix, Void Dragon, Chaos Titan, Chaos God
- **BossBar Görüntüsü**: Tüm bossların canı ve fazı (multi-phase bosslar için) ekranın üstünde gösterilir
- **Boss Drops**: Her boss özel item düşürür (Goblin Crown, Troll Heart, Titan Core, Void Dragon Heart vb.)
- **Boss Item Gereksinimleri**: Özel silah ve zırhlar craft etmek için boss item'ları gerekir
- **Arena Transformasyonu**: Güçlü bosslar spawn olduğunda etrafları dinamik olarak dönüşür (kuleler, tehlikeler, blok transformasyonu)
- **YENİ ÖZELLİK**: Arena sistemi dinamik öncelik sistemi ile optimize edildi:
  - **Config Entegrasyonu**: Tüm arena ayarları `config.yml` dosyasından değiştirilebilir
  - **Dinamik Öncelik**: Oyuncu sayısına göre arena limiti otomatik ayarlanır
  - **50 Blok Kuralı**: Uzak bossların alanları genişlemez (performans optimizasyonu)
  - **Otomatik Performans Optimizasyonu**: TPS düşerse ayarlar otomatik düşer
  - **Uzak Arena Tekrar Başlatma**: Oyuncu yaklaştığında durdurulmuş arenalar otomatik başlatılır
  - **Admin Komutları**: `/scadmin arena status`, `/scadmin arena groups`, `/scadmin arena settings`, `/scadmin arena reload` ile sistem yönetimi
  - **Performans Metrikleri**: Sistem durumu detaylı olarak izlenebilir

**Eğitme Sistemi**: Özel isimli tüm canlılar eğitilebilir! **Eğitim Çekirdeği** (`TAMING_CORE`) kullanarak ritüel platformu kur, aktivasyon itemi ile eğit, sahiplen, binebilir hale getir. 5 zorluk seviyesine göre farklı platformlar. Eğitim Çekirdeği merkez bloğa yerleştirilir ve ritüel deseni çekirdeğin altına yapılır.

**Üreme Sistemi**: Eğitilmiş canlıları **Üreme Çekirdeği** (`BREEDING_CORE`) ile çiftleştir, yavru üret, ordu büyüt! Memeli canlılar direkt yavru doğurur, yumurtlayan canlılar (Ejderha, Griffin, Phoenix) yumurta bırakır. **Cinsiyet Tarayıcısı** (`GENDER_SCANNER`) ile canlıların cinsiyetini kontrol et!

**Binme Mekaniği**: Ejderha, T-Rex, Griffin gibi canlılara bin, uç, savaş!

---

### 3. Madencilik ve Kaynak Toplama

Derinlerde sıradan elmas değil, **Titanyum** ve **Kızıl Elmas** gibi yeni cevherler bulunur. Bunlar olmadan güçlü zırhlar ve özel silahlar yapılamaz.

**Zorluk Sistemi**: Merkezden uzaklaştıkça zorlaşır ve değerli madenler bulunur! Her zorluk seviyesinde farklı moblar spawn olur.

**5 Zorluk Bölgesi**:
- **Seviye 1** (200-1000 blok): Kükürt, Boksit, Tuz Kayası | Moblar: Goblin, Yaban Domuzu, Kurt
- **Seviye 2** (1000-3000 blok): Titanyum | Moblar: Ork, Troll, Minotaur, Demir Golem, Buz Ejderi
- **Seviye 3** (3000-5000 blok): Mithril | Moblar: T-Rex, Griffin, Phoenix, Gölge Ejderi, Fırtına Dev
- **Seviye 4** (5000-10000 blok): Astral Cevheri | Moblar: Ejderha, Wyvern, Kızıl Şeytan, Kaos Ejderi
- **Seviye 5** (10000+ blok): Kızıl Elmas (en nadir!) | Moblar: Titan Golem, Hydra, Efsanevi Ejder, Tanrı Katili

**Özel Madenler**:
- **Titanyum**: Y -40 altı, Netherite +%20 defans
- **Kızıl Elmas**: Y -50 altı, en güçlü silahlar için
- **Karanlık Madde**: Sadece Felaket Boss'larından düşer, efsanevi
- **Adamantite**: Sadece Felaket bölgelerinde

---

### 4. Araştırma ve Teknoloji

**Tarif Kitabı Sistemi**: Çok güçlü eşyalar için zorunlu bilgi kaynağı.

**Nasıl Çalışır?**:
- Boss'lardan tarif kitapları düşer
- Lectern'e koy (Araştırma Masası)
- 10 blok yarıçapta herkes o tarifi kullanabilir
- Kitap olmadan craft EDİLEMEZ

**Örnekler**: 
- "Tektonik Sabitleyici" yapmak için Titan Golem'den tarif kitabı gerekli!
- "Hiperiyon Kılıcı" (Seviye 5 silah) yapmak için Void Dragon'dan tarif kitabı gerekli!
- Tüm 25 özel silah ve 25 özel zırh için boss'lardan tarif kitapları düşer

**Boss Item Gereksinimleri**:
- **Seviye 1 Silah/Zırh**: Goblin Crown (Goblin King)
- **Seviye 2 Silah/Zırh**: Troll Heart (Troll King)
- **Seviye 3 Silah/Zırh**: T-Rex Tooth (T-Rex)
- **Seviye 4 Silah/Zırh**: Titan Core (Titan Golem)
- **Seviye 5 Silah/Zırh**: Void Dragon Heart (Void Dragon)

**Güvenlik**: Boss item doğrulama sistemi aktif - Sadece gerçek boss item'ları (NBT kontrolü) craft için kullanılabilir!

**YENİ ÖZELLİK**: Araştırma sistemi performans optimizasyonları ve güvenlik iyileştirmeleri ile güncellendi. Detaylar için `15_arastirma_sistemi.md` dosyasına bakın.

---

### 5. Tuzak ve Savunma

**Tuzak Sistemi**: Düşmanları yakalamak için fiziksel düzenekler.

**Nasıl Kurulur?**:
```
1. Tuzak Çekirdeği craft et
2. Yere koy (Lodestone olur)
3. Etrafına 6-8 Magma Block diz
4. Yakıt ekle (Coal, Lava, Lightning Core vb.)
5. Shift + Sağ tık → Aktif!
```

**Tuzak Türleri**: Ateş, Şok, Zehir, Donma, Patlayıcı

**Mayın Sistemi**: Görünmez tuzaklar, basınç plakası ile kurulur. Mayın türleri: Patlama (TNT), Yıldırım (Lightning Core), Zehir (Spider Eye), Körlük (Ink Sac), Yorgunluk (Iron Pickaxe), Yavaşlık (Slime Ball). Kendi klan üyelerin mayınlarına basmaz!

**YENİ ÖZELLİK**: Tuzak sistemi performans optimizasyonları ile güncellendi. Detaylar için `08_tuzak_sistemi.md` dosyasına bakın.

---

### 6. Kuşatma ve Savaş

**Kuşatma Sistemi**: Düşman klanını yok etmenin resmi yolu.

**Nasıl Başlatılır?**:
```
1. Düşman klan 50 blok yakınına Beacon dik
2. 3x3 Obsidian piramit yap
3. TNT ile aktifleştir
4. 5 dakika hazırlık süresi
5. SAVAŞ BAŞLAR!
```

**Savaş Açma Koşulları**:
- **Yetki**: Sadece General ve Lider savaş açabilir
- **Aktif Üye**: Klanın %35'i aktif olmalı
- **General**: En az bir General aktif olmalı
- **Savunan Klan**: Karşı klandan en az 1 kişi online olmalı (offline baskın önleme)

**Kuşatma Silahları**:
- **Balista**: Stone Slab + Dispenser + 4 Iron Bars. Binilir, sol tıkla = ateş et. 30 mermi şarjör, 15sn yenileme.
- **Mancınık**: 3x3 Stone Brick Stairs. Binilir, magma bloğu fırlatır, alan hasarı. 10sn cooldown.

**Beyaz Bayrak - Pes Etme**:
- Savaşta kaybetmek üzereyseniz pes edebilirsiniz
- **Beyaz Bayrak** (White Banner) klan bölgende koy, Shift + Sağ tık (boş elle)
- Klan YOK OLMAZ (dağılmaz) ama sandıklardaki itemlerin YARISI gider
- Kasanın %50'si kazanan klana gider
- Sadece General ve Lider pes edebilir
- Kristal kırılmasından daha iyi (klan dağılmaz!)

**Zafer Ödülleri**: %50 kaybeden kasası + Fatih buff'ı (24 saat) + Yapı malzemeleri

**Ender Pearl Kısıtlaması**: Başkasının klan bölgesine Ender Pearl ile ışınlanamazsın!

---

### 7. Supply Drop ve Yarışma

**Supply Drop**: Gökyüzünden düşen hazine sandıkları. İlk bulan alır!

**İçerikler**:
- Garantili: 5-10 Diamond, 3-5 Emerald, 1-2 Netherite
- Rastgele: Elytra (%5), Notch Apple (%10), Tarif Kitabı (%2)

**Strateji**: Elytra ile uç, ilk ulaş, al ve kaç!

---

### 8. Özel Silahlar ve Araçlar

#### 🗡️ Özel Silahlar ve Zırhlar (25 Silah + 25 Zırh - 5 Seviye)

Stratocraft'ta **25 özel silah** ve **25 özel zırh** bulunur. Her silah ve zırhın benzersiz yetenekleri vardır ve **Boss Item'ları** ile craft edilir.

**Özel Zırhlar**: Her seviyede 5 farklı zırh seti (Helmet, Chestplate, Leggings, Boots) bulunur. Zırhlar, silahlarla birlikte kullanıldığında set bonusları verir.

**Seviye 1 Silahlar** (Goblin Kralı Taçı gerekir):
- **Hız Hançeri**: Elinde tutarken %20 hız verir, yüksek saldırı hızı
- **Çiftçi Tırpanı**: Alan hasarı (AoE) - 3 blok yarıçapındaki tüm moblara hasar
- **Yerçekimi Gürzü**: Sağ tıkla 5 blok yukarı fırlatır (kaçış için)
- **Patlayıcı Yay**: Okları patlar, blok kırmayan patlama
- **Vampir Dişi**: Vurduğun hasarın %20'si kadar can verir

**Seviye 2 Silahlar** (Troll Kralı Kalbi gerekir):
- **Alev Kılıcı**: Sağ tıkla alev dalgası atar, önündeki herkes yanar
- **Buz Asası**: Sağ tıkla hedefi 3 saniye dondurur
- **Zehirli Mızrak**: Fırlatıldığında zehir bulutu oluşturur
- **Golem Kalkanı**: Sneak+Sağ Tık ile dostları iyileştirir
- **Şok Baltası**: Kritik vuruşta yıldırım düşürür

**Seviye 3 Silahlar** (T-Rex Dişi gerekir):
- **Gölge Katanası**: Sağ tıkla 6 blok dash atar, yoluna çıkan her şeye hasar
- **Deprem Çekici**: Sağ tıkla yeri sarstırır, 5 blok çevredeki mobları havaya fırlatır
- **Taramalı Yay**: Sağ tık basılı tutulduğunda saniyede 5 ok atar
- **Büyücü Küresi**: Sağ tıkla en yakındaki 3 düşmana güdümlü mermi atar
- **Hayalet Hançeri**: Sağ tıkla 5 saniye görünmez olursun, ilk vuruş 3x hasar

**Seviye 4 Silahlar** (Titan Golem Çekirdeği gerekir - **Modlu**):
- **Element Kılıcı**: Mod 1 (Ateş) - Her vuruşta alev saçar | Mod 2 (Buz) - Yavaşlatır
- **Yaşam ve Ölüm**: Mod 1 (Ölüm) - Wither kafası fırlatır | Mod 2 (Yaşam) - Can basar
- **Mjölnir V2**: Mod 1 (Melee) - Zincirleme yıldırım | Mod 2 (Throw) - Fırlat ve geri döner
- **Avcı Yayı**: Mod 1 (Sniper) - 50 blok öteden x2 hasar | Mod 2 (Shotgun) - 5 ok birden
- **Manyetik Eldiven**: Mod 1 (Çek) - Hedefi kendine çeker | Mod 2 (İt) - Uzağa fırlatır

**Seviye 5 Silahlar** (Void Dragon Heart gerekir - **Modlu**):
- **Hiperiyon Kılıcı**: Mod 1 (Işınlanma) - 8 blok ışınlan ve patlat | Mod 2 (Kara Delik) - Gelen okları emer
- **Meteor Çağıran**: Mod 1 (Kıyamet) - Gökyüzünden 3 meteor | Mod 2 (Yer Yaran) - Lav yarığı aç
- **Titan Katili**: Mod 1 (%5 Hasar) - Bosslara mevcut canının %5'i | Mod 2 (Mızrak Yağmuru) - 10 mızrak düşer
- **Ruh Biçen**: Mod 1 (Çağır) - Öldürdüğün 3 mobu hortlak olarak çağır | Mod 2 (Patlat) - Hortlakları patlat
- **Zamanı Büken**: Mod 1 (Durdur) - 10 saniye tüm moblar donar | Mod 2 (Geri Sar) - 5 saniye önceki konuma dön

**Mod Değiştirme**: Tier 4 ve Tier 5 silahlar için **Shift+Sağ Tık** ile mod seçim menüsü açılır!

**Crafting**: Tüm silahlar crafting masasında yapılabilir. Her silah için **Boss Item** ve **Tarif Kitabı** gerekir. Detaylar için `05_ozel_esyalar.md` dosyasına bakın.

---

#### 🔧 Diğer Özel Araçlar

**Kancalar**:
- **Paslı Kanca**: 7 blok menzil, ucuz
- **Titan Kancası**: 40 blok menzil, Slow Falling buff, pahalı

**Casusluk Dürbünü**: 3 saniye hedefe bak, GUI menü açılır! Can, zırh, açlık, envanter ve aktif iksir efektlerini görüntüle.

**Cinsiyet Tarayıcısı**: Eğitilmiş canlıların cinsiyetini gösterir. Canlıya sağ tıkla, "♂ Erkek" veya "♀ Dişi" bilgisini al. Üreme için gerekli!

---

## 🌍 Dünya Nasıl Çalışır?

### Fiziksel Etkileşim Felsefesi

**ÖNEMLİ**: Bu oyunda **hiçbir `/komut` kullanılmaz**. Her şey **fiziksel blok düzenekleriyle** yapılır!

**Örnekler**:
- Klan kurmak → Çitlerle çevrele, kristal koy
- Üye almak → Ritüel platformu kur, çakmak ile aktifleştir
- Batarya yüklemek → Blokları diz, yakıt ile aktifleştir
- Tuzak kurmak → Çekirdek + çerçeve + yakıt

**Neden?**: Oyun **mühendislik ve strateji** üzerine kurulu. Komutlar değil, fiziksel düzenekler güç verir!

---

### Zorluk Progresyonu

Dünya **merkezden uzaklaştıkça zorlaşır**:

```
Merkez (0-200 blok): Güvenli başlangıç alanı
Seviye 1 (200-1000): Kolay moblar, temel madenler
Seviye 2 (1000-3000): Orta moblar, Titanyum
Seviye 3 (3000-5000): Zor moblar, Mithril
Seviye 4 (5000-10000): Çok zor moblar, Astral
Seviye 5 (10000+): Efsanevi moblar, Kızıl Elmas
```

**Felaketler**: 5000+ blok uzakta başlar, merkeze doğru ilerler!

---

### Biyom ve Zindan Sistemi

**Biyom Değişimi**: Zorluk seviyesine göre biyomlar değişir (Forest → Taiga → Nether → End)

**Zindanlar**: Yeraltında otomatik spawn olur (%5 şans), zorluk seviyesine göre içerik değişir.

---

## 🎮 Oyun Stratejisi: Nasıl Başlamalı?

### İlk Günler (0-1000 blok)

```
1. Temel ekipman topla (taş, demir)
2. Merkeze yakın kal (güvenli)
3. Klan kurmayı planla
4. Temel madenler topla (Kükürt, Boksit)
5. Goblin Kralı avla → Goblin Kralı Taçı al
6. İlk özel silahı craft et (Hız Hançeri, Çiftçi Tırpanı vb.)
```

---

### Orta Dönem (1000-3000 blok)

```
1. Klan kur (çitler + kristal)
2. İlk yapıları kur (Simya Kulesi Lv1, Zehir Reaktörü Lv1)
3. Titanyum madenciliğine başla
4. İlk bataryaları öğren (Ateş Topu)
5. Ork ve Troll eğit
6. Troll Kralı avla → Troll Kralı Kalbi al
7. Seviye 2 silahları craft et (Alev Kılıcı, Buz Asası vb.)
```

---

### İleri Dönem (3000-5000 blok)

```
1. Mithril topla
2. Yapıları geliştir (Lv3-4)
3. Ejderha ve Griffin avla
4. T-Rex avla → T-Rex Dişi al
5. Seviye 3 silahları craft et (Gölge Katanası, Deprem Çekici vb.)
6. Kuşatma silahları kur (Balista, Mancınık)
7. Klan savaşlarına hazırlan
```

---

### Uzman Dönem (5000+ blok)

```
1. Astral Cevheri ve Kızıl Elmas ara
2. Tektonik Sabitleyici kur (Felaket koruması)
3. Titan Golem avla → Titan Golem Çekirdeği al
4. Seviye 4 modlu silahları craft et (Element Kılıcı, Mjölnir V2 vb.)
5. Void Dragon avla → Void Dragon Heart al
6. Seviye 5 modlu silahları craft et (Hiperiyon Kılıcı, Zamanı Büken vb.)
7. Titan Golem ve Hydra eğit
8. Efsanevi ekipman craft et
9. Felaketlere karşı savaş!
```

---

## ⚠️ Önemli Kurallar ve İpuçları

### 1. Komut Yok, Fiziksel Etkileşim Var

- Klan kurmak için `/klan` yok → Çitlerle çevrele
- Üye almak için `/davet` yok → Ritüel platformu kur
- Batarya yüklemek için `/büyü` yok → Blokları diz, yakıt ekle

**Felsefe**: Mühendislik = Güç!

---

### 2. Takım Çalışması Zorunlu

- Felaketler tek başına yenilemez (minimum 3-5 oyuncu)
- Klan savaşları koordinasyon gerektirir
- Boss avı takım halinde yapılmalı

**Strateji**: Klan kur, rolleri dağıt (savaşçı, mühendis, tüccar, madencı)

---

### 3. Kaynak Yönetimi

- **Offline Koruma**: Klan üyeleri offline iken kristale yakıt ekle (Kömür, Kükürt, Karanlık Madde). Her saldırıda 1 birim yakıt tüketilir, maksimum 12 saat koruma.
- **Bataryalar**: Yakıt hazır tut (Elmas, Kızıl Elmas, Karanlık Madde)
- **Yapılar**: Boss malzemeleri topla

**Ekonomi**: Kontratlar, kervanlar, marketler ve görevler ile para kazan!
- **Kontratlar**: `/kontrat` komutu ile GUI menüden kontrat al (Güvenlik: Performans optimizasyonları, can kaybı geri kazanım)
- **Kervanlar**: Uzak bölgelere malzeme taşı
- **Marketler**: Eşya satıp al, teklif ver (Güvenlik: Dupe önleme, vergi kaçırma önleme, stok senkronizasyonu)
- **Görevler**: Totem'e sağ tık yap, görev al, tamamla (Güvenlik: Envanter kontrolü, ödül yere düşme)

---

### 4. Stratejik Planlama

- Bölge genişletmeyi aşamalı yap (20x20 → 40x40 → 70x70)
- Savunma katmanları kur (dış tuzaklar → orta yapılar → iç kristal)
- Felaket uyarılarını dinle, hazırlık yap

**Taktik**: Her felaket tipine özel strateji gerekir!

---

## 📚 Döküman Yapısı

Bu döküman, Stratocraft'ın temel felsefesini ve amacını anlatır. Detaylı bilgiler için diğer dökümanlara bakın:

- **01_klan_sistemi.md**: Klan kurma, üye yönetimi, rütbe sistemi
- **02_bolge_sistemi.md**: Bölge oluşturma, genişletme, korumalar
- **03_rituel_sistemi.md**: Tüm ritüeller (üye, terfi, savaş) - Not: Klan kurma artık sadece Klan Kristali ile yapılır
- **04_batarya_sistemi.md**: Büyü sistemi, batarya türleri, stratejiler
- **05_ozel_esyalar.md**: Yeni madenler, **25 özel silah** (detaylı açıklamalar, crafting tarifleri, mod sistemi), eşyalar
- **06_ozel_moblar.md**: Özel moblar, eğitme, binme
- **07_yapilar.md**: Yapı kategorileri, seviyeler, stratejiler
- **08_tuzak_sistemi.md**: Tuzak kurulumu, türleri, mayınlar (YENİ: Performans optimizasyonları)
- **09_kusatma_sistemi.md**: Kuşatma başlatma, savaş kuralları, ödüller
- **10_felaketler.md**: Felaket mekaniği, türleri, mücadele stratejileri
- **11_kontrat_sistemi.md**: Kontrat oluşturma, kan imzası, ihlal sistemi
- **12_kervan_sistemi.md**: Kervan oluşturma, yolculuk, riskler
- **13_ozel_araclar.md**: Kancalar, dürbün, stratejik kullanım
- **14_supply_drop.md**: Supply drop mekaniği, yarışma, ödüller
- **15_arastirma_sistemi.md**: Tarif kitabı sistemi, araştırma masası, boss item doğrulama (YENİ: Performans optimizasyonları ve güvenlik iyileştirmeleri)
- **22_boss_sistemi.md**: Boss çağırma, yetenekler, arena transformasyonu (YENİ: Config entegrasyonu, dinamik öncelik sistemi, admin komutları, performans metrikleri)
- **16-19_diger_sistemler.md**: Görev sistemi (GUI menü, 8 görev tipi), antrenman, lojistik, ekonomi
- **21_market_sistemi.md**: Market kurulumu, teklif sistemi, alışveriş, güvenlik özellikleri
- **17_egitme_sistemi.md**: Canlı eğitme, ritüeller, binme
- **18_ureme_sistemi.md**: Çiftleştirme, üreme tesisleri, yumurta
- **19_zorluk_sistemi.md**: Zorluk bölgeleri, mob/maden kısıtlamaları
- **20_admin_komutlari.md**: Admin komutları, test araçları, `/sgp` güç sistemi komutları

---

## 🎯 Sonuç: Oyunun Özü

Stratocraft, **mühendislik ve strateji** üzerine kurulu bir oyundur. Komutlar değil, **fiziksel düzenekler** güç verir. Tek başına hayatta kalamazsın; **klan kur, yapılar inşa et, felaketlere karşı savaş, dünyaya hükmet!**

**Unutma**: 
- ✅ Fiziksel etkileşim = Güç
- ✅ Takım çalışması = Hayatta kalma
- ✅ Mühendislik = Zafer
- ✅ Strateji = Üstünlük
- ✅ Güç Sistemi = Adil Rekabet (PvP koruma, dinamik felaket zorluğu)

---

## 🆕 YENİ ÖZELLİKLER (Son Güncellemeler)

### ⚡ Güç Sistemi (Stratocraft Power System - SGP)

**Tamamlanan Özellikler:**
- ✅ **Oyuncu Güç Hesaplama**: Eşya, ustalık, ritüel, buff güçleri
- ✅ **Klan Güç Hesaplama**: Üye güçleri, yapılar, ritüel blokları/kaynakları
- ✅ **Hibrit Seviye Sistemi**: Oyuncu seviyesi (karekök + logaritmik), Klan seviyesi (logaritmik)
- ✅ **PvP Koruma Sistemi**: Onurlu savaş aralığı, acemi koruması, klan içi koruma
- ✅ **Histerezis Sistemi**: Zırh çıkarma exploit önleme (60 saniye gecikme)
- ✅ **Felaket Entegrasyonu**: Dinamik zorluk sistemi (oyuncu gücüne göre)
- ✅ **4 Fazlı Felaket Sistemi**: Keşif → Saldırı → Öfke → Çaresizlik
- ✅ **Ritüel Güç Entegrasyonu**: Başarılı ritüeller ve batarya ateşlemeleri güç verir
- ✅ **HUD Entegrasyonu**: Güç bilgisi sağ taraftaki bilgi panosunda görünür
- ✅ **Komut Sistemi**: `/sgp` komutu ile güç görüntüleme, top sıralamaları
- ✅ **Performans Optimizasyonları**: Cache sistemleri, thread-safety, event-based tracking

**Detaylar için:**
- `01_klan_sistemi.md` - Klan Güç Sistemi bölümü
- `10_felaketler.md` - Dinamik Güç Sistemi ve Faz Sistemi bölümleri
- `03_rituel_sistemi.md` - Ritüel Güç Sistemi bölümü
- `04_batarya_sistemi.md` - Batarya Güç Sistemi bölümü
- `05_ozel_esyalar.md` - Eşya Güç Sistemi bölümü
- `17_egitme_sistemi.md` - Ustalık Güç Sistemi bölümü
- `07_yapilar.md` - Yapı Güç Sistemi bölümü
- `20_admin_komutlari.md` - Güç Sistemi Komutları bölümü
- `19_zorluk_sistemi.md` - Dinamik Zorluk Sistemi bölümü

---

**🎮 Klanını kur, bölgeni genişlet, yapılarını inşa et, felaketlere hükmet!**

---

*Bu döküman, Stratocraft'ın tüm sistemlerini kapsayan genel bir giriş rehberidir. Detaylı bilgiler için ilgili dökümanlara bakın.*

