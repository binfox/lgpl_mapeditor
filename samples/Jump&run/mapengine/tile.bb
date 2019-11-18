;---------------------------------------------------------------------ANIMBANK
;offset byte description
;---------------------------------------------------------------------
;00     2    animation frames
;02     2    animation start
;04     1    animation mode (1=paused, 2=forward, 3=backward)
;05     1    default tile value
;06     4    last millisecs time
;10     2    current frame number
;12...  2    animation image
;14...  2    animation time
;---------------------------------------------------------------------

Type tile
  Field anims  ;animation count
  Field banka  ;animations handle bank
  Field bankd  ;default tile value
  Field count  ;frame count
  Field factor ;y-factor
  Field file$  ;filename
  Field image  ;image handle
  Field mask   ;mask color
  Field sizex  ;tile size x
  Field sizey  ;tile sizey
End Type

Const tile_default=20
Const tile_minsize=08





;---------------------------------------------------------------------
Function tile_animate()
  Local bank
  Local frame
  Local frames
  Local i
  Local layer.layer
  Local mode
  Local tile.tile
  Local time1
  Local timediff
  Local timemax

  time2=MilliSecs()

  For tile=Each tile
    For i=1 To tile\anims

      bank    =PeekInt  (tile\banka,i*4-4)
      frames  =PeekShort(bank,0)
      mode    =PeekByte (bank,4)
      time1   =PeekInt  (bank,6)
      frame   =PeekShort(bank,10)
      timediff=time2-time1

      Repeat
        timemax=PeekShort(bank,frame*4+10)

        If mode=1 Or mode<0 Or mode>3 Or timemax=0 Then ;PAUSE
          PokeInt bank,6,time2
          Exit
        EndIf

        If timediff<=timemax Then
          PokeInt   bank,6 ,time2-timediff
          PokeShort bank,10,frame
          Exit
        EndIf

        timediff=timediff-timemax

        If mode=2 Then frame=frame+1 ;FORWARD
        If mode=3 Then frame=frame-1 ;BACKWARD

        If frame<1 Then frame=frames
        If frame>frames Then frame=1
      Forever

    Next
  Next


  For layer=Each layer
    If (layer\code=layer_image Or layer\code=layer_block) And layer\tile<>Null Then
      If layer\frame=>1 And layer\frame<=layer\tile\anims And layer\mode>0 Then

        bank    =PeekInt  (layer\tile\banka,layer\frame*4-4)
        frames  =PeekShort(bank,0)
        mode    =layer\mode
        time1   =layer\time
        frame   =layer\tmp
        timediff=time2-time1

        Repeat
          timemax=PeekShort(bank,frame*4+10)

          If mode=1 Or mode<0 Or mode>3 Or timemax=0 Then ;PAUSE
            layer\time=time2
            Exit
          EndIf

          If timediff<=timemax Then
            layer\time=time2-timediff
            layer\tmp=frame
            Exit
          EndIf

          timediff=timediff-timemax

          If mode=2 Then frame=frame+1 ;FORWARD
          If mode=3 Then frame=frame-1 ;BACKWARD

          If frame<1 Then frame=frames
          If frame>frames Then frame=1
        Forever

      EndIf
    EndIf
  Next
End Function





;---------------------------------------------------------------------
Function tile_animreset()
  Local bank
  Local i
  Local layer.layer
  Local start
  Local tile.tile

  time=MilliSecs()

  For tile=Each tile
    For i=1 To tile\anims
      bank=PeekInt(tile\banka,i*4-4)
      start=PeekShort(bank,2)
      PokeInt bank,6,time
      PokeShort bank,10,start
    Next
  Next

  For layer=Each layer
    If layer\code=layer_image Or layer\code=layer_block Then
      layer\time=time
      layer\tmp=layer\start
      If layer\tmp<0 Then layer\tmp=1
    EndIf
  Next
End Function





;---------------------------------------------------------------------
;file:   find this tileset file name
;RETURN: tileset type handle (null=not found)
;---------------------------------------------------------------------
Function tile_find.tile(file$)
  Local tile.tile

  For tile=Each tile
    If tile\file$=file$ Then Return tile
  Next
End Function





;---------------------------------------------------------------------
;tile:   tileset handle
;anim:   anim number (1...)
;RETURN: anim value (0-255)
;---------------------------------------------------------------------
Function tile_getanimval(tile.tile,anim)
  Local bank

  If tile=Null Then Return
  If tile\banka<>0 And anim=>1 And anim<=tile\anims Then
    bank=PeekInt(tile\banka,anim*4-4) ;animbank
    Return PeekByte(bank,5) ;default value
  EndIf
End Function





;---------------------------------------------------------------------
;tile:   tileset handle
;frame:  frame number (1...)
;RETURN: frame value (0-255)
;---------------------------------------------------------------------
Function tile_getframeval(tile.tile,frame)
  If tile=Null Then Return
  If tile\bankd<>0 And frame=>1 And frame<=tile\count Then
    Return PeekByte(tile\bankd,frame-1)
  EndIf
End Function





;---------------------------------------------------------------------
;RETURN: number of images with loading errors
;---------------------------------------------------------------------
Function tile_load()
  Local b
  Local error
  Local g
  Local r
  Local tile.tile

  For tile=Each tile
    If tile\image<>0 Then
      FreeImage tile\image
      tile\image=0
    EndIf

    If tile\file$<>"" Then
      tile\image=LoadAnimImage(map_path+tile\file$,tile\sizex,tile\sizey,0,tile\count,2) ;<<<remove 2 for BB2D/BB3D
      If tile\image=0 Then error=error+1
    EndIf

    If tile\image<>0 Then
      r=(tile\mask And $FF0000)/$10000
      g=(tile\mask And $FF00)/$100
      b=(tile\mask And $FF)
      MaskImage tile\image,r,g,b
    EndIf
  Next

  If map_image<>0 Then
    FreeImage map_image
    map_image=0
  EndIf

  If map_backgr$<>"" Then
    map_image=LoadImage(map_path$+map_backgr$,2) ;<<<remove 2 for BB2D/BB3D
    If map_image=0 Then error=error+1
  EndIf

  Return error
End Function





;---------------------------------------------------------------------
Function tile_reset()
  Local tile.tile

  For tile=Each tile
    If tile\banka<>0 Then FreeBank  tile\banka
    If tile\bankd<>0 Then FreeImage tile\bankd
    If tile\image<>0 Then FreeImage tile\image
    Delete tile
  Next
End Function





;---------------------------------------------------------------------
;tile: tileset handle
;anim: anim number (1...)
;frame: current anim frame (1...)
;---------------------------------------------------------------------
Function tile_setanimframe(tile.tile,anim,frame)
  Local bank
  Local frames

  If tile=Null Then Return
  If tile\banka<>0 And anim=>1 And anim<=tile\anims Then
    bank=PeekInt(tile\banka,anim*4-4) ;anim bank
    frames=PeekShort(bank,0)          ;anim frames

    If frame<1 Then frame=1
    If frame>frames Then frame=frames
    PokeInt bank,6,MilliSecs()        ;anim time
    PokeShort bank,10,frame           ;anim frame
  EndIf
End Function





;---------------------------------------------------------------------
;tile: tileset handle
;anim: anim number (1...)
;mode: anim mode (1=paused, 2=forward, 3=backward)
;---------------------------------------------------------------------
Function tile_setanimmode(tile.tile,anim,mode)
  Local bank

  If tile=Null Then Return
  If tile\banka<>0 And anim=>1 And anim<=tile\anims Then
    bank=PeekInt(tile\banka,anim*4-4) ;anim bank
    PokeByte bank,4,mode              ;anim mode
  EndIf
End Function





;---------------------------------------------------------------------
;tile:  tileset handle
;anim:  anim number (1...)
;value: default tile value (0-255)
;---------------------------------------------------------------------
Function tile_setanimval(tile.tile,anim,value)
  Local bank

  If tile=Null Then Return
  If tile\banka<>0 And anim=>1 And anim<=tile\anims Then
    bank=PeekInt(tile\banka,anim*4-4) ;animbank
    PokeByte bank,5,value             ;default value
  EndIf
End Function





;---------------------------------------------------------------------
;tile:  tileset handle
;frame: frame number (1...)
;value: default tile value (0-255)
;---------------------------------------------------------------------
Function tile_setframeval(tile.tile,frame,value)
  If tile=Null Then Return
  If tile\bankd<>0 And frame=>1 And frame<=tile\count Then
    PokeByte tile\bankd,frame-1,value
  EndIf
End Function