# lgpl_mapeditor
2D Mapeditor

Original von The Shadow www.mapeditor.de.vu

Download: https://www.blitzforum.de/upload/file.php?id=13415

Changelog

02.11.2019
Layer werden nun mit der passenden BitTiefe erstellt.

31.10.2019
Layer.bb undo, erase undo. und das klappt sogar
mapeditor.bb Strg+z für undo
toolbar.bb Undobutton eingefügt.

15.10.2019
setup.bb setup_Tilezoom zum einstellen ob die Tiles Skaliert werden sollen Auswählbar aus den Einstellungen
menu.bb Skalierung benutzen
window.bb Fenster initialgröße
splitter.bb splitter_update(mode) geht nun bis 1200 zu ziehen 
tile_prog.bb toolbar Tileeditor repariert


14.10.2019
language.ini &toolbar.bb -> hinttext für neue Symbole
editor_click &editor_draw $ Event $0201  Radierer
Verison als Const von map.bb nach mapeditor.bb und auf 1.411 gesetzt
map_save & mapeditor.bb & Event $4001 Timer Hinzugefügt für das Automatische speichern von Änderungen

13.10.2019
editor.bb & editor_zoom Einstellbare Zoomstufen aus der Config
toolbar.bb Toolbar Nummerierung geändert
toolbar.bb & map.bb & maps_saveas Speichern zu überschreiben geändert und speichern unter erstellt

12.10.2019
map.bb Version auf 1.41 gesetzt
toolbar.bb Toolbar repariert
Event $0204 Mausrad zum Zoomen
extra_splash HTML Gadget entfernt (Machte probleme bei Windows 10)
editor_slide Sliden nun richtig herum
event $0201 & event $0202 - 2. Maustaste zum scrollen
