; ---------------------------------------------------------------------
;layer:  layer handle (map only)
;image:  image handle
;RETURN: 0=no collision
;        1=collision
; ---------------------------------------------------------------------
Procedure.l layer_image_collision(*layer.layer, *image.layer)
  Protected bank.l, frame.l, px.l, py.l, start.l
  
  If *layer = 0 : ProcedureReturn #False : EndIf
  If *layer\tile = 0 : ProcedureReturn #False : EndIf
  If *layer\tile\image = 0 : ProcedureReturn #False : EndIf
  If *layer\code <> #layer_map : ProcedureReturn #False : EndIf
  
  If *image = 0 : ProcedureReturn #False : EndIf
  If *image\tile = 0 : ProcedureReturn #False : EndIf
  If *image\tile\image = 0 : ProcedureReturn #False : EndIf
  If *image\code <> #layer_image : ProcedureReturn #False : EndIf
  
  px = map_getscreen(*image\posx, *image\parax, map_scrollx)
  py = map_getscreen(*image\posy, *image\paray, map_scrolly)
  
  If *image\frame >= 1 And *image\frame <= *image\tile\anims
    If *image\mode = 0
      bank = PeekL(*image\tile\bank + (*image\frame * 4) - 4)
      start = PeekW(bank + 10) & $FFFF
      frame = PeekW(bank + (start * 4) + 8) & $FFFF
    Else
      bank = PeekL(*image\tile\bank + (*image\frame * 4) - 4)
      frame = PeekW(bank + (*image\tmp * 4) + 8) & $FFFF
    EndIf
  Else
    frame = *image\frame - *image\tile\anims
  EndIf
  
  If frame >= 1 And frame <= *image\tile\count
    ;-? Neuer Parameter
    ProcedureReturn layer_map_collision(*layer, px, py, *image\tile\image, *image\tile\sizex, *image\tile\sizey, frame - 1)
  EndIf
EndProcedure





; ---------------------------------------------------------------------
;layer: layer handle
; ---------------------------------------------------------------------
Procedure layer_image_draw(*layer.layer)
  Protected bank.l, frame.l, px.l, py.l, start.l
  
  If *layer = 0 : ProcedureReturn #False : EndIf
  If *layer\tile = 0 : ProcedureReturn #False : EndIf
  If *layer\tile\image = 0 : ProcedureReturn #False : EndIf
  
  px = map_getscreen(*layer\posx, *layer\parax, map_scrollx)
  py = map_getscreen(*layer\posy, *layer\paray, map_scrolly)
  
  If *layer\frame >= 1 And *layer\frame <= *layer\tile\anims
    If *layer\mode = 0
      bank = PeekL(*layer\tile\bank + (*layer\frame * 4) - 4)
      start = PeekW(bank + 10) & $FF
      frame = PeekW(bank + (start * 4) + 8) & $FFFF
    Else
      bank = PeekL(*layer\tile\bank + (*layer\frame * 4) - 4)
      frame = PeekW(bank + (*layer\tmp * 4) + 8) & $FFFF
    EndIf
  Else
    frame = *layer\frame - *layer\tile\anims
  EndIf
  
  If frame >= 1 And frame <= *layer\tile\count
    ClipSprite(*layer\tile\image, (frame - 1) * *layer\tile\sizex, 0, *layer\tile\sizex, *layer\tile\sizey)
    If *layer\mask = 1 : DisplayTransparentSprite(*layer\tile\image, px, py) : EndIf
    If *layer\mask = 0 : DisplaySprite(*layer\tile\image, px, py) : EndIf
  EndIf
EndProcedure 
; jaPBe Version=1.4.4.25
; FoldLines=0030004C
; Build=0
; CompileThis=mapengine.pbi
; FirstLine=0
; CursorPosition=37
; ExecutableFormat=Windows
; DontSaveDeclare
; EOF