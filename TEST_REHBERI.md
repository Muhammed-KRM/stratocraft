# 🎮 STRATOCRAFT TEST REHBERİ

Bu doküman, son eklenen özelliklerin nasıl test edileceğini ve tüm GUI menülerine nasıl erişileceğini açıklar.

---

## 📋 İÇİNDEKİLER

1. [GUI Menülerine Erişim](#gui-menülerine-erişim)
2. [Klan Sistemi Test](#klan-sistemi-test)
3. [Felaket Sistemi Test](#felaket-sistemi-test)
4. [Kontrat Sistemi Test](#kontrat-sistemi-test)
5. [Yeni Eklenen Menüler](#yeni-eklenen-menüler)

---

## 🎯 GUI MENÜLERİNE ERİŞİM

### Ana Klan Menüsü

**Komut:**
```
/klan
```

**Menü İçeriği:**
- **Slot 0-8:** Klan bilgileri ve temel işlemler
- **Slot 10:** Üyeler menüsü
- **Slot 12:** Görevler menüsü
- **Slot 14:** Bakiye menüsü
- **Slot 16:** Yapılar menüsü (BEACON)
- **Slot 18:** İttifaklar menüsü (DIAMOND)
- **Slot 19:** Eğitme/Üreme menüsü (SPAWNER) ⭐ YENİ
- **Slot 20:** Eğitim İlerlemesi menüsü (EXPERIENCE_BOTTLE) ⭐ YENİ
- **Slot 21:** İstatistikler menüsü (PAPER)

---

## 🏛️ KLAN SİSTEMİ TEST

### 1. Klan Oluşturma

**Komut:**
```
/klan kur <klan_ismi>
```

**Test Adımları:**
1. Komutu çalıştır
2. Klan başarıyla oluşturuldu mesajını kontrol et
3. `/klan` komutu ile menüyü aç
4. Klan bilgilerinin göründüğünü doğrula

### 2. Üye Yönetimi

**Menü Yolu:**
```
/klan → Slot 10 (Üyeler)
```

**Test Senaryoları:**
- Üye listesini görüntüle
- Online/offline durumlarını kontrol et
- Rütbe değiştirme (Lider/General)
- Üye çıkarma (onay sistemi)

### 3. Klan Bankası

**Menü Yolu:**
```
/klan → Slot 14 (Bakiye) → Banka Sandığı
```

**Test Senaryoları:**
- Ender Chest'e sağ tık (metadata: "ClanBank")
- Banka sandığını aç
- Item yatırma/çekme
- Maaş bilgilerini görüntüleme
- Transfer kontratlarını görüntüleme

### 4. Klan Yapıları

**Menü Yolu:**
```
/klan → Slot 16 (BEACON - Yapılar)
```

**Test Senaryoları:**
- Yapı listesini görüntüle
- Yapı detaylarını incele
- Yapı seviye yükseltme
- Yapı konumuna ışınlanma
- Güç katkısını kontrol et

### 5. İttifaklar

**Menü Yolu:**
```
/klan → Slot 18 (DIAMOND - İttifaklar)
```

**Test Senaryoları:**
- Aktif ittifakları listele
- İttifak detaylarını görüntüle
- Yeni ittifak oluştur (wizard)
- İttifak türü seçimi (Defensive, Offensive, Trade, Full)
- İttifak süresi belirleme
- İttifakı feshetme

### 6. Klan Görevleri

**Menü Yolu:**
```
/klan → Slot 12 (Görevler)
```

**Veya Fiziksel:**
- Lectern'a sağ tık (metadata: "ClanMissionBoard")

**Test Senaryoları:**
- Aktif görevleri görüntüle
- Görev ilerlemesini takip et
- Yeni görev oluştur
- Görev türü seçimi
- Üye bazlı ilerleme takibi

### 7. Klan İstatistikleri

**Menü Yolu:**
```
/klan → Slot 21 (PAPER - İstatistikler)
```

**Test Senaryoları:**
- Genel bilgileri görüntüle
- Güç istatistikleri
- Üye istatistikleri
- Yapı/görev istatistikleri
- Seviye bonusları
- En aktif/güçlü üyeler

---

## 🌋 FELAKET SİSTEMİ TEST

### 1. Felaket Başlatma

**Komut:**
```
/admin felaket başlat <tip> <seviye>
```

**Felaket Tipleri:**
- `METEOR_SHOWER` - Meteor yağmuru
- `EARTHQUAKE` - Deprem
- `VOLCANIC_ERUPTION` - Volkanik patlama
- `TSUNAMI` - Tsunami
- `PLAGUE` - Veba

**Test Senaryoları:**
1. Felaket başlat
2. Bildirim mesajlarını kontrol et
3. Arena transformasyonunu gözlemle
4. Faz geçişlerini takip et
5. Zayıf noktaları bul ve vur
6. Felaketi yenmeyi dene

### 2. Faz Geçişleri

**Test Adımları:**
1. Felaket başlat
2. Faz 1'den Faz 2'ye geçişi bekle
3. Bildirim mesajlarını kontrol et
4. Efektleri gözlemle
5. Faz özelliklerinin değiştiğini doğrula

### 3. Zorluk Entegrasyonu

**Test Senaryoları:**
- Merkezden uzaklığa göre felaket gücü
- Dinamik zorluk sistemi
- Oyuncu gücüne göre ayarlama

---

## 📜 KONTRAT SİSTEMİ TEST

### 1. Kontrat Menüsü

**Komut:**
```
/kontrat
```

**Menü İçeriği:**
- Kontrat listesi (sayfalama)
- Kontrat detayları
- Kontrat kabul/reddetme
- Yeni kontrat oluşturma

### 2. Kontrat Oluşturma Wizard

**Menü Yolu:**
```
/kontrat → Slot 47 (Yeni Kontrat Oluştur)
```

**Wizard Adımları:**

#### Adım 1: Kontrat Tipi Seçimi
- `MATERIAL_DELIVERY` - Malzeme teslimi
- `PLAYER_KILL` - Oyuncu öldürme
- `TERRITORY_RESTRICT` - Bölge kısıtlama
- `NON_AGGRESSION` - Saldırmazlık
- `BASE_PROTECTION` - Üs koruma
- `STRUCTURE_BUILD` - Yapı inşa

#### Adım 2: Kapsam Seçimi
- `PLAYER_TO_PLAYER` - Oyuncu → Oyuncu
- `CLAN_TO_CLAN` - Klan → Klan
- `PLAYER_TO_CLAN` - Oyuncu → Klan
- `CLAN_TO_PLAYER` - Klan → Oyuncu

#### Adım 3: Ödül Belirleme
- Slider GUI ile ödül miktarı
- +/- butonları
- Hızlı yüzde seçenekleri (%50, %100, %150, %200)

#### Adım 4: Ceza Belirleme
- Slider GUI ile ceza miktarı
- +/- butonları
- Hızlı yüzde seçenekleri

#### Adım 5: Süre Belirleme
- Gün/Hafta/Ay seçimi
- Saat ve dakika ayarlama
- Detaylı süre seçimi

#### Adım 6: Tip'e Özel Parametreler
- **MATERIAL_DELIVERY:** Malzeme seçimi (genişletilmiş liste, sayfalama)
- **PLAYER_KILL:** Hedef oyuncu seçimi (chat input)
- **TERRITORY_RESTRICT:** Konum seçimi (chat input: "x y z" veya "here")
- **NON_AGGRESSION:** Hedef oyuncu/klan seçimi
- **STRUCTURE_BUILD:** Yapı tipi seçimi

#### Adım 7: Özet ve Onay
- Tüm bilgileri gözden geçir
- "Onayla" butonu ile kontratı oluştur
- "Şablon Olarak Kaydet" ile şablon kaydet
- "İptal" ile iptal et

### 3. Kontrat Şablonları

**Menü Yolu:**
```
/kontrat → Slot 48 (Şablonlar)
```

**Test Senaryoları:**
- Şablon listesini görüntüle
- Şablon yükle (wizard'a otomatik doldur)
- Şablon sil
- Şablon oluştur (özet menüsünden)

### 4. Kontrat Geçmişi

**Menü Yolu:**
```
/kontrat → Slot 49 (Geçmiş)
```

**Test Senaryoları:**
- Tamamlanan kontratları görüntüle
- İhlal edilen kontratları görüntüle
- Sayfalama ile gezin
- Kontrat detaylarını incele

### 5. Kontrat Kabul/Reddetme

**Test Senaryoları:**
1. Kontrat listesinden bir kontrat seç
2. Detayları incele
3. "Kabul Et" butonuna tıkla
4. Kan imzası işlemini tamamla
5. Kontratın aktif olduğunu doğrula

---

## 🆕 YENİ EKLENEN MENÜLER

### 1. Eğitme/Üreme Menüsü

**Menü Yolu:**
```
/klan → Slot 19 (SPAWNER)
```

**Ana Menü Özellikleri:**
- Eğitilmiş canlıları listele
- Klan canlılarını görüntüle
- Canlı detayları (cinsiyet, sağlık, binilebilirlik)
- Canlı yönetimi (ışınlanma, binme)

**Üreme Menüsü:**
- Ana menüden Slot 49 (GOLDEN_APPLE) ile açılır
- Aktif üreme çiftlerini listele
- Üreme çifti detayları
- Yeni çift oluştur

**Test Senaryoları:**
1. Bir canlıyı eğit (ritüel ile)
2. Eğitme menüsünü aç
3. Canlıyı seç ve detayları görüntüle
4. Canlıya ışınlan
5. Binilebilirse canlıya bin
6. Üreme menüsünü aç
7. Dişi ve erkek canlıları seç
8. Üreme çifti oluştur

### 2. Eğitim İlerlemesi Menüsü

**Menü Yolu:**
```
/klan → Slot 20 (EXPERIENCE_BOTTLE)
```

**Menü Özellikleri:**
- Tüm ritüel/batarya antrenman durumları
- Antrenman ilerlemesi (%)
- Mastery seviyeleri (Usta, Uzman, Efsanevi)
- Güç çarpanları
- Kalan kullanım sayıları

**Test Senaryoları:**
1. Bir batarya/ritüel kullan
2. Eğitim menüsünü aç
3. İlerlemeyi kontrol et
4. Mastery seviyesini görüntüle
5. Sonraki seviye için gereken kullanımı kontrol et

### 3. Güç Menüsü

**Komut:**
```
/sgp
```

**Veya:**
```
/sgp menu
```

**Menü Özellikleri:**
- Oyuncu gücü görüntüleme
- Klan gücü görüntüleme
- En güçlü oyuncular listesi
- Güç bileşenleri (yapı, eşya, buff, eğitim, klan tech)

**Test Senaryoları:**
1. `/sgp` komutunu çalıştır
2. Ana menüyü aç
3. Oyuncu gücünü görüntüle
4. Klan gücünü görüntüle
5. En güçlü oyuncuları listele
6. Güç bileşenlerini incele

### 4. Kervan Menüsü

**Menü Yolu:**
```
/klan → Slot 22 (Kervan butonu - eğer varsa)
```

**Veya Komut:**
```
/kervan
```

**Menü Özellikleri:**
- Aktif kervanları listele
- Kervan detayları (sahip, durum, konum, kargo)
- Yeni kervan oluştur (wizard)
- Kervan konumuna ışınlan

**Test Senaryoları:**
1. Kervan menüsünü aç
2. Yeni kervan oluştur
3. Kargo seçimi
4. Hedef konum belirleme (chat input)
5. Kervan oluştur
6. Kervan detaylarını görüntüle
7. Kervan konumuna ışınlan

---

## 🔧 DİĞER TEST SENARYOLARI

### Boss Faz Sistemi

**Test Adımları:**
1. Boss spawn et (ritüel ile)
2. Boss'a hasar ver
3. Sağlık %66'ya düştüğünde Faz 2'ye geçişi gözlemle
4. Bildirim mesajlarını kontrol et
5. Faz efektlerini (hız artışı) doğrula
6. Zayıf noktaların güncellendiğini kontrol et
7. Sağlık %33'e düştüğünde Faz 3'e geçişi gözlemle
8. Güç artışını doğrula

### Zorluk Sistemi Entegrasyonu

**Test Senaryoları:**
1. Merkez noktasından uzaklaş
2. Boss spawn et
3. Boss gücünün arttığını doğrula (can, hasar, savunma)
4. Yüksek zorluk seviyelerinde ekstra efektleri kontrol et
5. Felaket sisteminin zorluk seviyesine göre ayarlandığını doğrula

---

## 📝 NOTLAR

### Menü Navigasyonu
- Tüm menülerde **Slot 45 veya 53** genellikle "Geri" butonudur
- Sayfalama için **Slot 45 (Önceki)** ve **Slot 53 (Sonraki)** kullanılır
- Detay menülerinde **Slot 13** genellikle ana öğe gösterimidir

### Hata Durumları
- Menü açılmıyorsa: Manager'ın başlatıldığından emin ol
- Null pointer hatası: Gerekli bağımlılıkların yüklendiğini kontrol et
- Menü boş görünüyorsa: Veri kaynağının dolu olduğunu doğrula

### Performans İpuçları
- Büyük listeler için sayfalama kullanılır (45 öğe/sayfa)
- Menüler thread-safe olarak tasarlanmıştır
- Reflection kullanımı güvenli şekilde yapılmıştır

---

## 🎯 HIZLI ERİŞİM TABLOSU

| Menü | Komut/Yol | Slot |
|------|-----------|------|
| Ana Klan Menüsü | `/klan` | - |
| Üyeler | `/klan` → Slot 10 | 10 |
| Görevler | `/klan` → Slot 12 | 12 |
| Bakiye/Banka | `/klan` → Slot 14 | 14 |
| Yapılar | `/klan` → Slot 16 | 16 |
| İttifaklar | `/klan` → Slot 18 | 18 |
| Eğitme/Üreme | `/klan` → Slot 19 | 19 |
| Eğitim | `/klan` → Slot 20 | 20 |
| İstatistikler | `/klan` → Slot 21 | 21 |
| Kontrat | `/kontrat` | - |
| Güç | `/sgp` | - |
| Kervan | `/kervan` veya menüden | - |

---

**Son Güncelleme:** 2024  
**Hazırlayan:** AI Assistant  
**Versiyon:** 1.0
