Global extra_canvas





;---------------------------------------------------------------------
Function extra_export()
  If FileType(setup_path$+"export.exe")=0 Then
    Notify language$(264)
    Return
  EndIf

  If map_file$="" Then
    Notify language$(265)
    Return
  EndIf

  map_save2(map_path$+map_file$)
  ExecFile(Chr$(34)+setup_path$+"export.exe"+Chr$(34)+" "+map_path$+map_file$+":"+map_passw$)
End Function





;---------------------------------------------------------------------
Function extra_help()
  If ExecFile(Chr$(34)+setup_path$+"lang_"+setup_language$+"\index.htm"+Chr$(34))=0 Then
    If ExecFile("iexplore.exe "+Chr$(34)+setup_path$+"lang_"+setup_language$+"\index.htm"+Chr$(34))=0 Then
      Notify language$(260)+" "+setup_path$+"lang_"+setup_language$+"\index.htm"
    EndIf
  EndIf
End Function





;---------------------------------------------------------------------
;gadget: gadget handle
;mode:   0=normal, 1=strong
;min:    min value
;max:    max value
;RETURN: value
;---------------------------------------------------------------------
Function extra_input(gadget,mode=0,min=-2147483648,max=2147483647)
  Local char$
  Local i
  Local neg
  Local txt1$
  Local txt2$
  Local value

  txt1$=TextFieldText$(gadget)
  For i=1 To Len(txt1$)
    char$=Mid$(txt1$,i,1)
    If (char$=>"0" And char$<="9") Or (char$="-" And min<0 And i=1) Then txt2$=txt2$+char$
  Next

  .again
  If Left$(txt2$,2)="00" Then txt2$=Mid$(txt2$,2) : Goto again
  If Left$(txt2$,2)="-0" Then txt2$="-"+Mid$(txt2$,3) : Goto again
  If Left$(txt2$,1)="0" And txt2$<>"0" Then txt2$=Mid$(txt2$,2)
  If Left$(txt2$,1)<>"-" And max<0 Then txt2$="-"+txt2$
  If Left$(txt2$,1)="-" Then neg=1

  If txt2$<>"" And txt2$<>"-" Then
    value=Int(txt2$)
    If neg=0 And txt2$<>Str$(value) Then txt2$="2147483647"
    If neg=1 And txt2$<>Str$(value) Then txt2$="-2147483648"
  EndIf

  value=Int(txt2$)
  If value<min And (min<=0 Or mode=1) Then txt2$=Str$(min)
  value=Int(txt2$)
  If value>max And (max=>0 Or mode=1) Then txt2$=Str$(max)
  If mode=1 Then txt2$=Str$(Int(txt2$))

  value=Int(txt2$)
  If value<min Then value=min
  If value>max Then value=max
  If txt2$="0" And value>0 Then txt2$=Str$(value)
  If txt2$="0" And value<0 Then txt2$=Str$(value)
  SetGadgetText gadget,txt2$

  Return value
End Function





;---------------------------------------------------------------------
;layer:  layer handle
;RETURN: layer number
;---------------------------------------------------------------------
Function extra_layer2num(layer.layer)
  Local count
  Local tmp.layer

  For tmp=Each layer
    If tmp\code=layer_map Or tmp\code=layer_iso1 Or tmp\code=layer_iso2 Or tmp\code=layer_hex1 Or tmp\code=layer_hex2 Or tmp\code=layer_clone Then
      count=count+1
      If tmp=layer Then Return count
    EndIf
  Next
End Function





;---------------------------------------------------------------------
;nr:     layer number
;RETURN: layer handle
;---------------------------------------------------------------------
Function extra_num2layer.layer(nr)
  Local count
  Local layer.layer

  For layer=Each layer
    If layer\code=layer_map Or layer\code=layer_iso1 Or layer\code=layer_iso2 Or layer\code=layer_hex1 Or layer\code=layer_hex2 Or layer\code=layer_clone Then
      count=count+1
      If count=nr Then Return layer
    EndIf
  Next
End Function





;---------------------------------------------------------------------
;nr:     tile number
;RETURN: tileset handle
;---------------------------------------------------------------------
Function extra_num2tile.tile(nr)
  Local count
  Local tile.tile

  For tile=Each tile
    count=count+1
    If count=nr Then Return tile
  Next
End Function





;---------------------------------------------------------------------
;RETURN: password string
;---------------------------------------------------------------------
Function extra_passw$()
  Local subwin   =CreateWindow   (language$(82),(window_maxx-400)/2,(window_maxy-100)/2,400,100,window,33)
  Local label    =CreateLabel    (language$(83),5,5,390,20,subwin)
  Local textfield=CreateTextField(5,36,390,22,subwin)
  Local button   =CreateButton   (language$(70),160,68,80,22,subwin)
  Local passw$

  DisableGadget window
  ActivateGadget textfield

  Repeat
    WaitEvent()
    Select EventID()
      Case $0110 ;ESC
        Return
      Case $0401 ;gadgetevent
        If EventSource()=textfield And EventData()=13 Then Exit
        If EventSource()=button Then Exit
      Case $0803 ;windowclose
        Return
    End Select
  Forever

  passw$=TextFieldText$(textfield)
  EnableGadget window
  FreeGadget button
  FreeGadget textfield
  FreeGadget label
  FreeGadget subwin
  ActivateWindow window

  Return passw$
End Function





;---------------------------------------------------------------------
Function extra_preview()
  If FileType(setup_path$+"preview.exe")=0 Then Return

  If map_file$<>"" Then map_save2(map_path$+map_file$)
  If map_file$<>"" Then ExecFile(Chr$(34)+setup_path$+"preview.exe"+Chr$(34)+" "+map_path$+map_file$+":"+map_passw$)
  If map_file$= "" Then ExecFile(Chr$(34)+setup_path$+"preview.exe"+Chr$(34))
End Function





;---------------------------------------------------------------------
;file:   input file handle
;max:    max string length
;RETURN: text string
;---------------------------------------------------------------------
Function extra_readstr$(file,max)
  Local char
  Local i
  Local noadd
  Local txt$

  For i=1 To max
    char=ReadByte(file)
    If char=0 Then noadd=1
    If noadd=0 Then txt$=txt$+Chr$(char)
  Next
  Return txt$
End Function





;---------------------------------------------------------------------
;path:   map-file-path
;RETURN: filename
;---------------------------------------------------------------------
Function extra_request$(path$)
  Local i
  Local name1$
  Local name2$

  ChangeDir path$
  name1$=RequestFile(language$(84),"bmp;*.jpg;*.png",0)
  ChangeDir map_app$

  If name1$="" Then Return

  name2$=name1$
  For i=Len(name1$) To 1 Step -1
    If Mid$(name1$,i,1)="\" Or Mid$(name1$,i,1)="/" Then
      name2$=Mid$(name1$,i+1)
      Exit
    EndIf
  Next

  If Len(name2$)>12 Then
    Notify language$(85)
    Return
  EndIf

  If FileType(path$+name2$)<>1 Then
    Notify language$(86)
    Return
  EndIf

  Return name2$
End Function





;---------------------------------------------------------------------
Function extra_splash()
  Local subwin  =CreateWindow  (language$(0),(window_maxx-500)/2,(window_maxy-400)/2,500,400,window,33)
  ;Local htmlview=CreateHtmlView(5,5,490,353,subwin,1)
  Local label   =CreateLabel("Das ist eine"+Chr$(13)+Chr$(13)+"DEMOVERSION",5,5,490,353,subwin,1)
  Local button  =CreateButton  (language$(70),210,368,80,22,subwin)

  ;HtmlViewGo htmlview, setup_path$+"lang_"+setup_language$+"/splash.htm"
  DisableGadget window

  Repeat
    WaitEvent()

    Select EventID()
      Case $0110 ;ESC-------------------------------------------------
        Exit

      Case $0401 ;gadgetevent-----------------------------------------
        If EventSource()=button Then Exit

      Case $0803 ;windowclose-----------------------------------------
        Exit
    End Select
  Forever


  EnableGadget window
  FreeGadget button
  FreeGadget htmlview
  FreeGadget subwin
  ActivateWindow window
End Function





;---------------------------------------------------------------------
;file: output file handle
;max:  max string length
;txt$: output text string
;---------------------------------------------------------------------
Function extra_writestr(file,max,txt$)
  Local char
  Local i
  Local lng

  lng=Len(txt$)
  For i=1 To max
    char=0
    If lng=>i Then char=Asc(Mid$(txt$,i,1))
    WriteByte file,char
  Next
End Function





;---------------------------------------------------------------------
;tile:   tileset handle
;RETURN: tile number
;---------------------------------------------------------------------
Function extra_tile2num(tile.tile)
  Local count
  Local tmp.tile

  For tmp=Each tile
    count=count+1
    If tmp=tile Then Return count
  Next
End Function