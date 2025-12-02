# STRATOCRAFT - İTTİFAK SİSTEMİ

## 🤝 İttifak Sistemi Nedir?

İttifaklar, klanlar arası **kalıcı ve bağlayıcı** anlaşmalardır. Kontrat sistemine benzer şekilde çalışır ancak **daha güçlü ve cezalı**dır.

---

## 📋 İÇİNDEKİLER

1. [İttifak Tipleri](#ittifak-tipleri)
2. [İttifak Kurma Ritüeli](#ittifak-kurma-ritüeli)
3. [İttifak İhlali ve Ceza](#ittifak-ihlali-ve-ceza)
4. [İttifak Sonlandırma](#ittifak-sonlandırma)

---

## 🎯 İTTİFAK TİPLERİ

### 1. Savunma İttifakı (DEFENSIVE)
- Bir klana saldırılırsa diğeri otomatik yardım eder
- Sadece savunma amaçlı

### 2. Saldırı İttifakı (OFFENSIVE)
- Birlikte saldırı yapılır
- Kuşatmalarda birlikte hareket edilir

### 3. Ticaret İttifakı (TRADE)
- Ticaret bonusları
- Malzeme değişimi kolaylaşır

### 4. Tam İttifak (FULL)
- Tüm özellikler
- En güçlü ittifak tipi

---

## 🔥 İTTİFAK KURMA RİTÜELİ

### Gereksinimler:
- **İki Lider**: Her iki klanın lideri olmalı
- **Elmas**: Her iki liderin elinde Elmas olmalı
- **Yakınlık**: İki lider birbirine 3 blok yakın olmalı
- **Shift**: Her iki lider Shift'e basılı tutmalı

### Adımlar:
```
1. İki lider birbirine yaklaşır (3 blok mesafe)
2. Her ikisi de Shift'e basılı tutar
3. Her ikisinin elinde Elmas olmalı
4. Bir lider diğerine Shift + Sağ Tık yapar
5. SONUÇ:
   - İttifak kurulur
   - Elmaslar tüketilir
   - Partikül efektleri
   - Sunucuya duyuru
```

### Görsel Efektler:
- HEART partikülleri (kırmızı)
- END_ROD partikülleri (beyaz)
- TOTEM partikülleri (renkli)
- "İTTİFAK KURULDU" title
- Sunucu broadcast mesajı

### Cooldown:
- Her klan 5 dakika içinde tekrar ittifak kuramaz

---

## ⚠️ İTTİFAK İHLALİ VE CEZA

### İhlal Durumları:
1. **İttifaklı klana saldırı**: İttifaklı klana kuşatma başlatmak
2. **İttifaklı klanı yok etme**: İttifaklı klanın kristalini kırmak
3. **İttifakı tek taraflı bozma**: Ritüel olmadan ittifakı sonlandırma

### Ceza Sistemi:
```
İhlal Edildiğinde:
- İhlal eden klanın bakiyesinin %20'si kesilir
- İhlal eden klan üyelerine "HAİN" etiketi verilir
- Diğer klana tazminat ödenir (ihlal eden klanın bakiyesinden %10)
- Sunucuya duyuru yapılır
```

### Örnek:
```
Klan A ve Klan B ittifak halinde
Klan A, Klan B'ye saldırır
→ Klan A'nın bakiyesi: 10000 altın
→ Ceza: 2000 altın kesilir
→ Klan B'ye tazminat: 1000 altın
→ Klan A üyeleri: [HAİN] etiketi alır
```

---

## 🔚 İTTİFAK SONLANDIRMA

### Karşılıklı Sonlandırma:
- İki lider birlikte ritüel yaparak ittifakı sonlandırabilir
- **Cezasız** sonlandırma
- Ritüel: Elinde Kırmızı Çiçek ile aynı ritüel

### Tek Taraflı Sonlandırma:
- İttifakı ihlal etmek = otomatik sonlandırma + ceza

### Süre Dolması:
- Eğer ittifak süreli ise, süre dolunca otomatik sona erer
- Cezasız sonlandırma

---

## 📊 İTTİFAK YÖNETİMİ

### İttifak Listesi:
- Her klan aktif ittifaklarını görebilir
- İttifak tipi ve süresi görüntülenir

### İttifak Bonusları:
- **Savunma İttifakı**: Saldırı anında yardım
- **Saldırı İttifakı**: Birlikte saldırı bonusu
- **Ticaret İttifakı**: Ticaret fiyat bonusu
- **Tam İttifak**: Tüm bonuslar

---

## ⚔️ İTTİFAK VE SAVAŞ

### Kurallar:
- İttifaklı klanlara **saldırılamaz** (otomatik engellenir)
- İttifaklı klanlara saldırı denemesi = **İttifak İhlali**
- İttifaklı klanlar birlikte boss'a saldırabilir

### Örnek Senaryo:
```
Klan A ve Klan B ittifak halinde
Klan C, Klan A'ya saldırmak ister
→ Klan B otomatik Klan A'yı savunur
→ Klan C hem Klan A hem Klan B ile savaşır
```

---

## 🎮 ÖNEMLİ NOTLAR

1. **İttifaklar Kalıcıdır**: Bozulmadıkça veya süre dolmadıkça devam eder
2. **İhlal Cezası Ağırdır**: İttifakı bozmak pahalıya mal olur
3. **Sadece Liderler**: İttifak kurma/sonlandırma sadece liderler yapabilir
4. **Cooldown Var**: Spam önleme için 5 dakika cooldown

---

*Bu sistem, kontrat sistemine benzer şekilde çalışır ancak daha güçlü ve bağlayıcıdır.*

