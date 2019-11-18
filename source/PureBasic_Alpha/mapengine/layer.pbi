
; ---------------------------------------------------------------------
;layer: layer handle
; ---------------------------------------------------------------------
Procedure layer_delete(*layer.layer)
  If *layer = 0 : ProcedureReturn #False : EndIf
  
  If *layer\code <> #layer_clone
    If *layer\bank1 : FreeMemory(*layer\bank1) : EndIf
    If *layer\bank2 : FreeMemory(*layer\bank2) : EndIf
  EndIf
  
  If *layer\bank3 : FreeMemory(*layer\bank3) : EndIf
  
  ChangeCurrentElement(layer(), *layer)
  DeleteElement(layer())
EndProcedure






; ---------------------------------------------------------------------
;layer:  layer handle
;x:      tile pos x
;y:      tile pos y
;RETURN: 0=void, 1=frame, 2=anim
; ---------------------------------------------------------------------
Procedure layer_getcode(*layer.layer, x.l, y.l)
  Protected anims.l, count.l, mode.l, offset.l, value.l
  
  If *layer = 0 : ProcedureReturn #False : EndIf
  If *layer\bank1 = 0 : ProcedureReturn #False : EndIf
  If x < 0 Or y < 0 Or x > *layer\sizex - 1 Or y > *layer\sizey - 1 : ProcedureReturn #False : EndIf
  
  If *layer\tile
    anims = *layer\tile\anims
    count = *layer\tile\count
  EndIf
  
  If *layer\depth1 = 4
    offset = ((y * *layer\sizex + x) / 2)
    mode = ((y * *layer\sizex + x) & 1) * 4
    value = ((PeekB(*layer\bank1 + offset) & $FF) >> mode) & 15
  ElseIf *layer\depth1 = 8
    offset = y * *layer\sizex + x
    value = PeekB(*layer\bank1 + offset) & $FF
  ElseIf *layer\depth1 = 12
    offset = ((y * *layer\sizex + x) * 3) / 2
    mode = ((y * *layer\sizex + x) & 1) * 4
    value = ((PeekW(*layer\bank1 + offset) & $FFFF) >> mode) & 4095
  ElseIf *layer\depth1 = 16
    offset = (y * *layer\sizex + x) * 2
    value = PeekW(*layer\bank1 + offset) & $FFFF
  EndIf
  
  If value >= 1 And value <= anims
    ProcedureReturn 2
  ElseIf value >= anims + 1 And value <= count + anims
    ProcedureReturn 1
  EndIf
EndProcedure






; ---------------------------------------------------------------------
;layer:  layer handle
;x:      tile pos x
;y:      tile pos y
;RETURN: tile value
; ---------------------------------------------------------------------
Procedure layer_getdata(*layer.layer, x.l, y.l)
  Protected mode.l, offset.l, value.l
  
  If *layer = 0 : ProcedureReturn #False : EndIf
  If *layer\bank2 = 0 : ProcedureReturn #False : EndIf
  If x < 0 Or y < 0 Or x > *layer\sizex - 1 Or y > *layer\sizey - 1 : ProcedureReturn #False : EndIf
  
  If *layer\depth2 = 4
    offset = ((y * *layer\sizex + x) / 2)
    mode = ((y * *layer\sizex + x) & 1) * 4
    value = ((PeekB(*layer\bank2 + offset) & $FF) >> mode) & 15
  ElseIf *layer\depth2 = 8
    offset = y * *layer\sizex + x
    value = PeekB(*layer\bank2 + offset) & $FF
  EndIf
  
  ProcedureReturn value
EndProcedure






; ---------------------------------------------------------------------
;layer:  layer handle
;x:      tile pos x
;y:      tile pos y
;RETURN: tile value
; ---------------------------------------------------------------------
Procedure layer_getvalue(*layer.layer, x.l, y.l)
  Protected anims.l, count.l, mode.l, offset.l, value.l
  
  If *layer = 0 : ProcedureReturn #False : EndIf
  If *layer\bank1 = 0 : ProcedureReturn #False : EndIf
  If x < 0 Or y < 0 Or x > *layer\sizex - 1 Or y > *layer\sizey - 1 : ProcedureReturn #False : EndIf
  
  If *layer\tile
    anims = *layer\tile\anims
    count = *layer\tile\count
  EndIf
  
  If *layer\depth1 = 4
    offset = ((y * *layer\sizex + x) / 2)
    mode = ((y * *layer\sizex + x) & 1) * 4
    value = ((PeekB(*layer\bank1 + offset) & $FF) >> mode) & 15
  ElseIf *layer\depth1 = 8
    offset = y * *layer\sizex + x
    value = PeekB(*layer\bank1 + offset) & $FF
  ElseIf *layer\depth1 = 12
    offset = ((y * *layer\sizex + x) * 3) / 2
    mode = ((y * *layer\sizex + x) & 1) * 4
    value = ((PeekW(*layer\bank1 + offset) & $FFFF) >> mode) & 4095
  ElseIf *layer\depth1 = 16
    offset = (y * *layer\sizex + x) * 2
    value = PeekW(*layer\bank1 + offset) & $FFFF
  EndIf
  
  If value >= 1 And value <= anims
    ProcedureReturn value
  ElseIf value >= anims + 1 And value <= count + anims
    ProcedureReturn value - anims
  EndIf
EndProcedure





; ---------------------------------------------------------------------
;layer:  layer handle
;x:      tile pos x
;y:      tile pos y
;RETURN: raw hartcoded value
; ---------------------------------------------------------------------
Procedure layer_getvalue2(*layer.layer, x.l, y.l)
  Protected mode.l, offset.l, value.l
  
  If *layer = 0 : ProcedureReturn #False : EndIf
  If *layer\bank1 = 0 : ProcedureReturn #False : EndIf
  If x < 0 Or y < 0 Or x > *layer\sizex - 1 Or y > *layer\sizey - 1 : ProcedureReturn #False : EndIf
  
  If *layer\depth1 = 4
    offset = ((y * *layer\sizex + x) / 2)
    mode = ((y * *layer\sizex + x) & 1) * 4
    value = ((PeekB(*layer\bank1 + offset) & $FF) >> mode) & 15
  ElseIf *layer\depth1 = 8
    offset = y * *layer\sizex + x
    value = PeekB(*layer\bank1 + offset) & $FF
  ElseIf *layer\depth1 = 12
    offset = ((y * *layer\sizex + x) * 3) / 2
    mode = ((y * *layer\sizex + x) & 1) * 4
    value = ((PeekW(*layer\bank1 + offset) & $FFFF) >> mode) & 4095
  ElseIf *layer\depth1 = 16
    offset = (y * *layer\sizex + x) * 2
    value = PeekW(*layer\bank1 + offset) & $FFFF
  EndIf
  
  ProcedureReturn value
EndProcedure






; ---------------------------------------------------------------------
Procedure layer_reset()
  ForEach layer()
    If layer()\code <> #layer_clone
      If layer()\bank1 : FreeMemory(layer()\bank1) : EndIf
      If layer()\bank2 : FreeMemory(layer()\bank2) : EndIf
    EndIf
    If layer()\bank3 : FreeMemory(layer()\bank3) : EndIf
    DeleteElement(layer())
  Next
EndProcedure





; ---------------------------------------------------------------------
;layer: layer handle
;x:     tile pos x
;y:     tile pos y
;value: tile value
; ---------------------------------------------------------------------
Procedure layer_setdata(*layer.layer, x.l, y.l, value.l)
  Protected mode.l, offset.l, value2.l
  
  If *layer = 0 : ProcedureReturn #False : EndIf
  If *layer\bank2 = 0 : ProcedureReturn #False : EndIf
  If x < 0 Or y < 0 Or x > *layer\sizex - 1 Or y > *layer\sizey - 1 : ProcedureReturn #False : EndIf
  If  value < 0 : value = 0 : EndIf
  
  If *layer\depth2 = 4
    If value > 15 : value = 0 : EndIf
    offset = ((y * *layer\sizex + x) / 2)
    mode = ((y * *layer\sizex + x) & 1) * 4
    value2 = PeekB(*layer\bank2 + offset) & ($F0 >> mode)
    PokeB(*layer\bank2 + offset, value2 | (value << mode))
  ElseIf *layer\depth2 = 8
    If value > 255 : value = 0 : EndIf
    offset = y * *layer\sizex + x
    PokeB(*layer\bank2 + offset, value)
  EndIf
EndProcedure






; ---------------------------------------------------------------------
;layer: layer handle
;x:     tile pos x
;y:     tile pos y
;value: tile value
;code:  0=void, 1=frame, 2=anim
; ---------------------------------------------------------------------
Procedure layer_setvalue(*layer.layer, x.l, y.l, value.l, code.l)
  Protected anims.l, count.l, mode.l, offset.l, value2.l
  
  If *layer = 0 : ProcedureReturn #False : EndIf
  If *layer\bank1 = 0 : ProcedureReturn #False : EndIf
  If x < 0 Or y < 0 Or x > *layer\sizex - 1 Or y > *layer\sizey - 1 : ProcedureReturn #False : EndIf
  
  If *layer\tile
    anims = *layer\tile\anims
    count = *layer\tile\count
  EndIf
  
  If code = 1
    If value < 1 Or value > count : value = 0 : EndIf
    If value < 0 : value + anims : EndIf
  ElseIf code = 2
    If value < 1 Or value > anims : value = 0 : EndIf
  Else
    value = 0
  EndIf
  
  If *layer\depth1 = 4
    If value > 15 : value = 0 : EndIf
    offset = ((y * *layer\sizex + x) / 2)
    mode = ((y * *layer\sizex + x) & 1) * 4
    value2 = PeekB(*layer\bank1 + offset) & ($F0 >> mode)
    PokeB(*layer\bank1 + offset, value2 | (value << mode))
  ElseIf *layer\depth1 = 8
    If value > 255 : value = 0 : EndIf
    offset = y * *layer\sizex + x
    PokeB(*layer\bank1 + offset, value)
  ElseIf *layer\depth1 = 12
    If value > 4095 : value = 0 : EndIf
    offset = ((y * *layer\sizex + x) * 3) / 2
    mode = ((y * *layer\sizex + x) & 1)
    value2 = PeekW(*layer\bank1 + offset) & ($F000 >> (mode * 12))
    PokeW(*layer\bank1 + offset, value2 | (value << (mode * 4)))
  ElseIf *layer\depth1 = 16
    If value > 65535 : value = 0 : EndIf
    offset = (y * *layer\sizex + x) * 2
    PokeW(*layer\bank1 + offset, value)
  EndIf
EndProcedure







; ---------------------------------------------------------------------
;layer: layer handle
;x:     tile pos x
;y:     tile pos y
;value: raw hardcoded value
; ---------------------------------------------------------------------
Procedure layer_setvalue2(*layer.layer, x.l, y.l, value.l)
  Protected anims.l, count.l, mode.l, offset.l, value2.l
  
  If *layer = 0 : ProcedureReturn #False : EndIf
  If *layer\bank1 = 0 : ProcedureReturn #False : EndIf
  If x < 0 Or y < 0 Or x > *layer\sizex - 1 Or y > *layer\sizey - 1 : ProcedureReturn #False : EndIf
  If value < 0 : value = 0 : EndIf
  
  If *layer\depth1 = 4
    If value > 15 : value = 0 : EndIf
    offset = ((y * *layer\sizex + x) / 2)
    mode = ((y * *layer\sizex + x) & 1) * 4
    value2 = PeekB(*layer\bank1 + offset) & ($F0 >> mode)
    PokeB(*layer\bank1 + offset, value2 | (value << mode))
  ElseIf *layer\depth1 = 8
    If value > 255 : value = 0 : EndIf
    offset = y * *layer\sizex + x
    PokeB(*layer\bank1 + offset, value)
  ElseIf *layer\depth1 = 12
    If value > 4095 : value = 0 : EndIf
    offset = ((y * *layer\sizex + x) * 3) / 2
    mode = ((y * *layer\sizex + x) & 1)
    value2 = PeekW(*layer\bank1 + offset) & ($F000 >> (mode * 12))
    PokeW(*layer\bank1 + offset, value2 | (value << (mode * 4)))
  ElseIf *layer\depth1 = 16
    If value > 65535 : value = 0 : EndIf
    offset = (y * *layer\sizex + x) * 2
    PokeW(*layer\bank1 + offset, value)
  EndIf
EndProcedure 
; jaPBe Version=1.4.4.25
; FoldLines=00040010004B005C0069008A009600AE00B600BF00CB00DE00EC011601240141
; Build=0
; CompileThis=..\preview_bbplus.pb
; FirstLine=0
; CursorPosition=0
; ExecutableFormat=Windows
; DontSaveDeclare
; EOF