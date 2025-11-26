@echo off
echo Paper 1.20.4 indiriliyor...
echo.
echo En son build numarasini kontrol ediyorum...
echo.

REM En son build numarasini al
for /f "tokens=*" %%i in ('curl -s "https://api.papermc.io/v2/projects/paper/versions/1.20.4/builds" ^| findstr /C:"build"') do set BUILD_INFO=%%i

echo.
echo Build bilgisi alindi. Direkt indirme linki:
echo.
echo https://api.papermc.io/v2/projects/paper/versions/1.20.4/builds
echo.
echo Yukaridaki linke gidin, en ustteki "build" numarasini bulun.
echo Sonra asagidaki formatta indirin:
echo https://api.papermc.io/v2/projects/paper/versions/1.20.4/builds/[BUILD_NO]/downloads/paper-1.20.4-[BUILD_NO].jar
echo.
echo Ornek: Build 445 ise
echo https://api.papermc.io/v2/projects/paper/versions/1.20.4/builds/445/downloads/paper-1.20.4-445.jar
echo.
pause

