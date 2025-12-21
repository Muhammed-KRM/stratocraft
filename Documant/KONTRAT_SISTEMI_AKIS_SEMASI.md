# KONTRAT SİSTEMİ AKIŞ ŞEMASI

## MEVCUT AKIŞ (Kod Analizi Sonrası)

### 1. İLK GÖNDEREN OYUNCU (SENDER) AKIŞI

```
[Ana Menü]
    ↓
[Yeni Kontrat Oluştur] tıkla
    ↓
[Tip Seçimi]
    ├─ RESOURCE_COLLECTION
    ├─ COMBAT
    ├─ TERRITORY
    └─ CONSTRUCTION
    ↓
[Kapsam Seçimi]
    ├─ PLAYER_TO_PLAYER (Personal Terminal'den sadece bu)
    ├─ CLAN_TO_CLAN
    ├─ PLAYER_TO_CLAN
    └─ CLAN_TO_PLAYER
    ↓
[Oyuncu Seçimi] (Sadece state'e kaydedilir, istek gönderilmez)
    ├─ Online oyuncular listesi
    └─ Chat input (oyuncu adı yaz)
    ↓
[Ödül Belirleme] (Slider menü)
    ↓
[Ceza Tipi Seçimi]
    ├─ CASH
    ├─ HEALTH
    └─ ITEM
    ↓
[Ceza Miktarı Belirleme] (Slider menü)
    ↓
[Süre Belirleme] (Gün/Hafta/Ay seçimi)
    ↓
[Tip'e Özel Parametreler]
    ├─ RESOURCE_COLLECTION → Malzeme + Miktar
    ├─ COMBAT → Hedef oyuncu/klan
    ├─ TERRITORY → Lokasyon + Yarıçap
    └─ CONSTRUCTION → Yapı tipi
    ↓
[Özet Menüsü]
    ├─ Tüm şartları göster
    ├─ [ONAYLA] butonu
    └─ [İPTAL] butonu
    ↓
[ONAYLA] tıkla
    ↓
✅ İstek gönderilir (ContractRequest oluşturulur)
✅ Sender'ın şartları kaydedilir (ContractTerms)
✅ Sender'ın şartları otomatik onaylanır
✅ Target oyuncuya bildirim gönderilir
```

### 2. HEDEF OYUNCU (TARGET) AKIŞI

```
[Ana Menü]
    ↓
[Gelen İstekler] tıkla
    ↓
[Gelen İstekler Listesi]
    ├─ Her istek için:
    │   ├─ Gönderen adı
    │   ├─ Gönderenin şartları (gösterilir)
    │   ├─ [Sol Tık: Kabul Et (Direkt)]
    │   ├─ [Orta Tık: Şart Ekle]
    │   └─ [Sağ Tık: Reddet]
    ↓
┌─────────────────────────────────────┐
│   SEÇENEK 1: SOL TIK (Direkt Kabul) │
└─────────────────────────────────────┘
    ↓
✅ İstek kabul edilir
✅ Sender'ın şartları direkt kabul edilir
✅ Sender'a "Son Onay Gerekiyor" mesajı gider
✅ Sender'a Final Onay Menüsü açılır
    ↓
[SON DURUM: Sender'ın onayı bekleniyor]

┌─────────────────────────────────────┐
│   SEÇENEK 2: ORTA TIK (Şart Ekle)   │
└─────────────────────────────────────┘
    ↓
✅ İstek kabul edilir
✅ Şart belirleme wizard'ı başlar
    ↓
[Tip Seçimi] (Target kendi tipini seçer)
    ↓
[Ödül Belirleme] (Target kendi ödülünü belirler)
    ↓
[Ceza Tipi Seçimi]
    ↓
[Ceza Miktarı Belirleme]
    ↓
[Süre Belirleme]
    ↓
[Tip'e Özel Parametreler]
    ↓
[Özet Menüsü]
    ├─ Target'ın şartlarını göster
    ├─ [ONAYLA] butonu
    └─ [İPTAL] butonu
    ↓
[ONAYLA] tıkla
    ↓
✅ Target'ın şartları kaydedilir
✅ Target'ın şartları otomatik onaylanır
✅ Sender'a "Son Onay Gerekiyor" mesajı gider
✅ Sender'a Final Onay Menüsü açılır
    ↓
[SON DURUM: Sender'ın onayı bekleniyor]
```

### 3. SENDER'IN SON ONAY AKIŞI

```
[Final Onay Menüsü] (Otomatik açılır)
    ├─ Target'ın şartlarını göster
    ├─ [ONAYLA] butonu
    └─ [REDDET] butonu
    ↓
┌─────────────────────────────────────┐
│   SEÇENEK 1: ONAYLA                  │
└─────────────────────────────────────┘
    ↓
✅ Her iki tarafın şartları onaylanmış
✅ Bilateral Contract oluşturulur
✅ Her iki oyuncuya bildirim gönderilir
✅ Kontrat aktif olur

┌─────────────────────────────────────┐
│   SEÇENEK 2: REDDET                  │
└─────────────────────────────────────┘
    ↓
❌ İstek iptal edilir
❌ Tüm şartlar silinir
❌ Target'a bildirim gönderilir
```

## SORUNLAR VE İYİLEŞTİRME ÖNERİLERİ

### 🔴 SORUN 1: Target Tip Seçimi
**Problem:** Target şart eklerken kendi tipini seçiyor. Bu mantıklı mı?
- Sender RESOURCE_COLLECTION seçmiş
- Target COMBAT seçebilir mi? Bu mantıklı mı?

**Çözüm:** Target'ın tip seçmesi mantıklı. Her iki taraf farklı kontrat tiplerinde anlaşabilir.
- Örnek: Sender "Bana 64 elmas getir" (RESOURCE_COLLECTION)
- Target "Sen bana 1000 altın öde" (COMBAT veya başka bir tip)

**Ancak:** Menüde daha açıklayıcı olmalı:
- "Sender RESOURCE_COLLECTION seçti, siz farklı bir tip seçebilirsiniz"

### 🔴 SORUN 2: Menü Başlıkları ve Açıklamalar
**Problem:** Oyuncu hangi adımda olduğunu anlamıyor.

**Çözüm:** Her menüde:
- Adım numarası göster (örn: "Adım 2/8: Ödül Belirle")
- Ne yapması gerektiğini açıkla
- Geri butonu her zaman olsun
- İptal butonu her zaman olsun

### 🔴 SORUN 3: Özet Menüsünde Her İki Tarafın Şartları
**Problem:** Target şartlarını belirledikten sonra özet menüsünde sadece kendi şartlarını görüyor.
Sender'ın şartlarını da görmeli.

**Çözüm:** Özet menüsünde:
- "Sizin Şartlarınız" bölümü
- "Karşı Tarafın Şartları" bölümü (eğer varsa)
- Her iki tarafın şartları yan yana karşılaştırılabilir şekilde

### 🔴 SORUN 4: Final Onay Menüsü
**Problem:** Sender final onay menüsünde sadece target'ın şartlarını görüyor.
Kendi şartlarını da görmeli.

**Çözüm:** Final onay menüsünde:
- "Sizin Şartlarınız" bölümü
- "Karşı Tarafın Şartları" bölümü
- Her iki tarafın şartları yan yana

### 🔴 SORUN 5: İptal Mekanizması
**Problem:** Wizard sırasında iptal etmek zor.

**Çözüm:** Her menüde:
- [İPTAL] butonu (kırmızı)
- İptal edildiğinde state temizlensin
- Oyuncuya bilgi mesajı gönderilsin

### 🔴 SORUN 6: Geri Butonu
**Problem:** Geri butonu her menüde yok veya tutarsız.

**Çözüm:** Her menüde:
- [GERİ] butonu (ok ikonu)
- Geri gidildiğinde önceki adıma dönülsün
- State korunsun (sadece adım değişsin)

### 🔴 SORUN 7: Chat Input İptal
**Problem:** Chat input beklerken iptal etmek zor.

**Çözüm:** 
- `/iptal` komutu her zaman çalışsın
- Chat input beklerken menüyü açabilme (eğer mümkünse)

### 🔴 SORUN 8: Bildirimler
**Problem:** Oyuncu ne olduğunu anlamıyor.

**Çözüm:**
- Her adımda açıklayıcı mesajlar
- HUD bildirimleri daha açıklayıcı
- Başarı/hata mesajları net

## İYİLEŞTİRİLMİŞ AKIŞ ŞEMASI

### 1. İLK GÖNDEREN OYUNCU (SENDER) - İYİLEŞTİRİLMİŞ

```
[Ana Menü]
    ↓
[Yeni Kontrat Oluştur] tıkla
    ↓
┌─────────────────────────────────────────┐
│ [Adım 1/8] Kontrat Tipi Seç            │
│                                         │
│ RESOURCE_COLLECTION - Kaynak Toplama    │
│ COMBAT - Savaş                          │
│ TERRITORY - Bölge                       │
│ CONSTRUCTION - İnşaat                   │
│                                         │
│ [GERİ] [İPTAL]                         │
└─────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────┐
│ [Adım 2/8] Kapsam Seç                   │
│                                         │
│ PLAYER_TO_PLAYER - Oyuncu → Oyuncu     │
│ (Diğer seçenekler...)                    │
│                                         │
│ [GERİ] [İPTAL]                         │
└─────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────┐
│ [Adım 3/8] Hedef Oyuncu Seç            │
│                                         │
│ ℹ️ Oyuncu seçildikten sonra şartları   │
│    belirleyeceksiniz. İstek şartlar     │
│    belirlendikten sonra gönderilecek.  │
│                                         │
│ [Oyuncu Listesi]                       │
│ [GERİ] [İPTAL]                         │
└─────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────┐
│ [Adım 4/8] Ödül Belirle                │
│                                         │
│ Mevcut: 100 Altın                       │
│ [Slider] [Hızlı Değerler]              │
│                                         │
│ [GERİ] [İPTAL] [İLERİ]                 │
└─────────────────────────────────────────┘
    ↓
[Ceza Tipi] → [Ceza Miktarı] → [Süre] → [Tip'e Özel]
    ↓
┌─────────────────────────────────────────┐
│ [Adım 8/8] Özet ve Onay                 │
│                                         │
│ 📋 SİZİN ŞARTLARINIZ:                  │
│    Tip: RESOURCE_COLLECTION            │
│    Ödül: 1000 Altın                    │
│    Ceza: 500 Altın                     │
│    Süre: 7 Gün                         │
│    Malzeme: Elmas x64                  │
│                                         │
│ ℹ️ Bu şartlar karşı tarafa gönderilecek │
│    Karşı taraf kabul ederse kontrat     │
│    aktif olacak.                       │
│                                         │
│ [GERİ] [İPTAL] [ONAYLA VE GÖNDER]      │
└─────────────────────────────────────────┘
    ↓
✅ İstek gönderilir
✅ Bildirim: "İstek gönderildi! Karşı taraf kabul ettiğinde bildirim alacaksınız."
```

### 2. HEDEF OYUNCU (TARGET) - İYİLEŞTİRİLMİŞ

```
[Ana Menü]
    ↓
[Gelen İstekler] tıkla (Bildirim: "3 yeni istek var!")
    ↓
┌─────────────────────────────────────────┐
│ Gelen İstekler (Sayfa 1)                │
│                                         │
│ [İstek 1]                               │
│   Gönderen: PlayerName                 │
│   📋 GÖNDERENİN ŞARTLARI:              │
│      Tip: RESOURCE_COLLECTION          │
│      Ödül: 1000 Altın                  │
│      Ceza: 500 Altın                   │
│      Süre: 7 Gün                       │
│      Malzeme: Elmas x64                │
│                                         │
│   [✅ Kabul Et] [➕ Şart Ekle] [❌ Reddet]│
│                                         │
│ [GERİ]                                 │
└─────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────┐
│   SEÇENEK 1: [✅ Kabul Et]              │
└─────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────┐
│ ✅ KONTrat KABUL EDİLDİ!                │
│                                         │
│ Karşı tarafın şartlarını kabul ettiniz.│
│ İlk gönderen oyuncu son onayı verdiğinde│
│ kontrat aktif olacak.                  │
│                                         │
│ [TAMAM]                                │
└─────────────────────────────────────────┘
    ↓
✅ Sender'a bildirim: "PlayerName şartlarınızı kabul etti! Son onay gerekiyor."

┌─────────────────────────────────────────┐
│   SEÇENEK 2: [➕ Şart Ekle]              │
└─────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────┐
│ [Adım 1/7] Kontrat Tipi Seç            │
│                                         │
│ ℹ️ Gönderen RESOURCE_COLLECTION seçti. │
│    Siz farklı bir tip seçebilirsiniz.   │
│                                         │
│ [Tip Seçenekleri]                      │
│ [GERİ] [İPTAL]                         │
└─────────────────────────────────────────┘
    ↓
[Ödül] → [Ceza Tipi] → [Ceza] → [Süre] → [Tip'e Özel]
    ↓
┌─────────────────────────────────────────┐
│ [Adım 7/7] Özet ve Onay                 │
│                                         │
│ 📋 SİZİN ŞARTLARINIZ:                  │
│    Tip: COMBAT                         │
│    Ödül: 2000 Altın                    │
│    Ceza: 1000 Altın                    │
│    Süre: 14 Gün                        │
│                                         │
│ 📋 KARŞI TARAFIN ŞARTLARI:             │
│    Tip: RESOURCE_COLLECTION            │
│    Ödül: 1000 Altın                    │
│    Ceza: 500 Altın                     │
│    Süre: 7 Gün                         │
│    Malzeme: Elmas x64                  │
│                                         │
│ ℹ️ Şartlarınız kaydedilecek. İlk        │
│    gönderen oyuncu onayladığında        │
│    kontrat aktif olacak.                │
│                                         │
│ [GERİ] [İPTAL] [ONAYLA]                │
└─────────────────────────────────────────┘
    ↓
✅ Target'ın şartları kaydedilir
✅ Sender'a bildirim: "PlayerName şartlarını belirledi! Son onay gerekiyor."
```

### 3. SENDER'IN SON ONAY - İYİLEŞTİRİLMİŞ

```
[Final Onay Menüsü] (Otomatik açılır)
    ↓
┌─────────────────────────────────────────┐
│ ⚠️ SON ONAY GEREKİYOR!                  │
│                                         │
│ PlayerName şartlarınızı kabul etti      │
│ (veya şartlarını belirledi).            │
│                                         │
│ 📋 SİZİN ŞARTLARINIZ:                   │
│    Tip: RESOURCE_COLLECTION            │
│    Ödül: 1000 Altın                    │
│    Ceza: 500 Altın                     │
│    Süre: 7 Gün                         │
│    Malzeme: Elmas x64                  │
│                                         │
│ 📋 KARŞI TARAFIN ŞARTLARI:              │
│    Tip: COMBAT                         │
│    Ödül: 2000 Altın                    │
│    Ceza: 1000 Altın                    │
│    Süre: 14 Gün                        │
│                                         │
│ ℹ️ Her iki tarafın şartlarını onaylarsanız│
│    kontrat aktif olacak.                │
│                                         │
│ [✅ ONAYLA] [❌ REDDET]                  │
└─────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────┐
│   SEÇENEK 1: [✅ ONAYLA]                │
└─────────────────────────────────────────┘
    ↓
✅ Bilateral Contract oluşturulur
✅ Her iki oyuncuya bildirim: "Kontrat aktif oldu!"
✅ HUD bildirimi

┌─────────────────────────────────────────┐
│   SEÇENEK 2: [❌ REDDET]                 │
└─────────────────────────────────────────┘
    ↓
❌ İstek iptal edilir
❌ Target'a bildirim: "Kontrat reddedildi."
```

## YAPILAN İYİLEŞTİRMELER ✅

### 1. ✅ Menü Başlıklarına Adım Numarası Eklendi
   - `openRewardSliderMenu` → `"§6[Adım 4/8] Ödül Belirle"`
   - `openTypeSelectionMenu` → `"§6[Adım 1/9] Kontrat Tipi Seç"`
   - `openScopeSelectionMenu` → `"§6[Adım 2/9] Kontrat Kapsamı Seç"`
   - `openPlayerSelectionMenuForRequest` → `"§6[Adım 3/9] Hedef Oyuncu Seç"`
   - `openSummaryMenu` → `"§6[Adım 9/9] Kontrat Özeti"`
   - Her menüde adım numarası gösteriliyor

### 2. ✅ Özet Menüsünde Her İki Tarafın Şartlarını Göster
   - `openSummaryMenu` metodunda `contractRequestId` varsa karşı tarafın şartlarını da gösteriyor
   - "SİZİN ŞARTLARINIZ" ve "KARŞI TARAFIN ŞARTLARI" bölümleri eklendi

### 3. ✅ Final Onay Menüsünde Her İki Tarafın Şartlarını Göster
   - `openTermsApprovalMenu` metodunda her iki tarafın şartlarını yan yana gösteriyor
   - Daha büyük menü (54 slot) kullanılıyor
   - Açıklayıcı başlık ve bilgi mesajları eklendi

### 4. ✅ Her Menüde İptal Butonu
   - Özet menüsünde [İPTAL] butonu var
   - Oyuncu seçim menüsünde [İPTAL] butonu eklendi
   - İptal edildiğinde state temizleniyor

### 5. ✅ Her Menüde Geri Butonu
   - Tüm menülerde [GERİ] butonu var
   - Geri gidildiğinde önceki adıma dönülüyor

### 6. ✅ Açıklayıcı Mesajlar
   - Özet menüsünde açıklayıcı bilgi mesajları eklendi
   - Oyuncu seçim menüsünde bilgi butonu eklendi
   - Her adımda oyuncuya ne yapması gerektiği açıklanıyor

### 7. ✅ Final Onay Menüsü İyileştirildi
   - Daha büyük menü (54 slot)
   - Her iki tarafın şartları yan yana gösteriliyor
   - Açıklayıcı başlık ve bilgi mesajları
   - [ONAYLA] ve [REDDET] butonları net bir şekilde yerleştirildi

## KOD DURUMU

✅ Tüm iyileştirmeler uygulandı
✅ Akış şeması güncellendi
✅ Menüler daha kullanıcı dostu hale getirildi
✅ Adım numaraları eklendi
✅ Açıklayıcı mesajlar eklendi
✅ Her iki tarafın şartları gösteriliyor
