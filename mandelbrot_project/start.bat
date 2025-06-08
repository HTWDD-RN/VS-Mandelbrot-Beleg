@echo off
cd /d %~dp0

echo -------------------------------------------------
echo 1) Compile alle Java‐Dateien
echo -------------------------------------------------
javac -encoding UTF-8 *.java
if errorlevel 1 (
  echo ***
  echo *** Kompilierung fehlgeschlagen. Fixe zuerst die Fehler. ***
  echo ***
  pause
  exit /b
)
echo Kompilierung erfolgreich.
echo.

echo -------------------------------------------------
echo 2) Starte Master (port 5000)
echo -------------------------------------------------
start "Master" cmd /k "java Master 5000"

REM Kurze Pause, damit Master das Registry binden kann
timeout /t 2 >nul

echo -------------------------------------------------
echo 3) Starte 4 Worker
echo -------------------------------------------------
start "Worker1" cmd /k "java Worker localhost 5000"
start "Worker2" cmd /k "java Worker localhost 5000"
start "Worker3" cmd /k "java Worker localhost 5000"
start "Worker4" cmd /k "java Worker localhost 5000"

REM Kurze Pause, damit Workers sich beim Master anmelden
timeout /t 2 >nul

echo -------------------------------------------------
echo 4) Starte Client GUI
echo -------------------------------------------------
start "Client" cmd /k "java Client localhost 5000"

echo.
echo *** Alle Prozesse gestartet. ***
echo *** Überprüfe die Fenster Master, Worker1-4 und Client. ***
