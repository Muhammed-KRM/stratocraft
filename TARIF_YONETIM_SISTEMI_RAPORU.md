# Tarif Yönetim Sistemi Raporu

## ✅ OLUŞTURULAN SİSTEM

### 1. RecipeManager Oluşturuldu
- **Dosya:** `src/main/java/me/mami/stratocraft/manager/RecipeManager.java`
- **Özellikler:**
  - Tüm tarifleri merkezi olarak yönetir
  - Tarifleri JSON/YAML formatında saklar
  - Tarif yükleme/kaydetme
  - Kategoriye ve tipe göre filtreleme
  - Thread-safe (ConcurrentHashMap kullanır)

### 2. Tarif Dizin Yapısı
```
plugins/Stratocraft/
  └── recipes/
      ├── structures/     # Yapı tarifleri
      ├── batteries/      # Batarya tarifleri
      ├── rituals/        # Ritüel tarifleri
      ├── crafting/       # Crafting tarifleri
      └── other/          # Diğer tarifler
```

### 3. YAML Tarif Formatı
```yaml
recipe_id:
  type: STRUCTURE          # RecipeType enum değeri
  category: STRUCTURE      # RecipeCategory enum değeri
  displayName: "Görev Loncası"
  level: 1
  ingredients:
    - material: COBBLESTONE
      amount: 1
    - material: LECTERN
      amount: 1
  result:
    material: END_CRYSTAL
    amount: 1
```

## 📋 METODLAR

### RecipeManager Metodları
- `registerRecipe(Recipe recipe)` - Tarif kaydet
- `saveRecipe(Recipe recipe)` - Tarifi dosyaya kaydet
- `getRecipe(String recipeId)` - Tarif al (ID ile)
- `getRecipesByCategory(RecipeCategory category)` - Kategoriye göre tarifler
- `getRecipesByType(RecipeType type)` - Tipe göre tarifler
- `getAllRecipes()` - Tüm tarifler
- `hasRecipe(String recipeId)` - Tarif var mı?
- `removeRecipe(String recipeId)` - Tarif sil
- `reload()` - Tüm tarifleri yeniden yükle

## 🔄 MEVCUT DURUM

### Structure Tarifleri
- **Konum:** `StructureRecipeManager.registerAllRecipes()`
- **Format:** Kod içi (BlockRecipe)
- **Durum:** Hala kod içinde, JSON'a taşınabilir

### Battery Tarifleri
- **Konum:** `NewBatteryManager.registerAllRecipes()`
- **Format:** Kod içi (RecipeChecker implementasyonları)
- **Durum:** Hala kod içinde, JSON'a taşınabilir

### Ritual Tarifleri
- **Konum:** `RitualInteractionListener`
- **Format:** Hard-coded pattern kontrolü
- **Durum:** Hala kod içinde, JSON'a taşınabilir

## ⚠️ YAPILMASI GEREKENLER

1. **Mevcut Tarifleri JSON'a Taşı:**
   - Structure tariflerini JSON'a export et
   - Battery tariflerini JSON'a export et
   - Ritual tariflerini JSON'a export et
   - Ghost tariflerini JSON'a export et

2. **Export Metodları:**
   - `StructureRecipeManager.exportRecipesToJSON()` - Structure tariflerini export et
   - `NewBatteryManager.exportRecipesToJSON()` - Battery tariflerini export et
   - `RitualInteractionListener.exportRecipesToJSON()` - Ritual tariflerini export et

3. **Yükleme Entegrasyonu:**
   - `StructureRecipeManager`'ı RecipeManager'dan tarifleri yükleyecek şekilde güncelle
   - `NewBatteryManager`'ı RecipeManager'dan tarifleri yükleyecek şekilde güncelle
   - `RitualInteractionListener`'ı RecipeManager'dan tarifleri yükleyecek şekilde güncelle

## 📝 NOTLAR

1. **Geriye Uyumluluk:** Mevcut kod içi tarifler hala çalışıyor, JSON tarifleri ek olarak yükleniyor.

2. **Performans:** JSON tarifleri plugin başlangıcında yükleniyor, runtime'da dosya okuma yok.

3. **Genişletilebilirlik:** Yeni tarifler kolayca JSON dosyası olarak eklenebilir.

