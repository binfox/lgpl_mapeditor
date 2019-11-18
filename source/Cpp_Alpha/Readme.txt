============================= MAP FILE LOADER =============================


Author:			TheShadow
Programm:		MapEditor
Version:		Professional
Homepage:		http://www.mapeditor.de.vu
eMail:			software@blitzbase.de

About this file:
Author:			DragonMaster
Homepage:		http://www.dragon-master.net
eMail:			stephan@dragon-master.net



In dieser ZIP Datei befindet sich der komplette Source Code zu dem File loader des MAP Dateiformates.

Enthaltene Dateien:
meloader.h / .cpp	Implementiert die MapFile Klasse.
Background.h / .cpp	Implementierung des Background Readers
Layer.h / .cpp		Implementierung des Layer Readers
Object.h / .cpp		Implementierung des Objekt Readers
SubData.h / .cpp	Implementierung des BasisDaten, DatenLayers und MetaDatan Readers
TileSet.h / .cpp	Implementierung des TileSet Readers. Liest auch die Animationsdaten


Um mit dem Loader zu Arbeiten muss in den eigenen Modulen nur die meloader.h eingefügt werden! Die anderen Code Files diehnen nur der Implementierung des Loaders.


Die ist die ProVersion des Map File Loaders. Diese Version ist nicht kompatible mit der Free Version des Map File Loaders, da es geringfügige Änderungen am Interface gibt. Die ProVersion unterstützt die Verschlüsselung der Daten. Beim laden der Datei werden die Daten direkt entschlüsselt. So ist brauch sich der Anwender nicht mehr mit dem Entschlüsseln der Daten beschäftigen.