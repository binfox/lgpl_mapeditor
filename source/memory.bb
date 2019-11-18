;---------------------------------------------------------------------
;bank:   bank handle
;offset: bank offset
;bytes:  size in bytes
;mode:   0=add at top
;        1=add at bottom
;max:    max bank size
;---------------------------------------------------------------------
Function memory_add(bank,offset,bytes,mode,max)
  Local i
  Local size

  size=BankSize(bank)

  If offset>size-1 Then offset=size-1
  If offset<0 Then offset=0

  If size+bytes>max Then bytes=max-size
  If bytes<=0 Then Return

  ResizeBank bank,size+bytes

  If size>0 Then
    If size-offset-mode>0 Then CopyBank bank,offset+mode,bank,offset+mode+bytes,size-offset-mode

    For i=0 To bytes-1
      PokeByte bank,offset+i+mode,0
    Next
  EndIf
End Function





;---------------------------------------------------------------------
;bank:   bank handle
;offset: bank offset
;bytes:  size in bytes
;---------------------------------------------------------------------
Function memory_delete(bank,offset,bytes)
  Local size

  size=BankSize(bank)

  If offset>size-1 Then offset=size-1
  If offset<0 Then offset=0

  If offset+bytes>size Then bytes=size-offset
  If bytes<=0 Then Return

  If size-offset-bytes>0 Then CopyBank bank,offset+bytes,bank,offset,size-offset-bytes
  ResizeBank bank,size-bytes
End Function





;---------------------------------------------------------------------
;bank:   bank handle
;offset: bank offset
;nr:     value number (1=Char, 2=Hex, 3=UByte, 4=UWord, 5=SByte, 6=SWord, 7=SLong)
;mode:   0=editor string
;        1=textfield string
;RETURN: string value
;---------------------------------------------------------------------
Function memory_getval$(bank,offset,nr,mode=0)
  Local size
  Local txt$
  Local value

  size=BankSize(bank)
  If offset>size-1 Then offset=size-1
  If offset<0 Then offset=0

  Select nr+mode*10
    Case 01
      value=PeekByte(bank,offset)
      Return Chr$(value)

    Case 02
      value=PeekByte(bank,offset)
      Return Right$(Hex$(value),2)

    Case 03
      value=PeekByte(bank,offset)
      Return Right$("000"+Str$(value),3)

    Case 04
      If offset+1<=size-1 Then
        value=PeekShort(bank,offset)
        Return Right$("00000"+Str$(value),5)
      Else
        Return "-----"
      EndIf

    Case 05
      value=PeekByte(bank,offset)
      If value>127 Then value=value-256
      txt$=Right$("000"+Str$(Abs(value)),3)
      If value<0 Then Return "-"+txt$ Else Return "+"+txt$

    Case 06
      If offset+1<=size-1 Then
        value=PeekShort(bank,offset)
        If value>32767 Then value=value-65536
        txt$=Right$("00000"+Str$(Abs(value)),5)
        If value<0 Then Return "-"+txt$ Else Return "+"+txt$
      Else
        Return "------"
      EndIf

    Case 07
      If offset+3<=size-1 Then
        value=PeekInt(bank,offset)
        txt$=Right$("0000000000"+Str$(Abs(value)),10)
        If value<0 Then Return "-"+txt$ Else Return "+"+txt$
      Else
        Return "-----------"
      EndIf

    Case 11
      value=PeekByte(bank,offset)
      Return Chr$(value)

    Case 12
      value=PeekByte(bank,offset)
      Return Right$(Hex$(value),2)

    Case 13
      value=PeekByte(bank,offset)
      Return Str$(value)

    Case 14
      If offset+1<=size-1 Then
        value=PeekShort(bank,offset)
        Return Str$(value)
      Else
        Return "-----"
      EndIf

    Case 15
      value=PeekByte(bank,offset)
      If value>127 Then value=value-256
      Return Str$(value)

    Case 16
      If offset+1<=size-1 Then
        value=PeekShort(bank,offset)
        If value>32767 Then value=value-65536
        Return Str$(value)
      Else
        Return "------"
      EndIf

    Case 17
      If offset+3<=size-1 Then
        value=PeekInt(bank,offset)
        Return Str$(value)
      Else
        Return "-----------"
      EndIf

    End Select
End Function





;---------------------------------------------------------------------
;bank:   input bank handle
;offset: bank offset
;max:    max chars
;RETURN: text string
;---------------------------------------------------------------------
Function memory_peekstr$(bank,offset,max)
  Local char
  Local i
  Local txt$

  For i=0 To max-1
    char=PeekByte(bank,offset+i)
    If char=0 Then Return txt$
    txt$=txt$+Chr$(char)
  Next
  Return txt$
End Function





;---------------------------------------------------------------------
;bank:   output bank handle
;offset: bank offset
;max:    max chars
;txt:    text
;---------------------------------------------------------------------
Function memory_pokestr(bank,offset,max,txt$)
  Local char
  Local i
  Local lng

  lng=Len(txt$)
  For i=0 To max-1
    char=0
    If lng>i Then char=Asc(Mid$(txt$,i+1,1))
    PokeByte bank,offset+i,char
  Next
End Function