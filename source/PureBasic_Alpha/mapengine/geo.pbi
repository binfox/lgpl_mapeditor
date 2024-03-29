Structure geo
  ascii.l
  bank3.l
  code.l
  name.s
  parax.l
  paray.l
  posx.l
  posy.l
  sizex.l
  sizey.l
  visible.l
EndStructure

NewList geo.geo()

; ---------------------------------------------------------------------
;geo: geo handle
; ---------------------------------------------------------------------
Procedure geo_delete(*geo.geo)
  If *geo = 0 : ProcedureReturn #False : EndIf
  If *geo\bank3 : FreeMemory(*geo\bank3) : EndIf
  ChangeCurrentElement(geo(), *geo)
  ProcedureReturn DeleteElement(geo())
EndProcedure


; ---------------------------------------------------------------------
Procedure geo_reset()
  ForEach geo()
    If geo()\bank3 : FreeMemory(geo()\bank3) : EndIf
    DeleteElement(geo())
  Next
EndProcedure 
; jaPBe Version=1.4.4.25
; FoldLines=0000000C00130018001C0021
; Build=0
; CompileThis=mapengine.pbi
; FirstLine=0
; CursorPosition=13
; ExecutableFormat=Windows
; DontSaveDeclare
; EOF