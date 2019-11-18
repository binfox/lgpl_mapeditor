Type map_err
  Field message$
End Type

Global map_file$
Global map_path$
Global map_passw$
Global map_tmp$=SystemProperty$("TEMPDIR")+"data.tmp"
Global map_app$=SystemProperty$("APPDIR")
If FileType(map_app$+"setup.ini")=0 Then map_app$=CurrentDir$()

Dim map_block (0)
Dim map_offset(0)





;---------------------------------------------------------------------
Function map_backup()
  If map_file$="" Then
    map_save()
  Else
    map_save2(map_path$+map_file$+".bck")
  EndIf
End Function





;---------------------------------------------------------------------
;RETURN: file size in byte
;---------------------------------------------------------------------
Function map_calc()
  Local bank
  Local count_anim
  Local count_base
  Local count_data
  Local count_def
  Local count_geo
  Local count_image
  Local count_layer
  Local count_meta
  Local count_tile
  Local count_total
  Local count_var
  Local frames
  Local i
  Local layer.layer
  Local maxsize
  Local size
  Local tile.tile


  For tile=Each tile
    count_tile=count_tile+1
    count_anim=count_anim+tile\anims
    If tile\needdef=1 Then count_def=count_def+1
  Next


  For layer=Each layer
    If layer\code=layer_map Or layer\code=layer_iso1 Or layer\code=layer_iso2 Or layer\code=layer_hex1 Or layer\code=layer_hex2 Or layer\code=layer_clone Then count_layer=count_layer+1
    If layer\code=layer_image Or layer\code=layer_block Then count_image=count_image+1
    If layer\code=layer_point Or layer\code=layer_line Or layer\code=layer_rect Or layer\code=layer_oval Then count_geo=count_geo+1
    If layer\code=layer_map Or layer\code=layer_iso1 Or layer\code=layer_iso2 Or layer\code=layer_hex1 Or layer\code=layer_hex2 Then
      count_base=count_base+1
      If layer\bank2<>0 Then count_data=count_data+1
    EndIf
    If layer\code=>0 Then
      If layer\bank3<>0 Then count_meta=count_meta+1
      If layer\bank4<>0 Then count_var=count_var+1
    EndIf
  Next
  count_total=1 + count_layer + count_image + count_geo + count_tile + count_anim + count_def + count_base + count_data + count_meta + count_var


  For tile=Each tile
    maxsize=maxsize+26 ;block size (tileset)

    For i=1 To tile\anims
      bank=PeekInt(tile\banka,i*4-4)
      frames=PeekShort(bank,0)
      maxsize=maxsize+13+frames*4 ;block size (animation)
    Next

    If tile\needdef=1 Then maxsize=maxsize+4+tile\count ;default value structure
  Next


  For i=0 To layer_count
    layer=layer_list[i]

    If layer\code=layer_back  Then maxsize=maxsize+36 ;block size (back)
    If layer\code=layer_map   Then maxsize=maxsize+36 ;block size (map)
    If layer\code=layer_iso1  Then maxsize=maxsize+34 ;block size (iso1)
    If layer\code=layer_iso2  Then maxsize=maxsize+35 ;block size (iso2)
    If layer\code=layer_hex1  Then maxsize=maxsize+35 ;block size (hex1)
    If layer\code=layer_hex2  Then maxsize=maxsize+35 ;block size (hex2)
    If layer\code=layer_clone Then maxsize=maxsize+26 ;block size (clone)
    If layer\code=layer_image Then maxsize=maxsize+32 ;block size (image)
    If layer\code=layer_block Then maxsize=maxsize+34 ;block size (block)
    If layer\code=layer_point Then maxsize=maxsize+24 ;block size (point)
    If layer\code=layer_line  Then maxsize=maxsize+32 ;block size (line)
    If layer\code=layer_rect  Then maxsize=maxsize+32 ;block size (rect)
    If layer\code=layer_oval  Then maxsize=maxsize+32 ;block size (oval)

    If layer\code=layer_map Or layer\code=layer_iso1 Or layer\code=layer_iso2 Or layer\code=layer_hex1 Or layer\code=layer_hex2 Then
      size=BankSize(layer\bank1)
      maxsize=maxsize+8+size ;block size (base)

      If layer\bank2<>0 Then
        size=BankSize(layer\bank2)
        maxsize=maxsize+8+size ;block size (data)
      EndIf
    EndIf

    If layer\code=>0 And layer\bank3<>0 Then
      size=BankSize(layer\bank3)
      maxsize=maxsize+8+size ;block size (meta)
    EndIf

    If layer\code=>0 And layer\bank4<>0 Then
      size=PeekInt(layer\bank4,0)
      maxsize=maxsize+8+size ;block size (meta)
    EndIf
  Next

  maxsize=maxsize+64+count_total*4
  If (maxsize Mod 4)>0 Then maxsize=maxsize+4-(maxsize Mod 4)

  If map_file$<>"" Then 
    SetGadgetText window,language$(0)+" "+UMEVersion+"  ["+map_file$+"  "+Str$(maxsize)+" byte]"
  Else
    SetGadgetText window,language$(0)+" "+UMEVersion+"  ["+Str$(maxsize)+" byte]"
  EndIf
End Function





;---------------------------------------------------------------------
Function map_check()
  Local button
  Local max
  Local name$
  Local subwin
  Local textarea


  If First layer=Null Then
    map_error(language$(95))
    map_new()
  EndIf

  If layer_list[0]=Null Then
    map_error(language$(96))
    layer        =New layer
    layer\ascii  =0
    layer\bank1  =0
    layer\bank2  =0
    layer\bank3  =0
    layer\code   =layer_back
    layer\depth1 =0
    layer\depth2 =0
    layer\frame  =0
    layer\layer  =Null
    layer\mask   =0
    layer\mode   =0
    layer\name$  =language$(40)
    layer\parax  =100
    layer\paray  =100
    layer\posx   =0
    layer\posy   =0
    layer\sizex  =0
    layer\sizey  =0
    layer\start  =0
    layer\tile   =Null
    layer\visible=1
  EndIf

  If layer_file$<>"" And layer_handle=0 Then
    map_error(language$(97))
  EndIf

  For tile=Each tile
    If tile\anims<>BankSize(tile\banka)/4 Then
      map_error("("+language$(75)+" '"+tile\file$+"') "+language$(98))
      tile\anims=BankSize(tile\banka)/4
    EndIf
  Next


  For layer=Each layer
    name$="("+language$(77)+" '"+layer\name$+"') "
    If layer\code=layer_back Then name$="("+language$(40)+") "

    If layer\depth1<>0 And layer\depth1<>4 And layer\depth1<>8 And layer\depth1<>12 And layer\depth1<>16 Then
      map_error(name$+language$(99))
      layer\depth1=4
    EndIf

    If layer\depth2<>0 And layer\depth2<>1 And layer\depth2<>2 And layer\depth2<>4 And layer\depth2<>8 Then
      map_error(name$+language$(100))+"("+layer\depth2+")"
      layer\depth2=4
    EndIf

    If layer\bank3<>0 Then
      If BankSize(layer\bank3)>meta_max Then
        map_error(name$+language$(101))
        ResizeBank layer\bank3,meta_max
      EndIf
    EndIf

    If layer\code=layer_map Or layer\code=layer_iso1 Or layer\code=layer_iso2 Or layer\code=layer_hex1 Or layer\code=layer_hex2 Then
      If layer\sizex>layer_maxtiles Then
        map_error(name$+language$(102))
        layer\sizex=layer_maxtiles
      EndIf

      max=layer_maxtiles / layer\sizex
      If layer\sizey>max Then
        map_error(name$+language$(103))
        layer\sizey=max
      EndIf

      If layer\bank1=0 Then
        map_error(name$+language$(104))
        layer\bank1=CreateBank(layer\sizex*layer\sizey)
        layer\depth1=8
      EndIf
    EndIf

    If layer\code=layer_back Or layer\code=layer_clone Or layer\code=layer_image Or layer\code=layer_block Or layer\code=layer_point Or layer\code=layer_line Or layer\code=layer_rect Or layer\code=layer_oval Then
      If layer\bank1<>0 Then
        map_error(name$+language$(105))
        FreeBank layer\bank1
        layer\bank1=0
        layer\depth1=0
      EndIf

      If layer\bank2<>0 Then
        map_error(name$+language$(106))
        FreeBank layer\bank2
        layer\bank2=0
        layer\depth2=0
      EndIf
    EndIf

    If layer\code=layer_map Or layer\code=layer_iso1 Or layer\code=layer_iso2 Or layer\code=layer_hex1 Or layer\code=layer_hex2 Or layer\code=layer_image Or layer\code=layer_block Then
      If layer\tile=Null And layer\tmp<>0 Then
        map_error(name$+language$(107))
      EndIf
      layer\tmp=0
    EndIf

    If layer\code=layer_iso2 Or layer\code=layer_hex1 Or layer\code=layer_hex2 Then
      If layer\start>1 Then
        map_error(name$+language$(108))
        layer\start=0
      EndIf
    EndIf

    If layer\ascii>1 Then
      map_error(name$+language$(109))
      layer\ascii=0
    EndIf

    If layer\mask>1 Then
      map_error(name$+language$(110))
      layer\mask=0
    EndIf

    If layer\visible>1 Then
      map_error(name$+language$(111))
      layer\visible=0
    EndIf

    If layer\code=layer_back Then
      If layer\mode>3 Then
        map_error(name$+language$(112))
        layer\mode=0
      EndIf

      If layer<>layer_list[0] Then
        map_error(name$+language$(113))
        If layer\bank3<>0 Then FreeBank layer\bank3
        Delete layer
      EndIf
    EndIf

    If layer\code=layer_clone Then
      If layer\layer=Null And layer\tmp<>0 Then
        map_error(name$+language$(114))
      EndIf
      layer\tmp=0
    EndIf

    If layer\code=layer_block Then
      If layer\layer=Null And layer\depth2<>0 Then
        map_error(name$+language$(114))
      EndIf
      layer\depth2=0
    EndIf
  Next


  layer=layer_list[0]
  layer_nr=0


  If First map_err=Null Then Return
  subwin  =CreateWindow  (language$(115),(window_maxx-400)/2,(window_maxy-300)/2,400,300,window,33)
  textarea=CreateTextArea(5,5,390,253,subwin)
  button  =CreateButton  (language$(70),160,268,80,22,subwin)
  SendMessage QueryObject(textarea,1),$CF,1,0
  DisableGadget window

  For map_err.map_err=Each map_err
    AddTextAreaText textarea,map_err\message$+Chr$(10)
  Next

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
  FreeGadget textarea
  FreeGadget subwin
  ActivateWindow window
End Function





;---------------------------------------------------------------------
;message: error message
;---------------------------------------------------------------------
Function map_error(message$)
  map_err.map_err =New map_err
  map_err\message$=message$
End Function





;---------------------------------------------------------------------
Function map_load()
  Local i%
  Local name$

  ChangeDir map_path$
  name$=RequestFile$(language$(2),"map")
  ChangeDir map_app$

  If name$="" Then Return


  For i=Len(name$) To 1 Step -1
    If Mid$(name$,i,1)="\" Or Mid$(name$,i,1)="/" Then
      map_path$=Left$(name$,i)
      map_file$=Mid$(name$,i+1)
      Exit
    EndIf
  Next

  map_load2(name$)
End Function





;---------------------------------------------------------------------
;name: full path name
;---------------------------------------------------------------------
Function map_load2(name$)
  Local bank
  Local bytes
  Local checksum
  Local code
  Local count_anim
  Local count_base
  Local count_data
  Local count_def
  Local count_geo
  Local count_image
  Local count_layer
  Local count_meta
  Local count_tile
  Local count_total
  Local count_var
  Local crypted
  Local dummy
  Local dummybytes
  Local file
  Local fsize
  Local frames
  Local hash$
  Local i
  Local j
  Local maxsize
  Local passw$
  Local proversion
  Local size1
  Local size2
  Local subversion
  Local version

  Delete Each map_err

  If FileType(name$)<>1 Then
    Notify language$(116)
    Return
  EndIf

  fsize=FileSize(name$)
  If fsize<64 Then
    Notify language$(117)
    Return
  EndIf

  file=ReadFile(name$)
  If file=0 Then
    Notify language$(118)
    Return
  EndIf


  ;HEADER-------------------------------------------------------------
  If ReadByte(file)<>85 Or ReadByte(file)<>77 Or ReadByte(file)<>70 Then
    Notify language$(119)
    CloseFile file
    Return
  EndIf

  version    =ReadByte(file)
  checksum   =ReadInt(file)
  crypted    =Sgn(version And 128)
  proversion =Sgn(version And 64)
  subversion =(version And 63)

  If proversion=1 And demo=1 Then
    Notify language$(120)
    CloseFile file
    Return
  EndIf

  If subversion>build Then
    If Confirm(language$(121))=0 Then
      CloseFile file
      Return
    EndIf
  EndIf

  For i=1 To 20
    hash$=hash$+Chr$(ReadByte(file))
  Next

  If crypted=1 Then
    CloseFile file

    passw$=extra_passw$()
    If passw$="" Then Return

    If sha1$(passw$)<>hash$ Then
      Notify language$(122)
      Return
    EndIf

    crypt_file(name$, map_tmp$, passw$)
    file=ReadFile(map_tmp$)
    If file=0 Then Return
    SeekFile file,28
  EndIf

  count_total=ReadShort(file)
  count_layer=ReadShort(file)
  count_image=ReadShort(file)
  count_geo  =ReadShort(file)
  count_tile =ReadShort(file)
  count_anim =ReadShort(file)
  count_base =ReadShort(file)
  count_data =ReadShort(file)
  count_meta =ReadShort(file)

  If subversion<3 Then
    dummybytes=18
  ElseIf subversion=>3 Then
    count_var=ReadShort(file)
    count_def=ReadShort(file)
    dummybytes=14
  EndIf

  For i=1 To dummybytes
    dummy=ReadByte(file)
  Next


  ;TABLE--------------------------------------------------------------
  maxsize=count_total*4+64

  If maxsize>fsize Then
    Notify language$(117)
    Goto abort
  EndIf

  Dim map_block (count_total)
  Dim map_offset(count_total)

  For i=1 To count_total
    map_block (i)=ReadInt(file)
    map_offset(i)=maxsize
    maxsize=maxsize+map_block(i)
  Next

  If (maxsize Mod 4)>0 Then maxsize=maxsize+4-(maxsize Mod 4)

  If maxsize<>fsize Then
    If Confirm(language$(123))=0 Then
      Goto abort
    EndIf
  EndIf


  ;BLOCKS-------------------------------------------------------------
  map_new(1)

  For i=1 To count_total
    SeekFile file,map_offset(i)
    code=ReadByte(file)

    Select code
      Case layer_back ;-----------------------------------------------
        layer         =New layer
        layer\code    =code
        layer\visible =ReadByte(file)
        layer_file$   =extra_readstr(file,12)
        layer\parax   =ReadByte(file)
        layer\paray   =ReadByte(file)
        layer\posx    =ReadInt (file)
        layer\posy    =ReadInt (file)
        layer\sizex   =ReadInt (file)
        layer\sizey   =ReadInt (file)
        layer\mode    =ReadByte(file)
        layer_colour  =(ReadByte(file) Shl 16) + (ReadByte(file) Shl 8) + ReadByte(file)
        layer\name$   =language$(40)

      Case layer_map ;------------------------------------------------
        layer         =New layer
        layer\code    =code
        layer\visible =ReadByte (file)
        layer\name$   =extra_readstr(file,12)
        layer\parax   =ReadByte (file)
        layer\paray   =ReadByte (file)
        layer\posx    =ReadInt  (file)
        layer\posy    =ReadInt  (file)
        layer\sizex   =ReadInt  (file)
        layer\sizey   =ReadInt  (file)
        layer\tmp     =ReadShort(file)
        layer\mask    =ReadByte (file)
        If subversion=>3 Then layer\mode=ReadByte(file)

      Case layer_iso1 ;-----------------------------------------------
        If demo=0 Then
          layer         =New layer
          layer\code    =code
          layer\visible =ReadByte (file)
          layer\name$   =extra_readstr(file,12)
          layer\parax   =ReadByte (file)
          layer\paray   =ReadByte (file)
          layer\posx    =ReadInt  (file)
          layer\posy    =ReadInt  (file)
          layer\sizex   =ReadInt  (file)
          layer\sizey   =ReadInt  (file)
          layer\tmp     =ReadShort(file)
        EndIf

      Case layer_iso2 ;-----------------------------------------------
        If demo=0 Then
          layer         =New layer
          layer\code    =code
          layer\visible =ReadByte (file)
          layer\name$   =extra_readstr(file,12)
          layer\parax   =ReadByte (file)
          layer\paray   =ReadByte (file)
          layer\posx    =ReadInt  (file)
          layer\posy    =ReadInt  (file)
          layer\sizex   =ReadInt  (file)
          layer\sizey   =ReadInt  (file)
          layer\tmp     =ReadShort(file)
          layer\start   =ReadByte (file)
        EndIf

      Case layer_hex1 ;-----------------------------------------------
        If demo=0 Then
          layer         =New layer
          layer\code    =code
          layer\visible =ReadByte (file)
          layer\name$   =extra_readstr(file,12)
          layer\parax   =ReadByte (file)
          layer\paray   =ReadByte (file)
          layer\posx    =ReadInt  (file)
          layer\posy    =ReadInt  (file)
          layer\sizex   =ReadInt  (file)
          layer\sizey   =ReadInt  (file)
          layer\tmp     =ReadShort(file)
          layer\start   =ReadByte (file)
        EndIf

      Case layer_hex2 ;-----------------------------------------------
        If demo=0 Then
          layer         =New layer
          layer\code    =code
          layer\visible =ReadByte (file)
          layer\name$   =extra_readstr(file,12)
          layer\parax   =ReadByte (file)
          layer\paray   =ReadByte (file)
          layer\posx    =ReadInt  (file)
          layer\posy    =ReadInt  (file)
          layer\sizex   =ReadInt  (file)
          layer\sizey   =ReadInt  (file)
          layer\tmp     =ReadShort(file)
          layer\start   =ReadByte (file)
        EndIf

      Case layer_clone ;----------------------------------------------
        layer         =New layer
        layer\code    =code
        layer\visible =ReadByte (file)
        layer\name$   =extra_readstr(file,12)
        layer\parax   =ReadByte (file)
        layer\paray   =ReadByte (file)
        layer\posx    =ReadInt  (file)
        layer\posy    =ReadInt  (file)
        layer\tmp     =ReadShort(file)

      Case layer_image ;----------------------------------------------
        layer         =New layer
        layer\code    =code
        layer\visible =ReadByte (file)
        layer\name$   =extra_readstr(file,12)
        layer\parax   =ReadByte (file)
        layer\paray   =ReadByte (file)
        layer\posx    =ReadInt  (file)
        layer\posy    =ReadInt  (file)
        layer\tmp     =ReadShort(file)
        layer\frame   =ReadShort(file)
        layer\start   =ReadShort(file)
        layer\mode    =ReadByte (file)
        layer\mask    =ReadByte (file)

      Case layer_block ;----------------------------------------------
        layer         =New layer
        layer\code    =code
        layer\visible =ReadByte (file)
        layer\name$   =extra_readstr(file,12)
        layer\parax   =ReadByte (file)
        layer\paray   =ReadByte (file)
        layer\posx    =ReadInt  (file)
        layer\posy    =ReadInt  (file)
        layer\tmp     =ReadShort(file)
        layer\frame   =ReadShort(file)
        layer\start   =ReadShort(file)
        layer\mode    =ReadByte (file)
        layer\mask    =ReadByte (file)
        layer\depth2  =ReadShort(file) ;<TMP

      Case layer_point ;----------------------------------------------
        layer         =New layer
        layer\code    =code
        layer\visible =ReadByte(file)
        layer\name$   =extra_readstr(file,12)
        layer\parax   =ReadByte(file)
        layer\paray   =ReadByte(file)
        layer\posx    =ReadInt (file)
        layer\posy    =ReadInt (file)

      Case layer_line ;-----------------------------------------------
        layer         =New layer
        layer\code    =code
        layer\visible =ReadByte(file)
        layer\name$   =extra_readstr(file,12)
        layer\parax   =ReadByte(file)
        layer\paray   =ReadByte(file)
        layer\posx    =ReadInt (file)
        layer\posy    =ReadInt (file)
        layer\sizex   =ReadInt (file)
        layer\sizey   =ReadInt (file)

      Case layer_rect ;-----------------------------------------------
        layer         =New layer
        layer\code    =code
        layer\visible =ReadByte(file)
        layer\name$   =extra_readstr(file,12)
        layer\parax   =ReadByte(file)
        layer\paray   =ReadByte(file)
        layer\posx    =ReadInt (file)
        layer\posy    =ReadInt (file)
        layer\sizex   =ReadInt (file)
        layer\sizey   =ReadInt (file)

      Case layer_oval ;-----------------------------------------------
        layer         =New layer
        layer\code    =code
        layer\visible =ReadByte(file)
        layer\name$   =extra_readstr(file,12)
        layer\parax   =ReadByte(file)
        layer\paray   =ReadByte(file)
        layer\posx    =ReadInt (file)
        layer\posy    =ReadInt (file)
        layer\sizex   =ReadInt (file)
        layer\sizey   =ReadInt (file)

      Case 100 ;tileset-----------------------------------------------
        tile        =New tile
        tile\mask   =(ReadByte(file) Shl 16) + (ReadByte(file) Shl 8) + ReadByte(file)
        tile\sizex  =ReadShort(file)
        tile\sizey  =ReadShort(file)
        tile\factor =ReadShort(file)
        tile\anims  =ReadShort(file)
        tile\count  =ReadShort(file)
        tile\file$  =extra_readstr(file,12)
        tile\banka  =CreateBank(0)
        tile\bankd  =CreateBank(65536)
        tile\bankx  =CreateBank(0)
        tile\banky  =CreateBank(0)

      Case 101 ;animation---------------------------------------------
        If tile<>Null Then
          frames =ReadShort(file)
          bytes  =frames*4+12

          If bytes+1<>map_block(i) Then
            map_error(language$(124))
          EndIf

          bank=CreateBank(bytes)
          PokeShort bank,0,frames
          ReadBytes bank,file,2,bytes-2

          bytes=BankSize(tile\banka)
          ResizeBank tile\banka,bytes+4
          PokeInt tile\banka,bytes,bank
        Else
          map_error(language$(125))
        EndIf

      Case 102 ;basedata----------------------------------------------
        If layer<>Null Then
          If layer\bank1<>0 Then
            map_error(language$(126))
            FreeBank layer\bank1
          EndIf

          layer\depth1=ReadByte  (file)
          bytes       =ReadInt   (file)
          dummy       =ReadShort (file)
          layer\bank1 =CreateBank(bytes)
          ReadBytes layer\bank1,file,0,bytes

          If bytes+8<>map_block(i) Then
            map_error(language$(127))
          EndIf
        Else
          map_error(language$(128))
        EndIf

      Case 103 ;datalayer---------------------------------------------
        If layer<>Null Then
          If layer\bank2<>0 Then
            map_error(language$(129))
            FreeBank layer\bank2
          EndIf

          layer\depth2=ReadByte  (file)
          bytes       =ReadInt   (file)
          dummy       =ReadShort (file)
          layer\bank2 =CreateBank(bytes)
          ReadBytes layer\bank2,file,0,bytes

          If bytes+8<>map_block(i) Then
            map_error(language$(130))
          EndIf
        Else
          map_error(language$(131))
        EndIf

      Case 104 ;metadata----------------------------------------------
        If demo=0 Then
          If layer<>Null Then
            If layer\bank3<>0 Then
              map_error(language$(132))
              FreeBank layer\bank3
            EndIf

            layer\ascii =ReadByte  (file)
            bytes       =ReadInt   (file)
            dummy       =ReadShort (file)
            layer\bank3 =CreateBank(bytes)
            ReadBytes layer\bank3,file,0,bytes

            If bytes+8<>map_block(i) Then
              map_error(language$(133))
            EndIf
          Else
            map_error(language$(134))
          EndIf
        EndIf

      Case 105 ;default values----------------------------------------
        If tile<>Null Then
          frames=ReadShort(file)
          dummy =ReadByte(file)
          If frames>0 Then
            ReadBytes tile\bankd,file,0,frames
            tile\needdef=1
          EndIf
        EndIf

      Case 106 ;variables---------------------------------------------
        If layer<>Null Then
          If layer\bank4<>0 Then
            map_error(language$(275))
            FreeBank layer\bank4
            layer\bank4=0
          EndIf

          dummy=ReadByte (file)
          count=ReadShort(file)
          bytes=ReadInt  (file)

          If bytes+8<>map_block(i) Then
            map_error(language$(276))
          Else
            layer\bank4=CreateBank(count*4+4)
            PokeInt layer\bank4,0,bytes
            For j=1 To count
              size1=ReadByte(file)
              size2=ReadByte(file)
              bank=CreateBank(2+size1+size2)
              PokeByte bank,0,size1
              PokeByte bank,1,size2
              ReadBytes bank,file,2,size1
              ReadBytes bank,file,2+size1,size2
              PokeInt layer\bank4,j*4,bank
            Next
          EndIf
        Else
          map_error(language$(277))
        EndIf

      Default ;-------------------------------------------------------
        map_error(language$(135))
        layer=Null
        tile=Null

    End Select
  Next


  ;FINAL
  layer_count=0
  For layer=Each layer
    If layer\code=0 Then
      layer_list[0]=layer
    ElseIf layer\code>0 Then
      If layer_count=layer_maxcount Then Exit
      layer_count=layer_count+1
      layer_list[layer_count]=layer
    EndIf

    If layer\code=layer_map Or layer\code=layer_iso1 Or layer\code=layer_iso2 Or layer\code=layer_hex1 Or layer\code=layer_hex2 Or layer\code=layer_image Or layer\code=layer_block Then
      layer\tile=extra_num2tile(layer\tmp)
    EndIf

    If layer\code=layer_clone Then
      layer\layer=extra_num2layer(layer\tmp)
    EndIf

    If layer\code=layer_block Then
      layer\layer=extra_num2layer(layer\depth2)
      layer\depth2=0
    EndIf
  Next

  For i=Len(name$) To 1 Step -1
    If Mid$(name$,i,1)="\" Or Mid$(name$,i,1)="/" Then
      map_path$=Left$(name$,i)
      map_file$=Mid$(name$,i+1)
      Exit
    EndIf
  Next

  If Lower$(Right$(map_file$,4))=".bck" Then
    map_file$=Left$(map_file$,Len(map_file$)-4)
  EndIf

  SetSliderValue editor_sliderx,0
  SetSliderValue editor_slidery,0

  map_passw$=passw$
  tile_fullupdate(1)
  map_check()
  list_update()
  menu_update()
  editor_update()

  .abort
  Dim map_block (0)
  Dim map_offset(0)

  CloseFile file
  If crypted=1 Then DeleteFile map_tmp$
End Function





;---------------------------------------------------------------------
;empty: 0=with background
;       1=without background
;---------------------------------------------------------------------
Function map_new(empty=0)
  layer_reset(empty)
  tile_reset()

  map_passw$=""
  map_path$=SystemProperty$("APPDIR")
  map_file$=""

  If empty=0 Then
    list_update()
    menu_update()
    editor_update()
  EndIf
End Function





;---------------------------------------------------------------------
Function map_restore()
  If map_file$="" Then
    map_load()
  Else
    map_load2(map_path$+map_file$+".bck")
  EndIf
End Function




;---------------------------------------------------------------------
Function map_saveas()
  Local i
  Local name$


    ChangeDir map_path$
    name$=RequestFile$(language$(3),"map",1)
    ChangeDir map_app$
    If name$="" Then Return

    For i=Len(name$) To 1 Step -1
      If Mid$(name$,i,1)="\" Or Mid$(name$,i,1)="/" Then
        map_path$=Left$(name$,i)
        map_file$=Mid$(name$,i+1)
        Exit
      EndIf
    Next

  map_save2(map_path$+map_file$)
  map_calc()
End Function


;---------------------------------------------------------------------
Function map_save(auto%=0)
  Local i
  Local name$

  If auto =0 Then
    If map_file$="" Or KeyDown(42)=1 Then
      ChangeDir map_path$
      name$=RequestFile$(language$(3),"map",1)
      ChangeDir map_app$
      If name$="" Then Return

      For i=Len(name$) To 1 Step -1
        If Mid$(name$,i,1)="\" Or Mid$(name$,i,1)="/" Then
          map_path$=Left$(name$,i)
          map_file$=Mid$(name$,i+1)
          Exit
        EndIf
      Next
    EndIf
    map_save2(map_path$+map_file$)
    map_calc()
  Else
    If ((Not(map_file$="")) And (doautosave%=1)) Then
      zeit$ = Replace(CurrentTime$(),":","_")
      datum$= Replace(CurrentDate$()," ","_")
      map_save2(map_path$+map_file$+"."+datum$+"_"+zeit$)
      doautosave%=0
    EndIf
  EndIf

  
End Function





;---------------------------------------------------------------------
;name: full path name
;---------------------------------------------------------------------
Function map_save2(name$)
  Local bank
  Local count
  Local count_anim
  Local count_base
  Local count_data
  Local count_def
  Local count_geo
  Local count_image
  Local count_layer
  Local count_meta
  Local count_tile
  Local count_total
  Local count_var
  Local crc32
  Local file
  Local frames
  Local hash$
  Local i
  Local j
  Local layer.layer
  Local pos
  Local size
  Local tile.tile
  Local version

  If demo=1 Then map_passw$=""
  If map_passw$<>"" Then
    file=WriteFile(map_tmp$)
  Else
    file=WriteFile(name$)
  EndIf

  If file=0 Then
    Notify language$(137)
    Return
  EndIf

  optimize_clear()


  ;HEADER-------------------------------------------------------------
  version=build
  If demo=0 Then version=version+64
  If map_passw$<>"" Then version=version+128

  For tile=Each tile
    count_tile=count_tile+1
    count_anim=count_anim+tile\anims
    If tile\needdef=1 Then count_def=count_def+1
  Next

  For layer=Each layer
    If layer\code=layer_map Or layer\code=layer_iso1 Or layer\code=layer_iso2 Or layer\code=layer_hex1 Or layer\code=layer_hex2 Or layer\code=layer_clone Then count_layer=count_layer+1
    If layer\code=layer_image Or layer\code=layer_block Then count_image=count_image+1
    If layer\code=layer_point Or layer\code=layer_line Or layer\code=layer_rect Or layer\code=layer_oval Then count_geo=count_geo+1
    If layer\code=layer_map Or layer\code=layer_iso1 Or layer\code=layer_iso2 Or layer\code=layer_hex1 Or layer\code=layer_hex2 Then
      count_base=count_base+1
      If layer\bank2<>0 Then count_data=count_data+1
    EndIf
    If layer\code=>0 Then
      If layer\bank3<>0 Then count_meta=count_meta+1
      If layer\bank4<>0 Then count_var=count_var+1
    EndIf
  Next

  count_total=1 + count_layer + count_image + count_geo + count_tile + count_anim + count_def + count_base + count_data + count_meta + count_var

  WriteByte  file,85            ;U
  WriteByte  file,77            ;M
  WriteByte  file,70            ;F
  WriteByte  file,version       ;version
  WriteInt   file,0             ;CRC32-checksum

  hash$=sha1$(map_passw$)
  For i=1 To 20
    WriteByte file,Asc(Mid$(hash$,i,1)) ;SHA1-fingerprint
  Next

  WriteShort file,count_total   ;count total
  WriteShort file,count_layer   ;count layer
  WriteShort file,count_image   ;count image
  WriteShort file,count_geo     ;count geo
  WriteShort file,count_tile    ;count tile
  WriteShort file,count_anim    ;count anim
  WriteShort file,count_base    ;count base
  WriteShort file,count_data    ;count data
  WriteShort file,count_meta    ;count meta
  WriteShort file,count_var     ;count var
  WriteShort file,count_def     ;count def

  For i=1 To 14
    WriteByte file,0            ;reserved
  Next


  ;TABLE--------------------------------------------------------------
  For tile=Each tile
    WriteInt file,26            ;block size (tileset)

    For i=1 To tile\anims
      bank=PeekInt(tile\banka,i*4-4)
      frames=PeekShort(bank,0)
      WriteInt file,13+frames*4 ;block size (animation)
    Next

    If tile\needdef=1 Then WriteInt file,4+tile\count  ;block size (default value table)
  Next

  For i=0 To layer_count
    layer=layer_list[i]

    If layer\code=layer_back  Then WriteInt file,36 ;block size (back)
    If layer\code=layer_map   Then WriteInt file,36 ;block size (map)
    If layer\code=layer_iso1  Then WriteInt file,34 ;block size (iso1)
    If layer\code=layer_iso2  Then WriteInt file,35 ;block size (iso2)
    If layer\code=layer_hex1  Then WriteInt file,35 ;block size (hex1)
    If layer\code=layer_hex2  Then WriteInt file,35 ;block size (hex2)
    If layer\code=layer_clone Then WriteInt file,26 ;block size (clone)
    If layer\code=layer_image Then WriteInt file,32 ;block size (image)
    If layer\code=layer_block Then WriteInt file,34 ;block size (block)
    If layer\code=layer_point Then WriteInt file,24 ;block size (point)
    If layer\code=layer_line  Then WriteInt file,32 ;block size (line)
    If layer\code=layer_rect  Then WriteInt file,32 ;block size (rect)
    If layer\code=layer_oval  Then WriteInt file,32 ;block size (oval)

    If layer\code=layer_map Or layer\code=layer_iso1 Or layer\code=layer_iso2 Or layer\code=layer_hex1 Or layer\code=layer_hex2 Then
      size=BankSize(layer\bank1)
      WriteInt file,8+size      ;block size (base)

      If layer\bank2<>0 Then
        size=BankSize(layer\bank2)
        WriteInt file,8+size    ;block size (data)
      EndIf
    EndIf

    If layer\code=>0 And layer\bank3<>0 Then
      size=BankSize(layer\bank3)
      WriteInt file,8+size      ;block size (meta)
    EndIf

    If layer\code=>0 And layer\bank4<>0 Then
      size=PeekInt(layer\bank4,0)
      WriteInt file,8+size      ;block size (var)
    EndIf
  Next


  ;TILESET------------------------------------------------------------
  For tile=Each tile
    WriteByte  file,100                        ;tileset signature
    WriteByte  file,(tile\mask Shr 16) And 255 ;tileset mask R
    WriteByte  file,(tile\mask Shr 08) And 255 ;tileset mask G
    WriteByte  file,(tile\mask Shr 00) And 255 ;tileset mask B
    WriteShort file,tile\sizex                 ;tileset width
    WriteShort file,tile\sizey                 ;tileset height
    WriteShort file,tile\factor                ;tileset factor
    WriteShort file,tile\anims                 ;tileset anims
    WriteShort file,tile\count                 ;tileset frames
    extra_writestr(file,12,tile\file$)         ;tileset imagefile

    ;ANIMATION--------------------------------------------------------
    For i=1 To tile\anims
      bank=PeekInt(tile\banka,i*4-4)
      size=BankSize(bank)
      WriteByte file,101                       ;animation signature
      WriteBytes bank,file,0,size              ;animation data
    Next

    ;DEFAULT VALUES---------------------------------------------------
    If tile\needdef=1 Then
      WriteByte  file,105                        ;default signature
      WriteShort file,tile\count                 ;frame count
      WriteByte  file,0                          ;*reserved*
      If tile\count>0 Then WriteBytes tile\bankd,file,0,tile\count    ;default values
    EndIf
  Next


  For i=0 To layer_count
    layer=layer_list[i]

    Select layer\code
      Case layer_back ;-----------------------------------------------
        WriteByte  file,layer_back                    ;background signature
        WriteByte  file,layer\visible                 ;background visible
        extra_writestr(file,12,layer_file$)           ;background imagefile
        WriteByte  file,layer\parax                   ;background parax
        WriteByte  file,layer\paray                   ;background paray
        WriteInt   file,layer\posx                    ;background posx
        WriteInt   file,layer\posy                    ;background posy
        WriteInt   file,layer\sizex                   ;background sizex
        WriteInt   file,layer\sizey                   ;background sizey
        WriteByte  file,layer\mode                    ;background mode
        WriteByte  file,(layer_colour Shr 16) And 255 ;background color R
        WriteByte  file,(layer_colour Shr 08) And 255 ;background color G
        WriteByte  file,(layer_colour Shr 00) And 255 ;background color B

      Case layer_map ;------------------------------------------------
        WriteByte  file,layer_map                     ;map signature
        WriteByte  file,layer\visible                 ;map visible
        extra_writestr(file,12,layer\name$)           ;map name
        WriteByte  file,layer\parax                   ;map parax
        WriteByte  file,layer\paray                   ;map paray
        WriteInt   file,layer\posx                    ;map posx
        WriteInt   file,layer\posy                    ;map posy
        WriteInt   file,layer\sizex                   ;map sizex
        WriteInt   file,layer\sizey                   ;map sizey
        WriteShort file,extra_tile2num(layer\tile)    ;map tileset
        WriteByte  file,layer\mask                    ;map mask
        WriteByte  file,layer\mode                    ;map endless tiling

      Case layer_iso1 ;-----------------------------------------------
        If demo=0 Then
          WriteByte  file,layer_iso1                    ;iso1 signature
          WriteByte  file,layer\visible                 ;iso1 visible
          extra_writestr(file,12,layer\name$)           ;iso1 name
          WriteByte  file,layer\parax                   ;iso1 parax
          WriteByte  file,layer\paray                   ;iso1 paray
          WriteInt   file,layer\posx                    ;iso1 posx
          WriteInt   file,layer\posy                    ;iso1 posy
          WriteInt   file,layer\sizex                   ;iso1 sizex
          WriteInt   file,layer\sizey                   ;iso1 sizey
          WriteShort file,extra_tile2num(layer\tile)    ;iso1 tileset
        EndIf

      Case layer_iso2 ;-----------------------------------------------
        If demo=0 Then
          WriteByte  file,layer_iso2                    ;iso2 signature
          WriteByte  file,layer\visible                 ;iso2 visible
          extra_writestr(file,12,layer\name$)           ;iso2 name
          WriteByte  file,layer\parax                   ;iso2 parax
          WriteByte  file,layer\paray                   ;iso2 paray
          WriteInt   file,layer\posx                    ;iso2 posx
          WriteInt   file,layer\posy                    ;iso2 posy
          WriteInt   file,layer\sizex                   ;iso2 sizex
          WriteInt   file,layer\sizey                   ;iso2 sizey
          WriteShort file,extra_tile2num(layer\tile)    ;iso2 tileset
          WriteByte  file,layer\start                   ;iso2 tileshift
        EndIf

      Case layer_hex1 ;-----------------------------------------------
        If demo=0 Then
          WriteByte  file,layer_hex1                    ;hex1 signature
          WriteByte  file,layer\visible                 ;hex1 visible
          extra_writestr(file,12,layer\name$)           ;hex1 name
          WriteByte  file,layer\parax                   ;hex1 parax
          WriteByte  file,layer\paray                   ;hex1 paray
          WriteInt   file,layer\posx                    ;hex1 posx
          WriteInt   file,layer\posy                    ;hex1 posy
          WriteInt   file,layer\sizex                   ;hex1 sizex
          WriteInt   file,layer\sizey                   ;hex1 sizey
          WriteShort file,extra_tile2num(layer\tile)    ;hex1 tileset
          WriteByte  file,layer\start                   ;hex1 tileshift
        EndIf

      Case layer_hex2 ;-----------------------------------------------
        If demo=0 Then
          WriteByte  file,layer_hex2                    ;hex2 signature
          WriteByte  file,layer\visible                 ;hex2 visible
          extra_writestr(file,12,layer\name$)           ;hex2 name
          WriteByte  file,layer\parax                   ;hex2 parax
          WriteByte  file,layer\paray                   ;hex2 paray
          WriteInt   file,layer\posx                    ;hex2 posx
          WriteInt   file,layer\posy                    ;hex2 posy
          WriteInt   file,layer\sizex                   ;hex2 sizex
          WriteInt   file,layer\sizey                   ;hex2 sizey
          WriteShort file,extra_tile2num(layer\tile)    ;hex2 tileset
          WriteByte  file,layer\start                   ;hex2 tileshift
        EndIf

      Case layer_clone ;----------------------------------------------
        WriteByte  file,layer_clone                   ;clone signature
        WriteByte  file,layer\visible                 ;clone visible
        extra_writestr(file,12,layer\name$)           ;clone name
        WriteByte  file,layer\parax                   ;clone parax
        WriteByte  file,layer\paray                   ;clone paray
        WriteInt   file,layer\posx                    ;clone posx
        WriteInt   file,layer\posy                    ;clone posy
        WriteShort file,extra_layer2num(layer\layer)  ;clone layer

      Case layer_image ;----------------------------------------------
        WriteByte  file,layer_image                   ;image signature
        WriteByte  file,layer\visible                 ;image visible
        extra_writestr(file,12,layer\name$)           ;image name
        WriteByte  file,layer\parax                   ;image parax
        WriteByte  file,layer\paray                   ;image paray
        WriteInt   file,layer\posx                    ;image posx
        WriteInt   file,layer\posy                    ;image posy
        WriteShort file,extra_tile2num(layer\tile)    ;image tileset
        WriteShort file,layer\frame                   ;image frame
        WriteShort file,layer\start                   ;image animstart
        WriteByte  file,layer\mode                    ;image animmode
        WriteByte  file,layer\mask                    ;image mask

      Case layer_block ;----------------------------------------------
        WriteByte  file,layer_block                   ;block signature
        WriteByte  file,layer\visible                 ;block visible
        extra_writestr(file,12,layer\name$)           ;block name
        WriteByte  file,layer\parax                   ;block justagex
        WriteByte  file,layer\paray                   ;block justagey
        WriteInt   file,layer\posx                    ;block posx
        WriteInt   file,layer\posy                    ;block posy
        WriteShort file,extra_tile2num(layer\tile)    ;block tileset
        WriteShort file,layer\frame                   ;block frame
        WriteShort file,layer\start                   ;block animstart
        WriteByte  file,layer\mode                    ;block animmode
        WriteByte  file,layer\mask                    ;block mask
        WriteShort file,extra_layer2num(layer\layer)  ;block layer

      Case layer_point ;----------------------------------------------
        WriteByte  file,layer_point                   ;point signature
        WriteByte  file,layer\visible                 ;point visible
        extra_writestr(file,12,layer\name$)           ;point name
        WriteByte  file,layer\parax                   ;point parax
        WriteByte  file,layer\paray                   ;point paray
        WriteInt   file,layer\posx                    ;point posx
        WriteInt   file,layer\posy                    ;point posy

      Case layer_line ;-----------------------------------------------
        WriteByte  file,layer_line                    ;line signature
        WriteByte  file,layer\visible                 ;line visible
        extra_writestr(file,12,layer\name$)           ;line name
        WriteByte  file,layer\parax                   ;line parax
        WriteByte  file,layer\paray                   ;line paray
        WriteInt   file,layer\posx                    ;line startposx
        WriteInt   file,layer\posy                    ;line startposy
        WriteInt   file,layer\sizex                   ;line endposx
        WriteInt   file,layer\sizey                   ;line endposy

      Case layer_rect ;-----------------------------------------------
        WriteByte  file,layer_rect                    ;rect signature
        WriteByte  file,layer\visible                 ;rect visible
        extra_writestr(file,12,layer\name$)           ;rect name
        WriteByte  file,layer\parax                   ;rect parax
        WriteByte  file,layer\paray                   ;rect paray
        WriteInt   file,layer\posx                    ;rect posx
        WriteInt   file,layer\posy                    ;rect posy
        WriteInt   file,layer\sizex                   ;rect sizex
        WriteInt   file,layer\sizey                   ;rect sizey

      Case layer_oval ;-----------------------------------------------
        WriteByte  file,layer_oval                    ;oval signature
        WriteByte  file,layer\visible                 ;oval visible
        extra_writestr(file,12,layer\name$)           ;oval name
        WriteByte  file,layer\parax                   ;oval parax
        WriteByte  file,layer\paray                   ;oval paray
        WriteInt   file,layer\posx                    ;oval posx
        WriteInt   file,layer\posy                    ;oval posy
        WriteInt   file,layer\sizex                   ;oval radiusx
        WriteInt   file,layer\sizey                   ;oval radiusy
    End Select

    If layer\code=layer_map Or layer\code=layer_iso1 Or layer\code=layer_iso2 Or layer\code=layer_hex1 Or layer\code=layer_hex2 Then
      size=BankSize(layer\bank1)
      WriteByte  file,102                             ;basedata signature
      WriteByte  file,layer\depth1                    ;basedata depth
      WriteInt   file,size                            ;basedata size
      WriteShort file,0                               ;basedata reserved
      WriteBytes layer\bank1,file,0,size              ;basedata bytes

      If layer\bank2<>0 Then
        size=BankSize(layer\bank2)
        WriteByte  file,103                           ;datalayer signature
        WriteByte  file,layer\depth2                  ;datalayer depth
        WriteInt   file,size                          ;datalayer size
        WriteShort file,0                             ;datalayer reserved
        WriteBytes layer\bank2,file,0,size            ;datalayer bytes
      EndIf
    EndIf

    If layer\code=>0 And layer\bank3<>0 And demo=0 Then
      size=BankSize(layer\bank3)
      WriteByte  file,104                             ;metadata signature
      WriteByte  file,layer\ascii                     ;metadata ascii
      WriteInt   file,size                            ;metadata size
      WriteShort file,0                               ;metadata reserved
      WriteBytes layer\bank3,file,0,size              ;metadata bytes
    EndIf

    If layer\code=>0 And layer\bank4<>0 And demo=0 Then
      count=BankSize(layer\bank4)/4-1
      size =PeekInt (layer\bank4,0)
      WriteByte  file,106                             ;variable signature
      WriteByte  file,0                               ;variable reserved
      WriteShort file,count                           ;variable count
      WriteInt   file,size                            ;variable size
      For j=1 To count
        bank=PeekInt(layer\bank4,j*4)
        WriteBytes bank,file,0,BankSize(bank)
      Next
    EndIf
  Next


  pos=FilePos(file)
  If (pos Mod 4)>0 Then
    For i=1 To 4-(pos Mod 4)
      WriteByte file,0
    Next
  EndIf


  CloseFile file

  If map_passw$<>"" Then
    crypt_file(map_tmp$, name$, map_passw$)
    DeleteFile map_tmp$
  EndIf

  crc32=crc32_file(name$)

  file=OpenFile(name$)
  If file<>0 Then
    SeekFile file,4
    WriteInt file,crc32
    CloseFile file
  EndIf
End Function