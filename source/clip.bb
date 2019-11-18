Global clip.layer





;---------------------------------------------------------------------
Function clip_copy()
  Local selected
  Local value
  Local x
  Local y

  If clip<>Null Then
    If clip\bank1<>0 Then FreeBank clip\bank1
    If clip\bank2<>0 Then FreeBank clip\bank2
    Delete clip
  EndIf

  If editor_layer=Null Then
    layer_clone()
    Return
  EndIf

  selected=SelectedGadgetItem(menu)

  clip.layer =New layer
  clip\code  =-editor_layer\code
  clip\depth1=editor_layer\depth1
  clip\depth2=editor_layer\depth2
  clip\sizex =editor_x2-editor_x1+1
  clip\sizey =editor_y2-editor_y1+1
  clip\tile  =editor_layer\tile

  If editor_layer\code=layer_iso2 Then
    clip\start=((editor_y1+editor_layer\start) And 1)
  ElseIf editor_layer\code=layer_hex1 Then
    clip\start=((editor_x1+editor_layer\start) And 1)
  ElseIf editor_layer\code=layer_hex2 Then
    clip\start=((editor_y1+editor_layer\start) And 1)
  EndIf

  If selected=0 Or selected=1 Or setup_copyall=1 Then
    If editor_layer\bank1<>0 And editor_layer\tile<>Null Then
      If clip\depth1=4  Then clip\bank1=CreateBank((clip\sizex*clip\sizey+1)/2)
      If clip\depth1=8  Then clip\bank1=CreateBank(clip\sizex*clip\sizey)
      If clip\depth1=12 Then clip\bank1=CreateBank((clip\sizex*clip\sizey+1)*3/2-1)
      If clip\depth1=16 Then clip\bank1=CreateBank(clip\sizex*clip\sizey*2)

      For y=0 To clip\sizey-1
        For x=0 To clip\sizex-1
          value=layer_getvalue2(editor_layer,editor_x1+x,editor_y1+y)
          layer_setvalue2(clip,x,y,value)
        Next
      Next
    EndIf
  EndIf

  If selected=2 Or setup_copyall=1 Then
    If editor_layer\bank2<>0 Then
      If clip\depth2=1 Then clip\bank2=CreateBank((clip\sizex*clip\sizey+7)/8)
      If clip\depth2=2 Then clip\bank2=CreateBank((clip\sizex*clip\sizey+3)/4)
      If clip\depth2=4 Then clip\bank2=CreateBank((clip\sizex*clip\sizey+1)/2)
      If clip\depth2=8 Then clip\bank2=CreateBank(clip\sizex*clip\sizey)

      For y=0 To clip\sizey-1
        For x=0 To clip\sizex-1
          value=layer_getdata(editor_layer,editor_x1+x,editor_y1+y)
          layer_setdata(clip,x,y,value)
        Next
      Next
    EndIf
  EndIf
End Function





;---------------------------------------------------------------------
Function clip_cut()
  If editor_layer=Null Then Return
  clip_copy()
  clip_delete()
End Function





;---------------------------------------------------------------------
Function clip_delete()
  Local selected
  Local x
  Local y

  If editor_layer=Null Then Return
  selected=SelectedGadgetItem(menu)

  If selected=0 Or selected=1 Or setup_copyall=1 Then
    If editor_layer\bank1<>0 And editor_layer\tile<>Null Then
      For y=editor_y1 To editor_y2
        For x=editor_x1 To editor_x2
          layer_setvalue2(editor_layer,x,y,0)
        Next
      Next
    EndIf
  EndIf

  If selected=2 Or setup_copyall=1 Then
    If editor_layer\bank2<>0 Then
      For y=editor_y1 To editor_y2
        For x=editor_x1 To editor_x2
          layer_setdata(editor_layer,x,y,0)
        Next
      Next
    EndIf
  EndIf

  editor_update(2)
End Function





;---------------------------------------------------------------------
Function clip_paste()
  Local control
  Local direction
  Local selected
  Local value
  Local x
  Local xx
  Local y
  Local yy

  If clip=Null Then Return
  If editor_layer=Null Then Return
  selected=SelectedGadgetItem(menu)

  If KeyDown(29)=1 Then control=1

  If selected=0 Or selected=1 Or setup_copyall=1 Then
    If clip\bank1<>0 And editor_layer\bank1<>0 And editor_layer\tile<>Null Then
      If clip\tile=Null Then
        If Confirm(language$(80))=0 Then Goto jump
      ElseIf clip\tile<>editor_layer\tile Then
        If Confirm(language$(81))=0 Then Goto jump
      EndIf

      If editor_x1=editor_x2 And editor_y1=editor_y2 Then ;POINT-INSERT

        If editor_layer\code=layer_iso2 And ((editor_y1+editor_layer\start) And 1)<>clip\start Then ;ISO2 -> paste and compensate shifted tiles

          direction=((editor_y1+editor_layer\start) And 1)-clip\start
          For y=0 To clip\sizey-1
            For x=0 To clip\sizex-1
              value=layer_getvalue2(clip,x,y)
              If control=0 Or (control=1 And value>0) Then
                If (y And 1)=0 Then layer_setvalue2(editor_layer,editor_x1+x,editor_y1+y,value)
                If (y And 1)=1 Then layer_setvalue2(editor_layer,editor_x1+x+direction,editor_y1+y,value)
              EndIf
            Next
          Next

        ElseIf editor_layer\code=layer_hex1 And ((editor_x1+editor_layer\start) And 1)<>clip\start Then ;HEX1 -> paste and compensate shifted tiles

          direction=((editor_x1+editor_layer\start) And 1)-clip\start
          For y=0 To clip\sizey-1
            For x=0 To clip\sizex-1
              value=layer_getvalue2(clip,x,y)
              If control=0 Or (control=1 And value>0) Then
                If (x And 1)=0 Then layer_setvalue2(editor_layer,editor_x1+x,editor_y1+y,value)
                If (x And 1)=1 Then layer_setvalue2(editor_layer,editor_x1+x,editor_y1+y+direction,value)
              EndIf
            Next
          Next

        ElseIf editor_layer\code=layer_hex2 And ((editor_y1+editor_layer\start) And 1)<>clip\start Then ;HEX2 -> paste and compensate shifted tiles

          direction=((editor_y1+editor_layer\start) And 1)-clip\start
          For y=0 To clip\sizey-1
            For x=0 To clip\sizex-1
              value=layer_getvalue2(clip,x,y)
              If control=0 Or (control=1 And value>0) Then
                If (y And 1)=0 Then layer_setvalue2(editor_layer,editor_x1+x,editor_y1+y,value)
                If (y And 1)=1 Then layer_setvalue2(editor_layer,editor_x1+x+direction,editor_y1+y,value)
              EndIf
            Next
          Next

        Else ;normal

          For y=0 To clip\sizey-1
            If editor_y1+y>editor_layer\sizey-1 Then Exit
            For x=0 To clip\sizex-1
              If editor_x1+x>editor_layer\sizex-1 Then Exit
              value=layer_getvalue2(clip,x,y)
              If control=0 Or (control=1 And value>0) Then
                layer_setvalue2(editor_layer,editor_x1+x,editor_y1+y,value)
              EndIf
            Next
          Next

        EndIf

      Else

        For y=editor_y1 To editor_y2 ;SELECTION-INSERT
          yy=y-editor_y1
          If yy>clip\sizey-2 Then yy=clip\sizey-2
          If yy<0 Then yy=0
          If y=editor_y2 Then yy=clip\sizey-1
          If y=editor_y1 Then yy=0

          For x=editor_x1 To editor_x2
            xx=x-editor_x1
            If xx>clip\sizex-2 Then xx=clip\sizex-2
            If xx<0 Then xx=0
            If x=editor_x2 Then xx=clip\sizex-1
            If x=editor_x1 Then xx=0

            value=layer_getvalue2(clip,xx,yy)
            If control=0 Or (control=1 And value>0) Then
              layer_setvalue2(editor_layer,x,y,value)
            EndIf
          Next
        Next

      EndIf
    EndIf
  EndIf


  .jump ;-------------------------------------------------------------


  If selected=2 Or setup_copyall=1 Then
    If clip\bank2<>0 And editor_layer\bank2<>0 Then

      If editor_x1=editor_x2 And editor_y1=editor_y2 Then ;POINT-INSERT

        If editor_layer\code=layer_iso2 And ((editor_y1+editor_layer\start) And 1)<>clip\start Then ;ISO2 -> paste and compensate shifted tiles

          direction=((editor_y1+editor_layer\start) And 1)-clip\start
          For y=0 To clip\sizey-1
            For x=0 To clip\sizex-1
              value=layer_getdata(clip,x,y)
              If control=0 Or (control=1 And value>0) Then
                If (y And 1)=0 Then layer_setdata(editor_layer,editor_x1+x,editor_y1+y,value)
                If (y And 1)=1 Then layer_setdata(editor_layer,editor_x1+x+direction,editor_y1+y,value)
              EndIf
            Next
          Next

        ElseIf editor_layer\code=layer_hex1 And ((editor_x1+editor_layer\start) And 1)<>clip\start Then ;HEX1 -> paste and compensate shifted tiles

          direction=((editor_x1+editor_layer\start) And 1)-clip\start
          For y=0 To clip\sizey-1
            For x=0 To clip\sizex-1
              value=layer_getdata(clip,x,y)
              If control=0 Or (control=1 And value>0) Then
                If (x And 1)=0 Then layer_setdata(editor_layer,editor_x1+x,editor_y1+y,value)
                If (x And 1)=1 Then layer_setdata(editor_layer,editor_x1+x,editor_y1+y+direction,value)
              EndIf
            Next
          Next

        ElseIf editor_layer\code=layer_hex2 And ((editor_y1+editor_layer\start) And 1)<>clip\start Then ;HEX2 -> paste and compensate shifted tiles

          direction=((editor_y1+editor_layer\start) And 1)-clip\start
          For y=0 To clip\sizey-1
            For x=0 To clip\sizex-1
              value=layer_getdata(clip,x,y)
              If control=0 Or (control=1 And value>0) Then
                If (y And 1)=0 Then layer_setdata(editor_layer,editor_x1+x,editor_y1+y,value)
                If (y And 1)=1 Then layer_setdata(editor_layer,editor_x1+x+direction,editor_y1+y,value)
              EndIf
            Next
          Next

        Else ;normal

          For y=0 To clip\sizey-1
            If editor_y1+y>editor_layer\sizey-1 Then Exit
            For x=0 To clip\sizex-1
              If editor_x1+x>editor_layer\sizex-1 Then Exit
              value=layer_getdata(clip,x,y)
              If control=0 Or (control=1 And value>0) Then
                layer_setdata(editor_layer,editor_x1+x,editor_y1+y,value)
              EndIf
            Next
          Next

        EndIf

      Else

        For y=editor_y1 To editor_y2 ;SELECTION-INSERT
          yy=y-editor_y1
          If yy>clip\sizey-2 Then yy=clip\sizey-2
          If yy<0 Then yy=0
          If y=editor_y2 Then yy=clip\sizey-1
          If y=editor_y1 Then yy=0

          For x=editor_x1 To editor_x2
            xx=x-editor_x1
            If xx>clip\sizex-2 Then xx=clip\sizex-2
            If xx<0 Then xx=0
            If x=editor_x2 Then xx=clip\sizex-1
            If x=editor_x1 Then xx=0

            value=layer_getdata(clip,xx,yy)
            If control=0 Or (control=1 And value>0) Then
              layer_setdata(editor_layer,x,y,value)
            EndIf
          Next
        Next

      EndIf
    EndIf
  EndIf


  editor_update(2)
End Function