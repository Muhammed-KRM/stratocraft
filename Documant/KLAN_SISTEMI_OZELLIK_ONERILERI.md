# 🏛️ KLAN SİSTEMİ ÖZELLİK ÖNERİLERİ

## 📋 İÇİNDEKİLER

1. [Giriş ve Ana Mantık](#giris)
2. [Kritik Özellikler](#kritik-ozellikler)
3. [Klan Yönetimi Geliştirmeleri](#klan-yonetimi)
4. [Savaş Sistemi Geliştirmeleri](#savas-sistemi)
5. [Item-Based Ekonomi Sistemi](#ekonomi) ⭐ **YENİ**
6. [Karmaşık İşlemler İçin Hibrit Sistem](#hibrit-sistem) ⭐ **YENİ**
7. [Sosyal ve Etkileşim Özellikleri](#sosyal)
8. [İlerleme ve Ödül Sistemleri](#ilerleme)
9. [Teknik Uygulama Detayları](#teknik)

---

## 🎯 GİRİŞ VE ANA MANTIK {#giris}

### Mevcut Sistemin Temel Prensipleri

**✅ Korunması Gerekenler:**
- **Fiziksel Etkileşim**: Komut yok, her şey fiziksel (ritüeller, çitler, kristaller)
- **5 Dakika Warmup**: Savaş ilanından sonra 5 dakika hazırlık süresi
- **24 Saat Grace Period**: Yeni kurulan klanlar 24 saat korunur
- **Güç Bazlı Koruma**: Mevcut güç sistemi koruma sağlıyor (%50 eşik)
- **Rütbe Sistemi**: Leader, General, Member, Recruit (fiziksel terfi ritüeli)
- **İttifak Sistemi**: Liderler arası fiziksel ritüel ile kurulur
- **Pes Etme**: Beyaz Bayrak ile pes etme (klan dağılmaz)

**⚠️ İyileştirilebilir Noktalar:**
- Seviye koruma sistemi güç sistemi ile uyumlu değil (3 seviye farkı eski sistem için)
- Klan yönetimi daha detaylı olabilir
- Üye aktivite takibi eksik
- Bireysel görev sistemi (lonca) yok
- Klan içi görev/ödül sistemi yok
- Klan istatistikleri sınırlı
- Klan içi kaynak paylaşımı basit (yetkiye göre detaylandırılmalı)

---

## 🔥 KRİTİK ÖZELLİKLER {#kritik-ozellikler}

### 0. Gelişmiş Oyuncu Koruma Sistemi ⭐ **YÜKSEK ÖNCELİK**

#### Mevcut Durum
- ✅ Güç bazlı koruma var (%50 eşik)
- ✅ Acemi koruması var (5000 güç altı)
- ✅ Klan içi koruma var (%60 eşik)
- ⚠️ Seviye bazlı koruma yok (eski 3 seviye kuralı artık uygun değil)
- ⚠️ Aktivite bazlı koruma yok
- ⚠️ Yeni oyuncu koruması eksik

#### Öneri: Hibrit Koruma Sistemi (Güç + Seviye + Aktivite)

**Neden Gerekli:**
- Yeni güç sistemi ile seviye hesaplama değişti (logaritmik)
- 3 seviye farkı artık çok fazla veya çok az olabilir
- Güç bazlı koruma daha adil ama seviye bazlı da gerekli
- Yeni oyuncuları daha iyi korumak gerekiyor

**Önerilen Koruma Sistemi:**

**1. Hibrit Koruma (Güç + Seviye)**
```yaml
Koruma Kuralları (Öncelik Sırası):

1. Klan Savaşı İstisnası (En Yüksek Öncelik):
   - Aktif savaşta tüm korumalar devre dışı
   - Stratejik saldırılar yapılabilir

2. Güç Bazlı Koruma (Mevcut - İyileştirilmiş):
   - Hedef Gücü < Saldıran Gücü × 0.40 ise → Saldırı YASAK
   - (Mevcut: %50, Önerilen: %40 - daha dengeli)
   - Neden: Güçlü oyuncular zayıflara saldıramaz

3. Seviye Bazlı Koruma (YENİ - Güç Sistemi ile Uyumlu):
   - Seviye Farkı > 5 ise → Saldırı YASAK
   - (Eski: 3 seviye, Yeni: 5 seviye - logaritmik sistem için)
   - Neden: Seviye 15 vs Seviye 8 çok büyük fark

4. Acemi Koruması (Mevcut - İyileştirilmiş):
   - Hedef Gücü < 3,000 VE Seviye < 5 ise → Saldırı YASAK
   - (Mevcut: 5,000 güç, Önerilen: 3,000 güç + Seviye kontrolü)
   - Neden: Yeni oyuncuları daha iyi korur

5. Aktivite Bazlı Koruma (YENİ):
   - Hedef 7 gün offline ise → Saldırı YASAK
   - Neden: Offline oyuncuları korur

6. Klan İçi Koruma (Mevcut - İyileştirilmiş):
   - Klan içinde: Hedef Gücü < Saldıran Gücü × 0.50 ise → Saldırı YASAK
   - (Mevcut: %60, Önerilen: %50 - daha dengeli)
   - Neden: Klan içi daha esnek olmalı ama yine de koruma olmalı
```

**Teknik Uygulama:**
```java
public class AdvancedProtectionSystem {
    /**
     * Gelişmiş koruma kontrolü (hibrit sistem)
     */
    public boolean canAttackPlayer(Player attacker, Player target) {
        // 1. Klan savaşı kontrolü (en yüksek öncelik)
        if (isClanAtWar(attacker, target)) {
            return true; // Savaşta herkes herkese saldırabilir
        }
        
        // Güç ve seviye hesapla
        PlayerPowerProfile attackerProfile = powerSystem.calculatePlayerProfile(attacker);
        PlayerPowerProfile targetProfile = powerSystem.calculatePlayerProfile(target);
        
        double attackerPower = attackerProfile.getTotalSGP();
        double targetPower = targetProfile.getTotalSGP();
        int attackerLevel = attackerProfile.getPlayerLevel();
        int targetLevel = targetProfile.getPlayerLevel();
        
        // 2. Güç bazlı koruma (%40 eşik)
        double powerThreshold = attackerPower * balanceConfig.getProtectionPowerThreshold(); // 0.40
        if (targetPower < powerThreshold) {
            attacker.sendMessage("§cBu oyuncu senin dengin değil! (Güç: " + 
                String.format("%.0f", targetPower) + " < " + 
                String.format("%.0f", powerThreshold) + ")");
            return false;
        }
        
        // 3. Seviye bazlı koruma (5 seviye farkı)
        int levelDiff = attackerLevel - targetLevel;
        int maxLevelDiff = balanceConfig.getProtectionMaxLevelDiff(); // 5
        if (levelDiff > maxLevelDiff) {
            attacker.sendMessage("§cSeviye farkı çok büyük! (Sen: " + attackerLevel + 
                ", Hedef: " + targetLevel + ", Fark: " + levelDiff + ")");
            return false;
        }
        
        // 4. Acemi koruması (3,000 güç + Seviye 5 altı)
        double rookiePowerThreshold = balanceConfig.getRookiePowerThreshold(); // 3,000
        int rookieLevelThreshold = balanceConfig.getRookieLevelThreshold(); // 5
        if (targetPower < rookiePowerThreshold && targetLevel < rookieLevelThreshold) {
            attacker.sendMessage("§cBu oyuncu çok yeni! Onurlu bir savaş değil.");
            return false;
        }
        
        // 5. Aktivite bazlı koruma (7 gün offline)
        long lastActivity = activitySystem.getLastActivity(target.getUniqueId());
        long inactiveThreshold = 604800000L; // 7 gün (ms)
        if (System.currentTimeMillis() - lastActivity > inactiveThreshold) {
            attacker.sendMessage("§cBu oyuncu uzun süredir offline! Saldırı yapılamaz.");
            return false;
        }
        
        // 6. Klan içi koruma (%50 eşik)
        Clan attackerClan = clanManager.getClanByPlayer(attacker.getUniqueId());
        Clan targetClan = clanManager.getClanByPlayer(target.getUniqueId());
        if (attackerClan != null && attackerClan.equals(targetClan)) {
            double clanThreshold = attackerPower * balanceConfig.getClanProtectionThreshold(); // 0.50
            if (targetPower < clanThreshold) {
                attacker.sendMessage("§cKlan içinde güçsüz üyelere saldıramazsın!");
                return false;
            }
        }
        
        return true; // Tüm kontroller geçti
    }
    
    /**
     * Hasar azaltma (koruma aktifse)
     */
    public double calculateDamageReduction(Player attacker, Player target) {
        if (canAttackPlayer(attacker, target)) {
            return 1.0; // Normal hasar
        }
        
        // Koruma aktifse hasar azalt
        PlayerPowerProfile attackerProfile = powerSystem.calculatePlayerProfile(attacker);
        PlayerPowerProfile targetProfile = powerSystem.calculatePlayerProfile(target);
        
        double attackerPower = attackerProfile.getTotalSGP();
        double targetPower = targetProfile.getTotalSGP();
        double powerRatio = targetPower / attackerPower;
        
        // Güç oranına göre hasar azaltma
        // %40'ın altındaysa %95 hasar azaltma
        if (powerRatio < 0.40) {
            return 0.05; // %5 hasar (eski sistem: %95 azaltma)
        }
        
        // %40-50 arasıysa kademeli azaltma
        if (powerRatio < 0.50) {
            double reduction = 0.05 + ((powerRatio - 0.40) / 0.10) * 0.45; // %5-%50 arası
            return reduction;
        }
        
        return 1.0; // Normal hasar
    }
}
```

**Config Ayarları:**
```yaml
protection-system:
  # Güç bazlı koruma
  power-threshold: 0.40  # %40 (eski: 0.50)
  
  # Seviye bazlı koruma
  max-level-diff: 5      # Maksimum seviye farkı (eski: 3)
  
  # Acemi koruması
  rookie-power-threshold: 3000.0  # Güç eşiği (eski: 5000.0)
  rookie-level-threshold: 5       # Seviye eşiği (YENİ)
  
  # Aktivite koruması
  inactive-threshold: 604800000   # 7 gün (ms)
  
  # Klan içi koruma
  clan-threshold: 0.50  # %50 (eski: 0.60)
  
  # Hasar azaltma
  damage-reduction-min: 0.05  # Minimum hasar (%5)
  damage-reduction-max: 0.50  # Maksimum hasar (%50)
```

**Neden Bu Değerler?**
- **%40 Güç Eşiği**: %50 çok yüksek, %30 çok düşük. %40 dengeli.
- **5 Seviye Farkı**: Logaritmik sistemde seviye 15 vs 8 çok büyük fark. 5 seviye daha mantıklı.
- **3,000 Güç + Seviye 5**: Yeni oyuncuları daha iyi korur. Sadece güç yeterli değil.
- **7 Gün Offline**: Offline oyuncuları korur ama çok uzun değil.

---

### 1. Klan Seviye Bazlı Bonuslar (Sınır Yok, Sadece Bonuslar) ⭐ **YÜKSEK ÖNCELİK**

#### Mevcut Durum
- ✅ Klan seviyesi hesaplanıyor (güç bazlı)
- ✅ Sınırsız üye, sınırsız toprak (özgürlük felsefesi)
- ⚠️ Seviye bazlı bonuslar yok

#### Öneri: Seviye Bazlı Bonuslar (Sınır Yok, Sadece Avantajlar)

**Felsefe:**
- **Sınır Yok**: Klanlar istediği kadar üye, toprak, yapı sahibi olabilir
- **Bonus Var**: Yüksek seviye klanlar ekstra avantajlar kazanır
- **Özgürlük**: Sunucu büyüdükçe (3000+ kişi) sınırlar sorun yaratmaz

**Önerilen Bonuslar (Sınır Değil, Avantaj):**
```yaml
Klan Seviyesi 1-3 (Başlangıç):
  - Bonus: Temel klan chat
  - Bonus: Klan bankası (sınırsız)
  - Özellik: Temel klan özellikleri

Klan Seviyesi 4-7 (Gelişmiş):
  - Bonus: %5 daha fazla güç (klan üyelerine)
  - Bonus: Klan içi market erişimi
  - Bonus: İttifak sistemi
  - Özellik: Gelişmiş klan özellikleri

Klan Seviyesi 8-12 (Güçlü):
  - Bonus: %10 daha fazla güç (klan üyelerine)
  - Bonus: Klan savaşları (ekstra ödüller)
  - Bonus: Özel yapılar erişimi
  - Özellik: Güçlü klan özellikleri

Klan Seviyesi 13-15 (Efsanevi):
  - Bonus: %15 daha fazla güç (klan üyelerine)
  - Bonus: Klan başkenti sistemi
  - Bonus: Özel event'lere erişim
  - Bonus: Özel rozetler ve unvanlar
  - Özellik: Efsanevi klan özellikleri
```

**Teknik Uygulama:**
```java
public class ClanLevelBonuses {
    private GameBalanceConfig balanceConfig;
    
    /**
     * Klan seviyesine göre güç bonusu
     */
    public double getClanPowerBonus(Clan clan) {
        int level = calculateClanLevel(clan);
        if (level <= 3) return 0.0; // Bonus yok
        if (level <= 7) return balanceConfig.getClanLevel4PowerBonus(); // %5
        if (level <= 12) return balanceConfig.getClanLevel8PowerBonus(); // %10
        return balanceConfig.getClanLevel13PowerBonus(); // %15
    }
    
    /**
     * Klan seviyesine göre özellik kontrolü
     */
    public boolean hasClanFeature(Clan clan, ClanFeature feature) {
        int level = calculateClanLevel(clan);
        return feature.getRequiredLevel() <= level;
    }
    
    /**
     * Klan üyelerine bonus uygula
     */
    public void applyClanBonuses(Clan clan) {
        double powerBonus = getClanPowerBonus(clan);
        if (powerBonus > 0) {
            for (UUID memberId : clan.getMembers().keySet()) {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null && member.isOnline()) {
                    // Güç bonusu uygula (BuffManager ile)
                    buffManager.applyClanPowerBonus(member, powerBonus);
                }
            }
        }
    }
}
```

**Nasıl Çalışır:**
- **Sınır Yok**: Klanlar istediği kadar üye ekleyebilir, toprak genişletebilir, yapı inşa edebilir
- **Bonus Var**: Yüksek seviye klanlar ekstra güç, özellikler, erişimler kazanır
- **Özgürlük**: Sunucu büyüdükçe sınırlar sorun yaratmaz, sadece bonuslar motivasyon sağlar

**Not:** Mevcut fiziksel etkileşim korunur, sadece bonuslar eklenir. Sınır yok!

---

### 2. Gelişmiş Rütbe Sistemi ⭐ **YÜKSEK ÖNCELİK**

#### Mevcut Durum
- ✅ Temel rütbeler var (Leader, General, Member, Recruit)
- ✅ Fiziksel terfi ritüeli var
- ⚠️ Rütbe bazlı yetkiler sınırlı

#### Öneri: Detaylı Yetki Sistemi (Ana Mantık Korunuyor)

**Yeni Rütbe: Elite (Elit Üye)**
- Leader ve General arasında bir rütbe
- Terfi: Altın Blok + Terfi Ritüeli

**Rütbe Yetkileri (Fiziksel Etkileşimle):**
```yaml
Lider (Leader):
  - Tüm yetkiler (mevcut)
  - Klan dağıtma (mevcut)
  - Liderlik devretme (YENİ - fiziksel ritüel)
  - Klan ayarları değiştirme (YENİ - fiziksel)

General (Komutan):
  - Üye ekleme/çıkarma (mevcut)
  - Yapı inşa etme/yıkma (mevcut)
  - Klan savaşı başlatma (mevcut)
  - Klan bankası yönetimi (YENİ - fiziksel)
  - İttifak yönetimi (YENİ - fiziksel)

Elite (Elit Üye) - YENİ:
  - Yapı inşa etme (Member'dan farklı: daha fazla yapı)
  - Ritüel yapma (YENİ)
  - Klan bankasından para çekme (limitli) (YENİ)
  - Misyon başlatma (YENİ)

Member (Üye):
  - Temel klan özellikleri (mevcut)
  - Yapı kullanma (mevcut)
  - Klan bankasına para yatırma (YENİ)

Recruit (Acemi):
  - Sadece klan chat (mevcut)
  - Klan arazisinde koruma (mevcut)
  - 7 gün sonra otomatik Member'a terfi (YENİ)
```

**Liderlik Devretme Ritüeli (YENİ):**
```
Gereksinimler:
- Mevcut Lider + Yeni Lider (General olmalı)
- Her ikisi de kristale 3 blok yakın
- Her ikisinin elinde Nether Star olmalı
- Shift + Sağ Tık (birbirlerine)

Sonuç:
- Liderlik devredilir
- Eski lider General olur
- Yeni lider Leader olur
- Partikül efektleri
```

**Teknik Uygulama:**
```java
public class AdvancedRankSystem {
    public enum Permission {
        BUILD_STRUCTURE,
        DESTROY_STRUCTURE,
        ADD_MEMBER,
        REMOVE_MEMBER,
        START_WAR,
        MANAGE_BANK,
        WITHDRAW_BANK, // Limitli
        MANAGE_ALLIANCE,
        USE_RITUAL,
        START_MISSION,
        TRANSFER_LEADERSHIP
    }
    
    public boolean hasPermission(Clan clan, UUID playerId, Permission permission) {
        Clan.Rank rank = clan.getRank(playerId);
        Set<Permission> rankPermissions = getRankPermissions(rank);
        return rankPermissions.contains(permission);
    }
    
    private Set<Permission> getRankPermissions(Clan.Rank rank) {
        switch (rank) {
            case LEADER:
                return EnumSet.allOf(Permission.class); // Tüm yetkiler
            case GENERAL:
                return EnumSet.of(
                    Permission.BUILD_STRUCTURE,
                    Permission.DESTROY_STRUCTURE,
                    Permission.ADD_MEMBER,
                    Permission.REMOVE_MEMBER,
                    Permission.START_WAR,
                    Permission.MANAGE_BANK,
                    Permission.MANAGE_ALLIANCE
                );
            case ELITE:
                return EnumSet.of(
                    Permission.BUILD_STRUCTURE,
                    Permission.USE_RITUAL,
                    Permission.WITHDRAW_BANK, // Limitli
                    Permission.START_MISSION
                );
            case MEMBER:
                return EnumSet.of(
                    Permission.BUILD_STRUCTURE // Sadece yapı kullanma
                );
            case RECRUIT:
                return EnumSet.noneOf(Permission.class); // Hiçbir yetki
        }
    }
    
    /**
     * Liderlik devretme ritüeli
     */
    public boolean transferLeadership(Player currentLeader, Player newLeader, Location crystalLoc) {
        Clan clan = clanManager.getClanByPlayer(currentLeader.getUniqueId());
        if (clan == null) return false;
        
        // Mesafe kontrolü (3 blok)
        if (currentLeader.getLocation().distance(crystalLoc) > 3 ||
            newLeader.getLocation().distance(crystalLoc) > 3) {
            return false;
        }
        
        // Yeni lider General olmalı
        if (clan.getRank(newLeader.getUniqueId()) != Clan.Rank.GENERAL) {
            currentLeader.sendMessage("§cLiderlik devretmek için hedef General olmalı!");
            return false;
        }
        
        // Nether Star kontrolü
        if (!hasItemInHand(currentLeader, Material.NETHER_STAR) ||
            !hasItemInHand(newLeader, Material.NETHER_STAR)) {
            return false;
        }
        
        // Liderlik devret
        clan.setRank(currentLeader.getUniqueId(), Clan.Rank.GENERAL);
        clan.setRank(newLeader.getUniqueId(), Clan.Rank.LEADER);
        
        // Partikül efektleri
        crystalLoc.getWorld().spawnParticle(Particle.TOTEM, crystalLoc, 50, 1, 1, 1, 0.1);
        crystalLoc.getWorld().playSound(crystalLoc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        
        // Broadcast
        Bukkit.broadcastMessage("§6§l" + currentLeader.getName() + " liderliği " + 
            newLeader.getName() + " devretti!");
        
        return true;
    }
}
```

**Not:** Mevcut fiziksel terfi ritüeli korunur, sadece yetkiler detaylandırılır.

---

### 3. Klan Bankası Sistemi (Item-Based) ⭐ **YÜKSEK ÖNCELİK**

**NOT: Bu bölüm "Item-Based Ekonomi Sistemi" bölümünde detaylı açıklanmıştır.**
**Bakınız: [Item-Based Ekonomi Sistemi](#ekonomi)**

---

### 4. Görev Sistemi (Bireysel + Klan) ⭐ **YÜKSEK ÖNCELİK**

#### Öneri: İkili Görev Sistemi (Ana Mantık Korunuyor)

**Sistem İkiye Ayrılır:**

**A. Bireysel Görevler (Lonca Sistemi) - YENİ**
- Her oyuncu kendi görevlerini alır
- Klandan bağımsız çalışır
- Fiziksel: Görev Loncası (Lectern + Özel işaret)
- Günlük/Haftalık görevler

**B. Klan Görevleri (Mevcut Öneri)**
- Klan için ortak görevler
- Tüm üyeler katkıda bulunur
- Fiziksel: Klan Görev Tahtası (Lectern + Klan işareti)
- Lider veya General oluşturabilir

#### A. Bireysel Görev Sistemi (Lonca)

**Görev Türleri:**
1. **Güç Artırma**: "X güç kazan" (eşya, ustalık, vb.)
2. **Kaynak Toplama**: "X elmas topla", "X item topla" (Item-Based)
3. **Savaş Görevleri**: "X oyuncu öldür", "X hasar ver"
4. **Ritüel Görevleri**: "X ritüel yap", "X batarya kullan"
5. **Yapı Görevleri**: "X yapı inşa et", "X yapı seviyesi artır"

**Görev Seviyeleri:**
- **Günlük Görevler**: Her gün yenilenir, küçük ödüller
- **Haftalık Görevler**: Haftalık yenilenir, büyük ödüller
- **Özel Görevler**: Belirli koşullarda açılır

**Fiziksel Etkileşim:**
```
Görev Loncası Oluşturma:
1. Lectern yerleştir
2. Üzerine Item Frame koy
3. Item Frame'e Name Tag koy (isim: "GOREV_LONCASI")
4. Shift + Sağ Tık (Lectern'e)
5. Görev loncası aktif!

Görev Alma:
1. Lectern'e yaklaş
2. Sağ Tık (kitabı al)
3. Görev listesi açılır
4. Görev seç ve al
```

**Teknik Uygulama:**
```java
public class IndividualQuestSystem {
    public class IndividualQuest {
        private UUID questId;
        private UUID playerId;
        private QuestType type;
        private QuestDifficulty difficulty; // EASY, MEDIUM, HARD, EXPERT
        private String description;
        private int target;
        private int progress;
        private Map<String, Object> rewards;
        private long expiryTime;
        private boolean isDaily; // Günlük mü haftalık mı?
    }
    
    /**
     * Görev loncası oluştur (fiziksel)
     */
    public boolean createQuestGuild(Player player, Location lecternLoc) {
        Block block = lecternLoc.getBlock();
        if (block.getType() != Material.LECTERN) {
            return false;
        }
        
        // Name Tag kontrolü (Item Frame'de)
        boolean hasQuestGuildTag = false;
        for (Entity entity : lecternLoc.getWorld().getNearbyEntities(lecternLoc, 2, 2, 2)) {
            if (entity instanceof ItemFrame) {
                ItemFrame frame = (ItemFrame) entity;
                ItemStack item = frame.getItem();
                if (item != null && item.getType() == Material.NAME_TAG) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null && meta.getDisplayName().contains("GOREV_LONCASI")) {
                        hasQuestGuildTag = true;
                        break;
                    }
                }
            }
        }
        
        if (!hasQuestGuildTag) {
            player.sendMessage("§cGörev loncası için Name Tag gerekli!");
            return false;
        }
        
        // Metadata ekle
        block.setMetadata("QuestGuild", new FixedMetadataValue(plugin, true));
        
        player.sendMessage("§aGörev loncası oluşturuldu!");
        return true;
    }
    
    /**
     * Günlük görevler oluştur
     */
    @ScheduledTask(period = 864000L) // Her gün
    public void generateDailyQuests() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            List<IndividualQuest> dailyQuests = generateQuestsForPlayer(player, true);
            playerQuests.put(player.getUniqueId(), dailyQuests);
            
            player.sendMessage("§aYeni günlük görevleriniz hazır! Görev loncasına gidin.");
        }
    }
    
    /**
     * Haftalık görevler oluştur
     */
    @ScheduledTask(period = 6048000L) // Her hafta
    public void generateWeeklyQuests() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            List<IndividualQuest> weeklyQuests = generateQuestsForPlayer(player, false);
            List<IndividualQuest> existing = playerQuests.getOrDefault(
                player.getUniqueId(), new ArrayList<>());
            existing.addAll(weeklyQuests);
            playerQuests.put(player.getUniqueId(), existing);
            
            player.sendMessage("§6Yeni haftalık görevleriniz hazır! Görev loncasına gidin.");
        }
    }
    
    /**
     * Görev ilerlemesi
     */
    public void updateQuestProgress(Player player, QuestType type, int amount) {
        List<IndividualQuest> quests = playerQuests.get(player.getUniqueId());
        if (quests == null) return;
        
        for (IndividualQuest quest : quests) {
            if (quest.getType() == type && quest.getProgress() < quest.getTarget()) {
                quest.setProgress(quest.getProgress() + amount);
                
                // Görev tamamlandı mı?
                if (quest.getProgress() >= quest.getTarget()) {
                    completeQuest(player, quest);
                }
            }
        }
    }
    
    /**
     * Görev tamamlama ödülleri
     */
    private void completeQuest(Player player, IndividualQuest quest) {
        // Item ödülleri (Item-Based)
        List<ItemStack> rewardItems = (List<ItemStack>) quest.getRewards().get("items");
        for (ItemStack reward : rewardItems) {
            HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(reward);
            if (!overflow.isEmpty()) {
                // Envanter dolu, özel sandığına aktar
                playerVaultSystem.depositToVault(player.getUniqueId(), reward);
            }
        }
        
        // XP ödülü
        int xpReward = (Integer) quest.getRewards().get("xp");
        player.giveExp(xpReward);
        
        // Güç bonusu (geçici)
        double powerBonus = (Double) quest.getRewards().getOrDefault("power", 0.0);
        if (powerBonus > 0) {
            buffManager.applyQuestPowerBonus(player, powerBonus, 3600000L); // 1 saat
        }
        
        player.sendMessage("§a§lGörev tamamlandı!");
        player.sendMessage("§eÖdül: " + rewardItems.size() + " item, §e" + xpReward + " XP");
        player.sendTitle("§a§lGÖREV TAMAMLANDI", quest.getDescription(), 10, 70, 20);
        
        // Görev listesinden çıkar
        playerQuests.get(player.getUniqueId()).remove(quest);
    }
}
```

#### B. Klan Görevleri (Geliştirilmiş)

**Görev Türleri:**
1. **Kaynak Toplama**: "Klan bankasına X item yatır" (Item-Based)
2. **Yapı İnşası**: "X yapı inşa et"
3. **Ritüel Yapma**: "X ritüel yap"
4. **Savaş Görevleri**: "Klan savaşında yer al", "X savaş kazan"

**Görev Sistemi:**
- Lider veya General görev oluşturabilir
- Fiziksel: Klan Görev Tahtası (Lectern + Klan işareti)
- Üyeler görevi alır ve tamamlar
- Ödüller: Itemler (Item-Based), XP, Klan gücü artışı, Klan bonusları

**Teknik Uygulama:**
```java
public class ClanQuestSystem {
    public class ClanQuest {
        private UUID questId;
        private UUID creatorId; // Lider veya General
        private QuestType type;
        private String description;
        private int target;
        private int progress;
        private Map<UUID, Integer> memberProgress; // Üye -> İlerleme
        private Map<String, Object> rewards;
        private long expiryTime;
    }
    
    /**
     * Görev oluştur (fiziksel: Lectern + Kitap)
     */
    public boolean createQuest(Player creator, Location lecternLoc, QuestType type, int target) {
        Clan clan = clanManager.getClanByPlayer(creator.getUniqueId());
        if (clan == null) return false;
        
        // Yetki kontrolü
        Clan.Rank rank = clan.getRank(creator.getUniqueId());
        if (rank != Clan.Rank.LEADER && rank != Clan.Rank.GENERAL) {
            creator.sendMessage("§cSadece Lider veya General görev oluşturabilir!");
            return false;
        }
        
        // Lectern kontrolü
        Block block = lecternLoc.getBlock();
        if (block.getType() != Material.LECTERN) {
            return false;
        }
        
        Lectern lectern = (Lectern) block.getState();
        
        // Kitap oluştur
        ItemStack book = createQuestBook(type, target);
        lectern.getInventory().setItem(0, book);
        lectern.update();
        
        // Görev kaydet
        ClanQuest quest = new ClanQuest();
        quest.setQuestId(UUID.randomUUID());
        quest.setCreatorId(creator.getUniqueId());
        quest.setType(type);
        quest.setTarget(target);
        quest.setExpiryTime(System.currentTimeMillis() + 604800000L); // 7 gün
        
        clanQuests.put(clan.getId(), quest);
        
        creator.sendMessage("§aKlan görevi oluşturuldu!");
        return true;
    }
    
    /**
     * Görev ilerlemesi
     */
    public void updateQuestProgress(Clan clan, UUID memberId, QuestType type, int amount) {
        ClanQuest quest = clanQuests.get(clan.getId());
        if (quest == null || quest.getType() != type) return;
        
        int currentProgress = quest.getMemberProgress().getOrDefault(memberId, 0);
        quest.getMemberProgress().put(memberId, currentProgress + amount);
        
        // Toplam ilerleme
        int totalProgress = quest.getMemberProgress().values().stream()
            .mapToInt(Integer::intValue).sum();
        quest.setProgress(totalProgress);
        
        // Görev tamamlandı mı?
        if (quest.getProgress() >= quest.getTarget()) {
            completeClanQuest(clan, quest);
        }
    }
    
    /**
     * Görev tamamlama ödülleri
     */
    private void completeClanQuest(Clan clan, ClanQuest quest) {
        // Tüm üyelere ödül
        for (UUID memberId : clan.getMembers().keySet()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null) {
                // Item ödülleri (Item-Based)
                List<ItemStack> rewardItems = (List<ItemStack>) quest.getRewards().get("items");
                for (ItemStack reward : rewardItems) {
                    HashMap<Integer, ItemStack> overflow = member.getInventory().addItem(reward);
                    if (!overflow.isEmpty()) {
                        // Envanter dolu, özel sandığına aktar
                        playerVaultSystem.depositToVault(member.getUniqueId(), reward);
                    }
                }
                
                // XP ödülü
                int xpReward = (Integer) quest.getRewards().get("xp");
                member.giveExp(xpReward);
                
                member.sendMessage("§a§lKlan görevi tamamlandı!");
                member.sendMessage("§eÖdül: " + rewardItems.size() + " item, " + xpReward + " XP");
            }
        }
        
        // Klan gücü artışı
        double powerBonus = (Double) quest.getRewards().getOrDefault("power", 0.0);
        // PowerSystem'e bildir
        
        clanQuests.remove(clan.getId());
    }
}
```

**Fiziksel Etkileşim:**
- Görev oluşturma: Lectern'e kitap koy + Shift + Sağ Tık
- Görev alma: Lectern'den kitabı al
- Görev ilerlemesi: Otomatik takip (kaynak yatırma, yapı inşa, vb.)

---

### 5. Klan İstatistikleri ve Raporlama ⭐ **ORTA ÖNCELİK**

#### Öneri: Detaylı İstatistikler (Ana Mantık Korunuyor)

**İstatistik Türleri:**
1. **Üye İstatistikleri**: Aktiflik, katkı, güç
2. **Savaş İstatistikleri**: Kazanılan/kaybedilen savaşlar
3. **Ekonomi İstatistikleri**: Banka durumu, gelir/gider
4. **Yapı İstatistikleri**: Yapı sayısı, seviyeleri

**Fiziksel Görüntüleme:**
- İstatistik Tahtası: Item Frame + Harita
- Shift + Sağ Tık ile görüntüle
- GUI açılır (27 slot)

**Teknik Uygulama:**
```java
public class ClanStatisticsSystem {
    public class ClanStats {
        private int totalWars;
        private int wonWars;
        private int lostWars;
        private double totalBankDeposits;
        private double totalBankWithdrawals;
        private int structuresBuilt;
        private Map<UUID, Long> memberActivity; // Üye -> Son aktiflik
        private Map<UUID, Double> memberContributions; // Üye -> Katkı
    }
    
    /**
     * İstatistik tahtası oluştur (Item Frame + Harita)
     */
    public void createStatsBoard(Player player, Location frameLoc) {
        Block block = frameLoc.getBlock();
        if (block.getType() != Material.ITEM_FRAME) {
            return;
        }
        
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) return;
        
        // Harita oluştur (GUI için)
        ItemStack map = createStatsMap(clan);
        
        ItemFrame frame = (ItemFrame) block.getState();
        frame.setItem(map);
        frame.update();
        
        player.sendMessage("§aİstatistik tahtası oluşturuldu!");
    }
    
    /**
     * İstatistik görüntüle (Shift + Sağ Tık)
     */
    public void showClanStats(Player player) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) return;
        
        ClanStats stats = getClanStats(clan);
        
        // GUI aç
        Inventory gui = Bukkit.createInventory(null, 27, 
            Component.text("§6§lKlan İstatistikleri"));
        
        // Savaş istatistikleri
        ItemStack warItem = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta warMeta = warItem.getItemMeta();
        warMeta.setDisplayName("§cSavaş İstatistikleri");
        List<String> warLore = new ArrayList<>();
        warLore.add("§7Toplam Savaş: §e" + stats.getTotalWars());
        warLore.add("§7Kazanılan: §a" + stats.getWonWars());
        warLore.add("§7Kaybedilen: §c" + stats.getLostWars());
        warMeta.setLore(warLore);
        warItem.setItemMeta(warMeta);
        gui.setItem(10, warItem);
        
        // Ekonomi istatistikleri (Item-Based)
        ItemStack economyItem = new ItemStack(Material.CHEST);
        ItemMeta economyMeta = economyItem.getItemMeta();
        economyMeta.setDisplayName("§6Banka İstatistikleri");
        List<String> economyLore = new ArrayList<>();
        economyLore.add("§7Banka Durumu: §e" + getBankItemCount(clan) + " item");
        economyLore.add("§7Toplam Yatırım: §a" + stats.getTotalBankDeposits() + " item");
        economyLore.add("§7Toplam Çekim: §c" + stats.getTotalBankWithdrawals() + " item");
        economyMeta.setLore(economyLore);
        economyItem.setItemMeta(economyMeta);
        gui.setItem(12, economyItem);
        
        // Üye istatistikleri
        ItemStack memberItem = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta memberMeta = memberItem.getItemMeta();
        memberMeta.setDisplayName("§bÜye İstatistikleri");
        List<String> memberLore = new ArrayList<>();
        memberLore.add("§7Toplam Üye: §e" + clan.getMembers().size());
        memberLore.add("§7Aktif Üye: §a" + getActiveMemberCount(clan));
        memberLore.add("§7En Aktif: §e" + getMostActiveMember(clan));
        memberMeta.setLore(memberLore);
        memberItem.setItemMeta(memberMeta);
        gui.setItem(14, memberItem);
        
        // Yapı istatistikleri
        ItemStack structureItem = new ItemStack(Material.BEACON);
        ItemMeta structureMeta = structureItem.getItemMeta();
        structureMeta.setDisplayName("§dYapı İstatistikleri");
        List<String> structureLore = new ArrayList<>();
        structureLore.add("§7Toplam Yapı: §e" + stats.getStructuresBuilt());
        structureLore.add("§7Klan Seviyesi: §6" + powerSystem.calculateClanLevel(clan));
        structureMeta.setLore(structureLore);
        structureItem.setItemMeta(structureMeta);
        gui.setItem(16, structureItem);
        
        player.openInventory(gui);
    }
}
```

---

### 6. Klan Savaşları Geliştirmeleri ⭐ **YÜKSEK ÖNCELİK**

#### Mevcut Durum
- ✅ 5 dakika warmup var
- ✅ Kuşatma sistemi çalışıyor
- ⚠️ Savaş türleri sınırlı

#### Öneri: Çeşitli Savaş Türleri (Ana Mantık Korunuyor)

**Mevcut Sistem:**
- 5 dakika warmup ✅ (korunuyor)
- Beacon + Obsidian + TNT ✅ (korunuyor)

**Yeni Savaş Türleri:**

**1. Hızlı Saldırı (Raid) - 30 Dakika**
- Warmup: 2 dakika (daha hızlı)
- Süre: 30 dakika
- Ödül: %30 ganimet (normal kuşatma %50)
- Fiziksel: Beacon + Obsidian + TNT + Clock (saat eklersen Raid olur)

**2. Açık Savaş (Open War) - Belirli Alan**
- Warmup: 5 dakika (normal)
- Süre: 1 saat
- Alan: Özel savaş bölgesi (her iki klanın dışında)
- Fiziksel: Beacon + Obsidian + TNT + Compass (pusula eklersen Open War)

**3. Klan Turnuvası - Çoklu Klan**
- 4 klan katılır
- Eleme usulü
- Kazanan: Büyük ödül
- Fiziksel: Özel turnuva tahtası

**Teknik Uygulama:**
```java
public class AdvancedWarSystem {
    public enum WarType {
        SIEGE,      // Normal kuşatma (mevcut)
        RAID,       // Hızlı saldırı (30 dk)
        OPEN_WAR,   // Açık savaş (1 saat)
        TOURNAMENT  // Turnuva
    }
    
    /**
     * Savaş türü belirleme (fiziksel: item ile)
     */
    public WarType determineWarType(Player player, Location beaconLoc) {
        Block beaconBlock = beaconLoc.getBlock();
        if (beaconBlock.getType() != Material.BEACON) {
            return WarType.SIEGE; // Varsayılan
        }
        
        // Yakındaki item frame'leri kontrol et
        for (Entity entity : beaconLoc.getWorld().getNearbyEntities(beaconLoc, 5, 5, 5)) {
            if (entity instanceof ItemFrame) {
                ItemFrame frame = (ItemFrame) entity;
                ItemStack item = frame.getItem();
                
                if (item.getType() == Material.CLOCK) {
                    return WarType.RAID; // Saat = Raid
                } else if (item.getType() == Material.COMPASS) {
                    return WarType.OPEN_WAR; // Pusula = Open War
                }
            }
        }
        
        return WarType.SIEGE; // Varsayılan
    }
    
    /**
     * Savaş başlat (tür bazlı)
     */
    public void startWar(Clan attacker, Clan defender, WarType type) {
        War war = new War(attacker, defender, type);
        
        // Warmup süresi (tür bazlı)
        long warmupTime = getWarmupTime(type);
        war.setWarmupTime(warmupTime);
        
        // Savaş süresi (tür bazlı)
        long warDuration = getWarDuration(type);
        war.setDuration(warDuration);
        
        // Ganimet yüzdesi (tür bazlı)
        double lootPercentage = getLootPercentage(type);
        war.setLootPercentage(lootPercentage);
        
        // Bildirim
        notifyClanMembers(attacker, "§cSavaş ilan edildi! " + 
            formatTime(warmupTime) + " sonra başlayacak.");
        notifyClanMembers(defender, "§cSize savaş ilan edildi! " + 
            formatTime(warmupTime) + " sonra başlayacak.");
        
        // Warmup task'ı başlat
        scheduleWarStart(war);
    }
    
    private long getWarmupTime(WarType type) {
        switch (type) {
            case RAID: return 120000L; // 2 dakika
            case SIEGE: return balanceConfig.getSiegeWarmupTime() * 1000L; // 5 dakika (config'den)
            case OPEN_WAR: return 300000L; // 5 dakika
            case TOURNAMENT: return 600000L; // 10 dakika
            default: return 300000L;
        }
    }
    
    private long getWarDuration(WarType type) {
        switch (type) {
            case RAID: return 1800000L; // 30 dakika
            case SIEGE: return Long.MAX_VALUE; // Sınırsız (kristal kırılana kadar)
            case OPEN_WAR: return 3600000L; // 1 saat
            case TOURNAMENT: return 7200000L; // 2 saat
            default: return Long.MAX_VALUE;
        }
    }
    
    private double getLootPercentage(WarType type) {
        switch (type) {
            case RAID: return 0.3; // %30
            case SIEGE: return balanceConfig.getSiegeLootPercentage(); // %50 (config'den)
            case OPEN_WAR: return 0.4; // %40
            case TOURNAMENT: return 0.6; // %60
            default: return 0.5;
        }
    }
}
```

**Not:** Mevcut 5 dakika warmup korunur, sadece Raid için 2 dakika seçeneği eklenir.

---

### 7. Klan Başkenti Sistemi ⭐ **ORTA ÖNCELİK**

#### Öneri: Başkent Seçimi (Ana Mantık Korunuyor)

**Özellikler:**
- Klan arazisinde bir nokta başkent olarak işaretlenir
- Fiziksel: Lider kristali başkent konumuna taşır (mevcut sistem)
- Başkent bonusları: Başkent yakınında %10 güç artışı
- Başkent koruması: Başkent yakınında daha güçlü savunma

**Teknik Uygulama:**
```java
public class ClanCapitalSystem {
    /**
     * Başkent belirle (kristal taşıma ile)
     */
    public void setCapital(Clan clan, Location location) {
        if (!clan.getTerritory().contains(location)) {
            return; // Başkent klan arazisi içinde olmalı
        }
        
        clan.setCapitalLocation(location);
        
        // Başkent bonusu uygula
        applyCapitalBonus(clan, location);
        
        // Broadcast
        Bukkit.broadcastMessage("§6§l" + clan.getName() + " klanı başkentini belirledi!");
    }
    
    /**
     * Başkent bonusu (yakındaki üyelere)
     */
    private void applyCapitalBonus(Clan clan, Location capital) {
        double bonusRadius = balanceConfig.getClanCapitalBonusRadius(); // 50 blok
        double powerBonus = balanceConfig.getClanCapitalPowerBonus(); // 0.10 (%10)
        
        for (UUID memberId : clan.getMembers().keySet()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                double distance = capital.distance(member.getLocation());
                if (distance <= bonusRadius) {
                    // Güç bonusu uygula (BuffManager ile)
                    buffManager.applyCapitalBonus(member, powerBonus);
                }
            }
        }
    }
}
```

**Fiziksel Etkileşim:**
- Mevcut kristal taşıma sistemi kullanılır
- Lider kristali taşıdığında o konum başkent olur
- Başkent partikül efekti: Sürekli Totem partikülleri

---

### 8. Üye Aktivite Takibi ⭐ **ORTA ÖNCELİK**

#### Öneri: Aktivite Sistemi (Ana Mantık Korunuyor)

**Özellikler:**
- Üyelerin son aktiflik zamanı takip edilir
- 30 gün offline üyeler otomatik Recruit'e düşer
- Aktif üyelere bonuslar verilir
- Fiziksel: Aktivite tahtası (Item Frame + Harita)

**Teknik Uygulama:**
```java
public class ClanActivitySystem {
    // Üye -> Son aktiflik zamanı
    private final Map<UUID, Long> lastActivityTime = new HashMap<>();
    
    /**
     * Aktivite güncelle (oyuncu online olduğunda)
     */
    public void updateActivity(UUID playerId) {
        lastActivityTime.put(playerId, System.currentTimeMillis());
    }
    
    /**
     * Uzun süre offline üyeleri Recruit'e düşür
     */
    @ScheduledTask(period = 864000L) // Her gün kontrol
    public void checkInactiveMembers() {
        long inactiveThreshold = 2592000000L; // 30 gün
        
        for (Clan clan : clanManager.getAllClans()) {
            for (UUID memberId : clan.getMembers().keySet()) {
                long lastActivity = lastActivityTime.getOrDefault(memberId, System.currentTimeMillis());
                long inactiveTime = System.currentTimeMillis() - lastActivity;
                
                if (inactiveTime > inactiveThreshold) {
                    Clan.Rank currentRank = clan.getRank(memberId);
                    if (currentRank != Clan.Rank.RECRUIT && currentRank != Clan.Rank.LEADER) {
                        // Recruit'e düşür
                        clan.setRank(memberId, Clan.Rank.RECRUIT);
                        
                        // Lider'e bildir
                        UUID leaderId = clan.getLeader();
                        Player leader = Bukkit.getPlayer(leaderId);
                        if (leader != null) {
                            Player inactivePlayer = Bukkit.getOfflinePlayer(memberId).getPlayer();
                            String playerName = inactivePlayer != null ? inactivePlayer.getName() : "Bilinmeyen";
                            leader.sendMessage("§c" + playerName + " 30 gün offline, Recruit'e düşürüldü!");
                        }
                    }
                }
            }
        }
    }
    
    /**
     * En aktif üyeleri göster
     */
    public List<UUID> getMostActiveMembers(Clan clan, int limit) {
        return clan.getMembers().keySet().stream()
            .sorted(Comparator.comparing(memberId -> 
                lastActivityTime.getOrDefault(memberId, 0L)).reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }
}
```

---

### 9. Klan İçi Kaynak Paylaşımı (Yetkiye Göre) ⭐ **ORTA ÖNCELİK**

#### Öneri: Gelişmiş Paylaşım Sistemi (Ana Mantık Korunuyor)

**Yeni Özellikler:**

**1. Ortak Sandık Sistemi (Rütbe Bazlı Erişim)**
- Klan arazisinde "Ortak Sandık" (Chest + Özel işaret)
- Rütbe bazlı erişim kontrolü
- Fiziksel: Chest'e Name Tag ile "KLAN_SANDIGI" yaz

**Rütbe Bazlı Erişim:**
```yaml
Leader (Lider):
  - Tüm sandıklara tam erişim
  - Sandık oluşturma/silme
  - Sandık yetkilerini ayarlama

General (Komutan):
  - Tüm sandıklara erişim
  - Sandık oluşturma (limitli: 3 sandık)
  - Sandık silme (sadece kendi oluşturduğu)

Elite (Elit Üye):
  - Belirli sandıklara erişim (General tarafından izin verilen)
  - Sandık oluşturma (limitli: 1 sandık)
  - Sandık silme (sadece kendi oluşturduğu)

Member (Üye):
  - Sadece "Public" sandıklara erişim
  - Sandık oluşturma YOK
  - Sandık silme YOK

Recruit (Acemi):
  - Sandık erişimi YOK
```

**2. Kaynak İstek Sistemi (Yetkiye Göre)**
- Üyeler kaynak isteyebilir
- Fiziksel: İstek tahtası (Lectern + Kitap)
- Rütbe bazlı istek limitleri
- Diğer üyeler bağış yapabilir (rütbe bazlı bağış limitleri)

**Rütbe Bazlı İstek/Bağış Limitleri (Sadece Spam Önleme İçin):**
```yaml
Leader (Lider):
  - İstek limiti: Sınırsız
  - Bağış limiti: Sınırsız

General (Komutan):
  - İstek limiti: Günlük 1,000,000 altın değerinde (spam önleme)
  - Bağış limiti: Günlük 2,000,000 altın değerinde (spam önleme)

Elite (Elit Üye):
  - İstek limiti: Günlük 500,000 altın değerinde (spam önleme)
  - Bağış limiti: Günlük 1,000,000 altın değerinde (spam önleme)

Member (Üye):
  - İstek limiti: Günlük 200,000 altın değerinde (spam önleme)
  - Bağış limiti: Günlük 500,000 altın değerinde (spam önleme)

Recruit (Acemi):
  - İstek limiti: Günlük 100,000 altın değerinde (spam önleme)
  - Bağış limiti: Günlük 200,000 altın değerinde (spam önleme)
```

**Not**: Bu limitler sadece spam/exploit önleme için. Normal kullanımda sorun çıkarmaz, çok yüksek değerler.

**3. Kaynak Transfer Sistemi (YENİ)**
- Rütbe bazlı kaynak transferi
- Elite ve üzeri üyeler kaynak transfer edebilir
- Fiziksel: Transfer tahtası (Lectern + Özel işaret)

**Teknik Uygulama:**
```java
public class AdvancedClanResourceSharing {
    /**
     * Ortak sandık kontrolü (rütbe bazlı)
     */
    public boolean canAccessSharedChest(Player player, Block chest) {
        if (!isSharedChest(chest)) {
            return false;
        }
        
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) return false;
        
        Clan.Rank rank = clan.getRank(player.getUniqueId());
        
        // Sandık metadata'sından erişim seviyesi al
        String accessLevel = getChestAccessLevel(chest); // "PUBLIC", "ELITE", "GENERAL", "LEADER"
        
        switch (accessLevel) {
            case "PUBLIC":
                return rank != Clan.Rank.RECRUIT; // Recruit hariç herkes
            case "ELITE":
                return rank == Clan.Rank.ELITE || rank == Clan.Rank.GENERAL || rank == Clan.Rank.LEADER;
            case "GENERAL":
                return rank == Clan.Rank.GENERAL || rank == Clan.Rank.LEADER;
            case "LEADER":
                return rank == Clan.Rank.LEADER;
            default:
                return false;
        }
    }
    
    /**
     * Kaynak isteği oluştur (rütbe bazlı limit)
     */
    public void createResourceRequest(Player player, Material material, int amount) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) return;
        
        Clan.Rank rank = clan.getRank(player.getUniqueId());
        
        // Günlük istek limiti kontrolü
        double itemValue = calculateItemValue(material, amount);
        double dailyLimit = getDailyRequestLimit(rank);
        double alreadyRequested = getDailyRequestedAmount(player.getUniqueId());
        
        if (alreadyRequested + itemValue > dailyLimit) {
            player.sendMessage("§cGünlük istek limitiniz: " + dailyLimit + " altın değerinde");
            player.sendMessage("§7Kalan limit: " + (dailyLimit - alreadyRequested) + " altın");
            return;
        }
        
        // İstek oluştur
        ResourceRequest request = new ResourceRequest();
        request.setRequesterId(player.getUniqueId());
        request.setMaterial(material);
        request.setAmount(amount);
        request.setCreatedTime(System.currentTimeMillis());
        
        clanResourceRequests.put(clan.getId(), request);
        updateDailyRequestedAmount(player.getUniqueId(), itemValue);
        
        // Tüm üyelere bildir
        for (UUID memberId : clan.getMembers().keySet()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && !member.equals(player)) {
                member.sendMessage("§e" + player.getName() + " " + amount + "x " + 
                    material.name() + " istiyor! (Değer: " + itemValue + " altın)");
            }
        }
    }
    
    /**
     * Kaynak bağışı (rütbe bazlı limit)
     */
    public void donateResource(Player donor, UUID requesterId, Material material, int amount) {
        Clan clan = clanManager.getClanByPlayer(donor.getUniqueId());
        if (clan == null) return;
        
        Clan.Rank rank = clan.getRank(donor.getUniqueId());
        
        // Günlük bağış limiti kontrolü
        double itemValue = calculateItemValue(material, amount);
        double dailyLimit = getDailyDonationLimit(rank);
        double alreadyDonated = getDailyDonatedAmount(donor.getUniqueId());
        
        if (alreadyDonated + itemValue > dailyLimit) {
            donor.sendMessage("§cGünlük bağış limitiniz: " + dailyLimit + " altın değerinde");
            return;
        }
        
        // Envanter kontrolü
        if (!donor.getInventory().contains(material, amount)) {
            donor.sendMessage("§cYeterli kaynağınız yok!");
            return;
        }
        
        // Kaynağı al
        donor.getInventory().removeItem(new ItemStack(material, amount));
        updateDailyDonatedAmount(donor.getUniqueId(), itemValue);
        
        // İsteyen oyuncuya ver
        Player requester = Bukkit.getPlayer(requesterId);
        if (requester != null && requester.isOnline()) {
            requester.getInventory().addItem(new ItemStack(material, amount));
            requester.sendMessage("§a" + donor.getName() + " size " + amount + "x " + 
                material.name() + " bağışladı!");
        }
        
        donor.sendMessage("§aKaynak bağışlandı! (Değer: " + itemValue + " altın)");
    }
    
    private double getDailyRequestLimit(Clan.Rank rank) {
        switch (rank) {
            case LEADER: return Double.MAX_VALUE;
            case GENERAL: return balanceConfig.getClanResourceGeneralRequestLimit(); // 50,000
            case ELITE: return balanceConfig.getClanResourceEliteRequestLimit(); // 20,000
            case MEMBER: return balanceConfig.getClanResourceMemberRequestLimit(); // 10,000
            case RECRUIT: return balanceConfig.getClanResourceRecruitRequestLimit(); // 5,000
            default: return 0.0;
        }
    }
    
    private double getDailyDonationLimit(Clan.Rank rank) {
        switch (rank) {
            case LEADER: return Double.MAX_VALUE;
            case GENERAL: return balanceConfig.getClanResourceGeneralDonationLimit(); // 100,000
            case ELITE: return balanceConfig.getClanResourceEliteDonationLimit(); // 50,000
            case MEMBER: return balanceConfig.getClanResourceMemberDonationLimit(); // 20,000
            case RECRUIT: return balanceConfig.getClanResourceRecruitDonationLimit(); // 10,000
            default: return 0.0;
        }
    }
}
```

**Teknik Uygulama:**
```java
public class ClanResourceSharing {
    /**
     * Ortak sandık kontrolü
     */
    public boolean isSharedChest(Block chest) {
        if (chest.getType() != Material.CHEST && 
            chest.getType() != Material.TRAPPED_CHEST) {
            return false;
        }
        
        // Metadata kontrolü
        if (chest.hasMetadata("ClanSharedChest")) {
            return true;
        }
        
        // Name Tag kontrolü (üzerindeki item frame)
        for (Entity entity : chest.getWorld().getNearbyEntities(
                chest.getLocation(), 2, 2, 2)) {
            if (entity instanceof ItemFrame) {
                ItemFrame frame = (ItemFrame) entity;
                ItemStack item = frame.getItem();
                if (item != null && item.getType() == Material.NAME_TAG) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null && meta.getDisplayName().contains("KLAN_SANDIGI")) {
                        chest.setMetadata("ClanSharedChest", 
                            new FixedMetadataValue(plugin, true));
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Kaynak isteği oluştur
     */
    public void createResourceRequest(Player player, Material material, int amount) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) return;
        
        ResourceRequest request = new ResourceRequest();
        request.setRequesterId(player.getUniqueId());
        request.setMaterial(material);
        request.setAmount(amount);
        request.setCreatedTime(System.currentTimeMillis());
        
        clanResourceRequests.put(clan.getId(), request);
        
        // Tüm üyelere bildir
        for (UUID memberId : clan.getMembers().keySet()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && !member.equals(player)) {
                member.sendMessage("§e" + player.getName() + " " + amount + "x " + 
                    material.name() + " istiyor!");
            }
        }
    }
    
    /**
     * Kaynak bağışı
     */
    public void donateResource(Player donor, UUID requesterId, Material material, int amount) {
        Clan clan = clanManager.getClanByPlayer(donor.getUniqueId());
        if (clan == null) return;
        
        ResourceRequest request = clanResourceRequests.get(clan.getId());
        if (request == null || !request.getRequesterId().equals(requesterId)) {
            return;
        }
        
        // Envanter kontrolü
        if (!donor.getInventory().contains(material, amount)) {
            donor.sendMessage("§cYeterli kaynağınız yok!");
            return;
        }
        
        // Kaynağı al
        donor.getInventory().removeItem(new ItemStack(material, amount));
        
        // İsteyen oyuncuya ver
        Player requester = Bukkit.getPlayer(requesterId);
        if (requester != null && requester.isOnline()) {
            requester.getInventory().addItem(new ItemStack(material, amount));
            requester.sendMessage("§a" + donor.getName() + " size " + amount + "x " + 
                material.name() + " bağışladı!");
        }
        
        donor.sendMessage("§aKaynak bağışlandı!");
    }
}
```

---

### 10. Klan İttifak Geliştirmeleri ⭐ **ORTA ÖNCELİK**

#### Mevcut Durum
- ✅ Temel ittifak sistemi var
- ✅ Fiziksel ritüel ile kurulur
- ⚠️ Gelişmiş özellikler eksik

#### Öneri: İttifak Özellikleri (Ana Mantık Korunuyor)

**Yeni Özellikler:**

**1. İttifak Chat**
- İttifaklı klanlar arası özel chat
- Fiziksel: Chat'e "@alliance" yazarsan ittifak chat'e gider

**2. Ortak Savunma**
- İttifaklı klana saldırılırsa otomatik yardım
- Fiziksel: Savaş başladığında ittifaklı klan üyelerine bildirim

**3. Kaynak Paylaşımı**
- İttifaklı klanlar kaynak paylaşabilir
- Fiziksel: İttifak tahtası (Lectern)

**Teknik Uygulama:**
```java
public class AdvancedAllianceSystem {
    /**
     * İttifak chat
     */
    public void sendAllianceMessage(Player sender, String message) {
        Clan senderClan = clanManager.getClanByPlayer(sender.getUniqueId());
        if (senderClan == null) return;
        
        List<Clan> alliedClans = getAlliedClans(senderClan);
        
        for (Clan alliedClan : alliedClans) {
            for (UUID memberId : alliedClan.getMembers().keySet()) {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null) {
                    member.sendMessage("§d[İttifak] §7" + sender.getName() + ": §f" + message);
                }
            }
        }
    }
    
    /**
     * Otomatik savunma
     */
    public void onAlliedClanAttacked(Clan defender, Clan attacker) {
        List<Clan> alliedClans = getAlliedClans(defender);
        
        for (Clan alliedClan : alliedClans) {
            // İttifaklı klan üyelerine bildir
            for (UUID memberId : alliedClan.getMembers().keySet()) {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null) {
                    member.sendMessage("§c§lİTTİFAKLI KLANINIZA SALDIRIYOR!");
                    member.sendMessage("§7" + defender.getName() + " klanına yardım edin!");
                    member.sendTitle("§c§lİTTİFAK YARDIMI", 
                        defender.getName() + " saldırı altında!", 10, 70, 20);
                }
            }
        }
    }
}
```

---

## 💰 ITEM-BASED EKONOMİ SİSTEMİ {#ekonomi}

### 🎯 EKONOMİ FELSEFESİ

**Temel Prensipler:**
- ❌ **Para Yok**: Oyun içinde para sistemi yok
- ✅ **Item-Based**: Tüm işlemler itemlerle yapılır
- ✅ **Özgürlük**: Oyuncular istediği sistemi kullanır veya hiç kullanmaz
- ✅ **Otomatik Sistemler**: Elle uğraşmak istemeyenler için otomatik çözümler
- ✅ **Küçük Klanlar**: Basit sistemler, büyük klanlar için gelişmiş sistemler

**Ekonomi Türleri:**
1. **Bireysel Ekonomi**: Oyuncular arası ticaret
2. **Klan İçi Ekonomi**: Klan üyeleri arası kaynak paylaşımı
3. **Klanlar Arası Ekonomi**: Klanlar arası ticaret ve anlaşmalar

---

### 1. Klan Bankası Sistemi (Item-Based) ⭐ **YÜKSEK ÖNCELİK**

#### Mevcut Durum
- ⚠️ Para sistemi var (kaldırılacak)
- ⚠️ Faiz sistemi var (kaldırılacak)
- ✅ Temel banka yapısı var

#### Öneri: Item-Based Banka Sistemi

**Fiziksel Yapı:**
```
Klan Bankası İnşası:
1. Klan arazisinde özel yapı:
   - 3x3 Obsidian platform
   - Ortada Ender Chest
   - 4 köşede Kızıltaş Blok
   - Üzerine Item Frame + Name Tag ("KLAN_BANKASI")

2. Ender Chest'e Shift + Sağ Tık:
   - GUI açılır
   - Banka yönetimi
```

**Banka Özellikleri:**

**1. Ortak Sandık Sistemi**
- Klan bankası = Ender Chest (özel işaretli)
- Tüm üyeler erişebilir (rütbe bazlı)
- Itemler fiziksel olarak sandıkta durur
- Sandık doluysa uyarı verilir

**2. Otomatik Maaş Sistemi (Item-Based)**
- Maaşlar itemlerle ödenir (Elmas, Altın, Demir, vb.)
- Her rütbe için farklı maaş (item türü ve miktarı)
- Otomatik dağıtım: Belirlenen günde otomatik üyenin sandığına aktarılır
- Sandık doluysa: Bekleme listesine eklenir, sandık boşalınca aktarılır

**3. Otomatik Transfer Kontratları (YENİ)**
- Oyuncular kontrat oluşturur: "X kişiye Y günde bir Z materyal otomatik yatır"
- Kontrat kağıdı ile oluşturulur (GUI ile)
- Otomatik sistem kontratları takip eder
- Sandık durumları kontrol edilir (dolu, boş, yeterli item var mı)

**Teknik Uygulama:**
```java
public class ItemBasedClanBank {
    /**
     * Klan bankası kontrolü
     */
    public boolean isClanBank(Block block) {
        if (block.getType() != Material.ENDER_CHEST) {
            return false;
        }
        
        // Name Tag kontrolü
        for (Entity entity : block.getWorld().getNearbyEntities(
                block.getLocation(), 2, 2, 2)) {
            if (entity instanceof ItemFrame) {
                ItemFrame frame = (ItemFrame) entity;
                ItemStack item = frame.getItem();
                if (item != null && item.getType() == Material.NAME_TAG) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null && meta.getDisplayName().contains("KLAN_BANKASI")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Banka GUI aç
     */
    public void openBankGUI(Player player) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) return;
        
        Inventory gui = Bukkit.createInventory(null, 54, 
            Component.text("§6§lKlan Bankası"));
        
        // Banka içeriği (Ender Chest'ten)
        Inventory bankChest = getBankChest(clan);
        if (bankChest != null) {
            ItemStack[] contents = bankChest.getContents();
            for (int i = 0; i < Math.min(contents.length, 27); i++) {
                gui.setItem(i, contents[i]);
            }
        }
        
        // İşlem butonları (Alt kısım)
        // Item yatır
        ItemStack deposit = new ItemStack(Material.EMERALD);
        ItemMeta depMeta = deposit.getItemMeta();
        depMeta.setDisplayName("§aItem Yatır");
        depMeta.setLore(Arrays.asList("§7Envanterinden item seç"));
        deposit.setItemMeta(depMeta);
        gui.setItem(45, deposit);
        
        // Item çek
        ItemStack withdraw = new ItemStack(Material.DIAMOND);
        ItemMeta withMeta = withdraw.getItemMeta();
        withMeta.setDisplayName("§bItem Çek");
        withMeta.setLore(Arrays.asList("§7Banka'dan item seç"));
        withdraw.setItemMeta(withMeta);
        gui.setItem(47, withdraw);
        
        // Maaş ayarları
        ItemStack salary = new ItemStack(Material.GOLD_INGOT);
        ItemMeta salMeta = salary.getItemMeta();
        salMeta.setDisplayName("§eMaaş Ayarları");
        salMeta.setLore(Arrays.asList("§7Rütbe bazlı maaş ayarları"));
        salary.setItemMeta(salMeta);
        gui.setItem(49, salary);
        
        // Otomatik transfer kontratları
        ItemStack contracts = new ItemStack(Material.PAPER);
        ItemMeta contMeta = contracts.getItemMeta();
        contMeta.setDisplayName("§dOtomatik Transfer Kontratları");
        contMeta.setLore(Arrays.asList("§7Otomatik item transfer kontratları"));
        contracts.setItemMeta(contMeta);
        gui.setItem(51, contracts);
        
        player.openInventory(gui);
    }
    
    /**
     * Item yatırma
     */
    public boolean depositItem(Player player, ItemStack item, int amount) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) return false;
        
        // Envanter kontrolü
        if (!player.getInventory().containsAtLeast(item, amount)) {
            player.sendMessage("§cYeterli item yok!");
            return false;
        }
        
        // Banka sandığı kontrolü
        Inventory bankChest = getBankChest(clan);
        if (bankChest == null) {
            player.sendMessage("§cKlan bankası bulunamadı!");
            return false;
        }
        
        // Sandık dolu mu kontrol et
        HashMap<Integer, ItemStack> overflow = bankChest.addItem(
            new ItemStack(item.getType(), amount));
        
        if (!overflow.isEmpty()) {
            // Sandık dolu, kalan itemleri geri ver
            player.sendMessage("§cBanka sandığı dolu! Kalan itemler envanterine eklendi.");
            for (ItemStack remaining : overflow.values()) {
                player.getInventory().addItem(remaining);
            }
            return false;
        }
        
        // Itemları envanterden al
        player.getInventory().removeItem(new ItemStack(item.getType(), amount));
        player.sendMessage("§a" + amount + "x " + item.getType().name() + " bankaya yatırıldı!");
        
        return true;
    }
    
    /**
     * Item çekme
     */
    public boolean withdrawItem(Player player, Material material, int amount) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) return false;
        
        // Yetki kontrolü
        Clan.Rank rank = clan.getRank(player.getUniqueId());
        if (!hasWithdrawPermission(rank)) {
            player.sendMessage("§cBu işlem için yetkiniz yok!");
            return false;
        }
        
        // Banka sandığı kontrolü
        Inventory bankChest = getBankChest(clan);
        if (bankChest == null) {
            player.sendMessage("§cKlan bankası bulunamadı!");
            return false;
        }
        
        // Sandıkta yeterli item var mı?
        if (!bankChest.containsAtLeast(new ItemStack(material), amount)) {
            player.sendMessage("§cBanka'da yeterli " + material.name() + " yok!");
            return false;
        }
        
        // Envanter dolu mu kontrol et
        HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(
            new ItemStack(material, amount));
        
        if (!overflow.isEmpty()) {
            // Envanter dolu, itemleri geri bankaya koy
            player.sendMessage("§cEnvanterin dolu! Itemler bankaya geri konuldu.");
            for (ItemStack remaining : overflow.values()) {
                bankChest.addItem(remaining);
            }
            return false;
        }
        
        // Itemları bankadan al
        bankChest.removeItem(new ItemStack(material, amount));
        player.sendMessage("§a" + amount + "x " + material.name() + " bankadan çekildi!");
        
        return true;
    }
}
```

---

### 2. Otomatik Maaş Sistemi (Item-Based) ⭐ **YÜKSEK ÖNCELİK**

#### Öneri: Item-Based Otomatik Maaş

**Maaş Türleri:**
- **Sabit Maaş**: Her rütbe için belirli item ve miktar
- **Değişken Maaş**: Klan seviyesine göre artar
- **Özel Maaş**: Lider/General özel maaş belirleyebilir

**Maaş Ayarlama (GUI ile):**
```
1. Klan Yönetim Merkezi'ne gir
2. "Maaş Ayarları" butonuna tıkla
3. Rütbe seç (Leader, General, Elite, Member)
4. Item türü seç (Elmas, Altın, Demir, vb.)
5. Miktar belirle
6. Dağıtım sıklığı seç (Günlük, Haftalık, Aylık)
7. Kaydet
```

**Otomatik Dağıtım Sistemi:**
```java
public class ItemBasedSalarySystem {
    public class SalaryContract {
        private UUID clanId;
        private Clan.Rank rank;
        private Material salaryItem;
        private int salaryAmount;
        private long distributionInterval; // Günlük, haftalık, aylık
        private long lastDistribution;
        private UUID targetPlayerId; // Özel maaş için (opsiyonel)
    }
    
    /**
     * Otomatik maaş dağıtımı
     */
    @ScheduledTask(period = 3600L) // Her saat kontrol
    public void distributeSalaries() {
        for (Clan clan : clanManager.getAllClans()) {
            List<SalaryContract> contracts = getSalaryContracts(clan);
            
            for (SalaryContract contract : contracts) {
                // Dağıtım zamanı geldi mi?
                if (System.currentTimeMillis() - contract.getLastDistribution() 
                    < contract.getDistributionInterval()) {
                    continue; // Henüz zamanı gelmedi
                }
                
                // Özel maaş mı?
                if (contract.getTargetPlayerId() != null) {
                    distributeToPlayer(clan, contract);
                } else {
                    // Rütbe bazlı maaş
                    distributeToRank(clan, contract);
                }
                
                // Son dağıtım zamanını güncelle
                contract.setLastDistribution(System.currentTimeMillis());
            }
        }
    }
    
    /**
     * Rütbe bazlı maaş dağıt
     */
    private void distributeToRank(Clan clan, SalaryContract contract) {
        Inventory bankChest = getBankChest(clan);
        if (bankChest == null) return;
        
        // Bankada yeterli item var mı?
        int totalNeeded = 0;
        List<UUID> eligibleMembers = new ArrayList<>();
        
        for (UUID memberId : clan.getMembers().keySet()) {
            Clan.Rank memberRank = clan.getRank(memberId);
            if (memberRank == contract.getRank()) {
                eligibleMembers.add(memberId);
                totalNeeded += contract.getSalaryAmount();
            }
        }
        
        if (!bankChest.containsAtLeast(
            new ItemStack(contract.getSalaryItem()), totalNeeded)) {
            // Yeterli item yok, lider'e bildir
            notifyLeaderInsufficientFunds(clan, contract, totalNeeded);
            return;
        }
        
        // Her üyeye maaş dağıt
        for (UUID memberId : eligibleMembers) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                distributeToPlayer(clan, contract, memberId);
            } else {
                // Offline üye, özel sandığına aktar (varsa)
                distributeToOfflinePlayer(clan, contract, memberId);
            }
        }
    }
    
    /**
     * Oyuncuya maaş dağıt
     */
    private void distributeToPlayer(Clan clan, SalaryContract contract, UUID playerId) {
        Inventory bankChest = getBankChest(clan);
        Player player = Bukkit.getPlayer(playerId);
        
        if (player == null || !player.isOnline()) {
            // Offline, özel sandığına aktar
            distributeToOfflinePlayer(clan, contract, playerId);
            return;
        }
        
        // Envanter kontrolü
        HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(
            new ItemStack(contract.getSalaryItem(), contract.getSalaryAmount()));
        
        if (!overflow.isEmpty()) {
            // Envanter dolu, özel sandığına aktar
            player.sendMessage("§cEnvanterin dolu! Maaşın özel sandığına aktarıldı.");
            distributeToOfflinePlayer(clan, contract, playerId);
            return;
        }
        
        // Bankadan item al
        bankChest.removeItem(new ItemStack(
            contract.getSalaryItem(), contract.getSalaryAmount()));
        
        player.sendMessage("§aMaaşınızı aldınız: " + contract.getSalaryAmount() + 
            "x " + contract.getSalaryItem().name());
    }
    
    /**
     * Offline oyuncuya maaş dağıt (özel sandık)
     */
    private void distributeToOfflinePlayer(Clan clan, SalaryContract contract, UUID playerId) {
        // Özel sandık sistemi (Player Vault benzeri)
        Location playerVault = getPlayerVaultLocation(playerId);
        if (playerVault == null) {
            // Özel sandık yok, bekleme listesine ekle
            addToPendingSalaries(clan, contract, playerId);
            return;
        }
        
        Block vaultBlock = playerVault.getBlock();
        if (vaultBlock.getType() != Material.CHEST) {
            addToPendingSalaries(clan, contract, playerId);
            return;
        }
        
        Chest vaultChest = (Chest) vaultBlock.getState();
        Inventory vaultInv = vaultChest.getInventory();
        
        // Sandık dolu mu?
        HashMap<Integer, ItemStack> overflow = vaultInv.addItem(
            new ItemStack(contract.getSalaryItem(), contract.getSalaryAmount()));
        
        if (!overflow.isEmpty()) {
            // Sandık dolu, bekleme listesine ekle
            addToPendingSalaries(clan, contract, playerId);
            return;
        }
        
        // Bankadan item al
        Inventory bankChest = getBankChest(clan);
        bankChest.removeItem(new ItemStack(
            contract.getSalaryItem(), contract.getSalaryAmount()));
    }
    
    /**
     * Bekleme listesindeki maaşları dağıt (oyuncu online olduğunda veya sandık boşaldığında)
     */
    public void processPendingSalaries(UUID playerId) {
        List<PendingSalary> pending = getPendingSalaries(playerId);
        
        for (PendingSalary salary : pending) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                // Tekrar dene
                distributeToPlayer(salary.getClan(), salary.getContract(), playerId);
                removePendingSalary(salary);
            }
        }
    }
}
```

**Edge Case'ler:**
- ✅ Sandık dolu → Bekleme listesine ekle, sandık boşalınca aktar
- ✅ Banka boş → Lider'e bildir, maaş dağıtılmaz
- ✅ Oyuncu offline → Özel sandığına aktar (varsa), yoksa bekleme listesi
- ✅ Envanter dolu → Özel sandığına aktar, o da doluysa bekleme listesi
- ✅ Yeterli item yok → Lider'e bildir, kısmi dağıtım yapılabilir

---

### 3. Otomatik Transfer Kontratları ⭐ **YÜKSEK ÖNCELİK**

#### Öneri: Item-Based Otomatik Transfer

**Kontrat Oluşturma:**
```
1. Kontrat Kağıdı Craft Et:
   - Kağıt + Demir + Mürekkep = Transfer Kontratı

2. Kağıda Tıkla (Sağ Tık):
   - GUI açılır
   - Hedef oyuncu seç
   - Item türü seç
   - Miktar belirle
   - Sıklık seç (Günlük, Haftalık, vb.)
   - Kaynak: Klan Bankası mı, Kişisel Sandık mı?

3. Kağıdı Mühürle (Shift + Sağ Tık):
   - Kontrat aktif olur
   - Otomatik sistem takip eder
```

**Kontrat Türleri:**
1. **Klan Bankası → Oyuncu**: Klan bankasından oyuncuya otomatik transfer
2. **Oyuncu → Klan Bankası**: Oyuncudan klan bankasına otomatik transfer
3. **Oyuncu → Oyuncu**: İki oyuncu arası otomatik transfer

**Teknik Uygulama:**
```java
public class AutomaticTransferContractSystem {
    public class TransferContract {
        private UUID contractId;
        private UUID creatorId; // Kontrat oluşturan
        private UUID sourceId; // Kaynak (Klan ID veya Player ID)
        private UUID targetId; // Hedef (Player ID)
        private Material transferItem;
        private int transferAmount;
        private long transferInterval; // Günlük, haftalık, vb.
        private long lastTransfer;
        private boolean isActive;
        private ContractType type; // CLAN_TO_PLAYER, PLAYER_TO_CLAN, PLAYER_TO_PLAYER
    }
    
    /**
     * Otomatik transfer kontrolü
     */
    @ScheduledTask(period = 3600L) // Her saat kontrol
    public void processTransferContracts() {
        for (TransferContract contract : activeContracts) {
            if (!contract.isActive()) continue;
            
            // Transfer zamanı geldi mi?
            if (System.currentTimeMillis() - contract.getLastTransfer() 
                < contract.getTransferInterval()) {
                continue;
            }
            
            // Transfer türüne göre işlem yap
            switch (contract.getType()) {
                case CLAN_TO_PLAYER:
                    transferFromClanToPlayer(contract);
                    break;
                case PLAYER_TO_CLAN:
                    transferFromPlayerToClan(contract);
                    break;
                case PLAYER_TO_PLAYER:
                    transferFromPlayerToPlayer(contract);
                    break;
            }
            
            contract.setLastTransfer(System.currentTimeMillis());
        }
    }
    
    /**
     * Klan bankasından oyuncuya transfer
     */
    private void transferFromClanToPlayer(TransferContract contract) {
        Clan clan = clanManager.getClanById(contract.getSourceId());
        if (clan == null) {
            deactivateContract(contract, "Klan bulunamadı!");
            return;
        }
        
        Inventory bankChest = getBankChest(clan);
        if (bankChest == null) {
            deactivateContract(contract, "Klan bankası bulunamadı!");
            return;
        }
        
        // Bankada yeterli item var mı?
        if (!bankChest.containsAtLeast(
            new ItemStack(contract.getTransferItem()), contract.getTransferAmount())) {
            // Yeterli item yok, kontratı askıya al (iptal etme, sadece beklet)
            suspendContract(contract, "Yeterli item yok, bekleniyor...");
            notifyContractCreator(contract, "§cTransfer kontratı askıya alındı: Yeterli item yok!");
            return;
        }
        
        Player target = Bukkit.getPlayer(contract.getTargetId());
        if (target == null || !target.isOnline()) {
            // Offline, özel sandığına aktar
            transferToOfflinePlayer(clan, contract);
            return;
        }
        
        // Envanter kontrolü
        HashMap<Integer, ItemStack> overflow = target.getInventory().addItem(
            new ItemStack(contract.getTransferItem(), contract.getTransferAmount()));
        
        if (!overflow.isEmpty()) {
            // Envanter dolu, özel sandığına aktar
            target.sendMessage("§cEnvanterin dolu! Transfer özel sandığına aktarıldı.");
            transferToOfflinePlayer(clan, contract);
            return;
        }
        
        // Bankadan item al
        bankChest.removeItem(new ItemStack(
            contract.getTransferItem(), contract.getTransferAmount()));
        
        target.sendMessage("§aOtomatik transfer: " + contract.getTransferAmount() + 
            "x " + contract.getTransferItem().name() + " aldınız!");
    }
    
    /**
     * Oyuncudan klan bankasına transfer
     */
    private void transferFromPlayerToClan(TransferContract contract) {
        Player source = Bukkit.getPlayer(contract.getSourceId());
        if (source == null || !source.isOnline()) {
            // Offline, özel sandığından al
            transferFromOfflinePlayerToClan(contract);
            return;
        }
        
        // Envanterde yeterli item var mı?
        if (!source.getInventory().containsAtLeast(
            new ItemStack(contract.getTransferItem()), contract.getTransferAmount())) {
            suspendContract(contract, "Oyuncuda yeterli item yok!");
            notifyContractCreator(contract, "§cTransfer kontratı askıya alındı!");
            return;
        }
        
        Clan clan = clanManager.getClanById(contract.getTargetId());
        if (clan == null) {
            deactivateContract(contract, "Klan bulunamadı!");
            return;
        }
        
        Inventory bankChest = getBankChest(clan);
        if (bankChest == null) {
            deactivateContract(contract, "Klan bankası bulunamadı!");
            return;
        }
        
        // Sandık dolu mu?
        HashMap<Integer, ItemStack> overflow = bankChest.addItem(
            new ItemStack(contract.getTransferItem(), contract.getTransferAmount()));
        
        if (!overflow.isEmpty()) {
            // Sandık dolu, kontratı askıya al
            suspendContract(contract, "Klan bankası dolu!");
            notifyContractCreator(contract, "§cTransfer kontratı askıya alındı: Banka dolu!");
            return;
        }
        
        // Oyuncudan item al
        source.getInventory().removeItem(new ItemStack(
            contract.getTransferItem(), contract.getTransferAmount()));
        
        source.sendMessage("§aOtomatik transfer: " + contract.getTransferAmount() + 
            "x " + contract.getTransferItem().name() + " klan bankasına yatırıldı!");
    }
    
    /**
     * Kontrat iptal etme (her iki tarafın kağıdını yakma)
     */
    public void cancelContract(TransferContract contract) {
        // Her iki tarafın kağıdını aynı yerde yakma kontrolü
        // (Lav veya Ateş'te yanma event'i ile)
        contract.setActive(false);
        notifyContractParties(contract, "§cTransfer kontratı iptal edildi!");
    }
}
```

**Edge Case'ler ve Çözümleri:**

**1. Klan Bankası Durumları:**
- ✅ **Banka boş**: Kontrat askıya alınır, item gelince otomatik devam eder
- ✅ **Banka dolu**: Kontrat askıya alınır, sandık boşalınca otomatik devam eder
- ✅ **Yeterli item yok**: Kısmi transfer yapılır, kalan bekler
- ✅ **Banka yok**: Kontrat iptal edilir, taraflara bildirilir

**2. Oyuncu Durumları:**
- ✅ **Offline**: Özel sandığına aktarılır (varsa), yoksa bekleme listesi
- ✅ **Envanter dolu**: Özel sandığına aktarılır, o da doluysa bekleme listesi
- ✅ **Item yok**: Kontrat askıya alınır, item gelince devam eder
- ✅ **Oyuncu yok**: Kontrat iptal edilir (30 gün offline)

**3. Kontrat Durumları:**
- ✅ **Kontrat iptal**: Her iki tarafın kağıdını aynı yerde yakma
- ✅ **Klan dağıldı**: Tüm kontratlar otomatik iptal edilir
- ✅ **Oyuncu klanı terk etti**: Klan→Oyuncu kontratları iptal, Oyuncu→Klan kontratları devam edebilir (ayar)
- ✅ **Kontrat süresi doldu**: Otomatik iptal edilir (süreli kontratlar için)

**4. Sandık Durumları:**
- ✅ **Özel sandık dolu**: Bekleme listesine eklenir, sandık boşalınca aktarılır
- ✅ **Özel sandık yok**: Bekleme listesine eklenir, sandık oluşturulunca aktarılır
- ✅ **Özel sandık kırıldı**: Bekleme listesine eklenir, yeni sandık oluşturulunca aktarılır

**5. Sistem Hataları:**
- ✅ **Sunucu restart**: Tüm kontratlar kaydedilir, restart sonrası devam eder
- ✅ **Chunk yüklenmemiş**: Chunk yüklenene kadar bekler
- ✅ **Sandık erişilemez**: Kontrat askıya alınır, erişilebilir olunca devam eder

---

### 4. Özel Sandık Sistemi (Player Vault) ⭐ **YÜKSEK ÖNCELİK**

#### Öneri: Oyuncu Özel Sandıkları

**Neden Gerekli:**
- Offline oyunculara maaş/transfer aktarımı için
- Envanter dolu olduğunda itemlerin kaybolmaması için
- Otomatik sistemlerin çalışması için

**Fiziksel Yapı:**
```
Özel Sandık İnşası:
1. Oyuncu kendi arazisinde (veya klan arazisinde):
   - Chest koy
   - Üzerine Item Frame + Name Tag (Oyuncu ismi)
   - Shift + Sağ Tık (Chest'e)
   - Özel sandık aktif!
```

**Özellikler:**
- Her oyuncu maksimum 3 özel sandık sahibi olabilir
- Sandıklar sadece sahibi tarafından açılabilir
- Otomatik sistemler sandıklara item aktarabilir
- Sandık doluysa uyarı verilir

**Teknik Uygulama:**
```java
public class PlayerVaultSystem {
    /**
     * Özel sandık oluştur
     */
    public boolean createPlayerVault(Player player, Block chest) {
        if (chest.getType() != Material.CHEST) {
            return false;
        }
        
        // Name Tag kontrolü
        boolean hasPlayerNameTag = false;
        String playerName = null;
        for (Entity entity : chest.getWorld().getNearbyEntities(
                chest.getLocation(), 2, 2, 2)) {
            if (entity instanceof ItemFrame) {
                ItemFrame frame = (ItemFrame) entity;
                ItemStack item = frame.getItem();
                if (item != null && item.getType() == Material.NAME_TAG) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null && meta.getDisplayName().equals(player.getName())) {
                        hasPlayerNameTag = true;
                        playerName = meta.getDisplayName();
                        break;
                    }
                }
            }
        }
        
        if (!hasPlayerNameTag) {
            player.sendMessage("§cÖzel sandık için Name Tag'de kendi isminiz olmalı!");
            return false;
        }
        
        // Maksimum 3 sandık kontrolü
        int vaultCount = getPlayerVaultCount(player.getUniqueId());
        if (vaultCount >= 3) {
            player.sendMessage("§cMaksimum 3 özel sandık sahibi olabilirsiniz!");
            return false;
        }
        
        // Metadata ekle
        chest.setMetadata("PlayerVault", 
            new FixedMetadataValue(plugin, player.getUniqueId()));
        
        player.sendMessage("§aÖzel sandık oluşturuldu!");
        return true;
    }
    
    /**
     * Özel sandığa item aktar (otomatik sistemler için)
     */
    public boolean depositToVault(UUID playerId, ItemStack item) {
        List<Location> vaults = getPlayerVaults(playerId);
        
        for (Location vaultLoc : vaults) {
            Block vaultBlock = vaultLoc.getBlock();
            if (vaultBlock.getType() != Material.CHEST) continue;
            
            Chest vaultChest = (Chest) vaultBlock.getState();
            Inventory vaultInv = vaultChest.getInventory();
            
            // Sandık dolu mu?
            HashMap<Integer, ItemStack> overflow = vaultInv.addItem(item);
            
            if (overflow.isEmpty()) {
                // Başarılı, item eklendi
                return true;
            }
        }
        
        // Tüm sandıklar dolu
        return false;
    }
}
```

---

### 5. Market Sistemi (Item-Based) ⭐ **YÜKSEK ÖNCELİK**

#### Mevcut Durum
- ✅ Market sistemi var (item-based zaten)
- ✅ Teklif sistemi var
- ⚠️ Klan marketi yok
- ⚠️ İttifak marketi yok
- ⚠️ Genel market yok

#### Öneri: Gelişmiş Market Sistemi

**Market Türleri:**

**1. Bireysel Market (Mevcut)**
- Oyuncular kendi marketlerini kurar
- Item-based (zaten var)
- Teklif sistemi (zaten var)
- Fiziksel: Chest + Tabela (mevcut sistem)

**2. Klan İçi Market (YENİ)**
- Sadece klan üyeleri erişebilir
- Fiziksel: Klan arazisinde özel market yapısı
- Özel fiyatlandırma (klan içi indirimler)
- GUI ile kolay erişim

**Fiziksel Yapı:**
```
Klan Marketi İnşası:
1. Klan arazisinde:
   - Chest koy
   - Üzerine Item Frame + Name Tag ("KLAN_MARKETI")
   - Shift + Sağ Tık (Chest'e)
   - Klan marketi aktif!
```

**3. İttifak Marketi (YENİ)**
- İttifaklı klanlar arası ticaret
- Fiziksel: İttifak tahtası (Lectern) + Market işareti
- Ortak kaynak paylaşımı
- GUI ile kolay erişim

**Fiziksel Yapı:**
```
İttifak Marketi İnşası:
1. İttifaklı klanların ortak bölgesinde:
   - Lectern koy
   - Üzerine Item Frame + Name Tag ("ITTIFAK_MARKETI")
   - Shift + Sağ Tık (Lectern'e)
   - İttifak marketi aktif!
```

**4. Genel Market (YENİ)**
- Tüm oyuncular erişebilir
- Fiziksel: Özel market bölgesi (spawn yakını)
- Büyük ticaret merkezi
- GUI ile kolay erişim

**Fiziksel Yapı:**
```
Genel Market:
1. Spawn bölgesinde özel yapı:
   - Büyük market binası
   - İçinde çoklu market sandıkları
   - Her sandık farklı oyuncuya ait
   - GUI ile tüm marketleri görüntüle
```

**Market GUI Sistemi:**
```java
public class AdvancedMarketSystem {
    /**
     * Klan marketi oluştur
     */
    public boolean createClanMarket(Player player, Location chestLoc) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) return false;
        
        // Yetki kontrolü
        Clan.Rank rank = clan.getRank(player.getUniqueId());
        if (rank != Clan.Rank.LEADER && rank != Clan.Rank.GENERAL) {
            player.sendMessage("§cSadece Lider veya General klan marketi oluşturabilir!");
            return false;
        }
        
        Block block = chestLoc.getBlock();
        if (block.getType() != Material.CHEST) {
            return false;
        }
        
        // Name Tag kontrolü (Item Frame'de)
        boolean hasMarketTag = false;
        for (Entity entity : chestLoc.getWorld().getNearbyEntities(chestLoc, 2, 2, 2)) {
            if (entity instanceof ItemFrame) {
                ItemFrame frame = (ItemFrame) entity;
                ItemStack item = frame.getItem();
                if (item != null && item.getType() == Material.NAME_TAG) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null && meta.getDisplayName().contains("KLAN_MARKETI")) {
                        hasMarketTag = true;
                        break;
                    }
                }
            }
        }
        
        if (!hasMarketTag) {
            player.sendMessage("§cKlan marketi için Name Tag gerekli!");
            return false;
        }
        
        // Metadata ekle
        block.setMetadata("ClanMarket", new FixedMetadataValue(plugin, clan.getId()));
        
        player.sendMessage("§aKlan marketi oluşturuldu!");
        return true;
    }
    
    /**
     * Klan marketi GUI
     */
    public void openClanMarket(Player player, Block marketChest) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) return;
        
        // Market bu klana ait mi?
        UUID marketClanId = (UUID) marketChest.getMetadata("ClanMarket").get(0).value();
        if (!marketClanId.equals(clan.getId())) {
            player.sendMessage("§cBu market sizin klanınıza ait değil!");
            return;
        }
        
        Inventory gui = Bukkit.createInventory(null, 54, 
            Component.text("§6§lKlan Marketi"));
        
        // Market listeleri (klan üyelerinin marketleri)
        List<MarketListing> clanListings = getClanMarketListings(clan);
        
        for (int i = 0; i < Math.min(clanListings.size(), 45); i++) {
            MarketListing listing = clanListings.get(i);
            ItemStack listingItem = createMarketListingItem(listing);
            gui.setItem(i, listingItem);
        }
        
        player.openInventory(gui);
    }
}
```

**Market Özellikleri:**
- **Item-Based**: Tüm işlemler itemlerle (para yok)
- **Teklif Sistemi**: Alternatif ödeme yöntemleri
- **Klan İçi İndirim**: Klan üyeleri %10 indirimli alır
- **İttifak Bonusu**: İttifaklı klanlar %5 indirimli alır

---

### 6. Bireysel Ticaret Sistemi ⭐ **ORTA ÖNCELİK**

#### Öneri: Oyuncular Arası Doğrudan Ticaret

**Fiziksel Etkileşim:**
```
1. Ticaret Kağıdı Craft Et:
   - Kağıt + Altın + Mürekkep = Ticaret Kağıdı

2. Kağıda Tıkla (Sağ Tık):
   - GUI açılır
   - Teklif oluştur (Ne veriyorum, Ne istiyorum)
   - Hedef oyuncu seç (opsiyonel: herkese açık)

3. Kağıdı Mühürle (Shift + Sağ Tık):
   - Teklif aktif olur
   - Diğer oyuncular görebilir

4. Teklif Kabul:
   - Hedef oyuncu kağıda tıklar
   - "Kabul Et" butonuna tıklar
   - Otomatik takas yapılır
```

**Teknik Uygulama:**
```java
public class PlayerTradeSystem {
    public class TradeOffer {
        private UUID offerId;
        private UUID creatorId;
        private UUID targetId; // null = herkese açık
        private List<ItemStack> offerItems; // Ne veriyorum
        private List<ItemStack> requestItems; // Ne istiyorum
        private boolean isActive;
        private long expiryTime;
    }
    
    /**
     * Ticaret kağıdı GUI
     */
    public void openTradePaperGUI(Player player, ItemStack paper) {
        Inventory gui = Bukkit.createInventory(null, 54, 
            Component.text("§6§lTicaret Teklifi Oluştur"));
        
        // Teklif itemleri (Sol taraf - Ne veriyorum)
        ItemStack offerSlot = new ItemStack(Material.EMERALD);
        ItemMeta offerMeta = offerSlot.getItemMeta();
        offerMeta.setDisplayName("§aTeklif Ekle (Ne Veriyorum)");
        offerMeta.setLore(Arrays.asList("§7Envanterinden item seç"));
        offerSlot.setItemMeta(offerMeta);
        gui.setItem(20, offerSlot);
        
        // İstek itemleri (Sağ taraf - Ne istiyorum)
        ItemStack requestSlot = new ItemStack(Material.DIAMOND);
        ItemMeta requestMeta = requestSlot.getItemMeta();
        requestMeta.setDisplayName("§bİstek Ekle (Ne İstiyorum)");
        requestMeta.setLore(Arrays.asList("§7İstediğin item türünü seç"));
        requestSlot.setItemMeta(requestMeta);
        gui.setItem(24, requestSlot);
        
        // Hedef oyuncu seçimi
        ItemStack targetSlot = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta targetMeta = targetSlot.getItemMeta();
        targetMeta.setDisplayName("§eHedef Oyuncu");
        targetMeta.setLore(Arrays.asList("§7Tıkla: Oyuncu seç", "§7Boş bırak: Herkese açık"));
        targetSlot.setItemMeta(targetMeta);
        gui.setItem(31, targetSlot);
        
        // Onay butonu
        ItemStack confirmSlot = new ItemStack(Material.GREEN_CONCRETE);
        ItemMeta confirmMeta = confirmSlot.getItemMeta();
        confirmMeta.setDisplayName("§a§lTeklif Oluştur");
        confirmMeta.setLore(Arrays.asList("§7Tıkla: Teklifi aktif et"));
        confirmSlot.setItemMeta(confirmMeta);
        gui.setItem(40, confirmSlot);
        
        player.openInventory(gui);
    }
}
```

---

### 7. Ekonomi Sistemi Özeti ve Entegrasyon

#### Ekonomi Akışı

**1. Klan İçi Ekonomi:**
```
Klan Bankası (Ender Chest)
    ↓
Otomatik Maaş Sistemi (Item-Based)
    ↓
Üyelerin Özel Sandıkları / Envanterleri
    ↓
Otomatik Transfer Kontratları (Opsiyonel)
```

**2. Klanlar Arası Ekonomi:**
```
İttifak Marketi
    ↓
Item Takası (Item-Based)
    ↓
Ortak Kaynak Paylaşımı
```

**3. Bireysel Ekonomi:**
```
Bireysel Marketler
    ↓
Oyuncular Arası Ticaret
    ↓
Ticaret Kağıdı Sistemi
```

#### Tüm Edge Case'ler ve Çözümleri

**Banka Durumları:**
- ✅ **Banka boş**: Kontrat askıya alınır, item gelince otomatik devam
- ✅ **Banka dolu**: Kontrat askıya alınır, sandık boşalınca otomatik devam
- ✅ **Yeterli item yok**: Kısmi transfer yapılır, kalan bekler
- ✅ **Banka yok**: Kontrat iptal edilir, taraflara bildirilir

**Oyuncu Durumları:**
- ✅ **Offline**: Özel sandığına aktarılır (varsa), yoksa bekleme listesi
- ✅ **Envanter dolu**: Özel sandığına aktarılır, o da doluysa bekleme listesi
- ✅ **Item yok**: Kontrat askıya alınır, item gelince devam eder
- ✅ **Oyuncu yok**: Kontrat iptal edilir (30 gün offline)

**Kontrat Durumları:**
- ✅ **Kontrat iptal**: Her iki tarafın kağıdını aynı yerde yakma
- ✅ **Klan dağıldı**: Tüm kontratlar otomatik iptal edilir
- ✅ **Oyuncu klanı terk etti**: Klan→Oyuncu kontratları iptal, Oyuncu→Klan kontratları devam edebilir
- ✅ **Kontrat süresi doldu**: Otomatik iptal edilir (süreli kontratlar için)

**Sandık Durumları:**
- ✅ **Özel sandık dolu**: Bekleme listesine eklenir, sandık boşalınca aktarılır
- ✅ **Özel sandık yok**: Bekleme listesine eklenir, sandık oluşturulunca aktarılır
- ✅ **Özel sandık kırıldı**: Bekleme listesine eklenir, yeni sandık oluşturulunca aktarılır

**Sistem Hataları:**
- ✅ **Sunucu restart**: Tüm kontratlar kaydedilir, restart sonrası devam eder
- ✅ **Chunk yüklenmemiş**: Chunk yüklenene kadar bekler
- ✅ **Sandık erişilemez**: Kontrat askıya alınır, erişilebilir olunca devam eder

#### Özgürlük Felsefesi

**Küçük Klanlar:**
- Basit sistemler yeterli
- Otomatik sistemler kullanmayabilir
- Elle yönetim mümkün

**Büyük Klanlar:**
- Otomatik sistemler gerekli
- Transfer kontratları kullanılır
- Gelişmiş marketler kurulur

**Herkes İçin:**
- İstediği sistemi kullanır veya hiç kullanmaz
- Para yok, sadece itemler
- Fiziksel etkileşim korunur

---

## 🎮 KARMAŞIK İŞLEMLER İÇİN HİBRİT SİSTEM ⭐ **YÜKSEK ÖNCELİK**

**Tespit Edilen Tüm Karmaşık İşlemler:**

| İşlem | Karmaşıklık | Yöntem | Fiziksel Etkileşim | GUI |
|-------|-------------|--------|-------------------|-----|
| **İttifak Kurma** | Yüksek | Özel Item (Kağıt) + GUI | Craft + Tıkla + Mühürle | ✅ |
| **Transfer Kontratı** | Yüksek | Özel Item (Kağıt) + GUI | Craft + Tıkla + Mühürle | ✅ |
| **Maaş Ayarları** | Orta | Özel Yapı (Yönetim Merkezi) + GUI | Yapıya gir | ✅ |
| **Görevler** | Orta | Özel Yapı (Görev Loncası) + GUI | Lectern'e tıkla | ✅ |
| **Banka İşlemleri** | Orta | Özel Yapı (Klan Bankası) + GUI | Ender Chest'e tıkla | ✅ |
| **Market İşlemleri** | Orta | Özel Yapı (Market) + GUI | Chest'e tıkla | ✅ |
| **Bireysel Ticaret** | Orta | Özel Item (Ticaret Kağıdı) + GUI | Craft + Tıkla + Mühürle | ✅ |
| **İstatistikler** | Düşük | Özel Yapı (İstatistik Tahtası) + GUI | Item Frame'e tıkla | ✅ |
| **Üye Yönetimi** | Orta | Özel Yapı (Yönetim Merkezi) + GUI | Yapıya gir | ✅ |
| **Klan Ayarları** | Orta | Özel Yapı (Yönetim Merkezi) + GUI | Yapıya gir | ✅ |

**Tüm İşlemler İçin Ortak Prensipler:**
- ✅ Fiziksel etkileşim korunuyor
- ✅ GUI ile kolay kullanım
- ✅ Item-based (para yok)
- ✅ Otomatik sistemler (elle uğraşma yok)
- ✅ Edge case'ler düşünülmüş (sandık dolu, boş, offline, vb.)

---

## 👥 SOSYAL VE ETKİLEŞİM ÖZELLİKLERİ {#sosyal}

### 12. Klan Chat Geliştirmeleri ⭐ **ORTA ÖNCELİK**

#### Öneri: Gelişmiş Chat (Ana Mantık Korunuyor)

**Yeni Özellikler:**
- Renkli chat (rütbe bazlı)
- Emoji desteği
- @mention sistemi
- Chat geçmişi (son 50 mesaj)

**Teknik Uygulama:**
```java
public class AdvancedClanChat {
    // Klan -> Son 50 mesaj
    private final Map<UUID, List<ChatMessage>> chatHistory = new ConcurrentHashMap<>();
    
    public class ChatMessage {
        private UUID senderId;
        private String senderName;
        private String message;
        private long timestamp;
        private Clan.Rank rank;
    }
    
    /**
     * Klan chat mesajı gönder
     */
    public void sendClanMessage(Player sender, String message) {
        Clan clan = clanManager.getClanByPlayer(sender.getUniqueId());
        if (clan == null) return;
        
        Clan.Rank rank = clan.getRank(sender.getUniqueId());
        String rankColor = getRankColor(rank);
        String rankPrefix = getRankPrefix(rank);
        
        // Mesaj formatı
        String formattedMessage = rankColor + rankPrefix + " §7" + 
            sender.getName() + ": §f" + message;
        
        // Tüm klan üyelerine gönder
        for (UUID memberId : clan.getMembers().keySet()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null) {
                member.sendMessage(formattedMessage);
            }
        }
        
        // Chat geçmişine ekle
        ChatMessage chatMsg = new ChatMessage();
        chatMsg.setSenderId(sender.getUniqueId());
        chatMsg.setSenderName(sender.getName());
        chatMsg.setMessage(message);
        chatMsg.setTimestamp(System.currentTimeMillis());
        chatMsg.setRank(rank);
        
        List<ChatMessage> history = chatHistory.computeIfAbsent(
            clan.getId(), k -> new ArrayList<>());
        history.add(chatMsg);
        
        // Son 50 mesajı tut
        if (history.size() > 50) {
            history.remove(0);
        }
    }
    
    private String getRankColor(Clan.Rank rank) {
        switch (rank) {
            case LEADER: return "§6"; // Altın
            case GENERAL: return "§c"; // Kırmızı
            case ELITE: return "§b"; // Açık mavi
            case MEMBER: return "§7"; // Gri
            case RECRUIT: return "§8"; // Koyu gri
            default: return "§f";
        }
    }
    
    private String getRankPrefix(Clan.Rank rank) {
        switch (rank) {
            case LEADER: return "[Lider]";
            case GENERAL: return "[General]";
            case ELITE: return "[Elite]";
            case MEMBER: return "[Üye]";
            case RECRUIT: return "[Acemi]";
            default: return "";
        }
    }
}
```

---

### 13. Klan Rozetleri ve Unvanlar ⭐ **DÜŞÜK ÖNCELİK**

#### Öneri: Rozet Sistemi (Ana Mantık Korunuyor)

**Rozet Türleri:**
- Savaş rozetleri: "Savaşçı", "Fatih", "Efsanevi Savaşçı"
- Yapı rozetleri: "İnşaatçı", "Mimar", "Usta İnşaatçı"
- Ritüel rozetleri: "Ritüel Ustası", "Ritüel Araştırmacısı"

**Fiziksel Görüntüleme:**
- Rozet Item Frame'de görünür
- Shift + Sağ Tık ile rozet seçilir

---

## 📈 İLERLEME VE ÖDÜL SİSTEMLERİ {#ilerleme}

### 14. Klan Seviye Ödülleri (Item-Based) ⭐ **YÜKSEK ÖNCELİK**

#### Öneri: Seviye Atlama Ödülleri (Item-Based)

**Özellikler:**
- Klan seviye atladığında tüm üyelere ödül
- Ödüller: Itemler (Elmas, Altın, Özel itemler), XP
- Fiziksel: Seviye atlama partikül efektleri

**Ödül Türleri (Seviye Bazlı):**
```yaml
Seviye 1-3:
  - Ödül: 10x Elmas + 50x Altın + 100 XP

Seviye 4-7:
  - Ödül: 25x Elmas + 100x Altın + 5x Titanyum + 200 XP

Seviye 8-12:
  - Ödül: 50x Elmas + 200x Altın + 10x Titanyum + Özel Item + 500 XP

Seviye 13-15:
  - Ödül: 100x Elmas + 500x Altın + 25x Titanyum + Efsanevi Item + 1000 XP
```

**Teknik Uygulama:**
```java
public class ItemBasedClanLevelRewards {
    /**
     * Seviye atlama kontrolü
     */
    public void checkLevelUp(Clan clan) {
        int oldLevel = getPreviousClanLevel(clan);
        int newLevel = powerSystem.calculateClanLevel(clan);
        
        if (newLevel > oldLevel) {
            // Seviye atladı!
            onClanLevelUp(clan, oldLevel, newLevel);
        }
    }
    
    private void onClanLevelUp(Clan clan, int oldLevel, int newLevel) {
        // Ödül listesi oluştur
        List<ItemStack> rewards = calculateLevelUpRewards(newLevel);
        
        // Tüm üyelere ödül
        for (UUID memberId : clan.getMembers().keySet()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null) {
                // Item ödülleri
                for (ItemStack reward : rewards) {
                    HashMap<Integer, ItemStack> overflow = 
                        member.getInventory().addItem(reward);
                    
                    if (!overflow.isEmpty()) {
                        // Envanter dolu, özel sandığına aktar
                        playerVaultSystem.depositToVault(memberId, reward);
                        member.sendMessage("§cEnvanterin dolu! Ödüller özel sandığına aktarıldı.");
                    }
                }
                
                // XP ödülü
                int xpReward = newLevel * 100;
                member.giveExp(xpReward);
                
                // Partikül efektleri
                member.getLocation().getWorld().spawnParticle(
                    Particle.TOTEM, member.getLocation(), 50, 1, 1, 1, 0.1);
                
                member.sendMessage("§6§lKLAN SEVİYE ATLADI!");
                member.sendMessage("§eSeviye: §6" + oldLevel + " §7→ §6" + newLevel);
                member.sendTitle("§6§lSEVİYE " + newLevel, "§eTebrikler!", 10, 70, 20);
            } else {
                // Offline üye, özel sandığına aktar
                for (ItemStack reward : rewards) {
                    playerVaultSystem.depositToVault(memberId, reward);
                }
            }
        }
        
        // Broadcast
        Bukkit.broadcastMessage("§6§l" + clan.getName() + " klanı seviye " + 
            newLevel + " oldu!");
    }
    
    private List<ItemStack> calculateLevelUpRewards(int level) {
        List<ItemStack> rewards = new ArrayList<>();
        
        if (level <= 3) {
            rewards.add(new ItemStack(Material.DIAMOND, 10));
            rewards.add(new ItemStack(Material.GOLD_INGOT, 50));
        } else if (level <= 7) {
            rewards.add(new ItemStack(Material.DIAMOND, 25));
            rewards.add(new ItemStack(Material.GOLD_INGOT, 100));
            rewards.add(new ItemStack(Material.IRON_INGOT, 5)); // Titanyum placeholder
        } else if (level <= 12) {
            rewards.add(new ItemStack(Material.DIAMOND, 50));
            rewards.add(new ItemStack(Material.GOLD_INGOT, 200));
            rewards.add(new ItemStack(Material.IRON_INGOT, 10)); // Titanyum placeholder
            rewards.add(createSpecialItem(level)); // Özel item
        } else {
            rewards.add(new ItemStack(Material.DIAMOND, 100));
            rewards.add(new ItemStack(Material.GOLD_INGOT, 500));
            rewards.add(new ItemStack(Material.IRON_INGOT, 25)); // Titanyum placeholder
            rewards.add(createLegendaryItem(level)); // Efsanevi item
        }
        
        return rewards;
    }
}
```

---

## 🎮 KARMAŞIK İŞLEMLER İÇİN HİBRİT SİSTEM ⭐ **YÜKSEK ÖNCELİK**

### Problem: Ritüeller Karmaşık İşlemler İçin Yetersiz

**Mevcut Durum:**
- ✅ Basit işlemler için ritüeller mükemmel (üye ekleme, terfi, vb.)
- ⚠️ Karmaşık işlemler için ritüeller çok zor (ittifak, kontrat, maaş ayarları, görevler)
- ⚠️ Ezberlenmesi zor, yavaş, karmaşık işlemler yapılamaz

**Çözüm: Hibrit Sistem**
- Basit işlemler → Ritüeller (mevcut sistem)
- Karmaşık işlemler → Özel Yapılar + GUI veya Özel Itemler + GUI

---

### Hibrit Sistem Mimarisi

**1. Basit İşlemler (Ritüeller - Mevcut)**
- Üye ekleme/çıkarma
- Terfi verme
- Kristal taşıma
- Pes etme (Beyaz Bayrak)

**2. Karmaşık İşlemler (Yeni Sistemler)**

#### A. İttifak ve Kontrat Sistemi → **Özel Item (Kağıt) + GUI**

**Fiziksel Etkileşim:**
```
1. İttifak/Kontrat Kağıdı Craft Et:
   - Kağıt + Altın + Mürekkep = İttifak Kağıdı
   - Kağıt + Demir + Mürekkep = Kontrat Kağıdı

2. Kağıda Tıkla (Sağ Tık):
   - GUI açılır
   - İttifak/Kontrat türü seçilir
   - Detaylar girilir (süre, koşullar, vb.)

3. Kağıdı Mühürle (Shift + Sağ Tık):
   - Kağıt "Mühürlü" olur
   - İki tarafın da mühürlemesi gerekir
   - Mühürlenince aktif olur

4. İptal Etme:
   - Her iki tarafın kağıdını aynı yerde yak (Lav veya Ateş)
   - Kontrat/İttifak geçersiz olur
```

**Teknik Uygulama:**
```java
public class AllianceContractItemSystem {
    public class AllianceContractPaper {
        private UUID paperId;
        private UUID creatorId; // İlk lider
        private UUID targetClanId; // Hedef klan
        private AllianceType type; // DEFENSIVE, OFFENSIVE, TRADE, FULL
        private Map<String, Object> terms; // Koşullar
        private boolean creatorSealed; // İlk lider mühürledi mi?
        private boolean targetSealed; // Hedef lider mühürledi mi?
        private long expiryTime; // Süre (opsiyonel)
    }
    
    /**
     * Kağıt craft etme
     */
    public ItemStack craftAlliancePaper(Player player) {
        // Craft kontrolü (Shapeless Recipe)
        // Kağıt + Altın + Mürekkep = İttifak Kağıdı
        
        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta meta = paper.getItemMeta();
        meta.setDisplayName("§6§lİttifak Kağıdı");
        List<String> lore = new ArrayList<>();
        lore.add("§7Sağ tık: İttifak oluştur");
        lore.add("§7Shift + Sağ tık: Mühürle");
        meta.setLore(lore);
        paper.setItemMeta(meta);
        
        // NBT Tag ekle (özel item işareti)
        NBTItem nbtItem = new NBTItem(paper);
        nbtItem.setString("alliance-paper", "true");
        nbtItem.setUUID("paper-id", UUID.randomUUID());
        
        return nbtItem.getItem();
    }
    
    /**
     * Kağıda tıklama (GUI aç)
     */
    @EventHandler
    public void onPaperClick(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null) return;
        
        NBTItem nbtItem = new NBTItem(item);
        if (!nbtItem.hasKey("alliance-paper")) return;
        
        if (event.getAction() == Action.RIGHT_CLICK_AIR || 
            event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            
            if (event.getPlayer().isSneaking()) {
                // Shift + Sağ Tık = Mühürle
                sealPaper(event.getPlayer(), item);
            } else {
                // Normal Sağ Tık = GUI aç
                openAllianceGUI(event.getPlayer(), item);
            }
        }
    }
    
    /**
     * İttifak GUI aç
     */
    private void openAllianceGUI(Player player, ItemStack paper) {
        Inventory gui = Bukkit.createInventory(null, 27, 
            Component.text("§6§lİttifak Oluştur"));
        
        // İttifak türü seçimi
        ItemStack defensive = new ItemStack(Material.SHIELD);
        ItemMeta defMeta = defensive.getItemMeta();
        defMeta.setDisplayName("§aSavunma İttifakı");
        List<String> defLore = new ArrayList<>();
        defLore.add("§7Bir klana saldırılırsa");
        defLore.add("§7diğeri otomatik yardım eder");
        defMeta.setLore(defLore);
        defensive.setItemMeta(defMeta);
        gui.setItem(10, defensive);
        
        // ... diğer türler
        
        // Süre seçimi (opsiyonel)
        ItemStack duration = new ItemStack(Material.CLOCK);
        ItemMeta durMeta = duration.getItemMeta();
        durMeta.setDisplayName("§eSüre: Sınırsız");
        durMeta.setLore(Arrays.asList("§7Tıkla: Süre değiştir"));
        duration.setItemMeta(durMeta);
        gui.setItem(16, duration);
        
        // Onay butonu
        ItemStack confirm = new ItemStack(Material.EMERALD);
        ItemMeta confMeta = confirm.getItemMeta();
        confMeta.setDisplayName("§a§lİttifak Oluştur");
        confMeta.setLore(Arrays.asList("§7Tıkla: İttifak kağıdını hazırla"));
        confirm.setItemMeta(confMeta);
        gui.setItem(22, confirm);
        
        player.openInventory(gui);
    }
    
    /**
     * Kağıdı mühürle
     */
    private void sealPaper(Player player, ItemStack paper) {
        NBTItem nbtItem = new NBTItem(paper);
        UUID paperId = nbtItem.getUUID("paper-id");
        
        AllianceContractPaper contract = getContract(paperId);
        if (contract == null) {
            player.sendMessage("§cBu kağıt henüz doldurulmamış!");
            return;
        }
        
        Clan playerClan = clanManager.getClanByPlayer(player.getUniqueId());
        if (playerClan == null) {
            player.sendMessage("§cKlan üyesi değilsiniz!");
            return;
        }
        
        // İlk lider mi?
        if (contract.getCreatorId().equals(player.getUniqueId())) {
            contract.setCreatorSealed(true);
            player.sendMessage("§aKağıdı mühürlediniz! Diğer liderin de mühürlemesi gerekiyor.");
        } 
        // Hedef lider mi?
        else if (contract.getTargetClanId().equals(playerClan.getId())) {
            contract.setTargetSealed(true);
            player.sendMessage("§aKağıdı mühürlediniz!");
        }
        
        // Her iki taraf da mühürledi mi?
        if (contract.isCreatorSealed() && contract.isTargetSealed()) {
            // İttifak aktif ol!
            activateAlliance(contract);
            
            // Her iki tarafa da bildir
            notifyAllianceCreated(contract);
        }
    }
    
    /**
     * Kağıtları yakarak iptal etme
     */
    @EventHandler
    public void onPaperBurn(BlockBurnEvent event) {
        // Lav veya ateşte yanan kağıtları kontrol et
        // Eğer her iki tarafın kağıdı da aynı yerde yanıyorsa iptal et
    }
}
```

---

#### B. Klan Yönetim İşlemleri → **Özel Yapı (Klan Yönetim Merkezi) + GUI**

**Fiziksel Etkileşim:**
```
1. Klan Yönetim Merkezi İnşa Et:
   - 3x3 Taş Tuğla platform
   - Ortada Enchantment Table
   - 4 köşede Kızıltaş Meşalesi
   - Üzerine Item Frame + Name Tag ("KLAN_YONETIM")

2. Yapıya Gir (Enchantment Table'a Shift + Sağ Tık):
   - GUI açılır
   - Menü seçenekleri:
     * Maaş Ayarları
     * Üye Yönetimi
     * Klan Ayarları
     * İstatistikler
     * Banka Yönetimi
```

**Teknik Uygulama:**
```java
public class ClanManagementCenter {
    /**
     * Klan Yönetim Merkezi kontrolü
     */
    public boolean isManagementCenter(Block block) {
        if (block.getType() != Material.ENCHANTING_TABLE) {
            return false;
        }
        
        // Name Tag kontrolü (Item Frame'de)
        for (Entity entity : block.getWorld().getNearbyEntities(
                block.getLocation(), 2, 2, 2)) {
            if (entity instanceof ItemFrame) {
                ItemFrame frame = (ItemFrame) entity;
                ItemStack item = frame.getItem();
                if (item != null && item.getType() == Material.NAME_TAG) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null && meta.getDisplayName().contains("KLAN_YONETIM")) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Yönetim merkezine tıklama (GUI aç)
     */
    @EventHandler
    public void onManagementCenterClick(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.ENCHANTING_TABLE) {
            return;
        }
        
        if (!isManagementCenter(block)) return;
        
        if (event.getPlayer().isSneaking() && 
            event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            
            // Ana menü aç
            openMainMenu(event.getPlayer());
        }
    }
    
    /**
     * Ana menü
     */
    private void openMainMenu(Player player) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) return;
        
        Inventory gui = Bukkit.createInventory(null, 27, 
            Component.text("§6§lKlan Yönetim Merkezi"));
        
        // Maaş Ayarları
        ItemStack salary = new ItemStack(Material.GOLD_INGOT);
        ItemMeta salMeta = salary.getItemMeta();
        salMeta.setDisplayName("§eMaaş Ayarları");
        List<String> salLore = new ArrayList<>();
        salLore.add("§7Rütbe bazlı maaş ayarları");
        salLore.add("§7Mevcut: " + getCurrentSalary(clan));
        salMeta.setLore(salLore);
        salary.setItemMeta(salMeta);
        gui.setItem(10, salary);
        
        // Üye Yönetimi
        ItemStack members = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta memMeta = members.getItemMeta();
        memMeta.setDisplayName("§bÜye Yönetimi");
        memMeta.setLore(Arrays.asList("§7Üyeleri görüntüle ve yönet"));
        members.setItemMeta(memMeta);
        gui.setItem(12, members);
        
        // Klan Ayarları
        ItemStack settings = new ItemStack(Material.COMPARATOR);
        ItemMeta setMeta = settings.getItemMeta();
        setMeta.setDisplayName("§dKlan Ayarları");
        setMeta.setLore(Arrays.asList("§7Klan ayarlarını değiştir"));
        settings.setItemMeta(setMeta);
        gui.setItem(14, settings);
        
        // Banka Yönetimi
        ItemStack bank = new ItemStack(Material.CHEST);
        ItemMeta bankMeta = bank.getItemMeta();
        bankMeta.setDisplayName("§6Banka Yönetimi");
        List<String> bankLore = new ArrayList<>();
        bankLore.add("§7Item çek/yatır");
        bankLore.add("§7Otomatik transfer kontratları");
        bankMeta.setLore(bankLore);
        bank.setItemMeta(bankMeta);
        gui.setItem(16, bank);
        
        player.openInventory(gui);
    }
    
    /**
     * Maaş ayarları GUI
     */
    private void openSalarySettings(Player player, Clan clan) {
        Inventory gui = Bukkit.createInventory(null, 27, 
            Component.text("§e§lMaaş Ayarları"));
        
        // Her rütbe için maaş ayarı
        for (Clan.Rank rank : Clan.Rank.values()) {
            ItemStack rankItem = new ItemStack(getRankMaterial(rank));
            ItemMeta meta = rankItem.getItemMeta();
            meta.setDisplayName(getRankDisplayName(rank));
            
            List<String> lore = new ArrayList<>();
            lore.add("§7Mevcut Maaş: §e" + getRankSalary(clan, rank));
            lore.add("§7Tıkla: Maaş değiştir");
            meta.setLore(lore);
            rankItem.setItemMeta(meta);
            
            gui.setItem(getRankSlot(rank), rankItem);
        }
        
        player.openInventory(gui);
    }
}
```

---

#### C. Görev Sistemi → **Özel Yapı (Görev Loncası) + GUI**

**Fiziksel Etkileşim:**
```
1. Görev Loncası İnşa Et:
   - Lectern + Item Frame + Name Tag ("GOREV_LONCASI")
   - (Mevcut sistemde var, GUI eklenir)

2. Lectern'e Tıkla (Sağ Tık):
   - GUI açılır
   - Bireysel görevler listesi
   - Klan görevleri listesi
   - Görev al/tamamla
```

**Teknik Uygulama:**
```java
public class QuestGuildGUI {
    /**
     * Görev loncası GUI
     */
    public void openQuestGuild(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, 
            Component.text("§6§lGörev Loncası"));
        
        // Bireysel Görevler (Sol taraf)
        List<IndividualQuest> dailyQuests = questSystem.getDailyQuests(player);
        for (int i = 0; i < Math.min(dailyQuests.size(), 21); i++) {
            IndividualQuest quest = dailyQuests.get(i);
            ItemStack questItem = createQuestItem(quest);
            gui.setItem(i, questItem);
        }
        
        // Klan Görevleri (Sağ taraf)
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan != null) {
            List<ClanQuest> clanQuests = questSystem.getClanQuests(clan);
            for (int i = 0; i < Math.min(clanQuests.size(), 21); i++) {
                ClanQuest quest = clanQuests.get(i);
                ItemStack questItem = createClanQuestItem(quest);
                gui.setItem(27 + i, questItem);
            }
        }
        
        // Görev İlerlemesi (Alt kısım)
        ItemStack progress = new ItemStack(Material.BOOK);
        ItemMeta progMeta = progress.getItemMeta();
        progMeta.setDisplayName("§eGörev İlerlemesi");
        List<String> progLore = new ArrayList<>();
        progLore.add("§7Tamamlanan: " + getCompletedQuests(player));
        progMeta.setLore(progLore);
        progress.setItemMeta(progMeta);
        gui.setItem(49, progress);
        
        player.openInventory(gui);
    }
}
```

---

#### D. Banka İşlemleri → **Özel Yapı (Klan Bankası) + GUI**

**Fiziksel Etkileşim:**
```
1. Klan Bankası İnşa Et:
   - Chest + Item Frame + Name Tag ("KLAN_BANKASI")
   - (Mevcut kristal sistemi ile entegre)

2. Chest'e Tıkla (Shift + Sağ Tık):
   - GUI açılır
   - Para yatır/çek
   - Maaş ayarları
   - Faiz bilgisi
```

---

### Hibrit Sistem Özeti

| İşlem Türü | Yöntem | Fiziksel Etkileşim | GUI |
|------------|--------|-------------------|-----|
| **Basit İşlemler** | Ritüel | Mevcut sistem | ❌ |
| **İttifak/Kontrat** | Özel Item (Kağıt) | Craft + Tıkla + Mühürle | ✅ |
| **Maaş Ayarları** | Özel Yapı (Yönetim Merkezi) | Yapıya gir | ✅ |
| **Görevler** | Özel Yapı (Görev Loncası) | Lectern'e tıkla | ✅ |
| **Banka** | Özel Yapı (Klan Bankası) | Chest'e tıkla | ✅ |
| **İstatistikler** | Özel Yapı (İstatistik Tahtası) | Item Frame'e tıkla | ✅ |

### Avantajlar

✅ **Fiziksel Etkileşim Korunuyor**: Tüm işlemler fiziksel (yapı, item, tıklama)
✅ **Pratik**: GUI ile karmaşık işlemler kolay
✅ **Ezberlenmesi Kolay**: Her işlem için net yöntem
✅ **Hızlı**: Ritüellerden çok daha hızlı
✅ **Esnek**: Her işlem türü için en uygun yöntem

---

## ⚙️ TEKNİK UYGULAMA DETAYLARI {#teknik}

### Config.yml Entegrasyonu

Tüm yeni özellikler config'den kontrol edilebilir olmalı:

```yaml
clan-system:
  # Seviye bazlı bonuslar (SINIR YOK, SADECE BONUSLAR)
  level-bonuses:
    level1-3:
      power-bonus: 0.0  # Bonus yok
      features: ["basic-chat", "bank"]
    level4-7:
      power-bonus: 0.05  # %5 güç bonusu
      features: ["basic-chat", "bank", "market", "alliance"]
    level8-12:
      power-bonus: 0.10  # %10 güç bonusu
      features: ["basic-chat", "bank", "market", "alliance", "war", "special-structures"]
    level13-15:
      power-bonus: 0.15  # %15 güç bonusu
      features: ["basic-chat", "bank", "market", "alliance", "war", "special-structures", "capital", "events", "badges"]
  
  # Banka sistemi (ITEM-BASED - PARA YOK)
  bank:
    # Otomatik maaş sistemi
    salary-check-interval: 3600 # Her saat kontrol (tick)
    salary-distribution-intervals:
      daily: 86400    # Günlük (tick)
      weekly: 604800  # Haftalık (tick)
      monthly: 2592000 # Aylık (tick)
    
    # Maaş ayarları (item-based)
    default-salary:
      leader:
        item: DIAMOND
        amount: 10
        interval: weekly
      general:
        item: DIAMOND
        amount: 5
        interval: weekly
      elite:
        item: GOLD_INGOT
        amount: 20
        interval: weekly
      member:
        item: IRON_INGOT
        amount: 10
        interval: weekly
    
    # Günlük çekme limitleri (item sayısı - spam önleme)
    daily-withdraw-limits:
      general: 1000  # Maksimum 1000 item/gün
      elite: 500     # Maksimum 500 item/gün
      member: 200    # Maksimum 200 item/gün
    
    # Özel sandık sistemi
    player-vault:
      max-vaults-per-player: 3  # Oyuncu başına maksimum özel sandık
  
  # Aktivite sistemi
  activity:
    inactive-threshold: 2592000000 # 30 gün (ms)
    auto-promote-recruit-days: 7 # 7 gün sonra Member
  
  # Başkent sistemi
  capital:
    bonus-radius: 50 # Blok
    power-bonus: 0.10 # %10
  
  # Savaş türleri
  war-types:
    raid:
      warmup-time: 120 # 2 dakika (saniye)
      duration: 1800 # 30 dakika (saniye)
      loot-percentage: 0.3 # %30
    open-war:
      warmup-time: 300 # 5 dakika
      duration: 3600 # 1 saat
      loot-percentage: 0.4 # %40
  
  # Kaynak paylaşımı (rütbe bazlı limitler - item sayısı, spam önleme)
  resource-sharing:
    # Günlük istek limitleri (item sayısı)
    daily-request-limits:
      general: 10000  # Maksimum 10000 item/gün
      elite: 5000
      member: 2000
      recruit: 1000
    
    # Günlük bağış limitleri (item sayısı)
    daily-donation-limits:
      general: 20000  # Maksimum 20000 item/gün
      elite: 10000
      member: 5000
      recruit: 2000
  
  # Otomatik transfer kontratları
  transfer-contracts:
    check-interval: 3600  # Her saat kontrol (tick)
    max-contracts-per-player: 10  # Oyuncu başına maksimum kontrat
    max-contracts-per-clan: 50    # Klan başına maksimum kontrat
    contract-expiry-days: 30      # Kontrat süresi (gün) - 0 = sınırsız
    pending-salary-check-interval: 300  # Bekleme listesi kontrolü (5 dakika)
  
  # Market sistemleri
  markets:
    # Klan marketi
    clan-market:
      max-markets-per-clan: 5  # Klan başına maksimum market
      discount-for-members: 0.10  # Klan üyeleri %10 indirim
    
    # İttifak marketi
    alliance-market:
      discount-for-allies: 0.05  # İttifaklı klanlar %5 indirim
    
    # Genel market
    global-market:
      max-listings-per-player: 20  # Oyuncu başına maksimum liste
      max-listings-per-market: 100  # Market başına maksimum liste

# Koruma Sistemi (YENİ - Hibrit)
protection-system:
  # Güç bazlı koruma
  power-threshold: 0.40  # %40 (eski: 0.50) - Hedef, saldıranın gücünün %40'ından düşükse saldırı yasak
  
  # Seviye bazlı koruma
  max-level-diff: 5      # Maksimum seviye farkı (eski: 3) - Logaritmik sistem için daha uygun
  
  # Acemi koruması
  rookie-power-threshold: 3000.0  # Güç eşiği (eski: 5000.0)
  rookie-level-threshold: 5       # Seviye eşiği (YENİ)
  
  # Aktivite koruması
  inactive-threshold: 604800000   # 7 gün (ms) - Offline oyuncuları korur
  
  # Klan içi koruma
  clan-threshold: 0.50  # %50 (eski: 0.60) - Daha dengeli
  
  # Hasar azaltma
  damage-reduction-min: 0.05  # Minimum hasar (%5) - Koruma aktifse
  damage-reduction-max: 0.50  # Maksimum hasar (%50) - Kademeli azaltma
```

---

## 🎯 ÖNCELİK SIRASI

### Faz 1: Kritik Özellikler (1-2 Hafta) ⚡ **EN ÖNCELİKLİ**
1. ⭐ **Hibrit İşlem Sistemi** (Karmaşık işlemler için GUI + Özel Yapılar/Itemler)
2. ⭐ **Gelişmiş Oyuncu Koruma Sistemi** (Hibrit: Güç + Seviye + Aktivite)
3. ⭐ Klan seviye bazlı bonuslar (Sınır yok, sadece avantajlar)
4. ⭐ Gelişmiş rütbe sistemi (Elite rütbesi)
5. ⭐ Görev sistemi (Bireysel + Klan)

### Faz 2: Yönetim Geliştirmeleri (2-3 Hafta)
5. ⭐ **Item-Based Ekonomi Sistemi** (Banka, Maaş, Otomatik Transfer)
6. ⭐ Klan seviye ödülleri (Item-Based)
7. ⭐ Klan istatistikleri
8. ⭐ Üye aktivite takibi
9. ⭐ Özel Sandık Sistemi (Player Vault)

### Faz 3: Savaş ve İttifak (2-3 Hafta)
9. ⭐ Çeşitli savaş türleri (Raid, Open War)
10. ⭐ İttifak geliştirmeleri
11. ⭐ Klan başkenti sistemi

### Faz 4: Sosyal Özellikler (1-2 Hafta)
12. ⭐ Kaynak paylaşımı (Yetkiye göre)
13. ⭐ Klan chat geliştirmeleri
14. ⭐ Klan rozetleri
15. ⭐ Toprak vergisi

---

## 📝 ÖNEMLİ NOTLAR

### Ana Mantık Koruma Prensipleri

1. **Fiziksel Etkileşim**: Tüm yeni özellikler fiziksel etkileşimle çalışmalı
2. **5 Dakika Warmup**: Normal savaşlar için 5 dakika warmup korunur (Raid için 2 dk seçeneği)
3. **24 Saat Grace Period**: Yeni klanlar 24 saat korunur (değişmez)
4. **Hibrit Koruma Sistemi**: Güç + Seviye + Aktivite bazlı koruma (3 seviye kuralı → 5 seviye + güç kontrolü)
5. **Rütbe Sistemi**: Mevcut fiziksel terfi ritüeli korunur

### İyileştirme Önerileri

**Mevcut Sistemde İyileştirilebilir Noktalar:**
- ⚠️ **Seviye Koruması Eski**: 3 seviye farkı yeni güç sistemi ile uyumsuz → Hibrit koruma (Güç + Seviye)
- ⚠️ **Seviye Bonusları Yok**: Yüksek seviye klanlar için ekstra avantajlar yok → Seviye bazlı bonuslar
- ⚠️ **Para Sistemi**: Para sistemi var → Item-based ekonomi (para yok)
- ⚠️ **Faiz Sistemi**: Faiz sistemi var → Kaldırılacak (item-based ekonomi)
- ⚠️ **Basit Banka**: Sadece para var → Item-based banka, otomatik maaş, transfer kontratları
- ⚠️ **Karmaşık İşlemler**: Ritüeller yetersiz → Hibrit sistem (GUI + Fiziksel)
- ⚠️ **Sınırlı Rütbe Yetkileri**: Rütbeler sadece temel yetkilere sahip → Detaylı yetki sistemi
- ⚠️ **Bireysel Görev Yok**: Sadece klan görevleri var → Lonca sistemi ekle
- ⚠️ **Basit Kaynak Paylaşımı**: Yetkiye göre detaylandırılmamış → Rütbe bazlı erişim

**Önerilen İyileştirmeler:**
- ✅ **Hibrit İşlem Sistemi** (Karmaşık işlemler için GUI + Özel Yapılar/Itemler) - EN ÖNCELİKLİ
- ✅ **Item-Based Ekonomi Sistemi** (Para yok, sadece itemler) - EN ÖNCELİKLİ
- ✅ **Hibrit Koruma Sistemi** (Güç + Seviye + Aktivite) - EN ÖNCELİKLİ
- ✅ **Seviye bazlı bonuslar** (Sınır yok, sadece avantajlar) - Özgürlük felsefesi
- ✅ **Otomatik Transfer Kontratları** (Elle uğraşma yok) - Özgürlük felsefesi
- ✅ Gelişmiş rütbe sistemi (daha iyi yönetim)
- ✅ İkili görev sistemi (Bireysel + Klan) - Motivasyon
- ✅ Yetkiye göre kaynak paylaşımı (dengeli ekonomi, spam önleme)
- ✅ Özel Sandık Sistemi (Offline oyuncular için)
- ✅ Aktivite takibi (aktif üyeleri ödüllendir)

**Özgürlük Felsefesi:**
- ✅ **Sınır Yok**: Klanlar istediği kadar üye, toprak, yapı sahibi olabilir
- ✅ **Bonus Var**: Yüksek seviye klanlar ekstra avantajlar kazanır
- ✅ **Büyüme Dostu**: Sunucu büyüdükçe (3000+ kişi) sınırlar sorun yaratmaz

---

## 🚀 UYGULAMA PLANI

### Adım 1: GameBalanceConfig Güncellemesi
- Klan seviye bonusları (limitler değil, bonuslar)
- Banka ayarları
- Aktivite ayarları
- Başkent ayarları

### Adım 2: Yeni Manager Sınıfları
- `HibritInteractionSystem.java` - Hibrit işlem sistemi (GUI + Fiziksel)
- `AllianceContractItemSystem.java` - İttifak/Kontrat kağıt sistemi
- `ClanManagementCenter.java` - Klan yönetim merkezi (GUI)
- `QuestGuildGUI.java` - Görev loncası GUI
- `ItemBasedClanBank.java` - Item-based klan bankası
- `ItemBasedSalarySystem.java` - Item-based maaş sistemi
- `AutomaticTransferContractSystem.java` - Otomatik transfer kontratları
- `PlayerVaultSystem.java` - Özel sandık sistemi
- `AdvancedMarketSystem.java` - Gelişmiş market sistemi (Klan, İttifak, Genel)
- `PlayerTradeSystem.java` - Bireysel ticaret sistemi
- `ClanBankGUI.java` - Klan bankası GUI
- `ClanLevelBonuses.java` - Seviye bazlı bonuslar (sınır yok)
- `ItemBasedClanLevelRewards.java` - Item-based seviye ödülleri
- `AdvancedRankSystem.java` - Gelişmiş rütbe sistemi
- `ClanQuestSystem.java` - Klan görevleri
- `IndividualQuestSystem.java` - Bireysel görevler (Lonca)
- `ClanStatisticsSystem.java` - İstatistikler
- `ClanActivitySystem.java` - Aktivite takibi

### Adım 3: Listener Güncellemeleri
- `ClanChatListener.java` - Gelişmiş chat
- `ClanInteractionListener.java` - Yeni fiziksel etkileşimler
- `WarListener.java` - Yeni savaş türleri

### Adım 4: Config.yml Güncellemesi
- Tüm yeni değerler config'e eklenir
- Açıklamalar ve önerilen aralıklar

---

---

## 📜 KONTRATLAR - DETAYLI SİSTEM TASARIMI

### 🎯 Problem Analizi

**Karmaşık İşlemler İçin Gereken Veriler:**

1. **Kontratlar:**
   - Kontrat türü seçimi (6+ tip)
   - Hedef belirleme (oyuncu, klan, bölge)
   - Ödül ve ceza miktarları (item-based)
   - Süre belirleme (gün, saat, dakika)
   - Bölge seçimi (koordinat, yarıçap)
   - Ceza türü seçimi (hain damgası, tazminat, can kaybı)
   - İptal koşulları
   - Özel şartlar (metin girişi)

2. **Görevler:**
   - Görev türü seçimi
   - Hedef miktar belirleme
   - Ödül belirleme (itemler, XP)
   - Süre belirleme
   - Kimler için geçerli (rütbe bazlı)

3. **Banka İşlemleri:**
   - Otomatik maaş ayarları (rütbe bazlı)
   - Item türü ve miktarı
   - Dağıtım sıklığı
   - Transfer kontratları (kaynak, hedef, sıklık)

4. **Market:**
   - Fiyat belirleme (item-based)
   - Stok miktarı
   - Teklif kabul edilebilir mi?
   - Klan içi indirim oranı

**Sorun:** Basit GUI menüleri bu kadar karmaşık veri girişi için yetersiz!

---

### 🔍 Araştırma Sonuçları

**Minecraft Plugin'lerinde Karmaşık Veri Girişi Çözümleri:**

1. **Multi-Step Form Wizard (En Yaygın Çözüm)**
   - Karta PlayerContract: Adım adım form sistemi
   - GigHub: Wizard-based contract creation
   - Her adımda tek bir konuya odaklanma
   - İlerleme göstergesi
   - Geri/İleri butonları

2. **Hybrid Sistem (GUI + Chat Input)**
   - Basit seçimler GUI'de
   - Karmaşık veriler chat'te (komut veya mesaj)
   - Örnek: `/contract create` → GUI açılır → Chat'te koordinat gir

3. **Anvil GUI (Metin Girişi İçin)**
   - Anvil GUI kullanarak metin girişi
   - Sayı girişi için özel GUI
   - Koordinat girişi için 3 ayrı anvil

4. **Sign-Based Input (Fiziksel)**
   - Tabela üzerine yazma
   - Sistem otomatik okur
   - Fiziksel etkileşim korunur

**Önerilen Çözüm: Multi-Step Form Wizard + Hybrid Sistem**

---

### 💡 Çözüm: Multi-Step Form Wizard Sistemi

#### Genel Mimarisi

**Prensip:**
- Her karmaşık işlem için **adım adım form wizard**
- Her adımda **tek bir konuya odaklanma**
- **İlerleme göstergesi** (1/5, 2/5, vb.)
- **Geri/İleri butonları**
- **Önizleme ekranı** (tüm bilgileri göster)
- **Onay ekranı** (son kontrol)

**Fiziksel Etkileşim:**
- Özel item (Kağıt) veya Özel Yapı ile başlatılır
- GUI wizard açılır
- Her adımda fiziksel etkileşim korunur

---

### 📋 KONTRAT OLUŞTURMA WIZARD (Detaylı)

#### Adım 1: Kontrat Türü Seçimi

**GUI (27 Slot):**
```
Slot 10: MATERIAL_DELIVERY (Malzeme Temini)
  - Icon: Material (Iron Ingot)
  - Lore: "Malzeme teslim kontratı"

Slot 12: PLAYER_KILL (Bounty)
  - Icon: Player Head
  - Lore: "Oyuncu öldürme kontratı"

Slot 14: TERRITORY_RESTRICT (Bölge Yasağı)
  - Icon: Barrier
  - Lore: "Bölge yasağı kontratı"

Slot 16: NON_AGGRESSION (Saldırmama)
  - Icon: Shield
  - Lore: "Saldırmama anlaşması"

Slot 18: BASE_PROTECTION (Base Koruma)
  - Icon: Chest
  - Lore: "Base koruma kontratı"

Slot 20: STRUCTURE_BUILD (Yapı İnşa)
  - Icon: Structure Block
  - Lore: "Yapı inşa kontratı"

Slot 22: İleri Butonu (Yeşil)
Slot 26: İptal Butonu (Kırmızı)
```

**Kullanıcı:** Kontrat türünü seçer (tıklar)

---

#### Adım 2: Kapsam Seçimi (Scope)

**GUI (27 Slot):**
```
Slot 10: PLAYER_TO_PLAYER
  - Icon: 2x Player Head
  - Lore: "Oyuncu → Oyuncu"

Slot 12: CLAN_TO_CLAN
  - Icon: 2x Banner
  - Lore: "Klan → Klan"

Slot 14: PLAYER_TO_CLAN
  - Icon: Player Head + Banner
  - Lore: "Oyuncu → Klan"

Slot 16: CLAN_TO_PLAYER
  - Icon: Banner + Player Head
  - Lore: "Klan → Oyuncu"

Slot 19: Geri Butonu
Slot 22: İleri Butonu
Slot 26: İptal Butonu
```

**Kullanıcı:** Kapsamı seçer

---

#### Adım 3: Hedef Belirleme (Tür'e Göre Değişir)

**A. MATERIAL_DELIVERY için:**
```
GUI (27 Slot):
Slot 10-16: Item Seçimi (Envanterden)
  - "Envanterinden item seç" butonu
  - Seçilen item gösterilir

Slot 19: Miktar Girişi (Anvil GUI)
  - "Miktar: 64" (tıklayınca Anvil açılır)
  - Anvil'de sayı girilir

Slot 22: İleri Butonu
Slot 26: Geri Butonu
```

**B. TERRITORY_RESTRICT için:**
```
GUI (27 Slot):
Slot 10: Bölge Ekle Butonu
  - "Bölge Ekle: Mevcut konumunuzu seçin"
  - Oyuncu bulunduğu yerde Shift + Sağ Tık yapar
  - Koordinat eklenir

Slot 12-16: Eklenen Bölgeler Listesi
  - Her bölge için: Koordinat + Yarıçap
  - Sil butonu

Slot 19: Yarıçap Ayarlama (Anvil GUI)
  - "Yarıçap: 50 blok" (tıklayınca Anvil)

Slot 22: İleri Butonu
Slot 26: Geri Butonu
```

**C. PLAYER_KILL için:**
```
GUI (27 Slot):
Slot 10: Hedef Oyuncu Seçimi
  - "Hedef Oyuncu: Tıkla ve seç"
  - Tıklayınca online oyuncular listesi açılır
  - Oyuncu seçilir

Slot 22: İleri Butonu
Slot 26: Geri Butonu
```

**D. NON_AGGRESSION için:**
```
GUI (27 Slot):
Slot 10: Hedef Seçimi
  - "Hedef: Oyuncu mı, Klan mı?"
  - Alt menü: Oyuncu / Klan seçimi
  - Seçime göre oyuncu/klan listesi

Slot 22: İleri Butonu
Slot 26: Geri Butonu
```

**E. BASE_PROTECTION için:**
```
GUI (27 Slot):
Slot 10: Korunacak Base Seçimi
  - "Base Seç: Mevcut konumunuzu seçin"
  - Oyuncu base'in merkezine gider
  - Shift + Sağ Tık (yere)
  - Koordinat eklenir

Slot 12: Koruma Yarıçapı (Anvil GUI)
  - "Yarıçap: 50 blok"

Slot 14: Koruma Süresi (Anvil GUI)
  - "Süre: 24 saat"

Slot 22: İleri Butonu
Slot 26: Geri Butonu
```

**F. STRUCTURE_BUILD için:**
```
GUI (27 Slot):
Slot 10: Yapı Türü Seçimi
  - "Yapı Türü: Tıkla ve seç"
  - Tıklayınca yapı türleri listesi açılır
  - Yapı seçilir

Slot 12: İnşa Lokasyonu
  - "Lokasyon: Mevcut konumunuzu seçin"
  - Shift + Sağ Tık (yere)
  - Koordinat eklenir

Slot 22: İleri Butonu
Slot 26: Geri Butonu
```

---

#### Adım 4: Ödül ve Ceza Belirleme (Item-Based)

**GUI (54 Slot):**
```
Üst Kısım (0-26): Ödül Itemleri
Slot 10: Ödül Item Ekle Butonu
  - "Envanterinden ödül itemi seç"
  - Seçilen itemler listelenir

Slot 19: Ödül Miktarı (Anvil GUI)
  - "Miktar: 10x" (her item için)

Alt Kısım (27-53): Ceza Itemleri
Slot 37: Ceza Item Ekle Butonu
  - "İhlal durumunda alınacak itemler"

Slot 46: Ceza Miktarı (Anvil GUI)

Slot 49: İleri Butonu
Slot 53: Geri Butonu
```

**Özel: Ceza Türü Seçimi:**
```
Slot 40-44: Ceza Türleri (Checkbox benzeri)
Slot 40: Hain Damgası (Traitor Tag)
  - Icon: Red Dye (seçiliyse Green Dye)
  - Lore: "7 gün boyunca [HAİN] etiketi"

Slot 41: Tazminat (Item Çekme)
  - Icon: Gold Ingot
  - Lore: "Otomatik item çekme"

Slot 42: Kalıcı Can Kaybı
  - Icon: Redstone
  - Lore: "-2 kalp kalıcı can kaybı"

Slot 43: Envanter Kilidi
  - Icon: Iron Bars
  - Lore: "Borç bitene kadar kilitli"
```

---

#### Adım 5: Süre ve Koşullar

**GUI (27 Slot):**
```
Slot 10: Süre Türü
  - "Süre: Gün / Saat / Dakika / Sınırsız"
  - Tıklayınca seçim menüsü

Slot 12: Süre Miktarı (Anvil GUI)
  - "Miktar: 7" (gün/saat/dakika)

Slot 14: İptal Koşulları
  - "İptal: Mümkün / İmkansız"
  - Tıklayınca seçim

Slot 16: Özel Şartlar (Anvil GUI - Metin)
  - "Özel Şartlar: (Opsiyonel)"
  - Tıklayınca Anvil'de metin girilir

Slot 22: İleri Butonu (Önizleme)
Slot 26: Geri Butonu
```

---

#### Adım 6: Önizleme ve Onay

**GUI (54 Slot):**
```
Üst Kısım (0-26): Kontrat Özeti
Slot 4: Kontrat Türü
Slot 13: Tüm Detaylar (Kitap)
  - Kontrat türü
  - Kapsam
  - Hedef
  - Ödüller
  - Ceza türleri
  - Süre
  - Özel şartlar

Alt Kısım (27-53): Onay Butonları
Slot 31: Kontrat Kağıdı Oluştur (Yeşil)
  - "Kağıt oluştur ve mühürle"
  - Kağıt envantere eklenir

Slot 35: Geri Butonu (Düzenle)
Slot 40: İptal Butonu (Kırmızı)
```

**Kullanıcı:** Tüm bilgileri kontrol eder, onaylar

---

### 🎮 KULLANIM AKIŞI (Örnek Senaryo)

**Senaryo: "Şu yere bir daha gitmeyeceksin" Kontratı**

```
1. Kontrat Kağıdı Craft Et (Kağıt + Demir + Mürekkep)

2. Kağıda Sağ Tık → Wizard Başlar

3. Adım 1: TERRITORY_RESTRICT seç

4. Adım 2: PLAYER_TO_PLAYER seç

5. Adım 3: 
   - "Bölge Ekle" butonuna tıkla
   - Yasak bölgeye git
   - Shift + Sağ Tık (yere)
   - Koordinat eklenir
   - Yarıçap: 50 blok (Anvil'de gir)

6. Adım 4:
   - Ödül: 10x Elmas
   - Ceza: Hain Damgası + Tazminat (20x Elmas)

7. Adım 5:
   - Süre: 30 gün
   - İptal: İmkansız
   - Özel Şart: "X koordinatına 50 blok yaklaşma"

8. Adım 6: Önizleme
   - Tüm bilgileri kontrol et
   - "Kontrat Kağıdı Oluştur" butonuna tıkla

9. Kağıt Envantere Eklenir
   - NBT tag'lerde tüm bilgiler saklanır
   - Kağıdı mühürle (Shift + Sağ Tık)
   - Hedef oyuncuya ver
   - Hedef oyuncu da mühürler
   - Kontrat aktif olur!
```

---

### 🔧 TEKNİK UYGULAMA

#### Wizard State Management

```java
public class ContractWizardSystem {
    public class ContractWizardState {
        private UUID wizardId;
        private UUID playerId;
        private int currentStep;
        private Contract.ContractType selectedType;
        private Contract.ContractScope selectedScope;
        private Map<String, Object> contractData; // Tüm veriler
        private boolean isActive;
    }
    
    // Wizard state'leri sakla
    private final Map<UUID, ContractWizardState> activeWizards = new ConcurrentHashMap<>();
    
    /**
     * Wizard başlat
     */
    public void startWizard(Player player, ItemStack contractPaper) {
        ContractWizardState state = new ContractWizardState();
        state.setWizardId(UUID.randomUUID());
        state.setPlayerId(player.getUniqueId());
        state.setCurrentStep(1);
        state.setActive(true);
        
        activeWizards.put(player.getUniqueId(), state);
        
        // İlk adımı aç
        openStep1(player, state);
    }
    
    /**
     * Adım 1: Kontrat Türü
     */
    private void openStep1(Player player, ContractWizardState state) {
        Inventory gui = Bukkit.createInventory(null, 27, 
            Component.text("§6§lKontrat Oluştur - Adım 1/6"));
        
        // Kontrat türleri
        ItemStack materialDelivery = new ItemStack(Material.IRON_INGOT);
        ItemMeta meta = materialDelivery.getItemMeta();
        meta.setDisplayName("§eMalzeme Temini");
        meta.setLore(Arrays.asList("§7Malzeme teslim kontratı"));
        materialDelivery.setItemMeta(meta);
        gui.setItem(10, materialDelivery);
        
        // ... diğer türler
        
        // İlerleme göstergesi
        ItemStack progress = new ItemStack(Material.PAPER);
        ItemMeta progMeta = progress.getItemMeta();
        progMeta.setDisplayName("§7İlerleme: 1/6");
        progress.setItemMeta(progMeta);
        gui.setItem(4, progress);
        
        player.openInventory(gui);
    }
    
    /**
     * Adım geçişi
     */
    public void nextStep(Player player) {
        ContractWizardState state = activeWizards.get(player.getUniqueId());
        if (state == null) return;
        
        int nextStep = state.getCurrentStep() + 1;
        if (nextStep > 6) {
            // Wizard tamamlandı
            completeWizard(player, state);
            return;
        }
        
        state.setCurrentStep(nextStep);
        openStep(player, state, nextStep);
    }
    
    /**
     * Wizard tamamlandı
     */
    private void completeWizard(Player player, ContractWizardState state) {
        // Kontrat oluştur
        Contract contract = createContractFromState(state);
        
        // Kağıda NBT tag ekle
        ItemStack paper = getContractPaper(player);
        NBTItem nbtItem = new NBTItem(paper);
        nbtItem.setString("contract-id", contract.getId().toString());
        nbtItem.setCompound("contract-data", contractToNBT(contract));
        
        // Kağıdı envantere ekle
        player.getInventory().addItem(nbtItem.getItem());
        
        player.sendMessage("§aKontrat kağıdı oluşturuldu! Mühürlemek için Shift + Sağ Tık yapın.");
        
        // Wizard'ı temizle
        activeWizards.remove(player.getUniqueId());
    }
}
```

#### Mühürleme ve İptal Mekanizması

**Mühürleme:**
```java
public class ContractSealingSystem {
    /**
     * Kağıdı mühürle (Shift + Sağ Tık)
     */
    @EventHandler
    public void onPaperSeal(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && 
            event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!event.getPlayer().isSneaking()) return;
        
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.PAPER) return;
        
        NBTItem nbtItem = new NBTItem(item);
        if (!nbtItem.hasKey("contract-id")) return;
        
        UUID contractId = UUID.fromString(nbtItem.getString("contract-id"));
        Contract contract = contractManager.getContract(contractId);
        if (contract == null) return;
        
        UUID playerId = event.getPlayer().getUniqueId();
        
        // İlk mühür (oluşturan)
        if (contract.getIssuer().equals(playerId) && !nbtItem.getBoolean("issuer-sealed")) {
            nbtItem.setBoolean("issuer-sealed", true);
            item.setItemMeta(nbtItem.getItem().getItemMeta());
            event.getPlayer().sendMessage("§aKontrat mühürlendi! Hedef oyuncuya verin.");
            return;
        }
        
        // İkinci mühür (kabul eden)
        if (contract.getAcceptor() != null && contract.getAcceptor().equals(playerId) && 
            !nbtItem.getBoolean("acceptor-sealed")) {
            nbtItem.setBoolean("acceptor-sealed", true);
            item.setItemMeta(nbtItem.getItem().getItemMeta());
            
            // Her iki taraf da mühürledi, kontrat aktif!
            if (nbtItem.getBoolean("issuer-sealed") && nbtItem.getBoolean("acceptor-sealed")) {
                activateContract(contract);
                event.getPlayer().sendMessage("§a§lKONTRAT AKTİF OLDU!");
                event.getPlayer().sendTitle("§a§lKONTRAT AKTİF", "Kan imzası gerekli!", 10, 70, 20);
            }
        }
    }
    
    /**
     * Kontrat aktif et (kan imzası)
     */
    private void activateContract(Contract contract) {
        // Kan imzası: Her iki taraf -3 kalp can kaybeder
        Player issuer = Bukkit.getPlayer(contract.getIssuer());
        Player acceptor = Bukkit.getPlayer(contract.getAcceptor());
        
        if (issuer != null && issuer.isOnline()) {
            applyBloodSignature(issuer);
        }
        
        if (acceptor != null && acceptor.isOnline()) {
            applyBloodSignature(acceptor);
        }
        
        // Kontrat aktif
        contract.setActive(true);
        
        // Broadcast
        Bukkit.broadcastMessage("§6§lKONTRAT İMZALANDI! #" + contract.getId().toString().substring(0, 8));
    }
    
    /**
     * Kan imzası uygula (-3 kalp)
     */
    private void applyBloodSignature(Player player) {
        // Kalıcı can kaybı: -3 kalp (6 can)
        applyPermanentHealthLoss(player, 3);
        
        // Partikül efekti
        player.getLocation().getWorld().spawnParticle(
            Particle.BLOOD, player.getLocation(), 50, 1, 1, 1, 0.1);
        
        player.sendMessage("§4§lKAN İMZASI!");
        player.sendMessage("§c-3 kalp can kaybettiniz (kalıcı)");
    }
}
```

**İptal Mekanizması:**
```java
public class ContractCancellationSystem {
    /**
     * Kağıt yakma (Lav veya Ateş'te)
     */
    @EventHandler
    public void onPaperBurn(BlockBurnEvent event) {
        // Kağıt yanıyor mu kontrol et
        // (ItemFrame'deki kağıt veya yerdeki kağıt)
    }
    
    @EventHandler
    public void onItemBurn(EntityCombustEvent event) {
        if (!(event.getEntity() instanceof Item)) return;
        
        Item item = (Item) event.getEntity();
        ItemStack itemStack = item.getItemStack();
        
        if (itemStack.getType() != Material.PAPER) return;
        
        NBTItem nbtItem = new NBTItem(itemStack);
        if (!nbtItem.hasKey("contract-id")) return;
        
        UUID contractId = UUID.fromString(nbtItem.getString("contract-id"));
        Contract contract = contractManager.getContract(contractId);
        if (contract == null) return;
        
        // İptal kontrolü: Her iki tarafın kağıdı aynı yerde yakılıyor mu?
        Location burnLocation = item.getLocation();
        checkContractCancellation(contract, burnLocation);
    }
    
    /**
     * Kontrat iptal kontrolü
     */
    private void checkContractCancellation(Contract contract, Location burnLocation) {
        // Her iki tarafın kağıdı aynı chunk'ta mı?
        // Aynı yerde (5 blok yakınlıkta) yakılıyor mu?
        
        // İptal edilir
        contract.setActive(false);
        contract.setCancelled(true);
        
        // Taraflara bildir
        Player issuer = Bukkit.getPlayer(contract.getIssuer());
        Player acceptor = Bukkit.getPlayer(contract.getAcceptor());
        
        if (issuer != null) {
            issuer.sendMessage("§cKontrat iptal edildi!");
        }
        
        if (acceptor != null) {
            acceptor.sendMessage("§cKontrat iptal edildi!");
        }
    }
}
```

#### İhlal Tespiti (Otomatik Sistemler)

**Bölge Yasağı İhlali:**
```java
@EventHandler
public void onPlayerMove(PlayerMoveEvent event) {
    Player player = event.getPlayer();
    Location to = event.getTo();
    
    // TERRITORY_RESTRICT kontratları kontrol et
    List<Contract> territoryContracts = contractManager.getPlayerContracts(player.getUniqueId())
        .stream()
        .filter(c -> c.getType() == Contract.ContractType.TERRITORY_RESTRICT)
        .filter(c -> c.isActive() && !c.isBreached())
        .collect(Collectors.toList());
    
    for (Contract contract : territoryContracts) {
        for (Location restrictedArea : contract.getRestrictedAreas()) {
            double distance = to.distance(restrictedArea);
            if (distance <= contract.getRestrictedRadius()) {
                // İHLAL!
                contractManager.breachContract(contract, player.getUniqueId(), 
                    "Yasak bölgeye girildi: " + restrictedArea.getBlockX() + ", " + 
                    restrictedArea.getBlockY() + ", " + restrictedArea.getBlockZ());
                return;
            }
        }
    }
}
```

**Saldırmama İhlali:**
```java
@EventHandler
public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Player)) return;
    if (!(event.getEntity() instanceof Player)) return;
    
    Player attacker = (Player) event.getDamager();
    Player victim = (Player) event.getEntity();
    
    // NON_AGGRESSION kontratları kontrol et
    Contract nonAggression = contractManager.getNonAggressionContract(
        attacker.getUniqueId(), victim.getUniqueId());
    
    if (nonAggression != null && nonAggression.isActive()) {
        // İHLAL!
        contractManager.breachContract(nonAggression, attacker.getUniqueId(), 
            "Saldırmama anlaşması ihlal edildi!");
    }
}
```

**Base Koruma İhlali:**
```java
@EventHandler
public void onBlockBreak(BlockBreakEvent event) {
    Player player = event.getPlayer();
    Location blockLoc = event.getBlock().getLocation();
    
    // BASE_PROTECTION kontratları kontrol et
    List<Contract> protectionContracts = contractManager.getActiveContracts()
        .stream()
        .filter(c -> c.getType() == Contract.ContractType.BASE_PROTECTION)
        .filter(c -> c.getAcceptor() != null && c.getAcceptor().equals(player.getUniqueId()))
        .filter(c -> c.isActive() && !c.isBreached())
        .collect(Collectors.toList());
    
    for (Contract contract : protectionContracts) {
        Location protectedBase = contract.getProtectedBase();
        if (protectedBase == null) continue;
        
        double distance = blockLoc.distance(protectedBase);
        if (distance <= contract.getProtectionRadius()) {
            // İHLAL! Base hasar aldı
            contractManager.breachContract(contract, player.getUniqueId(), 
                "Korunan base hasar aldı!");
            return;
        }
    }
}
```

#### Kontrat Ödül/Ceza Sistemi (Item-Based)

**ÖNEMLİ NOT:** Mevcut `Contract.java` sınıfında `reward` ve `penalty` `double` (para) olarak tanımlı. Item-based ekonomiye geçiş için bu alanların item listesi olarak değiştirilmesi gerekiyor.

**Önerilen Değişiklik:**
```java
// Eski (Para-based):
private final double reward; // Ödül (altın)
private final double penalty; // İhlal cezası (altın)

// Yeni (Item-Based):
private final List<ItemStack> rewardItems; // Ödül itemleri
private final List<ItemStack> penaltyItems; // Ceza itemleri
private final List<PenaltyType> penaltyTypes; // Ceza türleri (Hain Damgası, Can Kaybı, vb.)
```

**Kontrat Tamamlandığında:**
```java
public void completeContract(Contract contract) {
    // Item ödülleri ver
    Player acceptor = Bukkit.getPlayer(contract.getAcceptor());
    if (acceptor != null && acceptor.isOnline()) {
        for (ItemStack reward : contract.getRewardItems()) {
            HashMap<Integer, ItemStack> overflow = acceptor.getInventory().addItem(reward);
            if (!overflow.isEmpty()) {
                // Envanter dolu, özel sandığına aktar
                playerVaultSystem.depositToVault(acceptor.getUniqueId(), reward);
            }
        }
        
        // Kan imzası geri ödeme (+1 kalp)
        restorePermanentHealth(acceptor, 1);
        
        acceptor.sendMessage("§a§lSÖZLEŞME TAMAMLANDI!");
        acceptor.sendMessage("§7Ödül: " + contract.getRewardItems().size() + " item");
    }
}
```

**Kontrat İhlal Edildiğinde:**
```java
public void breachContract(Contract contract, UUID violator, String reason) {
    Player violatorPlayer = Bukkit.getPlayer(violator);
    if (violatorPlayer == null || !violatorPlayer.isOnline()) return;
    
    // Ceza türlerini uygula
    for (PenaltyType penaltyType : contract.getPenaltyTypes()) {
        switch (penaltyType) {
            case TRAITOR_TAG:
                applyTraitorTag(violatorPlayer);
                break;
            case PERMANENT_HEALTH_LOSS:
                applyPermanentHealthLoss(violatorPlayer, 2); // -2 kalp
                break;
            case INVENTORY_LOCK:
                applyInventoryLock(violatorPlayer);
                break;
        }
    }
    
    // Item cezaları (tazminat)
    for (ItemStack penalty : contract.getPenaltyItems()) {
        // Oyuncunun envanterinden/bankasından item çek
        withdrawPenaltyItems(violatorPlayer, penalty);
    }
    
    violatorPlayer.sendMessage("§4§lSÖZLEŞME İHLAL EDİLDİ!");
    violatorPlayer.sendMessage("§cSebep: §7" + reason);
}
```

---

### ⚠️ ÖNEMLİ NOTLAR VE UYARILAR

#### 1. Contract.java Güncellemesi Gerekli

**Mevcut Durum:**
- `Contract.java` sınıfında `reward` ve `penalty` `double` (para) olarak tanımlı
- Item-based ekonomiye geçiş için bu alanların güncellenmesi gerekiyor

**Önerilen Değişiklik:**
```java
// Eski:
private final double reward;
private final double penalty;

// Yeni (Item-Based):
private final List<ItemStack> rewardItems;
private final List<ItemStack> penaltyItems;
private final List<PenaltyType> penaltyTypes; // TRAITOR_TAG, HEALTH_LOSS, INVENTORY_LOCK
```

#### 2. ContractManager.java Güncellemesi Gerekli

**Mevcut Durum:**
- `deliverContract` metodunda para transferi var
- `breachContract` metodunda para cezası var

**Güncelleme:**
- Item transferi yapılmalı
- Item cezaları uygulanmalı

#### 3. Geriye Uyumluluk

**Eski Kontratlar:**
- Mevcut kontratlar para-based olabilir
- Migration script gerekebilir
- Veya eski kontratlar için para sistemi korunabilir (geçiş dönemi)

---

### ✅ DÜZELTME ÖZETİ

**Yapılan Düzeltmeler:**

1. ✅ **Kontrat Wizard**: BASE_PROTECTION ve STRUCTURE_BUILD türleri eklendi
2. ✅ **Görev Ödülleri**: Para → Item-based olarak güncellendi
3. ✅ **Klan Görevleri**: Para → Item-based olarak güncellendi
4. ✅ **İstatistikler**: Para → Item-based olarak güncellendi
5. ✅ **Mühürleme Mekanizması**: Detaylı açıklandı
6. ✅ **İptal Mekanizması**: Kağıt yakma detayları eklendi
7. ✅ **İhlal Tespiti**: Otomatik sistemler detaylandırıldı
8. ✅ **Kontrat Ödül/Ceza Sistemi**: Item-based sistem açıklandı
9. ✅ **Contract.java Uyarısı**: Güncelleme gereksinimi belirtildi

**Kalan İşler (Kod Tarafında):**
- ⚠️ `Contract.java` sınıfının item-based'e güncellenmesi
- ⚠️ `ContractManager.java` metodlarının item-based'e güncellenmesi
- ⚠️ Geriye uyumluluk kontrolü

---
```

#### Anvil GUI Entegrasyonu

```java
public class AnvilInputSystem {
    /**
     * Anvil GUI aç (sayı girişi için)
     */
    public void openAnvilInput(Player player, String title, int defaultValue, 
                               Consumer<Integer> callback) {
        AnvilGui gui = new AnvilGui(player);
        gui.setSlot(AnvilGui.Slot.INPUT_LEFT, new ItemStack(Material.PAPER));
        gui.setTitle(title);
        
        gui.setSlot(AnvilGui.Slot.OUTPUT, new ItemStack(Material.EMERALD));
        
        gui.setOnComplete((p, text) -> {
            try {
                int value = Integer.parseInt(text);
                callback.accept(value);
                return AnvilGui.Response.close();
            } catch (NumberFormatException e) {
                p.sendMessage("§cGeçersiz sayı!");
                return AnvilGui.Response.text("Geçersiz!");
            }
        });
        
        gui.open();
    }
    
    /**
     * Anvil GUI aç (metin girişi için)
     */
    public void openAnvilTextInput(Player player, String title, String defaultValue,
                                  Consumer<String> callback) {
        AnvilGui gui = new AnvilGui(player);
        gui.setSlot(AnvilGui.Slot.INPUT_LEFT, new ItemStack(Material.PAPER));
        gui.setTitle(title);
        
        gui.setOnComplete((p, text) -> {
            callback.accept(text);
            return AnvilGui.Response.close();
        });
        
        gui.open();
    }
}
```

#### Bölge Seçimi (Fiziksel)

```java
public class TerritorySelectionSystem {
    /**
     * Bölge seçim modu
     */
    public void enableTerritorySelection(Player player, Consumer<Location> callback) {
        player.sendMessage("§eBölge seçim modu aktif!");
        player.sendMessage("§7Yasak bölgenin merkezine gidin ve Shift + Sağ Tık yapın.");
        
        // Event listener'a ekle
        territorySelectionCallbacks.put(player.getUniqueId(), callback);
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!event.getPlayer().isSneaking()) return;
        
        UUID playerId = event.getPlayer().getUniqueId();
        Consumer<Location> callback = territorySelectionCallbacks.get(playerId);
        
        if (callback != null) {
            Location loc = event.getClickedBlock().getLocation();
            callback.accept(loc);
            territorySelectionCallbacks.remove(playerId);
            
            event.getPlayer().sendMessage("§aBölge seçildi: " + 
                loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
        }
    }
}
```

---

### 📊 DİĞER KARMAŞIK İŞLEMLER İÇİN WIZARD SİSTEMİ

#### Görev Oluşturma Wizard

**Adımlar:**
1. Görev Türü (Güç, Kaynak, Savaş, Ritüel, Yapı)
2. Hedef Miktar (Anvil GUI)
3. Ödüller (Item seçimi)
4. Süre (Günlük/Haftalık/Özel)
5. Kimler İçin (Rütbe bazlı)
6. Önizleme ve Onay

#### Otomatik Maaş Ayarlama Wizard

**Adımlar:**
1. Rütbe Seçimi
2. Maaş Item Türü (Envanterden seç)
3. Maaş Miktarı (Anvil GUI)
4. Dağıtım Sıklığı (Günlük/Haftalık/Aylık)
5. Özel Maaş mı? (Belirli oyuncu için)
6. Önizleme ve Onay

#### Transfer Kontratı Wizard

**Adımlar:**
1. Transfer Türü (Klan→Oyuncu, Oyuncu→Klan, Oyuncu→Oyuncu)
2. Kaynak Seçimi
3. Hedef Seçimi
4. Item Türü ve Miktarı
5. Transfer Sıklığı (Günlük/Haftalık)
6. Önizleme ve Onay

#### Market Oluşturma Wizard

**Adımlar:**
1. Market Türü (Bireysel/Klan/İttifak)
2. Satılacak Item (Envanterden)
3. İstenen Ödeme (Item seçimi)
4. Fiyat (Miktar)
5. Stok Miktarı
6. Teklif Kabul Edilebilir mi?
7. Önizleme ve Onay

---

### ✅ AVANTAJLAR

**Multi-Step Wizard Sisteminin Avantajları:**

1. ✅ **Kullanıcı Dostu**: Her adımda tek bir konuya odaklanma
2. ✅ **Hata Önleme**: Her adımda doğrulama yapılabilir
3. ✅ **Esneklik**: Karmaşık veri girişi mümkün
4. ✅ **Fiziksel Etkileşim**: Bölge seçimi gibi fiziksel işlemler korunur
5. ✅ **Geri Dönüş**: Her adımda geri gidip düzenleme yapılabilir
6. ✅ **Önizleme**: Son adımda tüm bilgileri görme şansı

---

### 🎯 SONUÇ

**Karmaşık İşlemler İçin Çözüm:**
- ✅ **Multi-Step Form Wizard** sistemi
- ✅ **Anvil GUI** ile sayı/metin girişi
- ✅ **Fiziksel Etkileşim** korunur (bölge seçimi)
- ✅ **NBT Tag** ile kağıtlarda veri saklama
- ✅ **Önizleme ve Onay** sistemi

**Tüm Karmaşık İşlemler İçin:**
- Kontratlar → 6 adımlı wizard
- Görevler → 6 adımlı wizard
- Maaş Ayarları → 6 adımlı wizard
- Transfer Kontratları → 6 adımlı wizard
- Market → 7 adımlı wizard

**Kullanıcı Deneyimi:**
- Her adım basit ve anlaşılır
- İlerleme göstergesi
- Geri/İleri butonları
- Son adımda önizleme ve onay

---

# 🏗️ YAPI SİSTEMİ - DETAYLI TASARIM

## 📋 GENEL BAKIŞ

Yapı sistemi, oyunda **3 ana kategoriye** ayrılmıştır:

1. **Klan Yapıları**: Klan arazisinde yapılır, klan gücüne/seviyesine katkı sağlar, 5 seviyeye sahiptir
2. **Klan Dışı - Özel Kullanım**: Klan dışında yapılır, sadece yapan oyuncu ve klanı kullanabilir, güç/seviye vermez
3. **Klan Dışı - Herkese Açık**: Klan dışında yapılır, herkes kullanabilir, güç/seviye vermez

### Yapı Seviye Sistemi

**Güç Puanı (Sadece Klan Yapıları İçin):**
```
- Seviye 1: 100 puan
- Seviye 2: 250 puan
- Seviye 3: 500 puan
- Seviye 4: 1200 puan
- Seviye 5: 2000 puan
```

**Yapı Özellikleri:**
- Seviye arttıkça **boyut büyür**
- Seviye arttıkça **malzeme zorlaşır**
- Seviye arttıkça **işlev güçlenir**
- Klan yapıları **klan seviyesini artırır**

---

## 🏰 1. KLAN YAPILARI

**Özellikler:**
- ⭐ Sadece **klan bölgesi içinde** yapılabilir
- ⭐ **Güç puanı** verir (klan seviyesini artırır)
- ⭐ **5 seviye** sistemi
- ⭐ Seviyeye göre **boyut ve malzeme** artar
- ⭐ **Pasif buff'lar** veya **özel işlevler** sağlar

### Seviye 1 Klan Yapıları

#### 1.1. Simya Kulesi (Alchemy Tower)

**Boyut:**
- Seviye 1: 3x3x5 blok (küçük kule)
- Seviye 2: 4x4x7 blok
- Seviye 3: 5x5x10 blok
- Seviye 4: 6x6x12 blok
- Seviye 5: 7x7x15 blok (dev kule)

**Seviye 1 Malzeme:**
- 50 Taş Bloğu
- 20 Demir Bloğu
- 5 Redstone Bloğu
- 1 Beacon (merkez)
- 4 Torch (köşeler)

**İşlev:**
- Seviye 1: Tüm bataryalar +%10 güç artışı
- Seviye 2: Tüm bataryalar +%20 güç artışı
- Seviye 3: Tüm bataryalar +%35 güç artışı
- Seviye 4: Tüm bataryalar +%50 güç artışı + %25 menzil artışı
- Seviye 5: Tüm bataryalar +%75 güç artışı + %50 menzil artışı + çift atış modu

**Güç Puanı:** 100 (Seviye 1)

**Yapım Tarifi:**
1. Klan bölgesi içinde 3x3 alan temizle
2. Merkeze Beacon yerleştir
3. Etrafına taş bloklar ile kule şekli ver (5 blok yükseklik)
4. Köşelere demir bloklar yerleştir
5. Redstone blokları ile enerji bağlantısı yap
6. Shift+Sağ Tık ile Blueprint kullanarak aktivasyon yap

---

#### 1.2. Gözetleme Kulesi (Watchtower)

**Boyut:**
- Seviye 1: 2x2x8 blok (küçük kule)
- Seviye 2: 3x3x12 blok
- Seviye 3: 3x3x15 blok
- Seviye 4: 4x4x18 blok
- Seviye 5: 5x5x20 blok (dev gözetleme kulesi)

**Seviye 1 Malzeme:**
- 30 Taş Bloğu
- 15 Cam Bloğu
- 10 Demir Bloğu
- 5 Redstone Torch
- 1 Observer (tepe)

**İşlev:**
- Seviye 1: 100 blok menzil → Düşman koordinat bilgisi
- Seviye 2: 150 blok menzil → Koordinat + sayı bilgisi
- Seviye 3: 200 blok menzil → Koordinat + sayı + ekipman bilgisi
- Seviye 4: 250 blok menzil → Tam analiz + yön tahmini
- Seviye 5: 300 blok menzil → Tam analiz + yön + hız + klan bilgisi

**Güç Puanı:** 100 (Seviye 1)

**Yapım Tarifi:**
1. Klan bölgesi içinde yüksek bir nokta seç
2. 2x2 temel at
3. Taş bloklar ile 8 blok yüksekliğinde kule inşa et
4. Üst kısma Observer yerleştir
5. Cam bloklar ile gözetleme pencereleri yap
6. Redstone Torch'lar ile sinyal sistemi kur
7. Shift+Sağ Tık ile Blueprint kullanarak aktivasyon yap

**Mesaj Örneği (Seviye 1):**
```
⚠️ DİKKAT! Kuzey'de düşman tespit edildi!
📍 Konum: X:1234, Z:5678
```

---

### Seviye 2 Klan Yapıları

#### 2.1. Zehir Reaktörü (Poison Reactor)

**Boyut:**
- Seviye 1: 3x3x4 blok
- Seviye 2: 5x5x6 blok
- Seviye 3: 7x7x8 blok
- Seviye 4: 9x9x10 blok
- Seviye 5: 11x11x12 blok (dev reaktör)

**Seviye 2 Malzeme:**
- 100 Taş Bloğu
- 50 Demir Bloğu
- 30 Zehirli Patates (Poisonous Potato)
- 20 Zehir Şişesi (Potion of Poison)
- 10 Redstone Bloğu
- 4 Beacon (köşeler)
- 1 Cauldron (merkez, zehir deposu)

**İşlev:**
- Seviye 1: 20 blok yarıçap → Poison I (sürekli)
- Seviye 2: 30 blok yarıçap → Poison II (sürekli)
- Seviye 3: 40 blok yarıçap → Poison III + Slowness I
- Seviye 4: 50 blok yarıçap → Poison III + Slowness II + Nausea
- Seviye 5: 60 blok yarıçap → Poison IV + Slowness III + Blindness

**Güç Puanı:** 250 (Seviye 2)

**Yapım Tarifi:**
1. Klan bölgesi içinde 5x5 alan temizle
2. Merkeze Cauldron yerleştir
3. Köşelere Beacon'lar yerleştir
4. Zehirli patatesleri ve zehir şişelerini Cauldron'a dök
5. Redstone blokları ile enerji bağlantısı yap
6. Taş ve demir bloklar ile reaktör çerçevesi inşa et
7. Shift+Sağ Tık ile Blueprint kullanarak aktivasyon yap

---

#### 2.2. Enerji Deposu (Energy Vault)

**Boyut:**
- Seviye 1: 3x3x3 blok
- Seviye 2: 5x5x4 blok
- Seviye 3: 7x7x5 blok
- Seviye 4: 9x9x6 blok
- Seviye 5: 11x11x7 blok (dev depo)

**Seviye 2 Malzeme:**
- 80 Taş Bloğu
- 40 Demir Bloğu
- 30 Redstone Bloğu
- 20 Lapis Lazuli Bloğu
- 10 Ender Chest
- 5 Beacon
- 1 Enchanting Table (merkez)

**İşlev:**
- Seviye 1: 1000 enerji kapasitesi, klan üyelerine +%5 hız
- Seviye 2: 2500 enerji kapasitesi, klan üyelerine +%10 hız
- Seviye 3: 5000 enerji kapasitesi, klan üyelerine +%15 hız + Haste I
- Seviye 4: 10000 enerji kapasitesi, klan üyelerine +%20 hız + Haste II
- Seviye 5: 20000 enerji kapasitesi, klan üyelerine +%25 hız + Haste III + Night Vision

**Güç Puanı:** 250 (Seviye 2)

**Yapım Tarifi:**
1. Klan bölgesi içinde 5x5 alan temizle
2. Merkeze Enchanting Table yerleştir
3. Etrafına Ender Chest'ler yerleştir (depolama)
4. Redstone ve Lapis blokları ile enerji ağı kur
5. Beacon'lar ile güçlendirme yap
6. Taş ve demir bloklar ile depo çerçevesi inşa et
7. Shift+Sağ Tık ile Blueprint kullanarak aktivasyon yap

---

### Seviye 3 Klan Yapıları

#### 3.1. Tektonik Sabitleyici (Tectonic Stabilizer)

**Boyut:**
- Seviye 1: 5x5x6 blok
- Seviye 2: 7x7x8 blok
- Seviye 3: 9x9x12 blok
- Seviye 4: 11x11x15 blok
- Seviye 5: 13x13x18 blok (dev sabitleyici)

**Seviye 3 Malzeme:**
- 200 Taş Bloğu
- 100 Obsidyen Bloğu
- 50 Demir Bloğu
- 30 Titanyum Bloğu (özel malzeme)
- 20 Redstone Bloğu
- 10 Beacon
- 5 Ender Crystal
- 1 Nether Star (merkez, felaket koruması)

**İşlev:**
- Seviye 1: %50 felaket hasar azaltma (30 blok yarıçap)
- Seviye 2: %70 felaket hasar azaltma (50 blok yarıçap)
- Seviye 3: %90 felaket hasar azaltma (70 blok yarıçap)
- Seviye 4: %95 felaket hasar azaltma + Golem'i yavaşlatır (90 blok yarıçap)
- Seviye 5: %99 felaket hasar azaltma + Tüm felaketlere karşı koruma (120 blok yarıçap)

**Güç Puanı:** 500 (Seviye 3)

**Gereksinim:** **Tarif Kitabı** (Boss dropu - Tektonik Sabitleyici Tarifi)

**Yapım Tarifi:**
1. Klan bölgesi içinde 9x9 alan temizle (çok büyük!)
2. Merkeze Nether Star yerleştir (özel platform üzerinde)
3. Etrafına Ender Crystal'lar yerleştir (enerji kaynağı)
4. Obsidyen bloklar ile güçlü çerçeve inşa et
5. Titanyum blokları ile stabilizasyon sistemi kur
6. Redstone ve Beacon'lar ile aktif koruma ağı oluştur
7. Shift+Sağ Tık ile Blueprint kullanarak aktivasyon yap

---

#### 3.2. Kuşatma Fabrikası (Siege Factory)

**Boyut:**
- Seviye 1: 5x5x4 blok
- Seviye 2: 7x7x5 blok
- Seviye 3: 10x10x6 blok
- Seviye 4: 12x12x7 blok
- Seviye 5: 15x15x8 blok (dev fabrika)

**Seviye 3 Malzeme:**
- 150 Taş Bloğu
- 80 Demir Bloğu
- 50 Redstone Bloğu
- 30 Titanyum Bloğu
- 20 TNT
- 10 Furnace
- 5 Anvil
- 3 Crafting Table
- 1 Smithing Table (merkez, üretim masası)

**İşlev:**
- Seviye 1: 1 saat = 1 Mancınık üretir
- Seviye 2: 45 dakika = 1 Mancınık + 1 Balista üretir
- Seviye 3: 30 dakika = 1 Mancınık + 1 Balista üretir
- Seviye 4: 20 dakika = 2 Mancınık + 2 Balista üretir
- Seviye 5: 15 dakika = 2 Mancınık + 2 Balista + 1 Trebuchet üretir

**Güç Puanı:** 500 (Seviye 3)

**Yapım Tarifi:**
1. Klan bölgesi içinde 10x10 alan temizle
2. Merkeze Smithing Table yerleştir
3. Etrafına Furnace'lar yerleştir (eritme)
4. Anvil'ler ile şekillendirme istasyonları kur
5. Crafting Table'lar ile montaj alanları oluştur
6. TNT ve Redstone ile otomasyon sistemi kur
7. Titanyum blokları ile güçlendirilmiş çerçeve inşa et
8. Shift+Sağ Tık ile Blueprint kullanarak aktivasyon yap

---

### Seviye 4 Klan Yapıları

#### 4.1. Otomatik Taret Sistemi (Auto Turret Network)

**Boyut:**
- Seviye 1: 3x3x4 blok (tek taret)
- Seviye 2: 5x5x5 blok (2 taret)
- Seviye 3: 7x7x6 blok (4 taret)
- Seviye 4: 9x9x7 blok (6 taret)
- Seviye 5: 11x11x8 blok (8 taret, dev ağ)

**Seviye 4 Malzeme:**
- 200 Taş Bloğu
- 100 Demir Bloğu
- 60 Adamantite Bloğu (özel malzeme)
- 40 Redstone Bloğu
- 30 Dispenser
- 20 Observer
- 10 Beacon
- 5 Ender Crystal
- 1 Hurda Teknolojisi Çekirdeği (merkez, özel item)

**İşlev:**
- Seviye 1: 1 ok/sn (2 kalp hasar), 20 blok menzil
- Seviye 2: 1.5 ok/sn (3 kalp hasar), 30 blok menzil
- Seviye 3: 2 ok/sn (3 kalp hasar) + ateşli ok, 40 blok menzil
- Seviye 4: 2.5 ok/sn (4 kalp hasar) + ateşli ok + zehirli ok, 50 blok menzil
- Seviye 5: 3 ok/sn (5 kalp hasar) + ateşli ok + zehirli ok + patlayıcı ok, 60 blok menzil

**Güç Puanı:** 1200 (Seviye 4)

**Gereksinim:** **Hurda Teknolojisi Çekirdeği** (Felaket enkazından)

**Yapım Tarifi:**
1. Klan bölgesi içinde 9x9 alan temizle
2. Merkeze Hurda Teknolojisi Çekirdeği yerleştir
3. Etrafına Dispenser'lar yerleştir (6 adet, seviye 4 için)
4. Observer'lar ile hedef tespit sistemi kur
5. Ender Crystal'lar ile enerji kaynağı oluştur
6. Adamantite blokları ile güçlendirilmiş platform inşa et
7. Redstone ve Beacon'lar ile otomasyon ağı kur
8. Shift+Sağ Tık ile Blueprint kullanarak aktivasyon yap

---

#### 4.2. Klan Bankası Yapısı (Clan Bank Structure)

**Boyut:**
- Seviye 1: 3x3x3 blok
- Seviye 2: 5x5x4 blok
- Seviye 3: 7x7x5 blok
- Seviye 4: 9x9x6 blok
- Seviye 5: 11x11x7 blok (dev banka)

**Seviye 4 Malzeme:**
- 150 Taş Bloğu
- 80 Demir Bloğu
- 50 Adamantite Bloğu
- 40 Ender Chest
- 30 Shulker Box
- 20 Redstone Bloğu
- 10 Beacon
- 5 Enchanting Table
- 1 Özel Banka Çekirdeği (merkez, özel item)

**İşlev:**
- Seviye 1: 50 slot item depolama, günlük 1000 item çekim limiti
- Seviye 2: 100 slot item depolama, günlük 2500 item çekim limiti
- Seviye 3: 200 slot item depolama, günlük 5000 item çekim limiti
- Seviye 4: 400 slot item depolama, günlük 10000 item çekim limiti + otomatik maaş sistemi
- Seviye 5: 800 slot item depolama, günlük 20000 item çekim limiti + otomatik maaş + transfer kontratları

**Güç Puanı:** 1200 (Seviye 4)

**Yapım Tarifi:**
1. Klan bölgesi içinde 9x9 alan temizle
2. Merkeze Özel Banka Çekirdeği yerleştir
3. Etrafına Ender Chest'ler yerleştir (depolama)
4. Shulker Box'lar ile ek depolama alanları oluştur
5. Enchanting Table ile güvenlik sistemi kur
6. Adamantite blokları ile güçlendirilmiş çerçeve inşa et
7. Redstone ve Beacon'lar ile otomasyon ağı kur
8. Shift+Sağ Tık ile Blueprint kullanarak aktivasyon yap

---

### Seviye 5 Klan Yapıları

#### 5.1. Karanlık Enerji Reaktörü (Dark Energy Reactor)

**Boyut:**
- Seviye 1: 5x5x6 blok
- Seviye 2: 7x7x8 blok
- Seviye 3: 9x9x10 blok
- Seviye 4: 11x11x12 blok
- Seviye 5: 15x15x15 blok (dev reaktör, en büyük yapı!)

**Seviye 5 Malzeme:**
- 500 Taş Bloğu
- 300 Obsidyen Bloğu
- 200 Karanlık Madde Bloğu (özel malzeme, boss dropu)
- 100 Ender Crystal
- 50 Nether Star
- 30 Beacon
- 20 Enchanting Table
- 10 Ender Chest
- 1 Karanlık Enerji Çekirdeği (merkez, en nadir item)

**İşlev:**
- Seviye 1: Tüm klan üyelerine +%10 güç artışı
- Seviye 2: Tüm klan üyelerine +%20 güç artışı + Haste I
- Seviye 3: Tüm klan üyelerine +%30 güç artışı + Haste II + Strength I
- Seviye 4: Tüm klan üyelerine +%40 güç artışı + Haste III + Strength II + Regeneration I
- Seviye 5: Tüm klan üyelerine +%50 güç artışı + Haste III + Strength III + Regeneration II + Resistance I + Night Vision

**Güç Puanı:** 2000 (Seviye 5)

**Gereksinim:** **Karanlık Enerji Çekirdeği** (En güçlü boss dropu)

**Yapım Tarifi:**
1. Klan bölgesi içinde 15x15 alan temizle (EN BÜYÜK YAPI!)
2. Merkeze Karanlık Enerji Çekirdeği yerleştir (özel platform üzerinde)
3. Etrafına Nether Star'lar yerleştir (güç kaynağı)
4. Ender Crystal'lar ile enerji ağı kur
5. Karanlık Madde blokları ile reaktör çerçevesi inşa et
6. Obsidyen bloklar ile güçlendirilmiş koruma katmanı oluştur
7. Enchanting Table'lar ile büyü ağı kur
8. Beacon'lar ile maksimum güçlendirme yap
9. Shift+Sağ Tık ile Blueprint kullanarak aktivasyon yap

---

#### 5.2. Uzay İstasyonu (Space Station)

**Boyut:**
- Seviye 1: 5x5x5 blok
- Seviye 2: 7x7x7 blok
- Seviye 3: 9x9x9 blok
- Seviye 4: 11x11x11 blok
- Seviye 5: 13x13x13 blok (dev istasyon, küp şeklinde)

**Seviye 5 Malzeme:**
- 400 Taş Bloğu
- 250 Demir Bloğu
- 200 Karanlık Madde Bloğu
- 150 Ender Crystal
- 100 Nether Star
- 50 Beacon
- 30 Enchanting Table
- 20 Ender Chest
- 10 Observer
- 1 Uzay İstasyonu Çekirdeği (merkez, en nadir item)

**İşlev:**
- Seviye 1: 500 blok menzil içinde tüm klan üyelerine koordinat bilgisi
- Seviye 2: 1000 blok menzil içinde tüm klan üyelerine koordinat + harita bilgisi
- Seviye 3: 1500 blok menzil içinde tüm klan üyelerine koordinat + harita + düşman tespiti
- Seviye 4: 2000 blok menzil içinde tüm klan üyelerine koordinat + harita + düşman tespiti + teleportasyon noktaları
- Seviye 5: 3000 blok menzil içinde tüm klan üyelerine koordinat + harita + düşman tespiti + teleportasyon + hava durumu kontrolü + felaket erken uyarı sistemi

**Güç Puanı:** 2000 (Seviye 5)

**Gereksinim:** **Uzay İstasyonu Çekirdeği** (En güçlü boss dropu)

**Yapım Tarifi:**
1. Klan bölgesi içinde 13x13 alan temizle (dev küp!)
2. Merkeze Uzay İstasyonu Çekirdeği yerleştir
3. Etrafına Nether Star'lar yerleştir (güç kaynağı)
4. Ender Crystal'lar ile enerji ağı kur
5. Karanlık Madde blokları ile istasyon çerçevesi inşa et
6. Observer'lar ile gözlem sistemi kur
7. Enchanting Table'lar ile büyü ağı oluştur
8. Beacon'lar ile maksimum güçlendirme yap
9. Ender Chest'ler ile veri depolama sistemi kur
10. Shift+Sağ Tık ile Blueprint kullanarak aktivasyon yap

---

## 🏕️ 2. KLAN DIŞI YAPILAR - ÖZEL KULLANIM

**Özellikler:**
- ⭐ **Klan dışında** yapılabilir (spawn/düşman bölgesi hariç)
- ⭐ Sadece **yapan oyuncu + klanı** kullanabilir
- ⭐ **Güç/seviye vermez**
- ⭐ **5 seviye** sistemi
- ⭐ Seviyeye göre **boyut ve malzeme** artar
- ⭐ **Geçici** veya **sınırlı süre** olabilir

### Seviye 1 Klan Dışı - Özel Yapılar

#### 1.1. Şifa Tapınağı (Healing Shrine)

**Boyut:**
- Seviye 1: 3x3x2 blok (küçük tapınak)
- Seviye 2: 4x4x3 blok
- Seviye 3: 5x5x4 blok
- Seviye 4: 6x6x5 blok
- Seviye 5: 7x7x6 blok (büyük tapınak)

**Seviye 1 Malzeme:**
- 20 Ametist Bloğu
- 10 Taş Bloğu
- 4 Beacon
- 1 Enchanting Table (merkez)
- 5 Torch

**İşlev:**
- Seviye 1: 5 blok yarıçap → Regeneration I (içine girenlere)
- Seviye 2: 8 blok yarıçap → Regeneration II
- Seviye 3: 12 blok yarıçap → Regeneration II + Absorption I
- Seviye 4: 16 blok yarıçap → Regeneration III + Absorption II
- Seviye 5: 20 blok yarıçap → Regeneration III + Absorption III + Saturation

**Yakıt:** Coal ile beslenir, 1 saat süre (Seviye 1)

**Yapım Tarifi:**
1. Klan dışında (vahşi alan) 3x3 alan temizle
2. Merkeze Enchanting Table yerleştir
3. Etrafına Ametist blokları yerleştir
4. Köşelere Beacon'lar yerleştir
5. Torch'lar ile aydınlatma yap
6. Shift+Sağ Tık ile Blueprint kullanarak aktivasyon yap
7. Coal ile yakıt ekle (1 saat süre)

---

#### 1.2. Geçici Kale (Temporary Fort)

**Boyut:**
- Seviye 1: 5x5x3 blok
- Seviye 2: 7x7x4 blok
- Seviye 3: 9x9x5 blok
- Seviye 4: 11x11x6 blok
- Seviye 5: 13x13x7 blok

**Seviye 1 Malzeme:**
- 50 Cobblestone
- 20 Taş Bloğu
- 10 Demir Bloğu
- 4 Torch
- 1 Chest (içinde malzeme)

**İşlev:**
- Seviye 1: İçindekilere +Resistance I, 30 dakika sonra otomatik yıkılır
- Seviye 2: İçindekilere +Resistance II, 45 dakika sonra otomatik yıkılır
- Seviye 3: İçindekilere +Resistance II + Regeneration I, 60 dakika sonra otomatik yıkılır
- Seviye 4: İçindekilere +Resistance III + Regeneration II, 90 dakika sonra otomatik yıkılır
- Seviye 5: İçindekilere +Resistance III + Regeneration III + Strength I, 120 dakika sonra otomatik yıkılır

**Yapım Tarifi:**
1. Klan dışında (savaş alanı) 5x5 alan temizle
2. Cobblestone ile kale duvarları inşa et
3. İçine Chest yerleştir (malzeme deposu)
4. Demir bloklar ile güçlendirme yap
5. Torch'lar ile aydınlatma yap
6. Shift+Sağ Tık ile Blueprint kullanarak aktivasyon yap
7. Otomatik yıkılma zamanlayıcısı başlar

---

### Seviye 2 Klan Dışı - Özel Yapılar

#### 2.1. Cephane Deposu (Ammo Cache)

**Boyut:**
- Seviye 1: 2x2x2 blok
- Seviye 2: 3x3x2 blok
- Seviye 3: 4x4x3 blok
- Seviye 4: 5x5x3 blok
- Seviye 5: 6x6x4 blok

**Seviye 2 Malzeme:**
- 30 Taş Bloğu
- 20 Demir Bloğu
- 10 Redstone Bloğu
- 1 Double Chest
- 4 Redstone Torch
- 1 Observer (güvenlik)

**İşlev:**
- Seviye 1: 20 slot depolama, sadece klan üyeleri açabilir, 1 saat sonra patlar
- Seviye 2: 40 slot depolama, sadece klan üyeleri açabilir, 2 saat sonra patlar
- Seviye 3: 60 slot depolama, sadece klan üyeleri açabilir, 3 saat sonra patlar
- Seviye 4: 80 slot depolama, sadece klan üyeleri açabilir, 4 saat sonra patlar
- Seviye 5: 100 slot depolama, sadece klan üyeleri açabilir, 5 saat sonra patlar

**Yapım Tarifi:**
1. Klan dışında (savaş alanı yakını) 3x3 alan temizle
2. Merkeze Double Chest yerleştir
3. Etrafına demir bloklar yerleştir (güvenlik)
4. Redstone Torch'lar ile sinyal sistemi kur
5. Observer ile güvenlik alarmı kur
6. Shift+Sağ Tık ile Blueprint kullanarak aktivasyon yap
7. Otomatik patlama zamanlayıcısı başlar

---

#### 2.2. Sismik Radar (Seismic Sensor)

**Boyut:**
- Seviye 1: 2x2x2 blok
- Seviye 2: 3x3x3 blok
- Seviye 3: 4x4x4 blok
- Seviye 4: 5x5x5 blok
- Seviye 5: 6x6x6 blok

**Seviye 2 Malzeme:**
- 30 Taş Bloğu
- 20 Demir Bloğu
- 15 Redstone Bloğu
- 9 Note Block (sensör)
- 4 Observer
- 1 Redstone Comparator (analiz)

**İşlev:**
- Seviye 1: 30 blok yarıçap → Hareket algılama, koordinat bilgisi
- Seviye 2: 50 blok yarıçap → Hareket algılama, koordinat + sayı bilgisi
- Seviye 3: 80 blok yarıçap → Hareket algılama, koordinat + sayı + yön bilgisi
- Seviye 4: 120 blok yarıçap → Hareket algılama, koordinat + sayı + yön + hız bilgisi
- Seviye 5: 150 blok yarıçap → Hareket algılama, koordinat + sayı + yön + hız + oyuncu/klan bilgisi

**Yapım Tarifi:**
1. Klan dışında (gizli üs çevresi) 3x3 alan temizle
2. Merkeze Redstone Comparator yerleştir
3. Etrafına Note Block'lar yerleştir (sensör ağı)
4. Observer'lar ile hareket tespit sistemi kur
5. Redstone blokları ile sinyal ağı oluştur
6. Shift+Sağ Tık ile Blueprint kullanarak aktivasyon yap

**Mesaj Örneği (Seviye 2):**
```
⚠️ 20 blok uzakta hareket tespit edildi!
📍 Konum: X:1234, Z:5678
👥 Sayı: 3 kişi
```

---

### Seviye 3 Klan Dışı - Özel Yapılar

#### 3.1. Hızlı İyileşme Merkezi (Rapid Healing Center)

**Boyut:**
- Seviye 1: 4x4x3 blok
- Seviye 2: 5x5x4 blok
- Seviye 3: 6x6x5 blok
- Seviye 4: 7x7x6 blok
- Seviye 5: 8x8x7 blok

**Seviye 3 Malzeme:**
- 100 Taş Bloğu
- 50 Demir Bloğu
- 30 Ametist Bloğu
- 20 Beacon
- 10 Enchanting Table
- 5 Ender Chest
- 1 Altın Bloğu (merkez, şifa kaynağı)

**İşlev:**
- Seviye 1: 10 blok yarıçap → Regeneration III, 5 dakika süre
- Seviye 2: 15 blok yarıçap → Regeneration III + Absorption II, 10 dakika süre
- Seviye 3: 20 blok yarıçap → Regeneration III + Absorption III + Saturation, 15 dakika süre
- Seviye 4: 25 blok yarıçap → Regeneration III + Absorption III + Saturation + Resistance I, 20 dakika süre
- Seviye 5: 30 blok yarıçap → Regeneration III + Absorption III + Saturation + Resistance II + Strength I, 30 dakika süre

**Yakıt:** Golden Apple ile beslenir, süre bitince otomatik yıkılır

**Yapım Tarifi:**
1. Klan dışında (savaş alanı) 6x6 alan temizle
2. Merkeze Altın Bloğu yerleştir
3. Etrafına Beacon'lar yerleştir (güçlendirme)
4. Ametist blokları ile şifa ağı kur
5. Enchanting Table'lar ile büyü sistemi oluştur
6. Ender Chest ile malzeme deposu kur
7. Shift+Sağ Tık ile Blueprint kullanarak aktivasyon yap
8. Golden Apple ile yakıt ekle

---

#### 3.2. Gizli Üs Girişi (Hidden Base Entrance)

**Boyut:**
- Seviye 1: 3x3x2 blok
- Seviye 2: 4x4x3 blok
- Seviye 3: 5x5x4 blok
- Seviye 4: 6x6x5 blok
- Seviye 5: 7x7x6 blok

**Seviye 3 Malzeme:**
- 80 Taş Bloğu
- 40 Obsidyen Bloğu
- 30 Redstone Bloğu
- 20 Piston
- 10 Observer
- 5 Ender Chest
- 1 Özel Gizlilik Çekirdeği (merkez, özel item)

**İşlev:**
- Seviye 1: Gizli giriş, sadece klan üyeleri görebilir, basit şifre sistemi
- Seviye 2: Gizli giriş, sadece klan üyeleri görebilir, gelişmiş şifre sistemi
- Seviye 3: Gizli giriş, sadece klan üyeleri görebilir, biyometrik tanıma (oyuncu UUID)
- Seviye 4: Gizli giriş, sadece klan üyeleri görebilir, biyometrik + zaman kısıtlaması
- Seviye 5: Gizli giriş, sadece klan üyeleri görebilir, biyometrik + zaman + konum kısıtlaması + alarm sistemi

**Yapım Tarifi:**
1. Klan dışında (gizli üs) 5x5 alan temizle
2. Merkeze Özel Gizlilik Çekirdeği yerleştir
3. Etrafına Piston'lar yerleştir (gizli kapı mekanizması)
4. Observer'lar ile hareket tespit sistemi kur
5. Redstone blokları ile otomasyon ağı oluştur
6. Obsidyen bloklar ile güçlendirilmiş çerçeve inşa et
7. Shift+Sağ Tık ile Blueprint kullanarak aktivasyon yap

---

### Seviye 4 Klan Dışı - Özel Yapılar

#### 4.1. Mobil Komuta Merkezi (Mobile Command Center)

**Boyut:**
- Seviye 1: 5x5x4 blok
- Seviye 2: 7x7x5 blok
- Seviye 3: 9x9x6 blok
- Seviye 4: 11x11x7 blok
- Seviye 5: 13x13x8 blok

**Seviye 4 Malzeme:**
- 200 Taş Bloğu
- 100 Demir Bloğu
- 60 Adamantite Bloğu
- 40 Redstone Bloğu
- 30 Observer
- 20 Beacon
- 10 Ender Chest
- 5 Enchanting Table
- 1 Komuta Merkezi Çekirdeği (merkez, özel item)

**İşlev:**
- Seviye 1: 200 blok menzil içinde klan üyelerine koordinat paylaşımı
- Seviye 2: 400 blok menzil içinde klan üyelerine koordinat + harita paylaşımı
- Seviye 3: 600 blok menzil içinde klan üyelerine koordinat + harita + düşman tespiti
- Seviye 4: 800 blok menzil içinde klan üyelerine koordinat + harita + düşman tespiti + strateji planlama
- Seviye 5: 1000 blok menzil içinde klan üyelerine koordinat + harita + düşman tespiti + strateji planlama + otomatik uyarı sistemi

**Yapım Tarifi:**
1. Klan dışında (savaş alanı) 11x11 alan temizle
2. Merkeze Komuta Merkezi Çekirdeği yerleştir
3. Etrafına Observer'lar yerleştir (gözlem sistemi)
4. Beacon'lar ile güçlendirme yap
5. Enchanting Table'lar ile büyü ağı kur
6. Adamantite blokları ile güçlendirilmiş çerçeve inşa et
7. Redstone blokları ile iletişim ağı oluştur
8. Shift+Sağ Tık ile Blueprint kullanarak aktivasyon yap

---

#### 4.2. Hızlı Üretim Atölyesi (Rapid Production Workshop)

**Boyut:**
- Seviye 1: 4x4x3 blok
- Seviye 2: 6x6x4 blok
- Seviye 3: 8x8x5 blok
- Seviye 4: 10x10x6 blok
- Seviye 5: 12x12x7 blok

**Seviye 4 Malzeme:**
- 150 Taş Bloğu
- 80 Demir Bloğu
- 50 Adamantite Bloğu
- 30 Furnace
- 20 Crafting Table
- 15 Anvil
- 10 Smithing Table
- 5 Ender Chest
- 1 Üretim Çekirdeği (merkez, özel item)

**İşlev:**
- Seviye 1: %50 daha hızlı üretim (eşya, silah, zırh)
- Seviye 2: %75 daha hızlı üretim
- Seviye 3: %100 daha hızlı üretim + %25 daha az malzeme gereksinimi
- Seviye 4: %150 daha hızlı üretim + %50 daha az malzeme gereksinimi
- Seviye 5: %200 daha hızlı üretim + %75 daha az malzeme gereksinimi + otomatik üretim

**Yakıt:** Coal ile beslenir, 2 saat süre (Seviye 4)

**Yapım Tarifi:**
1. Klan dışında (üretim alanı) 10x10 alan temizle
2. Merkeze Üretim Çekirdeği yerleştir
3. Etrafına Furnace'lar yerleştir (eritme)
4. Crafting Table'lar ile üretim istasyonları kur
5. Anvil'ler ile şekillendirme alanları oluştur
6. Smithing Table'lar ile gelişmiş üretim sistemi kur
7. Adamantite blokları ile güçlendirilmiş çerçeve inşa et
8. Shift+Sağ Tık ile Blueprint kullanarak aktivasyon yap
9. Coal ile yakıt ekle

---

### Seviye 5 Klan Dışı - Özel Yapılar

#### 5.1. Geçici Işınlanma Ağı (Temporary Teleportation Network)

**Boyut:**
- Seviye 1: 3x3x3 blok
- Seviye 2: 4x4x4 blok
- Seviye 3: 5x5x5 blok
- Seviye 4: 6x6x6 blok
- Seviye 5: 7x7x7 blok

**Seviye 5 Malzeme:**
- 200 Taş Bloğu
- 100 Obsidyen Bloğu
- 80 Ender Crystal
- 50 Nether Star
- 30 Enchanting Table
- 20 Beacon
- 10 Ender Chest
- 5 Observer
- 1 Işınlanma Çekirdeği (merkez, en nadir item)

**İşlev:**
- Seviye 1: 500 blok menzil içinde 1 noktaya ışınlanma, 10 dakika bekleme
- Seviye 2: 1000 blok menzil içinde 2 noktaya ışınlanma, 8 dakika bekleme
- Seviye 3: 1500 blok menzil içinde 3 noktaya ışınlanma, 6 dakika bekleme
- Seviye 4: 2000 blok menzil içinde 4 noktaya ışınlanma, 4 dakika bekleme
- Seviye 5: 3000 blok menzil içinde 5 noktaya ışınlanma, 2 dakika bekleme + grup ışınlanma

**Yakıt:** Ender Pearl ile beslenir, 1 saat süre (Seviye 5)

**Yapım Tarifi:**
1. Klan dışında (stratejik nokta) 7x7 alan temizle
2. Merkeze Işınlanma Çekirdeği yerleştir
3. Etrafına Nether Star'lar yerleştir (güç kaynağı)
4. Ender Crystal'lar ile enerji ağı kur
5. Enchanting Table'lar ile büyü sistemi oluştur
6. Observer'lar ile hedef tespit sistemi kur
7. Obsidyen bloklar ile güçlendirilmiş çerçeve inşa et
8. Shift+Sağ Tık ile Blueprint kullanarak aktivasyon yap
9. Ender Pearl ile yakıt ekle

---

#### 5.2. Geçici Güçlendirme Merkezi (Temporary Power Boost Center)

**Boyut:**
- Seviye 1: 4x4x3 blok
- Seviye 2: 5x5x4 blok
- Seviye 3: 6x6x5 blok
- Seviye 4: 7x7x6 blok
- Seviye 5: 8x8x7 blok

**Seviye 5 Malzeme:**
- 250 Taş Bloğu
- 150 Demir Bloğu
- 100 Karanlık Madde Bloğu
- 80 Beacon
- 50 Nether Star
- 30 Enchanting Table
- 20 Ender Crystal
- 10 Ender Chest
- 1 Güçlendirme Çekirdeği (merkez, en nadir item)

**İşlev:**
- Seviye 1: 15 blok yarıçap → Tüm klan üyelerine +Strength I +Haste I, 30 dakika süre
- Seviye 2: 20 blok yarıçap → Tüm klan üyelerine +Strength II +Haste II, 45 dakika süre
- Seviye 3: 25 blok yarıçap → Tüm klan üyelerine +Strength II +Haste III +Resistance I, 60 dakika süre
- Seviye 4: 30 blok yarıçap → Tüm klan üyelerine +Strength III +Haste III +Resistance II +Regeneration I, 90 dakika süre
- Seviye 5: 40 blok yarıçap → Tüm klan üyelerine +Strength III +Haste III +Resistance III +Regeneration II +Night Vision +Absorption III, 120 dakika süre

**Yakıt:** Golden Apple + Nether Star ile beslenir, süre bitince otomatik yıkılır

**Yapım Tarifi:**
1. Klan dışında (savaş alanı) 8x8 alan temizle
2. Merkeze Güçlendirme Çekirdeği yerleştir
3. Etrafına Nether Star'lar yerleştir (güç kaynağı)
4. Beacon'lar ile maksimum güçlendirme yap
5. Karanlık Madde blokları ile güç ağı kur
6. Enchanting Table'lar ile büyü sistemi oluştur
7. Ender Crystal'lar ile enerji bağlantısı yap
8. Shift+Sağ Tık ile Blueprint kullanarak aktivasyon yap
9. Golden Apple + Nether Star ile yakıt ekle

---

## 🌍 3. KLAN DIŞI YAPILAR - HERKESE AÇIK

**Özellikler:**
- ⭐ **Klan dışında** yapılabilir (spawn/düşman bölgesi hariç)
- ⭐ **Herkes kullanabilir** (rakipler bile!)
- ⭐ **Güç/seviye vermez**
- ⭐ **5 seviye** sistemi
- ⭐ Seviyeye göre **boyut ve malzeme** artar
- ⭐ **Sosyal/ekonomik** amaçlı
- ⭐ **Kalıcı** (kırılmaz, korumalı)

### Seviye 1 Klan Dışı - Herkese Açık Yapılar

#### 1.1. Görev Loncası (Quest Guild)

**Boyut:**
- Seviye 1: 3x3x3 blok (küçük totem)
- Seviye 2: 4x4x4 blok
- Seviye 3: 5x5x5 blok
- Seviye 4: 6x6x6 blok
- Seviye 5: 7x7x7 blok (büyük lonca)

**Seviye 1 Malzeme:**
- 20 Taş Bloğu
- 10 Oak Planks
- 5 Torch
- 1 Sign (görev panosu)
- 1 Lectern (merkez, görev kitabı)

**İşlev:**
- Seviye 1: Basit görevler ("64 Odun getir", "10 Zombi öldür"), item ödülleri
- Seviye 2: Orta görevler ("50 Demir getir", "1 Boss öldür"), daha iyi item ödülleri
- Seviye 3: Zor görevler ("100 Elmas getir", "3 Boss öldür"), nadir item ödülleri
- Seviye 4: Çok zor görevler ("200 Adamantite getir", "5 Boss öldür"), çok nadir item ödülleri
- Seviye 5: Efsanevi görevler ("500 Karanlık Madde getir", "10 Boss öldür"), efsanevi item ödülleri

**Yapım Tarifi:**
1. Klan dışında (herkese açık alan) 3x3 alan temizle
2. Merkeze Lectern yerleştir
3. Etrafına taş bloklar ile totem şekli ver
4. Sign ile görev panosu yerleştir
5. Torch'lar ile aydınlatma yap
6. Shift+Sağ Tık ile Blueprint kullanarak aktivasyon yap

---

#### 1.2. Ticaret Platformu (Trade Platform)

**Boyut:**
- Seviye 1: 2x2x1 blok
- Seviye 2: 3x3x2 blok
- Seviye 3: 4x4x3 blok
- Seviye 4: 5x5x4 blok
- Seviye 5: 6x6x5 blok

**Seviye 1 Malzeme:**
- 9 Oak Planks
- 4 Chest
- 1 Lectern (merkez, ticaret kitabı)
- 1 Sign (fiyat panosu)

**İşlev:**
- Seviye 1: Basit ticaret, %10 vergi (yapana gider)
- Seviye 2: Gelişmiş ticaret, %8 vergi
- Seviye 3: Profesyonel ticaret, %6 vergi + otomatik fiyatlandırma
- Seviye 4: Gelişmiş ticaret, %5 vergi + otomatik fiyatlandırma + stok takibi
- Seviye 5: Maksimum ticaret, %3 vergi + otomatik fiyatlandırma + stok takibi + otomatik alım-satım

**Yapım Tarifi:**
1. Klan dışında (ticaret bölgesi) 2x2 alan temizle
2. Merkeze Lectern yerleştir
3. Etrafına Chest'ler yerleştir (depolama)
4. Sign ile fiyat panosu yerleştir
5. Shift+Sağ Tık ile Blueprint kullanarak aktivasyon yap

---

### Seviye 2 Klan Dışı - Herkese Açık Yapılar

#### 2.1. Harita Taşı (Waypoint Stone)

**Boyut:**
- Seviye 1: 1x1x1 blok (küçük taş)
- Seviye 2: 2x2x2 blok
- Seviye 3: 3x3x3 blok
- Seviye 4: 4x4x4 blok
- Seviye 5: 5x5x5 blok (büyük anıt)

**Seviye 2 Malzeme:**
- 20 Taş Bloğu
- 10 Demir Bloğu
- 5 Torch
- 1 Sign (işaret tabelası)
- 1 Beacon (merkez, işaret ışığı)

**İşlev:**
- Seviye 1: Koordinat işaretleyici, haritada görünür (100 blok menzil)
- Seviye 2: Koordinat işaretleyici, haritada görünür (200 blok menzil) + isim gösterimi
- Seviye 3: Koordinat işaretleyici, haritada görünür (300 blok menzil) + isim + açıklama
- Seviye 4: Koordinat işaretleyici, haritada görünür (400 blok menzil) + isim + açıklama + kategori
- Seviye 5: Koordinat işaretleyici, haritada görünür (500 blok menzil) + isim + açıklama + kategori + teleportasyon noktası

**Yapım Tarifi:**
1. Klan dışında (önemli nokta) 2x2 alan temizle
2. Merkeze Beacon yerleştir
3. Etrafına taş bloklar ile anıt şekli ver
4. Sign ile işaret tabelası yerleştir
5. Torch'lar ile aydınlatma yap
6. Shift+Sağ Tık ile Blueprint kullanarak aktivasyon yap

---

#### 2.2. Dinlenme Kampı (Rest Camp)

**Boyut:**
- Seviye 1: 3x3x2 blok
- Seviye 2: 4x4x3 blok
- Seviye 3: 5x5x4 blok
- Seviye 4: 6x6x5 blok
- Seviye 5: 7x7x6 blok

**Seviye 2 Malzeme:**
- 30 Oak Planks
- 20 Taş Bloğu
- 4 Bed
- 1 Campfire (merkez)
- 5 Torch

**İşlev:**
- Seviye 1: Spawn noktası set edebilir (geçici), yemek pişirme, 1 gün süre
- Seviye 2: Spawn noktası set edebilir (geçici), yemek pişirme, Regeneration I, 2 gün süre
- Seviye 3: Spawn noktası set edebilir (geçici), yemek pişirme, Regeneration II, 3 gün süre
- Seviye 4: Spawn noktası set edebilir (geçici), yemek pişirme, Regeneration II + Saturation, 4 gün süre
- Seviye 5: Spawn noktası set edebilir (geçici), yemek pişirme, Regeneration III + Saturation + Absorption I, 5 gün süre

**Yapım Tarifi:**
1. Klan dışında (uzak bölge) 4x4 alan temizle
2. Merkeze Campfire yerleştir
3. Etrafına Bed'ler yerleştir
4. Oak Planks ile kamp çerçevesi inşa et
5. Torch'lar ile aydınlatma yap
6. Shift+Sağ Tık ile Blueprint kullanarak aktivasyon yap

---

### Seviye 3 Klan Dışı - Herkese Açık Yapılar

#### 3.1. Bilgi Panosu (Notice Board)

**Boyut:**
- Seviye 1: 1x2x1 blok (küçük pano)
- Seviye 2: 2x3x1 blok
- Seviye 3: 3x4x1 blok
- Seviye 4: 4x5x1 blok
- Seviye 5: 5x6x1 blok (büyük pano)

**Seviye 3 Malzeme:**
- 50 Oak Planks
- 30 Demir Bloğu
- 20 Sign
- 10 Torch
- 1 Lectern (merkez, mesaj kitabı)

**İşlev:**
- Seviye 1: 5 mesaj sınırı, herkes mesaj yazabilir
- Seviye 2: 10 mesaj sınırı, herkes mesaj yazabilir + tarih gösterimi
- Seviye 3: 20 mesaj sınırı, herkes mesaj yazabilir + tarih + oyuncu ismi
- Seviye 4: 50 mesaj sınırı, herkes mesaj yazabilir + tarih + oyuncu ismi + kategori
- Seviye 5: 100 mesaj sınırı, herkes mesaj yazabilir + tarih + oyuncu ismi + kategori + arama özelliği

**Yapım Tarifi:**
1. Klan dışında (sosyal alan) 3x4 alan temizle
2. Merkeze Lectern yerleştir
3. Etrafına Sign'lar yerleştir (mesaj panoları)
4. Demir bloklar ile çerçeve inşa et
5. Torch'lar ile aydınlatma yap
6. Shift+Sağ Tık ile Blueprint kullanarak aktivasyon yap

---

#### 3.2. Toplu Üretim Atölyesi (Community Workshop)

**Boyut:**
- Seviye 1: 4x4x3 blok
- Seviye 2: 5x5x4 blok
- Seviye 3: 6x6x5 blok
- Seviye 4: 7x7x6 blok
- Seviye 5: 8x8x7 blok

**Seviye 3 Malzeme:**
- 100 Taş Bloğu
- 50 Demir Bloğu
- 30 Furnace
- 20 Crafting Table
- 10 Anvil
- 5 Smithing Table
- 1 Özel Atölye Çekirdeği (merkez, özel item)

**İşlev:**
- Seviye 1: Herkes kullanabilir, %25 daha hızlı üretim
- Seviye 2: Herkes kullanabilir, %50 daha hızlı üretim
- Seviye 3: Herkes kullanabilir, %75 daha hızlı üretim + %10 daha az malzeme gereksinimi
- Seviye 4: Herkes kullanabilir, %100 daha hızlı üretim + %20 daha az malzeme gereksinimi
- Seviye 5: Herkes kullanabilir, %150 daha hızlı üretim + %30 daha az malzeme gereksinimi + otomatik üretim

**Yapım Tarifi:**
1. Klan dışında (toplu üretim alanı) 6x6 alan temizle
2. Merkeze Özel Atölye Çekirdeği yerleştir
3. Etrafına Furnace'lar yerleştir (eritme)
4. Crafting Table'lar ile üretim istasyonları kur
5. Anvil'ler ile şekillendirme alanları oluştur
6. Smithing Table'lar ile gelişmiş üretim sistemi kur
7. Shift+Sağ Tık ile Blueprint kullanarak aktivasyon yap

---

### Seviye 4 Klan Dışı - Herkese Açık Yapılar

#### 4.1. Global Pazar (Global Market)

**Boyut:**
- Seviye 1: 5x5x3 blok
- Seviye 2: 6x6x4 blok
- Seviye 3: 7x7x5 blok
- Seviye 4: 8x8x6 blok
- Seviye 5: 9x9x7 blok

**Seviye 4 Malzeme:**
- 200 Taş Bloğu
- 100 Demir Bloğu
- 60 Adamantite Bloğu
- 40 Ender Chest
- 30 Shulker Box
- 20 Beacon
- 10 Enchanting Table
- 1 Global Pazar Çekirdeği (merkez, özel item)

**İşlev:**
- Seviye 1: Herkes alım-satım yapabilir, %8 vergi, 100 slot depolama
- Seviye 2: Herkes alım-satım yapabilir, %6 vergi, 200 slot depolama
- Seviye 3: Herkes alım-satım yapabilir, %5 vergi, 400 slot depolama + otomatik fiyatlandırma
- Seviye 4: Herkes alım-satım yapabilir, %4 vergi, 800 slot depolama + otomatik fiyatlandırma + stok takibi
- Seviye 5: Herkes alım-satım yapabilir, %3 vergi, 1600 slot depolama + otomatik fiyatlandırma + stok takibi + otomatik alım-satım + pazar analizi

**Yapım Tarifi:**
1. Klan dışında (ticaret merkezi) 8x8 alan temizle
2. Merkeze Global Pazar Çekirdeği yerleştir
3. Etrafına Ender Chest'ler yerleştir (depolama)
4. Shulker Box'lar ile ek depolama alanları oluştur
5. Enchanting Table ile güvenlik sistemi kur
6. Adamantite blokları ile güçlendirilmiş çerçeve inşa et
7. Beacon'lar ile güçlendirme yap
8. Shift+Sağ Tık ile Blueprint kullanarak aktivasyon yap

---

#### 4.2. Toplu İyileşme Merkezi (Community Healing Center)

**Boyut:**
- Seviye 1: 4x4x3 blok
- Seviye 2: 5x5x4 blok
- Seviye 3: 6x6x5 blok
- Seviye 4: 7x7x6 blok
- Seviye 5: 8x8x7 blok

**Seviye 4 Malzeme:**
- 150 Taş Bloğu
- 80 Demir Bloğu
- 50 Ametist Bloğu
- 40 Beacon
- 20 Enchanting Table
- 10 Ender Chest
- 1 Altın Bloğu (merkez, şifa kaynağı)

**İşlev:**
- Seviye 1: Herkes kullanabilir, 10 blok yarıçap → Regeneration I
- Seviye 2: Herkes kullanabilir, 15 blok yarıçap → Regeneration II
- Seviye 3: Herkes kullanabilir, 20 blok yarıçap → Regeneration II + Absorption I
- Seviye 4: Herkes kullanabilir, 25 blok yarıçap → Regeneration III + Absorption II + Saturation
- Seviye 5: Herkes kullanabilir, 30 blok yarıçap → Regeneration III + Absorption III + Saturation + Resistance I

**Yakıt:** Golden Apple ile beslenir, sürekli çalışır (otomatik yenilenir)

**Yapım Tarifi:**
1. Klan dışında (sosyal alan) 7x7 alan temizle
2. Merkeze Altın Bloğu yerleştir
3. Etrafına Beacon'lar yerleştir (güçlendirme)
4. Ametist blokları ile şifa ağı kur
5. Enchanting Table'lar ile büyü sistemi oluştur
6. Ender Chest ile malzeme deposu kur
7. Shift+Sağ Tık ile Blueprint kullanarak aktivasyon yap
8. Golden Apple ile yakıt ekle (otomatik yenilenir)

---

### Seviye 5 Klan Dışı - Herkese Açık Yapılar

#### 5.1. Efsanevi Görev Loncası (Legendary Quest Guild)

**Boyut:**
- Seviye 1: 3x3x3 blok
- Seviye 2: 4x4x4 blok
- Seviye 3: 5x5x5 blok
- Seviye 4: 6x6x6 blok
- Seviye 5: 8x8x8 blok (dev lonca)

**Seviye 5 Malzeme:**
- 300 Taş Bloğu
- 200 Demir Bloğu
- 150 Karanlık Madde Bloğu
- 100 Beacon
- 50 Enchanting Table
- 30 Ender Chest
- 20 Observer
- 10 Nether Star
- 1 Efsanevi Lonca Çekirdeği (merkez, en nadir item)

**İşlev:**
- Seviye 1: Basit görevler, item ödülleri
- Seviye 2: Orta görevler, daha iyi item ödülleri
- Seviye 3: Zor görevler, nadir item ödülleri
- Seviye 4: Çok zor görevler, çok nadir item ödülleri
- Seviye 5: Efsanevi görevler, efsanevi item ödülleri + klan görevleri + günlük görevler + haftalık görevler + özel etkinlik görevleri

**Yapım Tarifi:**
1. Klan dışında (merkezi alan) 8x8 alan temizle
2. Merkeze Efsanevi Lonca Çekirdeği yerleştir
3. Etrafına Nether Star'lar yerleştir (güç kaynağı)
4. Beacon'lar ile maksimum güçlendirme yap
5. Karanlık Madde blokları ile güç ağı kur
6. Enchanting Table'lar ile büyü sistemi oluştur
7. Observer'lar ile görev takip sistemi kur
8. Ender Chest'ler ile ödül deposu oluştur
9. Shift+Sağ Tık ile Blueprint kullanarak aktivasyon yap

---

#### 5.2. Evrensel İletişim Merkezi (Universal Communication Center)

**Boyut:**
- Seviye 1: 4x4x3 blok
- Seviye 2: 5x5x4 blok
- Seviye 3: 6x6x5 blok
- Seviye 4: 7x7x6 blok
- Seviye 5: 9x9x7 blok (dev merkez)

**Seviye 5 Malzeme:**
- 400 Taş Bloğu
- 250 Demir Bloğu
- 200 Karanlık Madde Bloğu
- 150 Ender Crystal
- 100 Nether Star
- 80 Beacon
- 50 Enchanting Table
- 30 Observer
- 20 Ender Chest
- 1 İletişim Çekirdeği (merkez, en nadir item)

**İşlev:**
- Seviye 1: 500 blok menzil içinde mesajlaşma
- Seviye 2: 1000 blok menzil içinde mesajlaşma + koordinat paylaşımı
- Seviye 3: 2000 blok menzil içinde mesajlaşma + koordinat + harita paylaşımı
- Seviye 4: 3000 blok menzil içinde mesajlaşma + koordinat + harita + düşman tespiti paylaşımı
- Seviye 5: 5000 blok menzil içinde mesajlaşma + koordinat + harita + düşman tespiti + strateji planlama + otomatik uyarı sistemi + global duyurular

**Yapım Tarifi:**
1. Klan dışında (merkezi alan) 9x9 alan temizle
2. Merkeze İletişim Çekirdeği yerleştir
3. Etrafına Nether Star'lar yerleştir (güç kaynağı)
4. Ender Crystal'lar ile enerji ağı kur
5. Observer'lar ile iletişim ağı oluştur
6. Karanlık Madde blokları ile güç ağı kur
7. Enchanting Table'lar ile büyü sistemi oluştur
8. Beacon'lar ile maksimum güçlendirme yap
9. Ender Chest'ler ile veri depolama sistemi kur
10. Shift+Sağ Tık ile Blueprint kullanarak aktivasyon yap

---

## 📊 YAPI SİSTEMİ ÖZET TABLOSU

### Klan Yapıları (Güç Puanı Verir)

| Seviye | Güç Puanı | Örnek Yapılar | Boyut (Seviye 1 → 5) |
|--------|-----------|---------------|---------------------|
| 1 | 100 | Simya Kulesi, Gözetleme Kulesi | 3x3x5 → 7x7x15 |
| 2 | 250 | Zehir Reaktörü, Enerji Deposu | 3x3x4 → 11x11x12 |
| 3 | 500 | Tektonik Sabitleyici, Kuşatma Fabrikası | 5x5x6 → 15x15x18 |
| 4 | 1200 | Otomatik Taret Sistemi, Klan Bankası | 3x3x4 → 11x11x8 |
| 5 | 2000 | Karanlık Enerji Reaktörü, Uzay İstasyonu | 5x5x6 → 15x15x15 |

### Klan Dışı - Özel Kullanım (Güç Puanı Vermez)

| Seviye | Örnek Yapılar | Boyut (Seviye 1 → 5) | Süre |
|--------|---------------|---------------------|------|
| 1 | Şifa Tapınağı, Geçici Kale | 3x3x2 → 7x7x6 | 30 dk - 2 saat |
| 2 | Cephane Deposu, Sismik Radar | 2x2x2 → 6x6x6 | 1-5 saat |
| 3 | Hızlı İyileşme Merkezi, Gizli Üs Girişi | 4x4x3 → 8x8x7 | 5-30 dk |
| 4 | Mobil Komuta Merkezi, Hızlı Üretim Atölyesi | 5x5x4 → 13x13x8 | 2 saat |
| 5 | Geçici Işınlanma Ağı, Geçici Güçlendirme Merkezi | 3x3x3 → 8x8x7 | 1-2 saat |

### Klan Dışı - Herkese Açık (Güç Puanı Vermez)

| Seviye | Örnek Yapılar | Boyut (Seviye 1 → 5) | Süre |
|--------|---------------|---------------------|------|
| 1 | Görev Loncası, Ticaret Platformu | 2x2x1 → 7x7x7 | Kalıcı |
| 2 | Harita Taşı, Dinlenme Kampı | 1x1x1 → 7x7x6 | 1-5 gün |
| 3 | Bilgi Panosu, Toplu Üretim Atölyesi | 1x2x1 → 8x8x7 | Kalıcı |
| 4 | Global Pazar, Toplu İyileşme Merkezi | 4x4x3 → 9x9x7 | Kalıcı |
| 5 | Efsanevi Görev Loncası, Evrensel İletişim Merkezi | 3x3x3 → 9x9x7 | Kalıcı |

---

## ⚙️ YAPI AKTİVASYON SİSTEMİ

### Genel Aktivasyon Kuralları

**Tüm Yapılar İçin:**
1. **Blueprint** (Plan) item'ı elinde olmalı
2. Yapı için gerekli tarif kitabı öğrenilmiş olmalı (bazı yapılar için)
3. Yapı deseni doğru kurulmuş olmalı
4. Gerekli malzemeler yerleştirilmiş olmalı
5. **Shift + Sağ Tık** ile merkez bloğa tıklanmalı

**Klan Yapıları İçin Ek Gereksinimler:**
- Klan üyesi olmalı (Recruit hariç)
- Kendi klan bölgesinde olmalı
- Yeterli yetkiye sahip olmalı (yapı türüne göre)

**Klan Dışı Yapılar İçin Ek Gereksinimler:**
- Spawn bölgesinde olmamalı
- Başkasının klan bölgesinde olmamalı
- Vahşi alanlarda olmalı

---

## 🎮 STRATEJİK KULLANIM ÖNERİLERİ

### Klan İçin (Savunma ve Güç)

**Erken Oyun:**
- Seviye 1 Simya Kulesi (batarya gücü)
- Seviye 1 Gözetleme Kulesi (erken uyarı)

**Orta Oyun:**
- Seviye 2-3 Zehir Reaktörü (savunma)
- Seviye 2-3 Enerji Deposu (hız artışı)
- Seviye 3 Tektonik Sabitleyici (felaket koruması)

**Geç Oyun:**
- Seviye 4-5 Otomatik Taret Sistemi (otomatik savunma)
- Seviye 4-5 Klan Bankası (item yönetimi)
- Seviye 5 Karanlık Enerji Reaktörü (maksimum güçlendirme)
- Seviye 5 Uzay İstasyonu (tam kontrol)

### Savaş İçin (Klan Dışı - Özel)

**Hızlı Saldırı:**
- Seviye 1-2 Şifa Tapınağı (iyileşme)
- Seviye 1-2 Geçici Kale (sığınak)
- Seviye 2 Cephane Deposu (malzeme)

**Uzun Savaş:**
- Seviye 3-4 Hızlı İyileşme Merkezi (sürekli iyileşme)
- Seviye 4 Mobil Komuta Merkezi (koordinasyon)
- Seviye 5 Geçici Işınlanma Ağı (hızlı hareket)

### Sosyal İçin (Klan Dışı - Herkese Açık)

**Erken Oyun:**
- Seviye 1 Görev Loncası (görevler)
- Seviye 1 Ticaret Platformu (ticaret)

**Orta Oyun:**
- Seviye 2-3 Harita Taşı (işaretleme)
- Seviye 3 Bilgi Panosu (iletişim)
- Seviye 3 Toplu Üretim Atölyesi (üretim)

**Geç Oyun:**
- Seviye 4-5 Global Pazar (büyük ticaret)
- Seviye 5 Efsanevi Görev Loncası (efsanevi görevler)
- Seviye 5 Evrensel İletişim Merkezi (global iletişim)

---

## 📝 CONFIG.YML ENTEGRASYONU

```yaml
structure-system:
  # Klan Yapıları Güç Puanları
  clan-structures:
    power-points:
      level-1: 100
      level-2: 250
      level-3: 500
      level-4: 1200
      level-5: 2000
    crystal-base: 500  # Klan Kristali sabit bonusu
  
  # Yapı Aktivasyon
  activation:
    require-blueprint: true
    require-recipe: true  # Bazı yapılar için tarif kitabı gerekli
    shift-right-click: true
  
  # Klan Dışı Yapılar
  external-structures:
    # Özel Kullanım Yapıları
    private:
      max-duration-hours: 5  # Maksimum süre (saat)
      auto-destroy: true  # Süre bitince otomatik yıkılır
    
    # Herkese Açık Yapılar
    public:
      protected: true  # Kırılamaz, korumalı
      max-messages: 100  # Bilgi Panosu için
      max-waypoints: 50  # Harita Taşı için
  
  # Yapı Boyutları (Seviye 1-5)
  structure-sizes:
    clan:
      level-1: "3x3x5"
      level-2: "5x5x7"
      level-3: "9x9x12"
      level-4: "11x11x15"
      level-5: "15x15x18"
    external:
      level-1: "3x3x3"
      level-2: "5x5x5"
      level-3: "7x7x7"
      level-4: "9x9x9"
      level-5: "11x11x11"
```

---

# 🔍 EKSİK ÖZELLİKLER VE DERİNLEŞTİRME GEREKTİREN SİSTEMLER

## 📋 GENEL BAKIŞ

Bu bölümde, dokümanın diğer kısımlarında yüzeysel olarak bahsedilen veya hiç bahsedilmeyen özelliklerin detaylı tasarımları yer almaktadır.

---

## 🎯 1. KLAN ÇETESİ SİSTEMİ ⭐ **YENİ ÖZELLİK**

### Genel Bakış

Klan çetesi, klan üyelerinin birlikte görev yapabileceği, özel bonuslar alabileceği ve ortak hedeflere ulaşabileceği bir sistemdir.

**Özellikler:**
- Klan içinde çete oluşturulabilir (maksimum 5 üye)
- Çete üyeleri birlikte görev yapabilir
- Çete bonusları: Birlikte savaşırken +%15 hasar, birlikte görev yaparken +%20 ödül
- Çete lideri çeteyi yönetebilir

**Fiziksel Etkileşim:**
```
1. Çete Oluşturma:
   - Klan Yönetim Merkezi'ne git
   - "Çete Yönetimi" butonuna tıkla
   - "Yeni Çete Oluştur" seçeneğini seç
   - Çete ismi belirle
   - Üyeleri davet et (maksimum 5 üye)

2. Çete Daveti:
   - Çete lideri üyeye davet gönderir
   - Üye daveti kabul eder veya reddeder
   - Davet 24 saat geçerlidir

3. Çete Görevleri:
   - Çete üyeleri birlikte görev alabilir
   - Görev tamamlandığında tüm üyelere ödül verilir
   - Bonus: +%20 ödül (çete bonusu)
```

**Teknik Uygulama:**
```java
public class ClanGangSystem {
    public class Gang {
        private UUID gangId;
        private UUID clanId;
        private String name;
        private UUID leaderId;
        private List<UUID> members; // Maksimum 5 üye
        private Map<UUID, Long> joinTime; // Üye -> Katılma zamanı
        private int completedQuests; // Tamamlanan görevler
        private double totalDamageDealt; // Toplam hasar (çete bonusu için)
    }
    
    /**
     * Çete oluştur
     */
    public boolean createGang(Player leader, String name) {
        Clan clan = clanManager.getClanByPlayer(leader.getUniqueId());
        if (clan == null) return false;
        
        // Maksimum çete sayısı kontrolü (klan başına 10 çete)
        int gangCount = getClanGangCount(clan.getId());
        if (gangCount >= 10) {
            leader.sendMessage("§cKlanınız maksimum çete sayısına ulaştı!");
            return false;
        }
        
        Gang gang = new Gang();
        gang.setGangId(UUID.randomUUID());
        gang.setClanId(clan.getId());
        gang.setName(name);
        gang.setLeaderId(leader.getUniqueId());
        gang.setMembers(new ArrayList<>());
        gang.getMembers().add(leader.getUniqueId());
        
        gangs.put(gang.getGangId(), gang);
        
        leader.sendMessage("§aÇete oluşturuldu: §e" + name);
        return true;
    }
    
    /**
     * Çete daveti gönder
     */
    public void sendGangInvite(Gang gang, Player target) {
        Clan clan = clanManager.getClanByPlayer(target.getUniqueId());
        if (clan == null || !clan.getId().equals(gang.getClanId())) {
            return; // Aynı klan üyesi olmalı
        }
        
        if (gang.getMembers().size() >= 5) {
            return; // Maksimum üye sayısı
        }
        
        GangInvite invite = new GangInvite();
        invite.setGangId(gang.getGangId());
        invite.setInviterId(gang.getLeaderId());
        invite.setTargetId(target.getUniqueId());
        invite.setExpiryTime(System.currentTimeMillis() + 86400000L); // 24 saat
        
        pendingInvites.put(target.getUniqueId(), invite);
        
        target.sendMessage("§e" + gang.getName() + " çetesine davet edildiniz!");
        target.sendMessage("§7Daveti kabul etmek için: /gang accept");
    }
    
    /**
     * Çete bonusu (birlikte savaş)
     */
    public double calculateGangDamageBonus(Player attacker, Player target) {
        Gang attackerGang = getPlayerGang(attacker.getUniqueId());
        if (attackerGang == null) return 1.0;
        
        // Çete üyeleri yakında mı? (50 blok yarıçap)
        int nearbyGangMembers = 0;
        for (UUID memberId : attackerGang.getMembers()) {
            if (memberId.equals(attacker.getUniqueId())) continue;
            
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                double distance = attacker.getLocation().distance(member.getLocation());
                if (distance <= 50) {
                    nearbyGangMembers++;
                }
            }
        }
        
        // En az 2 çete üyesi yakındaysa bonus
        if (nearbyGangMembers >= 1) {
            return 1.15; // +%15 hasar
        }
        
        return 1.0;
    }
    
    /**
     * Çete görev bonusu
     */
    public double calculateGangQuestBonus(Gang gang) {
        // Çete üyeleri birlikte görev yapıyorsa +%20 ödül
        return 1.20;
    }
}
```

**Config Ayarları:**
```yaml
gang-system:
  max-members-per-gang: 5
  max-gangs-per-clan: 10
  damage-bonus: 0.15  # +%15 hasar
  quest-bonus: 0.20  # +%20 görev ödülü
  invite-expiry-hours: 24
  nearby-radius: 50  # Bonus için yakınlık yarıçapı (blok)
```

---

## 🏛️ 2. ŞUBE SİSTEMİ ⭐ **YENİ ÖZELLİK**

### Genel Bakış

Büyük klanlar, ana klan bölgesinden uzakta şubeler kurabilir. Şubeler, bağımsız bölgeler olarak çalışır ancak ana klana bağlıdır.

**Özellikler:**
- Ana klan seviyesi 5+ olmalı (şube açabilmek için)
- Şube, ana klan bölgesinden minimum 500 blok uzakta olmalı
- Şube, kendi kristali ve bölgesi olabilir
- Şube üyeleri ana klan üyeleriyle aynı haklara sahiptir
- Şube kaynakları ana klan bankasına aktarılabilir

**Fiziksel Etkileşim:**
```
1. Şube Kurma:
   - Ana klan lideri yeni bölgeye gider (500+ blok uzakta)
   - Şube Kristali craft eder (normal kristal + özel item)
   - Şube bölgesini çitlerle çevreler
   - Şube Kristali'ni yerleştirir
   - Şube aktif olur!

2. Şube Yönetimi:
   - Şube lideri atanır (ana klan lideri tarafından)
   - Şube lideri şube üyelerini yönetebilir
   - Şube kaynakları ana klan bankasına aktarılabilir

3. Şube Kapatma:
   - Ana klan lideri şube kristalini kırar
   - Şube kapanır, kaynaklar ana klana aktarılır
```

**Teknik Uygulama:**
```java
public class ClanBranchSystem {
    public class Branch {
        private UUID branchId;
        private UUID mainClanId;
        private String name;
        private Location location;
        private UUID branchLeaderId;
        private List<UUID> members;
        private Territory territory;
        private boolean isActive;
    }
    
    /**
     * Şube kur
     */
    public boolean createBranch(Player leader, Location location) {
        Clan mainClan = clanManager.getClanByPlayer(leader.getUniqueId());
        if (mainClan == null) return false;
        
        // Ana klan seviyesi kontrolü
        int clanLevel = powerSystem.calculateClanLevel(mainClan);
        if (clanLevel < 5) {
            leader.sendMessage("§cŞube açabilmek için klan seviyesi 5+ olmalı!");
            return false;
        }
        
        // Mesafe kontrolü (500+ blok)
        Location mainClanLocation = mainClan.getCrystalLocation();
        if (mainClanLocation != null) {
            double distance = mainClanLocation.distance(location);
            if (distance < 500) {
                leader.sendMessage("§cŞube, ana klan bölgesinden minimum 500 blok uzakta olmalı!");
                return false;
            }
        }
        
        // Şube oluştur
        Branch branch = new Branch();
        branch.setBranchId(UUID.randomUUID());
        branch.setMainClanId(mainClan.getId());
        branch.setName(mainClan.getName() + " Şubesi");
        branch.setLocation(location);
        branch.setBranchLeaderId(leader.getUniqueId());
        branch.setMembers(new ArrayList<>());
        branch.getMembers().add(leader.getUniqueId());
        branch.setActive(true);
        
        branches.put(branch.getBranchId(), branch);
        
        leader.sendMessage("§aŞube kuruldu: §e" + branch.getName());
        Bukkit.broadcastMessage("§6§l" + mainClan.getName() + " klanı yeni şube açtı!");
        
        return true;
    }
    
    /**
     * Şube kaynaklarını ana klana aktar
     */
    public void transferBranchResourcesToMain(Branch branch) {
        Clan mainClan = clanManager.getClanById(branch.getMainClanId());
        if (mainClan == null) return;
        
        // Şube bankasından ana klan bankasına aktar
        Inventory branchBank = getBranchBank(branch);
        Inventory mainBank = getClanBank(mainClan);
        
        if (branchBank != null && mainBank != null) {
            for (ItemStack item : branchBank.getContents()) {
                if (item != null && item.getType() != Material.AIR) {
                    HashMap<Integer, ItemStack> overflow = mainBank.addItem(item);
                    if (!overflow.isEmpty()) {
                        // Ana klan bankası dolu, uyarı ver
                        notifyClanLeaders(mainClan, "§cŞube kaynakları aktarılırken ana klan bankası dolu!");
                    }
                }
            }
        }
    }
}
```

**Config Ayarları:**
```yaml
branch-system:
  min-clan-level: 5  # Şube açabilmek için minimum klan seviyesi
  min-distance-from-main: 500  # Ana klan bölgesinden minimum uzaklık (blok)
  max-branches-per-clan: 3  # Klan başına maksimum şube sayısı
  branch-crystal-recipe:
    - main-crystal: 1
    - special-item: 1  # Özel item (boss dropu)
```

---

## 📊 3. GÖREV SİSTEMİ DETAYLARI ⭐ **DERİNLEŞTİRME**

### Görev Türleri ve Detayları

**A. Bireysel Görevler (Detaylı)**

**Görev Kategorileri:**
1. **Kaynak Toplama Görevleri**
   - Günlük: "64 Odun topla" → 10x Elmas ödülü
   - Haftalık: "500 Demir topla" → 50x Elmas + 5x Titanyum ödülü
   - Özel: "1000 Karanlık Madde topla" → Efsanevi item ödülü

2. **Savaş Görevleri**
   - Günlük: "10 Zombi öldür" → 5x Elmas ödülü
   - Haftalık: "1 Boss öldür" → 25x Elmas + Özel item ödülü
   - Özel: "3 Farklı Boss öldür" → Efsanevi item ödülü

3. **Yapı Görevleri**
   - Günlük: "1 Yapı inşa et" → 10x Elmas ödülü
   - Haftalık: "1 Yapı seviyesi artır" → 50x Elmas + 10x Titanyum ödülü
   - Özel: "5 Farklı yapı inşa et" → Özel item ödülü

4. **Ritüel Görevleri**
   - Günlük: "5 Ritüel yap" → 10x Elmas ödülü
   - Haftalık: "1 Büyük ritüel yap" → 50x Elmas + 5x Titanyum ödülü
   - Özel: "10 Farklı ritüel yap" → Özel item ödülü

**B. Klan Görevleri (Detaylı)**

**Görev Kategorileri:**
1. **Klan Savaş Görevleri**
   - "1 Savaş kazan" → Tüm üyelere 100x Elmas
   - "3 Savaş kazan" → Tüm üyelere 500x Elmas + Özel item
   - "5 Savaş kazan" → Tüm üyelere 1000x Elmas + Efsanevi item

2. **Klan Yapı Görevleri**
   - "5 Yapı inşa et" → Tüm üyelere 50x Elmas
   - "1 Yapı seviyesi 5'e çıkar" → Tüm üyelere 200x Elmas + 20x Titanyum
   - "10 Farklı yapı inşa et" → Tüm üyelere 500x Elmas + Özel item

3. **Klan Kaynak Görevleri**
   - "Klan bankasına 1000x Elmas yatır" → Tüm üyelere 100x Elmas
   - "Klan bankasına 5000x Demir yatır" → Tüm üyelere 200x Elmas + 10x Titanyum
   - "Klan bankasına 10000x Karanlık Madde yatır" → Tüm üyelere 1000x Elmas + Efsanevi item

**Görev İlerleme Takibi:**
```java
public class DetailedQuestSystem {
    /**
     * Görev ilerlemesi takip et
     */
    public void trackQuestProgress(Player player, QuestType type, Object data) {
        List<IndividualQuest> activeQuests = getActiveQuests(player);
        
        for (IndividualQuest quest : activeQuests) {
            if (quest.getType() != type) continue;
            
            // İlerleme güncelle
            switch (type) {
                case RESOURCE_COLLECTION:
                    if (data instanceof Material) {
                        Material collected = (Material) data;
                        if (quest.getTargetMaterial() == collected) {
                            quest.setProgress(quest.getProgress() + 1);
                        }
                    }
                    break;
                    
                case COMBAT:
                    if (data instanceof EntityType) {
                        EntityType killed = (EntityType) data;
                        if (quest.getTargetEntity() == killed) {
                            quest.setProgress(quest.getProgress() + 1);
                        }
                    }
                    break;
                    
                case STRUCTURE_BUILD:
                    if (data instanceof Structure.Type) {
                        Structure.Type built = (Structure.Type) data;
                        if (quest.getTargetStructure() == built) {
                            quest.setProgress(quest.getProgress() + 1);
                        }
                    }
                    break;
            }
            
            // Görev tamamlandı mı?
            if (quest.getProgress() >= quest.getTargetAmount()) {
                completeQuest(player, quest);
            }
        }
    }
    
    /**
     * Görev tamamlama (detaylı ödül sistemi)
     */
    private void completeQuest(Player player, IndividualQuest quest) {
        // Ödül hesapla (zorluk bazlı)
        List<ItemStack> rewards = calculateQuestRewards(quest);
        
        // Çete bonusu varsa uygula
        Gang playerGang = gangSystem.getPlayerGang(player.getUniqueId());
        if (playerGang != null) {
            double bonus = gangSystem.calculateGangQuestBonus(playerGang);
            rewards = applyBonusToRewards(rewards, bonus);
        }
        
        // Ödülleri ver
        for (ItemStack reward : rewards) {
            HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(reward);
            if (!overflow.isEmpty()) {
                playerVaultSystem.depositToVault(player.getUniqueId(), reward);
            }
        }
        
        // XP ödülü
        int xpReward = calculateXPReward(quest);
        player.giveExp(xpReward);
        
        // Partikül efektleri
        player.getLocation().getWorld().spawnParticle(
            Particle.TOTEM, player.getLocation(), 30, 1, 1, 1, 0.1);
        
        player.sendMessage("§a§lGÖREV TAMAMLANDI!");
        player.sendMessage("§eÖdül: §6" + rewards.size() + " item + " + xpReward + " XP");
        player.sendTitle("§a§lGÖREV TAMAMLANDI", "§eTebrikler!", 10, 70, 20);
    }
}
```

---

## 🛒 4. MARKET SİSTEMİ DETAYLARI ⭐ **DERİNLEŞTİRME**

### Fiyatlandırma Sistemi

**Otomatik Fiyatlandırma:**
- Market sistemi, item fiyatlarını otomatik olarak belirler
- Fiyat, arz-talep dengesine göre değişir
- Nadir itemler daha pahalıdır
- Çok satılan itemler ucuzlar

**Fiyat Hesaplama:**
```java
public class AdvancedMarketPricing {
    /**
     * Item fiyatı hesapla (arz-talep bazlı)
     */
    public double calculateItemPrice(Material material) {
        // Temel fiyat (item türüne göre)
        double basePrice = getBasePrice(material);
        
        // Arz-talep dengesi
        int supply = getMarketSupply(material); // Market'teki toplam item sayısı
        int demand = getMarketDemand(material); // Son 24 saatteki alım sayısı
        
        double supplyDemandRatio = (double) demand / (supply + 1); // +1 sıfıra bölme önleme
        
        // Fiyat hesapla
        double price = basePrice * (1.0 + supplyDemandRatio);
        
        // Minimum ve maksimum fiyat sınırları
        double minPrice = basePrice * 0.5; // %50 indirim maksimum
        double maxPrice = basePrice * 3.0; // %200 zam maksimum
        
        return Math.max(minPrice, Math.min(maxPrice, price));
    }
    
    /**
     * Otomatik alım-satım (Seviye 5 market için)
     */
    public void processAutoTrade(Market market) {
        if (market.getLevel() < 5) return; // Sadece seviye 5 market
        
        // Düşük fiyatlı itemleri otomatik al
        for (Material material : Material.values()) {
            double currentPrice = calculateItemPrice(material);
            double basePrice = getBasePrice(material);
            
            // Fiyat %30'un altındaysa otomatik al
            if (currentPrice < basePrice * 0.7) {
                autoBuyItem(market, material, 100); // 100 adet al
            }
            
            // Fiyat %50'nin üstündeyse otomatik sat
            if (currentPrice > basePrice * 1.5) {
                autoSellItem(market, material, 100); // 100 adet sat
            }
        }
    }
}
```

**Stok Yönetimi:**
- Market seviyesine göre maksimum stok kapasitesi
- Stok dolduğunda yeni satışlar kabul edilmez
- Stok boşaldığında otomatik uyarı

---

## 💰 5. MAAŞ SİSTEMİ DETAYLARI ⭐ **DERİNLEŞTİRME**

### Ödeme Zamanları ve Otomatik Dağıtım

**Maaş Türleri:**
1. **Günlük Maaş**: Her gün belirli saatte ödenir
2. **Haftalık Maaş**: Her hafta belirli günde ödenir
3. **Aylık Maaş**: Her ay belirli günde ödenir
4. **Anlık Maaş**: Görev tamamlandığında anında ödenir

**Otomatik Dağıtım Sistemi:**
```java
public class DetailedSalarySystem {
    /**
     * Otomatik maaş dağıtımı (zamanlanmış görev)
     */
    @ScheduledTask(period = 3600000L) // Her saat kontrol
    public void processSalaryPayments() {
        for (Clan clan : clanManager.getAllClans()) {
            Map<Clan.Rank, SalaryConfig> salaryConfigs = getClanSalaryConfigs(clan);
            
            for (Map.Entry<Clan.Rank, SalaryConfig> entry : salaryConfigs.entrySet()) {
                Clan.Rank rank = entry.getKey();
                SalaryConfig config = entry.getValue();
                
                // Ödeme zamanı geldi mi?
                if (!isPaymentTime(config)) continue;
                
                // Bu rütbedeki tüm üyelere maaş öde
                List<UUID> rankMembers = getRankMembers(clan, rank);
                for (UUID memberId : rankMembers) {
                    paySalary(clan, memberId, rank, config);
                }
                
                // Son ödeme zamanını güncelle
                updateLastPaymentTime(clan, rank);
            }
        }
    }
    
    /**
     * Maaş öde (detaylı)
     */
    private void paySalary(Clan clan, UUID memberId, Clan.Rank rank, SalaryConfig config) {
        // Maaş itemlerini al
        List<ItemStack> salaryItems = config.getSalaryItems();
        
        // Klan bankasından itemleri al
        Inventory bankChest = getClanBank(clan);
        if (bankChest == null) {
            notifyClanLeaders(clan, "§cMaaş ödenemedi: Klan bankası bulunamadı!");
            return;
        }
        
        // Bankada yeterli item var mı?
        for (ItemStack salaryItem : salaryItems) {
            if (!bankChest.containsAtLeast(salaryItem, salaryItem.getAmount())) {
                notifyClanLeaders(clan, "§cMaaş ödenemedi: Yeterli item yok! (" + 
                    salaryItem.getType().name() + " x" + salaryItem.getAmount() + ")");
                return;
            }
        }
        
        // Bankadan itemleri al
        for (ItemStack salaryItem : salaryItems) {
            bankChest.removeItem(salaryItem);
        }
        
        // Oyuncuya ver
        Player member = Bukkit.getPlayer(memberId);
        if (member != null && member.isOnline()) {
            // Envanter kontrolü
            for (ItemStack salaryItem : salaryItems) {
                HashMap<Integer, ItemStack> overflow = member.getInventory().addItem(salaryItem);
                if (!overflow.isEmpty()) {
                    // Envanter dolu, özel sandığına aktar
                    playerVaultSystem.depositToVault(memberId, salaryItem);
                    member.sendMessage("§cEnvanterin dolu! Maaş özel sandığına aktarıldı.");
                }
            }
            
            member.sendMessage("§a§lMAAŞ ALDINIZ!");
            member.sendMessage("§eRütbe: §6" + rank.name());
            member.sendMessage("§eMaaş: §6" + salaryItems.size() + " item");
            
            // Partikül efektleri
            member.getLocation().getWorld().spawnParticle(
                Particle.TOTEM, member.getLocation(), 20, 1, 1, 1, 0.1);
        } else {
            // Offline, özel sandığına aktar
            for (ItemStack salaryItem : salaryItems) {
                playerVaultSystem.depositToVault(memberId, salaryItem);
            }
        }
    }
}
```

**Özel Durumlar:**
- **Aktif Olmayan Üyeler**: 30 gün offline üyelere maaş ödenmez
- **Klan Bankası Boş**: Maaş ödenemez, liderlere uyarı gönderilir
- **Envanter Dolu**: Maaş özel sandığına aktarılır
- **Klan Dağıldı**: Tüm maaş kontratları iptal edilir

---

## ⚔️ 6. SAVAŞ TÜRLERİ DETAYLARI ⭐ **DERİNLEŞTİRME**

### Raid (Hızlı Saldırı) - Detaylı

**Özellikler:**
- Warmup: 2 dakika (normal kuşatma 5 dakika)
- Süre: 30 dakika (normal kuşatma sınırsız)
- Ödül: %30 ganimet (normal kuşatma %50)
- Fiziksel: Beacon + Obsidian + TNT + Clock (saat eklersen Raid olur)

**Raid Kuralları:**
- Sadece küçük klanlara (seviye 1-3) yapılabilir
- Büyük klanlara (seviye 4+) yapılamaz
- Raid sırasında kristal kırılamaz, sadece ganimet toplanır
- Raid bitince otomatik sona erer

### Open War (Açık Savaş) - Detaylı

**Özellikler:**
- Warmup: 5 dakika (normal)
- Süre: 1 saat
- Alan: Özel savaş bölgesi (her iki klanın dışında)
- Fiziksel: Beacon + Obsidian + TNT + Compass (pusula eklersen Open War)

**Open War Kuralları:**
- Savaş, özel bir bölgede yapılır (her iki klanın dışında)
- Bu bölgede normal korumalar devre dışıdır
- Savaş bitince bölge normale döner
- Kazanan klan, kaybeden klanın bankasından %40 ganimet alır

### Tournament (Turnuva) - Detaylı

**Özellikler:**
- 4 klan katılır
- Eleme usulü (yarı final, final)
- Warmup: 10 dakika
- Süre: 2 saat (tüm maçlar)
- Kazanan: Büyük ödül (1000x Elmas + Efsanevi item)

**Turnuva Kuralları:**
- Turnuva başlamadan önce kayıt olunmalı
- Kayıt ücreti: 100x Elmas (her klan)
- Kazanan klan, tüm ücretleri alır + ekstra ödül
- Turnuva sırasında dışarıdan müdahale yasaktır

---

## 🤝 7. İTTİFAK SİSTEMİ DETAYLARI ⭐ **DERİNLEŞTİRME**

### İhlal Mekanizmaları ve Cezalar

**İhlal Türleri:**
1. **İttifaklı Klana Saldırı**: İttifaklı klana kuşatma başlatmak
2. **İttifaklı Klanı Yok Etme**: İttifaklı klanın kristalini kırmak
3. **İttifakı Tek Taraflı Bozma**: Ritüel olmadan ittifakı sonlandırma

**Ceza Sistemi (Item-Based):**
```java
public class DetailedAllianceSystem {
    /**
     * İttifak ihlali tespit et ve ceza uygula
     */
    public void detectAllianceBreach(Clan violator, Clan victim) {
        Alliance alliance = getAlliance(violator, victim);
        if (alliance == null) return;
        
        // İhlal cezası (item-based)
        List<ItemStack> penaltyItems = calculatePenaltyItems(violator);
        
        // İhlal eden klanın bankasından itemleri al
        Inventory violatorBank = getClanBank(violator);
        if (violatorBank == null) {
            // Banka yok, tüm üyelere ceza uygula
            applyPenaltyToMembers(violator, penaltyItems);
            return;
        }
        
        // Bankadan itemleri al
        for (ItemStack penaltyItem : penaltyItems) {
            if (!violatorBank.containsAtLeast(penaltyItem, penaltyItem.getAmount())) {
                // Yeterli item yok, kısmi ceza
                ItemStack available = violatorBank.getItem(
                    violatorBank.first(penaltyItem.getType()));
                if (available != null) {
                    violatorBank.removeItem(available);
                }
            } else {
                violatorBank.removeItem(penaltyItem);
            }
        }
        
        // Tazminat (ihlal eden klanın bankasından mağdur klana)
        List<ItemStack> compensationItems = calculateCompensationItems(violator, penaltyItems);
        Inventory victimBank = getClanBank(victim);
        if (victimBank != null) {
            for (ItemStack compensationItem : compensationItems) {
                HashMap<Integer, ItemStack> overflow = victimBank.addItem(compensationItem);
                if (!overflow.isEmpty()) {
                    // Mağdur klan bankası dolu, uyarı ver
                    notifyClanLeaders(victim, "§cTazminat alınamadı: Banka dolu!");
                }
            }
        }
        
        // Hain damgası
        applyTraitorTag(violator);
        
        // İttifakı sonlandır
        terminateAlliance(alliance);
        
        // Broadcast
        Bukkit.broadcastMessage("§c§lİTTİFAK İHLALİ!");
        Bukkit.broadcastMessage("§c" + violator.getName() + " klanı " + 
            victim.getName() + " klanına ihanet etti!");
    }
    
    /**
     * Hain damgası uygula
     */
    private void applyTraitorTag(Clan clan) {
        for (UUID memberId : clan.getMembers().keySet()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null) {
                // Oyuncu isminin önüne [HAİN] etiketi ekle
                member.setDisplayName("§c[HAİN] §7" + member.getName());
                member.setPlayerListName("§c[HAİN] §7" + member.getName());
                
                // 30 gün süreyle etiket kalır
                traitorTags.put(memberId, System.currentTimeMillis() + 2592000000L);
            }
        }
    }
}
```

**Otomatik Yardım Sistemi:**
- İttifaklı klana saldırıldığında otomatik bildirim
- İttifaklı klan üyeleri savaş bölgesine ışınlanabilir (özel item ile)
- İttifaklı klan üyeleri birlikte savaşırken +%10 hasar bonusu

---

## 📈 8. AKTİVİTE TAKİBİ ÖDÜLLERİ ⭐ **DERİNLEŞTİRME**

### Aktif Üyelere Verilen Bonuslar

**Aktivite Seviyeleri:**
1. **Çok Aktif**: Son 7 gün içinde her gün online (günlük 2+ saat)
2. **Aktif**: Son 7 gün içinde 5+ gün online
3. **Orta Aktif**: Son 7 gün içinde 3+ gün online
4. **Az Aktif**: Son 7 gün içinde 1-2 gün online
5. **Pasif**: Son 7 gün içinde hiç online değil

**Bonuslar:**
```java
public class DetailedActivityRewards {
    /**
     * Aktivite bonusları uygula
     */
    public void applyActivityBonuses(Player player) {
        ActivityLevel level = calculateActivityLevel(player.getUniqueId());
        
        switch (level) {
            case VERY_ACTIVE:
                // Çok aktif: +%15 güç, +%10 hasar, +%10 hız
                buffManager.applyBuff(player, "very_active", 
                    Map.of("power", 0.15, "damage", 0.10, "speed", 0.10));
                player.sendMessage("§a§lÇOK AKTİF BONUSU!");
                player.sendMessage("§e+%15 Güç, +%10 Hasar, +%10 Hız");
                break;
                
            case ACTIVE:
                // Aktif: +%10 güç, +%5 hasar
                buffManager.applyBuff(player, "active", 
                    Map.of("power", 0.10, "damage", 0.05));
                player.sendMessage("§aAktif Bonusu: +%10 Güç, +%5 Hasar");
                break;
                
            case MODERATELY_ACTIVE:
                // Orta aktif: +%5 güç
                buffManager.applyBuff(player, "moderately_active", 
                    Map.of("power", 0.05));
                player.sendMessage("§eOrta Aktif Bonusu: +%5 Güç");
                break;
                
            default:
                // Bonus yok
                break;
        }
    }
    
    /**
     * Haftalık aktivite ödülü
     */
    @ScheduledTask(period = 604800000L) // Her hafta
    public void giveWeeklyActivityRewards() {
        for (Clan clan : clanManager.getAllClans()) {
            for (UUID memberId : clan.getMembers().keySet()) {
                ActivityLevel level = calculateActivityLevel(memberId);
                
                if (level == ActivityLevel.VERY_ACTIVE) {
                    // Çok aktif üyelere haftalık ödül
                    List<ItemStack> rewards = Arrays.asList(
                        new ItemStack(Material.DIAMOND, 50),
                        new ItemStack(Material.GOLD_INGOT, 100)
                    );
                    
                    Player member = Bukkit.getPlayer(memberId);
                    if (member != null && member.isOnline()) {
                        for (ItemStack reward : rewards) {
                            member.getInventory().addItem(reward);
                        }
                        member.sendMessage("§a§lHAFTALIK AKTİVİTE ÖDÜLÜ!");
                        member.sendMessage("§e50x Elmas + 100x Altın");
                    } else {
                        // Offline, özel sandığına aktar
                        for (ItemStack reward : rewards) {
                            playerVaultSystem.depositToVault(memberId, reward);
                        }
                    }
                }
            }
        }
    }
}
```

---

## 📊 9. KLAN İSTATİSTİKLERİ DETAYLARI ⭐ **DERİNLEŞTİRME**

### Detaylı İstatistik Türleri

**1. Savaş İstatistikleri (Detaylı)**
- Toplam savaş sayısı
- Kazanılan/kaybedilen savaşlar
- En çok savaşılan klan
- Ortalama savaş süresi
- Toplam ganimet miktarı
- En başarılı savaşçı

**2. Ekonomi İstatistikleri (Detaylı)**
- Toplam banka yatırımı (item sayısı)
- Toplam banka çekimi (item sayısı)
- En çok yatırılan item türü
- En çok çekilen item türü
- Ortalama maaş miktarı
- Toplam transfer kontratı sayısı

**3. Üye İstatistikleri (Detaylı)**
- Toplam üye sayısı
- Aktif üye sayısı (son 7 gün)
- En aktif üye
- En çok katkı yapan üye
- Ortalama üye gücü
- Üye dağılımı (rütbe bazlı)

**4. Yapı İstatistikleri (Detaylı)**
- Toplam yapı sayısı
- Yapı seviye dağılımı
- En güçlü yapı
- Toplam yapı gücü
- Yapı inşa edilme tarihleri

**Görselleştirme:**
- İstatistik tahtasında grafikler gösterilir
- Harita üzerinde klan bölgesi görüntülenir
- Zaman çizelgesi ile geçmiş istatistikler görüntülenir

---

---

## 📝 GÜNCELLEME NOTLARI (2024)

### ✅ Tamamlanan Özellikler

**1. Klan Üye Yönetimi GUI Menüsü (`ClanMemberMenu.java`)**
- ✅ Üye listesi görüntüleme (rütbe sırasına göre, online/offline durumu)
- ✅ Aktivite bilgisi (son görülme zamanı)
- ✅ Rütbe değiştirme (Lider/General, onay sistemi)
- ✅ Üye çıkarma (Lider/General, onay menüsü)
- ✅ NBT tabanlı UUID takibi (güvenilir üye tespiti)
- ✅ Thread-safe operations

**2. Klan Görev Sistemi GUI Menüsü (`ClanMissionMenu.java`)**
- ✅ Aktif görev görüntüleme
- ✅ Görev ilerlemesi takibi (toplam ve üye bazlı)
- ✅ Görev oluşturma (Lider/General, chat-based wizard)
- ✅ Görev iptal etme (Lider/General)
- ✅ Üye bazlı ilerleme gösterimi

**3. Klan İstatistikleri GUI Menüsü (`ClanStatsMenu.java`)**
- ✅ Genel bilgiler (seviye, üye sayısı, kuruluş tarihi, bölge bilgisi)
- ✅ Güç istatistikleri (toplam, ortalama, en güçlü üye)
- ✅ Üye istatistikleri (online/offline, rütbe dağılımı)
- ✅ Yapı istatistikleri (toplam, teknoloji seviyesi)
- ✅ Görev istatistikleri (aktif görev, ilerleme)
- ✅ Seviye bonusları (güç bonusu, erişilebilir özellikler)
- ✅ En aktif üyeler (8 üye, son görülme zamanı)
- ✅ En güçlü üyeler (8 üye, güç değerleri)

**4. Kontrat Sistemi GUI Menüleri (`ContractMenu.java`)**
- ✅ Kontrat listesi GUI menüsü (sayfalama, 45 kontrat/sayfa)
- ✅ Kontrat detayları GUI menüsü (tüm bilgiler, tip bazlı özel bilgiler)
- ✅ Kontrat kabul etme (kan imzası, 1 kalp kaybı)
- ✅ Kontrat reddetme
- ✅ Kontrat oluşturma wizard başlangıcı (tip seçimi)
- ✅ `/kontrat list` komutu GUI menüsünü açıyor
- ⚠️ Wizard tamamlanması gerekiyor (kapsam, ödül, ceza, süre adımları)

**Entegrasyonlar:**
- ✅ `ClanMenu.java` güncellendi (İstatistikler butonu eklendi - Slot 18)
- ✅ `Main.java` güncellendi (tüm yeni GUI menüleri initialize edildi)
- ✅ Event handler'lar eklendi (tüm menüler için)
- ✅ Thread-safe operations (ConcurrentHashMap kullanımı)
- ✅ Null kontrolleri ve exception handling

**Rapor Tarihi:** 2024  
**Versiyon:** 1.2 - Klan Sistemi Özellik Önerileri (GUI Menüleri Tamamlandı)  
**Durum:** ✅ GUI Menüleri Tamamlandı, Wizard Tamamlanması Bekleniyor

