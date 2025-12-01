# 🆓 ModelEngine Ücretsiz Versiyon - Detaylı Bilgi

## 📋 RESİMDE GÖRÜLEN BİLGİLER

Resimdeki bilgilere göre:

### FREE Versiyonu:
- ✅ **"All Features"** - Tüm özellikler mevcut!
- ⚠️ **"12 Model Limits"** - 12 model sınırı var

### PREMIUM Versiyonu:
- ✅ **"All Features"** - Tüm özellikler mevcut
- ✅ **"No Model Limit"** - Model sınırı yok
- ✅ **"Supports Ticko"** - Ticko desteği var

---

## ✅ ÜCRETSİZ VERSİYONUN ÖZELLİKLERİ

### Ne Var? (Tüm Özellikler):
- ✅ Custom entity model desteği
- ✅ Hazır modelleri kullanabilme
- ✅ Hitbox ayarları
- ✅ Çoklu hitbox desteği
- ✅ Animasyon desteği
- ✅ Tüm ModelEngine özellikleri

### Ne Yok? (Kısıtlamalar):
- ⚠️ **12 Model Sınırı** - En fazla 12 model yükleyebilirsiniz
- ❌ Model sınırı yok (PREMIUM'da)

---

## ❓ SIK SORULAN SORULAR

### 1. Hazır Modelleri Kullanabilir miyim?

**CEVAP: EVET! ✅**

- Ücretsiz versiyonda da hazır modelleri kullanabilirsiniz
- Sadece **12 model sınırı** var
- Yani en fazla 12 farklı model yükleyebilirsiniz

**Örnek:**
- Ork modeli → 1 model
- Goblin modeli → 1 model
- Ejderha modeli → 1 model
- ... toplam 12 model

### 2. Kullanım Süresi Var mı?

**CEVAP: HAYIR! ❌**

- Resimde kullanım süresi belirtilmemiş
- Genelde ModelEngine ücretsiz versiyonu **süresiz** kullanılabilir
- Sadece model sayısı sınırlı (12 model)

### 3. Hazır Model Kullanmada Sınır Var mı?

**CEVAP: EVET, 12 MODEL SINIRI VAR ⚠️**

- Hazır model kullanabilirsiniz
- Ama **en fazla 12 farklı model** yükleyebilirsiniz
- Aynı modeli birden fazla entity'de kullanabilirsiniz (sınır yok)
- Sadece **farklı model dosyaları** 12 ile sınırlı

**Örnek:**
```
✅ Ork modeli → 100 farklı ork entity'sinde kullanabilirsiniz (1 model sayılır)
✅ Goblin modeli → 50 farklı goblin entity'sinde kullanabilirsiniz (1 model sayılır)
...
⚠️ Toplam 12 farklı model dosyası yükleyebilirsiniz
```

### 4. Model Sınırı Aşılırsa Ne Olur?

**CEVAP:**
- 12. modelden sonra yeni model yüklenemez
- Mevcut modeller çalışmaya devam eder
- PREMIUM'a geçmeniz gerekir (sınırsız model için)

---

## 🎯 SİZİN DURUMUNUZ İÇİN DEĞERLENDİRME

### Ücretsiz Versiyon Yeterli mi?

**EVET, BAŞLANGIÇ İÇİN YETERLİ! ✅**

**Neden:**
- 12 model başlangıç için yeterli
- Tüm özellikler mevcut
- Hazır modelleri kullanabilirsiniz
- Çoklu hitbox desteği var

**Örnek Kullanım:**
```
1. Ork modeli (1.5x büyük)
2. Goblin modeli
3. Troll modeli
4. Ejderha modeli
5. Cehennem Ejderi modeli (uzun, çoklu hitbox)
6. T-Rex modeli
7. Cyclops modeli
8. Titan Golem modeli
9. Hydra modeli
10. Minotaur modeli
11. Harpy modeli
12. Basilisk modeli
```

**12 model yeterli mi?**
- Başlangıç için: ✅ EVET
- İleride daha fazla gerekiyorsa: PREMIUM'a geçebilirsiniz

---

## 📋 KURULUM ADIMLARI (ÜCRETSİZ VERSİYON)

### ADIM 1: ModelEngine FREE İndir

1. **Siteye Git:**
   - mythiccraft.io (resimdeki site)
   - Veya: https://www.spigotmc.org/resources/modelengine.107955/

2. **FREE Versiyonunu İndir:**
   - "FREE" kartına tıkla
   - "Latest Free: R4.0.8" versiyonunu indir
   - `.jar` dosyasını al

### ADIM 2: Test Server'a Kur

```
C:\mc\test-server\plugins\ModelEngine.jar
```

### ADIM 3: Citizens Kur (ZORUNLU)

```
C:\mc\test-server\plugins\Citizens.jar
```

**Citizens Ücretsiz:**
- https://www.spigotmc.org/resources/citizens.13811/

### ADIM 4: Sunucuyu Başlat

- Konsolda "ModelEngine enabled" mesajını görmelisiniz
- Model sınırını kontrol edin: `/meg info`

---

## 🎨 HAZIR MODEL KULLANIMI (12 MODEL SINIRI İLE)

### ADIM 1: Hazır Model Bul

**Kaynaklar:**
1. **PlanetMinecraft:**
   - Arama: "ModelEngine model"
   - Filtre: "Free" + "ModelEngine"

2. **MC-Market:**
   - ModelEngine model paketleri
   - Ücretsiz ve ücretli seçenekler

3. **ModelEngine Discord:**
   - Topluluk modelleri
   - Ücretsiz paylaşımlar

### ADIM 2: Model Dosyalarını Yerleştir

```
C:\mc\test-server\plugins\ModelEngine\
├── models\
│   ├── ork.bbmodel          ← 1. model
│   ├── goblin.bbmodel      ← 2. model
│   ├── dragon.bbmodel      ← 3. model
│   └── ... (toplam 12 model)
└── textures\
    ├── ork.png
    ├── goblin.png
    └── ...
```

### ADIM 3: Model Yükle

```bash
/meg model load ork
/meg model load goblin
/meg model load dragon
# ... toplam 12 model
```

### ADIM 4: Model Sayısını Kontrol Et

```bash
/meg model list
```

**12 modelden fazla yüklemeye çalışırsanız:**
- Hata mesajı alırsınız
- "Model limit reached" gibi bir mesaj görürsünüz

---

## ⚠️ ÖNEMLİ NOTLAR

### Model Sayısı Nasıl Hesaplanır?

**1 Model = 1 Model Dosyası**

- Aynı modeli birden fazla entity'de kullanmak → 1 model sayılır
- Farklı model dosyaları → Her biri 1 model sayılır

**Örnek:**
```
✅ ork.bbmodel → 1 model (100 entity'de kullanılabilir)
✅ goblin.bbmodel → 1 model (50 entity'de kullanılabilir)
✅ dragon.bbmodel → 1 model (10 entity'de kullanılabilir)
...
⚠️ Toplam 12 farklı .bbmodel dosyası
```

### Model Sınırını Aşmamak İçin:

1. **Öncelikli Modelleri Seçin:**
   - En çok kullanacağınız 12 modeli seçin
   - Diğerlerini sonra ekleyebilirsiniz (PREMIUM'a geçince)

2. **Model Birleştirme:**
   - Benzer modelleri birleştirebilirsiniz
   - Örnek: "Ork Variant 1", "Ork Variant 2" yerine tek "Ork" modeli

3. **Gereksiz Modelleri Kaldırın:**
   - Kullanmadığınız modelleri silin
   - Yer açmak için

---

## 💰 PREMIUM'A GEÇİŞ

### Ne Zaman PREMIUM Gerekli?

**PREMIUM'a geçmeniz gerekir eğer:**
- 12'den fazla model gerekiyorsa
- Sınırsız model istiyorsanız
- Ticko desteği gerekiyorsa

**PREMIUM Fiyat:**
- Resimde fiyat belirtilmemiş
- Genelde ~$20-30 arası
- mythiccraft.io'dan satın alabilirsiniz

---

## ✅ SONUÇ

### Ücretsiz Versiyon İçin:

**✅ YAPABİLİRSİNİZ:**
- Hazır modelleri kullanabilirsiniz
- Tüm özellikleri kullanabilirsiniz
- Çoklu hitbox desteği var
- Süresiz kullanabilirsiniz

**⚠️ KISITLAMALAR:**
- En fazla 12 model yükleyebilirsiniz
- 12'den fazla model gerekiyorsa PREMIUM gerekli

**ÖNERİ:**
- Başlangıç için ücretsiz versiyon yeterli
- 12 model başlangıç için çok iyi
- İleride daha fazla gerekiyorsa PREMIUM'a geçebilirsiniz

---

## 🚀 HIZLI BAŞLANGIÇ

1. **ModelEngine FREE İndir:**
   - mythiccraft.io'dan FREE versiyonunu indir

2. **Citizens Kur:**
   - Ücretsiz Citizens plugin'ini kur

3. **Test Server'a Yerleştir:**
   ```
   C:\mc\test-server\plugins\ModelEngine.jar
   C:\mc\test-server\plugins\Citizens.jar
   ```

4. **Hazır Model Bul:**
   - PlanetMinecraft'dan ModelEngine modelleri bul

5. **Model Yükle (12'ye kadar):**
   ```
   /meg model load <model_name>
   ```

6. **Test Et:**
   ```
   /summon zombie ~ ~ ~
   /meg model apply @e[type=zombie,limit=1] <model_name>
   ```

**Hepsi bu kadar! Ücretsiz ve çalışıyor!**

---

**Son Güncelleme:** 2024-12-01
**Versiyon:** 1.0

