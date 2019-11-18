Type geo        ; 9  10 11 12| <<<OBJECT TYPE
  Field ascii   ; X  X  X  X | ascii-metadata
  Field bank3   ; X  X  X  X | bank metadata
  Field bank4   ; X  X  X  X | bank variables
  Field code    ; X  X  X  X | object code
  Field name$   ; X  X  X  X | object name
  Field parax   ; X  X  X  X | parallax x
  Field paray   ; X  X  X  X | parallax y
  Field posx    ; X  X  X  X | object posx
  Field posy    ; X  X  X  X | object posy
  Field sizex   ;    X  X  X | object sizex
  Field sizey   ;    X  X  X | object sizey
  Field visible ; X  X  X  X | object visible
End Type





;---------------------------------------------------------------------
;geo: geo handle
;---------------------------------------------------------------------
Function geo_delete(geo.geo)
  Local bank
  Local i
  Local size

  If geo=Null Then Return

  If geo\bank3<>0 Then FreeBank geo\bank3

  If geo\bank4<>0 Then
    size=BankSize(geo\bank4)
    For i=0 To size/4-1
      bank=PeekInt(geo\bank4,i*4)
      FreeBank bank
    Next
    FreeBank geo\bank4
  EndIf

  Delete geo
End Function





;---------------------------------------------------------------------
;name:   find this geo name
;RETURN: geo type handle to a object (null=not found)
;---------------------------------------------------------------------
Function geo_find.geo(name$)
  Local geo.geo

  For geo=Each geo
    If geo\name$=name$ Then Return geo
  Next
End Function





;---------------------------------------------------------------------
Function geo_reset()
  Local bank
  Local i
  Local size

  Local geo.geo

  For geo=Each geo
    If geo\bank3<>0 Then FreeBank geo\bank3

    If geo\bank4<>0 Then
      size=BankSize(geo\bank4)
      For i=0 To size/4-1
        bank=PeekInt(geo\bank4,i*4)
        FreeBank bank
      Next
      FreeBank geo\bank4
    EndIf

    Delete geo
  Next
End Function