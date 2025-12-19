# 🎯 MENÜ ERİŞİM SİSTEMİ PLANI

Bu doküman, tüm GUI menülerinin komut yerine item/yapı/ritüel ile erişilebilmesi için detaylı planı içerir.

---

## 📋 İÇİNDEKİLER

1. [Menü Kategorizasyonu](#menü-kategorizasyonu)
2. [Kişisel Menüler](#kişisel-menüler)
3. [Klan Menüleri](#klan-menüleri)
4. [Genel Menüler](#genel-menüler)
5. [Yeni Item ve Yapılar](#yeni-item-ve-yapılar)
6. [Implementasyon Planı](#implementasyon-planı)
7. [Güvenlik ve Kontroller](#güvenlik-ve-kontroller)

---

## 🎯 MENÜ KATEGORİZASYONU

### Kategoriler:
1. **Kişisel Menüler** - Oyuncunun kendi verilerini yönettiği menüler
2. **Klan Menüleri** - Klan verilerini yönettiği menüler (yapı gerektirir)
3. **Genel Menüler** - Herkese açık menüler (yapı veya item ile)

---

## 👤 KİŞİSEL MENÜLER

Bu menüler oyuncunun kendi verilerini yönetir. **Özel bir item** ile açılır.

### 1. Kişisel Yönetim Terminali (PERSONAL_TERMINAL)

**Item ID:** `PERSONAL_TERMINAL`  
**Item İsmi:** `§e§lKişisel Yönetim Terminali`  
**Material:** `COMPASS`  
**Tarif:** Kolay (başlangıç item'i)
```
[P] [P] [P]
[P] [R] [P]
[P] [P] [P]
P = Paper (Kağıt)
R = Redstone (Kırmızı Taş)
```

**Açılan Menüler:**
- Güç Menüsü (PowerMenu)
- Eğitim İlerlemesi (TrainingMenu)
- Eğitilmiş Canlılar (TamingMenu)
- Üreme Yönetimi (BreedingMenu)
- Kişisel Kontratlar (ContractMenu - sadece oyuncunun kontratları)
- Kişisel Görevler (MissionMenu - sadece oyuncunun görevleri)

**Kullanım:**
- Sağ tık → Ana menü açılır
- Ana menüden alt menülere geçiş

**HUD Entegrasyonu:**
- Oyuncunun envanterinde yoksa HUD'da gösterilir:
  - "§eKişisel Yönetim Terminali yapmanız gerekiyor!"
  - "§7Tarif: 8x Kağıt + 1x Kırmızı Taş"

---

## 🏛️ KLAN MENÜLERİ

Bu menüler klan verilerini yönetir. **Özel yapılar** gerektirir.

### 1. Klan Yönetim Merkezi (CLAN_MANAGEMENT_CENTER)

**Yapı Tipi:** `CLAN_MANAGEMENT_CENTER` (YENİ)  
**Yapı İsmi:** `§6Klan Yönetim Merkezi`  
**Blok Pattern:** 3x3x3 (27 blok)
```
Seviye 1: Taş + Demir Bloğu
Seviye 2: Taş + Altın Bloğu
Seviye 3: Taş + Elmas Bloğu
```

**Açılan Menüler:**
- Ana Klan Menüsü (ClanMenu)
- Üye Yönetimi (ClanMemberMenu)
- Klan İstatistikleri (ClanStatsMenu)
- Yapı Yönetimi (ClanStructureMenu)
- İttifak Yönetimi (AllianceMenu)

**Kullanım:**
- Yapıya Shift + Sağ Tık → Ana menü açılır
- Ana menüden alt menülere geçiş

**Yetki Kontrolü:**
- Klan üyesi olmalı
- Yapı klan bölgesinde olmalı
- Recruit seviyesi bazı menülere erişemez

---

### 2. Klan Bankası (CLAN_BANK)

**Yapı Tipi:** `CLAN_BANK` (YENİ)  
**Yapı İsmi:** `§aKlan Bankası`  
**Blok Pattern:** 2x2x2 (8 blok)
```
Seviye 1: Ender Chest + Demir Bloğu
Seviye 2: Ender Chest + Altın Bloğu
Seviye 3: Ender Chest + Elmas Bloğu
```

**Açılan Menüler:**
- Klan Bankası Menüsü (ClanBankMenu)

**Kullanım:**
- Yapıya Sağ Tık → Banka menüsü açılır
- Ender Chest'e direkt erişim

**Yetki Kontrolü:**
- Klan üyesi olmalı
- Rütbe bazlı yetki (yatırma/çekme)

---

### 3. Klan Görev Loncası (CLAN_MISSION_GUILD)

**Yapı Tipi:** `CLAN_MISSION_GUILD` (YENİ)  
**Yapı İsmi:** `§eKlan Görev Loncası`  
**Blok Pattern:** 2x2x3 (12 blok)
```
Seviye 1: Lectern + Taş
Seviye 2: Lectern + Demir Bloğu
Seviye 3: Lectern + Altın Bloğu
```

**Açılan Menüler:**
- Klan Görevleri (ClanMissionMenu)

**Kullanım:**
- Yapıya Sağ Tık → Görev menüsü açılır
- Görev oluşturma/yönetme

**Yetki Kontrolü:**
- Klan üyesi olmalı
- General veya Lider rütbesi gerektirir (görev oluşturma için)

---

### 4. Eğitim Alanı (TRAINING_ARENA)

**Yapı Tipi:** `TRAINING_ARENA` (YENİ)  
**Yapı İsmi:** `§bEğitim Alanı`  
**Blok Pattern:** 3x3x2 (18 blok)
```
Seviye 1: Enchanting Table + Demir Bloğu
Seviye 2: Enchanting Table + Altın Bloğu
Seviye 3: Enchanting Table + Elmas Bloğu
```

**Açılan Menüler:**
- Eğitilmiş Canlılar (TamingMenu)
- Üreme Yönetimi (BreedingMenu)

**Kullanım:**
- Yapıya Sağ Tık → Eğitim menüsü açılır
- Canlı yönetimi ve üreme

**Yetki Kontrolü:**
- Klan üyesi olmalı
- Klan canlılarını görebilir

---

### 5. Kervan İstasyonu (CARAVAN_STATION)

**Yapı Tipi:** `CARAVAN_STATION` (YENİ)  
**Yapı İsmi:** `§6Kervan İstasyonu`  
**Blok Pattern:** 2x2x2 (8 blok)
```
Seviye 1: Chest + Demir Bloğu
Seviye 2: Chest + Altın Bloğu
Seviye 3: Chest + Elmas Bloğu
```

**Açılan Menüler:**
- Kervan Yönetimi (CaravanMenu)

**Kullanım:**
- Yapıya Sağ Tık → Kervan menüsü açılır
- Kervan oluşturma/yönetme

**Yetki Kontrolü:**
- Klan üyesi olmalı
- General veya Lider rütbesi gerektirir

---

## 🌐 GENEL MENÜLER

Bu menüler herkese açıktır. **Yapı veya item** ile erişilebilir.

### 1. Kontrat Bürosu (CONTRACT_OFFICE)

**Yapı Tipi:** `CONTRACT_OFFICE` (YENİ)  
**Yapı İsmi:** `§6Kontrat Bürosu`  
**Blok Pattern:** 2x2x2 (8 blok)
```
Seviye 1: Anvil + Taş
Seviye 2: Anvil + Demir Bloğu
Seviye 3: Anvil + Altın Bloğu
```

**Açılan Menüler:**
- Kontrat Menüsü (ContractMenu)

**Kullanım:**
- Yapıya Sağ Tık → Kontrat menüsü açılır
- Tüm kontratları görüntüleme
- Kontrat oluşturma

**Yetki Kontrolü:**
- Herkes erişebilir
- Kontrat oluşturma için klan üyesi olmalı (klan kontratları için)

---

### 2. Market (MARKET_PLACE)

**Yapı Tipi:** `MARKET_PLACE` (YENİ)  
**Yapı İsmi:** `§aMarket`  
**Blok Pattern:** 3x3x2 (18 blok)
```
Seviye 1: Chest + Sign + Taş
Seviye 2: Chest + Sign + Demir Bloğu
Seviye 3: Chest + Sign + Altın Bloğu
```

**Açılan Menüler:**
- Market Menüsü (ShopMenu)

**Kullanım:**
- Yapıya Sağ Tık → Market menüsü açılır
- Alışveriş ve teklif verme

**Yetki Kontrolü:**
- Herkes erişebilir

---

### 3. Tarif Kütüphanesi (RECIPE_LIBRARY)

**Yapı Tipi:** `RECIPE_LIBRARY` (YENİ)  
**Yapı İsmi:** `§eTarif Kütüphanesi`  
**Blok Pattern:** 2x2x2 (8 blok)
```
Seviye 1: Lectern + Bookshelf
Seviye 2: Lectern + Enchanted Bookshelf
Seviye 3: Lectern + Golden Bookshelf
```

**Açılan Menüler:**
- Tarif Menüsü (RecipeMenu)

**Kullanım:**
- Yapıya Sağ Tık → Tarif menüsü açılır
- Tüm tarifleri görüntüleme

**Yetki Kontrolü:**
- Herkes erişebilir
- Tarif kitaplarına sahip olanlar detayları görebilir

---

## 🆕 YENİ İTEM VE YAPILAR

### Yeni Item: Kişisel Yönetim Terminali

**Dosya:** `ItemManager.java`  
**Item ID:** `PERSONAL_TERMINAL`  
**Material:** `COMPASS`  
**Tarif:** ShapelessRecipe (8x Paper + 1x Redstone)

**Özellikler:**
- Başlangıç item'i (kolay tarif)
- Oyuncunun envanterinde olmalı
- HUD'da yoksa bilgilendirme gösterilir

---

### Yeni Yapı Tipleri

**Dosya:** `Structure.java`  
**Yeni Enum Değerleri:**
- `CLAN_MANAGEMENT_CENTER` - Klan Yönetim Merkezi
- `CLAN_BANK` - Klan Bankası
- `CLAN_MISSION_GUILD` - Klan Görev Loncası
- `TRAINING_ARENA` - Eğitim Alanı
- `CARAVAN_STATION` - Kervan İstasyonu
- `CONTRACT_OFFICE` - Kontrat Bürosu
- `MARKET_PLACE` - Market
- `RECIPE_LIBRARY` - Tarif Kütüphanesi

---

## 🔧 İMPLEMENTASYON PLANI

### Faz 1: Menü Kategorizasyonu ve Ayrımı

#### 1.1. Kişisel Menüleri Ayırma

**Dosyalar:**
- `PowerMenu.java` - Kişisel (oyuncu gücü)
- `TrainingMenu.java` - Kişisel (oyuncu eğitimi)
- `TamingMenu.java` - Kişisel (oyuncu canlıları)
- `BreedingMenu.java` - Kişisel (oyuncu üreme çiftleri)
- `ContractMenu.java` - Kısmen kişisel (oyuncunun kontratları)
- `MissionMenu.java` - Kişisel (oyuncu görevleri)

**Değişiklikler:**
- Menüleri kişisel/genel olarak ayır
- Kişisel menüler için `PersonalTerminalMenu` wrapper oluştur
- Genel menüler için yapı bazlı erişim

#### 1.2. Klan Menülerini Ayırma

**Dosyalar:**
- `ClanMenu.java` - Klan (yönetim merkezi)
- `ClanMemberMenu.java` - Klan (yönetim merkezi)
- `ClanStatsMenu.java` - Klan (yönetim merkezi)
- `ClanStructureMenu.java` - Klan (yönetim merkezi)
- `ClanBankMenu.java` - Klan (banka yapısı)
- `ClanMissionMenu.java` - Klan (görev loncası)
- `AllianceMenu.java` - Klan (yönetim merkezi)
- `CaravanMenu.java` - Klan (kervan istasyonu)

**Değişiklikler:**
- Her menü için yapı tipi belirle
- Yapı bazlı erişim kontrolü ekle

#### 1.3. Genel Menüleri Ayırma

**Dosyalar:**
- `ContractMenu.java` - Genel (kontrat bürosu)
- `ShopMenu.java` - Genel (market)
- `RecipeMenu.java` - Genel (tarif kütüphanesi)

**Değişiklikler:**
- Yapı bazlı erişim kontrolü ekle

---

### Faz 2: Yeni Item Oluşturma

#### 2.1. Kişisel Yönetim Terminali Item'ı

**Dosya:** `ItemManager.java`

**Eklemeler:**
```java
public static ItemStack PERSONAL_TERMINAL;

// init() metodunda:
PERSONAL_TERMINAL = create(Material.COMPASS, "PERSONAL_TERMINAL", 
    "§e§lKişisel Yönetim Terminali");

// registerRecipes() metodunda:
ShapelessRecipe terminalRecipe = new ShapelessRecipe(
    new NamespacedKey(Main.getInstance(), "craft_personal_terminal"),
    PERSONAL_TERMINAL);
for (int i = 0; i < 8; i++) {
    terminalRecipe.addIngredient(Material.PAPER);
}
terminalRecipe.addIngredient(Material.REDSTONE);
Bukkit.addRecipe(terminalRecipe);
```

---

### Faz 3: Yeni Yapı Tipleri Ekleme

#### 3.1. Structure.java Güncelleme

**Dosya:** `Structure.java`

**Eklemeler:**
```java
public enum Type {
    // ... mevcut yapılar ...
    
    // --- YÖNETİM & MENÜ YAPILARI ---
    CLAN_MANAGEMENT_CENTER,  // Klan Yönetim Merkezi
    CLAN_BANK,               // Klan Bankası
    CLAN_MISSION_GUILD,      // Klan Görev Loncası
    TRAINING_ARENA,          // Eğitim Alanı
    CARAVAN_STATION,         // Kervan İstasyonu
    CONTRACT_OFFICE,         // Kontrat Bürosu
    MARKET_PLACE,            // Market
    RECIPE_LIBRARY           // Tarif Kütüphanesi
}
```

#### 3.2. Yapı Pattern'leri Tanımlama

**Dosya:** `StructureActivationListener.java` veya yeni bir helper

**Pattern Tanımları:**
- Her yapı için 3x3, 2x2, vb. pattern
- Seviye bazlı malzeme gereksinimleri

---

### Faz 4: Listener'lar Oluşturma

#### 4.1. PersonalTerminalListener

**Dosya:** `PersonalTerminalListener.java` (YENİ)

**Özellikler:**
- `PERSONAL_TERMINAL` item'ına sağ tık kontrolü
- Ana menü açma
- Alt menülere yönlendirme

**Metodlar:**
- `onTerminalClick(PlayerInteractEvent)` - Item'a sağ tık
- `openMainMenu(Player)` - Ana terminal menüsü
- `handleMenuClick(InventoryClickEvent)` - Menü tıklamaları

#### 4.2. StructureMenuListener

**Dosya:** `StructureMenuListener.java` (YENİ veya mevcut listener'a ekleme)

**Özellikler:**
- Yapı tiplerine göre menü açma
- Yetki kontrolü
- Klan kontrolü

**Metodlar:**
- `onStructureInteract(PlayerInteractEvent)` - Yapıya sağ tık
- `openMenuForStructure(Player, Structure)` - Yapı tipine göre menü aç

---

### Faz 5: HUD Entegrasyonu

#### 5.1. HUDManager Güncelleme

**Dosya:** `HUDManager.java`

**Eklemeler:**
- Kişisel Terminal kontrolü
- Yapı erişim bilgilendirmesi
- Tarif bilgilendirmesi

**Metodlar:**
- `checkPersonalTerminal(Player)` - Terminal var mı kontrol
- `showTerminalHint(Player)` - Terminal yapması gerektiğini göster
- `showStructureHint(Player, Structure.Type)` - Yapı yapması gerektiğini göster

---

## 🔒 GÜVENLİK VE KONTROLLER

### Kişisel Menüler İçin:
- ✅ Item envanterde var mı kontrol
- ✅ Item ID kontrolü (sahte item koruması)
- ✅ Cooldown kontrolü (spam koruması)

### Klan Menüleri İçin:
- ✅ Klan üyeliği kontrolü
- ✅ Yapı sahipliği kontrolü (klan bölgesinde mi)
- ✅ Rütbe bazlı yetki kontrolü
- ✅ Yapı seviyesi kontrolü (bazı özellikler için)

### Genel Menüler İçin:
- ✅ Yapı varlığı kontrolü
- ✅ Cooldown kontrolü
- ✅ Bölge kontrolü (bazı menüler için)

---

## 📊 MENÜ ERİŞİM TABLOSU

| Menü | Tip | Erişim Yolu | Gereksinimler |
|------|-----|-------------|---------------|
| **Kişisel Menüler** |
| PowerMenu | Kişisel | Personal Terminal | Item: PERSONAL_TERMINAL |
| TrainingMenu | Kişisel | Personal Terminal | Item: PERSONAL_TERMINAL |
| TamingMenu | Kişisel | Personal Terminal veya Eğitim Alanı | Item veya Yapı: TRAINING_ARENA |
| BreedingMenu | Kişisel | Personal Terminal veya Eğitim Alanı | Item veya Yapı: TRAINING_ARENA |
| MissionMenu | Kişisel | Personal Terminal | Item: PERSONAL_TERMINAL |
| ContractMenu (Kişisel) | Kişisel | Personal Terminal | Item: PERSONAL_TERMINAL |
| **Klan Menüleri** |
| ClanMenu | Klan | Yönetim Merkezi | Yapı: CLAN_MANAGEMENT_CENTER |
| ClanMemberMenu | Klan | Yönetim Merkezi | Yapı: CLAN_MANAGEMENT_CENTER |
| ClanStatsMenu | Klan | Yönetim Merkezi | Yapı: CLAN_MANAGEMENT_CENTER |
| ClanStructureMenu | Klan | Yönetim Merkezi | Yapı: CLAN_MANAGEMENT_CENTER |
| AllianceMenu | Klan | Yönetim Merkezi | Yapı: CLAN_MANAGEMENT_CENTER |
| ClanBankMenu | Klan | Klan Bankası | Yapı: CLAN_BANK |
| ClanMissionMenu | Klan | Görev Loncası | Yapı: CLAN_MISSION_GUILD |
| CaravanMenu | Klan | Kervan İstasyonu | Yapı: CARAVAN_STATION |
| **Genel Menüler** |
| ContractMenu (Genel) | Genel | Kontrat Bürosu | Yapı: CONTRACT_OFFICE |
| ShopMenu | Genel | Market | Yapı: MARKET_PLACE |
| RecipeMenu | Genel | Tarif Kütüphanesi | Yapı: RECIPE_LIBRARY |

---

## 🎨 MENÜ YAPILARI

### Personal Terminal Ana Menüsü

**Boyut:** 27 slot (3x9)

**Layout:**
```
[Güç]     [Eğitim]    [Canlılar]
[Görevler] [Kontratlar] [Üreme]
[Geri]    [Bilgi]     [Kapat]
```

**Slotlar:**
- Slot 10: Güç Menüsü (POWER_CRYSTAL)
- Slot 12: Eğitim Menüsü (EXPERIENCE_BOTTLE)
- Slot 14: Canlılar Menüsü (SPAWNER)
- Slot 16: Görevler Menüsü (TOTEM_OF_UNDYING)
- Slot 20: Kontratlar Menüsü (PAPER)
- Slot 22: Üreme Menüsü (GOLDEN_APPLE)
- Slot 18: Bilgi (BOOK)
- Slot 26: Kapat (BARRIER)

---

### Klan Yönetim Merkezi Ana Menüsü

**Boyut:** 27 slot (3x9)

**Layout:**
```
[Ana Bilgiler] [Üyeler]    [İstatistikler]
[Yapılar]      [İttifaklar] [Geri]
```

**Slotlar:**
- Slot 10: Ana Bilgiler (BEACON)
- Slot 12: Üyeler (PLAYER_HEAD)
- Slot 14: İstatistikler (PAPER)
- Slot 16: Yapılar (BEACON)
- Slot 18: İttifaklar (DIAMOND)
- Slot 22: Geri (ARROW)

---

## 🛠️ TEKNİK DETAYLAR

### Item Kontrolü

**Helper Metod:**
```java
public static boolean hasPersonalTerminal(Player player) {
    for (ItemStack item : player.getInventory().getContents()) {
        if (item != null && ItemManager.isCustomItem(item, "PERSONAL_TERMINAL")) {
            return true;
        }
    }
    return false;
}
```

### Yapı Kontrolü

**Helper Metod:**
```java
public static Structure getNearbyStructure(Player player, Structure.Type type, double radius) {
    Location loc = player.getLocation();
    Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
    if (clan == null) return null;
    
    for (Structure structure : clan.getStructures()) {
        if (structure.getType() == type && 
            structure.getLocation().distance(loc) <= radius) {
            return structure;
        }
    }
    return null;
}
```

### HUD Bilgilendirme

**HUDManager'a Ekleme:**
```java
private void checkPersonalTerminal(Player player) {
    if (!hasPersonalTerminal(player)) {
        // HUD'da göster
        player.sendActionBar("§eKişisel Yönetim Terminali yapmanız gerekiyor! §7(8x Kağıt + 1x Kırmızı Taş)");
    }
}
```

---

## 📝 YAPILACAKLAR LİSTESİ

### Öncelik 1: Temel Altyapı
- [ ] Menüleri kişisel/genel olarak ayır
- [ ] Personal Terminal item'ı oluştur
- [ ] PersonalTerminalListener oluştur
- [ ] Personal Terminal ana menüsü oluştur

### Öncelik 2: Yapı Sistemi
- [ ] Yeni yapı tiplerini Structure.java'ya ekle
- [ ] Yapı pattern'lerini tanımla
- [ ] StructureMenuListener oluştur/güncelle
- [ ] Yapı aktivasyon sistemini güncelle

### Öncelik 3: Menü Entegrasyonu
- [ ] Kişisel menüleri Personal Terminal'e bağla
- [ ] Klan menülerini yapılara bağla
- [ ] Genel menüleri yapılara bağla
- [ ] Yetki kontrollerini ekle

### Öncelik 4: HUD ve Bilgilendirme
- [ ] HUD'a terminal kontrolü ekle
- [ ] HUD'a yapı bilgilendirmesi ekle
- [ ] Tarif bilgilendirmesi ekle

### Öncelik 5: Test ve Optimizasyon
- [ ] Tüm menüleri test et
- [ ] Performans optimizasyonu
- [ ] Hata kontrolleri

---

## 🔄 MİGRASYON PLANI

### Mevcut Komutlar
- `/klan` → Klan Yönetim Merkezi'ne yönlendirme mesajı
- `/kontrat` → Kontrat Bürosu'na yönlendirme mesajı
- `/sgp` → Personal Terminal'e yönlendirme mesajı

**Mesaj Örneği:**
```
§cBu menü artık komutla açılamaz!
§7Klan Yönetim Merkezi yapısına sağ tıklayın.
§7Tarif: [tarif bilgisi]
```

---

## 🎯 SONUÇ

Bu plan, tüm menülerin komut yerine item/yapı ile erişilebilmesini sağlar. Oyuncular:
- Kişisel işlemler için Personal Terminal kullanır
- Klan işlemleri için yapılar kullanır
- Genel işlemler için genel yapılar kullanır

**Avantajlar:**
- ✅ Komut kullanımı azalır
- ✅ Atmosferik oyun deneyimi
- ✅ Fiziksel yapılar oyuncuları klan bölgelerine çeker
- ✅ HUD bilgilendirmesi ile rehberlik

---

**Hazırlayan:** AI Assistant  
**Tarih:** 2024  
**Versiyon:** 1.0














