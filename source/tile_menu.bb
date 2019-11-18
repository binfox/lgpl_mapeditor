Global tile_menu
Global tile_menu_canvas
Global tile_menu_height
Global tile_menu_selected
Global tile_menu_sliderx
Global tile_menu_slidery
Global tile_menu_width





;---------------------------------------------------------------------
Function tile_menu_click()
  Local mx
  Local my
  Local nr
  Local posx
  Local posy
  Local rx
  Local ry
  Local sizex
  Local sizey
  Local stepy

  If tile=Null Then Return
  If tile\image=0 Then Return

  sizex=tile\sizex
  sizey=tile\sizey

  stepy=sizey
  If setup_tileview>0 Then stepy=stepy+12

  posx=SliderValue(tile_menu_sliderx)
  posy=SliderValue(tile_menu_slidery)

  mx=(MouseX(tile_menu_canvas)-6+posx)/(sizex+6)
  my=(MouseY(tile_menu_canvas)-6+posy)/(stepy+6)
  rx=(MouseX(tile_menu_canvas)-6+posx)-(sizex+6)*mx
  ry=(MouseY(tile_menu_canvas)-6+posy)-(stepy+6)*my

  nr=my*tile\countx+mx+1
  If nr>tile\count Then nr=0

  If mx<0 Then nr=0
  If my<0 Then nr=0
  If mx>tile\countx-1 Then nr=0
  If my>tile\county-1 Then nr=0

  If rx<0 Then nr=0
  If ry<0 Then nr=0
  If rx>sizex-1 Then nr=0
  If ry>sizey-1 Then nr=0

  tile_lastclick=1
  tile_menu_selected=nr
  tile_update()
End Function





;---------------------------------------------------------------------
Function tile_menu_exit()
  FreeGadget tile_menu_canvas
  FreeGadget tile_menu_slidery
  FreeGadget tile_menu_sliderx
  FreeGadget tile_menu
End Function





;---------------------------------------------------------------------
Function tile_menu_resize()
  FreeGadget tile_menu_canvas
  tile_menu_width =ClientWidth (tile_menu)
  tile_menu_height=ClientHeight(tile_menu)
  tile_menu_canvas=CreateCanvas(0,0,tile_menu_width-16,tile_menu_height-16,tile_menu)
  SetGadgetLayout tile_menu_canvas,1,1,1,1
  tile_menu_update()
End Function





;---------------------------------------------------------------------
Function tile_menu_start()
  tile_menu        =CreatePanel (0,tile_toolbar_height+75,220,tile_window_height-tile_toolbar_height-117,tile_window,1)
  tile_menu_width  =ClientWidth (tile_menu)
  tile_menu_height =ClientHeight(tile_menu)
  tile_menu_sliderx=CreateSlider(0,tile_menu_height-16,tile_menu_width-16,16,tile_menu,1)
  tile_menu_slidery=CreateSlider(tile_menu_width-16,0,16,tile_menu_height-16,tile_menu,2)
  tile_menu_canvas =CreateCanvas(0,0,tile_menu_width-16,tile_menu_height-16,tile_menu)
  tile_menu_selected=0

  SetGadgetLayout tile_menu,1,0,1,1
  SetGadgetLayout tile_menu_sliderx,1,1,0,1
  SetGadgetLayout tile_menu_slidery,0,1,1,1
  SetGadgetLayout tile_menu_canvas,1,1,1,1
End Function





;---------------------------------------------------------------------
Function tile_menu_update()
  Local canvash
  Local canvasw
  Local distx
  Local endx
  Local endy
  Local frame
  Local posx
  Local posy
  Local sizex
  Local sizey
  Local startx
  Local starty
  Local stepy
  Local strw
  Local x
  Local xx
  Local y
  Local yy

  SetBuffer CanvasBuffer(tile_menu_canvas)
  SetFont window_font3
  ClsColor 255,255,255
  Cls

  If tile=Null Then Goto abort
  If tile\image=0 Then Goto abort

  canvasw=GadgetWidth (tile_menu_canvas)
  canvash=GadgetHeight(tile_menu_canvas)

  sizex=tile\sizex
  sizey=tile\sizey

  stepy=sizey
  If setup_tileview>0 Then stepy=stepy+12

  strw=StringWidth(String$("9",Len(Str$(tile\count))))+5
  distx=Int(Ceil#(Float(strw)/Float(sizex+6)))
  If distx<1 Then distx=1

  SetSliderRange tile_menu_sliderx,canvasw,(sizex+6)*tile\countx+6
  SetSliderRange tile_menu_slidery,canvash,(stepy+6)*tile\county+6
  posx=SliderValue(tile_menu_sliderx)
  posy=SliderValue(tile_menu_slidery)

  startx=(posx-3)/(sizex+6)
  If startx>tile\countx-1 Then startx=tile\countx-1
  If startx<0 Then startx=0

  starty=(posy-3)/(stepy+6)
  If starty>tile\county-1 Then starty=tile\county-1
  If starty<0 Then starty=0

  endx=(posx-3+canvasw)/(sizex+6)
  If endx>tile\countx-1 Then endx=tile\countx-1
  If endx<0 Then endx=0

  endy=(posy-3+canvash)/(stepy+6)
  If endy>tile\county-1 Then endy=tile\county-1
  If endy<0 Then endy=0


  For y=starty To endy
    For x=startx To endx

      frame=y*tile\countx+x+1
      xx=x*(sizex+6)+6-posx
      yy=y*(stepy+6)+6-posy

      If tile_menu_selected=frame Then
        Color 0,0,0
        Rect xx-3,yy-3,sizex+6,sizey+6
        Color 255,255,255
        Rect xx-1,yy-1,sizex+2,sizey+2
      Else
        Color 200,200,200
        Rect xx-1,yy-1,sizex+2,sizey+2,0
      EndIf

      Color 0,0,0
      If setup_tileview=1 And (sizex=>28 Or (x Mod distx)=0) Then
        Text xx,yy+sizey+2,Str$(frame)
      ElseIf setup_tileview=2 And (sizex=>28 Or (x Mod distx)=0) Then
        Text xx,yy+sizey+2,Str$(frame+tile\anims)
      ElseIf setup_tileview=3 Then
        value=PeekByte(tile\bankd,frame-1)
        Text xx,yy+sizey+2,Str$(value)
      EndIf

      DrawImage tile\image,xx,yy,frame-1
    Next
  Next
  Goto quit


  .abort
  SetSliderRange tile_menu_sliderx,1,1
  SetSliderRange tile_menu_slidery,1,1
  .quit
  FlipCanvas tile_menu_canvas
End Function