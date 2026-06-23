@echo off
setlocal
cd /d "%~dp0"

set "MYSQL=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
set "USUARIO=root"
set "SENHA=root"
set "SCRIPT=wu.sql"

if not exist "%MYSQL%" (
    echo Nao encontrei o mysql.exe em:
    echo   %MYSQL%
    echo Ajuste a variavel MYSQL no topo deste arquivo.
    pause
    exit /b 1
)

if not exist "%SCRIPT%" (
    echo Nao encontrei o arquivo %SCRIPT% nesta pasta.
    pause
    exit /b 1
)

echo Importando %SCRIPT% para o MySQL...
"%MYSQL%" -u %USUARIO% -p%SENHA% < "%SCRIPT%"

if errorlevel 1 (
    echo.
    echo Falha ao importar o banco.
    pause
    exit /b 1
)

echo.
echo Banco importado com sucesso.
endlocal
