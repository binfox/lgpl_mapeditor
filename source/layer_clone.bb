;---------------------------------------------------------------------
Function layer_clone_add()
  layer        =New layer
  layer\ascii  =0
  layer\bank1  =0
  layer\bank2  =0
  layer\bank3  =0
  layer\code   =layer_clone
  layer\depth1 =0
  layer\depth2 =0
  layer\frame  =0
  layer\layer  =Null
  layer\mask   =0
  layer\mode   =0
  layer\name$  =language$(46)
  layer\parax  =100
  layer\paray  =100
  layer\posx   =0
  layer\posy   =0
  layer\sizex  =0
  layer\sizey  =0
  layer\start  =0
  layer\tile   =Null
  layer\visible=1

  layer_count=layer_count+1
  layer_nr=layer_count  
  layer_list[layer_nr]=layer
End Function





;---------------------------------------------------------------------
;layer:   layer handle
;sliderx: sliderx value
;slidery: slidery value
;x:       screen x
;y:       screen y
;check:   bound check
;RETURN:  0=outside, 1=inside (only if check is enabled)
;---------------------------------------------------------------------
Function layer_clone_coord(layer.layer,sliderx,slidery,x,y,check=0)
  If layer\layer=Null Then
    Return layer_map_coord(layer,sliderx,slidery,x,y,check)
  Else
    Select layer\layer\code
      Case layer_map
        Return layer_map_coord(layer,sliderx,slidery,x,y,check)
      Case layer_iso1
        Return layer_iso1_coord(layer,sliderx,slidery,x,y,check)
      Case layer_iso2
        Return layer_iso2_coord(layer,sliderx,slidery,x,y,check)
      Case layer_hex1
        Return layer_hex1_coord(layer,sliderx,slidery,x,y,check)
      Case layer_hex2
        Return layer_hex2_coord(layer,sliderx,slidery,x,y,check)
    End Select
  EndIf
End Function





;---------------------------------------------------------------------
;layer:   layer handle
;sliderx: sliderx value
;slidery: slidery value
;---------------------------------------------------------------------
Function layer_clone_draw(layer.layer,sliderx,slidery)
  layer_clone_update(layer)

  If layer\layer=Null Then
    layer_map_draw(layer,sliderx,slidery)
  Else
    Select layer\layer\code
      Case layer_map
        layer_map_draw(layer,sliderx,slidery)
      Case layer_iso1
        layer_iso1_draw(layer,sliderx,slidery)
      Case layer_iso2
        layer_iso2_draw(layer,sliderx,slidery)
      Case layer_hex1
        layer_hex1_draw(layer,sliderx,slidery)
      Case layer_hex2
        layer_hex2_draw(layer,sliderx,slidery)
    End Select
  EndIf
End Function





;---------------------------------------------------------------------
;layer:   layer handle
;sliderx: sliderx value
;slidery: slidery value
;---------------------------------------------------------------------
Function layer_clone_grid(layer.layer,sliderx,slidery)
  If layer\layer=Null Then
    layer_map_grid(layer,sliderx,slidery)
  Else
    Select layer\layer\code
      Case layer_map
        layer_map_grid(layer,sliderx,slidery)
      Case layer_iso1
        layer_iso1_grid(layer,sliderx,slidery)
      Case layer_iso2
        layer_iso2_grid(layer,sliderx,slidery)
      Case layer_hex1
        layer_hex1_grid(layer,sliderx,slidery)
      Case layer_hex2
        layer_hex2_grid(layer,sliderx,slidery)
    End Select
  EndIf
End Function





;---------------------------------------------------------------------
;layer:   layer handle
;sliderx: sliderx value
;slidery: slidery value
;x:       position x
;y:       position y
;RETURN:  0=error, 1=ok
;---------------------------------------------------------------------
Function layer_clone_pos(layer.layer,sliderx,slidery,x,y)
  Local result

  If layer\layer=Null Then
    result=layer_map_pos(layer,sliderx,slidery,x,y)
  Else
    Select layer\layer\code
      Case layer_map
        result=layer_map_pos(layer,sliderx,slidery,x,y)
      Case layer_iso1
        result=layer_iso1_pos(layer,sliderx,slidery,x,y)
      Case layer_iso2
        result=layer_iso2_pos(layer,sliderx,slidery,x,y)
      Case layer_hex1
        result=layer_hex1_pos(layer,sliderx,slidery,x,y)
      Case layer_hex2
        result=layer_hex2_pos(layer,sliderx,slidery,x,y)
    End Select
  EndIf

  Return result
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