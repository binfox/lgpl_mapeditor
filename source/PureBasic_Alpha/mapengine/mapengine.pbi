; ---------------------------------------------------------------------
;UNIVERSAL MAP EDITOR
;COPYRIGHT 2004 (www.mapeditor.de.vu)
; ---------------------------------------------------------------------
;ENGLISH: Licence free for games only
;GERMAN:  Lizenzfreie Verwendung nur f�r Spiele
; ---------------------------------------------------------------------

;{- MAP INIT
Dim map_block(0)
Dim map_offset(0)

Global map_ascii.l
Global map_backgr.s
Global map_bank3.l
Global map_colour.l
Global map_file.s
Global map_height.l
Global map_image.l
Global map_mode.l
Global map_parax.l
Global map_paray.l
Global map_path.s
Global map_posx.l
Global map_posy.l
Global map_scrollx.l
Global map_scrolly.l
Global map_sizex.l
Global map_sizey.l
Global map_tmp.s
Global map_visible.l
Global map_width.l
Global map_x.l
Global map_y.l
Global map_viewport.l

Procedure.s GetSystemTempPath()
  a.s = Space(1024)
  GetTempPath_(1024, a)
  ProcedureReturn a
EndProcedure


map_tmp.s = GetSystemTempPath() + "data.tmp"
;}

;{- TILE INIT
Structure tile
  anims.l
  bank.l
  count.l
  factor.l
  file.s
  image.l
  mask.l
  sizex.l
  sizey.l
EndStructure

#tile_default = 20
#tile_minsize = 08


NewList tile.tile()
;}

;{- LAYER INIT
Structure layer
  ascii.l
  bank1.l
  bank2.l
  bank3.l
  code.l
  depth1.l
  depth2.l
  frame.l
  *layer.layer
  mask.l
  mode.l
  name.s
  parax.l
  paray.l
  posx.l
  posy.l
  sizex.l
  sizey.l
  start.l
  *tile.tile
  time.l
  tmp.l
  visible.l
EndStructure

#layer_back  = 00
#layer_map   = 01
#layer_iso1  = 02
#layer_iso2  = 03
#layer_hex1  = 04
#layer_hex2  = 05
#layer_clone = 06
#layer_image = 07
#layer_block = 08
#layer_point = 09
#layer_line  = 10
#layer_rect  = 11
#layer_oval  = 12

Global layer_x.l, layer_y.l, layer_width.l, layer_height.l

NewList layer.layer()
;}

;{- Declares
Declare layer_map_pos(*layer.layer, x.l, y.l)
Declare layer_clone_pos(*layer.layer, x.l, y.l)
Declare map_getcoord(screen.l, para.l, scroll.l)
Declare layer_map_coord(*layer.layer, x.l, y.l, check.l) ; check = 0
Declare layer_clone_update(*layer.layer)
Declare layer_map_draw(*layer.layer)
Declare layer_map_size(*layer.layer)
Declare map_getscreen(coord.l, para.l, scroll.l)
Declare layer_map_collision(*layer.layer, spritex.l, spritey.l, Sprite.l, spritesizex.l, spritesizey.l, spriteframe.l)
Declare map_reset()
;}

;{- DirectX INIT
If InitSprite() = 0 Or InitKeyboard() = 0 Or InitMouse() = 0
  MessageRequester("DirectX INIT", "DX konnte nicht initialisiert werden.")
  End
EndIf
;}

;{- Zusatz
Procedure.f CosD(w.f)
  ProcedureReturn Cos(w * 0.01745329)
EndProcedure

Procedure.f SinD(w.f)
  ProcedureReturn Sin(w * 0.01745329)
EndProcedure
;}

Procedure RuntimeError(Text.s)
  MessageRequester("Runtime Error", Text, #MB_ICONERROR)
  End
EndProcedure


XIncludeFile "mapengine\tile.pbi"         ; ok
XIncludeFile "mapengine\layer.pbi"        ; ok
XIncludeFile "mapengine\extra.pbi"        ; ok
XIncludeFile "mapengine\geo.pbi"          ; ok
XIncludeFile "mapengine\layer_block.pbi"  ; ok
XIncludeFile "mapengine\layer_clone.pbi"  ; ok
XIncludeFile "mapengine\layer_image.pbi"  ; ok
XIncludeFile "mapengine\layer_map.pbi"    ; ok
XIncludeFile "mapengine\map.pbi"

;Ab PRO-Version kommen diese Includes hinzu

XIncludeFile "mapengine\crc32.bb"         ; ok
XIncludeFile "mapengine\crypt.bb"         ; ok
XIncludeFile "mapengine\layer_hex1.bb"    ; ok
XIncludeFile "mapengine\layer_hex2.bb"    ; ok
XIncludeFile "mapengine\layer_iso1.bb"
XIncludeFile "mapengine\layer_iso2.bb"    ; ok
XIncludeFile "mapengine\md5.bb"
XIncludeFile "mapengine\sha1.bb"          ; ok 
; jaPBe Version=1.4.4.25
; FoldLines=0008002C00240000002E0040002F00000042006E004300000070007B007D0082
; FoldLines=0084008C0085000000890000008E0091
; Build=0
; CompileThis=..\preview_bbplus.pb
; FirstLine=0
; CursorPosition=162
; ExecutableFormat=Windows
; DontSaveDeclare
; EOF