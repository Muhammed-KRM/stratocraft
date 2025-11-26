# Stratocraft Plugin Test Rehberi

## 📋 Gereksinimler

- Java 17 veya üzeri (projeniz Java 17 kullanıyor)
- Paper 1.20.4 sunucu dosyası
- WorldEdit plugin (zorunlu bağımlılık)
- Vault plugin (opsiyonel, ekonomi için)

## 🚀 Adım Adım Kurulum

### 1. Test Sunucusu Klasörü Oluştur

Yeni bir klasör oluşturun (örn: `C:\mc\test-server`)

### 2. Paper Sunucusunu İndir ve Kur

**ÖNEMLİ:** Plugin 1.20.4 için yazılmıştır. 1.21.10 ile uyumsuzluk olabilir!

**1.20.4 İndirme Yöntemleri:**

**Yöntem 1: Paper İndirme Sayfası**
1. https://papermc.io/downloads/paper adresine gidin
2. Sayfada sürüm seçici var, **1.20.4** sürümünü seçin
3. En son build'i indirin (örn: `paper-1.20.4-xxx.jar`)

**Yöntem 2: Direkt API Linki (Hızlı)**
Aşağıdaki linki tarayıcınıza yapıştırın (en son 1.20.4 build'i):
```
https://api.papermc.io/v2/projects/paper/versions/1.20.4/builds
```
Bu sayfada en üstteki build numarasını bulun, sonra:
```
https://api.papermc.io/v2/projects/paper/versions/1.20.4/builds/[BUILD_NO]/downloads/paper-1.20.4-[BUILD_NO].jar
```
(Örnek: Build 445 ise → `https://api.papermc.io/v2/projects/paper/versions/1.20.4/builds/445/downloads/paper-1.20.4-445.jar`)

**Yöntem 3: BuildTools (Alternatif)**
Eğer yukarıdakiler çalışmazsa, eski build'leri arşivden bulabilirsiniz.

4. İndirdiğiniz JAR dosyasını test sunucusu klasörüne koyun
5. Dosyayı `paper-1.20.4.jar` olarak yeniden adlandırın (veya script'teki ismi güncelleyin)

### 3. İlk Sunucu Başlatma

1. Test sunucusu klasöründe `test-server-start.bat` dosyasını çalıştırın
2. Sunucu ilk kez başlatıldığında:
   - `eula.txt` dosyası oluşacak
   - `eula.txt` dosyasını açın ve `eula=false` satırını `eula=true` yapın
   - Sunucuyu tekrar başlatın
3. Sunucu başladıktan sonra `stop` komutuyla durdurun

### 4. Plugin Klasörü Oluştur

Sunucu klasöründe `plugins` klasörü oluşturulacak (otomatik oluşur)

### 5. Stratocraft Plugin'ini Yükle

1. `stratocraft/target/stratocraft-10.0-RELEASE.jar` dosyasını kopyalayın
2. Test sunucusunun `plugins` klasörüne yapıştırın

### 6. Gerekli Bağımlılıkları Yükle

#### WorldEdit (Zorunlu)
1. https://dev.bukkit.org/projects/worldedit adresinden WorldEdit 7.2.9 indirin
2. JAR dosyasını `plugins` klasörüne koyun

#### Vault (Opsiyonel - Ekonomi için)
1. https://dev.bukkit.org/projects/vault adresinden Vault indirin
2. JAR dosyasını `plugins` klasörüne koyun
3. Bir ekonomi plugin'i de gerekli (EssentialsX, CMI, vb.)

### 7. Sunucuyu Başlat ve Test Et

1. `test-server-start.bat` dosyasını çalıştırın
2. Sunucu başladığında konsolda şu mesajları görmelisiniz:
   ```
   [WorldEdit] Loading WorldEdit v7.2.9
   [Stratocraft] Loading Stratocraft v10.0
   [Stratocraft] Stratocraft: Veriler yuklendi.
   ```
3. Hata varsa konsolda göreceksiniz

### 8. Oyuna Bağlan

1. Minecraft'ı açın
2. Multiplayer > Direct Connect
3. `localhost` veya `127.0.0.1` yazın
4. Bağlanın

### 9. Plugin Komutlarını Test Et

Oyunda şu komutları deneyin:
- `/klan` veya `/clan` - Klan komutları
- `/kontrat` veya `/contract` - Kontrat komutları

## 🔍 Hata Ayıklama

### Plugin Yüklenmiyor
- `plugins` klasöründe JAR dosyası var mı kontrol edin
- Konsolda hata mesajlarını okuyun
- `logs/latest.log` dosyasını kontrol edin

### Bağımlılık Hataları
- WorldEdit yüklü mü kontrol edin
- Plugin sürümleri uyumlu mu kontrol edin (Paper 1.20.4)

### Java Versiyonu Hatası
- `java -version` komutuyla Java versiyonunu kontrol edin
- Java 17 veya üzeri olmalı

## 📝 Notlar

- Test sunucusu için minimum 2GB RAM önerilir
- İlk başlatmada config dosyaları otomatik oluşur
- `plugins/Stratocraft/` klasöründe config dosyalarını düzenleyebilirsiniz

## 🎮 Hızlı Test Komutları

Sunucu konsolunda veya oyunda:
```
/klan kur <klanAdi>        - Klan kur
/klan menü                 - Klan menüsü
/kontrat list              - Kontrat listesi
```

