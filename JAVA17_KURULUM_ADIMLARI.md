# 🚨 Java 17 Kurulumu - ADIM ADIM

## ❌ Mevcut Durumunuz
- **Java 8 (1.8.0_471)** - ÇOK ESKİ! ❌
- **Client VM** - 32-bit Java olabilir ❌
- **Gereken:** Java 17 veya üzeri (64-bit) ✅

## ✅ ÇÖZÜM: Java 17 Kur

### ADIM 1: Eski Java'yı Kaldır (Opsiyonel ama Önerilir)

1. **Windows Ayarlar** → **Uygulamalar** → **Uygulamalar ve özellikler**
2. **"Java"** yazın ve arayın
3. **Java 8** varsa → **Kaldır** (opsiyonel, yeni Java ile birlikte durabilir)

### ADIM 2: Java 17 İndir

1. **Tarayıcınızda şu adrese gidin:**
   ```
   https://adoptium.net/
   ```

2. **Sayfada şunları seçin:**
   - **Version:** `17` (LTS - Long Term Support)
   - **Operating System:** `Windows`
   - **Architecture:** `x64` (64-bit - ÖNEMLİ!)
   - **Package Type:** `JDK` (veya JRE - sadece çalıştırmak için yeterli)

3. **"Latest release"** butonuna tıklayın
4. **İndirme başlayacak** (yaklaşık 150-200 MB)

### ADIM 3: Java 17 Kur

1. **İndirdiğiniz `.msi` dosyasına çift tıklayın**
   - Örnek isim: `OpenJDK17U-jdk_x64_windows_hotspot_17.0.x_x.msi`

2. **Kurulum sihirbazı açılacak:**
   - "Next" butonlarına tıklayın
   - Varsayılan ayarları kabul edin
   - **"Set JAVA_HOME variable"** seçeneğini işaretleyin (varsa)
   - "Install" butonuna tıklayın

3. **Kurulum tamamlanana kadar bekleyin** (1-2 dakika)

4. **"Finish" butonuna tıklayın**

### ADIM 4: Bilgisayarı Yeniden Başlat

**ÖNEMLİ:** Java kurulumundan sonra bilgisayarı yeniden başlatın!
- Windows menüsü → Güç → Yeniden Başlat

### ADIM 5: Java 17 Kontrolü

1. **Bilgisayar yeniden başladıktan sonra:**
   - **Windows tuşu + R** basın
   - **`cmd`** yazın ve **Enter**'a basın

2. **Şu komutu yazın:**
   ```
   java -version
   ```

3. **ŞÖYLE BİR ÇIKTI GÖRMELİSİNİZ:**
   ```
   openjdk version "17.0.x"
   OpenJDK Runtime Environment Temurin-17.0.x+8
   OpenJDK 64-Bit Server VM Temurin-17.0.x+8 (build 17.0.x+8, mixed mode, sharing)
   ```

4. **ÖNEMLİ KONTROLLER:**
   - ✅ "17.0" veya üzeri versiyon görünmeli
   - ✅ **"64-Bit"** yazısı görünmeli (32-bit değil!)
   - ✅ "Server VM" yazısı görünmeli (Client VM değil!)

### ADIM 6: Eski Java Hala Görünüyorsa

Eğer hala Java 8 görünüyorsa:

1. **PATH değişkenini kontrol edin:**
   - Windows tuşu + R → `sysdm.cpl` → Enter
   - "Gelişmiş" sekmesi → "Ortam Değişkenleri"
   - "Sistem değişkenleri" altında **"Path"** seçin → "Düzenle"

2. **Java 17 yolunu en üste taşıyın:**
   - Genellikle: `C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot\bin\`
   - Bu satırı bulun ve **en üste** taşıyın (yukarı ok ile)
   - Java 8 yolunu silin veya aşağı taşıyın

3. **Tüm pencereleri "Tamam" ile kapatın**
4. **Yeni bir komut satırı açın** (eski cmd penceresini kapatın)
5. **Tekrar `java -version` yazın**

---

## ✅ Başarı Kontrolü

Java 17 kurulduktan sonra:

1. ✅ `java -version` → "17.0" görünmeli
2. ✅ "64-Bit" görünmeli
3. ✅ "Server VM" görünmeli
4. ✅ `baslat.bat` dosyası çalışmalı

---

## 🎮 Sonraki Adım

Java 17 kurulduktan sonra:

1. **`C:\mc\test-server\` klasörüne gidin**
2. **`baslat.bat` dosyasına çift tıklayın**
3. **Sunucu başlamalı!** 🎉

---

## 📝 Notlar

- **Java 8 → Java 17:** Büyük bir sürüm atlaması, bazı eski programlar çalışmayabilir
- **64-bit şart:** 32-bit Java yeterli RAM ayıramaz
- **Server VM:** Daha iyi performans için
- **Java 17 LTS:** Uzun süre desteklenecek, güvenli seçim

---

## 🔗 Hızlı İndirme Linki

Direkt indirme (en son Java 17 LTS):
- https://adoptium.net/temurin/releases/?version=17

**Windows x64 JDK** seçin ve indirin!

