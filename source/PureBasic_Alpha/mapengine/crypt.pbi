Dim crypt_table(63)





; ---------------------------------------------------------------------
;passw: password string
; ---------------------------------------------------------------------
Procedure crypt_init(passw.s)
  Protected ascii.l, byte.l, i.l, j.l, key.s, pos.l
  
  key = md5(passw)
  
  For j = 0 To 15
    For i = 0 To 15
      pos = (j * 16 + i) / 4
      ascii = Asc(Mid(key, i + 1, 1))
      byte = ascii << ((i % 4) * 8)
      crypt_table(pos) = crypt_table(pos) | byte
    Next
    key = md5(key)
  Next
EndProcedure






; ---------------------------------------------------------------------
;source: source file
;dest:   dest file
;passw:  password string
; ---------------------------------------------------------------------
Procedure crypt_file(source.s, dest.s, passw.s)
  Protected bank.l, bytes.l, file1.l, file2.l, size.l, i.l, j.l, pos.l, value.l
  
  crypt_init(passw)
  
  size = FileSize(source)
  If size < 64 : ProcedureReturn #False : EndIf
  
  file1 = ReadFile(#PB_Any, source)
  If file1 = 0 : ProcedureReturn #False : EndIf
  
  file2 = CreateFile(#PB_Any, dest)
  If file2 = 0 : CloseFile(file1) : ProcedureReturn #False : EndIf
  
  bank = AllocateMemory($4000)
  UseFile(file1)
  ReadData(bank, 64)
  UseFile(file2)
  WriteData(bank, 64)
  
  For j = 1 To (size - 64) / $4000
    UseFile(file1)
    ReadData(bank, $4000)
    For i = 0 To $3FFF Step 4
      value = PeekL(bank + i)
      PokeL(bank + i, value ! crypt_table(pos))
      pos = (pos + 1) % 64
    Next
    UseFile(file2)
    WriteData(bank, $4000)
  Next
  
  bytes = (size - 64) % $4000
  If bytes > 0
    UseFile(file1)
    ReadData(bank, bytes)
    For i = 0 To bytes - 1 Step 4
      value = PeekL(bank + i)
      PokeL(bank + i, value ! crypt_table(pos))
      pos = (pos + 1) % 64
    Next
    UseFile(file2)
    WriteData(bank, bytes)
  EndIf
  
  CloseFile(file1)
  CloseFile(file2)
  FreeMemory(bank)
EndProcedure





; ---------------------------------------------------------------------
;name: tmp file name
; ---------------------------------------------------------------------
Procedure crypt_overwrite(name.s)
  Protected bank.l, bytes.l, file.l, i.l, size.l
  
  size = FileSize(name)
  file = OpenFile(name)
  If file = 0 : ProcedureReturn #False : EndIf
  
  bank = CreateBank($4000)
  For i = 0 To $3FFF
    PokeB(bank + i, Random(255))
  Next
  
  For i = 1 To size / $4000
    WriteData(bank, $4000)
  Next
  
  bytes = size % $4000
  If bytes > 0 : WriteData(bank, bytes) : EndIf
  
  CloseFile(file)
  FreeMemory(bank)
EndProcedure 
; jaPBe Version=1.4.4.25
; FoldLines=0009001700230053005C0071
; Build=0
; CompileThis=..\preview_bbplus.pb
; FirstLine=0
; CursorPosition=92
; ExecutableFormat=Windows
; DontSaveDeclare
; EOF