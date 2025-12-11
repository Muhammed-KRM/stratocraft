# 🎯 MENÜ ERİŞİM SİSTEMİ GELİŞTİRME PLANI

Bu doküman, Degisim klasöründeki kodları kullanarak yeni menü erişim sistemini adım adım geliştirme planını içerir.

---

## 📋 ÖNCELİK SIRASI

### Faz 1: Temel Altyapı (Öncelik: YÜKSEK)
1. ✅ ItemManager'a PERSONAL_TERMINAL item'ı ekleme
2. ✅ Structure.Type enum'una yeni yapı tipleri ekleme
3. ✅ ItemManager'da PERSONAL_TERMINAL tarifi kaydetme

### Faz 2: Personal Terminal Sistemi (Öncelik: YÜKSEK)
4. ✅ PersonalTerminalListener oluşturma (Degisim'den kopyala)
5. ✅ Main.java'da PersonalTerminalListener kaydetme
6. ✅ Menü entegrasyonları (PowerMenu, TrainingMenu, vb.)

### Faz 3: HUD Entegrasyonu (Öncelik: YÜKSEK - PERSONAL_TERMINAL item yapılması gerektiği bilgisi önemli)
7. ⚠️ HUDManager'a Personal Terminal kontrolü ekleme
8. ⚠️ HUDManager'a yapı bilgilendirmesi ekleme

### Faz 4: Yapı Menü Sistemi (Öncelik: ORTA)
9. ✅ StructureMenuListener oluşturma (Degisim'den kopyala)
10. ✅ Main.java'da StructureMenuListener kaydetme
11. ✅ Yapı tiplerine göre menü açma mantığı

### Faz 5: Yapı Aktivasyon Sistemi (Öncelik: ORTA)
12. ✅ StructureActivationListener güncelleme (yeni yapı tipleri için pattern'ler)
13. ✅ Yeni yapı pattern'lerini tanımlama
14. ✅ Yapı aktivasyon kontrolü

### Faz 6: Menü Entegrasyonları (Öncelik: DÜŞÜK)
15. ⚠️ PowerMenu - PersonalTerminalListener entegrasyonu
16. ⚠️ TrainingMenu - PersonalTerminalListener entegrasyonu
17. ⚠️ TamingMenu - PersonalTerminalListener + TRAINING_ARENA entegrasyonu
18. ⚠️ BreedingMenu - PersonalTerminalListener + TRAINING_ARENA entegrasyonu
19. ⚠️ ContractMenu - PersonalTerminalListener + CONTRACT_OFFICE entegrasyonu
20. ⚠️ ClanMenu - CLAN_MANAGEMENT_CENTER entegrasyonu
21. ⚠️ ClanBankMenu - CLAN_BANK entegrasyonu
22. ⚠️ CaravanMenu - CARAVAN_STATION entegrasyonu
23. ⚠️ ShopMenu - MARKET_PLACE entegrasyonu
24. ⚠️ RecipeMenu - RECIPE_LIBRARY entegrasyonu

---

## 🔧 ADIM ADIM GELİŞTİRME

### ✅ FAZ 1: TEMEL ALTYAPI

#### Adım 1.1: Structure.Type Enum'una Yeni Tipler Ekleme

**Dosya:** `src/main/java/me/mami/stratocraft/model/Structure.java`

**Yapılacaklar:**
- Degisim'deki Structure.java'dan yeni yapı tiplerini kopyala
- Mevcut projedeki Structure.java'ya ekle

**Yeni Yapı Tipleri:**
```java
// --- YÖNETİM & MENÜ YAPILARI ---
PERSONAL_MISSION_GUILD,  // Kişisel Görev Loncası (her yere yapılabilir)
CLAN_MANAGEMENT_CENTER, // Klan Yönetim Merkezi (Klan menüleri)
CLAN_BANK,              // Klan Bankası
CLAN_MISSION_GUILD,     // Klan Görev Loncası (sadece klan içine)
TRAINING_ARENA,         // Eğitim Alanı (Eğitilmiş Canlılar, Üreme)
CARAVAN_STATION,        // Kervan İstasyonu
CONTRACT_OFFICE,        // Kontrat Bürosu (genel)
MARKET_PLACE,           // Market
RECIPE_LIBRARY          // Tarif Kütüphanesi
```

#### Adım 1.2: ItemManager'a PERSONAL_TERMINAL Ekleme

**Dosya:** `src/main/java/me/mami/stratocraft/manager/ItemManager.java`

**Yapılacaklar:**
1. Degisim'deki ItemManager.java'dan PERSONAL_TERMINAL tanımını bul
2. Mevcut projedeki ItemManager.java'ya ekle
3. `init()` metodunda PERSONAL_TERMINAL'ı oluştur
4. `registerPersonalTerminalRecipe()` metodunu ekle (Degisim'den kopyala)

**Kod Örneği:**
```java
public static ItemStack PERSONAL_TERMINAL;

// init() metodunda:
PERSONAL_TERMINAL = create(Material.COMPASS, "PERSONAL_TERMINAL", 
    "§e§lKişisel Yönetim Terminali",
    java.util.Arrays.asList(
        "§7Kişisel işlemlerinizi yönetin",
        "§7Sağ tık ile menüyü açın"
    ));

// registerRecipes() metodunda:
registerPersonalTerminalRecipe();
```

---

### ✅ FAZ 2: PERSONAL TERMINAL SİSTEMİ

#### Adım 2.1: PersonalTerminalListener Oluşturma

**Dosya:** `src/main/java/me/mami/stratocraft/listener/PersonalTerminalListener.java` (YENİ)

**Yapılacaklar:**
1. Degisim klasöründeki PersonalTerminalListener.java'yı kopyala
2. Mevcut projeye ekle
3. Main.java'da kaydet

**Degisim'den Kopyalanacak Dosya:**
- `Degisim/src/main/java/me/mami/stratocraft/listener/PersonalTerminalListener.java`

**Main.java'da Kayıt:**
```java
// onEnable() metodunda:
if (personalTerminalListener == null) {
    personalTerminalListener = new PersonalTerminalListener(this);
    getServer().getPluginManager().registerEvents(personalTerminalListener, this);
}
```

#### Adım 2.2: Menü Entegrasyonları

**Yapılacaklar:**
- PowerMenu: `openMainMenu(Player player, boolean fromPersonalTerminal)` metodunu kullan
- TrainingMenu: `openMainMenu(Player player)` metodunu kullan
- TamingMenu: `openMainMenu(Player player, boolean personalOnly)` metodunu kullan (personalOnly=true)
- BreedingMenu: `openMainMenu(Player player)` metodunu kullan
- ContractMenu: `openMainMenu(Player player, int page)` metodunu kullan (page=0, sadece oyuncunun kontratları)

**Not:** PersonalTerminalListener zaten bu entegrasyonları içeriyor (Degisim'den).

---

### ✅ FAZ 3: YAPI MENÜ SİSTEMİ

#### Adım 3.1: StructureMenuListener Oluşturma

**Dosya:** `src/main/java/me/mami/stratocraft/listener/StructureMenuListener.java` (YENİ)

**Yapılacaklar:**
1. Degisim klasöründeki StructureMenuListener.java'yı kopyala
2. Mevcut projeye ekle
3. Main.java'da kaydet

**Degisim'den Kopyalanacak Dosya:**
- `Degisim/src/main/java/me/mami/stratocraft/listener/StructureMenuListener.java`

**Main.java'da Kayıt:**
```java
// onEnable() metodunda:
if (structureMenuListener == null) {
    structureMenuListener = new StructureMenuListener(
        this, 
        getClanManager(), 
        getTerritoryManager()
    );
    getServer().getPluginManager().registerEvents(structureMenuListener, this);
}
```

**Önemli:** StructureMenuListener şu yapı tiplerini destekler:
- PERSONAL_MISSION_GUILD
- CLAN_MANAGEMENT_CENTER
- CLAN_BANK
- CLAN_MISSION_GUILD
- TRAINING_ARENA
- CARAVAN_STATION
- CONTRACT_OFFICE
- MARKET_PLACE
- RECIPE_LIBRARY

---

### ✅ FAZ 4: YAPI AKTİVASYON SİSTEMİ

#### Adım 4.1: StructureActivationListener Güncelleme

**Dosya:** `src/main/java/me/mami/stratocraft/listener/StructureActivationListener.java`

**Yapılacaklar:**
1. Degisim klasöründeki StructureActivationListener.java'yı incele
2. Yeni yapı tipleri için pattern'leri ekle
3. `detectStructurePattern()` metodunu güncelle

**Yeni Pattern'ler (YAPI_TARIFLERI_REHBERI.md'den):**

1. **PERSONAL_MISSION_GUILD:**
   - 2x2 Taş + Lectern üstünde

2. **CLAN_MANAGEMENT_CENTER:**
   - 3x3 Demir Bloğu + Beacon üstünde

3. **CLAN_BANK:**
   - 2x2 Demir Bloğu + Ender Chest üstünde

4. **CLAN_MISSION_GUILD:**
   - 2x2 Demir Bloğu + Lectern üstünde

5. **TRAINING_ARENA:**
   - 2x2 Demir Bloğu + Enchanting Table üstünde

6. **CARAVAN_STATION:**
   - 2x2 Demir Bloğu + Chest üstünde

7. **CONTRACT_OFFICE:**
   - 2x2 Taş + Anvil üstünde

8. **MARKET_PLACE:**
   - 2x2 Taş + Chest üstünde + Sign yanında

9. **RECIPE_LIBRARY:**
   - Lectern + 2+ Bookshelf yanında

**Degisim'den Kopyalanacak Dosya:**
- `Degisim/src/main/java/me/mami/stratocraft/listener/StructureActivationListener.java`

---

### ⚠️ FAZ 5: MENÜ ENTEGRASYONLARI

#### Adım 5.1: PowerMenu Entegrasyonu

**Dosya:** `src/main/java/me/mami/stratocraft/gui/PowerMenu.java`

**Yapılacaklar:**
- `openMainMenu(Player player, boolean fromPersonalTerminal)` metodunu kullan
- PersonalTerminalListener zaten bu metod çağrısını yapıyor

**Kontrol:**
- Mevcut projedeki PowerMenu.java'da bu metod var mı?
- Yoksa ekle (Degisim'deki PowerMenu.java'dan kopyala)

#### Adım 5.2: TrainingMenu Entegrasyonu

**Dosya:** `src/main/java/me/mami/stratocraft/gui/TrainingMenu.java`

**Yapılacaklar:**
- `openMainMenu(Player player)` metodunu kullan
- PersonalTerminalListener zaten bu metod çağrısını yapıyor

**Kontrol:**
- Mevcut projedeki TrainingMenu.java'da bu metod var mı?
- Yoksa ekle

#### Adım 5.3: TamingMenu Entegrasyonu

**Dosya:** `src/main/java/me/mami/stratocraft/gui/TamingMenu.java`

**Yapılacaklar:**
- `openMainMenu(Player player, boolean personalOnly)` metodunu kullan
- PersonalTerminalListener: `openMainMenu(player, true)` çağırıyor (kişisel mod)
- StructureMenuListener (TRAINING_ARENA): `openMainMenu(player, false)` çağırmalı (klan modu)

**Kontrol:**
- Mevcut projedeki TamingMenu.java'da bu metod var mı?
- StructureMenuListener'da TRAINING_ARENA case'inde `openMainMenu(player, false)` çağrısı var mı?

#### Adım 5.4: BreedingMenu Entegrasyonu

**Dosya:** `src/main/java/me/mami/stratocraft/gui/BreedingMenu.java`

**Yapılacaklar:**
- `openMainMenu(Player player)` metodunu kullan
- PersonalTerminalListener zaten bu metod çağrısını yapıyor

**Kontrol:**
- Mevcut projedeki BreedingMenu.java'da bu metod var mı?
- Yoksa ekle

#### Adım 5.5: ContractMenu Entegrasyonu

**Dosya:** `src/main/java/me/mami/stratocraft/gui/ContractMenu.java`

**Yapılacaklar:**
- `openMainMenu(Player player, int page)` metodunu kullan
- PersonalTerminalListener: `openMainMenu(player, 0)` çağırıyor (sadece oyuncunun kontratları)
- StructureMenuListener (CONTRACT_OFFICE): `openMainMenu(player, 0)` çağırıyor (tüm kontratlar)

**Kontrol:**
- Mevcut projedeki ContractMenu.java'da bu metod var mı?
- StructureMenuListener'da CONTRACT_OFFICE case'inde doğru çağrı var mı?

#### Adım 5.6: ClanMenu Entegrasyonu

**Dosya:** `src/main/java/me/mami/stratocraft/gui/ClanMenu.java`

**Yapılacaklar:**
- `openMenu(Player player)` metodunu kullan
- StructureMenuListener (CLAN_MANAGEMENT_CENTER) zaten bu metod çağrısını yapıyor

**Kontrol:**
- Mevcut projedeki ClanMenu.java'da bu metod var mı?
- StructureMenuListener'da CLAN_MANAGEMENT_CENTER case'inde doğru çağrı var mı?

#### Adım 5.7: ClanBankMenu Entegrasyonu

**Dosya:** `src/main/java/me/mami/stratocraft/gui/ClanBankMenu.java`

**Yapılacaklar:**
- `openMainMenu(Player player)` metodunu kullan
- StructureMenuListener (CLAN_BANK) zaten bu metod çağrısını yapıyor

**Kontrol:**
- Mevcut projedeki ClanBankMenu.java'da bu metod var mı?
- StructureMenuListener'da CLAN_BANK case'inde doğru çağrı var mı?

#### Adım 5.8: CaravanMenu Entegrasyonu

**Dosya:** `src/main/java/me/mami/stratocraft/gui/CaravanMenu.java`

**Yapılacaklar:**
- `openMainMenu(Player player)` metodunu kullan
- StructureMenuListener (CARAVAN_STATION) zaten bu metod çağrısını yapıyor

**Kontrol:**
- Mevcut projedeki CaravanMenu.java'da bu metod var mı?
- StructureMenuListener'da CARAVAN_STATION case'inde doğru çağrı var mı?

#### Adım 5.9: ShopMenu Entegrasyonu

**Dosya:** `src/main/java/me/mami/stratocraft/gui/ShopMenu.java`

**Yapılacaklar:**
- `createMarketListMenu(List<Shop> shops, int page)` static metodunu ekle (Degisim'den)
- StructureMenuListener (MARKET_PLACE) bu metodu çağırıyor

**Kontrol:**
- Mevcut projedeki ShopMenu.java'da bu metod var mı?
- Yoksa Degisim'deki ShopMenu.java'dan kopyala

#### Adım 5.10: RecipeMenu Entegrasyonu

**Dosya:** `src/main/java/me/mami/stratocraft/gui/RecipeMenu.java`

**Yapılacaklar:**
- `createRecipeLibraryMenu(Player player, int page)` static metodunu ekle (Degisim'den)
- StructureMenuListener (RECIPE_LIBRARY) bu metodu çağırıyor

**Kontrol:**
- Mevcut projedeki RecipeMenu.java'da bu metod var mı?
- Yoksa Degisim'deki RecipeMenu.java'dan kopyala

---

### ⚠️ FAZ 6: HUD ENTEGRASYONU (OPSİYONEL)

#### Adım 6.1: HUDManager Güncelleme

**Dosya:** `src/main/java/me/mami/stratocraft/manager/HUDManager.java`

**Yapılacaklar:**
1. Degisim klasöründeki HUDManager.java'yı incele
2. Personal Terminal kontrolü ekle
3. Yapı bilgilendirmesi ekle

**Eklemeler:**
```java
private void checkPersonalTerminal(Player player) {
    if (!hasPersonalTerminal(player)) {
        player.sendActionBar("§eKişisel Yönetim Terminali yapmanız gerekiyor! §7(8x Kağıt + 1x Kırmızı Taş)");
    }
}

private boolean hasPersonalTerminal(Player player) {
    for (ItemStack item : player.getInventory().getContents()) {
        if (item != null && ItemManager.isCustomItem(item, "PERSONAL_TERMINAL")) {
            return true;
        }
    }
    return false;
}
```

---

## 📝 KONTROL LİSTESİ

### Faz 1: Temel Altyapı
- [ ] Structure.Type enum'una yeni tipler eklendi mi?
- [ ] ItemManager'da PERSONAL_TERMINAL tanımlandı mı?
- [ ] ItemManager'da PERSONAL_TERMINAL tarifi kaydedildi mi?

### Faz 2: Personal Terminal Sistemi
- [ ] PersonalTerminalListener oluşturuldu mu?
- [ ] Main.java'da PersonalTerminalListener kaydedildi mi?
- [ ] PersonalTerminalListener menü entegrasyonları çalışıyor mu?

### Faz 3: Yapı Menü Sistemi
- [ ] StructureMenuListener oluşturuldu mu?
- [ ] Main.java'da StructureMenuListener kaydedildi mi?
- [ ] Tüm yapı tipleri için menü açma mantığı çalışıyor mu?

### Faz 4: Yapı Aktivasyon Sistemi
- [ ] StructureActivationListener güncellendi mi?
- [ ] Yeni yapı pattern'leri tanımlandı mı?
- [ ] Yapı aktivasyon kontrolü çalışıyor mu?

### Faz 5: Menü Entegrasyonları
- [ ] PowerMenu entegrasyonu çalışıyor mu?
- [ ] TrainingMenu entegrasyonu çalışıyor mu?
- [ ] TamingMenu entegrasyonu çalışıyor mu?
- [ ] BreedingMenu entegrasyonu çalışıyor mu?
- [ ] ContractMenu entegrasyonu çalışıyor mu?
- [ ] ClanMenu entegrasyonu çalışıyor mu?
- [ ] ClanBankMenu entegrasyonu çalışıyor mu?
- [ ] CaravanMenu entegrasyonu çalışıyor mu?
- [ ] ShopMenu entegrasyonu çalışıyor mu?
- [ ] RecipeMenu entegrasyonu çalışıyor mu?

### Faz 6: HUD Entegrasyonu
- [ ] HUDManager'a Personal Terminal kontrolü eklendi mi?
- [ ] HUDManager'a yapı bilgilendirmesi eklendi mi?

---

## 🚀 BAŞLANGIÇ ADIMLARI

1. **Structure.java'yı güncelle** - Yeni yapı tiplerini ekle
2. **ItemManager.java'yı güncelle** - PERSONAL_TERMINAL ekle
3. **PersonalTerminalListener.java'yı kopyala** - Degisim'den mevcut projeye
4. **StructureMenuListener.java'yı kopyala** - Degisim'den mevcut projeye
5. **StructureActivationListener.java'yı güncelle** - Yeni pattern'leri ekle
6. **Main.java'yı güncelle** - Listener'ları kaydet
7. **Test et** - Her adımı test et

---

**Hazırlayan:** AI Assistant  
**Tarih:** 2024  
**Versiyon:** 1.0  
**Kaynak:** Degisim klasörü + MENU_ERISIM_SISTEMI_PLANI.md + OZELLIK_GELISTIRME_PLANI.md + YAPI_TARIFLERI_REHBERI.md

