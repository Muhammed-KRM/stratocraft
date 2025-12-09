# 🌋 FELAKET SİSTEMİ ÖNCELİK PLANI VE UYGULAMA REHBERİ

## 📊 MEVCUT DURUM ANALİZİ

### ✅ Tamamlanan Özellikler
- Temel felaket spawn sistemi
- Handler sistemi (18 farklı handler)
- Kristal hedefleme ve yok etme
- BossBar ve countdown sistemi
- Basit güç hesaplama (oyuncu sayısı + klan seviyesi)
- Ödül sistemi (temel)

### ❌ Eksik Özellikler
- Dinamik zorluk sistemi (detaylı güç hesaplama)
- Faz sistemi
- Özel yetenekler
- AI iyileştirmeleri
- Çevresel etkiler
- Görsel/işitsel efektler
- İşbirlikçi mekanikler

---

## 🎯 ÖNCELİK SIRASI

### **FAZ 1: DİNAMİK ZORLUK SİSTEMİ** ⚡ (ÖNCELİKLİ - 1. Hafta)

**Neden Öncelikli:**
- Oyun dengelemesi için kritik
- Diğer sistemlerin temelini oluşturur
- Oyuncu deneyimini doğrudan etkiler

**Yapılacaklar:**
1. ✅ `PlayerPowerCalculator.java` oluştur
   - Yapı gücü hesaplama
   - Eşya gücü hesaplama (ItemManager entegrasyonu)
   - Buff gücü hesaplama (PotionEffect + BuffManager)
   - Eğitim gücü hesaplama (TrainingManager entegrasyonu)
   - Klan tech gücü hesaplama

2. ✅ `ServerPowerCalculator.java` oluştur
   - Tüm oyuncuların güç puanlarını topla
   - Ortalama hesapla
   - Oyuncu sayısı çarpanı uygula

3. ✅ `DisasterManager.calculateDisasterPower()` güncelle
   - Eski sistemi koru (geriye dönük uyumluluk)
   - Yeni dinamik sistemi entegre et
   - Config'den ayarları oku

4. ✅ `config.yml` güncelle
   - Dinamik zorluk ayarları ekle
   - Ağırlık çarpanları
   - Yapı tipi çarpanları
   - Oyuncu sayısı çarpanları

5. ✅ Test ve dengeleme
   - Farklı senaryolarda test
   - Güç hesaplama doğruluğu
   - Performans kontrolü

**Bağımlılıklar:**
- ItemManager.getWeaponLevel() ✓ (mevcut)
- ItemManager.getArmorLevel() ✓ (mevcut)
- TrainingManager (mastery sistemi var, training level için adapte et)
- BuffManager (entegrasyon gerekli)
- SpecialItemManager (tier kontrolü)

**Tahmini Süre:** 3-5 gün

---

### **FAZ 2: FAZ SİSTEMİ** 🚀 (2. Hafta)

**Neden İkinci:**
- Dinamik zorluk sisteminden sonra mantıklı
- Oyun deneyimini önemli ölçüde artırır
- Özel yetenekler için temel oluşturur

**Yapılacaklar:**
1. ✅ `DisasterPhase.java` enum oluştur
   - 4 faz: EXPLORATION, ASSAULT, RAGE, DESPERATION
   - Her faz için: can yüzdesi, saldırı aralığı, aktif yetenek sayısı

2. ✅ `Disaster.java` model güncelle
   - Mevcut fazı tut
   - Faz geçiş zamanını tut
   - Faz geçişi kontrolü

3. ✅ `DisasterTask.java` güncelle
   - Faz kontrolü ekle
   - Faz geçişi animasyonları
   - Faz bazlı saldırı aralığı

4. ✅ Handler'ları güncelle
   - Faz bazlı davranış değişiklikleri
   - Faz geçişi mesajları

5. ✅ BossBar güncelle
   - Faz bilgisini göster
   - Faz renkleri

**Bağımlılıklar:**
- Dinamik zorluk sistemi (faz geçişleri güç bazlı olabilir)

**Tahmini Süre:** 3-5 gün

---

### **FAZ 3: ÖZEL YETENEKLER** ⚔️ (3-4. Hafta)

**Neden Üçüncü:**
- Faz sistemi ile birlikte çalışır
- Her felaket için benzersiz deneyim
- Oyun çeşitliliğini artırır

**Yapılacaklar:**
1. ✅ `DisasterAbility.java` interface oluştur
   - `execute()` metodu
   - `getCooldown()` metodu
   - `canUse()` metodu

2. ✅ Her felaket için 2-3 yetenek implement et
   - Titan Golem: Yer Sarsma, Taş Fırlatma, Taş Duvar
   - Khaos Ejderi: Ateş Püskürtme, Gökyüzü Saldırısı, Ateş Yağmuru
   - Hiçlik Solucanı: Yer Altına Dalış, Hiçlik Çekimi, Yer Yarığı
   - Buzul Leviathan: Buz Fırtınası, Buz Duvarı, Buz Patlaması

3. ✅ Yetenek sistemi entegrasyonu
   - Handler'lara yetenek sistemi ekle
   - Faz bazlı yetenek aktivasyonu
   - Cooldown yönetimi

4. ✅ Test ve dengeleme
   - Her yeteneği test et
   - Performans kontrolü
   - Dengeleme

**Bağımlılıklar:**
- Faz sistemi (yetenekler faz bazlı aktif olur)

**Tahmini Süre:** 1-2 hafta

---

### **FAZ 4: AI İYİLEŞTİRMELERİ** 🧠 (5. Hafta)

**Neden Dördüncü:**
- Temel sistemler tamamlandıktan sonra
- Oyun deneyimini artırır ama kritik değil

**Yapılacaklar:**
1. ✅ Akıllı hedef seçimi
   - En zayıf klan bulma
   - Oyuncu tehdit analizi
   - Çoklu hedef sistemi

2. ✅ Gelişmiş pathfinding
   - A* algoritması (basitleştirilmiş)
   - Dinamik rota bulma
   - Engelleri aşma

3. ✅ Akıllı hareket desenleri
   - Zigzag hareket
   - Sprint modu
   - Geri çekilme
   - Flanking

**Bağımlılıklar:**
- Temel sistemler

**Tahmini Süre:** 1 hafta

---

### **FAZ 5: ÇEVRESEL ETKİLER** 🌍 (6. Hafta)

**Neden Beşinci:**
- Görsel iyileştirme
- Atmosfer oluşturma

**Yapılacaklar:**
1. ✅ Gökyüzü değişimi
   - Felaket tipine göre gökyüzü
   - Renk değişimleri

2. ✅ Yer titremesi
   - Ekran sarsma
   - Blok titreşimi

3. ✅ Hava değişimi
   - Yağmur, kar, fırtına
   - Felaket tipine göre

4. ✅ Blok yıkımı
   - Kalıcı izler
   - Çevre hasarı

**Bağımlılıklar:**
- Temel sistemler

**Tahmini Süre:** 1 hafta

---

### **FAZ 6: GÖRSEL VE İŞİTSEL EFEKTLER** ✨ (7. Hafta)

**Neden Altıncı:**
- Polish (cilalama)
- Oyun deneyimini artırır

**Yapılacaklar:**
1. ✅ Partikül efektleri
   - Her felaket için özel efektler
   - Faz geçişi animasyonları
   - Kritik hasar efektleri

2. ✅ Ses efektleri
   - Ambient müzik (ResourcePack gerekli)
   - Yetenek sesleri
   - Faz geçişi sesleri

**Bağımlılıklar:**
- Faz sistemi
- Özel yetenekler

**Tahmini Süre:** 1 hafta

---

### **FAZ 7: İŞBİRLİKÇİ MEKANİKLER** 👥 (8. Hafta)

**Neden Yedinci:**
- İleri seviye özellik
- Oyun deneyimini artırır ama kritik değil

**Yapılacaklar:**
1. ✅ Tank/DPS/Healer sistemi
   - Rol bazlı mekanikler
   - Zayıf nokta mekanikleri

2. ✅ Koordinasyon gerektiren görevler
   - Eş zamanlı aktivasyon
   - Grup çalışması

**Bağımlılıklar:**
- Tüm temel sistemler

**Tahmini Süre:** 1 hafta

---

### **FAZ 8: ÖDÜL VE İLERLEME SİSTEMİ** 🏆 (9. Hafta)

**Neden Sekizinci:**
- Mevcut ödül sistemi var
- Genişletme ve iyileştirme

**Yapılacaklar:**
1. ✅ Faz geçişi ödülleri
   - Her faz geçişinde ödül
   - Faz bazlı ödül çeşitliliği

2. ✅ Özel başarımlar
   - Felaket defteri
   - Rozetler ve başarımlar

3. ✅ İlerleme sistemi
   - Felaket seviyesi
   - İstatistikler

**Bağımlılıklar:**
- Faz sistemi

**Tahmini Süre:** 1 hafta

---

### **FAZ 9: ÖZEL SENARYOLAR** 🎬 (10. Hafta)

**Neden Dokuzuncu:**
- Son eklemeler
- Özel anlar

**Yapılacaklar:**
1. ✅ Epik anlar
   - Felaket girişi
   - Faz geçişi
   - Ölüm animasyonu

2. ✅ Mini oyunlar
   - Felaket kaçışı
   - Felaket savunması

**Bağımlılıklar:**
- Tüm sistemler

**Tahmini Süre:** 1 hafta

---

## 📋 UYGULAMA DETAYLARI

### **FAZ 1: DİNAMİK ZORLUK SİSTEMİ - DETAYLI PLAN**

#### **1.1 PlayerPowerCalculator.java**

**Gereksinimler:**
- ItemManager.getWeaponLevel() ✓
- ItemManager.getArmorLevel() ✓
- SpecialItemManager.isSpecialItem() (kontrol et)
- SpecialItemManager.getTier() (kontrol et)
- TrainingManager (training level için adapte et)
- BuffManager (entegrasyon gerekli)
- ClanManager (yapılar için)

**Hesaplama Formülleri:**
```java
// Yapı Gücü
Yapı Gücü = Σ (Yapı Seviyesi × Yapı Tipi Çarpanı)
Çarpanlar: Batarya=2.0, Araştırma=1.5, Üretim=1.2, Savunma=1.8, Diğer=1.0

// Eşya Gücü
Silah: Math.pow(2, level-1) * 5  // 5, 10, 20, 40, 80
Zırh: Math.pow(2, level-1) * 3 * (tam set ? 1.5 : 1.0)  // 3, 6, 12, 24, 48
Özel Eşya: tier * tier * 10  // 10, 25, 50, 100

// Buff Gücü
PotionEffect: amplifier * 10 * buffTipiÇarpanı
BuffManager: TODO (entegrasyon gerekli)

// Eğitim Gücü
TrainingManager'dan mastery seviyesi al (adaptasyon gerekli)
Eğitim Gücü = masteryLevel * 5

// Klan Tech Gücü
Klan Tech Level * 10

// Toplam
Toplam = (Yapı × 0.3) + (Eşya × 0.4) + (Buff × 0.15) + (Eğitim × 0.1) + (Klan Tech × 0.05)
```

#### **1.2 ServerPowerCalculator.java**

**Gereksinimler:**
- PlayerPowerCalculator
- ClanManager
- TrainingManager

**Hesaplama:**
```java
Sunucu Güç Puanı = (Tüm Oyuncuların Güç Puanları Toplamı / Aktif Oyuncu Sayısı) × Oyuncu Sayısı Çarpanı

Oyuncu Sayısı Çarpanları:
- 1-3: 0.8x
- 4-6: 1.0x
- 7-10: 1.3x
- 11-15: 1.6x
- 16+: 2.0x
```

#### **1.3 DisasterManager Güncellemesi**

**Değişiklikler:**
- `calculateDisasterPower()` metodunu güncelle
- Eski sistemi koru (flag ile)
- Yeni sistemi entegre et
- Config'den ayarları oku

#### **1.4 Config.yml Güncellemesi**

**Eklenmesi Gerekenler:**
```yaml
disaster:
  power:
    # Eski sistem (geriye dönük uyumluluk)
    player-multiplier: 0.1
    clan-multiplier: 0.15
    
    # YENİ: Dinamik Zorluk Sistemi
    dynamic-difficulty:
      enabled: true
      power-scaling-factor: 1.0
      min-power-multiplier: 0.5
      max-power-multiplier: 5.0
      
      weights:
        structure: 0.3
        item: 0.4
        buff: 0.15
        training: 0.1
        clan-tech: 0.05
      
      structure-multipliers:
        battery: 2.0
        research-center: 1.5
        production: 1.2
        defense: 1.8
        default: 1.0
      
      player-count-multipliers:
        "1-3": 0.8
        "4-6": 1.0
        "7-10": 1.3
        "11-15": 1.6
        "16+": 2.0
```

---

## 🚀 BAŞLANGIÇ ADIMLARI

### **Adım 1: Hazırlık**
1. Mevcut kodları incele
2. Bağımlılıkları kontrol et
3. Config yapısını hazırla

### **Adım 2: PlayerPowerCalculator**
1. Sınıfı oluştur
2. Her hesaplama metodunu implement et
3. Test et

### **Adım 3: ServerPowerCalculator**
1. Sınıfı oluştur
2. Hesaplama metodunu implement et
3. Test et

### **Adım 4: DisasterManager Entegrasyonu**
1. calculateDisasterPower() güncelle
2. Config entegrasyonu
3. Test et

### **Adım 5: Config Güncellemesi**
1. config.yml'ye yeni ayarları ekle
2. Varsayılan değerleri ayarla
3. Test et

### **Adım 6: Test ve Dengeleme**
1. Farklı senaryolarda test
2. Güç hesaplama doğruluğu
3. Performans kontrolü
4. Dengeleme

---

## ⚠️ ÖNEMLİ NOTLAR

### **Performans**
- Güç hesaplama cache'lenmeli (her oyuncu için 5-10 saniyede bir)
- Tüm oyuncuları her tick'te hesaplama yapma
- Sadece felaket spawn olurken hesapla

### **Geriye Dönük Uyumluluk**
- Eski sistem flag ile korunmalı
- Config'den enable/disable edilebilmeli
- Varsayılan olarak yeni sistem aktif

### **Test Senaryoları**
1. Zayıf sunucu (3 oyuncu, düşük güç)
2. Orta güçlü sunucu (8 oyuncu, orta güç)
3. Çok güçlü sunucu (15 oyuncu, yüksek güç)
4. Tek oyuncu
5. Çok oyuncu (20+)

---

## 📊 İLERLEME TAKİBİ

- [ ] FAZ 1: Dinamik Zorluk Sistemi
  - [ ] PlayerPowerCalculator.java
  - [ ] ServerPowerCalculator.java
  - [ ] DisasterManager güncellemesi
  - [ ] Config.yml güncellemesi
  - [ ] Test ve dengeleme

- [ ] FAZ 2: Faz Sistemi
- [ ] FAZ 3: Özel Yetenekler
- [ ] FAZ 4: AI İyileştirmeleri
- [ ] FAZ 5: Çevresel Etkiler
- [ ] FAZ 6: Görsel/İşitsel Efektler
- [ ] FAZ 7: İşbirlikçi Mekanikler
- [ ] FAZ 8: Ödül/İlerleme Sistemi
- [ ] FAZ 9: Özel Senaryolar

---

**Son Güncelleme:** 2024
**Durum:** Planlama Tamamlandı - Uygulamaya Hazır

