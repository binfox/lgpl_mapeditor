;---------------------------------------------------------------------
;layer:  layer handle (map only)
;image:  image handle
;RETURN: 0=no collision
;        1=collision
;---------------------------------------------------------------------
Function layer_image_collision(layer.layer, image.layer)
  Local bank
  Local frame
  Local px
  Local py
  Local start

  If layer=Null Then Return
  If layer\tile=Null Then Return
  If layer\tile\image=0 Then Return
  If layer\code<>layer_map Then Return

  If image=Null Then Return
  If image\tile=Null Then Return
  If image\tile\image=0 Then Return
  If image\code<>layer_image Then Return

  px=map_getscreen(image\posx,image\parax,map_scrollx)
  py=map_getscreen(image\posy,image\paray,map_scrolly)

  If image\frame=>1 And image\frame<=image\tile\anims Then
    If image\mode=0 Then
      bank=PeekInt(image\tile\banka,image\frame*4-4)
      start=PeekShort(bank,10)
      frame=PeekShort(bank,start*4+8)
    Else
      bank=PeekInt(image\tile\banka,image\frame*4-4)
      frame=PeekShort(bank,image\tmp*4+8)
    EndIf
  Else
    frame=image\frame-image\tile\anims
  EndIf

  If frame=>1 And frame<=image\tile\count Then
    Return layer_map_collision(layer,px,py,image\tile\image,frame-1)
  EndIf
End Function





;---------------------------------------------------------------------
;layer: layer handle
;---------------------------------------------------------------------
Function layer_image_draw(layer.layer)
  Local bank
  Local frame
  Local px
  Local py
  Local start

  If layer=Null Then Return
  If layer\tile=Null Then Return
  If layer\tile\image=0 Then Return

  px=map_getscreen(layer\posx,layer\parax,map_scrollx)
  py=map_getscreen(layer\posy,layer\paray,map_scrolly)

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