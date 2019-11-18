Type layer          ; 0 1 2 3 4 5 6 7 8 | <<<OBJECT TYPE
  Field ascii       ; X X X X X X X X X | ascii-metadata
  Field bank1       ;   X X X X X .     | bank basedata
  Field bank2       ;   X X X X X .     | bank datalayer
  Field bank3       ; X X X X X X X X X | bank metadata
  Field bank4       ; X X X X X X X X X | bank variables
  Field code        ; X X X X X X X X X | object code
  Field depth1      ;   X X X X X .     | base depth
  Field depth2      ;   X X X X X .     | data depth
  Field frame       ;               X X | image frame
  Field layer.layer ;             X   X | layer handle
  Field mask        ;   X         . X X | tile mask   / image mask
  Field mode        ; X             X X | backgr mode / image mode (anim)
  Field name$       ; X X X X X X X X X | object name
  Field parax       ; X X X X X X X X X | parallax x / block offset x
  Field paray       ; X X X X X X X X X | parallax y / block offset y
  Field posx        ; X X X X X X X X X | object posx
  Field posy        ; X X X X X X X X X | object posy
  Field sizex       ; X X X X X X .     | object sizex
  Field sizey       ; X X X X X X .     | object sizey
  Field start       ;       X X X . X X | tile shift  / image start (anim)
  Field tile.tile   ;   X X X X X . X X | tileset handle
  Field time        ;               X X | last msec time
  Field tmp         ;               X X | tmp
  Field visible     ; X X X X X X X X X | object visible
End Type

Const layer_back  =00
Const layer_map   =01
Const layer_iso1  =02
Const layer_iso2  =03
Const layer_hex1  =04
Const layer_hex2  =05
Const layer_clone =06
Const layer_image =07
Const layer_block =08
Const layer_point =09
Const layer_line  =10
Const layer_rect  =11
Const layer_oval  =12

Global layer_x
Global layer_y
Global layer_width
Global layer_height





;---------------------------------------------------------------------
;layer: layer handle
;---------------------------------------------------------------------
Function layer_delete(layer.layer)
  Local bank
  Local i
  Local size

  If layer=Null Then Return

  If layer\code<>layer_clone Then
    If layer\bank1<>0 Then FreeBank layer\bank1
    If layer\bank2<>0 Then FreeBank layer\bank2
  EndIf

  If layer\bank3<>0 Then FreeBank layer\bank3

  If layer\bank4<>0 Then
    size=BankSize(layer\bank4)
    For i=0 To size/4-1
      bank=PeekInt(layer\bank4,i*4)
      FreeBank bank
    Next
    FreeBank layer\bank4
  EndIf

  Delete layer
End Function





;---------------------------------------------------------------------
;name:   find this layer name
;RETURN: layer type handle to a object (null=not found)
;---------------------------------------------------------------------
Function layer_find.layer(name$)
  Local layer.layer

  For layer=Each layer
    If layer\name$=name$ Then Return layer
  Next
End Function





;---------------------------------------------------------------------
;layer:  layer handle
;x:      tile pos x
;y:      tile pos y
;RETURN: 0=void, 1=frame, 2=anim
;---------------------------------------------------------------------
Function layer_getcode(layer.layer,x,y)
  Local anims
  Local count
  Local mode
  Local offset
  Local value

  If layer=Null Then Return
  If layer\bank1=0 Then Return
  If x<0 Or y<0 Or x>layer\sizex-1 Or y>layer\sizey-1 Then Return

  If layer\tile<>Null Then
    anims=layer\tile\anims
    count=layer\tile\count
  EndIf

  If layer\depth1=4 Then
    offset=((y*layer\sizex+x)/2)
    mode  =((y*layer\sizex+x) And 1)*4
    value =(PeekByte(layer\bank1,offset) Shr mode) And 15
  ElseIf layer\depth1=8 Then
    offset=y*layer\sizex+x
    value=PeekByte(layer\bank1,offset)
  ElseIf layer\depth1=12 Then
    offset=((y*layer\sizex+x)*3)/2
    mode  =((y*layer\sizex+x) And 1)*4
    value =(PeekShort(layer\bank1,offset) Shr mode) And 4095
  ElseIf layer\depth1=16 Then
    offset=(y*layer\sizex+x)*2
    value=PeekShort(layer\bank1,offset)
  EndIf

  If value=>1 And value<=anims Then ;anim
    Return 2
  ElseIf value=>anims+1 And value<=count+anims Then ;frame
    Return 1
  EndIf
End Function





;---------------------------------------------------------------------
;layer:  layer handle
;x:      tile pos x
;y:      tile pos y
;RETURN: tile value
;---------------------------------------------------------------------
Function layer_getdata(layer.layer,x,y)
  Local mode
  Local offset
  Local value

  If layer=Null Then Return
  If layer\bank2=0 Then Return
  If x<0 Or y<0 Or x>layer\sizex-1 Or y>layer\sizey-1 Then Return

  If layer\depth2=1 Then
    offset=((y*layer\sizex+x)/8)
    mode  =((y*layer\sizex+x) Mod 8)
    value =(PeekByte(layer\bank2,offset) Shr mode) And 1
  ElseIf layer\depth2=2 Then
    offset=((y*layer\sizex+x)/4)
    mode  =((y*layer\sizex+x) Mod 4)*2
    value =(PeekByte(layer\bank2,offset) Shr mode) And 3
  ElseIf layer\depth2=4 Then
    offset=((y*layer\sizex+x)/2)
    mode  =((y*layer\sizex+x) Mod 2)*4
    value =(PeekByte(layer\bank2,offset) Shr mode) And 15
  ElseIf layer\depth2=8 Then
    offset=y*layer\sizex+x
    value =PeekByte(layer\bank2,offset)
  EndIf

  Return value
End Function





;---------------------------------------------------------------------
;layer:  layer handle
;x:      tile pos x
;y:      tile pos y
;RETURN: tile value
;---------------------------------------------------------------------
Function layer_getvalue(layer.layer,x,y)
  Local anims
  Local count
  Local mode
  Local offset
  Local value

  If layer=Null Then Return
  If layer\bank1=0 Then Return
  If x<0 Or y<0 Or x>layer\sizex-1 Or y>layer\sizey-1 Then Return

  If layer\tile<>Null Then
    anims=layer\tile\anims
    count=layer\tile\count
  EndIf

  If layer\depth1=4 Then
    offset=((y*layer\sizex+x)/2)
    mode  =((y*layer\sizex+x) And 1)*4
    value =(PeekByte(layer\bank1,offset) Shr mode) And 15
  ElseIf layer\depth1=8 Then
    offset=y*layer\sizex+x
    value=PeekByte(layer\bank1,offset)
  ElseIf layer\depth1=12 Then
    offset=((y*layer\sizex+x)*3)/2
    mode  =((y*layer\sizex+x) And 1)*4
    value =(PeekShort(layer\bank1,offset) Shr mode) And 4095
  ElseIf layer\depth1=16 Then
    offset=(y*layer\sizex+x)*2
    value=PeekShort(layer\bank1,offset)
  EndIf

  If value=>1 And value<=anims Then ;anim
    Return value
  ElseIf value=>anims+1 And value<=count+anims Then ;frame
    Return value-anims
  EndIf
End Function





;---------------------------------------------------------------------
;layer:  layer handle
;x:      tile pos x
;y:      tile pos y
;RETURN: raw hardcoded value
;---------------------------------------------------------------------
Function layer_getvalue2(layer.layer,x,y)
  Local mode
  Local offset
  Local value

  If layer=Null Then Return
  If layer\bank1=0 Then Return
  If x<0 Or y<0 Or x>layer\sizex-1 Or y>layer\sizey-1 Then Return

  If layer\depth1=4 Then
    offset=((y*layer\sizex+x)/2)
    mode  =((y*layer\sizex+x) And 1)*4
    value =(PeekByte(layer\bank1,offset) Shr mode) And 15
  ElseIf layer\depth1=8 Then
    offset=y*layer\sizex+x
    value=PeekByte(layer\bank1,offset)
  ElseIf layer\depth1=12 Then
    offset=((y*layer\sizex+x)*3)/2
    mode  =((y*layer\sizex+x) And 1)*4
    value =(PeekShort(layer\bank1,offset) Shr mode) And 4095
  ElseIf layer\depth1=16 Then
    offset=(y*layer\sizex+x)*2
    value=PeekShort(layer\bank1,offset)
  EndIf

  Return value
End Function





;---------------------------------------------------------------------
Function layer_reset()
  Local bank
  Local i
  Local size

  Local layer.layer

  For layer=Each layer
    If layer\code<>layer_clone Then
      If layer\bank1<>0 Then FreeBank layer\bank1
      If layer\bank2<>0 Then FreeBank layer\bank2
    EndIf

    If layer\bank3<>0 Then FreeBank layer\bank3

    If layer\bank4<>0 Then
      size=BankSize(layer\bank4)
      For i=0 To size/4-1
        bank=PeekInt(layer\bank4,i*4)
        FreeBank bank
      Next
      FreeBank layer\bank4
    EndIf

    Delete layer
  Next
End Function





;---------------------------------------------------------------------
;layer: layer handle
;x:     tile pos x
;y:     tile pos y
;value: tile value
;---------------------------------------------------------------------
Function layer_setdata(layer.layer,x,y,value)
  Local mode
  Local offset
  Local value1
  Local value2

  If layer=Null Then Return
  If layer\bank2=0 Then Return
  If x<0 Or y<0 Or x>layer\sizex-1 Or y>layer\sizey-1 Then Return
  If value<0 Then value=0

  If layer\depth2=1 Then
    If value>1 Then value=0
    offset=((y*layer\sizex+x)/8)
    mode  =((y*layer\sizex+x) Mod 8)
    value1=PeekByte(layer\bank2,offset)
    value2=(value1 Shr mode) And 1
    PokeByte layer\bank2,offset,value1 Xor (value2 Shl mode) Xor (value Shl mode)
  ElseIf layer\depth2=2 Then
    If value>3 Then value=0
    offset=((y*layer\sizex+x)/4)
    mode  =((y*layer\sizex+x) Mod 4)*2
    value1=PeekByte(layer\bank2,offset)
    value2=(value1 Shr mode) And 3
    PokeByte layer\bank2,offset,value1 Xor (value2 Shl mode) Xor (value Shl mode)
  ElseIf layer\depth2=4 Then
    If value>15 Then value=0
    offset=((y*layer\sizex+x)/2)
    mode  =((y*layer\sizex+x) Mod 2)*4
    value2=PeekByte(layer\bank2,offset) And ($F0 Shr mode)
    PokeByte layer\bank2,offset,value2 Or (value Shl mode)
  ElseIf layer\depth2=8 Then
    If value>255 Then value=0
    offset=y*layer\sizex+x
    PokeByte layer\bank2,offset,value
  EndIf
End Function





;---------------------------------------------------------------------
;layer: layer handle
;x:     tile pos x
;y:     tile pos y
;value: tile value
;code:  0=void, 1=frame, 2=anim
;---------------------------------------------------------------------
Function layer_setvalue(layer.layer,x,y,value,code)
  Local anims
  Local count
  Local mode
  Local offset
  Local value2

  If layer=Null Then Return
  If layer\bank1=0 Then Return
  If x<0 Or y<0 Or x>layer\sizex-1 Or y>layer\sizey-1 Then Return

  If layer\tile<>Null Then
    anims=layer\tile\anims
    count=layer\tile\count
  EndIf

  If code=1 Then
    If value<1 Or value>count Then value=0
    If value>0 Then value=value+anims
  ElseIf code=2 Then
    If value<1 Or value>anims Then value=0
  Else
    value=0
  EndIf

  If layer\depth1=4 Then
    If value>15 Then value=0
    offset=((y*layer\sizex+x)/2)
    mode  =((y*layer\sizex+x) And 1)*4
    value2=PeekByte(layer\bank1,offset) And ($F0 Shr mode)
    PokeByte layer\bank1,offset,value2 Or (value Shl mode)
  ElseIf layer\depth1=8 Then
    If value>255 Then value=0
    offset=y*layer\sizex+x
    PokeByte layer\bank1,offset,value
  ElseIf layer\depth1=12 Then
    If value>4095 Then value=0
    offset=((y*layer\sizex+x)*3)/2
    mode  =((y*layer\sizex+x) And 1)
    value2=PeekShort(layer\bank1,offset) And ($F000 Shr mode*12)
    PokeShort layer\bank1,offset,value2 Or (value Shl mode*4)
  ElseIf layer\depth1=16 Then
    If value>65535 Then value=0
    offset=(y*layer\sizex+x)*2
    PokeShort layer\bank1,offset,value
  EndIf
End Function





;---------------------------------------------------------------------
;layer: layer handle
;x:     tile pos x
;y:     tile pos y
;value: raw hardcoded value
;---------------------------------------------------------------------
Function layer_setvalue2(layer.layer,x,y,value)
  Local anims
  Local count
  Local mode
  Local offset
  Local value2

  If layer=Null Then Return
  If layer\bank1=0 Then Return
  If x<0 Or y<0 Or x>layer\sizex-1 Or y>layer\sizey-1 Then Return
  If value<0 Then value=0

  If layer\depth1=4 Then
    If value>15 Then value=0
    offset=((y*layer\sizex+x)/2)
    mode  =((y*layer\sizex+x) And 1)*4
    value2=PeekByte(layer\bank1,offset) And ($F0 Shr mode)
    PokeByte layer\bank1,offset,value2 Or (value Shl mode)
  ElseIf layer\depth1=8 Then
    If value>255 Then value=0
    offset=y*layer\sizex+x
    PokeByte layer\bank1,offset,value
  ElseIf layer\depth1=12 Then
    If value>4095 Then value=0
    offset=((y*layer\sizex+x)*3)/2
    mode  =((y*layer\sizex+x) And 1)
    value2=PeekShort(layer\bank1,offset) And ($F000 Shr mode*12)
    PokeShort layer\bank1,offset,value2 Or (value Shl mode*4)
  ElseIf layer\depth1=16 Then
    If value>65535 Then value=0
    offset=(y*layer\sizex+x)*2
    PokeShort layer\bank1,offset,value
  EndIf
End Function