Dim crypt_table(63)





;---------------------------------------------------------------------
;source: source file
;dest:   dest file
;passw:  password string
;---------------------------------------------------------------------
Function crypt_file(source$,dest$,passw$)
  Local bank
  Local bytes
  Local file1
  Local file2
  Local fsize
  Local i
  Local j
  Local pos
  Local value

  crypt_init(passw$)

  fsize=FileSize(source$)
  If fsize<64 Then Return

  file1=ReadFile(source$)
  If file1=0 Then Return

  file2=WriteFile(dest$)
  If file2=0 Then CloseFile file1 : Return

  bank=CreateBank($4000)
  ReadBytes  bank,file1,0,64
  WriteBytes bank,file2,0,64

  For j=1 To (fsize-64)/$4000
    ReadBytes bank,file1,0,$4000
    For i=0 To $3FFF Step 4
      value=PeekInt(bank,i)
      PokeInt bank,i,(value Xor crypt_table(pos))
      pos=(pos+1) Mod 64
    Next
    WriteBytes bank,file2,0,$4000
  Next

  bytes=(fsize-64) Mod $4000
  If bytes>0 Then
    ReadBytes bank,file1,0,bytes
    For i=0 To bytes-1 Step 4
      value=PeekInt(bank,i)
      PokeInt bank,i,(value Xor crypt_table(pos))
      pos=(pos+1) Mod 64
    Next
    WriteBytes bank,file2,0,bytes
  EndIf

  CloseFile file1
  CloseFile file2
  FreeBank bank
End Function





;---------------------------------------------------------------------
;passw: password string
;---------------------------------------------------------------------
Function crypt_init(passw$)
  Local ascii
  Local byte
  Local i
  Local j
  Local key$
  Local pos

  key$=md5$(passw$)

  For j=0 To 15
    For i=0 To 15
      pos=(j*16+i)/4
      ascii=Asc(Mid$(key$,i+1,1))
      byte=ascii Shl ((i Mod 4)*8)
      crypt_table(pos)=crypt_table(pos) Or byte
    Next
    key$=md5$(key$)
  Next
End Function