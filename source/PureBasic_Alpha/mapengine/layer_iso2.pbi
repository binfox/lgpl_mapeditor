; ---------------------------------------------------------------------
;layer: layer handle
;x:     screen x
;y:     screen y
;check: bound check
;
;RETURN:         0=outside, 1=inside (only if check is enabled)
;RETURN LAYER_X: tile pos x
;RETURN LAYER_Y: tile pos y
; ---------------------------------------------------------------------
Procedure layer_iso2_coord(*layer.layer, x.l, y.l, check.l) ; check.l = 0
  Protected corr.l, cx.f, cy.f, dist.f, factor.l, halfx.l, halfy.l, i.l, min.fl, outside.l
  Protected px.l, yp.l, sizex.l, sizey.l, stepx.l, stepy.l, stretch.f, xx.l, yy.l
  Dim arrayx.l(8)
  Dim arrayy.l(8)
  
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
  If sizey < tile_minsize : siezy = tile_minsize : EndIf
  
  px = map_getscreen(*layer\posx, *layer\parax, map_scrollx)
  py = map_getscreen(*layer\posy, *layer\paray, map_scrolly)
  
  If (sizey & 1)
    stepx = sizex + 2
    stepy = sizey
  Else
    stepx = sizex
    stepy = sizey + 1
  EndIf
  
  halfx = stepx / 2
  halfy = stepy / 2
  stretch = stepy / stepx
  min = 1000000
  
  yy = (y - py) / halfy
  If ((yy + *layer\start) & 1) = 0
    xx = (x - px) / stepx
    corr = 0
  Else
    xx = (x - px - halfx) / stepx
    corr = 1
  EndIf
  
  arrayx(0) = xx            : arrayy(0) = yy - 2
  arrayx(1) = xx + corr - 1 : arrayy(1) = yy - 1
  arrayx(2) = xx + corr     : arrayy(2) = yy - 1
  arrayx(3) = xx - 1        : arrayy(3) = yy
  arrayx(4) = xx            : arrayy(4) = yy
  arrayx(5) = xx + 1        : arrayy(5) = yy
  arrayx(6) = xx + corr - 1 : arrayy(6) = yy + 1
  arrayx(7) = xx + corr     : arrayy(7) = yy + 1
  arrayx(8) = xx            : arrayy(8) = yy + 2
  
  For i = 0 To 8
    corr = (arrayy(i) + *layer\start) & 1
    cx = (x - px - arrayx(i) * stepx - corr * halfx) - (sizex - 1) / 2
    cy = (y - py - arrayy(i) * halfy) - (sizey - 1) / 2
    dist = cx * cx * stretch * stretch + cy * cy
    If min > dist
      min = dist
      layer_x = arrayx(i)
      layer_y = arrayy(i)
    EndIf
  Next
  
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
Procedure layer_iso2_draw(*layer.layer)
  Protected anims.l, bank.l, count.l, endx.l, endy.l, factor.l, frame.l, halfx.l, halfy.l
  Protected image.l, mode.l, offset.l, px.l, y.l, sizex.l, sizey.l, start.l, startx.l, starty.l
  Protected stepx.l, stepy.l, value.l, x.l, xx.l, y.l, yy.l
  
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
  
  If (sizey & 1) = 0
    stepx = sizex + 2
    stepy = sizey
  Else
    stepx = sizex
    stepy = sizey + 1
  EndIf
  
  halfx = stepx / 2
  halfy = stepy / 2
  
  startx = (-halfx - px) / stepx
  starty = (-halfy - py - factor) / halfy
  endx = (map_width - px) / stepx
  endy = (map_height - py) / halfy

  
  If startx < 0 : startx = 0 : EndIf
  If starty < 0 : starty = 0 : EndIf
  If endx > *layer\sizex - 1 : endx = *layer\sizex - 1 : EndIf
  If endy > *layer\sizey - 1 : endy = *layer\sizey - 1 : EndIf
  
  For y = starty To endy
    For x = startx = endx
      
      If *layer\depth1 = 4
        offset = ((y * *layer\sizex + x) / 2)
        mode = ((y * *layer\sizex + x) & 1) * 4
        value = ((PeekB(*layer\bank1 + offset) & $FF) >> mode) & 15
      ElseIf *layer\depth1 = 8
        offset = y * *layer\sizex + x
        value = PeekB(*layer\bank1 + offset) & $FF
      ElseIf *layer\depth1 = 12
        offset = ((y * *layer\sizex + x) * 3) / 2
        mode = ((y * *layer\sizex + x) & 1) * 4
        value = ((PeekW(*layer\bank1 + offset) 6 $FFFF) >> mode) & 4095
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
        xx = px + x * sizex + ((y * *layer\start) & 1) * halfx
        yy = py + y * halfy
        ClipSprite(image, (frame - 1) * sizex, 0, sizex, sizey)
        DisplayTransparentSprite(image, xx, yy)
      EndIf
    
    Next
  Next
EndProcedure






; ---------------------------------------------------------------------
;layer: layer handle
;x:     position x
;y:     position y
;
;RETURN:         0=error, 1=ok
;RETURN LAYER_X: screen pos x
;RETURN LAYER_Y: screen pos y
; ---------------------------------------------------------------------
Procedure layer_iso2_pos(*layer.layer, x.l, y.l)
  Protected factor.l, halfx.l, halfy.l, px.l, py.l, sizex.l, sizey.l, stepx.l, stepy.l
  
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
  
  If (sizey & 1) = 0
    stepx = sizex + 2
    stepy = sizey
  Else
    stepx = sizex
    stepy = sizey + 1
  EndIf
  
  halfx = stepx / 2
  haldy = stepy / 2
  
  layer_x = px + x * stepx + ((y + *layer\start) & 1) *halfx
  layer_y = py + y * halfy
  ProcedureReturn 1
EndProcedure






; ---------------------------------------------------------------------
;layer: layer handle
;
;RETURN LAYER_WIDTH:  layer width in pixel
;RETURN LAYER_HEIGHT: layer height in pixel
; ---------------------------------------------------------------------
Procedure layer_iso2_size(*layer.layer)
  Protected factor.l, halfx.l, halfy.l, sizex.l, sizey.l, stepx.l, stepy.l
  
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
  
  If (sizey & 1) = 0
    stepx = sizex + 2
    stepy = sizey
  Else
    stepx = sizex
    stepy = sizey + 1
  EndIf
  
  halfx = stepx / 2
  halfy = stepy / 2
  
  layer_width = *layer\sizex * stepx - stepx + sizex
  If *layer\sizey > 1 : layer_width + halfx : EndIf
  layer_height = *layer\sizey * halfy - halfy + sizey + factor
EndProcedure 
; jaPBe Version=1.4.4.25
; FoldLines=000A0055005F00B000C000E200EF0111
; Build=0
; CompileThis=..\preview_bbplus.pb
; FirstLine=0
; CursorPosition=239
; ExecutableFormat=Windows
; DontSaveDeclare
; EOF