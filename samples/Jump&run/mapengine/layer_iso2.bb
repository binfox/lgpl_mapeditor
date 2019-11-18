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
Function layer_iso2_coord(layer.layer,x,y,check=0)
  Local arrayx[8]
  Local arrayy[8]
  Local corr
  Local cx#
  Local cy#
  Local dist#
  Local factor
  Local halfx
  Local halfy
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

  If (sizey And 1)=0 Then
    stepx=sizex+2
    stepy=sizey
  Else
    stepx=sizex
    stepy=sizey+1
  EndIf

  halfx=stepx/2
  halfy=stepy/2
  stretch#=Float#(stepy)/Float#(stepx)
  min#=1000000

  yy=(y-py)/halfy
  If ((yy+layer\start) And 1)=0 Then
    xx=(x-px)/stepx
    corr=0
  Else
    xx=(x-px-halfx)/stepx
    corr=1
  EndIf

  arrayx[0]=xx        : arrayy[0]=yy-2
  arrayx[1]=xx+corr-1 : arrayy[1]=yy-1
  arrayx[2]=xx+corr   : arrayy[2]=yy-1
  arrayx[3]=xx-1      : arrayy[3]=yy
  arrayx[4]=xx        : arrayy[4]=yy
  arrayx[5]=xx+1      : arrayy[5]=yy
  arrayx[6]=xx+corr-1 : arrayy[6]=yy+1
  arrayx[7]=xx+corr   : arrayy[7]=yy+1
  arrayx[8]=xx        : arrayy[8]=yy+2

  For i=0 To 8
    corr=(arrayy[i]+layer\start) And 1
    cx#=(x-px-arrayx[i]*stepx-corr*halfx)-Float#(sizex-1)/2
    cy#=(y-py-arrayy[i]*halfy)-Float#(sizey-1)/2
    dist#=cx#*cx#*stretch#*stretch#+cy#*cy#
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
Function layer_iso2_draw(layer.layer)
  Local anims
  Local bank
  Local count
  Local endx
  Local endy
  Local factor
  Local frame
  Local halfx
  Local halfy
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

  If (sizey And 1)=0 Then
    stepx=sizex+2
    stepy=sizey
  Else
    stepx=sizex
    stepy=sizey+1
  EndIf

  halfx=stepx/2
  halfy=stepy/2

  startx=(-halfx-px)/stepx
  starty=(-halfy-py-factor)/halfy
  endx=(map_width-px)/stepx
  endy=(map_height-py)/halfy

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
        xx=px+x*stepx+((y+layer\start) And 1)*halfx
        yy=py+y*halfy
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
Function layer_iso2_pos(layer.layer,x,y)
  Local factor
  Local halfx
  Local halfy
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

  If (sizey And 1)=0 Then
    stepx=sizex+2
    stepy=sizey
  Else
    stepx=sizex
    stepy=sizey+1
  EndIf

  halfx=stepx/2
  halfy=stepy/2

  layer_x=px+x*stepx+((y+layer\start) And 1)*halfx
  layer_y=py+y*halfy
  Return 1
End Function





;---------------------------------------------------------------------
;layer: layer handle
;
;RETURN LAYER_WIDTH:  layer width in pixel
;RETURN LAYER_HEIGHT: layer height in pixel
;---------------------------------------------------------------------
Function layer_iso2_size(layer.layer)
  Local factor
  Local halfx
  Local halfy
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

  If (sizey And 1)=0 Then
    stepx=sizex+2
    stepy=sizey
  Else
    stepx=sizex
    stepy=sizey+1
  EndIf

  halfx=stepx/2
  halfy=stepy/2

  layer_width =layer\sizex*stepx-stepx+sizex+(layer\sizey>1)*halfx
  layer_height=layer\sizey*halfy-halfy+sizey+factor
End Function