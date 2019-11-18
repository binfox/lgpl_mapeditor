Global menu
Global menu_action
Global menu_button
Global menu_canvas
Global menu_deltax
Global menu_deltay
Global menu_height
Global menu_panel
Global menu_panelh
Global menu_panelw
Global menu_selected
Global menu_sliderx
Global menu_slidery
Global menu_width





;---------------------------------------------------------------------
Function menu_click()
  Local canvasw
  Local count
  Local max
  Local maxanims
  Local maxcount
  Local maxx
  Local maxy
  Local mx
  Local my
  Local nr
  Local pos
  Local rx
  Local ry
  Local selected
  Local sizex
  Local sizey
  Local zoom=1

  canvasw=GadgetWidth(menu_canvas)
  canvash=GadgetHeight(menu_canvas)
  selected=SelectedGadgetItem(menu)

  If selected=0 And setup_tilelayout=1 Then
    menu_click2()
    Return
  EndIf

  If layer=Null Then Return
  If selected=0 Or selected=1 Then
    If layer\tile=Null Then Return
    If layer\tile\image=0 Then Return
    tile=layer\tile
  EndIf

  If layer\code<>layer_map And layer\code<>layer_iso1 And layer\code<>layer_iso2 And layer\code<>layer_hex1 And layer\code<>layer_hex2 And layer\code<>layer_image And layer\code<>layer_block Then Return
  If (layer\code= layer_image Or  layer\code= layer_block) And selected=2 Then Return
  If (layer\code<>layer_image And layer\code<>layer_block) And selected=2 And layer\depth2=0 Then Return

  If selected=0 Or selected=1 Then
    If layer\code= layer_image Or  layer\code= layer_block Then max=$7FFFFFFF
    If layer\code<>layer_image And layer\code<>layer_block Then max=(1 Shl layer\depth1)-1

    maxanims=tile\anims
    If maxanims>max Then maxanims=max

    maxcount=tile\count
    If maxcount>max-maxanims Then maxcount=max-maxanims

    If selected=0 Then count=maxcount
    If selected=1 Then count=maxanims
    If count=0 Then Return

    sizex=tile\sizex
    If sizex>canvasw-12 Then sizex=canvasw-12

    sizey=tile\sizey
    If sizey>canvash-12 Then sizey=canvash-12
  ElseIf selected=2 Then
    count=(1 Shl layer\depth2)-1
    sizex=32
    sizey=32
  EndIf

;  If sizex<=setup_minzoom And sizey<=setup_minzoom Then
  If setup_tilezoom=1 Then
    zoom=3
    canvasw=canvasw/3
    canvash=canvash/3
  EndIf

  maxx=(canvasw-6)/(sizex+6)
  If maxx<1 Then maxx=1

  maxy=(count+maxx-1)/maxx
  pos=SliderValue(menu_slidery)

  mx=(MouseX(menu_canvas)/zoom-6    )/(sizex+6)
  my=(MouseY(menu_canvas)/zoom-6+pos)/(sizey+6)
  rx=(MouseX(menu_canvas)/zoom-6    )-(sizex+6)*mx
  ry=(MouseY(menu_canvas)/zoom-6+pos)-(sizey+6)*my

  nr=my*maxx+mx+1
  If nr>count Then nr=0

  If mx<0 Then nr=0
  If my<0 Then nr=0
  If mx>maxx-1 Then nr=0
  If my>maxy-1 Then nr=0

  If rx<0 Then nr=0
  If ry<0 Then nr=0
  If rx>sizex-1 Then nr=0
  If ry>sizey-1 Then nr=0

  menu_select(nr)

  If layer\code=layer_map Or layer\code=layer_iso1 Or layer\code=layer_iso2 Or layer\code=layer_hex1 Or layer\code=layer_hex2 Then
    If menu_selected>0 Then
      If toolbar_xtool=0 Or toolbar_xtool=1 Then toolbar_select(toolbar_draw)
    EndIf
  EndIf
End Function





;---------------------------------------------------------------------
Function menu_click2()
  Local canvash
  Local canvasw
  Local county
  Local max
  Local maxanims
  Local maxcount
  Local mx
  Local my
  Local nr
  Local posx
  Local posy
  Local rx
  Local ry
  Local sizex
  Local sizey
  Local zoom=1

  canvasw=GadgetWidth (menu_canvas)
  canvash=GadgetHeight(menu_canvas)

  If layer=Null Then Return
  If layer\tile=Null Then Return
  If layer\tile\image=0 Then Return
  If layer\code<>layer_map And layer\code<>layer_iso1 And layer\code<>layer_iso2 And layer\code<>layer_hex1 And layer\code<>layer_hex2 And layer\code<>layer_image And layer\code<>layer_block Then Return
  If layer\code= layer_image Or  layer\code= layer_block Then max=$7FFFFFFF
  If layer\code<>layer_image And layer\code<>layer_block Then max=(1 Shl layer\depth1)-1

  tile=layer\tile
  sizex=tile\sizex
  sizey=tile\sizey

  maxanims=tile\anims
  If maxanims>max Then maxanims=max

  maxcount=tile\count
  If maxcount>max-maxanims Then maxcount=max-maxanims

  county=Int(Ceil#(Float#(maxcount)/Float#(tile\countx)))

;  If sizex<=setup_minzoom And sizey<=setup_minzoom Then
  If setup_tilezoom=1 Then
    zoom=3
    canvasw=canvasw/3
    canvash=canvash/3
  EndIf

  posx=SliderValue(menu_sliderx)
  posy=SliderValue(menu_slidery)

  mx=(MouseX(menu_canvas)/zoom-6+posx)/(sizex+6)
  my=(MouseY(menu_canvas)/zoom-6+posy)/(sizey+6)
  rx=(MouseX(menu_canvas)/zoom-6+posx)-(sizex+6)*mx
  ry=(MouseY(menu_canvas)/zoom-6+posy)-(sizey+6)*my

  nr=my*tile\countx+mx+1
  If nr>maxcount Then nr=0

  If mx<0 Then nr=0
  If my<0 Then nr=0
  If mx>tile\countx-1 Then nr=0
  If my>county-1 Then nr=0

  If rx<0 Then nr=0
  If ry<0 Then nr=0
  If rx>sizex-1 Then nr=0
  If ry>sizey-1 Then nr=0

  menu_select2(nr)

  If layer\code=layer_map Or layer\code=layer_iso1 Or layer\code=layer_iso2 Or layer\code=layer_hex1 Or layer\code=layer_hex2 Then
    If menu_selected>0 Then
      If toolbar_xtool=0 Or toolbar_xtool=1 Then toolbar_select(toolbar_draw)
    EndIf
  EndIf
End Function





;---------------------------------------------------------------------
Function menu_exit()
  FreeGadget menu_button
  FreeGadget menu_canvas
  FreeGadget menu_slidery
  FreeGadget menu_sliderx
  FreeGadget menu_panel
  FreeGadget menu
End Function





;---------------------------------------------------------------------
Function menu_resize()
  FreeGadget menu_canvas
  menu_panelw=ClientWidth (menu_panel)
  menu_panelh=ClientHeight(menu_panel)
  menu_canvas=CreateCanvas(0,0,menu_panelw-16,menu_panelh-16,menu_panel)
  SetGadgetLayout menu_canvas,1,0,1,0
  menu_update()
End Function





;---------------------------------------------------------------------
;nr: frame nr
;---------------------------------------------------------------------
Function menu_select(nr)
  Local canvash
  Local canvasw
  Local count
  Local max
  Local maxanims
  Local maxcount
  Local maxx
  Local maxy
  Local pos
  Local selected
  Local sizex
  Local sizey
  Local y
  Local zoom=1

  canvasw=GadgetWidth(menu_canvas)
  canvash=GadgetHeight(menu_canvas)
  selected=SelectedGadgetItem(menu)

  If selected=0 And setup_tilelayout=1 Then
    menu_select2(nr)
    Return
  EndIf

  If layer=Null Then Return
  If selected=0 Or selected=1 Then
    If layer\tile=Null Then Return
    If layer\tile\image=0 Then Return
    tile=layer\tile
  EndIf

  If layer\code<>layer_map And layer\code<>layer_iso1 And layer\code<>layer_iso2 And layer\code<>layer_hex1 And layer\code<>layer_hex2 And layer\code<>layer_image And layer\code<>layer_block Then Return
  If (layer\code= layer_image Or  layer\code= layer_block) And selected=2 Then Return
  If (layer\code<>layer_image And layer\code<>layer_block) And selected=2 And layer\depth2=0 Then Return

  If selected=0 Or selected=1 Then
    If layer\code= layer_image Or  layer\code= layer_block Then max=$7FFFFFFF
    If layer\code<>layer_image And layer\code<>layer_block Then max=(1 Shl layer\depth1)-1

    maxanims=tile\anims
    If maxanims>max Then maxanims=max

    maxcount=tile\count
    If maxcount>max-maxanims Then maxcount=max-maxanims

    If selected=0 Then count=maxcount
    If selected=1 Then count=maxanims
    If count=0 Then Return

    sizex=tile\sizex
    If sizex>canvasw-12 Then sizex=canvasw-12

    sizey=tile\sizey
    If sizey>canvash-12 Then sizey=canvash-12
  ElseIf selected=2 Then
    count=(1 Shl layer\depth2)-1
    sizex=32
    sizey=32
  EndIf

;  If sizex<=setup_minzoom And sizey<=setup_minzoom Then
  If setup_tilezoom=1 Then
    zoom=3
    canvasw=canvasw/3
    canvash=canvash/3
  EndIf

  maxx=(canvasw-6)/(sizex+6)
  If maxx<1 Then maxx=1
  maxy=(count+maxx-1)/maxx

  If nr<0 Then nr=0
  If nr>count Then nr=0

  If nr>0 Then
    pos=SliderValue(menu_slidery)+canvash
    y=((nr-1)/maxx+1)*(sizey+6)+6
    If y>pos Then SetSliderValue menu_slidery,y-canvash

    pos=SliderValue(menu_slidery)
    y=((nr-1)/maxx)*(sizey+6)
    If y<pos Then SetSliderValue menu_slidery,y
  EndIf

  If layer\code=layer_image Or layer\code=layer_block Then
    layer\frame=0
    If selected=0 Then ;frames
      If nr>0 Then layer\frame=nr+tile\anims
    ElseIf selected=1 Then ;anims
      If nr>0 Then layer\frame=nr
    EndIf
  EndIf

  menu_selected=nr
  menu_update()
  editor_update(2)
End Function





;---------------------------------------------------------------------
;nr: frame nr
;---------------------------------------------------------------------
Function menu_select2(nr)
  Local canvash
  Local canvasw
  Local posx
  Local posy
  Local sizex
  Local sizey
  Local x
  Local y
  Local zoom=1

  canvasw=GadgetWidth (menu_canvas)
  canvash=GadgetHeight(menu_canvas)

  If layer=Null Then Return
  If layer\tile=Null Then Return
  If layer\tile\image=0 Then Return
  If layer\code<>layer_map And layer\code<>layer_iso1 And layer\code<>layer_iso2 And layer\code<>layer_hex1 And layer\code<>layer_hex2 And layer\code<>layer_image And layer\code<>layer_block Then Return

  tile=layer\tile
  sizex=tile\sizex
  sizey=tile\sizey

;  If sizex<=setup_minzoom And sizey<=setup_minzoom Then
  If setup_tilezoom=1 Then
    zoom=3
    canvasw=canvasw/3
    canvash=canvash/3
  EndIf

  If nr<0 Then nr=0
  If nr>tile\count Then nr=0

  If nr>0 Then
    posx=SliderValue(menu_sliderx)+canvasw
    x=((nr-1) Mod tile\countx+1)*(sizex+6)+6
    If x>posx Then SetSliderValue menu_sliderx,x-canvasw

    posx=SliderValue(menu_sliderx)
    x=((nr-1) Mod tile\countx)*(sizex+6)
    If x<posx Then SetSliderValue menu_sliderx,x

    posy=SliderValue(menu_slidery)+canvash
    y=((nr-1)/tile\countx+1)*(sizey+6)+6
    If y>posy Then SetSliderValue menu_slidery,y-canvash

    posy=SliderValue(menu_slidery)
    y=((nr-1)/tile\countx)*(sizey+6)
    If y<posy Then SetSliderValue menu_slidery,y
  EndIf

  menu_selected=nr
  menu_update()
  editor_update(2)
End Function





;---------------------------------------------------------------------
;mode: 0=mousedown, 1=mouseup, 2=mousemove
;---------------------------------------------------------------------
Function menu_slide(mode)
  Local sliderx
  Local slidery
  Local x
  Local y

  If mode=0 Then
    menu_action=1
    menu_deltax=MouseX(menu_canvas)
    menu_deltay=MouseY(menu_canvas)
    Return
  ElseIf mode=1 Then
    menu_action=0
    Return
  ElseIf mode=2 Then
    If menu_action=0 Then Return
  EndIf

  x=(menu_deltax-MouseX(menu_canvas)) ;/(toolbar_xzoom*2+1)
  y=(menu_deltay-MouseY(menu_canvas)) ;/(toolbar_xzoom*2+1)
  MoveMouse menu_deltax,menu_deltay,menu_canvas

  sliderx=SliderValue(menu_sliderx)
  slidery=SliderValue(menu_slidery)

  SetSliderValue menu_sliderx,sliderx+x
  SetSliderValue menu_slidery,slidery+y

  menu_update(1)
End Function





;---------------------------------------------------------------------
Function menu_start()
  menu=CreateTabber(0,toolbar_height+229,227,window_height-toolbar_height-229,window)

  SetGadgetIconStrip menu,window_strip3
  AddGadgetItem menu,language$(72),0,1
  AddGadgetItem menu,language$(73),0,1
  AddGadgetItem menu,language$(74),0,1

  ;<<<THIS COMPENSATE TABBER-BUG IN BB+ V1.39>>>
  SetGadgetShape menu,0,toolbar_height+229,227,window_height-toolbar_height-229

  menu_width  =ClientWidth (menu)
  menu_height =ClientHeight(menu)
  menu_panel  =CreatePanel (1,1,menu_width-3,menu_height-3,menu,1)
  menu_panelw =ClientWidth (menu_panel)
  menu_panelh =ClientHeight(menu_panel)
  menu_sliderx=CreateSlider(0,menu_panelh-16,menu_panelw-16,16,menu_panel,1)
  menu_slidery=CreateSlider(menu_panelw-16,0,16,menu_panelh-16,menu_panel,2)
  menu_canvas =CreateCanvas(0,0,menu_panelw-16,menu_panelh-16,menu_panel)
  menu_button =CreatePanel (menu_panelw-16,menu_panelh-16,16,16,menu_panel)

  SetGadgetLayout menu,1,0,1,1
  SetGadgetLayout menu_panel,1,1,1,1
  SetGadgetLayout menu_sliderx,1,1,0,1
  SetGadgetLayout menu_slidery,0,1,1,1
  SetGadgetLayout menu_canvas,1,0,1,0
  SetGadgetLayout menu_button,0,1,0,1

  If setup_tilelayout=0 Then SetPanelColor menu_button,192,192,192
  If setup_tilelayout=1 Then SetPanelColor menu_button,100,150,200
End Function





;---------------------------------------------------------------------
Function menu_switch()
  setup_tilelayout=1-setup_tilelayout
  If setup_tilelayout=0 Then SetPanelColor menu_button,192,192,192
  If setup_tilelayout=1 Then SetPanelColor menu_button,100,150,200
  menu_update()
End Function





;---------------------------------------------------------------------
;mode: 0=calculate map file size
;      1=do not calculate
;---------------------------------------------------------------------
Function menu_update(mode=0)
  Local bank
  Local canvash
  Local canvasw
  Local count
  Local endpos
  Local frame
  Local max
  Local maxanims
  Local maxcount
  Local maxx
  Local maxy
  Local menuh
  Local nr
  Local pos
  Local selected
  Local sizex
  Local sizey
  Local start
  Local startpos
  Local x
  Local xx
  Local y
  Local yy
  Local zoom=1

  If mode=0 Then
    menu_updatex()
    map_calc()
  EndIf

  canvasw=GadgetWidth(menu_canvas)
  canvash=GadgetHeight(menu_canvas)
  selected=SelectedGadgetItem(menu)

  If selected=0 And setup_tilelayout=1 Then
    menu_update2()
    Return
  EndIf

  SetBuffer CanvasBuffer(menu_canvas)
  ClsColor 255,255,255
  Cls

  If layer=Null Then Goto abort
  If selected=0 Or selected=1 Then
    If layer\tile=Null Then Goto abort
    If layer\tile\image=0 Then Goto abort
    tile=layer\tile
  EndIf

  If layer\code<>layer_map And layer\code<>layer_iso1 And layer\code<>layer_iso2 And layer\code<>layer_hex1 And layer\code<>layer_hex2 And layer\code<>layer_image And layer\code<>layer_block Then Goto abort
  If (layer\code= layer_image Or  layer\code= layer_block) And selected=2 Then Goto abort
  If (layer\code<>layer_image And layer\code<>layer_block) And selected=2 And layer\depth2=0 Then Goto abort

  If selected=0 Or selected=1 Then
    If layer\code= layer_image Or  layer\code= layer_block Then max=$7FFFFFFF
    If layer\code<>layer_image And layer\code<>layer_block Then max=(1 Shl layer\depth1)-1

    maxanims=tile\anims
    If maxanims>max Then maxanims=max

    maxcount=tile\count
    If maxcount>max-maxanims Then maxcount=max-maxanims

    If selected=0 Then count=maxcount
    If selected=1 Then count=maxanims
    If count=0 Then Goto abort

    sizex=tile\sizex
    If sizex>canvasw-12 Then sizex=canvasw-12

    sizey=tile\sizey
    If sizey>canvash-12 Then sizey=canvash-12
  ElseIf selected=2 Then
    count=(1 Shl layer\depth2)-1
    sizex=32
    sizey=32
  EndIf

  If layer\code=layer_image Or layer\code=layer_block Then
    menu_selected=0
    If layer\frame=>1 And layer\frame<=tile\anims Then
      If selected=1 Then menu_selected=layer\frame ;anims
    Else
      If selected=0 Then menu_selected=layer\frame-tile\anims ;frames
    EndIf
  EndIf

  
;  If sizex<=setup_minzoom And sizey<=setup_minzoom Then
  If setup_tilezoom=1 Then
    zoom=3
    canvasw=canvasw/3
    canvash=canvash/3
  EndIf

  maxx=(canvasw-6)/(sizex+6)
  If maxx<1 Then maxx=1

  maxy=(count+maxx-1)/maxx
  menuh=maxy*sizey+maxy*6+6

  SetSliderRange menu_slidery,canvash,menuh
  pos=SliderValue(menu_slidery)

  startpos=(pos-3)/(sizey+6)
  If startpos>maxy-1 Then startpos=maxy-1
  If startpos<0 Then startpos=0

  endpos=(pos-3+canvash)/(sizey+6)
  If endpos>maxy-1 Then endpos=maxy-1
  If endpos<0 Then endpos=0

  For y=startpos To endpos
    For x=0 To maxx-1

      nr=y*maxx+x+1
      xx=x*(sizex+6)+6
      yy=y*(sizey+6)+6-pos
      If nr>count Then Goto quit

      If menu_selected=nr Then
        Color 0,0,0
        Rect xx-3,yy-3,sizex+6,sizey+6
        Color 255,255,255
        Rect xx-1,yy-1,sizex+2,sizey+2
      Else
        Color 200,200,200
        Rect xx-1,yy-1,sizex+2,sizey+2,0
      EndIf

      If selected=0 Or selected=1 Then
        frame=nr
        If selected=1 Then
          bank=PeekInt(tile\banka,nr*4-4)
          start=PeekShort(bank,2)
          frame=PeekShort(bank,start*4+8)
        EndIf
        If frame=>1 And frame<=tile\count Then
          If (layer\code=layer_map Or layer\code=layer_image Or layer\code=layer_block) And layer\mask=0 Then
            DrawBlockRect tile\image,xx,yy,0,0,sizex,sizey,frame-1
          Else
            DrawImageRect tile\image,xx,yy,0,0,sizex,sizey,frame-1
          EndIf
        EndIf
      ElseIf selected=2 Then
        Color 0,0,0 
        Text xx+16,yy+16,Str$(nr),1,1
      EndIf

    Next
  Next
  Goto quit

  .abort
  SetSliderRange menu_slidery,1,1
  Color 200,200,200
  Line 0,0,canvasw-1,canvash-1
  Line canvasw-1,0,0,canvash-1

  .quit
  SetSliderRange menu_sliderx,1,1
  If zoom=3 Then menu_zoom()
  FlipCanvas menu_canvas
End Function





;---------------------------------------------------------------------
Function menu_update2()
  Local canvash
  Local canvasw
  Local county
  Local endx
  Local endy
  Local frame
  Local max
  Local maxanims
  Local maxcount
  Local posx
  Local posy
  Local sizex
  Local sizey
  Local startx
  Local starty
  Local x
  Local xx
  Local y
  Local yy
  Local zoom=1

  canvasw=GadgetWidth (menu_canvas)
  canvash=GadgetHeight(menu_canvas)

  SetBuffer CanvasBuffer(menu_canvas)
  ClsColor 255,255,255
  Cls

  If layer=Null Then Goto abort
  If layer\tile=Null Then Goto abort
  If layer\tile\image=0 Then Goto abort
  If layer\code<>layer_map And layer\code<>layer_iso1 And layer\code<>layer_iso2 And layer\code<>layer_hex1 And layer\code<>layer_hex2 And layer\code<>layer_image And layer\code<>layer_block Then Goto abort
  If layer\code= layer_image Or  layer\code= layer_block Then max=$7FFFFFFF
  If layer\code<>layer_image And layer\code<>layer_block Then max=(1 Shl layer\depth1)-1

  tile=layer\tile
  sizex=tile\sizex
  sizey=tile\sizey

  maxanims=tile\anims
  If maxanims>max Then maxanims=max

  maxcount=tile\count
  If maxcount>max-maxanims Then maxcount=max-maxanims

  county=Int(Ceil#(Float#(maxcount)/Float#(tile\countx)))

;  If ((sizex<=setup_minzoom) And (sizey<=setup_minzoom)) Then
  If setup_tilezoom=1 Then
    zoom=3
    canvasw=canvasw/3
    canvash=canvash/3
  EndIf

  SetSliderRange menu_sliderx,canvasw,(sizex+6)*tile\countx+6
  SetSliderRange menu_slidery,canvash,(sizey+6)*county+6
  posx=SliderValue(menu_sliderx)
  posy=SliderValue(menu_slidery)

  startx=(posx-3)/(sizex+6)
  If startx>tile\countx-1 Then startx=tile\countx-1
  If startx<0 Then startx=0

  starty=(posy-3)/(sizey+6)
  If starty>county-1 Then starty=county-1
  If starty<0 Then starty=0

  endx=(posx-3+canvasw)/(sizex+6)
  If endx>tile\countx-1 Then endx=tile\countx-1
  If endx<0 Then endx=0

  endy=(posy-3+canvash)/(sizey+6)
  If endy>county-1 Then endy=county-1
  If endy<0 Then endy=0


  For y=starty To endy
    For x=startx To endx

      frame=y*tile\countx+x+1
      If frame>maxcount Then Goto quit
      xx=x*(sizex+6)+6-posx
      yy=y*(sizey+6)+6-posy

      If menu_selected=frame Then
        Color 0,0,0
        Rect xx-3,yy-3,sizex+6,sizey+6
        Color 255,255,255
        Rect xx-1,yy-1,sizex+2,sizey+2
      Else
        Color 200,200,200
        Rect xx-1,yy-1,sizex+2,sizey+2,0
      EndIf

      If frame=>1 And frame<=tile\count Then
        If (layer\code=layer_map Or layer\code=layer_image Or layer\code=layer_block) And layer\mask=0 Then
          DrawBlock tile\image,xx,yy,frame-1
        Else
          DrawImage tile\image,xx,yy,frame-1
        EndIf
      EndIf

    Next
  Next
  Goto quit

  .abort
  SetSliderRange menu_sliderx,1,1
  SetSliderRange menu_slidery,1,1
  Color 200,200,200
  Line 0,0,canvasw-1,canvash-1
  Line canvasw-1,0,0,canvash-1

  .quit
  If zoom=3 Then menu_zoom()
  FlipCanvas menu_canvas
End Function





;---------------------------------------------------------------------
Function menu_updatex()
  Local max
  Local maxanims
  Local maxcount

  ModifyGadgetItem menu,0,language$(72),1
  ModifyGadgetItem menu,1,language$(73),1
  ModifyGadgetItem menu,2,language$(74),1

  If layer\code=layer_map Or layer\code=layer_iso1 Or layer\code=layer_iso2 Or layer\code=layer_hex1 Or layer\code=layer_hex2 Or layer\code=layer_image Or layer\code=layer_block Then
    If layer\tile<>Null Then
      If layer\code= layer_image Or  layer\code= layer_block Then max=$7FFFFFFF
      If layer\code<>layer_image And layer\code<>layer_block Then max=(1 Shl layer\depth1)-1

      maxanims=layer\tile\anims
      If maxanims>max Then maxanims=max

      maxcount=layer\tile\count
      If maxcount>max-maxanims Then maxcount=max-maxanims

      If layer\tile\count>0 And layer\tile\count<=maxcount Then ModifyGadgetItem menu,0,language$(72),0
      If layer\tile\count>maxcount Then ModifyGadgetItem menu,0,language$(72),6
    EndIf
  EndIf

  If layer\code=layer_map Or layer\code=layer_iso1 Or layer\code=layer_iso2 Or layer\code=layer_hex1 Or layer\code=layer_hex2 Or layer\code=layer_image Or layer\code=layer_block Then
    If layer\tile<>Null Then
      If layer\tile\anims>0 And layer\tile\image<>0 Then ModifyGadgetItem menu,1,language$(73),0
    EndIf
  EndIf

  If layer\code=layer_map Or layer\code=layer_iso1 Or layer\code=layer_iso2 Or layer\code=layer_hex1 Or layer\code=layer_hex2 Then
    If layer\bank2<>0 Then ModifyGadgetItem menu,2,language$(74),0
  EndIf
End Function





;---------------------------------------------------------------------
Function menu_zoom()
  Local buffer
  Local height
  Local i
  Local length
  Local width

  buffer=CanvasBuffer(menu_canvas)
  length=GadgetWidth (menu_canvas)
  width =GadgetWidth (menu_canvas)/3+1
  height=GadgetHeight(menu_canvas)/3+1

  For i=width-1 To 0 Step -1
    CopyRect i,0,1,height, i*3+0,0, buffer,buffer
    CopyRect i,0,1,height, i*3+1,0, buffer,buffer
    CopyRect i,0,1,height, i*3+2,0, buffer,buffer
  Next

  For i=height-1 To 0 Step -1
    CopyRect 0,i,length,1, 0,i*3+0, buffer,buffer
    CopyRect 0,i,length,1, 0,i*3+1, buffer,buffer
    CopyRect 0,i,length,1, 0,i*3+2, buffer,buffer
  Next
End Function