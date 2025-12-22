# KONTRAT SİSTEMİ AKIŞ ŞEMASI

## ✅ GÜNCEL AKIŞ (Düzeltilmiş Sıralama)

### 1. İLK GÖNDEREN OYUNCU (SENDER) AKIŞI

```
[Ana Menü]
    ↓
[Yeni Kontrat Oluştur] tıkla
    ↓
┌─────────────────────────────────────────┐
│ [Adım 1/9] Kontrat Kapsamı Seç        │
│                                         │
│ PLAYER_TO_PLAYER - Oyuncu → Oyuncu    │
│ CLAN_TO_CLAN - Klan → Klan            │
│ PLAYER_TO_CLAN - Oyuncu → Klan        │
│ CLAN_TO_PLAYER - Klan → Oyuncu        │
│                                         │
│ [GERİ] [İPTAL]                         │
└─────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────┐
│ [Adım 2/9] Hedef Oyuncu Seç            │
│ (Sadece PLAYER_TO_PLAYER için)         │
│                                         │
│ ℹ️ Oyuncu seçildikten sonra tip        │
│    seçimi yapılacak. Şartlar            │
│    belirlendikten sonra istek           │
│    gönderilecek.                       │
│                                         │
│ [Oyuncu Listesi]                       │
│ [GERİ] [İPTAL]                         │
└─────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────┐
│ [Adım 3/9] Kontrat Tipi Seç            │
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
│ [Adım 4/9] Ödül Belirle                │
│                                         │
│ ℹ️ Direkt onaylarsanız ödül            │
│    belirlenmeyecek. Ama ceza            │
│    belirlemek zorundasınız.            │
│                                         │
│ Mevcut: [Değer veya "Yok"]             │
│ [Slider] [Hızlı Değerler]              │
│ [Direkt Onayla (Ödül Yok)]             │
│                                         │
│ [GERİ] [İPTAL] [ONAYLA]               │
└─────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────┐
│ [Adım 5/9] Ceza Tipi Seç               │
│                                         │
│ HEALTH_PENALTY - Can Kaybı             │
│ BANK_PENALTY - Para Kaybı              │
│ MORTGAGE - İpotek                       │
│                                         │
│ [GERİ] [İPTAL]                         │
└─────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────┐
│ [Adım 6/9] Ceza Miktarı Belirle        │
│                                         │
│ ℹ️ Direkt onaylarsanız ceza             │
│    belirlenmeyecek. Ama ödül            │
│    belirlemediyseniz devam edemezsiniz.│
│                                         │
│ Mevcut: [Değer veya "Yok"]             │
│ [Slider] [Hızlı Değerler]              │
│ [Direkt Onayla (Ceza Yok)]             │
│                                         │
│ ⚠️ Kontrol: En az birini belirlemek    │
│    zorundasınız (Ödül VEYA Ceza)       │
│                                         │
│ [GERİ] [İPTAL] [ONAYLA]               │
└─────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────┐
│ [Adım 7/9] Süre Belirle                │
│                                         │
│ [Gün/Hafta/Ay seçimi]                  │
│                                         │
│ [GERİ] [İPTAL]                         │
└─────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────┐
│ [Adım 8/9] Tip'e Özel Parametreler     │
│                                         │
│ RESOURCE_COLLECTION → Malzeme + Miktar │
│ COMBAT → Hedef oyuncu/klan             │
│ TERRITORY → Lokasyon + Yarıçap         │
│ CONSTRUCTION → Yapı tipi                │
│                                         │
│ [GERİ] [İPTAL]                         │
└─────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────┐
│ [Adım 9/9] Kontrat Özeti               │
│                                         │
│ 📋 SİZİN ŞARTLARINIZ:                  │
│    Kapsam: PLAYER_TO_PLAYER            │
│    Hedef: PlayerName                   │
│    Tip: RESOURCE_COLLECTION            │
│    Ödül: 1000 Altın (veya Yok)         │
│    Ceza: 500 Altın (veya Yok)          │
│    Süre: 7 Gün                         │
│    Malzeme: Elmas x64                  │
│                                         │
│ ⚠️ Kontrol: En az birini belirlemek    │
│    zorundasınız (Ödül VEYA Ceza)       │
│                                         │
│ ℹ️ Bu şartlar karşı tarafa gönderilecek│
│    Karşı taraf kabul ederse kontrat    │
│    aktif olacak.                       │
│                                         │
│ [GERİ] [İPTAL] [ONAYLA VE GÖNDER]      │
└─────────────────────────────────────────┘
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
[Gelen İstekler] tıkla (Bildirim: "3 yeni istek var!")
    ↓
┌─────────────────────────────────────────┐
│ Gelen İstekler (Sayfa 1)                │
│                                         │
│ [İstek 1]                               │
│   Gönderen: PlayerName                 │
│   📋 GÖNDERENİN ŞARTLARI:              │
│      Kapsam: PLAYER_TO_PLAYER          │
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
│ ✅ KONTRAT KABUL EDİLDİ!                │
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
│ ℹ️ Scope ve oyuncu zaten belirlenmiş.   │
│    Sadece tip ve şartları belirleyin.  │
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
│ [Adım 2/7] Ödül Belirle                │
│                                         │
│ ℹ️ Direkt onaylarsanız ödül            │
│    belirlenmeyecek. Ama ceza            │
│    belirlemek zorundasınız.            │
│                                         │
│ [Slider] [Hızlı Değerler]              │
│ [Direkt Onayla (Ödül Yok)]             │
│                                         │
│ [GERİ] [İPTAL] [ONAYLA]               │
└─────────────────────────────────────────┘
    ↓
[Ceza Tipi] → [Ceza Miktarı] → [Süre] → [Tip'e Özel]
    ↓
┌─────────────────────────────────────────┐
│ [Adım 7/7] Özet ve Onay                 │
│                                         │
│ 📋 SİZİN ŞARTLARINIZ:                  │
│    Tip: COMBAT                         │
│    Ödül: 2000 Altın (veya Yok)         │
│    Ceza: 1000 Altın (veya Yok)          │
│    Süre: 14 Gün                        │
│                                         │
│ 📋 KARŞI TARAFIN ŞARTLARI:             │
│    Tip: RESOURCE_COLLECTION            │
│    Ödül: 1000 Altın                    │
│    Ceza: 500 Altın                     │
│    Süre: 7 Gün                         │
│    Malzeme: Elmas x64                  │
│                                         │
│ ⚠️ Kontrol: En az birini belirlemek    │
│    zorundasınız (Ödül VEYA Ceza)       │
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

### 3. SENDER'IN SON ONAY AKIŞI

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
│    Ödül: 1000 Altın (veya Yok)         │
│    Ceza: 500 Altın (veya Yok)          │
│    Süre: 7 Gün                         │
│    Malzeme: Elmas x64                  │
│                                         │
│ 📋 KARŞI TARAFIN ŞARTLARI:              │
│    Tip: COMBAT                         │
│    Ödül: 2000 Altın (veya Yok)         │
│    Ceza: 1000 Altın (veya Yok)         │
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
❌ Tüm şartlar silinir
❌ Target'a bildirim: "Kontrat reddedildi."
```

## ✅ YAPILAN DÜZELTMELER

### 1. ✅ Akış Sıralaması Düzeltildi
**ÖNCEKİ (YANLIŞ):**
```
[Yeni Kontrat Oluştur] → [Tip Seçimi] → [Kapsam Seçimi] → [Oyuncu Seçimi]
```

**YENİ (DOĞRU):**
```
[Yeni Kontrat Oluştur] → [Kapsam Seçimi] → [Oyuncu Seçimi] → [Tip Seçimi]
```

**Neden?**
- Kapsam seçilmeden oyuncu seçilemez (hangi kapsamda olduğunu bilmek gerekir)
- Oyuncu seçilmeden tip seçilemez (hangi oyuncuya gönderileceğini bilmek gerekir)
- Bu sıralama daha mantıklı ve döngüye girmez

### 2. ✅ Şart Ekleme Durumunda Scope ve Oyuncu Seçimi Atlandı
**ÖNCEKİ SORUN:**
- Target şart eklerken tekrar scope ve oyuncu seçimi yapılıyordu
- Bu gereksizdi çünkü zaten kontrat var, sadece şart ekleniyor

**YENİ ÇÖZÜM:**
- `contractRequestId` varsa (şart ekleme durumu)
- Scope ve oyuncu seçimi **ATLANIR**
- Direkt **Tip Seçimi** menüsüne gidilir
- Scope ve oyuncu zaten request'te belirlenmiş

**Kod:**
```java
// startTermsWizard() içinde
state.contractRequestId = requestId;
state.scope = request.getScope(); // Scope zaten request'te var
// Oyuncu seçimi yapılmaz - request'te zaten var
openTypeSelectionMenu(player); // Direkt tip seçimine git
```

### 3. ✅ Ödül/Ceza Mantığı Güncellendi
**ÖNCEKİ SORUN:**
- Her ikisi de zorunluydu
- Direkt onaylanamıyordu

**YENİ ÇÖZÜM:**
- **En az birini belirlemek zorunda** (Ödül VEYA Ceza)
- Ödül menüsünde direkt onaylarsa → Ödül `null` olur
- Ceza menüsünde direkt onaylarsa → Ceza `null` olur
- Ama **en az birini belirlemek zorunda**
- Özet menüsünde kontrol yapılır

**Kod:**
```java
// openRewardSliderMenu() içinde
if (state.reward > 0) {
    valueLore.add("§eMevcut Ödül: §a" + String.format("%.0f", state.reward) + " Altın");
} else {
    valueLore.add("§eMevcut Ödül: §7Yok (Direkt onaylarsanız ödül olmayacak)");
}
valueLore.add("§7ℹ️ Direkt onaylarsanız ödül");
valueLore.add("§7belirlenmeyecek. Ama ceza");
valueLore.add("§7belirlemek zorundasınız.");

// handlePenaltySliderClick() içinde
if (state.reward <= 0 && state.penalty <= 0) {
    player.sendMessage("§c§lHATA!");
    player.sendMessage("§7En az birini belirlemek zorundasınız:");
    player.sendMessage("§7- Ödül veya");
    player.sendMessage("§7- Ceza");
    return;
}

// handleSummaryMenuClick() içinde
if (state.reward <= 0 && state.penalty <= 0) {
    player.sendMessage("§c§lHATA!");
    player.sendMessage("§7En az birini belirlemek zorundasınız:");
    player.sendMessage("§7- Ödül veya");
    player.sendMessage("§7- Ceza");
    return;
}
```

### 4. ✅ Adım Numaraları Güncellendi
**Yeni Sıralama:**
1. Kapsam Seçimi
2. Oyuncu Seçimi (sadece PLAYER_TO_PLAYER için)
3. Tip Seçimi
4. Ödül Belirle
5. Ceza Tipi Seçimi
6. Ceza Miktarı Belirle
7. Süre Belirle
8. Tip'e Özel Parametreler
9. Özet ve Onay

**Şart Ekleme Durumu (7 adım):**
1. Tip Seçimi
2. Ödül Belirle
3. Ceza Tipi Seçimi
4. Ceza Miktarı Belirle
5. Süre Belirle
6. Tip'e Özel Parametreler
7. Özet ve Onay

## YAPILAN İYİLEŞTİRMELER ✅

### 1. ✅ Menü Başlıklarına Adım Numarası Eklendi
   - `openScopeSelectionMenu` → `"§6[Adım 1/9] Kontrat Kapsamı Seç"`
   - `openPlayerSelectionMenuForRequest` → `"§6[Adım 2/9] Hedef Oyuncu Seç"`
   - `openTypeSelectionMenu` → `"§6[Adım 3/9] Kontrat Tipi Seç"`
   - `openRewardSliderMenu` → `"§6[Adım 4/9] Ödül Belirle"`
   - `openPenaltyTypeSelectionMenu` → `"§6[Adım 5/9] Ceza Tipi Seç"`
   - `openPenaltySliderMenu` → `"§6[Adım 6/9] Ceza Miktarı Belirle"`
   - `openTimeSelectionMenu` → `"§6[Adım 7/9] Süre Belirle"`
   - `openTypeSpecificMenu` → `"§6[Adım 8/9] Tip'e Özel Parametreler"`
   - `openSummaryMenu` → `"§6[Adım 9/9] Kontrat Özeti"`
   - Her menüde adım numarası gösteriliyor

### 2. ✅ Özet Menüsünde Her İki Tarafın Şartlarını Göster
   - `openSummaryMenu` metodunda `contractRequestId` varsa karşı tarafın şartlarını da gösteriyor
   - "SİZİN ŞARTLARINIZ" ve "KARŞI TARAFIN ŞARTLARI" bölümleri eklendi
   - Ödül/Ceza null ise "Yok" gösteriliyor

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
   - State korunsun (sadece adım değişsin)

### 6. ✅ Açıklayıcı Mesajlar
   - Özet menüsünde açıklayıcı bilgi mesajları eklendi
   - Oyuncu seçim menüsünde bilgi butonu eklendi
   - Ödül/Ceza menülerinde direkt onaylama açıklaması eklendi
   - Her adımda oyuncuya ne yapması gerektiği açıklanıyor

### 7. ✅ Final Onay Menüsü İyileştirildi
   - Daha büyük menü (54 slot)
   - Her iki tarafın şartları yan yana gösteriliyor
   - Açıklayıcı başlık ve bilgi mesajları
   - [ONAYLA] ve [REDDET] butonları net bir şekilde yerleştirildi

### 8. ✅ Ödül/Ceza Null Desteği
   - Ödül menüsünde direkt onaylarsa ödül null olur
   - Ceza menüsünde direkt onaylarsa ceza null olur
   - En az birini belirlemek zorunda kontrolü eklendi
   - Özet menüsünde null değerler "Yok" olarak gösteriliyor

## KOD DURUMU

✅ Tüm düzeltmeler uygulandı
✅ Akış şeması güncellendi
✅ Menüler daha kullanıcı dostu hale getirildi
✅ Adım numaraları güncellendi
✅ Açıklayıcı mesajlar eklendi
✅ Her iki tarafın şartları gösteriliyor
✅ Ödül/Ceza null desteği eklendi
✅ Şart ekleme durumunda scope ve oyuncu seçimi atlanıyor

## ÖNEMLİ NOTLAR

### ⚠️ Ödül/Ceza Kontrolü
- **En az birini belirlemek zorunda** (Ödül VEYA Ceza)
- İkisi de null olamaz
- Biri null olabilir, ama ikisi birden null olamaz
- Özet menüsünde kontrol yapılır
- Ceza menüsünde de kontrol yapılır (ödül null ise ceza zorunlu)

### ⚠️ Şart Ekleme Durumu
- `contractRequestId` varsa → Şart ekleme durumu
- Scope ve oyuncu seçimi **YAPILMAZ**
- Direkt **Tip Seçimi** menüsüne gidilir
- Scope ve oyuncu zaten request'te belirlenmiş

### ⚠️ Akış Sıralaması
- **DOĞRU:** [Kapsam] → [Oyuncu] → [Tip] → [Ödül] → [Ceza] → [Süre] → [Tip'e Özel] → [Özet]
- **YANLIŞ:** [Tip] → [Kapsam] → [Oyuncu] (Döngüye girer)

**Son Güncelleme:** Son 3 Gün (Son Commit'ler)  
**Döküman Versiyonu:** 2.0
