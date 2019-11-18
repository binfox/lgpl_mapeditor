Type layer          ; 0 1 2 3 4 5 6 7 8 9 10 11 12|
  Field ascii       ; X X X X X X X X X X X  X  X | ascii-metadata
  Field bank1       ;   X X X X X .               | bank basedata
  Field bank2       ;   X X X X X .               | bank datalayer
  Field bank3       ; X X X X X X X X X X X  X  X | bank metadata
  Field bank4       ; X X X X X X X X X X X  X  X | bank variables
  Field code        ; X X X X X X X X X X X  X  X | object code
  Field depth1      ;   X X X X X .               | base depth
  Field depth2      ;   X X X X X .               | data depth
  Field frame       ;               X X           | image frame
  Field layer.layer ;             X   X           | layer handle
  Field mask        ;   X         . X X           | tile mask   / image mask
  Field mode        ; X X           X X           | backgr mode / map repeat / image mode (anim)
  Field name$       ; X X X X X X X X X X X  X  X | object name
  Field parax       ; X X X X X X X X X X X  X  X | parallax x / block offset x
  Field paray       ; X X X X X X X X X X X  X  X | parallax y / block offset y
  Field posx        ; X X X X X X X X X X X  X  X | object posx
  Field posy        ; X X X X X X X X X X X  X  X | object posy
  Field sizex       ; X X X X X X .       X  X  X | object sizex
  Field sizey       ; X X X X X X .       X  X  X | object sizey
  Field start       ;       X X X . X X           | tile shift  / image start (anim)
  Field tile.tile   ;   X X X X X . X X           | tileset handle
  Field tmp         ;                             | tmp
  Field visible     ; X X X X X X X X X X X  X  X | object visible
End Type

Type undolayer          ; 0 1 2 3 4 5 6 7 8 9 10 11 12|
  Field ascii       ; X X X X X X X X X X X  X  X | ascii-metadata
  Field bank1       ;   X X X X X .               | bank basedata
  Field bank2       ;   X X X X X .               | bank datalayer
  Field bank3       ; X X X X X X X X X X X  X  X | bank metadata
  Field bank4       ; X X X X X X X X X X X  X  X | bank variables
  Field code        ; X X X X X X X X X X X  X  X | object code
  Field depth1      ;   X X X X X .               | base depth
  Field depth2      ;   X X X X X .               | data depth
  Field frame       ;               X X           | image frame
  Field layer.layer ;             X   X           | layer handle
  Field mask        ;   X         . X X           | tile mask   / image mask
  Field mode        ; X X           X X           | backgr mode / map repeat / image mode (anim)
  Field name$       ; X X X X X X X X X X X  X  X | object name
  Field parax       ; X X X X X X X X X X X  X  X | parallax x / block offset x
  Field paray       ; X X X X X X X X X X X  X  X | parallax y / block offset y
  Field posx        ; X X X X X X X X X X X  X  X | object posx
  Field posy        ; X X X X X X X X X X X  X  X | object posy
  Field sizex       ; X X X X X X .       X  X  X | object sizex
  Field sizey       ; X X X X X X .       X  X  X | object sizey
  Field start       ;       X X X . X X           | tile shift  / image start (anim)
  Field tile.tile   ;   X X X X X . X X           | tileset handle
  Field tmp         ;                             | tmp
  Field visible     ; X X X X X X X X X X X  X  X | object visible
  Field undonr%
End Type

Const layer_back    =0
Const layer_map     =1
Const layer_iso1    =2
Const layer_iso2    =3
Const layer_hex1    =4
Const layer_hex2    =5
Const layer_clone   =6
Const layer_image   =7
Const layer_block   =8
Const layer_point   =9
Const layer_line    =10
Const layer_rect    =11
Const layer_oval    =12
Const layer_maxcount=5000      ;10 / 5000
Const layer_maxtiles=100000000 ;10000 / 100000000

Global layer_list.layer[layer_maxcount]
Global layer.layer
Global layer_count
Global layer_nr
Global layer_colour ;back
Global layer_file$  ;back
Global layer_handle ;back
Global layer_maxx   ;temp
Global layer_maxy   ;temp
Global layer_minx   ;temp
Global layer_miny   ;temp
Global layer_x      ;temp
Global layer_y      ;temp
Global layer_width  ;temp
Global layer_height ;temp
Global undolayers%=0


Function layer_eraseundo()
undolayers=0
	DisableToolBarItem toolbar,toolbar_undo 

	Local unlay.undolayer	
	For unlay=Each undolayer

			If unlay\bank1 <> 0 Then
				FreeBank( unlay\bank1)
			EndIf
			If unlay\bank2 <> 0 Then 
				FreeBank( unlay\bank2)
			EndIf
			If unlay\bank3 <> 0 Then 
				FreeBank( unlay\bank3)
			EndIf
			If unlay\bank4 <> 0 Then 
				FreeBank( unlay\bank4)
			EndIf		
		Delete unlay			
	Next	
End Function

Function layer_undo()
undolayers=undolayers-1
	Local searchlayer.undolayer
	Local maxlay%=0
	Local i%=0

	
	;höchste undolayernr suchen
	For searchlayer=Each undolayer
		If searchlayer\undonr > maxlay Then
			maxlay=searchlayer\undonr

		EndIf
	Next

	Local orglayer.layer
	Local unlay.undolayer	


	orglayer=First layer
	
	For unlay=Each undolayer
		
		If unlay\undonr = maxlay Then
				
			orglayer\ascii =	unlay\ascii 
			
			If unlay\bank1 <> 0 Then
				orglayer\bank1 = CreateBank(BankSize(unlay\bank1))
				CopyBank(unlay\bank1,0,orglayer\bank1,0,BankSize(unlay\bank1))
				FreeBank( unlay\bank1)
				unlay\bank1=0
				
			EndIf
			If unlay\bank2 <> 0 Then 
				orglayer\bank2 = CreateBank(BankSize(unlay\bank2))
				CopyBank(unlay\bank2,0,orglayer\bank2,0,BankSize(unlay\bank2))
				FreeBank( unlay\bank2)
				unlay\bank2=0

			EndIf
			If unlay\bank3 <> 0 Then 
				orglayer\bank3 = CreateBank(BankSize(unlay\bank3))
				CopyBank(unlay\bank3,0,orglayer\bank3,0,BankSize(unlay\bank3))
				FreeBank( unlay\bank3)
				unlay\bank3=0
			
			EndIf
			If unlay\bank4 <> 0 Then 
				orglayer\bank4 = CreateBank(BankSize(unlay\bank4))
				CopyBank(unlay\bank4,0,orglayer\bank4,0,BankSize(unlay\bank4))
				FreeBank( unlay\bank4)
				unlay\bank4=0
			EndIf

			orglayer\code=	unlay\code
			orglayer\depth1=	unlay\depth1
			orglayer\depth2=	unlay\depth2
			orglayer\frame =	unlay\frame 
			orglayer\layer=	unlay\layer
			orglayer\mask=	unlay\mask
			orglayer\mode=	unlay\mode
			orglayer\name$=	unlay\name
			orglayer\parax=	unlay\parax
			orglayer\paray=	unlay\paray
			orglayer\posx=	unlay\posx
			orglayer\posy =	unlay\posy 
			orglayer\sizex=	unlay\sizex
			orglayer\sizey=	unlay\sizey
			orglayer\start=	unlay\start
			orglayer\tile=	unlay\tile
			orglayer\tmp=	unlay\tmp
			orglayer\visible=unlay\visible
			unlay\undonr = -12

			;Delete unlay
			orglayer = After orglayer
		EndIf
	Next


	
;	list_select(layer_nr)
	editor_update(1)
	For unlay=Each undolayer
		If unlay\undonr = -12 Then
			Delete unlay
		EndIf
	Next	
End Function

Function layer_createundo()
undolayers=undolayers+1


	EnableToolBarItem toolbar,toolbar_undo 
	Local searchlayer.undolayer
	Local maxlay%=0
	
	;höchste undolayernr suchen
	For searchlayer=Each undolayer
		If searchlayer\undonr > maxlay Then maxlay=searchlayer\undonr
	Next
	
	maxlay=maxlay+1
	
	Local orglayer.layer
	
	Local newundo.undolayer

	For orglayer=Each layer
		newundo=New undolayer
		newundo\ascii =	orglayer\ascii 
		If orglayer\bank1 <> 0 Then
			newundo\bank1 = CreateBank(BankSize(orglayer\bank1))
			CopyBank(orglayer\bank1,0,newundo\bank1,0,BankSize(orglayer\bank1))
		EndIf
		If orglayer\bank2 <> 0 Then
			newundo\bank2 = CreateBank(BankSize(orglayer\bank2))
			CopyBank(orglayer\bank2,0,newundo\bank2,0,BankSize(orglayer\bank2))
		EndIf
		If orglayer\bank3 <> 0 Then
			newundo\bank3 = CreateBank(BankSize(orglayer\bank3))
			CopyBank(orglayer\bank3,0,newundo\bank3,0,BankSize(orglayer\bank3))
		EndIf
		If orglayer\bank4 <> 0 Then
			newundo\bank4 = CreateBank(BankSize(orglayer\bank4))
			CopyBank(orglayer\bank4,0,newundo\bank4,0,BankSize(orglayer\bank4))
		EndIf

		newundo\code=	orglayer\code
		newundo\depth1=	orglayer\depth1
		newundo\depth2=	orglayer\depth2
		newundo\frame =	orglayer\frame 
		newundo\layer=	orglayer\layer
		newundo\mask=	orglayer\mask
		newundo\mode=	orglayer\mode
		newundo\name$=	orglayer\name
		newundo\parax=	orglayer\parax
		newundo\paray=	orglayer\paray
		newundo\posx=	orglayer\posx
		newundo\posy =	orglayer\posy 
		newundo\sizex=	orglayer\sizex
		newundo\sizey=	orglayer\sizey
		newundo\start=	orglayer\start
		newundo\tile=	orglayer\tile
		newundo\tmp=	orglayer\tmp
		newundo\visible=orglayer\visible
    	newundo\undonr=maxlay
	Next
End Function




;---------------------------------------------------------------------
Function layer_add()
  If layer_count=>layer_maxcount Then Return

  Local count
  Local i
  Local ok
  Local option=1
  Local selected
  Local tile.tile
  Local time
  Local version

  Local subwin       =CreateWindow  (language$(7),GadgetX(window)+100,GadgetY(window)+90,200,330,window,33)
  Local subwin_width =ClientWidth   (subwin)
  Local subwin_height=ClientHeight  (subwin)
  Local panel        =CreatePanel   (5,5,subwin_width-10,250,subwin,1)
  Local panel_width  =ClientWidth   (panel)
  Local panel_height =ClientHeight  (panel)
  Local canvas       =CreateCanvas  (0,0,panel_width,panel_height,panel)
  Local canvas_width =GadgetWidth   (canvas)
  Local canvas_height=GadgetHeight  (canvas)
  Local label        =CreateLabel   (language$(75)+":",5,268,50,20,subwin)
  Local gadget_tile  =CreateComboBox(55,265,140,22,subwin)
  Local button_ok    =CreateButton  (language$(70),subwin_width/2-90,subwin_height-32,85,22,subwin)
  Local button_cancel=CreateButton  (language$(71),subwin_width/2+5,subwin_height-32,85,22,subwin)

  SetGadgetIconStrip gadget_tile,window_strip3

  For tile=Each tile
    count=count+1
    AddGadgetItem gadget_tile,tile\file$,(count=1),(tile\image=0)
  Next
  tile=First tile

  DisableGadget window
  Goto update


  Repeat
    WaitEvent()
    Select EventID()
      Case $0110 ;ESC-------------------------------------------------
        Exit

      Case $0201 ;mousedown---------------------------------------------
        If EventSource()=canvas Then
          i=(MouseY(canvas)-3)/20+1
          If i>layer_oval Then i=layer_oval
          If demo=1 Then
            If i=layer_iso1 Or i=layer_iso2 Or i=layer_hex1 Or i=layer_hex2 Then
              i=option
              time=MilliSecs()-300
            EndIf
          EndIf
          If option=i And MilliSecs()-time<300 Then ok=1 : Exit
          option=i
          time=MilliSecs()
        EndIf

      Case $0401 ;gadgetevent-----------------------------------------
        Select EventSource()
          Case button_ok
            ok=1
            Exit
          Case button_cancel
            Exit
          Case gadget_tile

            selected=SelectedGadgetItem(gadget_tile)
            count=0
            For tile=Each tile
              If count=selected Then Exit
              count=count+1
            Next
            
        End Select

      Case $0803 ;windowclose-----------------------------------------
        Exit
    End Select

    .update
    If First tile=Null Then
      DisableGadget gadget_tile
    ElseIf option=layer_map Or option=layer_iso1 Or option=layer_iso2 Or option=layer_hex1 Or option=layer_hex2 Or option=layer_image Or option=layer_block Then
      EnableGadget gadget_tile
    Else
      DisableGadget gadget_tile
    EndIf

    SetBuffer CanvasBuffer(canvas)
    SetFont window_font1
    ClsColor 255,255,255
    Cls

    Color 240,240,240
    Rect 3,option*20-17,canvas_width-6,20
    Color 0,0,0
    Rect 3,option*20-17,canvas_width-6,20,0

    For i=layer_map To layer_oval
      version=2
      If demo=1 Then
        If i=layer_iso1 Or i=layer_iso2 Or i=layer_hex1 Or i=layer_hex2 Then version=1
      EndIf
      If version=1 Then DrawImage window_icons1,5,i*20-15,i
      If version=2 Then DrawImage window_icons2,5,i*20-15,i
    Next

    For i=layer_map To layer_clone
      Text 25,i*20-15,language$(76)
    Next

    For i=layer_image To layer_oval
      Text 25,i*20-15,language$(77)
    Next


    Text 70,005,"("+language$(41)+")"
    Text 70,025,"("+language$(42)+")"
    Text 70,045,"("+language$(43)+")"
    Text 70,065,"("+language$(44)+")"
    Text 70,085,"("+language$(45)+")"
    Text 70,105,"("+language$(46)+")"
    Text 70,125,"("+language$(47)+")"
    Text 70,145,"("+language$(48)+")"
    Text 70,165,"("+language$(49)+")"
    Text 70,185,"("+language$(50)+")"
    Text 70,205,"("+language$(51)+")"
    Text 70,225,"("+language$(52)+")"

    FlipCanvas canvas
  Forever


  EnableGadget window
  FreeGadget button_cancel
  FreeGadget button_ok
  FreeGadget gadget_tile
  FreeGadget label
  FreeGadget canvas
  FreeGadget panel
  FreeGadget subwin
  ActivateWindow window
  If ok=0 Then Return

  Select option
    Case layer_map   : layer_map_add  (tile)
    Case layer_iso1  : layer_iso1_add (tile)
    Case layer_iso2  : layer_iso2_add (tile)
    Case layer_hex1  : layer_hex1_add (tile)
    Case layer_hex2  : layer_hex2_add (tile)
    Case layer_clone : layer_clone_add()
    Case layer_image : layer_image_add(tile)
    Case layer_block : layer_block_add(tile)
    Case layer_point : layer_point_add()
    Case layer_line  : layer_line_add ()
    Case layer_rect  : layer_rect_add ()
    Case layer_oval  : layer_oval_add ()
  End Select
  layer_eraseundo()
  list_select(layer_nr)
End Function





;---------------------------------------------------------------------
Function layer_clone()
  Local size
  Local tmplayer.layer=layer

  If layer=Null Then Return
  If layer\code<=0 Then Return

  layer        =New layer
  layer\ascii  =tmplayer\ascii
  layer\code   =tmplayer\code
  layer\depth1 =tmplayer\depth1
  layer\depth2 =tmplayer\depth2
  layer\frame  =tmplayer\frame
  layer\layer  =tmplayer\layer
  layer\mask   =tmplayer\mask
  layer\mode   =tmplayer\mode
  layer\name$  =tmplayer\name$
  layer\parax  =tmplayer\parax
  layer\paray  =tmplayer\paray
  layer\posx   =tmplayer\posx
  layer\posy   =tmplayer\posy
  layer\sizex  =tmplayer\sizex
  layer\sizey  =tmplayer\sizey
  layer\start  =tmplayer\start
  layer\tile   =tmplayer\tile
  layer\visible=tmplayer\visible

  If tmplayer\bank1<>0 Then
    size=BankSize(tmplayer\bank1)
    layer\bank1=CreateBank(size)
    CopyBank tmplayer\bank1,0,layer\bank1,0,size
  EndIf

  If tmplayer\bank2<>0 Then
    size=BankSize(tmplayer\bank2)
    layer\bank2=CreateBank(size)
    CopyBank tmplayer\bank2,0,layer\bank2,0,size
  EndIf

  If tmplayer\bank3<>0 Then
    size=BankSize(tmplayer\bank3)
    layer\bank3=CreateBank(size)
    CopyBank tmplayer\bank3,0,layer\bank3,0,size
  EndIf
  layer_eraseundo()
  layer_count=layer_count+1
  layer_nr=layer_count  
  layer_list[layer_nr]=layer
  list_select(layer_nr)
End Function





;---------------------------------------------------------------------
;nr:     layer nr
;update: 0=no, 1=yes
;---------------------------------------------------------------------
Function layer_delete(nr,update=1)
  Local bank
  Local i
  Local size

  If nr<1 Then Return
  If nr>layer_count Then Return

  If layer_list[nr]\code<>layer_clone Then
    If layer_list[nr]\bank1<>0 Then FreeBank layer_list[nr]\bank1
    If layer_list[nr]\bank2<>0 Then FreeBank layer_list[nr]\bank2
  EndIf
  If layer_list[nr]\bank3<>0 Then FreeBank layer_list[nr]\bank3
  If layer_list[nr]\bank4<>0 Then
    size=BankSize(layer_list[nr]\bank4)
    For i=1 To size/4-1 ;important! (entry 0=total size)
      bank=PeekInt(layer_list[nr]\bank4,i*4)
      FreeBank bank
    Next
    FreeBank layer_list[nr]\bank4
  EndIf

  Delete layer_list[nr]

  For i=nr To layer_count-1
    layer_list[i]=layer_list[i+1]
  Next
  layer_list[layer_count]=Null

  layer_count=layer_count-1
  If layer_nr>layer_count Then layer_nr=layer_count
  If update=1 Then list_select(layer_nr)
End Function





;---------------------------------------------------------------------
;nr: layer nr
;---------------------------------------------------------------------
Function layer_down(nr)
  If nr<2 Then Return
  If nr>layer_count Then Return

  layer=layer_list[nr]
  layer_list[nr]=layer_list[nr-1]
  layer_list[nr-1]=layer
  list_select(nr-1)
End Function





;---------------------------------------------------------------------
;layer:  layer handle
;x:      tile pos x
;y:      tile pos y
;RETURN: 0=void, 1=frame, 2=anim
;---------------------------------------------------------------------
Function layer_getcode(layer.layer,x,y)
  Local anims
  Local count
  Local mode
  Local offset
  Local value

  If layer=Null Then Return
  If layer\bank1=0 Then Return
  If x<0 Or y<0 Or x>layer\sizex-1 Or y>layer\sizey-1 Then Return

  If layer\tile<>Null Then
    anims=layer\tile\anims
    count=layer\tile\count
  EndIf

  If layer\depth1=4 Then
    offset=((y*layer\sizex+x)/2)
    mode  =((y*layer\sizex+x) And 1)*4
    value =(PeekByte(layer\bank1,offset) Shr mode) And 15
  ElseIf layer\depth1=8 Then
    offset=y*layer\sizex+x
    value=PeekByte(layer\bank1,offset)
  ElseIf layer\depth1=12 Then
    offset=((y*layer\sizex+x)*3)/2
    mode  =((y*layer\sizex+x) And 1)*4
    value =(PeekShort(layer\bank1,offset) Shr mode) And 4095
  ElseIf layer\depth1=16 Then
    offset=(y*layer\sizex+x)*2
    value=PeekShort(layer\bank1,offset)
  EndIf

  If value=>1 And value<=anims Then ;anim
    Return 2
  ElseIf value=>anims+1 And value<=count+anims Then ;frame
    Return 1
  EndIf
End Function





;---------------------------------------------------------------------
;layer:  layer handle
;x:      tile pos x
;y:      tile pos y
;RETURN: tile value
;---------------------------------------------------------------------
Function layer_getdata(layer.layer,x,y)
  Local mode
  Local offset
  Local value

  If layer=Null Then Return
  If layer\bank2=0 Then Return
  If x<0 Or y<0 Or x>layer\sizex-1 Or y>layer\sizey-1 Then Return

  If layer\depth2=1 Then
    offset=((y*layer\sizex+x)/8)
    mode  =((y*layer\sizex+x) Mod 8)
    value =(PeekByte(layer\bank2,offset) Shr mode) And 1
  ElseIf layer\depth2=2 Then
    offset=((y*layer\sizex+x)/4)
    mode  =((y*layer\sizex+x) Mod 4)*2
    value =(PeekByte(layer\bank2,offset) Shr mode) And 3
  ElseIf layer\depth2=4 Then
    offset=((y*layer\sizex+x)/2)
    mode  =((y*layer\sizex+x) Mod 2)*4
    value =(PeekByte(layer\bank2,offset) Shr mode) And 15
  ElseIf layer\depth2=8 Then
    offset=y*layer\sizex+x
    value =PeekByte(layer\bank2,offset)
  EndIf

  Return value
End Function





;---------------------------------------------------------------------
;layer:  layer handle
;x:      tile pos x
;y:      tile pos y
;RETURN: tile value
;---------------------------------------------------------------------
Function layer_getvalue(layer.layer,x,y)
  Local anims
  Local count
  Local mode
  Local offset
  Local value

  If layer=Null Then Return
  If layer\bank1=0 Then Return
  If x<0 Or y<0 Or x>layer\sizex-1 Or y>layer\sizey-1 Then Return

  If layer\tile<>Null Then
    anims=layer\tile\anims
    count=layer\tile\count
  EndIf

  If layer\depth1=4 Then
    offset=((y*layer\sizex+x)/2)
    mode  =((y*layer\sizex+x) And 1)*4
    value =(PeekByte(layer\bank1,offset) Shr mode) And 15
  ElseIf layer\depth1=8 Then
    offset=y*layer\sizex+x
    value=PeekByte(layer\bank1,offset)
  ElseIf layer\depth1=12 Then
    offset=((y*layer\sizex+x)*3)/2
    mode  =((y*layer\sizex+x) And 1)*4
    value =(PeekShort(layer\bank1,offset) Shr mode) And 4095
  ElseIf layer\depth1=16 Then
    offset=(y*layer\sizex+x)*2
    value=PeekShort(layer\bank1,offset)
  EndIf

  If value=>1 And value<=anims Then ;anim
    Return value
  ElseIf value=>anims+1 And value<=count+anims Then ;frame
    Return value-anims
  EndIf
End Function





;---------------------------------------------------------------------
;layer:  layer handle
;x:      tile pos x
;y:      tile pos y
;RETURN: tile value (MIXED anim+frame)
;---------------------------------------------------------------------
Function layer_getvalue2(layer.layer,x,y)
  Local mode
  Local offset
  Local value

  If layer=Null Then Return
  If layer\bank1=0 Then Return
  If x<0 Or y<0 Or x>layer\sizex-1 Or y>layer\sizey-1 Then Return

  If layer\depth1=4 Then
    offset=((y*layer\sizex+x)/2)
    mode  =((y*layer\sizex+x) And 1)*4
    value =(PeekByte(layer\bank1,offset) Shr mode) And 15
  ElseIf layer\depth1=8 Then
    offset=y*layer\sizex+x
    value=PeekByte(layer\bank1,offset)
  ElseIf layer\depth1=12 Then
    offset=((y*layer\sizex+x)*3)/2
    mode  =((y*layer\sizex+x) And 1)*4
    value =(PeekShort(layer\bank1,offset) Shr mode) And 4095
  ElseIf layer\depth1=16 Then
    offset=(y*layer\sizex+x)*2
    value=PeekShort(layer\bank1,offset)
  EndIf

  Return value
End Function





;---------------------------------------------------------------------
;layer:   layer handle
;---------------------------------------------------------------------
Function layer_jump(layer.layer)
  Local canvash
  Local canvasw
  Local code
  Local minx
  Local miny
  Local parax
  Local paray
  Local posx
  Local posy
  Local px
  Local py
  Local sizex
  Local sizey

  canvasw=editor_width()
  canvash=editor_height()

  minx=2147483647
  miny=2147483647

  code =layer\code
  parax=layer\parax
  paray=layer\paray
  posx =layer\posx
  posy =layer\posy
  sizex=layer\sizex
  sizey=layer\sizey

  Select code
    Case layer_line
      If parax=100 Or toolbar_xpara=0 Then
        px=(posx-50)
        If minx>px Then minx=px
        px=(sizex-50)
        If minx>px Then minx=px
      ElseIf parax>0 Then
        px=(posx-50)*100/parax
        If minx>px Then minx=px
        px=(sizex-50)*100/parax
        If minx>px Then minx=px
      EndIf

      If paray=100 Or toolbar_xpara=0 Then
        py=(posy-50)
        If miny>py Then miny=py
        py=(sizey-50)
        If miny>py Then miny=py
      ElseIf paray>0 Then
        py=(posy-50)*100/paray
        If miny>py Then miny=py
        py=(sizey-50)*100/paray
        If miny>py Then miny=py
      EndIf

    Case layer_oval
      If parax=100 Or toolbar_xpara=0 Then
        px=(posx-50-sizex)
        If minx>px Then minx=px
      ElseIf parax>0 Then
        px=(posx-50-sizex)*100/parax
        If minx>px Then minx=px
      EndIf

      If paray=100 Or toolbar_xpara=0 Then
        py=(posy-50-sizey)
        If miny>py Then miny=py
      ElseIf paray>0 Then
        py=(posy-50-sizey)*100/paray
        If miny>py Then miny=py
      EndIf

    Case layer_block
      layer_block_pos(layer)
      layer_block_size(layer)
      posx=layer_x
      posy=layer_y
      If layer\layer<>Null Then
        parax=layer\layer\parax
        paray=layer\layer\paray
      EndIf

      If parax=100 Or toolbar_xpara=0 Then
        px=(posx-50)
        If minx>px Then minx=px
      ElseIf parax>0 Then
        px=(posx-50)*100/parax
        If minx>px Then minx=px
      EndIf

      If paray=100 Or toolbar_xpara=0 Then
        py=(posy-50)
        If miny>py Then miny=py
      ElseIf paray>0 Then
        py=(posy-50)*100/paray
        If miny>py Then miny=py
      EndIf

    Default
      If code=layer_map   Then layer_map_size  (layer)
      If code=layer_iso1  Then layer_iso1_size (layer)
      If code=layer_iso2  Then layer_iso2_size (layer)
      If code=layer_hex1  Then layer_hex1_size (layer)
      If code=layer_hex2  Then layer_hex2_size (layer)
      If code=layer_clone Then layer_clone_size(layer)
      If code=layer_image Then layer_image_size(layer)
      If code=layer_point Then layer_width=0     : layer_height=0
      If code=layer_rect  Then layer_width=sizex : layer_height=sizey

      If parax=100 Or toolbar_xpara=0 Then
        px=(posx-50)
        If minx>px Then minx=px
      ElseIf parax>0 Then
        px=(posx-50)*100/parax
        If minx>px Then minx=px
      EndIf

      If paray=100 Or toolbar_xpara=0 Then
        py=(posy-50)
        If miny>py Then miny=py
      ElseIf paray>0 Then
        py=(posy-50)*100/paray
        If miny>py Then miny=py
      EndIf

  End Select

  If toolbar_xmap=1 And (code=layer_map Or code=layer_iso1 Or code=layer_iso2 Or code=layer_hex1 Or code=layer_hex2 Or code=layer_clone) Then
    SetSliderValue editor_sliderx,0
    SetSliderValue editor_slidery,0
  ElseIf code=layer_back Then
    ;nothing
  Else
    SetSliderValue editor_sliderx,-layer_minx+minx
    SetSliderValue editor_slidery,-layer_miny+miny
  EndIf
End Function





;---------------------------------------------------------------------
Function layer_range()
  Local canvash
  Local canvasw
  Local code
  Local corr
  Local i
  Local parax
  Local paray
  Local px
  Local py
  Local posx
  Local posy
  Local sizex
  Local sizey
  Local visible

  canvasw=editor_width()
  canvash=editor_height()

  layer_maxx=+50
  layer_maxy=+50
  layer_minx=-50
  layer_miny=-50

  For i=0 To layer_count
    code   =layer_list[i]\code
    parax  =layer_list[i]\parax
    paray  =layer_list[i]\paray
    posx   =layer_list[i]\posx
    posy   =layer_list[i]\posy
    sizex  =layer_list[i]\sizex
    sizey  =layer_list[i]\sizey
    visible=layer_list[i]\visible=1 Or layer_nr=i

    If visible=1 Then
      Select code
        Case layer_back
          If layer_maxx<sizex+50 Then layer_maxx=sizex+50
          If layer_maxy<sizey+50 Then layer_maxy=sizey+50

        Case layer_line
          If parax=100 Or toolbar_xpara=0 Then
            px=(posx-50)
            If layer_minx>px Then layer_minx=px
            px=(posx+50)
            If layer_maxx<px Then layer_maxx=px
            px=(sizex-50)
            If layer_minx>px Then layer_minx=px
            px=(sizex+50)
            If layer_maxx<px Then layer_maxx=px
          ElseIf parax>0 Then
            px=(posx-50)*100/parax
            If layer_minx>px Then layer_minx=px
            corr=canvasw*(100-parax)/100
            px=(posx+50-corr)*100/parax
            If layer_maxx<px Then layer_maxx=px
            px=(sizex-50)*100/parax
            If layer_minx>px Then layer_minx=px
            corr=canvasw*(100-parax)/100
            px=(sizex+50-corr)*100/parax
            If layer_maxx<px Then layer_maxx=px
          EndIf

          If paray=100 Or toolbar_xpara=0 Then
            py=(posy-50)
            If layer_miny>py Then layer_miny=py
            py=(posy+50)
            If layer_maxy<py Then layer_maxy=py
            py=(sizey-50)
            If layer_miny>py Then layer_miny=py
            py=(sizey+50)
            If layer_maxy<py Then layer_maxy=py
          ElseIf paray>0 Then
            py=(posy-50)*100/paray
            If layer_miny>py Then layer_miny=py
            corr=canvash*(100-paray)/100
            py=(posy+50-corr)*100/paray
            If layer_maxy<py Then layer_maxy=py
            py=(sizey-50)*100/paray
            If layer_miny>py Then layer_miny=py
            corr=canvash*(100-paray)/100
            py=(sizey+50-corr)*100/paray
            If layer_maxy<py Then layer_maxy=py
          EndIf

        Case layer_oval
          If parax=100 Or toolbar_xpara=0 Then
            px=(posx-50-sizex)
            If layer_minx>px Then layer_minx=px
            px=(posx+50+sizex)
            If layer_maxx<px Then layer_maxx=px
          ElseIf parax>0 Then
            px=(posx-50-sizex)*100/parax
            If layer_minx>px Then layer_minx=px
            corr=canvasw*(100-parax)/100
            px=(posx+50+sizex-corr)*100/parax
            If layer_maxx<px Then layer_maxx=px
          EndIf

          If paray=100 Or toolbar_xpara=0 Then
            py=(posy-50-sizey)
            If layer_miny>py Then layer_miny=py
            py=(posy+50+sizey)
            If layer_maxy<py Then layer_maxy=py
          ElseIf paray>0 Then
            py=(posy-50-sizey)*100/paray
            If layer_miny>py Then layer_miny=py
            corr=canvash*(100-paray)/100
            py=(posy+50+sizey-corr)*100/paray
            If layer_maxy<py Then layer_maxy=py
          EndIf

        Case layer_block
          layer_block_pos(layer_list[i])
          layer_block_size(layer_list[i])
          posx=layer_x
          posy=layer_y
          If layer_list[i]\layer<>Null Then
            parax=layer_list[i]\layer\parax
            paray=layer_list[i]\layer\paray
          EndIf

          If parax=100 Or toolbar_xpara=0 Then
            px=(posx-50)
            If layer_minx>px Then layer_minx=px
            px=(posx+50+layer_width)
            If layer_maxx<px Then layer_maxx=px
          ElseIf parax>0 Then
            px=(posx-50)*100/parax
            If layer_minx>px Then layer_minx=px
            corr=canvasw*(100-parax)/100
            px=(posx+50+layer_width-corr)*100/parax
            If layer_maxx<px Then layer_maxx=px
          EndIf

          If paray=100 Or toolbar_xpara=0 Then
            py=(posy-50)
            If layer_miny>py Then layer_miny=py
            py=(posy+50+layer_height)
            If layer_maxy<py Then layer_maxy=py
          ElseIf paray>0 Then
            py=(posy-50)*100/paray
            If layer_miny>py Then layer_miny=py
            corr=canvash*(100-paray)/100
            py=(posy+50+layer_height-corr)*100/paray
            If layer_maxy<py Then layer_maxy=py
          EndIf

        Default
          If code=layer_map   Then layer_map_size  (layer_list[i])
          If code=layer_iso1  Then layer_iso1_size (layer_list[i])
          If code=layer_iso2  Then layer_iso2_size (layer_list[i])
          If code=layer_hex1  Then layer_hex1_size (layer_list[i])
          If code=layer_hex2  Then layer_hex2_size (layer_list[i])
          If code=layer_clone Then layer_clone_size(layer_list[i])
          If code=layer_image Then layer_image_size(layer_list[i])
          If code=layer_point Then layer_width=0     : layer_height=0
          If code=layer_rect  Then layer_width=sizex : layer_height=sizey

          If parax=100 Or toolbar_xpara=0 Then
            px=(posx-50)
            If layer_minx>px Then layer_minx=px
            px=(posx+50+layer_width)
            If layer_maxx<px Then layer_maxx=px
          ElseIf parax>0 Then
            px=(posx-50)*100/parax
            If layer_minx>px Then layer_minx=px
            corr=canvasw*(100-parax)/100
            px=(posx+50+layer_width-corr)*100/parax
            If layer_maxx<px Then layer_maxx=px
          EndIf

          If paray=100 Or toolbar_xpara=0 Then
            py=(posy-50)
            If layer_miny>py Then layer_miny=py
            py=(posy+50+layer_height)
            If layer_maxy<py Then layer_maxy=py
          ElseIf paray>0 Then
            py=(posy-50)*100/paray
            If layer_miny>py Then layer_miny=py
            corr=canvash*(100-paray)/100
            py=(posy+50+layer_height-corr)*100/paray
            If layer_maxy<py Then layer_maxy=py
          EndIf

      End Select
    EndIf
  Next
End Function





;---------------------------------------------------------------------
;empty: 0=no background
;       1=with background
;---------------------------------------------------------------------
Function layer_reset(empty=0)
  Local bank
  Local i
  Local size

  For layer=Each layer
    If layer\code=>0 Then
      If layer\code<>layer_clone Then
        If layer\bank1<>0 Then FreeBank layer\bank1
        If layer\bank2<>0 Then FreeBank layer\bank2
      EndIf
      If layer\bank3<>0 Then FreeBank layer\bank3
      If layer\bank4<>0 Then
        size=BankSize(layer\bank4)
        For i=1 To size/4-1 ;important! (entry 0=total size)
          bank=PeekInt(layer\bank4,i*4)
          FreeBank bank
        Next
        FreeBank layer\bank4
      EndIf
      Delete layer
    EndIf
  Next

  If layer_handle<>0 Then FreeImage layer_handle

  If empty=0 Then
    layer        =New layer
    layer\ascii  =0
    layer\bank1  =0
    layer\bank2  =0
    layer\bank3  =0
    layer\code   =layer_back
    layer\depth1 =0
    layer\depth2 =0
    layer\frame  =0
    layer\layer  =Null
    layer\mask   =0
    layer\mode   =0
    layer\name$  =language$(40)
    layer\parax  =100
    layer\paray  =100
    layer\posx   =0
    layer\posy   =0
    layer\sizex  =0
    layer\sizey  =0
    layer\start  =0
    layer\tile   =Null
    layer\visible=1
    layer_list[0]=layer
  EndIf

  layer_nr     =0
  layer_count  =0
  layer_colour =0
  layer_file$  =""
  layer_handle =0
End Function





;---------------------------------------------------------------------
;layer:   layer handle
;sizex:   layer sizex
;sizey:   layer sizey
;depth1:  layer depth1 (4,8,12,16)
;depth2:  layer depth2 (0,1,2,4,8)
;addleft: add tiles to left (negative=delete)
;addtop:  add tiles to top  (negative=delete)
;---------------------------------------------------------------------
Function layer_resize(layer.layer,sizex,sizey,depth1,depth2,addleft=0,addtop=0)
  Local bank1
  Local bank2
  Local max
  Local mode
  Local offset
  Local problem
  Local value
  Local value2
  Local x
  Local y

  If layer=Null Then Return
  If layer\code<>layer_map And layer\code<>layer_iso1 And layer\code<>layer_iso2 And layer\code<>layer_hex1 And layer\code<>layer_hex2 Then Return
  If layer\sizex=sizex And layer\sizey=sizey And layer\depth1=depth1 And layer\depth2=depth2 And addleft=0 And addtop=0 Then Return
  If layer\sizex<>sizex Or layer\sizey<>sizey Or addleft<>0 Or addtop<>0 Then editor_layer=Null

  If sizex<1 Then sizex=1
  If sizex>layer_maxtiles Then sizex=layer_maxtiles

  If sizey<1 Then sizey=1
  If sizey>layer_maxtiles Then sizey=layer_maxtiles

  max=layer_maxtiles/sizex
  If sizey>max Then sizey=max

  If layer\bank1<>0 And layer\depth1>depth1 Then ;CHECKROUTINES
    problem=0
    For y=0 To sizey-1
      For x=0 To sizex-1
        If x=>addleft And y=>addtop And x<=addleft+layer\sizex-1 And y<=addtop+layer\sizey-1 Then
          If layer\depth1=8 Then
            offset=(y-addtop)*layer\sizex+(x-addleft)
            value =PeekByte(layer\bank1,offset)
          ElseIf layer\depth1=12 Then
            offset=(((y-addtop)*layer\sizex+(x-addleft))*3)/2
            mode  =(((y-addtop)*layer\sizex+(x-addleft)) And 1)*4
            value =(PeekShort(layer\bank1,offset) Shr mode) And 4095
          ElseIf layer\depth1=16 Then
            offset=((y-addtop)*layer\sizex+(x-addleft))*2
            value =PeekShort(layer\bank1,offset)
          EndIf
          If depth1=4  And value>15   Then problem=problem+1
          If depth1=8  And value>255  Then problem=problem+1
          If depth1=12 And value>4095 Then problem=problem+1
        EndIf
      Next
    Next
    If problem>0 Then
      If Confirm(language$(88)+" "+Str$(problem)+" "+language$(90))=0 Then depth1=layer\depth1
    EndIf
  EndIf

  If layer\bank2<>0 And layer\depth2>depth2 And depth2>0 Then ;CHECKROUTINES
    problem=0
    For y=0 To sizey-1
      For x=0 To sizex-1
        If x=>addleft And y=>addtop And x<=addleft+layer\sizex-1 And y<=addtop+layer\sizey-1 Then
          If layer\depth2=2 Then
            offset=(((y-addtop)*layer\sizex+(x-addleft))/4)
            mode  =(((y-addtop)*layer\sizex+(x-addleft)) Mod 4)*2
            value =(PeekByte(layer\bank2,offset) Shr mode) And 3
          ElseIf layer\depth2=4 Then
            offset=(((y-addtop)*layer\sizex+(x-addleft))/2)
            mode  =(((y-addtop)*layer\sizex+(x-addleft)) Mod 2)*4
            value =(PeekByte(layer\bank2,offset) Shr mode) And 15
          ElseIf layer\depth2=8 Then
            offset=(y-addtop)*layer\sizex+(x-addleft)
            value=PeekByte(layer\bank2,offset)
          EndIf
          If depth2=1 And value>1  Then problem=problem+1
          If depth2=2 And value>3  Then problem=problem+1
          If depth2=4 And value>15 Then problem=problem+1
        EndIf
      Next
    Next
    If problem>0 Then
      If Confirm(language$(89)+" "+Str$(problem)+" "+language$(90))=0 Then depth2=layer\depth2
    EndIf
  EndIf

  If layer\bank1<>0 Then
    If depth1=4  Then bank1=CreateBank((sizex*sizey+1)/2)
    If depth1=8  Then bank1=CreateBank(sizex*sizey)
    If depth1=12 Then bank1=CreateBank(sizex*sizey+(sizex*sizey+1)/2)
    If depth1=16 Then bank1=CreateBank(sizex*sizey*2)
    For y=0 To sizey-1
      For x=0 To sizex-1
        If x=>addleft And y=>addtop And x<=addleft+layer\sizex-1 And y<=addtop+layer\sizey-1 Then

          If layer\depth1=4 Then
            offset=(((y-addtop)*layer\sizex+(x-addleft))/2)
            mode  =(((y-addtop)*layer\sizex+(x-addleft)) And 1)*4
            value =(PeekByte(layer\bank1,offset) Shr mode) And 15
          ElseIf layer\depth1=8 Then
            offset=(y-addtop)*layer\sizex+(x-addleft)
            value=PeekByte(layer\bank1,offset)
          ElseIf layer\depth1=12 Then
            offset=(((y-addtop)*layer\sizex+(x-addleft))*3)/2
            mode  =(((y-addtop)*layer\sizex+(x-addleft)) And 1)*4
            value =(PeekShort(layer\bank1,offset) Shr mode) And 4095
          ElseIf layer\depth1=16 Then
            offset=((y-addtop)*layer\sizex+(x-addleft))*2
            value=PeekShort(layer\bank1,offset)
          EndIf

          If depth1=4 Then
            If value>15 Then value=0
            offset=((y*sizex+x)/2)
            mode  =((y*sizex+x) And 1)*4
            value2=PeekByte(bank1,offset)
            PokeByte bank1,offset,value2 Or (value Shl mode)
          ElseIf depth1=8 Then
            If value>255 Then value=0
            offset=y*sizex+x
            PokeByte bank1,offset,value
          ElseIf depth1=12 Then
            If value>4095 Then value=0
            offset=((y*sizex+x)*3)/2
            mode  =((y*sizex+x) And 1)*4
            value2=PeekShort(bank1,offset)
            PokeShort bank1,offset,value2 Or (value Shl mode)
          ElseIf depth1=16 Then
            offset=(y*sizex+x)*2
            PokeShort bank1,offset,value
          EndIf

        EndIf
      Next
    Next
    FreeBank layer\bank1
    layer\bank1=bank1
  EndIf

  If layer\bank2<>0 Then
    If depth2=1 Then bank2=CreateBank((sizex*sizey+7)/8)
    If depth2=2 Then bank2=CreateBank((sizex*sizey+3)/4)
    If depth2=4 Then bank2=CreateBank((sizex*sizey+1)/2)
    If depth2=8 Then bank2=CreateBank(sizex*sizey)
    For y=0 To sizey-1
      For x=0 To sizex-1
        If x=>addleft And y=>addtop And x<=addleft+layer\sizex-1 And y<=addtop+layer\sizey-1 Then

          If layer\depth2=1 Then
            offset=(((y-addtop)*layer\sizex+(x-addleft))/8)
            mode  =(((y-addtop)*layer\sizex+(x-addleft)) Mod 8)
            value =(PeekByte(layer\bank2,offset) Shr mode) And 1
          ElseIf layer\depth2=2 Then
            offset=(((y-addtop)*layer\sizex+(x-addleft))/4)
            mode  =(((y-addtop)*layer\sizex+(x-addleft)) Mod 4)*2
            value =(PeekByte(layer\bank2,offset) Shr mode) And 3
          ElseIf layer\depth2=4 Then
            offset=(((y-addtop)*layer\sizex+(x-addleft))/2)
            mode  =(((y-addtop)*layer\sizex+(x-addleft)) Mod 2)*4
            value =(PeekByte(layer\bank2,offset) Shr mode) And 15
          ElseIf layer\depth2=8 Then
            offset=(y-addtop)*layer\sizex+(x-addleft)
            value =PeekByte(layer\bank2,offset)
          EndIf

          If depth2=1 Then
            If value>1 Then value=0
            offset=((y*sizex+x)/8)
            mode  =((y*sizex+x) Mod 8)
            value2=PeekByte(bank2,offset)
            PokeByte bank2,offset,value2 Or (value Shl mode)
          ElseIf depth2=2 Then
            If value>3 Then value=0
            offset=((y*sizex+x)/4)
            mode  =((y*sizex+x) Mod 4)*2
            value2=PeekByte(bank2,offset)
            PokeByte bank2,offset,value2 Or (value Shl mode)
          ElseIf depth2=4 Then
            If value>15 Then value=0
            offset=((y*sizex+x)/2)
            mode  =((y*sizex+x) Mod 2)*4
            value2=PeekByte(bank2,offset)
            PokeByte bank2,offset,value2 Or (value Shl mode)
          ElseIf depth2=8 Then
            offset=y*sizex+x
            PokeByte bank2,offset,value
          EndIf

        EndIf
      Next
    Next
    FreeBank layer\bank2
    layer\bank2=bank2
  EndIf

  Select layer\code
    Case layer_map
      layer\start=0
    Case layer_iso1
      layer\start=0
    Case layer_iso2
      layer\start=(layer\start+addtop) And 1
      If sizey=1 Then layer\start=0
    Case layer_hex1
      layer\start=(layer\start+addleft) And 1
      If sizex=1 Then layer\start=0
    Case layer_hex2
      layer\start=(layer\start+addtop) And 1
      If sizey=1 Then layer\start=0
  End Select

  layer\depth1=depth1
  layer\depth2=depth2
  layer\sizex=sizex
  layer\sizey=sizey
  map_calc()
End Function





;---------------------------------------------------------------------
;layer: layer handle
;x:     tile pos x
;y:     tile pos y
;value: tile value
;overflow: is value is to great, set max value (=1)
;---------------------------------------------------------------------
Function layer_setdata(layer.layer,x,y,value,overflow=0)
  Local mode
  Local offset
  Local value1
  Local value2



  If layer=Null Then Return
  If layer\bank2=0 Then Return
  If x<0 Or y<0 Or x>layer\sizex-1 Or y>layer\sizey-1 Then Return
  If value<0 Then value=0

  If layer\depth2=1 Then
    If value>1 Then value=overflow*1
    offset=((y*layer\sizex+x)/8)
    mode  =((y*layer\sizex+x) Mod 8)
    value1=PeekByte(layer\bank2,offset)
    value2=(value1 Shr mode) And 1
    PokeByte layer\bank2,offset,value1 Xor (value2 Shl mode) Xor (value Shl mode)
  ElseIf layer\depth2=2 Then
    If value>3 Then value=overflow*3
    offset=((y*layer\sizex+x)/4)
    mode  =((y*layer\sizex+x) Mod 4)*2
    value1=PeekByte(layer\bank2,offset)
    value2=(value1 Shr mode) And 3
    PokeByte layer\bank2,offset,value1 Xor (value2 Shl mode) Xor (value Shl mode)
  ElseIf layer\depth2=4 Then
    If value>15 Then value=overflow*15
    offset=((y*layer\sizex+x)/2)
    mode  =((y*layer\sizex+x) Mod 2)*4
    value2=PeekByte(layer\bank2,offset) And ($F0 Shr mode)
    PokeByte layer\bank2,offset,value2 Or (value Shl mode)
  ElseIf layer\depth2=8 Then
    If value>255 Then value=overflow*255
    offset=y*layer\sizex+x
    PokeByte layer\bank2,offset,value
  EndIf
End Function





;---------------------------------------------------------------------
;layer: layer handle
;x:     tile pos x
;y:     tile pos y
;value: tile value
;code:  0=void, 1=frame, 2=anim
;---------------------------------------------------------------------
Function layer_setvalue(layer.layer,x,y,value,code)
  Local anims
  Local count
  Local mode
  Local offset
  Local value2


  If layer=Null Then Return
  If layer\bank1=0 Then Return
  If x<0 Or y<0 Or x>layer\sizex-1 Or y>layer\sizey-1 Then Return

  If layer\tile<>Null Then
    anims=layer\tile\anims
    count=layer\tile\count
  EndIf

  If code=1 Then
    If value<1 Or value>count Then value=0
    If value>0 Then value=value+anims
  ElseIf code=2 Then
    If value<1 Or value>anims Then value=0
  Else
    value=0
  EndIf

  If layer\depth1=4 Then
    If value>15 Then value=0
    offset=((y*layer\sizex+x)/2)
    mode  =((y*layer\sizex+x) And 1)*4
    value2=PeekByte(layer\bank1,offset) And ($F0 Shr mode)
    PokeByte layer\bank1,offset,value2 Or (value Shl mode)
  ElseIf layer\depth1=8 Then
    If value>255 Then value=0
    offset=y*layer\sizex+x
    PokeByte layer\bank1,offset,value
  ElseIf layer\depth1=12 Then
    If value>4095 Then value=0
    offset=((y*layer\sizex+x)*3)/2
    mode  =((y*layer\sizex+x) And 1)
    value2=PeekShort(layer\bank1,offset) And ($F000 Shr mode*12)
    PokeShort layer\bank1,offset,value2 Or (value Shl mode*4)
  ElseIf layer\depth1=16 Then
    If value>65535 Then value=0
    offset=(y*layer\sizex+x)*2
    PokeShort layer\bank1,offset,value
  EndIf
End Function





;---------------------------------------------------------------------
;layer: layer handle
;x:     tile pos x
;y:     tile pos y
;value: tile value
;---------------------------------------------------------------------
Function layer_setvalue2(layer.layer,x,y,value)
  Local anims
  Local count
  Local mode
  Local offset
  Local value2


  If layer=Null Then Return
  If layer\bank1=0 Then Return
  If x<0 Or y<0 Or x>layer\sizex-1 Or y>layer\sizey-1 Then Return
  If value<0 Then value=0

  If layer\depth1=4 Then
    If value>15 Then value=0
    offset=((y*layer\sizex+x)/2)
    mode  =((y*layer\sizex+x) And 1)*4
    value2=PeekByte(layer\bank1,offset) And ($F0 Shr mode)
    PokeByte layer\bank1,offset,value2 Or (value Shl mode)
  ElseIf layer\depth1=8 Then
    If value>255 Then value=0
    offset=y*layer\sizex+x
    PokeByte layer\bank1,offset,value
  ElseIf layer\depth1=12 Then
    If value>4095 Then value=0
    offset=((y*layer\sizex+x)*3)/2
    mode  =((y*layer\sizex+x) And 1)
    value2=PeekShort(layer\bank1,offset) And ($F000 Shr mode*12)
    PokeShort layer\bank1,offset,value2 Or (value Shl mode*4)
  ElseIf layer\depth1=16 Then
    If value>65535 Then value=0
    offset=(y*layer\sizex+x)*2
    PokeShort layer\bank1,offset,value
  EndIf
End Function





;---------------------------------------------------------------------
;nr: layer nr
;---------------------------------------------------------------------
Function layer_up(nr)
  If nr<1 Then Return
  If nr>layer_count-1 Then Return

  layer=layer_list[nr]
  layer_list[nr]=layer_list[nr+1]
  layer_list[nr+1]=layer
  list_select(nr+1)
End Function