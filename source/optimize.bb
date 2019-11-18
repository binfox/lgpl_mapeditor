Dim optimize_dim(0)





;---------------------------------------------------------------------
Function optimize_prog()
  If demo=1 Then
    Notify language$(261)
    Return
  EndIf

  If demo=0 Then ;**********

    Local subwin       =CreateWindow(language$(28),(window_maxx-230)/2,(window_maxy-355)/2,230,355,window,33)
    Local subwin_width =ClientWidth (subwin)
    Local subwin_height=ClientHeight(subwin)
    Local option01     =CreateButton(language$(152),5,005,subwin_width-10,20,subwin,2)
    Local option02     =CreateButton(language$(153),5,025,subwin_width-10,20,subwin,2)
    Local option03     =CreateButton(language$(154),5,045,subwin_width-10,20,subwin,2)
    Local option04     =CreateButton(language$(155),5,065,subwin_width-10,20,subwin,2)
    Local option05     =CreateButton(language$(156),5,085,subwin_width-10,20,subwin,2)
    Local option06     =CreateButton(language$(157),5,120,subwin_width-10,20,subwin,2)
    Local option07     =CreateButton(language$(158),5,140,subwin_width-10,20,subwin,2)
    Local option08     =CreateButton(language$(159),5,160,subwin_width-10,20,subwin,2)
    Local option09     =CreateButton(language$(160),5,180,subwin_width-10,20,subwin,2)
    Local option10     =CreateButton(language$(161),5,200,subwin_width-10,20,subwin,2)
    Local option11     =CreateButton(language$(162),5,235,subwin_width-10,20,subwin,2)
    Local option12     =CreateButton(language$(163),5,255,subwin_width-10,20,subwin,2)
    Local option13     =CreateButton(language$(164),5,275,subwin_width-10,20,subwin,2)
    Local option14     =CreateButton(language$(165),5,295,subwin_width-10,20,subwin,2)
    Local panel1       =CreatePanel (5,110,subwin_width-10,5,subwin)
    Local panel2       =CreatePanel (5,225,subwin_width-10,5,subwin)
    Local button_ok    =CreateButton(language$(70),subwin_width/2-90,subwin_height-32,85,22,subwin)
    Local button_cancel=CreateButton(language$(71),subwin_width/2+5,subwin_height-32,85,22,subwin)
    Local modify

    SetPanelColor panel1,150,150,150
    SetPanelColor panel2,150,150,150
    DisableGadget window


    Repeat
      WaitEvent()

      Select EventID()
        Case $0110 ;ESC-------------------------------------------------
          Goto abort

        Case $0401 ;gadgetevent-----------------------------------------
          Select EventSource()
            Case button_ok
              Exit
            Case button_cancel
              Goto abort
          End Select

        Case $0803 ;windowclose-----------------------------------------
          Goto abort
      End Select
    Forever


    If ButtonState(option01)=1 Then optimize_01() : modify=1
    If ButtonState(option02)=1 Then optimize_02() : modify=1
    If ButtonState(option03)=1 Then optimize_03() : modify=1
    If ButtonState(option04)=1 Then optimize_04() : modify=1
    If ButtonState(option05)=1 Then optimize_05() : modify=1
    If ButtonState(option06)=1 Then optimize_06() : modify=1
    If ButtonState(option07)=1 Then optimize_07() : modify=1
    If ButtonState(option08)=1 Then optimize_08() : modify=1
    If ButtonState(option09)=1 Then optimize_09() : modify=1
    If ButtonState(option10)=1 Then optimize_10() : modify=1
    If ButtonState(option11)=1 Then optimize_11() : modify=1
    If ButtonState(option12)=1 Then optimize_12() : modify=1
    If ButtonState(option13)=1 Then optimize_13() : modify=1
    If ButtonState(option14)=1 Then optimize_clear() : modify=1
    Dim optimize_dim(0)


    .abort
    EnableGadget window
    FreeGadget button_cancel
    FreeGadget button_ok
    FreeGadget panel2
    FreeGadget panel1
    FreeGadget option14
    FreeGadget option13
    FreeGadget option12
    FreeGadget option11
    FreeGadget option10
    FreeGadget option09
    FreeGadget option08
    FreeGadget option07
    FreeGadget option06
    FreeGadget option05
    FreeGadget option04
    FreeGadget option03
    FreeGadget option02
    FreeGadget option01
    FreeGadget subwin
    ActivateWindow window

    If modify=1 Then
      list_select(0)
      Notify language$(166)
    EndIf

  EndIf ;**********
End Function





;---------------------------------------------------------------------
;Daten bereinigen
;---------------------------------------------------------------------
Function optimize_clear()
  Local bank
  Local frames
  Local i
  Local j
  Local layer.layer
  Local maxvalue
  Local tile.tile
  Local value
  Local x
  Local y

  For tile=Each tile
    For i=tile\anims To 1 Step -1

      bank=PeekInt(tile\banka,i*4-4)
      frames=PeekShort(bank,0)

      For j=frames To 1 Step -1
        value=PeekShort(bank,j*4+8)
        If value>tile\count Then PokeShort bank,j*4+8,0
      Next

    Next
  Next

  For layer=Each layer
    If layer\tile= Null Then maxvalue=0
    If layer\tile<>Null Then maxvalue=layer\tile\anims+layer\tile\count

    If layer\code=layer_map Or layer\code=layer_iso1 Or layer\code=layer_iso2 Or layer\code=layer_hex1 Or layer\code=layer_hex2 Then

      For y=0 To layer\sizey-1
        For x=0 To layer\sizex-1
          value=layer_getvalue2(layer,x,y)
          If value>maxvalue Then layer_setvalue2(layer,x,y,0)
        Next
      Next

    ElseIf layer\code=layer_image Or layer\code=layer_block Then

      If layer\frame>maxvalue Then layer\frame=0

    EndIf
  Next
End Function





;---------------------------------------------------------------------
;Ungültige Tilesets entfernen
;---------------------------------------------------------------------
Function optimize_01()
  Local tile.tile

  For tile=Each tile
    If tile\image=0 Then tile_del(tile)
  Next
End Function





;---------------------------------------------------------------------
;Unbenutzte Tilesets entfernen
;---------------------------------------------------------------------
Function optimize_02()
  Local layer.layer
  Local tile.tile

  For tile=Each tile
    For layer=Each layer
      If layer\tile=tile Then Goto skip
    Next

    tile_del(tile)
    .skip
  Next
End Function





;---------------------------------------------------------------------
;Unbenutzte Animationen entfernen
;---------------------------------------------------------------------
Function optimize_03()
  Local i
  Local layer.layer
  Local tile.tile
  Local value
  Local x
  Local y

  For tile=Each tile
    If tile\anims>0 Then

      Dim optimize_dim(tile\anims)

      For layer=Each layer
        If layer\tile=tile Then
          If layer\code=layer_map Or layer\code=layer_iso1 Or layer\code=layer_iso2 Or layer\code=layer_hex1 Or layer\code=layer_hex2 Then

            For y=0 To layer\sizey-1
              For x=0 To layer\sizex-1
                value=layer_getvalue2(layer,x,y)
                If value=>1 And value<=tile\anims Then optimize_dim(value)=1
              Next
            Next

          ElseIf layer\code=layer_image Or layer\code=layer_block Then

            If layer\frame=>1 And layer\frame<=tile\anims Then optimize_dim(layer\frame)=1
        
          EndIf
        EndIf
      Next

      For i=tile\anims To 1 Step -1
        If optimize_dim(i)=0 Then tile_delanim(tile,i)
      Next

    EndIf
  Next
End Function





;---------------------------------------------------------------------
;Leere Frames entfernen
;---------------------------------------------------------------------
Function optimize_04()
  Local bank
  Local frames
  Local i
  Local j
  Local tile.tile
  Local value

  For tile=Each tile
    For i=tile\anims To 1 Step -1

      bank=PeekInt(tile\banka,i*4-4)
      frames=PeekShort(bank,0)

      For j=frames To 1 Step -1
        value=PeekShort(bank,j*4+8)
        If value=0 Or value>tile\count Then tile_delframe(tile,i,j)
      Next

    Next
  Next
End Function





;---------------------------------------------------------------------
;Leere Animationen entfernen
;---------------------------------------------------------------------
Function optimize_05()
  Local bank
  Local frames
  Local i
  Local j
  Local tile.tile
  Local value

  For tile=Each tile
    For i=tile\anims To 1 Step -1
      bank=PeekInt(tile\banka,i*4-4)
      frames=PeekShort(bank,0)

      For j=frames To 1 Step -1
        value=PeekShort(bank,j*4+8)
        If value=>1 And value<=tile\count Then Goto skip
      Next

      tile_delanim(tile,i)
      .skip
    Next
  Next
End Function





;---------------------------------------------------------------------
;Ungültige Layer entfernen
;---------------------------------------------------------------------
Function optimize_06()
  Local i
  Local layer.layer

  For i=layer_count To 1 Step -1
    layer=layer_list[i]

    If layer\code=layer_map Or layer\code=layer_iso1 Or layer\code=layer_iso2 Or layer\code=layer_hex1 Or layer\code=layer_hex2 Or layer\code=layer_clone Then
      If layer\tile=Null Then
        layer_delete(i,0)
      Else
        If layer\tile\image=0 Then layer_delete(i,0)
      EndIf
    EndIf
  Next
End Function





;---------------------------------------------------------------------
;Unbenutzte Layer entfernen
;---------------------------------------------------------------------
Function optimize_07()
  Local i
  Local layer.layer
  Local value
  Local x
  Local y

  For i=layer_count To 1 Step -1
    layer=layer_list[i]

    If layer\code=layer_map Or layer\code=layer_iso1 Or layer\code=layer_iso2 Or layer\code=layer_hex1 Or layer\code=layer_hex2 Then
      For y=0 To layer\sizey-1
        For x=0 To layer\sizex-1
          value=layer_getvalue2(layer,x,y)
          If value>0 Then Goto skip
        Next
      Next

      If layer\bank2<>0 Then
        For y=0 To layer\sizey-1
          For x=0 To layer\sizex-1
            value=layer_getdata(layer,x,y)
            If value>0 Then Goto skip
          Next
        Next
      EndIf
      
      layer_delete(i,0)
    EndIf

    .skip
  Next

  For i=layer_count To 1 Step -1
    layer=layer_list[i]
    If layer\code=layer_clone Then
      If layer\tile=Null Or layer\layer=Null Then layer_delete(i,0)
    EndIf
  Next
End Function





;---------------------------------------------------------------------
;Layergröße optimieren
;---------------------------------------------------------------------
Function optimize_08()
  Local layer.layer
  Local maxx
  Local maxy
  Local minx
  Local miny
  Local value
  Local x
  Local y

  For layer=Each layer
    If layer\code=layer_map Or layer\code=layer_iso1 Or layer\code=layer_iso2 Or layer\code=layer_hex1 Or layer\code=layer_hex2 Then

      minx=layer\sizex-1
      miny=layer\sizey-1
      maxx=0
      maxy=0

      For y=0 To layer\sizey-1
        For x=0 To layer\sizex-1
          value=layer_getvalue2(layer,x,y)
          If value>0 Then
            If minx>x Then minx=x
            If miny>y Then miny=y
            If maxx<x Then maxx=x
            If maxy<y Then maxy=y
          EndIf
        Next
      Next

      If layer\bank2<>0 Then
        For y=0 To layer\sizey-1
          For x=0 To layer\sizex-1
            value=layer_getdata(layer,x,y)
            If value>0 Then
              If minx>x Then minx=x
              If miny>y Then miny=y
              If maxx<x Then maxx=x
              If maxy<y Then maxy=y
            EndIf
          Next
        Next
      EndIf

      If maxx=0 And maxy=0 Then
        layer_resize(layer,1,1,layer\depth1,layer\depth2)
      Else
        layer_resize(layer,maxx-minx+1,maxy-miny+1,layer\depth1,layer\depth2,-minx,-miny)
      EndIf

    EndIf
  Next
End Function





;---------------------------------------------------------------------
;Alle Datenlayer entfernen
;---------------------------------------------------------------------
Function optimize_09()
  Local layer.layer

  For layer=Each layer
    If layer\code=layer_map Or layer\code=layer_iso1 Or layer\code=layer_iso2 Or layer\code=layer_hex1 Or layer\code=layer_hex2 Then
      If layer\bank2<>0 Then
        FreeBank layer\bank2
        layer\depth2=0
      EndIf
    EndIf
  Next
End Function





;---------------------------------------------------------------------
;Unbenutzte Datenlayer entfernen
;---------------------------------------------------------------------
Function optimize_10()
  Local layer.layer
  Local value
  Local x
  Local y

  For layer=Each layer
    If layer\code=layer_map Or layer\code=layer_iso1 Or layer\code=layer_iso2 Or layer\code=layer_hex1 Or layer\code=layer_hex2 Then
      If layer\bank2<>0 Then

        For y=0 To layer\sizey-1
          For x=0 To layer\sizex-1
            value=layer_getdata(layer,x,y)
            If value>0 Then Goto skip
          Next
        Next
         
        FreeBank layer\bank2
        layer\depth2=0

      EndIf
    EndIf

    .skip
  Next
End Function





;---------------------------------------------------------------------
;Ungültige Bild-Objekte entfernen
;---------------------------------------------------------------------
Function optimize_11()
  Local i
  Local kill
  Local layer.layer

  For i=layer_count To 1 Step -1
    layer=layer_list[i]

    If layer\code=layer_image Or layer\code=layer_block Then

      If layer\tile=Null Then
        kill=1
      Else
        If layer\frame=0 Or layer\frame>layer\tile\anims+layer\tile\count Then kill=1
        If layer\tile\image=0 Then kill=1
      EndIf

      If kill=1 Then layer_delete(i,0)
      kill=0

    EndIf
  Next
End Function





;---------------------------------------------------------------------
;Geometrie-Objekte entfernen
;---------------------------------------------------------------------
Function optimize_12()
  Local i
  Local layer.layer

  For i=layer_count To 1 Step -1
    layer=layer_list[i]

    If layer\code=layer_point Or layer\code=layer_line Or layer\code=layer_rect Or layer\code=layer_oval Then
      layer_delete(i,0)
    EndIf
  Next
End Function





;---------------------------------------------------------------------
;Metainfos entfernen
;---------------------------------------------------------------------
Function optimize_13()
  Local layer.layer

  For layer=Each layer
    If layer\bank3<>0 Then
      FreeBank layer\bank3
      layer\bank3=0
    EndIf
  Next
End Function