Dim map_block(0)
Dim map_offset(0)

Global map_ascii
Global map_backgr$
Global map_bank3
Global map_bank4
Global map_colour
Global map_file$
Global map_height
Global map_image
Global map_mode
Global map_parax
Global map_paray
Global map_path$
Global map_posx
Global map_posy
Global map_scrollx
Global map_scrolly
Global map_sizex
Global map_sizey
Global map_tmp$=SystemProperty$("TEMPDIR")+"data.tmp"
Global map_visible
Global map_width
Global map_x
Global map_y





;---------------------------------------------------------------------
Function map_draw()
  Local b
  Local g
  Local layer.layer
  Local px
  Local py
  Local r

  Viewport map_x, map_y, map_width, map_height
  Origin map_x, map_y

  If map_visible=1 Then
    If map_mode=1 Then
      r=(map_colour And $FF0000)/$10000
      g=(map_colour And $FF00)/$100
      b=(map_colour And $FF)
      ClsColor r,g,b
      Cls
    ElseIf map_mode=2 And map_image<>0 Then
      px=map_getscreen(map_posx,map_parax,map_scrollx)
      py=map_getscreen(map_posy,map_paray,map_scrolly)
      TileBlock map_image,px,py
    ElseIf map_mode=3 And map_image<>0 Then
      px=map_getscreen(map_posx,map_parax,map_scrollx)
      py=map_getscreen(map_posy,map_paray,map_scrolly)
      TileImage map_image,px,py
    EndIf
  EndIf

  For layer=Each layer
    If layer\visible=1 Then
      Select layer\code
        Case layer_map
          If layer\mode=0 Then
            layer_map_draw(layer)
          Else
            layer_map_draw_repeat(layer)
          EndIf
        Case layer_iso1
          layer_iso1_draw(layer)
        Case layer_iso2
          layer_iso2_draw(layer)
        Case layer_hex1
          layer_hex1_draw(layer)
        Case layer_hex2
          layer_hex2_draw(layer)
        Case layer_clone
          layer_clone_draw(layer)
        Case layer_image
          layer_image_draw(layer)
        Case layer_block
          layer_block_draw(layer)
      End Select
    EndIf
  Next
End Function





;---------------------------------------------------------------------
;screen: screen coordinate
;para:   parallax value (0-200)
;scroll: scroll position
;RETURN: global coordinate
;---------------------------------------------------------------------
Function map_getcoord(screen,para,scroll)
  If para=100 Then
    Return screen+scroll
  ElseIf para=0 Then
    Return screen
  Else
    Return screen+scroll*para/100
  EndIf
End Function





;---------------------------------------------------------------------
;coord:  global coordinate
;para:   parallax value (0-200)
;scroll: scroll position
;RETURN: screen coordinate
;---------------------------------------------------------------------
Function map_getscreen(coord,para,scroll)
  If para=100 Then
    Return coord-scroll
  ElseIf para=0 Then
    Return coord
  Else
    Return coord-scroll*para/100
  EndIf
End Function





;---------------------------------------------------------------------
;name:   map file name
;passw:  map password
;check:  checksum   (0=no, 1=yes)
;RETURN: error code (0=ok, 1=file not found, 2=file size corrupted, 3=no read access, 4=not valid file, 5=checksum problem, 6=password problem, 7=load image problem)
;        error code 7 do not free data or images!!!
;---------------------------------------------------------------------
Function map_load(name$,passw$="",check=0)
  Local bank
  Local bank4
  Local bytes
  Local checksum
  Local code
  Local count_anim
  Local count_base
  Local count_data
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
  Local error
  Local file
  Local frames
  Local fsize
  Local geo.geo
  Local hash$
  Local i
  Local j
  Local layer.layer
  Local maxsize
  Local proversion
  Local size1
  Local size2
  Local subversion
  Local tile.tile
  Local version

  map_reset()

  If name$="" Then Return 1

  If FileType(name$)<>1 Then Return 1

  fsize=FileSize(name$)
  If fsize<64 Then Return 2

  If check=1 Then crc32=crc32_file(name$)

  file=ReadFile(name$)
  If file=0 Then Return 3


  ;HEADER-------------------------------------------------------------
  If ReadByte(file)<>85 Or ReadByte(file)<>77 Or ReadByte(file)<>70 Then
    CloseFile file
    Return 4
  EndIf

  version    =ReadByte(file)
  checksum   =ReadInt(file)
  crypted    =Sgn(version And 128)
  proversion =Sgn(version And 64)
  subversion =(version And 63)

  If check=1 And checksum<>crc32 Then Return 5

  For i=1 To 20
    hash$=hash$+Chr$(ReadByte(file))
  Next

  If crypted=1 Then
    CloseFile file

    If passw$="" Then Return 6
    If sha1$(passw$)<>hash$ Then Return 6

    crypt_file(name$, map_tmp$, passw$)
    file=ReadFile(map_tmp$)
    If file=0 Then Return 3
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

  dummybytes=18
  If subversion=>3 Then count_var=ReadShort(file):dummybytes=16

  For i=1 To dummybytes
    dummy=ReadByte(file)
  Next


  ;TABLE--------------------------------------------------------------
  maxsize=count_total*4+64
  If maxsize>fsize Then error=2 : Goto abort

  Dim map_block (count_total)
  Dim map_offset(count_total)

  For i=1 To count_total
    map_block (i)=ReadInt(file)
    map_offset(i)=maxsize
    maxsize=maxsize+map_block(i)
  Next

  If (maxsize Mod 4)>0 Then maxsize=maxsize+4-(maxsize Mod 4)
  If maxsize<>fsize Then error=2 : Goto abort


  ;BLOCKS-------------------------------------------------------------
  For i=1 To count_total
    SeekFile file,map_offset(i)
    code=ReadByte(file)

    Select code
      Case layer_back ;-----------------------------------------------
        map_visible =ReadByte(file)
        map_backgr$ =extra_readstr(file,12)
        map_parax   =ReadByte(file)
        map_paray   =ReadByte(file)
        map_posx    =ReadInt (file)
        map_posy    =ReadInt (file)
        map_sizex   =ReadInt (file)
        map_sizey   =ReadInt (file)
        map_mode    =ReadByte(file)
        map_colour  =(ReadByte(file) Shl 16) + (ReadByte(file) Shl 8) + ReadByte(file)
        geo=Null

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
        geo=Null

      Case layer_iso1 ;-----------------------------------------------
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
        geo=Null

      Case layer_iso2 ;-----------------------------------------------
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
        geo=Null

      Case layer_hex1 ;-----------------------------------------------
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
        geo=Null

      Case layer_hex2 ;-----------------------------------------------
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
        geo=Null

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
        geo=Null

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
        geo=Null

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
        geo=Null

      Case layer_point ;----------------------------------------------
        geo         =New geo
        geo\code    =code
        geo\visible =ReadByte(file)
        geo\name$   =extra_readstr(file,12)
        geo\parax   =ReadByte(file)
        geo\paray   =ReadByte(file)
        geo\posx    =ReadInt (file)
        geo\posy    =ReadInt (file)
        layer=Null

      Case layer_line ;-----------------------------------------------
        geo         =New geo
        geo\code    =code
        geo\visible =ReadByte(file)
        geo\name$   =extra_readstr(file,12)
        geo\parax   =ReadByte(file)
        geo\paray   =ReadByte(file)
        geo\posx    =ReadInt (file)
        geo\posy    =ReadInt (file)
        geo\sizex   =ReadInt (file)
        geo\sizey   =ReadInt (file)
        layer=Null

      Case layer_rect ;-----------------------------------------------
        geo         =New geo
        geo\code    =code
        geo\visible =ReadByte(file)
        geo\name$   =extra_readstr(file,12)
        geo\parax   =ReadByte(file)
        geo\paray   =ReadByte(file)
        geo\posx    =ReadInt (file)
        geo\posy    =ReadInt (file)
        geo\sizex   =ReadInt (file)
        geo\sizey   =ReadInt (file)
        layer=Null

      Case layer_oval ;-----------------------------------------------
        geo         =New geo
        geo\code    =code
        geo\visible =ReadByte(file)
        geo\name$   =extra_readstr(file,12)
        geo\parax   =ReadByte(file)
        geo\paray   =ReadByte(file)
        geo\posx    =ReadInt (file)
        geo\posy    =ReadInt (file)
        geo\sizex   =ReadInt (file)
        geo\sizey   =ReadInt (file)
        layer=Null

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

      Case 101 ;animation---------------------------------------------
        If tile<>Null Then
          frames =ReadShort(file)
          bytes  =frames*4+12

          bank=CreateBank(bytes)
          PokeShort bank,0,frames
          ReadBytes bank,file,2,bytes-2

          bytes=BankSize(tile\banka)
          ResizeBank tile\banka,bytes+4
          PokeInt tile\banka,bytes,bank
        EndIf

      Case 102 ;basedata----------------------------------------------
        If layer<>Null Then
          If layer\bank1<>0 Then FreeBank layer\bank1
          layer\depth1=ReadByte  (file)
          bytes       =ReadInt   (file)
          dummy       =ReadShort (file)
          layer\bank1 =CreateBank(bytes)
          ReadBytes layer\bank1,file,0,bytes
        EndIf

      Case 103 ;datalayer---------------------------------------------
        If layer<>Null Then
          If layer\bank2<>0 Then FreeBank layer\bank2
          layer\depth2=ReadByte  (file)
          bytes       =ReadInt   (file)
          dummy       =ReadShort (file)
          layer\bank2 =CreateBank(bytes)
          ReadBytes layer\bank2,file,0,bytes
        EndIf

      Case 104 ;metadata----------------------------------------------
        If layer<>Null Then
          If layer\bank3<>0 Then FreeBank layer\bank3
          layer\ascii =ReadByte  (file)
          bytes       =ReadInt   (file)
          dummy       =ReadShort (file)
          layer\bank3 =CreateBank(bytes)
          ReadBytes layer\bank3,file,0,bytes
        ElseIf geo<>Null Then
          If geo\bank3<>0 Then FreeBank geo\bank3
          geo\ascii =ReadByte  (file)
          bytes     =ReadInt   (file)
          dummy     =ReadShort (file)
          geo\bank3 =CreateBank(bytes)
          ReadBytes geo\bank3,file,0,bytes
        ElseIf code=layer_back Then
          If map_bank3<>0 Then FreeBank map_bank3
          map_ascii =ReadByte  (file)
          bytes     =ReadInt   (file)
          dummy     =ReadShort (file)
          map_bank3 =CreateBank(bytes)
          ReadBytes map_bank3,file,0,bytes
        EndIf

      Case 105 ;default tile values-----------------------------------
        If tile<>Null Then
          frames=ReadShort(file)
          dummy =ReadByte(file)
          tile\bankd=CreateBank(frames)
          If frames>0 Then ReadBytes tile\bankd,file,0,frames
        EndIf

      Case 106 ;variables---------------------------------------------
        dummy=ReadByte (file)
        count=ReadShort(file)
        bytes=ReadInt  (file)
        bank4=CreateBank(count*4)

        If layer<>Null Then
          If layer\bank4<>0 Then FreeBank layer\bank4
          layer\bank4=bank4
        ElseIf geo<>Null Then
          If geo\bank4<>0 Then FreeBank geo\bank4
          geo\bank4=bank4
        ElseIf code=layer_back Then
          If map_bank4<>0 Then FreeBank map_bank4
          map_bank4=bank4
        EndIf

        For j=1 To count
          size1=ReadByte(file)
          size2=ReadByte(file)
          bank=CreateBank(2+size1+size2)
          PokeByte bank,0,size1
          PokeByte bank,1,size2
          ReadBytes bank,file,2,size1
          ReadBytes bank,file,2+size1,size2
          PokeInt bank4,j*4-4,bank
        Next

      Default ;-------------------------------------------------------
        geo  =Null
        layer=Null
        tile =Null

    End Select
  Next


  ;FINAL--------------------------------------------------------------
  For layer=Each layer
    If layer\code=layer_map Or layer\code=layer_iso1 Or layer\code=layer_iso2 Or layer\code=layer_hex1 Or layer\code=layer_hex2 Or layer\code=layer_image Or layer\code=layer_block Then
      layer\tile=extra_num2tile(layer\tmp)
      layer\tmp=0
    EndIf

    If layer\code=layer_clone Then
      layer\layer=extra_num2layer(layer\tmp)
      layer\tmp=0
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

  If tile_load()>0 Then error=7
  tile_animreset()

  .abort
  Dim map_block (0)
  Dim map_offset(0)

  CloseFile file
  If crypted=1 Then
    crypt_overwrite(map_tmp$)
    DeleteFile map_tmp$
  EndIf
  Return error
End Function





;---------------------------------------------------------------------
;RETURN: mouse x in map viewport
;---------------------------------------------------------------------
Function map_mousex()
  Return MouseX()-map_x ;<<<modify, if you use canvas
End Function





;---------------------------------------------------------------------
;RETURN: mouse y in map viewport
;---------------------------------------------------------------------
Function map_mousey()
  Return MouseY()-map_y ;<<<modify, if you use canvas
End Function





;---------------------------------------------------------------------
Function map_reset()
  Local bank
  Local i
  Local size

  geo_reset()
  layer_reset()
  tile_reset()

  If map_image<>0 Then FreeImage map_image 

  If map_bank3<>0 Then FreeBank map_bank3

  If map_bank4<>0 Then
    size=BankSize(map_bank4)
    For i=0 To size/4-1
      bank=PeekInt(map_bank4,i*4)
      FreeBank bank
    Next
   FreeBank map_bank4
  EndIf

  map_ascii   =0
  map_backgr$ =""
  map_bank3   =0
  map_colour  =0
  map_file$   =""
  map_height  =GraphicsHeight()
  map_image   =0
  map_mode    =0
  map_parax   =100
  map_paray   =100
  map_path$   =""
  map_posx    =0
  map_posy    =0
  map_scrollx =0
  map_scrolly =0
  map_sizex   =0
  map_sizey   =0
  map_visible =0
  map_width   =GraphicsWidth()
  map_x       =0
  map_y       =0
End Function





;---------------------------------------------------------------------
;x: map position x
;y: map position y
;---------------------------------------------------------------------
Function map_scroll(x,y)
  map_scrollx=x
  map_scrolly=y
End Function





;---------------------------------------------------------------------
;x:      map start x
;y:      map start y
;width:  map width
;height: map height
;---------------------------------------------------------------------
Function map_viewport(x,y,width,height)
  map_x=x
  map_y=y
  map_width=width
  map_height=height
End Function