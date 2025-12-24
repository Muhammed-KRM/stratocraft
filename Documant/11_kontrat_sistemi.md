# STRATOCRAFT - KONTRAT SİSTEMİ

## 📜 Kontrat Sistemi Nedir?

Kontratlar, oyuncular arasında **koda dayalı** anlaşmalardır. Sözleşmeyi bozan otomatik cezalandırılır!

**GÜVENLİK**: Performans optimizasyonları (1 saniye cooldown) ve can kaybı geri kazanım sistemi eklendi.

**YENİ ÖZELLİK**: Çift taraflı kontrat sistemi ve wizard tabanlı kontrat oluşturma eklendi. Detaylı akış şeması için `KONTRAT_SISTEMI_AKIS_SEMASI.md` dosyasına bakın.

---

## 📋 İÇİNDEKİLER

1. [Kontrat Oluşturma](#kontrat-oluşturma)
2. [Kan İmzası](#kan-imzasi)
3. [Kontrat Tipleri](#kontrat-tipleri)
4. [İhlal ve Ceza](#ihlal-ve-ceza)

---

## 📝 KONTRAT OLUŞTURMA ⭐ YENİ WIZARD SİSTEMİ

### Yeni Kontrat Oluşturma (GUI Wizard)

**Kullanım**:
```
1. /kontrat komutunu kullan
2. Ana menüde "Yeni Kontrat Oluştur" butonuna tıkla
3. Wizard sistemi başlar (9 adım)
4. Her adımda şartları belirle
5. Özet menüsünde [ONAYLA VE GÖNDER] tıkla
6. İstek karşı tarafa gönderilir
```

**Wizard Adımları**:
- [Adım 1/9] Kontrat Tipi Seç
- [Adım 2/9] Kapsam Seç (PLAYER_TO_PLAYER, CLAN_TO_CLAN, vb.)
- [Adım 3/9] Hedef Oyuncu Seç (sadece PLAYER_TO_PLAYER için)
- [Adım 4/9] Ödül Belirle
- [Adım 5/9] Ceza Tipi Seç
- [Adım 6/9] Ceza Miktarı Belirle
- [Adım 7/9] Süre Belirle
- [Adım 8/9] Tip'e Özel Parametreler
- [Adım 9/9] Özet ve Onay

**Önemli Notlar**:
- Oyuncu seçildiğinde istek hemen gönderilmez
- Önce tüm şartlar belirlenir
- Şartlar belirlendikten sonra özet menüsünde [ONAYLA VE GÖNDER] tıklanır
- O zaman istek gönderilir ve karşı tarafa bildirim gider

---

### Eski Sistem (DEPRECATED) ⚠️

**NOT:** Aşağıdaki sistem artık kullanılmıyor. Yeni wizard sistemi kullanılmalı.

~~**Adım 1: Kontrat Kağıdı Hazırla**~~
~~**Adım 2: İhale Panosuna As**~~
~~**Adım 3: Kontrat Kabul**~~

---

## 🩸 KAN İMZASI

### Mekanik

**Her İki Taraf Kan İle İmzalar**:
```
1. Kontrat kabul edildi
2. Her iki oyuncu -3 kalp can kaybeder (Blood signature)
3. Kontrat AKTİF olur
4. Süre başlar
```

**Görsel Efektler**:
```
- BLOOD partikülü (kırmızı)
- "KONTRAT İMZALANDI!" title
- Sözleşme numarası verilir (#12345)
```

---

## 📋 KONTRAT TİPLERİ (4 Tip)

### 1. Kaynak Toplama Kontratı (RESOURCE_COLLECTION)

**Özellikler**:
```
ŞART: Belirli malzemeden belirli miktar getir
SÜRE: Belirlenen süre (gün/hafta/ay)
ÖDÜL: Belirlenen altın miktarı
CEZA: Belirlenen ceza tipi ve miktarı

Parametreler:
- Malzeme: Herhangi bir Minecraft malzemesi
- Miktar: Getirilmesi gereken miktar
```

**İşleyiş**:
```
1. Kontrat aktif olur
2. Alıcı belirlenen süre içinde malzemeyi toplar
3. /kontrat teslim komutu ile teslim eder
4. Sistem otomatik kontrol eder
5. Doğruysa → Ödül transfer
6. Yanlışsa/Süre bitti → Ceza
```

---

### 2. Savaş Kontratı (COMBAT)

**Özellikler**:
```
ŞART: Belirli oyuncuya/klana saldır veya öldür
SÜRE: Belirlenen süre (gün/hafta/ay)
ÖDÜL: Belirlenen altın miktarı
CEZA: Belirlenen ceza tipi ve miktarı

Parametreler:
- Hedef: Oyuncu veya Klan
```

**İşleyiş**:
```
1. Kontrat aktif olur
2. Hedef oyuncu/klana saldırılır veya öldürülür
3. Sistem otomatik kontrol eder
4. Başarılıysa → Ödül transfer
5. Başarısızsa/Süre bitti → Ceza
```

---

### 3. Bölge Yasağı Kontratı (TERRITORY)

**Özellikler**:
```
ŞART: Belirli bölgelere girme
SÜRE: Belirlenen süre (gün/hafta/ay)
ÖDÜL: Belirlenen altın miktarı
CEZA: Belirlenen ceza tipi ve miktarı

Parametreler:
- Yasak Bölgeler: Lokasyon listesi
- Yarıçap: Her bölge için yarıçap (blok)
```

**İşleyiş**:
```
1. Kontrat aktif olur
2. Yasak bölgeler belirlenir (koordinat + yarıçap)
3. Oyuncu yasak bölgeye girerse → İhlal
4. Otomatik ceza uygulanır
5. Süre bitene kadar yasak bölgelere girilmezse → Ödül
```

---

### 4. Yapı İnşa Kontratı (CONSTRUCTION)

**Özellikler**:
```
ŞART: Belirli yapıyı inşa et
SÜRE: Belirlenen süre (gün/hafta/ay)
ÖDÜL: Belirlenen altın miktarı
CEZA: Belirlenen ceza tipi ve miktarı

Parametreler:
- Yapı Tipi: Belirlenen yapı tipi
```

**İşleyiş**:
```
1. Kontrat aktif olur
2. Yapı tipi belirlenir
3. Yapı inşa edilirse → Tamamlandı
4. Sistem otomatik kontrol eder
5. Başarılıysa → Ödül transfer
6. Başarısızsa/Süre bitti → Ceza
```

---

## ⚖️ İHLAL VE CEZA

### İhlal Durumları

```
1. Süre Bitti + Şart Yerine Gelmedi
   → Otomatik ihlal

2. Yanlış Malzeme Verildi
   → İhlal

3. Kanıt Sunulamadı
   → İhlal

4. Koruma Başarısız
   → İhlal
```

---

### Otomatik Ceza Sistemi

#### **1. Hain Damgası (Traitor Tag)**

```
İsim Rengi KIRMIZI olur:
§c[HAIN] Oyuncu_Adı

Etki:
- Herkes görür
- Kimse güvenmez
- 7 gün sürer (veya tazminat ödeyene kadar)
```

---

#### **2. Otomatik Tazminat**

```
Bankadan Otomatik Çekilir:

Örnek:
Kontrat: 1000 Altın ödül
İhlal: 1500 Altın tazminat

Sistem:
1. İhlal eden oyuncunun bankasını kontrol et
2. 1500 Altın varsa → Otomatik çek
3. Karşı tarafa transfer
4. Mesaj: "Tazminat ödendi."
```

---

#### **3. Kalıcı Can Kaybı** (Permanent Health Loss)

```
İhlal eden oyuncu:
- Maksimum can -2 kalp (kalıcı)
- Attribute modifier ile uygulanır
- Oyuncu giriş yaptığında otomatik uygulanır
- Tazminat ödense bile can geri gelmez
```

---

#### **4. Envanter Kilidi** (Para Yoksa)

```
Durum: Bankada para yok ama ihlal var

Ceza:
1. Envanteri KİLİTLENİR
2. Hiçbir şey düşüremez/kullanamaz
3. Sadece madencilik/loglama yapabilir
4. Kazandığı para otomatik kesilir
5. Tazminat tamam olana kadar sürer
```

**Görsel**:
```
Her login:
"§cKONTRAT BORCU: 1500 Altın kaldı!"
"§7Envanterin kilitli. Borcu öde."
```

---

## 🎯 KONTRAT STRATEJİLERİ

### Güvenli Kontrat Yazma

**İPUÇLARI**:
```
1. NET ŞART: "64 Titanyum" (belirsiz değil)
2. AÇIK SÜRE: "3 gün" (3 gün 0 saat 0 dakika)
3. TAZMİNAT EKLE: "İhlal: 1.5x ödül tazminat"
4. KANIT BELİRT: "Kanıt: Malzeme board'a koyulmalı"
```

---

### Tüccar Oyuncu İçin

**Para Kazanma**:
```
Strateji: Malzeme kontratları al

1. Contract board'ları tara
2. Kolay kontratları seç:
   - "64 Demir - 1 gün - 50 Altın"
   - "32 Odun - 2 saat - 20 Altın"
3. Hızlıca topla
4. Teslim et
5. Tekrarla

Günlük kazanç: 500-1000 Altın (safe)
```

---

### Klan İçin

**Büyük Kontratlar**:
```
Klan kontratı as:
"1000 Titanyum - 7 gün - 10,000 Altın"

Tek oyuncuya zor ama:
→ Takım halinde teslim edilir
→ Kar paylaşımı
→ Klan zenginleşir
```

---

## ⚠️ ÖNEMLİ NOTLAR

### Kontrat Kuralları

1. **İptal Edilemez**: İmzaladıktan sonra iptal YOK
2. **Kan Gerekli**: -3 kalp can kaybı (hazır ol)
3. **Tazminat Zorunlu**: İhlal = Otomatik ceza
4. **Hain Tag**: 7 gün boyunca kırmızı isim
5. **Envanter Kilidi**: Borç bitene kadar kilitli
6. **Çift Taraflı Kontrat**: Her iki tarafın şartları belirlenir ve onaylanır
7. **Final Onay**: Sender son onayı verir, her iki taraf onayladığında kontrat aktif olur

---

### Güvenlik İpuçları

**Kontrat Almadan Önce**:
```
1. Şartları DİKKATLE oku
2. Süreyi kontrol et (yetişir mi?)
3. Tazminatı gör (ödeyebilir misin?)
4. Karşı tarafa güven (scam riski)
```

**Kontrat Verirken**:
```
1. NET yaz (belirsizlik yok)
2. Ödül = Makul (çok yüksek verme)
3. Tazminat = Caydırıcı (1.5x-2x)
4. Kanıt iste (screenshot, item vb.)
```

---

## 🎯 HIZLI KONTRAT REHBERİ

### Basit Malzeme Kontratı Oluşturma

```
1. /kontrat komutunu kullan
2. "Yeni Kontrat Oluştur" butonuna tıkla
3. [Adım 1/9] RESOURCE_COLLECTION seç
4. [Adım 2/9] PLAYER_TO_PLAYER seç
5. [Adım 3/9] Hedef oyuncuyu seç
6. [Adım 4/9] Ödül belirle (örn: 1000 Altın)
7. [Adım 5/9] Ceza tipi seç (CASH)
8. [Adım 6/9] Ceza miktarı belirle (örn: 500 Altın)
9. [Adım 7/9] Süre belirle (örn: 7 Gün)
10. [Adım 8/9] Malzeme seç (örn: Elmas) ve miktar (örn: 64)
11. [Adım 9/9] Özet menüsünde [ONAYLA VE GÖNDER] tıkla
12. ✅ İstek gönderildi! Karşı taraf kabul ettiğinde bildirim alacaksınız.
```

### Kontrat İsteği Kabul Etme

```
1. /kontrat komutunu kullan
2. "Gelen İstekler" butonuna tıkla (bildirim varsa)
3. İstek listesinde gönderenin şartlarını gör
4. İki seçenek:
   
   SEÇENEK 1: [✅ Kabul Et (Direkt)]
   - Sol tıkla
   - Sender'ın şartlarını direkt kabul edersiniz
   - Sender'a son onay mesajı gider
   
   SEÇENEK 2: [➕ Şart Ekle]
   - Orta tıkla
   - Kendi şartlarınızı belirlersiniz
   - Wizard sistemi başlar
   - Şartlarınızı belirleyip onaylarsınız
   - Sender'a son onay mesajı gider
```

---

## 🖥️ GUI MENÜ SİSTEMİ

### Ana Kontrat Menüsü (54 Slot)

**Özellikler**:
```
- Sayfalama: Her sayfada 45 kontrat
- Kontrat ikonları: Tip'e göre farklı materyaller
- Detay görüntüleme: Kontrata tıkla → Detay menüsü
- Önceki/Sonraki sayfa butonları
- Gelen İstekler: Yeni kontrat istekleri bildirimi
- Atılan İstekler: Gönderdiğiniz isteklerin durumu
```

**Kontrat İkonları**:
```
- RESOURCE_COLLECTION → Chest (malzeme toplama)
- COMBAT → Diamond Sword (savaş)
- TERRITORY → Barrier (bölge yasağı)
- CONSTRUCTION → Structure Block (yapı inşaatı)
```

### Kontrat Oluşturma Wizard Sistemi ⭐ YENİ

**Özellikler**:
```
- Adım adım kontrat oluşturma
- Her menüde adım numarası gösterilir (örn: [Adım 4/9])
- Açıklayıcı bilgi mesajları
- Her menüde [GERİ] ve [İPTAL] butonları
- Şartlar belirlendikten sonra istek gönderilir
```

**Wizard Adımları**:
```
1. [Adım 1/9] Kontrat Tipi Seç
   - RESOURCE_COLLECTION
   - COMBAT
   - TERRITORY
   - CONSTRUCTION

2. [Adım 2/9] Kapsam Seç
   - PLAYER_TO_PLAYER (Personal Terminal'den sadece bu)
   - CLAN_TO_CLAN
   - PLAYER_TO_CLAN
   - CLAN_TO_PLAYER

3. [Adım 3/9] Hedef Oyuncu Seç
   - Online oyuncular listesi
   - Chat input desteği
   - ℹ️ Bilgi: Oyuncu seçildikten sonra şartlar belirlenecek

4. [Adım 4/9] Ödül Belirle
   - Slider menü
   - Hızlı değerler (100, 500, 1000, 5000)
   - Artırma/Azaltma butonları

5. [Adım 5/9] Ceza Tipi Seç
   - CASH (Altın)
   - HEALTH (Can kaybı)
   - ITEM (Eşya)

6. [Adım 6/9] Ceza Miktarı Belirle
   - Slider menü
   - Ödülün yüzdesi seçenekleri

7. [Adım 7/9] Süre Belirle
   - Gün/Hafta/Ay seçimi
   - Detaylı zaman ayarlama

8. [Adım 8/9] Tip'e Özel Parametreler
   - RESOURCE_COLLECTION → Malzeme + Miktar
   - COMBAT → Hedef oyuncu/klan
   - TERRITORY → Lokasyon + Yarıçap
   - CONSTRUCTION → Yapı tipi

9. [Adım 9/9] Özet ve Onay
   - Sizin şartlarınız gösterilir
   - Karşı tarafın şartları (eğer varsa) gösterilir
   - [ONAYLA VE GÖNDER] butonu
   - [İPTAL] butonu
```

### Çift Taraflı Kontrat Sistemi ⭐ YENİ

**Akış**:

#### İlk Gönderen (Sender) Akışı:
```
1. Kontrat oluşturma wizard'ını başlat
2. Tip, kapsam, oyuncu seç
3. Şartları belirle (ödül, ceza, süre, tip'e özel)
4. Özet menüsünde [ONAYLA VE GÖNDER] tıkla
5. ✅ İstek gönderilir (ContractRequest oluşturulur)
6. ✅ Sender'ın şartları kaydedilir (ContractTerms)
7. ✅ Sender'ın şartları otomatik onaylanır
8. ✅ Target oyuncuya bildirim gönderilir
```

#### Hedef Oyuncu (Target) Akışı:
```
1. [Gelen İstekler] menüsünde isteği gör
2. Gönderenin şartları gösterilir
3. İki seçenek:
   
   SEÇENEK 1: [✅ Kabul Et (Direkt)]
   - Sender'ın şartlarını direkt kabul eder
   - Sender'a "Son Onay Gerekiyor" mesajı gider
   - Sender'a Final Onay Menüsü açılır
   
   SEÇENEK 2: [➕ Şart Ekle]
   - Şart belirleme wizard'ı başlar
   - Target kendi şartlarını belirler
   - Özet menüsünde:
     * Sizin şartlarınız gösterilir
     * Karşı tarafın şartları gösterilir
   - [ONAYLA] tıkla
   - ✅ Target'ın şartları kaydedilir
   - ✅ Sender'a "Son Onay Gerekiyor" mesajı gider
```

#### Sender'ın Son Onay Akışı:
```
1. [Final Onay Menüsü] otomatik açılır
2. Her iki tarafın şartları yan yana gösterilir:
   - 📋 SİZİN ŞARTLARINIZ
   - 📋 KARŞI TARAFIN ŞARTLARI
3. İki seçenek:
   
   SEÇENEK 1: [✅ ONAYLA]
   - ✅ Bilateral Contract oluşturulur
   - ✅ Her iki oyuncuya bildirim: "Kontrat aktif oldu!"
   
   SEÇENEK 2: [❌ REDDET]
   - ❌ İstek iptal edilir
   - ❌ Target'a bildirim: "Kontrat reddedildi."
```

### Final Onay Menüsü (54 Slot) ⭐ YENİ

**Özellikler**:
```
- Daha büyük menü (54 slot = 6x9)
- Her iki tarafın şartları yan yana gösterilir
- Açıklayıcı başlık: "⚠️ SON ONAY GEREKİYOR!"
- Slot 20: Sizin Şartlarınız (sol taraf)
- Slot 24: Karşı Tarafın Şartları (sağ taraf)
- Slot 22: [✅ ONAYLA] butonu (ortada)
- Slot 40: [❌ REDDET] butonu
- Slot 0: [GERİ] butonu
```

### Detay Menüsü (27 Slot)

**Özellikler**:
```
- Slot 13: Kontrat bilgileri (tip, issuer, ödül, ceza, süre)
- Slot 11: "Kabul Et" butonu (yeşil emerald block)
- Slot 15: "Reddet" butonu (kırmızı redstone block)
- Slot 22: "Geri" butonu (ana menüye dön)
```

**Kontrat Bilgileri**:
```
- Tip: Kontrat tipi (Türkçe)
- Issuer: Kontratı veren oyuncu
- Ödül: Para miktarı
- Ceza: İhlal cezası
- Süre: Kalan süre (gün/saat/dakika)
- Tip'e özel bilgiler (hedef, malzeme, vb.)
```

---

## 🎮 KOMUT SİSTEMİ

### `/kontrat` Komutu

**Kullanım**:
```
/kontrat
```

**Özellikler**:
- GUI menü açar
- Aktif kontratları listeler
- Sayfalama desteği
- Detay görüntüleme
- Yeni kontrat oluşturma wizard'ı
- Gelen/Atılan istekleri görüntüleme

### Personal Terminal Entegrasyonu ⭐ YENİ

**Özellikler**:
```
- Personal Terminal'den kontrat oluşturma
- Sadece PLAYER_TO_PLAYER kontratları yapılabilir
- Klan kontratları için CONTRACT_OFFICE yapısı gerekli
```

## 🔄 ÇİFT TARAFLI KONTRAT AKIŞI ⭐ YENİ

### Akış Şeması

Detaylı akış şeması için: `KONTRAT_SISTEMI_AKIS_SEMASI.md` dosyasına bakın.

**Özet**:
1. **Sender**: Kontrat oluştur → Şartları belirle → İstek gönder
2. **Target**: İsteği gör → Kabul et veya şart ekle
3. **Sender**: Final onay ver → Kontrat aktif olur

### Önemli Özellikler

**Oyuncu Seçimi**:
- Oyuncu seçildiğinde istek hemen gönderilmez
- Önce şartlar belirlenir
- Şartlar belirlendikten sonra özet menüsünde [ONAYLA VE GÖNDER] tıklanır
- O zaman istek gönderilir

**Şart Ekleme**:
- Target şart eklerken kendi tipini seçebilir
- Her iki taraf farklı kontrat tiplerinde anlaşabilir
- Örnek: Sender "64 elmas getir" (RESOURCE_COLLECTION), Target "1000 altın öde" (COMBAT)

**Final Onay**:
- Her iki tarafın şartları yan yana gösterilir
- Sender son onayı verir
- Her iki taraf onayladığında kontrat aktif olur

---

## 🔄 KONTRAT KAPSAMI (Scope)

### Oyuncu → Oyuncu (PLAYER_TO_PLAYER)
```
İki oyuncu arasında bireysel kontrat
```

### Klan → Klan (CLAN_TO_CLAN)
```
İki klan arasında toplu kontrat
```

### Oyuncu → Klan (PLAYER_TO_CLAN)
```
Bir oyuncu bir klanla kontrat yapar
```

### Klan → Oyuncu (CLAN_TO_PLAYER)
```
Bir klan bir oyuncuyla kontrat yapar
```

---

## 🔒 GÜVENLİK VE PERFORMANS

### Performans Optimizasyonları

**Bölge Yasağı Kontrolü**:
- **1 saniye cooldown**: Spam önleme için kontrol sıklığı sınırlandırıldı
- **Blok değişimi kontrolü**: Sadece blok değiştiğinde kontrol yapılır
- **Cache kullanımı**: Kontrat listesi cache'den okunur

### Otomatik Temizleme Sistemi ⭐ YENİ

**Süresi Dolmuş İstekler**:
- **Otomatik temizleme**: Her 1 saatte bir süresi dolmuş kontrat istekleri otomatik temizlenir
- **Sistem**: `Main.java` içinde scheduled task olarak çalışır
- **Aralık**: 72000 tick (1 saat = 72000 tick)
- **Sonuç**: Süresi dolmuş istekler listeden kaldırılır, performans iyileşir

**Nasıl Çalışır?**:
```
1. Sistem her 1 saatte bir kontrol eder
2. Kontrat isteklerinin süresini kontrol eder
3. Süresi dolmuş istekleri bulur
4. Otomatik olarak iptal eder ve listeden kaldırır
5. Log'a kaydeder: "X adet süresi dolmuş kontrat isteği temizlendi"
```

### Can Kaybı Sistemi

**Kan İmzası Mekaniği**:
```
Kontrat İmzalanınca: -3 kalp (kan imzası)
Kontrat Tamamlanınca: +1 kalp geri (kan imzası geri ödeniyor)
Kontrat İhlal Edilince: -2 kalp kalıcı (ceza, geri verilmez)
```

**Örnek Senaryo**:
```
1. Oyuncu 3 kontrat imzalar: -9 kalp (11 kalp kaldı)
2. 2 kontrat tamamlar: +2 kalp (13 kalp)
3. 1 kontrat ihlal eder: -2 kalp kalıcı (11 kalp, geri verilmez)
4. Sonuç: 11 kalp maksimum can
```

**Önemli**: 
- Can kaybı kalıcıdır (ihlal cezası)
- Kan imzası geri ödenir (kontrat tamamlandığında)
- Maksimum can 1 kalpe kadar düşebilir (oyun oynanamaz hale gelir)

---

## 🔧 BUGÜN YAPILAN DÜZELTMELER ⭐ YENİ

### GUI Tıklama Sorunu Düzeltildi

**Sorun:**
- Kontrat menülerinde butonlar tıklanmıyordu
- Oyuncular item'ları envanterlerine alabiliyordu
- PersonalTerminalListener'daki gibi çalışmıyordu

**Çözüm:**
- `onMenuClick()` metodunda `title.contains()` kullanılarak dinamik başlıklar desteklendi
- `event.setCancelled(true)` eklendi GUI tıklamaları için
- `cancelIfGUIClick()` helper metodu eklendi
- PersonalTerminalListener'daki mantık uygulandı

**Kod Değişiklikleri:**
```java
@EventHandler(priority = EventPriority.HIGH)
public void onMenuClick(InventoryClickEvent event) {
    // ✅ ÖNEMLİ: Sadece GUI'ye tıklanırsa işle, oyuncu envanterine tıklanırsa atla
    if (event.getClickedInventory() != null && 
        event.getClickedInventory().equals(event.getView().getBottomInventory())) {
        // Oyuncu envanterine tıklandı - bu event'i işleme (item taşıma için izin ver)
        return;
    }
    
    String title = event.getView().getTitle();
    
    // ✅ ÖNEMLİ: Kontrat menülerinde GUI tıklamalarını iptal et (item alınmasını önle)
    // PersonalTerminalListener'daki gibi çalışır
    if (event.getClickedInventory() != null && 
        event.getClickedInventory().equals(event.getView().getTopInventory())) {
        // GUI'ye tıklandı - event'i iptal et
        event.setCancelled(true);
    }
    
    // Dinamik başlık kontrolü (title.contains() kullanılıyor)
    if (title.contains("Kontrat Tipi Seç") || title.contains("Kontrat Kategorisi Seç")) {
        handleTypeSelectionClick(event);
    }
    else if (title.contains("Ceza Tipi Seç")) {
        handlePenaltyTypeSelectionClick(event);
    }
    // ... diğer menüler
}

/**
 * ✅ YARDIMCI: GUI'ye tıklanıp tıklanmadığını kontrol et ve event'i iptal et
 * Oyuncu envanterine tıklanırsa false döner (item taşıma için izin ver)
 */
private boolean cancelIfGUIClick(InventoryClickEvent event) {
    if (event.getClickedInventory() != null && 
        event.getClickedInventory().equals(event.getView().getTopInventory())) {
        // GUI'ye tıklandı - iptal et
        event.setCancelled(true);
        return true;
    }
    // Oyuncu envanterine tıklandı - izin ver (item taşıma için)
    return false;
}
```

**Sonuç:**
- ✅ Butonlar artık tıklanabilir
- ✅ Item'lar envantere alınamaz
- ✅ PersonalTerminalListener ile aynı mantık
- ✅ Dinamik başlıklar destekleniyor

---

## 📊 GÜNCEL AKIŞ ŞEMASI ⭐ YENİ

Detaylı akış şeması için: `KONTRAT_SISTEMI_AKIS_SEMASI.md` dosyasına bakın.

### Özet Akış

**1. İlk Gönderen (Sender) Akışı:**
```
[Ana Menü] → [Yeni Kontrat Oluştur]
    ↓
[Adım 1/9] Kapsam Seç (PLAYER_TO_PLAYER, CLAN_TO_CLAN, vb.)
    ↓
[Adım 2/9] Hedef Oyuncu Seç (sadece PLAYER_TO_PLAYER için)
    ↓
[Adım 3/9] Kontrat Tipi Seç (RESOURCE_COLLECTION, COMBAT, TERRITORY, CONSTRUCTION)
    ↓
[Adım 4/9] Ödül Belirle (veya direkt onayla - ödül yok)
    ↓
[Adım 5/9] Ceza Tipi Seç (HEALTH_PENALTY, BANK_PENALTY, MORTGAGE)
    ↓
[Adım 6/9] Ceza Miktarı Belirle (veya direkt onayla - ceza yok)
    ↓
[Adım 7/9] Süre Belirle (Gün/Hafta/Ay)
    ↓
[Adım 8/9] Tip'e Özel Parametreler (Malzeme, Hedef, Lokasyon, Yapı Tipi)
    ↓
[Adım 9/9] Kontrat Özeti → [ONAYLA VE GÖNDER]
    ↓
✅ İstek gönderilir (ContractRequest oluşturulur)
✅ Sender'ın şartları kaydedilir (ContractTerms)
✅ Target oyuncuya bildirim gönderilir
```

**2. Hedef Oyuncu (Target) Akışı:**
```
[Ana Menü] → [Gelen İstekler] (Bildirim: "Yeni istek var!")
    ↓
[Gelen İstekler Menüsü]
    ↓
İki Seçenek:
    ├─ [✅ Kabul Et] → Sender'a bildirim: "Son onay gerekiyor"
    └─ [➕ Şart Ekle] → [Tip Seçimi] → [Şartlar] → [Özet] → [ONAYLA]
        ↓
    ✅ Target'ın şartları kaydedilir
    ✅ Sender'a bildirim: "Şartlar belirlendi! Son onay gerekiyor"
```

**3. Sender'ın Son Onay Akışı:**
```
[Final Onay Menüsü] (Otomatik açılır)
    ↓
Her iki tarafın şartları gösterilir:
    ├─ 📋 SİZİN ŞARTLARINIZ
    └─ 📋 KARŞI TARAFIN ŞARTLARI
    ↓
İki Seçenek:
    ├─ [✅ ONAYLA] → ✅ Bilateral Contract oluşturulur
    └─ [❌ REDDET] → ❌ İstek iptal edilir
```

### Önemli Özellikler

**✅ Dinamik Başlık Desteği:**
- Menü başlıkları `title.contains()` ile kontrol ediliyor
- Adım numaraları dinamik olarak gösteriliyor: `[Adım X/Y]`
- Örnek: `"§6[Adım 3/9] Kontrat Tipi Seç"`

**✅ GUI Tıklama Koruması:**
- GUI'ye tıklandığında `event.setCancelled(true)` ile iptal ediliyor
- Oyuncu envanterine tıklandığında izin veriliyor (item taşıma için)
- `cancelIfGUIClick()` helper metodu ile kontrol ediliyor

**✅ Ödül/Ceza Mantığı:**
- En az birini belirlemek zorunda (Ödül VEYA Ceza)
- Direkt onaylarsa null olur (ama en az biri zorunlu)
- Özet menüsünde kontrol yapılır

**✅ Şart Ekleme Durumu:**
- `contractRequestId` varsa → Şart ekleme durumu
- Scope ve oyuncu seçimi **ATLANIR**
- Direkt **Tip Seçimi** menüsüne gidilir

---

**🎮 Kontratlarla ticaret yap, güvenilir ol, zengin ol!**
