;---------------------------------------------------------------------
Function layer_oval_add()
  layer        =New layer
  layer\ascii  =0
  layer\bank1  =0
  layer\bank2  =0
  layer\bank3  =0
  layer\code   =layer_oval
  layer\depth1 =0
  layer\depth2 =0
  layer\frame  =0
  layer\layer  =Null
  layer\mask   =0
  layer\mode   =0
  layer\name$  =language$(52)
  layer\parax  =100
  layer\paray  =100
  layer\posx   =50
  layer\posy   =50
  layer\sizex  =50
  layer\sizey  =50
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
;active:  0=normal, 1=selected
;---------------------------------------------------------------------
Function layer_oval_draw(layer.layer,sliderx,slidery,active)
  Local b
  Local g
  Local px
  Local py
  Local r

  If layer=Null Then Return

  px=editor_getscreen(layer\posx,layer\parax,sliderx)
  py=editor_getscreen(layer\posy,layer\paray,slidery)

  r=(setup_color1 And $FF0000)/$10000
  g=(setup_color1 And $FF00)/$100
  b=(setup_color1 And $FF)
  Color r,g,b

  Oval px-layer\sizex,py-layer\sizey,layer\sizex*2+1,layer\sizey*2+1,0
  If active=1 Then
    Rect px-5,py,11,1
    Rect px,py-5,1,11
    Rect px-3-layer\sizex,py-3-layer\sizey,7,7
    Rect px-3-layer\sizex,py-3+layer\sizey,7,7
    Rect px-3+layer\sizex,py-3-layer\sizey,7,7
    Rect px-3+layer\sizex,py-3+layer\sizey,7,7
  EndIf
End Function





;---------------------------------------------------------------------
;layer:   layer handle
;sliderx: sliderx value
;slidery: slidery value
;x:       screen x
;y:       screen y
;RETURN:  0=outside, 1=over
;---------------------------------------------------------------------
Function layer_oval_over(layer.layer,sliderx,slidery,x,y)
  If layer=Null Then Return

  px=editor_getscreen(layer\posx,layer\parax,sliderx)
  py=editor_getscreen(layer\posy,layer\paray,slidery)

  diff1#=Float#(layer\sizey+3)/Float#(layer\sizex+3)
  diff2#=Float#(layer\sizey-3)/Float#(layer\sizex-3)

  radius1=Sqr#((px-x)^2+((py-y)/diff1#)^2)
  radius2=Sqr#((px-x)^2+((py-y)/diff2#)^2)

  dist1=Abs(x-px)-layer\sizex
  dist2=Abs(y-py)-layer\sizey

  If dist1=>-3 And dist2=>-3 And dist1<=3 And dist2<=3 Then Return 2 ;edges
  If radius1<=layer\sizex+3 And radius2=>layer\sizex-3 Then Return 1 ;border
End Function