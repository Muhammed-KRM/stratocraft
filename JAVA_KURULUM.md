# Java Kurulum Rehberi

## 🔍 Sorun: Java Bulunamıyor veya RAM Hatası

Resimde gördüğünüz hata, Java'nın düzgün kurulu olmadığını veya 32-bit Java kullandığınızı gösteriyor.

## ✅ Çözüm: Java 17 Kurulumu

### Adım 1: Java'yı İndir

1. **Adoptium (Eclipse Temurin) sitesine gidin:**
   - https://adoptium.net/

2. **Java 17 LTS seçin:**
   - "Latest LTS Release" bölümünden **17** seçin
   - **Windows x64** için indirin (32-bit değil, 64-bit!)

3. **İndirme seçenekleri:**
   - **JDK** (Java Development Kit) - Önerilen
   - Veya **JRE** (Java Runtime Environment) - Sadece çalıştırmak için yeterli

### Adım 2: Java'yı Kur

1. **İndirdiğiniz .msi dosyasına çift tıklayın**
2. **Kurulum sihirbazını takip edin:**
   - "Next" butonlarına tıklayın
   - Varsayılan ayarları kabul edin
   - "Install" butonuna tıklayın

3. **Kurulum tamamlandıktan sonra bilgisayarı yeniden başlatın** (önerilir)

### Adım 3: Java Kurulumunu Kontrol Et

1. **Windows tuşu + R** basın
2. **`cmd`** yazın ve Enter'a basın
3. **Şu komutu yazın:**
   ```
   java -version
   ```

4. **Şöyle bir çıktı görmelisiniz:**
   ```
   openjdk version "17.0.x"
   OpenJDK Runtime Environment Temurin-17.0.x+8
   OpenJDK 64-Bit Server VM Temurin-17.0.x+8
   ```

5. **ÖNEMLİ:** "64-Bit" yazısını görmelisiniz! 32-bit Java yeterli RAM ayıramaz.

### Adım 4: Script'i Güncelle

`baslat.bat` dosyasını güncelledim. Artık:
- RAM miktarı **1GB**'a düşürüldü (2GB yerine)
- Java kontrolü eklendi
- Daha iyi hata mesajları var

### Adım 5: Tekrar Dene

1. **`baslat.bat` dosyasına çift tıklayın**
2. Artık çalışmalı!

---

## 🔧 Alternatif: Java PATH Sorunu

Eğer Java kurulu ama hala bulunamıyorsa:

1. **Java kurulum yolunu bulun:**
   - Genellikle: `C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot\bin\`
   - Veya: `C:\Program Files\Java\jdk-17\bin\`

2. **PATH'e ekleyin:**
   - Windows tuşu + R → `sysdm.cpl` → Enter
   - "Gelişmiş" sekmesi → "Ortam Değişkenleri"
   - "Sistem değişkenleri" altında "Path" seçin → "Düzenle"
   - "Yeni" → Java'nın `bin` klasörünün yolunu ekleyin
   - Tüm pencereleri "Tamam" ile kapatın
   - Bilgisayarı yeniden başlatın

---

## 📝 Notlar

- **64-bit Java şart!** 32-bit Java maksimum ~1.5GB RAM ayırabilir
- **Java 17 veya üzeri** gerekli (Paper 1.20.4 için)
- Test sunucusu için **1GB RAM yeterli**, production için daha fazla gerekebilir

---

## ✅ Başarı Kontrolü

Java kurulumundan sonra:

1. Yeni bir komut satırı açın (cmd)
2. `java -version` yazın
3. "64-Bit" yazısını görüyorsanız ✅
4. `baslat.bat` dosyasını çalıştırın
5. Sunucu başlamalı! 🎉

