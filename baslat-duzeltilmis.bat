@echo off
echo ========================================
echo Stratocraft Test Sunucusu Baslatiliyor
echo ========================================
echo.

REM Java kontrolu
echo Java kontrol ediliyor...
java -version >nul 2>&1
if errorlevel 1 (
    echo.
    echo ========================================
    echo HATA: Java bulunamadi!
    echo ========================================
    echo.
    echo Lutfen Java 17 veya uzeri yukleyin: https://adoptium.net/
    pause
    exit
)

REM Java versiyon kontrolu (Java 8 kontrolu)
java -version 2>&1 | findstr /i /c:"1.8" >nul
if not errorlevel 1 (
    echo.
    echo ========================================
    echo UYARI: Java 8 (1.8) kullaniyorsunuz!
    echo ========================================
    echo.
    echo Plugin ve Paper 1.20.4 icin Java 17 GEREKLI!
    echo Mevcut Java versiyonu: Java 8 (yetersiz)
    echo.
    echo Lutfen Java 17 yukleyin: https://adoptium.net/
    echo Detayli rehber: JAVA17_KURULUM_ADIMLARI.md
    echo.
    echo Mevcut Java versiyonu:
    java -version
    echo.
    pause
    exit
)

echo Java versiyonu uygun gorunuyor.

REM Dosya adini kontrol et - paper-1.20.4-445.jar veya paper-1.20.4.jar olabilir
if exist "paper-1.20.4-445.jar" (
    echo Paper 1.20.4-445 bulundu, baslatiliyor...
    echo RAM: 1GB (test icin yeterli)
    java -Xmx1G -Xms512M -jar paper-1.20.4-445.jar nogui
) else if exist "paper-1.20.4.jar" (
    echo Paper 1.20.4 bulundu, baslatiliyor...
    echo RAM: 1GB (test icin yeterli)
    java -Xmx1G -Xms512M -jar paper-1.20.4.jar nogui
) else (
    echo HATA: Paper JAR dosyasi bulunamadi!
    echo Lutfen paper-1.20.4-445.jar veya paper-1.20.4.jar dosyasinin bu klasorde oldugundan emin olun.
    pause
    exit
)

pause


