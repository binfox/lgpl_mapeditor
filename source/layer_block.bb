;---------------------------------------------------------------------
;tile: tileset handle
;---------------------------------------------------------------------
Function layer_block_add(tile.tile)
  layer        =New layer
  layer\ascii  =0
  layer\bank1  =0
  layer\bank2  =0
  layer\bank3  =0
  layer\code   =layer_block
  layer\depth1 =0
  layer\depth2 =0
  layer\frame  =0
  layer\layer  =Null
  layer\mask   =1
  layer\mode   =0
  layer\name$  =language$(48)
  layer\parax  =0
  layer\paray  =0
  layer\posx   =0
  layer\posy   =0
  layer\sizex  =0
  layer\sizey  =0
  layer\start  =1
  layer\tile   =tile
  layer\visible=1

  layer_count=layer_count+1
  layer_nr=layer_count
  layer_list[layer_nr]=layer
End Function





;---------------------------------------------------------------------
;layer:   layer handle
;sliderx: sliderx value
;slidery: slidery value
;---------------------------------------------------------------------
Function layer_block_draw(layer.layer,sliderx,slidery)
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
        result=layer_map_pos(layer\layer,sliderx,slidery,layer\posx,layer\posy)
      Case layer_iso1
        result=layer_iso1_pos(layer\layer,sliderx,slidery,layer\posx,layer\posy)
      Case layer_iso2
        result=layer_iso2_pos(layer\layer,sliderx,slidery,layer\posx,layer\posy)
      Case layer_hex1
        result=layer_hex1_pos(layer\layer,sliderx,slidery,layer\posx,layer\posy)
      Case layer_hex2
        result=layer_hex2_pos(layer\layer,sliderx,slidery,layer\posx,layer\posy)
      Case layer_clone
        result=layer_clone_pos(layer\layer,sliderx,slidery,layer\posx,layer\posy)
    End Select
  EndIf

  If result=1 Then
    px=layer_x+layer\parax
    py=layer_y+layer\paray
  Else
    px=layer\parax-sliderx
    py=layer\paray-slidery
  EndIf

  If layer\frame=>1 And layer\frame<=layer\tile\anims Then
    bank=PeekInt(layer\tile\banka,layer\frame*4-4)
    start=PeekShort(bank,2)
    frame=PeekShort(bank,start*4+8)
  Else
    frame=layer\frame-layer\tile\anims
  EndIf

  If frame=>1 And frame<=layer\tile\count Then
    If layer\mask=0 Then DrawBlock layer\tile\image,px,py,frame-1
    If layer\mask=1 Then DrawImage layer\tile\image,px,py,frame-1
  EndIf
End Function





;---------------------------------------------------------------------
;layer:   layer handle
;sliderx: sliderx value
;slidery: slidery value
;---------------------------------------------------------------------
Function layer_block_grid(layer.layer,sliderx,slidery)
  Local b
  Local g
  Local px
  Local py
  Local r
  Local result

  If layer=Null Then Return

  If layer\tile<>Null Then
    sizex=layer\tile\sizex
    sizey=layer\tile\sizey
  Else
    sizex=tile_default
    sizey=tile_default
  EndIf

  If sizex<tile_minsize Then sizex=tile_minsize
  If sizey<tile_minsize Then sizey=tile_minsize

  If layer\layer<>Null Then
    Select layer\layer\code
      Case layer_map
        result=layer_map_pos(layer\layer,sliderx,slidery,layer\posx,layer\posy)
      Case layer_iso1
        result=layer_iso1_pos(layer\layer,sliderx,slidery,layer\posx,layer\posy)
      Case layer_iso2
        result=layer_iso2_pos(layer\layer,sliderx,slidery,layer\posx,layer\posy)
      Case layer_hex1
        result=layer_hex1_pos(layer\layer,sliderx,slidery,layer\posx,layer\posy)
      Case layer_hex2
        result=layer_hex2_pos(layer\layer,sliderx,slidery,layer\posx,layer\posy)
      Case layer_clone
        result=layer_clone_pos(layer\layer,sliderx,slidery,layer\posx,layer\posy)
    End Select
  EndIf

  If result=1 Then
    px=layer_x+layer\parax
    py=layer_y+layer\paray
  Else
    px=layer\parax-sliderx
    py=layer\paray-slidery
  EndIf

  r=(setup_color1 And $FF0000)/$10000
  g=(setup_color1 And $FF00)/$100
  b=(setup_color1 And $FF)
  Color r,g,b
  Rect px,py,sizex,sizey,0
  Rect px-3,py-3,7,7
  Rect px-3,py+sizey-4,7,7
  Rect px+sizex-4,py-3,7,7
  Rect px+sizex-4,py+sizey-4,7,7
End Function





;---------------------------------------------------------------------
;layer:   layer handle
;sliderx: sliderx value
;slidery: slidery value
;x:       screen x
;y:       screen y
;RETURN:  0=outside, 1=inside
;---------------------------------------------------------------------
Function layer_block_over(layer.layer,sliderx,slidery,x,y)
  Local px
  Local py
  Local result
  Local sizex
  Local sizey

  If layer=Null Then Return

  If layer\tile<>Null Then
    sizex=layer\tile\sizex
    sizey=layer\tile\sizey
  Else
    sizex=tile_default
    sizey=tile_default
  EndIf

  If sizex<tile_minsize Then sizex=tile_minsize
  If sizey<tile_minsize Then sizey=tile_minsize

  If layer\layer<>Null Then
    Select layer\layer\code
      Case layer_map
        result=layer_map_pos(layer\layer,sliderx,slidery,layer\posx,layer\posy)
      Case layer_iso1
        result=layer_iso1_pos(layer\layer,sliderx,slidery,layer\posx,layer\posy)
      Case layer_iso2
        result=layer_iso2_pos(layer\layer,sliderx,slidery,layer\posx,layer\posy)
      Case layer_hex1
        result=layer_hex1_pos(layer\layer,sliderx,slidery,layer\posx,layer\posy)
      Case layer_hex2
        result=layer_hex2_pos(layer\layer,sliderx,slidery,layer\posx,layer\posy)
      Case layer_clone
        result=layer_clone_pos(layer\layer,sliderx,slidery,layer\posx,layer\posy)
    End Select
  EndIf

  If result=1 Then
    px=layer_x+layer\parax
    py=layer_y+layer\paray
  Else
    px=layer\parax-sliderx
    py=layer\paray-slidery
  EndIf

  If x=>px And y=>py And x<=px+sizex-1 And y<=py+sizey-1 Then Return 1
End Function





;---------------------------------------------------------------------
;layer: layer handle
;---------------------------------------------------------------------
Function layer_block_pos(layer.layer)
  Local px
  Local py
  Local result

  If layer=Null Then Return

  If layer\layer<>Null Then
    Select layer\layer\code
      Case layer_map
        result=layer_map_pos(layer\layer,sliderx,slidery,layer\posx,layer\posy)
      Case layer_iso1
        result=layer_iso1_pos(layer\layer,sliderx,slidery,layer\posx,layer\posy)
      Case layer_iso2
        result=layer_iso2_pos(layer\layer,sliderx,slidery,layer\posx,layer\posy)
      Case layer_hex1
        result=layer_hex1_pos(layer\layer,sliderx,slidery,layer\posx,layer\posy)
      Case layer_hex2
        result=layer_hex2_pos(layer\layer,sliderx,slidery,layer\posx,layer\posy)
      Case layer_clone
        result=layer_clone_pos(layer\layer,sliderx,slidery,layer\posx,layer\posy)
    End Select
  EndIf

  If result=1 Then
    px=layer_x+layer\parax
    py=layer_y+layer\paray
    layer_x=editor_getcoord(px,layer\layer\parax,sliderx)
    layer_y=editor_getcoord(py,layer\layer\paray,slidery)
  Else
    layer_x=layer\parax
    layer_y=layer\paray
  EndIf
End Function





;---------------------------------------------------------------------
;layer: layer handle
;---------------------------------------------------------------------
Function layer_block_size(layer.layer)
  Local sizex
  Local sizey

  If layer=Null Then Return

  If layer\tile<>Null Then
    sizex=layer\tile\sizex
    sizey=layer\tile\sizey
  Else
    sizex=tile_default
    sizey=tile_default
  EndIf

  If sizex<tile_minsize Then sizex=tile_minsize
  If sizey<tile_minsize Then sizey=tile_minsize

  layer_width=sizex
  layer_height=sizey
End Function