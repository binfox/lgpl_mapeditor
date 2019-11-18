Module Jan.mapengine
'rem
ModuleInfo "Version: 0.8"
ModuleInfo "Author: Jan_(Jan Kuhnert)"
ModuleInfo "License: Public Domain"
ModuleInfo "Copyright: Keins, aber Credits waehren nett."
'EndRem

Import brl.max2d
'Import brl.glmax2d
'Import brl.d3d7max2d
Import jan.fps
Import brl.pngloader
Import brl.jpgloader
Import brl.bank
Import brl.bmploader
Import brl.retro

Private


Public

Include "bbtype.bmx"

'Sourcefile: mapengine.bmx
'{

'BlitzBasic Styled Typ
Global crc32_table%[255+1]
'---------------------------------------------------------------------
'name:   file name
'RETURN: crc32 value
'---------------------------------------------------------------------
Function crc32_file(name$)
  Local bank:TBank
  Local Byten
  Local bytes
  Local crc32
  Local file
  Local i
  Local j
  Local Size

  crc32_init()
  crc32=$FFFFFFFF

  Size=FileSize(name$)
  If Size<8 Then Return

  file=ReadFile(name$)
  If file=0 Then Return
  SeekStream file,8
  bank=CreateBank($00004000)

  For j=1 To (Size-8)/$00004000
    ReadBank bank,file,0,$00004000
    For i=0 To $00003FFF
      Byten=PeekByte(bank,i)
      'Print "b:"+Byten
      crc32=(crc32 Shr 8) ~ crc32_table%[Byten ~ (crc32 & $000000FF)]
      'Print "c:"+crc32
    Next
  Next

  bytes=(Size-8) Mod $00004000
  If bytes>0 Then
    ReadBank bank,file,0,bytes
    For i=0 To bytes-1
      Byten=PeekByte(bank,i)
      crc32=(crc32 Shr 8) ~ crc32_table%[Byten ~ (crc32 & $000000FF)]
    Next
  EndIf

  CloseFile file
  bank=Null
  Return ~crc32
End Function





'---------------------------------------------------------------------
Function crc32_init()
  Local i
  Local j
  Local value

  For i=0 To 255
    value=i
    For j=0 To 7
      If (value & 1) Then 
        value=(value Shr 1) ~ $EDB88320
      Else
        value=(value Shr 1)
      EndIf
    Next
    crc32_table[i]=value
  Next
End Function
'}crc32.bmx

'Sourcefile: crypt.bmx
'{
'Import "bbtype.bmx"
'Import "bbvkey.bmx"

Global crypt_table[63+1]





'---------------------------------------------------------------------
'source: source file
'dest:   dest file
'passw:  password string
'---------------------------------------------------------------------
Function crypt_file(source$,dest$,passw$)
  Local bank:TBank
  Local bytes
  Local file1
  Local file2
  Local Size
  Local i
  Local j
  Local Pos
  Local value
'DebugStop
  crypt_init(passw$)

  Size=FileSize(source$)
  If Size<64 Then Return

  file1=ReadFile(source$)
  If file1=0 Then Return

  file2=WriteFile(dest$)
  If file2=0 Then CloseFile file1 ; Return

  bank=CreateBank($4000)
  ReadBank  bank,file1,0,64
  WriteBank bank,file2,0,64

  For j=1 To (Size-64)/$4000
    ReadBank bank,file1,0,$4000
    For i=0 To $3FFF Step 4
      value=PeekInt(bank,i)
      PokeInt bank,i,(value ~ crypt_table[Pos])
      Pos=(Pos+1) Mod 64
    Next
    WriteBank bank,file2,0,$4000
  Next

  bytes=(Size-64) Mod $4000
  If bytes>0 Then
    ReadBank bank,file1,0,bytes
    For i=0 To bytes-1 Step 4
      value=PeekInt(bank,i)
      PokeInt bank,i,(value ~ crypt_table[Pos])
      Pos=(Pos+1) Mod 64
    Next
    WriteBank bank,file2,0,bytes
  EndIf

  CloseFile file1
  CloseFile file2
  bank=Null
End Function





'---------------------------------------------------------------------
'passw: password string
'---------------------------------------------------------------------
Function crypt_init(passw$)
  Local ascii
  Local Byten
  Local i
  Local j
  Local key$
  Local Pos

  key$=md5$(passw$)
'Print key$
  For j=0 To 15
    For i=0 To 15
      Pos=(j*16+i)/4
      ascii=Asc(Mid$(key$,i+1,1))
      Byten=ascii Shl ((i Mod 4)*8)
	  'Print ascii
      crypt_table[Pos]=crypt_table[Pos] | Byten

    Next
    key$=md5$(key$)
  Next
'Print "ende"
End Function





'---------------------------------------------------------------------
'name: tmp file name
'---------------------------------------------------------------------
Function crypt_overwrite(name$)
  Local bank:TBank
  Local bytes
  Local file
  Local i
  Local Size

  Size=FileSize(name$)
  file=OpenFile(name$)
  If file=0 Then Return

  bank=CreateBank($4000)
  For i=0 To $3FFF
    PokeByte bank,i,Rand(0,255)
  Next

  For i=1 To Size/$4000
    WriteBank bank,file,0,$4000
  Next

  bytes=Size Mod $4000
  If bytes>0 Then WriteBank bank,file,0,bytes

  CloseFile file
  bank=Null
End Function
'}crypt.bmx

'Sourcefile: map.bmx
'{
'Import "bbtype.bmx"
'Import "bbvkey.bmx"

Global map_block[0+1]
Global map_offset[0+1]

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
Global map_tmp$="data.tmp"
Global map_visible
Global map_width
Global map_x
Global map_y





'---------------------------------------------------------------------
Function map_draw()
  Local b%
  Local g%
  Local layer:bblayer
  Local px%
  Local py%
  Local r%

  SetViewport map_x, map_y, map_width, map_height
  SetOrigin map_x, map_y

  If map_visible=1 Then
    If map_mode=1 Then
      r=(map_colour & $FF0000)/$10000
      g=(map_colour & $FF00)/$100
      b=(map_colour & $FF)
      SetClsColor r,g,b
      Cls
    ElseIf map_mode=2 And map_image<>0 Then
      px=map_getscreen(map_posx,map_parax,map_scrollx)
      py=map_getscreen(map_posy,map_paray,map_scrolly)
      TileImage map_image,px,py
    ElseIf map_mode=3 And map_image<>0 Then
      px=map_getscreen(map_posx,map_parax,map_scrollx)
      py=map_getscreen(map_posy,map_paray,map_scrolly)
      TileImage map_image,px,py
    EndIf
  EndIf

  For layer=EachIn layer_list
    If layer.visible=1 Then
      Select layer.code
        Case layer_map
          If layer.mode=0 Then
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





'---------------------------------------------------------------------
'screen: screen coordinate
'para:   parallax value (0-200)
'scroll: scroll position
'RETURN: global coordinate
'---------------------------------------------------------------------
Function map_getcoord(screen,para,scroll)
  If para=100 Then
    Return screen+scroll
  ElseIf para=0 Then
    Return screen
  Else
    Return screen+scroll*para/100
  EndIf
End Function





'---------------------------------------------------------------------
'coord:  global coordinate
'para:   parallax value (0-200)
'scroll: scroll position
'RETURN: screen coordinate
'---------------------------------------------------------------------
Function map_getscreen(coord,para,scroll)
  If para=100 Then
    Return coord-scroll
  ElseIf para=0 Then
    Return coord
  Else
    Return coord-scroll*para/100
  EndIf
End Function





'---------------------------------------------------------------------
'name:   map file name
'passw:  map password
'check:  checksum   (0=no, 1=yes)
'RETURN: error code (0=ok, 1=file not found, 2=file size corrupted, 3=no read access, 4=not valid file, 5=checksum problem, 6=password problem, 7=load image problem)
'        error code 7 do not free data or images!!!
'---------------------------------------------------------------------
Function map_load(name$,passw$="",check=0)
  Local bank
  Local bank4:TBank
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
  Local geo:bbgeo
  Local hash$
  Local i
  Local j
  Local layer:bblayer
  Local maxsize
  Local proversion
  Local size1
  Local size2
  Local subversion
  Local tile:bbtile
  Local version
  Local crc32
  Local count

  map_reset()

  If name$="" Then Return 1

  If FileType(name$)<>1 Then Return 1

  fsize=FileSize(name$)
  'Print "Filesize: "+fsize
  If fsize<64 Then Return 2

  If check=1 Then crc32=crc32_file(name$)

  file=ReadFile(name$)
  If file=0 Then Return 3


  'HEADER-------------------------------------------------------------
  If ReadByte(file)<>85 Or ReadByte(file)<>77 Or ReadByte(file)<>70 Then
    CloseFile file
    Return 4
  EndIf

  version    =ReadByte(file)
  checksum   =ReadInt(file)
  crypted    =Sgn(version & 128)
  proversion =Sgn(version & 64)
  subversion =(version & 63)

  If check=1 And checksum<>crc32 Then Return 5

  For i=1 To 20
    hash$=hash$+Chr$(ReadByte(file))
  Next

  If crypted=1 Then
    CloseFile file

    If passw$="" Then Return 6
	'DebugStop
	'Print sha1$(passw$)' +" - "+hash$
  '  If sha1$(passw$)<>hash$ Then Return 6

    crypt_file(name$, map_tmp$, passw$)
    file=ReadFile(map_tmp$)
    If file=0 Then Return 3
    SeekStream file,28
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
  If subversion=>3 Then count_var=ReadShort(file);dummybytes=16

  For i=1 To dummybytes
    dummy=ReadByte(file)
  Next


  'TABLE--------------------------------------------------------------
  maxsize=count_total*4+64
  If maxsize>fsize Then
	error=2 '; Goto abort
	map_block=New Int[0+1]
    map_block=New Int[0+1]
    CloseFile file
    If crypted=1 Then
      crypt_overwrite(map_tmp$)
      DeleteFile map_tmp$
    EndIf
    Return error
  EndIf
  map_offset=New Int[count_total+1]
  map_block=New Int[count_total+1]
  'Global map_block [count_total+1]
  'Global map_offset[count_total+1]

  For i=1 To count_total
    map_block [i]=ReadInt(file)
    map_offset[i]=maxsize
    maxsize=maxsize+map_block[i]
  Next

  If (maxsize Mod 4)>0 Then maxsize=maxsize+4-(maxsize Mod 4)
  If maxsize<>fsize Then
    error=2' ; Goto abort
	map_block=New Int[0+1]
    map_block=New Int[0+1]
    CloseFile file
    If crypted=1 Then
      crypt_overwrite(map_tmp$)
      DeleteFile map_tmp$
    EndIf
    Return error
  EndIf


  'BLOCKS-------------------------------------------------------------
  For i=1 To count_total
    SeekStream file,map_offset[i]
    code=ReadByte(file)

    Select code
      Case layer_back '-----------------------------------------------
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

      Case layer_map '------------------------------------------------
        layer         =New bblayer
        layer.code    =code
        layer.visible =ReadByte (file)
        layer.name$   =extra_readstr(file,12)
        layer.parax   =ReadByte (file)
        layer.paray   =ReadByte (file)
        layer.posx    =ReadInt  (file)
        layer.posy    =ReadInt  (file)
        layer.sizex   =ReadInt  (file)
        layer.sizey   =ReadInt  (file)
        layer.tmp     =ReadShort(file)
        layer.mask    =ReadByte (file)
        If subversion=>3 Then layer.mode=ReadByte(file)
        geo=Null

      Case layer_iso1 '-----------------------------------------------
        layer         =New bblayer
        layer.code    =code
        layer.visible =ReadByte (file)
        layer.name$   =extra_readstr(file,12)
        layer.parax   =ReadByte (file)
        layer.paray   =ReadByte (file)
        layer.posx    =ReadInt  (file)
        layer.posy    =ReadInt  (file)
        layer.sizex   =ReadInt  (file)
        layer.sizey   =ReadInt  (file)
        layer.tmp     =ReadShort(file)
        geo=Null

      Case layer_iso2 '-----------------------------------------------
        layer         =New bblayer
        layer.code    =code
        layer.visible =ReadByte (file)
        layer.name$   =extra_readstr(file,12)
        layer.parax   =ReadByte (file)
        layer.paray   =ReadByte (file)
        layer.posx    =ReadInt  (file)
        layer.posy    =ReadInt  (file)
        layer.sizex   =ReadInt  (file)
        layer.sizey   =ReadInt  (file)
        layer.tmp     =ReadShort(file)
        layer.start   =ReadByte (file)
        geo=Null

      Case layer_hex1 '-----------------------------------------------
        layer         =New bblayer
        layer.code    =code
        layer.visible =ReadByte (file)
        layer.name$   =extra_readstr(file,12)
        layer.parax   =ReadByte (file)
        layer.paray   =ReadByte (file)
        layer.posx    =ReadInt  (file)
        layer.posy    =ReadInt  (file)
        layer.sizex   =ReadInt  (file)
        layer.sizey   =ReadInt  (file)
        layer.tmp     =ReadShort(file)
        layer.start   =ReadByte (file)
        geo=Null

      Case layer_hex2 '-----------------------------------------------
        layer         =New bblayer
        layer.code    =code
        layer.visible =ReadByte (file)
        layer.name$   =extra_readstr(file,12)
        layer.parax   =ReadByte (file)
        layer.paray   =ReadByte (file)
        layer.posx    =ReadInt  (file)
        layer.posy    =ReadInt  (file)
        layer.sizex   =ReadInt  (file)
        layer.sizey   =ReadInt  (file)
        layer.tmp     =ReadShort(file)
        layer.start   =ReadByte (file)
        geo=Null

      Case layer_clone '----------------------------------------------
        layer         =New bblayer
        layer.code    =code
        layer.visible =ReadByte (file)
        layer.name$   =extra_readstr(file,12)
        layer.parax   =ReadByte (file)
        layer.paray   =ReadByte (file)
        layer.posx    =ReadInt  (file)
        layer.posy    =ReadInt  (file)
        layer.tmp     =ReadShort(file)
        geo=Null

      Case layer_image '----------------------------------------------
        layer         =New bblayer
        layer.code    =code
        layer.visible =ReadByte (file)
        layer.name$   =extra_readstr(file,12)
        layer.parax   =ReadByte (file)
        layer.paray   =ReadByte (file)
        layer.posx    =ReadInt  (file)
        layer.posy    =ReadInt  (file)
        layer.tmp     =ReadShort(file)
        layer.Frame   =ReadShort(file)
        layer.start   =ReadShort(file)
        layer.mode    =ReadByte (file)
        layer.mask    =ReadByte (file)
        geo=Null

      Case layer_block '----------------------------------------------
        layer         =New bblayer
        layer.code    =code
        layer.visible =ReadByte (file)
        layer.name$   =extra_readstr(file,12)
        layer.parax   =ReadByte (file)
        layer.paray   =ReadByte (file)
        layer.posx    =ReadInt  (file)
        layer.posy    =ReadInt  (file)
        layer.tmp     =ReadShort(file)
        layer.Frame   =ReadShort(file)
        layer.start   =ReadShort(file)
        layer.mode    =ReadByte (file)
        layer.mask    =ReadByte (file)
        layer.depth2  =ReadShort(file) '<TMP
        geo=Null

      Case layer_point '----------------------------------------------
        geo         =New bbgeo
        geo.code    =code
        geo.visible =ReadByte(file)
        geo.name$   =extra_readstr(file,12)
        geo.parax   =ReadByte(file)
        geo.paray   =ReadByte(file)
        geo.posx    =ReadInt (file)
        geo.posy    =ReadInt (file)
        layer=Null

      Case layer_line '-----------------------------------------------
        geo         =New bbgeo
        geo.code    =code
        geo.visible =ReadByte(file)
        geo.name$   =extra_readstr(file,12)
        geo.parax   =ReadByte(file)
        geo.paray   =ReadByte(file)
        geo.posx    =ReadInt (file)
        geo.posy    =ReadInt (file)
        geo.sizex   =ReadInt (file)
        geo.sizey   =ReadInt (file)
        layer=Null

      Case layer_rect '-----------------------------------------------
        geo         =New bbgeo
        geo.code    =code
        geo.visible =ReadByte(file)
        geo.name$   =extra_readstr(file,12)
        geo.parax   =ReadByte(file)
        geo.paray   =ReadByte(file)
        geo.posx    =ReadInt (file)
        geo.posy    =ReadInt (file)
        geo.sizex   =ReadInt (file)
        geo.sizey   =ReadInt (file)
        layer=Null

      Case layer_oval '-----------------------------------------------
        geo         =New bbgeo
        geo.code    =code
        geo.visible =ReadByte(file)
        geo.name$   =extra_readstr(file,12)
        geo.parax   =ReadByte(file)
        geo.paray   =ReadByte(file)
        geo.posx    =ReadInt (file)
        geo.posy    =ReadInt (file)
        geo.sizex   =ReadInt (file)
        geo.sizey   =ReadInt (file)
        layer=Null

      Case 100 'tileset-----------------------------------------------
        tile        =New bbtile
        tile.mask   =(ReadByte(file) Shl 16) + (ReadByte(file) Shl 8) + ReadByte(file)
        tile.sizex  =ReadShort(file)
        tile.sizey  =ReadShort(file)
        tile.factor =ReadShort(file)
        tile.anims  =ReadShort(file)
        tile.count  =ReadShort(file)
        tile.file$  =extra_readstr(file,12)
        tile.banka  =CreateBank(0)

      Case 101 'animation---------------------------------------------
        If tile<>Null Then
          frames =ReadShort(file)
          bytes  =frames*4+12

          bank=CreateBank(bytes)
          PokeShort bank,0,frames
          ReadBank bank,file,2,bytes-2

          bytes=BankSize(tile.banka)
          ResizeBank tile.banka,bytes+4
          PokeInt tile.banka,bytes,bank

        EndIf

      Case 102 'basedata----------------------------------------------
        If layer<>Null Then
          If Not(layer.bank1=Null) Then layer.bank1=Null
          layer.depth1=ReadByte  (file)
          bytes       =ReadInt   (file)
          dummy       =ReadShort (file)
          layer.bank1 =CreateBank(bytes)
          ReadBank layer.bank1,file,0,bytes
        EndIf

      Case 103 'datalayer---------------------------------------------
        If layer<>Null Then
          If Not(layer.bank2=Null) Then layer.bank2=Null
          layer.depth2=ReadByte  (file)
          bytes       =ReadInt   (file)
          dummy       =ReadShort (file)
          layer.bank2 =CreateBank(bytes)
          ReadBank layer.bank2,file,0,bytes
        EndIf

      Case 104 'metadata----------------------------------------------
        If layer<>Null Then
          If Not(layer.bank3=Null) Then layer.bank3=Null
          layer.ascii =ReadByte  (file)
          bytes       =ReadInt   (file)
          dummy       =ReadShort (file)
          layer.bank3 =CreateBank(bytes)
          ReadBank layer.bank3,file,0,bytes
        ElseIf geo<>Null Then
          If geo.bank3<>0 Then geo.bank3=Null
          geo.ascii =ReadByte  (file)
          bytes     =ReadInt   (file)
          dummy     =ReadShort (file)
          geo.bank3 =CreateBank(bytes)
          ReadBank geo.bank3,file,0,bytes
        ElseIf code=layer_back Then
          If map_bank3<>0 Then map_bank3=Null
          map_ascii =ReadByte  (file)
          bytes     =ReadInt   (file)
          dummy     =ReadShort (file)
          map_bank3 =CreateBank(bytes)
          ReadBank map_bank3,file,0,bytes
        EndIf

      Case 105 'default tile values-----------------------------------
        If tile<>Null Then
          frames=ReadShort(file)
          dummy =ReadByte(file)
          tile.bankd=CreateBank(frames)
          If frames>0 Then ReadBank tile.bankd,file,0,frames
        EndIf

      Case 106 'variables---------------------------------------------
        dummy=ReadByte (file)
        count=ReadShort(file)
        bytes=ReadInt  (file)
        bank4=CreateBank(count*4)

        If layer<>Null Then
          If Not(layer.bank4=Null) Then layer.bank4=Null
          layer.bank4=bank4
        ElseIf geo<>Null Then
          If geo.bank4<>0 Then geo.bank4=Null
          geo.bank4=bank4
        ElseIf code=layer_back Then
          If map_bank4<>0 Then map_bank4=Null
          map_bank4=bank4
        EndIf

        For j=1 To count
          size1=ReadByte(file)
          size2=ReadByte(file)
          bank=CreateBank(2+size1+size2)
          PokeByte bank,0,size1
          PokeByte bank,1,size2
          ReadBank bank,file,2,size1
          ReadBank bank,file,2+size1,size2
          PokeInt bank4,j*4-4,bank
        Next

      Default '-------------------------------------------------------
        geo  =Null
        layer=Null
        tile =Null

    End Select
  Next


  'FINAL--------------------------------------------------------------
  For layer=EachIn layer_list
    If layer.code=layer_map Or layer.code=layer_iso1 Or layer.code=layer_iso2 Or layer.code=layer_hex1 Or layer.code=layer_hex2 Or layer.code=layer_image Or layer.code=layer_block Then
      layer.tile=extra_num2tile(layer.tmp)
      layer.tmp=0
    EndIf

    If layer.code=layer_clone Then
      layer.layer=extra_num2layer(layer.tmp)
      layer.tmp=0
    EndIf

    If layer.code=layer_block Then
      layer.layer=extra_num2layer(layer.depth2)
      layer.depth2=0
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

'#######################################################-Hier hat The Shadow GOTO benutzt, ih!-#######################################################
'#abort ' Abruch Label - Ih Goto
'  Global map_block [0+1]
map_block=New Int[0+1]
'  Global map_offset[0+1]
map_block=New Int[0+1]

  CloseFile file
  If crypted=1 Then
    crypt_overwrite(map_tmp$)
    DeleteFile map_tmp$
  EndIf
  Return error
End Function





'---------------------------------------------------------------------
'RETURN: mouse x in map viewport
'---------------------------------------------------------------------
Function map_mousex()
  Return MouseX()-map_x '<<<modify, if you use canvas
End Function





'---------------------------------------------------------------------
'RETURN: mouse y in map viewport
'---------------------------------------------------------------------
Function map_mousey()
  Return MouseY()-map_y '<<<modify, if you use canvas
End Function





'---------------------------------------------------------------------
Function map_reset()
  Local bank
  Local i
  Local Size

  geo_reset()
  layer_reset()
  tile_reset()

  If map_image<>0 Then 
	'FreeImage map_image 
	map_image=Null
  EndIf

  If map_bank3<>0 Then map_bank3=Null

  If map_bank4<>0 Then
    Size=BankSize(map_bank4)
    For i=0 To Size/4-1
      bank=PeekInt(map_bank4,i*4)
      bank=Null
    Next
   map_bank4=Null
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





'---------------------------------------------------------------------
'x: map position x
'y: map position y
'---------------------------------------------------------------------
Function map_scroll(x,y)
  map_scrollx=x
  map_scrolly=y
End Function





'---------------------------------------------------------------------
'x:      map start x
'y:      map start y
'width:  map width
'height: map height
'---------------------------------------------------------------------
Function map_viewport(x,y,width,height)
  map_x=x
  map_y=y
  map_width=width
  map_height=height
End Function
'}map.bmx


'Sourcefile: extra.bmx
'{
'Import "bbtype.bmx"
'Import "bbvkey.bmx"

'---------------------------------------------------------------------
'nr:     layer number
'RETURN: layer handle
'---------------------------------------------------------------------
Function extra_num2layer:bblayer(nr)
  Local count
  Local layer:bblayer

  For layer=EachIn layer_list
    If layer.code=layer_map Or layer.code=layer_iso1 Or layer.code=layer_iso2 Or layer.code=layer_hex1 Or layer.code=layer_hex2 Or layer.code=layer_clone Then
      count=count+1
      If count=nr Then Return layer
    EndIf
  Next
End Function





'---------------------------------------------------------------------
'nr:     tile number
'RETURN: tileset handle
'---------------------------------------------------------------------
Function extra_num2tile:bbtile(nr)
  Local count
  Local tile:bbtile

  For tile=EachIn tile_list
    count=count+1
    If count=nr Then Return tile
  Next
End Function





'---------------------------------------------------------------------
'file:   input file handle
'max:    max string length
'RETURN: text string
'---------------------------------------------------------------------
Function extra_readstr$(file,fmax)
  Local char
  Local i
  Local noadd
  Local txt$

  For i=1 To fmax
    char=ReadByte(file)
    If char=0 Then noadd=1
    If noadd=0 Then txt$=txt$+Chr$(char)
  Next
  Return txt$
End Function
'}extra.bmx

'Sourcefile: geo.bmx
'{
'Import "bbtype.bmx"
'Import "bbvkey.bmx"

Global geo_list:TList=New TList
Type bbgeo Extends TBBType       ' 9  10 11 12| <<<OBJECT TYPE

	Method New()
		Add(geo_list)
	End Method

	Method After:bbgeo()
		Local t:TLink
		t=_link.NextLink()
		If t Return bbgeo(t.value())
	End Method

	Method Before:bbgeo()
		Local t:TLink
		t=_link.PrevLink()
		If t Return bbgeo(t.value())
	End Method

  Field ascii   ' X  X  X  X | ascii-metadata
  Field bank3   ' X  X  X  X | bank metadata
  Field bank4   ' X  X  X  X | bank variables
  Field code    ' X  X  X  X | object code
  Field name$   ' X  X  X  X | object name
  Field parax   ' X  X  X  X | parallax x
  Field paray   ' X  X  X  X | parallax y
  Field posx    ' X  X  X  X | object posx
  Field posy    ' X  X  X  X | object posy
  Field sizex   '    X  X  X | object sizex
  Field sizey   '    X  X  X | object sizey
  Field visible ' X  X  X  X | object visible
End Type





'---------------------------------------------------------------------
'geo: geo handle
'---------------------------------------------------------------------
Function geo_delete(geo:bbgeo)
  Local bank
  Local i
  Local Size

  If geo=Null Then Return

  If geo.bank3<>0 Then geo.bank3=Null

  If geo.bank4<>0 Then
    Size=BankSize(geo.bank4)
    For i=0 To Size/4-1
      bank=PeekInt(geo.bank4,i*4)
      bank=Null
    Next
    geo.bank4=Null
  EndIf

  geo.Remove()
End Function





'---------------------------------------------------------------------
'name:   find this geo name
'RETURN: geo type handle to a object (null=not found)
'---------------------------------------------------------------------
Function geo_find:bbgeo(name$)
  Local geo:bbgeo

  For geo=EachIn geo_list
    If geo.name$=name$ Then Return geo
  Next
End Function





'---------------------------------------------------------------------
Function geo_reset()
  Local bank
  Local i
  Local Size

  Local geo:bbgeo

  For geo=EachIn geo_list
    If geo.bank3<>0 Then geo.bank3=Null

    If geo.bank4<>0 Then
      Size=BankSize(geo.bank4)
      For i=0 To Size/4-1
        bank=PeekInt(geo.bank4,i*4)
        bank=Null
      Next
      geo.bank4=Null
    EndIf

    geo.Remove()
  Next
End Function
'}geo.bmx

'Sourcefile: layer.bmx
'{
'Import "bbtype.bmx"
'Import "bbvkey.bmx"

Global layer_list:TList=New TList
Type bblayer Extends TBBType         ' 0 1 2 3 4 5 6 7 8 | <<<OBJECT TYPE

	Method New()
		Add(layer_list)
	End Method

	Method After:bblayer()
		Local t:TLink
		t=_link.NextLink()
		If t Return bblayer(t.value())
	End Method

	Method Before:bblayer()
		Local t:TLink
		t=_link.PrevLink()
		If t Return bblayer(t.value())
	End Method

  Field ascii       ' X X X X X X X X X | ascii-metadata
  Field bank1:TBank       '   X X X X X .     | bank basedata
  Field bank2:TBank       '   X X X X X .     | bank datalayer
  Field bank3:TBank       ' X X X X X X X X X | bank metadata
  Field bank4:TBank       ' X X X X X X X X X | bank variables
  Field code        ' X X X X X X X X X | object code
  Field depth1      '   X X X X X .     | base depth
  Field depth2      '   X X X X X .     | data depth
  Field Frame       '               X X | image frame
  Field layer:bblayer '             X   X | layer handle
  Field mask        '   X         . X X | tile mask   / image mask
  Field mode        ' X             X X | backgr mode / image mode (anim)
  Field name$       ' X X X X X X X X X | object name
  Field parax       ' X X X X X X X X X | parallax x / block offset x
  Field paray       ' X X X X X X X X X | parallax y / block offset y
  Field posx        ' X X X X X X X X X | object posx
  Field posy        ' X X X X X X X X X | object posy
  Field sizex       ' X X X X X X .     | object sizex
  Field sizey       ' X X X X X X .     | object sizey
  Field start       '       X X X . X X | tile shift  / image start (anim)
  Field tile:bbtile   '   X X X X X . X X | tileset handle
  Field time        '               X X | last msec time
  Field tmp         '               X X | tmp
  Field visible     ' X X X X X X X X X | object visible
End Type

Const layer_back  =00
Const layer_map   =01
Const layer_iso1  =02
Const layer_iso2  =03
Const layer_hex1  =04
Const layer_hex2  =05
Const layer_clone =06
Const layer_image =07
Const layer_block =08
Const layer_point =09
Const layer_line  =10
Const layer_rect  =11
Const layer_oval  =12

Global layer_x
Global layer_y
Global layer_width
Global layer_height





'---------------------------------------------------------------------
'layer: layer handle
'---------------------------------------------------------------------
Function layer_delete(layer:bblayer)
  Local bank
  Local i
  Local Size

  If layer=Null Then Return

  If layer.code<>layer_clone Then
    If Not(layer.bank1=Null) Then layer.bank1=Null
    If Not(layer.bank2=Null) Then layer.bank2=Null
  EndIf

  If Not(layer.bank3=Null) Then layer.bank3=Null

  If Not(layer.bank4=Null) Then
    Size=BankSize(layer.bank4)
    For i=0 To Size/4-1
      bank=PeekInt(layer.bank4,i*4)
      bank=Null
    Next
    layer.bank4=Null
  EndIf

  layer.Remove()
End Function





'---------------------------------------------------------------------
'name:   find this layer name
'RETURN: layer type handle to a object (null=not found)
'---------------------------------------------------------------------
Function layer_find:bblayer(name$)
  Local layer:bblayer

  For layer=EachIn layer_list
    If layer.name$=name$ Then Return layer
  Next
End Function





'---------------------------------------------------------------------
'layer:  layer handle
'x:      tile pos x
'y:      tile pos y
'RETURN: 0=void, 1=frame, 2=anim
'---------------------------------------------------------------------
Function layer_getcode(layer:bblayer,x,y)
  Local anims
  Local count
  Local mode
  Local Offset
  Local value

  If layer=Null Then Return
  If layer.bank1=Null Then Return
  If x<0 Or y<0 Or x>layer.sizex-1 Or y>layer.sizey-1 Then Return

  If layer.tile<>Null Then
    anims=layer.tile.anims
    count=layer.tile.count
  EndIf

  If layer.depth1=4 Then
    Offset=((y*layer.sizex+x)/2)
    mode  =((y*layer.sizex+x) & 1)*4
    value =(PeekByte(layer.bank1,Offset) Shr mode) & 15
  ElseIf layer.depth1=8 Then
    Offset=y*layer.sizex+x
    value=PeekByte(layer.bank1,Offset)
  ElseIf layer.depth1=12 Then
    Offset=((y*layer.sizex+x)*3)/2
    mode  =((y*layer.sizex+x) & 1)*4
    value =(PeekShort(layer.bank1,Offset) Shr mode) & 4095
  ElseIf layer.depth1=16 Then
    Offset=(y*layer.sizex+x)*2
    value=PeekShort(layer.bank1,Offset)
  EndIf

  If value=>1 And value<=anims Then 'anim
    Return 2
  ElseIf value=>anims+1 And value<=count+anims Then 'frame
    Return 1
  EndIf
End Function





'---------------------------------------------------------------------
'layer:  layer handle
'x:      tile pos x
'y:      tile pos y
'RETURN: tile value
'---------------------------------------------------------------------
Function layer_getdata(layer:bblayer,x,y)
  Local mode
  Local Offset
  Local value

  If layer=Null Then Return
  If layer.bank2=Null Then Return
  If x<0 Or y<0 Or x>layer.sizex-1 Or y>layer.sizey-1 Then Return

  If layer.depth2=1 Then
    Offset=((y*layer.sizex+x)/8)
    mode  =((y*layer.sizex+x) Mod 8)
    value =(PeekByte(layer.bank2,Offset) Shr mode) & 1
  ElseIf layer.depth2=2 Then
    Offset=((y*layer.sizex+x)/4)
    mode  =((y*layer.sizex+x) Mod 4)*2
    value =(PeekByte(layer.bank2,Offset) Shr mode) & 3
  ElseIf layer.depth2=4 Then
    Offset=((y*layer.sizex+x)/2)
    mode  =((y*layer.sizex+x) Mod 2)*4
    value =(PeekByte(layer.bank2,Offset) Shr mode) & 15
  ElseIf layer.depth2=8 Then
    Offset=y*layer.sizex+x
    value =PeekByte(layer.bank2,Offset)
  EndIf

  Return value
End Function





'---------------------------------------------------------------------
'layer:  layer handle
'x:      tile pos x
'y:      tile pos y
'RETURN: tile value
'---------------------------------------------------------------------
Function layer_getvalue(layer:bblayer,x,y)
  Local anims
  Local count
  Local mode
  Local Offset
  Local value

  If layer=Null Then Return
  If layer.bank1=Null Then Return
  If x<0 Or y<0 Or x>layer.sizex-1 Or y>layer.sizey-1 Then Return

  If layer.tile<>Null Then
    anims=layer.tile.anims
    count=layer.tile.count
  EndIf

  If layer.depth1=4 Then
    Offset=((y*layer.sizex+x)/2)
    mode  =((y*layer.sizex+x) & 1)*4
    value =(PeekByte(layer.bank1,Offset) Shr mode) & 15
  ElseIf layer.depth1=8 Then
    Offset=y*layer.sizex+x
    value=PeekByte(layer.bank1,Offset)
  ElseIf layer.depth1=12 Then
    Offset=((y*layer.sizex+x)*3)/2
    mode  =((y*layer.sizex+x) & 1)*4
    value =(PeekShort(layer.bank1,Offset) Shr mode) & 4095
  ElseIf layer.depth1=16 Then
    Offset=(y*layer.sizex+x)*2
    value=PeekShort(layer.bank1,Offset)
  EndIf

  If value=>1 And value<=anims Then 'anim
    Return value
  ElseIf value=>anims+1 And value<=count+anims Then 'frame
    Return value-anims
  EndIf
End Function





'---------------------------------------------------------------------
'layer:  layer handle
'x:      tile pos x
'y:      tile pos y
'RETURN: raw hardcoded value
'---------------------------------------------------------------------
Function layer_getvalue2(layer:bblayer,x,y)
  Local mode
  Local Offset
  Local value

  If layer=Null Then Return
  If layer.bank1=Null Then Return
  If x<0 Or y<0 Or x>layer.sizex-1 Or y>layer.sizey-1 Then Return

  If layer.depth1=4 Then
    Offset=((y*layer.sizex+x)/2)
    mode  =((y*layer.sizex+x) & 1)*4
    value =(PeekByte(layer.bank1,Offset) Shr mode) & 15
  ElseIf layer.depth1=8 Then
    Offset=y*layer.sizex+x
    value=PeekByte(layer.bank1,Offset)
  ElseIf layer.depth1=12 Then
    Offset=((y*layer.sizex+x)*3)/2
    mode  =((y*layer.sizex+x) & 1)*4
    value =(PeekShort(layer.bank1,Offset) Shr mode) & 4095
  ElseIf layer.depth1=16 Then
    Offset=(y*layer.sizex+x)*2
    value=PeekShort(layer.bank1,Offset)
  EndIf

  Return value
End Function





'---------------------------------------------------------------------
Function layer_reset()
  Local bank
  Local i
  Local Size

  Local layer:bblayer

  For layer=EachIn layer_list
    If layer.code<>layer_clone Then
      If Not(layer.bank1=Null) Then layer.bank1=Null
      If Not(layer.bank2=Null) Then layer.bank2=Null
    EndIf

    If Not(layer.bank3=Null) Then layer.bank3=Null

    If Not(layer.bank4=Null) Then
      Size=BankSize(layer.bank4)
      For i=0 To Size/4-1
        bank=PeekInt(layer.bank4,i*4)
        bank=Null
      Next
      layer.bank4=Null
    EndIf

    layer.Remove()
  Next
End Function





'---------------------------------------------------------------------
'layer: layer handle
'x:     tile pos x
'y:     tile pos y
'value: tile value
'---------------------------------------------------------------------
Function layer_setdata(layer:bblayer,x,y,value)
  Local mode
  Local Offset
  Local value1
  Local value2

  If layer=Null Then Return
  If layer.bank2=Null Then Return
  If x<0 Or y<0 Or x>layer.sizex-1 Or y>layer.sizey-1 Then Return
  If value<0 Then value=0

  If layer.depth2=1 Then
    If value>1 Then value=0
    Offset=((y*layer.sizex+x)/8)
    mode  =((y*layer.sizex+x) Mod 8)
    value1=PeekByte(layer.bank2,Offset)
    value2=(value1 Shr mode) & 1
    PokeByte layer.bank2,Offset,value1 ~ (value2 Shl mode) ~ (value Shl mode)
  ElseIf layer.depth2=2 Then
    If value>3 Then value=0
    Offset=((y*layer.sizex+x)/4)
    mode  =((y*layer.sizex+x) Mod 4)*2
    value1=PeekByte(layer.bank2,Offset)
    value2=(value1 Shr mode) & 3
    PokeByte layer.bank2,Offset,value1 ~ (value2 Shl mode) ~ (value Shl mode)
  ElseIf layer.depth2=4 Then
    If value>15 Then value=0
    Offset=((y*layer.sizex+x)/2)
    mode  =((y*layer.sizex+x) Mod 2)*4
    value2=PeekByte(layer.bank2,Offset) & ($F0 Shr mode)
    PokeByte layer.bank2,Offset,value2 | (value Shl mode)
  ElseIf layer.depth2=8 Then
    If value>255 Then value=0
    Offset=y*layer.sizex+x
    PokeByte layer.bank2,Offset,value
  EndIf
End Function





'---------------------------------------------------------------------
'layer: layer handle
'x:     tile pos x
'y:     tile pos y
'value: tile value
'code:  0=void, 1=frame, 2=anim
'---------------------------------------------------------------------
Function layer_setvalue(layer:bblayer,x,y,value,code)
  Local anims
  Local count
  Local mode
  Local Offset
  Local value2

  If layer=Null Then Return
  If layer.bank1=Null Then Return
  If x<0 Or y<0 Or x>layer.sizex-1 Or y>layer.sizey-1 Then Return

  If layer.tile<>Null Then
    anims=layer.tile.anims
    count=layer.tile.count
  EndIf

  If code=1 Then
    If value<1 Or value>count Then value=0
    If value>0 Then value=value+anims
  ElseIf code=2 Then
    If value<1 Or value>anims Then value=0
  Else
    value=0
  EndIf

  If layer.depth1=4 Then
    If value>15 Then value=0
    Offset=((y*layer.sizex+x)/2)
    mode  =((y*layer.sizex+x) & 1)*4
    value2=PeekByte(layer.bank1,Offset) & ($F0 Shr mode)
    PokeByte layer.bank1,Offset,value2 | (value Shl mode)
  ElseIf layer.depth1=8 Then
    If value>255 Then value=0
    Offset=y*layer.sizex+x
    PokeByte layer.bank1,Offset,value
  ElseIf layer.depth1=12 Then
    If value>4095 Then value=0
    Offset=((y*layer.sizex+x)*3)/2
    mode  =((y*layer.sizex+x) & 1)
    value2=PeekShort(layer.bank1,Offset) & ($F000 Shr mode*12)
    PokeShort layer.bank1,Offset,value2 | (value Shl mode*4)
  ElseIf layer.depth1=16 Then
    If value>65535 Then value=0
    Offset=(y*layer.sizex+x)*2
    PokeShort layer.bank1,Offset,value
  EndIf
End Function





'---------------------------------------------------------------------
'layer: layer handle
'x:     tile pos x
'y:     tile pos y
'value: raw hardcoded value
'---------------------------------------------------------------------
Function layer_setvalue2(layer:bblayer,x,y,value)
  Local anims
  Local count
  Local mode
  Local Offset
  Local value2

  If layer=Null Then Return
  If layer.bank1=Null Then Return
  If x<0 Or y<0 Or x>layer.sizex-1 Or y>layer.sizey-1 Then Return
  If value<0 Then value=0

  If layer.depth1=4 Then
    If value>15 Then value=0
    Offset=((y*layer.sizex+x)/2)
    mode  =((y*layer.sizex+x) & 1)*4
    value2=PeekByte(layer.bank1,Offset) & ($F0 Shr mode)
    PokeByte layer.bank1,Offset,value2 | (value Shl mode)
  ElseIf layer.depth1=8 Then
    If value>255 Then value=0
    Offset=y*layer.sizex+x
    PokeByte layer.bank1,Offset,value
  ElseIf layer.depth1=12 Then
    If value>4095 Then value=0
    Offset=((y*layer.sizex+x)*3)/2
    mode  =((y*layer.sizex+x) & 1)
    value2=PeekShort(layer.bank1,Offset) & ($F000 Shr mode*12)
    PokeShort layer.bank1,Offset,value2 | (value Shl mode*4)
  ElseIf layer.depth1=16 Then
    If value>65535 Then value=0
    Offset=(y*layer.sizex+x)*2
    PokeShort layer.bank1,Offset,value
  EndIf
End Function
'}layer.bmx

'Sourcefile: layer_block.bmx
'{
'Import "bbtype.bmx"
'Import "bbvkey.bmx"

'---------------------------------------------------------------------
'layer: layer handle
'---------------------------------------------------------------------
Function layer_block_draw(layer:bblayer)
  Local bank
  Local Frame
  Local px
  Local py
  Local result
  Local start

  If layer=Null Then Return
  If layer.tile=Null Then Return
  If layer.tile.image=Null Then Return

  If layer.layer<>Null Then
    Select layer.layer.code
      Case layer_map
        result=layer_map_pos(layer.layer,layer.posx,layer.posy)
      Case layer_iso1
        result=layer_iso1_pos(layer.layer,layer.posx,layer.posy)
      Case layer_iso2
        result=layer_iso2_pos(layer.layer,layer.posx,layer.posy)
      Case layer_hex1
        result=layer_hex1_pos(layer.layer,layer.posx,layer.posy)
      Case layer_hex2
        result=layer_hex2_pos(layer.layer,layer.posx,layer.posy)
      Case layer_clone
        result=layer_clone_pos(layer.layer,layer.posx,layer.posy)
    End Select
  EndIf

  If result=1 Then
    px=layer_x+layer.parax '<<<para=justage
    py=layer_y+layer.paray '<<<para=justage
  Else
    px=layer.parax-map_scrollx '<<<para=justage
    py=layer.paray-map_scrolly '<<<para=justage
  EndIf

  If layer.Frame=>1 And layer.Frame<=layer.tile.anims Then
    If layer.mode=0 Then
      bank=PeekInt(layer.tile.banka,layer.Frame*4-4)
      start=PeekShort(bank,10)
      Frame=PeekShort(bank,start*4+8)
    Else
      bank=PeekInt(layer.tile.banka,layer.Frame*4-4)
      Frame=PeekShort(bank,layer.tmp*4+8)
    EndIf
  Else
    Frame=layer.Frame-layer.tile.anims
  EndIf

  If Frame=>1 And Frame<=layer.tile.count Then
    If layer.mask=0 Then DrawImage layer.tile.image,px,py,Frame-1
    If layer.mask=1 Then DrawImage layer.tile.image,px,py,Frame-1',layer.tile.sizex,layer.tile.sizey,frame-1
  EndIf
End Function





'---------------------------------------------------------------------
'layer: layer handle
'
'RETURN LAYER_X: global pos x
'RETURN LAYER_Y: global pos y
'---------------------------------------------------------------------
Function layer_block_pos(layer:bblayer)
  Local px
  Local py
  Local result

  If layer=Null Then Return

  If layer.layer<>Null Then
    Select layer.layer.code
      Case layer_map
        result=layer_map_pos(layer.layer,layer.posx,layer.posy)
      Case layer_iso1
        result=layer_iso1_pos(layer.layer,layer.posx,layer.posy)
      Case layer_iso2
        result=layer_iso2_pos(layer.layer,layer.posx,layer.posy)
      Case layer_hex1
        result=layer_hex1_pos(layer.layer,layer.posx,layer.posy)
      Case layer_hex2
        result=layer_hex2_pos(layer.layer,layer.posx,layer.posy)
      Case layer_clone
        result=layer_clone_pos(layer.layer,layer.posx,layer.posy)
    End Select
  EndIf

  If result=1 Then
    px=layer_x+layer.parax '<<<para=justage
    py=layer_y+layer.paray '<<<para=justage
    layer_x=map_getcoord(px,layer.layer.parax,map_scrollx)
    layer_y=map_getcoord(py,layer.layer.paray,map_scrolly)
  Else
    layer_x=layer.parax '<<<para=justage
    layer_y=layer.paray '<<<para=justage
  EndIf
End Function
'}layer_block.bmx

'Sourcefile: layer_clone.bmx
'{
'Import "bbtype.bmx"
'Import "bbvkey.bmx"

'---------------------------------------------------------------------
'layer:   layer handle
'x:       screen x
'y:       screen y
'check:   bound check
'RETURN:  0=outside, 1=inside (only if check is enabled)
'---------------------------------------------------------------------
Function layer_clone_coord(layer:bblayer,x,y,check=0)
  If layer.layer=Null Then
    Return layer_map_coord(layer,x,y,check)
  Else
    Select layer.layer.code
      Case layer_map
        Return layer_map_coord(layer,x,y,check)
      Case layer_iso1
        Return layer_iso1_coord(layer,x,y,check)
      Case layer_iso2
        Return layer_iso2_coord(layer,x,y,check)
      Case layer_hex1
        Return layer_hex1_coord(layer,x,y,check)
      Case layer_hex2
        Return layer_hex2_coord(layer,x,y,check)
    End Select
  EndIf
End Function





'---------------------------------------------------------------------
'layer:   layer handle
'---------------------------------------------------------------------
Function layer_clone_draw(layer:bblayer)
  layer_clone_update(layer)

  If layer.layer=Null Then
    layer_map_draw(layer)
  Else
    Select layer.layer.code
      Case layer_map
        layer_map_draw(layer)
      Case layer_iso1
        layer_iso1_draw(layer)
      Case layer_iso2
        layer_iso2_draw(layer)
      Case layer_hex1
        layer_hex1_draw(layer)
      Case layer_hex2
        layer_hex2_draw(layer)
    End Select
  EndIf
End Function





'---------------------------------------------------------------------
'layer:   layer handle
'x:       position x
'y:       position y
'RETURN:  0=error, 1=ok
'---------------------------------------------------------------------
Function layer_clone_pos(layer:bblayer,x,y)
  If layer.layer=Null Then
    Return layer_map_pos(layer,x,y)
  Else
    Select layer.layer.code
      Case layer_map
        Return layer_map_pos(layer,x,y)
      Case layer_iso1
        Return layer_iso1_pos(layer,x,y)
      Case layer_iso2
        Return layer_iso2_pos(layer,x,y)
      Case layer_hex1
        Return layer_hex1_pos(layer,x,y)
      Case layer_hex2
        Return layer_hex2_pos(layer,x,y)
    End Select
  EndIf
End Function





'---------------------------------------------------------------------
'layer: layer handle
'---------------------------------------------------------------------
Function layer_clone_size(layer:bblayer)
  If layer.layer=Null Then
    layer_map_size(layer)
  Else
    Select layer.layer.code
      Case layer_map
        layer_map_size(layer)
      Case layer_iso1
        layer_iso1_size(layer)
      Case layer_iso2
        layer_iso2_size(layer)
      Case layer_hex1
        layer_hex1_size(layer)
      Case layer_hex2
        layer_hex2_size(layer)
    End Select
  EndIf
End Function





'---------------------------------------------------------------------
'layer: layer handle
'---------------------------------------------------------------------
Function layer_clone_update(layer:bblayer)
  If layer.layer=Null Then
    layer.bank1 =Null
    layer.bank2 =Null
    layer.depth1=0
    layer.depth2=0
    layer.mask  =0
    layer.sizex =10
    layer.sizey =10
    layer.start =0
    layer.tile  =Null
  Else
    layer.bank1 =layer.layer.bank1
    layer.bank2 =layer.layer.bank2
    layer.depth1=layer.layer.depth1
    layer.depth2=layer.layer.depth2
    layer.mask  =layer.layer.mask
    layer.sizex =layer.layer.sizex
    layer.sizey =layer.layer.sizey
    layer.start =layer.layer.start
    layer.tile  =layer.layer.tile
  EndIf
End Function
'}layer_clone.bmx

'Sourcefile: layer_hex1.bmx
'{
'Import "bbtype.bmx"
'Import "bbvkey.bmx"

'---------------------------------------------------------------------
'layer: layer handle
'x:     screen x
'y:     screen y
'check: bound check
'
'RETURN:         0=outside, 1=inside (only if check is enabled)
'RETURN LAYER_X: tile pos x
'RETURN LAYER_Y: tile pos y
'---------------------------------------------------------------------
Function layer_hex1_coord(layer:bblayer,x,y,check=0)
  Local arrayx[6]
  Local arrayy[6]
  Local corr
  Local cx#
  Local cy#
  Local dist#
  Local factor
  Local i
  Local fmin#
  Local outside
  Local px
  Local py
  Local sizex
  Local sizey
  Local stepx
  Local stepy
  Local stretch#
  Local width
  Local xx
  Local yy

  If layer=Null Then Return

  If layer.tile<>Null Then
    factor=layer.tile.factor
    sizex =layer.tile.sizex
    sizey =layer.tile.sizey-factor
  Else
    sizex=tile_default
    sizey=tile_default
  EndIf

  If sizex<tile_minsize Then sizex=tile_minsize
  If sizey<tile_minsize Then sizey=tile_minsize

  px=map_getscreen(layer.posx,layer.parax,map_scrollx)
  py=map_getscreen(layer.posy,layer.paray,map_scrolly)

  If sizex=Int((sizey/2.0)/Sin(60))*2 And (sizey Mod 4)=0 Then
    stepx=sizex-sizey/4
    stepy=sizey/2
  ElseIf sizex=sizey Then
    stepx=sizex-sizey/4
    stepy=sizey/2
  Else
    stepx=sizex-(sizey-2)/2
    stepy=sizey/2
  EndIf

  width=Int((sizey/2)/Sin(60))*2.0
  stretch#=width/sizex
  fmin#=1000000

  xx=(x-px)/stepx
  If ((xx+layer.start) And 1)=0 Then
    yy=(y-py)/sizey
    corr=0
  Else
    yy=(y-py-stepy)/sizey
    corr=1
  EndIf

  arrayx[0]=xx-1 ; arrayy[0]=yy+corr-1
  arrayx[1]=xx-1 ; arrayy[1]=yy+corr
  arrayx[2]=xx   ; arrayy[2]=yy-1
  arrayx[3]=xx   ; arrayy[3]=yy
  arrayx[4]=xx   ; arrayy[4]=yy+1
  arrayx[5]=xx+1 ; arrayy[5]=yy+corr-1
  arrayx[6]=xx+1 ; arrayy[6]=yy+corr

  For i=0 To 6
    corr=(arrayx[i]+layer.start) & 1
    cx#=(x-px-arrayx[i]*stepx)-(sizex-1)/2.0
    cy#=(y-py-arrayy[i]*sizey-corr*stepy)-(sizey-1)/2.0
    dist#=cx#*cx#*stretch#*stretch#+cy#*cy#
    If fmin#>dist# Then
      fmin#=dist#
      layer_x=arrayx[i]
      layer_y=arrayy[i]
    EndIf
  Next

  If check=1 Then
    If layer_x<0 Then layer_x=0 ; outside=1
    If layer_y<0 Then layer_y=0 ; outside=1
    If layer_x>layer.sizex-1 Then layer_x=layer.sizex-1 ; outside=1
    If layer_y>layer.sizey-1 Then layer_y=layer.sizey-1 ; outside=1
  EndIf

  Return 1-outside
End Function





'---------------------------------------------------------------------
'layer: layer handle
'---------------------------------------------------------------------
Function layer_hex1_draw(layer:bblayer)
  Local anims
  Local bank
  Local begin
  Local count
  Local endx
  Local endy
  Local factor
  Local Frame
  Local i
  Local image:timage
  Local mode
  Local Offset
  Local px
  Local py
  Local sizex
  Local sizey
  Local start
  Local startx
  Local starty
  Local stepx
  Local stepy
  Local value
  Local x
  Local xx
  Local y
  Local yy

  If layer=Null Then Return
  If layer.tile=Null Then Return

  anims =layer.tile.anims
  count =layer.tile.count
  factor=layer.tile.factor
  image =layer.tile.image
  sizex =layer.tile.sizex
  sizey =layer.tile.sizey-factor

  If image=Null Then Return
  If sizex<tile_minsize Then sizex=tile_minsize
  If sizey<tile_minsize Then sizey=tile_minsize

  px=map_getscreen(layer.posx,layer.parax,map_scrollx)
  py=map_getscreen(layer.posy,layer.paray,map_scrolly)

  If sizex=Int((sizey/2)/Sin(60))*2.0 And (sizey Mod 4)=0 Then
    stepx=sizex-sizey/4
    stepy=sizey/2
  ElseIf sizex=sizey Then
    stepx=sizex-sizey/4
    stepy=sizey/2
  Else
    stepx=sizex-(sizey-2)/2
    stepy=sizey/2
  EndIf

  startx=(-sizex-px+stepx)/stepx
  starty=(-stepy-py-factor)/sizey
  endx=(map_width-px)/stepx
  endy=(map_height-py)/sizey

  If startx<0 Then startx=0
  If starty<0 Then starty=0
  If endx>layer.sizex-1 Then endx=layer.sizex-1
  If endy>layer.sizey-1 Then endy=layer.sizey-1

  For y=starty To endy
    For i=0 To 1
      begin=startx+(startx+layer.start+i) & 1
      For x=begin To endx Step 2

        If layer.depth1=4 Then
          Offset=((y*layer.sizex+x)/2)
          mode  =((y*layer.sizex+x) & 1)*4
          value =(PeekByte(layer.bank1,Offset) Shr mode) & 15
        ElseIf layer.depth1=8 Then
          Offset=y*layer.sizex+x
          value =PeekByte(layer.bank1,Offset)
        ElseIf layer.depth1=12 Then
          Offset=((y*layer.sizex+x)*3)/2
          mode  =((y*layer.sizex+x) & 1)*4
          value =(PeekShort(layer.bank1,Offset) Shr mode) & 4095
        ElseIf layer.depth1=16 Then
          Offset=(y*layer.sizex+x)*2
          value =PeekShort(layer.bank1,Offset)
        EndIf

        If value=>1 And value<=anims Then
          bank=PeekInt(layer.tile.banka,value*4-4)
          start=PeekShort(bank,10)
          Frame=PeekShort(bank,start*4+8)
        Else
          Frame=value-anims
        EndIf

        If Frame=>1 And Frame<=count Then
          xx=px+x*stepx
          yy=py+y*sizey+((x+layer.start) & 1)*stepy
          DrawImage image,xx,yy,Frame-1
        EndIf

      Next
    Next
  Next
End Function





'---------------------------------------------------------------------
'layer: layer handle
'x:     position x
'y:     position y
'
'RETURN:         0=error, 1=ok
'RETURN LAYER_X: screen pos x
'RETURN LAYER_Y: screen pos y
'---------------------------------------------------------------------
Function layer_hex1_pos(layer:bblayer,x,y)
  Local factor
  Local px
  Local py
  Local sizex
  Local sizey
  Local stepx
  Local stepy

  If layer=Null Then Return

  If layer.tile<>Null Then
    factor=layer.tile.factor
    sizex =layer.tile.sizex
    sizey =layer.tile.sizey-factor
  Else
    sizex=tile_default
    sizey=tile_default
  EndIf

  If sizex<tile_minsize Then sizex=tile_minsize
  If sizey<tile_minsize Then sizey=tile_minsize

  px=map_getscreen(layer.posx,layer.parax,map_scrollx)
  py=map_getscreen(layer.posy,layer.paray,map_scrolly)

  If sizex=Int((sizey/2)/Sin(60))*2.0 And (sizey Mod 4)=0 Then
    stepx=sizex-sizey/4
    stepy=sizey/2
  ElseIf sizex=sizey Then
    stepx=sizex-sizey/4
    stepy=sizey/2
  Else
    stepx=sizex-(sizey-2)/2
    stepy=sizey/2
  EndIf

  layer_x=px+x*stepx
  layer_y=py+y*sizey+((x+layer.start) & 1)*stepy
  Return 1
End Function





'---------------------------------------------------------------------
'layer: layer handle
'
'RETURN LAYER_WIDTH:  layer width in pixel
'RETURN LAYER_HEIGHT: layer height in pixel
'---------------------------------------------------------------------
Function layer_hex1_size(layer:bblayer)
  Local factor
  Local sizex
  Local sizey
  Local stepx
  Local stepy

  If layer=Null Then Return

  If layer.tile<>Null Then
    factor=layer.tile.factor
    sizex =layer.tile.sizex
    sizey =layer.tile.sizey-factor
  Else
    sizex=tile_default
    sizey=tile_default
  EndIf

  If sizex<tile_minsize Then sizex=tile_minsize
  If sizey<tile_minsize Then sizey=tile_minsize

  If sizex=Int((sizey/2)/Sin(60))*2.0 And (sizey Mod 4)=0 Then
    stepx=sizex-sizey/4
    stepy=sizey/2
  ElseIf sizex=sizey Then
    stepx=sizex-sizey/4
    stepy=sizey/2
  Else
    stepx=sizex-(sizey-2)/2
    stepy=sizey/2
  EndIf

  layer_width =layer.sizex*stepx-stepx+sizex
  layer_height=layer.sizey*sizey+(layer.sizex>1)*stepy+factor
End Function
'}layer_hex1.bmx

'Sourcefile: layer_hex2.bmx
'{
'Import "bbtype.bmx"
'Import "bbvkey.bmx"

'---------------------------------------------------------------------
'layer: layer handle
'x:     screen x
'y:     screen y
'check: bound check
'
'RETURN:         0=outside, 1=inside (only if check is enabled)
'RETURN LAYER_X: tile pos x
'RETURN LAYER_Y: tile pos y
'---------------------------------------------------------------------
Function layer_hex2_coord(layer:bblayer,x,y,check=0)
  Local arrayx[8]
  Local arrayy[8]
  Local corr
  Local cx#
  Local cy#
  Local dist#
  Local factor
  Local height
  Local i
  Local fmin#
  Local outside
  Local px
  Local py
  Local sizex
  Local sizey
  Local stepx
  Local stepy
  Local stretch#
  Local xx
  Local yy

  If layer=Null Then Return

  If layer.tile<>Null Then
    factor=layer.tile.factor
    sizex =layer.tile.sizex
    sizey =layer.tile.sizey-factor
  Else
    sizex=tile_default
    sizey=tile_default
  EndIf

  If sizex<tile_minsize Then sizex=tile_minsize
  If sizey<tile_minsize Then sizey=tile_minsize

  px=map_getscreen(layer.posx,layer.parax,map_scrollx)
  py=map_getscreen(layer.posy,layer.paray,map_scrolly)

  stepx=sizex/2
  stepy=sizey-sizex/4
  height=Int((sizex/2)/Sin(60))*2.0
  stretch#=height/sizey
  fmin#=1000000

  yy=(y-py)/stepy
  If ((yy+layer.start) And 1)=0 Then
    xx=(x-px)/sizex
    corr=0
  Else
    xx=(x-px-stepx)/sizex
    corr=1
  EndIf

  arrayx[0]=xx+corr-1 ; arrayy[0]=yy-1
  arrayx[1]=xx+corr   ; arrayy[1]=yy-1
  arrayx[2]=xx-1      ; arrayy[2]=yy
  arrayx[3]=xx        ; arrayy[3]=yy
  arrayx[4]=xx+1      ; arrayy[4]=yy
  arrayx[5]=xx+corr-1 ; arrayy[5]=yy+1
  arrayx[6]=xx+corr   ; arrayy[6]=yy+1

  For i=0 To 6
    corr=(arrayy[i]+layer.start) & 1
    cx#=(x-px-arrayx[i]*sizex-corr*stepx)-(sizex-1)/2.0
    cy#=(y-py-arrayy[i]*stepy)-(sizey-1)/2.0
    dist#=cx#*cx#+cy#*cy#*stretch#*stretch#
    If fmin#>dist# Then
      fmin#=dist#
      layer_x=arrayx[i]
      layer_y=arrayy[i]
    EndIf
  Next

  If check=1 Then
    If layer_x<0 Then layer_x=0 ; outside=1
    If layer_y<0 Then layer_y=0 ; outside=1
    If layer_x>layer.sizex-1 Then layer_x=layer.sizex-1 ; outside=1
    If layer_y>layer.sizey-1 Then layer_y=layer.sizey-1 ; outside=1
  EndIf

  Return 1-outside
End Function





'---------------------------------------------------------------------
'layer: layer handle
'---------------------------------------------------------------------
Function layer_hex2_draw(layer:bblayer)
  Local anims
  Local bank
  Local count
  Local endx
  Local endy
  Local factor
  Local Frame
  Local image:timage
  Local mode
  Local Offset
  Local px
  Local py
  Local sizex
  Local sizey
  Local start
  Local startx
  Local starty
  Local stepx
  Local stepy
  Local value
  Local x
  Local xx
  Local y
  Local yy

  If layer=Null Then Return
  If layer.tile=Null Then Return

  anims =layer.tile.anims
  count =layer.tile.count
  factor=layer.tile.factor
  image =layer.tile.image
  sizex =layer.tile.sizex
  sizey =layer.tile.sizey-factor

  If image=Null Then Return
  If sizex<tile_minsize Then sizex=tile_minsize
  If sizey<tile_minsize Then sizey=tile_minsize

  px=map_getscreen(layer.posx,layer.parax,map_scrollx)
  py=map_getscreen(layer.posy,layer.paray,map_scrolly)

  stepx=sizex/2
  stepy=sizey-sizex/4

  startx=(-stepx-px)/sizex
  starty=(-sizey-py+stepy-factor)/stepy
  endx=(map_width-px)/sizex
  endy=(map_height-py)/stepy

  If startx<0 Then startx=0
  If starty<0 Then starty=0
  If endx>layer.sizex-1 Then endx=layer.sizex-1
  If endy>layer.sizey-1 Then endy=layer.sizey-1

  For y=starty To endy
    For x=startx To endx

      If layer.depth1=4 Then
        Offset=((y*layer.sizex+x)/2)
        mode  =((y*layer.sizex+x) & 1)*4
        value =(PeekByte(layer.bank1,Offset) Shr mode) & 15
      ElseIf layer.depth1=8 Then
        Offset=y*layer.sizex+x
        value =PeekByte(layer.bank1,Offset)
      ElseIf layer.depth1=12 Then
        Offset=((y*layer.sizex+x)*3)/2
        mode  =((y*layer.sizex+x) & 1)*4
        value =(PeekShort(layer.bank1,Offset) Shr mode) & 4095
      ElseIf layer.depth1=16 Then
        Offset=(y*layer.sizex+x)*2
        value =PeekShort(layer.bank1,Offset)
      EndIf

      If value=>1 And value<=anims Then
        bank=PeekInt(layer.tile.banka,value*4-4)
        start=PeekShort(bank,10)
        Frame=PeekShort(bank,start*4+8)
      Else
        Frame=value-anims
      EndIf

      If Frame=>1 And Frame<=count Then
        xx=px+x*sizex+((y+layer.start) & 1)*stepx
        yy=py+y*stepy
        DrawImage image,xx,yy,Frame-1
      EndIf

    Next
  Next
End Function





'---------------------------------------------------------------------
'layer: layer handle
'x:     position x
'y:     position y
'
'RETURN:         0=error, 1=ok
'RETURN LAYER_X: screen pos x
'RETURN LAYER_Y: screen pos y
'---------------------------------------------------------------------
Function layer_hex2_pos(layer:bblayer,x,y)
  Local factor
  Local px
  Local py
  Local sizex
  Local sizey
  Local stepx
  Local stepy

  If layer=Null Then Return

  If layer.tile<>Null Then
    factor=layer.tile.factor
    sizex =layer.tile.sizex
    sizey =layer.tile.sizey-factor
  Else
    sizex=tile_default
    sizey=tile_default
  EndIf

  If sizex<tile_minsize Then sizex=tile_minsize
  If sizey<tile_minsize Then sizey=tile_minsize

  px=map_getscreen(layer.posx,layer.parax,map_scrollx)
  py=map_getscreen(layer.posy,layer.paray,map_scrolly)

  stepx=sizex/2
  stepy=sizey-sizex/4

  layer_x=px+x*sizex+((y+layer.start) & 1)*stepx
  layer_y=py+y*stepy
  Return 1
End Function





'---------------------------------------------------------------------
'layer: layer handle
'
'RETURN LAYER_WIDTH:  layer width in pixel
'RETURN LAYER_HEIGHT: layer height in pixel
'---------------------------------------------------------------------
Function layer_hex2_size(layer:bblayer)
  Local factor
  Local sizex
  Local sizey
  Local stepx
  Local stepy

  If layer=Null Then Return

  If layer.tile<>Null Then
    factor=layer.tile.factor
    sizex =layer.tile.sizex
    sizey =layer.tile.sizey-factor
  Else
    sizex=tile_default
    sizey=tile_default
  EndIf

  If sizex<tile_minsize Then sizex=tile_minsize
  If sizey<tile_minsize Then sizey=tile_minsize

  stepx=sizex/2
  stepy=sizey-sizex/4

  layer_width =layer.sizex*sizex+(layer.sizey>1)*stepx
  layer_height=layer.sizey*stepy-stepy+sizey+factor
End Function
'}layer_hex2.bmx

'Sourcefile: layer_image.bmx
'{
'Import "bbtype.bmx"
'Import "bbvkey.bmx"

'---------------------------------------------------------------------
'layer:  layer handle (map only)
'image:  image handle
'RETURN: 0=no collision
'        1=collision
'---------------------------------------------------------------------
Function layer_image_collision(layer:bblayer, image:bblayer)
  Local bank
  Local Frame
  Local px
  Local py
  Local start

  If layer=Null Then Return
  If layer.tile=Null Then Return
  If layer.tile.image=Null Then Return
  If layer.code<>layer_map Then Return

  If image=Null Then Return
  If image.tile=Null Then Return
  If image.tile.image=Null Then Return
  If image.code<>layer_image Then Return

  px=map_getscreen(image.posx,image.parax,map_scrollx)
  py=map_getscreen(image.posy,image.paray,map_scrolly)

  If image.Frame=>1 And image.Frame<=image.tile.anims Then
    If image.mode=0 Then
      bank=PeekInt(image.tile.banka,image.Frame*4-4)
      start=PeekShort(bank,10)
      Frame=PeekShort(bank,start*4+8)
    Else
      bank=PeekInt(image.tile.banka,image.Frame*4-4)
      Frame=PeekShort(bank,image.tmp*4+8)
    EndIf
  Else
    Frame=image.Frame-image.tile.anims
  EndIf

  If Frame=>1 And Frame<=image.tile.count Then
    Return layer_map_collision(layer,px,py,image.tile.image,Frame-1)
  EndIf
End Function





'---------------------------------------------------------------------
'layer: layer handle
'---------------------------------------------------------------------
Function layer_image_draw(layer:bblayer)
  Local bank
  Local Frame
  Local px
  Local py
  Local start

  If layer=Null Then Return
  If layer.tile=Null Then Return
  If layer.tile.image=Null Then Return

  px=map_getscreen(layer.posx,layer.parax,map_scrollx)
  py=map_getscreen(layer.posy,layer.paray,map_scrolly)

  If layer.Frame=>1 And layer.Frame<=layer.tile.anims Then
    If layer.mode=0 Then
      bank=PeekInt(layer.tile.banka,layer.Frame*4-4)
      start=PeekShort(bank,10)
      Frame=PeekShort(bank,start*4+8)
    Else
      bank=PeekInt(layer.tile.banka,layer.Frame*4-4)
      Frame=PeekShort(bank,layer.tmp*4+8)
    EndIf
  Else
    Frame=layer.Frame-layer.tile.anims
  EndIf

  If Frame=>1 And Frame<=layer.tile.count Then
    If layer.mask=0 Then DrawImage layer.tile.image,px,py,Frame-1
    If layer.mask=1 Then DrawImage layer.tile.image,px,py,Frame-1
  EndIf
End Function
'}layer_image.bmx

'Sourcefile: layer_iso1.bmx
'{
'Import "bbtype.bmx"
'Import "bbvkey.bmx"

'---------------------------------------------------------------------
'layer: layer handle
'x:     screen x
'y:     screen y
'check: bound check
'
'RETURN:         0=outside, 1=inside (only if check is enabled)
'RETURN LAYER_X: tile pos x
'RETURN LAYER_Y: tile pos y
'---------------------------------------------------------------------
Function layer_iso1_coord(layer:bblayer,x,y,check=0)
  Local arrayx[8]
  Local arrayy[8]
  Local cx#
  Local cy#
  Local dist#
  Local factor
  Local halfx
  Local halfy
  Local i
  Local fmin#
  Local outside
  Local posx
  Local px
  Local py
  Local sizex
  Local sizey
  Local stepx
  Local stepy
  Local stretch#
  Local xx
  Local yy

  If layer=Null Then Return

  If layer.tile<>Null Then
    factor=layer.tile.factor
    sizex =layer.tile.sizex
    sizey =layer.tile.sizey-factor
  Else
    sizex=tile_default
    sizey=tile_default
  EndIf

  If sizex<tile_minsize Then sizex=tile_minsize
  If sizey<tile_minsize Then sizey=tile_minsize

  px=map_getscreen(layer.posx,layer.parax,map_scrollx)
  py=map_getscreen(layer.posy,layer.paray,map_scrolly)

  If (sizey And 1)=0 Then
    stepx=sizex+2
    stepy=sizey
  Else
    stepx=sizex
    stepy=sizey+1
  EndIf

  halfx=stepx/2
  halfy=stepy/2
  posx=(layer.sizey-1)*halfx
  stretch#=stepy/stepx
  fmin#=1000000

  xx=((x-px-posx)/(halfx) + (y)/(halfy) - (py)/(halfy))/2-1
  yy=((y-py)/(halfy) - (x)/(halfx) + (px)/(halfx) + (posx)/(halfx))/2

  arrayx[0]=xx-1 ; arrayy[0]=yy-1
  arrayx[1]=xx-1 ; arrayy[1]=yy
  arrayx[2]=xx   ; arrayy[2]=yy-1
  arrayx[3]=xx-1 ; arrayy[3]=yy+1
  arrayx[4]=xx   ; arrayy[4]=yy
  arrayx[5]=xx+1 ; arrayy[5]=yy-1
  arrayx[6]=xx   ; arrayy[6]=yy+1
  arrayx[7]=xx+1 ; arrayy[7]=yy
  arrayx[8]=xx+1 ; arrayy[8]=yy+1

  For i=0 To 8
    cx#=(x-px-posx-arrayx[i]*halfx+arrayy[i]*halfx)-(sizex-1)/2
    cy#=(y-py-arrayx[i]*halfy-arrayy[i]*halfy)-(sizey-1)/2
    dist#=cx#*cx#*stretch#*stretch#+cy#*cy#
    If fmin#>dist# Then
      fmin#=dist#
      layer_x=arrayx[i]
      layer_y=arrayy[i]
    EndIf
  Next

  If check=1 Then
    If layer_x<0 Then layer_x=0 ; outside=1
    If layer_y<0 Then layer_y=0 ; outside=1
    If layer_x>layer.sizex-1 Then layer_x=layer.sizex-1 ; outside=1
    If layer_y>layer.sizey-1 Then layer_y=layer.sizey-1 ; outside=1
  EndIf

  Return 1-outside
End Function





'---------------------------------------------------------------------
'layer: layer handle
'---------------------------------------------------------------------
Function layer_iso1_draw(layer:bblayer)
  Local anims
  Local arrayx[3]
  Local arrayy[3]
  Local bank
  Local count
  Local endx
  Local endy
  Local factor
  Local Frame
  Local halfx
  Local halfy
  Local i
  Local image:timage
  Local mode
  Local Offset
  Local posx
  Local px
  Local py
  Local sizex
  Local sizey
  Local start
  Local startx
  Local starty
  Local stepx
  Local stepy
  Local value
  Local x
  Local xx
  Local y
  Local yy

  If layer=Null Then Return
  If layer.tile=Null Then Return

  anims =layer.tile.anims
  count =layer.tile.count
  factor=layer.tile.factor
  image =layer.tile.image
  sizex =layer.tile.sizex
  sizey =layer.tile.sizey-factor

  If image=Null Then Return
  If sizex<tile_minsize Then sizex=tile_minsize
  If sizey<tile_minsize Then sizey=tile_minsize

  px=map_getscreen(layer.posx,layer.parax,map_scrollx)
  py=map_getscreen(layer.posy,layer.paray,map_scrolly)

  If (sizey And 1)=0 Then
    stepx=sizex+2
    stepy=sizey
  Else
    stepx=sizex
    stepy=sizey+1
  EndIf

  halfx=stepx/2
  halfy=stepy/2
  posx=(layer.sizey-1)*halfx

  layer_iso1_coord(layer,0,-factor) 'top_left
  arrayx[0]=layer_x
  arrayy[0]=layer_y

  layer_iso1_coord(layer,0,map_height) 'bottom_left
  arrayx[1]=layer_x
  arrayy[1]=layer_y

  layer_iso1_coord(layer,map_width,-factor) 'top_right
  arrayx[2]=layer_x
  arrayy[2]=layer_y

  layer_iso1_coord(layer,map_width,map_height) 'bottom_right
  arrayx[3]=layer_x
  arrayy[3]=layer_y

  startx=layer.sizex-1
  starty=layer.sizey-1

  For i=0 To 3
    If arrayx[i]<startx Then startx=arrayx[i]
    If arrayy[i]<starty Then starty=arrayy[i]
    If arrayx[i]>endx Then endx=arrayx[i]
    If arrayy[i]>endy Then endy=arrayy[i]
  Next

  If startx<0 Then startx=0
  If starty<0 Then starty=0
  If endx>layer.sizex-1 Then endx=layer.sizex-1
  If endy>layer.sizey-1 Then endy=layer.sizey-1

  For y=starty To endy
    For x=startx To endx
      xx=px+posx+(x-y)*halfx
      yy=py+(x+y)*halfy

      If xx=>-stepx And yy=>-stepy-factor And xx<=map_width And yy<=map_height Then

        If layer.depth1=4 Then
          Offset=((y*layer.sizex+x)/2)
          mode  =((y*layer.sizex+x) & 1)*4
          value =(PeekByte(layer.bank1,Offset) Shr mode) & 15
        ElseIf layer.depth1=8 Then
          Offset=y*layer.sizex+x
          value =PeekByte(layer.bank1,Offset)
        ElseIf layer.depth1=12 Then
          Offset=((y*layer.sizex+x)*3)/2
          mode  =((y*layer.sizex+x) & 1)*4
          value =(PeekShort(layer.bank1,Offset) Shr mode) & 4095
        ElseIf layer.depth1=16 Then
          Offset=(y*layer.sizex+x)*2
          value =PeekShort(layer.bank1,Offset)
        EndIf

        If value=>1 And value<=anims Then
          bank=PeekInt(layer.tile.banka,value*4-4)
          start=PeekShort(bank,10)
          Frame=PeekShort(bank,start*4+8)
        Else
          Frame=value-anims
        EndIf

        If Frame=>1 And Frame<=count Then DrawImage image,xx,yy,Frame-1

      EndIf
    Next
  Next
End Function





'---------------------------------------------------------------------
'layer: layer handle
'x:     position x
'y:     position y
'
'RETURN:         0=error, 1=ok
'RETURN LAYER_X: screen pos x
'RETURN LAYER_Y: screen pos y
'---------------------------------------------------------------------
Function layer_iso1_pos(layer:bblayer,x,y)
  Local factor
  Local halfx
  Local halfy
  Local posx
  Local px
  Local py
  Local sizex
  Local sizey
  Local stepx
  Local stepy

  If layer=Null Then Return

  If layer.tile<>Null Then
    factor=layer.tile.factor
    sizex =layer.tile.sizex
    sizey =layer.tile.sizey-factor
  Else
    sizex=tile_default
    sizey=tile_default
  EndIf

  If sizex<tile_minsize Then sizex=tile_minsize
  If sizey<tile_minsize Then sizey=tile_minsize

  px=map_getscreen(layer.posx,layer.parax,map_scrollx)
  py=map_getscreen(layer.posy,layer.paray,map_scrolly)

  If (sizey And 1)=0 Then
    stepx=sizex+2
    stepy=sizey
  Else
    stepx=sizex
    stepy=sizey+1
  EndIf

  halfx=stepx/2
  halfy=stepy/2
  posx=(layer.sizey-1)*halfx

  layer_x=px+posx+(x-y)*halfx
  layer_y=py+(x+y)*halfy
  Return 1
End Function





'---------------------------------------------------------------------
'layer: layer handle
'
'RETURN LAYER_WIDTH:  layer width in pixel
'RETURN LAYER_HEIGHT: layer height in pixel
'---------------------------------------------------------------------
Function layer_iso1_size(layer:bblayer)
  Local factor
  Local halfx
  Local halfy
  Local sizex
  Local sizey
  Local stepx
  Local stepy

  If layer=Null Then Return

  If layer.tile<>Null Then
    factor=layer.tile.factor
    sizex =layer.tile.sizex
    sizey =layer.tile.sizey-factor
  Else
    sizex=tile_default
    sizey=tile_default
  EndIf

  If sizex<tile_minsize Then sizex=tile_minsize
  If sizey<tile_minsize Then sizey=tile_minsize

  If (sizey And 1)=0 Then
    stepx=sizex+2
    stepy=sizey
  Else
    stepx=sizex
    stepy=sizey+1
  EndIf

  halfx=stepx/2
  halfy=stepy/2

  layer_width =sizex+(layer.sizex+layer.sizey-2)*halfx
  layer_height=sizey+(layer.sizex+layer.sizey-2)*halfy+factor
End Function
'}layer_iso1.bmx

'Sourcefile: layer_iso2.bmx
'{
'Import "bbtype.bmx"
'Import "bbvkey.bmx"

'---------------------------------------------------------------------
'layer: layer handle
'x:     screen x
'y:     screen y
'check: bound check
'
'RETURN:         0=outside, 1=inside (only if check is enabled)
'RETURN LAYER_X: tile pos x
'RETURN LAYER_Y: tile pos y
'---------------------------------------------------------------------
Function layer_iso2_coord(layer:bblayer,x,y,check=0)
  Local arrayx[8]
  Local arrayy[8]
  Local corr
  Local cx#
  Local cy#
  Local dist#
  Local factor
  Local halfx
  Local halfy
  Local i
  Local fmin#
  Local outside
  Local px
  Local py
  Local sizex
  Local sizey
  Local stepx
  Local stepy
  Local stretch#
  Local xx
  Local yy

  If layer=Null Then Return

  If layer.tile<>Null Then
    factor=layer.tile.factor
    sizex =layer.tile.sizex
    sizey =layer.tile.sizey-factor
  Else
    sizex=tile_default
    sizey=tile_default
  EndIf

  If sizex<tile_minsize Then sizex=tile_minsize
  If sizey<tile_minsize Then sizey=tile_minsize

  px=map_getscreen(layer.posx,layer.parax,map_scrollx)
  py=map_getscreen(layer.posy,layer.paray,map_scrolly)

  If (sizey And 1)=0 Then
    stepx=sizex+2
    stepy=sizey
  Else
    stepx=sizex
    stepy=sizey+1
  EndIf

  halfx=stepx/2
  halfy=stepy/2
  stretch#=stepy/stepx
  fmin#=1000000

  yy=(y-py)/halfy
  If ((yy+layer.start) And 1)=0 Then
    xx=(x-px)/stepx
    corr=0
  Else
    xx=(x-px-halfx)/stepx
    corr=1
  EndIf

  arrayx[0]=xx        ; arrayy[0]=yy-2
  arrayx[1]=xx+corr-1 ; arrayy[1]=yy-1
  arrayx[2]=xx+corr   ; arrayy[2]=yy-1
  arrayx[3]=xx-1      ; arrayy[3]=yy
  arrayx[4]=xx        ; arrayy[4]=yy
  arrayx[5]=xx+1      ; arrayy[5]=yy
  arrayx[6]=xx+corr-1 ; arrayy[6]=yy+1
  arrayx[7]=xx+corr   ; arrayy[7]=yy+1
  arrayx[8]=xx        ; arrayy[8]=yy+2

  For i=0 To 8
    corr=(arrayy[i]+layer.start) & 1
    cx#=(x-px-arrayx[i]*stepx-corr*halfx)-(sizex-1)/2.0
    cy#=(y-py-arrayy[i]*halfy)-(sizey-1)/2.0
    dist#=cx#*cx#*stretch#*stretch#+cy#*cy#
    If fmin#>dist# Then
      fmin#=dist#
      layer_x=arrayx[i]
      layer_y=arrayy[i]
    EndIf
  Next

  If check=1 Then
    If layer_x<0 Then layer_x=0 ; outside=1
    If layer_y<0 Then layer_y=0 ; outside=1
    If layer_x>layer.sizex-1 Then layer_x=layer.sizex-1 ; outside=1
    If layer_y>layer.sizey-1 Then layer_y=layer.sizey-1 ; outside=1
  EndIf

  Return 1-outside
End Function





'---------------------------------------------------------------------
'layer: layer handle
'---------------------------------------------------------------------
Function layer_iso2_draw(layer:bblayer)
  Local anims
  Local bank
  Local count
  Local endx
  Local endy
  Local factor
  Local Frame
  Local halfx
  Local halfy
  Local image:timage
  Local mode
  Local Offset
  Local px
  Local py
  Local sizex
  Local sizey
  Local start
  Local startx
  Local starty
  Local stepx
  Local stepy
  Local value
  Local x
  Local xx
  Local y
  Local yy

  If layer=Null Then Return
  If layer.tile=Null Then Return

  anims =layer.tile.anims
  count =layer.tile.count
  factor=layer.tile.factor
  image =layer.tile.image
  sizex =layer.tile.sizex
  sizey =layer.tile.sizey-factor

  If image=Null Then Return
  If sizex<tile_minsize Then sizex=tile_minsize
  If sizey<tile_minsize Then sizey=tile_minsize

  px=map_getscreen(layer.posx,layer.parax,map_scrollx)
  py=map_getscreen(layer.posy,layer.paray,map_scrolly)

  If (sizey And 1)=0 Then
    stepx=sizex+2
    stepy=sizey
  Else
    stepx=sizex
    stepy=sizey+1
  EndIf

  halfx=stepx/2
  halfy=stepy/2

  startx=(-halfx-px)/stepx
  starty=(-halfy-py-factor)/halfy
  endx=(map_width-px)/stepx
  endy=(map_height-py)/halfy

  If startx<0 Then startx=0
  If starty<0 Then starty=0
  If endx>layer.sizex-1 Then endx=layer.sizex-1
  If endy>layer.sizey-1 Then endy=layer.sizey-1

  For y=starty To endy
    For x=startx To endx

      If layer.depth1=4 Then
        Offset=((y*layer.sizex+x)/2)
        mode  =((y*layer.sizex+x) & 1)*4
        value =(PeekByte(layer.bank1,Offset) Shr mode) & 15
      ElseIf layer.depth1=8 Then
        Offset=y*layer.sizex+x
        value =PeekByte(layer.bank1,Offset)
      ElseIf layer.depth1=12 Then
        Offset=((y*layer.sizex+x)*3)/2
        mode  =((y*layer.sizex+x) & 1)*4
        value =(PeekShort(layer.bank1,Offset) Shr mode) & 4095
      ElseIf layer.depth1=16 Then
        Offset=(y*layer.sizex+x)*2
        value =PeekShort(layer.bank1,Offset)
      EndIf

      If value=>1 And value<=anims Then
        bank=PeekInt(layer.tile.banka,value*4-4)
        start=PeekShort(bank,10)
        Frame=PeekShort(bank,start*4+8)
      Else
        Frame=value-anims
      EndIf

      If Frame=>1 And Frame<=count Then
        xx=px+x*stepx+((y+layer.start) & 1)*halfx
        yy=py+y*halfy
        DrawImage image,xx,yy,Frame-1
      EndIf

    Next
  Next
End Function





'---------------------------------------------------------------------
'layer: layer handle
'x:     position x
'y:     position y
'
'RETURN:         0=error, 1=ok
'RETURN LAYER_X: screen pos x
'RETURN LAYER_Y: screen pos y
'---------------------------------------------------------------------
Function layer_iso2_pos(layer:bblayer,x,y)
  Local factor
  Local halfx
  Local halfy
  Local px
  Local py
  Local sizex
  Local sizey
  Local stepx
  Local stepy

  If layer=Null Then Return

  If layer.tile<>Null Then
    factor=layer.tile.factor
    sizex =layer.tile.sizex
    sizey =layer.tile.sizey-factor
  Else
    sizex=tile_default
    sizey=tile_default
  EndIf

  If sizex<tile_minsize Then sizex=tile_minsize
  If sizey<tile_minsize Then sizey=tile_minsize

  px=map_getscreen(layer.posx,layer.parax,map_scrollx)
  py=map_getscreen(layer.posy,layer.paray,map_scrolly)

  If (sizey And 1)=0 Then
    stepx=sizex+2
    stepy=sizey
  Else
    stepx=sizex
    stepy=sizey+1
  EndIf

  halfx=stepx/2
  halfy=stepy/2

  layer_x=px+x*stepx+((y+layer.start) & 1)*halfx
  layer_y=py+y*halfy
  Return 1
End Function





'---------------------------------------------------------------------
'layer: layer handle
'
'RETURN LAYER_WIDTH:  layer width in pixel
'RETURN LAYER_HEIGHT: layer height in pixel
'---------------------------------------------------------------------
Function layer_iso2_size(layer:bblayer)
  Local factor
  Local halfx
  Local halfy
  Local sizex
  Local sizey
  Local stepx
  Local stepy

  If layer=Null Then Return

  If layer.tile<>Null Then
    factor=layer.tile.factor
    sizex =layer.tile.sizex
    sizey =layer.tile.sizey-factor
  Else
    sizex=tile_default
    sizey=tile_default
  EndIf

  If sizex<tile_minsize Then sizex=tile_minsize
  If sizey<tile_minsize Then sizey=tile_minsize

  If (sizey And 1)=0 Then
    stepx=sizex+2
    stepy=sizey
  Else
    stepx=sizex
    stepy=sizey+1
  EndIf

  halfx=stepx/2
  halfy=stepy/2

  layer_width =layer.sizex*stepx-stepx+sizex+(layer.sizey>1)*halfx
  layer_height=layer.sizey*halfy-halfy+sizey+factor
End Function
'}layer_iso2.bmx

'Sourcefile: layer_map.bmx
'{
'Import "bbtype.bmx"
'Import "bbvkey.bmx"

'---------------------------------------------------------------------
'layer:       layer handle
'spritex:     sprite x (screen)
'spritey:     sprite y (screen)
'sprite:      sprite handle
'spriteframe: sprite frame
'RETURN:      0=no collision
'             1=collision
'---------------------------------------------------------------------
Function layer_map_collision(layer:bblayer,spritex,spritey,sprite:timage,spriteframe=0)
  Local anims
  Local bank
  Local count
  Local endx
  Local endy
  Local factor
  Local Frame
  Local image:timage
  Local mode
  Local Offset
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

  If layer=Null Then Return
  If layer.tile=Null Then Return

  anims =layer.tile.anims
  count =layer.tile.count
  factor=layer.tile.factor
  image =layer.tile.image
  sizex =layer.tile.sizex
  sizey =layer.tile.sizey-factor

  If image=Null Then Return
  If sprite=Null Then Return
  If sizex<tile_minsize Then sizex=tile_minsize
  If sizey<tile_minsize Then sizey=tile_minsize

  px=map_getscreen(layer.posx,layer.parax,map_scrollx)
  py=map_getscreen(layer.posy,layer.paray,map_scrolly)

  layer_map_coord(layer,spritex,spritey)
  startx=layer_x
  starty=layer_y

  layer_map_coord(layer,spritex+ImageWidth(sprite)-1,spritey+ImageHeight(sprite)-1)
  endx=layer_x
  endy=layer_y

  If startx<0 Then startx=0
  If starty<0 Then starty=0
  If endx>layer.sizex-1 Then endx=layer.sizex-1
  If endy>layer.sizey-1 Then endy=layer.sizey-1

  For y=starty To endy
    For x=startx To endx

      If layer.depth1=4 Then
        Offset=((y*layer.sizex+x)/2)
        mode  =((y*layer.sizex+x) & 1)*4
        value =(PeekByte(layer.bank1,Offset) Shr mode) & 15
      ElseIf layer.depth1=8 Then
        Offset=y*layer.sizex+x
        value =PeekByte(layer.bank1,Offset)
      ElseIf layer.depth1=12 Then
        Offset=((y*layer.sizex+x)*3)/2
        mode  =((y*layer.sizex+x) & 1)*4
        value =(PeekShort(layer.bank1,Offset) Shr mode) & 4095
      ElseIf layer.depth1=16 Then
        Offset=(y*layer.sizex+x)*2
        value =PeekShort(layer.bank1,Offset)
      EndIf

      If value=>1 And value<=anims Then
        bank=PeekInt(layer.tile.banka,value*4-4)
        start=PeekShort(bank,10)
        Frame=PeekShort(bank,start*4+8)
      Else
        Frame=value-anims
      EndIf

      If Frame=>1 And Frame<=count Then
        xx=px+x*sizex
        yy=py+y*sizey
        If ImagesCollide(image,xx,yy,Frame-1, sprite,spritex,spritey,spriteframe)=1 Then Return 1
      EndIf

    Next
  Next
End Function





'---------------------------------------------------------------------
'layer:  layer handle
'x:      screen x
'y:      screen y
'check:  bound check
'
'RETURN:         0=outside, 1=inside (only if check is enabled)
'RETURN LAYER_X: tile pos x
'RETURN LAYER_Y: tile pos y
'---------------------------------------------------------------------
Function layer_map_coord(layer:bblayer,x,y,check=0)
  Local factor
  Local outside
  Local px
  Local py
  Local sizex
  Local sizey

  If layer=Null Then Return

  If layer.tile<>Null Then
    factor=layer.tile.factor
    sizex =layer.tile.sizex
    sizey =layer.tile.sizey-factor
  Else
    sizex=tile_default
    sizey=tile_default
  EndIf

  If sizex<tile_minsize Then sizex=tile_minsize
  If sizey<tile_minsize Then sizey=tile_minsize

  px=map_getscreen(layer.posx,layer.parax,map_scrollx)
  py=map_getscreen(layer.posy,layer.paray,map_scrolly)

  layer_x=(x-px)/sizex
  layer_y=(y-py)/sizey

  If (x-px)<0 Then layer_x=layer_x-1
  If (y-py)<0 Then layer_y=layer_y-1

  If check=1 Then
    If layer_x<0 Then layer_x=0 ; outside=1
    If layer_y<0 Then layer_y=0 ; outside=1
    If layer_x>layer.sizex-1 Then layer_x=layer.sizex-1 ; outside=1
    If layer_y>layer.sizey-1 Then layer_y=layer.sizey-1 ; outside=1
  EndIf

  Return 1-outside
End Function





'---------------------------------------------------------------------
'layer: layer handle
'---------------------------------------------------------------------
Function layer_map_draw(layer:bblayer)
  Local anims
  Local bank
  Local count
  Local endx
  Local endy
  Local factor
  Local Frame
  Local image:timage
  Local mode
  Local Offset
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

  If layer=Null Then Return
  If layer.tile=Null Then Return

  anims =layer.tile.anims
  count =layer.tile.count
  factor=layer.tile.factor
  image =layer.tile.image
  sizex =layer.tile.sizex
  sizey =layer.tile.sizey-factor

  If image=Null Then Return
  If sizex<tile_minsize Then sizex=tile_minsize
  If sizey<tile_minsize Then sizey=tile_minsize

  px=map_getscreen(layer.posx,layer.parax,map_scrollx)
  py=map_getscreen(layer.posy,layer.paray,map_scrolly)

  startx=(0-px)/sizex
  starty=(0-py-factor)/sizey
  endx=(map_width-px)/sizex
  endy=(map_height-py)/sizey

  If startx<0 Then startx=0
  If starty<0 Then starty=0
  If endx>layer.sizex-1 Then endx=layer.sizex-1
  If endy>layer.sizey-1 Then endy=layer.sizey-1

  For y=starty To endy
    For x=startx To endx

      If layer.depth1=4 Then
        Offset=((y*layer.sizex+x)/2)
        mode  =((y*layer.sizex+x) & 1)*4
        value =(PeekByte(layer.bank1,Offset) Shr mode) & 15
      ElseIf layer.depth1=8 Then
        Offset=y*layer.sizex+x
        value =PeekByte(layer.bank1,Offset)
      ElseIf layer.depth1=12 Then
        Offset=((y*layer.sizex+x)*3)/2
        mode  =((y*layer.sizex+x) & 1)*4
        value =(PeekShort(layer.bank1,Offset) Shr mode) & 4095
      ElseIf layer.depth1=16 Then
        Offset=(y*layer.sizex+x)*2
        value =PeekShort(layer.bank1,Offset)
      EndIf

      If value=>1 And value<=anims Then
        bank=PeekInt(layer.tile.banka,value*4-4)
        start=PeekShort(bank,10)
        Frame=PeekShort(bank,start*4+8)
      Else
        Frame=value-anims
      EndIf

      If Frame=>1 And Frame<=count Then
        xx=px+x*sizex
        yy=py+y*sizey
        If layer.mask=0 Then DrawImage image,xx,yy,Frame-1
        If layer.mask=1 Then DrawImage image,xx,yy,Frame-1
      EndIf

    Next
  Next
End Function





'---------------------------------------------------------------------
'layer:   layer handle
'---------------------------------------------------------------------
Function layer_map_draw_repeat(layer:bblayer)
  Local anims
  Local bank
  Local count
  Local factor
  Local Frame
  Local image:timage
  Local mode
  Local Offset
  Local px
  Local py
  Local sizex
  Local sizey
  Local start
  Local startx
  Local starty
  Local stepx
  Local stepy
  Local value
  Local x
  Local xx
  Local y
  Local yy

  If layer=Null Then Return
  If layer.tile=Null Then Return

  anims =layer.tile.anims
  count =layer.tile.count
  factor=layer.tile.factor
  image =layer.tile.image
  sizex =layer.tile.sizex
  sizey =layer.tile.sizey-factor

  If image=Null Then Return
  If sizex<tile_minsize Then sizex=tile_minsize
  If sizey<tile_minsize Then sizey=tile_minsize

  px=map_getscreen(layer.posx,layer.parax,map_scrollx)
  py=map_getscreen(layer.posy,layer.paray,map_scrolly)

  stepx=px/sizex+(px>0 And px<>sizex)
  stepy=py/sizey+(py>0 And py<>sizey)

  px=px-stepx*sizex
  py=py-stepy*sizey

  startx=(0-stepx) Mod layer.sizex
  starty=(0-stepy) Mod layer.sizey
  If startx<0 Then startx=layer.sizex+startx
  If starty<0 Then starty=layer.sizey+starty

  y=starty
  For yy=py To map_height
    x=startx
    For xx=px To map_width

      If layer.depth1=4 Then
        Offset=((y*layer.sizex+x)/2)
        mode  =((y*layer.sizex+x) & 1)*4
        value =(PeekByte(layer.bank1,Offset) Shr mode) & 15
      ElseIf layer.depth1=8 Then
        Offset=y*layer.sizex+x
        value =PeekByte(layer.bank1,Offset)
      ElseIf layer.depth1=12 Then
        Offset=((y*layer.sizex+x)*3)/2
        mode  =((y*layer.sizex+x) & 1)*4
        value =(PeekShort(layer.bank1,Offset) Shr mode) & 4095
      ElseIf layer.depth1=16 Then
        Offset=(y*layer.sizex+x)*2
        value =PeekShort(layer.bank1,Offset)
      EndIf

      If value=>1 And value<=anims Then
        bank=PeekInt(layer.tile.banka,value*4-4)
        start=PeekShort(bank,10)
        Frame=PeekShort(bank,start*4+8)
      Else
        Frame=value-anims
      EndIf

      If Frame=>1 And Frame<=count Then
        If layer.mask=0 Then DrawImage image,xx,yy,Frame-1
        If layer.mask=1 Then DrawImage image,xx,yy,Frame-1
      EndIf

      x=(x+1) Mod layer.sizex
      xx=xx+sizex-1
    Next
    y=(y+1) Mod layer.sizey
    yy=yy+sizey-1
  Next
End Function





'---------------------------------------------------------------------
'layer:  layer handle
'x:      tile pos x
'y:      tile pos y
'
'RETURN:         0=error, 1=ok
'RETURN LAYER_X: screen pos x
'RETURN LAYER_Y: screen pos y
'---------------------------------------------------------------------
Function layer_map_pos(layer:bblayer,x,y)
  Local factor
  Local px
  Local py
  Local sizex
  Local sizey

  If layer=Null Then Return

  If layer.tile<>Null Then
    factor=layer.tile.factor
    sizex =layer.tile.sizex
    sizey =layer.tile.sizey-factor
  Else
    sizex=tile_default
    sizey=tile_default
  EndIf

  If sizex<tile_minsize Then sizex=tile_minsize
  If sizey<tile_minsize Then sizey=tile_minsize

  px=map_getscreen(layer.posx,layer.parax,map_scrollx)
  py=map_getscreen(layer.posy,layer.paray,map_scrolly)

  layer_x=px+x*sizex
  layer_y=py+y*sizey
  Return 1
End Function





'---------------------------------------------------------------------
'layer: layer handle
'
'RETURN LAYER_WIDTH:  layer width in pixel
'RETURN LAYER_HEIGHT: layer height in pixel
'---------------------------------------------------------------------
Function layer_map_size(layer:bblayer)
  Local factor
  Local sizex
  Local sizey

  If layer=Null Then Return

  If layer.tile<>Null Then
    factor=layer.tile.factor
    sizex =layer.tile.sizex
    sizey =layer.tile.sizey-factor
  Else
    sizex=tile_default
    sizey=tile_default
  EndIf

  If sizex<tile_minsize Then sizex=tile_minsize
  If sizey<tile_minsize Then sizey=tile_minsize

  layer_width =layer.sizex*sizex
  layer_height=layer.sizey*sizey+factor
End Function
'}layer_map.bmx



'Sourcefile: md5.bmx
'{
'Import "bbtype.bmx"
'Import "bbvkey.bmx"

Global md5_table[0+1]





'---------------------------------------------------------------------
'message: message string
'RETURN:  md5-fingerprint (16 byte)
'---------------------------------------------------------------------
Function md5$(message$)
  Local ascii
  Local blocks
  Local i
  Local length
  Local md5_a
  Local md5_b
  Local md5_c
  Local md5_d
  Local md5_aa
  Local md5_bb
  Local md5_cc
  Local md5_dd

  length=Len(message$)
  'Print "inlen: "+length
  

  blocks=((length+8) Shr 6)+1
  
  md5_table=New Int[blocks*16-1+1]

  'Global md5_table[blocks*16-1+1]

  For i=0 To length-1
    ascii=Asc(Mid(message$,i+1,1))
    'Print "I:"+ ascii
    md5_table[(i Shr 2)]=md5_table[(i Shr 2)] | (ascii Shl ((i Mod 4)*8))
  Next 

  md5_table[(i Shr 2)]=md5_table[(i Shr 2)] | (128 Shl ((i Mod 4)*8))
  md5_table[blocks*16-2]=length*8
    
  md5_a = $67452301
  md5_b = $EFCDAB89
  md5_c = $98BADCFE
  md5_d = $10325476

  For i=0 To blocks*16-1 Step 16
    md5_aa = md5_a
    md5_bb = md5_b
    md5_cc = md5_c
    md5_dd = md5_d

    md5_a = md5_ff(md5_a, md5_b, md5_c, md5_d, md5_table[i+00], 07, $D76AA478)
    md5_d = md5_ff(md5_d, md5_a, md5_b, md5_c, md5_table[i+01], 12, $E8C7B756)
    md5_c = md5_ff(md5_c, md5_d, md5_a, md5_b, md5_table[i+02], 17, $242070DB)
    md5_b = md5_ff(md5_b, md5_c, md5_d, md5_a, md5_table[i+03], 22, $C1BDCEEE)
    md5_a = md5_ff(md5_a, md5_b, md5_c, md5_d, md5_table[i+04], 07, $F57C0FAF)
    md5_d = md5_ff(md5_d, md5_a, md5_b, md5_c, md5_table[i+05], 12, $4787C62A)
    md5_c = md5_ff(md5_c, md5_d, md5_a, md5_b, md5_table[i+06], 17, $A8304613)
    md5_b = md5_ff(md5_b, md5_c, md5_d, md5_a, md5_table[i+07], 22, $FD469501)
    md5_a = md5_ff(md5_a, md5_b, md5_c, md5_d, md5_table[i+08], 07, $698098D8)
    md5_d = md5_ff(md5_d, md5_a, md5_b, md5_c, md5_table[i+09], 12, $8B44F7AF)
    md5_c = md5_ff(md5_c, md5_d, md5_a, md5_b, md5_table[i+10], 17, $FFFF5BB1)
    md5_b = md5_ff(md5_b, md5_c, md5_d, md5_a, md5_table[i+11], 22, $895CD7BE)
    md5_a = md5_ff(md5_a, md5_b, md5_c, md5_d, md5_table[i+12], 07, $6B901122)
    md5_d = md5_ff(md5_d, md5_a, md5_b, md5_c, md5_table[i+13], 12, $FD987193)
    md5_c = md5_ff(md5_c, md5_d, md5_a, md5_b, md5_table[i+14], 17, $A679438E)
    md5_b = md5_ff(md5_b, md5_c, md5_d, md5_a, md5_table[i+15], 22, $49B40821)

    md5_a = md5_gg(md5_a, md5_b, md5_c, md5_d, md5_table[i+01], 05, $F61E2562)
    md5_d = md5_gg(md5_d, md5_a, md5_b, md5_c, md5_table[i+06], 09, $C040B340)
    md5_c = md5_gg(md5_c, md5_d, md5_a, md5_b, md5_table[i+11], 14, $265E5A51)
    md5_b = md5_gg(md5_b, md5_c, md5_d, md5_a, md5_table[i+00], 20, $E9B6C7AA)
    md5_a = md5_gg(md5_a, md5_b, md5_c, md5_d, md5_table[i+05], 05, $D62F105D)
    md5_d = md5_gg(md5_d, md5_a, md5_b, md5_c, md5_table[i+10], 09, $02441453)
    md5_c = md5_gg(md5_c, md5_d, md5_a, md5_b, md5_table[i+15], 14, $D8A1E681)
    md5_b = md5_gg(md5_b, md5_c, md5_d, md5_a, md5_table[i+04], 20, $E7D3FBC8)
    md5_a = md5_gg(md5_a, md5_b, md5_c, md5_d, md5_table[i+09], 05, $21E1CDE6)
    md5_d = md5_gg(md5_d, md5_a, md5_b, md5_c, md5_table[i+14], 09, $C33707D6)
    md5_c = md5_gg(md5_c, md5_d, md5_a, md5_b, md5_table[i+03], 14, $F4D50D87)
    md5_b = md5_gg(md5_b, md5_c, md5_d, md5_a, md5_table[i+08], 20, $455A14ED)
    md5_a = md5_gg(md5_a, md5_b, md5_c, md5_d, md5_table[i+13], 05, $A9E3E905)
    md5_d = md5_gg(md5_d, md5_a, md5_b, md5_c, md5_table[i+02], 09, $FCEFA3F8)
    md5_c = md5_gg(md5_c, md5_d, md5_a, md5_b, md5_table[i+07], 14, $676F02D9)
    md5_b = md5_gg(md5_b, md5_c, md5_d, md5_a, md5_table[i+12], 20, $8D2A4C8A)

    md5_a = md5_hh(md5_a, md5_b, md5_c, md5_d, md5_table[i+05], 04, $FFFA3942)
    md5_d = md5_hh(md5_d, md5_a, md5_b, md5_c, md5_table[i+08], 11, $8771F681)
    md5_c = md5_hh(md5_c, md5_d, md5_a, md5_b, md5_table[i+11], 16, $6D9D6122)
    md5_b = md5_hh(md5_b, md5_c, md5_d, md5_a, md5_table[i+14], 23, $FDE5380C)
    md5_a = md5_hh(md5_a, md5_b, md5_c, md5_d, md5_table[i+01], 04, $A4BEEA44)
    md5_d = md5_hh(md5_d, md5_a, md5_b, md5_c, md5_table[i+04], 11, $4BDECFA9)
    md5_c = md5_hh(md5_c, md5_d, md5_a, md5_b, md5_table[i+07], 16, $F6BB4B60)
    md5_b = md5_hh(md5_b, md5_c, md5_d, md5_a, md5_table[i+10], 23, $BEBFBC70)
    md5_a = md5_hh(md5_a, md5_b, md5_c, md5_d, md5_table[i+13], 04, $289B7EC6)
    md5_d = md5_hh(md5_d, md5_a, md5_b, md5_c, md5_table[i+00], 11, $EAA127FA)
    md5_c = md5_hh(md5_c, md5_d, md5_a, md5_b, md5_table[i+03], 16, $D4EF3085)
    md5_b = md5_hh(md5_b, md5_c, md5_d, md5_a, md5_table[i+06], 23, $04881D05)
    md5_a = md5_hh(md5_a, md5_b, md5_c, md5_d, md5_table[i+09], 04, $D9D4D039)
    md5_d = md5_hh(md5_d, md5_a, md5_b, md5_c, md5_table[i+12], 11, $E6DB99E5)
    md5_c = md5_hh(md5_c, md5_d, md5_a, md5_b, md5_table[i+15], 16, $1FA27CF8)
    md5_b = md5_hh(md5_b, md5_c, md5_d, md5_a, md5_table[i+02], 23, $C4AC5665)

    md5_a = md5_ii(md5_a, md5_b, md5_c, md5_d, md5_table[i+00], 06, $F4292244)
    md5_d = md5_ii(md5_d, md5_a, md5_b, md5_c, md5_table[i+07], 10, $432AFF97)
    md5_c = md5_ii(md5_c, md5_d, md5_a, md5_b, md5_table[i+14], 15, $AB9423A7)
    md5_b = md5_ii(md5_b, md5_c, md5_d, md5_a, md5_table[i+05], 21, $FC93A039)
    md5_a = md5_ii(md5_a, md5_b, md5_c, md5_d, md5_table[i+12], 06, $655B59C3)
    md5_d = md5_ii(md5_d, md5_a, md5_b, md5_c, md5_table[i+03], 10, $8F0CCC92)
    md5_c = md5_ii(md5_c, md5_d, md5_a, md5_b, md5_table[i+10], 15, $FFEFF47D)
    md5_b = md5_ii(md5_b, md5_c, md5_d, md5_a, md5_table[i+01], 21, $85845DD1)
    md5_a = md5_ii(md5_a, md5_b, md5_c, md5_d, md5_table[i+08], 06, $6FA87E4F)
    md5_d = md5_ii(md5_d, md5_a, md5_b, md5_c, md5_table[i+15], 10, $FE2CE6E0)
    md5_c = md5_ii(md5_c, md5_d, md5_a, md5_b, md5_table[i+06], 15, $A3014314)
    md5_b = md5_ii(md5_b, md5_c, md5_d, md5_a, md5_table[i+13], 21, $4E0811A1)
    md5_a = md5_ii(md5_a, md5_b, md5_c, md5_d, md5_table[i+04], 06, $F7537E82)
    md5_d = md5_ii(md5_d, md5_a, md5_b, md5_c, md5_table[i+11], 10, $BD3AF235)
    md5_c = md5_ii(md5_c, md5_d, md5_a, md5_b, md5_table[i+02], 15, $2AD7D2BB)
    md5_b = md5_ii(md5_b, md5_c, md5_d, md5_a, md5_table[i+09], 21, $EB86D391)

    md5_a = md5_a + md5_aa
    md5_b = md5_b + md5_bb
    md5_c = md5_c + md5_cc
    md5_d = md5_d + md5_dd
  Next
'Print message$
  Local bank:TBank=CreateBank(4*4)
  PokeInt(bank,0,md5_a)
  PokeInt(bank,4,md5_b)
  PokeInt(bank,8,md5_c)
  PokeInt(bank,12,md5_d)
  Local s,out_txt$
  For s=0 To 15
	'Print "M:"+PeekByte(bank,s) 
	out_txt$=out_txt$+Chr(PeekByte(bank,s) )
  Next
  bank=Null
  'Print "--"+out_txt$'+Chr(13)+Chr(10)
  'Print Len(out_txt$)
  'Return Lower$(md5_int2chr$(md5_a) + md5_int2chr$(md5_b) + md5_int2chr$(md5_c) + md5_int2chr$(md5_d))
  'Mystery changes 4 Bmax needed.
  Return Lower(out_txt$)'Lower$(md5_int2chr$(md5_d) + md5_int2chr$(md5_c) + md5_int2chr$(md5_b) + md5_int2chr$(md5_a))
End Function





'---------------------------------------------------------------------
'x,y,z:  old values
'RETURN: new value
'---------------------------------------------------------------------
Function md5_f(x, y, z)
  Return (x & y) | ((~x) & z)
End Function





'---------------------------------------------------------------------
'x,y,z:  old values
'RETURN: new value
'---------------------------------------------------------------------
Function md5_g(x, y, z)
  Return (x & z) | (y & (~z))
End Function





'---------------------------------------------------------------------
'x,y,z:  old values
'RETURN: new value
'---------------------------------------------------------------------
Function md5_h(x, y, z)
  Return (x ~ y ~ z)
End Function





'---------------------------------------------------------------------
'x,y,z:  old values
'RETURN: new value
'---------------------------------------------------------------------
Function md5_i(x, y, z)
  Return (y ~ (x | (~z)))
End Function





'---------------------------------------------------------------------
'a,b,c,d,x: old values
's:         bit shift
'ac:        accumulator
'RETURN:    new value
'---------------------------------------------------------------------
Function md5_ff(a, b, c, d, x, s, ac)
  a = (a + ((md5_f(b, c, d) + x) + ac))
  a = md5_rotateleft(a, s)
  Return a + b
End Function





'---------------------------------------------------------------------
'a,b,c,d,x: old values
's:         bit shift
'ac:        accumulator
'RETURN:    new value
'---------------------------------------------------------------------
Function md5_gg(a, b, c, d, x, s, ac)
  a = (a + ((md5_g(b, c, d) + x) + ac))
  a = md5_rotateleft(a, s)
  Return a + b
End Function





'---------------------------------------------------------------------
'a,b,c,d,x: old values
's:         bit shift
'ac:        accumulator
'RETURN:    new value
'---------------------------------------------------------------------
Function md5_hh(a, b, c, d, x, s, ac)
  a = (a + ((md5_h(b, c, d) + x) + ac))
  a = md5_rotateleft(a, s)
  Return a + b
End Function





'---------------------------------------------------------------------
'a,b,c,d,x: old values
's:         bit shift
'ac:        accumulator
'RETURN:    new value
'---------------------------------------------------------------------
Function md5_ii(a, b, c, d, x, s, ac)
  a = (a + ((md5_i(b, c, d) + x) + ac))
  a = md5_rotateleft(a, s)
  Return a + b
End Function





'---------------------------------------------------------------------
'value:  integer value
'shift:  bit shift
'RETURN: shifted value
'---------------------------------------------------------------------
Function md5_rotateleft(value,shift)
  Return (value Shl shift) | (value Shr (32-shift))
End Function





'---------------------------------------------------------------------
'value:  integer value
'RETURN: 4 byte string
'---------------------------------------------------------------------
Function md5_int2chr$(value)
  Local Byten
  Local i
  Local txt$

  txt$=value & $000000FF
  For i=1 To 3
    Byten=(value Shr (i*8)) & $000000FF
	'Print byten
    txt$=txt$+Chr$(Byten)
  Next
  Return txt$
End Function
'}md5.bmx

'Sourcefile: sha1.bmx
'{
'Import "bbtype.bmx"
'Import "bbvkey.bmx"

Global sha1_low
Global sha1_high
Global sha1_index
Global sha1_hash[5]
Global sha1_message[64]





'---------------------------------------------------------------------
'txt:    text string
'RETURN: sha1-fingerprint (20 byte)
'---------------------------------------------------------------------
Function sha1$(txt$)
  Local ascii
  Local i
  Local length

  sha1_reset()
  length=Len(txt$)

  For i=1 To length
    ascii=Asc(Mid$(txt$,i,1))
    sha1_message[sha1_index]=ascii
    sha1_index=sha1_index+1

    sha1_low=sha1_low+8
    If sha1_low=0 Then
      sha1_high=sha1_high+1
      If sha1_high=0 Then Return
    EndIf

    If sha1_index=64 Then sha1_process()
  Next

  Return sha1_result$()
End Function





'---------------------------------------------------------------------
Function sha1_pad()
  If sha1_index>55 Then

    sha1_message[sha1_index]=$80
    sha1_index=sha1_index+1

    While sha1_index<64
      sha1_message[sha1_index]=0
      sha1_index=sha1_index+1
    Wend

    sha1_process()

    While sha1_index<56
      sha1_message[sha1_index]=0
      sha1_index=sha1_index+1
    Wend

  Else

    sha1_message[sha1_index]=$80
    sha1_index=sha1_index+1

    While sha1_index<56
      sha1_message[sha1_index]=0
      sha1_index=sha1_index+1
    Wend

  EndIf

  sha1_message[56]=sha1_high Shr 24
  sha1_message[57]=sha1_high Shr 16
  sha1_message[58]=sha1_high Shr 8
  sha1_message[59]=sha1_high
  sha1_message[60]=sha1_low Shr 24
  sha1_message[61]=sha1_low Shr 16
  sha1_message[62]=sha1_low Shr 8
  sha1_message[63]=sha1_low

  sha1_process()
End Function





'---------------------------------------------------------------------
Function sha1_process()
  Local a
  Local b
  Local c
  Local d
  Local e
  Local k[4]
  Local t
  Local temp
  Local w[80]

  k[0]=$5A827999
  k[1]=$6ED9EBA1
  k[2]=$8F1BBCDC
  k[3]=$CA62C1D6

  For t=0 To 15
    w[t]=sha1_message[t*4] Shl 24
    w[t]=w[t] | sha1_message[t*4+1] Shl 16
    w[t]=w[t] | sha1_message[t*4+2] Shl 8
    w[t]=w[t] | sha1_message[t*4+3]
  Next

  For t=16 To 79
    w[t]=sha1_rotateleft(w[t-3] ~ w[t-8] ~ w[t-14] ~ w[t-16],1)
  Next

  a=sha1_hash[0]
  b=sha1_hash[1]
  c=sha1_hash[2]
  d=sha1_hash[3]
  e=sha1_hash[4]

  For t=0 To 19
    temp=sha1_rotateleft(A,5) + ((B & C) | ((~B) & D)) + E + W[t] + K[0]
    e=d
    d=c
    c=sha1_rotateleft(B,30)
    b=a
    a=temp
  Next

  For t=20 To 39
    temp=sha1_rotateleft(A,5) + (B ~ C ~ D) + E + W[t] + K[1]
    e=d
    d=c
    c=sha1_rotateleft(B,30)
    b=a
    a=temp
  Next

  For t=40 To 59
    temp=sha1_rotateleft(A,5) + ((B & C) | (B & D) | (C & D)) + E + W[t] + K[2]
    e=d
    d=c
    c=sha1_rotateleft(B,30)
    b=a
    a=temp
  Next

  For t=60 To 79
    temp=sha1_rotateleft(A,5) + (B ~ C ~ D) + E + W[t] + K[3]
    e=d
    d=c
    c=sha1_rotateleft(B,30)
    b=a
    a=temp
  Next

  sha1_hash[0]=sha1_hash[0]+a
  sha1_hash[1]=sha1_hash[1]+b
  sha1_hash[2]=sha1_hash[2]+c
  sha1_hash[3]=sha1_hash[3]+d
  sha1_hash[4]=sha1_hash[4]+e

  sha1_index=0
End Function





'---------------------------------------------------------------------
Function sha1_reset()
  Local i

  sha1_low=0
  sha1_high=0
  sha1_index=0

  sha1_hash[0]=$67452301
  sha1_hash[1]=$EFCDAB89
  sha1_hash[2]=$98BADCFE
  sha1_hash[3]=$10325476
  sha1_hash[4]=$C3D2E1F0

  For i=0 To 63
    sha1_message[i]=0
  Next
End Function





'---------------------------------------------------------------------
'RETURN: sha1-fingerprint
'---------------------------------------------------------------------
Function sha1_result$()
  Local Byten
  Local i
  Local txt$=""

  sha1_pad()
  txt$=""
  For i=0 To 19
    Byten=sha1_hash[i / 4.0]  /(256.0^(3-(i & 3)))  
    
	byten=Byten Mod 256
	If byten < 0 Then
		If (i & 3)= 3 Then byten = byten+1
		byten=byten+255
	EndIf
	'Print byten
    txt$=txt$+Chr$(Byten)
  Next
  'Print "Raus: "+txt$
  Return txt$
End Function





'---------------------------------------------------------------------
'value:  integer value
'shift:  bit shift
'RETURN: shifted value
'---------------------------------------------------------------------
Function sha1_rotateleft(value,shift)
  Return ((value Shl shift) | (value Shr (32-shift)))
End Function
'}sha1.bmx

'Sourcefile: tile.bmx
'{
'Import "bbtype.bmx"
'Import "bbvkey.bmx"

Global tile_list:TList=New TList
'---------------------------------------------------------------------ANIMBANK
'offset byte description
'---------------------------------------------------------------------
'00     2    animation frames
'02     2    animation start
'04     1    animation mode (1=paused, 2=forward, 3=backward)
'05     1    default tile value
'06     4    last millisecs time
'10     2    current frame number
'12...  2    animation image
'14...  2    animation time
'---------------------------------------------------------------------

Type bbtile Extends TBBType

	Method New()
		Add(tile_list)
	End Method

	Method After:bbtile()
		Local t:TLink
		t=_link.NextLink()
		If t Return bbtile(t.value())
	End Method

	Method Before:bbtile()
		Local t:TLink
		t=_link.PrevLink()
		If t Return bbtile(t.value())
	End Method

  Field anims  'animation count
  Field banka:TBank  'animations handle bank
  Field bankd:TBank  'default tile value
  Field count  'frame count
  Field factor 'y-factor
  Field file$  'filename
  Field image:Timage  'image handle
  Field mask   'mask color
  Field sizex  'tile size x
  Field sizey  'tile size y
End Type

Const tile_default=20
Const tile_minsize=08





'---------------------------------------------------------------------
Function tile_animate()
  Local bank
  Local Frame
  Local frames
  Local i
  Local layer:bblayer
  Local mode
  Local tile:bbtile
  Local time1,time2
  Local timediff
  Local timemax

  time2=MilliSecs()

  For tile=EachIn tile_list
    For i=1 To tile.anims

      bank    =PeekInt  (tile.banka,i*4-4)
      frames  =PeekShort(bank,0)
      mode    =PeekByte (bank,4)
      time1   =PeekInt  (bank,6)
      Frame   =PeekShort(bank,10)
      timediff=time2-time1

      Repeat
        timemax=PeekShort(bank,Frame*4+10)

        If mode=1 Or mode<0 Or mode>3 Or timemax=0 Then 'PAUSE
          PokeInt bank,6,time2
          Exit
        EndIf

        If timediff<=timemax Then
          PokeInt   bank,6 ,time2-timediff
          PokeShort bank,10,Frame
          Exit
        EndIf

        timediff=timediff-timemax

        If mode=2 Then Frame=Frame+1 'FORWARD
        If mode=3 Then Frame=Frame-1 'BACKWARD

        If Frame<1 Then Frame=frames
        If Frame>frames Then Frame=1
      Forever

    Next
  Next


  For layer=EachIn layer_list
    If (layer.code=layer_image Or layer.code=layer_block) And layer.tile<>Null Then
      If layer.Frame=>1 And layer.Frame<=layer.tile.anims And layer.mode>0 Then

        bank    =PeekInt  (layer.tile.banka,layer.Frame*4-4)
        frames  =PeekShort(bank,0)
        mode    =layer.mode
        time1   =layer.time
        Frame   =layer.tmp
        timediff=time2-time1

        Repeat
          timemax=PeekShort(bank,Frame*4+10)

          If mode=1 Or mode<0 Or mode>3 Or timemax=0 Then 'PAUSE
            layer.time=time2
            Exit
          EndIf

          If timediff<=timemax Then
            layer.time=time2-timediff
            layer.tmp=Frame
            Exit
          EndIf

          timediff=timediff-timemax

          If mode=2 Then Frame=Frame+1 'FORWARD
          If mode=3 Then Frame=Frame-1 'BACKWARD

          If Frame<1 Then Frame=frames
          If Frame>frames Then Frame=1
        Forever

      EndIf
    EndIf
  Next
End Function





'---------------------------------------------------------------------
Function tile_animreset()
  Local bank
  Local i
  Local layer:bblayer
  Local start
  Local tile:bbtile
  Local time

  time=MilliSecs()

  For tile=EachIn tile_list
    For i=1 To tile.anims
      bank=PeekInt(tile.banka,i*4-4)
      start=PeekShort(bank,2)
      PokeInt bank,6,time
      PokeShort bank,10,start
    Next
  Next

  For layer=EachIn layer_list
    If layer.code=layer_image Or layer.code=layer_block Then
      layer.time=time
      layer.tmp=layer.start
      If layer.tmp<0 Then layer.tmp=1
    EndIf
  Next
End Function





'---------------------------------------------------------------------
'file:   find this tileset file name
'RETURN: tileset type handle (null=not found)
'---------------------------------------------------------------------
Function tile_find:bbtile(file$)
  Local tile:bbtile

  For tile=EachIn tile_list
    If tile.file$=file$ Then Return tile
  Next
End Function





'---------------------------------------------------------------------
'tile:   tileset handle
'anim:   anim number (1...)
'RETURN: anim value (0-255)
'---------------------------------------------------------------------
Function tile_getanimval(tile:bbtile,anim)
  Local bank

  If tile=Null Then Return
  If Not(tile.banka=Null) And anim=>1 And anim<=tile.anims Then
    bank=PeekInt(tile.banka,anim*4-4) 'animbank
    Return PeekByte(bank,5) 'default value
  EndIf
End Function





'---------------------------------------------------------------------
'tile:   tileset handle
'frame:  frame number (1...)
'RETURN: frame value (0-255)
'---------------------------------------------------------------------
Function tile_getframeval(tile:bbtile,Frame)
  If tile=Null Then Return
  If Not(tile.bankd=Null) And Frame=>1 And Frame<=tile.count Then
    Return PeekByte(tile.bankd,Frame-1)
  EndIf
End Function





'---------------------------------------------------------------------
'RETURN: number of images with loading errors
'---------------------------------------------------------------------
Function tile_load()
  Local b
  Local error
  Local g
  Local r
  Local tile:bbtile
  Local oldr,oldg,oldb
  Local pixmap:TPixmap,i%,rgb%
  Local x,y
  Local image:TImage

  For tile=EachIn tile_list
    If Not(tile.image=Null) Then
'      FreeImage tile.image
      tile.image=Null
      'tile.image=0
    EndIf
      
    r=(tile.mask & $FF0000)/$10000
    g=(tile.mask & $FF00)/$100
    b=(tile.mask & $FF)
    GetMaskColor(oldr,oldg,oldb)
    SetMaskColor r,g,b
	'Print "Maskcolor:"+r+"x"+g+"x"+b
    If tile.file$<>"" Then
      image=LoadAnimImage(map_path+tile.file$,tile.sizex,tile.sizey,0,tile.count,DYNAMICIMAGE|MASKEDIMAGE)
      tile.image=image

      If image=Null Then error=error+1

'      For i =0 To tile.count-1
'	    pixmap=LockImage(image,i)
'	    For x=0 To PixmapWidth(pixmap)-1
'			For y=0 To PixmapHeight(pixmap)-1
'				rgb=ReadPixel(pixmap,x,y)
'				If (rgb & $00FFFFFF) = (tile.mask & $00FFFFFF) Then
'				   WritePixel pixmap,x,y,(rgb & $00FFFFFF)
'			    Else
'			       WritePixel pixmap,x,y,(rgb & $00FFFFFF) + $FF000000
'				EndIf
'			Next
'		Next
'	    UnlockImage(image,i)
'	  Next
    EndIf

    SetMaskColor oldr,oldg,oldb
     
'    EndIf
  Next

  If map_image<>0 Then
    'FreeImage map_image
	map_image=Null
    'map_image=0
  EndIf

  If map_backgr$<>"" Then
    map_image=LoadImage(map_path$+map_backgr$)
    If map_image=0 Then error=error+1
  EndIf

  Return error
End Function





'---------------------------------------------------------------------
Function tile_reset()
  Local tile:bbtile

  For tile=EachIn tile_list
    If Not(tile.banka=Null) Then  tile.banka=Null
    If Not(tile.bankd=Null) Then tile.image=Null
'FreeImage tile.bankd
    If Not(tile.image=Null) Then tile.image=Null 
'FreeImage tile.image
    tile.Remove()
  Next
End Function





'---------------------------------------------------------------------
'tile: tileset handle
'anim: anim number (1...)
'frame: current anim frame (1...)
'---------------------------------------------------------------------
Function tile_setanimframe(tile:bbtile,anim,Frame)
  Local bank
  Local frames

  If tile=Null Then Return
  If Not(tile.banka=Null) And anim=>1 And anim<=tile.anims Then
    bank=PeekInt(tile.banka,anim*4-4) 'anim bank
    frames=PeekShort(bank,0)          'anim frames

    If Frame<1 Then Frame=1
    If Frame>frames Then Frame=frames
    PokeInt bank,6,MilliSecs()        'anim time
    PokeShort bank,10,Frame           'anim frame
  EndIf
End Function





'---------------------------------------------------------------------
'tile: tileset handle
'anim: anim number (1...)
'mode: anim mode (1=paused, 2=forward, 3=backward)
'---------------------------------------------------------------------
Function tile_setanimmode(tile:bbtile,anim,mode)
  Local bank

  If tile=Null Then Return
  If Not(tile.banka=Null) And anim=>1 And anim<=tile.anims Then
    bank=PeekInt(tile.banka,anim*4-4) 'anim bank
    PokeByte bank,4,mode              'anim mode
  EndIf
End Function





'---------------------------------------------------------------------
'tile:  tileset handle
'anim:  anim number (1...)
'value: default tile value (0-255)
'---------------------------------------------------------------------
Function tile_setanimval(tile:bbtile,anim,value)
  Local bank

  If tile=Null Then Return
  If Not(tile.banka=Null) And anim=>1 And anim<=tile.anims Then
    bank=PeekInt(tile.banka,anim*4-4) 'animbank
    PokeByte bank,5,value             'default value
  EndIf
End Function





'---------------------------------------------------------------------
'tile:  tileset handle
'frame: frame number (1...)
'value: default tile value (0-255)
'---------------------------------------------------------------------
Function tile_setframeval(tile:bbtile,Frame,value)
  If tile=Null Then Return
  If Not(tile.bankd=Null) And Frame=>1 And Frame<=tile.count Then
    PokeByte tile.bankd,Frame-1,value
  EndIf
End Function
'}tile.bmx

'Sourcefile: var.bmx
'{
'Import "bbtype.bmx"
'Import "bbvkey.bmx"

'---------------------------------------------------------------------
'bank:   bank handle (layer\bank4, geo\bank4, map_bank4)
'name:   search for this variable name
'RETURN: variable index (0=not found)
'---------------------------------------------------------------------
Function var_findvar(bank,name$)
  Local count
  Local i
  Local j
  Local length
  Local Size
  Local txt$
  Local varbank

  If bank=0 Then Return

  count=BankSize(bank)/4
  length=Len(name$)

  For i=1 To count
    varbank=PeekInt(bank,i*4-4)
    Size=PeekByte(varbank,0)

    If length=Size Then
      txt$=""
      For j=1 To Size
        txt$=txt$+Chr$(PeekByte(varbank,j+1))
      Next
      If txt$=name$ Then Return i
    EndIf
  Next
End Function





'---------------------------------------------------------------------
'bank:   bank handle (layer\bank4, geo\bank4, map_bank4)
'index:  variable index (1.....)
'RETURN: variable value
'---------------------------------------------------------------------
Function var_getvalue$(bank,index)
  Local i
  Local size1
  Local size2
  Local txt$
  Local varbank

  If bank=0 Then Return
  If index<1 Or index>BankSize(bank)/4 Then Return

  varbank=PeekInt (bank,index*4-4)
  size1  =PeekByte(varbank,0)
  size2  =PeekByte(varbank,1)

  For i=1 To size2
    txt$=txt$+Chr$(PeekByte(varbank,i+size1+1))
  Next

  Return txt$
End Function





'---------------------------------------------------------------------
'bank:   bank handle (layer\bank4, geo\bank4, map_bank4)
'index:  variable index (1.....)
'value:  variable value
'---------------------------------------------------------------------
Function var_setvalue(bank,index,value$)
  Local char
  Local i
  Local length
  Local size1
  Local size2
  Local varbank

  If bank=0 Then Return
  If index<1 Or index>BankSize(bank)/4 Then Return

  varbank=PeekInt (bank,index*4-4)
  size1  =PeekByte(varbank,0)
  size2  =PeekByte(varbank,1)
  length=Len(value$)
  If length>255 Then length=255

  ResizeBank varbank,2+size1+length

  For i=1 To length
    char=Asc(Mid$(value$,i,1))
    PokeByte varbank,i+size1+1,char
  Next
End Function
'}var.bmx



