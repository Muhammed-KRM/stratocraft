# Kontrat Sistemi Raporu

Bu rapor, kontrat sisteminin mevcut durumunu, GUI menüsünün nasıl çalıştığını ve hangi işlemlerin desteklendiğini detaylandırmaktadır.

## 1. Sistem Durumu

### ✅ Tamamlanan Özellikler

1. **Yeni Enum Entegrasyonu:**
   - `ContractType` enum'u kullanılıyor (RESOURCE_COLLECTION, CONSTRUCTION, COMBAT, TERRITORY)
   - `PenaltyType` enum'u kullanılıyor (HEALTH_PENALTY, BANK_PENALTY, MORTGAGE)
   - `ContractMenu` yeni enum'ları kullanıyor
   - `DataManager` yeni enum'ları kaydediyor ve yüklüyor

2. **Veritabanı Persistence:**
   - Kontratlar SQLite veritabanına kaydediliyor
   - Sunucu yeniden başlatıldığında kontratlar otomatik yükleniyor
   - `DataManager.loadContracts()` metodu yeni enum formatını destekliyor

3. **GUI Menü Sistemi:**
   - Kontrat oluşturma wizard'ı mevcut
   - Kategori seçimi menüsü çalışıyor
   - Ceza tipi seçim menüsü eklendi
   - Ödül ve ceza miktarı ayarlama menüleri mevcut
   - Süre belirleme menüleri (Gün/Saat/Dakika) mevcut

## 2. Kontrat Menüsü Nasıl Çalışır?

### 2.1. Kontrat Oluşturma Akışı

1. **Kategori Seçimi:**
   - Oyuncu `/contract create` komutunu kullanır veya menüden "Yeni Kontrat" butonuna tıklar
   - Açılan menüde 4 kategori seçeneği görünür:
     - **Kaynak Toplama** (RESOURCE_COLLECTION): Kaynak toplama kontratları
     - **İnşaat** (CONSTRUCTION): İnşaat kontratları
     - **Savaş** (COMBAT): Savaş kontratları (Öldürme, vurma)
     - **Bölge** (TERRITORY): Bölge kontratları (Gitme, gitmeme)

2. **Kapsam Seçimi:**
   - Kategori seçildikten sonra kapsam seçim menüsü açılır:
     - **Oyuncu → Oyuncu** (PLAYER_TO_PLAYER)
     - **Klan → Klan** (CLAN_TO_CLAN)
     - **Oyuncu → Klan** (PLAYER_TO_CLAN)
     - **Klan → Oyuncu** (CLAN_TO_PLAYER)

3. **Ödül Belirleme:**
   - Ödül miktarı slider menüsü ile ayarlanır
   - Artırma/Azaltma butonları ile ince ayar yapılabilir
   - Onay butonuna tıklanarak bir sonraki adıma geçilir

4. **Ceza Tipi Seçimi (YENİ):**
   - Ceza tipi seçim menüsü açılır:
     - **Can Cezası** (HEALTH_PENALTY): Kalıcı can kaybı
     - **Banka Cezası** (BANK_PENALTY): Bankadan item/para transferi
     - **Hipotek** (MORTGAGE): Belirli bir itemin silinmesi/transferi

5. **Ceza Miktarı Belirleme:**
   - Seçilen ceza tipine göre ceza miktarı slider menüsü ile ayarlanır
   - Artırma/Azaltma butonları ile ince ayar yapılabilir
   - Onay butonuna tıklanarak bir sonraki adıma geçilir

6. **Süre Belirleme:**
   - Süre seçim menüsü açılır:
     - **Gün** seçeneği: Gün bazında süre belirleme
     - **Saat** seçeneği: Saat bazında süre belirleme
     - **Dakika** seçeneği: Dakika bazında süre belirleme
   - Her seçenek için ayrı ayarlama menüsü açılır
   - Artırma/Azaltma butonları ile ince ayar yapılabilir

7. **Kategori'ye Özel Parametreler:**
   - **RESOURCE_COLLECTION:** Malzeme tipi ve miktarı belirlenir
   - **COMBAT:** Hedef oyuncu veya klan seçilir
   - **TERRITORY:** Yasak bölgeler ve yarıçap belirlenir
   - **CONSTRUCTION:** Yapı tipi belirlenir

8. **Kontrat Oluşturma:**
   - Tüm parametreler belirlendikten sonra "Kontrat Oluştur" butonuna tıklanır
   - Kontrat oluşturulur ve veritabanına kaydedilir
   - Oyuncuya başarı mesajı gösterilir

### 2.2. Kontrat Görüntüleme ve Yönetim

1. **Kontrat Listesi:**
   - Ana menüde "Kontratlarım" butonuna tıklanarak kontrat listesi açılır
   - Her kontrat için şu bilgiler gösterilir:
     - Kontrat tipi (kategori)
     - Ödül miktarı
     - Ceza tipi ve miktarı
     - Süre (kalan süre)
     - Durum (Açık, Kabul Edildi, Tamamlandı, İhlal Edildi)

2. **Kontrat Detayları:**
   - Kontrat item'ına sol tıklanarak detaylar görüntülenir
   - Detay menüsünde şu bilgiler gösterilir:
     - Veren (issuer) bilgisi
     - Kapsam (scope) bilgisi
     - Ödül ve ceza detayları
     - Süre bilgisi
     - Kategori'ye özel parametreler
     - Durum bilgisi

3. **Kontrat İşlemleri:**
   - **Kabul Et:** Açık kontratlar kabul edilebilir
   - **Reddet:** Açık kontratlar reddedilebilir
   - **İptal Et:** Kendi kontratlarınızı iptal edebilirsiniz
   - **Tamamla:** Kontrat şartlarını yerine getirdikten sonra tamamlanabilir

## 3. Desteklenen İşlemler

### 3.1. Kontrat Oluşturma
- ✅ Kategori seçimi (4 kategori)
- ✅ Kapsam seçimi (4 kapsam)
- ✅ Ödül miktarı belirleme
- ✅ Ceza tipi seçimi (3 tip)
- ✅ Ceza miktarı belirleme
- ✅ Süre belirleme (Gün/Saat/Dakika)
- ✅ Kategori'ye özel parametreler

### 3.2. Kontrat Görüntüleme
- ✅ Kontrat listesi görüntüleme
- ✅ Kontrat detayları görüntüleme
- ✅ Durum bilgisi görüntüleme

### 3.3. Kontrat Yönetimi
- ✅ Kontrat kabul etme
- ✅ Kontrat reddetme
- ✅ Kontrat iptal etme
- ✅ Kontrat tamamlama

### 3.4. Veritabanı İşlemleri
- ✅ Kontrat kaydetme (SQLite)
- ✅ Kontrat yükleme (sunucu başlangıcında)
- ✅ Kontrat güncelleme
- ✅ Kontrat silme

## 4. Teknik Detaylar

### 4.1. Dosya Yapısı

- **ContractMenu.java:** GUI menü sistemi
- **ContractManager.java:** Kontrat yönetim mantığı
- **DataManager.java:** Veritabanı kayıt/yükleme
- **Contract.java:** Kontrat modeli
- **ContractType.java:** Kontrat kategorileri enum'u
- **PenaltyType.java:** Ceza tipleri enum'u

### 4.2. Veri Akışı

1. **Kontrat Oluşturma:**
   ```
   ContractMenu → ContractManager.createContract() → Contract → DataManager.saveAll() → SQLite
   ```

2. **Kontrat Yükleme:**
   ```
   Main.onEnable() → DataManager.loadAll() → DataManager.loadContracts() → ContractManager.loadContract()
   ```

3. **Kontrat Güncelleme:**
   ```
   ContractManager → Contract → DataManager.saveAll() → SQLite
   ```

## 5. Kullanım Örnekleri

### 5.1. Kaynak Toplama Kontratı Oluşturma

1. `/contract create` komutunu kullan
2. "Kaynak Toplama" kategorisini seç
3. "Oyuncu → Oyuncu" kapsamını seç
4. Ödül miktarını belirle (örn: 1000 altın)
5. Ceza tipini seç (örn: Banka Cezası)
6. Ceza miktarını belirle (örn: 500 altın)
7. Süreyi belirle (örn: 7 gün)
8. Malzeme tipini seç (örn: DIAMOND)
9. Miktarı belirle (örn: 64)
10. "Kontrat Oluştur" butonuna tıkla

### 5.2. Savaş Kontratı (Bounty) Oluşturma

1. `/contract create` komutunu kullan
2. "Savaş" kategorisini seç
3. "Oyuncu → Oyuncu" kapsamını seç
4. Ödül miktarını belirle (örn: 5000 altın)
5. Ceza tipini seç (örn: Can Cezası)
6. Ceza miktarını belirle (örn: 2 kalp)
7. Süreyi belirle (örn: 14 gün)
8. Hedef oyuncuyu seç
9. "Kontrat Oluştur" butonuna tıkla

## 6. Sonuç

Kontrat sistemi tam olarak çalışıyor ve tüm özellikler destekleniyor. GUI menüsü kullanıcı dostu ve tüm işlemler menü üzerinden yapılabiliyor. Veritabanı persistence sistemi çalışıyor ve kontratlar sunucu yeniden başlatıldığında otomatik olarak yükleniyor.

