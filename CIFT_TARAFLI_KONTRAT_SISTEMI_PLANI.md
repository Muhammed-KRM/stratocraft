# ÇİFT TARAFLI KONTRAT SİSTEMİ - DETAYLI PLAN

## 📋 SİSTEM ÖZETİ

**Mevcut Sistem:** Tek taraflı (Bir kişi oluşturur, diğeri kabul eder)
**Yeni Sistem:** Çift taraflı (Her iki taraf da şartlar koyar, karşılıklı anlaşma)

---

## 🎯 SİSTEM MANTIĞI

### Temel Prensip
- **İki ayrı şart seti, tek kontrat**
- Her iki taraf da kendi şartlarını belirler
- Her iki taraf da onaylar
- Biri ihlal ederse → Her iki kontrat da biter → Sadece bozan kişi ceza alır

### Örnek Senaryo
```
Oyuncu A → Oyuncu B'ye: "Bana 50 elmas ver, 1 gün içinde"
Oyuncu B → Oyuncu A'ya: "Bana 500 kömür ver, 1 gün içinde"

Eğer Oyuncu A ihlal ederse:
- Her iki kontrat da biter
- Sadece Oyuncu A ceza alır
- Oyuncu B ceza almaz
```

---

## 📊 DETAYLI SÜREÇ AKIŞI

### **ADIM 1: Kontrat İsteği Gönderme (Oyuncu A)**

#### 1.1. Pusuladan Giriş
- Oyuncu A pusuladan (CONTRACT_PAPER) sağ tıklar
- Ana menü açılır

#### 1.2. Yeni Kontrat Seçimi
- Ana menüde **"Yeni Kontrat"** butonuna tıklar
- Kontrat tipi seçim menüsü açılır:
  - **Oyuncu-Oyuncu** (PLAYER_TO_PLAYER)
  - Klan-Klan (CLAN_TO_CLAN)
  - Oyuncu-Klan (PLAYER_TO_CLAN)
  - Klan-Oyuncu (CLAN_TO_PLAYER)

#### 1.3. Hedef Oyuncu Seçimi
- **Oyuncu-Oyuncu** seçilirse:
  - Hedef oyuncu seçim menüsü açılır
  - Online oyuncular listelenir (Oyuncu A hariç)
  - Oyuncu B seçilir
  - VEYA chat'ten oyuncu ismi yazılır

#### 1.4. İstek Gönderme
- Sistem **ContractRequest** oluşturur:
  ```java
  ContractRequest {
      UUID id;
      UUID sender;        // Oyuncu A
      UUID target;        // Oyuncu B
      ContractScope scope; // PLAYER_TO_PLAYER
      ContractRequestStatus status; // PENDING
      long createdAt;
  }
  ```
- İstek veritabanına kaydedilir
- Oyuncu A'ya mesaj: "§aKontrat isteği gönderildi! Oyuncu B'ye bildirim gidecek."
- Oyuncu B'ye bildirim: "§eOyuncu A size kontrat isteği gönderdi! Pusuladan kontrol edin."

**Durum:** İstek PENDING (Beklemede)

---

### **ADIM 2: İstek Görüntüleme ve Kabul (Oyuncu B)**

#### 2.1. Pusuladan Giriş
- Oyuncu B pusuladan sağ tıklar
- Ana menüde **"Gelen İstekler"** butonu görünür (yeni istek varsa)
- Buton üzerinde istek sayısı gösterilir: "§eGelen İstekler (1)"

#### 2.2. Gelen İstekler Menüsü
- Oyuncu B "Gelen İstekler" butonuna tıklar
- Gelen istekler listelenir:
  ```
  [Oyuncu A] - Oyuncu-Oyuncu Kontrat
  ──────────────────────────────
  Gönderen: Oyuncu A
  Tarih: 2 dakika önce
  ──────────────────────────────
  [Kabul Et] [Reddet]
  ```

#### 2.3. İstek Kabul
- Oyuncu B **"Kabul Et"** butonuna tıklar
- İstek durumu **ACCEPTED** olur
- Oyuncu A'ya bildirim: "§aOyuncu B kontrat isteğinizi kabul etti!"
- Oyuncu B'ye mesaj: "§aİstek kabul edildi! Şimdi şartlarınızı belirleyin."

**Durum:** İstek ACCEPTED (Kabul edildi)

---

### **ADIM 3: Şart Belirleme - Oyuncu A (İlk Taraf)**

#### 3.1. Bildirim ve Menü
- Oyuncu A pusulasında **"Kabul Edilen İstekler"** butonu görünür
- Buton üzerinde: "§aKabul Edilen İstekler (1)"
- Oyuncu A butona tıklar

#### 3.2. Kabul Edilen İstekler Menüsü
- Kabul edilen istekler listelenir:
  ```
  [Oyuncu B ile Kontrat]
  ──────────────────────────────
  Durum: Şartlarınızı Belirleyin
  ──────────────────────────────
  [Şartları Belirle]
  ```

#### 3.3. Şart Belirleme Wizard
- Oyuncu A **"Şartları Belirle"** butonuna tıklar
- Wizard başlar (mevcut wizard'a benzer):
  
  **Adım 1: Kontrat Tipi**
  - RESOURCE_COLLECTION
  - COMBAT
  - TERRITORY
  - CONSTRUCTION
  
  **Adım 2: Şart Detayları** (RESOURCE_COLLECTION için)
  - Malzeme seçimi
  - Miktar girişi
  - Süre belirleme (gün/saat/dakika)
  
  **Adım 3: Ödül ve Ceza**
  - Ödül miktarı (altın)
  - Ceza tipi (BANK_PENALTY, vb.)
  - Ceza miktarı (altın)
  
  **Adım 4: Özet ve Onay**
  - Şartlar özeti gösterilir:
    ```
    §7═══════════════════════
    §7§lSENİN ŞARTLARIN:
    §7Oyuncu B'den İstiyorsun:
    §7• 50 Elmas
    §7• 1 Gün İçinde
    §7• Ödül: 1000 Altın
    §7• Ceza: 500 Altın
    §7═══════════════════════
    §7[ONAYLA] [İPTAL]
    ```

#### 3.4. Şart Onaylama
- Oyuncu A **"ONAYLA"** butonuna tıklar
- Şartlar kaydedilir:
  ```java
  ContractTerms {
      UUID contractRequestId;
      UUID playerId;        // Oyuncu A
      ContractType type;    // RESOURCE_COLLECTION
      Material material;    // DIAMOND
      int amount;           // 50
      long deadline;        // 1 gün
      double reward;        // 1000
      PenaltyType penaltyType;
      double penalty;       // 500
      boolean approved;     // true
  }
  ```
- Oyuncu B'ye bildirim: "§eOyuncu A şartlarını belirledi! Şimdi sıra sizde."

**Durum:** Oyuncu A'nın şartları hazır, Oyuncu B bekleniyor

---

### **ADIM 4: Şart Belirleme - Oyuncu B (İkinci Taraf)**

#### 4.1. Bildirim ve Menü
- Oyuncu B pusulasında **"Kabul Edilen İstekler"** butonu görünür
- Buton üzerinde: "§aKabul Edilen İstekler (1)"
- Oyuncu B butona tıklar

#### 4.2. Kabul Edilen İstekler Menüsü
- İstek listelenir:
  ```
  [Oyuncu A ile Kontrat]
  ──────────────────────────────
  Durum: Şartlarınızı Belirleyin
  ──────────────────────────────
  [Şartları Belirle]
  ```

#### 4.3. Şart Belirleme Wizard
- Oyuncu B **"Şartları Belirle"** butonuna tıklar
- Aynı wizard açılır (Adım 3.3'e benzer)
- Oyuncu B kendi şartlarını belirler:
  ```
  §7═══════════════════════
  §7§lSENİN ŞARTLARIN:
  §7Oyuncu A'dan İstiyorsun:
  §7• 500 Kömür
  §7• 1 Gün İçinde
  §7• Ödül: 2000 Altın
  §7• Ceza: 1000 Altın
  §7═══════════════════════
  ```

#### 4.4. Şart Onaylama
- Oyuncu B **"ONAYLA"** butonuna tıklar
- Şartlar kaydedilir (Oyuncu B için ContractTerms oluşturulur)

**Durum:** Her iki tarafın şartları hazır

---

### **ADIM 5: Karşı Tarafın Şartlarını Görüntüleme**

#### 5.1. Her İki Taraf İçin
- Her iki oyuncu da pusulasından **"Kabul Edilen İstekler"** menüsüne girer
- İstek durumu: **"Şartlar Hazır - Onay Bekleniyor"**

#### 5.2. Şartları Görüntüleme
- Oyuncu A tıklarsa:
  ```
  [Oyuncu B ile Kontrat]
  ──────────────────────────────
  §7§lSENİN ŞARTLARIN:
  §7• 50 Elmas (Oyuncu B'den)
  §7• 1 Gün İçinde
  §7• Ödül: 1000 Altın
  §7
  §7§lOYUNCU B'NİN ŞARTLARI:
  §7• 500 Kömür (Senden)
  §7• 1 Gün İçinde
  §7• Ödül: 2000 Altın
  §7═══════════════════════
  §7[Kontratı Onayla] [İptal]
  ```

#### 5.3. Final Onay
- Her iki oyuncu da **"Kontratı Onayla"** butonuna tıklar
- Sistem kontrol eder:
  - Her iki tarafın şartları hazır mı? ✓
  - Her iki taraf da onayladı mı? ✓
- Kontrat aktif hale gelir:
  ```java
  Contract {
      UUID id;
      UUID playerA;           // Oyuncu A
      UUID playerB;           // Oyuncu B
      ContractTerms termsA;   // Oyuncu A'nın şartları
      ContractTerms termsB;   // Oyuncu B'nin şartları
      ContractStatus status;  // ACTIVE
      long startedAt;
  }
  ```

#### 5.4. Kan İmzası
- Her iki oyuncuya da kan imzası uygulanır:
  - Her biri **1 kalp** (2 can) kaybeder
  - Mesaj: "§cKan imzası! 1 kalp kaybettiniz."
  - Mesaj: "§7Kontrat tamamlandığında kalp geri verilecek."

**Durum:** Kontrat ACTIVE (Aktif)

---

### **ADIM 6: Kontrat Tamamlama**

#### 6.1. Görev Tamamlama
- **Oyuncu B** görevini tamamlar:
  - 50 elmas toplar
  - `/kontrat teslim <contract_id> <miktar>` komutunu kullanır
  - Sistem kontrol eder:
    - Oyuncu B'nin şartı (50 elmas) tamamlandı mı? ✓
    - Envanterde yeterli malzeme var mı? ✓
  - Malzeme Oyuncu A'ya verilir (envanter veya klan bankası)
  - `termsB.delivered += miktar`
  
- **Oyuncu A** görevini tamamlar:
  - 500 kömür toplar
  - `/kontrat teslim <contract_id> <miktar>` komutunu kullanır
  - Sistem kontrol eder:
    - Oyuncu A'nın şartı (500 kömür) tamamlandı mı? ✓
  - Malzeme Oyuncu B'ye verilir
  - `termsA.delivered += miktar`

#### 6.2. Kontrat Tamamlanma Kontrolü
- Sistem her teslimatta kontrol eder:
  ```java
  if (termsA.isCompleted() && termsB.isCompleted()) {
      // Kontrat tamamlandı!
      completeContract(contract);
  }
  ```

#### 6.3. Ödül Ödemesi
- Her iki tarafın ödülü ödenir:
  - Oyuncu B'nin ödülü (1000 altın) → Oyuncu A'nın klan bankasından çekilir → Oyuncu B'ye verilir
  - Oyuncu A'nın ödülü (2000 altın) → Oyuncu B'nin klan bankasından çekilir → Oyuncu A'ya verilir

#### 6.4. Kan İmzası Geri Ödeme
- Her iki oyuncuya da kalp geri verilir:
  - Her biri **+1 kalp** (2 can) kazanır
  - Mesaj: "§aKontrat tamamlandı! Kalp geri verildi."

**Durum:** Kontrat COMPLETED (Tamamlandı)

---

### **ADIM 7: Kontrat İhlali**

#### 7.1. İhlal Durumu
- Eğer bir taraf görevini tamamlamazsa:
  - Süre dolduğunda otomatik ihlal
  - VEYA manuel ihlal (sistem tarafından)

#### 7.2. İhlal Tespiti
- Sistem kontrol eder:
  ```java
  if (termsA.isBreached() || termsB.isBreached()) {
      // Kontrat ihlal edildi!
      breachContract(contract);
  }
  ```

#### 7.3. Ceza Uygulama
- **Sadece ihlal eden kişi ceza alır:**
  - Eğer Oyuncu A ihlal ettiyse:
    - Sadece Oyuncu A'nın cezası uygulanır (termsA.penalty)
    - Oyuncu B ceza almaz
  - Eğer Oyuncu B ihlal ettiyse:
    - Sadece Oyuncu B'nin cezası uygulanır (termsB.penalty)
    - Oyuncu A ceza almaz

#### 7.4. Her İki Kontratın Bitmesi
- İhlal edildiğinde:
  - `termsA.status = BREACHED`
  - `termsB.status = BREACHED`
  - `contract.status = BREACHED`
  - Her iki kontrat da biter

#### 7.5. Bildirimler
- İhlal eden oyuncuya:
  - "§c§lKONTRAT İHLAL EDİLDİ!"
  - "§7Ceza: 500 Altın"
- Diğer oyuncuya:
  - "§eKontrat ihlal edildi. Karşı taraf ceza aldı."

**Durum:** Kontrat BREACHED (İhlal edildi)

---

## 🗂️ VERİ YAPILARI

### ContractRequest
```java
public class ContractRequest {
    private UUID id;
    private UUID sender;              // İstek gönderen
    private UUID target;              // İstek alan
    private ContractScope scope;      // PLAYER_TO_PLAYER, vb.
    private ContractRequestStatus status; // PENDING, ACCEPTED, REJECTED, CANCELLED
    private long createdAt;
    private long respondedAt;         // Kabul/red zamanı
}
```

### ContractTerms
```java
public class ContractTerms {
    private UUID id;
    private UUID contractRequestId;   // Hangi isteğe ait
    private UUID playerId;           // Şartları koyan oyuncu
    private ContractType type;       // RESOURCE_COLLECTION, vb.
    
    // RESOURCE_COLLECTION için
    private Material material;
    private int amount;
    private int delivered = 0;
    
    // COMBAT için
    private UUID targetPlayer;
    
    // Genel
    private long deadline;           // Süre (milisaniye)
    private double reward;           // Ödül (altın)
    private PenaltyType penaltyType;
    private double penalty;          // Ceza (altın)
    
    private boolean approved = false; // Oyuncu onayladı mı?
    private boolean completed = false;
    private boolean breached = false;
}
```

### Contract (Güncellenmiş)
```java
public class Contract {
    private UUID id;
    private UUID playerA;            // İlk oyuncu
    private UUID playerB;            // İkinci oyuncu
    private ContractRequest originalRequest; // Orijinal istek
    
    private ContractTerms termsA;    // Oyuncu A'nın şartları
    private ContractTerms termsB;    // Oyuncu B'nin şartları
    
    private ContractStatus status;   // ACTIVE, COMPLETED, BREACHED
    private long startedAt;          // Aktif olma zamanı
    private long completedAt;        // Tamamlanma zamanı
    private long breachedAt;         // İhlal zamanı
    private UUID breacher;           // İhlal eden oyuncu
}
```

### Enum'lar
```java
public enum ContractRequestStatus {
    PENDING,      // Beklemede
    ACCEPTED,     // Kabul edildi
    REJECTED,     // Reddedildi
    CANCELLED     // İptal edildi
}

public enum ContractStatus {
    PENDING_TERMS_A,    // Oyuncu A şartlarını belirliyor
    PENDING_TERMS_B,    // Oyuncu B şartlarını belirliyor
    PENDING_APPROVAL,   // Her iki taraf da onay bekleniyor
    ACTIVE,             // Aktif
    COMPLETED,          // Tamamlandı
    BREACHED            // İhlal edildi
}
```

---

## 🎨 GUI MENÜLERİ

### 1. Ana Menü (Güncellenmiş)
```
┌─────────────────────────────┐
│  §6Kontrat Menüsü           │
├─────────────────────────────┤
│ [Yeni Kontrat]              │ ← Yeni buton
│ [Gelen İstekler] (1)        │ ← Yeni buton (sayı gösterir)
│ [Kabul Edilen İstekler] (1) │ ← Yeni buton (sayı gösterir)
│ [Aktif Kontratlarım]        │
│ [Benim Kontratlarım]        │
│ [Kabul Ettiğim Kontratlar]  │
│ [Kontrat Geçmişi]           │
└─────────────────────────────┘
```

### 2. Gelen İstekler Menüsü
```
┌─────────────────────────────┐
│  §eGelen İstekler           │
├─────────────────────────────┤
│ [Oyuncu A]                  │
│ ─────────────────────────── │
│ Gönderen: Oyuncu A          │
│ Tarih: 2 dakika önce         │
│ ─────────────────────────── │
│ [§aKabul Et] [§cReddet]      │
│                             │
│ [Geri]                      │
└─────────────────────────────┘
```

### 3. Kabul Edilen İstekler Menüsü
```
┌─────────────────────────────┐
│  §aKabul Edilen İstekler    │
├─────────────────────────────┤
│ [Oyuncu B ile Kontrat]      │
│ ─────────────────────────── │
│ Durum: Şartlarınızı Belirleyin│
│ ─────────────────────────── │
│ [§eŞartları Belirle]        │
│                             │
│ [Geri]                      │
└─────────────────────────────┘
```

### 4. Şart Görüntüleme Menüsü (Her İki Taraf Onayladıktan Sonra)
```
┌─────────────────────────────┐
│  §6Kontrat Şartları          │
├─────────────────────────────┤
│ §7§lSENİN ŞARTLARIN:        │
│ • 50 Elmas (Oyuncu B'den)   │
│ • 1 Gün İçinde              │
│ • Ödül: 1000 Altın          │
│                             │
│ §7§lOYUNCU B'NİN ŞARTLARI:  │
│ • 500 Kömür (Senden)        │
│ • 1 Gün İçinde              │
│ • Ödül: 2000 Altın          │
│ ─────────────────────────── │
│ [§aKontratı Onayla]         │
│ [§cİptal]                   │
└─────────────────────────────┘
```

---

## 🔧 TEKNİK DETAYLAR

### 1. Veritabanı Tabloları

#### contract_requests
```sql
CREATE TABLE contract_requests (
    id VARCHAR(36) PRIMARY KEY,
    sender VARCHAR(36) NOT NULL,
    target VARCHAR(36) NOT NULL,
    scope VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at BIGINT NOT NULL,
    responded_at BIGINT
);
```

#### contract_terms
```sql
CREATE TABLE contract_terms (
    id VARCHAR(36) PRIMARY KEY,
    contract_request_id VARCHAR(36) NOT NULL,
    player_id VARCHAR(36) NOT NULL,
    contract_type VARCHAR(30) NOT NULL,
    material VARCHAR(30),
    amount INT,
    delivered INT DEFAULT 0,
    target_player VARCHAR(36),
    deadline BIGINT NOT NULL,
    reward DOUBLE NOT NULL,
    penalty_type VARCHAR(20),
    penalty DOUBLE NOT NULL,
    approved BOOLEAN DEFAULT FALSE,
    completed BOOLEAN DEFAULT FALSE,
    breached BOOLEAN DEFAULT FALSE
);
```

#### contracts (Güncellenmiş)
```sql
CREATE TABLE contracts (
    id VARCHAR(36) PRIMARY KEY,
    player_a VARCHAR(36) NOT NULL,
    player_b VARCHAR(36) NOT NULL,
    contract_request_id VARCHAR(36) NOT NULL,
    terms_a_id VARCHAR(36) NOT NULL,
    terms_b_id VARCHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL,
    started_at BIGINT,
    completed_at BIGINT,
    breached_at BIGINT,
    breacher VARCHAR(36)
);
```

### 2. Manager Sınıfları

#### ContractRequestManager
```java
public class ContractRequestManager {
    // İstek gönderme
    public ContractRequest sendRequest(UUID sender, UUID target, ContractScope scope);
    
    // İstek kabul/red
    public boolean acceptRequest(UUID requestId, UUID playerId);
    public boolean rejectRequest(UUID requestId, UUID playerId);
    
    // İstek listeleme
    public List<ContractRequest> getPendingRequests(UUID playerId);
    public List<ContractRequest> getAcceptedRequests(UUID playerId);
    
    // İstek iptal
    public boolean cancelRequest(UUID requestId, UUID playerId);
}
```

#### ContractTermsManager
```java
public class ContractTermsManager {
    // Şart oluşturma
    public ContractTerms createTerms(UUID requestId, UUID playerId, ContractWizardState state);
    
    // Şart güncelleme
    public boolean updateTerms(UUID termsId, ContractWizardState state);
    
    // Şart onaylama
    public boolean approveTerms(UUID termsId, UUID playerId);
    
    // Şart listeleme
    public ContractTerms getTermsByRequest(UUID requestId, UUID playerId);
    public List<ContractTerms> getTermsByPlayer(UUID playerId);
}
```

#### ContractManager (Güncellenmiş)
```java
public class ContractManager {
    // Kontrat oluşturma (her iki şart hazır olduğunda)
    public Contract createContract(ContractRequest request, ContractTerms termsA, ContractTerms termsB);
    
    // Kontrat tamamlama
    public void completeContract(UUID contractId);
    
    // Kontrat ihlal
    public void breachContract(UUID contractId, UUID breacher);
    
    // Teslim etme (güncellenmiş)
    public void deliverContract(UUID contractId, UUID playerId, int amount);
}
```

### 3. GUI Menü Sınıfları

#### ContractMenu (Güncellenmiş)
```java
public class ContractMenu {
    // Yeni menüler
    public void openIncomingRequestsMenu(Player player, int page);
    public void openAcceptedRequestsMenu(Player player, int page);
    public void openTermsWizard(Player player, UUID requestId);
    public void openTermsViewMenu(Player player, UUID requestId);
    
    // Event handler'lar
    private void handleIncomingRequestsClick(InventoryClickEvent event);
    private void handleAcceptedRequestsClick(InventoryClickEvent event);
    private void handleTermsWizardClick(InventoryClickEvent event);
}
```

---

## 📝 İMPLEMENTASYON ADIMLARI

### Faz 1: Veri Yapıları (1-2 saat)
1. ✅ ContractRequest sınıfı oluştur
2. ✅ ContractTerms sınıfı oluştur
3. ✅ Contract sınıfını güncelle
4. ✅ Enum'ları ekle (ContractRequestStatus, ContractStatus)
5. ✅ Veritabanı tablolarını oluştur

### Faz 2: Manager Sınıfları (2-3 saat)
1. ✅ ContractRequestManager oluştur
2. ✅ ContractTermsManager oluştur
3. ✅ ContractManager'ı güncelle
4. ✅ Veritabanı işlemlerini ekle

### Faz 3: GUI Menüleri (3-4 saat)
1. ✅ Ana menüyü güncelle (yeni butonlar)
2. ✅ Gelen İstekler menüsü
3. ✅ Kabul Edilen İstekler menüsü
4. ✅ Şart belirleme wizard'ı (mevcut wizard'ı adapte et)
5. ✅ Şart görüntüleme menüsü

### Faz 4: İş Mantığı (2-3 saat)
1. ✅ İstek gönderme akışı
2. ✅ İstek kabul/red akışı
3. ✅ Şart belirleme akışı
4. ✅ Final onay akışı
5. ✅ Kontrat aktifleştirme
6. ✅ Teslim etme (güncellenmiş)
7. ✅ İhlal kontrolü (güncellenmiş)

### Faz 5: Bildirimler ve Mesajlar (1 saat)
1. ✅ İstek gönderme bildirimleri
2. ✅ İstek kabul bildirimleri
3. ✅ Şart belirleme bildirimleri
4. ✅ Final onay bildirimleri
5. ✅ Kontrat tamamlama bildirimleri
6. ✅ İhlal bildirimleri

### Faz 6: Test ve Hata Düzeltme (2-3 saat)
1. ✅ Unit testler
2. ✅ Entegrasyon testleri
3. ✅ Hata düzeltmeleri
4. ✅ Performans optimizasyonları

**Toplam Süre:** ~12-16 saat

---

## ⚠️ ÖNEMLİ NOTLAR

### 1. Geriye Uyumluluk
- Mevcut tek taraflı kontratlar çalışmaya devam etmeli
- Yeni sistem sadece PLAYER_TO_PLAYER için geçerli olabilir
- VEYA tüm scope'lar için geçerli olabilir (tasarım kararı)

### 2. İstek İptal
- İstek gönderen kişi isteği iptal edebilmeli (PENDING durumunda)
- Kabul edilmiş istek iptal edilemez (şart belirleme aşamasında)

### 3. Süre Kontrolü
- Her iki şartın da ayrı deadline'ı var
- Sistem her iki deadline'ı da kontrol etmeli
- Biri dolduğunda o şart ihlal edilir

### 4. Ödül Ödemesi
- Her iki tarafın ödülü ayrı ayrı ödenir
- Ödül, karşı tarafın klan bankasından çekilir
- Eğer klan yoksa, oyuncunun kişisel bakiyesinden çekilir

### 5. Ceza Uygulama
- Sadece ihlal eden kişi ceza alır
- Ceza, ihlal eden kişinin klan bankasından çekilir
- Eğer klan yoksa, oyuncunun kişisel bakiyesinden çekilir

---

## 🎯 SONUÇ

Bu sistem:
- ✅ Mantıklı ve yapılabilir
- ✅ Çift taraflı anlaşma sağlar
- ✅ Adil ceza sistemi (sadece bozan ceza alır)
- ✅ Esnek şart belirleme
- ✅ Kullanıcı dostu GUI

**Öneri:** Önce Faz 1-2'yi tamamla, sonra GUI'yi yap. Bu şekilde backend hazır olur, GUI sadece görsel katman olur.
