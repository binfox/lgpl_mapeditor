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





Include "mapengine\mapengine.bb"

width=640
height=480
Graphics width,height,0,1


error=map_load("0.map","") ;<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

If error>0 Then EndGraphics
Select error
  Case 1
    RuntimeError "File not found"
  Case 2
    RuntimeError "File size corrupted"
  Case 3
    RuntimeError "No read access"
  Case 4
    RuntimeError "Not valid file"
  Case 5
    RuntimeError "Checksum problem"
  Case 6
    RuntimeError "Password problem"
  Case 7
    RuntimeError "Load image problem"
End Select
If error>0 Then End





MoveMouse width/2,height/2
While Not KeyHit(1)
  If KeyDown(200)=1 Then map_scrolly=map_scrolly+height/10 ;up
  If KeyDown(208)=1 Then map_scrolly=map_scrolly-height/10 ;down
  If KeyDown(203)=1 Then map_scrollx=map_scrollx+width /10 ;left
  If KeyDown(205)=1 Then map_scrollx=map_scrollx-width /10 ;right

  mx=MouseX()
  my=MouseY()
  MoveMouse width/2,height/2

  map_scrollx=map_scrollx-(mx-width/2)
  map_scrolly=map_scrolly-(my-height/2)

  SetBuffer BackBuffer()
  ClsColor 0,0,0
  Cls

  tile_animate()
  map_draw()

  Text 0,0,"X:"+Str$(map_scrollx)+"  Y:"+Str$(map_scrolly)

  Flip
Wend