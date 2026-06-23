@echo off
setlocal
cd /d "%~dp0"

echo Compilando...
if not exist bin mkdir bin

dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -cp "lib/*" -d bin @sources.txt
del sources.txt

if errorlevel 1 (
    echo.
    echo Falha na compilacao.
    pause
    exit /b 1
)

echo Executando...
java -cp "bin;lib/*" app.Main
endlocal
