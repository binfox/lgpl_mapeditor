; ---------------------------------------------------------------------
;nr:     layer number
;RETURN: layer handle
; ---------------------------------------------------------------------
Procedure.l extra_num2layer(nr.l) ; Gibt Pointer auf Layer-Elemen zur�ck
  Protected count.l, *layer, *result
  
  *layer = @layer()
  
  ForEach layer()
    If layer()\code = #layer_map Or layer()\code = #layer_iso1 Or layer()\code = #layer_iso2 Or layer()\code = #layer_hex1 Or layer()\code = #layer_hex2 Or layer()\code = #layer_clone
      count + 1
      If count = nr : Break : EndIf
    EndIf
  Next
  
  *result = @layer()
  If *layer > 8 : ChangeCurrentElement(layer(), *layer) : EndIf
  
  ProcedureReturn *result 
EndProcedure





; ---------------------------------------------------------------------
;nr:     tile number
;RETURN: tileset handle
; ---------------------------------------------------------------------
Procedure.l extra_num2tile(nr.l)
  Protected count.l, *tile, *result
  
  *tile = @tile()
  
  ForEach tile()
    count + 1
    If count = nr : Break : EndIf
  Next
  
  *result = @tile()
  If *tile > 8 : ChangeCurrentElement(tile(), *tile) : EndIf
  
  ProcedureReturn *result
  
EndProcedure





; ---------------------------------------------------------------------
;file:   input file handle
;max:    max string length
;RETURN: text string
; ---------------------------------------------------------------------
Procedure.s extra_readstr(max.l)
  Protected char.l, i.l, noadd.l, txt.s
  
  For i = 1 To max
    char = ReadByte() & $FF
    If char = 0 : noadd = 1 : EndIf
    If noadd = 0 : txt + Chr(char) : EndIf
  Next
  
  ProcedureReturn txt
EndProcedure 
; jaPBe Version=1.4.4.25
; FoldLines=001E002D
; Build=0
; CompileThis=mapengine.pbi
; FirstLine=0
; CursorPosition=11
; ExecutableFormat=Windows
; DontSaveDeclare
; EOF