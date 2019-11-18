;---------------------------------------------------------------------
;layer: layer handle
;---------------------------------------------------------------------
Function layer_block_draw(layer.layer)
  Local bank
  Local frame
  Local px
  Local py
  Local result
  Local start

  If layer=Null Then Return
  If layer\tile=Null Then Return
  If layer\tile\image=0 Then Return

  If layer\layer<>Null Then
    Select layer\layer\code
      Case layer_map
        result=layer_map_pos(layer\layer,layer\posx,layer\posy)
      Case layer_iso1
        result=layer_iso1_pos(layer\layer,layer\posx,layer\posy)
      Case layer_iso2
        result=layer_iso2_pos(layer\layer,layer\posx,layer\posy)
      Case layer_hex1
        result=layer_hex1_pos(layer\layer,layer\posx,layer\posy)
      Case layer_hex2
        result=layer_hex2_pos(layer\layer,layer\posx,layer\posy)
      Case layer_clone
        result=layer_clone_pos(layer\layer,layer\posx,layer\posy)
    End Select
  EndIf

  If result=1 Then
    px=layer_x+layer\parax ;<<<para=justage
    py=layer_y+layer\paray ;<<<para=justage
  Else
    px=layer\parax-map_scrollx ;<<<para=justage
    py=layer\paray-map_scrolly ;<<<para=justage
  EndIf

  If layer\frame=>1 And layer\frame<=layer\tile\anims Then
    If layer\mode=0 Then
      bank=PeekInt(layer\tile\banka,layer\frame*4-4)
      start=PeekShort(bank,10)
      frame=PeekShort(bank,start*4+8)
    Else
      bank=PeekInt(layer\tile\banka,layer\frame*4-4)
      frame=PeekShort(bank,layer\tmp*4+8)
    EndIf
  Else
    frame=layer\frame-layer\tile\anims
  EndIf

  If frame=>1 And frame<=layer\tile\count Then
    If layer\mask=0 Then DrawBlock layer\tile\image,px,py,frame-1
    If layer\mask=1 Then DrawImage layer\tile\image,px,py,frame-1
  EndIf
End Function





;---------------------------------------------------------------------
;layer: layer handle
;
;RETURN LAYER_X: global pos x
;RETURN LAYER_Y: global pos y
;---------------------------------------------------------------------
Function layer_block_pos(layer.layer)
  Local px
  Local py
  Local result

  If layer=Null Then Return

  If layer\layer<>Null Then
    Select layer\layer\code
      Case layer_map
        result=layer_map_pos(layer\layer,layer\posx,layer\posy)
      Case layer_iso1
        result=layer_iso1_pos(layer\layer,layer\posx,layer\posy)
      Case layer_iso2
        result=layer_iso2_pos(layer\layer,layer\posx,layer\posy)
      Case layer_hex1
        result=layer_hex1_pos(layer\layer,layer\posx,layer\posy)
      Case layer_hex2
        result=layer_hex2_pos(layer\layer,layer\posx,layer\posy)
      Case layer_clone
        result=layer_clone_pos(layer\layer,layer\posx,layer\posy)
    End Select
  EndIf

  If result=1 Then
    px=layer_x+layer\parax ;<<<para=justage
    py=layer_y+layer\paray ;<<<para=justage
    layer_x=map_getcoord(px,layer\layer\parax,map_scrollx)
    layer_y=map_getcoord(py,layer\layer\paray,map_scrolly)
  Else
    layer_x=layer\parax ;<<<para=justage
    layer_y=layer\paray ;<<<para=justage
  EndIf
End Function