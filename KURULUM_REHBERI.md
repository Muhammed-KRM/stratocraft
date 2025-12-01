# 🚀 Stratocraft Plugin - Test Server Kurulum Rehberi

## 📍 TEST SERVER BİLGİLERİ

- **Test Server Yolu:** `C:\mc\test-server`
- **Minecraft Sürümü:** 1.20.4 (Paper)
- **Java Gereksinimi:** Java 17+
- **Mevcut Plugin'ler:** WorldEdit 7.3.0, Vault

---

## ✅ TAMAMLANAN İŞLEMLER

Tüm kodlar eklendi! Şimdi sadece şu adımları takip etmeniz gerekiyor:

---

## 📋 YAPMANIZ GEREKEN ADIMLAR

### ADIM 1: Plugin'i Build Etme ve Yerleştirme

#### 1.1 Maven ile Build Etme

1. **Proje Klasörüne Git:**
   ```bash
   cd C:\mc\stratocraft
   ```

2. **Maven Build:**
   ```bash
   mvn clean package
   ```
   
   **Alternatif (Maven yoksa):**
   - IntelliJ IDEA veya Eclipse kullanıyorsanız:
     - IntelliJ: `Build` → `Build Project` (Ctrl+F9)
     - Eclipse: `Project` → `Clean...` → `Build`

3. **JAR Dosyasını Bul:**
   - Build sonrası JAR dosyası şurada olacak:
   ```
   C:\mc\stratocraft\target\stratocraft-10.0-RELEASE.jar
   ```

#### 1.2 Test Server'a Yerleştirme

1. **Eski Plugin'i Kaldır (varsa):**
   ```
   C:\mc\test-server\plugins\stratocraft-10.0-RELEASE.jar
   ```
   - Bu dosyayı sil veya yedekle

2. **Yeni Plugin'i Kopyala:**
   - `C:\mc\stratocraft\target\stratocraft-10.0-RELEASE.jar` dosyasını kopyala
   - `C:\mc\test-server\plugins\` klasörüne yapıştır

3. **Klasör Yapısı Kontrolü:**
   ```
   C:\mc\test-server\plugins\
   ├── Stratocraft\
   │   ├── config.yml
   │   ├── lang.yml
   │   ├── data\
   │   └── schematics\  (otomatik oluşacak)
   ├── WorldEdit\
   ├── Vault\
   └── stratocraft-10.0-RELEASE.jar  ← YENİ JAR BURADA
   ```

---

### ADIM 2: Gerekli Plugin'lerin Kontrolü

#### 2.1 WorldEdit Kontrolü

✅ **ZATEN KURULU!** (WorldEdit 7.3.0)

- Dosya: `C:\mc\test-server\plugins\worldedit-bukkit-7.3.0.jar`
- Kontrol: Sunucu başladığında konsolda "WorldEdit enabled" mesajı görünmeli

#### 2.2 FAWE Kurulumu (OPSİYONEL - ÖNERİLİR)

FAWE, WorldEdit'in daha hızlı versiyonu. Büyük yapılar için önerilir.

1. **FAWE İndir:**
   - https://ci.athion.net/job/FastAsyncWorldEdit/ adresine git
   - "Last Successful Artifacts" bölümünden indir
   - Minecraft 1.20.4 için uygun olanı seç

2. **Kurulum:**
   - İndirdiğiniz `.jar` dosyasını `C:\mc\test-server\plugins\` klasörüne koy
   - **ÖNEMLİ:** WorldEdit ile birlikte çalışır, WorldEdit'i kaldırmayın!
   - Sunucuyu başlat

3. **Test:**
   - Konsolda "FastAsyncWorldEdit enabled" mesajını gör
   - FAWE yoksa normal WorldEdit kullanılır (sorun değil)

---

### ADIM 3: Config Ayarlarının Kontrolü

#### 3.1 Config Dosyası Yolu

```
C:\mc\test-server\plugins\Stratocraft\config.yml
```

#### 3.2 Önemli Ayarlar

Config dosyasını açıp şu ayarları kontrol edin:

**Zindan Sistemi:**
```yaml
dungeons:
  enabled: true  # ← true olmalı
  spawn-chance:
    level1: 0.05  # %5 şans
    level2: 0.08  # %8 şans
    level3: 0.10  # %10 şans
    level4: 0.12  # %12 şans
    level5: 0.15  # %15 şans
```

**Biyom Sistemi:**
```yaml
biomes:
  enabled: true  # ← true olmalı
```

**Dünya Merkez Noktası:**
```yaml
world:
  center:
    x: 0.0
    y: 64.0
    z: 0.0
    world: world
```

**NOT:** Eğer spawn noktanız farklıysa, bu değerleri güncelleyin!

---

### ADIM 4: Klasör Yapısı (OTOMATİK OLUŞTURULUR)

**İYİ HABER:** Klasörler otomatik oluşturulur! Sunucuyu bir kez başlattığınızda şu klasörler oluşur:

```
C:\mc\test-server\plugins\Stratocraft\
└── schematics\
    ├── dungeons\
    │   ├── level1\
    │   ├── level2\
    │   ├── level3\
    │   ├── level4\
    │   └── level5\
    └── biomes\
        ├── structures\
        └── custom\
```

**Yapmanız gereken:** Hiçbir şey! Sunucu başladığında otomatik oluşur.

**Kontrol:**
- Sunucuyu başlattıktan sonra konsolda şu mesajı görmelisiniz:
  ```
  [Stratocraft] Şema klasörleri oluşturuldu: C:\mc\test-server\plugins\Stratocraft\schematics
  ```

---

### ADIM 5: Hazır Şemaları İndirme ve Yerleştirme

#### 5.1 PlanetMinecraft'dan İndirme

1. **Siteye Git:**
   - https://www.planetminecraft.com/resources/schematics/
   - Veya direkt: https://www.planetminecraft.com/resources/projects/?m_order=date&m_sort=publish_date&m_orderby=desc&m_tags[]=dungeon

2. **Arama Yap:**
   - Arama kutusuna şunları yaz:
     - "dungeon"
     - "cave"
     - "underground"
     - "ruins"
     - "temple"
   - **Filtrele:**
     - "Free" seçeneğini işaretle
     - "Downloadable" seçeneğini işaretle

3. **Şema Seç:**
   - Beğendiğiniz bir şemaya tıklayın
   - Sayfada "Download" butonunu bulun
   - İndirme başlar

4. **Dosya Formatı:**
   - İndirilen dosya `.schematic` veya `.schem` olabilir
   - İkisi de çalışır!

#### 5.2 Şemaları Yerleştirme

**ÖNEMLİ:** Dosya isimleri config.yml'deki isimlerle eşleşmeli!

**Config'deki isimler:**
- Seviye 1: `goblin_cave`, `spider_nest`, `bandit_hideout`
- Seviye 2: `orc_fortress`, `skeleton_crypt`, `dark_temple`
- Seviye 3: `dragon_lair`, `ancient_ruins`, `demon_castle`
- Seviye 4: `titan_tomb`, `void_prison`, `hell_fortress`
- Seviye 5: `cosmic_temple`, `god_realm`, `chaos_dimension`

**Yerleştirme Adımları:**

1. İndirdiğiniz şema dosyasını bulun (`.schematic` veya `.schem`)

2. Dosyayı yeniden adlandırın (config'deki isimle):
   - Örnek: İndirdiğiniz dosya `dungeon_v1.schematic` ise
   - Yeniden adlandır: `goblin_cave.schem` (veya `.schematic`)

3. Doğru klasöre koyun:
   ```
   C:\mc\test-server\plugins\Stratocraft\schematics\dungeons\level1\goblin_cave.schem
   ```

4. **Her seviye için en az 1 şema olmalı!**
   - Seviye 1 için: `goblin_cave.schem` yeterli (diğerleri opsiyonel)
   - Ama daha fazla çeşitlilik için 3-5 şema önerilir

#### 5.3 Format Dönüştürme (Gerekirse)

Eğer `.schematic` formatında indirdiyseniz ve `.schem` istiyorsanız:

1. **WorldEdit ile (Oyunda):**
   ```
   /schematic load <dosya_adi>
   /schematic save <yeni_dosya_adi>
   ```

2. **Veya:**
   - `.schematic` uzantısını `.schem` olarak değiştirin
   - Genellikle çalışır (format benzer)

---

### ADIM 6: Sunucuyu Başlatma ve Test Etme

#### 6.1 Sunucuyu Başlatma

1. **Test Server Klasörüne Git:**
   ```
   cd C:\mc\test-server
   ```

2. **Sunucuyu Başlat:**
   - `baslat.bat` dosyasını çift tıklayın
   - Veya komut satırından:
     ```bash
     java -Xmx2G -Xms1G -jar paper-1.20.4.jar nogui
     ```

3. **Konsol Mesajlarını Kontrol Et:**
   Sunucu başladığında şu mesajları görmelisiniz:
   ```
   [Stratocraft] Şema klasörleri oluşturuldu: C:\mc\test-server\plugins\Stratocraft\schematics
   [Stratocraft] Zindan ayarları yüklendi: 5 seviye
   [Stratocraft] Biyom ayarları yüklendi: 5 seviye
   [WorldEdit] WorldEdit enabled
   ```

#### 6.2 Admin Komutları ile Test

Oyuna girip şu komutları test edin:

```bash
# Zindan listesi
/stratocraft dungeon list

# Zindan spawn (seviye 1, rastgele tip)
/stratocraft dungeon spawn 1

# Zindan spawn (seviye 1, belirli tip)
/stratocraft dungeon spawn 1 goblin_cave

# Biyom listesi
/stratocraft biome list

# Spawn edilmiş zindanları temizle
/stratocraft dungeon clear
```

#### 6.3 Otomatik Spawn Test

1. **Oyuna Gir:**
   - Test server'a bağlan

2. **Merkezden Uzaklaş:**
   - Spawn noktasından (0, 64, 0) 500+ blok uzaklaş
   - Yeni chunk'lar yüklenirken zindanlar otomatik spawn olmalı

3. **Zindanları Bul:**
   - Yer altında (y=30-50 arası) zindanlar görünmeli
   - `/locate structure` komutu ile bulabilirsiniz (eğer destekleniyorsa)

---

## 🎯 ÖRNEK ŞEMA İNDİRME SENARYOSU

### Senaryo: Seviye 1 Zindanı İndirme

1. **PlanetMinecraft'a git:**
   - https://www.planetminecraft.com/resources/schematics/

2. **Ara:**
   - "goblin cave" veya "small dungeon"
   - Filtrele: "Free"

3. **İndir:**
   - Beğendiğin bir şemayı indir
   - Örnek: `small_cave_dungeon.schematic`

4. **Yeniden Adlandır:**
   - `small_cave_dungeon.schematic` → `goblin_cave.schem`

5. **Yerleştir:**
   - `C:\mc\test-server\plugins\Stratocraft\schematics\dungeons\level1\goblin_cave.schem`

6. **Test:**
   - Sunucuyu yeniden başlat (veya `/stratocraft reload` varsa)
   - `/stratocraft dungeon spawn 1 goblin_cave`

---

## ⚠️ ÖNEMLİ NOTLAR

### Telif Hakları

✅ **Güvenli:**
- "Free to use" belirtilen şemalar
- "Creative Commons" lisanslı
- "No attribution required" olanlar

❌ **Dikkat:**
- "All rights reserved" olanlar
- Ücretli şemalar
- Belirsiz lisans

### Dosya İsimlendirme

- **Config'deki isimlerle eşleşmeli!**
- Büyük/küçük harf duyarlı değil
- `.schem` veya `.schematic` uzantısı kullanılabilir

### Minimum Gereksinimler

- **Her seviye için en az 1 şema** olmalı
- Şema yoksa zindan spawn olmaz (hata vermez, sadece spawn olmaz)
- Config'deki isimlerle dosya isimleri eşleşmeli

### Test Server Özel Notlar

- **Plugin Güncelleme:** Her kod değişikliğinden sonra:
  1. `mvn clean package` ile build et
  2. JAR'ı `C:\mc\test-server\plugins\` klasörüne kopyala
  3. Sunucuyu yeniden başlat

- **Config Güncelleme:** Config değişiklikleri için:
  1. `C:\mc\test-server\plugins\Stratocraft\config.yml` dosyasını düzenle
  2. Sunucuyu yeniden başlat (veya `/stratocraft reload` varsa)

---

## 🔧 SORUN GİDERME

### Problem: "Şema dosyası bulunamadı" hatası

**Çözüm:**
1. Dosya ismini kontrol et (config.yml ile eşleşiyor mu?)
2. Klasör yolunu kontrol et (`dungeons/level1/` doğru mu?)
3. Dosya uzantısını kontrol et (`.schem` veya `.schematic`)
4. Dosya yolunu kontrol et:
   ```
   C:\mc\test-server\plugins\Stratocraft\schematics\dungeons\level1\goblin_cave.schem
   ```

### Problem: Zindanlar spawn olmuyor

**Çözüm:**
1. Config'de `dungeons.enabled: true` olduğundan emin ol
2. Spawn şansını kontrol et (config.yml'de `spawn-chance` değerleri)
3. Yeni chunk'larda test et (eski chunk'larda spawn olmaz)
4. Difficulty seviyesini kontrol et (200+ blok uzakta olmalı)
5. Konsol loglarını kontrol et:
   ```
   [Stratocraft] Zindan spawn edildi: goblin_cave (Seviye 1)
   ```

### Problem: WorldEdit bulunamadı hatası

**Çözüm:**
1. WorldEdit plugin'inin `C:\mc\test-server\plugins\` klasöründe olduğundan emin ol
2. Dosya adı: `worldedit-bukkit-7.3.0.jar`
3. Sunucuyu yeniden başlat
4. Konsolda "WorldEdit enabled" mesajını kontrol et

### Problem: Plugin yüklenmiyor

**Çözüm:**
1. JAR dosyasının doğru yerde olduğundan emin:
   ```
   C:\mc\test-server\plugins\stratocraft-10.0-RELEASE.jar
   ```
2. Konsol hatalarını kontrol et (genellikle bağımlılık hatası)
3. Java versiyonunu kontrol et (Java 17+ gerekli):
   ```bash
   java -version
   ```
4. Maven build'in başarılı olduğundan emin ol

### Problem: Config ayarları kayboldu

**Çözüm:**
1. Config dosyası şurada olmalı:
   ```
   C:\mc\test-server\plugins\Stratocraft\config.yml
   ```
2. Eğer yoksa, sunucuyu bir kez başlat (otomatik oluşur)
3. `src/main/resources/config.yml` dosyasından kopyalayabilirsiniz

---

## 📝 ÖZET: YAPILACAKLAR LİSTESİ

- [x] Test server yapısı hazır (`C:\mc\test-server`)
- [x] WorldEdit kurulu (7.3.0)
- [ ] Plugin'i build et (`mvn clean package`)
- [ ] JAR'ı test server'a kopyala
- [ ] Config ayarlarını kontrol et
- [ ] Sunucuyu başlat (klasörler otomatik oluşur)
- [ ] PlanetMinecraft'dan şemaları indir
- [ ] Şemaları config'deki isimlerle yeniden adlandır
- [ ] Şemaları doğru klasörlere yerleştir
- [ ] Test et: `/stratocraft dungeon spawn 1`
- [ ] Yeni chunk'larda otomatik spawn'ı kontrol et

---

## 🎮 KULLANILABİLİR KOMUTLAR

### Zindan Komutları:
```
/stratocraft dungeon spawn <level> [type]
/stratocraft dungeon list [level]
/stratocraft dungeon clear
```

### Biyom Komutları:
```
/stratocraft biome list [level]
/stratocraft biome set <biome>
```

### Diğer Admin Komutları:
```
/stratocraft give <kategori> <item> [miktar]
/stratocraft spawn <kategori> <mob>
/stratocraft build <type> [level]
/stratocraft disaster <type> [konum]
```

---

## ✅ BAŞARILI KURULUM KONTROLÜ

Kurulum başarılıysa şunları görmelisiniz:

1. **Konsol Mesajları:**
   ```
   [Stratocraft] Şema klasörleri oluşturuldu: C:\mc\test-server\plugins\Stratocraft\schematics
   [Stratocraft] Zindan ayarları yüklendi: 5 seviye
   [Stratocraft] Biyom ayarları yüklendi: 5 seviye
   [WorldEdit] WorldEdit enabled
   ```

2. **Klasör Yapısı:**
   ```
   C:\mc\test-server\plugins\Stratocraft\schematics\dungeons\level1\ (şemalar burada)
   ```

3. **Komut Çalışıyor:**
   ```
   /stratocraft dungeon list
   → Seviye 1-5 zindanları listelenir
   ```

4. **Otomatik Spawn:**
   - Yeni chunk'larda zindanlar otomatik spawn olur
   - Yer altında (y=30-50) görünür

---

## 🚀 HAZIR!

Artık sistem çalışıyor! Şemaları ekledikçe daha fazla zindan çeşitliliği olacak.

**Sonraki Adımlar:**
- Daha fazla şema indir (çeşitlilik için)
- Spawn şanslarını config'den ayarla
- Yeni zindan tipleri ekle (config.yml'de)
- Test server'da oyun mekaniklerini test et

---

## 📞 HIZLI BAŞVURU

**Test Server Yolu:**
```
C:\mc\test-server
```

**Plugin JAR Yolu:**
```
C:\mc\test-server\plugins\stratocraft-10.0-RELEASE.jar
```

**Config Yolu:**
```
C:\mc\test-server\plugins\Stratocraft\config.yml
```

**Şema Klasörü:**
```
C:\mc\test-server\plugins\Stratocraft\schematics\dungeons\level1\
```

**Sunucu Başlatma:**
```
C:\mc\test-server\baslat.bat
```

---

**Son Güncelleme:** 2024-12-01
**Versiyon:** 10.0-RELEASE
**Minecraft:** 1.20.4 (Paper)
