;---------------------------------------------------------------------ANIMBANK
;offset byte description
;---------------------------------------------------------------------
;00     2    animation frames
;02     2    animation start
;04     1    animation mode (1=paused, 2=forward, 3=backward)
;05     1    default tile value
;06     4    (reserved for millisecs)
;10     2    (reserved for current frame number)
;12...  2    animation image
;14...  2    animation time
;---------------------------------------------------------------------

Type tile
  Field anims
  Field banka ;anim
  Field bankd ;default
  Field bankx
  Field banky
  Field count
  Field countx
  Field county
  Field factor
  Field file$
  Field image
  Field mask
  Field needdef
  Field sizex
  Field sizey
End Type

Type tile_mask
  Field rgb
  Field count
End Type

Const tile_maxanims   =65535
Const tile_maxframes  =500
Const tile_maxtiles   =65535
Const tile_maxtilesets=500   ;10 / 500
Const tile_maxsize    =500
Const tile_minsize    =8
Const tile_default    =20

Global tile.tile
Global tile_input_anim
Global tile_input_default
Global tile_input_defkill
Global tile_input_factor
Global tile_input_image
Global tile_input_mask
Global tile_input_set
Global tile_input_sizex
Global tile_input_sizey
Global tile_input_start
Global tile_input_time
Global tile_input_view
Global tile_lastclick
Global tile_toolbar
Global tile_toolbar_height
Global tile_toolbar_width
Global tile_window
Global tile_window_height
Global tile_window_width





;---------------------------------------------------------------------
Function tile_add()
  Local count
  Local i
  Local name1$
  Local name2$
  Local tmptile.tile

  For tmptile=Each tile
    count=count+1
  Next
  If count=>tile_maxtilesets Then Return

  ChangeDir map_path$
  name1$=RequestFile(language$(84),"bmp;*.jpg;*.png",0)
  ChangeDir map_app$

  If name1$="" Then Return

  name2$=name1$
  For i=Len(name1$) To 1 Step -1
    If Mid$(name1$,i,1)="\" Or Mid$(name1$,i,1)="/" Then
      name2$=Mid$(name1$,i+1)
      Exit
    EndIf
  Next

  If Len(name2$)>12 Then
    Notify language$(85)
    Return
  EndIf

  For tmptile=Each tile
    If Lower$(tmptile\file$)=Lower$(name2$) Then
      Notify language$(224)
      Return
    EndIf
  Next

  If FileType(map_path$+name2$)<>1 Then
    Notify language$(86)
    Return
  EndIf

  tile       =New tile
  tile\anims =0
  tile\banka =CreateBank(0)
  tile\bankd =CreateBank(65536)
  tile\bankx =CreateBank(0)
  tile\banky =CreateBank(0)
  tile\count =0
  tile\factor=0
  tile\file$ =name2$
  tile\image =0
  tile\mask  =0
  tile\sizex =0
  tile\sizey =0

  For tmptile=Each tile
    If Lower$(tmptile\file$)>Lower$(tile\file$) Then
      Insert tile Before tmptile
      Exit
    EndIf
  Next

  tile_load(tile,32,32,0,0)
  tile_mask(tile)
End Function





;---------------------------------------------------------------------
;tile: tileset handle
;---------------------------------------------------------------------
Function tile_addanim(tile.tile)
  Local bank

  If tile=Null Then Return
  If tile\anims=>tile_maxanims Then Return

  tile_recalc(tile,0)

  bank=CreateBank(16)
  PokeShort bank,00,1   ;frames
  PokeShort bank,02,1   ;start
  PokeByte  bank,04,2   ;mode
  PokeByte  bank,05,0   ;default value
  PokeShort bank,12,0   ;image
  PokeShort bank,14,500 ;time
 
  tile\anims=tile\anims+1
  tile_editor_anim=tile\anims
  tile_editor_frame=1

  ResizeBank tile\banka,tile\anims*4
  PokeInt tile\banka,tile\anims*4-4,bank ;animbank
End Function





;---------------------------------------------------------------------
;tile: tileset handle
;anim: animation number
;---------------------------------------------------------------------
Function tile_addframe(tile.tile,anim)
  Local bank
  Local frames

  If tile=Null Then Return
  If anim<1 Then Return
  If anim>tile\anims Then Return

  bank=PeekInt(tile\banka,anim*4-4) ;animbank
  frames=PeekShort(bank,0)          ;frames
  If frames=>tile_maxframes Then Return
  tile_editor_frame=frames+1

  ResizeBank bank,frames*4+16
  PokeShort  bank,0,frames+1      ;frames
  PokeShort  bank,frames*4+12,0   ;image
  PokeShort  bank,frames*4+14,500 ;time
End Function





;---------------------------------------------------------------------
;tile: tileset handle
;anim: animation number
;---------------------------------------------------------------------
Function tile_animate(tile.tile,anim)
  Local animcanvas
  Local animwindow
  Local bank
  Local frame
  Local frames
  Local mode
  Local nr
  Local time1
  Local time2
  Local timediff
  Local timemax

  If tile=Null Then Return
  If tile\image=0 Then Return
  If anim<1 Then Return
  If anim>tile\anims Then Return

  animwindow=CreateWindow(language$(78),(window_maxx-tile\sizex+60)/2,(window_maxy-tile\sizey+60)/2,tile\sizex+60,tile\sizey+60,tile_window,33)
  animcanvas=CreateCanvas(10,10,tile\sizex+40,tile\sizey+40,animwindow)
  DisableGadget tile_window

  bank  =PeekInt(tile\banka,anim*4-4) ;animbank
  frames=PeekShort(bank,0)            ;frames
  frame =PeekShort(bank,2)            ;start
  mode  =PeekByte (bank,4)            ;mode
  time1 =MilliSecs()

  Repeat
    SetBuffer CanvasBuffer(animcanvas)
    ClsColor 255,255,255
    Cls

    nr=PeekShort(bank,frame*4+8) ;image
    If nr=>1 And nr<=tile\count Then DrawImage tile\image,20,20,nr-1
    FlipCanvas animcanvas

    time2=MilliSecs()
    timediff=time2-time1

    Repeat
      timemax=PeekShort(bank,frame*4+10) ;time
      If mode=1 Then Exit
      If timemax=0 Then Exit
      If timediff<=timemax Then Exit

      timediff=timediff-timemax
      If mode=2 Then frame=frame+1
      If mode=3 Then frame=frame-1

      If frame<1 Then frame=frames
      If frame>frames Then frame=1
      time1=time2-timediff
    Forever

    WaitEvent(1)
    Select EventID()
      Case $0110 ;ESC-------------------------------------------------
        Exit
      Case $0803 ;windowclose-------------------------------------------
        Exit
    End Select
  Forever

  EnableGadget tile_window
  FreeGadget animcanvas
  FreeGadget animwindow
  ActivateWindow tile_window
End Function





;---------------------------------------------------------------------
;tile: tileset handle
;---------------------------------------------------------------------
Function tile_del(tile.tile)
  Local bank
  Local i

  If tile=Null Then Return

  For i=1 To tile\anims
    bank=PeekInt(tile\banka,i*4-4) ;animbank
    FreeBank bank
  Next

  FreeBank tile\banka
  FreeBank tile\bankd
  FreeBank tile\bankx
  FreeBank tile\banky
  If tile\image<>0 Then FreeImage tile\image

  Delete tile
End Function





;---------------------------------------------------------------------
;tile: tileset handle
;anim: animation number
;---------------------------------------------------------------------
Function tile_delanim(tile.tile,anim)
  Local bank
  Local i

  If tile=Null Then Return
  If anim<1 Then Return
  If anim>tile\anims Then Return

  tile_recalc(tile,anim)

  bank=PeekInt(tile\banka,anim*4-4) ;animbank
  FreeBank bank

  For i=anim To tile\anims-1
    bank=PeekInt(tile\banka,i*4)  ;animbank
    PokeInt tile\banka,i*4-4,bank ;animbank
  Next

  tile\anims=tile\anims-1
  ResizeBank tile\banka,tile\anims*4

  tile_editor_anim=0
  tile_editor_frame=0
End Function





;---------------------------------------------------------------------
;tile:  tileset handle
;anim:  animation number
;frame: animation frame (-1=last)
;---------------------------------------------------------------------
Function tile_delframe(tile.tile,anim,frame=-1)
  Local bank
  Local frames
  Local i
  Local layer.layer
  Local start
  Local value

  If tile=Null Then Return
  If anim<1 Then Return
  If anim>tile\anims Then Return

  bank=PeekInt(tile\banka,anim*4-4) ;animbank
  frames=PeekShort(bank,0)          ;frames
  If frames<=1 Then Return

  If frame=-1 Then frame=frames
  If frame<1 Then Return
  If frame>frames Then Return

  For i=frame To frames-1
    value=PeekInt(bank,i*4+12) ;image+time
    PokeInt bank,i*4+8,value   ;image+time
  Next

  ResizeBank bank,frames*4+8
  PokeShort bank,0,frames-1 ;frames

  start=PeekShort(bank,2) ;start
  If start>frames-1 Then PokeShort bank,2,frames-1 ;start
  If tile_editor_frame>frames-1 Then tile_editor_frame=frames-1

  For layer=Each layer
    If layer\tile=tile Then
      If layer\code=layer_image Or layer\code=layer_block Then
        If layer\frame=anim And layer\mode>0 And layer\start>frames-1 Then
          layer\start=frames-1
        EndIf
      EndIf
    EndIf
  Next
End Function





;---------------------------------------------------------------------
;load: 0=F5 refresh
;      1=load file
;---------------------------------------------------------------------
Function tile_fullupdate(load=0)
  Local b
  Local count
  Local countx
  Local county
  Local g
  Local height
  Local i
  Local image
  Local newcount
  Local newfactor
  Local newx
  Local newy
  Local r
  Local sizex
  Local sizey
  Local tile.tile
  Local width

  For tile=Each tile
    image    =0
    sizex    =0
    sizey    =0
    newcount =tile\count
    newfactor=tile\factor
    newx     =tile\sizex
    newy     =tile\sizey

    If tile\image<>0 Then FreeImage tile\image
    ResizeBank tile\bankx,0
    ResizeBank tile\banky,0
    tile\count =0
    tile\countx=0
    tile\county=0
    tile\image =0
    tile\sizex =0
    tile\sizey =0

    If tile\file$="" Then Goto skip
    image=LoadImage(map_path$+tile\file$,vram)
    If image=0 Then Goto skip

    width=ImageWidth(image)
    height=ImageHeight(image)
    FreeImage image

    If width<tile_minsize Or height<tile_minsize Then Goto skip

    For i=tile_minsize To width
      If i>tile_maxsize Then Exit
      If (width Mod i)=0 Then
        countx=countx+1
        If countx=1 Then sizex=i
        If newx=i Then sizex=i
        If sizex<>newx And sizex<tile_default And i=>tile_default Then sizex=i
        ResizeBank tile\bankx,countx*2
        PokeShort tile\bankx,countx*2-2,i
      EndIf
    Next

    For i=tile_minsize To height
      If i>tile_maxsize Then Exit
      If (height Mod i)=0 Then
        county=county+1
        If county=1 Then sizey=i
        If newy=i Then sizey=i
        If sizey<>newy And sizey<tile_default And i=>tile_default Then sizey=i
        ResizeBank tile\banky,county*2
        PokeShort tile\banky,county*2-2,i
      EndIf
    Next

    If countx=0 Or county=0 Then Goto skip

    count=(width/sizex) * (height/sizey)
    If count>tile_maxtiles Then count=tile_maxtiles
    image=LoadAnimImage(map_path$+tile\file$,sizex,sizey,0,count,vram)

    tile\count =count
    tile\countx=width/sizex
    tile\county=height/sizey
    tile\image =image
    tile\sizex =sizex
    tile\sizey =sizey

    r=(tile\mask And $FF0000)/$10000
    g=(tile\mask And $FF00)/$100
    b=(tile\mask And $FF)
    MaskImage tile\image,r,g,b

    If tile\factor>tile\sizey-tile_minsize Then tile\factor=tile\sizey-tile_minsize
    If tile\factor<0 Then tile\factor=0

    .skip

    If load=1 And image=0 Then
      map_error("("+language$(75)+" '"+tile\file$+"') "+language$(225))
    ElseIf load=1 And image<>0 Then
      If newx<>tile\sizex Or newy<>tile\sizey Then map_error("("+language$(75)+" '"+tile\file$+"') "+language$(226))
      If newcount<>tile\count Then map_error("("+language$(75)+" '"+tile\file$+"') "+language$(227))
      If newfactor<>tile\factor Then map_error("("+language$(75)+" '"+tile\file$+"') "+language$(228))
    EndIf
  Next

  If layer_file$<>"" Then
    If layer_handle<>0 Then FreeImage layer_handle
    layer_handle=LoadImage(map_path$+layer_file$,vram)
  EndIf

  If load=0 Then
    list_update()
    menu_update()
    editor_update()
  EndIf
End Function





;---------------------------------------------------------------------
;tile: tileset handle
;---------------------------------------------------------------------
Function tile_mask(tile.tile)
  Local b
  Local buffer
  Local g
  Local i
  Local match1
  Local match2
  Local match3
  Local match4
  Local max=1
  Local r
  Local rgb1
  Local rgb2
  Local rgb3
  Local rgb4
  Local tile_mask.tile_mask

  If tile=Null Then Return
  If tile\image=0 Then Return
  If tile\count=0 Then Return

  For i=0 To tile\count-1
    buffer=ImageBuffer(tile\image,i)

    rgb1=ReadPixel(0,0,buffer)
    rgb2=ReadPixel(0,tile\sizey-1,buffer)
    rgb3=ReadPixel(tile\sizex-1,0,buffer)
    rgb4=ReadPixel(tile\sizex-1,tile\sizey-1,buffer)

    match1=0
    match2=0
    match3=0
    match4=0

    For tile_mask=Each tile_mask
      If tile_mask\rgb=rgb1 Then
        tile_mask\count=tile_mask\count+1
        match1=1
      EndIf

      If tile_mask\rgb=rgb2 Then
        tile_mask\count=tile_mask\count+1
        match2=1
      EndIf

      If tile_mask\rgb=rgb3 Then
        tile_mask\count=tile_mask\count+1
        match3=1
      EndIf

      If tile_mask\rgb=rgb4 Then
        tile_mask\count=tile_mask\count+1
        match4=1
      EndIf
    Next

    If match1=0 Then
      tile_mask=New tile_mask
      tile_mask\count=1
      tile_mask\rgb=rgb1
    EndIf

    If match2=0 Then
      tile_mask=New tile_mask
      tile_mask\count=1
      tile_mask\rgb=rgb2
    EndIf

    If match3=0 Then
      tile_mask=New tile_mask
      tile_mask\count=1
      tile_mask\rgb=rgb3
    EndIf

    If match4=0 Then
      tile_mask=New tile_mask
      tile_mask\count=1
      tile_mask\rgb=rgb4
    EndIf
  Next

  For tile_mask=Each tile_mask
    If max<tile_mask\count Then
      max=tile_mask\count
      tile\mask=tile_mask\rgb
    EndIf
  Next

  r=(tile\mask And $FF0000)/$10000
  g=(tile\mask And $FF00)/$100
  b=(tile\mask And $FF)
  MaskImage tile\image,r,g,b

  Delete Each tile_mask
End Function





;---------------------------------------------------------------------
Function tile_needdef()
  Local i
  Local tile.tile

  For tile=Each tile
    tile\needdef=0

    If tile\count>0 Then
      For i=0 To tile\count-1
        If PeekByte(tile\bankd,i)>0 Then
          tile\needdef=1
          Goto skip
        EndIf
      Next
    EndIf

    .skip
  Next
End Function





;---------------------------------------------------------------------
;tile:  tileset handle
;newx:  new image sizex
;newy:  new image sizey
;mode:  0=load image every time
;       1=load image only if size is changed
;clear: 0=do not clear old data
;       1=clear old data
;---------------------------------------------------------------------
Function tile_load(tile.tile,newx,newy,mode,clear)
  Local b
  Local count
  Local countx
  Local county
  Local g
  Local height
  Local i
  Local image
  Local r
  Local sizex
  Local sizey
  Local width

  If tile=Null Then Return

  tile_editor_anim=0
  tile_editor_frame=0
  tile_menu_selected=0

  If mode=1 And tile\image<>0 Then
    If tile\sizex=newx And tile\sizey=newy Then Return
  EndIf

  If tile\image<>0 Then FreeImage tile\image
  If clear=1 Then
    ResizeBank tile\bankx,0
    ResizeBank tile\banky,0
    tile\count =0
    tile\countx=0
    tile\county=0
    tile\image =0
    tile\sizex =0
    tile\sizey =0
  EndIf

  If tile\file$="" Then Return
  image=LoadImage(map_path$+tile\file$,vram)
  If image=0 Then
    Notify language$(86)
    Return
  EndIf

  width=ImageWidth(image)
  height=ImageHeight(image)
  FreeImage image

  If width<tile_minsize Or height<tile_minsize Then
    Notify tile\file$+" "+language$(229)+" "+Str$(tile_minsize)+"x"+Str$(tile_minsize)+" "+language$(231)
    Return
  EndIf

  For i=tile_minsize To width
    If i>tile_maxsize Then Exit
    If (width Mod i)=0 Then
      countx=countx+1
      If countx=1 Then sizex=i
      If newx=i Then sizex=i
      If sizex<>newx And sizex<tile_default And i=>tile_default Then sizex=i
      ResizeBank tile\bankx,countx*2
      PokeShort tile\bankx,countx*2-2,i
    EndIf
  Next

  For i=tile_minsize To height
    If i>tile_maxsize Then Exit
    If (height Mod i)=0 Then
      county=county+1
      If county=1 Then sizey=i
      If newy=i Then sizey=i
      If sizey<>newy And sizey<tile_default And i=>tile_default Then sizey=i
      ResizeBank tile\banky,county*2
      PokeShort tile\banky,county*2-2,i
    EndIf
  Next

  If countx=0 Or county=0 Then
    Notify tile\file$+" "+language$(230)+" "+Str$(tile_maxsize)+"x"+Str$(tile_maxsize)+" "+language$(231)
    Return
  EndIf

  count=(width/sizex) * (height/sizey)
  If count>tile_maxtiles Then count=tile_maxtiles
  image=LoadAnimImage(map_path$+tile\file$,sizex,sizey,0,count,vram)

  tile\count =count
  tile\countx=width/sizex
  tile\county=height/sizey
  tile\image =image
  tile\sizex =sizex
  tile\sizey =sizey

  r=(tile\mask And $FF0000)/$10000
  g=(tile\mask And $FF00)/$100
  b=(tile\mask And $FF)
  MaskImage tile\image,r,g,b

  If tile\factor>tile\sizey-tile_minsize Then tile\factor=tile\sizey-tile_minsize
  If tile\factor<0 Then tile\factor=0

  SetSliderValue tile_editor_sliderx,0
  SetSliderValue tile_editor_slidery,0
  SetSliderValue tile_menu_sliderx,0
  SetSliderValue tile_menu_slidery,0
End Function





;---------------------------------------------------------------------
;tmptile=preselected tileset
;---------------------------------------------------------------------
Function tile_prog(tmptile.tile)
  Local b
  Local bank
  Local count
  Local frames
  Local g
  Local i
  Local max
  Local pos
  Local r
  Local selected
  Local sizex
  Local sizey
  Local start
  Local time

  tile_window        =CreateWindow   (language$(24),(window_maxx-675)/2,(window_maxy-480)/2,675,480,window,35)
  tile_window_width  =ClientWidth    (tile_window)
  tile_window_height =ClientHeight   (tile_window)
  tile_toolbar       =CreateToolBar  ("image/toolbar2.bmp",0,0,tile_window_width,16,tile_window)
  tile_toolbar_width =GadgetWidth    (tile_toolbar)
  tile_toolbar_height=GadgetHeight   (tile_toolbar)
  tile_input_image   =CreateComboBox (070,tile_toolbar_height+00,150,22,tile_window)
  tile_input_sizex   =CreateComboBox (070,tile_toolbar_height+25,150,22,tile_window)
  tile_input_sizey   =CreateComboBox (070,tile_toolbar_height+50,150,22,tile_window)
  tile_input_factor  =CreateTextField(295,tile_toolbar_height+00,150,22,tile_window)
  tile_input_mask    =CreatePanel    (295,tile_toolbar_height+25,150,22,tile_window,1)
  tile_input_default =CreateTextField(295,tile_toolbar_height+50,150-24,22,tile_window)
  tile_input_defkill =CreateButton   ("<",295+150-22,tile_toolbar_height+50,22,22,tile_window)

  tile_input_anim    =CreateComboBox (tile_window_width-150,tile_toolbar_height+00,150,22,tile_window)
  tile_input_start   =CreateTextField(tile_window_width-150,tile_toolbar_height+25,150,22,tile_window)
  tile_input_time    =CreateTextField(tile_window_width-150,tile_toolbar_height+50,126,22,tile_window)
  tile_input_set     =CreateButton   ("<",tile_window_width-22,tile_toolbar_height+50,22,22,tile_window)
  tile_input_view    =CreateComboBox (70,tile_window_height-32,150,22,tile_window)

  Local button       =CreateButton   (language$(070),(tile_window_width-85)/2,tile_window_height-32,85,22,tile_window)
  Local label1       =CreateLabel    (language$(216)+":",005,tile_toolbar_height+03,60,16,tile_window)
  Local label2       =CreateLabel    (language$(232)+":",005,tile_toolbar_height+28,60,16,tile_window)
  Local label3       =CreateLabel    (language$(233)+":",005,tile_toolbar_height+53,60,16,tile_window)
  Local label4       =CreateLabel    (language$(234)+":",230,tile_toolbar_height+03,60,16,tile_window)
  Local label5       =CreateLabel    (language$(235)+":",230,tile_toolbar_height+28,60,16,tile_window)
  Local label6       =CreateLabel    (language$(266)+":",230,tile_toolbar_height+53,60,16,tile_window)
  Local label7       =CreateLabel    (language$(078)+":",tile_window_width-220,tile_toolbar_height+03,60,16,tile_window)
  Local label8       =CreateLabel    (language$(168)+":",tile_window_width-220,tile_toolbar_height+28,60,16,tile_window)
  Local label9       =CreateLabel    (language$(236)+":",tile_window_width-220,tile_toolbar_height+53,60,16,tile_window)
  Local label10      =CreateLabel    (language$(267)+":",005,tile_window_height-29,60,16,tile_window)

  SetGadgetLayout tile_input_image  ,1,0,1,0
  SetGadgetLayout tile_input_sizex  ,1,0,1,0
  SetGadgetLayout tile_input_sizey  ,1,0,1,0
  SetGadgetLayout tile_input_factor ,1,0,1,0
  SetGadgetLayout tile_input_mask   ,1,0,1,0
  SetGadgetLayout tile_input_default,1,0,1,0
  SetGadgetLayout tile_input_defkill,1,0,1,0
  SetGadgetLayout tile_input_anim   ,0,1,1,0
  SetGadgetLayout tile_input_start  ,0,1,1,0
  SetGadgetLayout tile_input_time   ,0,1,1,0
  SetGadgetLayout tile_input_set    ,0,1,1,0
  SetGadgetLayout tile_input_view   ,1,0,0,1

  SetGadgetLayout button,0,0,0,1
  SetGadgetLayout label1,1,0,1,0
  SetGadgetLayout label2,1,0,1,0
  SetGadgetLayout label3,1,0,1,0
  SetGadgetLayout label4,1,0,1,0
  SetGadgetLayout label5,1,0,1,0
  SetGadgetLayout label6,1,0,1,0
  SetGadgetLayout label7,0,1,1,0
  SetGadgetLayout label8,0,1,1,0
  SetGadgetLayout label9,0,1,1,0
  SetGadgetLayout label10,1,0,0,1

  SetGadgetIconStrip tile_input_image,window_strip3
  SetGadgetIconStrip tile_input_anim ,window_strip3

  AddGadgetItem tile_input_anim,language$(176),0,3
  AddGadgetItem tile_input_anim,language$(177),0,4
  AddGadgetItem tile_input_anim,language$(178),0,5

  AddGadgetItem tile_input_view,language$(268)
  AddGadgetItem tile_input_view,language$(269)
  AddGadgetItem tile_input_view,language$(270)
  AddGadgetItem tile_input_view,language$(271)
  SelectGadgetItem tile_input_view,setup_tileview

  SetToolBarTips tile_toolbar,language$(237)+","+language$(238)+","+language$(239)+",,"+language$(240)+","+language$(241)+","+language$(242)+","+language$(243)+",,"+language$(244)
  DisableToolBarItem tile_toolbar,2
  DisableToolBarItem tile_toolbar,7

  SetMinWindowSize tile_window,GadgetWidth(tile_window),300
  DisableGadget window

  tile=tmptile
  tile_editor_start()
  tile_menu_start()
  tile_update()

  If tile=Null Then
    tile_add()
    tile_update()
  EndIf



  Repeat
    WaitEvent()

    Select EventID()
      Case $0110 ;ESC-------------------------------------------------
        Exit

      Case $0201 ;mousedown-------------------------------------------
        If EventSource()=tile_editor_canvas Then tile_editor_click()
        If EventSource()=tile_menu_canvas Then tile_menu_click()
        If EventSource()=tile_input_mask And tile<>Null Then
          r=(tile\mask And $FF0000)/$10000
          g=(tile\mask And $FF00)/$100
          b=(tile\mask And $FF)
          If RequestColor(r,g,b)=1 Then
            r=RequestedRed()
            g=RequestedGreen()
            b=RequestedBlue()
            If tile\image<>0 Then MaskImage tile\image,r,g,b
            tile\mask=r*$10000+g*$100+b
            tile_update()
          EndIf
        EndIf

      Case $0204 ;mousewheel--------------------------------------------
        Select extra_canvas
          Case tile_editor_canvas
            pos=SliderValue(tile_editor_slidery)
            SetSliderValue tile_editor_slidery,pos-MouseZSpeed()*20
            tile_editor_update(1)

          Case tile_menu_canvas
            pos=SliderValue(tile_menu_slidery)
            SetSliderValue tile_menu_slidery,pos-MouseZSpeed()*20
            tile_menu_update()
        End Select

      Case $0205 ;mouseenter--------------------------------------------
        extra_canvas=EventSource()

      Case $0401 ;gadgetevent-----------------------------------------
        Select EventSource()
          Case button
            Exit

          Case tile_editor_sliderx
            tile_editor_update(1)

          Case tile_editor_slidery
            tile_editor_update(1)

          Case tile_input_anim
            selected=SelectedGadgetItem(tile_input_anim)
            If selected=>0 And tile<>Null Then
              If tile_editor_anim=>1 And tile_editor_anim<=tile\anims Then
                bank=PeekInt(tile\banka,tile_editor_anim*4-4) ;animbank
                PokeByte bank,4,selected+1                    ;anim
                tile_update()
              EndIf
            EndIf

          Case tile_input_default
            If tile<>Null Then
              defval=extra_input(tile_input_default,0,0,255)
              If tile_lastclick=1 And tile_menu_selected=>1 And tile_menu_selected<=tile\count Then ;frame
                PokeByte tile\bankd,tile_menu_selected-1,defval
              ElseIf tile_lastclick=2 And tile_editor_anim=>1 And tile_editor_anim<=tile\anims Then
                bank=PeekInt(tile\banka,tile_editor_anim*4-4) ;animbank
                PokeByte bank,5,defval                        ;default value
              EndIf
              tile_menu_update()
              tile_editor_update()
            EndIf

          Case tile_input_defkill
            If tile<>Null Then
              If Confirm(language$(272))=1 Then
                ResizeBank tile\bankd,0
                ResizeBank tile\bankd,65536
                For i=1 To tile\anims
                  bank=PeekInt(tile\banka,i*4-4) ;animbank
                  PokeByte bank,5,0              ;default value
                Next
                tile_menu_update()
                tile_editor_update()
              EndIf
            EndIf

          Case tile_input_factor
            If tile<>Null Then max=tile\sizey-tile_minsize Else max=0
            If max<=0 Then SetGadgetText tile_input_factor,"0" : max=0
            tile\factor=extra_input(tile_input_factor,0,0,max)

          Case tile_input_image
            selected=SelectedGadgetItem(tile_input_image)
            If selected=>0 Then
              count=0
              For tile=Each tile
                If count=selected Then
                  tile_load(tile,tile\sizex,tile\sizey,0,1)
                  tile_update()
                  Exit
                EndIf
                count=count+1
              Next
            EndIf

          Case tile_input_set
            If tile<>Null Then
              If tile_editor_anim=>1 And tile_editor_anim<=tile\anims Then
                bank=PeekInt(tile\banka,tile_editor_anim*4-4) ;animbank
                frames=PeekShort(bank,0)                      ;frames
                If tile_editor_frame=>1 And tile_editor_frame<=frames Then
                  time=PeekShort(bank,tile_editor_frame*4+10) ;time
                  For i=1 To frames
                    PokeShort bank,i*4+10,time                ;time
                  Next
                  tile_update()
                EndIf
              EndIf
            EndIf

          Case tile_input_sizex
            selected=SelectedGadgetItem(tile_input_sizex)
            If selected=>0 And tile<>Null Then
              sizex=Int(GadgetItemText$(tile_input_sizex,selected))
              tile_load(tile,sizex,tile\sizey,0,1)
              tile_mask(tile)
              tile_update()
            EndIf

          Case tile_input_sizey
            selected=SelectedGadgetItem(tile_input_sizey)
            If selected=>0 And tile<>Null Then
              sizey=Int(GadgetItemText$(tile_input_sizey,selected))
              tile_load(tile,tile\sizex,sizey,0,1)
              tile_mask(tile)
              tile_update()
            EndIf

          Case tile_input_start
            If tile<>Null Then
              If tile_editor_anim=>1 And tile_editor_anim<=tile\anims Then
                bank=PeekInt(tile\banka,tile_editor_anim*4-4) ;animbank
                frames=PeekShort(bank,0)                      ;frames
                start=extra_input(tile_input_start,0,1,frames)
                PokeShort bank,2,start                        ;start
              EndIf
            EndIf

          Case tile_input_time
            If tile<>Null Then
              If tile_editor_anim=>1 And tile_editor_anim<=tile\anims Then
                bank=PeekInt(tile\banka,tile_editor_anim*4-4) ;animbank
                frames=PeekShort(bank,0)                      ;frames
                If tile_editor_frame=>1 And tile_editor_frame<=frames Then
                  time=extra_input(tile_input_time,0,0,32767)
                  PokeShort bank,tile_editor_frame*4+10,time  ;time
                EndIf
              EndIf
            EndIf

          Case tile_input_view
            setup_tileview=SelectedGadgetItem(tile_input_view)
            tile_update()

          Case tile_menu_sliderx
            tile_menu_update()

          Case tile_menu_slidery
            tile_menu_update()

          Case tile_toolbar

            Select EventData()
              Case -1 ;load
                tile_add()
              Case 0 ;del
                tile_del(tile)
              Case 1 ;replace
                tile_replace(tile)
              Case 3 ;anim add
                tile_addanim(tile)
              Case 4 ;anim del
                tile_delanim(tile,tile_editor_anim)
              Case 5 ;frame del
                tile_delframe(tile,tile_editor_anim)
              Case 6 ;frame add
                tile_addframe(tile,tile_editor_anim)
              Case 8 ;animate
                tile_animate(tile,tile_editor_anim)
            End Select
            tile_update()
        End Select

      Case $0802 ;windowsize--------------------------------------------
        tile_editor_resize()
        tile_menu_resize()

      Case $0803 ;windowclose-----------------------------------------
        Exit

    End Select
  Forever


  tile_editor_exit()
  tile_menu_exit()
  tile_needdef()

  EnableGadget window
  FreeGadget label10
  FreeGadget label9
  FreeGadget label8
  FreeGadget label7
  FreeGadget label6
  FreeGadget label5
  FreeGadget label4
  FreeGadget label3
  FreeGadget label2
  FreeGadget label1
  FreeGadget button
  FreeGadget tile_input_view
  FreeGadget tile_input_set
  FreeGadget tile_input_time
  FreeGadget tile_input_start
  FreeGadget tile_input_anim
  FreeGadget tile_input_defkill
  FreeGadget tile_input_default
  FreeGadget tile_input_mask
  FreeGadget tile_input_factor
  FreeGadget tile_input_sizey
  FreeGadget tile_input_sizex
  FreeGadget tile_input_image
  FreeGadget tile_toolbar
  FreeGadget tile_window
  ActivateWindow window

  maker_update()
  menu_update()
  editor_update()
End Function





;---------------------------------------------------------------------
;tile: tileset handle
;anim: 0=add anim
;      >0=del anim nr
;---------------------------------------------------------------------
Function tile_recalc(tile.tile,anim)
  Local count
  Local layer.layer
  Local value
  Local x
  Local y

  Local subwin =CreateWindow (language$(245),(window_maxx-220)/2,(window_maxy-40)/2,220,40,window,33)
  Local progbar=CreateProgBar(10,10,200,20,subwin)

  For layer=Each layer
    If layer\tile=tile Then
      If layer\code=layer_map Or layer\code=layer_iso1 Or layer\code=layer_iso2 Or layer\code=layer_hex1 Or layer\code=layer_hex2 Then

        For y=0 To layer\sizey-1
          For x=0 To layer\sizex-1
            value=layer_getvalue2(layer,x,y)
            If anim>0 And value=anim Then layer_setvalue2(layer,x,y,0)
            If anim>0 And value>anim Then layer_setvalue2(layer,x,y,value-1)
            If anim=0 And value>tile\anims Then layer_setvalue2(layer,x,y,value+1)
          Next
        Next

      ElseIf layer\code=layer_image Or layer\code=layer_block Then

        value=layer\frame
        If anim>0 And value=anim Then layer\frame=0
        If anim>0 And value>anim Then layer\frame=value-1
        If anim=0 And value>tile\anims Then layer\frame=value+1
     
      EndIf
    EndIf

    count=count+1
    UpdateProgBar progbar,Float#(count)/Float#(layer_count)
  Next

  FreeGadget progbar
  FreeGadget subwin
End Function





;---------------------------------------------------------------------
;tile: tileset handle
;---------------------------------------------------------------------
Function tile_replace(tile.tile)
  Local i
  Local name1$
  Local name2$
  Local tmptile.tile

  If tile=Null Then Return

  ChangeDir map_path$
  name1$=RequestFile(language$(84),"bmp;*.jpg;*.png",0)
  ChangeDir map_app$

  If name1$="" Then Return

  name2$=name1$
  For i=Len(name1$) To 1 Step -1
    If Mid$(name1$,i,1)="\" Or Mid$(name1$,i,1)="/" Then
      name2$=Mid$(name1$,i+1)
      Exit
    EndIf
  Next

  If Len(name2$)>12 Then
    Notify language$(85)
    Return
  EndIf

  For tmptile=Each tile
    If Lower$(tmptile\file$)=Lower$(name2$) Then
      Notify language$(224)
      Return
    EndIf
  Next

  If FileType(map_path$+name2$)<>1 Then
    Notify language$(86)
    Return
  EndIf

  If tile\image<>0 Then FreeImage tile\image
  tile\image=0
  tile\file$=name2$

  Insert tile After Last tile
  For tmptile=Each tile
    If Lower$(tmptile\file$)>Lower$(tile\file$) Then
      Insert tile Before tmptile
      Exit
    EndIf
  Next

  tile_load(tile,tile\sizex,tile\sizey,0,0)
End Function





;---------------------------------------------------------------------
Function tile_reset()
  Local bank
  Local i

  For tile=Each tile

    If tile\banka<>0 Then
      For i=1 To tile\anims
        bank=PeekInt(tile\banka,i*4-4) ;animbank
        FreeBank bank
      Next
      FreeBank tile\banka
    EndIf

    If tile\bankd<>0 Then FreeBank tile\bankd
    If tile\bankx<>0 Then FreeBank tile\bankx
    If tile\banky<>0 Then FreeBank tile\banky
    If tile\image<>0 Then FreeImage tile\image
  Next

  Delete Each tile
End Function





;---------------------------------------------------------------------
Function tile_update()
  Local tmptile.tile

  If tile=Null Then tile=First tile
  If tile<>Null Then
    banksizex=BankSize(tile\bankx)
    banksizey=BankSize(tile\banky)

    If tile_lastclick=1 And tile_menu_selected=>1 And tile_menu_selected<=tile\count Then ;frame
      defval=PeekByte(tile\bankd,tile_menu_selected-1)
      defenabled=1
    ElseIf tile_lastclick=2 And tile_editor_anim=>1 And tile_editor_anim<=tile\anims Then ;anim
      bank  =PeekInt(tile\banka,tile_editor_anim*4-4) ;animbank
      defval=PeekByte(bank,5) ;default value
      defenabled=1
    EndIf

    If tile_editor_anim=>1 And tile_editor_anim<=tile\anims Then
      bank  =PeekInt(tile\banka,tile_editor_anim*4-4) ;animbank
      frames=PeekShort(bank,0) ;frames
      start =PeekShort(bank,2) ;start
      anim  =PeekByte (bank,4) ;anim
      animenabled=1

      If tile_editor_frame=>1 And tile_editor_frame<=frames Then
        time=PeekShort(bank,tile_editor_frame*4+10) ;time
        animenabled2=1
      EndIf
    EndIf
  EndIf

  ;Image combobox
  If tile<>Null Then
    EnableGadget tile_input_image
    ClearGadgetItems tile_input_image
    For tmptile=Each tile
      AddGadgetItem tile_input_image , tmptile\file$ , (tmptile=tile) , (tmptile\image=0)
      count=count+1
    Next
  Else
    DisableGadget tile_input_image
    ClearGadgetItems tile_input_image
  EndIf

  ;Image width combobox
  If tile<>Null And banksizex/2>0 Then
    EnableGadget tile_input_sizex
    ClearGadgetItems tile_input_sizex
    For i=0 To banksizex/2-1
      value=PeekShort(tile\bankx,i*2)
      AddGadgetItem tile_input_sizex , Str$(value) , (value=tile\sizex)
    Next
  Else
    DisableGadget tile_input_sizex
    ClearGadgetItems tile_input_sizex
  EndIf

  ;Image height combobox
  If tile<>Null And banksizey/2>0 Then
    EnableGadget tile_input_sizey
    ClearGadgetItems tile_input_sizey
    For i=0 To banksizey/2-1
      value=PeekShort(tile\banky,i*2)
      AddGadgetItem tile_input_sizey , Str$(value) , (value=tile\sizey)
    Next
  Else
    DisableGadget tile_input_sizey
    ClearGadgetItems tile_input_sizey
  EndIf

  ;Tile factor
  If tile<>Null Then
    EnableGadget tile_input_factor
    SetGadgetText tile_input_factor,Str$(tile\factor)
  Else
    DisableGadget tile_input_factor
    SetGadgetText tile_input_factor,""
  EndIf  

  ;Mask color
  If tile<>Null Then
    r=(tile\mask And $FF0000)/$10000
    g=(tile\mask And $FF00)/$100
    b=(tile\mask And $FF)
    SetPanelColor tile_input_mask,r,g,b
  Else
    SetPanelColor tile_input_mask,192,192,192
  EndIf  

  ;Default value
  If tile<>Null And defenabled=1 Then
    EnableGadget tile_input_default
    SetGadgetText tile_input_default,Str$(defval)
  Else
    DisableGadget tile_input_default
    SetGadgetText tile_input_default,""
  EndIf

  ;Delete default values
  If tile<>Null Then
    EnableGadget tile_input_defkill
  Else
    DisableGadget tile_input_defkill
  EndIf

  ;Animation mode
  If tile<>Null And animenabled=1 Then
    EnableGadget tile_input_anim
    SelectGadgetItem tile_input_anim,anim-1
  Else
    DisableGadget tile_input_anim
    SelectGadgetItem tile_input_anim,1
  EndIf

  ;Animation startframe
  If tile<>Null And animenabled=1 Then
    EnableGadget tile_input_start
    SetGadgetText tile_input_start,Str$(start)
  Else
    DisableGadget tile_input_start
    SetGadgetText tile_input_start,""
  EndIf

  ;Animation time + button
  If tile<>Null And animenabled2=1 Then
    EnableGadget tile_input_set
    EnableGadget tile_input_time
    SetGadgetText tile_input_time,Str$(time)
  Else
    DisableGadget tile_input_set
    DisableGadget tile_input_time
    SetGadgetText tile_input_time,""
  EndIf

  DisableToolBarItem tile_toolbar,-1
  DisableToolBarItem tile_toolbar,0
  DisableToolBarItem tile_toolbar,1
  DisableToolBarItem tile_toolbar,3
  DisableToolBarItem tile_toolbar,4
  DisableToolBarItem tile_toolbar,5
  DisableToolBarItem tile_toolbar,6
  DisableToolBarItem tile_toolbar,8

  If count<tile_maxtilesets Then EnableToolBarItem tile_toolbar,-1 ;add set
  If tile<>Null Then
    EnableToolBarItem tile_toolbar,0 ;del set
    EnableToolBarItem tile_toolbar,1 ;replace set
    If tile\anims<tile_maxanims Then EnableToolBarItem tile_toolbar,3 ;add anim
    If tile_editor_anim=>1 And tile_editor_anim<=tile\anims Then
      EnableToolBarItem tile_toolbar,4 ;del anim
      EnableToolBarItem tile_toolbar,8 ;animate
      If frames>1 Then EnableToolBarItem tile_toolbar,5 ;del frame
      If frames<tile_maxframes Then EnableToolBarItem tile_toolbar,6 ;add frame
    EndIf
  EndIf

  If mode=0 Then
    tile_editor_update()
    tile_menu_update()
    ActivateGadget tile_window
  EndIf
End Function