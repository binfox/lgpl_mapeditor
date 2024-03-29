Dim crc32_table(255)




; ---------------------------------------------------------------------
Procedure crc32_init()
  Protected i.l, j.l, value.l
  
  For i = 0 To 255
    value = i
    For j = 0 To 7
      If (value & 1)
        value = (value >> 1) ! $EDB88320
      Else
        value >> 1
      EndIf
    Next
    crc32_table(i) = value
  Next
EndProcedure





; ---------------------------------------------------------------------
;name:   file name
;RETURN: crc32 value
; ---------------------------------------------------------------------
Procedure crc32_file(name.s)
  Protected bank.l, byte.l, bytes.l, crc32.l, file.l, i.l, j.l, size.l
  
  crc32_init()
  crc32 = $FFFFFFFF
  
  size = FileSize(name)
  If size < 8 : ProcedureReturn #False : EndIf
  
  file = ReadFile(#PB_Any, name)
  If file = 0 : ProcedureReturn #False : EndIf
  FileSeek(8)
  bank = AllocateMemory($4000)
  
  For j = 1 To (size - 8) / $4000
    ReadData(bank, $4000)
    For i = 0 To $3FFF
      byte = PeekB(bank + i) & $FF
      crc32 = (crc32 >> 8) ! crc32_table(byte ! (crc32 And $FF))
    Next
  Next
  
  bytes = (size - 8) % $4000
  If bytes > 0
    ReadData(bank, bytes)
    For i = 0 To bytes - 1
      byte = PeekB(bank + i) & $FF
      crc32 = (crc32 >> 8) ! crc32_table(byte ! (crc32 & $FF))
    Next
  EndIf
  
  CloseFile(file)
  FreeMemory(bank)
  ProcedureReturn ~crc32
EndProcedure 
; jaPBe Version=1.4.4.25
; FoldLines=00060014001E0040
; Build=0
; FirstLine=0
; CursorPosition=5
; ExecutableFormat=Windows
; DontSaveDeclare
; EOF