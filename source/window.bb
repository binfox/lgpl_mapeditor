Global window
Global window_font1
Global window_font2
Global window_font3
Global window_font5
Global window_font7
Global window_height
Global window_icons1
Global window_icons2
Global window_maxx
Global window_maxy
Global window_strip2
Global window_strip3
Global window_txt$
Global window_width





;---------------------------------------------------------------------
Function window_exit()
  FreeIconStrip window_strip3
  FreeIconStrip window_strip2
  FreeImage     window_icons2
  FreeImage     window_icons1
  FreeImage     window_font7
  FreeImage     window_font5
  FreeFont      window_font3
  FreeFont      window_font2
  FreeFont      window_font1
  FreeGadget    window
End Function





;---------------------------------------------------------------------
Function window_start()
  window_maxx  =ClientWidth  (Desktop())
  window_maxy  =ClientHeight (Desktop())
  window       =CreateWindow ("",(window_maxx-1024)/2,(window_maxy-768)/2,1024,768,0,1+2+8)
  window_width =ClientWidth  (window)
  window_height=ClientHeight (window)
  window_font1 =LoadFont     ("Arial",16)
  window_font2 =LoadFont     ("Courier",12)
  window_font3 =LoadFont     ("Arial",12)
  window_font5 =LoadAnimImage("image/font5.bmp",5,7,0,10,vram)
  window_font7 =LoadAnimImage("image/font7.bmp",7,10,0,10,vram)
  window_icons1=LoadAnimImage("image/iconstrip1.bmp",16,16,0,13,vram)
  window_icons2=LoadAnimImage("image/iconstrip2.bmp",16,16,0,13,vram)
  window_strip2=LoadIconStrip("image/iconstrip2.bmp")
  window_strip3=LoadIconStrip("image/iconstrip3.bmp")

  If window       =0 Then End
  If window_font1 =0 Then End
  If window_font2 =0 Then End
  If window_font5 =0 Then End
  If window_font7 =0 Then End
  If window_icons1=0 Then End
  If window_icons2=0 Then End
  If window_strip2=0 Then End
  If window_strip3=0 Then End

  MaskImage window_icons1,255,0,255
  MaskImage window_icons2,255,0,255
  SetMinWindowSize window,700,500
End Function





;---------------------------------------------------------------------
Function window_status()
  Local result
  Local sliderx
  Local slidery
  Local txt$
  Local value
  Local x
  Local y

  x=editor_x()
  y=editor_y()
  sliderx=SliderValue(editor_sliderx)+layer_minx
  slidery=SliderValue(editor_slidery)+layer_miny

  If layer\code=layer_map Or layer\code=layer_iso1 Or layer\code=layer_iso2 Or layer\code=layer_hex1 Or layer\code=layer_hex2 Or layer\code=layer_clone Then
    txt$="["+txt$+Str$(layer\sizex)+"x"+Str$(layer\sizey)+"]   "
  EndIf

  txt$=txt$+"X:"+Str$(x+sliderx)
  txt$=txt$+"   Y:"+Str$(y+slidery)

  If toolbar_xtool=0 And editor_action1=1 And editor_move>0 Then
    If layer\code=layer_line And editor_move=2 Then
      txt$=txt$+"   "+language$(246)+":"+Str$(layer\posx)
      txt$=txt$+"   "+language$(247)+":"+Str$(layer\posy)
    ElseIf layer\code=layer_line And editor_move=3 Then
      txt$=txt$+"   "+language$(248)+":"+Str$(layer\sizex)
      txt$=txt$+"   "+language$(249)+":"+Str$(layer\sizey)
    ElseIf layer\code=layer_rect And editor_move>1 Then
      txt$=txt$+"   "+language$(250)+":"+Str$(layer\posx)
      txt$=txt$+"   "+language$(251)+":"+Str$(layer\posy)
      txt$=txt$+"   "+language$(252)+":"+Str$(layer\sizex)
      txt$=txt$+"   "+language$(253)+":"+Str$(layer\sizey)
    ElseIf layer\code=layer_oval And editor_move>1 Then
      txt$=txt$+"   "+language$(204)+":"+Str$(layer\sizex)
      txt$=txt$+"   "+language$(205)+":"+Str$(layer\sizey)
    Else
      txt$=txt$+"   "+language$(250)+":"+Str$(layer\posx)
      txt$=txt$+"   "+language$(251)+":"+Str$(layer\posy)
    EndIf
  Else
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
      Case layer_clone
        result=layer_clone_coord(layer,sliderx,slidery,x,y,1)
    End Select

    If result=1 Then
      txt$=txt$+"   "+language$(254)+":"+Str$(layer_x)
      txt$=txt$+"   "+language$(255)+":"+Str$(layer_y)

      value=layer_getvalue(layer,layer_x,layer_y)
      If value>0 Then txt$=txt$+"   "+language$(149)+":"+Str$(value)

      value=layer_getcode(layer,layer_x,layer_y)
      If value=1 Then txt$=txt$+" ("+language$(72)+")"
      If value=2 Then txt$=txt$+" ("+language$(73)+")"

      value=layer_getvalue2(layer,layer_x,layer_y)
      If value>0 Then txt$=txt$+"   "+language$(256)+":"+Str$(value)

      value=layer_getdata(layer,layer_x,layer_y)
      If value>0 Then txt$=txt$+"   "+language$(74)+":"+Str$(value)
    EndIf
  EndIf

  If editor_layer<>Null Then
      txt$=txt$+"   "+language$(257)+":"+Str$(editor_x1)
      txt$=txt$+","  +Str$(editor_y1)
      txt$=txt$+" - "+Str$(editor_x2)
      txt$=txt$+","  +Str$(editor_y2)
      txt$=txt$+" (" +Str$(editor_x2-editor_x1+1)
      txt$=txt$+"x"  +Str$(editor_y2-editor_y1+1)+")"

      If layer\code=layer_iso2 Or layer\code=layer_hex2 Then
        If ((editor_y1+layer\start) And 1)=1 Then txt$=txt$+"   "+language$(258)
      ElseIf layer\code=layer_hex1 Then
        If ((editor_x1+layer\start) And 1)=1 Then txt$=txt$+"   "+language$(258)
      EndIf
  EndIf

  If window_txt$=txt$ Then Return
  window_txt$=txt$
  SetStatusText window,txt$
End Function