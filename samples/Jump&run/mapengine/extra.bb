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