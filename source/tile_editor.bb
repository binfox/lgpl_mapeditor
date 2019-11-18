Global tile_editor
Global tile_editor_anim
Global tile_editor_canvas
Global tile_editor_frame
Global tile_editor_height
Global tile_editor_max
Global tile_editor_sliderx
Global tile_editor_slidery
Global tile_editor_width





;---------------------------------------------------------------------
Function tile_editor_click()
  Local bank
  Local frames
  Local mx
  Local my
  Local nr
  Local posx
  Local posy
  Local rx
  Local ry
  Local sizex
  Local sizey

  If tile=Null Then Return

  sizex=tile\sizex
  If sizex<tile_minsize Then sizex=tile_minsize

  sizey=tile\sizey
  If sizey<tile_minsize Then sizey=tile_minsize

  posx=SliderValue(tile_editor_sliderx)
  posy=SliderValue(tile_editor_slidery)

  mx=(MouseX(tile_editor_canvas)-11+posx)/(sizex+07)
  my=(MouseY(tile_editor_canvas)-21+posy)/(sizey+37)
  rx=(MouseX(tile_editor_canvas)-11+posx)-(sizex+07)*mx
  ry=(MouseY(tile_editor_canvas)-21+posy)-(sizey+37)*my

  If my=>0 And my<=tile\anims-1 Then
    bank=PeekInt(tile\banka,my*4) ;animbank
    frames=PeekShort(bank,0)      ;frames
    If mx=>0 And mx<=frames-1 Then nr=mx+1
  EndIf

  If rx<0 Then nr=0
  If ry<0 Then nr=0
  If rx>sizex-1 Then nr=0
  If ry>sizey-1 Then nr=0

  If nr>0 Then
    tile_editor_anim=my+1
    tile_editor_frame=mx+1
    If tile_menu_selected=>0 And tile_menu_selected<=tile\count And MouseDown(1)=1 Then
      PokeShort bank,tile_editor_frame*4+8,tile_menu_selected ;image
    EndIf
  Else
    tile_editor_anim=0
    tile_editor_frame=0
  EndIf

  tile_lastclick=2
  tile_update()
End Function





;---------------------------------------------------------------------
Function tile_editor_exit()
  FreeGadget tile_editor_canvas
  FreeGadget tile_editor_slidery
  FreeGadget tile_editor_sliderx
  FreeGadget tile_editor
End Function





;---------------------------------------------------------------------
Function tile_editor_resize()
  FreeGadget tile_editor_canvas
  tile_editor_width =ClientWidth (tile_editor)
  tile_editor_height=ClientHeight(tile_editor)
  tile_editor_canvas=CreateCanvas(0,0,tile_editor_width-16,tile_editor_height-16,tile_editor)
  SetGadgetLayout tile_editor_canvas,1,1,1,1
  tile_editor_update(1)
End Function





;---------------------------------------------------------------------
Function tile_editor_start()
  tile_editor        =CreatePanel (222,tile_toolbar_height+75,tile_window_width-222,tile_window_height-tile_toolbar_height-117,tile_window,1)
  tile_editor_width  =ClientWidth (tile_editor)
  tile_editor_height =ClientHeight(tile_editor)
  tile_editor_sliderx=CreateSlider(0,tile_editor_height-16,tile_editor_width-16,16,tile_editor,1)
  tile_editor_slidery=CreateSlider(tile_editor_width-16,0,16,tile_editor_height-16,tile_editor,2)
  tile_editor_canvas =CreateCanvas(0,0,tile_editor_width-16,tile_editor_height-16,tile_editor)
  tile_editor_anim   =0
  tile_editor_frame  =0

  SetGadgetLayout tile_editor,1,1,1,1
  SetGadgetLayout tile_editor_sliderx,1,1,0,1
  SetGadgetLayout tile_editor_slidery,0,1,1,1
  SetGadgetLayout tile_editor_canvas,1,1,1,1
End Function





;---------------------------------------------------------------------
;mode: 0=complete refresh
;      1=fast refresh (no size check)
;---------------------------------------------------------------------
Function tile_editor_update(mode=0)
  Local bank
  Local canvash
  Local canvasw
  Local defval
  Local endpos
  Local frames
  Local height
  Local i
  Local posx
  Local posy
  Local sizex
  Local sizey
  Local startpos
  Local value
  Local width
  Local x
  Local xx
  Local y
  Local yy

  SetBuffer CanvasBuffer(tile_editor_canvas)
  SetFont window_font3
  ClsColor 255,255,255
  Cls

  If tile=Null Then
    SetSliderRange tile_editor_sliderx,1,1
    SetSliderRange tile_editor_slidery,1,1
    Goto abort
  EndIf

  canvasw=GadgetWidth (tile_editor_canvas)
  canvash=GadgetHeight(tile_editor_canvas)

  sizex=tile\sizex
  If sizex<tile_minsize Then sizex=tile_minsize

  sizey=tile\sizey
  If sizey<tile_minsize Then sizey=tile_minsize

  If mode=0 Then
    tile_editor_max=0
    For i=1 To tile\anims
      bank=PeekInt(tile\banka,i*4-4) ;animbank
      frames=PeekShort(bank,0)       ;frames
      If frames>tile_editor_max Then tile_editor_max=frames
    Next
  EndIf

  SetSliderRange tile_editor_sliderx,canvasw,tile_editor_max*(sizex+7)+15
  posx=SliderValue(tile_editor_sliderx)

  SetSliderRange tile_editor_slidery,canvash,tile\anims*(sizey+37)+5
  posy=SliderValue(tile_editor_slidery)

  startpos=(posy-5)/(sizey+37)+1
  If startpos>tile\anims Then startpos=tile\anims
  If startpos<0 Then startpos=0

  endpos=(posy-5+canvash)/(sizey+37)+1
  If endpos>tile\anims Then endpos=tile\anims
  If endpos<0 Then endpos=0

  If startpos>0 Then
    For y=startpos To endpos
      bank=PeekInt(tile\banka,y*4-4) ;animbank
      frames=PeekShort(bank,0)       ;frames
      defval=PeekByte (bank,5)       ;default value

      width=(sizex+7)*frames+5
      height=sizey+32
      yy=(y-1)*(sizey+37)-posy

      Color 220,220,220
      Rect 5-posx,5+yy,width,height

      If tile_editor_anim=y And tile_editor_frame=>1 And tile_editor_frame<=frames Then
        xx=(tile_editor_frame-1)*(sizex+7)+8-posx
        Color 0,0,0
        Rect xx,yy+18,sizex+6,sizey+6
      EndIf

      Color 255,255,255
      For x=0 To width/10
        Rect x*10+10-posx,yy+10,5,5
        Rect x*10+10-posx,yy+height-5,5,5
      Next

      For x=1 To frames
        xx=(x-1)*(sizex+7)+10-posx
        Rect xx,yy+20,sizex+2,sizey+2

        If tile\image<>0 Then
          value=PeekShort(bank,x*4+8) ;image
          If value=>1 And value<=tile\count Then DrawImageRect tile\image,xx+1,yy+21,0,0,sizex,sizey,value-1
          If value>tile\count Then
            Color 255,150,150
            Rect xx+1,yy+21,sizex,sizey
            Color 255,255,255
          EndIf
        EndIf
      Next

      If setup_tileview=1 Or setup_tileview=2 Then
        Color 220,220,220
        Rect 6-posx,yy+sizey+24,StringWidth(Str$(y))+8,12
        Color 0,0,0
        Text 10-posx,yy+sizey+24,Str$(y)
      ElseIf setup_tileview=3 Then
        Color 220,220,220
        Rect 6-posx,yy+sizey+24,StringWidth(Str$(defval))+8,12
        Color 0,0,0
        Text 10-posx,yy+sizey+24,Str$(defval)
      EndIf

    Next
  EndIf

  .abort
  FlipCanvas tile_editor_canvas
End Function