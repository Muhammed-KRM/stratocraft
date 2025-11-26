# 🎮 Stratocraft Plugin - DETAYLI KURULUM REHBERİ

## 📦 ADIM 1: Test Sunucusu Klasörü Oluştur

1. **Bilgisayarınızda bir yere gidin** (örnek: `C:\mc\` veya `C:\Users\KullaniciAdiniz\Desktop\`)
2. **Yeni bir klasör oluşturun** ve adını `test-server` yapın
   - Sağ tık > Yeni > Klasör
   - İsmini `test-server` yapın
   
   **Örnek tam yol:** `C:\mc\test-server\`

---

## 📥 ADIM 2: Paper JAR Dosyasını Koy

1. **İndirdiğiniz `paper-1.20.4-445.jar` dosyasını bulun**
   - Genellikle `İndirilenler` klasöründe olur
   
2. **Bu dosyayı `test-server` klasörüne kopyalayın**
   - Dosyayı seçin (Ctrl+C)
   - `test-server` klasörüne gidin
   - Yapıştırın (Ctrl+V)

3. **Dosyayı yeniden adlandırın** (kolaylık için)
   - Dosyaya sağ tık > Yeniden Adlandır
   - İsmini `paper-1.20.4.jar` yapın
   - (Veya script'teki ismi değiştirebilirsiniz)

**Şu an klasörünüzde şunlar olmalı:**
```
test-server/
  └── paper-1.20.4.jar  (veya paper-1.20.4-445.jar)
```

---

## 🚀 ADIM 3: Başlatma Script'ini Oluştur

1. **`test-server` klasöründe yeni bir metin dosyası oluşturun**
   - Sağ tık > Yeni > Metin Belgesi
   - İsmini `baslat.bat` yapın (`.txt` uzantısını silin!)

2. **Dosyaya sağ tık > Birlikte Aç > Not Defteri**

3. **İçine şunu yazın:**
```batch
@echo off
echo ========================================
echo Stratocraft Test Sunucusu Baslatiliyor
echo ========================================
echo.
java -Xmx2G -Xms1G -jar paper-1.20.4-445.jar nogui
pause
```

**ÖNEMLİ:** Eğer dosyayı `paper-1.20.4.jar` olarak yeniden adlandırdıysanız, yukarıdaki `paper-1.20.4-445.jar` yerine `paper-1.20.4.jar` yazın!

4. **Dosyayı kaydedin** (Ctrl+S) ve kapatın

---

## ⚙️ ADIM 4: İlk Sunucu Başlatma (EULA)

1. **`baslat.bat` dosyasına çift tıklayın**
   - Bir konsol penceresi açılacak
   - Sunucu başlamaya çalışacak

2. **İlk başlatmada `eula.txt` dosyası oluşacak**
   - Sunucu otomatik kapanacak (normal, endişelenmeyin!)

3. **`test-server` klasöründe `eula.txt` dosyasını açın**
   - Not Defteri ile açın

4. **İçindeki `eula=false` satırını bulun ve `eula=true` yapın**
   ```
   eula=true
   ```

5. **Dosyayı kaydedin** (Ctrl+S) ve kapatın

---

## 🔄 ADIM 5: Sunucuyu Tekrar Başlat

1. **`baslat.bat` dosyasına tekrar çift tıklayın**
   - Sunucu şimdi başlayacak
   - Bir sürü mesaj göreceksiniz (normal)

2. **Sunucu tamamen başladığında şunu göreceksiniz:**
   ```
   [Server] Done (X.XXXs)! For help, type "help"
   ```

3. **Sunucu konsolunda `stop` yazın ve Enter'a basın**
   - Sunucu kapanacak
   - `plugins` klasörü oluşmuş olmalı

**Şu an klasörünüzde şunlar olmalı:**
```
test-server/
  ├── paper-1.20.4.jar (veya paper-1.20.4-445.jar)
  ├── baslat.bat
  ├── eula.txt
  ├── plugins/          ← YENİ OLUŞTU!
  ├── logs/
  ├── world/
  └── (diğer dosyalar)
```

---

## 📦 ADIM 6: Stratocraft Plugin'ini Yükle

1. **Plugin JAR dosyasını bulun:**
   - Proje klasörünüzde: `stratocraft/target/stratocraft-10.0-RELEASE.jar`
   - Tam yol örneği: `C:\mc\stratocraft\stratocraft\target\stratocraft-10.0-RELEASE.jar`

2. **Bu dosyayı kopyalayın** (Ctrl+C)

3. **`test-server/plugins/` klasörüne yapıştırın** (Ctrl+V)

**Şu an `plugins` klasörünüzde şunlar olmalı:**
```
test-server/plugins/
  └── stratocraft-10.0-RELEASE.jar
```

---

## 🌍 ADIM 7: WorldEdit Plugin'ini Yükle (ZORUNLU)

**WorldEdit olmadan Stratocraft çalışmaz!**

1. **WorldEdit'i indirin:**
   - https://dev.bukkit.org/projects/worldedit adresine gidin
   - Veya direkt: https://dev.bukkit.org/projects/worldedit/files
   - **7.2.9** sürümünü indirin (plugin'iniz bu sürümü kullanıyor)

2. **İndirdiğiniz WorldEdit JAR dosyasını bulun**
   - Genellikle `worldedit-bukkit-7.2.9.jar` gibi bir isimle gelir

3. **Bu dosyayı `test-server/plugins/` klasörüne kopyalayın**

**Şu an `plugins` klasörünüzde şunlar olmalı:**
```
test-server/plugins/
  ├── stratocraft-10.0-RELEASE.jar
  └── worldedit-bukkit-7.2.9.jar (veya benzer isim)
```

---

## 🎮 ADIM 8: Sunucuyu Başlat ve Test Et

1. **`baslat.bat` dosyasına çift tıklayın**

2. **Konsolda şu mesajları görmelisiniz:**
   ```
   [WorldEdit] Loading WorldEdit v7.2.9
   [Stratocraft] Enabling Stratocraft v10.0
   [Stratocraft] Stratocraft: Veriler yuklendi.
   ```

3. **Hata görürseniz:**
   - Konsoldaki hata mesajını okuyun
   - `logs/latest.log` dosyasını kontrol edin

---

## 🖥️ ADIM 9: Minecraft'tan Bağlan

1. **Minecraft'ı açın**
   - **1.20.4** sürümünde olmalı (Paper 1.20.4 kullanıyorsunuz)

2. **Multiplayer'a gidin**
   - Ana menüden "Çok Oyunculu" seçeneğine tıklayın

3. **"Sunucuya Doğrudan Bağlan" veya "Direct Connect" seçin**

4. **Sunucu adresine şunu yazın:**
   ```
   localhost
   ```
   veya
   ```
   127.0.0.1
   ```

5. **"Sunucuya Katıl" veya "Join Server" butonuna tıklayın**

6. **Oyuna bağlanmalısınız!** 🎉

---

## ✅ ADIM 10: Plugin Komutlarını Test Et

Oyunda şu komutları deneyin:

```
/klan
```
veya
```
/clan
```

Eğer komut çalışıyorsa, plugin başarıyla yüklendi demektir!

**Diğer test komutları:**
- `/klan kur TestKlan` - Klan kur
- `/klan menü` - Klan menüsü
- `/kontrat list` - Kontrat listesi

---

## 🔧 SORUN GİDERME

### Java Hatası Alıyorum
- **Kontrol:** Komut satırında `java -version` yazın
- **Gerekli:** Java 17 veya üzeri olmalı
- **Çözüm:** Java'yı güncelleyin: https://adoptium.net/

### Plugin Yüklenmiyor
- **Kontrol:** `plugins` klasöründe JAR dosyası var mı?
- **Kontrol:** Dosya adı `.jar` ile bitiyor mu?
- **Kontrol:** Konsolda hata mesajı var mı?

### WorldEdit Hatası
- **Kontrol:** WorldEdit yüklü mü? (`plugins` klasöründe olmalı)
- **Kontrol:** WorldEdit sürümü 7.2.9 mu?

### Sunucu Başlamıyor
- **Kontrol:** `eula.txt` dosyasında `eula=true` yazıyor mu?
- **Kontrol:** Java yüklü mü? (`java -version`)

### Oyuna Bağlanamıyorum
- **Kontrol:** Sunucu tamamen başladı mı? ("Done!" mesajını gördünüz mü?)
- **Kontrol:** Minecraft sürümü 1.20.4 mü?
- **Kontrol:** Firewall sunucuyu engelliyor mu?

---

## 📁 KLASÖR YAPISI (SON HAL)

Başarılı kurulumdan sonra klasör yapınız şöyle olmalı:

```
test-server/
  ├── baslat.bat
  ├── paper-1.20.4.jar (veya paper-1.20.4-445.jar)
  ├── eula.txt
  ├── server.properties
  ├── plugins/
  │   ├── stratocraft-10.0-RELEASE.jar
  │   ├── worldedit-bukkit-7.2.9.jar
  │   └── Stratocraft/          ← Plugin çalıştıktan sonra oluşur
  │       ├── config.yml
  │       └── lang.yml
  ├── logs/
  ├── world/
  └── (diğer sunucu dosyaları)
```

---

## 🎯 HIZLI ÖZET

1. ✅ `test-server` klasörü oluştur
2. ✅ `paper-1.20.4-445.jar` dosyasını koy
3. ✅ `baslat.bat` script'i oluştur
4. ✅ İlk başlatmada `eula.txt`'yi `true` yap
5. ✅ `stratocraft-10.0-RELEASE.jar` dosyasını `plugins/` klasörüne koy
6. ✅ WorldEdit'i `plugins/` klasörüne koy
7. ✅ Sunucuyu başlat
8. ✅ Minecraft'tan `localhost` ile bağlan
9. ✅ `/klan` komutunu test et

**BAŞARILAR! 🚀**

