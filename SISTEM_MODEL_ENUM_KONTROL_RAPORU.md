# Sistem Model ve Enum Kontrol Raporu

## ✅ MEVCUT MODELLER VE ENUM'LAR

### Felaket Sistemi
- ✅ Disaster model var
- ✅ DisasterType enum var
- ✅ DisasterCategory enum var
- ✅ CreatureDisasterType enum var
- ✅ DisasterPhase model var
- ✅ DisasterConfig model var

### Görev Sistemi
- ✅ Mission model var
- ✅ MissionType enum var

### Kontrat Sistemi
- ✅ Contract model var
- ✅ ContractType enum var

### Yapı Sistemi
- ✅ BaseStructure model var
- ✅ ClanStructure model var
- ✅ PersonalStructure model var
- ✅ StructureType enum var
- ✅ StructureCategory enum var
- ✅ StructureEffectType enum var

### Klan Sistemi
- ✅ Clan model var
- ✅ ClanData model var
- ✅ TerritoryData model var
- ✅ Alliance model var
- ✅ AllianceType enum var

### Oyuncu Sistemi
- ✅ PlayerData model var

### Tuzak Sistemi
- ✅ TrapCoreBlock model var
- ✅ TrapType enum var

### Eşya Sistemi
- ✅ BaseItem model var
- ✅ WeaponItem model var
- ✅ OreItem model var
- ✅ WeaponType enum var
- ✅ ArmorType enum var

### Market Sistemi
- ✅ Shop model var

## ❌ EKSİK MODELLER VE ENUM'LAR

### Boss Sistemi
- ❌ Boss model yok (BossData inner class var ama ayrı model yok)
- ✅ BossType enum var

### Evcil Canlılar Sistemi
- ❌ TamingData model yok
- ✅ RideableType enum var (merkezi)
- ❌ Gender enum merkezi değil (TamingManager içinde)

### Araştırma Sistemi
- ❌ Research model yok
- ❌ ResearchType enum yok

### Tarif Sistemi
- ❌ Recipe model yok
- ❌ RecipeType enum yok
- ❌ RecipeCategory enum yok

### Banka Sistemi
- ❌ PersonalBank model yok
- ❌ BankTransaction model yok
- ❌ BankAccountType enum yok

### Market Sistemi (Global)
- ❌ Market model yok (global market için)
- ❌ MarketType enum yok

### Mayın Sistemi
- ❌ MineData model yok
- ✅ MineType enum var

### Batarya Sistemi
- ❌ BatteryData model yok
- ✅ BatteryCategory enum var
- ❌ BatteryType enum yok (merkezi)

## 🎯 OLUŞTURULACAK MODELLER VE ENUM'LAR

1. **Boss Model** - Boss verilerini tutacak
2. **TamingData Model** - Evcil canlı verilerini tutacak
3. **Gender Enum** - Merkezi enum olarak
4. **Research Model** - Araştırma verilerini tutacak
5. **ResearchType Enum** - Araştırma tipleri
6. **Recipe Model** - Tarif verilerini tutacak
7. **RecipeType Enum** - Tarif tipleri
8. **RecipeCategory Enum** - Tarif kategorileri
9. **PersonalBank Model** - Kişisel banka verilerini tutacak
10. **BankTransaction Model** - Banka işlemlerini tutacak
11. **BankAccountType Enum** - Banka hesap tipleri
12. **Market Model** - Global market verilerini tutacak
13. **MarketType Enum** - Market tipleri
14. **MineData Model** - Mayın verilerini tutacak
15. **BatteryData Model** - Batarya verilerini tutacak
16. **BatteryType Enum** - Batarya tipleri (merkezi)

