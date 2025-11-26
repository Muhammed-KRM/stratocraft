@echo off
echo ========================================
echo Stratocraft Test Sunucusu Baslatiliyor
echo ========================================
echo.

REM Dosya adını kontrol et - paper-1.20.4-445.jar veya paper-1.20.4.jar olabilir
if exist "paper-1.20.4-445.jar" (
    echo Paper 1.20.4-445 bulundu, baslatiliyor...
    java -Xmx2G -Xms1G -jar paper-1.20.4-445.jar nogui
) else if exist "paper-1.20.4.jar" (
    echo Paper 1.20.4 bulundu, baslatiliyor...
    java -Xmx2G -Xms1G -jar paper-1.20.4.jar nogui
) else (
    echo HATA: Paper JAR dosyasi bulunamadi!
    echo Lutfen paper-1.20.4-445.jar veya paper-1.20.4.jar dosyasinin bu klasorde oldugundan emin olun.
    pause
    exit
)

pause

