;Changelog Jan

;02.11.2019
;Layer werden nun mit der passenden BitTiefe erstellt.

;31.10.2019
;Layer.bb undo, erase undo. und das klappt sogar
;mapeditor.bb Strg+z für undo
;toolbar.bb Undobutton eingefügt.

;15.10.2019
;setup.bb setup_Tilezoom zum einstellen ob die Tiles Skaliert werden sollen Auswählbar aus den Einstellungen
;menu.bb Skalierung benutzen
;window.bb Fenster initialgröße
;splitter.bb splitter_update(mode) geht nun bis 1200 zu ziehen 
;tile_prog.bb toolbar Tileeditor repariert


;14.10.2019
;language.ini &toolbar.bb -> hinttext für neue Symbole
;editor_click &editor_draw $ Event $0201  Radierer
;Verison als Const von map.bb nach mapeditor.bb und auf 1.411 gesetzt
;map_save & mapeditor.bb & Event $4001 Timer Hinzugefügt für das Automatische speichern von Änderungen

;13.10.2019
;editor.bb & editor_zoom Einstellbare Zoomstufen aus der Config
;toolbar.bb Toolbar Nummerierung geändert
;toolbar.bb & map.bb & maps_saveas Speichern zu überschreiben geändert und speichern unter erstellt


;12.10.2019
;map.bb Version auf 1.41 gesetzt
;toolbar.bb Toolbar repariert
;Event $0204 Mausrad zum Zoomen
;extra_splash HTML Gadget entfernt (Machte probleme bei Windows 10)
;editor_slide Sliden nun richtig herum
;event $0201 & event $0202 - 2. Maustaste zum scrollen

;22.10.2023
;mapeditor.bb Version 1.413
;editor.bb Verschieben umgedreht, wie im Tileset.
;setup.bb LAyer-Fenster vergrößert und Button eingefügt.
;Language.ini eintrag 287
;Weiter unter: setup.bb DebugLog("Hier Jan!")

;23.10.2023
;export.bb angelegt


;<<<<<<<<<TODO Jan_
;Skalierung im Tileeditor
;Menü Auswahl der Tiles kein Leer mehr.
;Auswahltool bei Radierer?!
;blitzmax engine zu BlitzmaxNG convertieren
;eingine für Cerberus X

;<<<<<<<<<TODO The Shadow
;Converter-tool???



;.lib "user32.dll" 
;SendMessage%(hwnd%, msg%, wParam%, mParam%):"SendMessageA"
;LoadCursorFromFile%(filename$):"LoadCursorFromFileA"
;SetCursor%( ID ):"SetCursor" 

;                 FREE  PRO
;demo             1     0
;layer_maxcount   10    5000
;layer_maxtiles   10000 100000000
;tile_maxtilesets 10    500

Const UMEVersion$="1.413"
Const demo=0  ;0=fullversion, 1=freeversion
Const build=3
Global vram=2
Global autosavetimer = CreateTimer(0.005) ;alle 200 Sekunden Ticken zum speichern
Global autoundotimer = CreateTimer(0.1)
Global doautosave%=1

Dim language$(300)

Include "source\clip.bb"
Include "source\crc32.bb"
Include "source\crypt.bb"
Include "source\editor.bb"
Include "source\export.bb"
Include "source\extra.bb"
Include "source\layer.bb"
Include "source\layer_block.bb"
Include "source\layer_clone.bb"
Include "source\layer_hex1.bb"
Include "source\layer_hex2.bb"
Include "source\layer_image.bb"
Include "source\layer_iso1.bb"
Include "source\layer_iso2.bb"
Include "source\layer_line.bb"
Include "source\layer_map.bb"
Include "source\layer_oval.bb"
Include "source\layer_point.bb"
Include "source\layer_rect.bb"
Include "source\list.bb"
Include "source\maker.bb"
Include "source\map.bb"
Include "source\md5.bb"
Include "source\memory.bb"
Include "source\menu.bb"
Include "source\meta.bb"
Include "source\optimize.bb"
Include "source\setup.bb"
Include "source\sha1.bb"
Include "source\splitter.bb"
Include "source\tile_editor.bb"
Include "source\tile_menu.bb"
Include "source\tile_prog.bb"
Include "source\toolbar.bb"
Include "source\var.bb"
Include "source\window.bb"



ChangeDir map_app$

setup_start()
window_start()
toolbar_start()
list_start()
menu_start()
editor_start()
splitter_start()
maker_start()
map_new()

Dim cursor(7)
For i=0 To 7
  cursor(i)=LoadCursorFromFile("image\"+Str$(i)+".cur")
Next

HotKeyEvent 001,0,$110  ;ESC
HotKeyEvent 031,2,$111  ;CONTROL+S
HotKeyEvent 031,3,$111  ;CONTROL+S+SHIFT
HotKeyEvent 038,2,$112  ;CONTROL+L
HotKeyEvent 048,2,$113  ;CONTROL+B
HotKeyEvent 019,2,$114  ;CONTROL+R
HotKeyEvent 049,2,$115  ;CONTROL+N
HotKeyEvent 018,2,$116  ;CONTROL+E
HotKeyEvent 021,2,$117  ;CONTROL+Z


If demo=1 Then extra_splash()

If Trim$(CommandLine$())<>"" Then map_load2(Trim$(CommandLine$()))





Repeat
  window_status()
  WaitEvent()

  If setup_mouse=1 And EventSource()=editor_canvas Then SetCursor cursor(toolbar_xtool)
  Select EventID()
    Case $0101 ;keydown-----------------------------------------------
      mode=1-KeyDown(29)*2 ;left control
      key=EventData()
      Select key
        Case 072,073 ;num8,num9
          If (layer\code=layer_map And key=72) Or (layer\code=layer_iso1 And key=73) Or (layer\code=layer_iso2 And key=72) Or (layer\code=layer_hex1 And key=72) Or (layer\code=layer_hex2 And key=72) Then
            layer_resize(layer,layer\sizex,layer\sizey+mode,layer\depth1,layer\depth2,0,mode)
            editor_update()
          ElseIf layer\code=layer_block And key=72 Then
            layer\posy=layer\posy-1
            editor_update()
          EndIf
        Case 075,071 ;num4,num7
          If (layer\code=layer_map And key=75) Or (layer\code=layer_iso1 And key=71) Or (layer\code=layer_iso2 And key=75) Or (layer\code=layer_hex1 And key=75) Or (layer\code=layer_hex2 And key=75) Then
            layer_resize(layer,layer\sizex+mode,layer\sizey,layer\depth1,layer\depth2,mode,0)
            editor_update()
          ElseIf layer\code=layer_block And key=75 Then
            layer\posx=layer\posx-1
            editor_update()
          EndIf
        Case 077,081 ;num6,num3
          If (layer\code=layer_map And key=77) Or (layer\code=layer_iso1 And key=81) Or (layer\code=layer_iso2 And key=77) Or (layer\code=layer_hex1 And key=77) Or (layer\code=layer_hex2 And key=77) Then
            layer_resize(layer,layer\sizex+mode,layer\sizey,layer\depth1,layer\depth2,0,0)
            editor_update()
          ElseIf layer\code=layer_block And key=77 Then
            layer\posx=layer\posx+1
            editor_update()
          EndIf
        Case 080,079 ;num2,num1
          If (layer\code=layer_map And key=80) Or (layer\code=layer_iso1 And key=79) Or (layer\code=layer_iso2 And key=80) Or (layer\code=layer_hex1 And key=80) Or (layer\code=layer_hex2 And key=80) Then
            layer_resize(layer,layer\sizex,layer\sizey+mode,layer\depth1,layer\depth2,0,0)
            editor_update()
          ElseIf layer\code=layer_block And key=80 Then
            layer\posy=layer\posy+1
            editor_update()
          EndIf

        Case 014 ;backspace
          If setup_confirm=0 Or layer_nr=0 Then
            layer_delete(layer_nr)
          Else
            If Confirm(language$(136))=1 Then layer_delete(layer_nr)
          EndIf
        Case 059 ;F1
          extra_help()
        Case 060 ;F2
          clip_cut()
        Case 061 ;F3
          clip_copy()
        Case 062 ;F4
          clip_paste()
        Case 063 ;F5
          toolbar_xmap=1-toolbar_xmap
          toolbar_check(toolbar,toolbar_map,toolbar_xmap)
          editor_update()
        Case 064 ;F6
          toolbar_xzoom=1-toolbar_xzoom
          toolbar_check(toolbar,toolbar_zoom,toolbar_xzoom)
          editor_update()
        Case 065 ;F7
          toolbar_xgrid=1-toolbar_xgrid
          toolbar_check(toolbar,toolbar_grid,toolbar_xgrid)
          editor_update(2)
        Case 066 ;F8
          toolbar_xpara=1-toolbar_xpara
          toolbar_check(toolbar,toolbar_para,toolbar_xpara)
          editor_update()
        Case 067 ;F9
          tile_fullupdate()
        Case 087 ;F11
          tile_prog(tile)
        Case 088 ;F12
          setup_size()
        Case 074 ;minus (up)
          layer_up(layer_nr)
        Case 078 ;plus (down)
          layer_down(layer_nr)
        Case 082 ;num0
          SetSliderValue editor_sliderx,0-layer_minx
          SetSliderValue editor_slidery,0-layer_miny
          editor_update(2)
        Case 201 ;pageup
          list_select(layer_nr+1)
        Case 209 ;pagedown
          list_select(layer_nr-1)
        Case 210 ;insert
          layer_add()
        Case 211 ;delete
          If editor_layer<>Null Then clip_delete()
      End Select


    Case $0103 ;keystocke---------------------------------------------
      key=EventData()
      Select key
        Case $F700 ;up
          canvash=editor_height()
          posy=SliderValue(editor_slidery)
          SetSliderValue editor_slidery,posy-canvash/2
          editor_update(2)
        Case $F701 ;down
          canvash=editor_height()
          posy=SliderValue(editor_slidery)
          SetSliderValue editor_slidery,posy+canvash/2
          editor_update(2)
        Case $F702 ;left
          canvasw=editor_width()
          posx=SliderValue(editor_sliderx)
          SetSliderValue editor_sliderx,posx-canvasw/2
          editor_update(2)
        Case $F703 ;right
          canvasw=editor_width()
          posx=SliderValue(editor_sliderx)
          SetSliderValue editor_sliderx,posx+canvasw/2
          editor_update(2)
      End Select


    Case $0110 ;ESC---------------------------------------------------
      menu_selected=0
      menu_update()


    Case $0111 ;CONTROL+S---------------------------------------------
      map_save()


    Case $0112 ;CONTROL+L---------------------------------------------
      map_load()


    Case $0113 ;CONTROL+B---------------------------------------------
      map_backup()


    Case $0114 ;CONTROL+R---------------------------------------------
      map_restore()


    Case $0115 ;CONTROL+N---------------------------------------------
      If setup_confirm=0 Then
        map_new()
      Else
        If Confirm(language$(87))=1 Then map_new()
      EndIf


    Case $0116 ;CONTROL+E---------------------------------------------
      extra_export()
    Case $0117 ;CONTROL+Z---------------------------------------------
      layer_undo()    
		list_update()
		;list_click()

    Case $0201 ;mousedown---------------------------------------------
	  
      If EventSource()=menu_canvas   Then DebugLog ("menu_canvas")
      If EventSource()=editor_canvas Then DebugLog ("editor_canvas")
      If EventSource()=editor_scroll Then DebugLog ("editor_scroll")
      If EventSource()=list_canvas   Then DebugLog ("list_canvas")
      If EventSource()=menu_button   Then DebugLog ("menu_button")
      If EventSource()=splitter      Then DebugLog ("splitter")

	  
      If EventSource()=menu_canvas   And EventData()<2 Then menu_click()
      If EventSource()=menu_canvas   And EventData()=2 Then menu_slide(0)
      If EventSource()=editor_canvas And EventData()<2 Then editor_click(0)
      If EventSource()=editor_canvas And EventData()=2 Then editor_slide(0)
      If EventSource()=editor_scroll Then editor_scroll(0)
      If EventSource()=list_canvas   Then list_click()
      If EventSource()=menu_button   Then menu_switch()
      If EventSource()=splitter      Then splitter_update(0)


    Case $0202 ;mouseup-----------------------------------------------
      If EventSource()=menu_canvas And EventData()=2 Then menu_slide(1)
      If EventSource()=editor_canvas And EventData()<2 Then
		editor_click(1)
		map_save(1)
	  EndIf
      If EventSource()=editor_canvas And EventData()=2 Then editor_slide(1)
      If EventSource()=editor_scroll Then editor_scroll(1)
      If EventSource()=splitter      Then splitter_update(1)

      If editor_refresh=1 Then
        If layer\code=layer_rect Then
          If layer\sizex<0 Then
            layer\sizex=-layer\sizex
            layer\posx=layer\posx-layer\sizex
          EndIf
          If layer\sizey<0 Then
            layer\sizey=-layer\sizey
            layer\posy=layer\posy-layer\sizey
          EndIf
        EndIf

        editor_refresh=0
        editor_update(0)
      EndIf


    Case $0203 ;mousemove---------------------------------------------
      If EventSource()=menu_canvas And menu_action=1 Then menu_slide(2)
      If EventSource()=editor_canvas And editor_action1=1 Then editor_click(2)
      If EventSource()=editor_canvas And editor_action3=1 Then editor_slide(2)
      If EventSource()=editor_scroll Then editor_scroll(2)
      If EventSource()=splitter      Then splitter_update(2)


    Case $0204 ;mousewheel--------------------------------------------
      Select extra_canvas
        Case editor_canvas
           Local zspeed#=MouseZSpeed()
           If zspeed > 0 Then
	           toolbar_xzoom=1
		   EndIf	
		   If zspeed < 0 Then
		       toolbar_xzoom=0
		   EndIf

           toolbar_check(toolbar,toolbar_zoom,toolbar_xzoom)
           editor_update()
;          pos=SliderValue(editor_slidery)
;          SetSliderValue editor_slidery,pos-MouseZSpeed()*20
;          editor_update(2)

        Case list_canvas
          pos=SliderValue(list_slider)
          SetSliderValue list_slider,pos-MouseZSpeed()*20
          list_update()

        Case menu_canvas
          pos=SliderValue(menu_slidery)
          SetSliderValue menu_slidery,pos-MouseZSpeed()*20
          menu_update(1)
      End Select


    Case $0205 ;mouseenter--------------------------------------------
      extra_canvas=EventSource()


    Case $0206 ;mouseleave--------------------------------------------


    Case $0401 ;gadgetevent-------------------------------------------
      Select EventSource()
        Case editor_sliderx
          editor_update(2)


        Case editor_slidery
          editor_update(2)

        Case list_slider
          list_update()

        Case menu
          menu_selected=0
          menu_update()
          editor_update(2)

        Case menu_sliderx
          menu_update(1)

        Case menu_slidery
          menu_update(1)

        Case toolbar
;          Notify EventData()
          Select EventData()
            Case toolbar_new
              If setup_confirm=0 Then
                map_new()
              Else
                If Confirm(language$(87))=1 Then map_new()
              EndIf
            Case toolbar_load
              map_load()
            Case toolbar_save
              map_save()
            Case toolbar_saveas
              map_saveas()
            Case toolbar_cut
              clip_cut()
            Case toolbar_copy
              clip_copy()
            Case toolbar_paste
              clip_paste()
            Case toolbar_add
              layer_add()
            Case toolbar_del
              If setup_confirm=0 Or layer_nr=0 Then
                layer_delete(layer_nr)
              Else
                If Confirm(language$(136))=1 Then layer_delete(layer_nr)
              EndIf
            Case toolbar_up
              layer_up(layer_nr)
            Case toolbar_down
              layer_down(layer_nr)
            Case toolbar_undo
            	layer_undo()    
				list_update()
            Case toolbar_edit
              list_setup(layer_nr)
            Case toolbar_select
              toolbar_select(toolbar_select)
            Case toolbar_mark
              toolbar_select(toolbar_mark)
            Case toolbar_draw
              toolbar_select(toolbar_draw)
            Case toolbar_radierer
              toolbar_select(toolbar_radierer)
            Case toolbar_rect
              toolbar_select(toolbar_rect)
            Case toolbar_block
              toolbar_select(toolbar_block)
            Case toolbar_seq
              toolbar_select(toolbar_seq)
            Case toolbar_fill
              toolbar_select(toolbar_fill)
            Case toolbar_magic
              toolbar_select(toolbar_magic)
            Case toolbar_map
              SetSliderValue editor_sliderx,0
              SetSliderValue editor_slidery,0
              toolbar_xmap=1-toolbar_xmap
              toolbar_check(toolbar,toolbar_map,toolbar_xmap)
              editor_update()
            Case toolbar_zoom
              toolbar_xzoom=1-toolbar_xzoom
              toolbar_check(toolbar,toolbar_zoom,toolbar_xzoom)
              editor_update()
            Case toolbar_grid
              toolbar_xgrid=1-toolbar_xgrid
              toolbar_check(toolbar,toolbar_grid,toolbar_xgrid)
              editor_update(2)
            Case toolbar_para
              toolbar_xpara=1-toolbar_xpara
              toolbar_check(toolbar,toolbar_para,toolbar_xpara)
              editor_update()
            Case toolbar_tile
              tile_prog(tile)
            Case toolbar_meta
              meta_prog()
            Case toolbar_var
              var_prog()
            Case toolbar_size
              setup_size()
            Case toolbar_optim
              optimize_prog()
            Case toolbar_maker
              maker_prog()
            Case toolbar_view
              extra_preview()
            Case toolbar_setup
              setup_prog()
            Case toolbar_help
              extra_help()
          End Select
      End Select


    Case $0802 ;windowsize--------------------------------------------
      editor_resize()
      menu_resize()


    Case $0803 ;windowclose-------------------------------------------
      If setup_confirm=0 Then
        Exit
      Else
        If Confirm(language$(87))=1 Then Exit
      EndIf

	Case $4001 ; Timerticks-------------------------------------------
		Select EventSource()
		Case  autosavetimer 
	  		doautosave=1
		Case  autoundotimer
	  		layer_createundo()
	 	End Select	

  End Select
Forever



setup_exit()
map_new()
maker_exit()
splitter_exit()
editor_exit()
menu_exit()
list_exit()
toolbar_exit()
window_exit()