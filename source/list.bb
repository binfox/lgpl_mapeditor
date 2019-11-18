Global list
Global list_canvas
Global list_height
Global list_slider
Global list_timer
Global list_width





;---------------------------------------------------------------------
Function list_click()
  Local addtime
  Local mx
  Local my
  Local nr
  Local pos

  pos=SliderValue(list_slider)
  mx=MouseX(list_canvas)
  my=MouseY(list_canvas)
  nr=layer_count-(my+pos-3)/20

  If mx<24 Then
    list_switch(nr)
  ElseIf mx=>24 And layer_nr=nr And (MilliSecs()-list_timer)<300 Then
    list_setup(nr)
    addtime=300
  ElseIf mx=>24 Then ;And layer_nr<>nr Then
    list_select(nr,MouseHit(2))
  EndIf

  list_timer=MilliSecs()-addtime
End Function





;---------------------------------------------------------------------
Function list_exit()
  FreeGadget list_canvas
  FreeGadget list_slider
  FreeGadget list
End Function





;---------------------------------------------------------------------
Function list_resize()
  FreeGadget list_canvas
  list_width =ClientWidth  (list)
  list_height=ClientHeight (list)
  list_canvas=CreateCanvas (0,0,list_width-16,list_height,list)
  SetGadgetLayout list_canvas,1,0,1,0
  list_update()
End Function





;---------------------------------------------------------------------
;nr:   layer number
;jump: 0=no jump to object
;      1=jump to object
;---------------------------------------------------------------------
Function list_select(nr,jump=0)
  Local canvash
  Local pos

  If nr<0 Then Return
  If nr>layer_count Then Return

  layer=layer_list[nr]
  layer_nr=nr

  canvash=GadgetHeight(list_canvas)
  SetSliderRange list_slider,canvash,layer_count*20+26

  pos=SliderValue(list_slider)
  If (layer_count-nr)*20<pos Then SetSliderValue list_slider,(layer_count-nr)*20

  pos=SliderValue(list_slider)
  If (layer_count-nr)*20>pos+canvash-26 Then SetSliderValue list_slider,(layer_count-nr)*20-canvash+26

  menu_selected=0
  editor_layer=Null

  If layer_list[nr]\code=layer_clone Then layer_clone_update(layer_list[nr])

  If jump=1 Then layer_jump(layer)
  maker_update()
  list_update()
  menu_update()
  editor_update()
End Function





;---------------------------------------------------------------------
;nr:   layer number
;mode: 0=no selection
;      1=selection
;---------------------------------------------------------------------
Function list_setup(nr,mode=1)
  If nr<0 Then Return
  If nr>layer_count Then Return
  If mode=1 Then list_select(nr)

  Select layer\code
    Case layer_back
      setup_project()
    Case layer_map, layer_iso1, layer_iso2, layer_hex1, layer_hex2
      setup_layer()
    Case layer_clone
      setup_clone()
      layer_clone_update(layer_list[nr])
    Case layer_image
      setup_image()
    Case layer_block
      setup_block()
    Case layer_point, layer_line, layer_rect, layer_oval
      setup_object()
  End Select

  maker_update()
  list_update()
  menu_update()
  editor_update()
End Function





;---------------------------------------------------------------------
Function list_start()
  list       =CreatePanel  (0,toolbar_height,227,227,window,1)
  list_width =ClientWidth  (list)
  list_height=ClientHeight (list)
  list_slider=CreateSlider (list_width-16,0,16,list_height,list,2)
  list_canvas=CreateCanvas (0,0,list_width-16,list_height,list)
  SetGadgetLayout list,1,0,1,0
  SetGadgetLayout list_slider,0,1,1,1
  SetGadgetLayout list_canvas,1,0,1,0
End Function





;---------------------------------------------------------------------
;nr: layer number
;---------------------------------------------------------------------
Function list_switch(nr)
  If nr<0 Then Return
  If nr>layer_count Then Return

  layer_list[nr]\visible=1-layer_list[nr]\visible

  list_update()
  editor_update()
End Function





;---------------------------------------------------------------------
Function list_update()
  Local canvash
  Local canvasw
  Local endpos
  Local i
  Local j
  Local pos
  Local startpos

  canvasw=GadgetWidth (list_canvas)
  canvash=GadgetHeight(list_canvas)

  SetSliderRange list_slider,canvash,layer_count*20+26
  pos=SliderValue(list_slider)

  startpos=(pos-3)/20
  If startpos<0 Then startpos=0
  If startpos>layer_count Then startpos=layer_count

  endpos=(pos-3+canvash)/20
  If endpos<0 Then endpos=0
  If endpos>layer_count Then endpos=layer_count

  SetBuffer CanvasBuffer(list_canvas)
  SetFont window_font1
  Color 0,0,0
  ClsColor 255,255,255
  Cls

  For i=startpos To endpos
    j=(layer_count-i)
    If j=layer_nr Then
      Color 240,240,240
      Rect 3,3+i*20-pos,canvasw-6,20
      Color 0,0,0
      Rect 3,3+i*20-pos,canvasw-6,20,0
    EndIf

    If layer_list[j]\visible=0 DrawImage window_icons1,5,5+i*20-pos,layer_list[j]\code
    If layer_list[j]\visible=1 DrawImage window_icons2,5,5+i*20-pos,layer_list[j]\code

    Text 25,5+i*20-pos,layer_list[j]\name$

    If layer_list[j]\bank3<>0 Or layer_list[j]\bank4<>0 Then
      Color 255,0,0
      Text 28+StringWidth(layer_list[j]\name$),8+i*20-pos,"*"
      Color 0,0,0
    EndIf
  Next

  FlipCanvas list_canvas
End Function