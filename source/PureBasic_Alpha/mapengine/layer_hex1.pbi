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
Procedure layer_hex1_coord(*layer.layer, x.l, y.l, check.l) ; check = 0
  Protected corr.l, cx.f, cy.f, dist.f, factor.l, i.l, min.f, outside.l, px.l, py.l, sizex.l, sizey.l
  Protected stepx.l, stepy.l, stretch.f, Width.l, xx.l, yy.l
  Dim arrayx.l(6)
  Dim arrayy.l(6)
  
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
  
  If sizex = Int((sizey / 2) / SinD(60)) * 2 And (sizey % 4) = 0
    stepx = sizex - sizey / 4
    stepy = sizey / 2
  ElseIf sizex = sizey
    stepx = sizex - sizey / 4
    stepy = sizey / 2
  Else
    stepx = sizex - (sizey / 2) / 2
    stepy = sizey / 2
  EndIf
  
  Width = Int((sizey / 2) / SinD(60)) * 2
  stretch = Width / sizex
  min = 1000000
  
  xx = (x - px) / stepx
  If ((xx + *layer\start) & 1) = 0
    yy = (y - py) / sizey
    corr = 0
  Else
    yy = (y - py - stepy) / sizey
    corr = 1
  EndIf
  
  arrayx(0) = xx - 1 : arrayy(0) = yy + corr - 1
  arrayx(1) = xx - 1 : arrayy(1) = yy + corr
  arrayx(2) = xx     : arrayy(2) = yy - 1
  arrayx(3) = xx     : arrayy(3) = yy
  arrayx(4) = xx     : arrayy(4) = yy + 1
  arrayx(5) = xx + 1 : arrayy(5) = yy + corr - 1
  arrayx(6) = xx + 1 : arrayy(6) = yy + corr
  
  For i = 0 To 6
    corr = (arrayx(i) + *layer\start) & 1
    cx = (x - px - arrayx(i) * stepx) - (sizex - 1) / 2
    cy = (y - py - arrayy(i) * sizey - corr * stepy) - (sizey - 1) / 2
    dist = cx * cx * stretch * stretch + cy * cy
    If mid > dist
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
Procedure layer_hex1_draw(*layer.layer)
  Protected anims.l, bank.l, begin.l, count.l, endx.l, endy.l, factor.l, frame.l, i.l, image.l, mode.l
  Protected offset.l, px.l, py.l, sizex.l, sizey.l, start.l, startx.l, starty.l, stepx.l, stepy.l, value.l
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
  
  If sizex = Int((sizey / 2) / SinD(60)) * 2 And (sizey % 4) = 0
    stepx = sizex - sizey / 4
    stepy = sizey / 2
  ElseIf sizex = sizey
    stepx = sizex - sizey / 4
    stepy = sizey / 2
  Else
    stepx = sizex - (sizey - 2) / 2
    stepy = sizey / 2
  EndIf
  
  If startx < 0 : startx = 0 : EndIf
  If starty < 0 : starty = 0 : EndIf
  If endx > *layer\sizex - 1 : endx = *layer\sizex - 1 : EndIf
  If endy > *layer\sizey - 1 : endy = *layer\sizey - 1 : EndIf
  
  For y = starty To endy
    For i = 0 To 1
      begin = startx + (startx + *layer\start + 1) & 1
      For x = begin To endx Step 2
        
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
          frame = PeekW(bank + (start * 4) + 8)
        Else
          frame = value - anims
        EndIf
        
        If frame >= 1 And frame < count
          xx = px + x * stepx
          yy = py + y * sizey + ((x * *layer\start) & 1) * stepy
          ClipSprite(image, (frame - 1) * sizex, 0, sizex, sizey)
          DisplayTransparentSprite(image, xx, yy)
        EndIf
      
      Next
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
Procedure layer_hex1_pos(*layer.layer, x.l, y.l)
  Protected factor.l, px.l, py.l, sizex.l, sizey.l, stepx.l, stepy.l
  
  If *layer = 0 : ProcedureReturn #False : EndIf
  
  If *layer\tile
    factor = *layer\tile\actor
    sizex = *layer\tile\sizex
    sizey = *layer\tile_sizey - factor
  Else
    sizex = tile_default
    sizey = tile_default
  EndIf
  
  If sizex < tile_minsize : sizex = tile_minsize : EndIf
  If sizey < tile_minsize : sizey = tile_minsize : EndIf
  
  px = map_getscreen(*layer\posx, *layer\parax, map_scrollx)
  py = map_getscreen(*layer\posy, *layer\paray, map_scrolly)
  
  If sizex = Int((sizey / 2) / SinD(60)) * 2 And (sizey % 4) = 0
    stepx = sizex - sizey / 4
    stepy = sizey / 2
  ElseIf sizex = sizey
    stepx = sizex - sizey / 4
    stepy = sizey / 2
  Else
    stepx = sizex - (sizey - 2) / 2
    stepy = sizey / 2
  EndIf
  
  layer_x = px + x * stepx
  layer_y = py + y * sizey + ((x + *layer\start) & 1) * stepy
  ProcedureReturn 1
EndProcedure






; ---------------------------------------------------------------------
;layer: layer handle
;
;RETURN LAYER_WIDTH:  layer width in pixel
;RETURN LAYER_HEIGHT: layer height in pixel
; ---------------------------------------------------------------------
Procedure layer_hex1_size(*layer.layer)
  Protected factor.l, sizex.l, sizey.l, stepx.l, stepy.l
  
  If *layer = 0 : ProcedureReturn #False : EndIf
  
  If *layer\tile
    factor = *layer\tile\actor
    sizex = *layer\tile\sizex
    sizey = *layer\tile_sizey - factor
  Else
    sizex = tile_default
    sizey = tile_default
  EndIf
  
  If sizex < tile_minsize : sizex = tile_minsize : EndIf
  If sizey < tile_minsize : sizey = tile_minsize : EndIf
  
  px = map_getscreen(*layer\posx, *layer\parax, map_scrollx)
  py = map_getscreen(*layer\posy, *layer\paray, map_scrolly)
  
  If sizex = Int((sizey / 2) / SinD(60)) * 2 And (sizey % 4) = 0
    stepx = sizex - sizey / 4
    stepy = sizey / 2
  ElseIf sizex = sizey
    stepx = sizex - sizey / 4
    stepy = sizey / 2
  Else
    stepx = sizex - (sizey - 2) / 2
    stepy = sizey / 2
  EndIf
  
  layer_width = *layer\sizex * stepx - stepx + sizex
  layer_height = *layer\sizey * sizey + factor
  If *layer\sizex > 1 : layer_height + stepy : EndIf
  
EndProcedure 
; jaPBe Version=1.4.4.25
; FoldLines=000A0055
; Build=0
; CompileThis=..\preview_bbplus.pb
; FirstLine=147
; CursorPosition=269
; ExecutableFormat=Windows
; DontSaveDeclare
; EOF