Type fill
  Field x
  Field y
End Type

Global editor
Global editor_action1
Global editor_action2
Global editor_action3
Global editor_canvas
Global editor_deltax
Global editor_deltay
Global editor_height
Global editor_move
Global editor_refresh
Global editor_scroll
Global editor_sliderx
Global editor_slidery
Global editor_width



;SELECTION
Global editor_x1
Global editor_y1
Global editor_x2
Global editor_y2
Global editor_x3
Global editor_y3
Global editor_x4
Global editor_y4
Global editor_layer.layer





;---------------------------------------------------------------------
Function editor_block()
  Local bank
  Local defval
  Local selected
  Local x
  Local y

  If layer=Null Then Goto abort
  If layer<>editor_layer Then Goto abort

  selected=SelectedGadgetItem(menu)
  If selected=0 Or selected=1 Then
    If layer\tile=Null Then Goto abort
    If layer\tile\image=0 Then Goto abort
  EndIf

  ;GET DEFAULT VALUE
  If selected=0 And menu_selected>0 Then  
    defval=PeekByte(layer\tile\bankd,menu_selected-1)
  ElseIf selected=1 And menu_selected>0 Then
    bank  =PeekInt (layer\tile\banka,menu_selected*4-4) ;animbank
    defval=PeekByte(bank,5)                             ;default value
  EndIf

  For y=editor_y1 To editor_y2
    For x=editor_x1 To editor_x2
      If selected=0 Then ;frame
        layer_setvalue(layer,x,y,menu_selected,1)
        If setup_usedef=1 Then layer_setdata(layer,x,y,defval,1)
      ElseIf selected=1 Then ;anim
        layer_setvalue(layer,x,y,menu_selected,2)
        If setup_usedef=1 Then layer_setdata(layer,x,y,defval,1)
      ElseIf selected=2 Then ;data
        layer_setdata(layer,x,y,menu_selected)
      EndIf
    Next
  Next

  .abort
  editor_layer=Null
  editor_update(2)
End Function





;---------------------------------------------------------------------
;mode: 0=mousedown, 1=mouseup, 2=mousemove
;---------------------------------------------------------------------
Function editor_click(mode)
  Local tool

  tool=toolbar_xtool+toolbar_select

  If mode=0 Then
    editor_action1=1
    If EventData()=1 Then
      If tool=toolbar_select Then editor_select(0)
      If tool=toolbar_mark   Then editor_mark(0)
      If tool=toolbar_draw   Then editor_draw()
      If tool=toolbar_radierer   Then editor_draw(1)
      If tool=toolbar_rect   Then editor_mark(0)
      If tool=toolbar_block  Then editor_mark(0)
      If tool=toolbar_seq    Then editor_mark(0)
      If tool=toolbar_fill   Then editor_fill()
      If tool=toolbar_magic  Then editor_magic()
    ElseIf EventData()=2 Then
      editor_pick()
    EndIf

  ElseIf mode=1 Then
    editor_action1=0
    If EventData()=1 Then
      If tool=toolbar_rect   Then editor_rect()
      If tool=toolbar_block  Then editor_block()
      If tool=toolbar_seq    Then editor_seq()
    EndIf

  ElseIf mode=2 Then
    If tool=toolbar_select Then editor_select(1)
    If tool=toolbar_mark   Then editor_mark(1)
    If tool=toolbar_draw   Then editor_draw()
    If tool=toolbar_radierer   Then editor_draw(1)
    If tool=toolbar_rect   Then editor_mark(1)
    If tool=toolbar_block  Then editor_mark(1)
    If tool=toolbar_seq    Then editor_mark(1)
    If tool=toolbar_magic  Then editor_magic()
  EndIf
End Function





;---------------------------------------------------------------------
Function editor_draw(delmode=0)
  Local bank
  Local defval
  Local result
  Local selected
  Local sliderx
  Local slidery
  Local x
  Local y

  If editor_action1=0 Then Return

  x=editor_x()
  y=editor_y()
  sliderx=SliderValue(editor_sliderx)+layer_minx
  slidery=SliderValue(editor_slidery)+layer_miny

  Select layer\code
    Case layer_map
      result=layer_map_coord(layer,sliderx,slidery,x,y,1)
    Case layer_iso1
      result=layer_iso1_coord(layer,sliderx,slidery,x,y,1)
    Case layer_iso2
      result=layer_iso2_coord(layer,sliderx,slidery,x,y,1)
    Case layer_hex1
      result=layer_hex1_coord(layer,sliderx,slidery,x,y,1)
    Case layer_hex2
      result=layer_hex2_coord(layer,sliderx,slidery,x,y,1)
    Default
      Return
  End Select
  If result=0 Then Return

  selected=SelectedGadgetItem(menu)
  If selected=0 Or selected=1 Then
    If layer\tile=Null Then Return
    If layer\tile\image=0 Then Return
  EndIf

  ;GET DEFAULT VALUE
  If selected=0 And menu_selected>0 Then  
    defval=PeekByte(layer\tile\bankd,menu_selected-1)
  ElseIf selected=1 And menu_selected>0 Then
    bank  =PeekInt (layer\tile\banka,menu_selected*4-4) ;animbank
    defval=PeekByte(bank,5)                             ;default value
  EndIf

  If delmode=0 Then
    If selected=0 Then ;frame
      layer_setvalue(layer,layer_x,layer_y,menu_selected,1)
      If setup_usedef=1 Then layer_setdata(layer,layer_x,layer_y,defval,1)
    ElseIf selected=1 Then ;anim
      layer_setvalue(layer,layer_x,layer_y,menu_selected,2)
      If setup_usedef=1 Then layer_setdata(layer,layer_x,layer_y,defval,1)
    ElseIf selected=2 Then ;data
      layer_setdata(layer,layer_x,layer_y,menu_selected)
    EndIf
  Else
    If selected=0 Then ;frame
      layer_setvalue(layer,layer_x,layer_y,0,1)
      If setup_usedef=1 Then layer_setdata(layer,layer_x,layer_y,0,1)
    ElseIf selected=1 Then ;anim
      layer_setvalue(layer,layer_x,layer_y,0,2)
      If setup_usedef=1 Then layer_setdata(layer,layer_x,layer_y,0,1)
    ElseIf selected=2 Then ;data
      layer_setdata(layer,layer_x,layer_y,0)
    EndIf
  EndIf

  editor_update(2)
End Function





;---------------------------------------------------------------------
Function editor_exit()
  FreeImage  editor_zoomimage
  FreeGadget editor_scroll
  FreeGadget editor_canvas
  FreeGadget editor_slidery
  FreeGadget editor_sliderx
  FreeGadget editor
End Function





;---------------------------------------------------------------------
Function editor_fill()
  Local arrays
  Local arrayx[5]
  Local arrayy[5]
  Local bank
  Local defval
  Local fill.fill
  Local i
  Local nr1
  Local nr2
  Local result
  Local selected
  Local shift
  Local sliderx
  Local slidery
  Local x
  Local y

  If editor_action1=0 Then Return
  editor_action1=0

  x=editor_x()
  y=editor_y()
  sliderx=SliderValue(editor_sliderx)+layer_minx
  slidery=SliderValue(editor_slidery)+layer_miny

  Select layer\code
    Case layer_map
      result=layer_map_coord(layer,sliderx,slidery,x,y,1)
    Case layer_iso1
      result=layer_iso1_coord(layer,sliderx,slidery,x,y,1)
    Case layer_iso2
      result=layer_iso2_coord(layer,sliderx,slidery,x,y,1)
    Case layer_hex1
      result=layer_hex1_coord(layer,sliderx,slidery,x,y,1)
    Case layer_hex2
      result=layer_hex2_coord(layer,sliderx,slidery,x,y,1)
    Default
      Return
  End Select
  If result=0 Then Return

  selected=SelectedGadgetItem(menu)
  If selected=0 Or selected=1 Then
    If layer\tile=Null Then Return
    If layer\tile\image=0 Then Return
  EndIf

  ;GET DEFAULT VALUE
  If selected=0 And menu_selected>0 Then  
    defval=PeekByte(layer\tile\bankd,menu_selected-1)
  ElseIf selected=1 And menu_selected>0 Then
    bank  =PeekInt (layer\tile\banka,menu_selected*4-4) ;animbank
    defval=PeekByte(bank,5)                             ;default value
  EndIf

  If selected=0 Then ;frame
    nr1=layer_getvalue2(layer,layer_x,layer_y)
    layer_setvalue(layer,layer_x,layer_y,menu_selected,1)
    If setup_usedef=1 Then layer_setdata(layer,layer_x,layer_y,defval,1)
    nr2=layer_getvalue2(layer,layer_x,layer_y)
  ElseIf selected=1 Then ;anim
    nr1=layer_getvalue2(layer,layer_x,layer_y)
    layer_setvalue(layer,layer_x,layer_y,menu_selected,2)
    If setup_usedef=1 Then layer_setdata(layer,layer_x,layer_y,defval,1)
    nr2=layer_getvalue2(layer,layer_x,layer_y)
  ElseIf selected=2 Then ;data
    nr1=layer_getdata(layer,layer_x,layer_y)
    layer_setdata(layer,layer_x,layer_y,menu_selected)
    nr2=layer_getdata(layer,layer_x,layer_y)
  EndIf

  If nr1=nr2 Then Return
  Delete Each fill
  fill=New fill
  fill\x=layer_x
  fill\y=layer_y


  Repeat
    fill=First fill
    x=fill\x
    y=fill\y
    Delete fill

    Select layer\code
      Case layer_map, layer_iso1
        arrays=3
        arrayx[0]=0  : arrayy[0]=-1
        arrayx[1]=-1 : arrayy[1]=0
        arrayx[2]=1  : arrayy[2]=0
        arrayx[3]=0  : arrayy[3]=1
      Case layer_iso2
        shift=(y+layer\start) And 1
        arrays=3
        arrayx[0]=shift-1 : arrayy[0]=-1
        arrayx[1]=shift   : arrayy[1]=-1
        arrayx[2]=shift-1 : arrayy[2]=1
        arrayx[3]=shift   : arrayy[3]=1
      Case layer_hex1
        shift=(x+layer\start) And 1
        arrays=5
        arrayx[0]=-1 : arrayy[0]=shift-1
        arrayx[1]=-1 : arrayy[1]=shift
        arrayx[2]=0  : arrayy[2]=-1
        arrayx[3]=0  : arrayy[3]=1
        arrayx[4]=1  : arrayy[4]=shift-1
        arrayx[5]=1  : arrayy[5]=shift
      Case layer_hex2
        shift=(y+layer\start) And 1
        arrays=5
        arrayx[0]=shift-1 : arrayy[0]=-1
        arrayx[1]=shift   : arrayy[1]=-1
        arrayx[2]=-1      : arrayy[2]=0
        arrayx[3]=1       : arrayy[3]=0
        arrayx[4]=shift-1 : arrayy[4]=1
        arrayx[5]=shift   : arrayy[5]=1
    End Select

    For i=0 To arrays
      If x+arrayx[i]=>0 And y+arrayy[i]=>0 And x+arrayx[i]<=layer\sizex-1 And y+arrayy[i]<=layer\sizey-1 Then

        If selected=0 Or selected=1 Then ;frame/anim
          nr2=layer_getvalue2(layer,x+arrayx[i],y+arrayy[i])
        ElseIf selected=2 Then ;data
          nr2=layer_getdata(layer,x+arrayx[i],y+arrayy[i])
        EndIf

        If nr1=nr2 Then
          fill=New fill
          fill\x=x+arrayx[i]
          fill\y=y+arrayy[i]
          If selected=0 Then ;frame
            layer_setvalue(layer,x+arrayx[i],y+arrayy[i],menu_selected,1)
            If setup_usedef=1 Then layer_setdata(layer,x+arrayx[i],y+arrayy[i],defval,1)
          ElseIf selected=1 Then ;anim
            layer_setvalue(layer,x+arrayx[i],y+arrayy[i],menu_selected,2)
            If setup_usedef=1 Then layer_setdata(layer,x+arrayx[i],y+arrayy[i],defval,1)
          ElseIf selected=2 Then ;data
            layer_setdata(layer,x+arrayx[i],y+arrayy[i],menu_selected)
          EndIf
        EndIf

      EndIf
    Next

  Until First fill=Null


  editor_update(2)
End Function





;---------------------------------------------------------------------
;screen: screen coordinate
;para:   parallax value (0-200)
;slider: slider position
;RETURN: absolute coordinate
;---------------------------------------------------------------------
Function editor_getcoord(screen,para,slider)
  If para=100 Or toolbar_xpara=0 Then
    Return screen+slider
  ElseIf para=0 Then
    Return screen
  Else
    Return screen+slider*para/100
  EndIf
End Function





;---------------------------------------------------------------------
;coord:  absolute coordinate
;para:   parallax value (0-200)
;slider: slider position
;RETURN: screen coordinate
;---------------------------------------------------------------------
Function editor_getscreen(coord,para,slider)
  If toolbar_xmap=1 And (layer\code=layer_map Or layer\code=layer_iso1 Or layer\code=layer_iso2 Or layer\code=layer_hex1 Or layer\code=layer_hex2 Or layer\code=layer_clone) Then
    Return -slider
  ElseIf para=100 Or toolbar_xpara=0 Then
    Return coord-slider
  ElseIf para=0 Then
    Return coord
  Else
    Return coord-slider*para/100
  EndIf
End Function





;---------------------------------------------------------------------
;RETURN: editor height
;---------------------------------------------------------------------
Function editor_height()
  If toolbar_xzoom=0 Then
    Return GadgetHeight(editor_canvas)
  Else
    Return GadgetHeight(editor_canvas)/setup_minzoom
  EndIf
End Function





;---------------------------------------------------------------------
Function editor_magic()
  Local arrays
  Local arrayx[5]
  Local arrayy[5]
  Local bank
  Local code
  Local count
  Local defval
  Local i
  Local max
  Local maxanims
  Local maxcount
  Local result
  Local selected
  Local shift
  Local sliderx
  Local slidery
  Local value
  Local x
  Local y

  If editor_action1=0 Then Return

  x=editor_x()
  y=editor_y()
  sliderx=SliderValue(editor_sliderx)+layer_minx
  slidery=SliderValue(editor_slidery)+layer_miny

  Select layer\code
    Case layer_map
      result=layer_map_coord(layer,sliderx,slidery,x,y,1)
    Case layer_iso1
      result=layer_iso1_coord(layer,sliderx,slidery,x,y,1)
    Case layer_iso2
      result=layer_iso2_coord(layer,sliderx,slidery,x,y,1)
    Case layer_hex1
      result=layer_hex1_coord(layer,sliderx,slidery,x,y,1)
    Case layer_hex2
      result=layer_hex2_coord(layer,sliderx,slidery,x,y,1)
    Default
      Return
  End Select
  If result=0 Then Return


  selected=SelectedGadgetItem(menu)
  If selected=0 Or selected=1 Then ; frame/anim
    If layer\tile=Null Then Return
    If layer\tile\image=0 Then Return

    max=(1 Shl layer\depth1)-1

    maxanims=layer\tile\anims
    If maxanims>max Then maxanims=max

    maxcount=layer\tile\count
    If maxcount>max-maxanims Then maxcount=max-maxanims

    If selected=0 Then count=maxcount
    If selected=1 Then count=maxanims
  ElseIf selected=2 Then ;data
    count=(1 Shl layer\depth2)-1
  EndIf
  If count=0 Then Return

  ;GET DEFAULT VALUE
  If selected=0 And menu_selected>0 Then  
    defval=PeekByte(layer\tile\bankd,menu_selected-1)
  ElseIf selected=1 And menu_selected>0 Then
    bank  =PeekInt (layer\tile\banka,menu_selected*4-4) ;animbank
    defval=PeekByte(bank,5)                             ;default value
  EndIf

  If selected=0 Then ;frame
    layer_setvalue(layer,layer_x,layer_y,menu_selected,1)
    If setup_usedef=1 Then layer_setdata(layer,layer_x,layer_y,defval,1)
  ElseIf selected=1 Then ;anim
    layer_setvalue(layer,layer_x,layer_y,menu_selected,2)
    If setup_usedef=1 Then layer_setdata(layer,layer_x,layer_y,defval,1)
  ElseIf selected=2 Then ;data
    layer_setdata(layer,layer_x,layer_y,menu_selected)
  EndIf


  Select layer\code
    Case layer_map, layer_iso1
      arrays=3
      arrayx[0]=0  : arrayy[0]=-1
      arrayx[1]=-1 : arrayy[1]=0
      arrayx[2]=1  : arrayy[2]=0
      arrayx[3]=0  : arrayy[3]=1
    Case layer_iso2
      shift=(layer_y+layer\start) And 1
      arrays=3
      arrayx[0]=shift-1 : arrayy[0]=-1
      arrayx[1]=shift   : arrayy[1]=-1
      arrayx[2]=shift-1 : arrayy[2]=1
      arrayx[3]=shift   : arrayy[3]=1
    Case layer_hex1
      shift=(layer_x+layer\start) And 1
      arrays=5
      arrayx[0]=-1 : arrayy[0]=shift-1
      arrayx[1]=-1 : arrayy[1]=shift
      arrayx[2]=0  : arrayy[2]=-1
      arrayx[3]=0  : arrayy[3]=1
      arrayx[4]=1  : arrayy[4]=shift-1
      arrayx[5]=1  : arrayy[5]=shift
    Case layer_hex2
      shift=(layer_y+layer\start) And 1
      arrays=5
      arrayx[0]=shift-1 : arrayy[0]=-1
      arrayx[1]=shift   : arrayy[1]=-1
      arrayx[2]=-1      : arrayy[2]=0
      arrayx[3]=1       : arrayy[3]=0
      arrayx[4]=shift-1 : arrayy[4]=1
      arrayx[5]=shift   : arrayy[5]=1
  End Select


  For i=0 To arrays
    arrayx[i]=arrayx[i]+layer_x
    arrayy[i]=arrayy[i]+layer_y
    If arrayx[i]=>0 And arrayy[i]=>0 And arrayx[i]<=layer\sizex-1 And arrayy[i]<=layer\sizey-1 Then

      If selected=0 Then ;frame

        code=layer_getcode(layer,arrayx[i],arrayy[i])
        If code=0 Or code=1 Then
          value=layer_getvalue(layer,arrayx[i],arrayy[i])
          If menu_selected>1     And value<menu_selected Then layer_setvalue(layer,arrayx[i],arrayy[i],menu_selected-1,1)
          If menu_selected<count And value>menu_selected Then layer_setvalue(layer,arrayx[i],arrayy[i],menu_selected+1,1)
        EndIf

      ElseIf selected=1 Then ;anim

        code=layer_getcode(layer,arrayx[i],arrayy[i])
        If code=0 Or code=2 Then
          value=layer_getvalue(layer,arrayx[i],arrayy[i])
          If menu_selected>1     And value<menu_selected Then layer_setvalue(layer,arrayx[i],arrayy[i],menu_selected-1,2)
          If menu_selected<count And value>menu_selected Then layer_setvalue(layer,arrayx[i],arrayy[i],menu_selected+1,2)
        EndIf

      ElseIf selected=2 Then ;data

        value=layer_getdata(layer,arrayx[i],arrayy[i])
        If menu_selected>1     And value<menu_selected Then layer_setdata(layer,arrayx[i],arrayy[i],menu_selected-1)
        If menu_selected<count And value>menu_selected Then layer_setdata(layer,arrayx[i],arrayy[i],menu_selected+1)

      EndIf

    EndIf
  Next


  editor_update(2)
End Function





;---------------------------------------------------------------------
;mode: 0=mousedown, 1=mousemove
;---------------------------------------------------------------------
Function editor_mark(mode)
  Local result
  Local sliderx
  Local slidery
  Local tmp
  Local x
  Local y

  If editor_action1=0 Then Return

  x=editor_x()
  y=editor_y()
  sliderx=SliderValue(editor_sliderx)+layer_minx
  slidery=SliderValue(editor_slidery)+layer_miny

  Select layer\code
    Case layer_map
      result=layer_map_coord(layer,sliderx,slidery,x,y,1)
    Case layer_iso1
      result=layer_iso1_coord(layer,sliderx,slidery,x,y,1)
    Case layer_iso2
      result=layer_iso2_coord(layer,sliderx,slidery,x,y,1)
    Case layer_hex1
      result=layer_hex1_coord(layer,sliderx,slidery,x,y,1)
    Case layer_hex2
      result=layer_hex2_coord(layer,sliderx,slidery,x,y,1)
    Default
      Return
  End Select


  If mode=0 Then
    If result=0 Then
      editor_layer=Null
    ElseIf result=1 Then
      editor_x1=layer_x
      editor_y1=layer_y
      editor_x2=layer_x
      editor_y2=layer_y
      editor_x3=layer_x
      editor_y3=layer_y
      editor_x4=layer_x
      editor_y4=layer_y
      editor_layer=layer
    EndIf
  ElseIf mode=1 Then
    If result=1 Then
      editor_x4=layer_x
      editor_y4=layer_y
    EndIf

    editor_x1=editor_x3
    editor_y1=editor_y3
    editor_x2=editor_x4
    editor_y2=editor_y4

    If editor_x1>editor_x2 Then
      tmp=editor_x1
      editor_x1=editor_x2
      editor_x2=tmp
    EndIf

    If editor_y1>editor_y2 Then
      tmp=editor_y1
      editor_y1=editor_y2
      editor_y2=tmp
    EndIf
  EndIf


  editor_update(2)
End Function





;---------------------------------------------------------------------
Function editor_pick()
  Local nr
  Local result
  Local selected
  Local sliderx
  Local slidery
  Local x
  Local y

  If editor_action1=0 Then Return
  editor_action1=0

  x=editor_x()
  y=editor_y()
  sliderx=SliderValue(editor_sliderx)+layer_minx
  slidery=SliderValue(editor_slidery)+layer_miny

  Select layer\code
    Case layer_map
      result=layer_map_coord(layer,sliderx,slidery,x,y,1)
    Case layer_iso1
      result=layer_iso1_coord(layer,sliderx,slidery,x,y,1)
    Case layer_iso2
      result=layer_iso2_coord(layer,sliderx,slidery,x,y,1)
    Case layer_hex1
      result=layer_hex1_coord(layer,sliderx,slidery,x,y,1)
    Case layer_hex2
      result=layer_hex2_coord(layer,sliderx,slidery,x,y,1)
    Default
      Return
  End Select
  If result=0 Then Return

  selected=SelectedGadgetItem(menu)
  If selected=0 Or selected=1 Then ;frame/anim
    If layer\tile=Null Then Return
    If layer\tile\image=0 Then Return

    Select layer_getcode(layer,layer_x,layer_y)
      Case 0 : SelectGadgetItem menu,0
      Case 1 : SelectGadgetItem menu,0
      Case 2 : SelectGadgetItem menu,1
    End Select

    nr=layer_getvalue(layer,layer_x,layer_y)
    menu_select(nr)
  ElseIf selected=2 Then ;data
    nr=layer_getdata(layer,layer_x,layer_y)
    menu_select(nr)
  EndIf
End Function





;---------------------------------------------------------------------
Function editor_rect()
  Local bank
  Local defval
  Local selected
  Local x
  Local y

  If layer=Null Then Goto abort
  If layer<>editor_layer Then Goto abort

  selected=SelectedGadgetItem(menu)
  If selected=0 Or selected=1 Then
    If layer\tile=Null Then Goto abort
    If layer\tile\image=0 Then Goto abort
  EndIf

  ;GET DEFAULT VALUE
  If selected=0 And menu_selected>0 Then  
    defval=PeekByte(layer\tile\bankd,menu_selected-1)
  ElseIf selected=1 And menu_selected>0 Then
    bank  =PeekInt (layer\tile\banka,menu_selected*4-4) ;animbank
    defval=PeekByte(bank,5)                             ;default value
  EndIf

  For x=editor_x1 To editor_x2
    If selected=0 Then ;frame
      layer_setvalue(layer,x,editor_y1,menu_selected,1)
      layer_setvalue(layer,x,editor_y2,menu_selected,1)
      If setup_usedef=1 Then layer_setdata(layer,x,editor_y1,defval,1)
      If setup_usedef=1 Then layer_setdata(layer,x,editor_y2,defval,1)
    ElseIf selected=1 Then ;anim
      layer_setvalue(layer,x,editor_y1,menu_selected,2)
      layer_setvalue(layer,x,editor_y2,menu_selected,2)
      If setup_usedef=1 Then layer_setdata(layer,x,editor_y1,defval,1)
      If setup_usedef=1 Then layer_setdata(layer,x,editor_y2,defval,1)
    ElseIf selected=2 Then ;data
      layer_setdata(layer,x,editor_y1,menu_selected)
      layer_setdata(layer,x,editor_y2,menu_selected)
    EndIf
  Next

  For y=editor_y1 To editor_y2
    If selected=0 Then ;frame
      layer_setvalue(layer,editor_x1,y,menu_selected,1)
      layer_setvalue(layer,editor_x2,y,menu_selected,1)
      If setup_usedef=1 Then layer_setdata(layer,editor_x1,y,defval,1)
      If setup_usedef=1 Then layer_setdata(layer,editor_x2,y,defval,1)
    ElseIf selected=1 Then ;anim
      layer_setvalue(layer,editor_x1,y,menu_selected,2)
      layer_setvalue(layer,editor_x2,y,menu_selected,2)
      If setup_usedef=1 Then layer_setdata(layer,editor_x1,y,defval,1)
      If setup_usedef=1 Then layer_setdata(layer,editor_x2,y,defval,1)
    ElseIf selected=2 Then ;data
      layer_setdata(layer,editor_x1,y,menu_selected)
      layer_setdata(layer,editor_x2,y,menu_selected)
    EndIf
  Next

  .abort
  editor_layer=Null
  editor_update(2)
End Function





;---------------------------------------------------------------------
Function editor_resize()
  FreeGadget editor_canvas
  editor_width =ClientWidth (editor)
  editor_height=ClientHeight(editor)
  editor_canvas=CreateCanvas(0,0,editor_width-16,editor_height-16,editor)
  SetGadgetLayout editor_canvas,1,0,1,0
  editor_update()
End Function





;---------------------------------------------------------------------
;mode: 0=mousedown, 1=mouseup, 2=mousemove
;---------------------------------------------------------------------
Function editor_scroll(mode)
  Local sliderx
  Local slidery
  Local x
  Local y

  If mode=0 Then editor_action2=1
  If mode=1 Then editor_action2=0
  If mode<>2 Or editor_action2=0 Then Return

  x=MouseX(editor_scroll)/(toolbar_xzoom*2+1)
  y=MouseY(editor_scroll)/(toolbar_xzoom*2+1)
  MoveMouse 0,0,editor_scroll

  sliderx=SliderValue(editor_sliderx)
  slidery=SliderValue(editor_slidery)

  SetSliderValue editor_sliderx,sliderx+x
  SetSliderValue editor_slidery,slidery+y

  editor_update(2)
End Function





;---------------------------------------------------------------------
Function editor_seq()
  Local bank
  Local count
  Local defval
  Local max
  Local maxanims
  Local maxcount
  Local selected
  Local value
  Local x
  Local y

  If layer=Null Then Goto abort
  If layer<>editor_layer Then Goto abort
  If menu_selected=0 Then Goto abort

  selected=SelectedGadgetItem(menu)
  If selected=0 Or selected=1 Then ; frame/anim
    If layer\tile=Null Then Goto abort
    If layer\tile\image=0 Then Goto abort

    max=(1 Shl layer\depth1)-1

    maxanims=layer\tile\anims
    If maxanims>max Then maxanims=max

    maxcount=layer\tile\count
    If maxcount>max-maxanims Then maxcount=max-maxanims

    If selected=0 Then count=maxcount
    If selected=1 Then count=maxanims
  ElseIf selected=2 Then ;data
    count=(1 Shl layer\depth2)-1
  EndIf
  If count=0 Then Goto abort

  value=menu_selected

  For y=editor_y1 To editor_y2
    For x=editor_x1 To editor_x2

      ;GET DEFAULT VALUE
      If selected=0 And value>0 Then  
        defval=PeekByte(layer\tile\bankd,value-1)
      ElseIf selected=1 And value>0 Then
        bank  =PeekInt (layer\tile\banka,value*4-4) ;animbank
        defval=PeekByte(bank,5)                     ;default value
      EndIf

      If selected=0 Then ;frame
        layer_setvalue(layer,x,y,value,1)
        If setup_usedef=1 Then layer_setdata(layer,x,y,defval,1)
      ElseIf selected=1 Then ;anim
        layer_setvalue(layer,x,y,value,2)
        If setup_usedef=1 Then layer_setdata(layer,x,y,defval,1)
      ElseIf selected=2 Then ;data
        layer_setdata(layer,x,y,value)
      EndIf

      value=value+1
      If value>count Then value=1
    Next
  Next

  .abort
  editor_layer=Null
  editor_update(2)
End Function





;---------------------------------------------------------------------
;mode: 0=mousedown, 1=mouseup
;---------------------------------------------------------------------
Function editor_select(mode)
  Local i
  Local px
  Local py
  Local result
  Local sizex
  Local sizey
  Local sliderx
  Local slidery
  Local sx1
  Local sx2
  Local sy1
  Local sy2
  Local x
  Local y

  x=editor_x()
  y=editor_y()
  sliderx=SliderValue(editor_sliderx)+layer_minx
  slidery=SliderValue(editor_slidery)+layer_miny

  If mode=0 Then
    editor_move=0

    If layer\code=layer_map Or layer\code=layer_iso1 Or layer\code=layer_iso2 Or layer\code=layer_hex1 Or layer\code=layer_hex2 Or layer\code=layer_clone Then
      If toolbar_xmap=1 Then Return
    EndIf

    Select layer\code ;CURRENT GEO-OBJECTS
      Case layer_point
        result=layer_point_over(layer,sliderx,slidery,x,y)
      Case layer_line
        result=layer_line_over(layer,sliderx,slidery,x,y)
      Case layer_rect
        result=layer_rect_over(layer,sliderx,slidery,x,y)
      Case layer_oval
        result=layer_oval_over(layer,sliderx,slidery,x,y)
    End Select
    If result=>1 Then Goto skip

    For i=layer_count To 1 Step -1 ;OTHER GEO-OBJECTS
      If layer_list[i]\visible=1 Then
        Select layer_list[i]\code
          Case layer_point
            result=layer_point_over(layer_list[i],sliderx,slidery,x,y)
          Case layer_line
            result=layer_line_over(layer_list[i],sliderx,slidery,x,y)
          Case layer_rect
            result=layer_rect_over(layer_list[i],sliderx,slidery,x,y)
          Case layer_oval
            result=layer_oval_over(layer_list[i],sliderx,slidery,x,y)
        End Select
        If result=>1 Then list_select(i) : Goto skip
      EndIf
    Next

    Select layer\code ;CURRENT IMAGE-OBJECTS
      Case layer_image
        result=layer_image_over(layer,sliderx,slidery,x,y)
        If result=1 Then Goto skip
      Case layer_block
        result=layer_block_over(layer,sliderx,slidery,x,y)
        If result=1 Then Return
    End Select

    For i=layer_count To 1 Step -1 ;OTHER IMAGE-OBJECTS
      If layer_list[i]\visible=1 Then
        Select layer_list[i]\code
          Case layer_image
            result=layer_image_over(layer_list[i],sliderx,slidery,x,y)
            If result=1 Then list_select(i) : Goto skip
          Case layer_block
            result=layer_block_over(layer_list[i],sliderx,slidery,x,y)
            If result=1 Then list_select(i) : Return
        End Select
      EndIf
    Next

    Select layer\code ;CURRENT LAYER-OBJECTS
      Case layer_map
        result=layer_map_coord(layer,sliderx,slidery,x,y,1)
      Case layer_iso1
        result=layer_iso1_coord(layer,sliderx,slidery,x,y,1)
      Case layer_iso2
        result=layer_iso2_coord(layer,sliderx,slidery,x,y,1)
      Case layer_hex1
        result=layer_hex1_coord(layer,sliderx,slidery,x,y,1)
      Case layer_hex2
        result=layer_hex2_coord(layer,sliderx,slidery,x,y,1)
      Case layer_clone
        result=layer_clone_coord(layer,sliderx,slidery,x,y,1)
    End Select
    If result=1 Then Goto skip

    For i=layer_count To 1 Step -1 ;OTHER LAYER-OBJECTS
      If layer_list[i]\visible=1 Then
        Select layer_list[i]\code
          Case layer_map
            result=layer_map_coord(layer_list[i],sliderx,slidery,x,y,1)
          Case layer_iso1
            result=layer_iso1_coord(layer_list[i],sliderx,slidery,x,y,1)
          Case layer_iso2
            result=layer_iso2_coord(layer_list[i],sliderx,slidery,x,y,1)
          Case layer_hex1
            result=layer_hex1_coord(layer_list[i],sliderx,slidery,x,y,1)
          Case layer_hex2
            result=layer_hex2_coord(layer_list[i],sliderx,slidery,x,y,1)
          Case layer_clone
            result=layer_clone_coord(layer_list[i],sliderx,slidery,x,y,1)
        End Select
        If result=1 Then
          If toolbar_xmap=1 Then
            SetSliderValue editor_sliderx,0
            SetSliderValue editor_slidery,0
          EndIf
          list_select(i)
          Goto skip
        EndIf
      EndIf
    Next
    Return

    .skip
    px=editor_getscreen(layer\posx,layer\parax,sliderx)
    py=editor_getscreen(layer\posy,layer\paray,slidery)
    editor_deltax=x-px
    editor_deltay=y-py
    editor_move=result
    If KeyDown(29)=0 Then editor_move=0
    Return

  ElseIf mode=1 Then ;MOVE ALL OBJECTS
    If editor_move=0 Then Return

    If layer\code=layer_line Then
      If editor_move=1 Then ;move line
        sizex=layer\sizex-layer\posx
        sizey=layer\sizey-layer\posy
        layer\posx=editor_getcoord(x-editor_deltax,layer\parax,sliderx)
        layer\posy=editor_getcoord(y-editor_deltay,layer\paray,slidery)
        layer\sizex=layer\posx+sizex
        layer\sizey=layer\posy+sizey
      ElseIf editor_move=2 Then ;move 1 point
        layer\posx=editor_getcoord(x,layer\parax,sliderx)
        layer\posy=editor_getcoord(y,layer\paray,slidery)
      ElseIf editor_move=3 Then ;move 2 point
        layer\sizex=editor_getcoord(x,layer\parax,sliderx)
        layer\sizey=editor_getcoord(y,layer\paray,slidery)
      EndIf

    ElseIf layer\code=layer_rect Then
      If editor_move>1 Then
        sx1=editor_getscreen(layer\posx,layer\parax,sliderx)
        sy1=editor_getscreen(layer\posy,layer\paray,slidery)
        sx2=sx1+layer\sizex
        sy2=sy1+layer\sizey
      EndIf

      If editor_move=1 Then ;move line
        layer\posx=editor_getcoord(x-editor_deltax,layer\parax,sliderx)
        layer\posy=editor_getcoord(y-editor_deltay,layer\paray,slidery)
      ElseIf editor_move=2 Then ;move 1 point
        sx1=x
        sy1=y
      ElseIf editor_move=3 Then ;move 2 point
        sx1=x
        sy2=y
      ElseIf editor_move=4 Then ;move 3 point
        sx2=x
        sy1=y
      ElseIf editor_move=5 Then ;move 4 point
        sx2=x
        sy2=y
      EndIf

      If editor_move>1 Then
        layer\posx=editor_getcoord(sx1,layer\parax,sliderx)
        layer\posy=editor_getcoord(sy1,layer\paray,slidery)
        layer\sizex=sx2-sx1
        layer\sizey=sy2-sy1
      EndIf

    ElseIf layer\code=layer_oval Then
      If editor_move=1 Then ;move line
        layer\posx=editor_getcoord(x-editor_deltax,layer\parax,sliderx)
        layer\posy=editor_getcoord(y-editor_deltay,layer\paray,slidery)
      ElseIf editor_move=2 Then ;resize oval
        px=editor_getscreen(layer\posx,layer\parax,sliderx)
        py=editor_getscreen(layer\posy,layer\paray,slidery)
        layer\sizex=Abs(x-px)
        layer\sizey=Abs(y-py)
      EndIf

    Else
      layer\posx=editor_getcoord(x-editor_deltax,layer\parax,sliderx)
      layer\posy=editor_getcoord(y-editor_deltay,layer\paray,slidery)
    EndIf

    editor_update(1)
  EndIf
End Function





;---------------------------------------------------------------------
;mode: 0=mousedown, 1=mouseup, 2=mousemove
;---------------------------------------------------------------------
Function editor_slide(mode)
  Local sliderx
  Local slidery
  Local x
  Local y

  If mode=0 Then
    editor_action3=1
    editor_deltax=MouseX(editor_canvas)
    editor_deltay=MouseY(editor_canvas)
    Return
  ElseIf mode=1 Then
    editor_action3=0
    Return
  ElseIf mode=2 Then
    If editor_action3=0 Then Return
  EndIf

  x=-(editor_deltax-MouseX(editor_canvas))/(toolbar_xzoom*2+1)
  y=-(editor_deltay-MouseY(editor_canvas))/(toolbar_xzoom*2+1)
  MoveMouse editor_deltax,editor_deltay,editor_canvas

  sliderx=SliderValue(editor_sliderx)
  slidery=SliderValue(editor_slidery)

  SetSliderValue editor_sliderx,sliderx-x
  SetSliderValue editor_slidery,slidery-y

  editor_update(2)
End Function





;---------------------------------------------------------------------
Function editor_start()
  editor          =CreatePanel (234,toolbar_height,window_width-234,window_height-toolbar_height,window,1)
  editor_width    =ClientWidth (editor)
  editor_height   =ClientHeight(editor)
  editor_sliderx  =CreateSlider(0,editor_height-16,editor_width-16,16,editor,1)
  editor_slidery  =CreateSlider(editor_width-16,0,16,editor_height-16,editor,2)
  editor_canvas   =CreateCanvas(0,0,editor_width-16,editor_height-16,editor)
  editor_scroll   =CreateCanvas(editor_width-16,editor_height-16,16,16,editor)

  SetGadgetLayout editor,1,1,1,1
  SetGadgetLayout editor_sliderx,1,1,0,1
  SetGadgetLayout editor_slidery,0,1,1,1
  SetGadgetLayout editor_canvas,1,0,1,0
  SetGadgetLayout editor_scroll,0,1,0,1


  SetBuffer CanvasBuffer(editor_scroll)
  ClsColor 100,150,200
  Cls
  FlipCanvas editor_scroll
End Function





;---------------------------------------------------------------------
;mode: 0=calculate layer range (slow)
;      1=don't calculate layer range (fast)
;      2=don't calculate layer range and don't refresh after mouseup (ultrafast)
;---------------------------------------------------------------------
Function editor_update(mode=0)
  Local b
  Local canvash
  Local canvasw
  Local g
  Local i
  Local px
  Local py
  Local r
  Local sliderx
  Local slidery

  If toolbar_xmap=1 And (layer\code=layer_map Or layer\code=layer_iso1 Or layer\code=layer_iso2 Or layer\code=layer_hex1 Or layer\code=layer_hex2 Or layer\code=layer_clone) Then
    editor_update2()
    Return
  EndIf

  If mode=0 Then layer_range()
  If mode=1 Then editor_refresh=1

  canvasw=editor_width()
  canvash=editor_height()

  SetSliderRange editor_sliderx,canvasw,layer_maxx-layer_minx
  SetSliderRange editor_slidery,canvash,layer_maxy-layer_miny

  sliderx=SliderValue(editor_sliderx)+layer_minx
  slidery=SliderValue(editor_slidery)+layer_miny

  SetBuffer CanvasBuffer(editor_canvas)


  If layer_list[0]\visible=1 Or layer_nr=0 Then ;BACKGROUND
    If layer_list[0]\mode=1 Then
      r=(layer_colour And $FF0000)/$10000
      g=(layer_colour And $FF00)/$100
      b=(layer_colour And $FF)
      ClsColor r,g,b
      Cls
    ElseIf layer_list[0]\mode=>2 And layer_handle<>0 Then
      px=editor_getscreen(layer_list[0]\posx,layer_list[0]\parax,sliderx)
      py=editor_getscreen(layer_list[0]\posy,layer_list[0]\paray,slidery)
      TileBlock layer_handle,px,py
    Else
      ClsColor 0,0,0
      Cls
    EndIf
  Else
    ClsColor 0,0,0
    Cls
  EndIf


  For i=1 To layer_count ;LAYER
    If layer_list[i]\visible=1 Or layer_nr=i Then
      Select layer_list[i]\code
        Case layer_map
          If layer_list[i]\mode=0 Then
            layer_map_draw(layer_list[i],sliderx,slidery)
          Else
            layer_map_draw_repeat(layer_list[i],sliderx,slidery)
          EndIf
        Case layer_iso1
          layer_iso1_draw(layer_list[i],sliderx,slidery)
        Case layer_iso2
          layer_iso2_draw(layer_list[i],sliderx,slidery)
        Case layer_hex1
          layer_hex1_draw(layer_list[i],sliderx,slidery)
        Case layer_hex2
          layer_hex2_draw(layer_list[i],sliderx,slidery)
        Case layer_clone
          layer_clone_draw(layer_list[i],sliderx,slidery)
        Case layer_image
          layer_image_draw(layer_list[i],sliderx,slidery)
        Case layer_block
          layer_block_draw(layer_list[i],sliderx,slidery)
      End Select
    EndIf
  Next


  r=(setup_color4 And $FF0000)/$10000 ;ZEROPOINT
  g=(setup_color4 And $FF00)/$100
  b=(setup_color4 And $FF)
  Color r,g,b
  If sliderx<0 Or slidery<0 Then
    Rect -sliderx,0,1,canvash
    Rect 0,-slidery,canvasw,1
  EndIf
  Rect -sliderx,-slidery,layer_list[0]\sizex,layer_list[0]\sizey,0


  For i=1 To layer_count ;OBJECT
    If layer_list[i]\visible=1 Or layer_nr=i Then
      Select layer_list[i]\code
        Case layer_point
          layer_point_draw(layer_list[i],sliderx,slidery,layer_nr=i)
        Case layer_line
          layer_line_draw(layer_list[i],sliderx,slidery,layer_nr=i)
        Case layer_rect
          layer_rect_draw(layer_list[i],sliderx,slidery,layer_nr=i)
        Case layer_oval
          layer_oval_draw(layer_list[i],sliderx,slidery,layer_nr=i)
      End Select
    EndIf
  Next


  Select layer\code ;GRID/SELECTION/BORDER
    Case layer_map
      layer_map_grid(layer,sliderx,slidery)
    Case layer_iso1
      layer_iso1_grid(layer,sliderx,slidery)
    Case layer_iso2
      layer_iso2_grid(layer,sliderx,slidery)
    Case layer_hex1
      layer_hex1_grid(layer,sliderx,slidery)
    Case layer_hex2
      layer_hex2_grid(layer,sliderx,slidery)
    Case layer_clone
      layer_clone_grid(layer,sliderx,slidery)
    Case layer_image
      layer_image_grid(layer,sliderx,slidery)
    Case layer_block
      layer_block_grid(layer,sliderx,slidery)
  End Select

  editor_zoom()
  FlipCanvas editor_canvas
End Function





;---------------------------------------------------------------------
Function editor_update2()
  Local canvash
  Local canvasw
  Local sliderx
  Local slidery

  Select layer\code
    Case layer_map
      layer_map_size(layer)
    Case layer_iso1
      layer_iso1_size(layer)
    Case layer_iso2
      layer_iso2_size(layer)
    Case layer_hex1
      layer_hex1_size(layer)
    Case layer_hex2
      layer_hex2_size(layer)
    Case layer_clone
      layer_clone_size(layer)
  End Select

  layer_minx=0
  layer_miny=0

  canvasw=editor_width()
  canvash=editor_height()

  SetSliderRange editor_sliderx,canvasw,layer_width
  SetSliderRange editor_slidery,canvash,layer_height

  sliderx=SliderValue(editor_sliderx)
  slidery=SliderValue(editor_slidery)

  SetBuffer CanvasBuffer(editor_canvas)
  ClsColor 192,192,192
  Cls

  Color 0,0,0
  Rect 0,0,layer_width-sliderx,layer_height-slidery

  Select layer\code
    Case layer_map
      layer_map_draw(layer,sliderx,slidery)
      layer_map_grid(layer,sliderx,slidery)
    Case layer_iso1
      layer_iso1_draw(layer,sliderx,slidery)
      layer_iso1_grid(layer,sliderx,slidery)
    Case layer_iso2
      layer_iso2_draw(layer,sliderx,slidery)
      layer_iso2_grid(layer,sliderx,slidery)
    Case layer_hex1
      layer_hex1_draw(layer,sliderx,slidery)
      layer_hex1_grid(layer,sliderx,slidery)
    Case layer_hex2
      layer_hex2_draw(layer,sliderx,slidery)
      layer_hex2_grid(layer,sliderx,slidery)
    Case layer_clone
      layer_clone_draw(layer,sliderx,slidery)
      layer_clone_grid(layer,sliderx,slidery)
  End Select

  editor_zoom()
  FlipCanvas editor_canvas
End Function





;---------------------------------------------------------------------
;RETURN: editor width
;---------------------------------------------------------------------
Function editor_width()
  If toolbar_xzoom=0 Then
    Return GadgetWidth(editor_canvas)
  Else
    Return GadgetWidth(editor_canvas)/setup_minzoom
  EndIf
End Function





;---------------------------------------------------------------------
;RETURN: editor mouse x
;---------------------------------------------------------------------
Function editor_x()
  If toolbar_xzoom=0 Then
    Return MouseX(editor_canvas)
  Else
    Return (MouseX(editor_canvas)-1)/setup_minzoom
  EndIf
End Function





;---------------------------------------------------------------------
;RETURN: editor mouse y
;---------------------------------------------------------------------
Function editor_y()
  If toolbar_xzoom=0 Then
    Return MouseY(editor_canvas)
  Else
    Return (MouseY(editor_canvas)-1)/setup_minzoom
  EndIf
End Function





;---------------------------------------------------------------------
Function editor_zoom()
  Local buffer
  Local height
  Local i,j
  Local length
  Local width

  If toolbar_xzoom=0 Then Return

  buffer=CanvasBuffer(editor_canvas)
  length=GadgetWidth (editor_canvas)
  width =GadgetWidth (editor_canvas)/setup_minzoom+1
  height=GadgetHeight(editor_canvas)/setup_minzoom+1

  For i=width-1 To 0 Step -1
    for j=0 to setup_minzoom-1
      CopyRect i,0,1,height, i*setup_minzoom+j,0, buffer,buffer
    next
  Next

  For i=height-1 To 0 Step -1
    for j=0 to setup_minzoom-1
       CopyRect 0,i,length,1, 0,i*setup_minzoom+j, buffer,buffer
    next
  Next
End Function