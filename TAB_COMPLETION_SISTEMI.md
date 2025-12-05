# 📋 STRATOCRAFT ADMIN KOMUTLARI - TAB COMPLETION SİSTEMİ (GÜNCELLENMİŞ)

## 🔍 TAB COMPLETION HİYERARŞİSİ

### 1. İlk Seviye: `/stratocraft [TAB]`

**Çıkan Komutlar:**
- `give` - Eşya verme
- `spawn` - Mob spawn etme
- `disaster` - Felaket yönetimi
- `list` - Liste görüntüleme
- `help` - Yardım
- `siege` - Kuşatma yönetimi
- `clan` - Klan yönetimi ⭐ (YENİ)
- `contract` - Kontrat yönetimi
- `build` - Yapı/Batarya oluşturma
- `trap` - Tuzak yönetimi
- `dungeon` - Zindan yönetimi
- `biome` - Biome yönetimi
- `mine` - Mayın yönetimi
- `recipe` - Tarif yönetimi
- `boss` - Boss yönetimi
- `tame` - Eğitme yönetimi

**NOT:** `ballista` ve `caravan` komutları kaldırıldı. `ballista` artık `build ballista` altında, `caravan` ise `clan caravan` altında.

---

### 2. İkinci Seviye: Komut Parametreleri

#### `/stratocraft give [TAB]`
**Çıkan Kategoriler:**
- `weapon` - Silahlar
- `armor` - Zırhlar
- `material` - Malzemeler
- `mobdrop` - Mob drop'ları
- `special` - Özel eşyalar
- `ore` - Madenler
- `tool` - Araçlar
- `bossitem` - Boss itemleri
- `recipebook` - Tarif kitapları

#### `/stratocraft spawn [TAB]`
**Çıkan Kategoriler:**
- `level1` - Seviye 1 moblar
- `level2` - Seviye 2 moblar
- `level3` - Seviye 3 moblar
- `level4` - Seviye 4 moblar
- `level5` - Seviye 5 moblar
- `boss` - Boss moblar
- `special` - Özel moblar

#### `/stratocraft disaster [TAB]`
**Çıkan Komutlar:**
- `start` - Felaket başlat
- `stop` - Felaket durdur
- `info` - Felaket bilgisi
- `list` - Felaket listesi
- `clear` - Felaketleri temizle

#### `/stratocraft build [TAB]`
**Çıkan Kategoriler:**
- `weapon` - Silah yapıları
- `battery` - Bataryalar ⚡
- `structure` - Yapılar
- `ballista` - Balista yönetimi ⭐ (YENİ - build altına taşındı)

#### `/stratocraft clan [TAB]` ⭐ (YENİ)
**Çıkan Komutlar:**
- `list` - Tüm klanları listele
- `info` - Klan bilgisi
- `create` - Klan oluştur
- `disband` - Klanı dağıt
- `addmember` - Üye ekle
- `removemember` - Üye çıkar
- `caravan` - Kervan yönetimi ⭐ (clan altına taşındı)

#### `/stratocraft trap [TAB]`
**Çıkan Komutlar:**
- `list` - Tuzak listesi
- `give` - Tuzak ver
- `build` - Tuzak yapısı oluştur

#### `/stratocraft mine [TAB]`
**Çıkan Komutlar:**
- `list` - Mayın listesi
- `give` - Mayın ver

#### `/stratocraft dungeon [TAB]`
**Çıkan Komutlar:**
- `spawn` - Zindan spawn et
- `list` - Zindan listesi
- `clear` - Zindanları temizle

#### `/stratocraft biome [TAB]`
**Çıkan Komutlar:**
- `list` - Biome listesi
- `set` - Biome ayarla

#### `/stratocraft boss [TAB]`
**Çıkan Komutlar:**
- `spawn` - Boss spawn et
- `list` - Boss listesi
- `ritual` - Boss ritüeli
- `build` - Boss yapısı

#### `/stratocraft tame [TAB]`
**Çıkan Komutlar:**
- `ritual` - Eğitme ritüeli
- `list` - Eğitme listesi
- `info` - Eğitme bilgisi
- `build` - Eğitme yapısı
- `instant` - Anında eğitme
- `breed` - Üreme
- `facility` - Üreme tesisi

#### `/stratocraft recipe [TAB]`
**Çıkan Komutlar:**
- `remove` - Tarif kaldır
- `removeall` - Tüm tarifleri kaldır
- `list` - Tarif listesi

#### `/stratocraft ballista [TAB]` ❌ (KALDIRILDI)
**NOT:** Bu komut artık `/stratocraft build ballista` altında.

---

### 3. Üçüncü Seviye: Kategori Parametreleri

#### `/stratocraft build battery [TAB]` ⚡
**Çıkan Seviyeler:**
- `1` - Seviye 1 bataryalar
- `2` - Seviye 2 bataryalar
- `3` - Seviye 3 bataryalar
- `4` - Seviye 4 bataryalar
- `5` - Seviye 5 bataryalar

**ÖNEMLİ:** Batarya komutları için önce seviye seçilmelidir!

#### `/stratocraft build ballista [TAB]` ⭐ (YENİ)
**Çıkan Komutlar:**
- `spawn` / `create` - Balista oluştur
- `remove` - Balista kaldır
- `reload` - Balista mermisini doldur

#### `/stratocraft disaster start [TAB]` ⭐ (GÜNCELLENDİ)
**Çıkan Seviyeler:**
- `1` - Seviye 1 felaket
- `2` - Seviye 2 felaket
- `3` - Seviye 3 felaket

**ÖNEMLİ:** Felaket komutları için önce seviye seçilmelidir!

#### `/stratocraft give weapon [TAB]`
**Çıkan Seviyeler:**
- `1` - Seviye 1 silahlar
- `2` - Seviye 2 silahlar
- `3` - Seviye 3 silahlar
- `4` - Seviye 4 silahlar
- `5` - Seviye 5 silahlar

#### `/stratocraft give armor [TAB]`
**Çıkan Seviyeler:**
- `1` - Seviye 1 zırhlar
- `2` - Seviye 2 zırhlar
- `3` - Seviye 3 zırhlar
- `4` - Seviye 4 zırhlar
- `5` - Seviye 5 zırhlar

#### `/stratocraft clan caravan [TAB]` ⭐ (YENİ)
**Çıkan Komutlar:**
- `list` - Kervan listesi
- `clear` - Kervanları temizle

---

### 4. Dördüncü Seviye: Seviyeli Itemler

#### `/stratocraft build battery 1 [TAB]` ⚡
**Çıkan Batarya İsimleri (Seviye 1):**
- `Yıldırım Asası`
- `Cehennem Topu`
- `Buz Topu`
- `Zehir Oku`
- `Şok Dalgası`
- `Taş Köprü`
- `Obsidyen Duvar`
- `Demir Kafes`
- `Cam Duvar`
- `Ahşap Barikat`
- `Can Yenileme`
- `Hız Artışı`
- `Hasar Artışı`
- `Zırh Artışı`
- `Yenilenme`

**Toplam: 15 batarya (5 Saldırı + 5 Oluşturma + 5 Destek)**

#### `/stratocraft build battery 2 [TAB]` ⚡
**Çıkan Batarya İsimleri (Seviye 2):**
- `Çift Ateş Topu`
- `Zincir Yıldırım`
- `Buz Fırtınası`
- `Asit Yağmuru`
- `Elektrik Ağı`
- `Obsidyen Kafes`
- `Taş Köprü (Gelişmiş)`
- `Demir Duvar`
- `Cam Tünel`
- `Ahşap Kale`
- `Can + Hız Kombinasyonu`
- `Hasar + Zırh Kombinasyonu`
- `Yenilenme + Can Kombinasyonu`
- `Hız + Hasar Kombinasyonu`
- `Zırh + Yenilenme Kombinasyonu`

**Toplam: 15 batarya (5 Saldırı + 5 Oluşturma + 5 Destek)**

#### `/stratocraft build battery 3 [TAB]` ⚡
**Çıkan Batarya İsimleri (Seviye 3):**
- `Meteor Yağmuru`
- `Yıldırım Fırtınası`
- `Buz Çağı`
- `Zehir Bombası`
- `Elektrik Fırtınası`
- `Obsidyen Kale`
- `Netherite Köprü`
- `Demir Hapishane`
- `Cam Kule`
- `Taş Kale`
- `Absorption Kalkanı`
- `Uçma Yeteneği`
- `Kritik Vuruş Artışı`
- `Yansıtma Kalkanı`
- `Can Çalma`

**Toplam: 15 batarya (5 Saldırı + 5 Oluşturma + 5 Destek)**

#### `/stratocraft build battery 4 [TAB]` ⚡
**Çıkan Batarya İsimleri (Seviye 4):**
- `Tesla Kulesi`
- `Cehennem Ateşi`
- `Buz Kalesi`
- `Ölüm Bulutu`
- `Elektrik Kalkanı`
- `Obsidyen Hapishane`
- `Netherite Köprü (Gelişmiş)`
- `Demir Kale`
- `Cam Kule (Gelişmiş)`
- `Taş Şato`
- `Tam Can + Absorption`
- `Zaman Yavaşlatma`
- `Yıldırım Vuruşu`
- `Görünmezlik Kalkanı`
- `Ölümsüzlük Anı`

**Toplam: 15 batarya (5 Saldırı + 5 Oluşturma + 5 Destek)**

#### `/stratocraft build battery 5 [TAB]` ⚡
**Çıkan Batarya İsimleri (Seviye 5):**
- `Kıyamet Reaktörü`
- `Lava Tufanı`
- `Boss Katili`
- `Alan Yok Edici`
- `Dağ Yok Edici`
- `Obsidyen Hapishane (Efsanevi)`
- `Netherite Köprü (Efsanevi)`
- `Demir Kale (Efsanevi)`
- `Cam Kule (Efsanevi)`
- `Taş Kalesi (Efsanevi)`
- `Efsanevi Can Yenileme`
- `Zaman Durdurma`
- `Ölüm Dokunuşu`
- `Faz Değiştirme`
- `Yeniden Doğuş`

**Toplam: 15 batarya (5 Saldırı + 5 Oluşturma + 5 Destek)**

#### `/stratocraft give weapon 1 [TAB]` ⭐ (GÜNCELLENDİ)
**Çıkan Item İsimleri:**
- `weapon_l1_1` - Hız Hançeri
- `weapon_l1_2` - Çiftçi Tırpanı
- `weapon_l1_3` - Yerçekimi Gürzü
- `weapon_l1_4` - Patlayıcı Yay
- `weapon_l1_5` - Vampir Dişi

**NOT:** Artık `sword`, `axe` gibi genel tipler yerine gerçek item isimleri gösteriliyor!

#### `/stratocraft give armor 1 [TAB]` ⭐ (GÜNCELLENDİ)
**Çıkan Item İsimleri:**
- `armor_l1_1` - Demir Savaşçı Zırhı
- `armor_l1_2` - Demir Koruyucu Zırhı
- `armor_l1_3` - Demir Avcı Zırhı
- `armor_l1_4` - Demir Kaşif Zırhı
- `armor_l1_5` - Demir Şövalye Zırhı

**NOT:** Artık `helmet`, `chestplate` gibi genel tipler yerine gerçek item isimleri gösteriliyor!

#### `/stratocraft disaster start 1 [TAB]` ⭐ (GÜNCELLENDİ)
**Çıkan Felaket Tipleri:**
- `TITAN_GOLEM`
- `ABYSSAL_WORM`
- `CHAOS_DRAGON`
- `VOID_TITAN`
- `ICE_LEVIATHAN`
- `SOLAR_FLARE`
- `EARTHQUAKE`
- `METEOR_SHOWER`
- `VOLCANIC_ERUPTION`

**ÖNEMLİ:** Artık önce seviye, sonra felaket tipi seçiliyor!

---

## 📝 ÖNEMLİ NOTLAR

### Batarya Komutları Formatı
```
/stratocraft build battery <seviye> <isim>
```

**Örnekler:**
- `/stratocraft build battery 1 Yıldırım Asası`
- `/stratocraft build battery 5 Kıyamet Reaktörü`
- `/stratocraft build battery 3 Can Çalma`

### Felaket Komutları Formatı ⭐ (GÜNCELLENDİ)
```
/stratocraft disaster start <seviye> <tip>
```

**Örnekler:**
- `/stratocraft disaster start 1 TITAN_GOLEM`
- `/stratocraft disaster start 3 VOLCANIC_ERUPTION`

### Klan Komutları Formatı ⭐ (YENİ)
```
/stratocraft clan <komut> [parametreler]
```

**Örnekler:**
- `/stratocraft clan list`
- `/stratocraft clan create TestKlan`
- `/stratocraft clan caravan list`

### Balista Komutları Formatı ⭐ (GÜNCELLENDİ)
```
/stratocraft build ballista <spawn|remove|reload>
```

**Örnekler:**
- `/stratocraft build ballista spawn`
- `/stratocraft build ballista remove`

### Tab Completion Özellikleri
1. **Otomatik Filtreleme**: Yazdığınız harflere göre otomatik filtreleme yapılır
2. **Seviye Önceliği**: Bataryalar ve felaketler için önce seviye seçilir, sonra isim/tip önerilir
3. **Türkçe Karakter Desteği**: Türkçe karakterler normalize edilir (ç→c, ğ→g, vb.)
4. **Büyük/Küçük Harf Duyarsız**: Tüm tab completion büyük/küçük harf duyarsızdır

### Eski Sistem Kaldırıldı ❌
- Eski batarya isimleri (`magma_battery`, `lightning_battery`, vb.) artık desteklenmiyor
- Sadece yeni batarya sistemi (`/stratocraft build battery <seviye> <isim>`) kullanılıyor
- `ballista` komutu artık `build ballista` altında
- `caravan` komutu artık `clan caravan` altında

---

## ✅ TAB COMPLETION KONTROL LİSTESİ

- [x] İlk seviye komutlar (16 komut - ballista ve caravan kaldırıldı, clan eklendi)
- [x] İkinci seviye kategoriler (her komut için)
- [x] Üçüncü seviye parametreler (seviyeler, tipler)
- [x] Dördüncü seviye item isimleri (75 batarya dahil)
- [x] Batarya komutları: Seviye önceliği çalışıyor
- [x] Felaket komutları: Seviye önceliği çalışıyor ⭐
- [x] Silah/Zırh komutları: Gerçek item isimleri gösteriliyor ⭐
- [x] Klan komutları: Tam entegrasyon ⭐
- [x] Balista komutları: Build altına taşındı ⭐
- [x] Türkçe karakter normalizasyonu: Aktif
- [x] Filtreleme sistemi: Çalışıyor
- [x] Eski sistem kaldırıldı: Tamamen temizlendi ❌

---

**Son Güncelleme:** Tüm tab completion sistemi güncellendi ve düzeltildi! 🎉

**Değişiklikler:**
- ✅ `ballista` komutu `build ballista` altına taşındı
- ✅ `caravan` komutu `clan caravan` altına taşındı
- ✅ `clan` komutu eklendi
- ✅ `disaster start` için önce seviye, sonra felaket tipi
- ✅ `give weapon/armor` için gerçek item isimleri (weapon_l1_1, armor_l1_1)
- ✅ Eski batarya sistemi tamamen kaldırıldı
