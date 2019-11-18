'Strict

Framework brl.max2d
Import brl.glmax2d
Import brl.d3d7max2d
Import jan.fps
Import brl.pngloader
Import brl.jpgloader
Import brl.bank
Import brl.bmploader
Import brl.retro
Import jan.mapengine
'Include "Mapmodule.bmx"


'Graphic init
'{
	AppTitle$ = "Mapengine for www.mapeditor.de.vu -Techblood, Pixelfraks.org -"
	SetGraphicsDriver(GLMax2DDriver())
'	SetGraphicsDriver(D3D7Max2DDriver())
	Graphics 800,600,,60
	SetBlend( ALPHABLEND )
'}


Local width=GraphicsWidth()
Local height=GraphicsHeight()
Local x#,y#,sms#
sms#=MilliSecs()
Local akttime#,alpha#

SetAlpha 1
'Include "Mapmodule.bmx"

'Load_map_with_debug("Jan2.map","test123")
'Load_map_with_debug("Jan1.map")
Load_map_with_debug("darkw.map")
SetClsColor 0,0,0
Local oldmx%,oldmy%
MoveMouse width/2,height/2
While not KeyHit(KEY_ESCAPE)
	If KeyDown(KEY_UP)=1 Then map_scrolly=map_scrolly+height/10 'up
	If KeyDown(KEY_DOWN)=1 Then map_scrolly=map_scrolly-height/10 'down
	If KeyDown(KEY_LEFT)=1 Then map_scrollx=map_scrollx+width /10 'left
	If KeyDown(KEY_RIGHT)=1 Then map_scrollx=map_scrollx-width /10 'right
	If MouseHit(1)
		oldmx=MouseX()
  	oldmy=MouseY()
	EndIf

	If MouseDown(1)
	  Local mx=MouseX()
  	Local my=MouseY()
	  'MoveMouse width/2,height/2
  	map_scrollx=map_scrollx+((mx-oldmx)*100.0/get_current_fps())
	  map_scrolly=map_scrolly+((my-oldmy)*100.0/get_current_fps())
	EndIf
  
  'Cls
	tile_animate()
	map_draw()
	DrawText "X:"+map_scrollx+"  Y:"+map_scrolly,0,0
	update_FPS(250)
	DrawText get_user_FPS(),0,30
'  FlushMem()
	Flip 0
Wend

Function Load_map_with_debug(MapName$,Passwort$="",Checksum%=0)
	Local error=0
	error=map_load(MapName$,Passwort$,Checksum)
	If error>0 Then EndGraphics
	Select error
  	Case 1
		Print "File not found"
  	'  RuntimeError "File not found"
	  Case 2
		Print "File size corrupted"
    '	RuntimeError "File size corrupted"
	  Case 3
		Print "No read access"
    '	RuntimeError "No read access"
	  Case 4
		Print "Not valid file"
    '	RuntimeError "Not valid file"
	  Case 5
		Print "Checksum problem"
    '	RuntimeError "Checksum problem"
	  Case 6
		Print "Password problem"
    '	RuntimeError "Password problem"
	  Case 7
		Print "Load image problem"
    '	RuntimeError "Load image problem"
	End Select
	If error>0 Then
		Local File%
		file=WriteFile("Map Load Error.dat")
			WriteLine file,"Fehler beim Laden der Map aufgetretten"
			WriteLine file,""
			WriteLine file,"Datum:"+CurrentDate()
			WriteLine file,""
			WriteLine file,"Mapname:"+MapName$
			WriteLine file,"Passwort MD5:"+MD5(Passwort$)
			WriteLine file,"Fehler Code:"+error
		CloseFile  file
		End
	EndIf
End Function


















