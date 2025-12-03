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
- `bossitem` - Boss özel itemleri
- `recipebook` - Tarif kitapları

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

Tarif Kitapları:
recipe_tectonic - Tarif: Tektonik Sabitleyici
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
```
weapon_l1_1 ila weapon_l1_5 - Seviye 1 Silahlar (5 varyant)
weapon_l2_1 ila weapon_l2_5 - Seviye 2 Silahlar (5 varyant)
weapon_l3_1 ila weapon_l3_5 - Seviye 3 Silahlar (5 varyant)
weapon_l4_1 ila weapon_l4_5 - Seviye 4 Silahlar (5 varyant)
weapon_l5_1 ila weapon_l5_5 - Seviye 5 Silahlar (5 varyant)
Toplam: 25 silah
```

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

Felaketler:
titan_golem
abyssal_worm, hiclik_solucani
```

---

## ⚙️ SİSTEM KOMUTLARI

### `/scadmin disaster <tip>`

**Açıklama**: Felaket tetikle

**KOD DOĞRULANDI**:
```java
// AdminCommandExecutor.java satır 250-280
// DisasterManager kullanır
```

**Kullanım**:
```
/scadmin disaster titan_golem
/scadmin disaster abyssal_worm
/scadmin disaster solar_flare
```

**Feladet Tipleri**:
```
titan_golem - Yürüyen Dağ
abyssal_worm - Hiçlik Solucanı
solar_flare - Güneş Fırtınası
```

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
/scadmin contract list - Aktif kontratları listele
/scadmin contract clear - Tüm kontratları temizle
```

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
```

**Boss Tipleri**:
```
goblin_king, orc_chief, troll_king
dragon, trex, cyclops
titan_golem, hell_dragon, hydra, phoenix
void_dragon, chaos_titan, chaos_god
```

**Örnekler**:
```
/scadmin boss build goblin_king  → Goblin Kralı ritüeli yapısı
/scadmin boss build dragon       → Ejderha ritüeli yapısı
```

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
/scadmin disaster titan_golem

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

**🎮 Admin komutlarıyla sunucuyu yönet, test et, dengele!**
