# 🌋 FELAKET SİSTEMİ EPİK GELİŞTİRME ÖNERİLERİ

## 📋 İÇİNDEKİLER
1. [AI İyileştirmeleri](#1-ai-iyileştirmeleri)
2. [Faz Sistemi (Phase System)](#2-faz-sistemi-phase-system)
3. [Özel Yetenekler ve Hareketler](#3-özel-yetenekler-ve-hareketler)
4. [Çevresel Etkiler ve Korkutma](#4-çevresel-etkiler-ve-korkutma)
5. [Görsel ve İşitsel Efektler](#5-görsel-ve-işitsel-efektler)
6. [Dinamik Zorluk Sistemi](#6-dinamik-zorluk-sistemi)
7. [İşbirlikçi Mekanikler](#7-işbirlikçi-mekanikler)
8. [Ödül ve İlerleme Sistemi](#8-ödül-ve-ilerleme-sistemi)
9. [Özel Senaryolar ve Etkileşimler](#9-özel-senaryolar-ve-etkileşimler)

---

## 1. AI İYİLEŞTİRMELERİ

### 🧠 Akıllı Hedef Seçimi
**Mevcut Durum:** Felaketler sadece en yakın kristali hedefliyor
**Öneri:**
- **Stratejik Hedefleme:** En zayıf klanı (en az yapı, en düşük seviye) öncelikli hedefle
- **Çoklu Hedef:** Birden fazla kristali aynı anda hedefle (grup felaketler için)
- **Oyuncu Tehdit Analizi:** En çok hasar veren oyuncuları öncelikli hedefle
- **Yakındaki Tehditler:** Felakete saldıran oyuncuları otomatik hedefle

**Kod Örneği:**
```java
// DisasterAI.java
public Location selectBestTarget(Disaster disaster, Location current) {
    List<Clan> clans = territoryManager.getClanManager().getAllClans();
    
    // En zayıf klanı bul
    Clan weakestClan = clans.stream()
        .min(Comparator.comparingInt(c -> c.getStructures().size()))
        .orElse(null);
    
    // En yakın tehditli oyuncuyu bul
    Player threat = findHighestThreatPlayer(current, disaster);
    
    // Strateji: %70 zayıf klan, %30 tehditli oyuncu
    if (random.nextDouble() < 0.7 && weakestClan != null) {
        return weakestClan.getCrystalLocation();
    } else if (threat != null) {
        return threat.getLocation();
    }
    
    return findNearestCrystal(current);
}
```

### 🎯 Gelişmiş Pathfinding
**Mevcut Durum:** Basit direkt hareket
**Öneri:**
- **A* Pathfinding:** Engelleri akıllıca aş
- **Yol Optimizasyonu:** En kısa yolu bul, su/derinlik kontrolü
- **Dinamik Rota:** Oyuncu barikatlarını tespit et ve alternatif yol bul
- **Grup Koordinasyonu:** Grup felaketler için formasyon hareketi (V şekli, çember)

### 🏃 Akıllı Hareket Desenleri
**Öneri:**
- **Zigzag Hareket:** Düşman saldırılarından kaçın
- **Sprint Modu:** Kristale yaklaşınca hızlan
- **Geri Çekilme:** Can %30'un altına düşünce geçici geri çekilme
- **Flanking:** Oyuncuları çevreleme hareketi

---

## 2. FAZ SİSTEMİ (PHASE SYSTEM)

### 📊 Faz Geçişleri
**Öneri:** Her felaket 3-5 fazdan oluşsun, her fazda farklı davranış

**Faz 1: Keşif (100%-75% Can)**
- Normal hareket
- Temel saldırılar
- Kristal hedefleme

**Faz 2: Saldırı (75%-50% Can)**
- Daha agresif
- Özel yetenekler aktif
- Oyunculara daha sık saldırı

**Faz 3: Öfke (50%-25% Can)**
- Çok agresif
- Tüm özel yetenekler aktif
- Çevresel hasar artar

**Faz 4: Son Çare (25%-0% Can)**
- Umutsuz saldırılar
- Kendini feda etme yetenekleri
- Maksimum hasar

**Kod Örneği:**
```java
public enum DisasterPhase {
    EXPLORATION(1.0, 0.75, "Keşif"),
    ASSAULT(0.75, 0.50, "Saldırı"),
    RAGE(0.50, 0.25, "Öfke"),
    DESPERATION(0.25, 0.0, "Son Çare");
    
    public DisasterPhase getCurrentPhase(double healthPercent) {
        for (DisasterPhase phase : values()) {
            if (healthPercent <= phase.maxHealth && healthPercent > phase.minHealth) {
                return phase;
            }
        }
        return DESPERATION;
    }
}
```

---

## 3. ÖZEL YETENEKLER VE HAREKETLER

### ⚔️ Her Felaket Tipi İçin Özel Yetenekler

#### **Titan Golem:**
1. **Yer Sarsma (Ground Slam)**
   - Zıplar, yere iner, 20 blok yarıçapında şok dalgası
   - Oyuncuları havaya fırlatır
   - Blokları kırar

2. **Taş Fırlatma (Boulder Throw)**
   - Büyük taş blokları fırlatır
   - Hedef oyuncuya doğru parabolik atış
   - İsabet edince büyük hasar + yavaşlatma

3. **Taş Duvar (Stone Wall)**
   - Önünde 3x3 taş duvar oluşturur
   - Oyuncuları engeller
   - 10 saniye sonra patlar

#### **Khaos Ejderi:**
1. **Ateş Püskürtme (Fire Breath)**
   - 15 blok mesafeye kadar ateş püskürtür
   - Yerde lav bırakır
   - Yanıcı blokları tutuşturur

2. **Gökyüzü Saldırısı (Sky Dive)**
   - Gökyüzüne çıkar (Y+50)
   - Hedefe doğru dalış yapar
   - İnişte büyük patlama

3. **Ejder Ateşi Yağmuru (Dragon Fire Rain)**
   - Gökyüzünden ateş topları düşer
   - 30 blok yarıçapında rastgele hedefler
   - 10 saniye sürer

#### **Hiçlik Solucanı:**
1. **Yer Altına Dalış (Burrow)**
   - Yer altına girer
   - Oyuncuların altından çıkar
   - Sürpriz saldırı

2. **Hiçlik Çekimi (Void Pull)**
   - 15 blok yarıçapındaki oyuncuları kendine çeker
   - Yavaşlatma efekti
   - Sürekli hasar

3. **Yer Yarığı (Ground Split)**
   - Önünde 30 blok uzunluğunda yarık açar
   - Oyuncular düşerse hasar alır
   - 5 saniye sonra kapanır

#### **Buzul Leviathan:**
1. **Buz Fırtınası (Ice Storm)**
   - 20 blok yarıçapında buz fırtınası
   - Oyuncuları dondurur (yavaşlatma)
   - Su bloklarını buza çevirir

2. **Buz Duvarı (Ice Wall)**
   - Çevresinde buz duvarları oluşturur
   - Oyuncuları hapseder
   - 15 saniye sonra erir

3. **Buz Patlaması (Ice Explosion)**
   - Yerde buz kristalleri oluşturur
   - 3 saniye sonra patlar
   - Buz parçacıkları hasar verir

### 🎮 Yetenek Sistemi Mimarisi
```java
public interface DisasterAbility {
    String getName();
    long getCooldown();
    double getManaCost(); // Veya "rage" sistemi
    boolean canUse(Disaster disaster, Entity entity);
    void execute(Disaster disaster, Entity entity, Location target);
    void onPhaseChange(DisasterPhase newPhase);
}

// Örnek: Titan Golem Yer Sarsma
public class GroundSlamAbility implements DisasterAbility {
    @Override
    public void execute(Disaster disaster, Entity entity, Location target) {
        Giant golem = (Giant) entity;
        Location loc = golem.getLocation();
        
        // Zıplama animasyonu
        golem.setVelocity(new Vector(0, 1.5, 0));
        
        // 1 saniye sonra yere in
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Şok dalgası
            for (int radius = 1; radius <= 20; radius++) {
                for (double angle = 0; angle < 360; angle += 10) {
                    Location effectLoc = loc.clone().add(
                        Math.cos(angle) * radius,
                        0,
                        Math.sin(angle) * radius
                    );
                    effectLoc.getWorld().spawnParticle(
                        Particle.EXPLOSION_LARGE, effectLoc, 1
                    );
                }
            }
            
            // Oyunculara hasar
            loc.getWorld().getNearbyEntities(loc, 20, 20, 20)
                .stream()
                .filter(e -> e instanceof Player)
                .forEach(e -> {
                    ((Player) e).damage(20, golem);
                    e.setVelocity(new Vector(0, 1, 0)); // Havaya fırlat
                });
        }, 20L);
    }
}
```

---

## 4. ÇEVRESEL ETKİLER VE KORKUTMA

### 🌍 Çevre Değişiklikleri

#### **Felaket Yaklaşırken:**
1. **Gökyüzü Değişimi**
   - Gökyüzü kırmızı/turuncu olur
   - Bulutlar koyulaşır
   - Yıldırım efektleri

2. **Yer Titremesi**
   - Periyodik ekran sallanması
   - Bloklar titreşir
   - Partikül efektleri

3. **Hava Değişimi**
   - Rüzgar sesleri
   - Toz bulutları
   - Karanlık efektler

#### **Felaket Geldiğinde:**
1. **Blok Yıkımı**
   - Yol üzerindeki blokları yok et
   - Ağaçları devir
   - Yapıları hasarla

2. **Çevre Hasarı**
   - Su kaynaklarını kirlet (lav)
   - Toprağı çoraklaştır
   - Hayvanları kaçır

3. **Kalıcı İzler**
   - Yerde çatlaklar
   - Yanık izleri
   - Lav havuzları

### 🎭 Korkutma Mekanikleri

#### **Uyarı Sistemi:**
- **5 dakika önce:** Gökyüzü değişir, uyarı mesajları
- **2 dakika önce:** Yer titremesi başlar, sesler
- **30 saniye önce:** Ekran titremesi, korkutucu sesler
- **Geldiğinde:** Büyük patlama, ekran sarsılması

#### **Psikolojik Etkiler:**
- **Karanlık Mod:** Yakındaki oyunculara "Korku" efekti (görüş mesafesi azalır)
- **Ses Efektleri:** Korkutucu sesler (ejder kükremesi, yer sarsılması)
- **BossBar:** Kırmızı, titreyen, korkutucu

---

## 5. GÖRSEL VE İŞİTSEL EFEKTLER

### ✨ Partikül Efektleri

#### **Her Felaket İçin:**
- **Titan Golem:** Taş parçacıkları, toz bulutları, yer sarsılması
- **Khaos Ejderi:** Ateş, duman, lav damlaları
- **Hiçlik Solucanı:** Mor/siyah partiküller, hiçlik efektleri
- **Buzul Leviathan:** Buz parçacıkları, kar, soğuk buhar

#### **Özel Efektler:**
- **Kritik Hasar:** Büyük patlama, ekran titremesi
- **Faz Geçişi:** Büyük animasyon, ekran efekti
- **Ölüm:** Epik patlama, gökyüzü efekti

### 🔊 Ses Efektleri

#### **Ambient Sesler:**
- Felaket yaklaşırken: Korkutucu müzik
- Savaş sırasında: Epik müzik
- Faz geçişi: Dramatik ses

#### **Ses Efektleri:**
- Adım sesleri (büyük felaketler için)
- Saldırı sesleri
- Özel yetenek sesleri
- Ölüm sesi

---

## 6. DİNAMİK ZORLUK SİSTEMİ

### 📈 Adaptif Zorluk

#### **Oyuncu Sayısına Göre:**
- **1-3 oyuncu:** Normal zorluk
- **4-6 oyuncu:** %50 daha güçlü
- **7+ oyuncu:** %100 daha güçlü

#### **Klan Gücüne Göre:**
- Güçlü klanlar → Daha güçlü felaketler
- Zayıf klanlar → Daha zayıf felaketler

#### **Zaman İçinde Artış:**
- Her felaket sonrası %5 daha güçlü
- Maksimum %200'e kadar artabilir

### 🎯 Zorluk Modları

#### **Kolay (Easy):**
- %75 can
- %75 hasar
- Daha az özel yetenek

#### **Normal (Normal):**
- %100 can
- %100 hasar
- Tüm yetenekler

#### **Zor (Hard):**
- %150 can
- %150 hasar
- Daha sık özel yetenek

#### **Efsanevi (Legendary):**
- %200 can
- %200 hasar
- Sürekli özel yetenekler
- Ekstra fazlar

---

## 7. İŞBİRLİKÇİ MEKANİKLER

### 👥 Takım Çalışması

#### **Tank/DPS/Healer Sistemi:**
- **Tank:** Felaketi çeker, hasarı emer
- **DPS:** Yüksek hasar verir
- **Healer:** Takımı iyileştirir

#### **Koordinasyon Gerektiren Mekanikler:**
- **Zayıf Nokta:** Belirli bir yere saldırılması gereken zayıf nokta
- **Koruma Kalkanı:** Belirli oyuncuların koruması gereken alan
- **Zamanlama:** Belirli zamanlarda koordineli saldırı

### 🎯 Özel Etkileşimler

#### **Çevre Kullanımı:**
- **Yüksek Yerler:** Felaket yüksek yerlerden saldırı yapabilir
- **Su:** Buzul Leviathan suda daha güçlü
- **Lav:** Khaos Ejderi lavda daha güçlü

#### **Oyuncu Stratejileri:**
- **Barikat:** Oyuncular barikat kurabilir
- **Tuzak:** Felaket yolu üzerine tuzak kurulabilir
- **Çekme:** Felaketi belirli bir yere çekebilirler

---

## 8. ÖDÜL VE İLERLEME SİSTEMİ

### 🏆 Ödül Sistemi

#### **Temel Ödüller:**
- **Felaket Öldürme:** Büyük ödül
- **Kristal Koruma:** Bonus ödül
- **Faz Geçişi:** Her faz için küçük ödül

#### **Özel Ödüller:**
- **İlk Vuruş:** İlk hasar veren oyuncu
- **Son Vuruş:** Son hasar veren oyuncu
- **En Çok Hasar:** En çok hasar veren oyuncu
- **Takım Çalışması:** Takım halinde öldürme bonusu

#### **Nadir Ödüller:**
- **Felaket Parçaları:** Özel eşya yapımında kullanılır
- **Felaket Ruhu:** Güçlü buff için
- **Felaket Rozeti:** Başarı rozeti

### 📊 İlerleme Sistemi

#### **Felaket Defteri:**
- Her felaket tipini öldüren oyuncular için defter
- İstatistikler: Öldürme sayısı, hasar, süre
- Rozetler ve başarımlar

#### **Felaket Seviyesi:**
- Oyuncular felaket seviyesi kazanabilir
- Daha yüksek seviye = Daha iyi ödüller
- Özel yetenekler açılır

---

## 9. ÖZEL SENARYOLAR VE ETKİLEŞİMLER

### 🎬 Epik Anlar

#### **Felaket Girişi:**
- Gökyüzünden düşer
- Yerden çıkar
- Portal açılır
- Büyük patlama

#### **Faz Geçişi:**
- Ekran titremesi
- Büyük animasyon
- Ses efekti
- Mesaj

#### **Ölüm:**
- Yavaş çekim
- Büyük patlama
- Ödül yağmuru
- Zafer müziği

### 🎮 Mini Oyunlar

#### **Felaket Kaçışı:**
- Felaket geldiğinde oyuncular kaçmalı
- Belirli bir mesafeye ulaşmalı
- Başarılı olursa ödül

#### **Felaket Savunması:**
- Klan kristalini koruma görevi
- Belirli süre dayanma
- Başarılı olursa büyük ödül

---

## 🎯 ÖNCELİK SIRASI

### ⚡ Hızlı Kazanımlar (1-2 Gün)
1. Faz sistemi ekle
2. 2-3 özel yetenek ekle (her felaket için)
3. Görsel efektler iyileştir
4. Ses efektleri ekle

### 🚀 Orta Vadeli (1 Hafta)
1. AI iyileştirmeleri
2. Çevresel etkiler
3. Dinamik zorluk
4. Ödül sistemi iyileştirme

### 🏆 Uzun Vadeli (2-4 Hafta)
1. İşbirlikçi mekanikler
2. Özel senaryolar
3. İlerleme sistemi
4. Mini oyunlar

---

## 💡 EK ÖNERİLER

### 🎨 Görsel İyileştirmeler
- **BossBar Animasyonları:** Titreme, renk değişimi
- **Ekran Efektleri:** Hasar alınca kırmızı, faz geçişi efekti
- **UI İyileştirmeleri:** Felaket bilgisi, faz göstergesi

### 🎵 Ses İyileştirmeleri
- **Müzik Sistemi:** Felaket yaklaşırken müzik
- **Ses Efektleri:** Her yetenek için özel ses
- **Ambient Sesler:** Çevresel sesler

### 📱 Bildirim Sistemi
- **Uyarı Mesajları:** Felaket yaklaşırken
- **Faz Geçişi:** Faz değiştiğinde
- **Özel Yetenek:** Özel yetenek kullanıldığında

---

**Hazırlayan:** AI Assistant
**Tarih:** 2024
**Durum:** Öneriler - Uygulanmayı Bekliyor
