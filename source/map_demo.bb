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
End Function





;---------------------------------------------------------------------
;RETURN: file size in byte
;---------------------------------------------------------------------
Function map_calc()
  Local bank
  Local count_anim
  Local count_base
  Local count_data
  Local count_geo
  Local count_image
  Local count_layer
  Local count_meta
  Local count_tile
  Local count_total
  Local frames
  Local i
  Local layer.layer
  Local maxsize
  Local size
  Local tile.tile


  For tile=Each tile
    count_tile=count_tile+1
    count_anim=count_anim+tile\anims
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
    EndIf
  Next
  count_total=1 + count_layer + count_image + count_geo + count_tile*2 + count_anim + count_base + count_data + count_meta


  For tile=Each tile
    maxsize=maxsize+26 ;block size (tileset)

    maxsize=maxsize+4+tile\count ;default value structure

    For i=1 To tile\anims
      bank=PeekInt(tile\banka,i*4-4)
      frames=PeekShort(bank,0)
      maxsize=maxsize+13+frames*4 ;block size (animation)
    Next
  Next


  For i=0 To layer_count
    layer=layer_list[i]

    If layer\code=layer_back  Then maxsize=maxsize+36 ;block size (back)
    If layer\code=layer_map   Then maxsize=maxsize+35 ;block size (map)
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
  Next

  maxsize=maxsize+64+count_total*4
  If (maxsize Mod 4)>0 Then maxsize=maxsize+4-(maxsize Mod 4)

  If map_file$<>"" Then 
    SetGadgetText window,language$(0)+" 1.3  ["+map_file$+"  "+Str$(maxsize)+" byte]"
  Else
    SetGadgetText window,language$(0)+" 1.3  ["+Str$(maxsize)+" byte]"
  EndIf
End Function





;---------------------------------------------------------------------
Function map_check()
End Function





;---------------------------------------------------------------------
;message: error message
;---------------------------------------------------------------------
Function map_error(message$)
End Function





;---------------------------------------------------------------------
Function map_load()
  Notify "Not available in demo-version"+Chr$(13)+Chr$(13)+"In Demo-Version nicht verfügbar"
End Function





;---------------------------------------------------------------------
;name: full path name
;---------------------------------------------------------------------
Function map_load2(name$)
  Notify "Not available in demo-version"+Chr$(13)+Chr$(13)+"In Demo-Version nicht verfügbar"
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
End Function





;---------------------------------------------------------------------
Function map_save()
  Notify "Not available in demo-version"+Chr$(13)+Chr$(13)+"In Demo-Version nicht verfügbar"
End Function





;---------------------------------------------------------------------
;name: full path name
;---------------------------------------------------------------------
Function map_save2(name$)
  Notify "Not available in demo-version"+Chr$(13)+Chr$(13)+"In Demo-Version nicht verfügbar"
End Function