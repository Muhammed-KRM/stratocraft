# STRATOCRAFT - ADMIN KOMUTLARI

## 👑 Admin Komutları Nedir?

Admin komutları, **sadece yöneticilerin** kullanabileceği özel komutlardır. Oyunun tüm mekaniklerini test edebilir ve yönetebilirsiniz.

**KOD DOĞRULANDI**: AdminCommandExecutor.java'dan tüm komutlar doğrulanmıştır.

**YETKİ**: `stratocraft.admin` permission gerekli

---

## 📋 İÇİNDEKİLER

1. [Temel Komutlar](#temel-komutlar)
2. [Eşya Komutları](#eşya-komutlari)
3. [Mob Komutları](#mob-komutlari)
4. [Sistem Komutları](#sistem-komutlari)
5. [Klan Komutları](#klan-komutlari) ⭐ YENİ
6. [Güç Sistemi Komutları](#güç-sistemi-komutlari) ⭐ YENİ

---

## 🎮 TEMEL KOMUTLAR

### `/scadmin help`

**Açıklama**: Tüm admin komutlarını listeler

**Kullanım**:
```
/scadmin help
/scadmin
```

**Çıktı**: Komut listesi

---

## 🏗️ YAPI KOMUTLARI ⭐ GÜNCELLENDİ

### `/scadmin build structure <type> [level]`

**Açıklama**: Yapı build et (test için)

**YENİ ÖZELLİKLER** ⭐:
- **Yapı Çekirdeği Sistemi**: Tüm yapılar OAK_LOG + metadata ile çalışır
- **Otomatik Çekirdek Yerleştirme**: Build komutu yapı çekirdeğini otomatik yerleştirir
- **StructureCoreManager Entegrasyonu**: Çekirdek otomatik olarak kayıt edilir

**Kullanım**:
```
/scadmin build structure alchemy_tower 1
/scadmin build structure clan_bank 1
/scadmin build structure contract_office 1
```

**Desteklenen Yapılar**:
- `personal_mission_guild` - Kişisel Görev Loncası
- `clan_management_center` - Klan Yönetim Merkezi
- `clan_bank` - Klan Bankası
- `clan_mission_guild` - Klan Görev Loncası
- `training_arena` - Antrenman Arenası
- `caravan_station` - Kervan İstasyonu
- `contract_office` - Kontrat Bürosu
- `market_place` - Market
- `recipe_library` - Tarif Kütüphanesi
- `alchemy_tower` - Simya Kulesi (şema tabanlı)
- `tectonic_stabilizer` - Tektonik Sabitleyici (şema tabanlı)
- ... (diğer yapılar)

**Yapı Çekirdeği Detayları**:
- **Material**: OAK_LOG (normal OAK_LOG'dan farklı, metadata ile işaretli)
- **Metadata**: `METADATA_KEY_CORE`, `METADATA_KEY_OWNER`
- **Yerleştirme**: Build komutu otomatik olarak çekirdeği yerleştirir ve kayıt eder
- **Aktivasyon**: Yapı çekirdeği yerleştirildikten sonra yapı kurulur ve aktivasyon item'ı ile aktifleştirilir

**Not**: Build komutu yapıyı tam olarak build eder, ancak aktivasyon için doğru item gerekir.

---

## 🎁 EŞYA KOMUTLARI

### `/scadmin give <kategori> <item> [miktar]`

**Açıklama**: Özel eşya ver (kategorize edilmiş)

**KOD DOĞRULANDI**:
```java
// AdminCommandExecutor.java satır 168-220
// Miktar kontrolü: 1-2304 (36 slot * 64 stack)
if (amount > 2304) {
    p.sendMessage("§cMiktar çok yüksek! Maksimum 2304.");
}
```

**Kategoriler**:
- `weapon` - Silahlar
- `armor` - Zırhlar
- `material` - Malzemeler
- `mobdrop` - Mob dropları
- `special` - Özel eşyalar
- `ore` - Cevherler
- `tool` - Araçlar
- `bossitem` - Boss özel itemleri (11 adet)
- `recipebook` - Tarif kitapları (70+ adet)

**Kullanım**:
```
/scadmin give material titanium_ingot 64
/scadmin give material red_diamond 10
/scadmin give material dark_matter 5
/scadmin give bossitem goblin_crown 1
/scadmin give recipebook armor_l1_1 1
/scadmin give recipebook weapon_l5_3 1
```

**Özel Eşyalar**:
```
titanium_ingot - Titanyum Külçesi
titanium_ore - Titanyum Parçası
red_diamond - Kızıl Elmas
dark_matter - Karanlık Madde
star_core - Yıldız Çekirdeği
adamantite - Adamantite
ruby - Yakut
flame_amplifier - Alev Amplifikatörü
devil_horn - Şeytan Boynuzu
devil_snake_eye - İblis Yılanın Gözü
blueprint_paper - Mühendis Şeması
lightning_core - Yıldırım Çekirdeği
war_fan - Savaş Yelpazesi
tower_shield - Kule Kalkanı
hell_fruit - Cehennem Meyvesi
rusty_hook - Paslı Kanca
titan_grapple - Titan Kancası
trap_core - Tuzak Çekirdeği

Yeni Madenler:
sulfur_ore - Kükürt Cevheri
sulfur - Kükürt
bauxite_ore - Boksit Cevheri
bauxite_ingot - Boksit Külçesi
rock_salt_ore - Tuz Kayası
rock_salt - Tuz
mithril_ore - Mithril Cevheri
mithril_ingot - Mithril Külçesi
mithril_string - Mithril İpi
astral_ore - Astral Cevheri
astral_crystal - Astral Kristali

Yeni Güçlü Yiyecekler:
life_elixir - Yaşam İksiri (can ve açlık fulleyen)
power_fruit - Güç Meyvesi (5x hasar artışı, 30 saniye)
speed_elixir - Hız İksiri (hız artışı, 2 dakika)
regeneration_elixir - Yenilenme İksiri (can yenileme, 1 dakika)
strength_elixir - Güç İksiri (güç artışı, 2 dakika)

Boss İtemleri (bossitem kategorisi):
goblin_crown - Goblin Kralı Taçı
orc_amulet - Ork Şefi Amuleti
troll_heart - Troll Kralı Kalbi
dragon_scale - Ejderha Ölçeği
trex_tooth - T-Rex Dişi
cyclops_eye - Cyclops Gözü
titan_core - Titan Golem Çekirdeği
phoenix_feather - Phoenix Tüyü
kraken_tentacle - Kraken Dokunaçı
demon_lord_horn - Şeytan Lordu Boynuzu
void_dragon_heart - Void Dragon Heart

Tarif Kitapları (recipebook kategorisi - 70+ adet):
Silah Tarifleri (25 adet):
recipe_weapon_l1_1 ila recipe_weapon_l1_5 - Seviye 1 Silah Tarifleri
recipe_weapon_l2_1 ila recipe_weapon_l2_5 - Seviye 2 Silah Tarifleri
recipe_weapon_l3_1 ila recipe_weapon_l3_5 - Seviye 3 Silah Tarifleri
recipe_weapon_l4_1 ila recipe_weapon_l4_5 - Seviye 4 Silah Tarifleri
recipe_weapon_l5_1 ila recipe_weapon_l5_5 - Seviye 5 Silah Tarifleri

Zırh Tarifleri (25 adet):
recipe_armor_l1_1 ila recipe_armor_l1_5 - Seviye 1 Zırh Tarifleri
recipe_armor_l2_1 ila recipe_armor_l2_5 - Seviye 2 Zırh Tarifleri
recipe_armor_l3_1 ila recipe_armor_l3_5 - Seviye 3 Zırh Tarifleri
recipe_armor_l4_1 ila recipe_armor_l4_5 - Seviye 4 Zırh Tarifleri
recipe_armor_l5_1 ila recipe_armor_l5_5 - Seviye 5 Zırh Tarifleri

Yapı Tarifleri (24 adet):
recipe_core - Ana Kristal
recipe_alchemy_tower - Simya Kulesi
recipe_siege_factory - Kuşatma Fabrikası
recipe_wall_generator - Sur Jeneratörü
... (diğer yapılar)

Özel Eşya Tarifleri (30+ adet):
recipe_lightning_core - Yıldırım Çekirdeği
recipe_titanium_ingot - Titanyum Külçesi
recipe_dark_matter - Karanlık Madde
recipe_blueprint_paper - Mühendis Şeması
recipe_life_elixir - Yaşam İksiri
recipe_power_fruit - Güç Meyvesi
recipe_speed_elixir - Hız İksiri
recipe_regeneration_elixir - Yenilenme İksiri
recipe_strength_elixir - Güç İksiri
recipe_sulfur_ore - Kükürt Cevheri
recipe_bauxite_ore - Boksit Cevheri
recipe_mithril_ore - Mithril Cevheri
recipe_astral_ore - Astral Cevheri
recipe_taming_core - Eğitim Çekirdeği
recipe_summon_core - Çağırma Çekirdeği
recipe_breeding_core - Üreme Çekirdeği
recipe_gender_scanner - Cinsiyet Ayırıcı
... (diğer özel eşyalar)
```

**Yeni Özel Çekirdekler ve Araçlar** (tool kategorisi):
```
taming_core - Eğitim Çekirdeği
summon_core - Çağırma Çekirdeği
breeding_core - Üreme Çekirdeği
gender_scanner - Cinsiyet Tarayıcısı
```

**Özel Zırhlar** (armor kategorisi):
```
armor_l1_1 ila armor_l1_5 - Seviye 1 Zırhlar (5 varyant)
armor_l2_1 ila armor_l2_5 - Seviye 2 Zırhlar (5 varyant)
armor_l3_1 ila armor_l3_5 - Seviye 3 Zırhlar (5 varyant)
armor_l4_1 ila armor_l4_5 - Seviye 4 Zırhlar (5 varyant)
armor_l5_1 ila armor_l5_5 - Seviye 5 Zırhlar (5 varyant)
Toplam: 25 zırh
```

**Özel Silahlar** (weapon kategorisi):

**Format 1 (Önerilen - İsimlerle)**: `/stratocraft give weapon <seviye> <isim>`
```
/stratocraft give weapon 1 hız_hançeri        → Hız Hançeri
/stratocraft give weapon 1 çiftçi_tırpanı     → Çiftçi Tırpanı
/stratocraft give weapon 2 alev_kılıcı        → Alev Kılıcı
/stratocraft give weapon 3 gölge_katanası     → Gölge Katanası
/stratocraft give weapon 4 element_kılıcı     → Element Kılıcı
/stratocraft give weapon 5 zamanı_büken       → Zamanı Büken
```

**Format 2 (Direkt ID)**: `/stratocraft give weapon_l<seviye>_<varyant>`
```
/stratocraft give weapon_l1_1  → Hız Hançeri
/stratocraft give weapon_l5_5  → Zamanı Büken
```

**Format 3 (Eski Format - Tip ile)**: `/stratocraft give weapon <seviye> <tip>`
```
/stratocraft give weapon 1 sword   → Hız Hançeri
/stratocraft give weapon 1 axe     → Çiftçi Tırpanı
/stratocraft give weapon 5 hammer  → Zamanı Büken
```

**Tüm Silah İsimleri**:

**Seviye 1**:
- `hız_hançeri` - Hız Hançeri (Elinde tutarken hız verir)
- `çiftçi_tırpanı` - Çiftçi Tırpanı (Alan hasarı vurur)
- `yerçekimi_gürzü` - Yerçekimi Gürzü (Sağ tıkla havaya fırla!)
- `patlayıcı_yay` - Patlayıcı Yay (Okları patlar)
- `vampir_dişi` - Vampir Dişi (Can çalar)

**Seviye 2**:
- `alev_kılıcı` - Alev Kılıcı (Alev dalgası atar)
- `buz_asası` - Buz Asası (Düşmanı dondurur)
- `zehirli_mızrak` - Zehirli Mızrak (Zehir bulutu oluşturur)
- `golem_kalkanı` - Golem Kalkanı (Eğilince dostları iyileştirir)
- `şok_baltası` - Şok Baltası (Kritik vuruşta çarpar)

**Seviye 3**:
- `gölge_katanası` - Gölge Katanası
- `deprem_çekici` - Deprem Çekici
- `taramalı_yay` - Taramalı Yay
- `büyücü_küresi` - Büyücü Küresi
- `hayalet_hançeri` - Hayalet Hançeri

**Seviye 4** (Modlu):
- `element_kılıcı` - Element Kılıcı (Mod 1: Ateş, Mod 2: Buz)
- `yaşam_ve_ölüm` - Yaşam ve Ölüm (Mod 1: Ölüm, Mod 2: Yaşam)
- `mjölnir_v2` - Mjölnir V2 (Mod 1: Melee, Mod 2: Throw)
- `avcı_yayı` - Avcı Yayı (Mod 1: Sniper, Mod 2: Shotgun)
- `manyetik_eldiven` - Manyetik Eldiven (Mod 1: Çek, Mod 2: İt)

**Seviye 5** (Modlu):
- `hiperiyon_kılıcı` - Hiperiyon Kılıcı (Mod 1: Işınlanma, Mod 2: Kara Delik Kalkanı)
- `meteor_çağıran` - Meteor Çağıran (Mod 1: Kıyamet, Mod 2: Yer Yaran)
- `titan_katili` - Titan Katili (Mod 1: %5 Hasar, Mod 2: Mızrak Yağmuru)
- `ruh_biçen` - Ruh Biçen (Mod 1: Çağır, Mod 2: Ruh Patlaması)
- `zamanı_büken` - Zamanı Büken (Mod 1: Zamanı Durdur, Mod 2: Geri Sar)

**Toplam**: 25 silah

**Tab Completion**: `/stratocraft give weapon 1 [TAB]` → Tüm seviye 1 silah isimlerini gösterir

**Limit**: Maksimum 2304 adet (36 slot × 64)

---

## 👹 MOB KOMUTLARI

### `/scadmin spawn <mob>`

**Açıklama**: Özel mob spawn et

**KOD DOĞRULANDI**:
```java
// AdminCommandExecutor.java satır 118-248
// 30+ mob desteği
```

**Kullanım**:
```
/scadmin spawn hell_dragon
/scadmin spawn war_bear
/scadmin spawn titan_golem
```

**Desteklenen Moblar**:
```
Eğitilebilir:
hell_dragon, cehennem_ejderi, ejder
terror_worm, toprak_solucani, solucan
war_bear, savas_ayisi, ayi
shadow_panther, golge_panteri, panter
wyvern

Sık Canavarlar:
goblin, ork, troll
skeleton_knight, iskelet_sovalye
dark_mage, karanlik_buyucu
werewolf, kurt_adam
giant_spider, dev_orumcek
minotaur, harpy
basilisk

Nadir Canavarlar:
dragon, ejderha
trex, dinozor
cyclops, tek_gozlu_dev
griffin
wraith, hayalet
lich
kraken
phoenix
hydra
behemoth

Felaketler (9 tip):
Felaket Bossları: CATASTROPHIC_TITAN, CATASTROPHIC_ABYSSAL_WORM, CATASTROPHIC_CHAOS_DRAGON, CATASTROPHIC_VOID_TITAN, CATASTROPHIC_ICE_LEVIATHAN
Doğa Olayları: SOLAR_FLARE, EARTHQUAKE, METEOR_SHOWER, VOLCANIC_ERUPTION
```

---

## ⚙️ SİSTEM KOMUTLARI

### `/scadmin disaster <komut>`

**Açıklama**: Felaket yönetimi

**KOD DOĞRULANDI**:
```java
// AdminCommandExecutor.java satır 609-793
// DisasterManager kullanır
```

**Alt Komutlar**:
```
start [Kategori seviyesi] <Felaket ismi> <İç seviye> [konum] - Felaket başlat
stop - Felaketi durdur
info - Aktif felaket bilgisi
list - Tüm felaket tiplerini listele
clear - Felaketi yok et
```

**Kullanım**:
```
/scadmin disaster start 3 CATASTROPHIC_TITAN 3 ben
/scadmin disaster start 1 SOLAR_FLARE 2 ben
/scadmin disaster start 2 CATASTROPHIC_ABYSSAL_WORM 1 1000 64 1000
/scadmin disaster stop
/scadmin disaster info
/scadmin disaster list
```

**Parametreler**:
- `[Kategori seviyesi]`: 1-3 (opsiyonel, belirtilmezse otomatik belirlenir)
  - Kategori 1: Her gün gelen felaketler
  - Kategori 2: 3 günde bir gelen felaketler
  - Kategori 3: 7 günde bir gelen felaketler
- `<Felaket ismi>`: Felaket tipi (zorunlu)
- `<İç seviye>`: 1-3 (zorunlu) - Felaketin gücünü belirler
  - İç Seviye 1: Zayıf form (düşük can/hasar)
  - İç Seviye 2: Orta form (orta can/hasar)
  - İç Seviye 3: Güçlü form (yüksek can/hasar)
- `[konum]`: `ben` (oyuncunun yanında) veya `X Y Z` (koordinat) - opsiyonel

**Felaket Tipleri** (9 adet):

**Felaket Bossları** (Normal bosslardan ayrı, çok daha güçlü):
```
CATASTROPHIC_TITAN - Felaket Titanı (Kategori: 3, 7 günde bir, 30 blok boyunda)
CATASTROPHIC_ABYSSAL_WORM - Felaket Hiçlik Solucanı (Kategori: 2, 3 günde bir)
CATASTROPHIC_CHAOS_DRAGON - Felaket Khaos Ejderi (Kategori: 3, 7 günde bir)
CATASTROPHIC_VOID_TITAN - Felaket Boşluk Titanı (Kategori: 3, 7 günde bir)
CATASTROPHIC_ICE_LEVIATHAN - Felaket Buzul Leviathan (Kategori: 2, 3 günde bir)
```

**Doğa Olayları**:
```
SOLAR_FLARE - Güneş Fırtınası (Kategori: 1, her gün)
EARTHQUAKE - Deprem (Kategori: 2, 3 günde bir)
METEOR_SHOWER - Meteor Yağmuru (Kategori: 2, 3 günde bir)
VOLCANIC_ERUPTION - Volkanik Patlama (Kategori: 3, 7 günde bir)
```

**Notlar**:
- Kategori seviyesi belirtilmezse, felaket tipine göre otomatik belirlenir
- İç seviye felaketin gücünü, canını ve hasarını belirler
- `[konum]`: `ben` (oyuncunun yanında) veya `X Y Z` (koordinat)
- Felaket bossları için **BossBar** gösterilir (can ve süre)
- Doğa olayları için **ActionBar** gösterilir (sadece süre)
- **Önemli:** Felaket bossları normal bosslardan tamamen ayrıdır. Normal bosslar eğitilebilir, felaket bossları sadece klan kristallerini yok etmek için var.

---

### `/scadmin siege <komut> [parametreler]`

**Açıklama**: Savaş yönetimi

**Komutlar**:
```
/scadmin siege start <saldıran_klan> <savunan_klan>
→ Savaş başlat (admin)

/scadmin siege surrender <klan>
→ Klanı pes ettir (admin)

/scadmin siege clear
→ Tüm savaş yapılarını temizle

/scadmin siege list
→ Aktif savaş yapılarını listele
```

**Örnekler**:
```
/scadmin siege start KlanA KlanB
→ KlanA, KlanB'ye savaş açar

/scadmin siege surrender KlanB
→ KlanB pes eder
```

---

### `/scadmin caravan <oyuncu>`

**Açıklama**: Kervan bilgilerini göster

**Kullanım**:
```
/scadmin caravan MuhamMD
```

**Çıktı**: Aktif kervan sayısı ve bilgileri

---

### `/scadmin contract <list|clear>`

**Açıklama**: Kontrat yönetimi

**Kullanım**:
```
/scadmin contract list - Aktif kontratları listele (GUI menüsü açılır)
/scadmin contract clear - Tüm kontratları temizle
```

**Not**: `/kontrat list` komutu da GUI menüsünü açar (`ContractMenu.java`)

---

### `/scadmin alliance <komut> [parametreler]`

**Açıklama**: İttifak yönetimi (YENİ - Performans ve Veri Kaybı Düzeltmeleri)

**KOD DOĞRULANDI**:
```java
// AdminCommandExecutor.java - handleAlliance() metodu
// AllianceManager kullanır, DataManager ile kayıt/yükleme yapılır
```

**Komutlar**:
```
/scadmin alliance list
→ Tüm aktif ittifakları listele

/scadmin alliance create <klan1> <klan2> <tip> [süre_gün]
→ İttifak oluştur (admin)
→ Tip: defensive, offensive, trade, full
→ Süre: 0 = süresiz, >0 = gün sayısı

/scadmin alliance break <ittifak_id>
→ İttifakı boz (admin)

/scadmin alliance info <klan>
→ Klanın ittifaklarını göster
```

**İttifak Tipleri**:
```
defensive - Savunma İttifakı: Birine saldırılırsa diğeri yardım eder
offensive - Saldırı İttifakı: Birlikte saldırı yapılır
trade - Ticaret İttifakı: Ticaret bonusları
full - Tam İttifak: Her şey (en güçlü)
```

**Örnekler**:
```
/scadmin alliance list
→ Tüm aktif ittifakları gösterir

/scadmin alliance create KlanA KlanB defensive 7
→ KlanA ve KlanB arasında 7 günlük savunma ittifakı

/scadmin alliance create KlanA KlanB full 0
→ KlanA ve KlanB arasında süresiz tam ittifak

/scadmin alliance info KlanA
→ KlanA'nın tüm ittifaklarını gösterir

/scadmin alliance break <ittifak_id>
→ Belirtilen ittifakı bozar
```

**Not**: İttifaklar artık otomatik olarak kaydediliyor ve sunucu restart'tan sonra yükleniyor (veri kaybı önlendi).

---

### `/scadmin build <yapı_tipi> [seviye]`

**Açıklama**: Yapı oluştur

**Kullanım**:
```
/scadmin build alchemy_tower 3
/scadmin build poison_reactor 5
/scadmin build tectonic_stabilizer 4
```

**Yapı Tipleri**:
```
Savunma:
alchemy_tower - Simya Kulesi
poison_reactor - Zehir Reaktörü
tectonic_stabilizer - Tektonik Sabitleyici
siege_factory - Kuşatma Fabrikası
wall_generator - Sur Jeneratörü
gravity_well - Yerçekimi Kuyusu
lava_trencher - Lav Hendekçisi
watchtower - Gözetleme Kulesi
drone_station - Drone İstasyonu
auto_turret - Otomatik Taret

Ekonomi:
global_market_gate - Global Pazar Kapısı
auto_drill - Otomatik Madenci
xp_bank - Tecrübe Bankası
mag_rail - Manyetik Ray
teleporter - Işınlanma Platformu
food_silo - Buzdolabı
oil_refinery - Petrol Rafinerisi

Destek:
healing_beacon - Şifa Kulesi
weather_machine - Hava Kontrolcüsü
crop_accelerator - Tarım Hızlandırıcı
mob_grinder - Mob Öğütücü
invisibility_cloak - Görünmezlik Perdesi
armory - Cephanelik
library - Kütüphane
warning_sign - Yasaklı Bölge Tabelası
```

**Seviye**: 1-5 (varsayılan: 1)

---

### `/scadmin trap <komut> [parametreler]`

**Açıklama**: Tuzak yönetimi

**Komutlar**:
```
/scadmin trap build              → Tuzak yapısını otomatik oluştur
/scadmin trap give <oyuncu>      → Tuzak Çekirdeği ver
/scadmin trap list               → Aktif tuzakları listele
/scadmin trap remove <x> <y> <z> → Tuzak kaldır
```

**Tuzak Tipleri**:
```
fire - Ateş Tuzağı
shock - Şok Tuzağı
poison - Zehir Tuzağı
freeze - Donma Tuzağı
explosive - Patlayıcı Tuzak
```

---

### `/scadmin tame <komut> [parametreler]`

**Açıklama**: Eğitim sistemi yönetimi

**Komutlar**:
```
/scadmin tame build <seviye>     → Eğitim ritüeli yapısını otomatik oluştur
/scadmin tame pattern <seviye>   → Ritüel desenini göster
/scadmin tame facility <komut>   → Eğitim tesisi yönetimi
```

**Seviyeler**: 1-5

**Örnekler**:
```
/scadmin tame build 1  → Seviye 1 eğitim ritüeli yapısı
/scadmin tame build 5  → Seviye 5 eğitim ritüeli yapısı
```

---

### `/scadmin boss <komut> [parametreler]`

**Açıklama**: Boss sistemi yönetimi

**Komutlar**:
```
/scadmin boss build <boss_tipi>  → Boss ritüeli yapısını otomatik oluştur
/scadmin boss spawn <boss_tipi>  → Boss spawn et
/scadmin boss list               → Boss tiplerini listele
```

**Boss Tipleri** (13 adet):
```
goblin_king, orc_chief, troll_king
dragon, trex, cyclops
titan_golem, hell_dragon, hydra, phoenix
void_dragon, chaos_titan, chaos_god
```

**Örnekler**:
```
/scadmin boss build goblin_king  → Goblin Kralı ritüeli yapısı
/scadmin boss spawn dragon       → Ejderha spawn et
/scadmin boss list               → Tüm boss tiplerini göster
```

**BossBar Özelliği**:
- Tüm bosslar spawn edildiğinde **BossBar** gösterilir
- Ekranın üst kısmında görünür
- Boss ismi ve faz bilgisi (çok fazlı bosslar için)
- Can gösterimi: `Can/Maksimum Can` (örn: `200/200`)
- Progress bar: Can yüzdesine göre
- Renk değişimi: Kırmızı (>%60), Sarı (%30-60), Yeşil (<%30)

---

### `/scadmin breeding <komut> [parametreler]`

**Açıklama**: Üreme sistemi yönetimi

**Komutlar**:
```
/scadmin breeding build <seviye>     → Üreme tesisi yapısını otomatik oluştur
/scadmin breeding complete <location> → Çiftleştirmeyi anında tamamla
/scadmin breeding create <seviye>    → Üreme tesisi oluştur
```

**Seviyeler**: 1-5

**Örnekler**:
```
/scadmin breeding build 1  → Seviye 1 üreme tesisi yapısı
/scadmin breeding build 5  → Seviye 5 üreme tesisi yapısı
```

---

### `/stratocraft build battery <batarya_ismi>`

**Açıklama**: Yeni batarya sistemi (75 batarya) - Otomatik yapı oluşturma

**Kategoriler**:
- **Saldırı Bataryaları** (`attack_*`) - 25 batarya
- **Oluşturma Bataryaları** (`construction_*`) - 25 batarya
- **Destek Bataryaları** (`support_*`) - 25 batarya

**Seviye 1 Bataryalar** (5'er batarya):
```
Saldırı:
attack_fireball_l1, attack_lightning_l1, attack_ice_ball_l1, attack_poison_arrow_l1, attack_shock_l1

Oluşturma:
construction_obsidian_wall_l1, construction_stone_bridge_l1, construction_iron_cage_l1, construction_glass_wall_l1, construction_wood_barricade_l1

Destek:
support_heal_l1, support_speed_l1, support_damage_l1, support_armor_l1, support_regeneration_l1
```

**Seviye 2 Bataryalar** (5'er batarya):
```
Saldırı:
attack_double_fireball_l2, attack_chain_lightning_l2, attack_ice_storm_l2, attack_acid_rain_l2, attack_electric_net_l2

Oluşturma:
construction_obsidian_cage_l2, construction_stone_bridge_l2, construction_iron_wall_l2, construction_glass_tunnel_l2, construction_wood_castle_l2

Destek:
support_heal_l2, support_speed_l2, support_damage_l2, support_armor_l2, support_regeneration_l2
```

**Seviye 3 Bataryalar** (5'er batarya):
```
Saldırı:
attack_meteor_shower_l3, attack_storm_l3, attack_ice_age_l3, attack_poison_bomb_l3, attack_lightning_storm_l3

Oluşturma:
construction_obsidian_wall_l3, construction_netherite_bridge_l3, construction_iron_prison_l3, construction_glass_tower_l3, construction_stone_castle_l3

Destek:
support_heal_l3, support_speed_l3, support_damage_l3, support_armor_l3, support_regeneration_l3
```

**Seviye 4 Bataryalar** (5'er batarya):
```
Saldırı:
attack_hellfire_l4, attack_thunder_l4, attack_ice_age_l4, attack_death_cloud_l4, attack_electric_storm_l4

Oluşturma:
construction_obsidian_castle_l4, construction_netherite_bridge_l4, construction_iron_prison_l4, construction_glass_tower_l4, construction_stone_fortress_l4

Destek:
support_heal_l4, support_speed_l4, support_damage_l4, support_armor_l4, support_regeneration_l4
```

**Seviye 5 Bataryalar** (5'er batarya):
```
Saldırı:
attack_mountain_destroyer_l5, attack_lava_tsunami_l5, attack_boss_killer_l5, attack_area_destroyer_l5, attack_apocalypse_l5

Oluşturma:
construction_obsidian_prison_l5, construction_netherite_bridge_l5, construction_iron_castle_l5, construction_glass_tower_l5, construction_stone_fortress_l5

Destek:
support_heal_l5, support_speed_l5, support_damage_l5, support_armor_l5, support_regeneration_l5
```

**Yeni Format (Zorunlu)**: `/stratocraft build battery <seviye> <isim>`
```
/stratocraft build battery 1 ateş_topu
/stratocraft build battery 1 obsidyen_duvar
/stratocraft build battery 1 can_yenileme
/stratocraft build battery 5 dağ_yok_edici
/stratocraft build battery 5 netherite_köprü
/stratocraft build battery 5 efsanevi_can_yenileme
```

**Eski Format (Kaldırıldı)**: `/stratocraft build battery <isim>` ❌
- Artık çalışmıyor! Sadece yeni format kullanılmalı.

**Tab Completion**: 
- `/stratocraft build battery [TAB]` → Seviye önerir (1-5)
- `/stratocraft build battery 1 [TAB]` → Seviye 1 batarya isimlerini gösterir
- `/stratocraft build battery 5 [TAB]` → Seviye 5 batarya isimlerini gösterir

**Not**: Komut, baktığın yere batarya yapısını otomatik olarak oluşturur ve gerekli aktivasyon item'ını verir.

---

## ⚠️ ÖNEMLİ NOTLAR

### Yetki Kontrolü

**Permission**: `stratocraft.admin`

**KOD DOĞRULANDI**:
```java
// AdminCommandExecutor.java satır 34-37
if (!sender.hasPermission("stratocraft.admin")) {
    sender.sendMessage(langManager.getMessage("admin.no-permission"));
    return true;
}
```

**Mesaj**: Yetkisiz kullanımda hata mesajı

---

### Envanter Doluysa

**Otomatik Yere Düşürme**:
```java
// AdminCommandExecutor.java satır 102-109
// Envanter doluysa yere düşür
if (!overflow.isEmpty()) {
    for (ItemStack drop : overflow.values()) {
        p.getWorld().dropItemNaturally(p.getLocation(), drop);
    }
}
```

**Mesaj**: "Envanter doluydu, fazlalar yere düştü"

---

### Tab Complete

**Otomatik Tamamlama**:
```
/scadmin [TAB] → give, spawn, disaster, siege...
/scadmin give [TAB] → titanium_ingot, red_diamond...
/scadmin spawn [TAB] → hell_dragon, war_bear...
```

**KOD**: `TabCompleter` interface implement edilmiş

---

## 🎯 HIZLI ADMIN REHBERİ

### Test Ortamı Kurma

```
1. Eşya ver:
/scadmin give dark_matter 64
/scadmin give titanium_ingot 64
/scadmin give red_diamond 32

2. Mob spawn:
/scadmin spawn war_bear
/scadmin spawn hell_dragon

3. Felaket test:
/scadmin disaster start CATASTROPHIC_TITAN 3 ben

4. Yapı kur:
/scadmin build alchemy_tower 5
/scadmin build tectonic_stabilizer 5
```

### Hızlı Silahlanma

```
/scadmin give dark_matter 10
/scadmin give adamantite 20
/scadmin give red_diamond 50
/scadmin give flame_amplifier 5
/scadmin give tower_shield 1
```

---

---

## 🎮 YENİ ÖZELLİKLER

### Silah Modu Değiştirme

**Komut**: `/weaponmode <1|2|3>`

**Açıklama**: Seviye 5 özel silahların modunu değiştir

**Modlar**:
```
1 - Blok Fırlatma modu (Q tuşu ile blok fırlat)
2 - Duvar Yapma modu (F tuşu ile duvar yap)
3 - Atılma/Patlama modu (Shift+Sağ Tık ile atılma)
```

**Kullanım**:
```
/weaponmode 1  → Blok Fırlatma moduna geç
/weaponmode 2  → Duvar Yapma moduna geç
/weaponmode 3  → Atılma/Patlama moduna geç
```

**Alternatif**: Shift+Sağ Tık ile mod seçim menüsü açılır

---

## 💣 MAYIN KOMUTLARI

### `/stratocraft mine list`

**Açıklama**: Tüm mayınları listeler

**Kullanım**:
```
/stratocraft mine list
```

**Çıktı**: 25 benzersiz mayın listesi

---

### `/stratocraft mine give <seviye> <isim>`

**Açıklama**: Mayın basınç plakası verir

**Kullanım**:
```
/stratocraft mine give 1 explosive         → Seviye 1 Patlama Mayını
/stratocraft mine give 3 freeze            → Seviye 3 Dondurma Mayını
/stratocraft mine give 5 nuclear_explosive  → Seviye 5 Nükleer Patlama Mayını
```

**Seviye 1 Mayınlar**:
- `explosive` - Patlama Mayını
- `poison` - Zehir Mayını
- `slowness` - Yavaşlık Mayını
- `lightning` - Yıldırım Mayını
- `fire` - Yakma Mayını

**Seviye 2 Mayınlar**:
- `cage` - Kafes Hapsetme Mayını
- `launch` - Fırlatma Mayını
- `mob_spawn` - Canavar Spawn Mayını
- `blindness` - Körlük Mayını
- `weakness` - Zayıflık Mayını

**Seviye 3 Mayınlar**:
- `freeze` - Dondurma Mayını
- `confusion` - Karışıklık Mayını
- `fatigue` - Yorgunluk Mayını
- `poison_cloud` - Zehir Bulutu Mayını
- `lightning_storm` - Yıldırım Fırtınası Mayını

**Seviye 4 Mayınlar**:
- `mega_explosive` - Büyük Patlama Mayını
- `large_cage` - Büyük Kafes Mayını
- `super_launch` - Güçlü Fırlatma Mayını
- `elite_mob_spawn` - Güçlü Canavar Spawn Mayını
- `multi_effect` - Çoklu Efekt Mayını

**Seviye 5 Mayınlar**:
- `nuclear_explosive` - Nükleer Patlama Mayını
- `death_cloud` - Ölüm Bulutu Mayını
- `thunderstorm` - Gök Gürültüsü Mayını
- `boss_spawn` - Boss Spawn Mayını
- `chaos` - Kaos Mayını

**Tab Completion**: `/stratocraft mine give 1 [TAB]` → Tüm seviye 1 mayın isimlerini gösterir

---

### `/stratocraft mine give concealer`

**Açıklama**: Mayın Gizleme Aleti verir

**Kullanım**:
```
/stratocraft mine give concealer
```

**Açıklama**: Shift + Sağ Tık ile mayınları görünmez yapabilirsin

---

## 🏰 KLAN KOMUTLARI ⭐ YENİ

### `/stratocraft clan <komut>`

**Açıklama**: Klan yönetimi için admin komutları

**YETKİ**: `stratocraft.admin` permission gerekli

---

### Temel Klan Komutları

#### `/stratocraft clan list`

**Açıklama**: Tüm klanları listeler

**Kullanım**:
```
/stratocraft clan list
```

**Çıktı**: Tüm klanların listesi

---

#### `/stratocraft clan info <klan>`

**Açıklama**: Klan bilgilerini gösterir

**Kullanım**:
```
/stratocraft clan info TestKlan
```

**Çıktı**: Klan üyeleri, rütbeler, bölge bilgisi, banka bakiyesi

---

#### `/stratocraft clan create`

**Açıklama**: Admin komutu ile klan oluşturur (otomatik çit ve kristal)

**Kullanım**:
```
/stratocraft clan create
```

**Özellikler**:
- Otomatik çit oluşturur
- Otomatik kristal yerleştirir
- Klan ismi sorar

---

#### `/stratocraft clan disband <klan>`

**Açıklama**: Klanı dağıtır (tüm veriler silinir)

**Kullanım**:
```
/stratocraft clan disband TestKlan
```

**Uyarı**: Geri alınamaz!

---

### Üye Yönetimi

#### `/stratocraft clan addmember <klan> <oyuncu>`

**Açıklama**: Klan üyesi ekler

**Kullanım**:
```
/stratocraft clan addmember TestKlan PlayerName
```

**Varsayılan Rütbe**: RECRUIT

---

#### `/stratocraft clan removemember <klan> <oyuncu>`

**Açıklama**: Klan üyesi çıkarır

**Kullanım**:
```
/stratocraft clan removemember TestKlan PlayerName
```

---

### Rütbe Yönetimi

#### `/stratocraft clan setrank <klan> <oyuncu> <LEADER|GENERAL|ELITE|MEMBER|RECRUIT>`

**Açıklama**: Oyuncunun rütbesini değiştirir

**Kullanım**:
```
/stratocraft clan setrank TestKlan PlayerName MEMBER
/stratocraft clan setrank TestKlan PlayerName GENERAL
```

**Rütbeler**:
- `LEADER` - Lider (tüm yetkiler)
- `GENERAL` - Komutan (üye yönetimi, savaş ilanı)
- `ELITE` - Seçkin (yapı kurma, ritüel kullanma)
- `MEMBER` - Üye (yapı kullanma, blok kırma/koyma YOK) ⚠️ YENİ
- `RECRUIT` - Acemi (sadece gezinebilir)

---

#### `/stratocraft clan promote <klan> <oyuncu> <RECRUIT|MEMBER|ELITE|GENERAL>` ⭐ YENİ

**Açıklama**: Rütbe yükseltme test komutu (ritüel simülasyonu)

**Kullanım**:
```
/stratocraft clan promote TestKlan PlayerName MEMBER
/stratocraft clan promote TestKlan PlayerName GENERAL
/stratocraft clan terfi TestKlan PlayerName ELITE
```

**Özellikler**:
- ✅ Ritüel yapısı gerekmez
- ✅ Sadece yukarı doğru terfi (rütbe seviyesi kontrolü)
- ✅ Partikül efektleri (GENERAL için TOTEM, diğerleri için VILLAGER_HAPPY)
- ✅ Ses efektleri ve title mesajları
- ✅ Test için kullanılabilir

**Not**: Bu komut ritüel simülasyonu yapar. Normal oyunda terfi ritüeli ile yapılır.

---

### Bölge Yönetimi

#### `/stratocraft clan territory <klan> <expand|reset|info> [miktar]`

**Açıklama**: Klan bölgesi yönetimi

**Kullanım**:
```
/stratocraft clan territory TestKlan expand 25
/stratocraft clan territory TestKlan reset
/stratocraft clan territory TestKlan info
```

**Komutlar**:
- `expand <miktar>` - Bölgeyi genişletir (radius artırır)
- `reset` - Bölgeyi sıfırlar
- `info` - Bölge bilgilerini gösterir

---

### Banka Yönetimi

#### `/stratocraft clan bank <klan> <clear|info>`

**Açıklama**: Klan bankası yönetimi

**Kullanım**:
```
/stratocraft clan bank TestKlan info
/stratocraft clan bank TestKlan clear
```

**Komutlar**:
- `info` - Banka bilgilerini gösterir
- `clear` - Banka bakiyesini sıfırlar

---

### Görev Yönetimi

#### `/stratocraft clan mission <klan> <list|clear|complete> [id]`

**Açıklama**: Klan görevleri yönetimi

**Kullanım**:
```
/stratocraft clan mission TestKlan list
/stratocraft clan mission TestKlan clear
/stratocraft clan mission TestKlan complete 1
```

---

### Kontrat Yönetimi

#### `/stratocraft clan contract <klan> <list|cancel> [id]`

**Açıklama**: Transfer kontratları yönetimi

**Kullanım**:
```
/stratocraft clan contract TestKlan list
/stratocraft clan contract TestKlan cancel 1
```

---

### Aktivite Yönetimi

#### `/stratocraft clan activity <klan> <reset|info> [oyuncu]`

**Açıklama**: Klan aktivite yönetimi

**Kullanım**:
```
/stratocraft clan activity TestKlan info
/stratocraft clan activity TestKlan reset PlayerName
```

---

### Maaş Yönetimi

#### `/stratocraft clan salary <klan> <cancel|reset|info> [oyuncu]`

**Açıklama**: Klan maaş yönetimi

**Kullanım**:
```
/stratocraft clan salary TestKlan info
/stratocraft clan salary TestKlan cancel PlayerName
/stratocraft clan salary TestKlan reset
```

---

### Tab Completion

**Otomatik Tamamlama**:
```
/stratocraft clan [TAB] → list, info, create, disband, addmember, removemember, setrank, promote, terfi, testpromote, salary, territory, bank, mission, contract, activity, caravan
/stratocraft clan setrank TestKlan PlayerName [TAB] → LEADER, GENERAL, ELITE, MEMBER, RECRUIT
/stratocraft clan promote TestKlan PlayerName [TAB] → RECRUIT, MEMBER, ELITE, GENERAL
```

---

## 🔋 BATARYA SİSTEMİ - SON GÜNCELLEMELERİ

### ✅ Düzeltilen Sorunlar

#### 1. Batarya Yön Sorunu Düzeltildi
- **Sorun**: Bataryalar sadece North/South yönünde çalışıyordu
- **Çözüm**: 4 rotasyon (0°, 90°, 180°, 270°) sistemi eklendi
- **Sonuç**: Artık bataryalar **HER YÖNDE** çalışıyor!

#### 2. Partikül Sorunu Düzeltildi
- **Sorun**: Partiküller çok büyük ve önü kapatıyordu
- **Çözüm**: Partiküller artık sadece diğer oyunculara görünüyor
- **Sonuç**: Kendine görünmüyor, önünü kapatmıyor!

#### 3. Blok Yok Etme Mekaniği Düzeltildi
- **Sorun**: Alan Yok Edici ve Dağ Yok Edici blok yok edemiyordu
- **Çözüm**: `canModifyTerritory()` metodu esnestildi
- **Performans**: 2x hızlı (10 sütun/tick)
- **Sonuç**: Artık boş arazide, kendi klan alanında ve savaşta blok yok ediliyor!

#### 4. Antrenman Sistemi İyileştirildi
- **Sorun**: Antrenman sistemi basit ve görsel geri bildirim yoktu
- **Çözüm**: Seviye bazlı başlangıç gücü + dinamik güç artışı + görsel geri bildirim
- **Sonuç**: 
  - L1: %20 başlangıç → 5 kullanımda %100
  - L5: %80 başlangıç → 1 kullanımda %100
  - 30 kullanımda %150 (maksimum)

#### 5. Batarya Hasarları Artırıldı
- **Sorun**: L3, L4, L5 bataryaları çok az hasar veriyordu
- **Çözüm**: Hasarlar artırıldı
- **Sonuç**:
  - Seviye 3: 50-70 hasar
  - Seviye 4: 70-120 hasar
  - Seviye 5: 200-300 hasar

#### 6. Batarya Çakışma Sorunu Düzeltildi
- **Sorun**: Farklı tarifli bataryalar çakışıyordu
- **Çözüm**: Merkez blok kontrolü eklendi
- **Sonuç**: Sadece merkez bloğu aynı olan tarifler kontrol ediliyor

### ✅ Yeni Özellikler

#### 1. Komut Formatı Değişti
- **Eski**: `/stratocraft build battery <isim>` veya `/stratocraft build battery <seviye> <isim>`
- **Yeni**: `/stratocraft build battery <seviye> <isim>` (sadece bu format)
- **Tab Completion**: Seviye seçtikten sonra ilgili seviye bataryaları gösteriliyor

#### 2. Tam Tab Completion Desteği
- **Seviye 1**: `/stratocraft build battery [TAB]` → 1, 2, 3, 4, 5
- **Seviye 2**: `/stratocraft build battery 1 [TAB]` → Seviye 1 bataryaları
- **Örnek**: `/stratocraft build battery 5 [TAB]` → Kıyamet Reaktörü, Boss Katili, Alan Yok Edici, vb.

#### 3. Tüm Eski Sistem Kaldırıldı
- `magma_battery` ❌
- `lightning_battery` ❌
- `black_hole` ❌
- `bridge` ❌
- Tüm eski batarya isimleri kaldırıldı ✅

---

## 💣 MAYIN SİSTEMİ - YENİ SİSTEM

### ✅ Yeni Mayın Sistemi Özellikleri

#### 1. 25 Benzersiz Mayın
- Her mayının kendine özgü ismi ve efekti var
- MINE_EXPLOSIVE_L3 gibi generic isimler yok ❌
- FREEZE, DEATH_CLOUD, CHAOS gibi özel isimler var ✅

#### 2. Mayın Basınç Plakası Türleri
- **Seviye 1**: Stone Pressure Plate (Taş)
- **Seviye 2**: Oak Pressure Plate (Meşe)
- **Seviye 3**: Birch Pressure Plate (Huş)
- **Seviye 4**: Dark Oak Pressure Plate (Koyu Meşe)
- **Seviye 5**: Warped Pressure Plate (Warped)

#### 3. Mayın Görünürlüğü
- **Sahibi**: Mayın ismini her zaman görebilir
- **Klan Üyeleri**: Mayın ismini görebilir
- **Düşmanlar**: Mayın ismini göremez
- **Gizleme Aleti**: Mayını tamamen görünmez yapabilir

#### 4. Tab Completion Desteği
- `/stratocraft mine give [TAB]` → 1, 2, 3, 4, 5, concealer
- `/stratocraft mine give 1 [TAB]` → explosive, poison, slowness, fire, lightning
- `/stratocraft mine give 5 [TAB]` → nuclear_explosive, death_cloud, thunderstorm, boss_spawn, chaos

### ✅ En Güçlü Mayınlar (Seviye 5)

#### DEATH_CLOUD (Ölüm Bulutu)
- **Efekt**: Poison IV (20 saniye) + sürekli 0.5 hasar (50 toplam) + büyük duman
- **Kullanım**: Ölümcül alan
- **Komut**: `/stratocraft mine give 5 death_cloud`

#### CHAOS (Kaos)
- **Efekt**: Patlama + Poison + Slowness + Blindness + Weakness + Ateş + Yıldırım
- **Kullanım**: Tüm efektlerin birleşimi
- **Komut**: `/stratocraft mine give 5 chaos`

---

---

## 💪 GÜÇ SİSTEMİ KOMUTLARI (YENİ)

### `/sgp` - Güç Sistemi Komutları

**Açıklama**: Oyuncu ve klan güç bilgilerini gösterir

**Yetki**: Herkes kullanabilir

**Alt Komutlar:**

#### `/sgp` veya `/sgp me`
**Açıklama**: Kendi gücünü gösterir

**Kullanım**:
```
/sgp
/sgp me
```

**Çıktı**:
```
╔════════════════════════════════╗
║  OyuncuAdı Güç Bilgileri
╠════════════════════════════════╣
Toplam SGP: 1234.56
Combat Power: 800.00
Progression Power: 434.56
Seviye: 5
╚════════════════════════════════╝
```

#### `/sgp player <oyuncu>`
**Açıklama**: Belirtilen oyuncunun gücünü gösterir

**Kullanım**:
```
/sgp player OyuncuAdı
/sgp p OyuncuAdı
```

#### `/sgp clan`
**Açıklama**: Kendi klanının gücünü gösterir

**Kullanım**:
```
/sgp clan
/sgp c
```

**Çıktı**:
```
╔════════════════════════════════╗
║  KlanAdı Klan Güç Bilgileri
╠════════════════════════════════╣
Toplam Klan Gücü: 50000.00
Klan Seviyesi: 8
╚════════════════════════════════╝
```

#### `/sgp top [limit]`
**Açıklama**: En güçlü oyuncuları listeler

**Kullanım**:
```
/sgp top
/sgp top 20
```

**Çıktı**:
```
╔════════════════════════════════╗
║  Top 10 Oyuncu
╠════════════════════════════════╣
🥇 1. Oyuncu1 - 5000.00 SGP (Seviye 10)
🥈 2. Oyuncu2 - 4500.00 SGP (Seviye 9)
🥉 3. Oyuncu3 - 4000.00 SGP (Seviye 8)
...
╚════════════════════════════════╝
```

#### `/sgp components`
**Açıklama**: Güç bileşenlerini detaylı gösterir

**Kullanım**:
```
/sgp components
/sgp comp
```

**Çıktı**:
```
╔════════════════════════════════╗
║  OyuncuAdı Güç Bileşenleri
╠════════════════════════════════╣
Eşya Gücü: 840.00
Ustalık Gücü: 250.00
Buff Gücü: 50.00
Ritüel Gücü: 0.00
╠════════════════════════════════╣
Combat Power: 890.00
Progression Power: 250.00
╚════════════════════════════════╝
```

#### `/sgp help`
**Açıklama**: Komut yardımını gösterir

**Kullanım**:
```
/sgp help
/sgp ?
```

### Komut Kısaltmaları

```
/sgp = /sgp me
/sgp p <oyuncu> = /sgp player <oyuncu>
/sgp c = /sgp clan
/sgp comp = /sgp components
```

### HUD Entegrasyonu

**Güç bilgisi otomatik olarak HUD'da görünür:**
- Sağ taraftaki bilgi panosunda
- Her saniye güncellenir (cache ile optimize)

---

## 🌙 GECE SALDIRI DALGASI KOMUTLARI ⭐ YENİ

### `/stratocraft disaster wave start`

**Açıklama**: Gece dalgasını manuel olarak başlatır (dünya zamanını gece yarısına ayarlar)

**Kullanım**:
```
/stratocraft disaster wave start
/stratocraft disaster wave başlat
```

**Özellikler**:
- Dünya zamanını 18000 tick'e (gece yarısı) ayarlar
- Dalga otomatik olarak başlayacak
- Eğer dalga zaten aktifse hata mesajı gösterir

**Çıktı**:
```
§aGece dalgası başlatıldı! (Dünya zamanı gece yarısına ayarlandı)
§7Dalga otomatik olarak başlayacak...
```

---

### `/stratocraft disaster wave stop`

**Açıklama**: Gece dalgasını manuel olarak durdurur (dünya zamanını güneş doğuşuna ayarlar)

**Kullanım**:
```
/stratocraft disaster wave stop
/stratocraft disaster wave durdur
```

**Özellikler**:
- Dünya zamanını 0 tick'e (güneş doğuşu) ayarlar
- Dalga otomatik olarak duracak
- Eğer dalga zaten aktif değilse hata mesajı gösterir

**Çıktı**:
```
§aGece dalgası durduruldu! (Dünya zamanı güneş doğuşuna ayarlandı)
§7Dalga otomatik olarak duracak...
```

---

### `/stratocraft disaster wave status`

**Açıklama**: Gece dalgası durumunu gösterir

**Kullanım**:
```
/stratocraft disaster wave status
/stratocraft disaster wave durum
```

**Çıktı**:
```
§6=== Gece Dalgası Durumu ===
§7Dünya: §eworld
§7Durum: §aAktif
§7Zaman: §e18500 tick
§7Gece: §aEvet
§7Gece yarısına kalan: §e5500 tick
```

**Bilgiler**:
- Dünya adı
- Dalga durumu (Aktif/Pasif)
- Mevcut zaman (tick)
- Gece durumu (Evet/Hayır)
- Gece yarısına kalan süre (tick)

---

### Tab Completion

**Otomatik Tamamlama:**
- `/stratocraft disaster ` → `wave` seçeneği gösterilir
- `/stratocraft disaster wave ` → `start`, `stop`, `status` seçenekleri gösterilir

**Kullanım**:
```
/stratocraft disaster [TAB] → wave
/stratocraft disaster wave [TAB] → start, stop, status
```
- Format: `💪 Güç: 1234 SGP (Seviye 5)`

---

**🎮 Admin komutlarıyla sunucuyu yönet, test et, dengele!**
