; ---------------------------------------------------------------------
Procedure map_draw()
  Protected b.l, g.l, px.l, py.l, r.l, map_image_x.l, map_image_y.l, x.l, y.l
  
  If map_visible = 1
    If map_mode = 1
      r = (map_colour >> 16) & $FF
      g = (map_colour >> 8) & $FF
      b = map_colour & $FF
      StartDrawing(SpriteOutput(map_viewport))
        Box(0, 0, map_width - 1, map_height - 1, RGB(r, g, b))
      StopDrawing()
    ElseIf map_image
      UseBuffer(map_viewport)
      px = map_getscreen(map_posx, map_parax, map_scrollx)
      py = map_getscreen(map_posy, map_paray, map_scrolly)
      map_image_x = SpriteWidth(map_image)
      map_image_y = SpriteHeight(map_image)
      
      x = 0 : y = 0
      Repeat
        If map_mode = 2
          DisplaySprite(map_image, x, y)
        Else
          DisplayTransparentSprite(map_image, x, y)
        EndIf
        
        x + map_image_x
        If x > map_width : x = 0 : y + map_height : EndIf
      Until y > map_height
      
    EndIf
  EndIf
  
  ForEach layer()
    If layer()\visible = 1
      Select layer()\code
        Case #layer_map
          layer_map_draw(@layer())
        Case #layer_clone
          layer_clone_draw(@layer())
        Case #layer_image
          layer_image_draw(@layer())
        Case #layer_block
          layer_block_draw(@layer())
      EndSelect
    EndIf
  Next
  
  UseBuffer(-1)
  ;DisplaySprite(map_viewport, 0, 0)
  
EndProcedure





; ---------------------------------------------------------------------
;screen: screen coordinate
;para:   parallax value (0-200)
;scroll: scroll position
;RETURN: global coordinate
; ---------------------------------------------------------------------
Procedure map_getcoord(screen.l, para.l, scroll.l)
  If para = 100
    ProcedureReturn screen + scroll
  ElseIf para = 0
    ProcedureReturn screen
  Else
    ProcedureReturn screen + scroll * para / 100
  EndIf
EndProcedure






; ---------------------------------------------------------------------
;coord:  global coordinate
;para:   parallax value (0-200)
;scroll: scroll position
;RETURN: screen coordinate
; ---------------------------------------------------------------------
Procedure map_getscreen(coord.l, para.l, scroll.l)
  If para = 100
    ProcedureReturn coord - scroll
  ElseIf para = 0
    ProcedureReturn coord
  Else
    ProcedureReturn coord - scroll * para / 100
  EndIf
EndProcedure





Procedure.l ReadByteBB()
  ProcedureReturn ReadByte() & $FF
EndProcedure
Procedure.l ReadWordBB()
  ProcedureReturn ReadWord() & $FFFF
EndProcedure

Procedure MemorySize(*p)
  ProcedureReturn GlobalSize_(*p)
EndProcedure



; ---------------------------------------------------------------------
;name:   map file name
;passw:  map password (reserved!)
;RETURN: error code (0=ok, 1=file not found, 2=file size corrupted, 3=no read access, 4=not valid file, 5=checksum problem, 6=password problem, 7=load image problem)
;        error code 7 do not free data or images!!!
; ---------------------------------------------------------------------
Procedure map_load(name.s, passw.s) ;passw.s = ""
  Protected bank.l, bytes.l, checksum.l, code.l, code.l
  Protected count_anims.l, count_base.l, count_data.l, count_geo.l, count_image.l, count_layer.l
  Protected count_meta.l, count_tile.l, count_total.l
  Protected crypted.l, dummy.l, Error.l, file.l, frames.l, fsize.l, geo.geo, hash.s, i.l
  Protected maxsize.l, proversion.l, subversion.l, version.l
  
  UsePNGImageDecoder()
  UseJPEGImageDecoder()
  UseTGAImageDecoder()
  UseTIFFImageDecoder()
  
  map_reset()
  
  If name = "" : ProcedureReturn 1 : EndIf
  
  fsize = FileSize(name)
  If fsize < 64 : ProcedureReturn 2 : EndIf
  
  file = ReadFile(#PB_Any, name)
  If file = 0 : ProcedureReturn 3 : EndIf
  
  ;HEADER-------------------------------------------------------------
  If ReadByteBB() <> 85 Or ReadByteBB() <> 77 Or ReadByteBB() <> 70
    CloseFile(file)
    ProcedureReturn 4
  EndIf
  
  version = ReadByteBB()
  checksum = ReadLong()
  If version & 128 : crypted = 1 : Else : crypted = 0 : EndIf
  If version & 64 : proversion = 1 : Else : proversion = 0 : EndIf
  subversion = version & 63
  
  hash = Space(20)
  ReadData(@hash, 20)
  
  If crypted = 1
    CloseFile(file)
    ProcedureReturn 6
  EndIf
  
  count_total = ReadWordBB()
  count_layer = ReadWordBB()
  count_image = ReadWordBB()
  count_geo = ReadWordBB()
  count_tile = ReadWordBB()
  count_anim = ReadWordBB()
  count_base = ReadWordBB()
  count_data = ReadWordBB()
  count_meta = ReadWordBB()
  
  For i = 1 To 18
    dummy = ReadByteBB()
  Next
  
  
  ;TABLE--------------------------------------------------------------
  maxsize = count_total * 4 + 64
  If maxsize > fsize : Error = 2 : Goto abort : EndIf
  
  Dim map_block(count_total)
  Dim map_offset(count_total)
  
  For i = 1 To count_total
    map_block(i) = ReadLong()
    map_offset(i) = maxsize
    maxsize + map_block(i)
  Next
  
  If (maxsize % 4) > 0 : maxsize + 4 - (maxsize % 4) : EndIf
  If maxsize <> fsize : Error = 2 : Goto abort : EndIf
  
  
  ;BLOCKS-------------------------------------------------------------
  For i = 1 To count_total
    FileSeek(map_offset(i))
    code = ReadByteBB()
    
    Select code
      Case  #layer_back
        map_visible = ReadByteBB()
        map_backgr = extra_readstr(12)
        map_parax = ReadByteBB()
        map_paray = ReadByteBB()
        map_posx = ReadLong()
        map_posy = ReadLong()
        map_sizex = ReadLong()
        map_sizey = ReadLong()
        map_mode = ReadByteBB()
        map_colour = (ReadByteBB() << 16) + (ReadByteBB() << 8) + ReadByteBB()
        ResetList(geo())
      
      Case #layer_map
        If AddElement(layer())
          layer()\code = code
          layer()\visible = ReadByteBB()
          layer()\name = extra_readstr(12)
          layer()\parax = ReadByteBB()
          layer()\paray = ReadByteBB()
          layer()\posx = ReadLong()
          layer()\posy = ReadLong()
          layer()\sizex = ReadLong()
          layer()\sizey = ReadLong()
          layer()\tmp = ReadWordBB()
          layer()\mask = ReadByteBB()
          ResetList(geo())
        EndIf
      
      Case #layer_iso1
        If AddElement(layer())
          layer()\code = code
          layer()\visible = ReadByteBB()
          layer()\name = extra_readstr(12)
          layer()\parax = ReadByteBB()
          layer()\paray = ReadByteBB()
          layer()\posx = ReadLong()
          layer()\posy = ReadLong()
          layer()\sizex = ReadLong()
          layer()\sizey = ReadLong()
          layer()\tmp = ReadWordBB()
          ResetList(geo())
        EndIf
      
      Case #layer_iso2
        If AddElement(layer())
          layer()\code = code
          layer()\visible = ReadByteBB()
          layer()\name = extra_readstr(12)
          layer()\parax = ReadByteBB()
          layer()\paray = ReadByteBB()
          layer()\posx = ReadLong()
          layer()\posy = ReadLong()
          layer()\sizex = ReadLong()
          layer()\sizey = ReadLong()
          layer()\tmp = ReadWordBB()
          layer()\start = ReadByteBB()
          ResetList(geo())
        EndIf
      
      Case #layer_hex1
        If AddElement(layer())
          layer()\code = code
          layer()\visible = ReadByteBB()
          layer()\name = extra_readstr(12)
          layer()\parax = ReadByteBB()
          layer()\paray = ReadByteBB()
          layer()\posx = ReadLong()
          layer()\posy = ReadLong()
          layer()\sizex = ReadLong()
          layer()\sizey = ReadLong()
          layer()\tmp = ReadWordBB()
          layer()\start = ReadByteBB()
          ResetList(geo())
        EndIf
      
      Case #layer_hex2
        If AddElement(layer())
          layer()\code = code
          layer()\visible = ReadByteBB()
          layer()\name = extra_readstr(12)
          layer()\parax = ReadByteBB()
          layer()\paray = ReadByteBB()
          layer()\posx = ReadLong()
          layer()\posy = ReadLong()
          layer()\sizex = ReadLong()
          layer()\sizey = ReadLong()
          layer()\tmp = ReadWordBB()
          layer()\start = ReadByteBB()
          ResetList(geo())
        EndIf
      
      Case #layer_clone
        If AddElement(layer())
          layer()\code = code
          layer()\visible = ReadByteBB()
          layer()\name = extra_readstr(12)
          layer()\parax = ReadByteBB()
          layer()\paray = ReadByteBB()
          layer()\posx = ReadLong()
          layer()\posy = ReadLong()
          layer()\tmp = ReadWordBB()
          ResetList(geo())
        EndIf
      
      Case #layer_image
        If AddElement(layer())
          layer()\code = code
          layer()\visible = ReadByteBB()
          layer()\name = extra_readstr(12)
          layer()\parax = ReadByteBB()
          layer()\paray = ReadByteBB()
          layer()\posx = ReadLong()
          layer()\posy = ReadLong()
          layer()\tmp = ReadWordBB()
          layer()\frame = ReadWordBB()
          layer()\start = ReadWordBB()
          layer()\mode = ReadByteBB()
          layer()\mask = ReadByteBB()
          ResetList(geo())
        EndIf
      
      Case #layer_block
        If AddElement(layer())
          layer()\code = code
          layer()\visible = ReadByteBB()
          layer()\name = extra_readstr(12)
          layer()\parax = ReadByteBB()
          layer()\paray = ReadByteBB()
          layer()\posx = ReadLong()
          layer()\posy = ReadLong()
          layer()\tmp = ReadWordBB()
          layer()\frame = ReadWordBB()
          layer()\start = ReadWordBB()
          layer()\mode = ReadByteBB()
          layer()\mask = ReadByteBB()
          layer()\depth2 = ReadWordBB() ;<TMP
          ResetList(geo())
        EndIf
      
      Case #layer_point
        If AddElement(geo())
          geo()\code = code
          geo()\visible = ReadByteBB()
          geo()\name = extra_readstr(12)
          geo()\parax = ReadByteBB()
          geo()\paray = ReadByteBB()
          geo()\posx = ReadLong()
          geo()\posy = ReadLong()
          ResetList(layer())
        EndIf
      
      Case #layer_line
        If AddElement(geo())
          geo()\code = code
          geo()\visible = ReadByteBB()
          geo()\name = extra_readstr(12)
          geo()\parax = ReadByteBB()
          geo()\paray = ReadByteBB()
          geo()\posx = ReadLong()
          geo()\posy = ReadLong()
          geo()\sizex = ReadLong()
          geo()\sizey = ReadLong()
          ResetList(layer())
        EndIf
      
      Case #layer_rect
        If AddElement(geo())
          geo()\code = code
          geo()\visible = ReadByteBB()
          geo()\name = extra_readstr(12)
          geo()\parax = ReadByteBB()
          geo()\paray = ReadByteBB()
          geo()\posx = ReadLong()
          geo()\posy = ReadLong()
          geo()\sizex = ReadLong()
          geo()\sizey = ReadLong()
          ResetList(layer())
        EndIf
      
      Case #layer_oval
        If AddElement(geo())
          geo()\code = code
          geo()\visible = ReadByteBB()
          geo()\name = extra_readstr(12)
          geo()\parax = ReadByteBB()
          geo()\paray = ReadByteBB()
          geo()\posx = ReadLong()
          geo()\posy = ReadLong()
          geo()\sizex = ReadLong()
          geo()\sizey = ReadLong()
          ResetList(layer())
        EndIf
      
      Case 100
        If AddElement(tile())
          tile()\mask = (ReadByteBB() << 16) + (ReadByteBB() << 8) + ReadByteBB()
          tile()\sizex = ReadWordBB()
          tile()\sizey = ReadWordBB()
          tile()\factor = ReadWordBB()
          tile()\anims = ReadWordBB()
          tile()\count = ReadWordBB()
          tile()\file = extra_readstr(12)
          tile()\bank = AllocateMemory(1)
        EndIf
      
      Case 101
        If @tile() > 8
          frames = ReadWordBB()
          bytes = frames * 4 + 12
          
          bank = AllocateMemory(bytes)
          PokeW(bank, frames)
          ReadData(bank + 2, bytes - 2)
          
          bytes = MemorySize(tile()\bank)
          tile()\bank = ReAllocateMemory(tile()\bank, bytes + 4)
          PokeL(tile()\bank + bytes, bank)
          
        EndIf
      
      Case 102
        If @layer() > 8
          If layer()\bank1 : FreeMemory(layer()\bank1) : EndIf
          layer()\depth1 = ReadByteBB()
          bytes = ReadLong()
          dummy = ReadWordBB()
          If bytes
            layer()\bank1 = AllocateMemory(bytes)
            ReadData(layer()\bank1, bytes)
          Else
            layer()\bank1 = 0
          EndIf
        EndIf
      
      Case 103
        If @layer() > 8
          If layer()\bank2 : FreeMemory(layer()\bank2) : EndIf
          layer()\depth2 = ReadByteBB()
          bytes = ReadLong()
          dummy = ReadWordBB()
          If bytes
            layer()\bank2 = AllocateMemory(bytes)
            ReadData(layer()\bank2, bytes)
          Else
            layer()\bank2 = 0
          EndIf
        EndIf
      
      Case 104
        If @layer() > 8
          If layer()\bank3 : FreeMemory(layer()\bank3) : EndIf
          layer()\ascii = ReadByteBB()
          bytes = ReadLong()
          dummy = ReadWordBB()
          If bytes
            layer()\bank3 = AllocateMemory(bytes)
            ReadData(layer()\bank3, bytes)
          Else
            layer()\bank3 = 0
          EndIf
        ElseIf @geo() > 8
          If geo()\bank3 : FreeMemory(geo()\bank3) : EndIf
          geo()\ascii = ReadByteBB()
          bytes = ReadLong()
          dummy = ReadWordBB()
          If bytes
            geo()\bank3 = AllocateMemory(bytes)
            ReadData(geo()\bank3, bytes)
          Else
            geo()\bank3 = 0
          EndIf
        ElseIf code = #layer_back
          If map_bank3 : FreeMemory(map_bank3) : EndIf
          map_ascii = ReadByteBB()
          bytes = ReadLong()
          dummy = ReadWordBB()
          If bytes
            map_bank3 = AllocateMemory(bytes)
            ReadData(map_bank3, bytes)
          Else
            map_bank3 = 0
          EndIf
        EndIf
      
      Default
        ResetList(geo())
        ResetList(layer())
        ResetList(tile())
      
    EndSelect
  Next
  
  
  ;FINAL--------------------------------------------------------------
  ForEach layer()
    If layer()\code = #layer_map Or layer()\code = #layer_iso1 Or layer()\code = #layer_iso2 Or layer()\code = #layer_hex1 Or layer()\code = #layer_hex2 Or layer()\code = #layer_image Or layer()\code = #layer_block
      layer()\tile = extra_num2tile(layer()\tmp)
      layer()\tmp = 0
    EndIf
    
    If layer()\code = #layer_clone
      layer()\layer = extra_num2layer(layer()\tmp)
      layer()\tmp = 0
    EndIf
    
    If layer()\code = #layer_block
      layer()\code = extra_num2layer(layer()\depth2)
      layer()\depth2 = 0
    EndIf
  Next
  
  map_path = GetPathPart(name)
  map_file = GetFilePart(name)
  
  If tile_load() > 0 : Error = 7 : EndIf
  tile_animreset()
  
  abort:
  Dim map_block(0)
  Dim map_offset(0)
  
  CloseFile(file)
  ProcedureReturn Error
EndProcedure





; ---------------------------------------------------------------------
;RETURN: mouse x in map viewport
; ---------------------------------------------------------------------

Procedure map_mousex()
  ProcedureReturn MouseX() - map_x
EndProcedure






; ---------------------------------------------------------------------
;RETURN: mouse y in map viewport
; ---------------------------------------------------------------------
Procedure map_mousey()
  ProcedureReturn MouseY() - map_y
EndProcedure






; ---------------------------------------------------------------------
Procedure map_reset()
  geo_reset()
  layer_reset()
  tile_reset()
  
  If map_bank3 : FreeMemory(map_bank3) : EndIf
  If map_image : FreeSprite(map_image) : EndIf
  If map_viewport : FreeSprite(map_viewport) : EndIf
  
  map_ascii = 0
  map_backgr = ""
  map_bank3 = 0
  map_colour = 0
  map_file = ""
  map_height = ScreenHeight()
  map_image = 0
  map_mode = 0
  map_parax = 100
  map_paray = 100
  map_path = ""
  map_posx = 0
  map_posy = 0
  map_scrollx = 0
  map_scrolly = 0
  map_sizex = 0
  map_sizey = 0
  map_visible = 0
  map_width = ScreenWidth()
  map_x = 0
  map_y = 0
  map_viewport = CreateSprite(#PB_Any, map_width, map_height)
  
  If map_viewport : ProcedureReturn #True : EndIf
EndProcedure






; ---------------------------------------------------------------------
;x: map position x
;y: map position y
; ---------------------------------------------------------------------
Procedure map_scroll(x.l, y.l)
  map_scrollx = x
  map_scrolly = y
EndProcedure






; ---------------------------------------------------------------------
;x:      map start x
;y:      map start y
;width:  map width
;height: map height
; ---------------------------------------------------------------------
Procedure map_viewport(x.l, y.l, Width.l, Height.l)
  map_x = x
  map_y = y
  map_width = Width
  map_height = Height
  If map_viewport
    map_viewport = CreateSprite(map_viewport, map_width, map_height)
  Else
    map_viewport = CreateSprite(#PB_Any, map_width, map_height)
  EndIf
EndProcedure 
; jaPBe Version=1.4.4.25
; FoldLines=004000480055005D0063006500660068006A006C021402160220022202560259
; FoldLines=02660270
; Build=0
; CompileThis=..\preview_bbplus.pb
; FirstLine=67
; CursorPosition=135
; ExecutableFormat=Windows
; DontSaveDeclare
; EOF