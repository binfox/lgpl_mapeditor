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
Function layer_hex1_coord(layer.layer,x,y,check=0)
  Local arrayx[6]
  Local arrayy[6]
  Local corr
  Local cx#
  Local cy#
  Local dist#
  Local factor
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
  Local width
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

  If sizex=Int((sizey/2)/Sin#(60))*2 And (sizey Mod 4)=0 Then
    stepx=sizex-sizey/4
    stepy=sizey/2
  ElseIf sizex=sizey Then
    stepx=sizex-sizey/4
    stepy=sizey/2
  Else
    stepx=sizex-(sizey-2)/2
    stepy=sizey/2
  EndIf

  width=Int((sizey/2)/Sin#(60))*2
  stretch#=Float#(width)/Float#(sizex)
  min#=1000000

  xx=(x-px)/stepx
  If ((xx+layer\start) And 1)=0 Then
    yy=(y-py)/sizey
    corr=0
  Else
    yy=(y-py-stepy)/sizey
    corr=1
  EndIf

  arrayx[0]=xx-1 : arrayy[0]=yy+corr-1
  arrayx[1]=xx-1 : arrayy[1]=yy+corr
  arrayx[2]=xx   : arrayy[2]=yy-1
  arrayx[3]=xx   : arrayy[3]=yy
  arrayx[4]=xx   : arrayy[4]=yy+1
  arrayx[5]=xx+1 : arrayy[5]=yy+corr-1
  arrayx[6]=xx+1 : arrayy[6]=yy+corr

  For i=0 To 6
    corr=(arrayx[i]+layer\start) And 1
    cx#=(x-px-arrayx[i]*stepx)-Float#(sizex-1)/2
    cy#=(y-py-arrayy[i]*sizey-corr*stepy)-Float#(sizey-1)/2
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
Function layer_hex1_draw(layer.layer)
  Local anims
  Local bank
  Local begin
  Local count
  Local endx
  Local endy
  Local factor
  Local frame
  Local i
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

  If sizex=Int((sizey/2)/Sin#(60))*2 And (sizey Mod 4)=0 Then
    stepx=sizex-sizey/4
    stepy=sizey/2
  ElseIf sizex=sizey Then
    stepx=sizex-sizey/4
    stepy=sizey/2
  Else
    stepx=sizex-(sizey-2)/2
    stepy=sizey/2
  EndIf

  startx=(-sizex-px+stepx)/stepx
  starty=(-stepy-py-factor)/sizey
  endx=(map_width-px)/stepx
  endy=(map_height-py)/sizey

  If startx<0 Then startx=0
  If starty<0 Then starty=0
  If endx>layer\sizex-1 Then endx=layer\sizex-1
  If endy>layer\sizey-1 Then endy=layer\sizey-1

  For y=starty To endy
    For i=0 To 1
      begin=startx+(startx+layer\start+i) And 1
      For x=begin To endx Step 2

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
          xx=px+x*stepx
          yy=py+y*sizey+((x+layer\start) And 1)*stepy
          DrawImage image,xx,yy,frame-1
        EndIf

      Next
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
Function layer_hex1_pos(layer.layer,x,y)
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

  If sizex=Int((sizey/2)/Sin#(60))*2 And (sizey Mod 4)=0 Then
    stepx=sizex-sizey/4
    stepy=sizey/2
  ElseIf sizex=sizey Then
    stepx=sizex-sizey/4
    stepy=sizey/2
  Else
    stepx=sizex-(sizey-2)/2
    stepy=sizey/2
  EndIf

  layer_x=px+x*stepx
  layer_y=py+y*sizey+((x+layer\start) And 1)*stepy
  Return 1
End Function





;---------------------------------------------------------------------
;layer: layer handle
;
;RETURN LAYER_WIDTH:  layer width in pixel
;RETURN LAYER_HEIGHT: layer height in pixel
;---------------------------------------------------------------------
Function layer_hex1_size(layer.layer)
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

  If sizex=Int((sizey/2)/Sin#(60))*2 And (sizey Mod 4)=0 Then
    stepx=sizex-sizey/4
    stepy=sizey/2
  ElseIf sizex=sizey Then
    stepx=sizex-sizey/4
    stepy=sizey/2
  Else
    stepx=sizex-(sizey-2)/2
    stepy=sizey/2
  EndIf

  layer_width =layer\sizex*stepx-stepx+sizex
  layer_height=layer\sizey*sizey+(layer\sizex>1)*stepy+factor
End Function