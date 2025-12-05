# 🎯 SAĞ ÜST BİLGİ BARİ (HUD) SİSTEMİ - ÖNERİLER

## 📊 MEVCUT SİSTEMDEKİ BİLGİLER

### ✅ Gösterilebilecek Bilgiler:

#### 1. **Felaket Sayacı** ⏰ (Zaten Var)
- Sonraki felaket seviyesi
- Kalan süre (dd/hh/mm/ss)
- **Öncelik:** Yüksek

#### 2. **Aktif Batarya Bilgisi** ⚡
- Hangi slotta hangi batarya yüklü
- Batarya seviyesi ve yakıt durumu
- **Öncelik:** Yüksek
- **Kaynak:** `NewBatteryManager.getLoadedBattery()`

#### 3. **Alışveriş Teklif Bildirimleri** 💰
- Bekleyen teklif sayısı
- Yeni teklif var mı? (son 30 saniye içinde)
- **Öncelik:** Orta
- **Kaynak:** `ShopManager.getOffers()`

#### 4. **Aktif Görev İlerlemesi** 📋
- Görev tipi ve hedef
- İlerleme (örn: 5/10 mob öldür)
- Kalan süre
- **Öncelik:** Orta
- **Kaynak:** `MissionManager.getPlayerMission()`

#### 5. **Aktif Kontratlar** 📜
- Kaç aktif kontrat var?
- Bounty kontratı var mı? (başında ödül)
- Kalan süre
- **Öncelik:** Orta
- **Kaynak:** `ContractManager.getPlayerContracts()`

#### 6. **Klan Bilgileri** 🏰
- Klan adı
- Klan seviyesi
- Klan bakiyesi (kısa)
- **Öncelik:** Düşük
- **Kaynak:** `ClanManager.getClanByPlayer()`

#### 7. **Aktif Buff'lar** ⚡
- Fatih Buff'ı var mı? (süre)
- Kahraman Buff'ı var mı? (süre)
- Diğer özel buff'lar
- **Öncelik:** Düşük
- **Kaynak:** `BuffManager.getConquerorBuff()`, `BuffManager.getHeroBuff()`

#### 8. **Kuşatma Durumu** ⚔️
- Aktif kuşatma var mı?
- Kuşatma süresi
- **Öncelik:** Düşük
- **Kaynak:** `SiegeManager.getActiveSieges()`

#### 9. **Bölge Bilgisi** 🗺️
- Hangi klanın bölgesindesin?
- Dost mu düşman mı?
- **Öncelik:** Düşük
- **Kaynak:** `TerritoryManager.getTerritoryOwner()`

---

## 🎨 ÖNERİLEN HUD YAPISI

### **Scoreboard Sidebar (Sağ Üst Köşe)**

```
╔═══════════════════╗
║  ⏰ FELAKET SAYACI ║  ← Başlık
╠═══════════════════╣
║                   ║
║ ⏰ Sonraki: Lv 2  ║  ← Felaket bilgisi
║ Kalan: 02:15:30   ║
║                   ║
║ ⚡ Batarya: Slot 3║  ← Aktif batarya
║ Yıldırım Asası    ║
║                   ║
║ 💰 Teklif: 2 yeni ║  ← Alışveriş teklifleri
║                   ║
║ 📋 Görev: 5/10   ║  ← Görev ilerlemesi
║ Mob Öldür         ║
║                   ║
║ 📜 Kontrat: 1    ║  ← Aktif kontratlar
║ Bounty: 500 altın║
║                   ║
║ ⚡ Buff: Fatih    ║  ← Aktif buff'lar
║ Süre: 12:30:00   ║
╚═══════════════════╝
```

### **Dinamik Gösterim Sistemi**

- **Sadece aktif olanlar gösterilir** (boş satırlar gösterilmez)
- **Öncelik sırasına göre** sıralanır
- **Maksimum 15 satır** (Scoreboard limiti)
- **Her saniye güncellenir**

---

## 🔧 TEKNİK YAPILANDIRMA

### **1. HUDManager Sınıfı Oluştur**

```java
public class HUDManager {
    private Scoreboard hudScoreboard;
    private Objective hudObjective;
    private BukkitTask updateTask;
    
    // Bilgi kaynakları
    private DisasterManager disasterManager;
    private NewBatteryManager batteryManager;
    private ShopManager shopManager;
    private MissionManager missionManager;
    private ContractManager contractManager;
    private BuffManager buffManager;
    private ClanManager clanManager;
    private TerritoryManager territoryManager;
    
    // Oyuncu bazlı bilgi takibi
    private Map<UUID, Long> lastShopOfferTime; // Son teklif zamanı
}
```

### **2. Bilgi Toplama Metodları**

```java
// Her oyuncu için bilgileri topla
private List<HUDLine> getPlayerHUDInfo(Player player) {
    List<HUDLine> lines = new ArrayList<>();
    
    // 1. Felaket sayacı (her zaman göster)
    lines.add(getDisasterCountdown());
    
    // 2. Aktif batarya (varsa)
    HUDLine battery = getBatteryInfo(player);
    if (battery != null) lines.add(battery);
    
    // 3. Alışveriş teklifleri (varsa)
    HUDLine shop = getShopOfferInfo(player);
    if (shop != null) lines.add(shop);
    
    // 4. Görev (varsa)
    HUDLine mission = getMissionInfo(player);
    if (mission != null) lines.add(mission);
    
    // 5. Kontratlar (varsa)
    HUDLine contract = getContractInfo(player);
    if (contract != null) lines.add(contract);
    
    // 6. Buff'lar (varsa)
    HUDLine buff = getBuffInfo(player);
    if (buff != null) lines.add(buff);
    
    return lines;
}
```

### **3. Scoreboard Güncelleme**

```java
private void updateHUD(Player player) {
    List<HUDLine> lines = getPlayerHUDInfo(player);
    
    // Tüm entry'leri temizle
    for (String entry : hudScoreboard.getEntries()) {
        hudScoreboard.resetScores(entry);
    }
    
    // Yeni bilgileri ekle (yukarıdan aşağıya)
    int score = lines.size();
    for (HUDLine line : lines) {
        Team team = hudScoreboard.getTeam("team" + score);
        if (team == null) {
            team = hudScoreboard.registerNewTeam("team" + score);
        }
        team.setPrefix(line.getText());
        team.addEntry(getUniqueEntry(score));
        hudObjective.getScore(getUniqueEntry(score)).setScore(score);
        score--;
    }
    
    player.setScoreboard(hudScoreboard);
}
```

---

## 📋 ÖNERİLEN BİLGİ ÖNCELİKLERİ

### **Yüksek Öncelik (Her Zaman Göster)**
1. ⏰ **Felaket Sayacı** - Sürekli görünür
2. ⚡ **Aktif Batarya** - Yüklü batarya varsa göster

### **Orta Öncelik (Varsa Göster)**
3. 💰 **Alışveriş Teklifleri** - Yeni teklif varsa (son 30 saniye)
4. 📋 **Aktif Görev** - Görev varsa
5. 📜 **Aktif Kontratlar** - Kontrat varsa

### **Düşük Öncelik (Sadece Önemli Durumlarda)**
6. ⚡ **Aktif Buff'lar** - Özel buff varsa (Fatih, Kahraman)
7. ⚔️ **Kuşatma Durumu** - Aktif kuşatma varsa
8. 🗺️ **Bölge Bilgisi** - Düşman bölgesindeyse uyarı

---

## 🎯 ÖNERİLEN GÖSTERİM FORMATLARI

### **1. Felaket Sayacı**
```
⏰ Sonraki: Seviye 2
Kalan: 02:15:30
```

### **2. Aktif Batarya**
```
⚡ Batarya: Slot 3
Yıldırım Asası
```

### **3. Alışveriş Teklifleri**
```
💰 Teklif: 2 yeni
/shop offers
```

### **4. Görev İlerlemesi**
```
📋 Görev: 5/10
Mob Öldür
```

### **5. Kontratlar**
```
📜 Kontrat: 1 aktif
Bounty: 500 altın
```

### **6. Buff'lar**
```
⚡ Buff: Fatih
Süre: 12:30:00
```

---

## 💡 EK ÖNERİLER

### **1. Bildirim Sistemi**
- Yeni teklif geldiğinde **geçici olarak vurgula** (renk değiştir)
- Bounty kontratı varsa **kırmızı renkte göster**
- Görev tamamlanmak üzereyse **sarı renkte göster**

### **2. Tıklanabilir Bilgiler (Gelecek)**
- Scoreboard'a tıklayınca ilgili menüyü aç (örn: görev menüsü)
- **Not:** Scoreboard tıklama desteği yok, ama ActionBar ile kombinasyon yapılabilir

### **3. Özelleştirilebilir HUD**
- Oyuncular hangi bilgileri görmek istediğini seçebilir
- `/hud toggle <bilgi_tipi>` komutu

### **4. Performans Optimizasyonu**
- Her oyuncu için ayrı scoreboard (gerekirse)
- Cache kullanımı (1 saniye güncelleme)
- Sadece aktif bilgileri göster (boş satırlar yok)

---

## ✅ UYGULAMA ADIMLARI

1. **HUDManager sınıfı oluştur**
2. **DisasterManager'dan countdown bilgisini al**
3. **NewBatteryManager'dan aktif batarya bilgisini al**
4. **ShopManager'dan teklif bilgisini al**
5. **MissionManager'dan görev bilgisini al**
6. **ContractManager'dan kontrat bilgisini al**
7. **BuffManager'dan buff bilgisini al**
8. **Scoreboard'u oluştur ve güncelle**
9. **Her oyuncuya özel HUD göster**
10. **Main.java'da entegre et**

---

## 🎨 GÖRSEL ÖRNEK

```
╔═══════════════════════╗
║   ⏰ FELAKET SAYACI    ║
╠═══════════════════════╣
║                       ║
║  ⏰ Sonraki: Seviye 2 ║
║  Kalan: 02:15:30      ║
║                       ║
║  ⚡ Batarya: Slot 3   ║
║  Yıldırım Asası       ║
║                       ║
║  💰 Teklif: 2 yeni    ║
║  /shop offers         ║
║                       ║
║  📋 Görev: 5/10      ║
║  Mob Öldür            ║
║                       ║
║  📜 Kontrat: 1        ║
║  Bounty: 500 altın    ║
╚═══════════════════════╝
```

---

**Sonuç:** Bu sistem oyunculara tüm önemli bilgileri tek bir yerde, sürekli görünür şekilde sunacak! 🎉

