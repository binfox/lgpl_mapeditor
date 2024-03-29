; ---------------------------------------------------------------------ANIMBANK
;offset byte description
; ---------------------------------------------------------------------
;00     2    animation frames
;02     2    animation start
;04     2    animation mode (1=paused, 2=forward, 3=backward)
;06     4    last millisecs time
;10     2    current frame number
;12...  2    animation image
;14...  2    animation time
; ---------------------------------------------------------------------


; ---------------------------------------------------------------------
Procedure tile_animate()
  Protected bank.l, frame.l, frames.l, i.l, *layer, mode.l, *tile, time1.l, timediff.l, timemax.l, time2.l
  
  time2 = ElapsedMilliseconds()
  
  *tile = @tile()
  
  ForEach tile()
    For i = 1 To tile()\anims
      
      bank = PeekL(tile()\bank + (i * 4) - 4)
      frames = PeekW(bank + 0) & $FFFF
      mode = PeekW(bank + 4) & $FFFF
      time1 = PeekL(bank + 6)
      frame = PeekW(bank + 10) & $FFFF
      timediff = time2 - time1
      
      Repeat
        timemax = PeekW(bank + (frame * 4) + 10) & $FFFF
        
        If mode = 1 Or mode < 0 Or mode > 3 Or timemax = 0
          PokeL(bank + 6, time2)
          Break
        EndIf
        
        If timediff <= timemax
          PokeL(bank + 6, time2 - timediff)
          PokeW(bank + 10, frame)
          Break
        EndIf
        
        timediff - timemax
        
        If mode = 2 : frame + 1 : EndIf
        If mode = 3 : frame - 1 : EndIf
        
        If frame < 1 : frame = frames : EndIf
        If frame > frames : frame = 1 : EndIf
      ForEver
    Next
  Next
  
  If *tile > 8 : ChangeCurrentElement(tile(), *tile) : EndIf
  
  *layer = @layer()
  
  ForEach layer()
    If (layer()\code = #layer_image Or layer()\code = #layer_block) And layer()\tile
      If layer()\frame >= 1 And layer()\frame <= layer()\tile\anims And layer()\mode > 0
        
        bank = PeekL(layer()\tile\bank + (layer()\frame * 4) - 4)
        frames = PeekW(bank + 0) & $FFFF
        mode = layer()\mode
        time1 = layer()\time
        frame = layer()\tmp
        timediff = time2 - time1
        
        Repeat
          timemax = PeekW(bank + (frame * 4) + 10) & $FFFF
          
          If mode = 1 Or mode < 0 Or mode > 3 Or timemax = 0
            layer()\time = time2
            Break
          EndIf
          
          If timediff <= timemax
            layer()\time = time2 - timediff
            layer()\tmp = frame
            Break
          EndIf
          
          timediff - timemax
          
          If mode = 2 : frame + 1 : EndIf
          If mode = 3 : frame - 1 : EndIf
          
          If frame < 1 : frame = frames : EndIf
          If frame > frames : frame = 1 : EndIf
        ForEver
        
      EndIf
    EndIf
  Next
  
  If *layer > 8 : ChangeCurrentElement(layer(), *layer) : EndIf
  
EndProcedure






; ---------------------------------------------------------------------
Procedure tile_animreset()
  Protected bank.l, i.l, *layer, start, *tile
  
  time = ElapsedMilliseconds()
  
  *tile = @tile()
  
  ForEach tile()
    For i = 1 To tile()\anims
      bank = PeekL(tile()\bank + (i * 4) - 4)
      start = PeekW(bank + 2) & $FFFF
      PokeL(bank + 6, time)
      PokeW(bank + 10, start)
    Next
  Next
  
  If *tile > 8 : ChangeCurrentElement(tile(), *tile) : EndIf
  
  *layer = @layer()
  
  ForEach layer()
    If layer()\code = #layer_image Or layer()\code = #layer_block
      layer()\time = time
      layer()\tmp = layer()\start
      If layer()\tmp < 0 : layer()\tmp = 1 : EndIf
    EndIf
  Next
  
  If *layer > 8 : ChangeCurrentElement(layer(), *layer) : EndIf
  
EndProcedure






; ---------------------------------------------------------------------
;RETURN: number of images with loading errors
; ---------------------------------------------------------------------
Procedure tile_load()
  Protected b.l, Error.l, g.l, r.l, *tile, image.l, x.l, y.l, a.l, Width.l, Height.l
  
  *tile = @tile()
  
  ForEach tile()
    If tile()\image
      FreeSprite(tile()\image)
      tile()\image = 0
    EndIf
    
    If tile()\file <> ""
      image = LoadSprite(#PB_Any, map_path + tile()\file)
      If image = 0
        Error + 1
      Else
        tile()\image = CreateSprite(#PB_Any, tile()\sizex * tile()\count, tile()\sizey)
        If tile()\image
          UseBuffer(tile()\image)
          x = 0 : y = 0 : a = 0
          Width = SpriteWidth(image)
          Height = SpriteHeight(image)
          Repeat
            ClipSprite(image, x, y, tile()\sizex, tile()\sizey)
            DisplaySprite(image, a * tile()\sizex, 0)
            x + tile()\sizex
            If x >= Width : y + tile()\sizey : x = 0 : EndIf
            a + 1
          Until y > Height
          UseBuffer(-1)
          FreeSprite(image)
        Else
          Error + 1
        EndIf
      EndIf
    EndIf
    
    If tile()\image
      r = (tile()\mask >> 16) & $FF
      g = (tile()\mask >> 8) & $FF
      b = (tile()\mask) & $FF
      TransparentSpriteColor(tile()\image, r, g, b)
    EndIf
  Next
  
  If *tile > 8 : ChangeCurrentElement(tile(), *tile) : EndIf
  
  If map_image
    FreeSprite(map_image)
    map_image = 0
  EndIf
  
  If map_backgr <> ""
    map_image = LoadSprite(#PB_Any, map_path + map_backgr)
    If map_image = 0 : Error + 1 : EndIf
  EndIf
  
  ProcedureReturn Error
  
EndProcedure






; ---------------------------------------------------------------------
Procedure tile_reset()
  
  ForEach tile()
    If tile()\bank : FreeMemory(tile()\bank) : EndIf
    If tile()\image : FreeSprite(tile()\image) : EndIf
    DeleteElement(tile())
  Next
EndProcedure 
; jaPBe Version=1.4.4.25
; FoldLines=000E0064006C008A00D700DE
; Build=0
; CompileThis=..\preview_bbplus.pb
; FirstLine=34
; CursorPosition=178
; ExecutableFormat=Windows
; DontSaveDeclare
; EOF