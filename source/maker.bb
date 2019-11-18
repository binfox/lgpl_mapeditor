Global maker[4]
Global makerpattern





;---------------------------------------------------------------------
;form:   tile form (0 to 5)
;width:  tile width
;height: tile height
;RETURN: 0=no match, 1=match
;---------------------------------------------------------------------
Function maker_compare(form,width,height)
  Local x
  Local y

  Select form
    Case 0 ;iso1
      y=width/2-1
      If y=height And (width Mod 4)=0 Then Return 1

    Case 1 ;iso2
      y=width/2+1
      If y=height And ((width+2) Mod 4)=0 Then Return 1

    Case 2 ;iso3
      y=width-1
      If y=height And (width Mod 2)=0 Then Return 1

    Case 3 ;hex1
      y=width/2+1
      If y=height And (height Mod 2)=0 Then Return 1

    Case 4 ;hex2
      x=Int((height/2)/Sin#(60))*2
      If x=width And (height Mod 4)=0 Then Return 1

    Case 5 ;hex3
      y=Int((width/2)/Sin#(60))*2
      If y=height And (width Mod 4)=0 Then Return 1
  End Select
End Function





;---------------------------------------------------------------------
Function maker_exit()
  Local i

  For i=0 To 4
    If maker[i]<>0 Then FreeImage maker[i]
  Next

  If makerpattern<>0 Then FreeImage makerpattern
End Function





;---------------------------------------------------------------------
;form:   tile form (-1 to 5)
;width:  tile width
;height: tile height
;image:  image handle
;color1: background color
;color2: foreground color
;RETURN: image handle
;---------------------------------------------------------------------
Function maker_image(form,width,height,image,color1,color2)
  Local b
  Local g
  Local r
  Local size
  Local x
  Local xx
  Local y
  Local yy

  If image<>0 Then FreeImage image
  image=CreateImage(width,height,1,2)
  SetBuffer ImageBuffer(image)

  r=(color1 And $FF0000)/$10000
  g=(color1 And $FF00)/$100
  b=(color1 And $FF)
  ClsColor r,g,b
  Cls

  r=(color2 And $FF0000)/$10000
  g=(color2 And $FF00)/$100
  b=(color2 And $FF)
  Color r,g,b

  Select form
    Case -2 ;unknown
      If width<height Then size=width*0.35 Else size=height*0.35
      Oval width/2-size,height/2-size,size*2+1,size*2+1,1

    Case -1 ;rect
      Rect 0,0,width,height

    Case 0 ;iso1
      For y=0 To height/2
        Rect width/2-2-y*2,y,4+y*4,1
        Rect width/2-2-y*2,height-1-y,4+y*4,1
      Next

    Case 1 ;iso2
      For y=0 To height/2
        Rect width/2-1-y*2,y,2+y*4,1
        Rect width/2-1-y*2,height-1-y,2+y*4,1
      Next

    Case 2 ;iso3
      For y=0 To height/2
        Rect width/2-1-y,y,2+y*2,1
        Rect width/2-1-y,height-1-y,2+y*2,1
      Next

    Case 3 ;hex1
      For y=0 To height/2
        yy=(height/2-1)-y
        Rect yy,y,width-yy*2,1
        Rect yy,height-1-y,width-yy*2,1
      Next

    Case 4 ;hex2
      For y=0 To height/2
        yy=((height/2)-y)/2
        Rect yy,y,width-yy*2,1
        Rect yy,height-1-y,width-yy*2,1
      Next

    Case 5 ;hex3
      For x=0 To width/2
        xx=((width/2)-x)/2
        Rect x,xx,1,height-xx*2
        Rect width-1-x,xx,1,height-xx*2
      Next

  End Select

  Return image
End Function





;---------------------------------------------------------------------
;form: tile form (0-5)
;size: combobox handle
;---------------------------------------------------------------------
Function maker_size(form,size)
  Local x
  Local y

  ClearGadgetItems size

  Select form
    Case 0 ;iso1
      For x=36 To 400 Step 4
        y=x/2-1
        AddGadgetItem size,Str$(x)+" : "+Str$(y)
      Next

    Case 1 ;iso2
      For x=30 To 400 Step 4
        y=x/2+1
        AddGadgetItem size,Str$(x)+" : "+Str$(y)
      Next

    Case 2 ;iso3
      For x=18 To 400 Step 2
        y=x-1
        AddGadgetItem size,Str$(x)+" : "+Str$(y)
      Next

    Case 3 ;hex1
      For x=30 To 400 Step 4
        y=x/2+1
        AddGadgetItem size,Str$(x)+" : "+Str$(y)
      Next

    Case 4 ;hex2
      For y=16 To 344 Step 4
        x=Int((y/2)/Sin#(60))*2
        AddGadgetItem size,Str$(x)+" : "+Str$(y)
      Next

    Case 5 ;hex3
      For x=16 To 344 Step 4
        y=Int((x/2)/Sin#(60))*2
        AddGadgetItem size,Str$(x)+" : "+Str$(y)
      Next

  End Select

  SelectGadgetItem size,0
End Function





;---------------------------------------------------------------------
Function maker_start()
  Local buffer
  Local rgb
  Local x
  Local y

  makerpattern=CreateImage(16,16)
  buffer=ImageBuffer(makerpattern)
  LockBuffer buffer
  For y=0 To 15
    For x=0 To 15
      If ((x+y) And 1)=1 Then rgb=$000000 Else rgb=$FFFFFF
      WritePixelFast x,y,rgb,buffer
    Next
  Next
  UnlockBuffer buffer
  MaskImage makerpattern,255,255,255
End Function





;---------------------------------------------------------------------
Function maker_prog()
  If demo=1 Then
    Notify language$(261)
    Return
  EndIf

  If demo=0 Then ;**********

    Local b
    Local color1
    Local color2
    Local form=1
    Local g
    Local height
    Local image
    Local name$
    Local pos
    Local r
    Local size
    Local tilesize$
    Local width

    Local subwin       =CreateWindow   (language$(29),(window_maxx-610)/2,(window_maxy-414)/2,610,414,window,33)
    Local subwin_width =ClientWidth    (subwin)
    Local subwin_height=ClientHeight   (subwin)
    Local panel        =CreatePanel    (5,5,404,404,subwin,1)
    Local canvas       =CreateCanvas   (0,0,400,400,panel)
    Local label1       =CreateLabel    (language$(91)+":",420,15,70,20,subwin)
    Local label2       =CreateLabel    (language$(92)+":",420,40,70,20,subwin)
    Local label3       =CreateLabel    (language$(93)+":",420,65,70,20,subwin)
    Local label4       =CreateLabel    (language$(94)+":",420,90,70,20,subwin)
    Local gadget_form  =CreateComboBox (500,10,100,22,subwin)
    Local gadget_size  =CreateComboBox (500,35,100,22,subwin)
    Local gadget_color1=CreatePanel    (500,60,100,22,subwin,1)
    Local gadget_color2=CreatePanel    (500,85,100,22,subwin,1)
    Local button_save  =CreateButton   (language$(3),420,subwin_height-32,85,22,subwin)
    Local button_ok    =CreateButton   (language$(70),515,subwin_height-32,85,22,subwin)

    AddGadgetItem gadget_form,language$(65)+" 1",1
    AddGadgetItem gadget_form,language$(65)+" 2"
    AddGadgetItem gadget_form,language$(65)+" 3"
    AddGadgetItem gadget_form,language$(66)+" 1"
    AddGadgetItem gadget_form,language$(66)+" 2"
    AddGadgetItem gadget_form,language$(66)+" 3"

    maker_size(0,gadget_size)
    color1=$000000
    color2=$FFFFFF

    DisableGadget window
    Goto update


    Repeat
      WaitEvent()

      Select EventID()
        Case $0110 ;ESC-------------------------------------------------
          Exit

        Case $0201 ;mousedown-------------------------------------------
          If EventSource()=gadget_color1 Then
            r=(color1 And $FF0000)/$10000
            g=(color1 And $FF00)/$100
            b=(color1 And $FF)
            If RequestColor(r,g,b)=1 Then
              r=RequestedRed()
              g=RequestedGreen()
              b=RequestedBlue()
              color1=r*$10000+g*$100+b
            EndIf
          ElseIf EventSource()=gadget_color2 Then
            r=(color2 And $FF0000)/$10000
            g=(color2 And $FF00)/$100
            b=(color2 And $FF)
            If RequestColor(r,g,b)=1 Then
              r=RequestedRed()
              g=RequestedGreen()
              b=RequestedBlue()
              color2=r*$10000+g*$100+b
            EndIf
          EndIf

        Case $0401 ;gadgetevent-----------------------------------------
          Select EventSource()

            Case button_ok
              Exit

            Case button_save
              If image<>0 Then
                name$=RequestFile(language$(3),"bmp",1)
                If name$<>"" Then SaveImage image,name$
              EndIf

            Case gadget_form
              form=SelectedGadgetItem(gadget_form)
              maker_size(form,gadget_size)

          End Select

        Case $0803 ;windowclose-----------------------------------------
          Exit
      End Select

      .update
      form=SelectedGadgetItem(gadget_form)
      size=SelectedGadgetItem(gadget_size)
      tilesize$=GadgetItemText$(gadget_size,size)
      pos=Instr(tilesize$,":")
      width=Left$(tilesize$,pos-1)
      height=Mid$(tilesize$,pos+1)
      image=maker_image(form,width,height,image,color1,color2)

      SetBuffer CanvasBuffer(canvas)
      ClsColor 0,0,0
      Cls
      DrawBlock image,(400-width)/2,(400-height)/2
      FlipCanvas canvas

      r=(color1 And $FF0000)/$10000
      g=(color1 And $FF00)/$100
      b=(color1 And $FF)
      SetPanelColor gadget_color1,r,g,b

      r=(color2 And $FF0000)/$10000
      g=(color2 And $FF00)/$100
      b=(color2 And $FF)
      SetPanelColor gadget_color2,r,g,b

      ActivateGadget subwin
    Forever


    If image<>0 Then FreeImage image
    EnableGadget window
    FreeGadget button_save
    FreeGadget button_ok
    FreeGadget gadget_color2
    FreeGadget gadget_color1
    FreeGadget gadget_size
    FreeGadget gadget_form
    FreeGadget label4
    FreeGadget label3
    FreeGadget label2
    FreeGadget label1
    FreeGadget canvas
    FreeGadget panel
    FreeGadget subwin
    ActivateWindow window

  EndIf ;**********
End Function





;---------------------------------------------------------------------
Function maker_update()
  Local code
  Local factor
  Local i
  Local sizex
  Local sizey

  For i=0 To 4
    If maker[i]<>0 Then
      FreeImage maker[i]
      maker[i]=0
    EndIf
  Next

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

  code=layer\code
  If code=layer_clone And layer\layer= Null Then code=layer_map
  If code=layer_clone And layer\layer<>Null Then code=layer\layer\code

  Select code
    Case layer_map
      maker_update2(-1,sizex,sizey)

    Case layer_iso1,layer_iso2
      If maker_compare(0,sizex,sizey)=1 Then
        maker_update2(0,sizex,sizey)
      ElseIf maker_compare(1,sizex,sizey)=1 Then
        maker_update2(1,sizex,sizey)
      ElseIf maker_compare(2,sizex,sizey)=1 Then
        maker_update2(2,sizex,sizey)
      ElseIf sizex=sizey Then
        maker_update2(2,sizex,sizey)
      Else
        maker_update2(-2,sizex,sizey)
      EndIf

    Case layer_hex1
      If maker_compare(3,sizex,sizey)=1 Then
        maker_update2(3,sizex,sizey)
      ElseIf maker_compare(4,sizex,sizey)=1 Then
        maker_update2(4,sizex,sizey)
      ElseIf sizex=sizey Then
        maker_update2(4,sizex,sizey)
      Else
        maker_update2(-2,sizex,sizey)
      EndIf

    Case layer_hex2
      If maker_compare(5,sizex,sizey)=1 Then
        maker_update2(5,sizex,sizey)
      ElseIf sizex=sizey Then
        maker_update2(5,sizex,sizey)
      Else
        maker_update2(-2,sizex,sizey)
      EndIf

  End Select
End Function





;---------------------------------------------------------------------
;form:   tile form (-2 to 5)
;width:  tile width
;height: tile height
;---------------------------------------------------------------------
Function maker_update2(form,width,height)
  Local b3
  Local g3
  Local i
  Local r3
  Local size
  Local x
  Local xx
  Local y
  Local yy

  r3=(setup_color3 And $FF0000)/$10000
  g3=(setup_color3 And $FF00)/$100
  b3=(setup_color3 And $FF)

  Select form
    Case -2 ;unknown
      maker[1]=CreateImage(width,height,1,2)
      SetBuffer ImageBuffer(maker[1])
      Color r3,g3,b3
      If width<height Then size=width*0.35 Else size=height*0.35
      Oval width/2-size,height/2-size,size*2+1,size*2+1,0

    Case -1 ;rect
      maker[1]=CreateImage(width,height,1,2)
      SetBuffer ImageBuffer(maker[1])
      Color r3,g3,b3
      Rect 0,0,width,1
      Rect 0,0,1,height

    Case 0 ;iso1
      For i=1 To 3
        maker[i]=CreateImage(width,height,1,2)
        SetBuffer ImageBuffer(maker[i])
        Color r3,g3,b3
        For y=0 To height/2
          If i=1 Then Rect width/2-y*2-2,y,2,1
          If i=1 Then Rect width/2+y*2  ,y,2,1
          If i=2 Then Rect width/2-y*2-2,height-1-y,2,1
          If i=3 Then Rect width/2+y*2  ,height-1-y,2,1
        Next
      Next

    Case 1 ;iso2
      For i=1 To 3
        maker[i]=CreateImage(width,height,1,2)
        SetBuffer ImageBuffer(maker[i])
        Color r3,g3,b3
        For y=0 To height/2
          If i=1 Then Rect width/2-1-y*2,y,2,1
          If i=1 Then Rect width/2-1+y*2,y,2,1
          If i=2 Then Rect width/2-1-y*2,height-1-y,2,1
          If i=3 Then Rect width/2-1+y*2,height-1-y,2,1
        Next
      Next

    Case 2 ;iso3
      For i=1 To 3
        maker[i]=CreateImage(width,height,1,2)
        SetBuffer ImageBuffer(maker[i])
        For y=0 To height/2
          If i=1 Then WritePixel width/2-y-1,y,setup_color3
          If i=1 Then WritePixel width/2+y  ,y,setup_color3
          If i=2 Then WritePixel width/2-y-1,height-1-y,setup_color3
          If i=3 Then WritePixel width/2+y  ,height-1-y,setup_color3
        Next
      Next

    Case 3 ;hex1
      For i=1 To 4
        maker[i]=CreateImage(width,height,1,2)
        SetBuffer ImageBuffer(maker[i])
        Color r3,g3,b3
        For y=0 To height/2
          yy=(height/2-1)-y
          If i=1 Then WritePixel yy,y,setup_color3
          If i=1 Then WritePixel yy,height-1-y,setup_color3
          If i=1 And y=0 Then Rect yy,y,width-yy*2,1
          If i=2 And y=0 Then Rect yy,height-1-y,width-yy*2,1
          If i=3 Then WritePixel width-1-yy,y,setup_color3
          If i=4 Then WritePixel width-1-yy,height-1-y,setup_color3
        Next
      Next

    Case 4 ;hex2
      For i=1 To 4
        maker[i]=CreateImage(width,height,1,2)
        SetBuffer ImageBuffer(maker[i])
        Color r3,g3,b3
        For y=0 To height/2
          yy=((height/2)-y)/2
          If i=1 Then WritePixel yy,y,setup_color3
          If i=1 Then WritePixel yy,height-1-y,setup_color3
          If i=1 And y=0 Then Rect yy,y,width-yy*2,1
          If i=2 And y=0 Then Rect yy,height-1-y,width-yy*2,1
          If i=3 Then WritePixel width-1-yy,y,setup_color3
          If i=4 Then WritePixel width-1-yy,height-1-y,setup_color3
        Next
      Next

    Case 5 ;hex3
      For i=1 To 4
        maker[i]=CreateImage(width,height,1,2)
        SetBuffer ImageBuffer(maker[i])
        Color r3,g3,b3
        For x=0 To width/2
          xx=((width/2)-x)/2
          If i=1 Then WritePixel x,xx,setup_color3
          If i=1 Then WritePixel width-1-x,xx,setup_color3
          If i=1 And x=0 Then Rect x,xx,1,height-xx*2
          If i=2 And x=0 Then Rect width-1-x,xx,1,height-xx*2
          If i=3 Then WritePixel x,height-1-xx,setup_color3
          If i=4 Then WritePixel width-1-x,height-1-xx,setup_color3
        Next
      Next

  End Select

  maker[0]=maker_image(form,width,height,maker[0],$000000,setup_color2)
  SetBuffer ImageBuffer(maker[0])
  TileImage makerpattern
End Function