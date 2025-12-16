# Yapı Tarifleri Dizini

Bu dizin yapı tariflerinin organize edildiği yerdir.

## Yapı

- **Kod İçi Tarifler:** `StructureRecipeManager.java` içinde tanımlanır
- **Şema Tarifleri:** `schematics/` klasöründe `.schem` dosyaları olarak saklanır

## Gelecek Geliştirmeler

- Tarifleri JSON/YAML formatında buraya taşımak
- Tarif yönetimi için ayrı bir sistem oluşturmak
- Tarif doğrulama kurallarını buraya taşımak

## Mevcut Durum

Şu anda tüm tarifler `StructureRecipeManager.java` içinde `registerAllRecipes()` metodunda tanımlanmaktadır.

