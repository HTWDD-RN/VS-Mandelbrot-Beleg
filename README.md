# Verteiltes Mandelbrot-System

Dieses Projekt visualisiert die Mandelbrotmenge in einer grafischen Oberfläche und nutzt dabei eine verteilte Architektur zur Beschleunigung der Berechnungen. Es basiert auf dem Client–Master–Worker-Prinzip mit Java RMI und bietet sowohl eine lokale als auch eine verteilte Ausführung.

## Features

- Live-Zoom in die Mandelbrotmenge mit flüssiger Darstellung
- Verteilte Berechnung über Java RMI mit dynamischer Worker-Registrierung
- Vergleich lokal vs. verteilt mit Zeitmessung und grafischer Auswertung
- Konfigurierbare Parameter: Zoomfaktor, Iterationen, Auflösung, Threads, Layers
- Saubere Trennung in Client, Master und Worker
- Swing-GUI mit Steuerung, Statusanzeige und Zeitmessung

## Systemarchitektur

![Architektur](ARCHITEKTUR.png)

## Beispielhafte Ausführung

```bash
javac *.java

REM Starte Master
start "Master" cmd /k "java Master 5000"

REM Starte mehrere Worker
start "Worker1" cmd /k "java Worker localhost 5000"
start "Worker2" cmd /k "java Worker localhost 5000"
start "Worker3" cmd /k "java Worker localhost 5000"

REM Starte Client
start "Client" cmd /k "java Client localhost 5000"
```

## Ordnerstruktur

```
mandelbrot-verteiltes-system/
├── README.md
├── Dokumentation/
│   ├── Nutzung_und_Aufbau.adoc
│   ├── Architektur
|        ├── ARCHITEKTUR.png
|        ├── Architektur.adoc
│   └── Verteilungsgewinn/
│       ├── Messungen.adoc
|       ├── messwerte/
│          ├── lokal/*.txt
│          └── verteilt/*.txt
│       ├── Diagramme.adoc
│       ├── Interpretation.adoc
│       └── Diagramme/*.png
├── mandelbrot-project/
│   ├── client/
│   ├── master/
│   └── worker/
|   └── startskripte/
│     ├── start.bat
│     └── compile.bat
```

## Hinweise

- Die Zeitmessung erfolgte mit parallelen RMI-Prozessen auf einem einzigen PC (kein echtes Netzwerk).
- Diagramme und Tabellen findest du unter `Dokumentation/Verteilungsgewinn/`.
- Echte verteilte Ausführung würde noch besseren Speedup bringen.

## Lizenz

Dieses Projekt entstand im Rahmen des Moduls **Programmieren verteilter Systeme** und ist für Studienzwecke freigegeben.
