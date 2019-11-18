;---------------------------------------------------------------------
;layer:   layer handle
;x:       screen x
;y:       screen y
;check:   bound check
;RETURN:  0=outside, 1=inside (only if check is enabled)
;---------------------------------------------------------------------
Function layer_clone_coord(layer.layer,x,y,check=0)
  If layer\layer=Null Then
    Return layer_map_coord(layer,x,y,check)
  Else
    Select layer\layer\code
      Case layer_map
        Return layer_map_coord(layer,x,y,check)
      Case layer_iso1
        Return layer_iso1_coord(layer,x,y,check)
      Case layer_iso2
        Return layer_iso2_coord(layer,x,y,check)
      Case layer_hex1
        Return layer_hex1_coord(layer,x,y,check)
      Case layer_hex2
        Return layer_hex2_coord(layer,x,y,check)
    End Select
  EndIf
End Function





;---------------------------------------------------------------------
;layer:   layer handle
;---------------------------------------------------------------------
Function layer_clone_draw(layer.layer)
  layer_clone_update(layer)

  If layer\layer=Null Then
    layer_map_draw(layer)
  Else
    Select layer\layer\code
      Case layer_map
        layer_map_draw(layer)
      Case layer_iso1
        layer_iso1_draw(layer)
      Case layer_iso2
        layer_iso2_draw(layer)
      Case layer_hex1
        layer_hex1_draw(layer)
      Case layer_hex2
        layer_hex2_draw(layer)
    End Select
  EndIf
End Function





;---------------------------------------------------------------------
;layer:   layer handle
;x:       position x
;y:       position y
;RETURN:  0=error, 1=ok
;---------------------------------------------------------------------
Function layer_clone_pos(layer.layer,x,y)
  If layer\layer=Null Then
    Return layer_map_pos(layer,x,y)
  Else
    Select layer\layer\code
      Case layer_map
        Return layer_map_pos(layer,x,y)
      Case layer_iso1
        Return layer_iso1_pos(layer,x,y)
      Case layer_iso2
        Return layer_iso2_pos(layer,x,y)
      Case layer_hex1
        Return layer_hex1_pos(layer,x,y)
      Case layer_hex2
        Return layer_hex2_pos(layer,x,y)
    End Select
  EndIf
End Function





;---------------------------------------------------------------------
;layer: layer handle
;---------------------------------------------------------------------
Function layer_clone_size(layer.layer)
  If layer\layer=Null Then
    layer_map_size(layer)
  Else
    Select layer\layer\code
      Case layer_map
        layer_map_size(layer)
      Case layer_iso1
        layer_iso1_size(layer)
      Case layer_iso2
        layer_iso2_size(layer)
      Case layer_hex1
        layer_hex1_size(layer)
      Case layer_hex2
        layer_hex2_size(layer)
    End Select
  EndIf
End Function





;---------------------------------------------------------------------
;layer: layer handle
;---------------------------------------------------------------------
Function layer_clone_update(layer.layer)
  If layer\layer=Null Then
    layer\bank1 =0
    layer\bank2 =0
    layer\depth1=0
    layer\depth2=0
    layer\mask  =0
    layer\sizex =10
    layer\sizey =10
    layer\start =0
    layer\tile  =Null
  Else
    layer\bank1 =layer\layer\bank1
    layer\bank2 =layer\layer\bank2
    layer\depth1=layer\layer\depth1
    layer\depth2=layer\layer\depth2
    layer\mask  =layer\layer\mask
    layer\sizex =layer\layer\sizex
    layer\sizey =layer\layer\sizey
    layer\start =layer\layer\start
    layer\tile  =layer\layer\tile
  EndIf
End Function