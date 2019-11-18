;---------------------------------------------------------------------
;tile: tileset handle
;---------------------------------------------------------------------
Function layer_hex1_add(tile.tile)
  If demo=0 Then ;**********

  layer        =New layer
  layer\ascii  =0
  layer\bank2  =0
  layer\bank3  =0
  layer\code   =layer_hex1
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
  layer\name$  =language$(63)
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
Function layer_hex1_coord(layer.layer,sliderx,slidery,x,y,check=0)
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

  EndIf ;**********
End Function





;---------------------------------------------------------------------
;layer:   layer handle
;sliderx: sliderx value
;slidery: slidery value
;---------------------------------------------------------------------
Function layer_hex1_draw(layer.layer,sliderx,slidery)
  Local anims
  Local bank
  Local begin
  Local canvash
  Local canvasw
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
  endx=(canvasw-px)/stepx
  endy=(canvash-py)/sizey

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
          start=PeekShort(bank,2)
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

  EndIf ;**********
End Function





;---------------------------------------------------------------------
;layer:   layer handle
;sliderx: sliderx value
;slidery: slidery value
;---------------------------------------------------------------------
Function layer_hex1_grid(layer.layer,sliderx,slidery)
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
  Local i
  Local mode
  Local offset
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

  rectx=layer\sizex*stepx-stepx+sizex
  recty=layer\sizey*sizey+(layer\sizex>1)*stepy+factor

  startx=(-sizex-px+stepx)/stepx
  starty=(-stepy-py)/sizey
  endx=(canvasw-px)/stepx
  endy=(canvash-py)/sizey

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

          xx=px+x*stepx                                +(sizex-strlen*fontx)/2
          yy=py+y*sizey+((x+layer\start) And 1)*stepy  +(sizey-fonty)/2

          For i=0 To strlen-1
            DrawBlock font,xx+i*fontx,yy,Mid$(strnr$,i+1,1)
          Next
        EndIf

      Next
    Next
  EndIf

  If maker[0]<>0 And editor_layer=layer Then ;SELECTION
    For y=starty To endy
      For x=startx To endx
        If x=>editor_x1 And y=>editor_y1 And x<=editor_x2 And y<=editor_y2 Then
          xx=px+x*stepx
          yy=py+y*sizey+((x+layer\start) And 1)*stepy
          DrawImage maker[0],xx,yy
        EndIf
      Next
    Next
  EndIf

  If maker[1]<>0 And toolbar_xgrid=1 Then ;GRID
    For y=starty To endy
      For x=startx To endx
        xx=px+x*stepx
        yy=py+y*sizey+((x+layer\start) And 1)*stepy
        DrawImage maker[1],xx,yy
      Next
    Next
  EndIf

  If maker[2]<>0 And toolbar_xgrid=1 And endy=layer\sizey-1 Then ;GRID BOTTOM H
    For x=startx To endx
      If ((x+layer\start) And 1)=0 Then
        xx=px+x*stepx
        yy=py+endy*sizey
        DrawImage maker[2],xx,yy
      EndIf
    Next
  EndIf

  If maker[3]<>0 And toolbar_xgrid=1 And starty=0 Then ;GRID TOP
    For x=startx To endx
      If ((x+layer\start) And 1)=0 Then
        xx=px+x*stepx
        yy=py
        DrawImage maker[3],xx,yy
      EndIf
    Next
  EndIf

  If maker[4]<>0 And toolbar_xgrid=1 And endy=layer\sizey-1 Then ;GRID BOTTOM
    For x=startx To endx
      If ((x+layer\start) And 1)=1 Then
        xx=px+x*stepx
        yy=py+endy*sizey+stepy
        DrawImage maker[4],xx,yy
      EndIf
    Next
  EndIf

  If maker[3]<>0 And maker[4]<>0 And toolbar_xgrid=1 And endx=layer\sizex-1 Then ;GRID RIGHT
    For y=starty To endy
      xx=px+endx*stepx
      yy=py+y*sizey+((endx+layer\start) And 1)*stepy
      DrawImage maker[3],xx,yy
      DrawImage maker[4],xx,yy
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
Function layer_hex1_pos(layer.layer,sliderx,slidery,x,y)
  Local factor
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

  EndIf ;**********
End Function





;---------------------------------------------------------------------
;layer: layer handle
;---------------------------------------------------------------------
Function layer_hex1_size(layer.layer)
  Local factor
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

  EndIf ;**********
End Function