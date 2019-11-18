;---------------------------------------------------------------------
Function layer_rect_add()
  layer        =New layer
  layer\ascii  =0
  layer\bank1  =0
  layer\bank2  =0
  layer\bank3  =0
  layer\code   =layer_rect
  layer\depth1 =0
  layer\depth2 =0
  layer\frame  =0
  layer\layer  =Null
  layer\mask   =0
  layer\mode   =0
  layer\name$  =language$(51)
  layer\parax  =100
  layer\paray  =100
  layer\posx   =0
  layer\posy   =0
  layer\sizex  =100
  layer\sizey  =100
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
Function layer_rect_draw(layer.layer,sliderx,slidery,active)
  Local b
  Local g
  Local px
  Local py
  Local r
  Local sizex
  Local sizey

  If layer=Null Then Return

  px=editor_getscreen(layer\posx,layer\parax,sliderx)
  py=editor_getscreen(layer\posy,layer\paray,slidery)
  sizex=layer\sizex
  sizey=layer\sizey

  If sizex<0 Then
    sizex=-sizex
    px=px-sizex
  EndIf
  If sizey<0 Then
    sizey=-sizey
    py=py-sizey
  EndIf

  r=(setup_color1 And $FF0000)/$10000
  g=(setup_color1 And $FF00)/$100
  b=(setup_color1 And $FF)
  Color r,g,b

  Rect px,py,sizex,sizey,0
  If active=1 Then
    Rect px-3,py-3,7,7
    Rect px-3,py-4+sizey,7,7
    Rect px-4+sizex,py-3,7,7
    Rect px-4+sizex,py-4+sizey,7,7
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
Function layer_rect_over(layer.layer,sliderx,slidery,x,y)
  Local px
  Local py

  If layer=Null Then Return

  px=editor_getscreen(layer\posx,layer\parax,sliderx)
  py=editor_getscreen(layer\posy,layer\paray,slidery)

  If x=>px-3 And y=>py-3 And x<=px+3 And y<=py+3 Then Return 2 ;top-left
  If x=>px-3 And y=>py-4+layer\sizey And x<=px+3 And y<=py+2+layer\sizey Then Return 3 ;bottom-left
  If x=>px-4+layer\sizex And y=>py-3 And x<=px+2+layer\sizex And y<=py+3 Then Return 4 ;top-right
  If x=>px-4+layer\sizex And y=>py-4+layer\sizey And x<=px+2+layer\sizex And y<=py+2+layer\sizey Then Return 5 ;bottom-right

  If x=>px-3 And y=>py-3 And x<=px+2+layer\sizex And y<=py+2+layer\sizey Then
    If x<=px+3 Or y<=py+3 Or x=>px-4+layer\sizex Or y=>py-4+layer\sizey Then Return 1 ;border
  EndIf
End Function