Global toolbar       =00
Global toolbar_height=00

Global toolbar_width =00
Global toolbar_xgrid =01

Global toolbar_xmap  =00

Global toolbar_xzoom =00
Global toolbar_xpara =01

Global toolbar_xtool =00

Global toolbar_new   =-1
Global toolbar_load  =toolbar_new+1
Global toolbar_save  =toolbar_load +1
Global toolbar_saveas=toolbar_save +1


Global toolbar_undo   =toolbar_saveas +2
Global toolbar_cut   =toolbar_undo +1
Global toolbar_copy  =toolbar_cut +1
Global toolbar_paste =toolbar_copy +1

Global toolbar_add   =toolbar_paste +2
Global toolbar_del   =toolbar_add +1
Global toolbar_up    =toolbar_del +1
Global toolbar_down  =toolbar_up +1
Global toolbar_edit  =toolbar_down +1

Global toolbar_select=toolbar_edit +2
Global toolbar_mark  =toolbar_select +1
Global toolbar_draw  =toolbar_mark +1
Global toolbar_radierer=toolbar_draw  +1
Global toolbar_rect  =toolbar_radierer+1
Global toolbar_block =toolbar_rect +1
Global toolbar_seq   =toolbar_block +1
Global toolbar_fill  =toolbar_seq +1
Global toolbar_magic =toolbar_fill +1

Global toolbar_map   =toolbar_magic +2
Global toolbar_zoom  =toolbar_map +1
Global toolbar_grid  =toolbar_zoom +1
Global toolbar_para  =toolbar_grid +1

Global toolbar_tile  =toolbar_para +2
Global toolbar_meta  =toolbar_tile +1
Global toolbar_var   =toolbar_meta +1
Global toolbar_size  =toolbar_var +1
Global toolbar_optim =toolbar_size +1
Global toolbar_maker =toolbar_optim +1
Global toolbar_view  =toolbar_maker +1

Global toolbar_setup =toolbar_view +2
Global toolbar_help  =toolbar_setup +1





;---------------------------------------------------------------------
;toolbar: toolbar handle
;pos:     button position
;checked: 0=no, 1=yes
;enabled: 0=no, 1=yes
;---------------------------------------------------------------------
Function toolbar_check(toolbar,pos,checked,enabled=1)
  Local id

  id=QueryObject(toolbar,1)
  SendMessage(id,$401,pos+1,enabled)
  SendMessage(id,$402,pos+1,checked)
  SendMessage(id,$403,pos+1,0)
End Function





;---------------------------------------------------------------------
Function toolbar_exit()
  FreeGadget toolbar
End Function





;---------------------------------------------------------------------
;pos=selected tool
;---------------------------------------------------------------------
Function toolbar_select(pos)
  toolbar_check(toolbar,toolbar_select,0)
  toolbar_check(toolbar,toolbar_mark,0)
  toolbar_check(toolbar,toolbar_draw,0)
  toolbar_check(toolbar,toolbar_radierer,0)
  toolbar_check(toolbar,toolbar_rect,0)
  toolbar_check(toolbar,toolbar_block,0)
  toolbar_check(toolbar,toolbar_seq,0)
  toolbar_check(toolbar,toolbar_fill,0)
  toolbar_check(toolbar,toolbar_magic,0)
  toolbar_check(toolbar,pos,1)

  toolbar_xtool=pos-toolbar_select

  If editor_layer<>Null Then
    editor_layer=Null
    editor_update(2)
  EndIf
End Function





;---------------------------------------------------------------------
Function toolbar_start()
  toolbar       =CreateToolBar("image/toolbar1.bmp",0,0,window_width,16,window)
  toolbar_width =GadgetWidth  (toolbar)
  toolbar_height=GadgetHeight (toolbar)

  toolbar_check(toolbar,toolbar_select,1)
  toolbar_check(toolbar,toolbar_grid,1)
  toolbar_check(toolbar,toolbar_para,1)

  DisableToolBarItem toolbar,toolbar_undo   -1
  DisableToolBarItem toolbar,toolbar_add   -1
  DisableToolBarItem toolbar,toolbar_select-1
  DisableToolBarItem toolbar,toolbar_map   -1
  DisableToolBarItem toolbar,toolbar_tile  -1
  DisableToolBarItem toolbar,toolbar_setup -1

  SetToolBarTips toolbar,language$(1)+","+language$(2)+","+language$(3)+","+language$(283)+",,"+language$(286)+","+language$(4)+","+language$(5)+","+language$(6)+",,"+language$(7)+","+language$(8)+","+language$(9)+","+language$(10)+","+language$(11)+",,"+language$(12)+","+language$(13)+","+language$(14)+","+language$(284)+","+language$(15)+","+language$(16)+","+language$(17)+","+language$(18)+","+language$(19)+",,"+language$(20)+","+language$(21)+","+language$(22)+","+language$(23)+",,"+language$(24)+","+language$(25)+","+language$(26)+","+language$(27)+","+language$(28)+","+language$(29)+","+language$(30)+",,"+language$(31)+","+language$(32)
End Function