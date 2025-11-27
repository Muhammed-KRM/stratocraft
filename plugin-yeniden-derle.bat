@echo off
echo ========================================
echo Stratocraft Plugin Yeniden Derleme
echo ========================================
echo.

cd /d "%~dp0"

echo [1/3] Eski JAR'ları temizliyorum...
if exist "target\stratocraft-10.0-RELEASE.jar" del /q "target\stratocraft-10.0-RELEASE.jar"
if exist "target\classes\*.class" (
    echo Eski class dosyalarını temizliyorum...
    rmdir /s /q "target\classes\me" 2>nul
)

echo.
echo [2/3] Java derleme komutunu çalıştırıyorum...
echo NOT: Maven yüklü değilse bu adım başarısız olabilir.
echo IDE'den (IntelliJ/Eclipse) derlemeniz önerilir!
echo.

REM Maven wrapper varsa onu kullan
if exist "mvnw.cmd" (
    call mvnw.cmd clean package -DskipTests
) else (
    REM Maven yüklü mü kontrol et
    where mvn >nul 2>&1
    if %errorlevel% == 0 (
        mvn clean package -DskipTests
    ) else (
        echo.
        echo [HATA] Maven bulunamadı!
        echo.
        echo LUTFEN IDE'DEN DERLEYIN:
        echo - IntelliJ IDEA: Maven panel > Lifecycle > package
        echo - Eclipse: Projeye sag tik > Run As > Maven build > Goals: package
        echo.
        pause
        exit /b 1
    )
)

echo.
echo [3/3] JAR dosyasını kontrol ediyorum...
if exist "target\stratocraft-10.0-RELEASE.jar" (
    echo.
    echo [BASARILI] JAR olusturuldu: target\stratocraft-10.0-RELEASE.jar
    echo.
    echo SIMDI YAPMANIZ GEREKENLER:
    echo 1. Sunucuyu kapat (stop yaz)
    echo 2. Eski JAR'i sil: C:\mc\test-server\plugins\stratocraft-10.0-RELEASE.jar
    echo 3. Yeni JAR'i kopyala: target\stratocraft-10.0-RELEASE.jar -> plugins\
    echo 4. Sunucuyu baslat
    echo.
) else (
    echo.
    echo [HATA] JAR olusturulamadi!
    echo IDE'den derlemeniz gerekiyor.
    echo.
)

pause

