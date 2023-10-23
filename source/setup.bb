Global setup_color1=$FFFFFF ;border color
Global setup_color2=$FFFF00 ;mark color
Global setup_color3=$999999 ;grid color
Global setup_color4=$FF0000 ;zero color
Global setup_confirm=0
Global setup_copyall=0
Global setup_language$="deutsch"
Global setup_mouse=0
Global setup_path$
Global setup_started=0
Global setup_sysram=0
Global setup_tileview=0
Global setup_usedef=0
Global setup_minzoom=4
Global setup_tilelayout=1
Global setup_tilezoom=1





;---------------------------------------------------------------------
Function setup_block()
  If layer=Null Then Return
  If layer\code<>layer_block Then Return

  Local bank
  Local count
  Local frames
  Local max
  Local noupdate
  Local selected
  Local tmptile.tile
  Local tmplayer.layer

  Local mask =layer\mask
  Local mode =layer\mode
  Local name$=layer\name$
  Local parax=layer\parax
  Local paray=layer\paray
  Local posx =layer\posx
  Local posy =layer\posy
  Local start=layer\start
  Local tile.tile=layer\tile
  Local parent.layer=layer\layer
  Local image=layer\frame ;readonly

  Local subwin       =CreateWindow   (language$(167),(window_maxx-230)/2,(window_maxy-265)/2,210,290,window,33)
  Local subwin_width =ClientWidth    (subwin)
  Local subwin_height=ClientHeight   (subwin)
  Local label1       =CreateLabel    (language$(76) +":",5,008,70,20,subwin)
  Local label2       =CreateLabel    (language$(75) +":",5,033,70,20,subwin)
  Local label3       =CreateLabel    (language$(78) +":",5,058,70,20,subwin)
  Local label4       =CreateLabel    (language$(168)+":",5,083,70,20,subwin)
  Local label5       =CreateLabel    (language$(169)+":",5,108,70,20,subwin)
  Local label6       =CreateLabel    (language$(170)+":",5,133,70,20,subwin)
  Local label7       =CreateLabel    (language$(171)+":",5,158,70,20,subwin)
  Local label8       =CreateLabel    (language$(172)+":",5,183,70,20,subwin)
  Local label9       =CreateLabel    (language$(173)+":",5,208,70,20,subwin)
  Local gadget_parent=CreateComboBox (85,005,140,22,subwin)
  Local gadget_tile  =CreateComboBox (85,030,140,22,subwin)
  Local gadget_mode  =CreateComboBox (85,055,140,22,subwin)
  Local gadget_start =CreateTextField(85,080,140,22,subwin)
  Local gadget_name  =CreateTextField(85,105,140,22,subwin)
  Local gadget_posx  =CreateTextField(85,130,140,22,subwin)
  Local gadget_posy  =CreateTextField(85,155,140,22,subwin)
  Local gadget_parax =CreateTextField(85,180,140,22,subwin)
  Local gadget_paray =CreateTextField(85,205,140,22,subwin)
  Local gadget_mask  =CreateButton   (language$(174),85,230,140,22,subwin,2)
  Local button_ok    =CreateButton   (language$(70),subwin_width/2-90,subwin_height-32,85,22,subwin)
  Local button_cancel=CreateButton   (language$(71),subwin_width/2+5,subwin_height-32,85,22,subwin)

  SetGadgetIconStrip gadget_parent,window_strip2
  SetGadgetIconStrip gadget_tile,window_strip3
  SetGadgetIconStrip gadget_mode,window_strip3

  AddGadgetItem gadget_mode,language$(175),0,2
  AddGadgetItem gadget_mode,language$(176),0,3
  AddGadgetItem gadget_mode,language$(177),0,4
  AddGadgetItem gadget_mode,language$(178),0,5

  For tmptile=Each tile
    AddGadgetItem gadget_tile,tmptile\file$,(tmptile=layer\tile),(tmptile\image=0)
  Next

  For tmplayer=Each layer
    If tmplayer\code=layer_map Or tmplayer\code=layer_iso1 Or tmplayer\code=layer_iso2 Or tmplayer\code=layer_hex1 Or tmplayer\code=layer_hex2 Or tmplayer\code=layer_clone Then
      AddGadgetItem gadget_parent,tmplayer\name$,(tmplayer=parent),tmplayer\code
    EndIf
  Next

  DisableGadget window
  Goto update


  Repeat
    noupdate=0
    WaitEvent()

    Select EventID()
      Case $0110 ;ESC-------------------------------------------------
        Exit

      Case $0401 ;gadgetevent-----------------------------------------
        Select EventSource()

          Case button_ok
            Exit

          Case button_cancel
            Goto abort

          Case gadget_mode
            mode=SelectedGadgetItem(gadget_mode)

          Case gadget_mask
            mask=ButtonState(gadget_mask)

          Case gadget_name
            name$=Left$(TextFieldText$(gadget_name),12)

          Case gadget_parax
            parax=extra_input(gadget_parax)
            noupdate=3

          Case gadget_paray
            paray=extra_input(gadget_paray)
            noupdate=4

          Case gadget_parent
            selected=SelectedGadgetItem(gadget_parent)
            count=0
            parent=Null
            For tmplayer=Each layer
              If tmplayer\code=layer_map Or tmplayer\code=layer_iso1 Or tmplayer\code=layer_iso2 Or tmplayer\code=layer_hex1 Or tmplayer\code=layer_hex2 Or tmplayer\code=layer_clone Then
                If count=selected Then parent=tmplayer
                count=count+1
              EndIf
            Next

          Case gadget_posx
            posx=extra_input(gadget_posx)
            noupdate=1

          Case gadget_posy
            posy=extra_input(gadget_posy)
            noupdate=2

          Case gadget_start
            If tile<>Null Then max=tile\anims Else max=0
            If image=>1 And image<=max Then
              bank=PeekInt(tile\banka,image*4-4) ;animbank
              frames=PeekShort(bank,0)           ;frames
              start=extra_input(gadget_start,0,1,frames)
              noupdate=5
            EndIf

          Case gadget_tile
            selected=SelectedGadgetItem(gadget_tile)
            count=0
            tile=Null
            For tmptile=Each tile
              If count=selected Then tile=tmptile
              count=count+1
            Next

        End Select

      Case $0803 ;windowclose-----------------------------------------
        Goto abort
    End Select

    .update
    SetGadgetText gadget_name,name$
    SetButtonState gadget_mask,mask

    If tile<>Null Then max=tile\anims Else max=0
    If image=>1 And image<=max Then
      EnableGadget gadget_mode
      SelectGadgetItem gadget_mode,mode
    Else
      mode=0
      start=1
      DisableGadget gadget_mode
      SelectGadgetItem gadget_mode,mode
    EndIf

    If mode=0 Then
      DisableGadget gadget_start
      SetGadgetText gadget_start,""
    Else
      EnableGadget gadget_start
      If noupdate<>5 Then SetGadgetText gadget_start,Str$(start)
    EndIf

    If noupdate<>1 Then SetGadgetText gadget_posx ,Str$(posx)
    If noupdate<>2 Then SetGadgetText gadget_posy ,Str$(posy)
    If noupdate<>3 Then SetGadgetText gadget_parax,Str$(parax)
    If noupdate<>4 Then SetGadgetText gadget_paray,Str$(paray)
  Forever


  layer\mask =mask
  layer\mode =mode
  layer\name$=name$
  layer\parax=parax
  layer\paray=paray
  layer\posx =posx
  layer\posy =posy
  layer\start=start
  layer\tile =tile
  layer\layer=parent

  .abort
  EnableGadget window
  FreeGadget button_cancel
  FreeGadget button_ok
  FreeGadget gadget_mask
  FreeGadget gadget_paray
  FreeGadget gadget_parax
  FreeGadget gadget_posy
  FreeGadget gadget_posx
  FreeGadget gadget_name
  FreeGadget gadget_start
  FreeGadget gadget_mode
  FreeGadget gadget_tile
  FreeGadget gadget_parent
  FreeGadget label9
  FreeGadget label8
  FreeGadget label7
  FreeGadget label6
  FreeGadget label5
  FreeGadget label4
  FreeGadget label3
  FreeGadget label2
  FreeGadget label1
  FreeGadget subwin
  ActivateWindow window
End Function





;---------------------------------------------------------------------
Function setup_clone()
  If layer=Null Then Return
  If layer\code<>layer_clone Then Return

  Local count
  Local noupdate
  Local selected
  Local tmplayer.layer

  Local name$=layer\name$
  Local parax=layer\parax
  Local paray=layer\paray
  Local posx =layer\posx
  Local posy =layer\posy
  Local parent.layer=layer\layer

  Local subwin       =CreateWindow   (language$(179),(window_maxx-230)/2,(window_maxy-200)/2,230,200,window,33)
  Local subwin_width =ClientWidth    (subwin)
  Local subwin_height=ClientHeight   (subwin)
  Local label1       =CreateLabel    (language$(76) +":",5,008,70,20,subwin)
  Local label2       =CreateLabel    (language$(180)+":",5,033,70,20,subwin)
  Local label3       =CreateLabel    (language$(181)+":",5,058,70,20,subwin)
  Local label4       =CreateLabel    (language$(182)+":",5,083,70,20,subwin)
  Local label5       =CreateLabel    (language$(183)+":",5,108,70,20,subwin)
  Local label6       =CreateLabel    (language$(184)+":",5,133,70,20,subwin)
  Local gadget_parent=CreateComboBox (85,005,140,22,subwin)
  Local gadget_name  =CreateTextField(85,030,140,22,subwin)
  Local gadget_posx  =CreateTextField(85,055,140,22,subwin)
  Local gadget_posy  =CreateTextField(85,080,140,22,subwin)
  Local gadget_parax =CreateTextField(85,105,140,22,subwin)
  Local gadget_paray =CreateTextField(85,130,140,22,subwin)
  Local button_ok    =CreateButton   (language$(70),subwin_width/2-90,subwin_height-32,85,22,subwin)
  Local button_cancel=CreateButton   (language$(71),subwin_width/2+5,subwin_height-32,85,22,subwin)

  SetGadgetIconStrip gadget_parent,window_strip2

  For tmplayer=Each layer
    If tmplayer\code=layer_map Or tmplayer\code=layer_iso1 Or tmplayer\code=layer_iso2 Or tmplayer\code=layer_hex1 Or tmplayer\code=layer_hex2 Then
      AddGadgetItem gadget_parent,tmplayer\name$,(tmplayer=parent),tmplayer\code
    EndIf
  Next

  DisableGadget window
  Goto update


  Repeat
    noupdate=0
    WaitEvent()

    Select EventID()
      Case $0110 ;ESC-------------------------------------------------
        Exit

      Case $0401 ;gadgetevent-----------------------------------------
        Select EventSource()

          Case button_ok
            Exit

          Case button_cancel
            Goto abort

          Case gadget_name
            name$=Left$(TextFieldText$(gadget_name),12)

          Case gadget_parax
            parax=extra_input(gadget_parax,0,0,200)
            noupdate=3

          Case gadget_paray
            paray=extra_input(gadget_paray,0,0,200)
            noupdate=4

          Case gadget_posx
            posx=extra_input(gadget_posx)
            noupdate=1

          Case gadget_posy
            posy=extra_input(gadget_posy)
            noupdate=2

          Case gadget_parent
            selected=SelectedGadgetItem(gadget_parent)
            count=0
            parent=Null
            For tmplayer=Each layer
              If tmplayer\code=layer_map Or tmplayer\code=layer_iso1 Or tmplayer\code=layer_iso2 Or tmplayer\code=layer_hex1 Or tmplayer\code=layer_hex2 Then
                If count=selected Then parent=tmplayer
                count=count+1
              EndIf
            Next

        End Select

      Case $0803 ;windowclose-----------------------------------------
        Goto abort
    End Select

    .update
    SetGadgetText gadget_name,name$
    If noupdate<>1 Then SetGadgetText gadget_posx ,Str$(posx)
    If noupdate<>2 Then SetGadgetText gadget_posy ,Str$(posy)
    If noupdate<>3 Then SetGadgetText gadget_parax,Str$(parax)
    If noupdate<>4 Then SetGadgetText gadget_paray,Str$(paray)
  Forever


  layer\name$=name$
  layer\parax=parax
  layer\paray=paray
  layer\posx =posx
  layer\posy =posy
  layer\layer=parent

  .abort
  EnableGadget window
  FreeGadget button_cancel
  FreeGadget button_ok
  FreeGadget gadget_paray
  FreeGadget gadget_parax
  FreeGadget gadget_posy
  FreeGadget gadget_posx
  FreeGadget gadget_name
  FreeGadget gadget_parent
  FreeGadget label_6
  FreeGadget label_5
  FreeGadget label_4
  FreeGadget label_3
  FreeGadget label_2
  FreeGadget label_1
  FreeGadget subwin
  ActivateWindow window
End Function





;---------------------------------------------------------------------
Function setup_exit()
  Local file

  file=WriteFile(setup_path$+"setup.ini")
  If file<>0 Then
    WriteLine file,"color1="    +Str$(setup_color1)
    WriteLine file,"color2="    +Str$(setup_color2)
    WriteLine file,"color3="    +Str$(setup_color3)
    WriteLine file,"color4="    +Str$(setup_color4)
    WriteLine file,"confirm="   +Str$(setup_confirm)
    WriteLine file,"copyall="   +Str$(setup_copyall)
    WriteLine file,"language="  +setup_language$
    WriteLine file,"minzoom="   +Str$(setup_minzoom)
    WriteLine file,"mouse="     +Str$(setup_mouse)
    WriteLine file,"sysram="    +Str$(setup_sysram)
    WriteLine file,"tilelayout="+Str$(setup_tilelayout)
    WriteLine file,"tileview="  +Str$(setup_tileview)
    WriteLine file,"usedef="    +Str$(setup_usedef)
    WriteLine file,"started=1"
    CloseFile file
  EndIf
End Function





;---------------------------------------------------------------------
Function setup_firststart()
  Local count
  Local dir
  Local file$
  Local i
  Local selected
  Local txt$
  Local window_maxx=ClientWidth(Desktop())
  Local window_maxy=ClientHeight(Desktop())

  Local lang$   =setup_language$
  Local tmplang$=setup_language$

  Local subwin         =CreateWindow  ("Select your language",(window_maxx-230)/2,(window_maxy-100)/2,230,100,0,33)
  Local subwin_width   =ClientWidth   (subwin)
  Local subwin_height  =ClientHeight  (subwin)
  Local label1         =CreateLabel   ("Language:",5,008,70,20,subwin)
  Local gadget_language=CreateComboBox(85,005,140,22,subwin,1)
  Local button_ok      =CreateButton  ("OK",subwin_width/2-90,subwin_height-32,85,22,subwin)
  Local button_cancel  =CreateButton  ("Cancel",subwin_width/2+5,subwin_height-32,85,22,subwin)


  dir=ReadDir(setup_path$)
  Repeat
    file$=Lower$(NextFile$(dir))
    If file$="" Then Exit
    If FileType(setup_path$+file$)=2 Then
      If Left$(file$,5)="lang_" Then AddGadgetItem gadget_language,Mid$(file$,6)
    EndIf
  Forever
  CloseDir dir

  If CountGadgetItems(gadget_language)=0 Then Goto abort
  Goto update


  Repeat
    WaitEvent()

    Select EventID()
      Case $0110 ;ESC-------------------------------------------------
        Exit

      Case $0401 ;gadgetevent-----------------------------------------
        Select EventSource()
          Case button_ok
            Exit
          Case button_cancel
            Goto abort
          Case gadget_language
            selected=SelectedGadgetItem(gadget_language)
            lang$=GadgetItemText$(gadget_language,selected)
        End Select

      Case $0803 ;windowclose-----------------------------------------
        Goto abort
    End Select

    .update
    count=CountGadgetItems(gadget_language)
    For i=0 To count-1
      txt$=GadgetItemText$(gadget_language,i)
      If txt$=lang$ Then
        SelectGadgetItem(gadget_language,i)
        Exit
      EndIf
    Next
  Forever

  setup_language$=lang$

  .abort
  FreeGadget button_cancel
  FreeGadget button_ok
  FreeGadget gadget_language
  FreeGadget label1
  FreeGadget subwin
End Function





;---------------------------------------------------------------------
Function setup_image()
  If layer=Null Then Return
  If layer\code<>layer_image Then Return

  Local bank
  Local count
  Local frames
  Local max
  Local noupdate
  Local selected
  Local tmptile.tile

  Local image=layer\frame ;readonly
  Local mask =layer\mask
  Local mode =layer\mode
  Local name$=layer\name$
  Local parax=layer\parax
  Local paray=layer\paray
  Local posx =layer\posx
  Local posy =layer\posy
  Local start=layer\start
  Local tile.tile=layer\tile

  Local subwin       =CreateWindow   (language$(185),(window_maxx-230)/2,(window_maxy-265)/2,230,265,window,33)
  Local subwin_width =ClientWidth    (subwin)
  Local subwin_height=ClientHeight   (subwin)
  Local label1       =CreateLabel    (language$(75) +":",5,008,70,20,subwin)
  Local label2       =CreateLabel    (language$(78) +":",5,033,70,20,subwin)
  Local label3       =CreateLabel    (language$(168)+":",5,058,70,20,subwin)
  Local label4       =CreateLabel    (language$(169)+":",5,083,70,20,subwin)
  Local label5       =CreateLabel    (language$(186)+":",5,108,70,20,subwin)
  Local label6       =CreateLabel    (language$(187)+":",5,133,70,20,subwin)
  Local label7       =CreateLabel    (language$(183)+":",5,158,70,20,subwin)
  Local label8       =CreateLabel    (language$(184)+":",5,183,70,20,subwin)
  Local gadget_tile  =CreateComboBox (85,005,140,22,subwin)
  Local gadget_mode  =CreateComboBox (85,030,140,22,subwin)
  Local gadget_start =CreateTextField(85,055,140,22,subwin)
  Local gadget_name  =CreateTextField(85,080,140,22,subwin)
  Local gadget_posx  =CreateTextField(85,105,140,22,subwin)
  Local gadget_posy  =CreateTextField(85,130,140,22,subwin)
  Local gadget_parax =CreateTextField(85,155,140,22,subwin)
  Local gadget_paray =CreateTextField(85,180,140,22,subwin)
  Local gadget_mask  =CreateButton   (language$(174),85,205,140,22,subwin,2)
  Local button_ok    =CreateButton   (language$(70),subwin_width/2-90,subwin_height-32,85,22,subwin)
  Local button_cancel=CreateButton   (language$(71),subwin_width/2+5,subwin_height-32,85,22,subwin)

  SetGadgetIconStrip gadget_tile,window_strip3
  SetGadgetIconStrip gadget_mode,window_strip3

  AddGadgetItem gadget_mode,language$(175),0,2
  AddGadgetItem gadget_mode,language$(176),0,3
  AddGadgetItem gadget_mode,language$(177),0,4
  AddGadgetItem gadget_mode,language$(178),0,5

  For tmptile=Each tile
    AddGadgetItem gadget_tile,tmptile\file$,(tmptile=layer\tile),(tmptile\image=0)
  Next

  DisableGadget window
  Goto update


  Repeat
    noupdate=0
    WaitEvent()

    Select EventID()
      Case $0110 ;ESC-------------------------------------------------
        Exit

      Case $0401 ;gadgetevent-----------------------------------------
        Select EventSource()

          Case button_ok
            Exit

          Case button_cancel
            Goto abort

          Case gadget_mode
            mode=SelectedGadgetItem(gadget_mode)

          Case gadget_mask
            mask=ButtonState(gadget_mask)

          Case gadget_name
            name$=Left$(TextFieldText$(gadget_name),12)

          Case gadget_parax
            parax=extra_input(gadget_parax,0,0,200)
            noupdate=3

          Case gadget_paray
            paray=extra_input(gadget_paray,0,0,200)
            noupdate=4

          Case gadget_posx
            posx=extra_input(gadget_posx)
            noupdate=1

          Case gadget_posy
            posy=extra_input(gadget_posy)
            noupdate=2

          Case gadget_start
            If tile<>Null Then max=tile\anims Else max=0
            If image=>1 And image<=max Then
              bank=PeekInt(tile\banka,image*4-4) ;animbank
              frames=PeekShort(bank,0)           ;frames
              start=extra_input(gadget_start,0,1,frames)
              noupdate=5
            EndIf

          Case gadget_tile
            selected=SelectedGadgetItem(gadget_tile)
            count=0
            tile=Null
            For tmptile=Each tile
              If count=selected Then tile=tmptile
              count=count+1
            Next

        End Select

      Case $0803 ;windowclose-----------------------------------------
        Goto abort
    End Select

    .update
    SetGadgetText gadget_name,name$
    SetButtonState gadget_mask,mask

    If tile<>Null Then max=tile\anims Else max=0
    If image=>1 And image<=max Then
      EnableGadget gadget_mode
      SelectGadgetItem gadget_mode,mode
    Else
      mode=0
      start=1
      DisableGadget gadget_mode
      SelectGadgetItem gadget_mode,mode
    EndIf

    If mode=0 Then
      DisableGadget gadget_start
      SetGadgetText gadget_start,""
    Else
      EnableGadget gadget_start
      If noupdate<>5 Then SetGadgetText gadget_start,Str$(start)
    EndIf

    If noupdate<>1 Then SetGadgetText gadget_posx ,Str$(posx)
    If noupdate<>2 Then SetGadgetText gadget_posy ,Str$(posy)
    If noupdate<>3 Then SetGadgetText gadget_parax,Str$(parax)
    If noupdate<>4 Then SetGadgetText gadget_paray,Str$(paray)
  Forever


  layer\mask =mask
  layer\mode =mode
  layer\name$=name$
  layer\parax=parax
  layer\paray=paray
  layer\posx =posx
  layer\posy =posy
  layer\start=start
  layer\tile =tile

  .abort
  EnableGadget window
  FreeGadget button_cancel
  FreeGadget button_ok
  FreeGadget gadget_mask
  FreeGadget gadget_paray
  FreeGadget gadget_parax
  FreeGadget gadget_posy
  FreeGadget gadget_posx
  FreeGadget gadget_name
  FreeGadget gadget_start
  FreeGadget gadget_mode
  FreeGadget gadget_tile
  FreeGadget label8
  FreeGadget label7
  FreeGadget label6
  FreeGadget label5
  FreeGadget label4
  FreeGadget label3
  FreeGadget label2
  FreeGadget label1
  FreeGadget subwin
  ActivateWindow window
End Function





;---------------------------------------------------------------------
Function setup_layer()
  If layer=Null Then Return
  If layer\code<>layer_map And layer\code<>layer_iso1 And layer\code<>layer_iso2 And layer\code<>layer_hex1 And layer\code<>layer_hex2 Then Return

  Local count
  Local gadget_infinity
  Local gadget_mode
  Local infinity
  Local max
  Local mode
  Local noupdate
  Local selected
  Local tmptile.tile
  Local maxwin=315
  If layer\code=layer_map Then maxwin=maxwin+50
  If layer\code=layer_iso1 Then maxwin=maxwin-20

  Local depth1=layer\depth1
  Local depth2=layer\depth2
  Local name$ =layer\name$
  Local parax =layer\parax
  Local paray =layer\paray
  Local posx  =layer\posx
  Local posy  =layer\posy
  Local sizex =layer\sizex
  Local sizey =layer\sizey
  Local tile.tile=layer\tile

  Local subwin       =CreateWindow   (language$(188),(window_maxx-230)/2,(window_maxy-335)/2,250,maxwin,window,33)
  Local subwin_width =ClientWidth    (subwin)
  Local subwin_height=ClientHeight   (subwin)
  Local label1       =CreateLabel    (language$(75) +":",5,008,70,20,subwin)
  Local label2       =CreateLabel    (language$(189)+":",5,033,70,20,subwin)
  Local label3       =CreateLabel    (language$(190)+":",5,058,70,20,subwin)
  Local label4       =CreateLabel    (language$(191)+":",5,083,70,20,subwin)
  Local label5       =CreateLabel    (language$(192)+":",5,108,70,20,subwin)
  Local label6       =CreateLabel    (language$(193)+":",5,133,70,20,subwin)
  Local label7       =CreateLabel    (language$(194)+":",5,158,70,20,subwin)
  Local label8       =CreateLabel    (language$(195)+":",5,183,70,20,subwin)
  Local label9       =CreateLabel    (language$(183)+":",5,208,70,20,subwin)
  Local label10      =CreateLabel    (language$(184)+":",5,233,70,20,subwin)
  Local gadget_tile  =CreateComboBox (85,005,140,22,subwin)
  Local gadget_depth1=CreateComboBox (85,030,140,22,subwin)
  Local gadget_depth2=CreateComboBox (85,055,140,22,subwin)
  Local gadget_name  =CreateTextField(85,080,140,22,subwin)
  Local gadget_posx  =CreateTextField(85,105,140,22,subwin)
  Local gadget_posy  =CreateTextField(85,130,140,22,subwin)
  Local gadget_sizex =CreateTextField(85,155,140,22,subwin)
  Local gadget_sizey =CreateTextField(85,180,140,22,subwin)
  Local gadget_parax =CreateTextField(85,205,140,22,subwin)
  Local gadget_paray =CreateTextField(85,205+25,140,22,subwin)
  Local button_ok    =CreateButton   (language$(70),subwin_width/2-90,subwin_height-52,85,22,subwin)
  Local button_cancel=CreateButton   (language$(71),subwin_width/2+5,subwin_height-52,85,22,subwin)
  Local button_export

  If layer\code=layer_map Then
    gadget_mode=CreateButton(language$(174),85,255,140,22,subwin,2)
    mode=layer\mask
    gadget_infinity=CreateButton(language$(282),85,275,140,22,subwin,2)
    infinity=layer\mode
	button_export=CreateButton   (language$(287),subwin_width/2+5,subwin_height-25,85,22,subwin)
  ElseIf layer\code=layer_iso2 Or layer\code=layer_hex1 Or layer\code=layer_hex2 Then
    gadget_mode=CreateButton(language$(196),85,255,140,22,subwin,2)
    mode=layer\start
  EndIf

  SetGadgetIconStrip gadget_tile,window_strip3

  For tmptile=Each tile
    AddGadgetItem gadget_tile,tmptile\file$,(tmptile=layer\tile),(tmptile\image=0)
  Next

  AddGadgetItem gadget_depth1,"4 bit"
  AddGadgetItem gadget_depth1,"8 bit"
  AddGadgetItem gadget_depth1,"12 bit"
  AddGadgetItem gadget_depth1,"16 bit"
  AddGadgetItem gadget_depth2,language$(197)
  AddGadgetItem gadget_depth2,"1 bit"
  AddGadgetItem gadget_depth2,"2 bit"
  AddGadgetItem gadget_depth2,"4 bit"
  AddGadgetItem gadget_depth2,"8 bit"

  DisableGadget window
  Goto update ;WTF? --- Sorry, this was TheShadow


  Repeat
    noupdate=0
    WaitEvent()

    Select EventID()
      Case $0110 ;ESC-------------------------------------------------
        Exit

      Case $0401 ;gadgetevent-----------------------------------------
        Select EventSource()

          Case button_ok
            Exit

          Case button_cancel
            Goto abort

          Case button_export
            DebugLog("Hier Jan!")
			pfad$ = RequestFile(language$(287), "*", 1)
			If pfad$ <> "" Then 
              export_map(layer,pfad$)
			EndIf

          Case gadget_depth1
            selected=SelectedGadgetItem(gadget_depth1)
            If selected=0 Then depth1=4
            If selected=1 Then depth1=8
            If selected=2 Then depth1=12
            If selected=3 Then depth1=16

          Case gadget_depth2
            selected=SelectedGadgetItem(gadget_depth2)
            If selected=0 Then depth2=0
            If selected=1 Then depth2=1
            If selected=2 Then depth2=2
            If selected=3 Then depth2=4
            If selected=4 Then depth2=8

          Case gadget_infinity
            infinity=ButtonState(gadget_infinity)

          Case gadget_mode
            mode=ButtonState(gadget_mode)

          Case gadget_name
            name$=Left$(TextFieldText$(gadget_name),12)

          Case gadget_parax
            parax=extra_input(gadget_parax,0,0,200)
            noupdate=5

          Case gadget_paray
            paray=extra_input(gadget_paray,0,0,200)
            noupdate=6

          Case gadget_posx
            posx=extra_input(gadget_posx)
            noupdate=1

          Case gadget_posy
            posy=extra_input(gadget_posy)
            noupdate=2

          Case gadget_sizex
            max=layer_maxtiles/sizey
            sizex=extra_input(gadget_sizex,0,1,max)
            noupdate=3

          Case gadget_sizey
            max=layer_maxtiles/sizex
            sizey=extra_input(gadget_sizey,0,1,max)
            noupdate=4

          Case gadget_tile
            selected=SelectedGadgetItem(gadget_tile)
            count=0
            tile=Null
            For tmptile=Each tile
              If count=selected Then tile=tmptile
              count=count+1
            Next

        End Select

      Case $0803 ;windowclose-----------------------------------------
        Goto abort
    End Select

    .update
    If depth1=4  Then SelectGadgetItem gadget_depth1,0
    If depth1=8  Then SelectGadgetItem gadget_depth1,1
    If depth1=12 Then SelectGadgetItem gadget_depth1,2
    If depth1=16 Then SelectGadgetItem gadget_depth1,3

    If depth2=0 Then SelectGadgetItem gadget_depth2,0
    If depth2=1 Then SelectGadgetItem gadget_depth2,1
    If depth2=2 Then SelectGadgetItem gadget_depth2,2
    If depth2=4 Then SelectGadgetItem gadget_depth2,3
    If depth2=8 Then SelectGadgetItem gadget_depth2,4

    SetGadgetText gadget_name,name$
    If gadget_mode<>0 Then SetButtonState gadget_mode,mode
    If gadget_infinity<>0 Then SetButtonState gadget_infinity,infinity

    If noupdate<>1 Then SetGadgetText gadget_posx ,Str$(posx)
    If noupdate<>2 Then SetGadgetText gadget_posy ,Str$(posy)
    If noupdate<>3 Then SetGadgetText gadget_sizex,Str$(sizex)
    If noupdate<>4 Then SetGadgetText gadget_sizey,Str$(sizey)
    If noupdate<>5 Then SetGadgetText gadget_parax,Str$(parax)
    If noupdate<>6 Then SetGadgetText gadget_paray,Str$(paray)
  Forever


  If layer\code=layer_map Then
    layer\mask=mode
    layer\mode=infinity
  ElseIf layer\code=layer_iso2 Or layer\code=layer_hex1 Or layer\code=layer_hex2 Then
    layer\start=mode
  EndIf

  If depth2=0 And layer\depth2<>0 Then ;DATALAYER
    If Confirm(language$(198))=1 Then
      FreeBank layer\bank2
      layer\bank2=0
      layer\depth2=0
    EndIf
  ElseIf depth2<>0 And layer\depth2=0 Then
    If depth2=1 Then layer\bank2=CreateBank((layer\sizex*layer\sizey+7)/8)
    If depth2=2 Then layer\bank2=CreateBank((layer\sizex*layer\sizey+3)/4)
    If depth2=4 Then layer\bank2=CreateBank((layer\sizex*layer\sizey+1)/2)
    If depth2=8 Then layer\bank2=CreateBank(layer\sizex*layer\sizey)
    layer\depth2=depth2
  EndIf

  If layer\tile<>tile And layer\tile<>Null And tile<>Null Then
    If layer\tile\anims<>tile\anims Then
      If Confirm(language$(199))=0 Then tile=layer\tile
    EndIf
  EndIf

  layer\name$=name$
  layer\parax=parax
  layer\paray=paray
  layer\posx =posx
  layer\posy =posy
  layer\tile =tile
  layer_resize(layer,sizex,sizey,depth1,depth2,0,0)

  .abort
  EnableGadget window
  If gadget_mode<>0 Then FreeGadget gadget_mode
  If gadget_infinity<>0 Then FreeGadget gadget_infinity
  FreeGadget button_cancel
  FreeGadget button_ok
  FreeGadget gadget_paray
  FreeGadget gadget_parax
  FreeGadget gadget_sizey
  FreeGadget gadget_sizex
  FreeGadget gadget_posy
  FreeGadget gadget_posx
  FreeGadget gadget_name
  FreeGadget gadget_depth2
  FreeGadget gadget_depth1
  FreeGadget gadget_tile
  FreeGadget label10
  FreeGadget label9
  FreeGadget label8
  FreeGadget label7
  FreeGadget label6
  FreeGadget label5
  FreeGadget label4
  FreeGadget label3
  FreeGadget label2
  FreeGadget label1
  FreeGadget subwin
  ActivateWindow window
End Function





;---------------------------------------------------------------------
Function setup_object()
  If layer=Null Then Return
  If layer\code<>layer_point And layer\code<>layer_line And layer\code<>layer_rect And layer\code<>layer_oval Then Return

  Local noupdate
  Local code =layer\code
  Local name$=layer\name$
  Local parax=layer\parax
  Local paray=layer\paray
  Local posx =layer\posx
  Local posy =layer\posy
  Local sizex=layer\sizex
  Local sizey=layer\sizey

  Local subwin       =CreateWindow   (language$(200),(window_maxx-230)/2,(window_maxy-245)/2,230,245,window,33)
  Local subwin_width =ClientWidth    (subwin)
  Local subwin_height=ClientHeight   (subwin)
  Local label1       =CreateLabel    (language$(201)+":",5,008,70,20,subwin)
  Local label2       =CreateLabel    (language$(180)+":",5,033,70,20,subwin)
  Local label3       =CreateLabel    (language$(181)+":",5,058,70,20,subwin)
  Local label4       =CreateLabel    (language$(182)+":",5,083,70,20,subwin)
  Local label5       =CreateLabel    (language$(202)+":",5,108,70,20,subwin)
  Local label6       =CreateLabel    (language$(203)+":",5,133,70,20,subwin)
  Local label7       =CreateLabel    (language$(183)+":",5,158,70,20,subwin)
  Local label8       =CreateLabel    (language$(184)+":",5,183,70,20,subwin)
  Local gadget_code  =CreateComboBox (85,005,140,22,subwin)
  Local gadget_name  =CreateTextField(85,030,140,22,subwin)
  Local gadget_posx  =CreateTextField(85,055,140,22,subwin)
  Local gadget_posy  =CreateTextField(85,080,140,22,subwin)
  Local gadget_sizex =CreateTextField(85,105,140,22,subwin)
  Local gadget_sizey =CreateTextField(85,130,140,22,subwin)
  Local gadget_parax =CreateTextField(85,155,140,22,subwin)
  Local gadget_paray =CreateTextField(85,180,140,22,subwin)
  Local button_ok    =CreateButton   (language$(70),subwin_width/2-90,subwin_height-32,85,22,subwin)
  Local button_cancel=CreateButton   (language$(71),subwin_width/2+5,subwin_height-32,85,22,subwin)

  SetGadgetIconStrip gadget_code,window_strip2

  AddGadgetItem gadget_code,language$(49),0,layer_point
  AddGadgetItem gadget_code,language$(50),0,layer_line
  AddGadgetItem gadget_code,language$(51),0,layer_rect
  AddGadgetItem gadget_code,language$(52),0,layer_oval

  DisableGadget window
  Goto update


  Repeat
    noupdate=0
    WaitEvent()

    Select EventID()
      Case $0110 ;ESC-------------------------------------------------
        Exit

      Case $0401 ;gadgetevent-----------------------------------------
        Select EventSource()

          Case button_ok
            Exit

          Case button_cancel
            Goto abort

          Case gadget_code
            code=SelectedGadgetItem(gadget_code)+layer_point
            If code=layer_point Then
              sizex=0
              sizey=0
            EndIf
            If code<>layer_line Then
              sizex=Abs(sizex)
              sizey=Abs(sizey)
            EndIf

          Case gadget_name
            name$=Left$(TextFieldText$(gadget_name),12)

          Case gadget_parax
            parax=extra_input(gadget_parax,0,0,200)
            noupdate=5

          Case gadget_paray
            paray=extra_input(gadget_paray,0,0,200)
            noupdate=6

          Case gadget_posx
            posx=extra_input(gadget_posx)
            noupdate=1

          Case gadget_posy
            posy=extra_input(gadget_posy)
            noupdate=2

          Case gadget_sizex
            If code= layer_line Then sizex=extra_input(gadget_sizex)
            If code<>layer_line Then sizex=extra_input(gadget_sizex,0,0)
            noupdate=3

          Case gadget_sizey
            If code= layer_line Then sizey=extra_input(gadget_sizey)
            If code<>layer_line Then sizey=extra_input(gadget_sizey,0,0)
            noupdate=4

        End Select

      Case $0803 ;windowclose-----------------------------------------
        Goto abort
    End Select

    .update
    SelectGadgetItem gadget_code,code-layer_point
    SetGadgetText gadget_name,name$

    If noupdate<>1 Then SetGadgetText gadget_posx ,Str$(posx)
    If noupdate<>2 Then SetGadgetText gadget_posy ,Str$(posy)
    If noupdate<>3 Then SetGadgetText gadget_sizex,Str$(sizex)
    If noupdate<>4 Then SetGadgetText gadget_sizey,Str$(sizey)
    If noupdate<>5 Then SetGadgetText gadget_parax,Str$(parax)
    If noupdate<>6 Then SetGadgetText gadget_paray,Str$(paray)

    If code=layer_point Then
      DisableGadget gadget_sizex
      DisableGadget gadget_sizey
      SetGadgetText gadget_sizex,""
      SetGadgetText gadget_sizey,""
    Else
      EnableGadget gadget_sizex
      EnableGadget gadget_sizey
    EndIf

    If code=layer_oval Then
      SetGadgetText label5,language$(204)+":"
      SetGadgetText label6,language$(205)+":"
    ElseIf code=layer_line Then
      SetGadgetText label5,language$(206)+":"
      SetGadgetText label6,language$(207)+":"
    Else
      SetGadgetText label5,language$(202)+":"
      SetGadgetText label6,language$(203)+":"
    EndIf
  Forever


  layer\code =code
  layer\name$=name$
  layer\parax=parax
  layer\paray=paray
  layer\posx =posx
  layer\posy =posy
  layer\sizex=sizex
  layer\sizey=sizey

  .abort
  EnableGadget window
  FreeGadget button_cancel
  FreeGadget button_ok
  FreeGadget gadget_paray
  FreeGadget gadget_parax
  FreeGadget gadget_sizey
  FreeGadget gadget_sizex
  FreeGadget gadget_posy
  FreeGadget gadget_posx
  FreeGadget gadget_name
  FreeGadget gadget_code
  FreeGadget label8
  FreeGadget label7
  FreeGadget label6
  FreeGadget label5
  FreeGadget label4
  FreeGadget label3
  FreeGadget label2
  FreeGadget label1
  FreeGadget subwin
  ActivateWindow window
End Function





;---------------------------------------------------------------------
;EINSTELLUNGEN
Function setup_prog()
  Local b
  Local count
  Local dir
  Local file$
  Local g
  Local i
  Local r
  Local selected
  Local txt$

  Local color1   =setup_color1
  Local color2   =setup_color2
  Local color3   =setup_color3
  Local color4   =setup_color4
  Local conf     =setup_confirm
  Local copyall  =setup_copyall
  Local lang$    =setup_language$
  Local minzoom  =setup_minzoom
  Local mouse    =setup_mouse
  Local sysram   =setup_sysram
  Local usedef   =setup_usedef
  Local tmpsysram=setup_sysram
  Local tmplang$ =setup_language$
  Local tilezoom =setup_tilezoom

  Local subwin         =CreateWindow  (language$(31),(window_maxx-230)/2,(window_maxy-315)/2,230,335,window,33)
  Local subwin_width   =ClientWidth   (subwin)
  Local subwin_height  =ClientHeight  (subwin)
  Local label1         =CreateLabel   (language$(208)+":",5,008,70,20,subwin)
  Local label2         =CreateLabel   (language$(209)+":",5,033,70,20,subwin)
  Local label3         =CreateLabel   (language$(210)+":",5,058,70,20,subwin)
  Local label4         =CreateLabel   (language$(211)+":",5,083,70,20,subwin)
  Local label5         =CreateLabel   (language$(212)+":",5,108,70,20,subwin)
  Local label6         =CreateLabel   (language$(274)+":",5,133,70,20,subwin)
  Local label7         =CreateLabel   (language$(259),25,163,200,20,subwin)
  Local label8         =CreateLabel   (language$(262),25,183,200,20,subwin)
  Local label9         =CreateLabel   (language$(263),25,203,200,20,subwin)
  Local label10        =CreateLabel   (language$(273),25,223,200,20,subwin)
  Local label11        =CreateLabel   (language$(213),25,243,200,35,subwin)
  Local label12        =CreateLabel   (language$(285),25,273,200,35,subwin)


  Local gadget_language=CreateComboBox (85,005,140,22,subwin,1)
  Local gadget_color1  =CreatePanel    (85,030,140,22,subwin,1)
  Local gadget_color2  =CreatePanel    (85,055,140,22,subwin,1)
  Local gadget_color3  =CreatePanel    (85,080,140,22,subwin,1)
  Local gadget_color4  =CreatePanel    (85,105,140,22,subwin,1)
  Local gadget_minzoom =CreateTextField(85,130,140,22,subwin)
  Local gadget_confirm =CreateButton   ("",5,160,20,20,subwin,2)
  Local gadget_mouse   =CreateButton   ("",5,180,20,20,subwin,2)
  Local gadget_sysram  =CreateButton   ("",5,200,20,20,subwin,2)
  Local gadget_usedef  =CreateButton   ("",5,220,20,20,subwin,2)
  Local gadget_copyall =CreateButton   ("",5,240,20,20,subwin,2)
  Local gadget_tilezoom=CreateButton   ("",5,270,20,20,subwin,2)
  Local button_ok      =CreateButton   (language$(70),subwin_width/2-90,subwin_height-32,85,22,subwin)
  Local button_cancel  =CreateButton   (language$(71),subwin_width/2+5,subwin_height-32,85,22,subwin)


  dir=ReadDir(setup_path$)
  Repeat
    file$=Lower$(NextFile$(dir))
    If file$="" Then Exit
    If FileType(setup_path$+file$)=2 Then
      If Left$(file$,5)="lang_" Then AddGadgetItem gadget_language,Mid$(file$,6)
    EndIf
  Forever
  CloseDir dir

  If CountGadgetItems(gadget_language)=0 Then Goto abort
  DisableGadget window
  SetGadgetText gadget_minzoom,Str$(minzoom)
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
        ElseIf EventSource()=gadget_color3 Then
          r=(color3 And $FF0000)/$10000
          g=(color3 And $FF00)/$100
          b=(color3 And $FF)
          If RequestColor(r,g,b)=1 Then
            r=RequestedRed()
            g=RequestedGreen()
            b=RequestedBlue()
            color3=r*$10000+g*$100+b
          EndIf
        ElseIf EventSource()=gadget_color4 Then
          r=(color4 And $FF0000)/$10000
          g=(color4 And $FF00)/$100
          b=(color4 And $FF)
          If RequestColor(r,g,b)=1 Then
            r=RequestedRed()
            g=RequestedGreen()
            b=RequestedBlue()
            color4=r*$10000+g*$100+b
          EndIf
        EndIf

      Case $0401 ;gadgetevent-----------------------------------------
        Select EventSource()
          Case button_ok
            Exit
          Case button_cancel
            Goto abort
          Case gadget_confirm
            conf=ButtonState(gadget_confirm)
          Case gadget_copyall
            copyall=ButtonState(gadget_copyall)
          Case gadget_language
            selected=SelectedGadgetItem(gadget_language)
            lang$=GadgetItemText$(gadget_language,selected)
          Case gadget_minzoom
            minzoom=extra_input(gadget_minzoom,0,0,64)
          Case gadget_mouse
            mouse=ButtonState(gadget_mouse)
          Case gadget_sysram
            sysram=ButtonState(gadget_sysram)
          Case gadget_usedef
            usedef=ButtonState(gadget_usedef)
          Case gadget_tilezoom
            tilezoom=ButtonState(gadget_tilezoom)
        End Select

      Case $0803 ;windowclose-----------------------------------------
        Goto abort
    End Select

    .update
    count=CountGadgetItems(gadget_language)
    For i=0 To count-1
      txt$=GadgetItemText$(gadget_language,i)
      If txt$=lang$ Then
        SelectGadgetItem(gadget_language,i)
        Exit
      EndIf
    Next

    r=(color1 And $FF0000)/$10000
    g=(color1 And $FF00)/$100
    b=(color1 And $FF)
    SetPanelColor gadget_color1,r,g,b

    r=(color2 And $FF0000)/$10000
    g=(color2 And $FF00)/$100
    b=(color2 And $FF)
    SetPanelColor gadget_color2,r,g,b

    r=(color3 And $FF0000)/$10000
    g=(color3 And $FF00)/$100
    b=(color3 And $FF)
    SetPanelColor gadget_color3,r,g,b

    r=(color4 And $FF0000)/$10000
    g=(color4 And $FF00)/$100
    b=(color4 And $FF)
    SetPanelColor gadget_color4,r,g,b

    SetButtonState gadget_confirm,conf
    SetButtonState gadget_mouse  ,mouse
    SetButtonState gadget_sysram ,sysram
    SetButtonState gadget_usedef,usedef
    SetButtonState gadget_copyall,copyall
    SetButtonState gadget_tilezoom,tilezoom
  Forever


  setup_color1   =color1
  setup_color2   =color2
  setup_color3   =color3
  setup_color4   =color4
  setup_confirm  =conf
  setup_copyall  =copyall
  setup_language$=lang$
  setup_minzoom  =minzoom
  setup_mouse    =mouse
  setup_sysram   =sysram
  setup_usedef   =usedef
  setup_tilezoom =tilezoom

  maker_update()
  menu_update()
  editor_update(2)

  If tmplang$<>lang$ Or tmpsysram<>sysram Then Notify "Please quit and start this program again"+Chr$(13)+Chr$(13)+"Bitte dieses Programm beenden und neustarten"

  .abort
  EnableGadget window
  FreeGadget button_cancel
  FreeGadget button_ok
  FreeGadget gadget_copyall
  FreeGadget gadget_usedef
  FreeGadget gadget_sysram
  FreeGadget gadget_mouse
  FreeGadget gadget_confirm
  FreeGadget gadget_minzoom
  FreeGadget gadget_color4
  FreeGadget gadget_color3
  FreeGadget gadget_color2
  FreeGadget gadget_color1
  FreeGadget gadget_language
  FreeGadget gadget_tilezoom
  FreeGadget label11
  FreeGadget label10
  FreeGadget label9
  FreeGadget label8
  FreeGadget label7
  FreeGadget label6
  FreeGadget label5
  FreeGadget label4
  FreeGadget label3
  FreeGadget label2
  FreeGadget label1
  FreeGadget subwin
  ActivateWindow window
End Function





;---------------------------------------------------------------------
Function setup_project()
  If layer=Null Then Return
  If layer\code<>layer_back Then Return

  Local b
  Local g
  Local name$
  Local r
  Local noupdate

  Local passw$=map_passw$
  Local colour=layer_colour
  Local file$ =layer_file$
  Local mode  =layer\mode
  Local parax =layer\parax
  Local paray =layer\paray
  Local posx  =layer\posx
  Local posy  =layer\posy
  Local sizex =layer\sizex
  Local sizey =layer\sizey

  Local subwin       =CreateWindow   (language$(214),(window_maxx-230)/2,(window_maxy-295)/2,230,295,window,33)
  Local subwin_width =ClientWidth    (subwin)
  Local subwin_height=ClientHeight   (subwin)
  Local label1       =CreateLabel    (language$(40) +":",5,008,70,20,subwin)
  Local label2       =CreateLabel    (language$(215)+":",5,033,70,20,subwin)
  Local label3       =CreateLabel    (language$(216)+":",5,058,70,20,subwin)
  Local label4       =CreateLabel    (language$(217)+":",5,083,70,20,subwin)
  Local label5       =CreateLabel    (language$(218)+":",5,108,70,20,subwin)
  Local label6       =CreateLabel    (language$(183)+":",5,133,70,20,subwin)
  Local label7       =CreateLabel    (language$(184)+":",5,158,70,20,subwin)
  Local label8       =CreateLabel    (language$(219)+":",5,183,70,20,subwin)
  Local label9       =CreateLabel    (language$(220)+":",5,208,70,20,subwin)
  Local label10      =CreateLabel    (language$(82) +":",5,233,70,20,subwin)

  Local gadget_mode  =CreateComboBox (85,005,140,22,subwin)
  Local gadget_colour=CreatePanel    (85,030,140,22,subwin,1)
  Local gadget_file  =CreateButton   ("" ,085,55,118,22,subwin)
  Local gadget_delete=CreateButton   ("<",203,55,022,22,subwin)
  Local gadget_posx  =CreateTextField(85,080,140,22,subwin)
  Local gadget_posy  =CreateTextField(85,105,140,22,subwin)
  Local gadget_parax =CreateTextField(85,130,140,22,subwin)
  Local gadget_paray =CreateTextField(85,155,140,22,subwin)
  Local gadget_sizex =CreateTextField(85,180,140,22,subwin)
  Local gadget_sizey =CreateTextField(85,205,140,22,subwin)
  Local gadget_passw =CreateTextField(85,230,140,22,subwin)
  Local button_ok    =CreateButton   (language$(70),subwin_width/2-90,subwin_height-32,85,22,subwin)
  Local button_cancel=CreateButton   (language$(71),subwin_width/2+5,subwin_height-32,85,22,subwin)

  AddGadgetItem gadget_mode,language$(197)
  AddGadgetItem gadget_mode,language$(215)
  AddGadgetItem gadget_mode,language$(47)
  AddGadgetItem gadget_mode,language$(221)

  If demo=1 Then DisableGadget gadget_passw
  DisableGadget window
  Goto update


  Repeat
    noupdate=0
    WaitEvent()

    Select EventID()
      Case $0110 ;ESC-------------------------------------------------
        Exit

      Case $0201 ;mousedown-------------------------------------------
        If EventSource()=gadget_colour Then
          r=(colour And $FF0000)/$10000
          g=(colour And $FF00)/$100
          b=(colour And $FF)
          If RequestColor(r,g,b)=1 Then
            r=RequestedRed()
            g=RequestedGreen()
            b=RequestedBlue()
            colour=r*$10000+g*$100+b
            mode=1
          EndIf
        EndIf

      Case $0401 ;gadgetevent-----------------------------------------
        Select EventSource()

          Case button_ok
            Exit

          Case button_cancel
            Goto abort

          Case gadget_delete
            file$=""
            If mode=2 Or mode=3 Then mode=0

          Case gadget_file
            name$=extra_request$(map_path$)
            If name$<>"" Then
              file$=name$
              If mode<>2 And mode<>3 Then mode=2
            EndIf

          Case gadget_mode
            mode=SelectedGadgetItem(gadget_mode)
            If (mode=2 Or mode=3) And file$="" Then
              name$=extra_request$(map_path$)
              If name$<>"" Then file$=name$ Else mode=0
            EndIf

          Case gadget_parax
            parax=extra_input(gadget_parax,0,0,200)
            noupdate=5

          Case gadget_paray
            paray=extra_input(gadget_paray,0,0,200)
            noupdate=6

          Case gadget_passw
            passw$=Left$(TextFieldText$(gadget_passw),16)

          Case gadget_posx
            posx=extra_input(gadget_posx)
            noupdate=1

          Case gadget_posy
            posy=extra_input(gadget_posy)
            noupdate=2

          Case gadget_sizex
            sizex=extra_input(gadget_sizex,0,0,2147483647)
            noupdate=3

          Case gadget_sizey
            sizey=extra_input(gadget_sizey,0,0,2147483647)
            noupdate=4

        End Select

      Case $0803 ;windowclose-----------------------------------------
        Goto abort
    End Select

    .update
    r=(colour And $FF0000)/$10000
    g=(colour And $FF00)/$100
    b=(colour And $FF)
    SetPanelColor gadget_colour,r,g,b
    SelectGadgetItem gadget_mode,mode
    SetGadgetText gadget_file,file$
    SetGadgetText gadget_passw,passw$

    If noupdate<>1 Then SetGadgetText gadget_posx ,Str$(posx)
    If noupdate<>2 Then SetGadgetText gadget_posy ,Str$(posy)
    If noupdate<>3 Then SetGadgetText gadget_sizex,Str$(sizex)
    If noupdate<>4 Then SetGadgetText gadget_sizey,Str$(sizey)
    If noupdate<>5 Then SetGadgetText gadget_parax,Str$(parax)
    If noupdate<>6 Then SetGadgetText gadget_paray,Str$(paray)

    If mode=0 Or mode=1 Then
      DisableGadget gadget_posx
      DisableGadget gadget_posy
      DisableGadget gadget_parax
      DisableGadget gadget_paray
      SetGadgetText gadget_posx,""
      SetGadgetText gadget_posy,""
      SetGadgetText gadget_parax,""
      SetGadgetText gadget_paray,""
    Else
      EnableGadget gadget_posx
      EnableGadget gadget_posy
      EnableGadget gadget_parax
      EnableGadget gadget_paray
    EndIf
  Forever


  If mode=0 Or mode=1 Then file$=""
  If layer_handle<>0 Then FreeImage layer_handle : layer_handle=0
  If file$<>"" Then layer_handle=LoadImage(map_path$+file$,vram)

  map_passw$  =passw$
  layer_colour=colour
  layer_file$ =file$
  layer\mode  =mode
  layer\parax =parax
  layer\paray =paray
  layer\posx  =posx
  layer\posy  =posy
  layer\sizex =sizex
  layer\sizey =sizey

  .abort
  EnableGadget window
  FreeGadget button_cancel
  FreeGadget button_ok
  FreeGadget gadget_passw
  FreeGadget gadget_sizey
  FreeGadget gadget_sizex
  FreeGadget gadget_paray
  FreeGadget gadget_parax
  FreeGadget gadget_posy
  FreeGadget gadget_posx
  FreeGadget gadget_delete
  FreeGadget gadget_file
  FreeGadget gadget_colour
  FreeGadget gadget_mode
  FreeGadget label_10
  FreeGadget label_9
  FreeGadget label_8
  FreeGadget label_7
  FreeGadget label_6
  FreeGadget label_5
  FreeGadget label_4
  FreeGadget label_3
  FreeGadget label_2
  FreeGadget label_1
  FreeGadget subwin
  ActivateWindow window
End Function





;---------------------------------------------------------------------
Function setup_size()
  If layer=Null Then Return
  If layer\code<>layer_map And layer\code<>layer_iso1 And layer\code<>layer_iso2 And layer\code<>layer_hex1 And layer\code<>layer_hex2 Then Return

  Local max
  Local min
  Local noupdate
  Local sizex
  Local sizey
  Local xxx

  Local val_bottom
  Local val_left
  Local val_right
  Local val_top

  Local gadget_bottom
  Local gadget_left
  Local gadget_right
  Local gadget_top

  Local subwin       =CreateWindow(language$(27),(window_maxx-220)/2,(window_maxy-230)/2,220,230,window,33)
  Local subwin_width =ClientWidth (subwin)
  Local subwin_height=ClientHeight(subwin)
  Local label        =CreateLabel (language$(222),5,125,subwin_width-20,40,subwin)
  Local button_cut   =CreateButton(language$(223),subwin_width/2-90,subwin_height-64,180,22,subwin)
  Local button_ok    =CreateButton(language$(70),subwin_width/2-90,subwin_height-32,85,22,subwin)
  Local button_cancel=CreateButton(language$(71),subwin_width/2+5,subwin_height-32,85,22,subwin)

  If layer\code=layer_map Or layer\code=layer_iso2 Or layer\code=layer_hex1 Or layer\code=layer_hex2 Then
    gadget_top   =CreateTextField(080,10,60,22,subwin)
    gadget_left  =CreateTextField(010,50,60,22,subwin)
    gadget_right =CreateTextField(150,50,60,22,subwin)
    gadget_bottom=CreateTextField(080,90,60,22,subwin)
  ElseIf layer\code=layer_iso1 Then
    gadget_top   =CreateTextField(150,10,60,22,subwin)
    gadget_left  =CreateTextField(010,10,60,22,subwin)
    gadget_right =CreateTextField(150,90,60,22,subwin)
    gadget_bottom=CreateTextField(010,90,60,22,subwin)
  EndIf

  If editor_layer=Null Then DisableGadget button_cut
  DisableGadget window
  Goto update


  Repeat
    noupdate=0
    WaitEvent()

    Select EventID()
      Case $0110 ;ESC-------------------------------------------------
        Exit

      Case $0401 ;gadgetevent-----------------------------------------
        Select EventSource()

          Case button_cancel
            Goto abort

          Case button_cut
            sizex=editor_x2-editor_x1+1
            sizey=editor_y2-editor_y1+1
            layer_resize(layer,sizex,sizey,layer\depth1,layer\depth2,-editor_x1,-editor_y1)
            Goto abort

          Case button_ok
            Exit

          Case gadget_bottom
            xxx=layer_maxtiles/(layer\sizex+val_left+val_right)
            min=-layer\sizey-val_top+1
            max=xxx-layer\sizey-val_top
            val_bottom=extra_input(gadget_bottom,0,min,max)
            noupdate=1

          Case gadget_left
            xxx=layer_maxtiles/(layer\sizey+val_top+val_bottom)
            min=-layer\sizex-val_right+1
            max=xxx-layer\sizex-val_right
            val_left=extra_input(gadget_left,0,min,max)
            noupdate=2

          Case gadget_right
            xxx=layer_maxtiles/(layer\sizey+val_top+val_bottom)
            min=-layer\sizex-val_left+1
            max=xxx-layer\sizex-val_left
            val_right=extra_input(gadget_right,0,min,max)
            noupdate=3

          Case gadget_top
            xxx=layer_maxtiles/(layer\sizex+val_left+val_right)
            min=-layer\sizey-val_bottom+1
            max=xxx-layer\sizey-val_bottom
            val_top=extra_input(gadget_top,0,min,max)
            noupdate=4

        End Select

      Case $0803 ;windowclose-----------------------------------------
        Goto abort
    End Select

    .update
    If noupdate<>1 Then SetGadgetText gadget_bottom,Str$(val_bottom)
    If noupdate<>2 Then SetGadgetText gadget_left  ,Str$(val_left)
    If noupdate<>3 Then SetGadgetText gadget_right ,Str$(val_right)
    If noupdate<>4 Then SetGadgetText gadget_top   ,Str$(val_top)
  Forever


  sizex=layer\sizex+val_left+val_right
  sizey=layer\sizey+val_top+val_bottom
  layer_resize(layer,sizex,sizey,layer\depth1,layer\depth2,val_left,val_top)

  .abort
  EnableGadget window
  FreeGadget gadget_bottom
  FreeGadget gadget_right
  FreeGadget gadget_left
  FreeGadget gadget_top
  FreeGadget button_cancel
  FreeGadget button_ok
  FreeGadget button_cut
  FreeGadget label
  FreeGadget subwin
  ActivateWindow window
  editor_update()
End Function





;---------------------------------------------------------------------
Function setup_start()
  Local file
  Local num
  Local pos
  Local txt1$
  Local txt2$
  Local x$

  setup_path$=SystemProperty$("APPDIR")
  If FileType(setup_path$+"setup.ini")=0 Then setup_path$=CurrentDir$()

  If FileType(setup_path$+"setup.ini")=0 Then
    Notify "Program files not found"+Chr$(13)+Chr$(13)+"Programmdateien nicht gefunden"
    End
  EndIf

  file=ReadFile(setup_path$+"setup.ini")
  If file<>0 Then
    While Not Eof(file)
      x$=ReadLine$(file)
      pos=Instr(x$,"=")
      txt1$=Lower$(Left$(x$,pos-1))
      txt2$=Lower$(Mid$(x$,pos+1))
      If txt1$="color1"     Then setup_color1    =Int(txt2$)
      If txt1$="color2"     Then setup_color2    =Int(txt2$)
      If txt1$="color3"     Then setup_color3    =Int(txt2$)
      If txt1$="color4"     Then setup_color4    =Int(txt2$)
      If txt1$="confirm"    Then setup_confirm   =Int(txt2$)
      If txt1$="copyall"    Then setup_copyall   =Int(txt2$)
      If txt1$="language"   Then setup_language$ =txt2$
      If txt1$="minzoom"    Then setup_minzoom   =Int(txt2$)
      If txt1$="mouse"      Then setup_mouse     =Int(txt2$)
      If txt1$="sysram"     Then setup_sysram    =Int(txt2$)
      If txt1$="tilelayout" Then setup_tilelayout=Int(txt2$)
      If txt1$="tileview"   Then setup_tileview  =Int(txt2$)
      If txt1$="usedef"     Then setup_usedef    =Int(txt2$)
      If txt1$="started"    Then setup_started   =Int(txt2$)
      If txt1$="tilezoom"   Then setup_tilezoom  =Int(txt2$)
    Wend
    CloseFile file
  EndIf

  If setup_started=0 Then setup_firststart()

  file=ReadFile(setup_path$+"lang_"+setup_language$+"\language.ini")
  If file=0 Then End

  While Not Eof(file)
    x$=ReadLine$(file)
    If Trim$(x$)<>"" Then
      pos=Instr(x$,"=")
      num=Int(Left$(x$,pos-1))
      txt2$=Replace$(Mid$(x$,pos+1),Chr$(9),Chr$(13))
      txt2$=Trim$(txt2$)
      If num=>0 And num<=300 And pos>0 Then language$(num)=txt2$
    EndIf
  Wend

  If setup_sysram=1 Then vram=4

  CloseFile file
End Function