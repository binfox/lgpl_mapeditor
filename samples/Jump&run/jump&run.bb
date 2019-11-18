;//////////////////////////////////////////////////////////////
;//                                                          //
;// PLEASE COPY MAPENGINE-FOLDER INSIDE THIS EXAMPLE-FOLDER  //
;// BITTE KOPIERE MAPENGINE-ORDNER IN DIESEN BEISPIEL-ORDNER //
;//                                                          //
;//////////////////////////////////////////////////////////////
;//                                                          //
;// PLEASE START ONLY THIS MAIN-PROGRAM FILE AND NOT INCLUDE //
;// BITTE STARTE NUR DIESE HAUPTDATEI UND NICHT INCLUDE      //
;//                                                          //
;//////////////////////////////////////////////////////////////
;//                                                          //
;// SOME MODIFICATIONS FOR BB2D/3D NEEDED. REMOVE LAST       //
;// PARAMETER FROM LOADIMAGE OR LOADANIMIMAGE-LINES WITH     //
;// ERRORS AND START ONLY THIS FILE!
;//                                                          //
;// EINIGE MODIFIKATIONEN DER MAPENGINE FÜR BB2D/3D SIND     //
;// NOTWENDIG. ENTFERNE DEN LETZTEN PARAMETER BEI LOADIMAGE  //
;// ODER LOADANIMIMAGE BEI ZEILEN MIT FEHLERN UND STARTE     //
;// NUR DIESE DATEI!                                         //
;//                                                          //
;//////////////////////////////////////////////////////////////





; -----------------------------------------
; Universal Mapeditor Jump'n'Run Sample
; by Edlothiol
; -----------------------------------------
; Using SpriteLib GPL by Ari Feldman
; ari@arifeldman.com
; -----------------------------------------
; Should run in Blitz3D and BlitzPlus
; -----------------------------------------


; -----------------------------------------
; Universal Mapeditor Jump'n'Run Beispiel
; von Edlothiol
; -----------------------------------------
; Benutzt die SpriteLib GPL von Ari Feldman
; ari@arifeldman.com
; -----------------------------------------
; Sollte sowohl unter Blitz3D als auch 
; unter BlitzPlus laufen
; -----------------------------------------

Graphics 640,480,16
SetBuffer BackBuffer()

; Gehört zur Lizenz der SpriteLib GPL
; This is because of the usage of SpriteLib GPL
Text GraphicsWidth()/2,GraphicsHeight()/2 - FontHeight(),"BlitzBasic and Universal Mapeditor example by Edlothiol",True,True
Text GraphicsWidth()/2,GraphicsHeight()/2,"Game artwork created by Ari Feldman ari@arifeldman.com",True,True
Flip
time = MilliSecs()
While MilliSecs() < time + 3000
	If GetKey() Then Exit
Wend

Include "mapengine\mapengine.bb"

; Gegnerarten
; Enemy types
Const EnemyCount = 2
;    Bildname      HP   Anzahl Animationsschritte  Breite  Höhe  Geschwindigkeit Kollisionsschaden
; Image Filename   HP   Count of Animation Steps   Width   Height Speed          Damage on collision
Data "snail.png",  10,             4,              32,     31,   0.3,              100
Data "fly.png",    10,						 6,							 39,     30,	 1.5,							 20

; Zu Speichern der Gegnerarten
; A Type for the Enemy types...
Type TEnemyType
	Field img,hp,animcount,w,h,vel#,colldmg
End Type

Dim Enemies.TEnemyType(EnemyCount-1)
For in = 0 To EnemyCount-1
	Enemies(in) = New TEnemyType
	Read imgfilename$
	Read Enemies(in)\hp, Enemies(in)\animcount, Enemies(in)\w, Enemies(in)\h, Enemies(in)\vel, Enemies(in)\colldmg
	Enemies(in)\img = LoadAnimImage(imgfilename,Enemies(in)\w,Enemies(in)\h,0,Enemies(in)\animcount * 2)
Next

; Ein Stein
; A Stone
Type TStone

	Field x,y
	Field dx,dy ; Bewegung / Movement
	Field gravitation#

End Type

; Ein Gegner
; An enemy
Type TEnemy

	Field x#,y#
	Field HP
	Field path.geo
	
	Field dir,frame# ; Dir: 0 = Rechts 1 = Links / 0 = Right 1 = Left

End Type

; Eine Plattform
; A platform
Type TPlatform

	Field x#,y#
	Field path.geo
	Field dir

End Type

; Steinbild
; The image of the stone
Global Stone = LoadImage("stone.png")
MidHandle Stone

; Spielerdaten
; Player data
Global PlayerX#
Global PlayerY#
Global PlayerDX#, PlayerDY#
Global PlayerHP
Global PlayerPoints
Global PlayerGravitation#
Global PlayerLives = 3
Const MaxHP = 100
Const PlayerFramesPerDirection = 25

Global PlayerAmmo = 4 ; Munition (Steine) / Ammo (Stones)
Global PlayerAction
Const ActionJumping = 1
Const ActionThrowing = 2

Global OldPlayerX, OldPlayerY

; Animation des Spielers
; Animation of the player
Global PlayerStep#
Global PlayerAnim

; Bild des Spielers
; Image of the player
Global PlayerImg = LoadAnimImage("char.png",55,64,0,PlayerFramesPerDirection*2)

; Plattform - Bild
; Image of the platform
Global Platform = LoadImage("platform.png")
MidHandle Platform


; Die Spielfunktion
; The game function
Function Game(map$)

	; Spieler initialisieren
	; Initialize player
	PlayerX = 20
	PlayerY = 20 * 32 / 2 - ImageHeight(PlayerImg) / 2
	PlayerHP = MaxHP
	PlayerGravitation = 1
	PlayerDX = 0
	PlayerDY = 0
	
	PlayerStep = 0
	PlayerAnim = 0
	
	; Karte laden
	; Load map
	If map_load(map) > 0 Then RuntimeError("Karte konnte nicht geladen werden!")
	
	bg_layer.layer = GetLayerByName("BG")
	
	; Alle vorherigen Gegner löschen
	; Delete all enemies
	Delete Each TEnemy
	; Alle vorherigen Plattformen löschen
	; Delete all platforms
	Delete Each TPlatform
	; Gegner setzen nach den geometrischen Formen, die im Level vorkommen (ausgenommen die, die eine "Plattform" darstellen)
	; The lines in the map named "p" get platforms,
	; the other lines enemies
	For g.geo = Each geo
		If g\name <> "p" Then
	
			e.TEnemy = New TEnemy
			e\path = g
			e\x = g\posx
			e\y = g\posy
			e\hp = Enemies(g\name)\hp
			
		Else
		
			p.TPlatform = New TPlatform
			p\path = g
			p\x = g\posx
			p\y = g\posy
		
		End If	
	Next
	
	; Hauptschleife
	; Main loop
	While Not KeyHit(1)
	
		Cls
		
		; Alte Spielerposition sichern
		; Save old player position
		OldPlayerX = PlayerX
		OldPlayerY = PlayerY
		
		; Klettern
		; Scale
		layer_map_coord(bg_layer,PlayerX - map_scrollx + ImageWidth(PlayerImg) / 2,PlayerY - map_scrolly + ImageHeight(PlayerImg)/2)
		If layer_getdata(bg_layer,layer_x,layer_y) = 1 Then ; Erkletterbar / Scalable
		
			Klettern = True
			
		Else
		
			Klettern = False
		
		End If
		
		If layer_getdata(bg_layer,layer_x,layer_y) = 2 Then ; Spielende / Game over
			Return True
		End If
		
		If layer_getdata(bg_layer,layer_x,layer_y) = 3 Then ; Tod / Death
			KillPlayer()
		End If
		
		; Spieler nach unten bewegen
		; Move player downwards
		If Klettern = False
			If PlayerDY < 0 Then
				PlayerDY = PlayerDY + PlayerGravitation
				If PlayerDY > 0 Then PlayerDY = 0
			Else
				PlayerY = PlayerY + PlayerGravitation
			End If
			PlayerGravitation = PlayerGravitation + 0.2
		End If
		; Test on collision
		If CollidePlayer() <> 0 Then
		
			PlayerX = OldPlayerX
			PlayerY = OldPlayerY
			PlayerGravitation = 1
			PlayerDX = PlayerDX * 0.8
			If PlayerAction = ActionJumping Then PlayerAction = 0
			
			; Diese Variable wird gebraucht, damit man nicht in der Luft springen kann
			; You can only jump when on ground
			OnGround = True
		
		Else
		
			OnGround = False
		
		End If
		
		; Spieler steuern
		; Control player
		If Klettern Or (PlayerAction = 0) Then
			If KeyDown(203) Then; Links / Left
				PlayerX = PlayerX - 3
				PlayerAnim = PlayerFramesPerDirection
				PlayerStep = (PlayerStep + 0.1) Mod 7
			ElseIf KeyDown(205) ; Rechts / Right
				PlayerX = PlayerX + 3
				PlayerAnim = 0
				PlayerStep = (PlayerStep + 0.1) Mod 7
			Else
				PlayerStep = 8
			End If
			
			If KeyHit(28) And PlayerAmmo > 0 Then ; Return - Werfen / Return - Throw
			
				PlayerAction = ActionThrowing
				PlayerStep = 22
			
			End If
			
		End If
		
		If KeyHit(200) Then ; Springen / Jump
		
			If OnGround Then
				PlayerDX = 3 * KeyDown(205) + 3 * KeyDown(203)
				If PlayerAnim >= PlayerFramesPerDirection Then PlayerDX = -PlayerDX
				PlayerDY = -20
				PlayerAction = ActionJumping
				PlayerStep = 15
				OnGround = False
			End If
		
		End If
		
		PlayerX = PlayerX + PlayerDX
		PlayerY = PlayerY + PlayerDY
		
		; Sicht auf Spieler zentrieren
		; Center view on player
		map_scrollx = PlayerX - GraphicsWidth() / 2
		map_scrolly = PlayerY - GraphicsHeight() / 2
		If map_scrollx < 0 Then map_scrollx = 0
		If map_scrolly < 0 Then map_scrolly = 0
		If map_scrollx > (bg_layer\sizex * 32 - GraphicsWidth()) Then map_scrollx = (bg_layer\sizex * 32 - GraphicsWidth())
		If map_scrolly > (bg_layer\sizey * 32 - GraphicsHeight()) Then map_scrolly = (bg_layer\sizey * 32 - GraphicsHeight())
		
		; Ein zweites Mal Kollision
		; A second collision test
		If CollidePlayer() = 1 Then
		
			; Damit man nicht so leicht hängenbleibt und auch leichte schiefe Ebenen "erklimmen" kann
			; This is for walking up hills etc.
			PlayerY = PlayerY - 3
		
		End If
		
		If CollidePlayer() <> 0 Then
	
			PlayerX = OldPlayerX
			PlayerY = OldPlayerY
			
			If PlayerAction = ActionJumping Then PlayerAction = 0
			
		End If
		
		; Klettern
		; Scaling
		If KeyDown(200) Then ; Oben / Up
			If Klettern = True Then
			
				PlayerY = PlayerY - 1
				PlayerDX = 0
				PlayerDY = 0
			
			End If
		End If
		If KeyDown(208) Then ; Unten / Down
			If Klettern = True Then
			
				PlayerY = PlayerY + 1
				PlayerDX = 0
				PlayerDY = 0
			
			End If
		End If
		
		; Items einsammeln
		; Taking items
		layer_map_coord(GetLayerByName("Items"),PlayerX - map_scrollx + ImageWidth(PlayerImg) / 2,PlayerY - map_scrolly + ImageHeight(PlayerImg)/2)
		
		Select layer_getdata(GetlayerByName("Items"),layer_x,layer_y) ; Verschiedene Items
		Case 1: ; Essen / Food
			PlayerHP = PlayerHP + 5
		
		Case 2: ; Punkte / Points
			PlayerPoints = PlayerPoints + 1
			
		Case 3: ; +3 Munition / +3 Ammo
			PlayerAmmo = PlayerAmmo + 3
			
		Case 4: ; Ein Leben / One life
			PlayerLives = PlayerLives + 1
		
		End Select
		
		; Und das Item erntfernen
		; Remove the item
		If layer_getdata(GetLayerByName("Items"),layer_x,layer_y) > 0 Then
			layer_setvalue(GetLayerByName("Items"),layer_x,layer_y,0,0)
			layer_setdata(GetLayerByName("Items"),layer_x,layer_y,0)
		End If
		
		; Sprung und Wurf animieren
		; Animate jump and throwing
		Select PlayerAction
		Case ActionJumping:
			PlayerStep = PlayerStep + 0.1
			If PlayerStep > 19 Then PlayerStep = 19
			If OnGround Then
				PlayerStep = 8
			End If
			
		Case ActionThrowing:
			PlayerStep = PlayerStep + 0.1
			If PlayerStep > 24 Then
				; Abwerfen
				; Throw
				s.TStone = New TStone
				s\x = PlayerX + ImageWidth(PlayerImg) * (PlayerAnim < PlayerFramesPerDirection) ; Wenn nach rechts geworfen wird, muss noch die Breite des Spieler hinzuaddiert werden
				s\y = PlayerY
				s\dx = 5
				If PlayerAnim >= PlayerFramesPerDirection Then s\dx = -s\dx
				s\dy = -2
				
				PlayerAmmo = PlayerAmmo - 1
				PlayerAction = 0
			End If

		End Select
		
		; Tiles animieren
		; Animate tiles
		tile_animate()
		; Karte zeichnen
		; Draw map
		map_draw()
		
		; Charakter auf die Karte zeichnen
		; Draw player
		DrawImage PlayerImg, PlayerX - map_scrollx, PlayerY - map_scrolly, Floor(PlayerStep) + PlayerAnim
		
		; Steine bewegen
		; Update stones
		Stones
		
		; Gegner bewegen
		; Update enemies
		Enemy
		
		; Plattformen
		; Update platforms
		Platforms()
		
		; HP - Anzeige
		; Show HP etc.
		Text 0,0,"HP: " + PlayerHP + " Points: " + PlayerPoints + " Ammo: " + PlayerAmmo + " Lives: " + PlayerLives
		
		; Cheat - Taste C bringt ins nächste Level :)
		; Press C to get to next level :)
		If KeyHit(46) Then Return True
		
		; Ist der Spieler überhaupt noch auf der Karte? Sonst töten
		; Kill the player if he's not on the map anymore
		If layer_map_coord(GetLayerByName("BG"),PlayerX - map_scrollx,PlayerY - map_scrolly,True) = 0 And PlayerY >= 0 Then
			KillPlayer()
		End If
		
		If PlayerHP <= 0 Then KillPlayer()
		
		Flip
	
	Wend

End Function

level = 1

While Game("level" + level + ".map")

	level = level + 1
	If FileType("level" + level + ".map") <> 1 Then End

Wend

; Spieler verliert ein Leben
; Player loses one life
Function KillPlayer()

	PlayerLives = PlayerLives - 1
	
	PlayerX = 20
	PlayerY = 20 * 32 / 2 - ImageHeight(PlayerImg) / 2
	PlayerHP = MaxHP
	PlayerGravitation = 1
	PlayerDX = 0
	PlayerDY = 0
	
	PlayerStep = 0
	PlayerAnim = 0
	
	If PlayerAmmo < 2 Then PlayerAmmo = 2
	
	If PlayerLives = -1 Then RuntimeError("Game Over!")
	
	Text GraphicsWidth()/2,GraphicsHeight()/2,"You have lost one life. Press a key...",True,True
	Flip
	WaitKey

End Function


; Gibt einen Layer mit dem entsprechenden Namen zurück
; Return the layer with the name$
Function GetLayerByName.layer(name$)

	For l.layer = Each layer
		If l\name = name Then Return l
	Next
	
	Return Null
End Function

Function CollidePlayer()

	If layer_map_collision(GetLayerByName("Map"),PlayerX - map_scrollx,PlayerY - map_scrolly,PlayerImg,0) Then Return True
	
	For p.TPlatform = Each TPlatform
		If ImagesCollide(Platform,p\x,p\y,0,PlayerImg,PlayerX,PlayerY,0) Then Return 2
	Next
	
	Return False

End Function

Function Stones()

	For s.TStone = Each TStone
	
		s\x = s\x + s\dx
		s\y = s\y + s\dy
		
		s\y = s\y + s\gravitation
		s\gravitation = s\gravitation + 0.15
		
		DrawImage Stone, s\x-map_scrollx,s\y-map_scrolly
		
		; Kollision mit der Karte
		; Collide with map
		If layer_map_collision(GetLayerByName("Map"),s\x - map_scrollx,s\y - map_scrolly,Stone,0) Then
		
			Delete s
		
		End If
	
	Next

End Function

Function Enemy()

	For e.TEnemy = Each TEnemy
	
		e\frame = e\frame + Enemies(e\path\name)\vel * 0.2
		If e\frame > Enemies(e\path\name)\animcount Then e\frame = 0
		
		; Auf dem Pfad weiterbewegen
		; Continue moving on path
		e\x = NextXOnPath(e\path,e\x,e\y,e\dir,Enemies(e\path\name)\vel)
		e\y = NextYOnPath(e\path,e\x,e\y,e\dir,Enemies(e\path\name)\vel)
		e\dir = NextDirOnPath(e\path,e\x,e\y,e\dir,Enemies(e\path\name)\vel)
		
		DrawImage Enemies(e\path\name)\img, e\x-map_scrollx,e\y-map_scrolly, Floor(e\frame) + e\dir * Enemies(e\path\name)\animcount
	
		; Kollision mit dem Spieler
		; Collide with player
		If ImagesCollide(Enemies(e\path\name)\img, e\x,e\y, Floor(e\frame) + e\dir * Enemies(e\path\name)\animcount, PlayerImg,PlayerX,PlayerY,Floor(PlayerStep) + PlayerAnim) Then
		
			PlayerHP = PlayerHP - Enemies(e\path\name)\colldmg	
			
			; Spieler zurückschleudern, damit er am Leben bleibt :)
			; Throw player back so he stays at life :)
			PlayerDX = (Not e\x > PlayerX) * 10 + (e\x > PlayerX) * -10
		
		End If
		
		For s.TStone = Each TStone
		
			If ImagesCollide(Enemies(e\path\name)\img, e\x,e\y, Floor(e\frame) + e\dir * Enemies(e\path\name)\animcount, Stone, s\x,s\y,0) Then
			
				Delete s
				e\hp = e\hp - 10
				If e\hp <= 0 Then Delete e
			
			End If
		
		Next
	
	Next

End Function

Function Platforms()

	For p.TPlatform = Each TPlatform
	
		oldx = p\x
		oldy = p\y
	
		p\x = NextXOnPath(p\path,p\x,p\y,p\dir,0.8)
		p\y = NextYOnPath(p\path,p\x,p\y,p\dir,0.8)
		p\dir = NextDirOnPath(p\path,p\x,p\y,p\dir,0.8)
		
		; "Mitfahren" lassen - das ist ein bisschen buggy, er "rutscht"
		; Let the player "ride" on the platform - There's a bug, the player "slides"
		If ImagesCollide(Platform,p\x,p\y,0,PlayerImg,PlayerX,PlayerY,0) Then
		
			PlayerX = PlayerX + p\x - oldx
			PlayerY = PlayerY + p\y - oldy
			
			OldPlayerX = PlayerX + p\x - oldx
			OldPlayerY = PlayerY + p\y - oldy
		
		End If
	
		DrawImage Platform, p\x-map_scrollx, p\y-map_scrolly
	
	Next

End Function

; Funktionen für Bewegung auf Pfaden, sind nur für einfache Linien implementiert
; Functions for moving on paths - they are only implemented for simple lines, not ovals, rects etc.
Function NextXOnPath#(path.geo,x#,y#,dir,vel#)

	Select path\code
	Case layer_line:
		If dir = 0 Then
		
			Return x - vel * Sgn(x - path\sizex)
		
		Else
		
			Return x - vel * Sgn(x - path\posx)
		
		End If
		
		
	End Select

End Function

Function NextYOnPath#(path.geo,x#,y#,dir,vel#)

	Select path\code
	Case layer_line:
		If dir = 0 Then
		
			Return y - vel * Sgn(y - path\sizey)
		
		Else
		
			Return y - vel * Sgn(y - path\posy)
		
		End If
		
		
		
	End Select

End Function

Function NextDirOnPath#(path.geo,x#,y#,dir,vel#)

	Select path\code
	Case layer_line:
		If dir = 0 Then
		
			If (x + vel) > path\sizex And (x - vel) < path\sizex And (y + vel) > path\sizey  And (y - vel) < path\sizey Then
				Return 1 - dir
			End If
		
		Else
		
			If (x + vel) > path\posx And (x - vel) < path\posx And (y + vel) > path\posy  And (y - vel) < path\posy Then
				Return 1 - dir
			End If
		
		End If
		
	End Select
	
	Return dir

End Function