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
Function layer_iso1_coord(layer.layer,x,y,check=0)
  Local arrayx[8]
  Local arrayy[8]
  Local cx#
  Local cy#
  Local dist#
  Local factor
  Local halfx
  Local halfy
  Local i
  Local min#
  Local outside
  Local posx
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
  posx=(layer\sizey-1)*halfx
  stretch#=Float#(stepy)/Float#(stepx)
  min#=1000000

  xx=(Float#(x-px-posx)/Float#(halfx) + Float#(y)/Float#(halfy) - Float#(py)/Float#(halfy))/2-1
  yy=(Float#(y-py)/Float#(halfy) - Float#(x)/Float#(halfx) + Float#(px)/Float#(halfx) + Float#(posx)/Float#(halfx))/2

  arrayx[0]=xx-1 : arrayy[0]=yy-1
  arrayx[1]=xx-1 : arrayy[1]=yy
  arrayx[2]=xx   : arrayy[2]=yy-1
  arrayx[3]=xx-1 : arrayy[3]=yy+1
  arrayx[4]=xx   : arrayy[4]=yy
  arrayx[5]=xx+1 : arrayy[5]=yy-1
  arrayx[6]=xx   : arrayy[6]=yy+1
  arrayx[7]=xx+1 : arrayy[7]=yy
  arrayx[8]=xx+1 : arrayy[8]=yy+1

  For i=0 To 8
    cx#=(x-px-posx-arrayx[i]*halfx+arrayy[i]*halfx)-Float#(sizex-1)/2
    cy#=(y-py-arrayx[i]*halfy-arrayy[i]*halfy)-Float#(sizey-1)/2
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
Function layer_iso1_draw(layer.layer)
  Local anims
  Local arrayx[3]
  Local arrayy[3]
  Local bank
  Local count
  Local endx
  Local endy
  Local factor
  Local frame
  Local halfx
  Local halfy
  Local i
  Local image
  Local mode
  Local offset
  Local posx
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
  posx=(layer\sizey-1)*halfx

  layer_iso1_coord(layer,0,-factor) ;top_left
  arrayx[0]=layer_x
  arrayy[0]=layer_y

  layer_iso1_coord(layer,0,map_height) ;bottom_left
  arrayx[1]=layer_x
  arrayy[1]=layer_y

  layer_iso1_coord(layer,map_width,-factor) ;top_right
  arrayx[2]=layer_x
  arrayy[2]=layer_y

  layer_iso1_coord(layer,map_width,map_height) ;bottom_right
  arrayx[3]=layer_x
  arrayy[3]=layer_y

  startx=layer\sizex-1
  starty=layer\sizey-1

  For i=0 To 3
    If arrayx[i]<startx Then startx=arrayx[i]
    If arrayy[i]<starty Then starty=arrayy[i]
    If arrayx[i]>endx Then endx=arrayx[i]
    If arrayy[i]>endy Then endy=arrayy[i]
  Next

  If startx<0 Then startx=0
  If starty<0 Then starty=0
  If endx>layer\sizex-1 Then endx=layer\sizex-1
  If endy>layer\sizey-1 Then endy=layer\sizey-1

  For y=starty To endy
    For x=startx To endx
      xx=px+posx+(x-y)*halfx
      yy=py+(x+y)*halfy

      If xx=>-stepx And yy=>-stepy-factor And xx<=map_width And yy<=map_height Then

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
          bank=PeekInt(layer\tile\bank,value*4-4)
          start=PeekShort(bank,10)
          frame=PeekShort(bank,start*4+8)
        Else
          frame=value-anims
        EndIf

        If frame=>1 And frame<=count Then DrawImage image,xx,yy,frame-1

      EndIf
    Next
  Next
End Function





; ---------------------------------------------------------------------
;layer: layer handle
;x:     position x
;y:     position y
;
;RETURN:         0=error, 1=ok
;RETURN LAYER_X: screen pos x
;RETURN LAYER_Y: screen pos y
; ---------------------------------------------------------------------
Procedure layer_iso1_pos(*layer.l, x.l, y.l)
  Protected factor.l, halfx.l, halfy.l, posx.l, px.l, py.l, sizex.l, sizey.l, stepx.l, stepy.l
  
  If *layer = 0 : ProcedureReturn #False : EndIf
  
  If *layer\tile
    factor = *layer\tile\factor
    sizex = *layer\tile\sizex
    sizey = *layer\tile\sizey - factor
  Else
    sizex = tile_default
    sizey = tile_default
  EndIf
  
  If sizex < tile_minsize : sizex = tile_minsize : EndIf
  If sizey < tile_minsize : sizey = tile_minsize : EndIf
  
  px = map_getscreen(*layer\posx, *layer\parax, map_scrollx)
  py = map_getscreen(*layer\posy, *layer\paray, map_scrolly)
  
  If (sizey & 1) = 0
    stepx = sizex + 2
    stepy = sizey
  Else
    stepx = sizex
    stepy = sizey + 1
  EndIf
  
  halfx = stepx / 2
  halfy = stepy / 2
  posx = (*layer\izey - 1) * halfx
  
  layer_x = px + posx + (x - y) * halfx
  layer_y = py + (x + y) * halfy
  ProcedureReturn 1
EndProcedure






; ---------------------------------------------------------------------
;layer: layer handle
;
;RETURN LAYER_WIDTH:  layer width in pixel
;RETURN LAYER_HEIGHT: layer height in pixel
; ---------------------------------------------------------------------
Procedure layer_iso1_size(*layer.layer)
  Protected factor.l, halfx.l, halfy.l, sizex.l, sizey.l, stepx.l, stepy.l
  
  If *layer = 0 : ProcedureReturn #False : EndIf
  
  If *layer\tile
    factor = *layer\tile\factor
    sizex = *layer\tile\sizex
    sizey = *layer\tile\sizey - factor
  Else
    sizex = tile_default
    sizey = tile_default
  EndIf
  
  If sizex < tile_minsize : sizex = tile_minsize : EndIf
  If sizey < tile_minsize : sizey = tile_minsize : EndIf
  
  If (sizey & 1) = 0
    stepx = sizex + 2
    stepy = sizey
  Else
    stepx = sizex
    stepy = sizey + 1
  EndIf
  
  halfx = stepx / 2
  halfy = stepy / 2
  
  layer_width = sizex + (*layer\sizex + *layer\sizey - 2) * halfx
  layer_height = sizey + (*layer\sizex + *layer\sizey - 2) * halfy + factor
EndProcedure 
; jaPBe Version=1.4.4.25
; FoldLines=00F8011B
; Build=0
; CompileThis=..\preview_bbplus.pb
; FirstLine=0
; CursorPosition=248
; ExecutableFormat=Windows
; DontSaveDeclare
; EOF