;---------------------------------------------------------------------
;UNIVERSAL MAP EDITOR
;COPYRIGHT 2004 (www.mapeditor.de.vu)
;---------------------------------------------------------------------
;ENGLISH: Licence free for games only
;GERMAN:  Lizenzfreie Verwendung nur für Spiele
;---------------------------------------------------------------------



;---------------------------------------------------------------------
;CHANGES v1.4
;---------------------------------------------------------------------
;new include: var.bb
;new function: layer_map_draw_repeat()
;map_load(), layer_getdata(), layer_setdata(), layer_delete(),
;layer_reset(), geo_delete(), geo_reset(), map_reset() modified
;
;---------------------------------------------------------------------
;CHANGES v1.3
;---------------------------------------------------------------------
;new functions: tile_setanimframe() + tile_setanimmode()
;
;---------------------------------------------------------------------
;CHANGES v1.2
;---------------------------------------------------------------------
;tile_animate() modified! PLEASE UPDATE IN YOUR GAME!!!
;map_load() modified to load default tile values
;tile_reset() modified to free new bank handle (tile\bankd)
;new functions: geo_find(), layer_find(), tile_find(),
;               tile_getanimval(),  tile_setanimval(),
;               tile_getframeval(), tile_setframeval()
;---------------------------------------------------------------------



Include "mapengine\crc32.bb"
Include "mapengine\crypt.bb"
Include "mapengine\extra.bb"
Include "mapengine\geo.bb"
Include "mapengine\layer.bb"
Include "mapengine\layer_block.bb"
Include "mapengine\layer_clone.bb"
Include "mapengine\layer_hex1.bb"
Include "mapengine\layer_hex2.bb"
Include "mapengine\layer_image.bb"
Include "mapengine\layer_iso1.bb"
Include "mapengine\layer_iso2.bb"
Include "mapengine\layer_map.bb"
Include "mapengine\map.bb"
Include "mapengine\md5.bb"
Include "mapengine\sha1.bb"
Include "mapengine\tile.bb"
Include "mapengine\var.bb"