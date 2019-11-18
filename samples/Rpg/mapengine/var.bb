;---------------------------------------------------------------------
;bank:   bank handle (layer\bank4, geo\bank4, map_bank4)
;name:   search for this variable name
;RETURN: variable index (0=not found)
;---------------------------------------------------------------------
Function var_findvar(bank,name$)
  Local count
  Local i
  Local j
  Local length
  Local size
  Local txt$
  Local varbank

  If bank=0 Then Return

  count=BankSize(bank)/4
  length=Len(name$)

  For i=1 To count
    varbank=PeekInt(bank,i*4-4)
    size=PeekByte(varbank,0)

    If length=size Then
      txt$=""
      For j=1 To size
        txt$=txt$+Chr$(PeekByte(varbank,j+1))
      Next
      If txt$=name$ Then Return i
    EndIf
  Next
End Function





;---------------------------------------------------------------------
;bank:   bank handle (layer\bank4, geo\bank4, map_bank4)
;index:  variable index (1.....)
;RETURN: variable value
;---------------------------------------------------------------------
Function var_getvalue$(bank,index)
  Local i
  Local size1
  Local size2
  Local txt$
  Local varbank

  If bank=0 Then Return
  If index<1 Or index>BankSize(bank)/4 Then Return

  varbank=PeekInt (bank,index*4-4)
  size1  =PeekByte(varbank,0)
  size2  =PeekByte(varbank,1)

  For i=1 To size2
    txt$=txt$+Chr$(PeekByte(varbank,i+size1+1))
  Next

  Return txt$
End Function





;---------------------------------------------------------------------
;bank:   bank handle (layer\bank4, geo\bank4, map_bank4)
;index:  variable index (1.....)
;value:  variable value
;---------------------------------------------------------------------
Function var_setvalue(bank,index,value$)
  Local char
  Local i
  Local length
  Local size1
  Local size2
  Local varbank

  If bank=0 Then Return
  If index<1 Or index>BankSize(bank)/4 Then Return

  varbank=PeekInt (bank,index*4-4)
  size1  =PeekByte(varbank,0)
  size2  =PeekByte(varbank,1)
  length=Len(value$)
  If length>255 Then length=255

  ResizeBank varbank,2+size1+length

  For i=1 To length
    char=Asc(Mid$(value$,i,1))
    PokeByte varbank,i+size1+1,char
  Next
End Function