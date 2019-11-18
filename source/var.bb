;---------------------------------------------------------------------
Function var_prog()
  If layer=Null Then Return

  If demo=1 Then
    Notify language$(261)
    Return
  EndIf

  If demo=0 Then ;**********

  Local bank
  Local count
  Local i
  Local j
  Local lng1
  Local lng2
  Local pos
  Local selected
  Local size
  Local txt$
  Local txt1$
  Local txt2$

  Local subwin       =CreateWindow   (language$(26),(window_maxx-555)/2,(window_maxy-260)/2,555,260,window,33)
  Local subwin_width =ClientWidth    (subwin)
  Local subwin_height=ClientHeight   (subwin)
  Local subwin_list  =CreateListBox  (5,5,350,250,subwin)
  Local subwin_name  =CreateTextField(400,05,150,22,subwin)
  Local subwin_value =CreateTextField(400,30,150,22,subwin)
  Local subwin_label1=CreateLabel    (language$(279)+":",360,08,40,20,subwin)
  Local subwin_label2=CreateLabel    (language$(149)+":",360,33,40,20,subwin)
  Local subwin_add   =CreateButton   (language$(280) ,410,58,90,22,subwin)
  Local subwin_del   =CreateButton   (language$(281) ,410,83,90,22,subwin)
  Local subwin_exit  =CreateButton   (language$(070) ,410,subwin_height-32,90,22,subwin)

  DisableGadget window
  DisableGadget subwin_name
  DisableGadget subwin_value
  SetGadgetIconStrip subwin_list,window_strip3

  If layer\bank4<>0 Then
    size=BankSize(layer\bank4)

    For i=1 To size/4-1 ;important! (entry 0=total size)
      bank=PeekInt (layer\bank4,i*4)
      lng1=PeekByte(bank,0)
      lng2=PeekByte(bank,1)
      txt1$="" 
      txt2$=""

      For j=1 To lng1
        txt1$=txt1$+Chr$(PeekByte(bank,j+1))
      Next

      For j=1 To lng2
        txt2$=txt2$+Chr$(PeekByte(bank,j+lng1+1))
      Next

      AddGadgetItem subwin_list,txt1$+" = "+txt2$,0,0
      FreeBank bank
    Next

    FreeBank layer\bank4
    layer\bank4=0
  EndIf



  Repeat
    WaitEvent()

    Select EventID()
      Case $0110 ;ESC-------------------------------------------------
        Goto quit

      Case $0401 ;gadgetevent-----------------------------------------
        Select EventSource()
          Case subwin_add
            count=CountGadgetItems(subwin_list)
            If count<65535 Then
              SetGadgetText subwin_name,""
              SetGadgetText subwin_value,""
              EnableGadget subwin_name
              EnableGadget subwin_value
              AddGadgetItem subwin_list," = ",1,0
            EndIf
          Case subwin_del
            selected=SelectedGadgetItem(subwin_list)
            If selected=>0 Then
              SetGadgetText subwin_name,""
              SetGadgetText subwin_value,""
              DisableGadget subwin_name
              DisableGadget subwin_value
              RemoveGadgetItem subwin_list,selected
            EndIf
          Case subwin_exit
            Goto quit
          Case subwin_list
            selected=SelectedGadgetItem(subwin_list)
            txt$=GadgetItemText$(subwin_list,selected)
            pos=Instr(txt$," = ")
            txt1$=Left$(txt$,pos-1)
            txt2$=Mid$(txt$,pos+3)
            SetGadgetText subwin_name,txt1$
            SetGadgetText subwin_value,txt2$
            EnableGadget subwin_name
            EnableGadget subwin_value
          Case subwin_name,subwin_value
            txt1$=TextFieldText$(subwin_name)
            txt2$=TextFieldText$(subwin_value)
            SetGadgetText subwin_name ,Left$(Replace$(txt1$," = ","="),255)
            SetGadgetText subwin_value,Left$(Replace$(txt2$," = ","="),255)
            txt1$=TextFieldText$(subwin_name)
            txt2$=TextFieldText$(subwin_value)
            selected=SelectedGadgetItem(subwin_list)
            ModifyGadgetItem subwin_list,selected,txt1$+" = "+txt2$,0
        End Select

      Case $0803 ;windowclose-----------------------------------------
        Goto quit
    End Select
  Forever


  .quit
  count=CountGadgetItems(subwin_list)

  If count>0 Then
    layer\bank4=CreateBank(count*4+4)

    For i=0 To count-1
      txt$=GadgetItemText$(subwin_list,i)
      pos=Instr(txt$," = ")
      txt1$=Left$(txt$,pos-1)
      txt2$=Mid$(txt$,pos+3)
      lng1=Len(txt1$)
      lng2=Len(txt2$)
      total=total+(2+lng1+lng2)

      bank=CreateBank(2+lng1+lng2)
      PokeByte bank,0,lng1
      PokeByte bank,1,lng2

      For j=1 To lng1
        PokeByte bank,j+1,Asc(Mid$(txt1$,j,1))
      Next

      For j=1 To lng2
        PokeByte bank,j+lng1+1,Asc(Mid$(txt2$,j,1))
      Next

      PokeInt layer\bank4,i*4+4,bank
    Next

    PokeInt layer\bank4,0,total
  EndIf

  EnableGadget window
  FreeGadget subwin_exit
  FreeGadget subwin_dell
  FreeGadget subwin_add
  FreeGadget subwin_value
  FreeGadget subwin_name
  FreeGadget subwin_list
  FreeGadget subwin
  ActivateWindow window

  list_update()

  EndIf ;**********
End Function