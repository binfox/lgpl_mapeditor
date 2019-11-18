Global splitter
Global splitter_action





;---------------------------------------------------------------------
Function splitter_exit()
  FreeGadget splitter
End Function





;---------------------------------------------------------------------
Function splitter_start()
  Local col
  Local height
  Local i

  splitter=CreateCanvas(229,toolbar_height,3,window_height-toolbar_height,window)
  height=GadgetHeight(splitter)
  SetGadgetLayout splitter,1,0,1,1
  SetBuffer CanvasBuffer(splitter)

  For i=0 To height/3
    col=192-(i And 1)*80
    Color col,col,col
    Rect 0,i*3,3,3
  Next

  FlipCanvas splitter
End Function





;---------------------------------------------------------------------
;mode: 0=mousedown, 1=mouseup, 2=mousemove ???
;---------------------------------------------------------------------
Function splitter_update(mode)
  Local x

  If mode=0 Then splitter_action=1
  If mode=1 Then splitter_action=0
  If mode<>2 Or splitter_action=0 Then Return

  x=GadgetX(splitter)+EventX()-1
  If x<190 Then x=190
  If x>1200 Then x=1200
  If x=GadgetX(splitter) Then Return

  window_width =ClientWidth(window)
  window_height=ClientHeight(window)

  SetGadgetShape splitter,x,toolbar_height,3,window_height-toolbar_height

  SetGadgetShape list,0,toolbar_height,x-2,227
  list_resize()

  SetGadgetShape menu,0,toolbar_height+229,x-2,window_height-toolbar_height-229
  menu_resize()

  SetGadgetShape editor,x+5,toolbar_height,window_width-x-5,window_height-toolbar_height
  editor_resize()
End Function