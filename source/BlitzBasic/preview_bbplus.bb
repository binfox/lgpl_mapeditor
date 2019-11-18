;//////////////////////////////////////////////////////////////
;//                                                          //
;// PLEASE START ONLY THIS MAIN-PROGRAM FILE AND NOT INCLUDE //
;// BITTE STARTE NUR DIESE HAUPTDATEI UND NICHT INCLUDE      //
;//                                                          //
;//////////////////////////////////////////////////////////////





Include "mapengine\mapengine.bb"

command$=Trim$(CommandLine$())

If command$="" Then

  name$=RequestFile$("Load map","map",0)
  If name$="" Then End

  desktop_width =ClientWidth    (Desktop())
  desktop_height=ClientHeight   (Desktop())
  window        =CreateWindow   ("Preview",(desktop_width-300)/2,(desktop_height-100)/2,250,100,0,33)
  window_width  =ClientWidth    (window)
  window_height =ClientHeight   (window)
  label1        =CreateLabel    ("Resolution:",5,08,70,20,window)
  label2        =CreateLabel    ("Password:"  ,5,38,70,20,window)
  resolution    =CreateComboBox (80,5,165,22,window)
  password      =CreateTextField(80,35,165,22,window)
  button        =CreateButton   ("OK",(window_width-80)/2,window_height-32,80,22,window)

  count=CountGfxModes()
  Dim scr_width (count)
  Dim scr_height(count)
  Dim scr_depth (count)

  For i=1 To count
    scr_width (i)=GfxModeWidth (i)
    scr_height(i)=GfxModeHeight(i)
    scr_depth (i)=GfxModeDepth (i)
    selected=(scr_width(i)=800 And scr_height(i)=600) Or (i=1)
    AddGadgetItem resolution,Str$(scr_width(i))+"x"+Str$(scr_height(i))+"x"+Str$(scr_depth(i)),selected
  Next

  Repeat
    WaitEvent()

    Select EventID()
      Case $0401 ;gadgetevent-------------------------------------------
        Select EventSource()
          Case button
            Exit
        End Select
      Case $0803 ;windowclose-------------------------------------------
        End
    End Select
  Forever

  passw$  =TextFieldText$(password)
  selected=SelectedGadgetItem(resolution)+1
  width   =scr_width (selected)
  height  =scr_height(selected)
  depth   =scr_depth (selected)

  FreeGadget window
  Graphics width,height,depth,2

Else

  For pos=Len(command$) To 1 Step -1
    If Mid$(command$,pos,1)=":" Then Exit
  Next

  If pos=0 Then
    name$ =command$
    passw$=""
  Else
    name$ =Left$(command$,pos-1)
    passw$=Mid$ (command$,pos+1)
  EndIf

  Graphics 800,600,0,2
  width=800
  height=600

EndIf





error=map_load(name$,passw$)

If error>0 Then EndGraphics
Select error
  Case 1
    Notify "File not found"+Chr$(13)+name$
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