;---------------------------------------------------------------------
Function layer_line_add()
  layer        =New layer
  layer\ascii  =0
  layer\bank1  =0
  layer\bank2  =0
  layer\bank3  =0
  layer\code   =layer_line
  layer\depth1 =0
  layer\depth2 =0
  layer\frame  =0
  layer\layer  =Null
  layer\mask   =0
  layer\mode   =0
  layer\name$  =language$(50)
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
Function layer_line_draw(layer.layer,sliderx,slidery,active)
  Local b
  Local g
  Local px1
  Local px2
  Local py1
  Local py2
  Local r

  If layer=Null Then Return

  px1=editor_getscreen(layer\posx,layer\parax,sliderx)
  py1=editor_getscreen(layer\posy,layer\paray,slidery)
  px2=editor_getscreen(layer\sizex,layer\parax,sliderx)
  py2=editor_getscreen(layer\sizey,layer\paray,slidery)

  r=(setup_color1 And $FF0000)/$10000
  g=(setup_color1 And $FF00)/$100
  b=(setup_color1 And $FF)
  Color r,g,b

  Line px1,py1,px2,py2
  If active=1 Then
    Rect px1-3,py1-3,7,7
    Rect px2-3,py2-3,7,7
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
Function layer_line_over(layer.layer,sliderx,slidery,x,y)
  Local alpha#
  Local px1
  Local px2
  Local py1
  Local py2
  Local size
  Local xx
  Local yy

  If layer=Null Then Return

  px1=editor_getscreen(layer\posx,layer\parax,sliderx)
  py1=editor_getscreen(layer\posy,layer\paray,slidery)
  px2=editor_getscreen(layer\sizex,layer\parax,sliderx)
  py2=editor_getscreen(layer\sizey,layer\paray,slidery)

  size=Sqr#((layer\posy-layer\sizey)^2+(layer\sizex-layer\posx)^2)
  alpha#=ATan2#(layer\posy-layer\sizey,(layer\sizex-layer\posx))

  xx=(x-px1)*Cos#(alpha#)-(y-py1)*Sin#(alpha#)
  yy=(y-py1)*Cos#(alpha#)+(x-px1)*Sin#(alpha#)

  If x=>px1-3 And y=>py1-3 And x<=px1+3 And y<=py1+3 Then Return 2 ;first point
  If x=>px2-3 And y=>py2-3 And x<=px2+3 And y<=py2+3 Then Return 3 ;second point
  If xx=>-3 And yy=>-3 And xx<=size+3 And yy<=3 Then Return 1 ;line
End Function