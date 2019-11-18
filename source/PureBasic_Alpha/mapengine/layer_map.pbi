; ---------------------------------------------------------------------
;layer:       layer handle
;spritex:     sprite x (screen)
;spritey:     sprite y (screen)
;sprite:      sprite handle
;spriteframe: sprite frame
;RETURN:      0=no collision
;             1=collision
; ---------------------------------------------------------------------
Procedure layer_map_collision(*layer.layer, spritex.l, spritey.l, sprite.l, spritesizex.l, spritesizey.l, spriteframe.l) ; spriteframe = 0
  Protected anims.l, bank.l, count.l, endx.l, endy.l, factor.l, frame.l, image.l, mode.l, offset.l
  Protected px.l, py.l, sizex.l, sizey.l, start.l, startx.l, starty.l, value.l, x.l, xx.l, y.l, yy.l
  
  If *layer = 0 : ProcedureReturn #False : EndIf
  If *layer\tile = 0 : ProcedureReturn #False : EndIf
  
  anims = *layer\tile\anims
  count = *layer\tile\count
  factor = *layer\tile\factor
  image = *layer\tile\image
  sizex = *layer\tile\sizex
  sizey = *layer\tile\sizey - factor
  
  If image = 0 : ProcedureReturn #False : EndIf
  If sprite = 0 : ProcedureReturn #False : EndIf
  If sizex < tile_minsize : sizex = tile_minsize : EndIf
  If sizey < tile_minsize : sizey = tile_minsize : EndIf
  
  px = map_getscreen(*layer\posx, *layer\parax, map_scrollx)
  py = map_getscreen(*layer\posy, *layer\paray, map_scrolly)
  
  layer_map_coord(*layer, spritex + SpriteWidth(sprite) - 1, spritey + SpriteHeight(sprite) - 1, 0)
  endx = layer_x
  endy = layer_y
  
  If startx < 0 : startx = 0 : EndIf
  If starty < 0 : starty = 0 : EndIf
  If endx > *layer\sizex - 1 : endx = *layer\sizex - 1 : EndIf
  If endy > *layer\sizey - 1 : endy = *layer\sizey - 1 : EndIf
  
  For y = starty To endy
    For x = startx To endx
      
      If *layer\depth1 = 4
        offset = ((y * *layer\sizex + x) / 2)
        mode = ((y * *layer\sizex + x) & 1) * 4
        value = ((PeekB(*layer\bank1 + offset) & $FF) >> mode) & 15
      ElseIf  *layer\depth1 = 8
        offset = y * *layer\sizex + x
        value = PeekB(*layer\bank1 + offset) & $FF
      ElseIf *layer\depth1 = 12
        offset = ((y * *layer\sizex + x) * 3) / 2
        mode = ((y * *layer\sizex + x) & 1) * 4
        value = ((PeekW(*layer\bank1 + offset) & $FFFF) >> mode) & 4095
      ElseIf *layer\depth1 = 16
        offset = (y * *layer\sizex + x) * 2
        value = PeekW(*layer\bank1 + offset) & $FFFF
      EndIf
      
      If value >= 1 And value <= anims
        bank = PeekL(*layer\tile\bank + (value * 4) - 4)
        start = PeekW(bank + 10) & $FFFF
        frame = PeekW(bank + (start * 4) + 8) & $FFFF
      Else
        frame = value - anims
      EndIf
     
      If frame >= 1 And frame <= count
        xx = px + x * sizex
        yy = py + y * sizey
        ;-?
        ClipSprite(image, (frame - 1) * sizex, 0, sizex, sizey)
        ClipSprite(sprite, (spriteframe - 1) * spritesizex, 0, spritesizex, spritesizey)
        
        If SpritePixelCollision(image, xx, yy, sprite, spritex, spritey) : ProcedureReturn 1 : EndIf
      EndIf
      
    Next
  Next
  
  
EndProcedure






; ---------------------------------------------------------------------
;layer:  layer handle
;x:      screen x
;y:      screen y
;check:  bound check
;
;RETURN:         0=outside, 1=inside (only if check is enabled)
;RETURN LAYER_X: tile pos x
;RETURN LAYER_Y: tile pos y
; ---------------------------------------------------------------------
Procedure layer_map_coord(*layer.layer, x.l, y.l, check.l) ; check = 0
  Protected factor.l, outside.l, px.l, py.l, sizex.l, sizey.l
  
  If *layer = 0 : ProcedureReturn #False : EndIf
  
  If *layer\tile
    factor = *layer\tile\factor
    sizex = *layer\tile\sizex
    sizey = *layer\tile\sizey - factor
  Else
    sizex = tile_default
    sizey = tile_default
  EndIf
  
  If sizex < tile_minsize : sizex = tile_minsize : EndIf
  If sizey < tile_minsize : sizey = tile_minsize : EndIf
  
  px = map_getscreen(*layer\posx, *layer\parax, map_scrollx)
  py = map_getscreen(*layer\posy, *layer\paray, map_scrolly)
  
  layer_x = (x - px) / sizex
  layer_y = (y - py) / sizey
  
  If (x - px) < 0 : layer_x - 1 : EndIf
  If (y - py) < 0 : layer_y - 1 : EndIf
  
  If check = 1
    If layer_x < 0 : layer_x = 0 : outside = 1 : EndIf
    If layer_y < 0 : layer_y = 0 : outside = 1 : EndIf
    If layer_x > *layer\sizex - 1 : layer_x = *layer\sizex - 1 : outside = 1 : EndIf
    If layer_y > *layer\sizey - 1 : layer_y = *layer\sizey - 1 : outside = 1 : EndIf
  EndIf
  
  ProcedureReturn 1 - outside
EndProcedure





; ---------------------------------------------------------------------
;layer: layer handle
; ---------------------------------------------------------------------
Procedure layer_map_draw(*layer.layer)
  Protected anims.l, bank.l, count.l, endx.l, endy.l, factor.l, frame.l, image.l, mode.l, offset.l
  Protected px.l, py.l, sizex.l, sizey.l, start.l, startx.l, starty.l, value.l, x.l, xx.l, y.l, yy.l
  
  If *layer = 0 : ProcedureReturn #False : EndIf
  If *layer\tile = 0 : ProcedureReturn #False : EndIf
  
  anims = *layer\tile\anims
  count = *layer\tile\count
  factor = *layer\tile\factor
  image = *layer\tile\image
  sizex = *layer\tile\sizex
  sizey = *layer\tile\sizey - factor
  
  If image = 0 : ProcedureReturn #False : EndIf
  If sizex < tile_minsize : sizex = tile_minsize : EndIf
  If sizey < tile_minsize : sizey = tile_minsize : EndIf
  
  px = map_getscreen(*layer\posx, *layer\parax, map_scrollx)
  py = map_getscreen(*layer\posy, *layer\paray, map_scrolly)
  
  startx = (0 - px) / sizex
  starty = (0 - py - factor) / sizey
  endx = (map_width - px) / sizex
  endy = (map_height - py) / sizey
  
  If startx < 0 : startx = 0 : EndIf
  If starty < 0 : starty = 0 : EndIf
  If endx > *layer\sizex - 1 : endx = *layer\sizex - 1 : EndIf
  If endy > *layer\sizey - 1 : endy = *layer\sizey - 1 : EndIf
  
  For y = starty To endy
    For x = startx To endx
      
      If *layer\depth1 = 4
        offset = ((y * *layer\sizex + x) / 2)
        mode = ((y * *layer\sizex + x) & 1) * 4
        value = ((PeekB(*layer\bank1 + offset) & $FF) >> mode) & 15
      ElseIf  *layer\depth1 = 8
        offset = y * *layer\sizex + x
        value = PeekB(*layer\bank1 + offset) & $FF
      ElseIf *layer\depth1 = 12
        offset = ((y * *layer\sizex + x) * 3) / 2
        mode = ((y * *layer\sizex + x) & 1) * 4
        value = ((PeekW(*layer\bank1 + offset) & $FFFF) >> mode) & 4095
      ElseIf *layer\depth1 = 16
        offset = (y * *layer\sizex + x) * 2
        value = PeekW(*layer\bank1 + offset) & $FFFF
      EndIf
      
      If value >= 1 And value <= anims
        bank = PeekL(*layer\tile\bank + (value * 4) - 4)
        start = PeekW(bank + 10) & $FFFF
        frame = PeekW(bank + (start * 4) + 8)
      Else
        frame = value - anims
      EndIf
      
      If frame >= 1 And frame <= count
        xx = px + x * sizex
        yy = py + y * sizey
        ClipSprite(image, (frame - 1) * sizex, 0, sizex, *layer\tile\sizey)
        If *layer\mask = 0 : DisplaySprite(image, xx, yy) : EndIf
        If *layer\mask = 1 : DisplayTransparentSprite(image, xx, yy) : EndIf
      EndIf
      
    Next
  Next
EndProcedure







; ---------------------------------------------------------------------
;layer:  layer handle
;x:      tile pos x
;y:      tile pos y
;
;RETURN:         0=error, 1=ok
;RETURN LAYER_X: screen pos x
;RETURN LAYER_Y: screen pos y
; ---------------------------------------------------------------------
Procedure layer_map_pos(*layer.layer, x.l, y.l)
  Protected factor.l, px.l, py.l, sizex.l, sizey.l
  
  If *layer = 0 : ProcedureReturn #False : EndIf
  
  If *layer\tile
    factor = *layer\tile\factor
    sizex = *layer\tile\sizex
    sizey = *layer\tile\sizey - factor
  Else
    sizex = tile_default
    sizey = tile_default
  EndIf
  
  If sizex < tile_minsize : sizex = tile_minsize : EndIf
  If sizey < tile_minsize : sizey = tile_minsize : EndIf
  
  px = map_getscreen(*layer\posx, *layer\parax, map_scrollx)
  py = map_getscreen(*layer\posy, *layer\paray, map_scrolly)
  
  layer_x = px + x * sizex
  layer_y = py + y * sizey
  
  ProcedureReturn 1
EndProcedure






; ---------------------------------------------------------------------
;layer: layer handle
;
;RETURN LAYER_WIDTH:  layer width in pixel
;RETURN LAYER_HEIGHT: layer height in pixel
; ---------------------------------------------------------------------
Procedure layer_map_size(*layer.layer)
  Protected factor.l, sizex.l, sizey.l
  
  If *layer = 0 : ProcedureReturn #False : EndIf
  
  If *layer\tile
    factor = *layer\tile\factor
    sizex = *layer\tile\sizex
    sizey = *layer\tile\sizey - factor
  Else
    sizex = tile_default
    seizy = tile_default
  EndIf
  
  If sizex < tile_minsize : sizex = tile_minsize : EndIf
  If sizey < tile_minsize : sizey = tile_minsize : EndIf
  
  layer_width = *layer\sizex * sizex
  layer_height = *layer\sizey * sizey + factor
EndProcedure 
; jaPBe Version=1.4.4.25
; FoldLines=00620084008D00D100E200FA0107011A
; Build=0
; CompileThis=..\preview_bbplus.pb
; FirstLine=33
; CursorPosition=74
; ExecutableFormat=Windows
; DontSaveDeclare
; EOF