;---------------------------------------------------------------------
Function layer_point_add()
  layer        =New layer
  layer\ascii  =0
  layer\bank1  =0
  layer\bank2  =0
  layer\bank3  =0
  layer\code   =layer_point
  layer\depth1 =0
  layer\depth2 =0
  layer\frame  =0
  layer\layer  =Null
  layer\mask   =0
  layer\mode   =0
  layer\name$  =language$(49)
  layer\parax  =100
  layer\paray  =100
  layer\posx   =50
  layer\posy   =50
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
;active:  0=normal, 1=selected
;---------------------------------------------------------------------
Function layer_point_draw(layer.layer,sliderx,slidery,active)
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

  Rect px-5,py,11,1
  Rect px,py-5,1,11
  If active=1 Then Rect px-3,py-3,7,7
End Function





;---------------------------------------------------------------------
;layer:   layer handle
;sliderx: sliderx value
;slidery: slidery value
;x:       screen x
;y:       screen y
;RETURN:  0=outside, 1=over
;---------------------------------------------------------------------
Function layer_point_over(layer.layer,sliderx,slidery,x,y)
  Local px
  Local py

  If layer=Null Then Return

  px=editor_getscreen(layer\posx,layer\parax,sliderx)
  py=editor_getscreen(layer\posy,layer\paray,slidery)

  If x=>px-3 And y=>py-3 And x<=px+3 And y<=py+3 Then Return 1
End Function