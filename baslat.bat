@echo off
title Stratocraft Test Sunucusu
echo ========================================
echo Stratocraft Test Sunucusu Baslatiliyor
echo ========================================
echo.

REM Mevcut klasoru goster
echo Mevcut klasor: %CD%
echo.

REM Java kontrolu
echo [1/3] Java kontrol ediliyor...
java -version >nul 2>&1
if errorlevel 1 (
    echo.
    echo ========================================
    echo HATA: Java bulunamadi!
    echo ========================================
    echo.
    echo Lutfen Java 17 veya uzeri yukleyin: https://adoptium.net/
    echo.
    echo Java versiyonu kontrol ediliyor...
    java -version
    echo.
    echo.
    echo Bu pencereyi kapatmak icin bir tusa basin...
    pause >nul
    exit /b 1
)

echo Java bulundu! Versiyon:
java -version 2>&1 | findstr /i "version"
echo.

REM Dosya adini kontrol et - paper-1.20.4.jar
echo [2/3] Paper JAR dosyasi kontrol ediliyor...
if exist "paper-1.20.4.jar" (
    echo Paper 1.20.4.jar bulundu!
    echo.
) else (
    echo.
    echo ========================================
    echo HATA: Paper JAR dosyasi bulunamadi!
    echo ========================================
    echo.
    echo Mevcut klasor: %CD%
    echo.
    echo Bu klasorde su dosyalar var:
    dir /b *.jar 2>nul
    echo.
    echo Lutfen paper-1.20.4.jar dosyasinin bu klasorde oldugundan emin olun.
    echo.
    echo Bu pencereyi kapatmak icin bir tusa basin...
    pause >nul
    exit /b 1
)

REM Sunucuyu baslat
echo [3/3] Sunucu baslatiliyor...
echo.
echo RAM Ayarlari:
echo - Maksimum: 2GB (Xmx2G)
echo - Baslangic: 1GB (Xms1G)
echo.
echo ========================================
echo Sunucu baslatiliyor, lutfen bekleyin...
echo ========================================
echo.
echo NOT: Sunucu kapatmak icin konsola 'stop' yazin
echo.

java -Xmx2G -Xms1G -jar paper-1.20.4.jar nogui

REM Sunucu kapandiginda
echo.
echo ========================================
echo Sunucu kapandi.
echo ========================================
echo.
echo Bu pencereyi kapatmak icin bir tusa basin...
pause >nul
