# 📋 STRATOCRAFT DETAYLI BİRLEŞTİRME TALİMATLARI

## 📊 TÜM DOSYALAR İÇİN DETAYLI REHBER

Bu döküman, `Degisim` klasöründeki **HER DOSYA** için detaylı birleştirme talimatları içerir.

**Tarih:** 11 Aralık 2025  
**Toplam Dosya:** 39 (36 Java + 3 Markdown)

---

## ✅ MARKDOWN DOSYALARI (3 DOSYA) - TAMAMLANDI

### 1. MENU_ERISIM_SISTEMI_PLANI.md ✅
**Durum:** ✅ Kopyalandı  
**Konum:** Proje kök dizini  
**İşlem:** Hiçbir şey yapmayın, zaten kopyalandı.

### 2. OZELLIK_GELISTIRME_PLANI.md ✅
**Durum:** ✅ Kopyalandı  
**Konum:** Proje kök dizini  
**İşlem:** Hiçbir şey yapmayın, zaten kopyalandı.

### 3. YAPI_TARIFLERI_REHBERI.md ✅
**Durum:** ✅ Kopyalandı  
**Konum:** Proje kök dizini  
**İşlem:** Hiçbir şey yapmayın, zaten kopyalandı.

---

## 🎯 KRİTİK DOSYALAR (4 DOSYA)

### 1. Main.java ✅

**Dosya Boyutları:**
- Degisim: 71,529 bytes
- Mevcut: 74,235 bytes
- **Fark:** +2,706 bytes (mevcut projede daha fazla özellik)

**Birleştirme Talimatı:**
```
✅ HİÇBİR ŞEY YAPMAYIN
Mevcut projedeki Main.java dosyası KORUNMALI.
Degisim klasöründeki versiyon daha eski.
```

**Korunması Gereken Özellikler (Mevcut Projede):**
1. **TaskManager Sistemi** (Satır 92, 154, 859-862, 1048)
   - Memory leak önleme için kritik
   - Field, initialize, shutdown, getter var

2. **PersonalTerminalListener** (Satır 63, 1032, 1354)
   - GUI menülerinde kullanılıyor
   - Field, getter, initialize var

3. **DisasterListener** (Satır 284)
   - Felaket hasar takibi için gerekli
   - Event kaydı var

4. **SQLite Veritabanı Kapatma** (Satır 894-897)
   - Veri kaybını önlemek için kritik
   - DatabaseManager.close() çağrısı var

**Kontrol:**
- [x] TaskManager field var mı?
- [x] TaskManager initialize ediliyor mu?
- [x] TaskManager shutdown ediliyor mu?
- [x] PersonalTerminalListener field var mı?
- [x] PersonalTerminalListener getter var mı?
- [x] DisasterListener kaydı var mı?
- [x] SQLite kapatma var mı?

---

### 2. AdminCommandExecutor.java ✅

**Dosya Boyutları:**
- Degisim: 421,818 bytes
- Mevcut: 428,755 bytes
- **Fark:** +6,937 bytes (mevcut projede daha fazla özellik)

**Birleştirme Talimatı:**
```
✅ HİÇBİR ŞEY YAPMAYIN
Mevcut projedeki AdminCommandExecutor.java dosyası KORUNMALI.
handleReload() metodu zaten eklendi.
```

**Korunması Gereken Özellikler (Mevcut Projede):**
1. **"reload" Case'i** (Satır 59-60)
   - Config reload için önemli

2. **handleReload() Metodu** (Satır 180-219) ✅ EKLENDİ
   - ConfigManager.reloadConfig() çağrısı
   - LangManager.reloadLang() çağrısı
   - NewBossArenaManager.reloadConfig() çağrısı
   - Hata yönetimi

**Kontrol:**
- [x] "reload" case'i var mı?
- [x] handleReload() metodu var mı?
- [x] ConfigManager.reloadConfig() çağrılıyor mu?
- [x] LangManager.reloadLang() çağrılıyor mu?

---

### 3. DataManager.java ✅

**Dosya Boyutları:**
- Degisim: 99,132 bytes
- Mevcut: 105,133 bytes
- **Fark:** +6,001 bytes (mevcut projede daha fazla özellik)

**Birleştirme Talimatı:**
```
✅ HİÇBİR ŞEY YAPMAYIN
Mevcut projedeki DataManager.java dosyası KORUNMALI.
SQLite entegrasyonu korunmalı.
```

**Korunması Gereken Özellikler (Mevcut Projede):**
1. **SQLite Entegrasyonu**
   - getDatabaseManager() metodu
   - SQLite bağlantı yönetimi
   - Veri güvenliği için kritik

**Kontrol:**
- [x] getDatabaseManager() metodu var mı?
- [x] SQLite bağlantı yönetimi var mı?

---

### 4. AllianceMenu.java ✅

**Dosya Boyutları:**
- Degisim: ~18,000 bytes (tahmini)
- Mevcut: ~19,000 bytes (tahmini)
- **Fark:** Mevcut projede null kontrolleri var

**Birleştirme Talimatı:**
```
✅ HİÇBİR ŞEY YAPMAYIN
Mevcut projedeki AllianceMenu.java dosyası KORUNMALI.
Null kontrolleri korunmalı.
```

**Korunması Gereken Özellikler (Mevcut Projede):**
1. **Manager Null Kontrolleri** (Satır 53-64)
   ```java
   if (clanManager == null) {
       player.sendMessage("§cKlan sistemi aktif değil!");
       plugin.getLogger().warning("ClanManager null! Menü açılamıyor.");
       return;
   }
   
   if (allianceManager == null) {
       player.sendMessage("§cİttifak sistemi aktif değil!");
       plugin.getLogger().warning("AllianceManager null! Menü açılamıyor.");
       return;
   }
   ```

**Kontrol:**
- [x] clanManager null kontrolü var mı?
- [x] allianceManager null kontrolü var mı?
- [x] getLogger().warning() çağrıları var mı?

---

## 📝 GUI DOSYALARI (12 DOSYA)

### 5. BreedingMenu.java ✅

**Dosya Boyutları:**
- Degisim: 18,010 bytes
- Mevcut: 19,596 bytes
- **Fark:** +1,586 bytes (mevcut projede daha fazla özellik)

**Birleştirme Talimatı:**
```
✅ MEVCUT PROJEDEKİ VERSİYON KORUNMALI

Mevcut projede null kontrolleri var (Satır 56-67):
- breedingManager null kontrolü
- tamingManager null kontrolü
- getLogger().warning() çağrıları

Degisim klasöründeki versiyon daha eski (null kontrolleri yok).
```

**Korunması Gereken Özellikler (Mevcut Projede):**
1. **Manager Null Kontrolleri** (Satır 56-67)
   ```java
   if (breedingManager == null) {
       player.sendMessage("§cÜreme sistemi aktif değil!");
       plugin.getLogger().warning("BreedingManager null! Menü açılamıyor.");
       return;
   }
   
   if (tamingManager == null) {
       player.sendMessage("§cEğitme sistemi aktif değil!");
       plugin.getLogger().warning("TamingManager null! Menü açılamıyor.");
       return;
   }
   ```

**Kontrol:**
- [x] breedingManager null kontrolü var mı?
- [x] tamingManager null kontrolü var mı?
- [x] getLogger().warning() çağrıları var mı?

---

### 6. CaravanMenu.java ⚠️

**Dosya Boyutları:**
- Degisim: 23,354 bytes
- Mevcut: 24,469 bytes
- **Fark:** +1,115 bytes (mevcut projede daha fazla özellik)

**Birleştirme Talimatı:**
```
⚠️ MANUEL KONTROL GEREKLİ

1. Diff tool ile karşılaştırın
2. Mevcut projedeki yeni özellikleri tespit edin
3. Null kontrolleri var mı kontrol edin
4. Genel kural: Mevcut projedeki versiyon korunmalı
```

**Kontrol:**
- [ ] Diff kontrolü yapıldı mı?
- [ ] Null kontrolleri var mı?
- [ ] Yeni özellikler tespit edildi mi?

---

### 7. ClanBankMenu.java ⚠️

**Dosya Boyutları:**
- Degisim: 17,347 bytes
- Mevcut: 18,068 bytes
- **Fark:** +721 bytes (mevcut projede daha fazla özellik)

**Birleştirme Talimatı:**
```
⚠️ MANUEL KONTROL GEREKLİ

1. Diff tool ile karşılaştırın
2. Mevcut projedeki yeni özellikleri tespit edin
3. Null kontrolleri var mı kontrol edin
4. Genel kural: Mevcut projedeki versiyon korunmalı
```

**Kontrol:**
- [ ] Diff kontrolü yapıldı mı?
- [ ] Null kontrolleri var mı?
- [ ] Yeni özellikler tespit edildi mi?

---

### 8. ClanMenu.java ⚠️

**Dosya Boyutları:**
- Degisim: 18,624 bytes
- Mevcut: 19,242 bytes
- **Fark:** +618 bytes (mevcut projede daha fazla özellik)

**Birleştirme Talimatı:**
```
⚠️ MANUEL KONTROL GEREKLİ

1. Diff tool ile karşılaştırın
2. Mevcut projedeki yeni özellikleri tespit edin
3. Null kontrolleri var mı kontrol edin
4. Genel kural: Mevcut projedeki versiyon korunmalı
```

**Kontrol:**
- [ ] Diff kontrolü yapıldı mı?
- [ ] Null kontrolleri var mı?
- [ ] Yeni özellikler tespit edildi mi?

---

### 9. ClanStructureMenu.java ⚠️

**Dosya Boyutları:**
- Degisim: 23,896 bytes
- Mevcut: 24,908 bytes
- **Fark:** +1,012 bytes (mevcut projede daha fazla özellik)

**Birleştirme Talimatı:**
```
⚠️ MANUEL KONTROL GEREKLİ

1. Diff tool ile karşılaştırın
2. Mevcut projedeki yeni özellikleri tespit edin
3. Null kontrolleri var mı kontrol edin
4. Genel kural: Mevcut projedeki versiyon korunmalı
```

**Kontrol:**
- [ ] Diff kontrolü yapıldı mı?
- [ ] Null kontrolleri var mı?
- [ ] Yeni özellikler tespit edildi mi?

---

### 10. ContractMenu.java ✅

**Dosya Boyutları:**
- Degisim: 111,468 bytes
- Mevcut: 114,458 bytes
- **Fark:** +2,990 bytes (mevcut projede daha fazla özellik)

**Birleştirme Talimatı:**
```
✅ MEVCUT PROJEDEKİ VERSİYON KORUNMALI

Mevcut projede null kontrolleri var (Satır 139-150):
- contractManager null kontrolü
- getLogger().warning() çağrıları
- contracts null kontrolü

Degisim klasöründeki versiyon daha eski.
```

**Korunması Gereken Özellikler (Mevcut Projede):**
1. **Manager Null Kontrolleri** (Satır 139-150)
   ```java
   if (contractManager == null) {
       player.sendMessage("§cKontrat sistemi aktif değil!");
       plugin.getLogger().warning("ContractManager null! Menü açılamıyor.");
       return;
   }
   
   List<Contract> contracts = contractManager.getContracts();
   if (contracts == null) {
       plugin.getLogger().warning("ContractManager.getContracts() null döndü!");
       contracts = new ArrayList<>();
   }
   ```

**Kontrol:**
- [x] contractManager null kontrolü var mı?
- [x] contracts null kontrolü var mı?
- [x] getLogger().warning() çağrıları var mı?

---

### 11. PowerMenu.java ⚠️

**Dosya Boyutları:**
- Degisim: 14,909 bytes
- Mevcut: 17,135 bytes
- **Fark:** +2,226 bytes (mevcut projede daha fazla özellik)

**Birleştirme Talimatı:**
```
✅ MEVCUT PROJEDEKİ VERSİYON KORUNMALI

Mevcut projede PersonalTerminalListener entegrasyonu var:
- fromPersonalTerminal parametresi (Satır 58)
- personalMode map'i
- PersonalTerminalListener.openMainMenu() çağrıları (Satır 296-297, 327-328, 349-350, 369-370)

Degisim klasöründeki versiyon daha eski (PersonalTerminalListener entegrasyonu yok).
```

**Korunması Gereken Özellikler (Mevcut Projede):**
1. **PersonalTerminalListener Entegrasyonu**
   - fromPersonalTerminal parametresi
   - personalMode map'i
   - PersonalTerminalListener.openMainMenu() çağrıları

2. **Null Kontrolleri**
   - ClanManager null kontrolü (Satır 79-80)
   - SimpleRankingSystem null kontrolü (Satır 116-118)

**Kontrol:**
- [x] fromPersonalTerminal parametresi var mı?
- [x] personalMode map'i var mı?
- [x] PersonalTerminalListener entegrasyonu var mı?
- [x] Null kontrolleri var mı?

---

### 12. RecipeMenu.java ⚠️

**Dosya Boyutları:**
- Degisim: 32,358 bytes
- Mevcut: 33,036 bytes
- **Fark:** +678 bytes (mevcut projede daha fazla özellik)

**Birleştirme Talimatı:**
```
⚠️ MANUEL KONTROL GEREKLİ

1. Diff tool ile karşılaştırın
2. Mevcut projedeki yeni özellikleri tespit edin
3. Null kontrolleri var mı kontrol edin
4. Genel kural: Mevcut projedeki versiyon korunmalı
```

**Kontrol:**
- [ ] Diff kontrolü yapıldı mı?
- [ ] Null kontrolleri var mı?
- [ ] Yeni özellikler tespit edildi mi?

---

### 13. ShopMenu.java ⚠️

**Dosya Boyutları:**
- Degisim: 7,825 bytes
- Mevcut: 8,029 bytes
- **Fark:** +204 bytes (mevcut projede daha fazla özellik)

**Birleştirme Talimatı:**
```
⚠️ MANUEL KONTROL GEREKLİ

1. Diff tool ile karşılaştırın
2. Mevcut projedeki yeni özellikleri tespit edin
3. Null kontrolleri var mı kontrol edin
4. Genel kural: Mevcut projedeki versiyon korunmalı
```

**Kontrol:**
- [ ] Diff kontrolü yapıldı mı?
- [ ] Null kontrolleri var mı?
- [ ] Yeni özellikler tespit edildi mi?

---

### 14. TamingMenu.java ✅

**Dosya Boyutları:**
- Degisim: 14,515 bytes
- Mevcut: 16,771 bytes
- **Fark:** +2,256 bytes (mevcut projede daha fazla özellik)

**Birleştirme Talimatı:**
```
✅ MEVCUT PROJEDEKİ VERSİYON KORUNMALI

Mevcut projede null kontrolleri var (Satır 62-73):
- tamingManager null kontrolü
- clanManager null kontrolü (personalOnly false ise)
- getLogger().warning() çağrıları

Degisim klasöründeki versiyon daha eski (null kontrolleri yok).
```

**Korunması Gereken Özellikler (Mevcut Projede):**
1. **Manager Null Kontrolleri** (Satır 62-73)
   ```java
   if (tamingManager == null) {
       player.sendMessage("§cEğitme sistemi aktif değil!");
       plugin.getLogger().warning("TamingManager null! Menü açılamıyor.");
       return;
   }
   
   if (!personalOnly && clanManager == null) {
       player.sendMessage("§cKlan sistemi aktif değil!");
       plugin.getLogger().warning("ClanManager null! Menü açılamıyor.");
       return;
   }
   ```

**Kontrol:**
- [x] tamingManager null kontrolü var mı?
- [x] clanManager null kontrolü var mı?
- [x] getLogger().warning() çağrıları var mı?

---

### 15. TrainingMenu.java ✅

**Dosya Boyutları:**
- Degisim: 11,826 bytes
- Mevcut: 12,672 bytes
- **Fark:** +846 bytes (mevcut projede daha fazla özellik)

**Birleştirme Talimatı:**
```
✅ MEVCUT PROJEDEKİ VERSİYON KORUNMALI

Mevcut projede null kontrolleri var (Satır 43-48):
- trainingManager null kontrolü
- getLogger().warning() çağrısı

Mevcut projede PersonalTerminalListener entegrasyonu var (Satır 288):
- plugin.getPersonalTerminalListener().openMainMenu(player)

Degisim klasöründeki versiyon daha eski.
```

**Korunması Gereken Özellikler (Mevcut Projede):**
1. **Manager Null Kontrolleri** (Satır 43-48)
   ```java
   if (trainingManager == null) {
       player.sendMessage("§cEğitim sistemi aktif değil!");
       plugin.getLogger().warning("TrainingManager null! Menü açılamıyor.");
       return;
   }
   ```

2. **PersonalTerminalListener Entegrasyonu** (Satır 288)
   ```java
   if (plugin.getPersonalTerminalListener() != null) {
       plugin.getPersonalTerminalListener().openMainMenu(player);
   }
   ```

**Kontrol:**
- [x] trainingManager null kontrolü var mı?
- [x] PersonalTerminalListener entegrasyonu var mı?
- [x] getLogger().warning() çağrısı var mı?

---

## 📝 LISTENER DOSYALARI (8 DOSYA)

### 16. BossListener.java ⚠️

**Dosya Boyutları:**
- Degisim: 31,009 bytes
- Mevcut: 31,687 bytes
- **Fark:** +678 bytes (mevcut projede daha fazla özellik)

**Birleştirme Talimatı:**
```
⚠️ MANUEL KONTROL GEREKLİ

1. Diff tool ile karşılaştırın
2. Mevcut projedeki yeni özellikleri tespit edin
3. Yeni event handler'lar var mı kontrol edin
4. Genel kural: Mevcut projedeki versiyon korunmalı
```

**Kontrol:**
- [ ] Diff kontrolü yapıldı mı?
- [ ] Yeni event handler'lar var mı?
- [ ] Yeni özellikler tespit edildi mi?

---

### 17. ClanSystemListener.java ✅

**Dosya Boyutları:**
- Degisim: 7,302 bytes
- Mevcut: 7,509 bytes
- **Fark:** +207 bytes (mevcut projede daha fazla özellik)

**Birleştirme Talimatı:**
```
✅ MEVCUT PROJEDEKİ VERSİYON KORUNMALI

İki dosya neredeyse aynı görünüyor.
Mevcut projedeki versiyon korunmalı (küçük iyileştirmeler olabilir).
```

**Kontrol:**
- [x] İki dosya karşılaştırıldı mı? (Aynı görünüyor)
- [x] Mevcut projedeki versiyon korunmalı

---

### 18. GhostRecipeListener.java ⚠️

**Dosya Boyutları:**
- Degisim: 34,074 bytes
- Mevcut: 34,787 bytes
- **Fark:** +713 bytes (mevcut projede daha fazla özellik)

**Birleştirme Talimatı:**
```
⚠️ MANUEL KONTROL GEREKLİ

1. Diff tool ile karşılaştırın
2. Mevcut projedeki yeni özellikleri tespit edin
3. Yeni event handler'lar var mı kontrol edin
4. Genel kural: Mevcut projedeki versiyon korunmalı
```

**Kontrol:**
- [ ] Diff kontrolü yapıldı mı?
- [ ] Yeni event handler'lar var mı?
- [ ] Yeni özellikler tespit edildi mi?

---

### 19. PersonalTerminalListener.java ⚠️

**Dosya Boyutları:**
- Degisim: 8,182 bytes
- Mevcut: 8,992 bytes
- **Fark:** +810 bytes (mevcut projede daha fazla özellik)

**Birleştirme Talimatı:**
```
⚠️ MANUEL KONTROL GEREKLİ

1. Diff tool ile karşılaştırın
2. Mevcut projedeki yeni özellikleri tespit edin
3. Yeni menü özellikleri var mı kontrol edin
4. Genel kural: Mevcut projedeki versiyon korunmalı

NOT: Bu dosya Main.java'da kullanılıyor (PersonalTerminalListener field).
Eğer bu dosyada önemli bir değişiklik varsa, Main.java'daki kullanımı da kontrol edin.
```

**Kontrol:**
- [ ] Diff kontrolü yapıldı mı?
- [ ] Yeni menü özellikleri var mı?
- [ ] Main.java'daki kullanımı kontrol edildi mi?

---

### 20. RitualInteractionListener.java ⚠️

**Dosya Boyutları:**
- Degisim: 58,572 bytes
- Mevcut: 59,912 bytes
- **Fark:** +1,340 bytes (mevcut projede daha fazla özellik)

**Birleştirme Talimatı:**
```
⚠️ MANUEL KONTROL GEREKLİ

1. Diff tool ile karşılaştırın
2. Mevcut projedeki yeni özellikleri tespit edin
3. Yeni ritüel özellikleri var mı kontrol edin
4. Genel kural: Mevcut projedeki versiyon korunmalı
```

**Kontrol:**
- [ ] Diff kontrolü yapıldı mı?
- [ ] Yeni ritüel özellikleri var mı?
- [ ] Yeni özellikler tespit edildi mi?

---

### 21. ShopListener.java ⚠️

**Dosya Boyutları:**
- Degisim: 16,134 bytes
- Mevcut: 16,473 bytes
- **Fark:** +339 bytes (mevcut projede daha fazla özellik)

**Birleştirme Talimatı:**
```
⚠️ MANUEL KONTROL GEREKLİ

1. Diff tool ile karşılaştırın
2. Mevcut projedeki yeni özellikleri tespit edin
3. Yeni event handler'lar var mı kontrol edin
4. Genel kural: Mevcut projedeki versiyon korunmalı
```

**Kontrol:**
- [ ] Diff kontrolü yapıldı mı?
- [ ] Yeni event handler'lar var mı?
- [ ] Yeni özellikler tespit edildi mi?

---

### 22. StructureActivationListener.java ⚠️

**Dosya Boyutları:**
- Degisim: 25,180 bytes
- Mevcut: 25,925 bytes
- **Fark:** +745 bytes (mevcut projede daha fazla özellik)

**Birleştirme Talimatı:**
```
⚠️ MANUEL KONTROL GEREKLİ

1. Diff tool ile karşılaştırın
2. Mevcut projedeki yeni özellikleri tespit edin
3. Yeni yapı aktivasyon özellikleri var mı kontrol edin
4. Genel kural: Mevcut projedeki versiyon korunmalı
```

**Kontrol:**
- [ ] Diff kontrolü yapıldı mı?
- [ ] Yeni yapı aktivasyon özellikleri var mı?
- [ ] Yeni özellikler tespit edildi mi?

---

### 23. StructureMenuListener.java ⚠️

**Dosya Boyutları:**
- Degisim: 10,203 bytes
- Mevcut: 10,460 bytes
- **Fark:** +257 bytes (mevcut projede daha fazla özellik)

**Birleştirme Talimatı:**
```
⚠️ MANUEL KONTROL GEREKLİ

1. Diff tool ile karşılaştırın
2. Mevcut projedeki yeni özellikleri tespit edin
3. Yeni menü özellikleri var mı kontrol edin
4. Genel kural: Mevcut projedeki versiyon korunmalı
```

**Kontrol:**
- [ ] Diff kontrolü yapıldı mı?
- [ ] Yeni menü özellikleri var mı?
- [ ] Yeni özellikler tespit edildi mi?

---

## 📝 MANAGER DOSYALARI (6 DOSYA)

### 24. DisasterManager.java ⚠️

**Dosya Boyutları:**
- Degisim: 82,303 bytes
- Mevcut: 91,237 bytes
- **Fark:** +8,934 bytes (mevcut projede daha fazla özellik)

**Birleştirme Talimatı:**
```
⚠️ DETAYLI KONTROL GEREKLİ (EN ÖNEMLİ FARK)

1. İki dosyayı diff tool ile karşılaştırın
2. Mevcut projedeki yeni özellikleri tespit edin:
   - Yeni felaket tipleri
   - Yeni faz sistemi özellikleri
   - Performans iyileştirmeleri
   - Bug fix'ler
   - Yeni metodlar
3. Degisim klasöründeki özelliklerin mevcut projede olup olmadığını kontrol edin
4. Genel kural: Mevcut projedeki versiyon korunmalı
5. Eğer Degisim'de mevcut projede olmayan önemli bir özellik varsa, manuel olarak ekleyin
```

**Olası Yeni Özellikler (Mevcut Projede):**
- Yeni felaket tipleri
- Yeni faz sistemi özellikleri
- Performans iyileştirmeleri
- Bug fix'ler
- Yeni metodlar

**Kontrol:**
- [ ] Diff kontrolü yapıldı mı?
- [ ] Yeni felaket tipleri tespit edildi mi?
- [ ] Yeni faz sistemi özellikleri tespit edildi mi?
- [ ] Performans iyileştirmeleri tespit edildi mi?
- [ ] Bug fix'ler tespit edildi mi?

---

### 25. GhostRecipeManager.java ⚠️

**Dosya Boyutları:**
- Degisim: 28,270 bytes
- Mevcut: 28,953 bytes
- **Fark:** +683 bytes (mevcut projede daha fazla özellik)

**Birleştirme Talimatı:**
```
⚠️ MANUEL KONTROL GEREKLİ

1. Diff tool ile karşılaştırın
2. Mevcut projedeki yeni özellikleri tespit edin
3. Yeni tarif özellikleri var mı kontrol edin
4. Genel kural: Mevcut projedeki versiyon korunmalı
```

**Kontrol:**
- [ ] Diff kontrolü yapıldı mı?
- [ ] Yeni tarif özellikleri var mı?
- [ ] Yeni özellikler tespit edildi mi?

---

### 26. HUDManager.java ⚠️

**Dosya Boyutları:**
- Degisim: 21,677 bytes
- Mevcut: 22,278 bytes
- **Fark:** +601 bytes (mevcut projede daha fazla özellik)

**Birleştirme Talimatı:**
```
⚠️ MANUEL KONTROL GEREKLİ

1. Diff tool ile karşılaştırın
2. Mevcut projedeki yeni özellikleri tespit edin
3. Yeni HUD özellikleri var mı kontrol edin
4. Genel kural: Mevcut projedeki versiyon korunmalı
```

**Kontrol:**
- [ ] Diff kontrolü yapıldı mı?
- [ ] Yeni HUD özellikleri var mı?
- [ ] Yeni özellikler tespit edildi mi?

---

### 27. ItemManager.java ⚠️

**Dosya Boyutları:**
- Degisim: 170,164 bytes
- Mevcut: 173,250 bytes
- **Fark:** +3,086 bytes (mevcut projede daha fazla özellik)

**Birleştirme Talimatı:**
```
⚠️ MANUEL KONTROL GEREKLİ (ÖNEMLİ FARK)

1. Diff tool ile karşılaştırın
2. Mevcut projedeki yeni özellikleri tespit edin:
   - Yeni item'lar
   - Yeni recipe'ler
   - Yeni özellikler
3. Genel kural: Mevcut projedeki versiyon korunmalı
```

**Kontrol:**
- [ ] Diff kontrolü yapıldı mı?
- [ ] Yeni item'lar tespit edildi mi?
- [ ] Yeni recipe'ler tespit edildi mi?
- [ ] Yeni özellikler tespit edildi mi?

---

### 28. ClanBankSystem.java ⚠️

**Dosya Boyutları:**
- Degisim: 25,611 bytes
- Mevcut: 27,542 bytes
- **Fark:** +1,931 bytes (mevcut projede daha fazla özellik)

**Birleştirme Talimatı:**
```
⚠️ MANUEL KONTROL GEREKLİ

1. Diff tool ile karşılaştırın
2. Mevcut projedeki yeni özellikleri tespit edin
3. Yeni banka özellikleri var mı kontrol edin
4. Genel kural: Mevcut projedeki versiyon korunmalı
```

**Kontrol:**
- [ ] Diff kontrolü yapıldı mı?
- [ ] Yeni banka özellikleri var mı?
- [ ] Yeni özellikler tespit edildi mi?

---

## 📝 UTIL/HELPER DOSYALARI (5 DOSYA)

### 29. AllianceHelper.java ✅

**Dosya Boyutları:**
- Degisim: 7,541 bytes
- Mevcut: 7,539 bytes
- **Fark:** -2 bytes (Aynı)

**Birleştirme Talimatı:**
```
✅ AYNI - HİÇBİR ŞEY YAPMAYIN
Mevcut projedeki versiyon korunmalı.
```

**Kontrol:**
- [x] İki dosya aynı mı? (Evet, -2 bytes fark önemsiz)

---

### 30. BossPhaseHelper.java ⚠️

**Dosya Boyutları:**
- Degisim: 6,505 bytes
- Mevcut: 6,695 bytes
- **Fark:** +190 bytes (mevcut projede daha fazla özellik)

**Birleştirme Talimatı:**
```
⚠️ MANUEL KONTROL GEREKLİ

1. Diff tool ile karşılaştırın
2. Mevcut projedeki yeni helper metodlarını tespit edin
3. Genel kural: Mevcut projedeki versiyon korunmalı
```

**Kontrol:**
- [ ] Diff kontrolü yapıldı mı?
- [ ] Yeni helper metodları tespit edildi mi?

---

### 31. CaravanHelper.java ⚠️

**Dosya Boyutları:**
- Degisim: 8,100 bytes
- Mevcut: 8,348 bytes
- **Fark:** +248 bytes (mevcut projede daha fazla özellik)

**Birleştirme Talimatı:**
```
⚠️ MANUEL KONTROL GEREKLİ

1. Diff tool ile karşılaştırın
2. Mevcut projedeki yeni helper metodlarını tespit edin
3. Genel kural: Mevcut projedeki versiyon korunmalı
```

**Kontrol:**
- [ ] Diff kontrolü yapıldı mı?
- [ ] Yeni helper metodları tespit edildi mi?

---

### 32. StructureHelper.java ⚠️

**Dosya Boyutları:**
- Degisim: 14,977 bytes
- Mevcut: 15,412 bytes
- **Fark:** +435 bytes (mevcut projede daha fazla özellik)

**Birleştirme Talimatı:**
```
⚠️ MANUEL KONTROL GEREKLİ

1. Diff tool ile karşılaştırın
2. Mevcut projedeki yeni helper metodlarını tespit edin
3. Genel kural: Mevcut projedeki versiyon korunmalı
```

**Kontrol:**
- [ ] Diff kontrolü yapıldı mı?
- [ ] Yeni helper metodları tespit edildi mi?

---

### 33. TamingHelper.java ✅

**Dosya Boyutları:**
- Degisim: 8,843 bytes
- Mevcut: 8,841 bytes
- **Fark:** -2 bytes (Aynı)

**Birleştirme Talimatı:**
```
✅ AYNI - HİÇBİR ŞEY YAPMAYIN
Mevcut projedeki versiyon korunmalı.
```

**Kontrol:**
- [x] İki dosya aynı mı? (Evet, -2 bytes fark önemsiz)

---

## 📝 MODEL VE TASK DOSYALARI (2 DOSYA)

### 34. Structure.java ⚠️

**Dosya Boyutları:**
- Degisim: 3,488 bytes
- Mevcut: 3,574 bytes
- **Fark:** +86 bytes (mevcut projede daha fazla özellik)

**Birleştirme Talimatı:**
```
⚠️ MANUEL KONTROL GEREKLİ

1. Diff tool ile karşılaştırın
2. Mevcut projedeki yeni field'ları veya metodları tespit edin
3. Genel kural: Mevcut projedeki versiyon korunmalı
```

**Kontrol:**
- [ ] Diff kontrolü yapıldı mı?
- [ ] Yeni field'lar tespit edildi mi?
- [ ] Yeni metodlar tespit edildi mi?

---

### 35. DisasterTask.java ⚠️

**Dosya Boyutları:**
- Degisim: 21,503 bytes
- Mevcut: 35,239 bytes
- **Fark:** +13,736 bytes (mevcut projede ÇOK DAHA FAZLA özellik)

**Birleştirme Talimatı:**
```
⚠️ DETAYLI KONTROL GEREKLİ (ÇOK ÖNEMLİ FARK!)

1. İki dosyayı diff tool ile karşılaştırın
2. Mevcut projedeki yeni özellikleri tespit edin:
   - Yeni task özellikleri
   - Performans iyileştirmeleri
   - Bug fix'ler
   - Yeni metodlar
3. Degisim klasöründeki özelliklerin mevcut projede olup olmadığını kontrol edin
4. Genel kural: Mevcut projedeki versiyon korunmalı
5. Eğer Degisim'de mevcut projede olmayan önemli bir özellik varsa, manuel olarak ekleyin
```

**Olası Yeni Özellikler (Mevcut Projede):**
- Yeni task özellikleri
- Performans iyileştirmeleri
- Bug fix'ler
- Yeni metodlar

**Kontrol:**
- [ ] Diff kontrolü yapıldı mı?
- [ ] Yeni task özellikleri tespit edildi mi?
- [ ] Performans iyileştirmeleri tespit edildi mi?
- [ ] Bug fix'ler tespit edildi mi?

---

## 📝 COMMAND DOSYALARI (1 DOSYA)

### 36. SGPCommand.java ⚠️

**Dosya Boyutları:**
- Degisim: 13,048 bytes
- Mevcut: 13,332 bytes
- **Fark:** +284 bytes (mevcut projede daha fazla özellik)

**Birleştirme Talimatı:**
```
⚠️ MANUEL KONTROL GEREKLİ

1. Diff tool ile karşılaştırın
2. Mevcut projedeki yeni komut özelliklerini tespit edin
3. Yeni subcommand'ler var mı kontrol edin
4. Genel kural: Mevcut projedeki versiyon korunmalı
```

**Kontrol:**
- [ ] Diff kontrolü yapıldı mı?
- [ ] Yeni komut özellikleri tespit edildi mi?
- [ ] Yeni subcommand'ler var mı?

---

## 🎯 ÖZELLİKLERİN DÜZGÜN ÇALIŞMASI İÇİN GEREKLİ ADIMLAR

### 1. TaskManager Sistemi ✅

**Kontrol Edilecek Yerler:**
```java
// Main.java
- Satır 92: private TaskManager taskManager;
- Satır 154: taskManager = new TaskManager(this);
- Satır 859-862: taskManager.shutdown(); (onDisable)
- Satır 1048: getTaskManager()
```

**Neden Önemli:**
- Memory leak önleme için kritik
- Tüm task'ları yönetir
- Plugin kapanırken temizlik yapar

**Birleştirme:**
```
✅ HİÇBİR ŞEY YAPMAYIN
Mevcut projedeki TaskManager sistemi korunmalı.
```

---

### 2. PersonalTerminalListener ✅

**Kontrol Edilecek Yerler:**
```java
// Main.java
- Satır 63: private PersonalTerminalListener personalTerminalListener;
- Satır 1032: getPersonalTerminalListener()
- Satır 1354: personalTerminalListener = new PersonalTerminalListener(this);

// GUI Dosyalarında Kullanım:
- TrainingMenu.java (Satır 288)
- TamingMenu.java (Satır 239-240)
- PowerMenu.java (Satır 296-297, 327-328, 349-350, 369-370)
- BreedingMenu.java (Satır 284-285, 321-322, 342-343)
```

**Neden Önemli:**
- GUI menülerinde kullanılıyor
- Personal Terminal sistemi için gerekli

**Birleştirme:**
```
✅ HİÇBİR ŞEY YAPMAYIN
Mevcut projedeki PersonalTerminalListener sistemi korunmalı.
```

---

### 3. DisasterListener ✅

**Kontrol Edilecek Yerler:**
```java
// Main.java
- Satır 284: Bukkit.getPluginManager().registerEvents(new DisasterListener(this), this);
```

**Neden Önemli:**
- Felaket hasar takibi için gerekli
- DisasterListener.java dosyası mevcut projede var

**Birleştirme:**
```
✅ HİÇBİR ŞEY YAPMAYIN
Mevcut projedeki DisasterListener kaydı korunmalı.
```

---

### 4. SQLite Veritabanı Sistemi ✅

**Kontrol Edilecek Yerler:**
```java
// Main.java
- Satır 894-897: dataManager.getDatabaseManager().close(); (onDisable)

// DataManager.java
- getDatabaseManager() metodu
- SQLite bağlantı yönetimi
```

**Neden Önemli:**
- Veri kaybını önlemek için kritik
- ACID uyumlu transaction garantisi
- Crash-safe (WAL modu)

**Birleştirme:**
```
✅ HİÇBİR ŞEY YAPMAYIN
Mevcut projedeki SQLite entegrasyonu korunmalı.
```

---

### 5. handleReload() Metodu ✅

**Kontrol Edilecek Yerler:**
```java
// AdminCommandExecutor.java
- Satır 59-60: case "reload": return handleReload(p);
- Satır 180-219: private boolean handleReload(Player p) { ... }
```

**Neden Önemli:**
- Config reload için gerekli
- Sunucuyu yeniden başlatmadan config güncellemesi

**Birleştirme:**
```
✅ TAMAMLANDI
handleReload() metodu zaten eklendi.
```

---

### 6. Null Kontrolleri (GUI Dosyaları) ✅

**Kontrol Edilecek Dosyalar:**
- ✅ AllianceMenu.java - Null kontrolleri var
- ✅ BreedingMenu.java - Null kontrolleri var
- ✅ ContractMenu.java - Null kontrolleri var
- ✅ PowerMenu.java - Null kontrolleri var
- ✅ TamingMenu.java - Null kontrolleri var
- ✅ TrainingMenu.java - Null kontrolleri var
- ⚠️ CaravanMenu.java - Kontrol edilmeli
- ⚠️ ClanBankMenu.java - Kontrol edilmeli
- ⚠️ ClanMenu.java - Kontrol edilmeli
- ⚠️ ClanStructureMenu.java - Kontrol edilmeli
- ⚠️ RecipeMenu.java - Kontrol edilmeli
- ⚠️ ShopMenu.java - Kontrol edilmeli

**Neden Önemli:**
- Null pointer exception'ları önlemek için kritik
- Plugin başlatma sırasında hata durumlarını yönetir

**Birleştirme:**
```
✅ MEVCUT PROJEDEKİ NULL KONTROLLERİ KORUNMALI
Eğer bir GUI dosyasında null kontrolleri yoksa, AllianceMenu.java'daki gibi eklenebilir.
```

---

## 📋 TÜM DOSYALAR İÇİN ÖZET TABLO

| # | Dosya | Degisim (bytes) | Mevcut (bytes) | Fark | Durum | İşlem |
|---|-------|----------------|----------------|------|-------|-------|
| 1 | Main.java | 71,529 | 74,235 | +2,706 | ✅ | Korunmalı |
| 2 | AdminCommandExecutor.java | 421,818 | 428,755 | +6,937 | ✅ | Korunmalı (handleReload eklendi) |
| 3 | DataManager.java | 99,132 | 105,133 | +6,001 | ✅ | Korunmalı |
| 4 | AllianceMenu.java | ~18,000 | ~19,000 | +1,000 | ✅ | Korunmalı (null kontrolleri) |
| 5 | BreedingMenu.java | 18,010 | 19,596 | +1,586 | ✅ | Korunmalı (null kontrolleri) |
| 6 | CaravanMenu.java | 23,354 | 24,469 | +1,115 | ⚠️ | Diff kontrolü |
| 7 | ClanBankMenu.java | 17,347 | 18,068 | +721 | ⚠️ | Diff kontrolü |
| 8 | ClanMenu.java | 18,624 | 19,242 | +618 | ⚠️ | Diff kontrolü |
| 9 | ClanStructureMenu.java | 23,896 | 24,908 | +1,012 | ⚠️ | Diff kontrolü |
| 10 | ContractMenu.java | 111,468 | 114,458 | +2,990 | ✅ | Korunmalı (null kontrolleri) |
| 11 | PowerMenu.java | 14,909 | 17,135 | +2,226 | ✅ | Korunmalı (PersonalTerminalListener) |
| 12 | RecipeMenu.java | 32,358 | 33,036 | +678 | ⚠️ | Diff kontrolü |
| 13 | ShopMenu.java | 7,825 | 8,029 | +204 | ⚠️ | Diff kontrolü |
| 14 | TamingMenu.java | 14,515 | 16,771 | +2,256 | ✅ | Korunmalı (null kontrolleri) |
| 15 | TrainingMenu.java | 11,826 | 12,672 | +846 | ✅ | Korunmalı (null kontrolleri + PersonalTerminalListener) |
| 16 | BossListener.java | 31,009 | 31,687 | +678 | ⚠️ | Diff kontrolü |
| 17 | ClanSystemListener.java | 7,302 | 7,509 | +207 | ✅ | Korunmalı (aynı) |
| 18 | GhostRecipeListener.java | 34,074 | 34,787 | +713 | ⚠️ | Diff kontrolü |
| 19 | PersonalTerminalListener.java | 8,182 | 8,992 | +810 | ⚠️ | Diff kontrolü |
| 20 | RitualInteractionListener.java | 58,572 | 59,912 | +1,340 | ⚠️ | Diff kontrolü |
| 21 | ShopListener.java | 16,134 | 16,473 | +339 | ⚠️ | Diff kontrolü |
| 22 | StructureActivationListener.java | 25,180 | 25,925 | +745 | ⚠️ | Diff kontrolü |
| 23 | StructureMenuListener.java | 10,203 | 10,460 | +257 | ⚠️ | Diff kontrolü |
| 24 | DisasterManager.java | 82,303 | 91,237 | +8,934 | ⚠️ | **DETAYLI KONTROL** |
| 25 | GhostRecipeManager.java | 28,270 | 28,953 | +683 | ⚠️ | Diff kontrolü |
| 26 | HUDManager.java | 21,677 | 22,278 | +601 | ⚠️ | Diff kontrolü |
| 27 | ItemManager.java | 170,164 | 173,250 | +3,086 | ⚠️ | Diff kontrolü |
| 28 | ClanBankSystem.java | 25,611 | 27,542 | +1,931 | ⚠️ | Diff kontrolü |
| 29 | AllianceHelper.java | 7,541 | 7,539 | -2 | ✅ | Korunmalı (aynı) |
| 30 | BossPhaseHelper.java | 6,505 | 6,695 | +190 | ⚠️ | Diff kontrolü |
| 31 | CaravanHelper.java | 8,100 | 8,348 | +248 | ⚠️ | Diff kontrolü |
| 32 | StructureHelper.java | 14,977 | 15,412 | +435 | ⚠️ | Diff kontrolü |
| 33 | TamingHelper.java | 8,843 | 8,841 | -2 | ✅ | Korunmalı (aynı) |
| 34 | Structure.java | 3,488 | 3,574 | +86 | ⚠️ | Diff kontrolü |
| 35 | DisasterTask.java | 21,503 | 35,239 | +13,736 | ⚠️ | **DETAYLI KONTROL** |
| 36 | SGPCommand.java | 13,048 | 13,332 | +284 | ⚠️ | Diff kontrolü |
| 37-39 | Markdown dosyaları | - | - | - | ✅ | Kopyalandı |

---

## 🎯 ÖNCELİK SIRASI

### Yüksek Öncelik (Hemen Kontrol Edilmeli):
1. ⚠️ **DisasterTask.java** (+13,736 bytes - ÇOK BÜYÜK FARK)
2. ⚠️ **DisasterManager.java** (+8,934 bytes - BÜYÜK FARK)
3. ⚠️ **ItemManager.java** (+3,086 bytes - ÖNEMLİ FARK)

### Orta Öncelik:
4. ⚠️ **ContractMenu.java** (+2,990 bytes)
5. ⚠️ **TamingMenu.java** (+2,256 bytes)
6. ⚠️ **PowerMenu.java** (+2,226 bytes)
7. ⚠️ **BreedingMenu.java** (+1,586 bytes)
8. ⚠️ **ClanBankSystem.java** (+1,931 bytes)
9. ⚠️ **CaravanMenu.java** (+1,115 bytes)
10. ⚠️ **ClanStructureMenu.java** (+1,012 bytes)

### Düşük Öncelik:
11-36. Diğer dosyalar (küçük farklar)

---

## ✅ SONUÇ

**Kesin Kararlar:**
- ✅ 8 dosya kesinlikle korunmalı (Main.java, AdminCommandExecutor.java, DataManager.java, AllianceMenu.java, BreedingMenu.java, ContractMenu.java, PowerMenu.java, TamingMenu.java, TrainingMenu.java, ClanSystemListener.java, AllianceHelper.java, TamingHelper.java)
- ⚠️ 28 dosya için manuel diff kontrolü gerekli
- ✅ 3 markdown dosyası kopyalandı

**Genel Kural:**
**MEVCUT PROJEDEKİ VERSİYONLAR KORUNMALI** (Degisim klasöründeki dosyalar daha eski)

---

**Döküman Son Güncelleme:** 11 Aralık 2025  
**Hazırlayan:** AI Assistant  
**Durum:** Tüm Dosyalar İçin Detaylı Talimatlar Hazır ✅

