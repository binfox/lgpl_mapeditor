
;----------------------------------------------
;-     Beispiel f�r den Mapeditor von TS      -
;-RPG Grafiken mit Kollisionen und Pfadfindung-
;-             und levelwechsel               -
;-              von Jan Lahr Kuhnert               -
;----------------------------------------------

;----------------------------------------------
;-     Example vor the Mapeditor from TS      -
;-RPG Graphics with Collisions And Pathfinging-
;-             and levelswitch                -
;-              by Jan Lahr Kuhnert                -
;----------------------------------------------

;----------------------------------------------
;-      Programmed in Blitz Basic Plus        -
;----------------------------------------------




;Include the Mapengine
;L�dt die Zeichen routine, die beim Editor dabei ist.
Include "mapengine\mapengine.bb"

;Map, to load
;Karte die Zu laden ist


Global startx,goneX,posx,starty,goneY,posy,animframe,endx,endy,gox,goy

Type lvl_wechsel
	Field x
	Field y
	Field ziel$
End Type

Dim nodemap(0,0)
Dim map(0,0)



;some thing for the pathfinding
;paar was f�r die Pfadfindung
Global mapwidth
Global mapheight

Dim dirx(7) 
Dim diry(7) 
Dim dirz(7) 

Data 0,-1,0, -1,0,0, 1,0,0, 0,1,0, -1,-1,1, 1,-1,1, -1,1,1, 1,1,1 

For i=0 To 7 
	Read dirx(i) 
	Read diry(i) 
	Read dirz(i) 
Next 


; Create a window in the center of the Desktop
; Erstellt ein Fenster in der Mitte des Desktop
Fenster=CreateWindow ("Jans RPG Example", ClientWidth(Desktop())/2-200, ClientHeight(Desktop())/2-212, 401,450,0,11)

;Canvas
;erstellt das Sichfenster
Canvas=CreateCanvas (1,5, 390, 390, Fenster )
SetGadgetLayout Canvas,1,1,1,1


;timer for the fps
;Uhr, f�r die Frames pro Sekunde
Timer=CreateTimer (500)
;Without timer modern PC's have 200 and more FPS!
;ohne Timer Haben Moderne Rechner 200 und mehr Bilder pro sekunde!

loadlevel("Data\Jan1.map")


;Load the picture from the Hero
;l�dt das Bild, des Helden
Global Held_Pic = LoadAnimImage("Data\Held_jan.bmp",24,32,0,12)
MaskImage Held_pic,255,0,255

;Canvas sizes
;Zeichenfl�sche gr��en
Const width=390
Const height=390

;

;fill a string for the Statustext
;ein String f�r den Statustext
Werbung$ = "Blitzforum.de Yourlahr.de"

;init. the Backbuffer
;l�dt den Backbuffer
SetBuffer CanvasBuffer (Canvas)

;Main part
;Hauptschleife
While Not KeyHit(1)

	;Wait for Windows events
	;Wartet auf Windows Erreignisse
	Repeat 
	Event = WaitEvent ()
	If event = $803 Then End
	Until event = $4001

	;Make some comercial for the Statustext
	;so nun mal ein wenig Werbung :-)
	g$=""
	For m = 0 To i/5
		g$ = g$ + " "
	Next
	SetStatusText (Fenster, g$+Werbung$)
	i = i + h
	If i = 250 Then
		h = -1
	ElseIf i =0 Then
		h = 1
	ElseIf h = 0 Then
		h=1
	EndIf
;DebugLog 4
	;center the player in the Canvas
	;h�llt den Spieler mittig
	map_scrollx= (StartX * 16) - (width /2 - 8)+posx+goneX
	map_scrolly= (Starty * 16) - (height /2 - 8)+posy+goneY

	;Mapeditor routins
	;Mapeditor routinen
	tile_animate()
	draw_map()

	;calculate some things for the Mouse Position
	;errechnet die Mausposition
	realmouseX =  MouseX()-(GadgetX (fenster)+GadgetX (canvas))
	realmouseY =  MouseY()-(GadgetY (fenster)+GadgetY (canvas))
	FaktorX# = Float(width)/Float(GadgetWidth (canvas))
	Faktory# = Float(height)/Float(GadgetHeight (canvas))

	;find the path and move at Mousehit
	;l�sst die Figur beim Tastendruck laufen
	If MouseDown(1) Then 
		endx = ((realmouseX*FaktorX#)-posx+map_scrollx+8)/16-1 
		endy =  ((realmouseY*FaktorY#)-posy+map_scrolly)/16-2
		If endx > 0 And endy > 0 And endx < mapwidth And endy < mapheight Then
			If map(endx,endy) = 0 Then pathfinding(endx,endy,startx,starty) 
		EndIf
	EndIf 
	If goX*16 < (startX*16)+GoneX Then goRealX# = goRealX#-(1.0/FPS*40.0): Animfloat# = Animfloat# + (z#/FPS): Animframe = 9 + (Animfloat#/4)
	If goX*16 > (startX*16)+GoneX Then goRealX# = goRealX#+(1.0/FPS*40.0): Animfloat# = Animfloat# + (z#/FPS): Animframe = 3 + (Animfloat#/4)
	If goY*16 < (startY*16)+GoneY Then goRealY# = goRealY#-(1.0/FPS*40.0): Animfloat# = Animfloat# + (z#/FPS): Animframe = 0 + (Animfloat#/4)
	If goY*16 > (startY*16)+GoneY Then goRealY# = goRealY#+(1.0/FPS*40.0): Animfloat# = Animfloat# + (z#/FPS): Animframe = 6 + (Animfloat#/4)
	goneX = goRealX#
	goneY = goRealY#
	If goneX >=16 Then StartX = StartX+1:goRealX# = 0:gonex=0
	If goneY >=16 Then StartY = StartY+1:goRealY# = 0:goney=0
	If goneX <=-16 Then StartX = StartX-1:goRealX# = 0:gonex=0
	If goneY <=-16 Then StartY = StartY-1:goRealY# = 0:goney=0
	If Int(Animfloat#) >= 9 Then 
		z# = -40
	ElseIf Int(Animfloat#) = 0 Then 
		z# = 40
	ElseIf Int(Animfloat#) <= 2 Then 
		z# = 40
	EndIf


	
	
	;find the path
	;Pfadfinde Ergenisse auswerten
	If goneX = 0 And goneY = 0 Then
		For node.node=Each node 
			If node\parent<>Null Then 
				If node\X=  startx And node\Y =  starty Then
					gox = node\parent\x
					goy = node\parent\y
				EndIf	
			EndIf 
		Next 
	EndIf

	;Check if the player Collide with the Levelout
	;testet, ob der Spieler mit dem Levelausgang kollidiert
	For lvl.lvl_wechsel = Each lvl_wechsel
		If startx = lvl\x And starty = lvl\y Then 
			NewLvl$ = "Data\"+lvl\ziel$+".map"
			DebugLog lvl\ziel$+".map"
			loadlevel(NewLvl$)
		EndIf
	Next

	;Show the FPS
	;Zeigt die Bilder pro Sekunde
	t3 = t3+1:If t3 = 4 Then t3=0:t2=t1:t1 = MilliSecs():Fps = 5000.0/(t1-t2)
	Text 0,0,"FPS: " + FPS
	
	;Flip Buffers
	;Zeichnet flimmerfreies Bild
	FlipCanvas Canvas,0
Wend
End

; The function from TS, but with Player drawing
; Die function aus der Mapengine, nur mit Spieler zeichenen
Function draw_map()
  Local b
  Local g
  Local layer.layer
  Local px
  Local py
  Local r

  Viewport map_x, map_y, map_width, map_height
  Origin map_x, map_y

  If map_visible=1 Then
	If map_mode=2 And map_image<>0 Then
      px=map_getscreen(map_posx,map_parax,map_scrollx)
      py=map_getscreen(map_posy,map_paray,map_scrolly)
      TileBlock map_image,px,py
    ElseIf map_mode=3 And map_image<>0 Then
      px=map_getscreen(map_posx,map_parax,map_scrollx)
      py=map_getscreen(map_posy,map_paray,map_scrolly)
      TileImage map_image,px,py
    EndIf
  EndIf

  For layer=Each layer
    If layer\visible=1 Then
      Select layer\code
        Case layer_map
          layer_map_draw(layer)
        Case layer_clone
          layer_clone_draw(layer)
        Case layer_image
          layer_image_draw(layer)
        Case layer_block
          layer_block_draw(layer)
      End Select

    EndIf
	;Draw the Player
	;zeichnen des Spielers!
	If layer\name$ = "Player" Then 
		Color 0,0,255 
		Oval (endx*16)+posx-map_scrollx,(endy*16)+posy-map_scrolly,16,16,0
		Rect (endx*16)+posx-map_scrollx+7,(endy*16)+posy-map_scrolly,2,16,0
		Rect (endx*16)+posx-map_scrollx,(endy*16)+posy-map_scrolly+7,16,2,0 			
		Color 255,0,0 
		Oval (startx*16)+goneX+posx-map_scrollx,(starty*16)+goneY+posy-map_scrolly,16,16,0
		DrawImage Held_Pic,(startx*16)+goneX+posx-map_scrollx-4,(starty*16)+goneY+posy-map_scrolly-16,Int(Animframe )
	EndIf
  Next
End Function

Function loadlevel(Levelname$)
;Load the Map
;L�dt die Karte
error=map_load(Levelname$,"")

;Check for errors
;Verarbeitet die Fehler
Select error
	Case 1
		Notify "File not found"
	Case 2
		Notify "File size corrupted"
	Case 3
		Notify "No read access"
	Case 4
		Notify "Not valid file"
	Case 5
		Notify "Checksum problem"
	Case 6
		Notify "Password problem"
	Case 7
		Notify "Load image problem"
End Select
If error>0 Then End

;read some data from the mapengine
;liest Daten aus dem Types von TS's Mapengine
For node.node=Each node 
	Delete node
Next  
For lvl.lvl_wechsel =Each lvl_wechsel 
	Delete lvl
Next 

For layer.layer = Each layer
	;Data for the Collisions
	;Daten f�r die Kollisionen
	If layer\name$  = "Player" Then 
		mapwidth =layer\sizeX
		mapheight =layer\sizeY
		Dim nodemap(mapwidth,mapheight)
		PosX = layer\Posx
		PosY = layer\posy
		Dim map(layer\sizeX,layer\sizeY)
			For x = 0 To layer\sizeX
				For y = 0 To layer\sizeY
					map(X,Y)= layer_getvalue(layer,x,y)
				Next
			Next
	EndIf

	;Read the start position from the player
	;liest die Player Position
	If layer\name$ = "Start" Then
		startx = (layer\posx)/16-posx/16
		StartY = (layer\posy)/16-posy/16
		Endx = startx
		endy = StartY
		gox = startX
		Goy = StartY
	EndIf 
	
	;find the exit of the map
	;findet die Stellen, wo ein Levelwechsel Statfinden soll.
	If Left(layer\name$,3) = "lvl" Then
		lvl.lvl_wechsel = New lvl_wechsel
		lvl\x = (layer\posx)/16-(posx/16)
		lvl\Y = (layer\posy)/16-(posy/16)
		lvl\ziel$ = Right(layer\name$,Len(layer\name$)-3)
	EndIf 
Next
End Function

;the Pathfinding routine
;die Pfadfinde Routine
Type node 
	Field parent.node 
	Field cost 
	Field x 
	Field y 
End Type 

Type open 
	Field node.node 
End Type 

Type path 
	Field node.node 
End Type 

;you can change it with the 8 way routine, easyly
;man can diese durch die 8 wege Routine einfach austauschen.
Function pathfinding(startx,starty,endx,endy)
  Delete Each node
  Delete Each open
  Delete Each path
  Dim nodemap(mapwidth,mapheight)
  If startx=endx And starty=endy Then Return

  node.node=New node
  node\x=startx
  node\y=starty
  open.open=New open
  open\node=node
  nodemap(startx,starty)=1

  .again0
  node=Null
  cost=2147483647
  For open=Each open
    delta=Abs(open\node\x-endx)+Abs(open\node\y-endy)
    If open\node\cost+delta<cost Then
      cost=open\node\cost+delta
      node=open\node
      tempopen.open=open
    EndIf
  Next
  If node=Null Then Return
  Delete tempopen

  For i=0 To 3
    x=node\x+dirx(i)
    y=node\y+diry(i)
    If x=>0 And y=>0 And x<=mapwidth And y<=mapheight Then
      If map(x,y)=0 And nodemap(x,y)=0 Then
        tempnode.node=New node
        tempnode\parent=node
        tempnode\cost=node\cost+1
        tempnode\x=x
        tempnode\y=y
        open.open=New open
        open\node=tempnode
        nodemap(x,y)=1
        If x=endx And y=endy Then finish=1:Exit
      EndIf
    EndIf
  Next
  If finish=0 Then Goto again0

  While tempnode\parent<>Null
    path.path=New path
    path\node=tempnode
    tempnode=tempnode\parent
  Wend
  path.path=New path
  path\node=tempnode
End Function