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
Procedure layer_hex2_coord(*layer.layer, x.l, y.l check.l) ; check.l = 0
  Protected corr.l, cx.f, cy.f, dist.f, factor.l, Height.l, i.l, min.f, outside.l, px.l, py.l
  Protected sizex.l, sizey.l, stepx.l, stepy.l, stretch.f, xx.l, yy.l
  Dim arrayx(8)
  Dim arrayy(8)
  
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
  
  stepx = sizex / 2
  stepy = sizey - sizex / 4
  Height = Int((sizex / 2) / SinD(60)) * 2
  stretch = Height / sizey
  min = 1000000
  
  yy = (y - py) / stepy
  If ((yy + *layer\start) & 1) = 0
    xx = (x - px) / sizex
    corr = 0
  Else
    xx = (x - px - stepx) / sizex
    corr = 1
  EndIf
  
  arrayx(0) = xx + corr - 1 : arrayy(0) = yy - 1
  arrayx(1) = xx + corr     : arrayy(1) = yy - 1
  arrayx(2) = xx - 1        : arrayy(2) = yy
  arrayx(3) = xx            : arrayy(3) = yy
  arrayx(4) = xx + 1        : arrayy(4) = yy
  arrayx(5) = xx + corr - 1 : arrayy(5) = yy + 1
  arrayx(6) = xx + corr     : arrayy(6) = yy + 1
  
  If i = 0 To 6
    corr = (arrayy(i) + *layer\start) & 1
    cx = (x - px - arrayx(i) * sizex - corr * stepx) - (sizex - 1) / 2
    cy = (y - py - arrayy(i) * stepy) - (sizey - 1) / 2
    dist = cx * cx + cy * cy * stretch * stretch
    If min > dist
      min = dist
      layer_x = arrayx(i)
      layer_y = arrayy(i)
    EndIf
  Next
  
  If check = 1
    If layer_x < 0 : layer_x = 0 : outside = 1 : EndIf
    If layer_y < 0 : layer_y = 0 : outside = 1 : EndIf
    If layer_x > *layer\sizex - 1 : layer_x = *layer_sizex - 1 : outside = 1 : EndIf
    If layer_y > *layer\sizey - 1 : layer_y = *layer_sizey - 1 : outside = 1 : EndIf
  EndIf
  
  ProcedureReturn 1 - outside
EndProcedure







; ---------------------------------------------------------------------
;layer: layer handle
; ---------------------------------------------------------------------
Procedure layer_hex2_draw(*layer.layer)
  Protected anims.l, bank.l, count.l, endx.l, endy.l, factor.l, frame.l, image.l, mode.l, offset.l
  Protected px.l, py.l, sizex.l, sizey.l, start.l, startx.l, starty.l, stepx.l, stepy.l, value.l
  Protected x.l, xx.l, y.l, yy.l
  
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
  
  stepx = sizex / 2
  stepy = sizey - sizex / 4
  
  startx = (-stepx - px) / siezx
  starty = (-sizey - py + stepy - factor) / stepy
  endx = (map_width - px) / sizex
  endy = (map_height - py) / stepy
  
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
        xx = px + x * sizex + ((y * *layer\start) & 1) * stepx
        yy = py + y * stepy
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
Procedure layer_hex2_pos(*layer.layer, x.l, y.l)
  Protected factor.l, px.l, py.l, sizex.l, sizey.l, stepx.l, stepy.l
  
  If *layer = 0 : ProcedureReturn #False : EndIf
  
  If *layer\tile
    factor = *layer\tile\factor
    sizex = *layer\tile\sizex
    sizey = *layer\tile\sizey
  Else
    sizex = tile_default
    sizey = tile_default
  EndIf
  
  If sizex < tile_minsize : sizex = tile_minsize : EndIf
  If sizey < tile_minsize : sizey = tile_minsize : EndIf
  
  px = map_getscreen(*layer\posx, *layer\parax, map_scrollx)
  py = map_getscreen(*layer\posy, *layer\paray, map_scrolly)
  
  stepx = sizex / 2
  stepy = sizey - sizex / 4
  
  layer_x = px + x * sizex + ((y + *layer\start) & 1) * stepx
  layer_y = py + y * stepy
  ProcedureReturn 1
EndProcedure






; ---------------------------------------------------------------------
;layer: layer handle
;
;RETURN LAYER_WIDTH:  layer width in pixel
;RETURN LAYER_HEIGHT: layer height in pixel
; ---------------------------------------------------------------------
Procedure layer_hex2_size(*layer.layer)
  Protected factor.l, sizex.l, sizey.l, stepx.l, stepy.l
  
  If *layer = 0 : ProcedureReturn #False : EndIf
  
  If *layer\tile
    factor = *layer\tile\factor
    sizex = *layer\tile\sizex
    sizey = *layer\tile\sizey
  Else
    sizex = tile_default
    sizey = tile_default
  EndIf
  
  If sizex < tile_minsize : sizex = tile_minsize : EndIf
  If sizey < tile_minsize : sizey = tile_minsize : EndIf
  
  stepx = sizex / 2
  stepy = sizey - sizex / 4
  
  layer_width = *layer\sizex * sizex
  If *layer\sizey > 1 : layer_width + stepx : EndIf
  layer_height = *layer\sizey * sizey - stepy + sizey + factor
EndProcedure 
; jaPBe Version=1.4.4.25
; FoldLines=000A004C00AE00C800D500EC
; Build=0
; CompileThis=..\preview_bbplus.pb
; FirstLine=25
; CursorPosition=101
; ExecutableFormat=Windows
; DontSaveDeclare
; EOF