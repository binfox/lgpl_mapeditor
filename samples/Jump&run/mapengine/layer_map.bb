;---------------------------------------------------------------------
;layer:       layer handle
;spritex:     sprite x (screen)
;spritey:     sprite y (screen)
;sprite:      sprite handle
;spriteframe: sprite frame
;RETURN:      0=no collision
;             1=collision
;---------------------------------------------------------------------
Function layer_map_collision(layer.layer,spritex,spritey,sprite,spriteframe=0)
  Local anims
  Local bank
  Local count
  Local endx
  Local endy
  Local factor
  Local frame
  Local image
  Local mode
  Local offset
  Local px
  Local py
  Local sizex
  Local sizey
  Local start
  Local startx
  Local starty
  Local value
  Local x
  Local xx
  Local y
  Local yy

  If layer=Null Then Return
  If layer\tile=Null Then Return

  anims =layer\tile\anims
  count =layer\tile\count
  factor=layer\tile\factor
  image =layer\tile\image
  sizex =layer\tile\sizex
  sizey =layer\tile\sizey-factor

  If image=0 Then Return
  If sprite=0 Then Return
  If sizex<tile_minsize Then sizex=tile_minsize
  If sizey<tile_minsize Then sizey=tile_minsize

  px=map_getscreen(layer\posx,layer\parax,map_scrollx)
  py=map_getscreen(layer\posy,layer\paray,map_scrolly)

  layer_map_coord(layer,spritex,spritey)
  startx=layer_x
  starty=layer_y

  layer_map_coord(layer,spritex+ImageWidth(sprite)-1,spritey+ImageHeight(sprite)-1)
  endx=layer_x
  endy=layer_y

  If startx<0 Then startx=0
  If starty<0 Then starty=0
  If endx>layer\sizex-1 Then endx=layer\sizex-1
  If endy>layer\sizey-1 Then endy=layer\sizey-1

  For y=starty To endy
    For x=startx To endx

      If layer\depth1=4 Then
        offset=((y*layer\sizex+x)/2)
        mode  =((y*layer\sizex+x) And 1)*4
        value =(PeekByte(layer\bank1,offset) Shr mode) And 15
      ElseIf layer\depth1=8 Then
        offset=y*layer\sizex+x
        value =PeekByte(layer\bank1,offset)
      ElseIf layer\depth1=12 Then
        offset=((y*layer\sizex+x)*3)/2
        mode  =((y*layer\sizex+x) And 1)*4
        value =(PeekShort(layer\bank1,offset) Shr mode) And 4095
      ElseIf layer\depth1=16 Then
        offset=(y*layer\sizex+x)*2
        value =PeekShort(layer\bank1,offset)
      EndIf

      If value=>1 And value<=anims Then
        bank=PeekInt(layer\tile\banka,value*4-4)
        start=PeekShort(bank,10)
        frame=PeekShort(bank,start*4+8)
      Else
        frame=value-anims
      EndIf

      If frame=>1 And frame<=count Then
        xx=px+x*sizex
        yy=py+y*sizey
        If ImagesCollide(image,xx,yy,frame-1, sprite,spritex,spritey,spriteframe)=1 Then Return 1
      EndIf

    Next
  Next
End Function





;---------------------------------------------------------------------
;layer:  layer handle
;x:      screen x
;y:      screen y
;check:  bound check
;
;RETURN:         0=outside, 1=inside (only if check is enabled)
;RETURN LAYER_X: tile pos x
;RETURN LAYER_Y: tile pos y
;---------------------------------------------------------------------
Function layer_map_coord(layer.layer,x,y,check=0)
  Local factor
  Local outside
  Local px
  Local py
  Local sizex
  Local sizey

  If layer=Null Then Return

  If layer\tile<>Null Then
    factor=layer\tile\factor
    sizex =layer\tile\sizex
    sizey =layer\tile\sizey-factor
  Else
    sizex=tile_default
    sizey=tile_default
  EndIf

  If sizex<tile_minsize Then sizex=tile_minsize
  If sizey<tile_minsize Then sizey=tile_minsize

  px=map_getscreen(layer\posx,layer\parax,map_scrollx)
  py=map_getscreen(layer\posy,layer\paray,map_scrolly)

  layer_x=(x-px)/sizex
  layer_y=(y-py)/sizey

  If (x-px)<0 Then layer_x=layer_x-1
  If (y-py)<0 Then layer_y=layer_y-1

  If check=1 Then
    If layer_x<0 Then layer_x=0 : outside=1
    If layer_y<0 Then layer_y=0 : outside=1
    If layer_x>layer\sizex-1 Then layer_x=layer\sizex-1 : outside=1
    If layer_y>layer\sizey-1 Then layer_y=layer\sizey-1 : outside=1
  EndIf

  Return 1-outside
End Function





;---------------------------------------------------------------------
;layer: layer handle
;---------------------------------------------------------------------
Function layer_map_draw(layer.layer)
  Local anims
  Local bank
  Local count
  Local endx
  Local endy
  Local factor
  Local frame
  Local image
  Local mode
  Local offset
  Local px
  Local py
  Local sizex
  Local sizey
  Local start
  Local startx
  Local starty
  Local value
  Local x
  Local xx
  Local y
  Local yy

  If layer=Null Then Return
  If layer\tile=Null Then Return

  anims =layer\tile\anims
  count =layer\tile\count
  factor=layer\tile\factor
  image =layer\tile\image
  sizex =layer\tile\sizex
  sizey =layer\tile\sizey-factor

  If image=0 Then Return
  If sizex<tile_minsize Then sizex=tile_minsize
  If sizey<tile_minsize Then sizey=tile_minsize

  px=map_getscreen(layer\posx,layer\parax,map_scrollx)
  py=map_getscreen(layer\posy,layer\paray,map_scrolly)

  startx=(0-px)/sizex
  starty=(0-py-factor)/sizey
  endx=(map_width-px)/sizex
  endy=(map_height-py)/sizey

  If startx<0 Then startx=0
  If starty<0 Then starty=0
  If endx>layer\sizex-1 Then endx=layer\sizex-1
  If endy>layer\sizey-1 Then endy=layer\sizey-1

  For y=starty To endy
    For x=startx To endx

      If layer\depth1=4 Then
        offset=((y*layer\sizex+x)/2)
        mode  =((y*layer\sizex+x) And 1)*4
        value =(PeekByte(layer\bank1,offset) Shr mode) And 15
      ElseIf layer\depth1=8 Then
        offset=y*layer\sizex+x
        value =PeekByte(layer\bank1,offset)
      ElseIf layer\depth1=12 Then
        offset=((y*layer\sizex+x)*3)/2
        mode  =((y*layer\sizex+x) And 1)*4
        value =(PeekShort(layer\bank1,offset) Shr mode) And 4095
      ElseIf layer\depth1=16 Then
        offset=(y*layer\sizex+x)*2
        value =PeekShort(layer\bank1,offset)
      EndIf

      If value=>1 And value<=anims Then
        bank=PeekInt(layer\tile\banka,value*4-4)
        start=PeekShort(bank,10)
        frame=PeekShort(bank,start*4+8)
      Else
        frame=value-anims
      EndIf

      If frame=>1 And frame<=count Then
        xx=px+x*sizex
        yy=py+y*sizey
        If layer\mask=0 Then DrawBlock image,xx,yy,frame-1
        If layer\mask=1 Then DrawImage image,xx,yy,frame-1
      EndIf

    Next
  Next
End Function





;---------------------------------------------------------------------
;layer:   layer handle
;---------------------------------------------------------------------
Function layer_map_draw_repeat(layer.layer)
  Local anims
  Local bank
  Local count
  Local factor
  Local frame
  Local image
  Local mode
  Local offset
  Local px
  Local py
  Local sizex
  Local sizey
  Local start
  Local startx
  Local starty
  Local stepx
  Local stepy
  Local value
  Local x
  Local xx
  Local y
  Local yy

  If layer=Null Then Return
  If layer\tile=Null Then Return

  anims =layer\tile\anims
  count =layer\tile\count
  factor=layer\tile\factor
  image =layer\tile\image
  sizex =layer\tile\sizex
  sizey =layer\tile\sizey-factor

  If image=0 Then Return
  If sizex<tile_minsize Then sizex=tile_minsize
  If sizey<tile_minsize Then sizey=tile_minsize

  px=map_getscreen(layer\posx,layer\parax,map_scrollx)
  py=map_getscreen(layer\posy,layer\paray,map_scrolly)

  stepx=px/sizex+(px>0 And px<>sizex)
  stepy=py/sizey+(py>0 And py<>sizey)

  px=px-stepx*sizex
  py=py-stepy*sizey

  startx=(0-stepx) Mod layer\sizex
  starty=(0-stepy) Mod layer\sizey
  If startx<0 Then startx=layer\sizex+startx
  If starty<0 Then starty=layer\sizey+starty

  y=starty
  For yy=py To map_height
    x=startx
    For xx=px To map_width

      If layer\depth1=4 Then
        offset=((y*layer\sizex+x)/2)
        mode  =((y*layer\sizex+x) And 1)*4
        value =(PeekByte(layer\bank1,offset) Shr mode) And 15
      ElseIf layer\depth1=8 Then
        offset=y*layer\sizex+x
        value =PeekByte(layer\bank1,offset)
      ElseIf layer\depth1=12 Then
        offset=((y*layer\sizex+x)*3)/2
        mode  =((y*layer\sizex+x) And 1)*4
        value =(PeekShort(layer\bank1,offset) Shr mode) And 4095
      ElseIf layer\depth1=16 Then
        offset=(y*layer\sizex+x)*2
        value =PeekShort(layer\bank1,offset)
      EndIf

      If value=>1 And value<=anims Then
        bank=PeekInt(layer\tile\banka,value*4-4)
        start=PeekShort(bank,10)
        frame=PeekShort(bank,start*4+8)
      Else
        frame=value-anims
      EndIf

      If frame=>1 And frame<=count Then
        If layer\mask=0 Then DrawBlock image,xx,yy,frame-1
        If layer\mask=1 Then DrawImage image,xx,yy,frame-1
      EndIf

      x=(x+1) Mod layer\sizex
      xx=xx+sizex-1
    Next
    y=(y+1) Mod layer\sizey
    yy=yy+sizey-1
  Next
End Function





;---------------------------------------------------------------------
;layer:  layer handle
;x:      tile pos x
;y:      tile pos y
;
;RETURN:         0=error, 1=ok
;RETURN LAYER_X: screen pos x
;RETURN LAYER_Y: screen pos y
;---------------------------------------------------------------------
Function layer_map_pos(layer.layer,x,y)
  Local factor
  Local px
  Local py
  Local sizex
  Local sizey

  If layer=Null Then Return

  If layer\tile<>Null Then
    factor=layer\tile\factor
    sizex =layer\tile\sizex
    sizey =layer\tile\sizey-factor
  Else
    sizex=tile_default
    sizey=tile_default
  EndIf

  If sizex<tile_minsize Then sizex=tile_minsize
  If sizey<tile_minsize Then sizey=tile_minsize

  px=map_getscreen(layer\posx,layer\parax,map_scrollx)
  py=map_getscreen(layer\posy,layer\paray,map_scrolly)

  layer_x=px+x*sizex
  layer_y=py+y*sizey
  Return 1
End Function





;---------------------------------------------------------------------
;layer: layer handle
;
;RETURN LAYER_WIDTH:  layer width in pixel
;RETURN LAYER_HEIGHT: layer height in pixel
;---------------------------------------------------------------------
Function layer_map_size(layer.layer)
  Local factor
  Local sizex
  Local sizey

  If layer=Null Then Return

  If layer\tile<>Null Then
    factor=layer\tile\factor
    sizex =layer\tile\sizex
    sizey =layer\tile\sizey-factor
  Else
    sizex=tile_default
    sizey=tile_default
  EndIf

  If sizex<tile_minsize Then sizex=tile_minsize
  If sizey<tile_minsize Then sizey=tile_minsize

  layer_width =layer\sizex*sizex
  layer_height=layer\sizey*sizey+factor
End Function