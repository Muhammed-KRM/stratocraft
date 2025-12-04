# STRATOCRAFT - GUI/MENÜ SİSTEMİ ANALİZİ

## 📋 MİNECRAFT PLUGİNLERİNDE GUI SİSTEMLERİ

### 1. **Inventory Tabanlı GUI (En Yaygın Yöntem)**

**Nasıl Çalışır:**
```java
// 1. Özel envanter oluştur
Inventory menu = Bukkit.createInventory(null, 9, "Menü Başlığı");

// 2. ItemStack'ler ekle (butonlar)
ItemStack button = new ItemStack(Material.DIAMOND);
ItemMeta meta = button.getItemMeta();
meta.setDisplayName("§aButon");
meta.setLore(Arrays.asList("§7Açıklama"));
button.setItemMeta(meta);
menu.setItem(4, button); // 4. slot'a koy

// 3. Oyuncuya aç
player.openInventory(menu);

// 4. Tıklamaları dinle
@EventHandler
public void onInventoryClick(InventoryClickEvent event) {
    if (!event.getView().getTitle().equals("Menü Başlığı")) return;
    event.setCancelled(true); // Eşya çıkarılmasını engelle
    
    ItemStack clicked = event.getCurrentItem();
    if (clicked.getType() == Material.DIAMOND) {
        // Fonksiyon çalıştır
    }
}
```

**Menü Konumu:**
- `player.openInventory()` çağrıldığında menü **otomatik olarak oyuncunun ekranında ortalanmış şekilde** açılır
- Konum ayarlanamaz (Minecraft'ın kendi sistemi)
- Menü boyutu: 9, 18, 27, 36, 45, 54 slot olabilir

**Tetikleme Yöntemleri:**
- `PlayerInteractEvent` (Sağ/Sol tık)
- Komut (`/komut`)
- `PlayerMoveEvent` (Hareket)
- Scheduler task (Zamanlayıcı)

---

### 2. **ActionBar (Ekranın Altında)**

**Nasıl Çalışır:**
```java
player.sendActionBar(Component.text("§aMesaj burada görünür"));
```

**Kullanım:**
- Ekranın en altında küçük mesaj
- 2-3 saniye görünür
- Çok fazla bilgi gösterilemez

---

### 3. **Title/Subtitle (Ekranın Ortasında)**

**Nasıl Çalışır:**
```java
player.sendTitle(
    Component.text("§cBAŞLIK"),      // Büyük başlık
    Component.text("§7Alt başlık"),  // Küçük alt başlık
    10,  // Fade in (tick)
    40,  // Stay (tick)
    10   // Fade out (tick)
);
```

**Kullanım:**
- Ekranın ortasında büyük mesaj
- Önemli bildirimler için ideal

---

## 🎯 MEVCUT SİSTEMİMİZ

### ✅ **WeaponModeManager (Çok Modlu Silahlar)**

**Nasıl Çalışıyor:**
1. **Tetikleme**: Shift + Sağ Tık (`PlayerInteractEvent`)
2. **Menü Oluşturma**: `Bukkit.createInventory(null, 9, "Mod Seçimi")`
3. **Butonlar**: Her mod için özel ikon (Material + Lore)
4. **Tıklama**: `InventoryClickEvent` ile mod seçimi
5. **Fonksiyon**: Seçilen mod silahın NBT'sine kaydedilir

**Avantajlar:**
- ✅ Görsel ve kullanıcı dostu
- ✅ Tıklama ile kolay seçim
- ✅ Mevcut mod işaretleniyor (✓)
- ✅ Ses efektleri var

**Bu Sistem Zaten Modern ve Doğru!** 🎉

---

### 📊 **Casusluk Dürbünü (Şu Anki Durum)**

**Şu Anki Sistem:**
- Chat mesajları ile bilgi gösteriliyor
- 3 saniye bakınca otomatik gösteriliyor

**Öneri: GUI Menüsü Ekleyelim!**

---

## 🆕 CASUSLUK DÜRBÜNÜ İÇİN GUI MENÜSÜ

### Tasarım Önerisi:

**Menü Boyutu**: 27 slot (3x9)
**Başlık**: "§eCasusluk Raporu: [Oyuncu Adı]"

**Menü İçeriği:**
```
[ ][ ][ ][ ][ ][ ][ ][ ][ ]
[ ][C][ ][H][ ][A][ ][Z][ ]  C=Can, H=Açlık, A=Zırh, Z=Zırh Detay
[ ][E][ ][I][ ][X][ ][ ][ ]  E=Efektler, I=Envanter, X=Kapat
```

**Butonlar:**
- **Can Butonu** (Material: REDSTONE): Can/Max Can gösterir
- **Açlık Butonu** (Material: BREAD): Açlık/Doygunluk gösterir
- **Zırh Butonu** (Material: IRON_CHESTPLATE): Zırh puanı gösterir
- **Envanter Butonu** (Material: CHEST): Envanter doluluğu gösterir
- **Efektler Butonu** (Material: POTION): Aktif efektler listesi
- **Kapat Butonu** (Material: BARRIER): Menüyü kapatır

**Tıklama İşlemleri:**
- Butona tıklayınca o bilgiyi chat'te detaylı göster
- Veya alt menü aç (örneğin Efektler butonuna tıklayınca efekt listesi)

---

## 🔄 ALTERNATİF YÖNTEMLER

### 1. **ActionBar ile Anlık Bilgi**

```java
// Casusluk sırasında ActionBar'da bilgi göster
player.sendActionBar(Component.text(
    "§e" + target.getName() + 
    " §7| §c" + String.format("%.1f", target.getHealth()) + "❤" +
    " §7| §e" + target.getFoodLevel() + "🍖"
));
```

**Avantaj**: Sürekli görünür, dürbünü bırakmadan bilgi alır
**Dezavantaj**: Çok fazla bilgi gösterilemez

---

### 2. **Title ile Önemli Bilgiler**

```java
// 3 saniye sonra Title ile önemli bilgi göster
player.sendTitle(
    Component.text("§c" + target.getName()),
    Component.text("§7Can: " + String.format("%.1f", target.getHealth())),
    10, 60, 10
);
```

**Avantaj**: Dikkat çekici
**Dezavantaj**: Çok fazla bilgi gösterilemez

---

## 💡 ÖNERİLER

### **Casusluk Dürbünü için:**

**Seçenek 1: GUI Menüsü (Önerilen)**
- 3 saniye bakınca GUI menüsü açılsın
- Butonlara tıklayınca detaylı bilgi gösterilsin
- Daha profesyonel görünüm

**Seçenek 2: ActionBar + GUI Kombinasyonu**
- ActionBar'da anlık bilgi (Can, Açlık)
- Shift+Sağ Tık ile detaylı GUI menüsü açılsın

**Seçenek 3: Mevcut Sistem (Chat)**
- Basit ve hızlı
- Ama görsel değil

---

## 🎮 ÇOK MODLU SİLAHLARDA DURUM

**Mevcut Sistem: ZATEN MÜKEMMEL! ✅**

- ✅ Modern GUI sistemi kullanılıyor
- ✅ Shift+Sağ Tık ile tetikleniyor
- ✅ Görsel butonlar var
- ✅ Tıklama ile mod değişiyor
- ✅ Ses efektleri var

**İyileştirme Önerileri:**
1. Menü boyutunu 9'dan 18'e çıkarabiliriz (daha büyük butonlar)
2. Animasyon eklenebilir (particle efektleri)
3. Mod açıklamaları daha detaylı olabilir

---

## 📝 SONUÇ

**Mevcut Sistem:**
- ✅ WeaponModeManager: Modern GUI sistemi kullanıyor
- ✅ ClanMenu: GUI menüsü var
- ⚠️ Casusluk Dürbünü: Chat mesajları kullanıyor (GUI eklenebilir)

**Öneri:**
Casusluk Dürbünü için GUI menüsü ekleyelim! WeaponModeManager'daki sistemi kullanarak profesyonel bir menü oluşturabiliriz.
