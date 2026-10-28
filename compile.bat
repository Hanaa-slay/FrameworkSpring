@echo off
setlocal enabledelayedexpansion

cd /d "%~dp0"

echo Nettoyage...
if exist bin rmdir /s /q bin
mkdir bin

echo Recherche des fichiers Java...
rem Chemins entre guillemets avec des / : le dossier du projet contient des espaces
if exist sources.txt del sources.txt
for /r "src\main\java" %%f in (*.java) do (
    set "chemin=%%f"
    echo "!chemin:\=/!">> sources.txt
)

echo Compilation...
javac -parameters -cp "lib/*;." -d bin @sources.txt
if errorlevel 1 (
    del sources.txt
    echo.
    echo Echec de la compilation.
    exit /b 1
)

del sources.txt

echo.
echo =================================
echo  Compilation reussie ^^!
echo  Les .class sont dans : bin/
echo =================================

endlocal
