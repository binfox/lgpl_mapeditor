;---------------------------------------------------------------------
;tile: tileset handle
;---------------------------------------------------------------------
Function layer_iso1_add(tile.tile)
  If demo=0 Then ;**********

  layer        =New layer
  layer\ascii  =0
  layer\bank2  =0
  layer\bank3  =0
  layer\code   =layer_iso1
  	If tile\count > 4094 Then
		layer\depth1=16
		layer\bank1  =CreateBank(10*10*2)		
		
	ElseIf tile\count > 254 Then 
		layer\depth1=12
		layer\bank1  =CreateBank( (10*10) +((10*11)/2))		

	ElseIf tile\count > 14 Then 
		layer\depth1 =8
		layer\bank1  =CreateBank(10*10)
	Else
		layer\depth1 =4
		layer\bank1  =CreateBank(((10*10)+1)/2)	
	EndIf    

  layer\depth2 =0
  layer\frame  =0
  layer\layer  =Null
  layer\mask   =1
  layer\mode   =0
  layer\name$  =language$(61)
  layer\parax  =100
  layer\paray  =100
  layer\posx   =0
  layer\posy   =0
  layer\sizex  =10
  layer\sizey  =10
  layer\start  =0
  layer\tile   =tile
  layer\visible=1

  layer_count=layer_count+1
  layer_nr=layer_count  
  layer_list[layer_nr]=layer

  EndIf ;**********
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
Function layer_iso1_coord(layer.layer,sliderx,slidery,x,y,check=0)
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

  If demo=0 Then ;**********

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

  px=editor_getscreen(layer\posx,layer\parax,sliderx)
  py=editor_getscreen(layer\posy,layer\paray,slidery)

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

  EndIf ;**********
End Function





;---------------------------------------------------------------------
;layer:   layer handle
;sliderx: sliderx value
;slidery: slidery value
;---------------------------------------------------------------------
Function layer_iso1_draw(layer.layer,sliderx,slidery)
  Local anims
  Local arrayx[3]
  Local arrayy[3]
  Local bank
  Local canvash
  Local canvasw
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

  If demo=0 Then ;**********

  canvasw=editor_width()
  canvash=editor_height()

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

  px=editor_getscreen(layer\posx,layer\parax,sliderx)
  py=editor_getscreen(layer\posy,layer\paray,slidery)

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

  layer_iso1_coord(layer.layer,sliderx,slidery,0,-factor) ;top_left
  arrayx[0]=layer_x
  arrayy[0]=layer_y

  layer_iso1_coord(layer.layer,sliderx,slidery,0,canvash) ;bottom_left
  arrayx[1]=layer_x
  arrayy[1]=layer_y

  layer_iso1_coord(layer.layer,sliderx,slidery,canvasw,-factor) ;top_right
  arrayx[2]=layer_x
  arrayy[2]=layer_y

  layer_iso1_coord(layer.layer,sliderx,slidery,canvasw,canvash) ;bottom_right
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

      If xx=>-stepx And yy=>-stepy-factor And xx<=canvasw And yy<=canvash Then

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
          start=PeekShort(bank,2)
          frame=PeekShort(bank,start*4+8)
        Else
          frame=value-anims
        EndIf

        If frame=>1 And frame<=count Then DrawImage image,xx,yy,frame-1

      EndIf
    Next
  Next

  EndIf ;**********
End Function





;---------------------------------------------------------------------
;layer:   layer handle
;sliderx: sliderx value
;slidery: slidery value
;---------------------------------------------------------------------
Function layer_iso1_grid(layer.layer,sliderx,slidery)
  Local arrayx[3]
  Local arrayy[3]
  Local b
  Local canvash
  Local canvasw
  Local endx
  Local endy
  Local factor
  Local font
  Local fontx
  Local fonty
  Local g
  Local halfx
  Local halfy
  Local i
  Local mode
  Local offset
  Local posx
  Local px
  Local py
  Local r
  Local rectx
  Local recty
  Local sizex
  Local sizey
  Local startx
  Local starty
  Local stepx
  Local stepy
  Local strlen
  Local strnr$
  Local value
  Local x
  Local xx
  Local y
  Local yy

  If demo=0 Then ;**********

  canvasw=editor_width()
  canvash=editor_height()

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

  px=editor_getscreen(layer\posx,layer\parax,sliderx)
  py=editor_getscreen(layer\posy,layer\paray,slidery)

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
  rectx=sizex+(layer\sizex+layer\sizey-2)*halfx
  recty=sizey+(layer\sizex+layer\sizey-2)*halfy+factor

  layer_iso1_coord(layer.layer,sliderx,slidery,0,0) ;top_left
  arrayx[0]=layer_x
  arrayy[0]=layer_y

  layer_iso1_coord(layer.layer,sliderx,slidery,0,canvash) ;bottom_left
  arrayx[1]=layer_x
  arrayy[1]=layer_y

  layer_iso1_coord(layer.layer,sliderx,slidery,canvasw,0) ;top_right
  arrayx[2]=layer_x
  arrayy[2]=layer_y

  layer_iso1_coord(layer.layer,sliderx,slidery,canvasw,canvash) ;bottom_right
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

  If SelectedGadgetItem(menu)=2 And layer\bank2<>0 Then ;DATALAYER
    If sizex<32 Then
      font=window_font5
      fontx=5
      fonty=7
    Else
      font=window_font7
      fontx=7
      fonty=10
    EndIf

    For y=starty To endy
      For x=startx To endx
        xx=px+posx+(x-y)*halfx
        yy=py+(x+y)*halfy
        If xx=>-stepx And yy=>-stepy And xx<=canvasw And yy<=canvash Then

          If layer\depth2=1 Then
            offset=((y*layer\sizex+x)/8)
            mode  =((y*layer\sizex+x) Mod 8)
            value =(PeekByte(layer\bank2,offset) Shr mode) And 1
          ElseIf layer\depth2=2 Then
            offset=((y*layer\sizex+x)/4)
            mode  =((y*layer\sizex+x) Mod 4)*2
            value =(PeekByte(layer\bank2,offset) Shr mode) And 3
          ElseIf layer\depth2=4 Then
            offset=((y*layer\sizex+x)/2)
            mode  =((y*layer\sizex+x) Mod 2)*4
            value =(PeekByte(layer\bank2,offset) Shr mode) And 15
          ElseIf layer\depth2=8 Then
            offset=y*layer\sizex+x
            value =PeekByte(layer\bank2,offset)
          EndIf

          If value>0 Then
            strnr$=Str$(value)
            strlen=Len(strnr$)

            xx=xx  +(sizex-strlen*fontx)/2
            yy=yy  +(sizey-fonty)/2

            For i=0 To strlen-1
              DrawBlock font,xx+i*fontx,yy,Mid$(strnr$,i+1,1)
            Next
          EndIf

        EndIf
      Next
    Next
  EndIf

  If maker[0]<>0 And editor_layer=layer Then ;SELECTION
    For y=starty To endy
      For x=startx To endx
        xx=px+posx+(x-y)*halfx
        yy=py+(x+y)*halfy
        If xx=>-stepx And yy=>-stepy And xx<=canvasw And yy<=canvash Then
          If x=>editor_x1 And y=>editor_y1 And x<=editor_x2 And y<=editor_y2 Then
            DrawImage maker[0],xx,yy
          EndIf
        EndIf
      Next
    Next
  EndIf

  If maker[1]<>0 And toolbar_xgrid=1 Then ;GRID
    For y=starty To endy
      For x=startx To endx
        xx=px+posx+(x-y)*halfx
        yy=py+(x+y)*halfy
        If xx=>-stepx And yy=>-stepy And xx<=canvasw And yy<=canvash Then
          DrawImage maker[1],xx,yy
        EndIf
      Next
    Next
  EndIf

  If maker[2]<>0 And toolbar_xgrid=1 And endy=layer\sizey-1 Then ;GRID LEFT-BOTTOM
    For x=startx To endx
      xx=px+posx+(x-endy)*halfx
      yy=py+(x+endy)*halfy
      If xx=>-stepx And yy=>-stepy And xx<=canvasw And yy<=canvash Then
        DrawImage maker[2],xx,yy
      EndIf
    Next
  EndIf

  If maker[3]<>0 And toolbar_xgrid=1 And endx=layer\sizex-1 Then ;GRID RIGHT-BOTTOM
    For y=starty To endy
      xx=px+posx+(endx-y)*halfx
      yy=py+(endx+y)*halfy
      If xx=>-stepx And yy=>-stepy And xx<=canvasw And yy<=canvash Then
        DrawImage maker[3],xx,yy
      EndIf
    Next
  EndIf

  r=(setup_color1 And $FF0000)/$10000
  g=(setup_color1 And $FF00)/$100
  b=(setup_color1 And $FF)
  Color r,g,b
  Rect px,py,rectx,recty,0
  Rect px,py+recty-factor-1,rectx,1 ;FACTOR
  If toolbar_xmap=0 Then
    Rect px-3,py-3,7,7
    Rect px-3,py+recty-4,7,7
    Rect px+rectx-4,py-3,7,7
    Rect px+rectx-4,py+recty-4,7,7
  EndIf
  If layer\code=layer_clone Then
    Line px,py,px+rectx-1,py+recty-1-factor
    Line px+rectx-1,py,px,py+recty-1-factor
  EndIf

  EndIf ;**********
End Function





;---------------------------------------------------------------------
;layer:   layer handle
;sliderx: sliderx value
;slidery: slidery value
;x:       position x
;y:       position y
;RETURN:  0=error, 1=ok
;---------------------------------------------------------------------
Function layer_iso1_pos(layer.layer,sliderx,slidery,x,y)
  Local factor
  Local halfx
  Local halfy
  Local posx
  Local px
  Local py
  Local sizex
  Local sizey
  Local stepx
  Local stepy

  If demo=0 Then ;**********

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

  px=editor_getscreen(layer\posx,layer\parax,sliderx)
  py=editor_getscreen(layer\posy,layer\paray,slidery)

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

  layer_x=px+posx+(x-y)*halfx
  layer_y=py+(x+y)*halfy
  Return 1

  EndIf ;**********
End Function





;---------------------------------------------------------------------
;layer: layer handle
;---------------------------------------------------------------------
Function layer_iso1_size(layer.layer)
  Local factor
  Local halfx
  Local halfy
  Local sizex
  Local sizey
  Local stepx
  Local stepy

  If demo=0 Then ;**********

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

  layer_width =sizex+(layer\sizex+layer\sizey-2)*halfx
  layer_height=sizey+(layer\sizex+layer\sizey-2)*halfy+factor

  EndIf ;**********
End Function