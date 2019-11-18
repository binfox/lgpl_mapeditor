;---------------------------------------------------------------------
;layer: layer handle
;x:     screen x
;y:     screen y
;check: bound check
;
;RETURN:         0=outside, 1=inside (only if check is enabled)
;RETURN LAYER_X: tile pos x
;RETURN LAYER_Y: tile pos y
;---------------------------------------------------------------------
Function layer_hex2_coord(layer.layer,x,y,check=0)
  Local arrayx[8]
  Local arrayy[8]
  Local corr
  Local cx#
  Local cy#
  Local dist#
  Local factor
  Local height
  Local i
  Local min#
  Local outside
  Local px
  Local py
  Local sizex
  Local sizey
  Local stepx
  Local stepy
  Local stretch#
  Local xx
  Local yy

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

  stepx=sizex/2
  stepy=sizey-sizex/4
  height=Int((sizex/2)/Sin#(60))*2
  stretch#=Float#(height)/Float#(sizey)
  min#=1000000

  yy=(y-py)/stepy
  If ((yy+layer\start) And 1)=0 Then
    xx=(x-px)/sizex
    corr=0
  Else
    xx=(x-px-stepx)/sizex
    corr=1
  EndIf

  arrayx[0]=xx+corr-1 : arrayy[0]=yy-1
  arrayx[1]=xx+corr   : arrayy[1]=yy-1
  arrayx[2]=xx-1      : arrayy[2]=yy
  arrayx[3]=xx        : arrayy[3]=yy
  arrayx[4]=xx+1      : arrayy[4]=yy
  arrayx[5]=xx+corr-1 : arrayy[5]=yy+1
  arrayx[6]=xx+corr   : arrayy[6]=yy+1

  For i=0 To 6
    corr=(arrayy[i]+layer\start) And 1
    cx#=(x-px-arrayx[i]*sizex-corr*stepx)-Float#(sizex-1)/2
    cy#=(y-py-arrayy[i]*stepy)-Float#(sizey-1)/2
    dist#=cx#*cx#+cy#*cy#*stretch#*stretch#
    If min#>dist# Then
      min#=dist#
      layer_x=arrayx[i]
      layer_y=arrayy[i]
    EndIf
  Next

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
Function layer_hex2_draw(layer.layer)
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

  stepx=sizex/2
  stepy=sizey-sizex/4

  startx=(-stepx-px)/sizex
  starty=(-sizey-py+stepy-factor)/stepy
  endx=(map_width-px)/sizex
  endy=(map_height-py)/stepy

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
        xx=px+x*sizex+((y+layer\start) And 1)*stepx
        yy=py+y*stepy
        DrawImage image,xx,yy,frame-1
      EndIf

    Next
  Next
End Function





;---------------------------------------------------------------------
;layer: layer handle
;x:     position x
;y:     position y
;
;RETURN:         0=error, 1=ok
;RETURN LAYER_X: screen pos x
;RETURN LAYER_Y: screen pos y
;---------------------------------------------------------------------
Function layer_hex2_pos(layer.layer,x,y)
  Local factor
  Local px
  Local py
  Local sizex
  Local sizey
  Local stepx
  Local stepy

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

  stepx=sizex/2
  stepy=sizey-sizex/4

  layer_x=px+x*sizex+((y+layer\start) And 1)*stepx
  layer_y=py+y*stepy
  Return 1
End Function





;---------------------------------------------------------------------
;layer: layer handle
;
;RETURN LAYER_WIDTH:  layer width in pixel
;RETURN LAYER_HEIGHT: layer height in pixel
;---------------------------------------------------------------------
Function layer_hex2_size(layer.layer)
  Local factor
  Local sizex
  Local sizey
  Local stepx
  Local stepy

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

  stepx=sizex/2
  stepy=sizey-sizex/4

  layer_width =layer\sizex*sizex+(layer\sizey>1)*stepx
  layer_height=layer\sizey*stepy-stepy+sizey+factor
End Function