
;Exports a map as an Text-Array-File
;input:
;Layer :layer - the layer that should be exportet
;Filename : String - The File to write

Function export_map(layer.layer,filename$)
  Local anims
  Local bank
  Local canvash
  Local canvasw
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
  Local value
  Local x
  Local xx
  Local y
  Local yy
  Local Linie$


  If layer=Null Then Return

  anims =layer\tile\anims
  count =layer\tile\count
  factor=layer\tile\factor
  image =layer\tile\image
  sizex =layer\tile\sizex
  sizey =layer\tile\sizey-factor

  If sizex<tile_minsize Then sizex=tile_minsize
  If sizey<tile_minsize Then sizey=tile_minsize

  startx=0
  starty=0
  endx=layer\sizex-1
  endy=layer\sizey-1

  Local fileout = WriteFile(filename$)

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
        start=PeekShort(bank,2)
        frame=PeekShort(bank,start*4+8)
      Else
        frame=value-anims
      EndIf

      linie$ = linie$+Str(frame)+";"

;      If frame=>1 And frame<=count Then
;        xx=px+x*sizex
;        yy=py+y*sizey	
;        If layer\mask=0 Then DrawBlock image,xx,yy,frame-1
;        If layer\mask=1 Then DrawImage image,xx,yy,frame-1
;      EndIf

    Next
	WriteLine fileout,linie$
	linie$=""
  Next

  CloseFile fileout
End Function


