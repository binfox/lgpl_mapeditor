Const meta_max=65536 ;WARNING - do not change - see meta_convert() buffer
Global meta_nr
Global meta_line





;---------------------------------------------------------------------
;canvas:    canvas handle
;slider:    slider handle
;textfield: textfield handle
;---------------------------------------------------------------------
Function meta_click(canvas,slider,textfield)
  Local canvash
  Local canvasw
  Local charw=8
  Local mx
  Local my
  Local nr
  Local pos
  Local size

  canvasw=GadgetWidth(canvas)
  canvash=GadgetHeight(canvas)
  size=BankSize(layer\bank3)
  pos=SliderValue(slider)
  mx=MouseX(canvas)
  my=MouseY(canvas)

  If mx=>charw*08+2 And mx<=charw*08+2+charw*01+6 Then nr=1
  If mx=>charw*12+2 And mx<=charw*12+2+charw*02+6 Then nr=2
  If mx=>charw*17+2 And mx<=charw*17+2+charw*03+6 Then nr=3
  If mx=>charw*23+2 And mx<=charw*23+2+charw*05+6 Then nr=4
  If mx=>charw*31+2 And mx<=charw*31+2+charw*04+6 Then nr=5
  If mx=>charw*38+2 And mx<=charw*38+2+charw*06+6 Then nr=6
  If mx=>charw*47+2 And mx<=charw*47+2+charw*11+6 Then nr=7

  If nr>0 Then
    meta_nr=nr
    meta_line=(my-26+pos)/20
    If meta_line>size-1 Then meta_line=size-1
    If meta_line<0 Then meta_line=0
    If meta_line*20<pos Then SetSliderValue slider,meta_line*20
    If meta_line*20>pos+canvash-47 Then SetSliderValue slider,meta_line*20-canvash+47
    meta_update(canvas,slider,textfield)
  EndIf
End Function





;---------------------------------------------------------------------
;mode:      0=bin<-asc
;           1=bin->asc
;toolbar:   toolbar handle
;tabber:    tabber handle
;border:    border handle
;textarea:  textarea handle
;textfield: textfield handle
;---------------------------------------------------------------------
Function meta_convert(mode,toolbar,tabber,border,textarea,textfield)
  Local bad
  Local buffer$[63]
  Local byte
  Local i
  Local nr
  Local size
  Local txt$

  If mode=0 Then

    txt$=TextAreaText$(textarea)
    txt$=Replace$(txt$,Chr$(13),"")
    If Len(txt$)>meta_max Then txt$=Left$(txt$,meta_max)

    size=Len(txt$)
    ResizeBank layer\bank3,size
    For i=0 To size-1
      byte=Asc(Mid$(txt$,i+1,1))
      PokeByte layer\bank3,i,byte
    Next

    EnableToolBarItem toolbar,4
    EnableToolBarItem toolbar,5
    EnableToolBarItem toolbar,6
    EnableToolBarItem toolbar,8
    EnableToolBarItem toolbar,9
    EnableToolBarItem toolbar,10

    HideGadget textarea
    ShowGadget border
    SelectGadgetItem tabber,0

  Else

    size=BankSize(layer\bank3)
    If size>meta_max Then size=meta_max

    For i=0 To size-1
      nr=i/1024
      byte=PeekByte(layer\bank3,i)
      If byte<>9 And byte<>10 And byte<>13 And byte<32 Then bad=1 Else buffer[nr]=buffer[nr]+Chr$(byte)
    Next

    If bad=1 Then
      If Confirm(language$(138))=0 Then
        SelectGadgetItem tabber,0
        Return
      EndIf
    EndIf

    For i=0 To 63
      txt$=txt$+buffer[i]
    Next

    DisableToolBarItem toolbar,4
    DisableToolBarItem toolbar,5
    DisableToolBarItem toolbar,6
    DisableToolBarItem toolbar,8
    DisableToolBarItem toolbar,9
    DisableToolBarItem toolbar,10

    HideGadget border
    ShowGadget textarea
    SetGadgetText textarea,txt$
    SetGadgetFont textarea,window_font2
    SetTextAreaColor textarea,0,0,0
    SetGadgetText textfield,""
    DisableGadget textfield
    SelectGadgetItem tabber,1

  EndIf
End Function





;---------------------------------------------------------------------
;mode:     0=bin, 1=ascii
;textarea: textarea handle
;---------------------------------------------------------------------
Function meta_load(mode,textarea)
  Local file
  Local name$
  Local size
  Local txt$

  name$=RequestFile$(language$(140))
  If name$="" Then Return

  file=ReadFile(name$)
  If file=0 Then Return

  If mode=0 Then
    size=FileSize(name$)
    If size>meta_max Then size=meta_max
    ResizeBank layer\bank3,size
    ReadBytes layer\bank3,file,0,size
  Else
    While Not Eof(file)
      txt$=txt$+ReadLine$(file)+Chr$(10)
      If Len(txt$)>meta_max Then
        txt$=Left$(txt$,meta_max)
        Exit
      EndIf
    Wend
    SetGadgetText textarea,txt$
  EndIf

  CloseFile file
End Function





;---------------------------------------------------------------------
Function meta_prog()
  If demo=1 Then
    Notify language$(261)
    Return
  EndIf

  If demo=0 Then ;**********

    meta_line=0
    meta_nr=1

    Local bottom=1
    Local char$
    Local i
    Local pos
    Local pos1
    Local pos2
    Local selected
    Local txt$
    Local txt1$
    Local txt2$
    Local value

    Local subwin        =CreateWindow   (language$(25),(window_maxx-515)/2,(window_maxy-485)/2,515,485,window,33)
    Local subwin_width  =ClientWidth    (subwin)
    Local subwin_height =ClientHeight   (subwin)
    Local panel         =CreatePanel    (0,0,300,28,subwin)
    Local panel_width   =ClientWidth    (panel)
    Local panel_height  =ClientHeight   (panel)
    Local tabber        =CreateTabber   (5,panel_height,subwin_width-10,subwin_height-panel_height-42,subwin)

    AddGadgetItem tabber,language$(150)
    AddGadgetItem tabber,language$(151)

    ;<<<THIS COMPENSATE TABBER-BUG IN BB+ V1.39>>>
    SetGadgetShape tabber,5,panel_height,subwin_width-10,subwin_height-panel_height-42

    Local tabber_width  =ClientWidth    (tabber)
    Local tabber_height =ClientHeight   (tabber)
    Local border        =CreatePanel    (3,3,tabber_width-6,tabber_height-6,tabber,1)
    Local border_width  =ClientWidth    (border)
    Local border_height =ClientHeight   (border)
    Local canvas        =CreateCanvas   (0,0,border_width-16,border_height,border)
    Local slider        =CreateSlider   (border_width-16,0,16,border_height,border,2)
    Local label         =CreateLabel    (language$(149)+":",subwin_width-140,8,35,20,subwin)
    Local textfield     =CreateTextField(subwin_width-105,5,100,22,subwin)
    Local textarea      =CreateTextArea (3,3,tabber_width-6,tabber_height-6,tabber)
    Local button        =CreateButton   (language$(70),(subwin_width-85)/2,subwin_height-32,85,22,subwin)
    Local toolbar       =CreateToolBar  ("image/toolbar3.bmp",0,0,panel_width,panel_height,panel)

    SetToolBarTips toolbar,language$(1)+","+language$(140)+","+language$(141)+",,"+language$(142)+","+language$(143)+","+language$(144)+",,"+language$(145)+","+language$(146)+","+language$(147)+",,"+language$(148)
    DisableToolBarItem toolbar,3
    DisableToolBarItem toolbar,7
    DisableToolBarItem toolbar,11
    toolbar_check(toolbar,12,1)

    SetGadgetFont textarea,window_font2
    DisableGadget window
    HideGadget textarea

    If layer\bank3=0 Then layer\bank3=CreateBank(0)
    meta_update(canvas,slider,textfield)
    If layer\ascii=1 Then meta_convert(1,toolbar,tabber,border,textarea,textfield)

    Repeat
      If SelectedGadgetItem(tabber)=0 Then
        If BankSize(layer\bank3)=>meta_max Then
          DisableToolBarItem toolbar,4
          DisableToolBarItem toolbar,5
          DisableToolBarItem toolbar,6
        Else
          EnableToolBarItem toolbar,4
          EnableToolBarItem toolbar,5
          EnableToolBarItem toolbar,6
        EndIf
        If BankSize(layer\bank3)=0 Then
          DisableToolBarItem toolbar,8
          DisableToolBarItem toolbar,9
          DisableToolBarItem toolbar,10
        Else
          EnableToolBarItem toolbar,8
          EnableToolBarItem toolbar,9
          EnableToolBarItem toolbar,10
        EndIf
      EndIf

      WaitEvent()

      selected=SelectedGadgetItem(tabber)
      If selected=0 Then ActivateGadget textfield
      If selected=1 Then ActivateGadget textarea

      Select EventID()
        Case $0110 ;ESC-------------------------------------------------
          Exit

        Case $0201 ;mousedown-------------------------------------------
          meta_click(canvas,slider,textfield)

        Case $0204 ;mousewheel--------------------------------------------
          If selected=0 Then
            pos=SliderValue(slider)
            SetSliderValue slider,pos-MouseZSpeed()*20
            meta_update(canvas,slider,textfield)
          EndIf

        Case $0401 ;gadgetevent-----------------------------------------
          Select EventSource()
            Case button
              Exit

            Case slider
              meta_update(canvas,slider,textfield)

            Case tabber
              meta_convert(selected,toolbar,tabber,border,textarea,textfield)
              If selected=0 Then meta_update(canvas,slider,textfield)

            Case textfield
              If EventData()=13 Then
                meta_line=meta_line+1
                meta_update(canvas,slider,textfield)
                Goto skip
              EndIf

              Select meta_nr
                Case 1
                  char$=memory_getval$(layer\bank3,meta_line,1,1)
                  txt$=TextFieldText$(textfield)
                  For i=1 To Len(txt$)
                    If Mid$(txt$,i,1)<>char$ Then txt$=Mid$(txt$,i,1) : Exit
                  Next
                  txt$=Left$(txt$+Chr$(0),1)
                  value=Asc(txt$)
                  PokeByte layer\bank3,meta_line,value

                Case 2
                  txt$=""
                  txt2$=Upper$(TextFieldText$(textfield))
                  For i=1 To Len(txt2$)
                    char$=Mid$(txt2$,i,1)
                    If (char$=>"0" And char$<="9") Or (char=>"A" And char<="F") Then txt$=txt$+char$
                  Next
                  txt$=Left$(txt$,2)
                  txt1$=Mid$("00"+txt$,Len(txt$)+1,1)
                  txt2$=Mid$("00"+txt$,Len(txt$)+2,1)
                  pos1=Instr("0123456789ABCDEF",txt1$)
                  pos2=Instr("0123456789ABCDEF",txt2$)
                  value=(pos1-1)*16+(pos2-1)
                  PokeByte layer\bank3,meta_line,value

                Case 3
                  value=extra_input(textfield,0,0,255)
                  txt$=TextFieldText$(textfield)
                  PokeByte layer\bank3,meta_line,value

                Case 4
                  value=extra_input(textfield,0,0,65535)
                  txt$=TextFieldText$(textfield)
                  PokeShort layer\bank3,meta_line,value

                Case 5
                  value=extra_input(textfield,0,-128,127)
                  txt$=TextFieldText$(textfield)
                  PokeByte layer\bank3,meta_line,value

                Case 6
                  value=extra_input(textfield,0,-32768,32767)
                  txt$=TextFieldText$(textfield)
                  PokeShort layer\bank3,meta_line,value

                Case 7
                  value=extra_input(textfield,0)
                  txt$=TextFieldText$(textfield)
                  PokeInt layer\bank3,meta_line,value

              End Select
              meta_update(canvas,slider,textfield)
              SetGadgetText textfield,txt$

            Case toolbar
              Select EventData()
                Case 0 ;new
                  ResizeBank layer\bank3,0
                  SetGadgetText textarea,""
                Case 1 ;load
                  meta_load(selected,textarea)
                Case 2 ;save
                  meta_save(selected,textarea)
                Case 4 ;add 1
                  memory_add(layer\bank3,meta_line,1,bottom,meta_max)
                Case 5 ;add 10
                  memory_add(layer\bank3,meta_line,10,bottom,meta_max)
                Case 6 ;add 100
                  memory_add(layer\bank3,meta_line,100,bottom,meta_max)
                Case 8 ;del 1
                  memory_delete(layer\bank3,meta_line,1)
                Case 9 ;del 10
                  memory_delete(layer\bank3,meta_line,10)
                Case 10 ;del 100
                  memory_delete(layer\bank3,meta_line,100)
                Case 12 ;bottom
                  bottom=1-bottom
                  toolbar_check(toolbar,12,bottom)
              End Select
              If selected=0 Then meta_update(canvas,slider,textfield)
          End Select

        Case $0803 ;windowclose-----------------------------------------
          Exit

      End Select
      .skip
    Forever

    layer\ascii=SelectedGadgetItem(tabber)
    If layer\ascii=1 Then meta_convert(0,toolbar,tabber,border,textarea,textfield)

    If BankSize(layer\bank3)=0 Then
      FreeBank layer\bank3
      layer\bank3=0
    EndIf

    EnableGadget window
    FreeGadget toolbar
    FreeGadget button
    FreeGadget textarea
    FreeGadget textfield
    FreeGadget label
    FreeGadget slider
    FreeGadget canvas
    FreeGadget border
    FreeGadget tabber
    FreeGadget panel
    FreeGadget subwin
    ActivateWindow window

    list_update()

  EndIf ;**********
End Function





;---------------------------------------------------------------------
;mode:     0=bin, 1=ascii
;textarea: textarea handle
;---------------------------------------------------------------------
Function meta_save(mode,textarea)
  Local file
  Local name$
  Local size
  Local txt$

  name$=RequestFile$(language$(141),"",1)
  If name$="" Then Return

  file=WriteFile(name$)
  If file=0 Then Return

  If mode=0 Then
    size=BankSize(layer\bank3)
    WriteBytes layer\bank3,file,0,size
  Else
    txt$=TextAreaText$(textarea)
    WriteLine file,txt$
  EndIf

  CloseFile file
End Function





;---------------------------------------------------------------------
;canvas:    canvas handle
;slider:    slider handle
;textfield: textfield handle
;---------------------------------------------------------------------
Function meta_update(canvas,slider,textfield)
  Local canvash
  Local canvasw
  Local charw=8
  Local endpos
  Local i
  Local pos
  Local size
  Local startpos
  Local t$[7]
  Local txt$="--"

  canvasw=GadgetWidth (canvas)
  canvash=GadgetHeight(canvas)

  size=BankSize(layer\bank3)
  SetSliderRange slider,canvash-28,size*20
  pos=SliderValue(slider)

  startpos=pos/20
  If startpos<0 Then startpos=0
  If startpos>size-1 Then startpos=size-1

  endpos=(pos+canvash-28)/20
  If endpos<0 Then endpos=0
  If endpos>size-1 Then endpos=size-1

  SetBuffer CanvasBuffer(canvas)
  SetFont window_font2
  ClsColor 255,255,255
  Cls

  If meta_line>size-1 Then meta_line=size-1
  If meta_line<0 Then meta_line=0
  If size>0 Then
    Color 255,255,0
    If meta_nr=1 Then Rect charw*08+2,meta_line*20+27-pos,charw*01+6,18
    If meta_nr=2 Then Rect charw*12+2,meta_line*20+27-pos,charw*02+6,18
    If meta_nr=3 Then Rect charw*17+2,meta_line*20+27-pos,charw*03+6,18
    If meta_nr=4 Then Rect charw*23+2,meta_line*20+27-pos,charw*05+6,18
    If meta_nr=5 Then Rect charw*31+2,meta_line*20+27-pos,charw*04+6,18
    If meta_nr=6 Then Rect charw*38+2,meta_line*20+27-pos,charw*06+6,18
    If meta_nr=7 Then Rect charw*47+2,meta_line*20+27-pos,charw*11+6,18
    txt$=memory_getval$(layer\bank3,meta_line,meta_nr,1)
  EndIf
  If Left$(txt$,2)<>"--" Then
    SetGadgetText textfield,txt$
    EnableGadget textfield
  Else
    SetGadgetText textfield,""
    DisableGadget textfield
  EndIf

  If startpos=>0 Then
    Color 0,0,0
    For i=startpos To endpos
      t[0]=Right$("00000"+Str$(i),5)+"   "
      t[1]=memory_getval$(layer\bank3,i,1)+"   "
      t[2]=memory_getval$(layer\bank3,i,2)+"   "
      t[3]=memory_getval$(layer\bank3,i,3)+"   "
      t[4]=memory_getval$(layer\bank3,i,4)+"   "
      t[5]=memory_getval$(layer\bank3,i,5)+"   "
      t[6]=memory_getval$(layer\bank3,i,6)+"   "
      t[7]=memory_getval$(layer\bank3,i,7)
      Text 5,30+i*20-pos,t[0]+t[1]+t[2]+t[3]+t[4]+t[5]+t[6]+t[7]
      If (i Mod 2)=0 Then Oval charw*22+4,i*20+34-pos,5,5
      If (i Mod 2)=0 Then Oval charw*37+4,i*20+34-pos,5,5
      If (i Mod 4)=0 Then Oval charw*46+4,i*20+34-pos,5,5
    Next
  EndIf

  Color 240,240,240
  Rect 0,0,canvasw,24
  Color 0,0,0
  Rect 0,24,canvasw,1
  Text 5,5, "Offset  Chr Hex  UByte UWord   SByte  SWord    SLong"

  FlipCanvas canvas
End Function