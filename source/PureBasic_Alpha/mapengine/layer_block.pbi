; ---------------------------------------------------------------------
;layer: layer handle
; ---------------------------------------------------------------------
Procedure layer_block_draw(*layer.layer)
  Protected bank.l, frame.l, px.l, py.l, reyult.l, start.l
  
  If *layer = 0 : ProcedureReturn #False : EndIf
  If *layer\tile = 0 : ProcedureReturn #False : EndIf
  If *layer\tile\image = 0 : ProcedureReturn #False : EndIf
  
  If *layer\layer
    Select *layer\layer\code
      Case #layer_map
        result = layer_map_pos(*layer\layer, *layer\posx, *layer\posy)
      Case #layer_iso1
        result = layer_iso1_pos(*layer\layer, *layer\posx, *layer\posy)
      Case #layer_iso2
        result = layer_iso2_pos(*layer\layer, *layer\posx, *layer\posy)
      Case #layer_hex1
        result = layer_hex1_pos(*layer\layer, *layer\posx, *layer\posy)
      Case #layer_hex2
        result = layer_hex2_pos(*layer\layer, *layer\posx, *layer\posy)
      Case #layer_clone
        result = layer_clone_pos(*layer\layer, *layer\posx, *layer\posy)
    EndSelect
  EndIf
  
  If result = 1
    px = layer_x + *layer\parax
    py = layer_y + *layer\paray
  Else
    px = *layer\parax - map_scrollx
    py = *layer\paray - map_scrolly
  EndIf
  
  If *layer\frame >= 1 And *layer\frame <= *layer\tile\anims
    If *layer\mode = 0
      bank = PeekL(*layer\tile\bank + (*layer\frame * 4) - 4)
      start = PeekW(bank + 10) & $FFFF
      frame = PeekW(bank + (start * 4) + 8) & $FFFF
    Else
      bank = PeekL(*layer\tile\bank + (*layer\frame * 4) - 4)
      frame = PeekW(bank + (*layer\tmp * 4) + 8)
    EndIf
  Else
    frame = *layer\frame - *layer\tile\anims
  EndIf
  
  If frame >= 1 And frame <= *layer\tile\count
    ClipSprite(*layer\tile\image, (frame - 1) * *layer\tile\sizex, 0, *layer\tile\sizex, *layer\tile\sizey)
    If *layer\mask = 1 : DisplayTransparentSprite(*layer\tile\image, px, py) : EndIf
    If *layer\mask = 0 : DisplaySprite(*layer\tile\image, px, py) : EndIf
  EndIf
  
EndProcedure





; ---------------------------------------------------------------------
;layer: layer handle
;
;RETURN LAYER_X: global pos x
;RETURN LAYER_Y: global pos y
; ---------------------------------------------------------------------
Procedure layer_block_pos(*layer.layer)
  Protected px.l, py.l, result.l
  
  If *layer = 0 : ProcedureReturn #False : EndIf
  
  If *layer\layer
    Select *layer\layer\code
      Case #layer_map
        result = layer_map_pos(*layer\layer, *layer\posx, *layer\posy)
      Case #layer_iso1
        result = layer_iso1_pos(*layer\layer, *layer\posx, *layer\posy)
      Case #layer_iso2
        result = layer_iso2_pos(*layer\layer, *layer\posx, *layer\posy)
      Case #layer_hex1
        result = layer_hex1_pos(*layer\layer, *layer\posx, *layer\posy)
      Case #layer_hex2
        result = layer_hex2_pos(*layer\layer, *layer\posx, *layer\posy)
      Case #layer_clone
        result = layer_clone_pos(*layer\layer, *layer\posx, *layer\posy)
    EndSelect
  EndIf
  
  If result = 1
    px = layer_x + *layer\parax
    py = layer_y + *layer\paray
    layer_x = map_getcoord(px, *layer\layer\parax, map_scrollx)
    layer_y = map_getcoord(py, *layer\layer\paray, map_scrolly)
  Else
    layer_x = *layer\parax
    layer_y = *layer\paray
  EndIf
EndProcedure 
; jaPBe Version=1.4.4.25
; Build=0
; CompileThis=mapengine.pbi
; FirstLine=49
; CursorPosition=70
; ExecutableFormat=Windows
; DontSaveDeclare
; EOF